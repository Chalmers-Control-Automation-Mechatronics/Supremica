/**This class is representing the BOOL data type in
 * IEC 61131-3, 2nd Ed.
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;
public class TypeBOOL implements TypeANY_BIT {
    /** getType returns the corresponding type constant of an elementary type
     *  @return the type constant*/
    public TypeConstant getType() {
        return TypeConstant.T_BOOL;
    }

    //Methods and stuff concerning internal representation
    // of a IL boolean

    private boolean value;

    public TypeBOOL(boolean b){
        value = b;
    }
    public boolean getValue(){return value;};

    public String toString() {
        if (value) {
            return new String("TRUE");}
        else {
            return new String("FALSE");}
    }
}
