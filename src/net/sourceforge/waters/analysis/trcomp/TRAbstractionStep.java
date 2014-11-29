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
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.log4j.Logger;


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
  //# Constructors
  TRAbstractionStep(final String name)
  {
    mName = name;
    mOutputAutomaton = null;
  }

  TRAbstractionStep(final TRAutomatonProxy outputAut)
  {
    mName = outputAut.getName();
    mOutputAutomaton = outputAut;
  }


  //#########################################################################
  //# Interface Methods
  abstract Collection<TRAbstractionStep> getPredecessors();

  abstract TRAutomatonProxy createOutputAutomaton(int preferredConfig)
    throws AnalysisException;

  abstract void expandTrace(TRTraceProxy trace)
    throws AnalysisException;


  //#########################################################################
  //# Access Methods
  String getName()
  {
    return mName;
  }

  TRAutomatonProxy getOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    if (mOutputAutomaton == null) {
      mOutputAutomaton = createOutputAutomaton(preferredConfig);
    }
    return mOutputAutomaton;
  }

  TRAutomatonProxy setOutputAutomaton(final TRAutomatonProxy aut)
  {
    final TRAutomatonProxy old = mOutputAutomaton;
    mOutputAutomaton = aut;
    return old;
  }

  TRAbstractionStep getSuccessor()
  {
    return mSuccessor;
  }

  void setSuccessor(final TRAbstractionStep step)
  {
    mSuccessor = step;
  }

  void dispose()
  {
    mOutputAutomaton = null;
    for (final TRAbstractionStep pred : getPredecessors()) {
      pred.mSuccessor = null;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return ProxyTools.getShortClassName(this) + " " + getName();
  }

  public void report(final Logger logger)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Applying " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private TRAutomatonProxy mOutputAutomaton;
  private TRAbstractionStep mSuccessor;

}
