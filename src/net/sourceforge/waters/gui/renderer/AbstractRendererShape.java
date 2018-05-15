//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;


public abstract class AbstractRendererShape
  implements RendererShape
{

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  @Override
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw shadow if focused
    if (status.isFocused()) {
      g.setColor(status.getShadowColor());
      g.setStroke(SHADOWSTROKE);
      g.draw(getShape());
    }

    // Draw shape
    g.setColor(status.getColor());
    g.setStroke(BASICSTROKE);
    g.draw(getShape());
  }

  @Override
  public Rectangle2D getBounds2D()
  {
    return getShape().getBounds2D();
  }

  @Override
  public boolean isClicked(final Point point)
  {
    return getShape().contains(point);
  }


  //#########################################################################
  //# Static Class Methods
  public static void setBasicStroke(final Stroke stroke)
  {
    BASICSTROKE = stroke;
  }


  //#########################################################################
  //# Class constants
  /** Single line width, used as default when painting on screen. */
  public static final Stroke SINGLESTROKE = new BasicStroke();
  /** Double line width, used for nodegroup border. */
  public static final Stroke DOUBLESTROKE = new BasicStroke(2);
  /** Thick line used for drawing shadows. */
  public static final Stroke SHADOWSTROKE = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  /** Used as the basic stroke when printing--thinner than ordinary lines. */
  //public static final Stroke THINSTROKE = new BasicStroke(0.25f); // Too thin
  //public static final Stroke THINSTROKE = new BasicStroke(0.5f); // Also too thin
  /**
   * The default pen size. <STRONG>BUG</STRONG> Is not <CODE>final</CODE>
   * since it changes when printing --- so needs to be part of renderer.
   */
  public static Stroke BASICSTROKE = SINGLESTROKE;
}
