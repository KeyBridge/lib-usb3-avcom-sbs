package com.keybridgeglobal.sensor;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomfova.sbs.datagram.IWaveformResponseExtended;
import com.keybridgeglobal.sensor.avcom.datagram.DatagramFactory;
import com.keybridgeglobal.sensor.avcom.datagram.WaveformResponseExtended;
import com.keybridgeglobal.sensor.enums.EUSBDeviceType;
import com.keybridgeglobal.sensor.interfaces.*;
import com.keybridgeglobal.sensor.util.Debug;
import com.keybridgeglobal.sensor.util.SoftwareDescription;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import javax.swing.event.ChangeListener;

/**
 * Implementation supporting the Avcom RSA2500B Spectrum Analyzer.
 * <p>
 * 0410: changed jobQueue to ConcurrentHashMap for OSGI thread safety
 * <p>
 * @author jesse
 */
public class SpectrumAnalyzer_Avcom implements ISpectrumAnalyzer, IDatagramListener, Runnable {

  private Debug d = new Debug(false);
  // Listeners
  private final List<ISpectrumAnalyzerListener> spectrumAnalyzerListeners = Collections.synchronizedList(new ArrayList<ISpectrumAnalyzerListener>());
  private final List<ChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<ChangeListener>());
  // Spectrum Analyzer Equipment and Status
  private IDeviceConnector deviceConnector;
//  private CommPortIdentifier                                   commPortIdentifier;
  // Descriptor of this sensor
  private SensorEntity sensorEntity = new SensorEntity();
  // Spectrum Analyzer Analytics
  private final Analytics analytics = new Analytics();
  // For measuring the job sample time
  private long requestOutTimeMS;
  // Spectrum Analyzer Operation
  private Boolean isRunning = false;
  private Boolean runThread;
  private Thread captureThread;
  private final ConcurrentHashMap<String, ISensorJob> jobQueue = new ConcurrentHashMap<>();
  private String sensorSerialNumber;
  // SynchronousQueue acts as our lock object between job requests
  private SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();

  // private static final String TYPE = "AVCOM";
  /**
   * Empty constructor. Use this constructor if you want to use a DummyDevice.
   * To use it, follow the following steps:
   * <ul>
   * <li>add a CommPortIdentifier with {@link setCommPortIdentifier}
   * <ul>
   * <li>If using DummyDevice, set CommPortIdentifier to null.
   * <li>call {@link useDummyAdapter}
   * </ul>
   * <li>initialize with {@link initialize}
   * <li>add a jobs with {@link addJob} (optional)
   * <li>start the capture thread with {@link startCapture}
   * </ul>
   */
  public SpectrumAnalyzer_Avcom() {
//    new SpectrumAnalyzer_Avcom(null);
  }

  /**
   * Full constructor. <br>
   * Actions: Initializes the device, adds the default Job
   * (SimpleJob.getDefault) and starts the capture thread. <br>
   * DEPRECATED: If no job is present the capture thread sleeps checks until a
   * job is added. <br>
   * DEPRECATED: If no job is present the system defaults to wide-band spectrum
   * survey.
   * <p>
   * @param commPortIdentifier
   */
//  public SpectrumAnalyzer_Avcom(CommPortIdentifier commPortIdentifier) {
//    this.commPortIdentifier = commPortIdentifier;
//    initialize();
//    //
//    addJob(SimpleJob.getDefault());
//    startCapture();
//    // Add a shutdown hook to close the port when we're shutting down
//    Runtime.getRuntime().addShutdownHook(new Thread() {
//
//      @Override
//      public void run() {
//        d.out("Shutting down: SpectrumAnalyzer_Avcom");
//        stopCapture();
//        // SensorEntity se = buildSensorEntity();
//        // d.out("\n\n" + se.toString());
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//        }
//      }
//    });
//  }
  /**
   * Initialize the spectrum analyzer device connectors. <br>
   * Sets the communications port and connects the device connector if not
   * already done<br>
   * Note that the communicationsPort must be set before calling initialize.
   */
  public Boolean initialize() {
    d.out(this, "initializeDetector");
//    deviceConnector = new DeviceConnector_Avcom(commPortIdentifier);
    // deviceConnector.setDebug(debug);
    deviceConnector.addDatagramListener(this);
    return true;
  }

  /**
   * Disconnect the device connector. <br>
   * This method is required to support the OSGI lifecycle, where this spectrum
   * analyzer may be started or stopped. When stopped, we should free up the
   * serial port.
   */
  public void disconnect() {
    deviceConnector.disconnect();
  }

  /**
   * @param commPortIdentifier the commPortIdentifier to set
   */
