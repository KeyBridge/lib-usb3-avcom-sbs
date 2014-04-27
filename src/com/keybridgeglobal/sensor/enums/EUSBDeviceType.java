package com.keybridgeglobal.sensor.enums;

public enum EUSBDeviceType {

  AVCOM("Avcom Spectrum Analyzer"),
  GPS("GPS Receiver");

  private final String description;

  private EUSBDeviceType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
