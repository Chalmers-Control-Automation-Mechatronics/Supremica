package net.sourceforge.waters.analysis.distributed;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.EventSchema;
import net.sourceforge.waters.model.base.WatersRuntimeException;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import gnu.trove.THashSet;



/**
 * A node in a multi-processor state exploration. This is designed
 * to be multithreaded on a single processor, and also as a node in
 * a distributed application on a cluster.
 *
 * Objects of this class use its monitor to synchronise threads, 
 * calling notify will wake up worker threads to explore states.
 */
public class StateExplorerNode implements Runnable
{
  public StateExplorerNode(final ProductDESSchema model)
  {
    mModel = model;
    mEncoding = new NullStateEncoding(mModel);
    
    mStateList = new ArrayList<StateTuple>();
    mObservedSet = new THashSet<StateTuple>();

    mPlantTransitions = generateTransitionTables(AutomatonSchema.PLANT);
    mSpecTransitions = generateTransitionTables(AutomatonSchema.SPECIFICATION);

    int[] initial = findInitialState();
    addState(mEncoding.encodeState(initial));
  }

  /**
   * Start a worker thread processing this state explorer.  
   */
  public synchronized void runWorkerThread()
  {
    Thread t = new Thread(this);
    t.setDaemon(true);
    mWorkerThreads.add(t);
    t.start();
  }

  /**
   * Shut down the state explorer. This will pause the 
   * system, interrupt each thread, then join them, to ensure
   * they have terminated. This is necessary so the model checker
   * is reusable.
   * @return True if all worker threads terminated.
   */
  public boolean shutdown()
  {
    //All the threads will eventually be waiting
    pause();
    
    for (Thread t : mWorkerThreads)
      {
	try
	  {
	    t.interrupt();
	    t.join();
	  }
	catch (InterruptedException e)
	  {
	    //We were interrupted, not everything will
	    //be shut down though
	    return false;
	  }
      }
    
    return true;
  }

  /**
   * Add a state to be processed. If the state has not
   * already been visited it will be added to the state
   * list.
   */
  public synchronized void addState(StateTuple state)
  {
    if (!mObservedSet.contains(state))
      {
	mObservedSet.add(state);
	mStateList.add(state);
	this.notify();
      }
  }

  /**
   * Check if the state explorer appears to be idle.
   * This means that there are currently no jobs being
   * processed, and the queue is empty.
   */
  public synchronized boolean isIdle()
  {
    return noUnexploredStates() && areJobsFinished();
  }


  /**
   * Check if there are any unexplored states.
   */
  public synchronized boolean noUnexploredStates()
  {
    return mCurrentStateIndex == mStateList.size();
  }

  /**
   * Get the number of states which have been explored.
   */
  public synchronized int getExploredStateCount()
  {
    return mObservedSet.size();
  }

  public synchronized int getWaitingStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  /**
   * Get the next unprocessed state from the state list.
   * This will advance the current state index by one. It will
   * also record the state as being visited, as this prevents
   * race conditions.
   * If there are no more states, this will return null.
   * @returns The next state, or null if there are no states to 
   *          process
   */
  private synchronized StateTuple getNextState()
  {
    if (noUnexploredStates())
      return null;
    else
      {
	return mStateList.get(mCurrentStateIndex++);    
      }
  }


  /**
   * Wait for the next state to become available. It is a little
   * deceptive to mark this method as synchronised; most of the 
   * time it will leave the monitor unlocked while it waits.
   *
   * When a state is available to process, it will return it,
   * otherwise it will block indefinitely.
   *
   * When a state is acquired, it will indicate that a job is running,
   * in order to preserve atomicity. The calling thread must ensure the
   * job is finished in future.
   * 
   * @return A state for processing.
   * @throws InterruptedException If the current thread is interrupted
   *                              while waiting for a new state.
   */
  protected synchronized StateTuple waitForNextState() 
    throws InterruptedException
  {
  waiting: while (true)
      {
	while (noUnexploredStates() || isPaused())
	  {
	    this.wait();
	  }
	
	StateTuple state = getNextState();
	if (state == null)
	  continue waiting;
	
	jobStarted();
	return state;
      }
  }


