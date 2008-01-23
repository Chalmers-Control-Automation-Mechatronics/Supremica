/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT2;

import java.io.PrintStream;
import java.util.*;
//[]
/**
 * From paper by Clarke, Biere, Raimi, Zhu
 * "Bounded Mdel Checking Using Satisfiability Solving"
 *
 * @author alex
 */
public class Convert {
    
    public static enum ExType{
        MAND,
        MOR,
        LIT
    }
    
    public static ExType opposite(ExType t){
        switch(t){
            case MAND: return ExType.MOR;
            case MOR:  return ExType.MAND;
            default:   throw new IllegalArgumentException(
                        "don't know opposite to " + t.toString());
        }
    }
    
    public static interface Expr{
        public ExType getType();
    }

    public static class MOp extends HashSet<Expr> implements Expr{
        public final ExType type;
        public ExType getType(){
            return type;
        }
        public MOp(ExType t){
            type = t;
        }
        @Override
        public int hashCode(){
            int code = 0;
            for(Expr e: this)
                code += e.hashCode();
            return code*((type == ExType.MAND)?13:17);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MOp other = (MOp) obj;
            if(type != other.type)
                return false;
            if(size() != other.size())
                return false;
            for(Expr e: other)
                if(!this.contains(e))
                    return false;
            
            return true;
        }
    }    
    public static class Lit implements Expr{
        private final ExType type;
        public final int var; 
        @Override
        public int hashCode(){
            return var;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Lit other = (Lit) obj;
            if (this.var != other.var) {
                return false;
            }
            return true;
        }
        public ExType getType(){
            return type;
        }
        public Lit(int v){
            var = v;
            type = ExType.LIT;
        }
    }
        
    public static class Clauses extends HashSet<Clause>{
    }
    public static class Clause extends HashSet<Integer>{        
    }
    
    // ---- Variables ----
    private Map<Expr, Integer> cache = new HashMap<Expr,Integer>();
    // remember to reserve one variable for "TRUE" and never use variable "0"
    public int varCounter;  
    public int trueVar=1;
    
    PrintStream out = System.out;
    int clauseCounter = 0;
    // ----
    
    /**
     * OBS! do not forget to add trueVar explicitly to CNF if not use removeTrue
     * @param trueVar_
     * @param totalUsedVars
     */
    public Convert(int trueVar_, int totalUsedVars){
        varCounter = totalUsedVars;
        trueVar = trueVar_;
    }
    
    public Clauses convert(Expr f){
        return removeTrue(subst(f, trueVar), trueVar);
    }
    
    public Clauses subst(Expr f, int vf){
        if(wasSubstituted(f))
            return toCnfVarEqVar(vf, getCached(f));
        else if(atomic(f))
            return toCnfVarEqExpr(vf, f);
        else {
            Clauses res = 
                    new Clauses();
            MOp ori = new MOp( ((MOp)f).type);
            for(Expr h: (MOp)f){
                if(h.getType()==ExType.LIT){ // already atom
                    ori.add(h);
                } else {
                    int vh = nextBool();
                    Clauses ch = subst(h, vh);
                    res.addAll(ch);
                    ori.add(Lit(vh)); // TODO: "Lit" or somehow else?                    
                }
            }
            res.addAll(toCnfVarEqExpr(vf, ori));
            cache(f, vf);
            return res;
        }
    }
            
    public void substOnline(Expr f, int vf){
        if(wasSubstituted(f))
            printClauses(toCnfVarEqVar(vf, getCached(f)));
        else if(atomic(f))
            printClauses(toCnfVarEqExpr(vf, f));
        else {
            MOp ori = new MOp( ((MOp)f).type);
            for(Expr h: (MOp)f){
                if(h.getType()==ExType.LIT){ // already atom
                    ori.add(h);
                } else {
                    int vh = nextBool();
                    ori.add(Lit(vh)); // TODO: "Lit" or somehow else?                    
                    substOnline(h, vh);
                }
            }
            printClauses(toCnfVarEqExpr(vf, ori));
            cache(f, vf);
        }
    }

    private void printClauses(Clauses cs){
        for(Clause c: cs){
            clauseCounter++;
            for(int i: c)
                out.print(""+i+" ");
            out.print("0\n");
        }        
    }
    
    
    private void cache(Expr f, int vf){
        cache.put(f, vf);
    }
    private boolean wasSubstituted(Expr f){
        return cache.containsKey(f);
    }
    private int getCached(Expr f){
        return cache.get(f);
    }
    
