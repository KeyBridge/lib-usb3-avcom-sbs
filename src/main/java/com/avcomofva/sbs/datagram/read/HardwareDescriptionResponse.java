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
package com.avcomofva.sbs.datagram.read;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.*;
import ch.keybridge.sensor.util.ByteUtil;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Avcom Hardware Description Datagram Described in Table 7
 * <p>
 * @author Jesse Caulfield
 */
public final class HardwareDescriptionResponse extends ADatagram {

  /**
   * The HW description packet length. From Table 7. Length is MSB - LSB =
   * 0x0055 = 85 bytes.
   */
  private static final int HARDWARE_DESCRIPTION_RESPONSE_LENGTH = 0x0055; // table 7

  // Setting values Byte Location in Data
  /**
   * The Avcom product ID.
   */
  private EAvcomProductID productId;  // 4
  /**
   * The device major firmware version.
   */
  private int firmwareVersionMajor;// 5
  /**
   * The device minor firmware version.
   */
  private int firmwareVersionMinor;// 6
  /**
   * Stream mode (single, stream, off)
   */
  private int streamMode; // 7
  /**
   * The current device center frequency configuration (MHz)
   */
  private double currentCenterFrequencyMHz; // 8-11
  /**
   * The current device frequency span (MHz)
   */
  private double currentSpanMHz;// 12-15
  /**
   * The current device reference level (dB)
   */
  private EAvcomReferenceLevel currentReferenceLevel;  // 16
  /**
   * The current device resolution bandwidth (MHz)
   */
  private EAvcomResolutionBandwidth currentRBW; // 17
  /**
   * The maximum available resolution bandwidth at the currently configured span
   * (MHz)
   */
  private EAvcomResolutionBandwidth availableRBW;  // 18
  /**
   * The current RF input (1 through 6) that the device is listening on. This is
   * to support models having a controllable RF switch before the sensor device.
   */
  private int currentRFInput;// 19
  /**
   * Available RF input (1 through 6). This is to support models having a
   * controllable RF switch before the sensor device.
   */
  private int availableRFInputs;// 20
  private int currentSplitterGainCal; // 21
  private Boolean hasSplitter;// 22
  private int currentComPort;// 23
  private int availableComPorts;// 24
  private final byte[] currentInternalExtFreq = new byte[2];// 25
  private final byte[] currentInternalExt = new byte[2];// 26
  /**
   * The product serial number.
   */
  private String serialNumber;  // 29-44 (16 bytes)
  // private byte[] serialNumber = new byte[16]; // 29-44 (16 bytes)
  private EAvcomPCBRevision pcbRevision;// 45
  private int calibrationDay;// 46
  private int calibrationMonth; // 47
  private int calibrationYear;  // 48-49
  private int boardTemperature; // 50  0x80  =  0 degrees  C
  private int boardTemperatureMin; // 51  0x80  =  0 degrees  C
  private int boardTemperatureMax; // 52  0x80  =  0 degrees  C
  // private int reserved01; // 53
  private Boolean hasLNBPower;// 54
  private byte currentLNBPowerSetting; // 55
  private int isFirstRun; // 56  (save flag?)
  private Boolean isLocked;// 57 (0xAA = Locked)
  private int projectID;  // 58
  // private int reserved02; // 59 reserved for firmware < 2.13
  // LNB settings (table 12)
  private int LNBVoltage;
  private Boolean LNBReferenceClockIsOn;
  private Boolean LNBDisablePower;
  // ----------------------------------------------------------------------------
  // Device status metrics - these are set by the AvcomSBS instance to reflect
  // current operational status
  /**
   * The maximum elapsed time (ms) from all datagram read operations since power
   * on.
   */
  private long elapsedTimeMax = Long.MIN_VALUE;
  /**
   * The minimum elapsed time (ms) from all datagram read operations since power
   * on.
   */
  private long elapsedTimeMin = Long.MAX_VALUE;
  /**
   * The average elapsed time (ms) of the previous one hundred (100) datagram
   * read operations. This is a moving average calculation.
   */
  private long elapsedTimeAve = 0;
  /**
   * The number of datagrams written to the device since power on.
   */
  private long datagramWriteCount = 0;
  /**
   * The number of GOOD datagrams read out from the device since power on.
   */
  private long datagramReadCount = 0;
  /**
   * The number of ERROR datagrams read out from the device since power on.
   */
  private long datagramErrorCount = 0;

