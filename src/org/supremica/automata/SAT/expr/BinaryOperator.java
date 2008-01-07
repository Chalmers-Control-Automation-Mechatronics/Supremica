/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;

/**
 *
 * @author voronov
 */
public abstract class BinaryOperator extends Expr
{
    public final Expr left;
    public final Expr right;        
    public BinaryOperator(Expr l, Expr r)
    {
        if(l==null)
            throw new IllegalArgumentException("" +
                    "creating node with null left child?");            
        if(l==null)
            throw new IllegalArgumentException("" +
                    "creating node with null right child?");            
        left = l;
        right = r;
    }
}
