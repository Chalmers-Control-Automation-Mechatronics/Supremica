/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;
import  org.supremica.automata.SAT.expr.util.*;

/**
 *
 * @author voronov
 */
public class Not extends Expr {
    public final Expr child;

    public Not(Expr n){
        if(n==null)
            throw new IllegalArgumentException("" +
                    "creating node with null child?");
        child = n;
        type = ExprType.NOT;
    }        
    public Object accept(IVisitor v) {
        return v.visit(this);                    
    }

}
