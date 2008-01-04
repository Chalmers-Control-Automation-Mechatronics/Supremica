/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;
/**
 *
 * @author voronov
 */
public class PrinterDimacsSat {

    public static String Print(Expr n) {
        switch(n.type){
        case AND:
            return "*(" + Print(((And)n).left) + 
                    " " + Print(((And)n).right) +")";
        case OR:
            return "+(" + Print(((Or)n).left) + 
                    " " + Print(((Or)n).right) +")";
        case NOT:
            return "-(" + Print(((Not)n).child) + ")";
        case LIT:
            return (((Literal)n).isPositive?"":"-") + 
                    ((Literal)n).variable.id;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized (non-dimacs-sat?) node type");
        }
    }       
    public static String Print2(Expr n) {
        StringBuffer sb;
        switch(n.type){
        case MAND:
            sb = new StringBuffer();
            sb.append("*(");
            for(Expr e: (mAnd) n){
                sb.append(Print2(e,n.type));
                sb.append(" ");
            }
            sb.append(")");
            return sb.toString();
        case AND:            
            return "*(" + Print2(((And)n).left, n.type) + 
                    " " + Print2(((And)n).right, n.type) +")";
        case OR:
            return "+(" + Print2(((Or)n).left, n.type) + 
                    " " + Print2(((Or)n).right, n.type) +")";
        case MOR:
            sb = new StringBuffer();
            sb.append("+(");
            for(Expr e: (mOr) n){
                sb.append(Print2(e,n.type));
                sb.append(" ");
            }
            sb.append(")");
            return sb.toString();
        case NOT:
            return "-(" + Print2(((Not)n).child) + ")";
        case LIT:
            return (((Literal)n).isPositive?"":"-") + 
                    ((Literal)n).variable.id;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized (non-dimacs-sat?) node type");
        }
    }       
    public static String Print2(Expr n, ExprType t) {
        StringBuffer sb;
        switch(n.type){
        case AND:
            if(t==ExprType.AND || t==ExprType.MAND)
                return Print2(((And)n).left, n.type) + 
                    " " + Print2(((And)n).right, n.type);
            else
                return "*(" + Print2(((And)n).left, n.type) + 
                    " " + Print2(((And)n).right, n.type) +")";                
        case MAND:
            if(t==ExprType.AND || t==ExprType.MAND){
                
                sb = new StringBuffer();
                for(Expr e: (mAnd) n){
                    sb.append(Print2(e,n.type));
                    sb.append(" ");
                }
                return sb.toString();

            }else{
                sb = new StringBuffer();
                sb.append("*(");
                for(Expr e: (mAnd) n){
                    sb.append(Print2(e,n.type));
                    sb.append(" ");
                }
                sb.append(")");
                return sb.toString();
            }
        case OR:
            if(t==ExprType.OR || t==ExprType.MOR)
                return Print2(((Or)n).left, n.type) + 
                    " " + Print2(((Or)n).right, n.type);
            else
                return "+(" + Print2(((Or)n).left, n.type) + 
                    " " + Print2(((Or)n).right, n.type) +")";
        case MOR:
            if(t==ExprType.OR || t==ExprType.MOR){
                
                sb = new StringBuffer();
                for(Expr e: (mOr) n){
                    sb.append(Print2(e,n.type));
                    sb.append(" ");
                }
                return sb.toString();

            }else{
                sb = new StringBuffer();
                sb.append("+(");
                for(Expr e: (mOr) n){
                    sb.append(Print2(e,n.type));
                    sb.append(" ");
                }
                sb.append(")");
                return sb.toString();
            }
        case NOT:
            return "-(" + Print2(((Not)n).child) + ")";
        case LIT:
            return (((Literal)n).isPositive?"":"-") + 
                    ((Literal)n).variable.id;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized (non-dimacs-sat?) node type");
        }
    }       
}
