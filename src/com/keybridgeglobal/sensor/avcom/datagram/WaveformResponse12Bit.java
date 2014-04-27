package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;

/**
 * Waveform Response Datagram from Avcom devices 12-bit waveform packet
 * (Frimware rev >= v2.10 Table 11
 * <p>
 * @author jesse
 */
public class WaveformResponse12Bit implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  //----------------------------------------------------------------------------
  // Datagram housekeeping
  public static final byte datagramType = IDatagram.WAVEFORM_RESPONSE_12BIT_ID;
  private byte[] datagramData;
  private Boolean isValid = false;
  private int elapsedTimeMS;
  private String jobSerialNumber;
  private String sensorSerialNumber;
  //----------------------------------------------------------------------------
  // Setting values                     Byte Location in Data
  private final byte[] waveform = new byte[IDatagram.TRACE_DATA_12BIT_LENGTH]; // 4-483: 479 12-bit points scaled per description in constructor
  private int productId;                                                       // 484
  private double centerFrequencyMHz;                                              // 485-488
  private double spanMHz;                                                         // 489-492
  private EAvcomReferenceLevel referenceLevel;                                                  // 493
  private EAvcomResolutionBandwidth resolutionBandwidth;                                             // 494
  private int inputConnector;                                                  // 495
  private final byte[] internalExtender = new byte[2];                                  // 496-497
  private final byte[] externalExtender = new byte[2];                                  // 498-499
  private int lnbPower;                                                        // 500
  private int reserved01;                                                      // 501  typically 0xff
  private int reserved02;                                                      // 502  typically 0xff

  /**
   * Empty contructor for testing
   */
  public WaveformResponse12Bit() {
  }

  /**
   * Full contructor with bytes
   * <p>
   * @param bytes
   */
  public WaveformResponse12Bit(byte[] bytes) {
    this.isValid = this.parse(bytes);
  }

  @SuppressWarnings("static-access")
  public byte getSensorTypeId() {
    return this.datagramType;
  }

  /**
   * Get the raw data
   * <p>
   * @return
   */
  public byte[] getData() {
    return this.datagramData;
  }

  public boolean parse(byte[] bytes) {
    // Copy the bytes
    this.datagramData = new byte[bytes.length];
    System.arraycopy(bytes, 0, datagramData, 0, bytes.length);

    // Parse the bytes
    int i;
    // populate local values
    for (i = 0; i < IDatagram.TRACE_DATA_12BIT_LENGTH; i++) {
      this.waveform[i] = bytes[i + 4];
    }
    this.productId = bytes[484];
    this.centerFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 485) / 10000;
    this.spanMHz = ByteUtil.intFrom4Bytes(bytes, 489) / 10000;
    this.referenceLevel = EAvcomReferenceLevel.fromByteCode(bytes[493]);
    this.resolutionBandwidth = EAvcomResolutionBandwidth.fromByteCode(bytes[494]);
    this.inputConnector = bytes[495] - 9;
    for (i = 0; i < 2; i++) {
      this.internalExtender[i] = bytes[496 + i];
    }
    for (i = 0; i < 2; i++) {
      this.externalExtender[i] = bytes[498 + i];
    }
    this.lnbPower = bytes[500];
    this.reserved01 = bytes[501];
    this.reserved02 = bytes[502];

    //----------------------------------------------------------------------------
    if (ByteUtil.twoByteIntFromBytes(bytes, 1) == IDatagram.TRACE_RESPONSE_LENGTH) {
      return true;
    } else {
      return false;
    }
  }

  public byte[] serialize() {
    return this.datagramData;
  }

  public Boolean isValid() {
    return this.isValid;
  }

  //----------------------------------------------------------------------------
  // Get methods for internal parameters
  /**
   * Get the raw Byte values returned from the device
   * <p>
   * @return
   */
  public byte[] getWaveformByte() {
    return this.waveform;
  }

  /**
   * <b>NOT IMPLEMENTED</b><br>
   * Returns a 479 array of double values with units of dBm <br>
   * Per table 9, waveform data (dBm) = 0.20 * (int) byteValue +
   * referenceLevelOffset <br>
   * <p>
   * @return
   */
  public double[] getWaveformDBm() {
    //    double[] waveformDBm = new double[waveform.length];
    //    for (int i = 0; i < waveform.length; i++) {
    //      waveformDBm[i] = (0.20 * waveform[i] + this.referenceLevel.getWaveformOffset());
    //    }
    //    return waveformDBm;
    return null;
  }

  public int getProductId() {
    return this.productId;
  }

  public double getCenterFrequencyMHz() {
    return this.centerFrequencyMHz;
  }

  public double getSpanMHz() {
    return this.spanMHz;
  }

  public EAvcomReferenceLevel getReferenceLevel() {
    return this.referenceLevel;
  }

  public EAvcomResolutionBandwidth getResolutionBandwidth() {
    return this.resolutionBandwidth;
  }

  public int getInputConnector() {
    return this.inputConnector;
  }

  public byte[] getInternalExtender() {
    return this.internalExtender;
  }

  public byte[] getExternalExtender() {
    return this.externalExtender;
  }

  public int getLnbPower() {
    return this.lnbPower;
  }

  public int getReserved01() {
    return this.reserved01;
  }

  public int getReserved02() {
    return this.reserved02;
  }

  /**
   * Get the time required to create this datagram
   * <p>
   * @return
   */
  public int getElapsedTimeMS() {
    return this.elapsedTimeMS;
  }

  /**
   * Set the time required to create this datagram
   * <p>
   * @param elapsedTimeMS
   */
  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public String getTransactionId() {
    return this.jobSerialNumber;
  }

  public void setTransactionId(String serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public String getSensorSerialNumber() {
    return sensorSerialNumber;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  public String toStringBrief() {
    if (isValid) {
      return "WAV12: CF [" + centerFrequencyMHz + "] Span [" + spanMHz + "] RL [" + referenceLevel + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  @Override
  public String toString() {
    if (isValid) {
      //      String s = "";
      //      double[] wave = this.getWaveformDBm();
      //      for (int i = 0; i < wave.length; i++) {
      //        s += "[" + wave[i] + "]";
      //      }

      return "Waveform Response Datagram 12Bit Contents"
        + "\n index   name             value"
        + "\n --------------------------------"
        + "\n this.datagramType:             " + datagramType
        + "\n this.isValid:                  " + isValid
        + "\n this.datagramData length:      " + datagramData.length
        + "\n 4-324   waveform length:       " + waveform.length
        + "\n 324     productId:             " + productId
        + "\n 325-328 centerFrequencyMHz:    " + centerFrequencyMHz
        + "\n 329-332 spanMHz:               " + spanMHz
        + "\n 333     referenceLevel:        " + referenceLevel
        + "\n 334     resolutionBandwidth:   " + resolutionBandwidth
        + "\n 335     inputConnector:        " + inputConnector
        + "\n 336-337 internalExtender:    0x" + ByteUtil.toString(internalExtender)
        + "\n 338-339 externalExtender:    0x" + ByteUtil.toString(externalExtender)
        + "\n 340     lnbPower:            0x" + Integer.toHexString(lnbPower)
        + "\n 341     reserved01:          0x" + Integer.toHexString(reserved01)
        + "\n 342     reserved02:          0x" + Integer.toHexString(reserved02) //              + "\n waveform data in DBM:          " + s
        ;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

}
