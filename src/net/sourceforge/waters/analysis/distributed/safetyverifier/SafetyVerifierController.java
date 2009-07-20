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
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.application.Node;
import net.sourceforge.waters.analysis.distributed.application.ControllerID;
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
    System.out.println("Running safety verifier controller!");
    
    if (getNodes() == null)
      throw new IllegalStateException("No nodes was collection set");
    
    if (getJob() == null)
      throw new IllegalStateException("No job was set");


    Collection<Node> nodes = getNodes();
    VerificationJob job = new VerificationJob(getJob());

    //Build a schematic of the model, on which the model checking will
    //be done.
    mActualModel = job.getModel();
    KindTranslator translator = job.getKindTranslator();
    mModel = SchemaBuilder.build(mActualModel, translator);
    mStateEncoding = new NullStateEncoding(mModel);
    
    
    //Create workers. If this fails the job will fail. Oh well.
    SafetyVerifierWorker[] workers = new SafetyVerifierWorker[nodes.size()];
    int i = 0;
    for (Node n : nodes)
      {
	workers[i] = 
	  (SafetyVerifierWorker) n.createWorker(getControllerID(), WORKER_CLASS);
	i++;
      }


    //Create a set of unique worker IDs.
    Set<String> workerIdSet = new HashSet<String>();
    while (workerIdSet.size() < workers.length)
      {
	workerIdSet.add(UUID.randomUUID().toString());
      }
    String[] workerIDs = workerIdSet.toArray(new String[0]);

    //Create the state distribution function
    StateDistribution stateDist = new HashStateDistribution(workerIDs);
    for (i = 0; i < workers.length; i++)
      {
	String id = workerIDs[i];
	stateDist.setHandler(id, workers[i]);
      }


    //Set job parameters on the workers.
    for (i = 0; i < workers.length; i++)
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

    //Wowza! Add the first state!
    StateTuple initial = findInitialState();
    stateDist.addState(initial);

    StateTuple badState = null;

    while (true)
      {
	boolean bad = false;
	
	int total_states = 0;
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

	    total_states += w.getStateCount();
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
			  total_states, 
			  total_incoming1, 
			  total_outgoing1);
	
	if (terminate)
	  break;
	
	Thread.sleep(2000);
      }
  
    //Pausing the workers would be a good idea. Also check
    //the bad state now.
    int bestdepth = Integer.MAX_VALUE;
    badState = null;
    int badEvent = -1;
    for (SafetyVerifierWorker w : workers)
      {
	w.pause();
	
	StateTuple s = w.getBadState();
	if (s != null)
	  {
	    if (bestdepth >= s.getDepthHint())
	      {
		badState = s;
		badEvent = w.getBadEvent();
		bestdepth = s.getDepthHint();
		
	      }
	  }
      }
 
    VerificationJobResult result = new VerificationJobResult();

    //If badState is null, then the verification was
    //successful.
    if (badState == null)
      {
	result.setResult(true);
	result.setTrace(null);
      }
    else
      {
	int[] trace = findCounterExample(badState, badEvent, initial, workers);
	System.out.format("Counter-example:\n");
	for (int q = 0; q < trace.length; q++)
	  {
	    System.out.format(" > event %s\n", mModel.getEvent(trace[q]).getName());
	  }

	//Convert trace into event objects
	EventProxy[] ntrace = translateTraceToEvents(trace);

	result.setResult(false);
	result.setTrace(ntrace);
      }
    setResult(result);
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


  private ProductDESProxy mActualModel;
  private ProductDESSchema mModel;
  private StateEncoding mStateEncoding;

  private static final String WORKER_CLASS = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierWorkerImpl";
}