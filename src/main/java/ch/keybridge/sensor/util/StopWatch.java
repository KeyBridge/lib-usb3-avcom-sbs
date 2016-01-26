/*
 * Copyright (c) 2014, Jesse Caulfield <jesse@caulfield.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.keybridge.sensor.util;

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
