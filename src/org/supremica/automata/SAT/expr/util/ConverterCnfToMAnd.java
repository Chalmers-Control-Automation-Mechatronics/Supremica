/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;
import java.util.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;

/**
 *
 * @author voronov
 */
public class ConverterCnfToMAnd {
    
    public static Expr convert(Expr iNode)
    {
        if(iNode.type.equals(ExprType.AND))
            return flattenAnd((And)iNode);
        else
            throw new IllegalArgumentException("And expected...");

    }
    private static mAnd flattenAnd(And n0)
    {
        Stack<Expr> s = new Stack<Expr>();
        s.push(n0);
        mAnd res = new mAnd();
        while(!s.empty()){
            Expr n = s.pop();
            switch(n.type){
            case AND:
                s.push(((And)n).left);
                s.push(((And)n).right);
                break;
            case MAND:
                for(Expr n1: (mAnd)n)
                    s.push(n1);
                break;
            case OR:
            case MOR:
                res.add(flattenOr(n));
                break;
            case LIT:
                res.add(n);
                break;
            default:
                throw new IllegalArgumentException("unexpected " + n.type.toString());
                //res.add(n);
            }
        }
        for(Expr n : s)
            res.add(n);
        return res;
    }
    private static mOr flattenOr(Expr n0)
    {
        Stack<Expr> s = new Stack<Expr>();
        s.push(n0);
        mOr res = new mOr();
        while(!s.empty()){
            Expr n = s.pop();
            switch(n.type){
            case OR:
                s.push(((Or)n).left);
                s.push(((Or)n).right);
                break;
            case MOR:
                for(Expr n1: (mOr)n)
                    s.push(n1);
                break;                
            case LIT:
                res.add(n);
                break;
            case AND:
            case MAND:
                System.err.println(
                        "Warning! flattening Or with nested And (non-cnf");
                // break; 
            default:
                throw new IllegalArgumentException("unexpected " + n.type.toString());
            }
        }
        for(Expr n : s)
            res.add(n);
        return res;
    }

}
