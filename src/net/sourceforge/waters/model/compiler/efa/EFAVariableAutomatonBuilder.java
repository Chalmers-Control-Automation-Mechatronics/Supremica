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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.expr.EvalException;
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
     final SimpleExpressionCompiler compiler,
     final BindingContext context)
  {
    mFactory = factory;
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
	constructEventTransitions(event);
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
      mEdgeMap = null;
      mEdgeList = null;
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
    mEdgeMap = new TreeMap<EFAVariableEdge,EFAVariableEdge>();
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

  private void constructEventTransitions(final EFAEvent event)
    throws EvalException
  {
    final EFAVariableTransitionRelation rel = event.getTransitionRelation();
    final EFAVariableTransitionRelationPart part = rel.getPart(mVariable);
    if (part != null) {
      boolean hastrans = false;
      for (final EFAVariableTransition trans : part.getTransitions()) {
        final SimpleExpressionProxy source = trans.getSource();
        final SimpleExpressionProxy target = trans.getTarget();
        createTransition(source, target, event);
        hastrans = true;
      }
      if (!hastrans) {
        final IdentifierProxy ident = event.createIdentifier(mFactory);
        mBlockedEvents.add(ident);
      }
    }
  }

  private void constructEdges()
  {
    final int numedges = mEdgeMap.size();
    mEdgeList = new ArrayList<EdgeProxy>(numedges);
    for (final EFAVariableEdge vedge : mEdgeMap.keySet()) {
      final EdgeProxy edge = vedge.createEdgeProxy();
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

  private void createTransition(final SimpleExpressionProxy source,
				final SimpleExpressionProxy target,
				final EFAEvent event)
  {
    EFAVariableEdge edge = new EFAVariableEdge(source, target);
    final EFAVariableEdge found = mEdgeMap.get(edge);
    if (found == null) {
      mEdgeMap.put(edge, edge);
    } else {
      edge = found;
    }
    final IdentifierProxy ident = event.createIdentifier(mFactory);
    edge.addLabel(ident);
  }


  //#########################################################################
  //# Inner Class EFAVariableEdge
  private class EFAVariableEdge
    implements Comparable<EFAVariableEdge>
  {

    //#######################################################################
    //# Constructor
    private EFAVariableEdge(final SimpleExpressionProxy source,
				  final SimpleExpressionProxy target)
    {
      mSourceValue = source;
      mTargetValue = target;
      mLabels = null;
    }

    //#######################################################################
    //# Hashing and Comparing
    public int compareTo(final EFAVariableEdge edge)
    {
      final CompiledRange range = mVariable.getRange();
      final int source1 = range.indexOf(mSourceValue);
      final int source2 = range.indexOf(edge.mSourceValue);
      if (source1 != source2) {
        return source1 - source2;
      }
      final int target1 = range.indexOf(mTargetValue);
      final int target2 = range.indexOf(edge.mTargetValue);
      return target1 - target2;
    }

    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
	final EFAVariableEdge edge = (EFAVariableEdge) other;
	return
	  mSourceValue.equalsByContents(edge.mSourceValue) &&
	  mTargetValue.equalsByContents(edge.mTargetValue);
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
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final BindingContext mRootContext;

  private EFAVariable mVariable;
  private List<IdentifierProxy> mBlockedEvents;
  private List<SimpleNodeProxy> mNodeList;
  private List<EdgeProxy> mEdgeList;
  private Map<ProxyAccessor<SimpleExpressionProxy>,SimpleNodeProxy> mNodeMap;
  private Map<EFAVariableEdge,EFAVariableEdge> mEdgeMap;

}
