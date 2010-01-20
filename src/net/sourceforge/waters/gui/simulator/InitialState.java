package net.sourceforge.waters.gui.simulator;

import java.util.List;
import java.util.Map;

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
  }

  public void expand(final Simulation sim)
  {
    if (this.getChildAt(0).getClass() != AutomatonLeafNode.class)
    {
      final List<AutomatonProxy> isBlocking = sim.isNonControllableAtTime(-1);
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
