package org.jgrafchart.Transitions;



import com.nwoods.jgo.*;

import java.util.*;


public class TRStart
	extends SimpleNode
{

	TRStart(int id)
	{
		super(id);
	}

	public boolean evaluate()
	{

		boolean res;

		res = jjtGetChild(0).evaluate();

		return res;
	}

	public int intEvaluate()
	{
		return 0;
	}

	public boolean compile(ArrayList doc)
	{
		return jjtGetChild(0).compile(doc);
	}
}
