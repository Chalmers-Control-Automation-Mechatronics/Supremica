//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFATransitionRelationBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
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
    mSubsumptionCache =
      new HashMap<SubsumptionTestPair,SubsumptionResult>(256);
  }


  //#########################################################################
  //# Building Events
  void initEventRecords()
  {
    mEventRecords =
      new IdentityHashMap<EFAVariableTransitionRelation,EventRecord>();
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
      final EventRecord record = new EventRecord(rel, locations);
      final List<EFAVariableTransitionRelation> victims =
        new LinkedList<EFAVariableTransitionRelation>();
      final List<EventRecord> open = new LinkedList<EventRecord>();
      open.add(record);
      while (!open.isEmpty()) {
        EventRecord record1 = open.remove(0);
        final EFAVariableTransitionRelation rel1 =
          record1.getTransitionRelation();
        final Collection<Proxy> locations1 = record1.getSourceLocations();
        loop:
        for (final Map.Entry<EFAVariableTransitionRelation,EventRecord> entry :
               mEventRecords.entrySet()) {
          final EFAVariableTransitionRelation rel2 = entry.getKey();
          final EventRecord record2 = entry.getValue();
          final SubsumptionResult subsumption = subsumptionTest(rel1, rel2);
          switch (subsumption.getKind()) {
          case SUBSUMES:
            victims.add(rel2);
            final Collection<Proxy> locations2 = record2.getSourceLocations();
            record1.addSourceLocations(locations2);
            final EFAVariableTransitionRelation rel3 =
              subsumption.getTransitionRelation();
            final EventRecord record3 = new EventRecord(rel3, locations2);
            open.add(record3);
            break;
          case SUBSUMED_BY:
            record2.addSourceLocations(locations1);
            final EFAVariableTransitionRelation rel4 =
              subsumption.getTransitionRelation();
            final EventRecord record4 = new EventRecord(rel4, locations1);
            open.add(record4);
            record1 = null;
            break loop;
          default:
            break;
          }
        }
        for (final EFAVariableTransitionRelation victim : victims) {
          mEventRecords.remove(victim);
        }
        victims.clear();
        if (record1 != null) {
          mEventRecords.put(rel, record1);
        }
      }
    }
  }

  Collection<EventRecord> getEventRecords()
  {
    return mEventRecords.values();
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
      result.provideFormula(constraints);
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
    return result;
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
  static class EventRecord {

    //#######################################################################
    //# Constructor
    private EventRecord(final EFAVariableTransitionRelation rel,
                        final Collection<Proxy> locations)
    {
      mTransitionRelation = rel;
      mSourceLocations = new HashSet<Proxy>(locations);
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
    //# Constructor
    NonModifyingVariableRecord(final EFAVariable unprimed)
    {
      super(unprimed, null);
    }

    //#######################################################################
    //# Overrides
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
    //# Constructor
    ModifyingVariableRecord(final EFAVariable unprimed,
                            final EFAVariable primed)
    {
      super(unprimed, primed);
    }

    //#######################################################################
    //# Overrides
    EFAVariableTransitionRelationPart createTransitionRelationPart()
      throws EvalException
    {
      final List<? extends SimpleExpressionProxy> values =
        getRange().getValues();
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
        final boolean forward = innervar.equalsByContents(primed);
        final SimpleExpressionProxy outervar = forward ? unprimed : primed;
        final SimpleExpressionProxy expr = eqn.getRight();
        for (final SimpleExpressionProxy outervalue : values) {
          final BindingContext outercontext =
            new SingleBindingContext(outervar, outervalue, mContext);
          final SimpleExpressionProxy innervalue =
            mSimpleExpressionCompiler.eval(expr, outercontext);
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
      return result;
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
    public boolean equals(final Object other)
    {
      if (other != null && getClass() == other.getClass()) {
        final SubsumptionTestPair pair = (SubsumptionTestPair) other;
        return mRelation1 == pair.mRelation1 && mRelation2 == pair.mRelation2;
      } else {
        return false;
      }
    }

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
