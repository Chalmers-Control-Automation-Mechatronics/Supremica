/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.ComponentKind;
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
    private final ModuleSubjectFactory factory;
    private final ExpressionParser parser;
    
    /**
     * The constructor method of the AutomatonObserver class.
     * @param exAutomaton The input automaton for analyze
     */    
    public AutomatonObserver(ExtendedAutomata exAutomata){
        this.exAutomata = exAutomata;
        this.factory = ModuleSubjectFactory.getInstance();
        this.parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        this.observerTimer = new ActionTimer();
        this.nrQCIteration = 0;
        this.localEvents = getLocalEventSet(this.exAutomata);
        this.sharedEvents = eventDifference(new HashSet<EventDeclProxy>(this.exAutomata.getUnionAlphabet()), localEvents);
    }
    
    public void compute(){
        HashSet<ExtendedAutomaton> EFAs = new HashSet<ExtendedAutomaton>();
        int i = 0;
        observerTimer.reset();
        observerTimer.start();
        for(ExtendedAutomaton efa:exAutomata){
            while(true){
                ++i;
                HashSet<Partition> quasi_congruence = getQC(efa);
                ExtendedAutomaton quotient = getQuotient(efa, quasi_congruence);
                boolean hasLocalTransitions = extendEvent(efa, quotient, quasi_congruence);
                if(!hasLocalTransitions){
                    EFAs.add(quotient);
                    break;
                }
                if(i>500) 
                    throw new UnsupportedOperationException("Something is wrong. The algorithm can't calculate the abstracted model after 500 iteration. "
                        + "Check the algorithm or increase the exception value.");
            }
        }
        
        for(ExtendedAutomaton efa:EFAs)
            exAutomata.addAutomaton(efa);
        
        observerTimer.stop();
        
        System.err.println(getObserverTimer());
    }

    private HashSet<EventDeclProxy> getObservableEvents(ExtendedAutomata exAutomata){
        HashSet<EventDeclProxy> obs = new HashSet<EventDeclProxy>();
         for(EventDeclProxy event : exAutomata.getUnionAlphabet()){
             if(event.isObservable())
                 obs.add(event);
         }
         return obs;
    }
        
    private boolean updateSharedEvents(ExtendedAutomaton quotient) {
        boolean isUpdated = false;
        for(EdgeProxy tran : quotient.getTransitions()){
            for(Proxy e:tran.getLabelBlock().getEventList()){
                EventDeclProxy event = getEvent(e);
                if(localEvents.contains(event)){
                    isUpdated = true;
                    sharedEvents.add(event);
                    localEvents.remove(event);
                }
            }
        }
        return isUpdated;
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
                            if(getEvent(te1).getName().equals(e1.getName()) && t1.getGuardActionBlock() != null)
                                for(BinaryExpressionProxy expr:t1.getGuardActionBlock().getActions())
                                    if(!efa1.extractVariablesFromExpr(expr.getRight()).isEmpty() 
                                            || expr.getOperator().equals(cot.getIncrementOperator()) 
                                            || expr.getOperator().equals(cot.getDecrementOperator())) 
                                        continue outerloop;
                }
                locEvents.add(e1);
            }
        }
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
        
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<EdgeProxy> upTrans = new Stack<EdgeProxy>();
        Stack<EdgeProxy> downTrans = new Stack<EdgeProxy>();

        if(!efa.getNodes().contains(state))
            return eqStates;
        
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
                    EventDeclProxy e = getEvent(event);
                    for(EventDeclProxy ev : localEvents){
                        if(ev.equals(e)){
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
    
    private EventDeclProxy getEvent(Proxy event){
        return exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
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
                    EventDeclProxy e = getEvent(evnt);
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
        SimpleIdentifierSubject identifier = factory.createSimpleIdentifierProxy(efa.getName()+"_QUO");
        GraphSubject graph = factory.createGraphProxy();
        IndexedSetSubject<NodeSubject> nodes = graph.getNodesModifiable();
        ListSubject<EdgeSubject> edges = graph.getEdgesModifiable();
        
        for(Partition p:ps){
            HashSet<NodeProxy> coset = p.getCoset();
            final List<Proxy> propList = new LinkedList<Proxy>();
            if (hasMarkedState(efa, coset))
                propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));

            if (hasForbidden(efa, coset))
                propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME));

            final PlainEventListSubject markingProposition = factory.createPlainEventListProxy(propList);
            SimpleNodeSubject node = factory.createSimpleNodeProxy(getStateName(coset), markingProposition, hasInitialState(efa, coset), null, null, null);
            nodes.add(node);
            p.setState(node);
        }
        
        for(EdgeProxy tran:efa.getTransitions()){
            NodeProxy targetState = getCorrespondingState(tran.getTarget(), ps);
            NodeProxy sourceState = getCorrespondingState(tran.getSource(), ps);
            for(Proxy event:tran.getLabelBlock().getEventList()){
                EventDeclProxy e = getEvent(event);
                String guardText = "";
                String actionText = "";
                try {guardText = tran.getGuardActionBlock().getGuards().get(0).toString();} catch (Exception exp) {}
                try {actionText = tran.getGuardActionBlock().getActions().get(0).toString();} catch (Exception exp) {}
                if(!hasTransition(graph.getEdges(), sourceState, targetState, e)){
                    final List<Proxy> events = new LinkedList<Proxy>();
                    events.add(factory.createSimpleIdentifierProxy(e.getName()));
                    final LabelBlockSubject labelBlock = factory.createLabelBlockProxy(events, null);
                    SimpleExpressionSubject guard = null;
                    if(!guardText.trim().equals(""))
                        try {guard = (SimpleExpressionSubject) parser.parse(guardText, Operator.TYPE_BOOLEAN);} catch (Exception exp) {}
                    List<BinaryExpressionSubject> actions = null;
                    if(!actionText.trim().equals("")){
                        final String[] texts = actionText.split(";");
                        actions = new ArrayList<BinaryExpressionSubject>(texts.length);
                        for (final String text : texts){
                            if (text.length() > 0){
                                try{
                                    final SimpleExpressionSubject action = (SimpleExpressionSubject) parser.parse(text);
                                    final BinaryExpressionSubject binaction = (BinaryExpressionSubject) action;
                                    actions.add(binaction);
                                    } catch (Exception exp) {}
                            }
                        }
                    }
            final GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
            final List<SimpleExpressionSubject> blockGuards = guardActionBlock.getGuardsModifiable();
            blockGuards.clear();
            if (guard != null)
                    blockGuards.add(guard);

            final List<BinaryExpressionSubject> blockActions = guardActionBlock.getActionsModifiable();
            blockActions.clear();
            if (actions != null)
                    blockActions.addAll(actions);

            final EdgeSubject newEdge = factory.createEdgeProxy(sourceState, targetState, labelBlock, guardActionBlock, null, null, null);
                        
                    
                    if(sharedEvents.contains(e)){
                        edges.add(newEdge);
                    } else if(sourceState!=targetState){
                        edges.add(newEdge);
                    }
                }
            }
        }
        SimpleComponentSubject component = factory.createSimpleComponentProxy(identifier, efa.getKind(), graph);
        ExtendedAutomaton quotient = new ExtendedAutomaton(exAutomata, component);
        return quotient;
    }

    private boolean extendEvent(ExtendedAutomaton efa, ExtendedAutomaton quotient, HashSet<Partition> ps){
        
        // Enlargement from the local events in quotient EFA
        HashSet<EventDeclProxy> B = new HashSet<EventDeclProxy>();
        B.addAll(quotient.getAlphabet());
        boolean hasLocalTransitions = updateSharedEvents(quotient);
        // Set of states (partitions) where some share event leads to more than one state (partitions)
        HashSet<NodeProxy> N = getNdStates(quotient);
        
        // If it is deterministic then we are done
        if(N.isEmpty())
            return hasLocalTransitions;

        // The local events hidden in these cosets
        HashSet<EventDeclProxy> H = new HashSet<EventDeclProxy>();
        for(NodeProxy node:N)
            H.addAll(getHiddenLocalEvents(efa, getPartition(node, ps)));
        
        // The set of events which are in H but not in B
        HashSet<EventDeclProxy> H_B = eventDifference(H, B);
        
        addAllSharedEvent(H);
        // Copy of sigma for analyze
        for(EventDeclProxy e : H_B){
            addLocalEvent(e);
            for(NodeProxy y : N){
                if(!split(y, ps, quotient, efa)){
                    addSharedEvent(e);
                    break;
                }
            }
        }
        return hasLocalTransitions;
    }

    private boolean split(NodeProxy y, HashSet<Partition> ps, ExtendedAutomaton quotient, ExtendedAutomaton efa) {
        boolean answer;
        HashMap<EventDeclProxy, HashSet<EdgeProxy>> ndTransitions = getNdTransitions(y, quotient);
        Partition yP = getPartition(y, ps);
        for(EventDeclProxy ndEvent : ndTransitions.keySet()){
            HashSet<Partition> targetPs = new HashSet<Partition>();
            HashSet<EdgeProxy> ndTrans = ndTransitions.get(ndEvent);
            for(EdgeProxy ndTran:ndTrans)
                targetPs.add(getPartition(ndTran.getTarget(), ps));
        
            answer = nopath(yP, ndEvent, targetPs, efa);
            if(!answer)
                return answer;
        }
        return true;
    }

    private boolean nopath(Partition y, EventDeclProxy ndEvent, HashSet<Partition> ys, ExtendedAutomaton efa) {
        HashMap<Partition, HashSet<NodeProxy>> yMap = new HashMap<Partition, HashSet<NodeProxy>>();
        for(Partition p:ys)
            yMap.put(p, new HashSet<NodeProxy>());
        
        for(NodeProxy yNode : y.getCoset()){
            ArrayList<EdgeSubject> trans = efa.getLocationToOutgoingEdgesMap().get(yNode);
            for(EdgeSubject tran:trans){
                for(Proxy evt:tran.getLabelBlock().getEventList()){
                    EventDeclProxy e = getEvent(evt);
                    if(e.getName().equals(ndEvent.getName())){
                        for(Partition p:yMap.keySet()){
                            if(p.getCoset().contains(tran.getTarget())){
                                yMap.get(p).add(tran.getSource());
                            }
                        }
                    }
                }
            }
        }
        ArrayList<HashSet<NodeProxy>> Es = new ArrayList<HashSet<NodeProxy>>();
        for(HashSet<NodeProxy> value:yMap.values())
            Es.add(value);
        
        HashSet<NodeProxy> Ei, Ej;
        for(int i=0;i<Es.size();i++) {
            for(int j=i+1;j<Es.size();j++) {
                Ei = findEquivalentStates(efa, Es.get(i), true);
                Ej = Es.get(j);
                if(!stateIntersection(Ei, Ej).isEmpty())
                    return false;
                Ei=Es.get(i);
                Ej = findEquivalentStates(efa, Es.get(j), true);
                if(!stateIntersection(Ei, Ej).isEmpty())
                    return false;
            }
        }
        return true;
    }
    
    private HashSet<EventDeclProxy> getHiddenLocalEvents(ExtendedAutomaton efa, Partition partition) {
        HashSet<EventDeclProxy> hiddenEvents = new HashSet<EventDeclProxy>();
        HashSet<NodeProxy> nodes = partition.getCoset();
        for(EdgeProxy tran:efa.getTransitions()){
            NodeProxy sourceNode = tran.getSource();
            NodeProxy targetNode = tran.getTarget();
            if(nodes.contains(sourceNode) && nodes.contains(targetNode)){
                for(Proxy event:tran.getLabelBlock().getEventList()){
                    EventDeclProxy e = getEvent(event);
                    if(localEvents.contains(e)){
                        hiddenEvents.add(e);
                    }
                }
            }
        }
        return hiddenEvents;
    }

    private HashSet<NodeProxy> getNdStates(ExtendedAutomaton quotient) {
        HashSet<NodeProxy> ndStates = new HashSet<NodeProxy>();
        HashMap<NodeProxy, ArrayList<EdgeSubject>> map = quotient.getLocationToOutgoingEdgesMap();
        outerloop:
        for(NodeProxy st:map.keySet()){
            ArrayList<EdgeSubject> edges = map.get(st);
            HashSet<EventDeclProxy> events = new HashSet<EventDeclProxy>();
            for(EdgeSubject edge:edges){
                for(Proxy evt:edge.getLabelBlock().getEventList()){
                    EventDeclProxy e = getEvent(evt);
                    if(!events.contains(e)){
                        events.add(e);
                    } else {
                        ndStates.add(st);
                        continue outerloop;
                    }
                }
            }
        }
        return ndStates;
    }

    private HashMap<EventDeclProxy, HashSet<EdgeProxy>> getNdTransitions(NodeProxy state, ExtendedAutomaton quotient) {
        HashMap<EventDeclProxy, HashSet<EdgeProxy>> map = new HashMap<EventDeclProxy, HashSet<EdgeProxy>>();
        ArrayList<EdgeSubject> edges = quotient.getLocationToOutgoingEdgesMap().get(state);
        for(EdgeProxy edge : edges){
            for(Proxy evt:edge.getLabelBlock().getEventList()){
                EventDeclProxy e = getEvent(evt);
                if(!map.containsKey(e)){
                    HashSet<EdgeProxy> trans = new HashSet<EdgeProxy>();
                    trans.add(edge);
                    map.put(e, trans);
                } else {
                    map.get(e).add(edge);
                }
            }            
        }
        return map;
    }

    private Partition getPartition(NodeProxy state, HashSet<Partition> ps){
        for(Partition p:ps)
            if(p.getState().equals(state))
                return p;
        return null;
    }
    
    private boolean hasTransition(Collection<EdgeProxy> edges, NodeProxy sourceState, NodeProxy targetState, EventDeclProxy e) {
        for(EdgeProxy tran:edges){
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
    
    private boolean addSharedEvent(EventDeclProxy event){
        localEvents.remove(event);
        return sharedEvents.add(event);
    }

    private boolean addLocalEvent(EventDeclProxy event){
        sharedEvents.remove(event);
        return localEvents.add(event);
    }

    private void addAllSharedEvent(HashSet<EventDeclProxy> events){
        for(EventDeclProxy e:events){
            sharedEvents.add(e);
            localEvents.remove(e);
        }
    }

    private void addAllLocalEvent(HashSet<EventDeclProxy> events){
        for(EventDeclProxy e:events){
            sharedEvents.remove(e);
            localEvents.add(e);
        }
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
