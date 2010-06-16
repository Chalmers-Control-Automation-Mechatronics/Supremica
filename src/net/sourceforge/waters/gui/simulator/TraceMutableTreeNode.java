package net.sourceforge.waters.gui.simulator;

import javax.swing.tree.DefaultMutableTreeNode;

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
  }

  // #################################################################
  // # Simple Access
  /*
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
  */


  //#########################################################################
  //# Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    setupAllEvents(event.getSource());
    mParent.forceRecalculation();
    mSim.detach(this);
  }

  //#########################################################################
  //# Auxiliary Methods
  private void setupAllEvents(final Simulation sim)
  {
    removeAllChildren();
    for (int time = 0; time < sim.getHistorySize(); time++) {
      final SimulatorState state = sim.getHistoryState(time);
      final TraceStepTreeNode node =
        TraceStepTreeNode.createTraceStepNode(state, time);
      add(node);
    }
  }


  //#########################################################################
  //# Data Members
  private final TraceJTree mParent;
  private final Simulation mSim;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4899696734198560636L;

}