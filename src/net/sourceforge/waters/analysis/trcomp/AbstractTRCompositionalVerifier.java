//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   AbstractTRCompositionalVerifier
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

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.CompositionalVerificationResult;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.tr.DuplicateFreeQueue;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public abstract class AbstractTRCompositionalVerifier
  extends AbstractModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractTRCompositionalVerifier(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory,
                                         final KindTranslator translator,
                                         final ModelVerifier mono)
  {
    super(model, factory, translator);
    // TODO Make these configurable
    mPreselectionHeuristic = new PreselectionHeuristicMustL();
    final SelectionHeuristic<TRCandidate> minS = new SelectionHeuristicMinS();
    mSelectionHeuristic = new ChainSelectionHeuristic<TRCandidate>(minS);
    mSpecialEventsListener = new PartitioningListener();
    mSynchronousProductBuilder = new TRSynchronousProductBuilder(factory);
    mMonolithicAnalyzer = mono;
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
  public CompositionalVerificationResult getAnalysisResult()
  {
    return (CompositionalVerificationResult) super.getAnalysisResult();
  }

  @Override
  public CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult();
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


  //#########################################################################
  //# Configuration
  public int getInternalStateLimit()
  {
    return mInternalStateLimit;
  }

  public void setInternalStateLimit(final int limit)
  {
    mInternalStateLimit = limit > 0 ? limit : Integer.MAX_VALUE;
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
    mInternalTransitionLimit = limit > 0 ? limit : Integer.MAX_VALUE;
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
   * @see #isBlockedEventsEnabled()
   */
  public void setBlockedEventsEnabled(final boolean enable)
  {
    mBlockedEventsEnabled = enable;
  }

  /**
   * Returns whether blocked events are considered in abstraction.
   * Blocked events are events that are disabled in all reachable states of
   * some automaton. If supported, this will remove all transitions with
   * blocked events from the model.
   * @see #setBlockedEventsEnabled(boolean) setBlockedEventsEnabled()
   */
  public boolean isBlockedEventsEnabled()
  {
    return mBlockedEventsEnabled;
  }

  /**
   * Sets whether failing events are to be considered in abstraction.
   * @see #isFailingEventsEnabled()
   */
  public void setFailingEventsEnabled(final boolean enable)
  {
    mFailingEventsEnabled = enable;
  }

  /**
   * Returns whether failing events are considered in abstraction.
   * Failing events are events that always lead to a dump state in some
   * automaton. If supported, this will redirect failing events in other
   * automata to dump states.
   * @see #setFailingEventsEnabled(boolean) setFailingEventsEnabled()
   */
  public boolean isFailingEventsEnabled()
  {
    return mFailingEventsEnabled;
  }

  /**
   * Sets whether selfloop-only events are to be considered in abstraction.
   * @see #isSelfloopOnlyEventsEnabled()
   */
  public void setSelfloopOnlyEventsEnabled(final boolean enable)
  {
    mSelfloopOnlyEventsEnabled = enable;
  }

  /**
   * Returns whether selfloop-only events are considered in abstraction.
   * Selfloop-only events are events that appear only as selfloops in the
   * entire model or in all but one automaton in the model. Events that
   * are selfloop-only in the entire model can be removed, while events
   * that are selfloop-only in all but one automaton can be used to
   * simplify that automaton.
   * @see #setSelfloopOnlyEventsEnabled(boolean) setSelfloopOnlyEventsEnabled()
   */
  public boolean isSelfloopOnlyEventsEnabled()
  {
    return mSelfloopOnlyEventsEnabled;
  }

  /**
   * Sets whether always enabled events are to be considered in abstraction.
   * @see #isAlwaysEnabledEventsEnabled()
   */
  public void setAlwaysEnabledEventsEnabled(final boolean enable)
  {
    mAlwaysEnabledEventsEnabled = enable;
  }

  /**
   * Returns whether always enabled events are considered in abstraction.
   * Always enabled events are events that are enabled in all states of the
   * entire model or of all but one automaton in the model. Always enabled
   * events can help to simplify automata.
   * @see #setAlwaysEnabledEventsEnabled(boolean) setAlwaysEnabledEventsEnabled()
   * @see #isControllabilityConsidered()
   */
  public boolean isAlwaysEnabledEventsEnabled()
  {
    return mAlwaysEnabledEventsEnabled;
  }

  /**
   * Sets whether deadlock states are pruned in synchronous products.
   * @see TRSynchronousProductBuilder#setPruningDeadlocks(boolean)
   */
  public void setPruningDeadlocks(final boolean pruning)
  {
    mSynchronousProductBuilder.setPruningDeadlocks(pruning);
  }

  /**
   * Returns whether deadlock states are pruned.
   * @see #setPruningDeadlocks(boolean) setPruningDeadlocks()
   */
  public boolean isPruningDeadlocks()
  {
    return mSynchronousProductBuilder.getPruningDeadlocks();
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


  public void setSimplifier(final TransitionRelationSimplifier simplifier)
  {
    mTRSimplifier = simplifier;
  }

  public TransitionRelationSimplifier getSimplifier()
  {
    return mTRSimplifier;
  }

  public void setMonolithicAnalyzer(final ModelAnalyzer mono)
  {
    mMonolithicAnalyzer = mono;
  }

  public ModelAnalyzer getMonolithicAnalyzer()
  {
    return mMonolithicAnalyzer;
  }

  public ModelVerifier getMonolithicVerifier()
  {
    return (ModelVerifier) getMonolithicAnalyzer();
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
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();

    final CompositionalAnalysisResult result = getAnalysisResult();
    if (isBlockedEventsUsed()) {
      result.addBlockedEvents(0);
    }
    if (isFailingEventsUsed()) {
      result.addFailingEvents(0);
    }
    if (isSelfloopOnlyEventsUsed()) {
      result.addSelfloopOnlyEvents(0);
    }
    if (isAlwaysEnabledEventsUsed()) {
      result.addAlwaysEnabledEvents(0);
    }
    mTRSimplifier.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mSpecialEventsFinder = new SpecialEventsFinder();
    mSpecialEventsFinder.setAppliesPartitionAutomatically(false);
    mSpecialEventsFinder.setDefaultMarkingID(DEFAULT_MARKING);

    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final int numEvents = model.getEvents().size();
    if (isCounterExampleEnabled()) {
      mAbstractionSequence = new ArrayList<>(4 * numAutomata);
      mCurrentAutomataMap = new HashMap<>(numAutomata);
    }
    mCurrentSubsystem = new TRSubsystemInfo(numAutomata, numEvents);
    final int config = mTRSimplifier.getPreferredInputConfiguration();
    final List<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final TRAutomatonProxy tr = createInitialAutomaton(aut, config);
      if (tr != null) {
        trs.add(tr);
      } else if (result.isFinished()) {
        return;
      }
    }
    mCurrentSubsystem.addAutomata(trs);

    mSpecialEventsFinder.setBlockedEventsDetected(isBlockedEventsUsed());
    mSpecialEventsFinder.setFailingEventsDetected(isFailingEventsUsed());
    mSpecialEventsFinder.setSelfloopOnlyEventsDetected(isSelfloopOnlyEventsUsed());
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(false);
    mSpecialEventsFinder.setControllabilityConsidered(isControllabilityConsidered());
    for (final TRAutomatonProxy aut : trs) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mSpecialEventsFinder.setTransitionRelation(rel);
      mSpecialEventsFinder.run();
      final byte[] status = mSpecialEventsFinder.getComputedEventStatus();
      mCurrentSubsystem.registerEvents(aut, status);
    }
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(isAlwaysEnabledEventsUsed());

    final KindTranslator translator = getKindTranslator();
    mSynchronousProductBuilder.setDetailedOutputEnabled(true);
    mSynchronousProductBuilder.setKindTranslator(translator);
    mSynchronousProductBuilder.setPruningDeadlocks(true);
    mSynchronousProductBuilder.setRemovingSelfloops(true);
    mSynchronousProductBuilder.setNodeLimit(mInternalStateLimit);
    mSynchronousProductBuilder.setTransitionLimit(mInternalTransitionLimit);

    final ModelVerifier mono = getMonolithicVerifier();
    mono.setKindTranslator(translator);
    mono.setNodeLimit(getMonolithicStateLimit());
    mono.setTransitionLimit(getMonolithicTransitionLimit());
    mono.setCounterExampleEnabled(isCounterExampleEnabled());

    mSubsystemQueue = new PriorityQueue<>();
    mNeedsSimplification = new SimplificationQueue(trs);
    mNeedsDisjointSubsystemsCheck = true;
    mAlwaysEnabledDetectedInitially = !isAlwaysEnabledEventsUsed();

    result.addSimplifierStatistics(mSpecialEventsFinder);
    result.addSimplifierStatistics(mTRSimplifier);
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
        return setSatisfiedResult();
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
    mSpecialEventsFinder = null;
    mUsedPropositions = null;
    mAbstractionSequence = null;
    mIntermediateAbstractionSequence = null;
    mCurrentAutomataMap = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Hooks
  protected ChainTRSimplifier createDefaultAbstractionChain()
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final SpecialEventsTRSimplifier special = new SpecialEventsTRSimplifier();
    special.setSimplificationListener(mSpecialEventsListener);
    chain.add(special);
    return chain;
  }

  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    return null;
  }

  protected EventProxy getUsedPreconditionMarking()
  {
    return null;
  }

  protected Collection<EventProxy> getUsedPropositions()
    throws EventNotFoundException
  {
    if (mUsedPropositions == null) {
      final EventProxy defaultMarking = getUsedDefaultMarking();
      if (defaultMarking == null) {
        return mUsedPropositions = Collections.emptyList();
      }
      final EventProxy preconditionMarking = getUsedPreconditionMarking();
      if (preconditionMarking == null) {
        return mUsedPropositions =Collections.singletonList(defaultMarking);
      }
      mUsedPropositions = new ArrayList<>(2);
      mUsedPropositions.add(defaultMarking);
      mUsedPropositions.add(preconditionMarking);
    }
    return mUsedPropositions;
  }

  /**
   * Returns whether simplification needs to distinguish controllable and
   * uncontrollable events. If this is the case, it can affect how events
   * are encoded, and how special events are recognised. For example,
   * only uncontrollable events are ever considered as always enabled.
   * @see #isAlwaysEnabledEventsEnabled()
   */
  protected boolean isControllabilityConsidered()
  {
    return false;
  }

  protected boolean isBlockedEventsUsed()
  {
    return isBlockedEventsEnabled();
  }

  protected boolean isFailingEventsUsed()
  {
    return isFailingEventsEnabled() && getUsedPreconditionMarking() == null;
  }

  protected boolean isSelfloopOnlyEventsUsed()
  {
    return isSelfloopOnlyEventsEnabled();
  }

  protected boolean isAlwaysEnabledEventsUsed()
  {
    return
      isAlwaysEnabledEventsEnabled() &&
      mTRSimplifier.isAlwaysEnabledEventsSupported();
  }

  /**
   * Returns the minimum number of automata that can be retained in
   * a subsystem prior to monolithic analysis. The default for this
   * is 2, to avoid the expensive minimisation of a final automaton
   * prior to verification.
   */
  protected int getMonolithicAutomataLimit()
  {
    return 2;
  }

  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    return false;
  }


  //#########################################################################
  //# Specific Access
  protected SpecialEventsFinder getSpecialEventsFinder()
  {
    return mSpecialEventsFinder;
  }

  protected TRSubsystemInfo getCurrentSubsystem()
  {
    return mCurrentSubsystem;
  }


  //#########################################################################
  //# Algorithm
  private void analyseCurrentSubsystemCompositionally()
    throws AnalysisException
  {
    if (earlyTerminationCheck(mCurrentSubsystem)) {
      return;
    }
    final int limit = getMonolithicAutomataLimit();
    while (mCurrentSubsystem.getNumberOfAutomata() >= limit) {
      checkAbort();
      final boolean simplified = simplifyAllAutomataIndividually();
      if (simplified && earlyTerminationCheck(mCurrentSubsystem)) {
        return;
      } else if (disjointSubsystemsCheck()) {
        return;
      } else if (mCurrentSubsystem.getNumberOfAutomata() == limit) {
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
      } else if (earlyTerminationCheck(mCurrentSubsystem)) {
        return;
      }
    }
    analyseCurrentSubsystemMonolithically();
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
    recordStatistics(aut);
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
    countSpecialEvents(enc);
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
      mNeedsSimplification.setSuppressed(aut);
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
      recordUnsuccessfulComposition();
      return false;
    } finally {
      mNeedsSimplification.clearSuppressed();
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
      // TODO Event ordering ...
      final EventEncoding candidateEncoding = candidate.getEventEncoding();
      countSpecialEvents(candidateEncoding);
      final Collection<EventProxy> props = getUsedPropositions();
      final EventEncoding syncEncoding = candidate.createSyncEventEncoding(props);
      // Synchronise ...
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des = candidate.createProductDESProxy(factory);
      mSynchronousProductBuilder.setModel(des);
      mSynchronousProductBuilder.setEventEncoding(syncEncoding);
      mSynchronousProductBuilder.run();
      final TRSynchronousProductResult syncResult =
        mSynchronousProductBuilder.getAnalysisResult();
      final CompositionalAnalysisResult combinedResult = getAnalysisResult();
      combinedResult.addSynchronousProductAnalysisResult(syncResult);
      final TRAutomatonProxy sync = syncResult.getComputedAutomaton();
      recordStatistics(sync);
      mIntermediateAbstractionSequence =
        new IntermediateAbstractionSequence(sync);
      // Set up trace computation ...
      if (isCounterExampleEnabled()) {
        final List<TRAutomatonProxy> automata = candidate.getAutomata();
        final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
        final EventEncoding enc = candidate.getEventEncoding();
        final TRAbstractionStep step =
          new TRAbstractionStepSync(preds, enc, props, factory,
                                    mSynchronousProductBuilder, syncResult);
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
      mNeedsSimplification.setSuppressed(candidate, sync);
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
      return sync;
    } catch (final OverflowException exception) {
      // BUG May or may not have been counted by recordStatistics() already?
      recordUnsuccessfulComposition();
      throw exception;
    } finally {
      mNeedsSimplification.clearSuppressed();
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
      final long start = System.currentTimeMillis();
      mNeedsDisjointSubsystemsCheck = false;
      final List<TRSubsystemInfo> splits =
        mCurrentSubsystem.findEventDisjointSubsystems();
      final boolean splitSuccess = splits != null;
      if (splitSuccess) {
        mCurrentSubsystem = null;
        mSubsystemQueue.addAll(splits);
      }
      final long stop = System.currentTimeMillis();
      final CompositionalAnalysisResult result = getAnalysisResult();
      result.addSplitAttempt(splitSuccess, stop - start);
      return splitSuccess;
    } else {
      return false;
    }
  }

  private void analyseCurrentSubsystemMonolithically()
    throws AnalysisException
  {
    final ProductDESProxy des = createSubsystemDES(mCurrentSubsystem);
    if (des != null) {
      final Collection<AutomatonProxy> automata = des.getAutomata();
      final int numAutomata = automata.size();
      final List<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        trs.add(tr);
      }
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        double estimate = 1.0;
        for (final TRAutomatonProxy tr : trs) {
          final ListBufferTransitionRelation rel = tr.getTransitionRelation();
          estimate *= rel.getNumberOfReachableStates();
        }
        final String msg = String.format("Monolithically composing %d automata, " +
                                         "estimated %.0f states.",
                                         automata.size(), estimate);
        logger.debug(msg);
      }
      final ModelVerifier mono = getMonolithicVerifier();
      mono.setModel(des);
      mono.run();
      final AnalysisResult monolithicResult = mono.getAnalysisResult();
      final CompositionalAnalysisResult combinedResult = getAnalysisResult();
      combinedResult.addMonolithicAnalysisResult(monolithicResult);
      if (monolithicResult.isSatisfied()) {
        logger.debug("Subsystem is nonblocking.");
        dropSubsystem(mCurrentSubsystem);
      } else {
        logger.debug("Subsystem is blocking.");
        combinedResult.setSatisfied(false);
        if (isCounterExampleEnabled()) {
          dropPendingSubsystems();
          final List<TRAbstractionStep> preds = getAbstractionSteps(trs);
          final TraceProxy trace = mono.getCounterExample();
          final TRAbstractionStep step =
            new TRAbstractionStepMonolithic(preds, trace);
          mAbstractionSequence.add(step);
        }
      }
    }
  }

  protected ProductDESProxy createSubsystemDES(final TRSubsystemInfo subsys)
  {
    final List<TRAutomatonProxy> automata = subsys.getAutomata();
    if (automata.isEmpty()) {
      return null;
    } else {
      final String name = AutomatonTools.getCompositionName(automata);
      final ProductDESProxyFactory factory = getFactory();
      return AutomatonTools.createProductDESProxy(name, automata, factory);
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

  private void computeCounterExample() throws AnalysisException
  {
    final VerificationResult result = getAnalysisResult();
    if (!result.isSatisfied() && isCounterExampleEnabled()) {
      mSpecialEventsListener.setEnabled(true);
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
                                               getUsedPreconditionMarking(),
                                               getUsedDefaultMarking(),
                                               true,
                                               translator);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut,
                                                    final int config)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getComponentKind(aut)) {
    case PLANT:
    case SPEC:
      final EventEncoding eventEnc = createInitialEventEncoding(aut);
      final Collection<EventProxy> markings = getUsedPropositions();
      final StateProxy dumpState = AutomatonTools.findDumpState(aut, markings);
      final TRAutomatonProxy tr =
        new TRAutomatonProxy(aut, eventEnc, dumpState, config);
      if (!hasInitialState(tr)) {
        final Logger logger = getLogger();
        logger.debug("Terminating early as automaton " + aut.getName() +
                     " has no initial state.");
        setSatisfiedResult();
        return null;
      }
      if (isCounterExampleEnabled()) {
        final EventEncoding clonedEnc = new EventEncoding(eventEnc);
        final TRAbstractionStepInput step =
          new TRAbstractionStepInput(aut, clonedEnc, dumpState, tr);
        mAbstractionSequence.add(step);
        mCurrentAutomataMap.put(tr, step);
      }
      return tr;
    default:
      return null;
    }
  }

  protected EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws AnalysisException
  {
    final EventEncoding enc = new EventEncoding();
    final String name = aut.getName();
    enc.provideTauEvent(name);
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      enc.addEvent(event, translator, EventStatus.STATUS_NONE);
    }
    for (final EventProxy prop : getUsedPropositions()) {
      enc.addProposition(prop, false);
    }
    return enc;
  }

  protected boolean hasInitialState(final TRAutomatonProxy aut)
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


  private void dropPendingSubsystems()
  {
    for (final TRSubsystemInfo subsys : mSubsystemQueue) {
      dropSubsystem(subsys);
    }
    mSubsystemQueue.clear();
  }

  protected void dropSubsystem(final TRSubsystemInfo subsys)
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


  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied and marks the run as completed.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
  }


  //#########################################################################
  //# Statistics
  void countSpecialEvents(final EventEncoding enc)
  {
    final CompositionalAnalysisResult stats = getAnalysisResult();
    final Logger logger = getLogger();
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        info.countSpecialEvents(status, stats, logger);
      }
    }
  }

  void recordStatistics(final TRAutomatonProxy aut)
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addCompositionAttempt();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numStates = rel.getNumberOfReachableStates();
    result.updateNumberOfStates(numStates);
    final int numTrans = rel.getNumberOfTransitions();
    result.updateNumberOfTransitions(numTrans);
    result.updatePeakMemoryUsage();
  }

  void recordUnsuccessfulComposition()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }


  //#########################################################################
  //# Inner Class PartitioningListener
  class PartitioningListener implements TRSimplificationListener
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
      if (result && isCounterExampleEnabled() &&
          mIntermediateAbstractionSequence != null) {
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
    //# Suppressing Automata Currently Being Modified
    private void setSuppressed(final TRAutomatonProxy aut)
    {
      mSupressed = Collections.singleton(aut);
    }

    private void setSuppressed(final TRCandidate candidate,
                               final TRAutomatonProxy aut)
    {
      final Collection<TRAutomatonProxy> automata = candidate.getAutomata();
      final int size = automata.size() + 1;
      if (size <= 4) {
        mSupressed = new ArrayList<>(size);
      } else {
        mSupressed = new THashSet<>(size);
      }
      mSupressed.addAll(automata);
      mSupressed.add(aut);
    }

    private void clearSuppressed()
    {
      mSupressed = Collections.emptySet();
    }

    //#######################################################################
    //# Data Members
    private Collection<TRAutomatonProxy> mSupressed;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private int mInternalStateLimit = Integer.MAX_VALUE;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;
  private boolean mBlockedEventsEnabled;
  private boolean mFailingEventsEnabled;
  private boolean mSelfloopOnlyEventsEnabled;
  private boolean mAlwaysEnabledEventsEnabled;
  private boolean mTraceCheckingEnabled;

  private Collection<EventProxy> mUsedPropositions;

  // Tools
  private final PreselectionHeuristic mPreselectionHeuristic;
  private final SelectionHeuristic<TRCandidate> mSelectionHeuristic;
  private final PartitioningListener mSpecialEventsListener;
  private TransitionRelationSimplifier mTRSimplifier;
  private final TRSynchronousProductBuilder mSynchronousProductBuilder;
  private SpecialEventsFinder mSpecialEventsFinder;
  private ModelAnalyzer mMonolithicAnalyzer;

  // Data Structures
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
