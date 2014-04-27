package com.keybridgeglobal.sensor.avcom.adapter;

import com.keybridgeglobal.sensor.avcom.datagram.HardwareDescriptionRequest;
import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.avcom.datagram.*;
import com.avcomofva.sbs.enumerated.EAvcomPCBRevision;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.interfaces.IDatagramListener;
import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.interfaces.IDeviceAdapter;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.Debug;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jesse
 */
public class DummyPortAdapter implements IDeviceAdapter {

  // Listeners
  private final List<IDatagramListener> datagramListeners = new ArrayList<>();
  private final Debug d = new Debug(false);
  private int iter = 1;                                          // for stepping the sine wave
  private Boolean addNoise = false;                                      // for adding random noise
  private Boolean isConnected = true;
  HardwareDescriptionResponse hdr;
  SettingsRequest sr;
  // How fast should we run
  private static final int HW_DELAY_MS = 4;                                          // pick ~10 ms to match the typical performance of an Avcom sensor
  private static final int WAVEFORM_DELAY_MS = 10;                                         // pick ~100 ms to match the typical performance of an Avcom sensor

  public DummyPortAdapter(Boolean addNoise) {
    d.out(this, "DummyPortAdapter");
    this.addNoise = addNoise;
    this.initialize();
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        System.out.println("Shutdown Hook:\t DummyPortAdapter");
        disconnect();
      }
    });

  }

  public DummyPortAdapter() {
    //    d.out(this, "DummyPortAdapter");
    //    this.addNoise = false;
    //    this.initialize();
    //    Runtime.getRuntime().addShutdownHook(new Thread() {
    //
    //      @Override
    //      public void run() {
    //        System.out.println("Shutdown Hook:\t DummyPortAdapter 2");
    //        disconnect();
    //      }
    //    });
    new DummyPortAdapter(false);
  }

  /**
   * Initialize the Dummy port adapter values so it's ready to run without
   * manual intialization
   */
  private void initialize() {
    d.out(this, "initialize");
    // Initialize thyself
    sr = new SettingsRequest();
    sr.setCenterFrequencyMHz(1000);
    sr.setSpanMHz(500);
    sr.setResolutionBandwidth(EAvcomResolutionBandwidth.THREE_HUNDRED_KHZ);
    sr.setReferenceLevel(EAvcomReferenceLevel.MINUS_10);
    write(sr);
  }

  private void notifyListeners(IDatagram datagram) {
    if (datagramListeners != null) {
      for (IDatagramListener l : datagramListeners) {
        d.out(this, "notifyListeners " + l.getClass().getSimpleName());
        l.onDatagram(datagram);
      }
    }
  }

  /**
   * Connect - always returns true
   * <p>
   * @param port
   * @return
   */
  public Boolean connect() {
    d.out(this, "Connect");
    this.isConnected = true;
    //    t = new Thread(this);
    //    t.start();
    return this.isConnected;
  }

  /**
   * Disconnect - always returns true
   * <p>
   * @return
   */
  public Boolean disconnect() {
    //    try {
    d.out(this, "disconnect");
    this.isConnected = false;
    //      t.join();
    //    } catch (InterruptedException ex) {
    //    }
    return true;
  }

  /**
   * Running detector thread This may be necessary to notify events
   */
  //  public void run() {
  //    while(isConnected){
  //      try {
  //        Thread.sleep(Integer.MAX_VALUE);  // Sleep forever - no action to take
  ////        d.out(this,"DUMMY sleeping");
  //      } catch (InterruptedException ex) {
  //      }
  //    }
  //  }
  /**
   * Is connected ?
   * <p>
   * @return
   */
  public Boolean isConnected() {
    return this.isConnected;
  }

  /**
   * Create a well formed HW description response with values populated by the
   * SettingsRequest datagram we received earlier
   * <p>
   * @return
   */
  private byte[] getHWDBytes() {
    byte[] msg = new byte[IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH];
    for (int i = 0; i < IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH; i++) {
      msg[i] = (byte) i;
    }
    msg[0] = IDatagram.FLAG_STX;
    msg[1] = 0;
    msg[2] = 0x55;
    msg[3] = IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID;
    msg[4] = 0x1a; // productID = DUMMY
    // Copy the settings parameters
    ByteUtil.intToBytes((int) (sr.getCenterFrequencyMHz() * 10000), msg, 8);
    ByteUtil.intToBytes((int) (sr.getSpanMHz() * 10000), msg, 12);
    msg[16] = (byte) sr.getReferenceLevel().getByteCode();
    msg[17] = (byte) sr.getResolutionBandwidth().getByteCode();
    // Set the serial number to "DUMMY"
    // For ASCII codes, see: http://www.yampe.com/linux/fedora/images/ascii.gif
    for (int i = 0; i < 16; i++) {
      msg[29 + i] = 0;
    }
    msg[40] = 0x44;
    msg[41] = 0x55;
    msg[42] = 0x4d;
    msg[43] = 0x4d;
    msg[44] = 0x59;

    msg[45] = EAvcomPCBRevision.DUMMY.getByteCode();
    msg[46] = 01; // day 2010
    msg[46] = 01; // month
    msg[48] = 20; // year
    msg[49] = 10; // year

    // Set the board temp to zero
    msg[50] = (byte) 0x80;
    msg[51] = (byte) 0x80;
    msg[52] = (byte) 0x80;

    msg[54] = 0;
    msg[55] = 0;
    msg[56] = 0;
    msg[57] = 0;
    msg[58] = 0;

    msg[IDatagram.HARDWARE_DESCRIPTION_RESPONSE_LENGTH - 1] = IDatagram.FLAG_ETX;

    //    System.out.println(ByteUtil.toString(msg));
    return msg;
  }

  /**
   * Add a Datagram Listener to receive datagrams when ready
   * <p>
   * @param datagramListener
   * @return true if successful
   */
  public void addDatagramListener(IDatagramListener datagramListener) {
    datagramListeners.add(datagramListener);
  }

  /**
   * Remove a DatagramListener
   * <p>
   * @param datagramListener
   * @return true if successful
   */
  public void removeDatagramListener(IDatagramListener datagramListener) {
    datagramListeners.remove(datagramListener);
  }

  /**
   * Write a datagram to the Dummy Port Adapter
   * <p>
   * @param datagram
   */
  public void write(IDatagram datagram) {
    //    d.out(this, "write " + datagram.getSensorTypeId());

    switch (datagram.getSensorTypeId()) {
      case IDatagram.SETTINGS_REQUEST_ID:
        //        d.out(this, "write: Settings " + s.toStringBrief());
        this.sr = (SettingsRequest) datagram;
        this.hdr = new HardwareDescriptionResponse(getHWDBytes());
        // No action
        break;
      case IDatagram.HARDWARE_DESCRIPTION_REQUEST_ID:
        //        d.out(this, "write: " + hdr.toStringBrief());
        try {
          Thread.sleep(HW_DELAY_MS);
        } catch (InterruptedException ex) {
        }
        notifyListeners(hdr);
        break;
      case IDatagram.WAVEFORM_REQUEST_ID:
        try {
          Thread.sleep(WAVEFORM_DELAY_MS);
        } catch (InterruptedException ex) {
        }
        // Write the datagram data
        WaveformResponse wr = (WaveformResponse) DatagramFactory.createDatagram(generateWaveformResponse());
        //        d.out(this, "write: " + wr.toStringBrief());
        notifyListeners(wr);
        break;
      default:
      // no op - we should send an error datagram
    }
  }

  public String getCommPortName() {
    return "/dev/DUMMY";
  }

  /**
   * Generate a byte string that represents a properly formatted
   * WaveformResponse datagram
   * <p>
   * @return
   */
  private byte[] generateWaveformResponse() {
    byte[] wrheader = new byte[]{2, 1, 0x55, 9};
    byte[] wrfooter = new byte[]{(0x5a & 0xff), 0, 0x50, (0xdf & 0x0000000f), 0x20, 0, 4, (0xe2 & 0x000000f), 0, 0x32, 0x10, 0xa, 0, 0, 0, 0, 40,
                                 (0xff & 0x000000f), (0xff & 0x000000f), 3, 0};
    byte[] waveformData = new byte[IDatagram.TRACE_DATA_LENGTH];
    byte[] datagramBytes = new byte[wrheader.length + wrfooter.length + waveformData.length];
    // Generate the signal data
    waveformData = generateSignalData(getIter());
    // Populate the byte array
    System.arraycopy(wrheader, 0, datagramBytes, 0, wrheader.length);
    System.arraycopy(waveformData, 0, datagramBytes, wrheader.length, waveformData.length);
    System.arraycopy(wrfooter, 0, datagramBytes, (wrheader.length + waveformData.length), wrfooter.length);
    // Set the productID = DUMMY
    datagramBytes[324] = 0x1a;
    // Copy the settings parameters into the waveform
    ByteUtil.intToBytes((int) (sr.getCenterFrequencyMHz() * 10000), datagramBytes, 325);
    ByteUtil.intToBytes((int) (sr.getSpanMHz() * 10000), datagramBytes, 329);
    datagramBytes[333] = (byte) sr.getReferenceLevel().getByteCode();
    datagramBytes[334] = (byte) sr.getResolutionBandwidth().getByteCode();
    // Return the correctly formatted WaveformResponse datagram
    return datagramBytes;
  }

  /**
   * Generate random noise
   */
  //  private void generateRandomNoiseData() {
  //    Random r = new Random();
  //    for (int i = 0; i < DATA_LENGTH; i++) {
  //      waveformData[i] = (byte) (16 + (r.nextInt() % 20));
  //    }
  //    int len = waveformData.length + 1;
  //    int idx = 0;
  //    byte[] bytes = new byte[len + 3];
  //    bytes[idx++] = 2;
  //    bytes[idx++] = (byte) ((waveformData.length >> 8) & 0x000000ff);
  //    bytes[idx++] = (byte) ((waveformData.length) & 0x000000ff);
  ////    bytes[idx++] = WaveformRequest.TYPE_ID;
  //  }
  public int getNoise() {
    Random r = new Random();
    return (r.nextInt() % 128) / 32;
  }

  /**
   * Create a stepping sin wave with one period for the entire span
   */
  private byte[] generateSignalData(int offset) {
    byte[] signalData = new byte[IDatagram.TRACE_DATA_LENGTH];
    for (int i = 0; i < IDatagram.TRACE_DATA_LENGTH; i++) {
      //      waveformData[i] = (byte) (Math.sin((2 * Math.PI * i / 320) + (2 * Math.PI * offset / 320)) * 64 + 128);  // the 128 increases amplitude & + 128 shifts values to positive
      signalData[i] = (byte) (Math.sin((2 * Math.PI * i / 320) - Math.PI / 2 + (2 * Math.PI * offset / 320)) * 25 + 85); // the 128 increases amplitude & + 128 shifts values to positive
      // Add noise if requested
      if (addNoise) {
        signalData[i] += getNoise();
      }
    }
    return signalData;
  }

  public int getIter() {
    if (this.iter++ > Integer.MAX_VALUE) {
      iter = 1;
    }
    return iter % IDatagram.TRACE_DATA_LENGTH;
  }

  //  public void setIter(int i) {
  //    this.iter = i % Datagram.TRACE_DATA_LENGTH;
  //  }
  public void addNoise(Boolean addNoise) {
    this.addNoise = addNoise;
  }

  //------------------------------------------------------------------------------
  public static void main(String[] args) {
    DummyPortAdapter da = new DummyPortAdapter();

    System.out.println("name: " + da.getCommPortName());

    //    da.addNoise(true);
    da.write(new HardwareDescriptionRequest());
    SettingsRequest sr = new SettingsRequest();
    sr.setCenterFrequencyMHz(1200);
    sr.setSpanMHz(600);
    sr.setResolutionBandwidth(EAvcomResolutionBandwidth.ONE_HUNDRED_KHZ);
    sr.setReferenceLevel(EAvcomReferenceLevel.MINUS_50);
    da.write(sr);
    da.write(new HardwareDescriptionRequest());

    for (int i = 0; i < 10; i++) {
      sr.setCenterFrequencyMHz(1000 + i * 10);
      sr.setSpanMHz(100 + i * 10);
      da.write(sr);
      da.write(new WaveformRequest());
    }

    System.out.println(da.hdr.toString());

    //--------------------------------------------------------------------------
    // junk foo
    // HW Response
    //2 0 55 7 5a 2 c 0 0 50 df 20 0 4 e2 0 32 10 f8 a b 0 0 8 d 0 0 0 0 30 30 30 30 30 30 30 30 33 30 39 30 31 30 30 34 1b 21 10 14 9 ab 94 ae 0 0 40 3f aa ff ff e8 b1 82 67 95 88 7c 63 b5 80 9e ff d1 ff ff ff 1e 1a 1c 23 37 28 b4 9e 0 8 ff 3 0
    // Waveforms
    //2 1 55 9 10 14 13 13 10 10 11 10 13 12 11 12 12 12 11 12 12 10 13 13 11 12 12 12 10 12 10 11 11 11 11 12 11 10 11 12 14 10 10 10 10 12 12 13 12 12 11 12 11 12 11 11 11 10 11 14 13 12 12 10 14 12 12 11 14 12 14 13 14 12 11 12 14 13 13 12 12 12 11 14 13 13 14 12 12 13 12 13 13 12 15 13 12 12 12 11 12 13 13 13 13 11 12 13 13 13 12 13 14 16 15 1c 27 25 1d 16 15 15 14 14 16 13 14 13 14 13 14 14 12 13 12 13 14 15 13 13 15 15 14 14 14 14 13 13 16 13 13 13 13 12 15 14 12 13 14 14 17 1c 1b 16 1b 26 38 43 43 35 30 35 36 38 39 35 32 30 2b 25 23 20 22 24 22 27 27 2a 29 2e 2a 28 28 29 26 29 28 32 2f 2e 33 34 38 40 3f 41 40 40 44 3c 3d 3c 3e 3b 37 31 32 2f 33 2c 2a 2a 22 18 13 17 13 13 13 13 12 14 14 13 12 13 14 15 11 11 11 11 13 14 12 12 11 14 13 16 13 12 12 11 12 15 12 12 12 12 11 15 18 14 15 15 15 14 13 13 12 13 13 14 13 13 13 13 12 15 1d 24 22 18 13 15 14 13 10 11 13 13 13 12 12 13 14 13 12 12 11 11 14 14 13 12 11 11 12 12 12 13 12 12 14 12 13 13 12 12 13 12 11 12 5a 0 50 df 20 0 4 e2 0 32 10 a 0 0 0 0 40 ff ff 3 0
    //2 1 55 9 10 11 14 12 11 12 12 12 14 11 10 10 10 13 12 11 12 10 13 12 12 12 12 12 11 12 12 11 10 11 11 11 16 11 10 11 10 13 11 11 11 11 f 14 12 12 10 10 11 15 14 13 11 12 13 12 13 13 12 11 13 12 11 11 13 13 13 14 13 12 12 11 12 13 12 12 10 12 10 12 14 14 12 13 13 13 11 12 11 12 13 14 13 12 13 12 12 14 13 13 12 12 12 14 14 12 12 13 12 14 15 21 31 30 23 17 13 13 12 11 14 13 13 13 14 14 14 15 13 14 14 15 14 15 16 11 12 14 16 14 16 16 13 11 14 14 15 14 13 15 16 17 15 15 13 13 15 18 17 14 15 1a 2a 38 36 2d 24 24 28 28 29 27 24 21 23 25 22 25 26 29 28 2e 2f 2e 32 33 30 30 36 34 30 33 35 33 36 35 36 38 3b 44 43 41 3e 43 44 47 42 3f 42 46 3d 38 33 36 35 37 2e 28 23 1b 16 15 15 13 13 13 12 11 15 13 12 13 13 13 10 12 12 12 11 14 14 14 13 12 11 13 12 10 10 11 11 11 15 14 13 13 12 13 14 15 19 14 13 15 15 13 13 13 12 15 13 13 13 13 12 15 1c 26 23 1a 15 13 16 13 11 12 12 13 12 12 12 13 14 15 13 11 11 12 13 15 13 12 12 12 13 15 14 12 11 10 13 13 12 12 12 12 13 13 12 12 5a 0 50 df 20 0 4 e2 0 32 10 a 0 0 0 0 40 ff ff 3 0
    //2 1 55 9 12 14 13 12 11 12 11 15 11 11 11 11 13 11 12 11 11 10 15 11 13 13 11 12 16 12 12 12 11 13 12 12 11 11 11 12 11 11 12 10 11 12 12 11 10 11 12 11 13 12 12 12 12 11 12 13 11 12 12 14 13 13 13 12 12 11 13 13 12 13 11 11 14 12 12 12 14 12 15 11 12 11 12 13 14 14 13 12 12 13 12 11 12 13 13 14 16 13 12 12 12 12 12 11 12 13 12 12 15 15 18 27 3f 3d 29 1a 14 12 12 14 13 13 13 13 13 14 15 15 12 13 13 12 15 13 11 13 13 16 14 13 14 14 14 17 14 12 12 12 14 13 14 14 13 15 14 12 17 19 16 14 17 20 30 3d 37 2e 27 28 26 29 28 21 27 25 28 27 23 22 25 24 26 2c 32 2e 29 2b 2d 2f 2f 30 32 2d 35 33 33 37 3b 3e 3e 3e 40 3e 3d 3b 39 3c 3f 3a 3a 3f 35 35 38 35 31 2d 2d 29 22 1a 16 16 12 13 13 13 13 15 13 13 13 13 14 14 11 11 12 11 12 13 11 11 12 14 14 13 12 12 12 12 12 14 13 12 13 12 14 15 14 18 18 14 14 12 13 12 13 13 14 12 12 13 12 14 14 17 1c 25 23 19 15 15 13 12 10 13 13 13 13 13 12 14 15 12 12 12 12 12 15 12 11 12 12 12 13 12 12 13 11 13 14 15 15 14 12 12 13 13 15 13 5a 0 50 df 20 0 4 e2 0 32 10 a 0 0 0 0 40 ff ff 3 0
    //2 1 55 9 11 12 14 12 10 11 10 10 12 14 12 12 13 11 14 13 11 11 10 11 14 12 11 11 12 11 14 12 13 11 10 12 13 13 10 10 10 12 11 11 12 10 10 11 12 11 10 11 11 11 15 13 12 11 11 11 14 12 11 11 12 16 14 12 11 12 11 12 13 12 12 12 13 13 14 13 12 10 12 12 14 12 11 12 13 14 13 13 13 12 12 14 12 11 12 11 12 15 13 12 13 11 13 13 14 12 11 13 14 12 14 1f 2d 2e 1e 18 14 13 12 13 13 17 15 14 14 12 12 15 15 13 12 13 13 15 13 12 10 11 16 15 15 13 15 14 13 15 13 15 14 13 13 14 15 14 13 14 17 1c 1c 16 18 1e 37 3f 35 2b 24 22 26 23 1d 1f 1f 1f 23 27 23 1c 19 1f 1f 24 29 27 1d 24 2b 2e 2e 30 2f 31 33 34 32 36 37 3a 42 42 40 42 3f 3d 3d 41 3f 3d 41 40 41 3b 3e 37 3a 38 32 27 22 1b 14 14 14 12 12 13 13 15 13 12 12 13 13 13 10 11 10 12 13 14 12 11 11 11 12 13 12 12 12 12 11 16 11 12 13 13 13 14 15 18 15 14 14 13 12 12 15 13 14 15 14 13 16 13 12 17 1c 24 24 19 14 15 13 14 10 11 16 13 13 13 13 14 15 15 13 12 13 12 12 13 13 13 13 13 14 16 13 12 12 11 13 11 11 11 11 13 14 12 11 12 5a 0 50 df 20 0 4 e2 0 32 10 a 0 0 0 0 40 ff ff 3 0
    //    Random r = new Random();
    //    for (int i = 0; i < 10; i++) {
    //      int b = (r.nextInt() % 128) / 8;
    //      System.out.println("r: " + b);
    //    }
    //    System.out.println("8f " + 0x8f);
    //    System.out.println("random " + new Random().nextInt());
    //    System.out.println("key bridge " + 0x4b);
    //
    //    byte[] foo = new byte[12];
    //    ByteUtil.intToBytes(1000 * 10000, foo, 0);
    //    for (int i = 0; i < 12; i++) {
    //      System.out.println(i + " " + Integer.toHexString(foo[i]) + " " + 0x00989680);
    //    }
    //    System.out.println("bytes " + ByteUtil.intToBytes(1000, foo, 0));
  }

  /**
   * Required method from interface. Ignores the com port identifier and always
   * returns true
   */
//  public Boolean connect(CommPortIdentifier comPortIdentifier) {    return true;  }
  /**
   * Required method from interface. Always returns null.
   */
//  public CommPortIdentifier getComPortIdentifier() {    return null;  }
  /**
   * Required method from interface. Does no action.
   */
//  public void setComPortIdentifier(CommPortIdentifier comPortIdentifier) {  }
}
