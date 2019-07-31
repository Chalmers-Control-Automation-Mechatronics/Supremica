//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.Action;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.ErrorDisplay;
import net.sourceforge.waters.gui.transfer.Defocusable;


/**
 * <P>A text field with support for validation and error display.</P>
 *
 * <P>A validating cell allows the user to input text representing
 * representing an numeric value or other object. The type of object
 * edited is determined by the type parameter <CODE>T</CODE> of the
 * class. A {@link FormattedInputHandler} is used to determine how
 * these objects are converted to and from text.</P>
 *
 * <P>This class provides support for use inside a table or list. A
 * {@link FormattedInputHandler} is used to validate and format the input, and
 * error messages from the parsing process can be sent to a configurable
 * destination. Through the {@link FormattedInputHandler}, it is also
 * possible to restrict the allowed characters in the text field.</P>
 *
 * <P>This class is implemented as a subclass of Swing's {@link
 * JFormattedTextField}. Thanks to the {@link FormattedInputHandler}, it is
 * no longer necessary to implement the {@link InputVerifier}, {@link
 * javax.swing.JFormattedTextField.AbstractFormatter AbstractFormatter}, and
 * {@link DocumentFilter} interfaces explicitly. In addition, this class
 * provides type-safe access.</P>
 *
 * @author Robi Malik
 */

