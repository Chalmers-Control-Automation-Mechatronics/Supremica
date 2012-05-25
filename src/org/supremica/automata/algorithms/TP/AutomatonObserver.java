/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import org.supremica.automata.ExtendedAutomaton;

/**
 *
 * @author shoaei
 */
public class AutomatonObserver {
    private final ExtendedAutomaton exAutomaton;
        
    public AutomatonObserver(ExtendedAutomaton exAutomaton){
        this.exAutomaton = exAutomaton;
    }
    /**
     * Method to find the observation-equivalent states for the input state. These states are connected
     * to the state via unobservable transitions.
     * @param state
     * @param direction
     * @return 
     */
    
    public HashSet<NodeProxy> EquivalentStates(NodeProxy state, boolean downstream){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();
        Stack<EdgeProxy> downTrans = new Stack<EdgeProxy>();
        
        eqStates.add(state);
        stk.push(state);
        
        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            for(EdgeProxy e : exAutomaton.getTransitions()){
                if(downstream && e.getSource().getName().equals(currstate.getName()))
                    downTrans.push(e);

                if(!downstream && e.getTarget().getName().equals(currstate.getName()))
                    upTrans.push(e);
            }
            
            while(!upTrans.isEmpty() || !downTrans.isEmpty()){
                EdgeProxy tran;
                if(downstream)
                    tran = downTrans.pop(); 
                else
                    tran=upTrans.pop();
                
                List<Proxy> eventList = tran.getLabelBlock().getEventList();
                for(Proxy event : eventList){
                    if(!((EventProxy)event).isObservable()){
                        NodeProxy nxState = (downstream)?tran.getTarget():tran.getSource();
                        boolean result = eqStates.add(nxState);
                        if(result) stk.push(nxState);
                    }
                }
            }
        }
        return eqStates;
    }
    
    public HashSet<NodeProxy> EquivalentStates(NodeProxy state){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        eqStates.addAll(EquivalentStates(state, true));
        eqStates.addAll(EquivalentStates(state, false));
        return eqStates;
    }
}
