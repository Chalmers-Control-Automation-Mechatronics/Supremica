package org.jgrafchart.Transitions;



import com.nwoods.jgo.*;

import java.util.*;


public class TRConstant
	extends SimpleNode
{

	protected String name;
	protected boolean val;

	TRConstant(int id)
	{
		super(id);
	}

	public void setName(String n)
	{

		name = n;

		if (n.compareTo("false") == 0)
		{
			val = false;
		}
		else
		{
			val = true;
		}
	}

	public String toString()
	{
		return name;
	}

	public boolean evaluate()
	{
		return val;
	}

	public int intEvaluate()
	{

		if (val)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public boolean compile(ArrayList doc)
	{
		return true;
	}
}
