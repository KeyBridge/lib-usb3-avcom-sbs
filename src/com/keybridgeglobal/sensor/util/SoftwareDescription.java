package com.keybridgeglobal.sensor.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a complete description of the software
 * <p>
 * @author jesse
 */
public class SoftwareDescription {

  private final HashMap<String, String> settings = new HashMap<>();
  // ----------------------------------------------------------------------------
  private final String softwareVersion = ProjectBuildInfo.VERSION;
  // ----------------------------------------------------------------------------
  Calendar cal = Calendar.getInstance();
  private final String softwareCopyright = ProjectBuildInfo.COPYRIGHT;
  private final String softwareTerms = ProjectBuildInfo.TERMS;
  private final String softwareBuildDate = cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR) + " at " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

  /**
   * Returns software settings
   * <p>
   * <pre>
   * Key                     Meaning
   * -------------------     ------------------------------
   * "file.separator"        File separator (e.g., "/")
   * "java.class.path"       Java classpath
   * "java.class.version"    Java class version number
   * "java.home"             Java installation directory
   * "java.vendor"           Java vendor-specific string
   * <p>
   * "java.vendor.url"       Java vendor URL
   * "java.version"          Java version number
   * "line.separator"        Line separator
   * "os.arch"               Operating system architecture
   * "os.name"               Operating system name
   * <p>
   * "path.separator"        Path separator (e.g., ":")
   * "user.dir"              User's current working directory
   * "user.home"             User home directory
   * "user.name"             User account name
   * </pre>
   * <p>
   * @return
   */
  public Map<String, String> getStatus() {
    // System settings
    // settings.put("file.separator", System.getProperty("file.separator"));
    settings.put("software.java.class.path", System.getProperty("java.class.path"));
    settings.put("software.java.class.version", System.getProperty("java.class.version"));
    settings.put("software.java.home", System.getProperty("java.home"));
    settings.put("software.java.vendor", System.getProperty("java.vendor"));
    // settings.put("software.java.vendor.url",
    // System.getProperty("java.vendor.url"));
    settings.put("software.java.version", System.getProperty("java.version"));
    // settings.put("line.separator", System.getProperty("line.separator"));
    settings.put("software.os.arch", System.getProperty("os.arch"));
    settings.put("software.os.name", System.getProperty("os.name"));
    // settings.put("path.separator", System.getProperty("path.separator"));
    settings.put("software.user.dir", System.getProperty("user.dir"));
    settings.put("software.user.home", System.getProperty("user.home"));
    settings.put("software.user.name", System.getProperty("user.name"));
    settings.put("software.copyright", softwareCopyright);
    settings.put("software.use.terms", softwareTerms);
    settings.put("software.build.version", softwareVersion);
    settings.put("software.build.date", softwareBuildDate);
    try {
      settings.put("system.hostname", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException ex) {
      Logger.getLogger(SoftwareDescription.class.getName()).log(Level.SEVERE, null, ex);
    }
    return settings;
  }

  public static void main(String[] args) {
    SoftwareDescription sd = new SoftwareDescription();
    Map<?, ?> foo = sd.getStatus();
    // Set set = foo.keySet();
    Iterator<?> i = foo.keySet().iterator();
    System.out.println(String.format("%25s : %s", "Key", "Value"));
    System.out.println(String.format("%25s : %s", "--------------", "--------------"));
    while (i.hasNext()) {
      String key = (String) i.next();
      // System.out.println("" + key + "\t : " + foo.getStatus(key));
      System.out.println(String.format("%25s : %s", key, foo.get(key)));
    }
  }
}
