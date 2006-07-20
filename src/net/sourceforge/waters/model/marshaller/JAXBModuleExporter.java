//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleExporter
//###########################################################################
//# $Id: JAXBModuleExporter.java,v 1.11 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
//EFA----------
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
//-------------
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
import net.sourceforge.waters.xsd.module.BinaryExpression;
import net.sourceforge.waters.xsd.module.BoxGeometry;
import net.sourceforge.waters.xsd.module.Box;
import net.sourceforge.waters.xsd.module.ColorGeometry;
import net.sourceforge.waters.xsd.module.Color;
import net.sourceforge.waters.xsd.module.ConstantAlias;
import net.sourceforge.waters.xsd.module.Edge;
import net.sourceforge.waters.xsd.module.EnumSetExpression;
import net.sourceforge.waters.xsd.module.EventAlias;
import net.sourceforge.waters.xsd.module.EventBaseType;
import net.sourceforge.waters.xsd.module.EventDecl;
import net.sourceforge.waters.xsd.module.EventListExpression;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.EventParameter;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.ForeachComponent;
import net.sourceforge.waters.xsd.module.ForeachEventAlias;
import net.sourceforge.waters.xsd.module.ForeachEvent;
import net.sourceforge.waters.xsd.module.ForeachType;
import net.sourceforge.waters.xsd.module.Graph;
import net.sourceforge.waters.xsd.module.GroupNode;
import net.sourceforge.waters.xsd.module.IdentifiedType;
import net.sourceforge.waters.xsd.module.IdentifierType;
import net.sourceforge.waters.xsd.module.IndexedIdentifier;
import net.sourceforge.waters.xsd.module.Instance;
import net.sourceforge.waters.xsd.module.IntConstant;
import net.sourceforge.waters.xsd.module.IntParameter;
import net.sourceforge.waters.xsd.module.LabelBlock;
import net.sourceforge.waters.xsd.module.LabelGeometry;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.NodeType;
import net.sourceforge.waters.xsd.module.NodeRef;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.ParameterBinding;
import net.sourceforge.waters.xsd.module.PointGeometryType;
import net.sourceforge.waters.xsd.module.Point;
import net.sourceforge.waters.xsd.module.RangeParameter;
import net.sourceforge.waters.xsd.module.SimpleComponent;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifier;
import net.sourceforge.waters.xsd.module.SimpleNode;
import net.sourceforge.waters.xsd.module.SimpleParameterType;
import net.sourceforge.waters.xsd.module.SplineGeometry;
import net.sourceforge.waters.xsd.module.UnaryExpression;
//EFA-----------------
import net.sourceforge.waters.xsd.module.GuardActionBlock;
import net.sourceforge.waters.xsd.module.Variable;
import net.sourceforge.waters.xsd.module.BooleanConstant;
//-----------------------
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
//EFA------------------- 
  public GuardActionBlock visitGuardActionBlockProxy
    (final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    final GuardActionBlock element = mFactory.createGuardActionBlock();
    copyGuardActionBlockProxy(proxy, element);
    return element;
  }
  
  public Variable visitVariableProxy(final VariableProxy proxy)
    throws VisitorException
  {
    final Variable element = mFactory.createVariable();
    copyVariableProxy(proxy, element);
    return element;
  }

  public BooleanConstant visitBooleanConstantProxy
    (final BooleanConstantProxy proxy)
    throws VisitorException
  {
    final BooleanConstant element = mFactory.createBooleanConstant();
    copyBooleanConstantProxy(proxy, element);
    return element;
  }
  //--------------------------------

  public ElementType visitAliasProxy(final AliasProxy proxy)
    throws VisitorException
  {
    final ExpressionProxy expr = proxy.getExpression();
    if (expr instanceof SimpleExpressionProxy) {
      final ConstantAlias element = mFactory.createConstantAlias();
      copyConstantAliasProxy(proxy, element);
      return element;
    } else if (expr instanceof EventListExpressionProxy) {
      final EventAlias element = mFactory.createEventAlias();
      copyEventAliasProxy(proxy, element);
      return element;
    } else {
      throw new ClassCastException
        ("Unknown expression type in alias '" + proxy.getName() + "'!");
    }
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

  public EventDecl visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    final EventDecl element = mFactory.createEventDecl();
    copyEventDeclProxy(proxy, element);
    return element;
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public EventParameter visitEventParameterProxy
      (final EventParameterProxy proxy)
    throws VisitorException
  {
    final EventParameter element = mFactory.createEventParameter();
    copyEventParameterProxy(proxy, element);
    return element;
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

  public IntParameter visitIntParameterProxy
      (final IntParameterProxy proxy)
    throws VisitorException
  {
    final IntParameter element = mFactory.createIntParameter();
    copyIntParameterProxy(proxy, element);
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

  public Object visitParameterProxy
      (final ParameterProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
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

  public RangeParameter visitRangeParameterProxy
      (final RangeParameterProxy proxy)
    throws VisitorException
  {
    final RangeParameter element = mFactory.createRangeParameter();
    copyRangeParameterProxy(proxy, element);
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

  public Object visitSimpleParameterProxy
      (final SimpleParameterProxy proxy)
    throws VisitorException
  {
    return visitParameterProxy(proxy);
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


  //#########################################################################
  //# Copying Data
  // ------------------EFA
  private void copyGuardActionBlockProxy
    (final GuardActionBlockProxy proxy,
     final GuardActionBlock element)
    throws VisitorException
  {
    final String action = proxy.getAction();
    element.setAction(action);
    final String guard = proxy.getGuard();
    element.setGuard(guard);
    final LabelGeometryProxy geometryProxy = proxy.getGeometry();
    if (geometryProxy != null) {
      final LabelGeometry geometryElement =
        visitLabelGeometryProxy(geometryProxy);
      element.setLabelGeometry(geometryElement);
    }
  }
  
  private void copyVariableProxy
    (final VariableProxy proxy,
     final Variable element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    element.setName(proxy.getName());
    final SimpleExpressionProxy typeProxy = proxy.getType();
    final SimpleExpressionType typeElement = 
      (SimpleExpressionType) typeProxy.acceptVisitor(this);
    element.setType(typeElement);
    final SimpleExpressionProxy initialValueProxy = proxy.getInitialValue();
    final SimpleExpressionType initialValueElement = 
      (SimpleExpressionType) initialValueProxy.acceptVisitor(this);
    element.setInitialValue(initialValueElement);
    final SimpleExpressionProxy markedValueProxy = proxy.getMarkedValue();
    if(markedValueProxy != null) {
      final SimpleExpressionType markedValueElement = 
        (SimpleExpressionType) markedValueProxy.acceptVisitor(this);
      element.setMarkedValue(markedValueElement);
    }
  }

  private void copyBooleanConstantProxy
    (final BooleanConstantProxy proxy,
     final BooleanConstant element)
    throws VisitorException
  {
    copySimpleExpressionProxy(proxy, element);
    element.setValue(proxy.isValue());
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
      (final AliasProxy proxy,
       final ConstantAlias element)
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
      (final AliasProxy proxy,
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
      final ColorGeometry colorGeometryElement =
        visitColorGeometryProxy(colorGeometryProxy);
      element.setColorGeometry(colorGeometryElement);
    }
  }

  private void copyEventParameterProxy
      (final EventParameterProxy proxy,
       final EventParameter element)
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
      element.setDeterministic(proxy.isDeterministic());
      final LabelBlockProxy blockedEventsProxy = proxy.getBlockedEvents();
      if (!blockedEventsProxy.getEventList().isEmpty()) {
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
      final List<EdgeProxy> edgesProxy = proxy.getEdges();
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
       final IndexedIdentifier element)
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

  private void copyIntParameterProxy
      (final IntParameterProxy proxy,
       final IntParameter element)
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
    element.setAnchor(proxy.getAnchor());
  }

  private void copyModuleProxy
      (final ModuleProxy proxy,
       final Module element)
    throws VisitorException
  {
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

  private void copyParameterProxy
      (final ParameterProxy proxy,
       final NamedType element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
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

  private void copyRangeParameterProxy
      (final RangeParameterProxy proxy,
       final RangeParameter element)
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
     final SimpleComponent element)
    throws VisitorException
  {
    final List<VariableProxy> variablesProxy = proxy.getVariables();
    mSimpleComponentVariableListHandler.toJAXB(this, variablesProxy, element);
    copyComponentProxy(proxy, element);
    element.setKind(proxy.getKind());
    final GraphProxy graphProxy = proxy.getGraph();
    final Graph graphElement = visitGraphProxy(graphProxy);
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
       final SimpleIdentifier element)
    throws VisitorException
  {
    copyIdentifierProxy(proxy, element);
  }

  private void copySimpleNodeProxy
      (final SimpleNodeProxy proxy,
       final SimpleNode element)
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
      final LabelGeometry labelGeometryElement =
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
    element.setKind(proxy.getKind());
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
  //EFA
  private static final SimpleComponentVariableListHandler
  mSimpleComponentVariableListHandler =
  new SimpleComponentVariableListHandler(mFactory);
  
  //--------------------------
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
