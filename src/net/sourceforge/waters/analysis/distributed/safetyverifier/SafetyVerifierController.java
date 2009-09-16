package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sourceforge.waters.analysis.distributed.application.AbstractController;
import net.sourceforge.waters.analysis.distributed.application.Controller;
import net.sourceforge.waters.analysis.distributed.application.ControllerID;
import net.sourceforge.waters.analysis.distributed.application.ErrorCallback;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.application.Node;
import net.sourceforge.waters.analysis.distributed.application.Worker;
import net.sourceforge.waters.analysis.distributed.VerificationJob;
import net.sourceforge.waters.analysis.distributed.VerificationJobResult;

import net.sourceforge.waters.analysis.distributed.schemata.*;

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
    WorkerErrorCallback realErrorCallback = new WorkerErrorCallback();
    ErrorCallback errorCallback = 
      (ErrorCallback)UnicastRemoteObject.exportObject(realErrorCallback, 0);

    //Catch any exceptions, use this to unexport the error callback.
    try
      {
	System.out.println("Running safety verifier controller!");
	
	if (getNodes() == null)
	  throw new IllegalStateException("No nodes was collection set");
	
	if (getJob() == null)
	  throw new IllegalStateException("No job was set");
	
	
	Collection<Node> nodes = getNodes();
	SafetyVerificationJob job = new SafetyVerificationJob(getJob());
	
	//Build a schematic of the model, on which the model checking will
	//be done.
	mActualModel = job.getModel();
	KindTranslator translator = job.getKindTranslator();
	mModel = SchemaBuilder.build(mActualModel, translator);
	mStateEncoding = new PackedStateEncoding(mModel);
	((PackedStateEncoding)mStateEncoding).outputDebugging();
	//mStateEncoding = new NullStateEncoding(mModel);
	
	//Create workers. If this fails the job will fail. Oh well.
	SafetyVerifierWorker[] workers = new SafetyVerifierWorker[nodes.size()];
	int k = 0;
	for (Node n : nodes)
	  {
	    workers[k] = 
	      (SafetyVerifierWorker) n.createWorker(getControllerID(), WORKER_CLASS, errorCallback);
	    k++;
	  }
	
	
	//Create a set of unique worker IDs.
	Set<String> workerIdSet = new HashSet<String>();
	while (workerIdSet.size() < workers.length)
	  {
	    workerIdSet.add(UUID.randomUUID().toString());
	  }
	String[] workerIDs = workerIdSet.toArray(new String[0]);
	
	//Create the state distribution function, using a parameter
	//from the job.
	StateDistribution stateDist = null; 
	String distname = job.getStateDistribution();
	if ("prototype".equals(distname))
	  stateDist = new PrototypeStateDistribution(workerIDs, mModel, mStateEncoding, 8);
	else 
	  stateDist = new HashStateDistribution(workerIDs);
	
	for (int i = 0; i < workers.length; i++)
	  {
	    String id = workerIDs[i];
	    stateDist.setHandler(id, workers[i]);
	  }
	
	
	//Set job parameters on the workers.
	for (int i = 0; i < workers.length; i++)
	  {
	    SafetyVerifierWorker w = workers[i];
	    w.setJob(getJob());
	    w.setModelSchema(mModel);
	    w.setStateEncoding(mStateEncoding);
	    w.setWorkerID(workerIDs[i]);
	    w.setStateDistribution(stateDist);
	  }
	
	//Start some processing threads
	for (SafetyVerifierWorker w : workers)
	  {
	    w.startProcessingThreads(2, 8192);
	  }
	
	
	//Establish the walltime limit. This is stored as the absolute
	//time when the job should be terminated in milliseconds. This
	//makes testing easier. If there is no limit, the value will be
	//less than zero.
	long startTime = System.currentTimeMillis();
	long walltimeLimit = -1;
	if (job.getWalltimeLimit() != null)
	  walltimeLimit = job.getWalltimeLimit() * 1000 + startTime;
	
	System.err.format("The time is %d. The limit is %d\n", startTime, 
			  walltimeLimit);
	
	
	//Wowza! Add the first state!
	StateTuple initial = findInitialState();
	
	stateDist.addState(initial);
	
	StateTuple badState = null;
	
	//Store the total message counts for the purposes of
	//worker stats.
	long totalIncoming = 0;
	long totalOutgoing = 0;
	int totalStates = 0;
	boolean timeUp = false;
	
	while (true)
	  {
	    boolean bad = false;
	    
	    totalStates = 0;
	    long total_incoming1 = 0;
	    long total_incoming2 = 0;
	    
	    //Outgoing counts start at 1 to account for adding the
	    //initial state.
	    long total_outgoing1 = 1;
	    long total_outgoing2 = 1;
	    
	    for (SafetyVerifierWorker w : workers)
	      {
		w.startIdleTest();
	      }
	    
	    
	    for (SafetyVerifierWorker w : workers)
	      {
		StateTuple s = w.getBadState();
		if (s != null)
		  {
		    //Store the first bad state that is found
		    if (badState == null)
		      badState = s;
		    
		    bad = true;
		  }
		
		totalStates += w.getStateCount();
		total_incoming1 += w.getIncomingStateCount();
		total_outgoing1 += w.getOutgoingStateCount();
	      }
	    
	    for (SafetyVerifierWorker w : workers)
	      {
		total_incoming2 += w.getIncomingStateCount();
		total_outgoing2 += w.getOutgoingStateCount();
	      }
	    
	    boolean still_running = false;
	    for (SafetyVerifierWorker w : workers)
	      {
		if (!w.finishIdleTest())
		  still_running = true;
	      }
	    
	    //Condition for termination: all counters are equal and
	    //system is not still running (i.e. all nodes are idle)
	    boolean terminate = total_incoming1 == total_incoming2 
	      && total_outgoing1 == total_outgoing2 
	      && total_incoming1 == total_outgoing1
	      && !still_running;
	    
	    //Also terminate if there is a bad state.
	    if (badState != null)
	      terminate = true;
	    
	    System.out.format("%b %b %b %d %d %d\n", !bad, terminate, still_running,
			      totalStates, 
			      total_incoming1, 
			      total_outgoing1);
	    
	    //Check if the job has used up its time limit. This won't
	    //terminate the job if it just finished.
	    if (!terminate && walltimeLimit > 0 && walltimeLimit < System.currentTimeMillis())
	      {
		timeUp = true;
		terminate = true;
		System.err.println("Job walltime expired, terminating");
	      }
	    
	    if (terminate)
	      {
		totalIncoming = total_incoming1;
		totalOutgoing = total_outgoing1;
		break;
	      }
	    
	    Thread.sleep(2000);
	  }
	
	long explorationTime = System.currentTimeMillis() - startTime;
	
	//Pausing the workers would be a good idea. Also check
	//the bad state now. This tries to find the best available
	//bad state to start from (as there could be multiple).
	int bestDepth = Integer.MAX_VALUE;
	badState = null;
	int badEvent = -1;
	for (SafetyVerifierWorker w : workers)
	  {
	    w.pause();
	    
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
	
	SafetyVerificationJobResult result = new SafetyVerificationJobResult();
	result.setName(job.getName());
	
	long traceTime = 0;
	
	if (badState != null)
	  {
	    long traceStart = System.currentTimeMillis();
	    int[] trace = findCounterExample(badState, badEvent, initial, workers);
	    
	    //Convert trace into event objects
	    EventProxy[] ntrace = translateTraceToEvents(trace);
	    
	    traceTime = System.currentTimeMillis() - traceStart;
	    
	    result.setResult(false);
	    result.setTrace(ntrace);
	  }
	else if (timeUp)
	  {
	    result.setResult(false);
	    result.setException(new Exception("Job timed out"));
	  }
	else
	  {
	    result.setResult(true);
	    result.setTrace(null);
	  }
	
	
	//Collect up statistics of interest.
	JobStats[] workerStats = new JobStats[workers.length];
	for (int i = 0; i < workers.length; i++)
	  {
	    workerStats[i] = workers[i].getWorkerStats();
	  }
	
	
	JobStats controllerStats = new JobStats();
	controllerStats.set("job-name", job.getName());
	controllerStats.set("worker-stats", workerStats);
	controllerStats.set("bad-state-depth", bestDepth);
	controllerStats.set("total-states", totalStates);
	controllerStats.set("total-incoming", totalIncoming);
	controllerStats.set("total-outgoing", totalOutgoing);
	
	//Timing information
	controllerStats.set("walltime-expired", timeUp);
	controllerStats.set("exploration-time", explorationTime);
	controllerStats.set("trace-time", traceTime);
	controllerStats.set("walltime-limit", job.getWalltimeLimit());
	
	result.setJobStats(controllerStats);
	
	setResult(result);    
      }
    catch (Throwable t)
      {
	//Something happened! Unexport remote objects.
	UnicastRemoteObject.unexportObject(realErrorCallback, true);

	if (t instanceof Exception)
	  throw (Exception)t;
	else
	  throw new RuntimeException(t);
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
      mErrored = true;
      mErrorMap.put(workerid, throwable);
    }

    public synchronized Throwable getErrorForWorker(String workerid)
    {
      return mErrorMap.get(workerid);
    }

    public synchronized Throwable getLastError()
    {
      return mLastError;
    }

    public synchronized boolean hasErrored()
    {
      return mErrored;
    }

    private boolean mErrored = false;
    private Throwable mLastError = null;
    private Map<String,Throwable> mErrorMap = new HashMap<String,Throwable>();
  }


  private ProductDESProxy mActualModel;
  private ProductDESSchema mModel;
  private StateEncoding mStateEncoding;

  private static final String WORKER_CLASS = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierWorkerImpl";
}