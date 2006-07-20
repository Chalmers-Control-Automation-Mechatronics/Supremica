package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;

import net.sourceforge.waters.gui.renderer.Handle.HandleType;

public class GroupNodeProxyShape
	extends AbstractProxyShape
{
	public GroupNodeProxyShape(GroupNodeProxy proxy)
	{
		super(proxy);
		mRect = new Rectangle2D.Double();
		mRect.setRect(proxy.getGeometry().getRectangle());
    mHandles = new ArrayList<Handle>(8);
    Point2D min = new Point2D.Double(getShape().getMinX(), getShape().getMinY());
    Point2D c = new Point2D.Double(getShape().getCenterX(), getShape().getCenterY());
    Point2D max = new Point2D.Double(getShape().getMaxX(), getShape().getMaxY());
    mHandles.add(new DefaultHandle(min, Handle.HandleType.NW, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(c.getX(), min.getY())
                                   , Handle.HandleType.N, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(max.getX(), min.getY())
                                   , Handle.HandleType.NE, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(min.getX(), c.getY())
                                   , Handle.HandleType.W, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(max.getX(), c.getY())
                                   , Handle.HandleType.E, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(min.getX(), max.getY())
                                   , Handle.HandleType.SW, 6));
    mHandles.add(new DefaultHandle(new Point2D.Double(c.getX(), max.getY())
                                   , Handle.HandleType.S, 6));
    mHandles.add(new DefaultHandle(max, Handle.HandleType.SE, 6));
	}
  
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
    for (Handle h : getHandles()) {
      if (h.isClicked(ex, ey)) {
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
		if (status.isHighlighted())
		{
			g.setColor(status.getShadowColor());
			g.setStroke(SHADOWSTROKE);
			g.draw(getShape());
		}
		g.setColor(status.getColor());
		g.setStroke(DOUBLESTROKE);
		g.draw(getShape());
    if (status.showHandles()) {
      for (RendererShape handle : getHandles()) {
        handle.draw(g, status);
      }
    }
	}
	
	private final Rectangle2D mRect;
  private final List<Handle> mHandles;
}
