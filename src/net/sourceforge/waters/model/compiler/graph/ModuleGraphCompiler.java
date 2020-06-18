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

package net.sourceforge.waters.model.compiler.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * The fourth and final pass of the compiler.
 * <p>
 * This compiler accepts a {@link ModuleProxy} as the input and returns
 * a {@link ProductDESProxy} as the output. It assumes that the input
 * module only contains nodes of the type {@link SimpleNodeProxy}, and
 * that its edges have neither guards nor actions.
 *
 * @author Robi Malik
 */
public class ModuleGraphCompiler extends DefaultModuleProxyVisitor
                                 implements Abortable
{
  //##########################################################################
  //# Constructor
  public ModuleGraphCompiler(final ProductDESProxyFactory factory,
                             final CompilationInfo compilationInfo,
                             final ModuleProxy module)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mInputModule = module;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
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
  //# Aborting
  private void checkAbort() throws VisitorException
  {
    if (mIsAborting) {
      final EvalAbortException exception = new EvalAbortException();
      throw new VisitorException(exception);
    }
  }


  //##########################################################################
  //# Optimisation Configuration
  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }


  //##########################################################################
  //# Invocation
  public ProductDESProxy compile() throws EvalException
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
  /**
   * Converts a {@link ModuleProxy} to a {@link ProductDESProxy}.
   */
  @Override
  public ProductDESProxy visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    try {
      final String name = mInputModule.getName();
      final String comment = mInputModule.getComment();

      // Process event declarations.
      final List<EventDeclProxy> decls = mInputModule.getEventDeclList();
      final int numEvents = decls.size();
      mGlobalEventsMap = new HashMap<>(numEvents);
      mGlobalEventsList = new ArrayList<>(numEvents);
      visitCollection(decls);

      // Process components.
      final List<Proxy> components = mInputModule.getComponentList();
      final int numAut = components.size();
      mAutomataMap = new HashMap<>(numAut);
      mAutomataList = new ArrayList<>(numAut);
      visitCollection(components);

      return mFactory.createProductDESProxy(name, comment, null,
                                            mGlobalEventsList, mAutomataList);
    } finally {
      mGlobalEventsMap = null;
      mGlobalEventsList = null;
      mAutomataMap = null;
      mAutomataList = null;
    }
  }

  /**
   * Adds an event declaration ({@link EventDeclProxy}) to the global list of
   * events ({@link EventProxy}).
   */
  @Override
  public EventProxy visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    checkAbort();
    final IdentifierProxy ident = decl.getIdentifier();
    final String name = ident.toString();
    final EventKind kind = decl.getKind();
    final boolean observable = decl.isObservable();
    final Map<String,String> attribs = decl.getAttributes();
    final EventProxy event =
      mFactory.createEventProxy(name, kind, observable, attribs);
    addGlobalEvent(name, event, decl);
    return event;
  }

  /**
   * Converts a {@link SimpleComponentProxy} to an {@link AutomatonProxy}.
   */
  @Override
  public AutomatonProxy visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    try {
      mCurrentComponent = comp;

      // Prepare alphabet
      mLocalEventsMap = new HashMap<EventProxy,SelfloopInfo>();
      final GraphProxy graph = comp.getGraph();
      final EventListExpressionProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitEventListExpressionProxy(blocked);
      }

      // Pre-compile states
      mCurrentComponentIsDetermistic = graph.isDeterministic();
      mMaxInitialStates = mCurrentComponentIsDetermistic ? 1 : -1;
      final Collection<NodeProxy> nodes = graph.getNodes();
      final int numNodes = nodes.size();
      mPrecompiledNodesMap = new HashMap<NodeProxy,CompiledNode>(numNodes);
      mLocalStateNames = new HashSet<String>(numNodes);
      mLocalTransitionsList = new LinkedList<CompiledTransition>();
      visitCollection(nodes);
      if (mMaxInitialStates > 0) {
        throw new NondeterministicModuleException(comp);
      }

      // Pre-compile transitions
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      final Collection<EventProxy> keys = mLocalEventsMap.keySet();
      final List<EventProxy> alphabet = new ArrayList<EventProxy>(keys);

      // Optimisation
      if (mIsOptimizationEnabled) {
        mNumReachableStates = 0;
        mNumReachableTransitions = 0;
        final Queue<CompiledNode> queue =
          new ArrayDeque<CompiledNode>(numNodes);
        for (final CompiledNode node : mPrecompiledNodesMap.values()) {
          if (node.isInitial() && node.setReachable()) {
            queue.add(node);
            while (!queue.isEmpty()) {
              final CompiledNode current = queue.remove();
              for (final List<CompiledTransition> list : current
                .getOutgoingTransitions()) {
                for (final CompiledTransition trans : list) {
                  checkAbort();
                  trans.setReachable();
                  final CompiledNode target = trans.getTarget();
                  if (target.setReachable()) {
                    queue.add(target);
                  }
                }
              }
            }
          }
        }
        removeSelfloopedEvents(alphabet);
        if (mNumReachableStates > 0 && alphabet.isEmpty()) {
          return null;
        }
      } else {
        mNumReachableStates = nodes.size();
        mNumReachableTransitions = mLocalTransitionsList.size();
      }

      // Build alphabet, states, and transitions
      final List<StateProxy> states =
        new ArrayList<StateProxy>(mNumReachableStates);
      for (final NodeProxy node : nodes) {
        checkAbort();
        final CompiledNode cnode = mPrecompiledNodesMap.get(node);
        final StateProxy state = cnode.createStateProxy();
        if (state != null) {
          states.add(state);
        }
      }
      final List<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>(mNumReachableTransitions);
      for (final CompiledTransition ctrans : mLocalTransitionsList) {
        checkAbort();
        final TransitionProxy trans = ctrans.createTransitionProxy();
        if (trans != null) {
          transitions.add(trans);
        }
      }

      // Create automaton
      final IdentifierProxy ident = comp.getIdentifier();
      final String name = ident.toString();
      final ComponentKind kind = comp.getKind();
      final Map<String,String> attribs = comp.getAttributes();
      Collections.sort(alphabet);
      final AutomatonProxy aut =
        mFactory.createAutomatonProxy(name, kind, alphabet, states,
                                      transitions, attribs);
      addAutomaton(name, aut, comp);
      return aut;

    } catch (final EvalException exception) {
      throw wrap(exception);
    } finally {
      mCurrentComponent = null;
      mLocalEventsMap = null;
      mPrecompiledNodesMap = null;
      mLocalStateNames = null;
      mLocalTransitionsList = null;
      mMaxInitialStates = 0;
      mNumReachableStates = 0;
      mNumReachableTransitions = 0;
    }
  }

  @Override
  public CompiledNode visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    try {
      checkAbort();
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
          final Proxy location = mCompilationInfo.getErrorLocation(node);
          throw new NondeterministicModuleException
                      (mCurrentComponent, node, location);
        default:
          break;
        }
      }
      mCurrentSource = new CompiledNode(node);
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

  @Override
  public Object visitEdgeProxy(final EdgeProxy edge) throws VisitorException
  {
    try {
      final NodeProxy target = edge.getTarget();
      final LabelBlockProxy block = edge.getLabelBlock();
      mCurrentSource = mPrecompiledNodesMap.get(edge.getSource());
      mCurrentTarget = mPrecompiledNodesMap.get(target);
      return visitLabelBlockProxy(block);
    } finally {
      mCurrentSource = null;
      mCurrentTarget = null;
    }
  }

  @Override
  public Object visitEventListExpressionProxy(final EventListExpressionProxy elist)
    throws VisitorException
  {
    final List<Proxy> list = elist.getEventIdentifierList();
    visitCollection(list);
    return null;
  }

  @Override
  public EventProxy visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    try {
      checkAbort();
      final EventProxy event = findGlobalEvent(ident);
      if (event != null) {
        addLocalEvent(event);
        if (mCurrentSource == null) {
          // Do nothing for blocked events
        } else if (mCurrentTarget == null) {
          // Propositions of a state
          mCurrentSource.addProposition(event);
        } else {
          // Label block of an edge
          mCurrentTarget.addTransitionFrom(mCurrentSource, event,
                                           mCurrentSource, ident);
        }
      }
      return event;
    } catch (final NondeterministicModuleException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void addGlobalEvent(final String name,
                              final EventProxy event,
                              final EventDeclProxy decl)
    throws VisitorException
  {
    if (mGlobalEventsMap.containsKey(name)) {
      final DuplicateIdentifierException exception =
        new DuplicateIdentifierException(name, "event");
      exception.provideLocation(decl);
      mCompilationInfo.raiseInVisitor(exception);
    } else {
      mGlobalEventsMap.put(name, event);
      mGlobalEventsList.add(event);
      mCompilationInfo.add(event, decl);
    }
  }

  private EventProxy findGlobalEvent(final IdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.toString();
    final EventProxy event = mGlobalEventsMap.get(name);
    if (event != null) {
      return event;
    } else {
      final UndefinedIdentifierException exception =
        new UndefinedIdentifierException(ident, "event");
      mCompilationInfo.raiseInVisitor(exception);
      return null;
    }
  }

  private void addAutomaton(final String name,
                            final AutomatonProxy aut,
                            final SimpleComponentProxy comp)
    throws VisitorException
  {
    if (mAutomataMap.containsKey(name)) {
      final DuplicateIdentifierException exception =
        new DuplicateIdentifierException(name, "automaton");
      exception.provideLocation(comp);
      mCompilationInfo.raiseInVisitor(exception);
    } else {
      mAutomataMap.put(name, aut);
      mAutomataList.add(aut);
      mCompilationInfo.add(aut, comp);
    }
  }

  private void addLocalEvent(final EventProxy event)
  {
    if (!mLocalEventsMap.containsKey(event)) {
      final SelfloopInfo info = new SelfloopInfo();
      mLocalEventsMap.put(event, info);
      /* Special treatment of forbidden proposition:
       * exclude this from compiler optimisation.
       */
      final String name = event.getName();
      if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        info.addTransition(false);
      }
    }
  }

  private SelfloopInfo getSelfloopInfo(final EventProxy event)
  {
    return mLocalEventsMap.get(event);
  }

  private void removeSelfloopedEvents(final List<EventProxy> list)
  {
    if (list != null && mIsOptimizationEnabled) {
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
  //# Inner Class: CompiledNode
  private class CompiledNode
  {
    //#######################################################################
    //# Constructor
    CompiledNode(final SimpleNodeProxy node)
    {
      mmNode = node;
      mmPropositions = null;
      mmOutTransitions = new HashMap<EventProxy, List<CompiledTransition>>();
      mmState = null;
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    SimpleNodeProxy getNode()
    {
      return mmNode;
    }

    StateProxy getState()
    {
      return mmState;
    }

    boolean isInitial()
    {
      return mmNode.isInitial();
    }

    boolean isReachable()
    {
      return mmIsReachable;
    }

    //#######################################################################
    //# Algorithms
    void addProposition(final EventProxy event)
    {
      if (mmPropositions == null)
        mmPropositions = new LinkedList<EventProxy>();
      if (!mmPropositions.contains(event))
        mmPropositions.add(event);
    }

    void addTransitionFrom(final CompiledNode source, final EventProxy event,
                           final CompiledNode cause,  final Proxy orig)
      throws NondeterministicModuleException
    {
      source.addTransitionTo(this, event, cause, orig);
    }

    void addTransitionTo(final CompiledNode target, final EventProxy event,
                         final CompiledNode cause,  final Proxy orig)
      throws NondeterministicModuleException
    {
      List<CompiledTransition> transitions = mmOutTransitions.get(event);
      if (transitions == null) {
        transitions = new LinkedList<CompiledTransition>();
        mmOutTransitions.put(event, transitions);
      } else {
        for (final CompiledTransition trans : transitions) {
          if (trans.getTarget() == target) {
            return;
          } else if (mCurrentComponentIsDetermistic) {
            final Proxy location = mCompilationInfo.getErrorLocation(mmNode);
            throw new NondeterministicModuleException
                        (mCurrentComponent, mmNode, event, location);
          }
        }
      }
      final CompiledTransition trans =
        new CompiledTransition(this, event, target, cause, orig);
      transitions.add(trans);
      mLocalTransitionsList.add(trans);
    }

    boolean setReachable()
    {
      if (!mmIsReachable) {
        mmIsReachable = true;
        mNumReachableStates++;
        if (mmPropositions != null) {
          for (final EventProxy prop : mmPropositions) {
            final SelfloopInfo info = getSelfloopInfo(prop);
            info.addSelfloop();
          }
        }
        return true;
      } else {
        return false;
      }
    }

    Collection<List<CompiledTransition>> getOutgoingTransitions()
    {
      return mmOutTransitions.values();
    }

    StateProxy createStateProxy()
    {
      if (mmState == null) {
        if (!mIsOptimizationEnabled || mmIsReachable) {
          final SimpleNodeProxy node = mmNode;
          final String name = node.getName();
          final boolean initial = node.isInitial();
          removeSelfloopedEvents(mmPropositions);
          mmState = mFactory.createStateProxy(name, initial, mmPropositions);
          mCompilationInfo.add(mmState, node);
        }
      }
      return mmState;
    }

    //#######################################################################
    //# Data Members
    private final SimpleNodeProxy mmNode;
    private List<EventProxy> mmPropositions;
    private final Map<EventProxy,List<CompiledTransition>> mmOutTransitions;
    private boolean mmIsReachable;
    private StateProxy mmState;
  }


  //#########################################################################
  //# Inner Class: CompiledTransition
  private class CompiledTransition
  {
    //#######################################################################
    //# Constructor
    CompiledTransition(final CompiledNode source,
                       final EventProxy event,
                       final CompiledNode target,
                       final CompiledNode cause, final Proxy loc)
    {
      mSource = source;
      mEvent = event;
      mTarget = target;
      mLocation = loc;
    }

    //#######################################################################
    //# Simple Access
    CompiledNode getTarget()
    {
      return mTarget;
    }

    //#######################################################################
    //# Algorithms
    void setReachable()
    {
      final SelfloopInfo info = getSelfloopInfo(mEvent);
      info.addTransition(mSource == mTarget);
      mNumReachableTransitions++;
    }

    TransitionProxy createTransitionProxy()
    {
      final SelfloopInfo info = getSelfloopInfo(mEvent);
      if (mIsOptimizationEnabled
          && (info.isAllSelfloops() || !mSource.isReachable())) {
        return null;
      } else {
        final StateProxy sourcestate = mSource.getState();
        final StateProxy targetstate = mTarget.getState();
        final TransitionProxy trans =
          mFactory.createTransitionProxy(sourcestate, mEvent, targetstate);
        // Source info only for transitions that appear in the input module
        if (mCompilationInfo.getSourceInfo(mLocation) != null) {
          mCompilationInfo.add(trans, mLocation);
        }
        return trans;
      }
    }


    //#######################################################################
    //# Data Members
    private final CompiledNode mSource;
    private final EventProxy mEvent;
    private final CompiledNode mTarget;
    private final Proxy mLocation;
  }


  //#########################################################################
  //# Inner Class: SelfloopInfo
  private class SelfloopInfo
  {
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
  private final CompilationInfo mCompilationInfo;
  private final ModuleProxy mInputModule;

  private boolean mIsOptimizationEnabled = true;

  private Map<String,EventProxy> mGlobalEventsMap;
  private List<EventProxy> mGlobalEventsList;
  private Map<String,AutomatonProxy> mAutomataMap;
  private List<AutomatonProxy> mAutomataList;

  private SimpleComponentProxy mCurrentComponent;
  private boolean mCurrentComponentIsDetermistic;
  private Map<EventProxy,SelfloopInfo> mLocalEventsMap;
  private Map<NodeProxy,CompiledNode> mPrecompiledNodesMap;
  private Set<String> mLocalStateNames;
  private List<CompiledTransition> mLocalTransitionsList;
  private int mMaxInitialStates;
  private int mNumReachableStates;
  private int mNumReachableTransitions;

  private CompiledNode mCurrentSource;
  private CompiledNode mCurrentTarget;

  private boolean mIsAborting;
}
