//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalSafetyVerifier
  extends AbstractTRCompositionalVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public TRCompositionalSafetyVerifier(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final KindTranslator translator,
                                       final SafetyDiagnostics diag)
  {
    super(model, factory, translator,
          new NativeSafetyVerifier(translator, diag, factory));
    mDiagnostics = diag;
    final TransitionRelationSimplifier chain = createDefaultAbstractionChain();
    setSimplifier(chain);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public SafetyTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (SafetyTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    mPropertyAutomata = new ArrayList<>();
    mPropertiesHaveInitialState = true;
    super.setUp();
    final AnalysisResult result = getAnalysisResult();
    if (!result.isFinished()) {
      final Logger logger = getLogger();
      if (mPropertyAutomata.isEmpty()) {
        logger.debug
          ("Terminating early as all specifications are trivially satisfied.");
        result.setSatisfied(true);
      } else if (!mPropertiesHaveInitialState) {
        // TODO If the property has no initial state, and the plant has,
        // then we must return false, with an empty counterexample.
        result.setSatisfied(false);
      }
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPropertyAutomata = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected boolean isFailingEventsUsed()
  {
    return false;
  }

  @Override
  protected boolean isAlwaysEnabledEventsUsed()
  {
    return false;
  }

  @Override
  protected ChainTRSimplifier createDefaultAbstractionChain()
  {
    final ChainTRSimplifier chain = super.createDefaultAbstractionChain();
    final TRSimplificationListener listener = new PartitioningListener();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(listener);
    chain.add(loopRemover);
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(getInternalStateLimit());
    subset.setTransitionLimit(getInternalStateLimit());
    subset.setSimplificationListener(listener);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    final ObservationEquivalenceTRSimplifier.Equivalence eq =
      isSelfloopOnlyEventsUsed() ?
      ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION :
      ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE;
    bisimulator.setEquivalence(eq);
    bisimulator.setTransitionLimit(getInternalStateLimit());
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    return chain;
  }

  @Override
  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut,
                                                    final int config)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getComponentKind(aut)) {
    case PLANT:
      return super.createInitialAutomaton(aut, config);
    case SPEC:
      if (!mPropertiesHaveInitialState) {
        return null;
      }
      final EventEncoding eventEnc = createInitialEventEncoding(aut);
      final TRAutomatonProxy tr = new TRAutomatonProxy(aut, eventEnc, config);
      if (!hasInitialState(tr)) {
        mPropertyAutomata.clear();
        mPropertyAutomata.add(tr);
        mPropertiesHaveInitialState = false;
        return null;
      }
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      final SpecialEventsFinder finder = getSpecialEventsFinder();
      rel.removeProperSelfLoopEvents();
      finder.setBlockedEventsDetected(true);
      finder.setFailingEventsDetected(false);
      finder.setSelfloopOnlyEventsDetected(isSelfloopOnlyEventsEnabled());
      finder.setAlwaysEnabledEventsDetected(isAlwaysEnabledEventsEnabled());
      finder.setTransitionRelation(rel);
      finder.run();
      final byte[] status = finder.getComputedEventStatus();
      final byte controllablePattern = (byte)
        ((isBlockedEventsEnabled() ? EventStatus.STATUS_BLOCKED : 0) |
         (isSelfloopOnlyEventsEnabled() ? EventStatus.STATUS_SELFLOOP_ONLY : 0));
      final byte failingPattern = (byte)
        ((isFailingEventsEnabled() ? EventStatus.STATUS_FAILING : 0) |
         (isAlwaysEnabledEventsEnabled() ? EventStatus.STATUS_ALWAYS_ENABLED : 0));
      boolean trivial = true;
      for (int e = EventEncoding.NONTAU; e < status.length; e++) {
        if (!EventStatus.isUsedEvent(status[e])) {
          // skip
        } else if (EventStatus.isControllableEvent(status[e])) {
          status[e] &= controllablePattern;
        } else if (EventStatus.isBlockedEvent(status[e])) {
          status[e] = failingPattern;
          trivial = false;
        } else {
          status[e] = 0;  // normal external event
          trivial &= EventStatus.isAlwaysEnabledEvent(status[e]);
        }
      }
      if (trivial) {
        final Logger logger = getLogger();
        logger.debug("Dropping trivial specification " + aut.getName() + ".");
      } else {
        mPropertyAutomata.add(tr);
        final TRSubsystemInfo subsys = getCurrentSubsystem();
        subsys.registerEvents(tr, status, true);
      }
      return null;
    default:
      return null;
    }
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    for (final TREventInfo info : subsys.getEvents()) {
      if (info.isExternal()) {
        return false;
      }
    }
    final Logger logger = getLogger();
    logger.debug("Subsystem satisfies property " +
                 "because it shares no events with property automata.");
    dropSubsystem(subsys);
    return true;
  }

  @Override
  protected ProductDESProxy createSubsystemDES(final TRSubsystemInfo subsys)
  {
    final List<TRAutomatonProxy> plants = subsys.getAutomata();
    final String name = AutomatonTools.getCompositionName(plants);
    final int numAutomata = plants.size() + mPropertyAutomata.size();
    final List<TRAutomatonProxy> automata = new ArrayList<>(numAutomata);
    automata.addAll(plants);
    automata.addAll(mPropertyAutomata);
    final ProductDESProxyFactory factory = getFactory();
    return AutomatonTools.createProductDESProxy(name, automata, factory);
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private final SafetyDiagnostics mDiagnostics;

  // Data Structures
  private List<TRAutomatonProxy> mPropertyAutomata;
  private boolean mPropertiesHaveInitialState;

}
