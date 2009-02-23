//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFATransitionRelationBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * An auxiliary component of the EFA compiler to build the variable
 * transition relations for EFA events.
 *
 * @author Robi Malik
 */

class EFATransitionRelationBuilder
{

  //#########################################################################
  //# Constructor
  EFATransitionRelationBuilder
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final EFAModuleContext context,
     final SimpleExpressionCompiler compiler)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mContext = context;
    mSimpleExpressionCompiler = compiler;
    mCollector = new EFAVariableCollector(optable, context);

    mUniqueTransitionRelations =
      new HashMap<EFAVariableTransitionRelation,
                  EFAVariableTransitionRelation>();
    mUniqueTransitionRelationParts =
      new HashMap<EFAVariableTransitionRelationPart,
                  EFAVariableTransitionRelationPart>();
    mCachedModifyingParts =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,
                  EFAVariableTransitionRelationPart>();
    mCachedNonModifyingParts =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,
                  EFAVariableTransitionRelationPart>();
  }


  //#########################################################################
  //# Invocation
  EFAVariableTransitionRelation buildTransitionRelation
    (final EFAEventDecl edecl, final ConstraintList constraints)
    throws EvalException
  {
    try {
      int numLiterals = constraints.size();
      mVariableRecords = new HashMap<EFAVariable,VariableRecord>(numLiterals);
      final Collection<SimpleExpressionProxy> literals =
        constraints.getConstraints();
      for (final SimpleExpressionProxy literal : literals) {
        collectRecord(edecl, literal);
      }
      int numRecords = mVariableRecords.size();
      final EFAVariableTransitionRelation result =
        new EFAVariableTransitionRelation(numRecords);
      for (final VariableRecord record : mVariableRecords.values()) {
        final EFAVariable var = record.getUnprimed();
        final EFAVariableTransitionRelationPart part =
          record.createTransitionRelationPart();
        final EFAVariableTransitionRelationPart unique = getUnique(part);
        result.setPart(var, unique);
        if (result.isEmpty()) {
          break;
        }
      }
      return getUnique(result);
    } finally {
      mVariableRecords = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private VariableRecord collectRecord(final EFAEventDecl edecl,
                                       final SimpleExpressionProxy literal)
  {
    final Collection<EFAVariable> unprimedSet = new HashSet<EFAVariable>(1);
    final Collection<EFAVariable> primedSet = new HashSet<EFAVariable>(1);
    mCollector.collectAllVariables(literal, unprimedSet, primedSet);
    assert unprimedSet.size() <= 1;
    assert primedSet.size() <= 1;
    EFAVariable unprimed;
    EFAVariable primed;
    if (primedSet.isEmpty()) {
      unprimed = unprimedSet.iterator().next();
      primed = null;
    } else if (unprimedSet.isEmpty()) {
      primed = primedSet.iterator().next();
      final UnaryExpressionProxy primedname =
        (UnaryExpressionProxy) primed.getVariableName();
      final SimpleExpressionProxy varname = primedname.getSubTerm();
      unprimed = mContext.getVariable(varname);
    } else {
      unprimed = unprimedSet.iterator().next();
      primed = primedSet.iterator().next();
      assert primed.isPartnerOf(unprimed);
    }
    VariableRecord record = mVariableRecords.get(unprimed);
    if (record == null) {
      if (edecl.isEventVariable(unprimed)) {
        if (primed == null) {
          final UnaryOperator nextop = mOperatorTable.getNextOperator();
          final SimpleIdentifierProxy varname =
            (SimpleIdentifierProxy) unprimed.getVariableName();
          final UnaryExpressionProxy nextvarname =
            mFactory.createUnaryExpressionProxy(nextop, varname);
          primed = mContext.getVariable(nextvarname);
        }
        record = new ModifyingVariableRecord(unprimed, primed);
      } else {
        assert primed == null;
        record = new NonModifyingVariableRecord(unprimed);
      }
      mVariableRecords.put(unprimed, record);
    } else {
      assert primed == record.getPrimed();
    }
    record.addLiteral(literal);
    return record;
  }

  private EFAVariableTransitionRelationPart getUnique
    (final EFAVariableTransitionRelationPart part)
  {
    final EFAVariableTransitionRelationPart unique =
      mUniqueTransitionRelationParts.get(part);
    if (unique == null) {
      mUniqueTransitionRelationParts.put(part, part);
      return part;
    } else {
      return unique;
    }
  }

  private EFAVariableTransitionRelation getUnique
    (final EFAVariableTransitionRelation rel)
  {
    final EFAVariableTransitionRelation unique =
      mUniqueTransitionRelations.get(rel);
    if (unique == null) {
      mUniqueTransitionRelations.put(rel, rel);
      return rel;
    } else {
      return unique;
    }
  }


  //#########################################################################
  //# Inner Class VariableRecord
  private abstract class VariableRecord {

    //#######################################################################
    //# Constructors
    VariableRecord(final EFAVariable unprimed, final EFAVariable primed)
    {
      mUnprimed = unprimed;
      mPrimed = primed;
      mEquation = null;
      mOtherLiterals = new LinkedList<SimpleExpressionProxy>();
    }

    //#######################################################################
    //# Simple Access
    EFAVariable getUnprimed()
    {
      return mUnprimed;
    }

    EFAVariable getPrimed()
    {
      return mPrimed;
    }

    BinaryExpressionProxy getEquation()
    {
      return mEquation;
    }

    void addLiteral(final SimpleExpressionProxy literal)
    {
      if (mEquation == null && literal instanceof BinaryExpressionProxy) {
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) literal;
        final BinaryOperator op = binary.getOperator();
        if (op == mOperatorTable.getEqualsOperator()) {
          final OccursChecker checker = OccursChecker.getInstance();
          final SimpleExpressionProxy lhs = binary.getLeft();
          final SimpleExpressionProxy rhs = binary.getRight();
          if (mPrimed != null) {
            final SimpleExpressionProxy primed = mPrimed.getVariableName();
            if (primed.equalsByContents(lhs) && !checker.occurs(lhs, rhs)) {
              mEquation = binary;
              return;
            } else if (primed.equalsByContents(rhs) &&
                       !checker.occurs(rhs, lhs)) {
              mEquation = mFactory.createBinaryExpressionProxy(op, rhs, lhs);
              return;
            }
          }
          final SimpleExpressionProxy unprimed = mUnprimed.getVariableName();
          if (unprimed.equalsByContents(lhs) && !checker.occurs(lhs, rhs)) {
            mEquation = binary;
            return;
          } else if (unprimed.equalsByContents(rhs) &&
                     !checker.occurs(rhs, lhs)) {
            mEquation = mFactory.createBinaryExpressionProxy(op, rhs, lhs);
            return;
          }
        }
      }        
      mOtherLiterals.add(literal);
    }
    
    CompiledRange getRange()
    {
      return mUnprimed.getRange();
    }
    
    boolean evalOtherLiterals(final BindingContext context)
      throws EvalException
    {
      for (final SimpleExpressionProxy literal : mOtherLiterals) {
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(literal, context);
        if (!mSimpleExpressionCompiler.getBooleanValue(value)) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Overrides
    abstract EFAVariableTransitionRelationPart getCachedPart
      (ProxyAccessor<SimpleExpressionProxy> accessor);
    abstract void setCachedPart
      (ProxyAccessor<SimpleExpressionProxy> accessor,
       EFAVariableTransitionRelationPart part);
    abstract EFAVariableTransitionRelationPart createTransitionRelationPart()
      throws EvalException;

    //#######################################################################
    //# Data Members
    private EFAVariable mUnprimed;
    private EFAVariable mPrimed;
    private BinaryExpressionProxy mEquation;
    private Collection<SimpleExpressionProxy> mOtherLiterals;

  }


  //#########################################################################
  //# Inner Class NonModifyingVariableRecord
  private class NonModifyingVariableRecord extends VariableRecord {

    //#######################################################################
    //# Constructors
    NonModifyingVariableRecord(final EFAVariable unprimed)
    {
      super(unprimed, null);
    }

    //#######################################################################
    //# Overrides
    EFAVariableTransitionRelationPart getCachedPart
      (final ProxyAccessor<SimpleExpressionProxy> accessor)
    {
      return mCachedNonModifyingParts.get(accessor);
    }

    void setCachedPart
      (final ProxyAccessor<SimpleExpressionProxy> accessor,
       final EFAVariableTransitionRelationPart part)
    {
      mCachedNonModifyingParts.put(accessor, part);
    }
   
    EFAVariableTransitionRelationPart createTransitionRelationPart()
      throws EvalException
    {
      final CompiledRange range = getRange();
      final BinaryExpressionProxy eqn = getEquation();
      final List<? extends SimpleExpressionProxy> values;
      if (eqn == null) {
        values = range.getValues();
      } else {
        final SimpleExpressionProxy rhs = eqn.getRight();
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(rhs, mContext);
        if (range.contains(value)) {
          values = Collections.singletonList(value);
        } else {
          values = Collections.emptyList();
        }
      }
      final SimpleExpressionProxy varname = getUnprimed().getVariableName();
      final int size = values.size();
      final EFAVariableTransitionRelationPart result =
        new EFAVariableTransitionRelationPart(size);
      for (SimpleExpressionProxy value : values) {
        final BindingContext context =
          new SingleBindingContext(varname, value, mContext);
        if (evalOtherLiterals(context)) {
          result.addTransition(value, value);
        }
      }
      return result;
    }

  }


  //#########################################################################
  //# Inner Class ModifyingVariableRecord
  private class ModifyingVariableRecord extends VariableRecord {

    //#######################################################################
    //# Constructors
    ModifyingVariableRecord(final EFAVariable unprimed,
                            final EFAVariable primed)
    {
      super(unprimed, primed);
    }

    //#######################################################################
    //# Overrides
    EFAVariableTransitionRelationPart getCachedPart
      (final ProxyAccessor<SimpleExpressionProxy> accessor)
    {
      return mCachedModifyingParts.get(accessor);
    }

    void setCachedPart
      (final ProxyAccessor<SimpleExpressionProxy> accessor,
       final EFAVariableTransitionRelationPart part)
    {
      mCachedModifyingParts.put(accessor, part);
    }
    
    EFAVariableTransitionRelationPart createTransitionRelationPart()
    {
      return null;
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final EFAModuleContext mContext;
  private final EFAVariableCollector mCollector;

  private final
    Map<EFAVariableTransitionRelation,EFAVariableTransitionRelation>
    mUniqueTransitionRelations;
  private final
    Map<EFAVariableTransitionRelationPart,EFAVariableTransitionRelationPart>
    mUniqueTransitionRelationParts;
  private final
    Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariableTransitionRelationPart>
    mCachedNonModifyingParts;
  private final
    Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariableTransitionRelationPart>
    mCachedModifyingParts;

  private Map<EFAVariable,VariableRecord> mVariableRecords;

}
