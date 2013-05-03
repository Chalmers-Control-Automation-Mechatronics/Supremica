//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ProjectionStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * @author Robi Malik
 */

class ProjectionStep extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event represent silent transitions,
   *                           or <CODE>null</CODE>.
   */
  ProjectionStep(final CompositionalSafetyVerifier verifier,
                 final AutomatonProxy resultAut,
                 final AutomatonProxy originalAut,
                 final EventProxy tau)
  {
    super(verifier, resultAut, originalAut, tau, null);
  }


  //#########################################################################
  //# Simple Access
  @Override
  CompositionalSafetyVerifier getAnalyzer()
  {
    return (CompositionalSafetyVerifier) super.getAnalyzer();
  }


  //#########################################################################
  //# Trace Computation
  @Override
  List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> traceSteps)
    throws AnalysisException
  {
    final CompositionalSafetyVerifier verifier = getAnalyzer();
    final EventProxy tau = getTau();
    final AutomatonProxy resultAut = getResultAutomaton();
    final AutomatonProxy originalAut = getOriginalAutomaton();
    final ProjectionTraceExpander expander =
      new ProjectionTraceExpander(verifier, tau, resultAut, originalAut);
    return expander.convertTraceSteps(traceSteps);
  }

}
