
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
    private final TIntHashSet currEplsilon;
    
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
        currEplsilon = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, epsilon);
        currLocalEvents = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, inputLocalEvents);
        currLocalEvents.addAll(currEplsilon.toArray());
        currSharedEvents = ExtendedAutomataIndexFormHelper.setMinus(currAlphabet, currLocalEvents);
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

                EventDeclProxy currEvent = indexMap.getEventAt(event);
                prjEFA.addEvent(currEvent);
                
                int to = states[0];
                
                Partition pTo = getPartition(to);
                int statusTo = pTo.getStatus();
                NodeProxy currTarget = prjEFA.addState(getStateName(pTo.getCoset()), 
                                            ExtendedAutomataIndexFormHelper.isAccepting(statusTo), 
                                            ExtendedAutomataIndexFormHelper.isInitial(statusTo), 
                                            false);
                // If there is any guard then do as follows
                String guard = "";
                String action = "";
                if(indexMap.hasAnyGuard(indexAutomaton) || indexMap.hasAnyAction(indexAutomaton)){
                    for(int stFrom : pFrom.getCoset().toArray()){
                        try{
                            int[] currGuards = indexAutomata.getGuardStateEventTable()[indexAutomaton][stFrom][event];
                            if(currGuards.length > 1){
                                currGuards = ExtendedAutomataIndexFormHelper.clearMaxInteger(currGuards);
                                for(int currGuard : currGuards){
                                    SimpleExpressionProxy guardExp = indexMap.getGuardExpressionAt(currGuard);
                                    if(states.length == 2){
                                        guard = guardExp.toString();
                                        break;
                                    }
                                    String strGuard = "(" + guardExp.toString() + ")";
                                    if(guard.isEmpty()){
                                        guard = strGuard;
                                    } else {
                                        if(!guard.contains(guardExp.toString()))
                                            guard += cot.getOrOperator().getName() + strGuard;
                                    }
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
    
    /**
     * Here's where all the magic happens!
     * (1) Get the supremal quasi-congruence of the given EFA 
     * (2) Get the quotient EFA of in terms of congruence
     * (3) Extend the observable event set to eliminate the nondeterminism of the quotient EFA
     * (4) Check for nondeterministic
     * (4.1) If it is nondeterministic and at least one event is changed as observable to fix it then goto (1)
     * (4.2) If none is changed, i.e., the quotient EFA is already deterministic then continue
     * (5) Check for OCC (Output Control Consistency)
     * (5.1) If it is not OCC then fix and goto (1)
     * (5.2) If it is OCC then continue
     * (6) Final check for the critical states (outgoing transitions with the same source, event, and target 
     *     but different actions, and in case no hidden events, guards) and split them if possible
     */
    private void project(){
        boolean hasLocalEvents;
        boolean isOCC = (uncontrollableEvents.isEmpty())?true:false;
        while(true){
            ps = getQC();
            getQuotient();
            hasLocalEvents = extendEvent();
            if(!hasLocalEvents && isOCC){
                if(indexMap.hasAnyAction(indexAutomaton) || indexMap.hasAnyGuard(indexAutomaton)){
                    clearCriticalStates();
                }
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
                    diff = ExtendedAutomataIndexFormHelper.setMinus(X.getCoset(), phiB);
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
                // Forbidden location are not considered in this implementation!
//                if(!isForbbiden)
//                    isForbbiden = ExtendedAutomataIndexFormHelper.isForbidden(status);
            }
            int status = ExtendedAutomataIndexFormHelper.createStatus(isInitial, isAccepted, isForbbiden);
            p.setState(newstate++, status);
        }
        // <state> x <event> -> <state[]> 
        quotient = new int[ps.size()][indexAutomata.getNbrUnionEvents()][];
        
        for(Partition p : ps){
            int source = p.getState();
            if(source == Integer.MAX_VALUE)
                throw new NullPointerException("getQuotient : partition returns Null quotiont state");
            for(int event=0; event<indexAutomata.getNbrUnionEvents(); event++){
                quotient[source][event] = new int[]{Integer.MAX_VALUE};
                for(int currState : p.getCoset().toArray()){
                    int next = indexAutomata.getNextStateTable()[indexAutomaton][currState][event];
                    if(next == Integer.MAX_VALUE)
                        continue;
                    int target = getQuotientState(next, ps);
                    if(target == Integer.MAX_VALUE)
                        throw new NullPointerException("getQuotient : finding quotient for the state <" + next + "> returns Null");
                    int[] currQuotientStates = quotient[source][event];
                    if(currSharedEvents.contains(event) || source != target){
                        quotient[source][event] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(target,currQuotientStates);
                    } 
                    
                }
            }
        }
    }
    
    private boolean extendEvent(){
        boolean hasLocalEvent = false;
        // Enlargement from the local events in quotient EFA
        TIntHashSet B = new TIntHashSet();
        for(int state = 0; state < quotient.length; state++){
            for(int event = 0; event < quotient[state].length; event++){
                int[] nexts = quotient[state][event];
                if(nexts.length > 1 && currLocalEvents.contains(event) && !currEplsilon.contains(event)){
                    // Enlarge the set B to contains local events in the quotient EFA
                    B.add(event);
                    hasLocalEvent = true;
                }
            }
        }
       
        /**
         * First, check if the quotient EFA is nondeterministic
         */
        TIntHashSet N = getNdStates();
        
        // If it is nondeterministic then fix it by making local events to observable events
        if(!N.isEmpty()){
            // The local events hidden in the cosets
            TIntHashSet H = new TIntHashSet();
            for(int ndState : N.toArray()){
                Partition p = getPartition(ndState);
                TIntHashSet hiddenEvents = getHiddenEvents(p);
                H.addAll(hiddenEvents.toArray());
            }

            // Add all events in B and H (B union H) to the set of shared events 
            addAllSharedEvent(B);
            addAllSharedEvent(H);

            // The set of events which are in H but not in B
            TIntHashSet H_B = ExtendedAutomataIndexFormHelper.setMinus(H, B);


            // Check each event 
            for(int e : H_B.toArray()){
                addLocalEvent(e);
                for(int y : N.toArray()){
                    if(!split(y)){
                        addSharedEvent(e);
                        break;
                    }
                }
            }
        } else {
            // If it is deterministic then we are done in this part
            addAllSharedEvent(B);
        }
        
        /**
         * Second, check for possible critical states, i.e., two or more outgoing transitions with 
         * the same source, label, and target but different actions
         */
        if(indexMap.hasAnyAction(indexAutomaton) || indexMap.hasAnyGuard(indexAutomaton)){
            for(int source = 0; source < quotient.length; source++){
                for(int event = 0; event < quotient[source].length; event++){
                    // Removing duplicated states
                    int[] targets = quotient[source][event];
                    if(targets.length <= 2)
                        continue;
                    targets = ExtendedAutomataIndexFormHelper.clearMaxInteger(targets);
                    TIntHashSet temp = new TIntHashSet();
                    for(int target : targets){
                        boolean added = temp.add(target);
                        // If the state is already in the set then it might be critical 
                        if(!added){
                            boolean checkActions = checkActions(source, event, target);
                            boolean checkGuards = checkGuards(source, event, target);
                            if(!(checkActions && checkGuards)){
                                // Find the local events hidden in the cosets so by making them observable we may fix the problem otherwise later split them
                                TIntHashSet H = new TIntHashSet();
                                Partition p = getPartition(source);
                                TIntHashSet hiddenEvents = getHiddenEvents(p);
                                H.addAll(hiddenEvents.toArray());
                                if(!H.isEmpty()){
                                    for(int e : H.toArray()){
                                        if(!nopath2(getPartition(source), event, getPartition(target))){
                                            // If no path exists then make it observable to fix the guard or action problem 
                                            addSharedEvent(e);
                                            hasLocalEvent = true;
                                        }
                                    }                
                                }
                            }
                        }
                    }
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
                    if(currLocalEvents.contains(currEvent)){
                        int st = nextStateTable[currstate][currEvent];
                        if(st == Integer.MAX_VALUE)
                            continue;
                        boolean isNew = eqStates.add(st);
                        if(isNew) stk.push(st);
                    }
                }
            } else {
                for(int currEvent : currAlphabet.toArray()){
                    if(currLocalEvents.contains(currEvent)){
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
                if(currLocalEvents.contains(currEvent)){
                    for(int pre : preStates){
                        if(pre == Integer.MAX_VALUE)
                            continue;
                        boolean isNew = eqStates.add(pre);
                        if(isNew) stk.push(pre);
                    }
                } else if(currEvent == event){
                    for(int preState : preStates){
                        if(preState != Integer.MAX_VALUE)
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

    private void split2(int source) {
        HashSet<Partition> newPs = new HashSet<Partition>();
        HashSet<Partition> removePs = new HashSet<Partition>();
        for(int event = 0; event < quotient[source].length; event++){
            int[] states = quotient[source][event];
            if(states.length <= 2)
                continue;
            int target = states[0];
            Partition sourceP = getPartition(source);
            Partition targetP = getPartition(target);
            for(int st : sourceP.getCoset().toArray()){
                int next = indexAutomata.getNextStateTable()[indexAutomaton][st][event];
                if(targetP.getCoset().contains(next)){
                    TIntHashSet stEq = findEquivalentStates(st, false);
                    newPs.add(new Partition(stEq));
                }
            }
            removePs.add(sourceP);
        }
        ps.removeAll(removePs);
        ps.addAll(newPs);
        getQuotient();
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
            for(int i=0;i<Es.size();i++){
                for(int j=i+1;j<Es.size();j++){
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
    
    private boolean nopath2(Partition source, int event, Partition target) {
        TIntHashSet states = new TIntHashSet();
        for(int state : source.getCoset().toArray()){
            int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
            if(next == Integer.MAX_VALUE || !target.getCoset().contains(next))
                continue;
            states.add(state);
        }
        TIntHashSet Ei, Ej;
        int[] p = states.toArray();
        for(int i=0;i<p.length;i++){
            for(int j=i+1;j<p.length;j++){
                    Ei = findEquivalentStates(p[i], false);                
                    Ej = findEquivalentStates(p[j], false);                
                    if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty())
                        return false;
            }
        }
        return true;
    }
    
    private TIntHashSet getHiddenEvents(Partition p) {
        TIntHashSet hiddenEvents = new TIntHashSet();
        for(int state : p.getCoset().toArray()){
            int[] activeEvents = indexAutomata.getOutgoingEventsTable()[indexAutomaton][state];
            activeEvents = ExtendedAutomataIndexFormHelper.clearMaxInteger(activeEvents);
            for(int event : activeEvents){
                int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
                if(currLocalEvents.contains(event) && p.getCoset().contains(next) && !currEplsilon.contains(event))
                    hiddenEvents.add(event);
            }
        }
        return hiddenEvents;
    }

    private TIntHashSet getNdStates() {
        TIntHashSet ndStates = new TIntHashSet();
        for(int i=0; i < quotient.length; i++){
            for(int j=0; j < quotient[i].length; j++){
                // Removing duplicated states
                TIntHashSet states = new TIntHashSet(quotient[i][j]);
                // If the size is larger than MAX_VALUE and a state then it is nondeterministic
                if(states.size() > 2){
                    ndStates.add(i);
                }
            }
        }
        return ndStates;
    }
    
    private void clearCriticalStates() {
        for(int source = 0; source < quotient.length; source++){
            for(int event = 0; event < quotient[source].length; event++){
                // Removing duplicated states
                int[] targets = quotient[source][event];
                if(targets.length <= 2)
                    continue;
                TIntHashSet temp = new TIntHashSet();
                for(int target : targets){
                    if(target == Integer.MAX_VALUE) continue;
                    boolean added = temp.add(target);
                    // If the state is already in the set then it might be critical 
                    if(!added){
                        boolean checkAction = checkActions(source, event, target);
                        boolean checkGuard = checkGuards(source, event, target);
                        if(!(checkAction && checkGuard)){
                            boolean nopath = nopath2(getPartition(source), event, getPartition(target));
                            // No path then split
                            if(nopath) split2(source);
                        }
                    }
                }
            }
        }
    }

    private boolean checkActions(int quoFrom, int event, int quoTo) {
        Partition fromP = getPartition(quoFrom);
        Partition toP = getPartition(quoTo);
        HashSet<String> set = new HashSet<String>();
        for(int from : fromP.getCoset().toArray()){
            int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];
            if(next == Integer.MAX_VALUE || !toP.getCoset().contains(next))
                continue;
            
            int[] actions = indexAutomata.getActionStateEventTable()[indexAutomaton][from][event];
            if(actions == null || actions.length == 1)
                continue;
            
            String str = "";
            for(int action : actions){
                if(action != Integer.MAX_VALUE)
                    str += indexMap.getActionExpressionAt(action).toString();
            }
            if(!str.isEmpty())
                set.add(str);
        }
        
        if(set.size() > 1)
            return false;
        
        return true;
    }
    private boolean checkGuards(int quoFrom, int event, int quoTo) {
        Partition fromP = getPartition(quoFrom);
        Partition toP = getPartition(quoTo);
        HashSet<String> set = new HashSet<String>();
        for(int from : fromP.getCoset().toArray()){
            int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];
            
            if(next == Integer.MAX_VALUE || !toP.getCoset().contains(next))
                continue;
            
            int[] guards = indexAutomata.getGuardStateEventTable()[indexAutomaton][from][event];
            if(guards == null || guards.length == 1)
                continue;
            
            String str = "";
            for(int guard : guards){
                if(guard != Integer.MAX_VALUE)
                    str += indexMap.getGuardExpressionAt(guard).toString();
            }
            if(!str.isEmpty())
                set.add(str);
        }
        
        if(set.size() > 1){
            boolean noPath = nopath2(getPartition(quoFrom), event, getPartition(quoTo));
            // If there is no path then returns false since it is not a problem
            if(noPath) return false;
        }
        return true;
    }
 
    private Partition getPartition(int state){
        for(Partition p:ps)
            if(p.getState() == state)
                return p;
        return null;
    }
    
    private String getStateName(TIntHashSet coset) {
        String str = "{";
        for (TIntIterator it = coset.iterator(); it.hasNext();) {
            int st = it.next();
            NodeProxy loc = indexMap.getLocationAt(indexAutomaton, st);
            if(it.hasNext())
                str += loc.getName() + ",";
            else
                str += loc.getName() + "}";
        }
        return str;
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
    
    
