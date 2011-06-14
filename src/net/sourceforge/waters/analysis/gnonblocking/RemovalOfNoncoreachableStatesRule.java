//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfNoncoreachableStatesRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TObjectIntHashMap;

import java.util.Collection;

import net.sourceforge.waters.analysis.op.CoreachabilityTRSimplifier;
import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * For a given Automaton applies an abstraction rule which removes states from
 * which neither an alpha or omega state can be reached.
 *
 * @author Rachel Francis
 */

class RemovalOfNoncoreachableStatesRule extends TRSimplifierAbstractionRule
{

  //#########################################################################
  //# Constructors
  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory,
                                    final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory,
                                    final KindTranslator translator,
                                    final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions,
          new CoreachabilityTRSimplifier());
  }


  //#########################################################################
  //# Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  EventProxy getDefaultMarking()
  {
    return mDefaultMarking;
  }

  void setDefaultMarking(final EventProxy defaultMarking)
  {
    mDefaultMarking = defaultMarking;
  }


  //#########################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws AnalysisException
  {
    if (!autToAbstract.getEvents().contains(mAlphaMarking) ||
        !autToAbstract.getEvents().contains(mDefaultMarking)) {
      return autToAbstract;
    }
    mAutToAbstract = autToAbstract;
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
                          EventEncoding.FILTER_PROPOSITIONS);
    final int alphaID = eventEnc.getEventCode(mAlphaMarking);
    final int defaultID = eventEnc.getEventCode(mDefaultMarking);
    mInputEncoding = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation
                 (autToAbstract, eventEnc, mInputEncoding,
                  ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TransitionRelationSimplifier simplifier = getSimplifier();
    try {
      simplifier.setTransitionRelation(rel);
      simplifier.setPropositions(alphaID, defaultID);
      simplifier.setAppliesPartitionAutomatically(false);
      final boolean modified = simplifier.run();
      if (modified) {
        rel.removeTauSelfLoops();
        rel.removeProperSelfLoopEvents();
        rel.removeRedundantPropositions();
        final ProductDESProxyFactory factory = getFactory();
        mOutputEncoding = new StateEncoding();
        return rel.createAutomaton(factory, eventEnc, mOutputEncoding);
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

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    final StateProxy[] inputMap = mInputEncoding.getStatesArray();
    final TObjectIntHashMap<StateProxy> outputMap =
      mOutputEncoding.getStateCodeMap();
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               inputMap, outputMap);
  }

  public void cleanup()
  {
    mAutToAbstract = null;
    mInputEncoding = null;
    mOutputEncoding = null;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;

  private StateEncoding mInputEncoding;
  private StateEncoding mOutputEncoding;

}
