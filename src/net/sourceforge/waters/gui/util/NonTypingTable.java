//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.util
//# CLASS:   NonTypingTable
//###########################################################################
//# $Id: NonTypingTable.java,v 1.2 2008-03-16 21:27:39 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 * <p>A table with restricted editing capabilities.</p>
 *
 * <P>This class overrides standard editing behaviour of {@link JTable}.
 * Normally, when input is typed (or DELETE is pressed), {@link JTable}
 * will will open a cell editor for the current component and start
 * editing. This override disables the default behaviour, so editing is
 * only possible after having double-clicked a cell. Most importantly,
 * pressing DELETE can now be programmed to delete rows rather than start
 * editing.</P>
 *
 * @author Robi Malik
 */

public class NonTypingTable
  extends JTable
{

  //#########################################################################
  //# Constructors
  public NonTypingTable()
  {
  }

  public NonTypingTable(final int numrows, final int numcolumns)
  {
    super(numrows, numcolumns);
  }

  public NonTypingTable(final Object[][] rowdata, final Object[] columnnames)
  {
    super(rowdata, columnnames);
  }

  public NonTypingTable(final TableModel model)
  {
    super(model);
  }

  public NonTypingTable(final TableModel dmodel, final TableColumnModel cmodel)
  {
    super(dmodel, cmodel);
  }

  public NonTypingTable(final TableModel dmodel,
                        final TableColumnModel cmodel,
                        final ListSelectionModel lmodel)
  {
    super(dmodel, cmodel, lmodel);
  }


  //#########################################################################
  //# Special Key Bindings
  public void addCycleActions()
  {
    final Action down = new CycleAction(CYCLE_DOWN, STROKE_TAB, 1);
    addKeyboardAction(down);
    final Action up = new CycleAction(CYCLE_UP, STROKE_SHIFT_TAB, -1);
    addKeyboardAction(up);
  }

  public void addEscapeAction()
  {
    final Action action = new DeselectAllAction();
    addKeyboardAction(action);
  }

  public void addKeyboardAction(final Action action)
  {
    final String name = (String) action.getValue(Action.NAME);
    final KeyStroke key = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
    final InputMap imap = getInputMap();
    final ActionMap amap = getActionMap();
    if (key != null && imap != null && amap != null) {
      imap.put(key, name);
      amap.put(name, action);
    }
  }
  

  //#########################################################################
  //# Overrides for Base Class javax.swing.JTable
  protected boolean processKeyBinding(final KeyStroke stroke,
                                      final KeyEvent event,
                                      final int cond,
                                      final boolean pressed)
  {
    if (isEditing()) {
      return super.processKeyBinding(stroke, event, cond, pressed);
    } else if (isNavigationKey(stroke)) {
      return super.processKeyBinding(stroke, event, cond, pressed);
    } else if (hasEnabledBinding(this, stroke, cond)) {
      return super.processKeyBinding(stroke, event, cond, pressed);
    } else {
      final int row = getSelectedRow();
      final int column = getSelectedColumn();
      if (row >= 0 && column >= 0) {
        final TableCellRenderer renderer = getCellRenderer(row, column);
        final Component comp = prepareRenderer(renderer, row, column);
        if (comp instanceof JComponent) {
          final JComponent jcomp = (JComponent) comp;
          if (hasEnabledBinding(jcomp, stroke, cond)) {
            return super.processKeyBinding(stroke, event, cond, pressed);
          }
        }
      }
      return false;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static boolean isNavigationKey(final KeyStroke stroke)
  {
    if (stroke.getModifiers() == 0) {
      switch (stroke.getKeyCode()) {
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_END:
      case KeyEvent.VK_HOME:
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_PAGE_DOWN:
      case KeyEvent.VK_PAGE_UP:
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_UP:
        return true;
      default:
        return false;
      }
    } else {
      return false;
    }
  }

  private static boolean hasEnabledBinding(final JComponent comp,
                                           final KeyStroke stroke,
                                           final int cond)
  {
    final InputMap imap = comp.getInputMap();
    if (imap == null) {
      return false;
    }
    final ActionMap amap = comp.getActionMap();
    if (amap == null) {
      return false;
    }
    final Object binding = imap.get(stroke);
    if (binding == null) {
      return false;
    }
    final Action action = amap.get(binding);
    if (action == null) {
      return false;
    }
    return action.isEnabled();
  }


  //#########################################################################
  //# Inner Class DeselectAllAction
  private class DeselectAllAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    DeselectAllAction()
    {
      putValue(Action.NAME, "Deselect All");
      putValue(Action.SHORT_DESCRIPTION, "Clear the selection");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ESCAPE);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }
 
    //#######################################################################
    //# Interface javax.swing.Action
    public boolean isEnabled()
    {
      return getSelectedRowCount() > 0;
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      clearSelection();
    }

  }


  //#########################################################################
  //# Local Class CycleAction
  private class CycleAction extends AbstractAction
  {

    //#######################################################################
    //# Data Members
    private CycleAction(final String name,
                        final KeyStroke stroke,
                        final int offset)
    {
      super(name);
      putValue(Action.ACCELERATOR_KEY, stroke);
      mOffset = offset;
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      if (!isEditing()) {
	final ListSelectionModel selmodel = getSelectionModel();
	final int numrows = getRowCount();
	final int selrow = selmodel.getLeadSelectionIndex();
	final int newrow = (selrow + numrows + mOffset) % numrows;
	setRowSelectionInterval(newrow, newrow);
      }
    }

    //#######################################################################
    //# Data Members
    private final int mOffset;

  }


  //#########################################################################
  //# Class Constants
  private static final KeyStroke STROKE_TAB =
    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
  private static final KeyStroke STROKE_SHIFT_TAB =
    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);

  private static final String CYCLE_DOWN = "Cycle Down";
  private static final String CYCLE_UP = "Cycle Up";

}
