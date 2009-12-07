package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

public class EventMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{


  // ################################################################
  // # Constructor

  public EventMutableTreeNode(final Simulation sim, final EventJTree parent)
  {
    super("Event", true);
    sim.attach(this);
    setupAllEvents(sim);
    mParent = parent;
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
    for (final EventProxy event : sim.getAllEvents())
    {
      final ArrayList<AutomatonProxy> automatonInEvent = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy automaton : sim.getAutomata())
        if (automaton.getEvents().contains(event))
          automatonInEvent.add(automaton);
      final DefaultMutableTreeNode eventToAdd= new EventBranchNode(event);
      this.add(eventToAdd);
      for (final AutomatonProxy automaton : automatonInEvent)
      {
        eventToAdd.add(new AutomatonLeafNode(automaton));
      }
    }
  }

  // ##################################################################
  // # Data Members

  private final EventJTree mParent;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;
}
