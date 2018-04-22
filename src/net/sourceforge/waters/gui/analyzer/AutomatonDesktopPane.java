//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JDesktopPane;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDesktopPane
  extends JDesktopPane
  implements Observer
{

  //#########################################################################
  //# Constructor
  public AutomatonDesktopPane(final ModuleContainer container)
  {
    mContainer = container;
    order = new ArrayList<String>();
    setBackground(EditorColor.BACKGROUNDCOLOR);
    container.attach(this);
    addMouseListener(new MouseListener(){
      @Override
      public void mouseClicked(final MouseEvent e)
      {
      }

      @Override
      public void mouseEntered(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mouseExited(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mousePressed(final MouseEvent event)
      {
      }

      @Override
      public void mouseReleased(final MouseEvent event)
      {
      }
    });
  }


  //#########################################################################
  //# Simple Access

  @SuppressWarnings("unused")
  private Point findLocation(final ArrayList<Rectangle> bannedLocations, final Dimension newSize)
  {
    final ArrayList<Rectangle> bannedRegions = new ArrayList<Rectangle>();
    final ArrayList<Rectangle> otherScreens = bannedLocations;
    for (int y = 0; y < this.getHeight() - newSize.getHeight(); y++)
    {
      for (int x = 0; x < this.getWidth() - newSize.getWidth(); x++)
      {
        boolean failed = false;
        final Rectangle2D thisFrame = new Rectangle(x, y, (int)newSize.getWidth(), (int)newSize.getHeight());
        final Area thisArea = new Area(thisFrame);
        for (final Rectangle screen : otherScreens)
        {
          if (thisArea.intersects(screen))
          {
            final Rectangle newBanned = new Rectangle
            (x, y, (int) (screen.getWidth() + (screen.getX() - thisArea.getBounds().getX()))
                , (int) (screen.getHeight() + (screen.getY() - thisArea.getBounds().getY())));
            bannedRegions.add(newBanned);
          }
        }
        for (final Rectangle banned : bannedRegions)
        {
          if (thisArea.intersects(banned))
          {
            x = (int) (banned.getX() + banned.getWidth());
            failed = true;
          }
        }
        if (!failed)
          return new Point(x, y);
      }
    }
    for (int coords = 0; coords < Math.min(this.getHeight(), this.getWidth()); coords += 30)
    {
      boolean fail = false;
      for (final Rectangle rect : otherScreens)
      {
        if (Math.abs(rect.getX() - coords) < SCREENS_TOO_CLOSE && Math.abs(rect.getY() - coords) < SCREENS_TOO_CLOSE)
          fail = true;
      }
      if (!fail)
        return new Point(coords, coords);
    }
    return new Point(0,0);
  }


  //#########################################################################
  //# Interface Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mContainer.getActivePanel() instanceof WatersAnalyzerPanel &&
        mContainer.getCompiledDES() != null) {
      //onReOpen();
    }
  }

  //#########################################################################
  //# Data Members
  @SuppressWarnings("unused")
  private final ArrayList<String> order;
  private final ModuleContainer mContainer;


  //#########################################################################
  //# Class Constants
  private static final double SCREENS_TOO_CLOSE = 3;
  private static final long serialVersionUID = -5528014241244952875L;

}
