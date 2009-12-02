package net.sourceforge.waters.gui.simulator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.table.TableColumnModel;

public class PreserveWidthComponentListener implements PropertyChangeListener
{
  public PreserveWidthComponentListener(final TableColumnModel parent, final int column)
  {
    mParent = parent;
    initialWidth = 0;
    mColumn = column;
  }

  public void propertyChange(final PropertyChangeEvent evt)
  {
    if (initialWidth != 0)
    {
      final int totalWidth = mParent.getTotalColumnWidth();
      int totalPreferredWidth = 0;
      for (int columnIndex = 0; columnIndex < mParent.getColumnCount(); columnIndex++)
      {
        totalPreferredWidth += mParent.getColumn(columnIndex).getPreferredWidth();
      }
      final int preferredWidth = (initialWidth * totalPreferredWidth) / totalWidth;
      mParent.getColumn(mColumn).setPreferredWidth(preferredWidth);
      mParent.getColumn(mColumn).setPreferredWidth(1); // DEBUG: This should make the columns REALLY narrow. It doesn't.
        //If it does, remove this line, and the program should run as expected
    }
  }

  // ###############################################################################
  // # Data Members

  private final TableColumnModel mParent;
  private final int initialWidth;
  private final int mColumn;


}
