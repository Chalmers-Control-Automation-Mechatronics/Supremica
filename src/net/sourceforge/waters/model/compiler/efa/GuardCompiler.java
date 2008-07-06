//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   GuardCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class GuardCompiler
{

  //#########################################################################
  //# Constructors
  GuardCompiler(final ModuleProxyFactory factory,
                final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOpTable = optable;
    mDNFConverter = new DNFConverter(factory, optable);
    mCache =
      new HashMap<ProxyAccessor<GuardActionBlockProxy>,CompiledNormalForm>();
  }


  //#########################################################################
  //# Invocation
  CompiledNormalForm getCompiledGuard(final GuardActionBlockProxy block)
    throws EvalException
  {
    final ProxyAccessor<GuardActionBlockProxy> accessor =
      new ProxyAccessorByContents<GuardActionBlockProxy>(block);
    final CompiledNormalForm cached = mCache.get(accessor);
    if (cached != null) {
      return cached;
    }
    final CompiledNormalForm cnf = computeNormalForm(block);
    mCache.put(accessor, cnf);
    return cnf;
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledNormalForm computeNormalForm
    (final GuardActionBlockProxy block)
    throws EvalException
  {
    SimpleExpressionProxy expr = null;
    for (final SimpleExpressionProxy guard : block.getGuards()) {
      expr = combine(expr, guard);
    }
    for (final BinaryExpressionProxy action : block.getActions()) {
      final SimpleExpressionProxy norm = convertAction(action);
      expr = combine(expr, norm);
    }
    if (expr != null) {
      return mDNFConverter.convertToCNF(expr);
    } else {
      throw new ActionSyntaxException("Empty guard/action block encountered!",
                                      block);
    }
  }

  private SimpleExpressionProxy combine(final SimpleExpressionProxy lhs,
                                        final SimpleExpressionProxy rhs)
  {
    if (lhs == null) {
      return rhs;
    } else if (rhs == null) {
      return lhs;
    } else {
      final BinaryOperator op = mOpTable.getAndOperator();
      return mFactory.createBinaryExpressionProxy(op, lhs, rhs);
    }
  }

  private SimpleExpressionProxy convertAction
    (final BinaryExpressionProxy action)
    throws ActionSyntaxException
  {
    final SimpleExpressionProxy lhs = action.getLeft();
    if (!(lhs instanceof IdentifierProxy)) {
      throw new ActionSyntaxException(action, lhs);
    }
    final IdentifierProxy ident = (IdentifierProxy) lhs;
    final SimpleExpressionProxy expr = action.getRight();
    final BinaryOperator assignment = action.getOperator();
    final BinaryOperator op = mOpTable.getAssigningOperator(assignment);
    final BinaryOperator eqop = mOpTable.getAssignmentOperator();
    final SimpleExpressionProxy newexpr;
    if (op != null) {
      newexpr = mFactory.createBinaryExpressionProxy(op, ident, expr);
    } else if (op == eqop) {
      newexpr = expr;
    } else {
      throw new ActionSyntaxException(action);
    }
    final UnaryOperator nextop = mOpTable.getNextOperator();
    final UnaryExpressionProxy nextident =
      mFactory.createUnaryExpressionProxy(nextop, ident);
    return mFactory.createBinaryExpressionProxy(eqop, nextident, newexpr);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOpTable;
  private final DNFConverter mDNFConverter;

  private final Map<ProxyAccessor<GuardActionBlockProxy>,CompiledNormalForm>
    mCache;

}
