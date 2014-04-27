package com.keybridgeglobal.sensor.interfaces;

public interface IGPSService {

  public IGPSConstellation getGPSConstellation();

  public IGPSLocation getGPSLocation();

  public boolean isGPSAvailable();

  public void restartSession();

  public IGPSLocation persist();

  public void showMessages(boolean onoff);

  public void setDebug(boolean debug);
}
