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
public class ConverterBoolToCnfStruct {
    

    public static class Pair {
        public Pair(Expr m, mAnd t){
            main = m;
            top = t;
        }
        private Expr main;
        private mAnd top;

        public Expr getMain() {
            return main;
        }
        public mAnd getTop() {
            return top;
        }
    }
    
    Environment env;
    
    public ConverterBoolToCnfStruct(Environment e){
        env = e;
    }
    
    public Expr convertAll(Expr e){
        Pair p = convert(e);
        p.getTop().add(p.getMain());
        return p.getTop();
    }
    
    public Pair convert(Expr e){
        Expr en;
        mAnd et = new mAnd();
        switch(e.type){
            case MAND:
                en = new mAnd();
                for(Expr e1: (mAnd)e){
                    Pair p = convert(e1);
                    ((mAnd)en).add(p.getMain());
                    et.add(p.getTop());
                }
                return new Pair(en, et);
            case MOR:
                en = new mOr();
                for(Expr e1: (mOr)e){
                    Pair p = convert(e1);
                    ((mOr)en).add(p.getMain());
                    et.add(p.getTop());
                }
                if(shouldReplaceIn(en)){
                    mOr enn = new mOr();
                    for(Expr e1: (mOr)en){
                        Pair pr = replacement(e1);
                        enn.add(pr.getMain());
                        /* side effects are always in cnf */
                        Pair prc = convert(pr.getTop());
                        et.add(prc.getMain());
                        et.add(prc.getTop());
                    }
                } else {
                    en = ConverterBoolToCnfSat.convertAll(en);
                }        
                return new Pair(en, et);
            case LIT:
                return new Pair(e, new mAnd());
            //TODO: NOT
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    public boolean shouldReplaceIn(Expr e){
        //TODO: implement...
        return true;
    }
    public Pair replacement(Expr e){
        int vi = env.addBool();
        Literal v = new Literal(env.vars.get(vi), true);
        mAnd ma = new mAnd();
        // TODO: Or -> mOr
        ma.add(new Or(new Not(e), v));
        ma.add(new Or(e, new Not(v)));
        return new Pair(v, ma);
    }    
}