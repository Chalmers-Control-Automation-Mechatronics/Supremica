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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.
  CompilerExpressionComparator;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
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
    mContext = new ConstraintContext(root);
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
    mNormalizedConstraints = new TreeSet<SimpleExpressionProxy>(mComparator);
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
    mContext.reset();
    mNormalizedConstraints.clear();
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

  public List<SimpleExpressionProxy> getAllConstraints()
    throws EvalException
  {
    if (mIsFalse) {
      return null;
    } else {
      final List<SimpleExpressionProxy> result =
        new ArrayList<SimpleExpressionProxy>(mNormalizedConstraints);
      mContext.addAllConstraints(result);
      Collections.sort(result, mComparator);
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
        if (mNormalizedConstraints.contains(negation)) {
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
    mIsFalse = true;
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
  //# Inner Class ConstraintContext
  private class ConstraintContext implements VariableContext
  {

    //#######################################################################
    //# Constructor
    ConstraintContext(final VariableContext root)
    {
      final int size = root.getVariableNames().size();
      mRootContext = root;
      mBindings = new HashMap<ProxyAccessor<SimpleExpressionProxy>,
                              AbstractBinding>(size);
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy ident)
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(ident);
      final AbstractBinding binding = mBindings.get(accessor);
      if (binding != null) {
        final SimpleExpressionProxy expr = binding.getBoundExpression();
        if (expr != null) {
          return expr;
        }
      }
      return mRootContext.getBoundExpression(ident);
    }

    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mRootContext.isEnumAtom(ident);
    }

    public ModuleBindingContext getModuleBindingContext()
    {
      return mRootContext.getModuleBindingContext();
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
    public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      return getVariableRange(accessor);
    }

    public CompiledRange getVariableRange
      (final ProxyAccessor<SimpleExpressionProxy> accessor)
    {
      final AbstractBinding binding = mBindings.get(accessor);
      if (binding != null) {
        return binding.getContrainedRange();
      } else {
        return mRootContext.getVariableRange(accessor);
      }
    }

    public Collection<SimpleExpressionProxy> getVariableNames()
    {
      return mRootContext.getVariableNames();
    }


    //#######################################################################
    //# Specific Access
    CompiledRange getOriginalRange(final SimpleExpressionProxy varname)
    {
      return mRootContext.getVariableRange(varname);
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
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      AbstractBinding binding = mBindings.get(accessor);
      if (binding == null) {
        final CompiledRange current = mRootContext.getVariableRange(accessor);
        final CompiledRange estimate = estimateRange(expr);
        final CompiledRange intersection = current.intersection(estimate);
        if (intersection.isEmpty()) {
          setFalse();
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
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      AbstractBinding binding = mBindings.get(accessor);
      if (binding == null) {
        final CompiledRange current = mRootContext.getVariableRange(accessor);
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
            if (mIsFalse) {
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


    //#######################################################################
    //# Constraint Retrieval
    void addAllConstraints(final Collection<SimpleExpressionProxy> result)
      throws EvalException
    {
      for (final AbstractBinding binding : mBindings.values()) {
        binding.addAllConstraints(result);
      }
    }


    //#######################################################################
    //# Data Members
    private final VariableContext mRootContext;
    private final Map<ProxyAccessor<SimpleExpressionProxy>,AbstractBinding>
      mBindings;

  }


  //#########################################################################
  //# Inner Class AbstractBinding
  private abstract class AbstractBinding
  {

    //#######################################################################
    //# Constructor
    AbstractBinding(final SimpleExpressionProxy varname,
                    final CompiledRange range,
                    final SimpleExpressionProxy expr)
    {
      mVariableName = varname;
      setBoundExpression(expr);
      setConstrainedRange(range);
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

    void setConstrainedRange(final CompiledRange range)
    {
      mConstrainedRange = range;
      switch (range.size()) {
      case 0:
        setFalse();
        break;
      case 1:
        mIsAtomic = true;
        mBoundExpression = range.getValues().iterator().next();
        break;
      default:
        break;
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
      if (expr.equalsByContents(mBoundExpression)) {
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


    //#######################################################################
    //# Constraint Retrieval
    void addAllConstraints(final Collection<SimpleExpressionProxy> result)
      throws EvalException
    {
      if (mBoundExpression == null) {
        final CompiledRange orig = getOriginalRange();
        addRangeConstraints(result, orig);
      } else {
        addEquationConstraint(result);
        if (!mIsAtomic) {
          final CompiledRange estimate = estimateRange(mBoundExpression);
          final CompiledRange orig = getOriginalRange();
          final CompiledRange intersection = orig.intersection(estimate);
          addRangeConstraints(result, intersection);
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
                                      CompiledRange orig);

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mVariableName;
    private boolean mIsAtomic;
    private SimpleExpressionProxy mBoundExpression;
    private CompiledRange mConstrainedRange;

  }


  //#########################################################################
  //# Inner Class IntBinding
  private class IntBinding extends AbstractBinding
  {

    //#######################################################################
    //# Constructor
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
    void addEquationConstraint(final Collection<SimpleExpressionProxy> result)
    {
      if (isAtomic()) {
        final CompiledIntRange range = getIntRange();
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

    void addRangeConstraints(final Collection<SimpleExpressionProxy> result,
                             final CompiledRange orig)
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
  //# Inner Class EnumBinding
  private class EnumBinding extends AbstractBinding
  {

    //#######################################################################
    //# Constructor
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

    //#######################################################################
    //# Simple Access
    CompiledEnumRange getEnumRange()
    {
      return (CompiledEnumRange) getContrainedRange();
    }

    CompiledEnumRange getOriginalEnumRange()
    {
      return (CompiledEnumRange) getOriginalRange();
    }

    //#######################################################################
    //# Constraint Retrieval
    void addRangeConstraints(final Collection<SimpleExpressionProxy> result,
                             final CompiledRange orig)
    {
      final SimpleExpressionProxy varname = getVariableName();
      final CompiledEnumRange enumrange = getEnumRange();
      final CompiledEnumRange enumorig = (CompiledEnumRange) orig;
      if (enumrange.size() < enumorig.size() / 2) {
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
            SimpleExpressionProxy constraint =
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
