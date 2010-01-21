//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   AutomataTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


class AutomataTable extends JTable
{

  //#########################################################################
  //# Constructor
  AutomataTable(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super(new AutomataTableModel(sim, desktop));
    mSimulation = sim;
    mDesktop = desktop;
    setRowHeight(AUTOMATA_TABLE_HEIGHT);
    final TableCellRenderer textrenderer = new TextCellRenderer();
    setDefaultRenderer(String.class, textrenderer);
    final TableCellRenderer iconrenderer = new IconCellRenderer();
    setDefaultRenderer(ImageIcon.class, iconrenderer);
    setDefaultRenderer(Icon.class, iconrenderer);
    final TableColumnModel colmodel = getColumnModel();
    if (colmodel.getColumnCount() != 0) {
      colmodel.getColumn(0).setPreferredWidth(NARROW_WIDTH);
      colmodel.getColumn(0).setMaxWidth(NARROW_WIDTH);
      colmodel.getColumn(1).setPreferredWidth(BROAD_WIDTH);
      colmodel.getColumn(2).setPreferredWidth(NARROW_WIDTH);
      colmodel.getColumn(2).setMaxWidth(NARROW_WIDTH);
      colmodel.getColumn(3).setPreferredWidth(NARROW_WIDTH);
      colmodel.getColumn(3).setMaxWidth(NARROW_WIDTH);
      colmodel.getColumn(4).setPreferredWidth(BROAD_WIDTH);
    }
    addMouseListener(new AutomatonMouseListener());
    getTableHeader().setReorderingAllowed(false);
    final ListSelectionModel listMod = getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getTableHeader().addMouseListener(new TableHeaderMouseListener());
    setShowGrid(!DISABLE_AUTOMATON_GRIDLINES);
    this.addMouseMotionListener(new MouseMotionListener(){
      public void mouseDragged(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseMoved(final MouseEvent e)
      {
        final int row = AutomataTable.this.rowAtPoint(e.getPoint());
        final AutomatonProxy auto = AutomataTable.this.getModel().getAutomaton(row);
        String toolTipText = "";
        if (auto.getKind() == ComponentKind.PLANT){
          toolTipText += "Plant " + auto.getName();
        }
        else
        {
          toolTipText += "Specification " + auto.getName();
        }
        if (mSimulation.changedLastStep(auto))
          toolTipText += " has an event which was fired last step";
        setToolTipText(toolTipText);
      }

    });
  }


  //#########################################################################
  //# Overrides for javax.swing.JTable
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
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class TextCellRenderer
  private class IconCellRenderer extends DefaultTableCellRenderer
  {

    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
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
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class AutomatonMouseListener
  private class AutomatonMouseListener extends MouseAdapter
  {

    //#######################################################################
    //# Interface java.awt.event.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final int row = rowAtPoint(event.getPoint());
        if (row >= 0) {
          final AutomataTableModel model = getModel();
          final AutomatonProxy toAdd = model.getAutomaton(row);
          mDesktop.addAutomaton(toAdd.getName(), mSimulation.getContainer(),
                                mSimulation, event.getClickCount());
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class TableHeaderMouseListener
  private class TableHeaderMouseListener extends MouseAdapter
  {

    //#######################################################################
    //# Interface java.awt.event.MouseListener
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


  //#########################################################################
  //# Class Constants
  private static final boolean DISABLE_AUTOMATON_GRIDLINES = true;

  private static final int AUTOMATA_TABLE_HEIGHT = 20;

  // DEBUG: Arbitrary value: Any value will work,
  // but this is close to the 'normal' value
  private static final int DUMMY_WIDTH = 245;
  private static final int NARROW_WIDTH = DUMMY_WIDTH / 10;
  private static final int BROAD_WIDTH = 35 * DUMMY_WIDTH / 100;

  private static final long serialVersionUID = 1L;

}
