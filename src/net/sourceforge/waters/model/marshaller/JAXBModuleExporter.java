//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleExporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
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
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
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
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.xsd.base.AttributeMap;
import net.sourceforge.waters.xsd.base.ElementType;
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
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.ForeachComponent;
import net.sourceforge.waters.xsd.module.ForeachEvent;
import net.sourceforge.waters.xsd.module.ForeachEventAlias;
import net.sourceforge.waters.xsd.module.ForeachType;
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
import net.sourceforge.waters.xsd.module.ModuleSequence;
import net.sourceforge.waters.xsd.module.NodeRef;
import net.sourceforge.waters.xsd.module.NodeType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
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


public class JAXBModuleExporter
  extends JAXBDocumentExporter<ModuleProxy,Module>
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Overrides for Abstract Base Class JAXBExporter
  Module exportDocument(final ModuleProxy proxy)
    throws VisitorException
  {
    return visitModuleProxy(proxy);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Object visitAliasProxy(final AliasProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public BinaryExpression visitBinaryExpressionProxy
      (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    final BinaryExpression element = mFactory.createBinaryExpression();
    copyBinaryExpressionProxy(proxy, element);
    return element;
  }

  public BoxGeometry visitBoxGeometryProxy
      (final BoxGeometryProxy proxy)
    throws VisitorException
  {
    final BoxGeometry element = mFactory.createBoxGeometry();
    copyBoxGeometryProxy(proxy, element);
    return element;
  }

  public ElementType visitConstantAliasProxy(final ConstantAliasProxy proxy)
    throws VisitorException
  {
    final ConstantAlias element = mFactory.createConstantAlias();
    copyConstantAliasProxy(proxy, element);
    return element;
  }

  public ColorGeometry visitColorGeometryProxy
      (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    final ColorGeometry element = mFactory.createColorGeometry();
    copyColorGeometryProxy(proxy, element);
    return element;
  }

  public Object visitComponentProxy
      (final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Edge visitEdgeProxy
      (final EdgeProxy proxy)
    throws VisitorException
  {
    final Edge element = mFactory.createEdge();
    copyEdgeProxy(proxy, element);
    return element;
  }

  public PointGeometryType visitEndPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    final PointGeometryType element = mFactory.createPointGeometryType();
    copyPointGeometryProxy(proxy, element);
    return element;
  }

  public EnumSetExpression visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final EnumSetExpression element = mFactory.createEnumSetExpression();
    copyEnumSetExpressionProxy(proxy, element);
    return element;
  }

  public ElementType visitEventAliasProxy(final EventAliasProxy proxy)
    throws VisitorException
  {
    final EventAlias element = mFactory.createEventAlias();
    copyEventAliasProxy(proxy, element);
    return element;
  }

  public EventDecl visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    final EventDecl element = mFactory.createEventDecl();
    copyEventDeclProxy(proxy, element);
    return element;
  }

  public GuardActionBlock visitGuardActionBlockProxy
    (final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    final GuardActionBlock element = mFactory.createGuardActionBlock();
    copyGuardActionBlockProxy(proxy, element);
    return element;
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public Object visitExpressionProxy
      (final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public ForeachComponent visitForeachComponentProxy
      (final ForeachComponentProxy proxy)
    throws VisitorException
  {
    final ForeachComponent element = mFactory.createForeachComponent();
    copyForeachComponentProxy(proxy, element);
    return element;
  }

  public ForeachEventAlias visitForeachEventAliasProxy
      (final ForeachEventAliasProxy proxy)
    throws VisitorException
  {
    final ForeachEventAlias element = mFactory.createForeachEventAlias();
    copyForeachEventAliasProxy(proxy, element);
    return element;
  }

  public ForeachEvent visitForeachEventProxy
      (final ForeachEventProxy proxy)
    throws VisitorException
  {
    final ForeachEvent element = mFactory.createForeachEvent();
    copyForeachEventProxy(proxy, element);
    return element;
  }

  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Graph visitGraphProxy
      (final GraphProxy proxy)
    throws VisitorException
  {
    final Graph element = mFactory.createGraph();
    copyGraphProxy(proxy, element);
    return element;
  }

  public GroupNode visitGroupNodeProxy
      (final GroupNodeProxy proxy)
    throws VisitorException
  {
    final GroupNode element = mFactory.createGroupNode();
    copyGroupNodeProxy(proxy, element);
    return element;
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

  public IndexedIdentifier visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    final IndexedIdentifier element = mFactory.createIndexedIdentifier();
    copyIndexedIdentifierProxy(proxy, element);
    return element;
  }

  public PointGeometryType visitInitialArrowGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    final PointGeometryType element = mFactory.createPointGeometryType();
    copyPointGeometryProxy(proxy, element);
    return element;
  }

  public Instance visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    final Instance element = mFactory.createInstance();
    copyInstanceProxy(proxy, element);
    return element;
  }

  public IntConstant visitIntConstantProxy
      (final IntConstantProxy proxy)
    throws VisitorException
  {
    final IntConstant element = mFactory.createIntConstant();
    copyIntConstantProxy(proxy, element);
    return element;
  }

  public LabelBlock visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    final LabelBlock element = mFactory.createLabelBlock();
    copyLabelBlockProxy(proxy, element);
    return element;
  }

  public LabelGeometry visitLabelGeometryProxy
      (final LabelGeometryProxy proxy)
    throws VisitorException
  {
    final LabelGeometry element = mFactory.createLabelGeometry();
    copyLabelGeometryProxy(proxy, element);
    return element;
  }

  public Module visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    final Module element = mFactory.createModule();
    copyModuleProxy(proxy, element);
    return element;
  }

  public ModuleSequence visitModuleSequenceProxy
      (final ModuleSequenceProxy proxy)
    throws VisitorException
  {
    final ModuleSequence element = mFactory.createModuleSequence();
    copyModuleSequenceProxy(proxy, element);
    return element;
  }

  public Object visitNodeProxy
      (final NodeProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public ParameterBinding visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    final ParameterBinding element = mFactory.createParameterBinding();
    copyParameterBindingProxy(proxy, element);
    return element;
  }

  public EventListExpression visitPlainEventListProxy
      (final PlainEventListProxy proxy)
    throws VisitorException
  {
    final EventListExpression element = mFactory.createEventListExpression();
    copyPlainEventListProxy(proxy, element);
    return element;
  }

  public PointGeometryType visitPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    final PointGeometryType element = mFactory.createPointGeometryType();
    copyPointGeometryProxy(proxy, element);
    return element;
  }

  public QualifiedIdentifier visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy proxy)
    throws VisitorException
  {
    final QualifiedIdentifier element = mFactory.createQualifiedIdentifier();
    copyQualifiedIdentifierProxy(proxy, element);
    return element;
  }

  public SimpleComponent visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    final SimpleComponent element = mFactory.createSimpleComponent();
    copySimpleComponentProxy(proxy, element);
    return element;
  }

  public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public SimpleIdentifier visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    final SimpleIdentifier element = mFactory.createSimpleIdentifier();
    copySimpleIdentifierProxy(proxy, element);
    return element;
  }

  public SimpleNode visitSimpleNodeProxy
      (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    final SimpleNode element = mFactory.createSimpleNode();
    copySimpleNodeProxy(proxy, element);
    return element;
  }

  public SplineGeometry visitSplineGeometryProxy
      (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    final SplineGeometry element = mFactory.createSplineGeometry();
    copySplineGeometryProxy(proxy, element);
    return element;
  }

  public PointGeometryType visitStartPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    final PointGeometryType element = mFactory.createPointGeometryType();
    copyPointGeometryProxy(proxy, element);
    return element;
  }

  public UnaryExpression visitUnaryExpressionProxy
      (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    final UnaryExpression element = mFactory.createUnaryExpression();
    copyUnaryExpressionProxy(proxy, element);
    return element;
  }

  public VariableComponent visitVariableComponentProxy
      (final VariableComponentProxy proxy)
    throws VisitorException
  {
    final VariableComponent element = mFactory.createVariableComponent();
    copyVariableComponentProxy(proxy, element);
    return element;
  }

  public VariableMarking visitVariableMarkingProxy
      (final VariableMarkingProxy proxy)
    throws VisitorException
  {
    final VariableMarking element = mFactory.createVariableMarking();
    copyVariableMarkingProxy(proxy, element);
    return element;
  }


  //#########################################################################
  //# Copying Data
  // ------------------EFA
  private void copyGuardActionBlockProxy
    (final GuardActionBlockProxy proxy,
     final GuardActionBlock element)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> guardsProxy = proxy.getGuards();
    if (!guardsProxy.isEmpty()) {
      final Guards guardsElement = mFactory.createGuards();
      final List<ElementType> guardsList =
        Casting.toList(guardsElement.getList());
      copyCollection(guardsProxy, guardsList);
      element.setGuards(guardsElement);
    }
    final List<BinaryExpressionProxy> actionsProxy = proxy.getActions();
    if (!actionsProxy.isEmpty()) {
      final Actions actionsElement = mFactory.createActions();
      final List<ElementType> actionsList =
        Casting.toList(actionsElement.getList());
      copyCollection(actionsProxy, actionsList);
      element.setActions(actionsElement);
    }
    final LabelGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final LabelGeometry geometryElement =
        visitLabelGeometryProxy(geometryProxy);
      element.setLabelGeometry(geometryElement);
    }
  }
  // ------------------

  private void copyAliasProxy
      (final AliasProxy proxy,
       final IdentifiedType element)
    throws VisitorException
  {
    copyIdentifiedProxy(proxy, element);
  }

  private void copyBinaryExpressionProxy
      (final BinaryExpressionProxy proxy,
       final BinaryExpression element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    final List<SimpleExpressionType> subterms = element.getSubterms();
    final BinaryOperator operator = proxy.getOperator();
    element.setOperator(operator.getName());
    final SimpleExpressionProxy leftProxy = proxy.getLeft();
    final SimpleExpressionType leftElement =
      (SimpleExpressionType) leftProxy.acceptVisitor(this);
    subterms.add(leftElement);
    final SimpleExpressionProxy rightProxy = proxy.getRight();
    final SimpleExpressionType rightElement =
      (SimpleExpressionType) rightProxy.acceptVisitor(this);
    subterms.add(rightElement);
  }

  private void copyBoxGeometryProxy
      (final BoxGeometryProxy proxy,
       final BoxGeometry element)
    throws VisitorException
  {
    copyGeometryProxy(proxy, element);
    final Rectangle2D rectangle = proxy.getRectangle();
    final Box box = createBox(rectangle);
    element.setBox(box);
  }

  private void copyColorGeometryProxy
      (final ColorGeometryProxy proxy,
       final ColorGeometry element)
    throws VisitorException
  {
    copyGeometryProxy(proxy, element);
    final Set<java.awt.Color> colorSetProxy = proxy.getColorSet();
    for (final java.awt.Color color : colorSetProxy) {
      final int red = color.getRed();
      final int green = color.getGreen();
      final int blue = color.getBlue();
      final Color colorElement = createColor(red, green, blue);
      element.setColor(colorElement);
      // *** BUG *** Can't handle more than one color ***
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
      (final ConstantAliasProxy proxy,
       final ConstantAlias element)
    throws VisitorException
  {
    copyAliasProxy(proxy, element);
    final ConstantAliasExpression wrapper =
      mFactory.createConstantAliasExpression();
    final ExpressionProxy expressionProxy = proxy.getExpression();
    final SimpleExpressionType expressionElement =
      (SimpleExpressionType) expressionProxy.acceptVisitor(this);
    wrapper.setExpression(expressionElement);
    element.setConstantAliasExpression(wrapper);
    final ScopeKind scope = proxy.getScope();
    if (scope != ScopeKind.LOCAL) {
      element.setScope(scope);
    }
  }

  private void copyEdgeProxy
      (final EdgeProxy proxy,
       final Edge element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final NodeProxy sourceProxy = proxy.getSource();
    element.setSource(sourceProxy.getName());
    final NodeProxy targetProxy = proxy.getTarget();
    element.setTarget(targetProxy.getName());


    final LabelBlockProxy labelBlockProxy = proxy.getLabelBlock();

    if (!labelBlockProxy.getEventList().isEmpty()) {
      final LabelBlock labelBlockElement =
        visitLabelBlockProxy(labelBlockProxy);
      element.setLabelBlock(labelBlockElement);
    }

    //------------------EFA
    final GuardActionBlockProxy guardActionBlockProxy = proxy.getGuardActionBlock();
    if(guardActionBlockProxy != null) {
    	final GuardActionBlock guardActionBlockElement =
    		visitGuardActionBlockProxy(guardActionBlockProxy);
    	element.setGuardActionBlock(guardActionBlockElement);
    }
    //------------------

    final SplineGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final SplineGeometry geometryElement =
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
       final EnumSetExpression element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    final List<SimpleIdentifierProxy> itemsProxy = proxy.getItems();
    final List<ElementType> itemsElement = Casting.toList(element.getItems());
    copyCollection(itemsProxy, itemsElement);
  }

  private void copyEventAliasProxy
      (final EventAliasProxy proxy,
       final EventAlias element)
    throws VisitorException
  {
    copyAliasProxy(proxy, element);
    final ExpressionProxy eventListProxy = proxy.getExpression();
    final EventListExpression eventListElement =
      (EventListExpression) eventListProxy.acceptVisitor(this);
    element.setExpression(eventListElement);
  }

  private void copyEventDeclProxy
      (final EventDeclProxy proxy,
       final EventDecl element)
    throws VisitorException
  {
    copyIdentifiedProxy(proxy, element);
    element.setKind(proxy.getKind());
    if (!proxy.isObservable()) {
      element.setObservable(false);
    }
    final ScopeKind scope = proxy.getScope();
    if (scope != ScopeKind.LOCAL) {
      element.setScope(scope);
    }
    final List<SimpleExpressionProxy> rangesProxy = proxy.getRanges();
    if (!rangesProxy.isEmpty()) {
      final RangeList rangeList = mFactory.createRangeList();
      final List<ElementType> rangesElement =
        Casting.toList(rangeList.getRanges());
      copyCollection(rangesProxy, rangesElement);
      element.setRangeList(rangeList);
    }
    final ColorGeometryProxy colorGeometryProxy = proxy.getColorGeometry();
    if (colorGeometryProxy != null) {
      final ColorGeometry colorGeometryElement =
        visitColorGeometryProxy(colorGeometryProxy);
      element.setColorGeometry(colorGeometryElement);
    }
    final Map<String,String> attribs = proxy.getAttributes();
    if (attribs != null) {
      final AttributeMap attribsElement = createAttributeMap(attribs);
      element.setAttributeMap(attribsElement);
    }
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
       final ForeachComponent element)
    throws VisitorException
  {
    copyForeachProxy(proxy, element);
    final List<Proxy> body = proxy.getBody();
    mForeachComponentListHandler.toJAXB(this, body, element);
  }

  private void copyForeachEventAliasProxy
      (final ForeachEventAliasProxy proxy,
       final ForeachEventAlias element)
    throws VisitorException
  {
    copyForeachProxy(proxy, element);
    final List<Proxy> body = proxy.getBody();
    mForeachEventAliasListHandler.toJAXB(this, body, element);
  }

  private void copyForeachEventProxy
      (final ForeachEventProxy proxy,
       final ForeachEvent element)
    throws VisitorException
  {
    copyForeachProxy(proxy, element);
    final List<Proxy> body = proxy.getBody();
    mForeachEventListHandler.toJAXB(this, body, element);
  }

  private void copyForeachProxy
      (final ForeachProxy proxy,
       final ForeachType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final List<SimpleExpressionType> list = element.getRangeAndGuard();
    final SimpleExpressionProxy rangeProxy = proxy.getRange();
    final SimpleExpressionType rangeElement =
      (SimpleExpressionType) rangeProxy.acceptVisitor(this);
    list.add(rangeElement);
    final SimpleExpressionProxy guardProxy = proxy.getGuard();
    if (guardProxy != null) {
      final SimpleExpressionType guardElement =
        (SimpleExpressionType) guardProxy.acceptVisitor(this);
      list.add(guardElement);
    }
  }

  private void copyGraphProxy
      (final GraphProxy proxy,
       final Graph element)
    throws VisitorException
  {
    try {
      copyProxy(proxy, element);
      if (!proxy.isDeterministic()) {
        element.setDeterministic(false);
      }
      final LabelBlockProxy blockedEventsProxy = proxy.getBlockedEvents();
      if (blockedEventsProxy != null) {
        final LabelBlock blockedEventsElement =
          visitLabelBlockProxy(blockedEventsProxy);
        element.setBlockedEvents(blockedEventsElement);
      }
      mGraphNodeList = new CheckedExportList<NodeProxy>(proxy, "node");
      for (final NodeProxy node : proxy.getNodes()) {
        mGraphNodeList.checkAllUnique(node.getImmediateChildNodes());
        mGraphNodeList.insertUnique(node);
      }
      mGraphNodeListHandler.toJAXB(this, mGraphNodeList, element);
      final Collection<EdgeProxy> edgesProxy = proxy.getEdges();
      mGraphEdgeListHandler.toJAXB(this, edgesProxy, element);
    } finally {
      mGraphNodeList = null;
    }
  }

  private void copyGroupNodeProxy
      (final GroupNodeProxy proxy,
       final GroupNode element)
    throws VisitorException
  {
    copyNodeProxy(proxy, element);
    final Set<NodeProxy> childrenProxy = proxy.getImmediateChildNodes();
    final IndexedSet<NodeProxy> childrenChecked =
      new CheckedExportSet<NodeProxy>(childrenProxy, proxy, "node");
    final List<ElementType> childrenElement =
      Casting.toList(element.getNodes());
    for (final NodeProxy node : childrenChecked) {
      mGraphNodeList.checkUnique(node);
      final NodeRef noderef = mFactory.createNodeRef();
      copyNamedProxy(node, noderef);
      childrenElement.add(noderef);
    }
    final BoxGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final BoxGeometry geometryElement =
        visitBoxGeometryProxy(geometryProxy);
      element.setBoxGeometry(geometryElement);
    }
  }

  private void copyIdentifiedProxy
      (final IdentifiedProxy proxy,
       final IdentifiedType element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final IdentifierProxy identifierProxy = proxy.getIdentifier();
    if (identifierProxy instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple =
        (SimpleIdentifierProxy) identifierProxy;
      final String name = simple.getName();
      element.setName(name);
    } else {
      final IdentifierType identifierElement =
        (IdentifierType) identifierProxy.acceptVisitor(this);
      element.setIdentifier(identifierElement);
    }
  }

  private void copyIdentifierProxy
      (final IdentifierProxy proxy,
       final IdentifierType element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
  }

  private void copyIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy,
       final IndexedIdentifier element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
    element.setName(proxy.getName());
    final List<SimpleExpressionProxy> indexesProxy = proxy.getIndexes();
    final List<ElementType> indexesElement =
      Casting.toList(element.getIndexes());
    copyCollection(indexesProxy, indexesElement);
  }

  private void copyInstanceProxy
      (final InstanceProxy proxy,
       final Instance element)
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
       final IntConstant element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    element.setValue(proxy.getValue());
  }

  private void copyLabelBlockProxy
      (final LabelBlockProxy proxy,
       final LabelBlock element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final List<Proxy> eventListProxy = proxy.getEventList();
    final List<ElementType> eventListElement =
      Casting.toList(element.getList());
    copyCollection(eventListProxy, eventListElement);
    final LabelGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final LabelGeometry geometryElement =
        visitLabelGeometryProxy(geometryProxy);
      element.setLabelGeometry(geometryElement);
    }
  }

  private void copyLabelGeometryProxy
      (final LabelGeometryProxy proxy,
       final LabelGeometry element)
    throws VisitorException
  {
    copyGeometryProxy(proxy, element);
    final Point2D pointProxy = proxy.getOffset();
    final Point pointElement = createPoint(pointProxy);
    element.setPoint(pointElement);
    final AnchorPosition anchor = proxy.getAnchor();
    if (anchor != AnchorPosition.SW) {
      element.setAnchor(anchor);
    }
  }

  private void copyModuleProxy
      (final ModuleProxy proxy,
       final Module element)
    throws VisitorException
  {
    copyDocumentProxy(proxy, element);
    final List<ConstantAliasProxy> constantAliasListProxy =
      proxy.getConstantAliasList();
    mModuleConstantAliasListHandler.toJAXB
      (this, constantAliasListProxy, element);
    final List<EventDeclProxy> eventDeclListProxy = proxy.getEventDeclList();
    mModuleEventDeclListHandler.toJAXB(this, eventDeclListProxy, element);
    final List<Proxy> eventAliasListProxy = proxy.getEventAliasList();
    mModuleEventAliasListHandler.toJAXB(this, eventAliasListProxy, element);
    final List<Proxy> componentListProxy = proxy.getComponentList();
    mModuleComponentListHandler.toJAXB(this, componentListProxy, element);
  }

  private void copyModuleSequenceProxy(final ModuleSequenceProxy proxy,
                                       final ModuleSequence element)
    throws VisitorException
  {
    copyDocumentProxy(proxy, element);
    final List<ModuleProxy> list = proxy.getModules();
    mModuleSequenceListHandler.toJAXB(this, list, element);
  }

  private void copyNodeProxy
      (final NodeProxy proxy,
       final NodeType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final EventListExpressionProxy propositionsProxy = proxy.getPropositions();
    final EventListExpression propositionsElement =
      (EventListExpression) propositionsProxy.acceptVisitor(this);
    final EventListType propositionsList = propositionsElement.getEventList();
    element.setPropositions(propositionsList);
  }

  private void copyParameterBindingProxy
      (final ParameterBindingProxy proxy,
       final ParameterBinding element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    final ExpressionProxy expressionProxy = proxy.getExpression();
    final ExpressionType expressionElement =
      (ExpressionType) expressionProxy.acceptVisitor(this);
    element.setExpression(expressionElement);
  }

  private void copyPlainEventListProxy
      (final PlainEventListProxy proxy,
       final EventListExpression element)
    throws VisitorException
  {
    copyExpressionProxy(proxy, element);
    final List<Proxy> eventListProxy = proxy.getEventList();
    mEventListExpressionEventListHandler.toJAXB(this, eventListProxy, element);
  }

  private void copyPointGeometryProxy
      (final PointGeometryProxy proxy,
       final PointGeometryType element)
    throws VisitorException
  {
    copyGeometryProxy(proxy, element);
    final Point2D pointProxy = proxy.getPoint();
    final Point pointElement = createPoint(pointProxy);
    element.setPoint(pointElement);
  }

  private void copyQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy proxy,
       final QualifiedIdentifier element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
    final List<IdentifierType> list = element.getIdentifiers();
    final IdentifierProxy baseProxy = proxy.getBaseIdentifier();
    final IdentifierType baseElement =
      (IdentifierType) baseProxy.acceptVisitor(this);
    list.add(baseElement);
    final IdentifierProxy compProxy = proxy.getComponentIdentifier();
    final IdentifierType compElement =
      (IdentifierType) compProxy.acceptVisitor(this);
    list.add(compElement);
  }

  private void copySimpleComponentProxy
    (final SimpleComponentProxy proxy,
     final SimpleComponent element)
    throws VisitorException
  {
    copyComponentProxy(proxy, element);
    element.setKind(proxy.getKind());
    final GraphProxy graphProxy = proxy.getGraph();
    final Graph graphElement = visitGraphProxy(graphProxy);
    element.setGraph(graphElement);
    final Map<String,String> attribs = proxy.getAttributes();
    if (attribs != null) {
      final AttributeMap attribsElement = createAttributeMap(attribs);
      element.setAttributeMap(attribsElement);
    }
  }

  private void copySimpleExpressionProxy
      (final SimpleExpressionProxy proxy,
       final SimpleExpressionType element)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    element.setText(text);
    copyExpressionProxy(proxy, element);
  }

  private void copySimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy,
       final SimpleIdentifier element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
    element.setName(proxy.getName());
  }

  private void copySimpleNodeProxy
      (final SimpleNodeProxy proxy,
       final SimpleNode element)
    throws VisitorException
  {
    copyNodeProxy(proxy, element);
    if (proxy.isInitial()) {
      element.setInitial(true);
    }
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
      final LabelGeometry labelGeometryElement =
        visitLabelGeometryProxy(labelGeometryProxy);
      element.setLabelGeometry(labelGeometryElement);
    }
  }

  private void copySplineGeometryProxy
      (final SplineGeometryProxy proxy,
       final SplineGeometry element)
    throws VisitorException
  {
    copyGeometryProxy(proxy, element);
    final List<Point2D> pointListProxy = proxy.getPoints();
    final List<Point> pointListElement = Casting.toList(element.getPoints());
    for (final Point2D pointProxy : pointListProxy) {
      final Point pointElement = createPoint(pointProxy);
      pointListElement.add(pointElement);
    }
    final SplineKind kind = proxy.getKind();
    if (kind != SplineKind.INTERPOLATING) {
      element.setKind(kind);
    }
  }

  private void copyUnaryExpressionProxy
      (final UnaryExpressionProxy proxy,
       final UnaryExpression element)
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

  private void copyVariableComponentProxy
      (final VariableComponentProxy proxy,
       final VariableComponent element)
    throws VisitorException
  {
    copyComponentProxy(proxy, element);
    if (!proxy.isDeterministic()) {
      element.setDeterministic(false);
    }
    final SimpleExpressionProxy typeProxy = proxy.getType();
    final SimpleExpressionType typeElement =
      (SimpleExpressionType) typeProxy.acceptVisitor(this);
    final VariableRange vrange = mFactory.createVariableRange();
    vrange.setRange(typeElement);
    element.setVariableRange(vrange);
    final SimpleExpressionProxy predicateProxy =
      proxy.getInitialStatePredicate();
    final SimpleExpressionType predicateElement =
      (SimpleExpressionType) predicateProxy.acceptVisitor(this);
    final VariableInitial vinit = mFactory.createVariableInitial();
    vinit.setPredicate(predicateElement);
    element.setVariableInitial(vinit);
    final List<VariableMarkingProxy> listProxy = proxy.getVariableMarkings();
    final List<ElementType> listElement =
      Casting.toList(element.getVariableMarkings());
    copyCollection(listProxy, listElement);
  }

  private void copyVariableMarkingProxy
      (final VariableMarkingProxy proxy,
       final VariableMarking element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final List<SimpleExpressionType> pair =
      element.getPropositionAndPredicate();
    final IdentifierProxy identifierProxy = proxy.getProposition();
    final IdentifierType identifierElement =
      (IdentifierType) identifierProxy.acceptVisitor(this);
    pair.add(identifierElement);
    final SimpleExpressionProxy predicateProxy = proxy.getPredicate();
    final SimpleExpressionType predicateElement =
      (SimpleExpressionType) predicateProxy.acceptVisitor(this);
    pair.add(predicateElement);
  }


  //#########################################################################
  //# Creating Geometry Elements
  private static Box createBox(final Rectangle2D rect2d)
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

  private static Box createBox(final int x,
                                   final int y,
                                   final int width,
                                   final int height)
  {
    final Box box = mFactory.createBox();
    box.setX(x);
    box.setY(y);
    box.setWidth(width);
    box.setHeight(height);
    return box;
  }

  private static Point createPoint(final Point2D point2d)
  {
    java.awt.Point point;
    if (point2d instanceof java.awt.Point) {
      point = (java.awt.Point) point2d;
    } else {
      point = new java.awt.Point();
      point.setLocation(point2d);
    }
    return createPoint(point.x, point.y);
  }

  private static Point createPoint(final int x, final int y)
  {
    final Point point = mFactory.createPoint();
    point.setX(x);
    point.setY(y);
    return point;
  }

  private static Color createColor(final int red,
                                   final int green,
                                   final int blue)
  {
    final Color color = mFactory.createColor();
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
  private static final ModuleSequenceListHandler
    mModuleSequenceListHandler =
    new ModuleSequenceListHandler();

}
