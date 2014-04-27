package com.keybridgeglobal.sensor;

import com.keybridgeglobal.sensor.interfaces.ISensorJob;

/**
 * Basic Job object for the spectrum analyzer to pass jobs back and forth
 * <p>
 * @author jesse
 */
public class SimpleJob implements ISensorJob {

  private double centerFrequency;    // MHz
  private int isActive;           // 0 = no, 1 = yes
  private int period;             // duration between samples (s)
  private double referenceLevel;     // dBm
  private double resolutionBandwidth; // MHz
  private String snJob;              // job serial number
  private String snSensor;           // sensor serial number
  private double span;               // MHz

  /**
   * Get the default Job for a full-spectrum sweep. This has the values:<br>
   * job serial number = 0 (zero) <br>
   * center frequency = 1250 MHz<br>
   * span = 1250 MHz<br>
   * resolution bandwidth = 100 kHz<br>
   * reference level = -50 dBm
   * <p>
   * @return
   */
  public static SimpleJob getDefault() {
    SimpleJob s = new SimpleJob("0");
    s.setCenterFrequency(1250);
    s.setSpan(2500);
    s.setResolutionBandwidth(0.10);
    s.setReferenceLevel(-50);
    s.setPeriod(60);
    s.setIsActive(1);
    return s;
  }

  /**
   * SimpleJob minimal constructor.
   * <p>
   * @param snJob A job serial number is always required.
   */
  public SimpleJob(String snJob) {
    this.snJob = snJob;
  }

  /**
   * SimpleJob complete constructor
   * <p>
   * @param snJob               sensor job serial number
   * @param snSensor            sensor device serial number
   * @param isActive            is this job active
   * @param centerFrequency     cf in MHz
   * @param span                span in MHz
   * @param referenceLevel      rl in dBm
   * @param resolutionBandwidth rbw in MHz
   * @param period              period in MS
   */
  public SimpleJob(String snJob, String snSensor, int isActive, double centerFrequency, double span, double referenceLevel, double resolutionBandwidth, int period) {
    this.snJob = snJob;
    this.snSensor = snSensor;
    this.isActive = isActive;
    this.centerFrequency = centerFrequency;
    this.span = span;
    this.referenceLevel = referenceLevel;
    this.resolutionBandwidth = resolutionBandwidth;
    this.period = period;
  }

  public double getCenterFrequency() {
    return centerFrequency;
  }

  public int getIsActive() {
    return isActive;
  }

  public int getPeriod() {
    return period;
  }

  public double getReferenceLevel() {
    return referenceLevel;
  }

  public double getResolutionBandwidth() {
    return resolutionBandwidth;
  }

  public String getSnJob() {
    return snJob;
  }

  public String getSnSensor() {
    return snSensor;
  }

  public double getSpan() {
    return span;
  }

  public void setCenterFrequency(double centerFrequency) {
    this.centerFrequency = centerFrequency;
  }

  public void setIsActive(int isActive) {
    this.isActive = isActive;
  }

  public void setPeriod(int period) {
    this.period = period;
  }

  public void setReferenceLevel(double referenceLevel) {
    this.referenceLevel = referenceLevel;
  }

  public void setResolutionBandwidth(double resolutionBandwidth) {
    this.resolutionBandwidth = resolutionBandwidth;
  }

  public void setSnJob(String snJob) {
    this.snJob = snJob;
  }

  public void setSnSensor(String snSensor) {
    this.snSensor = snSensor;
  }

  public void setSpan(double span) {
    this.span = span;
  }

  @Override
  public String toString() {
    return " JobId [" + snJob + "] CF [" + centerFrequency + "] SP [" + span + "] RBW [" + resolutionBandwidth + "] RL [" + referenceLevel + "]";
  }

  @Override
  public String toStringBrief() {
    return " JobId [" + snJob + "] CF [" + centerFrequency + "] SP [" + span + "] RBW [" + resolutionBandwidth + "] RL [" + referenceLevel + "]";
  }
}
