package net.sourceforge.waters.analysis.composing;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class ComposeSafetyVerifier 
  extends AbstractModelVerifier
  implements SafetyVerifier {

  //#########################################################################
  //# Constructors
  public ComposeSafetyVerifier(final ProductDESProxy model,
                               final KindTranslator translator,
                               final ProductDESProxyFactory factory) {
    super(model, factory); 
    mTranslator = translator;  
  }
  
  
  //#########################################################################
  //# Simple Access
  public SafetyTraceProxy getCounterExample() {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator() {
    return mTranslator;
  }

  public void setKindTranslator(KindTranslator trans) {
    mTranslator = trans;
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException { 
    //debug
    //System.out.println(getModel().getName());
        
    final Compose compose = new Compose(getModel(), mTranslator, getFactory());
    ProductDESProxy des = compose.run();
    //System.out.println(des.getName());
    final SafetyVerifier checker =
      new NativeSafetyVerifier(des, mTranslator, getFactory());      
    final boolean result = checker.run();    
    if (result) {
      return setSatisfiedResult();
    } else {
      final SafetyTraceProxy counterexample = checker.getCounterExample();
      return setFailedResult(counterexample);
    }
  }
  
  private KindTranslator mTranslator;
  //#########################################################################
  //# Class Constants
  private static final Logger LOGGER =
    LoggerFactory.createLogger(ComposeControllabilityChecker.class);
}
