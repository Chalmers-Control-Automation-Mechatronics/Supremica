package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.THashMap;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.sourceforge.waters.analysis.distributed.application.AbstractWorker;
import net.sourceforge.waters.analysis.distributed.application.Worker;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.Node;

import net.sourceforge.waters.analysis.distributed.schemata.*;

public class SafetyVerifierWorkerImpl extends AbstractWorker implements SafetyVerifierWorker
{
  public SafetyVerifierWorkerImpl()
  {
    //The constructor is used sparingly; the created method is run
    //after the object has been successfully exported.
    System.out.format("Constructed safety verifier worker\n");
  }

  public void setJob(Job job)
  {
    mJob = job;
  }

  public void setModelSchema(ProductDESSchema des)
  {
    mModel = des;

    //Calculate plant and spec transition tables
    mTransitionTables = generateTransitionTables();
    mPlantTransitions = selectTransitionTables(mTransitionTables, AutomatonSchema.PLANT);
    mSpecTransitions = selectTransitionTables(mTransitionTables, AutomatonSchema.SPECIFICATION);
  }

  public void setWorkerID(String id)
  {
    mWorkerID = id;
  }

  public String getWorkerID()
  {
    return mWorkerID;
  }

  public void setStateDistribution(StateDistribution stateDist)
  {
    if (getWorkerID() == null)
      throw new IllegalStateException("Worker ID was not set");

    mStateDistribution = stateDist;

    //Set the state handler to the local object. This will
    //prevent calls from going through the RMI system for 
    //local states.
    mStateDistribution.setHandler(getWorkerID(), this);
  }

  public StateDistribution getStateDistribution()
  {
    return mStateDistribution;
  }

  public void setStateEncoding(StateEncoding encoding)
  {
    mStateEncoding = encoding;
  }

  public StateEncoding getStateEncoding()
  {
    return mStateEncoding;
  }

  public void startProcessingThreads(int n, int buffersize)
  {
    final String ExceptionText = "Cannot add new processing threads after "+
      "worker has been killed";
      
    //Check for kill state before and after entering the synchronized
    //region, as the kill state could be set while the thread is
    //waiting on mThreads.
    if (mKillState)
      throw new IllegalStateException(ExceptionText);

    synchronized (mThreads)
      {
	if (mKillState)
	  throw new IllegalStateException(ExceptionText);
	
	for (int i = 0; i < n; i++)
	  {
	    ProcessingThread t = new ProcessingThread(buffersize);
	    t.setDaemon(true);
	    t.start();
	    mThreads.add(t);
	  }
      }
  }

  public void addState(StateTuple state)
  {
    //This method will add states to be checked, and will 
    //maintain the hashtable of visited states etc.
    synchronized (mStateList)
      {
	if (!mObservedSet.containsKey(state))
	  {
	    mObservedSet.put(state, state);
	    mStateList.add(state);
	  }
	mIncomingStateCounter++;

	//Notify any threads that might be waiting for new states.
	mStateList.notifyAll();
      }
  }

  /**
   * A thread for processing states. Each processing thread has a 
   * buffer of states that it fills from the incoming queue.
   */
  private class ProcessingThread extends Thread
  {    
    public ProcessingThread(int bufferSize)
    {
      super();
      mBufferSize = bufferSize;
    }

    public void run()
    {
      try
	{
	  //Create a fixed size buffer of states to process.
	  StateTuple[] buffer = new StateTuple[mBufferSize];
	  
	  //Store the number of valid states in the buffer.
	  int bufLen = 0;
	  
	  //Used by fillBuffer to avoid incrementing the running count
	  //when this thread is first started.
	  boolean alreadyRunning = false;

	  while (true)
	    {
	      //Refill the buffer, block here until there is work
	      //to do.
	      bufLen = fillBuffer(buffer, alreadyRunning);
	      alreadyRunning = true;

	      //Process items in the buffer
	      for (int i = 0; i < bufLen; i++)
		{
		  //Pause the thread if necessary, while checking for
		  //the kill flag. If this method returns true then the 
		  //processing thread should terminate.
		  if (pauseOrDie())
		    {
		      //XXX: Should the remaining work buffer be returned to
		      //     the incoming queue here... perhaps it should.
		      return;
		    }

		  //Process the state.
		  StateTuple state = buffer[i];
		  processState(state);
		}
	    }
	}
      catch (InterruptedException e)
	{
	  //An interrupt, This should kill the thread. And will. This
	  //probably happened because the thread was waiting for more states.
	}
    }

