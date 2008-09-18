//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   AbstractModularSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A common superclass for all modular verifiers. This class provides
 * common implmenetations for the {@link
 * net.sourceforge.waters.model.analysis.SafetyVerifier SafetyVerifier}
 * interface and enables uniform access to set the heuristic ({@link
 * ModularHeuristic}).
 *
 * @author Robi Malik
 */

abstract class AbstractModularSafetyVerifier
  extends AbstractModelVerifier
  implements SafetyVerifier
{
 
  //#########################################################################
  //# Constructor
  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mHeuristicMethod = ModularHeuristicFactory.Method.RelMaxCommonEvents;
    mHeuristicPreference = ModularHeuristicFactory.Preference.NOPREF;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }
  
  public void setKindTranslator(final KindTranslator translator)
  {
    mTranslator = translator;
  }

  public ModularHeuristicFactory.Method getHeuristicMethod()
  {
    return mHeuristicMethod;
  }

  public void setHeuristicMethod(final ModularHeuristicFactory.Method method)
  {
    mHeuristicMethod = method;
  }

  public ModularHeuristicFactory.Preference getHeuristicPreference()
  {
    return mHeuristicPreference;
  }

  public void setHeuristicPreference
    (final ModularHeuristicFactory.Preference preference)
  {
    mHeuristicPreference = preference;
  }

  public ModularHeuristic getHeuristic()
  {
    final ModularHeuristicFactory factory =
      ModularHeuristicFactory.getInstance();
    return factory.getHeuristic(mHeuristicMethod, mHeuristicPreference);
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }

  
  //#########################################################################
  //# Data Members
  private KindTranslator mTranslator;
  private ModularHeuristicFactory.Method mHeuristicMethod;
  private ModularHeuristicFactory.Preference mHeuristicPreference;

}
