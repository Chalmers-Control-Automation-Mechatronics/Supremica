package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.Collection;

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
    
    //Create workers. If this fails the job will fail. Oh well.
    SafetyVerifierWorker[] workers = new SafetyVerifierWorker[nodes.size()];
    int i = 0;
    for (Node n : nodes)
      {
	workers[i] = 
	  (SafetyVerifierWorker) n.createWorker(getControllerID(), WORKER_CLASS);
	i++;
      }

    //Build a schematic of the model, on which the model checking will
    //be done
    ProductDESSchema schema = SchemaBuilder.build(job.getModel());
    
    //Set job parameters on the workers.
    for (SafetyVerifierWorker w : workers)
      {
	w.setJob(getJob());
	w.setModelSchema(schema);
      }

    //Start some processing threads
    for (SafetyVerifierWorker w : workers)
      {
	w.startProcessingThreads(2, 128);
      }
    
    VerificationJobResult result = new VerificationJobResult();
    result.setResult(false);
    result.setTrace(null);
    setResult(result);
  }

  private static final String WORKER_CLASS = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierWorkerImpl";
}