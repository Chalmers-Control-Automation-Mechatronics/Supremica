/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;

/**
 *
 * @author voronov
 */
public class ProblemFME {

    private ProblemBasics pb;
    
    /**
     * Lays out complete expression out of basic conjunctions
     * @param pb
     */
    public ProblemFME(ProblemBasics pb){
        this.pb = pb;
    }
    
    /**
     * precondition: automata should have indices already!
     * @param ats
     * @param steps
     * @param markingEvent 
     */
    public void go(Automata ats, int steps, LabeledEvent markingEvent){
        /* init */
        for(Automaton a: ats)
            pb.initialCondition(a);

        /* step-by-step */
        for(int step = 0; step < steps; step++){
            /* for each automaton */
            for(Automaton a: ats){
                /* we check all events of the union alphabet */
                for(LabeledEvent ev: ats.getUnionAlphabet()){
                    if(a.getAlphabet().contains(ev)){
                        /* if the events belong to the alphabet of this automaton, 
                         * we add the transition conditions for this event */
                        pb.transition(step, a, ev);
                    } else {
                        /* if event don't belong to the alphabet, 
                         * we say that if this event will happen, 
                         * then this automaton should keep its state */
                        pb.stay(step, a, ev);
                    }
                }
            }
        }
    
        /* goal */
        pb.fireMarkingEvent(markingEvent, steps);
        
        /* one value each step */
        for(int step = 0; step < steps; step++){
            pb.ensureOneEventThisStep(step);
            pb.ensureOneStateThisStep(step);
        }
        pb.ensureOneStateThisStep(steps);
        
    }
}
