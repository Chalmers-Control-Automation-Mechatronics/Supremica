//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote
{
  /**
   * An empty method that can be used to check if the 
   * node is alive.
   */
  public void ping() throws RemoteException;

  /**
   * A factory method to create workers given a class name.
   * The class name should be the full name for a class, and the
   * class must be assignable to the Worker interface.
   * 
   * Exceptions will be thrown if the worker cannot be created... 
   * somehow through the remote exception
   *
   * @param id Controller id, used for resource management.
   * @param classname name of the class to load.
   * @param cb error callback to use, null for no callback.
   * @return remote instance of a worker. 
   * @throws IllegalArgumentException if the controller ID or classname
   *                                  is null.
   */
  public Worker createWorker(ControllerID id, String classname, ErrorCallback cb) 
    throws 
    ClassNotFoundException,
    IllegalAccessException,
    IllegalArgumentException,
    InstantiationException,
    RemoteException;


  public void cleanup(ControllerID id) throws RemoteException;

  public void shutdown() throws RemoteException;
}
