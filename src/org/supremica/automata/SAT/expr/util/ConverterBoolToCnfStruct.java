/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;
/**
 *
 * @author voronov
 */
public class ConverterBoolToCnfStruct {
    

    private static class Pair {
        private final Expr main;
        private final mAnd top;

        public Pair(Expr m, mAnd t){
            main = m;
            top = t;
        }
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
    
    private Pair convert(Expr e){
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
                
                mOr enn = new mOr();
                //for(Expr e1: (mOr)en){
                for(int i=0; i < ((mOr)en).childs.size(); i++){
                    Expr e1 = ((mOr)en).childs.get(i);
                    if(e1.type==ExprType.MAND && shouldReplaceIn(en, i)){
                        Pair pr = replacement(e1);
                        enn.add(pr.getMain());
                        // side effects are always in cnf
                        Pair prc = convert(pr.getTop());
                        et.add(prc.getMain());
                        et.add(prc.getTop());
                    } else
                        enn.add(e1);
                }
                // convert the rest that was not replaced
                en = ConverterBoolToCnfSat.convert(enn);
                return new Pair(en, et);
            case LIT:
                return new Pair(e, new mAnd());
            //TODO: NOT
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    private Pair replacement(Expr e){
        
        int vi = env.addBool();
        Literal v  = new Literal(env.vars.get(vi), true);
        Literal nv = new Literal(env.vars.get(vi), false);
        
        Expr ne = ConverterToNonNegatedOld.convert(
                ConverterToNonNegated.pushNegationDown(e));
                //ConverterToNonNegated.removeAllNegations(new Not(e)));
        
        return new Pair(v, 
                And(Or(ne,v), Or(nv,e)));
    }    
    private mOr Or(Expr e1, Expr e2){
        mOr e = new mOr();
        e.add(e1);
        e.add(e2);
        return e;
    }
    private mAnd And(Expr e1, Expr e2){
        mAnd e = new mAnd();
        e.add(e1);
        e.add(e2);
        return e;
    }

    /**
     * see Nonnengart et al. On generating small clause normal form.
     * @param s  Expression
     * @param p  position index
     * @return
     */
    private boolean shouldReplaceIn(Expr s, int p){
        boolean pag2 = (pg1(s) && asg2(s, p))
                || (pg2(s) && asg1(s, p));
        boolean pbg2 = (npg1(s) && bsg2(s, p))
                || (npg2(s) && bsg1(s, p));

        boolean pag1 = ( pg1(s) && asg1(s, p));
        boolean pbg1 = (npg1(s) && bsg1(s, p));        
        
        return pag2 || pbg2 || (pag1 && pbg1);
    }

    /**
     * Number of clauses in formula is *g*reater than *1*
     * @param e
     * @return
     */
    private boolean pg1(Expr e){
        switch(e.type){
            case AND:
                return true;
            case OR:
                return pg1(((Or)e).left) || pg1(((Or)e).right);
            case MAND:
                if(((mAnd)e).childs.size()>1)
                    return true;
                else if(((mAnd)e).childs.size()<1)
                    return false;                                   
                else 
                    return pg1(((mAnd)e).childs.get(0));
            case MOR:
                for(Expr a: (mOr)e)
                    if(pg1(a))
                        return true;
                return false;
            case LIT:
                return false;
            case NOT:
                return npg1(((Not)e).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    private boolean npg1(Expr e){
        switch(e.type){
            case OR:
                return true;
            case AND:
                return pg1(((And)e).left) || pg1(((And)e).right);
            case MOR:
                if(((mOr)e).childs.size()>1)
                    return true;
                else if(((mOr)e).childs.size()<1)
                    return false;                                   
                else 
                    return npg1(((mOr)e).childs.get(0));
            case MAND:
                for(Expr a: (mAnd)e)
                    if(npg1(a))
                        return true;
                return false;
            case LIT:
                return false;
            case NOT:
                return pg1(((Not)e).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    private boolean pg2(Expr e){
        switch(e.type){
            case AND:
                return (pg1(((And)e).left) && pg1(((And)e).right)) 
                        || pg2(((And)e).left) 
                        || pg2(((And)e).right);
            case OR:
                return pg2(((Or)e).left) || pg2(((Or)e).right);
            case MAND:
                if(((mAnd)e).childs.size()>2)
                    return true;
                else if(((mAnd)e).childs.size()==2)
                    return pg1(((mAnd)e).childs.get(0)) ||
                            pg1(((mAnd)e).childs.get(1));
                else //if(((mAnd)e).childs.size()==1)
                    return pg2(((mAnd)e).childs.get(0));
            case MOR:
                int count = 0;
                for(Expr a: (mOr)e)
                    if(pg1(a))
                        count++;
                return count >= 2;
            case LIT:
                return false;
            case NOT:
                return npg2(((Not)e).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    private boolean npg2(Expr e){
        switch(e.type){
            case OR:
                return (pg1(((Or)e).left) && pg1(((Or)e).right)) 
                        || pg2(((Or)e).left) 
                        || pg2(((Or)e).right);
            case AND:
                return pg2(((And)e).left) || pg2(((And)e).right);
            case MOR:
                if(((mOr)e).childs.size()>2)
                    return true;
                else if(((mOr)e).childs.size()==2)
                    return pg1(((mOr)e).childs.get(0)) ||
                            pg1(((mOr)e).childs.get(1));
                else //if(((mOr)e).childs.size()==1)
                    return pg2(((mOr)e).childs.get(0));
            case MAND:
                int count = 0;
                for(Expr a: (mAnd)e)
                    if(pg1(a))
                        count++;
                return count >= 2;
            case LIT:
                return false;
            case NOT:
                return pg2(((Not)e).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+e.type.toString());
        }
    }
    /**
     * Number of multiplications of "number of clauses" of s[p] during
     * standart CNF conversion
     * 
     * @param e  expression containing element of interest
     * @param s  position of element of interest
     * @return
     */
    private boolean asg1(Expr s, int p){
        switch(s.type){
            case MOR:
                for(int i = 0; i<((mOr)s).childs.size();i++){                    
                    if(i!=p){
                        if(pg1(((mOr)s).childs.get(i)))
                            return true;
                    }
                }
                return false;
            case MAND:
                return false; //1
            case LIT:
                return false; // 1
//            case NOT:
//                return bsg1(((Not)s).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+s.type.toString());
        }        
    }
    private boolean asg2(Expr s, int p){
        switch(s.type){
            case MOR:
                int count = 0;
                for(int i = 0; i<((mOr)s).childs.size();i++){                    
                    if(i!=p){
                        if(pg2(((mOr)s).childs.get(i)))
                            return true;
                        if(pg1(((mOr)s).childs.get(i)))
                            count++;
                    }
                }
                return count>1;
            case MAND:
                return false; //1
            case LIT:
                return false; // 1
//            case NOT:
//                return bsg2(((Not)s).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+s.type.toString());
        }        
    }
    private boolean bsg1(Expr s, int p){
        switch(s.type){
            case MAND:
                for(int i = 0; i<((mAnd)s).childs.size();i++){                    
                    if(i!=p){
                        if(pg1(((mAnd)s).childs.get(i)))
                            return true;
                    }
                }
                return false;
            case MOR:
                return false; //1
            case LIT:
                return false; // 1
//            case NOT:
//                return asg1(((Not)s).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+s.type.toString());
        }        
    }
    private boolean bsg2(Expr s, int p){
        switch(s.type){
            case MAND:
                int count = 0;
                for(int i = 0; i<((mAnd)s).childs.size();i++){                    
                    if(i!=p){
                        if(pg2(((mAnd)s).childs.get(i)))
                            return true;
                        if(pg1(((mAnd)s).childs.get(i)))
                            count++;
                    }
                }
                return count>1;
            case MOR:
                return false; //1
            case LIT:
                return false; // 1
//            case NOT:
//                return asg2(((Not)s).child);
            default:
                throw new IllegalArgumentException(
                        "unrecognized node type:"+s.type.toString());
        }        
    }
}