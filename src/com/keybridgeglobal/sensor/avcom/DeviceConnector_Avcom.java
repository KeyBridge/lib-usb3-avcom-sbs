package com.keybridgeglobal.sensor.avcom;

import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.avcom.datagram.*;
import com.keybridgeglobal.sensor.interfaces.IDatagramListener;
import com.keybridgeglobal.sensor.interfaces.IDeviceAdapter;
import com.keybridgeglobal.sensor.interfaces.IDeviceConnector;
import com.keybridgeglobal.sensor.util.Debug;
import com.keybridgeglobal.sensor.util.LockObject;
import com.keybridgeglobal.sensor.util.StopWatch;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 0501: changed settings and waveform queues to ConcurrentHashMap for thread
 * safety <br>
 * 0430: add new run thread to allow the spectrum analyzer to run multiple jobs
 * <br>
 * 0429: synchronize requestWaveform() for multi-thread compatibility <br>
 * <p>
 * @author jesse
 */
public class DeviceConnector_Avcom implements IDeviceConnector, IDatagramListener, Runnable {

  private Debug d = new Debug(false);
  // Listeners
  private final List<IDatagramListener> datagramListeners = new ArrayList<>();
  private final List<ChangeListener> progressListeners = new ArrayList<>();
  // Device Connector parameters
  private IDeviceAdapter deviceAdapter = null;
  private HardwareDescriptionResponse hardwareDescription;
  // LockObject and Notification Variables
  private final LockObject lock = new LockObject();
  private Boolean notified = true;
  private final int NOTIFICATION_TIMEOUT = 200;
  private final int NOTIFICATION_TIMEOUT_SETTINGS = 1;
  private final int NOTIFICATION_TIMEOUT_HWDESCRIPTION = 100;
  private final int NOTIFICATION_TIMEOUT_WAVEFORM = NOTIFICATION_TIMEOUT;
  private long requestOutTime;
  private SettingsRequest settingsRequest;
  private final Map<Double, SettingsRequest> settingsRequestQueue = new ConcurrentHashMap<>();
  // private final Map<Double, Datagram_Interface> waveformResponseQueue =
  // Collections.synchronizedMap(new TreeMap<Double, Datagram_Interface>()); //
  // our ConcurrentHashMap
  private final Map<Double, IDatagram> waveformResponseQueue = new ConcurrentHashMap<>();
  private int waveformRequested;
  private int waveformProduced;
  private int datagramSent;
  private int datagramReceived;
  private int datagramTimeouts;
  private int datagramInvalid;
  private int elapsedTimeMax = 0;
  private int elapsedTimeMin = 0;
  private int elapsedTimeAve = 0;
  private double percentComplete = 0;
  private boolean settingsInterrupt;
//  private CommPortIdentifier                     commPortIdentifier;
  private SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();

