/**Class IlExprOperators provides common constants for
 * representing instruction constants
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Constants;
import java.util.*;
public class IlExprOperator {
    private static List theInstructions = new ArrayList();
    private String instruction;
    private IlExprOperator(String s) {
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

    public static IlExprOperator
	//il_expression -> il_expr_operator
        AND  = new IlExprOperator("AND("),// operators "AND(" and "&("
        //ÄR DESSA LIKA??????????????????????????????
        OR   = new IlExprOperator("OR("),
        XOR  = new IlExprOperator("XOR("),
        ANDN = new IlExprOperator("ANDN("), //operators "ANDN(" and "&N("
        ORN  = new IlExprOperator("ORN("),
        XORN = new IlExprOperator("XORN("),
        ADD  = new IlExprOperator("ADD("),
        SUB  = new IlExprOperator("SUB("),
        MUL  = new IlExprOperator("MUL("),
        DIV  = new IlExprOperator("DIV("),
        MOD  = new IlExprOperator("MOD("),
        GT   = new IlExprOperator("GT("),
        GE   = new IlExprOperator("GE("),
        EQ   = new IlExprOperator("EQ("),
        LT   = new IlExprOperator("LT("),
        LE   = new IlExprOperator("LE("),
        NE   = new IlExprOperator("NE(");

    public static IlExprOperator getOperator(String s)
        throws IllegalOperatorException{
        if (s.equals("AND("))
            return AND;
        else if (s.equals("&("))
            return AND;
        else if (s.equals("OR("))
            return OR;
        else if (s.equals("XOR("))
            return XOR;
        else if (s.equals("ANDN("))
            return ANDN;
        else if (s.equals("&N("))
            return ANDN;
        else if (s.equals("ORN("))
            return ORN;
        else if (s.equals("XORN("))
            return XORN;
        else if (s.equals("ADD("))
            return ADD;
        else if (s.equals("SUB("))
            return SUB;
        else if (s.equals("MUL("))
            return MUL;
        else if (s.equals("DIV("))
            return DIV;
        else if (s.equals("MOD("))
            return MOD;
        else if (s.equals("GT("))
            return GT;
        else if (s.equals("GE("))
            return GE;
        else if (s.equals("EQ("))
            return EQ;
        else if (s.equals("LT("))
            return LT;
        else if (s.equals("LE("))
            return LE;
        else if (s.equals("NE("))
            return NE;
        throw new IllegalOperatorException();
    }
}
