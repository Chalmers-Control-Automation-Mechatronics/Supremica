//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleProxyFactory
//###########################################################################
//# $Id: ModuleProxyFactory.java,v 1.5 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.SplineKind;


public interface ModuleProxyFactory
{
  /**
   * Creates a new alias.
   * @param identifier The identifier defining the name of the new alias.
   * @param expression The expression of the new alias.
   */
  public AliasProxy createAliasProxy
      (IdentifierProxy identifier,
       ExpressionProxy expression);

  /**
   * Creates a new binary expression.
   * @param operator The operator of the new binary expression.
   * @param left The left subterm of the new binary expression.
   * @param right The right subterm of the new binary expression.
   */
  public BinaryExpressionProxy createBinaryExpressionProxy
      (BinaryOperator operator,
       SimpleExpressionProxy left,
       SimpleExpressionProxy right);

  /**
   * Creates a new box geometry.
   * @param rectangle The rectangle of the new box geometry.
   */
  public BoxGeometryProxy createBoxGeometryProxy
      (Rectangle2D rectangle);

  /**
   * Creates a new color geometry.
   * @param colorSet The colour set of the new color geometry, or <CODE>null</CODE> if empty.
   */
  public ColorGeometryProxy createColorGeometryProxy
      (Collection<? extends Color> colorSet);

  /**
   * Creates a new color geometry using default values.
   * This method creates a color geometry with
   * an empty colour set.
   */
  public ColorGeometryProxy createColorGeometryProxy();

  /**
   * Creates a new edge.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   * @param geometry The rendering information of the new edge, or <CODE>null</CODE>.
   * @param startPoint The rendering information for the start point of the new edge, or <CODE>null</CODE>.
   * @param endPoint The rendering information for the end point of the new edge, or <CODE>null</CODE>.
   */
  public EdgeProxy createEdgeProxy
      (NodeProxy source,
       NodeProxy target,
       LabelBlockProxy labelBlock,
       SplineGeometryProxy geometry,
       PointGeometryProxy startPoint,
       PointGeometryProxy endPoint);

  /**
   * Creates a new edge using default values.
   * This method creates an edge with
   * the rendering information set to <CODE>null</CODE>,
   * the rendering information for the start point set to <CODE>null</CODE>, and
   * the rendering information for the end point set to <CODE>null</CODE>.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   */
  public EdgeProxy createEdgeProxy
      (NodeProxy source,
       NodeProxy target,
       LabelBlockProxy labelBlock);

  /**
   * Creates a new enumerated range.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionProxy createEnumSetExpressionProxy
      (Collection<? extends SimpleIdentifierProxy> items);

  /**
   * Creates a new enumerated range using default values.
   * This method creates an enumerated range with
   * an empty list of items.
   */
  public EnumSetExpressionProxy createEnumSetExpressionProxy();

  /**
   * Creates a new event declaration.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   * @param observable The observability status of the new event declaration.
   * @param ranges The list of index ranges of the new event declaration, or <CODE>null</CODE> if empty.
   * @param colorGeometry The color information of the new event declaration, or <CODE>null</CODE>.
   */
  public EventDeclProxy createEventDeclProxy
      (String name,
       EventKind kind,
       boolean observable,
       Collection<? extends SimpleExpressionProxy> ranges,
       ColorGeometryProxy colorGeometry);

  /**
   * Creates a new event declaration using default values.
   * This method creates an event declaration with
   * the observability status set to <CODE>true</CODE>,
   * an empty list of index ranges, and
   * the color information set to <CODE>null</CODE>.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   */
  public EventDeclProxy createEventDeclProxy
      (String name,
       EventKind kind);

  /**
   * Creates a new event parameter.
   * @param name The name of the new event parameter.
   * @param required The required status of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterProxy createEventParameterProxy
      (String name,
       boolean required,
       EventDeclProxy eventDecl);

  /**
   * Creates a new event parameter using default values.
   * This method creates an event parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterProxy createEventParameterProxy
      (String name,
       EventDeclProxy eventDecl);

  /**
   * Creates a new foreach construct for module components.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   * @param guard The guard of the new foreach construct for module components, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for module components, or <CODE>null</CODE> if empty.
   */
  public ForeachComponentProxy createForeachComponentProxy
      (String name,
       SimpleExpressionProxy range,
       SimpleExpressionProxy guard,
       Collection<? extends Proxy> body);

  /**
   * Creates a new foreach construct for module components using default values.
   * This method creates a foreach construct for module components with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   */
  public ForeachComponentProxy createForeachComponentProxy
      (String name,
       SimpleExpressionProxy range);

  /**
   * Creates a new foreach construct for aliases.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   * @param guard The guard of the new foreach construct for aliases, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for aliases, or <CODE>null</CODE> if empty.
   */
  public ForeachEventAliasProxy createForeachEventAliasProxy
      (String name,
       SimpleExpressionProxy range,
       SimpleExpressionProxy guard,
       Collection<? extends Proxy> body);

