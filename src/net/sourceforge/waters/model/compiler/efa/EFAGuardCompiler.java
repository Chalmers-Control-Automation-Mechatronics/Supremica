//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAGuardCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class EFAGuardCompiler
{

  //#########################################################################
  //# Constructors
  EFAGuardCompiler(final ModuleProxyFactory factory,
                   final CompilerOperatorTable optable)
  {
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mFactory = factory;
    mOperatorTable = optable;
    mCache =
      new ProxyAccessorHashMap<GuardActionBlockProxy,ConstraintList>(eq);
  }


  //#########################################################################
  //# Guard Compilation
  ConstraintList getCompiledGuard(final GuardActionBlockProxy block)
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
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    final UnaryExpressionProxy nextident =
      mFactory.createUnaryExpressionProxy(nextop, ident);
    return mFactory.createBinaryExpressionProxy(eqop, nextident, newexpr);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;

  private final ProxyAccessorMap<GuardActionBlockProxy,ConstraintList> mCache;

}
