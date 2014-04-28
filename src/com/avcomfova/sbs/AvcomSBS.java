/*
 * Copyright (c) 2014, Jesse Caulfield <jesse@caulfield.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.avcomfova.sbs;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.datagram.write.HardwareDescriptionRequest;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.ftdi.FTDI;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.*;
import static javax.usb.UsbConst.*;

/**
 * Sensor implementation supporting the Avcom SBS single-board-sensor platform.
 * This includes the RSA-2500 series and all Avcom of Virginia products build
 * upon that basic sensor platform.
 * <p>
 * This {@linkplain ISensor} implementation provides a set of simple methods to
 * control an Avcom SBS device and to stream data from that device. Actual
 * communication with the device is handled via a dedicated (new) thread
 * (created by this class) and is ASYNCHRONOUS: control messages are submitted
 * to the device as requests and data is read out from the device <em>as
 * available</em> in the future. For reference: there is typically a delay of
 * between 1 and 100 ms between an initial control request and data availability
 * from the device.
 * <p>
 * Developer note: To communicate with USB-attached devices this package uses
 * and requires the <code>usb4java</code> library and its underlying native
 * system library <code>libusb</code>.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class AvcomSBS {

  /**
   * 0403. The USB vendor ID (FTDI UART) used by Avcom devices.
   */
  private static final short USB_VENDOR_ID = 0x0403;
  /**
   * 6001. The USB product ID (FTDI UART) used by Avcom devices.
   */
  private static final short USB_PRODUCT_ID = 0x6001;
  /**
   * FTDI vendor specific USB command to set a modem control parameter.
   */
//  private static final byte SET_MODEM_CONTROL_REQUEST = 1;
  /**
   * FTDI vendor specific USB command to set a modem control parameter.
   */
//  private static final byte SET_BAUDRATE_REQUEST = 3;
  /**
   * Reset the port
   */
//  private static final byte SIO_RESET = 0;
  /**
   * Set the modem control register. Definition for flow control.
   */
//  private static final byte SIO_MODEM_CTRL = 1;
  /**
   * Set flow control register. Definition for flow control.
   */
//  private static final byte SIO_SET_FLOW_CTRL = 2;
  /**
   * Set baud rate. Definition for flow control.
   */
//  private static final byte SIO_SET_BAUD_RATE = 3;
  /**
   * Set the data characteristics of the port. Definition for flow control.
   */
