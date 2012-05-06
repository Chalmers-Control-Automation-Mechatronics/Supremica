//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   RangeEstimator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>An evaluation component to estimate the range of simple expressions.</P>
 *
 * <P>Given the ranges of variables in a {@link VariableContext}, the range
 * evaluator propagates these range through all operators to give an
 * estimate of the range of an expression. For example, if <CODE>x</CODE>
 * and&nbsp;<CODE>y</CODE> are variables with range&nbsp;<CODE>1..3</CODE>,
 * then the estimated range of <CODE>x+y</CODE>
 * is&nbsp;<CODE>2..6</CODE>.</P>
 *
 * <P>The range estimator does not make any attempt to evaluate array
 * indexes. The range of an indexed expression such
 * as&nbsp;<CODE>a[2]</CODE> is looked up in the context, by no attempt is
 * made to evaluate <CODE>i</CODE> in&nbsp;<CODE>a[i]</CODE>. Likewise,
 * primed (next-state) subexpressions are not evaluated.</P>
 *
 * <P>The range estimate of an expression with undefined or
 * variable-indexed identifiers is returned as&nbsp;<CODE>null</CODE>,
 * i.e., unknown.</P>
 *
 * @author Robi Malik
 */

public class RangeEstimator
  extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public RangeEstimator(final CompilerOperatorTable optable)
  {
    mBinaryEvaluatorMap = new HashMap<BinaryOperator,BinaryEvaluator>(32);
    mBinaryEvaluatorMap.put(optable.getAndOperator(),
                            new BinaryAndEvaluator());
    mBinaryEvaluatorMap.put(optable.getDivideOperator(),
                            new BinaryDivideEvaluator());
    mBinaryEvaluatorMap.put(optable.getEqualsOperator(),
                            new BinaryEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getGreaterEqualsOperator(),
                            new BinaryGreaterEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getGreaterThanOperator(),
                            new BinaryGreaterThanEvaluator());
    mBinaryEvaluatorMap.put(optable.getLessEqualsOperator(),
                            new BinaryLessEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getLessThanOperator(),
                            new BinaryLessThanEvaluator());
    mBinaryEvaluatorMap.put(optable.getMinusOperator(),
                            new BinaryMinusEvaluator());
    mBinaryEvaluatorMap.put(optable.getModuloOperator(),
                            new BinaryModuloEvaluator());
    mBinaryEvaluatorMap.put(optable.getNotEqualsOperator(),
                            new BinaryNotEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrOperator(),
                            new BinaryOrEvaluator());
    mBinaryEvaluatorMap.put(optable.getPlusOperator(),
                            new BinaryPlusEvaluator());
    mBinaryEvaluatorMap.put(optable.getTimesOperator(),
                            new BinaryTimesEvaluator());

    mUnaryEvaluatorMap = new HashMap<UnaryOperator,UnaryEvaluator>(8);
    mUnaryEvaluatorMap.put(optable.getNotOperator(),
                           new UnaryNotEvaluator());
    mUnaryEvaluatorMap.put(optable.getNextOperator(),
                           new UnaryNextEvaluator());
    mUnaryEvaluatorMap.put(optable.getUnaryMinusOperator(),
                           new UnaryMinusEvaluator());
  }


  //#########################################################################
  //# Invocation
  /**
   * Computes the estimated range of an expression in a given context.
   * @param  expr     The expression to be evaluated.
   * @param  context  The context that provides bindings to identifiers.
   * @return The range of possible values of the given expression,
   *         or <CODE>null</CODE> if the range cannot be estimated due
   *         to undefined identifiers.
   *         The result may be an over-approximation, meaning that not
   *         all values in the returned range can actually be obtained
   *         by assignment of values to variables.
   * @throws EvalException to indicate a problem while evaluation the
   *                  expression. Exceptions may be thrown even for values
   *                  that cannot actually be obtained, e.g., when
   *                  a Boolean operator is given an integer argument
   *                  whose estimated range contains values other than
   *                  0 or&nbsp;1.
   */
  public CompiledRange estimateRange
    (final SimpleExpressionProxy expr,
     final VariableContext context)
    throws EvalException
  {
    try {
      mContext = context;
      return process(expr);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mContext = null;
    }
  }

  public CompiledIntRange estimateIntRange
    (final SimpleExpressionProxy expr,
     final VariableContext context)
    throws EvalException
  {
    final CompiledRange range = estimateRange(expr, context);
    return checkIntRange(expr, range);
  }

  public CompiledIntRange estimateBooleanRange
    (final SimpleExpressionProxy expr,
     final VariableContext context)
    throws EvalException
  {
    final CompiledRange range = estimateRange(expr, context);
    return checkBooleanRange(expr, range);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledRange process(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy bound = mContext.getBoundExpression(expr);
    if (bound == null || bound == expr) {
      return (CompiledRange) expr.acceptVisitor(this);
    } else {
      return process(bound);
    }
  }

  private BinaryEvaluator getEvaluator(final BinaryOperator op)
    throws UnsupportedOperatorException
  {
    final BinaryEvaluator evaluator = mBinaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op, " in guard expressions");
    }
  }

  private UnaryEvaluator getEvaluator(final UnaryOperator op)
    throws UnsupportedOperatorException
  {
    final UnaryEvaluator evaluator = mUnaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op, " in guard expressions");
    }
  }


  //#########################################################################
  //# Type Checking
  private CompiledIntRange checkIntRange(final SimpleExpressionProxy expr,
                                         final CompiledRange range)
    throws TypeMismatchException
  {
    if (range instanceof CompiledIntRange) {
      return (CompiledIntRange) range;
    } else {
      throw new TypeMismatchException(expr, "INTEGER");
    }
  }

  private CompiledIntRange checkBooleanRange(final SimpleExpressionProxy expr,
                                             final CompiledRange range)
    throws TypeMismatchException
  {
    if (range == null) {
      return null;
    } else if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      if (intrange.getLower() >= 0 && intrange.getUpper() <= 1) {
        return intrange;
      }
    }
    throw new TypeMismatchException(expr, "BOOLEAN");
  }

  private IdentifierProxy checkIdentifier(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IdentifierProxy) {
      return (IdentifierProxy) expr;
    } else {
      throw new TypeMismatchException(expr, "IDENTIFIER");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public CompiledRange visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy leftexpr = expr.getLeft();
      final CompiledRange leftrange = process(leftexpr);
      if (leftrange == null) {
        return null;
      }
      final SimpleExpressionProxy rightexpr = expr.getRight();
      final CompiledRange rightrange = process(rightexpr);
      if (rightrange == null) {
        return null;
      }
      final BinaryOperator op = expr.getOperator();
      final BinaryEvaluator evaluator = getEvaluator(op);
      return evaluator.eval(leftexpr, leftrange, rightexpr, rightrange);
    } catch (final EvalException exception) {
      exception.provideLocation(expr);
      throw wrap(exception);
    }
  }


  public CompiledRange visitIdentifierProxy(final IdentifierProxy ident)
  {
    if (mContext.isEnumAtom(ident)) {
      final List<IdentifierProxy> list = Collections.singletonList(ident);
      return new CompiledEnumRange(list);
    } else {
      return mContext.getVariableRange(ident);
    }
  }

  public CompiledIntRange visitIntConstantProxy
    (final IntConstantProxy intconst)
  {
    final int value = intconst.getValue();
    return new CompiledIntRange(value, value);
  }

  public CompiledRange visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    try {
      final UnaryOperator op = expr.getOperator();
      final UnaryEvaluator evaluator = getEvaluator(op);
      return evaluator.eval(expr);
    } catch (final EvalException exception) {
      exception.provideLocation(expr);
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Inner Class BinaryEvaluator
  private abstract class BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    abstract CompiledRange eval(SimpleExpressionProxy leftexpr,
                                CompiledRange leftrange,
                                SimpleExpressionProxy rightexpr,
                                CompiledRange rightrange)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryIntEvaluator
  private abstract class BinaryIntEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledRange rightrange)
      throws EvalException
    {
      final CompiledIntRange leftint = checkIntRange(leftexpr, leftrange);
      final CompiledIntRange rightint = checkIntRange(rightexpr, rightrange);
      return eval(leftexpr, leftint, rightexpr, rightint);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy leftexpr,
                                   CompiledIntRange leftrange,
                                   SimpleExpressionProxy rightexpr,
                                   CompiledIntRange rightrange)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryBooleanEvaluator
  private abstract class BinaryBooleanEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledRange rightrange)
      throws EvalException
    {
      final CompiledIntRange leftint = checkBooleanRange(leftexpr, leftrange);
      final CompiledIntRange rightint =
        checkBooleanRange(rightexpr, rightrange);
      return eval(leftexpr, leftint, rightexpr, rightint);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy leftexpr,
                                   CompiledIntRange leftrange,
                                   SimpleExpressionProxy rightexpr,
                                   CompiledIntRange rightrange)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryEvaluator
  private abstract class UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    abstract CompiledRange eval(UnaryExpressionProxy expr)
      throws VisitorException;

  }


  //#########################################################################
  //# Inner Class UnaryRangeEvaluator
  private abstract class UnaryRangeEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledRange eval(UnaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        final CompiledRange subrange = process(subterm);
        if (subrange == null) {
          return null;
        }
        return eval(subterm, subrange);
      } catch (final EvalException exception) {
        exception.provideLocation(expr);
        throw wrap(exception);
      }
    }

    abstract CompiledRange eval(SimpleExpressionProxy subterm,
                                CompiledRange range)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryIntEvaluator
  private abstract class UnaryIntEvaluator extends UnaryRangeEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy subterm,
                          final CompiledRange range)
      throws EvalException
    {
      final CompiledIntRange intrange = checkIntRange(subterm, range);
      return eval(subterm, intrange);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy subterm,
                                   CompiledIntRange range)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryBooleanEvaluator
  private abstract class UnaryBooleanEvaluator extends UnaryRangeEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy subterm,
                          final CompiledRange range)
      throws EvalException
    {
      final CompiledIntRange intrange = checkBooleanRange(subterm, range);
      return eval(subterm, intrange);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy subterm,
                                   CompiledIntRange range)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryAndEvaluator
  private class BinaryAndEvaluator extends BinaryBooleanEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.equals(FALSE_RANGE) ||
          rightrange.equals(FALSE_RANGE)) {
        return FALSE_RANGE;
      } else if (leftrange.equals(TRUE_RANGE) &&
                 rightrange.equals(TRUE_RANGE)) {
        return TRUE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryDivideEvaluator
  private class BinaryDivideEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      int leftlower = leftrange.getLower();
      int leftupper = leftrange.getUpper();
      int rightlower = rightrange.getLower();
      int rightupper = rightrange.getUpper();
      if (rightlower == 0) {
        rightlower = 1;
      }
      if (rightupper == 0) {
        rightupper = -1;
      }
      if (leftlower > leftupper || rightlower > rightupper) {
        return EMPTY_RANGE;
      }
      if (leftupper < 0 ||
          rightupper < 0 && leftlower < 0 && leftupper > 0) {
        int aux = leftlower;
        leftupper = -leftlower;
        leftlower = -aux;
        aux = rightlower;
        rightupper = -rightlower;
        rightlower = -aux;
      }
      final int lower;
      final int upper;
      if (leftlower >= 0) {
        if (rightlower > 0) {
          lower = leftlower / rightupper;
          upper = leftupper /rightlower;
        } else if (rightupper < 0) {
          lower = leftupper / rightupper;
          upper = leftlower / rightlower;
        } else {
          lower = -leftupper;
          upper = leftupper;
        }
      } else { // leftlower < 0 && leftupper > 0
        if (rightlower > 0) {
          lower = leftlower / rightlower;
          upper = leftupper /rightlower;
        } else { // rightlower < 0 && rightupper > 0
          upper = -leftlower > leftupper ? -leftlower : leftupper;
          lower = -upper;
        }        
      }
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class BinaryEqualsEvaluator
  private class BinaryEqualsEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledRange rightrange)
    {
      if (leftrange.size() == 1 &&
          rightrange.size() == 1 &&
          leftrange.equals(rightrange)) {
        return TRUE_RANGE;
      } else if (leftrange.intersects(rightrange)) {
        return BOOLEAN_RANGE;
      } else {
        return FALSE_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryGreaterEqualsEvaluator
  private class BinaryGreaterEqualsEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.getLower() >= rightrange.getUpper()) {
        return TRUE_RANGE;
      } else if (leftrange.getUpper() < rightrange.getLower()) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryGreaterThanEvaluator
  private class BinaryGreaterThanEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.getLower() > rightrange.getUpper()) {
        return TRUE_RANGE;
      } else if (leftrange.getUpper() <= rightrange.getLower()) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryLessEqualsEvaluator
  private class BinaryLessEqualsEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.getUpper() <= rightrange.getLower()) {
        return TRUE_RANGE;
      } else if (leftrange.getLower() > rightrange.getUpper()) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryLessThanEvaluator
  private class BinaryLessThanEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.getUpper() < rightrange.getLower()) {
        return TRUE_RANGE;
      } else if (leftrange.getLower() >= rightrange.getUpper()) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryMinusEvaluator
  private class BinaryMinusEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      final int lower = leftrange.getLower() - rightrange.getUpper();
      final int upper = leftrange.getUpper() - rightrange.getLower();
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class BinaryModuloEvaluator
  private class BinaryModuloEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      int leftlower = leftrange.getLower();
      int leftupper = leftrange.getUpper();
      int rightlower = rightrange.getLower();
      int rightupper = rightrange.getUpper();
      if (rightlower == 0) {
        rightlower = 1;
      }
      if (rightupper == 0) {
        rightupper = -1;
      }
      if (leftlower > leftupper || rightlower > rightupper) {
        return EMPTY_RANGE;
      }
      if (rightlower < 0) {
        if (rightupper < 0) {
          final int aux = rightlower;
          rightupper = -rightlower;
          rightlower = -aux;
        } else {
          if (-rightlower > rightupper) {
            rightupper = -rightlower;
          }
          rightlower = 1;
        }
      }
      if (leftlower >= 0) {
        if (leftupper < rightupper) {
          return leftrange;
        } else if (leftupper - leftlower + 1 >= rightupper) {
          return new CompiledIntRange(0, rightupper - 1);
        }
      } else if (leftupper <= 0) {
        if (-leftlower < rightupper) {
          return leftrange;
        } else if (leftupper - leftlower + 1 >= rightupper) {
          return new CompiledIntRange(1 - rightupper, 0);
        }
      }
      int lower = leftlower % rightlower;
      int upper = lower;
      for (int dividend = leftlower; dividend <= leftupper; dividend++) {
        for (int divisor = rightlower; divisor <= rightupper; divisor++) {
          final int mod = dividend % divisor;
          if (mod < lower) {
            lower = mod;
          } else if (mod > upper) {
            upper = mod;
          }
        }
      }
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class BinaryNotEqualsEvaluator
  private class BinaryNotEqualsEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledRange rightrange)
    {
      if (leftrange.size() == 1 &&
          rightrange.size() == 1 &&
          leftrange.equals(rightrange)) {
        return FALSE_RANGE;
      } else if (leftrange.intersects(rightrange)) {
        return BOOLEAN_RANGE;
      } else {
        return TRUE_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryOrEvaluator
  private class BinaryOrEvaluator extends BinaryBooleanEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      if (leftrange.equals(TRUE_RANGE) ||
          rightrange.equals(TRUE_RANGE)) {
        return TRUE_RANGE;
      } else if (leftrange.equals(FALSE_RANGE) &&
                 rightrange.equals(FALSE_RANGE)) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryPlusEvaluator
  private class BinaryPlusEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      final int lower = leftrange.getLower() + rightrange.getLower();
      final int upper = leftrange.getUpper() + rightrange.getUpper();
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class BinaryTimesEvaluator
  private class BinaryTimesEvaluator extends BinaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy leftexpr,
                          final CompiledIntRange leftrange,
                          final SimpleExpressionProxy rightexpr,
                          final CompiledIntRange rightrange)
    {
      final int leftlower = leftrange.getLower();
      final int leftupper = leftrange.getUpper();
      final int rightlower = rightrange.getLower();
      final int rightupper = rightrange.getUpper();
      if (leftlower > leftupper || rightlower > rightupper) {
        return EMPTY_RANGE;
      }
      int lower = leftlower * rightlower;
      int upper = lower;
      final int lu = leftlower * rightupper;
      if (lu < lower) {
        lower = lu;
      } else if (lu > upper) {
        upper = lu;
      }
      final int ul = leftupper * rightlower;
      if (ul < lower) {
        lower = ul;
      } else if (ul > upper) {
        upper = ul;
      }
      final int uu = leftupper * rightupper;
      if (uu < lower) {
        lower = uu;
      } else if (uu > upper) {
        upper = uu;
      }
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class UnaryMinusEvaluator
  private class UnaryMinusEvaluator extends UnaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy subterm,
                          final CompiledIntRange range)
    {
      final int lower = -range.getUpper();
      final int upper = -range.getLower();
      return new CompiledIntRange(lower, upper);
    }

  }


  //#########################################################################
  //# Inner Class UnaryNextEvaluator
  private class UnaryNextEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledRange eval(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        final IdentifierProxy ident = checkIdentifier(subterm);
        if (mContext.isEnumAtom(ident)) {
          final List<IdentifierProxy> list = Collections.singletonList(ident);
          return new CompiledEnumRange(list);
        } else {
          return mContext.getVariableRange(expr);
        }
      } catch (final EvalException exception) {
        exception.provideLocation(expr);
        throw wrap(exception);
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryNotEvaluator
  private class UnaryNotEvaluator extends UnaryBooleanEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy subterm,
                          final CompiledIntRange range)
    {
      if (range.equals(FALSE_RANGE)) {
        return TRUE_RANGE;
      } else if (range.equals(TRUE_RANGE)) {
        return FALSE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final Map<BinaryOperator,BinaryEvaluator> mBinaryEvaluatorMap;
  private final Map<UnaryOperator,UnaryEvaluator> mUnaryEvaluatorMap;

  private VariableContext mContext;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);
  private static final CompiledIntRange FALSE_RANGE =
    new CompiledIntRange(0, 0);
  private static final CompiledIntRange TRUE_RANGE =
    new CompiledIntRange(1, 1);
  private static final CompiledIntRange EMPTY_RANGE =
    new CompiledIntRange(0, -1);

}
