
/**This interface is implemented by all constant data types (e.g. TypeANY)
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders R�ding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public interface IECConstant
{

	/**getType returns the type of the constant
	 * @see {@link TypeConstant}
	 * @return the type
	 */
	TypeConstant getType();
}
