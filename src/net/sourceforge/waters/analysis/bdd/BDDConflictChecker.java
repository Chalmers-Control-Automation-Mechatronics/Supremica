//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.des.ConflictKind;

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
    this(ConflictKindTranslator.getInstance(), factory);
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
    this(model, ConflictKindTranslator.getInstance(), factory);
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
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    getLogger().debug("BDDConflictChecker.run(): " +
                      getModel().getName() + " ...");
    try {
      setUp();
      createAutomatonBDDs();
      final VerificationResult result = getAnalysisResult();
      if (result.isFinished()) {
        return isSatisfied();
      }
      createEventBDDs();
      if (result.isFinished()) {
        return isSatisfied();
      }
      final BDD reachable = computeReachability();
      if (result.isFinished()) {
        return isSatisfied();
      }
      if (mDeadlockBDD != null) {
        mDeadlockBDD.free();
        mDeadlockBDD = null;
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
      final BDD bad = pre.andWith(coreachable.not());
      if (bad.isZero()) {
        return setSatisfiedResult();
      } else {
        coreachable.free();
        final ConflictTraceProxy counterexample =
          computeCounterExample(bad, ConflictKind.LIVELOCK);
        return setFailedResult(counterexample);
      }
    } catch (final WatersRuntimeException exception) {
      if (exception.getCause() instanceof AnalysisException) {
        throw (AnalysisException) exception.getCause();
      } else {
        throw exception;
      }
    } finally {
      tearDown();
      getLogger().debug("BDDConflictChecker.run(): " +
                        getModel().getName() + " done.");
    }
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
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
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
    mDeadlockBDD = null;
  }

  @Override
  EventBDD[] createEventBDDs()
    throws AnalysisException
  {
    final EventBDD[] eventBDDs = super.createEventBDDs();
    final VerificationResult result = getAnalysisResult();
    if (result.isFinished()) {
      return eventBDDs;
    }
    final EventProxy omega = getUsedMarkingProposition();
    mMarkingBDD = getMarkedStateBDD(omega);
    if (mMarkingBDD.isOne()) {
      setSatisfiedResult();
      return eventBDDs;
    }
    if (mPreconditionMarking != null) {
      mPreconditionBDD = getMarkedStateBDD(mPreconditionMarking);
      mPreconditionBDD.applyWith(mMarkingBDD.id(), BDDFactory.diff);
      if (mPreconditionBDD.isZero()) {
        setSatisfiedResult();
        return eventBDDs;
      } else if (mPreconditionBDD.isOne()) {
        mPreconditionBDD = null;
      }
    }
    if (mPreconditionBDD == null && mMarkingBDD.isZero()) {
      final BDD init = getInitialStateBDD();
      final ConflictTraceProxy counterexample =
        computeCounterExample(init, 0, ConflictKind.CONFLICT);
      setFailedResult(counterexample);
      return eventBDDs;
    }
    final int limit = getPartitioningSizeLimit();
    if (limit > 0) {
      final BDD nondeadlock = mMarkingBDD.id();
      final AutomatonBDD[] automatonBDDs = getAutomatonBDDs();
      final BDDFactory factory = getBDDFactory();
      final List<TransitionPartitionBDD> partitioning =
        getTransitionPartitioning().getFullPartition();
      for (final TransitionPartitionBDD part : partitioning) {
        checkAbort();
        final BDD nondeadlockPart =
          part.getNonDeadlockBDD(automatonBDDs, factory);
        nondeadlock.orWith(nondeadlockPart);
        if (nondeadlock.nodeCount() > limit) {
          nondeadlock.free();
          return eventBDDs;
        }
      }
      final BDD deadlock = nondeadlock.not();
      if (mPreconditionBDD != null) {
        deadlock.andWith(mPreconditionBDD.id());
      }
      if (!deadlock.isZero()) {
        mDeadlockBDD = deadlock;
      }
    }
    return eventBDDs;
  }

  @Override
  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException, OverflowException
  {
    if (mDeadlockBDD != null) {
      final BDD bad = reached.and(mDeadlockBDD);
      if (!bad.isZero()) {
        final int level = getDepth() - 1;
        final ConflictTraceProxy counterexample =
          computeCounterExample(bad, level, ConflictKind.DEADLOCK);
        setFailedResult(counterexample);
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
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }

  private ConflictTraceProxy computeCounterExample(final BDD bad,
                                                   final int index,
                                                   final ConflictKind kind)
    throws AnalysisAbortException, OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final List<TraceStepProxy> trace = computeTrace(bad, index);
      return createCounterExample(trace, kind);
    } else {
      return null;
    }
  }

  private ConflictTraceProxy computeCounterExample(final BDD bad,
                                                   final ConflictKind kind)
    throws AnalysisAbortException, OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final List<TraceStepProxy> trace = computeTrace(bad);
      return createCounterExample(trace, kind);
    } else {
      return null;
    }
  }

  private ConflictTraceProxy createCounterExample
    (final List<TraceStepProxy> trace, final ConflictKind kind)
  {
    final ProductDESProxyFactory desfactory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = AbstractConflictChecker.getTraceName(des);
    final List<AutomatonProxy> automata = getAutomata();
    return desfactory.createConflictTraceProxy
      (name, null, null, des, automata, trace, kind);
  }


  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;
  private EventProxy mPreconditionMarking;
  private BDD mMarkingBDD;
  private BDD mPreconditionBDD;
  private BDD mDeadlockBDD;

}
