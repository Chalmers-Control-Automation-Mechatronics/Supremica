//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplifier sub-task to normalise sums.</P>
 *
 * <P>The sum simplifier is invoked by the {@link SimpleExpressionCompiler}
 * to normalise expressions consisting of addition and subtraction. Such
 * sums are normalised by ensuring left associativity and ordering terms
 * according the the standard expression ordering ({@link ExpressionComparator}).
 * Constants are aggregated and terms that appears both positively and
 * negatively are removed. Subterms that use the unary minus operator are
 * also included in the normalisation process. The sum simplifier does not
 * investigate multiplicative subterms, nor does it aggregate subterms that
 * appear more than once into multiplicative terms.</P>
 *
 * <P>The sum simplifier includes support for the normalisation of equations
 * that involve sums, which is used by the {@link ConstraintPropagator}.</P>
 *
 * @author Robi Malik
 */

public class SumSimplifier extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a sum simplifier that does not recursively simplify subterms.
   * @param  factory  Factory used to create new expressions.
   */
  public SumSimplifier(final ModuleProxyFactory factory)
  {
    this(factory, null);
  }

  /**
   * Creates a sum simplifier.
   * @param  factory     Factory used to create new expressions.
   * @param  simplifier  Compiler used to simplify any subterms outside
   *                     of the scope of sum normalisation. This argument
   *                     can be set to <CODE>null</CODE> to disable
   *                     recursive simplification and only perform sum
   *                     normalisation.
   */
  public SumSimplifier(final ModuleProxyFactory factory,
                       final SimpleExpressionCompiler simplifier)
  {
    mFactory = factory;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mEquality = new ModuleEqualityVisitor(false);
    mSummandComparator = new ExpressionComparator(mOperatorTable);
    mSimplifier = simplifier;
    mPositiveTerms = new ArrayList<>();
    mNegativeTerms = new ArrayList<>();
    mCountingMap = new ProxyAccessorHashMap<>(mEquality);
  }


  //#########################################################################
  //# Invocation
  /**
   * Normalises the given sum.
   * @param  expr  The expression to be normalised, which should be a
   *               unary or binary expression with operator + or -.
   * @return The simplified expression. If the expression could not be
   *         simplified, then the argument is returned.
   */
  public SimpleExpressionProxy normaliseSum(final SimpleExpressionProxy expr)
    throws EvalException
  {
    try {
      setUp();
      // Collect positive and negative literals,
      // checking whether the structure could be already normalised.
      expr.acceptVisitor(this);
      if (mAlreadyNormalised) {
        if (mEquality.intersects(mPositiveTerms, mNegativeTerms)) {
          // If the visitor thinks the structure could be normalised,
          // but there is a term that appears both positive and negative,
          // then it is still not normalised.
          mAlreadyNormalised = false;
        } else {
          // Otherwise the sum is normalised and we can return it unchanged.
          return expr;
        }
      }
      // Otherwise we must create a new expression.
      recordBasicCounts();
      recordConstantCount();
      final List<SimpleExpressionProxy> orderedLiterals = getOrderedLiterals();
      return createNormalisedSum(orderedLiterals);
    } catch (final VisitorException exception) {
      if (exception.getCause() instanceof EvalException) {
        throw (EvalException) exception.getCause();
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      tearDown();
    }
  }

  /**
   * Checks whether an equation is in normalised form. This method is
   * used by the {@link ConstraintPropagator} to determine whether an
   * equation of the form <CODE>x = <I>sum</I></CODE> is in normalised
   * form. The equation is normalised, if the right-hand side <I>sum</I>
   * is normalised, and the left-hand-side <I>x</I> is either a constant
   * and there are no constants in the right-hand side, or precedes all
   * terms in the right hand side based on the given ordering.
   * @param  lhs                The left-hand side <I>x</I> of the equation.
   * @param  rhs                The right-hand side <I>sum</I> of the
   *                            equation.
   * @param  leadTermComparator The comparator to determine the lead term.
   * @return <CODE>true</CODE> if the equation is already normalised,
   *         <CODE>false</CODE> otherwise. A return value of <CODE>true</CODE>
   *         means that a call to {@link #normaliseEquation(SimpleExpressionProxy,SimpleExpressionProxy,BinaryOperator,Comparator)
   *         normaliseEquation()} will return a changed equation.
   */
  public boolean isNormalisedEquation
    (final SimpleExpressionProxy lhs,
     final SimpleExpressionProxy rhs,
     final Comparator<SimpleExpressionProxy> leadTermComparator)
    throws EvalException
  {
    try {
      setUp();
      rhs.acceptVisitor(this);
      if (!mAlreadyNormalised) {
        return false;
      } else if (lhs instanceof IntConstantProxy && mSumOfConstants != 0) {
        return false;
      }
      recordBasicCounts();
      for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,Integer>
           entry : mCountingMap.entrySet()) {
        if (Math.abs(entry.getValue()) != 1) {
          return false;
        }
        final ProxyAccessor<SimpleExpressionProxy> accessor = entry.getKey();
        final SimpleExpressionProxy expr = accessor.getProxy();
        if (leadTermComparator.compare(expr, lhs) <= 0) {
          return false;
        }
      }
      return true;
    } catch (final VisitorException exception) {
      if (exception.getCause() instanceof EvalException) {
        throw (EvalException) exception.getCause();
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      tearDown();
    }
  }

  /**
   * Normalises an equation. An equation of the form <CODE><I>lhs</I> =
   * <I>rhs</I></CODE>, where the compared terms <I>lhs</I> and <I>rhs</I>
   * are sums, is normalised by extracting the <I>lead term</I>, which is
   * either a constant or the smallest term in the given ordering that would
   * appear exactly once in the normalised equation, and rewriting the
   * equation into the form <CODE>x = <I>new-rhs</I></CODE>.
   * @param  lhs                The left-hand side of the equation.
   * @param  rhs                The right-hand side of the equation.
   * @param  op                 The equation operator, which should be either
   *                            <CODE>==</CODE> or <CODE>!=</CODE>.
   *                            Greater-than and less-than are not yet
   *                            fully supported.
   * @param  leadTermComparator The comparator to determine the lead term.
   * @return The normalised sum. This method always returns a new object;
   *         please use {@link #isNormalisedEquation(SimpleExpressionProxy,SimpleExpressionProxy,Comparator)
   *         isNormalisedEquation()} to check whether normalisation will
   *         result in change.
   */
  public BinaryExpressionProxy normaliseEquation
    (final SimpleExpressionProxy lhs,
     final SimpleExpressionProxy rhs,
     final BinaryOperator op,
     final Comparator<SimpleExpressionProxy> leadTermComparator)
    throws EvalException
  {
    try {
      setUp();
      mCurrentSign = 1;
      lhs.acceptVisitor(this);
      mCurrentSign = -1;
      rhs.acceptVisitor(this);
      recordBasicCounts();
      recordConstantCount();
      final SimpleExpressionProxy leadTerm = createLeadTerm(leadTermComparator);
      final List<SimpleExpressionProxy> orderedLiterals = getOrderedLiterals();
      final SimpleExpressionProxy normalisedSum =
        createNormalisedSum(orderedLiterals);
      return mFactory.createBinaryExpressionProxy(op, leadTerm, normalisedSum);
    } catch (final VisitorException exception) {
      if (exception.getCause() instanceof EvalException) {
        throw (EvalException) exception.getCause();
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Set up and Tear down
  private void setUp()
  {
    mCurrentSign = 1;
    mLiteralExpected = false;
    mPreviousLiteral = null;
    mAlreadyNormalised = true;
    mPositiveTerms.clear();
    mNegativeTerms.clear();
    mConstant = null;
    mSumOfConstants = 0;
  }

  private void tearDown()
  {
    mPositiveTerms.clear();
    mNegativeTerms.clear();
    mConstant = null;
    mCountingMap.clear();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final BinaryOperator op = expr.getOperator();
    if (op == mOperatorTable.getPlusOperator() ||
        op == mOperatorTable.getMinusOperator()) {
      if (mLiteralExpected) {
        setNotAlreadyNormalised();
      }
      final SimpleExpressionProxy lhs = expr.getLeft();
      lhs.acceptVisitor(this);
      final int oldSign = mCurrentSign;
      try {
        if (op == mOperatorTable.getMinusOperator()) {
          mCurrentSign = -mCurrentSign;
        }
        mLiteralExpected = true;
        final SimpleExpressionProxy rhs = expr.getRight();
        return rhs.acceptVisitor(this);
      } finally {
        mCurrentSign = oldSign;
      }
    } else {
      return visitSimpleExpressionProxy(expr);
    }
  }

  @Override
  public Object visitIntConstantProxy(final IntConstantProxy constant)
  {
    if (constant.getValue() == 0) {
      setNotAlreadyNormalised();
    } else if (mConstant == null) {
      mConstant = constant;
      mSumOfConstants = mCurrentSign * constant.getValue();
      checkAlreadyNormalised(constant);
    } else {
      setNotAlreadyNormalised();
      mSumOfConstants += mCurrentSign * constant.getValue();
    }
    return null;
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    if (mSimplifier == null) {
      recordTerm(expr);
      checkAlreadyNormalised(expr);
    } else {
      final SimpleExpressionProxy simplified =
        mSimplifier.invokeSimplificationVisitor(expr);
      if (mSimplifier.isAtomicValue(simplified)) {
        try {
          mSimplifier.getIntValue(simplified);
        } catch (final TypeMismatchException exception) {
          throw wrap(exception);
        }
      }
      if (simplified == expr) {
        recordTerm(simplified);
        checkAlreadyNormalised(simplified);
      } else {
        setNotAlreadyNormalised();
        final SimpleExpressionCompiler simplifier = mSimplifier;
        try {
          mSimplifier = null;
          simplified.acceptVisitor(this);
        } finally {
          mSimplifier = simplifier;
        }
      }
    }
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final UnaryOperator op = expr.getOperator();
    if (op == mOperatorTable.getUnaryMinusOperator()) {
      final int oldSign = mCurrentSign;
      try {
        mCurrentSign = -mCurrentSign;
        if (mLiteralExpected) {
          setNotAlreadyNormalised();
        } else {
          mLiteralExpected = true;
        }
        final SimpleExpressionProxy subTerm = expr.getSubTerm();
        return subTerm.acceptVisitor(this);
      } finally {
        mCurrentSign = oldSign;
      }
    } else {
      return visitSimpleExpressionProxy(expr);
    }
  }


  //#########################################################################
  //# Auxiliary Methods for Pass 1 - Counting
  private void recordTerm(final SimpleExpressionProxy expr)
  {
    if (mCurrentSign > 0) {
      mPositiveTerms.add(expr);
    } else {
      mNegativeTerms.add(expr);
    }
    checkAlreadyNormalised(expr);
  }

  private void checkAlreadyNormalised(final SimpleExpressionProxy expr)
  {
    if (mAlreadyNormalised) {
      if (mPreviousLiteral == null) {
        mPreviousLiteral = expr;
      } else if (mSummandComparator.compare(mPreviousLiteral, expr) > 0) {
        setNotAlreadyNormalised();
      } else {
        mPreviousLiteral = expr;
      }
    }
  }

  private void setNotAlreadyNormalised()
  {
    mAlreadyNormalised = false;
    mPreviousLiteral = null;
  }


  //#########################################################################
  //# Auxiliary Methods for Pass 2 - Output Creation
  private void recordBasicCounts()
  {
    mCountingMap.clear();
    for (final SimpleExpressionProxy expr : mPositiveTerms) {
      recordCount(expr, 1);
    }
    for (final SimpleExpressionProxy expr : mNegativeTerms) {
      recordCount(expr, -1);
    }
  }

  private void recordConstantCount()
  {
    if (mSumOfConstants != 0) {
      final int absSum = Math.abs(mSumOfConstants);
      final int sign = mSumOfConstants > 0 ? 1 : -1;
      final IntConstantProxy constant = getConstant(absSum);
      recordCount(constant, sign);
    }
  }

  private void recordCount(final SimpleExpressionProxy expr, int value)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mCountingMap.createAccessor(expr);
    final Integer found = mCountingMap.get(accessor);
    if (found != null) {
      value += found;
    }
    mCountingMap.put(accessor, value);
  }


  private SimpleExpressionProxy createLeadTerm
    (final Comparator<SimpleExpressionProxy> comparator)
  {
    SimpleExpressionProxy leadTerm = null;
    for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,Integer>
         entry : mCountingMap.entrySet()) {
      if (Math.abs(entry.getValue()) == 1) {
        final ProxyAccessor<SimpleExpressionProxy> accessor = entry.getKey();
        final SimpleExpressionProxy expr = accessor.getProxy();
        if (leadTerm == null || comparator.compare(expr, leadTerm) < 0) {
          leadTerm = expr;
        }
      }
    }
    if (leadTerm == null) {
      leadTerm = mFactory.createIntConstantProxy(0);
    } else {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        mCountingMap.createAccessor(leadTerm);
      int count = mCountingMap.remove(accessor);
      if (count > 0) {
        for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,Integer>
             entry : mCountingMap.entrySet()) {
          count = entry.getValue();
          entry.setValue(-count);
        }
      }
    }
    return leadTerm;
  }

  private List<SimpleExpressionProxy> getOrderedLiterals()
  {
    final List<SimpleExpressionProxy> result =
      new ArrayList<>(mCountingMap.size());
    for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,Integer>
         entry : mCountingMap.entrySet()) {
      if (entry.getValue() != 0) {
        final ProxyAccessor<SimpleExpressionProxy> accessor = entry.getKey();
        final SimpleExpressionProxy expr = accessor.getProxy();
        result.add(expr);
      }
    }
    Collections.sort(result, mSummandComparator);
    return result;
  }


  private SimpleExpressionProxy createNormalisedSum
    (final List<SimpleExpressionProxy> orderedLiterals)
  {
    if (orderedLiterals.isEmpty()) {
      return mFactory.createIntConstantProxy(0);
    } else {
      final Iterator<SimpleExpressionProxy> iter = orderedLiterals.iterator();
      final SimpleExpressionProxy first = iter.next();
      int count = mCountingMap.getByProxy(first);
      SimpleExpressionProxy sum;
      if (count > 0) {
        count--;
        sum = first;
      } else if (first instanceof IntConstantProxy) {
        final IntConstantProxy constant = (IntConstantProxy) first;
        final int negatedValue = -constant.getValue();
        sum = getConstant(negatedValue);
        count = 0;
      } else {
        final UnaryOperator op = mOperatorTable.getUnaryMinusOperator();
        sum = mFactory.createUnaryExpressionProxy(op, first);
        count++;
      }
      sum = extendSum(sum, first, count);
      while (iter.hasNext()) {
        final SimpleExpressionProxy next = iter.next();
        count = mCountingMap.getByProxy(next);
        sum = extendSum(sum, next, count);
      }
      return sum;
    }
  }

  private SimpleExpressionProxy extendSum(SimpleExpressionProxy sum,
                                          SimpleExpressionProxy literal,
                                          int count)
  {
    if (count != 0) {
      final BinaryOperator op;
      if (literal instanceof IntConstantProxy) {
        final IntConstantProxy constant = (IntConstantProxy) literal;
        final int value = count * constant.getValue();
        if (value > 0) {
          literal = getConstant(value);
          op = mOperatorTable.getPlusOperator();
        } else {
          literal = getConstant(-value);
          op = mOperatorTable.getMinusOperator();
        }
        count = 1;
      } else {
        if (count > 0) {
          op = mOperatorTable.getPlusOperator();
        } else {
          op = mOperatorTable.getMinusOperator();
          count = -count;
        }
      }
      for (int i = 0; i < count; i++) {
        sum = mFactory.createBinaryExpressionProxy(op, sum, literal);
      }
    }
    return sum;
  }

  IntConstantProxy getConstant(final int value)
  {
    if (mConstant != null && mConstant.getValue() == value) {
      return mConstant;
    } else {
      return mFactory.createIntConstantProxy(value);
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Factory used to create expressions.
   */
  private final ModuleProxyFactory mFactory;
  /**
   * Operator table used to lookup and identify operators.
   */
  private final CompilerOperatorTable mOperatorTable;
  /**
   * The equality used to determine whether terms are equal.
   */
  private final ModuleEqualityVisitor mEquality;
  /**
   * The comparator used to order the summands in an expression.
   * This is an {@link ExpressionComparator} that imposes the standard
   * expression ordering.
   */
  private final ExpressionComparator mSummandComparator;
  /**
   * The expression compiler used to further simplify subterms, or
   * <CODE>null</CODE> to suppress simplification other than sum
   * normalisation.
   */
  private SimpleExpressionCompiler mSimplifier;

  private int mCurrentSign;
  private boolean mLiteralExpected;
  private boolean mAlreadyNormalised;
  private SimpleExpressionProxy mPreviousLiteral;

  private final List<SimpleExpressionProxy> mPositiveTerms;
  private final List<SimpleExpressionProxy> mNegativeTerms;
  private IntConstantProxy mConstant;
  private int mSumOfConstants;
  private final ProxyAccessorHashMap<SimpleExpressionProxy,Integer> mCountingMap;

}
