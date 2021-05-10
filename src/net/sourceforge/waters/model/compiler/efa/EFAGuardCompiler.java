//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class EFAGuardCompiler
{

  //#########################################################################
  //# Constructors
  public EFAGuardCompiler(final ModuleProxyFactory factory,
                          final CompilerOperatorTable optable)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mFactory = factory;
    mOperatorTable = optable;
    mGuardChecker = new GuardChecker();
    mCache = new ProxyAccessorHashMap<>(eq);
  }


  //#########################################################################
  //# Guard Compilation
  public ConstraintList getCompiledGuard(final GuardActionBlockProxy block)
    throws EvalException
  {
    final ProxyAccessor<GuardActionBlockProxy> accessor =
      mCache.createAccessor(block);
    final ConstraintList cached = mCache.get(accessor);
    if (cached != null) {
      return cached;
    }
    final ConstraintList result = computeConstraintList(block);
    mCache.put(accessor, result);
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  private ConstraintList computeConstraintList
    (final GuardActionBlockProxy block)
    throws EvalException
  {
    final List<SimpleExpressionProxy> guards = block.getGuards();
    final List<BinaryExpressionProxy> actions = block.getActions();
    if (guards.isEmpty() && actions.isEmpty()) {
      throw new ActionSyntaxException("Empty guard/action block encountered!",
                                      block);
    }
    mGuardChecker.checkGuards(guards);
    final int size = guards.size() + actions.size();
    final List<SimpleExpressionProxy> list =
      new ArrayList<SimpleExpressionProxy>(size);
    list.addAll(guards);
    for (final BinaryExpressionProxy action : actions) {
      final SimpleExpressionProxy norm = convertAction(action);
      list.add(norm);
    }
    return new ConstraintList(list);
  }

  private SimpleExpressionProxy convertAction
    (final BinaryExpressionProxy action)
    throws EvalException
  {
    final SimpleExpressionProxy lhs = action.getLeft();
    if (!(lhs instanceof IdentifierProxy)) {
      throw new ActionSyntaxException(action, lhs);
    }
    mGuardChecker.checkExpression(lhs);
    final IdentifierProxy ident = (IdentifierProxy) lhs;
    final SimpleExpressionProxy expr = action.getRight();
    final BinaryOperator assignment = action.getOperator();
    final BinaryOperator op = mOperatorTable.getAssigningOperator(assignment);
    final BinaryOperator assop = mOperatorTable.getAssignmentOperator();
    final SimpleExpressionProxy newexpr;
    if (assignment == assop) {
      newexpr = expr;
    } else if (op != null) {
      newexpr = mFactory.createBinaryExpressionProxy(op, ident, expr);
    } else {
      throw new ActionSyntaxException(action);
    }
    mGuardChecker.checkExpression(expr);
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    final UnaryExpressionProxy nextident =
      mFactory.createUnaryExpressionProxy(nextop, ident);
    return mFactory.createBinaryExpressionProxy(eqop, nextident, newexpr);
  }


  //#########################################################################
  //# Inner Class GuardChecker
  /**
   * A visitor to search a guard or other expression for occurrences of
   * assignment operators such as = or&nbsp;+=, in order to report an error
   * in such cases.
   */
  private class GuardChecker extends DescendingModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    void checkGuards(final Collection<SimpleExpressionProxy> guards)
      throws EvalException
    {
      try {
        mWhere = "guard";
        for (final SimpleExpressionProxy guard : guards) {
          guard.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    void checkExpression(final SimpleExpressionProxy expr)
      throws EvalException
    {
      try {
        mWhere = "expression";
        expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final BinaryOperator op = expr.getOperator();
      if (op == mOperatorTable.getAssignmentOperator() ||
          mOperatorTable.getAssigningOperator(op) != null) {
        final ActionSyntaxException exception =
          new ActionSyntaxException(expr, mWhere);
        throw wrap(exception);
      }
      return super.visitBinaryExpressionProxy(expr);
    }

    //#######################################################################
    //# Data Members
    private String mWhere;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final GuardChecker mGuardChecker;

  private final ProxyAccessorMap<GuardActionBlockProxy,ConstraintList> mCache;

}
