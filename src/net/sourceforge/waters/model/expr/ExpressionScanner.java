//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   ExpressionScanner
//###########################################################################
//# $Id: ExpressionScanner.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


/**
 * <P>The expression scanner.
 * The expression scanner analyses a stream of characters and converts
 * it to tokens for the parser.</P>
 *
 * <P>In addition to the package-local scanning functionality,
 * this class provides a few methods that can be used to check
 * whether a character can be used in a Waters expression.</P>
 *
 * @author Robi Malik
 */

public class ExpressionScanner {

  //#########################################################################
  //# Constructors
  ExpressionScanner()
  {
    this(null);
  }

  ExpressionScanner(final Reader reader)
  {
    mInputStream = reader;
    mTokenText = new StringBuffer();
    mNextToken = null;
    mPutbackCharacter = -2;
    mPosition = -1;
  }
    

  //#########################################################################
  //# Setting the Input Stream
  void setInputStream(final Reader reader)
  {
    mInputStream = reader;
  }


  //#########################################################################
  //# High-Level Methods for Scanning
  Token next()
    throws IOException, ParseException
  {
    final Token token = peek();
    mNextToken = null;
    return token;    
  }

  Token peek()
    throws IOException, ParseException
  {
    if (mNextToken == null) {
      storeNextToken();
    }
    return mNextToken;
  }    

