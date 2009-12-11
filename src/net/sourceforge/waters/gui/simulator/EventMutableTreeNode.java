package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

public class EventMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{
  // ################################################################
  // # Constructor

  public EventMutableTreeNode(final Simulation sim, final EventJTree parent, final int sortByName, final int sortByEnabled)
  {
    super("Event", true);
    sim.attach(this);
    mParent = parent;
    mSim = sim;
    mSortByName = sortByName;
    mSortByEnabled = sortByEnabled;
    setupAllEvents(sim, sortByName, sortByEnabled);
  }

  // #################################################################
  // # Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    setupAllEvents(event.getSource(), mSortByName, mSortByEnabled);
    mParent.repaint();
  }



  // ##################################################################
  // # Auxillary Functions

  private void setupAllEvents(final Simulation sim, final int sortByName, final int sortByOrder)
  {
    this.removeAllChildren();
    final ArrayList<Integer> sortedIndexes = sortArrayList(sim.getAllEvents(), sortByName, sortByOrder);
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
    }
    mParent.expandPath(new TreePath(this));
  }


  private ArrayList<Integer> sortArrayList (final List<EventProxy> raw, final int sortByName, final int sortByEnabled)
  {
    final ArrayList<Integer> output = new ArrayList<Integer>();
    final ArrayList<EventProxy> temp = new ArrayList<EventProxy>();
    for (int looper = 0; looper < raw.size(); looper++)
    {
      final int index = findIndex(temp, raw.get(looper), sortByName, sortByEnabled);
      output.add(index, looper);
      temp.add(index, raw.get(looper));
    }
    return output;
  }
  private int findIndex(final ArrayList<EventProxy> sorted, final EventProxy toAdd, final int sortByName, final int sortByEnabled)
  {
    if (Math.abs(sortByName) < Math.abs(sortByEnabled) && sortByEnabled >= 0 && !mSim.getValidTransitions().contains(toAdd))
      return sorted.size();
    else if (Math.abs(sortByName) < Math.abs(sortByEnabled) && sortByEnabled < 0 && mSim.getValidTransitions().contains(toAdd))
      return sorted.size();
    for (int looper = 0; looper < sorted.size(); looper++)
    {
      if (!isLowerThan(toAdd, sorted.get(looper), sortByName, sortByEnabled))
        return looper;
    }
    return sorted.size();
  }
  /**
   * Returns TRUE if a is greater than b
   * @param a
   * @param b
   * @param sortByName Defines how the definition of 'lower' involves the name. If it is zero, then it is not used for the definition
   * If it is greater than zero, then it sorts using alphabetical order. If it is less than zero, then it sorts using reverse alphabetical
   * order. If the magnitude of this value is greater than or equal to the magnitude of sortByEnabled, this sorting method takes presedence
   * Otherwise, it only matters if the two events are both enabled or both disabled.
   * @param sortByEnabled Defines how the definition of 'lower' involves whether this event is enabled or not. If it is zero, then it is
   * not used for the definition. If it is greater than zero, then it sorts it, with the enabled events first. If it is less than zero, then
   * it sorts it with the disabled events first. If the magnitude of this value is strictly greater than the magnitude of sortByName, this
   * sorting method takes presedence. Otherwise, it only matters if the two events have the same name.
   * @return
   */
  private boolean isLowerThan(final EventProxy a, final EventProxy b, final int sortByName, final int sortByEnabled)
  {
    if (Math.abs(sortByName) >= Math.abs(sortByEnabled))
    {
      int i = sortByName(a, b);
      if (i < 0)
        return sortByName >= 0;
      else if (i > 0)
        return sortByName < 0;
      else
      {
        i = sortByEnabled(a, b);
        if (i > 0)
          return sortByEnabled >= 0;
        else
          return sortByEnabled < 0;
      }
    }
    else
    {
      int i = sortByEnabled(a, b);
      if (i > 0)
        return sortByEnabled < 0;
      else if (i < 0)
        return sortByEnabled >= 0;
      else
      {
        i = sortByName(a, b);
        if (i < 0)
          return sortByName >= 0;
        else
          return sortByName < 0;
      }
    }
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

  // ##################################################################
  // # Data Members

  private final EventJTree mParent;
  private final Simulation mSim;
  private final int mSortByName;
  private final int mSortByEnabled;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;

}
