//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleHierarchyChecker
//###########################################################################
//# $Id: ModuleHierarchyChecker.java,v 1.4 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
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
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;


class ModuleHierarchyChecker
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ModuleHierarchyChecker()
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
      subject.getEventListModifiable();
    visitProxyCollectionChild(events, proxy);
    return null;
  }

  public Object visitEventParameterProxy
      (final EventParameterProxy proxy)
    throws VisitorException
  {
    visitParameterProxy(proxy);
    final EventDeclProxy decl = proxy.getEventDecl();
    visitProxyChild(decl, proxy);
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
    visitProxyChild(blocked, proxy);
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
    final Collection<ParameterSubject> parameters =
      subject.getParameterListModifiable();
    visitProxyCollectionChild(parameters, proxy);
    final Collection<AliasSubject> constants =
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

  public Object visitSimpleParameterProxy
      (final SimpleParameterProxy proxy)
    throws VisitorException
  {
    visitParameterProxy(proxy);
    final SimpleExpressionProxy defaultValue = proxy.getDefaultValue();
    visitProxyChild(defaultValue, proxy);
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
      final StringBuffer buffer = new StringBuffer();
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
    final Class clazz = object.getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Data Members
  private Collection<NodeSubject> mNodeSet;

}
