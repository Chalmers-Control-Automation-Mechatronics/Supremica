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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.AbstractHashCodeVisitor;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;


/**
 * A visitor to compute hash code for Module objects based on
 * their contents. The ModuleHashCodeVisitor can be parameterised to respect
 * or not to respect geometry information.
 *
 * @author Robi Malik
 */

public class ModuleHashCodeVisitor
  extends AbstractHashCodeVisitor
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static ModuleHashCodeVisitor getInstance(final boolean geo)
  {
    if (geo) {
      return SingletonHolderWithGeometry.INSTANCE;
    } else {
      return SingletonHolderWithoutGeometry.INSTANCE;
    }
  }

  private ModuleHashCodeVisitor(final boolean geo)
  {
    super (geo);
  }

  private static class SingletonHolderWithoutGeometry {
    private static final ModuleHashCodeVisitor INSTANCE =
      new ModuleHashCodeVisitor(false);
  }

  private static class SingletonHolderWithGeometry {
    private static final ModuleHashCodeVisitor INSTANCE =
      new ModuleHashCodeVisitor(true);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Integer visitAliasProxy
    (final AliasProxy proxy)
    throws VisitorException
  {
    int result = visitIdentifiedProxy(proxy);
    final ExpressionProxy expression = proxy.getExpression();
    result *= 5;
    result += computeProxyHashCode(expression);
    return result;
  }

  public Integer visitBinaryExpressionProxy
    (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitSimpleExpressionProxy(proxy);
    final BinaryOperator operator = proxy.getOperator();
    result *= 5;
    result += computeOptionalHashCode(operator);
    final SimpleExpressionProxy left = proxy.getLeft();
    result *= 5;
    result += computeProxyHashCode(left);
    final SimpleExpressionProxy right = proxy.getRight();
    result *= 5;
    result += computeProxyHashCode(right);
    return result;
  }

  public Integer visitBoxGeometryProxy
    (final BoxGeometryProxy proxy)
    throws VisitorException
  {
    int result = visitGeometryProxy(proxy);
    final Rectangle2D rectangle = proxy.getRectangle();
    result *= 5;
    result += computeOptionalHashCode(rectangle);
    return result;
  }

  public Integer visitColorGeometryProxy
    (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    int result = visitGeometryProxy(proxy);
    final Set<Color> colorSet = proxy.getColorSet();
    result *= 5;
    result += computeOptionalHashCode(colorSet);
    return result;
  }

  public Integer visitComponentProxy
    (final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Integer visitConditionalProxy
    (final ConditionalProxy proxy)
    throws VisitorException
  {
    int result = visitNestedBlockProxy(proxy);
    final SimpleExpressionProxy guard = proxy.getGuard();
    result *= 5;
    result += computeProxyHashCode(guard);
    return result;
  }

  public Integer visitConstantAliasProxy
    (final ConstantAliasProxy proxy)
    throws VisitorException
  {
    int result = visitAliasProxy(proxy);
    final ScopeKind scope = proxy.getScope();
    result *= 5;
    result += computeOptionalHashCode(scope);
    return result;
  }

  public Integer visitEdgeProxy
    (final EdgeProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final NodeProxy source = proxy.getSource();
    result *= 5;
    result += computeRefHashCode(source);
    final NodeProxy target = proxy.getTarget();
    result *= 5;
    result += computeRefHashCode(target);
    final LabelBlockProxy labelBlock = proxy.getLabelBlock();
    result *= 5;
    result += computeProxyHashCode(labelBlock);
    final GuardActionBlockProxy guardActionBlock =
      proxy.getGuardActionBlock();
    result *= 5;
    result += computeProxyHashCode(guardActionBlock);
    if (isRespectingGeometry()) {
      final SplineGeometryProxy geometry = proxy.getGeometry();
      result *= 5;
      result += computeProxyHashCode(geometry);
      final PointGeometryProxy startPoint = proxy.getStartPoint();
      result *= 5;
      result += computeProxyHashCode(startPoint);
      final PointGeometryProxy endPoint = proxy.getEndPoint();
      result *= 5;
      result += computeProxyHashCode(endPoint);
    }
    return result;
  }

  public Integer visitEnumSetExpressionProxy
    (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitSimpleExpressionProxy(proxy);
    final List<SimpleIdentifierProxy> items = proxy.getItems();
    result *= 5;
    result += computeListHashCode(items);
    return result;
  }

  public Integer visitEventAliasProxy
    (final EventAliasProxy proxy)
    throws VisitorException
  {
    return visitAliasProxy(proxy);
  }

  public Integer visitEventDeclProxy
    (final EventDeclProxy proxy)
    throws VisitorException
  {
    int result = visitIdentifiedProxy(proxy);
    final EventKind kind = proxy.getKind();
    result *= 5;
    result += computeOptionalHashCode(kind);
    final boolean observable = proxy.isObservable();
    result *= 5;
    if (observable) {
      result++;
    }
    final ScopeKind scope = proxy.getScope();
    result *= 5;
    result += computeOptionalHashCode(scope);
    final List<SimpleExpressionProxy> ranges = proxy.getRanges();
    result *= 5;
    result += computeListHashCode(ranges);
    final Map<String,String> attributes = proxy.getAttributes();
    result *= 5;
    result += computeOptionalHashCode(attributes);
    if (isRespectingGeometry()) {
      final ColorGeometryProxy colorGeometry = proxy.getColorGeometry();
      result *= 5;
      result += computeProxyHashCode(colorGeometry);
    }
    return result;
  }

  public Integer visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitExpressionProxy(proxy);
    final List<Proxy> eventIdentifierList = proxy.getEventIdentifierList();
    result *= 5;
    result += computeListHashCode(eventIdentifierList);
    return result;
  }

  public Integer visitExpressionProxy
    (final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Integer visitForeachProxy
    (final ForeachProxy proxy)
    throws VisitorException
  {
    int result = visitNestedBlockProxy(proxy);
    final String name = proxy.getName();
    result *= 5;
    result += computeOptionalHashCode(name);
    final SimpleExpressionProxy range = proxy.getRange();
    result *= 5;
    result += computeProxyHashCode(range);
    return result;
  }

  public Integer visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitSimpleExpressionProxy(proxy);
    final String functionName = proxy.getFunctionName();
    result *= 5;
    result += computeOptionalHashCode(functionName);
    final List<SimpleExpressionProxy> arguments = proxy.getArguments();
    result *= 5;
    result += computeListHashCode(arguments);
    return result;
  }

  public Integer visitGraphProxy
    (final GraphProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final boolean deterministic = proxy.isDeterministic();
    result *= 5;
    if (deterministic) {
      result++;
    }
    final LabelBlockProxy blockedEvents = proxy.getBlockedEvents();
    result *= 5;
    result += computeProxyHashCode(blockedEvents);
    final Set<NodeProxy> nodes = proxy.getNodes();
    result *= 5;
    result += computeCollectionHashCode(nodes);
    final Collection<EdgeProxy> edges = proxy.getEdges();
    result *= 5;
    result += computeCollectionHashCode(edges);
    return result;
  }

  public Integer visitGroupNodeProxy
    (final GroupNodeProxy proxy)
    throws VisitorException
  {
    int result = visitNodeProxy(proxy);
    final Set<NodeProxy> immediateChildNodes = proxy.getImmediateChildNodes();
    result *= 5;
    result += computeRefCollectionHashCode(immediateChildNodes);
    if (isRespectingGeometry()) {
      final BoxGeometryProxy geometry = proxy.getGeometry();
      result *= 5;
      result += computeProxyHashCode(geometry);
    }
    return result;
  }

  public Integer visitGuardActionBlockProxy
    (final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final List<SimpleExpressionProxy> guards = proxy.getGuards();
    result *= 5;
    result += computeListHashCode(guards);
    final List<BinaryExpressionProxy> actions = proxy.getActions();
    result *= 5;
    result += computeListHashCode(actions);
    if (isRespectingGeometry()) {
      final LabelGeometryProxy geometry = proxy.getGeometry();
      result *= 5;
      result += computeProxyHashCode(geometry);
    }
    return result;
  }

  public Integer visitIdentifiedProxy
    (final IdentifiedProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final IdentifierProxy identifier = proxy.getIdentifier();
    result *= 5;
    result += computeProxyHashCode(identifier);
    return result;
  }

  public Integer visitIdentifierProxy
    (final IdentifierProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Integer visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    int result = visitIdentifierProxy(proxy);
    final String name = proxy.getName();
    result *= 5;
    result += computeOptionalHashCode(name);
    final List<SimpleExpressionProxy> indexes = proxy.getIndexes();
    result *= 5;
    result += computeListHashCode(indexes);
    return result;
  }

  public Integer visitInstanceProxy
    (final InstanceProxy proxy)
    throws VisitorException
  {
    int result = visitComponentProxy(proxy);
    final String moduleName = proxy.getModuleName();
    result *= 5;
    result += computeOptionalHashCode(moduleName);
    final List<ParameterBindingProxy> bindingList = proxy.getBindingList();
    result *= 5;
    result += computeListHashCode(bindingList);
    return result;
  }

  public Integer visitIntConstantProxy
    (final IntConstantProxy proxy)
    throws VisitorException
  {
    int result = visitSimpleExpressionProxy(proxy);
    final int value = proxy.getValue();
    result *= 5;
    result += value;
    return result;
  }

  public Integer visitLabelBlockProxy
    (final LabelBlockProxy proxy)
    throws VisitorException
  {
    int result = visitEventListExpressionProxy(proxy);
    if (isRespectingGeometry()) {
      final LabelGeometryProxy geometry = proxy.getGeometry();
      result *= 5;
      result += computeProxyHashCode(geometry);
    }
    return result;
  }

  public Integer visitLabelGeometryProxy
    (final LabelGeometryProxy proxy)
    throws VisitorException
  {
    int result = visitGeometryProxy(proxy);
    final Point2D offset = proxy.getOffset();
    result *= 5;
    result += computeOptionalHashCode(offset);
    final AnchorPosition anchor = proxy.getAnchor();
    result *= 5;
    result += computeOptionalHashCode(anchor);
    return result;
  }

  public Integer visitModuleProxy
    (final ModuleProxy proxy)
    throws VisitorException
  {
    int result = visitDocumentProxy(proxy);
    final List<ConstantAliasProxy> constantAliasList =
      proxy.getConstantAliasList();
    result *= 5;
    result += computeListHashCode(constantAliasList);
    final List<EventDeclProxy> eventDeclList = proxy.getEventDeclList();
    result *= 5;
    result += computeListHashCode(eventDeclList);
    final List<Proxy> eventAliasList = proxy.getEventAliasList();
    result *= 5;
    result += computeListHashCode(eventAliasList);
    final List<Proxy> componentList = proxy.getComponentList();
    result *= 5;
    result += computeListHashCode(componentList);
    return result;
  }

  public Integer visitModuleSequenceProxy
    (final ModuleSequenceProxy proxy)
    throws VisitorException
  {
    int result = visitDocumentProxy(proxy);
    final List<ModuleProxy> modules = proxy.getModules();
    result *= 5;
    result += computeListHashCode(modules);
    return result;
  }

  public Integer visitNestedBlockProxy
    (final NestedBlockProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final List<Proxy> body = proxy.getBody();
    result *= 5;
    result += computeListHashCode(body);
    return result;
  }

  public Integer visitNodeProxy
    (final NodeProxy proxy)
    throws VisitorException
  {
    int result = visitNamedProxy(proxy);
    final PlainEventListProxy propositions = proxy.getPropositions();
    result *= 5;
    result += computeProxyHashCode(propositions);
    final Map<String,String> attributes = proxy.getAttributes();
    result *= 5;
    result += computeOptionalHashCode(attributes);
    return result;
  }

  public Integer visitParameterBindingProxy
    (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    int result = visitNamedProxy(proxy);
    final ExpressionProxy expression = proxy.getExpression();
    result *= 5;
    result += computeProxyHashCode(expression);
    return result;
  }

  public Integer visitPlainEventListProxy
    (final PlainEventListProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Integer visitPointGeometryProxy
    (final PointGeometryProxy proxy)
    throws VisitorException
  {
    int result = visitGeometryProxy(proxy);
    final Point2D point = proxy.getPoint();
    result *= 5;
    result += computeOptionalHashCode(point);
    return result;
  }

  public Integer visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy proxy)
    throws VisitorException
  {
    int result = visitIdentifierProxy(proxy);
    final IdentifierProxy baseIdentifier = proxy.getBaseIdentifier();
    result *= 5;
    result += computeProxyHashCode(baseIdentifier);
    final IdentifierProxy componentIdentifier =
      proxy.getComponentIdentifier();
    result *= 5;
    result += computeProxyHashCode(componentIdentifier);
    return result;
  }

  public Integer visitSimpleComponentProxy
    (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    int result = visitComponentProxy(proxy);
    final ComponentKind kind = proxy.getKind();
    result *= 5;
    result += computeOptionalHashCode(kind);
    final GraphProxy graph = proxy.getGraph();
    result *= 5;
    result += computeProxyHashCode(graph);
    final Map<String,String> attributes = proxy.getAttributes();
    result *= 5;
    result += computeOptionalHashCode(attributes);
    return result;
  }

  public Integer visitSimpleExpressionProxy
    (final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitExpressionProxy(proxy);
    if (isRespectingGeometry()) {
      final String plainText = proxy.getPlainText();
      result *= 5;
      result += computeOptionalHashCode(plainText);
    }
    return result;
  }

  public Integer visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    int result = visitIdentifierProxy(proxy);
    final String name = proxy.getName();
    result *= 5;
    result += computeOptionalHashCode(name);
    return result;
  }

  public Integer visitSimpleNodeProxy
    (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    int result = visitNodeProxy(proxy);
    final boolean initial = proxy.isInitial();
    result *= 5;
    if (initial) {
      result++;
    }
    if (isRespectingGeometry()) {
      final PointGeometryProxy pointGeometry = proxy.getPointGeometry();
      result *= 5;
      result += computeProxyHashCode(pointGeometry);
      final PointGeometryProxy initialArrowGeometry =
        proxy.getInitialArrowGeometry();
      result *= 5;
      result += computeProxyHashCode(initialArrowGeometry);
      final LabelGeometryProxy labelGeometry = proxy.getLabelGeometry();
      result *= 5;
      result += computeProxyHashCode(labelGeometry);
    }
    return result;
  }

  public Integer visitSplineGeometryProxy
    (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    int result = visitGeometryProxy(proxy);
    final List<Point2D> points = proxy.getPoints();
    result *= 5;
    result += computeOptionalHashCode(points);
    final SplineKind kind = proxy.getKind();
    result *= 5;
    result += computeOptionalHashCode(kind);
    return result;
  }

  public Integer visitUnaryExpressionProxy
    (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    int result = visitSimpleExpressionProxy(proxy);
    final UnaryOperator operator = proxy.getOperator();
    result *= 5;
    result += computeOptionalHashCode(operator);
    final SimpleExpressionProxy subTerm = proxy.getSubTerm();
    result *= 5;
    result += computeProxyHashCode(subTerm);
    return result;
  }

  public Integer visitVariableComponentProxy
    (final VariableComponentProxy proxy)
    throws VisitorException
  {
    int result = visitComponentProxy(proxy);
    final SimpleExpressionProxy type = proxy.getType();
    result *= 5;
    result += computeProxyHashCode(type);
    final SimpleExpressionProxy initialStatePredicate =
      proxy.getInitialStatePredicate();
    result *= 5;
    result += computeProxyHashCode(initialStatePredicate);
    final List<VariableMarkingProxy> variableMarkings =
      proxy.getVariableMarkings();
    result *= 5;
    result += computeListHashCode(variableMarkings);
    return result;
  }

  public Integer visitVariableMarkingProxy
    (final VariableMarkingProxy proxy)
    throws VisitorException
  {
    int result = visitProxy(proxy);
    final IdentifierProxy proposition = proxy.getProposition();
    result *= 5;
    result += computeProxyHashCode(proposition);
    final SimpleExpressionProxy predicate = proxy.getPredicate();
    result *= 5;
    result += computeProxyHashCode(predicate);
    return result;
  }

}
