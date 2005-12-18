//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleExporter
//###########################################################################
//# $Id: JAXBModuleExporter.java,v 1.4 2005-12-18 21:11:32 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.RangeParameterProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.module.BinaryExpressionType;
import net.sourceforge.waters.xsd.module.BoxGeometryType;
import net.sourceforge.waters.xsd.module.BoxType;
import net.sourceforge.waters.xsd.module.ColorGeometryType;
import net.sourceforge.waters.xsd.module.ColorType;
import net.sourceforge.waters.xsd.module.ConstantAliasType;
import net.sourceforge.waters.xsd.module.EdgeType;
import net.sourceforge.waters.xsd.module.EnumSetExpressionType;
import net.sourceforge.waters.xsd.module.EventAliasType;
import net.sourceforge.waters.xsd.module.EventBaseType;
import net.sourceforge.waters.xsd.module.EventDeclType;
import net.sourceforge.waters.xsd.module.EventListExpressionType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.EventParameterType;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.ForeachComponentType;
import net.sourceforge.waters.xsd.module.ForeachEventAliasType;
import net.sourceforge.waters.xsd.module.ForeachEventType;
import net.sourceforge.waters.xsd.module.ForeachType;
import net.sourceforge.waters.xsd.module.GraphType;
import net.sourceforge.waters.xsd.module.GroupNodeType;
import net.sourceforge.waters.xsd.module.IdentifiedType;
import net.sourceforge.waters.xsd.module.IdentifierType;
import net.sourceforge.waters.xsd.module.IndexedIdentifierType;
import net.sourceforge.waters.xsd.module.InstanceType;
import net.sourceforge.waters.xsd.module.IntConstantType;
import net.sourceforge.waters.xsd.module.IntParameterType;
import net.sourceforge.waters.xsd.module.LabelBlockType;
import net.sourceforge.waters.xsd.module.LabelGeometryType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.NodeType;
import net.sourceforge.waters.xsd.module.NodeRefType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.ParameterBindingType;
import net.sourceforge.waters.xsd.module.PointGeometryType;
import net.sourceforge.waters.xsd.module.PointType;
import net.sourceforge.waters.xsd.module.RangeParameterType;
import net.sourceforge.waters.xsd.module.SimpleComponentType;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifierType;
import net.sourceforge.waters.xsd.module.SimpleNodeType;
import net.sourceforge.waters.xsd.module.SimpleParameterType;
import net.sourceforge.waters.xsd.module.SplineGeometryType;
import net.sourceforge.waters.xsd.module.UnaryExpressionType;


