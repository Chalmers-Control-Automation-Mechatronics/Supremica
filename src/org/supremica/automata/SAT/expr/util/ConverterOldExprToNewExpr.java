/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT2.Convert.*;

/**
 *
 * @author alex
 */
public class ConverterOldExprToNewExpr {

    public static org.supremica.automata.SAT2.Convert.Expr 
            convert(org.supremica.automata.SAT.expr.Expr e){
        MOp res;
        switch(e.type){
            case LIT:
                return org.supremica.automata.SAT2.Convert.Lit(
                        ((Literal)e).variable.id *
                        (((Literal)e).isPositive?1:-1));
            case AND:
                return org.supremica.automata.SAT2.Convert.And(
                        convert( ((And)e).left ),
                        convert( ((And)e).right )
                        );
            case OR:
                return org.supremica.automata.SAT2.Convert.Or(
                        convert( ((Or)e).left ),
                        convert( ((Or)e).right )
                        );
            case MAND:
                res = new MOp(ExType.MAND);
                for(org.supremica.automata.SAT.expr.Expr f: (mAnd)e)
                    res.add(convert(f));
                return res;
            case MOR:
                res = new MOp(ExType.MOR);
                for(org.supremica.automata.SAT.expr.Expr f: (mOr)e)
                    res.add(convert(f));
                return res;
            case NOT:
                return org.supremica.automata.SAT2.Convert.Not(convert(((Not)e).child));
            default:
                throw new IllegalArgumentException(
                        "can't convert expr of type " + e.type.toString());                
        }
    }
}
