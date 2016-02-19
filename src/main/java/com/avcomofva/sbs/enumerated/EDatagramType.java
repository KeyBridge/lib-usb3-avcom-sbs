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
package com.avcomofva.sbs.enumerated;

/**
 * Enumerated datagram types for the Avcom SBS Single Board Embedded Spectrum
 * Analyzer.
 * <p>
 * Each type contains its corresponding byte code.
 * <p>
 * A Datagram is a “self-contained, independent entity of data carrying
 * sufficient information to be routed from the source to the destination
 * computer without reliance on earlier exchanges between this source and
 * destination computer and the transporting network.”
 *
 * @author Jesse Caulfield
 */
public enum EDatagramType {
  /**
   * Change Control Settings Request Datagram for Avcom devices. (Table 12)
   */
  /**
   * Change Control Settings Request Datagram for Avcom devices. (Table 12)
   */
  /**
   * Change Control Settings Request Datagram for Avcom devices. (Table 12)
   */
  /**
   * Change Control Settings Request Datagram for Avcom devices. (Table 12)
   */
  SETTINGS_REQUEST((byte) 0x04),
  /**
   * Avcom Hardware Description Request Datagram.
   * <p>
   * Developer note: Avcom documentation says the type bytecode for
   * HARDWARE_DESCRIPTION_REQUEST is 0x07, which is the same as the
   * HARDWARE_DESCRIPTION_RESPONSE. This breaks bytecode matching. Since the
   * hardware never produces a HARDWARE_DESCRIPTION_REQUEST we hard-code it here
   * to 0x01 and hard-code the HARDWARE_DESCRIPTION_RESPONSE as 0x07 to avoid
   * the conflict.
   */
  HARDWARE_DESCRIPTION_REQUEST((byte) 0x01),
  /**
   * Avcom Hardware Description. (Table 7)
   */
  HARDWARE_DESCRIPTION_RESPONSE((byte) 0x07),
  /**
   * Get LNB Power description. (Table 5)
   */
  LNB_DESCRIPTION_REQUEST((byte) 0x0D),
  /**
   * LNB Power description. (For Avcom devices supporting LNB power.) (Table 8)
   */
  LNB_DESCRIPTION_RESPONSE((byte) 0x0D),
  /**
   * Avcom 8-bit Waveform Datagram request. (Table 6)
   */
  WAVEFORM_8BIT_REQUEST((byte) 0x03),
  /**
   * 8-bit Waveform Response. (Table 10)
   */
  WAVEFORM_8BIT_RESPONSE((byte) 0x09),
  /**
   * Avcom 12-bit Waveform Datagram request. (Table 6)
   */
  WAVEFORM_12BIT_REQUEST((byte) 0x03),
  /**
   * 12-bit Waveform Response. (Table 11)
   */
  WAVEFORM_12BIT_RESPONSE((byte) 0x0F),
  /**
   * Error datagram response. (Table 16)
   */
  ERROR_RESPONSE((byte) 0x60),
  /**
   * Reserved response datagram for hardware debugging. (Table 15)
   */
  RESERVED_RESPONSE((byte) 0x08),
  /**
   * Key Bridge extended Trace data container.
   */
  WAVEFORM((byte) 0xFF);

  /**
   * The datagram ID (byte-code).
   */
  private final byte byteCode;

  private EDatagramType(byte byteCode) {
    this.byteCode = byteCode;
  }

  /**
   * Get the datagram ID (byte-code).
   *
   * @return the datagram ID (byte-code).
   */
  public byte getByteCode() {
    return byteCode;
  }

  /**
   * Get an Avcom datagram type by its corresponding byte code.
   *
   * @param byteCode the byte code
   * @return the corresponding EDatagramType instance
   */
  public static EDatagramType fromByteCode(byte byteCode) {
    for (EDatagramType eAvcomDatagramType : EDatagramType.values()) {
      if (eAvcomDatagramType.getByteCode() == byteCode) {
        return eAvcomDatagramType;
      }
    }
    throw new IllegalArgumentException("Unrecognized Avcom datagram type: " + byteCode);
  }
}
