package com.avcomofva.sbs.enumerated;

public enum EJMSCommand {

//  THREE_MHZ(0x80, "3.00 MHz"),         // 2/04 - per Jay - no longer supported
  CHANGE_SETTINGS(1, "sensor.change.settings"),
  ADD_JOB(2, "sensor.add.job"),
  GET_STATUS(3, "sensor.get.status"),
  STOP(4, "sensor.stop"),
  START(5, "sensor.start"),
  RESET(6, "sensor.reset");
  private final int commandCode;
  private final String label;

  private EJMSCommand(int b, String label) {
    this.commandCode = b;
    this.label = label;
  }

  /**
   * Returns the Avcom bytecode for this resolution bandwidth value
   * <p>
   * @return
   */
  public int getCommandCode() {
    return this.commandCode;
  }

  /**
   * Returns and English label
   * <p>
   * @return
   */
  public String getLabel() {
    return this.label;
  }

  public static EJMSCommand byLabel(String label) {
    for (EJMSCommand eJMSCommand : EJMSCommand.values()) {
      if (eJMSCommand.getLabel().equalsIgnoreCase(label)) {
        return eJMSCommand;
      }
    }
    return null;
  }

  public static EJMSCommand byCommandCode(int commandCode) {
    for (EJMSCommand eJMSCommand : EJMSCommand.values()) {
      if (eJMSCommand.getCommandCode() == commandCode) {
        return eJMSCommand;
      }
    }
    return null;
  }
}
