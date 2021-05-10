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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.*;

/**
 * Estimates probability of an automaton changing based
 * on event probabilities.
 */
class ChangeProbabilityEstimator implements ProbabilityEstimator
{
  public ChangeProbabilityEstimator(ProductDESSchema model)
  {
    mModel = model;
  }

  private double eventProb(int event, AutomatonSchema[] automata)
  {
    double sum = 0;

    for (AutomatonSchema a : automata)
      {
	if (!a.hasEvent(event))
	  continue;

	sum += countWithEnabled(event, a) / 
	  ((double)a.getStateCount());
      }

    return sum;
  }

  private double change(AutomatonSchema a, AutomatonSchema[] automata)
  {
    double val = 0;

    for (int i = 0; i < mModel.getEventCount(); i++)
      {
	val += eventProb(i, automata) * (nonSelfloopEventTrans(i, a) /
					 ((double)a.getStateCount()));
      }

    return val;
  }

  private int nonSelfloopEventTrans(int event, AutomatonSchema aut)
  {
    int count = 0;
    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);

	if (t.getEventId() == event && t.getSource() != t.getTarget())
	  count++;
      }

    return count;
  }

  private int countWithEnabled(int event, AutomatonSchema aut)
  {
    boolean[] enabledInState = new boolean[aut.getStateCount()];
    int enabledCount = 0;

    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);
	
	if (t.getEventId() == event && !enabledInState[t.getSource()])
	  {
	    enabledCount++;
	    enabledInState[t.getSource()] = true;
	  }
      }

    return enabledCount;
  }

  public AutomatonProbability estimate(AutomatonSchema aut, AutomatonSchema[] automata)
  { 
    return new  AutomatonProbability(aut, change(aut, automata));
  }

  private final ProductDESSchema mModel;
}
