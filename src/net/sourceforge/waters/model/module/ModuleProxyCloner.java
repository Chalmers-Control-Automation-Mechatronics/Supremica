//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# THIS FILE HAS BEEN AUTOMATICALLY GENERATED BY A SCRIPT.
//# DO NOT EDIT.
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.module;

import gnu.trove.set.hash.THashSet;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;


/**
 * A tool to create deep copies of Module objects.
 * Parameterised by a factory, this visitor can accept objects from
 * one {@link Proxy} implementation and translate them to another.
 *
 * @author Robi Malik
 */

public class ModuleProxyCloner
  extends DefaultModuleProxyVisitor
  implements ProxyCloner
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module proxy cloner that preserves geometry.
   * @param  factory  The factory used to create cloned objects.
   */
  public ModuleProxyCloner(final ModuleProxyFactory factory)
  {
    this(factory, true);
  }

  /**
   * Creates a new module proxy cloner that optionally preserves geometry.
   * @param  factory  The factory used to create cloned objects.
   * @param  geo      Whether cloned objects retain geometry information.
   */
  public ModuleProxyCloner(final ModuleProxyFactory factory, boolean geo)
  {
    mFactory = factory;
    mCloningGeometry = geo;
    mNodeMap = null;
  }


  //#########################################################################
  //# Invocation
  public Proxy getClone(final Proxy proxy)
  {
    if (proxy == null) {
      return null;
    } else {
      try {
        return cloneProxy(proxy);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }
  }

  public <P extends Proxy>
  List<P> getClonedList(final Collection<? extends P> collection)
  {
    final int size = collection.size();
    final List<Proxy> result = new ArrayList<>(size);
    for (final P proxy : collection) {
      final Proxy cloned = getClone(proxy);
      result.add(cloned);
    }
    final List<?> precast = result;
    @SuppressWarnings("unchecked")
    final List<P> cast = (List<P>) precast;
    return cast;
  }

  public <P extends Proxy>
  Set<P> getClonedSet(final Collection<? extends P> collection)
  {
    final int size = collection.size();
    final Set<Proxy> result = new THashSet<>(size);
    for (final P proxy : collection) {
      final Proxy cloned = getClone(proxy);
      result.add(cloned);
    }
    final Set<?> precast = result;
    @SuppressWarnings("unchecked")
    final Set<P> cast = (Set<P>) precast;
    return cast;
  }

  /**
   * Creates a clone of the given graph using two factories.
   * This methods creates a clone of a graph's nodes and edges using
   * the standard factory of this cloner, then creates a new graph 
   * using another factory given as an argument.
   * @param  proxy    The graph to be duplicated.
   * @param  factory  The factory used to create the new graph.
   * @return The cloned graph.
   */
  public GraphProxy getClonedGraph(final GraphProxy proxy,
                                   final ModuleProxyFactory factory)
  {
    final int size = proxy.getNodes().size();
    mNodeMap = new HashMap<>(size);
    try {
      final boolean deterministic = proxy.isDeterministic();
      final LabelBlockProxy blockedEvents0 = proxy.getBlockedEvents();
      final LabelBlockProxy blockedEvents =
        (LabelBlockProxy) cloneProxy(blockedEvents0);
      final Collection<NodeProxy> nodes0 = proxy.getNodes();
      final Collection<NodeProxy> nodes = lookupNodeProxyCollection(nodes0);
      final Collection<EdgeProxy> edges0 = proxy.getEdges();
      final Collection<EdgeProxy> edges = cloneProxyCollection(edges0);
      return factory.createGraphProxy(deterministic,
                                      blockedEvents,
                                      nodes,
                                      edges);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mNodeMap = null;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public BinaryExpressionProxy visitBinaryExpressionProxy
    (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final BinaryOperator operator = proxy.getOperator();
    final SimpleExpressionProxy left0 = proxy.getLeft();
    final SimpleExpressionProxy left =
      (SimpleExpressionProxy) cloneProxy(left0);
    final SimpleExpressionProxy right0 = proxy.getRight();
    final SimpleExpressionProxy right =
      (SimpleExpressionProxy) cloneProxy(right0);
    return mFactory.createBinaryExpressionProxy(plainText,
                                                operator,
                                                left,
                                                right);
  }

  @Override
  public BoxGeometryProxy visitBoxGeometryProxy
    (final BoxGeometryProxy proxy)
    throws VisitorException
  {
    final Rectangle2D rectangle = proxy.getRectangle();
    return mFactory.createBoxGeometryProxy(rectangle);
  }

  @Override
  public ColorGeometryProxy visitColorGeometryProxy
    (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    final Collection<Color> colorSet = proxy.getColorSet();
    return mFactory.createColorGeometryProxy(colorSet);
  }

  @Override
  public ConditionalProxy visitConditionalProxy
    (final ConditionalProxy proxy)
    throws VisitorException
  {
    final Collection<Proxy> body0 = proxy.getBody();
    final Collection<Proxy> body = cloneProxyCollection(body0);
    final SimpleExpressionProxy guard0 = proxy.getGuard();
    final SimpleExpressionProxy guard =
      (SimpleExpressionProxy) cloneProxy(guard0);
    return mFactory.createConditionalProxy(body,
                                           guard);
  }

  @Override
  public ConstantAliasProxy visitConstantAliasProxy
    (final ConstantAliasProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final ExpressionProxy expression0 = proxy.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) cloneProxy(expression0);
    final ScopeKind scope = proxy.getScope();
    return mFactory.createConstantAliasProxy(identifier,
                                             expression,
                                             scope);
  }

  @Override
  public EdgeProxy visitEdgeProxy
    (final EdgeProxy proxy)
    throws VisitorException
  {
    final NodeProxy source0 = proxy.getSource();
    final NodeProxy source = lookupNodeProxy(source0);
    final NodeProxy target0 = proxy.getTarget();
    final NodeProxy target = lookupNodeProxy(target0);
    final LabelBlockProxy labelBlock0 = proxy.getLabelBlock();
    final LabelBlockProxy labelBlock =
      (LabelBlockProxy) cloneProxy(labelBlock0);
    final GuardActionBlockProxy guardActionBlock0 =
      proxy.getGuardActionBlock();
    final GuardActionBlockProxy guardActionBlock =
      (GuardActionBlockProxy) cloneProxy(guardActionBlock0);
    final SplineGeometryProxy geometry;
    if (mCloningGeometry) {
      final SplineGeometryProxy geometry0 = proxy.getGeometry();
      geometry = (SplineGeometryProxy) cloneProxy(geometry0);
    } else {
      geometry = null;
    }
    final PointGeometryProxy startPoint;
    if (mCloningGeometry) {
      final PointGeometryProxy startPoint0 = proxy.getStartPoint();
      startPoint = (PointGeometryProxy) cloneProxy(startPoint0);
    } else {
      startPoint = null;
    }
    final PointGeometryProxy endPoint;
    if (mCloningGeometry) {
      final PointGeometryProxy endPoint0 = proxy.getEndPoint();
      endPoint = (PointGeometryProxy) cloneProxy(endPoint0);
    } else {
      endPoint = null;
    }
    return mFactory.createEdgeProxy(source,
                                    target,
                                    labelBlock,
                                    guardActionBlock,
                                    geometry,
                                    startPoint,
                                    endPoint);
  }

  @Override
  public EnumSetExpressionProxy visitEnumSetExpressionProxy
    (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final Collection<SimpleIdentifierProxy> items0 = proxy.getItems();
    final Collection<SimpleIdentifierProxy> items =
      cloneProxyCollection(items0);
    return mFactory.createEnumSetExpressionProxy(plainText,
                                                 items);
  }

  @Override
  public EventAliasProxy visitEventAliasProxy
    (final EventAliasProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final ExpressionProxy expression0 = proxy.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) cloneProxy(expression0);
    return mFactory.createEventAliasProxy(identifier,
                                          expression);
  }

  @Override
  public EventDeclProxy visitEventDeclProxy
    (final EventDeclProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final EventKind kind = proxy.getKind();
    final boolean observable = proxy.isObservable();
    final ScopeKind scope = proxy.getScope();
    final Collection<SimpleExpressionProxy> ranges0 = proxy.getRanges();
    final Collection<SimpleExpressionProxy> ranges =
      cloneProxyCollection(ranges0);
    final ColorGeometryProxy colorGeometry;
    if (mCloningGeometry) {
      final ColorGeometryProxy colorGeometry0 = proxy.getColorGeometry();
      colorGeometry = (ColorGeometryProxy) cloneProxy(colorGeometry0);
    } else {
      colorGeometry = null;
    }
    final Map<String,String> attributes = proxy.getAttributes();
    return mFactory.createEventDeclProxy(identifier,
                                         kind,
                                         observable,
                                         scope,
                                         ranges,
                                         colorGeometry,
                                         attributes);
  }

  @Override
  public ForeachProxy visitForeachProxy
    (final ForeachProxy proxy)
    throws VisitorException
  {
    final Collection<Proxy> body0 = proxy.getBody();
    final Collection<Proxy> body = cloneProxyCollection(body0);
    final String name = proxy.getName();
    final SimpleExpressionProxy range0 = proxy.getRange();
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) cloneProxy(range0);
    return mFactory.createForeachProxy(body,
                                       name,
                                       range);
  }

  @Override
  public FunctionCallExpressionProxy visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final String functionName = proxy.getFunctionName();
    final Collection<SimpleExpressionProxy> arguments0 = proxy.getArguments();
    final Collection<SimpleExpressionProxy> arguments =
      cloneProxyCollection(arguments0);
    return mFactory.createFunctionCallExpressionProxy(plainText,
                                                      functionName,
                                                      arguments);
  }

  @Override
  public GraphProxy visitGraphProxy
    (final GraphProxy proxy)
    throws VisitorException
  {
    final int size = proxy.getNodes().size();
    mNodeMap = new HashMap<>(size);
    try {
      final boolean deterministic = proxy.isDeterministic();
      final LabelBlockProxy blockedEvents0 = proxy.getBlockedEvents();
      final LabelBlockProxy blockedEvents =
        (LabelBlockProxy) cloneProxy(blockedEvents0);
      final Collection<NodeProxy> nodes0 = proxy.getNodes();
      final Collection<NodeProxy> nodes = lookupNodeProxyCollection(nodes0);
      final Collection<EdgeProxy> edges0 = proxy.getEdges();
      final Collection<EdgeProxy> edges = cloneProxyCollection(edges0);
      return mFactory.createGraphProxy(deterministic,
                                       blockedEvents,
                                       nodes,
                                       edges);
    } finally {
      mNodeMap = null;
    }
  }

  @Override
  public GroupNodeProxy visitGroupNodeProxy
    (final GroupNodeProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final PlainEventListProxy propositions0 = proxy.getPropositions();
    final PlainEventListProxy propositions =
      (PlainEventListProxy) cloneProxy(propositions0);
    final Map<String,String> attributes = proxy.getAttributes();
    final Collection<NodeProxy> immediateChildNodes0 =
      proxy.getImmediateChildNodes();
    final Collection<NodeProxy> immediateChildNodes =
      lookupNodeProxyCollection(immediateChildNodes0);
    final BoxGeometryProxy geometry;
    if (mCloningGeometry) {
      final BoxGeometryProxy geometry0 = proxy.getGeometry();
      geometry = (BoxGeometryProxy) cloneProxy(geometry0);
    } else {
      geometry = null;
    }
    return mFactory.createGroupNodeProxy(name,
                                         propositions,
                                         attributes,
                                         immediateChildNodes,
                                         geometry);
  }

  @Override
  public GuardActionBlockProxy visitGuardActionBlockProxy
    (final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    final Collection<SimpleExpressionProxy> guards0 = proxy.getGuards();
    final Collection<SimpleExpressionProxy> guards =
      cloneProxyCollection(guards0);
    final Collection<BinaryExpressionProxy> actions0 = proxy.getActions();
    final Collection<BinaryExpressionProxy> actions =
      cloneProxyCollection(actions0);
    final LabelGeometryProxy geometry;
    if (mCloningGeometry) {
      final LabelGeometryProxy geometry0 = proxy.getGeometry();
      geometry = (LabelGeometryProxy) cloneProxy(geometry0);
    } else {
      geometry = null;
    }
    return mFactory.createGuardActionBlockProxy(guards,
                                                actions,
                                                geometry);
  }

  @Override
  public IndexedIdentifierProxy visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final String name = proxy.getName();
    final Collection<SimpleExpressionProxy> indexes0 = proxy.getIndexes();
    final Collection<SimpleExpressionProxy> indexes =
      cloneProxyCollection(indexes0);
    return mFactory.createIndexedIdentifierProxy(plainText,
                                                 name,
                                                 indexes);
  }

  @Override
  public InstanceProxy visitInstanceProxy
    (final InstanceProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final String moduleName = proxy.getModuleName();
    final Collection<ParameterBindingProxy> bindingList0 =
      proxy.getBindingList();
    final Collection<ParameterBindingProxy> bindingList =
      cloneProxyCollection(bindingList0);
    return mFactory.createInstanceProxy(identifier,
                                        moduleName,
                                        bindingList);
  }

  @Override
  public IntConstantProxy visitIntConstantProxy
    (final IntConstantProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final int value = proxy.getValue();
    return mFactory.createIntConstantProxy(plainText,
                                           value);
  }

  @Override
  public LabelBlockProxy visitLabelBlockProxy
    (final LabelBlockProxy proxy)
    throws VisitorException
  {
    final Collection<Proxy> eventIdentifierList0 =
      proxy.getEventIdentifierList();
    final Collection<Proxy> eventIdentifierList =
      cloneProxyCollection(eventIdentifierList0);
    final LabelGeometryProxy geometry;
    if (mCloningGeometry) {
      final LabelGeometryProxy geometry0 = proxy.getGeometry();
      geometry = (LabelGeometryProxy) cloneProxy(geometry0);
    } else {
      geometry = null;
    }
    return mFactory.createLabelBlockProxy(eventIdentifierList,
                                          geometry);
  }

  @Override
  public LabelGeometryProxy visitLabelGeometryProxy
    (final LabelGeometryProxy proxy)
    throws VisitorException
  {
    final Point2D offset = proxy.getOffset();
    final AnchorPosition anchor = proxy.getAnchor();
    return mFactory.createLabelGeometryProxy(offset,
                                             anchor);
  }

  @Override
  public ModuleProxy visitModuleProxy
    (final ModuleProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final String comment = proxy.getComment();
    final Collection<ConstantAliasProxy> constantAliasList0 =
      proxy.getConstantAliasList();
    final Collection<ConstantAliasProxy> constantAliasList =
      cloneProxyCollection(constantAliasList0);
    final Collection<EventDeclProxy> eventDeclList0 =
      proxy.getEventDeclList();
    final Collection<EventDeclProxy> eventDeclList =
      cloneProxyCollection(eventDeclList0);
    final Collection<Proxy> eventAliasList0 = proxy.getEventAliasList();
    final Collection<Proxy> eventAliasList =
      cloneProxyCollection(eventAliasList0);
    final Collection<Proxy> componentList0 = proxy.getComponentList();
    final Collection<Proxy> componentList =
      cloneProxyCollection(componentList0);
    return mFactory.createModuleProxy(name,
                                      comment,
                                      null,
                                      constantAliasList,
                                      eventDeclList,
                                      eventAliasList,
                                      componentList);
  }

  @Override
  public ModuleSequenceProxy visitModuleSequenceProxy
    (final ModuleSequenceProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final String comment = proxy.getComment();
    final Collection<ModuleProxy> modules0 = proxy.getModules();
    final Collection<ModuleProxy> modules = cloneProxyCollection(modules0);
    return mFactory.createModuleSequenceProxy(name,
                                              comment,
                                              null,
                                              modules);
  }

  @Override
  public ParameterBindingProxy visitParameterBindingProxy
    (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final ExpressionProxy expression0 = proxy.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) cloneProxy(expression0);
    return mFactory.createParameterBindingProxy(name,
                                                expression);
  }

  @Override
  public PlainEventListProxy visitPlainEventListProxy
    (final PlainEventListProxy proxy)
    throws VisitorException
  {
    final Collection<Proxy> eventIdentifierList0 =
      proxy.getEventIdentifierList();
    final Collection<Proxy> eventIdentifierList =
      cloneProxyCollection(eventIdentifierList0);
    return mFactory.createPlainEventListProxy(eventIdentifierList);
  }

  @Override
  public PointGeometryProxy visitPointGeometryProxy
    (final PointGeometryProxy proxy)
    throws VisitorException
  {
    final Point2D point = proxy.getPoint();
    return mFactory.createPointGeometryProxy(point);
  }

  @Override
  public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final IdentifierProxy baseIdentifier0 = proxy.getBaseIdentifier();
    final IdentifierProxy baseIdentifier =
      (IdentifierProxy) cloneProxy(baseIdentifier0);
    final IdentifierProxy componentIdentifier0 =
      proxy.getComponentIdentifier();
    final IdentifierProxy componentIdentifier =
      (IdentifierProxy) cloneProxy(componentIdentifier0);
    return mFactory.createQualifiedIdentifierProxy(plainText,
                                                   baseIdentifier,
                                                   componentIdentifier);
  }

  @Override
  public SimpleComponentProxy visitSimpleComponentProxy
    (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final ComponentKind kind = proxy.getKind();
    final GraphProxy graph0 = proxy.getGraph();
    final GraphProxy graph = (GraphProxy) cloneProxy(graph0);
    final Map<String,String> attributes = proxy.getAttributes();
    return mFactory.createSimpleComponentProxy(identifier,
                                               kind,
                                               graph,
                                               attributes);
  }

  @Override
  public SimpleIdentifierProxy visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final String name = proxy.getName();
    return mFactory.createSimpleIdentifierProxy(plainText,
                                                name);
  }

  @Override
  public SimpleNodeProxy visitSimpleNodeProxy
    (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final PlainEventListProxy propositions0 = proxy.getPropositions();
    final PlainEventListProxy propositions =
      (PlainEventListProxy) cloneProxy(propositions0);
    final Map<String,String> attributes = proxy.getAttributes();
    final boolean initial = proxy.isInitial();
    final PointGeometryProxy pointGeometry;
    if (mCloningGeometry) {
      final PointGeometryProxy pointGeometry0 = proxy.getPointGeometry();
      pointGeometry = (PointGeometryProxy) cloneProxy(pointGeometry0);
    } else {
      pointGeometry = null;
    }
    final PointGeometryProxy initialArrowGeometry;
    if (mCloningGeometry) {
      final PointGeometryProxy initialArrowGeometry0 =
        proxy.getInitialArrowGeometry();
      initialArrowGeometry =
        (PointGeometryProxy) cloneProxy(initialArrowGeometry0);
    } else {
      initialArrowGeometry = null;
    }
    final LabelGeometryProxy labelGeometry;
    if (mCloningGeometry) {
      final LabelGeometryProxy labelGeometry0 = proxy.getLabelGeometry();
      labelGeometry = (LabelGeometryProxy) cloneProxy(labelGeometry0);
    } else {
      labelGeometry = null;
    }
    return mFactory.createSimpleNodeProxy(name,
                                          propositions,
                                          attributes,
                                          initial,
                                          pointGeometry,
                                          initialArrowGeometry,
                                          labelGeometry);
  }

  @Override
  public SplineGeometryProxy visitSplineGeometryProxy
    (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    final Collection<Point2D> points = proxy.getPoints();
    final SplineKind kind = proxy.getKind();
    return mFactory.createSplineGeometryProxy(points,
                                              kind);
  }

  @Override
  public UnaryExpressionProxy visitUnaryExpressionProxy
    (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    final String plainText;
    if (mCloningGeometry) {
      plainText = proxy.getPlainText();
    } else {
      plainText = null;
    }
    final UnaryOperator operator = proxy.getOperator();
    final SimpleExpressionProxy subTerm0 = proxy.getSubTerm();
    final SimpleExpressionProxy subTerm =
      (SimpleExpressionProxy) cloneProxy(subTerm0);
    return mFactory.createUnaryExpressionProxy(plainText,
                                               operator,
                                               subTerm);
  }

  @Override
  public VariableComponentProxy visitVariableComponentProxy
    (final VariableComponentProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier0 = proxy.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) cloneProxy(identifier0);
    final SimpleExpressionProxy type0 = proxy.getType();
    final SimpleExpressionProxy type =
      (SimpleExpressionProxy) cloneProxy(type0);
    final SimpleExpressionProxy initialStatePredicate0 =
      proxy.getInitialStatePredicate();
    final SimpleExpressionProxy initialStatePredicate =
      (SimpleExpressionProxy) cloneProxy(initialStatePredicate0);
    final Collection<VariableMarkingProxy> variableMarkings0 =
      proxy.getVariableMarkings();
    final Collection<VariableMarkingProxy> variableMarkings =
      cloneProxyCollection(variableMarkings0);
    return mFactory.createVariableComponentProxy(identifier,
                                                 type,
                                                 initialStatePredicate,
                                                 variableMarkings);
  }

  @Override
  public VariableMarkingProxy visitVariableMarkingProxy
    (final VariableMarkingProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy proposition0 = proxy.getProposition();
    final IdentifierProxy proposition =
      (IdentifierProxy) cloneProxy(proposition0);
    final SimpleExpressionProxy predicate0 = proxy.getPredicate();
    final SimpleExpressionProxy predicate =
      (SimpleExpressionProxy) cloneProxy(predicate0);
    return mFactory.createVariableMarkingProxy(proposition,
                                               predicate);
  }


  //#########################################################################
  //# Hooks
  protected Proxy cloneProxy(final Proxy orig)
    throws VisitorException
  {
    if (orig == null) {
      return orig;
    } else {
      return (Proxy) orig.acceptVisitor(this);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private NodeProxy lookupNodeProxy(final NodeProxy orig)
    throws VisitorException
  {
    if (mNodeMap == null) {
      return orig;
    } else {
      final String name = orig.getName();
      NodeProxy node = mNodeMap.get(name);
      if (node == null) {
        node = (NodeProxy) cloneProxy(orig);
        mNodeMap.put(name, node);
      }
      return node;
    }
  }

  private Collection<NodeProxy> lookupNodeProxyCollection
    (final Collection<? extends NodeProxy> orig)
    throws VisitorException
  {
    final Collection<NodeProxy> result = new LinkedList<>();
    for (final NodeProxy orignode : orig) {
      final NodeProxy resnode = lookupNodeProxy(orignode);
      result.add(resnode);
    }
    return result;
  }

  private <P extends Proxy>
  Collection<P> cloneProxyCollection(final Collection<P> orig)
    throws VisitorException
  {
    final Collection<Proxy> result = new LinkedList<>();
    for (final Proxy origelem : orig) {
      final Proxy reselem = cloneProxy(origelem);
      result.add(reselem);
    }
    final Collection<?> precast = result;
    @SuppressWarnings("unchecked")
    Collection<P> cast = (Collection<P>) precast;
    return cast;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final boolean mCloningGeometry;
  private Map<String,NodeProxy> mNodeMap;

}
