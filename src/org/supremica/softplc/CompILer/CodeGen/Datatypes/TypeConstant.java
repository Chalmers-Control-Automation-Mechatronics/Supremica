
/** TypeConstant supplies constants for the elementary datatypes in IEC 61131-3
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;
import java.util.*;
public final class TypeConstant {
	private static List theTypes = new ArrayList();
    private String type;
    private TypeConstant(String t) {
		type = t;
		theTypes.add(this);
	}

	public static Iterator iterator()
	{
		return theTypes.iterator();
	}

    public String toString() {return type;}
    public static final TypeConstant
        T_BOOL    = new TypeConstant("BOOL"),    //Boolean
        T_SINT    = new TypeConstant("SINT"),    //Short integer
        T_INT     = new TypeConstant("INT"),     //Integer
        T_DINT    = new TypeConstant("DINT"),    //Double integer
        T_LINT    = new TypeConstant("LINT"),    //Long integer
        T_USINT   = new TypeConstant("USINT"),   //Unsigned short integer
        T_UINT    = new TypeConstant("UINT"),    //Unsigned integer
        T_UDINT   = new TypeConstant("UDINT"),   //Unsigned double integer
        T_ULINT   = new TypeConstant("ULINT"),   //Unsigned long integer
        T_REAL    = new TypeConstant("REAL"),    //Real numbers
        T_LREAL   = new TypeConstant("LREAL"),   //Long real numbers
        T_TIME    = new TypeConstant("TIME"),    //Duration
        T_DATE    = new TypeConstant("DATE"),    //Date (only)
        T_TOD     = new TypeConstant("TIME_OF_DAY"),  //Time of day (only)
        T_DT      = new TypeConstant("DATE_AND_TIME"),//Date and time of day

        //Variable-length single-byte character string
        T_STRING  = new TypeConstant("STRING"),
        T_BYTE    = new TypeConstant("BYTE"),	 //Bit string of length 8
        T_WORD    = new TypeConstant("WORD"),    //Bit string of length 16
        T_DWORD   = new TypeConstant("DWORD"),   //Bit string of length 32
        T_LWORD   = new TypeConstant("LWORD"),	 //Bit string of length 64

        //Variable-length double-byte character string
        T_WSTRING = new TypeConstant("WSTRING"),

		//Derived datatypes
		T_DERIVED = new TypeConstant("DERIVED"),

        /*Things that are not part of the IEC type tree, but similar */
        //Function Block
        T_FUNCTION_BLOCK = new TypeConstant("FUNCTION_BLOCK");

}
