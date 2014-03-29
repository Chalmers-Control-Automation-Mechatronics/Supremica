//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.CertainUnsupervisabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SynthesisObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

public class AutomataSynthesisAbstractionProcedure extends
  AbstractAbstractionProcedure
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
   * @see #CHAIN_SOE
   * @see #CHAIN_WSOE
   * @see #CHAIN_ALL
   */
  public static AutomataSynthesisAbstractionProcedure createSynthesisAbstractionProcedure
    (final CompositionalAutomataSynthesizer synthesizer,
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
    return new AutomataSynthesisAbstractionProcedure(synthesizer, chain);
  }


  //#########################################################################
  //# Constructor
  private AutomataSynthesisAbstractionProcedure(final CompositionalAutomataSynthesizer synthesizer,
                                        final ChainTRSimplifier chain)
  {
    super(synthesizer);
    mChain = chain;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.compositional.
  //# AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps,
                     final Candidate cand)
    throws AnalysisException
  {
    try {
      final EventEncoding mergedEnc = createEventEncoding(aut, local, true);
      final StateEncoding inputStateEnc = createStateEncoding(aut);
      final int inputConfig = mChain.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, mergedEnc,
                                         inputStateEnc, inputConfig);
      final CompositionalAutomataSynthesizer synthesizer = getAnalyzer();
      synthesizer.showDebugLog(rel);
      mChain.setTransitionRelation(rel);
      if (mChain.run()) {
        rel = mChain.getTransitionRelation();
        final TRPartition partition = mChain.getResultPartition();
        final Set<TransitionProxy> removedTransitions;
        final EventEncoding originalEnc;
        // 1. Check if the abstraction is deterministic
        ListBufferTransitionRelation detRel = rel;
        if (!mUsingRenaming) {
          // If there is no prior renaming, check the output directly
          if (isMergingPartition(partition)) {
            rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
            if (!rel.isDeterministic()) {
              detRel = null;
            }
          }
          removedTransitions = collectRemovedTransitions
            (aut, mergedEnc, inputStateEnc, rel, partition);
          originalEnc = mergedEnc;
        } else {
          // Otherwise try to build the unrenamed abstraction
          removedTransitions = collectRemovedTransitions
            (aut, mergedEnc, inputStateEnc, rel, partition);
          originalEnc = createEventEncoding(aut, local, false);
          detRel = createMergedAbstraction
            (aut, removedTransitions, originalEnc, inputStateEnc, partition);
        }
        final SynthesisAbstractionStep step;
        if (detRel != null) {
          final ProductDESProxyFactory factory = getFactory();
          final AutomatonProxy convertedAut =
            detRel.createAutomaton(factory, originalEnc, null);
          synthesizer.reportAbstractionResult(convertedAut, null);
          step = new SynthesisAbstractionStep
            (synthesizer, convertedAut, aut, originalEnc);
        } else {
          final HalfWaySynthesisTRSimplifier.OutputMode absMode =
            HalfWaySynthesisTRSimplifier.OutputMode.ABSTRACTION;
          final int outputConfig =
            ListBufferTransitionRelation.CONFIG_SUCCESSORS;
          final ListBufferTransitionRelation before =
            createPseudoSupervisorTR(aut, removedTransitions,
                                     originalEnc, inputStateEnc, inputStateEnc,
                                     partition, absMode, outputConfig);
          final Map<EventProxy,List<EventProxy>> renaming =
            findEventRenaming(before, originalEnc, partition);
          final EventEncoding renamedEnc =
            createRenamedEventEncoding(originalEnc, renaming);
          final AutomatonProxy abstraction =
            createRenamedAbstraction(before, originalEnc, renamedEnc, renaming,
                                     partition);
          step = new SynthesisAbstractionStep
            (synthesizer, abstraction, aut, renaming, originalEnc);
          final AutomatonProxy distinguisher =
            createDistinguisher(before, originalEnc, renaming,
                                inputStateEnc, partition);
          synthesizer.recordDistinuisherInfo(renaming, distinguisher);
          synthesizer.reportAbstractionResult(abstraction, distinguisher);
        }
        final HalfWaySynthesisTRSimplifier.OutputMode supMode =
          HalfWaySynthesisTRSimplifier.OutputMode.PSEUDO_SUPERVISOR;
        final int outputConfig =
          ListBufferTransitionRelation.CONFIG_SUCCESSORS;
        final ListBufferTransitionRelation supervisor =
          createPseudoSupervisorTR(aut, removedTransitions,
                                   originalEnc, inputStateEnc, null,
                                   partition, supMode, outputConfig);
        if (supervisor != null) {
          synthesizer.reportSupervisor("halfway synthesis", supervisor);
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
    final CompositionalAutomataSynthesisResult result = getAnalysisResult();
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

  @Override
  public void resetAbort()
  {
    mChain.resetAbort();
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.compositional.
  //# AbstractAbstractionProcedure
  @Override
  CompositionalAutomataSynthesizer getAnalyzer()
  {
    return (CompositionalAutomataSynthesizer) super.getAnalyzer();
  }

  @Override
  CompositionalAutomataSynthesisResult getAnalysisResult()
  {
    return (CompositionalAutomataSynthesisResult) super.getAnalysisResult();
  }

  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createEventEncoding(final AutomatonProxy aut,
                                            final Collection<EventProxy> local,
                                            final boolean useRenaming)
  {
    final CompositionalAutomataSynthesizer synthesizer = getAnalyzer();
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final EventEncoding encoding = new EventEncoding();
    EventProxy tauC = null;
    EventProxy tauU = null;
    mUsingRenaming = false;
    for (final EventProxy event : aut.getEvents()) {
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.PROPOSITION) { // proposition event
        if (props != null && props.contains(event)) {
          final int p = encoding.addEvent(event, translator, 0);
          if (event == getUsedDefaultMarking()) {
            mChain.setDefaultMarkingID(p);
          }
        }
      } else if (!local.contains(event)) { // shared event
        if (useRenaming) {
          final EventProxy original = synthesizer.getOriginalEvent(event);
          encoding.addEventAlias(event, original, translator, 0);
          mUsingRenaming |= original != event;
        } else {
          encoding.addEvent(event, translator, 0);
        }
      } else if (kind == EventKind.CONTROLLABLE) { // local controllable
        if (tauC == null || !useRenaming) {
          encoding.addEvent(event, translator,
                            EventEncoding.STATUS_LOCAL);
          tauC = event;
        } else {
          encoding.addEventAlias(event, tauC, translator,
                                 EventEncoding.STATUS_LOCAL);
          mUsingRenaming = true;
        }
      } else { // local uncontrollable
        if (tauU == null || !useRenaming) {
          encoding.addEvent(event, translator,
                            EventEncoding.STATUS_LOCAL);
          tauU = event;
        } else {
          encoding.addEventAlias(event, tauU, translator,
                                 EventEncoding.STATUS_LOCAL);
          mUsingRenaming = true;
        }
      }
    }
    if (useRenaming) {
      encoding.sortProperEvents((byte) ~EventEncoding.STATUS_LOCAL,
                                EventEncoding.STATUS_CONTROLLABLE);
    } else {
      encoding.sortProperEvents(EventEncoding.STATUS_CONTROLLABLE);
    }
    return encoding;
  }

  private StateEncoding createStateEncoding(final AutomatonProxy aut)
  {
    final StateEncoding encoding = new StateEncoding(aut);
    encoding.setNumberOfExtraStates(1);
    return encoding;
  }

  private boolean isMergingPartition(final TRPartition partition)
  {
    if (partition == null) {
      return false;
    } else {
      for (final int[] clazz : partition.getClasses()) {
        if (clazz != null && clazz.length > 1) {
          return true;
        }
      }
      return false;
    }
  }

  private int findDumpState(final TRPartition partition)
  {
    int dump = partition.getClasses().indexOf(null);
    if (dump < 0) {
      final int numStates = partition.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (partition.getClassCode(s) < 0) {
          dump = partition.getNumberOfClasses();
          break;
        }
      }
    }
    return dump;
  }

  /**
   * Creates a set of transitions of the input automaton states that have
   * removed in abstraction (unsupervisable transitions). Only transitions
   * between non-dump states are considered by this method.
   */
  private Set<TransitionProxy> collectRemovedTransitions
    (final AutomatonProxy inputAut,
     final EventEncoding inputEventEnc,
     final StateEncoding inputStateEnc,
     final ListBufferTransitionRelation outputRel,
     final TRPartition outputPartition)
  {
    Set<TransitionProxy> result = Collections.emptySet();
    if (!outputPartition.isEmpty()) {
      final boolean forward =
        (outputRel.getConfiguration() &
         ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0;
      final TransitionIterator iter = forward ?
        outputRel.createSuccessorsReadOnlyIterator() :
        outputRel.createPredecessorsReadOnlyIterator();
      for (final TransitionProxy trans : inputAut.getTransitions()) {
        final StateProxy source = trans.getSource();
        final int sourceCode = inputStateEnc.getStateCode(source);
        final int sourceClass = outputPartition.getClassCode(sourceCode);
        if (sourceClass < 0) {
          continue;
        }
        final StateProxy target = trans.getTarget();
        final int targetCode = inputStateEnc.getStateCode(target);
        final int targetClass = outputPartition.getClassCode(targetCode);
        if (targetClass < 0) {
          continue;
        }
        boolean found = false;
        final EventProxy event = trans.getEvent();
        final int e = inputEventEnc.getEventCode(event);
        final byte status = outputRel.getProperEventStatus(e);
        if (EventEncoding.isUsedEvent(status)) {
          if (forward) {
            iter.reset(sourceClass, e);
            while (iter.advance()) {
              if (iter.getCurrentTargetState() == targetClass) {
                found = true;
                break;
              }
            }
          } else {
            iter.reset(targetClass, e);
            while (iter.advance()) {
              if (iter.getCurrentSourceState() == sourceClass) {
                found = true;
                break;
              }
            }
          }
        } else {
          found = sourceClass == targetClass;
        }
        if (!found) {
          if (result.isEmpty()) {
            result = new THashSet<>();
          }
          result.add(trans);
        }
      }
    }
    return result;
  }

  private ListBufferTransitionRelation createMergedAbstraction
    (final AutomatonProxy aut,
     final Set<TransitionProxy> removedTransitions,
     final EventEncoding eventEnc,
     final StateEncoding inputStateEnc,
     final TRPartition partition)
    throws OverflowException
  {
    final String name = aut.getName();
    final ComponentKind  kind = aut.getKind();
    final int numClasses = partition.getNumberOfClasses();
    final int dump = findDumpState(partition);
    final int numStates = dump == numClasses ? numClasses + 1 : numClasses;
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final ListBufferTransitionRelation mergedTR =
      new ListBufferTransitionRelation(name, kind, eventEnc, numStates, config);
    if (!partition.isEmpty()) {
      final TransitionIterator iter = mergedTR.createSuccessorsReadOnlyIterator();
      final KindTranslator translator = getKindTranslator();
      for (final TransitionProxy trans : aut.getTransitions()) {
        final StateProxy source = trans.getSource();
        final int s = partition.getClassCode(inputStateEnc.getStateCode(source));
        if (s < 0) {
          continue;
        }
        if (removedTransitions.contains(trans)) {
          continue;
        }
        final EventProxy event = trans.getEvent();
        final int e = eventEnc.getEventCode(event);
        final StateProxy target = trans.getTarget();
        int t = partition.getClassCode(inputStateEnc.getStateCode(target));
        if (t < 0) {
          t = dump;
        }
        iter.reset(s, e);
        if (iter.advance()) {
          if (iter.getCurrentTargetState() != t) {
            return null;
          }
        } else if (t != dump ||
                   translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
          mergedTR.addTransition(s, e, t);
        }
      }
      final EventProxy defaultMarking = getUsedDefaultMarking();
      final int defaultMarkingID = eventEnc.getEventCode(defaultMarking);
      for (int c = 0; c < numClasses; c++) {
        final int[] clazz = partition.getStates(c);
        if (clazz != null) {
          for (final int s : clazz) {
            final StateProxy state = inputStateEnc.getState(s);
            if (state.isInitial()) {
              mergedTR.setInitial(c, true);
              break;
            }
          }
          if (defaultMarkingID >= 0) {
            for (final int s : clazz) {
              final StateProxy state = inputStateEnc.getState(s);
              if (state.getPropositions().contains(defaultMarking)) {
                mergedTR.setMarked(c, defaultMarkingID, true);
                break;
              }
            }
          }
        }
      }
      mergedTR.removeProperSelfLoopEvents(defaultMarkingID);
      mergedTR.removeRedundantPropositions();
      mergedTR.checkReachability();
    }
    return mergedTR;
  }

  private Map<EventProxy,List<EventProxy>> findEventRenaming
    (final ListBufferTransitionRelation before,
     final EventEncoding eventEnc,
     final TRPartition partition)
  {
    final ProductDESProxyFactory factory = getFactory();
    final int numEvents = eventEnc.getNumberOfProperEvents();
    final Map<EventProxy,List<EventProxy>> renaming =
      new HashMap<EventProxy,List<EventProxy>>(numEvents);
    final TransitionIterator iter =
      before.createSuccessorsReadOnlyIterator();
    for (int e = 0; e < numEvents; e++) {
      final byte status = before.getProperEventStatus(e);
      if (EventEncoding.isUsedEvent(status)) {
        int maxCount = 0;
        for (final int[] clazz : partition.getClasses()) {
          if (clazz != null) {
            final TIntHashSet successors = new TIntHashSet();
            for (final int s : clazz) {
              iter.reset(s, e);
              while (iter.advance()) {
                final int target = iter.getCurrentTargetState();
                final int targetClass = partition.getClassCode(target);
                successors.add(targetClass);
              }
            }
            final int count = successors.size();
            if (count > maxCount) {
              maxCount = count;
            }
          }
        }
        if (maxCount > 1) {
          final List<EventProxy> replacements =
            new ArrayList<EventProxy>(maxCount);
          final EventProxy event = eventEnc.getProperEvent(e);
          final String name = event.getName();
          final EventKind kind = event.getKind();
          final boolean observable = event.isObservable();
          for (int i = 0; i < maxCount; i++) {
            final String replacementName = "{" + name + ":" + i + "}";
            final EventProxy replacement =
              factory.createEventProxy(replacementName, kind, observable);
            replacements.add(replacement);
          }
          renaming.put(event, replacements);
        }
      }
    }
    return renaming;
  }

  private EventEncoding createRenamedEventEncoding
    (final EventEncoding eventEnc,
     final Map<EventProxy,List<EventProxy>> renaming)
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding renamedEnc = new EventEncoding();
    final int numEvents = eventEnc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents ; e++) {
      final EventProxy event = eventEnc.getProperEvent(e);
      final byte status = eventEnc.getProperEventStatus(e);
      final List<EventProxy> replacements = renaming.get(event);
      if (replacements == null) {
        renamedEnc.addEvent(event, translator, status);
      } else {
        for (final EventProxy replacement : replacements) {
          renamedEnc.addEvent(replacement, translator, status);
        }
      }
    }
    final int numProps = eventEnc.getNumberOfPropositions();
    for (int p = 0; p < numProps; p++) {
      final EventProxy prop = eventEnc.getProposition(p);
      renamedEnc.addEvent(prop, translator, 0);
    }
    return renamedEnc;
  }

  private ListBufferTransitionRelation createPseudoSupervisorTR
    (final AutomatonProxy aut,
     final Set<TransitionProxy> removedTransitions,
     final EventEncoding eventEnc,
     final StateEncoding inputStateEnc,
     final StateEncoding outputStateEnc,
     final TRPartition partition,
     final HalfWaySynthesisTRSimplifier.OutputMode mode,
     final int config)
    throws OverflowException
  {
    if (partition == null) {
      return null;
    } else if (partition.isEmpty()) {
      return new ListBufferTransitionRelation
        (":null", ComponentKind.SUPERVISOR, 1, 0, 0,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
    final AutomatonProxy supervisorAut = createPseudoSupervisorAut
      (aut, removedTransitions, eventEnc, inputStateEnc, partition, mode);
    if (supervisorAut == aut &&
        mode != HalfWaySynthesisTRSimplifier.OutputMode.ABSTRACTION) {
      return null;
    }
    return new ListBufferTransitionRelation
      (supervisorAut, eventEnc, outputStateEnc, config);
  }

  private AutomatonProxy createPseudoSupervisorAut
    (final AutomatonProxy aut,
     final Set<TransitionProxy> removedTransitions,
     final EventEncoding eventEnc,
     final StateEncoding stateEnc,
     final TRPartition partition,
     final HalfWaySynthesisTRSimplifier.OutputMode mode)
    throws OverflowException
  {
    // 1. Find bad states. Are there any?
    final int numStates = stateEnc.getNumberOfStates();
    final int numSafeStates = partition.getNumberOfAssignedStates();
    if (numSafeStates == numStates) {
      return aut;
    }

    // 2. Do we have to disable any controllable transitions?
    final KindTranslator translator = getKindTranslator();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    boolean disabling = false;
    for (final TransitionProxy trans : transitions) {
      final StateProxy target = trans.getTarget();
      final int t = stateEnc.getStateCode(target);
      if (partition.getClassCode(t) < 0) {
        final EventProxy event = trans.getEvent();
        final EventKind kind = translator.getEventKind(event);
        if (mode.isRetainedEvent(kind)) {
          final StateProxy source = trans.getSource();
          final int s = stateEnc.getStateCode(source);
          if (partition.getClassCode(s) >= 0) {
            disabling = true;
            break;
          }
        }
      }
    }
    if (!disabling) {
      return aut;
    }
    // OK, it seems we really need a supervisor ...

    // 3. Create state space
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final List<StateProxy> supervisorStates =
      new ArrayList<StateProxy>(numSafeStates + 1);
    StateProxy dumpState = null;
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      if (partition.getClassCode(s) >= 0) {
        supervisorStates.add(state);
      } else if (dumpState == null &&
                 !state.getPropositions().contains(defaultMarking)) {
        dumpState = state;
      }
    }
    assert dumpState != null;
    supervisorStates.add(dumpState);

    // 4. Count selfloops and determine actual events
    final int numEvents = eventEnc.getNumberOfProperEvents();
    final int[] selfloops = new int[numEvents];
    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final int s = stateEnc.getStateCode(source);
      if (partition.getClassCode(s) >= 0 &&
          !removedTransitions.contains(trans)) {
        final EventProxy event = trans.getEvent();
        final int e = eventEnc.getEventCode(event);
        if (source != trans.getTarget()) {
          selfloops[e] = -1;
        } else if (selfloops[e] >= 0) {
          selfloops[e]++;
        }
      }
    }
    int numSupervisorEvents = 0;
    for (int e = 0; e < numEvents; e++) {
      if (selfloops[e] < numSafeStates) {
        numSupervisorEvents++;
      }
    }
    final Collection<EventProxy> supervisorEvents;
    if (numSupervisorEvents == numEvents) {
      supervisorEvents = aut.getEvents();
    } else {
      supervisorEvents = new ArrayList<EventProxy>(numSupervisorEvents + 1);
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        if (selfloops[e] < numSafeStates) {
          final EventProxy event = eventEnc.getProperEvent(e);
          supervisorEvents.add(event);
        }
      }
      supervisorEvents.add(defaultMarking);
    }

    // 5. Collect transitions
    final ProductDESProxyFactory factory = getFactory();
    final Collection<TransitionProxy> supervisorTransitions =
      new ArrayList<TransitionProxy>(transitions.size());
    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final int s = stateEnc.getStateCode(source);
      if (partition.getClassCode(s) < 0 ||
          removedTransitions.contains(trans)) {
        continue;
      }
      final EventProxy event = trans.getEvent();
      final int e = eventEnc.getEventCode(event);
      if (selfloops[e] == numSafeStates) {
        continue;
      }
      final EventKind kind = translator.getEventKind(event);
      final StateProxy target = trans.getTarget();
      final int t = stateEnc.getStateCode(target);
      if (partition.getClassCode(t) >= 0) {
        supervisorTransitions.add(trans);
      } else if (mode.isRetainedEvent(kind)) {
        if (target == dumpState) {
          supervisorTransitions.add(trans);
        } else {
          final TransitionProxy supervisorTrans =
            factory.createTransitionProxy(source, event, dumpState);
          supervisorTransitions.add(supervisorTrans);
        }
      }
    }

    // 6. Create pseudo-supervisor automaton
    final String name = aut.getName();
    final AutomatonProxy supervisorAut =
      factory.createAutomatonProxy(name, ComponentKind.SUPERVISOR,
                                   supervisorEvents, supervisorStates,
                                   supervisorTransitions);
    return supervisorAut;
  }

  private AutomatonProxy createDistinguisher
    (final ListBufferTransitionRelation rel,
     final EventEncoding originalEnc,
     final Map<EventProxy,List<EventProxy>> renaming,
     final StateEncoding stateEnc,
     final TRPartition partition)
  {
    // 1. Create events --- renamed
    final int numEvents = originalEnc.getNumberOfProperEvents();
    final List<EventProxy> events = createAlphabet(rel, originalEnc, renaming);

    // 2. Create states --- no dump states in distinguisher
    final int numStates = stateEnc.getNumberOfStates();
    final int numSafeStates = partition.getNumberOfAssignedStates();
    final List<StateProxy> states = new ArrayList<StateProxy>(numSafeStates);
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      if (partition.getClassCode(s) >= 0) {
        states.add(state);
      }
    }

    // 3. Create transitions --- renamed
    final ProductDESProxyFactory factory = getFactory();
    final int numTrans = rel.getNumberOfTransitions();
    final List<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numTrans);
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (int e = 0; e < numEvents; e++) {
      final EventProxy event = originalEnc.getProperEvent(e);
      List<EventProxy> replacements = renaming.get(event);
      if (replacements == null) {
        replacements = Collections.singletonList(event);
      }
      for (final int[] clazz : partition.getClasses()) {
        if (clazz != null) {
          final TIntObjectHashMap<EventProxy> replacementMap =
            new TIntObjectHashMap<EventProxy>();
          int r = 0;
          for (final int s : clazz) {
            iter.reset(s, e);
            if (iter.advance()) {
              final int t = iter.getCurrentTargetState();
              final int c = partition.getClassCode(t);
              EventProxy replacement = replacementMap.get(c);
              if (replacement == null) {
                replacement = replacements.get(r++);
                replacementMap.put(c, replacement);
              }
              if (partition.getClassCode(t) >= 0) {
                final StateProxy source = stateEnc.getState(s);
                final StateProxy target = stateEnc.getState(t);
                final TransitionProxy trans =
                  factory.createTransitionProxy(source, replacement, target);
                transitions.add(trans);
              }
            }
          }
        }
      }
    }

    // 4. Make automaton
    final String name = "dis:" + rel.getName();
    final ComponentKind kind = ComponentKind.SUPERVISOR;
    return factory.createAutomatonProxy(name, kind, events, states, transitions);
  }

  private AutomatonProxy createRenamedAbstraction
    (final ListBufferTransitionRelation before,
     final EventEncoding originalEnc,
     final EventEncoding renamedEnc,
     final Map<EventProxy,List<EventProxy>> renaming,
     final TRPartition partition)
    throws OverflowException
  {
    // 1. Initialise transition relation for output
    final String name = before.getName();
    final ComponentKind kind = before.getKind();
    final int numClasses = partition.getNumberOfClasses();
    final int dump = findDumpState(partition);
    final int numStates = dump == numClasses ? numClasses + 1 : numClasses;
    final ListBufferTransitionRelation after =
      new ListBufferTransitionRelation(name, kind, renamedEnc, numStates,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    // 2. Set state attributes --- merged
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = renamedEnc.getEventCode(defaultMarking);
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = partition.getStates(c);
      if (clazz != null) {
        for (final int s : clazz) {
          if (before.isInitial(s)) {
            after.setInitial(c, true);
            break;
          }
        }
        if (defaultMarkingID >= 0) {
          for (final int s : clazz) {
            if (before.isMarked(s, defaultMarkingID)) {
              after.setMarked(c, defaultMarkingID, true);
              break;
            }
          }
        }
      } else if (c != dump) {
        after.setReachable(c, false);
      }
    }

    // 3. Copy transitions --- merged and renamed
    final int numEvents = originalEnc.getNumberOfProperEvents();
    final TransitionIterator iter = before.createSuccessorsReadOnlyIterator();
    boolean dumpReachable = false;
    for (int e = 0; e < numEvents; e++) {
      final EventProxy event = originalEnc.getProperEvent(e);
      final List<EventProxy> replacements = renaming.get(event);
      int[] rs;
      if (replacements == null) {
        rs = new int[1];
        rs[0] = renamedEnc.getEventCode(event);
      } else {
        rs = new int[replacements.size()];
        int ri = 0;
        for (final EventProxy replacement : replacements) {
          rs[ri++] = renamedEnc.getEventCode(replacement);
        }
      }
      for (int sc = 0; sc < numClasses; sc++) {
        final int[] clazz = partition.getStates(sc);
        if (clazz != null) {
          final TIntHashSet targetClasses = new TIntHashSet();
          int ri = 0;
          for (final int s : clazz) {
            iter.reset(s, e);
            if (iter.advance()) {
              final int t = iter.getCurrentTargetState();
              int tc = partition.getClassCode(t);
              if (tc < 0) {
                tc = dump;
              }
              if (targetClasses.add(tc)) {
                final int r = rs[ri++];
                after.addTransition(sc, r, tc);
                dumpReachable |= tc == dump;
              }
            }
          }
        }
      }
    }
    if (dump >= 0 && !dumpReachable) {
      after.setReachable(dump, false);
    }

    // 4. Clean up redundancy
    after.removeProperSelfLoopEvents(defaultMarkingID);
    after.removeRedundantPropositions();

    // 5. Make automaton
    final ProductDESProxyFactory factory = getFactory();
    return after.createAutomaton(factory, renamedEnc);
  }


  //#########################################################################
  //# Static Methods for Automaton Construction
  static List<EventProxy> createAlphabet
    (final ListBufferTransitionRelation rel,
     final EventEncoding originalEnc,
     final Map<EventProxy,List<EventProxy>> renaming)
  {
    final int numEvents = originalEnc.getNumberOfProperEvents();
    final List<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventEncoding.isUsedEvent(status)) {
        final EventProxy event = originalEnc.getProperEvent(e);
        if (renaming.get(event) != null) {
          events.addAll(renaming.get(event));
        } else {
          events.add(event);
        }
      }
    }
    for (int p = 0; p < originalEnc.getNumberOfPropositions(); p++) {
      if (rel.isUsedProposition(p)) {
        final EventProxy prop = originalEnc.getProposition(p);
        events.add(prop);
      }
    }
    return events;
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mChain;

  private boolean mUsingRenaming;


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
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalAutomataSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, and synthesis
   * observation equivalence.
   */
  static final int CHAIN_SOE = USE_HALFWAY | USE_BISIMULATION | USE_SOE;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalAutomataSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, and weak synthesis
   * observation equivalence. This is the default.
   */
  static final int CHAIN_WSOE = USE_HALFWAY | USE_BISIMULATION | USE_WSOE;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalAutomataSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, synthesis
   * observation equivalence, and weak synthesis observation equivalence.
   */
  static final int CHAIN_ALL = USE_HALFWAY | USE_BISIMULATION | USE_SOE
                               | USE_WSOE;

}
