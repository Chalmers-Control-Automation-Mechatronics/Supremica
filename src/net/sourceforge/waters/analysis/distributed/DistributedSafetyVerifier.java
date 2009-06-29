package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.UUID;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;

import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.application.Server;

/**
 * Some day this will be a distributed, multi-threaded implementation
 * of a controllability checker. For now it is just a test bench.
 * 
 * @author Sam Douglas
 */
public class DistributedSafetyVerifier
  extends AbstractModelVerifier
  implements SafetyVerifier
{
  public DistributedSafetyVerifier(final KindTranslator translator,
				   final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }


  public DistributedSafetyVerifier(final ProductDESProxy model,
				   final KindTranslator translator,
				   final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mKindTranslator = translator;
  }

  public boolean run() throws AnalysisException
  {
    //These arguments should be mandatory.
    assert(getHostname() != null);
    assert(getPort() > 0);
    final String controller = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierController";
    final ProductDESProxy model = getModel();

    try
      {
	Server server = connectToServer(getHostname(), getPort());
	
	System.out.println("Connected to server: " + server);

	VerificationJob job = new VerificationJob();
	job.setName("safety-" + UUID.randomUUID().toString());
	job.setController(controller);
	job.setNodeCount(getNodeCount());

	//The cast to Serializable is necessary because the interfaces
	//don't implement Serializable, but the object itself probably
	//does. The setAttribute method uses a generic type wildcard to
	//only allow Serializable objects to be added, but this fails at
	//compile time.
	job.setModel(model);
	
	VerificationJobResult result = new VerificationJobResult(server.submitJob(job));

	//Check for exceptions that might have occurred
	if (result.getException() != null)
	  throw new AnalysisException(result.getException());


	Boolean b = result.getResult();
	if (b == null)
	  {
	    throw new AnalysisException("Verification result was undefined!");
	  }

	if (b == true)
	  {
	    return setSatisfiedResult();
	  }
	else
	  {
	    //Get a counter-example from the job result
	    TraceProxy counterexample = null;
	    if (result.getTrace() != null)
	      {
		counterexample = result.getTrace();
	      }
	    return setFailedResult(counterexample);
	  }
	
	
      }
    catch (Exception e)
      {
	throw new AnalysisException(e);
      }
  }

  private Server connectToServer(String host, int port) throws Exception
  {
    //This should probably be replaced with a common constants
    //class or something. For now this will do
    String service = net.sourceforge.waters.analysis.distributed.application.DistributedServer.DEFAULT_SERVICE_NAME;

    Registry registry = LocateRegistry.getRegistry(host, port);
    Server server = (Server) registry.lookup(service);
    
    return server;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public void setKindTranslator(KindTranslator translator)
  {
    mKindTranslator = translator;
    clearAnalysisResult();
  }

  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }

  public void setHostname(String hostname)
  {
    mHostname = hostname;
  }

  public void setPort(int port)
  {
    mPort = port;
  }

  public String getHostname()
  {
    return mHostname;
  }

  public int getPort()
  {
    return mPort;
  }

  public void setNodeCount(int count)
  {
    mNodeCount = count;
  }

  public int getNodeCount()
  {
    return mNodeCount;
  }
  
  private String mHostname = null;
  private int mPort = 23232;
  private int mNodeCount = 10;
  private KindTranslator mKindTranslator;
}