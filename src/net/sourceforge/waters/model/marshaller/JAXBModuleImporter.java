//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleImporter
//###########################################################################
//# $Id: JAXBModuleImporter.java,v 1.13 2006-08-01 04:14:47 robi Exp $
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
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
//EFA-----------
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
//-------------
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
import net.sourceforge.waters.xsd.module.SplineKind;
import net.sourceforge.waters.xsd.module.UnaryExpression;
//EFA----------------
import net.sourceforge.waters.xsd.module.GuardActionBlock;
import net.sourceforge.waters.xsd.module.Variable;
import net.sourceforge.waters.xsd.module.BooleanConstant;
//-------------------


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
      {
        final BinaryExpression downcast = (BinaryExpression) element;
        return importBinaryExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BinaryExpression.class, handler);
    handler = new ImportHandler() {
      public BoxGeometryProxy importElement(final ElementType element)
      {
        final BoxGeometry downcast = (BoxGeometry) element;
        return importBoxGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BoxGeometry.class, handler);
    handler = new ImportHandler() {
      public Rectangle importElement(final ElementType element)
      {
        final Box downcast = (Box) element;
        return importRectangle(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Box.class, handler);
    handler = new ImportHandler() {
      public ColorGeometryProxy importElement(final ElementType element)
      {
        final ColorGeometry downcast = (ColorGeometry) element;
        return importColorGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ColorGeometry.class, handler);
    handler = new ImportHandler() {
      public java.awt.Color importElement(final ElementType element)
      {
        final Color downcast = (Color) element;
        return importColor(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Color.class, handler);
    handler = new ImportHandler() {
      public AliasProxy importElement(final ElementType element)
      {
        final ConstantAlias downcast = (ConstantAlias) element;
        return importConstantAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ConstantAlias.class, handler);
    handler = new ImportHandler() {
      public EdgeProxy importElement(final ElementType element)
      {
        final Edge downcast = (Edge) element;
        return importEdge(downcast);
      }
    };
    mHandlerMap.put
    (net.sourceforge.waters.xsd.module.Edge.class, handler);
    
    //-------------------------- EFA
    handler = new ImportHandler() {
        public GuardActionBlockProxy importElement(final ElementType element)
        {
          final GuardActionBlock downcast = (GuardActionBlock) element;
          return importGuardActionBlock(downcast);
        }
      };
      mHandlerMap.put
        (net.sourceforge.waters.xsd.module.GuardActionBlock.class, handler);
      
      handler = new ImportHandler() {
      public BooleanConstantProxy importElement(final ElementType element)
      {
        final BooleanConstant downcast = (BooleanConstant) element;
        return importBooleanConstant(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.BooleanConstant.class, handler);
    
      handler = new ImportHandler() {
 			public VariableProxy importElement(final ElementType element) {
 				final Variable downcast = (Variable) element;
 				return importVariable(downcast);
 			}
 		};
 		mHandlerMap.put(net.sourceforge.waters.xsd.module.Variable.class,
 				handler);
     
     // ---------------------------

    
    handler = new ImportHandler() {
      public EnumSetExpressionProxy importElement(final ElementType element)
      {
        final EnumSetExpression downcast = (EnumSetExpression) element;
        return importEnumSetExpression(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EnumSetExpression.class, handler);
    handler = new ImportHandler() {
      public AliasProxy importElement(final ElementType element)
      {
        final EventAlias downcast = (EventAlias) element;
        return importEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventAlias.class, handler);
    handler = new ImportHandler() {
      public EventDeclProxy importElement(final ElementType element)
      {
        final EventDecl downcast = (EventDecl) element;
        return importEventDecl(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventDecl.class, handler);
    handler = new ImportHandler() {
      public PlainEventListProxy importElement(final ElementType element)
      {
        final EventListExpression downcast =
          (EventListExpression) element;
        return importPlainEventList(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventListExpression.class, handler);
    handler = new ImportHandler() {
      public EventParameterProxy importElement(final ElementType element)
      {
        final EventParameter downcast = (EventParameter) element;
        return importEventParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.EventParameter.class, handler);
    handler = new ImportHandler() {
      public ForeachComponentProxy importElement(final ElementType element)
      {
        final ForeachComponent downcast = (ForeachComponent) element;
        return importForeachComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachComponent.class, handler);
    handler = new ImportHandler() {
      public ForeachEventAliasProxy importElement(final ElementType element)
      {
        final ForeachEventAlias downcast = (ForeachEventAlias) element;
        return importForeachEventAlias(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ForeachEventAlias.class, handler);
    handler = new ImportHandler() {
      public ForeachEventProxy importElement(final ElementType element)
      {
        final ForeachEvent downcast = (ForeachEvent) element;
        return importForeachEvent(downcast);
      }
    };
    mHandlerMap.put(net.sourceforge.waters.xsd.module.ForeachEvent.class,
				handler);
		handler = new ImportHandler() {
			public GraphProxy importElement(final ElementType element) {
				final Graph downcast = (Graph) element;
				return importGraph(downcast);
			}
		};
		mHandlerMap.put(net.sourceforge.waters.xsd.module.Graph.class, handler);
  
   
    handler = new ImportHandler() {
      public GroupNodeProxy importElement(final ElementType element)
      {
        final GroupNode downcast = (GroupNode) element;
        return importGroupNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.GroupNode.class, handler);
    handler = new ImportHandler() {
      public IndexedIdentifierProxy importElement(final ElementType element)
      {
        final IndexedIdentifier downcast = (IndexedIdentifier) element;
        return importIndexedIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IndexedIdentifier.class, handler);
    handler = new ImportHandler() {
      public InstanceProxy importElement(final ElementType element)
      {
        final Instance downcast = (Instance) element;
        return importInstance(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Instance.class, handler);
    handler = new ImportHandler() {
      public IntConstantProxy importElement(final ElementType element)
      {
        final IntConstant downcast = (IntConstant) element;
        return importIntConstant(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IntConstant.class, handler);
    handler = new ImportHandler() {
      public IntParameterProxy importElement(final ElementType element)
      {
        final IntParameter downcast = (IntParameter) element;
        return importIntParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.IntParameter.class, handler);
    
    handler = new ImportHandler() {
      public LabelBlockProxy importElement(final ElementType element)
      {
        final LabelBlock downcast = (LabelBlock) element;
        return importLabelBlock(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelBlock.class, handler);
    
    handler = new ImportHandler() {
      public LabelGeometryProxy importElement(final ElementType element)
      {
        final LabelGeometry downcast = (LabelGeometry) element;
        return importLabelGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.LabelGeometry.class, handler);
    
    
    handler = new ImportHandler() {
      public ModuleProxy importElement(final ElementType element)
      {
        final Module downcast = (Module) element;
        return importModule(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Module.class, handler);
    handler = new ImportHandler() {
      public ParameterBindingProxy importElement(final ElementType element)
      {
        final ParameterBinding downcast = (ParameterBinding) element;
        return importParameterBinding(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.ParameterBinding.class, handler);
    handler = new ImportHandler() {
      public PointGeometryProxy importElement(final ElementType element)
      {
        final PointGeometryType downcast = (PointGeometryType) element;
        return importPointGeometryType(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.PointGeometryType.class, handler);
    handler = new ImportHandler() {
      public java.awt.Point importElement(final ElementType element)
      {
        final Point downcast = (Point) element;
        return importPoint(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.Point.class, handler);
    handler = new ImportHandler() {
      public SimpleComponentProxy importElement(final ElementType element)
      {
        final SimpleComponent downcast = (SimpleComponent) element;
        return importSimpleComponent(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleComponent.class, handler);
    handler = new ImportHandler() {
      public RangeParameterProxy importElement(final ElementType element)
      {
        final RangeParameter downcast = (RangeParameter) element;
        return importRangeParameter(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.RangeParameter.class, handler);
    handler = new ImportHandler() {
      public SimpleIdentifierProxy importElement(final ElementType element)
      {
        final SimpleIdentifier downcast = (SimpleIdentifier) element;
        return importSimpleIdentifier(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleIdentifier.class, handler);
    handler = new ImportHandler() {
      public SimpleNodeProxy importElement(final ElementType element)
      {
        final SimpleNode downcast = (SimpleNode) element;
        return importSimpleNode(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SimpleNode.class, handler);
    handler = new ImportHandler() {
      public SplineGeometryProxy importElement(final ElementType element)
      {
        final SplineGeometry downcast = (SplineGeometry) element;
        return importSplineGeometry(downcast);
      }
    };
    mHandlerMap.put
      (net.sourceforge.waters.xsd.module.SplineGeometry.class, handler);
    handler = new ImportHandler() {
      public UnaryExpressionProxy importElement(final ElementType element)
      {
        final UnaryExpression downcast = (UnaryExpression) element;
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

  public ModuleProxy importDocument(final Module element,
                                    final URI uri)
  {
    return importModule(element, uri);
  }


  //#########################################################################
  //# Importing Elements
  private BinaryExpressionProxy importBinaryExpression
    (final BinaryExpression element)
  {
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
    return mFactory.createBinaryExpressionProxy(operator, left, right);
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

  private AliasProxy importConstantAlias(final ConstantAlias element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final ExpressionType expressionElement = element.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    return mFactory.createAliasProxy(identifier, expression);
  }
  
  
//EFA----------------------
  private BooleanConstantProxy importBooleanConstant(final BooleanConstant element)
  {
    final boolean value = element.isValue();
    return mFactory.createBooleanConstantProxy(value);
  }

  private VariableProxy importVariable(final Variable element) {
		final SimpleExpressionType typeElement = element.getType();
		final SimpleExpressionProxy type = (SimpleExpressionProxy) importElement(typeElement);

		final SimpleExpressionType initialValueElement = element
				.getInitialValue();
		final SimpleExpressionProxy initialValue = (SimpleExpressionProxy) importElement(initialValueElement);
		final String name = element.getName();

		if (element.getMarkedValue() != null) {
			final SimpleExpressionType markedValueElement = element
					.getMarkedValue();
			final SimpleExpressionProxy markedValue = (SimpleExpressionProxy) importElement(markedValueElement);
			return mFactory.createVariableProxy(name, type, initialValue, markedValue);
		}

		else {
			return mFactory.createVariableProxy(name, type, initialValue);
		}
	}
  private GuardActionBlockProxy importGuardActionBlock(
			final GuardActionBlock element) {
		
	  if(element != null) {
		  
		  final String guard = element.getGuard();
		  final String action = element.getAction();
		  final LabelGeometryProxy geometry = (LabelGeometryProxy) element.getLabelGeometry(); 
		  return mFactory
		  .createGuardActionBlockProxy(guard, action, geometry);
	  } else {
		  return null;
	  }
	}

   // ------------------------
 private EdgeProxy importEdge(final Edge element)
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
  {
    final List<SimpleIdentifierProxy> items =
      new LinkedList<SimpleIdentifierProxy>();
    final List<SimpleIdentifier> itemsElement =
      Casting.toList(element.getItems());
    for (final SimpleIdentifier itemElement : itemsElement) {
      final SimpleIdentifierProxy itemProxy =
        importSimpleIdentifier(itemElement);
      items.add(itemProxy);
    }
    return mFactory.createEnumSetExpressionProxy(items);
  }

  private AliasProxy importEventAlias(final EventAlias element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final EventListExpression eventListElement = element.getExpression();
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
    final ColorGeometry colorGeometryElement = element.getColorGeometry();
    final ColorGeometryProxy colorGeometry =
      importColorGeometry(colorGeometryElement);
    return mFactory.createEventDeclProxy(name,
                                         kind,
                                         observable,
                                         ranges,
                                         colorGeometry);
  }

  private EventParameterProxy importEventParameter
    (final EventParameter element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final EventDeclProxy eventDecl = importEventDecl(element);
    return mFactory.createEventParameterProxy(name, required, eventDecl);
  }

  private ForeachComponentProxy importForeachComponent
    (final ForeachComponent element)
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
      return mFactory.createGraphProxy(deterministic,
                                       blockedEvents,
                                       mGraphNodeList,
                                       edges);
    } finally {
      mGraphNodeList = null;
    }
  }

  private GroupNodeProxy importGroupNode(final GroupNode element)
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

  private InstanceProxy importInstance(final Instance element)
  {
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
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
    final int value = element.getValue();
    return mFactory.createIntConstantProxy(value);
  }

  private IntParameterProxy importIntParameter(final IntParameter element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final SimpleExpressionType defaultValueElement = element.getDefault();
    final SimpleExpressionProxy defaultValue =
      (SimpleExpressionProxy) importElement(defaultValueElement);
    return mFactory.createIntParameterProxy(name, required, defaultValue);
  }

  private LabelBlockProxy importLabelBlock(final LabelBlock element)
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
      return mFactory.createLabelBlockProxy(null, null);
    }
  }

  private LabelGeometryProxy importLabelGeometry
    (final LabelGeometry element)
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
  {
    return importModule(element, null);
  }

  private ModuleProxy importModule(final Module element,
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

  private NodeProxy importNodeRef(final NodeRef element)
    throws NameNotFoundException
  {
    final String name = element.getName();
    return mGraphNodeList.find(name);
  }

  private ParameterBindingProxy importParameterBinding
    (final ParameterBinding element)
  {
    final String name = element.getName();
    final ExpressionType expressionElement = element.getExpression();
    final ExpressionProxy expression =
      (ExpressionProxy) importElement(expressionElement);
    return mFactory.createParameterBindingProxy(name, expression);
  }

  private PlainEventListProxy importPlainEventList
    (final EventListExpression element)
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

  private RangeParameterProxy importRangeParameter
    (final RangeParameter element)
  {
    final String name = element.getName();
    final boolean required = element.isRequired();
    final SimpleExpressionType defaultValueElement = element.getDefault();
    final SimpleExpressionProxy defaultValue =
      (SimpleExpressionProxy) importElement(defaultValueElement);
    return mFactory.createRangeParameterProxy(name, required, defaultValue);
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
  {
    // EFA----------------
    final List<VariableProxy> variables = new LinkedList<VariableProxy>();
    mSimpleComponentVariableListHandler.fromJAXB(this, element, variables);
    // ------------------
    final IdentifierType identifierElement = element.getIdentifier();
    final IdentifierProxy identifier =
      (IdentifierProxy) importElement(identifierElement);
    final ComponentKind kind = element.getKind();
    final Graph graphElement = element.getGraph();
    final GraphProxy graph = importGraph(graphElement);
    return mFactory.createSimpleComponentProxy
      (identifier, kind, graph, variables);
  }

  private SimpleIdentifierProxy importSimpleIdentifier
    (final SimpleIdentifier element)
  {
    final String name = element.getName();
    return mFactory.createSimpleIdentifierProxy(name);
  }

  private SimpleNodeProxy importSimpleNode(final SimpleNode element)
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
//EFA-------------
  private static final SimpleComponentVariableListHandler
  mSimpleComponentVariableListHandler =
  new SimpleComponentVariableListHandler();
//------------------
 
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
