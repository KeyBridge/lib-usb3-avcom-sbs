package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomPCBRevision;
import com.avcomofva.sbs.enumerated.EAvcomProductID;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.util.ByteUtil;
import java.io.Serializable;

/**
 * Avcom Hardware Description Datagram Described in Table 7
 * <p>
 * @author jesse
 */
public class HardwareDescriptionResponse implements IDatagram, Serializable {

  private static final long serialVersionUID = 1L;
  // Datagram housekeeping
  // ---------------------------------------------------------
  public static final byte datagramType = IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID;
  private byte[] datagramData;
  private Boolean isValid = false;
  private int elapsedTimeMS;
  private String jobSerialNumber;
  private String sensorSerialNumber;
  // ----------------------------------------------------------------------------
  // Setting values Byte Location in Data
  private EAvcomProductID productId;                                                           // 4
  private int firmwareVersionMajor;                                                // 5
  private int firmwareVersionMinor;                                                // 6
  private int streamMode;                                                          // 7
  private double currentCenterFrequencyMHz;                                           // 8-11
  // divide
  // by
  // 10,000
  // to
  // getProductID
  // Mhz
  private double currentSpanMHz;                                                      // 12-15
  // divide
  // by
  // 10,000
  // to
  // getProductID
  // Mhz
  private EAvcomReferenceLevel currentReferenceLevel;                                               // 16
  private EAvcomResolutionBandwidth currentRBW;                                                          // 17
  private EAvcomResolutionBandwidth availableRBW;                                                        // 18
  private int currentRFInput;                                                      // 19
  private int availableRFInputs;                                                   // 20
  private int currentSplitterGainCal;                                              // 21
  private Boolean hasSplitter;                                                         // 22
  private int currentComPort;                                                      // 23
  private int availableComPorts;                                                   // 24
  private final byte[] currentInternalExtFreq = new byte[2];                              // 25
  private final byte[] currentInternalExt = new byte[2];                              // 26
  private String serialNumber;                                                        // 29-44
  // (16
  // bytes)
  // private byte[] serialNumber = new byte[16]; // 29-44 (16 bytes)
  private byte pcbRevision;                                                         // 45
  private int calibrationDay;                                                      // 46
  private int calibrationMonth;                                                    // 47
  private int calibrationYear;                                                     // 48-49
  private int boardTemperature;                                                    // 50
  // 0x80
  // =
  // 0
  // degrees
  // C
  private int boardTemperatureMin;                                                 // 51
  // 0x80
  // =
  // 0
  // degrees
  // C
  private int boardTemperatureMax;                                                 // 52
  // 0x80
  // =
  // 0
  // degrees
  // C
  // private int reserved01; // 53
  private Boolean hasLNBPower;                                                         // 54
  private byte currentLNBPowerSetting;                                              // 55
  private int isFirstRun;                                                          // 56
  // (save
  // flag?)
  private Boolean isLocked;                                                            // 57
  // (0xAA
  // =
  // Locked)
  private int projectID;                                                           // 58
  // private int reserved02; // 59 reserved for firmware < 2.13
  // LNB settings (table 12)
  private int LNBVoltage;
  private Boolean LNBReferenceClockIsOn;
  private Boolean LNBDisablePower;
  // ----------------------------------------------------------------------------
  // Setting values Device parameters
  private int maximumFrequencyResponse;
  private int minimumFrequencyResponse;
  private final int maximumFrequencySpan = 1300;
  private final int minimumFrequencySpan = 0;
  private final double minimumFrequencySpanStep = 0.001;                                    // min
  // span
  // =
  // 1
  // kHz
  // =
  // 0.001
  // MHz

  /**
   * Empty contructor for testing
   */
  public HardwareDescriptionResponse() {
  }

  /**
   * Full contructor with bytes
   * <p>
   * @param bytes
   */
  public HardwareDescriptionResponse(byte[] bytes) {
    this.isValid = this.parse(bytes);
  }

