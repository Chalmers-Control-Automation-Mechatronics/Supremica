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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to rewrite sums.</P>
 *
 * <PRE>LHS == RHS</PRE>
 *
 * <P>This rewrite rules partially solves linear equations to give
 * a value for the lowest-ordered variable, if possible. It handles
 * associativity and commutativity of addition, and cancellation by
 * subtraction. Aggregation of equal terms and distributivity are not
 * yet supported, nor is the unary minus operator.</P>
 *
 * <P>Following are examples of supported simplifications:</P>
 *
 * <PRE>
 *   1 == 1 + x      1 - y == x + y
 *   ----------      --------------
 *     x == 0        x == 1 - y - y
 * </PRE>
 *
 * @author Robi Malik
 */

class SumSimplificationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static SumSimplificationRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getEqualsOperator();
    final SumPlaceHolder LHS = new SumPlaceHolder(factory, "LHS");
    final SumPlaceHolder RHS = new SumPlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, lhs, rhs);
    return new SumSimplificationRule(template, LHS, RHS);
  }


  //#########################################################################
  //# Constructors
  private SumSimplificationRule(final SimpleExpressionProxy template,
                                final SumPlaceHolder LHS,
                                final SumPlaceHolder RHS)
  {
    super(template, new PlaceHolder[] {LHS, RHS});
    mLHS = LHS;
    mRHS = RHS;
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (super.match(constraint, propagator)) {
      final boolean lsum = mLHS.isBinaryPlusOrMinus();
      final boolean rsum = mRHS.isBinaryPlusOrMinus();
      if (lsum && rsum) {
        return true;
      } else if (lsum) {
        final SimpleExpressionProxy rhs = mRHS.getBoundExpression();
        return !mLHS.isNormalisedSum(rhs, propagator);
      } else if (rsum) {
        final SimpleExpressionProxy lhs = mLHS.getBoundExpression();
        return !mRHS.isNormalisedSum(lhs, propagator);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  boolean isMakingReplacement()
  {
    return true;
  }

  @Override
  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SumVisitor visitor = new SumVisitor();
    final SimpleExpressionProxy lhs = mLHS.getBoundExpression();
    visitor.collect(lhs, true);
    final SimpleExpressionProxy rhs = mRHS.getBoundExpression();
    visitor.collect(rhs, false);
    final SimpleExpressionProxy expr = this.getMatchedExpression();
    final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
    final BinaryOperator op = binary.getOperator();
    final SimpleExpressionProxy constraint =
      visitor.createNormalisedEquation(propagator, op);
    if (constraint != null) {
      propagator.addConstraint(constraint);
    }
  }


  //#########################################################################
  //# Inner Class SumPlaceHolder
  private static class SumPlaceHolder extends IntegerExpressionPlaceHolder
  {

    //#######################################################################
    //# Constructor
    SumPlaceHolder(final ModuleProxyFactory factory, final String name)
    {
      super(factory, name);
    }

    //#######################################################################
    //# Sum Matching and Normalisation
    private boolean isBinaryPlusOrMinus()
    {
      final SimpleExpressionProxy expr = getBoundExpression();
      if (expr instanceof BinaryExpressionProxy) {
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
        final BinaryOperator op = binary.getOperator();
        final CompilerOperatorTable optable =
          CompilerOperatorTable.getInstance();
        return
          op == optable.getPlusOperator() || op == optable.getMinusOperator();
      } else {
        return false;
      }
    }

    private boolean isNormalisedSum(final SimpleExpressionProxy otherside,
                                    final ConstraintPropagator propagator)
    {
      final CompilerOperatorTable optable =
        CompilerOperatorTable.getInstance();
      final BinaryOperator plus = optable.getPlusOperator();
      final BinaryOperator minus = optable.getMinusOperator();
      final SimpleExpressionProxy expr = getBoundExpression();
      if (!(expr instanceof BinaryExpressionProxy)) {
        return false;
      }
      BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
      BinaryOperator op = binary.getOperator();
      if (op != plus && op != minus) {
        return false;
      }
      final Comparator<SimpleExpressionProxy> eqComparator =
        propagator.getEquationComparator();
      final Comparator<SimpleExpressionProxy> listComparator =
        propagator.getListComparator();
      SimpleExpressionProxy last = null;
      BinaryOperator lastop = null;
      while (true) {
        final SimpleExpressionProxy rhs = binary.getRight();
        if (rhs instanceof BinaryExpressionProxy) {
          binary = (BinaryExpressionProxy) rhs;
          op = binary.getOperator();
          if (op == plus || op == minus) {
            return false;
          }
        }
        if (eqComparator.compare(otherside, rhs) >= 0) {
          return false;
        } else if (last != null) {
          final int cmpmax = op == lastop ? 0 : -1;
          if (listComparator.compare(rhs, last) > cmpmax) {
            return false;
          }
        }
        last = rhs;
        lastop = op;
        final SimpleExpressionProxy lhs = binary.getLeft();
        if (lhs instanceof BinaryExpressionProxy) {
          binary = (BinaryExpressionProxy) lhs;
          op = binary.getOperator();
          if (op == plus || op == minus) {
            continue;
          }
        }
        final int cmpmax = op == lastop ? 0 : -1;
        return listComparator.compare(lhs, rhs) <= cmpmax;
      }
    }

  }


  //#########################################################################
  //# Inner Class SumVisitor
  private static class SumVisitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private SumVisitor()
    {
      mCollectedLHS = new LinkedList<SimpleExpressionProxy>();
      mCollectedRHS = new LinkedList<SimpleExpressionProxy>();
      mConstantRHS = 0;
    }

    //#######################################################################
    //# Invocation
    private void collect(final SimpleExpressionProxy expr, final boolean lhs)
    {
      try {
        mIsLHS = lhs;
        expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy
      (final BinaryExpressionProxy binary)
      throws VisitorException
    {
      final BinaryOperator op = binary.getOperator();
      final CompilerOperatorTable optable =
        CompilerOperatorTable.getInstance();
      if (op == optable.getPlusOperator()) {
        final SimpleExpressionProxy lhs = binary.getLeft();
        lhs.acceptVisitor(this);
        final SimpleExpressionProxy rhs = binary.getRight();
        rhs.acceptVisitor(this);
      } else if (op == optable.getMinusOperator()) {
        final SimpleExpressionProxy lhs = binary.getLeft();
        lhs.acceptVisitor(this);
        mIsLHS = !mIsLHS;
        final SimpleExpressionProxy rhs = binary.getRight();
        rhs.acceptVisitor(this);
        mIsLHS = !mIsLHS;
      } else {
        visitSimpleExpressionProxy(binary);
      }
      return null;
    }

    @Override
    public Object visitIntConstantProxy(final IntConstantProxy intconst)
      throws VisitorException
    {
      final int value = intconst.getValue();
      if (mIsLHS) {
        mConstantRHS -= value;
      } else {
        mConstantRHS += value;
      }
      return null;
    }

    @Override
    public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
      throws VisitorException
    {
      if (mIsLHS) {
        mCollectedLHS.add(expr);
      } else {
        mCollectedRHS.add(expr);
      }
      return null;
    }

    //#######################################################################
    //# Output
    private BinaryExpressionProxy createNormalisedEquation
      (final ConstraintPropagator propagator, final BinaryOperator op)
    {
      final Comparator<SimpleExpressionProxy> eqComparator =
        propagator.getEquationComparator();
      Collections.sort(mCollectedLHS, eqComparator);
      Collections.sort(mCollectedRHS, eqComparator);
      final Iterator<SimpleExpressionProxy> iterLHS = mCollectedLHS.iterator();
      final Iterator<SimpleExpressionProxy> iterRHS = mCollectedRHS.iterator();
      while (iterLHS.hasNext() && iterRHS.hasNext()) {
        final SimpleExpressionProxy exprLHS = iterLHS.next();
        final SimpleExpressionProxy exprRHS = iterRHS.next();
        if (eqComparator.compare(exprLHS, exprRHS) == 0) {
          iterLHS.remove();
          iterRHS.remove();
        }
      }
      final ModuleProxyFactory factory = propagator.getFactory();
      SimpleExpressionProxy minexpr = null;
      if (mConstantRHS != 0) {
        final IntConstantProxy intconst =
          factory.createIntConstantProxy(mConstantRHS);
        minexpr = intconst;
      }
      SimpleExpressionProxy lhs = null;
      if (!mCollectedLHS.isEmpty()) {
        lhs = mCollectedLHS.iterator().next();
        if (compareNull(eqComparator, lhs, minexpr) < 0) {
          minexpr = lhs;
        }
      }
      SimpleExpressionProxy rhs = null;
      if (!mCollectedRHS.isEmpty()) {
        rhs = mCollectedRHS.iterator().next();
        if (compareNull(eqComparator, rhs, minexpr) < 0) {
          minexpr = rhs;
        }
      }
      if (minexpr == null) {
        return null;
      }
      if (minexpr == lhs) {
        mCollectedLHS.remove(0);
        rhs = buildSum(propagator, false);
      } else if (minexpr == rhs) {
        mCollectedRHS.remove(0);
        lhs = buildSum(propagator, true);
      } else {
        rhs = minexpr;
        mConstantRHS = 0;
        lhs = buildSum(propagator, true);
      }
      if (eqComparator.compare(lhs, rhs) > 0 && op.isSymmetric()){
        return factory.createBinaryExpressionProxy(op, rhs, lhs);
      } else {
        return factory.createBinaryExpressionProxy(op, lhs, rhs);
      }
    }

    private SimpleExpressionProxy buildSum
      (final ConstraintPropagator propagator, final boolean lhs)
    {
      final ModuleProxyFactory factory = propagator.getFactory();
      final Comparator<SimpleExpressionProxy> listComparator =
        propagator.getListComparator();
      Collections.sort(mCollectedLHS, listComparator);
      Collections.sort(mCollectedRHS, listComparator);
      final Iterator<SimpleExpressionProxy> iterLHS = mCollectedLHS.iterator();
      SimpleExpressionProxy exprLHS = iterLHS.hasNext() ? iterLHS.next() : null;
      final Iterator<SimpleExpressionProxy> iterRHS = mCollectedRHS.iterator();
      SimpleExpressionProxy exprRHS = iterRHS.hasNext() ? iterRHS.next() : null;
      final IntConstantProxy intconst;
      if (lhs) {
        intconst = factory.createIntConstantProxy(-mConstantRHS);
      } else {
        intconst = factory.createIntConstantProxy(mConstantRHS);
      }
      if (exprLHS == null && exprRHS == null) {
        return intconst;
      }
      SimpleExpressionProxy result = null;
      if (mConstantRHS != 0) {
        result = intconst;
      } else if (lhs) {
        if (compareNull(listComparator, exprLHS, exprRHS) > 0) {
          result = intconst;
        }
      } else {
        if (compareNull(listComparator, exprLHS, exprRHS) < 0) {
          result = intconst;
        }
      }
      final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
      final BinaryOperator plus = optable.getPlusOperator();
      final BinaryOperator minus = optable.getMinusOperator();
      while (exprLHS != null || exprRHS != null) {
        final SimpleExpressionProxy expr;
        final BinaryOperator op;
        if (compareNull(listComparator, exprLHS, exprRHS) < 0) {
          expr = exprLHS;
          op = lhs ? plus : minus;
          exprLHS = iterLHS.hasNext() ? iterLHS.next() : null;
        } else {
          expr = exprRHS;
          op = lhs ? minus : plus;
          exprRHS = iterRHS.hasNext() ? iterRHS.next() : null;
        }
        if (result == null) {
          result = expr;
        } else {
          result = factory.createBinaryExpressionProxy(op, result, expr);
        }
      }
      return result;
    }

    private static int compareNull
      (final Comparator<SimpleExpressionProxy> comparator,
       final SimpleExpressionProxy expr1,
       final SimpleExpressionProxy expr2)
    {
      if (expr1 == null) {
        return expr2 == null ? 0 : 1;
      } else if (expr2 == null) {
        return -1;
      } else {
        return comparator.compare(expr1, expr2);
      }
    }

    //#######################################################################
    //# Data Members;
    private final List<SimpleExpressionProxy> mCollectedLHS;
    private final List<SimpleExpressionProxy> mCollectedRHS;
    private int mConstantRHS;
    private boolean mIsLHS;

  }


  //#########################################################################
  //# Data Members
  private final SumPlaceHolder mLHS;
  private final SumPlaceHolder mRHS;

}








