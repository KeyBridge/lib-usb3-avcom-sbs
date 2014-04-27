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
import java.util.List;
import javax.usb.*;

/**
 *
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class Test_USBConnect {

  public static void main(String[] args) throws Exception {
    UsbDevice usbDevice = null;

    for (Object object : usbDevice.getUsbConfigurations()) {
      UsbConfiguration config = (UsbConfiguration) object;
      System.out.println("config " + config);
    }

    /**
     * Interfaces
     * <p>
     * When you want to communicate with an interface or with endpoints of this
     * interface then you have to claim it before using it and you have to
     * release it when you are finished. Example:
     */
    UsbConfiguration configuration = usbDevice.getActiveUsbConfiguration();
//    UsbConfiguration configuration = usbDevice.getUsbConfiguration((byte) 1);
    System.out.println("active config " + configuration);
    UsbInterface usbInterface = (UsbInterface) configuration.getUsbInterfaces().get(0);
    System.out.println("usb interface " + usbInterface);

//    UsbInterface usbInterface = configuration.getUsbInterface((byte) 0);
//    for (Object object : configuration.getUsbInterfaces()) {
//      usbInterface = (UsbInterface) object;
//      System.out.println("usbInterface " + usbInterface);
//    }
//    UsbInterface usbInterface = configuration.getUsbInterface((byte) 0x00);
//    UsbInterface iface = configuration.getUsbInterface((byte) 1);
    /**
     * It is possible that the interface you want to communicate with is already
     * used by a kernel driver. In this case you can try to force the claiming
     * by passing an interface policy to the claim method:
     */
    if (usbInterface == null) {
      System.out.println("null usb interface");
      return;
    }
    System.out.println("try to claim " + usbInterface);
    try {
      usbInterface.claim();
    } catch (UsbException | UsbNotActiveException | UsbDisconnectedException usbException) {
      System.err.println("FAIL to claim " + usbException.getMessage());
    }

    List<UsbEndpoint> endpoints = usbInterface.getUsbEndpoints();

    // Ignore interface if it does not have two endpoints
    if (endpoints.size() != 2) {
      System.out.println("DEBUG does not have 2 endpoints ");
//      continue;
    }

    // Ignore interface if it does not match the ADB specs
//    if (!isAdbInterface(iface)) {      continue;    }
    UsbEndpointDescriptor ed1 = endpoints.get(0).getUsbEndpointDescriptor();
    UsbEndpointDescriptor ed2 = endpoints.get(1).getUsbEndpointDescriptor();
    UsbEndpoint ep1 = endpoints.get(0);

    // Determine which endpoint is in and which is out. If both
    // endpoints are in or out then ignore the interface
    byte a1 = ed1.bEndpointAddress();
    byte a2 = ed2.bEndpointAddress();
    byte in, out;
    if (((a1 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) && ((a2 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
      in = a1;
      out = a2;
      System.out.println("debug in is a1 and out is a2");
    } else if (((a2 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) && ((a1 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
      out = a1;
      in = a2;
      System.out.println("debug in is a2 and out is a1");
    }

    UsbControlIrp irp = usbDevice.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_TYPE_CLASS | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
                                                      (byte) 0x09,
                                                      (short) 2,
                                                      (short) 1);
    irp.setData(new HardwareDescriptionRequest().serialize());
    usbDevice.syncSubmit(irp);

//    usbInterface.claim(new UsbInterfacePolicy() {
//
//      @Override
//      public boolean forceClaim(UsbInterface usbInterface) {
//        return true;
//      }
//    });
//    for (Object object : usbInterface.getUsbEndpoints()) {
//      UsbEndpoint usbEndpoint = (UsbEndpoint) object;
//      System.out.println("endpoint type " + usbEndpoint.getType() + " direction " + usbEndpoint.getDirection() + " " + usbEndpoint.getUsbEndpointDescriptor());
//
//      System.out.println("endpoint " + UsbConst.ENDPOINT_DIRECTION_IN);
//
//    }
//    iface.claim();
//    iface.claim(new UsbInterfacePolicy() {
//      @Override
//      public boolean forceClaim(UsbInterface usbInterface) {
//        return true;
//      }
//    });
//    try {
////    ... Communicate with the interface or endpoints ...
//    } finally {
//      iface.release();
//    }
  }

}
