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

import com.avcomfova.sbs.datagram.Datagrams;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.datagram.write.HardwareDescriptionRequest;
import com.avcomofva.sbs.datagram.write.TraceRequest;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.keybridgeglobal.sensor.interfaces.IDatagramListener;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.ftdi.FTDI;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
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
   * A set of IDatagramListener instances. These will be notified when a new
   * Avcom datagram is read from the device represented by this instance.
   */
  private final List<IDatagramListener> datagramListeners;

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
    /**
     * Set the USB Device.
     */
    this.usbDevice = usbDevice;
    /**
     * Initialize the Datagram listeners.
     */
    this.datagramListeners = new ArrayList<>();
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
//    System.out.println("DEBUG AvcomSBS claimed USB interface " + usbInterfaceTemp);
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
        System.out.println("DEBUG AvcomSBS WRITE  is " + usbPipeWrite);
      } else {
        usbPipeRead = usbEndpoint.getUsbPipe();
        System.out.println("DEBUG AvcomSBS READ is " + usbPipeRead);
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

  /**
   * Initialize the Avcom device by sending a few HardwareDescriptionRequest
   * datagrams down the wire.
   * <p>
   * @throws UsbException if the datagrams cannot be written to the device.
   */
  @SuppressWarnings("SleepWhileInLoop")
  private void initialize() throws UsbException, Exception {
    for (int i = 0; i < 3; i++) {
      write(new HardwareDescriptionRequest());
      IDatagram datagram = read();
      hardwareDescription = (HardwareDescriptionResponse) (datagram != null ? datagram : hardwareDescription);
//      System.out.println("DEBUG AvcomSBS initialize " + (hardwareDescription != null ? hardwareDescription.toStringBrief() : " null"));
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        Logger.getLogger(AvcomSBS.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
//    System.out.println("DEBUG AvcomSBS initialize " + hardwareDescription);
  }

  public void run() throws UsbException, Exception {
    System.out.println("RUN");
    for (int i = 0; i < 100; i++) {
      write(new TraceRequest());
      try {
        read();
      } catch (Exception exception) {
        System.err.println("DEBUG RUN ERROR " + exception.getMessage());
      }
    }
  }

  /**
   * Write a REQUEST datagram to the Avcom device.
   * <p>
   * @param datagram the REQUEST-type datagram to write to the Avcom device
   * @throws UsbException if the USB port cannot be written to
   */
  private void write(IDatagram datagram) throws UsbException {
//    System.out.println("DEBUG write " + datagram);
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
//      System.out.println("UsbPipe Write OPEN active [" + usbPipeWrite.isActive() + "] open [" + usbPipeWrite.isOpen() + "]");
    }
//    int transferred = usbPipeWrite.syncSubmit(datagram.serialize());
//    UsbIrp usbIrp = usbPipeWrite.asyncSubmit(datagram.serialize());
    UsbIrp usbIrp = usbPipeWrite.createUsbIrp();
    usbIrp.setData(datagram.serialize());
    usbPipeWrite.syncSubmit(usbIrp);
//    System.out.println("   WRITE isComplete [" + usbIrp.isComplete() + "] isUsbException [" + usbIrp.isUsbException() + "]");
//    System.out.println("   WRITE " + usbIrp.getActualLength() + " bytes [" + ByteUtil.toString(datagram.serialize()) + "]");
// DEPRECATED: write bytes directly to the port
//    UsbIrp usbIrp = usbPipeWrite.createUsbIrp();
//    usbIrp.setData(datagram.serialize());
//    System.out.println("   WRITE " + usbIrp.getLength() + " bytes " + ByteUtil.toString(usbIrp.getData()));
//    usbPipeWrite.syncSubmit(usbIrp);
//    usbPipeWrite.close();    System.out.println("UsbPipe Write CLOSE");
  }

  /**
   * Read data from the USB port.
   * <p>
   * @return an Avcom datagram instance.
   * @throws UsbException if the USB port cannot be accessed
   * @throws Exception    if the Avcom data cannot be parsed into a valid
   *                      datagram instance
   */
  @SuppressWarnings("NestedAssignment")
  private IDatagram read() throws Exception {
    if (!usbPipeRead.isOpen()) {
      usbPipeRead.open();
//      System.out.println("UsbPipe Read OPEN active [" + usbPipeRead.isActive() + "] open [" + usbPipeRead.isOpen() + "]");
    }
    /**
     * The return value will indicate the number of bytes successfully
     * transferred to or from the target endpoint (depending on direction). The
     * return value will never exceed the total size of the provided buffer. If
     * the operation was not sucessful the UsbException will accurately reflect
     * the cause of the error.
     */
    /**
     * Store the USB device maximum packet size in a local variable for
     * convenience.
     */
    short wMaxPacketSize = usbPipeRead.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize();
    /**
     * Initialize the first USB packet and the bytesRead indicator. These are
     * used in the first iteration of the while-loop below.
     * <p>
     * USB data is sent in packets Least Significant Bit (LSB) first. There are
     * 4 main USB packet types: Token, Data, Handshake and Start of Frame. Each
     * packet is constructed from different field types, namely SYNC, PID,
     * Address, Data, Endpoint, CRC and EOP. The packets are then bundled into
     * frames to create a USB message.
     * <p>
     * Each USB packet starts with a (1 byte) SYNC field. This is basically used
     * to synchronise the transmitter and the receiver so that the data can be
     * transferred accurately. In a USB slow / full speed system this SYNC field
     * consists 3 KJ pairs followed by 2 K’s to make up 8 bits of data. In a USB
     * Hi-Speed system the synchronisation requires 15 KJ pairs followed by 2
     * K’s to make up 32 bits of data.
     * <p>
     * Following on directly after the SYNC field is a (1 byte) Packet
     * Identifier Field. The Packet Identifier Field consists of a 4 bit
     * identifier and a further 4 bits which are the one’s compliment of the
     * identifier.
     * <p>
     * The data field is not a fixed length. It is within the range of 0 - 8192
     * bits long, and always an integral number of bytes.
     * <p>
     * USB DATA Packets: A data packet may be variable length, dependent upon
     * the data. However, the data field will be an integral number of bytes. A
     * USB Data packet is constructed thus: [SYNC][PID][DATA][CRC16][EOP].
     * Depending upon the bus speed the SYNC header is 8 or 31 bits. PID is
     * always 8 bits. DATA may range between 0 and 8192 bits. CRC16 is 16 bits
     * and EOP (end of packet) is 3 bits.
     * <p>
     * Developer note: CRC and EOP are not included in the returned byte array,
     * but rather are processed and used to raise an error condition within the
     * USB Pipe instance. Therefore the usbPacket is actually just
     * [SYNC][PID][DATA].
     */
    byte[] avcomDatagram = null;
    int avcomDatagramIndex = 0;
    byte[] usbPacket = new byte[wMaxPacketSize];
    int bytesRead;
    while ((bytesRead = usbPipeRead.syncSubmit(usbPacket)) > 2) {
//      System.out.println("  read " + bytesRead + " bytes " + usbPacket.length + " [" + ByteUtil.toString(usbPacket) + "]");
//      for (int i = 0; i < 8; i++) {        System.out.println("    usbPacket[0][" + i + "] " + ByteUtil.getBit(usbPacket[0], i));      }
//      for (int i = 0; i < 8; i++) {        System.out.println("    usbPacket[1][" + i + "] " + ByteUtil.getBit(usbPacket[1], i));      }
//      byte[] usbPacketData = new byte[bytesRead - 2];
      /**
       * Inspect the USB Packet for an Avcom STX flag (0x02). This will be
       * located at USB packet byte 3 (first data byte after the two-byte USB
       * Packet header).
       */
      if (usbPacket[2] == IDatagram.FLAG_STX && EAvcomDatagram.fromByteCode(usbPacket[5]) != null) {
        /**
         * Initialize the Avcom datagram byte buffer to the length indicated in
         * the datagram packet header (Avcom datagram bytes 1 and 2). The USB
         * Packet byte index 3 skips the 2-byte header.
         * <p>
         * Add four additional bytes the Avcom datagram byte array to include
         * the Avcom packet header information, which is not included in the
         * Avcom datagram length number.
         */
        avcomDatagram = new byte[ByteUtil.twoByteIntFromBytes(usbPacket, 3) + 4];
        /**
         * Important: Initialize the Avcom Datagram byte buffer Index. This is
         * used to copy fresh data into the buffer from subsequent USB packets.
         */
        avcomDatagramIndex = 0;
      }
      if (avcomDatagram != null) {
        /**
         * If the Avcom datagram has been initialized then copy the USB packet
         * data into the Avcom datagram byte buffer. Increment the Avcom
         * Datagram byte buffer Index by the number of bytes copied.
         * <p>
         * Developer note: The USB port data does not always align with the read
         * buffer. If there is more data than the datagram requires then only
         * copy up to the datagram fill and no more. CopyLength is the number of
         * array elements to be copied.
         */
        int copyLength = (bytesRead - 2 + avcomDatagramIndex > avcomDatagram.length
          ? avcomDatagram.length - avcomDatagramIndex
          : bytesRead - 2);

        System.out.println("  read " + bytesRead + " bytes "
          + usbPacket.length + " [" + ByteUtil.toString(usbPacket)
          + "]  avcomDatagram.length [" + avcomDatagram.length
          + "] avcomDatagramIndex [" + avcomDatagramIndex
          + "] copylength [" + copyLength + "]");

        System.arraycopy(usbPacket,
                         2,
                         avcomDatagram,
                         avcomDatagramIndex,
                         copyLength);
//        avcomDatagramIndex += bytesRead - 2;
        avcomDatagramIndex += copyLength;
      }
      /**
       * Important: Reinitialize the usbPacket byte array.
       * <p>
       * Developer note: The syncSubmit (and presumably asyncSubmit) do not
       * clear the input byte array - they merely write bytes into the provided
       * array. Reusing a previously populated byte array creates JUNK data as
       * new bytes are written over the old bytes but if the new packet is
       * shorter then old bytes will remain.
       */
      usbPacket = new byte[wMaxPacketSize];
    }
    /**
     * At this point the Avcom datagram is either null or has been completely
     * read. Process and return the Avcom datagram byte buffer as a valid
     * Datagram instance.
     */
//    System.out.println("DEBUG read avcom packet " + ByteUtil.toStringWithIndex(avcomDatagram));
//    System.out.println("DEBUG read avcom packet " + ByteUtil.toString(avcomDatagram));
    /**
     * If the data is null then return null;
     */
    if (avcomDatagram == null) {
      return null;
    }
    /**
     * If the data is not null then parse it into a datagram and notify all
     * listeners. Return the datagram for internal use.
     */
    IDatagram datagram = Datagrams.getInstance(avcomDatagram);
    notifyListeners(datagram);
    return datagram;
    //    return avcomDatagram == null      ? null      : Datagrams.getInstance(avcomDatagram);
  }

  /**
   * Internal method called when an Avcom datagram has been read off the device.
   * A copy of the datagram is forwarded to all listeners when they are
   * notified.
   * <p>
   * @param datagram
   */
  private void notifyListeners(IDatagram datagram) {
    for (IDatagramListener iDatagramListener : datagramListeners) {
      iDatagramListener.onDatagram(datagram);
    }
  }

  /**
   * Add a Datagram Listener to receive datagrams when ready
   * <p>
   * @param listener the listener instance
   */
  public synchronized void addListener(IDatagramListener listener) {
    this.datagramListeners.add(listener);
  }

  /**
   * Remove a DatagramListener
   * <p>
   * @param listener the listener instance
   */
  public synchronized void removeListener(IDatagramListener listener) {
    this.datagramListeners.remove(listener);
  }

}
