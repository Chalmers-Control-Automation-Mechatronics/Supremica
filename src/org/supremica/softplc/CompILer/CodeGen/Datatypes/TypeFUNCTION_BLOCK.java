package org.supremica.softplc.CompILer.CodeGen.Datatypes;

/**This class is representing FUNCTION BLOCK variable names in
 * IEC 61131-3, 2nd Ed.
 * See "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders R�ding
 */
public class TypeFUNCTION_BLOCK
	implements TypeANY_DERIVED
{

	/** getType returns the corresponding type constant
	 *  @return the type constant*/
	@Override
  public TypeConstant getType()
	{
		return TypeConstant.T_DERIVED;
	}

	// Methods and stuff concerning internal representation
	// of a FUNCTION_BLOCK
	private final String name;

	public TypeFUNCTION_BLOCK(final String n)
	{
		this.name = n;
	}

	public String getName()
	{
		return name;
	}

	@Override
  public String toString()
	{
		return name;
	}
}
