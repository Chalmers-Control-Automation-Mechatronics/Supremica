//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntStack;
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

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AutomatonResult;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;

/**
 * <P>A compositional conflict checker that can be configured to use different
 * abstraction sequences for its simplification steps.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.</P>
 *
 * @author Robi Malik, Rachel Francis
 */

public class CompositionalConflictChecker
  extends AbstractCompositionalModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final AbstractionMethod method,
                                      final ProductDESProxyFactory factory)
  {
    this(null, method, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to its default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final ProductDESProxy model,
                                      final AbstractionMethod method,
                                      final ProductDESProxyFactory factory)
  {
    this(model, null, method, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final ProductDESProxy model,
                                      final EventProxy marking,
                                      final AbstractionMethod method,
                                      final ProductDESProxyFactory factory)
  {
    super(model,
          factory,
          ConflictKindTranslator.getInstance(),
          new PreselectingMethodFactory(),
          new SelectingMethodFactory());
    mDefaultMarking = marking;
    mAbstractionMethod = method;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  public void setMarkingProposition(final EventProxy marking)
  {
    mDefaultMarking = marking;
    mUsedDefaultMarking = null;
  }

  public EventProxy getMarkingProposition()
  {
    return mDefaultMarking;
  }

  public void setPreconditionMarking(final EventProxy alpha)
  {
    mPreconditionMarking = alpha;
  }

  public EventProxy getPreconditionMarking()
  {
    return mPreconditionMarking;
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Configuration
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

  public void setCompositionalSafetyVerifier(final SafetyVerifier checker)
  {
    mCompositionalSafetyVerifier = checker;
  }

  public void setMonolithicSafetyVerifier(final SafetyVerifier checker)
  {
    mMonolithicSafetyVerifier = checker;
  }


  @Override
  public void setPreselectingMethod(final PreselectingMethod method)
  {
    super.setPreselectingMethod(method);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      final AbstractCompositionalModelVerifier.PreselectingMethodFactory
        factory = safetyVerifier.getPreselectingMethodFactory();
      final PreselectingMethod safetyMethod = factory.getEnumValue(method);
      if (safetyMethod != null) {
        safetyVerifier.setPreselectingMethod(safetyMethod);
      }
    }
  }

  @Override
  public void setSelectingMethod(final SelectingMethod method)
  {
    super.setSelectingMethod(method);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      final AbstractCompositionalModelVerifier.SelectingMethodFactory
        factory = safetyVerifier.getSelectingMethodFactory();
      final SelectingMethod safetyMethod = factory.getEnumValue(method);
      if (safetyMethod != null) {
        safetyVerifier.setSelectingMethod(safetyMethod);
      }
    }
  }

  @Override
  public void setSubumptionEnabled(final boolean enable)
  {
    super.setSubumptionEnabled(enable);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setSubumptionEnabled(enable);
    }
  }

  @Override
  public void setInternalStateLimit(final int limit)
  {
    super.setInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setInternalStateLimit(limit);
    }
  }

  @Override
  public void setLowerInternalStateLimit(final int limit)
  {
    super.setLowerInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setLowerInternalStateLimit(limit);
    }
  }

  @Override
  public void setUpperInternalStateLimit(final int limit)
  {
    super.setUpperInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setUpperInternalStateLimit(limit);
    }
  }

  @Override
  public void setMonolithicStateLimit(final int limit)
  {
    super.setMonolithicStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setMonolithicStateLimit(limit);
    } else if (mMonolithicSafetyVerifier != null) {
      mMonolithicSafetyVerifier.setNodeLimit(limit);
    }
  }

  @Override
  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setMonolithicTransitionLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setMonolithicTransitionLimit(limit);
    } else if (mMonolithicSafetyVerifier != null) {
      mMonolithicSafetyVerifier.setTransitionLimit(limit);
    }
  }

  @Override
  public void setInternalTransitionLimit(final int limit)
  {
    super.setInternalTransitionLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setInternalTransitionLimit(limit);
    }
  }


  //#########################################################################
  //# Specific Access
  @Override
  protected void setupMonolithicVerifier()
    throws EventNotFoundException
  {
    if (getCurrentMonolithicVerifier() == null) {
      final ConflictChecker configured =
        (ConflictChecker) getMonolithicVerifier();
      final ConflictChecker current;
      if (configured == null) {
        final ProductDESProxyFactory factory = getFactory();
        current = new NativeConflictChecker(factory);
      } else {
        current = configured;
      }
      current.setMarkingProposition(mUsedDefaultMarking);
      current.setPreconditionMarking(mPreconditionMarking);
      setCurrentMonolithicVerifier(current);
      super.setupMonolithicVerifier();
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
        final SafetyDiagnostics diag =
          LanguageInclusionDiagnostics.getInstance();
        final CompositionalSafetyVerifier safetyVerifier =
          new CompositionalSafetyVerifier(factory, null, diag);
        final AbstractCompositionalModelVerifier.PreselectingMethodFactory
          pfactory = safetyVerifier.getPreselectingMethodFactory();
        final PreselectingMethod pmethod = getPreselectingMethod();
        final PreselectingMethod ppmethod = pfactory.getEnumValue(pmethod);
        if (ppmethod != null) {
          safetyVerifier.setPreselectingMethod(ppmethod);
        }
        final AbstractCompositionalModelVerifier.SelectingMethodFactory
          sfactory = safetyVerifier.getSelectingMethodFactory();
        final SelectingMethod smethod = getSelectingMethod();
        final SelectingMethod ssmethod = sfactory.getEnumValue(smethod);
        if (ssmethod != null) {
          safetyVerifier.setSelectingMethod(ssmethod);
        }
        final boolean enable = isSubsumptionEnabled();
        safetyVerifier.setSubumptionEnabled(enable);
        final int lslimit = getLowerInternalStateLimit();
        safetyVerifier.setLowerInternalStateLimit(lslimit);
        final int uslimit = getUpperInternalStateLimit();
        safetyVerifier.setUpperInternalStateLimit(uslimit);
        final int mslimit = getMonolithicStateLimit();
        safetyVerifier.setMonolithicStateLimit(mslimit);
        final int itlimit = getInternalTransitionLimit();
        safetyVerifier.setInternalTransitionLimit(itlimit);
        final int mtlimit = getMonolithicTransitionLimit();
        safetyVerifier.setMonolithicTransitionLimit(mtlimit);
        mCurrentCompositionalSafetyVerifier  = safetyVerifier;
      } else {
        mCurrentCompositionalSafetyVerifier = mCompositionalSafetyVerifier;
      }
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
  @Override
  protected void setUp()
    throws AnalysisException
  {
    if (mDefaultMarking == null) {
      final ProductDESProxy model = getModel();
      mUsedDefaultMarking =
        AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mDefaultMarking;
    }
    final Collection<EventProxy> props;
    if (mPreconditionMarking == null) {
      props = Collections.singletonList(mUsedDefaultMarking);
    } else {
      final EventProxy[] markings = new EventProxy[2];
      markings[0] = mUsedDefaultMarking;
      markings[1] = mPreconditionMarking;
      props = Arrays.asList(markings);
    }
    setPropositions(props);
    final AbstractionProcedure proc =
      mAbstractionMethod.createAbstractionRule(this);
    setAbstractionProcedure(proc);
    super.setUp();
    setupSafetyVerifiers();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
    mCurrentCompositionalSafetyVerifier = null;
    mCurrentMonolithicSafetyVerifier = null;
    mAutomatonInfoMap = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {

    final byte status = getSubsystemPropositionStatus(automata);
    if ((status & NONE_ALPHA) != 0) {
      // The global system is nonblocking.
      final CompositionalVerificationResult result = getAnalysisResult();
      result.setSatisfied(true);
      return true;
    } else if ((status & ALL_OMEGA) != 0) {
      // This subsystem is trivially nonblocking.
      return true;
    } else if ((status & NONE_OMEGA) != 0) {
      // The global system is blocking if and only if alpha is reachable
      checkAlphaReachable((status & ALL_ALPHA) == 0);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected boolean confirmMonolithicCounterExample()
    throws AnalysisException
  {
    return checkAlphaReachable(false);
  }

  @Override
  protected ConflictTraceProxy createTrace
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String tracename = AbstractConflictChecker.getTraceName(model);
    final CompositionalVerificationResult result = getAnalysisResult();
    final ConflictTraceProxy trace =
      (ConflictTraceProxy) result.getCounterExample();
    final ConflictKind kind = trace.getKind();
    return factory.createConflictTraceProxy(tracename,
                                            null,  // comment?
                                            null,
                                            model,
                                            automata,
                                            steps,
                                            kind);
  }

  @Override
  protected void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkConflictCounterExample(steps, automata,
                                             mPreconditionMarking,
                                             mUsedDefaultMarking,
                                             true, translator);
  }


  //#########################################################################
  //# Chains
  private AbstractionProcedure createObservationEquivalenceChain
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
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    if (mPreconditionMarking != null) {
      return new GeneralisedConflictCheckerAbstractionProcedure(chain);
    } else {
      return new ConflictCheckerAbstractionProcedure(chain);
    }
  }

  private AbstractionProcedure createObserverProjectionChain()
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObserverProjectionTRSimplifier op =
      new ObserverProjectionTRSimplifier();
    final int limit = getInternalTransitionLimit();
    op.setTransitionLimit(limit);
    op.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    chain.add(op);
    return new ObserverProjectionAbstractionProcedure(chain, op);
  }

  private AbstractionProcedure createStandardNonblockingAbstractionChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
     final boolean includeNonAlphaDeterminisation,
     final boolean useProperCertainConflicts)
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
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    final int limit = getInternalTransitionLimit();
    incomingEquivalenceSimplifier.setTransitionLimit(limit);
    chain.add(incomingEquivalenceSimplifier);
    final int ccindex;
    if (useProperCertainConflicts)
    {
      final CertainConflictsTRSimplifier certainConflictsRemover = new CertainConflictsTRSimplifier();
      chain.add(certainConflictsRemover);
      ccindex = -1;
    }
    else
    {
      final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
        new LimitedCertainConflictsTRSimplifier();
      ccindex = chain.add(certainConflictsRemover);
    }

    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    if (includeNonAlphaDeterminisation) {
      final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
        new NonAlphaDeterminisationTRSimplifier();
      nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      nonAlphaDeterminiser.setTransitionLimit(limit);
      chain.add(nonAlphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new StandardConflictCheckerAbstractionProcedure(chain, ccindex);
  }

  private AbstractionProcedure createGeneralisedNonblockingAbstractionChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
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
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setTransitionLimit(limit);
    chain.add(nonAlphaDeterminiser);
    if (mPreconditionMarking != null) {
      final AlphaDeterminisationTRSimplifier alphaDeterminiser =
        new AlphaDeterminisationTRSimplifier();
      alphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      alphaDeterminiser.setTransitionLimit(limit);
      chain.add(alphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new GeneralisedConflictCheckerAbstractionProcedure(chain, recoveryIndex);
  }


  //#########################################################################
  //# Events+Automata Maps
  @Override
  protected void initialiseEventsToAutomata()
    throws OverflowException
  {
    super.initialiseEventsToAutomata();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    mAutomatonInfoMap =
      new HashMap<AutomatonProxy,AutomatonInfo>(numAutomata);
  }

  @Override
  protected void removeEventsToAutomata
    (final Collection<AutomatonProxy> victims)
  {
    for (final AutomatonProxy aut : victims) {
      mAutomatonInfoMap.remove(aut);
    }
    super.removeEventsToAutomata(victims);
  }


  //#########################################################################
  //# Proposition Analysis
  private byte getSubsystemPropositionStatus
    (final Collection<AutomatonProxy> automata)
  throws EventNotFoundException
  {
    byte all = ALL_ALPHA | ALL_OMEGA;
    byte none = 0;
    for (final AutomatonProxy aut : automata) {
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
    final CompositionalVerificationResult result = getAnalysisResult();
    if (mPreconditionMarking == null) {
      if (result.getCounterExample() == null) {
        final ConflictTraceProxy trace = createInitialStateTrace();
        result.setCounterExample(trace);
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
      final TraceProxy trace = result.getCounterExample();
      if (trace != null) {
        final ConflictTraceProxy conflict = (ConflictTraceProxy) trace;
        traces.add(conflict);
      }
      if (includeCurrent) {
        if (!isAlphaReachable(builder, getCurrentAutomata(), traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      for (final SubSystem subsys : getPostponedSubsystems()) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      for (final SubSystem subsys : getProcessedSubsystems()) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      final ConflictTraceProxy merged = mergeLanguageInclusionTraces(traces);
      result.setCounterExample(merged);
      return true;
    }
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final SubSystem subsys,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final List<AutomatonProxy> automata = subsys.getAutomata();
    return isAlphaReachable(builder, automata, traces);
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final List<AutomatonProxy> automata,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final ProductDESProxy des = createProductDESProxy(automata);
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
    automata.addAll(getCurrentAutomata());
    for (final SubSystem subsys : getPostponedSubsystems()) {
      final Collection<AutomatonProxy> moreAutomata = subsys.getAutomata();
      automata.addAll(moreAutomata);
    }
    for (final SubSystem subsys : getProcessedSubsystems()) {
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
  //# Inner Enumeration AbstractionMethod
  /**
   * The configuration setting to determine the abstraction method applied
   * to intermediate automata during compositional nonblocking verification.
   */
  public enum AbstractionMethod
  {
    /**
      * <P>Minimisation is performed according to a sequence of abstraction
     * rules for generalised nonblocking proposed, but using weak observation
     * equivalence instead of observation equivalence.</P>
     * <P><I>Reference:</I> Robi Malik, Ryan Leduc. A Compositional Approach
     * for Verifying Generalised Nonblocking, Proc. 7th International
     * Conference on Control and Automation, ICCA'09, 448-453, Christchurch,
     * New Zealand, 2009.</P>
     */
    GNB {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createGeneralisedNonblockingAbstractionChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
      }
    },
    /**
     * <P>Minimisation is performed according to a sequence of abstraction rules
     * for standard nonblocking, but using weak observation
     * equivalence instead of observation equivalence.</P>
     * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
     * Verification in Supervisory Control. SIAM Journal of Control and
     * Optimization, 48(3), 1914-1938, 2009.</P>
     */
    NB {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createStandardNonblockingAbstractionChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, false, false);
      }
    },
    /**
     * <P>Minimisation is performed according to a sequence of abstraction rules
     * for standard nonblocking, but using weak observation equivalence instead
     * of observation equivalence, and with an additional step of non-alpha
     * determinisation at the end.</P>
     * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
     * Verification in Supervisory Control. SIAM Journal of Control and
     * Optimization, 48(3), 1914-1938, 2009.</P>
     */
    NBA {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createStandardNonblockingAbstractionChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, true, false);
      }
    },
    /**
     * Automata are minimised according to <I>certain conflicts</I>.
     */
    CC {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createStandardNonblockingAbstractionChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE, false, true);
      }
    },
    /**
     * Automata are minimised according to <I>observation equivalence</I>.
     */
    OEQ {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createObservationEquivalenceChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE);
      }
    },
    /**
     * Automata are minimised according using <I>observer projection</I>.
     * The present implementation determines a coarsest causal reporter
     * map satisfying the observer property. Nondeterminism in the projected
     * automata is not resolved, nondeterministic abstractions are used instead.
     */
    OP {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createObserverProjectionChain();
      }
    },
    /**
     * <P>Automata are minimised according using an <I>observer projection</I>
     * obtained by the OP-search algorithm.</P>
     *
     * <P><I>Reference.</I> P. N. Pena, J. E. R. Cury, R. Malik, S. Lafortune.
     * Efficient Computation of Observer Projections using OP-Verifiers.
     * Proc. 10th Workshop on Discrete Event Systems, WODES'10, Berlin, 2010,
     * 416-421.</P>
     */
    OPSEARCH {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.new OPSearchAbstractionProcedure();
      }

      @Override
      boolean supportsNondeterminism()
      {
        return false;
      }
    },
    /**
     * <P>Automata are minimised according to <I>weak observation
     * equivalence</I>. Initial states and markings are not saturated, silent
     * transitions are retained instead in a bid to reduce the overall number of
     * transitions.</P>
     *
     * <P><I>Reference.</I> Rong Su, Jan H. van Schuppen, Jacobus E. Rooda,
     * Albert T. Hofkamp. Nonconflict check by using sequential automaton
     * abstractions based on weak observation equivalence. Automatica,
     * <STRONG>46</STRONG>(6), 968--978, 2010.</P>
     */
    WOEQ {
      @Override
      AbstractionProcedure createAbstractionRule
        (final CompositionalConflictChecker checker)
      {
        return checker.createObservationEquivalenceChain
          (ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
      }
    };

    abstract AbstractionProcedure createAbstractionRule
      (CompositionalConflictChecker checker);

    boolean supportsNondeterminism()
    {
      return true;
    }
  }


  //#########################################################################
  //# Inner Class PreselectingMethodFactory
  protected static class PreselectingMethodFactory
    extends AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethodFactory()
    {
      register(MinTa);
    }
  }


  //#########################################################################
  //# Preselection Methods
  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the fewest transitions connected to a precondition-marked
   * state to every other automaton in the model.
   */
  public static final PreselectingMethod MinTa =
      new PreselectingMethod("MinTa")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalConflictChecker checker =
          (CompositionalConflictChecker) analyzer;
      return checker.new HeuristicMinTAlpha();
    }
    @Override
    protected PreselectingMethod getCommonMethod()
    {
      return MinT;
    }
  };


  //#########################################################################
  //# Inner Class SelectingMethodFactory
  protected static class SelectingMethodFactory
    extends AbstractCompositionalModelVerifier.SelectingMethodFactory
  {
    //#######################################################################
    //# Constructors
    protected SelectingMethodFactory()
    {
      register(MinSa);
      register(MinSyncA);
    }
  }


  //#########################################################################
  //# Selection Methods
  /**
   * The selection heuristic that chooses the candidate with the minimum
   * estimated number of precondition-marked states in the synchronous
   * product.
   */
  public static final SelectingMethod MinSa =
      new SelectingMethod("MinSa")
  {
    @Override
    protected SelectingMethod getCommonMethod()
    {
      return MinS;
    }
    @Override
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalConflictChecker checker =
        (CompositionalConflictChecker) analyzer;
      if (checker.mPreconditionMarking == null) {
        return null;
      } else {
        return checker.new ComparatorMinSAlpha();
      }
    }
    @Override
    SelectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalConflictChecker checker =
        (CompositionalConflictChecker) analyzer;
      if (checker.mPreconditionMarking == null) {
        return MinS.createHeuristic(checker);
      } else {
        return super.createHeuristic(checker);
      }
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the minimum
   * number of precondition-marked states in the synchronous product.
   */
  public static final SelectingMethod MinSyncA =
      new SelectingMethod("MinSyncA")
  {
    @Override
    protected SelectingMethod getCommonMethod()
    {
      return MinSync;
    }
    @Override
    SelectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalConflictChecker checker =
        (CompositionalConflictChecker) analyzer;
      if (checker.mPreconditionMarking == null) {
        return MinSync.createHeuristic(checker);
      } else {
        final SelectingMethodFactory factory =
          (SelectingMethodFactory) analyzer.getSelectingMethodFactory();
        final Comparator<Candidate> alt =
          factory.createComparatorChain(analyzer, MinSa);
        return checker.new HeuristicMinSyncAlpha(1, 0, alt);
      }
    }
  };


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
      final EventProxy omega = mUsedDefaultMarking;
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
  //# Inner Class HeuristicMinSync
  private class HeuristicMinSyncAlpha
    extends SelectingHeuristic
    implements MonolithicSynchronousProductBuilder.StateCallback
  {

    //#######################################################################
    //# Constructor
    private HeuristicMinSyncAlpha(final int alphaWeight,
                                  final int nonAlphaWeight,
                                  final Comparator<Candidate> comparator)
    {
      super(comparator);
      mAlphaWeight = alphaWeight;
      mNonAlphaWeight = nonAlphaWeight;
    }

    //#######################################################################
    //# Overrides for SelectingHeuristic
    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      final List<Candidate> list = new ArrayList<Candidate>(candidates);
      final Comparator<Candidate> comparator = getComparator();
      Collections.sort(list, comparator);
      final MonolithicSynchronousProductBuilder builder =
        getCurrentSynchronousProductBuilder();
      final int limit = getCurrentInternalStateLimit();
      builder.setNodeLimit(limit);
      builder.setConstructsResult(false);
      builder.setStateCallback(this);
      mCurrentMinimum = Integer.MAX_VALUE;
      Candidate best = null;
      final List<EventProxy> props =
        Collections.singletonList(mPreconditionMarking);
      builder.setPropositions(props);
      for (final Candidate candidate : list) {
        final List<AutomatonProxy> automata = candidate.getAutomata();
        final ProductDESProxy des = createProductDESProxy(automata);
        builder.setModel(des);
        try {
          mCount = 0;
          builder.run();
          if (mCount < mCurrentMinimum) {
            best = candidate;
            mCurrentMinimum = mCount;
          }
        } catch (final OutOfMemoryError error) {
          getLogger().debug("<out of memory>");
          // skip this one ...
        } catch (final OverflowException overflow) {
          // skip this one ...
        } finally {
          final CompositionalVerificationResult stats = getAnalysisResult();
          final AutomatonResult result = builder.getAnalysisResult();
          stats.addSynchronousProductAnalysisResult(result);
        }
      }
      return best;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# MonolithicSynchronousProductBuilder.StateCounter
    public void countState(final int[] tuple)
      throws OverflowException
    {
      final MonolithicSynchronousProductBuilder builder =
        getCurrentSynchronousProductBuilder();
      boolean alpha = true;
      for (int a = 0; a < tuple.length; a++) {
        final List<EventProxy> props =
          builder.getStateMarking(a, tuple[a]);
        if (props.isEmpty()) {
          alpha = false;
          break;
        }
      }
      if (alpha) {
        mCount += mAlphaWeight;
      } else {
        mCount += mNonAlphaWeight;
      }
      if (mCount >= mCurrentMinimum) {
        throw new OverflowException(OverflowKind.NODE, mCurrentMinimum);
      }
    }

    public void recordStatistics(final AutomatonResult result)
    {
      result.setPeakNumberOfNodes(mCount);
    }

    //#######################################################################
    //# Data Members
    private final int mAlphaWeight;
    private final int mNonAlphaWeight;
    private int mCount;
    private int mCurrentMinimum;

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
  //# Inner Class ConflictCheckerAbstractionProcedure
  private class ConflictCheckerAbstractionProcedure
    extends TRSimplifierAbstractionProcedure
  {

    //#######################################################################
    //# Constructor
    ConflictCheckerAbstractionProcedure
      (final TransitionRelationSimplifier simplifier)
    {
      super(simplifier);
    }

    //#######################################################################
    //# Overrides for TRSimplifierAbstractionProcedure
    @Override
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                                final EventProxy tau)
    {
      final EventEncoding eventEnc =
        super.createEventEncoding(aut, tau);
      final TransitionRelationSimplifier simplifier = getSimplifier();
      final int omega = eventEnc.getEventCode(mUsedDefaultMarking);
      simplifier.setDefaultMarkingID(omega);
      return eventEnc;
    }

    @Override
    protected AbstractionStep createStep(final AutomatonProxy input,
                                         final StateEncoding inputStateEnc,
                                         final AutomatonProxy output,
                                         final StateEncoding outputStateEnc,
                                         final EventProxy tau)
    {
      final TransitionRelationSimplifier simplifier = getSimplifier();
      final List<int[]> partition = simplifier.getResultPartition();
      if (simplifier.isObservationEquivalentAbstraction()) {
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
    //# Trace Recovery
    protected EventProxy getUsedPreconditionMarking()
    {
      return null;
    }

    protected BitSet recoverMarkings(final AutomatonProxy aut,
                                     final EventProxy tau)
    throws AnalysisException
    {
      return null;
    }

  }


  //#########################################################################
  //# Inner Class StandardTRSimplifierAbstractionRule
  private class StandardConflictCheckerAbstractionProcedure
    extends ConflictCheckerAbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    private StandardConflictCheckerAbstractionProcedure
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
    protected ChainTRSimplifier getSimplifier()
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
    //# Overrides for class TRSimplifierAbstractionProcedure
    @Override
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                                final EventProxy tau)
    {
      final EventEncoding eventEnc = super.createEventEncoding(aut, tau);
      int markingID = eventEnc.getEventCode(mUsedDefaultMarking);
      if (markingID < 0) {
        final KindTranslator translator = getKindTranslator();
        markingID =
          eventEnc.addEvent(mUsedDefaultMarking, translator, true);
        final TransitionRelationSimplifier simplifier = getSimplifier();
        simplifier.setDefaultMarkingID(markingID);
      }
      return eventEnc;
    }

    @Override
    protected AbstractionStep createStep(final AutomatonProxy input,
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
  //# Inner Class GeneralisedConflictCheckerAbstractionProcedure
  private class GeneralisedConflictCheckerAbstractionProcedure
    extends ConflictCheckerAbstractionProcedure
  {
    //#######################################################################
    //# Constructors
    private GeneralisedConflictCheckerAbstractionProcedure
      (final ChainTRSimplifier simplifier)
    {
      this(simplifier, -1);
    }

    private GeneralisedConflictCheckerAbstractionProcedure
      (final ChainTRSimplifier simplifier, final int recoveryIndex)
    {
      super(simplifier);
      mRecoveryIndex = recoveryIndex;
      final EventProxy[] props = new EventProxy[2];
      props[0] = mUsedDefaultMarking;
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
    protected ChainTRSimplifier getSimplifier()
    {
      return (ChainTRSimplifier) super.getSimplifier();
    }

    //#######################################################################
    //# Overrides for class TRSimplifierAbstractionProcedure
    @Override
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
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
    protected MergeStep createStep(final AutomatonProxy input,
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
    //# Overrides for class ConflictCheckerAbstractionProcedure
    @Override
    protected EventProxy getUsedPreconditionMarking()
    {
      return mUsedPreconditionMarking;
    }

    @Override
    protected BitSet recoverMarkings(final AutomatonProxy aut,
                                     final EventProxy tau)
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
    private int mPreconditionMarkingID;
    private int mDefaultMarkingID;
  }


  //#########################################################################
  //# Inner Class ObserverProjectionAbstractionProcedure
  private class ObserverProjectionAbstractionProcedure
    extends ConflictCheckerAbstractionProcedure
  {
    //#######################################################################
    //# Constructors
    private ObserverProjectionAbstractionProcedure
      (final ChainTRSimplifier chain,
       final ObserverProjectionTRSimplifier op)
    {
      super(chain);
      mOPSimplifier = op;
    }

    //#######################################################################
    //# Overrides for class TRSimplifierAbstractionProcedure
    @Override
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                                final EventProxy tau)
    {
      final EventEncoding eventEnc = super.createEventEncoding(aut, tau);
      final ProductDESProxyFactory factory = getFactory();
      final String name = "vtau:" + aut.getName();
      final EventProxy vtau =
        factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
      final KindTranslator id = IdenticalKindTranslator.getInstance();
      final int codeOfVTau = eventEnc.addEvent(vtau, id, false);
      mOPSimplifier.setVisibleTau(codeOfVTau);
      return eventEnc;
    }

    //#########################################################################
    //# Data Members
    private final ObserverProjectionTRSimplifier mOPSimplifier;
  }


  //#########################################################################
  //# Inner Class OPSearchAbstractionProcedure
  private class OPSearchAbstractionProcedure
    extends AbstractionProcedure
  {

    //#######################################################################
    //# Constructors
    private OPSearchAbstractionProcedure()
    {
      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      mSimplifier = new OPSearchAutomatonSimplifier(factory, translator);
      final Collection<EventProxy> props = getPropositions();
      mSimplifier.setPropositions(props);
      mStatistics =
        new OPSearchTRSimplifierStatistics(mSimplifier, true, true);
    }

    //#######################################################################
    //# Rule Application
    @Override
    protected boolean run(final AutomatonProxy aut,
                          final Collection<EventProxy> local,
                          final List<AbstractionStep> steps)
      throws AnalysisException
    {
      final long start = System.currentTimeMillis();
      assert local.size() <= 1 : "Only one tau event supported!";
      try {
        mStatistics.recordStart(aut);
        if (local.isEmpty()) {
          mStatistics.recordFinish(aut, false);
          return false;
        }
        final EventProxy tau = local.iterator().next();
        mSimplifier.setModel(aut);
        mSimplifier.setHiddenEvents(local);
        mSimplifier.setOutputHiddenEvent(tau);
        final int limit = getCurrentInternalStateLimit();
        mSimplifier.setNodeLimit(limit);
        mSimplifier.run();
        final OPSearchAutomatonResult result =
          mSimplifier.getAnalysisResult();
        final AutomatonProxy convertedAut = result.getComputedProxy();
        if (aut == convertedAut) {
          mStatistics.recordFinish(aut, false);
          return false;
        }
        mStatistics.recordFinish(convertedAut, true);
        final int iter = result.getNumberOfIterations();
        mStatistics.recordIterations(iter);
        final StateEncoding inputEnc = result.getInputEncoding();
        final StateEncoding outputEnc = result.getOutputEncoding();
        final List<int[]> partition = result.getPartition();
        final ObservationEquivalenceStep step =
          new ObservationEquivalenceStep(convertedAut, aut, tau,
                                         inputEnc, partition, outputEnc);
        steps.add(step);
        return true;
      } catch (final AnalysisException exception) {
        mStatistics.recordOverflow(aut);
        throw exception;
      } catch (final OutOfMemoryError error) {
        mSimplifier.tearDown();
        getLogger().debug("<out of memory>");
        mStatistics.recordOverflow(aut);
        throw new OverflowException(error);
      } finally {
        mSimplifier.tearDown();
        final long stop = System.currentTimeMillis();
        mStatistics.recordRunTime(stop - start);
      }
    }

    @Override
    protected void storeStatistics()
    {
      final CompositionalVerificationResult result = getAnalysisResult();
      final List<OPSearchTRSimplifierStatistics> list =
        Collections.singletonList(mStatistics);
      result.setSimplifierStatistics(list);
    }

    @Override
    protected void resetStatistics()
    {
      mStatistics =
        new OPSearchTRSimplifierStatistics(mSimplifier, true, true);
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
    private OPSearchTRSimplifierStatistics mStatistics;
  }


  //#########################################################################
  //# Inner Class MergeStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * merging states of the original automaton (automaton quotient).
   */
  private abstract class MergeStep extends TRAbstractionStep
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
      super(resultAut, originalAut, tau, originalStateEnc);
      mPartition = partition;
      mHasReducedPreconditionMarking = reduced;
      mReverseOutputStateMap = resultStateEnc.getStateCodeMap();
    }

    //#######################################################################
    //# Trace Computation
    @Override
    protected List<TraceStepProxy> convertTraceSteps
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

    @Override
    protected void setupTraceConversion()
      throws AnalysisException
    {
      super.setupTraceConversion();
      recoverPreconditionMarking();
    }

    @Override
    protected void setupTraceConversion
      (final EventEncoding enc,
       final ListBufferTransitionRelation rel)
    {
      super.setupTraceConversion(enc, rel);
      final AbstractionProcedure proc = getAbstractionProcedure();
      if (proc instanceof ConflictCheckerAbstractionProcedure) {
        final ConflictCheckerAbstractionProcedure cproc =
          (ConflictCheckerAbstractionProcedure) proc;
        final EventProxy alpha = cproc.getUsedPreconditionMarking();
        mPreconditionMarkingID = enc.getEventCode(alpha);
      }
    }

    @Override
    protected void tearDownTraceConversion()
    {
      super.tearDownTraceConversion();
      mTargetSet = null;
      mRecoveredPreconditionMarking = null;
    }

    List<SearchRecord> getCrucialSteps(final List<TraceStepProxy> traceSteps)
    {
      final EventEncoding enc = getEventEncoding();
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
        final int eventID = enc.getEventCode(event);
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
        final ListBufferTransitionRelation rel = getTransitionRelation();
        return rel.isMarked(state, mPreconditionMarkingID);
      }
    }

    //#######################################################################
    //# Trace Computation
    void recoverPreconditionMarking()
      throws AnalysisException
    {
      final AbstractionProcedure proc = getAbstractionProcedure();
      if (proc instanceof ConflictCheckerAbstractionProcedure) {
        final EventEncoding enc = getEventEncoding();
        final ConflictCheckerAbstractionProcedure cproc =
          (ConflictCheckerAbstractionProcedure) proc;
        final EventProxy alpha = cproc.getUsedPreconditionMarking();
        mPreconditionMarkingID = enc.getEventCode(alpha);
        if (mHasReducedPreconditionMarking) {
          final AutomatonProxy aut = getOriginalAutomaton();
          final EventProxy tau = getTau();
          mRecoveredPreconditionMarking = cproc.recoverMarkings(aut, tau);
          if (mPreconditionMarkingID < 0) {
            final KindTranslator translator = getKindTranslator();
            mPreconditionMarkingID = enc.addEvent(alpha, translator, true);
          }
        }
      } else {
        mPreconditionMarkingID = -1;
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * Partition applied to original automaton.
     * Each entry lists states of the input encoding that have been merged.
     */
    private final List<int[]> mPartition;
    /**
     * A flag, indicating that the precondition markings have been reduced
     * during abstraction and need to be recovered for trace expansion.
     * @see #mRecoveredPreconditionMarking
     */
    private final boolean mHasReducedPreconditionMarking;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

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
     * The recovered precondition marking describes the set of alpha-markings
     * resulting from alpha-removal applied to the input automaton. When
     * expanding a counterexample, it is imperative to construct a trace
     * leading to an alpha-marked in this reduced set, even when checking
     * standard nonblocking. Other states may be coreachable in the original
     * automaton, if omega-removal has been used in the abstraction.
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
     * @param  tau               The event representing silent transitions,
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
     * @param  tau               The event representing silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding that relates states in the
     *                           original automaton to state numbers used in
     *                           the partition.
     * @param  partition         Partition that identifies classes of states
     *                           merged during abstraction.
     * @param  reduced           Whether or not the set of precondition
     *                           markings was reduced during abstraction.
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
     * @param  tau               The event representing silent transitions,
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
     * @param  tau               The event representing silent transitions,
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
    protected List<TraceStepProxy> convertTraceSteps
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
      final StandardConflictCheckerAbstractionProcedure proc =
        (StandardConflictCheckerAbstractionProcedure) getAbstractionProcedure();
      final ChainTRSimplifier chain = proc.getSimplifier();
      final int config = chain.getPreferredInputConfiguration();
      rel = new ListBufferTransitionRelation
        (originalAut, eventEnc, mOriginalStateEncoding, config);
      chain.setTransitionRelation(rel);
      final int ccindex = proc.getCertainConflictsIndex();
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
        proc.getCertainConflictsSimplifier();
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
      final int markingID = enc.getEventCode(mUsedDefaultMarking);
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
  //# Data Members
  private AbstractionMethod mAbstractionMethod;
  private EventProxy mDefaultMarking;
  private EventProxy mUsedDefaultMarking;
  private EventProxy mPreconditionMarking;
  private SafetyVerifier mCompositionalSafetyVerifier;
  private SafetyVerifier mMonolithicSafetyVerifier;

  private SafetyVerifier mCurrentCompositionalSafetyVerifier;
  private SafetyVerifier mCurrentMonolithicSafetyVerifier;

  /**
   * The automata currently being analysed. This list is updated after each
   * abstraction step and represents the current state of the model. It may
   * contain abstractions of only part of the original model, if event
   * disjoint subsystems are found.
   * @see #mPostponedSubsystems
   */
  private Map<AutomatonProxy,AutomatonInfo> mAutomatonInfoMap;


  //#########################################################################
  //# Class Constants
  private static final byte NONE_OMEGA = 0x01;
  private static final byte ALL_OMEGA = 0x02;
  private static final byte NONE_ALPHA = 0x04;
  private static final byte ALL_ALPHA = 0x08;

}
