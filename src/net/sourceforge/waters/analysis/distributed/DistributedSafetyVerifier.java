package net.sourceforge.waters.analysis.distributed;


import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;

import net.sourceforge.waters.analysis.distributed.schemata.SchemaBuilder;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

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

  public boolean run() throws OverflowException
  {
    final ProductDESProxy model = getModel();
    final ProductDESSchema modelSchema = SchemaBuilder.build(model);
    
    StateExplorerNode explorer = new StateExplorerNode(modelSchema);

    //Start some processing threads
    for (int i = 0; i < 1; i++)
      explorer.runWorkerThread();
    

    //Decide if the explorer is finished. In a distributed
    //checker, you could be reasonably sure the model checker
    //had finished by making sure each node was empty a certain 
    //number of times.
    int emptycount = 0;
    while (true) {
	if (explorer.noUnexploredStates())
	  emptycount++;
	else
	  emptycount = 0;
	
	System.out.format("%s; Number of states: %d %d %d\n", 
			  explorer.isUncontrollable() ? "Uncontrollable" : "Controllable",
			  explorer.getExploredStateCount(),
			  explorer.getWaitingStateCount(),
			  explorer.getWaitingSetSize());
	try
	  {
	    Thread.sleep(100);
	  }
	catch (Exception e){}

	if (emptycount > 10)
	  break;
      }

    //Feign success! That way I can ignore countertraces for a while
    return setSatisfiedResult();
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

  private KindTranslator mKindTranslator;
}