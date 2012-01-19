//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ProxyShapeProducer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
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
import net.sourceforge.waters.subject.module.GeometryTools;


public class ProxyShapeProducer
  extends AbstractModuleProxyVisitor
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
    mPrinter = new ModuleProxyPrinter();
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

  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return createLabelBlockShape(block, null);
  }

  public GroupNodeProxyShape visitGroupNodeProxy(final GroupNodeProxy group)
  {
    GroupNodeProxyShape shape = (GroupNodeProxyShape) lookup(group);
    if (shape == null) {
      shape = new GroupNodeProxyShape(group);
      mMap.put(group, shape);
    }
    return shape;
  }

  public SimpleNodeProxyShape visitSimpleNodeProxy(final SimpleNodeProxy simple)
  {
    final SimpleNodeProxyShape shape = createSimpleNodeProxyShape(simple);
    final LabelGeometryProxy geo = simple.getLabelGeometry();
    createNodeLabelProxyShape(geo, simple);
    return shape;
  }

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

  LabelProxyShape createNodeLabelProxyShape(final LabelGeometryProxy geo,
                                            final SimpleNodeProxy simple)
  {
    LabelProxyShape shape = (LabelProxyShape) lookup(geo);
    if (shape == null) {
      shape = new LabelProxyShape(simple, EditorColor.DEFAULT_FONT);
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

      createListShape(block.getEventList(), lx, y, 0);

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

  private double mHeight;
  private double mWidth;

  private void createListShape(final List<Proxy> list, final int x,
                               final double y, final int indent)
  {
    for (final Proxy proxy : list) {
      final int ly = (int) Math.round(y + mHeight);
      Font font = EditorColor.DEFAULT_FONT;
      LabelShape lshape = null;
      if (proxy instanceof IdentifierProxy) {
        final IdentifierProxy ident = (IdentifierProxy) proxy;
        font = mRenderingContext.getFont(ident);
        lshape = createEdgeLabelShape(proxy, x + indent, ly, font);
        mMap.put(proxy, lshape);
        adjustHeightAndWidth(lshape, indent);
      } else if (proxy instanceof ForeachProxy) {
        final ForeachProxy foreach = (ForeachProxy) proxy;
        lshape = createForeachLabelShape(foreach, x + indent, ly);
        mMap.put(proxy, lshape);
        adjustHeightAndWidth(lshape, indent);
        createListShape(foreach.getBody(), x, y, indent + 10);
      }
    }
  }

  private void adjustHeightAndWidth(final LabelShape shape, final int indent)
  {
    final RoundRectangle2D lrect = shape.getShape();
    mHeight += lrect.getHeight();
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
      final List<SimpleExpressionProxy> guards = block.getGuards();
      if(guards.size() == 3)
      {
          for (int i = 1;i<guards.size();i++)
          {
            final SimpleExpressionProxy guard = guards.get(i);
            final int ly = (int) Math.round(y + height);
            final LabelShape lshape =
              new LabelShape(guard, lx, ly, EditorColor.DEFAULT_FONT);
            mMap.put(guard, lshape);
            final RoundRectangle2D lrect = lshape.getShape();
            height += lrect.getHeight();
            if (width < lrect.getWidth()) {
              width = lrect.getWidth();
            }
          }
      }
      else if(guards.size() == 1 || guards.size() == 2)
      {
            final SimpleExpressionProxy guard = guards.get(0);
            final int ly = (int) Math.round(y + height);
            final LabelShape lshape =
              new LabelShape(guard, lx, ly, EditorColor.DEFAULT_FONT);
            mMap.put(guard, lshape);
            final RoundRectangle2D lrect = lshape.getShape();
            height += lrect.getHeight();
            if (width < lrect.getWidth()) {
              width = lrect.getWidth();
            }
      }
      if (!guards.isEmpty()) {
        height += 2;
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
    final String text = mPrinter.toString(shown);
    return new LabelShape(label, x, y, font, text);
  }

  private LabelShape createForeachLabelShape(final ForeachProxy foreach,
                                             final int x, final int y){
    final Font font = EditorColor.DEFAULT_FONT;
    final StringWriter stringWriter = new StringWriter();

    final ModuleProxyPrinter proxyPrinter = new ModuleProxyPrinter(stringWriter);
    try {
      proxyPrinter.pprint("FOR ");
      proxyPrinter.pprint(foreach.getName());
      proxyPrinter.pprint(" IN ");
      proxyPrinter.pprint(foreach.getRange());
      if(foreach.getGuard() != null){
        proxyPrinter.pprint(" WHERE ");
        proxyPrinter.pprint(foreach.getGuard());
      }

    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
    final String text = stringWriter.toString();
    return new LabelShape(foreach, x, y, font, text);
  }



  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final RenderingContext mRenderingContext;
  private final Map<Proxy,ProxyShape> mMap;
  private final ModuleProxyPrinter mPrinter;
  private final SimpleExpressionCompiler mCompiler;
  private final BindingContext mBindings;


  //#########################################################################
  //# Class Constants
  private static final String BLOCKED_HEADER = "BLOCKED:";
  private static final double BORDER = 5;
}
