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
package com.avcomofva.sbs.datagram.read;

import com.avcomfova.sbs.datagram.ADatagram;
import com.avcomofva.sbs.enumerated.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.usb.utility.ByteUtility;

/**
 * Avcom SBS Hardware Description Datagram.
 * <p>
 * This contains basic configuration information about the analyzer such as
 * Product ID, firmware revision, current Center Frequency, Span, etc.
 * <p>
 * (Table 7).
 *
 * @author Jesse Caulfield
 */
public final class HardwareDescriptionResponse extends ADatagram {

  /**
   * The Datagram type.
   */
  private static final EDatagramType TYPE = EDatagramType.HARDWARE_DESCRIPTION_RESPONSE;
  /**
   * The HW description packet length. (Table 7.)
   * <p>
   * Length is MSB - LSB = 0x0055 = 85 bytes.
   */
  private static final int DATAGRAM_LENGTH = 0x0055;

  /**
   * The Avcom product ID. Byte #4.
   */
  private EProductID productId;
  /**
   * The device major firmware version. Byte #5.
   */
  private int firmwareVersionMajor;
  /**
   * The device minor firmware version. Byte #6.
   */
  private int firmwareVersionMinor;
  /**
   * Stream mode (single, stream, off). Byte #7.
   */
  private int streamMode;
  /**
   * The current device center frequency configuration (MHz) Byte #8-11.
   */
  private double currentCenterFrequency;
  /**
   * The current device frequency span (MHz). Byte #12-15.
   */
  private double currentSpan;
  /**
   * The current device reference level (dB). Byte #16.
   */
  private EReferenceLevel currentReferenceLevel;
  /**
   * The current device resolution bandwidth (MHz). Byte #17.
   */
  private EResolutionBandwidth currentRBW;
  /**
   * The maximum available resolution bandwidth at the currently configured span
   * (MHz). Byte #18.
   */
  private EResolutionBandwidth availableRBW;
  /**
   * The current RF input (1 through 6) that the device is listening on. Byte
   * #19.
   * <p>
   * This is to support models having a controllable RF switch before the sensor
   * device.
   */
  private int currentRFInput;
  /**
   * Available RF input (1 through 6). Byte #20.
   * <p>
   * This is to support models having a controllable RF switch before the sensor
   * device.
   */
  private int availableRFInputs;
  /**
   * The current splitter gain calibration value. Byte #21.
   */
  private int currentSplitterGainCal;
  /**
   * Indicator that this device has a splitter. Byte #22.
   * <p>
   * See Table 7, Byte #22 for bit-encoded values.
   */
  private Boolean splitter;
  /**
   * The current communications port (USB or Ethernet). Byte #23.
   */
  private int currentComPort;
  /**
   * Available communications ports (USB or Ethernet). Byte #24.
   */
  private int availableComPorts;
  /**
   * The current internal extender frequency. (MSB-LSB) Bytes #25-26.
   */
  private final byte[] currentInternalExtFreq = new byte[2];
  /**
   * The current internal extender. (MSB-LSB) Bytes #27-28.
   */
  private final byte[] currentInternalExt = new byte[2];// 26
  /**
   * The product serial number. Byte #29-44 (16 bytes)
   */
  private String serialNumber;
  /**
   * The PCB revision. Byte #45.
   */
  private EPCBRevision pcbRevision;
  /**
   * The calibration date. This is assembled from the day (Byte #46), month
   * (Byte #47) and year (Byte #48-49) values..
   */
  private Date calibrationDate;

  /**
   * The current board temperature (Centigrade). Byte #50.
   * <p>
   * 0x80 = 0 degrees C
   */
  private int boardTemperature;
  /**
   * The minimum detected board temperature (Centigrade). Byte #51.
   */
  private int boardTemperatureMin;
  /**
   * The maximum detected board temperature (Centigrade). Byte #51.
   */
  private int boardTemperatureMax;
  // private int reserved01; // 53
  /**
   * Indicator that this device supplied LNB power. See Table 7, Byte 54 for bit
   * encoding details.
   */
  private Boolean lnbPower;
  /**
   * The current LNB power setting. Byte #55. See Table 14 for bit encoding
   * details.
   */
  private byte currentLNBPowerSetting;
  /**
   * Indicator that this device is fresh out of the box. Byte # 56.
   */
  private int isFirstRun;
  /**
   * Indicator that the configuration is locked. Byte #57. (0xAA = Locked)
   */
  private Boolean isLocked;
  /**
   * The manufacturer development project ID. This is used only for technical
   * support and RMA. Byte #58.
   */
  private int projectID;
  // private int reserved02; // 59 reserved for firmware < 2.13

