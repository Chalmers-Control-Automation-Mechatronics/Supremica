//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;

import org.apache.logging.log4j.Logger;


/**
 * An abstraction step representing a trivial or nonblocking automaton
 * that was dropped during compositional verification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepDrop
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepDrop(final TRAbstractionStep pred,
                        final TRAutomatonProxy aut)
  {
    super(pred.getName());
    mPredecessor = pred;
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    int init = -1;
    for (int s = 0; s < numStates; s++) {
      if (rel.isInitial(s)) {
        init = s;
        break;
      }
    }
    assert init >= 0;
    mInitialState = init;
  }

  TRAbstractionStepDrop(final TRAbstractionStep pred,
                        final int initialState)
  {
    super(pred.getName());
    mPredecessor = pred;
    mInitialState = initialState;
  }


  //#########################################################################
  //# Simple Access
  public int getInitialState()
  {
    return mInitialState;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.singletonList(mPredecessor);
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws OverflowException
  {
    final EventEncoding enc = new EventEncoding();
    final int numStates = mInitialState == 0 ? 2 : mInitialState + 1;
    final int dumpIndex = mInitialState == 0 ? 1 : 0;
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(":dummy",
                                       ComponentKind.PLANT,
                                       enc, numStates, dumpIndex,
                                       preferredConfig);
    for (int s = 0; s < numStates; s++) {
      final boolean init = s == mInitialState;
      rel.setReachable(s, init);
      rel.setInitial(s, init);
    }
    return new TRAutomatonProxy(enc, rel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalModelAnalyzer analyzer)
  {
    final int numSteps = trace.getNumberOfSteps();
    final int[] states = new int[numSteps];
    Arrays.fill(states, mInitialState);
    trace.addAutomaton(mPredecessor, states);
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Introducing dropped automaton " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final int mInitialState;

}
