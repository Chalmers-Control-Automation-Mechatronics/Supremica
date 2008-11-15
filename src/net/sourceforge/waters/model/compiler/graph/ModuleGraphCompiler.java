//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module.graph
//# CLASS:   ModuleGraphCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.context.
  DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ModuleGraphCompiler extends AbstractModuleProxyVisitor
{
 
  //##########################################################################
  //# Constructors
  public ModuleGraphCompiler(final ProductDESProxyFactory factory,
                             final SourceInfoBuilder builder,
                             final ModuleProxy module)
  {
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mInputModule = module;
  }


  //##########################################################################
  //# Invocation
  public ProductDESProxy compile()
    throws EvalException
  {
    try {
      return visitModuleProxy(mInputModule);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw new WatersRuntimeException(cause);
      }
    }
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Object visitEdgeProxy(final EdgeProxy edge)
  {
    // Edges must be processed in the proper order: 
    // innermost source nodes in group node hierarchy first.
    // Therefore, this following two-pass process ...
    final NodeProxy source = edge.getSource();
    final CompiledNode csource = mPrecompiledNodesMap.get(source);
    csource.addEdge(edge);
    return null;
  }

  private void visitEdgeProxy(final EdgeProxy edge, final CompiledNode source)
    throws VisitorException
  {
    try {
      final NodeProxy target = edge.getTarget();
      final LabelBlockProxy block = edge.getLabelBlock();
      mCurrentSource = source;
      mCurrentTarget = mPrecompiledNodesMap.get(target);
      visitLabelBlockProxy(block);
    } finally {
      mCurrentSource = null;
      mCurrentTarget = null;
    }
  }

  public EventProxy visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    final IdentifierProxy ident = decl.getIdentifier();
    final String name = ident.toString();
    final EventKind kind = decl.getKind();
    final boolean observable = decl.isObservable();
    final EventProxy event = mFactory.createEventProxy(name, kind, observable);
    addGlobalEvent(ident, event);
    mSourceInfoBuilder.add(event, decl);
    return event;
  }

  public Object visitEventListExpressionProxy
    (final EventListExpressionProxy elist)
    throws VisitorException
  {
    final List<Proxy> list = elist.getEventList();
    visitCollection(list);
    return null;
  }

  public CompiledNode visitGroupNodeProxy(final GroupNodeProxy group)
    throws VisitorException
  {
    try {
      final CompiledGroupNode cgroup = new CompiledGroupNode(group);
      mPrecompiledNodesMap.put(group, cgroup);
      for (final NodeProxy child : group.getImmediateChildNodes()) {
        final CompiledNode cchild = mPrecompiledNodesMap.get(child);
        cgroup.addImmediateChildNode(cchild);
      }
      final PlainEventListProxy elist = group.getPropositions();
      mCurrentSource = cgroup;
      visitPlainEventListProxy(elist);
      return mCurrentSource;
    } finally {
      mCurrentSource = null;
    }
  }

  public EventProxy visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    try {
      final EventProxy event = findGlobalEvent(ident);
      addLocalEvent(event);
      if (mCurrentSource == null) {
        // nothing --- blocked events list
      } else if (mCurrentTarget == null) {
        // propositions of a state
        mCurrentSource.addProposition(event);
      } else {
        // label block of an edge
        mCurrentTarget.addTransitionFrom(mCurrentSource,
                                         event,
                                         mCurrentSource,
                                         ident);
      }
      return event;
    } catch (final NondeterministicModuleException exception) {
      throw wrap(exception);
    } 
  }

  public ProductDESProxy visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    try {
      final String name = mInputModule.getName();
      final String comment = mInputModule.getComment();
      final List<EventDeclProxy> decls = mInputModule.getEventDeclList();
      final int numevents = decls.size();
      mGlobalEventsMap =
        new HashMap<ProxyAccessor<IdentifierProxy>,EventProxy>(numevents);
      mGlobalEventsList = new ArrayList<EventProxy>(numevents);
      visitCollection(decls);
      final List<Proxy> components = mInputModule.getComponentList();
      final int numaut = components.size();
      mAutomataMap =
        new HashMap<ProxyAccessor<IdentifierProxy>,AutomatonProxy>(numaut);
      mAutomataList = new ArrayList<AutomatonProxy>(numaut);
      visitCollection(components);
      return mFactory.createProductDESProxy
        (name, comment, null, mGlobalEventsList, mAutomataList);
    } finally {
      mGlobalEventsMap = null;
      mGlobalEventsList = null;
      mAutomataMap = null;
      mAutomataList = null;
    }
  }

  public AutomatonProxy visitSimpleComponentProxy
    (final SimpleComponentProxy comp)
    throws VisitorException
  {
    try {
      mCurrentComponent = comp;
      // Prepare alphabet ...
      mLocalEventsMap = new HashMap<EventProxy,SelfloopInfo>();
      mLocalEventsList = new LinkedList<EventProxy>();
      final GraphProxy graph = comp.getGraph();
      final EventListExpressionProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitEventListExpressionProxy(blocked);
      }
      // Precompile states ...
      mCurrentComponentIsDetermistic = graph.isDeterministic();
      mMaxInitialStates = mCurrentComponentIsDetermistic ? 1 : -1;
      final Collection<NodeProxy> nodes = graph.getNodes();
      final int numnodes = nodes.size();
      mPrecompiledNodesMap = new HashMap<NodeProxy,CompiledNode>(numnodes);
      mLocalStateNames = new HashSet<String>(numnodes);
      mLocalTransitionsList = new LinkedList<CompiledTransition>();
      visitCollection(nodes);
      // Precompile transitions ...
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      for (final NodeProxy node : nodes) {
        final CompiledNode cnode = mPrecompiledNodesMap.get(node);
        for (final EdgeProxy edge : cnode.getEdges()) {
          visitEdgeProxy(edge, cnode);
        }
      }
      // Reachability ...
      mNumReachableStates = 0;
      mNumReachableTransitions = 0;
      for (final CompiledNode node : mPrecompiledNodesMap.values()) {
        if (node.isInitial()) {
          node.explore();
        }
      }
      // Build alphabet, states, and transitions ...
      removeSelfloopedEvents(mLocalEventsList);
      if (mLocalEventsList.isEmpty()) {
        return null;
      }
      final List<StateProxy> states =
        new ArrayList<StateProxy>(mNumReachableStates);
      for (final NodeProxy node : nodes) {
        final CompiledNode cnode = mPrecompiledNodesMap.get(node);
        final StateProxy state = cnode.createStateProxy();
        if (state != null) {
          states.add(state);
        }
      }
      final List<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>(mNumReachableTransitions);
      for (final CompiledTransition ctrans : mLocalTransitionsList) {
        final TransitionProxy trans = ctrans.createTransitionProxy();
        if (trans != null) {
          transitions.add(trans);
        }
      }
      // Create automaton ...
      final IdentifierProxy ident = comp.getIdentifier();
      final String name = ident.toString();
      final ComponentKind kind = comp.getKind();
      final AutomatonProxy aut =
        mFactory.createAutomatonProxy(name, kind, mLocalEventsList,
                                      states, transitions);
      addAutomaton(ident, aut);
      mSourceInfoBuilder.add(aut, comp);
      return aut;
    } finally {
      mCurrentComponent = null;
      mLocalEventsMap = null;
      mLocalEventsList = null;
      mPrecompiledNodesMap = null;
      mLocalStateNames = null;
      mLocalTransitionsList = null;
      mMaxInitialStates = 0;
      mNumReachableStates = 0;
      mNumReachableTransitions = 0;
    }
  }

  public CompiledNode visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    try {
      final String name = node.getName();
      if (!mLocalStateNames.add(name)) {
        throw new DuplicateIdentifierException(name, "state", node);
      }
      if (node.isInitial()) {
        switch (mMaxInitialStates) {
        case 1:
          mMaxInitialStates = 0;
          break;
        case 0:
          throw new NondeterministicModuleException(mCurrentComponent, node);
        default:
          break;
        }
      }
      mCurrentSource = new CompiledSimpleNode(node);
      mPrecompiledNodesMap.put(node, mCurrentSource);
      final PlainEventListProxy elist = node.getPropositions();
      visitPlainEventListProxy(elist);
      return mCurrentSource;
    } catch (final EvalException exception) {
      throw wrap(exception);
    } finally {
      mCurrentSource = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void addGlobalEvent(final IdentifierProxy ident,
                              final EventProxy event)
    throws VisitorException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    if (mGlobalEventsMap.containsKey(accessor)) {
      final DuplicateIdentifierException exception =
        new DuplicateIdentifierException(ident, "event");
      throw wrap(exception);
    } else {
      mGlobalEventsMap.put(accessor, event);
      mGlobalEventsList.add(event);
    }
  }

  private EventProxy findGlobalEvent(final IdentifierProxy ident)
    throws VisitorException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final EventProxy event = mGlobalEventsMap.get(accessor);
    if (event != null) {
      return event;
    } else {
      final UndefinedIdentifierException exception =
        new UndefinedIdentifierException(ident, "event");
      throw wrap(exception);
    }
  }

  private void addAutomaton(final IdentifierProxy ident,
                            final AutomatonProxy aut)
    throws VisitorException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    if (mAutomataMap.containsKey(accessor)) {
      final DuplicateIdentifierException exception =
        new DuplicateIdentifierException(ident, "automaton");
      throw wrap(exception);
    } else {
      mAutomataMap.put(accessor, aut);
      mAutomataList.add(aut);
    }
  }

  private void addLocalEvent(final EventProxy event)
  {
    if (!mLocalEventsMap.containsKey(event)) {
      final SelfloopInfo info = new SelfloopInfo();
      mLocalEventsMap.put(event, info);
      mLocalEventsList.add(event);
    }
  }

  private SelfloopInfo getSelfloopInfo(final EventProxy event)
  {
    return mLocalEventsMap.get(event);
  }

  private void removeSelfloopedEvents(final List<EventProxy> list)
  {
    if (list != null) {
      final Iterator<EventProxy> iter = list.iterator();
      while (iter.hasNext()) {
        final EventProxy event = iter.next();
        final SelfloopInfo info = getSelfloopInfo(event);
        if (info.isAllSelfloops()) {
          iter.remove();
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class CompiledNode
  private abstract class CompiledNode {

    //#######################################################################
    //# Constructors
    CompiledNode(final NodeProxy node)
    {
      mNode = node;
      mEdges = new LinkedList<EdgeProxy>();
    }

    //#######################################################################
    //# Simple Access
    NodeProxy getNode()
    {
      return mNode;
    }

    void addEdge(final EdgeProxy edge)
    {
      mEdges.add(edge);
    }

    List<EdgeProxy> getEdges()
    {
      return mEdges;
    }

    abstract boolean isInitial();

    abstract boolean isReachable();

    //#######################################################################
    //# Algorithms
    abstract void addProposition(EventProxy event);

    abstract void addTransitionFrom(CompiledNode source,
                                    EventProxy event,
                                    CompiledNode cause,
                                    Proxy orig)
      throws NondeterministicModuleException;

    abstract void addTransitionTo(CompiledSimpleNode target,
                                  EventProxy event,
                                  CompiledNode cause,
                                  Proxy orig)
      throws NondeterministicModuleException;

    abstract boolean hasProperChildNode(CompiledNode node);

    abstract void explore();

    abstract StateProxy createStateProxy();

    //#######################################################################
    //# Data Members
    private final NodeProxy mNode;
    private final List<EdgeProxy> mEdges;

  }


  //#########################################################################
  //# Inner Class CompiledSimpleNode
  private class CompiledSimpleNode extends CompiledNode {

    //#######################################################################
    //# Constructors
    CompiledSimpleNode(final SimpleNodeProxy node)
    {
      super(node);
      mPropositions = null;
      mOutgoingTransitions =
        new HashMap<EventProxy,List<CompiledTransition>>();
      mState = null;
    }

    //#######################################################################
    //# Simple Access
    SimpleNodeProxy getNode()
    {
      return (SimpleNodeProxy) super.getNode();
    }

    boolean isInitial()
    {
      return getNode().isInitial();
    }

    boolean isReachable()
    {
      return mIsReachable;
    }

    //#######################################################################
    //# Specific Access
    StateProxy getState()
    {
      return mState;
    }

    //#######################################################################
    //# Algorithms
    void addProposition(final EventProxy event)
    {
      if (mPropositions == null) {
        mPropositions = new LinkedList<EventProxy>();
      }
      if (!mPropositions.contains(event)) {
        mPropositions.add(event);
      }
    }

    void addTransitionFrom(final CompiledNode source,
                           final EventProxy event,
                           final CompiledNode cause,
                           final Proxy loc)
      throws NondeterministicModuleException
    {
      source.addTransitionTo(this, event, cause, loc);
    }

    void addTransitionTo(final CompiledSimpleNode target,
                         final EventProxy event,
                         final CompiledNode cause,
                         final Proxy loc)
      throws NondeterministicModuleException
    {
      List<CompiledTransition> transitions = mOutgoingTransitions.get(event);
      if (transitions == null) {
        transitions = new LinkedList<CompiledTransition>();
        mOutgoingTransitions.put(event, transitions);
      } else {
        for (final CompiledTransition trans : transitions) {
          if (trans.getTarget() == target) {
            return;
          } else if (mCurrentComponentIsDetermistic) {
            final CompiledNode othercause = trans.getCause();
            if (cause.hasProperChildNode(othercause)) {
              return;
            } else {
              throw new NondeterministicModuleException
                (mCurrentComponent, getNode(), event);
            }
          }
        }
      }
      final CompiledTransition trans =
        new CompiledTransition(this, event, target, cause, loc);
      transitions.add(trans);
      mLocalTransitionsList.add(trans);
    }

    boolean hasProperChildNode(final CompiledNode node)
    {
      return false;
    }
    
    void explore()
    {
      if (!mIsReachable) {
        mIsReachable = true;
        mNumReachableStates++;
        if (mPropositions != null) {
          for (final EventProxy prop : mPropositions) {
            final SelfloopInfo info = getSelfloopInfo(prop);
            info.addSelfloop();
          }
        }
        final Collection<List<CompiledTransition>> outgoing =
          mOutgoingTransitions.values();
        for (final List<CompiledTransition> list : outgoing) {
          for (final CompiledTransition trans : list) {
            final CompiledSimpleNode target = trans.getTarget();
            target.explore();
            trans.explore();
          }
        }
      }
    }

    //#######################################################################
    //# Specific Algorithms
    StateProxy createStateProxy()
    {
      if (mState == null && mIsReachable) {
        final SimpleNodeProxy node = getNode();
        final String name = node.getName();
        final boolean initial = node.isInitial();
        removeSelfloopedEvents(mPropositions);
        mState = mFactory.createStateProxy(name, initial, mPropositions);
        mSourceInfoBuilder.add(mState, node);
      }
      return mState;
    }

    //#######################################################################
    //# Data Members
    private List<EventProxy> mPropositions;
    private Map<EventProxy,List<CompiledTransition>> mOutgoingTransitions;
    private boolean mIsReachable;
    private StateProxy mState;

  }


  //#########################################################################
  //# Inner Class CompiledGroupNode
  private class CompiledGroupNode extends CompiledNode {

    //#######################################################################
    //# Constructors
    CompiledGroupNode(final GroupNodeProxy group)
    {
      super(group);
      final int numchildren = group.getImmediateChildNodes().size();
      mImmediateChildNodes = new ArrayList<CompiledNode>(numchildren);
    }

    //#######################################################################
    //# Simple Access
    GroupNodeProxy getNode()
    {
      return (GroupNodeProxy) super.getNode();
    }

    boolean isInitial()
    {
      return false;
    }

    boolean isReachable()
    {
      return false;
    }

    //#######################################################################
    //# Specific Access
    void addImmediateChildNode(final CompiledNode child)
    {
      mImmediateChildNodes.add(child);
    }

    //#######################################################################
    //# Algorithms
    void addProposition(final EventProxy event)
    {
      for (final CompiledNode child : mImmediateChildNodes) {
        child.addProposition(event);
      }
    }

    void addTransitionFrom(final CompiledNode source,
                           final EventProxy event,
                           final CompiledNode cause,
                           final Proxy loc)
      throws NondeterministicModuleException
    {
      for (final CompiledNode child : mImmediateChildNodes) {
        child.addTransitionFrom(source, event, cause, loc);
      }
    }

    void addTransitionTo(final CompiledSimpleNode target,
                         final EventProxy event,
                         final CompiledNode cause,
                         final Proxy loc)
      throws NondeterministicModuleException
    {
      for (final CompiledNode child : mImmediateChildNodes) {
        child.addTransitionTo(target, event, cause, loc);
      }
    }

    boolean hasProperChildNode(final CompiledNode node)
    {
      for (final CompiledNode child : mImmediateChildNodes) {
        if (child == node || child.hasProperChildNode(node)) {
          return true;
        }
      }
      return false;
    }

    void explore()
    {
    }

    StateProxy createStateProxy()
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private final List<CompiledNode> mImmediateChildNodes;

  }


  //#########################################################################
  //# Inner Class CompiledTransition
  private class CompiledTransition {

    //#######################################################################
    //# Constructor
    CompiledTransition(final CompiledSimpleNode source,
                       final EventProxy event,
                       final CompiledSimpleNode target,
                       final CompiledNode cause,
                       final Proxy loc)
    {
      mSource = source;
      mEvent = event;
      mTarget = target;
      mCause = cause;
      mLocation = loc;
    }

    //#######################################################################
    //# Simple Access
    CompiledSimpleNode getSource()
    {
      return mSource;
    }

    EventProxy getEvent()
    {
      return mEvent;
    }

    CompiledSimpleNode getTarget()
    {
      return mTarget;
    }

    CompiledNode getCause()
    {
      return mCause;
    }

    Proxy getLocation() 
    {
      return mLocation;
    }

    //#######################################################################
    //# Algorithms
    void explore()
    {
      final SelfloopInfo info = getSelfloopInfo(mEvent);
      info.addTransition(mSource == mTarget);
      mNumReachableTransitions++;
    }

    TransitionProxy createTransitionProxy()
    {
      final SelfloopInfo info = getSelfloopInfo(mEvent);
      if (info.isAllSelfloops() || !mSource.isReachable()) {
        return null;
      } else {
        final StateProxy sourcestate = mSource.getState();
        final StateProxy targetstate = mTarget.getState();
        final TransitionProxy trans =
          mFactory.createTransitionProxy(sourcestate, mEvent, targetstate);
        mSourceInfoBuilder.add(trans, mLocation);
        return trans;
      }
    }

    //#######################################################################
    //# Data Members
    private final CompiledSimpleNode mSource;
    private final EventProxy mEvent;
    private final CompiledSimpleNode mTarget;
    private final CompiledNode mCause;
    private final Proxy mLocation;

  }


  //#########################################################################
  //# Inner Class SelfloopInfo
  private class SelfloopInfo {

    //#######################################################################
    //# Constructor
    private SelfloopInfo()
    {
      mNumSelfloops = 0;
    }

    //#######################################################################
    //# Simple Access
    void addSelfloop()
    {
      addTransition(true);
    }

    void addTransition(final boolean selfloop)
    {
      if (mNumSelfloops >= 0) {
        if (selfloop) {
          mNumSelfloops++;
        } else {
          mNumSelfloops = -1;
        }
      }
    }

    boolean isAllSelfloops()
    {
      return mNumSelfloops == mNumReachableStates;
    }

    //#######################################################################
    //# Data Members
    private int mNumSelfloops;

  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final ModuleProxy mInputModule;

  private Map<ProxyAccessor<IdentifierProxy>,EventProxy> mGlobalEventsMap;
  private List<EventProxy> mGlobalEventsList;
  private Map<ProxyAccessor<IdentifierProxy>,AutomatonProxy> mAutomataMap;
  private List<AutomatonProxy> mAutomataList;

  private SimpleComponentProxy mCurrentComponent;
  private boolean mCurrentComponentIsDetermistic;
  private Map<EventProxy,SelfloopInfo> mLocalEventsMap;
  private List<EventProxy> mLocalEventsList;
  private Map<NodeProxy,CompiledNode> mPrecompiledNodesMap;
  private Set<String> mLocalStateNames;
  private List<CompiledTransition> mLocalTransitionsList;
  private int mMaxInitialStates;
  private int mNumReachableStates;
  private int mNumReachableTransitions;

  private CompiledNode mCurrentSource;
  private CompiledNode mCurrentTarget;

}
