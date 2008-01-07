/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import java.io.PrintWriter;
import  org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;

/**
 *
 * @author voronov
 */
public class PrinterInfix implements IPrinter {

    public static String print(Expr n) {
        if(n==null)
            throw new IllegalArgumentException("Can't print null node");
        switch(n.type){
        case MAND:
            return "(" + printM((mAnd)n, " And ") + ")";
        case MOR:
            return "(" + printM((mOr)n, " Or ")+ ")";
        case AND:
            return "(" + print(((And)n).left, n.type) + 
                    " And " + print(((And)n).right, n.type) +")";
        case OR:
            return "(" + print(((Or)n).left, n.type) + 
                    " OR " + print(((Or)n).right, n.type) +")";
        case NOT:
            return "Not " + print(((Not)n).child, n.type);
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
    public static String print(Expr n, ExprType t) {

        if(n==null)
            throw new IllegalArgumentException("Can't print null node");
        
        switch(n.type){
        case MAND:
            if(t == ExprType.AND || t == ExprType.MAND)
                return printM((mAnd)n, " And ");
            else 
                return "("+printM((mAnd)n, " And ")+")";                            
        case AND:
            if(t == ExprType.AND || t == ExprType.MAND)
                return print(((And)n).left, n.type) + 
                    " And " + print(((And)n).right, n.type);
            else
                return "(" + print(((And)n).left, n.type) + 
                    " And " + print(((And)n).right, n.type) +")";
        case MOR:
            if(t == ExprType.OR || t == ExprType.MOR)
                return printM((mOr)n, " Or ");
            else 
                return "("+printM((mOr)n, " Or ")+")";                            
        case OR:
            if(t==ExprType.OR)
                return print(((Or)n).left, n.type) + 
                    " OR " + print(((Or)n).right, n.type);
            else
                return "(" + print(((Or)n).left, n.type) + 
                    " OR " + print(((Or)n).right, n.type) +")";
        case NOT:
            return "Not " + print(((Not)n).child, n.type);
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
    private static final String printM(Iterable<Expr> iter, String sep){
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for(Expr e: iter){
            if(first)
                first = false;
            else
                sb.append(sep);
            sb.append(print(e, ((Expr)iter).type));
        }
        return sb.toString();        
    }

    public void print(Expr e, PrintWriter pwOut) {
        pwOut.print(print(e));
    }    
}
