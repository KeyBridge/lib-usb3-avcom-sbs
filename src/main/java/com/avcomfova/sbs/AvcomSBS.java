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
package com.avcomfova.sbs;

import com.avcomfova.sbs.datagram.Datagrams;
import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.TraceDatagram;
import com.avcomofva.sbs.datagram.read.ErrorResponse;
import com.avcomofva.sbs.datagram.read.HardwareDescriptionResponse;
import com.avcomofva.sbs.datagram.read.TraceResponse;
import com.avcomofva.sbs.datagram.write.HardwareDescriptionRequest;
import com.avcomofva.sbs.datagram.write.SettingsRequest;
import com.avcomofva.sbs.datagram.write.TraceRequest;
import com.avcomofva.sbs.enumerated.EAvcomDatagram;
import com.ftdichip.usb.FTDI;
import static com.ftdichip.usb.FTDI.DEFAULT_BAUD_RATE;
import com.ftdichip.usb.enumerated.EFlowControl;
import com.ftdichip.usb.enumerated.ELineDatabits;
import com.ftdichip.usb.enumerated.ELineParity;
import com.ftdichip.usb.enumerated.ELineStopbits;
import com.keybridgeglobal.sensor.util.ByteUtil;
import com.keybridgeglobal.sensor.util.StopWatch;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.exception.UsbException;

