//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AttributeMapSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;


/**
 * @author Mohammad Reza Shoaei
 */

public class SimpleEFAHelper {

  /**
   * Creates an EFA helper instance based on the given factory and
   * operator table.
   */
  public SimpleEFAHelper(final ModuleProxyFactory factory,
                         final CompilerOperatorTable optable) {
    super();
    mFactory = factory;
    mOperatorTable = optable;
    mCloner = ModuleSubjectFactory.getCloningInstance();
  }

  /**
   * Creates an EFA helper instance based on the given factory and
   * the standard {@link CompilerOperatorTable}.
   */
  public SimpleEFAHelper(final ModuleProxyFactory factory)
  {
    this(factory, OPTABLE);
  }

  /**
   * Creates an EFA helper instance based on the standard
   * {@link ModuleSubjectFactory} and {@link CompilerOperatorTable}.
   */
  public SimpleEFAHelper()
  {
    this(FACTORY, OPTABLE);
  }

  public CompilerOperatorTable getOperatorTable()
  {
    return mOperatorTable;
  }

  public Collection<EventDeclProxy> getEventDeclProxy(final Collection<SimpleEFAEventDecl> list)
  {
    final Collection<EventDeclProxy> decls = new THashSet<>(list.size());
    for (final SimpleEFAEventDecl e : list) {
      final IdentifierProxy identifier = mFactory.createSimpleIdentifierProxy(e.getName());
      final EventDeclProxy event = mFactory.createEventDeclProxy(identifier,
                                                                 e.getKind(),
                                                                 e.isObservable(),
                                                                 ScopeKind.LOCAL,
                                                                 e.getRanges(),
                                                                 null,
                                                                 null);
      decls.add(event);
    }
    return decls;
  }

