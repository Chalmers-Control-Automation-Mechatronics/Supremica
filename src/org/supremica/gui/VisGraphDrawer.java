//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VisGraphDrawer
	extends JFrame
{
    private static final long serialVersionUID = 1L;
	/** The logger */
	private static Logger logger = LogManager.getLogger(VisGraphDrawer.class);

	private static final int OFFSET = 50;
	private final int DRAWABLE_AREA;
	private final int FLETCH_SPREAD = 5;

	private final ArrayList<GraphicalZone> zones = new ArrayList<GraphicalZone>();
	private final ArrayList<int[]> paths = new ArrayList<int[]>();

	private final double[] xyRange = new double[2];
	private String[] robotNames;

	public VisGraphDrawer(final int width, final int height)
	{
		super("Visibility Graph");

		DRAWABLE_AREA = Math.min(height, width) - 3 * OFFSET;

		setSize(width, height);
		setVisible(true);
	}

	public VisGraphDrawer(final int width, final int height, final String[] robotNames)
	{
		this(width, height);

		this.robotNames = new String[robotNames.length];
		for (int i=0; i<robotNames.length; i++)
		{
			this.robotNames[i] = robotNames[i];
		}
	}

	@Override
  public void paint(final Graphics g)
	{
		setBackground(Color.WHITE);
		g.setColor(Color.BLACK);

		//Coordinate axes (x)
		g.drawLine(OFFSET, getHeight() - OFFSET, getWidth() - OFFSET, getHeight() - OFFSET);
		g.drawLine(getWidth() - OFFSET, getHeight() - OFFSET, getWidth() - OFFSET - FLETCH_SPREAD, getHeight() - OFFSET - FLETCH_SPREAD);
		g.drawLine(getWidth() - OFFSET, getHeight() - OFFSET, getWidth() - OFFSET - FLETCH_SPREAD, getHeight() - OFFSET + FLETCH_SPREAD);

		//Coordinate axes (y)
		g.drawLine(OFFSET, getHeight() - OFFSET, OFFSET, OFFSET);
		g.drawLine(OFFSET, OFFSET, OFFSET - FLETCH_SPREAD, OFFSET + FLETCH_SPREAD);
		g.drawLine(OFFSET, OFFSET, OFFSET + FLETCH_SPREAD, OFFSET + FLETCH_SPREAD);

		//The names of the coordinate axis
		if (robotNames != null)
		{
			g.drawString(robotNames[0], getWidth() - OFFSET + FLETCH_SPREAD, getHeight() - OFFSET - FLETCH_SPREAD);
			g.drawString(robotNames[1], OFFSET + FLETCH_SPREAD, OFFSET - FLETCH_SPREAD);
		}

		//The goal cross
		final int xLimitPixel = getWidth() - 2 * OFFSET;
		final int yLimitPixel = 2 * OFFSET;

		g.drawLine(xLimitPixel, getHeight() - OFFSET - FLETCH_SPREAD, xLimitPixel, getHeight() - OFFSET + FLETCH_SPREAD);
		g.drawLine(OFFSET - FLETCH_SPREAD, yLimitPixel, OFFSET + FLETCH_SPREAD, yLimitPixel);
		g.drawLine(xLimitPixel - FLETCH_SPREAD, yLimitPixel + FLETCH_SPREAD, xLimitPixel + FLETCH_SPREAD, yLimitPixel - FLETCH_SPREAD);
		g.drawLine(xLimitPixel - FLETCH_SPREAD, yLimitPixel - FLETCH_SPREAD, xLimitPixel + FLETCH_SPREAD, yLimitPixel + FLETCH_SPREAD);

		//The goal coordinates
		g.drawString("" + xyRange[0], xLimitPixel - 2 * FLETCH_SPREAD, getHeight() - OFFSET + 4 * FLETCH_SPREAD);
		g.drawString("" + xyRange[1], OFFSET - 7 * FLETCH_SPREAD, yLimitPixel);

		//Zone graphics
		for (int i=0; i<zones.size(); i++)
		{
			final GraphicalZone currZone = zones.get(i);

			//Zones
			g.setColor(Color.RED);
			g.fillPolygon(currZone);

			//Their names
			g.setColor(Color.BLACK);
			g.drawString(currZone.getName(), currZone.getXPixels()[0] + FLETCH_SPREAD, currZone.getYPixels()[0] - FLETCH_SPREAD);

			//Coordinate marqueurs
			g.drawLine(currZone.getXPixels()[0], getHeight() - OFFSET - FLETCH_SPREAD, currZone.getXPixels()[0], getHeight() - OFFSET + FLETCH_SPREAD);
			g.drawLine(OFFSET - FLETCH_SPREAD, currZone.getYPixels()[0], OFFSET + FLETCH_SPREAD, currZone.getYPixels()[0]);
			g.drawLine(currZone.getXPixels()[1], getHeight() - OFFSET - FLETCH_SPREAD, currZone.getXPixels()[1], getHeight() - OFFSET + FLETCH_SPREAD);
			g.drawLine(OFFSET - FLETCH_SPREAD, currZone.getYPixels()[1], OFFSET + FLETCH_SPREAD, currZone.getYPixels()[1]);

			//Coordinate values
			g.drawString("" + currZone.getXCoords()[0], currZone.getXPixels()[0] - 2 * FLETCH_SPREAD, getHeight() - OFFSET + 4 * FLETCH_SPREAD);
			g.drawString("" + currZone.getXCoords()[1], currZone.getXPixels()[1] - 2 * FLETCH_SPREAD, getHeight() - OFFSET + 4 * FLETCH_SPREAD);
			g.drawString("" + currZone.getYCoords()[0], OFFSET - 7 * FLETCH_SPREAD, currZone.getYPixels()[0]);
			g.drawString("" + currZone.getYCoords()[1], OFFSET - 7 * FLETCH_SPREAD, currZone.getYPixels()[1]);
		}

		//Optimal paths
		for (int i=0; i<paths.size(); i++)
		{
			final int[] currPath = paths.get(i);

			g.setColor(Color.BLUE);
			//g.setFont(Font.BOLD); //PLAIN
			g.drawLine(currPath[0], currPath[1], currPath[2], currPath[3]);
		}
	}

	public void addZone(final double[] xCoords, final double[] yCoords, final String zoneName)
		throws Exception
	{
		try
		{
			if (xCoords != null && yCoords != null)
			{
				zones.add(new GraphicalZone(xCoords, yCoords, zoneName, this));
			}
		}
		catch (final Exception ex)
		{
			logger.error("Exception in VisGraphDrawer.addZone() ---> " + ex.getMessage());
			throw ex;
		}
	}

	public void addPath(final double[] pathStart, final double[] pathEnd)
		throws Exception
	{
		try
		{
			final int[] path = new int[pathStart.length + pathEnd.length];

			path[0] = toXPixels(pathStart[0]);
			path[1] = toYPixels(pathStart[1]);
			path[2] = toXPixels(pathEnd[0]);
			path[3] = toYPixels(pathEnd[1]);

			paths.add(path);
		}
		catch (final Exception ex)
		{
			logger.error("Exception in VisGraphDrawer.addPath() ---> " + ex.getMessage());
			throw ex;
		}
	}

	public int toXPixels(final double x)
	{
		return (int) Math.round(x / xyRange[0] * DRAWABLE_AREA) + OFFSET;
	}

	public int toYPixels(final double y)
	{
		return getHeight() - (int) Math.round(y / xyRange[1] * DRAWABLE_AREA) - OFFSET;
	}

	public double[] getXYRange()
	{
		return xyRange;
	}

	public void setXYRange(final double[] xyRange)
	{
		this.xyRange[0] = xyRange[0];
		this.xyRange[1] = xyRange[1];
	}
}

