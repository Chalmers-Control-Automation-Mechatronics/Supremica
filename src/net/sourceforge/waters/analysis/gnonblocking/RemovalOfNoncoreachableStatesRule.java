//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;

import net.sourceforge.waters.analysis.abstraction.GNBCoreachabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
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
          new GNBCoreachabilityTRSimplifier());
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
