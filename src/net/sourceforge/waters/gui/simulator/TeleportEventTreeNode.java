package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class TeleportEventTreeNode extends DefaultMutableTreeNode
{
  public TeleportEventTreeNode(final int currentTime)
  {
    super("Manual State Set", true);
    mTime = currentTime;
  }

  public void addAutomata(final Simulation sim, final Map<AutomatonProxy, StateProxy> currentStates)
  {
    if (this.getChildAt(0).getClass() != AutomatonLeafNode.class)
    {
      this.removeAllChildren();
      final ArrayList<AutomatonProxy> automatonInEvent = new ArrayList<AutomatonProxy>();
      automatonInEvent.add(sim.getAutomatonActivityAtTime(mTime).get(0));
      final List<AutomatonProxy> allInvalid = sim.isNonControllableAtTime(mTime);
      for (final AutomatonProxy automaton : automatonInEvent)
      {
        if (currentStates == null)
          this.add(new AutomatonLeafNode(automaton, null, allInvalid.contains(automaton)));
        else
          this.add(new AutomatonLeafNode(automaton, currentStates.get(automaton), allInvalid.contains(automaton)));
      }
    }
  }

  public int getTime()
  {
    return mTime;
  }

  public String getData(final int indents)
  {
    String output = getIndents(indents) + this.toString();
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

  private final int mTime;

  private static final long serialVersionUID = 1581075011997555080L;
}
