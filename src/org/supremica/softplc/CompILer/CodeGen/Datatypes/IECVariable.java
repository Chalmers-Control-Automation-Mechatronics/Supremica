
/**This interface is implemented by all variable classes
 *  (e.g. IECDirectVariable and IECSymbolicVariable)
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

public interface IECVariable
{

	/** getType returns the corresponding type constant of an elementary type
	 * @see TypeConstant
	 * @return the type constant
	 */
	TypeConstant getType();
}
