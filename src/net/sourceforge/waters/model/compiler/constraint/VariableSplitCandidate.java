//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mIteSplits = new ProxyAccessorHashMap<>(eq);
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

  @Override
  public boolean isDisjoint()
  {
    return true;
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
