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

import com.avcomfova.sbs.AvcomSBS;
import com.avcomfova.sbs.IDatagramListener;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.datagram.write.SettingsRequest;
import com.avcomofva.sbs.enumerated.EReferenceLevel;
import com.avcomofva.sbs.enumerated.EResolutionBandwidth;
import com.ftdichip.usb.FTDI;
import com.ftdichip.usb.FTDIUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.usb3.IUsbDevice;
import javax.usb3.IUsbHub;
import javax.usb3.IUsbServices;
import javax.usb3.UsbHostManager;
import javax.usb3.exception.UsbException;
import org.junit.Test;

/**
 *
 * @author Jesse Caulfield
 */
public class Test_AvcomSBS implements IDatagramListener {

  private static final short VENDOR_ID = 0x0403;
  private static final short PRODUCT_ID = 0x6001;

  @Test
  public void testAvcomSBS() throws UsbException, Exception {
    System.out.println("Test AvcomSBS");

    Test_AvcomSBS test = new Test_AvcomSBS();

//    AvcomSBS avcom = new AvcomSBS();
//    IUsbDevice iUsbDevice = test.findAvcomIUsbDevice();
    Collection<IUsbDevice> iUsbDeviceList = FTDIUtility.findFTDIDevices();
    if (iUsbDeviceList.isEmpty()) {
      System.out.println("No FTDI (ergo no AvcomSBS) devices attached. Aborting test.");
      return;
    }

    System.out.println("\nTest AvcomSBS at " + iUsbDeviceList.iterator().next());

    AvcomSBS avcom = new AvcomSBS(new FTDI(iUsbDeviceList.iterator().next()));
    avcom.addListener(test);
    avcom.setSettings(new SettingsRequest(1250, 2500, EReferenceLevel.MINUS_10, EResolutionBandwidth.ONE_MHZ));

    System.out.println("\nStarting Avcom SBS with full span sweep.");
    avcom.start();
    Thread.sleep(15000);
    System.out.println("\nChanging Avcom SBS with narrow span sweep.");
    avcom.setSettings(new SettingsRequest(500, 300, EReferenceLevel.MINUS_50, EResolutionBandwidth.ONE_MHZ));
    Thread.sleep(3000);
    System.out.println("\nStopping Avcom SBS");
    avcom.stop();

    System.out.println("\nDumping Configuration");
    Map<String, String> configuration = avcom.getConfiguration();
    System.out.println("  Avcom SBS ");
    System.out.println(String.format("  %-30s : %s", "Configuration", "Value"));
    System.out.println("--------------------------------------------------------");
    for (Map.Entry<String, String> entry : configuration.entrySet()) {
      System.out.println(String.format("%-30s : %s", entry.getKey(), entry.getValue()));
    }

    System.out.println("\nTest AvcomSBS DONE");
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
    List<IUsbDevice> iUsbDevices = getIUsbDeviceList(virtualRootUsbHub, VENDOR_ID, PRODUCT_ID);

    return iUsbDevices.isEmpty() ? null : iUsbDevices.get(0);
  }

  @Override
  public void onDatagram(IDatagram datagram) {
    System.out.println("DEBUG Test_Acvom onDatagram " + datagram.toString() + " elapsed time " + datagram.getElapsedTime());
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

  /**
   * This forms an inclusive list of all IUsbDevices connected to this
   * IUsbDevice.
   * <p>
   * The list includes the provided device. If the device is also a hub, the
   * list will include all devices connected to it, recursively.
   *
   * @param iUsbDevice The IUsbDevice to use.
   * @return An inclusive List of all connected IUsbDevices.
   */
  private List<IUsbDevice> getIUsbDeviceList(IUsbDevice iUsbDevice) {
    List<IUsbDevice> iUsbDeviceList = new ArrayList<>();
    iUsbDeviceList.add(iUsbDevice);
    if (iUsbDevice.isUsbHub()) {
//      List<IUsbDevice> attachedDevices = ((UsbHub) iUsbDevice).getAttachedIUsbDevices();
      for (Object attachedDevice : ((IUsbHub) iUsbDevice).getAttachedUsbDevices()) {
        iUsbDeviceList.addAll(getIUsbDeviceList((IUsbDevice) attachedDevice));
      }
    }
    return iUsbDeviceList;
  }
}
