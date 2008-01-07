/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import java.io.PrintWriter;
import  org.supremica.automata.SAT.expr.*;

/**
 *
 * @author voronov
 */
public class PrinterInfixFullBrackets implements IPrinter {
    public static String print(Expr n) {
        if(n==null)
            throw new IllegalArgumentException("Can't print null node");
        switch(n.type){
        case AND:
            return "(" + print(((And)n).left) + 
                    " And " + print(((And)n).right) +")";
        case OR:
            return "(" + print(((Or)n).left) + 
                    " OR " + print(((Or)n).right) +")";
        case NOT:
            return "Not " + print(((Not)n).child);
        case VAREQVAR:
            return ((VarEqVar)n).var1.Name + "=" + 
                    ((VarEqVar)n).var2.Name;
        case VAREQINT:
            return ((VarEqInt)n).variable.Name + "=" + 
                    ((VarEqInt)n).value;            
        case LIT:
            return (((Literal)n).isPositive?"":"-") + 
                    ((Literal)n).variable.Name;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized node type: "+n.type.toString());
        }
    }

    public void print(Expr e, PrintWriter pwOut) {
        pwOut.print(print(e));
    }
}
