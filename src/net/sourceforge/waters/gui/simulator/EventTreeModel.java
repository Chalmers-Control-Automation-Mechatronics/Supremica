package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class EventTreeModel
  implements TreeModel, SimulationObserver
{


  // ##############################################################
  // # Constructor

  public EventTreeModel(final Simulation sim, final ArrayList<Pair<Boolean, Integer>> sortingEvents)
  {
    sim.attach(this);
    mSim = sim;
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
      final ArrayList<AutomatonProxy> auto = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(search);
        }
      }
      return auto.get(index);
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
      final ArrayList<AutomatonProxy> auto = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(search);
        }
      }
      return auto.size();
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
      final ArrayList<AutomatonProxy> auto = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(search);
        }
      }
      return auto.indexOf(child);
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
    // Do nothing... yet.
  }

  // #####################################################################
  // # Sorting Methods

  private void setupAllEvents(final Simulation sim, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    final ArrayList<Integer> sortedIndexes = sortArrayList(sim.getAllEvents(), sortingMethods);
    final ArrayList<EventProxy> output = new ArrayList<EventProxy>();
    for (final Integer index : sortedIndexes)
    {
      final EventProxy event = sim.getAllEvents().get(index);
      output.add(event);
    }
    sortedEvents = output;
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
      compare = sortByEnabled(a, b);
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
   * @param a
   * @param b
   * @return
   */
  public int sortByName(final EventProxy a, final EventProxy b)
  {
    return -a.getName().compareTo(b.getName());
  }

  /**
   * Returns a POSITIVE NUMBER if a is enabled and b isn't, NEGATIVE if b is enabled and a isn't, and ZERO if they are the same
   * @param a
   * @param b
   * @return
   */
  public int sortByEnabled(final EventProxy a, final EventProxy b)
  {
    if (mSim.getValidTransitions().contains(a))
    {
      if (mSim.getValidTransitions().contains(b))
        return 0;
      else
        return 1;
    }
    else
    {
      if (mSim.getValidTransitions().contains(b))
        return -1;
      else
        return 0;
    }
  }

  /**
   * Returns a POSITIVE NUMBER if a is a controllable and b isn't, NEGATIVE if b is controllable and a isn't, and ZERO if they are the same
   * @param a
   * @param b
   * @return
   */
    public int sortByType(final EventProxy a, final EventProxy b)
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


  // #################################################################
  // # Data Members
  private final Simulation mSim;
  private LinkedList<TreeModelListener> mListeners;
  private ArrayList<EventProxy> sortedEvents;

}
