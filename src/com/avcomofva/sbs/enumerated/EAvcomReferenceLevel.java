package com.avcomofva.sbs.enumerated;

public enum EAvcomReferenceLevel {

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
  MINUS_50(0x32, "-50 dBm", -90);

  private final int byteCode;
  private final String label;
  private final int waveformOffset;

  private EAvcomReferenceLevel(int b, String label, int waveformOffset) {
    this.byteCode = b;
    this.label = label;
    this.waveformOffset = waveformOffset;
    // this.englishLabel = englishLabel;
  }

  public int getByteCode() {
    return this.byteCode;
  }

  public String getLabel() {
    return this.label;
  }

  public int getWaveformOffset() {
    return this.waveformOffset;
  }

  /**
   * Return the reference level value in dB
   * <p>
   * @return the value in dB (e.g. eBm + 30 dB)
   */
  public int getValueDB() {
    return this.waveformOffset + 40;
  }

  /**
   * Returns the nearest possible RL for the input value
   * <p>
   * @param d the input reference level, range between [-10, -50] (dBm)
   * @return the nearest possible allowed reference level
   */
  public static EAvcomReferenceLevel findNearest(double d) {
    EAvcomReferenceLevel rl = MINUS_10;
    double distance = 0;
    for (EAvcomReferenceLevel r : EAvcomReferenceLevel.values()) {
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

  public static EAvcomReferenceLevel fromByteCode(byte byteCode) {
    for (EAvcomReferenceLevel r : EAvcomReferenceLevel.values()) {
      if (r.getByteCode() == byteCode) {
        return r;
      }
    }
    return null;
  }
}
