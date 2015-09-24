//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.GuardActionBlockProxy;


public class GuardActionBlockProxyShape
    extends AbstractProxyShape
{
    public GuardActionBlockProxyShape(final GuardActionBlockProxy block, final RoundRectangle2D bounds)
    {
        super(block);
        mBlock = block;
        mBounds = bounds;
    }

    public GuardActionBlockProxy getProxy()
    {
        return (GuardActionBlockProxy)super.getProxy();
    }

    public RoundRectangle2D getShape()
    {
        return mBounds;
    }

    public void draw(final Graphics2D g, final RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (status.isFocused())
        {
            g.setColor(status.getShadowColor());
            g.setStroke(SHADOWSTROKE);
            g.fill(getShape());
        }
        if (status.isSelected()) {
          g.setColor(status.getColor());
          g.setStroke(BASICSTROKE);
          g.draw(getShape());
        }
    }

    private final RoundRectangle2D mBounds;
    GuardActionBlockProxy mBlock;
    public static final int DEFAULTARCW = 8;
    public static final int DEFAULTARCH = 8;
    public static final int DEFAULTOFFSETX = 0;
    public static final int DEFAULTOFFSETY = 10;
}
