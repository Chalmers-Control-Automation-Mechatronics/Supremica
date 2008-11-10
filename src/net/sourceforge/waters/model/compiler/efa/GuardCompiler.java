//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   GuardCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
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
                final CompilerOperatorTable optable,
                final Comparator<SimpleExpressionProxy> comparator,
                final EFASimpleExpressionEvaluator evaluator)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mDNFConverter = new DNFConverter(factory, optable, comparator);
    mEvaluator = evaluator;
    mCache =
      new HashMap<ProxyAccessor<GuardActionBlockProxy>,CompiledGuard>();
  }


  //#########################################################################
  //# Invocation
  CompiledGuard getCompiledGuard(final GuardActionBlockProxy block)
    throws EvalException
  {
    final ProxyAccessor<GuardActionBlockProxy> accessor =
      new ProxyAccessorByContents<GuardActionBlockProxy>(block);
    final CompiledGuard cached = mCache.get(accessor);
    if (cached != null) {
      return cached;
    }
    final CompiledGuard result = computeNormalForm(block);
    mCache.put(accessor, result);
    return result;
  }

  CompiledGuard getComplementaryGuard
    (final Collection<SimpleExpressionProxy> guards)
    throws EvalException
  {
    SimpleExpressionProxy expr = null;
    final UnaryOperator notop = mOperatorTable.getNotOperator();
    for (final SimpleExpressionProxy guard : guards) {
      final SimpleExpressionProxy notguard =
        mFactory.createUnaryExpressionProxy(notop, guard);
      expr = combine(expr, notguard);
    }
    final CompiledNormalForm dnf;
    if (expr == null) {
      dnf = CompiledNormalForm.getTrueDNF();
    } else {
      dnf = mDNFConverter.convertToDNF(expr);
      if (dnf.isFalse()) {
        return null;
      }
    }
    return new CompiledGuard(expr, dnf);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledGuard computeNormalForm
    (final GuardActionBlockProxy block)
    throws EvalException
  {
    final List<SimpleExpressionProxy> guards = block.getGuards();
    final List<BinaryExpressionProxy> actions = block.getActions();
    if (guards.isEmpty() && actions.isEmpty()) {
      throw new ActionSyntaxException("Empty guard/action block encountered!",
                                      block);
    }
    SimpleExpressionProxy expr = null;
    for (final SimpleExpressionProxy guard : guards) {
      final CompiledIntRange range = mEvaluator.evalBooleanRange(guard);
      if (range.getLower() == 1) {
        continue;
      } else if (range.getUpper() == 0) {
        final CompiledNormalForm dnf = CompiledNormalForm.getFalseDNF();
        return new CompiledGuard(expr, dnf);
      } else {
        expr = combine(expr, guard);
      }
    }
    for (final BinaryExpressionProxy action : actions) {
      final SimpleExpressionProxy norm = convertAction(action);
      expr = combine(expr, norm);
    }
    final CompiledNormalForm dnf;
    if (expr == null) {
      dnf = CompiledNormalForm.getTrueDNF();
    } else {
      dnf = mDNFConverter.convertToDNF(expr);
    }
    return new CompiledGuard(expr, dnf);
  }

  private SimpleExpressionProxy combine(final SimpleExpressionProxy lhs,
                                        final SimpleExpressionProxy rhs)
  {
    if (lhs == null) {
      return rhs;
    } else if (rhs == null) {
      return lhs;
    } else {
      final BinaryOperator op = mOperatorTable.getAndOperator();
      return mFactory.createBinaryExpressionProxy(op, lhs, rhs);
    }
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
    final CompiledRange irange = mEvaluator.evalRange(ident);
    final SimpleExpressionProxy expr = action.getRight();
    final BinaryOperator assignment = action.getOperator();
    final BinaryOperator op = mOperatorTable.getAssigningOperator(assignment);
    final BinaryOperator assop = mOperatorTable.getAssignmentOperator();
    final SimpleExpressionProxy newexpr;
    if (assignment == assop) {
      final CompiledRange erange = mEvaluator.evalRange(expr);
      if (irange.intersects(erange)) {
        newexpr = expr;
      } else {
        throw new TypeMismatchException(expr, erange.toString());
      }
    } else if (op != null) {
      mEvaluator.evalIntRange(expr);
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
  private final DNFConverter mDNFConverter;
  private final EFASimpleExpressionEvaluator mEvaluator;

  private final Map<ProxyAccessor<GuardActionBlockProxy>,CompiledGuard> mCache;

}
