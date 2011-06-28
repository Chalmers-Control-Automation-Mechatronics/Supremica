//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OPConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.HashFunctions;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntStack;
import gnu.trove.TObjectByteHashMap;
import gnu.trove.TObjectByteIterator;
import gnu.trove.TObjectIntHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.gnonblocking.Candidate;
import net.sourceforge.waters.analysis.modular.ModularControllabilityChecker;
import net.sourceforge.waters.analysis.monolithic.
  MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.AutomatonResult;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.TraceChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.apache.log4j.Logger;


/**
 * A compositional conflict checker that can be configured to use different
 * abstraction sequences for its simplification steps.
 *
 * @author Robi Malik, Rachel Francis
 */

public class OPConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public OPConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to the default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public OPConflictChecker(final ProductDESProxy model,
                           final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public OPConflictChecker(final ProductDESProxy model,
                           final EventProxy marking,
                           final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
    mAbstractionMethod = AbstractionMethod.OEQ;
    mPreselectingMethod = PreselectingMethod.MustL;
    mSelectingMethod = SelectingMethod.MinS;
    mSubsumptionEnabled = false;
    mLowerInternalStateLimit = mUpperInternalStateLimit = super.getNodeLimit();
    mInternalTransitionLimit = super.getTransitionLimit();
  }


  //#########################################################################
  //# Configuration
  @Override
  public EventProxy getPreconditionMarking()
  {
    return mPreconditionMarking;
  }

  @Override
  public void setPreconditionMarking(final EventProxy alpha)
  {
    mPreconditionMarking = alpha;
  }

  /**
   * Gets the abstraction strategy used to simplify automata.
   * @see AbstractionMethod
   */
  public AbstractionMethod getAbstractionMethod()
  {
    return mAbstractionMethod;
  }

  /**
   * Sets the abstraction strategy to be used to simplify automata.
   * @see AbstractionMethod
   */
  public void setAbstractionMethod(final AbstractionMethod method)
  {
    mAbstractionMethod = method;
  }

  /**
   * Sets the preselecting heuristics used to choose candidates.
   * @see PreselectingMethod
   */
  public PreselectingMethod getPreselectingMethod()
  {
    return mPreselectingMethod;
  }

  /**
   * Sets the preselecting heuristics to be used to choose candidates.
   * @see PreselectingMethod
   */
  public void setPreselectingMethod(final PreselectingMethod method)
  {
    mPreselectingMethod = method;
  }

  /**
   * Sets the selecting heuristics used to choose candidates.
   * @see SelectingMethod
   */
  public SelectingMethod getSelectingMethod()
  {
    return mSelectingMethod;
  }

  /**
   * Sets the selecting heuristics to be used to choose candidates.
   * @see SelectingMethod
   */
  public void setSelectingMethod(final SelectingMethod method)
  {
    mSelectingMethod = method;
  }

  /**
   * Returns whether subsumption is enabled in the selecting heuristic.
   * @see #setSubumptionEnabled(boolean)
   * @see SelectingMethod
   */
  public boolean isSubsumptionEnabled()
  {
    return mSubsumptionEnabled;
  }

  /**
   * Sets whether subsumption is enabled in the selecting heuristic.
   * If subsumption is enabled, and the heuristic returns a candidate that
   * is subsumed by another candidate, the a new candidate will be selected
   * from the list of all preselected candidates that subsume the originally
   * selected candidate. The selection heuristics will be used again to
   * resolve ties.
   * @see PreselectingMethod
   */
  public void setSubumptionEnabled(final boolean enable)
  {
    mSubsumptionEnabled = enable;
  }


  @Override
  public int getNodeLimit()
  {
    final int limit1 = getInternalStateLimit();
    final int limit2 = getMonolithicStateLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    setInternalStateLimit(limit);
    setMonolithicStateLimit(limit);
  }

  public int getInternalStateLimit()
  {
    return Math.max(mLowerInternalStateLimit, mUpperInternalStateLimit);
  }

  public void setInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = mUpperInternalStateLimit = limit;
  }

  public int getLowerInternalStateLimit()
  {
    return mLowerInternalStateLimit;
  }

  public void setLowerInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = limit;
  }

  public int getUpperInternalStateLimit()
  {
    return mUpperInternalStateLimit;
  }

  public void setUpperInternalStateLimit(final int limit)
  {
    mUpperInternalStateLimit = limit;
  }

  public int getMonolithicStateLimit()
  {
    return super.getNodeLimit();
  }

  public void setMonolithicStateLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
    setInternalTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    final int limit1 = getInternalTransitionLimit();
    final int limit2 = getMonolithicTransitionLimit();
    return Math.max(limit1, limit2);
  }

  public int getMonolithicTransitionLimit()
  {
    return super.getTransitionLimit();
  }

  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
  }

  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit;
  }


  public void setSynchronousProductBuilder
    (final SynchronousProductBuilder builder)
  {
    mSynchronousProductBuilder = builder;
  }

  public void setMonolithicConflictChecker(final ConflictChecker checker)
  {
    mMonolithicConflictChecker = checker;
  }

  public void setCompositionalSafetyVerifier(final SafetyVerifier checker)
  {
    mCompositionalSafetyVerifier = checker;
  }

  public void setMonolithicSafetyVerifier(final SafetyVerifier checker)
  {
    mMonolithicSafetyVerifier = checker;
  }


  private void setupSynchronousProductBuilder()
  {
    if (mCurrentSynchronousProductBuilder == null) {
      if (mSynchronousProductBuilder == null) {
        final ProductDESProxyFactory factory = getFactory();
        mCurrentSynchronousProductBuilder =
          new MonolithicSynchronousProductBuilder(factory);
      } else {
        mCurrentSynchronousProductBuilder = mSynchronousProductBuilder;
      }
      mCurrentSynchronousProductBuilder.setPropositions(mPropositions);
      final int tlimit = getInternalTransitionLimit();
      mCurrentSynchronousProductBuilder.setTransitionLimit(tlimit);
    }
  }

  private void setupMonolithicConflictChecker()
    throws EventNotFoundException
  {
    if (mCurrentMonolithicConflictChecker == null) {
      if (mMonolithicConflictChecker == null) {
        final ProductDESProxyFactory factory = getFactory();
        mCurrentMonolithicConflictChecker = new NativeConflictChecker(factory);
      } else {
        mCurrentMonolithicConflictChecker = mMonolithicConflictChecker;
      }
      final int nlimit = getMonolithicStateLimit();
      mCurrentMonolithicConflictChecker.setNodeLimit(nlimit);
      final int tlimit = getMonolithicTransitionLimit();
      mCurrentMonolithicConflictChecker.setTransitionLimit(tlimit);
      final KindTranslator translator = getKindTranslator();
      mCurrentMonolithicConflictChecker.setKindTranslator(translator);
      mCurrentMonolithicConflictChecker.setMarkingProposition
        (mCurrentDefaultMarking);
      mCurrentMonolithicConflictChecker.setPreconditionMarking
        (mPreconditionMarking);
    }
  }

  private void setupSafetyVerifiers()
  {
    final ProductDESProxyFactory factory = getFactory();
    if (mCurrentMonolithicSafetyVerifier == null) {
      if (mMonolithicSafetyVerifier == null) {
        mCurrentMonolithicSafetyVerifier =
          new NativeLanguageInclusionChecker(factory);
      } else {
        mCurrentMonolithicSafetyVerifier = mMonolithicSafetyVerifier;
      }
      final int nlimit = getMonolithicStateLimit();
      mCurrentMonolithicSafetyVerifier.setNodeLimit(nlimit);
      final int tlimit = getMonolithicTransitionLimit();
      mCurrentMonolithicSafetyVerifier.setTransitionLimit(tlimit);
    }
    if (mCurrentCompositionalSafetyVerifier == null) {
      if (mCompositionalSafetyVerifier == null) {
        mCurrentCompositionalSafetyVerifier =
          new ModularControllabilityChecker
            (factory, mCurrentMonolithicSafetyVerifier);
        final int nlimit = getMonolithicStateLimit();
        mCurrentMonolithicSafetyVerifier.setNodeLimit(nlimit);
        final int tlimit = getMonolithicTransitionLimit();
        mCurrentMonolithicSafetyVerifier.setTransitionLimit(tlimit);
      } else {
        mCurrentCompositionalSafetyVerifier = mCompositionalSafetyVerifier;
      }
    }
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      initialiseEventsToAutomata();
      simplify(true);
      boolean cancheck = true;
      OverflowException lastOverflow = null;
      Collection<Candidate> candidates;
      Candidate candidate = null;
      outer:
      do {
        subsystem:
        do {
          if (checkSubsystemTrivial()) {
            if (mGotGlobalResult) {
              break outer;
            } else {
              continue outer;
            }
          }
          candidates = mPreselectingHeuristic.findCandidates();
          candidate = selectCandidate(candidates);
          while (candidate != null) {
            try {
              mEventHasDisappeared = false;
              applyCandidate(candidate);
              simplify(mEventHasDisappeared);
              cancheck = true;
              continue subsystem;
            } catch (final OverflowException overflow) {
              recordUnsuccessfulComposition();
              final List<AutomatonProxy> automata = candidate.getAutomata();
              mOverflowCandidates.add(automata);
              candidates.remove(candidate);
              candidate = selectCandidate(candidates);
            }
          }
        } while (candidate != null);
        try {
          if (cancheck) {
            runMonolithicConflictCheck();
            lastOverflow = null;
          }
        } catch (final OverflowException overflow) {
          lastOverflow = overflow;
          cancheck = false;
        }
        if (lastOverflow != null) {
          if (mCurrentInternalStateLimit < mUpperInternalStateLimit) {
            mCurrentInternalStateLimit =
              Math.min(2 * mCurrentInternalStateLimit, mUpperInternalStateLimit);
            mOverflowCandidates.clear();
            final Logger logger = getLogger();
            if (logger.isDebugEnabled()) {
              final String msg =
                "State limit increased to " + mCurrentInternalStateLimit + ".";
              logger.debug(msg);
            }
          } else {
            throw lastOverflow;
          }
        }
      } while (lastOverflow != null ||
               !mGotGlobalResult && popEventDisjointSubsystem());

      if (mPreliminaryCounterexample == null) {
        return setSatisfiedResult();
      } else {
        mAbstractionRule.resetStatistics();
        restoreAutomata();
        final ConflictTraceProxy trace =
          expandTrace(mPreliminaryCounterexample);
        return setFailedResult(trace);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return mAbstractionMethod.supportsNondeterminism();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mAbstractionRule != null) {
      mAbstractionRule.requestAbort();
    }
    if (mCurrentSynchronousProductBuilder != null) {
      mCurrentSynchronousProductBuilder.requestAbort();
    }
    if (mCurrentMonolithicConflictChecker != null) {
      mCurrentMonolithicConflictChecker.requestAbort();
    }
    if (mCurrentCompositionalSafetyVerifier != null) {
      mCurrentCompositionalSafetyVerifier.requestAbort();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user has not
   * configured them.
   */
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final CompositionalVerificationResult result = getAnalysisResult();
    result.setNumberOfStates(0.0);
    result.setNumberOfTransitions(0.0);
    mCurrentDefaultMarking = getUsedMarkingProposition();
    if (mPreconditionMarking == null) {
      mPropositions = Collections.singletonList(mCurrentDefaultMarking);
    } else {
      final EventProxy[] markings = new EventProxy[2];
      markings[0] = mCurrentDefaultMarking;
      markings[1] = mPreconditionMarking;
      mPropositions = Arrays.asList(markings);
    }
    mAbstractionRule = mAbstractionMethod.createAbstractionRule(this);
    mAbstractionRule.storeStatistics();
    mPreselectingHeuristic = mPreselectingMethod.createHeuristic(this);
    mSelectingHeuristic = mSelectingMethod.createHeuristic(this);
    setupSynchronousProductBuilder();
    setupMonolithicConflictChecker();
    setupSafetyVerifiers();
    mModifyingSteps = new ArrayList<AbstractionStep>();
    mOverflowCandidates = new THashSet<List<AutomatonProxy>>();
    mCurrentInternalStateLimit = mLowerInternalStateLimit;
    mGotGlobalResult = false;
    mPreliminaryCounterexample = null;
  }

  protected void tearDown()
  {
    super.tearDown();
    mCurrentDefaultMarking = null;
    mPropositions = null;
    mAbstractionRule = null;
    mPreselectingHeuristic = null;
    mSelectingHeuristic = null;
    mCurrentSynchronousProductBuilder = null;
    mCurrentMonolithicConflictChecker = null;
    mCurrentCompositionalSafetyVerifier = null;
    mCurrentMonolithicSafetyVerifier = null;
    mCurrentAutomata = null;
    mAutomatonInfoMap = null;
    mEventInfoMap = null;
    mDirtyAutomata = null;
    mRedundantEvents = null;
    mPostponedSubsystems = null;
    mProcessedSubsystems = null;
    mModifyingSteps = null;
    mUsedEventNames = null;
    mOverflowCandidates = null;
    mPreliminaryCounterexample = null;
  }

  @Override
  protected CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult();
  }

  @Override
  public CompositionalVerificationResult getAnalysisResult()
  {
    return (CompositionalVerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Hooks
  /**
   * Creates a product DES consisting of the automata and events in the
   * given candidate. This hook is invoked before composing the automata
   * of a selected candidate. It may be overridden by specialised property
   * verifiers that modify the set of automata prior to composition.
   * @return A product DES to be passed to a {@link SynchronousProductBuilder}.
   */
  protected ProductDESProxy createProductDESProxy(final Candidate candidate)
  {
    final ProductDESProxyFactory factory = getFactory();
    return candidate.createProductDESProxy(factory);
  }


  //#########################################################################
  //# Chains
  private AbstractionRule createObservationEquivalenceChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    bisimulator.setTransitionLimit(mInternalTransitionLimit);
    chain.add(bisimulator);
    if (mPreconditionMarking != null) {
      return new GeneralisedTRSimplifierAbstractionRule(chain);
    } else {
      return new TRSimplifierAbstractionRule(chain);
    }
  }

  private AbstractionRule createStandardNonblockingAbstractionChain()
  throws EventNotFoundException
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(markingRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    chain.add(silentOutRemover);
    final SilentContinuationTRSimplifier silentContinuationRemover =
      new SilentContinuationTRSimplifier();
    silentContinuationRemover.setTransitionLimit(mInternalTransitionLimit);
    chain.add(silentContinuationRemover);
    final ActiveEventsTRSimplifier activeEventsMerger =
      new ActiveEventsTRSimplifier();
    activeEventsMerger.setTransitionLimit(mInternalTransitionLimit);
    chain.add(activeEventsMerger);
    final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    final int ccindex = chain.add(certainConflictsRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(mInternalTransitionLimit);
    chain.add(bisimulator);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new StandardTRSimplifierAbstractionRule(chain, ccindex);
  }

  private AbstractionRule createGeneralisedNonblockingAbstractionChain()
    throws EventNotFoundException
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final MarkingRemovalTRSimplifier alphaRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(alphaRemover);
    final int recoveryIndex = chain.size();
    final OmegaRemovalTRSimplifier omegaRemover =
      new OmegaRemovalTRSimplifier();
    chain.add(omegaRemover);
    if (mPreconditionMarking != null) {
      final CoreachabilityTRSimplifier nonCoreachableRemover =
        new CoreachabilityTRSimplifier();
      chain.add(nonCoreachableRemover);
    }
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    chain.add(silentOutRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(mInternalTransitionLimit);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier(bisimulator);
    chain.add(nonAlphaDeterminiser);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new GeneralisedTRSimplifierAbstractionRule(chain, recoveryIndex);
  }


  //#########################################################################
  //# Events+Automata Maps
  /**
   * Maps the events in the model to a set of the automata that contain the
   * event in their alphabet.
   */
  private void initialiseEventsToAutomata()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mCurrentAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    mAutomatonInfoMap = new HashMap<AutomatonProxy,AutomatonInfo>(numAutomata);
    final int numEvents = model.getEvents().size();
    mEventInfoMap = new HashMap<EventProxy,EventInfo>(numEvents);
    mDirtyAutomata = new LinkedList<AutomatonProxy>();
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) != ComponentKind.PROPERTY) {
        mCurrentAutomata.add(aut);
        addEventsToAutomata(aut);
        mDirtyAutomata.add(aut);
      }
    }
    final CompositionalVerificationResult result = getAnalysisResult();
    result.setNumberOfAutomata(mCurrentAutomata.size());
    mUsedEventNames = new THashSet<String>(numEvents + numAutomata);
    for (final EventProxy event : mEventInfoMap.keySet()) {
      final String name = event.getName();
      mUsedEventNames.add(name);
    }
    mRedundantEvents = new LinkedList<EventProxy>();
    for (final Map.Entry<EventProxy,EventInfo> entry :
         mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.isRemovable()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
    mPostponedSubsystems = new LinkedList<SubSystem>();
    mProcessedSubsystems = new LinkedList<SubSystem>();
  }

  private void updateEventsToAutomata(final AutomatonProxy autToAdd,
                                      final List<AutomatonProxy> autToRemove)
  {
    mCurrentAutomata.removeAll(autToRemove);
    mCurrentAutomata.add(autToAdd);
    addEventsToAutomata(autToAdd);
    removeEventsToAutomata(autToRemove);
  }

  private void addEventsToAutomata(final AutomatonProxy aut)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final TObjectByteHashMap<EventProxy> statusMap =
      new TObjectByteHashMap<EventProxy>(numEvents);
    for (final TransitionProxy trans : aut.getTransitions()) {
      final EventProxy event = trans.getEvent();
      if (trans.getSource() != trans.getTarget()) {
        statusMap.put(event, NOT_ONLY_SELFLOOP);
      } else if (!statusMap.containsKey(event)) {
        statusMap.put(event, ONLY_SELFLOOP);
      }
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        EventInfo info = mEventInfoMap.get(event);
        if (info == null) {
          info = new EventInfo();
          mEventInfoMap.put(event, info);
        }
        final byte lookup = statusMap.get(event);
        final byte status = lookup == UNKNOWN_SELFLOOP ? BLOCKED : lookup;
        info.addAutomaton(aut, status);
      }
    }
  }

  private void removeEventsToAutomata(final Collection<AutomatonProxy> victims)
  {
    for (final AutomatonProxy aut : victims) {
      mAutomatonInfoMap.remove(aut);
    }
    mRedundantEvents.clear();
    final Iterator<Map.Entry<EventProxy,EventInfo>> iter =
      mEventInfoMap.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<EventProxy,EventInfo> entry = iter.next();
      final EventInfo info = entry.getValue();
      info.removeAutomata(victims);
      if (info.isEmpty()) {
        iter.remove();
      } else if (info.isRemovable()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
  }

  /**
   * Finds the set of events that are local to a candidate (i.e. a set of
   * automata).
   */
  private Set<EventProxy> identifyLocalEvents
    (final Collection<AutomatonProxy> candidate)
  {
    final Set<EventProxy> localEvents = new THashSet<EventProxy>();
    for (final Map.Entry<EventProxy,EventInfo> entry : mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.containedIn(candidate)) {
        final EventProxy event = entry.getKey();
        localEvents.add(event);
      }
    }
    return localEvents;
  }

  private boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return
      automata.size() < mCurrentAutomata.size() &&
      !mOverflowCandidates.contains(automata);
  }

  private boolean removeRedundantEvents()
  {
    if (mRedundantEvents.isEmpty()) {
      return false;
    } else {
      final int numRedundant = mRedundantEvents.size();
      final Set<EventProxy> redundant = new THashSet<EventProxy>(numRedundant);
      for (final EventProxy event : mRedundantEvents) {
        redundant.add(event);
        mEventInfoMap.remove(event);
      }
      mRedundantEvents.clear();
      final ProductDESProxyFactory factory = getFactory();
      final int numAutomata = mCurrentAutomata.size();
      final List<AutomatonProxy> originals =
        new ArrayList<AutomatonProxy>(numAutomata);
      final List<AutomatonProxy> results =
        new ArrayList<AutomatonProxy>(numAutomata);
      for (int i = 0; i < numAutomata; i++) {
        final AutomatonProxy aut = mCurrentAutomata.get(i);
        final Collection<EventProxy> events = aut.getEvents();
        boolean found = false;
        for (final EventProxy event : events) {
          if (redundant.contains(event)) {
            found = true;
            break;
          }
        }
        if (!found) {
          continue;
        }
        final int numEvents = events.size();
        final Collection<EventProxy> newEvents =
          new ArrayList<EventProxy>(numEvents - 1);
        for (final EventProxy event : events) {
          if (!redundant.contains(event)) {
            newEvents.add(event);
          }
        }
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        final int numTrans = transitions.size();
        final Collection<TransitionProxy> newTransitions =
          new ArrayList<TransitionProxy>(numTrans);
        boolean dirty = false;
        for (final TransitionProxy trans : transitions) {
          final EventProxy event = trans.getEvent();
          if (!redundant.contains(event)) {
            newTransitions.add(trans);
          } else if (trans.getSource() != trans.getTarget()) {
            dirty = true;
          }
        }
        final String name = aut.getName();
        final ComponentKind kind = aut.getKind();
        final Collection<StateProxy> states = aut.getStates();
        final AutomatonProxy newAut = factory.createAutomatonProxy
          (name, kind, newEvents, states, newTransitions);
        originals.add(aut);
        results.add(newAut);
        mCurrentAutomata.set(i, newAut);
        for (final EventProxy event : newEvents) {
          final EventInfo info = mEventInfoMap.get(event);
          if (info != null) {
            info.replaceAutomaton(aut, newAut);
          }
        }
        if (dirty) {
          mDirtyAutomata.add(newAut);
        }
      }
      final AbstractionStep step = new EventRemovalStep(results, originals);
      mModifyingSteps.add(step);
      final CompositionalVerificationResult stats = getAnalysisResult();
      stats.addRedundantEvents(numRedundant);
      return true;
    }
  }

  private boolean findEventDisjointSubsystems()
    throws AnalysisException
  {
    if (mEventInfoMap.isEmpty()) {
      return false;
    }
    final Collection<AutomatonProxy> remainingAutomata =
      new THashSet<AutomatonProxy>(mCurrentAutomata);
    final List<EventProxy> remainingEvents =
      new LinkedList<EventProxy>(mEventInfoMap.keySet());
    Collections.sort(remainingEvents);
    final List<SubSystem> tasks = new LinkedList<SubSystem>();
    while (!remainingEvents.isEmpty()) {
      final int numAutomata = remainingAutomata.size();
      final int numEvents = remainingEvents.size();
      final Iterator<EventProxy> iter1 = remainingEvents.iterator();
      final EventProxy event1 = iter1.next();
      iter1.remove();
      final List<EventProxy> subSystemEvents =
        new ArrayList<EventProxy>(numEvents);
      subSystemEvents.add(event1);
      final EventInfo info1 = mEventInfoMap.get(event1);
      final Collection<AutomatonProxy> subSystemAutomata =
        info1.getAutomataSet();
      if (subSystemAutomata.size() == numAutomata) {
        subSystemEvents.addAll(remainingEvents);
        remainingEvents.clear();
      } else {
        boolean change;
        do {
          change = false;
          final Iterator<EventProxy> iter = remainingEvents.iterator();
          while (iter.hasNext()) {
            final EventProxy event = iter.next();
            final EventInfo info = mEventInfoMap.get(event);
            if (info.intersects(subSystemAutomata)) {
              if (info.addAutomataTo(subSystemAutomata)) {
                if (subSystemAutomata.size() == numAutomata) {
                  subSystemEvents.addAll(remainingEvents);
                  remainingEvents.clear();
                  change = false;
                  break;
                }
                change = true;
              }
              iter.remove();
              subSystemEvents.add(event);
            }
          }
        } while (change);
      }
      if (subSystemAutomata.size() < numAutomata) {
        remainingAutomata.removeAll(subSystemAutomata);
      } else if (tasks.isEmpty()) {
        return false;
      } else {
        remainingAutomata.clear();
      }
      final List<AutomatonProxy> subSystemAutomataList =
        new ArrayList<AutomatonProxy>(subSystemAutomata);
      Collections.sort(subSystemAutomataList);
      final SubSystem task = new SubSystem(subSystemEvents,
                                           subSystemAutomataList,
                                           mCurrentInternalStateLimit);
      tasks.add(task);
    }
    for (final AutomatonProxy aut : remainingAutomata) {
      final SubSystem task = new SubSystem(aut, mCurrentInternalStateLimit);
      tasks.add(task);
    }
    final Iterator<SubSystem> iter = tasks.iterator();
    SubSystem task0 = iter.next();
    while (iter.hasNext()) {
      final SubSystem task = iter.next();
      if (task0.compareTo(task) < 0) {
        mPostponedSubsystems.add(task);
      } else {
        mPostponedSubsystems.add(task0);
        task0 = task;
      }
    }
    loadSubSystem(task0);
    return tasks.size() > 1;
  }

  private boolean popEventDisjointSubsystem()
  {
    if (mPostponedSubsystems.isEmpty()) {
      return false;
    } else {
      final List<EventProxy> events =
        new ArrayList<EventProxy>(mEventInfoMap.keySet());
      Collections.sort(events);
      final SubSystem current =
        new SubSystem(events, mCurrentAutomata, mCurrentInternalStateLimit);
      mProcessedSubsystems.add(current);
      final SubSystem next = Collections.min(mPostponedSubsystems);
      mPostponedSubsystems.remove(next);
      loadSubSystem(next);
      return true;
    }
  }

  private void loadSubSystem(final SubSystem task)
  {
    mCurrentAutomata = task.getAutomata();
    mCurrentInternalStateLimit = task.getStateLimit();
    mEventInfoMap.clear();
    for (final AutomatonProxy aut : mCurrentAutomata) {
      addEventsToAutomata(aut);
    }
  }

  private void restoreAutomata()
  {
    for (final SubSystem task : mProcessedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
    for (final SubSystem task : mPostponedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
  }

  private void runMonolithicConflictCheck()
    throws AnalysisException
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      double estimate = 1.0;
      for (final AutomatonProxy aut : mCurrentAutomata) {
        estimate *= aut.getStates().size();
      }
      logger.debug("Monolithically composing " + mCurrentAutomata.size() +
                   " automata, estimated " + estimate + " states.");
    }
    final int numEvents = mEventInfoMap.size() + 1;
    final List<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    events.addAll(mEventInfoMap.keySet());
    events.add(mCurrentDefaultMarking);
    Collections.sort(events);
    final ProductDESProxy des = createDES(events, mCurrentAutomata);
    mCurrentMonolithicConflictChecker.setModel(des);
    mCurrentMonolithicConflictChecker.run();
    // Do not clean up before run, keep data just in case of overflow ...
    removeEventsToAutomata(mCurrentAutomata);
    final VerificationResult result =
      mCurrentMonolithicConflictChecker.getAnalysisResult();
    recordStatistics(result);
    if (!result.isSatisfied()) {
      mPreliminaryCounterexample =
        mCurrentMonolithicConflictChecker.getCounterExample();
      checkAlphaReachable(false);
      mGotGlobalResult = true;
    }
  }

  private ProductDESProxy createDES(final List<EventProxy> events,
                                    final List<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = Candidate.getCompositionName(automata);
    return factory.createProductDESProxy(name, events, automata);
  }


  //#########################################################################
  //# Proposition Analysis
  private boolean checkSubsystemTrivial()
    throws AnalysisException
  {
    final byte status = getSubsystemPropositionStatus();
    if ((status & NONE_ALPHA) != 0) {
      // The global system is nonblocking.
      return mGotGlobalResult = true;
    } else if ((status & ALL_OMEGA) != 0) {
      // This subsystem is trivially nonblocking.
      return true;
    } else if ((status & NONE_OMEGA) != 0) {
      // The global system is blocking if and only if alpha is reachable
      checkAlphaReachable((status & ALL_ALPHA) == 0);
      return mGotGlobalResult = true;
    } else {
      return false;
    }
  }

  private byte getSubsystemPropositionStatus()
    throws EventNotFoundException
  {
    byte all = ALL_ALPHA | ALL_OMEGA;
    byte none = 0;
    for (final AutomatonProxy aut : mCurrentAutomata) {
      final AutomatonInfo info = getAutomatonInfo(aut);
      if (info.isNeverPreconditionMarked()) {
        return NONE_ALPHA;
      }
      if (info.isNeverDefaultMarked()) {
        none |= NONE_OMEGA;
      }
      if (!info.isAlwaysDefaultMarked()) {
        all &= ~ALL_OMEGA;
      }
      if (!info.isAlwaysPreconditionMarked()) {
        all &= ~ALL_ALPHA;
      }
    }
    return (byte) (all | none);
  }

  private AutomatonInfo getAutomatonInfo(final AutomatonProxy aut)
  {
    AutomatonInfo info = mAutomatonInfoMap.get(aut);
    if (info == null) {
      info = new AutomatonInfo(aut);
      mAutomatonInfoMap.put(aut, info);
    }
    return info;
  }


  //#########################################################################
  //# Alpha-Reachability Check
  private boolean checkAlphaReachable(final boolean includeCurrent)
    throws AnalysisException
  {
    if (mPreconditionMarking == null) {
      if (mPreliminaryCounterexample == null) {
        mPreliminaryCounterexample = createInitialStateTrace();
      }
      return true;
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      final PropositionPropertyBuilder builder =
        new PropositionPropertyBuilder(factory,
                                       mPreconditionMarking,
                                       translator);
      final List<ConflictTraceProxy> traces =
        new LinkedList<ConflictTraceProxy>();
      if (includeCurrent) {
        if (!isAlphaReachable(builder, mEventInfoMap.keySet(),
                              mCurrentAutomata, traces)) {
          return false;
        }
      }
      for (final SubSystem subsys : mPostponedSubsystems) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          return false;
        }
      }
      for (final SubSystem subsys : mProcessedSubsystems) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          return false;
        }
      }
      if (mPreliminaryCounterexample != null) {
        traces.add(mPreliminaryCounterexample);
      }
      mPreliminaryCounterexample = mergeLanguageInclusionTraces(traces);
      return true;
    }
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final SubSystem subsys,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final Collection<EventProxy> events = subsys.getEvents();
    final List<AutomatonProxy> automata = subsys.getAutomata();
    return isAlphaReachable(builder, events, automata, traces);
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final Collection<EventProxy> events,
                                   final List<AutomatonProxy> automata,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final List<EventProxy> eventList = new ArrayList<EventProxy>(events);
    Collections.sort(eventList);
    final ProductDESProxy des = createDES(eventList, automata);
    builder.setInputModel(des);
    builder.run();
    final ProductDESProxy languageInclusionModel = builder.getOutputModel();
    final KindTranslator languageInclusionTranslator =
      builder.getKindTranslator();
    final SafetyVerifier checker;
    if (languageInclusionModel.getAutomata().size() > 2) {
      checker = mCurrentCompositionalSafetyVerifier;
    } else {
      checker = mCurrentMonolithicSafetyVerifier;
    }
    checker.setKindTranslator(languageInclusionTranslator);
    checker.setModel(languageInclusionModel);
    if (checker.run()) {
      return false;
    } else {
      final SafetyTraceProxy languageInclusionTrace =
        checker.getCounterExample();
      final ConflictTraceProxy conflictTrace =
        builder.getConvertedConflictTrace(languageInclusionTrace);
      traces.add(conflictTrace);
      return true;
    }
  }

  private ConflictTraceProxy createInitialStateTrace()
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String name = model.getName() + ":initial";
    final String comment = "Initial state trace";
    final int numAutomata = model.getAutomata().size();
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(numAutomata);
    automata.addAll(mCurrentAutomata);
    for (final SubSystem subsys : mPostponedSubsystems) {
      final Collection<AutomatonProxy> moreAutomata = subsys.getAutomata();
      automata.addAll(moreAutomata);
    }
    for (final SubSystem subsys : mProcessedSubsystems) {
      final Collection<AutomatonProxy> moreAutomata = subsys.getAutomata();
      automata.addAll(moreAutomata);
    }
    final TraceStepProxy step = factory.createTraceStepProxy(null);
    final List<TraceStepProxy> steps = Collections.singletonList(step);
    return factory.createConflictTraceProxy
      (name, comment, null, model, automata, steps, ConflictKind.CONFLICT);
  }

  private ConflictTraceProxy mergeLanguageInclusionTraces
    (final Collection<ConflictTraceProxy> traces)
  {
    int numAutomata = 0;
    int numSteps = 1;
    for (final ConflictTraceProxy trace : traces) {
      numAutomata += trace.getAutomata().size();
      numSteps += trace.getTraceSteps().size() - 1;
    }
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(numAutomata);
    final Map<AutomatonProxy,StateProxy> initMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    final List<TraceStepProxy> steps = new ArrayList<TraceStepProxy>(numSteps);
    steps.add(null);
    for (final ConflictTraceProxy trace : traces) {
      automata.addAll(trace.getAutomata());
      final List<TraceStepProxy> traceSteps = trace.getTraceSteps();
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      final TraceStepProxy traceInitStep = iter.next();
      initMap.putAll(traceInitStep.getStateMap());
      while (iter.hasNext()) {
        steps.add(iter.next());
      }
    }
    final ProductDESProxy model = getModel();
    final String name = model.getName() + mPreconditionMarking.getName();
    final ProductDESProxyFactory factory = getFactory();
    final TraceStepProxy initStep = factory.createTraceStepProxy(null, initMap);
    steps.set(0, initStep);
    return factory.createConflictTraceProxy(name, null, null, model, automata,
                                            steps, ConflictKind.CONFLICT);
  }


  //#########################################################################
  //# Candidate Selection
  /**
   * Performs the second step of candidate selection.
   * @param  preselected  List of preselected candidates from step&nbsp;1.
   * @return Preferred candidate from the given list, taking subsumption
   *         into account, or <CODE>null</CODE> if no suitable candidate
   *         could be found within the state limits.
   */
  private Candidate selectCandidate(final Collection<Candidate> preselected)
  throws AnalysisException
  {
    if (preselected.isEmpty()) {
      return null;
    } else {
      final Candidate result = mSelectingHeuristic.selectCandidate(preselected);
      if (mSubsumptionEnabled) {
        final Collection<Candidate> subsumedBy = new LinkedList<Candidate>();
        for (final Candidate candidate : preselected) {
          if (candidate.subsumes(result)) {
            subsumedBy.add(candidate);
          }
        }
        if (!subsumedBy.isEmpty()) {
          return selectCandidate(subsumedBy);
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Abstraction Steps
  private void simplify(final boolean eventsChanged)
    throws AnalysisException
  {
    final boolean change1 = simplifyDirtyAutomata();
    final boolean change2 = removeRedundantEvents();
    if (change1 || change2 || eventsChanged) {
      boolean change;
      do {
        change = simplifyDirtyAutomata() && removeRedundantEvents();
      } while (change);
      findEventDisjointSubsystems();
    }
  }

  private boolean simplifyDirtyAutomata()
    throws AnalysisException
  {
    boolean result = false;
    while (!mDirtyAutomata.isEmpty()) {
      final AutomatonProxy aut = mDirtyAutomata.remove();
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      final Set<EventProxy> local = new THashSet<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        final EventInfo info = mEventInfoMap.get(event);
        if (info != null && info.getNumberOfAutomata() == 1) {
          local.add(event);
        }
      }
      final List<AutomatonProxy> singleton = Collections.singletonList(aut);
      final Candidate candidate = new Candidate(singleton, local);
      result |= applyCandidate(candidate);
    }
    return result;
  }

  private boolean applyCandidate(final Candidate candidate)
    throws AnalysisException
  {
    final HidingStep syncStep = composeSynchronousProduct(candidate);
    final EventProxy tau;
    AutomatonProxy aut;
    if (syncStep == null) {
      aut = candidate.getAutomata().iterator().next();
      tau = null;
    } else {
      aut = syncStep.getResultAutomaton();
      tau = syncStep.getHiddenEvent();
    }
    recordStatistics(aut);
    final AbstractionStep simpStep = mAbstractionRule.applyRule(aut, tau);
    if (syncStep != null || simpStep != null) {
      if (syncStep != null) {
        mModifyingSteps.add(syncStep);
      }
      if (simpStep != null) {
        final Collection<EventProxy> oldEvents = aut.getEvents();
        aut = simpStep.getResultAutomaton();
        final Collection<EventProxy> newEvents =
          new THashSet<EventProxy>(aut.getEvents());
        for (final EventProxy event : oldEvents) {
          if (event != tau && !newEvents.contains(event)) {
            mEventHasDisappeared = true;
            break;
          }
        }
        mModifyingSteps.add(simpStep);
      }
      updateEventsToAutomata(aut, candidate.getAutomata());
      return true;
    } else {
      return false;
    }
  }


  /**
   * Builds the synchronous product for a given candidate.
   */
  private HidingStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    final ProductDESProxy des = createProductDESProxy(candidate);
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final ProductDESProxyFactory factory = getFactory();
    final EventProxy tau = createSilentEvent(candidate, factory);
    final Collection<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() > 1) {
      return composeSeveralAutomata(des, local, tau);
    } else {
      final AutomatonProxy aut = automata.iterator().next();
      return composeOneAutomaton(aut, local, tau);
    }
  }

  private HidingStep composeOneAutomaton(final AutomatonProxy aut,
                                         final Collection<EventProxy> local,
                                         final EventProxy tau)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc = new EventEncoding();
    eventEnc.addSilentEvent(tau);
    for (final EventProxy event : aut.getEvents()) {
      if (local.contains(event)) {
        eventEnc.addSilentEvent(event);
      } else if (translator.getEventKind(event) != EventKind.PROPOSITION ||
                 mPropositions.contains(event)) {
        eventEnc.addEvent(event, translator, false);
      }
    }
    final StateEncoding stateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (aut, eventEnc, stateEnc,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final boolean change1 = rel.checkReachability();
    final boolean change2 = rel.removeTauSelfLoops();
    final boolean change3 = rel.removeProperSelfLoopEvents();
    final boolean change4 = rel.removeRedundantPropositions();
    final EventProxy trueTau = change2 ? null : tau;
    mEventHasDisappeared |= change3;
    final ProductDESProxyFactory factory = getFactory();
    if (change4) {
      final StateEncoding newStateEnc = new StateEncoding();
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, newStateEnc);
      final SynchronousProductStateMap stateMap =
        new OneAutomatonStateMap(aut, stateEnc, newStateEnc);
      return new HidingStep(abstracted, local, trueTau, stateMap);
    } else if (tau != null || change1 || change2 || change3) {
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, stateEnc);
      return new HidingStep(abstracted, aut, local, trueTau);
    } else {
      return null;
    }
  }

  private HidingStep composeSeveralAutomata(final ProductDESProxy des,
                                            final Collection<EventProxy> local,
                                            final EventProxy tau)
    throws AnalysisException
  {
    mCurrentSynchronousProductBuilder.setModel(des);
    final Collection<EventProxy> events = des.getEvents();
    int expectedNumberOfEvents = events.size() - local.size();
    if (tau != null) {
      mCurrentSynchronousProductBuilder.addMask(local, tau);
      expectedNumberOfEvents++;
    }
    mCurrentSynchronousProductBuilder.setConstructsAutomaton(true);
    mCurrentSynchronousProductBuilder.setNodeLimit(mCurrentInternalStateLimit);
    try {
      mCurrentSynchronousProductBuilder.run();
      final AutomatonProxy sync =
        mCurrentSynchronousProductBuilder.getComputedAutomaton();
      mEventHasDisappeared |= sync.getEvents().size() < expectedNumberOfEvents;
      final SynchronousProductStateMap stateMap =
        mCurrentSynchronousProductBuilder.getStateMap();
      return new HidingStep(sync, local, tau, stateMap);
    } finally {
      final CompositionalVerificationResult stats = getAnalysisResult();
      final AutomatonResult result =
        mCurrentSynchronousProductBuilder.getAnalysisResult();
      stats.addSynchronousProductAnalysisResult(result);
      mCurrentSynchronousProductBuilder.clearMask();
    }
  }

  /**
   * Creates a silent event for hiding using the given candidate.
   * @return A new event named according to the candidate's automata,
   *         or <CODE>null</CODE> if the candidate does not have any local
   *         events.
   */
  private EventProxy createSilentEvent(final Candidate candidate,
                                       final ProductDESProxyFactory factory)
  {
    final Collection<EventProxy> local = candidate.getLocalEvents();
    if (local.isEmpty()) {
      return null;
    } else {
      final List<AutomatonProxy> automata = candidate.getAutomata();
      String name = Candidate.getCompositionName("tau:", automata);
      int prefix = 0;
      while (!mUsedEventNames.add(name)) {
        prefix++;
        name = Candidate.getCompositionName("tau" + prefix + ":", automata);
      }
      return factory.createEventProxy(name, EventKind.UNCONTROLLABLE, false);
    }
  }


  //#########################################################################
  //# Trace Computation
  private ConflictTraceProxy expandTrace(final ConflictTraceProxy trace)
    throws AnalysisException
  {
    final List<TraceStepProxy> unsat = trace.getTraceSteps();
    List<TraceStepProxy> traceSteps =
      getSaturatedTraceSteps(unsat, mCurrentAutomata);
    final int size = mModifyingSteps.size();
    final ListIterator<AbstractionStep> iter =
      mModifyingSteps.listIterator(size);
    final Collection<AutomatonProxy> check =
      new THashSet<AutomatonProxy>(mCurrentAutomata);
    //checkCounterExample(traceSteps, check);
    while (iter.hasPrevious()) {
      final AbstractionStep step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
      check.removeAll(step.getResultAutomata());
      check.addAll(step.getOriginalAutomata());
      //checkCounterExample(traceSteps, check);
    }
    final ProductDESProxyFactory factory = getFactory();
    final String tracename = getTraceName();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    return factory.createConflictTraceProxy(tracename,
                                            null,  // comment?
                                            null,
                                            model,
                                            automata,
                                            traceSteps,
                                            trace.getKind());
  }

  @SuppressWarnings("unused")
  private void checkCounterExample(final List<TraceStepProxy> steps,
                                   final Collection<AutomatonProxy> automata)
  throws AnalysisException
  {
    /*
    TraceChecker.checkCounterExample(steps, automata,
                                     mPreconditionMarking, true);
    */
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkConflictCounterExample(steps, automata,
                                             mPreconditionMarking,
                                             mCurrentDefaultMarking,
                                             true, translator);
  }

  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  private List<TraceStepProxy> getSaturatedTraceSteps
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final int numAutomata = automata.size();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = getInitialState(aut, firstMap);
      convertedFirstMap.put(aut, state);
    }
    final TraceStepProxy convertedFirstStep =
      factory.createTraceStepProxy(null, convertedFirstMap);
    convertedSteps.add(convertedFirstStep);
    Map<AutomatonProxy,StateProxy> previousStepMap = convertedFirstMap;
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final StateProxy prev = previousStepMap.get(aut);
        final StateProxy state = findSuccessor(aut, event, prev, stepMap);
        convertedStepMap.put(aut, state);
      }
      final TraceStepProxy convertedStep =
        factory.createTraceStepProxy(event, convertedStepMap);
      convertedSteps.add(convertedStep);
      previousStepMap = convertedStepMap;
    }
    return convertedSteps;
  }

  /**
   * Finds the initial state of an automaton in a trace.
   * A trace step's map is passed for the case of multiple initial states.
   */
  private StateProxy getInitialState
    (final AutomatonProxy aut, final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one initial state, the trace has the info.
    StateProxy initial = stepMap.get(aut);
    // Otherwise there is only one initial state.
    if (initial == null) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
    }
    return initial;
  }

  /**
   * Finds the successor state in trace, from a given state in an automaton.
   * A trace step's map is passed for the case of multiple successor states.
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
                                   final EventProxy event,
                                   final StateProxy sourceState,
                                   final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one successor state, the trace has the info.
    final StateProxy targetState = stepMap.get(aut);
    // Otherwise there is only one successor state.
    if (targetState == null) {
      if (aut.getEvents().contains(event)) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getEvent() == event && trans.getSource() == sourceState) {
            return trans.getTarget();
          }
        }
      } else {
        return sourceState;
      }
    }
    return targetState;
  }


  //#########################################################################
  //# Statistics
  private void recordStatistics(final AutomatonProxy aut)
  {
    final CompositionalVerificationResult result = getAnalysisResult();
    result.addCompositionAttempt();
    final int numStates = aut.getStates().size();
    final int numTrans = aut.getTransitions().size();
    final double totalStates = result.getTotalNumberOfStates() + numStates;
    result.setTotalNumberOfStates(totalStates);
    final double peakStates =
      Math.max(result.getPeakNumberOfStates(), numStates);
    result.setPeakNumberOfStates(peakStates);
    final double totalTrans = result.getTotalNumberOfTransitions() + numTrans;
    result.setTotalNumberOfTransitions(totalTrans);
    final double peakTrans =
      Math.max(result.getPeakNumberOfTransitions(), numTrans);
    result.setPeakNumberOfTransitions(peakTrans);
  }

  private void recordUnsuccessfulComposition()
  {
    final CompositionalVerificationResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }

  private void recordStatistics(final VerificationResult result)
  {
    final CompositionalVerificationResult global = getAnalysisResult();
    global.addMonolithicVerificationResult(result);
  }


  //#########################################################################
  //# Inner Enumeration AbstractionMethod
  /**
   * The configuration setting to determine the abstraction method applied
   * to intermediate automata during compositional nonblocking verification.
   */
  public enum AbstractionMethod
  {
    /**
     * Minimisation is performed according to a sequence of abstraction rules
     * for generalised nonblocking proposed in the paper by R. Malik and
     * R. Leduc in ICCA&nbsp;2009.
     */
    GNB {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
        throws EventNotFoundException
      {
        return checker.createGeneralisedNonblockingAbstractionChain();
      }
    },
    /**
     * <P>
     * Minimisation is performed according to a sequence of abstraction rules
     * for standard nonblocking.
     * </P>
     *
     * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
     * Verification in Supervisory Control. SIAM Journal of Control and
     * Optimization, 48(3), 1914-1938, 2009.</P>
     */
    NB {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
        throws EventNotFoundException
      {
        return checker.createStandardNonblockingAbstractionChain();
      }
    },
    /**
     * Automata are minimised according to <I>observation equivalence</I>.
     */
    OEQ {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
      {
        final ObservationEquivalenceTRSimplifier.Equivalence equivalence =
          ObservationEquivalenceTRSimplifier.Equivalence.
          OBSERVATION_EQUIVALENCE;
        return checker.createObservationEquivalenceChain(equivalence);
      }
    },
    /**
     * Automata are minimised according using <I>observer projection</I>.
     * The present implementation determines a coarsest causal reporter
     * map satisfying the observer property. Nondeterminism in the projected
     * automata is not resolved, nondeterministic abstraction are used instead.
     */
    OP {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
      {
        return checker.new ObserverProjectionAbstractionRule();
      }
    },
    /**
     * Automata are minimised according using an <I>observer projection</I>
     * obtained by the OP-search algorithm presented in the paper by
     * P. Pena, J.E.R. Cury, R. Malik, and S. Lafortune in WODES 2010.
     */
    OPSEARCH {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
      {
        return checker.new OPSearchAbstractionRule();
      }

      boolean supportsNondeterminism()
      {
        return false;
      }
    },
    /**
     * Automata are minimised according to <I>weak observation equivalence</I>.
     * Initial states and markings are not saturated, silent transitions
     * are retained instead in a bid to reduce the overall number of
     * transitions.
     */
    WOEQ {
      AbstractionRule createAbstractionRule(final OPConflictChecker checker)
      {
        final ObservationEquivalenceTRSimplifier.Equivalence equivalence =
          ObservationEquivalenceTRSimplifier.Equivalence.
          WEAK_OBSERVATION_EQUIVALENCE;
        return checker.createObservationEquivalenceChain(equivalence);
      }
    };

    abstract AbstractionRule createAbstractionRule
      (OPConflictChecker checker)
      throws EventNotFoundException;

    boolean supportsNondeterminism()
    {
      return true;
    }
  }


  //#########################################################################
  //# Inner Enumeration PreselectingMethod
  /**
   * The configuration setting to determine the {@link
   * OPConflictChecker.PreselectingHeuristic PreselectingHeuristic} used to
   * choose candidates during compositional verification. The preselecting
   * represents the first step of candidate selection. It generates a list
   * of candidates, from which the best candidate is to be chosen by the
   * selecting heuristic in the second step.
   *
   * @see SelectingMethod
   */
  public enum PreselectingMethod
  {
    /**
     * Every set of automata with at least one local event is considered
     * as a candidate.
     */
    MustL {
      @Override
      PreselectingHeuristic createHeuristic
        (final OPConflictChecker checker)
      {
        return checker.new HeuristicMustL();
      }
    },
    /**
     * Candidates are produced by pairing the automaton with the most states to
     * every other automaton in the model.
     */
    MaxS {
      @Override
      PreselectingHeuristic createHeuristic
        (final OPConflictChecker checker)
      {
        return checker.new HeuristicMaxS();
      }
    },
    /**
     * Candidates are produced by pairing the automaton with the fewest
     * transitions to every other automaton in the model.
     */
    MinT {
      @Override
      PreselectingHeuristic createHeuristic
        (final OPConflictChecker checker)
      {
        return checker.new HeuristicMinT();
      }
    },
    /**
     * Candidates are produced by pairing the automaton with the fewest
     * transitions connected to a precondition-marked state to every other
     * automaton in the model.
     */
    MinTa {
      @Override
      PreselectingHeuristic createHeuristic
        (final OPConflictChecker checker)
      {
        return checker.new HeuristicMinTAlpha();
      }
    };

    abstract PreselectingHeuristic createHeuristic(OPConflictChecker checker);
  }


  //#########################################################################
  //# Inner Enumeration SelectingMethod
  /**
   * <P>The configuration setting to determine the selecting heuristic
   * used to choose candidates during compositional verification.</P>
   *
   * <P>The selecting represents the second step of candidate selection. It
   * chooses the best candidate from a list of candidates generated by the
   * {@link OPConflictChecker.PreselectingHeuristic PreselectingHeuristic}
   * in the first step.</P>
   *
   * <P>Selection is implemented using a {@link Comparator}. The smallest
   * candidate according to the defined ordering gets selected.</P>
   *
   * @see PreselectingMethod
   */
  public enum SelectingMethod
  {
    /**
     * Chooses the candidate with the highest proportion of local events.
     */
    MaxL {
      @Override
      Comparator<Candidate> createComparator(final OPConflictChecker checker)
      {
        return checker.new ComparatorMaxL();
      }
    },
    /**
     * Chooses the candidate with the highest proportion of common events.
     * An event is considered as common if it is used by at least two
     * automata of the candidate.
     */
    MaxC {
      @Override
      Comparator<Candidate> createComparator(final OPConflictChecker checker)
      {
        return checker.new ComparatorMaxC();
      }
    },
    /**
     * Chooses the candidate with the minimum estimated number of
     * precondition-marked states in the synchronous product.
     */
    MinSa {
      @Override
      Comparator<Candidate> createComparator(final OPConflictChecker checker)
      {
        if (checker.mPreconditionMarking == null) {
          return null;
        } else {
          return checker.new ComparatorMinSAlpha();
        }
      }
      @Override
      SelectingHeuristic createHeuristic(final OPConflictChecker checker)
      {
        if (checker.mPreconditionMarking == null) {
          return MinS.createHeuristic(checker);
        } else {
          return super.createHeuristic(checker);
        }
      }

    },
    /**
     * Chooses the candidate with the minimum estimated number of states
     * in the synchronous product.
     */
    MinS {
      @Override
      Comparator<Candidate> createComparator(final OPConflictChecker checker)
      {
        return checker.new ComparatorMinS();
      }
    },
    /**
     * Chooses the candidate with the minimum actual number of states
     * in the synchronous product.
     */
    MinSync {
      @Override
      SelectingHeuristic createHeuristic(final OPConflictChecker checker)
      {
        final Comparator<Candidate> alt = MinS.createComparatorChain(checker);
        return checker.new HeuristicMinSync(alt);
      }
    };

    /**
     * Creates a comparator to implement this selecting heuristic.
     * This returns an implementation of only one heuristic, which
     * may consider two candidates as equal.
     * @param  checker The conflict checker requesting and using the
     *                heuristic.
     * @return A comparator, or <CODE>null</CODE> if the heuristic
     *         is not implemented by a comparator.
     */
    Comparator<Candidate> createComparator(final OPConflictChecker checker)
    {
      return null;
    }

    /**
     * Creates a comparator to implement this selecting heuristic.
     * The returned comparator first compares candidates according to this
     * selection methods. If two candidates are found equal, all other enabled
     * selection heuristics are used, in the order in which they are
     * defined in the enumeration. If the candidates are equal under
     * all heuristics, they are compared based on their names. This
     * guarantees that no two candidates are equal.
     * @param checker The conflict checker requesting and using the comparator.
     */
    Comparator<Candidate> createComparatorChain(final OPConflictChecker checker)
    {
      final List<Comparator<Candidate>> list =
        new LinkedList<Comparator<Candidate>>();
      Comparator<Candidate> heu = createComparator(checker);
      list.add(heu);
      for (final SelectingMethod method : values()) {
        if (method != this) {
          heu = method.createComparator(checker);
          if (heu != null) {
            list.add(heu);
          }
        }
      }
      return checker.new ComparatorChain(list);
    }

    /**
     * Creates a selecting heuristic that gives preferences to this method.
     * The returned heuristic first compares candidates according to this
     * selection methods. If two candidates are found equal, all other enabled
     * selection heuristics are used, in the order in which they are
     * defined in the enumeration. If the candidates are equal under
     * all heuristics, they are compared based on their names. This
     * guarantees that no two candidates are equal.
     * @param checker The conflict checker requesting and using the
     *                heuristic.
     */
    SelectingHeuristic createHeuristic(final OPConflictChecker checker)
    {
      final Comparator<Candidate> chain = createComparatorChain(checker);
      return checker.new SelectingHeuristic(chain);
    }

  }


  //#########################################################################
  //# Inner Class SubSystem
  /**
   * A collection of automata and associated events.
   * This class is used to store subsystems to be checked later.
   * Essentially it holds the contents of a {@link ProductDESProxy},
   * but in a more lightweight form.
   */
  private static class SubSystem
    implements Comparable<SubSystem>
  {

    //#######################################################################
    //# Constructors
    private SubSystem(final AutomatonProxy aut, final int limit)
    {
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      mEvents = new ArrayList<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        if (event.getKind() != EventKind.PROPOSITION) {
          mEvents.add(event);
        }
      }
      mAutomata = new ArrayList<AutomatonProxy>(1);
      mAutomata.add(aut);
      mStateLimit = limit;
    }

    private SubSystem(final List<EventProxy> events,
                      final List<AutomatonProxy> automata,
                      final int limit)
    {
      mEvents = events;
      mAutomata = automata;
      mStateLimit = limit;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SubSystem>
    public int compareTo(final SubSystem other)
    {
      final int aut1 = mAutomata.size();
      final int aut2 = other.mAutomata.size();
      if (aut1 != aut2) {
        return aut1 - aut2;
      }
      final int events1 = mEvents.size();
      final int events2 = other.mEvents.size();
      if (events1 != events2) {
        return events1 - events2;
      }
      final String name1 = Candidate.getCompositionName(mAutomata);
      final String name2 = Candidate.getCompositionName(other.mAutomata);
      return name1.compareTo(name2);
    }

    //#######################################################################
    //# Simple Access
    private List<EventProxy> getEvents()
    {
      return mEvents;
    }

    private List<AutomatonProxy> getAutomata()
    {
      return mAutomata;
    }

    private int getStateLimit()
    {
      return mStateLimit;
    }

    //#######################################################################
    //# Data Members
    private final List<EventProxy> mEvents;
    private final List<AutomatonProxy> mAutomata;
    private final int mStateLimit;

  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  /**
   * A record to store information about an automaton.
   * The automaton information record contains the number of precondition
   * and default markings.
   */
  private class AutomatonInfo
  {

    //#######################################################################
    //# Constructor
    private AutomatonInfo(final AutomatonProxy aut)
    {
      mAutomaton = aut;
      mNumPreconditionMarkedStates = mNumDefaultMarkedStates =
        mNumPreconditionTransitions -1;
    }

    //#######################################################################
    //# Access Methods
    private boolean isNeverPreconditionMarked()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates == 0;
    }

    private boolean isAlwaysPreconditionMarked()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates == mAutomaton.getStates().size();
    }

    private int getNumberOfPreconditionMarkedStates()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates;
    }

    private boolean isNeverDefaultMarked()
    {
      if (mNumDefaultMarkedStates < 0) {
        countPropositions();
      }
      return mNumDefaultMarkedStates == 0;
    }

    private boolean isAlwaysDefaultMarked()
    {
      if (mNumDefaultMarkedStates < 0) {
        countPropositions();
      }
      return mNumDefaultMarkedStates == mAutomaton.getStates().size();
    }

    private int getNumberOfPreconditionMarkedTransitions()
    {
      if (mNumPreconditionTransitions < 0) {
        final Collection<TransitionProxy> transitions =
          mAutomaton.getTransitions();
        if (isNeverPreconditionMarked()) {
          mNumPreconditionTransitions = 0;
        } else if (isAlwaysPreconditionMarked()) {
          mNumPreconditionTransitions = 2 * transitions.size();
        } else {
          final EventProxy alpha = mPreconditionMarking;
          mNumPreconditionTransitions = 0;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource().getPropositions().contains(alpha)) {
              mNumPreconditionTransitions++;
            }
            if (trans.getTarget().getPropositions().contains(alpha)) {
              mNumPreconditionTransitions++;
            }
          }
        }
      }
      return mNumPreconditionTransitions;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void countPropositions()
    {
      final EventProxy alpha = mPreconditionMarking;
      final EventProxy omega = mCurrentDefaultMarking;
      boolean usesAlpha = false;
      boolean usesOmega = false;
      for (final EventProxy event : mAutomaton.getEvents()) {
        if (event == omega) {
          usesOmega = true;
          if (usesAlpha || alpha == null) {
            break;
          }
        } else if (event == alpha) {
          usesAlpha = true;
          if (usesOmega) {
            break;
          }
        }
      }
      final Collection<StateProxy> states = mAutomaton.getStates();
      final int numStates = states.size();
      mNumDefaultMarkedStates = usesOmega ? 0 : numStates;
      mNumPreconditionMarkedStates = usesAlpha ? 0 : numStates;
      boolean hasinit = false;
      for (final StateProxy state : states) {
        hasinit |= state.isInitial();
        if (usesAlpha || usesOmega) {
          boolean containsAlpha = false;
          boolean containsOmega = false;
          for (final EventProxy prop : state.getPropositions()) {
            if (prop == omega) {
              containsOmega = true;
            } else if (prop == alpha) {
              containsAlpha = true;
            }
          }
          if (containsAlpha && usesAlpha) {
            mNumPreconditionMarkedStates++;
          }
          if (containsOmega && usesOmega) {
            mNumDefaultMarkedStates++;
          }
        }
      }
      if (!hasinit) {
        mNumPreconditionMarkedStates = 0;
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private int mNumPreconditionMarkedStates;
    private int mNumDefaultMarkedStates;
    private int mNumPreconditionTransitions;

  }


  //#########################################################################
  //# Inner Class EventInfo
  /**
   * A record to store information about the automata an event occurs in.
   * The event information record basically consists of the set of automata
   * it occurs in, plus information in which automata the event only appears
   * as selfloops.
   */
  private static class EventInfo
  {

    //#######################################################################
    //# Constructor
    private EventInfo()
    {
      mAutomataMap = new TObjectByteHashMap<AutomatonProxy>();
      mNumNonSelfloopAutomata = 0;
      mIsBlocked = false;
    }

    //#######################################################################
    //# Simple Access
    private boolean addAutomataTo(final Collection<AutomatonProxy> target)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      boolean added = false;
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        added |= target.add(aut);
      }
      return added;
    }

    private void addAutomaton(final AutomatonProxy aut, final byte status)
    {
      final byte present = mAutomataMap.get(aut);
      if (present != status) {
        mAutomataMap.put(aut, status);
        if (present == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
        if (status == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata++;
        }
        mIsBlocked |= status == BLOCKED;
      }
    }

    private boolean containedIn(final Collection<AutomatonProxy> automata)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        if (!automata.contains(aut)) {
          return false;
        }
      }
      return true;
    }

    private List<AutomatonProxy> getAutomataList()
    {
      final int size = mAutomataMap.size();
      final AutomatonProxy[] automata = new AutomatonProxy[size];
      mAutomataMap.keys(automata);
      return Arrays.asList(automata);
    }

    private Set<AutomatonProxy> getAutomataSet()
    {
      final int size = mAutomataMap.size();
      final Set<AutomatonProxy> automata = new THashSet<AutomatonProxy>(size);
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        automata.add(aut);
      }
      return automata;
    }

    private int getNumberOfAutomata()
    {
      return mAutomataMap.size();
    }

    private boolean intersects(final Collection<AutomatonProxy> automata)
    {
      for (final AutomatonProxy aut : automata) {
        if (mAutomataMap.containsKey(aut)) {
          return true;
        }
      }
      return false;
    }

    private boolean isEmpty()
    {
      return mAutomataMap.isEmpty();
    }

    private boolean isRemovable()
    {
      return mIsBlocked || mNumNonSelfloopAutomata == 0;
    }

    private void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      for (final AutomatonProxy aut : victims) {
        final byte code = mAutomataMap.remove(aut);
        if (code == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
      }
    }

    private boolean replaceAutomaton(final AutomatonProxy oldAut,
                                     final AutomatonProxy newAut)
    {
      final byte code = mAutomataMap.remove(oldAut);
      if (code == UNKNOWN_SELFLOOP) {
        // not found in map ...
        return false;
      } else {
        mAutomataMap.put(newAut, code);
        return true;
      }
    }

    //#######################################################################
    //# Data Members
    private final TObjectByteHashMap<AutomatonProxy> mAutomataMap;
    private int mNumNonSelfloopAutomata;
    private boolean mIsBlocked;

  }


  //#########################################################################
  //# Local Interface PreselectingHeuristic
  private interface PreselectingHeuristic
  {
    public Collection<Candidate> findCandidates();
  }


  //#########################################################################
  //# Inner Class PairingHeuristic
  private abstract class PairingHeuristic
    implements PreselectingHeuristic, Comparator<AutomatonProxy>
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    public Collection<Candidate> findCandidates()
    {
      final AutomatonProxy chosenAut = Collections.min(mCurrentAutomata, this);
      return pairAutomaton(chosenAut, mCurrentAutomata);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Collection<Candidate> pairAutomaton
      (final AutomatonProxy chosenAut,
       final Collection<AutomatonProxy> automata)
    {
      final Set<EventProxy> chosenEvents =
        new THashSet<EventProxy>(chosenAut.getEvents());
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
      for (final AutomatonProxy aut : automata) {
        if (aut != chosenAut && synchronises(chosenEvents, aut.getEvents())) {
          final List<AutomatonProxy> pair = new ArrayList<AutomatonProxy>(2);
          if (chosenAut.compareTo(aut) < 0) {
            pair.add(chosenAut);
            pair.add(aut);
          } else {
            pair.add(aut);
            pair.add(chosenAut);
          }
          if (isPermissibleCandidate(pair)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(pair);
            final Candidate candidate = new Candidate(pair, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }

    private boolean synchronises(final Set<EventProxy> set,
                                 final Collection<EventProxy> collection)
    {
      final KindTranslator translator = getKindTranslator();
      for (final EventProxy event : collection) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION &&
            set.contains(event)) {
          return true;
        }
      }
      return false;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicMinT
  private class HeuristicMinT
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans1 - numtrans2;
      }
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates1 - numstates2;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMinTAlpha
  private class HeuristicMinTAlpha
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numalpha1 =
        getAutomatonInfo(aut1).getNumberOfPreconditionMarkedTransitions();
      final int numalpha2 =
        getAutomatonInfo(aut2).getNumberOfPreconditionMarkedTransitions();
      if (numalpha1 != numalpha2) {
        return numalpha1 - numalpha2;
      }
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans1 - numtrans2;
      }
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates1 - numstates2;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMaxS
  /**
   * Performs step 1 of the approach to select the automata to compose. A
   * candidate is produced by pairing the automaton with the most states to
   * every other automaton in the model.
   */
  private class HeuristicMaxS
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates2 - numstates1;
      }
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans2 - numtrans1;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMustL
  private class HeuristicMustL
    implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    public Collection<Candidate> findCandidates()
    {
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
      for (final EventInfo info : mEventInfoMap.values()) {
        assert info.getNumberOfAutomata() > 0;
        if (info.getNumberOfAutomata() > 1) {
          final List<AutomatonProxy> list = info.getAutomataList();
          Collections.sort(list);
          if (isPermissibleCandidate(list)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(list);
            final Candidate candidate = new Candidate(list, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class SelectingHeuristic
  private class SelectingHeuristic {

    //#######################################################################
    //# Constructor
    private SelectingHeuristic(final Comparator<Candidate> comparator)
    {
      mComparator = comparator;
    }

    //#######################################################################
    //# Candidate Evaluation
    Comparator<Candidate> getComparator()
    {
      return mComparator;
    }

    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      return Collections.min(candidates, mComparator);
    }

    //#######################################################################
    //# Data Members
    private final Comparator<Candidate> mComparator;

  }


  //#########################################################################
  //# Inner Class HeuristicMinSync
  private class HeuristicMinSync extends SelectingHeuristic {

    //#######################################################################
    //# Constructor
    private HeuristicMinSync(final Comparator<Candidate> comparator)
    {
      super(comparator);
    }

    //#######################################################################
    //# Overrides for SelectingHeuristic
    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      final ProductDESProxyFactory factory = getFactory();
      final List<Candidate> list = new ArrayList<Candidate>(candidates);
      final Comparator<Candidate> comparator = getComparator();
      Collections.sort(list, comparator);
      int limit = mCurrentInternalStateLimit;
      mCurrentSynchronousProductBuilder.setNodeLimit(limit);
      mCurrentSynchronousProductBuilder.setConstructsAutomaton(false);
      Candidate best = null;
      for (final Candidate candidate : list) {
        final ProductDESProxy des = candidate.createProductDESProxy(factory);
        mCurrentSynchronousProductBuilder.setModel(des);
        try {
          mCurrentSynchronousProductBuilder.run();
          final AnalysisResult result =
            mCurrentSynchronousProductBuilder.getAnalysisResult();
          final double dsize = result.getTotalNumberOfStates();
          final int size = (int) Math.round(dsize);
          if (size < limit || best == null) {
            best = candidate;
            limit = size;
            mCurrentSynchronousProductBuilder.setNodeLimit(limit);
          }
        } catch (final OverflowException overflow) {
          // skip this one ...
        } finally {
          final CompositionalVerificationResult stats = getAnalysisResult();
          final AutomatonResult result =
            mCurrentSynchronousProductBuilder.getAnalysisResult();
          stats.addSynchronousProductAnalysisResult(result);
        }
      }
      return best;
    }

  }


  //#########################################################################
  //# Inner Class SelectingComparator
  private abstract class SelectingComparator
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      final double heu1 = getHeuristicValue(cand1);
      final double heu2 = getHeuristicValue(cand2);
      if (heu1 < heu2) {
        return -1;
      } else if (heu1 > heu2) {
        return 1;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    abstract double getHeuristicValue(final Candidate candidate);

  }


  //#########################################################################
  //# Inner Class ComparatorMaxL
  private class ComparatorMaxL extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getLocalEventCount() /
               (double) candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMaxC
  private class ComparatorMaxC extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getCommonEventCount() /
               (double) candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMinS
  private class ComparatorMinS extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMinSAlpha
  private class ComparatorMinSAlpha extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        final AutomatonInfo info = getAutomatonInfo(aut);
        product *= info.getNumberOfPreconditionMarkedStates();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }

  }


  //#########################################################################
  //# Inner Class ComparatorChain
  private class ComparatorChain
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Constructor
    private ComparatorChain(final List<Comparator<Candidate>> list)
    {
      mHeuristics = list;
    }

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      for (final Comparator<Candidate> heu : mHeuristics) {
        final int result = heu.compare(cand1, cand2);
        if (result != 0) {
          return result;
        }
      }
      return cand1.compareTo(cand2);
    }

    //#######################################################################
    //# Data Members
    private final List<Comparator<Candidate>> mHeuristics;

  }


  //#########################################################################
  //# Inner Class AbstractionRule
  private abstract class AbstractionRule
    implements Abortable
  {

    //#######################################################################
    //# Rule Application
    abstract AbstractionStep applyRule(final AutomatonProxy aut,
                                       final EventProxy tau)
      throws AnalysisException;

    abstract void storeStatistics();

    abstract void resetStatistics();

    //#######################################################################
    //# Trace Recovery
    EventProxy getUsedPreconditionMarking()
    {
      return null;
    }

    BitSet recoverMarkings(final AutomatonProxy aut, final EventProxy tau)
    throws AnalysisException
    {
      return null;
    }

  }


  //#########################################################################
  //# Inner Class TRSimplifierAbstractionRule
  private class TRSimplifierAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Constructor
    TRSimplifierAbstractionRule(final TransitionRelationSimplifier simplifier)
    {
      mSimplifier = simplifier;
    }

    //#######################################################################
    //# Overrides for AbstractionRule
    AbstractionStep applyRule(final AutomatonProxy aut, final EventProxy tau)
      throws AnalysisException
    {
      try {
        final EventEncoding eventEnc = createEventEncoding(aut, tau);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final int config = mSimplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int numStates = rel.getNumberOfStates();
        final int numTrans = rel.getNumberOfTransitions();
        final int numMarkings = rel.getNumberOfMarkings();
        mSimplifier.setTransitionRelation(rel);
        if (mSimplifier.run()) {
          if (rel.getNumberOfReachableStates() == numStates &&
              rel.getNumberOfTransitions() == numTrans &&
              rel.getNumberOfMarkings() == numMarkings) {
            return null;
          }
          rel.removeRedundantPropositions();
          final ProductDESProxyFactory factory = getFactory();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          return createStep
            (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
        } else {
          return null;
        }
      } finally {
        mSimplifier.reset();
      }
    }

    @Override
    void storeStatistics()
    {
      final CompositionalVerificationResult result = getAnalysisResult();
      result.setSimplifierStatistics(mSimplifier);
    }

    @Override
    void resetStatistics()
    {
      mSimplifier.createStatistics();
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#######################################################################
    //# Simple Access
    TransitionRelationSimplifier getSimplifier()
    {
      return mSimplifier;
    }

    //#######################################################################
    //# Auxiliary Methods
    EventEncoding createEventEncoding(final AutomatonProxy aut,
                                      final EventProxy tau)
    {
      final KindTranslator translator = getKindTranslator();
      final EventEncoding eventEnc =
        new EventEncoding(aut, translator, tau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      final int markingID = eventEnc.getEventCode(mCurrentDefaultMarking);
      mSimplifier.setDefaultMarkingID(markingID);
      return eventEnc;
    }

    AbstractionStep createStep(final AutomatonProxy input,
                               final StateEncoding inputStateEnc,
                               final AutomatonProxy output,
                               final StateEncoding outputStateEnc,
                               final EventProxy tau)
    {
      final List<int[]> partition = mSimplifier.getResultPartition();
      if (mSimplifier.isObservationEquivalentAbstraction()) {
        return new ObservationEquivalenceStep(output, input, tau,
                                              inputStateEnc, partition,
                                              false, outputStateEnc);
      } else {
        return new ConflictEquivalenceStep(output, input, tau,
                                           inputStateEnc, partition,
                                           false, outputStateEnc);
      }
    }

    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;

  }

  //#########################################################################
  //# Inner Class StandardTRSimplifierAbstractionRule
  private class StandardTRSimplifierAbstractionRule
    extends TRSimplifierAbstractionRule
  {

    //#######################################################################
    //# Constructor
    private StandardTRSimplifierAbstractionRule
      (final ChainTRSimplifier chain, final int ccindex)
    {
      super(chain);
      mCertainConflictsIndex = ccindex;
      if (ccindex >= 0) {
        mCertainConflictsSimplifier =
          (LimitedCertainConflictsTRSimplifier) chain.getStep(ccindex);
      }
    }

    //#######################################################################
    //# Simple Access
    @Override
    ChainTRSimplifier getSimplifier()
    {
      return (ChainTRSimplifier) super.getSimplifier();
    }

    LimitedCertainConflictsTRSimplifier getCertainConflictsSimplifier()
    {
      return mCertainConflictsSimplifier;
    }

    int getCertainConflictsIndex()
    {
      return mCertainConflictsIndex;
    }

    //#######################################################################
    //# Auxiliary Methods
    @Override
    EventEncoding createEventEncoding(final AutomatonProxy aut,
                                      final EventProxy tau)
    {
      final EventEncoding eventEnc = super.createEventEncoding(aut, tau);
      int markingID = eventEnc.getEventCode(mCurrentDefaultMarking);
      if (markingID < 0) {
        final KindTranslator translator = getKindTranslator();
        markingID =
          eventEnc.addEvent(mCurrentDefaultMarking, translator, true);
        final TransitionRelationSimplifier simplifier = getSimplifier();
        simplifier.setDefaultMarkingID(markingID);
      }
      return eventEnc;
    }

    @Override
    AbstractionStep createStep(final AutomatonProxy input,
                               final StateEncoding inputStateEnc,
                               final AutomatonProxy output,
                               final StateEncoding outputStateEnc,
                               final EventProxy tau)
    {
      if (mCertainConflictsSimplifier != null &&
          mCertainConflictsSimplifier.hasRemovedTransitions()) {
        final ChainTRSimplifier chain = getSimplifier();
        boolean oeq1 = true;
        for (int index = 0; index < mCertainConflictsIndex; index++) {
          final TransitionRelationSimplifier simp = chain.getStep(index);
          oeq1 &= simp.isObservationEquivalentAbstraction();
        }
        final int size = chain.size();
        boolean oeq2 = true;
        for (int index = mCertainConflictsIndex + 1; index < size; index++) {
          final TransitionRelationSimplifier simp = chain.getStep(index);
          oeq2 &= simp.isObservationEquivalentAbstraction();
        }
        final List<int[]> partition = chain.getResultPartition();
        return new CertainConflictsStep(output, input, tau, inputStateEnc,
                                        partition, outputStateEnc, oeq1, oeq2);
      } else {
        return super.createStep(input, inputStateEnc,
                                output, outputStateEnc, tau);
      }
    }

    //#######################################################################
    //# Data Members
    private LimitedCertainConflictsTRSimplifier mCertainConflictsSimplifier;
    private final int mCertainConflictsIndex;

  }


  //#########################################################################
  //# Inner Class GeneralisedTRSimplifierAbstractionRule
  private class GeneralisedTRSimplifierAbstractionRule
    extends TRSimplifierAbstractionRule
  {

    //#######################################################################
    //# Constructor
    private GeneralisedTRSimplifierAbstractionRule
      (final ChainTRSimplifier simplifier)
    {
      this(simplifier, -1);
    }

    private GeneralisedTRSimplifierAbstractionRule
      (final ChainTRSimplifier simplifier, final int recoveryIndex)
    {
      super(simplifier);
      mRecoveryIndex = recoveryIndex;
      final EventProxy[] props = new EventProxy[2];
      mUsedDefaultMarking = props[0] = mCurrentDefaultMarking;
      if (mPreconditionMarking == null) {
        final ProductDESProxyFactory factory = getFactory();
        mUsedPreconditionMarking = props[1] =
          factory.createEventProxy(":alpha", EventKind.PROPOSITION);
      } else {
        mUsedPreconditionMarking = props[1] = mPreconditionMarking;
      }
      mPropositions = Arrays.asList(props);
    }

    //#######################################################################
    //# Simple Access
    @Override
    EventProxy getUsedPreconditionMarking()
    {
      return mUsedPreconditionMarking;
    }

    @Override
    ChainTRSimplifier getSimplifier()
    {
      return (ChainTRSimplifier) super.getSimplifier();
    }

    @Override
    MergeStep createStep(final AutomatonProxy input,
                         final StateEncoding inputStateEnc,
                         final AutomatonProxy output,
                         final StateEncoding outputStateEnc,
                         final EventProxy tau)
    {
      final ChainTRSimplifier simplifier = getSimplifier();
      final List<int[]> partition = simplifier.getResultPartition();
      final boolean reduced =
        simplifier.isReducedMarking(mPreconditionMarkingID);
      if (simplifier.isObservationEquivalentAbstraction()) {
        return new ObservationEquivalenceStep(output, input, tau,
                                              inputStateEnc, partition,
                                              reduced, outputStateEnc);
      } else {
        return new ConflictEquivalenceStep(output, input, tau,
                                           inputStateEnc, partition,
                                           reduced, outputStateEnc);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    @Override
    EventEncoding createEventEncoding(final AutomatonProxy aut,
                                      final EventProxy tau)
    {
      final KindTranslator translator = getKindTranslator();
      final EventEncoding eventEnc =
        new EventEncoding(aut, translator, tau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      mPreconditionMarkingID =
        eventEnc.getEventCode(mUsedPreconditionMarking);
      if (mPreconditionMarkingID < 0) {
        mPreconditionMarkingID =
          eventEnc.addEvent(mUsedPreconditionMarking, translator, true);
      }
      mDefaultMarkingID = eventEnc.getEventCode(mUsedDefaultMarking);
      if (mDefaultMarkingID < 0) {
        mDefaultMarkingID =
          eventEnc.addEvent(mUsedDefaultMarking, translator, true);
      }
      final TransitionRelationSimplifier simplifier = getSimplifier();
      simplifier.setPropositions(mPreconditionMarkingID, mDefaultMarkingID);
      return eventEnc;
    }

    @Override
    BitSet recoverMarkings(final AutomatonProxy aut, final EventProxy tau)
    throws AnalysisException
    {
      try {
        final EventEncoding eventEnc = createEventEncoding(aut, tau);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final ChainTRSimplifier simplifier = getSimplifier();
        final int config = simplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int origNumStates = rel.getNumberOfStates();
        simplifier.setTransitionRelation(rel);
        simplifier.runTo(mRecoveryIndex);
        final BitSet result = new BitSet(origNumStates);
        final List<int[]> partition = simplifier.getResultPartition();
        if (partition == null) {
          for (int state = 0; state < origNumStates; state++) {
            if (rel.isMarked(state, mPreconditionMarkingID)) {
              result.set(state);
            }
          }
        } else {
          final int reducedNumStates = rel.getNumberOfStates();
          for (int state = 0; state < reducedNumStates; state++) {
            if (rel.isMarked(state, mPreconditionMarkingID)) {
              for (final int member : partition.get(state)) {
                result.set(member);
              }
            }
          }
        }
        return result;
      } finally {
        final ChainTRSimplifier simplifier = getSimplifier();
        simplifier.reset();
      }
    }

    //#######################################################################
    //# Data Members
    private final int mRecoveryIndex;
    private final List<EventProxy> mPropositions;
    private EventProxy mUsedPreconditionMarking;
    private final EventProxy mUsedDefaultMarking;
    private int mPreconditionMarkingID;
    private int mDefaultMarkingID;
  }


  //#########################################################################
  //# Inner Class ObserverProjectionAbstractionRule
  private class ObserverProjectionAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Constructors
    private ObserverProjectionAbstractionRule()
    {
      mSimplifier = new ObservationEquivalenceTRSimplifier();
      mSimplifier.setAppliesPartitionAutomatically(false);
    }

    //#######################################################################
    //# Rule Application
    ObservationEquivalenceStep applyRule(final AutomatonProxy aut,
                                         final EventProxy tau)
    throws AnalysisException
    {
      try {
        final ProductDESProxyFactory factory = getFactory();
        final String name = "vtau:" + aut.getName();
        final KindTranslator translator = getKindTranslator();
        final EventProxy vtau =
          factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
        final EventEncoding eventEnc =
          new EventEncoding(aut, translator, tau, mPropositions,
                            EventEncoding.FILTER_PROPOSITIONS);
        final KindTranslator id = IdenticalKindTranslator.getInstance();
        final int codeOfVTau = eventEnc.addEvent(vtau, id, false);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation
            (aut, eventEnc, inputStateEnc,
             ListBufferTransitionRelation.CONFIG_PREDECESSORS);
        mSimplifier.setTransitionRelation(rel);
        final List<int[]> partition =
          applySimplifier(mSimplifier, rel, codeOfVTau);
        if (partition != null) {
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          /*
          final IsomorphismChecker checker =
            new IsomorphismChecker(factory, false);
          checker.checkObservationEquivalence(aut, convertedAut, tau);
           */
          return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                                inputStateEnc, partition,
                                                outputStateEnc);
        } else {
          return null;
        }
      } catch (final OutOfMemoryError error) {
        mSimplifier.reset();
        throw new OverflowException(error);
      } finally {
        mSimplifier.reset();
      }
    }

    @Override
    void storeStatistics()
    {
      final CompositionalVerificationResult result = getAnalysisResult();
      result.setSimplifierStatistics(mSimplifier);
    }

    @Override
    void resetStatistics()
    {
      mSimplifier.createStatistics();
    }

    //#######################################################################
    //# Auxiliary Methods
    private List<int[]> applySimplifier
      (final ObservationEquivalenceTRSimplifier simplifier,
       final ListBufferTransitionRelation rel,
       final int vtau)
      throws AnalysisException
    {
      final int tau = EventEncoding.TAU;
      final int numTransBefore = rel.getNumberOfTransitions();
      List<int[]> partition;
      while (true) {
        final boolean modified = simplifier.run();
        if (!modified && rel.getNumberOfTransitions() == numTransBefore) {
          return null;
        }
        partition = simplifier.getResultPartition();
        if (partition == null) {
          break;
        } else if (!makeEventsVisible(rel, partition, vtau)) {
          break;
        }
        simplifier.setInitialPartition(partition);
      }
      simplifier.applyResultPartition();
      simplifier.reset();
      rel.replaceEvent(vtau, tau);
      rel.removeEvent(vtau);
      rel.removeRedundantPropositions();
      return partition;
    }

    private boolean makeEventsVisible
      (final ListBufferTransitionRelation rel,
       final List<int[]> partition,
       final int vtau)
    {
      final int numStates = rel.getNumberOfStates();
      final TIntIntHashMap pmap = new TIntIntHashMap(numStates);
      int code = 0;
      for (final int[] array : partition) {
        for (final int state : array) {
          pmap.put(state, code);
        }
        code++;
      }
      final TransitionIterator iter =
        rel.createPredecessorsModifyingIterator();
      final TIntArrayList victims = new TIntArrayList();
      final int tau = EventEncoding.TAU;
      boolean modified = false;
      for (int target= 0; target < numStates; target++) {
        if (rel.isReachable(target)) {
          final int targetClass = pmap.get(target);
          iter.reset(target, tau);
          while (iter.advance()) {
            final int source = iter.getCurrentSourceState();
            final int sourceClass = pmap.get(source);
            if (sourceClass != targetClass) {
              iter.remove();
              victims.add(source);
            }
          }
          if (!victims.isEmpty()) {
            modified = true;
            rel.addTransitions(victims, vtau, target);
            victims.clear();
          }
        }
      }
      return modified;
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#########################################################################
    //# Data Members
    private final ObservationEquivalenceTRSimplifier mSimplifier;

  }


  //#########################################################################
  //# Inner Class OPSearchAbstractionRule
  private class OPSearchAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Constructors
    private OPSearchAbstractionRule()
    {
      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      mSimplifier = new OPSearchAutomatonSimplifier(factory, translator);
      mSimplifier.setPropositions(mPropositions);
    }

    //#######################################################################
    //# Rule Application
    ObservationEquivalenceStep applyRule(final AutomatonProxy aut,
                                         final EventProxy tau)
    throws AnalysisException
    {
      try {
        if (tau == null) {
          return null;
        }
        final Collection<EventProxy> hidden = Collections.singletonList(tau);
        mSimplifier.setModel(aut);
        mSimplifier.setHiddenEvents(hidden);
        mSimplifier.setOutputHiddenEvent(tau);
        mSimplifier.setNodeLimit(mCurrentInternalStateLimit);
        mSimplifier.run();
        final PartitionedAutomatonResult result =
          mSimplifier.getAnalysisResult();
        final AutomatonProxy convertedAut = result.getAutomaton();
        if (aut == convertedAut) {
          return null;
        }
        final StateEncoding inputEnc = result.getInputEncoding();
        final StateEncoding outputEnc = result.getOutputEncoding();
        final List<int[]> partition = result.getPartition();
        return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                              inputEnc, partition, outputEnc);
      } catch (final OutOfMemoryError error) {
        mSimplifier.tearDown();
        throw new OverflowException(error);
      } finally {
        mSimplifier.tearDown();
      }
    }

    @Override
    void storeStatistics()
    {
    }

    @Override
    void resetStatistics()
    {
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#########################################################################
    //# Data Members
    private final OPSearchAutomatonSimplifier mSimplifier;

  }


  //#########################################################################
  //# Inner Class AbstractionStep
  private abstract class AbstractionStep
  {

    //#######################################################################
    //# Constructors
    AbstractionStep(final List<AutomatonProxy> results,
                    final List<AutomatonProxy> originals)
    {
      mResultAutomata = results;
      mOriginalAutomata = originals;
    }

    AbstractionStep(final AutomatonProxy result,
                    final Collection<AutomatonProxy> originals)
    {
      this(Collections.singletonList(result),
           new ArrayList<AutomatonProxy>(originals));
    }

    AbstractionStep(final AutomatonProxy result,
                    final AutomatonProxy original)
    {
      this(Collections.singletonList(result),
           Collections.singletonList(original));
    }

    //#######################################################################
    //# Simple Access
    List<AutomatonProxy> getResultAutomata()
    {
      return mResultAutomata;
    }

    AutomatonProxy getResultAutomaton()
    {
      if (mResultAutomata.size() == 1) {
        return mResultAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single result automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mResultAutomata.size() + " result automata!");
      }
    }

    List<AutomatonProxy> getOriginalAutomata()
    {
      return mOriginalAutomata;
    }

    AutomatonProxy getOriginalAutomaton()
    {
      if (mOriginalAutomata.size() == 1) {
        return mOriginalAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single input automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mOriginalAutomata.size() + " input automata!");
      }
    }

    //#######################################################################
    //# Trace Computation
    /**
     * Converts the given trace on the result of this rule application
     * to a trace on the original automaton before abstraction.
     * Assumes that a saturated trace is being passed.
     */
    abstract List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps) throws AnalysisException;

    //#######################################################################
    //# Data Members
    private final List<AutomatonProxy> mResultAutomata;
    private final List<AutomatonProxy> mOriginalAutomata;

  }


  //#########################################################################
  //# Inner Class EventRemovalStep
  private class EventRemovalStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private EventRemovalStep(final List<AutomatonProxy> results,
                             final List<AutomatonProxy> originals)
    {
      super(results, originals);
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    {
      final List<AutomatonProxy> results = getResultAutomata();
      final List<AutomatonProxy> originals = getOriginalAutomata();
      final int numAutomata = results.size();
      final Map<AutomatonProxy,AutomatonProxy> autMap =
        new HashMap<AutomatonProxy,AutomatonProxy>(numAutomata);
      final Iterator<AutomatonProxy> resultIter = results.iterator();
      final Iterator<AutomatonProxy> originalIter = originals.iterator();
      while (resultIter.hasNext()) {
        final AutomatonProxy result = resultIter.next();
        final AutomatonProxy original = originalIter.next();
        autMap.put(result, original);
      }
      final ProductDESProxyFactory factory = getFactory();
      final ListIterator<TraceStepProxy> iter = steps.listIterator();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final int size = stepMap.size();
        final Map<AutomatonProxy,StateProxy> newStepMap =
          new HashMap<AutomatonProxy,StateProxy>(size);
        for (final Map.Entry<AutomatonProxy,StateProxy> entry :
             stepMap.entrySet()) {
          final AutomatonProxy aut = entry.getKey();
          AutomatonProxy newAut = autMap.get(aut);
          if (newAut == null) {
            newAut = aut;
          }
          final StateProxy state = entry.getValue();
          newStepMap.put(newAut, state);
        }
        final EventProxy event = step.getEvent();
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newStepMap);
        iter.set(newStep);
      }
      return steps;
    }

  }


  //#########################################################################
  //# Inner Class HidingStep
  private class HidingStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private HidingStep(final AutomatonProxy composedAut,
                       final AutomatonProxy originalAut,
                       final Collection<EventProxy> localEvents,
                       final EventProxy tau)
    {
      super(composedAut, originalAut);
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = null;
    }

    private HidingStep(final AutomatonProxy composedAut,
                       final Collection<EventProxy> localEvents,
                       final EventProxy tau,
                       final SynchronousProductStateMap stateMap)
    {
      super(composedAut, stateMap.getInputAutomata());
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = stateMap;
    }

    //#######################################################################
    //# Simple Access
    EventProxy getHiddenEvent()
    {
      return mHiddenEvent;
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final Collection<AutomatonProxy> originalAutomata = getOriginalAutomata();
      final int convertedNumAutomata =
        steps.iterator().next().getStateMap().size() +
        originalAutomata.size() - 1;
      Map<AutomatonProxy,StateProxy> previousMap = null;
      final ListIterator<TraceStepProxy> iter = steps.listIterator();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>(convertedNumAutomata);
        convertedStepMap.putAll(stepMap);
        final StateProxy convertedState =
          convertedStepMap.remove(resultAutomaton);
        for (final AutomatonProxy aut : originalAutomata) {
          final StateProxy originalState =
            getOriginalState(convertedState, aut);
          convertedStepMap.put(aut, originalState);
        }
        EventProxy event = step.getEvent();
        if (event != null && event == mHiddenEvent) {
          event = findEvent(previousMap, convertedStepMap);
        }
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(event, convertedStepMap);
        iter.set(convertedStep);
        previousMap = convertedStepMap;
      }
      return steps;
    }

    private EventProxy findEvent(final Map<AutomatonProxy,StateProxy> sources,
                                 final Map<AutomatonProxy,StateProxy> targets)
    {
      final Collection<EventProxy> possible =
        new LinkedList<EventProxy>(mLocalEvents);
      for (final AutomatonProxy aut : getOriginalAutomata()) {
        if (possible.size() <= 1) {
          break;
        }
        final StateProxy source = sources.get(aut);
        final StateProxy target = targets.get(aut);
        final Collection<EventProxy> alphabet =
          new THashSet<EventProxy>(aut.getEvents());
        final int size = alphabet.size();
        final Collection<EventProxy> retained = new THashSet<EventProxy>(size);
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getSource() == source && trans.getTarget() == target) {
            final EventProxy event = trans.getEvent();
            retained.add(event);
          }
        }
        final Iterator<EventProxy> iter = possible.iterator();
        while (iter.hasNext()) {
          final EventProxy event = iter.next();
          if (alphabet.contains(event)) {
            if (!retained.contains(event)) {
              iter.remove();
            }
          } else {
            if (source != target) {
              iter.remove();
            }
          }
        }
      }
      return possible.iterator().next();
    }

    final StateProxy getOriginalState(final StateProxy convertedState,
                                      final AutomatonProxy aut)
    {
      if (mStateMap == null) {
        return convertedState;
      } else {
        return mStateMap.getOriginalState(convertedState, aut);
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mLocalEvents;
    private final EventProxy mHiddenEvent;
    private final SynchronousProductStateMap mStateMap;
  }


  //#########################################################################
  //# Inner Class MergeStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * merging states of the original automaton (automaton quotient).
   */
  private abstract class MergeStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new abstraction step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  reduced           Whether or not the set of precondition markings
     *                           was reduced during abstraction.
     * @param  resultStateEnc    State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     */
    MergeStep(final AutomatonProxy resultAut,
              final AutomatonProxy originalAut,
              final EventProxy tau,
              final StateEncoding originalStateEnc,
              final List<int[]> partition,
              final boolean reduced,
              final StateEncoding resultStateEnc)
    {
      super(resultAut, originalAut);
      mTau = tau;
      mOriginalStateEncoding = originalStateEnc;
      mPartition = partition;
      mHasReducedPreconditionMarking = reduced;
      mReverseOutputStateMap = resultStateEnc.getStateCodeMap();
    }

    //#######################################################################
    //# Simple Access
    ListBufferTransitionRelation getTransitionRelation()
    {
      return mTransitionRelation;
    }

    EventEncoding getEventEncoding()
    {
      return mEventEncoding;
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
    throws AnalysisException
    {
      setupTraceConversion();
      final List<SearchRecord> crucialSteps = getCrucialSteps(traceSteps);
      final List<SearchRecord> convertedSteps =
        convertCrucialSteps(crucialSteps);
      mergeTraceSteps(traceSteps, convertedSteps);
      tearDownTraceConversion();
      return traceSteps;
    }

    void setupTraceConversion()
    throws AnalysisException
    {
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final KindTranslator translator = getKindTranslator();
      mEventEncoding =
        new EventEncoding(originalAutomaton, translator, mTau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      recoverPreconditionMarking();
      mTransitionRelation = new ListBufferTransitionRelation
        (originalAutomaton, mEventEncoding, mOriginalStateEncoding,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }

    void setupTraceConversion(final EventEncoding enc,
                              final ListBufferTransitionRelation rel)
    {
      mEventEncoding = enc;
      mTransitionRelation = rel;
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final EventProxy alpha = mAbstractionRule.getUsedPreconditionMarking();
      mPreconditionMarkingID = enc.getEventCode(alpha);
    }

    void tearDownTraceConversion()
    {
      mTargetSet = null;
      mRecoveredPreconditionMarking = null;
      mEventEncoding = null;
      mTransitionRelation = null;
    }

    List<SearchRecord> getCrucialSteps(final List<TraceStepProxy> traceSteps)
    {
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final int tau = EventEncoding.TAU;
      final int len = traceSteps.size() + 1;
      final List<SearchRecord> crucialSteps = new ArrayList<SearchRecord>(len);
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      TraceStepProxy step = iter.next();
      Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      StateProxy crucialState = stepMap.get(resultAutomaton);
      int crucialEventID = tau;
      SearchRecord record;
      while (iter.hasNext()) {
        step = iter.next();
        final EventProxy event = step.getEvent();
        final int eventID = mEventEncoding.getEventCode(event);
        if (eventID < 0) {
          // Step of another automaton only --- skip.
        } else if (eventID == tau) {
          // Step by local tau --- skip but record target state.
          stepMap = step.getStateMap();
          crucialState = stepMap.get(resultAutomaton);
        } else {
          // Step by a proper event ---
          // 1) Add a step to the source state unless initial.
          if (crucialEventID != tau) {
            final int crucialStateID = mReverseOutputStateMap.get(crucialState);
            record = new SearchRecord(crucialStateID, crucialEventID);
            crucialSteps.add(record);
          }
          // 2) Record new event and target state.
          crucialEventID = eventID;
          stepMap = step.getStateMap();
          crucialState = stepMap.get(resultAutomaton);
        }
      }
      // Add step to last target state.
      final int crucialStateID = mReverseOutputStateMap.get(crucialState);
      record = new SearchRecord(crucialStateID, crucialEventID);
      crucialSteps.add(record);
      // Add final step to reach alpha.
      if (mPreconditionMarkingID >= 0) {
        record = new SearchRecord(-1, 0, tau, null);
        crucialSteps.add(record);
      }
      return crucialSteps;
    }

    abstract List<SearchRecord> convertCrucialSteps
      (final List<SearchRecord> crucialSteps);

    void setupTarget(final SearchRecord crucialStep)
    {
      final int targetClass = crucialStep.getState();
      if (targetClass < 0) {
        mTargetSet = null;
      } else if (mPartition == null) {
        mTargetSet = new TIntHashSet(1);
        mTargetSet.add(targetClass);
      } else {
        final int[] targetArray = mPartition.get(targetClass);
        mTargetSet = new TIntHashSet(targetArray);
      }
    }

    boolean isTargetState(final int state)
    {
      if (mTargetSet != null) {
        return mTargetSet.contains(state);
      } else {
        return isTraceEndState(state);
      }
    }

    boolean isTraceEndState(final int state)
    {
      if (mPreconditionMarkingID < 0) {
        return true;
      } else if (mRecoveredPreconditionMarking != null) {
        return mRecoveredPreconditionMarking.get(state);
      } else {
        return mTransitionRelation.isMarked(state, mPreconditionMarkingID);
      }
    }

    void mergeTraceSteps(final List<TraceStepProxy> traceSteps,
                         final List<SearchRecord> convertedSteps)
    {
      final int tau = EventEncoding.TAU;
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final ListIterator<TraceStepProxy> stepIter = traceSteps.listIterator();
      final TraceStepProxy initStep = stepIter.next();
      final Iterator<SearchRecord> convertedIter = convertedSteps.iterator();
      final SearchRecord initRecord = convertedIter.next();
      final Map<AutomatonProxy,StateProxy> map =
        new HashMap<AutomatonProxy,StateProxy>(initStep.getStateMap());
      map.remove(resultAutomaton);
      final int initID = initRecord.getState();
      final StateProxy initState = mOriginalStateEncoding.getState(initID);
      map.put(originalAutomaton, initState);
      final TraceStepProxy newInitStep =
        factory.createTraceStepProxy(null, map);
      stepIter.set(newInitStep);
      TraceStepProxy step = stepIter.hasNext() ? stepIter.next() : null;
      SearchRecord record =
        convertedIter.hasNext() ? convertedIter.next() : null;
      while (step != null || record != null) {
        if (step != null) {
          final EventProxy event = step.getEvent();
          final int eventID = mEventEncoding.getEventCode(event);
          if (eventID == tau) {
            // Skip tau in master trace, will insert later from converted.
            stepIter.remove();
            step = stepIter.hasNext() ? stepIter.next() : null;
            continue;
          } else if (eventID < 0) {
            // Step of another automaton only.
            final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
            map.putAll(stepMap);
            map.remove(resultAutomaton);
            final TraceStepProxy newStep =
              factory.createTraceStepProxy(event, map);
            stepIter.set(newStep);
            step = stepIter.hasNext() ? stepIter.next() : null;
            continue;
          }
        }
        if (record != null) {
          final int eventID = record.getEvent();
          if (eventID == tau) {
            // Step by local tau only.
            final int stateID = record.getState();
            final StateProxy state = mOriginalStateEncoding.getState(stateID);
            map.put(originalAutomaton, state);
            final TraceStepProxy newStep =
              factory.createTraceStepProxy(mTau, map);
            if (step == null) {
              stepIter.add(newStep);
            } else {
              stepIter.previous();
              stepIter.add(newStep);
              stepIter.next();
            }
            record = convertedIter.hasNext() ? convertedIter.next() : null;
            continue;
          }
        }
        // Step by shared event
        assert step != null;
        assert record != null;
        final EventProxy event = step.getEvent();
        final int stateID = record.getState();
        final StateProxy state = mOriginalStateEncoding.getState(stateID);
        map.put(originalAutomaton, state);
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        map.putAll(stepMap);
        map.remove(resultAutomaton);
        final TraceStepProxy newStep = factory.createTraceStepProxy(event, map);
        stepIter.set(newStep);
        step = stepIter.hasNext() ? stepIter.next() : null;
        record = convertedIter.hasNext() ? convertedIter.next() : null;
      }
    }

    //#######################################################################
    //# Trace Computation
    void recoverPreconditionMarking()
      throws AnalysisException
    {
      final EventProxy alpha = mAbstractionRule.getUsedPreconditionMarking();
      mPreconditionMarkingID = mEventEncoding.getEventCode(alpha);
      if (mHasReducedPreconditionMarking) {
        final AutomatonProxy aut = getOriginalAutomaton();
        mRecoveredPreconditionMarking =
          mAbstractionRule.recoverMarkings(aut, mTau);
        if (mPreconditionMarkingID < 0) {
          final KindTranslator translator = getKindTranslator();
          mPreconditionMarkingID =
            mEventEncoding.addEvent(alpha, translator, true);
        }
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * The event that was hidden from the original automaton,
     * or <CODE>null</CODE>.
     */
    private final EventProxy mTau;
    /**
     * State encoding of original automaton. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private final StateEncoding mOriginalStateEncoding;
    /**
     * Partition applied to original automaton.
     * Each entry lists states of the input encoding that have been merged.
     */
    private final List<int[]> mPartition;
    /**
     * A flag, indicating that the precondition markings have been reduced
     * during abstraction and need to be recovered for trace expansion.
     */
    private final boolean mHasReducedPreconditionMarking;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    /**
     * Transition relation that was simplified.
     * Only used when expanding trace.
     */
    private ListBufferTransitionRelation mTransitionRelation;
    /**
     * Event encoding for {@link #mTransitionRelation}.
     * Only used when expanding trace.
     */
    private EventEncoding mEventEncoding;
    /**
     * Code of precondition marking in {@link #mEventEncoding}.
     */
    private int mPreconditionMarkingID;
    /**
     * Set of target states of current search.
     * Only used when expanding trace.
     */
    private TIntHashSet mTargetSet;
    /**
     * Recovered precondition marking, if needed.
     * @see #mHasReducedPreconditionMarking
     */
    private BitSet mRecoveredPreconditionMarking;

  }


  //#########################################################################
  //# Inner Class ObservationEquivalenceStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * merging observation equivalent or weakly observation equivalent states.
   * This class provides more efficient trace computation than is possible
   * for a general merge.
   */
  private class ObservationEquivalenceStep extends MergeStep
  {

    //#######################################################################
    //# Constructors
    /**
     * Creates a new observation equivalence step record.
     * This constructor creates a step that assumes an unchanged set of
     * precondition markings.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  resultStateEnc    State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     */
    private ObservationEquivalenceStep(final AutomatonProxy resultAut,
                                       final AutomatonProxy originalAut,
                                       final EventProxy tau,
                                       final StateEncoding originalStateEnc,
                                       final List<int[]> partition,
                                       final StateEncoding resultStateEnc)
    {
      this(resultAut, originalAut, tau,
           originalStateEnc, partition, false, resultStateEnc);
    }

    /**
     * Creates a new observation equivalence step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  reduced           Whether or not the set of precondition markings
     *                           was reduced during abstraction.
     * @param  resultStateEnc    State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     */
    private ObservationEquivalenceStep(final AutomatonProxy resultAut,
                                       final AutomatonProxy originalAut,
                                       final EventProxy tau,
                                       final StateEncoding originalStateEnc,
                                       final List<int[]> partition,
                                       final boolean reduced,
                                       final StateEncoding resultStateEnc)
    {
      super(resultAut, originalAut, tau,
            originalStateEnc, partition, reduced, resultStateEnc);
    }

    //#######################################################################
    //# Trace Computation
    @Override
    List<SearchRecord> convertCrucialSteps
      (final List<SearchRecord> crucialSteps)
    {
      final List<SearchRecord> foundSteps = new LinkedList<SearchRecord>();
      int state = -1;
      for (final SearchRecord crucialStep : crucialSteps) {
        SearchRecord found = convertCrucialStep(state, crucialStep);
        state = found.getState();
        // Append the found search records in reverse order to the result
        final int end = foundSteps.size();
        final ListIterator<SearchRecord> iter = foundSteps.listIterator(end);
        while (found.getPredecessor() != null) {
          iter.add(found);
          iter.previous();
          found = found.getPredecessor();
        }
      }
      return foundSteps;
    }

    /**
     * Finds a partial trace in the original automaton before observation
     * equivalence. This method computes a sequence of tau transitions, followed
     * by a transition with the given event, followed by another sequence of tau
     * transitions linking the source state to some state in the class of the
     * target state in the simplified automaton.
     * @param originalSource
     *         State number of the source state in the original automaton,
     *         or -1 to request a search starting from all initial states.
     * @param crucialStep
     *         Search containing code of the event and state number of the
     *         target state in the simplified automaton (code of state
     *         class), with -1 request search for an alpha-marked state.
     * @return Search record describing the trace from source to
     *         target, in reverse order. The last entry in the list represents
     *         the first step after the source state, with its event and target
     *         state. The first step has a target state in the given target
     *         class. Events in the list can only be tau or the given event.
     */
    private SearchRecord convertCrucialStep(final int originalSource,
                                            final SearchRecord crucialStep)
    {
      setupTarget(crucialStep);
      // The crucial event may be tau, but only for the first or last step.
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int tau = EventEncoding.TAU;
      final int crucialEvent = crucialStep.getEvent();
      // There are two types of search records, representing the states
      // reached before or after execution of the crucial event, except
      // when the crucial event is tau. If the crucial event is tau, only
      // search states after the crucial event are considered, so a search
      // using only tau transitions is performed.
      final Set<SearchRecord> visited = new THashSet<SearchRecord>();
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      if (originalSource >= 0) {
        // Normal search starting from known state.
        final SearchRecord record;
        if (crucialEvent == tau) {
          record = new SearchRecord(originalSource, 1, -1, null);
          if (isTargetState(originalSource)) {
            return record;
          }
        } else {
          record = new SearchRecord(originalSource);
        }
        visited.add(record);
        open.add(record);
      } else {
        // Start from initial state. The dummy record ensures that the first
        // real search record will later be included in the trace.
        final SearchRecord dummy = new SearchRecord(-1);
        final int numStates = rel.getNumberOfStates();
        for (int state = 0; state < numStates; state++) {
          if (rel.isInitial(state)) {
            final SearchRecord record;
            if (crucialEvent == tau) {
              record = new SearchRecord(state, 1, -1, dummy);
              if (isTargetState(state)) {
                return record;
              }
            } else {
              record = new SearchRecord(state, 0, -1, dummy);
            }
            visited.add(record);
            open.add(record);
          }
        }
      }
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final int depth = current.getDepth();
        final boolean hasEvent = depth > 0;
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord record =
            new SearchRecord(target, depth, tau, current);
          if (hasEvent && isTargetState(target)) {
            return record;
          } else if (visited.add(record)) {
            open.add(record);
          }
        }
        if (!hasEvent) {
          iter.reset(source, crucialEvent);
          while (iter.advance()) {
            final int target = iter.getCurrentTargetState();
            final SearchRecord record =
              new SearchRecord(target, 1, crucialEvent, current);
            if (isTargetState(target)) {
              return record;
            } else if (visited.add(record)) {
              open.add(record);
            }
          }
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class ConflictEquivalenceStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * merging states in such a way that generalised conflict equivalence
   * is preserved. This class supports all conflict preserving merge
   * operations. Trace computation is achieved by breadth-first search,
   * with complexity O(|<I>s</I>||<I>Q</I>|) where |<I>s</I>| is the
   * length of the trace of the abstracted automaton and |<I>Q</I>| is the
   * number of states of the original automaton.
   */
  private class ConflictEquivalenceStep extends MergeStep
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new conflict equivalence step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  reduced           Whether or not the set of precondition markings
     *                           was reduced during abstraction.
     * @param  resultStateEnc    State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     */
    private ConflictEquivalenceStep(final AutomatonProxy resultAut,
                                    final AutomatonProxy originalAut,
                                    final EventProxy tau,
                                    final StateEncoding originalStateEnc,
                                    final List<int[]> partition,
                                    final boolean reduced,
                                    final StateEncoding resultStateEnc)
    {
      super(resultAut, originalAut, tau,
            originalStateEnc, partition, reduced, resultStateEnc);
    }

    //#######################################################################
    //# Trace Computation
    @Override
    List<SearchRecord> convertCrucialSteps
      (final List<SearchRecord> crucialSteps)
    {
      int len = crucialSteps.size();
      SearchRecord last = crucialSteps.get(len - 1);
      if (last.getState() < 0) {
        len--;
        last = crucialSteps.get(len - 1);
      }
      setupTarget(last);
      final SearchRecord[] crucialArray = new SearchRecord[len];
      int index = 0;
      for (final SearchRecord crucialStep : crucialSteps) {
        if (index >= len) {
          break;
        }
        crucialArray[index++] = crucialStep;
      }
      SearchRecord found = convertCrucialSteps(crucialArray);
      // Append the found search records in reverse order to the result
      final List<SearchRecord> foundSteps = new LinkedList<SearchRecord>();
      while (found.getPredecessor() != null) {
        foundSteps.add(0, found);
        found = found.getPredecessor();
      }
      return foundSteps;
    }

    SearchRecord convertCrucialSteps(final SearchRecord[] crucialSteps)
    {
      final int tau = EventEncoding.TAU;
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final Set<SearchRecord> visited = new THashSet<SearchRecord>();
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final boolean firstEnd =
        crucialSteps.length == 1 && crucialSteps[0].getEvent() == tau;
      // The dummy record ensures that the first
      // real search record will later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state)) {
          final SearchRecord record;
          if (!firstEnd) {
            record = new SearchRecord(state, 0, -1, dummy);
          } else if (!isTargetState(state)) {
            record = new SearchRecord(state, 1, -1, dummy);
          } else {
            record = new SearchRecord(state, 2, -1, dummy);
            if (isTraceEndState(state)) {
              return record;
            }
          }
          visited.add(record);
          open.add(record);
        }
      }
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final int depth = current.getDepth();
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          int nextDepth = depth;
          if (nextDepth == crucialSteps.length && isTargetState(target)) {
            nextDepth++;
          }
          final SearchRecord record =
            new SearchRecord(target, nextDepth, tau, current);
          if (nextDepth > crucialSteps.length && isTraceEndState(target)) {
            return record;
          } else if (visited.add(record)) {
            open.add(record);
          }
        }
        if (depth < crucialSteps.length) {
          final int event = crucialSteps[depth].getEvent();
          iter.reset(source, event);
          while (iter.advance()) {
            final int target = iter.getCurrentTargetState();
            int nextDepth = depth + 1;
            if (nextDepth == crucialSteps.length && isTargetState(target)) {
              nextDepth++;
            }
            final SearchRecord record =
              new SearchRecord(target, nextDepth, event, current);
            if (nextDepth > crucialSteps.length && isTraceEndState(target)) {
              return record;
            } else if (visited.add(record)) {
              open.add(record);
            }
          }
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class MergeStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * merging states of the original automaton (automaton quotient).
   */
  private class CertainConflictsStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new abstraction step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  resultStateEnc    State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  oeqBefore         Whether or not the abstraction steps applied
     *                           before identifying certain conflicts preserve
     *                           observation equivalence.
     * @param  oeqAfter          Whether or not the abstraction steps applied
     *                           after identifying certain conflicts preserve
     *                           observation equivalence.
     */
    CertainConflictsStep(final AutomatonProxy resultAut,
                         final AutomatonProxy originalAut,
                         final EventProxy tau,
                         final StateEncoding originalStateEnc,
                         final List<int[]> partition,
                         final StateEncoding resultStateEnc,
                         final boolean oeqBefore,
                         final boolean oeqAfter)
    {
      super(resultAut, originalAut);
      mTau = tau;
      mOriginalStateEncoding = originalStateEnc;
      mPartition = partition;
      mResultStateEncoding = resultStateEnc;
      mIsObservationEquivalentBefore = oeqBefore;
      mIsObservationEquivalentAfter = oeqAfter;
    }

    //#######################################################################
    //# Trace Computation
    @Override
    List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
    throws AnalysisException
    {
      // First check whether certain conflicts need to be considered
      // in trace expansion ...
      final AutomatonProxy originalAut = getOriginalAutomaton();
      final AutomatonProxy resultAut = getResultAutomaton();
      MergeStep delegate =
        createDelegate(resultAut, originalAut, mOriginalStateEncoding,
                       mPartition, mResultStateEncoding,
                       mIsObservationEquivalentBefore &&
                       mIsObservationEquivalentAfter);
      delegate.setupTraceConversion();
      List<SearchRecord> crucialSteps = delegate.getCrucialSteps(traceSteps);
      List<SearchRecord> convertedSteps =
        delegate.convertCrucialSteps(crucialSteps);
      ListBufferTransitionRelation rel = delegate.getTransitionRelation();
      final EventEncoding eventEnc = delegate.getEventEncoding();
      if (isBlockingTrace(convertedSteps, rel, eventEnc)) {
        delegate.mergeTraceSteps(traceSteps, convertedSteps);
        return traceSteps;
      }
      delegate = null;

      // OK, expanded trace is not blocking.
      // We need to try to add steps into certain conflicts and further
      // into blocking, or prove that the rest of the system blocks ...
      final StandardTRSimplifierAbstractionRule rule =
        (StandardTRSimplifierAbstractionRule) mAbstractionRule;
      final ChainTRSimplifier chain = rule.getSimplifier();
      final int config = chain.getPreferredInputConfiguration();
      rel = new ListBufferTransitionRelation
        (originalAut, eventEnc, mOriginalStateEncoding, config);
      chain.setTransitionRelation(rel);
      final int ccindex = rule.getCertainConflictsIndex();
      chain.runTo(ccindex);
      final List<int[]> partition1 = chain.getResultPartition();
      final List<int[]> partition2 = computeQuotientPartition(partition1);
      delegate =
        createDelegate(resultAut, null, null, partition2,
                       mResultStateEncoding, mIsObservationEquivalentAfter);
      delegate.setupTraceConversion(eventEnc, rel);
      crucialSteps = delegate.getCrucialSteps(traceSteps);
      convertedSteps = delegate.convertCrucialSteps(crucialSteps);
      final int numConvertedSteps = convertedSteps.size();
      SearchRecord record = convertedSteps.get(numConvertedSteps - 1);
      final int lastConvertedState = record.getState();
      final LimitedCertainConflictsTRSimplifier simplifier =
        rule.getCertainConflictsSimplifier();
      final int lconfig = simplifier.getPreferredInputConfiguration();
      ListBufferTransitionRelation copy =
        new ListBufferTransitionRelation(rel, lconfig);
      simplifier.setTransitionRelation(copy);
      simplifier.setAppliesPartitionAutomatically(false);
      simplifier.run();
      simplifier.setTransitionRelation(rel);
      copy = null;

      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      final CertainConflictsTraceExpander expander =
        new CertainConflictsTraceExpander(factory, translator,
                                          mCurrentCompositionalSafetyVerifier);
      final int numTraceSteps = traceSteps.size();
      final TraceStepProxy lastTraceStep = traceSteps.get(numTraceSteps - 1);
      expander.setStartStates(lastTraceStep);
      final StateEncoding stateEnc = new StateEncoding();
      final EventProxy prop =
        factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
      AutomatonProxy testaut = null;
      List<TraceStepProxy> additionalSteps = null;
      final int startLevel = simplifier.getLevel(lastConvertedState);
      final int maxlevel =
        startLevel < 0 ? simplifier.getMaxLevel() : startLevel - 2;
      for (int level = 0; level <= maxlevel; level += 2) {
        testaut = simplifier.createTestAutomaton
          (factory, eventEnc, stateEnc, lastConvertedState, prop, level);
        expander.setCertainConflictsAutomaton(resultAut, testaut, prop);
        additionalSteps = expander.run();
        if (additionalSteps != null) {
          break;
        }
        stateEnc.clear();
      }
      if (additionalSteps != null) {
        final Collection<AutomatonProxy> automata = expander.getTraceAutomata();
        final List<TraceStepProxy> saturatedSteps =
          getSaturatedTraceSteps(additionalSteps, automata);
        final Iterator<TraceStepProxy> iter = saturatedSteps.iterator();
        iter.next();
        while (iter.hasNext()) {
          final TraceStepProxy step = iter.next();
          final EventProxy event = step.getEvent();
          final int ecode = eventEnc.getEventCode(event);
          final Map<AutomatonProxy,StateProxy> map = step.getStateMap();
          final Map<AutomatonProxy,StateProxy> reducedMap =
            new HashMap<AutomatonProxy,StateProxy>(map);
          reducedMap.remove(testaut);
          final TraceStepProxy reducedStep =
            factory.createTraceStepProxy(event, reducedMap);
          traceSteps.add(reducedStep);
          if (ecode >= 0) {
            final StateProxy state = map.get(testaut);
            final int scode = stateEnc.getStateCode(state);
            record = new SearchRecord(scode, ecode);
            convertedSteps.add(record);
          }
        }
      } else if (startLevel > 0 && (startLevel & 1) != 0) {
        final int endState =
          simplifier.findTauReachableState(lastConvertedState, startLevel & ~1);
        record = new SearchRecord(endState, EventEncoding.TAU);
        convertedSteps.add(record);
      }
      delegate =
        createDelegate(resultAut, originalAut, mOriginalStateEncoding,
                       partition1, mResultStateEncoding,
                       mIsObservationEquivalentBefore);
      delegate.setupTraceConversion();
      convertedSteps = getCrucialSteps(convertedSteps);
      convertedSteps = delegate.convertCrucialSteps(convertedSteps);
      delegate.mergeTraceSteps(traceSteps, convertedSteps);
      return traceSteps;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean isBlockingTrace(final List<SearchRecord> steps,
                                    final ListBufferTransitionRelation rel,
                                    final EventEncoding enc)
    {
      final int markingID = enc.getEventCode(mCurrentDefaultMarking);
      assert markingID >= 0;
      final int traceEnd = steps.size() - 1;
      final SearchRecord step = steps.get(traceEnd);
      final int state= step.getState();
      if (rel.isMarked(state, markingID)) {
        return false;
      }
      final TIntStack stack = new TIntStack();
      final TIntHashSet visited = new TIntHashSet();
      stack.push(state);
      visited.add(state);
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      while (stack.size() > 0) {
        final int current = stack.pop();
        iter.resetState(current);
        while (iter.advance()) {
          final int succ = iter.getCurrentTargetState();
          if (visited.add(succ)) {
            if (rel.isMarked(succ, markingID)) {
              return false;
            }
            stack.push(succ);
          }
        }
      }
      return false;
    }

    private MergeStep createDelegate(final AutomatonProxy resultAut,
                                     final AutomatonProxy originalAut,
                                     final StateEncoding originalStateEnc,
                                     final List<int[]> partition,
                                     final StateEncoding resultStateEnc,
                                     final boolean oeq)
    {
      if (oeq) {
        return new ObservationEquivalenceStep(resultAut, originalAut, mTau,
                                              originalStateEnc, partition,
                                              false, resultStateEnc);
      } else {
        return new ConflictEquivalenceStep(resultAut, originalAut, mTau,
                                           originalStateEnc, partition,
                                           false, resultStateEnc);
      }
    }

    private List<int[]> computeQuotientPartition(final List<int[]> partition)
    {
      if (partition == null) {
        return mPartition;
      } else {
        final AutomatonProxy aut = getOriginalAutomaton();
        final int numStates = aut.getStates().size();
        final TIntIntHashMap classMap = new TIntIntHashMap(numStates);
        int code = 0;
        for (final int[] clazz : partition) {
          for (final int state : clazz) {
            classMap.put(state, code);
          }
          code++;
        }
        final int numClasses = mPartition.size();
        final List<int[]> quotient = new ArrayList<int[]>(numClasses);
        final TIntHashSet set = new TIntHashSet();
        boolean trivial = true;
        for (final int[] clazz : mPartition) {
          for (final int state : clazz) {
            code = classMap.get(state);
            set.add(code);
          }
          final int index = quotient.size();
          final int[] newclazz = set.toArray();
          Arrays.sort(newclazz);
          quotient.add(newclazz);
          trivial &= newclazz.length == 1 && newclazz[0] == index;
          set.clear();
        }
        return trivial ? null : quotient;
      }
    }

    private List<SearchRecord> getCrucialSteps
      (final List<SearchRecord> rawSteps)
    {
      final int tau = EventEncoding.TAU;
      final int len = rawSteps.size() - 1;
      final List<SearchRecord> crucialSteps = new ArrayList<SearchRecord>(len);
      int crucialState = -1;
      int crucialEvent = tau;
      SearchRecord record;
      for (final SearchRecord raw : rawSteps) {
        final int event = raw.getEvent();
        if (event >= tau) {
          if (crucialEvent != tau) {
            record = new SearchRecord(crucialState, crucialEvent);
            crucialSteps.add(record);
          }
          crucialEvent = event;
        }
        crucialState = raw.getState();
      }
      record = new SearchRecord(crucialState, crucialEvent);
      crucialSteps.add(record);
      return crucialSteps;
    }

    //#######################################################################
    //# Data Members
    /**
     * The event that was hidden from the original automaton,
     * or <CODE>null</CODE>.
     */
    private final EventProxy mTau;
    /**
     * State encoding of original automaton. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private final StateEncoding mOriginalStateEncoding;
    /**
     * Partition applied to original automaton.
     * Each entry lists states of the input encoding that have been merged.
     */
    private final List<int[]> mPartition;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final StateEncoding mResultStateEncoding;
    /**
     * A flag, indicating whether or not the abstraction steps applied
     * before identifying certain conflicts preserve observation equivalence.
     */
    private final boolean mIsObservationEquivalentBefore;
    /**
     * A flag, indicating whether or not the abstraction steps applied
     * after identifying certain conflicts preserve observation equivalence.
     */
    private final boolean mIsObservationEquivalentAfter;

  }


  //#########################################################################
  //# Inner Class OneAutomatonStateMap
  private class OneAutomatonStateMap
    implements SynchronousProductStateMap
  {
    //#######################################################################
    //# Constructor
    private OneAutomatonStateMap(final AutomatonProxy inputAut,
                                 final StateEncoding inputEnc,
                                 final StateEncoding outputEnc)
    {
      mOriginalAutomaton = inputAut;
      final int numStates = inputEnc.getNumberOfStates();
      mStateMap = new HashMap<StateProxy,StateProxy>(numStates);
      for (int s = 0; s < numStates; s++) {
        final StateProxy inputState = inputEnc.getState(s);
        final StateProxy outputState = outputEnc.getState(s);
        mStateMap.put(outputState, inputState);
      }
    }


    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.analysis.SynchronousProductStateMap
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return Collections.singletonList(mOriginalAutomaton);
    }

    public StateProxy getOriginalState(final StateProxy tuple,
                                       final AutomatonProxy aut)
    {
      if (aut == mOriginalAutomaton) {
        return mStateMap.get(tuple);
      } else {
        throw new IllegalArgumentException
          ("Unexpected original automaton '" + aut.getName() + "' in " +
           ProxyTools.getShortClassName(this) + "!");
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mOriginalAutomaton;
    private final Map<StateProxy,StateProxy> mStateMap;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  /**
   * A record to store information about a visited state while searching
   * to expand counterexamples.
   */
  private static class SearchRecord
  {

    //#######################################################################
    //# Constructors
    SearchRecord(final int state)
    {
      this(state, -1);
    }

    SearchRecord(final int state, final int event)
    {
      this(state, 0, event, null);
    }

    SearchRecord(final int state, final int depth, final int event,
                 final SearchRecord pred)
    {
      mState = state;
      mDepth = depth;
      mEvent = event;
      mPredecessor = pred;
    }

    //#######################################################################
    //# Getters
    int getState()
    {
      return mState;
    }

    int getDepth()
    {
      return mDepth;
    }

    SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return
        "{state=" + mState + "; event=" + mEvent + "; depth=" + mDepth + "}";
    }

    @Override
    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
        final SearchRecord record = (SearchRecord) other;
        return mState == record.mState && mDepth == record.mDepth;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return HashFunctions.hash(mState) + 5 * HashFunctions.hash(mDepth);
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final int mDepth;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mCurrentDefaultMarking;
  private EventProxy mPreconditionMarking;
  private Collection<EventProxy> mPropositions;
  private int mLowerInternalStateLimit;
  private int mUpperInternalStateLimit;
  private int mInternalTransitionLimit;

  private AbstractionMethod mAbstractionMethod;
  private PreselectingMethod mPreselectingMethod;
  private SelectingMethod mSelectingMethod;
  private boolean mSubsumptionEnabled;
  private SynchronousProductBuilder mSynchronousProductBuilder;
  private ConflictChecker mMonolithicConflictChecker;
  private SafetyVerifier mCompositionalSafetyVerifier;
  private SafetyVerifier mMonolithicSafetyVerifier;

  private List<AutomatonProxy> mCurrentAutomata;
  private Map<AutomatonProxy,AutomatonInfo> mAutomatonInfoMap;
  private Map<EventProxy,EventInfo> mEventInfoMap =
      new HashMap<EventProxy,EventInfo>();
  private Queue<AutomatonProxy> mDirtyAutomata;
  private Collection<EventProxy> mRedundantEvents;
  /**
   * A flag indicating that an event has disappeared unexpectedly.
   * This flag is set when a proper event has been found to be only selflooped
   * in an automaton after abstraction, and therefore has been removed from
   * the automaton alphabet.
   */
  private boolean mEventHasDisappeared;
  private Collection<SubSystem> mPostponedSubsystems;
  private Collection<SubSystem> mProcessedSubsystems;
  private List<AbstractionStep> mModifyingSteps;
  private Set<String> mUsedEventNames;
  private Set<List<AutomatonProxy>> mOverflowCandidates;
  private int mCurrentInternalStateLimit;
  private boolean mGotGlobalResult;
  private ConflictTraceProxy mPreliminaryCounterexample;

  private AbstractionRule mAbstractionRule;
  private PreselectingHeuristic mPreselectingHeuristic;
  private SelectingHeuristic mSelectingHeuristic;
  private SynchronousProductBuilder mCurrentSynchronousProductBuilder;
  private ConflictChecker mCurrentMonolithicConflictChecker;
  private SafetyVerifier mCurrentCompositionalSafetyVerifier;
  private SafetyVerifier mCurrentMonolithicSafetyVerifier;


  //#########################################################################
  //# Class Constants
  private static final byte UNKNOWN_SELFLOOP = 0;
  private static final byte ONLY_SELFLOOP = 1;
  private static final byte NOT_ONLY_SELFLOOP = 2;
  private static final byte BLOCKED = 3;

  private static final byte NONE_OMEGA = 0x01;
  private static final byte ALL_OMEGA = 0x02;
  private static final byte NONE_ALPHA = 0x04;
  private static final byte ALL_ALPHA = 0x08;

}
