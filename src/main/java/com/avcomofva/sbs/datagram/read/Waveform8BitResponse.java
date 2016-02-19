/*
 * Copyright (c) 2014, Jesse Caulfield
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
import com.avcomofva.sbs.enumerated.EDatagramType;
import com.avcomofva.sbs.enumerated.EProductID;
import com.avcomofva.sbs.enumerated.EReferenceLevel;
import com.avcomofva.sbs.enumerated.EResolutionBandwidth;
import java.util.Map;
import java.util.TreeMap;
import javax.usb3.utility.ByteUtility;

/**
 * Waveform Response Datagram from Avcom devices 8-bit waveform packet
 * <p>
 * Table 9. (Firmware rev >= v1.9)
 *
 * @author Jesse Caulfield
 */
public class Waveform8BitResponse extends ADatagram {

  /**
   * The Datagram type.
   */
  private static final EDatagramType TYPE = EDatagramType.WAVEFORM_8BIT_RESPONSE;
  /**
   * The datagram length (0x0155 = 341 bytes).
   * <p>
   * Table 9 (Firmware >= v1.9)
   */
  private static final int DATAGRAM_LENGTH = 0x0155;
  /**
   * The signal level above which the sensor should be considered saturated. If
   * any data points are above this level then a flag is raised and the waveform
   * should be resampled at a lower reference level.
   */
  private static final int SATURATED = 230;

  /**
   * 320 bytes. The length of the data portion of this RESPONSE message.
   * <p>
   * Byte positions 4 to 323 (inclusive) = 320. This is used internally and by
   * the AvcomSBS to create a piecewise SettingsRequest set.
   */
  public static final int DATAGRAM_PAYLOAD_LENGTH = 320;

  /**
   * Indicator that the sensor detected saturation while reading data for this
   * waveform.
   */
  private boolean saturated = false; // did the sensor detect saturation?
  /**
   * The Product ID. Byte #324.
   */
  private EProductID productId;
  /**
   * The center frequency (MHz). Byte #325-328.
   */
  private double centerFrequency;
  /**
   * The waveform span (MHz). Byte #329-332.
   */
  private double span;
  /**
   * The sensor reference level. Byte #333
   */
  private EReferenceLevel referenceLevel;
  /**
   * The waveform resolution bandwidth. Byte #334.
   */
  private EResolutionBandwidth resolutionBandwidth;
  /**
   * The sensor input connector. Byte #335.
   */
  private int inputConnector;
  /**
   * The sensor internal extender. Byte #336-337.
   */
  private final byte[] internalExtender = new byte[2];
  /**
   * The sensor external extender. Byte #338-339.
   */
  private final byte[] externalExtender = new byte[2];
  /**
   * The sensor LNB power for current RF input. Byte #340.
   */
  private int lnbPower;
  /**
   * Reserved byte. Byte #341. Typically 0xff.
   */
  private int reserved01;
  /**
   * Reserved byte. Byte #342. Typically 0xff.
   */
  private int reserved02;

  /**
   * Empty constructor. The data must be parsed separately.
   */
  public Waveform8BitResponse() {
    super(TYPE);
  }

