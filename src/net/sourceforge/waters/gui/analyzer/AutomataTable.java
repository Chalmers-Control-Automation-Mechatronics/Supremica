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

package net.sourceforge.waters.gui.analyzer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.ModuleContainer;


class AutomataTable extends JTable
{

  //#########################################################################
  //# Constructor
  AutomataTable(final ModuleContainer ModContainer,
                final WatersAnalyzerPanel Parent)
  {
    super(new AutomataTableModel(ModContainer));
    //mModuleContainer = ModContainer;
    //mCompiledDES = mModuleContainer.getCompiledDES();
    mParent = Parent;
    final TableCellRenderer textRenderer = new TextCellRenderer();
    setDefaultRenderer(String.class, textRenderer);
    setDefaultRenderer(int.class, textRenderer);
    final TableCellRenderer iconRenderer = new IconCellRenderer();
    setDefaultRenderer(ComponentKind.class, iconRenderer);
    setShowGrid(false);
    setIntercellSpacing(new Dimension(0, 0));

    final AutomataTableModel tableModel = getModel();
    final TableColumnModel columnModel = getColumnModel();
    final int columnGap = IconAndFontLoader.getTableColumnGap();
    final int rowHeight = IconAndFontLoader.getPreferredTableRowHeight();
    setRowHeight(rowHeight);
    final int columnCount = columnModel.getColumnCount();
    if (columnCount != 0) {
      final TableColumn column0 = columnModel.getColumn(0);
      column0.setMinWidth(rowHeight);
      column0.setMaxWidth(rowHeight);
      final BufferedImage img =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g2d = img.createGraphics();
      final FontMetrics fm = g2d.getFontMetrics(getFont());
      final String titleName = tableModel.getColumnName(1);
      final int widthName = fm.stringWidth(titleName);
      final TableColumn column1 = columnModel.getColumn(1);
      column1.setMinWidth(widthName + columnGap);
      column1.setPreferredWidth(2 * widthName);
      for (int i = 2; i < columnCount; i++) {
        final TableColumn column = columnModel.getColumn(i);
        column.setMinWidth(40);
        column.setMaxWidth(45);
      }
      g2d.dispose();
    }
    getTableHeader().setReorderingAllowed(false);
    final ListSelectionModel listModel = getSelectionModel();
    listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    this.addMouseListener(new TableMouseListener());
  }

  //#########################################################################
  //# Overrides for javax.swing.JTable
  @Override
  public AutomataTableModel getModel()
  {
    return (AutomataTableModel) super.getModel();
  }

  @Override
  public String getToolTipText(final MouseEvent event)
  {
    final AutomatonProxy aut = getAutomaton(event);
    final ComponentKind kind = aut.getKind();
    if (aut != null) {
      String Tooltip = ModuleContext.getComponentKindToolTip(kind);
      Tooltip += (" " + aut.getName());
      return Tooltip;
    } else {
      return null;
    }
  }

  //#########################################################################
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


  //#########################################################################
  //# Inner Class TextCellRenderer
  /**
   * A text renderer for the simulator's automata table. This renderer
   * displays text without the focus rectangle, and changes the font to bold
   * if the automaton is open on the simulator's desktop.
   */
  private class TextCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean selected,
                                                   final boolean focused,
                                                   final int row,
                                                   final int column)
    {
      final Component cell =
        super.getTableCellRendererComponent(table, value, selected, false,
                                            row, column);
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 2739259938102695664L;
  }


  //#########################################################################
  //# Inner Class IconCellRenderer
  private class IconCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean selected,
                                                   final boolean focused,
                                                   final int row,
                                                   final int column)
    {
      final JLabel cell =
        (JLabel) super.getTableCellRendererComponent(table, value, selected,
                                                     false, row, column);
      final ComponentKind kind = (ComponentKind) value;
      final Icon icon = ModuleContext.getComponentKindIcon(kind);
      cell.setIcon(icon);
      cell.setText(null);
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 7455415810847160716L;
  }


  //#########################################################################
  //# Inner Class TableHeaderMouseListener
  private class TableMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.event.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1
          && event.getClickCount() == 2) {
        final AutomatonProxy aut = getAutomaton(event);
        if (aut != null) {
          mParent.displaySelectedAutomata(aut);
        }
      }
    }
  }

  //#########################################################################
  //# Data Members
  final WatersAnalyzerPanel mParent;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -9036493474591272655L;

}
