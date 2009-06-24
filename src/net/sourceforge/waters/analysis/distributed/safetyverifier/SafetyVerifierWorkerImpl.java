package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.distributed.application.AbstractWorker;
import net.sourceforge.waters.analysis.distributed.application.Worker;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.Node;

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
  }

  public void startProcessingThreads(int n, int buffersize)
  {
    for (int i = 0; i < n; i++)
      {
	ProcessingThread t = new ProcessingThread(buffersize);
	t.setDaemon(true);
	t.start();
	mProcessingThreads.add(t);
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
	  
	  while (true)
	    {
	      //Refill the buffer, block here until there is work
	      //to do.
	      bufLen = fillBuffer(buffer);
	      
	      //Process items in the buffer
	      for (int i = 0; i < bufLen; i++)
		{
		  //Process the state.
		  StateTuple state = buffer[i];
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
     */ 
    private int fillBuffer(StateTuple[] buffer) throws InterruptedException
    {
      synchronized (mStateList)
	{
	waiting: while (true)
	    {
	      while (unprocessedStates() == 0)
		{
		  mStateList.wait();
		}
	      
	      //There should certainly be some states now. We want to take
	      //as many as we can fit into the buffer, but won't be greedy.
	      int nstates = Math.min(unprocessedStates(), buffer.length);
	      
	      if (nstates == 0)
		continue waiting;
	      
	      //Fill the buffer.
	      for (int i = 0; i < nstates; i++)
		{
		  buffer[i] = getNextState();
		}

	      return nstates;
	    }
	}
    }

    private final int mBufferSize;
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
  }

  private Job mJob = null;
  private List<Thread> mProcessingThreads = new ArrayList<Thread>();

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
  private long mIncomingStateCounter = 0;
  private long mOutgoingStateCounter = 0;

  
}