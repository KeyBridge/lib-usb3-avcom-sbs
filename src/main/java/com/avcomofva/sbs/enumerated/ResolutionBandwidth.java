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
 * A enumerated set of Resolution bandwidth values supported by the Avcom SBS
 * hardware.
 * <p>
 * Each entry specifies the corresponding hardware configuration byte-code value
 * and speed in MHz.
 * <p>
 * See the text in Table 12 for
 *
 * @author Key Bridge LLC
 */
public enum ResolutionBandwidth {

//  THREE_MHZ(0x80, "3.00 MHz", 3.00), // 02/04/10 - per Jay - no longer supported
  ONE_MHZ(0x40, "1.00 MHz", 1.00),
  THREE_HUNDRED_KHZ(0x20, "0.30 MHz", 0.30),
  ONE_HUNDRED_KHZ(0x10, "0.10 MHz", 0.10),
  TEN_KHZ(0x08, "0.01 MHz", 0.01),
  THREE_KHZ(0x04, "0.003 MHz", 0.003);

  /**
   * The Avcom byte-code for this configuration.
   */
  private final int byteCode;
  /**
   * A human readable label / description.
   */
  private final String label;
  /**
   * The RBW value in MHz (i.e. 0.10 for One hundred kiloHertz.)
   */
  private final double MHz;

  private ResolutionBandwidth(int byteCode, String label, double MHz) {
    this.byteCode = byteCode;
    this.label = label;
    this.MHz = MHz;
//    this.englishLabel = englishLabel;
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
   * Returns the RBW value in MHz (i.e. 0.10 for One hundred kiloHertz.)
   *
   * @return the RBW value in MHz
   */
  public double getMHz() {
    return this.MHz;
  }

  /**
   * Returns the nearest possible RBW for the input value
   *
   * @param frequencyMHz a frequency of interest
   * @return the nearest RBW value supported by the hardware
   */
  public static ResolutionBandwidth findNearest(double frequencyMHz) {
    ResolutionBandwidth rbw = null;
    double distance = -1;
    for (ResolutionBandwidth r : ResolutionBandwidth.values()) {
      double currentDistance = Math.abs(r.getMHz() - frequencyMHz);
      if (distance == -1) {
        distance = currentDistance;
        rbw = r;
      } else if (currentDistance < distance) {
        rbw = r;
        distance = currentDistance;
      }
    }
    return rbw;
  }

  /**
   * Convert a hardware byte-code value into a RBW instance
   *
   * @param byteCode a byte-code value from the hardware.
   * @return the corresponding RBW value
   */
  public static ResolutionBandwidth fromByteCode(byte byteCode) {
    for (ResolutionBandwidth r : ResolutionBandwidth.values()) {
      if (r.getByteCode() == byteCode) {
        return r;
      }
    }
    return null;
  }
}
