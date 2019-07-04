//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.ArrayList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractDeadlockChecker;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
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
import org.apache.logging.log4j.Logger;

/**
 * <P>A BDD implementation of the deadlock check algorithm.</P>
 *
 * @author Robi Malik
 */

public class BDDDeadlockChecker
  extends BDDModelVerifier
  implements DeadlockChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new BDD-based deadlock checker.
   * @param  factory     The factory used for trace construction.
   */
  public BDDDeadlockChecker(final ProductDESProxyFactory factory)
  {
    this(ConflictKindTranslator.getInstanceUncontrollable(), factory);
  }

  /**
   * Creates a new BDD-based deadlock checker.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDDeadlockChecker(final KindTranslator translator,
                            final ProductDESProxyFactory factory)
  {
    super(translator, factory);
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    LogManager.getLogger().debug("BDDDeadlockChecker.run(): " +
                      getModel().getName() + " ...");
    try {
      setUp();
      createAutomatonBDDs();
      final VerificationResult result = getAnalysisResult();
      if (result.isFinished()) {
        return isSatisfied();
      }
      final BDD init = createInitialStateBDD(true);
      if (result.isFinished()) {
        return isSatisfied();
      }
      createTransitionBDDs();
      if (result.isFinished()) {
        return isSatisfied();
      }
      final BDD reachable = computeReachability(init);
      if (reachable != null) {
        reachable.free();
        setSatisfiedResult();
      } else {
        computeCounterExample();
      }
      return isSatisfied();
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
      LogManager.getLogger().debug("BDDDeadlockChecker.run(): " +
                        getModel().getName() + " done.");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mDeadlockPartitioning != null) {
      mDeadlockPartitioning.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mDeadlockPartitioning != null) {
      mDeadlockPartitioning.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return (ConflictCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Algorithm Implementation
  @Override
  EventBDD createEventBDD(final EventProxy event)
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getEventKind(event)) {
    case UNCONTROLLABLE:
    case CONTROLLABLE:
      final int numAutomata = getNumberOfAutomata();
      final BDDFactory bddFactory = getBDDFactory();
      return new DeadlockEventBDD(event, numAutomata, bddFactory);
    default:
      return null;
    }
  }

  @Override
  void createTransitionBDDs(final TransitionPartitioningStrategy strategy,
                            final EventBDD[] eventBDDs)
    throws AnalysisException
  {
    super.createTransitionBDDs(strategy, eventBDDs);

    final AutomatonBDD[] automatonBDDs = getAutomatonBDDs();
    final BDDFactory bddFactory = getBDDFactory();
    final int limit = getPartitioningSizeLimit();
    mDeadlockPartitioning = new GreedyPartitioning<>
      (bddFactory, DisjunctiveConditionBDD.class, limit);
    final List<TransitionPartitionBDD> transPartition =
      getTransitionPartitioning().getFullPartition();
    int condCount0 = 0;
    for (final TransitionPartitionBDD part : transPartition) {
      checkAbort();
      final BDD bdd = part.getEnabledBDD(automatonBDDs, bddFactory);
      final DisjunctiveConditionBDD cond =
        new DisjunctiveConditionBDD(part, bdd);
      mDeadlockPartitioning.add(cond);
      condCount0++;
    }

    mDeadlockPartitioning.merge(automatonBDDs);
    final List<DisjunctiveConditionBDD> partition =
      mDeadlockPartitioning.getFullPartition();
    final int condCount1 = partition.size();
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled() && condCount0 > condCount1) {
      logger.debug("Merged deadlock conditions: " + condCount0 +
                   " >> " + condCount1);
    }
    if (mDeadlockPartitioning.isDominant()) {
      setSatisfiedResult();
    } else {
      mDeadlockBDDs = new ArrayList<>(condCount1);
      for (final DisjunctiveConditionBDD part : partition) {
        final BDD elig = part.getBDD();
        final BDD deadlock = elig.not();
        mDeadlockBDDs.add(deadlock);
        part.dispose();
      }
    }
    mDeadlockPartitioning = null;
  }

  @Override
  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException, OverflowException
  {
    final BDD possibleDeadlock = reached.id();
    for (final BDD cond : mDeadlockBDDs) {
      possibleDeadlock.andWith(cond.id());
      if (possibleDeadlock.isZero()) {
        return false;
      }
    }
    mBadStateBDD = possibleDeadlock;
    return true;
  }

  private ConflictCounterExampleProxy computeCounterExample()
    throws AnalysisAbortException, OverflowException
  {
    for (final BDD part : mDeadlockBDDs) {
      part.free();
    }
    final int level = getDepth();
    final List<TraceStepProxy> steps = computeTrace(mBadStateBDD, level);
    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = getTraceName();
    final List<AutomatonProxy> automata = getAutomata();
    final TraceProxy trace = desFactory.createTraceProxy(steps);
    final ConflictCounterExampleProxy counterExample =
      desFactory.createConflictCounterExampleProxy
      (name, null, null, des, automata, trace, ConflictKind.DEADLOCK);
    setFailedResult(counterExample);
    return counterExample;
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  private String getTraceName()
  {
    final ProductDESProxy des = getModel();
    return AbstractDeadlockChecker.getTraceName(des);
  }


  //#########################################################################
  //# Data Members
  private Partitioning<DisjunctiveConditionBDD> mDeadlockPartitioning;
  private List<BDD> mDeadlockBDDs;
  private BDD mBadStateBDD;

}
