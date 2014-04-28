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
package com.avcomfova.sbs.datagram;

import com.avcomofva.sbs.enumerated.EAvcomDatagram;

/**
 * An abstract Datagram implementation with all basic methods and variables
 * common to all datagrams.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
@SuppressWarnings("ProtectedField")
public abstract class ADatagram implements IDatagram {

  // Datagram housekeeping -----------------------------------------------------
  protected final EAvcomDatagram datagramType;
  /**
   * The trace data read by the device (dB)
   */
  protected double[] data;
  /**
   * Indicator that the data produced by the device gram was correctly and
   * completely parsed.
   */
  protected boolean valid;
  /**
   * how long it took for the hardware to create this Datagram in milliseconds
   */
  protected int elapsedTimeMS;
  /**
   * A (optional) transaction identifier. This is used by a device controller to
   * correlate TRACE_REQUESTS with TRACE_RESPONSES.
   */
  protected Long transactionId;

  /**
   * Construct a new ADatagram instance, setting the datagram type.
   * <p>
   * @param datagramType the datagram type
   */
  public ADatagram(EAvcomDatagram datagramType) {
    this.datagramType = datagramType;
  }

  /**
   * Get the datagram raw data contents. The byte array may be raw or cooked
   * depending upon the implementation.
   * <p>
   * @return the raw data array returned from the sensor
   */
  @Override
  public double[] getData() {
    return data;
  }

  /**
   * Get the datagram type identifier
   * <p>
   * @return the datagram type identifier
   */
  @Override
  public EAvcomDatagram getDatagramType() {
    return datagramType;
  }

  /**
   * Get the time required for the hardware to collect and create this Datagram
   * <p>
   * @return Elapsed time in milliseconds
   */
  @Override
  public int getElapsedTimeMS() {
    return elapsedTimeMS;
  }

  /**
   * Set the time required for the hardware to collect and create this Datagram
   * <p>
   * @param elapsedTimeMS Elapsed time in milliseconds
   */
  @Override
  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  /**
   * Set the serial number
   * <p>
   * @return the job serial number
   */
  @Override
  public Long getTransactionId() {
    return transactionId;
  }

  /**
   * Set the sensor job serial number.
   * <p>
   * @param transactionId the sensor job serial number.
   */
  @Override
  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Is this a properly structured and complete datagram - are all the bits in
   * place
   * <p>
   * @return true if the datagram is valid
   */
  @Override
  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    return datagramType + " ID: [" + transactionId + "]";
  }
}
