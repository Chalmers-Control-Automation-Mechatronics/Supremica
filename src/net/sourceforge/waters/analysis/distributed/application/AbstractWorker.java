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

/**
 * Abstract implementation of the worker interface. This provides a
 * default implementation of the WorkerLocal interface.
 */
public abstract class AbstractWorker implements Worker, WorkerLocal
{
  public void created() throws Exception
  {
  }

  public void deleted()
  {
  }

  public void setErrorCallback(ErrorCallback cb)
  {
    mErrorCallback = cb;
  }


  public void handle(Throwable throwable)
  {
    try
      {
	ErrorCallback cb = mErrorCallback;
	if (cb == null)
	  defaultErrorHandler(throwable);
	else
	  cb.handle(getWorkerID(), getWorkerProxy(), throwable);
      }
    catch (Exception e)
      {
	errorCallbackFailed(e, throwable);
	return;
      }

    errorCallbackSucceeded(throwable);
 }

  /**
   * Default error handler. This is called if there is no
   * remote callback set.
   */ 
  protected void defaultErrorHandler(Throwable throwable)
  {
    System.err.format("AbstractWorker default handler: %s", throwable);
    throwable.printStackTrace();
  }

  /**
   * Template method that is called after an error has been
   * successfully reported to the error callback.
   * @param throwable Error that was handled.
   */
  protected void errorCallbackSucceeded(Throwable throwable)
  {
  }

  /**
   * Template method that is called if an error callback 
   * fails.
   * @param why the callback failed.
   * @param throwable that was being handled.
   */
  protected void errorCallbackFailed(Exception why, Throwable throwable)
  {
    System.err.format("Worker handler callback failed");
    throw new RuntimeException(throwable);
  }

  public void setWorkerProxy(Worker proxy)
  {
    mWorkerProxy = proxy;
  }

  public Worker getWorkerProxy()
  {
    return mWorkerProxy;
  }

  public String getWorkerID()
  {
    return mWorkerID;
  }

  public void setWorkerID(String workerid)
  {
    mWorkerID = workerid;
  }

  private volatile Worker mWorkerProxy;
  private volatile ErrorCallback mErrorCallback;
  private volatile String mWorkerID;
}
