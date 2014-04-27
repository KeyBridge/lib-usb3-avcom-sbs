package com.avcomofva.sbs.enumerated;

/**
 * Enumerated Port recognition patters for FTDI USB chip by operating system
 * <p>
 * @author jesse
 */
public enum EFTDIUSBPort {

  LINUX("Linux", "USB"),
  MAXOSX("Mac OS X", "tty.usbserial"),
  VISTA("Windows Vista", "COM4"),
  WINDOWS("Windows", "COM4");
  private final String osName;
  private final String port;

  private EFTDIUSBPort(String osName, String port) {
    this.osName = osName;
    this.port = port;
  }

  /**
   * Returns the operating system this pattern matches
   * <p>
   * @return
   */
  public String getOSName() {
    return this.osName;
  }

  /**
   * Returns the serial comm port regex pattern
   * <p>
   * @return
   */
  public String getPort() {
    return this.port;
  }

  /**
   * Shortcut to get the USB comm port matching pattern for this operating
   * system.
   * <p>
   * @return the USB comm port matching pattern for this operating system
   */
  public static EFTDIUSBPort getCurrent() {
    return EFTDIUSBPort.byOperatingSystem(System.getProperty("os.name"));
  }

  /**
   * Get a USB Port object by its operating system
   * <p>
   * @param operatingSystem - value returned from
   *                        System.getProperties("os.name")
   * @return
   */
  public static EFTDIUSBPort byOperatingSystem(String operatingSystem) {
    for (EFTDIUSBPort fTDIUSBPort : EFTDIUSBPort.values()) {
      if (fTDIUSBPort.getOSName().equalsIgnoreCase(operatingSystem)) {
        return fTDIUSBPort;
      }
    }
    return null;
  }
}
