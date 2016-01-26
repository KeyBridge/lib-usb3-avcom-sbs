/*
 * Copyright (c) 2014, Jesse Caulfield <jesse@caulfield.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.keybridge.sensor.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
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
  // ----------------------------------------------------------------------------
  private Calendar cal = Calendar.getInstance();

  public static final String COPYRIGHT = "(C) Key Bridge Global LLC";
  public static final String TERMS = "Unauthorized use, disassembly or disclosure prohibited.";
  public static final String VERSION = "4.2.1 (OSGI)";

  private final String softwareBuildDate = cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR) + " at " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

  /**
   * Returns software settings
   * <p>
   * <
   * pre>
   * Key Meaning ------------------- ------------------------------
   * "file.separator" File separator (e.g., "/") "java.class.path" Java
   * classpath "java.class.version" Java class version number "java.home" Java
   * installation directory "java.vendor" Java vendor-specific string
   * <p>
   * "java.vendor.url" Java vendor URL "java.version" Java version number
   * "line.separator" Line separator "os.arch" Operating system architecture
   * "os.name" Operating system name
   * <p>
   * "path.separator" Path separator (e.g., ":") "user.dir" User's current
   * working directory "user.home" User home directory "user.name" User account
   * name
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
    settings.put("software.copyright", COPYRIGHT);
    settings.put("software.use.terms", TERMS);
    settings.put("software.build.version", VERSION);
    settings.put("software.build.date", softwareBuildDate);
    try {
      settings.put("system.hostname", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException ex) {
      Logger.getLogger(SoftwareDescription.class.getName()).log(Level.SEVERE, null, ex);
    }
    return settings;
  }

}
