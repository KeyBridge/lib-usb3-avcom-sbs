package com.keybridgeglobal.sensor.avcom.adapter;

import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.avcom.datagram.HardwareDescriptionRequest;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.Debug;
import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class USBSerialPortIdentifier implements SerialPortEventListener {

  private byte[] byteBuffer;
  private int byteBufferIndex;
  private final Debug d = new Debug(false);
  private InputStream inputStream;
  private boolean isAvcomPort;
  private OutputStream outputStream;
  private SerialPort serialPort;

  /**
   * Static method to find the first available attached Avcom device.
   * <p>
   * @return the communications port identifier
   */
  public static CommPortIdentifier findAvcomSensorPort() {
    USBSerialPortIdentifier usb = new USBSerialPortIdentifier();
    try {
      Enumeration<CommPortIdentifier> cpe = usb.getComPorts();
      while (cpe.hasMoreElements()) {
        CommPortIdentifier cp = cpe.nextElement();
        usb.connect(cp);
        // Send the HardwareDescriptionRequest
        try {
          usb.write(new HardwareDescriptionRequest().serialize());
        } catch (Exception e) {
        }
        // Wait for it to responsd
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        // If a device on this port responded affirmative, return it
        if (usb.isAvcomPort) {
          usb.disconnect();
          return cp;
        }
        usb.disconnect();
      }
    } catch (IOException ex) {
    }
    // No response was received, assume no devices found
    return null;
  }

  /**
   * Static method to find the first available attached Avcom device.
   * <p>
   * @return the communications port identifier
   */
  public static CommPortIdentifier findAvcomSensorPort(String portName) {
    if (portName == null) {
      return USBSerialPortIdentifier.findAvcomSensorPort();
    } else {
      USBSerialPortIdentifier usb = new USBSerialPortIdentifier();
      try {
        CommPortIdentifier cp = CommPortIdentifier.getPortIdentifier(portName);
        usb.connect(cp);
        // Send the HardwareDescriptionRequest
        try {
          usb.write(new HardwareDescriptionRequest().serialize());
        } catch (Exception e) {
        }
        // Wait for it to responsd
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        // If a device on this port responded affirmative, return it
        if (usb.isAvcomPort) {
          usb.disconnect();
          return cp;
        }
        usb.disconnect();
      } catch (IOException ex) {
      } catch (NoSuchPortException e) {
        e.printStackTrace();
      }
      // No response was received, assume no devices found
      return null;
    }
  }

  /**
   * Static method to find all attached Avcom devices. This may be used as an
   * imput for USBSerialPortAdapter for connecting to multiple devices
   * <p>
   * @return the communications port identifier
   */
  public static List<CommPortIdentifier> findAvcomSensorPorts() {
    List<CommPortIdentifier> foundAvcomPorts = new ArrayList<CommPortIdentifier>();
    USBSerialPortIdentifier usb = new USBSerialPortIdentifier();
    try {
      Enumeration<CommPortIdentifier> cpe = usb.getComPorts();
      while (cpe.hasMoreElements()) {
        CommPortIdentifier cp = cpe.nextElement();
        // System.out.println("Trying " + cp.getName());
        usb.connect(cp);
        // Send the HardwareDescriptionRequest
        try {
          usb.write(new HardwareDescriptionRequest().serialize());
        } catch (Exception e) {
        }
        // Wait for it to responsd
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        // If a device on this port responded affirmative, then add it to the
        // list
        if (usb.isAvcomPort) {
          foundAvcomPorts.add(cp);
          usb.isAvcomPort = false;
        }
        usb.disconnect();
      }
    } catch (IOException ex) {
    }
    return foundAvcomPorts;
  }

  /**
   * Returns a CommPortIdentifier object corresponding to the indicated device
   * name.
   * <p>
   * @param deviceName the com port (e.g. /dev/ttyUSB0 for linux, COM4 for
   *                   Windows, xxx for OS X)
   * @return a CommPortIdentifier object corresponding to the indicated device
   *         name
   */
  public static CommPortIdentifier getComPort(String deviceName) {
    CommPortIdentifier cpi = null;
    try {
      cpi = CommPortIdentifier.getPortIdentifier(deviceName);
    } catch (NoSuchPortException e) {
    }
    return cpi;
  }

  public void connect(CommPortIdentifier cp) throws IOException {
    // System.out.println("Checking com port [" + cp.getName() + "] type [" +
    // cp.getPortType() + "] owner [" + cp.getCurrentOwner() + "] string [" +
    // cp.toString() + "]");
    try {
      serialPort = (SerialPort) cp.open("USBSerialPortIdentifier", 5000);
      try {
        serialPort.addEventListener(this);
      } catch (TooManyListenersException ex) {
      }
      serialPort.notifyOnDataAvailable(true);
      setSerialPortParameters();
      outputStream = serialPort.getOutputStream();
      inputStream = serialPort.getInputStream();
    } catch (Exception ex) {
    }
  }

  public void disconnect() {
    if (serialPort != null) {
      try {
        outputStream.close();
        inputStream.close();
      } catch (IOException ex) {
      }
      serialPort.close();
    }
  }

  public Enumeration<CommPortIdentifier> getComPorts() {
    return CommPortIdentifier.getPortIdentifiers();
  }

  /**
   * Returns true if an Avcom device was found on this port
   * <p>
   * @return
   */
  public boolean isIsAvcomPort() {
    return isAvcomPort;
  }

  /**
   * Read byte data from the serial port <br>
   * This reassembles AvcomDatagrams. It is a copy of the same method in
   * USBSerialPortAdapter
   * <p>
   * @param bytes
   */
  public void processReceivedBytes(byte[] bytes) {
    d.out(this, "processReceivedBytes " + ByteUtil.toString(bytes));
    try {
      // If the first byte is Start Transmission, create a new byte buffer
      if (bytes[0] == 0x02) {
        // create a new bytebuffer of the appropriate length
        byteBuffer = new byte[ByteUtil.twoByteIntFromBytes(bytes, 1) + IDatagram.HEADER_SIZE + 1]; // 4
        // =
        // header[3bytes]
        // +
        // 1
        // [terminator]
        byteBufferIndex = 0; // <-- this is critically important
        d.out(this, "STX: Type: " + bytes[3] + ", length: " + byteBuffer.length);
      }
      // Build the datagram - this throws an exception if the Watchdog restarts
      // the port mid-stream
      try {
        System.arraycopy(bytes, 0, byteBuffer, byteBufferIndex, bytes.length);
        d.out(this, "  DATA: [" + byteBufferIndex + "] " + ByteUtil.toString(bytes));
      } catch (Exception ex) {
        // ignore exception & exit the method
        return;
      }
      byteBufferIndex += bytes.length;
      // If the byteBuffer is full or the FLAG_ETX flag is set and the
      // bytebuffer
      // is above zero (we have some data)
      if (byteBufferIndex + 1 == byteBuffer.length) {
        boolean match = byteBuffer.length == byteBufferIndex + 1;
        d.out(this, "ETX: size matched received: " + match);
        // Send the complete data. Only accept correct sized byte buffers, as
        // Windows sometimes has short buffers that may match
        if (byteBuffer.length == IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH + IDatagram.HEADER_SIZE + 1) {
          isAvcomPort = true;
        }
        byteBufferIndex = 0;
      } else if ((bytes[bytes.length - 2] == 0xFF) && (bytes[bytes.length - 1] == 0x03) && (byteBufferIndex > 0)) {
        // See if we're catching an FLAG_ETX somewhere else....
        d.out(this, "ETX: unexpected terminator flag at " + byteBufferIndex);
        if (byteBuffer.length == IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH + IDatagram.HEADER_SIZE + 1) {
          isAvcomPort = true;
        }
        byteBufferIndex = 0;
      }
    } catch (Exception e) {
    }
  }

  public void serialEvent(SerialPortEvent spe) {
    switch (spe.getEventType()) {
      case SerialPortEvent.DATA_AVAILABLE:
        try {
          while (inputStream.available() > 0) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            processReceivedBytes(buffer);
          }
        } catch (IOException e) {
        }
        break;
    }
  }

  private void setSerialPortParameters() throws UnsupportedCommOperationException {
    int baudRate = 115200;
    serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
  }

  private void write(byte[] hwRequest) {
    try {
      outputStream.write(hwRequest);
    } catch (IOException ex) {
    }
  }

  /**
   * Test harness
   * <p>
   * @param args
   */
  public static void main(String[] args) {
    // System.out.println("Searching for attached Avcom devices");
    // for (CommPortIdentifier commPortIdentifier : findAvcomSensorPorts()) {
    // System.out.println("  Avcom device found on port " +
    // commPortIdentifier.getName());
    // }
    // System.out.println("Waiting 5 seconds");
    // try {
    // Thread.sleep(5 * 1000);
    // } catch (InterruptedException e) {
    // }
    CommPortIdentifier cp = USBSerialPortIdentifier.findAvcomSensorPort("/dev/ttyUSB13");
    System.out.println("Waiting 5 seconds");
    try {
      Thread.sleep(5 * 1000);
    } catch (InterruptedException e) {
    }
    System.out.println("found " + cp.getName());
    System.out.println("Exiting");
    System.exit(1);
  }
}
