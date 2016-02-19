/*
 * Copyright (c) 2014, Jesse Caulfield
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
import com.avcomofva.sbs.enumerated.EDatagramType;
import com.avcomofva.sbs.enumerated.EProductID;
import com.avcomofva.sbs.enumerated.EReferenceLevel;
import com.avcomofva.sbs.enumerated.EResolutionBandwidth;
import java.util.Map;
import java.util.TreeMap;
import javax.usb.utility.ByteUtility;

/**
 * Waveform Response Datagram from Avcom devices 12-bit waveform packet. Table
 * 11. (Firmware rev >= v2.10)
 *
 * @author Jesse Caulfield
 * @deprecated 04/29/14 - 8 bits provides sufficient resolution for our needs.
 * This response datagram is not supported in the AvcomSBS extended data
 * handling implementation.
 */
public class Waveform12BitResponse extends ADatagram {

  /**
   * The Datagram type.
   */
  private static final EDatagramType TYPE = EDatagramType.WAVEFORM_12BIT_RESPONSE;
  /**
   * The datagram length (0x01F5 = 501 bytes).
   * <p>
   * (Table 11 Firmware >= 2.10)
   */
  private static final int DATAGRAM_LENGTH = 0x01F5;
  /**
   * 480 bytes. The length of the data portion of this RESPONSE message.
   * <p>
   * Byte positions 4 to 483 (inclusive) = 480 bytes encoding 12-bit data
   * points. This is used internally and by the AvcomSBS to create a piecewise
   * SettingsRequest set.
   */
  private static final int DATAGRAM_PAYLOAD_LENGTH = 480;

  /**
   * Indicator that the sensor detected saturation while reading data for this
   * waveform.
   */
  private boolean saturated = false;
  /**
   * The Product ID. Byte #484.
   */
  private EProductID productId;
  /**
   * The center frequency (MHz). Byte #485-488.
   */
  private double centerFrequency;
  /**
   * The waveform span (MHz). Byte #489-492.
   */
  private double span;
  /**
   * The sensor reference level. Byte #493
   */
  private EReferenceLevel referenceLevel;
  /**
   * The waveform resolution bandwidth. Byte #494
   */
  private EResolutionBandwidth resolutionBandwidth;
  /**
   * The sensor input connector. Byte #495.
   */
  private int inputConnector;
  /**
   * The sensor internal extender. Byte #496-497.
   */
  private final byte[] internalExtender = new byte[2];
  /**
   * The sensor external extender. Byte #498-499.
   */
  private final byte[] externalExtender = new byte[2];
  /**
   * The sensor LNB power for current RF input. Byte #500.
   */
  private int lnbPower;
  /**
   * Reserved byte. Byte #501. Typically 0xff.
   */
  private int reserved01;
  /**
   * Reserved byte. Byte #502. Typically 0xff.
   */
  private int reserved02;

  public Waveform12BitResponse(byte[] bytes) throws Exception {
    super(TYPE);
    this.parse(bytes);
  }

