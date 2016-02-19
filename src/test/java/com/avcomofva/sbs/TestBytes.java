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

import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.enumerated.EDatagramType;
import javax.usb.utility.ByteUtility;
import org.junit.Test;

/**
 *
 * @author Jesse Caulfield
 */
public class TestBytes {

  @Test
  public void test() throws Exception {
    byte[] foo = {0, 1, 2, (byte) 0x55, 3, 6, (byte) 0xff, 5, 4, 6, 9, 8, 7};

    System.out.println(ByteUtility.toStringFormatted(foo));

    byte seven = (byte) 0x07;

    EDatagramType dg = EDatagramType.fromByteCode(seven);
    System.out.println(dg);

    byte[] hw = {
      (byte) 0x2, (byte) 0x0, (byte) 0x55, (byte) 0x7, (byte) 0x5a, (byte) 0x2, (byte) 0xc, (byte) 0x0, (byte) 0x0, (byte) 0xe4, (byte) 0xe1, (byte) 0xc0, (byte) 0x0, (byte) 0xf, (byte) 0x42, (byte) 0x40, (byte) 0x1e, (byte) 0x40, (byte) 0xf8, (byte) 0xa, (byte) 0xb, (byte) 0x0, (byte) 0x0, (byte) 0x8, (byte) 0xd, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x34, (byte) 0x30, (byte) 0x39, (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x1b, (byte) 0x20, (byte) 0x10, (byte) 0x14, (byte) 0x9, (byte) 0xa7, (byte) 0x94, (byte) 0xae, (byte) 0x0, (byte) 0x0, (byte) 0x40, (byte) 0x3f, (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0xe8, (byte) 0xb1, (byte) 0x82, (byte) 0x67, (byte) 0x79, (byte) 0x6e, (byte) 0x72, (byte) 0x7f, (byte) 0xb4, (byte) 0x80, (byte) 0xa1, (byte) 0x0, (byte) 0x42, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x23, (byte) 0x1d, (byte) 0x1d, (byte) 0x20, (byte) 0x2d, (byte) 0x28, (byte) 0xb9, (byte) 0xa1, (byte) 0x0, (byte) 0x5, (byte) 0xff, (byte) 0x3, (byte) 0x0
    };

    System.out.println(ByteUtility.toStringFormatted(hw));
    HardwareDescriptionResponse hardware = new HardwareDescriptionResponse(hw);
    System.out.println(hardware.toString());

  }

}
