//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFASystemImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFASystemImporter
{

  //#########################################################################
  //# Constructors
  public UnifiedEFASystemImporter(final ModuleProxyFactory factory,
                                  final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy importModule(final UnifiedEFASystem system)
  {
    final List<UnifiedEFAVariable> variableList = system.getVariables();
    final List<UnifiedEFATransitionRelation> trs =
      system.getTransitionRelations();
    final int numComponents = variableList.size() + trs.size();
    final List<EventDeclProxy> eventList = new ArrayList<>(numComponents + 1);
    final SimpleIdentifierProxy accepting =
      mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy marking =
      mFactory.createEventDeclProxy(accepting, EventKind.PROPOSITION);
    eventList.add(marking);
    final Collection<AbstractEFAEvent> events = system.getEvents();
    final List<EventAliasProxy> aliasList = new ArrayList<>();
    final List<AbstractEFAEvent> leaves =
      importAliases(events, trs, aliasList);
    for (final AbstractEFAEvent unifiedEvent : leaves) {
      importEvent(eventList, unifiedEvent);
    }
    final List<ComponentProxy> compList = new ArrayList<>(numComponents);
    for (final UnifiedEFAVariable variable : variableList) {
      importVariable(compList, variable);
    }
    for (final UnifiedEFATransitionRelation tr : trs) {
      importTransitionRelation(compList, eventList, tr);
    }
    final SimpleComponentProxy guardAut = createGuardAutomaton(leaves);
    if (guardAut != null) {
      compList.add(guardAut);
    }
    return mFactory.createModuleProxy
      (system.getName(), null, null, null, eventList, aliasList, compList);
  }

  public SimpleComponentProxy importTransitionRelation
    (final UnifiedEFATransitionRelation tr)
  {
    final List<SimpleNodeProxy> nodeListFromEFA = tr.getNodeList();
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    final UnifiedEFAEventEncoding eventEncoding = tr.getEventEncoding();
    final String name = rel.getName();
    final int numStates = rel.getNumberOfStates();
    final List<SimpleNodeProxy> nodeList = new ArrayList<>(numStates);
    final SimpleNodeProxy[] nodeArray = new SimpleNodeProxy[numStates];
    final boolean isMarkingIsUsed =
      rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA);
    int numMarkedStates = isMarkingIsUsed ? 0 : -1;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        final boolean isInitial = rel.isInitial(s);
        final boolean isMarked =
          isMarkingIsUsed && rel.isMarked(s, UnifiedEFAEventEncoding.OMEGA);
        PlainEventListProxy props = null;
        if (isMarked) {
          numMarkedStates++;
          final SimpleIdentifierProxy ident =
            mFactory.createSimpleIdentifierProxy
            (EventDeclProxy.DEFAULT_MARKING_NAME);
          final List<SimpleIdentifierProxy> identList =
            Collections.singletonList(ident);
          props = mFactory.createPlainEventListProxy(identList);
        }
        final String nodeName;
        if (nodeListFromEFA == null) {
          nodeName = "S" + s;
        } else {
          final SimpleNodeProxy nodeFromEFSM = nodeListFromEFA.get(s);
          nodeName = nodeFromEFSM.getName();
        }
        final SimpleNodeProxy node =
          mFactory.createSimpleNodeProxy(nodeName, props, null,
                                         isInitial, null, null, null);
        nodeList.add(node);
        nodeArray[s] = node;
      }
    }
    final List<IdentifierProxy> blockedList = new LinkedList<>();
    if (numMarkedStates == 0) {
      final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy
        (EventDeclProxy.DEFAULT_MARKING_NAME);
      blockedList.add(ident);
    }
    final int numEvents = rel.getNumberOfProperEvents();
    final boolean forward =
      (rel.getConfiguration() & ListBufferTransitionRelation.CONFIG_SUCCESSORS)!=0;
    final boolean[] foundEvent = new boolean[numEvents];
    final List<EdgeProxy> edgeList =
      new ArrayList<>(rel.getNumberOfTransitions());
    final TransitionIterator transIter = rel.createAnyReadOnlyIterator();
    final TIntIntHashMap transitionMap = new TIntIntHashMap();
    final IntListBuffer toStateBuffer = new IntListBuffer();
    final IntListBuffer.ReadOnlyIterator bufferIter =
      toStateBuffer.createReadOnlyIterator();
    final List<IdentifierProxy> labels =new ArrayList<>();
    for (int fromState = 0; fromState < numStates; fromState++) {
      transIter.resetState(fromState);
      while (transIter.advance()) {
        final int e = transIter.getCurrentEvent();
        foundEvent[e] = true;
        final int toState = transIter.getCurrentToState();
        int list = transitionMap.get(toState);
        if (list== IntListBuffer.NULL) {
          list = toStateBuffer.createList();
          transitionMap.put(toState, list);
        }
        toStateBuffer.append(list, e);
      }
      for (int toState = 0; toState < numStates; toState++) {
        final int list = transitionMap.get(toState);
        if (list != IntListBuffer.NULL) {
          bufferIter.reset(list);
          while (bufferIter.advance()) {
            final int e = bufferIter.getCurrentData();
            final AbstractEFAEvent event = eventEncoding.getUpdate(e);
            final String eventName = event.getName();
            final IdentifierProxy ident =
              mFactory.createSimpleIdentifierProxy(eventName);
            labels.add(ident);
          }
          final LabelBlockProxy block =
            mFactory.createLabelBlockProxy(labels, null);
          labels.clear();
          final SimpleNodeProxy source;
          final SimpleNodeProxy target;
          if (forward) {
            source = nodeArray[fromState];
            target = nodeArray[toState];
          } else {
            source = nodeArray[toState];
            target = nodeArray[fromState];
          }
          final EdgeProxy edge = mFactory.createEdgeProxy
            (source, target, block, null, null, null, null);
          edgeList.add(edge);
        }
      }
      transitionMap.clear();
      toStateBuffer.clear();
    }
    for (int e = 0; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventEncoding.isUsedEvent(status) && !foundEvent[e]) {
        final AbstractEFAEvent event = eventEncoding.getUpdate(e);
        final String eventName = event.getName();
        final IdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(eventName);
        blockedList.add(ident);
      }
    }
    final LabelBlockProxy blocked = blockedList.isEmpty() ? null :
      mFactory.createLabelBlockProxy(blockedList, null);
    final boolean deterministic = rel.isDeterministic();
    final GraphProxy graph =
      mFactory.createGraphProxy(deterministic, blocked, nodeList, edgeList);
    final SimpleIdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy(name);
    final SimpleComponentProxy simpleComponent =
      mFactory.createSimpleComponentProxy(ident, rel.getKind(), graph);
    return simpleComponent;
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<AbstractEFAEvent> importAliases
    (final Collection<AbstractEFAEvent> events,
     final Collection<UnifiedEFATransitionRelation> trs,
     final List<EventAliasProxy> aliasList)
  {
    final Set<AbstractEFAEvent> leaveSet = new THashSet<>(events);
    for (final AbstractEFAEvent event : events) {
      for (AbstractEFAEvent original = event.getOriginalEvent();
           original != null; original = original.getOriginalEvent()) {
        leaveSet.remove(original);
      }
    }
    final List<AbstractEFAEvent> leaveList =
      new ArrayList<>(leaveSet.size());
    for (final AbstractEFAEvent event : events) {
      if (leaveSet.contains(event)) {
        leaveList.add(event);
      }
    }
    final Map<AbstractEFAEvent,List<AbstractEFAEvent>> aliasMap =
      new HashMap<>(events.size() - leaveSet.size());
    for (final UnifiedEFATransitionRelation tr : trs) {
      for (final AbstractEFAEvent event : tr.getUsedEventsExceptTau()) {
        if (!leaveSet.contains(event) && !aliasMap.containsKey(event)) {
          final List<AbstractEFAEvent> list = new ArrayList<>();
          aliasMap.put(event, list);
        }
      }
    }
    for (final AbstractEFAEvent leave : leaveList) {
      for (AbstractEFAEvent original = leave.getOriginalEvent();
           original != null; original = original.getOriginalEvent()) {
        final List<AbstractEFAEvent> list = aliasMap.get(original);
        if (list != null) {
          list.add(leave);
        }
      }
    }
    for (final Map.Entry<AbstractEFAEvent,List<AbstractEFAEvent>> entry :
         aliasMap.entrySet()) {
      final AbstractEFAEvent event = entry.getKey();
      final String name = event.getName();
      final List<AbstractEFAEvent> list = entry.getValue();
      final List<SimpleIdentifierProxy> elist = new ArrayList<>(list.size());
      for (final AbstractEFAEvent leave : list) {
        final String leaveName = leave.getName();
        if (!leaveName.equals(name)) {
          final SimpleIdentifierProxy leaveIdent =
            mFactory.createSimpleIdentifierProxy(leaveName);
          elist.add(leaveIdent);
        }
      }
      if (!elist.isEmpty()) {
        Collections.sort(elist);
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name);
        final PlainEventListProxy expr =
          mFactory.createPlainEventListProxy(elist);
        final EventAliasProxy alias =
          mFactory.createEventAliasProxy(ident, expr);
        aliasList.add(alias);
      }
    }
    return leaveList;
  }

  private void importEvent(final List<EventDeclProxy> eventList,
                           final AbstractEFAEvent unifiedEvent)
  {
    final String eventName = unifiedEvent.getName();
    final IdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy(eventName);
    final EventDeclProxy eventDecl =
      mFactory.createEventDeclProxy(ident, unifiedEvent.getKind(),
                                    unifiedEvent.isObservable(),
                                    ScopeKind.LOCAL, null, null, null);
    eventList.add(eventDecl);
  }

  private void importVariable(final List<ComponentProxy> compList,
                              final UnifiedEFAVariable variable)
  {
    final String variableName = variable.getName();
    final SimpleIdentifierProxy identifier =
      mFactory.createSimpleIdentifierProxy(variableName);
    final CompiledRange range = variable.getRange();
    final SimpleExpressionProxy type =
      range.createExpression(mFactory, mOperatorTable);
    final SimpleExpressionProxy initialStatePredicate =
      variable.getInitialStatePredicate();
    List<VariableMarkingProxy> markings = null;
    final SimpleExpressionProxy markedStatePredicate =
      variable.getMarkedStatePredicate();
    if (markedStatePredicate != null) {
      final SimpleIdentifierProxy markingIdent =
        mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      final VariableMarkingProxy marking =
        mFactory.createVariableMarkingProxy(markingIdent, markedStatePredicate);
      markings = Collections.singletonList(marking);
    }
    final VariableComponentProxy var =
      mFactory.createVariableComponentProxy
        (identifier, type, initialStatePredicate, markings);
    compList.add(var);
  }

  private void importTransitionRelation(final List<ComponentProxy> compList,
                                        final List<EventDeclProxy> eventList,
                                        final UnifiedEFATransitionRelation tr)
  {
    importTauEvent(eventList, tr);
    final SimpleComponentProxy comp = importTransitionRelation(tr);
    compList.add(comp);
  }


  void importTauEvent(final List<EventDeclProxy> eventList,
                      final UnifiedEFATransitionRelation tr)
  {
    if (tr.isUsedEvent(EventEncoding.TAU)) {
      final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
      final AbstractEFAEvent tau = encoding.getEvent(EventEncoding.TAU);
      final String name = tau.getName();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name);
      final EventKind kind = tau.getKind();
      final EventDeclProxy decl = mFactory.createEventDeclProxy(ident, kind);
      eventList.add(decl);
    }
  }

  private SimpleComponentProxy createGuardAutomaton
    (final Collection<AbstractEFAEvent> unifiedEvents)
  {
    final SimpleNodeProxy node =
      mFactory.createSimpleNodeProxy("init", null, null, true, null, null, null);
    final List<EdgeProxy> edges = new ArrayList<>(unifiedEvents.size());
    for (final AbstractEFAEvent event : unifiedEvents) {
      final ConstraintList update = event.getUpdate();
      if (update != null && !update.isTrue()) {
        final String name = event.getName();
        final IdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(name);
        final SimpleExpressionProxy guard =
          update.createExpression(mFactory, mOperatorTable.getAndOperator());
        final List<SimpleExpressionProxy> guards =
          Collections.singletonList(guard);
        final GuardActionBlockProxy ga =
          mFactory.createGuardActionBlockProxy(guards, null, null);
        final List<IdentifierProxy> labels = Collections.singletonList(ident);
        final LabelBlockProxy block =
          mFactory.createLabelBlockProxy(labels, null);
        final EdgeProxy edge =
          mFactory.createEdgeProxy(node, node, block, ga, null, null, null);
        edges.add(edge);
      }
    }
    if (edges.isEmpty()) {
      return null;
    }
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final GraphProxy graph =
      mFactory.createGraphProxy(true, null, nodes, edges);
    final IdentifierProxy name =
      mFactory.createSimpleIdentifierProxy(":updates");
    return
      mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;

}
