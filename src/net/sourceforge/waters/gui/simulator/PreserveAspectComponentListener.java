package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

public class PreserveAspectComponentListener implements ComponentListener
{

  private final AutomatonInternalFrame mFrame;
  private final AutomatonDisplayPane mDisplayPane;

  public PreserveAspectComponentListener(final AutomatonInternalFrame frame, final AutomatonDisplayPane pane)
  {
    mFrame = frame;
    mDisplayPane = pane;
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
    final Rectangle2D preferredSize = mDisplayPane.getMinimumBoundingRectangle();
    final int preferredWidth = (int) preferredSize.getWidth();
    final int preferredHeight = (int) preferredSize.getHeight();
    final int finalWidth = ((mDisplayPane.getWidth() + mDisplayPane.getHeight()) * preferredWidth) / (preferredHeight + preferredWidth);
    final int finalHeight = (preferredHeight * finalWidth) / preferredWidth;
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

}
