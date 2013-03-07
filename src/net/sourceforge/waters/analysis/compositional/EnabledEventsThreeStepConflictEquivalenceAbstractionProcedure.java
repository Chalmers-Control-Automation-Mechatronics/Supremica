//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ThreeStepConflictEquivalenceAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.EnabledEventsLimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.EnabledEventsSilentContinuationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
       final boolean useProperCertainConflicts)
  {
    final ChainTRSimplifier preChain = new ChainTRSimplifier();
    final ChainTRSimplifier postChain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    preChain.add(loopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    preChain.add(markingRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    preChain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    preChain.add(silentOutRemover);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    final int limit = analyzer.getInternalTransitionLimit();
    incomingEquivalenceSimplifier.setTransitionLimit(limit);
    preChain.add(incomingEquivalenceSimplifier);

    final EnabledEventsSilentContinuationTRSimplifier enabledEventsSimplifier =
      new EnabledEventsSilentContinuationTRSimplifier();
    preChain.add(enabledEventsSimplifier);





    final EnabledEventsLimitedCertainConflictsTRSimplifier limitedCertainConflictsRemover;
    if (useLimitedCertainConflicts) {
      limitedCertainConflictsRemover =
        new EnabledEventsLimitedCertainConflictsTRSimplifier();
    } else {
      limitedCertainConflictsRemover = null;
    }
    final CertainConflictsTRSimplifier certainConflictsRemover;
    if (useProperCertainConflicts) {
      certainConflictsRemover = new CertainConflictsTRSimplifier();
    } else {
      certainConflictsRemover = null;
    }
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
    (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
    (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    postChain.add(bisimulator);
    if (includeNonAlphaDeterminisation) {
      final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
        new NonAlphaDeterminisationTRSimplifier();
      nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      nonAlphaDeterminiser.setTransitionLimit(limit);
      postChain.add(nonAlphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    postChain.add(saturator);
    return new EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
      (analyzer, preChain, limitedCertainConflictsRemover,
       certainConflictsRemover, enabledEventsSimplifier, postChain);
  }


  //#########################################################################
  //# Constructor
  private EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier preChain,
     final EnabledEventsLimitedCertainConflictsTRSimplifier limitedCCSimplifier,
     final CertainConflictsTRSimplifier ccSimplifier,
     final EnabledEventsSilentContinuationTRSimplifier enabledEventsSimplifier,
     final ChainTRSimplifier postChain)
  {
    super(analyzer);
    mPreChain = preChain;
    mLimitedCertainConflictsSimplifier = limitedCCSimplifier;
    mCertainConflictsSimplifier = ccSimplifier;
    mEnabledEventsSimplifier = enabledEventsSimplifier;
    mPostChain = postChain;
    mCompleteChain = new ChainTRSimplifier();
    mCompleteChain.add(preChain);
    if (limitedCCSimplifier != null) {
      mCompleteChain.add(limitedCCSimplifier);
    }
    if (ccSimplifier != null) {
      mCompleteChain.add(ccSimplifier);
    }
    mCompleteChain.add(postChain);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    try {
      assert local.size() <= 1 : "At most one tau event supported!";
      final ProductDESProxyFactory factory = getFactory();
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final StateEncoding inputStateEnc = new StateEncoding(aut);       //can always access compisitionalconflictechcker
      final int config = mPreChain.getPreferredInputConfiguration();    //use that to find mNumberofEnabledEvents

      final EnabledEventsCompositionalConflictChecker enabledEventsAnalyzer = (EnabledEventsCompositionalConflictChecker)getAnalyzer();

      final List<EventProxy> eventsList = new ArrayList<EventProxy>(aut.getEvents().size());


      //mCurrentAutomaton where is it

      //maybe only let it do the next bit if it's already looked at all the auts to get enabledEvent info?


      int numEnabledEvents = 0;
      //for all the events
      for(final EventProxy events : aut.getEvents())
      {

        //Get event info somewhere
        final EnabledEventsCompositionalConflictChecker.EnabledEventsEventInfo eventInfo  = enabledEventsAnalyzer.getEventInfo(events);

        if(eventInfo != null)
        //check if event is always enabled or this automaton is only disabler
        if(eventInfo.isSingleDisablingAutomaton(aut))
        {

          eventsList.add(events);

      //count how many enabled events there are
          if(events != tau)
         numEnabledEvents++;
        }

      }

      for(final EventProxy events : aut.getEvents())
      {
        //Get event info somewhere
        final EnabledEventsCompositionalConflictChecker.EnabledEventsEventInfo eventInfo  = enabledEventsAnalyzer.getEventInfo(events);
        //check if event is always enabled or this automaton is only disabler

        if(eventInfo == null || !eventInfo.isSingleDisablingAutomaton(aut) )
          eventsList.add(events);

      }




      //Tell the simplifier how many enabled events there are
      mEnabledEventsSimplifier.setNumberOfEnabledEvents(numEnabledEvents);
      mLimitedCertainConflictsSimplifier.setNumberOfEnabledEvents(numEnabledEvents) ;

      //create Event Encoding in right order with all enabled events at front of list
      final EventEncoding eventEnc = createEventEncoding(eventsList, tau);
      //Give EventEncoding Marking


     // System.out.println(numEnabledEvents);



      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
      analyzer.showDebugLog(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings();
      AutomatonProxy lastAut = aut;
      StateEncoding lastStateEnc = inputStateEnc;
      List<int[]> partition = null;
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
            lccStep = new LimitedCertainConflictsStep                       //Give this lots of info
              (analyzer, mLimitedCertainConflictsSimplifier, outputAut,     //this creates the trace expander, so will get it this info
               lastAut, tau, lastStateEnc, outputStateEnc, eventEnc, numEnabledEvents);
          } else {
            final List<int[]> ccPart =
              mLimitedCertainConflictsSimplifier.getResultPartition();
            partition = ChainTRSimplifier.mergePartitions(partition, ccPart);
            preStep = createStep(aut, inputStateEnc,
                                 outputAut, outputStateEnc, tau,
                                 partition, oeq, false);
          }
          lastAut = outputAut;
          lastStateEnc = outputStateEnc;
        }
        maybeBlocking =
          mLimitedCertainConflictsSimplifier.getMaxLevel() >= 0;
      }
      AbstractionStep ccStep = null;
      if (maybeBlocking && mCertainConflictsSimplifier != null) {
        mCertainConflictsSimplifier.setTransitionRelation(rel);
        if (mCertainConflictsSimplifier.run()) {
          rel = mCertainConflictsSimplifier.getTransitionRelation();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy outputAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          ccStep = new LimitedCertainConflictsStep
            (analyzer, mCertainConflictsSimplifier, outputAut, lastAut,
             tau, lastStateEnc, outputStateEnc);
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
        } else if (lccStep == null && ccStep == null) {
          lastAut = aut;
          lastStateEnc = inputStateEnc;
          final List<int[]> postPart = mPostChain.getResultPartition();
          partition = ChainTRSimplifier.mergePartitions(partition, postPart);
          oeq &= mPostChain.isObservationEquivalentAbstraction();
        } else {
          recordStep(steps, preStep);
          recordStep(steps, lccStep);
          recordStep(steps, ccStep);
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
        recordStep(steps, ccStep);
      }
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


  //#########################################################################
  //# Auxiliary Methods
  protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final EventProxy tau)
  {
    final Collection<EventProxy> events = aut.getEvents();
    return createEventEncoding(events, tau);
  }

  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final EventProxy tau)
  {
    final KindTranslator translator = getKindTranslator();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final Collection<EventProxy> filter =
      Collections.singletonList(defaultMarking);
    final EventEncoding enc =
      new EventEncoding(events, translator, tau, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    final int defaultMarkingID = enc.getEventCode(defaultMarking);
    if (defaultMarkingID < 0) {
      enc.addEvent(defaultMarking, translator, true);
    }
    mCompleteChain.setDefaultMarkingID(defaultMarkingID);
    return enc;
  }

  private AbstractionStep createStep(final AutomatonProxy input,
                                     final StateEncoding inputStateEnc,
                                     final AutomatonProxy output,
                                     final StateEncoding outputStateEnc,
                                     final EventProxy tau,
                                     final List<int[]> partition,
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
    mLimitedCertainConflictsSimplifier;
  private final CertainConflictsTRSimplifier
    mCertainConflictsSimplifier;
  private final EnabledEventsSilentContinuationTRSimplifier
    mEnabledEventsSimplifier;
  private final ChainTRSimplifier mPostChain;
  private final ChainTRSimplifier mCompleteChain;

}
