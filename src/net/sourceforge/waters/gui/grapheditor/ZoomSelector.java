//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.grapheditor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEToolBar;


/**
 * The zoom selection combo box in the main toolbar.
 * The zoom selector appears in the toolbar ({@link IDEToolBar}) when a graph
 * is being edited. It allows the user to select a zoom factor from a set
 * of predefined values or enter a percentage into the text field. When
 * the selection changes, the selected value is applied to scale the displayed
 * graph. The zoom factor is remember separately for each graph, and the zoom
 * selector is updated when switching graphs.
 *
 * @author Robi Malik
 */

public class ZoomSelector
  extends JComboBox<ZoomSelector.ZoomOption>
  implements ItemListener, Observer
{

  //#########################################################################
  //# Constructor
  public ZoomSelector(final IDE ide)
  {
    super();
    mIDE = ide;
    setMaximumRowCount(2 * STANDARD_VALUES.length);
    for (final double value : STANDARD_VALUES) {
      final ZoomOption option = new FixedZoomOption(value);
      addItem(option);
      if (value == 1.0) {
        setSelectedItem(option);
      }
    }
    for (int i = STANDARD_VALUES.length - 2; i >= 0; i--) {
      final double value = 1.0 / STANDARD_VALUES[i];
      addItem(new FixedZoomOption(value));
    }
    addItem(new FitZoomOption());
    setMaximumSize(getMinimumSize());
    setEditable(true);
    final JTextComponent textField =
      (JTextComponent) getEditor().getEditorComponent();
    final AbstractDocument document =
      (AbstractDocument) textField.getDocument();
    document.setDocumentFilter(new PercentDocumentFilter());
    textField.addMouseListener(new SelectAllMouseListener(textField));
    addItemListener(this);
    mIDE.attach(this);
    setToolTipText("Zoom factor");
  }


  //#########################################################################
  //# Interface java.awt.event.ItemListener
  @Override
  public void itemStateChanged(final ItemEvent event)
  {
    if (event.getStateChange() == ItemEvent.SELECTED) {
      final Object item = event.getItem();
       if (item instanceof ZoomOption) {
        final ZoomOption option = (ZoomOption) item;
        final ComponentEditorPanel iface = mIDE.getActiveComponentEditorPanel();
        if (iface != null) {
          final GraphEditorPanel panel = iface.getGraphEditorPanel();
          option.select(panel);
        }
      } else if (item instanceof String) {
        try {
          final String text = (String) item;
          final ZoomOption option = new FixedZoomOption(text);
          setSelectedItem(option);
          // this will trigger another event and call the above
        } catch (final NumberFormatException exception) {
          // bad input - ignore
        }
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      final GraphEditorPanel panel = getGraphEditorPanel();
      if (panel != null) {
        final double zoom = panel.getZoomFactor();
        final Object item = getSelectedItem();
        if (item instanceof ZoomOption) {
          final ZoomOption option = (ZoomOption) item;
          if (option.getValue() == zoom) {
            return;
          }
        }
        removeItemListener(this);
        final ZoomOption option = new FixedZoomOption(zoom);
        setSelectedItem(option);
        addItemListener(this);
      }
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private GraphEditorPanel getGraphEditorPanel()
  {
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null) {
      return null;
    }
    final EditorPanel editor = container.getEditorPanel();
    if (editor == null) {
      return null;
    }
    final ComponentEditorPanel compEditor = editor.getActiveComponentEditorPanel();
    if (compEditor == null) {
      return null;
    }
    return compEditor.getGraphEditorPanel();
  }


  //#########################################################################
  //# Inner Interface ZoomOption
  interface ZoomOption
  {
    public double getValue();

    public void select(GraphEditorPanel panel);
  }


  //#########################################################################
  //# Inner Class FixedZoomOption
  private static class FixedZoomOption implements ZoomOption
  {
    //#######################################################################
    //# Constructor
    private FixedZoomOption(String text)
    {
      if (text.endsWith("%")) {
        final int len = text.length();
        text = text.substring(0, len - 1);
      }
      final int percentage = Integer.parseInt(text);
      if (percentage <= 0) {
        throw new NumberFormatException("Invalid zoom factor " + percentage + "!");
      }
      mValue = 0.01 * percentage;
    }

    private FixedZoomOption(final double value)
    {
      mValue = value;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.editor.ZoomSelector.ZoomOption
    @Override
    public double getValue()
    {
      return mValue;
    }

    @Override
    public void select(final GraphEditorPanel panel)
    {
      panel.setZoomFactor(mValue);
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return String.format("%.0f%%", 100.0 * mValue);
    }

    //#######################################################################
    //# Data Members
    private final double mValue;
  }


  //#########################################################################
  //# Inner Class FitZoomOption
  private class FitZoomOption implements ZoomOption
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.gui.editor.ZoomSelector.ZoomOption
    @Override
    public double getValue()
    {
      final GraphEditorPanel panel = getGraphEditorPanel();
      if (panel == null) {
        return 1.0;
      } else {
        return panel.getZoomFactorToFit();
      }
    }

    @Override
    public void select(final GraphEditorPanel panel)
    {
      final double value = getValue();
      final ZoomOption option = new FixedZoomOption(value);
      setSelectedItem(option);
      // This sets a fixed zoom factor to update the displayed percentage
      // in the zoom selector in addition to scaling the graph.
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return "Fit";
    }
  }


  //#########################################################################
  //# Inner Class PercentDocumentFilter
  /**
   * A document filter for the zoom selection text entry. It restricts
   * text entry to digits possibly followed by a percent sign,
   */
  private static class PercentDocumentFilter extends DocumentFilter
  {
    //#######################################################################
    //# Overrides for javax.swing.text.DocumentFilter
    @Override
    public void insertString(final DocumentFilter.FilterBypass bypass,
                             final int offset,
                             final String text,
                             final AttributeSet attribs)
      throws BadLocationException
    {
      final boolean atEnd = offset == bypass.getDocument().getLength();
      if (matches(text, atEnd)) {
        bypass.insertString(offset, text, attribs);
      }
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass bypass,
                        final int offset,
                        final int length,
                        final String text,
                        final AttributeSet attribs)
      throws BadLocationException
    {
      final boolean atEnd = offset + length == bypass.getDocument().getLength();
      if (matches(text, atEnd)) {
        bypass.replace(offset, length, text, attribs);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private static boolean matches(final String text, final boolean atEnd)
    {
      for (int i = 0; i < text.length(); i++) {
        final char ch = text.charAt(i);
        if (!Character.isDigit(ch)) {
          return ch == '%' && atEnd && i == text.length() - 1;
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class SelectAllMouseListener
  /**
   * A mouse listener to select all text when the zoom selector's text
   * box is clicked. This allows the user to type in digits without
   * having to erase the previous value.
   */
  private static class SelectAllMouseListener
    extends MouseAdapter
    implements Runnable
  {
    //#######################################################################
    //# Constructor
    private SelectAllMouseListener(final JTextComponent textField)
    {
      mTextField = textField;
    }

    //#######################################################################
    //# Interface java.awt.event.FocusListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      SwingUtilities.invokeLater(this);
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      mTextField.selectAll();
    }

    //#######################################################################
    //# Data Members
    private final JTextComponent mTextField;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;


  //#########################################################################
  //# Class Constants
  private static final double[] STANDARD_VALUES = {
    4.0, 2.0, 1.5, 1.25, 1.0
  };

  private static final long serialVersionUID = -5761159962597604945L;

}
