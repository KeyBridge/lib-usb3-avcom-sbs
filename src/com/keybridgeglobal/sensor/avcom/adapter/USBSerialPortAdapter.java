package com.keybridgeglobal.sensor.avcom.adapter;

//import gnu.io.CommPortIdentifier;
//import gnu.io.SerialPort;
//import gnu.io.SerialPortEvent;
//import gnu.io.SerialPortEventListener;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomProductID;
import com.keybridgeglobal.sensor.avcom.datagram.DatagramFactory;
import com.keybridgeglobal.sensor.avcom.datagram.HardwareDescriptionRequest;
import com.keybridgeglobal.sensor.avcom.datagram.HardwareDescriptionResponse;
import com.keybridgeglobal.sensor.interfaces.IDatagramListener;
import com.keybridgeglobal.sensor.interfaces.IDeviceAdapter;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 0512: modified to use CommPortIdentifier_Interface USB Serial port adapter.
 * When instantiated, automatically tries to connect to any available serial
 * port. If a port is found, the attached device is interrogated to determine
 * it's type This object will automatically connect. Its status can be
 * determined with isConnected - Boolean if connected or not getCommPortName -
 * String name of the USB port getHardwareType - String name of the attached
 * hardware
 * <p>
 * @author jesse
 */
public class USBSerialPortAdapter implements IDeviceAdapter, SerialPortEventListener {

  private Debug d = new Debug(false);
  private final List<IDatagramListener> datagramListeners = new ArrayList<>();
  private OutputStream outputStream;
  private InputStream inputStream;
  private Boolean isConnected = false;
  private SerialPort serialPort;
  private EAvcomProductID productId;                                                      // they
  // type
  // of
  // device
  // we're
  // connected
  // to
  private byte[] byteBuffer = null;
  int byteBufferIndex;
  private CommPortIdentifier commPortIdentifier;

