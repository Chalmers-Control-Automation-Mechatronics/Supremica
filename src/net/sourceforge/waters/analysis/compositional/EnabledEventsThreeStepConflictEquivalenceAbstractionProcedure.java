//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ThreeStepConflictEquivalenceAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.AlwaysEnabledEventsFinder;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.EnabledEventsLimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.EnabledEventsSilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A specialised abstraction procedure used by the compositional conflict
 * check algorithm. This abstraction procedure splits the abstraction
 * process into three stages. Abstraction steps before and after certain
 * conflicts are separated from certain conflicts computation to facilitate
 * counterexample expansion.
 *
 * @author Robi Malik
 */

class EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
    createThreeStepConflictEquivalenceAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer,
       final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final boolean includeNonAlphaDeterminisation,
       final boolean useLimitedCertainConflicts,
       final boolean useAlwaysEnabledLimitedCertainConflicts)
  {
    final int limit = analyzer.getInternalTransitionLimit();
    final ChainTRSimplifier preChain = new ChainTRSimplifier();
    final ChainTRSimplifier postChain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setDumpStateAware(true);
    preChain.add(loopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    preChain.add(markingRemover);
    final TransitionRemovalTRSimplifier transitionRemover =
      new TransitionRemovalTRSimplifier();
    transitionRemover.setTransitionLimit(limit);
    preChain.add(transitionRemover);

    final EnabledEventsSilentIncomingTRSimplifier enabledEventsSilentIncomingSimplifier =
      new EnabledEventsSilentIncomingTRSimplifier();
    enabledEventsSilentIncomingSimplifier.setRestrictsToUnreachableStates(true);
    enabledEventsSilentIncomingSimplifier.setDumpStateAware(true);
    preChain.add(enabledEventsSilentIncomingSimplifier);

    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    preChain.add(silentOutRemover);

    final LimitedCertainConflictsTRSimplifier limitedCertainConflictsRemover;
    if (useLimitedCertainConflicts) {
      limitedCertainConflictsRemover =
        new LimitedCertainConflictsTRSimplifier();
    } else {
      limitedCertainConflictsRemover = null;
    }

    final EnabledEventsLimitedCertainConflictsTRSimplifier enabledEventsLimitedCertainConflictsRemover;
    if (useAlwaysEnabledLimitedCertainConflicts) {
      enabledEventsLimitedCertainConflictsRemover = new EnabledEventsLimitedCertainConflictsTRSimplifier();
    } else {
      enabledEventsLimitedCertainConflictsRemover = null;
    }

    final ObservationEquivalenceTRSimplifier slBisimulator =
      new ObservationEquivalenceTRSimplifier();
    slBisimulator.setEquivalence(equivalence);
    slBisimulator.setTransitionRemovalMode(ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    slBisimulator.setMarkingMode(ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    slBisimulator.setTransitionLimit(limit);
    slBisimulator.setDumpStateAware(true);
    postChain.add(slBisimulator);

    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    postChain.add(incomingEquivalenceSimplifier);

    if (includeNonAlphaDeterminisation) {
      final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
        new NonAlphaDeterminisationTRSimplifier();
      nonAlphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      nonAlphaDeterminiser.setTransitionLimit(limit);
      nonAlphaDeterminiser.setDumpStateAware(true);
      postChain.add(nonAlphaDeterminiser);
    }

    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    postChain.add(saturator);

    final AlwaysEnabledEventsFinder finder = new AlwaysEnabledEventsFinder();
    postChain.add(finder);

    return new EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
      (analyzer, preChain, postChain,
       limitedCertainConflictsRemover,
       enabledEventsLimitedCertainConflictsRemover,
       enabledEventsSilentIncomingSimplifier, finder);
  }


  //#########################################################################
  //# Constructor
  private EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier preChain,
     final ChainTRSimplifier postChain,
     final LimitedCertainConflictsTRSimplifier limitedCCSimplifier,
     final EnabledEventsLimitedCertainConflictsTRSimplifier alwaysEnabledLimitedCCSimplifier,
     final EnabledEventsSilentIncomingTRSimplifier enabledEventsSilentIncomingSimplifier,
     final AlwaysEnabledEventsFinder finder)
  {
    super(analyzer);
    mPreChain = preChain;
    mEnabledEventsLimitedCertainConflictsSimplifier = alwaysEnabledLimitedCCSimplifier;
    mLimitedCertainConflictsSimplifier = limitedCCSimplifier;
    mEnabledEventsSilentIncomingSimplifier = enabledEventsSilentIncomingSimplifier;
    mAlwaysEnabledEventsFinder = finder;
    mPostChain = postChain;
    mCompleteChain = new ChainTRSimplifier();
    mCompleteChain.add(preChain);
    if (limitedCCSimplifier != null) {
      mCompleteChain.add(limitedCCSimplifier);
    }
    if (alwaysEnabledLimitedCCSimplifier != null) {
      mCompleteChain.add(alwaysEnabledLimitedCCSimplifier);
    }
    mCompleteChain.add(postChain);
  }


  //#########################################################################
  //# Simple Access
  Set<EventProxy> getAlwaysEnabledEvents()
  {
    return mAlwaysEnabledEvents;
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
    try {
      assert local.size() <= 1 : "At most one tau event supported!";
      final ProductDESProxyFactory factory = getFactory();
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = mPreChain.getPreferredInputConfiguration();

      final EnabledEventsCompositionalConflictChecker enabledEventsAnalyzer = (EnabledEventsCompositionalConflictChecker)getAnalyzer();

      final List<EventProxy> eventsList = new ArrayList<EventProxy>(aut.getEvents().size());
      int numEnabledEvents = 0;
      final boolean aePlus =
        enabledEventsAnalyzer.getEnabledEventSearchStateLimit() >=
        inputStateEnc.getNumberOfStates();
      if (aePlus) {
        final List<EventProxy> enabledEventsList =
          new ArrayList<EventProxy>(aut.getEvents().size());
        enabledEventsList.addAll
          (enabledEventsAnalyzer.calculateAlwaysEnabledEvents(aut, candidate));
        final List<EventProxy> otherEventsList =
          new ArrayList<EventProxy>(aut.getEvents().size());
        otherEventsList.addAll(aut.getEvents());
        otherEventsList.removeAll(enabledEventsList);
        //Creates an event encoding with always enabled events at start
        eventsList.addAll(enabledEventsList);
        eventsList.addAll(otherEventsList);
        numEnabledEvents = enabledEventsList.size();
      } else {
        for (final EventProxy event : aut.getEvents()) {
          final EnabledEventsCompositionalConflictChecker.EnabledEventsEventInfo eventInfo =
            enabledEventsAnalyzer.getEventInfo(event);
          if (eventInfo != null) {
            //check if event is always enabled or this automaton is only disabler
            if (eventInfo.isSingleDisablingCandidate(candidate)) {
              eventsList.add(event);
              // Count how many enabled events there are
              if (event != tau) { //Tau is added to the list here but not counted as always Enabled.
                numEnabledEvents++;
              }
            }
          }
        }
        for (final EventProxy events : aut.getEvents()) {
          final EnabledEventsCompositionalConflictChecker.EnabledEventsEventInfo eventInfo =
            enabledEventsAnalyzer.getEventInfo(events);
          //Adds the propositions and other events.
          if (eventInfo == null ||
              !eventInfo.isSingleDisablingCandidate(candidate)) {
            eventsList.add(events);
          }
        }
      }

      // Tell the simplifiers how many enabled events there are
      mEnabledEventsSilentIncomingSimplifier.
        setNumberOfEnabledEvents(numEnabledEvents);
      if (mEnabledEventsLimitedCertainConflictsSimplifier != null) {
        mEnabledEventsLimitedCertainConflictsSimplifier.
          setNumberOfEnabledEvents(numEnabledEvents);
      }
      // Create Event Encoding in right order with all enabled events at front of list
      final EventEncoding eventEnc =
        createEventEncoding(eventsList, local, numEnabledEvents, candidate);
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc, config);

      final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
      analyzer.showDebugLog(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = aut.getTransitions().size();
      final int numMarkings = rel.getNumberOfMarkings();
      AutomatonProxy lastAut = aut;
      StateEncoding lastStateEnc = inputStateEnc;
      TRPartition partition = null;
      boolean oeq = true;
      boolean reduced = false;
      AbstractionStep preStep = null;
      mPreChain.setTransitionRelation(rel);
      if (mPreChain.run()) {
        rel = mPreChain.getTransitionRelation();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy outputAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        partition = mPreChain.getResultPartition();
        oeq = mPreChain.isObservationEquivalentAbstraction();
        preStep = createStep(aut, inputStateEnc,
                             outputAut, outputStateEnc, tau,
                             partition, oeq, false);
        lastAut = outputAut;
        lastStateEnc = outputStateEnc;
      }
      boolean maybeBlocking = true;
      AbstractionStep lccStep = null;
      if (mLimitedCertainConflictsSimplifier != null) {
        mLimitedCertainConflictsSimplifier.setTransitionRelation(rel);
        if (mLimitedCertainConflictsSimplifier.run()) {
          rel = mLimitedCertainConflictsSimplifier.getTransitionRelation();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy outputAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          if (mLimitedCertainConflictsSimplifier.hasCertainConflictTransitions()) {
            lccStep = new LimitedCertainConflictsStep
              (analyzer, mLimitedCertainConflictsSimplifier, outputAut,
               lastAut, tau, lastStateEnc, outputStateEnc);
          } else {
            final TRPartition ccPart =
              mLimitedCertainConflictsSimplifier.getResultPartition();
            partition = TRPartition.combine(partition, ccPart);
            preStep = createStep(aut, inputStateEnc,
                                 outputAut, outputStateEnc, tau,
                                 partition, oeq, false);
          }
          lastAut = outputAut;
          lastStateEnc = outputStateEnc;
        }
        maybeBlocking =  mLimitedCertainConflictsSimplifier.getMaxLevel() >= 0;
      }
      AbstractionStep eelccStep = null;
      if (maybeBlocking && mEnabledEventsLimitedCertainConflictsSimplifier != null) {
        mEnabledEventsLimitedCertainConflictsSimplifier.setTransitionRelation(rel);
        if (mEnabledEventsLimitedCertainConflictsSimplifier.run()) {
          rel = mEnabledEventsLimitedCertainConflictsSimplifier.getTransitionRelation();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy outputAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          final TRPartition ccPart =
            mEnabledEventsLimitedCertainConflictsSimplifier.getResultPartition();
          if (mEnabledEventsLimitedCertainConflictsSimplifier.hasCertainConflictTransitions() ||
              lccStep != null) {
            final int[] levels =
              mEnabledEventsLimitedCertainConflictsSimplifier.getLevels();
            eelccStep = new EnabledEventsLimitedCertainConflictsStep
              (analyzer, outputAut, lastAut, tau, lastStateEnc,
               outputStateEnc, ccPart, levels);
          } else {
            partition = TRPartition.combine(partition, ccPart);
            preStep = createStep(aut, inputStateEnc,
                                 outputAut, outputStateEnc, tau,
                                 partition, oeq, false);
          }
          lastAut = outputAut;
          lastStateEnc = outputStateEnc;
        }
      }

      mPostChain.setTransitionRelation(rel);
      if (mPostChain.run()) {
        rel = mPostChain.getTransitionRelation();
        if (rel.getNumberOfReachableStates() == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings() == numMarkings) {
          return false;
        } else if (lccStep == null && eelccStep == null) {
          lastAut = aut;
          lastStateEnc = inputStateEnc;
          final TRPartition postPart = mPostChain.getResultPartition();
          partition = TRPartition.combine(partition, postPart);
          oeq &= mPostChain.isObservationEquivalentAbstraction();
        } else {
          recordStep(steps, preStep);
          recordStep(steps, lccStep);
          recordStep(steps, eelccStep);
          partition = mPostChain.getResultPartition();
          oeq = mPostChain.isObservationEquivalentAbstraction();
          reduced = false;
        }
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy outputAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        final AbstractionStep postStep =
          createStep(lastAut, lastStateEnc, outputAut, outputStateEnc,
                     tau, partition, oeq, reduced);
        recordStep(steps, postStep);
      } else {
        recordStep(steps, preStep);
        recordStep(steps, lccStep);
        recordStep(steps, eelccStep);
      }
      collectAlwaysEnabledEvents(eventEnc);
      return !steps.isEmpty();
    } finally {
      mCompleteChain.reset();
    }
  }

  @Override
  public void storeStatistics()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setSimplifierStatistics(mCompleteChain);
  }

  @Override
  public void resetStatistics()
  {
    mCompleteChain.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mCompleteChain.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mCompleteChain.isAborting();
  }

  @Override
  public void resetAbort()
  {
    mCompleteChain.resetAbort();
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                            final Collection<EventProxy> local,
                                            final int numAlwaysEnabled,
                                            final Candidate candidate)
    throws OverflowException
  {
    final EnabledEventsCompositionalConflictChecker analyzer =
      (EnabledEventsCompositionalConflictChecker) getAnalyzer();
    final KindTranslator translator = getKindTranslator();
    Collection<EventProxy> filter = getPropositions();
    if (filter == null) {
      filter = Collections.emptyList();
    }
    int e = 0;
    final EventEncoding enc = new EventEncoding();
    for (final EventProxy event : events) {
      if (local.contains(event)) {
        enc.addSilentEvent(event);
      } else if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (filter.contains(event)) {
          enc.addEvent(event, translator, 0);
        }
      } else {
        byte status = 0;
        if (analyzer.isUsingSpecialEvents()) {
          final AbstractCompositionalModelAnalyzer.EventInfo info =
            analyzer.getEventInfo(event);
          if (info.isOnlyNonSelfLoopCandidate(candidate)) {
            status |= EventStatus.STATUS_OUTSIDE_ONLY_SELFLOOP;
          }
        }
        if (e < numAlwaysEnabled) {
          status |= EventStatus.STATUS_OUTSIDE_ALWAYS_ENABLED;
        }
        enc.addEvent(event, translator, status);
        e++;
      }
    }
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = enc.getEventCode(defaultMarking);
    if (defaultMarkingID < 0) {
      enc.addEvent(defaultMarking, translator, EventStatus.STATUS_UNUSED);
    }
    mCompleteChain.setDefaultMarkingID(defaultMarkingID);
    return enc;
  }

  private void collectAlwaysEnabledEvents(final EventEncoding eventEnc)
  {
    final TIntArrayList codes =
      mAlwaysEnabledEventsFinder.getAlwaysEnabledEvents();
    mAlwaysEnabledEvents = new THashSet<>(codes.size());
    for (int i = 0; i < codes.size(); i++) {
      final int e = codes.get(i);
      final EventProxy event = eventEnc.getProperEvent(e);
      mAlwaysEnabledEvents.add(event);
    }
  }

  private AbstractionStep createStep(final AutomatonProxy input,
                                     final StateEncoding inputStateEnc,
                                     final AutomatonProxy output,
                                     final StateEncoding outputStateEnc,
                                     final EventProxy tau,
                                     final TRPartition partition,
                                     final boolean oeq,
                                     final boolean reduced)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    if (oeq) {
      return new ObservationEquivalenceStep(analyzer, output, input, tau,
                                            inputStateEnc, partition,
                                            reduced, outputStateEnc);
    } else {
      return new ConflictEquivalenceStep(analyzer, output, input, tau,
                                         inputStateEnc, partition,
                                         reduced, outputStateEnc);
    }
  }

  private void recordStep(final List<AbstractionStep> steps,
                          final AbstractionStep step)
  {
    if (step != null) {
      steps.add(step);
    }
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mPreChain;
  private final EnabledEventsLimitedCertainConflictsTRSimplifier
    mEnabledEventsLimitedCertainConflictsSimplifier;
  private final LimitedCertainConflictsTRSimplifier
    mLimitedCertainConflictsSimplifier;
  private final EnabledEventsSilentIncomingTRSimplifier
    mEnabledEventsSilentIncomingSimplifier;
  private final AlwaysEnabledEventsFinder mAlwaysEnabledEventsFinder;
  private final ChainTRSimplifier mPostChain;
  private final ChainTRSimplifier mCompleteChain;

  private Set<EventProxy> mAlwaysEnabledEvents;

}