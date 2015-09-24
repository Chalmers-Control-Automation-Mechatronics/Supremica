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

package net.sourceforge.waters.model.expr;


/**
 * A token passed from the scanner to the parser.
 *
 * @see ExpressionScanner
 * @see ExpressionParser
 *
 * @author Robi Malik
 */
class Token {

  //#########################################################################
  //# Constructors
  Token(final Type type, final String text, final int pos)
  {
    mType = type;
    mText = text;
    mPosition = pos;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the type of this token.
   * The token type identifies this token as a integer constant,
   * operator, parentheses, etc.
   * @return One of {@link Type#END}, {@link Type#OPENBR},
   *         {@link Type#OPENSQ}, {@link Type#CLOSEBR}, {@link Type#CLOSESQ},
   *         {@link Type#COMMA}, {@link Type#OPERATOR}, {@link Type#NUMBER},
   *         {@link Type#SYMBOL}, {@link Type#FUNCTION}.
   */
  Type getType()
  {
    return mType;
  }

  /**
   * Gets the text of this token.
   * @return The substring of the parsed expression that represents the token.
   */
  String getText()
  {
    return mText;
  }

  /**
   * Gets the start position of this token.
   * @return The position of the first character of this token
   *         in the parsed expression string. The first character
   *         is a position&nbsp;<CODE>0</CODE>.
   */
  int getPosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Overrides for base class java.lang.Object
  @Override
  public String toString()
  {
    return
      "{Token '" + mText + "'; type=" + mType + "; pos=" + mPosition + "}";
  }


  //#########################################################################
  //# Specialised Access Methods
  int getIntValue()
  {
    return Integer.parseInt(mText);
  }

  UnaryOperator getUnaryOperator()
  {
    return null;
  }

  BinaryOperator getBinaryOperator()
  {
    return null;
  }

  BuiltInFunction getBuiltInFunction()
  {
    return null;
  }

  Operator getPostfixOperator()
  {
    final UnaryOperator unop = getUnaryOperator();
    if (unop != null && !unop.isPrefix()) {
      return unop;
    } else {
      return getBinaryOperator();
    }
  }


  //#########################################################################
  //# Inner Enumeration Type
  static enum Type {
    END,
    OPENBR,
    OPENSQ,
    CLOSEBR,
    CLOSESQ,
    COMMA,
    OPERATOR,
    NUMBER,
    SYMBOL,
    FUNCTION
  }


  //#########################################################################
  //# Data Members
  private final Type mType;
  private final String mText;
  private final int mPosition;

}
