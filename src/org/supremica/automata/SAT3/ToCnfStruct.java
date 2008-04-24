/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

/**
 * From paper by Clarke, Biere, Raimi, Zhu
 * "Bounded Model Checking Using Satisfiability Solving"
 *
 * Algorithm:
 * ----------
 * 
 * procedure bool-to-cnf(f, vf)
 * {
 *   case 
 *     cached(f) == v:
 *       return clause(vf <-> v);
 *     atomic(f):
 *       return clause(f <-> vf);
 *     f == h . g:
 *       C1 = bool-to-cnf(h,vh);
 *       C2 = bool-to-cnf(g,vg);
 *       cached(f) = vf;
 *       return clause(vf <-> vh . vg) Union C1 Union C2;
 *   esac;
 * }
 * 
 *
 * 
 * I will try to modify it. We have AND, OR and LIT
 * If AND came, we recursively request that each of it's members to be in CNF.
 * If LIT came, it is in CNF
 * If OR came, we check each of its members
 *   - if it is LIT, we keep it
 *   - if it is AND, we modify:
 *       - if OR was atomic, i.e.  except *this AND* there is nothing or one literal,
 *         we replace it in a variables-preserving way
 *       - if OR contains more than one literal (or contains AND), then we replace
 *         this one in a structure-preserving way
 * 
 * @author voronov
 */
public class ToCnfStruct implements ToCnf {

    private int curNew;
    private CnfClauseAcceptor acceptor;
    private ExprFactory ef;
    
    public void accept(Expr expr) {
        fun(expr);
    }

    public ToCnfStruct(int startVariable, ExprFactory ef, CnfClauseAcceptor acceptor){
        this.curNew   = startVariable;
        this.ef       = ef;
        this.acceptor = acceptor;        
    }
    
    private void fun(Expr expr){        
        switch(expr.getType()){
            case AND:
                for(Expr elem: expr)
                    fun(elem);
                break;
            case LIT:
                yield(expr);
                break;
            case OR:
                if(onlyLiterals(expr)){
                    yield(expr);                    
                } else if (expr.size()==0) {
                    throw new IllegalArgumentException("OR with no childs");
                } else if (expr.size()==1) {
                    for(Expr elem: expr)
                        fun(elem);
                } else if (atomicOr(expr)) {
                    fun(OrToCnfVP(expr));
                } else {
                    Expr res = ef.Or();
                    for(Expr elem: expr){
                        switch(elem.getType()){
                            case LIT:
                                res.add(elem);
                                break;
                            case AND:
                                // add v instead of el
                                res.add(ef.Lit(curNew));
                                curNew++;
                                // v 'implies' el
                                fun(ef.Or(ef.Not(ef.Lit(curNew-1)), elem  ));
                                // el 'implies' v
                                fun(ef.Or( ef.Lit(curNew-1 ), ef.Not(elem) ));
                                break;
                            default:
                                throw new IllegalArgumentException("we expected Lit or And here...");
                        }
                    }
                    yield(res);
                }
                break;                
        }        
    }    
    /**
     * Atomic will correspond that this expression will be sent to 
     * variable-preserving conversion. 
     * This should be done if there is only two elements, one of which is Literal
     * 
     * @param expr  disjunction (OR) with not all literals for check
     * @return
     */
    private boolean atomicOr(Expr expr){        
        if(expr.size()==2)
            for(Expr elem: expr)
                if(elem.getType()==Expr.Type.LIT)
                    return true;
        return false;
    }
        
        
    /**
     * Convert atomic to cnf. two elements, one of which is literal and another is and
     * 
     * @param expr expression to convert. Precond.: atomicOr(expr) should be true
     * @return expression in CNF
     */
    private Expr OrToCnfVP(Expr expr){
        Expr lit = null, and = null;
        for(Expr elem: expr){
            if(elem.getType()==Expr.Type.LIT)
                lit = elem;
            else if(elem.getType()==Expr.Type.AND)
                and = elem;            
        }
        if(lit==null)
            throw new IllegalArgumentException("no literal found");
        if(lit==null)
            throw new IllegalArgumentException("no AND found");

        Expr res = ef.And();
        for(Expr elem: and)
            res = ef.And(res, ef.Or(lit, elem));
        return res;        
    }
    
    private boolean onlyLiterals(Expr expr){
        for(Expr elem: expr)
            if(elem.getType()!=Expr.Type.LIT)
                return false;
        return true;
    }
    
    private void yield(Expr expr){
        acceptor.accept(ef.PlainToCollection(expr));
    }
}
