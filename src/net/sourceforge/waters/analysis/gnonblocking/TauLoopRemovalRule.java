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
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Removes tau-loops. To acheive the best results this rule should be applied
 * before any other abstraction rules.
 *
 * @author Rachel Francis
 */

class TauLoopRemovalRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  TauLoopRemovalRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);

  }

  TauLoopRemovalRule(final ProductDESProxyFactory factory,
                     final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws AnalysisException
  {
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, tau, getPropositions(),
            EventEncoding.FILTER_PROPOSITIONS);
    mInputEncoding = new StateEncoding(autToAbstract);
    mTr =
        new ListBufferTransitionRelation(autToAbstract, eventEnc,
            mInputEncoding, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TauLoopRemovalTRSimplifier tauLoopRemover =
        new TauLoopRemovalTRSimplifier(mTr);

    final boolean modified = tauLoopRemover.run();
    if (modified) {
      mPartition = tauLoopRemover.getResultPartition();
      mTr.merge(mPartition);
      mTr.removeTauSelfLoops();
      mTr.removeProperSelfLoopEvents();
      final ProductDESProxyFactory factory = getFactory();
      mOutputEncoding = new StateEncoding();
      return mTr.createAutomaton(factory, eventEnc, mOutputEncoding);
    } else {
      return autToAbstract;
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
  }

  // #######################################################################
  // # Data Members
  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private ListBufferTransitionRelation mTr;
  private StateEncoding mInputEncoding;
  private List<int[]> mPartition;
  private StateEncoding mOutputEncoding;

}
