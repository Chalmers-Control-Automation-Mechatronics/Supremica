//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   OPVerifierAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * An experimental abstraction procedure to run OP-Verifier experiments
 * during a compositional conflict check.
 *
 * @author Robi Malik
 */

class OPVerifierAbstractionProcedure
  extends TRConflictEquivalenceAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static OPVerifierAbstractionProcedure createOPVerifierProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = analyzer.getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    return new OPVerifierAbstractionProcedure(analyzer, chain);
  }


  //#########################################################################
  //# Constructor
  private OPVerifierAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier simplifier)
  {
    super(analyzer, simplifier, false);
    mExperiment = OPVerifierExperiment.getInstance();
  }


  //#########################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps, final Candidate cand)
    throws AnalysisException
  {
    if (!local.isEmpty()) {
      assert local.size() <= 1 : "At most one tau event supported!";
      final EventProxy tau = local.iterator().next();
      final EventProxy omega = getUsedDefaultMarking();
      mExperiment.runExperiment(aut, tau, omega);
    }
    return super.run(aut, local, steps, cand);
  }


  //#########################################################################
  //# Data Members
  private final OPVerifierExperiment mExperiment;

}
