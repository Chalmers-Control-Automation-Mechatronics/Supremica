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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A split candidate resulting from a disjunction.
 *
 * Examples showing the usefulness for disjunctive split candidates:
 * <UL>
 * <LI><CODE>examples/waters/tests/compiler/efsm/alice_room.wmod</CODE></LI>
 * <LI><CODE>examples/waters/tests/compiler/efsm/batch_tank_vout.wmod</CODE></LI>
 * <LI><CODE>examples/waters/tests/compiler/efsm/GlobalAndLocalVariables.wmod</CODE></LI>
 * </UL>
 *
 * @see SplitComputer
 * @author Robi Malik
 */

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
  @Override
  public void recall(final ConstraintPropagator propagator)
  {
    propagator.removeConstraint(mDisjunction);
  }

  @Override
  public List<SimpleExpressionProxy> getSplitExpressions
    (final ModuleProxyFactory factory, final CompilerOperatorTable optable)
  {
    final DisjunctionVisitor visitor =
      new DisjunctionVisitor(factory, optable);
    return visitor.collect();
  }

  @Override
  public boolean isDisjoint()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.compiler.constraint.AbstractSplitCandidate
  @Override
  int getNumberOfOccurrences()
  {
    return 2;
  }

  @Override
  int getSplitSize()
  {
    return mSplitSize;
  }

  @Override
  boolean getOccursWithNext()
  {
    return false;
  }

  @Override
  int getKindValue()
  {
    return AbstractSplitCandidate.DISJUNCTION_SPLIT;
  }

  @Override
  SimpleExpressionProxy getSplitExpression()
  {
    return mDisjunction;
  }


  //#########################################################################
  //# Inner Class DisjunctionVisitor
  private class DisjunctionVisitor
    extends DefaultModuleProxyVisitor
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
        mResult = new ArrayList<>(mSplitSize);
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
    @Override
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

    @Override
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
