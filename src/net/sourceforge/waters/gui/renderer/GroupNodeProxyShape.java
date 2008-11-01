//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SubjectShapeProducer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.GroupNodeProxy;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;


public class GroupNodeProxyShape
    extends AbstractProxyShape
{

  //########################################################################
  //# Constructors
  public GroupNodeProxyShape(final GroupNodeProxy group)
  {
    super(group);
    mRect = new Rectangle2D.Double();
    mRect.setRect(group.getGeometry().getRectangle());
    mHandles = new ArrayList<Handle>(8);
    final Point2D min =
      new Point2D.Double(getShape().getMinX(), getShape().getMinY());
    final Point2D c =
      new Point2D.Double(getShape().getCenterX(), getShape().getCenterY());
    final Point2D max =
      new Point2D.Double(getShape().getMaxX(), getShape().getMaxY());
    mHandles.add(new DefaultHandle(min, Handle.HandleType.NW));
    mHandles.add(new DefaultHandle(c.getX(), min.getY(), Handle.HandleType.N));
    mHandles.add
      (new DefaultHandle(max.getX(), min.getY(), Handle.HandleType.NE));
    mHandles.add(new DefaultHandle(min.getX(), c.getY(), Handle.HandleType.W));
    mHandles.add(new DefaultHandle(max.getX(), c.getY(), Handle.HandleType.E));
    mHandles.add
      (new DefaultHandle(min.getX(), max.getY(), Handle.HandleType.SW));
    mHandles.add(new DefaultHandle(c.getX(), max.getY(), Handle.HandleType.S));
    mHandles.add(new DefaultHandle(max, Handle.HandleType.SE));
  }
    

  //########################################################################
  //# Simple Access
    public List<Handle> getHandles()
    {
        return mHandles;
    }
    
    public Rectangle2D getShape()
    {
        return mRect;
    }
    
    public GroupNodeProxy getProxy()
    {
        return (GroupNodeProxy)super.getProxy();
    }
    
    public boolean isClicked(int ex, int ey)
    {
        for (Handle h : getHandles())
        {
            if (h.isClicked(ex, ey))
            {
                return true;
            }
        }
        Rectangle2D rect = new Rectangle2D.Double(ex - 2, ey - 2, 4, 4);
        return (getShape().intersects(rect) && !getShape().contains(rect));
    }
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
                
        super.draw(g, status);

        // Thicker line for the rectangle!
        g.setColor(status.getColor());
        g.setStroke(DOUBLESTROKE);
        g.draw(getShape());
        
        if (status.showHandles())
        {
            for (RendererShape handle : getHandles())
            {
                handle.draw(g, status);
            }
        }
    }
    
    private final Rectangle2D mRect;
    private final List<Handle> mHandles;
}
