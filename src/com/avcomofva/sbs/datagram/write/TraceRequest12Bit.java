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
import com.avcomofva.sbs.datagram.read.TraceResponse12Bit;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.keybridgeglobal.sensor.util.ByteUtil;

/**
 * Avcom 12-bit Waveform Datagram (Firmware >= v2.10). From Table 6: Waveform
 * Transmission Settings.
 * <p>
 * The device responds with a {@linkplain TraceResponse12Bit}
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class TraceRequest12Bit extends ADatagram {

  /**
   * A pre-configured message to get a single 12-bit trace. This places value 5
   * at byte position 4. See Table 6 for byte order configuration.
   */
  private static final byte[] GET_TRACE_REQUEST_MESSAGE = new byte[]{2, 0, 3, 3, 5, 3};

  public TraceRequest12Bit() {
    super(EAvcomDatagram.TRACE_REQUEST);
    this.valid = true;
    this.elapsedTimeMS = 1;
    this.transactionId = System.currentTimeMillis();
  }

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @return TRUE if parse is successful
   */
  @Override
  public boolean parse(byte[] bytes) {
    return false;
  }

  @Override
  public byte[] serialize() {
    return GET_TRACE_REQUEST_MESSAGE;
  }

  @Override
  public String toString() {
    return "TR12: [" + datagramType
      + "] SN: [" + transactionId
      + "] Data: [" + ByteUtil.toString(GET_TRACE_REQUEST_MESSAGE) + "]";
  }

  public String toStringBrief() {
    return "TR12: [" + datagramType
      + "] SN: [" + transactionId + "]";
  }
}
