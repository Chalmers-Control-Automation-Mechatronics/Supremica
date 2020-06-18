//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.Comparator;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;


/**
 * A tuple containing an automaton and its estimated probability of
 * changing. Actually, it is currently a misnomer. It doesn't
 * calculate a probability. The values across a number of automata
 * could be normalised into a probability distribution.
 * @author Sam Douglas
 */
class AutomatonProbability
{
  public AutomatonProbability(AutomatonSchema automaton, double prob)
  {
    mAutomaton = automaton;
    mProbability = prob;
  }

  /**
   * Gets the automaton associated with this probability
   * estimate.
   */ 
  public AutomatonSchema getAutomaton()
  {
    return mAutomaton;
  }

  /**
   * Gets an estimate of the change probability.
   */
  public double getProbability()
  {
    return mProbability;
  }    

  public String toString()
  {
    return "(" + mAutomaton.getName() + ": " + mProbability + ")";
  }

  private final AutomatonSchema mAutomaton;
  private final double mProbability;
}


/**
 * Gives an ordering of AutomatonProbability tuples with
 * lowest probability first.
 */
class LowestProbabilityFirst 
  implements Comparator<AutomatonProbability>
{
  public int compare(AutomatonProbability a1, AutomatonProbability a2)
  {
    Double m1prob = new Double(a1.getProbability());
    Double m2prob = new Double(a2.getProbability());
    return m1prob.compareTo(m2prob);
  }
}


/**
 * A kludgy comparator that sorts by <code>probability^2 / |Qn|</code>
 */
class ProbabilityOverStatesSquared 
  implements Comparator<AutomatonProbability>
{ 
  public int compare(AutomatonProbability m1, AutomatonProbability m2)
  {
    Double m1prob = new Double(m1.getProbability() * Math.pow(m1.getAutomaton().getStateCount(), 2));
    Double m2prob = new Double(m2.getProbability() * Math.pow(m2.getAutomaton().getStateCount(), 2));
    return m1prob.compareTo(m2prob);
  }
}
