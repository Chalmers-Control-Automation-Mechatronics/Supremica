//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.AlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SelfloopSubsumptionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictCheckResult;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.AbstractTRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A compositional conflict checker based on {@link TRAutomatonProxy}
 * that can be configured to use different abstraction sequences for its
 * simplification steps.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractTRCompositionalModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker()
  {
    this(null);
  }

  public TRCompositionalConflictChecker(final ProductDESProxy model)
  {
    super(model,
          ConflictKindTranslator.getInstanceControllable(),
          new NativeConflictChecker(ProductDESElementFactory.getInstance()));
    setFailingEventsEnabled(true);
    setSelfloopOnlyEventsEnabled(true);
    setAlwaysEnabledEventsEnabled(true);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyzer
  @Override
  public CompositionalConflictCheckResult getAnalysisResult()
  {
    return (CompositionalConflictCheckResult) super.getAnalysisResult();
  }

  @Override
  public CompositionalConflictCheckResult createAnalysisResult()
  {
    return new CompositionalConflictCheckResult(getClass());
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

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictCounterExampleProxy) result.getCounterExample();
  }

  /**
   * Sets whether deadlock states are pruned in synchronous products.
   * @see TRSynchronousProductBuilder#setPruningDeadlocks(boolean)
   */
  public void setPruningDeadlocks(final boolean pruning)
  {
    mPruningDeadlocks = pruning;
  }

  /**
   * Returns whether deadlock states are pruned.
   * @see #setPruningDeadlocks(boolean) setPruningDeadlocks()
   */
  public boolean isPruningDeadlocks()
  {
    return mPruningDeadlocks;
  }


  //#########################################################################
  //# Configuration
  @Override
  public EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactory()
  {
    return getTRSimplifierFactoryStatic();
  }

  public static EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactoryStatic()
  {
    return
      new ListedEnumFactory<TRToolCreator<TransitionRelationSimplifier>>() {
      {
        register(OEQ);
        register(NB0);
        register(NB1);
        register(NB2, true);
        register(NB3);
        register(GNB);
      }
    };
  }

  public void setUsingLimitedCertainConflicts(final boolean conflicts)
  {
    mUsingLimitedCertainConflicts = conflicts;
  }

  public boolean isUsingLimitedCertainConflicts()
  {
    return mUsingLimitedCertainConflicts;
  }

  @Override
  public ConflictChecker getMonolithicAnalyzer()
  {
    return (ConflictChecker) super.getMonolithicAnalyzer();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    final ListIterator<Option<?>> iter = options.listIterator();
    while (iter.hasNext()) {
      final Option<?> option = iter.next();
      if (option.hasID(TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic)) {
        iter.remove();
        final Option<?> replacement =
          db.get(TRCompositionalModelAnalyzerFactory.
                 OPTION_TRCompositionalConflictChecker_PreselectionHeuristic);
        iter.add(replacement);
      } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                              OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic)) {
        iter.remove();
        final Option<?> replacement =
          db.get(TRCompositionalModelAnalyzerFactory.
                 OPTION_TRCompositionalConflictChecker_SelectionHeuristic);
        iter.add(replacement);
        final Option<?> addition =
          db.get(TRCompositionalModelAnalyzerFactory.
                 OPTION_TRCompositionalConflictChecker_SimplifierCreator);
        iter.add(addition);
      } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                              OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence)) {
        final Option<?> addition =
          db.get(TRCompositionalModelAnalyzerFactory.
                 OPTION_TRCompositionalConflictChecker_LimitedCertainConflicts);
        iter.add(addition);
      }
    }
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ConflictChecker_ConfiguredPreconditionMarking);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ConflictChecker_ConfiguredDefaultMarking);
    db.append(options,  AbstractModelAnalyzerFactory.
                        OPTION_SynchronousProductBuilder_PruningDeadlocks);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_TRCompositionalConflictChecker_Chain);
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_ConflictChecker_ConfiguredDefaultMarking)) {
      final PropositionOption propOption = (PropositionOption) option;
      setConfiguredDefaultMarking(propOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ConflictChecker_ConfiguredPreconditionMarking)) {
      final PropositionOption propOption = (PropositionOption) option;
      setConfiguredPreconditionMarking(propOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_TRCompositionalConflictChecker_PreselectionHeuristic)) {
      final EnumOption<TRPreselectionHeuristic> enumOption =
        (EnumOption<TRPreselectionHeuristic>) option;
      setPreselectionHeuristic(enumOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_TRCompositionalConflictChecker_SelectionHeuristic)) {
      final EnumOption<SelectionHeuristic<TRCandidate>> enumOption =
        (EnumOption<SelectionHeuristic<TRCandidate>>) option;
      setSelectionHeuristic(enumOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_TRCompositionalConflictChecker_SimplifierCreator)) {
      final EnumOption<TRToolCreator<TransitionRelationSimplifier>> enumOption =
        (EnumOption<TRToolCreator<TransitionRelationSimplifier>>) option;
      setSimplifierCreator(enumOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_TRCompositionalConflictChecker_LimitedCertainConflicts)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setUsingLimitedCertainConflicts(boolOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SynchronousProductBuilder_PruningDeadlocks)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setPruningDeadlocks(boolOption.getValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mLanguageInclusionChecker != null) {
      mLanguageInclusionChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mLanguageInclusionChecker != null) {
      mLanguageInclusionChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    mUsedPreconditionMarking = mConfiguredPreconditionMarking;
    super.setUp();
    final ConflictChecker mono = getMonolithicAnalyzer();
    mono.setConfiguredDefaultMarking(getUsedDefaultMarking());
    mono.setConfiguredPreconditionMarking(getUsedPreconditionMarking());
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
    mUsedPreconditionMarking = null;
    mLanguageInclusionChecker = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected boolean isFailingEventsUsed()
  {
    return isFailingEventsEnabled() && getUsedPreconditionMarking() == null;
  }

  @Override
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedDefaultMarking == null) {
      if (mConfiguredDefaultMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedDefaultMarking =
          AbstractConflictChecker.findMarkingProposition(model);
      } else {
        mUsedDefaultMarking = mConfiguredDefaultMarking;
      }
    }
    return mUsedDefaultMarking;
  }

  @Override
  protected EventProxy getUsedPreconditionMarking()
  {
    return mUsedPreconditionMarking;
  }

  @Override
  protected void addAuxiliaryEvents(final EventEncoding enc)
    throws AnalysisException
  {
    final EventProxy defaultMarking = getUsedDefaultMarking();
    enc.addProposition(defaultMarking, false);
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    if (preconditionMarking != null) {
      enc.addProposition(preconditionMarking, false);
    }
  }

  @Override
  protected StateProxy findDumpState(final AutomatonProxy aut)
    throws EventNotFoundException
  {
    if (mConfiguredPreconditionMarking == null) {
      final EventProxy marking = getUsedDefaultMarking();
      final Collection<EventProxy> markings = Collections.singletonList(marking);
      return AutomatonTools.findDumpState(aut, markings);
    } else {
      return null;
    }
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    boolean allAutomataOmega = true;
    TRAutomatonProxy omegaBlocker = null;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      boolean noStatesReachable = true;
      boolean noStatesOmega = rel.isPropositionUsed(DEFAULT_MARKING);
      boolean noStatesAlpha = rel.isPropositionUsed(PRECONDITION_MARKING);
      for (int s = 0; s < rel.getNumberOfStates(); s++) {
        if (rel.isReachable(s)) {
          noStatesReachable = false;
          final boolean alpha = rel.isMarked(s, PRECONDITION_MARKING);
          final boolean omega = rel.isMarked(s, DEFAULT_MARKING);
          if (alpha) {
            noStatesAlpha = false;
            if (!omega) {
              allAutomataOmega = false;
            }
          }
          if (omega) {
            noStatesOmega = false;
          }
        }
      }
      if (noStatesReachable) {
        logger.debug("The system is generalised nonblocking, because " +
                     aut.getName() + " has no reachable states.");
        return setSatisfiedResult();
      } else if (noStatesAlpha) {
        logger.debug("The system is generalised nonblocking, because " +
                     aut.getName() + " has no precondition-marked states.");
        return setSatisfiedResult();
      } else if (noStatesOmega) {
        if (mUsedPreconditionMarking == null) {
          setBooleanResult(false);
          logger.debug("Subsystem is blocking, because " + aut.getName() +
                       " has no marked states.");
          dropPendingSubsystems();
          subsys.moveToEnd(aut);
          dropSubsystem(subsys);
          return true;
        } else if (omegaBlocker == null) {
          omegaBlocker = aut;
        }
      }
    }
    if (allAutomataOmega) {
      if (mConfiguredPreconditionMarking == null) {
        logger.debug("Subsystem is nonblocking, because all states are marked.");
      } else {
        logger.debug("Subsystem is generalised nonblocking, because all " +
                     "precondition-marked states have the default marking.");
      }
      if (!getPendingSubsystems().isEmpty()) {
        final List<TRAbstractionStep> steps =
          checkForReachablePreconditionMarkedState(subsys);
        addAbstractionSteps(steps);
      }
      return true;
    } else if (omegaBlocker != null) {
      logger.debug("Subsystem is generalised blocking, because " +
                   omegaBlocker.getName() + " has no marked state.");
      subsys.moveToEnd(omegaBlocker);
      final List<TRAbstractionStep> steps =
        checkForReachablePreconditionMarkedState(subsys);
      if (steps == null) {
        setBooleanResult(true);
      } else if (checkPendingSubsystemsForReachablePreconditionMarkedState()) {
        addAbstractionSteps(steps);
        setBooleanResult(false);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected AbstractTRSynchronousProductBuilder createSynchronousProductBuilder()
  {
    final KindTranslator translator = getKindTranslator();
    final AbstractTRSynchronousProductBuilder builder =
      new TRSynchronousProductBuilder();
    builder.setDetailedOutputEnabled(true);
    builder.setKindTranslator(translator);
    builder.setRemovingSelfloops(true);
    builder.setNodeLimit(getInternalStateLimit());
    builder.setTransitionLimit(getInternalTransitionLimit());
    builder.setPruningDeadlocks(mPruningDeadlocks);
    return builder;
  }

  @Override
  protected boolean analyseSubsystemMonolithically
    (final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    if (subsys == null || subsys.getNumberOfAutomata() == 0) {
      return true;
    } else {
      final List<TRAutomatonProxy> automata = subsys.getAutomata();
      final String name = AutomatonTools.getCompositionName(automata);
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy(name, automata, factory);
      recordMonolithicAttempt(des);
      final ModelVerifier mono = getMonolithicVerifier();
      mono.setModel(des);
      final CompositionalAnalysisResult combinedResult = getAnalysisResult();
      final AnalysisResult monolithicResult;
      try {
        mono.run();
      } finally {
        monolithicResult = mono.getAnalysisResult();
        combinedResult.addMonolithicAnalysisResult(monolithicResult);
      }
      final Logger logger = LogManager.getLogger();
      if (monolithicResult.isSatisfied()) {
        if (mConfiguredPreconditionMarking == null) {
          logger.debug("Subsystem is nonblocking.");
          dropSubsystem(subsys);
        } else {
          logger.debug("Subsystem is generalised nonblocking.");
          if (!getPendingSubsystems().isEmpty()) {
            final List<TRAbstractionStep> steps =
              checkForReachablePreconditionMarkedState(subsys);
            addAbstractionSteps(steps);
          }
        }
        return true;
      } else {
        if (mConfiguredPreconditionMarking == null) {
          logger.debug("Subsystem is blocking.");
          dropPendingSubsystems();
        } else {
          logger.debug("Subsystem is generalised blocking.");
          if (!checkPendingSubsystemsForReachablePreconditionMarkedState()) {
            return true;
          }
        }
        combinedResult.setSatisfied(false);
        if (isCounterExampleEnabled()) {
          final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
          final CounterExampleProxy counter = mono.getCounterExample();
          final TRTraceProxy extension =
            TRAbstractionStepMonolithic.createTraceExtension(counter, preds, this);
          final TRAbstractionStep step =
            new TRAbstractionStepMonolithic(name, extension);
          addAbstractionStep(step);
        }
        return false;
      }
    }
  }

  @Override
  protected TRConflictTraceProxy createEmptyTrace(final ProductDESProxy des)
  {
    return new TRConflictTraceProxy(des);
  }

  @Override
  protected void checkIntermediateCounterExample(final TRTraceProxy trace)
    throws AnalysisException
  {
    final TRConflictTraceProxy conflictTrace = (TRConflictTraceProxy) trace;
    final TRConflictTraceProxy cloned =
      new TRConflictTraceProxy(conflictTrace);
    cloned.setUpForTraceChecking();
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkConflictCounterExample(cloned,
                                             getUsedPreconditionMarking(),
                                             getUsedDefaultMarking(),
                                             true,
                                             translator);
  }

  void recordCCLanguageInclusionChecks(final int count, final long time)
  {
    final CompositionalConflictCheckResult result = getAnalysisResult();
    result.addCCLanguageInclusionChecks(count, time);
  }


  //#########################################################################
  //# Generalised Nonblocking Trace Extension
  /**
   * <P>Checks whether a precondition-marked state is reachable in all
   * pending subsystems.</P>
   *
   * <P>This method is invoked in a generalised nonblocking check when a
   * subsystem has been found to be generalised blocking. In this case, if
   * the system has been split into event-disjoint subsystems, it still needs
   * to be checked whether a precondition-marked state is reachable.</P>
   *
   * <P>This method checks each pending subsystem for the reachability of
   * a precondition-marked state, if necessary by means of time-consuming
   * language inclusion checks. If a subsystem has such a state, the
   * counterexample is recorded and appropriate abstraction steps are filed.
   * If a subsystem does not have such a state, the global analysis result is
   * set to <CODE>true</CODE>, indicating that the system is generalised
   * nonblocking, and the checking is stopped.</P>
   *
   * @return <CODE>true</CODE> if all pending subsystems have been found
   *         to have a reachable precondition-marked state,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean checkPendingSubsystemsForReachablePreconditionMarkedState()
    throws AnalysisException
  {
    for (final TRSubsystemInfo subsys : getPendingSubsystems()) {
      final List<TRAbstractionStep> steps =
        checkForReachablePreconditionMarkedState(subsys);
      if (steps == null) {
        return false;
      }
      addAbstractionSteps(steps);
    }
    return true;
  }

  /**
   * <P>Checks whether the given subsystem has a reachable precondition-marked
   * state.</P>
   *
   * <P>This method is used in generalised nonblocking verification when
   * the model splits into event-disjoint subsystems. Then a system is
   * generalised nonblocking, if each subsystem is generalised nonblocking,
   * or if one subsystem has no reachable precondition-marked state.</P>
   *
   * <P>This method may need to invoke a time-consuming language inclusion
   * check. Depending on the subsystem, it performs one of the following
   * actions.</P>
   * <UL>
   * <LI>If no precondition marking is configured, or none of the automata in
   *     the subsystem uses the precondition marking, a list of abstraction
   *     steps of type {@link TRAbstractionStepDrop} is returned, indicating
   *     to trace expansion that every initial state is a valid end state for
   *     a counterexample.</LI>
   * <LI>If a reachable precondition-marked state can be found by the
   *     language inclusion check, an abstraction step of type {@link
   *     TRAbstractionStepMonolithic} containing the counterexample is
   *     returned.</LI>
   * <LI>If the language inclusion check determines that the subsystem has
   *     no reachable precondition-marked state, the global analysis
   *     result is set to <CODE>true</CODE> indicating that the complete
   *     system is generalised nonblocking. In this case, the method
   *     returns <CODE>null</CODE>.</LI>
   * </UL>
   *
   * @param  subsys  The subsystem to be analysed.
   * @return List of abstraction steps for trace expansion. The steps
   *         are returned separately, because the trace expansion steps
   *         for the blocking subsystem must be recorded last for sound
   *         trace checking. If there are pending subsystem to be checked
   *         for reachable alpha-marked states, the trace steps have to be
   *         recorded later. The returned list may be empty if counterexample
   *         computation is disabled. A return value of <CODE>null</CODE>
   *         indicates that the subsystem has no reachable
   *         precondition-marked state.
   */
  private List<TRAbstractionStep> checkForReachablePreconditionMarkedState
    (final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    if (mConfiguredPreconditionMarking == null) {
      return createDropSteps(subsys);
    }
    final Logger logger = LogManager.getLogger();
    logger.debug("Checking subsystem for reachable precondition-marked state ...");
    final TRLanguageInclusionChecker checker =
      getLanguageInclusionChecker();
    final TRToolCreator<TransitionRelationSimplifier> creator =
      checker.getSimplifierCreator();
    final TransitionRelationSimplifier simplifier =
      creator.create(mLanguageInclusionChecker.getCompositionalAnalyzer());
    final int config = simplifier.getPreferredInputConfiguration();
    final int numAutomata = subsys.getNumberOfAutomata();
    final List<TRAutomatonProxy> langAutomata = new ArrayList<>(numAutomata + 1);
    final Map<TRAutomatonProxy,TRAutomatonProxy> langMap =
      new HashMap<>(numAutomata);
    boolean trivial = true;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final TRAutomatonProxy langAut =
        createPreconditionCheckAutomaton(aut, config);
      langAutomata.add(langAut);
      langMap.put(langAut, aut);
      trivial &= aut == langAut;
    }
    if (trivial) {
      return createDropSteps(subsys);
    }
    final TRAutomatonProxy property = getPreconditionPropertyAutomaton(config);
    langAutomata.add(property);
    final ProductDESProxyFactory factory = getFactory();
    final String name = AutomatonTools.getCompositionName(langAutomata);
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(name, langAutomata, factory);
    checker.setModel(des);
    if (checker.run()) {
      logger.debug("The global system is generalised nonblocking, because " +
                   "this subsystem has no reachable precondition-marked state.");
      final AnalysisResult result = getAnalysisResult();
      result.setSatisfied(true);
      return null;
    } else {
      final TRSafetyTraceProxy langTrace =
        (TRSafetyTraceProxy) checker.getCounterExample();
      final TRConflictTraceProxy confTrace =
        new TRConflictTraceProxy(langTrace);
      for (final TRAutomatonProxy langAut : langAutomata) {
        final TRAutomatonProxy aut = langMap.get(langAut);
        final TRAbstractionStep step = getAbstractionStep(aut);
        if (step == null) {
          confTrace.removeInputAutomaton(langAut);
        } else {
          confTrace.replaceInputAutomaton(langAut, step);
        }
      }
      final TRAbstractionStep step =
        new TRAbstractionStepMonolithic(des.getName(), confTrace);
      return Collections.singletonList(step);
    }
  }

  private TRAutomatonProxy createPreconditionCheckAutomaton
    (final TRAutomatonProxy aut, final int config)
  {
    final EventEncoding enc = aut.getEventEncoding();
    if (enc.isPropositionUsed(PRECONDITION_MARKING)) {
      final EventEncoding langEnc = new EventEncoding(enc);
      langEnc.setPropositionUsed(PRECONDITION_MARKING, false);
      final EventProxy event = getPreconditionEvent();
      final int e = langEnc.addProperEvent(event, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final ListBufferTransitionRelation langRel =
        new ListBufferTransitionRelation(rel, langEnc, config);
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      langRel.setReachable(dumpIndex, true);
      boolean allMarked = true;
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s)) {
          if (rel.isMarked(s, PRECONDITION_MARKING)) {
            langRel.addTransition(s, e, dumpIndex);
          } else {
            allMarked = false;
          }
        }
      }
      return allMarked ? aut : new TRAutomatonProxy(langEnc, langRel);
    } else {
      return aut;
    }
  }

  private EventProxy getPreconditionEvent()
  {
    if (mPreconditionEvent == null) {
      final ProductDESProxyFactory factory = getFactory();
      final String name = mConfiguredPreconditionMarking.getName();
      mPreconditionEvent =
        factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
    }
    return mPreconditionEvent;
  }

  private TRAutomatonProxy getPreconditionPropertyAutomaton(final int config)
    throws OverflowException
  {
    if (mPreconditionPropertyAutomaton == null) {
      final EventEncoding enc = new EventEncoding();
      final EventProxy event = getPreconditionEvent();
      enc.addProperEvent(event, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(PROPERTY_NAME, ComponentKind.PROPERTY,
                                         enc, 1, config);
      rel.setInitial(0, true);
      mPreconditionPropertyAutomaton = new TRAutomatonProxy(enc, rel);
    }
    return mPreconditionPropertyAutomaton;
  }


  //#########################################################################
  //# Abstraction Chains
  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB0 =
    new TRToolCreator<TransitionRelationSimplifier>("NB0")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
      throws AnalysisConfigurationException
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (analyzer.isUsingWeakObservationEquivalence()
          ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
          : Equivalence.OBSERVATION_EQUIVALENCE,
         checker.isUsingLimitedCertainConflicts(), false, false, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB1 =
    new TRToolCreator<TransitionRelationSimplifier>("NB1")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
      throws AnalysisConfigurationException
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (analyzer.isUsingWeakObservationEquivalence()
          ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
            : Equivalence.OBSERVATION_EQUIVALENCE,
            checker.isUsingLimitedCertainConflicts(), true, false, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB2 =
    new TRToolCreator<TransitionRelationSimplifier>("NB2")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
      throws AnalysisConfigurationException
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (analyzer.isUsingWeakObservationEquivalence()
          ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
            : Equivalence.OBSERVATION_EQUIVALENCE,
            checker.isUsingLimitedCertainConflicts(), true, false, true);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Selfloop subsumption ({@link SelfloopSubsumptionTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB3 =
    new TRToolCreator<TransitionRelationSimplifier>("NB3")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
      throws AnalysisConfigurationException
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (analyzer.isUsingWeakObservationEquivalence()
          ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
            : Equivalence.OBSERVATION_EQUIVALENCE,
            checker.isUsingLimitedCertainConflicts(), true, true, true);
    }
  };

  /**
   * <P>An abstraction sequence for generalised nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Omega-removal ({@link OmegaRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Alpha determinisation ({@link AlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> GNB =
    new TRToolCreator<TransitionRelationSimplifier>("GNB")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createGeneralisedNonblockingChain
        (analyzer.isUsingWeakObservationEquivalence()
          ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
            : Equivalence.OBSERVATION_EQUIVALENCE,
         true);
    }
  };


  protected ChainTRSimplifier createConflictEquivalenceChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
     final boolean certainConflicts,
     final boolean earlyTransitionRemoval,
     final boolean selfloopSubsumption,
     final boolean nonAlphaDeterminisation)
    throws AnalysisConfigurationException
  {
    checkForStandardNonblocking();

    final int limit = getInternalTransitionLimit();
    final TRSimplificationListener specialEventsListener = getSpecialEventsListener();
    final TRSimplificationListener markingListener = new MarkingListener();
    final TRSimplificationListener partitioningListener = new PartitioningListener();
    final TRSimplificationListener certainConflictsListener = new CertainConflictsListener();

    final ChainTRSimplifier chain =
      ChainBuilder.createConflictEquivalenceChain(equivalence,
                                                certainConflicts,
                                                earlyTransitionRemoval,
                                                selfloopSubsumption,
                                                nonAlphaDeterminisation,
                                                specialEventsListener,
                                                markingListener,
                                                partitioningListener,
                                                certainConflictsListener);

    chain.setTransitionLimit(limit);
    chain.setDefaultMarkingID(DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    return chain;
  }

  protected ChainTRSimplifier createGeneralisedNonblockingChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
     final boolean earlyTransitionRemoval)
  {
    if (mUsedPreconditionMarking == null) {
      final ProductDESProxy model = getModel();
      final ProductDESProxyFactory factory = getFactory();
      mUsedPreconditionMarking =
        AbstractConflictChecker.createNewPreconditionMarking(model, factory);
    }

    final boolean hasConfiguredPreconditionMarking = (mUsedPreconditionMarking != null);
    final int limit = getInternalTransitionLimit();
    final TRSimplificationListener specialEventsListener = getSpecialEventsListener();
    final TRSimplificationListener markingListener = new MarkingListener();
    final TRSimplificationListener omegaRemovalListener = new OmegaRemovalListener();
    final TRSimplificationListener partitioningListener = new PartitioningListener();

    final ChainTRSimplifier chain =
      ChainBuilder.createGeneralisedNonblockingChain(equivalence,
                                                   earlyTransitionRemoval,
                                                   hasConfiguredPreconditionMarking,
                                                   specialEventsListener,
                                                   markingListener,
                                                   omegaRemovalListener,
                                                   partitioningListener);

    chain.setTransitionLimit(limit);
    chain.setPropositions(PRECONDITION_MARKING, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    return chain;
  }


  //#########################################################################
  //# Auxiliary Methods
  TRLanguageInclusionChecker getLanguageInclusionChecker()
  {
    if (mLanguageInclusionChecker == null) {
      mLanguageInclusionChecker = new TRLanguageInclusionChecker();
      mLanguageInclusionChecker.
        setInternalStateLimit(getInternalStateLimit());
      mLanguageInclusionChecker.
        setInternalTransitionLimit(getInternalTransitionLimit());
      mLanguageInclusionChecker.
        setMonolithicStateLimit(getMonolithicStateLimit());
      mLanguageInclusionChecker.
        setMonolithicTransitionLimit(getMonolithicTransitionLimit());
      mLanguageInclusionChecker.
        setBlockedEventsEnabled(isBlockedEventsEnabled());
      mLanguageInclusionChecker.
        setFailingEventsEnabled(isFailingEventsEnabled());
      mLanguageInclusionChecker.
        setSelfloopOnlyEventsEnabled(isSelfloopOnlyEventsEnabled());
      mLanguageInclusionChecker.
        setAlwaysEnabledEventsEnabled(isAlwaysEnabledEventsEnabled());
      mLanguageInclusionChecker.setPreservingEncodings(true);
    }
    return mLanguageInclusionChecker;
  }


  //#########################################################################
  //# Error Handling
  private void checkForStandardNonblocking()
    throws AnalysisConfigurationException
  {
    if (mConfiguredPreconditionMarking != null) {
      final TRToolCreator<?> creator = getSimplifierCreator();
      throw new AnalysisConfigurationException
        (creator.getName() +
         " abstraction chain does not support generalised nonblocking!");
    }
  }


  //#########################################################################
  //# Inner Class MarkingListener
  class MarkingListener extends PartitioningListener
  {
    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.analysis.abstraction.TRSimplificationListener
    @Override
    public boolean onSimplificationStart
      (final TransitionRelationSimplifier simplifier)
    {
      if (super.onSimplificationStart(simplifier)) {
        final ListBufferTransitionRelation rel =
          simplifier.getTransitionRelation();
        if (simplifier instanceof MarkingRemovalTRSimplifier &&
            isCounterExampleEnabled()) {
          mNumberOfDefaultMarkingsAtStart =
            rel.getNumberOfMarkings(DEFAULT_MARKING, true);
          mNumberOfPreconditionMarkingsAtStart =
            rel.getNumberOfMarkings(PRECONDITION_MARKING, true);
        }
        return true;
      } else {
        return false;
      }
    }

    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      if (result && isCounterExampleEnabled() && seq != null) {
        if (simplifier instanceof MarkingRemovalTRSimplifier) {
          mEventEncodingAtStart = seq.getCurrentEventEncoding();
        } else if (simplifier instanceof MarkingSaturationTRSimplifier) {
          final ListBufferTransitionRelation rel =
            simplifier.getTransitionRelation();
          final int numPreconditionMarkings =
            rel.getNumberOfMarkings(PRECONDITION_MARKING, true);
          final TransitionRelationSimplifier last =
            seq.getLastPartitionSimplifier();
          final EventEncoding enc = mEventEncodingAtStart;
          mEventEncodingAtStart = null;
          if (last instanceof MarkingRemovalTRSimplifier) {
            seq.removeLastPartitionSimplifier(enc);
            if (mNumberOfPreconditionMarkingsAtStart ==
                numPreconditionMarkings &&
                rel.getNumberOfMarkings(DEFAULT_MARKING, true) ==
                mNumberOfDefaultMarkingsAtStart) {
              return;
            }
          }
        }
      } else {
        mEventEncodingAtStart = null;
      }
      super.onSimplificationFinish(simplifier, result);
    }

    //#######################################################################
    //# Data Members
    private EventEncoding mEventEncodingAtStart;
    private int mNumberOfDefaultMarkingsAtStart;
    private int mNumberOfPreconditionMarkingsAtStart;
  }


  //#########################################################################
  //# Inner Class CertainConflictsListener
  class CertainConflictsListener extends PartitioningListener
  {
    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      if (result && isCounterExampleEnabled() && seq != null) {
        final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
          (LimitedCertainConflictsTRSimplifier) simplifier;
        if (certainConflictsRemover.hasCertainConflictTransitions()) {
          final TRAbstractionStep pred =
            seq.getLastIntermediateStepOrPredecessor();
          final EventEncoding enc = seq.getCurrentEventEncoding();
          final TRAbstractionStep step =
            new TRAbstractionStepCertainConflicts(pred, enc,
                                                  certainConflictsRemover);
          seq.append(step);
        } else {
          super.onSimplificationFinish(simplifier, result);
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class OmegaRemovalListener
  class OmegaRemovalListener extends PartitioningListener
  {
    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      super.onSimplificationFinish(simplifier, result);
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      final ListBufferTransitionRelation rel =
        simplifier.getTransitionRelation();
      if (result && isCounterExampleEnabled() && seq != null &&
          rel.isPropositionUsed(PRECONDITION_MARKING)) {
        final TransitionRelationSimplifier chain = getSimplifier();
        final TRPartition partition = chain.getResultPartition();
        final int numStates = partition == null ?
          rel.getNumberOfStates() : partition.getNumberOfStates();
        final BitSet markings = new BitSet(numStates);
        for (int s = 0; s < numStates; s++) {
          final int clazz = partition == null ? s : partition.getClassCode(s);
          if (clazz >= 0 && rel.isMarked(clazz, PRECONDITION_MARKING)) {
            markings.set(s);
          }
        }
        final TRAbstractionStepPartition step = (TRAbstractionStepPartition)
          seq.getLastIntermediateStepOrPredecessor();
        step.setRelevantPreconditionMarkings(markings);
      }
    }
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;
  private boolean mPruningDeadlocks = true;

  // Auxiliary events, status, and tools
  private EventProxy mUsedDefaultMarking;
  private EventProxy mUsedPreconditionMarking;
  private TRLanguageInclusionChecker mLanguageInclusionChecker;

  // For language inclusion check for generalised nonblocking
  private EventProxy mPreconditionEvent;
  private TRAutomatonProxy mPreconditionPropertyAutomaton;

  private boolean mUsingLimitedCertainConflicts = true;


  //#########################################################################
  //# Class Constants
  private static final String PROPERTY_NAME = ":never";

}
