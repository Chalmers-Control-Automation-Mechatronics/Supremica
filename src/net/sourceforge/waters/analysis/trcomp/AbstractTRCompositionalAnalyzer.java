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
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
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
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.log4j.Logger;


/**
 * <P>A general compositional model analyser to be subclassed for different
 * algorithms.</P>
 *
 * <P>This model analyser implements compositional minimisation
 * of the input model, and leaves it to the subclasses to decide what is
 * to be done with the minimisation result. This implementation is based
 * on transition relations ({@link TRAutomatonProxy}). It provides full
 * support for the special event types of <I>blocked</I>, <I>failing</I>,
 * <I>selfloop-only</I>, and <I>always enabled</I> events. It can be
 * configured to use different abstraction sequences and candidate selection
 * heuristics.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractTRCompositionalAnalyzer
  extends AbstractModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractTRCompositionalAnalyzer(final ProductDESProxy model,
                                         final KindTranslator translator,
                                         final ModelVerifier mono)
  {
    super(model, ProductDESElementFactory.getInstance(), translator);
    mTRSimplifierCreator = getTRSimplifierFactory().getDefaultValue();
    mPreselectionHeuristic = getPreselectionHeuristicFactory().getDefaultValue();
    final SelectionHeuristic<TRCandidate> heu =
      getSelectionHeuristicFactory().getDefaultValue();
    mSelectionHeuristic = heu.createDecisiveHeuristic();
    mSpecialEventsListener = new PartitioningListener();
    mSynchronousProductBuilder = new TRSynchronousProductBuilder();
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
  /**
   * Gets the factory to obtain transition relation simplifiers for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setSimplifierCreator(TRToolCreator)
   * setSimplifierCreator()} method.
   */
  public abstract EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactory();

  /**
   * Sets the tool that creates the transition relation simplifier that
   * defines the abstraction chain. Possible arguments for this method
   * can be obtained from the factory returned by {@link
   * #getTRSimplifierFactory()}.
   */
  public void setSimplifierCreator
    (final TRToolCreator<TransitionRelationSimplifier> creator)
  {
    mTRSimplifierCreator = creator;
  }

  /**
   * Gets the tool that creates the transition relation simplifier that
   * defines the abstraction chain.
   */
  public TRToolCreator<TransitionRelationSimplifier> getSimplifierCreator()
  {
    return mTRSimplifierCreator;
  }

  /**
   * Gets the factory to obtain preselection heuristics for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setPreselectionHeuristic(TRPreselectionHeuristic)
   * setPreselectionHeuristic()} method.
   */
  public EnumFactory<TRPreselectionHeuristic> getPreselectionHeuristicFactory()
  {
    return
      new ListedEnumFactory<TRPreselectionHeuristic>() {
      {
        register(PRESEL_MustL);
        register(PRESEL_MustSp);
        register(PRESEL_Pairs);
        register(PRESEL_MaxS);
        register(PRESEL_MinT);
        register(PRESEL_MaxT);
        register(PRESEL_MinS);
      }
    };
  }

  /**
   * Sets the selection heuristic to choose a candidates for composition from
   * the collection returned by the preselection heuristic. Possible arguments
   * for this method can be obtained from the factory returned by {@link
   * #getSelectionHeuristicFactory()}.
   */
  public void setSelectionHeuristic(final SelectionHeuristic<TRCandidate> heu)
  {
    mSelectionHeuristic = heu.createDecisiveHeuristic();
  }

  /**
   * Gets the selection heuristic used to choose a candidates for composition
   * from the collection returned by the preselection heuristic.
   */
  public SelectionHeuristic<TRCandidate> getSelectionHeuristic()
  {
    return mSelectionHeuristic;
  }

  /**
   * Gets the factory to obtain preselection heuristics for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setPreselectionHeuristic(TRPreselectionHeuristic)
   * setPreselectionHeuristic()} method.
   */
  public EnumFactory<SelectionHeuristic<TRCandidate>> getSelectionHeuristicFactory()
  {
    return
      new ListedEnumFactory<SelectionHeuristic<TRCandidate>>() {
      {
        register(SEL_MinS);
        register(SEL_MinSSp);
        register(SEL_MaxC);
        register(SEL_MaxL);
        register(SEL_MinE);
        register(SEL_MinF);
      }
    };
  }

  /**
   * Sets the preselection heuristic to create the possible candidates for
   * composition. Possible arguments for this method can be obtained from the
   * factory returned by {@link #getPreselectionHeuristicFactory()}.
   */
  public void setPreselectionHeuristic(final TRPreselectionHeuristic heu)
  {
    mPreselectionHeuristic = heu;
  }

  /**
   * Gets the preselection heuristic used to create the possible candidates
   * for composition.
   */
  public TRPreselectionHeuristic getPreselectionHeuristic()
  {
    return mPreselectionHeuristic;
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

  /**
   * Sets state and event encodings are to be preserved when copying
   * input automata of type {@link TRAutomatonProxy}. If set, the input
   * automata will be used with the exact same encoding, which has to
   * compatible with the expectations of the abstraction procedures.
   * Otherwise, the encoding may change, resulting in counterexamples
   * with possibly different encoding.
   */
  public void setPreservingEncodings(final boolean preserved)
  {
    mPreservingEncodings = preserved;
  }

  /**
   * Returns whether state and event encodings are to be preserved when
   * copying input automata of type {@link TRAutomatonProxy}.
   * @see #setTraceCheckingEnabled(boolean) setTraceCheckingEnabled()
   */
  public boolean getPreservingEncodings()
  {
    return mPreservingEncodings;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mPreselectionHeuristic != null) {
      mPreselectionHeuristic.requestAbort();
    }
    if (mSelectionHeuristic != null) {
      mSelectionHeuristic.requestAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.requestAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.requestAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
    if (mMonolithicAnalyzer != null) {
      mMonolithicAnalyzer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mPreselectionHeuristic != null) {
      mPreselectionHeuristic.resetAbort();
    }
    if (mSelectionHeuristic != null) {
      mSelectionHeuristic.resetAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.resetAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.resetAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
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

    mTRSimplifier = mTRSimplifierCreator.create(this);
    mTRSimplifier.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mPreselectionHeuristic.setContext(this);
    mSelectionHeuristic.setContext(this);
    mSpecialEventsFinder = new SpecialEventsFinder();
    mSpecialEventsFinder.setAppliesPartitionAutomatically(false);
    mSpecialEventsFinder.setDefaultMarkingID(DEFAULT_MARKING);

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

    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final int numEvents = model.getEvents().size();
    if (isCounterExampleEnabled()) {
      mAbstractionSequence = new ArrayList<>(4 * numAutomata);
      mCurrentAutomataMap = new HashMap<>(numAutomata);
    }
    mCurrentSubsystem = new TRSubsystemInfo(numAutomata, numEvents);
    final List<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final TRAutomatonProxy tr = createInitialAutomaton(aut);
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
        computeCounterExample();
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
        if (analyseSubsystemMonolithically(null)) {
          return setSatisfiedResult();
        } else {
          assert result.isFinished() && !result.isSatisfied() :
            "Failed analysis result not recorded correctly!";
          computeCounterExample();
          return false;
        }
      }
    } catch (final AnalysisException exception) {
      setExceptionResult(exception);
      throw exception;
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mTRSimplifier = null;
    mSpecialEventsFinder = null;
    mAbstractionSequence = null;
    mIntermediateAbstractionSequence = null;
    mCurrentAutomataMap = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Hooks
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    return null;
  }

  protected EventProxy getUsedPreconditionMarking()
  {
    return null;
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
    return isFailingEventsEnabled();
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
    throws AnalysisException
  {
    return false;
  }


  //#########################################################################
  //# Specific Access
  protected TransitionRelationSimplifier getSimplifier()
  {
    return mTRSimplifier;
  }

  protected SpecialEventsFinder getSpecialEventsFinder()
  {
    return mSpecialEventsFinder;
  }

  protected TRSubsystemInfo getCurrentSubsystem()
  {
    return mCurrentSubsystem;
  }

  protected Collection<TRSubsystemInfo> getPendingSubsystems()
  {
    return mSubsystemQueue;
  }

  protected IntermediateAbstractionSequence getIntermediateAbstractionSequence()
  {
    return mIntermediateAbstractionSequence;
  }

  protected int getPreferredInputConfiguration()
  {
    if (mTRSimplifier != null) {
      return mTRSimplifier.getPreferredInputConfiguration();
    } else {
      final TransitionRelationSimplifier simplifier =
        mTRSimplifierCreator.create(this);
      return simplifier.getPreferredInputConfiguration();
    }
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
    analyseSubsystemMonolithically(mCurrentSubsystem);
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
      final byte oldStatus = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(oldStatus)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        final byte newStatus = info.getEventStatus(aut);
        if (newStatus != oldStatus) {
          enc.setProperEventStatus(e, newStatus);
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
      recordUnsuccessfulComposition(exception);
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
    try {
      // Set up event encoding ...
      final EventEncoding candidateEncoding = candidate.getEventEncoding();
      countSpecialEvents(candidateEncoding);
      addAuxiliaryEvents(candidateEncoding);
      sortCandidateEvents(candidateEncoding);
      final EventEncoding syncEncoding = candidate.createSyncEventEncoding();
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
          new TRAbstractionStepSync(preds, enc, factory,
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
      updateEventStatus(sync, syncEncoding);
      for (final TRAutomatonProxy aut : candidate.getAutomata()) {
        mCurrentSubsystem.removeAutomaton(aut, mNeedsSimplification);
      }
      return sync;
    } catch (final OverflowException exception) {
      // BUG May or may not have been counted by recordStatistics() already?
      recordUnsuccessfulComposition(exception);
      throw exception;
    } finally {
      mNeedsSimplification.clearSuppressed();
      mIntermediateAbstractionSequence = null;
    }
  }

  private void sortCandidateEvents(final EventEncoding enc)
  {
    final byte pattern =
      EventStatus.STATUS_FAILING | EventStatus.STATUS_ALWAYS_ENABLED;
    final byte mask = (byte) (pattern | EventStatus.STATUS_UNUSED);
    final int numEvents = enc.getNumberOfProperEvents();
    boolean hasStronglyFailingEvent = false;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = enc.getProperEventStatus(e);
      if ((status & mask) == pattern) {
        hasStronglyFailingEvent = true;
        break;
      }
    }
    if (hasStronglyFailingEvent) {
      enc.sortProperEvents(~EventStatus.STATUS_ALWAYS_ENABLED,
                           ~EventStatus.STATUS_FAILING,
                           ~EventStatus.STATUS_LOCAL,
                           EventStatus.STATUS_CONTROLLABLE);
    } else {
      enc.sortProperEvents(~EventStatus.STATUS_LOCAL,
                           ~EventStatus.STATUS_ALWAYS_ENABLED,
                           EventStatus.STATUS_CONTROLLABLE);
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

  abstract protected boolean analyseSubsystemMonolithically
    (final TRSubsystemInfo subsys)
    throws AnalysisException;


  protected TRAbstractionStep getAbstractionStep(final TRAutomatonProxy aut)
  {
    return mCurrentAutomataMap.get(aut);
  }

  protected List<TRAbstractionStep> getAbstractionSteps
    (final List<TRAutomatonProxy> automata)
  {
    final List<TRAbstractionStep> preds = new ArrayList<>(automata.size());
    for (final TRAutomatonProxy aut : automata) {
      final TRAbstractionStep pred = mCurrentAutomataMap.get(aut);
      preds.add(pred);
    }
    return preds;
  }

  protected TRTraceProxy computeCounterExample() throws AnalysisException
  {
    final VerificationResult result = getAnalysisResult();
    if (!result.isSatisfied() && isCounterExampleEnabled()) {
      final Logger logger = getLogger();
      logger.debug("Starting trace expansion ...");
      mSpecialEventsListener.setEnabled(true);
      final ProductDESProxy des = getModel();
      final TRTraceProxy trace = createEmptyTrace(des);
      final int end = mAbstractionSequence.size();
      final ListIterator<TRAbstractionStep> iter =
        mAbstractionSequence.listIterator(end);
      while (iter.hasPrevious()) {
        checkAbort();
        final TRAbstractionStep step = iter.previous();
        step.reportExpansion();
        step.expandTrace(trace, this);
        if (mTraceCheckingEnabled) {
          checkIntermediateCounterExample(trace);
        }
        iter.remove();
      }
      result.setCounterExample(trace);
      return trace;
    } else {
      return null;
    }
  }

  protected abstract TRTraceProxy createEmptyTrace(ProductDESProxy des);

  protected void checkIntermediateCounterExample(final TRTraceProxy trace)
    throws AnalysisException
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  protected EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws AnalysisException
  {
    final EventEncoding enc = new EventEncoding();
    addAuxiliaryEvents(enc);
    final String name = aut.getName();
    enc.provideTauEvent(name);
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      switch (translator.getEventKind(event)) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        enc.addEvent(event, translator, EventStatus.STATUS_NONE);
        break;
      case PROPOSITION:
        final int p = enc.getEventCode(event);
        if (p >= 0) {
          enc.setPropositionUsed(p, true);
        }
        break;
      default:
        throw new IllegalArgumentException
          ("Unknown event kind " + translator.getEventKind(event) + "!");
      }
    }
    enc.sortProperEvents(EventStatus.STATUS_UNUSED,
                         EventStatus.STATUS_CONTROLLABLE);
    return enc;
  }

  protected void addAuxiliaryEvents(final EventEncoding enc)
    throws AnalysisException
  {
  }

  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getComponentKind(aut)) {
    case PLANT:
    case SPEC:
      final TRAbstractionStepInput step;
      if (mPreservingEncodings && aut instanceof TRAutomatonProxy) {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        step = new TRAbstractionStepInput(tr);
      } else {
        final EventEncoding eventEnc = createInitialEventEncoding(aut);
        final StateProxy dumpState = findDumpState(aut);
        step = new TRAbstractionStepInput(aut, eventEnc, dumpState);
      }
      final int config = mTRSimplifier.getPreferredInputConfiguration();
      final TRAutomatonProxy created = step.createOutputAutomaton(config);
      if (!hasInitialState(created)) {
        final Logger logger = getLogger();
        logger.debug("Terminating early as automaton " + aut.getName() +
                     " has no initial state.");
        setSatisfiedResult();
        return null;
      }
      if (isCounterExampleEnabled()) {
        addAbstractionStep(step, created);
      }
      return created;
    default:
      return null;
    }
  }

  protected StateProxy findDumpState(final AutomatonProxy aut)
    throws EventNotFoundException
  {
    return null;
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


  protected void dropPendingSubsystems()
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

  protected void dropTrivialAutomaton(final TRAutomatonProxy aut)
  {
    if (isCounterExampleEnabled()) {
      final TRAbstractionStep pred = mCurrentAutomataMap.remove(aut);
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int init = rel.getFirstInitialState();
      final TRAbstractionStep step = new TRAbstractionStepDrop(pred, init);
      addAbstractionStep(step);
    }
  }

  protected void addAbstractionStep(final TRAbstractionStep step,
                                    final TRAutomatonProxy outputAut)
  {
    addAbstractionStep(step);
    mCurrentAutomataMap.put(outputAut, step);
  }

  protected void addAbstractionStep(final TRAbstractionStep step)
  {
    mAbstractionSequence.add(step);
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
  //# Statistics and Logging
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

  void recordUnsuccessfulComposition(final OverflowException exception)
  {
    getLogger().debug(exception.getMessage());
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }

  void recordMonolithicAttempt(final List<TRAutomatonProxy> automata)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      double estimate = 1.0;
      for (final TRAutomatonProxy tr : automata) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        estimate *= rel.getNumberOfReachableStates();
      }
      final String code = estimate >= 1e7 ? "%e" : "%.0f";
      final String msg = String.format
        ("Monolithically composing %d automata, estimated " +
         code + " states.", automata.size(), estimate);
      logger.debug(msg);
    }
  }


  //#########################################################################
  //# Abstraction Chains
  /**
   * <P>The abstraction sequence that consists of only observation
   * equivalence. This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> OEQ =
    new TRToolCreator<TransitionRelationSimplifier>("OEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      return analyzer.createObservationEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE);
    }
  };

  /**
   * <P>The abstraction sequence that consists of only weak observation
   * equivalence. This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> WOEQ =
    new TRToolCreator<TransitionRelationSimplifier>("WOEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      return analyzer.createObservationEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE);
    }
  };


  protected ChainTRSimplifier createObservationEquivalenceChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final ChainTRSimplifier chain = startAbstractionChain();
    final TRSimplificationListener listener =
      new PartitioningListener();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(listener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    final int precond =
      getUsedPreconditionMarking() == null ? -1 : PRECONDITION_MARKING;
    chain.setPropositions(precond, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    return chain;
  }

  protected ChainTRSimplifier startAbstractionChain()
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final SpecialEventsTRSimplifier special = new SpecialEventsTRSimplifier();
    special.setSimplificationListener(mSpecialEventsListener);
    chain.add(special);
    return chain;
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
          final EventEncoding enc =
            mIntermediateAbstractionSequence.getCurrentEventEncoding();
          final TRAbstractionStep step =
            new TRAbstractionStepPartition(pred, enc, simplifier);
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
  class IntermediateAbstractionSequence
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
    TRAbstractionStep getPredecessor()
    {
      return mPredecessor;
    }

    EventEncoding getInputEventEncoding()
    {
      return mInputEventEncoding;
    }

    EventEncoding getCurrentEventEncoding()
    {
      return mCurrentEventEncoding;
    }

    TRAbstractionStep getLastIntermediateStep()
    {
      return mSteps.peekLast();
    }

    TRAbstractionStep getLastIntermediateStepOrPredecessor()
    {
      final TRAbstractionStep last = getLastIntermediateStep();
      if (last != null) {
        return last;
      } else {
        return getPredecessor();
      }
    }

    void append(final TRAbstractionStep step)
    {
      mSteps.add(step);
      mCurrentEventEncoding =
        new EventEncoding(mInputAutomaton.getEventEncoding());
    }

    TransitionRelationSimplifier getLastPartitionSimplifier()
    {
      final TRAbstractionStep step = getLastIntermediateStep();
      if (step == null) {
        return null;
      } else if (step instanceof TRAbstractionStepPartition) {
        final TRAbstractionStepPartition partStep =
          (TRAbstractionStepPartition) step;
        return partStep.getLastSimplifier();
      } else {
        return null;
      }
    }

    void removeLastPartitionSimplifier()
    {
      final TRAbstractionStep step = mSteps.peekLast();
      if (step != null && step instanceof TRAbstractionStepPartition) {
        final TRAbstractionStepPartition partStep =
          (TRAbstractionStepPartition) step;
        partStep.removeLastSimplifier();
        if (partStep.isEmpty()) {
          mSteps.removeLast();
        }
      }
    }

    //#######################################################################
    //# String Abstraction Steps
    private void commit(final TRAutomatonProxy result)
    {
      final TRAbstractionStep last = mSteps.peekLast();
      if (last != null && isCounterExampleEnabled()) {
        if (mPredecessor != null) {
          mPredecessor.clearOutputAutomaton();
        }
        last.provideOutputAutomaton(result);
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
  private boolean mBlockedEventsEnabled = true;
  private boolean mFailingEventsEnabled = false;
  private boolean mSelfloopOnlyEventsEnabled = false;
  private boolean mAlwaysEnabledEventsEnabled = false;
  private boolean mTraceCheckingEnabled = false;
  private boolean mPreservingEncodings = false;

  // Tools
  private TRPreselectionHeuristic mPreselectionHeuristic;
  private SelectionHeuristic<TRCandidate> mSelectionHeuristic;
  private final PartitioningListener mSpecialEventsListener;
  private TRToolCreator<TransitionRelationSimplifier> mTRSimplifierCreator;
  private SpecialEventsFinder mSpecialEventsFinder;
  private TransitionRelationSimplifier mTRSimplifier;
  private final TRSynchronousProductBuilder mSynchronousProductBuilder;
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
  static final TRPreselectionHeuristic PRESEL_MustL =
    new TRPreselectionHeuristicMustL();
  static final TRPreselectionHeuristic PRESEL_MustSp =
    new TRPreselectionHeuristicMustSp();
  static final TRPreselectionHeuristic PRESEL_Pairs =
    new TRPreselectionHeuristicPairs();
  static final TRPreselectionHeuristic PRESEL_MinS =
    new TRPreselectionHeuristicMinS();
  static final TRPreselectionHeuristic PRESEL_MaxS =
    new TRPreselectionHeuristicMaxS();
  static final TRPreselectionHeuristic PRESEL_MinT =
    new TRPreselectionHeuristicMinT();
  static final TRPreselectionHeuristic PRESEL_MaxT =
    new TRPreselectionHeuristicMaxT();

  static final SelectionHeuristic<TRCandidate> SEL_MaxC =
    new SelectionHeuristicMaxC();
  static final SelectionHeuristic<TRCandidate> SEL_MaxL =
    new SelectionHeuristicMaxL();
  static final SelectionHeuristic<TRCandidate> SEL_MinE =
    new SelectionHeuristicMinE();
  static final SelectionHeuristic<TRCandidate> SEL_MinF =
    new SelectionHeuristicMinF();
  static final SelectionHeuristic<TRCandidate> SEL_MinS =
    new SelectionHeuristicMinS();
  static final SelectionHeuristic<TRCandidate> SEL_MinSSp =
    new SelectionHeuristicMinSSp();

  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

}
