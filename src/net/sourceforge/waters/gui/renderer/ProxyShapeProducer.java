//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ProxyShapeProducer
//###########################################################################
//# $Id: ProxyShapeProducer.java,v 1.22 2007-02-22 08:45:58 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.xsd.base.EventKind;


public class ProxyShapeProducer
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ProxyShapeProducer(final GraphProxy graph,
                            final ModuleProxy module)
  {
    mGraph = graph;
    mModule = module;
    final int size = graph.getNodes().size() + graph.getEdges().size();
    final Map<Proxy,ProxyShape> map = new HashMap<Proxy,ProxyShape>(4 * size);
    mMap = Collections.synchronizedMap(map);
  }

    
  //#########################################################################
  //# Simple Access
  public GraphProxy getGraph()
  {
    return mGraph;
  }


  //#########################################################################
  //# Invocation
  public ProxyShape getShape(final Proxy proxy)
  {
    if (proxy != null) {
      try {
        return (ProxyShape) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
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
   * This method is only accurate if all shapes are cached. If this
   * cannot be guaranteed, {@link #createAllShapes()} should be called
   * first to ensure it.
   */
  public Rectangle getMinimumBoundingRectangle()
  {
    final Rectangle rect = new Rectangle(0, 0, 0, 0);
    synchronized (mMap) {
      for (final ProxyShape shape : mMap.values()) {
        rect.add(shape.getShape().getBounds2D());
      }
    }
    return rect;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public EdgeProxyShape visitEdgeProxy(final EdgeProxy edge)
  {
    EdgeProxyShape shape = (EdgeProxyShape) lookup(edge);
    if (shape == null) {
      if (GeometryTools.isSelfloop(edge)) {
        shape = new TieEdgeProxyShape(edge);
      } else if (edge.getGeometry() == null) {
        shape = new StraightEdgeProxyShape(edge);
      } else {
        shape = new QuadraticEdgeProxyShape(edge);
      }
      mMap.put(edge, shape);
    }
    createLabelBlockShape(edge.getLabelBlock(), shape);
    final GuardActionBlockProxy block = edge.getGuardActionBlock();
    if (block != null) {
      createGuardActionBlockShape(block, shape);
    }
    return shape;
  }

  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    final LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape != null) {
      return shape;
    } else {
      return createLabelBlockShape(block, null);
    }
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
    
  public SimpleNodeProxyShape visitSimpleNodeProxy(SimpleNodeProxy simple)
  {
    SimpleNodeProxyShape shape = (SimpleNodeProxyShape) lookup(simple);
    if (shape == null) {
      shape = new SimpleNodeProxyShape(simple, mModule);
      mMap.put(simple, shape);
    }
    final LabelGeometryProxy geo = simple.getLabelGeometry();
    LabelProxyShape label = (LabelProxyShape) lookup(geo);
    if (label == null) {
      label = new LabelProxyShape(simple, DEFAULT);
      mMap.put(geo, label);
    }
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
  private LabelBlockProxyShape createLabelBlockShape
    (final LabelBlockProxy block,
     final EdgeProxyShape eshape)
  {
    LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape == null) {
      double width = 0.0;
      double height = 1.0;
      double x;
      double y;
      final LabelGeometryProxy geo = block.getGeometry();
      if (geo == null) {
        x = LabelBlockProxyShape.DEFAULTOFFSETX;
        y = LabelBlockProxyShape.DEFAULTOFFSETY;
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
          new TextLayout(BLOCKED_HEADER, BOLD,
                         new FontRenderContext(null, true, true));
        width = text.getBounds().getWidth();
      }
      final int lx = (int) Math.round(x) + 1;
      for (final Proxy proxy : block.getEventList()) {
        // Use different font for different event kinds.
        Font font = DEFAULT;
        if (proxy instanceof IdentifierProxy) {
          final IdentifierProxy ident = (IdentifierProxy) proxy;
          final String name = ident.getName();
          for (final EventDeclProxy event : mModule.getEventDeclList()) {
            if (event.getName().equals(name)) {
              if (event.getKind() == EventKind.UNCONTROLLABLE) {
                font = UNCONTROLLABLE;
              }
              break;
            }
          }
        }
        final int ly = (int) Math.round(y + height);
        final LabelShape lshape = new LabelShape(proxy, lx, ly, font);
        mMap.put(proxy, lshape);
        final RoundRectangle2D lrect = lshape.getShape();
        height += lrect.getHeight();
        if (width < lrect.getWidth()) {
          width = lrect.getWidth();
        }
      }
      height += 4;
      width += 3;
      final RoundRectangle2D bounds =
        new RoundRectangle2D.Double(x, y, width, height,
                                    LabelBlockProxyShape.DEFAULTARCW,
                                    LabelBlockProxyShape.DEFAULTARCH);
      if (eshape != null) {
        shape = new LabelBlockProxyShape(block, bounds);
      } else {
        shape = new LabeledLabelBlockProxyShape(block, bounds,
                                                BLOCKED_HEADER, BOLD);
      }
      mMap.put(block, shape);
    }
    return shape;
  }
    
  private GuardActionBlockProxyShape createGuardActionBlockShape
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
      final List<SimpleExpressionProxy> guards = block.getGuards();
      for (final SimpleExpressionProxy guard : guards) {
        final int ly = (int) Math.round(y + height);
        final LabelShape lshape =
          new LabelShape(guard, lx, ly, DEFAULT, "guard");
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
          new LabelShape(action, lx, ly, DEFAULT, "action");
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
    

  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final ModuleProxy mModule;
  private final Map<Proxy,ProxyShape> mMap;

    
  //#########################################################################
  //# Class Constants
  public static final Font DEFAULT = new Font("Dialog", Font.PLAIN, 12);
  public static final Font BOLD = DEFAULT.deriveFont(Font.BOLD);
  public static final Font UNCONTROLLABLE = DEFAULT.deriveFont(Font.ITALIC);

  private static final String BLOCKED_HEADER = "BLOCKED:";

}
