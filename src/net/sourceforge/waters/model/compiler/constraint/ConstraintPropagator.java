//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   ConstraintPropagator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.
  CompilerExpressionComparator;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.
  SingleBindingVariableContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class ConstraintPropagator
{

  //#########################################################################
  //# Constructors
  public ConstraintPropagator
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final VariableContext root)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mContext = new ConstraintContext(root, this);
    mComparator = new CompilerExpressionComparator(optable, root);
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(factory, optable, false);
    mNormalizer = RelationNormalizationRule.createNormalRule(factory, optable);
    mNegator = RelationNormalizationRule.createNegatingRule(factory, optable);
    mNormalizationRules = new SimplificationRule[] {
      AndEliminationRule.createRule(factory, optable),
      mNormalizer,
      RelationNormalizationRule.createNegativeRule(factory, optable),
      TrueEliminationRule.createRule(factory, optable),
      FalseEliminationRule.createRule(factory, optable),
      DoubleNegationRule.createRule(factory, optable),
      DeMorgansOrRule.createRule(factory, optable),
      EqualsToTrueRule.createRule(factory, optable),
      EqualsToFalseRule.createRule(factory, optable),
      DisjunctionNormalizationRule.createNormalRule(factory, optable),
      DisjunctionNormalizationRule.createNegativeRule(factory, optable)
    };
    mRewriteRules = new SimplificationRule[] {
      PositiveLiteralRule.createRule(factory, optable),
      NegativeLiteralRule.createRule(factory, optable),
      EqualitySubstitutionRule.createRule(factory, optable),
      LeftNotEqualsRestrictionRule.createRule(factory, optable),
      LeftLessThanRestrictionRule.createRule(factory, optable),
      LeftLessEqualsRestrictionRule.createRule(factory, optable),
      RightLessThanRestrictionRule.createRule(factory, optable),
      RightLessEqualsRestrictionRule.createRule(factory, optable)
    };
    mUnprocessedConstraints = new LinkedList<SimpleExpressionProxy>();
    mNormalizedConstraints = new TreeSet<SimpleExpressionProxy>(mComparator);
    mProcessedEquations = new TreeSet<SimpleExpressionProxy>(mComparator);
    mIsFalse = false;
  }


  //#########################################################################
  //# Simple Access
  ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  CompilerOperatorTable getOperatorTable()
  {
    return mOperatorTable;
  }

  ConstraintContext getContext()
  {
    return mContext;
  }

  Comparator<SimpleExpressionProxy> getExpressionComparator()
  {
    return mComparator;
  }


  //#########################################################################
  //# Invocation
  public void init(final List<SimpleExpressionProxy> constraints)
  {
    reset();
    addConstraints(constraints);
  }

  public void reset()
  {
    mNormalizedConstraints.clear();
    mProcessedEquations.clear();
    mIsFalse = false;
  }

  public void addConstraints
    (final Collection<SimpleExpressionProxy> constraints)
  {
    for (final SimpleExpressionProxy constraint : constraints) {
      addConstraint(constraint);
    }
  }

  public void addConstraint(final SimpleExpressionProxy constraint)
  {
    mUnprocessedConstraints.add(constraint);
  }

  public List<SimpleExpressionProxy> getConstraints()
  {
    if (mIsFalse) {
      return null;
    } else {
      final int size =
        mProcessedEquations.size() + mNormalizedConstraints.size();
      final List<SimpleExpressionProxy> result =
        new ArrayList<SimpleExpressionProxy>(size);
      result.addAll(mProcessedEquations);
      result.addAll(mNormalizedConstraints);
      for (final SimpleExpressionProxy varname : mContext.getVariableNames()) {
        addRangeConstraints(varname, result);
      }
      return result;
    }
  }

  public boolean propagate()
    throws EvalException
  {
    int i = 0;
    boolean change = false;
    outer:
    while (!mIsFalse) {
      if (!mUnprocessedConstraints.isEmpty()) {
        // normalise unprocessed constraints first ...
        final SimpleExpressionProxy constraint =
          mUnprocessedConstraints.remove(0);
        final SimpleExpressionProxy simplified =
          mSimpleExpressionCompiler.simplify(constraint, mContext);
        for (final SimplificationRule rule : mNormalizationRules) {
          if (rule.match(simplified, this)) {
            rule.execute(this);
            change = true;
            continue outer;
          }
        }
        final SimpleExpressionProxy negation = getNegatedLiteral(simplified);
        if (mNormalizedConstraints.contains(negation) ||
            mProcessedEquations.contains(negation)) {
          setFalse();
          change = true;
          break;
        }
        mNormalizedConstraints.add(simplified);
      } else {
        // all constraints normalised ...
        for (final SimpleExpressionProxy constraint : mNormalizedConstraints) {
          for (final SimplificationRule rule : mRewriteRules) {
            if (rule.match(constraint, this)) {
              // System.err.println
              //   ("MATCH: " + rule.getClass().getName() + " " + constraint);
              if (rule.isMakingReplacement()) {
                mNormalizedConstraints.remove(constraint);
              }
              rule.execute(this);
              change = true;
              continue outer;
            }
          }
        }
        break;
      }
    }
    return change;
  }


  //#########################################################################
  //# Callbacks from Rules
  void processEquation(final SimpleExpressionProxy varname,
                       final SimpleExpressionProxy replacement,
                       final SimpleExpressionProxy eqn)
    throws EvalException
  {
    final VariableContext context =
      new SingleBindingVariableContext(varname, replacement, mContext);
      rewriteProcessedEquations(mContext);
    final Iterator<SimpleExpressionProxy> iter =
      mNormalizedConstraints.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy constraint = iter.next();
      final SimpleExpressionProxy simp =
        mSimpleExpressionCompiler.simplify(constraint, context);
      if (simp != constraint) {
        iter.remove();
        addConstraint(simp);
      }
    }
    rewriteProcessedEquations(context);
    mProcessedEquations.add(eqn);
  }

  void addProcessedEquation(final SimpleExpressionProxy varname,
                            final SimpleExpressionProxy value)
  {
    final BinaryOperator op = mOperatorTable.getEqualsOperator();
    final BinaryExpressionProxy eqn =
      mFactory.createBinaryExpressionProxy(op, varname, value);
    mProcessedEquations.add(eqn);
  }

  void restrictRange(final SimpleExpressionProxy varname,
                     final CompiledRange range)
    throws EvalException
  {
    if (mContext.restrictRange(varname, range)) {
      addConstraints(mNormalizedConstraints);
      mNormalizedConstraints.clear();
      rewriteProcessedEquations(mContext);
    }
  }

  void setFalse()
  {
    mIsFalse = true;
    mUnprocessedConstraints.clear();
    mNormalizedConstraints.clear();
    mProcessedEquations.clear();
  }

  SimpleExpressionProxy getNormalisedLiteral
    (final SimpleExpressionProxy literal, final boolean negated)
    throws EvalException
  {
    if (negated) {
      return getNegatedLiteral(literal);
    } else {
      return getNormalisedLiteral(literal);
    }
  }

  SimpleExpressionProxy getNormalisedLiteral(final SimpleExpressionProxy expr)
    throws EvalException
  {
    if (mNormalizer.match(expr, this)) {
      return mNormalizer.getResult(this);
    } else {
      return expr;
    }
  }

  SimpleExpressionProxy getNegatedLiteral(final SimpleExpressionProxy expr)
    throws EvalException
  {
    final UnaryOperator notop = mOperatorTable.getNotOperator();
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      if (unary.getOperator() == notop) {
        final SimpleExpressionProxy subterm = unary.getSubTerm();
        return getNormalisedLiteral(subterm);
      }
    } else if (mNegator.match(expr, this)) {
      return mNegator.getResult(this);
    }
    return mFactory.createUnaryExpressionProxy(notop, expr);
  }

  boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    return mSimpleExpressionCompiler.isAtomicValue(expr, mContext);
  }

  CompiledRange estimateRange(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mSimpleExpressionCompiler.estimateRange(expr, mContext);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void rewriteProcessedEquations(final VariableContext context)
    throws EvalException
  {
    final int size = mProcessedEquations.size();
    final List<SimpleExpressionProxy> rewritten =
      new ArrayList<SimpleExpressionProxy>(size);
    final Iterator<SimpleExpressionProxy> iter =
      mProcessedEquations.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy constraint = iter.next();
      final SimpleExpressionProxy simp =
        rewriteProcessedEquation(constraint, context);
      if (simp != constraint) {
        iter.remove();
        rewritten.add(simp);
      }
    }
    mProcessedEquations.addAll(rewritten);
  }

  private SimpleExpressionProxy rewriteProcessedEquation
    (final SimpleExpressionProxy expr, final VariableContext context)
    throws EvalException
  {
    if (!(expr instanceof BinaryExpressionProxy)) {
      return expr;
    }
    final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
    final SimpleExpressionProxy rhs = binary.getRight();
    final SimpleExpressionProxy simp =
      mSimpleExpressionCompiler.simplify(rhs, context);
    if (simp == rhs) {
      return expr;
    }
    final BinaryOperator op = binary.getOperator();
    final SimpleExpressionProxy lhs = binary.getLeft();
    return mFactory.createBinaryExpressionProxy(op, lhs, simp);
  }

  private void addRangeConstraints
    (final SimpleExpressionProxy varname,
     final Collection<SimpleExpressionProxy> constraints)
  {
    final CompiledRange restricted = mContext.getVariableRange(varname);
    final CompiledRange original = mContext.getOriginalRange(varname);
    if (restricted != original) {
      if (restricted instanceof CompiledIntRange) {
        final CompiledIntRange intrestricted = (CompiledIntRange) restricted;
        final CompiledIntRange intoriginal = (CompiledIntRange) original;
        final int lower = intrestricted.getLower();
        final int upper = intrestricted.getUpper();
        if (lower == upper) {
          final BinaryOperator op = mOperatorTable.getEqualsOperator();
          final IntConstantProxy intconst =
            mFactory.createIntConstantProxy(lower);
          final BinaryExpressionProxy constraint =
            mFactory.createBinaryExpressionProxy(op, varname, intconst);
          constraints.add(constraint);
        } else {
          final BinaryOperator op = mOperatorTable.getLessEqualsOperator();
          if (lower > intoriginal.getLower()) {
            final IntConstantProxy intconst =
              mFactory.createIntConstantProxy(lower);
            final BinaryExpressionProxy constraint =
              mFactory.createBinaryExpressionProxy(op, intconst, varname);
            constraints.add(constraint);
          }
          if (upper < intoriginal.getUpper()) {
            final IntConstantProxy intconst =
              mFactory.createIntConstantProxy(upper);
            final BinaryExpressionProxy constraint =
              mFactory.createBinaryExpressionProxy(op, varname, intconst);
            constraints.add(constraint);
          }
        }
      } else if (restricted instanceof CompiledEnumRange) {
        final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
        final BinaryOperator orop = mOperatorTable.getOrOperator();
        final Iterator<? extends SimpleExpressionProxy> iter =
          restricted.getValues().iterator();
        final SimpleExpressionProxy first = iter.next();
        SimpleExpressionProxy constraint =
          mFactory.createBinaryExpressionProxy(eqop, varname, first);
        while (iter.hasNext()) {
          final SimpleExpressionProxy next = iter.next();
          final SimpleExpressionProxy eqn =
            mFactory.createBinaryExpressionProxy(eqop, varname, next);
          constraint =
            mFactory.createBinaryExpressionProxy(orop, constraint, eqn);
        }
        constraints.add(constraint);
      } else {
        throw new ClassCastException
          ("Unknown type of range: " + restricted.getClass().getName() + "!");
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ConstraintContext mContext;
  private final Comparator<SimpleExpressionProxy> mComparator;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final RelationNormalizationRule mNormalizer;
  private final RelationNormalizationRule mNegator;
  private final SimplificationRule[] mNormalizationRules;
  private final SimplificationRule[] mRewriteRules;

  private List<SimpleExpressionProxy> mUnprocessedConstraints;
  private Collection<SimpleExpressionProxy> mNormalizedConstraints;
  private Collection<SimpleExpressionProxy> mProcessedEquations;
  private boolean mIsFalse;

}
