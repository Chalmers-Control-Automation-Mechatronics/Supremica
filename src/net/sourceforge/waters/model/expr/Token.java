//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   Token
//###########################################################################
//# $Id$
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
