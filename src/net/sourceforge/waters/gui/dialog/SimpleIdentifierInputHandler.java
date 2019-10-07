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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ExpressionScanner;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * An input handler to support simple identifiers ({@link
 * SimpleIdentifierProxy}), for use with a {@link SimpleExpressionInputCell}.
 *
 * @see SimpleExpressionInputCell
 * @author Robi Malik
 */

public class SimpleIdentifierInputHandler
  extends DocumentFilter
  implements FormattedInputHandler<SimpleIdentifierProxy>
{

  //#########################################################################
  //# Constructor
  public SimpleIdentifierInputHandler(final SimpleIdentifierProxy oldIdent,
                                      final ExpressionParser parser,
                                      final boolean nullAllowed)
  {
    mOldName = oldIdent.getName();
    mOldIdentifier = oldIdent;
    mExpressionParser = parser;
    mNullAllowed = nullAllowed;
  }

  public SimpleIdentifierInputHandler(final String oldName,
                                      final ExpressionParser parser,
                                      final boolean nullAllowed)
  {
    if (oldName.length() > 0) {
      mOldName = oldName;
      mOldIdentifier = new SimpleIdentifierSubject(oldName);
    } else if (nullAllowed) {
      mOldName = oldName;
      mOldIdentifier = null;
    } else {
      mOldName = null;
      mOldIdentifier = null;
    }
    mExpressionParser = parser;
    mNullAllowed = nullAllowed;
  }


  //#########################################################################
  //# Simple Access
  public String getOldName()
  {
    return mOldName;
  }

  public SimpleIdentifierProxy getOldIdentifier()
  {
    return mOldIdentifier;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.gui.FormattedInputHandler<SimpleIdentifierProxy>
  @Override
  public String format(final Object value)
  {
    if (value == null) {
      return "";
    } else {
      final SimpleIdentifierProxy ident = (SimpleIdentifierProxy) value;
      return ident.getName();
    }
  }

  @Override
  public SimpleIdentifierProxy parse(final String text)
    throws java.text.ParseException
  {
    if (text.equals(mOldName)) {
      return mOldIdentifier;
    } else if (text.length() != 0) {
      try {
        return mExpressionParser.parseSimpleIdentifier(text);
      } catch (final ParseException exception) {
        throw new java.text.ParseException(NO_IDENT, 0);
      }
    } else if (mNullAllowed) {
      return null;
    } else {
      throw new java.text.ParseException(NO_IDENT, 0);
    }
  }

  @Override
  public DocumentFilter getDocumentFilter()
  {
    return this;
  }


  //#########################################################################
  //# Overrides for class javax.swing.DocumentFilter
  @Override
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

  @Override
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
  protected String filter(final String text)
  {
    if (text == null) {
      return null;
    } else {
      final int len = text.length();
      final StringBuilder buffer = new StringBuilder(len);
      for (int i = 0; i < len; i++) {
        final char ch = text.charAt(i);
        if (ExpressionScanner.isIdentifierCharacter(ch)) {
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
  private final boolean mNullAllowed;


  //#######################################################################
  //# Class Constants
  private static final String NO_IDENT = "Please enter an identifier name.";

}
