package com.keybridgeglobal.sensor;

import com.avcomfova.sbs.datagram.IDatagram;
import com.keybridgeglobal.sensor.util.Debug;
import java.util.*;

/**
 * Analytics package to study the incoming waveforms and produce useful results
 * <p>
 * @author jesse
 */
public class Analytics {

  // A running tally of the Sensor Entities we are aware of
  private final Map<String, SensorEntity> sensorEntitiesMap = new TreeMap<>();
  private Map<String, TreeMap<Long, IDatagram>> waveformQueuesbyJob = new HashMap<>();
  private Map<String, TreeMap<Double, Double>> maxWaveformByJob = new HashMap<>();
  private Map<String, TreeMap<Double, Double>> minWaveformByJob = new HashMap<>();
  private Map<String, TreeMap<Double, Double>> aveWaveformByJob = new HashMap<>();
  // Performance measurements
  private final Map<String, Integer> aveDurationPerJob = Collections.synchronizedMap(new HashMap<String, Integer>());
  private final Map<String, Integer> maxDurationPerJob = Collections.synchronizedMap(new HashMap<String, Integer>());
  private final Map<String, Integer> minDurationPerJob = Collections.synchronizedMap(new HashMap<String, Integer>());
  private int aveDuration = 0;
  private int maxDuration = Integer.MIN_VALUE;
  private int minDuration = Integer.MAX_VALUE;
  // Should we collect detailed statistics for each job
  private Boolean detailedStatistics = true;
  // Operating Statistics
  private Calendar startedRunning;
  private int waveformReceivedCount;
  private int waveformSentCount;
  // How many waveforms to store in each job queue
  private int JOB_QUEUE_DEPTH = 100;
  private Debug d = new Debug(false);

  public Analytics() {
    d.out(this, "Analytics");
    // Note that we've started running.
    if (this.startedRunning == null) {
      this.startedRunning = Calendar.getInstance();
    }
  }

  /**
   * Receive and process a waveform
   * <p>
   * @param datagram WaveformResponseExtended_Interface
   */
  public void update(IDatagram datagram) {
    d.out(this, "update enter");
//    WaveformResponseExtended wre = (WaveformResponseExtended) datagram;
    // This datagrams serialnumber
    Long serialNum = datagram.getTransactionId();
    // Calculate the statistic params
    if (this.waveformReceivedCount == Integer.MAX_VALUE - 1) {
      this.waveformReceivedCount = 1;
    } else {
      this.waveformReceivedCount++;
    }
    // d.out(this, "update [" + waveformCount + "] " + wre.toStringBrief());
    // Create a new data queue if necessary
    if (waveformQueuesbyJob.get(serialNum) == null) {
      waveformQueuesbyJob.put(serialNum, new TreeMap<Long, IDatagram>());
      // add a new Waveform Repsonse Queue for <time , WRE datagram>
    }
    // Add the received datagram to the appropriate data queue
    waveformQueuesbyJob.get(serialNum).put(System.currentTimeMillis(), datagram);
    // Trim the data queue if necessary
    if (waveformQueuesbyJob.get(serialNum).size() > JOB_QUEUE_DEPTH) {
      waveformQueuesbyJob.get(serialNum).remove(waveformQueuesbyJob.get(serialNum).firstKey());
    }
    // Update the max, min, average waveform data
    if (detailedStatistics) {
      updateMaxMinAve(datagram);
    }
    d.out(this, "update exit");
  }

  /**
   * Increment the running counter of waveforms sent
   */
  public void notifyWaveformSent() {
    if (this.waveformSentCount == Integer.MAX_VALUE - 1) {
      this.waveformSentCount = 1;
    } else {
      this.waveformSentCount++;
    }
  }

  public void setDebug(Boolean debug) {
    this.d = new Debug(debug);
  }

  /**
   * Set whether we should track the max/min/ave values for each job
   * <p>
   * @param getDetailed
   */
  public void setDetailedStatistics(Boolean getDetailed) {
    this.detailedStatistics = getDetailed;
  }

  /**
   * Are we tracking the max/min/ave values for each job?
   * <p>
   * @return
   */
  public Boolean isDetailedStatistics() {
    return this.detailedStatistics;
  }

