/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT.*;

/**
 *
 * @author voronov
 */
public class Evaluator 
{
    public static boolean Evaluate(Expr n, Environment env) {
        switch(n.type){
        case MAND:
            for(Expr e: (mAnd)n)
                if(!Evaluate(e,env))
                    return false;
            return true;
        case AND:
            return Evaluate(((And)n).left, env) && 
                    Evaluate(((And)n).right, env) ;
        case MOR:
            for(Expr e: (mOr)n)
                if(Evaluate(e,env))
                    return true;
            return false;
        case OR:
            return Evaluate(((Or)n).left, env) ||
                    Evaluate(((Or)n).right, env) ;
        case NOT:
            return !Evaluate(((Not)n).child, env);
        case VAREQVAR:
            return env.getValueFor(((VarEqVar)n).var1) ==
                    env.getValueFor(((VarEqVar)n).var2);
        case VAREQINT:
            return env.getValueFor(((VarEqInt)n).variable) == ((VarEqInt)n).value;            
        case LIT:
            boolean v =  0 != env.getValueFor(
                    ((Literal)n).variable);
            return  (((Literal)n).isPositive)?v:(!v);
            
        default:
            throw new IllegalArgumentException("Unrecognized node type");
        }
    }   
    
}
