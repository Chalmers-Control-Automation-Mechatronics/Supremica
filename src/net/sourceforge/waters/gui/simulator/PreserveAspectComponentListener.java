package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

public class PreserveAspectComponentListener implements ComponentListener
{
  public PreserveAspectComponentListener(final AutomatonInternalFrame frame, final AutomatonDisplayPane pane)
  {
    mFrame = frame;
    mDisplayPane = pane;
    oldBounds = frame.getBounds();
  }

  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  private boolean close (final int a, final int b, final int delta)
  {
    return (Math.abs(a - b) < delta);
  }
  private boolean close (final double a, final double b, final double delta)
  {
    return (Math.abs(a - b) < delta);
  }

  public void componentResized(final ComponentEvent e)
  {
    final int finalWidth;
    final int finalHeight;
    final Rectangle2D automatonSize = mDisplayPane.getMinimumBoundingRectangle();
    final int automatonWidth = (int) automatonSize.getWidth();
    final int automatonHeight = (int) automatonSize.getHeight();
    final boolean growing = getGrowing(getResizeCorner());
    if (growing)
    {
      finalWidth = Math.max(mDisplayPane.getWidth(), (automatonWidth * mDisplayPane.getHeight()) / automatonHeight);
      finalHeight = Math.max(mDisplayPane.getHeight(), (automatonHeight * mDisplayPane.getWidth()) / automatonWidth);
      System.out.println("DEBUG: Growing");
    }
    else
    {
      finalWidth = Math.min(mDisplayPane.getWidth(), (automatonWidth * mDisplayPane.getHeight()) / automatonHeight);
      finalHeight = Math.min(mDisplayPane.getHeight(), (automatonHeight * mDisplayPane.getWidth()) / automatonWidth);
      System.out.println("DEBUG: Shrinking");
    }
    // To prevent it from shrinking by itself, prevent it growing by 2x2 pixels
    if (!close(finalWidth, mDisplayPane.getWidth(), 2) || !close(finalHeight, mDisplayPane.getHeight(), 2))
    {
      mDisplayPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
      mFrame.pack();
      mDisplayPane.repaint();
    }
  }

  public void componentShown(final ComponentEvent e)
  {
    // Do Nothing
  }

  // ########################################################################
  // # Auxillary Functions

  private boolean getGrowing(final Direction constantEdge)
  {
    System.out.println("Constant edge is:" + constantEdge);
    final boolean heightGrow = oldBounds.getHeight() < mFrame.getBounds().getHeight();
    final boolean widthGrow = oldBounds.getWidth() < mFrame.getBounds().getWidth();
    if (constantEdge == null)
      return true; // If nothing is changing
    switch (constantEdge)
    {
    case TOP_LEFT:
      return (heightGrow || widthGrow);
    case BOTTOM_LEFT:
      return (heightGrow && widthGrow);
    case BOTTOM_RIGHT:
      return (heightGrow && widthGrow);
    case TOP_RIGHT:
      System.out.println("TOP RIGHT fired: Growing: " + widthGrow);
      return (widthGrow);
    case TOP:
      return (heightGrow);
    case LEFT:
      return (widthGrow);
    case RIGHT:
      return (widthGrow);
    case BOTTOM:
      return (heightGrow);
    default:
      throw new UnsupportedOperationException("Direction is not supported");
    }
  }

  private Direction getResizeCorner()
  {
    final Rectangle newBounds = mFrame.getBounds();
    final boolean leftEdge = close(oldBounds.getX(), newBounds.getX(), 0.01);
    final boolean rightEdge = close(oldBounds.getX() + oldBounds.getWidth(), newBounds.getX() + newBounds.getWidth(), 0.01);
    final boolean topEdge = close(oldBounds.getY(), newBounds.getY(), 0.01);
    final boolean bottomEdge = close(oldBounds.getY() + oldBounds.getHeight(), newBounds.getY() + newBounds.getHeight(), 0.01);
    if (leftEdge && rightEdge && topEdge)
      return Direction.TOP;
    if (rightEdge && topEdge && bottomEdge)
      return Direction.LEFT;
    if (leftEdge && rightEdge && bottomEdge)
      return Direction.BOTTOM;
    if (leftEdge && topEdge && bottomEdge)
      return Direction.RIGHT;
    if (leftEdge && topEdge)
      return Direction.TOP_LEFT;
    if (rightEdge && bottomEdge)
      return Direction.BOTTOM_RIGHT;
    if (bottomEdge && rightEdge)
      return Direction.BOTTOM_LEFT;
    if (rightEdge && topEdge)
      return Direction.TOP_RIGHT;
    System.out.println("DEBUG: ERROR: Constant Edge is NULL!! Values are TOP "
        + topEdge + " BOTTOM + " + bottomEdge + " LEFT " + leftEdge + " RIGHT " + rightEdge);
    return null;
  }

  // ########################################################################
  // # Interface InternalFrameMouseupObserver

  public void setBounds(final Rectangle bounds)
  {
    oldBounds = bounds;
  }

  private final AutomatonInternalFrame mFrame;
  private final AutomatonDisplayPane mDisplayPane;
  private Rectangle oldBounds;
}
