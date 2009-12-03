package net.sourceforge.waters.gui.simulator;

import java.awt.Rectangle;
import java.util.EventObject;

public class InternalFrameMouseupEvent extends EventObject
{
   public InternalFrameMouseupEvent(final Rectangle bounds)
  {
    super(bounds);
    mBounds = bounds;
  }

  public Rectangle getBounds()
  {
    return mBounds;
  }

  private final Rectangle mBounds;

  private static final long serialVersionUID = -5364461195690862699L;
}
