/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keybridgeglobal.sensor.interfaces;

import com.avcomfova.sbs.datagram.IDatagram;

/**
 * Interface describing the variables and methods that a instances must support
 * <p>
 * @author jesse
 */
public interface IDatagramListener {

  public void onDatagram(IDatagram datagram);

//  public void onDatagram(Datagram d);
}
