//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleExpressionCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.12 2008-03-13 01:30:11 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
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



public class SimpleExpressionCell
  extends JFormattedTextField
{

  //#########################################################################
  //# Constructors
  public SimpleExpressionCell(final ExpressionParser parser)
  {
    this(Operator.TYPE_ANY, parser);
  }

  public SimpleExpressionCell(final int mask,
                              final ExpressionParser parser)
  {
    this(new DefaultInputParser(mask, parser));
  }

  public SimpleExpressionCell(final Object value,
                              final ExpressionParser parser)
  {
    this(value, Operator.TYPE_ANY, parser);
  }

  public SimpleExpressionCell(final Object value,
                              final int mask,
                              final ExpressionParser parser)
  {
    this(value, new DefaultInputParser(mask, parser));
  }

  public SimpleExpressionCell(final FormattedInputParser parser)
  {
    this(null, parser);
  }

  public SimpleExpressionCell(final Object value,
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
    setValue(value);

    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final InputMap imap = getInputMap();
    final ActionMap amap = getActionMap();
    final Object name = imap.get(stroke);
    mTextFieldEscapeAction = amap.get(name);
  }


  //#########################################################################
  //# Simple Access
  public FormattedInputParser getFormattedInputParser()
  {
    return mParser;
  }

  public void setAllowNull(final boolean allow)
  {
    mAllowNull = allow;
  }


  //#########################################################################
  //# Overrides for Base Class javax.swing.JFormattedTextField
  public void setValue(final Object value)
  {
    super.setValue(value);
    mAllowNull = (value == null);
  }


  //#########################################################################
  //# Overrides for Base Class javax.swing.JComponent
  public void setToolTipText(final String text)
  {
    super.setToolTipText(text);
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final InputMap imap = getInputMap();
    final ActionMap amap = getActionMap();
    final Object name = imap.get(stroke);
    final Action action = amap.get(name);
    if (text == null) {
      if (action != mTextFieldEscapeAction) {
        amap.put(name, mTextFieldEscapeAction);
      }
    } else {
      if (!(action instanceof CombinedEscapeAction)) {
        final Action combined =
          new CombinedEscapeAction(mTextFieldEscapeAction, action);
        amap.put(name, combined);
      }
    }
  }


  //#########################################################################
  //# Input Verification
  public boolean shouldYieldFocus()
  {
    return mVerifier.shouldYieldFocus(this);
  }

  public void revert()
  {
    final Object oldvalue = getValue();
    setValue(oldvalue);
  }


  //#########################################################################
  //# Error Display
  public ErrorDisplay getErrorDisplay()
  {
    return mErrorDisplay;
  }

  public void setErrorDisplay(final ErrorDisplay display)
  {
    mErrorDisplay = display;
  }


  //#########################################################################
  //# Auxiliary Methods
  public void setErrorMessage(final String msg)
  {
    if (mErrorDisplay != null) {
      mErrorDisplay.displayError(msg);
    }
  }

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
          throw exception.getJavaException();
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
  //# Inner Class CombinedEscapeAction
  /**
   * This is an attempt to fix a bug in Swing. When a text field has a
   * tooltip set, any ESCAPE key events are consumed by the tooltip,
   * even if the tooltip is not visible. This wrapper class sends the
   * action to the tooltip if it is visible, otherwise uses the old
   * textfield action.
   */
  private static class CombinedEscapeAction implements Action
  {

    //#######################################################################
    //# Constructor
    private CombinedEscapeAction(final Action text, final Action tooltip)
    {
      mTextFieldAction = text;
      mToolTipAction = tooltip;
    }

    //#######################################################################
    //# Interface javax.swing.Action
    public void addPropertyChangeListener
      (final PropertyChangeListener listener)
    {
      mTextFieldAction.addPropertyChangeListener(listener);
      mToolTipAction.addPropertyChangeListener(listener);
    }

    public Object getValue(final String key)
    {
      return mTextFieldAction.getValue(key);
    }

    public boolean isEnabled()
    {
      return mTextFieldAction.isEnabled() || mToolTipAction.isEnabled();
    }

    public void putValue(final String key, final Object value)
    {
      mTextFieldAction.putValue(key, value);
    }

    public void removePropertyChangeListener
      (final PropertyChangeListener listener)
    {
      mTextFieldAction.removePropertyChangeListener(listener);
      mToolTipAction.removePropertyChangeListener(listener);
    }

    public void setEnabled(final boolean enabled)
    {
      mTextFieldAction.setEnabled(enabled);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      if (mToolTipAction.isEnabled()) {
        mToolTipAction.actionPerformed(event);
      } else if (mTextFieldAction.isEnabled()) {
        mTextFieldAction.actionPerformed(event);
      }
    }

    //#######################################################################
    //# Data Members
    private final Action mTextFieldAction;
    private final Action mToolTipAction;

  }


  //#########################################################################
  //# Data Members
  private final FormattedInputParser mParser;
  private final SimpleExpressionVerifier mVerifier;
  private final Action mTextFieldEscapeAction;

  private boolean mAllowNull;
  private ErrorDisplay mErrorDisplay;

}
