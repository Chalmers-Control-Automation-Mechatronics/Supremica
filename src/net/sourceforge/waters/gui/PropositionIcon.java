//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   PropositionIcon
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

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
    //# Auxiliary Methods
    public PropositionIcon getIcon()
    {
      if (mForbidden) {
        return new PropositionIcon(this);
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
  private static final int SIZE = IconLoader.ICON_EVENT.getIconHeight();
  private static final int OFFSET = SIZE >> 2;
  private static final int DIAMETER = SIZE - OFFSET - OFFSET;

  private static ColorInfo MARKED_COLORS;
  private static ColorInfo UNMARKED_COLORS;

  private static PropositionIcon MARKED_ICON;
  private static PropositionIcon UNMARKED_ICON;

}