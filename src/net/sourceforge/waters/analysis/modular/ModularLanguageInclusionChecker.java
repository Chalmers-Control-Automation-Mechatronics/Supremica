//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id: ModularLanguageInclusionChecker.java,v 1.2 2006-11-15 05:20:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.plain.des.SafetyTraceElement;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Set;
import java.util.HashSet;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;

public class ModularLanguageInclusionChecker
  extends AbstractModelVerifier
  implements LanguageInclusionChecker
{
  private final LanguageInclusionChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  
  public ModularLanguageInclusionChecker(ProductDESProxy model,
                                         ProductDESProxyFactory factory,
                                         LanguageInclusionChecker checker,
                                         ModularHeuristic heuristic)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
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
    Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>(getModel().getAutomata());
    return true;
  }
}
