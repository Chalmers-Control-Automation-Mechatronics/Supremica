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

import java.util.Comparator;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification rule used for normalisation of binary relations.</P>
 *
 * <P>There are three versions of this rule, the <I>normal</I>, the
 * <I>negative</I>, and the <I>negating</I> rule.</P>
 *
 * <P><U>Normal:</U></P>
 *
 * <PRE>
 *   LHS == RHS   LHS != RHS   LHS &gt; RHS    LHS &gt;= RHS
 *   ----------   ----------   ---------    ----------
 *   LHS == RHS   LHS != RHS   RHS &lt; LHS    RHS &lt;= LHS
 * </PRE>
 *
 * <P><U>Negative:</U></P>
 *
 * <PRE>
 *   !(LHS == RHS)   !(LHS != RHS)
 *   -------------   -------------
 *    LHS != RHS      LHS == RHS
 *
 *
 *   !(LHS &lt; RHS)   !(LHS &lt;= RHS)   !(LHS &gt; RHS)   !(LHS &gt;= RHS)
 *   ------------   -------------   ------------   -------------
 *    RHS &lt;= LHS      RHS &lt; LHS      LHS &lt;= RHS      LHS &lt; RHS
 * </PRE>
 *
 * <P><U>Negating:</U></P>
 *
 * <PRE>
 *   LHS == RHS   LHS != RHS
 *   ----------   ----------
 *   LHS != RHS   LHS == RHS
 *
 *
 *   LHS &lt; RHS    LHS &lt;= RHS   LHS &gt; RHS    LHS &gt;= RHS
 *   ----------   ----------   ----------   ----------
 *   RHS &lt;= LHS   RHS &lt; LHS    LHS &lt;= RHS   LHS &lt; RHS
 * </PRE>
 *
 * <P>For equality and inequality, all rules ensure that the subterms are
 * ordered according to a given expression ordering.</P>
 *
 * @author Robi Malik
 */

class RelationNormalizationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static RelationNormalizationRule createNormalRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder placeholder =
      new NotNormalRelationPlaceHolder(factory, "REL");
    final SimpleIdentifierProxy ident = placeholder.getIdentifier();
    return new RelationNormalizationRule(ident, placeholder, false);
  }

  static RelationNormalizationRule createNegativeRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder placeholder = new RelationPlaceHolder(factory, "REL");
    final SimpleIdentifierProxy ident = placeholder.getIdentifier();
    final UnaryOperator op = optable.getNotOperator();
    final UnaryExpressionProxy template =
      factory.createUnaryExpressionProxy(op, ident);
    return new RelationNormalizationRule(template, placeholder, true);
  }

  static RelationNormalizationRule createNegatingRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder placeholder = new RelationPlaceHolder(factory, "REL");
    final SimpleIdentifierProxy ident = placeholder.getIdentifier();
    return new RelationNormalizationRule(ident, placeholder, true);
  }


  //#########################################################################
  //# Constructors
  private RelationNormalizationRule(final SimpleExpressionProxy template,
                                    final PlaceHolder placeholder,
                                    final boolean negated)
  {
    super(template, placeholder);
    mPlaceHolder = placeholder;
    mNegated = negated;
  }


  //#########################################################################
  //# Invocation Interface
  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
  {
    final BinaryExpressionProxy normal = getResult(propagator);
    propagator.addConstraint(normal);
  }


  //#########################################################################
  //# Specific Access
  BinaryExpressionProxy getResult(final ConstraintPropagator propagator)
  {
    final CompilerOperatorTable optable = propagator.getOperatorTable();
    final BinaryExpressionProxy binary =
      (BinaryExpressionProxy) mPlaceHolder.getBoundExpression();
    final SimpleExpressionProxy lhs = binary.getLeft();
    final SimpleExpressionProxy rhs = binary.getRight();
    final boolean swap;
    BinaryOperator op = binary.getOperator();
    if (mNegated) {
      op = optable.getComplementaryOperator(op);
    }
    if (op.isSymmetric()) {
      final Comparator<SimpleExpressionProxy> comparator =
        propagator.getEquationComparator();
      swap = comparator.compare(lhs, rhs) > 0;
    } else {
      final BinaryOperator swapop = optable.getSwappedNormalOperator(op);
      swap = (swapop != null);
      if (swap) {
        op = swapop;
      }
    }
    final ModuleProxyFactory factory = propagator.getFactory();
    if (swap) {
      return factory.createBinaryExpressionProxy(op, rhs, lhs);
    } else {
      return factory.createBinaryExpressionProxy(op, lhs, rhs);
    }
  }


  //#########################################################################
  //# Inner Class RelationPlaceHolder
  private static class RelationPlaceHolder extends PlaceHolder
  {

    //#######################################################################
    //# Constructor
    RelationPlaceHolder(final ModuleProxyFactory factory, final String name)
    {
      super(factory, name);
    }

    //#######################################################################
    //# Matching
    boolean accepts(final SimpleExpressionProxy expr,
                    final ConstraintPropagator propagator)
    {
      if (expr instanceof BinaryExpressionProxy) {
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
        final BinaryOperator op = binary.getOperator();
        final CompilerOperatorTable optable = propagator.getOperatorTable();
        return optable.getComplementaryOperator(op) != null;
      } else {
        return false;
      }
    }

  }


  //#########################################################################
  //# Inner Class NotNormalRelationPlaceHolder
  private static class NotNormalRelationPlaceHolder extends PlaceHolder
  {

    //#######################################################################
    //# Constructor
    NotNormalRelationPlaceHolder(final ModuleProxyFactory factory,
                                 final String name)
    {
      super(factory, name);
    }

    //#######################################################################
    //# Matching
    boolean accepts(final SimpleExpressionProxy expr,
                    final ConstraintPropagator propagator)
    {
      if (expr instanceof BinaryExpressionProxy) {
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
        final BinaryOperator op = binary.getOperator();
        final CompilerOperatorTable optable = propagator.getOperatorTable();
        if (optable.getComplementaryOperator(op) == null) {
          return false;
        } else if (op.isSymmetric()) {
          final Comparator<SimpleExpressionProxy> comparator =
            propagator.getEquationComparator();
          final SimpleExpressionProxy lhs = binary.getLeft();
          final SimpleExpressionProxy rhs = binary.getRight();
          return comparator.compare(lhs, rhs) > 0;
        } else {
          return optable.getSwappedNormalOperator(op) != null;
        }
      } else {
        return false;
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mPlaceHolder;
  private final boolean mNegated;

}







