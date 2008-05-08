/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public class ModifyMSRToFME {
    
    public static final String MARKED = "marking";
    public static final LabeledEvent marked = new LabeledEvent(MARKED);

    /**
     * Add self loop with marking event to every marked state.
     * If no state is marked, then it means that all states are marked
     * @param ats
     */
    public static void modify(Automata ats){                
        
        /* add marked event to all automata */
        for(Automaton a: ats)
            a.getAlphabet().addEvent(marked);
            
        /* add selfloops with marking event to all marked states */        
        for(Automaton a: ats)
            if(hasNoAccepting(a))
                for(State s: a)
                    a.addArc(new Arc(s,s,marked));
            else
                for(State s: a)
                    if(s.isAccepting())
                        a.addArc(new Arc(s,s,marked));                
    }

    private static boolean hasNoAccepting(Automaton a){
        for(State s: a)
            if(s.isAccepting())
                return false;
        return true;
    }
}
