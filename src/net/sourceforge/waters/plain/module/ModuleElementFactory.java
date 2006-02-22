//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ModuleElementFactory
//###########################################################################
//# $Id: ModuleElementFactory.java,v 1.5 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.SplineKind;


public class ModuleElementFactory
  implements ModuleProxyFactory
{

  //#########################################################################
  //# Static Class Methods
  public static ModuleElementFactory getInstance()
  {
    return INSTANCE;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyFactory
  /**
   * Creates a new alias.
   * @param identifier The identifier defining the name of the new alias.
   * @param expression The expression of the new alias.
   */
  public AliasElement createAliasProxy
      (final IdentifierProxy identifier,
       final ExpressionProxy expression)
  {
    return new AliasElement(identifier,
                            expression);
  }

  /**
   * Creates a new binary expression.
   * @param operator The operator of the new binary expression.
   * @param left The left subterm of the new binary expression.
   * @param right The right subterm of the new binary expression.
   */
  public BinaryExpressionElement createBinaryExpressionProxy
      (final BinaryOperator operator,
       final SimpleExpressionProxy left,
       final SimpleExpressionProxy right)
  {
    return new BinaryExpressionElement(operator,
                                       left,
                                       right);
  }

  /**
   * Creates a new box geometry.
   * @param rectangle The rectangle of the new box geometry.
   */
  public BoxGeometryElement createBoxGeometryProxy
      (final Rectangle2D rectangle)
  {
    return new BoxGeometryElement(rectangle);
  }

  /**
   * Creates a new color geometry.
   * @param colorSet The colour set of the new color geometry, or <CODE>null</CODE> if empty.
   */
  public ColorGeometryElement createColorGeometryProxy
      (final Collection<? extends Color> colorSet)
  {
    return new ColorGeometryElement(colorSet);
  }

  /**
   * Creates a new color geometry using default values.
   * This method creates a color geometry with
   * an empty colour set.
   */
  public ColorGeometryElement createColorGeometryProxy()
  {
    return new ColorGeometryElement();
  }

  /**
   * Creates a new edge.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   * @param geometry The rendering information of the new edge, or <CODE>null</CODE>.
   * @param startPoint The rendering information for the start point of the new edge, or <CODE>null</CODE>.
   * @param endPoint The rendering information for the end point of the new edge, or <CODE>null</CODE>.
   */
  public EdgeElement createEdgeProxy
      (final NodeProxy source,
       final NodeProxy target,
       final LabelBlockProxy labelBlock,
       final SplineGeometryProxy geometry,
       final PointGeometryProxy startPoint,
       final PointGeometryProxy endPoint)
  {
    return new EdgeElement(source,
                           target,
                           labelBlock,
                           geometry,
                           startPoint,
                           endPoint);
  }

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
  public EdgeElement createEdgeProxy
      (final NodeProxy source,
       final NodeProxy target,
       final LabelBlockProxy labelBlock)
  {
    return new EdgeElement(source,
                           target,
                           labelBlock);
  }

  /**
   * Creates a new enumerated range.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionElement createEnumSetExpressionProxy
      (final Collection<? extends SimpleIdentifierProxy> items)
  {
    return new EnumSetExpressionElement(items);
  }

  /**
   * Creates a new enumerated range using default values.
   * This method creates an enumerated range with
   * an empty list of items.
   */
  public EnumSetExpressionElement createEnumSetExpressionProxy()
  {
    return new EnumSetExpressionElement();
  }

  /**
   * Creates a new event declaration.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   * @param observable The observability status of the new event declaration.
   * @param ranges The list of index ranges of the new event declaration, or <CODE>null</CODE> if empty.
   * @param colorGeometry The color information of the new event declaration, or <CODE>null</CODE>.
   */
  public EventDeclElement createEventDeclProxy
      (final String name,
       final EventKind kind,
       final boolean observable,
       final Collection<? extends SimpleExpressionProxy> ranges,
       final ColorGeometryProxy colorGeometry)
  {
    return new EventDeclElement(name,
                                kind,
                                observable,
                                ranges,
                                colorGeometry);
  }

  /**
   * Creates a new event declaration using default values.
   * This method creates an event declaration with
   * the observability status set to <CODE>true</CODE>,
   * an empty list of index ranges, and
   * the color information set to <CODE>null</CODE>.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   */
  public EventDeclElement createEventDeclProxy
      (final String name,
       final EventKind kind)
  {
    return new EventDeclElement(name,
                                kind);
  }

  /**
   * Creates a new event parameter.
   * @param name The name of the new event parameter.
   * @param required The required status of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterElement createEventParameterProxy
      (final String name,
       final boolean required,
       final EventDeclProxy eventDecl)
  {
    return new EventParameterElement(name,
                                     required,
                                     eventDecl);
  }

  /**
   * Creates a new event parameter using default values.
   * This method creates an event parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterElement createEventParameterProxy
      (final String name,
       final EventDeclProxy eventDecl)
  {
    return new EventParameterElement(name,
                                     eventDecl);
  }

  /**
   * Creates a new foreach construct for module components.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   * @param guard The guard of the new foreach construct for module components, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for module components, or <CODE>null</CODE> if empty.
   */
  public ForeachComponentElement createForeachComponentProxy
      (final String name,
       final SimpleExpressionProxy range,
       final SimpleExpressionProxy guard,
       final Collection<? extends Proxy> body)
  {
    return new ForeachComponentElement(name,
                                       range,
                                       guard,
                                       body);
  }

  /**
   * Creates a new foreach construct for module components using default values.
   * This method creates a foreach construct for module components with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   */
  public ForeachComponentElement createForeachComponentProxy
      (final String name,
       final SimpleExpressionProxy range)
  {
    return new ForeachComponentElement(name,
                                       range);
  }

  /**
   * Creates a new foreach construct for aliases.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   * @param guard The guard of the new foreach construct for aliases, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for aliases, or <CODE>null</CODE> if empty.
   */
  public ForeachEventAliasElement createForeachEventAliasProxy
      (final String name,
       final SimpleExpressionProxy range,
       final SimpleExpressionProxy guard,
       final Collection<? extends Proxy> body)
  {
    return new ForeachEventAliasElement(name,
                                        range,
                                        guard,
                                        body);
  }

  /**
   * Creates a new foreach construct for aliases using default values.
   * This method creates a foreach construct for aliases with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   */
  public ForeachEventAliasElement createForeachEventAliasProxy
      (final String name,
       final SimpleExpressionProxy range)
  {
    return new ForeachEventAliasElement(name,
                                        range);
  }

  /**
   * Creates a new foreach construct for events.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   * @param guard The guard of the new foreach construct for events, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for events, or <CODE>null</CODE> if empty.
   */
  public ForeachEventElement createForeachEventProxy
      (final String name,
       final SimpleExpressionProxy range,
       final SimpleExpressionProxy guard,
       final Collection<? extends Proxy> body)
  {
    return new ForeachEventElement(name,
                                   range,
                                   guard,
                                   body);
  }

  /**
   * Creates a new foreach construct for events using default values.
   * This method creates a foreach construct for events with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   */
  public ForeachEventElement createForeachEventProxy
      (final String name,
       final SimpleExpressionProxy range)
  {
    return new ForeachEventElement(name,
                                   range);
  }

  /**
   * Creates a new graph.
   * @param deterministic The determinism status of the new graph.
   * @param blockedEvents The list of blocked events of the new graph.
   * @param nodes The set of nodes of the new graph, or <CODE>null</CODE> if empty.
   * @param edges The list of edges of the new graph, or <CODE>null</CODE> if empty.
   */
  public GraphElement createGraphProxy
      (final boolean deterministic,
       final LabelBlockProxy blockedEvents,
       final Collection<? extends NodeProxy> nodes,
       final Collection<? extends EdgeProxy> edges)
  {
    return new GraphElement(deterministic,
                            blockedEvents,
                            nodes,
                            edges);
  }

  /**
   * Creates a new graph using default values.
   * This method creates a graph with
   * the determinism status set to <CODE>true</CODE>,
   * an empty set of nodes, and
   * an empty list of edges.
   * @param blockedEvents The list of blocked events of the new graph.
   */
  public GraphElement createGraphProxy
      (final LabelBlockProxy blockedEvents)
  {
    return new GraphElement(blockedEvents);
  }

  /**
   * Creates a new group node.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   * @param immediateChildNodes The set of immediate child nodes of the new group node, or <CODE>null</CODE> if empty.
   * @param geometry The geometric information of the new group node, or <CODE>null</CODE>.
   */
  public GroupNodeElement createGroupNodeProxy
      (final String name,
       final EventListExpressionProxy propositions,
       final Collection<? extends NodeProxy> immediateChildNodes,
       final BoxGeometryProxy geometry)
  {
    return new GroupNodeElement(name,
                                propositions,
                                immediateChildNodes,
                                geometry);
  }

  /**
   * Creates a new group node using default values.
   * This method creates a group node with
   * an empty set of immediate child nodes and
   * the geometric information set to <CODE>null</CODE>.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   */
  public GroupNodeElement createGroupNodeProxy
      (final String name,
       final EventListExpressionProxy propositions)
  {
    return new GroupNodeElement(name,
                                propositions);
  }

  /**
   * Creates a new indexed identifier.
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierElement createIndexedIdentifierProxy
      (final String name,
       final Collection<? extends SimpleExpressionProxy> indexes)
  {
    return new IndexedIdentifierElement(name,
                                        indexes);
  }

  /**
   * Creates a new indexed identifier using default values.
   * This method creates an indexed identifier with
   * an empty list of array indexes.
   * @param name The name of the new indexed identifier.
   */
  public IndexedIdentifierElement createIndexedIdentifierProxy
      (final String name)
  {
    return new IndexedIdentifierElement(name);
  }

  /**
   * Creates a new instance.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   * @param bindingList The binding list of the new instance, or <CODE>null</CODE> if empty.
   */
  public InstanceElement createInstanceProxy
      (final IdentifierProxy identifier,
       final String moduleName,
       final Collection<? extends ParameterBindingProxy> bindingList)
  {
    return new InstanceElement(identifier,
                               moduleName,
                               bindingList);
  }

  /**
   * Creates a new instance using default values.
   * This method creates an instance with
   * an empty binding list.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   */
  public InstanceElement createInstanceProxy
      (final IdentifierProxy identifier,
       final String moduleName)
  {
    return new InstanceElement(identifier,
                               moduleName);
  }

  /**
   * Creates a new integer constant.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantElement createIntConstantProxy
      (final int value)
  {
    return new IntConstantElement(value);
  }

  /**
   * Creates a new integer parameter.
   * @param name The name of the new integer parameter.
   * @param required The required status of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterElement createIntParameterProxy
      (final String name,
       final boolean required,
       final SimpleExpressionProxy defaultValue)
  {
    return new IntParameterElement(name,
                                   required,
                                   defaultValue);
  }

  /**
   * Creates a new integer parameter using default values.
   * This method creates an integer parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterElement createIntParameterProxy
      (final String name,
       final SimpleExpressionProxy defaultValue)
  {
    return new IntParameterElement(name,
                                   defaultValue);
  }

  /**
   * Creates a new label block.
   * @param eventList The list of events of the new label block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new label block, or <CODE>null</CODE>.
   */
  public LabelBlockElement createLabelBlockProxy
      (final Collection<? extends Proxy> eventList,
       final LabelGeometryProxy geometry)
  {
    return new LabelBlockElement(eventList,
                                 geometry);
  }

  /**
   * Creates a new label block using default values.
   * This method creates a label block with
   * an empty list of events and
   * the geometry set to <CODE>null</CODE>.
   */
  public LabelBlockElement createLabelBlockProxy()
  {
    return new LabelBlockElement();
  }

  /**
   * Creates a new label geometry.
   * @param offset The offset of the new label geometry.
   * @param anchor The anchor position of the new label geometry.
   */
  public LabelGeometryElement createLabelGeometryProxy
      (final Point2D offset,
       final AnchorPosition anchor)
  {
    return new LabelGeometryElement(offset,
                                    anchor);
  }

  /**
   * Creates a new label geometry using default values.
   * This method creates a label geometry with
   * the anchor position set to <CODE>AnchorPosition.NW</CODE>.
   * @param offset The offset of the new label geometry.
   */
  public LabelGeometryElement createLabelGeometryProxy
      (final Point2D offset)
  {
    return new LabelGeometryElement(offset);
  }

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
  public ModuleElement createModuleProxy
      (final String name,
       final URI location,
       final Collection<? extends ParameterProxy> parameterList,
       final Collection<? extends AliasProxy> constantAliasList,
       final Collection<? extends EventDeclProxy> eventDeclList,
       final Collection<? extends Proxy> eventAliasList,
       final Collection<? extends Proxy> componentList)
  {
    return new ModuleElement(name,
                             location,
                             parameterList,
                             constantAliasList,
                             eventDeclList,
                             eventAliasList,
                             componentList);
  }

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
  public ModuleElement createModuleProxy
      (final String name,
       final URI location)
  {
    return new ModuleElement(name,
                             location);
  }

  /**
   * Creates a new parameter binding.
   * @param name The name of the new parameter binding.
   * @param expression The expression of the new parameter binding.
   */
  public ParameterBindingElement createParameterBindingProxy
      (final String name,
       final ExpressionProxy expression)
  {
    return new ParameterBindingElement(name,
                                       expression);
  }

  /**
   * Creates a new plain event list.
   * @param eventList The list of events of the new plain event list, or <CODE>null</CODE> if empty.
   */
  public PlainEventListElement createPlainEventListProxy
      (final Collection<? extends Proxy> eventList)
  {
    return new PlainEventListElement(eventList);
  }

  /**
   * Creates a new plain event list using default values.
   * This method creates a plain event list with
   * an empty list of events.
   */
  public PlainEventListElement createPlainEventListProxy()
  {
    return new PlainEventListElement();
  }

  /**
   * Creates a new point geometry.
   * @param point The point of the new point geometry.
   */
  public PointGeometryElement createPointGeometryProxy
      (final Point2D point)
  {
    return new PointGeometryElement(point);
  }

  /**
   * Creates a new range parameter.
   * @param name The name of the new range parameter.
   * @param required The required status of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterElement createRangeParameterProxy
      (final String name,
       final boolean required,
       final SimpleExpressionProxy defaultValue)
  {
    return new RangeParameterElement(name,
                                     required,
                                     defaultValue);
  }

  /**
   * Creates a new range parameter using default values.
   * This method creates a range parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterElement createRangeParameterProxy
      (final String name,
       final SimpleExpressionProxy defaultValue)
  {
    return new RangeParameterElement(name,
                                     defaultValue);
  }

  /**
   * Creates a new simple component.
   * @param identifier The identifier defining the name of the new simple component.
   * @param kind The kind of the new simple component.
   * @param graph The graph of the new simple component.
   */
  public SimpleComponentElement createSimpleComponentProxy
      (final IdentifierProxy identifier,
       final ComponentKind kind,
       final GraphProxy graph)
  {
    return new SimpleComponentElement(identifier,
                                      kind,
                                      graph);
  }

  /**
   * Creates a new simple identifier.
   * @param name The name of the new simple identifier.
   */
  public SimpleIdentifierElement createSimpleIdentifierProxy
      (final String name)
  {
    return new SimpleIdentifierElement(name);
  }

  /**
   * Creates a new simple node.
   * @param name The name of the new simple node.
   * @param propositions The list of propositions of the new simple node.
   * @param initial The initial status of the new simple node.
   * @param pointGeometry The geometric position of the new simple node, or <CODE>null</CODE>.
   * @param initialArrowGeometry The position of the initial state arrow of the new simple node, or <CODE>null</CODE>.
   * @param labelGeometry The geometric position of the label of the new simple node, or <CODE>null</CODE>.
   */
  public SimpleNodeElement createSimpleNodeProxy
      (final String name,
       final EventListExpressionProxy propositions,
       final boolean initial,
       final PointGeometryProxy pointGeometry,
       final PointGeometryProxy initialArrowGeometry,
       final LabelGeometryProxy labelGeometry)
  {
    return new SimpleNodeElement(name,
                                 propositions,
                                 initial,
                                 pointGeometry,
                                 initialArrowGeometry,
                                 labelGeometry);
  }

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
  public SimpleNodeElement createSimpleNodeProxy
      (final String name,
       final EventListExpressionProxy propositions)
  {
    return new SimpleNodeElement(name,
                                 propositions);
  }

  /**
   * Creates a new spline geometry.
   * @param points The list of control points of the new spline geometry, or <CODE>null</CODE> if empty.
   * @param kind The kind of the new spline geometry.
   */
  public SplineGeometryElement createSplineGeometryProxy
      (final Collection<? extends Point2D> points,
       final SplineKind kind)
  {
    return new SplineGeometryElement(points,
                                     kind);
  }

  /**
   * Creates a new spline geometry using default values.
   * This method creates a spline geometry with
   * an empty list of control points and
   * the kind set to <CODE>SplineKind.INTERPOLATING</CODE>.
   */
  public SplineGeometryElement createSplineGeometryProxy()
  {
    return new SplineGeometryElement();
  }

  /**
   * Creates a new unary expression.
   * @param operator The operator of the new unary expression.
   * @param subTerm The subterm of the new unary expression.
   */
  public UnaryExpressionElement createUnaryExpressionProxy
      (final UnaryOperator operator,
       final SimpleExpressionProxy subTerm)
  {
    return new UnaryExpressionElement(operator,
                                      subTerm);
  }


  //#########################################################################
  //# Data Members
  private static final ModuleElementFactory INSTANCE =
    new ModuleElementFactory();

}
