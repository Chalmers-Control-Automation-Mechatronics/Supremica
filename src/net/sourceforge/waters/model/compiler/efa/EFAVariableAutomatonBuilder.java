//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableAutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An auxiliary component of the EFA compiler to build the automata for a
 * variable. The EFA automaton builder converts a variable ({@link
 * VariableComponentProxy}) to a simple automaton ({@link
 * SimpleComponentProxy}).
 *
 * @author Robi Malik
 */

class EFAVariableAutomatonBuilder
{

  //#########################################################################
  //# Constructor
  EFAVariableAutomatonBuilder
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final Comparator<SimpleExpressionProxy> comparator,
     final SimpleExpressionCompiler compiler,
     final BindingContext context)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mComparator = comparator;
    mSimpleExpressionCompiler = compiler;
    mRootContext = context;
  }


  //#########################################################################
  //# Invocation
  SimpleComponentProxy constructSimpleComponent(final EFAVariable var)
    throws EvalException
  {
    try {
      mVariable = var;
      mBlockedEvents = new LinkedList<IdentifierProxy>();
      constructVariableRange();
      for (final EFAEvent event : var.getEFAEvents()) {
        mEvent = event;
        final EFAEventDecl edecl = event.getEFAEventDecl();
        final CompiledClause conditions = event.getConditions();
	constructEventTransitions(edecl, conditions);
      }
      constructEdges();
      final VariableComponentProxy comp =
        (VariableComponentProxy) mVariable.getComponent();
      final boolean deterministic = comp.isDeterministic();
      final LabelBlockProxy blocked =
	mFactory.createLabelBlockProxy(mBlockedEvents, null);
      final GraphProxy graph =
	mFactory.createGraphProxy(deterministic, blocked,
				  mNodeList, mEdgeList);
      final ModuleProxyCloner cloner = mFactory.getCloner();
      final IdentifierProxy ident = comp.getIdentifier();
      final IdentifierProxy iclone = (IdentifierProxy) cloner.getClone(ident);
      return mFactory.createSimpleComponentProxy
	(iclone, ComponentKind.PLANT, graph);
    } finally {
      mVariable = null;
      mBlockedEvents = null;
      mNodeList = null;
      mNodeMap = null;
      mTransitions = null;
      mEdgeList = null;
      mEvent = null;
    }
  }

  boolean isSatisfiable(final EFAEventDecl edecl,
                        final CompiledClause conditions)
    throws EvalException
  {
    for (final EFAVariable var : edecl.getVariables()) {
      if (!isSatisfiable(var, edecl, conditions)) {
        return false;
      }
    }
    return true;
  }

  boolean isSatisfiable(final EFAVariable var,
                        final EFAEventDecl edecl,
                        final CompiledClause conditions)
    throws EvalException
  {
    try {
      mVariable = var;
      mEvent = null;
      return constructEventTransitions(edecl, conditions);
    } finally {
      mVariable = null;
    }
  }


  //#########################################################################
  //# Data Members
  private void constructVariableRange()
    throws EvalException
  {
    final VariableComponentProxy comp =
      (VariableComponentProxy) mVariable.getComponent();
    final IdentifierProxy varname = comp.getIdentifier();
    final SimpleExpressionProxy initpred = comp.getInitialStatePredicate();
    final List<VariableMarkingProxy> markings = comp.getVariableMarkings();
    final int nummarkings = markings.size();
    final Set<IdentifierProxy> blocked =
      new HashSet<IdentifierProxy>(nummarkings);
    for (final VariableMarkingProxy marking : markings) {
      final IdentifierProxy prop = marking.getProposition();
      blocked.add(prop);
    }
    final List<IdentifierProxy> props =
      new ArrayList<IdentifierProxy>(nummarkings);
    final CompiledRange range = mVariable.getRange();
    final int rangesize = range.size();
    mNodeList = new ArrayList<SimpleNodeProxy>(rangesize);
    mNodeMap =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,SimpleNodeProxy>
      (rangesize);
    mTransitions = new TreeMap<EFAVariableTransition,EFAVariableTransition>();
    for (final SimpleExpressionProxy value : range.getValues()) {
      final String name = value.toString();
      final BindingContext context =
	new SingleBindingContext(varname, value, mRootContext);
      final SimpleExpressionProxy initval =
	mSimpleExpressionCompiler.eval(initpred, context);
      final boolean initial =
	mSimpleExpressionCompiler.getBooleanValue(initval);
      for (final VariableMarkingProxy marking : markings) {
	final SimpleExpressionProxy pred = marking.getPredicate();
	final SimpleExpressionProxy predval =
	  mSimpleExpressionCompiler.eval(pred, context);
	if (mSimpleExpressionCompiler.getBooleanValue(predval)) {
	  final IdentifierProxy prop = marking.getProposition();
	  props.add(prop);
	  blocked.remove(prop);
	}
      }
      final PlainEventListProxy elist =
	props.isEmpty() ? null : mFactory.createPlainEventListProxy(props);
      final SimpleNodeProxy node = mFactory.createSimpleNodeProxy
	(name, elist, initial, null, null, null);
      mNodeList.add(node);
      final ProxyAccessor<SimpleExpressionProxy> accessor =
	new ProxyAccessorByContents<SimpleExpressionProxy>(value);
      mNodeMap.put(accessor, node);
      props.clear();
    }
    if (!blocked.isEmpty()) {
      for (final VariableMarkingProxy marking : markings) {
	final IdentifierProxy prop = marking.getProposition();
	if (blocked.contains(prop)) {
	  mBlockedEvents.add(prop);
	}
      }
    }
  }

  private boolean constructEventTransitions(final EFAEventDecl edecl,
                                            final CompiledClause conditions)
    throws EvalException
  {
    final OccursChecker checker = OccursChecker.getInstance();
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    final IdentifierProxy varname = mVariable.getComponent().getIdentifier();
    final Collection<SimpleExpressionProxy> literals =
      conditions.getLiterals();
    final int numliterals = literals.size();
    final List<SimpleExpressionProxy> guards =
      new ArrayList<SimpleExpressionProxy>(numliterals);
    SimpleExpressionProxy curexpr = null;
    SimpleExpressionProxy nextexpr = null;
    for (final SimpleExpressionProxy cond : conditions.getLiterals()) {
      if (checker.occurs(varname, cond)) {
	if (cond instanceof BinaryExpressionProxy) {
	  final BinaryExpressionProxy binary = (BinaryExpressionProxy) cond;
	  final BinaryOperator op = binary.getOperator();
	  if (op == eqop) {
	    final SimpleExpressionProxy lhs = binary.getLeft();
	    final SimpleExpressionProxy rhs = binary.getRight();
	    if (isNextVarName(lhs, varname)) {
	      if (nextexpr == null && !checker.occurs(lhs, rhs)) {
		nextexpr = rhs;
		continue;
	      }
	    } else if (isNextVarName(rhs, varname)) {
	      if (nextexpr == null && !checker.occurs(rhs, lhs)) {
		nextexpr = lhs;
		continue;
	      }
	    } else if (lhs.equalsByContents(varname)) {
	      if (curexpr == null && !checker.occurs(varname, rhs)) {
		curexpr = rhs;
		continue;
	      }
	    } else if (rhs.equalsByContents(varname)) {
	      if (curexpr == null && !checker.occurs(varname, lhs)) {
		curexpr = lhs;
		continue;
	      }
	    }
	  }
	}
	guards.add(cond);
      }
    }

    final CompiledRange range = mVariable.getRange();
    final List<? extends SimpleExpressionProxy> values = range.getValues();
    final List<? extends SimpleExpressionProxy> curvalues;
    if (curexpr != null) {
      final SimpleExpressionProxy curvalue =
	mSimpleExpressionCompiler.eval(curexpr, mRootContext);
      if (range.contains(curvalue)) {
        curvalues = Collections.singletonList(curvalue);
      } else {
        curvalues = Collections.emptyList();
      }
    } else {
      curvalues = values;
    }
    boolean hastrans = false;
    int selfloops = 0;
    if (edecl.isEventVariable(mVariable)) {
      // In event alphabet --- transitions depend on current and next state.
      final UnaryOperator nextop = mOperatorTable.getNextOperator();
      final UnaryExpressionProxy nextvarname =
	mFactory.createUnaryExpressionProxy(nextop, varname);
      for (final SimpleExpressionProxy curvalue : curvalues) {
	final BindingContext curcontext =
	  new SingleBindingContext(varname, curvalue, mRootContext);
	final List<? extends SimpleExpressionProxy> nextvalues;
	if (nextexpr != null) {
	  final SimpleExpressionProxy nextvalue =
	    mSimpleExpressionCompiler.eval(nextexpr, curcontext);
          if (!range.contains(nextvalue)) {
            continue;
          }
	  nextvalues = Collections.singletonList(nextvalue);
	} else {
	  nextvalues = values;
	}
	for (final SimpleExpressionProxy nextvalue : nextvalues) {
	  final BindingContext nextcontext =
	    new SingleBindingContext(nextvarname, nextvalue, curcontext);
	  if (evalGuards(guards, nextcontext)) {
            if (mEvent == null) {
              return true;
            } else {
              createTransition(curvalue, nextvalue, mEvent);
              hastrans = true;
              if (selfloops >= 0) {
                if (curvalue.equalsByContents(nextvalue)) {
                  selfloops++;
                } else {
                  selfloops = -1;
                }
              }
            }
	  }
	}
      }
    } else {
      // Not in event alphabet --- transitions depend on current state only.
      for (final SimpleExpressionProxy curvalue : curvalues) {
	final BindingContext curcontext =
	  new SingleBindingContext(varname, curvalue, mRootContext);
	if (evalGuards(guards, curcontext)) {
          if (mEvent == null) {
            return true;
          } else {
            createTransition(curvalue, curvalue, mEvent);
            hastrans = true;
            selfloops++;
          }
	}
      }
    }

    if (mEvent == null) {
      // nothing
    } else if (!hastrans) {
      final IdentifierProxy ident = mEvent.createIdentifier(mFactory);
      mBlockedEvents.add(ident);
    } else if (selfloops == range.size()) {
      for (final SimpleExpressionProxy value : values) {
        removeLastSelfloop(value);
      }
    }
    return hastrans;
  }

  private void constructEdges()
  {
    final int numedges = mTransitions.size();
    mEdgeList = new ArrayList<EdgeProxy>(numedges);
    for (final EFAVariableTransition trans : mTransitions.keySet()) {
      final EdgeProxy edge = trans.createEdgeProxy();
      mEdgeList.add(edge);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleNodeProxy getNode(final SimpleExpressionProxy value)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(value);
    return mNodeMap.get(accessor);
  }

  private boolean isNextVarName(final SimpleExpressionProxy expr,
				final IdentifierProxy varname)
  {
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      return
	unary.getOperator() == mOperatorTable.getNextOperator() &&
	unary.getSubTerm().equalsByContents(varname);
    } else {
      return false;
    }
  }

  private boolean evalGuards(final List<SimpleExpressionProxy> guards,
			     final BindingContext context)
    throws EvalException
  {
    for (final SimpleExpressionProxy guard : guards) {
      final SimpleExpressionProxy gvalue =
	mSimpleExpressionCompiler.eval(guard, context);
      if (!mSimpleExpressionCompiler.getBooleanValue(gvalue)) {
	return false;
      }
    }
    return true;
  }

  private void createTransition(final SimpleExpressionProxy source,
				final SimpleExpressionProxy target,
				final EFAEvent event)
  {
    EFAVariableTransition trans = new EFAVariableTransition(source, target);
    final EFAVariableTransition found = mTransitions.get(trans);
    if (found == null) {
      mTransitions.put(trans, trans);
    } else {
      trans = found;
    }
    final IdentifierProxy ident = event.createIdentifier(mFactory);
    trans.addLabel(ident);
  }

  private void removeLastSelfloop(final SimpleExpressionProxy state)
  {
    final EFAVariableTransition trans =
      new EFAVariableTransition(state, state);
    final EFAVariableTransition found = mTransitions.get(trans);
    found.removeLastTransition();
  }



  //#########################################################################
  //# Inner Class EFAVariableTransition
  private class EFAVariableTransition
    implements Comparable<EFAVariableTransition>
  {

    //#######################################################################
    //# Constructor
    private EFAVariableTransition(final SimpleExpressionProxy source,
				  final SimpleExpressionProxy target)
    {
      mSourceValue = source;
      mTargetValue = target;
      mLabels = null;
    }

    //#######################################################################
    //# Hashing and Comparing
    public int compareTo(final EFAVariableTransition trans)
    {
      final int sourcecomp =
	mComparator.compare(mSourceValue, trans.mSourceValue);
      if (sourcecomp != 0) {
	return sourcecomp;
      } else {
	return mComparator.compare(mTargetValue, trans.mTargetValue);
      }
    }

    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
	final EFAVariableTransition trans = (EFAVariableTransition) other;
	return
	  mSourceValue.equalsByContents(trans.mSourceValue) &&
	  mTargetValue.equalsByContents(trans.mTargetValue);
      } else {
	return false;
      }
    }

    public int hashCode()
    {
      return
	mSourceValue.hashCodeByContents() +
	5 * mTargetValue.hashCodeByContents();
    }

    //#######################################################################
    //# Simple Access
    private void addLabel(final IdentifierProxy label)
    {
      if (mLabels == null) {
	mLabels = new LinkedList<IdentifierProxy>();
      }
      mLabels.add(label);
    }

    private void removeLastTransition()
    {
      final int index = mLabels.size() - 1;
      mLabels.remove(index);
    }

    private EdgeProxy createEdgeProxy()
    {
      final SimpleNodeProxy source = getNode(mSourceValue);
      final SimpleNodeProxy target = getNode(mTargetValue);
      final LabelBlockProxy block =
	mFactory.createLabelBlockProxy(mLabels, null);
      return mFactory.createEdgeProxy
	(source, target, block, null, null, null, null);
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mSourceValue;
    private final SimpleExpressionProxy mTargetValue;
    private List<IdentifierProxy> mLabels;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final Comparator<SimpleExpressionProxy> mComparator;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final BindingContext mRootContext;

  private EFAVariable mVariable;
  private List<IdentifierProxy> mBlockedEvents;
  private List<SimpleNodeProxy> mNodeList;
  private List<EdgeProxy> mEdgeList;
  private Map<ProxyAccessor<SimpleExpressionProxy>,SimpleNodeProxy> mNodeMap;
  private Map<EFAVariableTransition,EFAVariableTransition> mTransitions;
  private EFAEvent mEvent;

}
