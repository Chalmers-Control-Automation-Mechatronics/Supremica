package org.supremica.external.fbd2smv.isagrafReader;

import java.util.*;

public class VAR
{
	private String index;
	private String name;

	public VAR(String index, String name)
	{
		this.index = index;
		this.name = name;
	}

	public int hashCode()
	{
		return index.hashCode();
	}

	public boolean equals(Object other)
	{
		return index.equals(((VAR) other).index);
	}

	public String getIndex()
	{
		return index;
	}

	public String getName()
	{
		if (name.equals("TRUE"))
		{
			return "1";
		}
		else if (name.equals("FALSE"))
		{
			return "0";
		}
		else
		{
			return name;
		}
	}

	public String toString()
	{
		return "Index: " + index + " Name: " + name;
	}

	public boolean isOutputVariable(List theArcs)
	{
		for (Iterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
		{
			String S = null;
			ARC currARC = (ARC) arcIt.next();

			if (index.equals(String.valueOf(currARC.getTargetIndex())))
			{
				return true;
			}
		}

		return false;
	}
}
