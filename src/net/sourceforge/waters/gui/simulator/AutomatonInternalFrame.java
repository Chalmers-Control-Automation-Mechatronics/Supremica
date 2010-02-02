package net.sourceforge.waters.gui.simulator;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.GraphSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonInternalFrame extends JInternalFrame implements ModelObserver
{

  //##########################################################################
  //# Constructor
  public AutomatonInternalFrame(final AutomatonProxy aut,
                                final GraphSubject graph,
                                final AutomatonDesktopPane parent,
                                final ModuleContainer container,
                                final Simulation sim)
    throws GeometryAbsentException
  {
    super(aut.getName(), true, true, false, true);
    mParent = parent;
    mDisplayPane = new AutomatonDisplayPane(aut, graph, container, sim, this);
    setContentPane(mDisplayPane);
    addMouseListener(new InternalFrameMouseAdapter());
    addComponentListener(new PreserveAspectComponentListener());
    setVisible(true);
    pack();
    // Store the initial reference position after the window is open.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        storeReferenceFrame();
      }
    });
    this.setFrameIcon(ModuleContext.getComponentKindIcon(aut.getKind()));
    container.getModule().addModelObserver(this);
  }


  //##########################################################################
  //# Overrides for javax.swing.JInternalFrame
  public void dispose()
  {
    mParent.removeAutomaton(this.getTitle());
    mDisplayPane.close();
    super.dispose();
  }

  //##########################################################################
  //# Aspect-preserving resizing
  /**
   * Resizes the window to preserve the graph's aspect ratio.
   * @param  grow   A flag, indicating whether the window should grow or
   *                shrink.
   */
  void adjustSize(final boolean grow)
  {
    // First find the size of the panel and the automaton.
    final int panelWidth = mDisplayPane.getWidth();
    final int panelHeight = mDisplayPane.getHeight();
    final Rectangle2D automatonSize =
      mDisplayPane.getMinimumBoundingRectangle();
    final double automatonWidth = automatonSize.getWidth();
    final double automatonHeight = automatonSize.getHeight();
    // Calculate preferred panel sizes with high precision.
    final int scaledWidth =
      (int) Math.round(automatonWidth * panelHeight / automatonHeight);
    final int scaledHeight =
      (int) Math.round(automatonHeight * panelWidth / automatonWidth);
    // Calculate an aspect-preserving new panel size,
    // smaller (when shrinking) or larger (when growing) than the current size.
    final int finalWidth;
    final int finalHeight;
    if (grow) {
      finalWidth = Math.max(panelWidth, scaledWidth);
      finalHeight = Math.max(panelHeight, scaledHeight);
    } else {
      finalWidth = Math.min(panelWidth, scaledWidth);
      finalHeight = Math.min(panelHeight, scaledHeight);
    }
    // Do not resize if the width and height are unchanged, or if the old bounds haven't yet been loaded
    if ((finalWidth != panelWidth || finalHeight != panelHeight) && mOldBounds != null) {
      // Before resizing, check whether we need to adjust the position.
      // This is necessary when dragging the window to the left or up,
      // otherwise the window may be moved due to rounding errors.
      final Point pos = getLocation();
      int x = pos.x;
      if (x != mOldBounds.x) {
        x += panelWidth - finalWidth;
      }
      int y = pos.y;
      if (y != mOldBounds.y) {
        y += panelHeight - finalHeight;
      }
      // Set the new position and size with a single call to reduce
      // spurious events.
      setBounds(x, y, finalWidth + mBorderWidth, finalHeight + mBorderHeight);
    }
  }

  public JDesktopPane getDesktopPane()
  {
    return mParent;
  }

  /**
   * Stores the current position of the window as a reference frame for
   * aspect-preserving resizing. Also calculates the size of the window
   * decorations when called the first time.
   */
  void storeReferenceFrame()
  {
    final Rectangle bounds = getBounds();
    if (mOldBounds == null) {
      mOldBounds = new Rectangle(bounds);
      mBorderWidth = bounds.width - mDisplayPane.getWidth();
      mBorderHeight = bounds.height - mDisplayPane.getHeight();
    } else {
      mOldBounds.setBounds(bounds);
    }
  }

  // ###########################################################################
  // # Access methods for event handling

  public void execute()
  {
    mDisplayPane.execute();
  }

  public void resize()
  {
    if (canResize())
    {
      //final Rectangle2D size = mDisplayPane.getMinimumBoundingRectangle();
      //mDisplayPane.setSize(new Dimension((int)size.getX(), (int)size.getY()));
      //System.out.println("DEBUG: AutomatonInternalFrame[155]: Can Resize before pack: " + canResize());
      this.pack();
      //System.out.println("DEBUG: AutomatonInternalFrame[157]: Can Resize after pack: " + canResize());
      SwingUtilities.invokeLater(new Thread(){public void run(){AutomatonInternalFrame.this.repaint();System.out.println("DEBUG: AutomatonInternalFrame[158]: Can Resize after repaint: " + canResize());}});
      storeReferenceFrame();
    }
  }

  public boolean canResize()
  {
    System.out.println("DEBUG: AutomatonInternalFrame [165]: Size of desired outcome is : " + mDisplayPane.getPreferredSize());
    System.out.println("DEBUG: Height Difference is : " + (mDisplayPane.getSize().getHeight() - mDisplayPane.getPreferredSize().getHeight()));
    System.out.println("DEBUG: Width Difference is : " + (mDisplayPane.getSize().getWidth() - mDisplayPane.getPreferredSize().getWidth()));
    return (Math.abs(mDisplayPane.getSize().getHeight() - mDisplayPane.getPreferredSize().getHeight()) > 10
            && Math.abs(mDisplayPane.getSize().getWidth() - mDisplayPane.getPreferredSize().getWidth()) > 10);
  }

  // ###########################################################################
  // # Interface ModelObserver

  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() == ModelChangeEvent.GEOMETRY_CHANGED)
    {
      this.adjustSize(false);
      this.repaint();
    }
  }

  //##########################################################################
  //# Inner class InternalFrameMouseAdapter
  private class InternalFrameMouseAdapter extends MouseAdapter
  {

    //########################################################################
    //# Interface java.awt.event.MouseListener
    /**
     * Listener for mouse-release events.
     * When mouse button 1 is released, a window-resize operation may
     * have been completed, so we store the new location of the reference
     * frame.
     */
    public void mouseReleased(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        storeReferenceFrame();
      }
    }

  }


  //##########################################################################
  //# Inner Class PreserveAspectComponentListener
  private class PreserveAspectComponentListener extends ComponentAdapter
  {

    //########################################################################
    //# Interface java.awt.event.MouseListener
    /**
     * Listener for component-moved events.
     * When the window has been moved, we must update the reference frame,
     * but not if the movement happens while resizing.
     */
    public void componentMoved(final ComponentEvent event)
    {
      final Rectangle bounds = getBounds();
      if (mOldBounds == null ||
          mOldBounds.width == bounds.width &&
          mOldBounds.height == bounds.height) {
        storeReferenceFrame();
      }
    }

    /**
     * Listener for component-resized events.
     * This listener tries to change the window size to preserve the aspect
     * ratio of the graph, but only if a reference frame is available, and
     * the spring embedder is not running.
     */
    public void componentResized(final ComponentEvent event)
    {
      if (mOldBounds != null && !mDisplayPane.isEmbedderRunning()) {
        final Rectangle bounds = getBounds();
        final boolean grow =
          bounds.width > mOldBounds.width || bounds.height > mOldBounds.height;
        adjustSize(grow);
      }
    }

  }


  //##########################################################################
  //# Data Members
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mParent;

  /**
   * The reference position of this internal frame.
   * This represents the last known stable position of the frame and is
   * used as a reference while resizing.
   */
  private Rectangle mOldBounds = null;
  /**
   * The calculated total width of the window decorations.
   * This is the difference between the window width and the width of the
   * content pane.
   */
  private int mBorderWidth = 0;
  /**
   * The calculated total height of the window decorations.
   * This is the difference between the window height and the height of the
   * content pane.
   */
  private int mBorderHeight = 0;


  //##########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;


}
