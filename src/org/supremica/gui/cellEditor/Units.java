package org.supremica.gui.cellEditor;

import java.util.*;

public class Units
{
	protected HashMap theUnits;

	public Units()
	{
		theUnits = new HashMap();
	}

	public void addUnit(Unit theUnit)
	{
		theUnits.put(theUnit.getName(), theUnit);
	}

	public void removeUnit(Unit theUnit)
	{
		theUnits.remove(theUnit.getName());
	}

	public Iterator iterator()
	{
		return theUnits.values().iterator();
	}
}
