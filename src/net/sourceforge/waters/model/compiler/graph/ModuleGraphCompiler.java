//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module.graph
//# CLASS:   ModuleGraphCompiler
//###########################################################################
//# $Id: ModuleGraphCompiler.java,v 1.1 2008-06-19 11:34:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
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
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ModuleGraphCompiler extends AbstractModuleProxyVisitor
{
 
  //##########################################################################
  //# Constructors
  public ModuleGraphCompiler(final ProductDESProxyFactory factory,
                             final ModuleProxy module)
  {
    mFactory = factory;
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
    throws VisitorException
  {
    final NodeProxy source = edge.getSource();
    final CompiledNode entry = mPrecompiledNodesMap.get(source);
    entry.addEdge(edge);
    return null;
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
    return event;
  }

  public List<EventProxy> visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final List<Proxy> list = proxy.getEventList();
      final int size = list.size();
      mCurrentEventsList = new ArrayList<EventProxy>(size);
      visitCollection(list);
      return mCurrentEventsList;
    } finally {
      mCurrentEventsList = null;
    }
  }

  public CompiledNode visitGroupNodeProxy(final GroupNodeProxy proxy)
    throws VisitorException
  {
    final CompiledNode compiled = new CompiledNode(proxy);
    mPrecompiledNodesMap.put(proxy, compiled);
    return compiled;
  }

  public EventProxy visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    final EventProxy event = findGlobalEvent(ident);
    // Check type? Again?
    addLocalEvent(event);
    mCurrentEventsList.add(event);
    return event;
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
      mLocalEventsSet = new HashSet<EventProxy>();
      mLocalEventsList = new LinkedList<EventProxy>();
      final GraphProxy graph = comp.getGraph();
      final EventListExpressionProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitEventListExpressionProxy(blocked);
      }
      // Precompile states ...
      final boolean deterministic = graph.isDeterministic();
      mMaxInitialStates = deterministic ? 1 : -1;
      final Collection<NodeProxy> nodes = graph.getNodes();
      final int numnodes = nodes.size();
      mPrecompiledNodesMap = new HashMap<NodeProxy,CompiledNode>(numnodes);
      mLocalStatesList = new ArrayList<StateProxy>(numnodes);
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      // Build transitions ...
      mLocalTransitionsList = new LinkedList<TransitionProxy>();
      for (final NodeProxy source : nodes) {
        final CompiledNode sourceEntry = mPrecompiledNodesMap.get(source);
        for (final EdgeProxy edge : sourceEntry.getEdges()) {
          final NodeProxy target = edge.getTarget();
          final EventListExpressionProxy labels = edge.getLabelBlock();
          final List<EventProxy> events =
            visitEventListExpressionProxy(labels);
          createTransitions(source, events, target, sourceEntry,
                            deterministic);
        }
        sourceEntry.clearProperChildNodes();
      }
      // Create automaton ...
      final IdentifierProxy ident = comp.getIdentifier();
      final String name = ident.toString();
      final ComponentKind kind = comp.getKind();
      final AutomatonProxy aut =
        mFactory.createAutomatonProxy(name, kind, mLocalEventsList,
                                      mLocalStatesList, mLocalTransitionsList);
      addAutomaton(ident, aut);
      return aut;
    } finally {
      mCurrentComponent = null;
      mPrecompiledNodesMap = null;
      mLocalStatesList = null;
      mLocalEventsSet = null;
      mLocalEventsList = null;
      mLocalTransitionsList = null;
    }
  }

  public CompiledNode visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    final String name = node.getName();
    final boolean initial = node.isInitial();
    final EventListExpressionProxy nodeprops = node.getPropositions();
    final List<EventProxy> stateprops =
      visitEventListExpressionProxy(nodeprops);
    if (initial) {
      switch (mMaxInitialStates) {
      case 1:
        mMaxInitialStates = 0;
        break;
      case 0:
        final NondeterministicModuleException exception =
          new NondeterministicModuleException(mCurrentComponent, node);
        throw wrap(exception);
      default:
        break;
      }
    }
    final StateProxy state =
      mFactory.createStateProxy(name, initial, stateprops);
    return addLocalState(state, node);
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
    if (!mLocalEventsSet.contains(event)) {
      mLocalEventsList.add(event);
    }
  }

  private CompiledNode addLocalState(final StateProxy state,
                                     final SimpleNodeProxy node)
    throws VisitorException
  {
    final String name = state.getName();
    if (mLocalStatesMap.containsKey(name)) {
      final DuplicateIdentifierException exception =
        new DuplicateIdentifierException(name, "state", node);
      throw wrap(exception);
    }
    mLocalStatesMap.put(name, state);
    mLocalStatesList.add(state);
    final CompiledNode compiled = new CompiledNode(node, state);
    mPrecompiledNodesMap.put(node, compiled);
    return compiled;
  }

  private void createTransitions(final NodeProxy source,
                                 final List<EventProxy> events,
                                 final NodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic)
    throws VisitorException
  {
    if (source instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simpleSource = (SimpleNodeProxy) source;
      createTransitions(simpleSource, events, target, groupEntry,
                        deterministic);
    } else {
      for (final NodeProxy child : source.getImmediateChildNodes()) {
        createTransitions(child, events, target, groupEntry,
                          deterministic);
      }
    }
  }

  private void createTransitions(final SimpleNodeProxy source,
                                 final List<EventProxy> events,
                                 final NodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic)
    throws VisitorException
  {
    if (target instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simpleTarget = (SimpleNodeProxy) target;
      createTransitions(source, events, simpleTarget, groupEntry,
                        deterministic);
    } else {
      for (final NodeProxy child : target.getImmediateChildNodes()) {
        createTransitions(source, events, child, groupEntry,
                          deterministic);
      }
    }
  }

  private void createTransitions(final SimpleNodeProxy source,
                                 final List<EventProxy> events,
                                 final SimpleNodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic)
    throws VisitorException
  {
    final CompiledNode sourceEntry = mPrecompiledNodesMap.get(source);
    final CompiledNode targetEntry = mPrecompiledNodesMap.get(target);
    final StateProxy sourceState = sourceEntry.getState();
    final StateProxy targetState = targetEntry.getState();
    for (final EventProxy event : events) {
      CompiledTransition duplicate = null;
      boolean create = true;
      final Collection<CompiledTransition> compiledTransitions =
        sourceEntry.getCompiledTransitions(event);
      for (final CompiledTransition ctrans : compiledTransitions) {
        if (ctrans.getTarget() == targetEntry.getState()) {
          duplicate = ctrans;
          continue;
        }
        final NodeProxy cause = ctrans.getGroup();
        if (groupEntry.hasProperChildNode(cause)) {
          create = false;
          break;
        } else if (deterministic) {
          final NondeterministicModuleException exception =
            new NondeterministicModuleException
            (mCurrentComponent, source, event);
          throw wrap(exception);
        }
      }
      if (create) {
        final NodeProxy group = groupEntry.getNode();
        final TransitionProxy trans;
        if (duplicate == null) {
          trans = mFactory.createTransitionProxy
            (sourceState, event, targetState);
          mLocalTransitionsList.add(trans);
        } else {
          trans = duplicate.getTransition();
        }
        sourceEntry.addTransition(trans, group);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final ModuleProxy mInputModule;

  private Map<ProxyAccessor<IdentifierProxy>,EventProxy> mGlobalEventsMap;
  private List<EventProxy> mGlobalEventsList;
  private Map<ProxyAccessor<IdentifierProxy>,AutomatonProxy> mAutomataMap;
  private List<AutomatonProxy> mAutomataList;

  private SimpleComponentProxy mCurrentComponent;
  private Set<EventProxy> mLocalEventsSet;
  private List<EventProxy> mLocalEventsList;
  private Map<NodeProxy,CompiledNode> mPrecompiledNodesMap;
  private Map<String,StateProxy> mLocalStatesMap;
  private List<StateProxy> mLocalStatesList;
  private int mMaxInitialStates;
  private List<TransitionProxy> mLocalTransitionsList;

  private List<EventProxy> mCurrentEventsList;

}
