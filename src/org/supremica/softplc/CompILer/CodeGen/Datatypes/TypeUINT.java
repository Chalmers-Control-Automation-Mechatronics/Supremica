package org.supremica.softplc.CompILer.CodeGen.Datatypes;

/**This class is representing the UINT data type in
 * IEC 61131-3, 2nd Ed.
 * See "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders R�ding
 */
public class TypeUINT
	implements TypeANY_INT
{

	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	@Override
  public TypeConstant getType()
	{
		return TypeConstant.T_UINT;
	}
}