    /**
     * Fill the thread's buffer from the list of unprocessed states. 
     * This method will wait on the state list monitor, and should be 
     * notified if there are states to be processed.
     * 
     * This method also controls the running processing thread
     * counter. By returning successfully (that is to say, with a
     * non-empty buffer), the thread is considered running until it
     * comes back for more states. Before attempting to get any
     * states, this thread is set as idle. It only indicates the
     * thread is running just before returning successfully. If
     * interrupted in this method, the thread will not be counted as
     * running.
     *
     * The thread count will only be decremented if the alreadyRunning
     * parameter is true. This is used to prevent the the counter being 
     * decremented on threads that are not yet running.
     * @throws InterruptedException if the thread is interrupted or if 
     *                              the kill state is set.
     */ 
    private int fillBuffer(StateTuple[] buffer, boolean alreadyRunning) 
      throws InterruptedException
    {
      synchronized (mStateList)
	{
	  //If the processing thread is not current running then do
	  //not decrement the running thread counter. This is used so
	  //that the first time the buffer is filled, the thread is
	  //not considered to be running. While this would be cleaner
	  //to do outside the call to fillBuffer, this means it can be
	  //done with a single lock operation in the best case.
	  if (alreadyRunning)
	    {
	      mRunningProcessingThreads--;
	      killCanary();
	    }

	waiting: while (true)
	    {
	      while (unprocessedStates() == 0 || mPausedState)
		{
		  if (mKillState)
		    throw new InterruptedException("Thread should be killed");

		  mStateList.wait();
		}
	      
	      //There should certainly be some states now. We want to
	      //take as many as we can fit into the buffer, but won't
	      //be greedy.
	      int nstates = Math.min(unprocessedStates(), buffer.length);
	      
	      if (nstates == 0)
		continue waiting;
	      
	      //Fill the buffer.
	      for (int i = 0; i < nstates; i++)
		{
		  buffer[i] = getNextState();
		}

	      //After returning with states, the thread is considered
	      //to be running.
	      killCanary();
	      mRunningProcessingThreads++;
	      return nstates;
	    }
	}
    }

    private final int mBufferSize;
  }


