/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public class ProblemBasics {
    
    private ToCnf acceptor;
    ExprFactory ef;
    Automata ats;
    VarsLayout vl;
    
    private void init(){
        for(Automaton a: ats)
            yeild(vl.StateEqState(a, 0, a.getInitialState()));
    }
    
    
    
    
    /**
     * ti=e -> ( bigOR qi=x & q[i+1]=f(x,e), forAll x: f(x,e) defined )
     * 
     * if event belongs to automaton but is never used, return TRUE (or nothing)
     * if event to not belong to automaton, throw exception
     * 
     * @return
     */
    private void transition(int step, Automaton a, LabeledEvent ev){
        
        if(!a.getAlphabet().contains(ev))
            throw new IllegalArgumentException("event don't belong to automaton");
        
        Expr t = ef.Or();
        for(State x: a)
            if(x.doesDefine(ev))
                t.add(ef.And(
                        vl.StateEqState(a, step, x), 
                        vl.StateEqState(a, step+1, x.nextState(ev))));
        if(t.size()>0)
            yeild(impl(vl.TransitionEqLabel(step, ev), t));
        // else return ef.TRUE <=> do nothing        
    }
    
    private Expr impl(Expr e1, Expr e2){
        return ef.Or(ef.Not(e1), e2);
    }
    
        
    private void yeild(Expr expr){
        acceptor.accept(expr);
    }

}
