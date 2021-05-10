//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;


public class UnderlineShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructor
  public UnderlineShape(final Proxy proxy,
                        final double x,
                        final double y,
                        final double width)
  {
    super(proxy);
    mLine = new Line2D.Double(x, y, x + width, y);
  }


  //##########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.AbstractRendererShape
  @Override
  public void draw(final Graphics2D graphics,final RenderingInformation status)
  {
    if (status.isUnderlined()) {
      graphics.setColor(EditorColor.ERRORCOLOR);
      graphics.setStroke(UNDERLINESTROKE);
      graphics.draw(mLine);
    }
  }

  @Override
  public Shape getShape()
  {
    return mLine;
  }


  //##########################################################################
  //# Data Members
  private final Line2D.Double mLine;


  //##########################################################################
  //# Class Constants
  private final float[] DASHES = new float[] { 2, 4 };
  private final Stroke UNDERLINESTROKE = new BasicStroke(1,
          BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, DASHES, 0);

}
