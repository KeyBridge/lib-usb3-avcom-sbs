/*
 * Copyright (c) 2014, Jesse Caulfield
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
package com.avcomofva.sbs;

import com.avcomofva.sbs.datagram.write.HardwareDescriptionRequest;
import com.ftdichip.usb.FTDI;
import com.ftdichip.usb.FTDIUtility;
import com.ftdichip.usb.enumerated.FlowControl;
import com.ftdichip.usb.enumerated.LineDatabit;
import com.ftdichip.usb.enumerated.LineParity;
import com.ftdichip.usb.enumerated.LineStopbit;
import java.util.ArrayList;
import java.util.List;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbHub;
import javax.usb3.IUsbServices;
import javax.usb3.UsbHostManager;
import javax.usb3.exception.UsbException;
import javax.usb3.request.BMRequestType;
import javax.usb3.utility.ByteUtility;

import static javax.usb3.enumerated.EEndpointDirection.HOST_TO_DEVICE;

/**
 *
 * @author Jesse Caulfield
 */
public class Test_FTDI {

  private static final short vendorId = 0x0403;
  private static final short productId = 0x6001;

//  @Test
  public void test() throws Exception {
    Test_FTDI test = new Test_FTDI();
    IUsbDevice usbDevice = test.findAvcomIUsbDevice();

//    System.out.println(ByteUtility.toStringFormatted(FTDI.FTDI_DEVICE_OUT_REQTYPE));
//    System.out.println(ByteUtility.toStringFormatted(FTDI.FTDI_USB_CONFIGURATION_WRITE));
    System.out.println("HOST_TO_DEVICE \n" + ByteUtility.toStringFormatted(HOST_TO_DEVICE.getByteCode()));
    System.out.println("VENDOR         \n" + ByteUtility.toStringFormatted(BMRequestType.EType.VENDOR.getByteCode()));
    System.out.println("DEVICE         \n" + ByteUtility.toStringFormatted(BMRequestType.ERecipient.DEVICE.getByteCode()));

    System.out.println("device " + usbDevice);

//    FTDI ftdi = new FTDI();
    FTDIUtility.setBaudRate(usbDevice, 115200);
    FTDIUtility.setLineProperty(usbDevice,
                                LineDatabit.BITS_8,
                                LineStopbit.STOP_BIT_1,
                                LineParity.NONE);
    FTDIUtility.setFlowControl(usbDevice, FlowControl.DISABLE_FLOW_CTRL);
    FTDIUtility.setDTRRTS(usbDevice, false, true);

    FTDI ftdi = new FTDI(usbDevice);

    byte[] write = new HardwareDescriptionRequest().serialize();

    for (int i = 0; i < 5; i++) {
      ftdi.write(write);
      System.out.println("WRITE [" + write.length + "] " + ByteUtility.toString(write));

      byte[] usbFrame = ftdi.read();
      while (usbFrame.length > 0) {
        System.out.println("   READ [" + usbFrame.length + "] " + ByteUtility.toString(usbFrame));
        usbFrame = ftdi.read();
      }
    }

    // release the port for more tests.
    ftdi.close();

//    ftdi.setDtr(false);
  }

  /**
   * Search the USB device tree and return the FIRST detected Avcom sensor
   * device.
   *
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
   *
   * @param iUsbDevice The IUsbDevice to check.
   * @param vendorId   The vendor id to match.
   * @param productId  The product id to match.
   * @param A          List of any matching IUsbDevice(s).
   */
  private List<IUsbDevice> getIUsbDeviceList(IUsbDevice iUsbDevice, short vendorId, short productId) {
    List<IUsbDevice> iUsbDeviceList = new ArrayList<>();
    /*
     * A device's descriptor is always available. All descriptor field names and
     * types match exactly what is in the USB specification.
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
