/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.List;
import net.sourceforge.waters.model.des.EventProxy;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;

/**
 * 
 * @author shoaei
 * 
 */
public class AutomataTP {
    
    private final ExtendedAutomata theAutomata;
    private final List<EventProxy> localEvents;
    
    public AutomataTP(ExtendedAutomata exAutomata, List<EventProxy> localEvents){
        this.theAutomata = exAutomata;
        this.localEvents = localEvents;
    }
     
    public void project(){
    }
       
}
