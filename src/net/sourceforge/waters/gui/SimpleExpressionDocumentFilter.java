//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleExpressionDocumentFilter
//###########################################################################
//# $Id: SimpleExpressionDocumentFilter.java,v 1.2 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;


class SimpleExpressionDocumentFilter
  extends DocumentFilter
{

  //#########################################################################
  //# Constructor
  SimpleExpressionDocumentFilter(final ExpressionParser parser)
  {
    mExpressionParser = parser;
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
  String filter(final String text)
  {
    if (text == null) {
      return null;
    } else {
      final int len = text.length();
      final StringBuffer buffer = new StringBuffer(len);
      for (int i = 0; i < len; i++) {
	final char ch = text.charAt(i);
	if (isAllowedCharacter(ch)) {
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

  boolean isAllowedCharacter(final char ch)
  {
    return mExpressionParser.isExpressionCharacter(ch);
  }


  //#######################################################################
  //# Data Members
  private final ExpressionParser mExpressionParser;

}
