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
package com.avcomfova.sbs.datagram;

import com.avcomofva.sbs.enumerated.DatagramType;

/**
 * Interface describing the variables and methods that a Datagram instance must
 * support.
 * <p>
 * Datagram implementations wrap a byte array of raw data containing a header
 * describing the datagram type and a body containing all the data. On creation
 * a Datagram instances should parse the byte data and populate their own
 * internal configuration.
 * <p>
 * Developer notes: All data packets start with a 4-byte packet header
 * consisting of <ul>
 * <li> [0] STX: the start transmission (0x02) byte </li>
 * <li> [1-2] LEN: a two-byte payload length value (which does not include the
 * STX or LEN bytes)</li>
 * <li> [3] TYPE: a packet type indicator byte </li>
 * </ul>
 * All data packets end with the ETX (0x03) byte.
 * <p>
 * A 8-bit Waveform datagram is either 341 bytes (firmware &lt; v1.9) or 344
 * bytes (firmware &ge; v1.9). The first 320 bytes of data (following the
 * header) are unsigned 8-bit trace data. The remaining bytes identify the trace
 * configuration parameters of center frequency, span, reference level, etc.
 * <p>
 * Trace data bytes must be converted to power units according to a method
 * dependent upon the device firmware version and described in the protocol
 * documentation.
 * <p>
 * While the protocol includes parameters for streaming the currently available
 * Avcom SBS devices do not support streaming: a new REQUEST must be sent to the
 * device each time a RESPONSE is desired.
 * <p>
 * Also note that Devices will (randomly) discard requests when they are over
 * driven (e.g. too many requests coming too fast.) To prevent this the device
 * controller MUST wait for a complete response before sending a new request.
 *
 * @author Jesse Caulfield
 */
public interface IDatagram {

// Avcom Datagram header/footer flags ------------------------------------------
  /**
   * 0x02. All data packets start with a start transmission (STX) 0x02 byte.
   */
  public static final byte STX = 0x02;
  /**
   * 0x03. A data packets end with a End Transmission (ETX) 0x03 byte.
   */
  public static final byte ETX = 0x03; // end datagram flag
  /**
   * All data packets include a three byte header: a start transmission (STX)
   * byte plus two Length bytes. STX[1] + length[2].
   */
  public static final int HEADER_SIZE = 3;

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   *
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  public void parse(byte[] bytes) throws Exception;

  /**
   * Convert this datagram into a byte array so it may be sent to the detector
   *
   * @return a byte array
   */
  public byte[] serialize();

  /**
   * Get the datagram data contents. The array value units of measure depend
   * upon the implementation but are typically in <code>dB</code>.
   *
   * @return the data array returned from the sensor
   */
  public double[] getData();

  /**
   * Is this a properly structured and complete datagram - are all the bits in
   * place
   *
   * @return true if the datagram is valid
   */
  public boolean isValid();

  /**
   * Get the time required for the hardware to collect and create this Datagram
   *
   * @return Elapsed time in milliseconds
   */
  public long getElapsedTime();

  /**
   * Set the time required for the hardware to collect and create this Datagram
   *
   * @param elapsedTimeMS Elapsed time in milliseconds
   */
  public void setElapsedTime(long elapsedTimeMS);

  /**
   * Get the datagram type identifier
   *
   * @return the datagram type identifier
   */
  public DatagramType getType();

  /**
   * Get the transaction identifier. This is used by a device controller to
   * correlate TRACE_REQUESTS with TRACE_RESPONSES.
   *
   * @return a unique transaction id
   */
  public Long getTransactionId();

  /**
   * Set the transaction identifier. This is used by a device controller to
   * correlate TRACE_REQUESTS with TRACE_RESPONSES.
   *
   * @param transactionId a unique transaction id
   */
  public void setTransactionId(Long transactionId);

}
