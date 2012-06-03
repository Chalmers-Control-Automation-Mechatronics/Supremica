/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import org.supremica.automata.ExtendedAutomata;
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
    private final ExtendedAutomata exAutomata;
    private final ActionTimer observerTimer;
    private HashSet<EventDeclProxy> sharedEvents;
    private HashSet<EventDeclProxy> localEvents;
    private int nrQCIteration;
    
    /**
     * The constructor method of the AutomatonObserver class.
     * @param exAutomaton The input automaton for analyze
     */    
    public AutomatonObserver(ExtendedAutomata exAutomata){
        this.exAutomata = exAutomata;
        this.observerTimer = new ActionTimer();
        this.nrQCIteration = 0;
        this.localEvents = getLocalEventSet(this.exAutomata);
        this.sharedEvents = eventDifference(new HashSet<EventDeclProxy>(this.exAutomata.getUnionAlphabet()), localEvents);
    }
    
    private HashSet<EventDeclProxy> getObservableEvents(ExtendedAutomata exAutomata){
        HashSet<EventDeclProxy> obs = new HashSet<EventDeclProxy>();
         for(EventDeclProxy event : exAutomata.getUnionAlphabet()){
             if(event.isObservable())
                 obs.add(event);
         }
         return obs;
    }
    
    public void compute(){
        String s = "";
        s += "\n All shared event: " + getAllSharedEvents() 
                + "\n All local events: " + getAllLocalEvents()
                + "\n +++++++++++++++++";
        
        observerTimer.reset();
        observerTimer.start();
        HashSet<ExtendedAutomaton> EFAs = new HashSet<ExtendedAutomaton>();
        for(ExtendedAutomaton efa:exAutomata){
            s += ("\n EFA: " + efa.getName()
                    + "\n Shared event: " + getSharedEvents(efa)
                    + "\n LocalEvents events: " + getLocalEvents(efa));
            HashSet<Partition> qc = getQC(efa);
            for(Partition p : qc){
                s += "\n Coset members: {";
                for (Iterator<NodeProxy> it = p.getCoset().iterator(); it.hasNext();) {
                    NodeProxy st = it.next();
                    if(it.hasNext())
                        s += st.getName() + ", ";
                    else
                        s += st.getName() + "}";
                }
            }
            s += "\n Observer calculated in [" 
                    + getNrIterations() 
                    + "] iterations. \n ----------------------------";
            
            ExtendedAutomaton quo = getQuotient(efa, qc);
            EFAs.add(quo);
        }
        
        for(ExtendedAutomaton efa:EFAs)
            exAutomata.addAutomaton(efa);
        
        observerTimer.stop();
        s += "\n Observer calculated in [" 
                + getObserverTimer() 
                + "].";
        logger.info(s);
    }
    
    private HashSet<EventDeclProxy> getLocalEventSet(ExtendedAutomata exAutomata){        
        HashSet<EventDeclProxy> locEvents = new HashSet<EventDeclProxy>();
        CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        for(ExtendedAutomaton efa1:exAutomata){            
            HashSet<EventDeclProxy> sigma1 = new HashSet<EventDeclProxy>(efa1.getAlphabet());
            HashSet<EventDeclProxy> sigma2 = new HashSet<EventDeclProxy>();
            HashSet<VariableComponentProxy> guardVar = new HashSet<VariableComponentProxy>();
            
            for(EventDeclProxy ev:efa1.getAlphabet())
                if(efa1.getGuardVariables(ev)!=null)
                    guardVar.addAll(efa1.getGuardVariables(ev));
            
            for(ExtendedAutomaton efa2:exAutomata){
                if(!efa2.getName().equals(efa1.getName())){
                    for(EventDeclProxy ev:efa2.getAlphabet()){
                        sigma2.add(ev);
                        if(efa2.getGuardVariables(ev)!=null)
                            guardVar.addAll(efa2.getGuardVariables(ev));
                    }
                }
            }
            
            HashSet<EventDeclProxy> uniqueEvents1 = (sigma2.isEmpty())?sigma1:eventDifference(sigma1, sigma2);
            outerloop:
            for(EventDeclProxy e1:uniqueEvents1){
                if(efa1.getGuardVariables(e1) != null && !efa1.getGuardVariables(e1).isEmpty()) 
                    continue outerloop;
                
                Set<VariableComponentProxy> actionVar1 = efa1.getActionVariables(e1);
                if(actionVar1 != null && !actionVar1.isEmpty()){
                    for(VariableComponentProxy a1:actionVar1)
                        if(guardVar.contains(a1)) 
                            continue outerloop;
                    
                    for(EdgeProxy t1:efa1.getTransitions())
                        for(Proxy te1:t1.getLabelBlock().getEventList())
                            if(((SimpleIdentifierSubject)te1).getName().equals(e1.getName()) && t1.getGuardActionBlock() != null)
                                for(BinaryExpressionProxy expr:t1.getGuardActionBlock().getActions())
                                    if(!efa1.extractVariablesFromExpr(expr.getRight()).isEmpty() 
                                            || expr.getOperator().equals(cot.getIncrementOperator()) 
                                            || expr.getOperator().equals(cot.getDecrementOperator())) 
                                        continue outerloop;
                }
                locEvents.add(e1);
            }
        }
        // Setting the observability of all events
        setObservability(locEvents);
        return locEvents;
    }
    
    private void setObservability(HashSet<EventDeclProxy> locEvents){
        for(EventDeclProxy e:exAutomata.getUnionAlphabet())
            if(locEvents.contains(e))
                ((EventDeclSubject)e).setObservable(false);
            else
                ((EventDeclSubject)e).setObservable(true);
    }
    
    public HashSet<EventDeclProxy> getAllSharedEvents(){
        return sharedEvents;
    }

    public HashSet<EventDeclProxy> getAllLocalEvents(){
        return localEvents;
    }

    public HashSet<EventDeclProxy> getSharedEvents(ExtendedAutomaton efa){
        return eventIntersection(sharedEvents, new HashSet<EventDeclProxy>(efa.getAlphabet()));
    }

    public HashSet<EventDeclProxy> getLocalEvents(ExtendedAutomaton efa){
        return eventIntersection(localEvents, new HashSet<EventDeclProxy>(efa.getAlphabet()));
    }
    
    /**
     * Method to find the coset of observation-equivalent states for the input <B>state</B>. The set of states in the coset are connected
     * to the <B>state</B> via unobservable transitions. Note that by default all transitions are observable.
     * @param state An state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states or <CODE>null</CODE> if <B>state</B> is not a state in the given automaton
     */
    private HashSet<NodeProxy> findEquivalentStates(ExtendedAutomaton efa, NodeProxy state, boolean downstream){
        if(!efa.getNodes().contains(state))
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
                downTrans.addAll(getDownStreamEdges(efa,currstate));
            else
                upTrans.addAll(getUpStreamEdges(efa,currstate));
            
            while(!upTrans.isEmpty() || !downTrans.isEmpty()){
                EdgeProxy tran = (downstream)?downTrans.pop():upTrans.pop();
                List<Proxy> eventList = tran.getLabelBlock().getEventList();
                for(Proxy event : eventList){
                    final String eventName = ((SimpleIdentifierSubject)event).getName();
                    for(EventDeclProxy ev : localEvents){
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
    private HashSet<NodeProxy> findEquivalentStates(ExtendedAutomaton efa, HashSet<NodeProxy> states, boolean downstream){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        for(NodeProxy state : states)
            eqStates.addAll(findEquivalentStates(efa, state, downstream));
        
        return eqStates;
    }
    /**
     * Method to find the equivalent states up and downstream.
     * @param state An state in the automaton
     * @return Set of the equivalent states
     */
    private HashSet<NodeProxy> findEquivalentStates(ExtendedAutomaton efa, NodeProxy state){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        eqStates.addAll(findEquivalentStates(efa, state, true));
        eqStates.addAll(findEquivalentStates(efa, state, false));
        return eqStates;
    }
    
    private EventDeclProxy getEvent(ExtendedAutomaton efa, String event){
        EventDeclProxy e = null;
        for(EventDeclProxy ev : efa.getAlphabet()){
            if(ev.getName().equals(event)){
                e = ev;
                break;
            }
        }
        return e;
    }
    
    private HashSet<EdgeProxy> getUpStreamEdges(ExtendedAutomaton efa, NodeProxy st){
        HashSet<EdgeProxy> upTrans = new HashSet<EdgeProxy>();
            for(EdgeProxy e : efa.getTransitions())
                if(e.getTarget().getName().equals(st.getName()))
                    upTrans.add(e);
        return upTrans;
    }

    private HashSet<EdgeProxy> getDownStreamEdges(ExtendedAutomaton efa, NodeProxy st){
        HashSet<EdgeProxy> downTrans = new HashSet<EdgeProxy>();
            for(EdgeProxy e : efa.getTransitions())
                if(e.getSource().getName().equals(st.getName()))
                    downTrans.add(e);
        return downTrans;
    }
    
    private HashSet<NodeProxy> findImdPreImg(ExtendedAutomaton efa, NodeProxy state, EventDeclProxy event){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();

        eqStates.add(state);
        stk.push(state);

        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            upTrans.addAll(getUpStreamEdges(efa, currstate));

            while(!upTrans.isEmpty()){
                EdgeProxy tran = upTrans.pop();
                for(Proxy evnt : tran.getLabelBlock().getEventList()){
                    EventDeclProxy e = getEvent(efa, ((SimpleIdentifierSubject)evnt).getName());
                    NodeProxy nxState = tran.getSource();
                    if(localEvents.contains(e)){
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

    private HashSet<NodeProxy> findImdPreImg(ExtendedAutomaton efa, HashSet<NodeProxy> states, EventDeclProxy event){
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        for(NodeProxy state : states)
            imdImg.addAll(findImdPreImg(efa, state, event));

        return imdImg;
    }
            
    private HashSet<Partition> getQC(ExtendedAutomaton efa){
        Stack<Partition> W = new Stack<Partition>(); // Set W (splitter) in WONG paper 
        HashSet<Partition> R = new HashSet<Partition>(); // Set Rho in WONG paper 
        HashSet<NodeProxy> Q,phiB,inter,diff; // Sets of initial partition, phiB, intersection, and difference
        Partition B,X; // B: temporary partition X: A partition in Rho
        Q = new HashSet<NodeProxy>(efa.getNodes());
        W.push(new Partition(Q));
        R.add(new Partition(Q));
        // New event Tao_m to handle marked states
        EventDeclSubject m = new EventDeclSubject(new SimpleIdentifierSubject("TAOm"), EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, null, null, null);
        sharedEvents.add(m);
        efa.getAlphabet().add(m);
        int i = 0;
        while(!W.isEmpty()){
            B = W.pop();
            i++;
            for(EventDeclProxy e : efa.getAlphabet()){
                if(!sharedEvents.contains(e)) continue;
                HashSet<Partition> I = new HashSet<Partition>();
                HashSet<Partition> I12 = new HashSet<Partition>();
                
                if(e.getName().equals("TAOm"))
                    phiB = phiTAOm(efa, B.getCoset());
                else
                    phiB = phiTao(efa, B.getCoset(),e);
                
                if(phiB.isEmpty()) continue;
                
                for (Iterator<Partition> it = R.iterator(); it.hasNext();) {
                    X = it.next();
                    inter = stateIntersection(X.getCoset(), phiB);
                    if (inter.isEmpty()) continue;
                    diff = stateDifference(X.getCoset(), phiB);
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
        sharedEvents.remove(m);
        efa.getAlphabet().remove(m);
        nrQCIteration = i;
        return R;
    }
    
    private HashSet<NodeProxy> phiTao(ExtendedAutomaton efa, HashSet<NodeProxy> b, EventDeclProxy e) {
        HashSet<NodeProxy> imdImg = findImdPreImg(efa, b, e);
        HashSet<NodeProxy> phiB = findEquivalentStates(efa, imdImg, false);
        return phiB;
    }

    private HashSet<NodeProxy> phiTAOm(ExtendedAutomaton efa, HashSet<NodeProxy> b) {
        HashSet<NodeProxy> phiB = new HashSet<NodeProxy>();
        Set<NodeProxy> m = efa.getMarkedLocations();
        for(NodeProxy st : b)
            if(m.contains(st))
                phiB=findEquivalentStates(efa, st, false);
        
        return phiB;
    }    
    
    private ExtendedAutomaton getQuotient(ExtendedAutomaton efa, HashSet<Partition> ps){
        ExtendedAutomaton quotient = new ExtendedAutomaton(efa.getName() + "_QUO", exAutomata, false);
        
        for(Partition p:ps){
            HashSet<NodeProxy> coset = p.getCoset();
            SimpleNodeSubject st = quotient.addState(getStateName(coset), hasMarkedState(efa, coset), hasInitialState(efa, coset), hasForbidden(efa, coset));
            p.setState(st);
        }
        
        for(EdgeProxy tran:efa.getTransitions()){
            NodeProxy targetState = getCorrespondingState(tran.getTarget(), ps);
            NodeProxy sourceState = getCorrespondingState(tran.getSource(), ps);
            for(Proxy event:tran.getLabelBlock().getEventList()){
                EventDeclProxy e = exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
                String ename = e.getName() + ";";
                String guard = "";
                String action = "";
                try {guard = tran.getGuardActionBlock().getGuards().get(0).toString();} catch (Exception exp) {}
                try {action = tran.getGuardActionBlock().getActions().get(0).toString();} catch (Exception exp) {}
                if(!hasTransition(quotient, sourceState, targetState, e))
                if(sharedEvents.contains(e)){
                    quotient.addTransition(
                            sourceState.getName(), 
                            targetState.getName(), 
                            ename, 
                            guard, 
                            action);
                } else if(sourceState!=targetState){
                    quotient.addTransition(
                            sourceState.getName(), 
                            targetState.getName(), 
                            ename, 
                            guard, 
                            action);
                }
            }
        }
        return quotient;
    }

    private boolean extendEvent(ExtendedAutomaton efa, ExtendedAutomaton quotient, HashSet<Partition> ps){
        boolean hasLocalTransitions = false;
        
        for(EventDeclProxy e:quotient.getAlphabet())
            if(localEvents.contains(e))
                hasLocalTransitions = true;
        
        HashSet<EventDeclProxy> B = new HashSet<EventDeclProxy>(quotient.getAlphabet());
        HashSet<NodeProxy> N = getNdQuoStates(quotient);
        
        if(N.isEmpty())
            return hasLocalTransitions;
        
        HashSet<Partition> partitions = new HashSet<Partition>();
        for(Partition p:ps)
            if(N.contains(p.getState()))
                partitions.add(p);
        
        HashSet<EventDeclProxy> H = getHiddenLocalEvents(efa, partitions);
        HashSet<EventDeclProxy> sigma = eventUnion(B, H);
        HashSet<EventDeclProxy> H_B = eventDifference(H, B);
        HashSet<EventDeclProxy> T = new HashSet<EventDeclProxy>(sigma);
        for(EventDeclProxy e : H_B){
            T.remove(e);
            for(NodeProxy y:N)
                if(split(y,T))
                    sigma.remove(e);
        }
        sharedEvents = eventIntersection(sharedEvents, sigma);
        return hasLocalTransitions;
    }

    private boolean split(NodeProxy y, HashSet<EventDeclProxy> T) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private HashSet<EventDeclProxy> getHiddenLocalEvents(ExtendedAutomaton efa, HashSet<Partition> partitions) {
        HashSet<EventDeclProxy> hiddenEvents = new HashSet<EventDeclProxy>();
        for(Partition p:partitions){
            HashSet<NodeProxy> nodes = p.getCoset();
            for(EdgeProxy tran:efa.getTransitions()){
                NodeProxy sourceNode = tran.getSource();
                NodeProxy targetNode = tran.getTarget();
                if(nodes.contains(sourceNode) && nodes.contains(targetNode)){
                    for(Proxy event:tran.getLabelBlock().getEventList()){
                        EventDeclProxy e = exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
                        if(localEvents.contains(e)){
                            hiddenEvents.add(e);
                        }
                    }
                }
            }
        }
        return hiddenEvents;
    }

    private HashSet<NodeProxy> getNdQuoStates(ExtendedAutomaton quotient) {
        HashSet<NodeProxy> ndStates = new HashSet<NodeProxy>();
        outerloop:
        for(NodeProxy st:quotient.getNodes()){
            HashSet<EventDeclProxy> outgoingEvents = new HashSet<EventDeclProxy>();
            for(EdgeProxy tran:quotient.getTransitions()){
                if(tran.getSource().equals(st)){
                    for(Proxy event:tran.getLabelBlock().getEventList()){
                        EventDeclProxy e = exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
                        if(sharedEvents.contains(e)){
                            boolean hasNotThisEvent = outgoingEvents.add(e);
                            if(!hasNotThisEvent){
                                ndStates.add(st);
                                continue outerloop;
                            }
                        }
                    }
                }
            }
        }
        return ndStates;
    }

    private boolean hasTransition(ExtendedAutomaton quotient, NodeProxy sourceState, NodeProxy targetState, EventDeclProxy e) {
        for(EdgeProxy tran:quotient.getTransitions()){
            for(Proxy event : tran.getLabelBlock().getEventList()){
                String name = ((SimpleIdentifierSubject)event).getName();
                if(e.getName().equals(name) && tran.getSource().equals(sourceState) && tran.getTarget().equals(targetState))
                    return true;
            }
        }
        return false;
    }
    
    private NodeProxy getCorrespondingState(NodeProxy source, HashSet<Partition> ps) {
        for(Partition p:ps)
            if(p.getCoset().contains(source))
                return p.getState();
        return null;
    }
    
    private String getStateName(HashSet<NodeProxy> coset) {
        String s = "{";
        for (Iterator<NodeProxy> it = coset.iterator(); it.hasNext();) {
            NodeProxy st = it.next();
            if(it.hasNext())
                s += st.getName() + ",";
            else
                s += st.getName() + "}";
        }
        return s;
    }

    private boolean hasMarkedState(ExtendedAutomaton efa, HashSet<NodeProxy> coset) {
        for(NodeProxy st:coset)
            if(efa.isLocationAccepted(st))
                return true;
        return false;
    }

    private boolean hasInitialState(ExtendedAutomaton efa, HashSet<NodeProxy> coset) {
        for(NodeProxy st:coset)
            if(efa.isLocationInitial(st))
                return true;
        return false;
    }

    private boolean hasForbidden(ExtendedAutomaton efa, HashSet<NodeProxy> coset) {
        for(NodeProxy st:coset)
            if(efa.isLocationForbidden(st))
                return true;
        return false;
    }
    
    public ActionTimer getObserverTimer(){
        return observerTimer;
    }
    
    public int getNrIterations(){
        return nrQCIteration;
    }
    
    private HashSet<NodeProxy> stateIntersection(HashSet<NodeProxy> x, HashSet<NodeProxy> y){
        HashSet<NodeProxy> result = new HashSet<NodeProxy>(x);
        result.retainAll(y);
        return result;
    }
    
    private HashSet<NodeProxy> stateDifference(HashSet<NodeProxy> x, HashSet<NodeProxy> y){
        HashSet<NodeProxy> result = new HashSet<NodeProxy>();
        for (NodeProxy n:x)
            if(!y.contains(n))
                result.add(n);
        return result;
    }    

    private HashSet<EventDeclProxy> eventIntersection(HashSet<EventDeclProxy> x, HashSet<EventDeclProxy> y){
        HashSet<EventDeclProxy> result = new HashSet<EventDeclProxy>(x);
        result.retainAll(y);
        return result;
    }
    
    private HashSet<EventDeclProxy> eventDifference(HashSet<EventDeclProxy> x, HashSet<EventDeclProxy> y){
        HashSet<EventDeclProxy> result = new HashSet<EventDeclProxy>();
        for (EventDeclProxy n:x)
            if(!y.contains(n))
                result.add(n);
        return result;
    }    

    private HashSet<EventDeclProxy> eventUnion(HashSet<EventDeclProxy> x, HashSet<EventDeclProxy> y){
        HashSet<EventDeclProxy> result = new HashSet<EventDeclProxy>();
        result.addAll(x);
        result.addAll(y);
        return result;
    }    
    
    private HashSet<EdgeProxy> getOutgoingTransitions(ExtendedAutomaton efa, HashSet<NodeProxy> coset) {
        HashSet<EdgeProxy> result = new HashSet<EdgeProxy>();
        for(EdgeProxy tran : efa.getTransitions())
            if(coset.contains(tran.getTarget()))
                result.add(tran);
        return result;
    }

    class Partition{
        private HashSet<NodeProxy> coset;
        private NodeProxy state;

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
            this.state = null;
        }
        
        public void setState(NodeProxy state){
            this.state = state;
        }
        
        public NodeProxy getState(){
            return this.state;
        }
    }
}
