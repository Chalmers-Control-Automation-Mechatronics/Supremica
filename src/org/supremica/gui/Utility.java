//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

/**
 * The utility class that contains the functions that do the real job.
 *
 * @author Martin Fabian
 */

public class Utility
{

    /**
     * Returns a point for the upper left corner of a centred component of size comp_d
     */
    public static Point getPosForCenter(final Dimension comp_d)
    {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		final Window windowFrame = kfm.getFocusedWindow();
		Point centerPoint = null;
		if (windowFrame != null)
		{
			final Point frameTopLeft = windowFrame.getLocation();
			final Dimension frameSize = windowFrame.getSize();
			final int x = (int)(frameTopLeft.getX() + (frameSize.width/2) - (comp_d.width/2));
			final int y = (int)(frameTopLeft.getY() + (frameSize.height/2) - (comp_d.height/2));
			centerPoint = new Point(x, y);
		}
		else
		{
        	final Toolkit tool_kit = Toolkit.getDefaultToolkit();
        	final Dimension screen_d = tool_kit.getScreenSize();
        	centerPoint = new Point((screen_d.width - comp_d.width) / 2, (screen_d.height - comp_d.height) / 2);
		}
		return centerPoint;
    }

    public static void setupFrame(final JFrame frame, final int width, final int height)
    {
        frame.setSize(width, height);    // from Component
        frame.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component
        final List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
        frame.setIconImages(images);
    }

    public static void setupFrame(final JFrame frame, final Dimension dimension)
    {
        setupFrame(frame, (int)dimension.getWidth(), (int)dimension.getHeight());
    }

    public static void setupPane(final JScrollPane pane)
    {
        pane.getViewport().setBackground(Color.white);
    }

    public static void setupDialog(final JDialog dialog, final int width, final int height)
    {
        dialog.setSize(width, height);    // from Component
        dialog.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component

        // dialog.setIconImage(Supremica.cornerImage);    // from Frame
    }

    public static JButton setDefaultButton(final JFrame frame, final JButton b)
    {
        frame.getRootPane().setDefaultButton(b);

        return b;
    }

    public static JButton setDefaultButton(final JDialog dialog, final JButton b)
    {
        dialog.getRootPane().setDefaultButton(b);

        return b;
    }

    public static JButton setDisabledButton(final JFrame frame, final JButton b)
    {
        b.setEnabled(false);

        return b;
    }
}
