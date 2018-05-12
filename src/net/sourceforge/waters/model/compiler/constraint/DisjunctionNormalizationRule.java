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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification rule used for normalisation of disjunctions.</P>
 *
 * <P>There are two versions of this rule, a <I>normal</I> and a
 * <I>negative</I> rule.</P>
 *
 * <P><U>Normal:</U></P>
 *
 * <PRE>
 *   A1 | A2 | ... | An
 *   ------------------
 *   A1 | A2 | ... | An
 * </PRE>
 *
 * <P><U>Negative:</U></P>
 *
 * <PRE>
 *   !(A1 & A2 & ... & An)
 *   ---------------------
 *   !A1 | !A2 | ... | !An
 * </PRE>
 *
 * <P>Both rules ensure that disjuncts are ordered according to expression
 * ordering and process nested negations. Duplicates are removed.
 * Furthermore, the law of excluded middle is applied, simplifying a
 * disjunction containing complementary subformulas to true.</P>
 *
 * @author Robi Malik
 */

class DisjunctionNormalizationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static DisjunctionNormalizationRule createNormalRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder LHS = new PlaceHolder(factory, "LHS");
    final PlaceHolder RHS = new PlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final BinaryOperator op = optable.getOrOperator();
    final BinaryExpressionProxy template =
      factory.createBinaryExpressionProxy(op, lhs, rhs);
    return new DisjunctionNormalizationRule(template, LHS, RHS);
  }

  static DisjunctionNormalizationRule createNegativeRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder LHS = new PlaceHolder(factory, "LHS");
    final PlaceHolder RHS = new PlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final BinaryOperator andop = optable.getAndOperator();
    final BinaryExpressionProxy conjunction =
      factory.createBinaryExpressionProxy(andop, lhs, rhs);
    final UnaryOperator notop = optable.getNotOperator();
    final UnaryExpressionProxy template =
      factory.createUnaryExpressionProxy(notop, conjunction);
    return new DisjunctionNormalizationRule(template, LHS, RHS);
  }


  //#########################################################################
  //# Constructors
  private DisjunctionNormalizationRule(final SimpleExpressionProxy template,
                                       final PlaceHolder LHS,
                                       final PlaceHolder RHS)
  {
    super(template, new PlaceHolder[] {LHS, RHS});
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (super.match(constraint, propagator)) {
      mList = COLLECTOR.collect(constraint, propagator);
      return mList != null;
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
  {
    if (!mList.isEmpty()) {
      final Comparator<SimpleExpressionProxy> comparator =
        propagator.getListComparator();
      Collections.sort(mList, comparator);
      final ModuleProxyFactory factory = propagator.getFactory();
      final CompilerOperatorTable optable = propagator.getOperatorTable();
      final BinaryOperator op = optable.getOrOperator();
      final Iterator<SimpleExpressionProxy> iter = mList.iterator();
      SimpleExpressionProxy result = iter.next();
      while (iter.hasNext()) {
        final SimpleExpressionProxy rhs = iter.next();
        result = factory.createBinaryExpressionProxy(op, result, rhs);
      }
      propagator.addConstraint(result);
    }
  }


  //#########################################################################
  //# Inner Class DisjunctionCollector
  private static class DisjunctionCollector
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private DisjunctionCollector()
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mAllLiterals = new LinkedList<>();
      mPositiveLiterals = new ProxyAccessorHashSet<>(eq);
      mNegativeLiterals = new ProxyAccessorHashSet<>(eq);
    }

    //#######################################################################
    //# Invocation
    List<SimpleExpressionProxy> collect(final SimpleExpressionProxy expr,
                                        final ConstraintPropagator propagator)
      throws EvalException
    {
      try {
        mPropagator = propagator;
        mTrueDisjunction = false;
        mNegated = false;
        mHasModifications = false;
        mInRHS = false;
        mPrevious = null;
        expr.acceptVisitor(this);
        if (!mHasModifications) {
          return null;
        } else if (mTrueDisjunction) {
          return Collections.emptyList();
        } else {
          return new ArrayList<SimpleExpressionProxy>(mAllLiterals);
        }
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mAllLiterals.clear();
        mPositiveLiterals.clear();
        mNegativeLiterals.clear();
        mPrevious = null;
      }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final CompilerOperatorTable optable = mPropagator.getOperatorTable();
      final BinaryOperator op = expr.getOperator();
      final boolean deeper;
      if (mNegated) {
        deeper = (op == optable.getAndOperator());
        mHasModifications |= deeper;
      } else {
        deeper = (op == optable.getOrOperator());
      }
      if (deeper) {
        mHasModifications |= mInRHS;
        final SimpleExpressionProxy lhs = expr.getLeft();
        lhs.acceptVisitor(this);
        if (!mTrueDisjunction) {
          mInRHS = true;
          final SimpleExpressionProxy rhs = expr.getRight();
          rhs.acceptVisitor(this);
          mInRHS = false;
        }
        return null;
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy norm =
          mPropagator.getNormalisedLiteral(expr, mNegated);
        addLiteral(norm);
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final CompilerOperatorTable opTable = mPropagator.getOperatorTable();
      final SimpleExpressionProxy negTerm = opTable.getNegatedSubterm(expr);
      if (negTerm == null) {
        return visitSimpleExpressionProxy(expr);
      } else {
        final boolean negated = mNegated;
        mHasModifications |= negated;
        try {
          mNegated = !mNegated;
          return negTerm.acceptVisitor(this);
        } finally {
          mNegated = negated;
        }
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addLiteral(final SimpleExpressionProxy expr)
    {
      final CompilerOperatorTable opTable = mPropagator.getOperatorTable();
      final SimpleExpressionProxy negTerm = opTable.getNegatedSubterm(expr);
      if (negTerm == null) {
        if (mNegativeLiterals.containsProxy(expr)) {
          mTrueDisjunction = mHasModifications = true;
          mAllLiterals.clear();
          return;
        } else if (!mPositiveLiterals.addProxy(expr)) {
          mHasModifications = true;
          return;
        }
      } else {
        if (mPositiveLiterals.containsProxy(negTerm)) {
          mTrueDisjunction = mHasModifications = true;
          mAllLiterals.clear();
          return;
        } else if (!mNegativeLiterals.addProxy(negTerm)) {
          mHasModifications = true;
          return;
        }
      }
      mAllLiterals.add(expr);
      if (mPrevious != null && !mHasModifications) {
        final Comparator<SimpleExpressionProxy> comparator =
          mPropagator.getListComparator();
        mHasModifications = comparator.compare(mPrevious, expr) > 0;
      }
      mPrevious = expr;
    }

    //#######################################################################
    //# Data Members
    private final List<SimpleExpressionProxy> mAllLiterals;
    private final ProxyAccessorSet<SimpleExpressionProxy> mPositiveLiterals;
    private final ProxyAccessorSet<SimpleExpressionProxy> mNegativeLiterals;

    private ConstraintPropagator mPropagator;
    private boolean mTrueDisjunction;
    private boolean mHasModifications;
    private boolean mNegated;
    private boolean mInRHS;
    private SimpleExpressionProxy mPrevious;

  }


  //#########################################################################
  //# Data Members
  private List<SimpleExpressionProxy> mList;


  //#########################################################################
  //# Static Class Constants
  private static final DisjunctionCollector COLLECTOR =
    new DisjunctionCollector();

}
