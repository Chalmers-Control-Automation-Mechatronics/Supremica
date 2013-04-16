package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sourceforge.waters.analysis.distributed.application.AbstractController;
import net.sourceforge.waters.analysis.distributed.application.ErrorCallback;
import net.sourceforge.waters.analysis.distributed.application.Node;
import net.sourceforge.waters.analysis.distributed.application.Worker;
import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.analysis.distributed.schemata.SchemaBuilder;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class SafetyVerifierController extends AbstractController
{
  public SafetyVerifierController()
  {
  }

  protected void executeController() throws Exception
  {
    //Create an error callback remote object for the controller.
    mRealCallback = new WorkerErrorCallback();
    mErrorCallback = (ErrorCallback)
      UnicastRemoteObject.exportObject(mRealCallback, 0);

    SafetyVerificationJobResult result = new SafetyVerificationJobResult();

    try
      {
	initialise();

	result.setName(mJob.getName());

	createWorkers();
	initialiseStateDistribution();
	startWorkerProcessingThreads();

	//Begin state exploration.
	try
	  {
	    controlStateExploration();
	  }
	catch (TimeupException ex)
	  {
	    //Job time limit exceeded.
	    throw new RuntimeException(ex);
	  }
	catch (AsyncWorkerException ex)
	  {
	    //Do something!
	    throw new RuntimeException(ex);
	  }
	catch (StateExplorationException ex)
	  {
	    //State exploration failed, do some cleanup
	    //here, collect stats etc.
	    throw new RuntimeException(ex);
	  }


	//Figure out the result of the state exploration.
	//This means finding if a bad state was found. For
	//the purposes of creating a counter-example, we 
	//find the best bad state (if more than one exists),
	//as well as the bad event (which caused the property
	//to fail).
	int bestDepth = Integer.MAX_VALUE;
	StateTuple badState = null;
	int badEvent = -1;
	for (SafetyVerifierWorker w : mWorkers)
	  {
	    StateTuple s = w.getBadState();
	    if (s != null)
	      {
		if (bestDepth >= s.getDepthHint())
		  {
		    //This should not be a race condition, the workers
		    //will only store the state/event if there is
		    //no current state, so it isn't necessary to
		    //get the state and event at the same time here.
		    badState = s;
		    badEvent = w.getBadEvent();
		    bestDepth = s.getDepthHint();
		  }
	      }
	  }

	//If no bad state was found, then the verification
	//result is true.
	boolean verificationResult = badState == null;

	result.setName(mJob.getName());
	result.setResult(verificationResult);

	//If the verification was unsuccessful, then find
	//a counter-example.
	if (verificationResult == false)
	  {
	    long traceStart = System.currentTimeMillis();
	    int[] trace = findCounterExample(badState, badEvent, mInitialState, mWorkers);

	    //Convert trace into event objects
	    EventProxy[] ntrace = translateTraceToEvents(trace);
	    
	    mTraceTime = System.currentTimeMillis() - traceStart;
	    result.setTrace(ntrace);
	  }
      }
    catch (Throwable e)
      {
	//Can't just throw e, because the compiler will want 
	//us to declare the method as throwing Throwable, but
	//that's too general and shouldn't happen.
	if (e instanceof Error)
	  throw (Error)e;
	else if (e instanceof Exception)
	  {
	    result.setException((Exception)e);
	    throw (Exception)e;
	  }
	else
	  throw new RuntimeException("Caught an unexpected throwable!", e);
      }
    finally
      {
	//Try and get stats for the job. On a successful run there shouldn't be any problems with this
	result.setJobStats(getControllerStats());
	setResult(result);

	//Unexport the error callback.
	UnicastRemoteObject.unexportObject(mRealCallback, true);
      }
  }

  /**
   * Initialise some data for the job, construct model schematic.
   */
  private void initialise() throws Exception
  {
    if (getNodes() == null)
      throw new IllegalArgumentException("No nodes collection was set");
    if (getJob() == null)
      throw new IllegalArgumentException("No job was set");

    mNodes = getNodes();
    
    if (mNodes.size() == 0)
      throw new IllegalArgumentException("Node collection was empty");
    mJob = new SafetyVerificationJob(getJob());

    //Extract parameters from the job.
    mActualModel = mJob.getModel();
    
    if (mActualModel == null)
      throw new IllegalArgumentException("No model was set");

    mTranslator = mJob.getKindTranslator();

    if (mTranslator == null)
      throw new IllegalArgumentException("No KindTranslator was set");

    try
      {
	mModel = SchemaBuilder.build(mActualModel, mTranslator);
      }
    catch (Exception e)
      {
	throw new RuntimeException("Building model schematic failed", e);
      }

    mStateEncoding = createStateEncoding();
  }

  private StateEncoding createStateEncoding()
  {
    return new GreedyPackedStateEncoding(mModel);
  }

  private void createWorkers() throws Exception
  {
    mWorkers = new SafetyVerifierWorker[mNodes.size()];
    
    int k = 0;
    for (Node n : mNodes)
      {
	mWorkers[k++] =
	  (SafetyVerifierWorker) n.createWorker(getControllerID(), WORKER_CLASS, mErrorCallback);
      }
						
    //Create some unique IDs for the workers.
    Set<String> workerIdSet = new HashSet<String>();
    while (workerIdSet.size() < mWorkers.length)
      {
	workerIdSet.add("worker-" + UUID.randomUUID().toString());
      }

    mWorkerIDs = workerIdSet.toArray(new String[0]);
    
    //Set parameters on workers.
    for (int i = 0; i < mWorkers.length; i++)
      {
	SafetyVerifierWorker w = mWorkers[i];
	w.setJob(getJob());
	w.setModelSchema(mModel);
	w.setStateEncoding(mStateEncoding);
	w.setWorkerID(mWorkerIDs[i]);
      }
  }

  private void initialiseStateDistribution() throws Exception
  {
    mStateDistribution = createStateDistribution();

    for (int i = 0; i < mWorkers.length; i++)
      {
	String id = mWorkerIDs[i];
	mStateDistribution.setHandler(id, mWorkers[i]);
      }

    for (SafetyVerifierWorker w : mWorkers)
      {
	w.setStateDistribution(mStateDistribution);
      }
  }

  private StateDistribution createStateDistribution() throws Exception
  {
    String distname = mJob.getStateDistribution();
    if ("prototype".equals(distname))
      return new PrototypeStateDistribution(mWorkerIDs, mModel, mStateEncoding, 8);
    else if ("lowestprob".equals(distname))
      {
	ProbabilityEstimator est = new ChangeProbabilityEstimator(mModel);
	AutomataSelector as =  new LowestProbabilitySelector
	  (mModel, est, Util.clog2(mWorkerIDs.length * 2));
	return new SelectorDistribution(mWorkerIDs, mModel, mStateEncoding, as);
      }
    else if ("independent_events".equals(distname))
      {
	ProbabilityEstimator est = new ChangeProbabilityEstimator(mModel);
	AutomataSelector as =  new IndependentEventsSelector
	  (mModel, est);
	return new SelectorDistribution(mWorkerIDs, mModel, mStateEncoding, as);

      }
    else
      return new HashStateDistribution(mWorkerIDs);
  }

  private void startWorkerProcessingThreads() throws Exception
  {
    for (SafetyVerifierWorker w : mWorkers)
      {
	w.startProcessingThreads(2, 8192);  //Arbitrarily chosen numbers
      }
  }

  //Some specific exceptions for dealing with various failure modes.
  private static class StateExplorationException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public StateExplorationException(String message, Exception e)
    {
      super(message, e);
    }
  }

  /**
   * A worker has failed asynchronously, this exception is thrown when
   * something detects the failure and needs to handle it.
   */
  private static class AsyncWorkerException extends Exception
  {
	private static final long serialVersionUID = 1L;

	public AsyncWorkerException(String message, String workerid, Throwable e)
    {
      super(message, e);
      
      mWorkerID = workerid;
    }

    @SuppressWarnings("unused")
	public String getWorkerID()
    {
      return mWorkerID;
    }

    private String mWorkerID;
  }

  private static class TimeupException extends StateExplorationException
  {
    private static final long serialVersionUID = 1L;

    public TimeupException(String message)
    {
      super(message, null);
    }
  } 

  /**
   * Control logic for the state exploration. This is a 
   * fairly long bit of code.
   *
   * A StateExplorationException will be thrown if exploration
   * fails. Upon termination of exploration, all workers will be
   * paused to prevent further state exploration.
   * @throws StateExplorationException if something bad happens.
   */
  private void controlStateExploration() 
    throws StateExplorationException, AsyncWorkerException
  {
    //Establish the walltime limit. This is stored as the absolute
    //time when the job should be terminated in milliseconds. This
    //makes testing easier. If there is no limit, the value will be
    //less than zero.
    long startTime = System.currentTimeMillis();
    long walltimeLimit = -1;

    if (mJob.getWalltimeLimit() != null)
      walltimeLimit = mJob.getWalltimeLimit() * 1000 + startTime;

    try
      {
	//At this point, the workers are all waiting for something
	//to do. To start the state exploration, we just need to
	//add the initial state to the appropriate worker.
	mInitialState = findInitialState();
	mStateDistribution.addState(mInitialState);
	
	//Reasons for termination. Used after the main
	//while loop to figure out the current state.
    exploration: while (true)
	  {
	    //For termination detection, the total number of incoming and
	    //outgoing messages are counted twice.  If all the counts are
	    //equal, then there has been no communication between workers
	    //between the counts.  This is the 4 counter algorithm from
	    //"Algorithms for distributed termination detection"
	    //(Friedemann Mattern, 1987). The technique published in that
	    //paper assumes messages can only be sent when the node is
	    //idle, however this is not the case in this program.  In
	    //addition to the message counts, an idle test is done on each
	    //worker, which checks if they do any significant work during
	    //the termination test.

	    //Outgoing message counts adjusted to include the initial 
	    //state being dispatched to the appropriate worker.
	    long totalIncoming1 = 0;
	    long totalOutgoing1 = 1;
	    long totalIncoming2 = 0;
	    long totalOutgoing2 = 1;
	    long totalStates = 0;
	    
	    for (SafetyVerifierWorker w : mWorkers)
	      w.startIdleTest();

	    
	    //Check for bad states. If found, then set
	    //flag and break out of loop
	    for (SafetyVerifierWorker w : mWorkers)
	      if (w.getBadState() != null)
		{
		  break exploration;
		}

	    //Do first count, also count the number of
	    //explored states, so that status can be 
	    //printed out.
	    for (SafetyVerifierWorker w : mWorkers)
	      {
		totalStates += w.getStateCount();
		totalIncoming1 += w.getIncomingStateCount();
		totalOutgoing1 += w.getOutgoingStateCount();
	      }

	    //Count states a second time, so that we can
	    //ensure nothing has happened.
	    for (SafetyVerifierWorker w :mWorkers)
	      {
		totalIncoming2 += w.getIncomingStateCount();
		totalOutgoing2 += w.getOutgoingStateCount();
	      }

	    //Determine if the workers have run while counting
	    //messages (indicative of some kind of activity)
	    boolean stillRunning = false;
	    for (SafetyVerifierWorker w : mWorkers)
	      if (!w.finishIdleTest())
		{
		  stillRunning = true;
		  break;
		}

	    //Calculate the termination 'predicate' -- if the
	    //counters are all equal and the workers are not
	    //still running.
	    boolean terminate = totalIncoming1 == totalIncoming2
	      && totalOutgoing1 == totalOutgoing2
	      && totalIncoming1 == totalOutgoing1
	      && !stillRunning;

	    System.out.format("%b %d %d %d\n", terminate, totalStates, totalIncoming2, totalOutgoing2);

	    //Update some global stat counters now. This means if it
	    //terminates, we won't lose them
	    mTotalStates = totalStates;
	    mTotalIncoming = totalIncoming2;
	    mTotalOutgoing = totalOutgoing2;

	    //Check the termination predicate and store any
	    //necessary state.
	    if (terminate)
	      {
		break exploration;
	      }

	    if (walltimeLimit > 0 && walltimeLimit < System.currentTimeMillis())
	      {
		throw new TimeupException("Job walltime exceeded");
	      }

	    //Check for asynchronous errors.
	    if (mRealCallback.hasErrored())
	      {
		Throwable t = mRealCallback.getFirstError();
		String workerid = mRealCallback.getFirstErrorWorkerID();
		throw new AsyncWorkerException("Asynchronous worker error detected from " + workerid,
					       workerid,
					       t);
	      }

	    
	    updatePeriodicStats();

	    Thread.sleep(2000);
	  }
      }
    catch (Exception e)
      {
	throw new StateExplorationException("Exploration failed", e);
      }
    finally
      {
	mExplorationTime = System.currentTimeMillis() - startTime;

	//Pause workers.
	for (SafetyVerifierWorker w : mWorkers)
	  {
	    try
	      {
		w.pause();
	      }
	    catch (Exception e)
	      {
		//Excuses. This could probably be made more descriptive.
		System.err.format("Worker didn't pause?");
	      }
	  }
      }
  }
  
  private JobStats[] getWorkerStats()
  {
    if (mWorkers == null) {
      return null;
    } else {
      final JobStats[] stats = new JobStats[mWorkers.length];
      for (int i = 0; i < stats.length; i++) {
	if (mWorkers[i] != null) {
	  try {
	    stats[i] = mWorkers[i].getWorkerStats();
	  } catch (RemoteException e) {
	    //Oh well, null shall be stored
	  }
	}
	//If no stats were obtained (for whatever reason),
	//try the periodic stats
	if (stats[i] == null && mPeriodicStats != null) {
	  stats[i] = mPeriodicStats[i];
	}
      }
      return stats;
    }
  }
  
  private JobStats getControllerStats()
  {
    JobStats stats = new JobStats();
    if (mJob != null) {
      stats.set("job-name", mJob.getName());
    }
    stats.set("worker-stats", getWorkerStats());
    
    stats.set("exploration-time", mExplorationTime);
    stats.set("trace-time", mTraceTime);

    stats.set("total-states", mTotalStates);
    stats.set("total-incoming", mTotalIncoming);
    stats.set("total-outgoing", mTotalOutgoing);
    
    return stats;
  }


  /**
   * Keep an archive of the last good stats for each worker.
   * This means that if a worker goes down we have a record of it.
   */
  private void updatePeriodicStats()
  {
    //The get worker stats method also looks at the periodic stats
    //table. This isn't really a problem but it means we will get 
    //given archived versions of stats.
    JobStats[] stats = getWorkerStats();

    if (mPeriodicStats == null)
      mPeriodicStats = stats;
    else
      {
	for (int i = 0; i < stats.length; i++)
	  {
	    if (stats[i] != null)
	      {
		stats[i].set("archived", true);
		mPeriodicStats[i] = stats[i];
	      }
	  }
      }
  }


  /**
   * Translates event ids into EventProxy objects. This is used to get
   * a trace into a form that can be sent back to the client and then 
   * used to build a real safety trace object.
   */
  private EventProxy[] translateTraceToEvents(int[] trace)
  {
    Map<String,EventProxy> eventmap = createEventNameMap(mActualModel);
    EventProxy[] ntrace = new EventProxy[trace.length];

    for (int i = 0; i < trace.length; i++)
      {
	ntrace[i] = eventmap.get(mModel.getEvent(trace[i]).getName());
      }

    return ntrace;
  }

  /**
   * Creates a map of event names to EventProxy objects. This method
   * assumes that event names are unique.
   */
  private Map<String,EventProxy> createEventNameMap(ProductDESProxy model)
  {
    Map<String,EventProxy> events = new HashMap<String,EventProxy>();

    for (EventProxy ep : model.getEvents())
      {
	events.put(ep.getName(), ep);
      }

    return events;
  }

  private StateTuple findInitialState()
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

    return mStateEncoding.encodeState(start, 0);
  }


  private int[] findCounterExample(StateTuple bad, 
				   int badevent, 
				   StateTuple initial, 
				   SafetyVerifierWorker[] workers) 
    throws Exception
  {
    TraceFinder tf = new TraceFinder(mModel, mStateEncoding, workers);
    return tf.findTrace(bad, badevent, initial);
  }

  /**
   * Callback for handling errors that occur in a worker 
   * instance.
   */
  private class WorkerErrorCallback implements ErrorCallback
  {
    public synchronized void handle(String workerid, Worker worker, Throwable throwable)
    {
      //System.err.format("An error occurred: %s\n", throwable);
      //throwable.printStackTrace();
      mErrored = true;
      mErrorMap.put(workerid, throwable);

      if (mFirstError == null)
	{
	  mFirstError = throwable;
	  mFirstErrorWorkerID = workerid;
	}
    }

    @SuppressWarnings("unused")
	public synchronized Throwable getErrorForWorker(String workerid)
    {
      return mErrorMap.get(workerid);
    }

    public synchronized Throwable getFirstError()
    {
      return mFirstError;
    }

    public synchronized String getFirstErrorWorkerID()
    {
      return mFirstErrorWorkerID;
    }

    public synchronized boolean hasErrored()
    {
      return mErrored;
    }

    private boolean mErrored = false;
    private Throwable mFirstError = null;
    private String mFirstErrorWorkerID = null;
    private Map<String,Throwable> mErrorMap = new HashMap<String,Throwable>();
  }


  private WorkerErrorCallback mRealCallback;
  private ErrorCallback mErrorCallback;
  private Collection<Node> mNodes;
  private SafetyVerificationJob mJob;
  private KindTranslator mTranslator;
  private SafetyVerifierWorker[] mWorkers;
  private String[] mWorkerIDs;
  private ProductDESProxy mActualModel;
  private ProductDESSchema mModel;
  private StateEncoding mStateEncoding;
  private StateDistribution mStateDistribution;
  private StateTuple mInitialState = null;

  private JobStats[] mPeriodicStats = null;

  //Values for timekeeping and other stats.
  private long mExplorationTime = 0;
  private long mTraceTime = 0;
  private long mTotalStates = 0;
  private long mTotalIncoming = 0;
  private long mTotalOutgoing = 0;

  private static final String WORKER_CLASS = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierWorkerImpl";
}