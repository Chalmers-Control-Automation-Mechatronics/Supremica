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

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


/**
 * <P>A peculiar type of label block to support blocked events list.
 * In addition to the normal label block, it displays a title in
 * a given font on top of the list.</P>
 *
 * @author Simon Ware
 */

public class TitledLabelBlockProxyShape
  extends LabelBlockProxyShape
{

  //#########################################################################
  //# Constructor
  public TitledLabelBlockProxyShape(final LabelBlockProxy block,
                                    final RoundRectangle2D bounds,
                                    final String title,
                                    final Font font)
  {
    super(block, bounds);
    mTitle = title;
    mFont = font;
    final Rectangle2D titleBounds = getTitleBounds();
    mUnderline = new UnderlineShape(block, titleBounds.getMinX(),
        titleBounds.getMaxY(), titleBounds.getWidth(), font);
  }


  //#########################################################################
  //# Simple Access
  @Override
  boolean shouldBeDrawn(){
    return true;
  }

  @Override
  public Rectangle2D getBounds2D()
  {
    return getTitleBounds();
  }

  @Override
  public boolean isClicked(final int x, final int y)
  {
    return super.isClicked(x, y) || getTitleBounds().contains(x, y);
  }


  //#########################################################################
  //# Drawing
  @Override
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    super.draw(g, status);
    final Rectangle2D titleBounds = getTitleBounds();
    final int x = (int) titleBounds.getMinX();
    final int y = (int) titleBounds.getMaxY();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                       RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setFont(mFont);
    g.setStroke(BASICSTROKE);
    g.setColor(status.getColor());
    g.drawString(mTitle, x, y);
    mUnderline.draw(g, status);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Rectangle2D getTitleBounds()
  {
    final Rectangle2D shapeBounds = getShape().getBounds2D();
    final double x = shapeBounds.getX();
    final double y = shapeBounds.getY();
    final FontRenderContext context =
      new FontRenderContext(mFont.getTransform(), false, false);
    final Rectangle2D titleBounds = mFont.getStringBounds(mTitle, context);
    final double width = titleBounds.getWidth();
    final double height = titleBounds.getHeight();
    titleBounds.setRect(x, y - height - 2, width, height);
    return titleBounds;
  }


  //#########################################################################
  //# Data Members
  private final String mTitle;
  private final Font mFont;
  private final UnderlineShape mUnderline;

}
