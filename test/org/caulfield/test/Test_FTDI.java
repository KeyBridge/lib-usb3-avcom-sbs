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
package org.caulfield.test;

import com.avcomofva.sbs.datagram.write.HardwareDescriptionRequest;
import com.ftdichip.usb.FTDI;
import com.ftdichip.usb.FTDIUtil;
import com.ftdichip.usb.enumerated.EFlowControl;
import com.ftdichip.usb.enumerated.ELineDatabits;
import com.ftdichip.usb.enumerated.ELineParity;
import com.ftdichip.usb.enumerated.ELineStopbits;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.util.ArrayList;
import java.util.List;
import javax.usb.*;
import javax.usb.exception.UsbException;
import static javax.usb.ri.enumerated.EEndpointDirection.HOST_TO_DEVICE;
import javax.usb.ri.request.BMRequestType;

/**
 *
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class Test_FTDI {

  private static final short vendorId = 0x0403;
  private static final short productId = 0x6001;

  public static void main(String[] args) throws Exception {
    Test_FTDI test = new Test_FTDI();
    IUsbDevice usbDevice = test.findAvcomIUsbDevice();

//    System.out.println(ByteUtil.toStringFormatted(FTDI.FTDI_DEVICE_OUT_REQTYPE));
//    System.out.println(ByteUtil.toStringFormatted(FTDI.FTDI_USB_CONFIGURATION_WRITE));
    System.out.println("HOST_TO_DEVICE \n" + ByteUtil.toStringFormatted(HOST_TO_DEVICE.getByteCode()));
    System.out.println("VENDOR         \n" + ByteUtil.toStringFormatted(BMRequestType.EType.VENDOR.getByteCode()));
    System.out.println("DEVICE         \n" + ByteUtil.toStringFormatted(BMRequestType.ERecipient.DEVICE.getByteCode()));

    System.out.println("device " + usbDevice);

//    FTDI ftdi = new FTDI();
    FTDIUtil.setBaudRate(usbDevice, 115200);
    FTDIUtil.setLineProperty(usbDevice,
                             ELineDatabits.BITS_8,
                             ELineStopbits.STOP_BIT_1,
                             ELineParity.NONE);
    FTDIUtil.setFlowControl(usbDevice, EFlowControl.DISABLE_FLOW_CTRL);
    FTDIUtil.setDTRRTS(usbDevice, false, true);

    FTDI ftdi = new FTDI(usbDevice);

    byte[] write = new HardwareDescriptionRequest().serialize();

    for (int i = 0; i < 5; i++) {
      ftdi.write(write);
      System.out.println("WRITE [" + write.length + "] " + ByteUtil.toString(write));

      byte[] usbPacket = ftdi.read();
      while (usbPacket.length > 0) {
        System.out.println("   READ [" + usbPacket.length + "] " + ByteUtil.toString(usbPacket));
        usbPacket = ftdi.read();
      }
    }

//    ftdi.setDtr(false);
  }

  /**
   * Search the USB device tree and return the FIRST detected Avcom sensor
   * device.
   * <p>
   * @return the first detected Avcom sensor device, null if none are found
   * @throws UsbException if the USB port cannot be read
   */
  public IUsbDevice findAvcomIUsbDevice() throws UsbException {
    IUsbServices usbServices = UsbHostManager.getUsbServices();
    IUsbHub virtualRootUsbHub = usbServices.getRootUsbHub();
    List<IUsbDevice> iUsbDevices = getIUsbDeviceList(virtualRootUsbHub, vendorId, productId);

    return iUsbDevices.isEmpty() ? null : iUsbDevices.get(0);
  }

  /**
   * Get a List of all devices that match the specified vendor and product id.
   * <p>
   * @param iUsbDevice The IUsbDevice to check.
   * @param vendorId   The vendor id to match.
   * @param productId  The product id to match.
   * @param A          List of any matching IUsbDevice(s).
   */
  private List<IUsbDevice> getIUsbDeviceList(IUsbDevice iUsbDevice, short vendorId, short productId) {
    List<IUsbDevice> iUsbDeviceList = new ArrayList<>();
    /*
     * A device's descriptor is always available. All descriptor field names and
     * types match exactly what is in the USB specification. Note that Java does
     * not have unsigned numbers, so if you are comparing 'magic' numbers to the
     * fields, you need to handle it correctly. For example if you were checking
     * for Intel (vendor id 0x8086) devices, if (0x8086 ==
     * descriptor.idVendor()) will NOT work. The 'magic' number 0x8086 is a
     * positive integer, while the _short_ vendor id 0x8086 is a negative
     * number! So you need to do either if ((short)0x8086 ==
     * descriptor.idVendor()) or if (0x8086 ==
     * UsbUtil.unsignedInt(descriptor.idVendor())) or short intelVendorId =
     * (short)0x8086; if (intelVendorId == descriptor.idVendor()) Note the last
     * one, if you don't cast 0x8086 into a short, the compiler will fail
     * because there is a loss of precision; you can't represent positive 0x8086
     * as a short; the max value of a signed short is 0x7fff (see
     * Short.MAX_VALUE).
     *
     * See javax.usb.util.UsbUtil.unsignedInt() for some more information.
     */
    if (vendorId == iUsbDevice.getUsbDeviceDescriptor().idVendor()
      && productId == iUsbDevice.getUsbDeviceDescriptor().idProduct()) {
      iUsbDeviceList.add(iUsbDevice);
    }
    /*
     * If the device is a HUB then recurse and scan the hub connected devices.
     * This is just normal recursion: Nothing special.
     */
    if (iUsbDevice.isUsbHub()) {

      for (Object object : ((IUsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceList.addAll(getIUsbDeviceList((IUsbDevice) object, vendorId, productId));
      }
    }
    return iUsbDeviceList;
  }

}