  public DeviceConnector_Avcom() {
//    d.out(this, "DeviceConnector_Avcom " + cpi.getName());
    // Set the com port identifier
//    commPortIdentifier = cpi;
    // Attempt to connect
    connect();
    // Add a shutdown hook to close the port when we're shutting down
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        d.out("Shutdown Hook: DeviceConnector_Avcom");
        disconnect();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
      }
    });
  }

  /**
   * Set the Device Connector settings <br>
   * If required, the settings will be divided into multiple smaller
   * SettingsRequest datagrams and placed onto the TreeMap queue
   * 'settingsRequestQueue'. The method is:
   * <p>
   * <
   * pre>
   * | <------ w -----> | <------ w -----> | <------ w -----> | <---- w * i --->
   * | | <------------------------------- span -------------------------------->
   * | | ^ ^ ^ ^ start cf0 |cf1 centerFrequency
   * <p>
   * Where: w = Span of each individual sample = TRACE_DATA_LENGTH
   * (bytes/sample) * ResolutionBandwidth (MHz/byte) = 320 * RBW (MHz) i =
   * Number of samples required = span / w = number of required samples to
   * capture the entire span start = centerFrequency - 1/2 span centerFreq_i = w
   * * (1/2 + i) + start
   * </pre>
   * <p>
   * @param settingsRequest
   */
  public void setSettings(IDatagram settingsRequestDatagram) {
    // Purge the queue
    synchronized (settingsRequestQueue) {
      settingsRequestQueue.clear();
      waveformResponseQueue.clear();
    }
    settingsRequest = (SettingsRequest) settingsRequestDatagram;
    // If a Settings Queue is required, create and populate
    if (settingsRequest.getSpanMHz() > IDatagram.TRACE_DATA_LENGTH * settingsRequest.getResolutionBandwidth().getMHz()) {
      // Our request is longer than the sample size. Use multiple samples.
      double startMHz = settingsRequest.getCenterFrequencyMHz() - settingsRequest.getSpanMHz() / 2;
      double stopMHz = settingsRequest.getCenterFrequencyMHz() + settingsRequest.getSpanMHz() / 2;
      double sampleSpanMHz = IDatagram.TRACE_DATA_LENGTH * settingsRequest.getResolutionBandwidth().getMHz();
      int numSamples = (int) (settingsRequest.getSpanMHz() / sampleSpanMHz) + 1;
      d.out(this, "setSettings: " + settingsRequest.toStringBrief() + " requires a queue of " + numSamples + " with startf: [" + startMHz + "] stopf: [" + stopMHz + "] each span: [" + sampleSpanMHz + "]");
      for (int iterator = 0; iterator < numSamples; iterator++) {
        double cfiMHz = sampleSpanMHz * (0.5 + iterator) + startMHz;
        // Create a new SettingsRequest
        SettingsRequest set = new SettingsRequest();
        set.setCenterFrequencyMHz(cfiMHz); // new center frequency
        set.setSpanMHz(sampleSpanMHz); // new span
        set.setReferenceLevel(settingsRequest.getReferenceLevel()); // copy same
        // values
        set.setResolutionBandwidth(settingsRequest.getResolutionBandwidth()); // copy
        // same
        // values
        set.setInputConnector(settingsRequest.getInputConnector()); // copy same
        // values
        set.setLnbPower(settingsRequest.getLnbPower()); // copy same values
        // Add each new SettingsRequest to the queue, sorted by centerFrequency
        // Run it through the factory to make sure the values are OK
        // TODO - get this from the device type
        if ((set.getStartFrequencyMHz() > IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ) && (set.getStopFrequencyMHz() < IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ)) {
          synchronized (settingsRequestQueue) {
            // d.out(this, "  setSettings.put " + set.toStringBrief());
            settingsRequestQueue.put(cfiMHz, DatagramFactory.copySettingsRequest(set));
          }
        } else if ((set.getStartFrequencyMHz() < IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ) && (set.getStopFrequencyMHz() > IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ)) {
          // low sample, shift right
          double span = set.getStopFrequencyMHz() - IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ;
          double newcf = span / 2d + IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ;
          set.setCenterFrequencyMHz(newcf);
          set.setSpanMHz(span);
          // d.out(this, "    low: adjusted to " + set.toStringBrief());
          settingsRequestQueue.put(newcf, DatagramFactory.copySettingsRequest(set));
        } else if ((set.getStartFrequencyMHz() < IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ) && (set.getStopFrequencyMHz() > IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ)) {
          // low sample, shift right
          double span = IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ - set.getStartFrequencyMHz();
          double newcf = span / 2d + set.getStartFrequencyMHz();
          set.setCenterFrequencyMHz(newcf);
          set.setSpanMHz(span);
          // d.out(this, "    high: adjusted to " + set.toStringBrief());
          settingsRequestQueue.put(newcf, DatagramFactory.copySettingsRequest(set));
        } else {
          // d.out(this, "  set is out of bounds. discard " +
          // set.toStringBrief());
        }
      }
    } else {
      d.out(this, "setSettings: Requested settings do not require splitting");
      // Adjust left or right if out of bounds
      if (settingsRequest.getStartFrequencyMHz() < IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ) {
        double span = settingsRequest.getStopFrequencyMHz() - IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ;
        double newcf = span / 2d + IDatagram.AVCOM_MINIMUM_FREQUENCY_MHZ;
        settingsRequest.setCenterFrequencyMHz(newcf);
        settingsRequest.setSpanMHz(span);
      }
      if (settingsRequest.getStopFrequencyMHz() > IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ) {
        double span = IDatagram.AVCOM_MAXIMUM_FREQUENCY_MHZ - settingsRequest.getStartFrequencyMHz();
        double newcf = span / 2d + settingsRequest.getStartFrequencyMHz();
        settingsRequest.setCenterFrequencyMHz(newcf);
        settingsRequest.setSpanMHz(span);
      }
      // Add SettingsRequest to the queue, sorted by centerFrequency
      synchronized (settingsRequestQueue) {
        // d.out(this, "  setSettings.put " + settingsRequest.toStringBrief());
        settingsRequestQueue.put(settingsRequest.getCenterFrequencyMHz(), DatagramFactory.copySettingsRequest(settingsRequest));
      }
    }
    // notify the request loop that we have new settings
    settingsInterrupt = true;
    try {
      synchronousQueue.take();
    } catch (InterruptedException e) {
    }
  }

  /**
   * Connect this Device Connector to a Device Adapter. <br>
   * If and only if a DUMMY port adapter is require, preceed the connect method
   * by setting the communications port to 'DUMMY' via
   * <p>
   * @param cpi
   * @link{setCommunicationsPort <br>
   * The proper Device Adapter (DUMMY or USB) is automatically created with this
   * Device Connector and the communications port is automatically set.<br>
   * This method is required by the DeviceConnector_Interface<br>
   * @return
   */
  public Boolean connect() {
    d.out(this, "connect");
    if (deviceAdapter != null) {
      if (this.isConnected()) {
        // We're already connected - no action required - sync just in case
        return true;
      }
    }
    // Create a USB Port adapter
//    this.deviceAdapter = new USBSerialPortAdapter(commPortIdentifier);
    if (deviceAdapter.isConnected() == false) {
      d.out(this, "Device Connector ERROR: USB Port Adapter failed to connect");
      return false;
    }
    // Add ourselves as a listener
    deviceAdapter.addDatagramListener(this);
    initializeAdapter(); // required to populate this object's hardware
    // description response object
    return true;
  }

  /**
   * UPDATE: 0410: Ping the hardware a few times when we start up to make sure
   * we get a good hardware description. If we don't, bad things happen when
   * trying to start the spectrum analyzer as we don't know what we're attached
   * to.
   * <p>
   * DEPRECATED: 0310 - hardware is initialized at the DeviceAdapter level, not
   * DeviceConnector_Avcom
   * <p>
   * Initialize the hardware by sending a HW description request
   */
  public void initializeAdapter() {
    d.out(this, "initialize");
    // Query the device for its status
    for (int i = 0; i < 4; i++) {
      synchronized (lock) {
        this.writeDatagram(new HardwareDescriptionRequest());
      }
    }
  }

  /**
   * Turn debug messaging on or off
   * <p>
   * @param d
   */
  public void setDebug(Boolean debug) {
    this.d = new Debug(debug);
  }

  /**
   * Is the Device Connector connected to a Device Adapter?
   * <p>
   * @return
   */
  public Boolean isConnected() {
    if (deviceAdapter == null) {
      return false;
    } else {
      return deviceAdapter.isConnected();
    }
  }

  /**
   * Add a datagram listener to receive datagrams from this Device Connector
   * <p>
   * @param datagramListener
   */
  public void addDatagramListener(IDatagramListener datagramListener) {
    d.out(this, "addDatagramListener: " + datagramListener.getClass().getSimpleName());
    this.datagramListeners.add(datagramListener);
  }

  public void removeDatagramListener(IDatagramListener datagramListener) {
    d.out(this, "addDatagramListener: " + datagramListener.getClass().getSimpleName());
    this.datagramListeners.remove(datagramListener);
  }

  /**
   * Add a change listener to receive notifications about the progress
   * <p>
   * @param changeListener
   */
  public void addProgressChangeListener(ChangeListener changeListener) {
    d.out(this, "addChangeListener");
    this.progressListeners.add(changeListener);
  }

  public void removeProgressChangeListener(ChangeListener changeListener) {
    d.out(this, "addChangeListener");
    this.progressListeners.remove(changeListener);
  }

  /**
   * Assemble a WaveformResponseExtended_Interface object from the
   * waveformResponseQueue, then notify all DeviceConnector_Listeners.<br>
   * Because some samples may overlap, we use a TreeMap, indexed on each sample
   * byte's own center frequency.<br>
   * In a waveform, each sample byte can be described by it's own center
   * frequency and span.<br>
   * Aggregated waveform data is stored at dBm values with centerFrequency
   * indices.<br>
   * (not yet) For conveneince, it it also available at an array of doubles
   * (double[])
   * <pre>
   * | <----------------------- span s --------------------> |
   * | ^ startFrequency           ^ centerFrequency          |
   * |[   ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]|
   * |  ^cfi
   * <p>
   * Where:
   *   startFrequency = centerFrequency - span / 2
   *   i = byte count (from 0 to length of sample
   *   cfi = resolutionBandwidth * i + startFrequency
   * </pre>
   */
  private WaveformResponseExtended assembleWaveform() {
    WaveformResponseExtended wre = null;
    // Assemble the waveform
    Iterator<IDatagram> waveformIterator = waveformResponseQueue.values().iterator();
    synchronized (waveformResponseQueue) {
      while (waveformIterator.hasNext()) {
        WaveformResponse wr = (WaveformResponse) waveformIterator.next();
        // Create the wrExtended if it doesn't exist yet
        if (wre == null) {
          wre = DatagramFactory.extendWaveformResponse(wr);
        }
        wre.putData(wr.getWaveformMap());
      }
      // Update the CF and Span by validating the new Datagram
      wre.setTransactionId(lock.getSerialNumber());
      wre.validate(settingsRequest); // trim extra or unwanted data
      wre.setSampleSize(waveformResponseQueue.size());
      // Empty the queue
      waveformResponseQueue.clear();
    }
    // Set the elapsed time
    wre.setElapsedTimeMS(stopWatch.getElapsedTimeMillis());
    return wre;
  }

  /**
   * Convert the bytebuffer into a datagram and send it to all registered
   * listeneres Notification is handled in a separate thread with a 50
   * millisecond interrupt
   * <p>
   * @param byteBuffer
   */
  private void notifyListeners(final IDatagram datagram) {
    long onDatagramTimeout = System.currentTimeMillis() + 50;
    if (datagram.isValid()) {
      for (final IDatagramListener l : datagramListeners) {
        /*
         * Instantiate a new thread to handle the notification. Interrupt it if
         * the thread hasn't returned fast enough
         */
        Thread t = new Thread(new Runnable() {

          public void run() {
            try {
              l.onDatagram(datagram);
            } catch (Exception e) {
              d.out("ERROR: DeviceConnector notify run thread caught exception on " + l.getClass().getSimpleName() + ".onDatagram()");
            }
          }
        });
        t.start();
        while (t.isAlive()) {
          if (System.currentTimeMillis() > onDatagramTimeout) {
            d.out("ERROR: DeviceConnector notify run thread timed out on " + l.getClass().getSimpleName() + ".onDatagram()");
            t.interrupt();
            try {
              t.join(100);
            } catch (InterruptedException e) {
            }
          }
        }
      }
      // Update the counter of waveforms produced
      if (waveformProduced++ > Integer.MAX_VALUE - 100) {
        waveformProduced = 1;
      }
    } else {
      d.out(this, "ERROR: notifyListeners got invalid Datagram");
      // Update the counter of invalid datagrams
      if (datagramInvalid++ > Integer.MAX_VALUE - 100) {
        datagramInvalid = 1;
      }
    }
    // Wait until we've received new settings
    try {
      synchronousQueue.put("notifyListeners");
    } catch (InterruptedException e) {
    }
  }

  /**
   * Action required when we've received a datagram<br>
   * This method must notify the lock object via 'lock.notify()'<br>
   */
  public void onDatagram(IDatagram datagram) {
    // d.out(this, "onDatagram " + datagram.getSensorTypeId());
    // d.out(this, "onDatagram type " + datagram.getSensorTypeId() + " time " +
    // s.getElapsedTimeMillis());
    // d.out(this,"onDatagram " + datagram.getSensorTypeId());
    // --------------------------------------------------------------------------
    // Note the max/min/ave running stats
    // Increment the received counter
    if (this.datagramReceived++ > Integer.MAX_VALUE - 100) {
      this.datagramReceived = 1;
    }
    // Record the elapsed time
    int elapsedTimeMS = (int) (Calendar.getInstance().getTimeInMillis() - requestOutTime);
    // Set the elapsed time counters , initializ them first if necessary
    if ((elapsedTimeMax == 0) || (elapsedTimeMin == 0) || (elapsedTimeAve == 0)) {
      elapsedTimeMax = elapsedTimeMS;
      elapsedTimeMin = elapsedTimeMS;
      elapsedTimeAve = elapsedTimeMS;
    } else {
      elapsedTimeMax = elapsedTimeMax > elapsedTimeMS ? elapsedTimeMax : elapsedTimeMS;
      elapsedTimeMin = elapsedTimeMin < elapsedTimeMS ? elapsedTimeMin : elapsedTimeMS;
      elapsedTimeAve = (elapsedTimeAve * (this.datagramReceived - 1) + elapsedTimeMS) / datagramReceived;
    }
    // Debug output
    if (datagram.getSensorTypeId() == IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID) {
      d.out(this, "onDatagram HARDWARE_DESCRIPTION_RESPONSE_ID [" + elapsedTimeMS + "] " + ((HardwareDescriptionResponse) datagram).toStringBrief());
      d.out(this, "onDatagram HARDWARE_DESCRIPTION_RESPONSE_ID [" + elapsedTimeMS + "] " + ((HardwareDescriptionResponse) datagram).toString());
    }
    // if (datagram.getSensorTypeId() == Datagram.WAVEFORM_RESPONSE_ID) {
    // d.out(this, "onDatagram WAVEFORM_RESPONSE_ID [" + lock.getSerialNumber()
    // + "] " + ((WaveformResponse) datagram).toStringBrief());
    // }
    // if (datagram.getSensorTypeId() == Datagram.WAVEFORM_RESPONSE_12BIT_ID) {
    // d.out(this, "onDatagram WAVEFORM_RESPONSE_12BIT_ID [" + elapsedTimeMS +
    // "] " + ((WaveformResponse12Bit) datagram).toStringBrief());
    // }
    // else {
    // d.out(this, "onDatagram UNKNOWN [" + elapsedTimeMS + "] " +
    // datagram.getSensorTypeId());
    // }
    /**
     * We only expect to receive the following types of datagrams and take the
     * following actions: NOTE: This must also be synchronized with LOCK to
     * work, else everything stalls Waveform 8 & 12 bit : Add it to the
     * waveformResponseQueue Hardware description: Update our own Error : Log
     * and forward to listeners
     */
    synchronized (lock) {
      switch (datagram.getSensorTypeId()) {
        case IDatagram.WAVEFORM_RESPONSE_ID:
          WaveformResponse wr = (WaveformResponse) datagram;
          wr.setElapsedTimeMS(elapsedTimeMS);
          wr.setTransactionId(lock.getSerialNumber());
          // Add it to the queue
          waveformResponseQueue.put(wr.getCenterFrequencyMHz(), wr);
          // Update our percent complete
          percentComplete = (double) waveformResponseQueue.size() / (double) settingsRequestQueue.size();
          fireProgressChange();
          // If the queue is full, send a complete waveform
          if (waveformResponseQueue.size() == settingsRequestQueue.size()) {
            notifyListeners(assembleWaveform());
          }
          break;
        case IDatagram.WAVEFORM_RESPONSE_12BIT_ID:
          WaveformResponse12Bit wrb = (WaveformResponse12Bit) datagram;
          wrb.setTransactionId(lock.getSerialNumber());
          // Add it to the queue
          waveformResponseQueue.put(wrb.getCenterFrequencyMHz(), wrb);
          if (waveformResponseQueue.size() == settingsRequestQueue.size()) {
            // If the queue is full, send a complete waveform
            notifyListeners(assembleWaveform());
          }
          break;
        case IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID:
          this.hardwareDescription = (HardwareDescriptionResponse) datagram;
          break;
        case IDatagram.ERROR_RESPONSE_ID:
          datagram.setTransactionId(lock.getSerialNumber());
          notifyListeners(datagram);
          break;
      }
      // --------------------------------------------------------------------------
      // Release the lock for another request
      this.notified = true;
      lock.notify();
    }
  }

  // --------------------------------------------------------------------------
  // Release the lock for another request
  /**
   * Write a datagram to the Device - This is the default method to communicate
   * with the device
   * <p>
   * @param datagram
   * @return
   */
  public Boolean writeDatagram(IDatagram datagram) {
    // Set the clock
    requestOutTime = Calendar.getInstance().getTimeInMillis();
    // Write the datagram
    try {
      synchronized (lock) {
        this.notified = false;
        // Write the datagram
        deviceAdapter.write(datagram);
        // Wait for the datagram response
        switch (datagram.getSensorTypeId()) {
          case IDatagram.WAVEFORM_REQUEST_ID:
            assert datagram.getSensorTypeId() == 3;
            lock.wait(NOTIFICATION_TIMEOUT_WAVEFORM);
            break;
          case IDatagram.SETTINGS_REQUEST_ID:
            assert datagram.getSensorTypeId() == 4;
            lock.wait(NOTIFICATION_TIMEOUT_SETTINGS);
            break;
          case IDatagram.HARDWARE_DESCRIPTION_REQUEST_ID:
            assert datagram.getSensorTypeId() == 7;
            lock.wait(NOTIFICATION_TIMEOUT_HWDESCRIPTION);
            break;
          default:
            lock.wait(NOTIFICATION_TIMEOUT);
            break;
        }
      }
    } catch (Exception ex) {
      d.out(this, "writeDatagram EXCEPTION: " + ex.toString() + " : " + datagram.toStringBrief());
      return false;
    } // Increment the counter
    if (datagramSent++ > Integer.MAX_VALUE - 100) {
      datagramSent = 1;
    }
    return true;
  }

  private final StopWatch stopWatch = new StopWatch();

  /**
   * Request an extended WaveformResponse and mark the returned Datagram with
   * the specified serial number <br>
   * This iterates through all internal settings requests
   * <p>
   * @param serialNumber
   * @return
   */
  public synchronized Boolean requestWaveform(String serialNumber) {
    // Update the request counter
    if (this.waveformRequested++ > Integer.MAX_VALUE - 100) {
      waveformRequested = 1;
    } // Settings must be set, as the tell us what waveforms to collect
    if (settingsRequestQueue.isEmpty()) {
      d.out(this, "ERROR: getWaveform: Settings Request Queue is Empty.");
      try {
        Thread.sleep(QUEUE_TIMEOUT);
      } catch (InterruptedException e) {
      }
      return false;
    } // Clear the response queue
    waveformResponseQueue.clear();
    // Request a sequence of waveforms to accommodate the requested settings
    if (this.notified) {
      // Start the stopwatch to measure elapsed time.
      stopWatch.start();
      // Build a new list of Datagrams, interleaving settings with waveforms
      Vector<IDatagram> requestVector = new Vector<>();
      Iterator<SettingsRequest> settingsIterator = settingsRequestQueue.values().iterator();
      synchronized (settingsRequestQueue) {
        while (settingsIterator.hasNext()) {
          SettingsRequest sr = settingsIterator.next();
          requestVector.add(sr);
          requestVector.add(new WaveformRequest());
        }
      }
      // Burn through the waveform requests
      Iterator<IDatagram> requestIterator = requestVector.iterator();
      while (requestIterator.hasNext()) {
        synchronized (lock) {
          // Set the serial number
          lock.setSerialNumber(serialNumber);
          this.writeDatagram(requestIterator.next());
          // exit the loop if new settings have been received (this improves GUI
          // response)
          if (settingsInterrupt) {
            settingsInterrupt = false;
            break;
          }
        }
      }
      return true;
    } else {
      // We did not receive a proper message - reset
      synchronized (lock) {
        // Set the serial number
        lock.setSerialNumber(serialNumber);
        this.writeDatagram(new HardwareDescriptionRequest());
      } // Increment the timeout counter
      if (datagramTimeouts++ > Integer.MAX_VALUE - 100) {
        datagramTimeouts = 1;
      }
      return true;
    }
  }

  /**
   * When requesting a very large waveform, tracks the percent complete
   * <p>
   * @return
   */
  public int getPercentComplete() {
    return (int) (percentComplete * 100);
  }

  /**
   * Set the comm port identifier. if not null, this device connector will
   * instruct its USBSerialPortAdapter to use this CommPortIdentifier
   * <p>
   * @param commPortIdentifier the commPortIdentifier to set
   */
