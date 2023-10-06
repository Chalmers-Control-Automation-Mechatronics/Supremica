//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


class EventTreeModel
  implements TreeModel, SimulationObserver
{

  //#########################################################################
  //# Constructor
  EventTreeModel(final Simulation sim,
                 final ArrayList<Pair<Boolean, Integer>> sortingEvents)
  {
    sim.attach(this);
    mSim = sim;
    mSortingEvents = sortingEvents;
    mSortedEvents = sim.getOrderedEvents();
    setupAllEvents(mSim, sortingEvents);
  }


  //#########################################################################
  //# Interface javax.swing.tree.TreeModel
  @Override
  public void addTreeModelListener(final TreeModelListener l)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<TreeModelListener>();
    }
    mListeners.add(l);
  }

  @Override
  public Object getChild(final Object parent, int index)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.get(index);
    } else if (parent instanceof EventProxy) {
      final EventProxy event = (EventProxy) parent;
      final List<AutomatonProxy> automata =
        mSim.getAutomataSensitiveToEvent(event);
      final SimulatorState state = mSim.getCurrentState();
      if (state.getNumberOfDisabledAutomata() == 0) {
        return automata.get(index);
      } else {
        for (final AutomatonProxy aut : automata) {
          if (state.getStatus(aut) != AutomatonStatus.DISABLED) {
            if (index == 0) {
              return aut;
            } else {
              index--;
            }
          }
        }
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount(final Object parent)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.size();
    } else if (parent instanceof EventProxy) {
      final EventProxy event = (EventProxy) parent;
      final List<AutomatonProxy> automata =
        mSim.getAutomataSensitiveToEvent(event);
      int count = automata.size();
      final SimulatorState state = mSim.getCurrentState();
      if (state.getNumberOfDisabledAutomata() > 0) {
        for (final AutomatonProxy aut : automata) {
          if (state.getStatus(aut) == AutomatonStatus.DISABLED) {
            count--;
          }
        }
      }
      return count;
    } else {
      return 0;
    }
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.indexOf(child);
    } else if (parent instanceof EventProxy) {
      final EventProxy event = (EventProxy) parent;
      final List<AutomatonProxy> automata =
        mSim.getAutomataSensitiveToEvent(event);
      final SimulatorState state = mSim.getCurrentState();
      int index = 0;
      for (final AutomatonProxy aut : automata) {
        if (state.getStatus(aut) != AutomatonStatus.DISABLED) {
          if (aut == child) {
            return index;
          } else {
            index++;
          }
        }
      }
      return -1;
    } else {
      return -1;
    }
  }

  @Override
  public Object getRoot()
  {
    return mSim;
  }

  @Override
  public boolean isLeaf(final Object node)
  {
    return node instanceof AutomatonProxy;
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l)
  {
    mListeners.remove(l);
    if (mListeners.isEmpty()) {
      mListeners = null;
    }
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue)
  {
    throw new UnsupportedOperationException
      ("SimulatorTreeModel does not support value change!");
  }


  //#########################################################################
  //# Interface SimulationObserver
  @Override
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (mListeners != null) {
      for (final TreeModelListener l : mListeners)
        for (final EventProxy selectedEvent : event.getSource().getOrderedEvents())
          l.treeNodesChanged(new TreeModelEvent(this, new Object[]{event.getSource(), selectedEvent}));
      setupAllEvents(mSim, mSortingEvents);
    }
  }


  //#########################################################################
  //# Sorting Methods
  private void setupAllEvents(final Simulation sim,
                              final List<Pair<Boolean,Integer>> sortingMethods)
  {
    final EventComparator comparator =
      new EventComparator(sortingMethods);
    Collections.sort(mSortedEvents, comparator);
  }


  //#########################################################################
  //# Inner Class EventComparator
  private class EventComparator implements Comparator<EventProxy>
  {

    //#######################################################################
    //# Constructor
    private EventComparator(final List<Pair<Boolean, Integer>> sortingMethods)
    {
      mSortingMethods = sortingMethods;
    }

    //#######################################################################
    //# Interface java.util.Comparator<EventProxy>
    @Override
    public int compare(final EventProxy a, final EventProxy b)
    {
      for (final Pair<Boolean,Integer> pair : mSortingMethods) {
        final int sortingMethod = pair.getSecond();
        final boolean isAscending = pair.getFirst();
        int compare;
        switch (sortingMethod) {
        case 0:
          compare = sortByType(a, b);
          break;
        case 1:
          compare = sortByName(a, b);
          break;
        case 2:
          compare = sortByEnabled(a, b);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported Sort Method!");
        }
        if (compare != 0) {
          return isAscending ? compare : -compare;
        }
      }
      return 0;
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Returns a negative number if a comes before b alphabetically,
     * positive number if b comes before a, and zero if they are equal.
     */
    private int sortByName(final EventProxy a, final EventProxy b)
    {
      return a.compareTo(b);
    }

    /**
     * Returns a negative number if a is before b in the default setting.
     * The default order is ENABLED, WARNING, DISABLED, BLOCKING
     */
    private int sortByEnabled(final EventProxy a, final EventProxy b)
    {
      if (a == b) {
        return 0;
      } else {
        final EventStatus status1 = mSim.getEventStatus(a);
        final EventStatus status2 = mSim.getEventStatus(b);
        return status2.compareTo(status1);
      }
    }

    /**
     * Returns a positive number if a is a controllable and b is not,
     * negative if b is controllable and a is not, and zero if they are
     * the same.
     */
     private int sortByType(final EventProxy a, final EventProxy b)
     {
       final EventKind kind1 = a.getKind();
       final EventKind kind2 = b.getKind();
       return kind2.compareTo(kind1);
     }

     //#######################################################################
     //# Data Members
     private final List<Pair<Boolean,Integer>> mSortingMethods;
  }


  //#########################################################################
  //# Data Members
  private final Simulation mSim;
  private LinkedList<TreeModelListener> mListeners;
  private final List<EventProxy> mSortedEvents;
  private final ArrayList<Pair<Boolean,Integer>> mSortingEvents;
}
