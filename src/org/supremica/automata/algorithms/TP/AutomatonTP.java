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
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author shoaei
 */
public class AutomatonTP {
    
    private final Logger logger = LoggerFactory.createLogger(IDE.class);    
    private final ExtendedAutomata exAutomata;
    private final ExtendedAutomaton efa;
    private HashSet<EventDeclProxy> sharedEvents;
    private HashSet<EventDeclProxy> localEvents;
    private final ModuleSubjectFactory factory;
    private final ExpressionParser parser;
    private Transitions transitions;
    
    /**
     * The constructor method of the AutomatonTP class.
     * @param exAutomaton The input automaton for analyze
     */    
    public AutomatonTP(ExtendedAutomata exAutomata, ExtendedAutomaton EFA, HashSet<EventDeclProxy> localEvents){
        this.exAutomata = exAutomata;
        this.efa = EFA;
        this.factory = ModuleSubjectFactory.getInstance();
        this.parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        this.localEvents = localEvents;
        this.sharedEvents = eventDifference(new HashSet<EventDeclProxy>(efa.getAlphabet()), localEvents);
        this.transitions = new Transitions();
        this.transitions.AddAllTransitions(efa);
    }
    
    public ExtendedAutomaton compute(){        
        System.err.println("Transition Projections automaton computing [" + efa.getName() + "] ...");
        ExtendedAutomaton quotient;  
        boolean hasUncontrollable = !efa.getUncontrollableAlphabet().isEmpty();
        int i = 0;
        while(true){
            ++i;
            HashSet<Partition> quasi_congruence = getQC(efa);
            quotient = getQuotient(efa, quasi_congruence);
            System.err.println("Q: " + quotient.getNodes().size());
            boolean hasLocalTransitions = extendEvent(quotient, quasi_congruence);
            if(!hasLocalTransitions){
                if(hasUncontrollable){
                    boolean isOCC = checkOCC(quotient, efa, quasi_congruence);
                    if(isOCC){
                        break;
                    }
                } else {
                    break;
                }
            }
            if(i>500) throw new UnsupportedOperationException("Something is wrong. The algorithm can't calculate the abstracted model after 500 iterations. "
                    + "Check the algorithm or increase the number of iteration.");
        }
        removeLocalEvents(quotient);
        return quotient;
    }
    