  public boolean parse(byte[] bytes) {
    // Copy the bytes
    this.datagramData = new byte[bytes.length];
    System.arraycopy(bytes, 0, datagramData, 0, bytes.length);
    // Parse the bytes
    int i;
    this.productId = EAvcomProductID.fromByteCode(bytes[4]);
    this.firmwareVersionMajor = bytes[5];
    this.firmwareVersionMinor = bytes[6];
    this.streamMode = bytes[7];
    this.currentCenterFrequencyMHz = ByteUtil.intFrom4Bytes(bytes, 8) / 10000;
    this.currentSpanMHz = ByteUtil.intFrom4Bytes(bytes, 12) / 10000;
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
    String serialNumTemp = "";
    for (i = 0; i < 16; i++) {
      // this.serialNumber[i] = bytes[29 + i];
      serialNumTemp += (char) bytes[29 + i];
    }
    this.serialNumber = serialNumTemp;
    // this.pcbRevision = PCBRevision.getByByteCode(bytes[45]);
    this.pcbRevision = bytes[45];
    this.calibrationDay = ByteUtil.intFromByte(bytes[46]) - 10; // per mitch
    this.calibrationMonth = ByteUtil.intFromByte(bytes[47]) - 10; // per mitch
    this.calibrationYear = Integer.valueOf(String.format("%02d", bytes[48]) + "" + String.format("%02d", bytes[49])); // ByteUtil.intFrom2Bytes(bytes,
    // 48)
    // -
    // 3120;
    // //
    // per
    // mitch
    this.boardTemperature = ByteUtil.intFromByte(bytes[50]) - 128;
    this.boardTemperatureMin = ByteUtil.intFromByte(bytes[51]) - 128;
    this.boardTemperatureMax = ByteUtil.intFromByte(bytes[52]) - 128;
    // this.reserved01 = bytes[53];
    this.hasLNBPower = ByteUtil.getBit(bytes[54], 8) != 0;
    this.currentLNBPowerSetting = bytes[55];
    this.isFirstRun = bytes[56];
    this.isLocked = bytes[57] == 0xAA;
    this.projectID = ByteUtil.intFromByte(bytes[58]);
    // this.reserved02 = bytes[59];
    // Extended - set LNB values based on bit flag settings in bytes[55]
    this.LNBVoltage = ByteUtil.getBit(bytes[55], 3) == 0 ? 13 : 18;
    this.LNBReferenceClockIsOn = ByteUtil.getBit(bytes[55], 5) != 0;
    this.LNBDisablePower = ByteUtil.getBit(bytes[55], 2) != 0;
    // ----------------------------------------------------------------------------
    // Set the product max/min frequency response
    if (this.productId != null) {
      switch (this.productId) {
        case RSA2150:
          this.maximumFrequencyResponse = 2250;
          this.minimumFrequencyResponse = 1;
          break;
        case RSA1100:
          this.maximumFrequencyResponse = 1150;
          this.minimumFrequencyResponse = 5;
          break;
        case RSA2500:
          this.maximumFrequencyResponse = 2500;
          this.minimumFrequencyResponse = 5;
          break;
        default:
          this.maximumFrequencyResponse = 2500;
          this.minimumFrequencyResponse = 5;
          break;
      }
    }
    // ----------------------------------------------------------------------------
    // confirm this is a valid datagram
    if (ByteUtil.twoByteIntFromBytes(bytes, 1) == IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH) {
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings("static-access")
  public byte getSensorTypeId() {
    return this.datagramType;
  }

  /**
   * Hardware Description datagrams should not be sent to the device <br>
   * Instead, use HardwareDescriptionRequest datagrams
   * <p>
   * @return
   */
  public byte[] serialize() {
    return null;
  }

  /**
   * Must override, else it calls isValid for super() and returns false (!)
   * <p>
   * @return
   */
  public Boolean isValid() {
    return this.isValid;
  }

  // ----------------------------------------------------------------------------
  // Get methods for internal parameters
  public EAvcomProductID getProductId() {
    return this.productId;
  }

  public int getFirmwareVersionMajor() {
    return this.firmwareVersionMajor;
  }

  public int getFirmwareVersionMinor() {
    return this.firmwareVersionMinor;
  }

  public int getStreamMode() {
    return this.streamMode;
  }

  public double getCurrentCenterFrequencyMHz() {
    return this.currentCenterFrequencyMHz;
  }

  public double getCurrentSpanMHz() {
    return this.currentSpanMHz;
  }

  public EAvcomReferenceLevel getCurrentReferenceLevel() {
    return this.currentReferenceLevel;
  }

  public EAvcomResolutionBandwidth getCurrentRBW() {
    return this.currentRBW;
  }

  public EAvcomResolutionBandwidth getAvailableRBW() {
    return this.availableRBW;
  }

  public int getCurrentRFInput() {
    return this.currentRFInput;
  }

  public int getAvailableRFInputs() {
    return this.availableRFInputs;
  }

  public int getCurrentSplitterGainCal() {
    return this.currentSplitterGainCal;
  }

  public Boolean getSplitterBits() {
    return this.hasSplitter;
  }

  public int getCurrentComPort() {
    return this.currentComPort;
  }

  public int getAvailableComPorts() {
    return this.availableComPorts;
  }

  public byte[] getCurrentInternalExtFreq() {
    return this.currentInternalExtFreq;
  }

  public byte[] getCurrentInternalExt() {
    return this.currentInternalExt;
  }

  public String getSerialNumber() {
    return this.serialNumber;
  }

  public String getPcbRevision() {
    return EAvcomPCBRevision.labelFromBytecode(this.pcbRevision);
  }

  public int getCalibrationDay() {
    return this.calibrationDay;
  }

  public int getCalibrationMonth() {
    return this.calibrationMonth;
  }

  public int getCalibrationYear() {
    return this.calibrationYear;
  }

  public int getBoardTemperature() {
    return this.boardTemperature;
  }

  public int getBoardTemperatureMin() {
    return this.boardTemperatureMin;
  }

  public int getBoardTemperatureMax() {
    return this.boardTemperatureMax;
  }

  public Boolean getHasLNBPower() {
    return this.hasLNBPower;
  }

  public int getCurrentLNBPowerSetting() {
    return this.currentLNBPowerSetting;
  }

  public int getIsFirstRun() {
    return this.isFirstRun;
  }

  public Boolean getIsLocked() {
    return this.isLocked;
  }

  public int getProjectID() {
    return this.projectID;
  }

  public int getLNBVoltage() {
    return this.LNBVoltage;
  }

  public Boolean getLNBReferenceClockIsOn() {
    return this.LNBReferenceClockIsOn;
  }

  public Boolean getLNBDisablePower() {
    return this.LNBDisablePower;
  }

  public int getMaximumFrequencyResponse() {
    return this.maximumFrequencyResponse;
  }

  public int getMinimumFrequencyResponse() {
    return this.minimumFrequencyResponse;
  }

  public int getMaximumFrequencySpan() {
    return this.maximumFrequencySpan;
  }

  public int getMinimumFrequencySpan() {
    return this.minimumFrequencySpan;
  }

  public double getMinimumFrequencySpanStep() {
    return this.minimumFrequencySpanStep;
  }

  public String getCalibrationDate() {
    return calibrationMonth + "/" + calibrationDay + "/" + calibrationYear;
  }

  /**
   * Get the time required to create this datagram
   * <p>
   * @return
   */
  public int getElapsedTimeMS() {
    return this.elapsedTimeMS;
  }

  public byte[] getData() {
    return this.datagramData;
  }

  public String getTransactionId() {
    return this.jobSerialNumber;
  }

  public void setTransactionId(String serialNumber) {
    this.jobSerialNumber = serialNumber;
  }

  public String getSensorSerialNumber() {
    return this.sensorSerialNumber;
  }

  public void setSensorSerialNumber(String sensorSerialNumber) {
    this.sensorSerialNumber = sensorSerialNumber;
  }

  /**
   * Set the time required to create this datagram
   * <p>
   * @param elapsedTimeMS
   */
  public void setElapsedTimeMS(int elapsedTimeMS) {
    this.elapsedTimeMS = elapsedTimeMS;
  }

  public String toStringBrief() {
    if (isValid) {
      return "HDR: CF [" + currentCenterFrequencyMHz
        + "] Span [" + currentSpanMHz
        + "] RL [" + currentReferenceLevel
        + "] RBW [" + currentRBW + "]";
    } else {
      return "Hardware Description datagram not initialized.";
    }
  }

  @Override
  public String toString() {
    // return ByteUtil.toString(datagramData, true);
    if (isValid) {
      return "Hardware Description Datagram Contents"
        + "\n index   name             value"
        + "\n --------------------------------"
        + "\n this.datagramType:          " + datagramType
        + "\n this.isValid:               " + isValid
        + "\n this.datagramData length:   " + datagramData.length
        + "\n 4 productId:                " + productId
        + "\n 5 firmwareVersionMajor:     " + firmwareVersionMajor
        + "\n 6 firmwareVersionMinor:     " + firmwareVersionMinor
        + "\n 7 streamMode:               " + streamMode
        + "\n 8-11 currentCenterFrequency:" + currentCenterFrequencyMHz
        + "\n 12-15 currentSpan:          " + currentSpanMHz
        + "\n 16 currentReferenceLevel:   " + currentReferenceLevel
        + "\n 17 currentRBW:              " + currentRBW
        + "\n 18 availableRBW:            " + availableRBW
        + "\n 19 currentRFInput:          " + currentRFInput
        + "\n 20 availableRFInputs:       " + availableRFInputs
        + "\n 21 currentSplitterGainCal:  " + currentSplitterGainCal
        + "\n 22 hasSplitter :            " + hasSplitter
        + "\n 23 currentComPort:          0x" + Integer.toHexString(currentComPort)
        + "\n 24 availableComPorts:       0x" + Integer.toHexString(availableComPorts)
        + "\n 25-26 currentInternalExtFreq:0x" + ByteUtil.toString(currentInternalExtFreq)
        + "\n 27-28 currentInternalExt:   0x" + ByteUtil.toString(currentInternalExt)
        // + "\n serialNumber : " + ByteUtil.toString(serialNumber)
        + "\n 29-44 serialNumber:         " + serialNumber
        + "\n 45 pcbRevision:             " + pcbRevision
        + "\n 46 calibration Day:         " + calibrationDay
        + "\n 47 calibration Month:       " + calibrationMonth
        + "\n 48-49 calibration Year:     " + calibrationYear
        + "\n 50 boardTemperatureC:       " + boardTemperature
        + "\n 51 boardTemperatureMinC:    " + boardTemperatureMin
        + "\n 52 boardTemperatureMaxC:    " + boardTemperatureMax
        + "\n 53 Reserved: " + "\n 54 hasLNBPower:             " + hasLNBPower
        + "\n 55 currentLNBPowerSetting:  " + currentLNBPowerSetting
        + "\n 56 isFirstRun:              " + isFirstRun
        + "\n 57 isLocked:                " + isLocked
        + "\n 58 projectID:               0x" + Integer.toHexString(projectID) + " = " + projectID
        + "\n    maximum frequency resp:  " + maximumFrequencyResponse
        + "\n    minimum frequency resp:  " + minimumFrequencyResponse
        + "\n    calibration date:        " + getCalibrationDate();
    } else {
      return "Hardware Description datagram not initialized.";
    }
  }

  // ----------------------------------------------------------------------------
  /**
   * scratchbox
   * <p>
   * @param args
   */
  public static void main(String[] args) {
    // Construct a bogus HW data stream
    byte[] msg = new byte[IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH];
    for (int i = 0; i < IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH; i++) {
      msg[i] = (byte) i;
    }
    msg[0] = IDatagram.FLAG_STX;
    msg[1] = 0;
    msg[2] = 0x55;
    msg[3] = IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID;
    msg[50] = (byte) 0x81;
    msg[IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH - 1] = IDatagram.FLAG_ETX;
    // byte[] msg = new byte[] {0x02, 0x00, 0x55, 0x07, 0x5a, 0x02, 0xc, 0x00,
    // 0x00, 0x04, 0x01, 0xa0, 0x00, 0x0f, 0x42, 0x40, 0x15, 0x40, 0x08, 0x0a,
    // 0x0b, 0x00, 0x00, 0x08, 0x0d, 0x00, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30,
    // 0x30, 0x30, 0x30, 0x30, 0x30, 0x33, 0x30, 0x39, 0x30, 0x31, 0x30, 0x30,
    // 0x34, 0x1b, 0x21, 0x10, 0x14, 0x09, 0xab, 0x94, 0xae, 0x0, 0x00, 0x40,
    // 0x3f, 0xaa, 0xff, 0xff, 0xe8, 0xb1, 0x82, 0x67, 0x95, 0x88, 0x7c, 0x63,
    // 0xb5, 0x80, 0x9e, 0xff, 0xd1, 0xff, 0xff, 0xff, 0x1e, 0x1a, 0x1c, 0x23,
    // 0x37, 0x28, 0xb4, 0x9e, 0x00, 0x08, 0xff, 0x03};
    // System.out.println("bytes " + ByteUtil.toString(msg));
    // HardwareDescriptionResponse h = new HardwareDescriptionResponse(msg);
    // System.out.println(h.toString());
    System.out.println("--> try the factory");
    HardwareDescriptionResponse d = (HardwareDescriptionResponse) DatagramFactory.createDatagram(msg);
    System.out.println("<-- back from the factory ");
    System.out.println(d.toString());
    // System.out.println("valid: " + d.isValid());
    // System.out.println("type: " + d.getSensorTypeId());
    // byte[] sn = d.getSerialNumber();
    // String s = "";
    // for(int i = 0 ; i < sn.length; i++){
    // s += sn[i]+"";
    // }System.out.println("serial : " +s);
    int pre = 20;
    int post = 9;
    System.out.println(String.format("%02d%02d", pre, post));
  }
}
