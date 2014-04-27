package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;

/**
 * Change Settings Request Datagram for Avcom devices Table 12
 * <p>
 * @author jesse
 */
public class SettingsRequest implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  // ----------------------------------------------------------------------------
  // Datagram housekeeping
  public static final byte datagramType = IDatagram.SETTINGS_REQUEST_ID;
  // private byte[] datagramData;
  private final Boolean isValid = true;
  private int elapsedTimeMS;
  private String jobSerialNumber = "0";
  private String sensorSerialNumber;
  // ----------------------------------------------------------------------------
  // Avcom Hardware Setting with default values
  private double centerFrequencyMHz;
  private double spanMHz;
  private EAvcomReferenceLevel referenceLevel;
  private EAvcomResolutionBandwidth resolutionBandwidth;
  private int inputConnector = 1;
  // default to usb port
  private int lnbPower = 0;

  // default to off
  /**
   * Creates a new Settings Request Datagram
   */
  public SettingsRequest() {
  }

  /**
   * Standard constructor
   * <p>
   * @param datagramSerialNumber
   */
  public SettingsRequest(String datagramSerialNumber) {
    this.jobSerialNumber = datagramSerialNumber;
  }

  /**
   * Full constructor (used in DatagramFactory)
   * <p>
   * @param jobSerialNumber
   * @param centerFrequencyMHz
   * @param spanMHz
   * @param referenceLevel
   * @param resolutionBandwidth
   */
  public SettingsRequest(String jobSerialNumber, double centerFrequencyMHz, double spanMHz, EAvcomReferenceLevel referenceLevel, EAvcomResolutionBandwidth resolutionBandwidth) {
    super();
    this.jobSerialNumber = jobSerialNumber;
    this.centerFrequencyMHz = centerFrequencyMHz;
    this.spanMHz = spanMHz;
    this.referenceLevel = referenceLevel;
    this.resolutionBandwidth = resolutionBandwidth;
  }

  public static SettingsRequest getDefault() {
    return new SettingsRequest("0", 1250, 12550, EAvcomReferenceLevel.MINUS_50, EAvcomResolutionBandwidth.ONE_HUNDRED_KHZ);
  }

  // ----------------------------------------------------------------------------
  // Datagram Interface methods
  @SuppressWarnings("static-access")
  public byte getSensorTypeId() {
    return this.datagramType;
  }

  /**
   * Not supported
   * <p>
   * @return
   */
  public byte[] getData() {
    return null;
  }

  /**
   * Not supported
   * <p>
   * @param bytes
   * @return
   */
  public boolean parse(byte[] bytes) {
    return false;
  }

  /**
   * Convert internal parameters into a valid byte stream to communicate with
   * the Avcom detector
   * <p>
   * @return
   */
  public byte[] serialize() {
    // Convert from MHz to Avcom values
    long centerFrequency = (long) (this.centerFrequencyMHz * 10000);
    long span = (long) (this.spanMHz * 10000);
    // Create a byte stream
    byte[] b = new byte[IDatagram.SETTINGS_REQUEST_LENGTH + IDatagram.HEADER_SIZE];
    int idx = 0;
    b[idx++] = IDatagram.FLAG_STX;
    b[idx++] = 0;
    b[idx++] = IDatagram.SETTINGS_REQUEST_LENGTH;
    b[idx++] = IDatagram.SETTINGS_REQUEST_ID;
    b[idx++] = (byte) (centerFrequency >>> 24);
    b[idx++] = (byte) (centerFrequency >>> 16);
    b[idx++] = (byte) (centerFrequency >>> 8);
    b[idx++] = (byte) centerFrequency;
    b[idx++] = (byte) (span >>> 24);
    b[idx++] = (byte) (span >>> 16);
    b[idx++] = (byte) (span >>> 8);
    b[idx++] = (byte) span;
    b[idx++] = (byte) this.referenceLevel.getByteCode();
    b[idx++] = (byte) this.resolutionBandwidth.getByteCode();
    b[idx++] = (byte) (this.inputConnector + 10);
    b[idx++] = (byte) this.lnbPower;
    b[b.length - 1] = IDatagram.FLAG_ETX;
    return b;
  }

  public boolean isValid() {
    if ((this.resolutionBandwidth == null) || (this.referenceLevel == null)) {
      return false;
    } else {
      return this.isValid;
    }
  }

  // ----------------------------------------------------------------------------
  // Get methods for internal parameters
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

  public int getLnbPower() {
    return this.lnbPower;
  }

  public String getTransactionId() {
    // if (this.datagramSerialNumber == 0) {
    // this.datagramSerialNumber = System.currentTimeMillis();
    // }
    return this.jobSerialNumber;
  }

  /**
   * Get the start frequency for this request
   * <p>
   * @return
   */
  public double getStartFrequencyMHz() {
    return centerFrequencyMHz - spanMHz / 2;
  }

  /**
   * Get the stop frequency for this request
   * <p>
   * @return
   */
  public double getStopFrequencyMHz() {
    return centerFrequencyMHz + spanMHz / 2;
  }

  // ----------------------------------------------------------------------------
  // Set methods for internal parameters
  public void setCenterFrequencyMHz(double centerFrequencyMHz) {
    this.centerFrequencyMHz = centerFrequencyMHz;
  }

  public void setSpanMHz(double spanMHz) {
    this.spanMHz = spanMHz;
  }

  public void setReferenceLevel(EAvcomReferenceLevel referenceLevel) {
    this.referenceLevel = referenceLevel;
  }

  public void setResolutionBandwidth(EAvcomResolutionBandwidth resolutionBandwidth) {
    this.resolutionBandwidth = resolutionBandwidth;
  }

  public void setInputConnector(int inputConnector) {
    this.inputConnector = inputConnector;
  }

  public void setLnbPower(int lnbPower) {
    this.lnbPower = lnbPower;
  }

  public int getElapsedTimeMS() {
    return this.elapsedTimeMS;
  }

  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public void setTransactionId(String serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public String getSensorSerialNumber() {
    return this.sensorSerialNumber;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  // Housekeeping --------------------------------------------------------------
  public String toStringBrief() {
    if (this.isValid()) {
      return "SET: [" + jobSerialNumber + "] CF [" + centerFrequencyMHz + "] span [" + spanMHz + "] RL [" + referenceLevel + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "SettingsRequest: Not initialized. Reference Level & Resolution Bandwidth must be set.";
    }
  }

  @Override
  public String toString() {
    if (this.isValid()) {
      return "Settings Request Datagram" + "\n name                 value" + "\n --------------------------------" + "\n centerFrequencyMHz:  " + centerFrequencyMHz + "\n spanMHz:             " + spanMHz + "\n referenceLevel:      " + referenceLevel + "\n resolutionBandwidth: " + resolutionBandwidth + "\n inputConnector:      " + inputConnector + "\n lnbPower:            " + lnbPower + "\n byte message:        " + ByteUtil.toString(this.serialize());
    } else {
      return "SettingsRequest: Not initialized. Reference Level & Resolution Bandwidth must be set.";
    }
  }

  /**
   * scratchbox
   */
  public static void main(String[] main) {
    SettingsRequest s = new SettingsRequest();
    System.out.println("Pre settings: \n" + s.toString());
    s.setCenterFrequencyMHz(1000);
    s.setSpanMHz(500);
    s.setReferenceLevel(EAvcomReferenceLevel.MINUS_40);
    s.setResolutionBandwidth(EAvcomResolutionBandwidth.THREE_HUNDRED_KHZ);
    System.out.println("Post settings: \n" + s.toString());
    System.out.println("Post settings brief: \n" + s.toStringBrief());
    // System.out.println("Bytes: " + ByteUtil.toString(s.serialize()));
    // System.out.println("0x0010 " + 0x0010);
  }
}
