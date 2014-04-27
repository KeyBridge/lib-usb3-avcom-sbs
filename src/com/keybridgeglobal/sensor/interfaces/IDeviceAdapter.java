package com.keybridgeglobal.sensor.interfaces;

import com.avcomfova.sbs.datagram.IDatagram;

public interface IDeviceAdapter {

  /**
   * Connect to the indicated physical port
   * <p>
   * @param port
   * @return
   */
//  public Boolean connect(CommPortIdentifier comPortIdentifier);
  public Boolean connect();

  /**
   * Disconnect from all attached Device Adapters
   * <p>
   * @return
   */
  public Boolean disconnect();

  /**
   * Is the device connector (and its Device Adapter) connected?
   * <p>
   * @return
   */
  public Boolean isConnected();

  /**
   * Add a Datagram Listener to receive the datagrams that this adapter will
   * generate
   * <p>
   * @param deviceConnector
   */
  public void addDatagramListener(IDatagramListener datagramListener);

  /**
   * Remove a Device Listener from the list
   * <p>
   * @param datagramListener
   */
  public void removeDatagramListener(IDatagramListener datagramListener);

  /**
   * Write a complete datagram to the physical device
   * <p>
   * @param datagram
   */
  public void write(IDatagram datagram);

//  public void setComPortIdentifier(CommPortIdentifier comPortIdentifier);
//  public CommPortIdentifier getComPortIdentifier();
}
