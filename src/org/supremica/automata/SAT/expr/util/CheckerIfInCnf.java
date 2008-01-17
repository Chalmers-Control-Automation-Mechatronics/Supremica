/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.*;

/**
 *
 * @author alex
 */
public class CheckerIfInCnf {
    public static boolean isInCNF(Expr n){
        return isInCNF(n, false);
    }
    private static boolean isInCNF(Expr n, boolean seenOr)
    {
        switch(n.type)
        {
        case LIT:
            return true;
        case AND:
            And a = (And)n;
            return !seenOr && 
                    isInCNF(a.left, seenOr) && 
                    isInCNF(a.right, seenOr);
        case MAND:
            if(seenOr)
                return false;
            for(Expr n1: (mAnd)n)
                if(!isInCNF(n1, seenOr))
                    return false;
            return true;
        case OR:
            Or o = (Or)n;
            return isInCNF(o.left, true) && 
                    isInCNF(o.right, true);
        case MOR:
            for(Expr n1: (mOr)n)
                if(!isInCNF(n1, true))
                    return false;
            return true;                
        default:
            return false;
        }
    }
}
