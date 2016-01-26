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
package com.avcomofva.sbs.datagram.write;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import ch.keybridge.sensor.util.ByteUtil;

/**
 * Change Settings Request Datagram for Avcom devices. From Table 12: Change
 * Settings firmware &ge; v1.9.
 * <p>
 * Developer note: Devices do not reply to a change settings command.
 * <p>
 * @author Jesse Caulfield
 */
public class SettingsRequest extends ADatagram {

  /**
   * The length of a change settings message (10 bytes)
   */
  private static final int SETTINGS_REQUEST_LENGTH = 0x0010; // table 12 Firmware >= 1.9

  // Avcom Hardware Setting with default values
  /**
   * The new center frequency (MHz)
   */
  private double centerFrequencyMHz;
  /**
   * The new span (MHz)
   */
  private double spanMHz;
  /**
   * The new reference level (dB)
   */
  private EAvcomReferenceLevel referenceLevel;
  /**
   * The new resolution bandwidth (MHz)
   */
  private EAvcomResolutionBandwidth resolutionBandwidth;
  /**
   * The sensor input connector. Default is 1 (one). Change this value ONLY when
   * operating with a multi-input RF front end, such as the RSA
   * configuration,which has up to 6 RF inputs.
   */
  private int inputConnector = 1;
  // default to usb port
  /**
   * The LNB power configuration. Default is to turn LNB power OFF. Change this
   * value ONLY if the device supports LNB power.
   */
  private int lnbPower = 0;

  /**
   * Construct a new (empty) settings request. The center frequency, span,
   * reference level and RBW must be configured prior to use.
   */
  public SettingsRequest() {
    super(EAvcomDatagram.SETTINGS_REQUEST);
    this.valid = true;
    this.elapsedTimeMillis = 1;
    this.transactionId = System.currentTimeMillis();
  }

  /**
   * Construct a new, fully qualified settings request.
   * <p>
   * @param centerFrequencyMHz  The new center frequency (MHz)
   * @param spanMHz             The new span (MHz)
   * @param referenceLevel      The new reference level (dB)
   * @param resolutionBandwidth The new resolution bandwidth (MHz)
   */
  public SettingsRequest(double centerFrequencyMHz, double spanMHz, EAvcomReferenceLevel referenceLevel, EAvcomResolutionBandwidth resolutionBandwidth) {
    this();
    this.centerFrequencyMHz = centerFrequencyMHz;
    this.spanMHz = spanMHz;
    this.referenceLevel = referenceLevel;
    this.resolutionBandwidth = resolutionBandwidth;
  }

  /**
   * Get a default SettingsRequest instance. This configures the AVCOM sensor to
   * run full span with -50 dBm reference level and 1 MHz RBW.
   * <p>
   * This is useful for an out-of-the box start up configuration to view all
   * available frequencies.
   * <p>
   * @return a default SettingsRequest instance
   */
  public static SettingsRequest getInstance() {
    return new SettingsRequest(1250, 1250, EAvcomReferenceLevel.MINUS_50, EAvcomResolutionBandwidth.ONE_MHZ);
  }

  //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
  public double getCenterFrequencyMHz() {
    return centerFrequencyMHz;
  }

  public void setCenterFrequencyMHz(double centerFrequencyMHz) {
    this.centerFrequencyMHz = centerFrequencyMHz;
  }

  public int getInputConnector() {
    return inputConnector;
  }

  public void setInputConnector(int inputConnector) {
    this.inputConnector = inputConnector;
  }

  public int getLnbPower() {
    return lnbPower;
  }

  public void setLnbPower(int lnbPower) {
    this.lnbPower = lnbPower;
  }

  public EAvcomReferenceLevel getReferenceLevel() {
    return referenceLevel;
  }

  public void setReferenceLevel(EAvcomReferenceLevel referenceLevel) {
    this.referenceLevel = referenceLevel;
  }

