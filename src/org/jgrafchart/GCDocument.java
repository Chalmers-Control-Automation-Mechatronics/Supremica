package org.jgrafchart;



import java.awt.datatransfer.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.*;

import com.nwoods.jgo.*;


public class GCDocument
	extends JGoDocument
	implements Referencable
{

	private String myName = "";
	private String myLocation = "";
	public boolean simulation = true;
	public int threadSpeed = 40;
	public static final int NAME_CHANGED = JGoDocumentEvent.LAST + 1;
	public boolean dimming = false;
	public int dimTicks = 25;
	public Rectangle bounds = null;
	public double currentScale = 1.0;
	private String myReadLocation = "";
	private String myWriteLocation = "";

	public GCDocument() {}

	public void setName(String newname)
	{

		String oldName = getName();

		if (!oldName.equals(newname))
		{
			myName = newname;

			fireUpdate(NAME_CHANGED, 0, null, 0, oldName);
		}
	}

	public String getName()
	{
		return myName;
	}

	public boolean isSimulating()
	{
		return simulation;
	}

	public int getSpeed()
	{
		return threadSpeed;
	}

	public void setSpeed(int s)
	{
		threadSpeed = s;
	}

	public ArrayList getSymbolTable()
	{

		ArrayList symbolList = new ArrayList();
		JGoListPosition pos = getFirstObjectPos();
		JGoObject obj = getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof Referencable)
			{
				symbolList.add(obj);
			}

			pos = getNextObjectPos(pos);
			obj = getObjectAtPos(pos);
		}

		return symbolList;
	}

	public void propagateDimmingInfo(boolean dim, int i, int j)
	{

		dimming = dim;
		dimTicks = i;
		threadSpeed = j;

		JGoListPosition pos = getFirstObjectPos();
		JGoObject obj = getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) obj;

				ms.myContentDocument.propagateDimmingInfo(dim, i, j);
			}

			pos = getNextObjectPos(pos);
			obj = getObjectAtPos(pos);
		}
	}

	public void setReadFileLocation(String newloc)
	{

		String oldLocation = getReadFileLocation();

		if (!oldLocation.equals(newloc))
		{
			myReadLocation = newloc;

			// fireUpdate(LOCATION_CHANGED, 0, null, 0, oldLocation);
			// 
			// updateLocationModifiable();
		}
	}

	public String getReadFileLocation()
	{
		return myReadLocation;
	}

	public void setWriteFileLocation(String newloc)
	{

		String oldLocation = getWriteFileLocation();

		if (!oldLocation.equals(newloc))
		{
			myWriteLocation = newloc;

			// fireUpdate(LOCATION_CHANGED, 0, null, 0, oldLocation);
			// 
			// updateLocationModifiable();
		}
	}

	public String getWriteFileLocation()
	{
		return myWriteLocation;
	}
}
