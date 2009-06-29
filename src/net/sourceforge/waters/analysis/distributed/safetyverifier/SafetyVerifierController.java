package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.Collection;
import java.util.HashSet;
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
    mModel = SchemaBuilder.build(job.getModel());
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
	w.startProcessingThreads(16, 128);
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

	boolean still_running = false;

	for (SafetyVerifierWorker w : workers)
	  {
	    if (w.getBadState() != null)
	      {
		//Store the first bad state that is found
		if (badState == null)
		  badState = w.getBadState();

		bad = true;
	      }

	    if (!w.appearsFinished())
	      still_running = true;

	    total_states += w.getStateCount();
	    total_incoming1 += w.getIncomingStateCount();
	    total_outgoing1 += w.getOutgoingStateCount();
	  }
	
	boolean terminate = false;
	if (!still_running)
	  {
	    for (SafetyVerifierWorker w : workers)
	      {
		if (!w.appearsFinished())
		  still_running = true;

		total_incoming2 += w.getIncomingStateCount();
		total_outgoing2 += w.getOutgoingStateCount();
	      }

	    //Condition for termination: all counters are equal and
	    //system is not still running (i.e. all nodes are idle)
	    terminate = total_incoming1 == total_incoming2 
	      && total_outgoing1 == total_outgoing2 
	      && total_incoming1 == total_outgoing1
	      && !still_running;
	  }

	//Also terminate if there is a bad state.
	if (badState != null)
	  terminate = true;

	System.out.format("%b %b %d %d %d\n", !bad, terminate, 
			  total_states, 
			  total_incoming1, 
			  total_outgoing1);

	if (terminate)
	  break;

	Thread.sleep(500);
      }
 
    VerificationJobResult result = new VerificationJobResult();

    //If badState is null, then the verification was
    //successful.
    result.setResult(badState == null);
    result.setTrace(null);
    setResult(result);
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

    return mStateEncoding.encodeState(start);
  }

  private ProductDESSchema mModel;
  private StateEncoding mStateEncoding;
  private static final String WORKER_CLASS = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierWorkerImpl";
}