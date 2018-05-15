//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

/**
 * The non-remote interface for a worker. This contains methods 
 * that are called when the worker is created and deleted etc.
 */
public interface WorkerLocal
{
  /**
   * Called once the worker has been created. This method can
   * create threads, allocate resources etc. that cannot be done
   * in the constructor.
   *
   * It is called by the node after a worker is created, but before
   * the stub for the worker is returned to the controller.
   *
   * If an exception occurs in this method, it will be returned to 
   * the controller that created it, and the worker will be deleted.
   * This will result in the deleted() method being called.
   */
  public void created() throws Exception;

  /**
   * Called when the worker is deleted. This happens before the RMI 
   * object is unexported.
   *
   * There is no guarantee that the created() method has been executed
   * when this runs, nor is there a guarantee that the worker has been
   * exported correctly, or that the controller still exists.
   *
   * This method should ensure that any threads have been stopped and 
   * resources freed up so that the worker object can be garbage 
   * collected.
   */
  public void deleted();

  /**
   * Sets the RMI proxy for this worker object.
   * @param proxy to use
   */
  public void setWorkerProxy(Worker proxy);

  /**
   * Gets the worker proxy object.
   * @return proxy object for this worker.
   */
  public Worker getWorkerProxy();

  /**
   * Can be called by the worker code when an exception or error
   * occurs. The behaviour of this depends on the worker
   * implementation.
   * @param throwable exception or error to handle.
   */
  public void handle(Throwable throwable);

  /**
   * Set an object to handle errors and exceptions that occur in the
   * worker, but do not occur as a direct result of a remote method
   * call, for example an exception occurs in another thread.
   * By default there should be no callback. A null callback will 
   * prevent remote calls when an error occurs.
   * @param callback to handle remote error, null to disable.
   */
  public void setErrorCallback(ErrorCallback callback);

}
