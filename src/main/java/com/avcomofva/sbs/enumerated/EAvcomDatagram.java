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
public enum EAvcomDatagram {

  // Datagram types that the sensor accepts --------------------------------------
  /**
   * Avcom Hardware Description Request Datagram.
   * <p>
   * Developer note: Avcom documentation says the type bytecode for
   * HARDWARE_DESCRIPTION_REQUEST is 0x07, which is the same as the
   * HARDWARE_DESCRIPTION_RESPONSE. This breaks bytecode matching. Since the
   * hardware never produces a HARDWARE_DESCRIPTION_REQUEST we hard-code it here
   * to 0x01 and hard-code it in the HARDWARE_DESCRIPTION_REQUEST instance as
   * 0x07 to avoid the conflict.
   */
  HARDWARE_DESCRIPTION_REQUEST((byte) 0x01),
  /**
   * Get LNB Power description request.
   */
  LNB_DESCRIPTION_REQUEST((byte) 0x0D),
  /**
   * Change Settings Request Datagram for Avcom devices
   */
  SETTINGS_REQUEST((byte) 0x04),
  /**
   * Avcom 8-bit Waveform Datagram Trace request
   */
  TRACE_REQUEST((byte) 0x03),
  /**
   * Avcom 12-bit Waveform Datagram Trace request
   */
  TRACE_REQUEST_12BIT((byte) 0x03),
  // Datagram types that the sensor creates --------------------------------------
  /**
   * Error datagram response.
   */
  ERROR_RESPONSE((byte) 0x60),
  /**
   * Avcom Hardware Description Datagram
   */
  HARDWARE_DESCRIPTION_RESPONSE((byte) 0x07),
  /**
   * LNB Power description response datagram for Avcom devices supporting LNB
   * power.
   */
  LNB_DESCRIPTION_RESPONSE((byte) 0x0D),
  /**
   * Reserved response datagram for hardware debugging
   */
  RESERVED_RESPONSE((byte) 0x08), // for hardware debugging
  /**
   * Waveform Response Datagram from Avcom devices 8-bit waveform packet
   */
  TRACE_RESPONSE((byte) 0x09), // 8-bit encoded waveform data
  /**
   * Waveform Response Datagram from Avcom devices 12-bit waveform packet
   */
  TRACE_RESPONSE_12BIT((byte) 0x0F), // 12-bit encoded waveform data
  // Key Bridge type extension ---------------------------------------------------
  /**
   * Key Bridge extended Trace data container.
   */
  TRACE_DATAGRAM((byte) 0xFF);

  private final byte byteCode;

  private EAvcomDatagram(byte byteCode) {
    this.byteCode = byteCode;
  }

  public byte getByteCode() {
    return byteCode;
  }

  /**
   * Get an Avcom datagram type by its corresponding byte code.
   * <p>
   * @param byteCode the byte code
   * @return the corresponding EAvcomDatagram instance
   */
  public static EAvcomDatagram fromByteCode(byte byteCode) {
    for (EAvcomDatagram eAvcomDatagramType : EAvcomDatagram.values()) {
      if (eAvcomDatagramType.getByteCode() == byteCode) {
        return eAvcomDatagramType;
      }
    }
    return null;
  }
}
