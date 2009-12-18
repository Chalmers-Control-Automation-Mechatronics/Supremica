package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.LinkedList;

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

  public EventTreeModel(final Simulation sim)
  {
    sim.attach(this);
    mSim = sim;
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
      return ((Simulation)parent).getAllEvents().get(index);
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
      return ((Simulation)parent).getAllEvents().size();
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
      return ((Simulation)parent).getAllEvents().indexOf(child);
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

  // #################################################################
  // # Data Members
  private final Simulation mSim;
  private LinkedList<TreeModelListener> mListeners;

}
