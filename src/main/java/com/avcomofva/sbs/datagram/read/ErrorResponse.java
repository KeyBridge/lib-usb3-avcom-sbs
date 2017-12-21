/* 
 * Copyright (c) 2017, Key Bridge
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
package com.avcomofva.sbs.datagram.read;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.DatagramType;
import java.util.Arrays;
import javax.usb3.utility.ByteUtility;

/**
 * Avcom Error Response message. (Table 16: Error Message.)
 *
 * @author Jesse Caulfield
 */
public final class ErrorResponse extends ADatagram {

  /**
   * The error message returned from the Avcom device.
   */
  private String errorMessage;

  /**
   * Construct a new ErrorResponse message from a byte array provided by an
   * Avcom device.
   *
   * @param bytes a device message
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  public ErrorResponse(byte[] bytes) throws Exception {
    super(DatagramType.ERROR_RESPONSE);
    this.valid = true;
    this.elapsedTimeMillis = 1;
    this.transactionId = System.currentTimeMillis();
    parse(bytes);
  }

  /**
   * Get the error message returned from the Avcom device.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

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
    int length = ByteUtility.twoByteIntFromBytes(bytes, 1);
//    char[] chars = new char[length];
//    System.arraycopy(bytes, 4, chars, 0, length);
//    errorMessage = String.copyValueOf(chars);
    this.errorMessage = new String(Arrays.copyOfRange(bytes, 4, length));
    this.valid = true;
  }

  /**
   * @return always returns null
   */
  @Override
  public byte[] serialize() {
    return null;
  }

  @Override
  public String toString() {
    return "ERR: [" + type
           + "] MSG: [" + errorMessage
           + "]";
  }
}
