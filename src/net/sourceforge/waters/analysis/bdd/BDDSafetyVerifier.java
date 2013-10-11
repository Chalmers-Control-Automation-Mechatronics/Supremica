//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;

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
    getLogger().debug("BDDSafetyVerifier.run(): " +
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
      if (reachable != null) {
        reachable.free();
        setSatisfiedResult();
      } else {
        computeCounterExample();
      }
      return isSatisfied();
    } finally {
      tearDown();
      getLogger().debug("BDDSafetyVerifier.run(): " +
                        getModel().getName() + " done.");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mConditionPartitioning != null) {
      mConditionPartitioning.requestAbort();
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
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Algorithm Implementation
  @Override
  void createAutomatonBDDs()
    throws AnalysisAbortException, OverflowException
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
      final Logger logger = getLogger();
      final EventBDD[] eventBDDs = super.createEventBDDs();
      final VerificationResult result = getAnalysisResult();
      if (!result.isFinished()) {
        final BDDFactory bddFactory = getBDDFactory();
        final int limit = getPartitioningSizeLimit();
        mConditionPartitioning = new GreedyPartitioning<ConditionPartitionBDD>
          (bddFactory, ConditionPartitionBDD.class, limit);
        int condcount0 = 0;
        for (final EventBDD eventBDD : eventBDDs) {
          final BDD cond = eventBDD.getControllabilityConditionBDD();
          if (cond != null) {
            final ConditionPartitionBDD part =
              new ConditionPartitionBDD(eventBDD);
            mConditionPartitioning.add(part);
            condcount0++;
          }
        }
        final AutomatonBDD[] automatonBDDs = getAutomatonBDDs();
        mConditionPartitioning.merge(automatonBDDs);
        mConditionBDDs = mConditionPartitioning.getFullPartition();
        mConditionPartitioning = null;
        final int condcount1 = mConditionBDDs.size();
        if (logger.isDebugEnabled() && condcount0 > condcount1) {
          logger.debug("Merged conditions: " + condcount0 +
                       " >> " + condcount1);
        }
      }
      return eventBDDs;
    }
  }

  @Override
  BDD createInitialStateBDD(final AutomatonBDD autBDD)
  {
    final BDDFactory bddFactory = getBDDFactory();
    final BDD autinit = autBDD.createInitialStateBDD(bddFactory);
    if (autinit.isZero()) {
      final VerificationResult result = getAnalysisResult();
      final KindTranslator translator = getKindTranslator();
      if (result.isFinished() && result.isSatisfied()) {
        // result is already set
      } else if (autBDD.getKind() == ComponentKind.PLANT ||
                 translator.getEventKind(KindTranslator.INIT) ==
                 EventKind.CONTROLLABLE) {
        result.setSatisfied(true);
      } else {
        final ProductDESProxyFactory desFactory = getFactory();
        final String tracename = getTraceName();
        final AutomatonProxy aut = autBDD.getAutomaton();
        final String comment = getTraceComment(null, aut, null);
        final ProductDESProxy model = getModel();
        final List<AutomatonProxy> automata = getAutomata();
        final TraceStepProxy step = desFactory.createTraceStepProxy(null);
        final List<TraceStepProxy> steps = Collections.singletonList(step);
        final SafetyTraceProxy counterexample =
          desFactory.createSafetyTraceProxy
          (tracename, comment, null, model, automata, steps);
        result.setCounterExample(counterexample);
      }
      return null;
    } else {
      return autinit;
    }
  }

  @Override
  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException, OverflowException
  {
    for (final ConditionPartitionBDD part : mConditionBDDs) {
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

  private SafetyTraceProxy computeCounterExample()
    throws AnalysisAbortException, OverflowException
  {
    for (final PartitionBDD part : mConditionBDDs) {
      part.dispose();
    }
    final int level = getDepth() - 1;
    final List<TraceStepProxy> trace = computeTrace(mBadStateBDD, level);
    final ProductDESProxyFactory desfactory = getFactory();
    final TraceStepProxy step = desfactory.createTraceStepProxy(mBadEvent);
    trace.add(step);
    final ProductDESProxy des = getModel();
    final String name = getTraceName();
    // TODO String comment = getTraceComment(mBadEvent, null, null);
    final List<AutomatonProxy> automata = getAutomata();
    final SafetyTraceProxy counterex = desfactory.createSafetyTraceProxy
      (name, null, null, des, automata, trace);
    setFailedResult(counterex);
    return counterex;
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
  private Partitioning<ConditionPartitionBDD> mConditionPartitioning;
  private List<ConditionPartitionBDD> mConditionBDDs;
  private BDD mBadStateBDD;
  private EventProxy mBadEvent;

}
