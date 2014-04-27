package com.keybridgeglobal.sensor;

import com.keybridgeglobal.sensor.interfaces.ISensorEntity;
import com.keybridgeglobal.sensor.interfaces.ISensorJob;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Unimaginative Javabean holding device entity parameters
 * <p>
 * @author jesse
 */
public class SensorEntity implements ISensorEntity, Serializable {

  private static final long serialVersionUID = 1L;
  private String deviceModel;
  private String devicePortName;
  private String deviceSerialNumber;
  private String deviceType;
  private Map<String, Object> entityValues;
  private final Collection<ISensorJob> jobQueue = new ArrayList<>();

  public Object get(String key) {
    return key;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public String getDevicePortName() {
    return devicePortName;
  }

  public String getDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public Map<String, Object> getEntityValues() {
    return entityValues;
  }

  public void setEntityValues(Map<String, Object> entityValues) {
    initialize(entityValues);
  }

  public int getJobCount() {
    return jobQueue.size();
  }

  public Collection<ISensorJob> getJobQueue() {
    return jobQueue;
  }

  /**
   * Set the parameters of this Device Entity from the device.status message
   * <p>
   * @param entityValues
   */
  public void initialize(Map<String, Object> entityValues) {
    this.entityValues = entityValues;
    try {
      setDeviceModel((String) entityValues.get("device.model"));
      setDeviceSerialNumber((String) entityValues.get("device.serial.number"));
      setDevicePortName((String) entityValues.get("device.serial.port"));
    } catch (Exception e) {
    }
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  public void setDevicePortName(String portName) {
    this.devicePortName = portName;
  }

  public void setDeviceSerialNumber(String deviceSerialNumber) {
    this.deviceSerialNumber = deviceSerialNumber;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public void setJobQueue(Collection<ISensorJob> jobQueue) {
    this.jobQueue.clear();
    this.jobQueue.addAll(jobQueue);
  }

  // ----------------------------------------------------------------------------
  @Override
  public String toString() {
    String descriptionString = "Key Bridge Internet Enabled Spectrum Analyzer" + "\n\n";
    descriptionString += String.format(" %-30s   %s \n", "System Parameter", "Current Setting");
    descriptionString += String.format(" %-30s   %s \n", "-----------------------", "------------------------------");
    TreeMap<String, Object> paramsTreeMap = new TreeMap<>();
    paramsTreeMap.putAll(entityValues);
    for (Object key : paramsTreeMap.keySet()) {
      if (!((String) key).contains("job.")) {
        descriptionString += String.format(" %-30s : %s\n", key, paramsTreeMap.get(key));
      }
    }
    descriptionString += String.format("\n %-30s   %s \n", "Job Serial Number", "Job Description");
    descriptionString += String.format(" %-30s   %s \n", "-----------------------", "------------------------------");
    int j = 0;
    for (ISensorJob sr : jobQueue) {
      descriptionString += String.format(" %-30s : %s\n", j++, sr.toStringBrief());
    }
    return descriptionString;
  }

  /**
   * Returns a brief description of this Device Entity
   * <p>
   * @return
   */
  public String toStringBrief() {
    // return "Sensor Entity contains " + entityValues.size() + " entries.";
    return "Device: SN [" + deviceSerialNumber
      + "] Model [" + deviceModel
      + "] Port [" + devicePortName
      + "] Type [" + deviceType
      + "] ";
  }
}
