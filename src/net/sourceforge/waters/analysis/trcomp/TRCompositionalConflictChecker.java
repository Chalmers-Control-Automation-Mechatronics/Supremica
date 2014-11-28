//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractTRCompositionalVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public TRCompositionalConflictChecker(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory)
  {
    super(model, factory,
          ConflictKindTranslator.getInstanceControllable(),
          new NativeConflictChecker(factory));
    final TransitionRelationSimplifier chain = createDefaultAbstractionChain();
    setSimplifier(chain);
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
  public ConflictTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected ChainTRSimplifier createDefaultAbstractionChain()
  {
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
    chain.setPropositions(PRECONDITION_MARKING, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    return chain;
  }

  @Override
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedDefaultMarking == null) {
      if (mConfiguredDefaultMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedDefaultMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedDefaultMarking = mConfiguredDefaultMarking;
      }
    }
    return mUsedDefaultMarking;
  }

  @Override
  protected EventProxy getUsedPreconditionMarking()
  {
    // TODO Create precondition marking for GNB chain
    return getConfiguredPreconditionMarking();
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    boolean allStatesMarked = true;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(DEFAULT_MARKING)) {
        boolean noStatesMarked = true;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s)) {
            if (rel.isMarked(s, DEFAULT_MARKING)) {
              noStatesMarked = false;
            } else {
              allStatesMarked = false;
            }
            if (!allStatesMarked && !noStatesMarked) {
              break;
            }
          }
        }
        if (noStatesMarked) {
          final AnalysisResult result = getAnalysisResult();
          result.setSatisfied(false);
          getLogger().debug("Subsystem is blocking, because " + aut.getName() +
                            " has no marked states.");
          dropSubsystem(subsys);
          return true;
        }
      }
    }
    // TODO Generalised nonblocking stuff ...
    if (allStatesMarked) {
      final Logger logger = getLogger();
      logger.debug("Subsystem is nonblocking, because all states are marked.");
      dropSubsystem(subsys);
    }
    return allStatesMarked;
  }


  //#########################################################################
  //# Auxiliary Methods


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;

  private EventProxy mUsedDefaultMarking;

}
