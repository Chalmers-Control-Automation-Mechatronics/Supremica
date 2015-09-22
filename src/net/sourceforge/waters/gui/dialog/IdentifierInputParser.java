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

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;


/**
 * An input parser to support identifiers ({@link IdentifierProxy}), for use
 * with a {@link javax.swing.JFormattedTextField JFormattedTextField}. This
 * parser allows entry of arbitrary structured identifiers. No additional
 * checks are performed.
 *
 * @see SimpleExpressionCell
 * @author Robi Malik
 */

public class IdentifierInputParser
  implements FormattedInputParser
{

  //#########################################################################
  //# Constructor
  public IdentifierInputParser(final IdentifierProxy oldIdent,
                               final ExpressionParser parser)
  {
    mOldIdentifier = oldIdent;
    mOldName = oldIdent.toString();
    mEquality = new ModuleEqualityVisitor(true);
    mExpressionParser = parser;
    mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
  }


  //#########################################################################
  //# Simple Access
  public IdentifierProxy getOldIdentifier()
  {
    return mOldIdentifier;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  @Override
  public IdentifierProxy parse(final String text)
    throws ParseException
  {
    if (text.equals(mOldName)) {
      return mOldIdentifier;
    }
    final IdentifierProxy ident = mExpressionParser.parseIdentifier(text);
    if (mEquality.equals(mOldIdentifier, ident)) {
      return mOldIdentifier;
    }
    return ident;
  }

  @Override
  public DocumentFilter getDocumentFilter()
  {
    return mDocumentFilter;
  }


  //#######################################################################
  //# Data Members
  private final IdentifierProxy mOldIdentifier;
  private final String mOldName;
  private final ModuleEqualityVisitor mEquality;
  private final ExpressionParser mExpressionParser;
  private final DocumentFilter mDocumentFilter;

}