//  public void setComPortIdentifier(CommPortIdentifier comPortIdentifier) {    this.commPortIdentifier = comPortIdentifier;  }
  /**
   * @return the commPortIdentifier
   */
//  public CommPortIdentifier getComPortIdentifier() {    return commPortIdentifier;  }
  /**
   * Let all our listeners know we have a progress update
   */
  private void fireProgressChange() {
    for (ChangeListener changeListener : progressListeners) {
      changeListener.stateChanged(new ChangeEvent(this));
    }
  }

  /**
   * Request an extended WaveformResponse with serial number set to zero "0".
   * This is typically used for GUI applications
   * <p>
   * @return
   */
  public Boolean requestWaveform() {
    return this.requestWaveform("0");
  }

  /**
   * Disconnect from the physical port.
   * <p>
   * @return
   */
  public Boolean disconnect() {
    if (deviceAdapter.isConnected()) {
      deviceAdapter.disconnect();
    }
    // Wait for the port to reconnect
    try {
      d.out(this, "disconnect: sleep " + NOTIFICATION_TIMEOUT + " for disconnect");
      Thread.sleep(NOTIFICATION_TIMEOUT);
    } catch (InterruptedException ex) {
      Logger.getLogger(DeviceConnector_Avcom.class.getName()).log(Level.SEVERE, null, ex);
    }
    return deviceAdapter.isConnected();
  }

  /**
   * Disconnect & reconnect to the port
   * <p>
   * @return
   */
  public Boolean reconnect() {
    if (disconnect()) {
//      deviceAdapter.connect(commPortIdentifier);
    }
    if (this.deviceAdapter.isConnected()) {
//      d.out(this, "Reconnected to " + deviceAdapter.getComPortIdentifier().getName());
      return true;
    } else {
      d.out(this, "Reconnection Failed");
      return false;
    }
  }

  /**
   * Get the run-time status of the sensor
   * <p>
   * @return
   */
  public Map<String, Object> getStatus() {
    d.out(this, "getStatus");
    Map<String, Object> status = Collections.synchronizedMap(new HashMap<String, Object>());
    synchronized (status) {
      // Add information from Status Counters
      status.put("device.datagram.received", this.datagramReceived);
      status.put("device.datagram.sent", this.datagramSent);
      status.put("device.datagram.timeouts", this.datagramTimeouts);
      status.put("device.datagram.time.max", this.elapsedTimeMax);
      status.put("device.datagram.time.min", this.elapsedTimeMin);
      status.put("device.datagram.time.ave", this.elapsedTimeAve);
      status.put("device.waveform.requested", this.waveformRequested);
      status.put("device.waveform.produced", this.waveformProduced);
      // Add hardware descriptions
      status.put("device.model", hardwareDescription.getProductId().getModel());
      status.put("device.firmware.version.major", hardwareDescription.getFirmwareVersionMajor());
      status.put("device.firmware.version.minor", hardwareDescription.getFirmwareVersionMinor());
      // description.put("current.center.frequency.mhz",
      // hardwareDescription.getCurrentCenterFrequencyMHz());
      // description.put("current.span.mhz",
      // hardwareDescription.getCurrentSpanMHz());
      // description.put("current.reference.level.dbm",
      // hardwareDescription.getCurrentReferenceLevel().getWaveformOffset() +
      // 40);
      // description.put("current.rbw.mhz",
      // hardwareDescription.getCurrentRBW().getMHz());
      // description.put("current.rf.input",
      // hardwareDescription.getCurrentRFInput());
      // description.put("device.available.rf.inputs",
      // hardwareDescription.getAvailableRFInputs());
      // description.put("current.splitter.gain.calibration",
      // hardwareDescription.getCurrentSplitterGainCal());
      status.put("device.has.splitter", hardwareDescription.getSplitterBits());
      // description.put("current.communications.port",
      // hardwareDescription.getCurrentComPort());
      // description.put("device.available.communications.ports",
      // hardwareDescription.getAvailableComPorts());
      // description.put("current.internal.ext.freq",
      // ByteUtil.toString(hardwareDescription.getCurrentInternalExtFreq()));
      // description.put("current.internal.ext",
      // ByteUtil.toString(hardwareDescription.getCurrentInternalExt()));
      status.put("device.calibration.date", hardwareDescription.getCalibrationDate());
      status.put("device.serial.number", hardwareDescription.getSerialNumber());
      status.put("device.board.revision", hardwareDescription.getPcbRevision());
      status.put("device.temperature", hardwareDescription.getBoardTemperature());
      status.put("device.temperature.min", hardwareDescription.getBoardTemperatureMin());
      status.put("device.temperature.max", hardwareDescription.getBoardTemperatureMax());
      status.put("device.has.lnb.power", hardwareDescription.getHasLNBPower());
      if (hardwareDescription.getHasLNBPower()) {
        status.put("device.lnb.power.setting.bits", Integer.toBinaryString(hardwareDescription.getCurrentLNBPowerSetting()));
        status.put("device.lnb.power.voltage", hardwareDescription.getLNBVoltage());
        status.put("device.lnb.power.22kHz.clock.on", hardwareDescription.getLNBReferenceClockIsOn());
        status.put("device.lnb.power.disabled", hardwareDescription.getLNBDisablePower());
      }
      // description.put("device.is.first.run",
      // hardwareDescription.getIsFirstRun());
      status.put("device.is.locked", hardwareDescription.getIsLocked());
      status.put("device.board.project.id", Integer.toHexString(hardwareDescription.getProjectID()));
      // description.put("availableRBW",
      // hardwareDescription.getAvailableRBW().getMHz().getLabel());
      // status.put("calDay", hardwareDescription.getCalibrationDay());
      // status.put("calMonth", hardwareDescription.getCalibrationMonth());
      // status.put("calYear", hardwareDescription.getCalibrationYear());
      // description.put("reserved01", hardwareDescription.getReserved01());
      status.put("device.frequency.response.max", hardwareDescription.getMaximumFrequencyResponse());
      status.put("device.frequency.response.min", hardwareDescription.getMinimumFrequencyResponse());
      status.put("device.frequency.span.max", hardwareDescription.getMaximumFrequencySpan());
      status.put("device.frequency.span.min", hardwareDescription.getMinimumFrequencySpan());
      status.put("device.frequency.span.step", hardwareDescription.getMinimumFrequencySpanStep());
//      status.put("device.serial.port", deviceAdapter.getComPortIdentifier().getName());
    }
    return status;
  }

  // Runnable Impl ------------------------------------------------------------
  private Boolean isrunning = false;
  private Thread runThread;
  private long QUEUE_TIMEOUT = 25;

  public void startThread() {
    d.out(this, "Start run thread");
    this.isrunning = true;
    runThread = new Thread(this, "DeviceConnector_Avcom Run Thread");
    runThread.start();
  }

  public void stopThread() {
    d.out(this, "Stop run thread");
    this.isrunning = false;
    runThread.interrupt();
  }

  @Override
  public void run() {
    d.out(this, "run");
    while (isrunning) {
      if (!settingsRequestQueue.isEmpty()) {
        this.requestWaveform(settingsRequest.getTransactionId());
      } else {
        // If the queue is empty then we've caught it in the middle of being
        // updated. Wait a bit and try again
        try {
          Thread.sleep(QUEUE_TIMEOUT);
        } catch (InterruptedException ex) {
        }
      }
    }
  }
}
