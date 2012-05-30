/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.plain.module.EventDeclElement;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
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
    private HashSet<EventDeclProxy> observableEvents;
    private HashSet<EventDeclProxy> unobservableEvents;
    private int nrQCIteration;
    
    /**
     * The constructor method of the AutomatonObserver class.
     * @param exAutomaton The input automaton for analyze
     */    
    public AutomatonObserver(ExtendedAutomaton exAutomaton){
        this.exAutomaton = exAutomaton;
        observableEvents = new HashSet<EventDeclProxy>();
        unobservableEvents = new HashSet<EventDeclProxy>();
        for(EventDeclProxy event : this.exAutomaton.getAlphabet()){
            if(event.isObservable())
                observableEvents.add(event);
            else
                unobservableEvents.add(event);
        }
        observerTimer = new ActionTimer();
    }
    
    public HashSet<EventDeclProxy> getObservableEvents(){
        return observableEvents;
    }

    public HashSet<EventDeclProxy> getUnobservableEvents(){
        return unobservableEvents;
    }
    
    /**
     * Method to find the coset of observation-equivalent states for the input <B>state</B>. The set of states in the coset are connected
     * to the <B>state</B> via unobservable transitions. Note that by default all transitions are observable.
     * @param state An state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states or <CODE>null</CODE> if <B>state</B> is not a state in the given automaton
     */
    private HashSet<NodeProxy> findEquivalentStates(NodeProxy state, boolean downstream){
        if(!exAutomaton.getNodes().contains(state))
            return null;
        
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();
        Stack<EdgeProxy> downTrans = new Stack<EdgeProxy>();
        
        eqStates.add(state);
        stk.push(state);
        
        while(!stk.empty()){
            NodeProxy currstate = stk.pop();

            if(downstream)
                downTrans.addAll(getDownStreamEdges(currstate));
            else
                upTrans.addAll(getUpStreamEdges(currstate));
            
            while(!upTrans.isEmpty() || !downTrans.isEmpty()){
                EdgeProxy tran = (downstream)?downTrans.pop():upTrans.pop();
                List<Proxy> eventList = tran.getLabelBlock().getEventList();
                for(Proxy event : eventList){
                    final String eventName = ((SimpleIdentifierSubject)event).getName();
                    for(EventDeclProxy ev : unobservableEvents){
                        if(ev.getName().equals(eventName)){
                            NodeProxy nxState = (downstream)?tran.getTarget():tran.getSource();
                            boolean result = eqStates.add(nxState);
                            if(result) stk.push(nxState);
                            break;
                        }
                    }
                }
            }
        }
        return eqStates;
    }
    
   /**
     * Method to find the coset of observation-equivalent states for the input set of <B>states</B>. The set of states in the coset are connected
     * to each state in the set <B>states</B> via unobservable transitions. Note that by default all transitions are observable.
     * @param states Set of state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states
     */    
    private HashSet<NodeProxy> findEquivalentStates(HashSet<NodeProxy> states, boolean downstream){
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
    private HashSet<NodeProxy> findEquivalentStates(NodeProxy state){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        eqStates.addAll(findEquivalentStates(state, true));
        eqStates.addAll(findEquivalentStates(state, false));
        return eqStates;
    }
    
    private EventDeclProxy getEvent(String event){
        EventDeclProxy e = null;
        for(EventDeclProxy ev : exAutomaton.getAlphabet()){
            if(ev.getName().equals(event)){
                e = ev;
                break;
            }
        }
        return e;
    }
    
    private HashSet<EdgeProxy> getUpStreamEdges(NodeProxy st){
        HashSet<EdgeProxy> upTrans = new HashSet<EdgeProxy>();
            for(EdgeProxy e : exAutomaton.getTransitions())
                if(e.getTarget().getName().equals(st.getName()))
                    upTrans.add(e);
        return upTrans;
    }

    private HashSet<EdgeProxy> getDownStreamEdges(NodeProxy st){
        HashSet<EdgeProxy> downTrans = new HashSet<EdgeProxy>();
            for(EdgeProxy e : exAutomaton.getTransitions())
                if(e.getSource().getName().equals(st.getName()))
                    downTrans.add(e);
        return downTrans;
    }
    
    private HashSet<NodeProxy> findImdPreImg(NodeProxy state, EventDeclProxy event){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();

        eqStates.add(state);
        stk.push(state);

        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            upTrans.addAll(getUpStreamEdges(currstate));

            while(!upTrans.isEmpty()){
                EdgeProxy tran = upTrans.pop();
                for(Proxy evnt : tran.getLabelBlock().getEventList()){
                    EventDeclProxy e = getEvent(((SimpleIdentifierSubject)evnt).getName());
                    NodeProxy nxState = tran.getSource();
                    if(unobservableEvents.contains(e)){
                        boolean result = eqStates.add(nxState);
                        if(result) stk.push(nxState);
                    }
                    else if(e.getName().equals(event.getName())){
                        imdImg.add(nxState);
                    }
                    break;
                }
            }
        }
        return imdImg;
    }

    private HashSet<NodeProxy> findImdPreImg(HashSet<NodeProxy> states, EventDeclProxy event){
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        for(NodeProxy state : states)
            imdImg.addAll(findImdPreImg(state, event));

        return imdImg;
    }
            
    private HashSet<Partition> getQC(){
        Stack<Partition> W = new Stack<Partition>(); // Set W in WONG paper 
        HashSet<Partition> R = new HashSet<Partition>(); // Set Rho in WONG paper 
        HashSet<NodeProxy> Q,phiB,inter,diff;
        Partition B,X;
        Q = new HashSet<NodeProxy>(exAutomaton.getNodes());
        W.push(new Partition(Q));
        R.add(new Partition(Q));
        EventDeclSubject m = new EventDeclSubject(new SimpleIdentifierSubject("TAOm"), EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, null, null, null);
        observableEvents.add(m);
        int i = 0;
        while(!W.isEmpty()){
            i++;
            B = W.pop();
            for(EventDeclProxy e : observableEvents){
                HashSet<Partition> I = new HashSet<Partition>();
                HashSet<Partition> I12 = new HashSet<Partition>();
                
                if(e.getName().equals("TAOm"))
                    phiB = phiTAOm(B.getCoset());
                else
                    phiB = phiTao(B.getCoset(),e);
                
                if(phiB.isEmpty()) continue;
                
                for (Iterator<Partition> it = R.iterator(); it.hasNext();) {
                    X = it.next();
                    inter = setIntersection(X.getCoset(), phiB);
                    if (inter.isEmpty()) continue;
                    diff = setDifference(X.getCoset(), phiB);
                    if (diff.isEmpty()) continue;
                    I.add(X);
                    I12.add(new Partition(inter));
                    I12.add(new Partition(diff));
                }
                for(Partition p : I){
                    R.remove(p);
                    W.remove(p);
                }
                for(Partition p : I12){
                    R.add(p);
                    W.push(p);
                }
            }
        }
        observableEvents.remove(m);
        nrQCIteration = i;
        return R;
    }
    
    private HashSet<NodeProxy> phiTao(HashSet<NodeProxy> b, EventDeclProxy e) {
        HashSet<NodeProxy> imdImg = findImdPreImg(b, e);
        HashSet<NodeProxy> phiB = findEquivalentStates(imdImg, false);
        return phiB;
    }

    private HashSet<NodeProxy> phiTAOm(HashSet<NodeProxy> b) {
        HashSet<NodeProxy> phiB = new HashSet<NodeProxy>();
        Set<NodeProxy> m = exAutomaton.getMarkedLocations();
        for(NodeProxy st : b)
            if(m.contains(st))
                phiB=findEquivalentStates(st, false);
        
        return phiB;
    }    
    
    public ActionTimer getObserverTimer(){
        return observerTimer;
    }
    
    public int getNrIterations(){
        return nrQCIteration;
    }
    
    private HashSet<NodeProxy> setIntersection(HashSet<NodeProxy> x, HashSet<NodeProxy> y){
        HashSet<NodeProxy> result = new HashSet<NodeProxy>(x);
        result.retainAll(y);
        return result;
    }
    
    private HashSet<NodeProxy> setDifference(HashSet<NodeProxy> x, HashSet<NodeProxy> y){
        HashSet<NodeProxy> result = new HashSet<NodeProxy>();
        for (NodeProxy n:x)
            if(!y.contains(n))
                result.add(n);
        return result;
    }    
    
    public HashSet<HashSet<NodeProxy>> getCongruence(){
        HashSet<HashSet<NodeProxy>> qc = new HashSet<HashSet<NodeProxy>>();
        observerTimer.reset();
        observerTimer.start();
        HashSet<Partition> ps = getQC();
        observerTimer.stop();
        for(Partition p : ps)
            qc.add(new HashSet<NodeProxy>(p.getCoset()));
                
        return qc;
    }
    
    private void getQuotiont(HashSet<Partition> ps){
        for(Partition p : ps){
            
        }
    }
    
    class Partition{
        private HashSet<NodeProxy> coset;

        public Partition(){
            this.coset = new HashSet<NodeProxy>();
        }

        public Partition(HashSet<NodeProxy> coset){
            this.coset = coset;
        }
        
        public HashSet<NodeProxy> getCoset(){
            return coset;
        }
        
        public void setCoset(HashSet<NodeProxy> coset){
            this.coset = coset;
        }
        
        public void clear(){
            this.coset.clear();
        }
        
        public boolean addState(NodeProxy state){
            return coset.add(state);
        }
        
        public boolean addState(HashSet<NodeProxy> states){
            return coset.addAll(states);
        }

        public boolean removeState(NodeProxy state){
            return coset.remove(state);
        }
        
    }
    
}
