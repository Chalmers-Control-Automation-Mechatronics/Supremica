package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SerializableKindTranslator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;

import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.application.Server;

/**
 * XXX
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
    final ProductDESProxyFactory factory = getFactory();
    final KindTranslator translator = mKindTranslator;

    try
      {
	Server server = connectToServer(getHostname(), getPort());

	VerificationJob job = new VerificationJob();
	job.setName("safety-" + UUID.randomUUID().toString());
	job.setController(controller);
	job.setNodeCount(getNodeCount());
	job.setModel(model);

	//Set the kind translator for the job by serialising
	//the current translator, regardless of the type.
	SerializableKindTranslator kx = new SerializableKindTranslator(translator, model);
	job.setKindTranslator(kx);
	
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
	    EventProxy[] trace  = null;
	    SafetyTraceProxy counterexample = null;
	    if (result.getTrace() != null)
	      {
		trace = result.getTrace();
		
		//'Sanitise' the trace. This ensures the EventProxy
		//objects that are in the trace are the same objects
		//as the ones in the original model, as is expected
		//for Waters verifiers.
		List<EventProxy> tracelist = sanitiseTrace(trace);
		
		counterexample = factory.createSafetyTraceProxy(model, tracelist);
	      }
	    return setFailedResult(counterexample);
	  }
	
	
      }
    catch (Exception e)
      {
	throw new AnalysisException(e);
      }
  }


  private List<EventProxy> sanitiseTrace(EventProxy[] trace) throws AnalysisException
  {
    List<EventProxy> nt = new ArrayList<EventProxy>();
    
    for (EventProxy ev : trace)
      {
	nt.add(sanitiseEvent(ev));
      }

    return nt;
  }


  /**
   * Returns the event proxy instance from the actual model that is
   * equal by contents to the supplied event proxy.
   * @param event event to sanitise
   * @return event considered equal from the model
   * @throws AnalysisException if no events were equal
   */
  private EventProxy sanitiseEvent(EventProxy event) throws AnalysisException
  {
    for (EventProxy ev : getModel().getEvents())
      {
	if (ev.equalsByContents(event))
	  return ev;
      }

    throw new AnalysisException("EventProxy could not be sanitised: not in model?"); 
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