//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFASystemBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * <P>
 * A compiler to convert an instantiated module ({@link ModuleProxy}) into and
 * EFSM system ({@link UnifiedEFASystem}).
 * </P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFASystemBuilder extends AbstractEFAAlgorithm
{

  //#########################################################################
  //# Constructors
  public UnifiedEFASystemBuilder(final ModuleProxyFactory factory,
                           final SourceInfoBuilder builder,
                           final ModuleProxy module,
                           final ProxyAccessorMap<IdentifierProxy, ConstraintList> map)
  {
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mInputModule = module;
    mEventUpdateMap = map;
    mEvents = new ArrayList<>(module.getEventDeclList().size());
    final String moduleName = module.getName();
    final CompilerOperatorTable opTable = CompilerOperatorTable.getInstance();
    mVariableContext = new UnifiedEFAVariableContext(module, opTable);
    final int size = module.getComponentList().size();
    mResultEFAMSystem = new UnifiedEFASystem(moduleName, mVariableContext, size);
  }


  //#########################################################################
  //# Invocation
  public UnifiedEFASystem compile()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      return mResultEFAMSystem;
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
  @SuppressWarnings("unused")
  private void createEvents()
  {
    final List<EventDeclProxy> eventDecls = mInputModule.getEventDeclList();
    for (final EventDeclProxy eventDec : eventDecls) {
      final IdentifierProxy ident = eventDec.getIdentifier();
      final ConstraintList update = mEventUpdateMap.getByProxy(ident);
      final UnifiedEFAEvent event = new UnifiedEFAEvent(eventDec, update);
      mEvents.add(event);
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
    }
    /*
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
    public UnifiedEFATransitionRelation visitSimpleComponentProxy
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
        final UnifiedEFATransitionRelation unifiedEFATransitionRelation =
          new UnifiedEFATransitionRelation(rel, mEventEncoding, variables, mNodeList);
        unifiedEFATransitionRelation.register();
        mResultEFAMSystem.addTransitionRelation(unifiedEFATransitionRelation);
        return unifiedEFATransitionRelation;
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
      mStateMap =
        new TObjectIntHashMap<SimpleNodeProxy>(nodes.size(), 0.5f, -1);
      if (mSourceInfoBuilder != null) {
        mNodeList = new ArrayList<SimpleNodeProxy>(nodes.size());
      } else {
        mNodeList = null;
      }
      visitCollection(nodes);
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mSimplifiedGuardActionBlockMap =
        new ProxyAccessorHashMap<GuardActionBlockProxy,ConstraintList>
          (eq, edges.size());
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
        mSimplifiedGuardActionBlockMap.putByProxy(update, mTrueGuard);
        mEventEncoding.createEventId(mTrueGuard);
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
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
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
          mUsesMarking = rel.isUsedProposition(0);
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
          final int numVars = mResultEFAMSystem.getVariables().size();
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
    */
  }

  //#########################################################################
  //# Data Members
  @SuppressWarnings("unused")
  private final ModuleProxyFactory mFactory;
  @SuppressWarnings("unused")
  private final SourceInfoBuilder mSourceInfoBuilder;

  private boolean mIsOptimizationEnabled;
  private IdentifierProxy mDefaultMarking;

  private final ModuleProxy mInputModule;
  private final ProxyAccessorMap<IdentifierProxy, ConstraintList> mEventUpdateMap;
  private final List<UnifiedEFAEvent> mEvents;
  private final UnifiedEFAVariableContext mVariableContext;
  private final UnifiedEFASystem mResultEFAMSystem;

}
