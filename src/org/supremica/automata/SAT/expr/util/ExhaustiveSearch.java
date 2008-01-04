/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.Environment;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;

/**
 *
 * @author voronov
 */
public class ExhaustiveSearch {

    public static boolean isSatisfiable(Expr n, Environment env){
        return isSatisfiable(n, env, 0);        
    }
    static boolean isSatisfiable(Expr n, Environment env, int varNum){
        if(varNum >= env.vars.size()) /* all assigned */ {            
            return Evaluator.Evaluate(n, env);
//            return ( (Boolean)n.accept(
//                    new NodeVisitorForEvaluation(env)
//                    )).booleanValue();
        }
        
        for(int val=0; val < env.vars.get(varNum).domain.size(); val++){
            env.assign(env.vars.get(varNum), val);
            if(isSatisfiable(n, env, varNum+1))
                return true;            
        }
        return false;
    }
}
