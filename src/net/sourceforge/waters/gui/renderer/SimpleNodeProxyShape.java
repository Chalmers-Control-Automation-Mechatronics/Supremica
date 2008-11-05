//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SimpleNodeProxyShape
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import org.supremica.properties.Config;


public class SimpleNodeProxyShape
  extends AbstractProxyShape
{
    
  //#########################################################################
  //# Constructor
  SimpleNodeProxyShape(SimpleNodeProxy proxy, ModuleProxy module)
  {
    super(proxy);
    mModule = module;
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final int diameter = radius + radius;
    final Point2D p = getProxy().getPointGeometry().getPoint();
    final Rectangle2D rect =
      new Rectangle2D.Double(p.getX() - radius, p.getY() - radius,
                             diameter, diameter);
    mCircleShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
    mShape = new GeneralPath(mCircleShape);

    // Create handles
    if (proxy.isInitial()) {
      final Handle handle = new InitialStateHandle(proxy);
      mHandles = Collections.singletonList(handle);
      mShape.append(handle.getShape(), false);
    } else {
      mHandles = Collections.emptyList();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  public GeneralPath getShape()
  {
    return mShape;
  }
    
    
  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  public SimpleNodeProxy getProxy()
  {
    return (SimpleNodeProxy) super.getProxy();
  }

  public List<Handle> getHandles()
  {
    return mHandles;
  }
  
  
  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.gui.renderer.AbstractProxyShape
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    // Draw the filling (depends on marking)
    updateColors();
    // This rectangle is not the same as the one used to create the
    // mCircleShape! It gives rounding errors!
    // Rectangle2D rect = mCircleShape.getBounds();
    // This one is correct!
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final int diameter = radius + radius;
    final Rectangle2D rect =
      new Rectangle2D.Double(mCircleShape.getX(), mCircleShape.getY(),
                             diameter, diameter);
    Arc2D arc;
    double i = 0;
    double degrees = ((double)360 / (double)mColors.size());
    if (mColors.isEmpty()) {
      arc = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
      g.setColor(FILLCOLOR);
      g.fill(arc);
    } else {
      // Draw marking
      final Object layoutMode = Config.GUI_EDITOR_LAYOUT_MODE.get();
      if (layoutMode.equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES)) {
        // CHALMERS IDES MODE---SINGLE TYPE OF MARKING, DOUBLE CIRCLES
        g.setColor(EditorColor.DEFAULTCOLOR);
        g.setStroke(SINGLESTROKE);
        arc = new Arc2D.Double(rect.getX()+2, rect.getY()+2,
                               rect.getWidth()-4, rect.getHeight()-4,
                               0, 360, Arc2D.OPEN);
        g.draw(arc);
      } else {
        // DEFAULT MODE
        for (final Color c : mColors) {
          arc = new Arc2D.Double(rect, i, degrees, Arc2D.PIE);
          g.setColor(c);
          g.fill(arc);
          i += degrees;
        }
      }
    }
        
    // Draw handles (initial state arrow)
    for (final Handle handle : mHandles) {
      g.setColor(status.getColor());
      handle.draw(g, status);
    }

    // The above handle drawing should not be necessary (it's drawn below) but 
    // the initial arrow refuses to be drawn filled in the editor
    // (not in printed output!?)?
      
    // Draw the basic shape (the outline + handles (initial state arrow))
    super.draw(g, status);
        
    // Cross out if forbidden
    if (mIsForbidden) {
      g.setColor(EditorColor.ERRORCOLOR);
      g.setStroke(DOUBLESTROKE);
      g.drawLine((int) rect.getMaxX(), (int) rect.getMaxY(),
                 (int) rect.getMinX(), (int) rect.getMinY());
      g.drawLine((int) rect.getMaxX(), (int) rect.getMinY(),
                 (int) rect.getMinX(), (int) rect.getMaxY());
    }
  }
    

  //#########################################################################
  //# Auxiliary Methods
  /**
   * I think this method updates the set of colors used
   * (if this is a marked node).
   */
  private void updateColors()
  {
    mColors.clear();
    if (mModule != null) {
      Map<String, EventDeclProxy> map = new HashMap<String, EventDeclProxy>
        (mModule.getEventDeclList().size());
      final List<Proxy> list = getProxy().getPropositions().getEventList();
      if (list.isEmpty()) {
        return;
      }
      for (EventDeclProxy e : mModule.getEventDeclList()) {
        map.put(e.getName(), e);
      }
      for (final Proxy prop : list) {
        // BUG: ForeachEventSubject not supported!
        final String name;
        if (prop instanceof SimpleIdentifierProxy) {
          final SimpleIdentifierProxy ident = (SimpleIdentifierProxy) prop;
          name = ident.getName();
        } else if (prop instanceof IndexedIdentifierProxy) {
          final IndexedIdentifierProxy ident = (IndexedIdentifierProxy) prop;
          name = ident.getName();
        } else {
          continue;
        }
        final EventDeclProxy decl = map.get(name);
        if (decl == null) {
          mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
          continue;
        }
        if (decl.getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          mIsForbidden = true;
          continue;
        }
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo == null) {
          mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
          continue;
        }
        mColors.addAll(geo.getColorSet());
      }
    }
  }
    
    
  //#########################################################################
  //# Data Members
  private final ModuleProxy mModule;
  private final Arc2D mCircleShape;
  private final GeneralPath mShape; // To incorporate the initial state arrow
  private final List<Handle> mHandles;

  private Collection<Color> mColors = new ArrayList<Color>();
  private boolean mIsForbidden = false;
   
    
  //#########################################################################
  //# Class Constants
  public static final int DEFAULT_OFFSET_X = 5;
  public static final int DEFAULT_OFFSET_Y = 5;
  public static final Point2D DEFAULT_OFFSET =
    new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

  public static final int DEFAULT_INITARROW_X = -5;
  public static final int DEFAULT_INITARROW_Y = -5;
  public static final Point2D DEFAULT_INITARROW =
    new Point(DEFAULT_INITARROW_X, DEFAULT_INITARROW_Y);
    
  private static final Color FILLCOLOR = Color.WHITE;

}
