
/**Class IlExprOperators provides common constants for
 * representing instruction constants
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Constants;

import java.util.*;

public class IlReturnOperator
{
	private static List theInstructions = new ArrayList();
	private String instruction;

	private IlReturnOperator(String s)
	{
		instruction = s;

		theInstructions.add(this);
	}

	/**iterator gives an iterator over the instruction constants*/
	public static Iterator iterator()
	{
		return theInstructions.iterator();
	}

	/**toString gives a string representation of an instruction*/
	public String toString()
	{
		return instruction;
	}

	public static IlReturnOperator

	// dfsfsd -> il_return_operator
	RET = new IlReturnOperator("RET"), RETC = new IlReturnOperator("RETC"),
	RETCN = new IlReturnOperator("RETCN");

	public static IlReturnOperator getOperator(String s)
		throws IllegalOperatorException
	{
		if (s.equals("RET"))
		{
			return RET;
		}
		else if (s.equals("RETC"))
		{
			return RETC;
		}
		else if (s.equals("RETCN"))
		{
			return RETCN;
		}

		throw new IllegalOperatorException();
	}
}
