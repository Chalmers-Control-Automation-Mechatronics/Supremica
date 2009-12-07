package net.sourceforge.waters.gui.simulator;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;


public class EventTreeModel extends DefaultTreeModel
{
  public EventTreeModel(final TreeNode root, final Simulation sim)
  {
    super(root, false);
  }

  private static final long serialVersionUID = -3232920524730761620L;
}