    private HashSet<Partition> getQC(ExtendedAutomaton efa){
        System.err.println("QC computing  ...");
        System.err.println("Shared: " + sharedEvents);
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
            i++;
            if(i>1000) throw new UnsupportedOperationException("Something is wrong when computing QC. The algorithm can't calculate the QC after 500 iterations"
                    + "Check the algorithm or increase the number of iteration.");
            
            
            B = W.pop();
            System.err.println("\n ---------------- \n QC step 1 W: " + W.size() + "\n ---------------- \n");
            for(EventDeclProxy e : sharedEvents){
                System.err.println("QC step 1 e: " + e.getName());
                //if(!sharedEvents.contains(e)) continue;
                HashSet<Partition> I = new HashSet<Partition>();
                HashSet<Partition> I12 = new HashSet<Partition>();
                
                if(e.getName().equals("TAOm"))
                    phiB = phiTAOm(efa, B.getCoset());
                else
                    phiB = phiTao(B.getCoset(),e);
                
                System.err.println("QC step 2");
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
        return R;
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
        
        for(Object[] tran : transitions){
            NodeProxy sourceState = getCorrespondingState((NodeProxy)tran[0], ps);
            NodeProxy targetState = getCorrespondingState((NodeProxy)tran[1], ps);
                EventDeclProxy e = (EventDeclProxy)tran[2];
                String guardText = "";
                String actionText = "";
                try {guardText = ((SimpleExpressionProxy)tran[3]).toString();} catch (Exception exp) {}
                try {actionText = ((BinaryExpressionProxy)tran[4]).toString();} catch (Exception exp) {}
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
        SimpleComponentSubject component = factory.createSimpleComponentProxy(identifier, efa.getKind(), graph);
        ExtendedAutomaton quotient = new ExtendedAutomaton(exAutomata, component);
        return quotient;
    }
    
    private boolean extendEvent(ExtendedAutomaton quotient, HashSet<Partition> ps){
        
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
            H.addAll(getHiddenLocalEvents(getPartition(node, ps)));
        
        // The set of events which are in H but not in B
        HashSet<EventDeclProxy> H_B = eventDifference(H, B);
        
        addAllSharedEvent(H);
        // Copy of sigma for analyze
        for(EventDeclProxy e : H_B){
            addLocalEvent(e);
            for(NodeProxy y : N){
                if(!split(y, ps, quotient)){
                    addSharedEvent(e);
                    break;
                }
            }
        }
        return hasLocalTransitions;
    }

    private boolean checkOCC(ExtendedAutomaton quotient, ExtendedAutomaton efa, HashSet<Partition> ps) {
        boolean isOCC = true;
        for(NodeProxy state : quotient.getNodes()){
            HashSet<NodeProxy> sourceCoset = getPartition(state, ps).getCoset();
            if(sourceCoset.size() > 1){
                boolean hasUnconTran = false;
                HashMap<EventDeclProxy, NodeProxy> map = new HashMap<EventDeclProxy, NodeProxy>();
                ArrayList<EdgeSubject> outTrans = quotient.getLocationToOutgoingEdgesMap().get(state);
                for(EdgeSubject tran:outTrans){
                    for(Proxy evt:tran.getLabelBlock().getEventList()){
                        EventDeclProxy e = getEvent(evt);
                        if(e.getKind() == EventKind.UNCONTROLLABLE){
                            hasUnconTran = true;
                            map.put(e, tran.getTarget());
                        }
                    }
                }
                if(hasUnconTran){
                    Stack<NodeProxy> stack = new Stack<NodeProxy>();
                    for(EventDeclProxy event : map.keySet()){
                        HashSet<NodeProxy> targetCoset = getPartition(map.get(event), ps).getCoset();
                        for(Object[] tran : transitions){
                            if(sourceCoset.contains((NodeProxy)tran[0]) && targetCoset.contains((NodeProxy)tran[1])){
                                if(((EventDeclProxy)tran[2]).getName().equals(event.getName()))
                                    stack.add((NodeProxy)tran[0]);
                            }
                        }
                    }
                    HashSet<NodeProxy> visited = new HashSet<NodeProxy>();
                    while(!stack.isEmpty()){
                        NodeProxy node = stack.pop();
                        if(!visited.add(node)) 
                            continue;
                        ArrayList<EdgeSubject> inTrans = efa.getLocationToIngoingEdgesMap().get(node);
                        for(EdgeSubject tran : inTrans){
                            if(sourceCoset.contains(tran.getSource())){
                                for(Proxy evt:tran.getLabelBlock().getEventList()){
                                    EventDeclProxy e = getEvent(evt);
                                    if(e.getKind() == EventKind.UNCONTROLLABLE){
                                        stack.add(tran.getSource());
                                    } else {
                                        addSharedEvent(e);
                                        isOCC = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return isOCC;
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
    private HashSet<NodeProxy> findEquivalentStates(NodeProxy state, boolean downstream){
        
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<Object[]> upTrans = new Stack<Object[]>();
        Stack<Object[]> downTrans = new Stack<Object[]>();

        if(!efa.getNodes().contains(state))
            return eqStates;
        
        eqStates.add(state);
        stk.push(state);
        
        while(!stk.empty()){
            NodeProxy currstate = stk.pop();

            if(downstream)
                downTrans.addAll(getDownStreamEdges(currstate));
            else
                upTrans.addAll(getUpStreamEdges(currstate));
            
            while(!upTrans.isEmpty() || !downTrans.isEmpty()){
                Object[] tran = (downstream)?downTrans.pop():upTrans.pop();
                    EventDeclProxy e = (EventDeclProxy)tran[2];
                    for(EventDeclProxy ev : localEvents){
                        if(ev.equals(e)){
                            NodeProxy nxState = (downstream)?(NodeProxy)tran[1]:(NodeProxy)tran[0];
                            boolean result = eqStates.add(nxState);
                            if(result) stk.push(nxState);
                            break;
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
    
    private EventDeclProxy getEvent(Proxy event){
        return exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
    }
    
//    private HashSet<EdgeProxy> getUpStreamEdges(ExtendedAutomaton efa, NodeProxy st){
//        HashSet<EdgeProxy> upTrans = new HashSet<EdgeProxy>();
//            for(EdgeProxy e : efa.getTransitions())
//                if(e.getTarget().getName().equals(st.getName()))
//                    upTrans.add(e);
//        return upTrans;
//    }

//    private HashSet<EdgeProxy> getDownStreamEdges(ExtendedAutomaton efa, NodeProxy st){
//        HashSet<EdgeProxy> downTrans = new HashSet<EdgeProxy>();
//            for(EdgeProxy e : efa.getTransitions())
//                if(e.getSource().getName().equals(st.getName()))
//                    downTrans.add(e);
//        return downTrans;
//    }

    private HashSet<Object[]> getUpStreamEdges(NodeProxy st){
        return transitions.getIncommingTransitions(st);
    }
    
    private HashSet<Object[]> getDownStreamEdges(NodeProxy st){
        return transitions.getOutgoingTransitions(st);
    }
    
    private HashSet<NodeProxy> findImdPreImg(NodeProxy state, EventDeclProxy event){
        HashSet<NodeProxy> eqStates = new HashSet<NodeProxy>();
        HashSet<NodeProxy> imdImg = new HashSet<NodeProxy>();
        Stack<NodeProxy> stk = new Stack<NodeProxy>();
        Stack<Object[]> upTrans = new Stack<Object[]>();

        eqStates.add(state);
        stk.push(state);

        while(!stk.empty()){
            NodeProxy currstate = stk.pop();
            upTrans.addAll(getUpStreamEdges(currstate));

            while(!upTrans.isEmpty()){
                Object[] tran = upTrans.pop();
                EventDeclProxy e = (EventDeclProxy)tran[2];
                NodeProxy nxState = (NodeProxy)tran[0];
                if(localEvents.contains(e)){
                    boolean result = eqStates.add(nxState);
                    if(result) stk.push(nxState);
                }
                else if(e.getName().equals(event.getName())){
                    imdImg.add(nxState);
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
                
    private HashSet<NodeProxy> phiTao(HashSet<NodeProxy> b, EventDeclProxy e) {
        System.err.println("phiTao: imdImg");
        HashSet<NodeProxy> imdImg = findImdPreImg(b, e);
        System.err.println("phiTao: phiB");
        HashSet<NodeProxy> phiB = findEquivalentStates(imdImg, false);
        return phiB;
    }

    private HashSet<NodeProxy> phiTAOm(ExtendedAutomaton efa, HashSet<NodeProxy> b) {
        HashSet<NodeProxy> phiB = new HashSet<NodeProxy>();
        System.err.println("phiTAom: phiB");
        Set<NodeProxy> m = efa.getMarkedLocations();
        for(NodeProxy st : b)
            if(m.contains(st))
                phiB=findEquivalentStates(st, false);
        
        return phiB;
    }    

    private boolean split(NodeProxy y, HashSet<Partition> ps, ExtendedAutomaton quotient) {
        boolean answer;
        HashMap<EventDeclProxy, HashSet<EdgeProxy>> ndTransitions = getNdTransitions(y, quotient);
        Partition yP = getPartition(y, ps);
        for(EventDeclProxy ndEvent : ndTransitions.keySet()){
            HashSet<Partition> targetPs = new HashSet<Partition>();
            HashSet<EdgeProxy> ndTrans = ndTransitions.get(ndEvent);
            for(EdgeProxy ndTran:ndTrans)
                targetPs.add(getPartition(ndTran.getTarget(), ps));
        
            answer = nopath(yP, ndEvent, targetPs);
            if(!answer)
                return answer;
        }
        return true;
    }

    private boolean nopath(Partition y, EventDeclProxy ndEvent, HashSet<Partition> ys) {
        HashMap<Partition, HashSet<NodeProxy>> yMap = new HashMap<Partition, HashSet<NodeProxy>>();
        for(Partition p:ys)
            yMap.put(p, new HashSet<NodeProxy>());
        
        for(NodeProxy yNode : y.getCoset()){
            HashSet<Object[]> trans = transitions.getOutgoingTransitions(yNode);
            for(Object[] tran : trans){
                    EventDeclProxy e = (EventDeclProxy)tran[2];
                    if(e.getName().equals(ndEvent.getName())){
                        for(Partition p:yMap.keySet()){
                            if(p.getCoset().contains((NodeProxy)tran[1])){
                                yMap.get(p).add((NodeProxy)tran[0]);
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
                Ei = findEquivalentStates(Es.get(i), true);
                Ej = Es.get(j);
                if(!stateIntersection(Ei, Ej).isEmpty())
                    return false;
                Ei=Es.get(i);
                Ej = findEquivalentStates(Es.get(j), true);
                if(!stateIntersection(Ei, Ej).isEmpty())
                    return false;
            }
        }
        return true;
    }
    
    private HashSet<EventDeclProxy> getHiddenLocalEvents(Partition partition) {
        HashSet<EventDeclProxy> hiddenEvents = new HashSet<EventDeclProxy>();
        HashSet<NodeProxy> nodes = partition.getCoset();
        for(Object[] tran:transitions){
            NodeProxy sourceNode = (NodeProxy)tran[0];
            NodeProxy targetNode = (NodeProxy)tran[1];
            if(nodes.contains(sourceNode) && nodes.contains(targetNode)){
                EventDeclProxy e = (EventDeclProxy)tran[2];
                if(localEvents.contains(e)){
                    hiddenEvents.add(e);
                }
            }
        }
        return hiddenEvents;
    }

    private HashSet<NodeProxy> getNdStates(ExtendedAutomaton quotient) {
        Transitions trans = new Transitions();
        trans.AddAllTransitions(quotient);
        HashSet<NodeProxy> ndStates = new HashSet<NodeProxy>();
        for(NodeProxy node : quotient.getNodes()){
            HashSet<String> events = new HashSet<String>();
            for(Object[] tran : trans.getOutgoingTransitions(node)){
                boolean hasNotThis = events.add(((EventDeclProxy)tran[2]).getName());
                if(!hasNotThis){
                    ndStates.add(node);
                    break;
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

    private void removeLocalEvents(ExtendedAutomaton quotient) {
        for(EventDeclProxy e:localEvents)
            quotient.getAlphabet().remove(e);
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
    
    class Transitions implements Iterable<Object[]> {
        private HashSet<Object[]> transitions;
        public Transitions(){
            this.transitions = new HashSet<Object[]>();
        }
        
        public void AddAllTransitions(ExtendedAutomaton efa){
            for(EdgeProxy tran : efa.getTransitions())
                for(Proxy e:tran.getLabelBlock().getEventList()){
                    Object[] t = new Object[5];
                    t[0] = tran.getSource();
                    t[1] = tran.getTarget();
                    t[2] = getEvent(e);
                    if(tran.getGuardActionBlock() != null && !tran.getGuardActionBlock().getGuards().isEmpty())
                        t[3] = tran.getGuardActionBlock().getGuards().get(0);
                    else 
                        t[3] = null;

                    if(tran.getGuardActionBlock() != null && !tran.getGuardActionBlock().getActions().isEmpty())
                        t[4] = tran.getGuardActionBlock().getActions().get(0);
                    else 
                        t[4] = null;
                    
                    
                    transitions.add(t);
                }
        }
        
        public void addTransition(NodeProxy source, NodeProxy target, EventDeclProxy event, 
                SimpleExpressionProxy guard, BinaryExpressionProxy action){
            Object[] t = new Object[5];
            t[0] = source;
            t[1] = target;
            t[2] = event;
            t[3] = guard;
            t[4] = action;
            transitions.add(t);
        }
        
        public HashSet<Object[]> getTransitions(){
            return transitions;
        }
        
        public HashSet<Object[]> getOutgoingTransitions(NodeProxy node){
            HashSet<Object[]> out = new HashSet<Object[]>();
            for(Object[] t:transitions)
                if((NodeProxy)t[0] == node)
                    out.add(t);
            return out;
        }
        
        public HashSet<Object[]> getIncommingTransitions(NodeProxy node){
            HashSet<Object[]> out = new HashSet<Object[]>();
            for(Object[] t:transitions)
                if((NodeProxy)t[1] == node)
                    out.add(t);
            return out;
        }

        public Iterator<Object[]> iterator() {
            return transitions.iterator();
        }
        
    }
    
}
