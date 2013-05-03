//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRAbstractionStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * An abstraction step that uses a {@link ListBufferTransitionRelation}
 * for trace expansion
 *
 * @author Robi Malik
 */

class TRAbstractionStep extends AbstractionStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event represent silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding of input automaton, or
   *                           <CODE>null</CODE> to use a temporary
   *                           encoding.
   */
  protected TRAbstractionStep(final AbstractCompositionalModelAnalyzer analyzer,
                              final AutomatonProxy resultAut,
                              final AutomatonProxy originalAut,
                              final EventProxy tau,
                              final StateEncoding originalStateEnc)
  {
    super(analyzer, resultAut, originalAut);
    mTau = tau;
    mOriginalStateEncoding = originalStateEnc;
  }


  //#########################################################################
  //# Simple Access
  protected EventProxy getTau()
  {
    return mTau;
  }

  protected ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  protected EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  protected StateEncoding getOriginalStateEncoding()
  {
    return mOriginalStateEncoding;
  }


  //#########################################################################
  //# Data Members
  /**
   * The event that was hidden from the original automaton,
   * or <CODE>null</CODE>.
   */
  private final EventProxy mTau;
  /**
   * State encoding of original automaton. Maps state codes in the input
   * transition relation to state objects in the input automaton.
   */
  private final StateEncoding mOriginalStateEncoding;
  /**
   * Transition relation that was simplified.
   * Only used when expanding trace.
   */
  private ListBufferTransitionRelation mTransitionRelation;
  /**
   * Event encoding for {@link #mTransitionRelation}.
   * Only used when expanding trace.
   */
  private EventEncoding mEventEncoding;

}
