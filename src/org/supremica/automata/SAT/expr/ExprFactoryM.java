/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;

/**
 *
 * @author voronov
 */
public class ExprFactoryM implements IExprFactory {

    public Expr And(Expr e1, Expr e2) {
        if(e1==null)
            return e2;
        if(e2==null)
            return e1;
        switch(e1.type){
            case MAND:
                switch(e2.type){
                    case MAND:
                        ((mAnd)e1).childs.addAll(((mAnd)e2).childs);
                        return e1;
                    case AND:
                        ((mAnd)e1).childs.add(((And)e2).left);
                        ((mAnd)e1).childs.add(((And)e2).right);
                        return e1;
                    default:
                        ((mAnd)e1).add(e2);
                }
            default:
                mAnd res = new mAnd();
                res.add(e1);
                res.add(e2);
                return res;
        }
    }

    public Expr Or(Expr e1, Expr e2) {
        if(e1==null)
            return e2;
        if(e2==null)
            return e1;        
        switch(e1.type){
            case MOR:
                switch(e2.type){
                    case MOR:
                        ((mOr)e1).childs.addAll(((mOr)e2).childs);
                        return e1;
                    case OR:
                        ((mOr)e1).childs.add(((Or)e2).left);
                        ((mOr)e1).childs.add(((Or)e2).right);
                        return e1;
                    default:
                        ((mOr)e1).add(e2);
                }
            default:
                mOr res = new mOr();
                res.add(e1);
                res.add(e2);
                return res;
        }
    }

    public Expr InitAnd() {
        return new mAnd();
    }

    public Expr InitOr() {
        return new mOr();
    }        

    public Expr Not(Expr e){
        return new Not(e);
    }
    public Expr VarEqVar(Variable v1, Variable v2){
        return new VarEqVar(v1, v2);
    }
    public Expr VarEqInt(Variable v1, int val){
        return new VarEqInt(v1, val);
    }
    public Expr VarEqInt(Variable v1, int val, boolean skipDomainCheck){
        return new VarEqInt(v1, val, skipDomainCheck);
    }
}
