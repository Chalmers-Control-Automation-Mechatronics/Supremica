//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   ExpressionParser
//###########################################################################
//# $Id: ExpressionParser.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.UnexpectedWatersException;


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
 *   final ExpressionParser parser = new {@link #ExpressionParser()};
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
 * example, the type of an expression <CODE>1&nbsp;+&nbsp;+3</CODE> can be
 * accurately identified as integer. The type of an identifier such as
 * <CODE>ev2</CODE>, however, remains unknown because names may be bound to
 * different values depending on the context in which they are
 * evaluated.</P>
 *
 * <P>Type checking is performed automatically on subterms of expressions.
 * For example, it is checked whether the arguments of an addition
 * expression can be integers. In addition, parser can be instructed to
 * check the expected type of an expression to be parsed by passing it a
 * type mask. To check whether an expression may an <I>integer</I> or an
 * <I>atom</I>, e.g., the following code can be used.</P>
 * 
 * <PRE>
 *   expr = parser.{@link #parse(String,int) parse}
 *     (text, {@link SimpleExpressionProxy#TYPE_INT} | {@link SimpleExpressionProxy#TYPE_ATOM});
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
  public ExpressionParser()
  {
    mScanner = new ExpressionScanner();
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
    return parse(input, SimpleExpressionProxy.TYPE_ANY);
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
    return (IdentifierProxy) parse(input, SimpleExpressionProxy.TYPE_NAME);
  }

  /**
   * Parses a string into an expression and performs simple type checking.
   * @param  input       The string to be parsed.
   * @param  mask        A bit mask identifying the expected type of the
   *                     evaluated expression, using the type constants
   *                     {@link SimpleExpressionProxy#TYPE_INT},
   *                     {@link SimpleExpressionProxy#TYPE_ATOM},
   *                     {@link SimpleExpressionProxy#TYPE_RANGE},
   *                     and {@link SimpleExpressionProxy#TYPE_NAME}.
   * @throws ParseException to indicate that the string could not be
   *                     parsed because of some syntax error.
   */
  public SimpleExpressionProxy parse(final String input, final int mask)
    throws ParseException
  {
    try {
      final Reader reader = new StringReader(input);
      return parse(reader, mask);
    } catch (final IOException exception) {
      throw new UnexpectedWatersException(exception);
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
   *                     {@link SimpleExpressionProxy#TYPE_INT},
   *                     {@link SimpleExpressionProxy#TYPE_ATOM},
   *                     {@link SimpleExpressionProxy#TYPE_RANGE},
   *                     and {@link SimpleExpressionProxy#TYPE_NAME}.
   * @throws ParseException to indicate that the input could not be
   *                     parsed because of some syntax error.
   * @throws IOException to indicate that reading from the input stream
   *                     has failed.
   */
  public SimpleExpressionProxy parse(final Reader reader, final int mask)
    throws IOException, ParseException
  {
    try {
      mScanner.setInputStream(reader);
      final SimpleExpressionProxy result = parse(Token.END, null);
      checkType(result, mask, null);
      return result;
    } finally {
      mScanner.setInputStream(null);
    }
  }


  //#########################################################################
  //# Auxiliary Methods for Parsing
  private SimpleExpressionProxy parse(final int limit, final Token opening)
    throws IOException, ParseException
  {
    final SimpleExpressionProxy expr =
      parse(OperatorTable.PRIORITY_OUTER, OperatorTable.ASSOC_NONE);
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
    return expr;
  }

  private SimpleExpressionProxy parse(final int outerpri, final int outerassoc)
    throws IOException, ParseException
  {
    final Token token = mScanner.peek();
    switch (token.getType()) {
    case Token.END:
      throw createParseException("Unexpected end of input!", token);
    case Token.OPENBR:
      mScanner.next();
      final SimpleExpressionProxy lhs = parse(Token.CLOSEBR, token);
      return parse(lhs, token, outerpri, outerassoc);
    case Token.OPENEN:
      mScanner.next();
      final SimpleExpressionProxy enumset = parseEnumSet();
      return parse(enumset, token, outerpri, outerassoc);
    case Token.OPENSQ:
    case Token.CLOSEBR:
    case Token.CLOSEEN:
    case Token.CLOSESQ:
    case Token.COMMA:
      throw createUnexpectedTokenException(token);
    case Token.OPERATOR:
      final UnaryOperator op = token.getUnaryOperator();
      if (op != null) {
	final Token nexttoken = mScanner.next();
	final SimpleExpressionProxy subterm =
	  parse(op.getPriority(), OperatorTable.ASSOC_NONE);
	checkType(subterm, op.getArgTypes(), nexttoken);
	final SimpleExpressionProxy unary = op.createExpression(subterm);
	return parse(unary, token, outerpri, outerassoc);
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
      final SimpleExpressionProxy number = new IntConstantProxy(value);
      return parse(number, token, outerpri, outerassoc);
    case Token.SYMBOL:
      mScanner.next();
      final String name = token.getText();
      final IdentifierProxy ident = parseIdent(name);
      return parse(ident, token, outerpri, outerassoc);
    default:
      throw new IllegalStateException
	("Bad token type " + token.getType() + "!");
    }
  }

  private SimpleExpressionProxy parse(final SimpleExpressionProxy lhs,
				      final Token lhstoken,
				      final int outerpri,
				      final int outerassoc)
    throws IOException, ParseException
  {
    final Token token = mScanner.peek();
    if (token.getType() == Token.OPERATOR) {
      final BinaryOperator op = token.getBinaryOperator();
      if (op == null) {
	throw createUnexpectedTokenException(token);
      } 
      final int innerpri = op.getPriority();
      if (innerpri < outerpri ||
	  innerpri == outerpri && outerassoc != OperatorTable.ASSOC_RIGHT) {
	return lhs;
      }
      checkType(lhs, op.getLHSTypes(), lhstoken);
      final Token rhstoken = mScanner.next();
      final int innerassoc = op.getAssociativity();
      final SimpleExpressionProxy rhs = parse(innerpri, innerassoc);
      checkType(rhs, op.getRHSTypes(), rhstoken);
      final SimpleExpressionProxy binary = op.createExpression(lhs, rhs);
      return parse(binary, token, outerpri, outerassoc);      
    } else {
      return lhs;
    }
  }

  private IdentifierProxy parseIdent(final String name)
    throws IOException, ParseException
  {
    Token token = mScanner.peek();
    if (token.getType() == Token.OPENSQ) {
      final List indexes = new LinkedList();
      while (token.getType() == Token.OPENSQ) {
	final Token indextoken = mScanner.next();
	final SimpleExpressionProxy index = parse(Token.CLOSESQ, token);
	checkType(index, SimpleExpressionProxy.TYPE_INT, indextoken);
	indexes.add(index);
	token = mScanner.peek();
      }
      return new IndexedIdentifierProxy(name, indexes);      
    } else {
      return new SimpleIdentifierProxy(name);
    }
  }

  private EnumSetExpressionProxy parseEnumSet()
    throws IOException, ParseException
  {
    final List items = new LinkedList();
    Token token = mScanner.next();
    if (token.getType() != Token.CLOSEEN) {
      while (true) {
	if (token.getType() != Token.SYMBOL) {
	  throw createUnexpectedTokenException(token);
	}
	final String name = token.getText();
	final SimpleIdentifierProxy ident = new SimpleIdentifierProxy(name);
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
    return new EnumSetExpressionProxy(items);
  }


  //#########################################################################
  //# Error Handling
  private void checkType(final SimpleExpressionProxy expr,
			 final int mask,
			 final Token token)
    throws ParseException
  {
    if ((expr.getResultTypes() & mask) == 0) {
      if (token == null) {
	throw createParseException
	  ("Expression is not of type " +
	   SimpleExpressionProxy.getTypeName(mask) + "!", token);
      } else {
	throw createParseException
	  ("Subterm '" + expr + "' is not of type " +
	   SimpleExpressionProxy.getTypeName(mask) + "!", token);
      }
    }
  }

  private ParseException createUnexpectedTokenException(final Token token)
  {
    return createParseException
      ("Unexpected token '" + token.getText() + "'!", token);
  }

  private ParseException createParseException(final String msg,
					      final Token token)
  {
    final int pos = token == null ? 0 : token.getPosition();
    return new ParseException(msg, pos);
  }


  //#########################################################################
  //# Data Members
  private final ExpressionScanner mScanner;

}
