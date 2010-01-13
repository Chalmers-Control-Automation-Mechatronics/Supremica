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
    if (sim.getAutomatonHistory().size() == 0)
    {
      this.add(new InitialState(sim.getCurrentStates()));
    }
    else
      this.add(new InitialState(sim.getAutomatonHistory().get(0)));
    for (int looper = 0; looper < sim.getEventHistory().size(); looper++)
    {
      final Step step = sim.getEventHistory().get(looper);
      final DefaultMutableTreeNode eventToAdd= new EventBranchNode(step.getEvent(), looper);
      this.add(eventToAdd);
      eventToAdd.add(new DefaultMutableTreeNode("Placeholder. You shouldn't ever see this"));
    }
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