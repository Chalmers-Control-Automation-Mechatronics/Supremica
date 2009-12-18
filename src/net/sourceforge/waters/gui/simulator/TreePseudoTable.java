package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;

public class TreePseudoTable extends MouseAdapter
{
  public TreePseudoTable(final EventJTree tree, final JTableHeader parent)
  {
    mTree = tree;
    mParent = parent;
  }

  // #########################################################################
  // # Mouse Adapter

  public void mouseClicked(final MouseEvent e){
    final int column = mParent.columnAtPoint(e.getPoint());
    mTree.sortBy(column);
    System.out.println("DEBUG: Sorted by " + column);
  }

  private final EventJTree mTree;
  private final JTableHeader mParent;
}
