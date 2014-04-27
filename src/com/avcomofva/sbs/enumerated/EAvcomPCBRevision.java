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
package com.avcomofva.sbs.enumerated;

/**
 * Enumerated PCB Revisions. From Table 2: PCB Revision
 * <p>
 * @author jesse
 */
public enum EAvcomPCBRevision {

  // PBC Revisions by Byte
  FAB08D02RevA1((byte) 0x41, "FAB-08D02 Revision A1"),
  FAB08D02RevB1((byte) 0x42, "FAB-08D02 Revision B1"),
  FAB08D02RevC1((byte) 0x43, "FAB-08D02 Revision C1"),
  FAB08D02RevA2((byte) 0x0a, "FAB-08D02 Revision A2"),
  FAB08D02RevB2((byte) 0x0b, "FAB-08D02 Revision B2"),
  FAB08D02RevC2((byte) 0x0c, "FAB-08D02 Revision C2"),
  FAB08H01RevA1((byte) 0x1a, "FAB-08H01 Revision A1"),
  FAB08H01RevB1((byte) 0x1b, "FAB-08H01 Revision B1"),
  FAB08H01RevC1((byte) 0x1c, "FAB-08H01 Revision C1"),
  FAB09D09RevA1((byte) 0x2a, "FAB-09D09 Revision A1"),
  FAB09D09RevB1((byte) 0x2b, "FAB-09D09 Revision B1"),
  FAB09D09RevC1((byte) 0x2c, "FAB-09D09 Revision C1"),
  UNKNOWN((byte) 0xFF, "Unrecognized"),
  DUMMY((byte) 0xDD, "Software Dummy Adapter");

  private final byte code;
  private final String label;

  private EAvcomPCBRevision(byte code, String label) {
    this.code = code;
    this.label = label;
  }

  /**
   * The PCB board revision byte code.
   * <p>
   * @return a byte value
   */
  public byte getByteCode() {
    return this.code;
  }

  /**
   * The PCB board revision label
   * <p>
   * @return a string label value
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Get a product from its byte code
   * <p>
   * @param byteCode
   * @return
   */
  public static EAvcomPCBRevision fromByteCode(byte byteCode) {
    for (EAvcomPCBRevision eAvcomPCBRevision : EAvcomPCBRevision.values()) {
      if (eAvcomPCBRevision.getByteCode() == byteCode) {
        return eAvcomPCBRevision;
      }
    }
    return null;
  }

  /**
   * Get a product from its label
   * <p>
   * @param label
   * @return
   */
  public static EAvcomPCBRevision fromLabel(String label) {
    for (EAvcomPCBRevision eAvcomPCBRevision : EAvcomPCBRevision.values()) {
      if (eAvcomPCBRevision.getLabel().equalsIgnoreCase(label)) {
        return eAvcomPCBRevision;
      }
    }
    return null;
  }

  /**
   * Get a product label from its byte code
   * <p>
   * @param byteCode
   * @return
   */
  public static String labelFromBytecode(byte byteCode) {
    EAvcomPCBRevision rev = fromByteCode(byteCode);
    return rev != null ? rev.getLabel() : "Unrecognized PCB Board Revision [" + Integer.toHexString(byteCode) + "]";
  }
}
