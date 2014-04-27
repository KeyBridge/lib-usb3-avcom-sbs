package com.avcomofva.sbs.enumerated;

public enum EAvcomResolutionBandwidth {

  //  THREE_MHZ(0x80, "3.00 MHz"),         // 02/04/10 - per Jay - no longer supported
  ONE_MHZ(0x40, "1.00 MHz", 1.00),
  THREE_HUNDRED_KHZ(0x20, "0.30 MHz", 0.30),
  ONE_HUNDRED_KHZ(0x10, "0.10 MHz", 0.10),
  TEN_KHZ(0x08, "0.01 MHz", 0.01);
  private final int byteCode;
  private final String label;
  private final double MHz;

  private EAvcomResolutionBandwidth(int byteCode, String label, double MHz) {
    this.byteCode = byteCode;
    this.label = label;
    this.MHz = MHz;
//    this.englishLabel = englishLabel;
  }

  /**
   * Returns the RBW value in MHz (i.e. 0.10 for One hundred kiloHertz.)
   * <p>
   * @return
   */
  public double getMHz() {
    return this.MHz;
  }

  /**
   * Returns the Avcom bytecode for this resolution bandwidth value
   * <p>
   * @return
   */
  public int getByteCode() {
    return this.byteCode;
  }

  /**
   * Returns and English label
   * <p>
   * @return
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Returns the nearest possible RBW for the input value
   * <p>
   * @param frequencyMHz
   * @return
   */
  public static EAvcomResolutionBandwidth findNearest(double frequencyMHz) {
    EAvcomResolutionBandwidth rbw = null;
    double distance = -1;
    for (EAvcomResolutionBandwidth r : EAvcomResolutionBandwidth.values()) {
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

  public static EAvcomResolutionBandwidth fromByteCode(byte byteCode) {
    for (EAvcomResolutionBandwidth r : EAvcomResolutionBandwidth.values()) {
      if (r.getByteCode() == byteCode) {
        return r;
      }
    }
    return null;
  }
}
