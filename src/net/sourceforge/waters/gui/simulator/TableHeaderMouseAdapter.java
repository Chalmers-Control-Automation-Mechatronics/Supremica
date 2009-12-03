package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

// Marked for Moving to Inner Class
public class TableHeaderMouseAdapter extends MouseAdapter
{
  // #########################################################################
  // # Constructor
  public TableHeaderMouseAdapter(final JTable table, final JTableHeader parent)
  {
    mTable = table;
    mParent = parent;
  }

  // #########################################################################
  // # Mouse Adapter

  public void mouseClicked(final MouseEvent e){
    final int column = mParent.columnAtPoint(e.getPoint());
    ((AbstractTunnelTable)mTable.getModel()).getComparitor().addNewSortingMethod(column);
    ((AbstractTunnelTable)mTable.getModel()).tableOrderChanged();
  }

  // #########################################################################
  // # Data Members

  private final JTable mTable;
  private final JTableHeader mParent;
}
