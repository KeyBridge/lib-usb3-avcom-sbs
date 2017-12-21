/* 
 * Copyright (c) 2017, Key Bridge
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
import com.avcomofva.sbs.enumerated.DatagramType;

/**
 * Avcom Hardware Description Request Datagram.
 * <p>
 * Begin communication with a Avcom SBS board by sending a Get Hardware
 * Description request to discover basic information about the analyzer such as
 * Product ID, firmware revision, current Center Frequency, Span, etc.
 * <p>
 * From Table 4: Get HW Description.
 *
 * @author Jesse Caulfield
 */
public class HardwareDescriptionRequest extends ADatagram {

  /**
   * The Datagram type.
   */
  private static final DatagramType TYPE = DatagramType.HARDWARE_DESCRIPTION_REQUEST;
  /**
   * A pre-configured message to get the HW description.
   * <p>
 Developer note: Avcom documentation says the type bytecode for
 HARDWARE_DESCRIPTION_REQUEST is 0x07, which is the same as the
 HARDWARE_DESCRIPTION_RESPONSE. This breaks bytecode matching. Since the
 hardware never produces a HARDWARE_DESCRIPTION_REQUEST we hard-code it in
 the DatagramType as 0x01 and hard-code it in this HARDWARE_DESCRIPTION_REQUEST
 instance as 0x07 to avoid the conflict.
   */
  private static final byte[] MESSAGE_BYTES = new byte[]{STX,
                                                         0,
                                                         3,
                                                         7, // hard coded
                                                         0,
                                                         ETX};

  /**
   * Construct a new HardwareDescriptionRequest datagram.
   */
  public HardwareDescriptionRequest() {
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
    /**
     * NO OP.
     */
  }

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   *
   * @return a byte array
   */
  @Override
  public byte[] serialize() {
    return MESSAGE_BYTES;
  }
}
