//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

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
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.Actions;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.BinaryExpression;
import net.sourceforge.waters.xsd.module.Box;
import net.sourceforge.waters.xsd.module.BoxGeometry;
import net.sourceforge.waters.xsd.module.Color;
import net.sourceforge.waters.xsd.module.ColorGeometry;
import net.sourceforge.waters.xsd.module.ConstantAlias;
import net.sourceforge.waters.xsd.module.ConstantAliasExpression;
import net.sourceforge.waters.xsd.module.Edge;
import net.sourceforge.waters.xsd.module.EnumSetExpression;
import net.sourceforge.waters.xsd.module.EventAlias;
import net.sourceforge.waters.xsd.module.EventDecl;
import net.sourceforge.waters.xsd.module.EventListExpression;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.ForeachComponent;
import net.sourceforge.waters.xsd.module.ForeachEvent;
import net.sourceforge.waters.xsd.module.ForeachEventAlias;
import net.sourceforge.waters.xsd.module.Graph;
import net.sourceforge.waters.xsd.module.GroupNode;
import net.sourceforge.waters.xsd.module.GuardActionBlock;
import net.sourceforge.waters.xsd.module.Guards;
import net.sourceforge.waters.xsd.module.IdentifiedType;
import net.sourceforge.waters.xsd.module.IdentifierType;
import net.sourceforge.waters.xsd.module.IndexedIdentifier;
import net.sourceforge.waters.xsd.module.Instance;
import net.sourceforge.waters.xsd.module.IntConstant;
import net.sourceforge.waters.xsd.module.LabelBlock;
import net.sourceforge.waters.xsd.module.LabelGeometry;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.NodeRef;
import net.sourceforge.waters.xsd.module.ParameterBinding;
import net.sourceforge.waters.xsd.module.Point;
import net.sourceforge.waters.xsd.module.PointGeometryType;
import net.sourceforge.waters.xsd.module.QualifiedIdentifier;
import net.sourceforge.waters.xsd.module.RangeList;
import net.sourceforge.waters.xsd.module.ScopeKind;
import net.sourceforge.waters.xsd.module.SimpleComponent;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifier;
import net.sourceforge.waters.xsd.module.SimpleNode;
import net.sourceforge.waters.xsd.module.SplineGeometry;
import net.sourceforge.waters.xsd.module.SplineKind;
import net.sourceforge.waters.xsd.module.UnaryExpression;
import net.sourceforge.waters.xsd.module.VariableComponent;
import net.sourceforge.waters.xsd.module.VariableInitial;
import net.sourceforge.waters.xsd.module.VariableMarking;
import net.sourceforge.waters.xsd.module.VariableRange;


