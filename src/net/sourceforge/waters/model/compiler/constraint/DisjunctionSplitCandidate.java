//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DisjunctionSplitCandidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DisjunctionSplitCandidate
  extends AbstractSplitCandidate
{

  //#########################################################################
  //# Constructor
  DisjunctionSplitCandidate(final SimpleExpressionProxy disj,
                            final int size)
  {
    mDisjunction = disj;
    mParts = null;
    mSplitSize = size;
  }

  DisjunctionSplitCandidate
    (final SimpleExpressionProxy disj,
     final Collection<List<SimpleExpressionProxy>> parts)
  {
    mDisjunction = disj;
    mParts = parts;
    mSplitSize = parts.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.constraint.SplitCandidate
  public SimpleExpressionProxy getRecallable()
  {
    return null;
  }

  public List<SimpleExpressionProxy> getSplitExpressions
    (final ModuleProxyFactory factory, final CompilerOperatorTable optable)
  {
    final DisjunctionVisitor visitor =
      new DisjunctionVisitor(factory, optable);
    return visitor.collect();
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.compiler.constraint.AbstractSplitCandidate
  int getNumberOfOccurrences()
  {
    return 2;
  }

  int getSplitSize()
  {
    return mSplitSize;
  }

  boolean getOccursWithNext()
  {
    return false;
  }

  int getKindValue()
  {
    return AbstractSplitCandidate.DISJUNCTION_SPLIT;
  }

  SimpleExpressionProxy getSplitExpression()
  {
    return mDisjunction;
  }


  //#########################################################################
  //# Inner Class DisjunctionVisitor
  private class DisjunctionVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private DisjunctionVisitor(final ModuleProxyFactory factory,
                               final CompilerOperatorTable optable)
    {
      mFactory = factory;
      mOrOperator = optable.getOrOperator();
    }

    //#######################################################################
    //# Invocation
    private List<SimpleExpressionProxy> collect()
    {
      try {
        mResult = new ArrayList<SimpleExpressionProxy>(mSplitSize);
        mDisjunction.acceptVisitor(this);
        assert mResult.size() == mSplitSize :
          "Unexpected size of split list---wrong expressions in parts?";
        return mResult;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mResult = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      if (expr.getOperator() == mOrOperator) {
        final SimpleExpressionProxy lhs = expr.getLeft();
        lhs.acceptVisitor(this);
        final SimpleExpressionProxy rhs = expr.getRight();
        rhs.acceptVisitor(this);
        return null;
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      if (mParts == null) {
        mResult.add(expr);
      } else {
        for (final List<SimpleExpressionProxy> part : mParts) {
          if (part.iterator().next() == expr) {
            final SimpleExpressionProxy disj = buildDisjunction(part);
            mResult.add(disj);
            break;
          }
        }
      }
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private SimpleExpressionProxy buildDisjunction
      (final List<SimpleExpressionProxy> list)
    {
      final Iterator<SimpleExpressionProxy> iter = list.iterator();
      SimpleExpressionProxy result = iter.next();
      while (iter.hasNext()) {
        final SimpleExpressionProxy rhs = iter.next();
        result =
          mFactory.createBinaryExpressionProxy(mOrOperator, result, rhs);
      }
      return result;
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxyFactory mFactory;
    private final BinaryOperator mOrOperator;
    private List<SimpleExpressionProxy> mResult;

  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mDisjunction;
  private final Collection<List<SimpleExpressionProxy>> mParts;
  private final int mSplitSize;

}