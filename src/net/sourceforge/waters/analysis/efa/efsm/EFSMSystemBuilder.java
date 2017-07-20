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

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>
 * A compiler to convert an instantiated module ({@link ModuleProxy}) into and
 * EFSM system ({@link EFSMSystem}).
 * </P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMSystemBuilder extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public EFSMSystemBuilder(final ModuleProxyFactory factory,
                           final CompilationInfo compilationInfo,
                           final ModuleProxy module)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mCompilationInfo,
                                   mOperatorTable);
    mEFSMVariableFinder = new EFSMVariableFinder(mOperatorTable);
    mInputModule = module;
    final String moduleName = module.getName();
    mVariableContext = new EFSMVariableContext(module, mOperatorTable);
    mMarkedVariables = new ArrayList<EFSMVariable>();
    mVariableMarkingPredicates = new ArrayList<SimpleExpressionProxy>();
    final int size = module.getComponentList().size();
    mResultEFSMSystem = new EFSMSystem(moduleName, mVariableContext, size);
  }


  //#########################################################################
  //# Invocation
  public EFSMSystem compile()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      final Pass3Visitor pass3 = new Pass3Visitor();
      mInputModule.acceptVisitor(pass3);
      createMarkingTR();
      return mResultEFSMSystem;
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else if (cause instanceof AnalysisException) {
        throw (AnalysisException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Configuration
  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }

  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mDefaultMarking = marking;
  }

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mDefaultMarking;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createMarkingTR()
    throws AnalysisAbortException
  {
    if (!mMarkedVariables.isEmpty()) {
      try {
        checkAbort();
        final EFSMEventEncoding eventEncoding = new EFSMEventEncoding(2);
        final ConstraintList markingUpdate =
          new ConstraintList(mVariableMarkingPredicates);
        final int event = eventEncoding.createEventId(markingUpdate);
        final String name = ":marking:" + mDefaultMarking.toString();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation
            (name, ComponentKind.PLANT, 2, 1, 2,
             ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        rel.setInitial(0, true);
        rel.setMarked(1, 0, true);
        rel.addTransition(0, event, 1);
        final EFSMTransitionRelation efsmTR =
          new EFSMTransitionRelation(rel, eventEncoding, mMarkedVariables);
        mResultEFSMSystem.addTransitionRelation(efsmTR);
        efsmTR.register();
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass1Visitor()
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mGlobalEventsMap = new ProxyAccessorHashMap<>(eq);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<SimpleIdentifierProxy> visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy bEvents = graph.getBlockedEvents();
      final Collection<EdgeProxy> edges = graph.getEdges();
      if (bEvents != null) {
        visitLabelBlockProxy(bEvents);
      }
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy label = edge.getLabelBlock();
      return visitLabelBlockProxy(label);
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy label)
      throws VisitorException
    {
      final List<Proxy> list = label.getEventIdentifierList();
      return visitCollection(list);
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      checkAbortInVisitor();
      final SimpleComponentProxy comp = mGlobalEventsMap.getByProxy(ident);
      if (comp == null) {
        mGlobalEventsMap.putByProxy(ident, mCurrentComponent);
      } else if (comp != mCurrentComponent) {
        final SharedEventException exception =
          new SharedEventException(ident, comp, mCurrentComponent);
        throw wrap(exception);
      }
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      mCurrentComponent = comp;
      final GraphProxy graph = comp.getGraph();
      return visitGraphProxy(graph);
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<IdentifierProxy,SimpleComponentProxy> mGlobalEventsMap;
    private SimpleComponentProxy mCurrentComponent;
  }


  //#########################################################################
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      try {
        for (final SimpleIdentifierProxy ident : expr.getItems()) {
          mVariableContext.insertEnumAtom(ident);
        }
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      return null;
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy simple)
    {
      return null;
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final SimpleExpressionProxy type = var.getType();
        type.acceptVisitor(this);
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(type, null);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        mCurrentVariable =
          new EFSMVariable(var, range, mFactory, mOperatorTable);
        mVariableContext.addVariable(mCurrentVariable);
        mResultEFSMSystem.addVariable(mCurrentVariable);
        visitCollection(var.getVariableMarkings());
        return mCurrentVariable;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFSMVariable visitVariableMarkingProxy
      (final VariableMarkingProxy marking)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final IdentifierProxy prop = marking.getProposition();
      if (eq.equals(prop, mDefaultMarking)) {
        final SimpleExpressionProxy pred = marking.getPredicate();
        mVariableMarkingPredicates.add(pred);
        mMarkedVariables.add(mCurrentVariable);
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private EFSMVariable mCurrentVariable;
  }


  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the third pass of EFA compilation.
   */
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mConstraintPropagator =
        new ConstraintPropagator(mFactory, mCompilationInfo,
                                 mOperatorTable, mVariableContext);
      mEFSMVariableCollector = new EFSMVariableCollector(mOperatorTable, mVariableContext);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public EFSMTransitionRelation visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        ListBufferTransitionRelation rel = createTransitionRelation(comp);
        if (simplifyTransitionRelation(rel)) {
          if (mEventEncoding.size() <= 1 && !mUsesMarking) {
            return null;
          }
          rel = createTransitionRelation(comp);
        }
        if (mUsesMarking && !mMarkedVariables.isEmpty()) {
          // Throw an exception because markings in variables and
          // automata together are not yet supported.
          final EFSMVariable var = mMarkedVariables.get(0);
          final SharedEventException exception =
            new SharedEventException(mDefaultMarking, comp, var.getComponent());
          throw wrap(exception);
        }
        final Collection<EFSMVariable> variables = new THashSet<EFSMVariable>();
        mEFSMVariableCollector.collectAllVariables(mEventEncoding, variables);
        final EFSMTransitionRelation efsmTransitionRelation =
          new EFSMTransitionRelation(rel, mEventEncoding, variables, mNodeList);
        efsmTransitionRelation.register();
        mResultEFSMSystem.addTransitionRelation(efsmTransitionRelation);
        return efsmTransitionRelation;
      } catch (final AnalysisException exception) {
        throw wrap(exception);
      } finally {
        mEventEncoding = null;
        mStateMap = null;
        mNodeList = null;
      }
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      mUsesMarking = false;
      final LabelBlockProxy block = graph.getBlockedEvents();
      if (block != null) {
        mUsesMarking = containsMarkingProposition(block);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      mStateMap = new TObjectIntHashMap<>(nodes.size(), 0.5f, -1);
      if (mCompilationInfo.isSourceInfoEnabled()) {
        mNodeList = new ArrayList<>(nodes.size());
      } else {
        mNodeList = null;
      }
      visitCollection(nodes);
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mSimplifiedGuardActionBlockMap =
        new ProxyAccessorHashMap<>(eq, edges.size());
      mEventEncoding = new EFSMEventEncoding(edges.size());
      return visitCollection(edges);
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final int code = mStateMap.size();
      mStateMap.put(node, code);
      if (mNodeList != null) {
        mNodeList.add(node);
      }
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mUsesMarking = true;
      }
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      checkAbortInVisitor();
      final GuardActionBlockProxy update = edge.getGuardActionBlock();
      if (update == null) {
        mSimplifiedGuardActionBlockMap.putByProxy(update, ConstraintList.TRUE);
        mEventEncoding.createEventId(ConstraintList.TRUE);
      } else {
        visitGuardActionBlockProxy(update);
      }
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy update)
      throws VisitorException
    {
      try {
        final ConstraintList list = mGuardCompiler.getCompiledGuard(update);
        mConstraintPropagator.init(list);
        mConstraintPropagator.propagate();
        if (!mConstraintPropagator.isUnsatisfiable()) {
          mConstraintPropagator.removeUnchangedVariables();
          final ConstraintList allConstraints =
            mConstraintPropagator.getAllConstraints();
          mSimplifiedGuardActionBlockMap.putByProxy(update, allConstraints);
          mEventEncoding.createEventId(allConstraints);
        }
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean containsMarkingProposition
      (final EventListExpressionProxy list)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      return eq.contains(list.getEventIdentifierList(), mDefaultMarking);
    }

    private ListBufferTransitionRelation createTransitionRelation
      (final SimpleComponentProxy comp)
      throws AnalysisException
    {
      final GraphProxy graph = comp.getGraph();
      final String name = comp.getName();
      final int eventSize = mEventEncoding.size();
      final ComponentKind kind = comp.getKind();
      final int numProps = mUsesMarking ? 1 : 0;
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(name,
                                         kind,
                                         eventSize,
                                         numProps,
                                         mStateMap.size(),
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (mIsOptimizationEnabled) {
        mEventEncoding.setSelfloops(rel, mEFSMVariableFinder);
      }
      final TObjectIntIterator<SimpleNodeProxy> iter = mStateMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final SimpleNodeProxy node = iter.key();
        final int code = iter.value();
        if (node.isInitial()) {
          rel.setInitial(code, true);
        }
        if (mUsesMarking &&
            containsMarkingProposition(node.getPropositions())) {
          rel.setMarked(code, 0, true);
        }
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      for (final EdgeProxy edge : edges) {
        checkAbort();
        final GuardActionBlockProxy update = edge.getGuardActionBlock();
        final ConstraintList simplifiedList =
          mSimplifiedGuardActionBlockMap.getByProxy(update);
        if (simplifiedList == null) {
          continue;
        }
        final int event = mEventEncoding.getEventId(simplifiedList);
        if (event < 0) {
          continue;
        }
        final SimpleNodeProxy source = (SimpleNodeProxy) edge.getSource();
        final int sourceState = mStateMap.get(source);
        if (sourceState < 0) {
          continue;
        }
        final SimpleNodeProxy target = (SimpleNodeProxy) edge.getTarget();
        final int targetState = mStateMap.get(target);
        rel.addTransition(sourceState, event, targetState);
      }
      return rel;
    }

    private boolean simplifyTransitionRelation
      (final ListBufferTransitionRelation rel)
    {
      if (mIsOptimizationEnabled) {
        // 1. Check reachability
        final boolean hasUnreachableStates = checkReachability(rel);
        // 2. Check for unused events or selfloops
        final boolean hasRemovableEvents = removeRedundantEvents(rel);
        // 3. Check for redundant propositions
        if (rel.removeRedundantPropositions()) {
          mUsesMarking = rel.isPropositionUsed(0);
        }
        return hasUnreachableStates || hasRemovableEvents;
      } else {
        return false;
      }
    }

    private boolean checkReachability(final ListBufferTransitionRelation rel)
    {
      if (rel.checkReachability()) {
        final int numStates = rel.getNumberOfStates();
        final int[] newCodes = new int[numStates];
        final List<SimpleNodeProxy> newNodeList;
        if (mNodeList == null) {
          newNodeList = null;
        } else {
          newNodeList = new ArrayList<SimpleNodeProxy>(numStates);
        }
        int newCode = 0;
        for (int s = 0; s < numStates; s++) {
          if (rel.isReachable(s)) {
            newCodes[s] = newCode++;
            if (newNodeList != null) {
              final SimpleNodeProxy node = mNodeList.get(s);
              newNodeList.add(node);
            }
          } else {
            newCodes[s] = -1;
          }
        }
        final TObjectIntHashMap<SimpleNodeProxy> newStateMap =
          new TObjectIntHashMap<SimpleNodeProxy>(numStates, 0.5f, -1);
        final TObjectIntIterator<SimpleNodeProxy> iter = mStateMap.iterator();
        while (iter.hasNext()) {
          iter.advance();
          final int s = iter.value();
          newCode = newCodes[s];
          if (newCode >= 0) {
            final SimpleNodeProxy node = iter.key();
            newStateMap.put(node, newCode);
          }
        }
        mStateMap = newStateMap;
        mNodeList = newNodeList;
        return true;
      } else {
        return false;
      }
    }

    private boolean removeRedundantEvents(final ListBufferTransitionRelation rel)
    {
      final int oldNumEvents = rel.getNumberOfProperEvents();
      final boolean[] usedEvents = new boolean[oldNumEvents];
      final int[] selfloops = new int[oldNumEvents];
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        if (event != EventEncoding.TAU) {
          final int source = iter.getCurrentSourceState();
          if (rel.isReachable(source)) {
            usedEvents[event] = true;
            if (selfloops[event] >= 0) {
              final int target = iter.getCurrentTargetState();
              if (source == target) {
                selfloops[event]++;
              } else {
                selfloops[event] = -1;
              }
            }
          }
        }
      }
      final int numReachable = mStateMap.size();
      int newNumEvents = 1;
      boolean hasRemovableEvents = false;
      for (int e = EventEncoding.NONTAU; e < oldNumEvents; e++) {
        if (!usedEvents[e]) {
          hasRemovableEvents = true;
        } else if (selfloops[e] == numReachable) {
          usedEvents[e] = false;
          hasRemovableEvents = true;
          final ConstraintList update = mEventEncoding.getUpdate(e);
          final int numVars = mResultEFSMSystem.getVariables().size();
          final List<EFSMVariable> vars =
            new ArrayList<EFSMVariable>(numVars);
          mEFSMVariableCollector.collectAllVariables(update, vars);
          for (final EFSMVariable var : vars) {
            var.addSelfloop(update);
          }
        } else {
          newNumEvents++;
        }
      }
      if (hasRemovableEvents) {
        usedEvents[EventEncoding.TAU] = true;
        final EFSMEventEncoding newEncoding =
          new EFSMEventEncoding(newNumEvents);
        for (int e = 0; e < oldNumEvents; e++) {
          if (usedEvents[e]) {
            final ConstraintList update = mEventEncoding.getUpdate(e);
            newEncoding.createEventId(update);
          }
        }
        mEventEncoding = newEncoding;
      }
      return hasRemovableEvents;
    }


    //#######################################################################
    //# Data Members
    private EFSMEventEncoding mEventEncoding;
    private TObjectIntHashMap<SimpleNodeProxy> mStateMap;
    private List<SimpleNodeProxy> mNodeList;
    private boolean mUsesMarking;
    private ProxyAccessorMap<GuardActionBlockProxy,ConstraintList>
      mSimplifiedGuardActionBlockMap;

    private final EFAGuardCompiler mGuardCompiler;
    private final ConstraintPropagator mConstraintPropagator;
    private final EFSMVariableCollector mEFSMVariableCollector;
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final EFSMVariableFinder mEFSMVariableFinder;

  private boolean mIsOptimizationEnabled;
  private IdentifierProxy mDefaultMarking;

  private final ModuleProxy mInputModule;
  private final EFSMVariableContext mVariableContext;
  private final List<EFSMVariable> mMarkedVariables;
  private final List<SimpleExpressionProxy> mVariableMarkingPredicates;
  private final EFSMSystem mResultEFSMSystem;

}
