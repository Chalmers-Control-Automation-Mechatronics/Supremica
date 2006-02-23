//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleImporter
//###########################################################################
//# $Id: JAXBModuleImporter.java,v 1.7 2006-02-23 01:52:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
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
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.module.AnchorPosition;
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
import net.sourceforge.waters.xsd.module.SplineKind;
import net.sourceforge.waters.xsd.module.UnaryExpressionType;


public class JAXBModuleImporter
  extends JAXBDocumentImporter<ModuleProxy,ModuleType>
{

  //#########################################################################
  //# Constructors
  public JAXBModuleImporter(final ModuleProxyFactory factory,
                            final OperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mHandlerMap = new HashMap<Class,ImportHandler>(128);

    ImportHandler handler;
    handler = new ImportHandler() {
      public BinaryExpressionProxy importElement(final ElementType element)
      {
        final BinaryExpressionType downcast = (BinaryExpressionType) element;
        return importBinaryExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BinaryExpression.class, handler);
    handler = new ImportHandler() {
      public BoxGeometryProxy importElement(final ElementType element)
      {
        final BoxGeometryType downcast = (BoxGeometryType) element;
        return importBoxGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BoxGeometry.class, handler);
    handler = new ImportHandler() {
      public Rectangle importElement(final ElementType element)
      {
        final BoxType downcast = (BoxType) element;
        return importRectangle(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Box.class, handler);
    handler = new ImportHandler() {
      public ColorGeometryProxy importElement(final ElementType element)
      {
        final ColorGeometryType downcast = (ColorGeometryType) element;
        return importColorGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ColorGeometry.class, handler);
    handler = new ImportHandler() {
      public Color importElement(final ElementType element)
      {
        final ColorType downcast = (ColorType) element;
        return importColor(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Color.class, handler);
    handler = new ImportHandler() {
      public AliasProxy importElement(final ElementType element)
      {
        final ConstantAliasType downcast = (ConstantAliasType) element;
        return importConstantAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ConstantAlias.class, handler);
    handler = new ImportHandler() {
      public EdgeProxy importElement(final ElementType element)
      {
        final EdgeType downcast = (EdgeType) element;
        return importEdge(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Edge.class, handler);
    handler = new ImportHandler() {
      public EnumSetExpressionProxy importElement(final ElementType element)
      {
        final EnumSetExpressionType downcast = (EnumSetExpressionType) element;
        return importEnumSetExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EnumSetExpression.class, handler);
    handler = new ImportHandler() {
      public AliasProxy importElement(final ElementType element)
      {
        final EventAliasType downcast = (EventAliasType) element;
        return importEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventAlias.class, handler);
    handler = new ImportHandler() {
      public EventDeclProxy importElement(final ElementType element)
      {
        final EventDeclType downcast = (EventDeclType) element;
        return importEventDecl(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventDecl.class, handler);
    handler = new ImportHandler() {
      public PlainEventListProxy importElement(final ElementType element)
      {
        final EventListExpressionType downcast =
          (EventListExpressionType) element;
        return importPlainEventList(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventListExpression.class, handler);
    handler = new ImportHandler() {
      public EventParameterProxy importElement(final ElementType element)
      {
        final EventParameterType downcast = (EventParameterType) element;
        return importEventParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventParameter.class, handler);
    handler = new ImportHandler() {
      public ForeachComponentProxy importElement(final ElementType element)
      {
        final ForeachComponentType downcast = (ForeachComponentType) element;
        return importForeachComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachComponent.class, handler);
    handler = new ImportHandler() {
      public ForeachEventAliasProxy importElement(final ElementType element)
      {
        final ForeachEventAliasType downcast = (ForeachEventAliasType) element;
        return importForeachEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachEventAlias.class, handler);
    handler = new ImportHandler() {
      public ForeachEventProxy importElement(final ElementType element)
      {
        final ForeachEventType downcast = (ForeachEventType) element;
        return importForeachEvent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachEvent.class, handler);
    handler = new ImportHandler() {
      public GraphProxy importElement(final ElementType element)
      {
        final GraphType downcast = (GraphType) element;
        return importGraph(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Graph.class, handler);
    handler = new ImportHandler() {
      public GroupNodeProxy importElement(final ElementType element)
      {
        final GroupNodeType downcast = (GroupNodeType) element;
        return importGroupNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.GroupNode.class, handler);
    handler = new ImportHandler() {
      public IndexedIdentifierProxy importElement(final ElementType element)
      {
        final IndexedIdentifierType downcast = (IndexedIdentifierType) element;
        return importIndexedIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IndexedIdentifier.class, handler);
    handler = new ImportHandler() {
      public InstanceProxy importElement(final ElementType element)
      {
        final InstanceType downcast = (InstanceType) element;
        return importInstance(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Instance.class, handler);
    handler = new ImportHandler() {
      public IntConstantProxy importElement(final ElementType element)
      {
        final IntConstantType downcast = (IntConstantType) element;
        return importIntConstant(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IntConstant.class, handler);
    handler = new ImportHandler() {
      public IntParameterProxy importElement(final ElementType element)
      {
        final IntParameterType downcast = (IntParameterType) element;
        return importIntParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IntParameter.class, handler);
    handler = new ImportHandler() {
      public LabelBlockProxy importElement(final ElementType element)
      {
        final LabelBlockType downcast = (LabelBlockType) element;
        return importLabelBlock(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelBlock.class, handler);
    handler = new ImportHandler() {
      public LabelGeometryProxy importElement(final ElementType element)
      {
        final LabelGeometryType downcast = (LabelGeometryType) element;
        return importLabelGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelGeometry.class, handler);
    handler = new ImportHandler() {
      public ModuleProxy importElement(final ElementType element)
      {
        final ModuleType downcast = (ModuleType) element;
        return importModule(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Module.class, handler);
    handler = new ImportHandler() {
      public ParameterBindingProxy importElement(final ElementType element)
      {
        final ParameterBindingType downcast = (ParameterBindingType) element;
        return importParameterBinding(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ParameterBinding.class, handler);
    handler = new ImportHandler() {
      public PointGeometryProxy importElement(final ElementType element)
      {
        final PointGeometryType downcast = (PointGeometryType) element;
        return importPointGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.PointGeometry.class, handler);
    handler = new ImportHandler() {
      public Point importElement(final ElementType element)
      {
        final PointType downcast = (PointType) element;
        return importPoint(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Point.class, handler);
    handler = new ImportHandler() {
      public SimpleComponentProxy importElement(final ElementType element)
      {
        final SimpleComponentType downcast = (SimpleComponentType) element;
        return importSimpleComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleComponent.class, handler);
    handler = new ImportHandler() {
      public RangeParameterProxy importElement(final ElementType element)
      {
        final RangeParameterType downcast = (RangeParameterType) element;
        return importRangeParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.RangeParameter.class, handler);
    handler = new ImportHandler() {
      public SimpleIdentifierProxy importElement(final ElementType element)
      {
        final SimpleIdentifierType downcast = (SimpleIdentifierType) element;
        return importSimpleIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleIdentifier.class, handler);
    handler = new ImportHandler() {
      public SimpleNodeProxy importElement(final ElementType element)
      {
        final SimpleNodeType downcast = (SimpleNodeType) element;
        return importSimpleNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleNode.class, handler);
    handler = new ImportHandler() {
      public SplineGeometryProxy importElement(final ElementType element)
      {
        final SplineGeometryType downcast = (SplineGeometryType) element;
        return importSplineGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SplineGeometry.class, handler);
    handler = new ImportHandler() {
      public UnaryExpressionProxy importElement(final ElementType element)
      {
        final UnaryExpressionType downcast = (UnaryExpressionType) element;
        return importUnaryExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.UnaryExpression.class, handler);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBImporter
  Object importElement(final ElementType element)
  {
    final Class clazz = element.getClass();
    ImportHandler handler = mHandlerMap.get(clazz);
    if (handler == null) {
      final Class[] ifaces = clazz.getInterfaces();
      for (int i = 0; i < ifaces.length; i++) {
        handler = mHandlerMap.get(ifaces[i]);
        if (handler != null) {
          mHandlerMap.put(clazz, handler);
          break;
        }
      }
    }
    if (handler == null) {
      throw new ClassCastException
        ("JAXBModuleImporter cannot handle element of type " +
         element.getClass().getName() + "!");
    }
    return handler.importElement(element);
  }

  public ModuleProxy importDocument(final ModuleType element,
                                    final URI uri)
  {
    return importModule(element, uri);
  }


  //#########################################################################
  //# Importing Elements
  private BinaryExpressionProxy importBinaryExpression
    (final BinaryExpressionType element)
  {
    final String operatorName = element.getOperator();
    final BinaryOperator operator =
      mOperatorTable.getBinaryOperator(operatorName);
    final SimpleExpressionType leftElement = element.getLeft();
    final SimpleExpressionProxy left =
      (SimpleExpressionProxy) importElement(leftElement);
    final SimpleExpressionType rightElement = element.getRight();
    final SimpleExpressionProxy right =
      (SimpleExpressionProxy) importElement(rightElement);
    return mFactory.createBinaryExpressionProxy(operator, left, right);
  }

  private BoxGeometryProxy importBoxGeometry(final BoxGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final BoxType boxElement = element.getBox();
      final Rectangle rectangle = importRectangle(boxElement);
      return mFactory.createBoxGeometryProxy(rectangle);
    }
  }

  private Color importColor(final ColorType element)
  {
    final int red = element.getRed();
    final int green = element.getGreen();
    final int blue = element.getBlue();
    return new Color(red, green, blue);
  }

  private ColorGeometryProxy importColorGeometry
    (final ColorGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final ColorType colorElement = element.getColor();
      final Color color = importColor(colorElement);
      final Set<Color> colorSet = Collections.singleton(color);
      return mFactory.createColorGeometryProxy(colorSet);
    }
  }

  private AliasProxy importConstantAlias(final ConstantAliasType element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final ExpressionType expressionElement = element.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    return mFactory.createAliasProxy(identifier, expression);
  }

  private EdgeProxy importEdge(final EdgeType element)
  {
    final String sourceName = element.getSource();
    final NodeProxy source = mGraphNodeList.find(sourceName);
    final String targetName = element.getTarget();
    final NodeProxy target = mGraphNodeList.find(targetName);
    final LabelBlockType labelBlockElement = element.getLabelBlock();
    final LabelBlockProxy labelBlock = importLabelBlock(labelBlockElement);
    final SplineGeometryType geometryElement = element.getSplineGeometry();
    final SplineGeometryProxy geometry = importSplineGeometry(geometryElement);
    final PointGeometryType startPointElement =
      element.getStartPointGeometry();
    final PointGeometryProxy startPoint =
      importPointGeometry(startPointElement);
    final PointGeometryType endPointElement = element.getEndPointGeometry();
    final PointGeometryProxy endPoint =
      importPointGeometry(endPointElement);
    return mFactory.createEdgeProxy(source,
                                    target,
                                    labelBlock,
                                    geometry,
                                    startPoint,
                                    endPoint);
  }

  private EnumSetExpressionProxy importEnumSetExpression
    (final EnumSetExpressionType element)
  {
    final List<SimpleIdentifierProxy> items =
      new LinkedList<SimpleIdentifierProxy>();
    final List<SimpleIdentifierType> itemsElement =
      Casting.toList(element.getItems());
    for (final SimpleIdentifierType itemElement : itemsElement) {
      final SimpleIdentifierProxy itemProxy =
        importSimpleIdentifier(itemElement);
      items.add(itemProxy);
    }
    return mFactory.createEnumSetExpressionProxy(items);
  }

  private AliasProxy importEventAlias(final EventAliasType element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final EventListExpressionType eventListElement = element.getExpression();
    final EventListExpressionProxy eventList =
      importPlainEventList(eventListElement);
    return mFactory.createAliasProxy(identifier, eventList);
  }

  private EventDeclProxy importEventDecl(final EventBaseType element)
  {
    final String name = element.getName();
    final EventKind kind = element.getKind();
    final boolean observable = element.isObservable();
    final List<SimpleExpressionProxy> ranges =
      new LinkedList<SimpleExpressionProxy>();
    final List<SimpleExpressionType> rangesElement =
      Casting.toList(element.getRanges());
    for (final SimpleExpressionType itemElement : rangesElement) {
      final SimpleExpressionProxy itemProxy =
        (SimpleExpressionProxy) importElement(itemElement);
      ranges.add(itemProxy);
    }
    final ColorGeometryType colorGeometryElement = element.getColorGeometry();
    final ColorGeometryProxy colorGeometry =
      importColorGeometry(colorGeometryElement);
    return mFactory.createEventDeclProxy(name,
                                         kind,
                                         observable,
                                         ranges,
                                         colorGeometry);
  }

  private EventParameterProxy importEventParameter
    (final EventParameterType element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final EventDeclProxy eventDecl = importEventDecl(element);
    return mFactory.createEventParameterProxy(name, required, eventDecl);
  }

  private ForeachComponentProxy importForeachComponent
    (final ForeachComponentType element)
  {
    final String name = element.getName();
    final SimpleExpressionType rangeElement = element.getRange();
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    final SimpleExpressionType guardElement = element.getGuard();
    final SimpleExpressionProxy guard =
      guardElement == null ? null :
      (SimpleExpressionProxy) importElement(guardElement);
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachComponentListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachComponentProxy(name, range, guard, body);
  }

  private ForeachEventAliasProxy importForeachEventAlias
    (final ForeachEventAliasType element)
  {
    final String name = element.getName();
    final SimpleExpressionType rangeElement = element.getRange();
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    final SimpleExpressionType guardElement = element.getGuard();
    final SimpleExpressionProxy guard =
      guardElement == null ? null :
      (SimpleExpressionProxy) importElement(guardElement);
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachEventAliasListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachEventAliasProxy(name, range, guard, body);
  }

  private ForeachEventProxy importForeachEvent(final ForeachEventType element)
  {
    final String name = element.getName();
    final SimpleExpressionType rangeElement = element.getRange();
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    final SimpleExpressionType guardElement = element.getGuard();
    final SimpleExpressionProxy guard =
      guardElement == null ? null :
      (SimpleExpressionProxy) importElement(guardElement);
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachEventListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachEventProxy(name, range, guard, body);
  }

  private GraphProxy importGraph(final GraphType element)
  {
    try {
      final boolean deterministic = element.isDeterministic();
      final LabelBlockType blockedEventsElement = element.getBlockedEvents();
      final LabelBlockProxy blockedEvents =
        importLabelBlock(blockedEventsElement);
      mGraphNodeList =
        new CheckedImportList<NodeProxy>(GraphProxy.class, "node");
      mGraphNodeListHandler.fromJAXBChecked(this, element, mGraphNodeList);
      final List<EdgeProxy> edges = new LinkedList<EdgeProxy>();
      mGraphEdgeListHandler.fromJAXB(this, element, edges);
      return mFactory.createGraphProxy(deterministic,
                                       blockedEvents,
                                       mGraphNodeList,
                                       edges);
    } finally {
      mGraphNodeList = null;
    }
  }

  private GroupNodeProxy importGroupNode(final GroupNodeType element)
  {
    final String name = element.getName();
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mNodeEventListHandler.fromJAXB(this, element, eventList);
    final EventListExpressionProxy propositions =
      mFactory.createPlainEventListProxy(eventList);
    final Collection<NodeProxy> immediateChildNodes =
      new CheckedImportSet<NodeProxy>(GroupNodeProxy.class, name, "node");
    final List<NodeRefType> immediateChildNodesElement =
      Casting.toList(element.getNodes());
    for (final NodeRefType ref : immediateChildNodesElement) {
      final NodeProxy itemProxy = importNodeRef(ref);
      immediateChildNodes.add(itemProxy);
    }
    final BoxGeometryType geometryElement = element.getBoxGeometry();
    final BoxGeometryProxy geometry =
      importBoxGeometry(geometryElement);
    return mFactory.createGroupNodeProxy(name,
                                         propositions,
                                         immediateChildNodes,
                                         geometry);
  }

  private IndexedIdentifierProxy importIndexedIdentifier
    (final IndexedIdentifierType element)
  {
    final String name = element.getName();
    final List<SimpleExpressionProxy> indexes =
      new LinkedList<SimpleExpressionProxy>();
    final List<SimpleExpressionType> indexesElement =
      Casting.toList(element.getIndexes());
    for (final SimpleExpressionType itemElement : indexesElement) {
      final SimpleExpressionProxy itemProxy =
        (SimpleExpressionProxy) importElement(itemElement);
      indexes.add(itemProxy);
    }
    return mFactory.createIndexedIdentifierProxy(name, indexes);
  }

  private InstanceProxy importInstance(final InstanceType element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final String moduleName = element.getModuleName();
    final List<ParameterBindingProxy> bindingList =
      new LinkedList<ParameterBindingProxy>();
    final List<ParameterBindingType> bindingListElement =
      Casting.toList(element.getBindings());
    for (final ParameterBindingType itemElement : bindingListElement) {
      final ParameterBindingProxy itemProxy =
        importParameterBinding(itemElement);
      bindingList.add(itemProxy);
    }
    return mFactory.createInstanceProxy(identifier, moduleName, bindingList);
  }

  private IntConstantProxy importIntConstant(final IntConstantType element)
  {
    final int value = element.getValue();
    return mFactory.createIntConstantProxy(value);
  }

  private IntParameterProxy importIntParameter(final IntParameterType element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final SimpleExpressionType defaultValueElement = element.getDefault();
    final SimpleExpressionProxy defaultValue =
      (SimpleExpressionProxy) importElement(defaultValueElement);
    return mFactory.createIntParameterProxy(name, required, defaultValue);
  }

  private LabelBlockProxy importLabelBlock(final LabelBlockType element)
  {
    if (element != null) {
      final List<Proxy> eventList = new LinkedList<Proxy>();
      final List<ElementType> eventListElement =
        Casting.toList(element.getList());
      for (final ElementType itemElement : eventListElement) {
        final Proxy itemProxy = (Proxy) importElement(itemElement);
        eventList.add(itemProxy);
      }
      final LabelGeometryType geometryElement = element.getLabelGeometry();
      final LabelGeometryProxy geometry = importLabelGeometry(geometryElement);
      return mFactory.createLabelBlockProxy(eventList, geometry);
    } else {
      return mFactory.createLabelBlockProxy(null, null);
    }
  }

  private LabelGeometryProxy importLabelGeometry
    (final LabelGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final PointType offsetElement = element.getPoint();
      final Point offset = importPoint(offsetElement);
      final AnchorPosition anchor = element.getAnchor();
      return mFactory.createLabelGeometryProxy(offset, anchor);
    }
  }

  private ModuleProxy importModule(final ModuleType element)
  {
    return importModule(element, null);
  }

  private ModuleProxy importModule(final ModuleType element,
                                   final URI uri)
  {
    final String name = element.getName();
    final List<ParameterProxy> parameterList =
      new LinkedList<ParameterProxy>();
    mModuleParameterListHandler.fromJAXB(this, element, parameterList);
    final List<AliasProxy> constantAliasList = new LinkedList<AliasProxy>();
    mModuleConstantAliasListHandler.fromJAXB(this, element, constantAliasList);
    final List<EventDeclProxy> eventDeclList =
      new LinkedList<EventDeclProxy>();
    mModuleEventDeclListHandler.fromJAXB(this, element, eventDeclList);
    final List<Proxy> eventAliasList = new LinkedList<Proxy>();
    mModuleEventAliasListHandler.fromJAXB(this, element, eventAliasList);
    final List<Proxy> componentList = new LinkedList<Proxy>();
    mModuleComponentListHandler.fromJAXB(this, element, componentList);
    return mFactory.createModuleProxy(name,
                                      uri,
                                      parameterList,
                                      constantAliasList,
                                      eventDeclList,
                                      eventAliasList,
                                      componentList);
  }

  private NodeProxy importNodeRef(final NodeRefType element)
    throws NameNotFoundException
  {
    final String name = element.getName();
    return mGraphNodeList.find(name);
  }

  private ParameterBindingProxy importParameterBinding
    (final ParameterBindingType element)
  {
    final String name = element.getName();
    final ExpressionType expressionElement = element.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    return mFactory.createParameterBindingProxy(name, expression);
  }

  private PlainEventListProxy importPlainEventList
    (final EventListExpressionType element)
  {
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mEventListExpressionEventListHandler.fromJAXB(this, element, eventList);
    return mFactory.createPlainEventListProxy(eventList);
  }

  private Point importPoint(final PointType element)
  {
    final int x = element.getX();
    final int y = element.getY();
    return new Point(x, y);
  }

  private PointGeometryProxy importPointGeometry
    (final PointGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final PointType pointElement = element.getPoint();
      final Point point = importPoint(pointElement);
      return mFactory.createPointGeometryProxy(point);
    }
  }

  private RangeParameterProxy importRangeParameter
    (final RangeParameterType element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final SimpleExpressionType defaultValueElement = element.getDefault();
    final SimpleExpressionProxy defaultValue =
      (SimpleExpressionProxy) importElement(defaultValueElement);
    return mFactory.createRangeParameterProxy(name, required, defaultValue);
  }

  private Rectangle importRectangle(final BoxType element)
  {
    final int x = element.getX();
    final int y = element.getY();
    final int width = element.getWidth();
    final int height = element.getHeight();
    return new Rectangle(x, y, width, height);
  }

  private SimpleComponentProxy importSimpleComponent
    (final SimpleComponentType element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final ComponentKind kind = element.getKind();
    final GraphType graphElement = element.getGraph();
    final GraphProxy graph = importGraph(graphElement);
    return mFactory.createSimpleComponentProxy(identifier, kind, graph);
  }

  private SimpleIdentifierProxy importSimpleIdentifier
    (final SimpleIdentifierType element)
  {
    final String name = element.getName();
    return mFactory.createSimpleIdentifierProxy(name);
  }

  private SimpleNodeProxy importSimpleNode(final SimpleNodeType element)
  {
    final String name = element.getName();
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mNodeEventListHandler.fromJAXB(this, element, eventList);
    final EventListExpressionProxy propositions =
      mFactory.createPlainEventListProxy(eventList);
    final boolean initial = element.isInitial();
    final PointGeometryType pointGeometryElement = element.getPointGeometry();
    final PointGeometryProxy pointGeometry =
      importPointGeometry(pointGeometryElement);
    final PointGeometryType arrowGeometryElement =
      element.getInitialArrowGeometry();
    final PointGeometryProxy arrowGeometry =
      importPointGeometry(arrowGeometryElement);
    final LabelGeometryType labelGeometryElement = element.getLabelGeometry();
    final LabelGeometryProxy labelGeometry =
      importLabelGeometry(labelGeometryElement);
    return mFactory.createSimpleNodeProxy(name,
                                          propositions,
                                          initial,
                                          pointGeometry,
                                          arrowGeometry,
                                          labelGeometry);
  }

  private SplineGeometryProxy importSplineGeometry
    (final SplineGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final List<Point> points = new LinkedList<Point>();
      final List<PointType> pointsElement =
        Casting.toList(element.getPoints());
      for (final PointType pointElement : pointsElement) {
        final Point point = importPoint(pointElement);
        points.add(point);
      }
      final SplineKind kind = element.getKind();
      return mFactory.createSplineGeometryProxy(points, kind);
    }
  }

  private UnaryExpressionProxy importUnaryExpression
    (final UnaryExpressionType element)
  {
    final String operatorName = element.getOperator();
    final UnaryOperator operator =
      mOperatorTable.getUnaryOperator(operatorName);
    final SimpleExpressionType subTermElement = element.getSubTerm();
    final SimpleExpressionProxy subTerm =
      (SimpleExpressionProxy) importElement(subTermElement);
    return mFactory.createUnaryExpressionProxy(operator, subTerm);
  }


  //#########################################################################
  //# Inner Class ImportHandler
  private interface ImportHandler
  {
    public Object importElement(ElementType element);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final OperatorTable mOperatorTable;
  private final Map<Class,ImportHandler> mHandlerMap;
  private IndexedList<NodeProxy> mGraphNodeList;

  private static final EventListExpressionEventListHandler
    mEventListExpressionEventListHandler =
    new EventListExpressionEventListHandler();
  private static final ForeachComponentListHandler
    mForeachComponentListHandler =
    new ForeachComponentListHandler();
  private static final ForeachEventAliasListHandler
    mForeachEventAliasListHandler =
    new ForeachEventAliasListHandler();
  private static final ForeachEventListHandler
    mForeachEventListHandler =
    new ForeachEventListHandler();
  private static final GraphEdgeListHandler
    mGraphEdgeListHandler =
    new GraphEdgeListHandler();
  private static final GraphNodeListHandler
    mGraphNodeListHandler =
    new GraphNodeListHandler();
  private static final ModuleComponentListHandler
    mModuleComponentListHandler =
    new ModuleComponentListHandler();
  private static final ModuleConstantAliasListHandler
    mModuleConstantAliasListHandler =
    new ModuleConstantAliasListHandler();
  private static final ModuleEventAliasListHandler
    mModuleEventAliasListHandler =
    new ModuleEventAliasListHandler();
  private static final ModuleEventDeclListHandler
    mModuleEventDeclListHandler =
    new ModuleEventDeclListHandler();
  private static final ModuleParameterListHandler
    mModuleParameterListHandler =
    new ModuleParameterListHandler();
  private static final NodeEventListHandler
    mNodeEventListHandler =
    new NodeEventListHandler();

}
