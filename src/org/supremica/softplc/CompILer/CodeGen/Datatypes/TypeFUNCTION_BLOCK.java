
/**This class is representing FUNCTION BLOCK variable names in
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public class TypeFUNCTION_BLOCK
	implements TypeANY_DERIVED
{

	/** getType returns the corresponding type constant
	 *  @return the type constant*/
	public TypeConstant getType()
	{
		return null; //TypeConstant.T_FUNCTION_BLOCK;
	}

	// Methods and stuff concerning internal representation
	// of a FUNCTION_BLOCK
	private String name;

	public TypeFUNCTION_BLOCK(String n)
	{
		System.err.println("Using deprecated class TypeFUNCTION_BLOCK");
		this.name = n;
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return name;
	}
}