  /**
   * Construct a new HardwareDescriptionResponse and populate the internal
   * fields with the byte array returned from the sensor.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the byte array fails to parse
   */
  public HardwareDescriptionResponse(byte[] bytes) throws Exception {
    super(EAvcomDatagram.HARDWARE_DESCRIPTION_RESPONSE);
    this.parse(bytes);
  }

  //<editor-fold defaultstate="collapsed" desc="Getter and (some) Setter Methods">
  public int getAvailableComPorts() {
    return availableComPorts;
  }

  public EAvcomResolutionBandwidth getAvailableRBW() {
    return availableRBW;
  }

  public int getAvailableRFInputs() {
    return availableRFInputs;
  }

  public int getBoardTemperature() {
    return boardTemperature;
  }

  public int getBoardTemperatureMax() {
    return boardTemperatureMax;
  }

  public int getBoardTemperatureMin() {
    return boardTemperatureMin;
  }

  public int getCalibrationDay() {
    return calibrationDay;
  }

  public int getCalibrationMonth() {
    return calibrationMonth;
  }

  public int getCalibrationYear() {
    return calibrationYear;
  }

  public Date getCalibrationDate() {
    Calendar calendar = Calendar.getInstance();
    /**
     * Calendar months begin at zero so subtract one.
     */
    calendar.set(calibrationYear, calibrationMonth - 1, calibrationDay);
    return calendar.getTime();
  }

  public double getCurrentCenterFrequencyMHz() {
    return currentCenterFrequencyMHz;
  }

  public int getCurrentComPort() {
    return currentComPort;
  }

  public byte[] getCurrentInternalExt() {
    return currentInternalExt;
  }

  public byte[] getCurrentInternalExtFreq() {
    return currentInternalExtFreq;
  }

  public byte getCurrentLNBPowerSetting() {
    return currentLNBPowerSetting;
  }

  public EAvcomResolutionBandwidth getCurrentRBW() {
    return currentRBW;
  }

  public int getCurrentRFInput() {
    return currentRFInput;
  }

  public EAvcomReferenceLevel getCurrentReferenceLevel() {
    return currentReferenceLevel;
  }

  public double getCurrentSpanMHz() {
    return currentSpanMHz;
  }

  public int getCurrentSplitterGainCal() {
    return currentSplitterGainCal;
  }

  public long getDatagramErrorCount() {
    return datagramErrorCount;
  }

  /**
   * Increment the datagram ERROR count by one.
   */
  public void setDatagramError() {
    /**
     * Reset the counter if it is approaching its limit.
     */
    if (this.datagramErrorCount > Long.MAX_VALUE - 100) {
      this.datagramErrorCount = 0;
    }
    this.datagramErrorCount += 1;
  }

  public long getDatagramReadCount() {
    return datagramReadCount;
  }

  /**
   * Increment the datagram READ count by one.
   */
  public void setDatagramRead() {
    /**
     * Reset the counter if it is approaching its limit.
     */
    if (this.datagramReadCount > Long.MAX_VALUE - 100) {
      this.datagramReadCount = 0;
    }
    this.datagramReadCount += 1;
  }

  public long getDatagramWriteCount() {
    return datagramWriteCount;
  }

  /**
   * Increment the datagram WRITE count by one.
   */
  public void setDatagramWrite() {
    /**
     * Reset the counter if it is approaching its limit.
     */
    if (this.datagramWriteCount > Long.MAX_VALUE - 100) {
      this.datagramWriteCount = 0;
    }
    this.datagramWriteCount += 1;
  }

  public long getElapsedTimeAve() {
    return elapsedTimeAve;
  }

  public long getElapsedTimeMax() {
    return elapsedTimeMax;
  }

