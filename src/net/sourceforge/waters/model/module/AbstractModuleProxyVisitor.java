//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   AbstractModuleProxyVisitor
//###########################################################################
//# $Id: AbstractModuleProxyVisitor.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;


public class AbstractModuleProxyVisitor
  extends AbstractProxyVisitor
  implements ModuleProxyVisitor
{

  public Object visitAliasProxy(final AliasProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitBooleanConstantProxy(final BooleanConstantProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitBoxGeometryProxy(final BoxGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitColorGeometryProxy(final ColorGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitComponentProxy(final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Object visitEdgeProxy(final EdgeProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitEventDeclProxy(final EventDeclProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitEventListExpressionProxy(final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public Object visitEventParameterProxy(final EventParameterProxy proxy)
    throws VisitorException
  {
    return visitParameterProxy(proxy);
  }

  public Object visitExpressionProxy(final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitForeachComponentProxy(final ForeachComponentProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachEventAliasProxy(final ForeachEventAliasProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachEventProxy(final ForeachEventProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachProxy(final ForeachProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitGraphProxy(final GraphProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitGroupNodeProxy(final GroupNodeProxy proxy)
    throws VisitorException
  {
    return visitNodeProxy(proxy);
  }

  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitIdentifierProxy(final IdentifierProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    return visitIdentifierProxy(proxy);
  }

  public Object visitInstanceProxy(final InstanceProxy proxy)
    throws VisitorException
  {
    return visitComponentProxy(proxy);
  }

  public Object visitIntConstantProxy(final IntConstantProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitIntParameterProxy(final IntParameterProxy proxy)
    throws VisitorException
  {
    return visitSimpleParameterProxy(proxy);
  }

  public Object visitLabelBlockProxy(final LabelBlockProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitLabelGeometryProxy(final LabelGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitModuleProxy(final ModuleProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  public Object visitNodeProxy(final NodeProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitParameterBindingProxy(final ParameterBindingProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitParameterProxy(final ParameterProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitPlainEventListProxy(final PlainEventListProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitPointGeometryProxy(final PointGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitRangeParameterProxy(final RangeParameterProxy proxy)
    throws VisitorException
  {
    return visitSimpleParameterProxy(proxy);
  }

  public Object visitSimpleComponentProxy(final SimpleComponentProxy proxy)
    throws VisitorException
  {
    return visitComponentProxy(proxy);
  }

  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    return visitIdentifierProxy(proxy);
  }

  public Object visitSimpleNodeProxy(final SimpleNodeProxy proxy)
    throws VisitorException
  {
    return visitNodeProxy(proxy);
  }

  public Object visitSimpleParameterProxy(final SimpleParameterProxy proxy)
    throws VisitorException
  {
    return visitParameterProxy(proxy);
  }

  public Object visitSplineGeometryProxy(final SplineGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitVariableProxy(final VariableProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

}
