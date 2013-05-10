//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMVariablePartitionComputer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMVariablePartitionComputer
{
  //#########################################################################
  //# Constructors
  public EFSMVariablePartitionComputer(final ModuleProxyFactory factory,
                                       final CompilerOperatorTable op)
  {
    mFactory = factory;
    mCompilerOperatorTable = op;
    mEFSMVariableFinder = new EFSMVariableFinder(op);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);

  }

  public List<int[]> computePartition(final EFSMVariable var, final EFSMSystem system)
    throws EvalException, AnalysisException
  {
    System.err.println("Computing partition for " + var.getName() + " (" +
                       var.getRange().size() + " states) ...");
    mEFSMVariable = var;
    mEFSMVariableCollector =
      new EFSMVariableCollector(mCompilerOperatorTable,
                                system.getVariableContext());
    mUnfoldingVariableContext =
      new UnfoldingVariableContext(mCompilerOperatorTable,
                                   system.getVariableContext(), var);
    mConstraintPropagator = new ConstraintPropagator(mFactory,
                                                     mCompilerOperatorTable,
                                                     mUnfoldingVariableContext);
    mRangeValues = var.getRange().getValues();
    final SimpleExpressionProxy efsmVariableName = var.getVariableName();
    final SimpleExpressionProxy efsmVariableNamePrimed = var.getPrimedVariableName();

    // 1. Check whether updates are feasible, and collect them ...
    // An update is feasible if it can be written as A & B,
    // where A contains only x (unfolded variable) and
    // B does not contain x.
    mRelevantUpdates = new ArrayList<ConstraintList>();
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
    if (!checkEventEncoding(encoding)) {
      return null;
    }
    final EFSMEventEncoding selfloops = system.getSelfloops();
    if (!checkEventEncoding(selfloops)) {
      return null;
    }

    // 2. Create a transition relation for the variable automaton ...
    final String name = var.getName();
    final int numEvents = mRelevantUpdates.size() + 1;
    final int numStates = var.getRange().size();
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(name, ComponentKind.PLANT,
                                       numEvents, 0, numStates, config);
    int event = EventEncoding.NONTAU;
    for (final ConstraintList update : mRelevantUpdates) {
      mEFSMVariableFinder.findVariable(update, mEFSMVariable);
      if (!mEFSMVariableFinder.containsVariable()) {
        // Unprimed variable not in update --- no need for any transitions
      } else if (!mEFSMVariableFinder.containsPrimedVariable()) {
        // Only unprimed variable in update --- refine initial partition
        mUnfoldingVariableContext.setCurrentValue(null);
        mConstraintPropagator.init(update);
        mConstraintPropagator.propagate();
        assert !mConstraintPropagator.isUnsatisfiable();
        final VariableContext context = mConstraintPropagator.getContext();
        SimpleExpressionProxy beforeExpr =
          context.getBoundExpression(efsmVariableName);
        int beforeValue = -1;
        if (beforeExpr != null) {
          final CompiledRange range = mEFSMVariable.getRange();
          beforeValue = range.indexOf(beforeExpr);
        }
        if (beforeValue >= 0) {
          rel.addTransition(beforeValue, event, beforeValue);
        } else {
          for (beforeValue = 0; beforeValue < mRangeValues.size(); beforeValue++) {
            beforeExpr = mRangeValues.get(beforeValue);
            mUnfoldingVariableContext.setCurrentValue(beforeExpr);
            mConstraintPropagator.init(update);
            mConstraintPropagator.propagate();
            if (!mConstraintPropagator.isUnsatisfiable()) {
              rel.addTransition(beforeValue, event, beforeValue);
            }
          }
        }
      } else {
        // Both primed and unprimed variable in update --- create transitions
        int beforeValue = 0;
        for (final SimpleExpressionProxy beforeExpr : mRangeValues) {
          mUnfoldingVariableContext.setCurrentValue(beforeExpr);
          mUnfoldingVariableContext.setPrimedValue(null);
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          if (mConstraintPropagator.isUnsatisfiable()) {
            continue;
          }
          final VariableContext context = mConstraintPropagator.getContext();
          SimpleExpressionProxy afterExpr =
            context.getBoundExpression(efsmVariableNamePrimed);
          int afterValue = -1;
          if (afterExpr != null) {
            final CompiledRange range = mEFSMVariable.getRange();
            afterValue = range.indexOf(afterExpr);
          }
          if (afterValue >= 0) {
            rel.addTransition(beforeValue, event, afterValue);
          } else {
            for (afterValue = 0; afterValue < mRangeValues.size(); afterValue++) {
              afterExpr = mRangeValues.get(afterValue);
              mUnfoldingVariableContext.setPrimedValue(afterExpr);
              mConstraintPropagator.init(update);
              mConstraintPropagator.propagate();
              if (!mConstraintPropagator.isUnsatisfiable()) {
                rel.addTransition(beforeValue, event, afterValue);
              }
            }
          }
          beforeValue++;
        }
      }
      event++;
    }

    // 3. Calculate partition using bisimulation algorithm
    mBisimulator.setTransitionRelation(rel);
    if (mBisimulator.run()) {
      final List<int[]> partition = mBisimulator.getResultPartition();
      if (partition == null) {
        System.err.println("NULL result partition");
      } else {
        System.err.println("Result partition size " + partition.size());
      }
      return partition;
    } else {
      System.err.println("No result partition");
      return null;
    }
  }

  private boolean checkEventEncoding(final EFSMEventEncoding encoding)
  {
    for(int i = EventEncoding.NONTAU; i < encoding.size(); i++) {
      final ConstraintList update = encoding.getUpdate(i);
      if (!checkUpdate(update)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkUpdate(final ConstraintList update)
  {
    final Set<EFSMVariable> variables = new THashSet<EFSMVariable>();
    final List<SimpleExpressionProxy> constraints = update.getConstraints();
    final List<SimpleExpressionProxy> relevantConstraints =
      new ArrayList<SimpleExpressionProxy>(constraints.size());
    for (final SimpleExpressionProxy expr : constraints) {
      variables.clear();
      mEFSMVariableCollector.collectAllVariables(expr, variables);
      if (variables.contains(mEFSMVariable)){
        if (variables.size() > 1) {
          System.err.println("Failed update: " + update.toString());
          return false;
        } else {
          relevantConstraints.add(expr);
        }
      }
    }
    if (!relevantConstraints.isEmpty()) {
      final ConstraintList relevantUpdate = new ConstraintList(relevantConstraints);
      mRelevantUpdates.add(relevantUpdate);
    }
    return true;
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mCompilerOperatorTable;
  private EFSMVariableCollector mEFSMVariableCollector;
  private final EFSMVariableFinder mEFSMVariableFinder;
  private UnfoldingVariableContext mUnfoldingVariableContext;
  private ConstraintPropagator mConstraintPropagator;
  private final ObservationEquivalenceTRSimplifier mBisimulator;

  private EFSMVariable mEFSMVariable;
  private List<ConstraintList> mRelevantUpdates;
  private List<? extends SimpleExpressionProxy> mRangeValues;

}
