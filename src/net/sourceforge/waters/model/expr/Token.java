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
  Token(final int type, final String text, final int pos)
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
   * @return One of {@link #END}, {@link #OPENBR}, {@link #OPENSQ},
   *         {@link #CLOSEBR}, {@link #CLOSESQ}, {@link #COMMA},
   *         {@link #OPERATOR}, {@link #NUMBER}, {@link #SYMBOL}.
   */
  int getType()
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
  //# Token Type Constants
  static final int END = 0;
  static final int OPENBR = 1;
  static final int OPENSQ = 2;
  static final int CLOSEBR = 3;
  static final int CLOSESQ = 4;
  static final int COMMA = 5;
  static final int OPERATOR = 6;
  static final int NUMBER = 7;
  static final int SYMBOL = 8;


  //#########################################################################
  //# Data Members
  private final int mType;
  private final String mText;
  private final int mPosition;

}
