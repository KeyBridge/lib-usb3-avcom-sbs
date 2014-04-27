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

import com.keybridgeglobal.sensor.avcom.datagram.HardwareDescriptionResponse;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.*;

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
   * The USB Device to which this AvcomSBS device is attached.
   */
  private UsbDevice usbDevice;
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
  public AvcomSBS(final UsbDevice usbDevice) throws UsbException {
    System.out.println("Opening AvcomSBS on USB " + usbDevice);
    this.usbDevice = usbDevice;
    /**
     * Connect to the USB device.
     */
    connectUSB();
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
   * Connect to a USB-attached sensor.
   * <p>
   * @throws UsbException if the USB Device cannot be attached or claimed for
   *                      use
   */
  private void connectUSB() throws UsbException {
    System.out.println("AvcomSBS connectUSB");
    /**
     * If communicating via serial port set DTP and RTS lines as DTR unasserted
     * and RTS asserted. Send data at 115200 bits per seconds, 8 data bits, no
     * parity, 1 stop bit, no flow control.
     */
//    UsbDevice usbDevice = getUSBDeviceList(UsbHostManager.getUsbServices().getRootUsbHub(),
//                                           USB_VENDOR_ID,
//                                           USB_PRODUCT_ID).get(0);
    /**
     * Interfaces: When you want to communicate with an interface or with
     * endpoints of this interface then you have to claim it before using it and
     * you have to release it when you are finished. Example:
     */
    UsbConfiguration configuration = usbDevice.getActiveUsbConfiguration();
    /**
     * Get the first available UsbInterface.
     * <p>
     * Avcom devices typically only have ONE UsbInterface. The returned
     * interface setting will be the current active alternate setting if this
     * configuration (and thus the contained interface) is active. If this
     * configuration is not active, the returned interface setting will be an
     * implementation-dependent alternate setting.
     */
    UsbInterface usbInterfaceTemp = (UsbInterface) configuration.getUsbInterfaces().get(0);
    /**
     * Claim this USB interface. This will attempt whatever claiming the native
     * implementation provides, if any. If the interface is already claimed, or
     * the native claim fails, this will fail. This must be done before opening
     * and/or using any UsbPipes.
     * <p>
     * It is possible that the interface you want to communicate with is already
     * used by a kernel driver. In this case you can try to force the claiming
     * by passing an interface policy to the claim method:
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
     * level field. This will be referenced upon termination for release.
     */
    this.usbInterface = usbInterfaceTemp;
    /**
     * Now scan the interface to open the READ and WRITE UsbEndPoint and
     * UsbPipe.
     */
    for (Object object : usbInterfaceTemp.getUsbEndpoints()) {
      UsbEndpoint usbEndpoint = (UsbEndpoint) object;

      System.out.println("DEBUG usbEndpoint bEndpointAddress byte " + ByteUtil.intFromByte(usbEndpoint.getUsbEndpointDescriptor().bEndpointAddress()));

      if ((usbEndpoint.getUsbEndpointDescriptor().bEndpointAddress() & UsbConst.ENDPOINT_DIRECTION_IN) != 0) {
        usbPipeRead = usbEndpoint.getUsbPipe();

        System.out.println("DEBUG AvcomSBS USB Pipe READ is " + usbPipeRead);

      } else if ((usbEndpoint.getUsbEndpointDescriptor().bEndpointAddress() & UsbConst.ENDPOINT_DIRECTION_IN) == 0) {
        usbPipeWrite = usbEndpoint.getUsbPipe();

        System.out.println("DEBUG AvcomSBS USB Pipe WRITE is " + usbPipeWrite);

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
}
