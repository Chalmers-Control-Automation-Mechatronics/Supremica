//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Keeps track of workers that have been created on this
 * node, and which controller was responsible. This allows
 * all the workers for a particular controller to be cleaned
 * up and unexported.
 *
 * @author Sam Douglas 
 */
class WorkerCleanup
{
  public WorkerCleanup()
  {
    mWorkers = new HashMap<ControllerID, Set<WorkerLocal>>();
  }

  public synchronized void registerWorker(ControllerID id, WorkerLocal worker)
  {
    assert(id != null);
    assert(worker != null);

    Set<WorkerLocal> wset = mWorkers.get(id);
    if (wset == null)
      {
	wset = new HashSet<WorkerLocal>();
	mWorkers.put(id, wset);
      }

    wset.add(worker);
  }

  /**
   * Cleanup the workers associated with an id. Errors that occur when
   * trying to clean up the object will be ignored.
   *
   * Remote objects will be unexported forcefully, which may cause calls 
   * that are in progress to fail.
   * 
   * After calling this method, there will be no workers associated 
   * with the controller ID, even if some of them failed to be cleaned
   * up properly (nothing we can do?)
   *
   * @param id The controller ID to clean up for.
   */
  public synchronized void cleanup(ControllerID id)
  {
    Set<WorkerLocal> wset = null;

    synchronized (this)
      {
	wset = mWorkers.get(id);
	mWorkers.remove(id);
      }

    if (wset == null)
      return;

    for (WorkerLocal w : wset)
      {
	//Ideally, we want to cast to a Worker (the remote interface)
	//and forcefully unexport the object.
	try
	  {
	    Worker remote = (Worker)w;
	    boolean b = UnicastRemoteObject.unexportObject(remote, true);

	    System.err.format("Worker cleanup: %b\n", b);
	  }
	catch (ClassCastException e)
	  {
	    //The worker is a bit messed up, not remote.
	    //This probably shouldn't be able to happen.
	    System.err.println(e);
	  }
	catch (NoSuchObjectException e)
	  {
	    //The worker wasn't exported. This is no big 
	    //deal. We will just ignore it and continue
	    System.err.println(e);
	  }

	w.deleted();    
      }

    //Empty the set
    wset.clear();
  }

  //Map of controller id to set of workers. This really assumes
  //that workers implement object equality and hashcode.
  private final Map<ControllerID, Set<WorkerLocal>> mWorkers;
}