  public GuardActionBlockProxy createGuardActionBlock(
   final ConstraintList constraints,
   final CompilerOperatorTable op)
  {
    if (constraints.isTrue()) {
      return null;
    } else {
      final BinaryOperator bop = op.getAndOperator();
      SimpleExpressionProxy guard = null;
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        final SimpleExpressionSubject subjectConstraint =
         (SimpleExpressionSubject) mCloner.getClone(constraint);
        if (guard == null) {
          guard = subjectConstraint;
        } else {
          guard = mFactory.createBinaryExpressionProxy(bop, guard, subjectConstraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards = Collections.singletonList(guard);
      return mFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }

  public SimpleEFAStateEncoding getStateEncoding(final ListBufferTransitionRelation rel)
  {
    final SimpleEFAStateEncoding encoding = new SimpleEFAStateEncoding(rel.getNumberOfStates());
    final int numStates = rel.getNumberOfStates();
    for (int i = 0; i < numStates; i++) {
      if (!rel.isReachable(i)) {
        continue;
      }
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked = rel.isMarked(i, DEFAULT_MARKING_ID);
      final boolean isForbidden = rel.isMarked(i, DEFAULT_FORBIDDEN_ID);
      final String nodeName = "S" + i;
      final SimpleNodeProxy state = getSimpleNodeSubject(nodeName, isInitial, isMarked, isForbidden,
                                                         null);
      encoding.createSimpleStateId(state);
    }
    return encoding;
  }

  public SimpleNodeProxy getSimpleNodeSubject(final String name, final boolean isInitial, final boolean isMarked,
                                              final boolean isForbbiden, final Map<String, String> attributes)
  {
    return new SimpleNodeSubject(name, createPropositions(isMarked, isForbbiden), attributes,
                                 isInitial, null, null, null);
  }

  private PlainEventListProxy createPropositions(final boolean isMarked, final boolean isForbbiden)
  {
    final List<Proxy> list = new ArrayList<>();
    if (isForbbiden) {
      list.add(mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME));
    }
    if (isMarked) {
      list.add(mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));
    }
    if (list.isEmpty()) {
      return null;
    } else {
      return mFactory.createPlainEventListProxy(list);
    }
  }

  public ModuleProxy getModuleProxy(final SimpleEFASystem system)
  {
    final List<SimpleEFAVariable> variableList = system.getVariables();
    final List<SimpleEFAComponent> comps = system.getComponents();
    final Collection<EventDeclProxy> decls = getEventDeclProxy(system.getEventEncoding()
     .getEventDeclListExceptTau());
    final TreeMap<String, SimpleComponentProxy> compList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final TreeMap<String, VariableComponentProxy> varList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (final SimpleEFAComponent comp : comps) {
      compList.put(comp.getName(), getSimpleComponentProxy(comp));
    }
    for (final SimpleEFAVariable variable : variableList) {
      varList.put(variable.getName(), variable.getVariableComponent(mFactory));
    }
    final List<ComponentProxy> list = new ArrayList<>(compList.size() + varList.size());
    list.addAll(compList.values());
    list.addAll(varList.values());

    return mFactory.createModuleProxy(system.getName(), null, null, null, decls, null, list);
  }

  public SimpleComponentProxy getSimpleComponentProxy(final SimpleEFAComponent component)
  {
    return getSimpleComponentProxy(component.getName(), component.getTransitionRelation(), component
     .getTransitionLabelEncoding(), component.getStateEncoding(), component.getKind(), component
     .getBlockedEvents());
  }

  public SimpleComponentProxy getSimpleComponentProxy(final String name, final ListBufferTransitionRelation rel,
                                                      final SimpleEFALabelEncoding labelEncoding,
                                                      SimpleEFAStateEncoding stateEncoding,
                                                      final ComponentKind kind,
                                                      final int[] blockedLabels)
  {
    if (stateEncoding == null) {
      stateEncoding = getStateEncoding(rel);
    }
    final TIntObjectHashMap<NodeProxy> nodeMap = new TIntObjectHashMap<>();
    final List<EdgeProxy> edgeList = new ArrayList<>(rel.getNumberOfTransitions());
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int label = iter.getCurrentEvent();
      final int source = iter.getCurrentSourceState();
      final int target = iter.getCurrentTargetState();
      final ConstraintList condition = labelEncoding.getConstraintByLabelId(label);
      final List<SimpleIdentifierProxy> identList = new ArrayList<>();
      final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(labelEncoding
       .getEventDeclByLabelId(label).getName());
      identList.add(ident);
      final GuardActionBlockProxy guardActionBlock = createGuardActionBlock(condition, OPTABLE);
      final LabelBlockProxy block = mFactory.createLabelBlockProxy(identList, null);
      final SimpleNodeProxy sourceNode = (SimpleNodeProxy) mCloner.getClone(stateEncoding.getSimpleState(source));
      final SimpleNodeProxy targetNode = (SimpleNodeProxy) mCloner.getClone(stateEncoding.getSimpleState(target));
      if (!nodeMap.contains(source)) {
        nodeMap.put(source, sourceNode);
      }
      if (!nodeMap.contains(target)) {
        nodeMap.put(target, targetNode);
      }

      final EdgeProxy edge = mFactory.createEdgeProxy(sourceNode, targetNode, block,
                                                      guardActionBlock, null, null, null);
      edgeList.add(edge);
    }

//    final boolean isMarkingIsUsed = rel.isUsedProposition(SimpleEFAHelper.DEFAULT_MARKING_ID);

    LabelBlockProxy markingBlock = null;
    final List<SimpleIdentifierProxy> identList = new ArrayList<>();
    final Collection<SimpleEFAEventDecl> blockedEvents = new ArrayList<>();
    for (final int block : blockedLabels) {
      blockedEvents.add(labelEncoding.getEventDecl(block));
    }
    if (!blockedEvents.isEmpty()) {
      for (final SimpleEFAEventDecl e : blockedEvents) {
        identList.add(mFactory.createSimpleIdentifierProxy(e.getName()));
      }
    }
    /*
     if (isMarkingIsUsed && numOfMarkingState < 1) {      final SimpleIdentifierProxy marking = mFactory.createSimpleIdentifierProxy(
       EventDeclProxy.DEFAULT_MARKING_NAME);
      identList.add(marking);
    }
     */
    if (!identList.isEmpty()) {
      markingBlock = mFactory.createLabelBlockProxy(identList, null);
    }
    final GraphProxy graph = mFactory.createGraphProxy(true, markingBlock,
                                                       nodeMap.valueCollection(),
                                                       edgeList);
    final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name);

