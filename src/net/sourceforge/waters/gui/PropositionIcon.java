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
      return getDefaultIcon();
    } else {
      return new PropositionIcon(color);
    }
  }

  static PropositionIcon getDefaultIcon()
  {
    if (DEFAULT_ICON == null) {
      DEFAULT_ICON = new PropositionIcon(EditorColor.DEFAULTMARKINGCOLOR);
    }
    return DEFAULT_ICON;
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
    graphics.setColor(Color.BLACK);
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

  private static PropositionIcon DEFAULT_ICON;

}