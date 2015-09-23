//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

class IndependentEventsSelector implements AutomataSelector
{
  public IndependentEventsSelector(ProductDESSchema model, ProbabilityEstimator estimator)
  {
    mModel = model;
    mEstimator = estimator;
  }

  public AutomatonSchema[] select(AutomatonSchema[] automata)
  {
    AutomatonProbability[] probs = new AutomatonProbability[automata.length];
    for (int i = 0; i < automata.length; i++)
      {
	probs[i] = mEstimator.estimate(automata[i], automata);
      }

    Arrays.sort(probs, new LowestProbabilityFirst());

    //Events that have already been used. If an automaton shares any
    //events with this set, then the automaton should not be chosen.
    Set<Integer> usedEvents = new TreeSet<Integer>();

    //Take the sorted list of probabilities as a list and start adding
    //events to the used event set. If an event has been used, the 
    //automaton should be discarded from the list.
    List<AutomatonProbability> aplist = 
      new ArrayList<AutomatonProbability>(Arrays.asList(probs));

    Iterator<AutomatonProbability> it = aplist.iterator();
  iterating: while (it.hasNext())
      {
	AutomatonProbability ap = it.next();
	AutomatonSchema aut = ap.getAutomaton();
	
	//Are any of the automaton's events in the used 
	//set?
	for (int i = 0; i < aut.getEventIdCount(); i++)
	  {
	    if (usedEvents.contains(aut.getEventId(i)))
	      {
		//Not a good candidate.
		it.remove();
		continue iterating;
	      }
	  }

	//All good, add the events to the used set
	for (int i = 0; i < aut.getEventIdCount(); i++)
	  {
	    usedEvents.add(aut.getEventId(i));
	  }
      }

    //Return whatever is left over.
    AutomatonSchema[] result = new AutomatonSchema[aplist.size()];
    for (int i = 0; i < aplist.size(); i++)
      {
	result[i] = aplist.get(i).getAutomaton();
      }

    return result;
  }

  private final ProbabilityEstimator mEstimator;
  @SuppressWarnings("unused")
  private final ProductDESSchema mModel;
}
