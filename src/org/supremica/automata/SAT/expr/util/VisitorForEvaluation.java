/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;

/**
 *
 * @author voronov
 */
public class VisitorForEvaluation implements IVisitor {
    
    Environment env;
    
    public VisitorForEvaluation(Environment iEnv){
        env = iEnv;
    }

    public Object visit(And n) {        
        return (Boolean)n.left.accept(this) && 
                (Boolean)n.right.accept(this);
//        return 
//                (( ((Boolean)n.left.accept(this)).compareTo(Boolean.TRUE) 
//                == 0) && 
//                (((Boolean)n.right.accept(this)).compareTo(Boolean.TRUE) 
//                == 0)) ? Boolean.TRUE:Boolean.FALSE;
    }

    public Object visit(Or n) {
        return (Boolean)n.left.accept(this) || 
                (Boolean)n.right.accept(this);
//        return 
//                (( ((Boolean)n.left.accept(this)).compareTo(Boolean.TRUE) 
//                == 0) ||
//                (((Boolean)n.right.accept(this)).compareTo(Boolean.TRUE) 
//                == 0)) ? Boolean.TRUE:Boolean.FALSE;        
    }

    public Object visit(Not n) {
        return !(Boolean)n.child.accept(this);
//        return ((Boolean)n.child.accept(this)).booleanValue()?
//            Boolean.FALSE:Boolean.TRUE;
    }

    public Object visit(VarEqVar n) {
        return env.getValueFor(n.var1) == env.getValueFor(n.var2);
//        return (env.getValueFor(n.var1) == env.getValueFor(n.var2))?
//            Boolean.TRUE:Boolean.FALSE;
    }

    public Object visit(VarEqInt n) {
        return env.getValueFor(n.variable) == n.value;
//        return (env.getValueFor(n.variable) == n.value)?
//            Boolean.TRUE:Boolean.FALSE;
    }

    public Object visit(Literal n) {
        boolean v = env.getValueFor(n.variable)==0?false:true;
        v = n.isPositive?v:(!v);
        return v;
//        return v?Boolean.TRUE:Boolean.FALSE;
    }
    public Object visit(mAnd n) {        
        Boolean res = true;
        for(Expr n1: n)
            res = res & ((Boolean)(n1.accept(this)));
        return res;
    }
    public Object visit(mOr n) {        
        Boolean res = false;
        for(Expr n1: n)
            res = res | ((Boolean)(n1.accept(this)));
        return res;
    }
    
}
