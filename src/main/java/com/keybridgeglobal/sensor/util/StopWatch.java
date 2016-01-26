package com.keybridgeglobal.sensor.util;

import java.util.Calendar;

/**
 * A simple stopwatch timer utility that may be used to measure lap and elapsed
 * times in milliseconds or seconds.
 * <p/>
 * This class makes use of the internal system clock. If the system clock is
 * updated while a stopwatch measurement is running then (perhaps obviously) the
 * reported elapsed times will be invalid.
 * <p/>
 * @author jesse
 */
public class StopWatch {

  /**
   * The stopwatch start time, recorded in milliseconds on the current system
   * clock.
   */
  private long startTime = 0;
  /**
   * The stopwatch stop time, recorded in milliseconds on the current system
   * clock.
   */
  private long stopTime = 0;
  /**
   * Internal boolean indicator that the stopwatch is running (true) or stopped
   * (false).
   */
  private boolean running = false;

  public StopWatch() {
    this.startTime = Calendar.getInstance().getTimeInMillis();
    this.running = true;
  }

  /**
   * Start the StopWatch timer.
   */
  public void startTimer() {
    this.startTime = Calendar.getInstance().getTimeInMillis();
    this.running = true;
  }

  /**
   * Stop the StopWatch timer.
   * <p/>
   * @return The (total) elapsed time in milliseconds.
   */
  public long stopTimer() {
    this.stopTime = Calendar.getInstance().getTimeInMillis();
    this.running = false;
    return stopTime - startTime;
  }

  /**
   * The lap time in milliseconds. This method does NOT stop the clock.
   * <p/>
   * @return The lap time in milliseconds.
   */
  public int getLapTimeMillis() {
    return (int) (Calendar.getInstance().getTimeInMillis() - startTime);
  }

  /**
   * The lap time in seconds. This method does NOT stop the clock.
   * <p/>
   * @return The lap time in seconds.
   */
  public double getLapTimeSeconds() {
    return (Calendar.getInstance().getTimeInMillis() - startTime) / 1000d;
  }

  /**
   * The elapsed time in milliseconds. This method STOPS the clock.
   * <p/>
   * @return The (total) elapsed time in milliseconds.
   */
  public long getElapsedTimeMillis() {
    if (running) {
      stopTimer();
    }
    return stopTime - startTime;
  }

  /**
   * The elapsed time in seconds. This method STOPS the clock.
   * <p/>
   * @return The elapsed time in seconds.
   */
  public double getElapsedTimeSeconds() {
    if (running) {
      stopTimer();
    }
    return (stopTime - startTime) / 1000d;
  }
}
