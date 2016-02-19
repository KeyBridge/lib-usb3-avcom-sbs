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

import java.util.List;
import javax.usb3.*;
import javax.usb3.exception.UsbDisconnectedException;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbNotActiveException;
import org.junit.Test;

/**
 *
 * @author Jesse Caulfield
 */
public class Test_USBConnect {

  @Test
  public void test() throws Exception {

    IUsbDevice iUsbDevice = new Test_FTDI().findAvcomIUsbDevice();
//    IUsbDevice iUsbDevice = null;
    for (IUsbConfiguration iUsbConfiguration : iUsbDevice.getUsbConfigurations()) {
      System.out.println("config " + iUsbConfiguration);
    }

    /**
     * Interfaces
     * <p>
     * When you want to communicate with an interface or with endpoints of this
     * interface then you have to claim it before using it and you have to
     * release it when you are finished. Example:
     */
    IUsbConfiguration configuration = iUsbDevice.getActiveUsbConfiguration();
//    UsbConfiguration configuration = iUsbDevice.getUsbConfiguration((byte) 1);
    System.out.println("active config " + configuration);
    IUsbInterface iUsbInterface = (IUsbInterface) configuration.getUsbInterfaces().iterator().next();
    System.out.println("usb interface " + iUsbInterface);

//    IUsbInterface iUsbInterface = configuration.getIUsbInterface((byte) 0);
//    for (Object object : configuration.getIUsbInterfaces()) {
//      iUsbInterface = (IUsbInterface) object;
//      System.out.println("iUsbInterface " + iUsbInterface);
//    }
//    IUsbInterface iUsbInterface = configuration.getIUsbInterface((byte) 0x00);
//    IUsbInterface iface = configuration.getIUsbInterface((byte) 1);
    /**
     * It is possible that the interface you want to communicate with is already
     * used by a kernel driver. In this case you can try to force the claiming
     * by passing an interface policy to the claim method:
     */
    if (iUsbInterface == null) {
      System.out.println("null usb interface");
      return;
    }
    System.out.println("try to claim " + iUsbInterface);
    try {
      iUsbInterface.claim();
    } catch (UsbException | UsbNotActiveException | UsbDisconnectedException usbException) {
      System.err.println("FAIL to claim " + usbException.getMessage());
    }

    List<IUsbEndpoint> endpoints = iUsbInterface.getUsbEndpoints();

    // Ignore interface if it does not have two endpoints
    if (endpoints.size() != 2) {
      System.out.println("DEBUG does not have 2 endpoints ");
//      continue;
    }
    // Ignore interface if it does not match the ADB specs
//    if (!isAdbInterface(iface)) {      continue;    }
    IUsbEndpointDescriptor ed1 = endpoints.get(0).getUsbEndpointDescriptor();
    IUsbEndpointDescriptor ed2 = endpoints.get(1).getUsbEndpointDescriptor();
    IUsbEndpoint ep1 = endpoints.get(0);

    // Determine which endpoint is in and which is out. If both
    // endpoints are in or out then ignore the interface
//    byte a1 = ed1.bEndpointAddress();
//    byte a2 = ed2.bEndpointAddress();
//    byte in, out;
//    if (((a1 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) && ((a2 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
//      in = a1;
//      out = a2;
//      System.out.println("debug in is a1 and out is a2");
//    } else if (((a2 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) && ((a1 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
//      out = a1;
//      in = a2;
//      System.out.println("debug in is a2 and out is a1");
//    }
//
//    UsbControlIrp irp = iUsbDevice.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_TYPE_CLASS | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
//                                                       (byte) 0x09,
//                                                       (short) 2,
//                                                       (short) 1);
//    irp.setData(new HardwareDescriptionRequest().serialize());
//    iUsbDevice.syncSubmit(irp);
//    iUsbInterface.claim(new IUsbInterfacePolicy() {
//
//      @Override
//      public boolean forceClaim(IUsbInterface iUsbInterface) {
//        return true;
//      }
//    });
//    for (Object object : iUsbInterface.getUsbEndpoints()) {
//      UsbEndpoint usbEndpoint = (UsbEndpoint) object;
//      System.out.println("endpoint type " + usbEndpoint.getType() + " direction " + usbEndpoint.getDirection() + " " + usbEndpoint.getUsbEndpointDescriptor());
//
//      System.out.println("endpoint " + UsbConst.ENDPOINT_DIRECTION_IN);
//
//    }
//    iface.claim();
//    iface.claim(new IUsbInterfacePolicy() {
//      @Override
//      public boolean forceClaim(IUsbInterface iUsbInterface) {
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
