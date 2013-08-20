//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFASystemBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.*;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A compiler to convert an instantiated module ({@link ModuleProxy}) into an
 * EFA system ({@link SimpleEFASystem}).
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystemBuilder implements Abortable
{

  public SimpleEFASystemBuilder(final ModuleProxyFactory factory,
   final SourceInfoBuilder builder,
   final ModuleProxy module)
  {
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mTrueGuard = new ConstraintList();
    mSimpleExpressionCompiler =
     new SimpleExpressionCompiler(mFactory, mOperatorTable);
//    mEFAVariableFinder = new SimpleEFAVariableFinder(mOperatorTable);
    mInputModule = module;
    final String moduleName = module.getName();
    mVariableContext = new SimpleEFAVariableContext(module, mOperatorTable);
    mMarkedVariables = new ArrayList<SimpleEFAVariable>();
    mVariableMarkingPredicates = new ArrayList<SimpleExpressionProxy>();
    final int size = module.getComponentList().size();
    mResultEFASystem = new SimpleEFASystem(moduleName, mVariableContext, size);
    mUserPass = new THashSet<DefaultModuleProxyVisitor>(5, 0.8f);
  }

  public SimpleEFASystem compile() throws EvalException
  {
    try {
      // Pass 1 ... Checking the user passes
      for (DefaultModuleProxyVisitor pass : mUserPass) {
        mInputModule.acceptVisitor(pass);
      }
      // Pass 2 ... Cunstructing global event sets and all variables used 
      //            in the given system.
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ... Cunstructing EFAs.
      final Pass3Visitor pass3 = new Pass3Visitor();
      mInputModule.acceptVisitor(pass3);
      // Add all events 
      mResultEFASystem.AddSystemEvents(mEFAEventDeclMap.values());
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

  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mDefaultMarking = marking;
  }

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mDefaultMarking;
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

  //#########################################################################
  //# Auxiliary Methods
  private void insertEventDecl(final IdentifierProxy ident,
   final SimpleEFAEventDecl edecl)
   throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
     mEFAEventDeclMap.createAccessor(ident);
    if (mEFAEventDeclMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident, "event");
    } else {
      mEFAEventDeclMap.put(accessor, edecl);
    }
  }

  private SimpleEFAEventDecl getEventDecl(final IdentifierProxy ident)
  {
    return mEFAEventDeclMap.getByProxy(ident);
  }

  private boolean hasIdentifier(final IdentifierProxy ident)
  {
    return (getEventDecl(ident) != null);
  }

  private SimpleEFAEventDecl findEventDecl(final IdentifierProxy ident)
   throws UndefinedIdentifierException
  {
    final SimpleEFAEventDecl edecl = getEventDecl(ident);
    if (edecl == null) {
      throw new UndefinedIdentifierException(ident, "event");
    } else {
      return edecl;
    }
  }

  void checkAbort()
   throws AnalysisAbortException
  {
    if (mIsAborting) {
      throw new AnalysisAbortException();
    }
  }

  void checkAbortInVisitor()
   throws VisitorException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw new VisitorException(exception);
    }
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
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation. In this step,
   * the global event map (mEFAEventDeclMap) and variables context
   * (mVariableContext) is constructed. Furthermore, all found variables will be
   * added to the EFASystem.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {

    private SimpleEFAVariable mCurrentVariable;

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
     throws VisitorException
    {
      final ModuleEqualityVisitor eq =
       ModuleEqualityVisitor.getInstance(false);
      final List<EventDeclProxy> events = module.getEventDeclList();
      final int size = events.size();
      mEFAEventDeclMap =
       new ProxyAccessorHashMap<IdentifierProxy, SimpleEFAEventDecl>(eq, size);
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
        final SimpleExpressionProxy value =
         mSimpleExpressionCompiler.eval(type, null);
        final CompiledRange range =
         mSimpleExpressionCompiler.getRangeValue(value);
        mCurrentVariable =
         new SimpleEFAVariable(var, range, mFactory, mOperatorTable);
        mVariableContext.addVariable(mCurrentVariable);
        mResultEFASystem.addVariable(mCurrentVariable);
        visitCollection(var.getVariableMarkings());
        return mCurrentVariable;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public SimpleEFAVariable visitVariableMarkingProxy(
     final VariableMarkingProxy marking)
    {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      final IdentifierProxy prop = marking.getProposition();
      if (eq.equals(prop, mDefaultMarking)) {
        final SimpleExpressionProxy pred = marking.getPredicate();
        mVariableMarkingPredicates.add(pred);
        mMarkedVariables.add(mCurrentVariable);
      }
      return null;
    }

    @Override
    public SimpleEFAEventDecl visitEventDeclProxy(final EventDeclProxy decl)
     throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final IdentifierProxy ident = decl.getIdentifier();
        final SimpleEFAEventDecl edecl = new SimpleEFAEventDecl(decl);
        insertEventDecl(ident, edecl);
        return edecl;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }
  }

  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the third pass of EFA compilation. It this step,
   * the EFA components are constructed and will be added to the EFASystem.
   */
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {

    private SimpleEFATransitionLabelEncoding mEventEncoding;
    private TObjectIntHashMap<SimpleNodeProxy> mStateMap;
    private List<SimpleNodeProxy> mNodeList;
    private boolean mUsesMarking;
    private ProxyAccessorMap<EdgeProxy, SimpleEFATransitionLabel> mEdgeLabelMap;
    private ConstraintList mSimplifyConstraint;
    private final EFAGuardCompiler mGuardCompiler;
    private final ConstraintPropagator mConstraintPropagator;
    private final SimpleEFAVariableCollector mEFAVariableCollector;
    private EdgeProxy mEdge;
    private Collection<SimpleEFAEventDecl> mEvents;
    private THashSet<SimpleEFAVariable> mAllPrimeVars;
    private THashSet<SimpleEFAVariable> mAllUnprimeVars;
    private THashSet<SimpleEFAVariable> mCurrentPrime;
    private THashSet<SimpleEFAVariable> mCurrentUnprime;

    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mConstraintPropagator =
       new ConstraintPropagator(mFactory, mOperatorTable, mVariableContext);
      mEFAVariableCollector = new SimpleEFAVariableCollector(mOperatorTable,
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
        } catch (final AnalysisException exception) {
          throw wrap(exception);
        } catch (final DuplicateIdentifierException ex) {
          throw wrap(ex);
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
        // visiting the graph model (node and edges)
        visitGraphProxy(graph);
        // creating a transition relation based on the information we obtained 
        // by visiting graph proxy.
        ListBufferTransitionRelation rel = createTransitionRelation(comp);
        // simplifying the transition relation.
        if (simplifyTransitionRelation(rel)) {
          if (mEventEncoding.size() <= 1 && !mUsesMarking) {
            return null;
          }
          rel = createTransitionRelation(comp);
        }
        final Collection<SimpleEFAVariable> variables =
         new THashSet<SimpleEFAVariable>();
        // Collecting all the variables in the colletion "variables".
        mEFAVariableCollector.collectAllVariables(mEventEncoding, variables);
        // Creating a simple EFA component
        final SimpleEFAComponent efaComponent =
         new SimpleEFAComponent(comp.getName(), rel, mEventEncoding, variables,
         mNodeList);
        // Registering this component to all the variables.
        efaComponent.register();
        // Add this component to the variables' modifiers (i.e. updating), 
        // if it does so. 
        for (final SimpleEFAVariable v : mAllPrimeVars) {
          v.addModifier(efaComponent);
        }
        // Add this component to the variables' visitors (i.e. checking), 
        // if it does so.
        for (final SimpleEFAVariable v : mAllUnprimeVars) {
          v.addVisitor(efaComponent);
        }
        // Adding the new component to the system
        mResultEFASystem.addComponent(efaComponent);
        return efaComponent;
      } catch (final AnalysisException exception) {
        throw wrap(exception);
      } finally {
        mEventEncoding = null;
        mStateMap = null;
        mNodeList = null;
        mEvents = null;
        mAllPrimeVars = null;
        mAllUnprimeVars = null;
        mCurrentPrime = null;
        mCurrentUnprime = null;
      }
    }

    // Visiting the graph proxy to build up the nodes and edges
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
      mStateMap =
       new TObjectIntHashMap<SimpleNodeProxy>(nodes.size(), 0.5f, -1);
      if (mSourceInfoBuilder != null) {
        mNodeList = new ArrayList<SimpleNodeProxy>(nodes.size());
      } else {
        mNodeList = null;
      }
      // visiting visitSimpleNodeProxy
      visitCollection(nodes);

      final ModuleEqualityVisitor eq =
       ModuleEqualityVisitor.getInstance(false);
      final Collection<EdgeProxy> edges = graph.getEdges();

      mEdgeLabelMap =
       new ProxyAccessorHashMap<EdgeProxy, SimpleEFATransitionLabel>(eq, edges.
       size());
      mEventEncoding = new SimpleEFATransitionLabelEncoding(edges.size());
      mEvents = new ArrayList<SimpleEFAEventDecl>();
      int nbVariables = mVariableContext.getNumberOfVariables();
      mAllPrimeVars = new THashSet<SimpleEFAVariable>(nbVariables);
      mAllUnprimeVars = new THashSet<SimpleEFAVariable>(nbVariables);
      mCurrentPrime = new THashSet<SimpleEFAVariable>(nbVariables);
      mCurrentUnprime = new THashSet<SimpleEFAVariable>(nbVariables);

      // visiting visitEdgeProxy
      return visitCollection(edges);
    }

    // vising nodes in the graph
    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final int nodeId = mStateMap.size();
      // Adding the node and its id
      mStateMap.put(node, nodeId);
      if (mNodeList != null) {
        mNodeList.add(node);
      }
      // do we have a marked node?
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mUsesMarking = true;
      }
      return null;
    }

    // visiting edges inthe graph
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
     throws VisitorException
    {
      checkAbortInVisitor();

      mEdge = edge;
      final GuardActionBlockProxy update = edge.getGuardActionBlock();
      if (update == null) {
        // no gurad and action then it is always true
        mSimplifyConstraint = mTrueGuard;
      } else {
        // it has a guard and/or action then visit visitGuardActionBlockProxy
        mSimplifyConstraint = visitGuardActionBlockProxy(update);
      }
      // if the guard is not always false
      if (mSimplifyConstraint != null) {
        // if it is not true
        if (!mSimplifyConstraint.isTrue()) {
          // keeping the track of which variables value are change (mPrimeVars)
          // or checked (mUnprimeVars) by this edge.
          mEFAVariableCollector.collectAllVariables(mSimplifyConstraint,
           mCurrentUnprime, mCurrentPrime);
          mAllPrimeVars.addAll(mCurrentPrime);
          mAllUnprimeVars.addAll(mCurrentUnprime);
        }

        // Visintg the label block and collecting all the events in that block
        // see visitLabelBlockProxy
        visitLabelBlockProxy(edge.getLabelBlock());
        final SimpleEFAEventDecl[] events = mEvents.toArray(
         new SimpleEFAEventDecl[mEvents.size()]);
        // creating a new label which contains an array of events and a condition
        final SimpleEFATransitionLabel label = new SimpleEFATransitionLabel(
         mSimplifyConstraint, events);
        mEdgeLabelMap.putByProxy(mEdge, label);
        mEventEncoding.createTransitionLabelId(label);
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
      try {
        checkAbortInVisitor();
        final SimpleEFAEventDecl edecl = findEventDecl(ident);
        // Adding which variables value is change / check by this event
        edecl.addAllPrimeVariable(mCurrentPrime);
        edecl.addAllUnPrimeVariable(mCurrentUnprime);
        mEvents.add(edecl);
        return null;
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
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
        mConstraintPropagator.init(list);
        mConstraintPropagator.propagate();
        // if it is not a contradiction (always false in the variables context)
        if (!mConstraintPropagator.isUnsatisfiable()) {
          mConstraintPropagator.removeUnchangedVariables();
          return mConstraintPropagator.getAllConstraints();
        }
        return null;
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
    private boolean containsMarkingProposition(
     final EventListExpressionProxy list)
    {
      final ModuleEqualityVisitor eq =
       ModuleEqualityVisitor.getInstance(false);
      return eq.contains(list.getEventIdentifierList(), mDefaultMarking);
    }

    // Constructing the transition relation
    private ListBufferTransitionRelation createTransitionRelation(
     final SimpleComponentProxy comp)
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
      final TObjectIntIterator<SimpleNodeProxy> iter = mStateMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final SimpleNodeProxy node = iter.key();
        final int code = iter.value();
        if (node.isInitial()) {
          rel.setInitial(code, true);
        }
        if (mUsesMarking && containsMarkingProposition(node.getPropositions())) {
          rel.setMarked(code, 0, true);
        }
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      for (final EdgeProxy edge : edges) {
        checkAbort();
        final SimpleEFATransitionLabel label = mEdgeLabelMap.getByProxy(edge);
        if (label == null) {
          continue;
        }
        final int labelId = mEventEncoding.getTransitionLabelId(label);

        final SimpleNodeProxy source = (SimpleNodeProxy) edge.getSource();
        final int sourceState = mStateMap.get(source);
        if (sourceState < 0) {
          continue;
        }
        final SimpleNodeProxy target = (SimpleNodeProxy) edge.getTarget();
        final int targetState = mStateMap.get(target);
        rel.addTransition(sourceState, labelId, targetState);
      }
      return rel;
    }

    private boolean simplifyTransitionRelation(
     final ListBufferTransitionRelation rel)
    {
      if (mIsOptimizationEnabled) {
        // 1. Check reachability
        final boolean hasUnreachableStates = checkReachability(rel);
        // 2. Check for unused events
        final boolean hasRemovableEvents = removeRedundantEvents(rel);

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
        final SimpleEFATransitionLabelEncoding newEncoding =
         new SimpleEFATransitionLabelEncoding(newNumEvents);
        for (int e = 0; e < oldNumEvents; e++) {
          if (usedEvents[e]) {
            final SimpleEFATransitionLabel label = mEventEncoding.
             getTransitionLabel(e);
            newEncoding.createTransitionLabelId(label);
          }
        }
        mEventEncoding = newEncoding;
      }
      return hasRemovableEvents;
    }

    // Creating a marking variable EFA. Note that, all variables' 
    // marking propositions will be removed. Furthermore, this introduces 
    // extra node/transition to the system hence the supervisor is larger than 
    // of that computed by marking propositions.
    private void createMarkingVariablesEFA()
     throws AnalysisAbortException, DuplicateIdentifierException
    {
      if (!mMarkedVariables.isEmpty()) {
        try {
          checkAbort();
          final SimpleEFATransitionLabelEncoding eventEncoding =
           new SimpleEFATransitionLabelEncoding(2);
          final ConstraintList markingUpdate =
           new ConstraintList(mVariableMarkingPredicates);

          final String eventName = "variable:markings";
          final SimpleIdentifierProxy eventIdent =
           mFactory.createSimpleIdentifierProxy(eventName);
          SimpleEFAEventDecl edecl = null;
          if (!hasIdentifier(eventIdent)) {
            final EventDeclProxy decl =
             mFactory.createEventDeclProxy(eventIdent, EventKind.CONTROLLABLE);
            edecl = new SimpleEFAEventDecl(decl);
            insertEventDecl(decl.getIdentifier(), edecl);
          } else {
            edecl = getEventDecl(eventIdent);
          }
          final SimpleEFATransitionLabel label = new SimpleEFATransitionLabel(
           markingUpdate, edecl);
          edecl.addAllUnPrimeVariable(mMarkedVariables);
          label.addVariables(mMarkedVariables);
          final int eventId = eventEncoding.createTransitionLabelId(label);
          final ListBufferTransitionRelation rel =
           new ListBufferTransitionRelation(eventName, ComponentKind.PLANT, 2,
           1, 2, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          rel.setInitial(0, true);
          rel.setMarked(1, 0, true);
          rel.addTransition(0, eventId, 1);
          final SimpleEFAComponent markingEFA =
           new SimpleEFAComponent("VariableMarkings", rel, eventEncoding,
           mMarkedVariables);
          mResultEFASystem.addComponent(markingEFA);
          for (final SimpleEFAVariable var : mMarkedVariables) {
            var.clearVariableMarkings();
          }
          markingEFA.register();
        } catch (final OverflowException exception) {
          throw new WatersRuntimeException(exception);
        }
      }
    }
  }
  
  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  //  private final SimpleEFAVariableFinder mEFAVariableFinder;
  private final ConstraintList mTrueGuard;
  private ProxyAccessorHashMap<IdentifierProxy, SimpleEFAEventDecl> mEFAEventDeclMap;
  private IdentifierProxy mDefaultMarking;
  private boolean mIsOptimizationEnabled;
  private boolean mIsMarkingVariablEFAEnable;
  private final ModuleProxy mInputModule;
  private final SimpleEFAVariableContext mVariableContext;
  private final List<SimpleEFAVariable> mMarkedVariables;
  private final List<SimpleExpressionProxy> mVariableMarkingPredicates;
  private final SimpleEFASystem mResultEFASystem;
  private boolean mIsAborting;
  private final Collection<DefaultModuleProxyVisitor> mUserPass;
  
}
