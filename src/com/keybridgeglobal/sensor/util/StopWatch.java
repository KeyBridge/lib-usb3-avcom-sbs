package com.keybridgeglobal.sensor.util;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jesse
 */
public class StopWatch {

  private long startTime = 0;
  private long stopTime = 0;
  private boolean running = false;

  public StopWatch() {
    this.start();
  }

  public void start() {
    this.startTime = Calendar.getInstance().getTimeInMillis();
    this.running = true;
  }

  public void stop() {
    this.stopTime = Calendar.getInstance().getTimeInMillis();
    this.running = false;

    //    return Calendar.getInstance().getTimeInMillis() - startTime;
  }

  /**
   * The elapsed time in milliseconds
   * <p>
   * @return
   */
  public int getElapsedTimeMillis() {
    int elapsedTime = (int) (Calendar.getInstance().getTimeInMillis() - startTime);
    //    if (this.running) {
    //      this.stop();
    //    }
    //    return (int) (stopTime - startTime);
    return elapsedTime;
  }

  /**
   * The elapsed time in seconds
   * <p>
   * @return
   */
  public float getElapsetTimeSeconds() {
    if (this.running) {
      this.stop();
    }
    float millis = stopTime - startTime;
    float seconds = millis / 1000;
    return seconds;
  }

  /**
   * Write this to database
   * <p>
   * @param className
   * @param methodName
   */
  //  public void persist(String className, String methodName) {
  //    MethodTimer.persist(null, className, methodName, this.getElapsedTimeMillis());
  //  }
  /**
   * stratchpad
   * <p>
   * @param args
   */
  public static void main(String[] args) {
    try {
      StopWatch s = new StopWatch();
      s.start();
      Thread.sleep(100);
      System.out.println("Elapsed: " + s.getElapsedTimeMillis() + " ");
      Thread.sleep(100);
      System.out.println("Elapsed: " + s.getElapsedTimeMillis() + " ");
    } catch (InterruptedException ex) {
      Logger.getLogger(StopWatch.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
