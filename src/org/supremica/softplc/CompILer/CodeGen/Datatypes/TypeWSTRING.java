/**This class is representing the DINT data type in
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;
public class TypeWSTRING implements TypeANY_STRING {
	/** getType returns the corresponding type constant of an elementary type
	 *  @return the type constant*/
	public TypeConstant getType() {
		return TypeConstant.T_WSTRING;
	}

	//Methods and stuff concerning internal representation
	// of an IL WideSTRING

   	private String value;

	public TypeWSTRING(String s){
		value = s;
	}
	public String getValue(){return value;};

    public String toString() {return value;}
}
