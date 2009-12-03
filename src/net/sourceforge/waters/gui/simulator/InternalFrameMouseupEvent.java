package net.sourceforge.waters.gui.simulator;

import java.awt.Rectangle;
import java.util.EventObject;

public class InternalFrameMouseupEvent extends EventObject
{
  // #########################################################################
  // # Constructor
   public InternalFrameMouseupEvent(final Rectangle bounds)
  {
    super(bounds);
    mBounds = bounds;
  }

   // #########################################################################
   // # Simple Access
  public Rectangle getBounds()
  {
    return mBounds;
  }

  // #########################################################################
  // # Data Members
  private final Rectangle mBounds;

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = -5364461195690862699L;
}