  public USBSerialPortAdapter(CommPortIdentifier cpi) {
    d.out(this, "USBSerialPortAdapter");
    // Set our com port identifier
    this.commPortIdentifier = cpi;
    // Attempt to connect to any available USB port
    connect(cpi);
    // Initialize - send a HW request
    initialize();
    // Add a shutdown hook to close the port when we're shutting down
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        d.out("Shutdown Hook: USBSerialPortAdapter");
        disconnect();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
      }
    });
  }

  /**
   * Public method to turn debugging messages on or off
   * <p>
   * @param b
   */
  public void setDebug(boolean b) {
    d = new Debug(b);
  }

  /**
   * Attach to the USB serial port and open it for reading and writing. If
   * connect is successful, will set the internal parameter {@link isConnected}
   * <p>
   * @param commPortIdentifier
   * @return true if the port was able to connect
   */
  public Boolean connect(CommPortIdentifier cpi) {
    d.out(this, "connect");
    if (cpi == null) {
      return false;
    }
    String portName = cpi.getName(); // convenience field for debug printing
    // Try to open the serial port
    try {
      d.out(this, "  " + portName + " opening as USBSerialPortAdapter");
      serialPort = (SerialPort) cpi.open("USBSerialPortAdapter", 2000);
      // Get read/write byte streams
      d.out(this, "  " + portName + " getting input stream");
      this.inputStream = serialPort.getInputStream();
      this.outputStream = serialPort.getOutputStream();
      // Listend for Serial Events
      d.out(this, "  " + portName + " adding serial event listener to receive data");
      serialPort.addEventListener(this);
      // Set serial port parameters
      d.out(this, "  " + portName + " setting serial port parameters");
      serialPort.notifyOnDataAvailable(true);
      serialPort.notifyOnCarrierDetect(true);
      serialPort.notifyOnParityError(true);
      serialPort.notifyOnOverrunError(true);
      serialPort.notifyOnCTS(true);
      serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      serialPort.setInputBufferSize(4096);
      serialPort.setOutputBufferSize(4096);
      serialPort.sendBreak(0);
      // Set our status to connected
      d.out(this, "connect SUCCESS to " + portName);
      this.isConnected = true;
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Empty connect method required by interface. Do NOT use for USB connections.
   * Instead, use @link{connect(CommPortIdentifier commPortIdentifier)}
   */
  public Boolean connect() {
    if (this.commPortIdentifier != null) {
      return connect(commPortIdentifier);
    } else {
      d.out(this, "ERROR: connect attempt with no com port identifier");
      return false;
    }
  }

  /**
   * Initialize the attached hardware. For Avcom, this sends a new
   * HardwareDescriptionRequest to the device and waits for a response.
   */
  public void initialize() {
    d.out(this, "initialize");
    write(new HardwareDescriptionRequest());
    try {
      Thread.sleep(100); // 100 ms is enough time to the device to respond
    } catch (InterruptedException ex) {
      Logger.getLogger(USBSerialPortAdapter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Write a datagram to the physical device <br>
   * Converts the datagram to its serial bytes, then sends it to the device
   * <p>
   * @param datagram
   */
  public void write(IDatagram datagram) {
    if (this.isConnected) {
      d.out(this, "write " + datagram.getTransactionId());
      try {
        outputStream.write(datagram.serialize());
      } catch (IOException ex) {
        Logger.getLogger(USBSerialPortAdapter.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      d.out(this, "write ERROR: not connected");
    }
  }

  /**
   * Write a datagram to the physical device <br>
   * Converts the datagram to its serial bytes, then sends it to the device
   * <p>
   * @param datagram
   */
  public void write(byte[] bytes) {
    if (this.isConnected) {
      d.out(this, "write bytes : " + ByteUtil.toString(bytes));
      try {
        outputStream.write(bytes);
      } catch (IOException ex) {
        Logger.getLogger(USBSerialPortAdapter.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      d.out(this, "write ERROR: not connected");
    }
  }

  /**
   * Disconnect the USB serial port
   */
  public Boolean disconnect() {
    d.out(this, "disconnect");
    if (serialPort != null) {
      try {
        serialPort.removeEventListener();
        serialPort.close();
      } catch (Exception e) {
        // 0410 JMC: removing listeners sometimes throws an exception when
        // exiting the program. OK to ignore
      }
    }
    this.isConnected = false;
    return true;
  }

  /**
   * Tests whether the serial port is connected
   * <p>
   * @return
   */
  public Boolean isConnected() {
    return this.isConnected;
  }

  /**
   * The type of product this port is connected to
   * <p>
   * @return
   */
  public EAvcomProductID getProductId() {
    return productId;
  }

  /**
   * This is the method triggered by the SerialPortEventListener interface.
   * <p>
   * When the device returns data it does so in many byte[] chunks.
   * <p>
   * Whenever data is available, process it with 'processReceivedBytes'
   * <p>
   * Note: 'processReceivedBytes' will construct a datagram and automatically
   * call 'notifyListeners' when the datagram is ready.
   * <p>
   * @param ev
   */
  public void serialEvent(SerialPortEvent event) {
    switch (event.getEventType()) {
      case SerialPortEvent.BI:
      case SerialPortEvent.OE:
      case SerialPortEvent.FE:
      case SerialPortEvent.PE:
      case SerialPortEvent.CD:
      case SerialPortEvent.CTS:
      case SerialPortEvent.DSR:
      case SerialPortEvent.RI:
      case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
        break;
      case SerialPortEvent.DATA_AVAILABLE:
        try {
          while (inputStream.available() > 0) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            processReceivedBytes(buffer);
          }
        } catch (IOException e) {
          // noop
        }
        break;
      default:
        d.out(this, "serialEvent: " + event.getEventType());
    }
  }

  /**
   * Read byte data from the serial port and broadcast it to all of the
   * registered listeners <br>
   * This method is called by the SerialCommAdapter whenever the device sends
   * data to the detector <br>
   * This is iteratively called as long as there is data on the wire and so must
   * reassemble AvcomDatagrams
   * <p>
   * @param bytes
   */
  public void processReceivedBytes(byte[] bytes) {
    // If the first byte is Start Transmission, create a new byte buffer
    if (bytes[0] == 0x02) {
      // create a new bytebuffer of the appropriate length
      byteBuffer = new byte[ByteUtil.twoByteIntFromBytes(bytes, 1) + 4];
// 4 = header[3bytes] + 1 [terminator]
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
//     If the byteBuffer is full or
//     the FLAG_ETX flag is set and
//     the bytebuffer is above zero (we have some data)
    if (byteBufferIndex + 1 == byteBuffer.length) {
      boolean match = byteBuffer.length == byteBufferIndex + 1;
      d.out(this, "ETX: size matched received: " + match);
      // Send the complete data
      notifyListeners(byteBuffer);
      byteBufferIndex = 0;
    } else if ((bytes[bytes.length - 2] == 0xFF) && (bytes[bytes.length - 1] == 0x03) && (byteBufferIndex > 0)) {
      // See if we're catching an FLAG_ETX somewhere else....
      d.out(this, "ETX: unexpected terminator flag at " + byteBufferIndex);
      notifyListeners(byteBuffer); // may be a bogus datagram, but handled at a
      // higher level
      byteBufferIndex = 0;
    }
  }

  /**
   * Convert the bytebuffer into a datagram and send it to all registered
   * listeneres
   * <p>
   * @param byteBuffer
   */
  private void notifyListeners(byte[] byteBuffer) {
    // d.out(this, "notifyListeners " + ByteUtil.toString(byteBuffer));
    IDatagram datagram = DatagramFactory.createDatagram(byteBuffer);
    d.out(this, "notifyListeners type [" + datagram.getSensorTypeId() + "]");
    if (datagram.isValid()) {
      d.out(this, "  is valid type " + datagram.getSensorTypeId());
      if ((productId == null) && (datagram.getSensorTypeId() == IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID)) {
        HardwareDescriptionResponse hr = (HardwareDescriptionResponse) datagram;
        productId = hr.getProductId();
      }
      for (IDatagramListener listener : datagramListeners) {
        d.out(this, "    listeners " + listener.getClass().getSimpleName());
        listener.onDatagram(datagram);
      }
    } else {
      // TODO - count the number of invalid datagrams
      d.out(this, "notifyListeners ERROR: Invalid Datagram");
    }
  }

  /**
   * Add a Datagram Listener to receive datagrams when ready
   * <p>
   * @param datagramListener
   * @return true if successful
   */
  public void addDatagramListener(IDatagramListener datagramListener) {
    d.out(this, "addDatagramListener " + datagramListener.getClass().getSimpleName());
    datagramListeners.add(datagramListener);
  }

  /**
   * Remove a DatagramListener
   * <p>
   * @param datagramListener
   * @return true if successful
   */
  public void removeDatagramListener(IDatagramListener datagramListener) {
    datagramListeners.remove(datagramListener);
  }

  public CommPortIdentifier getComPortIdentifier() {
    return this.commPortIdentifier;
  }

  public void setComPortIdentifier(CommPortIdentifier comPortIdentifier) {
    this.commPortIdentifier = comPortIdentifier;
  }
  /**
   * scratchbox - run once for each attached device
   * <p>
   * @param args
   */
  // public static void main(String[] args) {
  // USBSerialPortAdapter u = new USBSerialPortAdapter();
  // u.setDebug(true);
  // // u.setDebug(true);
  // // if (u.connect()) {
  // // System.out.println("connected!");
  // // } else {
  // // System.out.println("NOT connected!");
  // // System.exit(1);
  // // }
  //
  // // byte[] hdrequest = new byte[] { 2, 0, 3, 7, 0, 3 };
  // byte[] wrequest = new byte[] { 2, 0, 3, 3, 3, 3 };
  //
  // System.out.println("write!");
  // try {
  // System.out.println("------------------------------------------------------HW");
  // u.write(new HardwareDescriptionRequest());
  // Thread.sleep(1000);
  // //u.write(new HardwareDescriptionRequest());
  // for (int i = 0; i < 100; i++) {
  // System.out.println("------------------------------------------------------Wave");
  // u.write(wrequest);
  // Thread.sleep(1000);
  //
  // }
  //
  // } catch (Exception ex) {
  // Logger.getLogger(USBSerialPortAdapter.class.getName()).log(Level.SEVERE,
  // null, ex);
  // }
  // System.out.println("device : " + u.getProductId());
  // System.out.println("port: " + u.getCommPortName());
  // System.out.println("Sleeping for 30 seconds");
  //
  // try {
  // Thread.sleep(30 * 1000);
  // } catch (InterruptedException ex) {
  // Logger.getLogger(USBSerialPortAdapter.class.getName()).log(Level.SEVERE,
  // null, ex);
  // }
  //
  // // System.out.println("Disconnecting");
  //
  // // u.disconnect();
  //
  // // if we don't disconnect (above), the program will linger and we must
  // force it to quite
  // // if we force quite, the shutdown handler will disconnect the port
  //
  // System.out.println("Exiting");
  // System.exit(1);
  // }
}