//  public void setCommPortIdentifier(CommPortIdentifier commPortIdentifier) {
//    this.commPortIdentifier = commPortIdentifier;
//  }
  /**
   * @return the commPortIdentifier
   */
//  public CommPortIdentifier getCommPortIdentifier() {
//    return commPortIdentifier;
//  }
  /**
   * Forces the device connector to use a dummy port adapter instead of a real
   * device adapter
   * <p>
   * @param useDummyAdapter
   */
  public void useDummyAdapter(boolean useDummyAdapter) {
  }

  // Job Management -----------------------------------------------------------
  /**
   * Add a pre-defined SettingsRequest to the Job Queue
   * <p>
   * @param job a Jobs object
   */
  public void addJob(ISensorJob job) {
    d.out(this, "addJob " + job.toString());
    this.jobQueue.put(job.getSnJob(), job);
    fireChange();
  }

  /**
   * Remove a Job identified by serialNumber. If the job is not here, no action
   * is taken.
   * <p>
   * @param serialNumber
   */
  public void removeJob(String serialNumber) {
    if (this.jobQueue.containsKey(serialNumber)) {
      d.out(this, "removeJob " + serialNumber);
      this.jobQueue.remove(serialNumber);
      fireChange();
    }
  }

  /**
   * Returns the entire Job queue
   * <p>
   * @return
   */
  public Collection<ISensorJob> getJobs() {
    return jobQueue.values();
  }

  /**
   * Remove all jobs from the queue and clear all job listeners;
   */
  public void removeAllJobs() {
    d.out(this, "removeAllJobs");
    this.jobQueue.clear();
    fireChange();
  }

  // ----------------------------------------------------------------------------
  // Operations management
  /**
   * Start the Device Connector thread. This will fetch waveforms for each
   * SettingsRequest in the Queue settingsRequestQueue
   */
  public void startCapture() {
    d.out(this, "Start capture thread");
    this.runThread = true;
    captureThread = new Thread(this, "Avcom Capture Tread");
    captureThread.start();
    deviceConnector.startThread();
  }

  /**
   * Start the Device Connector thread. This will tell the thread to stop and
   * also send it an interrupt.
   */
  public void stopCapture() {
    d.out(this, "Stopping capture thread");
    this.runThread = false;
    captureThread.interrupt();
    deviceConnector.stopThread();
  }

  /**
   * Is the capture thread running?
   */
  public Boolean isRunning() {
    return this.isRunning;
  }

  /**
   * Turn debug messages on or off
   */
  public void setDebug(boolean debug) {
    this.d = new Debug(debug);
  }

  /**
   * Run a separate capture thread. If jobs exist in the queue, they will be
   * run. If no jobs are present, this thread will sleep and take no action.
   * This thread rechecks the jobQuee every {@link JOB_SLEEP_TIMER} ms.
   * <p>
   * DO NOT USE DIRECTLY: You will likely spawn a zombie thread. Instead, <br>
   * - To start the capture thread, use {@link startCapture}. <br>
   * - To stop the capture thread, use {@link stopCapture}
   */
  public void run() {
    this.isRunning = true;
    // Begin the main capture loop
    while (this.runThread) {
      if (jobQueue.isEmpty()) {
        // we caught the job queue in the middle of a refresh
        d.out(this, "Avcom Capture Tread caught empty job queue");
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
        }
      } else {
        synchronized (jobQueue) {
          for (ISensorJob runJob : jobQueue.values()) {
            if (isReadyTimer(runJob.getSnJob())) {
              // d.out(this, "Avcom Capture Tread pushing job [" +
              // runJob.getSnJob() + "] to device connector");
              this.requestOutTimeMS = System.currentTimeMillis();
              this.deviceConnector.setSettings(DatagramFactory.getSettingsRequest(runJob));
              setReadyTimer(runJob.getSnJob());
            }
            // Wait for the job to finish before starting the next
            try {
              synchronousQueue.put(runJob.getSnJob());
            } catch (InterruptedException ex) {
            }
          }
        }
      }// End main capture loop
    }
    this.isRunning = false;
    d.out(this, "Capture thread stopped");
  }

  // Job Period Timing Management ---------------------------------------------
  private TreeMap<String, Long> jobReadyTimer = new TreeMap<>();

  /**
   * Indicate that this job has just been run
   * <p>
   * @param jobSerialNumber
   */
  private void setReadyTimer(String jobSn) {
    jobReadyTimer.put(jobSn, System.currentTimeMillis());
  }

  /**
   * Has enough time passed for this job to run again?
   * <p>
   * @param jobSn
   * @return
   */
  private boolean isReadyTimer(String jobSn) {
    if (jobReadyTimer.get(jobSn) == null) {
      jobReadyTimer.put(jobSn, System.currentTimeMillis());
      return true;
    } else {
      synchronized (jobQueue) {
        return (System.currentTimeMillis() - jobReadyTimer.get(jobSn)) > jobQueue.get(jobSn).getPeriod();
      }
    }
  }

  // Listener Management ------------------------------------------------------
  /**
   * Add a listener to the specified job
   * <p>
   * @param saListener
   * @param jobSerialNumber
   */
  public void addSpectrumAnalyzerListener(ISpectrumAnalyzerListener saListener) {
    spectrumAnalyzerListeners.add(saListener);
    d.out(this, "addSpectrumAnalyzerListener " + saListener.getClass().getSimpleName());
  }

  public void removeSpectrumAnalyzerListener(ISpectrumAnalyzerListener saListener) {
    spectrumAnalyzerListeners.remove(saListener);
    d.out(this, "removeSpectrumAnalyzerListener " + saListener.getClass().getSimpleName());
  }

  /**
   * All directly registered listeners are updated by calling their onSADatagram
   * method. If a JMSMessenger is present, then the waveform datagram is also
   * sent to the network message queue.
   * <p>
   * @param datagram Either a WaveformResponse or
   *                 WaveformResponseExtended_Interface datagram object
   */
  private void notifyListeners(final IWaveformResponseExtended datagram) {
    long notifyTimeout = System.currentTimeMillis() + 100;
    // d.out(this, "notifyListeners " + datagram.toStringBrief() + " time [" +
    // datagram.getElapsedTimeMS() + "]");
    for (final ISpectrumAnalyzerListener l : spectrumAnalyzerListeners) {
      /*
       * Instantiate a new thread to handle listener notification. Interrupt the
       * thread if it has not returned fast enough
       */
      Thread t = new Thread(new Runnable() {

        public void run() {
          try {
            l.onSADatagram(datagram);
          } catch (Exception e) {
            d.out("ERROR: notifyListeners run thread caught exception on " + l.getClass().getSimpleName() + ".onSADatagram");
          }
        }
      }, "Spectrum Analyzer Notify Thread");
      t.start();
      while (t.isAlive()) {
        if (System.currentTimeMillis() > notifyTimeout) {
          // d.out("ERROR: notifyListeners run thread timed out for " +
          // l.getClass().getSimpleName() + ".onSADatagram");
          t.interrupt();
        }
      }
    }
    // Update the waveform sent counter
    analytics.notifyWaveformSent();
  }

  /**
   * Notify listeners that something has changed
   */
  private void fireChange() {
    for (ChangeListener l : changeListeners) {
      d.out(this, "fireChange > " + l.getClass().getSimpleName());
      l.stateChanged(null);
    }
  }

  // ----------------------------------------------------------------------------
  // Set Methods
  // ----------------------------------------------------------------------------
  // Request and Handle Datagrams from the Hardware
  /**
   * Request a waveform, setting its serialnumber to the indicated value<br>
   * Serial numbers may be matched to the responses
   * <p>
   * @param serialNumber
   * @return
   */
  public void requestWaveform(String serialNumber) {
    this.requestOutTimeMS = System.currentTimeMillis();
    d.out(this, "requestWaveform " + serialNumber + " timeout " + requestOutTimeMS);
    this.deviceConnector.requestWaveform(serialNumber);
  }

  /**
   * Shortcut to request a waveform. This method sets the serial number to zero
   * <br>
   * It is useful when directly attaching or embedding within a GUI client.
   * <p>
   * @return
   */
  public void requestWaveform() {
    this.requestWaveform("0");
  }

  /**
   * Action to take when a datagram is received. We only expect to receive
   * WaveformResponseExtended_Interface datagrams. <br>
   * Other parameters (settings, hardware description, etc.) are exchanged by
   * getStatus/set settings.
   * <p>
   * @param datagram default is a WaveformResponseExtended_Interface unless
   *                 WaveformResponse if sensor.streaming.mode is 'yes'
   */
  public void onDatagram(IDatagram datagram) {
    // d.out(this, "onDatagram " + datagram.getSensorTypeId());
    // Create and configure the waveform parameters
    WaveformResponseExtended wre = (WaveformResponseExtended) datagram;
    wre.setElapsedTimeMS((int) (System.currentTimeMillis() - requestOutTimeMS));
    wre.setSensorSerialNumber(getSerialNumber());
//    wre.setSensorPortName(commPortIdentifier.getName());
    notifyListeners(wre);
    // d.out(this, "enter analytics notifyListeners");
    // analytics.update(wre);
    // d.out(this, "exit analytics notifyListeners");
    try {
      // take from the SynchronousQueue - this allows us to put new settings
      synchronousQueue.take();
    } catch (InterruptedException ex) {
    }
  }

  /**
   * Get the lastest sensor status
   * <p>
   * @return
   */
  public ISensorEntity getSensorEntity() {
    this.sensorEntity = buildSensorEntity();
    return this.sensorEntity;
  }

  /**
   * Get the sensor serial number. This method updates the sensorEntity if it is
   * null. Else, it returns the serial number from the current sensorEntity.
   * <p>
   * @return
   */
  public String getSerialNumber() {
    if (this.sensorSerialNumber == null || this.sensorEntity == null) {
      this.sensorEntity = buildSensorEntity();
      this.sensorSerialNumber = sensorEntity.getDeviceSerialNumber();
    }
    return this.sensorSerialNumber;
  }

  /**
   * Return a description of the hardware and software in HashMap format. It it
   * called by
   * <p>
   * @look{getSensorStatus
   * @return
   */
  private SensorEntity buildSensorEntity() {
    d.out(this, "buildSensorEntity");
    // First populate a map of parameters
    // Object will be either a String or number
    Map<String, Object> sensorStatus = new HashMap<>();
    SensorEntity se = new SensorEntity();
    // surround with a try/catch as the deviceconnector may not be connected
    try {
      // Add hardware parameters
      sensorStatus.putAll(this.deviceConnector.getStatus());
      sensorStatus.putAll(this.analytics.getStatus());
      sensorStatus.putAll(new SoftwareDescription().getStatus());
      // Then build and populate a new SensorEntity
      se.initialize(sensorStatus);
      se.setDeviceType(EUSBDeviceType.AVCOM.name());
      se.setJobQueue(jobQueue.values());
      // TODO getStatus local IP address and public internet ip address
      // TODO getStatus system operating params like CPU type, meminfo, process
      // load, etc.
      // See for more details:
      // http://www.redhat.com/docs/manuals/enterprise/RHEL-4-Manual/en-US/Reference_Guide/s2-proc-uptime.html
      // Update our own sensor entity to reflect the latest status
      // se.setDeviceComPort(getCommPort());
      this.sensorEntity = se;
    } catch (Exception e) {
      d.out(this, "buildSensorEntity threw exception: " + e.getClass().getSimpleName());
      e.printStackTrace();
    }
    return se;
  }

  @Override
  public String toString() {
    return buildSensorEntity().toString();
  }

  // Change Listener Impl -----------------------------------------------------
  public void addChangeListener(ChangeListener listener) {
    d.out(this, "addChangeListener " + listener.getClass().getSimpleName());
    changeListeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    d.out(this, "removeChangeListener " + listener.getClass().getSimpleName());
    changeListeners.remove(listener);
  }

  /**
   * Get the serial communications port this sensor is attached to
   */
  public String getCommPort() {
//    return this.commPortIdentifier.getName();
    return "";
  }

  /**
   * Set the job list this Spectrum Analyzer should run
   * <p>
   * @param jobs
   */
  public void setJobs(List<ISensorJob> jobs) {
    d.out(this, "setJobs [" + jobs.size() + "] ");
    boolean modifiedJobQueue = false;
    if (jobs == null) {
      d.out(this, "  ERROR: received null jobs list... [" + this.sensorSerialNumber + "] flushing all jobs and reverting to default");
      this.removeAllJobs();
      this.addJob(SimpleJob.getDefault());
      return;
    } else {
      List<String> persistedKeys = new ArrayList<>();
      // Add new jobs
      for (ISensorJob job : jobs) {
        // Console message
        if (this.sensorSerialNumber.contains(job.getSnSensor())) {
          d.out(this, "  [" + this.sensorSerialNumber + "] add/update job " + job.toStringBrief());
          // put the keys in a list for the next step: trimming
          persistedKeys.add(job.getSnJob());
          // add it if not already here
          // if (!jobQueue.containsKey(job.getSnJob())) {
          // d.out(this, "  sensor serial number matched j [" +
          // job.getSnSensor() + "] s [" + this.sensorSerialNumber + "] p [" +
          // this.commPortIdentifier.getName() + "]");
          // d.out(this, "  replacing job already in queue with new job " +
          // job.toString());
          this.removeJob(job.getSnJob());
          this.addJob(job);
          modifiedJobQueue = true;
          // }
          // else {
          // d.out(this, "    sensor serial number not matched j [" +
          // job.getSnSensor() + "] s [" + this.sensorSerialNumber + "] p [" +
          // this.commPortIdentifier.getName() + "]");
          // }
        }
      }
      // Trim any running jobs that have been removed
      d.out(this, "  Trim old jobs");
      for (String jobSN : jobQueue.keySet()) {
        if (!persistedKeys.contains(jobSN)) {
          d.out(this, "    Running job inactivated: " + jobQueue.get(jobSN).toString());
          this.removeJob(jobSN);
          modifiedJobQueue = true;
        }
      }
      // Error check: If no running jobs then run the default job
      // if (jobQueue.size() == 0) {
      // d.out(this, "INFO: jobqueue is empty - adding default");
      // this.addJob(SimpleJob.getDefault());
      // modifiedJobQueue = true;
      // }
      // dump the jobqueue
      d.out(this, "  INFO: Current Job Queue:");
      for (ISensorJob q : jobQueue.values()) {
        d.out(this, "    " + q.toString());
      }
    }
    // Housekeeping: Free up the job-runner blocking queue just in case
    if (modifiedJobQueue) {
      d.out(this, "  Job Queue modified - freeing the synchronousQueue");
      try {
        synchronousQueue.take();
      } catch (InterruptedException e) {
      }
    }
  }
}
