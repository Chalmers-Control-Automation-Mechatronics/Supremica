/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.*;

/**
 *
 * @author voronov
 */
public class ConverterToNonNegatedOld {
    public static Expr convert(Expr node)           
    {
        switch(node.type)
        {
            case NOT:
                return toCNFPushNegationDown((Not)node);
            case OR:
                return new Or(
                        convert(((Or)node).left), 
                        convert(((Or)node).right));
            case AND:
                return new And(
                        convert(((And)node).left), 
                        convert(((And)node).right));
            case MAND:
                mAnd ma = new mAnd();
                for(Expr n1: (mAnd)node)
                    ma.add(convert(n1));
                return ma;
            case LIT:
                return node;
            case MOR:
                mOr mo = new mOr();
                for(Expr n1: (mOr)node)
                    mo.add(convert(n1));
                return mo;
            default:                    
                throw new IllegalArgumentException(
                        "Illegal node type: " 
                        + node.type.toString());
        }
    }
    
    
    /**
     * !(a&b) = !a | !b
     * !(a|b) = !a & !b
     */
    static Expr toCNFPushNegationDown(Not node)
    {
        Expr c = node.child;
        switch(c.type)
        {
            case AND:
                return (
                        new Or( 
                            convert(new Not(((And)c).left)), 
                            convert(new Not(((And)c).right)) 
                        ));                
            case OR:                
                return (
                        new And(
                            convert(new Not(((Or)c).left)), 
                            convert(new Not(((Or)c).right))
                        ));                
            case MOR:
                mAnd ma = new mAnd();
                for(Expr n1: (mOr)c)
                    ma.add(convert(new Not(n1)));
                return ma;
            case MAND:
                mOr mo = new mOr();
                for(Expr n1: (mAnd)c)
                    mo.add(convert(new Not(n1)));
                return mo;            
            case NOT:
                Not n = (Not)c;
                return convert((n.child));
            case LIT:
                Literal l = (Literal)c;
                return new Literal(l.variable, !l.isPositive);
            default:
                throw new IllegalArgumentException(
                        "Illegal (non-cnf?) node type: " 
                        + node.type.toString());

        }        
    }
}
