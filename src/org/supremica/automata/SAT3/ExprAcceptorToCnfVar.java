/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.io.PrintWriter;


/**
 *
 * @author voronov
 */
public class ExprAcceptorToCnfVar implements ExprAcceptor {
    
    ExprFactory ef;
    CnfClauseAcceptor acceptor;

    public ExprAcceptorToCnfVar(ExprFactory ef, CnfClauseAcceptor acceptor){
        this.ef = ef;
        this.acceptor = acceptor;
    }

    public void accept(Expr expr) {
        Expr cnf = fun(expr);
        switch(cnf.getType()){
            case LIT:
                acceptor.accept(ef.PlainToCollection(cnf));
                break;
            case AND:
                try{
                for(Expr elem: cnf)
                    acceptor.accept(ef.PlainToCollection(elem));
                } catch(IllegalArgumentException e){
                    (new ExprAcceptorPlainPrint(ef, new PrintWriter(System.err, true))).accept(cnf);
                    throw e;
                }
                break;                
            case OR:
                if(isOnlyLits(expr))
                    acceptor.accept(ef.PlainToCollection(cnf));
                else 
                    throw new IllegalArgumentException(
                            "bad conjunction out of CNF conversion");
                break;
            default:
                throw new IllegalArgumentException(
                        "unexpeted type of cnf conversion:" +
                        cnf.getType().toString());                
        }
    }

    private Expr fun(Expr expr){
        switch(expr.getType()){
            case LIT:
                return expr;
            case AND:
                Expr res = ef.And();
                for(Expr elem: expr)
                    res = ef.add(res, fun(elem));
                return res;
            case OR:
                if(isOnlyLits(expr))
                    return expr;
                else
                    return fun(permute(onlyAnds(expr)));
            default:
                throw new IllegalArgumentException("unexpected strange type");
        }
    }
    
    private boolean isOnlyLits(Expr expr){
        for(Expr elem: expr)
            if(elem.getType()!=Expr.Type.LIT)
                return false;
        
        return true;
    }
    
    /**
     * move all free literal of disjunction (OR) to single AND inside of this OR
     * 
     * @param expr disjunction to convert
     * @return
     */
    private Expr onlyAnds(Expr expr){
        Expr res = ef.Or();
        Expr lits = ef.And();
        for(Expr elem: expr){
            switch(elem.getType()){
                case AND:
                    res = ef.add(res, elem);
                    break;
                case LIT:
                    lits = ef.add(lits, elem);
                    break;
                default:
                    throw new IllegalArgumentException("and or lit expected");
            }
        }
        if(lits.size()>0)
            res = ef.add(res, lits);
        
        return res;
    }
    /**
     * <pre>
     * a&b | x    ->  a|x  &  b|x 
     * a&b | x&y  ->  a|x  &  a|y  &  b|x  & b|y 
     * abc | xyz  ->  ax ay az   bx by bz   cx cy cz
     * ab cd xy   ->  acx acy adx ady    bcx bcy bdx bdy
     * </pre>
     * @param souDis
     * @return
     */
    private Expr permute(Expr souDis){
        
        Expr resConj = ef.And();
        
        for(Expr oneSouCon: souDis){
            Expr newResConj = ef.And();
            for(Expr oneElemOfSouCon: oneSouCon){
                if(resConj.size()==0)
                    newResConj = ef.add(newResConj,oneElemOfSouCon);
                else
                    for(Expr oneResDis: resConj){
                        newResConj = ef.add(newResConj, ef.Or(oneResDis, oneElemOfSouCon));
                }                
            }
            resConj = newResConj;
            //newResConj = ef.And();
        }
        return resConj;
    }        
}
