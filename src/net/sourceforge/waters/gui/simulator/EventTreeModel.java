//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   EventTreeModel
//###########################################################################
//# $Id$
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

import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class EventTreeModel
  implements TreeModel, SimulationObserver
{

  //#########################################################################
  //# Constructor
  public EventTreeModel(final Simulation sim,
                        final ArrayList<Pair<Boolean, Integer>> sortingEvents)
  {
    sim.attach(this);
    mSim = sim;
    mSortingEvents = sortingEvents;
    mSortedEvents = sim.getAllEvents();
    setupAllEvents(mSim, sortingEvents);
  }


  //#########################################################################
  //# Interface javax.swing.tree.TreeModel
  public void addTreeModelListener(final TreeModelListener l)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<TreeModelListener>();
    }
    mListeners.add(l);
  }

  public Object getChild(final Object parent, final int index)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.get(index);
    } else if (parent instanceof EventProxy) {
      return mSim.getAutomataSensitiveToEvent((EventProxy)parent).get(index);
    } else {
      return null;
    }
  }

  public int getChildCount(final Object parent)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.size();
    } else if (parent instanceof EventProxy) {
      final EventProxy event = (EventProxy) parent;
      final List<AutomatonProxy> automata =
        mSim.getAutomataSensitiveToEvent(event);
      return automata.size();
    } else {
      return 0;
    }
  }

  public int getIndexOfChild(final Object parent, final Object child)
  {
    if (parent instanceof Simulation) {
      return mSortedEvents.indexOf(child);
    } else if (parent instanceof EventProxy) {
      return mSim.getAutomataSensitiveToEvent((EventProxy)parent).indexOf(child);
    } else {
      return -1;
    }
  }

  public Object getRoot()
  {
    return mSim;
  }

  public boolean isLeaf(final Object node)
  {
    return node instanceof AutomatonProxy;
  }

  public void removeTreeModelListener(final TreeModelListener l)
  {
    mListeners.remove(l);
    if (mListeners.isEmpty()) {
      mListeners = null;
    }
  }

  public void valueForPathChanged(final TreePath path, final Object newValue)
  {
    throw new UnsupportedOperationException
      ("SimulatorTreeModel does not support value change!");
  }


  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (mListeners != null) {
      for (final TreeModelListener l : mListeners)
        for (final EventProxy selectedEvent : event.getSource().getAllEvents())
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
          return isAscending ? -compare : compare;
        }
      }
      return 0;
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Returns a POSITIVE NUMBER if a comes before b alphabetically,
     * NEGATIVE number if b comes before a, and ZERO if they are equal.
     */
    private int sortByName(final EventProxy a, final EventProxy b)
    {
      return -a.getName().compareTo(b.getName());
    }

    /**
     * Returns a POSITIVE NUMBER if a is before b in the default setting.
     * The default order is ENABLED, WARNING, DISABLED, BLOCKING
     */
    private int sortByEnabled(final EventProxy a, final EventProxy b)
    {
      boolean aIsWarning = false;
      boolean bIsWarning = false;
      for (final Step step : mSim.getWarningProperties().keySet())
      {
        if (step.getEvent() == a)
          aIsWarning = true;

        if (step.getEvent() == b)
          bIsWarning = true;
      }
      final boolean aIsEnabled = mSim.getActiveEvents().contains(a) && !aIsWarning;
      final boolean bIsEnabled = mSim.getActiveEvents().contains(b) && !bIsWarning;
      final boolean aIsBlocking = mSim.getNonControllable(a).size() != 0;
      final boolean bIsBlocking = mSim.getNonControllable(b).size() != 0;
      final boolean aIsDisabled = !aIsEnabled && !aIsBlocking && !aIsWarning;
      final boolean bIsDisabled = !bIsEnabled && !bIsBlocking && !bIsWarning;
      if (aIsEnabled)
      {
        if (bIsEnabled)
          return 0;
        else if (bIsWarning || bIsDisabled || bIsBlocking)
          return 1;
      }
      else if (aIsWarning)
      {
        if (bIsEnabled)
          return -1;
        else if (bIsWarning)
          return 0;
        else if (bIsDisabled || bIsBlocking)
          return 1;
      }
      else if (aIsDisabled)
      {
        if (bIsEnabled || bIsWarning)
          return -1;
        else if (bIsDisabled)
          return 0;
        else if (bIsBlocking)
          return 1;
      }
      else if (aIsBlocking)
      {
        if (bIsEnabled || bIsWarning || bIsDisabled)
          return -1;
        else if (bIsBlocking)
          return 0;
      }
      throw new IllegalArgumentException("Either a or b is not blocking, warning, disabled, or enabled!");
    }

    /**
     * Returns a POSITIVE NUMBER if a is a controllable and b isn't,
     * NEGATIVE if b is controllable and a isn't, and ZERO if they are the same
     */
     private int sortByType(final EventProxy a, final EventProxy b)
     {
       if (a.getKind() == EventKind.CONTROLLABLE)
       {
         if (b.getKind() == EventKind.CONTROLLABLE)
           return 0;
         else
           return 1;
       }
       else
       {
         if (b.getKind() == EventKind.CONTROLLABLE)
           return -1;
         else
           return 0;
       }
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
