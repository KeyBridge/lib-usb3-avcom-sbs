package com.keybridgeglobal.sensor.avcom.datagram;

import com.avcomfova.sbs.datagram.IDatagram;
import com.avcomofva.sbs.enumerated.EAvcomReferenceLevel;
import com.avcomofva.sbs.enumerated.EAvcomResolutionBandwidth;
import com.keybridgeglobal.sensor.SimpleJob;
import com.keybridgeglobal.sensor.interfaces.ISensorJob;

/**
 * @author jesse
 */
public class DatagramFactory {

  /**
   * Create a datagram based on the type header
   * <p>
   * @param bytes
   * @return
   */
  public static IDatagram createDatagram(byte[] bytes) {
    switch (bytes[IDatagram.HEADER_SIZE]) {
      case IDatagram.HARDWARE_DESCRIPTION_RESPONSE_ID:
        return new HardwareDescriptionResponse(bytes);
      case IDatagram.WAVEFORM_RESPONSE_ID:
        return new WaveformResponse(bytes);
      default:
        return new IDatagram(bytes); // return a generic datagram
    }
  }

  /**
   * Copy a SettingsRequest Datagram, applying limits to the requested center
   * frequency
   * <p>
   * @param settingsRequest
   * @return
   */
  public static SettingsRequest copySettingsRequest(SettingsRequest settingsRequest) {
    SettingsRequest s = new SettingsRequest();
    // Set the SettingsRequest
    s.setCenterFrequencyMHz(settingsRequest.getCenterFrequencyMHz());
    s.setSpanMHz(settingsRequest.getSpanMHz());
    s.setReferenceLevel(settingsRequest.getReferenceLevel());
    s.setResolutionBandwidth(settingsRequest.getResolutionBandwidth());
    s.setInputConnector(settingsRequest.getInputConnector());
    s.setLnbPower(settingsRequest.getLnbPower());
    return s;
  }

  /**
   * Convert a SensorJob_Interface object to a SettingsRequest datagram
   * <p>
   * @param job
   * @return
   */
  public static SettingsRequest getSettingsRequest(ISensorJob job) {
    if (job == null) {
      // If a job has been deleted we may receive a null input. Handle it
      // gracefully.
      job = SimpleJob.getDefault();
    }
    SettingsRequest s = new SettingsRequest();
    // Set the SettingsRequest
    s.setTransactionId(job.getSnJob());
    s.setCenterFrequencyMHz(job.getCenterFrequency());
    s.setSpanMHz(job.getSpan());
    s.setReferenceLevel(EAvcomReferenceLevel.findNearest(job.getReferenceLevel()));
    s.setResolutionBandwidth(EAvcomResolutionBandwidth.findNearest(job.getResolutionBandwidth()));
    // s.setInputConnector(settingsRequest.getInputConnector());
    // s.setLnbPower(settingsRequest.getLnbPower());
    return s;
  }

  /**
   * Convert a SettingsRequest datagram to a SimpleJob object (compliant with
   * SensorJob_Interface)
   * <p>
   * @param sr
   * @return
   */
  public static ISensorJob getJob(IDatagram settingsRequest) {
    if (settingsRequest == null) {
      settingsRequest = SettingsRequest.getDefault();
    }
    SettingsRequest sr = (SettingsRequest) settingsRequest;
    ISensorJob sensorJob = new SimpleJob(sr.getTransactionId());
    sensorJob.setCenterFrequency(sr.getCenterFrequencyMHz());
    sensorJob.setReferenceLevel(sr.getReferenceLevel().getValueDB());
    sensorJob.setResolutionBandwidth(sr.getResolutionBandwidth().getMHz());
    sensorJob.setSnSensor(sr.getSensorSerialNumber());
    sensorJob.setSpan(sr.getSpanMHz());
    return sensorJob;
  }

  /**
   * Copy the run-time parameters from a WaveformResponse into a
   * WaveformResponseExtended_Interface datagram.<br>
   * centerFrequency and span are not set because these are not valid: WRE has
   * an extended CF and span.
   * <p>
   * @param waveformResponse
   * @return WaveformRepsonse with populated values (except for
   *         centerFrequencyMHz & spanMHz
   */
  public static WaveformResponseExtended extendWaveformResponse(WaveformResponse waveformResponse) {
    WaveformResponseExtended wre = new WaveformResponseExtended();
    // Set the Extended Waveform Response
    wre.setReferenceLevel(waveformResponse.getReferenceLevel());
    wre.setResolutionBandwidth(waveformResponse.getResolutionBandwidth());
    wre.setIsValid(Boolean.TRUE);
    wre.setSaturated(waveformResponse.isSaturated());
    return wre;
  }
}
