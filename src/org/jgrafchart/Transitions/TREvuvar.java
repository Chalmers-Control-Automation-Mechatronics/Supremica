package org.jgrafchart.Transitions;



import com.nwoods.jgo.*;

import org.jgrafchart.*;


public class TREvuvar
	extends TRVar
{

	TREvuvar(int id)
	{
		super(id);
	}

	public void setName(String n)
	{
		super.setName(n.substring(1));
	}

	public String toString()
	{
		return "UpEvent: " + name;
	}

	public boolean evaluate()
	{

		if (dotX)
		{
			return (((GrafcetObject) in).x &&!((GrafcetObject) in).oldx);
		}
		else
		{
			if (dotT)
			{
				return false;
			}
			else
			{
				boolean val = in.getBoolVal() &&!in.getOldBoolVal();

				return val;
			}
		}
	}

	public int intEvaluate()
	{
		return 0;
	}
}
