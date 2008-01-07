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
public class VarEqInt extends Expr
{
    public final Variable variable;
    public final int value;

    public VarEqInt(Variable var, int val){
        this(var, val, false);
    }
    public VarEqInt(Variable var, int val, boolean skipDomainCheck){
        if(var == null)
            throw new IllegalArgumentException(
                    "variable can't be null!");
        int maxSgn = 1 << var.domain.significantBits();
        int max = skipDomainCheck? maxSgn:var.domain.size();
        if(val >= max || val<0)
            throw new IllegalArgumentException(
                    "value for variable is out of domain size");
        variable = var;
        value    = val;
        type     = ExprType.VAREQINT;            
    }
    public Object accept(IVisitor v) {
        return v.visit(this);                    
    }

}    

