//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   VariableSplitCandidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class VariableSplitCandidate
  extends AbstractSplitCandidate
{

  //#########################################################################
  //# Constructor
  VariableSplitCandidate(final SimpleExpressionProxy varname,
                         final CompiledRange range)
  {
    mVariableName = varname;
    mRange = range;
    mNumberOfOccurrences = 0;
    mOccursWithNext = false;
    mIteSplits = null;
  }


  //#########################################################################
  //# Simple Access
  void setOccursWithNext()
  {
    mOccursWithNext = true;
  }

  void addOccurrence()
  {
    mNumberOfOccurrences++;
  }

  void addIteSplit(final SimpleExpressionProxy cond)
  {
    if (mIteSplits == null) {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      mIteSplits = new ProxyAccessorHashMap<SimpleExpressionProxy,Integer>(eq);
    }
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mIteSplits.createAccessor(cond);
    final Integer count = mIteSplits.get(accessor);
    if (count == null) {
      mIteSplits.put(accessor, 1);
    } else {
      mIteSplits.put(accessor, count + 1);
    }
  }


  //#########################################################################
  //# net.sourceforge.waters.model.compiler.constraint.SplitCandidate
  @Override
  public void recall(final ConstraintPropagator propagator)
  {
    propagator.recallBinding(mVariableName);
  }

  @Override
  public List<SimpleExpressionProxy> getSplitExpressions
    (final ModuleProxyFactory factory, final CompilerOperatorTable optable)
  {
    final int size = getSplitSize();
    final List<SimpleExpressionProxy> result =
      new ArrayList<SimpleExpressionProxy>(size);
    if (mIteSplits != null) {
      // Use most frequent \ite condition over regular split
      final Comparator<SimpleExpressionProxy> comparator =
        ExpressionComparator.getInstance();
      SimpleExpressionProxy cond = null;
      final int maxval = 0;
      for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,Integer> entry :
           mIteSplits.entrySet()) {
        if (entry.getValue() >= maxval) {
          final SimpleExpressionProxy current = entry.getKey().getProxy();
          if (entry.getValue() > maxval ||
              comparator.compare(current, cond) < 0) {
            cond = current;
          }
        }
      }
      final UnaryOperator op = optable.getNotOperator();
      final UnaryExpressionProxy negcond =
        factory.createUnaryExpressionProxy(op, cond);
      result.add(cond);
      result.add(negcond);
    } else if (hasBooleanRange()) {
      final UnaryOperator op = optable.getNotOperator();
      final UnaryExpressionProxy negliteral =
        factory.createUnaryExpressionProxy(op, mVariableName);
      result.add(negliteral);
      result.add(mVariableName);
    } else {
      final BinaryOperator op = optable.getEqualsOperator();
      for (final SimpleExpressionProxy value : mRange.getValues()) {
        final BinaryExpressionProxy eqn =
          factory.createBinaryExpressionProxy(op, mVariableName, value);
        result.add(eqn);
      }
    }
    return result;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.compiler.constraint.AbstractSplitCandidate
  @Override
  int getNumberOfOccurrences()
  {
    return mNumberOfOccurrences;
  }

  @Override
  int getSplitSize()
  {
    if (mIteSplits == null) {
      return mRange.size();
    } else {
      return 2;
    }
  }

  @Override
  boolean getOccursWithNext()
  {
    return mOccursWithNext;
  }

  @Override
  int getKindValue()
  {
    return AbstractSplitCandidate.VARIABLE_SPLIT;
  }

  @Override
  SimpleExpressionProxy getSplitExpression()
  {
    return mVariableName;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean hasBooleanRange()
  {
    if (mRange instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) mRange;
      return intrange.isBooleanRange();
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mVariableName;
  private final CompiledRange mRange;

  private int mNumberOfOccurrences;
  private boolean mOccursWithNext;
  private ProxyAccessorMap<SimpleExpressionProxy,Integer> mIteSplits;

}