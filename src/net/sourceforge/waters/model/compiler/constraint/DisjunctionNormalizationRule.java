//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DisjunctionNormalizationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
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
 * ordering and process nested negations. Duplicates are also removed.</P>
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


  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
  {
    final Comparator<SimpleExpressionProxy> comparator =
      propagator.getExpressionComparator();
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


  //#########################################################################
  //# Inner Class DisjunctionCollector
  private static class DisjunctionCollector
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private DisjunctionCollector()
    {
      mLiterals = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
    }

    //#######################################################################
    //# Invocation
    List<SimpleExpressionProxy> collect(final SimpleExpressionProxy expr,
                                        final ConstraintPropagator propagator)
      throws EvalException
    {
      try {
        mPropagator = propagator;
        mNegated = false;
        mHasModifications = false;
        mInRHS = false;
        mPrevious = null;
        expr.acceptVisitor(this);
        if (mHasModifications) {
          return new ArrayList<SimpleExpressionProxy>(mLiterals.values());
        } else {
          return null;
        }
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mLiterals.clear();
        mPrevious = null;
      }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
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
        mInRHS = true;
        final SimpleExpressionProxy rhs = expr.getRight();
        rhs.acceptVisitor(this);
        mInRHS = false;
        return null;
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy norm =
          mPropagator.getNormalisedLiteral(expr, mNegated);
        if (mLiterals.addProxy(norm)) {
          if (mPrevious != null) {
            final Comparator<SimpleExpressionProxy> comparator =
              mPropagator.getExpressionComparator();
            mHasModifications |= comparator.compare(mPrevious, norm) > 0;
          }
          mPrevious = norm;
        } else {
          mHasModifications = true;
        }
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final CompilerOperatorTable optable = mPropagator.getOperatorTable();
      if (expr.getOperator() == optable.getNotOperator()) {
        final boolean negated = mNegated;
        mHasModifications |= negated;
        try {
          mNegated = !mNegated;
          final SimpleExpressionProxy subterm = expr.getSubTerm();
          return subterm.acceptVisitor(this);
        } finally {
          mNegated = negated;
        }
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<SimpleExpressionProxy> mLiterals;

    private ConstraintPropagator mPropagator;
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