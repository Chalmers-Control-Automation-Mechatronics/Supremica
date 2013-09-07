//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFASystemBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.efa.efsm.EFSMVariable;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


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
  public UnifiedEFASystemBuilder
    (final ModuleProxyFactory factory,
     final SourceInfoBuilder builder,
     final ModuleProxy module,
     final ProxyAccessorMap<IdentifierProxy,ConstraintList> map)
  {
    mFactory = factory;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSourceInfoBuilder = builder;
    mInputModule = module;
    mEventUpdateMap = map;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    final int numEvents = mInputModule.getEventDeclList().size();
    mIdentifierMap = new ProxyAccessorHashMap<>(eq,numEvents);
    final String moduleName = mInputModule.getName();
    final CompilerOperatorTable opTable = CompilerOperatorTable.getInstance();
    mVariableContext = new UnifiedEFAVariableContext(mInputModule, opTable);
    final int numComponents = mInputModule.getComponentList().size();
    mResultEFASystem =
      new UnifiedEFASystem(moduleName, mVariableContext, numComponents);
  }

  public UnifiedEFASystem compile()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      // Pass 1 ...
      createEvents();
      // Pass 2 ...
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      return mResultEFASystem;
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

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSimpleExpressionCompiler = null;
    mIdentifierMap = null;
    mVariableContext = null;
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
  //# Pass 1
  private void createEvents()
  {
    final List<EventDeclProxy> eventDecls = mInputModule.getEventDeclList();
    for (final EventDeclProxy eventDec : eventDecls) {
      final IdentifierProxy ident = eventDec.getIdentifier();
      final ConstraintList update = mEventUpdateMap.getByProxy(ident);
      if (update != null) {
        final UnifiedEFAEvent event = new UnifiedEFAEvent(eventDec, update);
        mResultEFASystem.addEvent(event);
        mIdentifierMap.putByProxy(ident, event);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean containsMarkingProposition
    (final EventListExpressionProxy list)
  {
    final ModuleEqualityVisitor eq =
      ModuleEqualityVisitor.getInstance(false);
    return eq.contains(list.getEventIdentifierList(), mDefaultMarking);
  }


  //#########################################################################
  //# Inner Class EventCollectVisitor
  private class EventCollectVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private UnifiedEFAEventEncoding collectEvents(final SimpleComponentProxy comp)
    {
      try {
        mCollectedEvents = new UnifiedEFAEventEncoding();
        mFoundDefaultMarking = false;
        visitSimpleComponentProxy(comp);
        return mCollectedEvents;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mCollectedEvents = null;
      }
    }

    //#######################################################################
    //# Simple Access
    private boolean getFoundDefaultMarking()
    {
      return mFoundDefaultMarking;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge) throws VisitorException
    {
      final LabelBlockProxy labelBlock = edge.getLabelBlock();
      visitLabelBlockProxy(labelBlock);
      return null;
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph) throws VisitorException
    {
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final LabelBlockProxy blockedEvents = graph.getBlockedEvents();
      if (blockedEvents != null) {
        visitLabelBlockProxy(blockedEvents);
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      final UnifiedEFAEvent event = mIdentifierMap.getByProxy(ident);
      if (event != null) {
        mCollectedEvents.createEventId(event);
      } else {
        final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
        if (eq.equals(ident, mDefaultMarking)) {
          mFoundDefaultMarking = true;
        }
      }
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy labelBlock)
      throws VisitorException
    {
      final List<Proxy> idents = labelBlock.getEventIdentifierList();
      visitCollection(idents);
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      final GraphProxy graph = comp.getGraph();
      visitGraphProxy(graph);
      return null;
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mFoundDefaultMarking = true;
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private UnifiedEFAEventEncoding mCollectedEvents;
    private boolean mFoundDefaultMarking;
  }


  //#########################################################################
  //# Inner Class Pass2Visitor
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass2Visitor()
    {
      mEventCollectVisitor = new EventCollectVisitor();
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
    public UnifiedEFATransitionRelation visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        mEventEncoding = mEventCollectVisitor.collectEvents(comp);
        mFoundDefaultMarking = mEventCollectVisitor.getFoundDefaultMarking();
        final GraphProxy graph = comp.getGraph();
        final String name = comp.getName();
        final ComponentKind kind = comp.getKind();
        int numProperEvents = mEventEncoding.size();
        int numPropositions = mFoundDefaultMarking ? 1 : 0;
        int numStates = graph.getNodes().size();
        final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
        mTransitionRelation = new ListBufferTransitionRelation
          (name, kind, numProperEvents, numPropositions, numStates, config);
        mRevisiting = false;
        visitGraphProxy(graph);
        if (simplifyTransitionRelation(mTransitionRelation)) {
          if (mEventEncoding.size() <= 1 && !mFoundDefaultMarking) {
            return null;
          }
          numProperEvents = mEventEncoding.size();
          numPropositions = mFoundDefaultMarking ? 1 : 0;
          numStates = mStateMap.size();
          mTransitionRelation = new ListBufferTransitionRelation
            (name, kind, numProperEvents, numPropositions, numStates, config);
          mRevisiting = true;
          visitGraphProxy(graph);
        }
        mTransitionRelation.setProperEventStatus(EventEncoding.TAU,
                                                 EventEncoding.STATUS_FULLY_LOCAL
                                                 | EventEncoding.STATUS_UNUSED);
        if (isTrivial(mTransitionRelation)) {
          return null;
        }
        final UnifiedEFATransitionRelation unifiedEFATransitionRelation =
          new UnifiedEFATransitionRelation(mTransitionRelation,
                                           mEventEncoding, mNodeList);
        mResultEFASystem.addTransitionRelation(unifiedEFATransitionRelation);
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
      final Collection<NodeProxy> nodes = graph.getNodes();
      if (!mRevisiting) {
        mStateMap =
          new TObjectIntHashMap<SimpleNodeProxy>(nodes.size(), 0.5f, -1);
        if (mSourceInfoBuilder != null) {
          mNodeList = new ArrayList<SimpleNodeProxy>(nodes.size());
        } else {
          mNodeList = null;
        }
      }
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      return visitCollection(edges);
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final int code;
      if (mRevisiting) {
        code = mStateMap.get(node);
        if (code < 0) {
          return null;
        }
      } else {
        code = mStateMap.size();
        mStateMap.put(node, code);
        if (mNodeList != null) {
          mNodeList.add(node);
        }
      }
      if (node.isInitial()) {
        mTransitionRelation.setInitial(code, true);
      }
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mTransitionRelation.setMarked(code, UnifiedEFAEventEncoding.OMEGA, true);
      }
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      checkAbortInVisitor();
      final NodeProxy source = edge.getSource();
      mSource = mStateMap.get(source);
      if (mSource < 0) {
        return null;
      }
      final NodeProxy target = edge.getTarget();
      mTarget = mStateMap.get(target);
      if (mTarget < 0) {
        return null;
      }
      final LabelBlockProxy labelBlock = edge.getLabelBlock();
      visitLabelBlockProxy(labelBlock);
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy labelBlock)
      throws VisitorException
    {
      final List<Proxy> idents = labelBlock.getEventIdentifierList();
      visitCollection(idents);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      final UnifiedEFAEvent event = mIdentifierMap.getByProxy(ident);
      final int eventCode = mEventEncoding.getEventId(event);
      if (eventCode < 0) {
        return null;
      }
      mTransitionRelation.addTransition(mSource, eventCode, mTarget);
      return null;
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy type = var.getType();
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(type, mVariableContext);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        final UnifiedEFAVariable result =
          new UnifiedEFAVariable(var, range, mDefaultMarking, mFactory, mOperatorTable);
        mVariableContext.addVariable(result);
        mResultEFASystem.addVariable(result);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean simplifyTransitionRelation
      (final ListBufferTransitionRelation rel)
    {
      if (mIsOptimizationEnabled) {
        // 1. Check reachability
        final boolean hasUnreachableStates = checkReachability(rel);
        // 2. Check for unused events or selfloops
        final boolean hasRemovableEvents = removeRedundantEvents(rel);
        // 3. Check for redundant propositions
        final boolean hasRemovablePropositions = rel.removeRedundantPropositions();
        if (hasRemovablePropositions) {
          mFoundDefaultMarking = rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA);
        }
        return hasUnreachableStates || hasRemovableEvents || hasRemovablePropositions;
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
        } else {
          newNumEvents++;
        }
      }
      if (hasRemovableEvents) {
        final UnifiedEFAEventEncoding newEncoding =
          new UnifiedEFAEventEncoding(newNumEvents);
        for (int e = EventEncoding.NONTAU; e < oldNumEvents; e++) {
          if (usedEvents[e]) {
            final UnifiedEFAEvent update = mEventEncoding.getUpdate(e);
            newEncoding.createEventId(update);
          }
        }
        mEventEncoding = newEncoding;
      }
      return hasRemovableEvents;
    }

    private boolean isTrivial(final ListBufferTransitionRelation rel)
    {
      for (int e = EventEncoding.TAU; e < rel.getNumberOfProperEvents(); e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventEncoding.isUsedEvent(status)) {
          return false;
        }
      }
      for (int p = 0; p < rel.getNumberOfPropositions(); p++) {
        if (rel.isUsedProposition(p)) {
          return false;
        }
      }
      for (int s = 0; s < rel.getNumberOfStates(); s++) {
        if (rel.isInitial(s)){
          return true;
        }
      }
        return false;
    }

    //#######################################################################
    //# Data Members
    private final EventCollectVisitor mEventCollectVisitor;
    private UnifiedEFAEventEncoding mEventEncoding;
    private ListBufferTransitionRelation mTransitionRelation;
    private TObjectIntHashMap<SimpleNodeProxy> mStateMap;
    private List<SimpleNodeProxy> mNodeList;
    private boolean mFoundDefaultMarking;
    private int mSource;
    private int mTarget;
    private boolean mRevisiting;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SourceInfoBuilder mSourceInfoBuilder;

  private boolean mIsOptimizationEnabled;
  private IdentifierProxy mDefaultMarking;

  private final ModuleProxy mInputModule;
  private SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ProxyAccessorMap<IdentifierProxy,ConstraintList> mEventUpdateMap;
  private ProxyAccessorMap<IdentifierProxy, UnifiedEFAEvent> mIdentifierMap;
  private UnifiedEFAVariableContext mVariableContext;
  private UnifiedEFASystem mResultEFASystem;

}
