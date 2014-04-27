package com.keybridgeglobal.sensor.interfaces;

import java.util.Collection;
import java.util.Map;

public interface ISensorEntity {

  public Object get(String key);

  public String getDeviceModel();

  public String getDevicePortName();

  public String getDeviceSerialNumber();

  public String getDeviceType();

  /**
   * MAP of all the contained values within this sensorEntity
   */
  public Map<String, Object> getEntityValues();

  public int getJobCount();

  public Collection<ISensorJob> getJobQueue();

  public void setDeviceModel(String deviceModel);

  public void setDevicePortName(String portName);

  public void setDeviceSerialNumber(String deviceSerialNumber);

  public void setDeviceType(String deviceType);

  public void setJobQueue(Collection<ISensorJob> jobQueue);

  public String toStringBrief();

  public void setEntityValues(Map<String, Object> entityValues);

  public void initialize(Map<String, Object> entityValues);
}
