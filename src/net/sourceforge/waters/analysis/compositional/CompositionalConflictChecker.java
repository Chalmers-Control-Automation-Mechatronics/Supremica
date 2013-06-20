//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
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
import net.sourceforge.waters.xsd.des.ConflictKind;


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
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker without a model or marking proposition.
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalConflictChecker
    (final ProductDESProxyFactory factory,
     final ConflictAbstractionProcedureFactory abstractionFactory)
  {
    this(null, null, factory, abstractionFactory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to its default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final ProductDESProxy model,
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
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalConflictChecker(final ProductDESProxy model,
                                      final EventProxy marking,
                                      final ProductDESProxyFactory factory)
  {
    this(model, marking, factory, ConflictAbstractionProcedureFactory.OEQ);
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
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalConflictChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory,
     final ConflictAbstractionProcedureFactory abstractionFactory)
  {
    this(model,
         marking,
         factory,
         abstractionFactory,
         new PreselectingMethodFactory(),
         new SelectingMethodFactory());
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
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalConflictChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory,
     final ConflictAbstractionProcedureFactory abstractionFactory,
     final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory preselectingMethodFactory,
     final AbstractCompositionalModelAnalyzer.SelectingMethodFactory selectingMethodFactory)
  {
    super(model,
          factory,
          ConflictKindTranslator.getInstance(),
          abstractionFactory,
          preselectingMethodFactory,
          selectingMethodFactory);
    setPruningDeadlocks(true);
    setConfiguredDefaultMarking(marking);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Configuration
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
      final EventProxy defaultMarking = getUsedDefaultMarking();
      current.setConfiguredDefaultMarking(defaultMarking);
      final EventProxy preconditionMarking = getUsedPreconditionMarking();
      current.setConfiguredPreconditionMarking(preconditionMarking);
      setCurrentMonolithicVerifier(current);
      super.setupMonolithicVerifier();
    }
  }

  SafetyVerifier getCurrentCompositionalSafetyVerifier()
  {
    return mCurrentCompositionalSafetyVerifier;
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
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mCurrentCompositionalSafetyVerifier != null) {
      mCurrentCompositionalSafetyVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mCurrentCompositionalSafetyVerifier != null) {
      mCurrentCompositionalSafetyVerifier.resetAbort();
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
    final EventProxy defaultMarking = createDefaultMarking();
    final AbstractionProcedureFactory abstraction =
      getAbstractionProcedureFactory();
    final EventProxy preconditionMarking;
    if (abstraction.expectsAllMarkings()) {
      preconditionMarking = createPreconditionMarking();
    } else {
      preconditionMarking = getConfiguredPreconditionMarking();
    }
    setPropositionsForMarkings(defaultMarking, preconditionMarking);
    super.setUp();
    setupSafetyVerifiers();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCurrentCompositionalSafetyVerifier = null;
    mCurrentMonolithicSafetyVerifier = null;
    mAutomatonInfoMap = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected HidingStep createSynchronousProductStep
    (final Collection<AutomatonProxy> automata,
     final AutomatonProxy sync,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
  {
    final SynchronousProductBuilder builder = getSynchronousProductBuilder();
    final SynchronousProductStateMap stateMap =  builder.getStateMap();
    return new ConflictHidingStep(this, sync, hidden, tau, stateMap);
  }

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
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    TraceChecker.checkConflictCounterExample(steps, automata,
                                             preconditionMarking,
                                             defaultMarking,
                                             true, translator);
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
    final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
    final CompositionalVerificationResult result = getAnalysisResult();
    if (preconditionMarking == null) {
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
                                       preconditionMarking,
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
    final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
    final String name = model.getName() + preconditionMarking.getName();
    final ProductDESProxyFactory factory = getFactory();
    final TraceStepProxy initStep = factory.createTraceStepProxy(null, initMap);
    steps.set(0, initStep);
    return factory.createConflictTraceProxy(name, null, null, model, automata,
                                            steps, ConflictKind.CONFLICT);
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
      if (checker.getUsedPreconditionMarking() == null) {
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
      if (checker.getUsedPreconditionMarking() == null) {
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
      if (checker.getUsedPreconditionMarking() == null) {
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
          final EventProxy alpha = getUsedPreconditionMarking();
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
      final EventProxy alpha = getUsedPreconditionMarking();
      final EventProxy omega = getUsedDefaultMarking();
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
    @Override
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
    @Override
    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      final List<Candidate> list = new ArrayList<Candidate>(candidates);
      final Comparator<Candidate> comparator = getComparator();
      Collections.sort(list, comparator);
      final MonolithicSynchronousProductBuilder builder =
        getSynchronousProductBuilder();
      final int limit = getCurrentInternalStateLimit();
      builder.setNodeLimit(limit);
      builder.setConstructsResult(false);
      builder.setStateCallback(this);
      mCurrentMinimum = Integer.MAX_VALUE;
      Candidate best = null;
      final EventProxy alpha = getUsedPreconditionMarking();
      final List<EventProxy> props = Collections.singletonList(alpha);
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
    @Override
    public void countState(final int[] tuple)
      throws OverflowException
    {
      final MonolithicSynchronousProductBuilder builder =
        getSynchronousProductBuilder();
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

    @Override
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
    @Override
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
  //# Data Members
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
