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

import com.avcomofva.sbs.datagram.read.TraceResponse;
import com.avcomofva.sbs.datagram.write.SettingsRequest;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.avcomofva.sbs.enumerated.EAvcomProductID;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import java.util.Map;
import java.util.TreeMap;

/**
 * An extended Trace data container used to exchange spectrum trace data
 * received from Avcom sensors. This TraceDatagram supports serialization and
 * arbitrary trace size.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class TraceDatagram extends ADatagram {

  /**
   * Indicator that the sensor detect saturation somewhere within the trace.
   */
  private boolean saturated = false;
  /**
   * The Avcom product ID that produced this data.
   */
  private EAvcomProductID productId;
  /**
   * The sweep center frequency MHz.
   */
  private double centerFrequencyMHz;
  /**
   * The sweep span (MHz)
   */
  private double spanMHz;
  /**
   * The sweep reference level (dBm)
   */
  private EAvcomReferenceLevel referenceLevel;
  /**
   * The sweep resolution bandwidth (enumerated) (MHz)
   */
  private EAvcomResolutionBandwidth resolutionBandwidth;
  /**
   * A map of center frequency (MHz) vs. power level (dBm).
   */
  private final Map<Double, Double> traceData;

  public TraceDatagram() {
    super(EAvcomDatagram.TRACE_DATAGRAM);
    this.traceData = new TreeMap<>();
  }

  /**
   * Build and return a new TraceDatagram instance with center frequency and
   * span values from an input SettingsRequest instance. This is used when
   * assembling a TraceDatagram from multiple TraceResponse instances that are
   * generated from a single wide-band SettingsRequest that was split into
   * multiple requests.
   * <p>
   * To be useful the component TraceResponses must be added to the
   * TraceDatagram returned by this method.
   * <p>
   * @param settingsRequest the settings request instance to copy values from.
   * @return an empty TraceDatagram instance.
   */
  public static TraceDatagram getInstance(SettingsRequest settingsRequest) {
    TraceDatagram traceDatagram = new TraceDatagram();
    traceDatagram.centerFrequencyMHz = settingsRequest.getCenterFrequencyMHz();
    traceDatagram.spanMHz = settingsRequest.getSpanMHz();
    traceDatagram.transactionId = settingsRequest.getTransactionId();
    return traceDatagram;
  }

  //<editor-fold defaultstate="collapsed" desc="Getter Methods">
  public double getCenterFrequencyMHz() {
    return centerFrequencyMHz;
  }

  public EAvcomProductID getProductId() {
    return productId;
  }

  public EAvcomReferenceLevel getReferenceLevel() {
    return referenceLevel;
  }

  public EAvcomResolutionBandwidth getResolutionBandwidth() {
    return resolutionBandwidth;
  }

  public double getSpanMHz() {
    return spanMHz;
  }

  /**
   * Get a copy of the internal Trace data. Trace data is presented as a sorted
   * map of center frequency (MHz) vs. power level (dBm).
   * <p>
   * @return a non-null TreeMap instance.
   */
  public Map<Double, Double> getTraceData() {
    return traceData != null ? new TreeMap<>(traceData) : new TreeMap<Double, Double>();
  }

  public boolean isSaturated() {
    return saturated;
  }//</editor-fold>

  /**
   * Add data from a TraceResponse into this TraceDatagram instance. The data,
   * reference level, resolutionBandwidth and saturated state are initialized
   * from the first datagram added. All subsequent datagrams must match the
   * first datagram reference level, resolutionBandwidth and product ID.
   * <p>
   * @param datagram the TraceResponse datagram to import
   * @throws java.lang.Exception if the datagram configuration does not match
   *                             the TraceDatagram configuration (RBW, RL and
   *                             Product ID)
   */
  public void addData(TraceResponse datagram) throws Exception {
    /**
     * Set the configuration values from the first datagram. All subsequent
     * datagram configurations must match.
     */
    if (resolutionBandwidth == null) {
      resolutionBandwidth = datagram.getResolutionBandwidth();
    } else if (!resolutionBandwidth.equals(datagram.getResolutionBandwidth())) {
      throw new Exception("Resolution Bandwidth values do not match. Have " + resolutionBandwidth + " adding " + datagram.getResolutionBandwidth());
    }
    if (referenceLevel == null) {
      referenceLevel = datagram.getReferenceLevel();
    } else if (!referenceLevel.equals(datagram.getReferenceLevel())) {
      throw new Exception("Reference level values do not match. Have " + referenceLevel + " adding " + datagram.getReferenceLevel());
    }
    if (productId == null) {
      productId = datagram.getProductId();
    } else if (!productId.equals(datagram.getProductId())) {
      throw new Exception("Product ID values do not match. Have " + productId + " adding " + datagram.getProductId());
    }
    /**
     * Set the saturated flag as the OR value of the current value and the new
     * value.
     */
    this.saturated = this.saturated || datagram.isSaturated();
    /**
     * Add the elapsed time to the current elapsed time.
     */
    this.elapsedTimeMillis += datagram.getElapsedTimeMillis();
    /**
     * Add all the datagram data. This may overwrite some values but that is OK.
     */
    traceData.putAll(datagram.getTraceMap());
    /**
     * With data the TraceDatagram may be considered valid and ready for use.
     */
    this.valid = true;
  }

  //<editor-fold defaultstate="collapsed" desc="IDatagram Parse & Serialize are not supported">
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
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public byte[] serialize() {
    throw new UnsupportedOperationException("Not supported.");
  }//</editor-fold>

  public String toStringFull() {
    if (valid) {
      return "Trace Datagram"
        + "\n name                    value"
        + "\n --------------------------------"
        + "\n datagramType           " + datagramType
        + "\n elapsedTimeMillis      " + elapsedTimeMillis
        + "\n data length            " + traceData.size()
        + "\n centerFrequency MHz    " + centerFrequencyMHz
        + "\n span MHz               " + spanMHz
        + "\n referenceLevel         " + referenceLevel
        + "\n resolutionBandwidth    " + resolutionBandwidth;
    } else {
      return "Trace Datagram not initialized.";
    }
  }

  @Override
  public String toString() {
    if (valid) {
      return "TRACE: CF [" + centerFrequencyMHz
        + "] Span [" + spanMHz
        + "] RL [" + referenceLevel
        + "] RBW [" + resolutionBandwidth
        + "] DATA [" + traceData.size() + " bytes"
        + "]";
    } else {
      return "Trace Datagram not initialized.";
    }
  }

}
