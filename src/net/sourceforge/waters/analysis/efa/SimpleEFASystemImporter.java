//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE:
//# CLASS:   SimpleEFASystemImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
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
import net.sourceforge.waters.subject.module.ComponentSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * A utility to pack a SimpleEFASystem into a module
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystemImporter
{

  public SimpleEFASystemImporter(final ModuleProxyFactory factory,
                                 final CompilerOperatorTable optable)
  {
    mModuleFactory = factory;
    mOperatorTable = optable;
    mSubjectFactory = ModuleSubjectFactory.getInstance();
    mCloner = ModuleSubjectFactory.getCloningInstance();
    mGlobalEvents = new THashSet<EventDeclSubject>();
  }
  
  /**
   * Creating a module representing the system
   * <p/>
   * @param system An EFA system ({@link SimpleEFASystem})
   * <p/>
   * @return A module containing the EFA component, variables, and system
   *         events.
   */
  public ModuleProxy importModule(final SimpleEFASystem system)
  {
    return createModule(system);
  }

  private ModuleProxy createModule(final SimpleEFASystem system)
  {
    final List<SimpleEFAVariable> variableList = system.getVariables();
    final List<SimpleEFAComponent> comps =
     system.getTransitionRelations();
    mGlobalEvents.addAll(getEventDeclSubjects(system.getSystemEvents()));
    final TreeMap<String, SimpleComponentProxy> compList =
     new TreeMap<String, SimpleComponentProxy>(String.CASE_INSENSITIVE_ORDER);
    final TreeMap<String, VariableComponentProxy> varList =
     new TreeMap<String, VariableComponentProxy>(String.CASE_INSENSITIVE_ORDER);
    for (final SimpleEFAComponent comp : comps) {
      compList.put(comp.getName(), getSimpleComponent(comp));
    }
    for (final SimpleEFAVariable variable : variableList) {
      varList.put(variable.getName(), getVariableComponent(variable));
    }
    final List<ComponentProxy> list = new ArrayList<ComponentProxy>(compList
     .size() + varList.size());
    list.addAll(compList.values());
    list.addAll(varList.values());
    final ModuleProxy createModuleProxy = mModuleFactory.createModuleProxy(
     system.getName(), null, null, null, mGlobalEvents, null, list);

    return createModuleProxy;
  }

  private VariableComponentProxy getVariableComponent(
   final SimpleEFAVariable variable)
  {
    IdentifierProxy iden = 
     (IdentifierProxy) mCloner.getClone(variable.getVariableName());
    final CompiledRange range = variable.getRange();
    final SimpleExpressionProxy type =
     range.createExpression(mSubjectFactory, mOperatorTable);

    final SimpleExpressionProxy initialStatePredicate =
     (SimpleExpressionProxy) mCloner.getClone(variable.
     getInitialStatePredicate());
    final Collection<VariableMarkingProxy> variableMarkings =
     mCloner.getClonedList(variable.getVariableMarkings());

    return mSubjectFactory.createVariableComponentProxy(iden,
                                                        type,
                                                        variable.isDeterministic(),
                                                        initialStatePredicate,
                                                        variableMarkings);
  }

  private SimpleComponentProxy getSimpleComponent(
   final SimpleEFAComponent efaComponent)
  {
    final List<SimpleNodeProxy> nodes = efaComponent.getNodeList();
    final ListBufferTransitionRelation rel =
     efaComponent.getTransitionRelation();
    final SimpleEFATransitionLabelEncoding efaEvent =
     efaComponent.getTransitionLabelEncoding();
    final String name = rel.getName();
    final boolean isMarkingIsUsed = 
     rel.isUsedProposition(SimpleEFACompiler.DEFAULT_MARKING_ID);
    final boolean isForbiddenIsUsed = 
     rel.isUsedProposition(SimpleEFACompiler.DEFAULT_FORBIDDEN_ID);
    final int numStates = rel.getNumberOfStates();
    final List<SimpleNodeProxy> nodeList =
     new ArrayList<SimpleNodeProxy>(numStates);
    int numOfMarkingState = 0;
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked = 
       rel.isMarked(i, SimpleEFACompiler.DEFAULT_MARKING_ID);
      final boolean isForbidden = 
       rel.isMarked(i, SimpleEFACompiler.DEFAULT_FORBIDDEN_ID);
      final List<SimpleIdentifierProxy> identList = 
       new ArrayList<SimpleIdentifierProxy>();
      if (isMarkingIsUsed && isMarked) {
        numOfMarkingState++;
        final SimpleIdentifierProxy ident =
         mModuleFactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_MARKING_NAME);
        identList.add(ident);
      }
      if (isForbiddenIsUsed && isForbidden) {
        final SimpleIdentifierProxy ident =
         mModuleFactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
        identList.add(ident);
      }
      final PlainEventListProxy props = 
       identList.isEmpty() ? null : 
        mModuleFactory.createPlainEventListProxy(identList);
      final String nodeName;
      if (nodes == null) {
        nodeName = "S" + i;
      } else {
        final SimpleNodeProxy nodeFromEFA = nodes.get(i);
        nodeName = nodeFromEFA.getName();
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
      final int eventId = iter.getCurrentEvent();
      final int source = iter.getCurrentSourceState();
      final int target = iter.getCurrentTargetState();
      final SimpleEFATransitionLabel label =
       efaEvent.getTransitionLabel(eventId);
      final ConstraintList condition = label.getConstraint();
      final List<SimpleIdentifierProxy> identList =
       new ArrayList<SimpleIdentifierProxy>();

      for (final SimpleEFAEventDecl e : label.getEvents()) {
        final SimpleIdentifierProxy ident =
         mModuleFactory.createSimpleIdentifierProxy(e.getName());
        identList.add(ident);
      }
      final GuardActionBlockProxy guardActionBlock = createGuard(condition);
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

    return mModuleFactory
     .createSimpleComponentProxy(ident, rel.getKind(), graph);
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
          guard = mModuleFactory.createBinaryExpressionProxy(op, guard,
                                                             constraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards =
       Collections.singletonList(guard);
      return mModuleFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }

  @SuppressWarnings("unused")
  private List<ComponentSubject> getComponentSubjects(final List<Proxy> list)
  {

    final List<ComponentSubject> result = new ArrayList<ComponentSubject>();
    for (final Proxy comp : list) {
      final ComponentSubject sbj = (ComponentSubject) mCloner.getClone(comp);
      result.add(sbj);
    }
    return result;
  }

  public Collection<EventDeclSubject> getEventDeclSubjects(
   final Collection<SimpleEFAEventDecl> list)
  {
    final Collection<EventDeclSubject> decls =
     new THashSet<EventDeclSubject>(list.size());
    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    for (final SimpleEFAEventDecl e : list) {
      final IdentifierProxy identifier =
       factory.createSimpleIdentifierProxy(e.getName());
      final EventDeclSubject event = new EventDeclSubject(identifier,
                                                          e.getKind(),
                                                          e.isObservable(),
                                                          ScopeKind.LOCAL,
                                                          e.getRanges(),
                                                          null,
                                                          null);
      decls.add(event.clone());
    }
    return decls;
  }
  
  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mModuleFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleSubjectFactory mSubjectFactory;
  private final ModuleProxyCloner mCloner;
  private final Collection<EventDeclSubject> mGlobalEvents;
  
}
