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

//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE:
//# CLASS:   EFSMSystemImporter
//###########################################################################
//# $Id$
//###########################################################################

/**
 * @author saharm
 */
public class EFSMSystemImporter
{

  /**
   *
   */
  public EFSMSystemImporter (final ModuleProxyFactory factory,
                             final CompilerOperatorTable optable,
                             final EFSMSystem system)
  {
    mModuleFactory = factory;
    mOptable = optable;
  }

  public ModuleProxy visitSimpleComponentProxy
  (final EFSMSystem system)
  {

    final List<EFSMVariable> variableList = system.getVariables();
    for(final EFSMVariable variable : variableList) {
      final String variableName = variable.getName();
      final SimpleIdentifierProxy identifier =
        mModuleFactory.createSimpleIdentifierProxy(variableName);
      final CompiledRange range = variable.getRange();
      final SimpleExpressionProxy type = range.createExpression(mModuleFactory, mOptable);
      final SimpleExpressionProxy initialStatePredicate = null;
      mModuleFactory.createVariableComponentProxy(identifier, type, false, initialStatePredicate);
    }
    final List<EFSMTransitionRelation> trList = system.getTransitionRelations();
    final List<ComponentProxy> compList =
      new ArrayList<ComponentProxy>(variableList.size()+trList.size());
    for (final EFSMTransitionRelation trElement : trList) {
      final ListBufferTransitionRelation rel = trElement.getTransitionRelation();
      final EFSMEventEncoding efsmEvents = trElement.getEventEncoding();
      final String name = rel.getName();
      final String eventName = "tau:" + name;
      final int numStates = rel.getNumberOfStates();
      final List<SimpleNodeProxy> nodeList = new ArrayList<SimpleNodeProxy>(numStates);
      for (int i = 0; i < numStates; i++) {
        final boolean isInitial = rel.isInitial(i);
        final boolean isMarked = rel.isMarked(i, 0);
        PlainEventListProxy props = null;
        if (isMarked) {
          final SimpleIdentifierProxy ident =
            mModuleFactory
              .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
          final List<SimpleIdentifierProxy> identList =
            Collections.singletonList(ident);
          props = mModuleFactory.createPlainEventListProxy(identList);
        }
        final String nodeName = "S" + i;
        final SimpleNodeProxy node =
          mModuleFactory.createSimpleNodeProxy(nodeName, props, null,
                                               isInitial, null, null, null);
        nodeList.add(node);
      }
      final List<EdgeProxy> edgeList =
        new ArrayList<EdgeProxy>(rel.getNumberOfTransitions());
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while(iter.advance()) {
        final int event = iter.getCurrentEvent();
        final int source = iter.getCurrentSourceState();
        final int target = iter.getCurrentTargetState();
        final ConstraintList update = efsmEvents.getUpdate(event);
        final List<SimpleExpressionProxy> guards = update.getConstraints();
        final GuardActionBlockProxy guardActionBlock =
          mModuleFactory.createGuardActionBlockProxy(guards, null, null);
        final SimpleIdentifierProxy ident =
          mModuleFactory.createSimpleIdentifierProxy(eventName);
        final List<SimpleIdentifierProxy> identList = Collections.singletonList(ident);
        final LabelBlockProxy block =
          mModuleFactory.createLabelBlockProxy(identList, null);
        final SimpleNodeProxy sourceNode = nodeList.get(source);
        final SimpleNodeProxy targetNode = nodeList.get(target);
        final EdgeProxy edge = mModuleFactory.createEdgeProxy
          (sourceNode, targetNode, block, guardActionBlock, null, null, null);
        edgeList.add(edge);
      }
      final GraphProxy graph =
        mModuleFactory.createGraphProxy(false, null, nodeList, edgeList);
      final SimpleIdentifierProxy ident =
        mModuleFactory.createSimpleIdentifierProxy(name);

      final SimpleComponentProxy simpleComponent =
        mModuleFactory.createSimpleComponentProxy(ident, rel.getKind(), graph);
      compList.add(simpleComponent);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mModuleFactory;
  private final CompilerOperatorTable mOptable;

}
