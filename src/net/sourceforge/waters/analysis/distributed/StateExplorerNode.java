package net.sourceforge.waters.analysis.distributed;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.model.base.WatersRuntimeException;

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
    mEncoding = new PackedStateEncoding(mModel);
    
    mStateList = new ArrayList<StateTuple>();
    mVisitedSet = new THashSet<StateTuple>();
  }

  public void runWorkerThread()
  {
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.start();
  }

  /**
   * Add a state to be processed. If the state has not
   * already been visited it will be added to the state
   * list.
   */
  public synchronized void addState(StateTuple state)
  {
    if (!mVisitedSet.contains(state))
      {
	mStateList.add(state);
	this.notify();
      }
  }


  /**
   * Check if there are any unexplored states.
   */
  public synchronized boolean noUnexploredStates()
  {
    return mCurrentStateIndex == mStateList.size();
  }


  /**
   * Get the next unprocessed state from the state list.
   * This will advance the current state index by one.
   * If there are no more states, this will return null.
   * @returns The next state, or null if there are no states to 
   *          process
   */
  private synchronized StateTuple getNextState()
  {
    if (noUnexploredStates())
      return null;
    else
      return mStateList.get(mCurrentStateIndex++);    
  }


  /**
   * Wait for the next state to become available. It is a little
   * deceptive to mark this method as synchronised; most of the 
   * time it will leave the monitor unlocked while it waits.
   *
   * When a state is available to process, it will return it,
   * otherwise it will block indefinitely.
   * 
   * @return A state for processing.
   */
  protected synchronized StateTuple waitForNextState()
  {
  waiting: while (true)
      {
	while (noUnexploredStates())
	  {
	    try
	      {
		this.wait();
	      }
	    catch (InterruptedException e)
	      {
		throw new WatersRuntimeException(e);
		//Interruptions are unimportant. If interrupted but
		//there is still nothing to do, just wait again.
	      }
	  }
	
	StateTuple state = getNextState();
	if (state == null)
	  continue waiting;
	
	return state;
      }
  }


  /**
   * Entry point for a processing thread.
   */
  public void run()
  {
    while (true)
      {
	StateTuple state = waitForNextState();

	//Decode the state
	int[] decoded = mEncoding.decodeState(state);
	int autCount = mModel.getAutomataCount();
	int eventCount = mModel.getEventCount();
	int[] successor = new int[autCount];

	/*
	events:
	for (int event = 0; event < eventCount; event++) {
	  for (int aut = 0; aut < autCount; aut++) {
	    TransitionTable table = getTransitionTable(aut);
	    int succ = table.getSuccessorState(decoded[aut], event);
	    if (succ >= 0) {
	      successor[aut] = succ;
	    } else if (aut < mModel.getFirstSpecIndex()) {
	      continue events;
	    } else if (mModel.getEvent(event).getKind() ==
		       EventSchema.UNCONTROLLABLE) {
	      // produceCounterExample(state, event);
	    }
	  }
	  // May have to send the state somewhere in distributed case ...
	  StateTuple tuple = mEncoding.encode(successor);
	  addState(tuple);
	}
	*/
      }
  }

  private final ProductDESSchema mModel;
  private final PackedStateEncoding mEncoding;

  /**
   * A set of the states that have been visited and 
   * expanded by this state explorer.
   */
  private final THashSet<StateTuple> mVisitedSet;

  /**
   * Stores a list of states that have been visited and
   * need to be visited. This list should be randomly 
   * accessible.
   */
  private final List<StateTuple> mStateList;

  private int mCurrentStateIndex = 0;
}