public abstract class ValidatingTextCell<T>
  extends JFormattedTextField
  implements Defocusable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a customised validating cell.
   * @param  handler    An input handler to validate and format the text input.
   * @see    FormattedInputHandler
   */
  public ValidatingTextCell(final FormattedInputHandler<? extends T> handler)
  {
    mInputHandler = handler;
    mVerifier = new ValidatingCellVerifier();
    final JFormattedTextField.AbstractFormatter formatter =
      new ValidatingCellFormatter();
    final DefaultFormatterFactory factory =
      new DefaultFormatterFactory(formatter);
    setFormatterFactory(factory);
    setInputVerifier(mVerifier);
  }

  /**
   * Creates a customised validating cell.
   * @param  value      The initial value for the text field.
   * @param  handler    An input handler to validate and format the text input.
   * @see    FormattedInputHandler
   */
  public ValidatingTextCell(final T value,
                            final FormattedInputHandler<? extends T> handler)
  {
    this(handler);
    setValue(value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.Defocusable
  /**
   * Checks whether the current cell input is valid.
   * This method calls the cell's input parser to perform input validation.
   * @return <CODE>true</CODE> if the cell contents have been found to be
   *         valid and keyboard focus can be transferred to another
   *         component.
   */
  @Override
  public boolean shouldYieldFocus()
  {
    if (isEnabled()) {
      return mVerifier.shouldYieldFocus(this);
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Restores the cell to the content it had when it was last committed.
   */
  public void revert()
  {
    final Object oldValue = getValue();
    setValue(oldValue);
  }

  public void addSimpleDocumentListener(final SimpleDocumentListener listener)
  {
    final DocumentListener docListener = new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent event)
      {
        listener.documentChanged(event);
      }
      @Override
      public void removeUpdate(final DocumentEvent event)
      {
        listener.documentChanged(event);
      }
      @Override
      public void changedUpdate(final DocumentEvent event)
      {
        listener.documentChanged(event);
      }
    };
    addDocumentListener(docListener);
  }

  public void addDocumentListener(final DocumentListener listener)
  {
    getDocument().addDocumentListener(listener);
  }


  //#########################################################################
  //# Actions
  /**
   * Sets a keyboard binding for the &langle;ENTER&rangle; key.
   */
  public void addEnterAction(final Action action)
  {
    addKeyAction(KeyEvent.VK_ENTER, action);
  }

  /**
   * Sets a keyboard binding for the &langle;ESCAPE&rangle; key.
   */
  public void addEscapeAction(final Action action)
  {
    addKeyAction(KeyEvent.VK_ESCAPE, action);
  }

  /**
   * Sets a keyboard binding for the given key.
   */
  public void addKeyAction(final int keycode, final Action action)
  {
    final KeyStroke stroke = KeyStroke.getKeyStroke(keycode, 0);
    final String name = (String) action.getValue(Action.NAME);
    getInputMap().put(stroke, name);
    getActionMap().put(name, action);
  }


  //#########################################################################
  //# Error Display
  /**
   * Gets the error display associated with this cell.
   * @see #setErrorDisplay(ErrorDisplay) setErrorDisplay()
   */
  public ErrorDisplay getErrorDisplay()
  {
    return mErrorDisplay;
  }

  /**
   * Associates an error display with this cell.
   * The error display gets notified about any errors obtained
   * from the parser during input validation.
   */
  public void setErrorDisplay(final ErrorDisplay display)
  {
    mErrorDisplay = display;
  }

  /**
   * Sets the given message in the cell's error display.
   * @see #setErrorDisplay(ErrorDisplay) setErrorDisplay()
   */
  public void setErrorMessage(final String msg)
  {
    if (mErrorDisplay != null) {
      mErrorDisplay.displayError(msg);
    }
  }

  /**
   * Clears the cell's error display.
   * @see #setErrorDisplay(ErrorDisplay) setErrorDisplay()
   */
  public void clearErrorMessage()
  {
    if (mErrorDisplay != null) {
      mErrorDisplay.clearDisplay();
    }
  }

  /**
   * Requests the keyboard focus for this cell and displays an error message.
   * This message is typically called when an external commit attempt has
   * detected an error at a higher level. It requests to set the focus to the
   * cell to allow the user to make corrections and afterwards displays the
   * given message in the cell's error display.
   */
  public void requestFocusWithErrorMessage(final String msg)
  {
    requestFocusInWindow();
    if (mErrorDisplay != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          mErrorDisplay.displayError(msg);
        }
      });
    }
  }


  //#########################################################################
  //# Inner Class ValidatingCellFormatter
  private class ValidatingCellFormatter extends DefaultFormatter
  {
    //#######################################################################
    //# Constructors
    private ValidatingCellFormatter()
    {
      setCommitsOnValidEdit(false);
    }

    //#######################################################################
    //# Overrides for class javax.swing.text.DefaultFormatter
    @Override
    public Object stringToValue(final String text)
      throws ParseException
    {
      try {
        final Object value = mInputHandler.parse(text);
        clearErrorMessage();
        return value;
      } catch (final java.text.ParseException exception) {
        final String msg = exception.getMessage();
        setErrorMessage(msg);
        final Object oldValue = getValue();
        if (oldValue != null) {
          final String oldText = valueToString(oldValue);
          if (text.equals(oldText)) {
            return oldValue;
          }
        }
        throw exception;
      }
    }

    @Override
    public String valueToString(final Object value)
    {
      return mInputHandler.format(value);
    }

    @Override
    protected DocumentFilter getDocumentFilter()
    {
      return mInputHandler.getDocumentFilter();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -4485842434559114931L;
  }


  //#########################################################################
  //# Inner Class ValidatingCellVerifier
  private class ValidatingCellVerifier
    extends InputVerifier
  {
    //#######################################################################
    //# Overrides for class javax.swing.InputVerifier
    @Override
    public boolean verify(final JComponent input)
    {
      try {
        final JFormattedTextField textfield = (JFormattedTextField) input;
        final JFormattedTextField.AbstractFormatter formatter =
          textfield.getFormatter();
        final String text = textfield.getText();
        formatter.stringToValue(text);
        return true;
      } catch (final ParseException exception) {
        return false;
      }
    }

    @Override
    public boolean shouldYieldFocus(final JComponent input)
    {
      final JFormattedTextField textfield = (JFormattedTextField) input;
      try {
        textfield.commitEdit();
        return true;
      } catch (final ParseException exception) {
        final int pos = exception.getErrorOffset();
        textfield.setCaretPosition(pos);
        return false;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final FormattedInputHandler<? extends T> mInputHandler;
  private final ValidatingCellVerifier mVerifier;
  private ErrorDisplay mErrorDisplay;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 22908014877081408L;

}
