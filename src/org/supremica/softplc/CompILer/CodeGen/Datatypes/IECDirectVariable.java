
/**This class is used to represent Direct variables
 * IEC 61131-3
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public class IECDirectVariable
	implements IECVariable
{

	/**Direct variables must be booleans in this implementation
	///** getType returns the corresponding type constant of an elementary type
	//*  @return the type constant*/

	// public TypeConstant getType() {
	// return TypeConstant.T_DINT;
	// }
	private String name;
	private boolean inputVariable;    // is it an input or output variable?

	// memory direct variables are not allowed
	private TypeConstant type = TypeConstant.T_BOOL;    // only allowed;
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

	public int getNumber()
	{
		return number;
	}
	;

	public boolean isInput()
	{
		return inputVariable;
	}

	public TypeConstant getType()
	{
		return type;
	}

	public String toString()
	{
		return name;
	}
}
