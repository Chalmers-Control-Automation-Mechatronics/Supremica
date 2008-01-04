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
public class Or extends BinaryOperator
{
    public Or(Expr l, Expr r){
        super(l,r);
        type = ExprType.OR;
    }    
    public Object accept(Visitor v) {
        return v.visit(this);                    
    }
}
