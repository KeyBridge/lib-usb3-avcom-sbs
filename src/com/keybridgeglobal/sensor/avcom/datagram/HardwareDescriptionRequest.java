package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;

/**
 * Avcom Hardware Description Request Datagram
 * <p>
 * @author jesse
 */
public class HardwareDescriptionRequest implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  public static final byte datagramType = IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID;
  private final byte[] datagramData = IDatagram.GET_HARDWARE_DESCRIPTION_REQUEST_MESSAGE;
  private final boolean isValid = true;
  private int elapsedTimeMS = 1;
  private String jobSerialNumber;
  private String sensorSerialNumber;

  public HardwareDescriptionRequest(String datagramSerialNumber) {
    this.jobSerialNumber = datagramSerialNumber;
  }

  /**
   * Returns a HardwareDescriptionRequest datagram with a (probably) unique
   * serial number set to current time in milliseconds
   */
  public HardwareDescriptionRequest() {
    new HardwareDescriptionRequest(String.valueOf(System.currentTimeMillis()));
  }

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

  public void setTransactionId(String serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public String getSensorSerialNumber() {
    return this.sensorSerialNumber;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  /**
   * Return a long description of the datagram including:
   * <ol>
   * <li>type
   * <li>serial number
   * <li>data payload
   * </ol>
   * <p>
   * @return
   */
  @Override
  public String toString() {
    return "HDRq: [" + datagramType + "] SN: [" + jobSerialNumber + "] Data: [" + ByteUtil.toString(datagramData) + "]";
  }

  /**
   * Return a short description of the datagram including:
   * <ol>
   * <li>type
   * <li>serial number
   * <li>data length
   * </ol>
   * <p>
   * @return
   */
  public String toStringBrief() {
    return "HDRq: [" + datagramType + "] SN: [" + jobSerialNumber + "]";
  }

  public static void main(String args[]) {
    HardwareDescriptionRequest hr = new HardwareDescriptionRequest();
    System.out.println("typeid: " + hr.getSensorTypeId());
  }
}
