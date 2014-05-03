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

import com.avcomfova.sbs.AvcomSBS;
import com.avcomfova.sbs.IDatagramListener;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.datagram.write.SettingsRequest;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.ftdi.FTDI;
import java.util.ArrayList;
import java.util.List;
import javax.usb.*;
import javax.usb.exception.UsbException;
import org.usb4java.javax.UsbHub;

/**
 *
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class Test_AvcomSBS implements IDatagramListener {

  private static final short vendorId = 0x0403;
  private static final short productId = 0x6001;

  public static void main(String[] args) throws UsbException, Exception {
    System.out.println("DEBUG Test_AvcomSBS");

    System.out.println("DEBUG FTDI_USB_CONFIGURATION_WRITE ");

    ByteUtil.toString(FTDI.FTDI_USB_CONFIGURATION_WRITE);

    Test_AvcomSBS test = new Test_AvcomSBS();

//    AvcomSBS avcom = new AvcomSBS();
//    IUsbDevice iUsbDevice = test.findAvcomIUsbDevice();
    List<IUsbDevice> iUsbDeviceList = FTDI.findFTDIDevices();
    if (iUsbDeviceList.isEmpty()) {
      System.out.println("No AvcomSBS devices attached. EXIT.");
      return;
    }

    System.out.println("DEBUG Test_AvcomSBS " + iUsbDeviceList);

    AvcomSBS avcom = new AvcomSBS(iUsbDeviceList.get(0));
    avcom.addListener(test);
    avcom.setSettings(new SettingsRequest(1250, 2500, EAvcomReferenceLevel.MINUS_10, EAvcomResolutionBandwidth.ONE_MHZ));

//    System.out.println("RUN");
//    avcom.run();
    avcom.start();

    Thread.sleep(15000);

    avcom.setSettings(new SettingsRequest(500, 300, EAvcomReferenceLevel.MINUS_50, EAvcomResolutionBandwidth.ONE_MHZ));
    Thread.sleep(3000);

    avcom.stop();
//    Map<String, String> configuration = avcom.getConfiguration();
//    System.out.println("Configuration -------------------");
//    for (Map.Entry<String, String> entry : configuration.entrySet()) {
//      System.out.println(entry.getKey() + " = " + entry.getValue());
//    }

    System.out.println("DEBUG Test_AvcomSBS DONE");
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

  @Override
  public void onDatagram(IDatagram datagram) {
    System.out.println("DEBUG Test_Acvom onDatagram " + datagram.toString() + " elapsed time " + datagram.getElapsedTimeMillis());
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
      for (Object object : ((UsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceList.addAll(getIUsbDeviceList((IUsbDevice) object, vendorId, productId));
      }
    }
    return iUsbDeviceList;
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
  private List<IUsbDevice> getIUsbDeviceList(IUsbDevice iUsbDevice) {
    List<IUsbDevice> iUsbDeviceList = new ArrayList<>();
    iUsbDeviceList.add(iUsbDevice);
    if (iUsbDevice.isUsbHub()) {
//      List<IUsbDevice> attachedDevices = ((UsbHub) iUsbDevice).getAttachedIUsbDevices();
      for (Object attachedDevice : ((UsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceList.addAll(getIUsbDeviceList((IUsbDevice) attachedDevice));
      }
    }
    return iUsbDeviceList;
  }
}
