
/**This class is representing the TIME data type in
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders R�ding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public class TypeTime
	implements TypeANY_MAGNITUDE
{

	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	public TypeConstant getType()
	{
		return TypeConstant.T_TIME;
	}
}
