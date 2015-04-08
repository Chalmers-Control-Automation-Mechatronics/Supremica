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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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


  //#########################################################################
  //# Interface Methods
  abstract Collection<TRAbstractionStep> getPredecessors();

  abstract TRAutomatonProxy createOutputAutomaton(int preferredConfig)
    throws AnalysisException;

  abstract void expandTrace(TRTraceProxy trace,
                            AbstractTRCompositionalAnalyzer analyzer)
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

  void provideOutputAutomaton(final TRAutomatonProxy aut)
  {
    mOutputAutomaton = aut;
  }

  void clearOutputAutomaton()
  {
    mOutputAutomaton = null;
  }

  TRAutomatonProxy getClonedOutputAutomaton(final EventEncoding enc,
                                            final int preferredConfig)
    throws AnalysisException
  {
    final TRAutomatonProxy aut = getOutputAutomaton(preferredConfig);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    assert numEvents == enc.getNumberOfProperEvents() :
      "Unexpected number of events in event encoding!";
    final ListBufferTransitionRelation clonedRel =
      new ListBufferTransitionRelation(rel, enc, preferredConfig);
    return new TRAutomatonProxy(enc, clonedRel);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return ProxyTools.getShortClassName(this) + " " + getName();
  }

  protected void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Applying " + getName() + " ...");
    }
  }

  protected void reportRebuilding()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Rebuilding output automaton " + getName() + " ...");
    }
  }

  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private TRAutomatonProxy mOutputAutomaton;

}
