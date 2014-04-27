/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.keybridgeglobal.sensor.interfaces;

/**
 * @author jesse
 */
public interface IGPSSatellite {

  public boolean isUsed();

  public int getElevation();

  public int getPrn();

  public int getSignalStrength();

  public int getAzimuth();
}
