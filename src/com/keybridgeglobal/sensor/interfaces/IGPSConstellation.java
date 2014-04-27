/**
 * <p>
 * Copyright 2010 Key Bridge Global LLC.
 * <p>
 * http://keybridgeglobal.com
 * <p>
 * This file is part of the Java package "GPSd Java Client: (GPSdClient)".
 * <p>
 * GPSdClient is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * GPSdClient is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * GPSdClient. If not, see <http://www.gnu.org/licenses/>.
 */
package com.keybridgeglobal.sensor.interfaces;

import java.util.List;

/**
 * @author jesse
 */
public interface IGPSConstellation {

  public Long getGpsTimestamp();

  public short getSatelliteCount();

  public List<IGPSSatellite> getSatellites();

  public String getConstellation();

  public String toStringBrief();
}
