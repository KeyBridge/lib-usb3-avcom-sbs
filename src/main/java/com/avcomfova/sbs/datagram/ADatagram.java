/*
 * Copyright (c) 2014, Jesse Caulfield
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
package com.avcomfova.sbs.datagram;

import com.avcomofva.sbs.enumerated.DatagramType;
import com.avcomofva.utility.SerialNumber;

/**
 * An abstract Datagram implementation with basic methods and variables common
 * to all datagrams.
 *
 * @author Jesse Caulfield
 */
public abstract class ADatagram implements IDatagram {

  /**
   * The datagram type.
   */
  protected final DatagramType type;
  /**
   * The datagram contents. E.g. trace data read by the device (dB)
   */
  protected double[] data;
  /**
   * Indicator that the data in this datagram was completely and correctly
   * parsed.
   */
  protected boolean valid;
  /**
   * The time (i.e. duration) it took for the hardware to create this Datagram
   * (in milliseconds).
   */
  protected long elapsedTimeMillis;
  /**
   * A (optional) transaction identifier. This is used by a device controller to
   * correlate {@code REQUEST} and {@code RESPONSE} datagrams.
   */
  protected Long transactionId;

  /**
   * Construct a new ADatagram instance, setting the datagram type.
   *
   * @param datagramType the datagram type
   */
  public ADatagram(DatagramType datagramType) {
    this.type = datagramType;
    this.transactionId = SerialNumber.get();
  }

  /**
   * Get the datagram raw data contents. The byte array may be raw or cooked
   * depending upon the implementation.
   *
   * @return the raw data array returned from the sensor
   */
  @Override
  public double[] getData() {
    return data;
  }

  /**
   * Get the datagram type identifier
   *
   * @return the datagram type identifier
   */
  @Override
  public DatagramType getType() {
    return type;
  }

  /**
   * Get the time required for the hardware to collect and create this Datagram
   *
   * @return Elapsed time in milliseconds
   */
  @Override
  public long getElapsedTime() {
    return elapsedTimeMillis;
  }

  /**
   * Set the time required for the hardware to collect and create this Datagram
   *
   * @param elapsedTimeMS Elapsed time in milliseconds
   */
  public void setElapsedTime(long elapsedTimeMS) {
    this.elapsedTimeMillis = elapsedTimeMS;
  }

  /**
   * Set the serial number
   *
   * @return the job serial number
   */
  @Override
  public Long getTransactionId() {
    return transactionId;
  }

  /**
   * Set the sensor job serial number.
   *
   * @param transactionId the sensor job serial number.
   */
  @Override
  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Is this a properly structured and complete datagram - are all the bits in
   * place
   *
   * @return true if the datagram is valid
   */
  @Override
  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    return type + " ID: [" + transactionId + "]";
  }
}
