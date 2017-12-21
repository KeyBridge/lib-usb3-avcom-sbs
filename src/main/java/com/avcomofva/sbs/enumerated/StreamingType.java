/*
 * Copyright 2016 Caulfield IP Holdings (Caulfield) and affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * Software Code is protected by copyright. Caulfield hereby
 * reserves all rights and copyrights and no license is
 * granted under said copyrights in this Software License Agreement.
 * Caulfield generally licenses software for commercialization
 * pursuant to the terms of either a Standard Software Source Code
 * License Agreement or a Standard Product License Agreement.
 * A copy of these agreements may be obtained by sending a request
 * via email to info@caufield.org.
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
