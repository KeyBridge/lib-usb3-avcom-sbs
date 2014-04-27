package com.keybridgeglobal.sensor.interfaces;

import java.util.Collection;
import java.util.List;

import javax.swing.event.ChangeListener;

/**
 * Interface describing the variables and methods that a spectrum analyzer must
 * support SpectrumAnalyzer_Interface instances must contain the following
 * variables private double centerFrequencyMHz private double spanMHz private
 * double resolutionBandwidthMHz private <ReferenceLevel_Interface>
 * referenceLevel : this should be an enumerated value
 * <p>
 * Note: Implementations must also contain the private method notifyListeners
 * described below (in comments)
 * <p>
 * Note: spectrum analyzer implementations should also have the following
 * method: <br>
 * private void notifyListeners(WaveformResponseExtended_Interface datagram);
 */
public interface ISpectrumAnalyzer {

  public void addChangeListener(ChangeListener listener);

  public void addJob(ISensorJob settingsRequest);

  public void addSpectrumAnalyzerListener(ISpectrumAnalyzerListener saListener);

  public void disconnect(); // disconnects from the serial port - required for

  public String getCommPort(); // e.g. /dev/ttyUSB0 or COM5

  public Collection<ISensorJob> getJobs();

  public ISensorEntity getSensorEntity();

  public String getSerialNumber();

  public Boolean initialize();

  public Boolean isRunning();

  public void removeAllJobs();

  public void removeChangeListener(ChangeListener listener);

  public void removeJob(String serialNumber);

  public void removeSpectrumAnalyzerListener(ISpectrumAnalyzerListener saListener);

  public void requestWaveform();

  public void requestWaveform(String serialNumber);

  public void setDebug(boolean debug);

  public void startCapture();

  public void stopCapture();

  public void setJobs(List<ISensorJob> jobs);
}
