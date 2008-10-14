package net.sourceforge.waters.analysis.composing;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


public class ComposingLanguageInclusionChecker
  extends ComposingSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public ComposingLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ComposingLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {    
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);   
    plants      = new HashSet<AutomatonProxy>();
    specs       = new HashSet<AutomatonProxy>();    
    plantEvents = new HashSet<EventProxy>();


    cModel = new ConvertModelLang();
  }
  
  public boolean run() throws AnalysisException {
    int goodProperty = 0;
    int badProperty  = 0;
    mModel = getModel();
    for (AutomatonProxy automaton : mModel.getAutomata()) { 
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      plantEvents.addAll(automaton.getEvents());
                      break;
        case SPEC  :  specs.add(automaton);                                            
                      break;
        default : break;
      }
    }
    for (AutomatonProxy spec : specs) {
      newEvents   = new HashSet<EventProxy>();
      final Set<AutomatonProxy> comp = new HashSet<AutomatonProxy>(); 
      comp.addAll(plants);
      comp.add(spec); 
      newEvents.addAll(plantEvents);
      newEvents.addAll(spec.getEvents());
      ProductDESProxy newModel = 
                  getFactory().createProductDESProxy(spec.getName()+"_ComposedModel", 
                                                     newEvents, comp);	
      super.setModel(newModel);	
      System.out.println("\nChecking "+spec.getName()+" ...");    
	    if (super.run()) {
	      System.out.println(spec.getName()+" is true!");
	      goodProperty++;
	    } else {
	        System.out.println(spec.getName()+" is false!");	        
	        badProperty++;
	        final String tracename = mModel.getName() + ":uncontrollable";
	        final SafetyTraceProxy counterexample = (SafetyTraceProxy)super.getCounterExample();
          List<EventProxy> composedTrace = new LinkedList<EventProxy>(counterexample.getEvents());
	        final SafetyTraceProxy fixedCounterexample =
               getFactory().createSafetyTraceProxy(tracename, mModel, composedTrace); 
	        return setFailedResult(fixedCounterexample); 
	      } 
	  }	  
	  return setSatisfiedResult();  
  }
  
  public ProductDESProxy getConvertedModel() {
    cModel = new ConvertModelLang(getModel(), 
				                          LanguageInclusionKindTranslator.getInstance(), 
				                          getFactory());  
	  return cModel.run(); 
  }
  
  public List<EventProxy> convertTrace(List<EventProxy> trace) {
    EventProxy ne = cModel.getOriginalEvent(trace.get(trace.size()-1)); 
    trace.remove(trace.size()-1);
    trace.add(ne);
    return trace;
  }

  private ConvertModelLang            cModel;  
  private ProductDESProxy             mModel;
  private Set<AutomatonProxy>         plants;    
  private Set<AutomatonProxy>         specs;
  private Set<EventProxy>             newEvents;
  private Set<EventProxy>             plantEvents;
}
