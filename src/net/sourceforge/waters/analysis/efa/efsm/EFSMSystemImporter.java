package net.sourceforge.waters.analysis.efa.efsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE:
//# CLASS:   UnifiedEFASystemImporter
//###########################################################################
//# $Id$
//###########################################################################

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMSystemImporter
{

  /**
   *
   */
  public EFSMSystemImporter(final ModuleProxyFactory factory,
                            final CompilerOperatorTable optable)
  {
    mModuleFactory = factory;
    mOperatorTable = optable;
  }

  public ModuleProxy importModule(final EFSMSystem system)
  {
    final List<EFSMVariable> variableList = system.getVariables();
    final List<EFSMTransitionRelation> trList =
      system.getTransitionRelations();
    final int numComponents = variableList.size() + trList.size();
    final List<EventDeclProxy> eventList =
      new ArrayList<EventDeclProxy>(numComponents + 1);
    final SimpleIdentifierProxy accepting =
      mModuleFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy marking =
      mModuleFactory.createEventDeclProxy(accepting, EventKind.PROPOSITION);
    eventList.add(marking);
    final List<ComponentProxy> compList =
      new ArrayList<ComponentProxy>(numComponents);
    for (final EFSMVariable variable : variableList) {
      importVariable(eventList, compList, variable);
    }
    for (final EFSMTransitionRelation efsmTransition : trList) {
      importTransitionRelation(eventList, compList, efsmTransition);
    }
    return mModuleFactory.createModuleProxy
      (system.getName(), null, null, null, eventList, null, compList);
  }

  private void importVariable(final List<EventDeclProxy> eventList,
                              final List<ComponentProxy> compList,
                              final EFSMVariable variable)
  {
    final String variableName = variable.getName();
    final SimpleIdentifierProxy identifier =
      mModuleFactory.createSimpleIdentifierProxy(variableName);
    final CompiledRange range = variable.getRange();
    final SimpleExpressionProxy type =
      range.createExpression(mModuleFactory, mOperatorTable);
    final SimpleExpressionProxy initialStatePredicate =
      variable.getInitialStatePredicate();
    final VariableComponentProxy var =
      mModuleFactory.createVariableComponentProxy
        (identifier, type, initialStatePredicate);
    compList.add(var);
    importSelfloops(eventList, compList, variable);
  }

  private void importSelfloops(final List<EventDeclProxy> eventList,
                               final List<ComponentProxy> compList,
                               final EFSMVariable variable)
  {
    final EFSMEventEncoding selfloops = variable.getSelfloops();
    final int numSelfloops = selfloops.size() - 1;
    if (numSelfloops > 0) {
      final String varName = variable.getName();
      final String eventName = "tau:" + varName;
      final SimpleIdentifierProxy eventIdent =
        mModuleFactory.createSimpleIdentifierProxy(eventName);
      final EventDeclProxy decl =
        mModuleFactory.createEventDeclProxy(eventIdent, EventKind.CONTROLLABLE);
      eventList.add(decl);
      final SimpleNodeProxy node = mModuleFactory.createSimpleNodeProxy
        (":sl", null, null, true, null, null, null);
      final Collection<SimpleNodeProxy> nodes = Collections.singletonList(node);
      final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>(numSelfloops);
      for (int e = EventEncoding.NONTAU; e <= numSelfloops; e++) {
        final ConstraintList update = selfloops.getUpdate(e);
        final GuardActionBlockProxy guardActionBlock = createGuard(update);
        final SimpleIdentifierProxy label =
          mModuleFactory.createSimpleIdentifierProxy(eventName);
        final List<SimpleIdentifierProxy> labels =
          Collections.singletonList(label);
        final LabelBlockProxy block =
          mModuleFactory.createLabelBlockProxy(labels, null);
        final EdgeProxy edge =
          mModuleFactory.createEdgeProxy(node, node, block,
                                         guardActionBlock, null, null, null);
        edges.add(edge);
      }
      final GraphProxy graph =
        mModuleFactory.createGraphProxy(true, null, nodes, edges);
      final String compName = "selfloops:" + varName;
      final SimpleIdentifierProxy compIndent =
        mModuleFactory.createSimpleIdentifierProxy(compName);
      final SimpleComponentProxy comp =
        mModuleFactory.createSimpleComponentProxy(compIndent,
                                                  ComponentKind.PLANT,
                                                  graph);
      compList.add(comp);
    }
  }

  private void importTransitionRelation(final List<EventDeclProxy> eventList,
                                        final List<ComponentProxy> compList,
                                        final EFSMTransitionRelation efsmTransition)
  {
    final List<SimpleNodeProxy> nodeListFromEFSM = efsmTransition.getNodeList();
    final ListBufferTransitionRelation rel =
      efsmTransition.getTransitionRelation();
    final EFSMEventEncoding efsmEvents = efsmTransition.getEventEncoding();
    final String name = rel.getName();
    // Should marking ID be zero?
    final boolean isMarkingIsUsed = rel.isUsedProposition(0);
    final String eventName = "tau:" + name;
    final SimpleIdentifierProxy identEvent =
      mModuleFactory.createSimpleIdentifierProxy(eventName);
    final EventKind kind = EventKind.CONTROLLABLE;
    final EventDeclProxy eventDecl = mModuleFactory.createEventDeclProxy
      (identEvent, kind, true, ScopeKind.LOCAL, null, null, null);
    eventList.add(eventDecl);
    final int numStates = rel.getNumberOfStates();
    final List<SimpleNodeProxy> nodeList =
      new ArrayList<SimpleNodeProxy>(numStates);
    int numOfMarkingState = 0;
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked = rel.isMarked(i, 0);
      PlainEventListProxy props = null;
      if (isMarked && isMarkingIsUsed) {
        numOfMarkingState++;
        final SimpleIdentifierProxy ident =
          mModuleFactory.createSimpleIdentifierProxy
            (EventDeclProxy.DEFAULT_MARKING_NAME);
        final List<SimpleIdentifierProxy> identList =
          Collections.singletonList(ident);
        props = mModuleFactory.createPlainEventListProxy(identList);
      }
      final String nodeName;
      if (nodeListFromEFSM == null) {
        nodeName = "S" + i;
      } else {
        final SimpleNodeProxy nodeFromEFSM = nodeListFromEFSM.get(i);
        nodeName = nodeFromEFSM.getName();
      }
      final SimpleNodeProxy node =
        mModuleFactory.createSimpleNodeProxy(nodeName, props, null,
                                             isInitial, null, null, null);
      nodeList.add(node);
    }
    LabelBlockProxy markingBlock = null;
    if (isMarkingIsUsed && numOfMarkingState < 1) {
      final SimpleIdentifierProxy ident = mModuleFactory
          .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      final List<SimpleIdentifierProxy> identList =
        Collections.singletonList(ident);
      markingBlock = mModuleFactory.createLabelBlockProxy(identList, null);
    }
    final List<EdgeProxy> edgeList =
      new ArrayList<EdgeProxy>(rel.getNumberOfTransitions());
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int event = iter.getCurrentEvent();
      final int source = iter.getCurrentSourceState();
      final int target = iter.getCurrentTargetState();
      final ConstraintList update = efsmEvents.getUpdate(event);
      final GuardActionBlockProxy guardActionBlock = createGuard(update);
      final SimpleIdentifierProxy ident =
        mModuleFactory.createSimpleIdentifierProxy(eventName);
      final List<SimpleIdentifierProxy> identList =
        Collections.singletonList(ident);
      final LabelBlockProxy block =
        mModuleFactory.createLabelBlockProxy(identList, null);
      final SimpleNodeProxy sourceNode = nodeList.get(source);
      final SimpleNodeProxy targetNode = nodeList.get(target);
      final EdgeProxy edge =
        mModuleFactory.createEdgeProxy(sourceNode, targetNode, block,
                                       guardActionBlock, null, null, null);
      edgeList.add(edge);
    }
    final GraphProxy graph =
      mModuleFactory.createGraphProxy(false, markingBlock, nodeList, edgeList);
    final SimpleIdentifierProxy ident =
      mModuleFactory.createSimpleIdentifierProxy(name);

    final SimpleComponentProxy simpleComponent =
      mModuleFactory.createSimpleComponentProxy(ident, rel.getKind(), graph);
    compList.add(simpleComponent);
  }


  //#########################################################################
  //# Auxiliary Method
  private GuardActionBlockProxy createGuard(final ConstraintList constraints)
  {
    if (constraints.isTrue()) {
      return null;
    } else {
      final BinaryOperator op = mOperatorTable.getAndOperator();
      SimpleExpressionProxy guard = null;
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        if (guard == null) {
          guard = constraint;
        } else {
          guard = mModuleFactory.createBinaryExpressionProxy
            (op, guard, constraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards =
        Collections.singletonList(guard);
      return mModuleFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mModuleFactory;
  private final CompilerOperatorTable mOperatorTable;

}
