//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleExpressionCell
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A text field to enter simple expressions.</P>
 *
 * <P>A SimpleExpressionCell allows the user to input text representing
 * a Waters simple expression ({@link SimpleExpressionProxy}) of a specific
 * type.</P>
 *
 * <P>This class provides support for use inside a table or list. An
 * {@link ExpressionParser} is used to validate the input, and error messages
 * from the parser can be sent to a configurable destination. Attempts are
 * made to prevent the entry of characters that are not allowed in an
 * expression of the expected type.</P>
 *
 * @author Robi Malik
 */

public class SimpleExpressionCell
  extends JFormattedTextField
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a cell to enter expressions of an arbitrary type.
   * @param  parser    The expression parser to be used for input validation.
   *                   It can be obtained from the
   *                   {@link ModuleWindowInterface}.
   */
  public SimpleExpressionCell(final ExpressionParser parser)
  {
    this(Operator.TYPE_ANY, parser);
  }

  /**
   * Creates a cell to enter expressions of a specific type.
   * @param  mask      Type mask of supported types.
   *                   It can be defined using the constants in
   *                   {@link Operator}.
   * @param  parser    The expression parser to be used for input validation.
   *                   It can be obtained from the
   *                   {@link ModuleWindowInterface}.
   */
  public SimpleExpressionCell(final int mask,
                              final ExpressionParser parser)
  {
    this(new DefaultInputParser(mask, parser));
  }

  /**
   * Creates a cell to enter expressions of an arbitrary type.
   * @param  expr      The initial value for the text field.
   * @param  parser    The expression parser to be used for input validation.
   *                   It can be obtained from the
   *                   {@link ModuleWindowInterface}.
   */
  public SimpleExpressionCell(final SimpleExpressionProxy expr,
                              final ExpressionParser parser)
  {
    this(expr, Operator.TYPE_ANY, parser);
  }

  /**
   * Creates a cell to enter expressions of a specific type.
   * @param  expr      The initial value for the text field.
   * @param  mask      Type mask of supported types.
   *                   It can be defined using the constants in
   *                   {@link Operator}.
   * @param  parser    The expression parser to be used for input validation.
   *                   It can be obtained from the
   *                   {@link ModuleWindowInterface}.
   */
  public SimpleExpressionCell(final SimpleExpressionProxy expr,
                              final int mask,
                              final ExpressionParser parser)
  {
    this(expr, new DefaultInputParser(mask, parser));
  }

  /**
   * Creates a customised simple expression cell.
   * @param  parser    An input parser to validate the text input.
   *                   By specifying a customised input parser, the user
   *                   possible to implement type checking beyond the type
   *                   masks.
   * @see    SimpleIdentifierInputParser
   */
  public SimpleExpressionCell(final FormattedInputParser parser)
  {
    this(null, parser);
  }

  /**
   * Creates a customised simple expression cell.
   * @param  expr      The initial value for the text field.
   * @param  parser    An input parser to validate the text input.
   *                   By specifying a customised input parser, the user
   *                   possible to implement type checking beyond the type
   *                   masks.
   * @see    SimpleIdentifierInputParser
   */
  public SimpleExpressionCell(final SimpleExpressionProxy expr,
                              final FormattedInputParser parser)
  {
    mParser = parser;
    mVerifier = new SimpleExpressionVerifier();
    final JFormattedTextField.AbstractFormatter formatter =
      new SimpleExpressionFormatter();
    final DefaultFormatterFactory factory =
      new DefaultFormatterFactory(formatter);
    setFormatterFactory(factory);
    setInputVerifier(mVerifier);
    setValue(expr);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the cell's currently used input parser.
   * The input parser is a wrapper around the cell's {@link ExpressionParser}
   * to perform customised type checking in addition to parsing.
   */
  public FormattedInputParser getFormattedInputParser()
  {
    return mParser;
  }

  /**
   * Sets whether the cell allows empty input.
   * If allowed, an empty text input is accepted and causes a
   * <CODE>null</CODE> expression to be returned. If not allowed
   * (the default), attempting to commit the cell without input
   * causes an error to be reported.
   */
  public void setAllowNull(final boolean allow)
  {
    mAllowNull = allow;
  }


  //#########################################################################
  //# Input Verification
  /**
   * Checks whether the current cell input is valid.
   * This method calls the cell's input parser to perform input validation.
   * @return <CODE>true</CODE> if the cell contents have been found to be
   *         valid and keyboard focus can be transferred to another
   *         component.
   */
  public boolean shouldYieldFocus()
  {
    return mVerifier.shouldYieldFocus(this);
  }

  /**
   * Restores the cell to the content it had when it was last committed.
   */
  public void revert()
  {
    final Object oldvalue = getValue();
    setValue(oldvalue);
  }


  //#########################################################################
  //# Actions
  /**
   * Sets a keyboard binding for the &lt;ENTER&gt; key.
   */
  public void addEnterAction(final Action action)
  {
    addKeyAction(KeyEvent.VK_ENTER, action);
  }

  /**
   * Sets a keyboard binding for the &lt;ESCAPE&gt; key.
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


  //#########################################################################
  //# Inner Class DefaultInputParser
  private static class DefaultInputParser
    extends DocumentFilter
    implements FormattedInputParser
  {

    //#######################################################################
    //# Constructors
    private DefaultInputParser(final int mask, final ExpressionParser parser)
    {
      mParser = parser;
      mTypeMask = mask;
      mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    public SimpleExpressionProxy parse(final String text)
      throws ParseException
    {
      return mParser.parse(text, mTypeMask);
    }

    public DocumentFilter getDocumentFilter()
    {
      return mDocumentFilter;
    }

    //#######################################################################
    //# Data Members
    private final ExpressionParser mParser;
    private final int mTypeMask;
    private final DocumentFilter mDocumentFilter;

  }


  //#########################################################################
  //# Inner Class SimpleExpressionFormatter
  private class SimpleExpressionFormatter extends DefaultFormatter
  {

    //#######################################################################
    //# Constructors
    private SimpleExpressionFormatter()
    {
      setCommitsOnValidEdit(false);
    }


    //#######################################################################
    //# Overrides for class javax.swing.text.DefaultFormatter
    public Object stringToValue(final String text)
      throws java.text.ParseException
    {
      if (text.length() != 0) {
        try {
          final Object value = mParser.parse(text);
          clearErrorMessage();
          return value;
        } catch (final ParseException exception) {
          final String msg = exception.getMessage();
          setErrorMessage(msg);
          final SimpleExpressionProxy oldvalue =
            (SimpleExpressionProxy) getValue();
          final String oldtext = valueToString(oldvalue);
          if (text.equals(oldtext)) {
            return oldvalue.clone();
          } else {
            throw exception.getJavaException();
          }
        }
      } else if (mAllowNull) {
        return null;
      } else {
        final String msg = "Empty input!";
        setErrorMessage(msg);
        throw new java.text.ParseException(msg, 0);
      }
    }

    public String valueToString(final Object value)
    {
      if (value == null) {
        return "";
      } else {
        return value.toString();
      }
    }

    protected DocumentFilter getDocumentFilter()
    {
      return mParser.getDocumentFilter();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Inner Class SimpleExpressionVerifier
  private class SimpleExpressionVerifier
    extends InputVerifier
  {

    //#######################################################################
    //# Overrides for class javax.swing.InputVerifier
    public boolean verify(final JComponent input)
    {
      try {
        final JFormattedTextField textfield = (JFormattedTextField) input;
        final JFormattedTextField.AbstractFormatter formatter =
          textfield.getFormatter();
        final String text = textfield.getText();
        formatter.stringToValue(text);
        return true;
      } catch (final java.text.ParseException exception) {
        return false;
      }
    }

    public boolean shouldYieldFocus(final JComponent input)
    {
      final SimpleExpressionCell textfield = (SimpleExpressionCell) input;
      try {
        textfield.commitEdit();
        return true;
      } catch (final java.text.ParseException exception) {
        final int pos = exception.getErrorOffset();
        textfield.setCaretPosition(pos);
        return false;
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final FormattedInputParser mParser;
  private final SimpleExpressionVerifier mVerifier;

  private boolean mAllowNull;
  private ErrorDisplay mErrorDisplay;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
