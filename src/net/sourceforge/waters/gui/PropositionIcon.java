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
import java.util.List;

import javax.swing.Icon;


/**
 * A custom icon showing a circle with customisable colour.
 * Used for proposition events to show the proposition colour
 * chosen by the user.
 *
 * @author Robi Malik
 */

class PropositionIcon implements Icon
{

  //#########################################################################
  //# Factory Methods
  static PropositionIcon getIcon(final Color color)
  {
    if (color.equals(EditorColor.DEFAULTMARKINGCOLOR)) {
      return getDefaultMarkedIcon();
    } else {
      return new PropositionIcon(color);
    }
  }

  static PropositionIcon getIcon(final List<Color> colors)
  {
    if (colors.isEmpty()) {
      return getUnmarkedIcon();
    } else if (colors.size() == 1 &&
            colors.iterator().next().equals(EditorColor.DEFAULTMARKINGCOLOR)) {
      return getDefaultMarkedIcon();
    } else {
      return null;
    }
  }

  static PropositionIcon getUnmarkedIcon()
  {
    if (UNMARKED_ICON == null) {
      UNMARKED_ICON = new PropositionIcon(EditorColor.BACKGROUNDCOLOR);
    }
    return UNMARKED_ICON;
  }

  static PropositionIcon getDefaultMarkedIcon()
  {
    if (MARKED_ICON == null) {
      MARKED_ICON = new PropositionIcon(EditorColor.DEFAULTMARKINGCOLOR);
    }
    return MARKED_ICON;
  }


  //#########################################################################
  //# Constructor
  private PropositionIcon(final Color color)
  {
    mColor = color;
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
    graphics.setColor(mColor);
    graphics.fillOval(x + OFFSET, y + OFFSET, DIAMETER, DIAMETER);
    graphics.setColor(EditorColor.DEFAULTCOLOR);
    graphics.drawOval(x + OFFSET, y + OFFSET, DIAMETER, DIAMETER);
  }


  //#########################################################################
  //# Data Members
  private final Color mColor;


  //#########################################################################
  //# Static Class Constants
  private static final int SIZE = IconLoader.ICON_EVENT.getIconHeight();
  private static final int OFFSET = SIZE >> 2;
  private static final int DIAMETER = SIZE - OFFSET - OFFSET;

  private static PropositionIcon MARKED_ICON;
  private static PropositionIcon UNMARKED_ICON;

}