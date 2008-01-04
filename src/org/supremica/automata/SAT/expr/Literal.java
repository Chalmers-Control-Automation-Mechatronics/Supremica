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
public class Literal extends Expr
{
    public boolean  isPositive = true;
    public Variable variable;

    public Literal(Variable var, boolean p){
        if(var.domain.size() > 2)
            throw new IllegalArgumentException(
                    "Literals can have only two values");
        variable = var;
        isPositive = p;
        type = ExprType.LIT;
    }
    public Object accept(Visitor v) {
        return v.visit(this);                    
    }        
}       
