package com.keybridgeglobal.sensor.util;

/**
 * LockObject object that allows the management of a serial number
 */
public class LockObject {

  private String serialNumber = "0";

  public void setSerialNumber(String sn) {
    this.serialNumber = sn;
  }

  public String getSerialNumber() {
    return this.serialNumber;
  }
}
