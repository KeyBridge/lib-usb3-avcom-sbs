package com.keybridgeglobal.sensor.interfaces;

/**
 * Interface describing the variables and methods that this instance must
 * support
 * <p>
 * @author jesse
 */
public interface ISpectrumAnalyzerListener {

  /**
   * Action to take when a datagram is received
   * <p>
   * @param datagram
   */
  public void onSADatagram(Object datagram);

  /**
   * Light weight change notification that some parameter has changed.
   * <p>
   * @param sensorEntity
   */
  public void onChange();
}
