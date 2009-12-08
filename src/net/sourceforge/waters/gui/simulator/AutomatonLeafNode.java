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