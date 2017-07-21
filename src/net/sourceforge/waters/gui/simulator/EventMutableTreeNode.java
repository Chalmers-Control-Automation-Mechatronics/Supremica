//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class EventMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{
  // ################################################################
  // # Constructor

  public EventMutableTreeNode(final Simulation sim, final EventJTree parent, final List<Pair<Boolean, Integer>> sortingMethods,
      final ArrayList<String> expandedNodes)
  {
    super("Event", true);
    sim.attach(this);
    mSim = sim;
    setupAllEvents(sim, sortingMethods, expandedNodes);
  }


  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    mSim.detach(this);
    // This mutable tree node has now become invalidated
  }


  //#########################################################################
  //# Auxiliary Functions
  private void setupAllEvents(final Simulation sim, final List<Pair<Boolean, Integer>> sortingMethods,
      final ArrayList<String> expandedNodes)
  {
    final ArrayList<Integer> sortedIndexes = sortArrayList(sim.getOrderedEvents(), sortingMethods);
    final int time = mSim.getCurrentTime();
    for (final Integer index : sortedIndexes)
    {
      final EventProxy event = sim.getOrderedEvents().get(index);
      final DefaultMutableTreeNode eventToAdd =
        TraceStepTreeNode.createEventStepNode(event, time);
      this.add(eventToAdd);
    }
  }

  private ArrayList<Integer> sortArrayList (final List<EventProxy> raw, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    final ArrayList<Integer> output = new ArrayList<Integer>();
    final ArrayList<EventProxy> temp = new ArrayList<EventProxy>();
    for (int looper = 0; looper < raw.size(); looper++)
    {
      final int index = findIndex(temp, raw.get(looper), sortingMethods);
      output.add(index, looper);
      temp.add(index, raw.get(looper));
    }
    return output;
  }

  private int findIndex(final ArrayList<EventProxy> sorted, final EventProxy toAdd, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    for (int looper = 0; looper < sorted.size(); looper++)
    {
      if (!isLowerThan(toAdd, sorted.get(looper), sortingMethods))
        return looper;
    }
    return sorted.size();
  }

  /**
   * Returns TRUE if a is lower in the tree than b
   */
  private boolean isLowerThan(final EventProxy a, final EventProxy b, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    if (sortingMethods.size() == 0)
    {
      return true;
    }
    final int sortingMethod = sortingMethods.get(0).getSecond();
    final boolean isAscending = sortingMethods.get(0).getFirst();
    int compare;
    switch (sortingMethod)
    {
    case 0:
      compare = sortByType(a, b);
      break;
    case 1:
      compare = sortByName(a, b);
      break;
    case 2:
      compare = sortByStatus(a, b);
      break;
    default:
      throw new UnsupportedOperationException("Unsupported Sort Method");
    }
    if ((compare < 0 && isAscending) || (compare > 0 && !isAscending))
      return true;
    if ((compare < 0 && !isAscending || compare > 0 && isAscending))
      return false;
    return isLowerThan(a, b, sortingMethods.subList(1, sortingMethods.size()));
  }

  /**
   * Returns a POSITIVE NUMBER if a comes before b alphabetically, NEGATIVE number if b comes before a, and ZERO if they are equal
   */
  private int sortByName(final EventProxy a, final EventProxy b)
  {
    return -a.getName().compareTo(b.getName());
  }

  /**
   * Returns a POSITIVE NUMBER if a is enabled and b isn't, NEGATIVE if b is enabled and a isn't, and ZERO if they are the same
   */
  private int sortByStatus(final EventProxy a, final EventProxy b)
  {
    final EventStatus status1 = mSim.getEventStatus(a);
    final EventStatus status2 = mSim.getEventStatus(b);
    return status1.compareTo(status2);
  }

  /**
   * Returns a POSITIVE NUMBER if a is a controllable and b isn't, NEGATIVE if b is controllable and a isn't, and ZERO if they are the same
   */
   private int sortByType(final EventProxy a, final EventProxy b)
   {
     final EventKind kind1 = a.getKind();
     final EventKind kind2 = b.getKind();
     return kind1.compareTo(kind2);
   }


  // ##################################################################
  // # Data Members

  private final Simulation mSim;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;

}
