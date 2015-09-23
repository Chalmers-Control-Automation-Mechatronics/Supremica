//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.gui.dialog;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

import net.sourceforge.waters.gui.ErrorDisplay;
import net.sourceforge.waters.model.expr.ExpressionParser;


/**
 * @author Robi Malik
 */

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
  public void setAllowNull(final boolean allow)
  {
    final SimpleExpressionCell cell = getComponent();
    cell.setAllowNull(allow);
  }

  public void setToolTipText(final String tooltip)
  {
    final SimpleExpressionCell cell = getComponent();
    cell.setToolTipText(tooltip);
  }

  public void addFocusListener(final FocusListener listener)
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
