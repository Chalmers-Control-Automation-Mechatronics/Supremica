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
 */

public class AutomatonTP {
    private final ExtendedAutomaton exAutomaton;
    
    public AutomatonTP(ExtendedAutomaton exAutomaton){
        this.exAutomaton = exAutomaton;
    }
    
    public ExtendedAutomaton project(List<EventProxy> localEvents){
        ExtendedAutomaton result = null;
        return result;
    }
}
