//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   SimpleExpressionProxy
//###########################################################################
//# $Id: SimpleExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * The abstract base class of all expressions.
 *
 * This is the abstract base class of all expressions defined in the {@link
 * net.sourceforge.waters.model.expr} package. This includes all
 * expressions except event lists ({@link
 * net.sourceforge.waters.model.module.EventListExpressionProxy}).
 *
 * @author Robi Malik
 */

public abstract class SimpleExpressionProxy
  extends ExpressionProxy
  implements Comparable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an expression.
   */
  SimpleExpressionProxy()
  {
  }

  /**
   * Creates a simple expression from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  SimpleExpressionProxy(final SimpleExpressionType expr)
  {
    super(expr);
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  /**
   * Compares this simple expression to another.
   * This method establishes a linear ordering on all expressions,
   * which is roughly, but not perfectly, alphabetical.
   */
  public int compareTo(final Object partner)
  {
    SimpleExpressionProxy expr = (SimpleExpressionProxy) partner;
    return getOrderIndex() - expr.getOrderIndex();
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer, OperatorTable.PRIORITY_OUTER, false);
  }

  void pprint(final ModelPrinter printer,
	      final int outerpri,
	      final boolean assocbraces)
    throws IOException
  {
  }


  //#########################################################################
  //# Marshalling
  /**
   * Creates an empty new XML element that matches the type of this
   * expression.  This method is used internally for marshalling and should
   * not be called directly; use class {@link
   * net.sourceforge.waters.model.base.ProxyMarshaller ProxyMarshaller}
   * instead.
   * @return The created element.
   * @throws JAXBException to indicate that a fatal error occurred while
   *                 creating or copying some element.
   * @see    net.sourceforge.waters.model.base.ProxyMarshaller
   */
  public abstract SimpleExpressionType createElement
    (final ObjectFactory factory)
    throws JAXBException;

  /**
   * Creates a new XML element representing the contents of this element.
   * This method is used internally for marshalling and should not be
   * called directly; use class {@link
   * net.sourceforge.waters.model.base.ProxyMarshaller ProxyMarshaller}
   * instead.
   * @return The created element.
   * @throws JAXBException to indicate that a fatal error occurred while
   *                 creating or copying some element.
   * @see    net.sourceforge.waters.model.base.ProxyMarshaller
   */
  public SimpleExpressionType toSimpleExpressionType()
    throws JAXBException
  {
    final ElementFactory factory = new SimpleExpressionElementFactory();
    return (SimpleExpressionType) toJAXB(factory);
  }


  //#########################################################################
  //# Evaluation
  /**
   * Evaluates this expression.
   * This method evaluates all operators occurring in the expression
   * and looks up all identifiers in the context, until a primitive
   * object can be returned as value. It is an error if an identifier
   * that is not defined in the given context is encountered.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return A value object representing the result of evaluation.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context, because of
   *                     type or similar errors.
   */
  public abstract Value eval(Context context) throws EvalException;

  /**
   * Gets the possible type of the result of this expression.
   * This method provides a light-weight type checking facility, providing
   * only a rough guess for the possible type without taking context into
   * account. An integer constant will be correctly classified as integer,
   * while an identifier may be of any type because the context is not
   * known.
   * @return A bit mask composed of the values {@link #TYPE_INT}, {@link
   *         #TYPE_ATOM}, {@link #TYPE_RANGE}, and {@link #TYPE_NAME}. Bits
   *         are set exactly if the expression may have the corresponding
   *         type.
   */
  public abstract int getResultTypes();


  //#########################################################################
  //# Type Checking
  /**
   * Evaluates this expression to an integer.
   * This method calls {@link #eval(Context) eval()} to evaluate this
   * expression and then checks whether the result is an integer value.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return An integer value object representing the result of evaluation.
   * @throws TypeMismatchException to indicate that the expression does
   *                     not evaluate to an integer.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context.
   */
  public IntValue evalToInt(final Context context)
    throws EvalException
  {
    final Value value = eval(context);
    if (value instanceof IntValue) {
      return (IntValue) value;
    } else {
      throw new TypeMismatchException(this, value, TYPE_INT);
    }
  }

  /**
   * Evaluates this expression to an index.
   * This method calls {@link #eval(Context) eval()} to evaluate this
   * expression and then checks whether the result can be used as an array
   * index, i.e., whether it is an integer or an enumeration atom.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return A value object representing the result of evaluation.
   * @throws TypeMismatchException to indicate that the expression does
   *                     not evaluate to an integer or atom.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context.
   */
  public Value evalToIndex(final Context context)
    throws EvalException
  {
    final Value value = eval(context);
    if (value instanceof IntValue || value instanceof AtomValue) {
      return value;
    } else {
      throw new TypeMismatchException(this, value, TYPE_INT | TYPE_ATOM);
    }
  }

  /**
   * Evaluates this expression to a range.
   * This method calls {@link #eval(Context) eval()} to evaluate this
   * expression and then checks whether the result is a range.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return A range value object representing the result of evaluation.
   * @throws TypeMismatchException to indicate that the expression does
   *                     not evaluate to a range.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context.
   */
  public RangeValue evalToRange(final Context context)
    throws EvalException
  {
    final Value value = eval(context);
    if (value instanceof RangeValue) {
      return (RangeValue) value;
    } else {
      throw new TypeMismatchException(this, value, TYPE_RANGE);
    }
  }

  /**
   * Evaluates this expression to a boolean.
   * This method calls {@link #eval(Context) eval()} to evaluate this
   * expression to an integer, and then converts it to a boolean value.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return <CODE>true</CODE> if the result of evaluation was a non-zero
   *         integer, <CODE>false</CODE> if the result was zero.
   * @throws TypeMismatchException to indicate that the expression does
   *                     not evaluate to an integer.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context.
   */
  public boolean evalToBoolean(final Context context)
    throws EvalException
  {
    final IntValue value = evalToInt(context);
    return value.getValue() != 0;
  }

  /**
   * Evaluates this expression to a name.
   * This method calls {@link #eval(Context) eval()} to evaluate this
   * expression, checks whether the result is an identifier, and returns
   * the identifier's name.
   * @param  context     The binding context to be used to look up the
   *                     values for identifiers.
   * @return The name of the identifier obtained from evaluation.
   * @throws TypeMismatchException to indicate that the expression does
   *                     not evaluate to an identifier.
   * @throws EvalException to indicate that the expression cannot be
   *                     evaluated in the given context.
   */
  public String evalToName(final Context context)
    throws EvalException
  {
    throw new TypeMismatchException(this, TYPE_NAME);
  }


  //#########################################################################
  //# Type Names
  /**
   * Gets a readable representation of a type mask.
   * @param  mask        A bit mask composed of the values {@link #TYPE_INT},
   *                     {@link #TYPE_ATOM}, {@link #TYPE_RANGE},
   *                     and {@link #TYPE_NAME}.
   * @return A string that represents the type mask and can be presented
   *         to the user.
   */
  public static String getTypeName(final int mask)
  {
    final StringBuffer buffer = new StringBuffer();
    boolean first = true;
    if ((mask & TYPE_INT) != 0) {
      first = false;
      buffer.append(TYPENAME_INT);
    }
    if ((mask & TYPE_ATOM) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or");
      }
      buffer.append(TYPENAME_ATOM);
    }
    if ((mask & TYPE_RANGE) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or");
      }
      buffer.append(TYPENAME_RANGE);
    }
    if ((mask & TYPE_NAME) != 0) {
      if (first) {
	first = false;
      } else {
	buffer.append(" or");
      }
      buffer.append(TYPENAME_NAME);
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Comparing
  abstract int getOrderIndex();


  //#########################################################################
  //# Class Constants
  /**
   * A type bit indicating an expression of integer type.
   * @see    #getResultTypes()
   */
  public static final int TYPE_INT = 1;
  /**
   * A type bit indicating an expression of atom type.
   * An atom is a simple identifier without indexes, as it may occur as
   * a value in an enumeration type.
   * @see    #getResultTypes()
   */
  public static final int TYPE_ATOM = 2;
  /**
   * A type bit indicating an expression of range type.
   * A range is a list of values, either an integer range such as
   * <CODE>1..10</CODE> or an enumerated range such as
   * <CODE>{a,&nbsp;b,&nbsp;c}</CODE>.
   * @see    #getResultTypes()
   */
  public static final int TYPE_RANGE = 4;
  /**
   * A type bit indicating an expression representing a name.
   * Name expressions are used to identify events or other module
   * components. They may be simple identifiers or identifiers with
   * array indexes.
   * @see    #getResultTypes()
   */
  public static final int TYPE_NAME = 8;
  /**
   * All possible type bits.
   * This constant represents a disjunction of all possible type bits.
   * It can used to bypass type checking.
   */
  public static final int TYPE_ANY =
    TYPE_INT | TYPE_ATOM | TYPE_RANGE | TYPE_NAME;


  static final int ORDERINDEX_INTCONSTANT = 0;
  static final int ORDERINDEX_IDENTIFIER = 1;
  static final int ORDERINDEX_UNARYEXPRESSION = 2;
  static final int ORDERINDEX_BINARYEXPRESSION = 3;
  static final int ORDERINDEX_ENUMSET = 4;


  private static final String TYPENAME_INT = "INTEGER";
  private static final String TYPENAME_ATOM = "ATOM";
  private static final String TYPENAME_RANGE = "RANGE";
  private static final String TYPENAME_NAME = "NAME";
  
}
