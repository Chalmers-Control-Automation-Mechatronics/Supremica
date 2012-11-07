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
  extends ConflictCheckerAbstractionProcedure
{

  //#######################################################################
  //# Constructor
  OPVerifierAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final TransitionRelationSimplifier simplifier)
  {
    super(analyzer, simplifier);
    mExperiment = OPVerifierExperiment.getInstance();
  }

  //#######################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    if (!local.isEmpty()) {
      assert local.size() <= 1 : "At most one tau event supported!";
      final EventProxy tau = local.iterator().next();
      final EventProxy omega = getUsedDefaultMarking();
      mExperiment.runExperiment(aut, tau, omega);
    }
    return super.run(aut, local, steps);
  }

  //#######################################################################
  //# Data Members
  private final OPVerifierExperiment mExperiment;

}
