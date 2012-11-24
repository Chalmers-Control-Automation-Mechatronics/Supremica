//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   SimpleIdentifierInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * @author Robi Malik
 */

public class SimpleIdentifierInputParser
  extends DocumentFilter
  implements FormattedInputParser
{

  //#########################################################################
  //# Constructor
  public SimpleIdentifierInputParser(final SimpleIdentifierProxy oldident,
                                     final ExpressionParser parser)
  {
    mOldName = oldident.getName();
    mOldIdentifier = oldident;
    mExpressionParser = parser;
  }

  public SimpleIdentifierInputParser(final String oldname,
                                     final ExpressionParser parser)
  {
    mOldName = oldname;
    mOldIdentifier = new SimpleIdentifierSubject(oldname);
    mExpressionParser = parser;
  }


  //#########################################################################
  //# Simple Access
  public String getOldName()
  {
    return mOldName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  public SimpleIdentifierProxy parse(final String text)
    throws ParseException
  {
    if (!text.equals(mOldName)) {
      final SimpleIdentifierProxy result =
        mExpressionParser.parseSimpleIdentifier(text);
      return result;
    } else {
      return mOldIdentifier;
    }
  }

  public DocumentFilter getDocumentFilter()
  {
    return this;
  }


  //#########################################################################
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


  //#########################################################################
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
        if (mExpressionParser.isIdentifierCharacter(ch)) {
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


  //#########################################################################
  //# Data Members
  private final String mOldName;
  private final SimpleIdentifierProxy mOldIdentifier;
  private final ExpressionParser mExpressionParser;

}