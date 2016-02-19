/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.avcomfova.sbs;

import com.avcomfova.sbs.datagram.IDatagram;
import java.util.EventListener;

/**
 * Interface describing the methods called when a datagram is received from a
 * sensor device. This extends EventListener.
 *
 * @author Jesse Caulfield
 */
public interface IDatagramListener extends EventListener {

  public void onDatagram(IDatagram datagram);
//  public void onDatagram(Datagram d);
}
