//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   GuardCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
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
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class GuardCompiler
{

  //#########################################################################
  //# Constructors
  GuardCompiler(final ModuleProxyFactory factory,
                final CompilerOperatorTable optable,
                final Comparator<SimpleExpressionProxy> comparator,
                final EFARangeEvaluator evaluator)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mDNFConverter = new DNFConverter(factory, optable, comparator);
    mEvaluator = evaluator;
    mCache =
      new HashMap<ProxyAccessor<GuardActionBlockProxy>,CompiledGuard>();
  }


  //#########################################################################
  //# Guard Compilation
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
  //# Transition Group Processing
  void makeDisjoint(final EFATransitionGroup group)
    throws TypeMismatchException
  {
    final List<EFATransition> list =
      new ArrayList<EFATransition>(group.getPartialTransitions());
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        final EFATransition trans1 = list.get(i);
        final CompiledClause cond1 = trans1.getConditions();
        final Set<NodeProxy> nodes1 = trans1.getSourceNodes();
        final EFATransition trans2 = list.get(j);
        final CompiledClause cond2 = trans2.getConditions();
        final Set<NodeProxy> nodes2 = trans2.getSourceNodes();
        if (isDisjoint(cond1, cond2) || nodes1.equals(nodes2)) {
          continue;
        } else if (nodes1.containsAll(nodes2)) {
          // strengthen : cond2 >> !cond1 & cond2
          final Collection<CompiledClause> strengthened = 
            strengthenByNegation(cond2, cond1);
          group.replaceTransitions(cond2, strengthened);
          replaceInList(list, j, group, strengthened);
        } else if (nodes2.containsAll(nodes1)) {
          // strengthen : cond1 >> cond1 & !cond2
          final Collection<CompiledClause> strengthened = 
            strengthenByNegation(cond1, cond2);
          group.replaceTransitions(cond1, strengthened);
          replaceInList(list, i, group, strengthened);
        } else {
          // split : cond1 >> cond1 & cond2, cond1 & !cond2
          // split : cond2 >> cond1 & cond2, !cond1 & cond2
          final CompiledClause conjunction = buildConjunction(cond1, cond2);
          final Collection<CompiledClause> strengthened1 = 
            strengthenByNegation(cond1, cond2);
          final Collection<CompiledClause> strengthened2 = 
            strengthenByNegation(cond2, cond1);
          strengthened1.add(conjunction);
          strengthened2.add(conjunction);
          group.replaceTransitions(cond2, strengthened2);
          replaceInList(list, j, group, strengthened2);
          group.replaceTransitions(cond1, strengthened1);
          replaceInList(list, i, group, strengthened1);
        }
      }
    }
  }


  //#########################################################################
  //# Guard Compilation Auxiliaries
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
  //# Transition Group Processing Axiliaries
  private boolean isDisjoint(final CompiledClause cond1,
                             final CompiledClause cond2)
    throws TypeMismatchException
  {
    if (cond1.size() <= cond2.size()) {
      for (final SimpleExpressionProxy literal : cond1.getLiterals()) {
        final SimpleExpressionProxy complement =
          mDNFConverter.getNegatedLiteral(literal);
        if (cond2.contains(complement)) {
          return true;
        }
      }
      return false;
    } else {
      return isDisjoint(cond2, cond1);
    }
  }

  private CompiledClause buildConjunction(final CompiledClause cond1,
                                          final CompiledClause cond2)
  {
    final BinaryOperator op = cond1.getOperator();
    final int size = cond1.size() + cond2.size();
    final Collection<SimpleExpressionProxy> literals1 = cond1.getLiterals();
    final Collection<SimpleExpressionProxy> literals2 = cond2.getLiterals();
    final CompiledClause conjunction = new CompiledClause(op, size);
    conjunction.addAll(literals1);
    conjunction.addAll(literals2);
    return conjunction;
  }

  private Collection<CompiledClause> strengthenByNegation
    (final CompiledClause cond, final CompiledClause additions)
    throws TypeMismatchException
  {
    final int asize = additions.size();
    final Collection<CompiledClause> result =
      new ArrayList<CompiledClause>(asize);
    final BinaryOperator op = cond.getOperator();
    final int csize = cond.size() + 1;
    final Collection<SimpleExpressionProxy> cliterals = cond.getLiterals();
    for (final SimpleExpressionProxy literal : additions.getLiterals()) {
      final SimpleExpressionProxy complement =
        mDNFConverter.getNegatedLiteral(literal);
      if (!cond.contains(complement)) {
        final CompiledClause clause = new CompiledClause(op, csize);
        clause.addAll(cliterals);
        clause.add(complement);
        result.add(clause);
      }
    }
    return result;
  }

  private void replaceInList(final List<EFATransition> list,
                             final int index,
                             final EFATransitionGroup group,
                             final Collection<CompiledClause> conds)
  {
    final Iterator<CompiledClause> iter = conds.iterator();
    final CompiledClause cond1 = iter.next();
    final EFATransition trans1 = group.getPartialTransition(cond1);
    list.set(index, trans1);
    if (iter.hasNext()) {
      final int listsize = list.size();
      final int tailsize = listsize - index - 1;
      final List<EFATransition> tail = 
        tailsize == 0 ? null : new ArrayList<EFATransition>(tailsize);
      for (int i = index + 1; i < listsize; i++) {
        tail.add(list.get(i));
      }
      for (int i = listsize - 1; i > index; i--) {
        list.remove(i);
      }
      while (iter.hasNext()) {
        final CompiledClause cond = iter.next();
        final EFATransition trans = group.getPartialTransition(cond);
        list.add(trans);
      }
      if (tail != null) {
        list.addAll(tail);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final DNFConverter mDNFConverter;
  private final EFARangeEvaluator mEvaluator;

  private final Map<ProxyAccessor<GuardActionBlockProxy>,CompiledGuard> mCache;

}
