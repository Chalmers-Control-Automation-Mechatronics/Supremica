package org.supremica.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import org.supremica.log.*;

public class VisGraphDrawer
	extends JFrame
{
	/** The logger */
	private static Logger logger = LoggerFactory.createLogger(VisGraphDrawer.class);
	
	private static final int OFFSET = 50;
	private final int DRAWABLE_AREA;
	private final int FLETCH_SPREAD = 5;
	
	private ArrayList<GraphicalZone> zones = new ArrayList<GraphicalZone>();
	private ArrayList<int[]> paths = new ArrayList<int[]>();

	private double[] xyRange = new double[2];
	private String[] robotNames;

	public VisGraphDrawer(int width, int height)
	{
		super("Visibility Graph");
		
		DRAWABLE_AREA = Math.min(height, width) - 3 * OFFSET;

		setSize(width, height);
		setVisible(true);
	}

	public VisGraphDrawer(int width, int height, String[] robotNames)
	{
		this(width, height);

		this.robotNames = new String[robotNames.length];
		for (int i=0; i<robotNames.length; i++)
		{
			this.robotNames[i] = robotNames[i];
		}
	}

	public void paint(Graphics g)
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
		int xLimitPixel = getWidth() - 2 * OFFSET;
		int yLimitPixel = 2 * OFFSET;
		
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
			GraphicalZone currZone = zones.get(i);

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
			int[] currPath = paths.get(i);

			g.setColor(Color.BLUE);
			//g.setFont(Font.BOLD); //PLAIN
			g.drawLine(currPath[0], currPath[1], currPath[2], currPath[3]);
		}
	}

	public void addZone(double[] xCoords, double[] yCoords, String zoneName)
		throws Exception 
	{
		try 
		{
			if (xCoords != null && yCoords != null) 
			{
				zones.add(new GraphicalZone(xCoords, yCoords, zoneName, this));
			}
		}
		catch (Exception ex) 
		{
			logger.error("Exception in VisGraphDrawer.addZone() ---> " + ex.getMessage());
			throw ex;
		}
	}

	public void addPath(double[] pathStart, double[] pathEnd)
		throws Exception
	{
		try 
		{
			int[] path = new int[pathStart.length + pathEnd.length];
			
			path[0] = toXPixels(pathStart[0]);
			path[1] = toYPixels(pathStart[1]);
			path[2] = toXPixels(pathEnd[0]);
			path[3] = toYPixels(pathEnd[1]);
			
			paths.add(path);
		}
		catch (Exception ex) 
		{
			logger.error("Exception in VisGraphDrawer.addPath() ---> " + ex.getMessage());
			throw ex;
		}
	}

	public int toXPixels(double x)
	{
		return (int) Math.round(x / xyRange[0] * DRAWABLE_AREA) + OFFSET;
	}

	public int toYPixels(double y)
	{
		return getHeight() - (int) Math.round(y / xyRange[1] * DRAWABLE_AREA) - OFFSET;
	}

	public double[] getXYRange() 
	{
		return xyRange;
	}

	public void setXYRange(double[] xyRange)
	{
		this.xyRange[0] = xyRange[0];
		this.xyRange[1] = xyRange[1];
	}
}

class GraphicalZone 
	extends Polygon
{
	private double[] xCoords = new double[2];
	private double[] yCoords = new double[2];
	private int[] xPixels = new int[2];
	private int[] yPixels = new int[2];
	private String name = "";

	GraphicalZone()
	{
		super();
	}

	GraphicalZone(double[] xCoords, double[] yCoords, String name, VisGraphDrawer drawer)
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