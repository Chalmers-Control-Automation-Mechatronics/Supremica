package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import java.rmi.RemoteException;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

public class PredecessorSearch
{
  public PredecessorSearch(SafetyVerifierWorkerImpl worker)
  {
    mWorker = worker;

    //A synchronised queue to share between the producer and
    //consumer. The array blocking queue has a fixed size, 32,768 is
    //probably far too big for any reasonable model, but doesn't take
    //up enough memory for it to matter. Even if this gets full, it
    //doesn't matter, the producer thread will just block.
    mQueue = new ArrayBlockingQueue<Predecessor>(32768);
    mProducer = new PredecessorProducer(mQueue, worker);
    mConsumer = new PredecessorConsumer(mQueue, worker);

    mProducer.start();
    mConsumer.start();
  }


  /**
   * Sets the target for the predecessor search.
   */
  public void setSearchTarget(StateTuple state, PredecessorCallback cb)
  {
    //Stop the producer and consumer while changing parameters.
    mProducer.setStopped(true);
    mConsumer.setStopped(true);
    
    //Reset the search targets.
    mProducer.setSearchState(state);
    mConsumer.setSearchCallback(cb);

    mQueue.clear();

    mProducer.setStopped(false);
    mConsumer.setStopped(false);
  }

  /**
   * Shuts down the predecessor search, causing any threads to be
   * stopped.
   */
  public void shutdown()
  {
    mProducer.setStopped(true);
    mConsumer.setStopped(true);

    mProducer.shutdown();
    mConsumer.shutdown();

    try
      {
	mProducer.join();
	mConsumer.join();
      }
    catch (InterruptedException e)
      {
	//Ignore interrupts and continue.
      }
  }

  private final PredecessorProducer mProducer;
  private final PredecessorConsumer mConsumer;
  private final BlockingQueue<Predecessor> mQueue;
  private final SafetyVerifierWorkerImpl mWorker;
}

class PredecessorProducer extends Thread
{
  public PredecessorProducer(BlockingQueue<Predecessor> dataqueue,
			     SafetyVerifierWorkerImpl worker)
  {
    mQueue = dataqueue;
    mWorker = worker;
    mStateEncoding = worker.getStateEncoding();
  }

  /**
   * Sets the state to generate predecessors from.
   * @param state to search from
   */
  public synchronized void setSearchState(StateTuple state)
  {
    mSearchState = state;
    poke();
  }

  /**
   * Signal for this thread to shutdown.
   */
  public synchronized void shutdown()
  {
    mKill = true;
    poke();
  }

  /**
   * Controls if the producer is stopped. Stopping means the current
   * predecessor search will be interrupted.
   */
  public synchronized void setStopped(boolean value)
  {
    mStopped = value;
    poke();
  }

  private synchronized void poke()
  {
    notifyAll();
    interrupt();
  }
  
