//# -*-  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   VariableMarkingTableModel
//###########################################################################
//# $Id: VariableMarkingTableModel.java,v 1.1 2007-08-11 10:44:03 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.VariableMarkingElement;


/**
 * @author Robi Malik
 */

public class VariableMarkingTableModel
  extends AbstractTableModel
  implements CellEditorListener
{  

  //#########################################################################
  //# Constructors
  VariableMarkingTableModel(final List<VariableMarkingProxy> list)
  {
    mList = list;
    fireTableChanged(new TableModelEvent(this));
  }


  //#########################################################################
  //# Interface javax.swing.TableModel
  public int getRowCount()
  {
    return mList.size();
  }

  public int getColumnCount()
  {
    return 2;
  }

  public SimpleExpressionProxy getValueAt(final int row, final int column)
  {
    final VariableMarkingProxy entry = mList.get(row);
    switch (column) {
    case 0:
      return entry.getProposition();
    case 1:
      return entry.getPredicate();
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for variable marking table model!");
    }
  }

  public boolean isCellEditable(final int row, final int column)
  {
    return true;
  }

  public void setValueAt(final Object value,
                         final int row,
                         final int column)
  {
    if (column <= 1) {
      final VariableMarkingProxy entry = mList.get(row);
      if (value == null && entry == null) {
        mList.remove(row);
        fireTableRowsDeleted(row, row);
      }
      final IdentifierProxy prop;
      final SimpleExpressionProxy pred;
      if (column == 0) {
        prop = (IdentifierProxy) value;
        pred = entry == null ? null : entry.getPredicate();
      } else {
        prop = entry == null ? null : entry.getProposition();
        pred = (SimpleExpressionProxy) value;
      }
      final VariableMarkingProxy newentry =
        new VariableMarkingElement(prop, pred);
      mList.set(row, newentry);
      fireTableRowsUpdated(row, row);
    } else {
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for variable marking table model!");
    }
  }


  //#########################################################################
  //# Interface javax.swing.event.CellEditorListener
  public void editingCanceled(final ChangeEvent event)
  {
    cleanupEditedItemAtEnd();
  }

  public void editingStopped(final ChangeEvent event)
  {
    cleanupEditedItemAtEnd();
  }


  //#########################################################################
  //# Data Access
  List<VariableMarkingProxy> getList()
  {
    return mList;
  }


  //#########################################################################
  //# List Modifications
  int createEditedItemAtEnd()
  {
    final int row = mList.size();
    if (row > 0 && mList.get(row - 1) == null) {
      return row - 1;
    } else {
      mList.add(null);
      fireTableRowsInserted(row, row);
      return row;
    }
  }

  /**
   * Checks whether the last entry in the list is <CODE>null</CODE>,
   * and if so, removes it.
   */
  void cleanupEditedItemAtEnd()
  {
    int row = mList.size();
    if (row > 0 && mList.get(row - 1) == null) {
      row--;
      removeRow(row);
    }
  }

  void removeRow(final int row)
  {
    mList.remove(row);
    fireTableRowsDeleted(row, row);
  }

  int moveUp(final int[] selrows)
  {
    final int numselrows = selrows.length;
    if (numselrows > 0) {
      final int selrow0 = selrows[0];
      final int firstrow = selrow0 > 0 ? selrow0 - 1 : selrow0;
      final int lastrow = selrows[numselrows - 1];
      if (firstrow + numselrows - 1 < lastrow) {
        final List<VariableMarkingProxy> list = new ArrayList<VariableMarkingProxy>(lastrow - firstrow + 1);
        for (int i = 0; i < numselrows; i++) {
          final int row = selrows[i];
          final VariableMarkingProxy item = mList.get(row);
          list.add(item);
        }
        int nextindex = 0;
        for (int row = firstrow; row <= lastrow; row++) {
          if (row < selrows[nextindex]) {
            final VariableMarkingProxy item = mList.get(row);
            list.add(item);
          } else {
            nextindex++;
          }
        }
        int putrow = firstrow;
        for (final VariableMarkingProxy item : list) {
          mList.set(putrow++, item);
        }
        fireTableRowsUpdated(firstrow, lastrow);
        return firstrow;
      }
    }
    return -1;
  }

  int moveDown(final int[] selrows)
  {
    final int numselrows = selrows.length;
    if (numselrows > 0) {
      final int lastindex = numselrows - 1;
      final int eol = mList.size() - 1;
      final int selrown = selrows[lastindex];
      final int firstrow = selrows[0];
      final int lastrow = selrown < eol ? selrown + 1 : selrown;
      if (firstrow + lastindex < lastrow) {
        final List<VariableMarkingProxy> list = new ArrayList<VariableMarkingProxy>(lastrow - firstrow + 1);
        for (int i = lastindex; i >= 0; i--) {
          final int row = selrows[i];
          final VariableMarkingProxy item = mList.get(row);
          list.add(item);
        }
        int nextindex = lastindex;
        for (int row = lastrow; row >= firstrow; row--) {
          if (row > selrows[nextindex]) {
            final VariableMarkingProxy item = mList.get(row);
            list.add(item);
          } else {
            nextindex--;
          }
        }
        int putrow = lastrow;
        for (final VariableMarkingProxy item : list) {
          mList.set(putrow--, item);
        }
        fireTableRowsUpdated(firstrow, lastrow);
        return lastrow - lastindex;
      }
    }
    return -1;
  }


  //#######################################################################
  //# Data Members
  private final List<VariableMarkingProxy> mList;

}
