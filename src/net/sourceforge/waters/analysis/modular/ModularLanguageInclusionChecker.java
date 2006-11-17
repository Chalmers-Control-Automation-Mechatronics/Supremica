//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id: ModularLanguageInclusionChecker.java,v 1.3 2006-11-17 03:38:22 robi Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class ModularLanguageInclusionChecker
  extends AbstractModelVerifier
  implements LanguageInclusionChecker
{
  //private final LanguageInclusionChecker mChecker;
  //private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  
  public ModularLanguageInclusionChecker(ProductDESProxy model,
                                         ProductDESProxyFactory factory,
                                         LanguageInclusionChecker checker,
                                         ModularHeuristic heuristic)
  {
    super(model, factory);
    //mChecker = checker;
    //mHeuristic = heuristic;
  }
  
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }
  
  public void setKindTranslator(KindTranslator trans)
  {
    mTranslator = trans;
  }
  
  public boolean run()
  {
    // Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>(getModel().getAutomata());
    return true;
  }
}
