//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   AbstractModularSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A common superclass for all modular verifiers. This class provides
 * common implementations for the {@link
 * net.sourceforge.waters.model.analysis.des.SafetyVerifier SafetyVerifier}
 * interface and enables uniform access to set the heuristic ({@link
 * ModularHeuristic}).</P>
 *
 * @author Robi Malik
 */

abstract class AbstractModularSafetyVerifier
  extends AbstractSafetyVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier mono)
  {
    this(model, null, null, factory, mono);
  }

  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final KindTranslator translator,
                                       final SafetyDiagnostics diag,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier mono)
  {
    super(model, translator, diag, factory);
    mMonolithicVerifier = mono;
    mHeuristicMethod = ModularHeuristicFactory.Method.RelMaxCommonEvents;
    mHeuristicPreference = ModularHeuristicFactory.Preference.NOPREF;
  }


  //#########################################################################
  //# Configuration
  public SafetyVerifier getMonolithicVerifier()
  {
    return mMonolithicVerifier;
  }

  public void setMonolithicVerifier(final SafetyVerifier verifier)
  {
    mMonolithicVerifier = verifier;
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
    final KindTranslator translator = getKindTranslator();
    return
      factory.getHeuristic(mHeuristicMethod, mHeuristicPreference, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mMonolithicVerifier.supportsNondeterminism();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mMonolithicVerifier != null) {
      mMonolithicVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mMonolithicVerifier != null) {
      mMonolithicVerifier.resetAbort();
    }
  }


  //#########################################################################
  //# Data Members
  private SafetyVerifier mMonolithicVerifier;
  private ModularHeuristicFactory.Method mHeuristicMethod;
  private ModularHeuristicFactory.Preference mHeuristicPreference;

}
