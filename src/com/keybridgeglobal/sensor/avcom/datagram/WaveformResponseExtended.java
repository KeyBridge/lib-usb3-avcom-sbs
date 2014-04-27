package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.IWaveformResponseExtended;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * EXTENDED Waveform Response Datagram from Avcom devices 8-bit waveform packet
 * Table 9
 * <p>
 * @author jesse
 */
public class WaveformResponseExtended implements IWaveformResponseExtended, Serializable {

  // Datagram Housekeeping -----------------------------------------------------
  public static final byte datagramType = IDatagram.WAVEFORM_RESPONSE_EXTENDED_ID;
  private static final long serialVersionUID = 1L;
  // Configuration Meta-Data ---------------------------------------------------
  private double centerFrequencyMHz;
  private double referenceLevel;
  private double resolutionBandwidth;
  private double spanMHz;
  // GPS Impl -----------------------------------------------------------------
  private double latitude;
  private double longitude;
  // Operations ---------------------------------------------------------------
  private String jobSerialNumber;
  private String sensorSerialNumber;
  private int elapsedTimeMS;
  private Boolean isValid = false;
  private int sampleSize;
  private boolean saturated = false;
  private long timeStamp;
  private String sensorPortName;
  // Data ----------------------------------------------------------------------
  private final TreeMap<Double, Double> waveformDBm = new TreeMap<>();

  /**
   * Empty contructor for testing
   */
  public WaveformResponseExtended() {
  }

  /**
   * Full contructor with bytes
   * <p>
   * @param bytes
   */
  public WaveformResponseExtended(byte[] bytes) {
    this.isValid = this.parse(bytes);
  }

  public double getCenterFrequencyMHz() {
    return centerFrequencyMHz;
  }

  /**
   * Get the raw data Not implemented
   * <p>
   * @return
   */
  public byte[] getData() {
    byte[] data = new byte[waveformDBm.values().size()];
    for (int i = 0; i < data.length; i++) {
      data[i] = ((Double) waveformDBm.values().toArray()[i]).byteValue();
    }
    return data;
  }

