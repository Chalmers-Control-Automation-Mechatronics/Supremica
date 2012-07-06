
package org.supremica.automata.algorithms.TransitionProjection;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * AutomatonTransitionProjection class to project the given EFA/DFA. 
 * 
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class AutomatonTransitionProjection {
    
    private final Logger logger = LoggerFactory.createLogger(AutomatonTransitionProjection.class);
    private final ExtendedAutomataIndexForm indexAutomata;
    private final int indexAutomaton;
    private final ExtendedAutomataIndexMap indexMap;
    private final TIntHashSet currAlphabet;
    private final TIntHashSet uncontrollableEvents;
    private final int MARK_EVENT = Short.MAX_VALUE;
    private final int[][] nextStateTable;
    private TIntHashSet currLocalEvents;
    private TIntHashSet currSharedEvents;
    private HashSet<Partition> ps;
    // <state> x <event> -> <state[]> 
    private int[][][] quotient;
    
    /**
     * Constructor method of AutomatonTransitionProjection. 
     * @param exAutomataIndexForm Index form of the Automata model
     * @param exAutomatonIndex Index of the automaton to be projected
     * @param localEventsIndex Set of local events
     */
    public AutomatonTransitionProjection(ExtendedAutomataIndexForm exAutomataIndexForm, int exAutomatonIndex, int[] localEventsIndex){
        indexAutomata = exAutomataIndexForm;
        indexAutomaton = exAutomatonIndex;
        indexMap = exAutomataIndexForm.getAutomataIndexMap();
        nextStateTable = indexAutomata.getNextStateTable()[indexAutomaton];
        currAlphabet = ExtendedAutomataIndexFormHelper.getTrueIndexes(exAutomataIndexForm.getAlphabetEventsTable()[exAutomatonIndex]);
        TIntHashSet inputLocalEvents = ExtendedAutomataIndexFormHelper.toIntHashSet(localEventsIndex);
        TIntHashSet epsilon = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getEpsilonEventsTable());
        currLocalEvents = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, inputLocalEvents);
        currSharedEvents = ExtendedAutomataIndexFormHelper.setDifference(currAlphabet, currLocalEvents);
        currSharedEvents = ExtendedAutomataIndexFormHelper.setDifference(currSharedEvents, epsilon);
        TIntHashSet allUncontrollableEvents = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getUncontrollableEventsTable());
        uncontrollableEvents = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, allUncontrollableEvents);
        ps = null;
        quotient = null;
    }

    public ExtendedAutomaton getProjectedEFA(){
        ExtendedAutomaton originalEFA = indexMap.getExtendedAutomatonAt(indexAutomaton);
        ExtendedAutomaton prjEFA = new ExtendedAutomaton(originalEFA.getName(), originalEFA.getKind());
        CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        project();
        for(int e : currSharedEvents.toArray()){
            EventDeclProxy event = indexMap.getEventAt(e);
            prjEFA.addEvent(event);
        }
        for(int from = 0; from < quotient.length; from++){
            Partition pFrom = getPartition(from);
            int statusFrom = pFrom.getStatus();
            NodeProxy currSource = prjEFA.addState(getStateName(pFrom.getCoset()), 
                                         ExtendedAutomataIndexFormHelper.isAccepting(statusFrom), 
                                         ExtendedAutomataIndexFormHelper.isInitial(statusFrom), 
                                         false);
            for(int event=0; event<quotient[from].length; event++){
                int[] states = quotient[from][event];
                
                if(states.length == 1)
                    continue;

                String guard = "";
                String action = "";
                EventDeclProxy currEvent = indexMap.getEventAt(event);
                states = ExtendedAutomataIndexFormHelper.clearMaxInteger(states);
                int to = states[0];
                Partition pTo = getPartition(to);
                int statusTo = pTo.getStatus();
                NodeProxy currTarget = prjEFA.addState(getStateName(pTo.getCoset()), 
                                            ExtendedAutomataIndexFormHelper.isAccepting(statusTo), 
                                            ExtendedAutomataIndexFormHelper.isInitial(statusTo), 
                                            false);
                // If there is any guard then do as follows
                if(indexMap.hasAnyGuard(indexAutomaton) || indexMap.hasAnyAction(indexAutomaton)){
                    for(int stFrom : pFrom.getCoset().toArray()){
                        try{
                            int[] currGuards = indexAutomata.getGuardStateEventTable()[indexAutomaton][stFrom][event];
                            if(currGuards.length > 1){
                                currGuards = ExtendedAutomataIndexFormHelper.clearMaxInteger(currGuards);
                                for(int currGuard : currGuards){
                                    SimpleExpressionProxy guardExp = indexMap.getGuardExpressionAt(currGuard);
                                    if(states.length == 1){
                                        guard = guardExp.toString();
                                        break;
                                    }
                                    String strGuard = "(" + guardExp.toString() + ")";
                                    if(guard.isEmpty())
                                        guard = strGuard;
                                    else
                                        guard += cot.getOrOperator().getName() + strGuard;
                                }
                            }
                        } catch(Exception exc){}
                        try{
                            int[] currActions = indexAutomata.getActionStateEventTable()[indexAutomaton][stFrom][event];
                            if(currActions.length > 1){
                                currActions = ExtendedAutomataIndexFormHelper.clearMaxInteger(currActions);
                                for(int currAction : currActions){
                                    BinaryExpressionProxy actionExp = indexMap.getActionExpressionAt(currAction);
                                    if(action.isEmpty()){
                                        action = actionExp.toString();
                                    } else {
                                        if(!action.contains(actionExp.toString()))
                                            action += "; " + actionExp.toString();
                                    }
                                }
                            }
                        } catch(Exception exc){}
                    }
                }
                prjEFA.addTransition(currSource.getName(), currTarget.getName(), currEvent.getName(), guard, action);
            }
        }
        return prjEFA;
    }
    
    private void project(){
        boolean hasLocalEvents;
        boolean isOCC = (uncontrollableEvents.isEmpty())?true:false;
        while(true){
            ps = getQC();
            getQuotient();
            hasLocalEvents = extendEvent();
            if(!hasLocalEvents && isOCC){
                break;
            } else if(!hasLocalEvents){
                isOCC = checkOCC();
            }
        }
    }
    
    private HashSet<Partition> getQC(){
        Stack<Partition> W = new Stack<Partition>(); // Set W (splitter) in WONG paper 
        HashSet<Partition> R = new HashSet<Partition>(); // Set Rho in WONG paper 
        TIntHashSet phiB,inter,diff; // Sets of phiB, intersection, and difference

        TIntHashSet Q = new TIntHashSet();
        
        int nbrStates = indexAutomata.getAutomataSize()[indexAutomaton];
        for(int i=0; i < nbrStates; i++)
            Q.add(i);
        
        // Initiate the sets W and R with all states
        W.push(new Partition(Q));
        R.add(new Partition(Q));
        
        // New mark event (Tao_m) to handle marked states
        currSharedEvents.add(MARK_EVENT);
        while(!W.isEmpty()){
            Partition B = W.pop(); // B: temporary partition
            for(int e : currSharedEvents.toArray()){
                HashSet<Partition> I = new HashSet<Partition>();
                HashSet<Partition> I12 = new HashSet<Partition>();
                
                if(e == MARK_EVENT){
                    phiB = phiTAOm(B.getCoset());
                } else {
                    phiB = phiTao(B.getCoset(),e);
                }
                if(phiB.isEmpty()) continue;
                
                // X: A partition in Rho
                for (Partition X : R) {
                    inter = ExtendedAutomataIndexFormHelper.setIntersection(X.getCoset(), phiB);
                    if (inter.isEmpty()) continue;
                    diff = ExtendedAutomataIndexFormHelper.setDifference(X.getCoset(), phiB);
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
        currSharedEvents.remove(MARK_EVENT);
        return R;
    }

    private void getQuotient(){
        // <state> x <event> -> <state[]> 
        quotient = new int[ps.size()][indexAutomata.getNbrUnionEvents()][];
        for(int i=0; i<ps.size();i++){
            for(int j=0; j<indexAutomata.getNbrUnionEvents();j++){
                quotient[i][j] = new int[]{Integer.MAX_VALUE};
            }
        }
        // Building up new quotient state space
        int newstate = 0;
        for(Partition p : ps){
            boolean isInitial = false;
            boolean isAccepted = false;
            boolean isForbbiden = false;
            for(int st : p.getCoset().toArray()){
                int status = indexAutomata.getStateStatusTable()[indexAutomaton][st];
                if(!isInitial)
                    isInitial = ExtendedAutomataIndexFormHelper.isInitial(status);
                if(!isAccepted)
                    isAccepted = ExtendedAutomataIndexFormHelper.isAccepting(status); 
//                if(!isForbbiden)
//                    isForbbiden = ExtendedAutomataIndexFormHelper.isForbidden(status);
            }
            int status = ExtendedAutomataIndexFormHelper.createStatus(isInitial, isAccepted, isForbbiden);
            p.setState(newstate++, status);
        }
        for(Partition p : ps){
            int fromQuotientState = p.getState();
            if(fromQuotientState == Integer.MAX_VALUE)
                throw new NullPointerException("getQuotient : fromQuotientState : Null value");
            for(int currState : p.getCoset().toArray()){
                for(int currEvent : currAlphabet.toArray()){
                    int st = indexAutomata.getNextStateTable()[indexAutomaton][currState][currEvent];
                    if(st == Integer.MAX_VALUE)
                        continue;
                    int toQuotientState = getQuotientState(st, ps);
                    if(toQuotientState == Integer.MAX_VALUE)
                        throw new NullPointerException("getQuotient : Cannot find state " + st + "in any partition");
                    int[] currQuotientStates = quotient[fromQuotientState][currEvent];
                    if(currSharedEvents.contains(currEvent) || fromQuotientState != toQuotientState){
                        quotient[fromQuotientState][currEvent] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(toQuotientState,currQuotientStates);
                    } 
                }
            }
        }
    }
    
    private boolean extendEvent(){
        boolean hasLocalEvent = false;
        // Enlargement from the local events in quotient EFA
        TIntHashSet B = new TIntHashSet();
        for(Partition p : ps){
            int currQuoState = p.getState();
            if(currQuoState == Integer.MAX_VALUE)
                throw new NullPointerException("getQuotient : fromQuotientState : Null");
            
            for(int currEvent : currAlphabet.toArray()){
                int[] quoStates = quotient[currQuoState][currEvent];
                if(quoStates.length > 1){
                    B.add(currEvent);
                    currSharedEvents.add(currEvent);
                    boolean removed = currLocalEvents.remove(currEvent);
                    if(removed) hasLocalEvent = true;
                }
            }
        }
        // Set of states (partitions) where some share event leads to more than one state (partitions)
        TIntHashSet N = getNdStates();
        
        // If it is deterministic then we are done
        if(N.isEmpty())
            return hasLocalEvent;

        // The local events hidden in these cosets
        TIntHashSet H = new TIntHashSet();
        for(int ndState : N.toArray()){
            Partition p = getPartition(ndState);
            H.addAll(getHiddenLocalEvents(p).toArray());
        }
        // The set of events which are in H but not in B
        TIntHashSet H_B = ExtendedAutomataIndexFormHelper.setDifference(H, B);
        
        addAllSharedEvent(H);
        // Copy of sigma for analyze
        for(int e : H_B.toArray()){
            addLocalEvent(e);
            for(int y : N.toArray()){
                if(!split(y)){
                    addSharedEvent(e);
                    break;
                }
            }
        }
        return hasLocalEvent;
    }

    private boolean checkOCC() {
        boolean isOCC = true;
        for(int state = 0; state < quotient.length; state++){
            TIntHashSet sourceCoset = getPartition(state).getCoset();
            if(sourceCoset.size() > 1){
                boolean hasUnconEvent = false;
                TIntIntHashMap map = new TIntIntHashMap();
                int[][] eventState = quotient[state];
                for(int event = 0; event < eventState.length; event++){
                    if(uncontrollableEvents.contains(event)){
                        hasUnconEvent = true;
                        int[] states = eventState[event];
                        if(states.length == 1)
                            continue;
                        states = ExtendedAutomataIndexFormHelper.clearMaxInteger(states);
                        map.put(event, states[0]);
                    }
                }
                if(hasUnconEvent){
                    TIntStack stack = new TIntStack();
                    for(int event : map.keys()){
                        TIntHashSet targetCoset = getPartition(map.get(event)).getCoset();
                        for(int source : sourceCoset.toArray()){
                            if(source == Integer.MAX_VALUE)
                                continue;
                            int st = nextStateTable[source][event];
                            if(st == Integer.MAX_VALUE)
                                continue;
                            if(targetCoset.contains(st))
                                stack.push(source);
                        }
                    }
                    
                    TIntHashSet visited = new TIntHashSet();
                    while(stack.size() > 0){
                        int node = stack.pop();
                        if(!visited.add(node)) 
                            continue;
                        int[][] preEventStates = indexAutomata.getPrevStatesTable()[indexAutomaton][node];
                        for(int event : currAlphabet.toArray()){
                            if(currSharedEvents.contains(event) || indexAutomata.getEpsilonEventsTable()[event])
                                continue;
                            
                            int[] preStates = preEventStates[event];
                            if(preStates.length == 1)
                                continue;
                            
                            if(uncontrollableEvents.contains(event)){
                                for(int preState : preStates)
                                    if(preState != Integer.MAX_VALUE)
                                        stack.push(preState);
                            } else {
                                addSharedEvent(event);
                                isOCC = false;
                            }
                        }
                    }
                }
            }
        }
        return isOCC;
    }

    /**
     * Method to find the observation-equivalent states for the given <B>state</B>. The set of states in the coset are connected
     * to the <B>state</B> via unobservable or local transitions. Note that by default all transitions are observable.
     * @param state An state in the automaton
     * @param direction The direction of the breadth-first search. <CODE>true</CODE> for downstream and <CODE>false</CODE> for upstream
     * @return The set of the equivalent states or empty set if <B>state</B> is not a state in the given automaton
     */
    private TIntHashSet findEquivalentStates(int state, boolean downstream){
        TIntHashSet eqStates = new TIntHashSet();
        TIntStack stk = new TIntStack();
        eqStates.add(state);
        stk.push(state);
        
        while(stk.size() > 0){
            int currstate = stk.pop();
            if(downstream){
                for(int currEvent : currAlphabet.toArray()){
                    if(currLocalEvents.contains(currEvent) || indexAutomata.getEpsilonEventsTable()[currEvent]){
                        int st = nextStateTable[currstate][currEvent];
                        if(st == Integer.MAX_VALUE)
                            continue;
                        boolean isNew = eqStates.add(st);
                        if(isNew) stk.push(st);
                    }
                }
            } else {
                for(int currEvent : currAlphabet.toArray()){
                    if(currLocalEvents.contains(currEvent) || indexAutomata.getEpsilonEventsTable()[currEvent]){
                        int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                        if(preStates.length == 1)
                            continue;
                        for(int pre : preStates){
                            if(pre == Integer.MAX_VALUE) continue;
                            boolean isNew = eqStates.add(pre);
                            if(isNew) stk.push(pre);
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
    private TIntHashSet findEquivalentStates(TIntHashSet states, boolean downstream){
        TIntHashSet eqStates = new TIntHashSet();
        for(TIntIterator itr = states.iterator();itr.hasNext();){
            TIntHashSet eqsts = findEquivalentStates(itr.next(), downstream);
            eqStates.addAll(eqsts.toArray());
        }
        return eqStates;
    }

    private TIntHashSet findImdPreImg(int state, int event){
        TIntHashSet eqStates = new TIntHashSet();
        TIntHashSet imdImg = new TIntHashSet();
        TIntStack stk = new TIntStack();

        eqStates.add(state);
        stk.push(state);
        while(stk.size() > 0){
            int currstate = stk.pop();
            for(int currEvent : currAlphabet.toArray()){
                int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                if(preStates.length == 1)
                    continue;
                if(currLocalEvents.contains(currEvent) || indexAutomata.getEpsilonEventsTable()[currEvent]){
                    for(int pre : preStates){
                        if(pre == Integer.MAX_VALUE)
                            continue;
                        boolean isNew = eqStates.add(pre);
                        if(isNew) stk.push(pre);
                    }
                } else if(currEvent == event){
                    for(int preState : preStates){
                        if(preState == Integer.MAX_VALUE)
                            continue;
                        imdImg.add(preState);
                    }                    
                }
            }
        }
        return imdImg;
    }

    private TIntHashSet findImdPreImg(TIntHashSet states, int event){
        TIntHashSet imdImg = new TIntHashSet();
        for(TIntIterator itr = states.iterator();itr.hasNext();)
            imdImg.addAll(findImdPreImg(itr.next(), event).toArray());

        return imdImg;
    }
                
    private TIntHashSet phiTao(TIntHashSet b, int e) {
        TIntHashSet imdImg = findImdPreImg(b, e);
        TIntHashSet phiB = findEquivalentStates(imdImg, false);
        return phiB;
    }

    private TIntHashSet phiTAOm(TIntHashSet b) {
        TIntHashSet phiB = new TIntHashSet();
        for(TIntIterator itr = b.iterator(); itr.hasNext();){
            int st = itr.next();
            int status = indexAutomata.getStateStatusTable()[indexAutomaton][st];
            if(ExtendedAutomataIndexFormHelper.isAccepting(status))
                phiB.addAll(findEquivalentStates(st, false).toArray());
        }
        return phiB;
    }    

    private boolean split(int ndQuoState) {
        boolean answer;
        Partition ndQuoPartition = getPartition(ndQuoState);
        TIntHashSet ndEvents = new TIntHashSet();
        int[][] eventState = quotient[ndQuoState];
        for(int i=0; i < eventState.length; i++){
            int[] states = eventState[i];
            if(states.length == 1)
                continue;
            else if(states.length > 2)
                ndEvents.add(i);
        }
        
        for(int ndEvent : ndEvents.toArray()){
            int[] nextQuoStates = quotient[ndQuoState][ndEvent];
            nextQuoStates = ExtendedAutomataIndexFormHelper.clearMaxInteger(nextQuoStates);
            HashSet<Partition> nextPs = new HashSet<Partition>();
            for(int nextQuoState : nextQuoStates){
                Partition p = getPartition(nextQuoState);
                nextPs.add(p);
            }
            answer = nopath(ndQuoPartition, ndEvent, nextPs);
            if(!answer)
                return answer;
        }
        return true;
    }

    private boolean nopath(Partition y, int ndEvent, HashSet<Partition> ys) {
        HashMap<Partition, TIntHashSet> yMap = new HashMap<Partition, TIntHashSet>();
        for(Partition p:ys)
            yMap.put(p, new TIntHashSet());

        for(int yNode : y.getCoset().toArray()){
            int st = nextStateTable[yNode][ndEvent];

            if(st == Integer.MAX_VALUE)
                continue;

            for(Partition p:yMap.keySet()){
                TIntHashSet coset = p.getCoset();
                if(coset.contains(st)){
                    yMap.get(p).add(yNode);
                }
            }
        }
        
        ArrayList<TIntHashSet> Es = new ArrayList<TIntHashSet>();
        for(TIntHashSet value:yMap.values())
            Es.add(value);
        
        TIntHashSet Ei, Ej;
        for(int i=0;i<Es.size();i++) {
            for(int j=i+1;j<Es.size();j++) {
                Ei = findEquivalentStates(Es.get(i), true);
                Ej = Es.get(j);
                
                if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty())
                    return false;
                Ei=Es.get(i);
                Ej = findEquivalentStates(Es.get(j), true);
                if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty())
                    return false;
            }
        }
        return true;
    }
    
    private TIntHashSet getHiddenLocalEvents(Partition p) {
        TIntHashSet hiddenEvents = new TIntHashSet();
        for(int node : p.getCoset().toArray()){
            int[] activeEvents = indexAutomata.getOutgoingEventsTable()[indexAutomaton][node];
            activeEvents = ExtendedAutomataIndexFormHelper.clearMaxInteger(activeEvents);
            for(int event : activeEvents)
                if(currLocalEvents.contains(event) && indexAutomata.getEpsilonEventsTable()[event])
                    hiddenEvents.add(event);
        }
        return hiddenEvents;
    }

    private TIntHashSet getNdStates() {
        TIntHashSet ndStates = new TIntHashSet();
        for(int i=0; i < quotient.length; i++){
            int[][] events = quotient[i];
            for(int j=0; j < events.length; j++){
                int[] states = events[j];
                if(states.length == 1)
                    continue;
                else if(states.length > 2)
                    ndStates.add(i);
            }
        }
        return ndStates;
    }

    private Partition getPartition(int state){
        for(Partition p:ps)
            if(p.getState() == state)
                return p;
        return null;
    }
    
    private String getStateName(TIntHashSet coset) {
        String s = "{";
        for (TIntIterator it = coset.iterator(); it.hasNext();) {
            int st = it.next();
            NodeProxy loc = indexMap.getLocationAt(indexAutomaton, st);
            if(it.hasNext())
                s += loc.getName() + ",";
            else
                s += loc.getName() + "}";
        }
        return s;
    }

    private void addSharedEvent(int event){
        currSharedEvents.add(event);
        currLocalEvents.remove(event);
    }

    private void addLocalEvent(int event){
        currLocalEvents.add(event);
        currSharedEvents.remove(event);
    }

    private void addAllSharedEvent(TIntHashSet events){
        currSharedEvents.addAll(events.toArray());
        currLocalEvents.removeAll(events.toArray());
    }

    private int getQuotientState(int state, HashSet<Partition> ps) {
        for(Partition p : ps){
            for(int st : p.getCoset().toArray()){
                if(state == st){
                    return p.getState();
                }
            }
        }
        return Integer.MAX_VALUE;
    }
    
    public HashSet<Integer> getSharedEvents(){
        HashSet<Integer> shared = new HashSet<Integer>();
        for(int e : currSharedEvents.toArray())
            shared.add(e);
        return shared;
    }

    public HashSet<Integer> getLocalEvents(){
        HashSet<Integer> local = new HashSet<Integer>();
        for(int e : currLocalEvents.toArray())
            local.add(e);
        return local;
    }
    
    class Partition{
        private TIntHashSet coset;
        private int state;
        private int status;

        public Partition(){
            coset = new TIntHashSet();
            state = Integer.MAX_VALUE;
            status = Integer.MAX_VALUE;
        }

        public Partition(TIntHashSet coset){
            this.coset = coset;
            state = Integer.MAX_VALUE;
            status = Integer.MAX_VALUE;
        }
        
        public TIntHashSet getCoset(){
            return coset;
        }
        
        public void setCoset(TIntHashSet coset){
            this.coset = coset;
        }
        
        public void clear(){
            this.coset.clear();
            this.state = Integer.MAX_VALUE;
            this.status = Integer.MAX_VALUE;
        }
        
        public void setState(int state, int status){
            this.state = state;
            this.status = status;
        }
        
        public int getState(){
            return this.state;
        }
        
        public int getStatus(){
            return this.status;
        }
        
    }
}
    
    
