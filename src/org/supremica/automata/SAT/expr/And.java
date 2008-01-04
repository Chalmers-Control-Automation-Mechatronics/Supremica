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
public class And extends BinaryOperator
{
    public And(Expr l, Expr r){
        super(l,r);
        type = ExprType.AND;
    }        

    @Override
    public Object accept(Visitor v) {
        return v.visit(this);                    
    }
}
