//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFASimpleExpressionEvaluator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * @author Robi Malik
 */

class EFASimpleExpressionEvaluator
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  EFASimpleExpressionEvaluator(final ModuleProxyFactory factory,
                               final CompilerOperatorTable optable,
                               final EFAVariableMap varmap)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mVariableMap = varmap;

    mBinaryEvaluatorMap = new HashMap<BinaryOperator,BinaryEvaluator>(32);
    mBinaryEvaluatorMap.put(optable.getAndOperator(),
                            new BinaryAndEvaluator());
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
    mBinaryEvaluatorMap.put(optable.getNotEqualsOperator(),
                            new BinaryNotEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrOperator(),
                            new BinaryOrEvaluator());

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
  CompiledRange evalRange(final SimpleExpressionProxy expr,
                          final BindingContext context)
    throws EvalException
  {
    try {
      mContext = context;
      return evalRange(expr);
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

  CompiledIntRange evalIntRange(final SimpleExpressionProxy expr,
                                final BindingContext context)
    throws EvalException
  {
    final CompiledRange range = evalRange(expr, context);
    return checkIntRange(expr, range);
  }

  CompiledIntRange evalBooleanRange(final SimpleExpressionProxy expr,
                                    final BindingContext context)
    throws EvalException
  {
    final CompiledRange range = evalRange(expr, context);
    return checkBooleanRange(expr, range);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledRange evalRange(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy bound = mContext.getBoundExpression(expr);
    if (bound == null) {
      return (CompiledRange) expr.acceptVisitor(this);
    } else {
      return evalRange(bound);
    }
  }

  private BinaryEvaluator getEvaluator(final BinaryOperator op)
    throws UnsupportedOperatorException
  {
    final BinaryEvaluator evaluator = mBinaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op);
    }
  }

  private UnaryEvaluator getEvaluator(final UnaryOperator op)
    throws UnsupportedOperatorException
  {
    final UnaryEvaluator evaluator = mUnaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op);
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
    if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      if (intrange.getLower() >= 0 && intrange.getUpper() <= 1) {
        return intrange;
      }
    }
    throw new TypeMismatchException(expr, "BOOLEAN");
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public CompiledRange visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy leftexpr = expr.getLeft();
      final CompiledRange leftrange = evalRange(leftexpr);
      final SimpleExpressionProxy rightexpr = expr.getRight();
      final CompiledRange rightrange = evalRange(rightexpr);
      final BinaryOperator op = expr.getOperator();
      final BinaryEvaluator evaluator = getEvaluator(op);
      return evaluator.eval(leftexpr, leftrange, rightexpr, rightrange);
    } catch (final EvalException exception) {
      exception.provideLocation(expr);
      throw wrap(exception);
    }
  }


  public CompiledRange visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    // TODO: Evaluate indexes in qualified and indexed identifiers!
    final EFAVariable var = mVariableMap.getVariable(ident);
    if (var != null) {
      return var.getRange();
    } else if (mContext.isEnumAtom(ident)) {
      final List<IdentifierProxy> list = Collections.singletonList(ident);
      return new CompiledEnumRange(list);
    } else {
      final Throwable exception = new UndefinedIdentifierException(ident);
      throw wrap(exception);
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
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      final CompiledRange subrange = evalRange(subterm);
      final UnaryOperator op = expr.getOperator();
      final UnaryEvaluator evaluator = getEvaluator(op);
      return evaluator.eval(subterm, subrange);
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
    abstract CompiledRange eval(SimpleExpressionProxy expr,
                                CompiledRange range)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryIntEvaluator
  private abstract class UnaryIntEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy expr,
                          final CompiledRange range)
      throws EvalException
    {
      final CompiledIntRange intrange = checkIntRange(expr, range);
      return eval(expr, intrange);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy expr,
                                   CompiledIntRange range)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryBooleanEvaluator
  private abstract class UnaryBooleanEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy expr,
                          final CompiledRange range)
      throws EvalException
    {
      final CompiledIntRange intrange = checkBooleanRange(expr, range);
      return eval(expr, intrange);
    }

    abstract CompiledIntRange eval(SimpleExpressionProxy expr,
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
  //# Inner Class UnaryMinusEvaluator
  private class UnaryMinusEvaluator extends UnaryIntEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy expr,
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
    CompiledRange eval(final SimpleExpressionProxy expr,
                       final CompiledRange range)
      throws TypeMismatchException
    {
      if (mVariableMap.getVariable(expr) != null) {
        return range;
      } else {
        throw new TypeMismatchException(expr, "VARIABLE");
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryNotEvaluator
  private class UnaryNotEvaluator extends UnaryBooleanEvaluator {

    //#######################################################################
    //# Evaluation
    CompiledIntRange eval(final SimpleExpressionProxy expr,
                          final CompiledIntRange range)
    {
      if (range.equals(FALSE_RANGE)) {
        return TRUE_RANGE;
      } else if (range.equals(TRUE_RANGE)) {
        return TRUE_RANGE;
      } else {
        return BOOLEAN_RANGE;
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final EFAVariableMap mVariableMap;
  private final Map<BinaryOperator,BinaryEvaluator> mBinaryEvaluatorMap;
  private final Map<UnaryOperator,UnaryEvaluator> mUnaryEvaluatorMap;

  private BindingContext mContext;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);
  private static final CompiledIntRange FALSE_RANGE =
    new CompiledIntRange(0, 0);
  private static final CompiledIntRange TRUE_RANGE =
    new CompiledIntRange(1, 1);

}
