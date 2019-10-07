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

import java.text.ParseException;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ExpressionScanner;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An input handler to support identifiers ({@link IdentifierProxy}), for use
 * with a {@link SimpleExpressionInputCell}. This handler allows entry of arbitrary
 * structured identifiers. No additional checks are performed.
 *
 * @see SimpleExpressionInputCell
 * @author Robi Malik
 */

public class IdentifierInputHandler
  extends AbstractSimpleExpressionInputHandler<IdentifierProxy>
{

  //#########################################################################
  //# Constructor
  public IdentifierInputHandler(final IdentifierProxy oldIdent,
                                final ExpressionParser parser,
                                final boolean nullAllowed)
  {
    super(Operator.TYPE_NAME, parser, nullAllowed);
    mOldIdentifier = oldIdent;
    mOldName = oldIdent == null ? "" : oldIdent.toString();
    mEquality = new ModuleEqualityVisitor(true);
  }


  //#########################################################################
  //# Simple Access
  public IdentifierProxy getOldIdentifier()
  {
    return mOldIdentifier;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.gui.FormattedInputParser<IdentifierProxy>
  @Override
  public IdentifierProxy parse(final String text)
    throws ParseException
  {
    if (text.length() == 0) {
      if (isNullAllowed()) {
        return null;
      } else {
        throw new ParseException(NO_IDENT, 0);
      }
    } else if (text.equals(mOldName)) {
      return mOldIdentifier;
    } else {
      try {
        final SimpleExpressionProxy expr = callParser(text);
        if (!(expr instanceof IdentifierProxy)) {
          throw new ParseException(NO_IDENT, 0);
        }
        final IdentifierProxy ident = (IdentifierProxy) expr;
        if (mEquality.equals(ident, mOldIdentifier)) {
          return mOldIdentifier;
        }
        return ident;
      } catch (final ParseException exception) {
        if (hasOnlyIdentifierCharacters(text)) {
          throw new ParseException(NO_IDENT, 0);
        } else {
          throw exception;
        }
      }
    }
  }


  //#######################################################################
  //# Auxiliary Methods
  private static boolean hasOnlyIdentifierCharacters(final String text)
  {
    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (!ExpressionScanner.isWhitespace(ch) &&
          !ExpressionScanner.isIdentifierCharacter(ch)) {
        return false;
      }
    }
    return true;
  }


  //#######################################################################
  //# Data Members
  private final IdentifierProxy mOldIdentifier;
  private final String mOldName;
  private final ModuleEqualityVisitor mEquality;


  //#######################################################################
  //# Class Constants
  private static final String NO_IDENT = "Please enter an identifier name.";

}
