package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;
import java.util.Random;

/**
 * @author jesse
 */
public class DummyDatagram implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  private final byte datagramType = 0x07;
  //  private byte[] data = new byte[]{(byte) 0x07, (byte) 0x5a, (byte) 0x02, (byte) 0x08, 0, 0, (byte) 0x98, (byte) 0x96, (byte) 0x80, 0, (byte) 0x0f, (byte) 0x42, (byte) 0x40, (byte) 0x0a, (byte) 0x08, (byte) 0x78, (byte) 0x0a, (byte) 0x0b, 0, 0, (byte) 0x08, (byte) 0x0d, 0, 0, 0, 0, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x33, (byte) 0x30, (byte) 0x39, (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x34, (byte) 0x1b, (byte) 0x1a, (byte) 0x0e, (byte) 0x14, (byte) 0x09, (byte) 0xaa, (byte) 0x92, (byte) 0xaf, 0, 0, (byte) 0x40, (byte) 0x3f, (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0xe8, (byte) 0xb1, (byte) 0x82, (byte) 0x67, (byte) 0x80, (byte) 0x5b, (byte) 0x49, (byte) 0x33, (byte) 0x96, (byte) 0x80, (byte) 0x9c, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x28, (byte) 0x1b, (byte) 0x1b, (byte) 0x23, (byte) 0x38, (byte) 0x28, (byte) 0xb4, (byte) 0x9c, 0, (byte) 0x04, (byte) 0xff, (byte) 0x03};
  private static int DATA_LENGTH = 80;
  private final byte[] datagramData = new byte[DATA_LENGTH];
  private final boolean isValid = true;
  private int elapsedTimeMS = 1;
  private String jobSerialNumber;
  private String sensorSerialNumber;
  private int iter;

  public byte[] getData() {
    generateSinSignalData();
    return this.datagramData;
  }

  public boolean parse(byte[] bytes) {
    return true;
  }

  public byte[] serialize() {
    int s = datagramData.length + 2;
    int i;
    byte[] bytes = new byte[s];
    bytes[0] = datagramType;
    for (i = 1; i < datagramData.length; i++) {
      bytes[i] = datagramData[i];
    }
    return bytes;
  }

  public Boolean isValid() {
    return isValid;
  }

  /**
   * Generate random noise
   */
  @SuppressWarnings("unused")
  private void generateRandomNoiseData() {
    Random r = new Random();
    for (int i = 0; i < DATA_LENGTH; i++) {
      datagramData[i] = (byte) (16 + (r.nextInt() % 20));
    }
    int len = datagramData.length + 1;
    int idx = 0;
    byte[] bytes = new byte[len + 3];
    bytes[idx++] = 2;
    bytes[idx++] = (byte) ((datagramData.length >> 8) & 0x000000ff);
    bytes[idx++] = (byte) ((datagramData.length) & 0x000000ff);
    //    bytes[idx++] = WaveformRequest.TYPE_ID;
  }

  /**
   * Generate fake signal data
   */
  private void generateSinSignalData() {
    for (int i = 0; i < DATA_LENGTH; i++) {
      datagramData[i] = (byte) (Math.sin(2 * Math.PI * i / 320 + iter * Math.PI * 2 / 320) * 128 + 128);
    }
    //    data[0] = (byte) WaveformRequest.TYPE_ID;
  }

  /**
   * Used to step the signal data
   * <p>
   * @param i
   */
  private void setIter(int i) {
    this.iter = i % DATA_LENGTH;
  }

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

  public byte getSensorTypeId() {
    return this.datagramType;
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
   * scratchbox
   * <p>
   * @param args
   */
  public static void main(String args[]) {
    DummyDatagram dd = new DummyDatagram();
    System.out.println("dd: " + dd.toString());
    dd.setIter(1);

    for (int i = 0; i < 100; i++) {
      dd.setIter(i);
      System.out.println(dd.toString());
    }

  }
}
