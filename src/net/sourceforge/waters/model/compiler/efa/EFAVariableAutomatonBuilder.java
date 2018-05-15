//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
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

class EFAVariableAutomatonBuilder extends AbortableCompiler
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
    mEquality = new ModuleEqualityVisitor(false);
    mHashCodeVisitor = ModuleHashCodeVisitor.getInstance(false);
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
      final LabelBlockProxy blocked =
        mFactory.createLabelBlockProxy(mBlockedEvents, null);
      final GraphProxy graph =
        mFactory.createGraphProxy(false, blocked, mNodeList, mEdgeList);
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
    final List<IdentifierProxy> props = new ArrayList<>(nummarkings);
    final CompiledRange range = mVariable.getRange();
    final int rangesize = range.size();
    mNodeList = new ArrayList<SimpleNodeProxy>(rangesize);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mNodeMap = new ProxyAccessorHashMap<>(eq, rangesize);
    mEdgeMap = new TreeMap<>();
    for (final SimpleExpressionProxy value : range.getValues()) {
      checkAbort();
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
        (name, elist, null, initial, null, null, null);
      mNodeList.add(node);
      mNodeMap.putByProxy(value, node);
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
        checkAbort();
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
    throws EvalAbortException
  {
    final int numedges = mEdgeMap.size();
    mEdgeList = new ArrayList<EdgeProxy>(numedges);
    for (final EFAVariableEdge vedge : mEdgeMap.keySet()) {
      checkAbort();
      final EdgeProxy edge = vedge.createEdgeProxy();
      mEdgeList.add(edge);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleNodeProxy getNode(final SimpleExpressionProxy value)
  {
    return mNodeMap.getByProxy(value);
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
    @Override
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

    @Override
    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
        final EFAVariableEdge edge = (EFAVariableEdge) other;
        return
          mEquality.equals(mSourceValue, edge.mSourceValue) &&
          mEquality.equals(mTargetValue, edge.mTargetValue);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return
        mHashCodeVisitor.hashCode(mSourceValue) +
        5 * mHashCodeVisitor.hashCode(mTargetValue);
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
  private final ModuleEqualityVisitor mEquality;
  private final ModuleHashCodeVisitor mHashCodeVisitor;
  private final BindingContext mRootContext;

  private EFAVariable mVariable;
  private List<IdentifierProxy> mBlockedEvents;
  private List<SimpleNodeProxy> mNodeList;
  private List<EdgeProxy> mEdgeList;
  private ProxyAccessorMap<SimpleExpressionProxy,SimpleNodeProxy> mNodeMap;
  private Map<EFAVariableEdge,EFAVariableEdge> mEdgeMap;

}
