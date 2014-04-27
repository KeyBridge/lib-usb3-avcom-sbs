/*
 * Copyright (c) 2014, Jesse Caulfield <jesse@caulfield.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *this list of conditions and the following disclaimer in the documentation
 *and/or other materials provided with the distribution.
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
package com.avcomofva.sbs.datagram.read;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.EAvcomDatagramType;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.util.Map;
import java.util.TreeMap;

/**
 * Waveform Response Datagram from Avcom devices 8-bit waveform packet (Frimware
 * rev >= v1.9 Table 9
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class TraceResponse extends ADatagram {

  /**
   * The datagram length (bytes) for a standard TRACE_RESPONSE.
   */
  private static final int TRACE_RESPONSE_LENGTH = 0x0155; // table 9Firmware >= v1.9
  /**
   * The length of the data portion of a TRACE_RESPONSE message.
   */
  private static final int TRACE_DATA_LENGTH = 320;// first 320 bytes are 8-bit unsigned int data

  private boolean saturated = false; // did the sensor detect saturation?
  //----------------------------------------------------------------------------
  // Setting valuesByte Location in Data
  private int productId; // 324
  private double centerFrequencyMHz; // 325-328
  private double spanMHz;// 329-332
  private EAvcomReferenceLevel referenceLevel;  // 333
  private EAvcomResolutionBandwidth resolutionBandwidth;// 334
  private int inputConnector;  // 335
  private final byte[] internalExtender = new byte[2]; // 336-337
  private final byte[] externalExtender = new byte[2]; // 338-339
  private int lnbPower;  // 340
  private int reserved01;// 341  typically 0xff
  private int reserved02;// 342  typically 0xff

  public TraceResponse(byte[] bytes) {
    super(EAvcomDatagramType.TRACE_RESPONSE);
    this.valid = this.parse(bytes);
  }

  //<editor-fold defaultstate="collapsed" desc="Getter Methods">
  public double getCenterFrequencyMHz() {
    return centerFrequencyMHz;
  }

  public byte[] getExternalExtender() {
    return externalExtender;
  }

  public int getInputConnector() {
    return inputConnector;
  }

  public byte[] getInternalExtender() {
    return internalExtender;
  }

  public int getLnbPower() {
    return lnbPower;
  }

  public int getProductId() {
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

  public boolean isSaturated() {
    return saturated;
  }//</editor-fold>

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @return TRUE if parse is successful
   */
  @Override
  public boolean parse(byte[] bytes) {
    this.productId = bytes[324];
    this.centerFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 325) / 10000;
    this.spanMHz = ByteUtil.intFrom4Bytes(bytes, 329) / 10000;
    this.referenceLevel = EAvcomReferenceLevel.fromByteCode(bytes[333]);
    this.resolutionBandwidth = EAvcomResolutionBandwidth.fromByteCode(bytes[334]);
    this.inputConnector = bytes[335] - 9;
    for (int i = 0; i < 2; i++) {
      this.internalExtender[i] = bytes[336 + i];
    }
    for (int i = 0; i < 2; i++) {
      this.externalExtender[i] = bytes[338 + i];
    }
    this.lnbPower = bytes[340];
    this.reserved01 = bytes[341];
    this.reserved02 = bytes[342];
    /**
     * Finally, copy the data bytes into the local array, converting from byte
     * to units dB.
     * <p>
     * Developer note: Trace data is 320 8-bit points covering a span range
     * centered on the center frequency. Scale the byte value to dB using the
     * following equation: <code>dB = +0.20 * B + RL </code> where RL is the the
     * current reference level plus 40. (e.g. -10 dB would be -50.
     * <p>
     * Per table 9, waveform data (dBm) = 0.20 * (unsigned int) byteValue +
     * referenceLevelOffset
     * <p>
     * If the sensor value is above 230 (90% of 255) then raise the 'saturated'
     * flag. This indicates that the reference level should be reduced.
     */
    this.data = new double[TRACE_DATA_LENGTH];
    for (int i = 0; i < TRACE_DATA_LENGTH; i++) {
      /**
       * Upshift negative byte values to accommodate an unsigned int.
       */
      int unsignedInt = bytes[i + 4] < 0 ? bytes[i + 4] + 255 : bytes[i + 4];
      this.data[i] = 0.20 * unsignedInt + this.referenceLevel.getWaveformOffset();
      /**
       * Note the saturation status if greater than 90%
       */
      if (unsignedInt >= 225) {
        this.saturated = true;
      }
      this.data[i] = bytes[i + 4];
    }

    return ByteUtil.twoByteIntFromBytes(bytes, 1) == TRACE_RESPONSE_LENGTH;
  }

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   * <p>
   * @return a byte array
   */
  @Override
  public byte[] serialize() {
    return null;
  }

  /**
   * Assemble a Map object from the waveform data.<br>
   * In a waveform, each sample byte can be described by it's own center
   * frequency and span.<br>
   * Aggregated waveform data is stored at dBm values with centerFrequency
   * indices.<br>
   * <p>
   * <
   * pre>
   * | <----------------------- span s --------------------> | | ^
   * startFrequency ^ centerFrequency | |[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]| |
   * ^cfi
   * <p>
   * Where: startFrequency = centerFrequency - span / 2 i = byte count (from 0
   * to length of sample cfi = resolutionBandwidth * i + startFrequency
   * </pre>
   * <p>
   * @return
   */
  public Map<Double, Double> getTraceMap() {
    // need to scale the data if necessary
    Map<Double, Double> traceMap = new TreeMap<>();
    double startMHz = centerFrequencyMHz - spanMHz / 2;
    double cfi;

    double xScale = spanMHz / data.length;
    for (int i = 0; i < data.length; i++) {
      cfi = i * xScale + startMHz;
      traceMap.put(cfi, data[i]);
    }
    return traceMap;
  }

  @Override
  public String toString() {
    //    return ByteUtil.toString(datagramData, true);
    if (valid) {
      //      double[] wave = this.getWaveformDBm();
      //      String s = "";
      //      for (int i = 0; i < wave.length; i++) {
      //        s += "[" + wave[i] + "]";
      //      }
      return "Trace Response Datagram"
        + "\n index   name             value"
        + "\n --------------------------------"
        + "\n this.datagramType            " + datagramType
        + "\n this.isValid                 " + valid
        + "\n 4-324   trace length         " + data.length
        + "\n 324     productId            " + productId
        + "\n 325-328 centerFrequencyMHz:  " + centerFrequencyMHz
        + "\n 329-332 spanMHz              " + spanMHz
        + "\n 333     referenceLevel       " + referenceLevel
        + "\n 334     resolutionBandwidth: " + resolutionBandwidth
        + "\n 335     inputConnector       " + inputConnector
        + "\n 336-337 internalExtender     0x " + ByteUtil.toString(internalExtender)
        + "\n 338-339 externalExtender     0x " + ByteUtil.toString(externalExtender)
        + "\n 340     lnbPower             0x" + Integer.toHexString(lnbPower)
        + "\n 341     reserved01           0x" + Integer.toHexString(reserved01)
        + "\n 342     reserved02           0x" + Integer.toHexString(reserved02) //              + "\n waveform data in DBM           " + s
        ;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  public String toStringBrief() {
    if (valid) {
      return "TRACE: CF [" + centerFrequencyMHz
        + "] Span [" + spanMHz
        + "] RL [" + referenceLevel
        + "] RBW [" + resolutionBandwidth + "]";
    } else {
      return "Trace Response datagram not initialized.";
    }
  }

  public String toStringData() {
    if (valid) {
      String s = "";
      for (double element : data) {
        s += "[" + element + "]";
      }
      return s;
    } else {
      return "Trace Response datagram not initialized.";
    }
  }

}