    private static Clauses toCnfVarEqVar(int u, int v){
        Clauses res = new Clauses();
        
        Clause c1 = new Clause();
        c1.add(-u); 
        c1.add(v);
        res.add(c1);
        
        Clause c2 = new Clause();
        c2.add(-v);
        c2.add(u);
        res.add(c2);
        
        return res;
    }
    private static Clauses toCnfVarEqExpr(int v, Expr e){
        Clauses res = new Clauses();
        switch(e.getType()){
            case LIT:
                return toCnfVarEqVar(v, ((Lit)e).var);
            case MAND:
                // a&b&c -> x  == (!a, !b, !c, x)
                {
                    Clause c = new Clause();
                    for(Expr f: (MOp)e){
                        //we assume them atomic!
                        c.add(-((Lit)f).var);
                    }
                    c.add(v);
                    res.add(c);
                }
                    
                // x -> a&b&c  ==  (!x,a)(!x,b)(!x,c)
                for(Expr f: (MOp)e){
                    Clause c = new Clause();
                    c.add(-v);                    
                    c.add(((Lit)f).var); //we assume them atomic!
                    res.add(c);
                }
                
                return res;
            case MOR:

                // a b c -> x  == (!a,x) (!b,x) (!c,x)
                for(Expr f: (MOp)e){
                    Clause c = new Clause();
                    c.add(-((Lit)f).var); //we assume them atomic!
                    c.add(v);                    
                    res.add(c);
                }
                // x -> a b c  ==  (!x,a,b,c)
                {
                    Clause c = new Clause();
                    c.add(-v);
                    for(Expr f: (MOp)e){
                        //we assume them atomic!
                        c.add(((Lit)f).var);
                    }
                    res.add(c);
                }
                    
                
                return res;                    
            default:
                throw new IllegalArgumentException(
                        "unknown type: " + e.getType().toString());            
        }        
    }
    private static boolean atomic(Expr f){
        switch(f.getType()){
            case LIT: 
                return true;
            case MAND:
            case MOR:
                for(Expr e: (MOp)f)
                    if(e.getType()!=ExType.LIT)
                        return false;
                return true;
            default:
                throw new IllegalArgumentException(
                        "unknown type: " + f.getType().toString());            
        }
    }
    private int nextBool(){
        varCounter++;
        return varCounter;
    }

 
    public static Expr Lit(int i){
        return new Lit(i);
    }
    public static Expr And(Expr e1, Expr e2){
        MOp res = new MOp(ExType.MAND);
        add(res,e1);
        add(res,e2);
        return res;
    }
    public static Expr Or(Expr e1, Expr e2){
        MOp res = new MOp(ExType.MOR);
        add(res,e1);
        add(res,e2);
        return res;
    }
    public static Expr Impl(Expr e1, Expr e2){
        return Or(Not(e1), e2);
    }
    public static Expr Eq(Expr e1, Expr e2){
        return And(Impl(e1,e2), Impl(e2,e1));
    }
    private static void add(MOp big, Expr e){
        if(e.getType()==big.type)
            big.addAll((MOp)e);
        else 
            big.add(e);
    }
    public static Expr Not(Expr e){
        switch(e.getType()){
            case LIT:
                return Lit(-((Lit)e).var);
            case MAND:
            case MOR:
                MOp res = new MOp(opposite(e.getType()));
                for(Expr f: (MOp)e){
                    res.add(Not(f));
                }
                return res;
            default:
                throw new IllegalArgumentException(
                        "unknown type: " + e.getType().toString());
        }
    }
    
    public static void print(Clauses cs, int totalVars, PrintStream out){
        out.println("p cnf " + totalVars + " " + cs.size());
        for(Clause c: cs){
            for(int i: c)
                out.print(""+i+" ");
            out.println("0");
        }
    }
    public static String toDimacsCnfString(Clauses cs, int totalVars){
        StringBuilder sb = new StringBuilder();
        sb.append("p cnf " + totalVars + " " + cs.size() + "\n");
        for(Clause c: cs){
            for(int i: c){
                sb.append(""+i+" ");
            }
            sb.append("0\n");
        }
        sb.append("  \n");
        return sb.toString();
    }
    public static Clauses removeTrue(Clauses cs, int trueValue){
        Clauses csRes = new Clauses();
        for(Clause c: cs){
            boolean addNextClause = true;
            Clause cRes = new Clause();
            for(int i: c){
                if(i==trueValue)
                    addNextClause = false;
                if(i != -trueValue)
                    cRes.add(i);                    
            }
            if(addNextClause)
                csRes.add(cRes);
        }
        Clause one = new Clause();
        one.add(trueValue);
        csRes.add(one);
        return csRes;
    }
    
    public static Expr te = Or(And(Lit(1),Lit(2)), Or(And(Lit(3),Lit(4)), And(Lit(1),Lit(3))) );
    
    public static void main(String[] args){
        do1();
        System.out.println("---");
        do3();
    }
    private static void do1(){
//        Expr e = Or(And(Lit(1), Lit(2)),Not(And(Lit(3), Lit(4))));
        Convert conv = new Convert(10, 10);
        Clauses cs = conv.convert(te);
        //Convert.print(cs, conv.varCounter, System.out);
        System.out.println(toDimacsCnfString(cs, conv.varCounter));
    }
    private static void do2(){
        
        Expr e1 = Or(Lit(1),Lit(2));
        Expr e2 = And(Lit(1),Lit(2));
        
        System.out.println("e1 h: " + e1.hashCode());
        System.out.println("e2 h: " + e2.hashCode());
        System.out.println(e1.equals(e2)?"eq":"ne!");
    }
    private static void do3(){
//        Expr e = Or(And(Lit(1), Lit(2)),Not(And(Lit(3), Lit(4))));
        int t = 10;
        Convert conv = new Convert(t,10);
        conv.substOnline(te, t);
        System.out.println(""+t+" 0");
        System.out.println("p cnf " 
                + conv.varCounter + " " 
                + (conv.clauseCounter+1));
        
    }
}
