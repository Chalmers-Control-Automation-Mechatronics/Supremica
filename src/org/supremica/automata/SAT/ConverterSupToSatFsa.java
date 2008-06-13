/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sourceforge.fsa2sat.fsa.AutomatonM;
import net.sourceforge.fsa2sat.fsa.AutomatonMArrayList;
import net.sourceforge.fsa2sat.fsa.Event;
import net.sourceforge.fsa2sat.fsa.EventImpl;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


/**
 *
 * @author voronov
 */
public class ConverterSupToSatFsa {

    public net.sourceforge.fsa2sat.fsa.Automata convert(Automata ats){
        
        net.sourceforge.fsa2sat.fsa.AutomataImpl res = 
                new net.sourceforge.fsa2sat.fsa.AutomataImpl();
        
        for(Automaton a: ats){
            
            AutomatonM am = new AutomatonMArrayList(a.getName(), getType(a));
            
            Map<LabeledEvent, Event>                      eventsMap = 
                    new HashMap<LabeledEvent, Event>();
            Map<State, net.sourceforge.fsa2sat.fsa.State> statesMap = 
                    new HashMap<State, net.sourceforge.fsa2sat.fsa.State>();
            
            /* Events */
            for(LabeledEvent e: a.getAlphabet()){
                Event ev = new EventImpl(e.getLabel(), e.isControllable());
                eventsMap.put(e, ev);
                am.addEvent(ev);                
            }
            
            /* States */
            for(State s: a){
                net.sourceforge.fsa2sat.fsa.State s1 
                        = new net.sourceforge.fsa2sat.fsa.StateImpl(
                            s.getName(), 
                            s.isAccepting());
            
                statesMap.put(s, s1);
                am.addState( s1, s.isInitial());
            }
            
            /* Transitions */
            for(Iterator<Arc> i = a.arcIterator(); i.hasNext();){
                Arc arc = i.next();
                am.addArc
                ( statesMap.get(arc.getSource())
                , statesMap.get(arc.getTarget())
                , eventsMap.get(arc.getEvent())
                );
            }
            
            res.add(am);
        }
        
        return res;
    }    
    
    private net.sourceforge.fsa2sat.fsa.Automaton.Type getType(Automaton a){
        if(a.isPlant())
            return net.sourceforge.fsa2sat.fsa.Automaton.Type.PLANT;
        if(a.isSupervisor())
            return net.sourceforge.fsa2sat.fsa.Automaton.Type.SUPERVISOR;
        
        return net.sourceforge.fsa2sat.fsa.Automaton.Type.SPECIFICATION;
    }
}
