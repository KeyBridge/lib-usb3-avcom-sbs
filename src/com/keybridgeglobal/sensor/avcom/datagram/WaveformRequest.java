package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;

/**
 * Avcom 8-Waveform Datagram (Firmware >= v1.9) May be sent to the device
 * Defined in Table 9
 * <p>
 * @author jesse
 */
public class WaveformRequest implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  //----------------------------------------------------------------------------
  // Datagram housekeeping
  public static final byte datagramType = IDatagram.WAVEFORM_REQUEST_ID;
  private final byte[] datagramData = IDatagram.GET_TRACE_REQUEST_MESSAGE;
  private final boolean isValid = true;
  private int elapsedTimeMS = 1;
  private String jobSerialNumber;
  private String sensorSerialNumber;

  @SuppressWarnings("static-access")
  public byte getSensorTypeId() {
    return this.datagramType;
  }

  public byte[] getData() {
    return this.datagramData;
  }

  public boolean parse(byte[] bytes) {
    return true;
  }

  public byte[] serialize() {
    return this.datagramData;
  }

  public Boolean isValid() {
    return this.isValid;
  }

  public int getElapsedTimeMS() {
    return this.elapsedTimeMS;
  }

  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public String getTransactionId() {
    return this.jobSerialNumber;
  }

  public void sesetTransactionIdtring serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public String getSensorSerialNumber() {
    return this.sensorSerialNumber;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  @Override
  public String toString() {
    return "WR: [" + datagramType + "] SN: [" + jobSerialNumber + "] Data: [" + ByteUtil.toString(datagramData) + "]";
  }

  public String toStringBrief() {
    return "WR: [" + datagramType + "] SN: [" + jobSerialNumber + "]";
  }
}
