package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.distributed.application.AbstractWorker;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.EventSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;


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

  public ProductDESSchema getModelSchema()
  {
    return mModel;
  }

  public void setStateDistribution(StateDistribution stateDist)
  {
    if (getWorkerID() == null)
      throw new IllegalStateException("Worker ID was not set");

    mStateDistribution = stateDist;

    //Set the state handler to the local object. This will
    //prevent calls from going through the RMI system for 
    //local states.
    mLocalHandler = new CountingStateHandler(this);
    mStateDistribution.setHandler(getWorkerID(), mLocalHandler);

    //mOutputDispatcher = new SynchronousOutputDispatcher(mStateDistribution);
    //Use 16Mb of memory for the output queue, at which point the 
    //processing threads will be blocked until the queue empties.
    mOutputDispatcher = new ThreadedOutputDispatcher(this, mStateDistribution, 8, 1 << 22);
  }


  public StateDistribution getStateDistribution()
  {
    return mStateDistribution;
  }


  public void setStateEncoding(StateEncoding encoding)
  {
    mStateEncoding = encoding;

    //Now we can get the encoded state length and create
    //the state storage. There should be a better place to
    //do this, like a 'ready' function. Maybe another day.
    mStateStorage = new SlabStateStorage(mStateEncoding.getEncodedLength());
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
    synchronized (mIncomingLock)
      {
	mStateStorage.addState(state);
	mIncomingStateCounter++;

	//Notify any threads that might be waiting for new states.
	mIncomingLock.notifyAll();
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
      mOutgoingBuffer = new StateTuple[bufferSize];
      
      //A small cache for recently queued states.
      mOutputCache = new HashCache(1 << 18);
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

	      //May as well flush the buffer now.
	      flushOutputBuffer();
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
      synchronized (mIncomingLock)
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

		  mIncomingLock.wait();
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
	  
	  //Eventually dispatch the state.

	  StateTuple packed = mStateEncoding.encodeState(successor, state.getDepthHint() + 1);
	  
	  //If the state has been recently dispatched, there is no reason
	  //to dispatch it again. This can be done by checking if it is 
	  //inside the cache.
	  if (!mOutputCache.contains(packed))
	    {
	      mOutputCache.put(packed);
	      outputState(packed);
	    }
	}
    }
    
    
    //Queue a state to be dispatched in the outgoing buffer, and send 
    //if the buffer is full.
    private void outputState(StateTuple state)
    {
      try
	{
	  mOutputDispatcher.addState(state);
	  synchronized (mOutgoingLock)
	    {	  
	      mOutgoingStateCounter++;
	    }
	}
      catch (Exception e)
	{
	  handle(new RuntimeException("Dispatch state failed", e));
	}
      
      /*
      mOutgoingBuffer[mOutgoingBufferIndex++] = state;

      if (mOutgoingBufferIndex >= mOutgoingBuffer.length)
	{
	  flushOutputBuffer();
	}
      */
      
    }


    private void flushOutputBuffer()
    {
      /* 
      try
	{
	  //Send the state to the output dispatcher and
	  //increment the outgoing counter. This means that
	  //once the state `leaves' the processing thread, it is
	  //considered in transit, even if it is sitting in a queue
	  //waiting to be sent. This distinction isn't important to
	  //the application as the network traffic could be sitting
	  //in operating system buffers or something anyway.
	  mOutputDispatcher.addStates(mOutgoingBuffer, 0, mOutgoingBufferIndex);
	  synchronized (mOutgoingLock)
	    {
	      mOutgoingStateCounter += mOutgoingBufferIndex;
	    }
	}
      catch (Exception e)
	{
	  throw new RuntimeException(e);
	}

      mOutgoingBufferIndex = 0;
      */
    }

      

    @SuppressWarnings("unused")
	private final StateTuple[] mOutgoingBuffer;
    @SuppressWarnings("unused")
	private int mOutgoingBufferIndex = 0;
    private final int mBufferSize;
    private final HashCache mOutputCache; 
  }

    
  /**
   * Gets the number of unprocessed states. This method synchronises on
   * the state list.
   * @return the number of unprocessed states.
   */
  private int unprocessedStates()
  {
    synchronized (mIncomingLock)
      {
	return mStateStorage.getUnprocessedStateCount();
      }
  }


  private StateTuple getNextState()
  {
    synchronized (mIncomingLock)
      {
	return mStateStorage.getNextState();
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
    return mStateStorage.getProcessedStateCount();
  }


  public long getIncomingStateCount()
  {
    synchronized (mIncomingLock)
      {
	return mIncomingStateCounter;
      }
  }


  public long getOutgoingStateCount()
  {
    synchronized (mOutgoingLock)
      {
	return mOutgoingStateCounter;
      }
  }


  public int getWaitingStateCount()
  {
    synchronized (mIncomingLock)
      {
	return mStateStorage.getUnprocessedStateCount();
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
    synchronized (mIncomingLock)
      {
	mIncomingLock.notifyAll();
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
    synchronized (mIncomingLock)
      {
	mIncomingLock.notifyAll();
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
    synchronized (mIncomingLock)
      {
	mIncomingLock.notifyAll();
      }
  }

  
  public void created() throws Exception
  {
    super.created();
  }


  public void deleted()
  {
    super.deleted();
    System.err.format("deleted() called on safety verifier worker\n");

    //Clean up running threads so the object can be garbage collected.
    kill();

    if (mOutputDispatcher != null)
      {
	mOutputDispatcher.shutdown();
	mOutputDispatcher = null;
      }

    if (mPredecessorSearch != null)
      {
	mPredecessorSearch.shutdown();
	mPredecessorSearch = null;
      }

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
   * Override the basic error handling mechanism to pause the
   * worker.
   * @param throwable error to handle.
   */
  public void handle(Throwable throwable)
  {
    //Pause the worker. This should attempt to stop the bork.
    pause();

    super.handle(throwable);
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
    if (mPredecessorSearch == null)
      {
	mPredecessorSearch = new PredecessorSearch(this);
      }

    mPredecessorSearch.setSearchTarget(original, callback);
  }


  public void startIdleTest()
  {
    synchronized (mIncomingLock)
      {
	//We initialise the idle flag to whether the system appears to
	//be idle now. If it is idle then any changes to the state will
	//hopefully be detected by the idle canary.
	mIdleCanaryFlag = appearsIdle();
      }
  }


  public boolean finishIdleTest()
  {
    synchronized (mIncomingLock)
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


  public JobStats getWorkerStats()
  {
    JobStats stats = new JobStats();
    
    stats.set("worker-id", getWorkerID());
    stats.set("bad-state", getBadState());
    stats.set("bad-event", getBadEvent());
    stats.set("processing-threads", mThreads.size());
    stats.set("job-name", mJob.getName());

    if (mLocalHandler != null)
      {
	stats.set("local-added-states", mLocalHandler.getCount());
      }
    
    synchronized (mIncomingLock)
      {
	if (mStateStorage != null)
	  {
	    stats.set("state-count", mStateStorage.getStateCount());
	    stats.set("processed-states", mStateStorage.getProcessedStateCount());
	    stats.set("unprocessed-states", mStateStorage.getUnprocessedStateCount());
	  }
	stats.set("incoming-state-count", mIncomingStateCounter);
      }

    synchronized (mOutgoingLock)
      {
	stats.set("outgoing-state-count", mOutgoingStateCounter);
      }

    return stats;
  }


  private boolean appearsIdle()
  {
    synchronized (mIncomingLock)
      {
	return getWaitingStateCount() == 0 && mRunningProcessingThreads == 0;
      }
  }


  private void killCanary()
  {
    synchronized (mIncomingLock)
      {
	mIdleCanaryFlag = false;
      }
  }

  public int getStateDepth(StateTuple state)
  {
    synchronized (mIncomingLock)
      {
	return mStateStorage.getStateDepth(state);
      }
  }

  public boolean containsState(StateTuple state)
  {
    synchronized (mIncomingLock)
      {
	return mStateStorage.containsState(state);
      }
  }

  /**
   * Get the transition table for an automaton
   */
  public TransitionTable getTransitionTable(int automaton)
  {
    return mTransitionTables[automaton];
  }

  private StateDistribution mStateDistribution = null;
  private StateEncoding mStateEncoding = null;
  private Job mJob = null;
  private List<Thread> mThreads = new ArrayList<Thread>();

  private ProductDESSchema mModel = null;
  
  private final Object mIncomingLock = new Object();
  
  private StateStorage mStateStorage;

  /**
   * A count of incoming states to this node. This is incremented
   * after each addState call, and should be protected by the
   * mIncomingLock monitor when modified. States that have already been
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

  private volatile PredecessorSearch mPredecessorSearch = null;
  private volatile OutputDispatcher mOutputDispatcher = null;

  //Allows the number of locally added states to be counted. This 
  //should provide useful numbers for testing the effectiveness of 
  //state distribution implementations
  private volatile CountingStateHandler mLocalHandler = null;

  private static final long serialVersionUID = 1L;

}