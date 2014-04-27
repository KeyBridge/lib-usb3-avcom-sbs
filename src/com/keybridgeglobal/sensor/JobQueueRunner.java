package com.keybridgeglobal.sensor;

import com.keybridgeglobal.sensor.avcom.DeviceConnector_Avcom;
import com.keybridgeglobal.sensor.interfaces.IDatagramListener;
import com.avcomfova.sbs.datagram.IDatagram;

public class JobQueueRunner implements Runnable, IDatagramListener {

  private DeviceConnector_Avcom dc;
  private String jobSerialNumber;
  private final Object lock = new Object();

  public JobQueueRunner(DeviceConnector_Avcom dc) {
    this.dc = dc;
    dc.addDatagramListener(this);
  }

  @Override
  public void run() {
    synchronized (lock) {
      dc.requestWaveform(jobSerialNumber);
      try {
        lock.wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void setJobSerialNumber(String jobSerialNumber) {
    this.jobSerialNumber = jobSerialNumber;
  }

  @Override
  public void onDatagram(IDatagram datagram) {
    lock.notify();
  }
}
