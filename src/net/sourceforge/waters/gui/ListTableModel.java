//# -*-  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ListTableModel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;


/**
 * @author Robi Malik
 */

public class ListTableModel<E>
  extends AbstractTableModel
  implements CellEditorListener
{  

  //#########################################################################
  //# Constructors
  ListTableModel(final List<E> list, final Class<E> clazz)
  {
    mList = list;
    mClass = clazz;
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
    return 1;
  }

  public Object getValueAt(final int row, final int column)
  {
    if (column == 0) {
      return mList.get(row);
    } else {
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for list table model!");
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
    if (column == 0) {
      if (value == null) {
        mList.remove(row);
        fireTableRowsDeleted(row, row);
      } else {
        final E downcast = mClass.cast(value);
        mList.set(row, downcast);
        fireTableRowsUpdated(row, row);
      }
    } else {
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for list table model!");
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
  List<E> getList()
  {
    return mList;
  }

  void setList(final List<E> list)
  {
    mList = list;
    fireTableChanged(new TableModelEvent(this));
  }


  //#########################################################################
  //# List Modifications
  int createEditedItemAtEnd()
  {
    return createEditedItemAtEnd(null);
  }

  int createEditedItemAtEnd(final E item)
  {
    final int row = mList.size();
    if (row > 0 && mList.get(row - 1) == item) {
      return row - 1;
    } else {
      mList.add(item);
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
        final List<E> list = new ArrayList<E>(lastrow - firstrow + 1);
        for (int i = 0; i < numselrows; i++) {
          final int row = selrows[i];
          final E item = mList.get(row);
          list.add(item);
        }
        int nextindex = 0;
        for (int row = firstrow; row <= lastrow; row++) {
          if (row < selrows[nextindex]) {
            final E item = mList.get(row);
            list.add(item);
          } else {
            nextindex++;
          }
        }
        int putrow = firstrow;
        for (final E item : list) {
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
        final List<E> list = new ArrayList<E>(lastrow - firstrow + 1);
        for (int i = lastindex; i >= 0; i--) {
          final int row = selrows[i];
          final E item = mList.get(row);
          list.add(item);
        }
        int nextindex = lastindex;
        for (int row = lastrow; row >= firstrow; row--) {
          if (row > selrows[nextindex]) {
            final E item = mList.get(row);
            list.add(item);
          } else {
            nextindex--;
          }
        }
        int putrow = lastrow;
        for (final E item : list) {
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
  private List<E> mList;
  private final Class<E> mClass;

}
