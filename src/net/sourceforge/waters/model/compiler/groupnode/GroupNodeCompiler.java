//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler.groupnode;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.MultiExceptionModuleProxyVisitor;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.compiler.efa.EFAModuleContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * <P>The second pass of the compiler.</P>
 *
 * <P>This pass of the compiler accepts a module ({@link ModuleProxy}) as the
 * input and produces another module as the output. It assumes that the input
 * module has neither nested blocks nor instantiations, and it removes the
 * group nodes by explicitly creating new edges between their simple child
 * nodes ({@link SimpleNodeProxy}).</P>
 *
 * <P>It is ensured that the resultant module only contains objects of the
 * following types:</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e. the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy} containing no {@link GroupNodeProxy};</LI>
 * <LI>{@link VariableComponentProxy}.</LI>
 * </UL>
 *
 * <P>The group node compiler also rewrites conditional blocks and
 * guard/action blocks to reflect overriding in nested groups, and passes
 * them to the next pass of the {@link ModuleCompiler}. In this case, it
 * determines automatically whether the input module uses conditional blocks
 * or guard/action blocks, and outputs a compatible module. That is, in
 * addition to the above, the output module may include conditional blocks
 * ({@link ConditionalProxy}) or guard/action blocks ({@link
 * GuardActionBlockProxy}) on edges, if these are found in the input.</P>
 *
 * @author Roger Su, Robi Malik
 */