  /**
   * Creates a new foreach construct for aliases using default values.
   * This method creates a foreach construct for aliases with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   */
  public ForeachEventAliasProxy createForeachEventAliasProxy
      (String name,
       SimpleExpressionProxy range);

  /**
   * Creates a new foreach construct for events.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   * @param guard The guard of the new foreach construct for events, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for events, or <CODE>null</CODE> if empty.
   */
  public ForeachEventProxy createForeachEventProxy
      (String name,
       SimpleExpressionProxy range,
       SimpleExpressionProxy guard,
       Collection<? extends Proxy> body);

  /**
   * Creates a new foreach construct for events using default values.
   * This method creates a foreach construct for events with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   */
  public ForeachEventProxy createForeachEventProxy
      (String name,
       SimpleExpressionProxy range);

  /**
   * Creates a new graph.
   * @param deterministic The determinism status of the new graph.
   * @param blockedEvents The list of blocked events of the new graph.
   * @param nodes The set of nodes of the new graph, or <CODE>null</CODE> if empty.
   * @param edges The list of edges of the new graph, or <CODE>null</CODE> if empty.
   */
  public GraphProxy createGraphProxy
      (boolean deterministic,
       LabelBlockProxy blockedEvents,
       Collection<? extends NodeProxy> nodes,
       Collection<? extends EdgeProxy> edges);

  /**
   * Creates a new graph using default values.
   * This method creates a graph with
   * the determinism status set to <CODE>true</CODE>,
   * an empty set of nodes, and
   * an empty list of edges.
   * @param blockedEvents The list of blocked events of the new graph.
   */
  public GraphProxy createGraphProxy
      (LabelBlockProxy blockedEvents);

  /**
   * Creates a new group node.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   * @param immediateChildNodes The set of immediate child nodes of the new group node, or <CODE>null</CODE> if empty.
   * @param geometry The geometric information of the new group node, or <CODE>null</CODE>.
   */
  public GroupNodeProxy createGroupNodeProxy
      (String name,
       EventListExpressionProxy propositions,
       Collection<? extends NodeProxy> immediateChildNodes,
       BoxGeometryProxy geometry);

  /**
   * Creates a new group node using default values.
   * This method creates a group node with
   * an empty set of immediate child nodes and
   * the geometric information set to <CODE>null</CODE>.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   */
  public GroupNodeProxy createGroupNodeProxy
      (String name,
       EventListExpressionProxy propositions);

  /**
   * Creates a new indexed identifier.
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierProxy createIndexedIdentifierProxy
      (String name,
       Collection<? extends SimpleExpressionProxy> indexes);

  /**
   * Creates a new indexed identifier using default values.
   * This method creates an indexed identifier with
   * an empty list of array indexes.
   * @param name The name of the new indexed identifier.
   */
  public IndexedIdentifierProxy createIndexedIdentifierProxy
      (String name);

  /**
   * Creates a new instance.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   * @param bindingList The binding list of the new instance, or <CODE>null</CODE> if empty.
   */
  public InstanceProxy createInstanceProxy
      (IdentifierProxy identifier,
       String moduleName,
       Collection<? extends ParameterBindingProxy> bindingList);

  /**
   * Creates a new instance using default values.
   * This method creates an instance with
   * an empty binding list.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   */
  public InstanceProxy createInstanceProxy
      (IdentifierProxy identifier,
       String moduleName);

  /**
   * Creates a new integer constant.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantProxy createIntConstantProxy
      (int value);

  /**
   * Creates a new integer parameter.
   * @param name The name of the new integer parameter.
   * @param required The required status of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterProxy createIntParameterProxy
      (String name,
       boolean required,
       SimpleExpressionProxy defaultValue);

  /**
   * Creates a new integer parameter using default values.
   * This method creates an integer parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterProxy createIntParameterProxy
      (String name,
       SimpleExpressionProxy defaultValue);

  /**
   * Creates a new label block.
   * @param eventList The list of events of the new label block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new label block, or <CODE>null</CODE>.
   */
  public LabelBlockProxy createLabelBlockProxy
      (Collection<? extends Proxy> eventList,
       LabelGeometryProxy geometry);

  /**
   * Creates a new label block using default values.
   * This method creates a label block with
   * an empty list of events and
   * the geometry set to <CODE>null</CODE>.
   */
  public LabelBlockProxy createLabelBlockProxy();

  /**
   * Creates a new label geometry.
   * @param offset The offset of the new label geometry.
   * @param anchor The anchor position of the new label geometry.
   */
  public LabelGeometryProxy createLabelGeometryProxy
      (Point2D offset,
       AnchorPosition anchor);

  /**
   * Creates a new label geometry using default values.
   * This method creates a label geometry with
   * the anchor position set to <CODE>AnchorPosition.NW</CODE>.
   * @param offset The offset of the new label geometry.
   */
  public LabelGeometryProxy createLabelGeometryProxy
      (Point2D offset);