class GraphicalZone
	extends Polygon
{
    private static final long serialVersionUID = 1L;

	private final double[] xCoords = new double[2];
	private final double[] yCoords = new double[2];
	private final int[] xPixels = new int[2];
	private final int[] yPixels = new int[2];
	private String name = "";

	GraphicalZone()
	{
		super();
	}

	GraphicalZone(final double[] xCoords, final double[] yCoords, final String name, final VisGraphDrawer drawer)
	{
		this();

		xPixels[0] = drawer.toXPixels(xCoords[0]);
		xPixels[1] = drawer.toXPixels(xCoords[1]);
		yPixels[0] = drawer.toYPixels(yCoords[0]);
		yPixels[1] = drawer.toYPixels(yCoords[1]);

		this.xCoords[0] = xCoords[0];
		this.xCoords[1] = xCoords[1];
		this.yCoords[0] = yCoords[0];
		this.yCoords[1] = yCoords[1];

		this.name = name;

		addPoint(xPixels[0], yPixels[0]);
		addPoint(xPixels[0], yPixels[1]);
		addPoint(xPixels[1], yPixels[1]);
		addPoint(xPixels[1], yPixels[0]);
	}

	public double[] getXCoords()
	{
		return xCoords;
	}

	public double[] getYCoords()
	{
		return yCoords;
	}

	public int[] getXPixels()
	{
		return xPixels;
	}

	public int[] getYPixels()
	{
		return yPixels;
	}

	public String getName()
	{
		return name;
	}
}
