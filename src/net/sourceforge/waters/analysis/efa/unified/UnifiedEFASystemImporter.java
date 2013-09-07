//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFASystemImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
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
    mCloner = mFactory.getCloner();
    mOperatorTable = optable;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy importModule(final UnifiedEFASystem system)
  {
    final List<UnifiedEFAVariable> variableList = system.getVariables();
    final List<UnifiedEFATransitionRelation> trList =
      system.getTransitionRelations();
    final int numComponents = variableList.size() + trList.size();
    final List<EventDeclProxy> eventList = new ArrayList<>(numComponents + 1);
    final SimpleIdentifierProxy accepting =
      mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy marking =
      mFactory.createEventDeclProxy(accepting, EventKind.PROPOSITION);
    eventList.add(marking);
    final Collection<UnifiedEFAEvent> unifiedEvents = system.getEvents();
    for (final UnifiedEFAEvent unifiedEvent : unifiedEvents) {
      importEvent(eventList, unifiedEvent);
    }
    final List<ComponentProxy> compList = new ArrayList<>(numComponents);
    for (final UnifiedEFAVariable variable : variableList) {
      importVariable(compList, variable);
    }
    for (final UnifiedEFATransitionRelation efsmTransition : trList) {
      importTransitionRelation(compList, efsmTransition);
    }
    final SimpleComponentProxy guardAut = createGuardAutomaton(unifiedEvents);
    if (guardAut != null) {
      compList.add(guardAut);
    }
    return mFactory.createModuleProxy
      (system.getName(), null, null, null, eventList, null, compList);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void importEvent(final List<EventDeclProxy> eventList,
                           final UnifiedEFAEvent uniEvent)
  {
    final EventDeclProxy eventDecl = uniEvent.getEventDecl();
    final EventDeclProxy eventDeclClone = (EventDeclProxy) mCloner.getClone(eventDecl);
    eventList.add(eventDeclClone);
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
        (identifier, type, false, initialStatePredicate, markings);
    compList.add(var);
  }

  private void importTransitionRelation(final List<ComponentProxy> compList,
                                        final UnifiedEFATransitionRelation efsmTransition)
  {
    final List<SimpleNodeProxy> nodeListFromEFA = efsmTransition.getNodeList();
    final ListBufferTransitionRelation rel =
      efsmTransition.getTransitionRelation();
    final UnifiedEFAEventEncoding eventEncoding = efsmTransition.getEventEncoding();
    final String name = rel.getName();
    final boolean isMarkingIsUsed =
      rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA);
    final int numStates = rel.getNumberOfStates();
    final List<SimpleNodeProxy> nodeList =
      new ArrayList<SimpleNodeProxy>(numStates);
    int numOfMarkingStates = 0;
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked =
        isMarkingIsUsed && rel.isMarked(i, UnifiedEFAEventEncoding.OMEGA);
      PlainEventListProxy props = null;
      if (isMarked) {
        numOfMarkingStates++;
        final SimpleIdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy
            (EventDeclProxy.DEFAULT_MARKING_NAME);
        final List<SimpleIdentifierProxy> identList =
          Collections.singletonList(ident);
        props = mFactory.createPlainEventListProxy(identList);
      }
      final String nodeName;
      if (nodeListFromEFA == null) {
        nodeName = "S" + i;
      } else {
        final SimpleNodeProxy nodeFromEFSM = nodeListFromEFA.get(i);
        nodeName = nodeFromEFSM.getName();
      }
      final SimpleNodeProxy node =
        mFactory.createSimpleNodeProxy(nodeName, props, null,
                                             isInitial, null, null, null);
      nodeList.add(node);
    }
    LabelBlockProxy markingBlock = null;
    if (isMarkingIsUsed && numOfMarkingStates < 1) {
      final SimpleIdentifierProxy ident = mFactory
          .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      final List<SimpleIdentifierProxy> identList =
        Collections.singletonList(ident);
      markingBlock = mFactory.createLabelBlockProxy(identList, null);
    }
    final List<EdgeProxy> edgeList =
      new ArrayList<EdgeProxy>(rel.getNumberOfTransitions());
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int eventCode = iter.getCurrentEvent();
      final int source = iter.getCurrentSourceState();
      final int target = iter.getCurrentTargetState();
      final UnifiedEFAEvent event = eventEncoding.getUpdate(eventCode);
      final IdentifierProxy ident = event.getEventDecl().getIdentifier();
      final IdentifierProxy cloneIdent =
        (IdentifierProxy) mCloner.getClone(ident);
      final List<IdentifierProxy> identList =
        Collections.singletonList(cloneIdent);
      final LabelBlockProxy block =
        mFactory.createLabelBlockProxy(identList, null);
      final SimpleNodeProxy sourceNode = nodeList.get(source);
      final SimpleNodeProxy targetNode = nodeList.get(target);
      final EdgeProxy edge =
        mFactory.createEdgeProxy(sourceNode, targetNode, block,
                                       null, null, null, null);
      edgeList.add(edge);
    }
    final boolean deterministic = rel.isDeterministic();
    final GraphProxy graph =
      mFactory.createGraphProxy(deterministic, markingBlock, nodeList, edgeList);
    final SimpleIdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy(name);
    final SimpleComponentProxy simpleComponent =
      mFactory.createSimpleComponentProxy(ident, rel.getKind(), graph);
    compList.add(simpleComponent);
  }

  private SimpleComponentProxy createGuardAutomaton
    (final Collection<UnifiedEFAEvent> events)
  {
    final SimpleNodeProxy node =
      mFactory.createSimpleNodeProxy("init", null, null, true, null, null, null);
    final List<EdgeProxy> edges = new ArrayList<>(events.size());
    for (final UnifiedEFAEvent event : events) {
      final IdentifierProxy ident = event.getEventDecl().getIdentifier();
      final ConstraintList update = event.getUpdate();
      if (update != null && !update.isTrue()) {
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
  private final ModuleProxyCloner mCloner;
  private final CompilerOperatorTable mOperatorTable;

}
