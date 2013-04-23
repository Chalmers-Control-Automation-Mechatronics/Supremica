package net.sourceforge.waters.analysis.efsm;

import java.util.ArrayList;
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
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE:
//# CLASS:   EFSMSystemImporter
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
    mOptable = optable;
  }

  public ModuleProxy importModule(final EFSMSystem system)
  {

    final List<EFSMVariable> variableList = system.getVariables();
    final List<EventDeclProxy> eventList =
      new ArrayList<EventDeclProxy> (system.getTransitionRelations().size()+1);
    final SimpleIdentifierProxy accepting =
      mModuleFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy marking =
      mModuleFactory.createEventDeclProxy(accepting, EventKind.PROPOSITION);
    eventList.add(marking);
    final List<EFSMTransitionRelation> trList =
      system.getTransitionRelations();
    final List<ComponentProxy> compList =
      new ArrayList<ComponentProxy>(variableList.size() + trList.size());

    for (final EFSMVariable variable : variableList) {
      final String variableName = variable.getName();
      final SimpleIdentifierProxy identifier =
        mModuleFactory.createSimpleIdentifierProxy(variableName);
      final CompiledRange range = variable.getRange();
      final SimpleExpressionProxy type =
        range.createExpression(mModuleFactory, mOptable);
      final SimpleExpressionProxy initialStatePredicate =
        variable.getInitialStatePredicate();
      final VariableComponentProxy var = mModuleFactory.createVariableComponentProxy
        (identifier, type, false, initialStatePredicate);
      compList.add(var);
    }

    for (final EFSMTransitionRelation efsmTransition : trList) {
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
              mModuleFactory
                .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
            final List<SimpleIdentifierProxy> identList =
              Collections.singletonList(ident);
            props = mModuleFactory.createPlainEventListProxy(identList);
        }
        final SimpleNodeProxy nodeFromEFSM = nodeListFromEFSM.get(i);

        final String nodeName = nodeFromEFSM.getName();
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
        markingBlock =
          mModuleFactory.createLabelBlockProxy(identList, null);
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
        final List<SimpleExpressionProxy> guards = update.getConstraints();
        final GuardActionBlockProxy guardActionBlock =
          mModuleFactory.createGuardActionBlockProxy(guards, null, null);
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
        mModuleFactory
          .createSimpleComponentProxy(ident, rel.getKind(), graph);
      compList.add(simpleComponent);
    }
    return mModuleFactory.createModuleProxy
      (system.getName(), null, null, null, eventList, null, compList);
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mModuleFactory;
  private final CompilerOperatorTable mOptable;

}
