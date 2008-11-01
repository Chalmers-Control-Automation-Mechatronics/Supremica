//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.gui
//# CLASS:   SimpleExpressionEditor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

import net.sourceforge.waters.model.expr.ExpressionParser;


public class SimpleExpressionEditor
  extends DefaultCellEditor
  implements FocusListener
{

  //#########################################################################
  //# Constructors
  public SimpleExpressionEditor(final int mask,
                                final ExpressionParser parser)
  {
    super(new SimpleExpressionCell(mask, parser));
    final SimpleExpressionCell cell = getComponent();
    cell.addFocusListener(this);
  }


  public SimpleExpressionEditor(final int mask,
                                final ExpressionParser parser,
                                final ErrorDisplay display)
  {
    super(new SimpleExpressionCell(mask, parser));
    final SimpleExpressionCell cell = getComponent();
    cell.setErrorDisplay(display);
    cell.addFocusListener(this);
  }


  //#########################################################################
  //# Simple Access
  void setAllowNull(final boolean allow)
  {
    final SimpleExpressionCell cell = getComponent();
    cell.setAllowNull(allow);
  }

  void setToolTipText(final String tooltip)
  {
    final SimpleExpressionCell cell = getComponent();
    cell.setToolTipText(tooltip);
  }

  void addFocusListener(final FocusListener listener)
  {
    final SimpleExpressionCell cell = getComponent();
    cell.addFocusListener(listener);
  }


  //#########################################################################
  //# Overrides for base class javax.swing.DefaultCellEditor
  public SimpleExpressionCell getTableCellEditorComponent
    (final JTable table, final Object value, final boolean isSelected,
     final int row, final int column)
  {
    final SimpleExpressionCell textfield =
      (SimpleExpressionCell) super.getTableCellEditorComponent
        (table, value, isSelected, row, column);
    textfield.setValue(value);
    return textfield;
  }

  public Object getCellEditorValue()
  {
    final SimpleExpressionCell textfield = getComponent();
    return textfield.getValue();
  }

  public boolean stopCellEditing()
  {
    final SimpleExpressionCell textfield = getComponent();
    return textfield.shouldYieldFocus() && super.stopCellEditing();
  }

  public SimpleExpressionCell getComponent()
  {
    return (SimpleExpressionCell) super.getComponent();
  }


  //#########################################################################
  //# Interface java.awt.event.FocusListener
  /**
   * Does nothing.
   */
  public void focusGained(final FocusEvent event)
  {
  }

  /**
   * Fixes a bug in Swing.
   * Called when the editor component loses focus,
   * this handler makes sure that every non-temporary loss of focus
   * causes editing to stop.
   */
  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      if (!stopCellEditing()) {
        cancelCellEditing();
      }
      final SimpleExpressionCell textfield = getComponent();
      textfield.clearErrorMessage();
    }
  }

}
