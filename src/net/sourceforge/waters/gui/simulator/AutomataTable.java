//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;


class AutomataTable extends JTable
{

  //#########################################################################
  //# Constructor
  AutomataTable(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super(new AutomataTableModel(sim, desktop));
    mSimulation = sim;
    mDesktop = desktop;
    final TableCellRenderer textRenderer = new TextCellRenderer();
    setDefaultRenderer(String.class, textRenderer);
    final TableCellRenderer iconRenderer = new IconCellRenderer();
    setDefaultRenderer(ImageIcon.class, iconRenderer);
    setDefaultRenderer(Icon.class, iconRenderer);
    setShowGrid(false);
    setIntercellSpacing(new Dimension(0, 0));

    final AutomataTableModel tableModel = getModel();
    final TableColumnModel columnModel = getColumnModel();
    final int iconSize = IconAndFontLoader.getWatersIconSize();
    final int absoluteGap =
      Math.round(IconAndFontLoader.TABLE_ROW_GAP * iconSize);
    setRowHeight(iconSize + absoluteGap);
    final int columnCount = columnModel.getColumnCount();
    if (columnCount != 0) {
      final TableColumn column0 = columnModel.getColumn(0);
      column0.setMinWidth(rowHeight);
      column0.setMaxWidth(rowHeight);
      final BufferedImage img =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g2d = img.createGraphics();
      final FontMetrics fm = g2d.getFontMetrics(getFont());
      for (int i = 1; i < columnCount; i++) {
        final String title = tableModel.getColumnName(i);
        final int width = fm.stringWidth(title);
        final TableColumn column = columnModel.getColumn(i);
        column.setMinWidth(width + absoluteGap);
        final Class<?> clazz = tableModel.getColumnClass(i);
        if (Icon.class.isAssignableFrom(clazz)) {
          column.setMaxWidth(width + absoluteGap);
        } else if (String.class.isAssignableFrom(clazz)) {
          column.setPreferredWidth(2 * width);
        }
      }
      g2d.dispose();
    }
    getTableHeader().setReorderingAllowed(false);
    final ListSelectionModel listModel = getSelectionModel();
    listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    addMouseListener(new AutomatonMouseListener());
    getTableHeader().addMouseListener(new TableHeaderMouseListener());
    mPopupFactory = new SimulatorPopupFactory(sim);
    addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(final MouseEvent event)
      {
        // Do nothing
      }

      @Override
      public void mouseMoved(final MouseEvent event)
      {
        final int row = rowAtPoint(event.getPoint());
        final AutomatonProxy aut = tableModel.getAutomaton(row);
        final ToolTipVisitor visitor = sim.getToolTipVisitor();
        final String tooltip = visitor.getToolTip(aut, true);
        setToolTipText(tooltip);
      }
    });
  }


  //#########################################################################
  //# Overrides for javax.swing.JTable
  @Override
  public AutomataTableModel getModel()
  {
    return (AutomataTableModel) super.getModel();
  }


  //#########################################################################
  //# Inner Class TextCellRenderer
  private class TextCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent
      (final JTable table, final Object value, final boolean selected,
       final boolean focused, final int row, final int column)
    {
      final Component cell = super.getTableCellRendererComponent
        (table, value, selected, false, row, column);
      final AutomataTableModel model = getModel();
      if (mDesktop.automatonIsOpen(model.getAutomaton(row))) {
        final Font oldFont = cell.getFont();
        cell.setFont(oldFont.deriveFont(Font.BOLD));
      }
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 2739259938102695664L;
  }


  //#########################################################################
  //# Inner Class TextCellRenderer
  private class IconCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent
      (final JTable table, final Object value, final boolean selected,
       final boolean focused, final int row, final int column)
    {
      final JLabel cell = (JLabel) super.getTableCellRendererComponent
        (table, value, selected, false, row, column);
      final Icon icon = (Icon) value;
      cell.setIcon(icon);
      cell.setText(null);
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 7455415810847160716L;
  }


  //#########################################################################
  //# Inner Class AutomatonMouseListener
  private class AutomatonMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.event.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final AutomatonProxy aut = getAutomaton(event);
        if (aut != null) {
          mDesktop.addAutomaton(aut.getName(), mSimulation.getModuleContainer(),
                                mSimulation, event.getClickCount());
        }
      }
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      final AutomatonProxy aut = getAutomaton(event);
      if (aut != null) {
        mPopupFactory.maybeShowPopup(AutomataTable.this, event, aut);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      final AutomatonProxy aut = getAutomaton(event);
      if (aut != null) {
        mPopupFactory.maybeShowPopup(AutomataTable.this, event, aut);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private AutomatonProxy getAutomaton(final MouseEvent event)
    {
      final int row = rowAtPoint(event.getPoint());
      if (row >= 0) {
        final AutomataTableModel model = getModel();
        return model.getAutomaton(row);
      } else {
        return null;
      }
    }
  }


  //#########################################################################
  //# Inner Class TableHeaderMouseListener
  private class TableHeaderMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.event.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final JTableHeader header = getTableHeader();
        final int column = header.columnAtPoint(event.getPoint());
        final AutomataTableModel model = getModel();
        model.addSortingMethod(column);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Simulation mSimulation;
  private final AutomatonDesktopPane mDesktop;
  private final SimulatorPopupFactory mPopupFactory;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -9036493474591272655L;

}
