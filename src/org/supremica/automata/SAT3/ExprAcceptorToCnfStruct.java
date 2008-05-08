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
 *       - if OR contains more than one literal together with this AND, 
 *         or contains other AND:s, then we replace this one in a structure-preserving way
 * 
 * @author voronov
 */
public class ExprAcceptorToCnfStruct implements ExprAcceptor {

    private int curNew;
    private CnfClauseAcceptor acceptor;
    private ExprFactory ef;
    
    public void accept(Expr expr) {
        fun(expr);
    }

    public ExprAcceptorToCnfStruct(int startVariable, ExprFactory ef, CnfClauseAcceptor acceptor){
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
                } else if (isAtomicOr(expr)) {
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
     * Determines if there is only two elements in this OR, 
     * at least one of which is Literal.
     * 
     * Only formulas that are atomicOr:s have to be sent to 
     * variable-preserving conversion. 
     * 
     * @param expr  disjunction (OR) for check, with some non-LIT:s
     * @return
     */
    private boolean isAtomicOr(Expr expr){        
        if(expr.size()==2)
            for(Expr elem: expr)
                if(elem.getType()==Expr.Type.LIT)
                    return true;
        return false;
    }
        
        
    /**
     * Convert OR that is atomicOr, i.e. has two elems, one of which is LIT,
     * to CNF using variable-preserving transformation
     * 
     * @param expr expression to convert. Precond.: atomicOr(expr) should be true
     * @return expression in CNF
     */
    private Expr OrToCnfVP(Expr expr){
        Expr lit = null, and = null;
        for(Expr elem: expr){
            switch(elem.getType()){
                case LIT: lit = elem; break;
                case AND: and = elem; break;
                default:
                    throw new IllegalArgumentException("unexpected type");                    
            }
        }
        if(lit==null)
            throw new IllegalArgumentException("no LIT found");
        if(lit==null)
            throw new IllegalArgumentException("no AND found");

        Expr res = ef.And();
        for(Expr elem: and)
            res.add(ef.Or(lit, elem));
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
