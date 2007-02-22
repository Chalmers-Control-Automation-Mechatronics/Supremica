//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ProxyShapeProducer
//###########################################################################
//# $Id: ProxyShapeProducer.java,v 1.20 2007-02-22 03:08:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
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
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.xsd.base.EventKind;


public class ProxyShapeProducer
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ProxyShapeProducer(final ModuleProxy module)
  {
    mModule = module;
    mMap = Collections.synchronizedMap(new HashMap<Proxy,ProxyShape>());
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
    

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public EdgeProxyShape visitEdgeProxy(final EdgeProxy edge)
  {
    EdgeProxyShape shape = (EdgeProxyShape) mMap.get(edge);
    if (shape == null) {
      if (GeometryTools.isSelfloop(edge)) {
        shape = new TieEdgeProxyShape(edge);
      } else if (edge.getGeometry() == null) {
        shape = new StraightEdgeProxyShape(edge);
      } else {
        shape = new QuadraticEdgeProxyShape(edge);
      }
      mMap.put(edge, shape);
      createLabelBlockShape(edge.getLabelBlock(), shape);
      final GuardActionBlockProxy ga = edge.getGuardActionBlock();
      if (ga != null) {
        createGuardActionBlockShape(ga, shape);
      }
    }
    return shape;
  }

  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    final LabelBlockProxyShape shape = (LabelBlockProxyShape) mMap.get(block);
    if (shape != null) {
      return shape;
    } else {
      return createLabelBlockShape(block, null);
    }
  }
    
  public GroupNodeProxyShape visitGroupNodeProxy(final GroupNodeProxy group)
  {
    GroupNodeProxyShape shape = (GroupNodeProxyShape) mMap.get(group);
    if (shape == null) {
      shape = new GroupNodeProxyShape(group);
      mMap.put(group, shape);
    }
    return shape;
  }
    
  public SimpleNodeProxyShape visitSimpleNodeProxy(SimpleNodeProxy simple)
  {
    final LabelGeometryProxy geo = simple.getLabelGeometry();
    LabelProxyShape label = (LabelProxyShape) mMap.get(geo);
    if (label == null) {
      label = new LabelProxyShape(simple, DEFAULT);
      mMap.put(geo, label);
    }
    SimpleNodeProxyShape shape = (SimpleNodeProxyShape) mMap.get(simple);
    if (shape == null) {
      shape = new SimpleNodeProxyShape(simple, mModule);
      mMap.put(simple, shape);
    }
    return shape;
  }
    
  public ProxyShape visitProxy(Proxy p) throws VisitorException
  {
    ProxyShape s = mMap.get(p);
    if (s == null)
      {
        throw new VisitorException(p + " is not in the map");
      }
    return s;
  }
    

  //#########################################################################
  //# Auxiliary Access
  public Rectangle getMinimumBoundingRectangle()
  {
    // *** BUG ***
    // This is only accurate when the graph has been drawn!
    // ***
    Collection<ProxyShape> shapes = mMap.values();
    Rectangle rect = new Rectangle(0,0,0,0);
    synchronized(mMap)
      {
        for (ProxyShape shape : shapes)
          {
            rect.add(shape.getShape().getBounds2D());
          }
      }
    return rect;
  }
    
  protected Map<Proxy, ProxyShape> getMap()
  {
    return mMap;
  }
    

  //#########################################################################
  //# Auxiliary Methods
  LabelBlockProxyShape createLabelBlockShape
    (final LabelBlockProxy block,
     final EdgeProxyShape eshape)
  {
    double height = 1.0;
    double width = 0.0;
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
    final LabelBlockProxyShape shape;
    if (eshape != null) {
      shape = new LabelBlockProxyShape(block, bounds);
    } else {
      shape = new LabeledLabelBlockProxyShape(block, bounds,
                                              BLOCKED_HEADER, BOLD);
    }
    mMap.put(block, shape);
    return shape;
  }
    
  GuardActionBlockProxyShape createGuardActionBlockShape
    (final GuardActionBlockProxy ga,
     final EdgeProxyShape eshape)
  {
    final Point2D turn = eshape.getTurningPoint();
    double x = turn.getX();
    double y = turn.getY();
    final LabelGeometryProxy geo = ga.getGeometry();
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
    final List<SimpleExpressionProxy> guards = ga.getGuards();
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
    final List<BinaryExpressionProxy> actions = ga.getActions();
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
    final GuardActionBlockProxyShape shape =
      new GuardActionBlockProxyShape(ga, bounds);
    mMap.put(ga, shape);
    return shape;
  }
    

  //#########################################################################
  //# Data Members
  private final ModuleProxy mModule;
  private final Map<Proxy,ProxyShape> mMap;

    
  //#########################################################################
  //# Class Constants
  public static final Font DEFAULT = new Font("Dialog", Font.PLAIN, 12);
  public static final Font BOLD = DEFAULT.deriveFont(Font.BOLD);
  public static final Font UNCONTROLLABLE = DEFAULT.deriveFont(Font.ITALIC);

  private static final String BLOCKED_HEADER = "BLOCKED:";

}
