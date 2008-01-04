/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT.*;
import  java.io.PrintWriter;

/**
 *
 * @author voronov
 */
public class PrinterDimacsCnf {
    public static String print(Expr n){
        if(!ConverterBoolToCnfSat.isInCNF(n))
            throw new IllegalArgumentException("expression is not in CNF");
        
        StringBuffer s;
        switch(n.type)
        {
        case AND:
            return print(((And)n).left) + " 0\n " + print(((And)n).right);
        case OR:
            return print(((Or)n).left) + " " + print(((Or)n).right);
        case LIT:
            return (((Literal)n).isPositive?"":"-") + ((Literal)n).variable.id;
        case MAND:
            s = new StringBuffer();
            for(Expr n1:(mAnd)n)
                s.append(print(n1) + " 0\n");
            return s.toString();
        case MOR:
            s = new StringBuffer();
            for(Expr n1:(mOr)n)
                s.append(print(n1) + " ");
            return s.toString();
        default:
            throw new IllegalArgumentException(
                    "unrecognized (non-CNF?) Node Type: " + n.type.toString());
        }
        
    }
    public static void print(Expr n, PrintWriter out){
        if(!ConverterBoolToCnfSat.isInCNF(n))
            throw new IllegalArgumentException("expression is not in CNF");
        
        switch(n.type)
        {
        case AND:
            print(((And)n).left, out); 
            out.println(" 0"); 
            print(((And)n).right, out);
            break;
        case OR:
            print(((Or)n).left, out);
            out.print(" ");
            print(((Or)n).right, out);
            break;
        case LIT:
            out.print((((Literal)n).isPositive?"":"-") + ((Literal)n).variable.id);
            break;
        case MAND:
            for(Expr n1:(mAnd)n){
                print(n1, out);
                out.println(" 0");
            }
            break;
        case MOR:
            for(Expr n1:(mOr)n){
                print(n1, out);
                out.print(" ");
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "unrecognized (non-CNF?) Node Type: " + n.type.toString());
        }        
    }
    
}
