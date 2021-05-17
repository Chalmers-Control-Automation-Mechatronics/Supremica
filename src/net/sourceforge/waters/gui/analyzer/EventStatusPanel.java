//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.set.hash.THashSet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * @author Benjamin Wheeler
 */
public class EventStatusPanel extends JScrollPane
{

  //#########################################################################
  //# Constructors
  public EventStatusPanel(final GUIOptionContext context, final Set<EventProxy> eventSet, final EventEncoding enc)
  {
    final Set<EventProxy> properEventSet = new THashSet<>();
    for (final EventProxy event : eventSet) {
      if (EventSetOption.DefaultKind.PROPER_EVENT.isDefault(event.getKind())) {
        properEventSet.add(event);
      }
    }

    final List<EventProxy> el = new ArrayList<EventProxy>();
    for (int i=0;i<1;i++) for (final EventProxy x : properEventSet) el.add(x);

    final EventProxy[] events = new EventProxy[el.size()];
    el.toArray(events);
    Arrays.sort(events);

    final String[] columnNames =
    {
      "Event",
      "Local",
      "<html>Self-loop<br>Only</html>",
      "<html>Always<br>Enabled</html>",
      "Blocked",
      "Failing"
    };

    mData = new Object[events.length][];

    for (int e=0; e<events.length; e++) {
      final EventProxy event = events[e];
      final int code = enc.getEventCode(event);
      final byte status = code != -1
        ? enc.getProperEventStatus(code) : 0;
      mData[e] = new Object[]
        {
          event,
          EventStatus.isLocalEvent(status),
          EventStatus.isSelfloopOnlyEvent(status),
          EventStatus.isAlwaysEnabledEvent(status),
          EventStatus.isBlockedEvent(status),
          EventStatus.isFailingEvent(status)
        };
    }

    final EventStatusTableModel model = new EventStatusTableModel(columnNames, mData);
    mTable = new JTable(model);
    mTable.setDefaultRenderer(Boolean.class, new ColorRenderer());
    mTable.getColumn("Event").setCellRenderer(new EventRenderer(context));
    mTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e)
      {
        final int r = mTable.rowAtPoint(e.getPoint());
        final int c = mTable.columnAtPoint(e.getPoint());
        if (c < 1 || r < 0) return;
        else {
          final boolean current = (boolean) mTable.getModel().getValueAt(r, c);
          mTable.getModel().setValueAt(!current, r, c);
          if (c == 1) {
            mTable.getModel().setValueAt(!current, r, c+1);
            mTable.getModel().setValueAt(!current, r, c+2);
          }
        }
      }
    });

    setViewportView(mTable);
    mTable.setFillsViewportHeight(true);

    //Adjust column sizes
    final TableColumnModel columnModel = mTable.getColumnModel();
    int maxHeaderWidth = 0;
    int maxHeaderHeight = 0;
    for (int c=1; c<columnNames.length; c++) {
      maxHeaderWidth = Math.max
        (maxHeaderWidth, getColumnHeaderWidth(mTable, c));
      maxHeaderHeight = Math.max
        (maxHeaderHeight, getColumnHeaderHeight(mTable, c));
    }
    for (int c=1; c<columnNames.length; c++) {
      columnModel.getColumn(c).setPreferredWidth(maxHeaderWidth);
    }
    final int headerHeight = maxHeaderHeight;
    setColumnHeader(new JViewport() {
      @Override public Dimension getPreferredSize() {
        final Dimension d = super.getPreferredSize();
        d.height = headerHeight;
        return d;
      }
      private static final long serialVersionUID = 6265069619818553380L;
    });

    int eventColumnWidth = getColumnHeaderWidth(mTable, 0);
    final TableCellRenderer renderer = columnModel.getColumn(0).getCellRenderer();
    for (int r=0; r<mTable.getRowCount(); r++) {
      final Object value = mTable.getValueAt(r, 0);
      final Component comp = renderer.getTableCellRendererComponent(mTable, value, false, false, -1, 0);
      eventColumnWidth = Math.max(eventColumnWidth, comp.getPreferredSize().width);
    }

  }


  //#########################################################################
  //# Simple Access
  public Object[][] getData() {
    return mData;
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getColumnHeaderWidth(final JTable table, final int c)
  {
    final TableColumn tableColumn = table.getColumnModel().getColumn(c);
    final Object value = tableColumn.getHeaderValue();
    final TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
    final Component comp = renderer.getTableCellRendererComponent(table, value, false, false, -1, c);
    return comp.getPreferredSize().width;
  }

  private int getColumnHeaderHeight(final JTable table, final int c)
  {
    final TableColumn tableColumn = table.getColumnModel().getColumn(c);
    final Object value = tableColumn.getHeaderValue();
    final TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
    final Component comp = renderer.getTableCellRendererComponent(table, value, false, false, -1, c);
    return comp.getPreferredSize().height;
  }


  //#########################################################################
  //# Inner Class EventRenderer
  private static class EventRenderer extends JLabel implements TableCellRenderer {

    //#########################################################################
    //# Constructors
    public EventRenderer(final GUIOptionContext context)
    {
      mContext = context;
      setOpaque(true);
    }

    //#########################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus, final int row,
                                                   final int column)
    {
      final EventProxy event = (EventProxy) value;
      setText(event.getName());
      setIcon(mContext.getEventIcon(event));
      setBorder(new EmptyBorder(2, 2, 2, 2));
      if (isSelected) {
        setBackground(table.getSelectionBackground());
        setForeground(table.getSelectionForeground());
      } else {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
      }
      return this;
    }

    //#########################################################################
    //# Data Members
    private final GUIOptionContext mContext;

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 4503571208511104976L;

  }

  //#########################################################################
  //# Inner Class ColorRenderer
  private static class ColorRenderer extends JCheckBox implements TableCellRenderer {

    //#########################################################################
    //# Constructors
    public ColorRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
    }

    //#########################################################################
    //# Interface Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus, final int row,
                                                   final int column)
    {
      if (isSelected) {
        setBackground(table.getSelectionBackground());
      }
      else {
        setBackground(table.getBackground());
      }
      this.setSelected((boolean)value);
      return this;
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = -8561134602306643073L;

  }

  //#########################################################################
  //# Inner Class EventStatusTableModel
  private class EventStatusTableModel extends AbstractTableModel {

    //#########################################################################
    //# Constructors
    public EventStatusTableModel(final String[] columnNames, final Object[][] data) {
      mColumnNames = columnNames;
      mData = data;
    }

    //#########################################################################
    //# Interface javax.swing.table.TableModel
    @Override
    public int getRowCount()
    {
      return mData.length;
    }

    @Override
    public int getColumnCount()
    {
      return mColumnNames.length;
    }

    @Override
    public Object getValueAt(final int r, final int c)
    {
      return mData[r][c];
    }

    //#########################################################################
    //# Overrides for javax.swing.table.AbstractTableModel
    @Override
    public Class<?> getColumnClass(final int c)
    {
      return getValueAt(0, c).getClass();
    }

    @Override
    public String getColumnName(final int c)
    {
      return mColumnNames[c];
    }

    @Override
    public void setValueAt(final Object value, final int r, final int c) {
      mData[r][c] = value;
      fireTableCellUpdated(r, c);
    }

    //#########################################################################
    //# Data Members
    private final String[] mColumnNames;
    private final Object[][] mData;

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 8729609836711878449L;

  }

  //#########################################################################
  //# Data Members
  private final Object[][] mData;
  private final JTable mTable;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -6431481942044924463L;

}
