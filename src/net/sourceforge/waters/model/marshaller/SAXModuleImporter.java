//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AnchorPosition;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
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
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.ScopeKind;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.SplineKind;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.SchemaBase;
import net.sourceforge.waters.xsd.SchemaModule;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class SAXModuleImporter
  extends SAXDocumentImporter<ModuleProxy>
{
  //#########################################################################
  //# Constructors
  public SAXModuleImporter(final ModuleProxyFactory factory,
                           final OperatorTable optable)
    throws SAXException, ParserConfigurationException
  {
    super("waters-module.xsd", SchemaModule.NUMBER_OF_ELEMENTS);
    mFactory = factory;
    mOperatorTable = optable;

    registerHandler(SchemaModule.ELEMENT_BinaryExpression,
      new SAXHandlerCreator<BinaryExpressionProxy>() {
        @Override
        AbstractContentHandler<BinaryExpressionProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new BinaryExpressionProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_ConstantAlias,
      new SAXHandlerCreator<ConstantAliasProxy>() {
        @Override
        AbstractContentHandler<ConstantAliasProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ConstantAliasProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_Edge,
      new SAXHandlerCreator<EdgeProxy>() {
        @Override
        AbstractContentHandler<EdgeProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new EdgeProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_EnumSetExpression,
      new SAXHandlerCreator<EnumSetExpressionProxy>() {
        @Override
        AbstractContentHandler<EnumSetExpressionProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new EnumSetExpressionProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_EventAlias,
      new SAXHandlerCreator<EventAliasProxy>() {
        @Override
        AbstractContentHandler<EventAliasProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new EventAliasProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_EventDecl,
      new SAXHandlerCreator<EventDeclProxy>() {
        @Override
        AbstractContentHandler<EventDeclProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new EventDeclProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_EventListExpression,
      new SAXHandlerCreator<ExpressionProxy>() {
        @Override
        AbstractContentHandler<ExpressionProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new UnpackingEventListProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_ForeachComponent,
      new SAXHandlerCreator<ForeachProxy>() {
        @Override
        AbstractContentHandler<ForeachProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ForeachProxyHandler(parent,
                                         SchemaModule.ELEMENT_ComponentList);
        }
      });
    registerHandler(SchemaModule.ELEMENT_ForeachEvent,
      new SAXHandlerCreator<ForeachProxy>() {
        @Override
        AbstractContentHandler<ForeachProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ForeachProxyHandler(parent,
                                         SchemaModule.ELEMENT_EventList);
        }
      });
    registerHandler(SchemaModule.ELEMENT_ForeachEventAlias,
      new SAXHandlerCreator<ForeachProxy>() {
        @Override
        AbstractContentHandler<ForeachProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ForeachProxyHandler(parent,
                                         SchemaModule.ELEMENT_EventAliasList);
        }
      });
    registerHandler(SchemaModule.ELEMENT_FunctionCallExpression,
      new SAXHandlerCreator<FunctionCallExpressionProxy>() {
        @Override
        AbstractContentHandler<FunctionCallExpressionProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new FunctionCallExpressionProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_GroupNode,
      new SAXHandlerCreator<GroupNodeProxy>() {
        @Override
        AbstractContentHandler<GroupNodeProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new GroupNodeProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_IndexedIdentifier,
      new SAXHandlerCreator<IndexedIdentifierProxy>() {
        @Override
        AbstractContentHandler<IndexedIdentifierProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new IndexedIdentifierProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_Instance,
      new SAXHandlerCreator<InstanceProxy>() {
        @Override
        AbstractContentHandler<InstanceProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new InstanceProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_IntConstant,
      new SAXHandlerCreator<IntConstantProxy>() {
        @Override
        AbstractContentHandler<IntConstantProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new IntConstantProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_Module,
      new SAXHandlerCreator<ModuleProxy>() {
        @Override
        AbstractContentHandler<ModuleProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ModuleProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_QualifiedIdentifier,
      new SAXHandlerCreator<QualifiedIdentifierProxy>() {
        @Override
        AbstractContentHandler<QualifiedIdentifierProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new QualifiedIdentifierProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_SimpleComponent,
      new SAXHandlerCreator<SimpleComponentProxy>() {
        @Override
        AbstractContentHandler<SimpleComponentProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new SimpleComponentProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_SimpleIdentifier,
      new SAXHandlerCreator<SimpleIdentifierProxy>() {
        @Override
        AbstractContentHandler<SimpleIdentifierProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new SimpleIdentifierProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_SimpleNode,
      new SAXHandlerCreator<SimpleNodeProxy>() {
        @Override
        AbstractContentHandler<SimpleNodeProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new SimpleNodeProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_UnaryExpression,
      new SAXHandlerCreator<UnaryExpressionProxy>() {
        @Override
        AbstractContentHandler<UnaryExpressionProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new UnaryExpressionProxyHandler(parent);
        }
      });
    registerHandler(SchemaModule.ELEMENT_VariableComponent,
      new SAXHandlerCreator<VariableComponentProxy>() {
        @Override
        AbstractContentHandler<VariableComponentProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new VariableComponentProxyHandler(parent);
        }
      });
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.SAXDocumentImporter<ModuleProxy>
  @Override
  Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }


  //#########################################################################
  //# Inner Class BinaryExpressionProxyHandler
  private class BinaryExpressionProxyHandler
    extends SimpleExpressionProxyHandler<BinaryExpressionProxy>
  {
    //#######################################################################
    //# Constructor
    private BinaryExpressionProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<BinaryExpressionProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      final AbstractContentHandler<?> handler = pushHandler(localName, atts);
      if (handler instanceof SimpleExpressionProxyHandler) {
        if (mLHSHandler == null) {
          mLHSHandler = (SimpleExpressionProxyHandler<?>) handler;
        } else if (mRHSHandler == null) {
          mRHSHandler = (SimpleExpressionProxyHandler<?>) handler;
        }
      }
    }

    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Operator)) {
        mOperator = mOperatorTable.getBinaryOperator(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    BinaryExpressionProxy getResult() throws SAXParseException
    {
      final String text = getText();
      final SimpleExpressionProxy lhs = mLHSHandler.getResult();
      final SimpleExpressionProxy rhs = mRHSHandler.getResult();
      return mFactory.createBinaryExpressionProxy(text, mOperator, lhs, rhs);
    }

    //#######################################################################
    //# Data Members
    private BinaryOperator mOperator;
    private SimpleExpressionProxyHandler<?> mLHSHandler;
    private SimpleExpressionProxyHandler<?> mRHSHandler;
  }


  //#########################################################################
  //# Inner Class BoxGeometryProxyHandler
  private class BoxGeometryProxyHandler
    extends AbstractContentHandler<BoxGeometryProxy>
  {
    //#######################################################################
    //# Constructor
    private BoxGeometryProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<BoxGeometryProxy>
    @Override
    public void startSubElement(final String localName,
                             final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Box)) {
        mBoxHandler = new BoxHandler(this);
        pushHandler(mBoxHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    BoxGeometryProxy getResult()
    {
      final Rectangle box = mBoxHandler.getResult();
      return mFactory.createBoxGeometryProxy(box);
    }

    //#######################################################################
    //# Data Members
    private BoxHandler mBoxHandler = null;
  }


  //#########################################################################
  //# Inner Class BoxHandler
  private class BoxHandler
    extends AbstractContentHandler<Rectangle>
  {
    //#######################################################################
    //# Constructor
    private BoxHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<Rectangle>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaModule.ATTRIB_X)) {
        mX = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Y)) {
        mY = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Width)) {
        mWidth = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Height)) {
        mHeight = Integer.parseInt(value);
      }
    }

    @Override
    Rectangle getResult()
    {
      return new Rectangle(mX, mY, mWidth, mHeight);
    }

    //#######################################################################
    //# Data Members
    private int mX;
    private int mY;
    private int mWidth;
    private int mHeight;
  }


  //#########################################################################
  //# Inner Class ColorGeometryProxyHandler
  private class ColorGeometryProxyHandler
    extends AbstractContentHandler<ColorGeometryProxy>
  {
    //#######################################################################
    //# Constructor
    private ColorGeometryProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mColorHandler = new ColorHandler(this);
      mColors = new ArrayList<>(1);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ColorGeometryProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Color)) {
        pushHandler(mColorHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
    {
      if (subHandler == mColorHandler) {
        final Color color = mColorHandler.getResult();
        mColors.add(color);
      }
    }

    @Override
    ColorGeometryProxy getResult()
    {
      return mFactory.createColorGeometryProxy(mColors);
    }

    //#######################################################################
    //# Data Members
    private final ColorHandler mColorHandler;
    private final List<Color> mColors;
  }


  //#########################################################################
  //# Inner Class ColorHandler
  private class ColorHandler
    extends AbstractContentHandler<Color>
  {
    //#######################################################################
    //# Constructor
    private ColorHandler(final ColorGeometryProxyHandler parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<Color>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaModule.ATTRIB_Red)) {
        mRed = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Green)) {
        mGreen = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Blue)) {
        mBlue = Integer.parseInt(value);
      }
    }

    @Override
    Color getResult()
    {
      return new Color(mRed, mGreen, mBlue);
    }

    //#######################################################################
    //# Data Members
    private int mRed;
    private int mGreen;
    private int mBlue;
  }


  //#########################################################################
  //# Inner Class ConstantAliasProxyHandler
  private class ConstantAliasProxyHandler
    extends IdentifiedProxyHandler<ConstantAliasProxy>
  {
    //#######################################################################
    //# Constructor
    private ConstantAliasProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ConstantAliasProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Scope)) {
        mScope = Enum.valueOf(ScopeKind.class, value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_ConstantAliasExpression)) {
        mExpressionHandler =
          new SingletonHandler<>(this, SimpleExpressionProxy.class);
        pushHandler(mExpressionHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    ConstantAliasProxy getResult()
    {
      final IdentifierProxy ident = getIdentifier();
      final SimpleExpressionProxy expr = mExpressionHandler.getResult();
      return mFactory.createConstantAliasProxy(ident, expr, mScope);
    }

    //#######################################################################
    //# Data Members
    private ScopeKind mScope = SchemaModule.DEFAULT_ScopeKind;
    private SingletonHandler<SimpleExpressionProxy> mExpressionHandler = null;
  }


  //#########################################################################
  //# Inner Class EdgeProxyHandler
  private class EdgeProxyHandler
    extends AbstractContentHandler<EdgeProxy>
  {
    //#######################################################################
    //# Constructor
    private EdgeProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EdgeProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Source)) {
        mSource = getNode(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Target)) {
        mTarget = getNode(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_LabelBlock)) {
        mLabelBlockProxyHandler = new LabelBlockProxyHandler(this);
        pushHandler(mLabelBlockProxyHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_GuardActionBlock)) {
        mGuardActionBlockProxyHandler = new GuardActionBlockProxyHandler(this);
        pushHandler(mGuardActionBlockProxyHandler);
      } else if (isImportingGeometry()) {
        if (localName.equals(SchemaModule.ELEMENT_SplineGeometry)) {
          mSplineGeometryHandler = new SplineGeometryProxyHandler(this);
          pushHandler(mSplineGeometryHandler, atts);
        } else if (localName.equals(SchemaModule.ELEMENT_StartPointGeometry)) {
          mStartPointHandler = new PointGeometryProxyHandler(this);
          pushHandler(mStartPointHandler);
        } else if (localName.equals(SchemaModule.ELEMENT_EndPointGeometry)) {
          mEndPointHandler = new PointGeometryProxyHandler(this);
          pushHandler(mEndPointHandler);
        } else {
          super.startSubElement(localName, atts);
        }
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    EdgeProxy getResult() throws SAXParseException
    {
      final LabelBlockProxy labelBlock = mLabelBlockProxyHandler == null ?
        null : mLabelBlockProxyHandler.getResult();
      final GuardActionBlockProxy gaBlock =
        mGuardActionBlockProxyHandler == null ? null :
        mGuardActionBlockProxyHandler.getResult();
      final SplineGeometryProxy geo = mSplineGeometryHandler == null ?
        null : mSplineGeometryHandler.getResult();
      final PointGeometryProxy startGeo = mStartPointHandler == null ?
        null : mStartPointHandler.getResult();
      final PointGeometryProxy endGeo = mEndPointHandler == null ?
        null : mEndPointHandler.getResult();
      return mFactory.createEdgeProxy(mSource, mTarget, labelBlock,
                                      gaBlock, geo, startGeo, endGeo);
    }

    //#######################################################################
    //# Auxiliary Methods
    private NodeProxy getNode(final String name)
      throws SAXParseException
    {
      final GraphProxyHandler handler = getAncestor(GraphProxyHandler.class);
      final NodeProxy node = handler.getNode(name);
      if (node == null) {
        final StringBuilder builder = new StringBuilder();
        builder.append("An edge references an undefined node named '");
        builder.append(name);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
      return node;
    }

    //#######################################################################
    //# Data Members
    private NodeProxy mSource;
    private NodeProxy mTarget;
    private LabelBlockProxyHandler mLabelBlockProxyHandler = null;
    private GuardActionBlockProxyHandler mGuardActionBlockProxyHandler = null;
    private SplineGeometryProxyHandler mSplineGeometryHandler = null;
    private PointGeometryProxyHandler mStartPointHandler = null;
    private PointGeometryProxyHandler mEndPointHandler = null;
  }


  //#########################################################################
  //# Inner Class EnumSetExpressionProxyHandler
  private class EnumSetExpressionProxyHandler
    extends SimpleExpressionProxyHandler<EnumSetExpressionProxy>
  {
    //#######################################################################
    //# Constructor
    private EnumSetExpressionProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EnumSetExpressionProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_SimpleIdentifier)) {
        pushHandler(mIdentifierHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
    {
      if (subHandler == mIdentifierHandler) {
        final SimpleIdentifierProxy ident = mIdentifierHandler.getResult();
        mList.add(ident);
        mIdentifierHandler.reset();
      }
    }

    @Override
    EnumSetExpressionProxy getResult()
    {
      final String text = getText();
      return mFactory.createEnumSetExpressionProxy(text, mList);
    }

    //#######################################################################
    //# Data Members
    private final SimpleIdentifierProxyHandler mIdentifierHandler =
      new SimpleIdentifierProxyHandler(this);
    private final List<SimpleIdentifierProxy> mList = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class EventDeclProxyHandler
  private class EventDeclProxyHandler
    extends IdentifiedProxyHandler<EventDeclProxy>
  {
    //#######################################################################
    //# Constructor
    private EventDeclProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventDeclProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Kind)) {
        mKind = Enum.valueOf(EventKind.class, value);
      } else if (localName.equals(SchemaModule.ATTRIB_Observable)) {
        mObservable = Boolean.parseBoolean(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Scope)) {
        mScope = Enum.valueOf(ScopeKind.class, value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_RangeList)) {
        mRangeListHandler = new GenericListHandler<>(this, SimpleExpressionProxy.class);
        pushHandler(mRangeListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_ColorGeometry) &&
                 isImportingGeometry()) {
        mColorGeometryProxyHandler = new ColorGeometryProxyHandler(this);
        pushHandler(mColorGeometryProxyHandler);
      } else if (localName.equals(SchemaBase.ELEMENT_AttributeMap)) {
        mAttributeMapHandler = new AttributeMapHandler(this);
        pushHandler(mAttributeMapHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    EventDeclProxy getResult() throws SAXParseException
    {
      final IdentifierProxy ident = getIdentifier();
      final List<SimpleExpressionProxy> rangeList =
        mRangeListHandler == null ? null : mRangeListHandler.getResult();
      final ColorGeometryProxy geo = mColorGeometryProxyHandler == null ? null :
        mColorGeometryProxyHandler.getResult();
      final Map<String,String> attribs =
        mAttributeMapHandler == null ? null : mAttributeMapHandler.getResult();
      return mFactory.createEventDeclProxy(ident, mKind, mObservable,
                                           mScope, rangeList, geo, attribs);
    }

    //#######################################################################
    //# Data Members
    private EventKind mKind = null;
    private boolean mObservable = SchemaModule.DEFAULT_Observable;
    private ScopeKind mScope = SchemaModule.DEFAULT_ScopeKind;
    private ListHandler<SimpleExpressionProxy> mRangeListHandler = null;
    private ColorGeometryProxyHandler mColorGeometryProxyHandler = null;
    private AttributeMapHandler mAttributeMapHandler = null;
  }


  //#########################################################################
  //# Inner Class EventAliasProxyHandler
  private class EventAliasProxyHandler
    extends IdentifiedProxyHandler<EventAliasProxy>
  {
    //#######################################################################
    //# Constructor
    private EventAliasProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventAliasProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_EventListExpression)) {
        mExpressionHandler = new UnpackingEventListProxyHandler(this);
        pushHandler(mExpressionHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    EventAliasProxy getResult()
      throws SAXParseException
    {
      final IdentifierProxy ident = getIdentifier();
      final ExpressionProxy expr = mExpressionHandler.getResult();
      return mFactory.createEventAliasProxy(ident, expr);
    }

    //#######################################################################
    //# Data Members
    UnpackingEventListProxyHandler mExpressionHandler = null;
  }


  //#########################################################################
  //# Inner Class EventListExpressionProxyHandler
  private abstract class EventListExpressionProxyHandler
    <P extends ExpressionProxy>
    extends AbstractContentHandler<P>
  {
    //#######################################################################
    //# Constructor
    private EventListExpressionProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
     @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final Object result = subHandler.getResult();
      if (result != null &&
          (result instanceof IdentifierProxy ||
           result instanceof ForeachProxy)) {
        final Proxy proxy = (Proxy) result;
        mEventList.add(proxy);
      }
    }

    //#######################################################################
    //# Parsing Support
    List<Proxy> getEventList()
    {
      return mEventList;
    }

    //#######################################################################
    //# Data Members
    private final List<Proxy> mEventList = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class ForeachProxyHandler
  private class ForeachProxyHandler
    extends NamedProxyHandler<ForeachProxy>
  {
    //#######################################################################
    //# Constructor
    private ForeachProxyHandler(final AbstractContentHandler<?> parent,
                                final String key)
    {
      super(parent);
      mListKey = key;
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ForeachProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(mListKey)) {
        mBodyHandler = new GenericListHandler<>(this, Proxy.class);
        pushHandler(mBodyHandler);
      } else {
        final AbstractContentHandler<?> handler = pushHandler(localName, atts);
        if (handler instanceof SimpleExpressionProxyHandler) {
          if (mRangeHandler == null) {
            mRangeHandler = (SimpleExpressionProxyHandler<?>) handler;
          } else if (mGuardHandler == null) {
            mGuardHandler = (SimpleExpressionProxyHandler<?>) handler;
          }
        }
      }
    }

    @Override
    ForeachProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      final SimpleExpressionProxy range =
        mRangeHandler == null ? null : mRangeHandler.getResult();
      final SimpleExpressionProxy guard =
        mGuardHandler == null ? null : mGuardHandler.getResult();
      final List<Proxy> body =
        mBodyHandler == null ? null : mBodyHandler.getResult();
      return mFactory.createForeachProxy(name, range, guard, body);
    }

    //#######################################################################
    //# Data Members
    private final String mListKey;
    private SimpleExpressionProxyHandler<?> mRangeHandler = null;
    private SimpleExpressionProxyHandler<?> mGuardHandler = null;
    private ListHandler<Proxy> mBodyHandler = null;
  }


  //#########################################################################
  //# Inner Class FunctionCallExpressionProxyHandler
  private class FunctionCallExpressionProxyHandler
    extends SimpleExpressionProxyHandler<FunctionCallExpressionProxy>
  {
    //#######################################################################
    //# Constructor
    private FunctionCallExpressionProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<FunctionCallExpressionProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_FunctionName)) {
        mFunctionName = value;
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final Object result = subHandler.getResult();
      if (result != null && result instanceof SimpleExpressionProxy) {
        final SimpleExpressionProxy arg = (SimpleExpressionProxy) result;
        mArguments.add(arg);
      }
    }

    @Override
    FunctionCallExpressionProxy getResult()
    {
      final String text = getText();
      return mFactory.createFunctionCallExpressionProxy
        (text, mFunctionName, mArguments);
    }

    //#######################################################################
    //# Data Members
    private String mFunctionName;
    private final List<SimpleExpressionProxy> mArguments = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class GraphProxyHandler
  private class GraphProxyHandler extends AbstractContentHandler<GraphProxy>
  {
    //#######################################################################
    //# Constructor
    private GraphProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<GraphProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Deterministic)) {
        mDeterministic = Boolean.parseBoolean(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_NodeList)) {
        mNodeListHandler = new GenericUniqueListHandler<>(this, NodeProxy.class);
        pushHandler(mNodeListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_EdgeList)) {
        mEdgeListHandler = new GenericListHandler<>(this, EdgeProxy.class);
        pushHandler(mEdgeListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_LabelBlock)) {
        mBlockedEventsHandler = new LabelBlockProxyHandler(this);
        pushHandler(mBlockedEventsHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    GraphProxy getResult()
      throws SAXParseException
    {
      final LabelBlockProxy blocked =
        mBlockedEventsHandler == null ? null : mBlockedEventsHandler.getResult();
      final List<NodeProxy> nodes =
        mNodeListHandler == null ? null : mNodeListHandler.getResult();
      final List<EdgeProxy> edges =
        mEdgeListHandler == null ? null : mEdgeListHandler.getResult();
      return mFactory.createGraphProxy(mDeterministic, blocked, nodes, edges);
    }

    //#######################################################################
    //# Parsing Support
    NodeProxy getNode(final String name)
    {
      return mNodeListHandler == null ? null : mNodeListHandler.get(name);
    }

    //#######################################################################
    //# Data Members
    private boolean mDeterministic = SchemaModule.DEFAULT_Deterministic;
    private LabelBlockProxyHandler mBlockedEventsHandler = null;
    private UniqueListHandler<NodeProxy> mNodeListHandler = null;
    private ListHandler<EdgeProxy> mEdgeListHandler = null;
  }


  //#########################################################################
  //# Inner Class GroupNodeProxyHandler
  private class GroupNodeProxyHandler
    extends NodeProxyHandler<GroupNodeProxy>
  {
    //#######################################################################
    //# Constructor
    private GroupNodeProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<GroupNodeProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_NodeRef)) {
        pushHandler(mNodeRefHandler, atts);
      } else if (isImportingGeometry() &&
                 localName.equals(SchemaModule.ELEMENT_BoxGeometry)) {
        mGeometryHandler = new BoxGeometryProxyHandler(this);
        pushHandler(mGeometryHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mNodeRefHandler) {
        final NodeProxy node = mNodeRefHandler.getResult();
        mChildNodes.add(node);
      }
    }

    @Override
    GroupNodeProxy getResult()
    {
      final String name = getName();
      final PlainEventListProxy props = getEventList();
      final Map<String,String> attribs = getAttributeMap();
      final BoxGeometryProxy geo =
        mGeometryHandler == null ? null : mGeometryHandler.getResult();
      final GroupNodeProxy node =
        mFactory.createGroupNodeProxy(name, props, attribs, mChildNodes, geo);
      return node;
    }

    //#######################################################################
    //# Data Members
    private final NodeRefHandler mNodeRefHandler = new NodeRefHandler(this);
    private final List<NodeProxy> mChildNodes = new ArrayList<>();
    private BoxGeometryProxyHandler mGeometryHandler = null;
  }


  //#########################################################################
  //# Inner Class GuardActionBlockProxyHandler
  private class GuardActionBlockProxyHandler
    extends AbstractContentHandler<GuardActionBlockProxy>
  {
    //#######################################################################
    //# Constructor
    private GuardActionBlockProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<GuardActionBlockProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Guards)) {
        mGuardHandler = new GenericListHandler<>(this, SimpleExpressionProxy.class);
        pushHandler(mGuardHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_Actions)) {
        mActionHandler = new GenericListHandler<>(this, BinaryExpressionProxy.class);
        pushHandler(mActionHandler);
      } else if (isImportingGeometry() &&
                 localName.equals(SchemaModule.ELEMENT_LabelGeometry)) {
        mGeometryHandler = new LabelGeometryProxyHandler(this);
        pushHandler(mGeometryHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    GuardActionBlockProxy getResult() throws SAXParseException
    {
      final List<SimpleExpressionProxy> guards =
        mGuardHandler == null ? null : mGuardHandler.getResult();
      final List<BinaryExpressionProxy> actions =
        mActionHandler == null ? null : mActionHandler.getResult();
      final LabelGeometryProxy geo =
        mGeometryHandler == null ? null : mGeometryHandler.getResult();
      return mFactory.createGuardActionBlockProxy(guards, actions, geo);
    }

    //#######################################################################
    //# Data Members
    private ListHandler<SimpleExpressionProxy> mGuardHandler = null;
    private ListHandler<BinaryExpressionProxy> mActionHandler = null;
    private LabelGeometryProxyHandler mGeometryHandler = null;
  }


  //#########################################################################
  //# Inner Class IdentifiedProxyHandler
  private abstract class IdentifiedProxyHandler<P extends IdentifiedProxy>
    extends NamedProxyHandler<P>
  {
    //#######################################################################
    //# Constructor
    private IdentifiedProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (mIdentifier ==  null &&
          subHandler instanceof IdentifierProxyHandler) {
        mIdentifier = (IdentifierProxy) subHandler.getResult();
      }
    }

    @Override
    void reset()
    {
      super.reset();
      mIdentifier = null;
    }

    //#######################################################################
    //# Parsing Support
    IdentifierProxy getIdentifier()
    {
      if (mIdentifier == null) {
        final String name = getName();
        return mFactory.createSimpleIdentifierProxy(name);
      } else {
        return mIdentifier;
      }
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mIdentifier = null;
  }


  //#########################################################################
  //# Inner Class IdentifierProxyHandler
  private abstract class IdentifierProxyHandler<P extends IdentifierProxy>
    extends SimpleExpressionProxyHandler<P>
  {
    //#######################################################################
    //# Constructor
    private IdentifierProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Name)) {
        mName = value;
      } else {
        super.setAttribute(localName, value);
      }
    }

    //#######################################################################
    //# Parsing Support
    String getName()
    {
      return mName;
    }

    //#######################################################################
    //# Data Members
    private String mName = "";
  }


  //#########################################################################
  //# Inner Class IndexedIdentifierProxyHandler
  private class IndexedIdentifierProxyHandler
    extends IdentifierProxyHandler<IndexedIdentifierProxy>
  {
    //#######################################################################
    //# Constructor
    private IndexedIdentifierProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<IndexedIdentifierProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      final AbstractContentHandler<?> handler = pushHandler(localName, atts);
      if (handler instanceof SimpleExpressionProxyHandler) {
        mIndexHandler = (SimpleExpressionProxyHandler<?>) handler;
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mIndexHandler) {
        final SimpleExpressionProxy expr = mIndexHandler.getResult();
        mIndexes.add(expr);
      }
    }

    @Override
    IndexedIdentifierProxy getResult()
    {
      final String text = getText();
      final String name = getName();
      return mFactory.createIndexedIdentifierProxy(text, name, mIndexes);
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxyHandler<?> mIndexHandler = null;
    private final List<SimpleExpressionProxy> mIndexes = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class InstanceProxyHandler
  private class InstanceProxyHandler
    extends IdentifiedProxyHandler<InstanceProxy>
  {
    //#######################################################################
    //# Constructor
    private InstanceProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<InstanceProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_ModuleName)) {
        mModuleName = value;
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_ParameterBinding)) {
        pushHandler(mBindingHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mBindingHandler) {
        final ParameterBindingProxy binding = mBindingHandler.getResult();
        mBindings.add(binding);
        mBindingHandler.reset();
      } else {
        super.endSubElement(subHandler);
      }
    }

    @Override
    InstanceProxy getResult()
    {
      final IdentifierProxy ident = getIdentifier();
      return mFactory.createInstanceProxy(ident, mModuleName, mBindings);
    }

    //#######################################################################
    //# Data Members
    private String mModuleName;
    private final ParameterBindingProxyHandler mBindingHandler =
      new ParameterBindingProxyHandler(this);
    private final List<ParameterBindingProxy> mBindings = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class IntConstantProxyHandler
  private class IntConstantProxyHandler
    extends SimpleExpressionProxyHandler<IntConstantProxy>
  {
    //#######################################################################
    //# Constructor
    private IntConstantProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<IntConstantProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Value)) {
        mValue = Integer.parseInt(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    IntConstantProxy getResult()
    {
      final String text = getText();
      return mFactory.createIntConstantProxy(text, mValue);
    }

    //#######################################################################
    //# Data Members
    private int mValue;
  }


  //#########################################################################
  //# Inner Class LabelBlockProxyHandler
  private class LabelBlockProxyHandler
    extends EventListExpressionProxyHandler<LabelBlockProxy>
  {
    //#######################################################################
    //# Constructor
    private LabelBlockProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<LabelBlockProxy>
    @Override
    public void startSubElement(final String localName,
                             final Attributes atts)
      throws SAXParseException
    {
      if (isImportingGeometry() &&
          localName.equals(SchemaModule.ELEMENT_LabelGeometry)) {
        mGeometryHandler = new LabelGeometryProxyHandler(this);
        pushHandler(mGeometryHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    LabelBlockProxy getResult()
    {
      final List<Proxy> eventList = getEventList();
      final LabelGeometryProxy geo =
        mGeometryHandler == null ? null : mGeometryHandler.getResult();
      return mFactory.createLabelBlockProxy(eventList, geo);
    }

    //#######################################################################
    //# Data Members
    private LabelGeometryProxyHandler mGeometryHandler = null;
  }



  //#########################################################################
  //# Inner Class LabelGeometryProxyHandler
  private class LabelGeometryProxyHandler
    extends AbstractContentHandler<LabelGeometryProxy>
  {
    //#######################################################################
    //# Constructor
    private LabelGeometryProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<LabelGeometryProxy>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaModule.ATTRIB_Anchor)) {
        mAnchor = Enum.valueOf(AnchorPosition.class, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Point)) {
        mPointHandler = new PointHandler(this);
        pushHandler(mPointHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    LabelGeometryProxy getResult()
    {
      final Point point = mPointHandler.getResult();
      return mFactory.createLabelGeometryProxy(point, mAnchor);
    }

    //#######################################################################
    //# Data Members
    private PointHandler mPointHandler = null;
    private AnchorPosition mAnchor = SchemaModule.DEFAULT_AnchorPosition;
  }


  //#########################################################################
  //# Inner Class ModuleProxyHandler
  private class ModuleProxyHandler extends DocumentProxyHandler<ModuleProxy>
  {
    //#######################################################################
    //# Constructor
    private ModuleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ModuleProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_ConstantAliasList)) {
        mConstantAliasListHandler =
          new GenericUniqueListHandler<>(this, ConstantAliasProxy.class);
        pushHandler(mConstantAliasListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_EventDeclList)) {
        mEventDeclListHandler =
          new GenericUniqueListHandler<>(this, EventDeclProxy.class);
        pushHandler(mEventDeclListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_EventAliasList)) {
        mEventAliasListHandler = new GenericListHandler<>(this, Proxy.class);
        pushHandler(mEventAliasListHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_ComponentList)) {
        mComponentListHandler = new GenericListHandler<>(this, Proxy.class);
        pushHandler(mComponentListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    ModuleProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<ConstantAliasProxy> constantAliasList =
        mConstantAliasListHandler == null ? null :
        mConstantAliasListHandler.getResult();
      final List<EventDeclProxy> eventDeclList =
        mEventDeclListHandler == null ? null :
        mEventDeclListHandler.getResult();
      final List<Proxy> eventAliasList =
        mEventAliasListHandler == null ? null :
        mEventAliasListHandler.getResult();
      final List<Proxy> componentList =
        mComponentListHandler == null ? null :
        mComponentListHandler.getResult();
      return mFactory.createModuleProxy(name, comment, uri,
                                        constantAliasList, eventDeclList,
                                        eventAliasList, componentList);
    }

    //#######################################################################
    //# Data Members
    private ListHandler<ConstantAliasProxy> mConstantAliasListHandler;
    private ListHandler<EventDeclProxy> mEventDeclListHandler;
    private ListHandler<Proxy> mEventAliasListHandler;
    private ListHandler<Proxy> mComponentListHandler;
  }


  //#########################################################################
  //# Inner Class NodeProxyHandler
  private abstract class NodeProxyHandler<P extends NodeProxy>
    extends NamedProxyHandler<P>
  {
    //#######################################################################
    //# Constructor
    private NodeProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_EventList)) {
        mEventListHandler = new PlainEventListProxyHandler(this);
        pushHandler(mEventListHandler);
      } else if (localName.equals(SchemaBase.ELEMENT_AttributeMap)) {
        mAttributeMapHandler = new AttributeMapHandler(this);
        pushHandler(mAttributeMapHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    //#######################################################################
    //# Parsing Support
    PlainEventListProxy getEventList()
    {
      return mEventListHandler == null ? null : mEventListHandler.getResult();
    }

    Map<String,String> getAttributeMap()
    {
      return
        mAttributeMapHandler == null ? null : mAttributeMapHandler.getResult();
    }

    //#######################################################################
    //# Data Members
    private PlainEventListProxyHandler mEventListHandler = null;
    private AttributeMapHandler mAttributeMapHandler = null;
  }


  //#########################################################################
  //# Inner Class NodeRefHandler
  private class NodeRefHandler
    extends NamedProxyHandler<NodeProxy>
  {
    //#######################################################################
    //# Constructor
    private NodeRefHandler(final GroupNodeProxyHandler parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<NodeProxy>
    @Override
    NodeProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      return getNode(name);
    }

    //#######################################################################
    //# Auxiliary Methods
    private NodeProxy getNode(final String refName)
      throws SAXParseException
    {
      final GraphProxyHandler handler = getAncestor(GraphProxyHandler.class);
      final NodeProxy node = handler.getNode(refName);
      if (node == null) {
        final StringBuilder builder = new StringBuilder();
        final GroupNodeProxyHandler parent = (GroupNodeProxyHandler) getParent();
        final String groupName = parent.getName();
        if (groupName != null && groupName.length() > 0) {
          builder.append("The group node '");
          builder.append(groupName);
          builder.append('\'');
        } else {
          builder.append("A group node");
        }
        builder.append(" references an undefined node named '");
        builder.append(refName);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
      return node;
    }
  }


  //#########################################################################
  //# Inner Class ParameterBindingProxyHandler
  private class ParameterBindingProxyHandler
    extends NamedProxyHandler<ParameterBindingProxy>
  {
    //#######################################################################
    //# Constructor
    private ParameterBindingProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ParameterBindingProxy>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (mExpression == null) {
        final Object result = subHandler.getResult();
        if (result != null && result instanceof ExpressionProxy) {
          mExpression = (ExpressionProxy) result;
        }
      }
    }

    @Override
    ParameterBindingProxy getResult()
    {
      final String name = getName();
      return mFactory.createParameterBindingProxy(name, mExpression);
    }

    @Override
    void reset()
    {
      super.reset();
      mExpression = null;
    }

    //#######################################################################
    //# Data Members
    private ExpressionProxy mExpression;
  }


  //#########################################################################
  //# Inner Class PlainEventListProxyHandler
  private class PlainEventListProxyHandler
    extends EventListExpressionProxyHandler<PlainEventListProxy>
  {
    //#######################################################################
    //# Constructor
    private PlainEventListProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<PlainEventListProxy>
    @Override
    PlainEventListProxy getResult()
    {
      final List<Proxy> eventList = getEventList();
      return mFactory.createPlainEventListProxy(eventList);
    }
  }


  //#########################################################################
  //# Inner Class PointGeometryProxyHandler
  private class PointGeometryProxyHandler
    extends AbstractContentHandler<PointGeometryProxy>
  {
    //#######################################################################
    //# Constructor
    private PointGeometryProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<PointGeometryProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Point)) {
        mPointHandler = new PointHandler(this);
        pushHandler(mPointHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    PointGeometryProxy getResult()
    {
      final Point point = mPointHandler.getResult();
      return mFactory.createPointGeometryProxy(point);
    }

    //#######################################################################
    //# Data Members
    private PointHandler mPointHandler = null;
  }


  //#########################################################################
  //# Inner Class PointHandler
  private class PointHandler
    extends AbstractContentHandler<Point>
  {
    //#######################################################################
    //# Constructor
    private PointHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<Point>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaModule.ATTRIB_X)) {
        mX = Integer.parseInt(value);
      } else if (localName.equals(SchemaModule.ATTRIB_Y)) {
        mY = Integer.parseInt(value);
      }
    }

    @Override
    Point getResult()
    {
      return new Point(mX, mY);
    }

    //#######################################################################
    //# Data Members
    private int mX;
    private int mY;
  }


  //#########################################################################
  //# Inner Class QualifiedIdentifierProxyHandler
  private class QualifiedIdentifierProxyHandler
    extends IdentifierProxyHandler<QualifiedIdentifierProxy>
  {
    //#######################################################################
    //# Constructor
    private QualifiedIdentifierProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<QualifiedIdentifierProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      final AbstractContentHandler<?> handler = pushHandler(localName, atts);
      if (handler instanceof IdentifierProxyHandler) {
        if (mBaseHandler == null) {
          mBaseHandler = (IdentifierProxyHandler<?>) handler;
        } else if (mComponentHandler == null) {
          mComponentHandler = (IdentifierProxyHandler<?>) handler;
        }
      }
    }

    @Override
    QualifiedIdentifierProxy getResult()
      throws SAXParseException
    {
      final String text = getText();
      final IdentifierProxy base = mBaseHandler.getResult();
      final IdentifierProxy comp = mComponentHandler.getResult();
      return mFactory.createQualifiedIdentifierProxy(text, base, comp);
    }


    //#######################################################################
    //# Data Members
    private IdentifierProxyHandler<?> mBaseHandler = null;
    private IdentifierProxyHandler<?> mComponentHandler = null;
  }


  //#########################################################################
  //# Inner Class SimpleComponentProxyHandler
  private class SimpleComponentProxyHandler
    extends IdentifiedProxyHandler<SimpleComponentProxy>
  {
    //#######################################################################
    //# Constructor
    private SimpleComponentProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<SimpleComponentProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Kind)) {
        mKind = Enum.valueOf(ComponentKind.class, value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Graph)) {
        mGraphProxyHandler = new GraphProxyHandler(this);
        pushHandler(mGraphProxyHandler, atts);
      } else if (localName.equals(SchemaBase.ELEMENT_AttributeMap)) {
        mAttributeMapHandler = new AttributeMapHandler(this);
        pushHandler(mAttributeMapHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    SimpleComponentProxy getResult() throws SAXParseException
    {
      final IdentifierProxy ident = getIdentifier();
      final GraphProxy graph = mGraphProxyHandler.getResult();
      final Map<String,String> attribs =
        mAttributeMapHandler == null ? null : mAttributeMapHandler.getResult();
      return mFactory.createSimpleComponentProxy(ident, mKind, graph, attribs);
    }

    //#######################################################################
    //# Data Members
    private ComponentKind mKind = null;
    private GraphProxyHandler mGraphProxyHandler = null;
    private AttributeMapHandler mAttributeMapHandler = null;
  }


  //#########################################################################
  //# Inner Class SimpleExpressionProxyHandler
  private abstract class SimpleExpressionProxyHandler
    <P extends SimpleExpressionProxy>
    extends AbstractContentHandler<P>
  {
    //#######################################################################
    //# Constructor
    private SimpleExpressionProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Text) && isImportingGeometry()) {
        mText = value;
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    void reset()
    {
      mText = null;
    }

    //#######################################################################
    //# Parsing Support
    String getText()
    {
      return mText;
    }

    //#######################################################################
    //# Data Members
    private String mText = null;
  }


  //#########################################################################
  //# Inner Class SimpleIdentifierProxyHandler
  private class SimpleIdentifierProxyHandler
    extends IdentifierProxyHandler<SimpleIdentifierProxy>
  {
    //#######################################################################
    //# Constructor
    private SimpleIdentifierProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<SimpleIdentifierProxy>
    @Override
    SimpleIdentifierProxy getResult()
    {
      final String text = getText();
      final String name = getName();
      return mFactory.createSimpleIdentifierProxy(text, name);
    }
  }


  //#########################################################################
  //# Inner Class SimpleNodeProxyHandler
  private class SimpleNodeProxyHandler
    extends NodeProxyHandler<SimpleNodeProxy>
  {
    //#######################################################################
    //# Constructor
    private SimpleNodeProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<SimpleNodeProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Initial)) {
        mInitial = Boolean.parseBoolean(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (isImportingGeometry()) {
        if (localName.equals(SchemaModule.ELEMENT_PointGeometry)) {
          mPointGeometryHandler = new PointGeometryProxyHandler(this);
          pushHandler(mPointGeometryHandler, atts);
        } else if (localName.equals(SchemaModule.ELEMENT_InitialArrowGeometry)) {
          mInitialGeometryHandler = new PointGeometryProxyHandler(this);
          pushHandler(mInitialGeometryHandler, atts);
        } else if (localName.equals(SchemaModule.ELEMENT_LabelGeometry)) {
          mLabelGeometryHandler = new LabelGeometryProxyHandler(this);
          pushHandler(mLabelGeometryHandler, atts);
        } else {
          super.startSubElement(localName, atts);
        }
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    SimpleNodeProxy getResult()
    {
      final String name = getName();
      final PlainEventListProxy props = getEventList();
      final Map<String,String> attribs = getAttributeMap();
      final PointGeometryProxy pointGeo = mPointGeometryHandler == null ?
        null : mPointGeometryHandler.getResult();
      final PointGeometryProxy initialGeo = mInitialGeometryHandler == null ?
        null : mInitialGeometryHandler.getResult();
      final LabelGeometryProxy labelGeo = mLabelGeometryHandler == null ?
        null : mLabelGeometryHandler.getResult();
      final SimpleNodeProxy node =
        mFactory.createSimpleNodeProxy(name, props, attribs, mInitial,
                                       pointGeo, initialGeo, labelGeo);
      return node;
    }

    //#######################################################################
    //# Data Members
    private boolean mInitial;
    private PointGeometryProxyHandler mPointGeometryHandler = null;
    private PointGeometryProxyHandler mInitialGeometryHandler = null;
    private LabelGeometryProxyHandler mLabelGeometryHandler = null;
  }


  //#########################################################################
  //# Inner Class SplineGeometryProxyHandler
  private class SplineGeometryProxyHandler
    extends AbstractContentHandler<SplineGeometryProxy>
  {
    //#######################################################################
    //# Constructor
    private SplineGeometryProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mPointHandler = new PointHandler(this);
      mPoints = new ArrayList<>(1);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ColorGeometryProxy>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaModule.ATTRIB_Kind)) {
        mKind = Enum.valueOf(SplineKind.class, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_Point)) {
        pushHandler(mPointHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
    {
      if (subHandler == mPointHandler) {
        final Point point = mPointHandler.getResult();
        mPoints.add(point);
      }
    }

    @Override
    SplineGeometryProxy getResult()
    {
      return mFactory.createSplineGeometryProxy(mPoints, mKind);
    }

    //#######################################################################
    //# Data Members
    private final PointHandler mPointHandler;
    private final List<Point> mPoints;
    private SplineKind mKind = SchemaModule.DEFAULT_SplineKind;
  }


  //#########################################################################
  //# Inner Class UnaryExpressionProxyHandler
  private class UnaryExpressionProxyHandler
    extends SimpleExpressionProxyHandler<UnaryExpressionProxy>
  {
    //#######################################################################
    //# Constructor
    private UnaryExpressionProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<UnaryExpressionProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Operator)) {
        mOperator = mOperatorTable.getUnaryOperator(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                             final Attributes atts)
      throws SAXParseException
    {
      final AbstractContentHandler<?> handler = pushHandler(localName, atts);
      if (handler instanceof SimpleExpressionProxyHandler &&
          mSubTermHandler == null) {
        mSubTermHandler = (SimpleExpressionProxyHandler<?>) handler;
      }
    }

    @Override
    UnaryExpressionProxy getResult() throws SAXParseException
    {
      final String text = getText();
      final SimpleExpressionProxy lhs = mSubTermHandler.getResult();
      return mFactory.createUnaryExpressionProxy(text, mOperator, lhs);
    }

    //#######################################################################
    //# Data Members
    private UnaryOperator mOperator;
    private SimpleExpressionProxyHandler<?> mSubTermHandler;
  }


  //#########################################################################
  //# Inner Class UnpackingEventListProxyHandler
  private class UnpackingEventListProxyHandler
    extends AbstractContentHandler<ExpressionProxy>
  {
    //#######################################################################
    //# Constructor
    private UnpackingEventListProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<PlainEventListProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ATTRIB_Unpack)) {
        mUnpackFlag = Boolean.parseBoolean(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                             final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_EventList)) {
        mEventListHandler = new GenericListHandler<>(this, Proxy.class);
        pushHandler(mEventListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    ExpressionProxy getResult()
      throws SAXParseException
    {
      final List<Proxy> eventList = mEventListHandler.getResult();
      if (mUnpackFlag && eventList.size() == 1) {
        final Proxy first = eventList.get(0);
        try {
          return (ExpressionProxy) first;
        } catch (final ClassCastException exception) {
          throw createTypeMismatchException
            (first, ExpressionProxy.class, exception);
        }
      } else {
        return mFactory.createPlainEventListProxy(eventList);
      }
    }

    //#######################################################################
    //# Data Members
    private ListHandler<Proxy> mEventListHandler = null;
    private boolean mUnpackFlag = SchemaModule.DEFAULT_Unpack;
  }


  //#########################################################################
  //# Inner Class VariableComponentProxyHandler
  private class VariableComponentProxyHandler
    extends IdentifiedProxyHandler<VariableComponentProxy>
  {
    //#######################################################################
    //# Constructor
    private VariableComponentProxyHandler
      (final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<VariableComponentProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaModule.ELEMENT_VariableRange)) {
        mRangeHandler =
          new SingletonHandler<>(this, SimpleExpressionProxy.class);
        pushHandler(mRangeHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_VariableInitial)) {
        mInitHandler =
          new SingletonHandler<>(this, SimpleExpressionProxy.class);
        pushHandler(mInitHandler);
      } else if (localName.equals(SchemaModule.ELEMENT_VariableMarking)) {
        if (mMarkingHandler == null) {
          mMarkingHandler = new VariableMarkingProxyHandler(this);
          mMarkings = new ArrayList<>(1);
        }
        pushHandler(mMarkingHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mMarkingHandler) {
        final VariableMarkingProxy marking = mMarkingHandler.getResult();
        mMarkings.add(marking);
        mMarkingHandler.reset();
      } else {
        super.endSubElement(subHandler);
      }
    }

    @Override
    VariableComponentProxy getResult()
    {
      final IdentifierProxy ident = getIdentifier();
      final SimpleExpressionProxy range = mRangeHandler.getResult();
      final SimpleExpressionProxy init = mInitHandler.getResult();
      return mFactory.createVariableComponentProxy(ident, range,
                                                   init, mMarkings);
    }

    //#######################################################################
    //# Data Members
    private SingletonHandler<SimpleExpressionProxy> mRangeHandler;
    private SingletonHandler<SimpleExpressionProxy> mInitHandler;
    private VariableMarkingProxyHandler mMarkingHandler = null;
    private List<VariableMarkingProxy> mMarkings = null;
  }


  //#########################################################################
  //# Inner Class VariableMarkingProxyHandler
  private class VariableMarkingProxyHandler
    extends AbstractContentHandler<VariableMarkingProxy>
  {
    //#######################################################################
    //# Constructor
    private VariableMarkingProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<VariableMarkingProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      final AbstractContentHandler<?> handler = pushHandler(localName, atts);
      if (mIdentifierHandler == null &&
          handler instanceof IdentifierProxyHandler) {
        mIdentifierHandler = (IdentifierProxyHandler<?>) handler;
      } else if (mExpressionHandler == null &&
                 handler instanceof SimpleExpressionProxyHandler) {
        mExpressionHandler = (SimpleExpressionProxyHandler<?>) handler;
      }
    }

    @Override
    VariableMarkingProxy getResult()
      throws SAXParseException
    {
      final IdentifierProxy ident = mIdentifierHandler.getResult();
      final SimpleExpressionProxy expr = mExpressionHandler.getResult();
      return mFactory.createVariableMarkingProxy(ident, expr);
    }

    @Override
    void reset()
    {
      mIdentifierHandler = null;
      mExpressionHandler = null;
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxyHandler<?> mIdentifierHandler = null;
    private SimpleExpressionProxyHandler<?> mExpressionHandler = null;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final OperatorTable mOperatorTable;

}
