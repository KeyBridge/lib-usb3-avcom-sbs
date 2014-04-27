package com.keybridgeglobal.sensor.util;

import java.io.Serializable;

/**
 * Routine to print messages to the console if the user desires
 * <p>
 * @author jesse
 */
public class Debug implements Serializable {

  private static final long serialVersionUID = 1L;
  private final Boolean write;

  public Debug(Boolean properties) {
    this.write = properties;
  }

  public void out(Object o, String message) {
    if (write) {
      System.out.println(String.format("%-25s : %-40s", o.getClass().getSimpleName(), message));
    }
  }

  public void out(String message) {
    if (write) {
      System.out.println("Debug: " + message);
    }
  }
}
