//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

import org.apache.log4j.Logger;


/**
 * <P>A constraint propagator used to simplify expressions found in
 * guard/action blocks.</P>
 *
 * <P>The constraint propagator is initialised with a list of formulas
 * understood as a conjunction of constraints, and a context providing
 * values bound to symbols and ranges of variables. It attempts to simplify
 * these constraints to produce a simpler list of expressions.</P>
 *
 * <P>The constraint propagator is not complete. It can handle equality
 * substitutions, some linear arithmetic, and some Boolean normalisation.
 * More complex formulas, for example involving disjunctions or non-linear
 * arithmetic, are not simplified.</P>
 *
 * <P>The basic usage pattern of the constraint propagator is as follows.</P>
 *
 * <PRE>  {@link ModuleProxyFactory} factory = {@link net.sourceforge.waters.plain.module.ModuleElementFactory#getInstance() ModuleElementFactory.getInstance()};
 *  {@link CompilerOperatorTable} optable = {@link CompilerOperatorTable#getInstance()};
 *  ConstraintPropagator propagator =
 *    new {@link #ConstraintPropagator(ModuleProxyFactory, CompilerOperatorTable, VariableContext) ConstraintPropagator}(factory, optable, context);
 *  propagator.{@link #addConstraints(ConstraintList) addConstraints}(constraints);  // load constraints
 *  propagator.{@link #propagate()};  // simplify
 *  {@link ConstraintList} result = propagator.{@link #getAllConstraints()};  // get result</PRE>
 *
 * @author Robi Malik
 */