  public long getElapsedTimeMin() {
    return elapsedTimeMin;
  }

  /**
   * Record the elapsed time (ms). This updates the internal max/min/ave values.
   * <p>
   * Developer note: The datagram READ/WRITE/ERROR counters MUST be updated
   * before calling this method as this method internally references those other
   * values.
   * <p>
   * @param elapsedTime the elapsed time of a given read operation.
   */
  public void setElapsedTime(long elapsedTime) {
    elapsedTimeMax = elapsedTimeMax > elapsedTime ? elapsedTimeMax : elapsedTime;
    elapsedTimeMin = elapsedTimeMin < elapsedTime ? elapsedTimeMin : elapsedTime;
    /**
     * If the elapsed time has not been set (==0) then use the current elapsed
     * time value.
     */
    elapsedTimeAve = ((elapsedTimeAve == 0 ? elapsedTime : elapsedTimeAve) * 99 + elapsedTime) / 100;
  }

  public int getFirmwareVersionMajor() {
    return firmwareVersionMajor;
  }

  public int getFirmwareVersionMinor() {
    return firmwareVersionMinor;
  }

  public int getIsFirstRun() {
    return isFirstRun;
  }

  public int getLNBVoltage() {
    return LNBVoltage;
  }

  public EAvcomPCBRevision getPcbRevision() {
    return pcbRevision;
  }

  public EAvcomProductID getProductId() {
    return productId;
  }