  public EAvcomResolutionBandwidth getResolutionBandwidth() {
    return resolutionBandwidth;
  }

  public void setResolutionBandwidth(EAvcomResolutionBandwidth resolutionBandwidth) {
    this.resolutionBandwidth = resolutionBandwidth;
  }

  public double getSpanMHz() {
    return spanMHz;
  }

  public void setSpanMHz(double spanMHz) {
    this.spanMHz = spanMHz;
  }

  /**
   * Get the (calculated) start frequency for this request
   * <p>
   * @return the start frequency for this request
   */
  public double getStartFrequencyMHz() {
    return centerFrequencyMHz - spanMHz / 2;
  }

  /**
   * Get the (calculated) stop frequency for this request
   * <p>
   * @return the start frequency for this request
   */
  public double getStopFrequencyMHz() {
    return centerFrequencyMHz + spanMHz / 2;
  }//</editor-fold>

  /**
   * Get an exact copy of this SettingsRequest instance.
   * <p>
   * @return a copy of the selected SettingsRequest instance.
   */
  public SettingsRequest copy() {
    return new SettingsRequest(centerFrequencyMHz, spanMHz, referenceLevel, resolutionBandwidth);
  }

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  @Override
  public void parse(byte[] bytes) throws Exception {
    /**
     * NO OP.
     */
  }

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   * <p>
   * @return a byte array
   */
  @Override
  @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
  public byte[] serialize() {
// Convert from MHz to Avcom values
    long centerFrequency = (long) (this.centerFrequencyMHz * 10000);
    long span = (long) (this.spanMHz * 10000);
    // Create a byte stream
    byte[] b = new byte[SETTINGS_REQUEST_LENGTH + IDatagram.HEADER_SIZE];
    int idx = 0;
    b[idx++] = IDatagram.FLAG_STX;
    b[idx++] = 0;
    b[idx++] = SETTINGS_REQUEST_LENGTH;
    b[idx++] = EAvcomDatagram.SETTINGS_REQUEST.getByteCode();
    b[idx++] = (byte) (centerFrequency >>> 24);
    b[idx++] = (byte) (centerFrequency >>> 16);
    b[idx++] = (byte) (centerFrequency >>> 8);
    b[idx++] = (byte) centerFrequency;
    b[idx++] = (byte) (span >>> 24);
    b[idx++] = (byte) (span >>> 16);
    b[idx++] = (byte) (span >>> 8);
    b[idx++] = (byte) span;
    b[idx++] = (byte) this.referenceLevel.getByteCode();
    b[idx++] = (byte) this.resolutionBandwidth.getByteCode();
    b[idx++] = (byte) (this.inputConnector + 10);
    b[idx++] = (byte) this.lnbPower;
    b[b.length - 1] = IDatagram.FLAG_ETX;
    return b;
  }

  /**
   * Return a complete output of this Setting configuration.
   * <p>
   * @return a multi-line string.
   */
  public String toStringFull() {
    if (this.isValid()) {
      return "Settings Request Datagram"
        + "\n name                 value"
        + "\n --------------------------------"
        + "\n centerFrequencyMHz:  " + centerFrequencyMHz
        + "\n spanMHz:             " + spanMHz
        + "\n referenceLevel:      " + referenceLevel
        + "\n resolutionBandwidth: " + resolutionBandwidth
        + "\n inputConnector:      " + inputConnector
        + "\n lnbPower:            " + lnbPower
        + "\n byte message:        " + ByteUtil.toString(this.serialize());
    } else {
      return "SettingsRequest: Not initialized. Reference Level & Resolution Bandwidth must be set.";
    }
  }

  @Override
  public String toString() {
    if (this.isValid()) {
      return "SET CF [" + centerFrequencyMHz
        + "] span [" + spanMHz
        + "] RL [" + referenceLevel
        + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "SettingsRequest: Not initialized. Reference Level & Resolution Bandwidth must be set.";
    }
  }
}
