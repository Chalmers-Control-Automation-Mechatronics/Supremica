/**This class is representing the DINT data type in
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;
public class TypeDINT implements TypeANY_INT {
	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	public TypeConstant getType() {
		return TypeConstant.T_DINT;
	}

	//Methods and stuff concerning internal representation
	// of a 32 bit IL Integer

   	private int value;

	public TypeDINT(String s, int radix){
		value = Integer.parseInt(s,radix);
	}
	public int getValue(){return value;};

    public String toString() {return Integer.toString(value);}
}
