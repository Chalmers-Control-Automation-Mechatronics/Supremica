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
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;

/**
 *
 * @author shoaei
 */
public class AutomatonObserver {
    
    private final Logger logger = LoggerFactory.createLogger(IDE.class);    
    private final ExtendedAutomaton exAutomaton;
    private ActionTimer observerTimer;
    
    /**
     * The constructor method of the AutomatonObserver class.
     * @param exAutomaton The input automaton for analyze
     */    
    public AutomatonObserver(ExtendedAutomaton exAutomaton){
        this.exAutomaton = exAutomaton;
    }
    
    /**
     * Method to find the coset of observation-equivalent states for the input <B>state</B>. The set of states in the coset are connected
     * to the <B>state</B> via unobservable transitions. Note that by default all transitions are observable.
     * @param state An state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states or <CODE>null</CODE> if <B>state</B> is not a state in the given automaton
     */
    public HashSet<NodeProxy> findEquivalentStates(NodeProxy state, boolean downstream){
        if(!exAutomaton.getNodes().contains(state))
            return null;
        
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();
        Stack<EdgeProxy> downTrans = new Stack<EdgeProxy>();
        
        eqStates.add(state);
        stk.push(state);
        
        observerTimer = new ActionTimer();
        observerTimer.start();
        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            for(EdgeProxy e : exAutomaton.getTransitions()){
                if(downstream && e.getSource().getName().equals(currstate.getName()))
                    downTrans.push(e);

                if(!downstream && e.getTarget().getName().equals(currstate.getName()))
                    upTrans.push(e);
            }
            
            while(!upTrans.isEmpty() || !downTrans.isEmpty()){
                EdgeProxy tran = (downstream)?downTrans.pop():upTrans.pop();
                List<Proxy> eventList = tran.getLabelBlock().getEventList();
                for(Proxy event : eventList){
                    final String eventName = ((SimpleIdentifierSubject)event).getName();
                    for(EventDeclProxy ev : exAutomaton.getAlphabet()){
                        if(ev.getName().equals(eventName)){
                            if(!ev.isObservable()){
                                NodeProxy nxState = (downstream)?tran.getTarget():tran.getSource();
                                boolean result = eqStates.add(nxState);
                                if(result) stk.push(nxState);
                            }
                            break;
                        }
                    }
                }
            }
        }
        observerTimer.stop();
        return eqStates;
    }
    
   /**
     * Method to find the coset of observation-equivalent states for the input set of <B>states</B>. The set of states in the coset are connected
     * to each state in the set <B>states</B> via unobservable transitions. Note that by default all transitions are observable.
     * @param states Set of state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states
     */    
    public HashSet<NodeProxy> findEquivalentStates(HashSet<NodeProxy> states, boolean downstream){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        for(NodeProxy state : states)
            eqStates.addAll(findEquivalentStates(state, downstream));
        
        return eqStates;
    }
    /**
     * Method to find the equivalent states up and downstream.
     * @param state An state in the automaton
     * @return Set of the equivalent states
     */
    public HashSet<NodeProxy> findEquivalentStates(NodeProxy state){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        eqStates.addAll(findEquivalentStates(state, true));
        eqStates.addAll(findEquivalentStates(state, false));
        return eqStates;
    }
    
    public HashSet<NodeProxy> findImdPreImg(NodeProxy state, EventProxy event){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();

        eqStates.add(state);
        stk.push(state);

        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            for(EdgeProxy e : exAutomaton.getTransitions())
                if(e.getTarget().getName().equals(currstate.getName()))
                    upTrans.push(e);

            while(!upTrans.isEmpty()){
                EdgeProxy tran = upTrans.pop();
                List<Proxy> eventList = tran.getLabelBlock().getEventList();
                for(Proxy evnt : eventList){
                    final String eventName = ((SimpleIdentifierSubject)evnt).getName();
                    for(EventDeclProxy ev : exAutomaton.getAlphabet()){
                        if(ev.getName().equals(eventName)){
                            NodeProxy nxState = tran.getSource();
                            if(!ev.isObservable()){
                                boolean result = eqStates.add(nxState);
                                if(result) stk.push(nxState);
                            }
                            else if(ev.getName().equals(event.getName())){
                                imdImg.add(nxState);
                            }
                        }
                    }
                }
            }
        }

        return imdImg;
    }

    public HashSet<NodeProxy> findImdPreImg(HashSet<NodeProxy> states, EventProxy event){
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        for(NodeProxy state : states)
            imdImg.addAll(findImdPreImg(state, event));

        return imdImg;
    }
            
    public void getCongruence(){
        Partition cong = new Partition();
        Partition splitter,p1,p2,ps;
        
        splitter = new Partition();
        ps = new Partition();
        splitter.setNext(ps);
        
        for(NodeProxy node : exAutomaton.getNodes()){
            splitter.addState(node);
            cong.addState(node);
        }
        
        while((ps = splitter.getNext()) != null){
            HashSet<NodeProxy> b = ps.getCoset();
            splitter.setNext(ps.getNext());
            for(EventDeclProxy e : exAutomaton.getAlphabet()){
                HashSet<NodeProxy> phiB = phiTao(b,e);
            }
        }
        
    }

    private HashSet<NodeProxy> phiTao(HashSet<NodeProxy> b, EventDeclProxy e) {
        HashSet<NodeProxy> phiB = new HashSet<NodeProxy>();
        
        return phiB;
    }
    
    
    public ActionTimer getObserverTimer(){
        return observerTimer;
    }
    
    class Partition{
        private HashSet<NodeProxy> coset;
        private Partition next;
        public Partition(){
            this.coset = new HashSet<NodeProxy>();
            this.next = null;
        }
        
        public HashSet<NodeProxy> getCoset(){
            return coset;
        }
        
        public void setCoset(HashSet<NodeProxy> coset){
            this.coset = coset;
        }
        
        public void clearCoset(){
            this.coset.clear();
        }
        
        public boolean addState(NodeProxy state){
            return coset.add(state);
        }
        
        public boolean removeState(NodeProxy state){
            return coset.remove(state);
        }
        
        public void setNext(Partition next){
            this.next=next;
        }
        
        public Partition getNext(){
            return this.next;
        }
    }
    
}
