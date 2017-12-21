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