  public void run()
  {
    while (true)
      {
	//Get the state we want to search for predecessors
	//to. This will block the thread until we get a state.
	//Interrupting the thread will wake it up and the exception 
	//handler will repeat this loop when this happens.
	StateTuple state = null;
	try
	  {
	    synchronized (this)
	      {
		while (mSearchState == null || mStopped)
		  {
		    //Check if the thread should terminate.
		    if (mKill)
		      return;
		    
		    wait();
		  }
		state = mSearchState;
	      }
	    
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
	synchronized (this)
	  {
	    
	    if (mSearchState == state)
	      mSearchState = null;
	  }
      }
  }

  private void produce(StateTuple state) throws InterruptedException
  {
    ProductDESSchema model = mWorker.getModelSchema();
    int[] pre = new int[model.getAutomataCount()];
    int[] current = mStateEncoding.decodeState(state);
	
    for (int ev = 0; ev < model.getEventCount(); ev++)
      {
	//Expand the predecessor states for the current event.
	expandReverse(ev, pre, state, current, 0);
      }

    //Add an 'end of search' element to the queue. This will
    //cause the consumer to report the search is complete if
    //it gets to that point.
    mQueue.put(new Predecessor(state, null, -1));
  }

  private void expandReverse(int event, 
			     int[] pre, 
			     StateTuple packedCurrent, 
			     int[] current, 
			     int automaton) 
    throws InterruptedException
  {
    //An interrupt signals that we should be doing something else, but
    //we aren't in a blocking call so throw an interrupted exception
    //manually
    if (Thread.interrupted())
      {
	throw new InterruptedException("You called?");
      }

    if (automaton < current.length)
      {
	//Recursively call this method for each predecessor in the
	//current automaton. This will explore all combinations of
	//predecessors.
	TransitionTable tt = mWorker.getTransitionTable(automaton);
	    
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
	StateTuple t = mWorker.getExploredState(pred_state);

	    
	//If the state is not in our local observed set, then
	//we cannot tell if the state is reachable... if it is, some other
	//worker will take care of it
	if (t == null)
	  return;
	else
	  pred_state = t;

	Predecessor p = new Predecessor(packedCurrent, pred_state, event);
	mQueue.put(p);
      }
  }

  private final BlockingQueue<Predecessor> mQueue;
  private final SafetyVerifierWorkerImpl mWorker;
  private final StateEncoding mStateEncoding;
  private volatile StateTuple mSearchState = null;
  private volatile boolean mKill = false;
  private volatile boolean mStopped = false;
}


class PredecessorConsumer extends Thread
{
  public PredecessorConsumer(BlockingQueue<Predecessor> dataqueue,
			     SafetyVerifierWorkerImpl worker)
  {
    mWorker = worker;
    mQueue = dataqueue;
  }

  public synchronized void setSearchCallback(PredecessorCallback cb)
  {
    mCallback = cb;    
    mBestDepth = Integer.MAX_VALUE;
    poke();
  }
  
  public synchronized void shutdown()
  {
    mKill = true;
    poke();
  }
  
  public synchronized void setStopped(boolean value)
  {
    mStopped = value;
    poke();
  }

  private synchronized void poke()
  {
    notifyAll();
    interrupt();
  }
      
  public void run()
  {
    while (true)
      {
	PredecessorCallback cb = null;
	try
	  {
	    synchronized (this)
	      {
		while (mCallback == null || mStopped)
		  {
		    if (mKill)
		      return;
		    
		    wait();
		  }
		cb = mCallback;
	      }
	    
	    
	    Predecessor p = mQueue.take();
		
	    if (p.getPredecessor() == null)
	      {
		cb.searchCompleted(p.getOriginal(),
				   mWorker.getWorkerID());
	      }
	    else
	      {
		int depth = p.getPredecessor().getDepthHint();
		boolean goodPredecessor = false;
		synchronized (this)
		  {
		    goodPredecessor = depth < mBestDepth;
		  }
		    
		if (goodPredecessor)
		  {
		    depth = cb.takePredecessor(p.getOriginal(), 
					       p.getPredecessor(),
					       p.getEvent());
			
		    synchronized (this)
		      {
			mBestDepth = depth;
		      }
		  }
	      }
	  }
	catch (InterruptedException e)
	  {
	    //If interrupted, continue, the main loop
	    //will determine what happens.
	    continue;
	  }
	catch (RemoteException e)
	  {
	    System.err.println("Remote exception in predecessor consumer:");
	    e.printStackTrace();
	  }
      }
  }
      
  private final BlockingQueue<Predecessor> mQueue;
  private final SafetyVerifierWorkerImpl mWorker;
  private PredecessorCallback mCallback;
  private int mBestDepth = Integer.MAX_VALUE;
  private boolean mKill = false;
  private boolean mStopped = false;
}



/**
 * An immutable predecessor class. Stores the located predecessor
 * state and the state it is a predecessor to.
 */
class Predecessor
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