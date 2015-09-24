//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


import net.sourceforge.waters.analysis.distributed.application.WorkerLocal;

public class ThreadedOutputDispatcher extends AbstractOutputDispatcher
{
  private static final long serialVersionUID = 1L;

  public ThreadedOutputDispatcher(WorkerLocal worker, StateDistribution dist, int threadcount, int waitinglimit)
  {
    super(dist);

    mWaitingLimit = waitinglimit;

    mWorker = worker;

    //An arbitrary, somewhat big queue. We don't want to
    //block the processing threads until there really is a 
    //large backlog of states.
    mOutputQueue = new ArrayDeque<StateTuple>(mWaitingLimit);

    for (int i = 0; i < threadcount; i++)
      {
	DispatchThread thread = new DispatchThread();
	thread.setDaemon(true);
	mDispatchers.add(thread);
	thread.start();
      }
  }

  public void addState(StateTuple state) throws InterruptedException
  {
    synchronized (mOutputQueue)
      {
	//If the queue is full, then block until it is less 
	//full.
	while (mOutputQueue.size() + 1 > mWaitingLimit)
	  {
	    mOutputQueue.wait();
	  }

	mOutputQueue.addLast(state);
	mOutputQueue.notifyAll();
      }
  }

  public void addStates(StateTuple[] states, int offset, int length) throws InterruptedException
  {
    synchronized (mOutputQueue)
      {
	while (mOutputQueue.size() + length > mWaitingLimit)
	  {
	    mOutputQueue.wait();
	  }

	for (int i = offset; i < offset+length && i < states.length; i++)
	  {
	    mOutputQueue.addLast(states[i]);
	  }
	mOutputQueue.notifyAll();
      }
  }

  class DispatchThread extends Thread
  {
    public void run()
    {
      final StateDistribution dist = getStateDistribution();
      try
	{
	  while (true)
	    {
	      StateTuple t = getNextState();
	      try
		{
		  dist.addState(t);
		}
	      catch (Exception e)
		{
		  mWorker.handle(new RuntimeException("Asynchronous add state " + 
						      "failed in dispatch thread. " +
						      "Verification compromised?", e));
		  halt();
		}
	    }
	}
      catch (InterruptedException e)
	{
	  //When a dispatch thread is interrupted, it 
	  //is probably because the dispatcher needs to
	  //terminate
	  return;
	}
    }
  }

  private StateTuple getNextState() throws InterruptedException
  {
    synchronized (mOutputQueue)
      {
	while (mOutputQueue.size() == 0 || mHalt)
	  {
	    mOutputQueue.wait();
	  }

	StateTuple t = mOutputQueue.removeFirst();

	//Let the vultures swoop in
	mOutputQueue.notifyAll();
	return t;
      }
  }

  private void halt()
  {
    mHalt = true;
  }

  public synchronized void shutdown()
  {
    //Interrupt all dispatch threads then wait for them
    //to terminate
    notifyAll();
    for (DispatchThread t : mDispatchers)
      {
	t.interrupt();
      }

    for (DispatchThread t : mDispatchers)
      {
	try
	  {
	    t.join();
	  }
	catch (InterruptedException e)
	  {
	    //Ignore the interrupt and don't bother joining 
	    //the thread. No huge loss.
	  }
      }
  }

  private final List<DispatchThread> mDispatchers = new ArrayList<DispatchThread>();

  //Access to this queue is externally synchronized
  private final Deque<StateTuple> mOutputQueue;

  //Used for error reporting
  private final WorkerLocal mWorker;

  //Used to stop the dispatcher if something bad happens. This should help
  //mitigate massive spam if an error occurs.
  private volatile boolean mHalt;

  private final int mWaitingLimit;
}
