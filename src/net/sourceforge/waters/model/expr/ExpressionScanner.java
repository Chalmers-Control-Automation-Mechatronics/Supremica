//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>The expression scanner.
 * The expression scanner analyses a stream of characters and converts
 * it to tokens for the parser.</P>
 *
 * <P>Supported tokens:</P>
 * <DL>
 * <DT>Separators</DT>
 * <DD>The special separator characters produce tokens of their own:
 * <CODE>'('</CODE> ({@link Token.Type#OPENBR}),
 * <CODE>'['</CODE> ({@link Token.Type#OPENSQ}),
 * <CODE>')'</CODE> ({@link Token.Type#CLOSEBR}),
 * <CODE>']'</CODE> ({@link Token.Type#CLOSESQ}),
 * <CODE>','</CODE> ({@link Token.Type#COMMA}).</DD>
 * <DT>Operators (<CODE>{@link Token.Type#OPERATOR}</CODE>)</DT>
 * <DD>Operator tokens such as <CODE>+</CODE> or <CODE>..</CODE> as
 * defined by a {@link OperatorTable}.</DD>
 * <DT>Numbers (<CODE>{@link Token.Type#NUMBER}</CODE>)</DT>
 * <DD>Any sequence of digits, optionally preceded by a minus sign,
 * forms a number token.</DD>
 * <DT>Symbols (<CODE>{@link Token.Type#SYMBOL}</CODE>)</DT>
 * <DD>Any character sequence matching the regular expression
 * <CODE>[a-zA-Z:_][a-zA-Z0-9:_]*</CODE> forms a symbol token. In addition, any
 * sequence of characters enclosed in curled braces <CODE>{...}</CODE> can
 * form a part of a symbol. Possible symbols are: <CODE>x25</CODE>,
 * <CODE>:accepting</CODE>, <CODE>a{1+7}bc{- +}</CODE>.
 * </DL>
 *
 * @author Robi Malik
 */

public class ExpressionScanner {

  //#########################################################################
  //# Constructors
  ExpressionScanner(final OperatorTable optable)
  {
    this(optable, null);
  }

  ExpressionScanner(final OperatorTable optable, final Reader reader)
  {
    mOperatorTable = optable;
    mTokenText = new StringBuilder();
    setInputStream(reader);
  }


  //#########################################################################
  //# Setting the Input Stream
  void setInputStream(final Reader reader)
  {
    mInputStream = reader;
    mNextToken = null;
    mPutbackCharacter = -2;
    mPosition = -1;
  }


  //#########################################################################
  //# High-Level Methods for Scanning
  Token next()
    throws IOException, ParseException
  {
    return next(true);
  }

  Token next(final boolean allowNeg)
    throws IOException, ParseException
  {
    final Token token = peek(allowNeg);
    mNextToken = null;
    return token;
  }

  Token peek()
    throws IOException, ParseException
  {
    return peek(true);
  }

  Token peek(final boolean allowNeg)
    throws IOException, ParseException
  {
    if (mNextToken == null) {
      storeNextToken(allowNeg);
    }
    return mNextToken;
  }

  int getPosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Character Classes
  /**
   * Checks whether a character is whitespace.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as
   *         whitespace in a Waters expression.
   */
  public static boolean isWhitespace(final int ch)
  {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }

  /**
   * Checks whether a character represents a digit.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as
   *         a digit in a Waters expression.
   */
  public static boolean isDigit(final int ch)
  {
    return ch >= '0' && ch <= '9';
  }

  /**
   * Checks whether a character represents an identifier start.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as the
   *         first character of a Waters identifier.
   */
  public static boolean isIdentifierStart(final int ch)
  {
    return
      ch >= 'a' && ch <= 'z' ||
      ch >= 'A' && ch <= 'Z' ||
      ch == '_' ||
      ch == ':';
  }

  /**
   * Checks whether a character represents an identifier character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used in
   *         a Waters identifier.
   */
  public static boolean isIdentifierCharacter(final int ch)
  {
    return isIdentifierStart(ch) || isDigit(ch);
  }

  /**
   * Checks whether a character represents an operator character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used in
   *         a Waters operator.
   */
  boolean isOperatorCharacter(final int ch)
  {
    return ch >= 0 && mOperatorTable.isOperatorCharacter((char) ch);
  }

  /**
   * Checks whether a character represents a separator character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as a
   *         special, or separator, character in a Waters expression.
   */
  boolean isSeparatorCharacter(final int ch)
  {
    final Integer character = new Integer(ch);
    return SEPARATORS.containsKey(character);

  }

  /**
   * Checks whether a character represents the escape-start character
   * <CODE>'{'</CODE>.
   */
  public static boolean isEscapeStartCharachter(final int ch)
  {
    return ch == '{';
  }

  /**
   * Checks whether a character represents the escape-end character
   * <CODE>'}'</CODE>.
   */
  public static boolean isEscapeEndCharacter(final int ch)
  {
    return ch == '}';
  }

  /**
   * Checks whether a character represents the function-key character
   * <CODE>'\'</CODE>.
   */
  boolean isFunctionKeyCharacter(final int ch)
  {
    return ch == mOperatorTable.getFunctionKeyCharacter();
  }

  /**
   * Checks whether a character is an expression character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can occur
   *         somewhere in a syntactically correct expression.
   */
  boolean isExpressionCharacter(final int ch)
  {
    return
      isWhitespace(ch) ||
      isDigit(ch) ||
      isIdentifierCharacter(ch) ||
      isOperatorCharacter(ch) ||
      isSeparatorCharacter(ch) ||
      isEscapeStartCharachter(ch) ||
      isEscapeEndCharacter(ch) ||
      isFunctionKeyCharacter(ch);
  }

  /**
   * Checks whether a string qualifies as a Waters identifier. Strings that
   * pass this test can be used as the name of a valid {@link
   * net.sourceforge.waters.model.module.SimpleIdentifierProxy
   * SimpleIdentifierProxy}, or as names of any Waters elements that are
   * identified by strings rather than identifiers.
   * Identifiers must start with a letter, underscore, colon, or escaped
   * group, which can be followed by more letter, digits, underscores,
   * colons, or escaped groups.
   * @param  word        The string to be examined.
   * @return <CODE>true</CODE> if the given string is a valid identifier.
   */
  public static boolean isWatersIdentifier(final String word)
  {
    final int len = word.length();
    if (len == 0) {
      return false;
    }
    char ch = word.charAt(0);
    if (isIdentifierStart(ch)) {
      int index;
      for (index = 1; index < len; index++) {
        ch = word.charAt(index);
        if (isEscapeStartCharachter(ch)) {
          break;
        } else if (!isIdentifierCharacter(word.charAt(index))) {
          return false;
        }
      }
      if (index == len) {
        return true;
      }
    } else if (!isEscapeStartCharachter(ch)) {
      return false;
    }
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final StringReader reader = new StringReader(word);
    try {
      final ExpressionScanner scanner = new ExpressionScanner(optable, reader);
      final Token token = scanner.next();
      if (token.getType() != Token.Type.SYMBOL) {
        return false;
      } else if (scanner.getNextCharacter() >= 0) {
        return false;
      } else {
        return true;
      }
    } catch (final ParseException exception) {
      return false;
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * Checks whether the given simple identifier has a syntactically
   * correct name. Identifiers must pass the test {@link
   * #isWatersIdentifier(String) isWatersIdentifier()}.
   * @throws TypeMismatchException to indicate the name is invalid.
   */
  public static void checkWatersIdentifier(final SimpleIdentifierProxy ident)
    throws TypeMismatchException
  {
    final String name = ident.getName();
    if (!isWatersIdentifier(name)) {
      throw new TypeMismatchException(ident, "IDENTIFIER");
    }
  }


  //#########################################################################
  //# Auxiliary Methods for Scanning
  private void storeNextToken(final boolean allowNeg)
    throws IOException, ParseException
  {
    try {
      int ch;
      do {
        ch = getNextCharacter();
      } while (isWhitespace(ch));
      mTokenStart = mPosition;
      mTokenText.append((char) ch);
      if (isDigit(ch)) {
        storeNumberToken();
      } else if (isIdentifierStart(ch) || isEscapeStartCharachter(ch)) {
        storeSymbolToken(ch);
      } else if (allowNeg && ch == '-') {
        ch = getNextCharacter();
        if (isDigit(ch)) {
          mTokenText.append((char) ch);
          storeNumberToken();
        } else {
          putback(ch);
          storeOperatorToken();
        }
      } else if (isOperatorCharacter(ch)) {
        storeOperatorToken();
      } else if (isFunctionKeyCharacter(ch)) {
        storeFunctionToken();
      } else if (isSeparatorCharacter(ch)) {
        mNextToken = createSeparatorToken(ch);
      } else if (ch >= 32) {
        throw new ParseException
          ("Illegal character '" + (char) ch + "'!", mPosition);
      } else {
        throw new ParseException
          ("Illegal character code " + ch + "!", mPosition);
      }
    } finally {
      clearTokenText();
    }
  }

  private void storeNumberToken()
    throws IOException
  {
    int ch = getNextCharacter();
    while (isDigit(ch)) {
      mTokenText.append((char) ch);
      ch = getNextCharacter();
    }
    putback(ch);
    mNextToken = createNumberToken();
  }

  private void storeSymbolToken(int ch)
    throws IOException
  {
    while (true) {
      if (isEscapeStartCharachter(ch)) {
        final Token.Type tokentype = appendEscapeGroup();
        if (tokentype == Token.Type.END) {
          return;
        }
      }
      ch = getNextCharacter();
      if (isIdentifierCharacter(ch) || isEscapeStartCharachter(ch)) {
        mTokenText.append((char) ch);
      } else {
        break;
      }
    }
    putback(ch);
    mNextToken = createSymbolToken();
  }

  private void storeOperatorToken()
    throws IOException, ParseException
  {
    Token token;
    final int ch = getNextCharacter();
    if (isOperatorCharacter(ch)) {
      mTokenText.append((char) ch);
      token = createOperatorToken();
      if (token == null) {
        putback(ch);
        mTokenText.deleteCharAt(1);
        token = createOperatorToken();
        if (token == null) {
          throw new ParseException
            ("Unknown operator '" + mTokenText + "' or '" +
             mTokenText + (char) ch + "'!", mTokenStart);
        }
      }
    } else {
      putback(ch);
      token = createOperatorToken();
      if (token == null) {
        throw new ParseException
          ("Unknown operator '" + mTokenText + "'!", mTokenStart);
      }
    }
    mNextToken = token;
  }

  private void storeFunctionToken()
    throws IOException, ParseException
  {
    int ch = getNextCharacter();
    if (isIdentifierStart(ch)) {
      mTokenText.append((char) ch);
      do {
        ch = getNextCharacter();
        mTokenText.append((char) ch);
      } while (isIdentifierCharacter(ch));
      final int end = mTokenText.length() - 1;
      mTokenText.deleteCharAt(end);
      putback(ch);
      final Token token = createFunctionToken();
      if (token == null) {
        throw new ParseException
          ("Unknown built-in function '" + mTokenText + "'!", mTokenStart);
      }
      mNextToken = token;
    } else {
      throw new ParseException
        ("Function character '" + mTokenText +
         "' not followed by function name!", mTokenStart);
    }
  }

  private Token.Type appendEscapeGroup()
    throws IOException
  {
    int nesting = 1;
    int ch;
    do {
      ch = getNextCharacter();
      if (ch < 0) {
        mNextToken = createToken(Token.Type.END);
        return Token.Type.END;
      }
      mTokenText.append((char) ch);
      if (isEscapeStartCharachter(ch)) {
        nesting++;
      } else if (isEscapeEndCharacter(ch)) {
        nesting--;
      }
    } while (nesting > 0);
    return Token.Type.SYMBOL;
  }


  //#########################################################################
  //# Stream Reading
  private int getNextCharacter()
    throws IOException
  {
    mPosition++;
    if (mPutbackCharacter == -2) {
      return mInputStream.read();
    } else {
      final int ch = mPutbackCharacter;
      mPutbackCharacter = -2;
      return ch;
    }
  }

  private void putback(final int ch)
  {
    if (mPutbackCharacter == -2) {
      mPosition--;
      mPutbackCharacter = ch;
    } else {
      throw new IllegalStateException
        ("Trying to put back more than one character!");
    }
  }


  //#########################################################################
  //# Token Creation
  private Token createSeparatorToken(final int ch)
  {
    final Integer character = new Integer(ch);
    final SeparatorTokenCreator creator = SEPARATORS.get(character);
    return creator.createToken(mTokenStart);
  }

  private Token createOperatorToken()
  {
    final String text = mTokenText.toString();
    if (mOperatorTable.containsOperator(text)) {
      return new OperatorToken(text, mTokenStart);
    } else {
      return null;
    }
  }

  private Token createFunctionToken()
  {
    final String text = mTokenText.toString();
    final BuiltInFunction function = mOperatorTable.getBuiltInFunction(text);
    if (function != null) {
      return new BuiltInFunctionToken(text, mTokenStart);
    } else {
      return null;
    }
  }

  private Token createNumberToken()
  {
    return createToken(Token.Type.NUMBER);
  }

  private Token createSymbolToken()
  {
    return createToken(Token.Type.SYMBOL);
  }

  private Token createToken(final Token.Type type)
  {
    final String text = mTokenText.toString();
    return new Token(type, text, mTokenStart);
  }

  private void clearTokenText()
  {
    mTokenText.delete(0, mTokenText.length());
  }


  //#########################################################################
  //# Initialising the Separator Token Map
  private static Map<Integer,SeparatorTokenCreator> createSeparatorTokenMap()
  {
    final Map<Integer,SeparatorTokenCreator> map =
      new HashMap<Integer,SeparatorTokenCreator>(16);
    storeSeparatorTokenCreator(map, '(', Token.Type.OPENBR);
    storeSeparatorTokenCreator(map, ')', Token.Type.CLOSEBR);
    storeSeparatorTokenCreator(map, '[', Token.Type.OPENSQ);
    storeSeparatorTokenCreator(map, ']', Token.Type.CLOSESQ);
    storeSeparatorTokenCreator(map, ',', Token.Type.COMMA);
    storeSeparatorTokenCreator(map, -1, Token.Type.END);
    return map;
  }

  private static void storeSeparatorTokenCreator
    (final Map<Integer,SeparatorTokenCreator> map,
     final int character,
     final Token.Type type)
  {
    final String name =
      character == -1 ? null : Character.toString((char) character);
    final SeparatorTokenCreator creator =
      new SeparatorTokenCreator(type, name);
    map.put(character, creator);
  }


  //#########################################################################
  //# Local Class OperatorToken
  private class OperatorToken extends Token
  {
    //#######################################################################
    //# Constructors
    private OperatorToken(final String text, final int pos)
    {
      super(Token.Type.OPERATOR, text, pos);
    }

    //#######################################################################
    //# Specialised Access Methods
    @Override
    UnaryOperator getUnaryOperator()
    {
      final String text = getText();
      return mOperatorTable.getUnaryOperator(text);
    }

    @Override
    BinaryOperator getBinaryOperator()
    {
      final String text = getText();
      return mOperatorTable.getBinaryOperator(text);
    }
  }


  //#########################################################################
  //# Local Class BuiltInFunctionToken
  private class BuiltInFunctionToken extends Token
  {
    //#######################################################################
    //# Constructors
    private BuiltInFunctionToken(final String text, final int pos)
    {
      super(Token.Type.FUNCTION, text, pos);
    }

    //#######################################################################
    //# Specialised Access Methods
    @Override
    BuiltInFunction getBuiltInFunction()
    {
      final String text = getText();
      return mOperatorTable.getBuiltInFunction(text);
    }
  }


  //#########################################################################
  //# Local Class SeparatorTokenCreator
  private static class SeparatorTokenCreator
  {
    //#######################################################################
    //# Constructors
    private SeparatorTokenCreator(final Token.Type type, final String name)
    {
      mType = type;
      mName = name;
    }

    //#######################################################################
    //# Token Creation
    private Token createToken(final int pos)
    {
      return new Token(mType, mName, pos);
    }

    //#######################################################################
    //# Data Members
    private final Token.Type mType;
    private final String mName;
  }


  //#########################################################################
  //# Data Members
  private final OperatorTable mOperatorTable;
  private final StringBuilder mTokenText;

  private Reader mInputStream;
  private Token mNextToken;
  private int mPutbackCharacter;
  private int mPosition;
  private int mTokenStart;


  //#########################################################################
  //# Static Class Variables
  private static final Map<Integer,SeparatorTokenCreator> SEPARATORS =
    createSeparatorTokenMap();

}
