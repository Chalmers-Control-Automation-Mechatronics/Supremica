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

package net.sourceforge.waters.subject.module;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;


class ModuleHierarchyChecker
  extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public static ModuleHierarchyChecker getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModuleHierarchyChecker();
    }
    return theInstance;
  }

  protected ModuleHierarchyChecker()
  {
  }


  //#########################################################################
  //# Invocation
  public void check(final Proxy proxy)
    throws VisitorException
  {
    proxy.acceptVisitor(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ProxyVisitor
  public Object visitProxy(final Proxy proxy)
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Object visitAliasProxy
      (final AliasProxy proxy)
    throws VisitorException
  {
    visitIdentifiedProxy(proxy);
    final ExpressionProxy expression = proxy.getExpression();
    visitProxyChild(expression, proxy);
    return null;
  }

  public Object visitBinaryExpressionProxy
      (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    visitSimpleExpressionProxy(proxy);
    final SimpleExpressionProxy left = proxy.getLeft();
    visitProxyChild(left, proxy);
    final SimpleExpressionProxy right = proxy.getRight();
    visitProxyChild(right, proxy);
    return null;
  }

  public Object visitColorGeometryProxy
      (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    final ColorGeometrySubject subject = (ColorGeometrySubject) proxy;
    visitGeometryProxy(proxy);
    final Collection<?> colors = subject.getColorSetModifiable(); 
    visitSimpleCollectionChild(colors, proxy);
    return null;
 }

  public Object visitEdgeProxy
      (final EdgeProxy proxy)
    throws VisitorException
  {
    visitProxy(proxy);
    final NodeProxy source = proxy.getSource();
    visitProxyChild(source, mNodeSet);
    final NodeProxy target = proxy.getTarget();
    visitProxyChild(target, mNodeSet);
    final LabelBlockProxy labelBlock = proxy.getLabelBlock();
    visitProxyChild(labelBlock, proxy);
    final SplineGeometryProxy geometry = proxy.getGeometry();
    visitOptionalProxyChild(geometry, proxy);
    final PointGeometryProxy start = proxy.getStartPoint();
    visitOptionalProxyChild(start, proxy);
    final PointGeometryProxy end = proxy.getEndPoint();
    visitOptionalProxyChild(end, proxy);
    return null;
  }

  public Object visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final EnumSetExpressionSubject subject = (EnumSetExpressionSubject) proxy;
    visitSimpleExpressionProxy(proxy);
    final Collection<SimpleIdentifierSubject> items =
      subject.getItemsModifiable();
    visitProxyCollectionChild(items, proxy);
    return null;
  }

  public Object visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    final EventDeclSubject subject = (EventDeclSubject) proxy;
    visitNamedProxy(proxy);
    final Collection<SimpleExpressionSubject> ranges =
      subject.getRangesModifiable();
    visitProxyCollectionChild(ranges, proxy);
    final ColorGeometryProxy geometry = proxy.getColorGeometry();
    visitOptionalProxyChild(geometry, proxy);
    return null;
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    final EventListExpressionSubject subject =
      (EventListExpressionSubject) proxy;
    visitExpressionProxy(proxy);
    final Collection<AbstractSubject> events =
      subject.getEventIdentifierListModifiable();
    visitProxyCollectionChild(events, proxy);
    return null;
  }

  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    final ForeachSubject subject = (ForeachSubject) proxy;
    visitNamedProxy(proxy);
    final SimpleExpressionProxy range = proxy.getRange();
    visitProxyChild(range, proxy);
    final SimpleExpressionProxy guard = proxy.getGuard();
    visitOptionalProxyChild(guard, proxy);
    final Collection<AbstractSubject> body = subject.getBodyModifiable();
    visitProxyCollectionChild(body, proxy);
    return null;
  }

  public Object visitGraphProxy
      (final GraphProxy proxy)
    throws VisitorException
  {
    final GraphSubject subject = (GraphSubject) proxy;
    visitProxy(proxy);
    final LabelBlockProxy blocked = proxy.getBlockedEvents();
    if (blocked != null) {
      visitProxyChild(blocked, proxy);
    }
    mNodeSet = subject.getNodesModifiable();
    visitProxyCollectionChild(mNodeSet, proxy);
    final Collection<EdgeSubject> edges = subject.getEdgesModifiable();
    visitProxyCollectionChild(edges, proxy);
    mNodeSet = null;
    return null;
  }

  public Object visitGroupNodeProxy
      (final GroupNodeProxy proxy)
    throws VisitorException
  {
    final GroupNodeSubject subject = (GroupNodeSubject) proxy;
    visitNodeProxy(proxy);
    final Collection<NodeSubject> children =
      subject.getImmediateChildNodesModifiable();
    checkParent(children, proxy);
    for (final NodeProxy node : children) {
      visitProxyChild(node, mNodeSet);
    }
    final BoxGeometryProxy geometry = proxy.getGeometry();
    visitOptionalProxyChild(geometry, proxy);
    return null;
  }

  public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    final IndexedIdentifierSubject subject = (IndexedIdentifierSubject) proxy;
    visitIdentifierProxy(proxy);
    final Collection<SimpleExpressionSubject> indexes =
      subject.getIndexesModifiable();
    visitProxyCollectionChild(indexes, proxy);
    return null;
  }

  public Object visitIdentifiedProxy
      (final IdentifiedProxy proxy)
    throws VisitorException
  {
    visitProxy(proxy);
    final IdentifierProxy identifier = proxy.getIdentifier();
    visitProxyChild(identifier, proxy);
    return null;
  }

  public Object visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    final InstanceSubject subject = (InstanceSubject) proxy;
    visitComponentProxy(proxy);
    final Collection<ParameterBindingSubject> bindings =
      subject.getBindingListModifiable();
    visitProxyCollectionChild(bindings, proxy);
    return null;
  }

  public Object visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    visitEventListExpressionProxy(proxy);
    final LabelGeometryProxy geometry = proxy.getGeometry();
    visitOptionalProxyChild(geometry, proxy);
    return null;
  }

  public Object visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    final ModuleSubject subject = (ModuleSubject) proxy;
    visitDocumentProxy(proxy);
    final Collection<ConstantAliasSubject> constants =
      subject.getConstantAliasListModifiable();
    visitProxyCollectionChild(constants, proxy);
    final Collection<EventDeclSubject> events =
      subject.getEventDeclListModifiable();
    visitProxyCollectionChild(events, proxy);
    final Collection<AbstractSubject> aliases =
      subject.getEventAliasListModifiable();
    visitProxyCollectionChild(aliases, proxy);
    final Collection<AbstractSubject> components =
      subject.getComponentListModifiable();
    visitProxyCollectionChild(components, proxy);
    return null;
  }

  public Object visitNodeProxy
      (final NodeProxy proxy)
    throws VisitorException
  {
    visitNamedProxy(proxy);
    final EventListExpressionProxy propositions = proxy.getPropositions();
    visitProxyChild(propositions, proxy);
    return null;
  }

  public Object visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    visitComponentProxy(proxy);
    final GraphProxy graph = proxy.getGraph();
    visitProxyChild(graph, proxy);
    return null;
  }

  public Object visitSimpleNodeProxy
      (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    visitNamedProxy(proxy);
    final PointGeometryProxy geometry = proxy.getPointGeometry();
    visitOptionalProxyChild(geometry, proxy);
    final LabelGeometryProxy labelgeo = proxy.getLabelGeometry();
    visitOptionalProxyChild(labelgeo, proxy);
    return visitNodeProxy(proxy);
  }

  public Object visitSplineGeometryProxy
      (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    final SplineGeometrySubject subject = (SplineGeometrySubject) proxy;
    visitGeometryProxy(proxy);
    final Collection<?> points = subject.getPointsModifiable(); 
    visitSimpleCollectionChild(points, proxy);
    return visitGeometryProxy(proxy);
  }

  public Object visitUnaryExpressionProxy
      (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    visitSimpleExpressionProxy(proxy);
    final SimpleExpressionProxy subTerm = proxy.getSubTerm();
    visitProxyChild(subTerm, proxy);
    return null;
  }

  public Object visitVariableComponentProxy
      (final VariableComponentProxy proxy)
    throws VisitorException
  {
    final VariableComponentSubject subject = (VariableComponentSubject) proxy;
    visitIdentifiedProxy(proxy);
    final SimpleExpressionProxy type = proxy.getType();
    visitProxyChild(type, proxy);
    final SimpleExpressionProxy predicate = proxy.getInitialStatePredicate();
    visitProxyChild(predicate, proxy);
    final Collection<VariableMarkingSubject> markings =
      subject.getVariableMarkingsModifiable();
    visitProxyCollectionChild(markings, proxy);
    return null;
  }

  public Object visitVariableMarkingProxy
      (final VariableMarkingProxy proxy)
    throws VisitorException
  {
    visitProxy(proxy);
    final IdentifierProxy proposition = proxy.getProposition();
    visitProxyChild(proposition, proxy);
    final SimpleExpressionProxy predicate = proxy.getPredicate();
    visitProxyChild(predicate, proxy);
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void visitOptionalProxyChild(final Proxy child, final Object parent)
    throws VisitorException
  {
    if (child != null) {
      visitProxyChild(child, parent);
    }
  }

  private void visitProxyChild(final Proxy child, final Object parent)
    throws VisitorException
  {
    checkParent(child, parent);
    child.acceptVisitor(this);
  }

  private void visitSimpleCollectionChild(final Collection<?> child,
                                          final Object parent)
    throws VisitorException
  {
    checkParent(child, parent);
  }

  private void visitProxyCollectionChild
    (final Collection<? extends Proxy> child,
     final Object parent)
    throws VisitorException
  {
    checkParent(child, parent);
    for (final Proxy proxy : child) {
      checkParent(proxy, child);
      proxy.acceptVisitor(this);
    }
  }

  private void checkParent(final Object object, final Object parent)
    throws VisitorException
  {
    final Subject subject = (Subject) object;
    final Subject foundparent = subject.getParent();
    if (foundparent != parent) {
      final StringBuilder buffer = new StringBuilder();
      if (foundparent == null) {
        buffer.append("NULL");
      } else {
        buffer.append("Bad");
      }
      buffer.append(" parent found for ");
      buffer.append(getShortClassName(object));
      if (object instanceof NamedProxy) {
        final NamedProxy named = (NamedProxy) object;
        buffer.append(" '");
        buffer.append(named.getName());
        buffer.append('\'');
      }
      buffer.append('!');
      throw new VisitorException(buffer.toString());
    }
  }

  private String getShortClassName(final Object object)
  {
    final Class<?> clazz = object.getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Data Members
  private Collection<NodeSubject> mNodeSet;


  //#########################################################################
  //# Class Variables
  private static ModuleHierarchyChecker theInstance = null;

}








