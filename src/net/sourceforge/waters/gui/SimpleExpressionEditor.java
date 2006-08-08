//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.gui
//# CLASS:   SimpleExpressionEditor
//###########################################################################
//# $Id: SimpleExpressionEditor.java,v 1.1 2006-08-08 23:59:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;


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
    }
  }

}