public class JAXBModuleExporter
  extends JAXBDocumentExporter<ModuleProxy,ModuleType>
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Overrides for Abstract Base Class JAXBExporter
  ModuleType exportDocument(final ModuleProxy proxy)
    throws VisitorException
  {
    return visitModuleProxy(proxy);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public ElementType visitAliasProxy(final AliasProxy proxy)
    throws VisitorException
  {
    try {
      final ExpressionProxy expr = proxy.getExpression();
      if (expr instanceof SimpleExpressionProxy) {
        final ConstantAliasType element = mFactory.createConstantAlias();
        copyConstantAliasProxy(proxy, element);
        return element;
      } else if (expr instanceof EventListExpressionProxy) {
        final EventAliasType element = mFactory.createEventAlias();
        copyEventAliasProxy(proxy, element);
        return element;
      } else {
        throw new ClassCastException
          ("Unknown expression type in alias '" + proxy.getName() + "'!");
      }
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public BinaryExpressionType visitBinaryExpressionProxy
      (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final BinaryExpressionType element = mFactory.createBinaryExpression();
      copyBinaryExpressionProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public BoxGeometryType visitBoxGeometryProxy
      (final BoxGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final BoxGeometryType element = mFactory.createBoxGeometry();
      copyBoxGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public ColorGeometryType visitColorGeometryProxy
      (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final ColorGeometryType element = mFactory.createColorGeometry();
      copyColorGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitComponentProxy
      (final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public EdgeType visitEdgeProxy
      (final EdgeProxy proxy)
    throws VisitorException
  {
    try {
      final EdgeType element = mFactory.createEdge();
      copyEdgeProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public PointGeometryType visitEndPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final PointGeometryType element = mFactory.createEndPointGeometry();
      copyPointGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public EnumSetExpressionType visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final EnumSetExpressionType element = mFactory.createEnumSetExpression();
      copyEnumSetExpressionProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public EventDeclType visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    try {
      final EventDeclType element = mFactory.createEventDecl();
      copyEventDeclProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public EventParameterType visitEventParameterProxy
      (final EventParameterProxy proxy)
    throws VisitorException
  {
    try {
      final EventParameterType element = mFactory.createEventParameter();
      copyEventParameterProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitExpressionProxy
      (final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public ForeachComponentType visitForeachComponentProxy
      (final ForeachComponentProxy proxy)
    throws VisitorException
  {
    try {
      final ForeachComponentType element = mFactory.createForeachComponent();
      copyForeachComponentProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public ForeachEventAliasType visitForeachEventAliasProxy
      (final ForeachEventAliasProxy proxy)
    throws VisitorException
  {
    try {
      final ForeachEventAliasType element = mFactory.createForeachEventAlias();
      copyForeachEventAliasProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public ForeachEventType visitForeachEventProxy
      (final ForeachEventProxy proxy)
    throws VisitorException
  {
    try {
      final ForeachEventType element = mFactory.createForeachEvent();
      copyForeachEventProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public GraphType visitGraphProxy
      (final GraphProxy proxy)
    throws VisitorException
  {
    try {
      final GraphType element = mFactory.createGraph();
      copyGraphProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public GroupNodeType visitGroupNodeProxy
      (final GroupNodeProxy proxy)
    throws VisitorException
  {
    try {
      final GroupNodeType element = mFactory.createGroupNode();
      copyGroupNodeProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitIdentifiedProxy
      (final IdentifiedProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitIdentifierProxy
      (final IdentifierProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  public IndexedIdentifierType visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    try {
      final IndexedIdentifierType element = mFactory.createIndexedIdentifier();
      copyIndexedIdentifierProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public PointGeometryType visitInitialArrowGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final PointGeometryType element = mFactory.createInitialArrowGeometry();
      copyPointGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public InstanceType visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    try {
      final InstanceType element = mFactory.createInstance();
      copyInstanceProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public IntConstantType visitIntConstantProxy
      (final IntConstantProxy proxy)
    throws VisitorException
  {
    try {
      final IntConstantType element = mFactory.createIntConstant();
      copyIntConstantProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public IntParameterType visitIntParameterProxy
      (final IntParameterProxy proxy)
    throws VisitorException
  {
    try {
      final IntParameterType element = mFactory.createIntParameter();
      copyIntParameterProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public LabelBlockType visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    try {
      final LabelBlockType element = mFactory.createLabelBlock();
      copyLabelBlockProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public LabelGeometryType visitLabelGeometryProxy
      (final LabelGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final LabelGeometryType element = mFactory.createLabelGeometry();
      copyLabelGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public ModuleType visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    try {
      final ModuleType element = mFactory.createModule();
      copyModuleProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitNodeProxy
      (final NodeProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public ParameterBindingType visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    try {
      final ParameterBindingType element = mFactory.createParameterBinding();
      copyParameterBindingProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitParameterProxy
      (final ParameterProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public EventListExpressionType visitPlainEventListProxy
      (final PlainEventListProxy proxy)
    throws VisitorException
  {
    try {
      final EventListExpressionType element =
        mFactory.createEventListExpression();
      copyPlainEventListProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public PointGeometryType visitPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final PointGeometryType element = mFactory.createPointGeometry();
      copyPointGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public RangeParameterType visitRangeParameterProxy
      (final RangeParameterProxy proxy)
    throws VisitorException
  {
    try {
      final RangeParameterType element = mFactory.createRangeParameter();
      copyRangeParameterProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public SimpleComponentType visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    try {
      final SimpleComponentType element = mFactory.createSimpleComponent();
      copySimpleComponentProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public SimpleIdentifierType visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    try {
      final SimpleIdentifierType element = mFactory.createSimpleIdentifier();
      copySimpleIdentifierProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public SimpleNodeType visitSimpleNodeProxy
      (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    try {
      final SimpleNodeType element = mFactory.createSimpleNode();
      copySimpleNodeProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public Object visitSimpleParameterProxy
      (final SimpleParameterProxy proxy)
    throws VisitorException
  {
    return visitParameterProxy(proxy);
  }

  public SplineGeometryType visitSplineGeometryProxy
      (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final SplineGeometryType element = mFactory.createSplineGeometry();
      copySplineGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public PointGeometryType visitStartPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    try {
      final PointGeometryType element = mFactory.createStartPointGeometry();
      copyPointGeometryProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public UnaryExpressionType visitUnaryExpressionProxy
      (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final UnaryExpressionType element = mFactory.createUnaryExpression();
      copyUnaryExpressionProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Copying Data
  private void copyAliasProxy
      (final AliasProxy proxy,
       final IdentifiedType element)
    throws VisitorException
  {
    copyIdentifiedProxy(proxy, element);
  }

  private void copyBinaryExpressionProxy
      (final BinaryExpressionProxy proxy,
       final BinaryExpressionType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    final BinaryOperator operator = proxy.getOperator();
    element.setOperator(operator.getName());
    final SimpleExpressionProxy leftProxy = proxy.getLeft();
    final SimpleExpressionType leftElement =
      (SimpleExpressionType) leftProxy.acceptVisitor(this);
    element.setLeft(leftElement);
    final SimpleExpressionProxy rightProxy = proxy.getRight();
    final SimpleExpressionType rightElement =
      (SimpleExpressionType) rightProxy.acceptVisitor(this);
    element.setRight(rightElement);
  }

  private void copyBoxGeometryProxy
      (final BoxGeometryProxy proxy,
       final BoxGeometryType element)
    throws VisitorException
  {
    try {
      copyGeometryProxy(proxy, element);
      final Rectangle2D rectangle = proxy.getRectangle();
      final BoxType box = createBox(rectangle);
      element.setBox(box);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyColorGeometryProxy
      (final ColorGeometryProxy proxy,
       final ColorGeometryType element)
    throws VisitorException
  {
    try {
      copyGeometryProxy(proxy, element);
      final Set<Color> colorSetProxy = proxy.getColorSet();
      for (final Color color : colorSetProxy) {
        final int red = color.getRed();
        final int green = color.getGreen();
        final int blue = color.getBlue();
        final ColorType colorElement = createColor(red, green, blue);
        element.setColor(colorElement);
        // *** BUG *** Can't handle more than one color *** 
      }
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyComponentProxy
      (final ComponentProxy proxy,
       final IdentifiedType element)
    throws VisitorException
  {
    copyIdentifiedProxy(proxy, element);
  }

  private void copyConstantAliasProxy
      (final AliasProxy proxy,
       final ConstantAliasType element)
    throws VisitorException
  {
    copyAliasProxy(proxy, element);
    final ExpressionProxy expressionProxy = proxy.getExpression();
    final SimpleExpressionType expressionElement =
      (SimpleExpressionType) expressionProxy.acceptVisitor(this);
    element.setExpression(expressionElement);
  }

  private void copyEdgeProxy
      (final EdgeProxy proxy,
       final EdgeType element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final NodeProxy sourceProxy = proxy.getSource();
    element.setSource(sourceProxy.getName());
    final NodeProxy targetProxy = proxy.getTarget();
    element.setTarget(targetProxy.getName());
    final LabelBlockProxy labelBlockProxy = proxy.getLabelBlock();
    if (!labelBlockProxy.getEventList().isEmpty()) {
      final LabelBlockType labelBlockElement =
        visitLabelBlockProxy(labelBlockProxy);
      element.setLabelBlock(labelBlockElement);
    }
    final SplineGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final SplineGeometryType geometryElement =
        visitSplineGeometryProxy(geometryProxy);
      element.setSplineGeometry(geometryElement);
    }
    final PointGeometryProxy startPointProxy = proxy.getStartPoint();
    if (startPointProxy != null) {
      final PointGeometryType startPointElement =
        visitStartPointGeometryProxy(startPointProxy);
      element.setStartPointGeometry(startPointElement);
    }
    final PointGeometryProxy endPointProxy = proxy.getEndPoint();
    if (endPointProxy != null) {
      final PointGeometryType endPointElement =
        visitEndPointGeometryProxy(endPointProxy);
      element.setEndPointGeometry(endPointElement);
    }
  }

  private void copyEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy,
       final EnumSetExpressionType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    final List<SimpleIdentifierProxy> itemsProxy = proxy.getItems();
    final List<ElementType> itemsElement = Casting.toList(element.getItems());
    copyCollection(itemsProxy, itemsElement);
  }

  private void copyEventAliasProxy
      (final AliasProxy proxy,
       final EventAliasType element)
    throws VisitorException
  {
    copyAliasProxy(proxy, element);
    final ExpressionProxy eventListProxy = proxy.getExpression();
    final EventListExpressionType eventListElement =
      (EventListExpressionType) eventListProxy.acceptVisitor(this);
    element.setExpression(eventListElement);
  }

  private void copyEventDeclProxy
      (final EventDeclProxy proxy,
       final EventBaseType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    element.setKind(proxy.getKind());
    element.setObservable(proxy.isObservable());
    final List<SimpleExpressionProxy> rangesProxy = proxy.getRanges();
    final List<ElementType> rangesElement =
      Casting.toList(element.getRanges());
    copyCollection(rangesProxy, rangesElement);
    final ColorGeometryProxy colorGeometryProxy = proxy.getColorGeometry();
    if (colorGeometryProxy != null) {
      final ColorGeometryType colorGeometryElement =
        visitColorGeometryProxy(colorGeometryProxy);
      element.setColorGeometry(colorGeometryElement);
    }
  }

  private void copyEventParameterProxy
      (final EventParameterProxy proxy,
       final EventParameterType element)
    throws VisitorException
  {
    final EventDeclProxy eventDeclProxy = proxy.getEventDecl();
    copyEventDeclProxy(eventDeclProxy, element);
    element.setRequired(proxy.isRequired());
  }

  private void copyExpressionProxy
      (final ExpressionProxy proxy,
       final ExpressionType element)
    throws VisitorException
  {
    copyProxy(proxy, element);
  }

  private void copyForeachComponentProxy
      (final ForeachComponentProxy proxy,
       final ForeachComponentType element)
    throws VisitorException
  {
    try {
      copyForeachProxy(proxy, element);
      final List<Proxy> body = proxy.getBody();
      mForeachComponentListHandler.toJAXB(this, body, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyForeachEventAliasProxy
      (final ForeachEventAliasProxy proxy,
       final ForeachEventAliasType element)
    throws VisitorException
  {
    try {
      copyForeachProxy(proxy, element);
      final List<Proxy> body = proxy.getBody();
      mForeachEventAliasListHandler.toJAXB(this, body, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyForeachEventProxy
      (final ForeachEventProxy proxy,
       final ForeachEventType element)
    throws VisitorException
  {
    try {
      copyForeachProxy(proxy, element);
      final List<Proxy> body = proxy.getBody();
      mForeachEventListHandler.toJAXB(this, body, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyForeachProxy
      (final ForeachProxy proxy,
       final ForeachType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final SimpleExpressionProxy rangeProxy = proxy.getRange();
    final SimpleExpressionType rangeElement =
      (SimpleExpressionType) rangeProxy.acceptVisitor(this);
    element.setRange(rangeElement);
    final SimpleExpressionProxy guardProxy = proxy.getGuard();
    if (guardProxy != null) {
      final SimpleExpressionType guardElement =
        (SimpleExpressionType) guardProxy.acceptVisitor(this);
      element.setGuard(guardElement);
    }
  }

  private void copyGraphProxy
      (final GraphProxy proxy,
       final GraphType element)
    throws VisitorException
  {
    try {
      copyProxy(proxy, element);
      element.setDeterministic(proxy.isDeterministic());
      final LabelBlockProxy blockedEventsProxy = proxy.getBlockedEvents();
      if (!blockedEventsProxy.getEventList().isEmpty()) {
        final LabelBlockType blockedEventsElement =
          visitLabelBlockProxy(blockedEventsProxy);
        element.setBlockedEvents(blockedEventsElement);
      }
      mGraphNodeList = new CheckedExportList<NodeProxy>(proxy, "node");
      for (final NodeProxy node : proxy.getNodes()) {
        mGraphNodeList.checkAllUnique(node.getImmediateChildNodes());
        mGraphNodeList.insertUnique(node);
      }
      mGraphNodeListHandler.toJAXB(this, mGraphNodeList, element);
      final List<EdgeProxy> edgesProxy = proxy.getEdges();
      mGraphEdgeListHandler.toJAXB(this, edgesProxy, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    } finally {
      mGraphNodeList = null;
    }
  }

  private void copyGroupNodeProxy
      (final GroupNodeProxy proxy,
       final GroupNodeType element)
    throws VisitorException
  {
    try {
      copyNodeProxy(proxy, element);
      final Set<NodeProxy> childrenProxy = proxy.getImmediateChildNodes();
      final IndexedSet<NodeProxy> childrenChecked =
        new CheckedExportSet<NodeProxy>(childrenProxy, proxy, "node");
      final List<ElementType> childrenElement =
        Casting.toList(element.getNodes());
      for (final NodeProxy node : childrenChecked) {
        mGraphNodeList.checkUnique(node);
        final NodeRefType noderef = mFactory.createNodeRef();
        copyNamedProxy(node, noderef);
        childrenElement.add(noderef);
      }
      final BoxGeometryProxy geometryProxy = proxy.getGeometry();
      if (geometryProxy != null) {
        final BoxGeometryType geometryElement =
          visitBoxGeometryProxy(geometryProxy);
        element.setBoxGeometry(geometryElement);
      }
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyIdentifiedProxy
      (final IdentifiedProxy proxy,
       final IdentifiedType element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final IdentifierProxy identifierProxy = proxy.getIdentifier();
    final IdentifierType identifierElement =
      (IdentifierType) identifierProxy.acceptVisitor(this);
    element.setIdentifier(identifierElement);
  }

  private void copyIdentifierProxy
      (final IdentifierProxy proxy,
       final IdentifierType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    element.setName(proxy.getName());
  }

  private void copyIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy,
       final IndexedIdentifierType element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
    final List<SimpleExpressionProxy> indexesProxy = proxy.getIndexes();
    final List<ElementType> indexesElement =
      Casting.toList(element.getIndexes());
    copyCollection(indexesProxy, indexesElement);
  }

  private void copyInstanceProxy
      (final InstanceProxy proxy,
       final InstanceType element)
    throws VisitorException
  {
    copyComponentProxy(proxy, element);
    element.setModuleName(proxy.getModuleName());
    final List<ParameterBindingProxy> bindingsProxy = proxy.getBindingList();
    final List<ElementType> bindingsElement =
      Casting.toList(element.getBindings());
    copyCollection(bindingsProxy, bindingsElement);
  }

  private void copyIntConstantProxy
      (final IntConstantProxy proxy,
       final IntConstantType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    element.setValue(proxy.getValue());
  }

  private void copyIntParameterProxy
      (final IntParameterProxy proxy,
       final IntParameterType element)
    throws VisitorException
  {
    copySimpleParameterProxy(proxy, element);
    final SimpleExpressionProxy defaultValueProxy = proxy.getDefaultValue();
    final SimpleExpressionType defaultValueElement =
      (SimpleExpressionType) defaultValueProxy.acceptVisitor(this);
    element.setDefault(defaultValueElement);
  }

  private void copyLabelBlockProxy
      (final LabelBlockProxy proxy,
       final LabelBlockType element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final List<Proxy> eventListProxy = proxy.getEventList();
    final List<ElementType> eventListElement =
      Casting.toList(element.getList());
    copyCollection(eventListProxy, eventListElement);
    final LabelGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final LabelGeometryType geometryElement =
        visitLabelGeometryProxy(geometryProxy);
      element.setLabelGeometry(geometryElement);
    }
  }

  private void copyLabelGeometryProxy
      (final LabelGeometryProxy proxy,
       final LabelGeometryType element)
    throws VisitorException
  {
    try {
      copyGeometryProxy(proxy, element);
      final Point2D pointProxy = proxy.getOffset();
      final PointType pointElement = createPoint(pointProxy);
      element.setPoint(pointElement);
      element.setAnchor(proxy.getAnchor());
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyModuleProxy
      (final ModuleProxy proxy,
       final ModuleType element)
    throws VisitorException
  {
    try {
      copyDocumentProxy(proxy, element);
      final List<ParameterProxy> parameterListProxy = proxy.getParameterList();
      mModuleParameterListHandler.toJAXB(this, parameterListProxy, element);
      final List<AliasProxy> constantAliasListProxy =
        proxy.getConstantAliasList();
      mModuleConstantAliasListHandler.toJAXB
        (this, constantAliasListProxy, element);
      final List<EventDeclProxy> eventDeclListProxy = proxy.getEventDeclList();
      mModuleEventDeclListHandler.toJAXB(this, eventDeclListProxy, element);
      final List<Proxy> eventAliasListProxy = proxy.getEventAliasList();
      mModuleEventAliasListHandler.toJAXB(this, eventAliasListProxy, element);
      final List<Proxy> componentListProxy = proxy.getComponentList();
      mModuleComponentListHandler.toJAXB(this, componentListProxy, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyNodeProxy
      (final NodeProxy proxy,
       final NodeType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final EventListExpressionProxy propositionsProxy = proxy.getPropositions();
    final EventListExpressionType propositionsElement =
      (EventListExpressionType) propositionsProxy.acceptVisitor(this);
    final EventListType propositionsList = propositionsElement.getEventList();
    element.setPropositions(propositionsList);
  }

  private void copyParameterBindingProxy
      (final ParameterBindingProxy proxy,
       final ParameterBindingType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final ExpressionProxy expressionProxy = proxy.getExpression();
    final ExpressionType expressionElement =
      (ExpressionType) expressionProxy.acceptVisitor(this);
    element.setExpression(expressionElement);
  }

  private void copyParameterProxy
      (final ParameterProxy proxy,
       final NamedType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
  }

  private void copyPlainEventListProxy
      (final PlainEventListProxy proxy,
       final EventListExpressionType element)
    throws VisitorException
  {
    try {
      copyExpressionProxy(proxy, element);
      final List<Proxy> eventListProxy = proxy.getEventList();
      mEventListExpressionEventListHandler.toJAXB
        (this, eventListProxy, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyPointGeometryProxy
      (final PointGeometryProxy proxy,
       final PointGeometryType element)
    throws VisitorException
  {
    try {
      copyGeometryProxy(proxy, element);
      final Point2D pointProxy = proxy.getPoint();
      final PointType pointElement = createPoint(pointProxy);
      element.setPoint(pointElement);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyRangeParameterProxy
      (final RangeParameterProxy proxy,
       final RangeParameterType element)
    throws VisitorException
  {
    copySimpleParameterProxy(proxy, element);
    final SimpleExpressionProxy defaultValueProxy = proxy.getDefaultValue();
    final SimpleExpressionType defaultValueElement =
      (SimpleExpressionType) defaultValueProxy.acceptVisitor(this);
    element.setDefault(defaultValueElement);
  }

  private void copySimpleComponentProxy
      (final SimpleComponentProxy proxy,
       final SimpleComponentType element)
    throws VisitorException
  {
    copyComponentProxy(proxy, element);
    element.setKind(proxy.getKind());
    final GraphProxy graphProxy = proxy.getGraph();
    final GraphType graphElement = visitGraphProxy(graphProxy);
    element.setGraph(graphElement);
  }

  private void copySimpleExpressionProxy
      (final SimpleExpressionProxy proxy,
       final SimpleExpressionType element)
    throws VisitorException
  {
    copyExpressionProxy(proxy, element);
  }

  private void copySimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy,
       final SimpleIdentifierType element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
  }

  private void copySimpleNodeProxy
      (final SimpleNodeProxy proxy,
       final SimpleNodeType element)
    throws VisitorException
  {
    copyNodeProxy(proxy, element);
    element.setInitial(proxy.isInitial());
    final PointGeometryProxy pointGeometryProxy = proxy.getPointGeometry();
    if (pointGeometryProxy != null) {
      final PointGeometryType pointGeometryElement =
        visitPointGeometryProxy(pointGeometryProxy);
      element.setPointGeometry(pointGeometryElement);
    }
    final PointGeometryProxy arrowGeometryProxy =
      proxy.getInitialArrowGeometry();
    if (arrowGeometryProxy != null) {
      final PointGeometryType arrowGeometryElement =
        visitInitialArrowGeometryProxy(arrowGeometryProxy);
      element.setInitialArrowGeometry(arrowGeometryElement);
    }
    final LabelGeometryProxy labelGeometryProxy = proxy.getLabelGeometry();
    if (labelGeometryProxy != null) {
      final LabelGeometryType labelGeometryElement =
        visitLabelGeometryProxy(labelGeometryProxy);
      element.setLabelGeometry(labelGeometryElement);
    }
  }

  private void copySimpleParameterProxy
      (final SimpleParameterProxy proxy,
       final SimpleParameterType element)
    throws VisitorException
  {
    copyParameterProxy(proxy, element);
    element.setRequired(proxy.isRequired());
  }

  private void copySplineGeometryProxy
      (final SplineGeometryProxy proxy,
       final SplineGeometryType element)
    throws VisitorException
  {
    try {
      copyGeometryProxy(proxy, element);
      final List<Point2D> pointListProxy = proxy.getPoints();
      final List<PointType> pointListElement =
        Casting.toList(element.getPoints());
      for (final Point2D pointProxy : pointListProxy) {
        final PointType pointElement = createPoint(pointProxy);
        pointListElement.add(pointElement);
      }
      element.setKind(proxy.getKind());
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyUnaryExpressionProxy
      (final UnaryExpressionProxy proxy,
       final UnaryExpressionType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    final UnaryOperator operator = proxy.getOperator();
    element.setOperator(operator.getName());
    final SimpleExpressionProxy subTermProxy = proxy.getSubTerm();
    final SimpleExpressionType subTermElement =
      (SimpleExpressionType) subTermProxy.acceptVisitor(this);
    element.setSubTerm(subTermElement);
  }


  //#########################################################################
  //# Creating Geometry Elements
  private static BoxType createBox(final Rectangle2D rect2d)
    throws JAXBException
  {
    Rectangle rect;
    if (rect2d instanceof Rectangle) {
      rect = (Rectangle) rect2d;
    } else {
      rect = new Rectangle();
      rect.setRect(rect2d);
    }
    return createBox(rect.x, rect.y, rect.width, rect.height);
  }

  private static BoxType createBox(final int x,
                                   final int y,
                                   final int width,
                                   final int height)
    throws JAXBException
  {
    final BoxType box = mFactory.createBox();
    box.setX(x);
    box.setY(y);
    box.setWidth(width);
    box.setHeight(height);
    return box;
  }

  private static PointType createPoint(final Point2D point2d)
    throws JAXBException
  {
    Point point;
    if (point2d instanceof Point) {
      point = (Point) point2d;
    } else {
      point = new Point();
      point.setLocation(point2d);
    }
    return createPoint(point.x, point.y);
  }

  private static PointType createPoint(final int x, final int y)
    throws JAXBException
  {
    final PointType point = mFactory.createPoint();
    point.setX(x);
    point.setY(y);
    return point;
  }

  private static ColorType createColor(final int red,
                                       final int green,
                                       final int blue)
    throws JAXBException
  {
    final ColorType color = mFactory.createColor();
    color.setRed(red);
    color.setGreen(green);
    color.setBlue(blue);
    return color;
  }


  //#########################################################################
  //# Data Members
  private IndexedList<NodeProxy> mGraphNodeList;

  private static final ObjectFactory mFactory = new ObjectFactory();

  private static final EventListExpressionEventListHandler
    mEventListExpressionEventListHandler =
    new EventListExpressionEventListHandler(mFactory);
  private static final ForeachComponentListHandler
    mForeachComponentListHandler =
    new ForeachComponentListHandler(mFactory);
  private static final ForeachEventAliasListHandler
    mForeachEventAliasListHandler =
    new ForeachEventAliasListHandler(mFactory);
  private static final ForeachEventListHandler
    mForeachEventListHandler =
    new ForeachEventListHandler(mFactory);
  private static final GraphEdgeListHandler
    mGraphEdgeListHandler =
    new GraphEdgeListHandler(mFactory);
  private static final GraphNodeListHandler
    mGraphNodeListHandler =
    new GraphNodeListHandler(mFactory);
  private static final ModuleComponentListHandler
    mModuleComponentListHandler =
    new ModuleComponentListHandler(mFactory);
  private static final ModuleConstantAliasListHandler
    mModuleConstantAliasListHandler =
    new ModuleConstantAliasListHandler(mFactory);
  private static final ModuleEventAliasListHandler
    mModuleEventAliasListHandler =
    new ModuleEventAliasListHandler(mFactory);
  private static final ModuleEventDeclListHandler
    mModuleEventDeclListHandler =
    new ModuleEventDeclListHandler(mFactory);
  private static final ModuleParameterListHandler
    mModuleParameterListHandler =
    new ModuleParameterListHandler(mFactory);

}