/**
 * Sensor implementation supporting the Avcom SBS single-board-sensor platform.
 * This includes the RSA-2500 series and all Avcom of Virginia products build
 * upon that basic sensor platform.
 * <p>
 * This {@linkplain ISensor} implementation provides a set of simple methods to
 * control an Avcom SBS device and to stream data from that device. Actual
 * communication with the device is handled via a dedicated (new) thread
 * (created by this class) and is ASYNCHRONOUS: control messages are submitted
 * to the device as requests and data is read out from the device <em>as
 * available</em> in the future. For reference: there is typically a delay of
 * between 1 and 100 ms between an initial control request and data availability
 * from the device.
 * <p>
 * Developer note: To communicate with USB-attached devices this package uses
 * and requires the <code>usb4java</code> library and its underlying native
 * system library <code>libusb</code>.
 * <p>
 * The Avcom SBS device uses the FTDI UART to USB converter. This implementation
 * includes an internal FTDI USB/serial port driver. It is known to work on the
 * following Linux flavors: AMD64, i386, ARM. Other operating systems and
 * variants should work but are not tested.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class AvcomSBS implements Runnable {

  /**
   * The USB Device to which this AvcomSBS device is attached. This is the FTDI
   * serial I/O USB device through which the Avcom SBS sensor communicates.
   */
  private final FTDI ftdi;

  /**
   * The hardware description response message provided by the attached Avcom
   * device. The response message contains all available hardware description
   * parameters.
   */
  private HardwareDescriptionResponse hardwareDescription;
  /**
   * The current data collection settings request. This defines the current
   * operation of the sensor.
   */
  private SettingsRequest settingsRequest;

  /**
   * A set of IDatagramListener instances. These will be notified when a new
   * Avcom datagram is read from the device represented by this instance.
   */
  private final List<IDatagramListener> datagramListeners;

  /**
   * Avcom devices return a maximum of 320 (for 8-bit) or 480 (for 12 bit) bytes
   * of data per trace. This places a limit on the available resolution during a
   * wideband spectrum sweep. This limitation is overcome by stepping through a
   * list of device configurations, iteratively re-tuning the sensor to perform
   * a piecewise wideband scan.
   * <p>
   * This queue of device configurations (SettingsRequest datagrams) is scanned
   * by the data capture thread and so must support concurrence. Settings may be
   * added or removed at any time. If the device is in the middle of an extended
   * scan it will be interrupted if the settings queue is updated.
   * <p>
   * SettingsRequest entries are stored using the sample center frequency (MHz)
   * as a key.
   */
  private static final ConcurrentMap<Double, SettingsRequest> settingsRequestQueue = new ConcurrentSkipListMap<>();
  /**
   * As with the SettingsRequestQueue, this queue stores piecewise sampled trace
   * data corresponding to each SettingsRequest entry.
   * <p>
   * TraceResponse (8 bit) entries are stored using their center frequency (MHz)
   * as a key. An interface is required in this map since the READ method does
   * not return type-specific datagram instances.
   */
  private static final ConcurrentMap<Double, IDatagram> traceDatagramQueue = new ConcurrentSkipListMap<>();
  /**
   * Tread helper flag to indicate that new settings have been requested and any
   * current scans (especially a wide-band scan) should be immediately
   * interrupted and the new settings should be used. As wide-band scans can
   * take some time (up to 15 or 20 seconds) this thread interrupt is important
   * for a responsive user interface.
   */
  private boolean newSettings = false;

  /**
   * Construct a new AvcomSBS instance connected via the indicated USB device
   * port.
   * <p>
   * This will automatically connect to the device. This also created a shutdown
   * hook to automatically disconnect from the USB port when the application
   * exits.
   * <p>
   * @param ftdi the FTDI serial I/O USB Device to which this AvcomSBS device is
   *             attached.
   * @throws UsbException if the USB Device cannot be attached or claimed for
   *                      use
   */
  public AvcomSBS(final FTDI ftdi) throws UsbException, Exception {
    System.out.println("Opening AvcomSBS on USB " + ftdi);
    /**
     * Set the USB Device.
     */
    this.ftdi = ftdi;
    /**
     * Initialize the Datagram listeners.
     */
    this.datagramListeners = new ArrayList<>();
    /**
     * Initialize the device. This sends a few HardwareDescriptionRequests and
     * attempts to populate the internal HardwareDescriptionResponse field.
     */
    initialize();
  }

  /**
   * Set the Avcom device settings.
   * <p>
   * If required, the user settings will be divided into multiple smaller
   * SettingsRequest datagrams and placed onto an internal queue for iterative
   * processing.
   * <p>
   * @param settingsRequest
   */
  public void setSettings(SettingsRequest settingsRequest) {
    /**
     * Save the current device settings request.
     */
    this.settingsRequest = settingsRequest;
    /**
     * Purge the current SettingsRequest queue. The Trace data queue will be
     * cleared by the data collector thread.
     */
    synchronized (settingsRequestQueue) {
      settingsRequestQueue.clear();
    }
    /**
     * If the SettingsRequest span and RBW require more data points than a
     * single Trace can carry then spread the data sample over multiple
     * SettingsRequest instances.
     * <p>
     * Developer note: The method of deconstruction is:
     * <pre>
     *  | <------ w -----> | <------ w -----> | <------ w -----> | <---- w * i ---> |
     *  | <-------------------------------  span  --------------------------------> |
     *  | ^       ^                  ^        ^
     *    start   cf0                |cf1     centerFrequency
     * <p>
     * Where:
     *    w  = Span of each individual sample
     *       = WAVEFORM_DATA_LENGTH (bytes/sample) * ResolutionBandwidth (MHz/byte)
     *       = 320 * RBW (MHz)
     *    i  = Number of samples required
     *       = span / w = number of required samples to capture the entire span
     * start = centerFrequency - 1/2 span
     *  cf_i = w * (1/2 + i) + start
     * </pre>
     */
    if (settingsRequest.getSpanMHz() > TraceResponse.TRACE_DATA_LENGTH * settingsRequest.getResolutionBandwidth().getMHz()) {
//      System.out.println("DEBUG setSettings must be split into multiples " + settingsRequest);
      /**
       * The request requires more data points than a single Trace can carry.
       */
      double startMHz = settingsRequest.getCenterFrequencyMHz() - settingsRequest.getSpanMHz() / 2;
      double stopMHz = settingsRequest.getCenterFrequencyMHz() + settingsRequest.getSpanMHz() / 2;
      double sampleSpanMHz = TraceResponse.TRACE_DATA_LENGTH * settingsRequest.getResolutionBandwidth().getMHz();
      int numSettings = (int) (settingsRequest.getSpanMHz() / sampleSpanMHz) + 1;
//      System.out.println("DEBUG setSettings start/stop " + startMHz + " / " + stopMHz + " samplespan " + sampleSpanMHz + " numSettings " + numSettings);
      for (int iterator = 0; iterator < numSettings; iterator++) {
        /**
         * The iterator center frequency cf_i in MHz.
         */
        double cfiMHz = sampleSpanMHz * (0.5 + iterator) + startMHz;
        /**
         * Create a new SettingsRequest copying all the previous values and
         * setting a new center frequency and span.
         */
        SettingsRequest sr = settingsRequest.copy();
        sr.setCenterFrequencyMHz(cfiMHz); // new center frequency
        sr.setSpanMHz(sampleSpanMHz); // new span
        /**
         * Add each new SettingsRequest to the queue, sorted by centerFrequency.
         */
        if (sr.getStartFrequencyMHz() > hardwareDescription.getProductId().getMinFrequency()
            && sr.getStopFrequencyMHz() < hardwareDescription.getProductId().getMaxFrequency()) {
          synchronized (settingsRequestQueue) {
            settingsRequestQueue.put(cfiMHz, sr);
          }
        } else if (sr.getStartFrequencyMHz() < hardwareDescription.getProductId().getMinFrequency()
                   && sr.getStopFrequencyMHz() > hardwareDescription.getProductId().getMinFrequency()) {
          /**
           * The settings request is low. Shift the frequencies to the right
           * (higher).
           */
          double span = sr.getStopFrequencyMHz() - hardwareDescription.getProductId().getMinFrequency();
          double newcf = span / 2d + hardwareDescription.getProductId().getMinFrequency();
          sr.setCenterFrequencyMHz(newcf);
          sr.setSpanMHz(span);
//          System.out.println("  low: adjusted to " + sr);
          synchronized (settingsRequestQueue) {
            settingsRequestQueue.put(newcf, sr);
          }
        } else if (sr.getStartFrequencyMHz() < hardwareDescription.getProductId().getMaxFrequency()
                   && sr.getStopFrequencyMHz() > hardwareDescription.getProductId().getMaxFrequency()) {
          /**
           * The settings request is hight. Shift the frequencies to the left
           * (lower).
           */
          double span = hardwareDescription.getProductId().getMaxFrequency() - sr.getStartFrequencyMHz();
          double newcf = span / 2d + sr.getStartFrequencyMHz();
          sr.setCenterFrequencyMHz(newcf);
          sr.setSpanMHz(span);
//          System.out.println("high: adjusted to " + sr);
          synchronized (settingsRequestQueue) {
            settingsRequestQueue.put(newcf, sr);
          }
        } else {
//          System.out.println("settings are out of bounds. discard " + sr);
        }
      } // end for numsamples
    } else {
      /**
       * The SettingsRequest can be handled by a single Trace and does not
       * require piecewise reassembly. Adjust left (lower) or right (higher) if
       * the span is out of bounds.
       */
      SettingsRequest sr = settingsRequest.copy();
      if (sr.getStartFrequencyMHz() < hardwareDescription.getProductId().getMinFrequency()) {
        double span = sr.getStopFrequencyMHz() - hardwareDescription.getProductId().getMinFrequency();
        double newcf = span / 2d + hardwareDescription.getProductId().getMaxFrequency();
        sr.setCenterFrequencyMHz(newcf);
        sr.setSpanMHz(span);
      }
      if (sr.getStopFrequencyMHz() > hardwareDescription.getProductId().getMaxFrequency()) {
        double span = hardwareDescription.getProductId().getMaxFrequency() - sr.getStartFrequencyMHz();
        double newcf = span / 2d + settingsRequest.getStartFrequencyMHz();
        sr.setCenterFrequencyMHz(newcf);
        sr.setSpanMHz(span);
      }
      synchronized (settingsRequestQueue) {
        settingsRequestQueue.put(settingsRequest.getCenterFrequencyMHz(), sr);
      }
    }  // end else
    /**
     * Set the new settings flag. This will be picked up by the data collection
     * thread and the new settings will be immediately used.
     */
    newSettings = true;
  }

  /**
   * Get the device configuration. This returns a sorted map of device
   * configuration names and their corresponding values.
   * <p>
   * @return a non-null Map instance.
   */
  public Map<String, String> getConfiguration() {
    return hardwareDescription != null ? hardwareDescription.getConfiguration() : new HashMap<String, String>();
  }

  /**
   * Connect to a network-attached sensor.
   * <p>
   * @param inetAddress the IP address of the remote sensor. The factory default
   *                    IP address is <code>192.168.118.242</code>, listening on
   *                    port <code>26482</code>.
   */
  public void connectEthernet(InetAddress inetAddress) {
    /**
     * The factory default IP address is <code>192.168.118.242</code>, listening
     * on port <code>26482</code>. The device Ethernet controller internally
     * converts inbound IP packet data to RS232. Therefor the output baud rate
     * setting is critical and must be set to 115200 bits per second.
     */
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Initialize the Avcom device by sending a few HardwareDescriptionRequest
   * datagrams down the wire.
   * <p>
   * @throws UsbException if the datagrams cannot be written to the device.
   */
  @SuppressWarnings("SleepWhileInLoop")
  private void initialize() throws UsbException, Exception {
    /**
     * Configure the FTDI serial port to Avcom specifications.
     */
    ftdi.setSerialPort(DEFAULT_BAUD_RATE, ELineDatabits.BITS_8, ELineStopbits.STOP_BIT_1, ELineParity.NONE, EFlowControl.DISABLE_FLOW_CTRL);
    /**
     * Get a HardwareDescriptionResponse from the device. Try a few times to
     * allow for the device to boot up and also to accommodate some sloppiness
     * on the USB line (not all datagrams are clean)..
     * <p>
     * Ping the hardware a few times when we start up to make sure we get a good
     * hardware description. If we don't have a good hardware description then
     * bad things happen later when trying to take data from the spectrum
     * analyzer as we don't know what we're attached to.
     */
    for (int i = 0; i < 5; i++) {
      write(new HardwareDescriptionRequest());
      IDatagram datagram = read();
      if (datagram instanceof HardwareDescriptionResponse) {
        hardwareDescription = (HardwareDescriptionResponse) datagram;
//        System.out.println("DEBUG AvcomSBS device initialized " + hardwareDescription);
        break; // bread out of the FOR loop if hardware description received.
      }
      /**
       * If no HardwareDescriptionResponse datagram was read from the USB port
       * then wait one second a try again.
       */
      try {
        System.err.println("AvcomSBS device initialization try " + (i + 1) + ".");
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        Logger.getLogger(AvcomSBS.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    /**
     * If no HardwareDescriptionResponse datagram was read after five tries then
     * raise an error condition.
     */
    if (hardwareDescription == null) {
      throw new Exception("AvcomSBS initialization failed. Unable to retrieve a Hardware Description Response from " + ftdi.toString());
    }
    /**
     * At this point a HardwareDescriptionResponse has been retrieved and the
     * device is ready for service. Initialize the device with a default
     * wide-band setting to start taking data right away.
     */
    setSettings(SettingsRequest.getInstance());
  }

  /**
   * Read data from the USB port. When the device returns data it does so in
   * many byte[] chunks. This method blocks until all available data is read off
   * the port. Once the port read is completed the data is parsed into an Avcom
   * datagram instance and all datagram listeners are notified.
   * <p>
   * @return an Avcom datagram instance.
   * @throws UsbException if the USB port cannot be accessed
   * @throws Exception    if the Avcom data cannot be parsed into a valid
   *                      datagram instance
   */
  @SuppressWarnings({"NestedAssignment", "ValueOfIncrementOrDecrementUsed"})
  private IDatagram read() throws Exception {
    /**
     * Make a note of the (attempted) read operation.
     */
    if (hardwareDescription != null) {
      hardwareDescription.setDatagramRead();
    }
    /**
     * Initialize the first USB packet and the bytesRead indicator. These are
     * used in the first iteration of the while-loop below.
     * <p>
     * USB data is sent in packets Least Significant Bit (LSB) first. There are
     * 4 main USB packet types: Token, Data, Handshake and Start of Frame. Each
     * packet is constructed from different field types, namely SYNC, PID,
     * Address, Data, Endpoint, CRC and EOP. The packets are then bundled into
     * frames to create a USB message.
     * <p>
     * Each USB packet starts with a (1 byte) SYNC field. This is basically used
     * to synchronize the transmitter and the receiver so that the data can be
     * transferred accurately. In a USB slow / full speed system this SYNC field
     * consists 3 KJ pairs followed by 2 K’s to make up 8 bits of data. In a USB
     * Hi-Speed system the synchronization requires 15 KJ pairs followed by 2
     * K’s to make up 32 bits of data.
     * <p>
     * Following on directly after the SYNC field is a (1 byte) Packet
     * Identifier Field. The Packet Identifier Field consists of a 4 bit
     * identifier and a further 4 bits which are the one’s compliment of the
     * identifier.
     * <p>
     * The data field is not a fixed length. It is within the range of 0 - 8192
     * bits long, and always an integral number of bytes.
     * <p>
     * USB DATA Packets: A data packet may be variable length, dependent upon
     * the data. However, the data field will be an integral number of bytes. A
     * USB Data packet is constructed thus: [SYNC][PID][DATA][CRC16][EOP].
     * Depending upon the bus speed the SYNC header is 8 or 31 bits. PID is
     * always 8 bits. DATA may range between 0 and 8192 bits. CRC16 is 16 bits
     * and EOP (end of packet) is 3 bits.
     * <p>
     * Developer note: CRC and EOP are not included in the returned byte array,
     * but rather are processed and used to raise an error condition within the
     * USB Pipe instance. Therefore the usbPacket is actually just
     * [SYNC][PID][DATA].
     */
    byte[] avcomDatagram = null;
    int avcomDatagramIndex = 0;
    int readLoop = 0;
    /**
     * The return value will indicate the number of bytes successfully
     * transferred from the target endpoint. The return value will never exceed
     * the total size of the provided buffer.
     * <p>
     * FTDI UART chips add a two-byte modem status header to every USB Packet
     * they send. The modem status is send as a header for each read access. In
     * the absence of data the FTDI chip will generate the status every 40 ms.
     */
    byte[] usbPacket = ftdi.read();
    while (usbPacket.length > 0) {
//      System.out.println("    READ [" + usbPacket.length + "] " + ByteUtil.toString(usbPacket));
      /**
       * Developer note: There is a race condition with the FTDI chip where it
       * will produce infinite zeros if the settings are not configured properly
       * or if you try to read data from a USB write port.
       * <p>
       * If no AvcomDatagram is initialized after 10 USB Packets then there is
       * probably an error in your implementation and/or port selection. (10 is
       * an arbitrary number, but the Avcom device typically initializes an
       * Avcom Datagram on the first USB packet returned; the second if there is
       * old data in the out buffer.) Avoid the race condition by breaking out
       * of the WHILE READ loop.
       */
      if (avcomDatagram == null && readLoop++ > 10) {
        break;
      }
      /**
       * Inspect the USB Packet data bytes for an Avcom STX flag (0x02) and a
       * valid Datagram identifier.
       * <p>
       * The STX flag will be located at the third USB packet byte (index = 2:
       * the first data byte after the two-byte USB Packet header). The Avcom
       * datagram identifier always follows the STX byte and a two-byte datagram
       * length indicator. Add the USB header and it is located at the sixth USB
       * packet byte (index = 5).
       */
      if (usbPacket[0] == IDatagram.FLAG_STX && EAvcomDatagram.fromByteCode(usbPacket[3]) != null) {
        /**
         * Initialize the Avcom datagram byte buffer to the length indicated in
         * the datagram packet header (Avcom datagram byte index 1 and 2). The
         * USB Packet byte index 3 tells ByteUtil to skip the 2-byte header.
         * <p>
         * Add four additional bytes the Avcom datagram byte array to include
         * the Avcom packet header information, which is not included in the
         * Avcom datagram length number.
         */
        avcomDatagram = new byte[ByteUtil.twoByteIntFromBytes(usbPacket, 3) + 4];
        /**
         * Important: Initialize the Avcom Datagram byte buffer index to zero.
         * The avcomDatagramIndex is used to copy fresh data into the byte
         * buffer from subsequent USB packets.
         */
        avcomDatagramIndex = 0;
      }
      /**
       * If the Avcom datagram byte buffer has been initialized then copy the
       * USB packet data into the Avcom datagram byte buffer. Increment the
       * Avcom Datagram byte buffer index by the number of bytes copied.
       * <p>
       * Developer note: The current read strategy requires that each new Avcom
       * datagram start with a fresh USB packet. However if the HOST system is
       * running slow the Avcom sensor can stuff the USB port buffer. In this
       * case USB packet data does not always align with an Avcom datagram: e.g.
       * the end of one datagram and the beginning of another can appear in the
       * same USB packet, with an Avcom datagram STX code somewhere in the
       * middle of a USB Packet.
       * <p>
       * We prevent a buffer overrun by keeping track of how many more bytes the
       * current Avcom datagram are required/allowed. If there is more data in
       * the USB packet than the datagram requires then only copy up to the
       * datagram fill and no more.
       * <p>
       * Extra data (the next Avcom datagram) in the USB packet will be
       * discarded and all subsequent USB packets containing the data for the
       * discarded datagram will also be discarded. Eventually (after the
       * discarded datagram is read through) the host will have caught up with
       * the Avcom sensor and USB packets will re-align with an Avcom datagram.
       * <p>
       * @TODO: Analyze USB packet data as a byte stream. Detect and trigger
       * Avcom datagram STX and ETX flags to start and conclude a read
       * operation.
       */
      if (avcomDatagram != null) {
        /**
         * copyLength is the number of array elements to be copied.
         */
        int copyLength = (usbPacket.length + avcomDatagramIndex > avcomDatagram.length
                          ? avcomDatagram.length - avcomDatagramIndex
                          : usbPacket.length);

        /**
         * DEBUG output. This dumps the bytes read to the console for analysis.
         */
//        System.out.println("  USB PIPE READ " + bytesRead + " bytes "
//          + usbPacket.length + " [" + ByteUtil.toString(usbPacket)
//          + "]  avcomDatagram.length [" + avcomDatagram.length
//          + "] avcomDatagramIndex [" + avcomDatagramIndex
//          + "] copylength [" + copyLength + "]");
        System.arraycopy(usbPacket,
                         0,
                         avcomDatagram,
                         avcomDatagramIndex,
                         copyLength);
        avcomDatagramIndex += copyLength;
      }
      /**
       * Important: READ new data into the usbPacket byte array.
       * <p>
       * Developer note: The syncSubmit (and presumably asyncSubmit) do not
       * clear the input byte array - they merely write bytes into the provided
       * array. Reusing a previously populated byte array creates JUNK data as
       * new bytes are written over the old bytes but if the new packet is
       * shorter then old bytes will remain.
       */
      usbPacket = ftdi.read();
    }
    /**
     * At this point the Avcom datagram is either null (e.g. the HOST is
     * catching up from an overrun) or has been completely read. Process and
     * return the Avcom datagram byte buffer as a valid Datagram instance.
     * <p>
     * If the data is null then return null; If the data is not null then parse
     * it into a datagram and return the datagram for internal handling.
     * <p>
     * Developer note: No NOT notify listeners from here. Listeners are notified
     * from the data collection RUN process, which assembles and distributes
     * TraceDatagram instances.
     */
    return avcomDatagram == null ? null : Datagrams.getInstance(avcomDatagram);
  }

  /**
   * Write a REQUEST datagram to the Avcom device.
   * <p>
   * @param datagram the REQUEST-type datagram to write to the Avcom device
   * @throws UsbException if the USB port cannot be written to
   */
  private void write(IDatagram datagram) throws UsbException {
    /**
     * Since all Avcom write operations are small we use a standard USB IRP
     * container to send commands to the device or write bytes directly to the
     * port. Both methods achieve the same result. In this case since all
     * datagrams have a serialize method writing bytes is easier to code.
     */
    ftdi.write(datagram.serialize());
//    System.out.println("    WRITE [" + datagram.serialize().length + "] " + ByteUtil.toString(datagram.serialize()));
    /**
     * Developer note: Important: Wait a bit for the datagram to be processed
     * (especially new settings) to take effect. Avcom devices need about 2 to 5
     * milliseconds to write any new settings to RAM. Give the device a little
     * buffer with 10 milliseconds.
     * <p>
     * This slows down the device IO but ensures that all write/read
     * transactions are correctly and predictably handled.
     */
    try {
      Thread.sleep(10);
    } catch (InterruptedException interruptedException) {
    }
    /**
     * Finally make a note of the write operation.
     */
    if (hardwareDescription != null) {
      hardwareDescription.setDatagramWrite();
    }
  }

  //<editor-fold defaultstate="collapsed" desc="IDatagramListener Manager methods">
  /**
   * Internal method called when an Avcom datagram has been read off the device.
   * The datagram is forwarded to all listeners when they are notified.
   * <p>
   * @param datagram the datagram to forward
   */
  private void notifyListeners(IDatagram datagram) {
    for (IDatagramListener iDatagramListener : datagramListeners) {
      iDatagramListener.onDatagram(datagram);
    }
  }

  /**
   * Add a Datagram Listener to receive datagrams when ready
   * <p>
   * @param listener the listener instance
   */
  public synchronized void addListener(IDatagramListener listener) {
    this.datagramListeners.add(listener);
  }

  /**
   * Remove a DatagramListener
   * <p>
   * @param listener the listener instance
   */
  public synchronized void removeListener(IDatagramListener listener) {
    this.datagramListeners.remove(listener);
  }//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Threaded Runnable Methods">
  /**
   * Indicator that the device should be capturing data.
   */
  private boolean run;
  /**
   * The separate thread that is running the data capture.
   */
  private Thread runThread;
  /**
   * When requesting a very large waveform this value tracks the percent
   * complete, from zero to 100.
   */
  private double percentComplete;

  /**
   * Get the percent complete of the current sweep.
   * <p>
   * @return the sweep progress, from zero to 100.
   */
  public int getPercentComplete() {
    return (int) (percentComplete * 100);
  }

  /**
   * Runnable method executed in a new thread when this AvcomSBS device is
   * activated.
   */
  @Override
  public void run() {
    StopWatch stopwatch = new StopWatch();
    while (run) {
      try {
        /**
         * Clear the datagram queue to be filled with new entries.
         */
        traceDatagramQueue.clear();
        for (Map.Entry<Double, SettingsRequest> entry : settingsRequestQueue.entrySet()) {
          /**
           * Write the SettingsRequest, then immediately request and read a new
           * TRACE. READ will notify any listeners of the new data Trace.
           * <p>
           * The new Trace is stored in the Trace Data Queue until all the
           * settings have been run. Then a final Trace is assembled.
           * <p>
           * The following blocks until completed.
           * <p>
           * Developer note: Avcom devices do not respond to a SettingsRequest
           * write so we can write the SettingsRequest immediately followed by a
           * TraceRequest.
           */
          stopwatch.startTimer();
          write(entry.getValue());
          write(new TraceRequest());
          IDatagram datagram = read();
          datagram.setElapsedTimeMillis(stopwatch.getElapsedTimeMillis());
          /**
           * Developer note: Important: READ can return any type of datagram,
           * including a NULL value. Always inspect the returned datagram to
           * ensure it is not null and is actually a TraceResponse.
           */
          if (datagram instanceof TraceResponse) {
            traceDatagramQueue.put(entry.getKey(), datagram);
            hardwareDescription.setElapsedTime(datagram.getElapsedTimeMillis());
          } else if (datagram instanceof ErrorResponse) {
            hardwareDescription.setDatagramError();
//            System.err.println("AvcomSBS data capture received error response: " + ((ErrorResponse) datagram).getErrorMessage());
          }
          /**
           * Update the percent complete. This is used to provide user interface
           * progress and feedback.
           */
          percentComplete = (double) traceDatagramQueue.size() / (double) settingsRequestQueue.size();
          /**
           * Fire a progress change event. This is picked up by any UI widgets
           * watching this instance.
           * <p>
           * @TODO: Not yet implemented. Fire a progress change event
           */
//          fireProgressChange(percentComplete)/
          /**
           * If new settings were set then break out of the current FOR loop and
           * restart a new FOR loop with the new settings (we are still within
           * the WHILE loop).
           */
          if (newSettings) {
            /**
             * Reset the new settings flag.
             */
            newSettings = false;
            /**
             * Clear the datagram queue of all trace entries with the previous
             * settings.
             */
            traceDatagramQueue.clear();
            /**
             * Break out of the current FOR loop and start a new one with the
             * new settings.
             */
            break;
          }
        }
        /**
         * Assemble a final Trace from the collected Trace Data Queue.
         */
        TraceDatagram traceDatagram = TraceDatagram.getInstance(settingsRequest);
        for (Map.Entry<Double, IDatagram> entry : traceDatagramQueue.entrySet()) {
          traceDatagram.addData((TraceResponse) entry.getValue());
        }
        /**
         * Notify all listeners with the assembled TraceDatagram.
         */
        notifyListeners(traceDatagram);
      } catch (Exception exception) {
        /**
         * Since we are operating in a rapid loop ignore individual datagram
         * build-related run capture errors. This will cause any errored
         * datagram to be discarded.
         */
//        System.err.println("DEBUG AvcomSBS data capture run error: " + exception.getMessage());
//        Logger.getLogger(AvcomSBS.class.getName()).log(Level.SEVERE, null, exception);
        /**
         * Note the error in the hardware description queue.
         */
        hardwareDescription.setDatagramError();

      }
    }
  }

  /**
   * Start the Avcom SBS data capture. Data capture runs in a separate thread.
   */
  public void start() {
    this.run = true;
    this.runThread = new Thread(this, ftdi.toString());
    this.runThread.start();
  }

  /**
   * Stop the data capture thread.
   */
  public void stop() {
    this.run = false;
    this.runThread.interrupt();
  }//</editor-fold>
}
