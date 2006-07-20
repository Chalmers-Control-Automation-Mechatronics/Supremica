package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.Color;
import java.awt.Graphics2D;

public class GeneralShape
  implements MiscShape
{
  private final Shape mShape;
  private final Color mColor;
  private final Color mFillColor;
  
  public GeneralShape(Shape shape, Color color)
  {
    this(shape, color, null);
  }
  
  public GeneralShape(Shape shape, Color color, Color fillColor)
  {
    mShape = shape;
    mColor = color;
    mFillColor = fillColor;
  }
  
  public void draw(Graphics2D graphics)
  {
    graphics.setStroke(AbstractRendererShape.BASICSTROKE);
    if (mFillColor != null) {
      graphics.setColor(mFillColor);
      graphics.fill(mShape);
    }
    graphics.setColor(mColor);
    graphics.draw(mShape);
  }
}
