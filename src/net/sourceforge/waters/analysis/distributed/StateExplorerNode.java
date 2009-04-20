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
    mVisitedSet = new THashSet<StateTuple>();
    mWaitingSet = new THashSet<StateTuple>();

    mPlantTransitions = generateTransitionTables(AutomatonSchema.PLANT);
    mSpecTransitions = generateTransitionTables(AutomatonSchema.SPECIFICATION);

    int[] initial = findInitialState();
    addState(mEncoding.encodeState(initial));


    System.err.println(mModel);
  }

  /**
   * Start a worker thread processing this state explorer.  
   */
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
    if (!mVisitedSet.contains(state) && !mWaitingSet.contains(state))
      {
	mWaitingSet.add(state);
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
   * Get the number of states which have been explored.
   */
  public synchronized int getExploredStateCount()
  {
    return mVisitedSet.size();
  }

  public synchronized int getWaitingStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  public synchronized int getWaitingSetSize()
  {
    return mWaitingSet.size();
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
	StateTuple t = mStateList.get(mCurrentStateIndex++);    
	mVisitedSet.add(t);
	mWaitingSet.remove(t);
	return t;
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
    int autCount = mModel.getAutomataCount();
    int eventCount = mModel.getEventCount();
    int[] successor = new int[autCount];

    while (true)
      {
	StateTuple state = waitForNextState();

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
		  successor[at] = succ;
		else if (mModel.getEvent(ev).getKind() == 
			 EventSchema.UNCONTROLLABLE)
		  {
		    //The specification disallows this 
		    //uncontrollable event. Produce a 
		    //counterexample here.
		    setUncontrollable();
		    
		    //This isn't the right thing to do,
		    //but meh.
		    continue events;
		  }
	      }

	    StateTuple tuple = mEncoding.encodeState(successor);
	    addState(tuple);
	  }

	/*
	events:
	for (int event = 0; event < eventCount; event++) {
	  for (int aut = 0; aut < autCount; aut++) {
	    TransitionTable table = getTransitionTable(aut);
	    int succ = table.getSuccessorState(decoded[aut], event);
	    if (succ >= 0) {
	      successor[aut] = succ;
	    } else if (aut >= mModel.getFirstSpecIndex() &&
	               mModel.getEvent(event).getKind() ==
		       EventSchema.UNCONTROLLABLE) {
	      // produceCounterExample(state, event);
	    } else {
	      continue events;
	    }
	  }
	  // May have to send the state somewhere in distributed case ...
	  StateTuple tuple = mEncoding.encodeState(successor);
	  addState(tuple);
	}
	*/
      }
  }

  /**
   * Generate transition tables for a kind of automaton in a model.
   * This allows you to build a list of the transition relation for
   * all the plants or specifications in the model.
   * @param The kind of automaton to generate transition tables for.
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

  private final ProductDESSchema mModel;
  private final StateEncoding mEncoding;

  /**
   * A set of the states that have been visited and 
   * expanded by this state explorer.
   */
  private final THashSet<StateTuple> mVisitedSet;
  private final THashSet<StateTuple> mWaitingSet;

  /**
   * Stores a list of states that have been visited and
   * need to be visited. This list should be randomly 
   * accessible.
   */
  private final List<StateTuple> mStateList;

  private final TransitionTable[] mPlantTransitions;
  private final TransitionTable[] mSpecTransitions;

  private int mCurrentStateIndex = 0;
  private boolean mUncontrollable = false;
}