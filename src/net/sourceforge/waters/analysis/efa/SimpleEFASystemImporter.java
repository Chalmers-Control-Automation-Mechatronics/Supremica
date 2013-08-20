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
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.module.ScopeKind;
import org.supremica.gui.ide.actions.IDEActionInterface;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;

/**
 * A utility to import a SimpleEFASystem to the editor or getting a module
 * containing this system
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
   * Creating a module containing this system.
   * <p/>
   * @param system An EFA system
   * <p/>
   * @return A module containing the EFA component, variables, and system
   *         events.
   */
  public ModuleProxy importModule(final SimpleEFASystem system)
  {
    return createModule(system);
  }

  /**
   * An importer to the editor. Note that, all the current components and events
   * will be replace by the new ones.
   * <p/>
   * @param system The EFA system
   * @param ide    The IDE interface
   */
  public void importToIDE(final SimpleEFASystem system,
                          final IDEActionInterface ide)
   throws IOException, UnsupportedFlavorException
  {
    final ModuleProxy mModule = createModule(system);

    final List<ComponentSubject> componentList =
     getComponentSubjects(mModule.getComponentList());
    final ModuleSubject oModule =
     ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

    oModule.getEventDeclListModifiable().clear();
    oModule.getEventDeclListModifiable().addAll(mGlobalEvents);

    final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
     getActiveDocumentContainer().getActivePanel();
    final SelectionOwner panel = root.getComponentsPanel();

    final List<InsertInfo> oList = new LinkedList<InsertInfo>();
    final List<InsertInfo> deletionVictims = panel.getDeletionVictims(panel.
     getAllSelectableItems());
    oList.addAll(deletionVictims);
    final Command deleteCommand = new DeleteCommand(oList, panel);
    root.getUndoInterface().executeCommand(deleteCommand);
    final InstanceSubject template =
     new InstanceSubject(new SimpleIdentifierSubject(""), "");
    final Transferable transfer =
     WatersDataFlavor.createTransferable(template);
    final List<InsertInfo> tInserts = panel.getInsertInfo(transfer);
    final InsertInfo tInsert = tInserts.get(0);
    final Object position = tInsert.getInsertPosition();
    final List<InsertInfo> nList = new ArrayList<InsertInfo>();
    for (int i = componentList.size() - 1; i >= 0; i--) {
      final InsertInfo insert = new InsertInfo(componentList.get(i),
                                               position);
      nList.add(insert);
    }
    final Command insertCommand = new InsertCommand(nList, panel, root);
    root.getUndoInterface().executeCommand(insertCommand);
    panel.clearSelection(false);
  }

  /**
   * Creating a module representing the system
   * <p/>
   * @param system An EFA system
   * <p/>
   * @return A module proxy
   */
  private ModuleProxy createModule(final SimpleEFASystem system)
  {
    final List<SimpleEFAVariable> variableList = system.getVariables();
    final List<SimpleEFAComponent> comps =
     system.getTransitionRelations();
    final int numComponents = variableList.size() + comps.size();
    mGlobalEvents.addAll(getEventDeclSubjects(system.getSystemEvents()));
    final List<SimpleComponentProxy> compList =
     new ArrayList<SimpleComponentProxy>(numComponents);

    final List<VariableComponentProxy> varList =
     new ArrayList<VariableComponentProxy>(numComponents);

    for (final SimpleEFAComponent comp : comps) {
      importComponents(compList, comp);
    }

    for (final SimpleEFAVariable variable : variableList) {
      importVariable(varList, variable);
    }

    Collections.sort(compList, comparator);
    Collections.sort(varList, comparator);
    final List<ComponentProxy> list = new ArrayList<ComponentProxy>();
    list.addAll(compList);
    list.addAll(varList);
    final ModuleProxy createModuleProxy = mModuleFactory.createModuleProxy(
     system.getName(), null, null, null, mGlobalEvents, null, list);
    return createModuleProxy;
  }

  private void importVariable(final List<VariableComponentProxy> compList,
                              final SimpleEFAVariable variable)
  {

    final String variableName = variable.getName();
    final SimpleIdentifierProxy identifier =
     mSubjectFactory.createSimpleIdentifierProxy(variableName);
    final CompiledRange range = variable.getRange();
    final SimpleExpressionProxy type =
     range.createExpression(mSubjectFactory, mOperatorTable);

    final SimpleExpressionProxy initialStatePredicate =
     (SimpleExpressionProxy) mCloner.getClone(variable.
     getInitialStatePredicate());
    final Collection<VariableMarkingProxy> variableMarkings =
     mCloner.getClonedList(variable.getVariableMarkings());
    final VariableComponentProxy var =
     mSubjectFactory.createVariableComponentProxy(identifier,
                                                  type,
                                                  variable.isDeterministic(),
                                                  initialStatePredicate,
                                                  variableMarkings);
    compList.add(var);
  }

  private void importComponents(final List<SimpleComponentProxy> compList,
                                final SimpleEFAComponent efaComponent)
  {
    final List<SimpleNodeProxy> nodes = efaComponent.getNodeList();
    final ListBufferTransitionRelation rel =
     efaComponent.getTransitionRelation();
    final SimpleEFATransitionLabelEncoding efaEvent =
     efaComponent.getTransitionLabelEncoding();
    final String name = rel.getName();
    // Should marking ID be zero?
    final boolean isMarkingIsUsed = rel.isUsedProposition(0);
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
         mModuleFactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_MARKING_NAME);
        final List<SimpleIdentifierProxy> identList =
         Collections.singletonList(ident);
        props = mModuleFactory.createPlainEventListProxy(identList);
      }
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
          guard = mModuleFactory.createBinaryExpressionProxy(op, guard,
                                                             constraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards =
       Collections.singletonList(guard);
      return mModuleFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }

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
  private final Comparator<ComponentProxy> comparator =
   new Comparator<ComponentProxy>()
  {
    @Override
    public int compare(final ComponentProxy c1, final ComponentProxy c2)
    {
      return c2.compareTo(c2); // sorting by name
    }
  };
  
}
