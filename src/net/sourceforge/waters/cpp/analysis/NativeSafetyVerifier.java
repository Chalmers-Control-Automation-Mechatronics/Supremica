//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

public class NativeSafetyVerifier
  extends NativeModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public NativeSafetyVerifier(final KindTranslator translator,
                              final SafetyDiagnostics diag,
                              final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
  }

  public NativeSafetyVerifier(final ProductDESProxy model,
                              final KindTranslator translator,
                              final SafetyDiagnostics diag,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory, translator);
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns how to handle automata without initial states.
   * @return <CODE>true</CODE> if verification is to fail when encountering
   *         a specification without an initial state.
   */
  public boolean isInitialUncontrollable()
  {
    final KindTranslator translator = getKindTranslator();
    return translator.getEventKind(KindTranslator.INIT) ==
      EventKind.UNCONTROLLABLE;
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  @Override
  public String getTraceName()
  {
    final ProductDESProxy des = getModel();
    if (mDiagnostics == null) {
      final String desname = des.getName();
      return desname + "-unsafe";
    } else {
      return mDiagnostics.getTraceName(des);
    }
  }

  /**
   * Generates a comment to be used for a counterexample generated for
   * the current model.
   * @param  event  The event that causes the safety property under
   *                investigation to fail.
   * @param  aut    The automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @param  state  The state in the automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @return An English string that describes why the safety property is
   *         violated, which can be used as a trace comment.
   */
  public String getTraceComment(final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    if (mDiagnostics == null) {
      return null;
    } else {
      final ProductDESProxy des = getModel();
      return mDiagnostics.getTraceComment(des, event, aut, state);
    }
  }


  //#########################################################################
  //# Data Members
  private final SafetyDiagnostics mDiagnostics;

}
