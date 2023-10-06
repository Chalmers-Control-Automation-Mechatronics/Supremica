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

package net.sourceforge.waters.model.compiler.efsm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class EFSMConditionCompiler
{

  //#########################################################################
  //# Constructors
  public EFSMConditionCompiler(final ModuleProxyFactory factory,
                               final CompilerOperatorTable optable,
                               final CompilationInfo info,
                               final BindingContext context)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mCompilationInfo = info;
    mContext = context;
    mTopLevelVisitor = new TopLevelVisitor();
    mInnerLevelVisitor = new InnerLevelVisitor();
   }


  //#########################################################################
  //# Guard Compilation
  public ConstraintList compileCondition(final SimpleExpressionProxy cond)
    throws EvalException
  {
    return compile(cond);
  }

  public ConstraintList compileGuardActionBlock(final GuardActionBlockProxy ga)
    throws EvalException
  {
    if (ga == null) {
      return ConstraintList.TRUE;
    } else {
      final List<SimpleExpressionProxy> guards = ga.getGuards();
      final List<BinaryExpressionProxy> actions = ga.getActions();
      if (guards.isEmpty() && actions.isEmpty()) {
        throw new ActionSyntaxException("Empty guard/action block encountered!",
                                        ga);
      }
      return compile(ga);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ConstraintList compile(final Proxy proxy)
  {
    try {
      mCollectedConstraints = new LinkedList<>();
      proxy.acceptVisitor(mTopLevelVisitor);
      return new ConstraintList(mCollectedConstraints);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mCollectedConstraints = null;
    }
  }


  //#########################################################################
  //# Inner Class TopLevelVisitor
  private class TopLevelVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final BinaryOperator op = expr.getOperator();
      if (op == mOperatorTable.getAndOperator()) {
        final SimpleExpressionProxy lhs = expr.getLeft();
        lhs.acceptVisitor(this);
        final SimpleExpressionProxy rhs = expr.getRight();
        rhs.acceptVisitor(this);
        return null;
      } else if (mOperatorTable.isAssignment(expr)) {
        final SimpleExpressionProxy ident = expr.getLeft();
        final UnaryOperator next = mOperatorTable.getNextOperator();
        final UnaryExpressionProxy primedIdent =
          mFactory.createUnaryExpressionProxy(next, ident);
        mCompilationInfo.add(primedIdent, expr);
        final SimpleExpressionProxy arg = expr.getRight();
        final BinaryOperator eq = mOperatorTable.getEqualsOperator();
        final SimpleExpressionProxy rhs;
        if (op == mOperatorTable.getAssignmentOperator()) {
          rhs = arg;
        } else {
          final BinaryOperator transformer =
            mOperatorTable.getAssigningOperator(op);
          rhs = mFactory.createBinaryExpressionProxy(transformer, ident, arg);
          mCompilationInfo.add(rhs, expr);
        }
        final SimpleExpressionProxy eqn =
          mFactory.createBinaryExpressionProxy(eq, primedIdent, rhs);
        mCompilationInfo.add(eqn, expr);
        mCollectedConstraints.add(eqn);
        return null;
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy ga)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> guards = ga.getGuards();
      visitCollection(guards);
      final List<BinaryExpressionProxy> actions = ga.getActions();
      visitCollection(actions);
      return null;
    }

    @Override
    public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy compiled =
        (SimpleExpressionProxy) expr.acceptVisitor(mInnerLevelVisitor);
      mCollectedConstraints.add(compiled);
      return expr;
    }
  }


  //#########################################################################
  //# Inner Class InnerLevelVisitor
  private class InnerLevelVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public BinaryExpressionProxy visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final SimpleExpressionProxy compiledLHS =
        (SimpleExpressionProxy) lhs.acceptVisitor(this);
      final SimpleExpressionProxy rhs = expr.getRight();
      final SimpleExpressionProxy compiledRHS =
        (SimpleExpressionProxy) rhs.acceptVisitor(this);
      if (lhs == compiledLHS && rhs == compiledRHS) {
        return expr;
      } else {
        final BinaryOperator op = expr.getOperator();
        final BinaryExpressionProxy compiled =
          mFactory.createBinaryExpressionProxy(op, compiledLHS, compiledRHS);
        mCompilationInfo.add(compiled, expr);
        return compiled;
      }
    }

    @Override
    public FunctionCallExpressionProxy visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> args = expr.getArguments();
      final List<SimpleExpressionProxy> compiledArgs =
        new ArrayList<>(args.size());
      boolean change = false;
      for (final SimpleExpressionProxy arg : args) {
        final SimpleExpressionProxy compiledArg =
          (SimpleExpressionProxy) arg.acceptVisitor(this);
        compiledArgs.add(compiledArg);
        change |= arg != compiledArg;
      }
      if (change) {
        final String function = expr.getFunctionName();
        final FunctionCallExpressionProxy compiled =
          mFactory.createFunctionCallExpressionProxy(function, compiledArgs);
        mCompilationInfo.add(compiled, expr);
        return compiled;
      } else {
        return expr;
      }
    }

    @Override
    public SimpleExpressionProxy visitIdentifierProxy
      (final IdentifierProxy ident)
    {
      if (mPrimedParent == null || mContext.isEnumAtom(ident)) {
        return ident;
      } else if (ident == mPrimedParent.getSubTerm()) {
        return mPrimedParent;
      } else {
        final UnaryOperator op = mOperatorTable.getNextOperator();
        final UnaryExpressionProxy compiled =
          mFactory.createUnaryExpressionProxy(op, ident);
        mCompilationInfo.add(compiled, mPrimedParent);
        return compiled;
      }
    }

    @Override
    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return expr;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final UnaryOperator op = expr.getOperator();
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      if (op == mOperatorTable.getNextOperator()) {
        try {
          assert mPrimedParent == null;
          mPrimedParent = expr;
          return subTerm.acceptVisitor(this);
        } finally {
          mPrimedParent = null;
        }
      } else {
        final SimpleExpressionProxy compiledSubTerm =
          (SimpleExpressionProxy) subTerm.acceptVisitor(this);
        if (compiledSubTerm == subTerm) {
          return expr;
        } else {
          final UnaryExpressionProxy compiled =
            mFactory.createUnaryExpressionProxy(op, compiledSubTerm);
          mCompilationInfo.add(compiled, expr);
          return compiled;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private UnaryExpressionProxy mPrimedParent;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final CompilationInfo mCompilationInfo;
  private final BindingContext mContext;

  private final TopLevelVisitor mTopLevelVisitor;
  private final InnerLevelVisitor mInnerLevelVisitor;

  private List<SimpleExpressionProxy> mCollectedConstraints;

}
