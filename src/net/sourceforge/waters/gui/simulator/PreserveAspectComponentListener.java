package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

public class PreserveAspectComponentListener implements ComponentListener, InternalFrameMouseupObserver
{



  public PreserveAspectComponentListener(final AutomatonInternalFrame frame, final AutomatonDisplayPane pane)
  {
    mFrame = frame;
    mDisplayPane = pane;
    oldBounds = frame.getBounds();
    topRight = new Point((int)(frame.getLocation().getX() + frame.getWidth()), (int)frame.getLocation().getY());
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

  public void componentResized(final ComponentEvent e)
  {
    final int finalWidth;
    final int finalHeight;
    final Rectangle2D preferredSize = mDisplayPane.getMinimumBoundingRectangle();
    final int preferredWidth = (int) preferredSize.getWidth();
    final int preferredHeight = (int) preferredSize.getHeight();
    finalWidth = Math.max(mDisplayPane.getWidth(), (preferredWidth * mDisplayPane.getHeight()) / preferredHeight);
    finalHeight = Math.max(mDisplayPane.getHeight(), (preferredHeight * mDisplayPane.getWidth()) / preferredWidth);
    // To prevent it from shrinking by itself, prevent it growing by 2x2 pixels
    if (!close(finalWidth, mDisplayPane.getWidth(), 2) || !close(finalHeight, mDisplayPane.getHeight(), 2))
    {
      mDisplayPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
      mFrame.pack();
      mDisplayPane.repaint();
    }
    /*
     * Psuedo code for improved resizing code:
     * ResizeDirection = findDirectionOfResize();
     * Relative relativeDimension;
     * Constant constantCorner;
     * switch (ResizeDirection)
     * {
     *  case (TOP_LEFT):
     *      relativeDimension = MAXIMUM;
     *      constantCorner = BOTTOM_LEFT;
     *      break;
     *  case (TOP):
     *      relativeDimension = HEIGHT;
     *      constantCorner = BOTTOM_RIGHT;
     *      break;
     *  case (TOP_RIGHT):
     *      relativeDimension = MAXIMUM;
     *      constantCorner = BOTTOM_LEFT;
     *      break;
     *  case (RIGHT):
     *      relativeDimension = WIDTH;
     *      constantCorner = TOP_LEFT;
     *      break;
     *  case (BOTTOM_RIGHT):
     *      relativeDimension = MAXIMUM;
     *      constantCorner = TOP_LEFT;
     *      break;
     *  case (BOTTOM):
     *      relativeDimension = HEIGHT;
     *      constantCorner = TOP_LEFT;
     *      break;
     *  case (BOTTOM_LEFT):
     *      relativeDimension = MAXIMUM;
     *      constantCorner = TOP_RIGHT;
     *      break;
     *  case (LEFT):
     *      relativeDimension = WIDTH;
     *      constantCorner = BOTTOM_LEFT;
     *      break;
     *  default:
     *      error_msg();
     * }
     * newDimensions(preferredWidth, preferredHeight, relativeDimensions, constantCorner);
     *
     */
  }

  public void componentShown(final ComponentEvent e)
  {
    // Do Nothing
  }

  // ########################################################################
  // # Auxillary Functions

  private void newDimensions(final int automatonWidth, final int automatonHeight, final Relative relative, final Constant constant)
  {
    final int finalWidth;
    final int finalHeight;
    switch (relative)
    {
    case MAXIMUM:
      finalWidth = Math.max(mDisplayPane.getWidth(), (automatonWidth * mDisplayPane.getHeight()) / automatonHeight);
      finalHeight = Math.max(mDisplayPane.getHeight(), (automatonHeight  * mDisplayPane.getWidth()) / automatonWidth);
      break;
    case HEIGHT:
      finalWidth = (automatonWidth * mDisplayPane.getHeight()) / automatonHeight;
      finalHeight = mDisplayPane.getHeight();
      break;
    case WIDTH:
      finalWidth = mDisplayPane.getWidth();
      finalHeight = (automatonHeight * mDisplayPane.getWidth()) / automatonWidth;
      break;
    default:
        throw new UnsupportedOperationException("Illegal Relative Setting");
    }
    if (!close(finalWidth, mDisplayPane.getWidth(), 2) || !close(finalHeight, mDisplayPane.getHeight(), 2))
    {
      mDisplayPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
      mFrame.pack();
      mDisplayPane.repaint();
    }
    final Point topRightCorner = new Point(oldBounds.getLocation().x, oldBounds.getLocation().y);
    final Point bottomRightCorner = new Point(oldBounds.getLocation().x, (int)(oldBounds.getLocation().y + oldBounds.getHeight()));
    final Point topLeftCorner = new Point((int)(oldBounds.getLocation().x + oldBounds.getWidth()), oldBounds.getLocation().y);
    final Point bottomLeftCorner = new Point((int)(oldBounds.getLocation().x + oldBounds.getWidth()),
        (int)(oldBounds.getLocation().y + oldBounds.getHeight()));
    switch (constant)
    {
    case TOP_RIGHT_CORNER:
      mFrame.setLocation(topRightCorner);
      break;
    case TOP_LEFT_CORNER:
      mFrame.setLocation(new Point((int)(topLeftCorner.getX() - mFrame.getWidth()), (int)topLeftCorner.getY()));
      break;
    case BOTTOM_RIGHT_CORNER:
      mFrame.setLocation(new Point((int)bottomRightCorner.getX(), (int)(bottomRightCorner.getY() - mFrame.getHeight())));
      break;
    case BOTTOM_LEFT_CORNER:
      mFrame.setLocation(new Point((int)(bottomLeftCorner.getX() - mFrame.getWidth()),
          (int)(bottomLeftCorner.getY() - mFrame.getHeight())));
      break;
      default:
        throw new UnsupportedOperationException("Illegal Constant Setting");
    }
  }

  // ########################################################################
  // # Interface InternalFrameMouseupObserver

  public void performMouseUpEvent(final InternalFrameMouseupEvent event)
  {
    topRight = new Point((int)(mFrame.getLocation().getX() + mFrame.getWidth()), (int)mFrame.getLocation().getY());
  }

  private final AutomatonInternalFrame mFrame;
  private final AutomatonDisplayPane mDisplayPane;
  private final Rectangle oldBounds;
  private Point topRight;
}
