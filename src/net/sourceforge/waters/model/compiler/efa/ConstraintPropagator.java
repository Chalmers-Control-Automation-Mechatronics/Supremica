//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   ConstraintPropagator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class ConstraintPropagator
{

  //#########################################################################
  //# Constructors
  ConstraintPropagator
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final Comparator<SimpleExpressionProxy> comparator,
     final SimpleExpressionCompiler compiler,
     final EFAVariableMap varmap)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mComparator = comparator;
    mSimpleExpressionCompiler = compiler;
    mDNFConverter = new DNFConverter(factory, optable, comparator);
    mVariableMap = varmap;
  }


  //#########################################################################
  //# Invocation
  CompiledClause propagate(final CompiledClause clause,
                           final BindingContext context)
    throws EvalException
  {
    try {
      final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
      final int size = literals.size();
      setup(context, size);
      mOpenLiterals.addAll(literals);
      return propagate();
    } finally {
      cleanup();
    }
  }

  CompiledClause propagate(final CompiledClause clause1,
                           final CompiledClause clause2,
                           final BindingContext context)
    throws EvalException
  {
    try {
      final Collection<SimpleExpressionProxy> literals1 =
        clause1.getLiterals();
      final Collection<SimpleExpressionProxy> literals2 =
        clause2.getLiterals();
      final int size = literals1.size() + literals2.size();
      setup(context, size);
      mOpenLiterals.addAll(literals1);
      mOpenLiterals.addAll(literals2);
      return propagate();
    } finally {
      cleanup();
    }
  }

  CompiledClause propagate(final CompiledClause clause,
                           final SimpleExpressionProxy varname,
                           final SimpleExpressionProxy value,
                           final BindingContext context)
    throws EvalException
  {
    try {
      final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
      final int size = literals.size();
      setup(context, size + 1);
      mOpenLiterals.addAll(literals);
      substitute(varname, value);
      final BinaryExpressionProxy equation = createEquation(varname, value);
      mProcessedEquations.add(equation);
      return propagate();
    } finally {
      cleanup();
    }
  }


  //#########################################################################
  //# Algorithm
  private void setup(final BindingContext context, final int size)
  {
    mRootContext = context;
    mIsFalse = false;
    mOpenLiterals = new TreeSet<SimpleExpressionProxy>(mComparator);
    mProcessedEquations = new ArrayList<BinaryExpressionProxy>(size);
  }

  private void cleanup()
  {
    mRootContext = null;
    mOpenLiterals = null;
    mProcessedEquations = null;
  }

  private CompiledClause propagate()
    throws EvalException
  {
    while (simplify()) {
      // nothing ...
    }
    if (mIsFalse) {
      return null;
    } else {
      final BinaryOperator andop = mOperatorTable.getAndOperator();
      final int size = mOpenLiterals.size() + mProcessedEquations.size();
      final CompiledClause result = new CompiledClause(andop, size);
      result.addAll(mProcessedEquations);
      result.addAll(mOpenLiterals);
      return result;
    }
  }

  private boolean simplify()
    throws EvalException
  {
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    final Iterator<SimpleExpressionProxy> iter = mOpenLiterals.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy literal = iter.next();
      if (!(literal instanceof BinaryExpressionProxy)) {
        continue;
      }
      final BinaryExpressionProxy equation = (BinaryExpressionProxy) literal;
      final BinaryOperator op = equation.getOperator();
      if (op != eqop) {
        continue;
      }
      final SimpleExpressionProxy lhs = equation.getLeft();
      final EFAVariable var = mVariableMap.getVariable(lhs);
      if (var == null) {
        continue;
      }
      final OccursChecker checker = OccursChecker.getInstance();
      final SimpleExpressionProxy rhs = equation.getRight();
      if (checker.occurs(lhs, rhs)) {
        continue;
      }
      iter.remove();
      mProcessedEquations.add(equation);
      substitute(lhs, rhs);
      return !mIsFalse;
    }
    return false;
  }

  private void substitute(final SimpleExpressionProxy varname,
                          final SimpleExpressionProxy replacement)
    throws EvalException
  {
    final BindingContext context =
      new SingleBindingContext(varname, replacement, mRootContext);
    final List<SimpleExpressionProxy> literals =
      new ArrayList<SimpleExpressionProxy>(mOpenLiterals);
    mOpenLiterals.clear();
    for (final SimpleExpressionProxy literal : literals) {
      final SimpleExpressionProxy simp =
        mSimpleExpressionCompiler.simplify(literal, context);
      if (isBooleanValue(simp)) {
        if (!getBooleanValue(simp)) {
          mIsFalse = true;
          return;
        }
      } else if (mOpenLiterals.add(simp)) {
        final SimpleExpressionProxy complement = getNegatedLiteral(simp);
        if (mOpenLiterals.contains(complement)) {
          mIsFalse = true;
          return;
        }
      }
    }
    final int numequations = mProcessedEquations.size();
    for (int i = 0; i < numequations; i++) {
      final BinaryExpressionProxy equation = mProcessedEquations.get(i);
      final SimpleExpressionProxy rhs = equation.getRight();
      final SimpleExpressionProxy simp =
        mSimpleExpressionCompiler.simplify(rhs, context);
      if (!rhs.equalsByContents(simp)) {
        final SimpleExpressionProxy lhs = equation.getLeft();
        final BinaryExpressionProxy newequation = createEquation(lhs, simp);
        mProcessedEquations.set(i, newequation);
      }
    }
  }

  private BinaryExpressionProxy createEquation
    (final SimpleExpressionProxy expr1,
     final SimpleExpressionProxy expr2)
  {
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    if (mComparator.compare(expr1, expr2) < 0) {
      return mFactory.createBinaryExpressionProxy(eqop, expr1, expr2);
    } else {
      return mFactory.createBinaryExpressionProxy(eqop, expr2, expr1);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy simplify(final SimpleExpressionProxy literal)
  {
    try {
      return mSimpleExpressionCompiler.simplify(literal, null);
    } catch (final EvalException exception) {
      throw exception.getRuntimeException();
    }
  }

  private boolean isBooleanValue(final SimpleExpressionProxy literal)
  {
    return mSimpleExpressionCompiler.isBooleanValue(literal);
  }

  private boolean getBooleanValue(final SimpleExpressionProxy literal)
  {
    try {
      return mSimpleExpressionCompiler.getBooleanValue(literal);
    } catch (final TypeMismatchException exception) {
      throw exception.getRuntimeException();
    }
  }

  private SimpleExpressionProxy getNegatedLiteral
    (final SimpleExpressionProxy literal)
  {
    try {
      return mDNFConverter.getNegatedLiteral(literal);
    } catch (final TypeMismatchException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final Comparator<SimpleExpressionProxy> mComparator;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final DNFConverter mDNFConverter;
  private final EFAVariableMap mVariableMap;

  private BindingContext mRootContext;
  private boolean mIsFalse;
  private Collection<SimpleExpressionProxy> mOpenLiterals;
  private List<BinaryExpressionProxy> mProcessedEquations;

}
