//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * An auxiliary component of the EFA compiler to build the variable
 * transition relations for EFA events.
 *
 * @author Robi Malik
 */

class EFATransitionRelationBuilder extends AbortableCompiler
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
    mEquality = new ModuleEqualityVisitor(false);

    mUniqueTransitionRelations = new HashMap<>();
    mUniqueTransitionRelationParts = new HashMap<>();
    mSubsumptionCache = new HashMap<>(256);
  }


  //#########################################################################
  //# Building Events
  void initEventRecords()
  {
    mEventRecords = new IdentityHashMap<>();
  }

  void addEventRecord(final EFAEventDecl edecl,
                      final ConstraintList constraints,
                      final Collection<Proxy> locations)
    throws EvalException
  {
    final EFAVariableTransitionRelation rel =
      buildTransitionRelation(edecl, constraints);
    final EventRecord found = mEventRecords.get(rel);
    if (found != null) {
      found.addSourceLocations(locations);
    } else {
      final List<EFAVariableTransitionRelation> victims = new LinkedList<>();
      for (final Map.Entry<EFAVariableTransitionRelation,EventRecord> entry :
             mEventRecords.entrySet()) {
        checkAbort();
        final EFAVariableTransitionRelation rel2 = entry.getKey();
        final EventRecord record2 = entry.getValue();
        final Collection<Proxy> locations2 = record2.getSourceLocations();
        final SubsumptionResult subsumption = subsumptionTest(rel, rel2);
        switch (subsumption.getKind()) {
        case SUBSUMES:
          if (locations2.containsAll(locations)) {
            assert victims.isEmpty();
            return;
          }
          break;
        case SUBSUMED_BY:
          if (locations.containsAll(locations2)) {
            victims.add(rel2);
          }
          break;
        default:
          break;
        }
      }
      for (final EFAVariableTransitionRelation victim : victims) {
        mEventRecords.remove(victim);
      }
      final EventRecord record = new EventRecord(rel, locations);
      mEventRecords.put(rel, record);
    }
  }

  List<EventRecord> getSortedEventRecords()
  {
    final List<EventRecord> result =
      new ArrayList<EventRecord>(mEventRecords.values());
    Collections.sort(result);
    return result;
  }

  void clearEventRecords()
  {
    mEventRecords = null;
  }


  //#########################################################################
  //# Building Transition Relations
  private EFAVariableTransitionRelation buildTransitionRelation
    (final EFAEventDecl edecl, final ConstraintList constraints)
    throws EvalException
  {
    try {
      final EFAVariableTransitionRelation result;
      if (constraints == null) {
        result = new EFAVariableTransitionRelation(true);
      } else {
        final int numLiterals = constraints.size();
        mVariableRecords =
          new HashMap<EFAVariable,VariableRecord>(numLiterals);
        final Collection<SimpleExpressionProxy> literals =
          constraints.getConstraints();
        for (final SimpleExpressionProxy literal : literals) {
          collectRecord(edecl, literal);
        }
        final int numRecords = mVariableRecords.size();
        result = new EFAVariableTransitionRelation(numRecords);
        result.provideFormula(constraints);
        for (final VariableRecord record : mVariableRecords.values()) {
          checkAbort();
          final EFAVariableTransitionRelationPart part =
            record.createTransitionRelationPart();
          if (part != null) {
            final EFAVariableTransitionRelationPart unique = getUnique(part);
            final EFAVariable var = record.getUnprimed();
            result.addPart(var, unique);
            if (result.isEmpty()) {
              break;
            }
          }
        }
        for (final EFAVariable var : edecl.getVariables()) {
          if (!mVariableRecords.containsKey(var)) {
            checkAbort();
            final EFAVariable primedvar = getPrimedVariable(var);
            final VariableRecord record =
              new ModifyingVariableRecord(var, primedvar);
            final EFAVariableTransitionRelationPart part =
              record.createTransitionRelationPart();
            final EFAVariableTransitionRelationPart unique = getUnique(part);
            result.addPart(var, unique);
          }
        }
      }
      return getUnique(result);
    } finally {
      mVariableRecords = null;
    }
  }

  private SubsumptionResult subsumptionTest
    (final EFAVariableTransitionRelation rel1,
     final EFAVariableTransitionRelation rel2)
  {
    final SubsumptionTestPair pair = new SubsumptionTestPair(rel1, rel2);
    final SubsumptionResult cached = mSubsumptionCache.get(pair);
    if (cached != null) {
      return cached;
    }
    final SubsumptionTestPair revpair = new SubsumptionTestPair(rel2, rel1);
    final SubsumptionResult revcached = mSubsumptionCache.get(revpair);
    if (revcached != null) {
      return revcached.reverse();
    }
    final SubsumptionResult.Kind kind = rel1.subsumptionTest(rel2);
    final SubsumptionResult result = SubsumptionResult.create(kind);
    mSubsumptionCache.put(pair, result);
    /*
    EFAVariableTransitionRelation delta;
    switch (kind) {
    case SUBSUMES:
      delta = rel2.difference(rel1);
      break;
    case SUBSUMED_BY:
      delta = rel1.difference(rel2);
      break;
    default:
      return result;
    }
    delta = getUnique(delta);
    if (delta.getFormula() == null) {
      if (kind == SubsumptionResult.Kind.SUBSUMES) {
        final ConstraintList formula = buildDifferenceFormula(rel2, rel1);
        delta.provideFormula(formula);
      } else {
        final ConstraintList formula = buildDifferenceFormula(rel1, rel2);
        delta.provideFormula(formula);
      }
    }
    result.setTransitionRelation(delta);
    */
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  private VariableRecord collectRecord(final EFAEventDecl edecl,
                                       final SimpleExpressionProxy literal)
  {
    final Collection<EFAVariable> unprimedSet = new THashSet<EFAVariable>(1);
    final Collection<EFAVariable> primedSet = new THashSet<EFAVariable>(1);
    mCollector.collectAllVariables(literal, unprimedSet, primedSet);
    // Note: 'primed' is the unprimed version of the primed variable!
    assert unprimedSet.size() <= 1;
    assert primedSet.size() <= 1;
    final EFAVariable var;
    if (primedSet.isEmpty()) {
      var = unprimedSet.iterator().next();
    } else {
      var = primedSet.iterator().next();
    }
    VariableRecord record = mVariableRecords.get(var);
    if (record == null) {
      if (edecl.isEventVariable(var)) {
        final EFAVariable primedvar = getPrimedVariable(var);
        record = new ModifyingVariableRecord(var, primedvar);
      } else {
        record = new NonModifyingVariableRecord(var);
      }
      mVariableRecords.put(var, record);
    }
    record.addLiteral(literal);
    return record;
  }

  @SuppressWarnings("unused")
  private ConstraintList buildDifferenceFormula
    (final EFAVariableTransitionRelation rel1,
     final EFAVariableTransitionRelation rel2)
  {
    final ConstraintList formula1 = rel1.getFormula();
    final ConstraintList formula2 = rel2.getFormula();
    final BinaryOperator andop = mOperatorTable.getAndOperator();
    SimpleExpressionProxy conjunction = null;
    for (final SimpleExpressionProxy literal : formula2.getConstraints()) {
      if (!formula1.contains(literal)) {
        if (conjunction == null) {
          conjunction = literal;
        } else {
          conjunction =
            mFactory.createBinaryExpressionProxy(andop, conjunction, literal);
        }
      }
    }
    final List<SimpleExpressionProxy> list =
      new ArrayList<SimpleExpressionProxy>(formula1.size() + 1);
    list.addAll(formula1.getConstraints());
    if (conjunction != null) {
      final UnaryOperator notop = mOperatorTable.getNotOperator();
      final UnaryExpressionProxy negexpr =
        mFactory.createUnaryExpressionProxy(notop, conjunction);
      list.add(negexpr);
    }
    return new ConstraintList(list);
  }

  private EFAVariable getPrimedVariable(final EFAVariable var)
  {
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    final IdentifierProxy varname = (IdentifierProxy) var.getVariableName();
    final UnaryExpressionProxy nextvarname =
      mFactory.createUnaryExpressionProxy(nextop, varname);
    return mContext.getVariable(nextvarname);
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
      unique.provideFormula(rel);
      return unique;
    }
  }


  //#########################################################################
  //# Inner Class EventRecord
  static class EventRecord implements Comparable<EventRecord> {

    //#######################################################################
    //# Constructor
    private EventRecord(final EFAVariableTransitionRelation rel,
                        final Collection<Proxy> locations)
    {
      mTransitionRelation = rel;
      mSourceLocations = new THashSet<Proxy>(locations);
    }

    //#######################################################################
    //# Simple Access
    EFAVariableTransitionRelation getTransitionRelation()
    {
      return mTransitionRelation;
    }

    Collection<Proxy> getSourceLocations()
    {
      return mSourceLocations;
    }

    void addSourceLocations(final Collection<Proxy> locations)
    {
      mSourceLocations.addAll(locations);
    }

    //#######################################################################
    //# Interface java.lang.Comparable
    @Override
    public int compareTo(final EventRecord record)
    {
      return mTransitionRelation.compareTo(record.mTransitionRelation);
    }

    //#######################################################################
    //# Data Members
    private final EFAVariableTransitionRelation mTransitionRelation;
    private final Collection<Proxy> mSourceLocations;

  }


  //#########################################################################
  //# Inner Class VariableRecord
  private abstract class VariableRecord {

    //#######################################################################
    //# Constructor
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
            if (mEquality.equals(primed, lhs) &&
                !checker.occurs(lhs, rhs)) {
              mEquation = binary;
              return;
            } else if (mEquality.equals(primed, rhs) &&
                       !checker.occurs(rhs, lhs)) {
              mEquation = mFactory.createBinaryExpressionProxy(op, rhs, lhs);
              return;
            }
          }
          final SimpleExpressionProxy unprimed = mUnprimed.getVariableName();
          if (mEquality.equals(unprimed, lhs) &&
              !checker.occurs(lhs, rhs)) {
            mEquation = binary;
            return;
          } else if (mEquality.equals(unprimed, rhs) &&
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
    abstract EFAVariableTransitionRelationPart createTransitionRelationPart()
      throws EvalException;

    //#######################################################################
    //# Data Members
    private final EFAVariable mUnprimed;
    private final EFAVariable mPrimed;
    private BinaryExpressionProxy mEquation;
    private final Collection<SimpleExpressionProxy> mOtherLiterals;

  }


  //#########################################################################
  //# Inner Class NonModifyingVariableRecord
  private class NonModifyingVariableRecord extends VariableRecord {

    //#######################################################################
    //# Constructor
    NonModifyingVariableRecord(final EFAVariable unprimed)
    {
      super(unprimed, null);
    }

    //#######################################################################
    //# Overrides
    @Override
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
      for (final SimpleExpressionProxy value : values) {
        final BindingContext context =
          new SingleBindingContext(varname, value, mContext);
        if (evalOtherLiterals(context)) {
          result.addTransition(value, value);
        }
      }
      if (result.size() == range.size()) {
        return null;
      } else {
        return result;
      }
    }

  }


  //#########################################################################
  //# Inner Class ModifyingVariableRecord
  private class ModifyingVariableRecord extends VariableRecord {

    //#######################################################################
    //# Constructor
    ModifyingVariableRecord(final EFAVariable unprimed,
                            final EFAVariable primed)
    {
      super(unprimed, primed);
    }

    //#######################################################################
    //# Overrides
    @Override
    EFAVariableTransitionRelationPart createTransitionRelationPart()
      throws EvalException
    {
      final CompiledRange range = getRange();
      final List<? extends SimpleExpressionProxy> values = range.getValues();
      final BinaryExpressionProxy eqn = getEquation();
      final SimpleExpressionProxy unprimed = getUnprimed().getVariableName();
      final SimpleExpressionProxy primed = getPrimed().getVariableName();
      final EFAVariableTransitionRelationPart result =
        new EFAVariableTransitionRelationPart();
      if (eqn == null) {
        for (final SimpleExpressionProxy curvalue : values) {
          final BindingContext curcontext =
            new SingleBindingContext(unprimed, curvalue, mContext);
          for (final SimpleExpressionProxy nextvalue : values) {
            final BindingContext nextcontext =
              new SingleBindingContext(primed, nextvalue, curcontext);
            if (evalOtherLiterals(nextcontext)) {
              result.addTransition(curvalue, nextvalue);
            }
          }
        }
      } else {
        final SimpleExpressionProxy innervar = eqn.getLeft();
        final boolean forward = mEquality.equals(innervar, primed);
        final SimpleExpressionProxy outervar = forward ? unprimed : primed;
        final SimpleExpressionProxy expr = eqn.getRight();
        for (final SimpleExpressionProxy outervalue : values) {
          final BindingContext outercontext =
            new SingleBindingContext(outervar, outervalue, mContext);
          final SimpleExpressionProxy innervalue =
            mSimpleExpressionCompiler.eval(expr, outercontext);
          if (range.contains(innervalue)) {
            final BindingContext innercontext =
              new SingleBindingContext(innervar, innervalue, outercontext);
            if (evalOtherLiterals(innercontext)) {
              if (forward) {
                result.addTransition(outervalue, innervalue);
              } else {
                result.addTransition(innervalue, outervalue);
              }
            }
          }
        }
      }
      if (result.isAllSelfloops() && result.size() == range.size()) {
        return null;
      } else {
        return result;
      }
    }

  }


  //#########################################################################
  //# Inner Class SubsumptionTestPair
  private static class SubsumptionTestPair
  {

    //#######################################################################
    //# Constructor
    private SubsumptionTestPair(final EFAVariableTransitionRelation rel1,
                                final EFAVariableTransitionRelation rel2)
    {
      mRelation1 = rel1;
      mRelation2 = rel2;
    }

    //#######################################################################
    //# Overrides for Base Class java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && getClass() == other.getClass()) {
        final SubsumptionTestPair pair = (SubsumptionTestPair) other;
        return mRelation1 == pair.mRelation1 && mRelation2 == pair.mRelation2;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return mRelation1.objectHashCode() + 5 * mRelation2.objectHashCode();
    }

    //#######################################################################
    //# Data Members
    private final EFAVariableTransitionRelation mRelation1;
    private final EFAVariableTransitionRelation mRelation2;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final EFAModuleContext mContext;
  private final EFAVariableCollector mCollector;
  private final ModuleEqualityVisitor mEquality;

  private final
    Map<EFAVariableTransitionRelation,EFAVariableTransitionRelation>
    mUniqueTransitionRelations;
  private final
    Map<EFAVariableTransitionRelationPart,EFAVariableTransitionRelationPart>
    mUniqueTransitionRelationParts;
  private final Map<SubsumptionTestPair,SubsumptionResult> mSubsumptionCache;

  private Map<EFAVariableTransitionRelation,EventRecord> mEventRecords;
  private Map<EFAVariable,VariableRecord> mVariableRecords;

}
