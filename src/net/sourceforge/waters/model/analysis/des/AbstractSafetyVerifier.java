//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * An abstract base class that can be used for all safety verifier
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModelVerifier}, this class provides some support for
 * counterexample generation.
 *
 * @author Robi Malik
 */

public abstract class AbstractSafetyVerifier
  extends AbstractModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public AbstractSafetyVerifier(final KindTranslator translator,
                                final SafetyDiagnostics diag,
                                final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
  }

  /**
   * Creates a new safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public AbstractSafetyVerifier(final ProductDESProxy model,
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

  @Override
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
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
  protected String getTraceComment(final EventProxy event,
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
