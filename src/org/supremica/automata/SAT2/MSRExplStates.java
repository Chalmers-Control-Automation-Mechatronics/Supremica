/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT2;

import org.supremica.automata.*;
import org.supremica.automata.IO.ProjectBuildFromXML;

/**
 *
 * @author alex
 */
public class MSRExplStates {
    
    Automata ats; 
    static final int trueVariable = 1;
    /**
     * total steps is total number of transitions, 
     * thus number of states is one more (totalSteps+1)
     */
    int totalSteps;
    
    static final LabeledEvent markingEvent = new LabeledEvent(MSR.MARKING_NAME);
    
    public MSRExplStates(Automata ats_, int steps){
        ats = ats_;
        totalSteps = steps;
    }
    
    private Convert.Expr tran(){
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);
        for(Automaton a: ats){
            for(int step = 0; step < totalSteps; step++){
                for(LabeledEvent e: a.getAlphabet()){
                    Convert.MOp tran = new Convert.MOp((Convert.ExType.MOR));
                    for(State s: a){
                        if(s.doesDefine(e)){
                            Convert.add(tran, Convert.And(
                                    stateEq(step, a, s),
                                    stateEq(step+1, a, s.nextState(e))));
                        }
                    }                
                    // TODO: what to do if event in alphabet but not in transitions?
                    // should imply false
                    if(tran.size()<1){
                        System.err.println(" warning: event " + e.getLabel() + 
                                " has no transitions");
                        tran.add(Convert.Not(Convert.Lit(trueVariable)));
                    }
                    Convert.add(res, Convert.Impl(tranEq(step, e), tran));
                }
            }
        }
        return res;
    }
    
    private Convert.Expr stay(){
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);
        for(Automaton a: ats){
            for(int step = 0; step < totalSteps; step++){
                Convert.MOp allEvents = new Convert.MOp(Convert.ExType.MOR);
                for(LabeledEvent e: a.getAlphabet()){
                    Convert.add(allEvents, tranEq(step, e));
                }
                res.add(Convert.Or(keepState(step, a), allEvents));
            }                        
        }
        return res;
    }
    private Convert.Expr init(){
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);
        for(Automaton a: ats){
            Convert.add(res, stateEq(0, a, a.getInitialState()));
        }
        return res;
    }    
    private Convert.Expr goalMarkingEvent(){
        Convert.MOp res = new Convert.MOp(Convert.ExType.MOR);
        for(int step = 0; step < totalSteps; step++)
            Convert.add(res, tranEq(step, markingEvent));
        return res;
    }
    
    /*
     *      | step 0                              | step 1
     *      + - - - - - - - - - - - - - - - - - - + - - - - - 
     * true | event | state A | state B | state C | event | state A ...
     * 
     * */
    
    private int startStepVar(int step){
        int stepLength = ats.getUnionAlphabet().size();
        for(Automaton a1: ats)
            stepLength += a1.nbrOfStates();
        
        int start = 1/*never use 0*/ + 1/*true*/ + step*stepLength;
        return start;
    }

    private Convert.Expr tranEq(int step, LabeledEvent e){
        int start = startStepVar(step);
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);        
        for(LabeledEvent e1: ats.getUnionAlphabet()){
            Convert.Expr n = Convert.Lit(start);
            Convert.add(res, e1.equals(e.getLabel()) ? n : Convert.Not(n));
            start++;
        }            
        return res;
    }

    private Convert.Expr stateEq(int step, Automaton a, State s){
        int start = startStepVar(step);
        inc: for(Automaton a1: ats){
            if(a1.equals(a))
                break inc;
            start += a1.nbrOfStates();
        }
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);        
        for(State s1: a){
            Convert.Expr n = Convert.Lit(start);
            Convert.add(res, s1.equalState(s) ? n : Convert.Not(n));
            start++;
        }            
        return res;
    }

    private Convert.Expr keepState(int step, Automaton a){
        int startCur = startStepVar(step);
        int startNext = startStepVar(step+1);
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);        
        for(State s: a){
            Convert.add(res, Convert.Eq(
                Convert.Lit(startCur), 
                Convert.Lit(startNext)
            ));
            startCur++;
            startNext++;
        }
        return res;
    }
    
    public Convert.Expr fullExpr(){
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);
        Convert.add(res, Convert.Lit(trueVariable));
        Convert.add(res, goalMarkingEvent());
        Convert.add(res, init());
        Convert.add(res, tran());
        Convert.add(res, stay());
        return res;
    }
    
    public static void main(String [] args) throws Exception{       
        int          steps = Integer.parseInt(args[0]);
        Project      ats   = (new ProjectBuildFromXML()).build(System.in);
        MSR.modifyCV(ats);
        
        MSRExplStates msr  = new MSRExplStates(ats, steps);
        Convert.Expr e     = msr.fullExpr();
        Convert      conv  = new Convert(msr.trueVariable,
                msr.startStepVar(steps+2));
        Convert.Clauses cs = conv.convert(e);
        String out         = Convert.toDimacsCnfString(cs, conv.varCounter);
        
        System.out.println(out);       
    }
    

}
