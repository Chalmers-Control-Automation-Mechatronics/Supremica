//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.Abortable;
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
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
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
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A compiler to convert an instantiated module ({@link ModuleProxy}) into an
 * EFA system ({@link SimpleEFASystem}).
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystemBuilder implements Abortable
{

  public SimpleEFASystemBuilder(final ModuleProxyFactory factory,
                                final CompilationInfo compilationInfo,
                                final ModuleProxy module)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(mFactory, mCompilationInfo,
                                                             mOperatorTable);
    mInputModule = module;
    final String moduleName = module.getName();
    mVariableContext =
     new SimpleEFAVariableContext(module, mOperatorTable, factory);
    mMarkedVariables = new TIntArrayList();
    mVariableMarkingPredicates = new ArrayList<>();
    final int size = module.getComponentList().size();
    mResultEFASystem = new SimpleEFASystem(moduleName, mVariableContext, size);
    mUserPass = new THashSet<>(5, 0.8f);
    mHelper = new SimpleEFAHelper(factory, mOperatorTable);
    mEventEncoding = null;
  }

  public SimpleEFASystemBuilder(final ModuleProxyFactory factory,
                                final ModuleProxy module)
  {
    this(factory, new CompilationInfo(false, false), module);
  }

  public SimpleEFASystemBuilder(final ModuleProxy module)
  {
    this(ModuleElementFactory.getInstance(), module);
  }

  public SimpleEFASystem compile() throws EvalException
  {
    try {
      // Pass 1 ... Checking the user passes
      for (final DefaultModuleProxyVisitor pass : mUserPass) {
        mInputModule.acceptVisitor(pass);
      }
      // Pass 2 ... Cunstructing global event sets and all variables used
      //            in the given system.
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);

      if (mIsMarkingVariablEFAEnable) {
        final String eventName = SimpleEFAHelper.DEFAULT_MARKINGEVENT_NAME;
        final SimpleIdentifierProxy eventIdent = mFactory.createSimpleIdentifierProxy(eventName);
        if (mEventEncoding.getEventDecl(eventName) == null) {
          final EventDeclProxy decl = mFactory.createEventDeclProxy(eventIdent,
                                                                    EventKind.CONTROLLABLE);
          final SimpleEFAEventDecl edecl = new SimpleEFAEventDecl(decl);
          mEventEncoding.createEventId(edecl);
        }
      }
      // Add collected information
      mResultEFASystem.setEventEncoding(mEventEncoding);
      mResultEFASystem.addVariables(mVariableContext.getVariables());

      // Pass 3 ... Cunstructing EFAs.
      final Pass3Visitor pass3 = new Pass3Visitor();
      mInputModule.acceptVisitor(pass3);

      return mResultEFASystem;
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    }
  }

  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }

  public boolean isConstraintPropagatorEnabled()
  {
    return mIsConstraintPropagatorEnabled;
  }

  public void setConstraintPropagatorEnabled(final boolean enable)
  {
    mIsConstraintPropagatorEnabled = enable;
  }

  public boolean isMarkingVariablEFAEnable()
  {
    return mIsMarkingVariablEFAEnable;
  }

  /**
   *
   * @param enable If marking variable EFA has to be constructed. Note that
   *               marking expressions will be removed in all variables.
   */
  public void setMarkingVariablEFAEnable(final boolean enable)
  {
    mIsMarkingVariablEFAEnable = enable;
  }

  /**
   * Setting a collection of passes to check before constructing the system.
   * These passes can be used to verify any condition on the structure of the
   * system.
   * <p/>
   * @param passes A collection of passes ({@link DefaultModuleProxyVisitor}) to
   *               be checked.
   */
  public void setPass(final Collection<DefaultModuleProxyVisitor> passes)
  {
    mUserPass.addAll(passes);
  }

  public Collection<DefaultModuleProxyVisitor> getPass()
  {
    return mUserPass;
  }
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }

  //#########################################################################
  // Auxiliary methods

  void checkAbort() throws AnalysisAbortException
  {
    if (mIsAborting) {
      throw new AnalysisAbortException();
    }
  }

  void checkAbortInVisitor() throws VisitorException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw new VisitorException(exception);
    }
  }

  //#########################################################################
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation. In this step,
   * the global event map (mEFAEventDeclMap) and variables context
   * (mVariableContext) is constructed. Furthermore, all found variables will be
   * added to the EFASystem.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
     throws VisitorException
    {
      final List<EventDeclProxy> events = module.getEventDeclList();
      final int size = events.size();
      mEventEncoding = new SimpleEFAEventEncoding(size);
      visitCollection(events);
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
     throws VisitorException
    {
      return null;
    }

    @Override
    public SimpleEFAVariable visitVariableComponentProxy(
     final VariableComponentProxy var)
     throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final SimpleExpressionProxy type = var.getType();
        final SimpleExpressionProxy value = mSimpleExpressionCompiler.eval(type, null);
        final CompiledRange range = mSimpleExpressionCompiler.getRangeValue(value);
        mCurrentVariable = mVariableContext.createVariables(var, range);
        visitCollection(var.getVariableMarkings());
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
      return null;
    }

    @Override
    public SimpleEFAVariable visitVariableMarkingProxy(
     final VariableMarkingProxy marking)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final IdentifierProxy prop = marking.getProposition();
      if (eq.equals(prop, mHelper.getMarkingIdentifier())) {
        final SimpleExpressionProxy pred = marking.getPredicate();
        mVariableMarkingPredicates.add(pred);
        mMarkedVariables.add(mCurrentVariable);
      }
      return null;
    }

    @Override
    public Object visitEventDeclProxy(final EventDeclProxy decl)
     throws VisitorException
    {
      checkAbortInVisitor();
      final SimpleEFAEventDecl edecl = new SimpleEFAEventDecl(decl);
      mEventEncoding.createEventId(edecl);
      return null;
    }

    //#########################################################################
    //# Pass2 Data Members
    private int mCurrentVariable;

  }

  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the third pass of EFA compilation. It this step,
   * the EFA components are constructed and will be added to the EFASystem.
   */
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mConstraintPropagator = new ConstraintPropagator(mFactory, mCompilationInfo, mOperatorTable,
                                                       mVariableContext);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    // The vistor always first visit (call) this method.
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
     throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      /**
       * All the components will be visited (see visitSimpleComponentProxy for
       * simple components and visitVariableComponentProxy for variables).
       */
      visitCollection(components);
      // If we are asked for creating a marking variable EFA, then we visit
      // {@link createMarkingVariablesEFA} method.
      if (mIsMarkingVariablEFAEnable) {
        try {
          createMarkingVariablesEFA();
        } catch (final AnalysisException | DuplicateIdentifierException exception) {
          throw wrap(exception);
        }
      }
      return null;
    }

    // Visitin simple components
    @Override
    public SimpleEFAComponent visitSimpleComponentProxy(
     final SimpleComponentProxy comp)
     throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        if (graph.getNodes().isEmpty()) {
          return null;
        }

        // visiting the graph model (node and edges)
        visitGraphProxy(graph);
        // creating a transition relation based on the information we obtained
        // by visiting graph proxy.
        ListBufferTransitionRelation rel = createTransitionRelation(comp);
        // simplifying the transition relation.
        if (simplifyTransitionRelation(rel)) {
          rel = createTransitionRelation(comp);
        }
        final TIntHashSet variables = new TIntHashSet();
        variables.addAll(mAllPrimeVars);
        variables.addAll(mAllUnprimeVars);

        // Creating a simple EFA component
        final SimpleEFAComponent efaComponent = new SimpleEFAComponent(comp.getName(),
                                                                       variables.toArray(),
                                                                       mVariableContext,
                                                                       mStateEncoding,
                                                                       mLabelEncoding, rel,
                                                                       mBlockedEvents.toArray(),
                                                                       comp.getKind());

        efaComponent.setPrimeVariables(mAllPrimeVars.toArray());
        efaComponent.setUnprimeVariables(mAllUnprimeVars.toArray());
        efaComponent.setIsEFA(mIsEFA);

        // Registering this component to all the events and variables.
        efaComponent.register();

        // Adding the new component to the system
        mResultEFASystem.addComponent(efaComponent);
        return efaComponent;
      } catch (final AnalysisException exception) {
        throw wrap(exception);
      } finally {
        mLabelEncoding = null;
        mStateEncoding = null;
        mEdgeLabelMap = null;
        mNodeList = null;
        mEdge = null;
        mEvents = null;
        mAllPrimeVars = null;
        mAllUnprimeVars = null;
        mCurrentPrime = null;
        mCurrentUnprime = null;
        mSimplifyConstraint = null;
        mBlockedEvents = null;
      }
    }

    // Visiting the graph proxy to build up the nodes and edges
    @Override
    public Object visitGraphProxy(final GraphProxy graph)
     throws VisitorException
    {
      mIsEFA = false;
      mBlockedEvents = new TIntHashSet();
      mEvents = new TIntHashSet();
      final LabelBlockProxy block = graph.getBlockedEvents();
      if (block != null) {
        visitLabelBlockProxy(block);
        for (final int e : mEvents.toArray()) {
          mBlockedEvents.add(e);
        }
      }
      // Clearing set for the next use
      mEvents.clear();

      final Collection<NodeProxy> nodes = graph.getNodes();
      mStateEncoding = new SimpleEFAStateEncoding(nodes.size());
      if (mCompilationInfo.isSourceInfoEnabled()) {
        mNodeList = new ArrayList<>(nodes.size());
      } else {
        mNodeList = null;
      }

      final int nbVariables = mVariableContext.getNumberOfVariables();
      mAllPrimeVars = new TIntHashSet(nbVariables);
      mAllUnprimeVars = new TIntHashSet(nbVariables);
      mCurrentPrime = new TIntHashSet(nbVariables);
      mCurrentUnprime = new TIntHashSet(nbVariables);

      // visiting visitSimpleNodeProxy
      visitCollection(nodes);

      final ModuleEqualityVisitor eq2 = new ModuleEqualityVisitor(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mEdgeLabelMap = new ProxyAccessorHashMap<>(eq2, edges.size());
      mLabelEncoding = new SimpleEFALabelEncoding(mEventEncoding, edges.size());

      // visiting visitEdgeProxy
      return visitCollection(edges);
    }

    // vising nodes in the graph
    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      mStateEncoding.createSimpleStateId(node);
      // Adding the node and its id
      if (mNodeList != null) {
        mNodeList.add(node);
      }
      return null;
    }

    @Override
    public Object visitGroupNodeProxy(final GroupNodeProxy proxy)
     throws VisitorException
    {
      //TODO: Flattening the GroupNodes
      throw new VisitorException("EFA System builder: GroupNodes are not supported.");
    }

    // visiting edges inthe graph
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
     throws VisitorException
    {
      checkAbortInVisitor();

      mEdge = edge;
      final GuardActionBlockProxy conditions = edge.getGuardActionBlock();
      if (conditions == null) {
        // no gurad and action then it is always true
        mSimplifyConstraint = ConstraintList.TRUE;
      } else if (conditions.getGuards().isEmpty() && conditions.getActions()
       .isEmpty()) {
        mSimplifyConstraint = ConstraintList.TRUE;
      } else {
        // it has a guard and/or action then visit visitGuardActionBlockProxy
        mSimplifyConstraint = visitGuardActionBlockProxy(conditions);
      }
      // if the guard is not always false
      if (mSimplifyConstraint != null) {
        // if it is not true
        if (!mSimplifyConstraint.isTrue()) {
          // keeping the track of which variables value are change (mPrimeVars)
          // or checked (mUnprimeVars) by this edge.
          mVariableContext.collectAllVariables(mSimplifyConstraint, mCurrentUnprime, mCurrentPrime);
          mAllPrimeVars.addAll(mCurrentPrime);
          mAllUnprimeVars.addAll(mCurrentUnprime);
          mIsEFA = true;
        }

        // Visintg the label block and collecting all the events in that block
        // see visitLabelBlockProxy
        visitLabelBlockProxy(edge.getLabelBlock());

        final TIntArrayList labels = new TIntArrayList(mEvents.size());
        for (final int e : mEvents.toArray()) {
          final SimpleEFAEventDecl edecl = mEventEncoding.getEventDecl(e);
          edecl.addAllPrimeVariable(mCurrentPrime.toArray());
          edecl.addAllUnPrimeVariable(mCurrentUnprime.toArray());
          // creating a new label which is a pair of an event and a condition
          final int labelId = mLabelEncoding.createTransitionLabelId(edecl, mSimplifyConstraint);
          labels.add(labelId);
        }
        mEdgeLabelMap.putByProxy(mEdge, labels);
      }
      // Clearing the sets for the next round
      mCurrentPrime.clear();
      mCurrentUnprime.clear();
      mEvents.clear();

      return null;
    }

    // visiting label block to collect all events in that block
    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
     throws VisitorException
    {
      final List<Proxy> list = block.getEventIdentifierList();
      // visiting visitIdentifierProxy
      visitCollection(list);
      return null;
    }

    @Override
    public SimpleEFAEventDecl visitIdentifierProxy(final IdentifierProxy ident)
     throws VisitorException
    {
        checkAbortInVisitor();
      final SimpleEFAEventDecl edecl = mEventEncoding.getEventDecl(ident.toString());
      if (edecl == null) {
        throw new VisitorException("Event '" + ident + "' cannot be found!");
      }
      // Adding which variables value is change / check by this event
        mEvents.add(mEventEncoding.getEventId(edecl));
        return null;

    }

    // visiting the guard and action block
    @Override
    public ConstraintList visitGuardActionBlockProxy(
     final GuardActionBlockProxy update)
     throws VisitorException
    {
      try {
        // compiling guard/action to a predicate (update)
        final ConstraintList list = mGuardCompiler.getCompiledGuard(update);
        if (mIsConstraintPropagatorEnabled) {
          mConstraintPropagator.init(list);
          mConstraintPropagator.propagate();
          // if it is not a contradiction (always false in the variables context)
          if (!mConstraintPropagator.isUnsatisfiable()) {
            mConstraintPropagator.removeUnchangedVariables();
            return mConstraintPropagator.getAllConstraints(false);
          } else {
            return null;
          }
        } else {
          return list;
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    // visiting variables components. Since we already visited them
    // it returns null.
    @Override
    public SimpleEFAVariable visitVariableComponentProxy(
     final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods

    // Constructing the transition relation
    private ListBufferTransitionRelation createTransitionRelation(final SimpleComponentProxy comp)
     throws AnalysisException
    {
      final GraphProxy graph = comp.getGraph();
      final String name = comp.getName();
      final int eventSize = mLabelEncoding.size();
      final ComponentKind kind = comp.getKind();
      final boolean usesMarking = mStateEncoding.hasMarkedState();
      final boolean usesForbidden = mStateEncoding.hasForbbidenState();
      final int numProps = (usesMarking ? 1 : 0) + (usesForbidden ? 1 : 0);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation(
       name, kind, eventSize, numProps, mStateEncoding.size(),
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      for (final SimpleNodeProxy state : mStateEncoding) {
        final int stateId = mStateEncoding.getStateId(state);
        if (state.isInitial()) {
          rel.setInitial(stateId, true);
        }
        if (usesMarking && mStateEncoding.isMarked(stateId)) {
          rel.setMarked(stateId, SimpleEFAHelper.DEFAULT_MARKING_ID, true);
        }
        if (usesForbidden && mStateEncoding.isForbidden(stateId)) {
          rel.setMarked(stateId, SimpleEFAHelper.DEFAULT_FORBIDDEN_ID, true);
        }
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      for (final EdgeProxy edge : edges) {
        checkAbort();
        final SimpleNodeProxy source = (SimpleNodeProxy) edge.getSource();
        final int sourceState = mStateEncoding.getStateId(source);
        final SimpleNodeProxy target = (SimpleNodeProxy) edge.getTarget();
        final int targetState = mStateEncoding.getStateId(target);
        if ((sourceState < 0) || (targetState < 0)) {
          continue;
        }
        final TIntArrayList labels = mEdgeLabelMap.getByProxy(edge);
        if (labels.isEmpty()) {
          continue;
        }
        for (final int labelId : labels.toArray()) {
          rel.addTransition(sourceState, labelId, targetState);
        }
      }
      return rel;
    }

    private boolean simplifyTransitionRelation(
     final ListBufferTransitionRelation rel) throws AnalysisException
    {
      if (mIsOptimizationEnabled) {
        // 1. Check reachability
        final boolean hasUnreachableStates = checkReachability(rel);
        // 2. Check for unused events
        //final boolean hasRemovableEvents = removeRedundantEvents(rel);

        return hasUnreachableStates; // || hasRemovableEvents;
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
          newNodeList = new ArrayList<>(numStates);
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
        final SimpleEFAStateEncoding newStateEncoding =
         new SimpleEFAStateEncoding(numStates);
        for (final SimpleNodeProxy state : mStateEncoding.getSimpleStates()) {
          final int s = mStateEncoding.getStateId(state);
          newCode = newCodes[s];
          if (newCode >= 0) {
            newStateEncoding.createSimpleStateId(state);
          }
        }
        mStateEncoding = newStateEncoding;
        mNodeList = newNodeList;
        return true;
      } else {
        return false;
      }
    }
    /*
     private boolean removeRedundantEvents(final ListBufferTransitionRelation rel)
     throws AnalysisException
    {
      final int oldNumEvents = rel.getNumberOfProperEvents();
      final boolean[] usedEvents = new boolean[oldNumEvents];
      final TransitionIterator iter =
       rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
          final int source = iter.getCurrentSourceState();
          if (rel.isReachable(source)) {
            usedEvents[event] = true;
          }
        }
      int newNumEvents = 1;
      boolean hasRemovableEvents = false;
      for (int e = 0; e < oldNumEvents; e++) {
        if (!usedEvents[e]) {
          hasRemovableEvents = true;
        } else {
          newNumEvents++;
        }
      }
      if (hasRemovableEvents) {
        final SimpleEFALabelEncoding newEncoding = new SimpleEFALabelEncoding(
         mEventEncoding, newNumEvents);
        for (int e = 0; e < oldNumEvents; e++) {
          Long label = mLabelEncoding.getTransitionLabel(e);
          SimpleEFAEventDecl eventDecl = mLabelEncoding.getEventDecl(label);
          ConstraintList constraint = mLabelEncoding.getConstraint(label);
          if (usedEvents[e]) {
            newEncoding.createTransitionLabelId(eventDecl, constraint);
          } else {
            mBlockedEvents.add(SimpleEFALabelEncoding.getEventId(label));
          }
        }
        mLabelEncoding = newEncoding;
        }
      return hasRemovableEvents;
    }
*/
    // Creating a marking variable EFA. Note that, all variables'
    // marking propositions will be removed. Furthermore, this introduces
    // extra node/transition to the system hence the supervisor is larger than
    // of that computed by marking propositions.
    private void createMarkingVariablesEFA()
     throws DuplicateIdentifierException,
            AnalysisException
    {
      if (!mMarkedVariables.isEmpty()) {
        try {
          checkAbort();
          final SimpleEFALabelEncoding eventEncoding
           = new SimpleEFALabelEncoding(mEventEncoding);
          final ConstraintList markingUpdate =
           new ConstraintList(mVariableMarkingPredicates);

          final String eventName = SimpleEFAHelper.DEFAULT_MARKINGEVENT_NAME;
          final SimpleEFAEventDecl edecl = mEventEncoding.getEventDecl(eventName);
          edecl.addAllUnPrimeVariable(mMarkedVariables.toArray());
          final int eventId = eventEncoding.createTransitionLabelId(edecl, markingUpdate);
          final ListBufferTransitionRelation rel =
           new ListBufferTransitionRelation(eventName, ComponentKind.PLANT,
                                            2, 1, 2,
                                            ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          rel.setInitial(0, true);
          rel.setMarked(1, SimpleEFAHelper.DEFAULT_MARKING_ID, true);
          rel.addTransition(0, eventId, 1);
          final SimpleEFAComponent markingEFA = new SimpleEFAComponent("VariablesMarking",
                                                                       mMarkedVariables.toArray(),
                                                                       mVariableContext,
                                                                       mHelper.getStateEncoding(rel),
                                                                       eventEncoding, rel);
          for (final int varId : mMarkedVariables.toArray()) {
            mVariableContext.getVariable(varId).clearVariableMarkings();
          }
          markingEFA.setUnprimeVariables(mMarkedVariables.toArray());
          markingEFA.setIsEFA(true);
          markingEFA.register();
          mResultEFASystem.addComponent(markingEFA);
        } catch (final OverflowException exception) {
          throw new WatersRuntimeException(exception);
        }
      }
    }

    //#########################################################################
  //# Pass3 Data Members
    private SimpleEFALabelEncoding mLabelEncoding;
    private SimpleEFAStateEncoding mStateEncoding;
    private List<SimpleNodeProxy> mNodeList;
    private ProxyAccessorMap<EdgeProxy, TIntArrayList> mEdgeLabelMap;
    private ConstraintList mSimplifyConstraint;
    private final EFAGuardCompiler mGuardCompiler;
    private final ConstraintPropagator mConstraintPropagator;
    //private final SimpleEFAVariableCollector mEFAVariableCollector;
    private EdgeProxy mEdge;
    private TIntHashSet mEvents;
    private TIntHashSet mBlockedEvents;
    private TIntHashSet mAllPrimeVars;
    private TIntHashSet mAllUnprimeVars;
    private TIntHashSet mCurrentPrime;
    private TIntHashSet mCurrentUnprime;
    private boolean mIsEFA;

  }

  //#########################################################################
  //# SimpleEFASystemBuilder Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private boolean mIsOptimizationEnabled = true;
  private boolean mIsMarkingVariablEFAEnable;
  private final ModuleProxy mInputModule;
  private final SimpleEFAVariableContext mVariableContext;
  private final TIntArrayList mMarkedVariables;
  private final List<SimpleExpressionProxy> mVariableMarkingPredicates;
  private final SimpleEFASystem mResultEFASystem;
  private boolean mIsAborting;
  private final Collection<DefaultModuleProxyVisitor> mUserPass;
  private final SimpleEFAHelper mHelper;
  private boolean mIsConstraintPropagatorEnabled;
  private SimpleEFAEventEncoding mEventEncoding;

}
