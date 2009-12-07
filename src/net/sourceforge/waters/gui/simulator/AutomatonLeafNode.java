package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonLeafNode extends DefaultMutableTreeNode
{public AutomatonLeafNode(final AutomatonProxy event)
  {
    super(event.getName(), false);
    mAutomata = event;
  }

  public AutomatonProxy getAutomata()
  {
    return mAutomata;
  }

  private final AutomatonProxy mAutomata;

  static ImageIcon unblockingPlantIcon;
  static ImageIcon blockingPlantIcon;
  static ImageIcon unblockingSpecIcon;
  static ImageIcon blockingSpecIcon;
  static ImageIcon unblockingVarIcon;
  static ImageIcon blockingVarIcon;

  private static final long serialVersionUID = 4785226183311677790L;
}