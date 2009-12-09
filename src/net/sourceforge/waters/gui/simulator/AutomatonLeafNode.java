package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class AutomatonLeafNode extends DefaultMutableTreeNode
{

  public AutomatonLeafNode(final AutomatonProxy automata, final StateProxy overloadedState)
  {
    super(automata.getName(), false);
    mAutomata = automata;
    mState = overloadedState;

  }

  public AutomatonProxy getAutomata()
  {
    return mAutomata;
  }
  public StateProxy getOverloadedState()
  {
    return mState;
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

  private final AutomatonProxy mAutomata;
  private final StateProxy mState;

  static ImageIcon unblockingPlantIcon;
  static ImageIcon blockingPlantIcon;
  static ImageIcon unblockingSpecIcon;
  static ImageIcon blockingSpecIcon;
  static ImageIcon unblockingVarIcon;
  static ImageIcon blockingVarIcon;

  private static final long serialVersionUID = 4785226183311677790L;

}