
/**This class is representing the WSTRING data type in
 * IEC 61131-3, 2nd Ed.
 * @see "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public class TypeWSTRING
	implements TypeANY_STRING
{

	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	public TypeConstant getType()
	{
		return TypeConstant.T_WSTRING;
	}

	// Methods and stuff concerning internal representation
	// of an IL WideSTRING
	private String value;

	public TypeWSTRING(String s)
	{
		value = s;
	}

	public String getValue()
	{
		return value;
	}

	public String toString()
	{
		return value;
	}
}
