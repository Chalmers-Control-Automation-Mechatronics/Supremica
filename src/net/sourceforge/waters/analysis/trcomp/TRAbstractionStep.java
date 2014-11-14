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
import net.sourceforge.waters.model.analysis.OverflowException;


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

  abstract TRAbstractionStep getSuccessor();

  abstract TRAutomatonProxy createOutputAutomaton()
    throws OverflowException;

  abstract void expandTrace(TRTraceProxy trace);


  //#########################################################################
  //# Access Methods
  void setOutputAutomaton(final TRAutomatonProxy aut)
  {
    mOutputAutomaton = aut;
  }

  TRAutomatonProxy getOutputAutomaton()
    throws OverflowException
  {
    if (mOutputAutomaton == null) {
      mOutputAutomaton = createOutputAutomaton();
    }
    return mOutputAutomaton;
  }


  //#########################################################################
  //# Data Members
  private TRAutomatonProxy mOutputAutomaton;

}