  int getPosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Auxiliary Methods for Scanning
  private void storeNextToken()
    throws IOException, ParseException
  {
    int ch;
    do {
      ch = getNextCharacter();
    } while (isWhitespace(ch));
    mTokenStart = mPosition;
    mTokenText.append((char) ch);
    if (isDigit(ch)) {
      storeNumberToken();
    } else if (isIdentifierStart(ch)) {
      storeSymbolToken();
    } else if (ch == '-') {
      ch = getNextCharacter();
      if (isDigit(ch)) {
	mTokenText.append((char) ch);
	storeNumberToken();
      } else {
	putback(ch);
	mNextToken = createOperatorToken();
      }
    } else if (isOperatorCharacter(ch)) {
      storeOperatorToken();
    } else if (isSeparatorCharacter(ch)) {
      mNextToken = createSeparatorToken(ch);
    } else if (ch >= 32) {
      throw new ParseException
	("Illegal character '" + (char) ch + "'!", mPosition);
    } else {
      throw new ParseException
	("Illegal character code " + ch + "!", mPosition);
    }
    clearTokenText();
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

  private void storeSymbolToken()
    throws IOException
  {
    int ch = getNextCharacter();
    while (isIdentifierCharacter(ch)) {
      mTokenText.append((char) ch);
      ch = getNextCharacter();
    }
    putback(ch);
    mNextToken = createSymbolToken();
  }

  private void storeOperatorToken()
    throws IOException, ParseException
  {
    final int ch = getNextCharacter();
    mTokenText.append((char) ch);
    Token token = createOperatorToken();
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
    mNextToken = token;
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
    final SeparatorTokenCreator creator =
      (SeparatorTokenCreator) sSeparatorTokens.get(character);
    return creator.createToken(mTokenStart);
  }

  private Token createOperatorToken()
  {
    final String text = mTokenText.toString();
    final OperatorTable.Entry entry = OperatorTable.getOperatorEntry(text);
    if (entry == null) {
      return null;
    } else {
      return new OperatorToken(text, mTokenStart, entry);
    }
  }

  private Token createNumberToken()
  {
    return createToken(Token.NUMBER);
  }

  private Token createSymbolToken()
  {
    return createToken(Token.SYMBOL);
  }

  private Token createToken(final int type)
  {
    final String text = mTokenText.toString();
    return new Token(type, text, mTokenStart);
  }

  private void clearTokenText()
  {
    mTokenText.delete(0, mTokenText.length());
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
    return ch == ' ' || ch == '\t';
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
  public static boolean isOperatorCharacter(final int ch)
  {
    return ch >= 0 && OperatorTable.isOperatorCharacter((char) ch);
  }

  /**
   * Checks whether a character represents a separator character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as a
   *         special, or separator, character in a Waters expression.
   */
  public static boolean isSeparatorCharacter(final int ch)
  {
    final Integer character = new Integer(ch);
    return sSeparatorTokens.containsKey(character);
    
  }

  /**
   * Checks whether a character is an expression character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can occur
   *         somewhere in a syntactically correct expression.
   */
  public static boolean isExpressionCharacter(final int ch)
  {
    return
      isWhitespace(ch) ||
      isDigit(ch) ||
      isIdentifierCharacter(ch) ||
      isOperatorCharacter(ch) ||
      isSeparatorCharacter(ch);
  }

  /**
   * Checks whether a string qualifies as a Waters identifier.
   * Strings that pass this test can be used as the name of a valid
   * {@link SimpleIdentifierProxy}, or as names of any Waters elements that
   * are identified by strings rather than identifiers.
   * @param  word        The string to be examined.
   * @return <CODE>true</CODE> if the given string is a valid identifier.
   */
  public static boolean isWatersIdentifier(final String word)
  {
    final int len = word.length();
    if (len == 0 || !isIdentifierStart(word.charAt(0))) {
      return false;
    } else {
      for (int index = 1; index < len; index++) {
	if (!isIdentifierCharacter(word.charAt(index))) {
	  return false;
	}
      }
      return true;
    }
  }


  //#########################################################################
  //# Initialising the Separator Token Map
  private static Map createSeparatorTokenMap()
  {
    final Map map = new HashMap(16);
    storeSeparatorTokenCreator(map, '(', Token.OPENBR);
    storeSeparatorTokenCreator(map, ')', Token.CLOSEBR);
    storeSeparatorTokenCreator(map, '[', Token.OPENSQ);
    storeSeparatorTokenCreator(map, ']', Token.CLOSESQ);
    storeSeparatorTokenCreator(map, '{', Token.OPENEN);
    storeSeparatorTokenCreator(map, '}', Token.CLOSEEN);
    storeSeparatorTokenCreator(map, ',', Token.COMMA);
    storeSeparatorTokenCreator(map, -1, Token.END);
    return map;
  }

  private static void storeSeparatorTokenCreator(final Map map,
						 final int ch,
						 final int type)
  {
    final Integer character = new Integer(ch);
    final String name = ch == -1 ? null : Character.toString((char) ch);
    final SeparatorTokenCreator creator =
      new SeparatorTokenCreator(type, name);
    map.put(character, creator);
  }


  //#########################################################################
  //# Local Class OperatorToken
  private static class OperatorToken extends Token
  {
    //#######################################################################
    //# Constructors
    private OperatorToken(final String text,
			  final int pos,
			  final OperatorTable.Entry entry)
    {
      super(Token.OPERATOR, text, pos);
      mOperatorEntry = entry;
    }

    //#######################################################################
    //# Specialised Access Methods
    UnaryOperator getUnaryOperator()
    {
      return mOperatorEntry.getUnaryOperator();
    }

    BinaryOperator getBinaryOperator()
    {
      return mOperatorEntry.getBinaryOperator();
    }

    //#######################################################################
    //# Data Members
    private final OperatorTable.Entry mOperatorEntry;
  }


  //#########################################################################
  //# Local Class SeparatorTokenCreator
  private static class SeparatorTokenCreator
  {
    //#######################################################################
    //# Constructors
    private SeparatorTokenCreator(final int type, final String name)
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
    private final int mType;
    private final String mName;
  }

  
  //#########################################################################
  //# Data Members
  private final StringBuffer mTokenText;

  private Reader mInputStream;
  private Token mNextToken;
  private int mPutbackCharacter;
  private int mPosition;
  private int mTokenStart;


  //#########################################################################
  //# Static Class Variables
  private static final Map sSeparatorTokens = createSeparatorTokenMap();

}
