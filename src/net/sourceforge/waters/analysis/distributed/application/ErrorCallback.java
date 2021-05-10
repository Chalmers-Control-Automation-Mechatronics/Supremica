//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

/**
 * An interface for providing remote error callbacks for 
 * workers.
 *
 * @author Sam Douglas
 */
public interface ErrorCallback extends Remote
{
  /**
   * Handle an error that occurs. This shouldn't really 
   * throw anything, except a remote exception. It's purpose
   * is to handle errors and exceptions... not make more!
   * @param workerid A string to uniquely identify the worker.
   * @param worker A remote proxy for the worker
   * @param throwable to handle. This is supposed to handle
   *        errors too.
   * @throws RemoteException if it is a remote callback and 
   *                         the RMI call fails.
   */
  public void handle(String workerid, Worker worker, Throwable throwable) throws RemoteException;
}
