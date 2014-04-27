package com.keybridgeglobal.sensor.util;

/**
 * @author jesse
 */
public class ProjectBuildInfo {

  public ProjectBuildInfo() {
  }

  // public static final String COPYRIGHT =
  // "(C) rfsDB LLC / Key Bridge Global LLC";
  public static final String COPYRIGHT = "(C) Key Bridge Global LLC";
  public static final String TERMS = "Unauthorized use, disassembly or disclosure prohibited.";
  public static final String VERSION = "4.2.1 (OSGI)";

  public static String getCopyright() {
    return COPYRIGHT;
  }

  public static String getTerms() {
    return TERMS;
  }

  public static String getVersion() {
    return VERSION;
  }

  public static void main(String args[]) {
    System.out.println(ProjectBuildInfo.getCopyright());
  }
}
