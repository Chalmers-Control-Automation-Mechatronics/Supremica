/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.ArrayList;
import java.util.HashMap;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public class VarsLayoutStepSeq implements VarsLayout {
    
    
    /*
     *      | step 0                              | step 1
     *      + - - - - - - - - - - - - - - - - - - + - - - - - 
     * true | event | state A | state B | state C | event | state A ...
     * 
     * */
    

    /** all automata to be encoded */
    Automata ats;
    
    /** number of bool variables to represent transition variable */
    int tranVarWidth;
    
    /** number of bool variables to represent transition and states of one step */
    int stepWidth;
    
    ExprFactory ef;
    
    CoDecIntToBool coder;
    
    private HashMap<LabeledEvent, Integer> eventsIndexes;
    private HashMap<Automaton, Integer>    automataIndexes;
    private HashMap<Automaton, HashMap<State, Integer>> statesIndexes;
    
    /**
     * @param ats    Automata to code
     * @param ef     basic operations for expressions (AND,OR,LIT)
     * @param coder  convert integer to list of booleans
     */
    public VarsLayoutStepSeq(
            Automata ats, 
            ExprFactory ef, 
            CoDecIntToBool coder
            )
    {
        this.ats     = ats;
        this.ef      = ef;
        this.coder   = coder;
        
        
        tranVarWidth = width(ats.getUnionAlphabet().size());
        stepWidth    = getStepWidth(tranVarWidth, ats);
        
        eventsIndexes   = reindex(ats.getUnionAlphabet());
        automataIndexes = reindex(ats);
        statesIndexes   = reindexStates(ats);
    }
    
    private int getStepWidth(int tranVarWidth, Automata ats){
        int sum = 0;
        for(Automaton a: ats)
            sum += width(a);
        return tranVarWidth + sum;
    }
    
    public Expr StateEqState(Automaton a, int step, State s) {
        return eqBits(
                toBits(getIndex(a,s), width(a)), 
                stepStart(step)+startStateVar(a));
    }
    private int startStateVar(Automaton a){
        int res = tranVarWidth;
        for(Automaton ai: ats){
            if(getIndex(a)==getIndex(ai))
                return res;
            res += width(ai);
        }
        throw new IllegalArgumentException(
                "automaton " + a.getName() + " not found, getIndex is: " + getIndex(a));
    }

    public Expr TransitionEqLabel(int step, LabeledEvent e) {
        return eqBits(toBits(getIndex(e), tranVarWidth), stepStart(step));
    }

    /**
     * stateA(i) <-> stateA(i+1)
     * @param a
     * @param currentStep
     * @return
     */
    public Expr KeepState(Automaton a, int currentStep) {
        return equals(width(a), 
                stepStart(currentStep)+startStateVar(a),
                stepStart(currentStep+1)+startStateVar(a));
    }

    /**
     * a <-> b :  a1->b1 & b1->a1 & a2->b2 & b2->a2
     * @param width
     * @param from1
     * @param from2
     * @return
     */
    private Expr equals(int width, int from1, int from2){
        Expr res = ef.And();
        for(int i = 0; i < width; i++){
            res.add(impl(ef.Lit(from1+i), ef.Lit(from2+i)));
            res.add(impl(ef.Lit(from2+i), ef.Lit(from1+i)));
        }
        return res;
    }
    private Expr impl(Expr e1, Expr e2){
        return ef.Or(ef.Not(e1), e2);
    }
    
    /**
     * Assigns list of bits to variables, starting from varFrom
     * 
     * Example: (bits=[1,1,0,1], varFrom=4) -> Expr: AND(4 5 -6 7)
     * 
     * @param valueBits
     * @param varFrom
     * @return
     */
    private Expr eqBits(ArrayList<Boolean> valueBits, int varFrom){
        Expr res = ef.And();
        int var = varFrom;
        for(boolean bit: valueBits){
            Expr lit = ef.Lit(var);
            if(!bit)
                lit = ef.Not(lit);
            var++;
            res.add(lit);
        }
        return res;
    }
    
    /** first variable of the step
     * @param step
     * @return
     */
    private int stepStart(int step){
        return    1                /* 0 can't be used (+0 -0) */ 
                + 1                /* we reserved 1 for TRUE  */ 
                + step*stepWidth;
    }
    
    private int width(Automaton a){
        return width(a.nbrOfStates());        
    }

    /** convert any value of given width to its bit representation
     * 
     * @param val    value
     * @param width  width (number of bits to use)
     * @return       list of bits of length "width"
     */
    private ArrayList<Boolean> toBits(int val, int width){
        return coder.toBits(val, width);
    }
        
    private int width(int value){
        return coder.width(value);
    }
    
    /**
     * (e=1 & e!=2 & e!=3) | (e!=1 & e=2 & e!=3) | ( )
     * @param step
     * @return
     */
    private Expr ensureOneEventThisStepOld(int step){
        Expr e1 = ef.Or();
        for(LabeledEvent evEq: ats.getUnionAlphabet()){
            Expr e2 = ef.And();
            for(LabeledEvent evAll: ats.getUnionAlphabet()){
                Expr e = TransitionEqLabel(step, evAll);
                if(evAll.equals(evEq))
                    e2.add(e);
                else
                    e2.add(ef.Not(e));    
            }
            e1.add(e2);
        }
        return e1;        
    }
    
    private Expr ensureOneStateThisStepOld(int step, Automaton a){
        Expr e1 = ef.Or();
        for(State sEq: a){
            Expr e2 = ef.And();
            for(State sAll: a){
                Expr e = StateEqState(a, step, sAll);                
                if(sAll.equals(sEq))
                    e2.add(e);
                else
                    e2.add(ef.Not(e));    
            }
            e1.add(e2);
        }
        return e1;
    }
    /**
     * e=1 | e=2 | e=3
     * @param step
     * @return
     */
    public Expr EnsureOneEventThisStep(int step){
        Expr res = ef.Or();
        for(LabeledEvent ev: ats.getUnionAlphabet())
            res = ef.add(res, TransitionEqLabel(step, ev));
        
        return res;        
    }
    
    public Expr EnsureOneStateThisStep(int step){
        Expr res = ef.And();
        for(Automaton a: ats)
            res.add(ensureOneStateThisStep(step, a));            
        return res;
    }
    
    private Expr ensureOneStateThisStep(int step, Automaton a){
        Expr res = ef.Or();
        for(State s: a)
            res = ef.add(res, StateEqState(a, step, s));
        
        return res;
    }

    public int FirstFreeAfterNSteps(int steps) {
        return stepStart(steps);
    }
    
    private <T> HashMap<T, Integer> reindex(Iterable<T> collection){
        HashMap<T, Integer> map = new HashMap<T, Integer>();
        int i = 0;
        for(T a: collection){
            map.put(a, i);
            i++;
        }
        
        return map;        
    }
    private HashMap<Automaton, HashMap<State, Integer>> reindexStates(Automata ats){
        HashMap<Automaton, HashMap<State, Integer>> map = 
                new HashMap<Automaton, HashMap<State, Integer>>();
        for(Automaton a: ats)
            map.put(a, reindex(a));
        
        return map;
    }

    private int getIndex(LabeledEvent ev){
        return eventsIndexes.get(ev);
    }
    private int getIndex(Automaton a){
        return automataIndexes.get(a);
    }
    private int getIndex(Automaton a, State s){
        return (statesIndexes.get(a)).get(s);
    }
    

}
