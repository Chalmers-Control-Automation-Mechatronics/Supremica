//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRule
//###########################################################################
//# $Id: ObservationEquivalenceRule.java 5596 2010-04-29 23:44:19Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.op.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Removes tau-loops. To achieve the best results this rule should be applied
 * before any other abstraction rules.
 *
 * @author Rachel Francis
 */

class TauLoopRemovalRule extends TRSimplifierAbstractionRule
{

  //#######################################################################
  //# Constructor
  TauLoopRemovalRule(final ProductDESProxyFactory factory,
                     final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  TauLoopRemovalRule(final ProductDESProxyFactory factory,
                     final KindTranslator translator,
                     final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions, new TauLoopRemovalTRSimplifier());
  }


  //#######################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws AnalysisException
  {
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
            EventEncoding.FILTER_PROPOSITIONS);
    mInputEncoding = new StateEncoding(autToAbstract);
    mTr =
        new ListBufferTransitionRelation(autToAbstract, eventEnc,
            mInputEncoding, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TransitionRelationSimplifier simplifier = getSimplifier();
    try {
      simplifier.setTransitionRelation(mTr);
      final boolean modified = simplifier.run();
      if (modified) {
        mPartition = simplifier.getResultPartition();
        final ProductDESProxyFactory factory = getFactory();
        mOutputEncoding = new StateEncoding();
        return mTr.createAutomaton(factory, eventEnc, mOutputEncoding);
      } else {
        return autToAbstract;
      }
    } catch (final OutOfMemoryError error) {
      simplifier.reset();
      throw new OverflowException(error);
    } finally {
      simplifier.reset();
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createObservationEquivalenceStep(abstractedAut,
                                                    mAutToAbstract, mTau,
                                                    mInputEncoding, mPartition,
                                                    mOutputEncoding);
  }

  public void cleanup()
  {
    mTr = null;
    mInputEncoding = null;
    mPartition = null;
    mOutputEncoding = null;
    mAutToAbstract = null;
  }


  //#######################################################################
  //# Data Members
  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private ListBufferTransitionRelation mTr;
  private StateEncoding mInputEncoding;
  private List<int[]> mPartition;
  private StateEncoding mOutputEncoding;

}
