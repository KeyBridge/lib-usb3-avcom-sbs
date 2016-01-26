/*
 * Copyright (c) 2014, Jesse Caulfield <jesse@caulfield.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
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
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.avcomofva.sbs.enumerated.EAvcomProductID;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import ch.keybridge.sensor.util.ByteUtil;
import java.util.Map;
import java.util.TreeMap;

/**
 * Waveform Response Datagram from Avcom devices 12-bit waveform packet
 * (Firmware rev >= v2.10 Table 11
 * <p>
 * The device responds with a {@linkplain TraceResponse12Bit}
 * <p>
 * @author Jesse Caulfield
 * @deprecated 04/29/14 - 8 bits provides sufficient resolution for our needs.
 * This response datagram is not supported in the AvcomSBS extended data
 * handling implementation.
 */
public class TraceResponse12Bit extends ADatagram {

  /**
   * The datagram length (bytes) for a standard TRACE_RESPONSE.
   */
  private static final int TRACE_RESPONSE_LENGTH_12BIT = 0x01F5; // table 11 Firmware >= 2.10
  /**
   * The length of the data portion of a TRACE_RESPONSE message.
   */
  private static final int TRACE_DATA_LENGTH_12BIT = 344;// 12-bit encoded waveform data

  private boolean saturated = false; // did the sensor detect saturation?
  //----------------------------------------------------------------------------
  // Setting values  Byte Location in Data
//  private final byte[] waveform = new byte[TRACE_DATA_12BIT_LENGTH]; // 4-483: 479 12-bit points scaled per description in constructor
  private EAvcomProductID productId; // 484
  private double centerFrequencyMHz; // 485-488
  private double spanMHz;  // 489-492
  private EAvcomReferenceLevel referenceLevel; // 493
  private EAvcomResolutionBandwidth resolutionBandwidth;// 494
  private int inputConnector; // 495
  private final byte[] internalExtender = new byte[2];  // 496-497
  private final byte[] externalExtender = new byte[2];  // 498-499
  private int lnbPower;  // 500
  private int reserved01;// 501  typically 0xff
  private int reserved02;// 502  typically 0xff

  public TraceResponse12Bit(byte[] bytes) throws Exception {
    super(EAvcomDatagram.TRACE_RESPONSE_12BIT);
    this.parse(bytes);
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

  public boolean isSaturated() {
    return saturated;
  }//</editor-fold>

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
    this.productId = EAvcomProductID.fromByteCode(bytes[484]);
    this.centerFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 485) / 10000;
    this.spanMHz = ByteUtil.intFrom4Bytes(bytes, 489) / 10000;
    this.referenceLevel = EAvcomReferenceLevel.fromByteCode(bytes[493]);
    this.resolutionBandwidth = EAvcomResolutionBandwidth.fromByteCode(bytes[494]);
    this.inputConnector = bytes[495] - 9;
    for (int i = 0; i < 2; i++) {
      this.internalExtender[i] = bytes[496 + i];
    }
    for (int i = 0; i < 2; i++) {
      this.externalExtender[i] = bytes[498 + i];
    }
    this.lnbPower = bytes[500];
    this.reserved01 = bytes[501];
    this.reserved02 = bytes[502];
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
    this.data = new double[TRACE_DATA_LENGTH_12BIT];
    for (int i = 0; i < TRACE_DATA_LENGTH_12BIT; i++) {
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
    this.valid = true;
//    ByteUtil.twoByteIntFromBytes(bytes, 1) == TRACE_RESPONSE_LENGTH_12BIT;
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
   * Assemble a Map object from the waveform data.
   * <p>
   * In a waveform, each sample byte can be described by it's own center
   * frequency and span. Aggregated waveform data is stored at dBm values with
   * centerFrequency indices as follows:
   * <pre>
   * | <----------------------- span s -------------------->   |
   * | ^ startFrequency          ^ centerFrequency             |
   * | [   ][   ][   ][   ][   ][   ][   ][   ] [   ][   ][   ]|
   * |   ^cfi
   * <p>
   * Where: startFrequency = centerFrequency - span / 2 i = byte count (from 0
   * to length of sample and  cfi = resolutionBandwidth * i + startFrequency.
   * </pre>
   * <p>
   * @return a non-null TreeMap of center frequencies and corresponding power
   *         level
   */
  public Map<Double, Double> getTraceMap() {
// need to scale the data if necessary
    Map<Double, Double> traceMap = new TreeMap<>();
//    double[] waveformDBm = getTraceDBm();
//double rbwMHz = resolutionBandwidth.getMHz();
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
      return "Trace Response Datagram 12-bit"
        + "\n index   name             value"
        + "\n --------------------------------"
        + "\n this.datagramType            " + datagramType
        + "\n this.isValid                 " + valid
        + "\n 4-483   trace length         " + (data != null ? data.length : "null")
        + "\n 484     productId            " + productId
        + "\n 485-488 centerFrequencyMHz:  " + centerFrequencyMHz
        + "\n 489-492 spanMHz              " + spanMHz
        + "\n 293     referenceLevel       " + referenceLevel
        + "\n 494     resolutionBandwidth: " + resolutionBandwidth
        + "\n 495     inputConnector       " + inputConnector
        + "\n 496-497 internalExtender     0x " + ByteUtil.toString(internalExtender)
        + "\n 498-499 externalExtender     0x " + ByteUtil.toString(externalExtender)
        + "\n 500     lnbPower             0x" + Integer.toHexString(lnbPower)
        + "\n 501     reserved01           0x" + Integer.toHexString(reserved01)
        + "\n 502     reserved02           0x" + Integer.toHexString(reserved02) //              + "\n waveform data in DBM           " + s
        ;
    } else {
      return "Trace Response datagram not initialized.";
    }
  }

  public String toStringBrief() {
    if (valid) {
      return "TR12: CF [" + centerFrequencyMHz
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
