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
package com.avcomofva.sbs.datagram.write;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;

/**
 * Avcom Hardware Description Request Datagram. From Table 4: Get HW
 * Description.
 * <p>
 * Get HW Description is recommended to be performed first to determine
 * available control options and operating parameters.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class HardwareDescriptionRequest extends ADatagram {

  /**
   * A pre-configured message to get the HW description.
   * <p>
   * Developer note: Avcom documentation says the type bytecode for
   * HARDWARE_DESCRIPTION_REQUEST is 0x07, which is the same as the
   * HARDWARE_DESCRIPTION_RESPONSE. This breaks bytecode matching. Since the
   * hardware never produces a HARDWARE_DESCRIPTION_REQUEST we hard-code it in
   * the EAvcomDatagram as 0x01 and hard-code it in this
   * HARDWARE_DESCRIPTION_REQUEST instance as 0x07 to avoid the conflict.
   */
  private static final byte[] GET_HARDWARE_DESCRIPTION_REQUEST_MESSAGE = new byte[]{2, 0, 3, 7, 0, 3};

  /**
   * Construct a new HardwareDescriptionRequest datagram.
   */
  public HardwareDescriptionRequest() {
    super(EAvcomDatagram.HARDWARE_DESCRIPTION_REQUEST);
    this.valid = true;
    this.elapsedTimeMillis = 1;
    this.transactionId = System.currentTimeMillis();
  }

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
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
   * <p>
   * @return a byte array
   */
  @Override
  public byte[] serialize() {
    return GET_HARDWARE_DESCRIPTION_REQUEST_MESSAGE;
  }
}