public class GroupNodeCompiler extends MultiExceptionModuleProxyVisitor
  implements Abortable
{
  //#########################################################################
  //# Constructor
  public GroupNodeCompiler(final ModuleProxyFactory factory,
                           final CompilationInfo compilationInfo,
                           final ModuleProxy module)
  {
    super(compilationInfo);
    mFactory = factory;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, compilationInfo, mOperatorTable);
    mEquality = new ModuleEqualityVisitor(false);
    mPrimedVariableFinder = new PrimeFinder(mOperatorTable);
    mInputModule = module;
    mCloner = new SourceInfoCloner(factory, compilationInfo);
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile() throws EvalException
  {
    try {
      return visitModuleProxy(mInputModule);
    } catch (final VisitorException exception) {
      throwAsEvalException(exception);
      return null;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  /**
   * Visits the graph ({@link GraphProxy}) of a {@link SimpleComponentProxy},
   * and resolves all group nodes in a graph.
   * @param  graph  The original graph, which may contain group nodes.
   * @return The new graph without any group nodes.
   */
  @Override
  public GraphProxy visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    final Set<NodeProxy> nodes = graph.getNodes();
    final List<CompiledNode> compiledNodes = new ArrayList<>(nodes.size());
    final Map<NodeProxy,CompiledNode> nodeMap = new HashMap<>(nodes.size());
    int numSimpleNodes = 0;
    for (final NodeProxy node : nodes) {
      checkAbortInVisitor();
      final CompiledNode compiledNode = new CompiledNode(node);
      compiledNodes.add(compiledNode);
      nodeMap.put(node, compiledNode);
      if (compiledNode.isSimple()) {
        numSimpleNodes++;
      } else {
        for (final NodeProxy child : node.getImmediateChildNodes()) {
          final CompiledNode compiledChild = nodeMap.get(child);
          compiledNode.addChild(compiledChild);
        }
      }
    }
    if (numSimpleNodes == nodes.size()) {
      return graph;
    }

    mUsingGuardActionBlocks = false;
    for (final EdgeProxy edge : graph.getEdges()) {
      checkAbortInVisitor();
      mUsingGuardActionBlocks |= edge.getGuardActionBlock() != null;
      final NodeProxy source = edge.getSource();
      final CompiledNode compiledSource = nodeMap.get(source);
      compiledSource.addEdge(edge);
    }

    final List<NodeProxy> simpleNodes = new ArrayList<>(numSimpleNodes);
    for (final CompiledNode node : compiledNodes) {
      if (node.isSimple()) {
        simpleNodes.add(node.getInputNode());
      }
      node.createEdges();
    }
    int numEdges = 0;
    for (final CompiledNode node : compiledNodes) {
      numEdges += node.getNumberOfOutputEdges();
    }
    final List<EdgeProxy> newEdges = new ArrayList<>(numEdges);
    for (final CompiledNode node : compiledNodes) {
      node.collectOutputEdges(newEdges);
    }

    return mFactory.createGraphProxy(graph.isDeterministic(),
                                     graph.getBlockedEvents(),
                                     simpleNodes, newEdges);
  }

  @Override
  public ModuleProxy visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    mRootContext = new EFAModuleContext(mInputModule);
    final List<Proxy> components = module.getComponentList();
    mComponents = new ArrayList<>(components.size());
    visitCollection(components);
    return mFactory.createModuleProxy(module.getName(),
                                      module.getComment(),
                                      module.getLocation(),
                                      module.getConstantAliasList(),
                                      module.getEventDeclList(),
                                      module.getEventAliasList(),
                                      mComponents);
  }

  /**
   * Visits a {@link SimpleComponentProxy} of a {@link ModuleProxy}.
   * If the graph of a component contains group nodes, a new component is
   * created and added to the result list of components.
   * Otherwise, the component is added to the list unchanged.
   */
  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    checkAbortInVisitor();
    final GraphProxy oldGraph = comp.getGraph();
    final GraphProxy newGraph = visitGraphProxy(oldGraph);
    if (newGraph != oldGraph) {
      final SimpleComponentProxy newComponent =
        mFactory.createSimpleComponentProxy(comp.getIdentifier(),
                                            comp.getKind(), newGraph,
                                            comp.getAttributes());
      linkCompilationInfo(newComponent, comp);
      mComponents.add(newComponent);
    } else {
      mComponents.add(comp);
    }
    return null;
  }

  @Override
  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy expr = var.getType();
      final SimpleExpressionProxy value =
        mSimpleExpressionCompiler.eval(expr, mRootContext);
      final CompiledRange range =
        mSimpleExpressionCompiler.getRangeValue(value);
      mRootContext.createVariables(var, range, mFactory, mOperatorTable);
      mComponents.add(var);
      return null;
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Inner Class EdgeCreationVisitor
  private class EdgeCreationVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    EdgeCreationVisitor(final CompiledNode source)
    {
      mSource = source;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitConditionalProxy(final ConditionalProxy cond)
      throws VisitorException
    {
      final ConstraintPropagator oldPropagator = mPropagator;
      final SimpleExpressionProxy oldFoundPrime = mFoundPrime;
      final SimpleExpressionProxy guard = cond.getGuard();
      try {
        mPropagator = new ConstraintPropagator(mPropagator);
        mPropagator.addConstraint(guard);
        mPropagator.propagate();
        if (!mPropagator.isUnsatisfiable()) {
          if (mFoundPrime == null) {
            mFoundPrime = mPrimedVariableFinder.containsPrimedVariable(guard);
          }
          visitCollection(cond.getBody());
        }
        return null;
      } catch (final EvalException exception) {
        exception.provideLocation(guard);
        throw wrap(exception);
      } finally {
        mPropagator = oldPropagator;
        mFoundPrime = oldFoundPrime;
      }
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      checkAbortInVisitor();
      mPropagator = new ConstraintPropagator(mFactory, getCompilationInfo(),
                                             mOperatorTable, mRootContext);
      mFoundPrime = null;
      final GuardActionBlockProxy ga = edge.getGuardActionBlock();
      if (ga != null) {
        final List<SimpleExpressionProxy> guards = ga.getGuards();
        if (guards != null && !guards.isEmpty()) {
          final SimpleExpressionProxy guard = guards.get(0);
          mPropagator.addConstraint(guard);
          try {
            mPropagator.propagate();
          } catch (final EvalException exception) {
            exception.provideLocation(guard);
            throw wrap(exception);
          }
          if (mPropagator.isUnsatisfiable()) {
            return null;
          }
          mFoundPrime = mPrimedVariableFinder.containsPrimedVariable(guard);
        }
        mActions = ga.getActions();
      } else {
        mActions = Collections.emptyList();
      }
      mTarget = (SimpleNodeProxy) edge.getTarget();
      final LabelBlockProxy block = edge.getLabelBlock();
      visitCollection(block.getEventIdentifierList());
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        final ConstraintList guard = mPropagator.getAllConstraints();
        mSource.addGuard(ident, guard, mActions, mFoundPrime);
        mSource.createEdges(ident, mPropagator, mActions, mTarget);
        return null;
      } catch (final EvalException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private final CompiledNode mSource;

    private ConstraintPropagator mPropagator;
    private List<BinaryExpressionProxy> mActions;
    private SimpleExpressionProxy mFoundPrime;
    private SimpleNodeProxy mTarget;
  }


  //#########################################################################
  //# Inner Class CompiledNode
  /**
   * A representation of a node ({@link NodeProxy}) with additional
   * information to facilitate group node compilation.
   */
  private class CompiledNode
  {
    //#######################################################################
    //# Constructor
    private CompiledNode(final NodeProxy node)
    {
      mSimple = node instanceof SimpleNodeProxy;
      mInputNode = node;
    }

    //#######################################################################
    //# Simple Access
    private NodeProxy getInputNode()
    {
      return mInputNode;
    }

    private boolean isSimple()
    {
      return mSimple;
    }

    //#######################################################################
    //# Setting Up
    private void addChild(final CompiledNode child)
    {
      mChildren.add(child);
    }

    private void addEdge(final EdgeProxy edge)
    {
      mInputEdges.add(edge);
    }

    //#######################################################################
    //# Algorithm
    private void createEdges()
      throws VisitorException
    {
      final EdgeCreationVisitor visitor = new EdgeCreationVisitor(this);
      for (final EdgeProxy edge : mInputEdges) {
        try {
          visitor.visitEdgeProxy(edge);
        } catch (final VisitorException exception) {
          recordCaughtExceptionInVisitor(exception);
        }
      }
    }

    private void createEdges(final IdentifierProxy ident,
                             final ConstraintPropagator propagator,
                             final List<BinaryExpressionProxy> actions,
                             final SimpleNodeProxy target)
      throws EvalException, VisitorException
    {
      createEdges(ident, propagator, actions, target, true);
    }

    private void createEdges(final IdentifierProxy ident,
                             final ConstraintPropagator propagator,
                             final List<BinaryExpressionProxy> actions,
                             final SimpleNodeProxy target,
                             final boolean original)
      throws EvalException, VisitorException
    {
      checkAbort();
      if (mSimple) {
        final ConstraintList guard = propagator.getAllConstraints();
        addEdge(ident, guard, actions, target, original);
      } else {
        for (final CompiledNode child : mChildren) {
          final GuardInfo info = child.getGuards(ident);
          final ConstraintPropagator childPropagator;
          if (info == null) {
            childPropagator = propagator;
          } else {
            final SimpleExpressionProxy action = info.getFirstAction();
            if (action != null) {
              final String msg = "Variable updates are not yet supported " +
                                 "in combination with group nodes.";
              throw new ActionSyntaxException(msg, action);
            }
            final List<ConstraintList> guards = info.getGuards();
            childPropagator = new ConstraintPropagator(propagator);
            for (final ConstraintList guard : guards) {
              childPropagator.addNegation(guard);
            }
            childPropagator.propagate();
            if (childPropagator.isUnsatisfiable()) {
              continue;
            }
          }
          child.createEdges(ident, childPropagator, actions, target, false);
        }
      }
    }

    private void addGuard(final IdentifierProxy ident,
                          final ConstraintList guard,
                          final List<BinaryExpressionProxy> actions,
                          final SimpleExpressionProxy foundPrime)
    {
      final ProxyAccessor<IdentifierProxy> accessor =
        mGuards.createAccessor(ident);
      GuardInfo info = mGuards.get(accessor);
      if (info == null) {
        info = new GuardInfo();
        mGuards.put(accessor, info);
      }
      info.addGuard(guard);
      info.addAction(actions, foundPrime);
    }

    private GuardInfo getGuards(final IdentifierProxy ident)
    {
      return mGuards.getByProxy(ident);
    }

    private void addEdge(final IdentifierProxy ident,
                         final ConstraintList guard,
                         final List<BinaryExpressionProxy> actions,
                         final SimpleNodeProxy target,
                         final boolean original)
    {
      final TargetInfo info = new TargetInfo(guard, actions, target);
      List<IdentifierProxy> list = mOutputEdges.get(info);
      if (list == null) {
        list = new LinkedList<>();
        mOutputEdges.put(info, list);
      }
      list.add(ident);
      if (!original) {
        mModifiedTargets.add(target);
      }
    }

    //#######################################################################
    //# Output
    private int getNumberOfOutputEdges()
    {
      if (!mSimple) {
        return 0;
      } else if (mUsingGuardActionBlocks) {
        return mOutputEdges.size();
      } else {
        final Set<SimpleNodeProxy> targets = new THashSet<>(mOutputEdges.size());
        for (final TargetInfo info : mOutputEdges.keySet()) {
          final SimpleNodeProxy target = info.getTarget();
          targets.add(target);
        }
        return targets.size();
      }
    }

    private void collectOutputEdges(final List<EdgeProxy> output)
    {
      if (mSimple) {
        for (final EdgeProxy edge : mInputEdges) {
          final NodeProxy target = edge.getTarget();
          if (!mModifiedTargets.contains(target)) {
            output.add(edge);
          }
        }
        if (mUsingGuardActionBlocks) {
          collectOutputEdgesGA(output);
        } else {
          collectOutputEdgesConditional(output);
        }
      }
    }

    private void collectOutputEdgesGA(final List<EdgeProxy> output)
    {
      final SimpleNodeProxy source = (SimpleNodeProxy) mInputNode;
      for (final Map.Entry<TargetInfo,List<IdentifierProxy>> entry :
           mOutputEdges.entrySet()) {
        final TargetInfo info = entry.getKey();
        final SimpleNodeProxy target = info.getTarget();
        if (mModifiedTargets.contains(target)) {
          final ConstraintList guard = info.getGuard();
          final List<SimpleExpressionProxy> guards;
          if (guard.isTrue()) {
            guards = null;
          } else {
            final SimpleExpressionProxy guardExpression =
              guard.createExpression(mFactory, mOperatorTable.getAndOperator());
            guards = Collections.singletonList(guardExpression);
          }
          final List<BinaryExpressionProxy> actions = info.getActions();
          final List<BinaryExpressionProxy> clonedActions =
            actions.isEmpty() ? null : mCloner.getClonedList(actions);
          final GuardActionBlockProxy ga;
          if (guards == null && clonedActions == null) {
            ga = null;
          } else {
            ga = mFactory.createGuardActionBlockProxy
              (guards, clonedActions, null);
          }
          final List<IdentifierProxy> labels = entry.getValue();
          final List<Proxy> clonedLabels = new ArrayList<>(labels.size());
          final ProxyAccessorSet<IdentifierProxy> uniqueLebels =
            new ProxyAccessorHashSet<>(mEquality, labels.size());
          collectUniqueLabels(labels, uniqueLebels, clonedLabels);
          final LabelBlockProxy block =
            mFactory.createLabelBlockProxy(clonedLabels, null);
          final EdgeProxy edge =
            mFactory.createEdgeProxy(source, target, block, ga, null, null, null);
          output.add(edge);
        }
      }
    }

    private void collectOutputEdgesConditional(final List<EdgeProxy> output)
    {
      final int numEntries = mOutputEdges.size() - mModifiedTargets.size();
      final Map<SimpleNodeProxy,List<TargetInfo>> targetMap =
        new LinkedHashMap<>(numEntries);
      for (final TargetInfo info : mOutputEdges.keySet()) {
        assert info.getActions().isEmpty();
        final SimpleNodeProxy target = info.getTarget();
        if (mModifiedTargets.contains(target)) {
          List<TargetInfo> list = targetMap.get(target);
          if (list == null) {
            list = new LinkedList<>();
            targetMap.put(target, list);
          }
          list.add(info);
        }
      }
      final SimpleNodeProxy source = (SimpleNodeProxy) mInputNode;
      for (final Map.Entry<SimpleNodeProxy,List<TargetInfo>> entry :
           targetMap.entrySet()) {
        int size = 0;
        for (final TargetInfo info : entry.getValue()) {
          final ConstraintList guard = info.getGuard();
          if (guard.isTrue()) {
            final List<IdentifierProxy> labels = mOutputEdges.get(info);
            size += labels.size();
          } else {
            size++;
          }
        }
        final List<Proxy> labelBlockBody = new ArrayList<>(size);
        final ProxyAccessorSet<IdentifierProxy> uniqueLebels =
          new ProxyAccessorHashSet<>(mEquality, size);
        for (final TargetInfo info : entry.getValue()) {
          final ConstraintList guard = info.getGuard();
          final List<IdentifierProxy> labels = mOutputEdges.get(info);
          if (guard.isTrue()) {
            collectUniqueLabels(labels, uniqueLebels, labelBlockBody);
          } else {
            final int numLabels = labels.size();
            final List<Proxy> condLabels = new ArrayList<>(numLabels);
            final ProxyAccessorSet<IdentifierProxy> condUniqueLebels =
              new ProxyAccessorHashSet<>(mEquality, numLabels);
            collectUniqueLabels(labels, condUniqueLebels, condLabels);
            final SimpleExpressionProxy guardExpression =
              guard.createExpression(mFactory, mOperatorTable.getAndOperator());
            final ConditionalProxy cond =
              mFactory.createConditionalProxy(condLabels, guardExpression);
            labelBlockBody.add(cond);
          }
        }
        final LabelBlockProxy block =
          mFactory.createLabelBlockProxy(labelBlockBody, null);
        final SimpleNodeProxy target = entry.getKey();
        final EdgeProxy edge =
          mFactory.createEdgeProxy(source, target, block, null, null, null, null);
        output.add(edge);
      }
    }

    private void collectUniqueLabels(final List<IdentifierProxy> input,
                                     final ProxyAccessorSet<IdentifierProxy> unique,
                                     final List<Proxy> output)
    {
      for (final IdentifierProxy label : input) {
        if (unique.addProxy(label)) {
          final Proxy clonedLabel = mCloner.getClone(label);
          output.add(clonedLabel);
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mInputNode.toString();
    }

    //#######################################################################
    //# Data Members
    private final boolean mSimple;
    private final NodeProxy mInputNode;
    private final List<EdgeProxy> mInputEdges = new LinkedList<>();
    private final List<CompiledNode> mChildren = new LinkedList<>();
    private final ProxyAccessorMap<IdentifierProxy,GuardInfo> mGuards =
      new ProxyAccessorHashMap<>(mEquality);
    private final Map<TargetInfo,List<IdentifierProxy>> mOutputEdges =
      new LinkedHashMap<>();
    private final Set<SimpleNodeProxy> mModifiedTargets = new THashSet<>();
  }


  //#########################################################################
  //# Inner Class GuardInfo
  private static class GuardInfo
  {
    //#######################################################################
    //# Simple Access
    private List<ConstraintList> getGuards()
    {
      return mGuards;
    }

    private SimpleExpressionProxy getFirstAction()
    {
      return mFirstAction;
    }

    private void addGuard(final ConstraintList guard)
    {
      mGuards.add(guard);
    }

    private void addAction(final List<BinaryExpressionProxy> actions,
                           final SimpleExpressionProxy foundPrime)
    {
      if (mFirstAction == null) {
        if (!actions.isEmpty()) {
          mFirstAction = actions.iterator().next();
        } else {
          mFirstAction = foundPrime;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final List<ConstraintList> mGuards = new LinkedList<>();
    private SimpleExpressionProxy mFirstAction;
  }


  //#########################################################################
  //# Inner Class TargetInfo
  private class TargetInfo
  {
    //#######################################################################
    //# Constructor
    private TargetInfo(final ConstraintList guard,
                       final List<BinaryExpressionProxy> actions,
                       final SimpleNodeProxy target)
    {
      mGuard = guard;
      mActions = actions;
      mTarget = target;
    }

    //#######################################################################
    //# Simple Access
    private ConstraintList getGuard()
    {
      return mGuard;
    }

    private List<BinaryExpressionProxy> getActions()
    {
      return mActions;
    }

    private SimpleNodeProxy getTarget()
    {
      return mTarget;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final TargetInfo info = (TargetInfo) other;
        return
          mGuard.equals(info.mGuard) &&
          mEquality.isEqualList(mActions, info.mActions) &&
          mTarget == info.mTarget;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      final ModuleHashCodeVisitor hash = mEquality.getHashCodeVisitor();
      return
        mGuard.hashCode() +
        5 * hash.getListHashCode(mActions) +
        25 * mTarget.hashCode();
    }

    //#######################################################################
    //# Data Members
    private final ConstraintList mGuard;
    private final List<BinaryExpressionProxy> mActions;
    private final SimpleNodeProxy mTarget;
  }


  //#########################################################################
  //# Data Members
  // These variables are related to the entire instance of this class.
  private final ModuleProxy mInputModule;   // The original input module
  private List<ComponentProxy> mComponents; // The list of output components
  private EFAModuleContext mRootContext;

  // This variable is reset for every graph.
  private boolean mUsingGuardActionBlocks;

  // These variables are utilities used by the compiler.
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleEqualityVisitor mEquality;
  private final PrimeFinder mPrimedVariableFinder;
  private final ModuleProxyCloner mCloner;
}
