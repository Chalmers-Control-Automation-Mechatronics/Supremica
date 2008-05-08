/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public class ProblemBasics {
    
    ExprAcceptor acceptor;
    ExprFactory ef;
    VarsLayout vl;
    
    /**
     * 
     * @param ef       AND,OR,LIT
     * @param vl       Vars layout 
     * @param acceptor acceptor of results
     */
    public ProblemBasics(ExprFactory ef, VarsLayout vl, ExprAcceptor acceptor){
        this.acceptor = acceptor;
        this.vl = vl;
        this.ef = ef;
    }    
    
    
    /**
     * ti=e -> ( bigOR qi=x & q_(i+1)=f(x,e), forAll x: f(x,e) defined )
     * 
     * if event belongs to automaton but is never used, return TRUE (=nothing)
     * if event to not belong to automaton, throw exception
     * 
     * @param step
     * @param a
     * @param ev 
     */
    public void transition(int step, Automaton a, LabeledEvent ev){
        
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
    /**
     * t_i = e   ->   x_i = x_(i+1)
     * @param step
     * @param a
     * @param ev
     */
    public void stay(int step, Automaton a, LabeledEvent ev){
        yeild(impl(
                vl.TransitionEqLabel(step, ev),
                vl.KeepState(a, step)));
    }
    
    /**
     * q_0 = a.initial
     * @param a
     */
    public void initialCondition(Automaton a){
        if(!a.hasInitialState())
            throw new IllegalArgumentException(
                    "automaton "+a.getName()+
                    " has no initial state to create expression for initial condition");
        yeild(vl.StateEqState(a, 0, a.getInitialState()));
    }
    /**
     * t_0=e | t_1=1 | ... | t_(n-1)=e | t_n = e
     * @param ev        marking event
     * @param steps     number <b>n</b> of steps
     */
    public void fireMarkingEvent(LabeledEvent ev, int steps){
        Expr res = ef.Or();
        for(int step = 0; step <= steps; step++)
            res.add(vl.TransitionEqLabel(step, ev));
        yeild(res);
    }
    
    public void ensureOneEventThisStep(int step){
        yeild(vl.EnsureOneEventThisStep(step));
    }
    public void ensureOneStateThisStep(int step){
        yeild(vl.EnsureOneStateThisStep(step));
    }
    
    private Expr impl(Expr e1, Expr e2){
        return ef.Or(ef.Not(e1), e2);
    }
            
    private void yeild(Expr expr){
        acceptor.accept(expr);
    }

}
