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
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.module.ScopeKind;

/**
 * A utility to pack a SimpleEFASystem into a module
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystemImporter
{

  /**
   *
   * @param factory Factory to be used for components construction
   * @param optable Compiler operator table to used for variable and guard
   *                expression construction
   */
  public SimpleEFASystemImporter(final ModuleProxyFactory factory,
                                 final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mCloner = ModuleSubjectFactory.getCloningInstance();
    mGlobalEvents = new THashSet<>();
  }

  /**
   * Using operation table {(@CompilerOperatorTable)} instances
   * @param factory Factory to be used for components construction
   */
  public SimpleEFASystemImporter(final ModuleProxyFactory factory)
  {
    this(factory, CompilerOperatorTable.getInstance());
  }

  /**
   * Using subject factory {(@ModuleSubjectFactory)} and 
   * operation table {(@CompilerOperatorTable)} instances
   */
  public SimpleEFASystemImporter()
  {
    this(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
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

  public VariableComponentProxy getVariableComponent(final SimpleEFAVariable variable)
  {
    IdentifierProxy iden =
     (IdentifierProxy) mCloner.getClone(variable.getVariableName());
    final CompiledRange range = variable.getRange();
    final SimpleExpressionProxy type =
     range.createExpression(mFactory, mOperatorTable);

    final SimpleExpressionProxy initialStatePredicate =
     (SimpleExpressionProxy) mCloner.getClone(variable.
     getInitialStatePredicate());
    final Collection<VariableMarkingProxy> variableMarkings =
     mCloner.getClonedList(variable.getVariableMarkings());

    return mFactory.createVariableComponentProxy(iden,
                                                 type,
                                                 variable.isDeterministic(),
                                                 initialStatePredicate,
                                                 variableMarkings);
  }

  /**
   * Constructing {@SimpleComponentProxy} from {@SimpleEFAComponent}
   * @param efaComponent The simple EFA component
   * @return The simple component proxy
   */
  public SimpleComponentProxy getSimpleComponent(
   final SimpleEFAComponent efaComponent)
  {
    final String name = efaComponent.getName();
    final List<SimpleNodeProxy> nodes = efaComponent.getLocationSet();
    final List<EdgeProxy> edgeList = efaComponent.getEdges();
    final boolean isMarkingIsUsed =
     efaComponent.getTransitionRelation()
     .isUsedProposition(SimpleEFACompiler.DEFAULT_MARKING_ID);
    int numOfMarkingState = efaComponent.getMarkedLocationSet().size();
    LabelBlockProxy markingBlock = null;
    final List<SimpleIdentifierProxy> identList = new ArrayList<>();
    Collection<SimpleEFAEventDecl> blockedEvents =
     efaComponent.getBlockedEvents();
    if(!blockedEvents.isEmpty()){
      for (SimpleEFAEventDecl e : blockedEvents){
        identList.add(mFactory.createSimpleIdentifierProxy(e.getName()));
      }
    }
    if (isMarkingIsUsed && numOfMarkingState < 1) {
      identList.add(getMarkingIdentifier());
    }
    if (!identList.isEmpty()){
      markingBlock = mFactory.createLabelBlockProxy(identList, null);
    }
    final GraphProxy graph =
     mFactory.createGraphProxy(false, markingBlock, nodes, edgeList);
    final SimpleIdentifierProxy ident =
     mFactory.createSimpleIdentifierProxy(name);

    return mFactory
     .createSimpleComponentProxy(ident, efaComponent.getKind(), graph);
  }

  //#########################################################################

  //# Auxiliary Method
  public GuardActionBlockProxy createGuard(
   final ConstraintList constraints)
  {
    if (constraints.isTrue()) {
      return null;
    } else {
      final BinaryOperator op = mOperatorTable.getAndOperator();
      SimpleExpressionProxy guard = null;
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        SimpleExpressionSubject subjectConstraint =
         (SimpleExpressionSubject) mCloner.getClone(constraint);
        if (guard == null) {
          guard = subjectConstraint;
        } else {
          guard =
           mFactory.createBinaryExpressionProxy(op, guard, subjectConstraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards =
       Collections.singletonList(guard);
      return mFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }
  public Collection<EventDeclProxy> getEventDeclProxy(final Collection<SimpleEFAEventDecl> list)
  {
    final Collection<EventDeclProxy> decls =
     new THashSet<>(list.size());
    for (final SimpleEFAEventDecl e : list) {
      final IdentifierProxy identifier =
       mFactory.createSimpleIdentifierProxy(e.getName());
      final EventDeclProxy event = mFactory.createEventDeclProxy(identifier,
                                                                 e.getKind(),
                                                                 e
       .isObservable(),
                                                                 ScopeKind.LOCAL,
                                                                 e.getRanges(),
                                                                 null,
                                                                 null);
      decls.add(event);
    }
    return decls;
  }

  public SimpleIdentifierProxy getMarkingIdentifier(
   )
  {
      return mFactory
       .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    
  }
  
  private ModuleProxy createModule(final SimpleEFASystem system){
    final List<SimpleEFAVariable> variableList = system.getVariables();
    final List<SimpleEFAComponent> comps =
     system.getComponents();
    mGlobalEvents.addAll(getEventDeclProxy(system.getSystemEvents()));
    final TreeMap<String, SimpleComponentProxy> compList =
     new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final TreeMap<String, VariableComponentProxy> varList =
     new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (final SimpleEFAComponent comp : comps) {
      compList.put(comp.getName(), getSimpleComponent(comp));
    }
    for (final SimpleEFAVariable variable : variableList) {
      varList.put(variable.getName(), getVariableComponent(variable));
    }
    final List<ComponentProxy> list = new ArrayList<>(compList
     .size() + varList.size());
    list.addAll(compList.values());
    list.addAll(varList.values());
    
    return mFactory.createModuleProxy(
     system.getName(), null, null, null, mGlobalEvents, null, list);
  }
  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleProxyCloner mCloner;
  private final Collection<EventDeclProxy> mGlobalEvents;
}
