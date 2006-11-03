package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.Point;

public class DefaultHandle
  extends AbstractRendererShape
  implements Handle
{
  public static int DEFAULTWIDTH = 4;
  
  private final Rectangle2D mShape;
  private final HandleType mHandleType;
  
  public DefaultHandle(Point2D centre, HandleType type)
  {
    this(centre, type, DEFAULTWIDTH);
  }
  
  public DefaultHandle(Point2D centre, HandleType type, int width)
  {
    Point2D corner = new Point((int)centre.getX() + width/2,
                               (int)centre.getY() + width/2);
    mShape = new Rectangle2D.Double();
    mShape.setFrameFromCenter(centre, corner);
    mHandleType = type;
  }
  
  public Rectangle2D getShape()
  {
    return mShape;
  }
  
  public void draw(Graphics2D g, RenderingInformation status)
  {
    super.draw(g, status);
    g.fill(getShape());
  }
  
  public boolean isClicked(int x, int y)
	{
    Rectangle2D rect = new Rectangle2D.Double(getShape().getMinX() - 1,
                                              getShape().getMinY() - 1, 
                                              getShape().getWidth() + 2, 
                                              getShape().getWidth() + 2); 
		return rect.contains(x,y);
	}
  
  public HandleType getType()
  {
    return mHandleType;
  }
}