  //<editor-fold defaultstate="collapsed" desc="Getter Methods">
  public double getCenterFrequency() {
    return centerFrequency;
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

  public EProductID getProductId() {
    return productId;
  }

  public EReferenceLevel getReferenceLevel() {
    return referenceLevel;
  }

  public EResolutionBandwidth getResolutionBandwidth() {
    return resolutionBandwidth;
  }

  public double getSpan() {
    return span;
  }

  public boolean isSaturated() {
    return saturated;
  }//</editor-fold>

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   *
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  @Override
  public void parse(byte[] bytes) throws Exception {
    this.productId = EProductID.fromByteCode(bytes[484]);
    this.centerFrequency = ByteUtility.intFrom4Bytes(bytes, 485) / 10000;
    this.span = ByteUtility.intFrom4Bytes(bytes, 489) / 10000;
    this.referenceLevel = EReferenceLevel.fromByteCode(bytes[493]);
    this.resolutionBandwidth = EResolutionBandwidth.fromByteCode(bytes[494]);
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
     * Developer note: Trace data is 479 12-bit points covering a span range
     * centered on the center frequency. Scale the byte value to dB using the
     * following equation: <code>dB = +0.20 * B + RL </code> where RL is the the
     * current reference level plus 40. (e.g. -10 dB would be -50.
     * <p>
     * Per Table 11, waveform data (dBm) = 0.20 * (unsigned int) byteValue +
     * referenceLevelOffset
     * <p>
     * If the sensor value is above 230 (90% of 255) then raise the 'saturated'
     * flag. This indicates that the reference level should be reduced and the
     * scan should be repeated.
     */
    this.data = new double[DATAGRAM_PAYLOAD_LENGTH];
    /**
     * 12-bit Waveform data cover the span range centered on the
     * CenterFrequency. Each set of 3 nibbles is a data point. Nibbles 1 and 2
     * are the integer value. The third nibble value divided by 16 is the
     * fractional value added to the integer. The sum then scaled according to
     * the waveform scaling function (per Table 11):
     * <p>
     * dB = +0.20 * X + RLOffset.
     *
     * @TODO: Extract 12 bit data.
     *
     * Extracting 12 bit data requires wading through the bit array 12 bits at a
     * time. Java is unfortunately ill-suited to this task.
     */
//    for (int i = 0; i < DATAGRAM_PAYLOAD_LENGTH; i += 3) {
//      int unsignedInt = bytes[i + 4] < 0 ? bytes[i + 4] + 255 : bytes[i + 4];
//      this.data[i] = 0.20 * unsignedInt + this.referenceLevel.getWaveformOffset();
//      /**
//       * Note the saturation status if greater than 90%
//       */
//      if (unsignedInt >= 225) {
//        this.saturated = true;
//      }
//      this.data[i] = bytes[i + 4];
//    }
    this.valid = true;
//    ByteUtility.twoByteIntFromBytes(bytes, 1) == TRACE_RESPONSE_LENGTH_12BIT;

    throw new Exception("Parsing 12-bit data not yet implemented.");

  }

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   *
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
   *
   * @return a non-null TreeMap of center frequencies and corresponding power
   *         level
   */
  public Map<Double, Double> getTraceMap() {
// need to scale the data if necessary
    Map<Double, Double> traceMap = new TreeMap<>();
//    double[] waveformDBm = getTraceDBm();
//double rbwMHz = resolutionBandwidth.getMHz();
    double startMHz = centerFrequency - span / 2;
    double cfi;

    double xScale = span / data.length;
    for (int i = 0; i < data.length; i++) {
      cfi = i * xScale + startMHz;
      traceMap.put(cfi, data[i]);
    }
    return traceMap;
  }

  @Override
  public String toString() {
    //    return ByteUtility.toString(datagramData, true);
    if (valid) {
      //      double[] wave = this.getWaveformDBm();
      //      String s = "";
      //      for (int i = 0; i < wave.length; i++) {
      //        s += "[" + wave[i] + "]";
      //      }
      return "Trace Response Datagram 12-bit"
             + "\n index   name             value"
             + "\n --------------------------------"
             + "\n this.datagramType            " + type
             + "\n this.isValid                 " + valid
             + "\n 4-483   trace length         " + (data != null ? data.length : "null")
             + "\n 484     productId            " + productId
             + "\n 485-488 centerFrequencyMHz:  " + centerFrequency
             + "\n 489-492 spanMHz              " + span
             + "\n 293     referenceLevel       " + referenceLevel
             + "\n 494     resolutionBandwidth: " + resolutionBandwidth
             + "\n 495     inputConnector       " + inputConnector
             + "\n 496-497 internalExtender     0x " + ByteUtility.toString(internalExtender)
             + "\n 498-499 externalExtender     0x " + ByteUtility.toString(externalExtender)
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
      return "TR12: CF [" + centerFrequency
             + "] Span [" + span
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