public class ConstraintPropagator
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a new constraint propagator.
   * @param factory  Factory used to create expressions.
   * @param optable  Operator table to provide operators.
   * @param root     Context providing values of bound symbols and
   *                 ranges of EFA variables.
   */
  public ConstraintPropagator(final ModuleProxyFactory factory,
                              final CompilerOperatorTable optable,
                              final VariableContext root)
  {
    this(factory, new CompilationInfo(false, false), optable, root);
  }

  /**
   * Creates a new constraint propagator.
   * @param factory  Factory used to create expressions.
   * @param optable  Operator table to provide operators.
   * @param root     Context providing values of bound symbols and
   *                 ranges of EFA variables.
   */
  public ConstraintPropagator(final ModuleProxyFactory factory,
                              final CompilationInfo compilationInfo,
                              final CompilerOperatorTable optable,
                              final VariableContext root)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mContext = new ConstraintContext(root);
    mListComparator = new ExpressionComparator(optable);
    mEquationComparator =
      new RelationNormalizationComparator(optable, mContext);
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(factory, compilationInfo, optable, false);
    mPrimedVariableCollector = new PrimedVariableCollector(optable, root);
    mNormalizer = RelationNormalizationRule.createNormalRule(factory, optable);
    mNegator = RelationNormalizationRule.createNegatingRule(factory, optable);
    mNormalizationRules = new SimplificationRule[] {
      AndEliminationRule.createRule(factory, optable),
      RelationNormalizationRule.createNegativeRule(factory, optable),
      SumSimplificationRule.createRule(factory, optable),
      mNormalizer,
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
      BooleanLiteralRule.createPositiveLiteralRule(factory, optable),
      BooleanLiteralRule.createNegativeLiteralRule(factory, optable),
      EqualitySubstitutionRule.createRule(factory, optable),
      LeftNotEqualsRestrictionRule.createRule(factory, optable),
      LeftLessThanRestrictionRule.createRule(factory, optable),
      LeftLessEqualsRestrictionRule.createRule(factory, optable),
      RightLessThanRestrictionRule.createRule(factory, optable),
      RightLessEqualsRestrictionRule.createRule(factory, optable)
    };
    mUnprocessedConstraints = new LinkedList<SimpleExpressionProxy>();
    mNormalizedConstraints = new TreeSet<>(mListComparator);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mPrimedVariables = new ProxyAccessorHashSet<UnaryExpressionProxy>(eq);
    mIsUnsatisfiable = false;
    mNumberOfInvocations = 0;
  }

  /**
   * Duplicates a constraint propagator.
   * This constructor creates a new constraint propagator, which is
   * initialised to exactly the same state as the given constraint
   * propagator. It has the same context and starts with the same
   * bindings and formulas.
   */
  public ConstraintPropagator(final ConstraintPropagator propagator)
  {
    mFactory = propagator.mFactory;
    mOperatorTable = propagator.mOperatorTable;
    mContext = new ConstraintContext(propagator.mContext);
    mListComparator = propagator.mListComparator;
    mEquationComparator = propagator.mEquationComparator;
    mSimpleExpressionCompiler = propagator.mSimpleExpressionCompiler;
    mPrimedVariableCollector = propagator.mPrimedVariableCollector;
    mNormalizer = propagator.mNormalizer;
    mNegator = propagator.mNegator;
    mNormalizationRules = propagator.mNormalizationRules;
    mRewriteRules = propagator.mRewriteRules;
    mUnprocessedConstraints =
      new LinkedList<>(propagator.mUnprocessedConstraints);
    mNormalizedConstraints = new TreeSet<>(mListComparator);
    mNormalizedConstraints.addAll(propagator.mNormalizedConstraints);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mPrimedVariables =
      new ProxyAccessorHashSet<>(eq, propagator.mPrimedVariables);
    mIsUnsatisfiable = propagator.mIsUnsatisfiable;
    mNumberOfInvocations = 0;
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

  Comparator<SimpleExpressionProxy> getListComparator()
  {
    return mListComparator;
  }

  Comparator<SimpleExpressionProxy> getEquationComparator()
  {
    return mEquationComparator;
  }


  //#########################################################################
  //# Loading
  /**
   * Initialises this constraint propagator.
   * This method clears any constraints and bindings,
   * and replaces them by the contents of the given list.
   * @param clist  New list of constraints to replace previous state.
   */
  public void init(final ConstraintList clist)
  {
    reset();
    addConstraints(clist);
  }

  /**
   * Resets this constraint propagator.
   * This method clears all constraints and bindings.
   */
  public void reset()
  {
    mContext.reset();
    mUnprocessedConstraints.clear();
    mNormalizedConstraints.clear();
    mPrimedVariables.clear();
    mIsUnsatisfiable = false;
  }

  /**
   * <P>Adds the given constraints to this constraint propagator.</P>
   * <P>This method does not clear any constraints or bindings already
   * present. It merely adds the new constraints to its list unprocessed
   * constraints, further restricting the present state.</P>
   * <P>The additional constraints are not automatically simplified.
   * To simplify, {@link #propagate()} should be called.</P>
   * @param clist  List of constraints to be added.
   */
  public void addConstraints(final ConstraintList clist)
  {
    addConstraints(clist.getConstraints());
  }

  /**
   * <P>Adds the given constraints to this constraint propagator.</P>
   * <P>This method does not clear any constraints or bindings already
   * present. It merely adds the new constraints to its list unprocessed
   * constraints, further restricting the present state.</P>
   * <P>The additional constraints are not automatically simplified.
   * To simplify, {@link #propagate()} should be called.</P>
   * @param constraints  List of constraints to be added.
   */
  public void addConstraints(final Collection<SimpleExpressionProxy> constraints)
  {
    for (final SimpleExpressionProxy constraint : constraints) {
      addConstraint(constraint);
    }
  }

  /**
   * <P>Adds the given constraint to this constraint propagator.</P>
   * <P>This method does not clear any constraints or bindings already
   * present. It merely adds the new constraint to its list unprocessed
   * constraints, further restricting the present state.</P>
   * <P>The additional constraint is not automatically simplified.
   * To simplify, {@link #propagate()} should be called.</P>
   * @param constraint  Constraint to be added.
   */
  public void addConstraint(final SimpleExpressionProxy constraint)
  {
    assert constraint != null;
    if (!mIsUnsatisfiable) {
      mUnprocessedConstraints.add(constraint);
      addPrimedVariables(constraint);
    }
  }

  /**
   * <P>Adds the negation of the given constraint list to this constraint
   * propagator.</P>
   * <P>This method does not clear any constraints or bindings
   * already present. It merely adds the new constraint to its list
   * unprocessed constraints, further restricting the present state.</P>
   * <P>The additional constraints are not automatically simplified.
   * To simplify, {@link #propagate()} should be called.</P>
   * @param clist  List of constraints to be negated and added.
   *               If the constraint list contains multiple constraints
   *               in conjunction, this results in a disjunctive constraint
   *               added to this constraint propagator.
   */
  public void addNegation(final ConstraintList clist)
    throws EvalException
  {
    if (clist.isTrue()) {
      setFalse();
    } else if (!mIsUnsatisfiable) {
      final BinaryOperator op = mOperatorTable.getOrOperator();
      SimpleExpressionProxy expr = null;
      for (final SimpleExpressionProxy constraint : clist.getConstraints()) {
        final SimpleExpressionProxy negation = getNegatedLiteral(constraint);
        if (expr == null) {
          expr = negation;
        } else {
          expr = mFactory.createBinaryExpressionProxy(op, expr, negation);
        }
      }
      addConstraint(expr);
    }
  }

  /**
   * Adds all primed variables in the given expressions to this constraint
   * propagator. This adds variables to the constraint propagator without
   * adding constraints, in order to ensure the output mentions these
   * variables using equalities such as x'==x'.
   */
  public void addPrimedVariables
    (final Collection<SimpleExpressionProxy> constraints)
  {
    for (final SimpleExpressionProxy constraint : constraints) {
      addPrimedVariables(constraint);
    }
  }

  /**
   * Adds all primed variables in the given expression to this constraint
   * propagator. This adds variables to the constraint propagator without
   * adding constraints, in order to ensure the output mentions these
   * variables using equalities such as x'==x'.
   */
  public void addPrimedVariables(final SimpleExpressionProxy constraint)
  {
    mPrimedVariableCollector.collectPrimedVariables(constraint,
                                                    mPrimedVariables);
  }

  /**
   * Removes the given variable from the constraint propagator.
   * This method deletes any bindings associated with the variable in
   * its primed and unprimed form, and removes the variable from the
   * set of primed variables. This will remove all equations mentioning the
   * variable from the constraint propagator output.
   * @param  varName  The name of the variable to be removed,
   *                  without prime.
   */
  public void removeVariable(final IdentifierProxy varName)
  {
    final UnaryOperator prime = mOperatorTable.getNextOperator();
    final UnaryExpressionProxy unary =
      mFactory.createUnaryExpressionProxy(prime, varName);
    mContext.removeBinding(varName);
    mContext.removeBinding(unary);
    mPrimedVariables.removeProxy(unary);
  }

  /**
   * Removes unchanged primed variables from this constraint propagator.
   * This method checks for primed variables with bindings that are equal
   * to the current values of the variables, and removes them. This
   * suppresses equations of the form x'==x in the output, provided that
   * x' does not appear elsewhere.
   */
  public void removeUnchangedVariables()
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final int numPrimed = mPrimedVariables.size();
    final ProxyAccessorSet<UnaryExpressionProxy> primedVars =
      new ProxyAccessorHashSet<>(eq, numPrimed);
    mPrimedVariableCollector.collectPrimedVariables
      (mNormalizedConstraints, primedVars);
    mContext.collectPrimedVariables(primedVars);
    final List<UnaryExpressionProxy> victims = new LinkedList<>();
    for (final UnaryExpressionProxy primed : mPrimedVariables) {
      if (!primedVars.containsProxy(primed)) {
        final SimpleExpressionProxy varName = primed.getSubTerm();
        final SimpleExpressionProxy primedBinding =
          mContext.getBoundExpression(primed);
        if (primedBinding == null) {
          continue;
        } else if (eq.equals(primedBinding, varName)) {
          victims.add(primed);
          continue;
        }
        final SimpleExpressionProxy varBinding =
          mContext.getBoundExpression(varName);
        if (eq.equals(primedBinding, varBinding)) {
          victims.add(primed);
        }
      }
    }
    for (final UnaryExpressionProxy victim : victims) {
      mContext.removeBinding(victim);
      mPrimedVariables.removeProxy(victim);
    }
  }


  //#########################################################################
  //# Invocation
  /**
   * Simplifies the current set of constraints.
   * This method attempts to simplify the given set of constraints under
   * the current bindings as much as possible. It applies all available
   * simplifications rules until no further simplification is possible.
   * It changes the state of the constraint propagator to reflect any
   * changes, and the results can be retrieved using the methods
   * {@link #isUnsatisfiable()}, {@link #getAllConstraints()}, and
   * {@link #getContext()}.
   */
  public void propagate() throws EvalException
  {
    mNumberOfInvocations++;
    outer:
    while (!mIsUnsatisfiable) {
      if (!mUnprocessedConstraints.isEmpty()) {
        // Normalise unprocessed constraints first.
        final SimpleExpressionProxy constraint =
          mUnprocessedConstraints.remove(0);
        final SimpleExpressionProxy simplified =
          mSimpleExpressionCompiler.simplify(constraint, mContext);
        for (final SimplificationRule rule : mNormalizationRules) {
          if (rule.match(simplified, this)) {
            rule.execute(this);
            continue outer;
          }
        }
        if (mNormalizedConstraints.add(simplified)) {
          final SimpleExpressionProxy negation = getNegatedLiteral(simplified);
          if (mNormalizedConstraints.contains(negation)) {
            setFalse();
            break;
          }
        }
      } else {
        // Now all constraints are normalised.
        final Iterator<SimpleExpressionProxy> iter =
          mNormalizedConstraints.iterator();
        while (iter.hasNext()) {
          final SimpleExpressionProxy constraint = iter.next();
          for (final SimplificationRule rule : mRewriteRules) {
            if (rule.match(constraint, this)) {
              if (rule.isMakingReplacement()) {
                iter.remove();
              }
              rule.execute(this);
              continue outer;
            }
          }
        }
        break;
      }
    }
  }


  //#########################################################################
  //# Result Retrieval
  /**
   * Returns whether the current set of constraints has been found to be
   * false.
   * @return <CODE>true</CODE> if it has been found that the given
   *         constraints cannot be simultaneously true in the current
   *         context. Otherwise <CODE>false</CODE> is returned, indicating
   *         the the constraints may be simultaneously true, or it is not
   *         known whether they are satisfiable.
   */
  public boolean isUnsatisfiable()
  {
    return mIsUnsatisfiable;
  }

  /**
   * <P>Returns a constraint list representing the current state of this
   * constraint propagator. The returned list of expressions describes
   * all constraints given as formulas as well as any inferred range
   * constraints, as concisely as possible.</P>
   * <P>This method calls {@link #getAllConstraints(boolean)} with the
   * <CODE>pretty</CODE> argument set to&nbsp;<CODE>true</CODE>.
   * @return List of constraints, or <CODE>null</CODE> if the current
   *         constraints have been found to be unsatisfiable.
   */
  public ConstraintList getAllConstraints()
    throws EvalException
  {
    return getAllConstraints(true);
  }

  /**
   * Returns a constraint list representing the current state of this
   * constraint propagator. The returned list of expressions describes
   * all constraints given as formulas as well as any inferred range
   * constraints, as concisely as possible.
   * @param  pretty   Whether enumeration constraints should be shortened
   *                  using inequalities. For example, if a
   *                  variable&nbsp;<CODE>x</CODE> can take all values from
   *                  a ten-valued enumeration except <CODE>b</CODE>
   *                  or&nbsp;<CODE>c</CODE>, this may be represented as
   *                  <CODE>x&nbsp;!=&nbsp;b&nbsp;&amp; x&nbsp;!=&nbsp;c</CODE>.
   * @return List of constraints, or <CODE>null</CODE> if the current
   *         constraints have been found to be unsatisfiable.
   */
  public ConstraintList getAllConstraints(final boolean pretty)
    throws EvalException
  {
    if (mIsUnsatisfiable) {
      return null;
    } else {
      final List<SimpleExpressionProxy> list =
        new ArrayList<SimpleExpressionProxy>(mNormalizedConstraints);
      mContext.addAllConstraints(list, pretty);
      addPrimedVariables(list);
      if (list.isEmpty()) {
        return ConstraintList.TRUE;
      }
      Collections.sort(list, mListComparator);
      return new ConstraintList(list);
    }
  }

  /**
   * Retrieves the constrained variable context.
   * This method returns a variable context that results from constraint
   * propagation. It may contain additional bindings or ranges that are
   * further constrained than in the original context.
   */
  public VariableContext getContext()
  {
    return mContext;
  }

  /**
   * Returns whether the given expression represents a literal value
   * (integer constant or enumeration member) in the current context of
   * this constraint propagator.
   */
  public boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    return mSimpleExpressionCompiler.isAtomicValue(expr, mContext);
  }

  /**
   * Returns whether the given expression represents a variable name
   * (primed or unprimed) in the current context of this constraint
   * propagator.
   */
  public boolean isVariable(final SimpleExpressionProxy expr)
  {
    if (expr instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) expr;
      return mContext.getVariableRange(ident) != null;
    } else {
      return false;
    }
  }

  /**
   * Returns the number of times the {@link #propagate()} method of this
   * constraint propagator has been called since its creation.
   */
  public int getNumberOfInvocations()
  {
    return mNumberOfInvocations;
  }


  //#########################################################################
  //# Restricted Access
  void removeConstraint(final SimpleExpressionProxy constraint)
  {
    mNormalizedConstraints.remove(constraint);
  }

  BinaryExpressionProxy recallBinding(final SimpleExpressionProxy varname)
  {
    final BinaryExpressionProxy eqn = mContext.recallBinding(varname);
    if (eqn != null) {
      addConstraint(eqn);
    }
    return eqn;
  }


  //#########################################################################
  //# Callbacks from Rules
  void processEquation(final SimpleExpressionProxy varname,
                       final SimpleExpressionProxy replacement)
    throws EvalException
  {
    if (mContext.addBinding(varname, replacement)) {
      reevaluate();
    }
  }

  void restrictRange(final SimpleExpressionProxy varname,
                     final CompiledRange range)
    throws EvalException
  {
    if (mContext.restrictRange(varname, range)) {
      reevaluate();
    }
  }

  void reevaluate()
    throws EvalException
  {
    final Iterator<SimpleExpressionProxy> iter =
      mNormalizedConstraints.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy constraint = iter.next();
      final SimpleExpressionProxy simp =
        mSimpleExpressionCompiler.simplify(constraint, mContext);
      if (simp != constraint) {
        iter.remove();
        addConstraint(simp);
      }
    }
  }

  void setFalse()
  {
    mIsUnsatisfiable = true;
    mUnprocessedConstraints.clear();
    mNormalizedConstraints.clear();
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

  CompiledRange estimateRange(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mSimpleExpressionCompiler.estimateRange(expr, mContext);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Adds back in primed variables.
   * This method checks for primed variables that have been removed
   * by the constraint propagator and adds terms x'==x' to the given list
   * of constraints for all variables x that were present in the original
   * input but which have been removed from the list.
   */
  private void addPrimedVariables(final List<SimpleExpressionProxy> list)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final ProxyAccessorSet<UnaryExpressionProxy> remainingPrimed =
      new ProxyAccessorHashSet<>(eq, mPrimedVariables.size());
    mPrimedVariableCollector.collectPrimedVariables(list, remainingPrimed);
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final BinaryOperator op = mOperatorTable.getEqualsOperator();
    for (final UnaryExpressionProxy var : mPrimedVariables) {
      if (!remainingPrimed.containsProxy(var)) {
        final UnaryExpressionProxy lhs =
          (UnaryExpressionProxy) cloner.getClone(var);
        final UnaryExpressionProxy rhs =
          (UnaryExpressionProxy) cloner.getClone(var);
        final BinaryExpressionProxy constraint =
          mFactory.createBinaryExpressionProxy(op, lhs, rhs);
        list.add(constraint);
      }
    }
  }


  //#########################################################################
  //# Logging
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Inner Class: ConstraintContext
  private class ConstraintContext implements VariableContext
  {
    //#######################################################################
    //# Constructors
    ConstraintContext(final ConstraintContext context)
    {
      mRootContext = context.mRootContext;
      final int size = 2 * mRootContext.getNumberOfVariables();
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mBindings = new ProxyAccessorHashMap<>(eq, size);
      for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,
                           AbstractBinding> entry :
             context.mBindings.entrySet()) {
        final ProxyAccessor<SimpleExpressionProxy> accessor = entry.getKey();
        final AbstractBinding binding = entry.getValue();
        final AbstractBinding clone;
        if (binding instanceof IntBinding) {
          final IntBinding intbinding = (IntBinding) binding;
          clone = new IntBinding(intbinding);
        } else if (binding instanceof EnumBinding) {
          final EnumBinding enumbinding = (EnumBinding) binding;
          clone = new EnumBinding(enumbinding);
        } else {
          throw new ClassCastException
            ("Unknown binding type " + binding.getClass().getName() + "!");
        }
        mBindings.put(accessor, clone);
      }
    }

    ConstraintContext(final VariableContext root)
    {
      mRootContext = root;
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final int size = 2 * root.getNumberOfVariables();
      mBindings = new ProxyAccessorHashMap<>(eq, size);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    @Override
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy ident)
    {
      final AbstractBinding binding = mBindings.getByProxy(ident);
      if (binding != null) {
        final SimpleExpressionProxy expr = binding.getBoundExpression();
        if (expr != null) {
          return expr;
        }
      }
      return mRootContext.getBoundExpression(ident);
    }

    @Override
    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mRootContext.isEnumAtom(ident);
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mRootContext.getModuleBindingContext();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
    @Override
    public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
    {
      final AbstractBinding binding = mBindings.getByProxy(varname);
      if (binding != null) {
        return binding.getContrainedRange();
      } else {
        return mRootContext.getVariableRange(varname);
      }
    }

    @Override
    public int getNumberOfVariables()
    {
      return mRootContext.getNumberOfVariables();
    }

    //#######################################################################
    //# Specific Access
    CompiledRange getOriginalRange(final SimpleExpressionProxy varname)
    {
      return mRootContext.getVariableRange(varname);
    }

    void collectPrimedVariables
      (final ProxyAccessorSet<UnaryExpressionProxy> collection)
    {
      for (final AbstractBinding binding : mBindings.values()) {
        final SimpleExpressionProxy expr = binding.getBoundExpression();
        if (expr != null) {
          mPrimedVariableCollector.collectPrimedVariables(expr, collection);
        }
      }
    }

    //#######################################################################
    //# Range Modifications
    void reset()
    {
      mBindings.clear();
    }

    boolean addBinding(final SimpleExpressionProxy varname,
                       final SimpleExpressionProxy expr)
      throws EvalException
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        mBindings.createAccessor(varname);
      AbstractBinding binding = mBindings.get(accessor);
      if (binding == null) {
        final CompiledRange current = mRootContext.getVariableRange(varname);
        final CompiledRange estimate = estimateRange(expr);
        final CompiledRange intersection = current.intersection(estimate);
        if (intersection.isEmpty()) {
          setFalse();
          return true;
        } else if (intersection instanceof CompiledIntRange) {
          final CompiledIntRange intrange = (CompiledIntRange) intersection;
          binding = new IntBinding(varname, intrange, expr);
        } else if (intersection instanceof CompiledEnumRange) {
          final CompiledEnumRange enumrange = (CompiledEnumRange) intersection;
          binding = new EnumBinding(varname, enumrange, expr);
        } else {
          throw new ClassCastException
            ("Unknown range type " + intersection.getClass().getName());
        }
        mBindings.put(accessor, binding);
        reevaluate();
        return true;
      } else if (binding.restrictRange(expr)) {
        reevaluate();
        return true;
      } else {
        return false;
      }
    }

    boolean restrictRange(final SimpleExpressionProxy varname,
                          final CompiledRange restriction)
      throws EvalException
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        mBindings.createAccessor(varname);
      AbstractBinding binding = mBindings.get(accessor);
      if (binding == null) {
        final CompiledRange current = mRootContext.getVariableRange(varname);
        assert current != null :
        "Attempting to restrict undefined range for " + varname + "!";
        final CompiledRange intersection = current.intersection(restriction);
        if (current == intersection) {
          return false;
        } else if (intersection.isEmpty()) {
          setFalse();
          return true;
        } else if (intersection instanceof CompiledIntRange) {
          final CompiledIntRange intrange = (CompiledIntRange) intersection;
          binding = new IntBinding(varname, intrange);
        } else if (intersection instanceof CompiledEnumRange) {
          final CompiledEnumRange enumrange = (CompiledEnumRange) intersection;
          binding = new EnumBinding(varname, enumrange);
        } else {
          throw new ClassCastException
            ("Unknown range type " + intersection.getClass().getName());
        }
        mBindings.put(accessor, binding);
        reevaluate();
        return true;
      } else if (binding.restrictRange(restriction)) {
        reevaluate();
        return true;
      } else {
        return false;
      }
    }

    void reevaluate()
      throws EvalException
    {
      AbstractBinding changed = null;
      do {
        for (final AbstractBinding binding : mBindings.values()) {
          if (binding.reevaluate()) {
            if (mIsUnsatisfiable) {
              return;
            } else {
              changed = binding;
            }
          } else if (binding == changed) {
            return;
          }
        }
      } while (changed != null);
    }

    void removeBinding(final SimpleExpressionProxy varname)
    {
      mBindings.removeProxy(varname);
    }

    BinaryExpressionProxy recallBinding(final SimpleExpressionProxy varname)
    {
      final AbstractBinding binding = mBindings.getByProxy(varname);
      if (binding == null) {
        return null;
      } else {
        return binding.recall();
      }
    }

    //#######################################################################
    //# Constraint Retrieval
    void addAllConstraints(final Collection<SimpleExpressionProxy> result,
                           final boolean pretty)
      throws EvalException
    {
      for (final AbstractBinding binding : mBindings.values()) {
        binding.addAllConstraints(result, pretty);
      }
    }

    //#######################################################################
    //# Data Members
    private final VariableContext mRootContext;
    private final ProxyAccessorMap<SimpleExpressionProxy,AbstractBinding>
      mBindings;

  }


  //#########################################################################
  //# Inner Class: AbstractBinding
  private abstract class AbstractBinding
  {
    //#######################################################################
    //# Constructors
    AbstractBinding(final SimpleExpressionProxy varname,
                    final CompiledRange range,
                    final SimpleExpressionProxy expr)
    {
      mVariableName = varname;
      setBoundExpression(expr);
      setConstrainedRange(range);
    }

    AbstractBinding(final AbstractBinding binding)
    {
      mVariableName = binding.mVariableName;
      mIsAtomic = binding.mIsAtomic;
      mBoundExpression = binding.mBoundExpression;
      mConstrainedRange = binding.mConstrainedRange;
    }

    //#######################################################################
    //# Overrides for Base Class java.lang.Object
    @Override
    public String toString()
    {
      final StringBuilder buffer = new StringBuilder();
      buffer.append(mVariableName.toString());
      buffer.append("=");
      if (mBoundExpression == null) {
        buffer.append("?");
      } else {
        buffer.append(mBoundExpression.toString());
      }
      buffer.append(" : ");
      buffer.append(mConstrainedRange.toString());
      return buffer.toString();
    }

    //#######################################################################
    //# Simple Access
    SimpleExpressionProxy getVariableName()
    {
      return mVariableName;
    }

    boolean isAtomic()
    {
      return mIsAtomic;
    }

    SimpleExpressionProxy getBoundExpression()
    {
      return mBoundExpression;
    }

    CompiledRange getContrainedRange()
    {
      return mConstrainedRange;
    }

    void setBoundExpression(final SimpleExpressionProxy expr)
    {
      if (expr == null) {
        mIsAtomic = false;
      } else {
        mIsAtomic = mSimpleExpressionCompiler.isAtomicValue(expr, mContext);
      }
      mBoundExpression = expr;
    }

    /**
     * Assigns a new range to this binding.
     * If the new range is empty, the constraint propagator is set to the
     * unsatisfiable or 'false' state.
     * If the new range contains only one element, that element is assigned
     * as the new bound expression. This may produce new constraints the
     * old bound expression contains variables.
     * @return <CODE>true</CODE> if the constraint propagator state has
     *         been changed by this call.
     */
    boolean setConstrainedRange(final CompiledRange range)
    {
      mConstrainedRange = range;
      switch (range.size()) {
      case 0:
        setFalse();
        return true;
      case 1:
        if (!mIsAtomic) {
          final SimpleExpressionProxy value =
            range.getValues().iterator().next();
          if (mBoundExpression != null) {
            final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
            final BinaryExpressionProxy constraint =
              mFactory.createBinaryExpressionProxy(eqop,
                                                   value, mBoundExpression);
            mUnprocessedConstraints.add(constraint);
          }
          mBoundExpression = value;
          mIsAtomic = true;
        }
        return true;
      default:
        return false;
      }
    }

    CompiledRange getOriginalRange()
    {
      return mContext.getOriginalRange(mVariableName);
    }

    //#######################################################################
    //# Modifications
    boolean restrictRange(final CompiledRange restriction)
    {
      final CompiledRange intersection =
        mConstrainedRange.intersection(restriction);
      if (mConstrainedRange == intersection) {
        return false;
      } else {
        setConstrainedRange(intersection);
        return true;
      }
    }

    boolean restrictRange(final SimpleExpressionProxy expr)
      throws EvalException
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      if (eq.equals(expr, mBoundExpression)) {
        return false;
      } else {
        mBoundExpression = expr;
        final CompiledRange range = estimateRange(expr);
        restrictRange(range);
        return true;
      }
    }

    boolean reevaluate()
      throws EvalException
    {
      if (mBoundExpression == null) {
        return false;
      }
      final SimpleExpressionProxy simp =
        mSimpleExpressionCompiler.simplify(mBoundExpression, mContext);
      if (mBoundExpression == simp) {
        final CompiledRange range = estimateRange(mBoundExpression);
        return restrictRange(range);
      } else {
        mBoundExpression = simp;
        final CompiledRange range = estimateRange(simp);
        restrictRange(range);
        return true;
      }
    }

    BinaryExpressionProxy recall()
    {
      if (mBoundExpression == null) {
        return null;
      } else {
        final BinaryOperator op = mOperatorTable.getEqualsOperator();
        final BinaryExpressionProxy eqn = mFactory.createBinaryExpressionProxy
          (op, mVariableName, mBoundExpression);
        mBoundExpression = null;
        return eqn;
      }
    }

    //#######################################################################
    //# Constraint Retrieval
    void addAllConstraints(final Collection<SimpleExpressionProxy> result,
                           final boolean pretty)
      throws EvalException
    {
      if (mBoundExpression == null) {
        final CompiledRange orig = getOriginalRange();
        addRangeConstraints(result, orig, pretty);
      } else {
        addEquationConstraint(result);
        if (!mIsAtomic) {
          final CompiledRange estimate = estimateRange(mBoundExpression);
          final CompiledRange orig = getOriginalRange();
          final CompiledRange intersection = orig.intersection(estimate);
          addRangeConstraints(result, intersection, pretty);
        }
      }
    }

    void addEquationConstraint(final Collection<SimpleExpressionProxy> result)
    {
      final BinaryOperator op = mOperatorTable.getEqualsOperator();
      final BinaryExpressionProxy eqn = mFactory.createBinaryExpressionProxy
        (op, mVariableName, mBoundExpression);
      result.add(eqn);
    }

    abstract void addRangeConstraints(Collection<SimpleExpressionProxy> result,
                                      CompiledRange orig,
                                      boolean pretty);

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mVariableName;
    private boolean mIsAtomic;
    private SimpleExpressionProxy mBoundExpression;
    private CompiledRange mConstrainedRange;
  }


  //#########################################################################
  //# Inner Class: IntBinding
  private class IntBinding extends AbstractBinding
  {
    //#######################################################################
    //# Constructors
    IntBinding(final SimpleExpressionProxy varname,
               final CompiledIntRange range)
    {
      super(varname, range, null);
    }

    IntBinding(final SimpleExpressionProxy varname,
               final CompiledIntRange range,
               final SimpleExpressionProxy expr)
    {
      super(varname, range, expr);
    }

    IntBinding(final IntBinding binding)
    {
      super(binding);
    }

    //#######################################################################
    //# Simple Access
    CompiledIntRange getIntRange()
    {
      return (CompiledIntRange) getContrainedRange();
    }

    CompiledIntRange getOriginalIntRange()
    {
      return (CompiledIntRange) getOriginalRange();
    }

    //#######################################################################
    //# Constraint Retrieval
    @Override
    void addEquationConstraint(final Collection<SimpleExpressionProxy> result)
    {
      if (isAtomic()) {
        final CompiledIntRange range = getOriginalIntRange();
        if (range.isBooleanRange()) {
          final SimpleExpressionProxy varname = getVariableName();
          final IntConstantProxy intconst =
            (IntConstantProxy) getBoundExpression();
          switch (intconst.getValue()) {
          case 0:
            final UnaryOperator op = mOperatorTable.getNotOperator();
            final UnaryExpressionProxy expr =
              mFactory.createUnaryExpressionProxy(op, varname);
            result.add(expr);
            return;
          case 1:
            result.add(varname);
            return;
          default:
            throw new IllegalStateException
              ("Constant value " + intconst + " unexpected in Boolean range!");
          }
        }
      }
      super.addEquationConstraint(result);
    }

    @Override
    void addRangeConstraints(final Collection<SimpleExpressionProxy> result,
                             final CompiledRange orig,
                             final boolean pretty)
    {
      final BinaryOperator op = mOperatorTable.getLessEqualsOperator();
      final SimpleExpressionProxy varname = getVariableName();
      final CompiledIntRange intrange = getIntRange();
      final CompiledIntRange intorig = (CompiledIntRange) orig;
      final int lower = intrange.getLower();
      final int upper = intrange.getUpper();
      if (lower > intorig.getLower()) {
        final IntConstantProxy intconst =
          mFactory.createIntConstantProxy(lower);
        final BinaryExpressionProxy constraint =
          mFactory.createBinaryExpressionProxy(op, intconst, varname);
        result.add(constraint);
      }
      if (upper < intorig.getUpper()) {
        final IntConstantProxy intconst =
          mFactory.createIntConstantProxy(upper);
        final BinaryExpressionProxy constraint =
          mFactory.createBinaryExpressionProxy(op, varname, intconst);
        result.add(constraint);
      }
    }
  }


  //#########################################################################
  //# Inner Class: EnumBinding
  private class EnumBinding extends AbstractBinding
  {
    //#######################################################################
    //# Constructors
    EnumBinding(final SimpleExpressionProxy varname,
                final CompiledEnumRange range)
    {
      super(varname, range, null);
    }

    EnumBinding(final SimpleExpressionProxy varname,
                final CompiledEnumRange range,
                final SimpleExpressionProxy expr)
    {
      super(varname, range, expr);
    }

    EnumBinding(final EnumBinding binding)
    {
      super(binding);
    }

    //#######################################################################
    //# Simple Access
    CompiledEnumRange getEnumRange()
    {
      return (CompiledEnumRange) getContrainedRange();
    }

    //#######################################################################
    //# Constraint Retrieval
    @Override
    void addRangeConstraints(final Collection<SimpleExpressionProxy> result,
                             final CompiledRange orig,
                             final boolean pretty)
    {
      final SimpleExpressionProxy varname = getVariableName();
      final CompiledEnumRange enumrange = getEnumRange();
      final CompiledEnumRange enumorig = (CompiledEnumRange) orig;
      if (pretty && enumrange.size() < enumorig.size() / 2) {
        final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
        final BinaryOperator orop = mOperatorTable.getOrOperator();
        final Iterator<? extends SimpleExpressionProxy> iter =
          enumrange.getValues().iterator();
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
        result.add(constraint);
      } else {
        final BinaryOperator neqop = mOperatorTable.getNotEqualsOperator();
        for (final SimpleExpressionProxy value : enumorig.getValues()) {
          if (!enumrange.contains(value)) {
            final SimpleExpressionProxy constraint =
              mFactory.createBinaryExpressionProxy(neqop, varname, value);
            result.add(constraint);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ConstraintContext mContext;
  private final Comparator<SimpleExpressionProxy> mListComparator;
  private final Comparator<SimpleExpressionProxy> mEquationComparator;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final PrimedVariableCollector mPrimedVariableCollector;
  private final RelationNormalizationRule mNormalizer;
  private final RelationNormalizationRule mNegator;
  private final SimplificationRule[] mNormalizationRules;
  private final SimplificationRule[] mRewriteRules;

  private final List<SimpleExpressionProxy> mUnprocessedConstraints;
  private final Collection<SimpleExpressionProxy> mNormalizedConstraints;
  private final ProxyAccessorSet<UnaryExpressionProxy> mPrimedVariables;
  private boolean mIsUnsatisfiable;
  private int mNumberOfInvocations;
}
