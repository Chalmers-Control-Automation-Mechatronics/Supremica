package org.supremica.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class VisGraphDrawer
	extends JFrame
{
	private static final int OFFSET = 50;
	private final int DRAWABLE_AREA;
	
	private ArrayList<Polygon> zones = new ArrayList<Polygon>();
	private ArrayList<String> zoneNames = new ArrayList<String>();

	private int[] goalCoords = null; 

	public VisGraphDrawer(int width, int height)
	{
		super("Visibility Graph");
		
		DRAWABLE_AREA = Math.min(height, width) - 3 * OFFSET;

		setSize(width, height);
		setVisible(true);
	}

	public void paint(Graphics g)
	{
		setBackground(Color.WHITE);
		
		int FLETCH_SPREAD = 5;
		//Coordinate axes (x)
		g.drawLine(OFFSET, getHeight() - OFFSET, getWidth() - OFFSET, getHeight() - OFFSET);
		g.drawLine(getWidth() - OFFSET, getHeight() - OFFSET, getWidth() - OFFSET - FLETCH_SPREAD, getHeight() - OFFSET - FLETCH_SPREAD); 
		g.drawLine(getWidth() - OFFSET, getHeight() - OFFSET, getWidth() - OFFSET - FLETCH_SPREAD, getHeight() - OFFSET + FLETCH_SPREAD); 

		//Coordinate axes (y)		
		g.drawLine(OFFSET, getHeight() - OFFSET, OFFSET, OFFSET);
		g.drawLine(OFFSET, OFFSET, OFFSET - FLETCH_SPREAD, OFFSET + FLETCH_SPREAD);
		g.drawLine(OFFSET, OFFSET, OFFSET + FLETCH_SPREAD, OFFSET + FLETCH_SPREAD);

		for (int i=0; i<zones.size(); i++)
		{
			//Zones
			g.setColor(Color.RED);
			g.fillPolygon(zones.get(i));

			//Their names
			g.setColor(Color.BLACK);
			Rectangle rect = zones.get(i).getBounds();
			int x = (int) Math.round(rect.getX());
			int y = (int) Math.round(rect.getY());
			g.drawString(zoneNames.get(i), x + 5, y + 15);

			//Coordinate marqueurs
			g.drawLine(x, getHeight() - OFFSET - FLETCH_SPREAD, x, getHeight() - OFFSET + FLETCH_SPREAD);
			g.drawLine(OFFSET - FLETCH_SPREAD, y, OFFSET + FLETCH_SPREAD, y);
			g.drawLine(x + (int)rect.getWidth(), getHeight() - OFFSET - FLETCH_SPREAD, x + (int)rect.getWidth(), getHeight() - OFFSET + FLETCH_SPREAD);
			g.drawLine(OFFSET - FLETCH_SPREAD, y + (int)rect.getHeight(), OFFSET + FLETCH_SPREAD, y + (int)rect.getHeight());
		}

		if (goalCoords != null)
		{
			g.drawLine(goalCoords[0], getHeight() - OFFSET - FLETCH_SPREAD, goalCoords[0], getHeight() - OFFSET + FLETCH_SPREAD);
			g.drawLine(OFFSET - FLETCH_SPREAD, goalCoords[1], OFFSET + FLETCH_SPREAD, goalCoords[1]);
			g.drawLine(goalCoords[0] - 5, goalCoords[1] + 5, goalCoords[0] + 5, goalCoords[1] - 5);
			g.drawLine(goalCoords[0] - 5, goalCoords[1] - 5, goalCoords[0] + 5, goalCoords[1] + 5);
		}
	}

	private int toXPixels(double x, double xTot)
	{
		return (int) Math.round(x / xTot * DRAWABLE_AREA) + OFFSET;
	}

	private int toYPixels(double y, double yTot)
	{
		return getHeight() - (int) Math.round(y /yTot * DRAWABLE_AREA) - OFFSET;
	}

	public void addZone(double[] xCoords, double[] yCoords, double[] xyTots, String zoneName)
	{
		if (xCoords != null && yCoords != null) 
		{
			Polygon zone = new Polygon();
			
			zone.addPoint(toXPixels(xCoords[0], xyTots[0]), toYPixels(yCoords[0], xyTots[1]));
			zone.addPoint(toXPixels(xCoords[0], xyTots[0]), toYPixels(yCoords[1], xyTots[1]));
			zone.addPoint(toXPixels(xCoords[1], xyTots[0]), toYPixels(yCoords[1], xyTots[1]));
			zone.addPoint(toXPixels(xCoords[1], xyTots[0]), toYPixels(yCoords[0], xyTots[1]));
			
			zones.add(zone);
			zoneNames.add(zoneName);
		}
	}

	public void addGoal(double[] goalTimes, double[] xyTots)
	{
		goalCoords = new int[goalTimes.length];

	    goalCoords[0] = toXPixels(goalTimes[0], xyTots[0]);
	    goalCoords[1] = toXPixels(goalTimes[1], xyTots[1]);
	}
}