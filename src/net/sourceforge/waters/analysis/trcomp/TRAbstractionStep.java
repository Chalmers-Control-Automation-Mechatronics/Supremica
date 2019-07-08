//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
                            AbstractTRCompositionalModelAnalyzer analyzer)
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
    return LogManager.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private TRAutomatonProxy mOutputAutomaton;

}
