//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ProxyShapeProducer
//###########################################################################
//# $Id: ProxyShapeProducer.java,v 1.31 2008-02-19 01:03:46 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
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

    
  //##########################################################################
  //# Clean up
  public void close()
  {
  }


  //#########################################################################
  //# Simple Access
  public GraphProxy getGraph()
  {
    return mGraph;
  }

  public ModuleProxy getModule()
  {
    return mModule;
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
      getShape(edge.getLabelBlock());
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
        return rect;
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

  public SimpleNodeProxyShape visitSimpleNodeProxy(SimpleNodeProxy simple)
  {
    final SimpleNodeProxyShape shape = createSimpleNodeProxyShape(simple);
    final LabelGeometryProxy geo = simple.getLabelGeometry();
    createLabelProxyShape(geo, simple);
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
      shape = new SimpleNodeProxyShape(simple, mModule);
      mMap.put(simple, shape);
    }
    return shape;
  }

  LabelProxyShape createLabelProxyShape(final LabelGeometryProxy geo,
                                        final SimpleNodeProxy simple)
  {
    LabelProxyShape shape = (LabelProxyShape) lookup(geo);
    if (shape == null) {
      shape = new LabelProxyShape(simple, DEFAULT);
      mMap.put(geo, shape);
    }
    return shape;
  }

  EdgeProxyShape createEdgeProxyShape(final EdgeProxy edge)
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
    return shape;
  }

  LabelBlockProxyShape createLabelBlockShape
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
          new TextLayout(BLOCKED_HEADER, BOLD,
                         new FontRenderContext(null, true, true));
        width = text.getBounds().getWidth();
      }
      final int lx = (int) Math.round(x) + 1;
      for (final Proxy proxy : block.getEventList()) {
        // Use different font for different event kinds.
        Font font = DEFAULT;
        final String name;
        if (proxy instanceof SimpleIdentifierProxy) {
          final SimpleIdentifierProxy ident = (SimpleIdentifierProxy) proxy;
          name = ident.getName();
        } else if (proxy instanceof IndexedIdentifierProxy) {
          final IndexedIdentifierProxy ident = (IndexedIdentifierProxy) proxy;
          name = ident.getName();
        } else {
          name = null;
        }
        if (name != null) {
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
