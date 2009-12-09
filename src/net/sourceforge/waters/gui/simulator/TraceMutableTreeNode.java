package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

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
    /*for (int looper = sim.getEventHistory().size() - 1; looper >= 0; looper--)
    {
      final DefaultMutableTreeNode eventToAdd = new EventBranchNode(sim.getEventHistory().get(looper));
      HashMap<AutomatonProxy, StateProxy> stateInEvent = new HashMap<AutomatonProxy, StateProxy>();
      stateInEvent = sim.getAutomatonHistory().get(looper);
      this.add(eventToAdd);
      for (final AutomatonProxy automaton : stateInEvent.keySet())
      {
        eventToAdd.add(new AutomatonLeafNode(automaton, stateInEvent.get(automaton)));
      }
    }*/
    for (int looper = sim.getEventHistory().size() - 1; looper >= 0; looper--)
    {
      //System.out.println("DEBUG: Size = " + sim.getEventHistory().size());
      //System.out.println("DEBUG: Data = " + sim.getEventHistory());
      final EventProxy event = sim.getEventHistory().get(looper);
      final DefaultMutableTreeNode eventToAdd= new EventBranchNode(event);
      final ArrayList<AutomatonProxy> automatonInEvent = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy automaton : sim.getAutomata())
        if (automaton.getEvents().contains(event))
          automatonInEvent.add(automaton);
      this.add(eventToAdd);
      for (final AutomatonProxy automaton : automatonInEvent)
      {
        eventToAdd.add(new AutomatonLeafNode(automaton, sim.getCurrentStates().get(automaton)));
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