  /**
   * Creates a new module.
   * @param name The name of the new module.
   * @param location The location of the new module.
   * @param parameterList The parameter list of the new module, or <CODE>null</CODE> if empty.
   * @param constantAliasList The constant definition list of the new module, or <CODE>null</CODE> if empty.
   * @param eventDeclList The event declaration list of the new module, or <CODE>null</CODE> if empty.
   * @param eventAliasList The event alias list of the new module, or <CODE>null</CODE> if empty.
   * @param componentList The component list of the new module, or <CODE>null</CODE> if empty.
   */
  public ModuleProxy createModuleProxy
      (String name,
       URI location,
       Collection<? extends ParameterProxy> parameterList,
       Collection<? extends AliasProxy> constantAliasList,
       Collection<? extends EventDeclProxy> eventDeclList,
       Collection<? extends Proxy> eventAliasList,
       Collection<? extends Proxy> componentList);

  /**
   * Creates a new module using default values.
   * This method creates a module with
   * an empty parameter list,
   * an empty constant definition list,
   * an empty event declaration list,
   * an empty event alias list, and
   * an empty component list.
   * @param name The name of the new module.
   * @param location The location of the new module.
   */
  public ModuleProxy createModuleProxy
      (String name,
       URI location);

  /**
   * Creates a new parameter binding.
   * @param name The name of the new parameter binding.
   * @param expression The expression of the new parameter binding.
   */
  public ParameterBindingProxy createParameterBindingProxy
      (String name,
       ExpressionProxy expression);

  /**
   * Creates a new plain event list.
   * @param eventList The list of events of the new plain event list, or <CODE>null</CODE> if empty.
   */
  public PlainEventListProxy createPlainEventListProxy
      (Collection<? extends Proxy> eventList);

  /**
   * Creates a new plain event list using default values.
   * This method creates a plain event list with
   * an empty list of events.
   */
  public PlainEventListProxy createPlainEventListProxy();

  /**
   * Creates a new point geometry.
   * @param point The point of the new point geometry.
   */
  public PointGeometryProxy createPointGeometryProxy
      (Point2D point);

  /**
   * Creates a new range parameter.
   * @param name The name of the new range parameter.
   * @param required The required status of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterProxy createRangeParameterProxy
      (String name,
       boolean required,
       SimpleExpressionProxy defaultValue);

  /**
   * Creates a new range parameter using default values.
   * This method creates a range parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterProxy createRangeParameterProxy
      (String name,
       SimpleExpressionProxy defaultValue);

  /**
   * Creates a new simple component.
   * @param identifier The identifier defining the name of the new simple component.
   * @param kind The kind of the new simple component.
   * @param graph The graph of the new simple component.
   */
  public SimpleComponentProxy createSimpleComponentProxy
      (IdentifierProxy identifier,
       ComponentKind kind,
       GraphProxy graph);

  /**
   * Creates a new simple identifier.
   * @param name The name of the new simple identifier.
   */
  public SimpleIdentifierProxy createSimpleIdentifierProxy
      (String name);

  /**
   * Creates a new simple node.
   * @param name The name of the new simple node.
   * @param propositions The list of propositions of the new simple node.
   * @param initial The initial status of the new simple node.
   * @param pointGeometry The geometric position of the new simple node, or <CODE>null</CODE>.
   * @param initialArrowGeometry The position of the initial state arrow of the new simple node, or <CODE>null</CODE>.
   * @param labelGeometry The geometric position of the label of the new simple node, or <CODE>null</CODE>.
   */
  public SimpleNodeProxy createSimpleNodeProxy
      (String name,
       EventListExpressionProxy propositions,
       boolean initial,
       PointGeometryProxy pointGeometry,
       PointGeometryProxy initialArrowGeometry,
       LabelGeometryProxy labelGeometry);

  /**
   * Creates a new simple node using default values.
   * This method creates a simple node with
   * the initial status set to <CODE>false</CODE>,
   * the geometric position set to <CODE>null</CODE>,
   * the position of the initial state arrow set to <CODE>null</CODE>, and
   * the geometric position of the label set to <CODE>null</CODE>.
   * @param name The name of the new simple node.
   * @param propositions The list of propositions of the new simple node.
   */
  public SimpleNodeProxy createSimpleNodeProxy
      (String name,
       EventListExpressionProxy propositions);

  /**
   * Creates a new spline geometry.
   * @param points The list of control points of the new spline geometry, or <CODE>null</CODE> if empty.
   * @param kind The kind of the new spline geometry.
   */
  public SplineGeometryProxy createSplineGeometryProxy
      (Collection<? extends Point2D> points,
       SplineKind kind);

  /**
   * Creates a new spline geometry using default values.
   * This method creates a spline geometry with
   * an empty list of control points and
   * the kind set to <CODE>SplineKind.INTERPOLATING</CODE>.
   */
  public SplineGeometryProxy createSplineGeometryProxy();

  /**
   * Creates a new unary expression.
   * @param operator The operator of the new unary expression.
   * @param subTerm The subterm of the new unary expression.
   */
  public UnaryExpressionProxy createUnaryExpressionProxy
      (UnaryOperator operator,
       SimpleExpressionProxy subTerm);

}
