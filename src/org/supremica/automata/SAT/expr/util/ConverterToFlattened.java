/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.*;
import java.util.*;

/**
 *
 * @author voronov
 */
public class ConverterToFlattened {
    public static Expr convert(Expr iNode)
    {
        switch(iNode.type){
            case AND:
            case MAND:
                return flattenAnd(iNode);
            case OR:
            case MOR:
                return flattenOr(iNode);
            case LIT:
                return iNode;
            default:
                throw new IllegalArgumentException("unexpected " + 
                        iNode.type.toString());                
        }
    }
    private static mAnd flattenAnd(Expr n0)
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
            case NOT:
                System.err.println("warn: flattening NOT");
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
            case NOT:
                System.err.println("warn: flattening NOT");
            case LIT:
                res.add(n);
                break;
            case AND:
            case MAND:
                res.add(flattenAnd(n));
                break;
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
