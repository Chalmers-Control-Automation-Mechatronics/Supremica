//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class DeterminisationOfNonAlphaStatesRule
  extends TRSimplifierAbstractionRule
{

  //#########################################################################
  //# Constructor
  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory,
                                      final KindTranslator translator)
  {
    this(factory, translator, null);

  }

  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory,
                                      final KindTranslator translator,
                                      final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions,
          new NonAlphaDeterminisationTRSimplifier());
  }


  //#########################################################################
  //# Configuration
  @Override
  NonAlphaDeterminisationTRSimplifier getSimplifier()
  {
    return (NonAlphaDeterminisationTRSimplifier) super.getSimplifier();
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  void setTransitionRemovalMode
    (final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    final NonAlphaDeterminisationTRSimplifier simplifier = getSimplifier();
    simplifier.setTransitionRemovalMode(mode);
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  ObservationEquivalenceTRSimplifier.TransitionRemoval
    getTransitionRemovalMode()
  {
    final NonAlphaDeterminisationTRSimplifier simplifier = getSimplifier();
    return simplifier.getTransitionRemovalMode();
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be constructed by the observation equivalence
   * algorithm. An attempt to store more transitions leads to an
   * {@link net.sourceforge.waters.model.analysis.OverflowException
   * OverflowException}.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  void setTransitionLimit(final int limit)
  {
    final NonAlphaDeterminisationTRSimplifier simplifier = getSimplifier();
    simplifier.setTransitionLimit(limit);
  }

  /**
   * Gets the transition limit.
   * @see {@link #setTransitionLimit(int) setTransitionLimit()}
   */
  int getTransitionLimit()
  {
    final NonAlphaDeterminisationTRSimplifier simplifier = getSimplifier();
    return simplifier.getTransitionLimit();
  }

  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }


  //#########################################################################
  //# Rule Application
  @Override
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
    throws AnalysisException
  {
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      return autToAbstract;
    }
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
                          EventEncoding.FILTER_PROPOSITIONS);
    final int alphaCode = eventEnc.getEventCode(mAlphaMarking);
    mInputEncoding = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
            (autToAbstract, eventEnc, mInputEncoding,
             ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final NonAlphaDeterminisationTRSimplifier simplifier = getSimplifier();
    try {
      simplifier.setTransitionRelation(rel);
      simplifier.setPropositions(alphaCode, -1);
      if (simplifier.run()) {
        mPartition = simplifier.getResultPartition();
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

  @Override
  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createDeterminisationOfNonAlphaStatesStep(abstractedAut,
                                                             mAutToAbstract,
                                                             mTau,
                                                             mInputEncoding,
                                                             mPartition,
                                                             mOutputEncoding);
  }

  @Override
  public void cleanup()
  {
    mInputEncoding = null;
    mPartition = null;
    mOutputEncoding = null;
    mAutToAbstract = null;
  }


  //#########################################################################
  //# Data Members
  private AutomatonProxy mAutToAbstract;
  private EventProxy mAlphaMarking;
  private EventProxy mTau;
  private StateEncoding mInputEncoding;
  private TRPartition mPartition;
  private StateEncoding mOutputEncoding;

}








