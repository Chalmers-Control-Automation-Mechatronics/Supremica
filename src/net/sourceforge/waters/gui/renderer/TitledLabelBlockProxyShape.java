//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


/**
 * <P>A peculiar type of label block to support blocked events lists.
 * In addition to the normal label block, it displays a bold title in
 * a given font on top of the list.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public class TitledLabelBlockProxyShape
  extends LabelBlockProxyShape
{

  //#########################################################################
  //# Constructor
  static TitledLabelBlockProxyShape createShape(final LabelBlockProxy block,
                                                final Rectangle2D textBounds,
                                                final String title,
                                                final Font font,
                                                final double baseLineSkip)
  {
    final double x0 = textBounds.getX() - LabelBlockProxyShape.INSETS;
    final double y0 =
      textBounds.getY() - LabelBlockProxyShape.INSETS - baseLineSkip;
    final LabelShape titleShape = new LabelShape(block, x0, y0, title, font);
    final Rectangle2D titleBounds = titleShape.getBounds2D();
    final double titleWidth = titleBounds.getWidth();
    final double margins = 2 * LabelBlockProxyShape.INSETS;
    if (titleWidth > textBounds.getWidth() + margins) {
      textBounds.setRect(textBounds.getX(), textBounds.getY(),
                         titleWidth - margins, textBounds.getHeight());
    }
    return new TitledLabelBlockProxyShape(block, textBounds, titleShape);
  }

  private TitledLabelBlockProxyShape(final LabelBlockProxy block,
                                     final Rectangle2D textBounds,
                                     final LabelShape titleShape)
  {
    super(block, textBounds);
    mTitleShape = titleShape;
    final Rectangle2D titleBounds = titleShape.getBounds2D();
    final Rectangle2D labelBounds = super.getBounds2D();
    mCombinedBounds = new Rectangle2D.Double();
    Rectangle2D.union(titleBounds, labelBounds, mCombinedBounds);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.LabelBlockProxyShape
  @Override
  boolean shouldBeDrawn()
  {
    return true;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  @Override
  public Rectangle2D getBounds2D()
  {
    return mCombinedBounds;
  }

  @Override
  public boolean isClicked(final Point point)
  {
    return super.isClicked(point) || mTitleShape.isClicked(point);
  }

  @Override
  public void draw(final Graphics2D graphics,
                   final RenderingInformation status)
  {
    super.draw(graphics, status);
    mTitleShape.draw(graphics, status);
  }


  //#########################################################################
  //# Data Members
  private final LabelShape mTitleShape;
  private final Rectangle2D mCombinedBounds;


  //#########################################################################
  //# Class Constants
  public static final String BLOCKED_HEADER = "BLOCKED:";

}
