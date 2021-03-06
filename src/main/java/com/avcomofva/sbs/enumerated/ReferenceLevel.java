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
package com.avcomofva.sbs.enumerated;

/**
 * A enumerated set of reference level setting values supported by the Avcom SBS
 * hardware.
 * <p>
 * Each entry specifies the corresponding hardware configuration byte-code value
 * and value in dBm.
 *
 * @author Key Bridge LLC
 */
public enum ReferenceLevel {

  /**
   * -10 dBm.
   */
  MINUS_10(0x0a, "-10 dBm", -50),
  /**
   * -20 dBm.
   */
  MINUS_20(0x14, "-20 dBm", -60),
  /**
   * -30 dBm.
   */
  MINUS_30(0x1e, "-30 dBm", -70),
  /**
   * -40 dBm.
   */
  MINUS_40(0x28, "-40 dBm", -80),
  /**
   * -50 dBm.
   */
  MINUS_50(0x32, "-50 dBm", -90),
  /**
   * -70 dBm.
   */
  MINUS_70(0x6E, "-70 dBm", -110);

  /**
   * The Avcom byte-code for this configuration. This is the (negative)
   * reference level in hexadecimal.
   */
  private final int byteCode;
  /**
   * A human readable label / description.
   */
  private final String label;
  /**
   * A hardware scaling value to convert the hardware-reported reference level
   * to a value in dB.
   */
  private final int waveformOffset;

  private ReferenceLevel(int b, String label, int waveformOffset) {
    this.byteCode = b;
    this.label = label;
    this.waveformOffset = waveformOffset;
    // this.englishLabel = englishLabel;
  }

  /**
   * Returns the Avcom hardware bytecode for this value
   *
   * @return the Avcom bytecode value
   */
  public int getByteCode() {
    return this.byteCode;
  }

  /**
   * Returns a human readable label.
   *
   * @return a human readable label / description.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Get the hardware scaling value to convert the hardware-reported reference
   * level to a value in dB.
   *
   * @return the hardware scaling value
   */
  public int getWaveformOffset() {
    return this.waveformOffset;
  }

  /**
   * Return the hardware scaling value in dBm.
   * <p>
   * This is just the waveformOffset value shifted by +40.
   * <p>
   * This value is used to scale the sensor waveform data from 8-bit points to
   * dB. See Table 10 (Byte #4-323) and Table 11 (Byte #4-483).
   *
   * @return the reference level offset value in dBm.
   */
  public int getValueDBm() {
    return this.waveformOffset + 40;
  }

  /**
   * Returns the nearest possible RL for the input value
   *
   * @param d the input reference level, range between [-10, -50] (dBm)
   * @return the nearest possible allowed reference level
   */
  public static ReferenceLevel findNearest(double d) {
    ReferenceLevel rl = MINUS_10;
    double distance = 0;
    for (ReferenceLevel r : ReferenceLevel.values()) {
      double currentDistance = Math.abs(r.getWaveformOffset() + 40 - d);
      if (distance == 0) {
        distance = currentDistance;
        rl = r;
      } else if (currentDistance < distance) {
        rl = r;
        distance = currentDistance;
      }
    }
    return rl;
  }

  public static ReferenceLevel fromByteCode(byte byteCode) {
    for (ReferenceLevel r : ReferenceLevel.values()) {
      if (r.getByteCode() == byteCode) {
        return r;
      }
    }
    return null;
  }
}
