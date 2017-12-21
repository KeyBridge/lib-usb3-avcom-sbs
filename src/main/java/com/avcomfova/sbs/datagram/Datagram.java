/*
 * Copyright (c) 2014, Jesse Caulfield
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

import com.avcomofva.sbs.datagram.read.ErrorResponse;
import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.datagram.read.Waveform8BitResponse;
import com.avcomofva.sbs.enumerated.DatagramType;

/**
 * A helper utility class to quickly identify and parse Avcom datagram bytes
 * into a useful datagram instance.
 *
 * @author Jesse Caulfield
 */
public class Datagram {

  /**
   * Utility method to get and build an Avcom datagram instance from a raw data
   * byte array. The byte array must be well formed according the the Avcom
   * Remote Spectrum Analyzer Protocol.
   * <p>
   * The returned interface will likely need to be casted to gain access to its
   * type-specific methods and fields.
   *
   * @param data the Avcom datagram raw byte array. This will be inspected and
   *             parsed
   * @return an Avcom IDatagram instance
   * @throws Exception if the datagram type cannot be interpreted or if the data
   *                   cannot be parsed.
   */
  public static IDatagram getInstance(byte[] data) throws Exception {
    if (data == null) {
      throw new Exception("Null data error.");
    }
    /**
     * The datagram type is always at byte address 3.
     */
    DatagramType datagramType = DatagramType.fromByteCode(data[3]);
    /**
     * No op for request types.
     */
    switch (datagramType) {
      case HARDWARE_DESCRIPTION_REQUEST:
      case LNB_DESCRIPTION_REQUEST:
      case SETTINGS_REQUEST:
      case WAVEFORM_8BIT_REQUEST:
      case WAVEFORM_12BIT_REQUEST:
        break;
      case LNB_DESCRIPTION_RESPONSE:
      case RESERVED_RESPONSE:
      case WAVEFORM:
        throw new UnsupportedOperationException(datagramType + " not yet implemented.");
      case ERROR_RESPONSE:
        return new ErrorResponse(data);
      case HARDWARE_DESCRIPTION_RESPONSE:
        return new HardwareDescriptionResponse(data);
      case WAVEFORM_8BIT_RESPONSE:
        return new Waveform8BitResponse(data);
      case WAVEFORM_12BIT_RESPONSE:
        throw new UnsupportedOperationException(datagramType + " not yet implemented.");
//          return new Waveform12BitResponse(data);
      default:
        throw new AssertionError(datagramType.name());
    }
    throw new Exception("Unrecognized Avcom datagram type : [" + data[3] + "]");
  }
}
