//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * Abstract base class for abstraction steps recorded during compositional
 * verification.
 *
 * An abstraction step contains information about an intermediate automaton
 * and how it was created. Abstraction steps are linked to their predecessors
 * and successors to form a bidirectional tree. Methods are provided to
 * facilitate trace computation.
 *
 * @author Robi Malik
 */

abstract class TRAbstractionStep
{

  //#########################################################################
  //# Interface Methods
  abstract Collection<TRAbstractionStep> getPredecessors();

  abstract TRAutomatonProxy createOutputAutomaton(int preferredConfig)
    throws AnalysisException;

  abstract void expandTrace(TRTraceProxy trace)
    throws AnalysisException;


  //#########################################################################
  //# Access Methods
  TRAutomatonProxy getOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    if (mOutputAutomaton == null) {
      mOutputAutomaton = createOutputAutomaton(preferredConfig);
    }
    return mOutputAutomaton;
  }

  void setOutputAutomaton(final TRAutomatonProxy aut)
  {
    mOutputAutomaton = aut;
  }

  TRAbstractionStep getSuccessor()
  {
    return mSuccessor;
  }

  void setSuccessor(final TRAbstractionStep step)
  {
    mSuccessor = step;
  }


  //#########################################################################
  //# Data Members
  private TRAutomatonProxy mOutputAutomaton;
  private TRAbstractionStep mSuccessor;

}
