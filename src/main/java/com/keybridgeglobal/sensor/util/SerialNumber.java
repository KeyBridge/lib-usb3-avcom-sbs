package com.keybridgeglobal.sensor.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 06/11/11 - created based on SerialNumber class in Sensor 0.4.1 package
 * Modified to return Long instead of String, Made this class static
 * <p>
 * Usage: serialnumber.getSerialNumber
 * <p>
 * @author jesse
 */
public class SerialNumber {

  private static final AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

  /**
   * @return An atomically unique serial number based on a time stamp.
   */
  public static long get() {
    return atomicLong.getAndIncrement();
  }

  private SerialNumber() {
  }
}
