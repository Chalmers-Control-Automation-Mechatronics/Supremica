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

package net.sourceforge.waters.analysis.bdd;

import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.logging.log4j.LogManager;

/**
 * <P>A BDD implementation of a standard conflict checker.</P>
 *
 * @author Robi Malik
 */

public class BDDConflictChecker
  extends BDDModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new BDD-based conflict checker.
   * @param  factory     The factory used for trace construction.
   */
  public BDDConflictChecker(final ProductDESProxyFactory factory)
  {
    this(ConflictKindTranslator.getInstanceUncontrollable(), factory);
  }

  /**
   * Creates a new BDD-based conflict checker.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDConflictChecker(final KindTranslator translator,
                            final ProductDESProxyFactory factory)
  {
    super(translator, factory);
  }

  /**
   * Creates a new BDD-based conflict checker.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDConflictChecker(final KindTranslator translator,
                            final ProductDESProxyFactory desfactory,
                            final BDDPackage bddpackage)
  {
    super(translator, desfactory, bddpackage);
  }

  /**
   * Creates a new BDD-based conflict checker to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  factory     The factory used for trace construction.
   */
  public BDDConflictChecker(final ProductDESProxy model,
                            final ProductDESProxyFactory factory)
  {
    this(model, ConflictKindTranslator.getInstanceUncontrollable(), factory);
  }

  /**
   * Creates a new BDD-based conflict checker to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDConflictChecker(final ProductDESProxy model,
                            final KindTranslator translator,
                            final ProductDESProxyFactory factory)
  {
    super(model, translator, factory);
  }

  /**
   * Creates a new BDD-based conflict checker to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDConflictChecker(final ProductDESProxy model,
                            final KindTranslator translator,
                            final ProductDESProxyFactory desfactory,
                            final BDDPackage bddpackage)
  {
    super(model, translator, desfactory, bddpackage);
  }


  //#########################################################################
  //# Configuration
  /**
   * Requests the computation of a shortest counterexample.
   * With this enabled, the BDD conflict checker performs a strict
   * breadth-first search that ensures a shortest counterexample.
   * This disables early termination when deadlock states are encountered
   * and forces exploration of the full state space.
   */
  public void setShortCounterExampleRequested(final boolean req)
  {
    mShortCounterExampleRequested = req;
  }

  /**
   * Returns whether a short counterexample is requested.
   * @see #setShortCounterExampleRequested(boolean)
   */
  public boolean isShortCounterExampleRequested()
  {
    return mShortCounterExampleRequested;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mPreconditionMarking;
  }

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return (ConflictCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.insertAfter(options,
                   AbstractModelAnalyzerFactory.
                   OPTION_ModelVerifier_ShortCounterExampleRequested,
                   AbstractModelAnalyzerFactory.
                   OPTION_ModelVerifier_DetailedOutputEnabled);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ConflictChecker_ConfiguredPreconditionMarking);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ConflictChecker_ConfiguredDefaultMarking);
    return options;
  }

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
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    LogManager.getLogger().debug("BDDConflictChecker.run(): {} ...",
                                 getModel().getName());
    try {
      setUp();
      createAutomatonBDDs();
      final VerificationResult result = getAnalysisResult();
      if (result.isFinished()) {
        return isSatisfied();
      }
      final EventBDD[] eventBDDs = createTransitionBDDs();
      final boolean earlyDeadlockEnabled = isEarlyDeadlockEnabled();
      BDD init = createInitialStateBDD(earlyDeadlockEnabled);
      if (result.isFinished()) {
        return isSatisfied();
      }
      final BDD reachable = computeReachability(init);
      if (result.isFinished()) {
        return isSatisfied();
      }
      if (mBadStatesBDD != null) {
        mBadStatesBDD.free();
        mBadStatesBDD = null;
      }
      if (mPreconditionBDD != null) {
        mPreconditionBDD.andWith(reachable.id());
      }
      final BDD coreachable = computeCoreachability(mMarkingBDD, reachable);
      mMarkingBDD = null;
      if (result.isFinished()) {
        return isSatisfied();
      }
      final BDD pre =
        mPreconditionBDD == null ? reachable.id() : mPreconditionBDD;
      mPreconditionBDD = null;
      reachable.free();
      mBadStatesBDD = pre.andWith(coreachable.not());
      if (mBadStatesBDD.isZero()) {
        return setSatisfiedResult();
      } else if (earlyDeadlockEnabled) {
        coreachable.free();
        mConflictKind = ConflictKind.LIVELOCK;
        final ConflictCounterExampleProxy counterExample =
          computeCounterExample(mBadStatesBDD);
        return setFailedResult(counterExample);
      } else {
        coreachable.free();
        mConflictKind = ConflictKind.CONFLICT;
        super.createTransitionBDDs(TransitionPartitioningStrategy.GREEDY,
                                   eventBDDs);
        init = createInitialStateBDD(true);
        computeReachability(init);
        return false;
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } catch (final WatersRuntimeException exception) {
      if (exception.getCause() instanceof AnalysisException) {
        final AnalysisException cause = (AnalysisException) exception.getCause();
        throw setExceptionResult(cause);
      } else {
        throw exception;
      }
    } finally {
      tearDown();
      LogManager.getLogger().debug("BDDConflictChecker.run(): {} done",
                                   getModel().getName());
    }
  }


  //#########################################################################
  //# Algorithm Implementation
  @Override
  public void tearDown()
  {
    super.tearDown();
    mUsedMarking = null;
    mMarkingBDD = null;
    mPreconditionBDD = null;
    mBadStatesBDD = null;
  }

  @Override
  void createTransitionBDDs(final TransitionPartitioningStrategy strategy,
                            final EventBDD[] eventBDDs)
    throws AnalysisException
  {
    super.createTransitionBDDs(strategy, eventBDDs);

    final EventProxy omega = getUsedMarkingProposition();
    mMarkingBDD = getMarkedStateBDD(omega);
    if (mMarkingBDD.isOne()) {
      setSatisfiedResult();
      return;
    }
    if (mPreconditionMarking != null) {
      mPreconditionBDD = getMarkedStateBDD(mPreconditionMarking);
      mPreconditionBDD.applyWith(mMarkingBDD.id(), BDDFactory.diff);
      if (mPreconditionBDD.isZero()) {
        setSatisfiedResult();
        return;
      } else if (mPreconditionBDD.isOne()) {
        mPreconditionBDD = null;
      }
    }
    if (mPreconditionBDD == null && mMarkingBDD.isZero()) {
      mConflictKind = ConflictKind.CONFLICT;
      final BDD init = createInitialStateBDD(true);
      final ConflictCounterExampleProxy counterexample =
        computeCounterExample(init, 0);
      setFailedResult(counterexample);
      return;
    }
    final int limit = getPartitioningSizeLimit();
    if (isEarlyDeadlockEnabled() && limit > 0) {
      final BDD nonDeadlock = mMarkingBDD.id();
      final AutomatonBDD[] automatonBDDs = getAutomatonBDDs();
      final BDDFactory factory = getBDDFactory();
      final List<TransitionPartitionBDD> partitioning =
        getTransitionPartitioning().getFullPartition();
      for (final TransitionPartitionBDD part : partitioning) {
        checkAbort();
        final BDD nondeadlockPart =
          part.getStronglyEnabledBDD(automatonBDDs, factory);
        nonDeadlock.orWith(nondeadlockPart);
        if (nonDeadlock.nodeCount() > limit) {
          nonDeadlock.free();
          return;
        }
      }
      final BDD deadlock = nonDeadlock.not();
      if (mPreconditionBDD != null) {
        deadlock.andWith(mPreconditionBDD.id());
      }
      if (!deadlock.isZero()) {
        mConflictKind = ConflictKind.DEADLOCK;
        mBadStatesBDD = deadlock;
      }
    }
  }

  @Override
  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException, OverflowException
  {
    if (mBadStatesBDD != null) {
      final BDD bad = reached.and(mBadStatesBDD);
      if (!bad.isZero()) {
        final int level = getDepth();
        final ConflictCounterExampleProxy counterExample =
          computeCounterExample(bad, level);
        setFailedResult(counterExample);
        return true;
      }
    }
    return false;
  }

  @Override
  boolean isCoreachabilityExhausted(final BDD coreached)
  {
    if (mPreconditionBDD != null) {
      final BDD imp = mPreconditionBDD.imp(coreached);
      if (imp.isOne()) {
        return setSatisfiedResult();
      }
      imp.free();
    }
    return false;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns whether early deadlock detection is enabled during the initial
   * reachability search. This is used to decide whether a BDD representing
   * deadlock states should be constructed and checked against after every
   * step. The method can only be called after the transition partitioning
   * has been constructed, because the decision also depends on whether the
   * search is BFS or not.
   */
  private boolean isEarlyDeadlockEnabled()
  {
    return
      getTransitionPartitioning().isStrictBFS() ||
      !isShortCounterExampleRequested();
  }

  /**
   * Gets the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setConfiguredDefaultMarking(EventProxy) setMarkingProposition()} method, if
   * non-null, or the default marking proposition of the input model.
   * @throws IllegalArgumentException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  private EventProxy getUsedMarkingProposition()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.findMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }

  private ConflictCounterExampleProxy computeCounterExample(final BDD bad,
                                                            final int index)
    throws AnalysisAbortException, OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final List<TraceStepProxy> trace = computeTrace(bad, index);
      return createCounterExample(trace);
    } else {
      return null;
    }
  }

  private ConflictCounterExampleProxy computeCounterExample(final BDD bad)
    throws AnalysisAbortException, OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final List<TraceStepProxy> trace = computeTrace(bad);
      return createCounterExample(trace);
    } else {
      return null;
    }
  }

  private ConflictCounterExampleProxy createCounterExample
    (final List<TraceStepProxy> steps)
  {
    final ProductDESProxyFactory desfactory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = AbstractConflictChecker.getTraceName(des);
    final List<AutomatonProxy> automata = getAutomata();
    final TraceProxy trace = desfactory.createTraceProxy(steps);
    return desfactory.createConflictCounterExampleProxy
      (name, null, null, des, automata, trace, mConflictKind);
  }


  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;
  private EventProxy mPreconditionMarking;
  private boolean mShortCounterExampleRequested;
  private BDD mMarkingBDD;
  private BDD mPreconditionBDD;
  private ConflictKind mConflictKind = ConflictKind.CONFLICT;
  private BDD mBadStatesBDD;

}
