package net.sourceforge.waters.gui.simulator;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class TraceMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{
  // ################################################################
  // # Constructor
  public TraceMutableTreeNode(final Simulation sim, final TraceJTree parent)
  {
    super("Trace", true);
    sim.attach(this);
    mParent = parent;
    setupAllEvents(sim);
  }

  // #################################################################
  // # Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    setupAllEvents(event.getSource());
    mParent.repaint();
  }

  // ##################################################################
  // # Auxillary Functions

  private void setupAllEvents(final Simulation sim)
  {
    this.removeAllChildren();
    for (int looper = 0; looper < sim.getEventHistory().size(); looper++)
    {
      System.out.println("DEBUG: Looped " + looper + " times, out of " + sim.getEventHistory().size() + " total times.");
      final EventBranchNode eventToAdd = new EventBranchNode(sim.getEventHistory().get(looper));
      HashMap<AutomatonProxy, StateProxy> stateInEvent = new HashMap<AutomatonProxy, StateProxy>();
      stateInEvent = sim.getAutomatonHistory().get(looper);
      this.add(eventToAdd);
      System.out.println("DEBUG: Added event: " + eventToAdd);
      for (final AutomatonProxy automaton : stateInEvent.keySet())
      {
        eventToAdd.add(new AutomatonLeafNode(automaton, stateInEvent.get(automaton)));
      }
    }
    mParent.expandPath(new TreePath(this));
  }

  // ##################################################################
  // # Data Members

  private final TraceJTree mParent;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;

}