  public int getProjectID() {
    return projectID;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public int getStreamMode() {
    return streamMode;
  }

  public Boolean hasLNBPower() {
    return hasLNBPower;
  }

  public Boolean hasSplitter() {
    return hasSplitter;
  }

  public Boolean isIsLocked() {
    return isLocked;
  }

  public Boolean isLNBDisablePower() {
    return LNBDisablePower;
  }

  public Boolean isLNBReferenceClockIsOn() {
    return LNBReferenceClockIsOn;
  }//</editor-fold>

  /**
   * Get the device configuration as a sorted Map of configuration name and
   * configuration value.
   * <p>
   * @return a non-null TreeMap instance
   */
  public Map<String, String> getConfiguration() {
    Map<String, String> deviceConfiguration = new TreeMap<>();
    /**
     * Use Bean introspection to dump all bean field values into a Map using the
     * respective field name as the key.
     */
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        /**
         * Get the invoked object instance.
         */
        Method readMethod = propertyDescriptor.getReadMethod();
        /**
         * Skip if no read method for this property.
         */
        if (readMethod == null) {
          continue;
        }
        /**
         * Important: Do not call the read method for "configuration", which is
         * "getConfiguration", as this will cause an infinite recursion.
         */
        if (readMethod.getName().equals("getConfiguration")) {
          continue;
        }
        /**
         * Load the field names and their values into the Map and return.
         */
        try {
          Object objectInstance = readMethod.invoke(this, (Object[]) null);
          /**
           * Note object fields that are empty with "Not configured" to prevent
           * them being perceived as an error by the user.
           */
          deviceConfiguration.put(propertyDescriptor.getDisplayName(),
                                  (objectInstance == null ? "Not configured" : objectInstance.toString()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//          Logger.getLogger(HardwareDescriptionResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    } catch (IntrospectionException introspectionException) {
    }
    return deviceConfiguration;
  }

  /**
   * Parse the byte array returned from the sensor and use it populate internal
   * fields.
   * <p>
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the parse operation fails or encounters an
   *                             error
   */
  @Override
  public void parse(byte[] bytes) throws Exception {
    // Parse the bytes
    int i;
    /**
     * The Avcom product ID.
     */
    this.productId = EAvcomProductID.fromByteCode(bytes[4]);
    /**
     * The major firmware number.
     */
    this.firmwareVersionMajor = bytes[5];
    /**
     * The minor firmware number.
     */
    this.firmwareVersionMinor = bytes[6];
    /**
     * The device streaming mode. (single, stream, off).
     */
    this.streamMode = bytes[7];
    /**
     * The current center frequency configuration (MHz).
     */
    this.currentCenterFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 8) / 10000; // divide by 10,000 to get Mhz

    this.currentSpanMHz = ByteUtil.intFrom4Bytes(bytes, 12) / 10000; // divide by 10,000 to get Mhz
    this.currentReferenceLevel = EAvcomReferenceLevel.fromByteCode(bytes[16]);
    this.currentRBW = EAvcomResolutionBandwidth.fromByteCode(bytes[17]);
    this.availableRBW = EAvcomResolutionBandwidth.fromByteCode(bytes[18]);
    this.currentRFInput = bytes[19] - 9;
    this.availableRFInputs = bytes[20] - 9;
    this.currentSplitterGainCal = bytes[21];
    this.hasSplitter = ByteUtil.getBit(bytes[22], 0) != 0;
    this.currentComPort = bytes[23];
    this.availableComPorts = bytes[24];
    for (i = 0; i < 2; i++) {
      this.currentInternalExtFreq[i] = bytes[25 + i];
    }
    for (i = 0; i < 2; i++) {
      this.currentInternalExt[i] = bytes[27 + i];
    }
    // serial number is 16 bytes
    String serialNumTemp = "";
    for (i = 0; i < 16; i++) {
      // this.serialNumber[i] = bytes[29 + i];
      serialNumTemp += (char) bytes[29 + i];
    }
    this.serialNumber = serialNumTemp;
    // this.pcbRevision = PCBRevision.getByByteCode(bytes[45]);
    this.pcbRevision = EAvcomPCBRevision.fromByteCode(bytes[45]);
    this.calibrationDay = ByteUtil.intFromByte(bytes[46]) - 10; // per mitch
    this.calibrationMonth = ByteUtil.intFromByte(bytes[47]) - 10; // per mitch
    this.calibrationYear = Integer.valueOf(String.format("%02d", bytes[48]) + "" + String.format("%02d", bytes[49]));
    // ByteUtil.intFrom2Bytes(bytes,  48) -  3120; // per mitch
    /**
     * The current board temperature in degrees Celsius.
     */
    this.boardTemperature = ByteUtil.intFromByte(bytes[50]) - 128;    //  0x80  =  0 degrees  C
    /**
     * The lifetime minimum board temperature in degrees Celsius. This is used
     * for warranty validation.
     */
    this.boardTemperatureMin = ByteUtil.intFromByte(bytes[51]) - 128; //  0x80  =  0 degrees  C
    /**
     * The lifetime maximum board temperature in degrees Celsius. This is used
     * for warranty validation.
     */
    this.boardTemperatureMax = ByteUtil.intFromByte(bytes[52]) - 128; //  0x80  =  0 degrees  C
    // this.reserved01 = bytes[53];
    /**
     * Indicator that this device has an LNB power supply and can drive an
     * external LNB device. Sensor models built for satellite applications
     * employ an external Low-noise block downconverter (LNB).
     */
    this.hasLNBPower = ByteUtil.getBit(bytes[54], 8) != 0;
    /**
     * The current LNB power setting configuration. The byte value configuration
     * is described in table 14. If set the values are interpreted and set in
     * the fields LNBVoltage, LNBReferenceClockIsOn, LNBDisablePower of this
     * datagram.
     */
    this.currentLNBPowerSetting = bytes[55];
    this.isFirstRun = bytes[56];
    this.isLocked = bytes[57] == 0xAA;
    this.projectID = ByteUtil.intFromByte(bytes[58]);
    // this.reserved02 = bytes[59];
    // Extended - set LNB values based on bit flag settings in bytes[55]
    /**
     * The LNB output line voltage (Volts).
     */
    this.LNBVoltage = ByteUtil.getBit(bytes[55], 3) == 0 ? 13 : 18;
    /**
     * Indicator that the 22 kHz LNB reference clock is on.
     */
    this.LNBReferenceClockIsOn = ByteUtil.getBit(bytes[55], 5) != 0;
    /**
     * Indicator that the LNB power is disabled.
     */
    this.LNBDisablePower = ByteUtil.getBit(bytes[55], 2) != 0;
    // ----------------------------------------------------------------------------
    this.valid = true;
//      ByteUtil.twoByteIntFromBytes(bytes, 1) == HARDWARE_DESCRIPTION_RESPONSE_LENGTH;
  }

  /**
   * Hardware Description datagrams should not be sent to the device <br>
   * Instead, use HardwareDescriptionRequest datagrams
   * <p>
   * @return
   */
  @Override
  public byte[] serialize() {
    return null;
  }

  /**
   * A complete, multi-line output of the datagram contents.
   * <p>
   * @return
   */
  public String toStringFull() {
    // return ByteUtil.toString(datagramData, true);
    if (valid) {
      return "Hardware Description Datagram Contents"
        + "\n index   name             value"
        + "\n -----------------------------------------------"
        + "\n this.datagramType             " + datagramType
        + "\n this.isValid                  " + valid
        + "\n this.data length              " + (data != null ? data.length : "null")
        + "\n 4 productId                   " + productId
        + "\n 5 firmwareVersionMajor        " + firmwareVersionMajor
        + "\n 6 firmwareVersionMinor        " + firmwareVersionMinor
        + "\n 7 streamMode                  " + streamMode
        + "\n 8-11 currentCenterFrequency   " + currentCenterFrequencyMHz
        + "\n 12-15 currentSpan             " + currentSpanMHz
        + "\n 16 currentReferenceLevel      " + currentReferenceLevel
        + "\n 17 currentRBW                 " + currentRBW
        + "\n 18 availableRBW               " + availableRBW
        + "\n 19 currentRFInput             " + currentRFInput
        + "\n 20 availableRFInputs          " + availableRFInputs
        + "\n 21 currentSplitterGainCal     " + currentSplitterGainCal
        + "\n 22 hasSplitter                " + hasSplitter
        + "\n 23 currentComPort           0x" + Integer.toHexString(currentComPort)
        + "\n 24 availableComPorts        0x" + Integer.toHexString(availableComPorts)
        + "\n 25-26 currentInternalExtFrq:0x" + ByteUtil.toString(currentInternalExtFreq)
        + "\n 27-28 currentInternalExt    0x" + ByteUtil.toString(currentInternalExt)
        + "\n 29-44 serialNumber            " + serialNumber
        + "\n 45 pcbRevision                " + pcbRevision
        + "\n 46 calibration Day            " + calibrationDay
        + "\n 47 calibration Month          " + calibrationMonth
        + "\n 48-49 calibration Year        " + calibrationYear
        + "\n 50 boardTemperatureC          " + boardTemperature
        + "\n 51 boardTemperatureMinC       " + boardTemperatureMin
        + "\n 52 boardTemperatureMaxC       " + boardTemperatureMax
        + "\n 53 Reserved"
        + "\n 54 hasLNBPower                " + hasLNBPower
        + "\n 55 currentLNBPowerSetting     " + currentLNBPowerSetting
        + "\n 56 isFirstRun                 " + isFirstRun
        + "\n 57 isLocked                   " + isLocked
        + "\n 58 projectID                0x" + Integer.toHexString(projectID) + " = " + projectID
        //        + "\n    calibration date           " + calibrationMonth + "/" + calibrationDay + "/" + calibrationYear
        + "";
    } else {
      return "Hardware Description datagram not initialized.";
    }
  }

  /**
   * A brief output of the datagram contents.
   * <p>
   * @return
   */
  @Override
  public String toString() {
    if (valid) {
      return "HDR Product [" + productId
        + "] SN [" + serialNumber
        + "] FIRMWARE [" + firmwareVersionMajor + "." + firmwareVersionMinor
        + "] REV [" + pcbRevision
        + "] CALIBRATED [" + calibrationMonth + "/" + calibrationDay + "/" + calibrationYear
        + "]";
    } else {
      return "Hardware Description datagram not initialized.";
    }
  }

  public String toStringSettings() {
    return "HDR CF [" + currentCenterFrequencyMHz
      + "] Span [" + currentSpanMHz
      + "] RL [" + currentReferenceLevel
      + "] RBW [" + currentRBW
      + "]";
  }
}
