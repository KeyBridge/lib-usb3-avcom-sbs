package com.keybridgeglobal.sensor.interfaces;

/**
 * Standard interface to exchange Jobs between spectrum analyzer implementations
 * and job control, persistence, and other modules
 * <p>
 * Note: implementations should also instantiate <br>
 * public static SensorJob_Interface getDefault();
 * <p>
 * The minimum required parameters are:
 * <p>
 * centerFrequency <br>
 * span<br>
 * referenceLevel<br>
 * resolutionBandwidth<br>
 * period (time in seconds between job runs)<br>
 * snJob (job serial number)<br>
 * snSensor (optional sensor serial number)<br>
 */
public interface ISensorJob {

  public double getCenterFrequency();

  public int getIsActive();

  public int getPeriod();

  public double getReferenceLevel();

  public double getResolutionBandwidth();

  public String getSnJob();

  public String getSnSensor();

  public double getSpan();

  public void setCenterFrequency(double centerFrequency);

  public void setIsActive(int isActive);

  public void setPeriod(int period);

  public void setReferenceLevel(double referenceLevel);

  public void setResolutionBandwidth(double resolutionBandwidth);

  public void setSnJob(String snJob);

  public void setSnSensor(String snSensor);

  public void setSpan(double span);

  public String toStringBrief();
}
