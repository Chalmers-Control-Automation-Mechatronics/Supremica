//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import net.sourceforge.waters.model.module.ConditionalProxy;
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
    createNodeLabelShape(simple);
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
        mRenderingContext.getMarkingColorInfo(mGraph, simple);
      shape = new SimpleNodeProxyShape(simple, colorinfo);
      mMap.put(simple, shape);
    }
    return shape;
  }

  LabelShape createNodeLabelShape(final SimpleNodeProxy node)
  {
    final LabelGeometryProxy geo = node.getLabelGeometry();
    LabelShape shape = (LabelShape) lookup(geo);
    if (shape == null) {
      final Point2D nodePos = node.getPointGeometry().getPoint();
      final Point2D offset =
        geo == null ? LabelShape.DEFAULT_NODE_LABEL_OFFSET : geo.getOffset();
      final double x = nodePos.getX() + offset.getX();
      final double y = nodePos.getY() + offset.getY();
      shape = new LabelShape(node, x, y, EditorColor.DEFAULT_FONT);
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
    (final LabelBlockProxy block, final EdgeProxyShape edgeShape)
  {
    LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape == null) {
      final FontRenderContext context = new FontRenderContext(null, true, true);
      final Font font = EditorColor.DEFAULT_FONT;
      final Rectangle2D charBounds = font.getMaxCharBounds(context);
      final double baseLineSkip = charBounds.getHeight();
      double x0, y0;
      final LabelGeometryProxy geo = block.getGeometry();
      if (geo == null) {
        x0 = LabelBlockProxyShape.DEFAULT_OFFSET_X;
        y0 = LabelBlockProxyShape.DEFAULT_OFFSET_Y;
      } else {
        final Point2D offset = geo.getOffset();
        x0 = offset.getX();
        y0 = offset.getY();
      }
      if (edgeShape != null) {
        final Point2D turn = edgeShape.getTurningPoint();
        x0 += turn.getX();
        y0 += turn.getY();
      }
      x0 += LabelBlockProxyShape.INSETS;
      y0 += LabelBlockProxyShape.INSETS;
      final double x = x0;
      final double y = y0;
      mMaxY = y;
      mMaxX = x;
      createListShape(block.getEventIdentifierList(), x, y, baseLineSkip);
      final double width = mMaxX - x0;
      final double height = mMaxY - y0;
      final Rectangle2D textBounds =
        new Rectangle2D.Double(x0, y0, width, height);
      if (edgeShape != null) {
        shape = new LabelBlockProxyShape(block, textBounds);
      } else {
        // Oh no! It's the blocked events list!
        shape = TitledLabelBlockProxyShape.createShape
          (block, textBounds,
           TitledLabelBlockProxyShape.BLOCKED_HEADER,
           EditorColor.DEFAULT_FONT, baseLineSkip);
      }
      mMap.put(block, shape);
    }
    return shape;
  }

  private double createListShape(final List<Proxy> list,
                                 final double x,
                                 double y,
                                 final double baseLineSkip)
  {
    for (final Proxy proxy : list) {
      LabelShape shape = null;
      if (proxy instanceof IdentifierProxy) {
        final IdentifierProxy ident = (IdentifierProxy) proxy;
        shape = createEdgeLabelShape(ident, x, y, EditorColor.DEFAULT_FONT);
        mMap.put(proxy, shape);
        y += baseLineSkip;
      } else if (proxy instanceof ConditionalProxy) {
        final ConditionalProxy cond = (ConditionalProxy) proxy;
        shape = createConditionalLabelShape(cond, x, y);
        mMap.put(proxy, shape);
        y += baseLineSkip;
        y = createListShape(cond.getBody(),
                            x + LabelBlockProxyShape.INDENTATION, y,
                            baseLineSkip);
      } else if (proxy instanceof ForeachProxy) {
        final ForeachProxy foreach = (ForeachProxy) proxy;
        shape = createForeachLabelShape(foreach, x, y);
        mMap.put(proxy, shape);
        y += baseLineSkip;
        y = createListShape(foreach.getBody(),
                            x + LabelBlockProxyShape.INDENTATION, y,
                            baseLineSkip);
      }
      final Rectangle2D bounds = shape.getBounds2D();
      mMaxX = Math.max(mMaxX, bounds.getMaxX());
      mMaxY = Math.max(mMaxY, bounds.getMaxY());
    }
    return y;
  }


  private LabelShape createConditionalLabelShape
    (final ConditionalProxy cond, final double x, final double y)
  {
    return new LabelShape(cond, x, y, EditorColor.DEFAULT_FONT);
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
        x += GuardActionBlockProxyShape.DEFAULT_OFFSET_X;
        y += GuardActionBlockProxyShape.DEFAULT_OFFSET_Y;
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
          final Rectangle2D rect = lshape.getShape();
          height += rect.getHeight();
          if (width < rect.getWidth()) {
            width = rect.getWidth();
          }
        }
      }
      final List<BinaryExpressionProxy> actions = block.getActions();
      for (final BinaryExpressionProxy action : actions) {
        final int ly = (int) Math.round(y + height);
        final LabelShape lshape =
          createEdgeLabelShape(action, lx, ly, EditorColor.DEFAULT_FONT);
        mMap.put(action, lshape);
        final Rectangle2D rect = lshape.getShape();
        height += rect.getHeight();
        if (width < rect.getWidth()) {
          width = rect.getWidth();
        }
      }
      final RoundRectangle2D bounds =
        new RoundRectangle2D.Double(x, y, width, height,
                                    GuardActionBlockProxyShape.DEFAULT_ARC_WIDTH,
                                    GuardActionBlockProxyShape.DEFAULT_ARC_HEIGHT);
      shape = new GuardActionBlockProxyShape(block, bounds);
      mMap.put(block, shape);
    }
    return shape;
  }

  private LabelShape createEdgeLabelShape(final SimpleExpressionProxy expr,
                                                    final double x,
                                                    final double y,
                                                    final Font font)
  {
    SimpleExpressionProxy shown = expr;
    if (mBindings != null) {
      try {
        shown = mCompiler.simplify(expr, mBindings);
      } catch (final EvalException exception) {
        // OK, use the original label
      }
    }
    return new LabelShape(shown, x, y, font);
  }

  private LabelShape createForeachLabelShape
    (final ForeachProxy foreach, final double x, final double y)
  {
    return new LabelShape(foreach, x, y, EditorColor.DEFAULT_FONT);
  }


  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final RenderingContext mRenderingContext;
  private final Map<Proxy,ProxyShape> mMap;
  private final SimpleExpressionCompiler mCompiler;
  private final BindingContext mBindings;
  private double mMaxX;
  private double mMaxY;


  //#########################################################################
  //# Class Constants
  private static final double BORDER = 5;
}
