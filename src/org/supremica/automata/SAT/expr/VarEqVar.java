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
public class VarEqVar extends Expr
{
    public Variable var1;
    public Variable var2;

    public VarEqVar(Variable var1i, Variable var2i)
    {
        if(var1i==null)
            throw new IllegalArgumentException(
                    "first variable can't be null");
        if(var2i==null)
            throw new IllegalArgumentException(
                    "second variable can't be null");
        if(!var1i.domain.equals(var2i.domain)) 
            throw new IllegalArgumentException(
                    "different domains of variables");

        var1 = var1i;
        var2 = var2i;
        type = ExprType.VAREQVAR;
    }   
    public Object accept(Visitor v) {
        return v.visit(this);                    
    }

}    
