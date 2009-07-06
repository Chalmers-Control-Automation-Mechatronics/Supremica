package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.THashSet;

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
    mPlantTransitions = generateTransitionTables(AutomatonSchema.PLANT);
    mSpecTransitions = generateTransitionTables(AutomatonSchema.SPECIFICATION);
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
	if (!mObservedSet.contains(state))
	  {
	    mObservedSet.add(state);
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
		setBadState(state);
		
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
	StateTuple packed = mStateEncoding.encodeState(successor);

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
  

  private void setBadState(StateTuple bad)
  {
    synchronized (mBadStateLock)
      {
	//While this could have a concurrent update problem, it shouldn't
	//matter because the assignment will be atomic.
	if (mBadState == null)
	  mBadState = bad;
	
	//The bad state should not be null after this call.
	assert(mBadState != null);

	System.out.println("Set bad state");
      }
  }

  public StateTuple getBadState()
  {
    synchronized (mBadStateLock)
      {
	return mBadState;
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
    mObservedSet = new THashSet<StateTuple>();
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
    callback.takePredecessor(original, null, 42);
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

  /**
   * A class to encapsulate searching for a predecessor state.
   */
  private class PredecessorSearch
  {
    public PredecessorSearch()
    {

    }

    public void setSearchTarget(StateTuple state, PredecessorCallback cb)
    {
      //Set the search running to find predecessors to the given state
      //that are available on this worker. If the predecessor search is 
      //not currently running, then this should be set as the new target. 
    }

    /**
     * An immutable predecessor class. Stores the located predecessor
     * state and the state it is a predecessor to.
     */
    private class Predecessor
    {
      public Predecessor(StateTuple original, StateTuple predecessor)
      {
	mOriginalState = original;
	mPredecessorState = predecessor;
      }
      
      private final StateTuple mOriginalState;
      private final StateTuple mPredecessorState;
    }
    
    private class PredecessorProducer extends Thread
    {
      public PredecessorProducer(BlockingQueue<Predecessor> dataqueue)
      {
	mQueue = dataqueue;
      }
      
      public void run()
      {
	while (true)
	  {
	    
	  }
      }
      
      private final BlockingQueue<Predecessor> mQueue;
    }
    
    
    private class PredecessorConsumer extends Thread
    {
      public PredecessorConsumer(BlockingQueue<Predecessor> dataqueue)
      {
	mQueue = dataqueue;
      }
      
      public void run()
      {
	while (true)
	  {
	    
	  }
      }
      
      private final BlockingQueue<Predecessor> mQueue;
    }
  }


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
  private THashSet<StateTuple> mObservedSet;
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

  private final Object mBadStateLock = new Object();
  private StateTuple mBadState = null;

  private final Object mWorkerState = new Object();
  private boolean mPausedState = false;
  private boolean mKillState = false;
}