/**Class IlCallOperators provides common constants for
 * representing instructions
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Constants;
import java.util.*;
public class IlCallOperator {
    private static List theInstructions = new ArrayList();
    private String instruction;
    private IlCallOperator(String s) {
        instruction = s;
        theInstructions.add(this);
    }

    /**iterator gives an iterator over the instruction constants*/
    public static Iterator iterator(){
        return theInstructions.iterator();
    }

    /**toString gives a string representation of an instruction*/
    public String toString(){
        return instruction;
    }

    public static IlCallOperator
        //il_jump_operation -> il_jump_operator
        CAL   = new IlCallOperator("JMP"),
        CALC  = new IlCallOperator("JMPC"),
        CALCN = new IlCallOperator("JMPCN");

    public static IlCallOperator getOperator(String s)
        throws IllegalOperatorException{
        if (s.equals("CAL"))
            return CAL;
        else if (s.equals("CALC"))
            return CALC;
        else if (s.equals("CALCN"))
            return CALCN;
        throw new IllegalOperatorException();
    }
}
