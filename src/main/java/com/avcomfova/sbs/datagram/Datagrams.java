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

import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.datagram.read.TraceResponse;
import com.avcomofva.sbs.datagram.read.TraceResponse12Bit;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;

/**
 * A helper utility class to quickly identify and parse Avcom datagram bytes
 * into a useful datagram instance.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class Datagrams {

  /**
   * Utility method to get and build an Avcom datagram instance from a raw data
   * byte array. The byte array must be well formed according the the Avcom
   * Remote Spectrum Analyzer Protocol.
   * <p>
   * The returned interface will likely need to be casted to gain access to its
   * type-specific methods and fields.
   * <p>
   * @param data the Avcom datagram raw byte array. This will be inspected and
   *             parsed
   * @return an Avcom IDatagram instance
   * @throws Exception if the datagram type cannot be interpreted or if the data
   *                   cannot be parsed.
   */
  public static IDatagram getInstance(byte[] data) throws Exception {
    if (data == null) {
      throw new Exception("Null Avcom datagram data");
    }
    /**
     * The datagram type is always at byte address 3.
     */
    EAvcomDatagram avcomDatagram = EAvcomDatagram.fromByteCode(data[3]);
    if (avcomDatagram != null) {
      switch (avcomDatagram) {
        case HARDWARE_DESCRIPTION_REQUEST:
        case LNB_DESCRIPTION_REQUEST:
        case SETTINGS_REQUEST:
        case TRACE_REQUEST:
        case TRACE_REQUEST_12BIT:
          /**
           * No op for request types.
           */
          break;
        case ERROR_RESPONSE:
        case LNB_DESCRIPTION_RESPONSE:
        case RESERVED_RESPONSE:
        case TRACE_DATAGRAM:
          throw new UnsupportedOperationException(avcomDatagram + " not supported yet.");

        case HARDWARE_DESCRIPTION_RESPONSE:
          return new HardwareDescriptionResponse(data);
        case TRACE_RESPONSE:
          return new TraceResponse(data);
        case TRACE_RESPONSE_12BIT:
          return new TraceResponse12Bit(data);
        default:
          throw new AssertionError(avcomDatagram.name());

      }
    }
    throw new Exception("Unrecognized Avcom datagram type : [" + data[3] + "]");
  }
}
