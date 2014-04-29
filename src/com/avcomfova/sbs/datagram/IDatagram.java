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
package com.avcomfova.sbs.datagram;

import com.avcomofva.sbs.enumerated.EAvcomDatagram;

/**
 * Interface describing the variables and methods that a Datagram instance must
 * support.
 * <p>
 * Datagram implementations wrap a byte array of raw data containing a header
 * describing the datagram type and a body containing all the data. On creation
 * Datagram instances should parse the header and data and pre-populate their
 * own internal parameters. These are, at minimum, the <code>DatagramType</code>
 * <p>
 * Developer notes: All data packets start with a 4-byte packet header
 * consisting of <ul><li> [0] STX: the start transmission (0x02) byte <li>
 * <li> [1-2] LEN: a two-byte length value (does not include STX or LEN
 * bytes)</li>
 * <li> [3] TYPE: a packet type indicator byte </li></ul>
 * <p>
 * All data packets end with the ETX (0x03) byte.
 * <p>
 * A TRACE packet is either 341 bytes (firmware &lt; v1.9) or 344 bytes
 * (firmware &ge; v1.9). The first 320 bytes of data (following the header) are
 * unsigned 8-bit trace data. The remaining bytes identify the trace
 * configuration parameters of center frequency, span, reference level, etc.
 * <p>
 * Trace data bytes must be converted to power units according to a method
 * dependent upon the device firmware version.
 * <p>
 * Device do not support streaming: a new TRACE_REQUEST must be sent to the
 * device each time a TRACE_RESPONSE is desired. Devices will (randomly) discard
 * requests when over driven (e.g. too many requests coming too fast.) To
 * prevent this the device controller MUST wait for a complete response before
 * sending a new request.
 * <p>
 * @author jesse
 */
public interface IDatagram {

// Datagram types that the sensor accepts --------------------------------------
//  public static final byte WAVEFORM_REQUEST_ID = 0x03;
//  public static final byte SETTINGS_REQUEST_ID = 0x04;
//  public static final byte HARDWARE_DESCRIPTION_REQUEST_ID = 0x07;
//  public static final byte LNB_DESCRIPTION_REQUEST_ID = 0x0D;
// Datagram types that the sensor creates --------------------------------------
//  public static final byte HARDWARE_DESCRIPTION_RESPONSE_ID = 0x07;
//  public static final byte RESERVED_RESPONSE_ID = 0x08; // for hardware debugging
//  public static final byte WAVEFORM_RESPONSE_ID = 0x09; // 8-bit encoded waveform data
//  public static final byte LNB_DESCRIPTION_RESPONSE_ID = 0x0D;
//  public static final byte WAVEFORM_RESPONSE_12BIT_ID = 0x0F; // 12-bit encoded waveform data
//  public static final byte ERROR_RESPONSE_ID = 0x60;
// Key Bridge type extension ---------------------------------------------------
//  public static final byte WAVEFORM_RESPONSE_EXTENDED_ID = 0x0A;
// Datagram Lengths ----------------------------------------------------------
//  public static final int HARDWARE_DESCRIPTION_REQUEST_LENGTH = 0x0003; // table 4
//  public static final int LNB_POWER_DESCRIPTION_REQUEST_LENGTH = 0x002;// table 5
//  public static final int TRACE_REQUEST_LENGTH = 0x003;// table 6
//  public static final int HARDWARE_DESCRIPTION_RESPONSE_LENGTH = 0x0055; // table 7
  public static final int LNB_POWER_RESPONSE_LENGTH = 0x002D; // table 8
//static final int WaveformDataOldLength = 0x0155;// table 10 Firmware < v1.9
//static final int SETTINGS_RESPONSE_OLD_LENGTH = 0x000D; // table 13 Firmware < 1.9
//  public static final int SETTINGS_REQUEST_LENGTH = 0x0010; // table 12 Firmware >= 1.9
//  public static final int TRACE_RESPONSE_LENGTH = 0x0155; // table 9Firmware >= v1.9
//  public static final int TRACE_DATA_LENGTH = 320;// first 320 bytes are 8-bit unsigned int data
//  public static final int TRACE_RESPONSE_12BIT_LENGTH = 0x01F5; // table 11 Firmware >= 2.10
//  public static final int TRACE_DATA_12BIT_LENGTH = 344;// 12-bit encoded waveform data
//static final int UNKNOWN_DATAGRAM_LENGTH = 0x003; // table 15
// Avcom Datagram header/footer flags ------------------------------------------
  public static final byte FLAG_STX = 0x02; // start datagram flag
  public static final byte FLAG_ETX = 0x03; // end datagram flag
  public static final int HEADER_SIZE = 3;// FLAG_STX[1] + length[2]
// Pre-Cooked request messages -------------------------------------------------
//  public static final byte[] GET_TRACE_REQUEST_MESSAGE = new byte[]{2, 0, 3, 3, 3, 3};
//  public static final byte[] GET_HARDWARE_DESCRIPTION_REQUEST_MESSAGE = new byte[]{2, 0, 3, 7, 0, 3};
//----------------------------------------------------------------------------
// TODO - determine these parameters from the device type - see ProductID enum
//  public static final double AVCOM_MAXIMUM_FREQUENCY_MHZ = 2520; // the highest frequency this device will support
//  public static final double AVCOM_MINIMUM_FREQUENCY_MHZ = 5;// the lowest frequency this device will support
// Datagram housekeeping -----------------------------------------------------
//  private byte datagramType = 0;
//  private byte[] datagramData = null;
//  private Boolean isValid;
// how long it took for the hardware to create this Datagram in milliseconds
//  private int elapsedTimeMS = 0;
//  private String jobSerialNumber = "0";
//  private String sensorSerialNumber;

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  public void parse(byte[] bytes) throws Exception;

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   * <p>
   * @return a byte array
   */
  public byte[] serialize();

  /**
   * Get the datagram data contents. The array value units of measure depend
   * upon the implementation but are typically in <code>dB</code>.
   * <p>
   * @return the data array returned from the sensor
   */
  public double[] getData();

  /**
   * Is this a properly structured and complete datagram - are all the bits in
   * place
   * <p>
   * @return true if the datagram is valid
   */
  public boolean isValid();

  /**
   * Get the time required for the hardware to collect and create this Datagram
   * <p>
   * @return Elapsed time in milliseconds
   */
  public long getElapsedTimeMillis();

  /**
   * Set the time required for the hardware to collect and create this Datagram
   * <p>
   * @param elapsedTimeMS Elapsed time in milliseconds
   */
  public void setElapsedTimeMillis(long elapsedTimeMS);

  /**
   * Get the datagram type identifier
   * <p>
   * @return the datagram type identifier
   */
  public EAvcomDatagram getDatagramType();

  /**
   * Get the transaction identifier. This is used by a device controller to
   * correlate TRACE_REQUESTS with TRACE_RESPONSES.
   * <p>
   * @return a unique transaction id
   */
  public Long getTransactionId();

  /**
   * Set the transaction identifier. This is used by a device controller to
   * correlate TRACE_REQUESTS with TRACE_RESPONSES.
   * <p>
   * @param transactionId a unique transaction id
   */
  public void setTransactionId(Long transactionId);

}