  //<editor-fold defaultstate="collapsed" desc="LNB Power Settings">
  /**
   * The LNB voltage value. Read from {@link #currentLNBPowerSetting} Byte #55
   * and parsed according to Table 14.
   */
  private int LNBVoltage;
  /**
   * Indicator that the LNB reference clock is ON or OFF. Read from
   * {@link #currentLNBPowerSetting} Byte #55 and parsed according to Table 14.
   */
  private Boolean LNBReferenceClockIsOn;
  /**
   * Indicator that the LNB power is disabled. Read from
   * {@link #currentLNBPowerSetting} Byte #55 and parsed according to Table 14.
   */
  private Boolean LNBDisablePower;//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Device Status Metrics">
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
  private long datagramErrorCount = 0;//</editor-fold>

  /**
   * Construct a new HardwareDescriptionResponse and populate the internal
   * fields with the byte array returned from the sensor.
   *
   * @param bytes the byte array returned from the sensor
   * @throws java.lang.Exception if the byte array fails to parse
   */
  public HardwareDescriptionResponse(byte[] bytes) throws Exception {
    super(EDatagramType.HARDWARE_DESCRIPTION_RESPONSE);
    this.parse(bytes);
  }

  //<editor-fold defaultstate="collapsed" desc="Getter and (some) Setter Methods">
  public int getAvailableComPorts() {
    return availableComPorts;
  }

