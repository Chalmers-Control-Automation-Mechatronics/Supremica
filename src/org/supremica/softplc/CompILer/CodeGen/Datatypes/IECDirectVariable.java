package org.supremica.softplc.CompILer.CodeGen.Datatypes;

/**This class is used to represent Direct variables
 * @see "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders Röding
 */
public class IECDirectVariable
	implements IECVariable
{
	private String name;
	private boolean inputVariable;    /* is it an input or output variable?
																	   * memory direct variables are not allowed
																	   */
	private TypeConstant type = TypeConstant.T_BOOL;

	/* only allowed; this implementation
	 * only allows BOOL direct variables
	 */
	private int number;    // direct variable number

	// should be a list to represent the "dot"-notation
	public IECDirectVariable(String s)
	{
		name = s;

		if (s.regionMatches(false, 0, "%IX", 0, 3))
		{
			inputVariable = true;
			number = Integer.parseInt(s.substring(3), 10);
		}
		else if (s.regionMatches(false, 0, "%QX", 0, 3))
		{
			inputVariable = false;
			number = Integer.parseInt(s.substring(3), 10);
		}
		else
		{
			System.err.println("Error in direct(?) variable: " + s);
		}
	}

	/**
	 * @return the number/address to this direct variable.
	 */
	public int getNumber()
	{
		return number;
	}

	/**
	 * @return true if this direct variable is declared as input var; false otherwise.
	 */
	public boolean isInput()
	{
		return inputVariable;
	}

	/**Direct variables must be booleans in this implementation
	 * getType returns the corresponding type constant of an elementary type
	 *  @return the type constant
	 */
	public TypeConstant getType()
	{
		return type;
	}

	public String toString()
	{
		return name;
	}
}
