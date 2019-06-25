//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleSequenceProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.SchemaBase;
import net.sourceforge.waters.xsd.SchemaModule;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.ScopeKind;
import net.sourceforge.waters.xsd.module.SplineKind;


public class StAXModuleWriter
  extends StAXDocumentWriter<ModuleProxy>
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitAliasProxy(final AliasProxy proxy) throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_BinaryExpression);
    writeAttribute(SchemaModule.ATTRIB_Operator, expr.getOperator().getName());
    visitSimpleExpressionProxy(expr);
    expr.getLeft().acceptVisitor(this);
    expr.getRight().acceptVisitor(this);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitBoxGeometryProxy(final BoxGeometryProxy geo)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_BoxGeometry);
    visitGeometryProxy(geo);
    final Rectangle2D box = geo.getRectangle();
    writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_Box);
    writeIntAttribute(SchemaModule.ATTRIB_X, box.getX());
    writeIntAttribute(SchemaModule.ATTRIB_Y, box.getY());
    writeIntAttribute(SchemaModule.ATTRIB_Width, box.getWidth());
    writeIntAttribute(SchemaModule.ATTRIB_Height, box.getHeight());
    writeEndElement();
    writeEndElement();
    return null;
  }

  @Override
  public Object visitColorGeometryProxy(final ColorGeometryProxy geo)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_ColorGeometry);
    visitGeometryProxy(geo);
    for (final Color color : geo.getColorSet()) {
      writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_Color);
      writeIntAttribute(SchemaModule.ATTRIB_Red, color.getRed());
      writeIntAttribute(SchemaModule.ATTRIB_Green, color.getGreen());
      writeIntAttribute(SchemaModule.ATTRIB_Blue, color.getBlue());
      writeEndElement();
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitComponentProxy(final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  @Override
  public Object visitConstantAliasProxy(final ConstantAliasProxy alias)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_ConstantAlias);
    writeAttribute(SchemaModule.ATTRIB_Scope, alias.getScope(),
                   ScopeKind.LOCAL); // TODO use default
    visitAliasProxy(alias);
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_ConstantAliasExpression);
    alias.getExpression().acceptVisitor(this);
    writeEndElement();
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEdgeProxy(final EdgeProxy edge) throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_Edge);
    final NodeProxy source = edge.getSource();
    writeAttribute(SchemaModule.ATTRIB_Source, source.getName());
    final NodeProxy target = edge.getTarget();
    writeAttribute(SchemaModule.ATTRIB_Target, target.getName());
    visitProxy(edge);
    visitLabelBlockProxy(edge.getLabelBlock());
    writePointGeometry(edge.getStartPoint(),
                       SchemaModule.ELEMENT_StartPointGeometry);
    writeOptionalItem(edge.getGeometry());
    writePointGeometry(edge.getEndPoint(),
                       SchemaModule.ELEMENT_EndPointGeometry);
    writeOptionalItem(edge.getGuardActionBlock());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EnumSetExpression);
    visitSimpleExpressionProxy(expr);
    for (final SimpleIdentifierProxy ident : expr.getItems()) {
      visitSimpleIdentifierProxy(ident);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEventAliasProxy(final EventAliasProxy alias)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventAlias);
    visitAliasProxy(alias);
    final ExpressionProxy expr = alias.getExpression();
    if (expr instanceof EventListExpressionProxy) {
      expr.acceptVisitor(this);
    } else {
      writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventListExpression);
      writeAttribute(SchemaModule.ATTRIB_Unpack, true);
      writeNewLine();
      writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventList);
      writeNewLine();
      expr.acceptVisitor(this);
      writeEndElement();
      writeEndElement();
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    final IdentifierProxy ident = decl.getIdentifier();
    final List<SimpleExpressionProxy> ranges = decl.getRanges();
    final ColorGeometryProxy geo = decl.getColorGeometry();
    final Map<String,String> attribs = decl.getAttributes();
    final boolean empty = ident instanceof SimpleIdentifierProxy &&
      ranges.isEmpty() && geo == null && attribs.isEmpty();
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventDecl, empty);
    writeAttribute(SchemaModule.ATTRIB_Kind, decl.getKind());
    writeAttribute(SchemaModule.ATTRIB_Observable, decl.isObservable(),
                   SchemaModule.DEFAULT_Observable);
    writeAttribute(SchemaModule.ATTRIB_Scope, decl.getScope(),
                   ScopeKind.LOCAL);  // use default
    visitIdentifiedProxy(decl);
    writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_RangeList, ranges);
    writeOptionalItem(geo);
    writeAttributeMap(attribs);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEventListExpressionProxy
    (final EventListExpressionProxy expr)
    throws VisitorException
  {
    final ListType oldType = mListType;
    try {
      visitExpressionProxy(expr);
      mListType = ListType.EVENTS;
      for (final Proxy proxy : expr.getEventIdentifierList()) {
        proxy.acceptVisitor(this);
      }
      return null;
    } finally {
      mListType = oldType;
    }
  }

  @Override
  public Object visitExpressionProxy(final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, mListType.getForeachName());
    visitNamedProxy(foreach);
    foreach.getRange().acceptVisitor(this);
    writeOptionalItem(foreach.getGuard());
    writeOptionalList(NAMESPACE, mListType.getListName(), foreach.getBody());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_FunctionCallExpression);
    writeAttribute(SchemaModule.ATTRIB_FunctionName, expr.getFunctionName());
    visitSimpleExpressionProxy(expr);
    for (final SimpleExpressionProxy arg : expr.getArguments()) {
      arg.acceptVisitor(this);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitGraphProxy(final GraphProxy graph) throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_Graph);
    writeAttribute(SchemaModule.ATTRIB_Deterministic, graph.isDeterministic(),
                   SchemaModule.DEFAULT_Deterministic);
    visitProxy(graph);
    writeOptionalItem(graph.getBlockedEvents());
    writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_NodeList, graph.getNodes());
    writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_EdgeList, graph.getEdges());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitGroupNodeProxy(final GroupNodeProxy group)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_GroupNode);
    visitNodeProxy(group);
    for (final NodeProxy node : group.getImmediateChildNodes()) {
      writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_NodeRef);
      writeAttribute(SchemaBase.ATTRIB_Name, node.getName());
      writeEndElement();
    }
    writeOptionalItem(group.getGeometry());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy ga)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_GuardActionBlock);
    visitProxy(ga);
    writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_Guards, ga.getGuards());
    writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_Actions, ga.getActions());
    writeOptionalItem(ga.getGeometry());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy ident = proxy.getIdentifier();
    if (ident instanceof SimpleIdentifierProxy) {
      return visitNamedProxy(proxy);
    } else {
      visitProxy(proxy);
      return ident.acceptVisitor(this);
    }
  }

  @Override
  public Object visitIdentifierProxy(final IdentifierProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  @Override
  public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_IndexedIdentifier);
    writeAttribute(SchemaModule.ATTRIB_Name, ident.getName());
    visitIdentifierProxy(ident);
    for (final SimpleExpressionProxy expr : ident.getIndexes()) {
      expr.acceptVisitor(this);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitInstanceProxy(final InstanceProxy inst)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_Instance);
    writeAttribute(SchemaModule.ATTRIB_ModuleName, inst.getModuleName());
    visitIdentifiedProxy(inst);
    for (final ParameterBindingProxy binding : inst.getBindingList()) {
      visitParameterBindingProxy(binding);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitIntConstantProxy(final IntConstantProxy expr)
    throws VisitorException
  {
    writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_IntConstant);
    writeAttribute(SchemaModule.ATTRIB_Value, expr.getValue());
    visitSimpleExpressionProxy(expr);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitLabelBlockProxy(final LabelBlockProxy block)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_LabelBlock);
    visitEventListExpressionProxy(block);
    writeOptionalItem(block.getGeometry());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_LabelGeometry);
    writeAttribute(SchemaModule.ATTRIB_Anchor, geo.getAnchor(),
                   AnchorPosition.SW);  // TODO use default
    visitGeometryProxy(geo);
    writePoint(geo.getOffset());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final ListType oldType = mListType;
    try {
      writeStartElement(NAMESPACE, SchemaModule.ELEMENT_Module);
      writeDefaultNamespace(SchemaModule.NAMESPACE);
      visitDocumentProxy(module);
      writeOptionalList(SchemaModule.NAMESPACE,
                        SchemaModule.ELEMENT_ConstantAliasList,
                        module.getConstantAliasList());
      writeOptionalList(SchemaModule.NAMESPACE,
                        SchemaModule.ELEMENT_EventDeclList,
                        module.getEventDeclList());
      mListType = ListType.ALIASES;
      writeOptionalList(SchemaModule.NAMESPACE,
                        SchemaModule.ELEMENT_EventAliasList,
                        module.getEventAliasList());
      mListType = ListType.COMPONENTS;
      writeOptionalList(SchemaModule.NAMESPACE,
                        SchemaModule.ELEMENT_ComponentList,
                        module.getComponentList());
      writeEndElement();
    return null;
    } finally {
      mListType = oldType;
    }
  }

  @Override
  public Object visitModuleSequenceProxy(final ModuleSequenceProxy seq)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_ModuleSequence);
    writeDefaultNamespace(SchemaModule.NAMESPACE);
    visitDocumentProxy(seq);
    for (final ModuleProxy module : seq.getModules()) {
      visitModuleProxy(module);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitNodeProxy(final NodeProxy node) throws VisitorException
  {
    visitNamedProxy(node);
    final PlainEventListProxy props = node.getPropositions();
    if (props != null) {
      writeOptionalList(NAMESPACE, SchemaModule.ELEMENT_EventList,
                        props.getEventIdentifierList());
    }
    writeAttributeMap(node.getAttributes());
    return null;
  }

  @Override
  public Object visitParameterBindingProxy(final ParameterBindingProxy binding)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_ParameterBinding);
    visitNamedProxy(binding);
    binding.getExpression().acceptVisitor(this);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitPlainEventListProxy(final PlainEventListProxy expr)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventListExpression);
    writeNewLine();
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_EventList);
    visitEventListExpressionProxy(expr);
    writeEndElement();
    writeEndElement();
    return null;
  }

  @Override
  public Object visitPointGeometryProxy(final PointGeometryProxy geo)
    throws VisitorException
  {
    writePointGeometry(geo);
    return null;
  }

  @Override
  public Object visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_QualifiedIdentifier);
    visitIdentifierProxy(ident);
    ident.getBaseIdentifier().acceptVisitor(this);
    ident.getComponentIdentifier().acceptVisitor(this);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_SimpleComponent);
    writeAttribute(SchemaModule.ATTRIB_Kind, comp.getKind());
    visitIdentifiedProxy(comp);
    visitGraphProxy(comp.getGraph());
    writeAttributeMap(comp.getAttributes());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    writeAttribute(SchemaModule.ATTRIB_Text, expr.getPlainText());
    return visitProxy(expr);
  }

  @Override
  public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    throws VisitorException
  {
    writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_SimpleIdentifier);
    writeAttribute(SchemaModule.ATTRIB_Name, ident.getName());
    visitIdentifierProxy(ident);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    final PlainEventListProxy props = node.getPropositions();
    final PointGeometryProxy pointGeo = node.getPointGeometry();
    final PointGeometryProxy initGeo = node.getInitialArrowGeometry();
    final LabelGeometryProxy labelGeo = node.getLabelGeometry();
    final boolean empty = props.getEventIdentifierList().isEmpty() &&
      pointGeo == null && initGeo == null && labelGeo == null;
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_SimpleNode, empty);
    writeAttribute(SchemaModule.ATTRIB_Initial, node.isInitial());
    visitNodeProxy(node);
    writePointGeometry(pointGeo);
    writePointGeometry(initGeo, SchemaModule.ELEMENT_InitialArrowGeometry);
    writeOptionalItem(labelGeo);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitSplineGeometryProxy(final SplineGeometryProxy geo)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_SplineGeometry);
    writeAttribute(SchemaModule.ATTRIB_Kind, geo.getKind(),
                   SplineKind.INTERPOLATING);  // TODO use default
    visitGeometryProxy(geo);
    for (final Point2D point : geo.getPoints()) {
      writePoint(point);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_UnaryExpression);
    writeAttribute(SchemaModule.ATTRIB_Operator, expr.getOperator().getName());
    visitSimpleExpressionProxy(expr);
    expr.getSubTerm().acceptVisitor(this);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_VariableComponent);
    visitIdentifiedProxy(var);
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_VariableRange);
    var.getType().acceptVisitor(this);
    writeEndElement();
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_VariableInitial);
    var.getInitialStatePredicate().acceptVisitor(this);
    writeEndElement();
    for (final VariableMarkingProxy marking : var.getVariableMarkings()) {
      visitVariableMarkingProxy(marking);
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitVariableMarkingProxy(final VariableMarkingProxy marking)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaModule.ELEMENT_VariableMarking);
    visitProxy(marking);
    marking.getProposition().acceptVisitor(this);
    marking.getPredicate().acceptVisitor(this);
    writeEndElement();
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void writePointGeometry(final PointGeometryProxy geo)
    throws VisitorException
  {
    writePointGeometry(geo, SchemaModule.ELEMENT_PointGeometry);
  }

  private void writePointGeometry(final PointGeometryProxy geo,
                                  final String key)
    throws VisitorException
  {
    if (geo != null) {
      writeStartElement(NAMESPACE, key);
      visitGeometryProxy(geo);
      writePoint(geo.getPoint());
      writeEndElement();
    }
  }

  private void writePoint(final Point2D point)
    throws VisitorException
  {
    writeEmptyElement(NAMESPACE, SchemaModule.ELEMENT_Point);
    writeIntAttribute(SchemaModule.ATTRIB_X, point.getX());
    writeIntAttribute(SchemaModule.ATTRIB_Y, point.getY());
    writeEndElement();
  }


  //#########################################################################
  //# Inner Enumeration ListType
  private static enum ListType
  {
    ALIASES(SchemaModule.ELEMENT_EventAliasList,
            SchemaModule.ELEMENT_ForeachEventAlias),
    EVENTS(SchemaModule.ELEMENT_EventList,
           SchemaModule.ELEMENT_ForeachEvent),
    COMPONENTS(SchemaModule.ELEMENT_ComponentList,
               SchemaModule.ELEMENT_ForeachComponent);

    //#######################################################################
    //# Constructor
    private ListType(final String listName, final String foreachName)
    {
      mListName = listName;
      mForeachName = foreachName;
    }

    //#######################################################################
    //# Simple Access
    private String getListName()
    {
      return mListName;
    }

    private String getForeachName()
    {
      return mForeachName;
    }

    //#######################################################################
    //# Data Members
    private final String mListName;
    private final String mForeachName;
  }


  //#3#######################################################################
  //# Data Members
  private ListType mListType = null;


  //#########################################################################
  //# Class Constants
  private static final String NAMESPACE = SchemaModule.NAMESPACE;

}
