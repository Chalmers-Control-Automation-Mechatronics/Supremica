package net.sourceforge.waters.gui.simulator;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class TraceMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{




  // ################################################################
  // # Constructor
  public TraceMutableTreeNode(final Simulation sim, final TraceJTree parent)
  {
    super("Trace", true);
    sim.attach(this);
    mSim = sim;
    mParent = parent;
    setupAllEvents(sim);
    if (mStartTime == 0) mStartTime = System.currentTimeMillis();
  }

  // #################################################################
  // # Simple Access

  public String getData(final int indents)
  {
    String output = getIndents(indents) + this.toString() + "[" + (System.currentTimeMillis() - mStartTime) + "]";
    for (int childLoop = 0; childLoop < this.getChildCount(); childLoop++)
    {
      if (this.getChildAt(childLoop).getClass() == EventBranchNode.class)
      {
        final EventBranchNode node = (EventBranchNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
      else if (this.getChildAt(childLoop).getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode node = (AutomatonLeafNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
    }
    return output;
  }
  private String getIndents(final int indents)
  {
    String output = "";
    for (int looper = 0; looper < indents; looper++)
      output += "-";
    return output;
  }


  // #################################################################
  // # Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    setupAllEvents(event.getSource());
    mParent.forceRecalculation();
    mSim.detach(this);
  }

  // ##################################################################
  // # Auxillary Functions

  private void setupAllEvents(final Simulation sim)
  {
    this.removeAllChildren();
    for (int looper = sim.getEventHistory().size() - 1; looper >= 0; looper--)
    {
      final EventProxy event = sim.getEventHistory().get(looper);
      final DefaultMutableTreeNode eventToAdd= new EventBranchNode(event, looper);
      this.add(eventToAdd);
      final HashMap<AutomatonProxy, StateProxy> stateInEvent = sim.getAutomatonHistory().get(looper);
      this.add(eventToAdd);
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
  static private long mStartTime = 0;
  private final Simulation mSim;

  // ##################################################################
  // # Class Constants

  private static final long serialVersionUID = 4899696734198560636L;
}