  public EResolutionBandwidth getAvailableRBW() {
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

  public Date getCalibrationDate() {
    return calibrationDate;
  }

  public double getCurrentCenterFrequency() {
    return currentCenterFrequency;
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

  public EResolutionBandwidth getCurrentRBW() {
    return currentRBW;
  }

  public int getCurrentRFInput() {
    return currentRFInput;
  }

  public EReferenceLevel getCurrentReferenceLevel() {
    return currentReferenceLevel;
  }

  public double getCurrentSpan() {
    return currentSpan;
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
   *
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

  public EPCBRevision getPcbRevision() {
    return pcbRevision;
  }

  public EProductID getProductId() {
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
    return lnbPower;
  }

  public Boolean hasSplitter() {
    return splitter;
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
   *
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
   *
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
    this.productId = EProductID.fromByteCode(bytes[4]);
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
     * The current center frequency configuration (MHz). Divide by 10,000 to
     * convert to Mhz
     */
    this.currentCenterFrequency = ByteUtility.intFrom4Bytes(bytes, 8) / 10000; // divide by 10,000 to get Mhz
    /**
     * The current span. Divide by 10,000 to convert to Mhz.
     */
    this.currentSpan = ByteUtility.intFrom4Bytes(bytes, 12) / 10000;
    this.currentReferenceLevel = EReferenceLevel.fromByteCode(bytes[16]);
    this.currentRBW = EResolutionBandwidth.fromByteCode(bytes[17]);
    this.availableRBW = EResolutionBandwidth.fromByteCode(bytes[18]);
    this.currentRFInput = bytes[19] - 9;
    this.availableRFInputs = bytes[20] - 9;
    this.currentSplitterGainCal = bytes[21];
    this.splitter = ByteUtility.getBit(bytes[22], 0) != 0;
    this.currentComPort = bytes[23];
    this.availableComPorts = bytes[24];
    for (i = 0; i < 2; i++) {
      this.currentInternalExtFreq[i] = bytes[25 + i];
    }
    for (i = 0; i < 2; i++) {
      this.currentInternalExt[i] = bytes[27 + i];
    }
    /**
     * Serial number is a 16 byte String. Copy the range from 29 to 44 (=45
     * exclusive).
     */
    this.serialNumber = new String(Arrays.copyOfRange(bytes, 29, 45));
//    String serialNumTemp = "";    for (i = 0; i < 16; i++) {      serialNumTemp += (char) bytes[29 + i];    }    this.serialNumber = serialNumTemp;
    this.pcbRevision = EPCBRevision.fromByteCode(bytes[45]);
    /**
     * Mitch: Calibration Day and Month are up-shifted by 10 in the hardware.
     * Calendar months begin at zero so subtract one.
     */
    int calibrationDay = ByteUtility.intFromByte(bytes[46]) - 10; // per mitch
    int calibrationMonth = ByteUtility.intFromByte(bytes[47]) - 10; // per mitch
    int calibrationYear = Integer.valueOf(String.format("%02d", bytes[48]) + "" + String.format("%02d", bytes[49]));
    Calendar calibration = Calendar.getInstance();
    calibration.set(calibrationYear, calibrationMonth - 1, calibrationDay);
    this.calibrationDate = calibration.getTime();
    // ByteUtility.intFrom2Bytes(bytes,  48) -  3120; // per mitch
    /**
     * The current board temperature in degrees Celsius. 0x80 = 0 degrees C.
     */
    this.boardTemperature = ByteUtility.intFromByte(bytes[50]) - 128;
    /**
     * The lifetime minimum board temperature in degrees Celsius. 0x80 = 0
     * degrees C. This is used for warranty validation.
     */
    this.boardTemperatureMin = ByteUtility.intFromByte(bytes[51]) - 128;
    /**
     * The lifetime maximum board temperature in degrees Celsius. 0x80 = 0
     * degrees C. This is used for warranty validation.
     */
    this.boardTemperatureMax = ByteUtility.intFromByte(bytes[52]) - 128;
    // this.reserved01 = bytes[53];
    /**
     * Indicator that this device has an LNB power supply and can drive an
     * external LNB device. Sensor models built for satellite applications
     * employ an external Low-noise block downconverter (LNB).
     */
    this.lnbPower = ByteUtility.getBit(bytes[54], 8) != 0;
    /**
     * The current LNB power setting configuration. The byte value configuration
     * is described in table 14. If set the values are interpreted and set in
     * the fields LNBVoltage, LNBReferenceClockIsOn, LNBDisablePower of this
     * datagram.
     */
    this.currentLNBPowerSetting = bytes[55];
    this.isFirstRun = bytes[56];
    this.isLocked = bytes[57] == 0xAA;
    this.projectID = ByteUtility.intFromByte(bytes[58]);
    // this.reserved02 = bytes[59];
    // Extended - set LNB values based on bit flag settings in bytes[55]
    /**
     * The LNB output line voltage (Volts).
     */
    this.LNBVoltage = ByteUtility.getBit(bytes[55], 3) == 0 ? 13 : 18;
    /**
     * Indicator that the 22 kHz LNB reference clock is on.
     */
    this.LNBReferenceClockIsOn = ByteUtility.getBit(bytes[55], 5) != 0;
    /**
     * Indicator that the LNB power is disabled.
     */
    this.LNBDisablePower = ByteUtility.getBit(bytes[55], 2) != 0;
    // ----------------------------------------------------------------------------
    this.valid = true;
//      ByteUtility.twoByteIntFromBytes(bytes, 1) == HARDWARE_DESCRIPTION_RESPONSE_LENGTH;
  }

  /**
   * Hardware Description datagrams should not be sent to the device <br>
   * Instead, use HardwareDescriptionRequest datagrams
   *
   * @return
   */
  @Override
  public byte[] serialize() {
    return null;
  }

  /**
   * A complete, multi-line output of the datagram contents.
   *
   * @return
   */
  public String toStringFull() {
    // return ByteUtility.toString(datagramData, true);
    if (valid) {
      return "Hardware Description Datagram Contents"
             + "\n index   name             value"
             + "\n -----------------------------------------------"
             + "\n this.datagramType             " + type
             + "\n this.isValid                  " + valid
             + "\n this.data length              " + (data != null ? data.length : "null")
             + "\n 4 productId                   " + productId
             + "\n 5 firmwareVersionMajor        " + firmwareVersionMajor
             + "\n 6 firmwareVersionMinor        " + firmwareVersionMinor
             + "\n 7 streamMode                  " + streamMode
             + "\n 8-11 currentCenterFrequency   " + currentCenterFrequency
             + "\n 12-15 currentSpan             " + currentSpan
             + "\n 16 currentReferenceLevel      " + currentReferenceLevel
             + "\n 17 currentRBW                 " + currentRBW
             + "\n 18 availableRBW               " + availableRBW
             + "\n 19 currentRFInput             " + currentRFInput
             + "\n 20 availableRFInputs          " + availableRFInputs
             + "\n 21 currentSplitterGainCal     " + currentSplitterGainCal
             + "\n 22 hasSplitter                " + splitter
             + "\n 23 currentComPort           0x" + Integer.toHexString(currentComPort)
             + "\n 24 availableComPorts        0x" + Integer.toHexString(availableComPorts)
             + "\n 25-26 currentInternalExtFrq:0x" + ByteUtility.toString(currentInternalExtFreq)
             + "\n 27-28 currentInternalExt    0x" + ByteUtility.toString(currentInternalExt)
             + "\n 29-44 serialNumber            " + serialNumber
             + "\n 45 pcbRevision                " + pcbRevision
             + "\n 46 calibration Date           " + new SimpleDateFormat("dd/MM/yyyy").format(calibrationDate)
             + "\n 50 boardTemperatureC          " + boardTemperature
             + "\n 51 boardTemperatureMinC       " + boardTemperatureMin
             + "\n 52 boardTemperatureMaxC       " + boardTemperatureMax
             + "\n 53 Reserved"
             + "\n 54 hasLNBPower                " + lnbPower
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
   *
   * @return
   */
  @Override
  public String toString() {
    if (valid) {
      return "HDR Product [" + productId
             + "] SN [" + serialNumber
             + "] FIRMWARE [" + firmwareVersionMajor + "." + firmwareVersionMinor
             + "] REV [" + pcbRevision
             + "] CALIBRATED [" + new SimpleDateFormat("dd/MM/yyyy").format(calibrationDate)
             + "]";
    } else {
      return "Hardware Description datagram not initialized.";
    }
  }

  public String toStringSettings() {
    return "HDR CF [" + currentCenterFrequency
           + "] Span [" + currentSpan
           + "] RL [" + currentReferenceLevel
           + "] RBW [" + currentRBW
           + "]";
  }
}
