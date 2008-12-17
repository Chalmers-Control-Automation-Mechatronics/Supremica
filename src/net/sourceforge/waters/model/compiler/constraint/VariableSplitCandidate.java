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
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
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


  //#########################################################################
  //# net.sourceforge.waters.model.compiler.constraint.SplitCandidate
  public void recall(final ConstraintPropagator propagator)
  {
    propagator.recallBinding(mVariableName);
  }

  public List<SimpleExpressionProxy> getSplitExpressions
    (final ModuleProxyFactory factory, final CompilerOperatorTable optable)
  {
    final int size = mRange.size();
    final List<SimpleExpressionProxy> result =
      new ArrayList<SimpleExpressionProxy>(size);
    if (hasBooleanRange()) {
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
  int getNumberOfOccurrences()
  {
    return mNumberOfOccurrences;
  }

  int getSplitSize()
  {
    return mRange.size();
  }

  boolean getOccursWithNext()
  {
    return mOccursWithNext;
  }

  int getKindValue()
  {
    return AbstractSplitCandidate.VARIABLE_SPLIT;
  }

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

}