//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <P>A BDD implementation of a general safety verifier.</P>
 *
 * @author Robi Malik
 */

public class BDDSafetyVerifier
  extends BDDModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new BDD-based safety verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDSafetyVerifier(final KindTranslator translator,
                           final SafetyDiagnostics diag,
                           final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
  }

  /**
   * Creates a new BDD-based safety verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDSafetyVerifier(final KindTranslator translator,
                           final SafetyDiagnostics diag,
                           final ProductDESProxyFactory desfactory,
                           final BDDPackage bddpackage)
  {
    this(null, translator, diag, desfactory, bddpackage);
  }

  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   */
  public BDDSafetyVerifier(final ProductDESProxy model,
                           final KindTranslator translator,
                           final SafetyDiagnostics diag,
                           final ProductDESProxyFactory desfactory)
  {
    super(model, translator, desfactory);
    mDiagnostics = diag;
  }

  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDSafetyVerifier(final ProductDESProxy model,
                           final KindTranslator translator,
                           final SafetyDiagnostics diag,
                           final ProductDESProxyFactory desfactory,
                           final BDDPackage bddpackage)
  {
    super(model, translator, desfactory, bddpackage);
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    LogManager.getLogger().debug("BDDSafetyVerifier.run(): " +
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
      } else if (isDetailedOutputEnabled()) {
        computeCounterExample();
      } else {
        setBooleanResult(false);
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
      LogManager.getLogger().debug("BDDSafetyVerifier.run(): " +
                                   getModel().getName() + " done.");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    if (mConditionPartitioning != null) {
      mConditionPartitioning.requestAbort(sender);
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mConditionPartitioning != null) {
      mConditionPartitioning.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.bdd.BDDModelVerifier
  @Override
  void createAutomatonBDDs()
    throws AnalysisAbortException
  {
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    boolean trivial = true;
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        trivial = false;
        break;
      }
    }
    if (trivial) {
      setSatisfiedResult();
    } else {
      super.createAutomatonBDDs();
    }
  }

  @Override
  EventBDD[] createEventBDDs()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    boolean trivial = true;
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        trivial = false;
        break;
      }
    }
    if (trivial) {
      setSatisfiedResult();
      return null;
    } else {
      return super.createEventBDDs();
    }
  }

  @Override
  void createTransitionBDDs(final TransitionPartitioningStrategy strategy,
                            final EventBDD[] eventBDDs)
    throws AnalysisException
  {
    super.createTransitionBDDs(strategy, eventBDDs);

    final BDDFactory bddFactory = getBDDFactory();
    final int limit = getPartitioningSizeLimit();
    mConditionPartitioning = new GreedyPartitioning<ConjunctiveConditionBDD>
    (bddFactory, ConjunctiveConditionBDD.class, limit);
    int condcount0 = 0;
    for (final EventBDD eventBDD : eventBDDs) {
      final BDD cond = eventBDD.getControllabilityConditionBDD();
      if (cond != null) {
        final BitSet automata = eventBDD.getControllabilityTestedAutomata();
        final ConjunctiveConditionBDD part =
          new ConjunctiveConditionBDD(eventBDD, cond, automata);
        mConditionPartitioning.add(part);
        condcount0++;
      }
    }
    final AutomatonBDD[] automatonBDDs = getAutomatonBDDs();
    mConditionPartitioning.merge(automatonBDDs);
    mConditionBDDs = mConditionPartitioning.getFullPartition();
    mConditionPartitioning = null;
    final int condcount1 = mConditionBDDs.size();
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled() && condcount0 > condcount1) {
      logger.debug("Merged conditions: " + condcount0 +
                   " >> " + condcount1);
    }
  }

  @Override
  BDD createInitialStateBDD()
    throws AnalysisException
  {
    final BDDFactory bddFactory = getBDDFactory();
    final BDD initial = bddFactory.one();
    AutomatonBDD emptySpecBDD = null;
    for (final AutomatonBDD autBDD : getAutomatonBDDs()) {
      checkAbort();
      final BDD autInit = createInitialStateBDD(autBDD);
      if (autInit == null) {
        final KindTranslator translator = getKindTranslator();
        if (autBDD.getKind() == ComponentKind.PLANT ||
            translator.getEventKind(LanguageInclusionKindTranslator.INIT) ==
            EventKind.CONTROLLABLE) {
          setSatisfiedResult();
          return null;
        } else {
          emptySpecBDD = autBDD;
        }
      } else {
        initial.andWith(autInit);
      }
    }
    if (emptySpecBDD != null) {
      final ProductDESProxyFactory desFactory = getFactory();
      final String tracename = getTraceName();
      final AutomatonProxy aut = emptySpecBDD.getAutomaton();
      final String comment = getTraceComment(null, aut, null);
      final ProductDESProxy model = getModel();
      final List<AutomatonProxy> automata = getAutomata();
      final TraceStepProxy step = desFactory.createTraceStepProxy(null);
      final List<TraceStepProxy> steps = Collections.singletonList(step);
      final TraceProxy trace = desFactory.createTraceProxy(steps);
      final SafetyCounterExampleProxy counterexample =
        desFactory.createSafetyCounterExampleProxy
        (tracename, comment, null, model, automata, trace);
      setFailedResult(counterexample);
      return null;
    }
    return initial;
  }

  @Override
  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException
  {
    for (final ConjunctiveConditionBDD part : mConditionBDDs) {
      BDD condpart = part.getBDD();
      BDD imp = reached.imp(condpart);
      if (!imp.isOne()) {
        final Map<EventProxy,PartitionBDD> map = part.getComponents();
        final Iterator<Map.Entry<EventProxy,PartitionBDD>> iter =
          map.entrySet().iterator();
        Map.Entry<EventProxy,PartitionBDD> entry = iter.next();
        if (iter.hasNext()) {
          while (true) {
            checkAbort();
            imp.free();
            condpart = entry.getValue().getBDD();
            imp = reached.imp(condpart);
            if (!imp.isOne()) {
              break;
            }
            entry = iter.next();
          }
        }
        mBadEvent = entry.getKey();
        mBadStateBDD = imp.not();
        imp.free();
        return true;
      }
    }
    return false;
  }

  private SafetyCounterExampleProxy computeCounterExample()
    throws AnalysisAbortException
  {
    for (final PartitionBDD part : mConditionBDDs) {
      part.dispose();
    }
    final int level = getDepth();
    final List<TraceStepProxy> steps = computeTrace(mBadStateBDD, level);
    final ProductDESProxyFactory desFactory = getFactory();
    final TraceStepProxy step = desFactory.createTraceStepProxy(mBadEvent);
    steps.add(step);
    final ProductDESProxy des = getModel();
    final String traceName = getTraceName();
    // TODO String comment = getTraceComment(mBadEvent, ?, ?);
    final List<AutomatonProxy> automata = getAutomata();
    final TraceProxy trace = desFactory.createTraceProxy(steps);
    final SafetyCounterExampleProxy counterexample =
      desFactory.createSafetyCounterExampleProxy
      (traceName, null, null, des, automata, trace);
    setFailedResult(counterexample);
    return counterexample;
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  private String getTraceName()
  {
    final ProductDESProxy des = getModel();
    if (mDiagnostics == null) {
      final String desname = des.getName();
      return desname + "-unsafe";
    } else {
      return mDiagnostics.getTraceName(des);
    }
  }

  /**
   * Generates a comment to be used for a counterexample generated for
   * the current model.
   * @param  event  The event that causes the safety property under
   *                investigation to fail.
   * @param  aut    The automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @param  state  The state in the automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @return An English string that describes why the safety property is
   *         violated, which can be used as a trace comment.
   */
  private String getTraceComment(final EventProxy event,
                                 final AutomatonProxy aut,
                                 final StateProxy state)
  {
    if (mDiagnostics == null) {
      return null;
    } else {
      final ProductDESProxy des = getModel();
      return mDiagnostics.getTraceComment(des, event, aut, state);
    }
  }


  //#########################################################################
  //# Data Members
  private final SafetyDiagnostics mDiagnostics;
  private Partitioning<ConjunctiveConditionBDD> mConditionPartitioning;
  private List<ConjunctiveConditionBDD> mConditionBDDs;
  private BDD mBadStateBDD;
  private EventProxy mBadEvent;

}
