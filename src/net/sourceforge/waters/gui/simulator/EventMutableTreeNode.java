package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import net.sourceforge.waters.model.des.AutomatonProxy;
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
    mParent = parent;
    mSim = sim;
    setupAllEvents(sim, sortingMethods, expandedNodes);
  }

  // #################################################################
  // # Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    mSim.detach(this);
    mParent.forceRecalculation();
  }

  // ##################################################################
  // # Auxillary Functions

  private void setupAllEvents(final Simulation sim, final List<Pair<Boolean, Integer>> sortingMethods,
      final ArrayList<String> expandedNodes)
  {
    //this.removeAllChildren();
    //mParent.expandPath(new TreePath(this));
    final ArrayList<Integer> sortedIndexes = sortArrayList(sim.getAllEvents(), sortingMethods);
    for (final Integer index : sortedIndexes)
    {
      final EventProxy event = sim.getAllEvents().get(index);
      final ArrayList<AutomatonProxy> automatonInEvent = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy automaton : sim.getAutomata())
        if (automaton.getEvents().contains(event))
          automatonInEvent.add(automaton);
      final DefaultMutableTreeNode eventToAdd= new EventBranchNode(event, sim.getCurrentTime());
      this.add(eventToAdd);
      for (final AutomatonProxy automaton : automatonInEvent)
      {
        eventToAdd.add(new AutomatonLeafNode(automaton, null));
      }
      for (int looper = 0; looper < expandedNodes.size(); looper++)
      {
        final String name = expandedNodes.get(looper);
        if (event.getName().compareTo(name) == 0)
        {
          //mParent.expandPath(new TreePath(eventToAdd.getPath()));
        }
      }
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


  // ##################################################################
  // # Data Members

  private final EventJTree mParent;
  private final Simulation mSim;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;

}
