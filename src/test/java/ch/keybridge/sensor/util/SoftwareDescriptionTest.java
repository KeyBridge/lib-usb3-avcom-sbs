/*
 * Copyright 2016 Caulfield IP Holdings (Caulfield) and affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * Software Code is protected by copyright. Caulfield hereby
 * reserves all rights and copyrights and no license is
 * granted under said copyrights in this Software License Agreement.
 * Caulfield generally licenses software for commercialization
 * pursuant to the terms of either a Standard Software Source Code
 * License Agreement or a Standard Product License Agreement.
 * A copy of these agreements may be obtained by sending a request
 * via email to info@caufield.org.
 */
package ch.keybridge.sensor.util;

import java.util.Iterator;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author Key Bridge LLC
 */
public class SoftwareDescriptionTest {

  public SoftwareDescriptionTest() {
  }

  @Test
  public void testSomeMethod() {
    SoftwareDescription sd = new SoftwareDescription();
    Map<?, ?> foo = sd.getStatus();
    // Set set = foo.keySet();
    Iterator<?> i = foo.keySet().iterator();
    System.out.println(String.format("%-25s : %s", "Key", "Value"));
    System.out.println(String.format("%-25s : %s", "--------------", "--------------"));
    while (i.hasNext()) {
      String key = (String) i.next();
      // System.out.println("" + key + "\t : " + foo.getStatus(key));
      System.out.println(String.format("%-25s : %s", key, foo.get(key).toString()));
    }
  }

}