  /**
   * Entry point for a processing thread.
   */
  public void run()
  {
    int autCount = mModel.getAutomataCount();
    int eventCount = mModel.getEventCount();
    int[] successor = new int[autCount];

    while (true)
      {
	StateTuple state = null;
	try 
	  {
	    state = waitForNextState();
	  }
	catch (InterruptedException e)
	  {
	    //Terminate the thread.
	    return;
	  }

	//Decode the state
	int[] decoded = mEncoding.decodeState(state);

	

      events:for (int ev = 0; ev < eventCount; ev++)
	  {
	    //Calculate successor states by first checking
	    //if an event is enabled by all the plants, and
	    //if it is, whether the event is enabled by
	    //the specifications.
	    for (int p = 0; p < mPlantTransitions.length; p++)
	      {
		TransitionTable tt = mPlantTransitions[p];
		int at = tt.getAutomatonIndex();
		int succ = tt.getSuccessorState(decoded[at], ev);
		
		//If the transition is disallowed, then continue
		//with the next event.
		if (succ >= 0)
		  successor[at] = succ;
		else
		  continue events;
	      }

	    for (int s = 0; s < mSpecTransitions.length; s++)
	      {
		TransitionTable tt = mSpecTransitions[s];
		int at = tt.getAutomatonIndex();
		int succ = tt.getSuccessorState(decoded[at], ev);

		if (succ >= 0)
		  {
		    successor[at] = succ;
		  }
		else if (mModel.getEvent(ev).getKind() == 
			 EventSchema.UNCONTROLLABLE)
		  {
		    //The specification disallows this 
		    //uncontrollable event. Produce a 
		    //counterexample here.
		    setUncontrollable();

		    //Here we should probably stop, but lets keep exploring
		    //anyway.
		    continue events;
		  }
		else
		  {
		    continue events;
		  }
	      }

	    StateTuple tuple = mEncoding.encodeState(successor);
	    addState(tuple);
	  }

	jobFinished();
      }
  }

  /**
   * Generate transition tables for a kind of automaton in a model.
   * This allows you to build a list of the transition relation for
   * all the plants or specifications in the model.
   * @param  kind    The kind of automaton to generate transition tables for.
   * @return An array of transition tables.
   */
  protected TransitionTable[] generateTransitionTables(int kind)
  {
    List<TransitionTable> tts = new ArrayList<TransitionTable>();
    
    for (int i = 0; i < mModel.getAutomataCount(); i++)
      {
	if (mModel.getAutomaton(i).getKind() == kind)
	  tts.add(new TransitionTable(mModel, i));
      }

    return tts.toArray(new TransitionTable[0]);
  }

  protected int[] findInitialState()
  {
    int[] start = new int[mModel.getAutomataCount()];
    
    for (int i = 0; i < mModel.getAutomataCount(); i++)
      {
	AutomatonSchema aut = mModel.getAutomaton(i);
	
	for (int s = 0; s < aut.getStateCount(); s++)
	  {
	    if (aut.getState(s).getInitial())
	      {
		start[i] = s;
		break;
	      }
	  }
      }

    return start;
  }

  private synchronized void setUncontrollable()
  {
    mUncontrollable = true;
  }

  public synchronized boolean isUncontrollable()
  {
    return mUncontrollable;
  }

  /**
   * Pause the state exploration. Pausing will prevent the
   * waitForNextState method to block, until the state exploratation
   * is unpaused. This will not prevent new states from being added,
   * nor will it interrupt any states currently being processed.
   */
  public synchronized void pause()
  {
    mPaused = true;
  }

  /**
   * Unpause the state exploration. This clears the paused flag and
   * notifies all threads waiting on this object's monitor, so that
   * they will start exploring states immediately.
   */
  public synchronized void unpause()
  {
    mPaused = false;
    this.notifyAll();
  }

  /**
   * Check if the current state exploration is paused.
   */
  public synchronized boolean isPaused()
  {
    return mPaused;
  }

  /**
   * Call when a job is started. Increments a count of 
   * running jobs.
   */
  private synchronized void jobStarted()
  {
    mRunningJobs++;
  }


  /**
   * Call when a job is finished. Decrement a count of
   * running jobs.
   */
  private synchronized void jobFinished()
  {
    assert mRunningJobs > 0: "Tried to decrement running job count too many times!";
    mRunningJobs--;
  }

  /**
   * Check if all jobs are finished.
   */
  public synchronized boolean areJobsFinished()
  {
    return mRunningJobs == 0;
  }

  private final ProductDESSchema mModel;
  private final StateEncoding mEncoding;

  /**
   * A set of the states that have been observed (expanded
   * or queued for expansion) by this state explorer.
   */
  private final THashSet<StateTuple> mObservedSet;

  /**
   * Stores a list of states that have been visited and
   * need to be visited. This list should be randomly 
   * accessible.
   */
  private final List<StateTuple> mStateList;

  private final TransitionTable[] mPlantTransitions;
  private final TransitionTable[] mSpecTransitions;


  private List<Thread> mWorkerThreads = new ArrayList<Thread>();
  private int mCurrentStateIndex = 0;
  private boolean mUncontrollable = false;
  private boolean mPaused = false;
  private int mRunningJobs = 0;
}