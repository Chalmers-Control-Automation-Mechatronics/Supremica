//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.simulator;


import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonInternalFrame
  extends JInternalFrame
  implements ModelObserver
{

  //#########################################################################
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
      @Override
      public void run() {
        storeReferenceFrame();
      }
    });
    final ComponentKind kind = aut.getKind();
    setFrameIcon(kind);
    container.getModule().addModelObserver(this);
  }


  //#########################################################################
  //# Overrides for javax.swing.JInternalFrame
  @Override
  public void dispose()
  {
    mParent.removeAutomaton(this.getTitle());
    mDisplayPane.close();
    super.dispose();
  }

  //#########################################################################
  //# Aspect-preserving resizing
  /**
   * Resizes the window to preserve the graph's aspect ratio.
   * This method is called graph has changed externally, and it attempts
   * to change the height of the frame to accommodate the new aspect
   * ratio.
   */
  void adjustSize()
  {
    final Rectangle bounds = getBounds();
    adjustSize(bounds, true);
    storeReferenceFrame();
  }

  /**
   * Resizes the window to preserve the graph's aspect ratio.
   * @param  newBounds  The new size and position of the frame, including
   *                    window decorations.
   * @param  force      <CODE>true</CODE> to force resize even if the frame
   *                    size has not been changed. This is needed when the
   *                    graph has changed externally, as opposed to drag
   *                    events changing the frame.
   */
  void adjustSize(final Rectangle newBounds,
                  final boolean force)
  {
    // First check whether anything has changed at all.
    if (mOldBounds == null) {
      return;
    }
    final int newWidth = newBounds.width;
    final int newHeight = newBounds.height;
    final int oldWidth = mOldBounds.width;
    final int oldHeight = mOldBounds.height;
    final int widthChange = Math.abs(newWidth - oldWidth);
    final int heightChange = Math.abs(newHeight - oldHeight);
    if (!force && widthChange == 0 && heightChange == 0) {
      return;
    }
    // Find the natural size of the automaton and aspect ratio.
    final Rectangle2D automatonSize =
      mDisplayPane.getMinimumBoundingRectangle();
    final double aspectRatio =
      automatonSize.getHeight() / automatonSize.getWidth();
    // Scale the height and width to aspect ratio.
    // Should we adjust the height or width?
    // Keep whatever quantity has changed more and scale the other.
    final int newAutomatonWidth, newAutomatonHeight;
    if (widthChange >= heightChange) {
      newAutomatonWidth = newWidth - mBorderWidth;
      newAutomatonHeight = (int) Math.round(newAutomatonWidth * aspectRatio);
    } else {
      newAutomatonHeight = newHeight - mBorderHeight;
      newAutomatonWidth = (int) Math.round(newAutomatonHeight / aspectRatio);
    }
    newBounds.width = newAutomatonWidth + mBorderWidth;
    newBounds.height = newAutomatonHeight + mBorderHeight;
    // If the window is being dragged to the left or up,
    // we now may also have to adjust the position.
    if (widthChange > 0 && newBounds.x != mOldBounds.x) {
      // anchor is at the right
      newBounds.x = mOldBounds.x + oldWidth - newBounds.width;
    }
    if (heightChange > 0 && newBounds.y != mOldBounds.y) {
      // anchor is at the bottom
      newBounds.y = mOldBounds.y + oldHeight - newBounds.height;
    }
    // Set the new position and size with a single call to reduce
    // spurious events.
    setBounds(newBounds);
    // Don't set mOldBounds - this is done by mouse handler.
  }

  @Override
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


  //#########################################################################
  //# Access Methods for Event Handling
  public void execute(final Proxy proxyToFire)
  {
    mDisplayPane.execute(proxyToFire);
  }

  public void resize()
  {
    if (canResize())
    {
      this.pack(); // This code automatically resizes the Internal Frame to the size it was when it started
      SwingUtilities.invokeLater(new Thread(){@Override
      public void run(){AutomatonInternalFrame.this.repaint();}});
      storeReferenceFrame();
    }
  }

  public boolean canResize()
  {
    return (Math.abs(mDisplayPane.getSize().getHeight() - mDisplayPane.getPreferredSize().getHeight()) > 10
            && Math.abs(mDisplayPane.getSize().getWidth() - mDisplayPane.getPreferredSize().getWidth()) > 10);
  }


  //#########################################################################
  //# Interface ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() == ModelChangeEvent.GEOMETRY_CHANGED) {
      adjustSize();
      repaint();
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.RENDERING_PRIORITY;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setFrameIcon(final ComponentKind kind)
  {
    final List<ImageIcon> icons = ModuleContext.getComponentKindIconList(kind);
    ImageIcon icon = null;
    if (icons.size() > 1) {
      try {
        final BasicInternalFrameUI ui =
          (javax.swing.plaf.basic.BasicInternalFrameUI) getUI();
        final int titleBarHeight = ui.getNorthPane().getPreferredSize().height;
        for (final ImageIcon candidate : icons) {
          if (candidate.getIconHeight() <= titleBarHeight) {
            icon = candidate;
          }
        }
      } catch (final ClassCastException exception) {
        // Just in case the UI is a different type ...
      }
    }
    if (icon == null && !icons.isEmpty()) {
      icon = icons.get(0);
    }
    setFrameIcon(icon);
  }


  //#########################################################################
  //# Inner class InternalFrameMouseAdapter
  private class InternalFrameMouseAdapter extends MouseAdapter
  {

    //#######################################################################
    //# Interface java.awt.event.MouseListener
    /**
     * Listener for mouse-release events.
     * When mouse button 1 is released, a window-resize operation may
     * have been completed, so we store the new location of the reference
     * frame.
     */
    @Override
    public void mouseReleased(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        storeReferenceFrame();
      }
    }

  }


  //#########################################################################
  //# Inner Class PreserveAspectComponentListener
  private class PreserveAspectComponentListener extends ComponentAdapter
  {

    //#######################################################################
    //# Interface java.awt.event.ComponentListener
    /**
     * Listener for component-moved events.
     * When the window has been moved, we must update the reference frame,
     * but not if the movement happens while resizing.
     */
    @Override
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
    @Override
    public void componentResized(final ComponentEvent event)
    {
      if (mOldBounds != null && !mDisplayPane.isEmbedderRunning()) {
        final Rectangle bounds = getBounds();
        adjustSize(bounds, false);
        // Don't set mOldBounds - this is done by mouse handler.
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mParent;

  /**
   * The reference position of this internal frame.
   * This represents the last known stable position of the frame and is
   * used as a reference while resizing.
   * The bounding box includes the window decorations.
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5838879656333394405L;

}
