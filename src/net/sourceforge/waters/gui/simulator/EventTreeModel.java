package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

public class EventTreeModel
  implements TreeModel, SimulationObserver
{
  // ##############################################################
  // # Constructor

  public EventTreeModel(final Simulation sim, final ArrayList<Pair<Boolean, Integer>> sortingEvents)
  {
    sim.attach(this);
    mSim = sim;
    mSortingEvents = sortingEvents;
    sortedEvents = sim.getAllEvents();
    setupAllEvents(mSim, sortingEvents);
  }

  // ##############################################################
  // # Interface TreeModel
  public void addTreeModelListener(final TreeModelListener l)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<TreeModelListener>();
    }
    mListeners.add(l);
  }

  public Object getChild(final Object parent, final int index)
  {
    if (Simulation.class.isInstance(parent))
    {
      return sortedEvents.get(index);
    }
    else if (EventProxy.class.isInstance(parent))
    {
      return mSim.getAutomataSensitiveToEvent((EventProxy)parent).get(index);
    }
    else
      return null;
  }

  public int getChildCount(final Object parent)
  {
    if (Simulation.class.isInstance(parent))
    {
      return sortedEvents.size();
    }
    else if (EventProxy.class.isInstance(parent))
    {
      System.out.println("Number of children:" + mSim.getAutomataSensitiveToEvent((EventProxy)parent).size());
      return mSim.getAutomataSensitiveToEvent((EventProxy)parent).size();
    }
    else
      return 0;
  }

  public int getIndexOfChild(final Object parent, final Object child)
  {
    if (Simulation.class.isInstance(parent))
    {
      return sortedEvents.indexOf(child);
    }
    else if (EventProxy.class.isInstance(parent))
    {
      return mSim.getAutomataSensitiveToEvent((EventProxy)parent).indexOf(child);
    }
    else
      return -1;
  }

  public Object getRoot()
  {
    return mSim;
  }

  public boolean isLeaf(final Object node)
  {
    return (AutomatonProxy.class.isInstance(node));
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

  // ###############################################################
  // # Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (mListeners != null)
    {
      for (final TreeModelListener l : mListeners)
        for (final EventProxy selectedEvent : event.getSource().getAllEvents())
          l.treeNodesChanged(new TreeModelEvent(this, new Object[]{event.getSource(), selectedEvent}));
      setupAllEvents(mSim, mSortingEvents);
    }
  }

  // #####################################################################
  // # Sorting Methods

  private void setupAllEvents(final Simulation sim, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    //final long time = System.currentTimeMillis();
    final EventJTreeComparitor comparitor = new EventJTreeComparitor(sim, sortingMethods);
    Collections.sort(sortedEvents, comparitor);
  }


  // #################################################################
  // # Data Members
  private final Simulation mSim;
  private LinkedList<TreeModelListener> mListeners;
  private final List<EventProxy> sortedEvents;
  private final ArrayList<Pair<Boolean,Integer>> mSortingEvents;
}