  /**
   * Update the Maximum, Minimum and Average values received
   * <p>
   * @param WaveformResponseExtended_Interface datagram
   */
  private void updateMaxMinAve(IDatagram wre) {
    String serialNum = wre.getTransactionId();
    // If no WRE exists, then create one
    if ((maxWaveformByJob.get(serialNum) == null) || (minWaveformByJob.get(serialNum) == null) || (aveWaveformByJob.get(serialNum) == null)) {
      d.out(this, "updateMaxMinAve " + serialNum + " not found. Initializing");
      maxWaveformByJob.put(serialNum, wre.getWaveformDBm());
      minWaveformByJob.put(serialNum, wre.getWaveformDBm());
      aveWaveformByJob.put(serialNum, wre.getWaveformDBm());
    }
    // d.out(this, "updateMaxMinAve aggregate timing");
    // Update the aggregate (scalar) timing counters
    maxDuration = wre.getElapsedTimeMS() > maxDuration ? wre.getElapsedTimeMS() : maxDuration;
    minDuration = wre.getElapsedTimeMS() < minDuration ? wre.getElapsedTimeMS() : minDuration;
    aveDuration = (aveDuration * (waveformReceivedCount - 1) + wre.getElapsedTimeMS()) / waveformReceivedCount;
    if ((maxDurationPerJob.get(serialNum) == null) || (minDurationPerJob.get(serialNum) == null) || (aveDurationPerJob.get(serialNum) == null)) {
      maxDurationPerJob.put(serialNum, wre.getElapsedTimeMS());
      minDurationPerJob.put(serialNum, wre.getElapsedTimeMS());
      aveDurationPerJob.put(serialNum, wre.getElapsedTimeMS());
    }
    // d.out(this, "updateMaxMinAve per-job counters");
    // Update the per-job (Mapped) timing counters
    int max = maxDurationPerJob.get(serialNum);
    int min = minDurationPerJob.get(serialNum);
    int ave = aveDurationPerJob.get(serialNum);
    maxDurationPerJob.put(serialNum, max > wre.getElapsedTimeMS() ? max : wre.getElapsedTimeMS());
    minDurationPerJob.put(serialNum, min < wre.getElapsedTimeMS() ? min : wre.getElapsedTimeMS());
    aveDurationPerJob.put(serialNum, (wre.getElapsedTimeMS() + ave * (waveformReceivedCount - 1)) / waveformReceivedCount);
    // Test if durations is working . == Yes
    // d.out(this,"durations " + maxDurationPerJob + " " + minDurationPerJob +
    // " " + aveDurationPerJob );
    // Get the current data
    TreeMap<Double, Double> currentWaveformDBm = wre.getWaveformDBm();
    // Update the temporary holders max/min/ave data
    Iterator<Double> iter = currentWaveformDBm.keySet().iterator();
    Double cf;
    TreeMap<Double, Double> maxMap = new TreeMap<>();
    TreeMap<Double, Double> minMap = new TreeMap<>();
    TreeMap<Double, Double> aveMap = new TreeMap<>();
    while (iter.hasNext()) {
      cf = iter.next();
      maxMap.put(cf, currentWaveformDBm.get(cf) > (Double) maxWaveformByJob.get(serialNum).get(cf) ? currentWaveformDBm.get(cf) : (Double) maxWaveformByJob.get(serialNum).get(cf));
      minMap.put(cf, currentWaveformDBm.get(cf) < (Double) minWaveformByJob.get(serialNum).get(cf) ? currentWaveformDBm.get(cf) : (Double) minWaveformByJob.get(serialNum).get(cf));
      aveMap.put(cf, (currentWaveformDBm.get(cf) + (Double) aveWaveformByJob.get(serialNum).get(cf) * (waveformReceivedCount - 1)) / waveformReceivedCount);
    }
    maxWaveformByJob.put(serialNum, maxMap);
    minWaveformByJob.put(serialNum, minMap);
    aveWaveformByJob.put(serialNum, aveMap);
    // Test if ave/min/max is working == YES
    // d.out(this, "curwav: " + currentWaveformDBm.toString());
    // d.out(this, "curmax: " + maxWaveformByJob.get(serialNum).toString());
    // d.out(this, "curmin: " + minWaveformByJob.get(serialNum).toString());
    // d.out(this, "curave: " + aveWaveformByJob.get(serialNum).toString());
  }

  /**
   * Reset all counters and empty all queues
   */
  public void reset() {
    this.maxWaveformByJob = null;
    this.minWaveformByJob = null;
    this.aveWaveformByJob = null;
    this.waveformQueuesbyJob = null;
    this.waveformReceivedCount = 0;
  }

