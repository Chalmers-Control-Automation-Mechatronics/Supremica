//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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


import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.GraphSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonInternalFrame
  extends JInternalFrame
  implements ModelObserver
{

  //#########################################################################
  //# Constructor
  public AutomatonInternalFrame(final GraphSubject graph,
                                final AutomatonProxy aut,
                                final BindingContext bindings,
                                final ModuleContainer container,
                                final Simulation sim,
                                final AutomatonDesktopPane parent)
    throws GeometryAbsentException
  {
    super(aut.getName(), true, true, false, true);
    mParent = parent;
    mDisplayPane =
      new AutomatonDisplayPane(graph, aut, bindings, container, sim, this);
    setContentPane(mDisplayPane);
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
  //# Simple Access
  @Override
  public JDesktopPane getDesktopPane()
  {
    return mParent;
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
  //# Aspect-preserving Resizing
  /**
   * Resizes the window to preserve the graph's aspect ratio.
   * This method is called when graph has changed externally, and it attempts
   * to change the height of the frame to fit the automaton's aspect
   * ratio.
   */
  void adjustSize()
  {
    mPreviousResizeBounds = null;
    final Rectangle bounds = getBounds();
    adjustSize(bounds, false);
    storeReferenceFrame();
  }

  /**
   * Stores the current position of the window as a reference frame for
   * aspect-preserving resizing. Also calculates the size of the window
   * decorations when called the first time.
   */
  void storeReferenceFrame()
  {
    final Rectangle bounds = getBounds();
    if (mReferenceBounds == null) {
      mBorderWidth = bounds.width - mDisplayPane.getWidth();
      mBorderHeight = bounds.height - mDisplayPane.getHeight();
    }
    mReferenceBounds = bounds;
    mPreviousResizeBounds = null;
    mHorizontalAnchor = mVerticalAnchor = ANCHOR_UNKNOWN;
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
      SwingUtilities.invokeLater(new Thread() {
        @Override
        public void run() {
          AutomatonInternalFrame.this.repaint();
        }
      });
      storeReferenceFrame();
    }
  }

  public boolean canResize()
  {
    return Math.abs(mDisplayPane.getSize().getHeight() -
                    mDisplayPane.getPreferredSize().getHeight()) > 10 &&
           Math.abs(mDisplayPane.getSize().getWidth() -
                    mDisplayPane.getPreferredSize().getWidth()) > 10;
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
      if (mReferenceBounds == null ||
          mPreviousResizeBounds == null ||
          mPreviousResizeBounds.width == bounds.width &&
          mPreviousResizeBounds.height == bounds.height &&
          (mPreviousResizeBounds.x != bounds.x ||
           mPreviousResizeBounds.y != bounds.y)) {
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
      if (mReferenceBounds != null && !mDisplayPane.isEmbedderRunning()) {
        final Rectangle bounds = getBounds();
        adjustSize(bounds, true);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Resizes the window to preserve the graph's aspect ratio.
   * @param  newBounds   The new size and position of the frame, including
   *                     window decorations.
   * @param  interactive <CODE>true</CODE> if the resize occurs during
   *                     drag operation by the user, <CODE>false</CODE>
   *                     if called programmatically in response to model
   *                     change.
   */
  private void adjustSize(final Rectangle newBounds,
                          final boolean interactive)
  {
    if (mReferenceBounds == null) {
      // Don't even try if there is no reference frame.
      return;
    } else if (newBounds.equals(mPreviousResizeBounds)) {
      // Also skip if the new bounds are those just set by this method.
      return;
    }
    final int newLeft = newBounds.x;
    final int newRight = newLeft + newBounds.width;
    final int newTop = newBounds.y;
    final int newBottom = newTop + newBounds.height;
    if (mPreviousResizeBounds != null) {
      // Continuing previous resize.
      // Check whether the reference frame has changed. This can be detected
      // if the the latest drag has changed a window edge opposite to an
      // edge changed before.
      final int prevLeft = mPreviousResizeBounds.x;
      final int prevRight = prevLeft + mPreviousResizeBounds.width;
      final int prevTop = mPreviousResizeBounds.y;
      final int prevBottom = prevTop + mPreviousResizeBounds.height;
      if (mHorizontalAnchor == ANCHOR_LEFT && newLeft != prevLeft ||
          mHorizontalAnchor == ANCHOR_RIGHT && newRight != prevRight ||
          mVerticalAnchor == ANCHOR_TOP && newTop != prevTop ||
          mVerticalAnchor == ANCHOR_BOTTOM && newBottom != prevBottom) {
        mReferenceBounds = mPreviousResizeBounds;
        mHorizontalAnchor = mVerticalAnchor = ANCHOR_UNKNOWN;
      }
      // Try to improve on previous guesses of the anchors.
      if (mHorizontalAnchor == ANCHOR_UNKNOWN) {
        if (newRight != prevRight) {
          mHorizontalAnchor = ANCHOR_LEFT;
        } else if (newLeft != prevLeft) {
          mHorizontalAnchor = ANCHOR_RIGHT;
        }
      }
      if (mVerticalAnchor == ANCHOR_UNKNOWN) {
        if (newBottom != prevBottom) {
          mVerticalAnchor = ANCHOR_TOP;
        } else if (newTop != prevTop) {
          mVerticalAnchor = ANCHOR_BOTTOM;
        }
      }
    } else {
      // Starting a new resize. Guess the anchors as best we can.
      final int refLeft = mReferenceBounds.x;
      final int refRight = refLeft + mReferenceBounds.width;
      if (newRight != refRight) {
        mHorizontalAnchor = ANCHOR_LEFT;
      } else if (newLeft != refLeft) {
        mHorizontalAnchor = ANCHOR_RIGHT;
      } else {
        mHorizontalAnchor = ANCHOR_UNKNOWN;
      }
      final int refTop = mReferenceBounds.y;
      final int refBottom = refTop + mReferenceBounds.height;
      if (newBottom != refBottom) {
        mVerticalAnchor = ANCHOR_TOP;
      } else if (newTop != refTop) {
        mVerticalAnchor = ANCHOR_BOTTOM;
      } else {
        mVerticalAnchor = ANCHOR_UNKNOWN;
      }
    }
    if (interactive) {
      updateAnchorsFromMouse();
    }

    // Find how the new size has changed compared to the reference size.
    final int newWidth = newBounds.width;
    final int newHeight = newBounds.height;
    final int refWidth = mReferenceBounds.width;
    final int refHeight = mReferenceBounds.height;
    final int widthChange = Math.abs(newWidth - refWidth);
    final int heightChange = Math.abs(newHeight - refHeight);
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
    final Rectangle finalBounds = new Rectangle();
    finalBounds.width = newAutomatonWidth + mBorderWidth;
    finalBounds.height = newAutomatonHeight + mBorderHeight;
    // If the calculated size is the same as the current size, then cancel.
    if (newBounds.width == finalBounds.width &&
        newBounds.height == finalBounds.height) {
      mPreviousResizeBounds = newBounds;
      return;
    }
    // Calculate the new window position depending on anchors.
    if (mHorizontalAnchor != ANCHOR_RIGHT) {
      finalBounds.x = mReferenceBounds.x;
    } else {
      finalBounds.x = mReferenceBounds.x + refWidth - finalBounds.width;
    }
    if (mVerticalAnchor != ANCHOR_BOTTOM) {
      finalBounds.y = mReferenceBounds.y;
    } else {
      finalBounds.y = mReferenceBounds.y + refHeight - finalBounds.height;
    }

    // Set the new position and size with a single call (reduce events).
    setBounds(finalBounds);
    mPreviousResizeBounds = finalBounds;
  }

  /**
   * Tries to guess anchor positions based on the current position of the
   * mouse pointer. If any anchors are unknown, the are set to the frame
   * edges farthest away from the mouse.
   */
  private void updateAnchorsFromMouse()
  {
    if (mHorizontalAnchor == ANCHOR_UNKNOWN ||
        mVerticalAnchor == ANCHOR_UNKNOWN) {
      final PointerInfo info = MouseInfo.getPointerInfo();
      if (info != null) {
        final Point location = info.getLocation();
        SwingUtilities.convertPointFromScreen(location, this);
        if (mHorizontalAnchor == ANCHOR_UNKNOWN) {
          if (location.x < getWidth() >> 1) {
            mHorizontalAnchor = ANCHOR_RIGHT;
          } else {
            mHorizontalAnchor = ANCHOR_LEFT;
          }
        }
        if (mVerticalAnchor == ANCHOR_UNKNOWN) {
          if (location.y < getHeight() >> 1) {
            mVerticalAnchor = ANCHOR_BOTTOM;
          } else {
            mVerticalAnchor = ANCHOR_TOP;
          }
        }
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
  private Rectangle mReferenceBounds = null;
  /**
   * The last position and size assigned to the frame during resize
   * operations. This is used to determine how the frame is being dragged
   * and scale accordingly. May be <CODE>null</CODE> when not resizing.
   */
  private Rectangle mPreviousResizeBounds = null;
  /**
   * The horizontal anchor position while resizing. This is either
   * {@link #ANCHOR_LEFT} when the window is resized by dragging to the right,
   * or {@link #ANCHOR_RIGHT} when the window is resized by dragging to the
   * left, or {@link #ANCHOR_UNKNOWN}.
   */
  private int mHorizontalAnchor = ANCHOR_UNKNOWN;
  /**
   * The vertical anchor position while resizing. This is either
   * {@link #ANCHOR_TOP} when the window is resized by dragging downwards,
   * or {@link #ANCHOR_BOTTOM} when the window is resized by dragging upwards,
   * or {@link #ANCHOR_UNKNOWN}.
   */
  private int mVerticalAnchor = ANCHOR_UNKNOWN;
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

  private static final int ANCHOR_UNKNOWN = 0;
  private static final int ANCHOR_LEFT = 1;
  private static final int ANCHOR_RIGHT = 2;
  private static final int ANCHOR_TOP = 1;
  private static final int ANCHOR_BOTTOM = 2;

}
