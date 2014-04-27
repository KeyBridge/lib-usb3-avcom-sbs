package com.keybridgeglobal.sensor.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Usage: serialnumber.getSerialNumber
 * <p>
 * @author jesse
 */
public class SerialNumber {

  private long time;
  private final AtomicInteger current;

  public SerialNumber() {
    this.time = System.currentTimeMillis();
    this.current = new AtomicInteger();
  }

  /**
   * Returns an atomically unique serial number based on a time stamp
   * <p>
   * @return String
   */
  public String getSerialNumber() {
    String s;
    if (time != System.currentTimeMillis()) {
      time = System.currentTimeMillis();
      current.set(0);
    }
    s = time + "" + current.getAndIncrement();
    return s;
  }

  /**
   * scratch box to make sure it works ok
   * <p>
   * @param args
   */
  public static void main(String args[]) {
    SerialNumber t = new SerialNumber();
    for (int i = 0; i < 1000; i++) {
      System.out.println(t.getSerialNumber());
    }

    System.out.println("serial: " + t.getSerialNumber());
    System.out.println("serial: " + t.getSerialNumber());
    System.out.println("serial: " + t.getSerialNumber());
    System.out.println("serial: " + t.getSerialNumber());
  }
}
