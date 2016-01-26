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

import java.util.ArrayList;
import java.util.List;
import javax.usb.IUsbDevice;
import javax.usb.UsbHostManager;
import javax.usb.exception.UsbException;
import org.usb4java.javax.UsbHub;

/**
 * Communications adapter implementation for USB serial ports.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class USBPortAdapter {

  /**
   * A list of USB devices having the configured USB vendor and product id.
   */
  private final List<IUsbDevice> iUsbDeviceList;

  /**
   * Construct a new USB Port Adapter. This constructor searches the USB tree to
   * find one or more devices having the provided USB vendor and product id.
   * Found devices devices are placed within the list iUsbDeviceList.
   * <p>
   * @param usbVendorId  the USB vendor ID to search for
   * @param usbProductId the USB product ID to search for
   * @throws UsbException if the running user has insufficient privileges to
   *                      access the UDB tree
   */
  public USBPortAdapter(short usbVendorId, short usbProductId) throws UsbException {
    this.iUsbDeviceList = buildIUsbDeviceList(UsbHostManager.getUsbServices().getRootUsbHub(), usbVendorId, usbProductId);
  }

  /**
   * Get the list of USB devices having the configured USB vendor and product
   * id.
   * <p>
   * @return a non-null but possibly empty ArrayList instance.
   */
  public List<IUsbDevice> getIUsbDeviceList() {
    return iUsbDeviceList == null ? new ArrayList<IUsbDevice>() : new ArrayList<>(iUsbDeviceList);
  }

  /**
   * Get a List of all devices that match the specified vendor and product id.
   * <p>
   * @param iUsbDevice The IUsbDevice to check.
   * @param vendorId   The vendor id to match.
   * @param productId  The product id to match.
   * @param A          List of any matching IUsbDevice(s).
   */
  private List<IUsbDevice> buildIUsbDeviceList(IUsbDevice iUsbDevice, short vendorId, short productId) {
    List<IUsbDevice> iUsbDeviceListTemp = new ArrayList<>();
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
      iUsbDeviceListTemp.add(iUsbDevice);
    }
    /*
     * If the device is a HUB then recurse and scan the hub connected devices.
     * This is just normal recursion: Nothing special.
     */
    if (iUsbDevice.isUsbHub()) {
      for (Object object : ((UsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceListTemp.addAll(buildIUsbDeviceList((IUsbDevice) object, vendorId, productId));
      }
    }
    return iUsbDeviceListTemp;
  }

  /**
   * This forms an inclusive list of all IUsbDevices connected to this
   * IUsbDevice.
   * <p>
   * The list includes the provided device. If the device is also a hub, the
   * list will include all devices connected to it, recursively.
   * <p>
   * @param iUsbDevice The IUsbDevice to use.
   * @return An inclusive List of all connected IUsbDevices.
   */
  public List<IUsbDevice> getIUsbDeviceList(IUsbDevice iUsbDevice) {
    List<IUsbDevice> iUsbDeviceListTemp = new ArrayList<>();
    iUsbDeviceListTemp.add(iUsbDevice);
    if (iUsbDevice.isUsbHub()) {
//      List<IUsbDevice> attachedDevices = ((UsbHub) iUsbDevice).getAttachedIUsbDevices();
      for (Object attachedDevice : ((UsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceListTemp.addAll(getIUsbDeviceList((IUsbDevice) attachedDevice));
      }
    }
    return iUsbDeviceListTemp;
  }
}
