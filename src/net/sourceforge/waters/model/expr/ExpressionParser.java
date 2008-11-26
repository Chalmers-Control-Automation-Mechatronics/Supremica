//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   ExpressionParser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>The expression parser.</P>
 *
 * <P>The expression parser can produce expression objects ({@link
 * SimpleExpressionProxy}) from textual representations. It is therefore
 * used to analyse all user input for events, ranges, aliases, etc.</P>
 *
 * <P>In order to translate a string into an expression, an
 * ExpressionParser object must be created first. The following code
 * can be used to parse a string called <CODE>text</CODE>.</P>
 *
 * <PRE>
 *   final {@link ModuleProxyFactory} factory = {@link net.sourceforge.waters.plain.module.ModuleElementFactory ModuleElementFactory}.{@link net.sourceforge.waters.plain.module.ModuleElementFactory#getInstance() getInstance}();
 *   final {@link OperatorTable} optable = {@link net.sourceforge.waters.model.compiler.CompilerOperatorTable CompilerOperatorTable}.{@link net.sourceforge.waters.model.compiler.CompilerOperatorTable#getInstance() getInstance}();
 *   final ExpressionParser parser = new {@link #ExpressionParser(ModuleProxyFactory,OperatorTable)}(factory, optable);
 *   try {
 *     expr = parser.{@link #parse(String) parse}(text);
 *   } catch (final {@link ParseException} exception) {
 *     // an error has occurred ...
 *     ...
 *   }
 * </PRE>
 *
 * <P>The parser includes simple type checking functionality, which works
 * correctly as long as the evaluation context is not required. For
 * example, the type of an expression <CODE>1&nbsp;+&nbsp;3</CODE> can be
 * accurately identified as integer. The type of an identifier such as
 * <CODE>ev2</CODE>, however, remains unknown because names may be bound to
 * different values depending on the context in which they are
 * evaluated.</P>
 *
 * <P>Type checking is performed automatically on subterms of expressions.
 * For example, it is checked whether the arguments of an addition
 * expression can be integers. In addition, the parser can be instructed to
 * check the expected type of an expression to be parsed by passing it a
 * type mask. To check whether an expression may be an <I>integer</I> or an
 * <I>atom</I>, e.g., the following code can be used.</P>
 *
 * <PRE>
 *   expr = parser.{@link #parse(String,int) parse}
 *     (text, {@link Operator#TYPE_INT} | {@link Operator#TYPE_ATOM});
 * </PRE>
 *
 * @author Robi Malik
 */

public class ExpressionParser {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new expression parser.
   */
  public ExpressionParser(final ModuleProxyFactory factory,
                          final OperatorTable optable)
  {
    this(factory, new ExpressionScanner(optable));
  }

  /**
   * Creates a new expression parser.
   */
  public ExpressionParser(final ModuleProxyFactory factory,
                          final ExpressionScanner scanner)
  {
    mFactory = factory;
    mScanner = scanner;
  }


  //#########################################################################
  //# High-Level Methods for Parsing
  /**
   * Parses a string into an expression.
   * @param  input       The string to be parsed.
   * @throws ParseException to indicate that the string could not be
   *                     parsed because of some syntax error.
   */
  public SimpleExpressionProxy parse(final String input)
    throws ParseException
  {
    return parse(input, Operator.TYPE_ANY);
  }

  /**
   * Parses a string into a simple identifier.
   * @param  input       The string to be parsed.
   * @throws ParseException to indicate that the string could not be
   *                     parsed or converted to a simple identifier because of
   *                     some syntax error.
   */
  public SimpleIdentifierProxy parseSimpleIdentifier(final String input)
    throws ParseException
  {
    final IdentifierProxy ident = parseIdentifier(input);
    if (ident instanceof SimpleIdentifierProxy) {
      return (SimpleIdentifierProxy) ident;
    } else {
      final int len = input.length();
      int pos = 0;
      do {
        final char ch = input.charAt(pos);
        if (!isIdentifierCharacter(ch) && !isWhitespace(ch)) {
          break;
        }
      } while (++pos < len);
      throw new ParseException("Structured names are not allowed here!", pos);
    }
  }

  /**
   * Parses a string into an identifier.
   * @param  input       The string to be parsed.
   * @throws ParseException to indicate that the string could not be
   *                     parsed or converted to an identifier because of
   *                     some syntax error.
   */
  public IdentifierProxy parseIdentifier(final String input)
    throws ParseException
  {
    return (IdentifierProxy) parse(input, Operator.TYPE_NAME);
  }

  /**
   * Parses a string into an expression and performs simple type checking.
   * @param  input       The string to be parsed.
   * @param  mask        A bit mask identifying the expected type of the
   *                     evaluated expression, using the type constants
   *                     {@link Operator#TYPE_INT},
   *                     {@link Operator#TYPE_ATOM},
   *                     {@link Operator#TYPE_RANGE},
   *                     and {@link Operator#TYPE_NAME}.
   * @throws ParseException to indicate that the string could not be
   *                     parsed because of some syntax error.
   */
  public SimpleExpressionProxy parse(String input, final int mask)
    throws ParseException
  {
    input = removeBlanks(input);
    try {
      final Reader reader = new StringReader(input);
      return parse(reader, mask, input);
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * Parses an expression from an input stream.
   * This method tries to read the entire stream and produce an expression
   * out of it. It is an error if the stream contains extra characters
   * at the end.
   * @param  reader      A reader providing input from a stream.
   * @param  mask        A bit mask identifying the expected type of the
   *                     evaluated expression, using the type constants
   *                     {@link Operator#TYPE_INT},
   *                     {@link Operator#TYPE_ATOM},
   *                     {@link Operator#TYPE_RANGE},
   *                     and {@link Operator#TYPE_NAME}.
   * @throws ParseException to indicate that the input could not be
   *                     parsed because of some syntax error.
   * @throws IOException to indicate that reading from the input stream
   *                     has failed.
   */
  public SimpleExpressionProxy parse(final Reader reader, final int mask)
    throws IOException, ParseException
  {
    return parse(reader, mask, null);
  }

  /**
   * Parses an expression from an input stream.
   * This method tries to read the entire stream and produce an expression
   * out of it. It is an error if the stream contains extra characters
   * at the end.
   * @param  reader      A reader providing input from a stream.
   * @param  mask        A bit mask identifying the expected type of the
   *                     evaluated expression, using the type constants
   *                     {@link Operator#TYPE_INT},
   *                     {@link Operator#TYPE_ATOM},
   *                     {@link Operator#TYPE_RANGE},
   *                     and {@link Operator#TYPE_NAME}.
   * @param  input       The original text of the expression.
   *                     If non-<CODE>null</CODE>, this string is provided
   *                     as (optional) rendering text to the parsed
   *                     expression.
   * @throws ParseException to indicate that the input could not be
   *                     parsed because of some syntax error.
   * @throws IOException to indicate that reading from the input stream
   *                     has failed.
   */
  public SimpleExpressionProxy parse(final Reader reader,
                                     final int mask,
                                     final String input)
    throws IOException, ParseException
  {
    try {
      mScanner.setInputStream(reader);
      final ParseResult result = parseResult(Token.END, null);
      checkType(result, mask, null);
      final SimpleExpressionProxy expr = result.createProxy(mFactory);
      if (input == null || expr.toString().equals(input)) {
        return expr;
      } else {
        return result.createProxy(mFactory, input);
      }
    } finally {
      mScanner.setInputStream(null);
    }
  }


  //#########################################################################
  //# Character Classes
  /**
   * Checks whether a character is whitespace.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as
   *         whitespace in a Waters expression.
   */
  public boolean isWhitespace(final int ch)
  {
    return mScanner.isWhitespace(ch);
  }

  /**
   * Checks whether a character represents a digit.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as
   *         a digit in a Waters expression.
   */
  public boolean isDigit(final int ch)
  {
    return mScanner.isDigit(ch);
  }

  /**
   * Checks whether a character represents an identifier start.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as the
   *         first character of a Waters identifier.
   */
  public boolean isIdentifierStart(final int ch)
  {
    return mScanner.isIdentifierStart(ch);
  }

  /**
   * Checks whether a character represents an identifier character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used in
   *         a Waters identifier.
   */
  public boolean isIdentifierCharacter(final int ch)
  {
    return mScanner.isIdentifierCharacter(ch);
  }

  /**
   * Checks whether a character represents an operator character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used in
   *         a Waters operator.
   */
  public boolean isOperatorCharacter(final int ch)
  {
    return mScanner.isOperatorCharacter(ch);
  }

  /**
   * Checks whether a character represents a separator character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can be used as a
   *         special, or separator, character in a Waters expression.
   */
  public boolean isSeparatorCharacter(final int ch)
  {
    return mScanner.isSeparatorCharacter(ch);
  }

  /**
   * Checks whether a character is an expression character.
   * @param  ch          The character to be checked.
   * @return <CODE>true</CODE> if the given character can occur
   *         somewhere in a syntactically correct expression.
   */
  public boolean isExpressionCharacter(final int ch)
  {
    return mScanner.isExpressionCharacter(ch);
  }

  /**
   * Checks whether a string qualifies as a Waters identifier. Strings that
   * pass this test can be used as the name of a valid {@link
   * net.sourceforge.waters.model.module.SimpleIdentifierProxy
   * SimpleIdentifierProxy}, or as names of any Waters elements that are
   * identified by strings rather than identifiers.
   * @param  word        The string to be examined.
   * @return <CODE>true</CODE> if the given string is a valid identifier.
   */
  public boolean isWatersIdentifier(final String word)
  {
    return mScanner.isWatersIdentifier(word);
  }


  private String removeBlanks(final String text)
  {
    final int len = text.length();
    int start = 0;
    char c;
    for (; start < len; start++) {
      c = text.charAt(start);
      if (!isWhitespace(c)) {
        break;
      }
    }
    if (start == len) {
      return "";
    }
    int end = len;
    do {
      c = text.charAt(--end);
    } while (isWhitespace(c));
    return text.substring(start, end + 1);
  }


  //#########################################################################
  //# Auxiliary Methods for Parsing
  private ParseResult parseResult(final int limit,
                                  final Token opening)
    throws IOException, ParseException
  {
    final ParseResult result =
      parseResult(OperatorTable.PRIORITY_OUTER, BinaryOperator.ASSOC_NONE);
    final Token after = mScanner.peek();
    if (after.getType() != limit) {
      switch (limit) {
      case Token.END:
        throw createUnexpectedTokenException(after);
      case Token.CLOSEBR:
        throw createParseException("Matching ')' not found!", opening);
      case Token.CLOSESQ:
        throw createParseException("Matching ']' not found!", opening);
      default:
        throw new IllegalStateException("Bad limit type " + limit + "!");
      }
    } else if (limit != Token.END) {
      mScanner.next();
    }
    return result;
  }

  private ParseResult parseResult(final int outerpri, final int outerassoc)
    throws IOException, ParseException
  {
    final Token token = mScanner.peek();
    switch (token.getType()) {
    case Token.END:
      throw createUnexpectedTokenException(token);
    case Token.OPENBR:
      mScanner.next();
      final ParseResult lhs = parseResult(Token.CLOSEBR, token);
      return parseResult(lhs, token, outerpri, outerassoc);
    case Token.OPENEN:
      mScanner.next();
      final ParseResult enumset = parseEnumSetResult();
      return parseResult(enumset, token, outerpri, outerassoc);
    case Token.OPENSQ:
    case Token.CLOSEBR:
    case Token.CLOSEEN:
    case Token.CLOSESQ:
    case Token.COMMA:
      throw createUnexpectedTokenException(token);
    case Token.OPERATOR:
      final UnaryOperator op = token.getUnaryOperator();
      if (op != null) {
        mScanner.next();
        final ParseResult subresult =
          parseResult(op.getPriority(), BinaryOperator.ASSOC_NONE);
        final ParseResult result = new UnaryExpressionResult(op, subresult);
        final int types = result.getTypeMask();
        if (types == 0) {
          final SimpleExpressionProxy subterm =
            subresult.createProxy(mFactory);
          final int subtypes = subresult.getTypeMask();
          final String subtypesname = getTypeName(subtypes);
          throw createParseException
            ("Operator " + op.getName() +
             " cannot be applied to argument '" + subterm +
             "' of type " + subtypesname, token);
        }
        return parseResult(result, token, outerpri, outerassoc);
      } else {
        throw createUnexpectedTokenException(token);
      }
    case Token.NUMBER:
      int value;
      mScanner.next();
      try {
        value = token.getIntValue();
      } catch (final NumberFormatException exception) {
        throw createParseException("Integer constant out of range!", token);
      }
      final ParseResult result = new IntConstantResult(value);
      return parseResult(result, token, outerpri, outerassoc);
    case Token.SYMBOL:
      mScanner.next();
      final String name = token.getText();
      final ParseResult ident = parseIdentifierResult(name);
      return parseResult(ident, token, outerpri, outerassoc);
    default:
      throw new IllegalStateException
        ("Bad token type " + token.getType() + "!");
    }
  }

  private ParseResult parseResult(final ParseResult lhs,
                                  final Token lhstoken,
                                  final int outerpri,
                                  final int outerassoc)
    throws IOException, ParseException
  {
    final Token token = mScanner.peek(false);
    if (token.getType() != Token.OPERATOR) {
      return lhs;
    }
    final Operator op = token.getPostfixOperator();
    if (op == null) {
      throw createUnexpectedTokenException(token);
    }
    final int innerpri = op.getPriority();
    if (innerpri < outerpri) {
      return lhs;
    }
    if (op instanceof BinaryOperator) {
      if (innerpri == outerpri && outerassoc != BinaryOperator.ASSOC_RIGHT) {
        return lhs;
      }
      mScanner.next();
      final BinaryOperator binop = (BinaryOperator) op;
      final int innerassoc = binop.getAssociativity();
      final ParseResult rhs = parseResult(innerpri, innerassoc);
      final ParseResult result = new BinaryExpressionResult(binop, lhs, rhs);
      final int resultTypes = result.getTypeMask();
      if (resultTypes == 0) {
        final SimpleExpressionProxy lhsExpr = lhs.createProxy(mFactory);
        final SimpleExpressionProxy rhsExpr = rhs.createProxy(mFactory);
        final int lhsTypes = lhs.getTypeMask();
        final int rhsTypes = rhs.getTypeMask();
        final String lhsTypesName = getTypeName(lhsTypes);
        final String rhsTypesName = getTypeName(rhsTypes);
        throw createParseException
          ("Operator " + op.getName() +
           " cannot be applied to '" + lhsExpr + "' of type " + lhsTypesName +
           " and '" + rhsExpr + "' of type " + rhsTypesName, lhstoken);
      }
      return parseResult(result, token, outerpri, outerassoc);
    } else {
      mScanner.next();
      final UnaryOperator unop = (UnaryOperator) op;
      final ParseResult result = new UnaryExpressionResult(unop, lhs);
      final int resultTypes = result.getTypeMask();
      if (resultTypes == 0) {
        final SimpleExpressionProxy lhsExpr = lhs.createProxy(mFactory);
        final int lhsTypes = lhs.getTypeMask();
        final String lhsTypesName = getTypeName(lhsTypes);
        throw createParseException
          ("Operator " + op.getName() +
           " cannot be applied to '" + lhsExpr + "' of type " + lhsTypesName,
           lhstoken);
      }
      return parseResult(result, lhstoken, outerpri, outerassoc);
    }
  }

  private ParseResult parseIdentifierResult(final String name)
    throws IOException, ParseException
  {
    final List<ParseResult> indexes = new LinkedList<ParseResult>();
    Token token = mScanner.peek(false);
    while (token.getType() == Token.OPENSQ) {
      mScanner.next();
      final Token indexToken = mScanner.peek();
      final ParseResult index = parseResult(Token.CLOSESQ, token);
      checkType(index, Operator.TYPE_INDEX, indexToken);
      indexes.add(index);
      token = mScanner.peek();
    }
    return new IdentifierResult(name, indexes);
  }

  private ParseResult parseEnumSetResult()
    throws IOException, ParseException
  {
    final List<SimpleIdentifierProxy> items =
      new LinkedList<SimpleIdentifierProxy>();
    Token token = mScanner.next();
    if (token.getType() != Token.CLOSEEN) {
      while (true) {
        if (token.getType() != Token.SYMBOL) {
          throw createUnexpectedTokenException(token);
        }
        final String name = token.getText();
        final SimpleIdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(name);
        items.add(ident);
        token = mScanner.next();
        if (token.getType() == Token.CLOSEEN) {
          break;
        } else if (token.getType() != Token.COMMA) {
          throw createUnexpectedTokenException(token);
        }
        token = mScanner.next();
      }
    }
    return new EnumSetExpressionResult(items);
  }


  //#########################################################################
  //# Error Handling
  private void checkType(final ParseResult result,
                         final int mask,
                         final Token token)
    throws ParseException
  {
    if ((result.getTypeMask() & mask) == 0) {
      final String typeName = getTypeName(mask);
      if (token == null) {
        throw createParseException
          ("Expression is not of type " + typeName + "!", null);
      } else {
        final SimpleExpressionProxy expr = result.createProxy(mFactory);
        throw createParseException
          ("Subterm '" + expr + "' is not of type " + typeName + "!", token);
      }
    }
  }

  private ParseException createUnexpectedTokenException(final Token token)
  {
    if (token.getType() == Token.END) {
      return createParseException("Unexpected end of input!", token);
    } else {
      return createParseException
        ("Unexpected token '" + token.getText() + "'!", token);
    }
  }

  private ParseException createParseException(final String msg,
                                              final Token token)
  {
    final int pos = token == null ? 0 : token.getPosition();
    return new ParseException(msg, pos);
  }

  /**
   * Gets a readable representation of a type mask.
   * @param  mask        A bit mask composed of the values
   *                     {@link Operator#TYPE_INT},
   *                     {@link Operator#TYPE_BOOLEAN},
   *                     {@link Operator#TYPE_ATOM},
   *                     {@link Operator#TYPE_RANGE},
   *                     and {@link Operator#TYPE_NAME}.
   * @return A string that represents the type mask and can be presented
   *         to the user.
   */
  private static String getTypeName(final int mask)
  {
    final StringBuffer buffer = new StringBuffer();
    boolean first = true;
    if ((mask & Operator.TYPE_INT) != 0) {
      first = false;
      buffer.append(TYPENAME_INT);
    }
    if ((mask & Operator.TYPE_BOOLEAN) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or ");
      }
      buffer.append(TYPENAME_BOOLEAN);
    }
    if ((mask & Operator.TYPE_ATOM) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or ");
      }
      buffer.append(TYPENAME_ATOM);
    }
    if ((mask & Operator.TYPE_RANGE) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or ");
      }
      buffer.append(TYPENAME_RANGE);
    }
    if ((mask & Operator.TYPE_NAME) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or ");
      }
      buffer.append(TYPENAME_NAME);
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final ExpressionScanner mScanner;

  private static final String TYPENAME_INT = "INTEGER";
  private static final String TYPENAME_BOOLEAN = "BOOLEAN";
  private static final String TYPENAME_ATOM = "ATOM";
  private static final String TYPENAME_RANGE = "RANGE";
  private static final String TYPENAME_NAME = "NAME";

}
