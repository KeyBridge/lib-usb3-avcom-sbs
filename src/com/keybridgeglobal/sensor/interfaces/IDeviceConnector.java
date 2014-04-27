package com.keybridgeglobal.sensor.interfaces;

import com.avcomfova.sbs.datagram.IDatagram;
import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Interface describing the variables and methods that this instance must
 * support DeviceConnectors are the method by which Spectrum Analyzer objects
 * communite with the port DeviceConnectors are an abstraction level above
 * PortAdapters, which are the physical interfaces themselves DeviceConnectors
 * must support: - a queuing mechanism for accepting writeDatagram commands
 * (writeDatagram should not dump to the port directly) - a queuing mechanism
 * for writing out datagrams from the - a method to DCRead raw data from the
 * port and convert it to usable datagrams - a locking mechanism to synchrnonize
 * port reads and writes where necessary
 * <p>
 * @author jesse
 */
public interface IDeviceConnector {

  public void addDatagramListener(IDatagramListener datagramListener);

  public void addProgressChangeListener(ChangeListener changeListener);

  public Boolean connect();

  public Boolean disconnect();

  public Map<String, Object> getStatus();

  public void initializeAdapter();

  public Boolean isConnected();

  public void removeDatagramListener(IDatagramListener datagramListener);

  public void removeProgressChangeListener(ChangeListener changeListener);

  public Boolean requestWaveform();

  public Boolean requestWaveform(String serialNumber);

  public void setDebug(Boolean debug);

  public void setSettings(IDatagram settings);

  public void startThread();

  public void stopThread();
}
