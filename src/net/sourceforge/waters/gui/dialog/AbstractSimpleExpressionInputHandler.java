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

package net.sourceforge.waters.gui.dialog;

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public abstract class AbstractSimpleExpressionInputHandler
  <E extends SimpleExpressionProxy>
  implements FormattedInputHandler<E>
{

  //#########################################################################
  //# Constructors
  protected AbstractSimpleExpressionInputHandler(final int mask,
                                                 final ExpressionParser parser,
                                                 final boolean nullAllowed)
  {
    mParser = parser;
    mTypeMask = mask;
    mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
    mNullAllowed = nullAllowed;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputHandler<E>
  @Override
  public String format(final Object value)
  {
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public DocumentFilter getDocumentFilter()
  {
    return mDocumentFilter;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected SimpleExpressionProxy callParser(final String text)
    throws java.text.ParseException
  {
    if (text.length() != 0) {
      try {
        return mParser.parse(text, mTypeMask);
      } catch (final ParseException exception) {
        throw exception.getJavaException();
      }
    } else if (mNullAllowed) {
      return null;
    } else {
      throw new java.text.ParseException("Please enter an expression.", 0);
    }
  }

  protected boolean isNullAllowed()
  {
    return mNullAllowed;
  }


  //#########################################################################
  //# Data Members
  private final ExpressionParser mParser;
  private final int mTypeMask;
  private final DocumentFilter mDocumentFilter;
  private final boolean mNullAllowed;

}
