package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Timer;


public class PreserveAspectComponentListener implements ComponentListener
{
  // #########################################################################
  // # Constructor
  public PreserveAspectComponentListener(final AutomatonInternalFrame frame,
      final AutomatonDisplayPane pane)
  {
    mFrame = frame;
    mDisplayPane = pane;
    oldBounds = frame.getBounds();
    draggingEnabled = true;
  }

  // ########################################################################
  // # Interface InternalFrameMouseupObserver

  public void setBounds(final Rectangle bounds)
  {
    oldBounds = bounds;
    System.out.println("Bounds Changed");
  }

  // #########################################################################
  // # Class Component Listener
  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    if (draggingEnabled)
    {
      oldBounds = mFrame.getBounds();
      //System.out.println("Bounds Changed. Reason: " + e.getID());
    }
  }

  public void componentResized(final ComponentEvent e)
  {
    final int finalWidth;
    final int finalHeight;
    final Rectangle2D automatonSize =
        mDisplayPane.getMinimumBoundingRectangle();
    final int automatonWidth = (int) automatonSize.getWidth();
    final int automatonHeight = (int) automatonSize.getHeight();
    final Direction resizeCorner = getResizeCorner();
    if (resizeCorner != Direction.NONE && resizeCorner != Direction.ILLEGAL) {
      final boolean growing = getGrowing(resizeCorner);
      if (growing) {
        finalWidth =
            Math.max(mDisplayPane.getWidth(),
                (automatonWidth * mDisplayPane.getHeight()) / automatonHeight);
        finalHeight =
            Math.max(mDisplayPane.getHeight(),
                (automatonHeight * mDisplayPane .getWidth()) / automatonWidth);
        //System.out.println("DEBUG: Growing");
      } else {
        finalWidth =
            Math.min(mDisplayPane.getWidth(),
                (automatonWidth * mDisplayPane.getHeight()) / automatonHeight);
        finalHeight =
            Math.min(mDisplayPane.getHeight(),
                (automatonHeight * mDisplayPane.getWidth()) / automatonWidth);
        //System.out.println("DEBUG: Shrinking");
      }
      // To prevent it from shrinking by itself, prevent it growing by 2x2
      // pixels
      if (!close(finalWidth, mDisplayPane.getWidth(), MINIMUM_CHANGE_TO_RESIZE)
          || !close(finalHeight, mDisplayPane.getHeight(), MINIMUM_CHANGE_TO_RESIZE)) {
        mDisplayPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
        mFrame.pack();
        mDisplayPane.repaint();
      }
    }
    final int delay = 100;
    final ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(final ActionEvent evt) {
            PreserveAspectComponentListener.this.enableDragging();
        }
    };
    new Timer(delay, taskPerformer).start();
    draggingEnabled = false;
  }

  public void componentShown(final ComponentEvent e)
  {
    // Do Nothing
  }

  // ########################################################################
  // # Auxillary Functions

  private boolean close(final int a, final int b, final int delta)
  {
    return (Math.abs(a - b) < delta);
  }

  private boolean close(final double a, final double b, final double delta)
  {
    return (Math.abs(a - b) < delta);
  }

  private void enableDragging()
  {
    draggingEnabled = true;
  }

  private boolean getGrowing(final Direction constantEdge)
  {
    final boolean heightGrow =
        oldBounds.getHeight() < mFrame.getBounds().getHeight();
    final boolean widthGrow =
        oldBounds.getWidth() < mFrame.getBounds().getWidth();
    if (constantEdge == Direction.ILLEGAL || constantEdge == Direction.NONE)
      throw new IllegalArgumentException(
          "No direction, or illegal direction specified");
    switch (constantEdge) {
    case TOP_LEFT:
      return (heightGrow || widthGrow);
    case BOTTOM_LEFT:
      return (heightGrow && widthGrow);
    case BOTTOM_RIGHT:
      return (heightGrow && widthGrow);
    case TOP_RIGHT:
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
    //System.out.println("DEBUG: Old Bounds:" + oldBounds + " New Bounds:"
        //+ newBounds);
    final boolean leftEdge = close(oldBounds.getX(), newBounds.getX(), MINIMUM_CHANGE_TO_NON_CONSTANT_BORDER);
    final boolean rightEdge =
        close(oldBounds.getX() + oldBounds.getWidth(), newBounds.getX()
            + newBounds.getWidth(), MINIMUM_CHANGE_TO_NON_CONSTANT_BORDER);
    final boolean topEdge = close(oldBounds.getY(), newBounds.getY(), MINIMUM_CHANGE_TO_NON_CONSTANT_BORDER);
    final boolean bottomEdge =
        close(oldBounds.getY() + oldBounds.getHeight(), newBounds.getY()
            + newBounds.getHeight(), MINIMUM_CHANGE_TO_NON_CONSTANT_BORDER);
    if (leftEdge && rightEdge && topEdge && bottomEdge)
      return Direction.NONE;
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
    if (bottomEdge && leftEdge)
      return Direction.BOTTOM_LEFT;
    if (rightEdge && topEdge)
      return Direction.TOP_RIGHT;
    //System.out.println("DEBUG: ERROR: Constant Edge is ILLEGAL!! Values are TOP "
    //    + topEdge + " BOTTOM + " + bottomEdge + " LEFT " + leftEdge + " RIGHT "
    //    + rightEdge);
    return Direction.ILLEGAL;
  }



  // #########################################################################
  // # Data Members

  private final AutomatonInternalFrame mFrame;
  private final AutomatonDisplayPane mDisplayPane;
  private Rectangle oldBounds;
  private boolean draggingEnabled;

  // ########################################################################
  // # Class Constants

  private final int MINIMUM_CHANGE_TO_RESIZE = 2; // The minimum change in size which corresponds to needing a resize to take place.
    // Prevents the frame from automatically shrinking. It might be able to safely set it to 0.
  private final int MINIMUM_CHANGE_TO_NON_CONSTANT_BORDER = 40; // The minimum difference in location before a border is defined as being 'moved'
    // Too high, and there will be many Direction.NONE calls, preventing the frame from resizing. Too low, and there will be many
    // Direction.ILLEGAL calls, preventing the frame from resizing.


}
