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
package com.avcomofva.sbs.datagram.write;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.EDatagramType;
import com.avcomofva.sbs.enumerated.EStreamingType;
import javax.usb3.utility.ByteUtility;

/**
 * Avcom 12-bit Waveform Datagram (Firmware >= v2.10). From Table 6: Waveform
 * Transmission Settings.
 *
 * @author Jesse Caulfield
 * @deprecated 04/29/14 - 8 bits provides sufficient resolution for our needs.
 * The 12-bit trace response datagram is not supported in the AvcomSBS extended
 * data handling implementation.
 */
public class Waveform12BitRequest extends ADatagram {

  /**
   * The Datagram type.
   */
  private static final EDatagramType TYPE = EDatagramType.WAVEFORM_12BIT_REQUEST;
  /**
   * A pre-configured message to get a single 12-bit trace. This places value 5
   * at byte position 4. See Table 6 for byte order configuration.
   */
  private static final byte[] MESSAGE_BYTES = new byte[]{STX,
                                                         0,
                                                         3,
                                                         TYPE.getByteCode(),
                                                         EStreamingType.SEND_12BIT,
                                                         ETX};

  public Waveform12BitRequest() {
    super(TYPE);
    this.valid = true;
    this.elapsedTimeMillis = 1;
    this.transactionId = System.currentTimeMillis();
  }

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   *
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  @Override
  public void parse(byte[] bytes) throws Exception {
    // NO OP
  }

  @Override
  public byte[] serialize() {
    return MESSAGE_BYTES;
  }

  @Override
  public String toString() {
    return "TR12: [" + type
           + "] SN: [" + transactionId
           + "] Data: [" + ByteUtility.toString(MESSAGE_BYTES) + "]";
  }

  public String toStringBrief() {
    return "TR12: [" + type
           + "] SN: [" + transactionId + "]";
  }
}
