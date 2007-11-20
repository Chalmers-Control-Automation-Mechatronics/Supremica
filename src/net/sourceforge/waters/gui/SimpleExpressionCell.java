//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleExpressionCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.10 2007-11-20 03:37:35 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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
  }


  //#########################################################################
  //# Overrides for Base Class JFormattedTextField
  public void setValue(final Object value)
  {
    super.setValue(value);
    mAllowNull = (value == null);
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
  //# Local Class DefaultInputParser
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
      return this;
    }


    //#######################################################################
    //# Overrides for class javax.swing.DocumentFilter
    public void insertString(final DocumentFilter.FilterBypass bypass,
                             final int offset,
                             final String text,
                             final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text);
      if (filtered != null) {
        super.insertString(bypass, offset, filtered, attribs);
      }
    }

    public void replace(final DocumentFilter.FilterBypass bypass,
                        final int offset,
                        final int length,
                        final String text,
                        final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text);
      if (filtered != null) {
        super.replace(bypass, offset, length, filtered, attribs);
      }
    }


    //#######################################################################
    //# Auxiliary Methods
    private String filter(final String text)
    {
      if (text == null) {
        return null;
      } else {
        final int len = text.length();
        final StringBuffer buffer = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
          final char ch = text.charAt(i);
          if (mParser.isExpressionCharacter(ch)) {
            buffer.append(ch);
          }
        }
        if (buffer.length() == 0) {
          return null;
        } else {
          return buffer.toString();
        }
      }
    }


    //#######################################################################
    //# Data Members
    private final ExpressionParser mParser;
    private final int mTypeMask;

  }


  //#########################################################################
  //# Local Class SimpleExpressionFormatter
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
  //# Local Class SimpleExpressionVerifier
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

}
