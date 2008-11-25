//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   RelationNormalizationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Comparator;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification rule used for normalisation of binary relations.</P>
 *
 * <P>There are two versions of this rule, the <I>normal</I>, the
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
        propagator.getExpressionComparator();
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
  //# Inner Class RelationPlaceHolder
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
            propagator.getExpressionComparator();
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