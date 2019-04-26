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

package net.sourceforge.waters.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;


/**
 * A custom icon showing a circle with customisable colour.
 * Used for proposition events to show the proposition colour
 * chosen by the user.
 *
 * @author Robi Malik
 */

public class PropositionIcon implements Icon
{

  //#########################################################################
  //# Factory Methods
  public static PropositionIcon getIcon(final Color color)
  {
    if (color.equals(EditorColor.DEFAULTMARKINGCOLOR)) {
      return getDefaultMarkedIcon();
    } else {
      return new PropositionIcon(color);
    }
  }

  public static PropositionIcon getUnmarkedIcon()
  {
    if (UNMARKED_ICON == null) {
      final ColorInfo info = getUnmarkedColors();
      UNMARKED_ICON = new PropositionIcon(info);
    }
    return UNMARKED_ICON;
  }

  public static PropositionIcon getDefaultMarkedIcon()
  {
    if (MARKED_ICON == null) {
      final ColorInfo info = getDefaultMarkedColors();
      MARKED_ICON = new PropositionIcon(info);
    }
    return MARKED_ICON;
  }

  public static ColorInfo getNeutralColors()
  {
    if (NEUTRAL_COLORS == null) {
      NEUTRAL_COLORS = new ColorInfo(null, false);
    }
    return NEUTRAL_COLORS;
  }

  public static ColorInfo getUnmarkedColors()
  {
    if (UNMARKED_COLORS == null) {
      UNMARKED_COLORS = new ColorInfo(EditorColor.BACKGROUNDCOLOR);
    }
    return UNMARKED_COLORS;
  }

  public static ColorInfo getDefaultMarkedColors()
  {
    if (MARKED_COLORS == null) {
      MARKED_COLORS = new ColorInfo(EditorColor.DEFAULTMARKINGCOLOR);
    }
    return MARKED_COLORS;
  }


  //#########################################################################
  //# Constructors
  private PropositionIcon(final Color color)
  {
    this(new ColorInfo(color));
  }

  private PropositionIcon(final ColorInfo info)
  {
    mColorInfo = info;
  }


  //#########################################################################
  //# Interface javax.swing.Icon
  public int getIconHeight()
  {
    return SIZE;
  }

  public int getIconWidth()
  {
    return SIZE;
  }

  public void paintIcon(final Component comp, final Graphics graphics,
                        final int x, final int y)
  {
    final Graphics2D g2d = (Graphics2D) graphics;
    final int x0 = x + OFFSET;
    final int y0 = y + OFFSET;
    final Rectangle2D bounds =
      new Rectangle(x0, y0, DIAMETER, DIAMETER);
    SimpleNodeProxyShape.drawNode(g2d, bounds, mColorInfo.getColors());
    g2d.setColor(EditorColor.DEFAULTCOLOR);
    g2d.drawOval(x0, y0, DIAMETER, DIAMETER);
    if (mColorInfo.isForbidden()) {
      SimpleNodeProxyShape.drawForbidden(g2d, bounds);
    }
  }


  //#######################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mColorInfo.toString();
  }


  //#########################################################################
  //# Inner Class ColorInfo
  public static class ColorInfo {

    //#######################################################################
    //# Constructors
    public ColorInfo(final Color color)
    {
      this(Collections.singletonList(color), false);
    }

    public ColorInfo(final List<Color> list, final boolean forbidden)
    {
      mList = list;
      mForbidden = forbidden;
    }

    //#######################################################################
    //# Simple Access
    public List<Color> getColors()
    {
      return mList;
    }

    public boolean isForbidden()
    {
      return mForbidden;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    public String toString()
    {
      if (mForbidden) {
        return mList.toString() + ",forbidden";
      } else {
        return mList.toString();
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    public PropositionIcon getIcon()
    {
      if (mForbidden) {
        return new PropositionIcon(this);
      } else if (mList == null) {
        return null;
      } else if (mList.isEmpty()) {
        return getUnmarkedIcon();
      } else if (mList.size() == 1 &&
          mList.iterator().next().equals(EditorColor.DEFAULTMARKINGCOLOR)) {
        return getDefaultMarkedIcon();
      } else {
        return new PropositionIcon(this);
      }
    }


    //#######################################################################
    //# Data Members
    private final List<Color> mList;
    private final boolean mForbidden;
  }


  //#########################################################################
  //# Data Members
  private final ColorInfo mColorInfo;


  //#########################################################################
  //# Static Class Constants
  private static final int SIZE = IconAndFontLoader.ICON_EVENT.getIconHeight();
  private static final int OFFSET = SIZE >> 2;
  private static final int DIAMETER = SIZE - OFFSET - OFFSET;

  private static ColorInfo MARKED_COLORS;
  private static ColorInfo NEUTRAL_COLORS;
  private static ColorInfo UNMARKED_COLORS;

  private static PropositionIcon MARKED_ICON;
  private static PropositionIcon UNMARKED_ICON;

}
