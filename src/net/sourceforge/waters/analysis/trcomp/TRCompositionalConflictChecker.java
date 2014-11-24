//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.tr.DuplicateFreeQueue;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractModelAnalyzer
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker(final ProductDESProxyFactory factory)
  {
    super(factory, ConflictKindTranslator.getInstanceControllable());
    // TODO Make these configurable
    mPreselectionHeuristic = new PreselectionHeuristicMustL();
    final SelectionHeuristic<TRCandidate> minS = new SelectionHeuristicMinS();
    mSelectionHeuristic = new ChainSelectionHeuristic<TRCandidate>(minS);
    mPartitioningListener = new PartitioningListener();
    mSpecialEventsListener = new PartitioningListener();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final SpecialEventsTRSimplifier special = new SpecialEventsTRSimplifier();
    special.setSimplificationListener(mSpecialEventsListener);
    chain.add(special);
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(mPartitioningListener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                               Equivalence.OBSERVATION_EQUIVALENCE);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    bisimulator.setSimplificationListener(mPartitioningListener);
    chain.add(bisimulator);
    chain.setPropositions(PRECONDITION_MARKING, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mTRSimplifier = chain;
    mMonolithicAnalyzer = new NativeConflictChecker(factory);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public void setNodeLimit(final int limit)
  {
    setMonolithicStateLimit(limit);
    setInternalStateLimit(limit);
  }

  @Override
  public int getNodeLimit()
  {
    final int limit1 = getMonolithicStateLimit();
    final int limit2 = getInternalStateLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    setMonolithicTransitionLimit(limit);
    setInternalTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    final int limit1 = getInternalTransitionLimit();
    final int limit2 = getMonolithicTransitionLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }

  @Override
  public VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
  }

  @Override
  public boolean isSatisfied()
  {
    final AnalysisResult result = getAnalysisResult();
    return result.isSatisfied();
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredDefaultMarking = marking;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mConfiguredPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mConfiguredPreconditionMarking;
  }


  //#########################################################################
  //# Configuration
  public int getInternalStateLimit()
  {
    return mInternalStateLimit;
  }

  public void setInternalStateLimit(final int limit)
  {
    mInternalStateLimit = limit;
  }

  public int getMonolithicStateLimit()
  {
    return super.getNodeLimit();
  }

  public void setMonolithicStateLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit;
  }

  public int getMonolithicTransitionLimit()
  {
    return super.getTransitionLimit();
  }

  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
  }

  /**
   * Sets whether blocked events are to be considered in abstraction.
   * @see #isBlockedEventsSupported()
   */
  public void setBlockedEventsSupported(final boolean enable)
  {
    mBlockedEventsSupported = enable;
  }

  /**
   * Returns whether blocked events are considered in abstraction.
   * Blocked events are events that are disabled in all reachable states of
   * some automaton. If supported, this will remove all transitions with
   * blocked events from the model.
   * @see #setBlockedEventsSupported(boolean) setBlockedEventsSupported()
   */
  public boolean isBlockedEventsSupported()
  {
    return mBlockedEventsSupported;
  }

  /**
   * Sets whether failing events are to be considered in abstraction.
   * @see #isFailingEventsSupported()
   */
  public void setFailingEventsSupported(final boolean enable)
  {
    mFailingEventsSupported = enable;
  }

  /**
   * Returns whether failing events are considered in abstraction.
   * Failing events are events that always lead to a dump state in some
   * automaton. If supported, this will redirect failing events in other
   * automata to dump states.
   * @see #setFailingEventsSupported(boolean) setFailingEventsSupported()
   */
  public boolean isFailingEventsSupported()
  {
    return mFailingEventsSupported;
  }

  /**
   * Sets whether selfloop-only events are to be considered in abstraction.
   * @see #isSelfloopOnlyEventsSupported()
   */
  public void setSelfloopOnlyEventsSupported(final boolean enable)
  {
    mSelfloopOnlyEventsSupported = enable;
  }

  /**
   * Returns whether selfloop-only events are considered in abstraction.
   * Selfloop-only events are events that appear only as selfloops in the
   * entire model or in all but one automaton in the model. Events that
   * are selfloop-only in the entire model can be removed, while events
   * that are selfloop-only in all but one automaton can be used to
   * simplify that automaton.
   * @see #setSelfloopOnlyEventsSupported(boolean) setSelfloopOnlyEventsSupported()
   */
  public boolean isSelfloopOnlyEventsSupported()
  {
    return mSelfloopOnlyEventsSupported;
  }

  /**
   * Sets whether always enabled events are to be considered in abstraction.
   * @see #isAlwaysEnabledEventsSupported()
   */
  public void setAlwaysEnabledEventsSupported(final boolean enable)
  {
    mAlwaysEnabledEventsSupported = enable;
  }

  /**
   * Returns whether always enabled events are considered in abstraction.
   * Always enabled events are events that are enabled in all states of the
   * entire model or of all but one automaton in the model. Always enabled
   * events can help to simplify automata.
   * @see #setAlwaysEnabledEventsSupported(boolean) setAlwaysEnabledEventsSupported()
   * @see #isControllabilityConsidered()
   */
  public boolean isAlwaysEnabledEventsSupported()
  {
    return mAlwaysEnabledEventsSupported;
  }

  /**
   * Sets whether counterexample checking is enabled.
   * If enabled, the generated counterexample is checked for correctness
   * after each step during counterexample. This is a very slow process,
   * and only recommend for testing and debugging.
   * This setting is disabled by default.
   */
  public void setTraceCheckingEnabled(final boolean checking)
  {
    mTraceCheckingEnabled = checking;
  }

  /**
   * Returns whether counterexample checking is enabled.
   * @see #setTraceCheckingEnabled(boolean) setTraceCheckingEnabled()
   */
  public boolean isTraceCheckingEnabled()
  {
    return mTraceCheckingEnabled;
  }


  //#########################################################################
  //# Hooks
  /**
   * Returns whether simplification needs to distinguish controllable and
   * uncontrollable events. If this is the case, it can affect how events
   * are encoded, and how special events are recognised. For example,
   * only uncontrollable events are ever considered as always enabled.
   * @see #isAlwaysEnabledEventsSupported()
   */
  protected boolean isControllabilityConsidered()
  {
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy model = getModel();
    if (mConfiguredDefaultMarking == null) {
      mUsedDefaultMarking = AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mConfiguredDefaultMarking;
    }
    // TODO Generalised nonblocking ...
    mTRSimplifier.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int config = mTRSimplifier.getPreferredInputConfiguration();

    final Collection<EventProxy> markings =
      Collections.singleton(mUsedDefaultMarking);
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    if (isCounterExampleEnabled()) {
      mAbstractionSequence = new ArrayList<>(4 * numAutomata);
      mCurrentAutomataMap = new HashMap<>(numAutomata);
    }
    final List<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      if (isProperAutomaton(aut)) {
        final EventEncoding eventEnc = createInitialEventEncoding(aut);
        final StateProxy dumpState = AutomatonTools.findDumpState(aut, markings);
        final TRAutomatonProxy tr =
          new TRAutomatonProxy(aut, eventEnc, dumpState, config);
        if (!hasInitialState(tr)) {
          final VerificationResult result = getAnalysisResult();
          result.setSatisfied(true);
          return;
        }
        if (isCounterExampleEnabled()) {
          final EventEncoding clonedEnc = new EventEncoding(eventEnc);
          final TRAbstractionStepInput step =
            new TRAbstractionStepInput(aut, clonedEnc, dumpState, tr);
          mAbstractionSequence.add(step);
          mCurrentAutomataMap.put(tr, step);
        }
        trs.add(tr);
      }
    }

    mBlockedEventsUsed = mBlockedEventsSupported;
    mFailingEventsUsed =
      mFailingEventsSupported && mConfiguredPreconditionMarking == null;
    mSelfloopOnlyEventsUsed = mSelfloopOnlyEventsSupported;
    mAlwaysEnabledEventsUsed =
      mAlwaysEnabledEventsSupported && mTRSimplifier.isSupportingAlwaysEnabledEvents();

    final int numEvents = model.getEvents().size();
    mSpecialEventsFinder = new SpecialEventsFinder();
    mSpecialEventsFinder.setDefaultMarkingID(DEFAULT_MARKING);
    mSpecialEventsFinder.setBlockedEventsDetected(mBlockedEventsUsed);
    mSpecialEventsFinder.setFailingEventsDetected(mFailingEventsUsed);
    mSpecialEventsFinder.setSelfloopOnlyEventsDetected(mSelfloopOnlyEventsUsed);
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(false);
    mSpecialEventsFinder.setControllabilityConsidered(isControllabilityConsidered());
    mCurrentSubsystem = new TRSubsystemInfo(trs, numEvents);
    for (final TRAutomatonProxy aut : trs) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mSpecialEventsFinder.setTransitionRelation(rel);
      mSpecialEventsFinder.run();
      final byte[] status = mSpecialEventsFinder.getComputedEventStatus();
      mCurrentSubsystem.registerEvents(aut, status);
    }
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(mAlwaysEnabledEventsUsed);

    final ProductDESProxyFactory factory = getFactory();
    final KindTranslator translator = getKindTranslator();
    mSynchronousProductBuilder = new TRSynchronousProductBuilder(factory);
    mSynchronousProductBuilder.setDetailedOutputEnabled(true);
    mSynchronousProductBuilder.setKindTranslator(translator);
    mSynchronousProductBuilder.setPruningDeadlocks(true);
    mSynchronousProductBuilder.setPruningForbiddenEvents(false);
    mSynchronousProductBuilder.setRemovingSelfloops(true);
    mSynchronousProductBuilder.setNodeLimit(mInternalStateLimit);
    mSynchronousProductBuilder.setTransitionLimit(mInternalTransitionLimit);

    mMonolithicAnalyzer.setKindTranslator(translator);
    mMonolithicAnalyzer.setNodeLimit(getMonolithicStateLimit());
    mMonolithicAnalyzer.setTransitionLimit(getMonolithicTransitionLimit());
    mMonolithicAnalyzer.setCounterExampleEnabled(isCounterExampleEnabled());

    mSubsystemQueue = new PriorityQueue<>();
    mNeedsSimplification = new SimplificationQueue(trs);
    mNeedsDisjointSubsystemsCheck = true;
    mAlwaysEnabledDetectedInitially = !mAlwaysEnabledEventsUsed;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      final Logger logger = getLogger();
      setUp();
      final AnalysisResult result = getAnalysisResult();
      if (result.isFinished()) {
        return result.isSatisfied();
      } else {
        do {
          if (logger.isDebugEnabled()) {
            final String name = mCurrentSubsystem.toString();
            if (name.length() <= 40) {
              logger.debug("Processing new subsystem " + name + " ...");
            } else {
              logger.debug("Processing new subsystem with " +
                           mCurrentSubsystem.getNumberOfAutomata() +
                           " automata ...");
            }
          }
          analyseCurrentSubsystemCompositionally();
          if (result.isFinished()) {
            computeCounterExample();
            return result.isSatisfied();
          }
          mCurrentSubsystem = mSubsystemQueue.poll();
        } while (mCurrentSubsystem != null);
        result.setSatisfied(true);
        return true;
      }
    } catch (final OutOfMemoryError error) {
      throw new OverflowException(error);
    } catch (final StackOverflowError error) {
      throw new OverflowException(error);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSynchronousProductBuilder = null;
    mSpecialEventsFinder = null;
    mUsedDefaultMarking = null;
    mAbstractionSequence = null;
    mIntermediateAbstractionSequence = null;
    mCurrentAutomataMap = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.requestAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.requestAbort();
    }
    if (mMonolithicAnalyzer != null) {
      mMonolithicAnalyzer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.resetAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.resetAbort();
    }
    if (mMonolithicAnalyzer != null) {
      mMonolithicAnalyzer.resetAbort();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean hasInitialState(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    boolean hasInit = false;
    boolean hasAlpha = !rel.isPropositionUsed(PRECONDITION_MARKING);
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      hasInit |= rel.isInitial(s);
      hasAlpha |= rel.isMarked(s, PRECONDITION_MARKING);
      if (hasInit && hasAlpha) {
        return true;
      }
    }
    return false;
  }

  private EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws OverflowException
  {
    final EventEncoding enc = new EventEncoding();
    final String name = aut.getName();
    enc.provideTauEvent(name);
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      enc.addEvent(event, translator, EventStatus.STATUS_NONE);
    }
    enc.addProposition(mUsedDefaultMarking, false);
    if (mConfiguredPreconditionMarking != null) {
      enc.addProposition(mConfiguredPreconditionMarking, false);
    }
    return enc;
  }

  private void analyseCurrentSubsystemCompositionally()
    throws AnalysisException
  {
    if (earlyTerminationCheckCurrentSubsystem()) {
      return;
    }
    while (mCurrentSubsystem.getNumberOfAutomata() >= 2) {
      checkAbort();
      final boolean simplified = simplifyAllAutomataIndividually();
      if (simplified && earlyTerminationCheckCurrentSubsystem()) {
        return;
      } else if (disjointSubsystemsCheck()) {
        return;
      } else if (mCurrentSubsystem.getNumberOfAutomata() == 2) {
        break;
      }
      final Collection<TRCandidate> candidates =
        mPreselectionHeuristic.collectCandidates(mCurrentSubsystem);
      TRCandidate candidate = mSelectionHeuristic.select(candidates);
      while (candidate != null) {
        try {
          computeSynchronousProduct(candidate);
          break;
        } catch (final OverflowException exception) {
          mPreselectionHeuristic.addOverflowCandidate(candidate);
          candidates.remove(candidate);
          candidate = mSelectionHeuristic.select(candidates);
        }
      }
      if (candidate == null) {
        break;
      } else if (earlyTerminationCheckCurrentSubsystem()) {
        return;
      }
    }
    analyseCurrentSubsystemMonolithically();
  }

  private boolean earlyTerminationCheckCurrentSubsystem()
  {
    boolean allStatesMarked = true;
    for (final TRAutomatonProxy aut : mCurrentSubsystem.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(DEFAULT_MARKING)) {
        boolean noStatesMarked = true;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s)) {
            if (rel.isMarked(s, DEFAULT_MARKING)) {
              noStatesMarked = false;
            } else {
              allStatesMarked = false;
            }
            if (!allStatesMarked && !noStatesMarked) {
              break;
            }
          }
        }
        if (noStatesMarked) {
          final AnalysisResult result = getAnalysisResult();
          result.setSatisfied(false);
          getLogger().debug("Subsystem is blocking, because " + aut.getName() +
                            " has no marked states.");
          dropSubsystem(mCurrentSubsystem);
          return true;
        }
      }
    }
    // TODO Generalised nonblocking stuff ...
    if (allStatesMarked) {
      final Logger logger = getLogger();
      logger.debug("Subsystem is nonblocking, because all states are marked.");
      dropSubsystem(mCurrentSubsystem);
    }
    return allStatesMarked;
  }

  private boolean simplifyAllAutomataIndividually()
    throws AnalysisException
  {
    boolean simplified = false;
    int remaining =
      mAlwaysEnabledDetectedInitially ? 0 : mNeedsSimplification.size();
    while (!mNeedsSimplification.isEmpty()) {
      final TRAutomatonProxy aut = mNeedsSimplification.poll();
      simplified |= simplifyAutomatonIndividually(aut);
      if (remaining > 0) {
        mAlwaysEnabledDetectedInitially = (--remaining == 0);
      }
    }
    return simplified;
  }

  private boolean simplifyAutomatonIndividually(final TRAutomatonProxy aut)
    throws AnalysisException
  {
    // Log ...
    final Logger logger = getLogger();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    if (logger.isDebugEnabled()) {
      logger.debug("Simplifying " + aut.getName() + " ...");
      rel.logSizes(logger);
    }
    // Set event status ...
    final EventEncoding enc = aut.getEventEncoding();
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      checkAbort();
      byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        final byte newStatus = info.getEventStatus(aut);
        if (newStatus != status) {
          status = newStatus;
          enc.setProperEventStatus(e, status);
        }
      }
    }
    try {
      // Set up trace computation ...
      mIntermediateAbstractionSequence =
        new IntermediateAbstractionSequence(aut);
      // Simplify ...
      final int oldNumStates = rel.getNumberOfStates();
      mSpecialEventsListener.setEnabled(true);
      mTRSimplifier.setTransitionRelation(rel);
      final boolean simplified = mTRSimplifier.run();
      if (rel.getNumberOfStates() != oldNumStates) {
        aut.resetStateNames();
      }
      // Record steps and update event status ...
      mIntermediateAbstractionSequence.commit(aut);
      if (simplified || !mAlwaysEnabledDetectedInitially) {
        final EventEncoding oldEncoding =
          mIntermediateAbstractionSequence.getInputEventEncoding();
        updateEventStatus(aut, oldEncoding);
      }
      if (simplified && isTrivialAutomaton(aut)) {
        logger.debug("Dropping trivial automaton " + aut.getName());
        dropTrivialAutomaton(aut);
        mCurrentSubsystem.removeAutomaton(aut, mNeedsSimplification);
      }
      return simplified;
    } catch (final OverflowException exception) {
      return false;
    } finally {
      mIntermediateAbstractionSequence = null;
    }
  }

  private TRAutomatonProxy computeSynchronousProduct
    (final TRCandidate candidate)
    throws AnalysisException
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Composing " + candidate + " ...");
    }
    // Set up event encoding ...
    try {
      final EventEncoding syncEncoding =
        candidate.createSyncEventEncoding(mUsedDefaultMarking,
                                          mConfiguredPreconditionMarking);
      // Synchronise ...
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des = candidate.createProductDESProxy(factory);
      mSynchronousProductBuilder.setModel(des);
      mSynchronousProductBuilder.setEventEncoding(syncEncoding);
      mSynchronousProductBuilder.run();
      final TRSynchronousProductResult result =
        mSynchronousProductBuilder.getAnalysisResult();
      final TRAutomatonProxy sync = result.getComputedAutomaton();
      mIntermediateAbstractionSequence =
        new IntermediateAbstractionSequence(sync);
      // Set up trace computation ...
      if (isCounterExampleEnabled()) {
        final List<TRAutomatonProxy> automata = candidate.getAutomata();
        final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
        final EventEncoding enc = candidate.getEventEncoding();
        final TRAbstractionStep step =
          new TRAbstractionStepSync(preds,
                                    enc,
                                    mUsedDefaultMarking,
                                    mConfiguredPreconditionMarking,
                                    factory,
                                    mSynchronousProductBuilder,
                                    result);
        mIntermediateAbstractionSequence.append(step);
      }
      // Simplify ...
      final ListBufferTransitionRelation rel = sync.getTransitionRelation();
      rel.logSizes(logger);
      mSpecialEventsListener.setEnabled(false);
      mTRSimplifier.setTransitionRelation(rel);
      mTRSimplifier.run();
      // Update trace information ...
      if (isCounterExampleEnabled()) {
        for (final TRAutomatonProxy aut : candidate.getAutomata()) {
          mCurrentAutomataMap.remove(aut);
        }
        mIntermediateAbstractionSequence.commit(sync);
      }
      // Update event status ...
      mNeedsSimplification.setCurrentComposition(candidate);
      if (isTrivialAutomaton(sync)) {
        logger.debug("Dropping trivial automaton " + sync.getName());
        dropTrivialAutomaton(sync);
      } else {
        mCurrentSubsystem.addAutomaton(sync);
      }
      final EventEncoding oldEncoding =
        mIntermediateAbstractionSequence.getInputEventEncoding();
      updateEventStatus(sync, oldEncoding);
      for (final TRAutomatonProxy aut : candidate.getAutomata()) {
        mCurrentSubsystem.removeAutomaton(aut, mNeedsSimplification);
      }
      mNeedsSimplification.setCurrentComposition(null);
      return sync;
    } finally {
      mIntermediateAbstractionSequence = null;
    }
  }

  private boolean isTrivialAutomaton(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    for (int e = EventEncoding.TAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        return false;
      }
    }
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      if (rel.isReachable(s)) {
        if (!rel.isInitial(s)) {
          return false;
        } else if (rel.isPropositionUsed(DEFAULT_MARKING) &&
                   !rel.isMarked(s, DEFAULT_MARKING)) {
          return false;
        } else if (rel.isPropositionUsed(PRECONDITION_MARKING) &&
                   !rel.isMarked(s, PRECONDITION_MARKING)) {
          return false;
        }
      }
    }
    return true;
  }

  private void updateEventStatus(final TRAutomatonProxy aut,
                                 final EventEncoding oldEncoding)
    throws AnalysisException
  {
    final EventEncoding enc = aut.getEventEncoding();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    mSpecialEventsFinder.setTransitionRelation(rel);
    mSpecialEventsFinder.run();
    final byte[] newStatus = mSpecialEventsFinder.getComputedEventStatus();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      checkAbort();
      final byte oldStatus = oldEncoding.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(oldStatus)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        info.updateAutomatonStatus(aut, newStatus[e], mNeedsSimplification);
        if (info.isEmpty()) {
          mCurrentSubsystem.removeEvent(event);
        }
        mNeedsDisjointSubsystemsCheck |=
          !EventStatus.isLocalEvent(oldStatus) &&
          !EventStatus.isUsedEvent(newStatus[e]);
      }
    }
  }

  private boolean disjointSubsystemsCheck()
  {
    if (mNeedsDisjointSubsystemsCheck) {
      mNeedsDisjointSubsystemsCheck = false;
      final List<TRSubsystemInfo> splits =
        mCurrentSubsystem.findEventDisjointSubsystems();
      if (splits == null) {
        return false;
      } else {
        mCurrentSubsystem = null;
        mSubsystemQueue.addAll(splits);
        return true;
      }
    } else {
      return false;
    }
  }

  private void analyseCurrentSubsystemMonolithically()
    throws AnalysisException
  {
    final List<TRAutomatonProxy> automata = mCurrentSubsystem.getAutomata();
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      double estimate = 1.0;
      for (final TRAutomatonProxy aut : automata) {
        final ListBufferTransitionRelation rel = aut.getTransitionRelation();
        estimate *= rel.getNumberOfReachableStates();
      }
      final String msg = String.format("Monolithically composing %d automata, " +
                                       "estimated %.0f states.",
                                       automata.size(), estimate);
      logger.debug(msg);
    }
    if (!automata.isEmpty()) {
      final String name = AutomatonTools.getCompositionName(automata);
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy(name, automata, factory);
      mMonolithicAnalyzer.setModel(des);
      final boolean satisfied = mMonolithicAnalyzer.run();
      if (satisfied) {
        logger.debug("Subsystem is nonblocking.");
        dropSubsystem(mCurrentSubsystem);
      } else {
        logger.debug("Subsystem is blocking.");
        final VerificationResult result = getAnalysisResult();
        result.setSatisfied(false);
        if (isCounterExampleEnabled()) {
          final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
          final TraceProxy trace = mMonolithicAnalyzer.getCounterExample();
          final TRAbstractionStep step =
            new TRAbstractionStepMonolithic(preds, trace);
          mAbstractionSequence.add(step);
          mCurrentAutomataMap = null;
        }
      }
    }
  }

  private List<TRAbstractionStep> getAbstractionSteps
    (final List<TRAutomatonProxy> automata)
  {
    final List<TRAbstractionStep> preds = new ArrayList<>(automata.size());
    for (final TRAutomatonProxy aut : automata) {
      final TRAbstractionStep pred = mCurrentAutomataMap.get(aut);
      preds.add(pred);
    }
    return preds;
  }

  private void dropPendingSubsystems()
  {
    for (final TRSubsystemInfo subsys : mSubsystemQueue) {
      dropSubsystem(subsys);
    }
    mSubsystemQueue.clear();
  }

  private void dropSubsystem(final TRSubsystemInfo subsys)
  {
    if (isCounterExampleEnabled()) {
      for (final TRAutomatonProxy aut : subsys.getAutomata()) {
        dropTrivialAutomaton(aut);
      }
    }
  }

  private void dropTrivialAutomaton(final TRAutomatonProxy aut)
  {
    if (isCounterExampleEnabled()) {
      final TRAbstractionStep pred = mCurrentAutomataMap.remove(aut);
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int init = rel.getFirstInitialState();
      final TRAbstractionStep step = new TRAbstractionStepDrop(pred, init);
      mAbstractionSequence.add(step);
    }
  }

  private void computeCounterExample() throws AnalysisException
  {
    final VerificationResult result = getAnalysisResult();
    if (!result.isSatisfied() && isCounterExampleEnabled()) {
      mSpecialEventsListener.setEnabled(true);
      dropPendingSubsystems();
      final ProductDESProxy des = getModel();
      final TRConflictTraceProxy trace = new TRConflictTraceProxy(des);
      final int end = mAbstractionSequence.size();
      final ListIterator<TRAbstractionStep> iter =
        mAbstractionSequence.listIterator(end);
      while (iter.hasPrevious()) {
        final TRAbstractionStep step = iter.previous();
        step.expandTrace(trace);
        checkIntermediateCounterExample(trace);
        step.dispose();
        iter.remove();
      }
      result.setCounterExample(trace);
    }
  }

  private void checkIntermediateCounterExample(final TRConflictTraceProxy trace)
    throws AnalysisException
  {
    if (mTraceCheckingEnabled) {
      final TRConflictTraceProxy cloned = new TRConflictTraceProxy(trace);
      cloned.setUpForTraceChecking();
      final KindTranslator translator = getKindTranslator();
      TraceChecker.checkConflictCounterExample(cloned,
                                               mConfiguredPreconditionMarking,
                                               mUsedDefaultMarking,
                                               true,
                                               translator);
    }
  }


  //#########################################################################
  //# Inner Class SimplificationQueue
  private static class SimplificationQueue
    extends DuplicateFreeQueue<TRAutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private SimplificationQueue(final Collection<TRAutomatonProxy> automata)
    {
      super(automata);
      mSupressed = Collections.emptySet();
    }

    //#######################################################################
    //# Interface java.util.Queue<TRAutomatonProxy>
    @Override
    public boolean offer(final TRAutomatonProxy aut)
    {
      if (mSupressed.contains(aut)) {
        return true;
      } else {
        return super.offer(aut);
      }
    }

    //#######################################################################
    //# Data Members
    private void setCurrentComposition(final TRCandidate candidate)
    {
      if (candidate == null) {
        mSupressed = Collections.emptySet();
      } else {
        mSupressed = new THashSet<>(candidate.getAutomata());
      }
    }

    //#######################################################################
    //# Data Members
    private Set<TRAutomatonProxy> mSupressed;
  }


  //#########################################################################
  //# Inner Class IntermediateAbstractionSequence
  private class IntermediateAbstractionSequence
  {
    //#######################################################################
    //# Constructor
    private IntermediateAbstractionSequence(final TRAutomatonProxy aut)
    {
      mPredecessor =
        mCurrentAutomataMap == null ? null : mCurrentAutomataMap.get(aut);
      mInputAutomaton = aut;
      mInputEventEncoding = mCurrentEventEncoding =
        new EventEncoding(aut.getEventEncoding());
      mSteps = new LinkedList<>();
    }

    //#######################################################################
    //# Access
    private TRAbstractionStep getPredecessor()
    {
      return mPredecessor;
    }

    private EventEncoding getInputEventEncoding()
    {
      return mInputEventEncoding;
    }

    private EventEncoding getCurrentEventEncoding()
    {
      return mCurrentEventEncoding;
    }

    private TRAbstractionStep getLastIntermediateStep()
    {
      return mSteps.peekLast();
    }

    private void append(final TRAbstractionStep step)
    {
      mSteps.add(step);
      mCurrentEventEncoding =
        new EventEncoding(mInputAutomaton.getEventEncoding());
    }

    private void commit(final TRAutomatonProxy result)
    {
      final TRAbstractionStep last = mSteps.peekLast();
      if (last != null && isCounterExampleEnabled()) {
        last.setOutputAutomaton(result);
        mAbstractionSequence.addAll(mSteps);
        mCurrentAutomataMap.put(result, last);
      }
    }

    //#######################################################################
    //# Data Members
    private final TRAbstractionStep mPredecessor;
    private final TRAutomatonProxy mInputAutomaton;
    private final EventEncoding mInputEventEncoding;
    private EventEncoding mCurrentEventEncoding;
    private final LinkedList<TRAbstractionStep> mSteps;
  }


  //#########################################################################
  //# Inner Class PartitioningListener
  private class PartitioningListener implements TRSimplificationListener
  {
    //#######################################################################
    //# Simple Access
    private void setEnabled(final boolean enabled)
    {
      mEnabled = enabled;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.analysis.abstraction.TRSimplificationListener
    @Override
    public boolean onSimplificationStart
      (final TransitionRelationSimplifier simplifier)
    {
      return mEnabled;
    }

    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      if (result && mIntermediateAbstractionSequence != null) {
        final TRAbstractionStep last =
          mIntermediateAbstractionSequence.getLastIntermediateStep();
        if (last != null && last instanceof TRAbstractionStepPartition) {
          final TRAbstractionStepPartition partPred =
            (TRAbstractionStepPartition) last;
          partPred.merge(simplifier);
        } else {
          final TRAbstractionStep pred = last == null ?
            mIntermediateAbstractionSequence.getPredecessor() : last;
          pred.setOutputAutomaton(null);
          final EventEncoding enc =
            mIntermediateAbstractionSequence.getCurrentEventEncoding();
          final TRAbstractionStep step =
            new TRAbstractionStepPartition(pred, enc,
                                           DEFAULT_MARKING,
                                           PRECONDITION_MARKING,
                                           simplifier);
          mIntermediateAbstractionSequence.append(step);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mEnabled = true;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;
  private int mInternalStateLimit;
  private int mInternalTransitionLimit;
  private boolean mBlockedEventsSupported;
  private boolean mFailingEventsSupported;
  private boolean mSelfloopOnlyEventsSupported;
  private boolean mAlwaysEnabledEventsSupported;
  private boolean mTraceCheckingEnabled;

  private boolean mBlockedEventsUsed;
  private boolean mFailingEventsUsed;
  private boolean mSelfloopOnlyEventsUsed;
  private boolean mAlwaysEnabledEventsUsed;

  // Tools
  private final PreselectionHeuristic mPreselectionHeuristic;
  private final SelectionHeuristic<TRCandidate> mSelectionHeuristic;
  private final TransitionRelationSimplifier mTRSimplifier;
  private final PartitioningListener mSpecialEventsListener;
  private final PartitioningListener mPartitioningListener;
  private TRSynchronousProductBuilder mSynchronousProductBuilder;
  private SpecialEventsFinder mSpecialEventsFinder;
  private final ConflictChecker mMonolithicAnalyzer;

  // Data Structures
  private EventProxy mUsedDefaultMarking;
  private List<TRAbstractionStep> mAbstractionSequence;
  private IntermediateAbstractionSequence mIntermediateAbstractionSequence;
  private Map<TRAutomatonProxy,TRAbstractionStep> mCurrentAutomataMap;
  private Queue<TRSubsystemInfo> mSubsystemQueue;
  private TRSubsystemInfo mCurrentSubsystem;
  private SimplificationQueue mNeedsSimplification;
  private boolean mNeedsDisjointSubsystemsCheck;
  private boolean mAlwaysEnabledDetectedInitially;


  //#########################################################################
  //# Class Constants
  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

}
