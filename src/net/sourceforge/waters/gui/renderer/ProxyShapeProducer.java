//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.module.GeometryTools;


public class ProxyShapeProducer
  extends DefaultModuleProxyVisitor
{
  //#########################################################################
  //# Constructors
  public ProxyShapeProducer(final GraphProxy graph,
                            final RenderingContext context)
  {
    this(graph, context, null, null);
  }

  public ProxyShapeProducer(final GraphProxy graph,
                            final RenderingContext context,
                            final SimpleExpressionCompiler compiler,
                            final BindingContext binding)
  {
    mGraph = graph;
    mRenderingContext = context;
    final int size = graph.getNodes().size() + graph.getEdges().size();
    final Map<Proxy,ProxyShape> map = new HashMap<Proxy,ProxyShape>(4 * size);
    mMap = Collections.synchronizedMap(map);
    mCompiler = compiler;
    mBindings = binding;
  }


  //##########################################################################
  //# Clean up
  public void close()
  {
  }

  public void clear()
  {
    mMap.clear();
  }


  //#########################################################################
  //# Simple Access
  public GraphProxy getGraph()
  {
    return mGraph;
  }

  public RenderingContext getRenderingContext()
  {
    return mRenderingContext;
  }


  //#########################################################################
  //# Invocation
  public ProxyShape getShape(final Proxy proxy)
  {
    if (proxy != null) {
      try {
        return (ProxyShape) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    } else {
      return null;
    }
  }

  public void createAllShapes()
  {
    getShape(mGraph.getBlockedEvents());
    for (final NodeProxy node : mGraph.getNodes()) {
      getShape(node);
    }
    for (final EdgeProxy edge : mGraph.getEdges()) {
      getShape(edge);
    }
  }

  /**
   * Calculates a minimum bounding rectangle of all shapes displayed.
   */
  public Rectangle2D getMinimumBoundingRectangle()
  {
    createAllShapes();
    synchronized (mMap) {
      if (mMap.isEmpty()) {
        return new Rectangle(0, 0, 0, 0);
      } else {
        final Iterator<ProxyShape> iter = mMap.values().iterator();
        final ProxyShape shape0 = iter.next();
        final Rectangle2D rect = shape0.getBounds2D();
        while (iter.hasNext()) {
          final ProxyShape shape = iter.next();
          rect.add(shape.getBounds2D());
        }
        final Rectangle output =
          new Rectangle((int)(rect.getX() - BORDER),
                        (int)(rect.getY() - BORDER),
                        (int)(rect.getWidth() + BORDER * 2),
                        (int)(rect.getHeight() + BORDER * 2));
        return output;
      }
    }
  }

  /**
   * Calculates a minimum bounding rectangle for the given items.
   */
  public Rectangle2D getMinimumBoundingRectangle
    (final Collection<? extends Proxy> proxies)
  {
    if (proxies.isEmpty()) {
      return new Rectangle(0, 0, 0, 0);
    } else {
      final Iterator<? extends Proxy> iter = proxies.iterator();
      final Proxy proxy0 = iter.next();
      final ProxyShape shape0 = getShape(proxy0);
      final Rectangle2D rect = shape0.getBounds2D();
      while (iter.hasNext()) {
        final Proxy proxy = iter.next();
        final ProxyShape shape = getShape(proxy);
        rect.add(shape.getBounds2D());
      }
      return rect;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public EdgeProxyShape visitEdgeProxy(final EdgeProxy edge)
  {
    final EdgeProxyShape shape = createEdgeProxyShape(edge);
    createLabelBlockShape(edge.getLabelBlock(), shape);
    final GuardActionBlockProxy block = edge.getGuardActionBlock();
    if (block != null) {
      createGuardActionBlockShape(block, shape);
    }
    return shape;
  }

  @Override
  public Object visitGeometryProxy(final GeometryProxy proxy)
    throws VisitorException
  {
    final GeometrySubject geo = (GeometrySubject)proxy;
    final Proxy parent = (Proxy) geo.getParent();
    return parent.acceptVisitor(this);
  }

  @Override
  public Object visitLabelGeometryProxy(final LabelGeometryProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return createLabelBlockShape(block, null);
  }

  @Override
  public GroupNodeProxyShape visitGroupNodeProxy(final GroupNodeProxy group)
  {
    GroupNodeProxyShape shape = (GroupNodeProxyShape) lookup(group);
    if (shape == null) {
      shape = new GroupNodeProxyShape(group);
      mMap.put(group, shape);
    }
    return shape;
  }

  @Override
  public SimpleNodeProxyShape visitSimpleNodeProxy(final SimpleNodeProxy simple)
  {
    final SimpleNodeProxyShape shape = createSimpleNodeProxyShape(simple);
    final LabelGeometryProxy geo = simple.getLabelGeometry();
    createNodeLabelShape(geo, simple);
    return shape;
  }

  @Override
  public ProxyShape visitProxy(final Proxy proxy)
    throws VisitorException
  {
    final ProxyShape shape = lookup(proxy);
    if (shape == null) {
      throw new VisitorException(proxy + " is not in the map!");
    }
    return shape;
  }


  //#########################################################################
  //# Static Methods
  /**
   * Gets the list of the guards in a {@link GuardActionBlockProxy}
   * that are actually displayed. Only one or two guards are displayed,
   * in an attempt to support 'seamless synthesis'.
   */
  public static List<SimpleExpressionProxy>
    getDisplayedGuards(final GuardActionBlockProxy block)
  {
    final List<SimpleExpressionProxy> guards = block.getGuards();
    switch (guards.size()) {
    case 0:
      return Collections.emptyList();
    case 1:
    case 2:
      return Collections.singletonList(guards.get(0));
    default:
      final List<SimpleExpressionProxy> list = new ArrayList<>(2);
      list.add(guards.get(1));
      list.add(guards.get(2));
      return list;
    }
  }


  //#########################################################################
  //# Auxiliary Access
  ProxyShape lookup(final Proxy proxy)
  {
    return mMap.get(proxy);
  }

  void unmap(final Proxy proxy)
  {
    mMap.remove(proxy);
  }


  //#########################################################################
  //# Auxiliary Methods
  SimpleNodeProxyShape createSimpleNodeProxyShape(final SimpleNodeProxy simple)
  {
    SimpleNodeProxyShape shape = (SimpleNodeProxyShape) lookup(simple);
    if (shape == null) {
      final PropositionIcon.ColorInfo colorinfo =
        mRenderingContext.getColorInfo(mGraph, simple);
      shape = new SimpleNodeProxyShape(simple, colorinfo);
      mMap.put(simple, shape);
    }
    return shape;
  }

  LabelShape createNodeLabelShape(final LabelGeometryProxy geo,
                                  final SimpleNodeProxy simple)
  {
    LabelShape shape = (LabelShape) lookup(geo);
    if (shape == null) {
      shape = new LabelShape(simple, EditorColor.DEFAULT_FONT);
      mMap.put(geo, shape);
    }
    return shape;
  }

  EdgeProxyShape createEdgeProxyShape(final EdgeProxy edge)
  {
    EdgeProxyShape shape = (EdgeProxyShape) lookup(edge);
    if (shape == null) {
      final SplineGeometryProxy geo = edge.getGeometry();
      if (GeometryTools.isSelfloop(edge)) {
        shape = new TieEdgeProxyShape(edge);
      } else if (geo == null) {
        shape = new StraightEdgeProxyShape(edge);
      } else {
        switch (geo.getPoints().size()) {
        case 0:
          shape = new StraightEdgeProxyShape(edge);
          break;
        case 1:
          shape = new QuadraticEdgeProxyShape(edge);
          break;
        case 2:
          shape = new CubicEdgeProxyShape(edge);
          break;
        default:
          throw new IllegalArgumentException
            ("Unsupported number of control points in spline!");
        }
      }
      mMap.put(edge, shape);
    }
    return shape;
  }

  LabelBlockProxyShape createLabelBlockShape
    (final LabelBlockProxy block,
     final EdgeProxyShape eshape)
  {
    LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape == null) {
      mWidth = 0;
      mHeight = 1;
      double x;
      double y;
      final LabelGeometryProxy geo = block.getGeometry();
      if (geo == null) {
        x = LabelBlockProxyShape.DEFAULT_OFFSET_X;
        y = LabelBlockProxyShape.DEFAULT_OFFSET_Y;
      } else {
        final Point2D offset = geo.getOffset();
        x = offset.getX();
        y = offset.getY();
      }
      if (eshape != null) {
        final Point2D turn = eshape.getTurningPoint();
        x += turn.getX();
        y += turn.getY();
      } else {
        // Oh no! It's the blocked events list!
        final TextLayout text =
          new TextLayout(BLOCKED_HEADER, EditorColor.HEADER_FONT,
                         new FontRenderContext(null, true, true));
        mWidth = (int) text.getBounds().getWidth();
      }
      final int lx = (int) Math.round(x) + 1;

      createListShape(block.getEventIdentifierList(), lx, y, 0);

      mHeight += 4;
      mWidth += 3;
      final RoundRectangle2D bounds =
        new RoundRectangle2D.Double(x, y, mWidth, mHeight,
                                    LabelBlockProxyShape.DEFAULTARCW,
                                    LabelBlockProxyShape.DEFAULTARCH);
      if (eshape != null) {
        shape = new LabelBlockProxyShape(block, bounds);
      } else {
        shape = new TitledLabelBlockProxyShape
          (block, bounds, BLOCKED_HEADER, EditorColor.HEADER_FONT);
      }
      mMap.put(block, shape);
    }
    return shape;
  }

  private void createListShape(final List<Proxy> list, final int x,
                               final double y, final int indent)
  {
    for (final Proxy proxy : list) {
      final int ly = (int) Math.round(y + mHeight);
      Font font = EditorColor.DEFAULT_FONT;
      mMaxBounds = font.getMaxCharBounds(new FontRenderContext(null, true, true));
      AbstractLabelShape lshape = null;
      if (proxy instanceof IdentifierProxy) {
        final IdentifierProxy ident = (IdentifierProxy) proxy;
        font = mRenderingContext.getFont(ident);
        mMaxBounds = font.getMaxCharBounds(new FontRenderContext(null, true, true));
        lshape = createEdgeLabelShape(proxy, x + indent, ly, font);
        mMap.put(proxy, lshape);
        adjustRect(lshape, indent);
      } else if (proxy instanceof ForeachProxy) {
        final ForeachProxy foreach = (ForeachProxy) proxy;
        lshape = createForeachLabelShape(foreach, x + indent, ly);
        mMap.put(proxy, lshape);
        adjustRect(lshape, indent);
        createListShape(foreach.getBody(), x, y, indent + 10);
      }

    }
  }

  private Rectangle2D mMaxBounds;
  private double mMaxHeight = 0;

  private void adjustRect(final AbstractLabelShape shape, final int indent)
  {
    if(mMaxHeight < mMaxBounds.getHeight()){
      mMaxHeight = mMaxBounds.getHeight();
    }
    final RoundRectangle2D lrect = shape.getShape();
    lrect.setFrame(lrect.getX(), lrect.getY(), lrect.getWidth(), mMaxHeight);
    mHeight += mMaxHeight;
    if (mWidth < lrect.getWidth() + indent) {
      mWidth = (int) lrect.getWidth() + indent;
    }
  }


  GuardActionBlockProxyShape createGuardActionBlockShape
    (final GuardActionBlockProxy block,
     final EdgeProxyShape eshape)
  {
    GuardActionBlockProxyShape shape =
      (GuardActionBlockProxyShape) lookup(block);
    if (shape == null) {
      final Point2D turn = eshape.getTurningPoint();
      double x = turn.getX();
      double y = turn.getY();
      final LabelGeometryProxy geo = block.getGeometry();
      if (geo == null) {
        x += GuardActionBlockProxyShape.DEFAULTOFFSETX;
        y += GuardActionBlockProxyShape.DEFAULTOFFSETY;
      } else {
        final Point2D offset = geo.getOffset();
        x += offset.getX();
        y += offset.getY();
      }
      final int lx = (int) Math.round(x);
      double width = 0.0;
      double height = 2.0;

      //Create a rectangle for only those guards that will be displayed ultimately.
      final List<SimpleExpressionProxy> guards = getDisplayedGuards(block);
      if (guards.isEmpty()) {
        height += 2;
      } else {
        for (final SimpleExpressionProxy guard : guards) {
          final int ly = (int) Math.round(y + height);
          final LabelShape lshape =
            createEdgeLabelShape(guard, lx, ly, EditorColor.DEFAULT_FONT);
          mMap.put(guard, lshape);
          final RoundRectangle2D lrect = lshape.getShape();
          height += lrect.getHeight();
          if (width < lrect.getWidth()) {
            width = lrect.getWidth();
          }
        }
      }
      final List<BinaryExpressionProxy> actions = block.getActions();
      for (final BinaryExpressionProxy action : actions) {
        final int ly = (int) Math.round(y + height);
        final LabelShape lshape =
          createEdgeLabelShape(action, lx, ly, EditorColor.DEFAULT_FONT);
        mMap.put(action, lshape);
        final RoundRectangle2D lrect = lshape.getShape();
        height += lrect.getHeight();
        if (width < lrect.getWidth()) {
          width = lrect.getWidth();
        }
      }
      final RoundRectangle2D bounds =
        new RoundRectangle2D.Double(x, y, width, height,
                                    GuardActionBlockProxyShape.DEFAULTARCW,
                                    GuardActionBlockProxyShape.DEFAULTARCH);
      shape = new GuardActionBlockProxyShape(block, bounds);
      mMap.put(block, shape);
    }
    return shape;
  }

  private LabelShape createEdgeLabelShape(final Proxy label,
                                          final int x, final int y,
                                          final Font font)
  {
    Proxy shown = label;
    if (mBindings != null && label instanceof SimpleExpressionProxy) {
      final SimpleExpressionProxy expr = (SimpleExpressionProxy) label;
      try {
        shown = mCompiler.simplify(expr, mBindings);
      } catch (final EvalException exception) {
        // OK, use the original label
      }
    }
    final String text = ModuleProxyPrinter.getPrintString(shown);
    return new LabelShape(label, x, y, font, text);
  }

  private AbstractLabelShape createForeachLabelShape(final ForeachProxy foreach,
                                                     final int x, final int y)
  {
    final Font keyword = EditorColor.DEFAULT_FONT.deriveFont(Font.BOLD);
    final ForeachLabelShapeBuilder builder =
      new ForeachLabelShapeBuilder(EditorColor.DEFAULT_FONT);
    builder.add(keyword, "FOR");
    builder.add(" " + foreach.getName() + " ");
    builder.add(keyword, "IN");
    builder.add(" ");
    builder.add(foreach.getRange());
    if (foreach.getGuard() != null) {
      builder.add(" ");
      builder.add(keyword, "WHERE");
      builder.add(" ");
      builder.add(foreach.getGuard());
    }
    return builder.create(foreach, x, y, mMap);
  }



  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final RenderingContext mRenderingContext;
  private final Map<Proxy,ProxyShape> mMap;
  private final SimpleExpressionCompiler mCompiler;
  private final BindingContext mBindings;
  private double mHeight;
  private double mWidth;


  //#########################################################################
  //# Class Constants
  private static final String BLOCKED_HEADER = "BLOCKED:";
  private static final double BORDER = 5;
}
