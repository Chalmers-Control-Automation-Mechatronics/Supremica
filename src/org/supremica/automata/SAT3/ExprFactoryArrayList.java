/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author voronov
 */
public class ExprFactoryArrayList implements ExprFactory {

    private class Lit extends ArrayList<Expr> implements Expr{
        
        private int var;
        
        public Lit(int var){
            this.var = var;
        }
        
        public int getVar(){
            return var;
        }

        public Type getType() {
            return Expr.Type.LIT;
        }        
    }
    
    private class Mop extends ArrayList<Expr> implements Expr {
        
        private Expr.Type type;
        
        public Mop(Expr.Type type){
            this.type = type;
        }

        public Type getType() {
            return type;
        }
        
    }

    public Expr Or() {
        return new Mop(Expr.Type.OR);
    }

    public Expr Or(Expr e1, Expr e2) {
        return combine(e1, e2, Expr.Type.OR);
    }

    public Expr And() {
        return new Mop(Expr.Type.AND);
    }

    public Expr And(Expr e1, Expr e2) {
        return combine(e1, e2, Expr.Type.AND);
    }

    private Expr combine(Expr e1, Expr e2, Expr.Type type){
        Mop res = new Mop(type);
        /*res = */add(res,e1);
        /*return*/add(res,e2);
        return res;
    }
    /**
     * combine destructively two expression giving "type"
     * @param e1
     * @param e2
     * @param type     AND or OR
     * @return
     */
    private Expr combineD(Expr e1, Expr e2, Expr.Type type){
        if(e1.getType()==type){
            return add(e1, e2);
        } else if (e2.getType() == type){
            return add(e2, e1);
        } else {
            return combine(e1,e2, type);
        }                
    }
    
    public Expr Lit(int varNumber) {
        return new Lit(varNumber);
    }

    public Expr Not(Expr e) {
        switch(e.getType()){
            case LIT:
                return Lit(-((Lit)e).getVar());
            case AND:
            case OR:
                Mop res = new Mop(oppositeType(e.getType()));
                for(Expr f: (Mop)e){
                    res.add(Not(f));
                }
                return res;
            default:
                throw new IllegalArgumentException(
                        "unknown type: " + e.getType().toString());
        }    
    }

    public Collection<Integer> PlainToCollection(Expr expr) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        switch(expr.getType()){
            case LIT:
                res.add(((Lit)expr).getVar());
                return res;
            case OR:
                for(Expr elem: expr){
                    switch(elem.getType()){
                        case LIT:
                            res.add(((Lit)elem).getVar());
                            break;
                        default:
                            throw new IllegalArgumentException(
                                "this is not a clause, it contains: " + 
                                elem.getType().toString());                            
                    }
                }
                return res;
            default:
                StringBuilder sb = new StringBuilder();
                buildStringFromExpr(expr,sb);
                throw new IllegalArgumentException(
                        "this is not a clause, this is: " + expr.getType().toString()+
                        " "+ sb.toString());
        }
    }
    
    public Expr add(Expr big, Expr e){
        if(big.getType()==Expr.Type.LIT)
            throw new IllegalArgumentException("can't add to LIT");
        else if(e instanceof Mop){
            if(e.getType()==big.getType() || ((Mop)e).size()==1)
                for(Expr e1: (Mop)e)
                    add(big, e1);
            else if (((Mop)e).size()>1)
                big.add(e);
            // else if e.size == 0 do nothing
        }
        else // e is Lit
            big.add(e);        
        
        return big;
    }

    private Expr.Type oppositeType(Expr.Type t){
        switch(t){
            case AND: return Expr.Type.OR;                      
            case OR:  return Expr.Type.AND;                       
            default:   throw new IllegalArgumentException(
                        "don't know opposite to " + t.toString());
        }
    }    
    
    public void buildStringFromExpr(Expr e, StringBuilder sb){
        switch(e.getType()){
            case LIT:
                sb.append(LitToString(e)+" ");
                break;
            case AND:
                sb.append("AND(");
                for(Expr elem: e)
                    buildStringFromExpr(elem, sb);
                sb.append(")");
                break;
            case OR:
                sb.append("OR(");
                for(Expr elem: e)
                    buildStringFromExpr(elem, sb);
                sb.append(")");
                break;
        }
    }

    public String LitToString(Expr expr) {
        return Integer.toString(((Lit)expr).getVar());
    }
}