  /**
   * Construct a new TraceResponse instance, automatically parsing the data byte
   * array.
   *
   * @param bytes the data byte array provided by an Avcom sensor
   * @throws java.lang.Exception if the bytes fail to parse correctly.
   */
  public Waveform8BitResponse(byte[] bytes) throws Exception {
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
    this.productId = EProductID.fromByteCode(bytes[324]);
    this.centerFrequency = ByteUtility.intFrom4Bytes(bytes, 325) / 10000;
    this.span = ByteUtility.intFrom4Bytes(bytes, 329) / 10000;
    this.referenceLevel = EReferenceLevel.fromByteCode(bytes[333]);
    this.resolutionBandwidth = EResolutionBandwidth.fromByteCode(bytes[334]);
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
     * Finally, copy the data bytes into the local array (of doubles),
     * converting from byte to units dB.
     * <p>
     * Developer note: Per Table 9: waveform data is 320 8-bit points covering a
     * span range centered on the center frequency. Scale the byte value to dB
     * using the following equation: <code>dB = +0.20 * B + RL </code> where RL
     * is the the current reference level plus 40. (e.g. -10 dB would be -50.
     * <p>
     * If the sensor value is above 230 (90% of 255) then raise the 'saturated'
     * flag. This indicates to the controller that the reference level should be
     * reduced and the waveform data should be resampled.
     */
    this.data = new double[DATAGRAM_PAYLOAD_LENGTH];
    /**
     * Error condition - if the ReferenceLevel was not read then FAIL parsing.
     */
    if (this.referenceLevel == null) {
      throw new Exception("Invalid Reference Level value: " + bytes[333]);
    }
    for (int i = 0; i < DATAGRAM_PAYLOAD_LENGTH; i++) {
      /**
       * Upshift negative byte values when converting to an unsigned int.
       */
      int unsignedInt = bytes[i + 4] < 0 ? bytes[i + 4] + 255 : bytes[i + 4];
      this.data[i] = 0.20 * unsignedInt + this.referenceLevel.getWaveformOffset();
      /**
       * Note the saturation status if greater than 90%.
       */
      if (unsignedInt >= SATURATED) {
        this.saturated = true;
      }
    }
    this.valid = true;
  }

  /**
   * Convert this datagram into a byte array.
   *
   * @return a byte array
   */
  @Override
  public byte[] serialize() {
    return null;
  }

  /**
   * Assemble a Map object from the waveform data. In a waveform, each sample
   * byte can be described by it's own center frequency and span. Aggregated
   * waveform data is stored at dBm values with centerFrequency indices.
   * <pre>
   * | <----------------------- span s --------------------> |
   * | ^ startFrequency           ^ centerFrequency          |
   * |[   ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]|
   * |  ^cf_i
   * <p>
   * Where:
   *   startFrequency = centerFrequency - span / 2
   *   i              = byte count (from 0 to length of sample
   *   cf_i           = resolutionBandwidth * i + startFrequency
   * </pre>
   *
   * @return
   */
  public Map<Double, Double> getTraceMap() {
    // need to scale the data if necessary
    Map<Double, Double> traceMap = new TreeMap<>();
    double startMHz = centerFrequency - span / 2;
    double cfi;

    double xScale = span / data.length;
    for (int i = 0; i < data.length; i++) {
      cfi = i * xScale + startMHz;
      traceMap.put(cfi, data[i]);
    }
    return traceMap;
  }

  public String toStringFull() {
    //    return ByteUtility.toString(datagramData, true);
    if (valid) {
      //      double[] wave = this.getWaveformDBm();
      //      String s = "";
      //      for (int i = 0; i < wave.length; i++) {
      //        s += "[" + wave[i] + "]";
      //      }
      return "Trace Response Datagram"
             + "\n index   name             value"
             + "\n --------------------------------"
             + "\n this.datagramType            " + type
             + "\n this.isValid                 " + valid
             + "\n 4-324   trace length         " + (data != null ? data.length : "null")
             + "\n 324     productId            " + productId
             + "\n 325-328 centerFrequencyMHz:  " + centerFrequency
             + "\n 329-332 spanMHz              " + span
             + "\n 333     referenceLevel       " + referenceLevel
             + "\n 334     resolutionBandwidth: " + resolutionBandwidth
             + "\n 335     inputConnector       " + inputConnector
             + "\n 336-337 internalExtender     0x " + ByteUtility.toString(internalExtender)
             + "\n 338-339 externalExtender     0x " + ByteUtility.toString(externalExtender)
             + "\n 340     lnbPower             0x" + Integer.toHexString(lnbPower)
             + "\n 341     reserved01           0x" + Integer.toHexString(reserved01)
             + "\n 342     reserved02           0x" + Integer.toHexString(reserved02) //              + "\n waveform data in DBM           " + s
              ;
    } else {
      return "Waveform Response datagram not initialized.";
    }
  }

  @Override
  public String toString() {
    if (valid) {
      return "TR8: CF [" + centerFrequency
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
