/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.keybridgeglobal.sensor.interfaces;

/**
 * @author jesse
 */
public interface IGPSLocation {

  public Double getAltitude();

  public Double getClimb();

  public Double getCourse();

  public Double getErrorClimb();

  public Double getErrorCourse();

  public Double getErrorHorizontal();

  public Double getErrorSpeed();

  public Double getErrorTime();

  public Double getErrorVertical();

  public Double getLatitude();

  public Double getLongitude();

  public Double getSpeed();

  public Long getGpsTimestamp();

  public Short getGpsMode();

  public String toStringBrief();
}
