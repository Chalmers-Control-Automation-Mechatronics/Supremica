package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class TableHeaderMouseAdapter extends MouseAdapter
{

  private final JTable mTable;
  private final JTableHeader mParent;

  public TableHeaderMouseAdapter(final JTable table, final JTableHeader parent)
  {
    mTable = table;
    mParent = parent;
  }

  public void mouseClicked(final MouseEvent e){
    final int column = mParent.columnAtPoint(e.getPoint());
    ((AbstractTunnelTable)mTable.getModel()).getComparitor().addNewSortingMethod(column);
    ((AbstractTunnelTable)mTable.getModel()).tableOrderChanged();
  }
}
