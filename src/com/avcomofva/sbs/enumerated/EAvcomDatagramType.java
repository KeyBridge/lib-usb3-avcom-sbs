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
package com.avcomofva.sbs.enumerated;

/**
 * Avcom Enumerated Datagram types. Each type contains its corresponding byte
 * code.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public enum EAvcomDatagramType {

  // Datagram types that the sensor accepts --------------------------------------
  TRACE_REQUEST((byte) 0x03),
  SETTINGS_REQUEST((byte) 0x04),
  HARDWARE_DESCRIPTION_REQUEST((byte) 0x07),
  LNB_DESCRIPTION_REQUEST((byte) 0x0D),
  // Datagram types that the sensor creates --------------------------------------
  HARDWARE_DESCRIPTION_RESPONSE((byte) 0x07),
  RESERVED_RESPONSE((byte) 0x08), // for hardware debugging
  TRACE_RESPONSE((byte) 0x09), // 8-bit encoded waveform data
  LNB_DESCRIPTION_RESPONSE((byte) 0x0D),
  TRACE_RESPONSE_12BIT((byte) 0x0F), // 12-bit encoded waveform data
  ERROR_RESPONSE((byte) 0x60),
  // Key Bridge type extension ---------------------------------------------------
  TRACE_RESPONSE_EXTENDED((byte) 0x0A);

  private final byte byteCode;

  private EAvcomDatagramType(byte byteCode) {
    this.byteCode = byteCode;
  }

  public byte getByteCode() {
    return byteCode;
  }
}
