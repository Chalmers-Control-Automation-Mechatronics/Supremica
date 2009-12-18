package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class TraceTreeModel
  implements TreeModel, SimulationObserver
{


  // ##############################################################
  // # Constructor

  public TraceTreeModel(final Simulation sim)
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
      return ((Simulation)parent).getEventHistory().get(index);
    }
    else if (EventProxy.class.isInstance(parent))
    {
      final ArrayList<StateProxy> auto = new ArrayList<StateProxy>();
      final int time = mSim.getEventHistory().indexOf(parent);
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(mSim.getAutomatonHistory().get(time).get(search));
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
      return ((Simulation)parent).getEventHistory().size();
    }
    else if (EventProxy.class.isInstance(parent))
    {
      final ArrayList<StateProxy> auto = new ArrayList<StateProxy>();
      final int time = mSim.getEventHistory().indexOf(parent);
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(mSim.getAutomatonHistory().get(time).get(search));
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
      final ArrayList<StateProxy> auto = new ArrayList<StateProxy>();
      final int time = mSim.getEventHistory().indexOf(parent);
      for (final AutomatonProxy search : mSim.getAutomata())
      {
        if (search.getEvents().contains((EventProxy)parent))
        {
          auto.add(mSim.getAutomatonHistory().get(time).get(search));
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
    return (StateProxy.class.isInstance(node));
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
