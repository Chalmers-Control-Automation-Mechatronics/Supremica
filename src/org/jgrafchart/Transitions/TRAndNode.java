package org.jgrafchart.Transitions;

import com.nwoods.jgo.*;
import java.util.*;

public class TRAndNode
	extends SimpleNode
{
	TRAndNode(int id)
	{
		super(id);
	}

	public boolean evaluate()
	{
		boolean res;

		res = jjtGetChild(0).evaluate() && jjtGetChild(1).evaluate();

		return res;
	}

	public int intEvaluate()
	{
		return 0;
	}

	public boolean compile(ArrayList doc)
	{
		boolean result = true;

		result = result && jjtGetChild(0).compile(doc);
		result = result && jjtGetChild(1).compile(doc);

		return result;
	}
}
