package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;

public class ThreadedOutputDispatcher extends AbstractOutputDispatcher
{
  public ThreadedOutputDispatcher(StateDistribution dist, int threadcount)
  {
    super(dist);
    //An arbitrary, somewhat big queue. We don't want to
    //block the processing threads until there really is a 
    //large backlog of states.
    mOutputQueue = new ArrayDeque<StateTuple>(32768);

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
	mOutputQueue.addLast(state);
	mOutputQueue.notify();
      }
  }

  public void addStates(StateTuple[] states, int offset, int length) throws InterruptedException
  {
    synchronized (mOutputQueue)
      {
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
		  //XXX: what to do here?
		  //The model verification is possibly compromised
		  throw new WatersRuntimeException("Asynchronous add state " + 
						   "failed in dispatch thread. " +
						   "Verification compromised?");
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
	while (mOutputQueue.size() == 0)
	  {
	    mOutputQueue.wait();
	  }

	StateTuple t = mOutputQueue.removeFirst();
	return t;
      }
  }

  public void shutdown()
  {
    //Interrupt all dispatch threads then wait for them
    //to terminate
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
}