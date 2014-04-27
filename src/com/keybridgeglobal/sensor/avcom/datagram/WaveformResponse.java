package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Waveform Response Datagram from Avcom devices 8-bit waveform packet (Frimware
 * rev >= v1.9 Table 9
 * <p>
 * @author jesse
 */
public class WaveformResponse implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  //----------------------------------------------------------------------------
  // Datagram housekeeping
  public static final byte datagramType = IDatagram.WAVEFORM_RESPONSE_ID;
  private byte[] datagramData;
  private Boolean isValid = false;
  private int elapsedTimeMS;                                             // how long it took for the hardware to create this Datagram in milliseconds
  private String jobSerialNumber;                                           // the job this waveform is associated with
  private String sensorSerialNumber;                                        // serialNumber of the sensor - presently set to avcom serial number
  private boolean isSaturated = false;                                  // did the sensor detect saturation?
  //----------------------------------------------------------------------------
  // Setting values                     Byte Location in Data
  private final byte[] waveform = new byte[IDatagram.TRACE_DATA_LENGTH]; // 4-323: 320 8-bit points scaled per description in constructor
  private int productId;                                                 // 324
  private double centerFrequencyMHz;                                        // 325-328
  private double spanMHz;                                                   // 329-332
  private EAvcomReferenceLevel referenceLevel;                                            // 333
  private EAvcomResolutionBandwidth resolutionBandwidth;                                       // 334
  private int inputConnector;                                            // 335
  private final byte[] internalExtender = new byte[2];                            // 336-337
  private final byte[] externalExtender = new byte[2];                            // 338-339
  private int lnbPower;                                                  // 340
  private int reserved01;                                                // 341  typically 0xff
  private int reserved02;                                                // 342  typically 0xff

  /**
   * Empty contructor for testing
   */
  public WaveformResponse() {
  }

  /**
   * Full contructor with bytes
   * <p>
   * @param bytes
   */
  public WaveformResponse(byte[] bytes) {
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
    for (i = 0; i < IDatagram.TRACE_DATA_LENGTH; i++) {
      this.waveform[i] = bytes[i + 4];
    }
    this.productId = bytes[324];
    this.centerFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 325) / 10000;
    this.spanMHz = ByteUtil.intFrom4Bytes(bytes, 329) / 10000;
    this.referenceLevel = EAvcomReferenceLevel.fromByteCode(bytes[333]);
    this.resolutionBandwidth = EAvcomResolutionBandwidth.fromByteCode(bytes[334]);
    this.inputConnector = bytes[335] - 9;
    for (i = 0; i < 2; i++) {
      this.internalExtender[i] = bytes[336 + i];
    }
    for (i = 0; i < 2; i++) {
      this.externalExtender[i] = bytes[338 + i];
    }
    this.lnbPower = bytes[340];
    this.reserved01 = bytes[341];
    this.reserved02 = bytes[342];

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
   * If true, then the detector may be saturating on a powerful signal. Consider
   * reducing the reference level.
   * <p>
   * @return
   */
  public boolean isSaturated() {
    return isSaturated;
  }

  /**
   * Returns a 320 array of integer values with units of dBm <br>
   * Per table 9, waveform data (dBm) = 0.20 * (unsigned int) byteValue +
   * referenceLevelOffset <br>
   * If the sensor value is above 230 (90%) then raise the 'saturated' flag.
   * This indicates that the reference level may be reduced if desired.
   * <p>
   * @return
   */
  public double[] getWaveformDBm() {
    double[] waveformDBm = new double[waveform.length];
    int uInt = 0;
    for (int i = 0; i < waveform.length; i++) {
      uInt = waveform[i];
      // accommodation for unsigned int
      if (waveform[i] < 0) {
        uInt = 255 + waveform[i];
      }
      // if greater than 90%
      if (uInt >= 225) {
        this.isSaturated = true;
      }
      waveformDBm[i] = (0.20 * uInt + this.referenceLevel.getWaveformOffset());
    }
    return waveformDBm;
  }

  /**
   * Assemble a Map object from the waveform data.<br>
   * In a waveform, each sample byte can be described by it's own center
   * frequency and span.<br>
   * Aggregated waveform data is stored at dBm values with centerFrequency
   * indices.<br>
   * <p>
   * <
   * pre>
   * | <----------------------- span s --------------------> | | ^
   * startFrequency ^ centerFrequency | |[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]| |
   * ^cfi
   * <p>
   * Where: startFrequency = centerFrequency - span / 2 i = byte count (from 0
   * to length of sample cfi = resolutionBandwidth * i + startFrequency
   * </pre>
   * <p>
   * @return
   */
  public Map<Double, Double> getWaveformMap() {
    // need to scale the data if necessary
    Map<Double, Double> w = new TreeMap<>();
    double[] waveformDBm = getWaveformDBm();
    //    double rbwMHz = resolutionBandwidth.getMHz();
    double startMHz = centerFrequencyMHz - spanMHz / 2;
    double cfi;

    double xScale = spanMHz / waveformDBm.length;
    for (int i = 0; i < waveformDBm.length; i++) {
      cfi = i * xScale + startMHz;
      w.put(cfi, waveformDBm[i]);
    }
    return w;
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

  /**
   * Set the time required to create this datagram
   * <p>
   * @param elapsedTimeMS
   */
  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public String toStringBrief() {
    if (isValid) {
      return "WAV: CF [" + centerFrequencyMHz + "] Span [" + spanMHz + "] RL [" + referenceLevel + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  public String toStringData() {
    if (isValid) {
      String s = "";
      double[] wave = this.getWaveformDBm();
      for (double element : wave) {
        s += "[" + element + "]";
      }
      return s;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  @Override
  public String toString() {
    //    return ByteUtil.toString(datagramData, true);
    if (isValid) {
      //      double[] wave = this.getWaveformDBm();
      //      String s = "";
      //      for (int i = 0; i < wave.length; i++) {
      //        s += "[" + wave[i] + "]";
      //      }
      return "Waveform Response Datagram" + "\n index   name             value" + "\n --------------------------------" + "\n this.datagramType:           "
        + datagramType + "\n this.isValid:                " + isValid + "\n this.datagramData length:    " + datagramData.length
        + "\n 4-324   waveform length:     " + waveform.length + "\n 324     productId:           " + productId + "\n 325-328 centerFrequencyMHz:  "
        + centerFrequencyMHz + "\n 329-332 spanMHz:             " + spanMHz + "\n 333     referenceLevel:      " + referenceLevel
        + "\n 334     resolutionBandwidth: " + resolutionBandwidth + "\n 335     inputConnector:      " + inputConnector
        + "\n 336-337 internalExtender:    0x " + ByteUtil.toString(internalExtender) + "\n 338-339 externalExtender:    0x "
        + ByteUtil.toString(externalExtender) + "\n 340     lnbPower:            0x" + Integer.toHexString(lnbPower) + "\n 341     reserved01:          0x"
        + Integer.toHexString(reserved01) + "\n 342     reserved02:          0x" + Integer.toHexString(reserved02) //              + "\n waveform data in DBM:          " + s
        ;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }
}