  public int getElapsedTimeMS() {
    return this.elapsedTimeMS;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  /**
   * Get the serial number.
   * <p>
   * @return
   */
  public String getTransactionId() {
    return this.jobSerialNumber;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  @Override
  public double getReferenceLevel() {
    return this.referenceLevel;
  }

  @Override
  public double getResolutionBandwidth() {
    return this.resolutionBandwidth;
  }

  public int getSampleSize() {
    return sampleSize;
  }

  public String getSensorSerialNumber() {
    return sensorSerialNumber;
  }

  public double getSpanMHz() {
    return spanMHz;
  }

  public double getStartFrequencyMHz() {
    return waveformDBm.firstKey();
  }

  public double getStopFrequencyMHz() {
    return waveformDBm.lastKey();
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  @SuppressWarnings("static-access")
  public byte getSensorTypeId() {
    return this.datagramType;
  }

  public TreeMap<Double, Double> getWaveformDBm() {
    return waveformDBm;
  }

  // ----------------------------------------------------------------------------
  // GET/SET methods for internal parameters
  /**
   * Indicates whether the sensor has detected a strong signal that is
   * saturating the input
   * <p>
   * @return
   */
  public boolean isSaturated() {
    return saturated;
  }

  public Boolean isValid() {
    return this.isValid;
  }

  /**
   * Not implemented
   * <p>
   * @param bytes
   * @return
   */
  public boolean parse(byte[] bytes) {
    return false;
  }

  /**
   * Add a data value to the waveform. Used when constructing the waveformDBm.
   * <p>
   * @param centerFrequency
   * @param powerDBm
   */
  public void putData(double centerFrequency, double powerDBm) {
    this.waveformDBm.put(centerFrequency, powerDBm);
  }

  /**
   * Convenience method to bulk-add a waveform response to this waveform
   * response extended
   * <p>
   * @param waveform
   */
  public void putData(Map<Double, Double> waveform) {
    this.waveformDBm.putAll(waveform);
  }

  /**
   * Not implemented
   * <p>
   * @return
   */
  public byte[] serialize() {
    return null;
  }

  public void setCenterFrequencyMHz(double centerFrequencyMHz) {
    this.centerFrequencyMHz = centerFrequencyMHz;
  }

  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  /**
   * Set the serial number. <br>
   * Note this is typically done using the validate() method
   * <p>
   * @param serialNumber
   */
  public void setTransactionId(String serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setReferenceLevel(double referenceLevel) {
    this.referenceLevel = referenceLevel;
  }

  public void setReferenceLevel(EAvcomReferenceLevel referenceLevel) {
    this.referenceLevel = referenceLevel.getValueDB();
  }

  public void setResolutionBandwidth(double resolutionBandwidth) {
    this.resolutionBandwidth = resolutionBandwidth;
  }

  public void setResolutionBandwidth(EAvcomResolutionBandwidth resolutionBandwidth) {
    this.resolutionBandwidth = resolutionBandwidth.getMHz();
  }

  public void setSampleSize(int sampleSize) {
    this.sampleSize = sampleSize;
  }

  public void setSaturated(boolean saturated) {
    this.saturated = saturated;
  }

  public String getSensorPortName() {
    return sensorPortName;
  }

  public void setSensorPortName(String sensorPortName) {
    this.sensorPortName = sensorPortName;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  public void setSpanMHz(double spanMHz) {
    this.spanMHz = spanMHz;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public void setWaveformDBm(TreeMap<Double, Double> waveformDBm) {
    this.waveformDBm.clear();
    this.waveformDBm.putAll(waveformDBm);
  }

  @Override
  public String toString() {
    if (isValid) {
      return "\nWaveform Response Extended"
        + "\n index   name             value"
        + "\n --------------------------------"
        + "\n this.datagramType:           " + datagramType
        + "\n this.isValid:                " + isValid
        + "\n this.datagramSerialNumber:   " + jobSerialNumber
        + "\n this.elapsedTimeMS:          " + elapsedTimeMS
        + "\n this.sampleSize:             " + sampleSize
        + "\n waveform length:             " + waveformDBm.size()
        + "\n centerFrequencyMHz:          " + centerFrequencyMHz
        + "\n spanMHz:                     " + spanMHz
        + "\n referenceLevel:              " + referenceLevel
        + "\n resolutionBandwidth:         " + resolutionBandwidth;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  public String toStringBrief() {
    if (isValid) {
      return "WRE: SN [" + jobSerialNumber
        + "] CF [" + centerFrequencyMHz
        + "] Span [" + spanMHz
        + "] RL [" + referenceLevel
        + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  /**
   * Validates the waveform request. Trims the waveform of unwanted data and
   * sets internal parameters. This is the final step to completing WRE
   * creation.
   * <p>
   * @param settingsRequest
   * @return
   */
  public boolean validate(IDatagram settingsRequest) {
    if (waveformDBm.isEmpty()) {
      // ERROR. No data in waveformDBm
      this.isValid = false;
      return false;
    } else {
      SettingsRequest sr = (SettingsRequest) settingsRequest;
      // Set the timestamp & serialNumber
      this.timeStamp = System.currentTimeMillis();
      // Find and Trim values outside the requested range
      List<Double> removeKeys = new ArrayList<>();
      for (Double key : waveformDBm.keySet()) {
        if ((key < sr.getStartFrequencyMHz()) || (key > sr.getStopFrequencyMHz())) {
          removeKeys.add(key);
        }
      }
      for (Double key : removeKeys) {
        waveformDBm.remove(key);
      }
      // Set the span & CF based on actual values
      this.spanMHz = waveformDBm.lastKey() - waveformDBm.firstKey();
      this.centerFrequencyMHz = waveformDBm.firstKey() + spanMHz / 2d;
      this.isValid = true;
      return true;
    }
  }
}
