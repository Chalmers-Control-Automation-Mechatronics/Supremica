//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstraction procedure to compute the natural projection.
 * The abstraction sequence consists of tau-loop removal, followed
 * by subset construction, and the deterministic automata minimisation
 * algorithm.
 *
 * @author Robi Malik
 */

class ProjectionAbstractionProcedure extends TRAbstractionProcedure
{

  //#########################################################################
  //# Constructor
  public static ProjectionAbstractionProcedure
    createProjectionAbstractionProcedure
      (final CompositionalSafetyVerifier verifier)
  {
    final int slimit = verifier.getInternalStateLimit();
    final int tlimit = verifier.getInternalTransitionLimit();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(slimit);
    subset.setTransitionLimit(tlimit);
    subset.setFailingEventsAsSelfloops(true);
    final ObservationEquivalenceTRSimplifier bisimulator1 =
      new ObservationEquivalenceTRSimplifier();
    bisimulator1.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
    if (verifier.isSelfloopOnlyEventsEnabled()) {
      // Selfloop-only events must be handled through bisimulation.
      final ObservationEquivalenceTRSimplifier bisimulator2 =
        new ObservationEquivalenceTRSimplifier();
      bisimulator2.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
      bisimulator2.setInfoEnabled(true);
    }
    chain.add(bisimulator1);
    return new ProjectionAbstractionProcedure(verifier, chain);
  }


  //#########################################################################
  //# Constructor
  private ProjectionAbstractionProcedure
    (final CompositionalSafetyVerifier verifier,
     final TransitionRelationSimplifier simplifier)
  {
    super(verifier, simplifier, false);
  }


  //#########################################################################
  //# Simple Access
  @Override
  CompositionalSafetyVerifier getAnalyzer()
  {
    return (CompositionalSafetyVerifier) super.getAnalyzer();
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps,
                     final Candidate candidate)
    throws AnalysisException
  {
    final CompositionalSafetyVerifier verifier = getAnalyzer();
    final TransitionRelationSimplifier simplifier = getSimplifier();
    try {
      EventProxy tau = null;
      for (final EventProxy event : local) {
        if (verifier.getPropertyStatus(event) ==
            CompositionalSafetyVerifier.NONPROPERTY) {
          tau = event;
          break;
        }
      }
      final Collection<EventProxy> taus = Collections.singletonList(tau);
      final EventEncoding eventEnc = createEventEncoding(aut, taus, candidate);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      for (final EventProxy event : local) {
        if (verifier.getPropertyStatus(event) ==
            CompositionalSafetyVerifier.FORBIDDEN) {
          final int e = eventEnc.getEventCode(event);
          if (e >= 0) {
            byte status = eventEnc.getProperEventStatus(e);
            status |= EventStatus.STATUS_FAILING;
            status |= EventStatus.STATUS_ALWAYS_ENABLED;
            eventEnc.setProperEventStatus(e, status);
          }
        }
      }
      eventEnc.sortProperEvents(~EventStatus.STATUS_FAILING,
                                ~EventStatus.STATUS_ALWAYS_ENABLED);
      final int config = simplifier.getPreferredInputConfiguration();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      verifier.showDebugLog(rel);
      simplifier.setTransitionRelation(rel);
      simplifier.run();
      final ProductDESProxyFactory factory = getFactory();
      final StateEncoding outputStateEnc = new StateEncoding();
      final AutomatonProxy convertedAut =
        rel.createAutomaton(factory, eventEnc, outputStateEnc);
      final AbstractionStep step = createStep
        (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
      steps.add(step);
      return true;
    } finally {
      simplifier.reset();
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.TRAbstractionProcedure
  @Override
  ProjectionStep createStep(final AutomatonProxy input,
                            final StateEncoding inputStateEnc,
                            final AutomatonProxy output,
                            final StateEncoding outputStateEnc,
                            final EventProxy tau)
  {
    final CompositionalSafetyVerifier verifier = getAnalyzer();
    return new ProjectionStep(verifier, output, input, tau);
  }

}
