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
        if (!ExpressionScanner.isIdentifierCharacter((int) ch) && !ExpressionScanner.isWhitespace((int) ch)) {
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
    final SimpleExpressionProxy expr = parse(input, Operator.TYPE_NAME);
    if (!(expr instanceof IdentifierProxy)) {
      System.err.println("ERROR: ExpressionParser fails to recognise " +
                         input + " as a non-identifier!");
    }
    return (IdentifierProxy) expr;
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
      final ParseResult result = parseResult(Token.Type.END, null);
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

  private String removeBlanks(final String text)
  {
    final int len = text.length();
    int start = 0;
    char c;
    for (; start < len; start++) {
      c = text.charAt(start);
      if (!ExpressionScanner.isWhitespace((int) c)) {
        break;
      }
    }
    if (start == len) {
      return "";
    }
    int end = len;
    do {
      c = text.charAt(--end);
    } while (ExpressionScanner.isWhitespace((int) c));
    return text.substring(start, end + 1);
  }


  //#########################################################################
  //# Auxiliary Methods for Parsing
  private ParseResult parseResult(final Token.Type limit,
                                  final Token opening)
    throws IOException, ParseException
  {
    final ParseResult result =
      parseResult(OperatorTable.PRIORITY_OUTER, BinaryOperator.ASSOC_NONE);
    final Token after = mScanner.peek();
    if (after.getType() != limit) {
      switch (limit) {
      case END:
        throw createUnexpectedTokenException(after);
      case COMMA:
        if (after.getType() == Token.Type.CLOSEBR) {
          break;
        }
      case CLOSEBR:
        throw createParseException("Matching ')' not found!", opening);
      case CLOSESQ:
        throw createParseException("Matching ']' not found!", opening);
      default:
        throw new IllegalStateException("Bad limit type " + limit + "!");
      }
    }
    return result;
  }

  private ParseResult parseResult(final int outerpri, final int outerassoc)
    throws IOException, ParseException
  {
    final Token token = mScanner.peek();
    switch (token.getType()) {
    case END:
      throw createUnexpectedTokenException(token);
    case OPENBR:
      mScanner.next();
      final ParseResult lhs = parseResult(Token.Type.CLOSEBR, token);
      mScanner.next();
      return parseResult(lhs, token, outerpri, outerassoc);
    case OPENSQ:
      mScanner.next();
      final ParseResult enumset = parseEnumSetResult();
      return parseResult(enumset, token, outerpri, outerassoc);
    case CLOSEBR:
    case CLOSESQ:
    case COMMA:
      throw createUnexpectedTokenException(token);
    case OPERATOR:
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
    case NUMBER:
      int value;
      mScanner.next();
      try {
        value = token.getIntValue();
      } catch (final NumberFormatException exception) {
        throw createParseException("Integer constant out of range!", token);
      }
      final ParseResult result = new IntConstantResult(value);
      return parseResult(result, token, outerpri, outerassoc);
    case SYMBOL:
      mScanner.next();
      final String name = token.getText();
      final ParseResult ident = parseIdentifierResult(name);
      return parseResult(ident, token, outerpri, outerassoc);
    case FUNCTION:
      mScanner.next();
      final BuiltInFunction function = token.getBuiltInFunction();
      final ParseResult funcall = parseFunctionCallResult(function);
      return parseResult(funcall, token, outerpri, outerassoc);
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
    if (token.getType() != Token.Type.OPERATOR) {
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
    while (token.getType() == Token.Type.OPENSQ) {
      mScanner.next();
      final Token indexToken = mScanner.peek();
      final ParseResult index = parseResult(Token.Type.CLOSESQ, token);
      checkType(index, Operator.TYPE_INDEX, indexToken);
      indexes.add(index);
      mScanner.next();
      token = mScanner.peek(false);
    }
    return new IdentifierResult(name, indexes);
  }

  private ParseResult parseFunctionCallResult(final BuiltInFunction function)
    throws IOException, ParseException
  {
    Token token = mScanner.next();
    if (token.getType() != Token.Type.OPENBR) {
      throw createUnexpectedTokenException(token);
    }
    final List<ParseResult> args = new LinkedList<ParseResult>();
    int argno = 0;
    final int max = function.getMaximumNumberOfArguments();
    do {
      final Token argToken = mScanner.peek();
      final ParseResult arg = parseResult(Token.Type.COMMA, token);
      if (argno >= max) {
        throw createParseException
          ("Attempting to pass more than " + max +
           " arguments to function " + function.getName(), token);
      }
      checkType(arg, function.getArgumentTypes(argno), argToken);
      args.add(arg);
      argno++;
      token = mScanner.next();
    } while (token.getType() == Token.Type.COMMA);
    final int min = function.getMinimumNumberOfArguments();
    if (argno < min) {
      throw createParseException
        ("Attempting to pass only " + argno +
         " arguments to function " + function.getName() +
         ", but " + min + " are required", token);
    }
    return new FunctionCallResult(function, args);
  }

  private ParseResult parseEnumSetResult()
    throws IOException, ParseException
  {
    final List<SimpleIdentifierProxy> items =
      new LinkedList<SimpleIdentifierProxy>();
    Token token = mScanner.next();
    if (token.getType() != Token.Type.CLOSESQ) {
      while (true) {
        if (token.getType() != Token.Type.SYMBOL) {
          throw createUnexpectedTokenException(token);
        }
        final String name = token.getText();
        final SimpleIdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(name);
        items.add(ident);
        token = mScanner.next();
        if (token.getType() == Token.Type.CLOSESQ) {
          break;
        } else if (token.getType() != Token.Type.COMMA) {
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
    if (token.getType() == Token.Type.END) {
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
    final StringBuilder buffer = new StringBuilder();
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
