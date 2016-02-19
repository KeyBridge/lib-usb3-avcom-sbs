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
package com.avcomofva.sbs.enumerated;

/**
 * Enumerated Avcom Product PCB Revisions.
 * <p>
 * From Table 2: PCB Revision
 *
 * @author Jesse Caulfield
 */
public enum EPCBRevision {

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

  /**
   * The Product PCB Revision (byte-code).
   */
  private final byte byteCode;
  /**
   * A human readable label / description.
   */
  private final String label;

  private EPCBRevision(byte code, String label) {
    this.byteCode = code;
    this.label = label;
  }

  /**
   * The PCB board revision byte code.
   *
   * @return a byte value
   */
  public byte getByteCode() {
    return this.byteCode;
  }

  /**
   * The PCB board revision label
   *
   * @return a string label value
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Get a Product PCB Revision from its byte code
   *
   * @param byteCode the byte-code value
   * @return the Product PCB Revision
   */
  public static EPCBRevision fromByteCode(byte byteCode) {
    for (EPCBRevision eAvcomPCBRevision : EPCBRevision.values()) {
      if (eAvcomPCBRevision.getByteCode() == byteCode) {
        return eAvcomPCBRevision;
      }
    }
    return null;
  }

}
