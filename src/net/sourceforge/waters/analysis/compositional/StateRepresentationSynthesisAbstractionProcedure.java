//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.analysis.abstraction.CertainUnsupervisabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SynthesisObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SynthesisTransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class StateRepresentationSynthesisAbstractionProcedure extends
  TRAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  /**
   * <P>
   * Creates a synthesis abstraction procedure.
   * </P>
   *
   * <P>
   * The abstraction chain is specified by flags, that indicate whether or not
   * a particular method is used. The order of abstraction is predefined:
   * first halfway synthesis, then bisimulation, then synthesis observation
   * equivalence, and finally weak synthesis observation equivalence, if these
   * methods are included.
   * </P>
   *
   * @param synthesizer
   *          The compositional synthesiser that will control the entire
   *          synthesis run.
   * @param abstractionMethods
   *          An integer combination of flags specifying which abstraction
   *          methods are in the chain. For example use {@link #USE_HALFWAY}
   *          &nbsp;|&nbsp;{@link #USE_BISIMULATION} to specify an abstraction
   *          sequence that performs only halfway synthesis and bisimulation.
   *
   * @see #USE_HALFWAY
   * @see #USE_UNSUP
   * @see #USE_BISIMULATION
   * @see #USE_SOE
   * @see #USE_WSOE
   * @see #USE_TRANSITIONREMOVAL
   * @see #CHAIN_SOE
   * @see #CHAIN_WSOE
   * @see #CHAIN_ALL
   */
  public static StateRepresentationSynthesisAbstractionProcedure createSynthesisAbstractionProcedure
    (final CompositionalStateRepresentationSynthesizer synthesizer,
     final int abstractionMethods)
  {
    final int limit = synthesizer.getInternalTransitionLimit();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    if ((abstractionMethods & USE_HALFWAY) != 0) {
      final HalfWaySynthesisTRSimplifier halfWay =
        new HalfWaySynthesisTRSimplifier();
      chain.add(halfWay);
    }
    if ((abstractionMethods & USE_UNSUP) != 0) {
      final CertainUnsupervisabilityTRSimplifier unSup =
        new CertainUnsupervisabilityTRSimplifier();
      unSup.setTransitionLimit(limit);
      chain.add(unSup);
    }
    if ((abstractionMethods & USE_BISIMULATION) != 0) {
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
      chain.add(bisimulator);
    }
    if ((abstractionMethods & USE_SOE) != 0) {
      final SynthesisObservationEquivalenceTRSimplifier synthesisAbstraction =
        new SynthesisObservationEquivalenceTRSimplifier();
      synthesisAbstraction.setTransitionLimit(limit);
      synthesisAbstraction.setUsesWeakSynthesisObservationEquivalence(false);
      chain.add(synthesisAbstraction);
    }
    if ((abstractionMethods & USE_WSOE) != 0) {
      final SynthesisObservationEquivalenceTRSimplifier synthesisAbstraction =
        new SynthesisObservationEquivalenceTRSimplifier();
      synthesisAbstraction.setTransitionLimit(limit);
      synthesisAbstraction.setUsesWeakSynthesisObservationEquivalence(true);
      chain.add(synthesisAbstraction);
    }
    if ((abstractionMethods & USE_TRANSITIONREMOVAL) != 0) {
      final SynthesisTransitionRemovalTRSimplifier transitionRemoval =
        new SynthesisTransitionRemovalTRSimplifier();
      transitionRemoval.setTransitionLimit(limit);
      chain.add(transitionRemoval);
    }
    return new StateRepresentationSynthesisAbstractionProcedure(synthesizer, chain);
  }


  //#########################################################################
  //# Constructor
  private StateRepresentationSynthesisAbstractionProcedure
    (final CompositionalStateRepresentationSynthesizer synthesizer,
     final ChainTRSimplifier chain)
  {
    super(synthesizer, chain, false);
  }


  //#########################################################################
  //# Overrides for AbstractAbstractionProcedure
  @Override
  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxyFactory factory = getFactory();
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    Collection<EventProxy> filter = getPropositions();
    if (filter == null) {
      filter = Collections.emptyList();
    }
    EventProxy tauC = null;
    EventProxy tauU = null;
    byte tauStatus = EventStatus.STATUS_LOCAL;
    if (analyzer.isSelfloopOnlyEventsEnabled()) {
      tauStatus |= EventStatus.STATUS_SELFLOOP_ONLY;
    }
    final EventEncoding enc = new EventEncoding();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (filter.contains(event)) {
          enc.addEvent(event, translator, 0);
        }
      } else if (local.contains(event)) {
        final EventKind kind = translator.getEventKind(event);
        if (kind == EventKind.CONTROLLABLE) {
          if (tauC == null) {
            final List<AutomatonProxy> automata = candidate.getAutomata();
            final String name = Candidate.getCompositionName("tau_C:", automata);
            tauC = factory.createEventProxy(name, kind, false);
          }
          enc.addEventAlias(event, tauC, translator, tauStatus);
        } else {
          if (tauU == null) {
            final List<AutomatonProxy> automata = candidate.getAutomata();
            final String name = Candidate.getCompositionName("tau_U:", automata);
            tauU = factory.createEventProxy(name, kind, false);
          }
          enc.addEventAlias(event, tauU, translator, tauStatus);
        }
      } else {
        byte status = 0;
        if (analyzer.isUsingSpecialEvents()) {
          final AbstractCompositionalModelAnalyzer.EventInfo info =
            analyzer.getEventInfo(event);
          if (info.isOnlyNonSelfLoopCandidate(candidate)) {
            status = EventStatus.STATUS_SELFLOOP_ONLY;
          }
        }
        enc.addEvent(event, translator, status);
      }
    }
    enc.sortProperEvents((byte) ~EventStatus.STATUS_LOCAL,
                         EventStatus.STATUS_CONTROLLABLE);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    int defaultMarkingID = -1;
    if (defaultMarking != null) {
      defaultMarkingID = enc.getEventCode(defaultMarking);
    }
    final TransitionRelationSimplifier simplifier = getSimplifier();
    simplifier.setDefaultMarkingID(defaultMarkingID);
    return enc;
  }

  @Override
  AbstractionStep createStep(final AutomatonProxy input,
                             final StateEncoding inputStateEnc,
                             final AutomatonProxy output,
                             final StateEncoding outputStateEnc, final EventProxy tau)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final TRPartition partition = simplifier.getResultPartition();
    return new MergeStep(analyzer, output, input, tau, inputStateEnc,
                         partition, false, outputStateEnc);
  }


  //#########################################################################
  //# Class Constants
  /**
   * Flag to include halfway synthesis in abstraction chain.
   */
  static final int USE_HALFWAY = 0x01;
  /**
   * Flag to include certain unsupervisability in abstraction chain.
   */
  static final int USE_UNSUP = 0x02;
  /**
   * Flag to include halfway bisimulation in abstraction chain.
   */
  static final int USE_BISIMULATION = 0x04;
  /**
   * Flag to include synthesis observation equivalence in abstraction chain.
   */
  static final int USE_SOE = 0x08;
  /**
   * Flag to include weak synthesis observation equivalence in abstraction
   * chain.
   */
  static final int USE_WSOE = 0x10;
  /**
   * Flag to include transition removal in abstraction chain.
   */
  static final int USE_TRANSITIONREMOVAL = 0x20;

  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalStateRepresentationSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, synthesis
   * observation equivalence and transition removal.
   */
  static final int CHAIN_SOE = USE_HALFWAY | USE_BISIMULATION | USE_SOE
                               | USE_TRANSITIONREMOVAL;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalStateRepresentationSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, weak synthesis
   * observation equivalence and transition removal. This is the default.
   */
  static final int CHAIN_WSOE = USE_HALFWAY | USE_BISIMULATION | USE_WSOE
                                | USE_TRANSITIONREMOVAL;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalStateRepresentationSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, synthesis
   * observation equivalence, weak synthesis observation equivalence and
   * transition removal.
   */
  static final int CHAIN_ALL = USE_HALFWAY | USE_BISIMULATION | USE_SOE
                               | USE_WSOE | USE_TRANSITIONREMOVAL;
}