//  private static final byte SIO_SET_DATA = 4;

  /**
   * The USB Device to which this AvcomSBS device is attached.
   */
  private final UsbDevice usbDevice;
  /**
   * The USB interface (within the UsbDevice) through which this AvcomSBS device
   * communicates. This is extracted from the UsbDevice and stored here (at the
   * class level) for convenience.
   * <p>
   * UsbInterface a synchronous wrapper through which this application sends and
   * receives messages with the device.
   */
  private UsbInterface usbInterface;
  /**
   * The USB Pipe used to READ data from the connected device.
   */
  private UsbPipe usbPipeRead;
  /**
   * The USB Pipe used to WRITE data from the connected device.
   */
  private UsbPipe usbPipeWrite;

  /**
   * The hardware description response message provided by the attached Avcom
   * device. The response message contains all available hardware description
   * parameters.
   */
  private HardwareDescriptionResponse hardwareDescription;

  /**
   * Construct a new AvcomSBS instance connected via the indicated USB device
   * port.
   * <p>
   * This will automatically connect to the device. This also created a shutdown
   * hook to automatically disconnect from the USB port when the application
   * exits.
   * <p>
   * @param usbDevice the USB Device to which this AvcomSBS device is attached.
   * @throws UsbException if the USB Device cannot be attached or claimed for
   *                      use
   */
  public AvcomSBS(final UsbDevice usbDevice) throws UsbException, Exception {
    System.out.println("Opening AvcomSBS on USB " + usbDevice);
    this.usbDevice = usbDevice;
    /**
     * Connect to the USB device.
     */
    connectUSB();
    /**
     * Initialize the device.
     */
    initialize();

    // Add a shutdown hook to close the port when we're shutting down
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        disconnectUSB();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        System.out.println("Closed AvcomSBS on USB " + usbDevice);
      }
    });
  }

  /**
   * Connect to the USB-attached sensor device.
   * <p>
   * @throws UsbException if the USB Device cannot be attached or claimed for
   *                      use
   */
  private void connectUSB() throws UsbException, Exception {
    /**
     * Developer note: If communicating via serial port set DTP and RTS lines as
     * DTR unasserted and RTS asserted. Send data at 115200 bits per seconds, 8
     * data bits, no parity, 1 stop bit, no flow control.
     * <p>
     * USB Interfaces: When you want to communicate with an interface or with
     * endpoints of this interface then you have to claim it before using it and
     * you have to release it when you are finished. Example:
     */
    UsbConfiguration configuration = usbDevice.getActiveUsbConfiguration();
    /**
     * <p>
     * Developer note: AvcomSBS devices have only ONE UsbInterface (Interface
     * #0). Therefore always get and use the first available UsbInterface from
     * the list.
     * <p>
     * The returned interface setting will be the current active alternate
     * setting if this configuration (and thus the contained interface) is
     * active. If this configuration is not active, the returned interface
     * setting will be an implementation-dependent alternate setting.
     */
    UsbInterface usbInterfaceTemp = (UsbInterface) configuration.getUsbInterfaces().get(0);
    /**
     * Claim this USB interface. This will attempt whatever claiming the native
     * implementation provides, if any. If the interface is already claimed, or
     * the native claim fails, this will fail. This must be done before opening
     * and/or using any UsbPipes.
     * <p>
     * Developer note: It is possible (nee likely) that the interface is already
     * used by the ftdi_sio kernel driver and mapped to a TTY device file.
     * Always force the claim by passing an interface policy to the claim
     * method:
     */
    usbInterfaceTemp.claim(new UsbInterfacePolicy() {

      @Override
      public boolean forceClaim(UsbInterface usbInterface) {
        return true;
      }
    });
    System.out.println("DEBUG AvcomSBS claimed USB interface " + usbInterfaceTemp);
    /**
     * If the interface was successfully claimed then assign it to the class
     * field. This is referenced later for release when the application closes.
     */
    this.usbInterface = usbInterfaceTemp;
    /**
     * Set the FTDI serial port settings.
     */
//    byte bmRequestType = REQUESTTYPE_TYPE_VENDOR | REQUESTTYPE_RECIPIENT_DEVICE | REQUESTTYPE_DIRECTION_OUT;
    /**
     * Set the serial port baud rate. '26' is the pre-calculated sub-integer
     * divisor corresponding to a baud rate of 115,384 baud: the closest
     * supported baud rate to Avcoms specified requirement of 115,200.
     */
//    usbDevice.syncSubmit(usbDevice.createUsbControlIrp(FTDI_DEVICE_OUT_REQTYPE, SIO_SET_BAUDRATE_REQUEST, (short) 26, (short) 0));
//    usbDevice.syncSubmit(usbDevice.createUsbControlIrp(FTDI_DEVICE_IN_REQTYPE, SIO_SET_BAUDRATE_REQUEST, (short) 26, (short) 0));
    /**
     * Set the serial port DTR to 'unasserted'. '256' is the pre-calculated
     * control value.
     */
//    usbDevice.syncSubmit(usbDevice.createUsbControlIrp(FTDI_DEVICE_OUT_REQTYPE, SET_MODEM_CONTROL_REQUEST, (short) 256, (short) 0));
//    usbDevice.syncSubmit(usbDevice.createUsbControlIrp(FTDI_DEVICE_OUT_REQTYPE, SIO_SET_FLOW_CTRL_REQUEST, SIO_DISABLE_FLOW_CTRL, (short) 0));
//    usbDevice.syncSubmit(usbDevice.createUsbControlIrp(FTDI_DEVICE_OUT_REQTYPE, SET_MODEM_CONTROL_REQUEST, SIO_SET_RTS_HIGH, (short) 0));

//    FTDI ftdi = new FTDI(usbDevice);
//    ftdi.setBaudRate(115200);
    FTDI.setBaudRate(usbDevice, 115200);
    FTDI.setDTRRTS(usbDevice, false, true);
    FTDI.setLineProperty(usbDevice, FTDI.LineDatabits.BITS_8, FTDI.LineStopbits.STOP_BIT_1, FTDI.LineParity.NONE);
    FTDI.setFlowControl(usbDevice, FTDI.SIO_DISABLE_FLOW_CTRL);
    /**
     * Scan the interface UsbEndPoint list to set the READ and WRITE UsbPipe.
     */
    for (Object object : usbInterfaceTemp.getUsbEndpoints()) {
      UsbEndpoint usbEndpoint = (UsbEndpoint) object;

      System.out.println("DEBUG UsbEndpoint " + usbEndpoint.getUsbEndpointDescriptor());

      /**
       * Developer Note: The USB direction value is the position 7 bit in the
       * bmRequestType field returned by the native libusb library. If the bit
       * is ZERO then the direction is host-to-device (WRITE).
       * <p>
       * Identify the READ/WRITE pipes by their end point address, which is read
       * from the bmRequestType field in USB setup.
       * <pre>
       * d t t r r r r r, where
       * d ..... direction: 0=host->device, 1=device->host
       * t ..... type: 0=standard, 1=class, 2=vendor, 3=reserved
       * r ..... recipient: 0=device, 1=interface, 2=endpoint, 3=other
       * </pre> The UsbConst values reflect the bit shifted mask from the
       * bmRequestType byte: USBRQ_DIR_MASK 0x80 (integer 128),
       * USBRQ_DIR_HOST_TO_DEVICE (0&lt;&lt;7) (integer zero)
       * USBRQ_DIR_DEVICE_TO_HOST (1&lt;&lt;7) (integer -128). The negative
       * integer value is an artifact of bit shifting.
       */
      if ((usbEndpoint.getUsbEndpointDescriptor().bEndpointAddress() & ENDPOINT_DIRECTION_IN) == 0) {
        usbPipeWrite = usbEndpoint.getUsbPipe();
        System.out.println("DEBUG AvcomSBS READ  is " + usbPipeWrite);
      } else {
        usbPipeRead = usbEndpoint.getUsbPipe();
        System.out.println("DEBUG AvcomSBS WRITE is " + usbPipeRead);
      }
    }
  }

  /**
   * Release this interface.
   * <p>
   * This will only succeed if the interface has been properly claimed. If the
   * native release fails, this will fail. This should be done after the
   * interface is no longer being used. All pipes must be closed before this can
   * be released.
   */
  public void disconnectUSB() {
    if (this.usbInterface != null) {
      try {
        usbInterface.release();
      } catch (UsbException | UsbNotActiveException | UsbDisconnectedException ex) {
        Logger.getLogger(AvcomSBS.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Connect to a network-attached sensor.
   * <p>
   * @param inetAddress the IP address of the remote sensor. The factory default
   *                    IP address is <code>192.168.118.242</code>, listening on
   *                    port <code>26482</code>.
   */
  public void connectEthernet(InetAddress inetAddress) {
    /**
     * The factory default IP address is <code>192.168.118.242</code>, listening
     * on port <code>26482</code>. The device Ethernet controller internally
     * converts inbound IP packet data to RS232. Therefor the output baud rate
     * setting is critical and must be set to 115200 bits per second.
     */
  }

  private void initialize() throws UsbException {
    System.out.println("DEBUG initialize");
    for (int i = 0; i < 2; i++) {

      write(new HardwareDescriptionRequest());
      read();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        Logger.getLogger(AvcomSBS.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }

  private void write(IDatagram datagram) throws UsbException {
    System.out.println("DEBUG write " + datagram);
    /**
     * Create a UsbIrp. This creates a UsbIrp that may be optimized for use on
     * this UsbPipe. Using this UsbIrp instead of a DefaultUsbIrp may increase
     * performance or decrease memory requirements. The UsbPipe cannot require
     * this UsbIrp to be used, all submit methods must accept any UsbIrp
     * implementation (or UsbControlIrp implementation if this is a Control-type
     * UsbPipe).
     */
    if (!usbPipeWrite.isOpen()) {
      usbPipeWrite.open();
      System.out.println("UsbPipe Write OPEN active [" + usbPipeWrite.isActive() + "] open [" + usbPipeWrite.isOpen() + "]");
    }
    /**
     * syncSubmit returns the number of bytes actually transferred.
     */

    int transferred = usbPipeWrite.syncSubmit(datagram.serialize());
    System.out.println("   WRITE " + transferred + " bytes [" + ByteUtil.toString(datagram.serialize()) + "]");

//    UsbIrp usbIrp = usbPipeWrite.createUsbIrp();
//    usbIrp.setData(datagram.serialize());
//    System.out.println("   WRITE " + usbIrp.getLength() + " bytes " + ByteUtil.toString(usbIrp.getData()));
//    usbPipeWrite.syncSubmit(usbIrp);
//    usbPipeWrite.close();    System.out.println("UsbPipe Write CLOSE");
  }

  private IDatagram read() throws UsbException {
    if (!usbPipeRead.isOpen()) {
      usbPipeRead.open();
      System.out.println("UsbPipe Read OPEN active [" + usbPipeRead.isActive() + "] open [" + usbPipeRead.isOpen() + "]");
    }

//    UsbIrp usbIrp = usbPipeRead.createUsbIrp();
//    usbPipeRead.syncSubmit(usbIrp);
//    System.out.println("   READ UsbIrp " + ByteUtil.toString(usbIrp.getData()));
    byte[] bytes = new byte[186];
    int bytesRead = usbPipeRead.syncSubmit(bytes);
    System.out.println("   READ " + bytesRead + " bytes " + ByteUtil.toString(bytes));

    HardwareDescriptionResponse hw = null;
    try {
      hw = new HardwareDescriptionResponse(bytes);
    } catch (Exception e) {
      System.err.println("   READ Failed to parse bytes " + e.getMessage());
    }
    System.out.println(hw);
//    usbPipeRead.close();    System.out.println("UsbPipe Read CLOSE");
    return null;
  }

}
