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
package com.avcomofva.sbs.enumerated;

/**
 * Enumerated Waveform transmission settings types. (From Table 6).
 * <p>
 * The byte codes from this enumerated type are entered in byte #4 of a Waveform
 * request datagram.
 *
 * @author Key Bridge LLC
 */
public class StreamingType {

  /**
   * 0x00. Stop streaming.
   */
  public static final byte STOP = (byte) 0x00;
  /**
   * 0x01. Start streaming.
   *
   * @deprecated not implemented
   */
  public static final byte START = (byte) 0x01;
  /**
   * 0x03. Send a single waveform with 8-bit resolution.
   */
  public static final byte SEND_8BIT = (byte) 0x03;
  /**
   * 0x04. Stream AM waveform data.
   *
   * @deprecated not implemented
   */
  public static final byte STREAM_AM = (byte) 0x04;
  /**
   * 0x05. Send a single waveform with 12-bit resolution. (Firmware Rev >=
   * v2.10)
   */
  public static final byte SEND_12BIT = (byte) 0x05;

}
