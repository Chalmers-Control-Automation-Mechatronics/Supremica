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
public class ConverterToNonNegated {
    /**
     * Remove all negations in the whole tree
     * @param  node    starting node
     * @return         expression without negations
     */
    public static Expr removeAllNegations(Expr node) {
        switch (node.type) {
        case NOT :
            return removeAllNegations(
                    pushNegationDown(((Not) node).child));

        case OR :
            return new Or(
                    removeAllNegations(((Or) node).left), 
                    removeAllNegations(((Or) node).right));

        case AND :
            return new And(
                    removeAllNegations(((And) node).left), 
                    removeAllNegations(((And) node).right));

        case MAND :
            mAnd ma = new mAnd();
            for (Expr n1 : (mAnd) node) 
                ma.add(removeAllNegations(n1));            
            return ma;

        case LIT :
            return node;

        case MOR :
            mOr mo = new mOr();
            for (Expr n1 : (mOr) node) 
                mo.add(removeAllNegations(n1));           
            return mo;

        default :
            throw new IllegalArgumentException("Illegal node type: " + node.type.toString());
        }
    }

    /**
     * Push one negation down as far as possible
     * !(a&b) = !a | !b
     * !(a|b) = !a & !b
     *
     * @param c  child node of NOT
     * @return   expression with removed NOT
     */
    public static Expr pushNegationDown(Expr c) {
        //Expr c = node.child;

        switch (c.type) {
        case AND :
            return new Or(
                    pushNegationDown(((And)c).left),
                    pushNegationDown(((And)c).right)
                    );
        case OR :
            return new And(
                    pushNegationDown(((Or)c).left),
                    pushNegationDown(((Or)c).right)
                    );

        case MOR :
            mAnd ma = new mAnd();
            for (Expr n1 : (mOr) c) 
                ma.add(pushNegationDown(n1));            
            return ma;

        case MAND :
            mOr mo = new mOr();
            for (Expr n1 : (mAnd) c) 
                mo.add(pushNegationDown(n1));         
            return mo;

        case NOT :
            return ((Not)c).child;

        case LIT :
            Literal l = (Literal) c;
            return new Literal(l.variable, !l.isPositive);

        default :
            throw new IllegalArgumentException(
                    "Illegal child node type: " + c.type.toString());
        }
    }
}
