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
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
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
    super.setUp();
    if (mPropertyAutomata.isEmpty()) {
      setSatisfiedResult();
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
  protected ChainTRSimplifier createDefaultAbstractionChain()
  {
    // TODO
    final ChainTRSimplifier chain = super.createDefaultAbstractionChain();
    final TRSimplificationListener listener = new PartitioningListener();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(listener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                               Equivalence.OBSERVATION_EQUIVALENCE);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
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
      // TODO
      final EventEncoding eventEnc = createInitialEventEncoding(aut);
      final TRAutomatonProxy tr = new TRAutomatonProxy(aut, eventEnc, config);
      if (!hasInitialState(tr)) {
        // TODO If the property has no initial state, and the plant has,
        // then we must return false, with an empty counterexample.
        setSatisfiedResult();
        return null;
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


  //#########################################################################
  //# Auxiliary Methods


  //#########################################################################
  //# Data Members
  // Configuration
  private final SafetyDiagnostics mDiagnostics;

  // Data Structures
  private List<TRAutomatonProxy> mPropertyAutomata;

}
