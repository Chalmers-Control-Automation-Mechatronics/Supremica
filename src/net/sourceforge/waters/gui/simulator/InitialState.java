package net.sourceforge.waters.gui.simulator;

import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class InitialState extends DefaultMutableTreeNode
{

  public InitialState(final Map<AutomatonProxy,StateProxy> hashMap)
  {
    super("Initial State", true);
    toExpand = hashMap;
    this.add(new DefaultMutableTreeNode("You shouldn't ever see this", false));
    // To ensure that the events menu can be expanded. This is removed as soon as the menu is expanded however.
    // It appears as a grey box on the TraceJTree however.
  }

  public void expand(final Simulation sim)
  {
    if (this.getChildAt(0).getClass() != AutomatonLeafNode.class)
    {
      final Set<AutomatonProxy> isBlocking = sim.isNonControllableAtTime(-1);
      this.removeAllChildren();
      for (final AutomatonProxy automaton : toExpand.keySet())
      {
        this.add(new AutomatonLeafNode(automaton, toExpand.get(automaton), isBlocking.contains(automaton)));
      }
    }
  }

  private final Map<AutomatonProxy,StateProxy> toExpand;

  private static final long serialVersionUID = -8469400574115123118L;
}
