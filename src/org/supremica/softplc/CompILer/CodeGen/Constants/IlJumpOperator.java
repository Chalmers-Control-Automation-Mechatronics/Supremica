/**Class IlJumpOperators provides common constants for
 * representing instruction constants
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Constants;
import java.util.*;
public class IlJumpOperator {
    private static List theInstructions = new ArrayList();
    private String instruction;
    private IlJumpOperator(String s) {
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

    public static IlJumpOperator
        //il_jump_operation -> il_jump_operator
        JMP   = new IlJumpOperator("JMP"),
        JMPC  = new IlJumpOperator("JMPC"),
        JMPCN = new IlJumpOperator("JMPCN");

    public static IlJumpOperator getOperator(String s)
        throws IllegalOperatorException{
        if (s.equals("JMP"))
            return JMP;
        else if (s.equals("JMPC"))
            return JMPC;
        else if (s.equals("JMPCN"))
            return JMPCN;
        throw new IllegalOperatorException();
    }
}
