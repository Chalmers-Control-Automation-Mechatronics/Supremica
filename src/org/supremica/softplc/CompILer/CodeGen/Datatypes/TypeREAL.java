package org.supremica.softplc.CompILer.CodeGen.Datatypes;

/**This class is representing the REAL data type in
 * IEC 61131-3, 2nd Ed.
 * See "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders Röding
 */
public class TypeREAL
	implements TypeANY_REAL
{

	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	@Override
  public TypeConstant getType()
	{
		return TypeConstant.T_REAL;
	}

	// Methods and stuff concerning internal representation
	// of a 32 bit IL Real
	private final float value;

	public TypeREAL(final String s)
	{
		value = Float.parseFloat(s);
	}

	public float getValue()
	{
		return value;
	}

	@Override
  public String toString()
	{
		return Float.toString(value);
	}
}
