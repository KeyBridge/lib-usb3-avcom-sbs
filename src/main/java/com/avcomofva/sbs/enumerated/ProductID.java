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
 * Enumerated Avcom Product descriptions corresponding to defined ID byte
 * values.
 * <p>
 * From Table 1 & 2: Product ID and PCB Revision
 *
 * @author Jesse Caulfield
 */
public enum ProductID {

  /**
   * Software Test Device.
   */
  DUMMY((byte) 0x1a, "Software Test Device", 2500, 5, -100),
  /**
   * AVCom RSA-2150. 930 - 2,500 MHz.
   */
  RSA2150((byte) 0x3a, "AVCom RSA-2150", 2500, 930, -88),
  /**
   * AVCom RSA-1100. 5 - 1,300 MHz and 930 MHz - 2,500 MHz.
   */
  RSA1100((byte) 0x4a, "AVCom RSA-1100", 2500, 5, -88),
  /**
   * AVCom RSA-2500. 5 - 1,300 MHz and 930 MHz - 2,500 MHz.
   */
  RSA2500((byte) 0x5a, "AVCom RSA-2500", 2500, 5, -88);

  /**
   * The product ID (byte-code).
   */
  private final byte byteCode;
  /**
   * The Model name.
   */
  private final String model;
  /**
   * The maximum tuning frequency. (MHz)
   */
  private final double maxFrequency;
  /**
   * The minimum tuning frequency. (MHz)
   */
  private final double minFrequency;
  /**
   * The minimum sensitivity. (dBm)
   */
  private final double minSensitivity;

  private ProductID(byte code, String label, double maxFrequency, double minFrequency, double minSensivity) {
    this.byteCode = code;
    this.model = label;
    this.maxFrequency = maxFrequency;
    this.minFrequency = minFrequency;
    this.minSensitivity = minSensivity;
  }

  public byte getByteCode() {
    return this.byteCode;
  }

  public String getModel() {
    return this.model;
  }

  public double getMaxFrequency() {
    return maxFrequency;
  }

  public double getMinFrequency() {
    return minFrequency;
  }

  public double getMinSensitivity() {
    return minSensitivity;
  }

  /**
   * Get a ProductID from the corresponding product ID byte value.
   *
   * @param byteCode the product ID byte value.
   * @return the corresponding ProductID
   */
  public static ProductID fromByteCode(byte byteCode) {
    for (ProductID eAvcomProductID : ProductID.values()) {
      if (eAvcomProductID.getByteCode() == byteCode) {
        return eAvcomProductID;
      }
    }
    return null;
  }

}