public class JAXBModuleImporter
  extends JAXBDocumentImporter<ModuleProxy,Module>
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
        throws WatersUnmarshalException
      {
        final BinaryExpression downcast = (BinaryExpression) element;
        return importBinaryExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BinaryExpression.class, handler);
    handler = new ImportHandler() {
      public BoxGeometryProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final BoxGeometry downcast = (BoxGeometry) element;
        return importBoxGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BoxGeometry.class, handler);
    handler = new ImportHandler() {
      public Rectangle importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Box downcast = (Box) element;
        return importRectangle(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Box.class, handler);
    handler = new ImportHandler() {
      public ColorGeometryProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ColorGeometry downcast = (ColorGeometry) element;
        return importColorGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ColorGeometry.class, handler);
    handler = new ImportHandler() {
      public java.awt.Color importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Color downcast = (Color) element;
        return importColor(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Color.class, handler);
    handler = new ImportHandler() {
      public ConstantAliasProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ConstantAlias downcast = (ConstantAlias) element;
        return importConstantAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ConstantAlias.class, handler);
    handler = new ImportHandler() {
      public EdgeProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Edge downcast = (Edge) element;
        return importEdge(downcast);
      }
    };
    mHandlerMap.put(net.sourceforge.waters.xsd.module.Edge.class, handler);
    handler = new ImportHandler() {
      public EnumSetExpressionProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final EnumSetExpression downcast = (EnumSetExpression) element;
        return importEnumSetExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EnumSetExpression.class, handler);
    handler = new ImportHandler() {
      public EventAliasProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final EventAlias downcast = (EventAlias) element;
        return importEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventAlias.class, handler);
    handler = new ImportHandler() {
      public EventDeclProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final EventDecl downcast = (EventDecl) element;
        return importEventDecl(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventDecl.class, handler);
    handler = new ImportHandler() {
      public PlainEventListProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final EventListExpression downcast =
          (EventListExpression) element;
        return importPlainEventList(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventListExpression.class, handler);
    handler = new ImportHandler() {
      public ForeachComponentProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ForeachComponent downcast = (ForeachComponent) element;
        return importForeachComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachComponent.class, handler);
    handler = new ImportHandler() {
      public ForeachEventAliasProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ForeachEventAlias downcast = (ForeachEventAlias) element;
        return importForeachEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachEventAlias.class, handler);
    handler = new ImportHandler() {
      public ForeachEventProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ForeachEvent downcast = (ForeachEvent) element;
        return importForeachEvent(downcast);
      }
    };
    mHandlerMap.put(net.sourceforge.waters.xsd.module.ForeachEvent.class,
                    handler);
    handler = new ImportHandler() {
        public GraphProxy importElement(final ElementType element)
          throws WatersUnmarshalException
        {
          final Graph downcast = (Graph) element;
          return importGraph(downcast);
        }
      };
    mHandlerMap.put(net.sourceforge.waters.xsd.module.Graph.class, handler);
    handler = new ImportHandler() {
      public GroupNodeProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final GroupNode downcast = (GroupNode) element;
        return importGroupNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.GroupNode.class, handler);
    handler = new ImportHandler() {
      public IndexedIdentifierProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final IndexedIdentifier downcast = (IndexedIdentifier) element;
        return importIndexedIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IndexedIdentifier.class, handler);
    handler = new ImportHandler() {
      public GuardActionBlockProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final GuardActionBlock downcast = (GuardActionBlock) element;
        return importGuardActionBlock(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.GuardActionBlock.class, handler);
    handler = new ImportHandler() {
      public InstanceProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Instance downcast = (Instance) element;
        return importInstance(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Instance.class, handler);
    handler = new ImportHandler() {
      public IntConstantProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final IntConstant downcast = (IntConstant) element;
        return importIntConstant(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IntConstant.class, handler);
    handler = new ImportHandler() {
      public LabelBlockProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final LabelBlock downcast = (LabelBlock) element;
        return importLabelBlock(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelBlock.class, handler);
    
    handler = new ImportHandler() {
      public LabelGeometryProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final LabelGeometry downcast = (LabelGeometry) element;
        return importLabelGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelGeometry.class, handler);
    handler = new ImportHandler() {
      public ModuleProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Module downcast = (Module) element;
        return importModule(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Module.class, handler);
    handler = new ImportHandler() {
      public ParameterBindingProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final ParameterBinding downcast = (ParameterBinding) element;
        return importParameterBinding(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ParameterBinding.class, handler);
    handler = new ImportHandler() {
      public PointGeometryProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final PointGeometryType downcast = (PointGeometryType) element;
        return importPointGeometryType(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.PointGeometryType.class, handler);
    handler = new ImportHandler() {
      public java.awt.Point importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final Point downcast = (Point) element;
        return importPoint(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Point.class, handler);
    handler = new ImportHandler() {
      public SimpleComponentProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final SimpleComponent downcast = (SimpleComponent) element;
        return importSimpleComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleComponent.class, handler);
    handler = new ImportHandler() {
      public QualifiedIdentifierProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final QualifiedIdentifier downcast = (QualifiedIdentifier) element;
        return importQualifiedIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.QualifiedIdentifier.class, handler);
    handler = new ImportHandler() {
      public SimpleIdentifierProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final SimpleIdentifier downcast = (SimpleIdentifier) element;
        return importSimpleIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleIdentifier.class, handler);
    handler = new ImportHandler() {
      public SimpleNodeProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final SimpleNode downcast = (SimpleNode) element;
        return importSimpleNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleNode.class, handler);
    handler = new ImportHandler() {
      public SplineGeometryProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final SplineGeometry downcast = (SplineGeometry) element;
        return importSplineGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SplineGeometry.class, handler);
    handler = new ImportHandler() {
      public UnaryExpressionProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final UnaryExpression downcast = (UnaryExpression) element;
        return importUnaryExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.UnaryExpression.class, handler);
    handler = new ImportHandler() {
      public VariableComponentProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final VariableComponent downcast = (VariableComponent) element;
        return importVariableComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.VariableComponent.class, handler);
    handler = new ImportHandler() {
      public VariableMarkingProxy importElement(final ElementType element)
        throws WatersUnmarshalException
      {
        final VariableMarking downcast = (VariableMarking) element;
        return importVariableMarking(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.VariableMarking.class, handler);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBImporter
  Object importElement(final ElementType element)
    throws WatersUnmarshalException
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

  public ModuleProxy importDocument(final Module element,
                                    final URI uri)
    throws WatersUnmarshalException
  {
    return importModule(element, uri);
  }


  //#########################################################################
  //# Importing Elements
  private BinaryExpressionProxy importBinaryExpression
    (final BinaryExpression element)
    throws WatersUnmarshalException
  {
    final String text = element.getText();
    final String operatorName = element.getOperator();
    final BinaryOperator operator =
      mOperatorTable.getBinaryOperator(operatorName);
    final List<SimpleExpressionType> subterms = element.getSubterms();
    final SimpleExpressionType leftElement = subterms.get(0);
    final SimpleExpressionProxy left =
      (SimpleExpressionProxy) importElement(leftElement);
    final SimpleExpressionType rightElement = subterms.get(1);
    final SimpleExpressionProxy right =
      (SimpleExpressionProxy) importElement(rightElement);
    return mFactory.createBinaryExpressionProxy(text, operator, left, right);
  }

  private BoxGeometryProxy importBoxGeometry(final BoxGeometry element)
  {
    if (element == null) {
      return null;
    } else {
      final Box boxElement = element.getBox();
      final Rectangle rectangle = importRectangle(boxElement);
      return mFactory.createBoxGeometryProxy(rectangle);
    }
  }

  private java.awt.Color importColor(final Color element)
  {
    final int red = element.getRed();
    final int green = element.getGreen();
    final int blue = element.getBlue();
    return new java.awt.Color(red, green, blue);
  }

  private ColorGeometryProxy importColorGeometry
    (final ColorGeometry element)
  {
    if (element == null) {
      return null;
    } else {
      final Color colorElement = element.getColor();
      final java.awt.Color color = importColor(colorElement);
      final Set<java.awt.Color> colorSet = Collections.singleton(color);
      return mFactory.createColorGeometryProxy(colorSet);
    }
  }

  private ConstantAliasProxy importConstantAlias(final ConstantAlias element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final ConstantAliasExpression wrapper =
      element.getConstantAliasExpression();
    final ExpressionType expressionElement = wrapper.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    final ScopeKind scope = element.getScope();
    return mFactory.createConstantAliasProxy(identifier, expression, scope);
  }
  
  
//EFA----------------------
  private GuardActionBlockProxy importGuardActionBlock
    (final GuardActionBlock element)
    throws WatersUnmarshalException
  {
    if (element == null) {
      return null;
    } else {
      final Guards guards = element.getGuards();
      final List<SimpleExpressionProxy> guardList =
        new LinkedList<SimpleExpressionProxy>();
      if (guards != null) {
        final List<SimpleExpressionType> guardListElement =
          Casting.toList(guards.getList());
        for (final SimpleExpressionType exprElement : guardListElement) {
          final SimpleExpressionProxy exprProxy =
            (SimpleExpressionProxy) importElement(exprElement);
          guardList.add(exprProxy);
        }
      }
      final Actions actions = element.getActions();
      final List<BinaryExpressionProxy> actionList =
        new LinkedList<BinaryExpressionProxy>();
      if (actions != null) {
        final List<BinaryExpression> actionListElement =
          Casting.toList(actions.getList());
        for (final BinaryExpression exprElement : actionListElement) {
          final BinaryExpressionProxy exprProxy =
            (BinaryExpressionProxy) importElement(exprElement);
          actionList.add(exprProxy);
        }
      }
      final LabelGeometry geometryElement = element.getLabelGeometry();
      final LabelGeometryProxy geometry = importLabelGeometry(geometryElement);
      return mFactory.createGuardActionBlockProxy
          (guardList, actionList, geometry);
    }
  }
  // ------------------------

  private EdgeProxy importEdge(final Edge element)
    throws WatersUnmarshalException
  {
    final String sourceName = element.getSource();
    final NodeProxy source = mGraphNodeList.find(sourceName);
    final String targetName = element.getTarget();
    final NodeProxy target = mGraphNodeList.find(targetName);

    final LabelBlock labelBlockElement = element.getLabelBlock();
    final LabelBlockProxy labelBlock = importLabelBlock(labelBlockElement);
    
    final GuardActionBlock guardActionBlockElement =
      element.getGuardActionBlock();
    final GuardActionBlockProxy guardActionBlock =
      importGuardActionBlock(guardActionBlockElement);

    final SplineGeometry geometryElement = element.getSplineGeometry();
    final SplineGeometryProxy geometry = importSplineGeometry(geometryElement);
    final PointGeometryType startPointElement =
      element.getStartPointGeometry();
    final PointGeometryProxy startPoint =
      importPointGeometryType(startPointElement);
    final PointGeometryType endPointElement = element.getEndPointGeometry();
    final PointGeometryProxy endPoint =
      importPointGeometryType(endPointElement);
    return mFactory.createEdgeProxy(source,
                                    target,
                                    labelBlock,
                                    guardActionBlock, //EFA---------
                                    geometry,
                                    startPoint,
                                    endPoint);
  }

  private EnumSetExpressionProxy importEnumSetExpression
    (final EnumSetExpression element)
    throws WatersUnmarshalException
  {
    final String text = element.getText();
    final List<SimpleIdentifierProxy> items =
      new LinkedList<SimpleIdentifierProxy>();
    final List<SimpleIdentifier> itemsElement =
      Casting.toList(element.getItems());
    for (final SimpleIdentifier itemElement : itemsElement) {
      final SimpleIdentifierProxy itemProxy =
        importSimpleIdentifier(itemElement);
      items.add(itemProxy);
    }
    return mFactory.createEnumSetExpressionProxy(text, items);
  }

  private EventAliasProxy importEventAlias(final EventAlias element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final EventListExpression eventListElement = element.getExpression();
    final EventListExpressionProxy eventList =
      importPlainEventList(eventListElement);
    return mFactory.createEventAliasProxy(identifier, eventList);
  }

  private EventDeclProxy importEventDecl(final EventDecl element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final EventKind kind = element.getKind();
    final boolean observable = element.isObservable();
    final ScopeKind scope = element.getScope();
    final List<SimpleExpressionProxy> ranges =
      new LinkedList<SimpleExpressionProxy>();
    final RangeList rangeList = element.getRangeList();
    if (rangeList != null) {
      final List<SimpleExpressionType> rangesElement =
        Casting.toList(rangeList.getRanges());
      for (final SimpleExpressionType itemElement : rangesElement) {
        final SimpleExpressionProxy itemProxy =
          (SimpleExpressionProxy) importElement(itemElement);
        ranges.add(itemProxy);
      }
    }
    final ColorGeometry colorGeometryElement = element.getColorGeometry();
    final ColorGeometryProxy colorGeometry =
      importColorGeometry(colorGeometryElement);
    return mFactory.createEventDeclProxy(identifier,
                                         kind,
                                         observable,
                                         scope,
                                         ranges,
                                         colorGeometry);
  }

  private ForeachComponentProxy importForeachComponent
    (final ForeachComponent element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final List<SimpleExpressionType> list = element.getRangeAndGuard();
    final SimpleExpressionType rangeElement = list.get(0);
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    SimpleExpressionProxy guard = null;
    if (list.size() > 1) {
      final SimpleExpressionType guardElement = list.get(1);
      guard = (SimpleExpressionProxy) importElement(guardElement);
    }
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachComponentListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachComponentProxy(name, range, guard, body);
  }

  private ForeachEventAliasProxy importForeachEventAlias
    (final ForeachEventAlias element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final List<SimpleExpressionType> list = element.getRangeAndGuard();
    final SimpleExpressionType rangeElement = list.get(0);
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    SimpleExpressionProxy guard = null;
    if (list.size() > 1) {
      final SimpleExpressionType guardElement = list.get(1);
      guard = (SimpleExpressionProxy) importElement(guardElement);
    }
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachEventAliasListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachEventAliasProxy(name, range, guard, body);
  }

  private ForeachEventProxy importForeachEvent(final ForeachEvent element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final List<SimpleExpressionType> list = element.getRangeAndGuard();
    final SimpleExpressionType rangeElement = list.get(0);
    final SimpleExpressionProxy range =
      (SimpleExpressionProxy) importElement(rangeElement);
    SimpleExpressionProxy guard = null;
    if (list.size() > 1) {
      final SimpleExpressionType guardElement = list.get(1);
      guard = (SimpleExpressionProxy) importElement(guardElement);
    }
    final List<Proxy> body = new LinkedList<Proxy>();
    mForeachEventListHandler.fromJAXB(this, element, body);
    return mFactory.createForeachEventProxy(name, range, guard, body);
  }


  private GraphProxy importGraph(final Graph element)
    throws WatersUnmarshalException
  {
    try {
      final boolean deterministic = element.isDeterministic();
      final LabelBlock blockedEventsElement = element.getBlockedEvents();
      final LabelBlockProxy blockedEvents =
        importLabelBlock(blockedEventsElement);
      mGraphNodeList =
        new CheckedImportList<NodeProxy>(GraphProxy.class, "node");
      mGraphNodeListHandler.fromJAXBChecked(this, element, mGraphNodeList);
      final List<EdgeProxy> edges = new LinkedList<EdgeProxy>();
      mGraphEdgeListHandler.fromJAXB(this, element, edges);
      final BoxGeometry geometryElement = element.getBoxGeometry();
      final BoxGeometryProxy geometry = importBoxGeometry(geometryElement);
      return mFactory.createGraphProxy(deterministic,
                                       blockedEvents,
                                       mGraphNodeList,
                                       edges,
                                       geometry);
    } finally {
      mGraphNodeList = null;
    }
  }

  private GroupNodeProxy importGroupNode(final GroupNode element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mNodeEventListHandler.fromJAXB(this, element, eventList);
    final PlainEventListProxy propositions =
      mFactory.createPlainEventListProxy(eventList);
    final Collection<NodeProxy> immediateChildNodes =
      new CheckedImportSet<NodeProxy>(GroupNodeProxy.class, name, "node");
    final List<NodeRef> immediateChildNodesElement =
      Casting.toList(element.getNodes());
    for (final NodeRef ref : immediateChildNodesElement) {
      final NodeProxy itemProxy = importNodeRef(ref);
      immediateChildNodes.add(itemProxy);
    }
    final BoxGeometry geometryElement = element.getBoxGeometry();
    final BoxGeometryProxy geometry =
      importBoxGeometry(geometryElement);
    return mFactory.createGroupNodeProxy(name,
                                         propositions,
                                         immediateChildNodes,
                                         geometry);
  }

  private IndexedIdentifierProxy importIndexedIdentifier
    (final IndexedIdentifier element)
    throws WatersUnmarshalException
  {
    final String text = element.getText();
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
    return mFactory.createIndexedIdentifierProxy(text, name, indexes);
  }

  private InstanceProxy importInstance(final Instance element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final String moduleName = element.getModuleName();
    final List<ParameterBindingProxy> bindingList =
      new LinkedList<ParameterBindingProxy>();
    final List<ParameterBinding> bindingListElement =
      Casting.toList(element.getBindings());
    for (final ParameterBinding itemElement : bindingListElement) {
      final ParameterBindingProxy itemProxy =
        importParameterBinding(itemElement);
      bindingList.add(itemProxy);
    }
    return mFactory.createInstanceProxy(identifier, moduleName, bindingList);
  }

  private IntConstantProxy importIntConstant(final IntConstant element)
  {
    final String text = element.getText();
    final int value = element.getValue();
    return mFactory.createIntConstantProxy(text, value);
  }

  private LabelBlockProxy importLabelBlock(final LabelBlock element)
    throws WatersUnmarshalException
  {
    if (element != null) {
      final List<Proxy> eventList = new LinkedList<Proxy>();
      final List<ElementType> eventListElement =
        Casting.toList(element.getList());
      for (final ElementType itemElement : eventListElement) {
        final Proxy itemProxy = (Proxy) importElement(itemElement);
        eventList.add(itemProxy);
      }
      final LabelGeometry geometryElement = element.getLabelGeometry();
      final LabelGeometryProxy geometry = importLabelGeometry(geometryElement);
      return mFactory.createLabelBlockProxy(eventList, geometry);
    } else {
      return null;
    }
  }

  private LabelGeometryProxy importLabelGeometry
    (final LabelGeometry element)
    throws WatersUnmarshalException
  {
    if (element == null) {
      return null;
    } else {
      final Point offsetElement = element.getPoint();
      final java.awt.Point offset = importPoint(offsetElement);
      final AnchorPosition anchor = element.getAnchor();
      return mFactory.createLabelGeometryProxy(offset, anchor);
    }
  }

  private ModuleProxy importModule(final Module element)
    throws WatersUnmarshalException
  {
    return importModule(element, null);
  }

  private ModuleProxy importModule(final Module element,
                                   final URI uri)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final String comment = element.getComment();
    final List<ConstantAliasProxy> constantAliasList =
      new LinkedList<ConstantAliasProxy>();
    mModuleConstantAliasListHandler.fromJAXB(this, element, constantAliasList);
    final List<EventDeclProxy> eventDeclList =
      new LinkedList<EventDeclProxy>();
    mModuleEventDeclListHandler.fromJAXB(this, element, eventDeclList);
    final List<Proxy> eventAliasList = new LinkedList<Proxy>();
    mModuleEventAliasListHandler.fromJAXB(this, element, eventAliasList);
    final List<Proxy> componentList = new LinkedList<Proxy>();
    mModuleComponentListHandler.fromJAXB(this, element, componentList);
    return mFactory.createModuleProxy(name,
                                      comment,
                                      uri,
                                      constantAliasList,
                                      eventDeclList,
                                      eventAliasList,
                                      componentList);
  }

  private NodeProxy importNodeRef(final NodeRef element)
  {
    final String name = element.getName();
    return mGraphNodeList.find(name);
  }

  private ParameterBindingProxy importParameterBinding
    (final ParameterBinding element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final ExpressionType expressionElement = element.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    return mFactory.createParameterBindingProxy(name, expression);
  }

  private PlainEventListProxy importPlainEventList
    (final EventListExpression element)
    throws WatersUnmarshalException
  {
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mEventListExpressionEventListHandler.fromJAXB(this, element, eventList);
    return mFactory.createPlainEventListProxy(eventList);
  }

  private java.awt.Point importPoint(final Point element)
  {
    final int x = element.getX();
    final int y = element.getY();
    return new java.awt.Point(x, y);
  }

  private PointGeometryProxy importPointGeometryType
    (final PointGeometryType element)
  {
    if (element == null) {
      return null;
    } else {
      final Point pointElement = element.getPoint();
      final java.awt.Point point = importPoint(pointElement);
      return mFactory.createPointGeometryProxy(point);
    }
  }

  private QualifiedIdentifierProxy importQualifiedIdentifier
    (final QualifiedIdentifier element)
    throws WatersUnmarshalException
  {
    final String text = element.getText();
    final List<IdentifierType> idents = element.getIdentifiers();
    final IdentifierType baseElement = idents.get(0);
    final IdentifierProxy base = (IdentifierProxy) importElement(baseElement);
    final IdentifierType compElement = idents.get(1);
    final IdentifierProxy comp = (IdentifierProxy) importElement(compElement);
    return mFactory.createQualifiedIdentifierProxy(text, base, comp);
  }

  private Rectangle importRectangle(final Box element)
  {
    final int x = element.getX();
    final int y = element.getY();
    final int width = element.getWidth();
    final int height = element.getHeight();
    return new Rectangle(x, y, width, height);
  }

  private SimpleComponentProxy importSimpleComponent
    (final SimpleComponent element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final ComponentKind kind = element.getKind();
    final Graph graphElement = element.getGraph();
    final GraphProxy graph = importGraph(graphElement);
    return mFactory.createSimpleComponentProxy(identifier, kind, graph);
  }

  private SimpleIdentifierProxy importSimpleIdentifier
    (final SimpleIdentifier element)
  {
    final String text = element.getText();
    final String name = element.getName();
    return mFactory.createSimpleIdentifierProxy(text, name);
  }

  private SimpleNodeProxy importSimpleNode(final SimpleNode element)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final List<Proxy> eventList = new LinkedList<Proxy>();
    mNodeEventListHandler.fromJAXB(this, element, eventList);
    final PlainEventListProxy propositions =
      mFactory.createPlainEventListProxy(eventList);
    final boolean initial = element.isInitial();
    final PointGeometryType pointGeometryElement = element.getPointGeometry();
    final PointGeometryProxy pointGeometry =
      importPointGeometryType(pointGeometryElement);
    final PointGeometryType arrowGeometryElement =
      element.getInitialArrowGeometry();
    final PointGeometryProxy arrowGeometry =
      importPointGeometryType(arrowGeometryElement);
    final LabelGeometry labelGeometryElement = element.getLabelGeometry();
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
    (final SplineGeometry element)
  {
    if (element == null) {
      return null;
    } else {
      final List<java.awt.Point> points = new LinkedList<java.awt.Point>();
      final List<Point> pointsElement =
        Casting.toList(element.getPoints());
      for (final Point pointElement : pointsElement) {
        final java.awt.Point point = importPoint(pointElement);
        points.add(point);
      }
      final SplineKind kind = element.getKind();
      return mFactory.createSplineGeometryProxy(points, kind);
    }
  }

  private UnaryExpressionProxy importUnaryExpression
    (final UnaryExpression element)
    throws WatersUnmarshalException
  {
    final String text = element.getText();
    final String operatorName = element.getOperator();
    final UnaryOperator operator =
      mOperatorTable.getUnaryOperator(operatorName);
    final SimpleExpressionType subTermElement = element.getSubTerm();
    final SimpleExpressionProxy subTerm =
      (SimpleExpressionProxy) importElement(subTermElement);
    return mFactory.createUnaryExpressionProxy(text, operator, subTerm);
  }

  private VariableComponentProxy importVariableComponent
    (final VariableComponent element)
    throws WatersUnmarshalException
  {
    final IdentifierProxy identifier = createIdentifier(element);
    final boolean deterministic = element.isDeterministic();
    final VariableRange vrange = element.getVariableRange();
    final SimpleExpressionType typeElement = vrange.getRange();
    final SimpleExpressionProxy type =
      (SimpleExpressionProxy) importElement(typeElement);
    final VariableInitial vinit = element.getVariableInitial();
    final SimpleExpressionType initialStatePredicateElement =
      vinit.getPredicate();
    final SimpleExpressionProxy initialStatePredicate =
      (SimpleExpressionProxy) importElement(initialStatePredicateElement);
    final List<VariableMarkingProxy> markingList =
      new LinkedList<VariableMarkingProxy>();
    final List<VariableMarking> markingListElement =
      Casting.toList(element.getVariableMarkings());
    for (final VariableMarking markingElement : markingListElement) {
      final VariableMarkingProxy markingProxy =
        importVariableMarking(markingElement);
      markingList.add(markingProxy);
    }
    return mFactory.createVariableComponentProxy
      (identifier, type, deterministic, initialStatePredicate, markingList);
  }

  private VariableMarkingProxy importVariableMarking
    (final VariableMarking element)
    throws WatersUnmarshalException
  {
    final List<SimpleExpressionType> pair =
      element.getPropositionAndPredicate();
    // what if the cast to IdentifierType fails???
    final IdentifierType propositionElement = (IdentifierType) pair.get(0);
    final IdentifierProxy proposition =
      (IdentifierProxy) importElement(propositionElement);
    final SimpleExpressionType predicateElement = pair.get(1);
    final SimpleExpressionProxy predicate =
      (SimpleExpressionProxy) importElement(predicateElement);
    return mFactory.createVariableMarkingProxy(proposition, predicate);
  }


  //#########################################################################
  //# Auxiliary Methods
  private IdentifierProxy createIdentifier(final IdentifiedType element)
    throws WatersUnmarshalException
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final String name = element.getName();
    if (identifierElement == null) {
      return mFactory.createSimpleIdentifierProxy(name);
    } else if (name == null) {
      return (IdentifierProxy) importElement(identifierElement);
    } else {
      final IdentifierProxy ident =
        (IdentifierProxy) importElement(identifierElement);
      throw new WatersUnmarshalException
        ("Identified element with two names: '" + name + "' and '" +
         ident.toString() + "'!");
    }
  }


  //#########################################################################
  //# Inner Class ImportHandler
  private interface ImportHandler
  {
    public Object importElement(ElementType element)
      throws WatersUnmarshalException;
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
  private static final NodeEventListHandler
    mNodeEventListHandler =
    new NodeEventListHandler();

}
