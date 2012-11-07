//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AbstractSynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SynthesisObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 * @author robi
 */
public class SynthesisAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#######################################################################
  //# Constructor
  protected SynthesisAbstractionProcedure
    (final CompositionalSynthesizer synthesizer,
     final ChainTRSimplifier chain)
  {
    super(synthesizer);
    mChain = chain;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.analysis.compositional.
  //# AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    try {
      final EventEncoding eventEnc = createEventEncoding(aut, local);
      final StateEncoding inputStateEnc = createStateEncoding(aut);
      final int config = mChain.getPreferredInputConfiguration();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      mChain.setTransitionRelation(rel);
      if (mChain.run()) {
        final ListBufferTransitionRelation original =
          getTransitionRelationBeforeSOE(rel);
        final ListBufferTransitionRelation supervisor =
          getPseudoSupervisor();
        final CompositionalSynthesizer synthesizer = getAnalyzer();
        synthesizer.reportSupervisor("halfway synthesis", supervisor);
        final SynthesisAbstractionStep step;
        if (original == null) {
          final ProductDESProxyFactory factory = getFactory();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          synthesizer.reportAbstractionResult(convertedAut, null);
          step = new SynthesisAbstractionStep(synthesizer, convertedAut, aut,
                                              supervisor, eventEnc);
        } else {
          final List<int[]> partition = getResultPartition();
          step = synthesizer.createDeterministicAutomaton(aut, original, rel,
                                                          partition, eventEnc);
          step.setSupervisor(supervisor);
        }
        steps.add(step);
        return true;
      } else {
        return false;
      }
    } finally {
      mChain.reset();
    }
  }

  @Override
  public void storeStatistics()
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    result.setSimplifierStatistics(mChain);
  }

  @Override
  public void resetStatistics()
  {
    mChain.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mChain.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mChain.isAborting();
  }


  //#######################################################################
  //# Overrides for net.sourceforge.waters.analysis.compositional.
  //# AbstractAbstractionProcedure
  @Override
  CompositionalSynthesizer getAnalyzer()
  {
    return (CompositionalSynthesizer) super.getAnalyzer();
  }

  @Override
  CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createEventEncoding
    (final AutomatonProxy aut,
     final Collection<EventProxy> local)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final Collection<EventProxy> filter;
    if (props == null) {
      filter = Collections.emptyList();
    } else {
      filter = props;
    }
    final Collection<EventProxy> autAlphabet = aut.getEvents();
    final Collection<EventProxy> localUncontrollableEvents =
      new ArrayList<EventProxy>(local.size());
    final Collection<EventProxy> sharedUncontrollableEvents =
      new ArrayList<EventProxy>(autAlphabet.size() - local.size());
    final Collection<EventProxy> localControllableEvents =
      new ArrayList<EventProxy>(local.size());
    final Collection<EventProxy> sharedControllableEvents =
      new ArrayList<EventProxy>(autAlphabet.size() - local.size());
    final Collection<EventProxy> encodedEvents =
      new ArrayList<EventProxy>(autAlphabet.size());
    for (final EventProxy event : autAlphabet) {
      switch (translator.getEventKind(event)) {
      case CONTROLLABLE:
        if (local.contains(event)) {
          localControllableEvents.add(event);
        } else {
          sharedControllableEvents.add(event);
        }
        break;
      case UNCONTROLLABLE:
        if (local.contains(event)) {
          localUncontrollableEvents.add(event);
        } else {
          sharedUncontrollableEvents.add(event);
        }
        break;
      case PROPOSITION:
        // Put propositions in last list---its size does not matter.
        sharedControllableEvents.add(event);
        break;
      default:
        throw new IllegalArgumentException
          ("Unknown event kind " + translator.getEventKind(event) +
           " found for event " + event.getName() + "!");
      }
    }
    final int lastLocalUncontrollableEvent =
      localUncontrollableEvents.size();
    final int lastLocalControllableEvent =
      lastLocalUncontrollableEvent + localControllableEvents.size();
    final int lastSharedUncontrollableEvent =
      lastLocalControllableEvent + sharedUncontrollableEvents.size();
    encodedEvents.addAll(localUncontrollableEvents);
    encodedEvents.addAll(localControllableEvents);
    encodedEvents.addAll(sharedUncontrollableEvents);
    encodedEvents.addAll(sharedControllableEvents);
    final EventEncoding encoding =
      new EventEncoding(encodedEvents, translator, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    final CompositionalSynthesizer synthesizer = getAnalyzer();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = encoding.getEventCode(defaultMarking);
    mChain.setDefaultMarkingID(defaultMarkingID);
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof AbstractSynthesisTRSimplifier) {
        final AbstractSynthesisTRSimplifier simp =
          (AbstractSynthesisTRSimplifier) step;
        simp.setLastLocalControllableEvent(lastLocalControllableEvent);
        simp.setLastLocalUncontrollableEvent(lastLocalUncontrollableEvent);
        simp.setLastSharedUncontrollableEvent(lastSharedUncontrollableEvent);
        if (simp instanceof HalfWaySynthesisTRSimplifier) {
          final HalfWaySynthesisTRSimplifier halfWay =
            (HalfWaySynthesisTRSimplifier) simp;
          final TIntHashSet renamed =
            synthesizer.getRenamedControllables(encoding);
          halfWay.setRenamedEvents(renamed);
        }
      }
    }
    return encoding;
  }

  private StateEncoding createStateEncoding(final AutomatonProxy aut)
  {
    final StateEncoding encoding = new StateEncoding(aut);
    encoding.setNumberOfExtraStates(1);
    return encoding;
  }

  private ListBufferTransitionRelation getTransitionRelationBeforeSOE
    (final ListBufferTransitionRelation rel)
  {
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof SynthesisObservationEquivalenceTRSimplifier) {
        final SynthesisObservationEquivalenceTRSimplifier soe =
          (SynthesisObservationEquivalenceTRSimplifier) step;
        final ListBufferTransitionRelation result =
          soe.getOriginalTransitionRelation();
        if (result != null) {
          rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          if (rel.isDeterministic()) {
            return null;
          } else {
            return result;
          }
        }
      }
    }
    return null;
  }

  private List<int[]> getResultPartition()
  {
    return mChain.getResultPartition();
  }

  private ListBufferTransitionRelation getPseudoSupervisor()
  {
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof HalfWaySynthesisTRSimplifier) {
        final HalfWaySynthesisTRSimplifier halfWay =
          (HalfWaySynthesisTRSimplifier) step;
        return halfWay.getPseudoSupervisor();
      }
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mChain;
}