  /**
   * Get the current running status <br>
   * This requires a synchronized (= thread safe) map because parameters may be
   * updated by the capture thread while we are trying to read them
   * <p>
   * @return Hashmap of the sensor operational status, including running jobs
   */
  public Map<String, Object> getStatus() {
    // d.out(this, "getStatus");
    Map<String, Object> status = Collections.synchronizedMap(new HashMap<String, Object>());
    synchronized (status) {
      // Add information from Status Counters
      status.put("device.jobs.time.ave", this.aveDuration);
      status.put("device.jobs.time.max", this.maxDuration);
      status.put("device.jobs.time.min", this.minDuration);
      status.put("device.waveforms.rcd", this.waveformReceivedCount);
      status.put("device.waveforms.sent", this.waveformSentCount);
      status.put("device.started", startedRunning.get(Calendar.MONTH) + "/" + startedRunning.get(Calendar.DAY_OF_MONTH) + "/" + startedRunning.get(Calendar.YEAR) + " at " + startedRunning.get(Calendar.HOUR_OF_DAY) + ":" + startedRunning.get(Calendar.MINUTE) + ":" + startedRunning.get(Calendar.SECOND));
      status.put("device.jobs.running", waveformQueuesbyJob.size());
      // Add description of statitics for each job in the queue
      /**
       * foreach job: serial number : ave, min, max, jobs run
       */
      Iterator<String> jobsIterator = this.waveformQueuesbyJob.keySet().iterator();
      String jobSn;
      while (jobsIterator.hasNext()) {
        jobSn = jobsIterator.next();
        status.put("job." + jobSn + ".time.min", minDurationPerJob.get(jobSn));
        status.put("job." + jobSn + ".time.max", maxDurationPerJob.get(jobSn));
        status.put("job." + jobSn + ".time.ave", aveDurationPerJob.get(jobSn));
        // d.out(this, "getStatus jobs: " + jobSn);
        // status.put("sensor.jobSn", );
      }
      return status;
    }
  }

  // Waveform Queue ------------------------------------------------------------
  /**
   * Get the entire waveform queue by job
   * <p>
   * @param job job serial number
   * @return Treemap of Time : WaveformResponseExtended_Interface
   */
  public TreeMap<Long, IDatagram> getWaveformQueueByJob(Long job) {
    return waveformQueuesbyJob.get(job);
  }

  // SensorEntities ------------------------------------------------------------
  /**
   * Get a Collection of sensor entities, sorted by AvcomSerialNumber
   * <p>
   * @return
   */
  public Collection<SensorEntity> getSensorEntities() {
    return sensorEntitiesMap.values();
  }

  /**
   * Add a sensor entity
   * <p>
   * @param sensorEntity
   */
  public void putSensorEntity(SensorEntity sensorEntity) {
    this.sensorEntitiesMap.put(sensorEntity.getDeviceSerialNumber(), sensorEntity);
  }

  // Queue Depth ------------------------------------------------------------
  /**
   * How many waveforms we should store in our queue depth
   * <p>
   * @return
   */
  public int getJOB_QUEUE_DEPTH() {
    return JOB_QUEUE_DEPTH;
  }

  /**
   * How many waveforms should we hold in memory
   * <p>
   * @param JOB_QUEUE_DEPTH
   */
  public void setJOB_QUEUE_DEPTH(int JOB_QUEUE_DEPTH) {
    this.JOB_QUEUE_DEPTH = JOB_QUEUE_DEPTH;
  }
  /**
   * Return
   * <p>
   * @param serialNumber
   */
  // public WaveformResponseExtended_Interface getWaveformExtended(String
  // serialNumber) {
  // // WaveformResponseExtended_Interface wre =
  // (WaveformResponseExtended_Interface)
  // waveformQueuesbyJob.get(serialNumber).lastEntry().getValue();
  // // d.out(this, "getWaveformExtended " + wre.toStringBrief());
  // // wre.setWaveformDBmMax(maxWaveformByJob.get(serialNumber));
  // // wre.setWaveformDBmMin(minWaveformByJob.get(serialNumber));
  // // wre.setWaveformDBmAve(aveWaveformByJob.get(serialNumber));
  // // d.out(this,"getWaveformExtended ");
  // // return wre;
  // return (WaveformResponseExtended_Interface)
  // waveformQueuesbyJob.get(serialNumber).lastEntry().getValue();
  // // waveformQueuesbyJob.get(serialNumber).get(d)
  // }
}
