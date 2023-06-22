//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

// Imports for picking up mouse events from the JTable.
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableSorter
    extends TableMap
{
    private static final long serialVersionUID = 1L;

    int indexes[];
    Vector<Integer> sortingColumns = new Vector<Integer>();
    boolean ascending = true;
    int compares;

    public TableSorter()
    {
        indexes = new int[0];    // for consistency
    }

    public TableSorter(final TableModel model)
    {
        setModel(model);
    }

    @Override
    public void setModel(final TableModel model)
    {
        super.setModel(model);
        reallocateIndexes();
    }

    public int compareRowsByColumn(final int row1, final int row2, final int column)
    {
        final Class<?> type = model.getColumnClass(column);
        final TableModel data = model;

        // Check for nulls.
        final Object o1 = data.getValueAt(row1, column);
        final Object o2 = data.getValueAt(row2, column);

        // If both values are null, return 0.
        if ((o1 == null) && (o2 == null))
        {
            return 0;
        }
        else if (o1 == null)
        {    // Define null less than everything.
            return -1;
        }
        else if (o2 == null)
        {
            return 1;
        }

                /*
                 * We copy all returned values from the getValue call in case
                 * an optimised model is reusing one object to return many
                 * values.  The Number subclasses in the JDK are immutable and
                 * so will not be used in this way but other subclasses of
                 * Number might want to do this to save space and avoid
                 * unnecessary heap allocation.
                 */
        if (type.getSuperclass() == java.lang.Number.class)
        {
            final Number n1 = (Number) data.getValueAt(row1, column);
            final double d1 = n1.doubleValue();
            final Number n2 = (Number) data.getValueAt(row2, column);
            final double d2 = n2.doubleValue();

            if (d1 < d2)
            {
                return -1;
            }
            else if (d1 > d2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == java.util.Date.class)
        {
            final Date d1 = (Date) data.getValueAt(row1, column);
            final long n1 = d1.getTime();
            final Date d2 = (Date) data.getValueAt(row2, column);
            final long n2 = d2.getTime();

            if (n1 < n2)
            {
                return -1;
            }
            else if (n1 > n2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == String.class)
        {
            final String s1 = (String) data.getValueAt(row1, column);
            final String s2 = (String) data.getValueAt(row2, column);
            final int result = s1.compareToIgnoreCase(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == Boolean.class)
        {
            final Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            final boolean b1 = bool1.booleanValue();
            final Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            final boolean b2 = bool2.booleanValue();

            if (b1 == b2)
            {
                return 0;
            }
            else if (b1)
            {    // Define false < true
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            final Object v1 = data.getValueAt(row1, column);
            final String s1 = v1.toString();
            final Object v2 = data.getValueAt(row2, column);
            final String s2 = v2.toString();
            final int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

    public int compare(final int row1, final int row2)
    {
        compares++;

        for (int level = 0; level < sortingColumns.size(); level++)
        {
            final Integer column = sortingColumns.elementAt(level);
            final int result = compareRowsByColumn(row1, row2, column.intValue());

            if (result != 0)
            {
                return ascending
                    ? result
                    : -result;
            }
        }

        return 0;
    }

    public void reallocateIndexes()
    {
        final int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++)
        {
            indexes[row] = row;
        }
    }

    @Override
    public void tableChanged(final TableModelEvent e)
    {

        // System.out.println("Sorter: tableChanged");
        reallocateIndexes();
        super.tableChanged(e);
    }

    public void checkModel()
    {
        if (indexes.length != model.getRowCount())
        {

            // System.err.println("Sorter not informed of a change in model.");
        }
    }

    public void sort(final Object sender)
    {
        checkModel();

        compares = 0;

        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort(indexes.clone(), indexes, 0, indexes.length);

        // System.out.println("Compares: "+compares);
    }

    public void n2sort()
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            for (int j = i + 1; j < getRowCount(); j++)
            {
                if (compare(indexes[i], indexes[j]) == -1)
                {
                    swap(i, j);
                }
            }
        }
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assignments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(final int from[], final int to[], final int low, final int high)
    {
        if (high - low < 2)
        {
            return;
        }

        final int middle = (low + high) / 2;

        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

                /*
                 * This is an optional short-cut; at each recursive call,
                 * check to see if the elements in this subset are already
                 * ordered.  If so, no further comparisons are needed; the
                 * sub-array can just be copied.  The array must be copied rather
                 * than assigned otherwise sister calls in the recursion might
                 * get out of sinc.  When the number of elements is three they
                 * are partitioned so that the first set, [low, mid), has one
                 * element and and the second, [mid, high), has two. We skip the
                 * optimisation when the number of elements is three or less as
                 * the first compare in the normal merge will produce the same
                 * sequence of steps. This optimisation seems to be worthwhile
                 * for partially ordered lists but some analysis is needed to
                 * find out how the performance drops to Nlog(N) as the initial
                 * order diminishes - it may drop very quickly.
                 */
        if ((high - low >= 4) && (compare(from[middle - 1], from[middle]) <= 0))
        {
            for (int i = low; i < high; i++)
            {
                to[i] = from[i];
            }

            return;
        }

        // A normal merge.
        for (int i = low; i < high; i++)
        {
            if ((q >= high) || ((p < middle) && (compare(from[p], from[q]) <= 0)))
            {
                to[i] = from[p++];
            }
            else
            {
                to[i] = from[q++];
            }
        }
    }

    public void swap(final int i, final int j)
    {
        final int tmp = indexes[i];

        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".
    @Override
    public Object getValueAt(final int aRow, final int aColumn)
    {
        checkModel();

        try
        {
            return model.getValueAt(indexes[aRow], aColumn);
        }
        catch (final Exception ex)
        {
            // Throws irritating exceptions... let's just ignore them...
            return null;
        }
    }

    public int getOriginalRowIndex(final int aRow)
    {
        return indexes[aRow];
    }

    @Override
    public void setValueAt(final Object aValue, final int aRow, final int aColumn)
    {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(final int column)
    {
        sortByColumn(column, true);
    }

    public void sortByColumn(final int column, final boolean ascending)
    {
        this.ascending = ascending;

        sortingColumns.removeAllElements();
        sortingColumns.addElement(column);
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(final JTable table)
    {
        final TableSorter sorter = this;
        final JTable tableView = table;

        tableView.setColumnSelectionAllowed(false);

        final MouseAdapter listMouseListener = new MouseAdapter()
        {
            private int currentSortColumn = -1;
            private boolean ascending = true;

            @Override
            public void mouseClicked(final MouseEvent e)
            {
                final TableColumnModel columnModel = tableView.getColumnModel();
                final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                final int column = tableView.convertColumnIndexToModel(viewColumn);

                if ((e.getClickCount() == 1) && (column != -1))
                {
                    // System.out.println("Sorting ...");
                    //int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    //boolean ascending = (shiftPressed == 0);
                    if (column == currentSortColumn)
                    {
                        // Another click on same column? Turn order around!
                        ascending = !ascending;
                    }
                    else
                    {
                        currentSortColumn = column;
                        ascending = true;
                    }

                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        final JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