  private void processState(StateTuple state)
  {    
    int[] decoded = mStateEncoding.decodeState(state);
    int autCount = mModel.getAutomataCount();
    int eventCount = mModel.getEventCount();
    int[] successor = new int[autCount];    

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
		setBadState(state, ev);
		
		//Here we should probably stop, but lets keep exploring
		//anyway.
		continue events;
	      }
	    else
	      {
		continue events;
	      }
	  }

	//Dispatch state
	StateTuple packed = mStateEncoding.encodeState(successor, state.getDepthHint() + 1);

	try
	  {
	    mStateDistribution.addState(packed);
	    synchronized (mOutgoingLock)
	      {
		mOutgoingStateCounter++;
	      }
	  }
	catch (Exception e)
	  {
	    throw new RuntimeException(e);
	  }
      }
  }
    
  /**
   * Gets the number of unprocessed states. This method synchronises on
   * the state list.
   * @return the number of unprocessed states.
   */
  private int unprocessedStates()
  {
    synchronized (mStateList)
      {
	return mStateList.size() - mCurrentStateIndex;
      }
  }

  private StateTuple getNextState()
  {
    synchronized (mStateList)
      {
	return mStateList.get(mCurrentStateIndex++);
      }
  }
  

  private void setBadState(StateTuple bad, int event)
  {
    synchronized (mBadStateLock)
      {
	if (mBadState == null)
	  {
	    mBadState = bad;
	    mBadEvent = event;
	  }
      }
  }

  public StateTuple getBadState()
  {
    synchronized (mBadStateLock)
      {
	return mBadState;
      }
  }

  public int getBadEvent()
  {
    synchronized (mBadStateLock)
      {
	return mBadEvent;
      }
  }


  /**
   * Generates transition tables for automata in the model. The
   * transition tables will have the same ordering as automata in the
   * model.
   * @return An array of transition tables.
   */
  protected TransitionTable[] generateTransitionTables()
  {
    List<TransitionTable> tts = new ArrayList<TransitionTable>();
    
    for (int i = 0; i < mModel.getAutomataCount(); i++)
      {
	tts.add(new TransitionTable(mModel, i));
      }

    return tts.toArray(new TransitionTable[0]);
  }


  /**
   * Selects transition tables from an array based on kind.  This
   * makes no assumptions about the ordering of the tables array, it
   * uses the automaton id from the transition table.
   * @param tables Transition tables to select from
   * @param kind the automaton kind to select
   * @return an array containing the transition tables corresponding
   *         to the automaton kind. This could be empty.
   */
  protected TransitionTable[] selectTransitionTables(TransitionTable[] tables,
						     int kind)
  {
    List<TransitionTable> tts = new ArrayList<TransitionTable>();

    for (int i = 0; i < tables.length; i++)
      {
	int at = tables[i].getAutomatonIndex();
	if (mModel.getAutomaton(at).getKind() == kind)
	  tts.add(tables[i]);
      }

    return tts.toArray(new TransitionTable[0]);
  }


  public int getStateCount()
  {
    return mCurrentStateIndex;
  }

  public long getIncomingStateCount()
  {
    return mIncomingStateCounter;
  }

  public long getOutgoingStateCount()
  {
    return mOutgoingStateCounter;
  }

  public int getWaitingStateCount()
  {
    synchronized (mStateList)
      {
	return mStateList.size() - mCurrentStateIndex;
      }
  }

  public void pause()
  {
    //It's not strictly necessary to notify all here, but it means
    //that threads waiting on the worker state monitor get notified
    //when the state changes.
    synchronized (mWorkerState)
      {
	mPausedState = true;
	mWorkerState.notifyAll();
      }

    //Hack? Also wake up threads waiting on the incoming state list.
    //This is necessary so that any processing threads that are waiting
    //on states can check the pause/kill status too.
    synchronized (mStateList)
      {
	mStateList.notifyAll();
      }
  }

  public void resume()
  {
    synchronized (mWorkerState)
      {
	mPausedState = false;
	mWorkerState.notifyAll();
      }    
    
    //Hack? Also wake up threads waiting on the incoming state list.
    //This is necessary so that any processing threads that are waiting
    //on states can check the pause/kill status too.
    synchronized (mStateList)
      {
	mStateList.notifyAll();
      }
  }

  public boolean isPaused()
  {
    return mPausedState;
  }
  
  public void kill()
  {
    synchronized (mWorkerState)
      {
	mKillState = true;
	mWorkerState.notifyAll();
      }    

    //Hack? Also wake up threads waiting on the incoming state list.
    //This is necessary so that any processing threads that are waiting
    //on states can check the pause/kill status too.
    synchronized (mStateList)
      {
	mStateList.notifyAll();
      }
  }
  
  public void created() throws Exception
  {
    super.created();
    
    mStateList = new ArrayList<StateTuple>();
    mObservedSet = new THashMap<StateTuple,StateTuple>();
  }

  public void deleted()
  {
    super.deleted();
    System.err.format("deleted() called on safety verifier worker\n");

    //Clean up running threads so the object can be garbage collected.
    kill();

    //Now wait for the threads to die. This might be a long critical
    //section, but it prevents new threads from being added.
    synchronized (mThreads)
      {
	for (Thread t : mThreads)
	  {
	    try
	      {
		t.join();
	      }
	    catch (InterruptedException e)
	      {
		//Ignore the interrupt, continue waiting for the other
		//threads to die.
	      }
	  }
      }
    
    System.err.format("Safety verifier worker: all threads terminated\n");
  }

  /**
   * Checks for the kill and pause state, blocking if
   * appropriate. This tries to minimise the amount of synchronisation
   * needed between threads. If the kill flag is set then this method
   * will return true, and the calling thread should probably
   * terminate. this also 
   */
  private boolean pauseOrDie() throws InterruptedException
  {
    //If the kill flag is set, then the caller should be informed it
    //should terminate.
    if (mKillState)
      return true;

    //If the thread isn't paused, then there is no need to synchronise
    //or wait.
    if (!mPausedState)
      {
	return false;
      }
    else
      {
	//The thread is paused, we need to wait on the state sync
	//object until notified.
	synchronized (mWorkerState)
	  {
	    while (true)
	      {
		if (mKillState)
		  return true;

		if (!mPausedState)
		  return false;

		//Wait on the state sync object
		mWorkerState.wait();
	      }
	  }
      }
  }

  public void predecessorSearch(StateTuple original, PredecessorCallback callback) throws RemoteException
  {
    mPredecessorSearch.setSearchTarget(original, callback);
  }

  public void startIdleTest()
  {
    synchronized (mStateList)
      {
	//We initialise the idle flag to whether the system appears to
	//be idle now. If it is idle then any changes to the state will
	//hopefully be detected by the idle canary.
	mIdleCanaryFlag = appearsIdle();
      }
  }

  public boolean finishIdleTest()
  {
    synchronized (mStateList)
      {
	//Store the current/old value of the idle flag.
	boolean canary = mIdleCanaryFlag;
	mIdleCanaryFlag = false;

	//Return true if the state appears to have been idle for the
	//duration of the test, and additionally is idle now. The second
	//test is perhaps unnecessary.
	return canary && appearsIdle();
      }
  }

  private boolean appearsIdle()
  {
    synchronized (mStateList)
      {
	return getWaitingStateCount() == 0 && mRunningProcessingThreads == 0;
      }
  }

  private void killCanary()
  {
    synchronized (mStateList)
      {
	mIdleCanaryFlag = false;
      }
  }

  //####################################################################
  // Predecessor Searching

  /**
   * A class to encapsulate searching for a predecessor state.
   */
  private class PredecessorSearch
  {
    public PredecessorSearch()
    {
      //Use a fixed size array blocking queue. A capacity of 1024 should
      //be enough in most situations. It might fill up on very complex models.
      mDataQueue = new ArrayBlockingQueue<Predecessor>(1024);
      mProducer = new PredecessorProducer(mDataQueue);
      mConsumer = new PredecessorConsumer(mDataQueue);

      mProducer.setDaemon(true);
      mConsumer.setDaemon(true);
      mProducer.start();
      mConsumer.start();
    }

    public void setSearchTarget(StateTuple state, PredecessorCallback cb)
    {
      synchronized (mSearchMonitor)
	{
	  mSearchState = state;
	  mCallback = cb;
	  
	  mConsumer.resetDepth();
	  
	  mSearchMonitor.notifyAll();
	}

      //Is interrupting the producer thread necessary here? Perhaps if
      //it includes numerous blocking calls?
    }

    /**
     * An immutable predecessor class. Stores the located predecessor
     * state and the state it is a predecessor to.
     */
    private class Predecessor
    {
      public Predecessor(StateTuple original, StateTuple predecessor, int event)
      {
	mOriginalState = original;
	mPredecessorState = predecessor;
	mEvent = event;
      }

      public StateTuple getOriginal()
      {
	return mOriginalState;
      }

      public StateTuple getPredecessor()
      {
	return mPredecessorState;
      }

      public int getEvent()
      {
	return mEvent;
      }
       
      private final StateTuple mOriginalState;
      private final StateTuple mPredecessorState;
      private final int mEvent;
    }
    
    private class PredecessorProducer extends Thread
    {
      public PredecessorProducer(BlockingQueue<Predecessor> dataqueue)
      {
	tQueue = dataqueue;
      }
      
      public void run()
      {
	while (true)
	  {
	    try
	      {
		//Get the state we want to search for predecessors
		//to. This will block the thread until we get a state.
		StateTuple state = null;
		synchronized (mSearchMonitor)
		  {
		    while (mSearchState == null)
		      {
			mSearchMonitor.wait();
		      }
		    state = mSearchState;
		  }
		
		try
		  {
		    produce(state);
		  }
		catch (InterruptedException e)
		  {
		    //If interrupted, the thread should continue
		    //processing the current search state, which may have
		    //changed. This will prevent the search state from
		    //being cleared further down in the loop
		    continue;
		  }
		
		//Clear the search state if we have finished. This is only
		//done if the search state differs from the state we just
		//searched.
		synchronized (mSearchMonitor)
		  {
		    
		    if (mSearchState == state)
		  mSearchState = null;
		  }
		
	      }
	    catch (InterruptedException e)
	      {
		//XXXXXXXXXXXXXXXXXXXX FIX ME XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		System.err.println("Fixme: Predecessor producer interrupted. Terminating thread.");
		return;
	      }
	  }
      }

      private void produce(StateTuple state) throws InterruptedException
      {
	int[] pre = new int[mModel.getAutomataCount()];
	int[] current = mStateEncoding.decodeState(state);
	
	for (int ev = 0; ev < mModel.getEventCount(); ev++)
	  {
	    //Expand the predecessor states for the current event.
	    expandReverse(ev, pre, state, current, 0);
	  }

	//Add an 'end of search' element to the queue. This will
	//cause the consumer to report the search is complete if
	//it gets to that point.
	tQueue.put(new Predecessor(state, null, -1));
      }

      private void expandReverse(int event, 
				 int[] pre, 
				 StateTuple packedCurrent, 
				 int[] current, 
				 int automaton) 
	throws InterruptedException
      {
	System.out.format("expandReverse: event: %d, %s %d\n",
			  event, 
			  mStateEncoding.interpret(packedCurrent),
			  automaton);

	if (automaton < current.length)
	  {
	    //Recursively call this method for each predecessor in the
	    //current automaton. This will explore all combinations of
	    //predecessors.
	    TransitionTable tt = mTransitionTables[automaton];
	    
	    if (tt.isInAlphabet(event))
	      {
		//Expand, using each possible predecessor for the current 
		//automaton.
		for (int state : tt.getPredecessorStates(current[automaton], event))
		  {
		    pre[automaton] = state;
		    expandReverse(event, pre, packedCurrent, current, automaton + 1);
		  }
	      }
	    else
	      {
		//As the event is not in the automaton's alphabet, the
		//current automaton will not have changed state on this
		//event so use the current state's value and expand.
		pre[automaton] = current[automaton];
		expandReverse(event, pre, packedCurrent, current, automaton + 1);
	      }
	  }
	else
	  {
	    //A potentially reachable predecessor state has been
	    //found. We now need to check if it is in the visited
	    //state set. If it is, then add it to the queue to be
	    //sent back to the controller and continue.

	    StateTuple pred_state = mStateEncoding.encodeState(pre, Integer.MAX_VALUE);
	    StateTuple t = null;
	    synchronized (mStateList)
	      {
		t = mObservedSet.get(pred_state);
	      }

	    
	    //If the state is not in our local observed set, then
	    //we cannot tell if the state is reachable... if it is, some other
	    //worker will take care of it
	    if (t == null)
	      return;
	    else
	      pred_state = t;

	    Predecessor p = new Predecessor(packedCurrent, pred_state, event);
	    System.out.format("Generated predecessor %s for event %d\n", 
			      mStateEncoding.interpret(pred_state),
			      event);
	    tQueue.put(p);
	  }
      }

      private final BlockingQueue<Predecessor> tQueue;
    }
    
    
    private class PredecessorConsumer extends Thread
    {
      public PredecessorConsumer(BlockingQueue<Predecessor> dataqueue)
      {
	tQueue = dataqueue;
      }

      public synchronized void resetDepth()
      {
	tBestDepth = Integer.MAX_VALUE;
      }
      
      public void run()
      {
	while (true)
	  {
	    //A simple implementation is to just send back all states
	    //in the data queue.

	    try 
	      {
		//Store the callback. This means if it changes during the
		//operation we won't care
		PredecessorCallback cb = null;
		synchronized (mSearchMonitor)
		  {
		    while (mCallback == null)
		      {
			mSearchMonitor.wait();
		      }
		    cb = mCallback;
		  }
		
		
		Predecessor p = tQueue.take();
		
		if (p.getPredecessor() == null)
		  {
		    cb.searchCompleted(p.getOriginal(),
				       getWorkerID());
		  }
		else
		  {
		    int depth = p.getPredecessor().getDepthHint();
		    boolean goodPredecessor = false;
		    synchronized (this)
		      {
			goodPredecessor = depth < tBestDepth;
		      }
		    
		    if (goodPredecessor)
		      {
			depth = cb.takePredecessor(p.getOriginal(), 
						   p.getPredecessor(),
						   p.getEvent());
			
			synchronized (this)
			  {
			    tBestDepth = depth;
			  }
		      }
		  }
	      }
	    catch (InterruptedException e)
	      {
		//XXXXXXXXXXXXXXXXXXXX FIX ME XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		System.err.println("Fixme: Predecessor consumer interrupted. Terminating thread.");
		return;
	      }
	    catch (RemoteException e)
	      {
		System.err.println("Remote exception:");
		e.printStackTrace();
	      }
	  }
      }
      
      private int tBestDepth = Integer.MAX_VALUE;
      private final BlockingQueue<Predecessor> tQueue;
    }

          
    private final BlockingQueue<Predecessor> mDataQueue;
    private final PredecessorProducer mProducer;
    private final PredecessorConsumer mConsumer;
    
    private final Object mSearchMonitor = new Object();
    private StateTuple mSearchState = null;
    private PredecessorCallback mCallback = null;
  }

  //####################################################################

  private String mWorkerID = null;
  private StateDistribution mStateDistribution = null;
  private StateEncoding mStateEncoding = null;
  private Job mJob = null;
  private List<Thread> mThreads = new ArrayList<Thread>();

  private ProductDESSchema mModel = null;
  
  /**
   * List of states which have been visited, or need to be visited on
   * this node. This acts both as a list and a queue.
   *
   * This object is also used for synchronisation in the addState
   * method.
   */
  private List<StateTuple> mStateList;
  
  //This is used like a set but is actually a map because 
  //we need to be able to retrieve the actual state tuple from
  //the set, which isn't possible in the standard set interface.
  private THashMap<StateTuple,StateTuple> mObservedSet;
  private int mCurrentStateIndex = 0;

  /**
   * A count of incoming states to this node. This is incremented
   * after each addState call, and should be protected by the
   * mStateList monitor when modified. States that have already been
   * observed are counted as well.
   */
  private int mRunningProcessingThreads = 0;
  private long mIncomingStateCounter = 0;
  private long mOutgoingStateCounter = 0;
  private boolean mIdleCanaryFlag = false;
  
  //An object, for the purpose of synchronisation
  private final Object mOutgoingLock = new Object();

  private TransitionTable[] mPlantTransitions;
  private TransitionTable[] mSpecTransitions;
 
  //An array of transition tables, indexed by automaton id.
  private TransitionTable[] mTransitionTables;

  private final Object mBadStateLock = new Object();
  private StateTuple mBadState = null;
  private int mBadEvent = -1;

  private final Object mWorkerState = new Object();
  private boolean mPausedState = false;
  private boolean mKillState = false;

  private final PredecessorSearch mPredecessorSearch = new PredecessorSearch();
}