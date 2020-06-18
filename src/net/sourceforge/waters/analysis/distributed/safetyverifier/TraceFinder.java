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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.model.analysis.AnalysisException;

import gnu.trove.list.array.TIntArrayList;

/**
 * Finds a trace between two reachable states in a distributed
 * synchronous product.
 *
 * This class is designed to find a trace from
 * the initial state to a bad state, but it could be used to find a
 * path between two states provided the target ('bad') state is
 * reachable from the supplied initial state, that is, they lie on the
 * same path with initial a predecessor of the bad state.
 *
 * It works by finding the predecessors of a given state, and the
 * event that is taken to reach the current state (from the
 * predecessor). Some of the predecessors will be unreachable, these
 * are eliminated by looking at the set of visited states in the
 * synchronous product. The best (lowest depth hint) immediate
 * predecessor is selected and this process is repeated for that
 * state.
 *
 * As the synchronous product is distributed over multiple workers,
 * this process is complicated slightly, as each worker only stores a
 * partition of the state space. To get good performance and low
 * network overhead, each worker searches for predecessors for the
 * state and filters out those that aren't stored locally. The worker
 * then returns the best predecessor states it finds to this object.
 *
 * This class is responsible for coordinating the search and
 * collecting the results; most of the work of generating predecessors
 * is done on each worker.
 *
 * @author Sam Douglas
 */
class TraceFinder implements PredecessorCallback
{
  public TraceFinder(final ProductDESSchema model,
		     final StateEncoding encoding,
		     final SafetyVerifierWorker[] workers)
  {
    mModel = model;
    mStateEncoding = encoding;
    mWorkers = workers.clone();
  }

  public ProductDESSchema getModel()
  {
    return mModel;
  }

  public StateEncoding getStateEncoding()
  {
    return mStateEncoding;
  }

  public SafetyVerifierWorker[] getWorkers()
  {
    return mWorkers;
  }

  @Override
  public synchronized int takePredecessor(final StateTuple original,
					  final StateTuple predecessor,
					  final int event)
  {
    System.out.format("Given predecessor %s of %s (depth: %d), event %d\n",
		      mStateEncoding.interpret(predecessor),
		      mStateEncoding.interpret(original),
		      predecessor.getDepthHint(),
		      event);

    //Check if the predecessor state is actually relevant
    if (original.equals(mSearchState))
      {
	//Check if the predecessor is better than what we already
	//have, updating the current best if it is, then return the
	//depth of the current best state so that future predecessors
	//can be filtered by workers.
	final int pdepth = predecessor.getDepthHint();
	if (pdepth < mBestDepth)
	  {
	    mBestDepth = pdepth;
	    mBestPredecessor = predecessor;
	    mBestEvent = event;
	  }

	return mBestDepth;
      }
    else
      {
	//This is nasty, but should give the hint we don't want to
	//hear about this search any more. This situation should
	//rarely occur.
	return Integer.MIN_VALUE;
      }
  }

  @Override
  public synchronized void searchCompleted(final StateTuple original, final String worker)
  {
    //If the search was relevant, then decrement the running search
    //count (unless this will put the value less than zero). When the
    //value gets to zero, any waiting threads should be awakened.
    if (mPredecessorSearchCount > 0 && original.equals(mSearchState))
      mPredecessorSearchCount--;

    if (mPredecessorSearchCount == 0)
      notifyAll();
  }

  private synchronized void setPredecessorSearch(final StateTuple state,
						 final int counter)
  {
    mSearchState = state;
    mPredecessorSearchCount = counter;

    //Reset the current best variables
    mBestPredecessor = null;
    mBestDepth = Integer.MAX_VALUE;
    mBestEvent = -1;
  }

  /**
   * Finds a trace from the supplied initial state to the supplied bad
   * state. It is assumed that the initial state is a predecessor of
   * the bad state, that is to say the bad state is reachable from the
   * initial state. The bad event is added as the first step in the
   * trace.
   *
   * The trace is returned as an array of event ids, starting at the
   * initial state that will arrive at the bad state.
   *
   * If a trace cannot be created, some kind of AnalysisException will
   * be thrown, for example if the initial state is not returned as a
   * predecessor.
   *
   * This method exports this object as a RMI remote object, and
   * unexports it when finished.
   *
   * @param bad state to generate a trace to
   * @param badevent the event that caused the property to fail on the
   *                 bad state
   * @param initial the initial state
   * @return array of events needed to reach the bad state from
   *         initial state
   * @throws RemoteException if something bad happens with RMI calls
   * @throws AnalysisException if something bad happens while
   *                           generating the trace
   */
  public int[] findTrace(final StateTuple bad,
                         final int badevent,
                         final StateTuple initial)
    throws RemoteException, AnalysisException
  {
    //Export this object on an anonymous port. This will be passed to
    //workers when a predecessor search is initiated.
    final PredecessorCallback cb = (PredecessorCallback)
      UnicastRemoteObject.exportObject(this, 0);

    //Create the trace list. This will be filled out backwards
    //(starting from the bad state), and reversed at the end.
    final TIntArrayList trace = new TIntArrayList();
    trace.add(badevent);

    final SafetyVerifierWorker[] workers = getWorkers();

    try
      {
	StateTuple state = bad;
	while (!initial.equals(state))
	  {
	    //Start a search for the current state on each worker.
	    setPredecessorSearch(state, workers.length);
	    for (final SafetyVerifierWorker w : workers)
	      w.predecessorSearch(state, cb);

	    //Wait for all workers to indicate they have
	    //terminated. When the count reaches 0, the
	    //searchCompleted method should notify all waiting threads
	    //and wake this up, but a timeout is set 'just in
	    //case'. This should also allow us to just move on if for
	    //some reason the search is taking a long time and we have
	    //results.
	    synchronized (this)
	      {
		//Wait for all the searches we started to finish
		while (mPredecessorSearchCount > 0)
		  {
		    try
		      {
			wait(1000);
		      }
		    catch (final InterruptedException e)
		      {
			throw new AnalysisException("Finding trace interrupted!");
		      }
		  }

		//Add the best event to the trace list.
		trace.add(mBestEvent);
		state = mBestPredecessor;
	      }

	    //If this occurs it is because all the searches returned
	    //without finding a good predecessor state. This suggests
	    //something bad, as it should never search for
	    //predecessors to the initial state (the only case where
	    //there should not be predecessors)
	    if (state == null)
	      {
		throw new AnalysisException
		  ("No predecessor states were found when " +
		   "building trace, but it wasn't the initial state!");
	      }
	  }
      }
    finally
      {
	//Unexport the remote object. Hopefully this will succeed and
	//further calls will fail, but that's not our problem.
	UnicastRemoteObject.unexportObject(this, true);
      }

    trace.reverse();
    return trace.toArray();
  }

  private int mPredecessorSearchCount = 0;
  private StateTuple mSearchState = null;

  private int mBestDepth = Integer.MAX_VALUE;
  private StateTuple mBestPredecessor = null;
  private int mBestEvent = -1;

  private final ProductDESSchema mModel;
  private final StateEncoding mStateEncoding;
  private final SafetyVerifierWorker[] mWorkers;
}