    return mFactory.createSimpleComponentProxy(ident, kind, graph);
  }

  public boolean containsMarkingProposition(final EventListExpressionProxy list)
  {
    if (list == null) {
      return false;
    }
    return list.toString().contains(EventDeclProxy.DEFAULT_MARKING_NAME);
  }

  public boolean containsForbiddenProposition(final EventListExpressionProxy list)
  {
    if (list == null) {
      return false;
    }
    return list.toString().contains(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
  }

  public SimpleIdentifierProxy getMarkingIdentifier()
  {
    return mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
  }

  public SimpleIdentifierProxy getForbiddenIdentifier()
  {
    return mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
  }

  public EventDeclProxy getTAUDecl()
  {
    final String name = "tau:";
    final SimpleIdentifierProxy iden = mFactory.createSimpleIdentifierProxy(name);
    return mFactory.createEventDeclProxy(iden, EventKind.CONTROLLABLE, false,
                                         ScopeKind.LOCAL, null, null, null);
  }

  public SimpleEFAVariable getSimpleEFAVariable(
   final VariableComponentProxy comp,
   final CompiledRange range)
  {
    final VariableComponentProxy cloneVar = (VariableComponentProxy) mCloner.getClone(comp);
    return new SimpleEFAVariable(cloneVar, range, mFactory, mOperatorTable);
  }

  public List<SimpleExpressionProxy> parseString(final String str,
                                                 final String opening,
                                                 final String closing)
  {
    final List<SimpleExpressionProxy> exps = new ArrayList<>();
    final Pattern pattern = Pattern.compile(opening + "(.*?)" + closing);
    final Matcher matcher = pattern.matcher(str);
    while (matcher.find()) {
      matcher.start();
      final String exp = matcher.group(1);
      exps.addAll(parse(exp));
    }
    return exps;
  }

  public List<SimpleExpressionProxy> parse(final String... str)
  {
    final ExpressionParser parser = new ExpressionParser(mFactory, mOperatorTable);
    final List<SimpleExpressionProxy> exps = new ArrayList<>();
    for (final String s : str) {
      try {
        exps.add(parser.parse(s));
      } catch (final ParseException ignored) {
    	  return null;
      }
    }
    return exps;
  }

  public List<SimpleExpressionProxy> parse(final String str,
                                           final String seperator)
  {
    final ExpressionParser parser = new ExpressionParser(mFactory, mOperatorTable);
    final List<SimpleExpressionProxy> exps = new ArrayList<>();
    final String[] values = str.split(seperator);
    for (final String s : values) {
      try {
        exps.add(parser.parse(s));
      } catch (final ParseException ignored) {
    	  return null;
      }
    }
    return exps;
  }

  private Collection<EventDeclSubject> getEventSubject(final List<EventDeclProxy> list)
  {
    final int size = list.size();
    final Collection<EventDeclSubject> result = new ArrayList<>(size);
    for (final Proxy item : list) {
      result.add((EventDeclSubject) mCloner.getClone(item));
    }
    return result;
  }

  public SimpleExpressionProxy createExpression(final List<SimpleExpressionProxy> exp,
                                                final BinaryOperator operator)
  {
    final ListIterator<SimpleExpressionProxy> iter = exp.listIterator(exp.size());
    if (iter.hasPrevious()) {
      SimpleExpressionProxy result = iter.previous();
      while (iter.hasPrevious()) {
        final SimpleExpressionProxy previous = iter.previous();
        result = mFactory.createBinaryExpressionProxy(operator, previous, result);
      }
      return result;
    } else {
      return null;
    }
  }

  public void importToIDE(final ModuleWindowInterface root, final ModuleSubject oModule,
                          final List<ComponentProxy> list)
   throws IOException, UnsupportedFlavorException
  {
    final List<ComponentProxy> componentList = mCloner.getClonedList(list);
    final SelectionOwner panel = root.getComponentsPanel();
    final InstanceSubject template = new InstanceSubject(new SimpleIdentifierSubject(""), "");
    final Transferable transfer = WatersDataFlavor.createTransferable(template);
    final Object position = panel.getInsertInfo(transfer).get(0).getInsertPosition();
    final List<InsertInfo> nList = new ArrayList<>();
    for (int i = componentList.size() - 1; i >= 0; i--) {
      final InsertInfo insert = new InsertInfo(componentList.get(i), position);
      nList.add(insert);
    }
    panel.insertItems(nList);
    panel.clearSelection(true);
  }

  public void importToIDE(final ModuleWindowInterface root, final ModuleProxy nModule,
                          final ModuleSubject oModule)
   throws IOException, UnsupportedFlavorException
  {
    final List<Proxy> componentList = mCloner.getClonedList(nModule.getComponentList());

    oModule.getEventDeclListModifiable().clear();
    final Collection<EventDeclSubject> events = getEventSubject(nModule.getEventDeclList());
    oModule.getEventDeclListModifiable().addAll(events);

    final SelectionOwner panel = root.getComponentsPanel();

    final List<InsertInfo> oList = new LinkedList<>();
    final List<InsertInfo> deletionVictims = panel.getDeletionVictims(panel.getAllSelectableItems());
    oList.addAll(deletionVictims);
    final Command deleteCommand = new DeleteCommand(oList, panel);
    root.getUndoInterface().executeCommand(deleteCommand);
    final InstanceSubject template = new InstanceSubject(new SimpleIdentifierSubject(""), "");
    final Transferable transfer = WatersDataFlavor.createTransferable(template);
    final List<InsertInfo> tInserts = panel.getInsertInfo(transfer);
    final InsertInfo tInsert = tInserts.get(0);
    final Object position = tInsert.getInsertPosition();
    final List<InsertInfo> nList = new ArrayList<>();
    for (int i = componentList.size() - 1; i >= 0; i--) {
      final InsertInfo insert = new InsertInfo(componentList.get(i), position);
      nList.add(insert);
    }
    final Command insertCommand = new InsertCommand(nList, panel, root);
    root.getUndoInterface().executeCommand(insertCommand);
    panel.clearSelection(false);
  }

  public static Collection<SimpleEFAVariable> getStateVariables(final String value,
                                                                final Collection<SimpleEFAVariable> vars)
  {
    final Collection<SimpleEFAVariable> list = new THashSet<>();
    if (value != null && !value.isEmpty() && !vars.isEmpty()) {
      for (final SimpleEFAVariable var : vars) {
        if (value.contains(var.getName())) {
          list.add(var);
        }
      }
    }
    return list;
  }

  public static String printer(final List<SimpleExpressionProxy> exps,
                               final String opening,
                               final String separator,
                               final String closing)
  {
    final StringBuilder result = new StringBuilder();
    result.append(opening);
    for (final SimpleExpressionProxy exp : exps) {
      result.append(exp.toString());
      result.append(separator);
    }
    result.delete(result.length() - separator.length(), result.length());
    result.append(closing);
    return result.toString();
  }

  public static String printer(final ConstraintList constraints,
                               final String opening,
                               final String separator,
                               final String closing)
  {
    return printer(constraints.getConstraints(), opening, separator, closing);
  }

  public static String printer(final ConstraintList constraints, final String separator)
  {
    return printer(constraints, "", separator, "");
  }

  public static String printer(final ConstraintList constraints)
  {
    return printer(constraints, DEFAULT_VALUE_SEPARATOR);
  }

  public static String printer(final List<SimpleExpressionProxy> exps, final String separator)
  {
    return printer(exps, "", separator, "");
  }

  public static void merge(final AttributeMapSubject attribute1,
                           final AttributeMapSubject attribute2,
                           final String separator)
  {
    if ((attribute1 == null) || (attribute2 == null) || attribute2.isEmpty()) {
      return;
    }
    if (attribute1.isEmpty()) {
      attribute1.putAll(attribute2);
      return;
    }
    for (final String key2 : attribute2.keySet()) {
      final String val2 = attribute2.get(key2);
      if (val2 == null || val2.isEmpty()) {
        continue;
      }
      mergeToAttribute(attribute1, key2, val2, separator);
    }
  }

  public static void mergeToAttribute(final AttributeMapSubject attribute, final String key,
                                      final String value, final String separator)
  {
    final String oVal = attribute.get(key);
    String nVal = "";
    if (oVal != null) {
      final TreeSet<String> values = new TreeSet<>(Arrays.asList(oVal.split(separator)));
      values.addAll(Arrays.asList(value.split(separator)));
      for (final String v : values) {
        nVal += v + separator;
      }
      nVal = nVal.substring(0, nVal.length() - separator.length());
    } else {
      nVal = value;
    }
    attribute.put(key, nVal);
  }

  public static ConstraintList merge(final ConstraintList con1, final ConstraintList con2)
  {
    if (con1.isTrue()) {
      return con2;
    }
    if (con2.isTrue()) {
      return con1;
    }
    final List<SimpleExpressionProxy> con = new ArrayList<>(con1.getConstraints());
    con.addAll(con2.getConstraints());
    return new ConstraintList(con);
  }

  public static ConstraintList getFalseConstraint()
  {
    return new ConstraintList(getFalseExpression());
  }

  public static List<SimpleExpressionProxy> getFalseExpression()
  {
    return Collections.singletonList((SimpleExpressionProxy) new IntConstantSubject(0));
  }

  public static SimpleEFAEventDecl getSubjectTAUDecl()
  {
    final EventDeclSubject ev
     = FACTORY.createEventDeclProxy(FACTORY.createSimpleIdentifierProxy(DEFAULT_TAU_NAME),
                                    EventKind.CONTROLLABLE, false,
                                    ScopeKind.LOCAL, null, null, null);
    return new SimpleEFAEventDecl(ev);
  }

  public static IdentifierProxy getSimpleIdentifierSubject(final String str)
  {
    return FACTORY.createSimpleIdentifierProxy(str);
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleProxyCloner mCloner;
  public static final String DEFAULT_MARKINGEVENT_NAME = ":vm";
  public static final String DEFAULT_TAU_NAME = ":tau";
  public static final String DEFAULT_STATEVALUE_KEY = "PE:";
  public static final String DEFAULT_SPEC_KEY = "SPEC:";
  public static final String DEFAULT_STATE_NAME = "S";
  public static final String DEFAULT_OPENING_STRING = "(";
  public static final String DEFAULT_CLOSING_STRING = ")";
  public static final String DEFAULT_VALUE_SEPARATOR = ",";
  public static final String DEFAULT_SPEC_TO = "#";
  public static final String DEFAULT_SPEC_LOCGUARD = "_";
  public static final ModuleSubjectFactory FACTORY = ModuleSubjectFactory.getInstance();
  public static final CompilerOperatorTable OPTABLE = CompilerOperatorTable.getInstance();

  public static final int DEFAULT_MARKING_ID = 0;
  public static final int DEFAULT_FORBIDDEN_ID = 1;

}
