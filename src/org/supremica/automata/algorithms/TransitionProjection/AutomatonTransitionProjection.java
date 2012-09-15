
package org.supremica.automata.algorithms.TransitionProjection;

import gnu.trove.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.createLogger(AutomatonTransitionProjection.class);
    private final ExtendedAutomataIndexForm indexAutomata;
    private final int indexAutomaton;
    private final ExtendedAutomataIndexMap indexMap;
    private final TIntHashSet currAlphabet;
    private final TIntHashSet uncontrollableEvents;
    private final int MAX_VALUE = Integer.MAX_VALUE;    
    private final int MARK_EVENT = Short.MAX_VALUE;
    private final int[][] nextStateTable;
    private final TIntHashSet currEplsilon;
    private final TIntHashSet currLocalEvents;
    private final TIntHashSet currSharedEvents;
    // <state> x <event> -> <state[]>
    private int[][][] quotient;
    // <state> x <coset>
    private TIntObjectHashMap<TIntHashSet> mapStateCoset;
    // <state> x <status>
    private TIntIntHashMap mapStateStatus;
    // partitions
    private THashSet<TIntHashSet> qc;

    /**
     * Constructor method of AutomatonTransitionProjection.
     * @param exAutomataIndexForm Index form of the Automata model
     * @param exAutomatonIndex Index of the automaton to be projected
     * @param localEventsIndex Set of local events
     */
    public AutomatonTransitionProjection(final ExtendedAutomataIndexForm exAutomataIndexForm, final int exAutomatonIndex, final int[] localEventsIndex){
        indexAutomata = exAutomataIndexForm;
        indexAutomaton = exAutomatonIndex;
        indexMap = exAutomataIndexForm.getAutomataIndexMap();
        nextStateTable = indexAutomata.getNextStateTable()[indexAutomaton];
        currAlphabet = ExtendedAutomataIndexFormHelper.getTrueIndexes(exAutomataIndexForm.getAlphabetEventsTable()[exAutomatonIndex]);
        final TIntHashSet inputLocalEvents = ExtendedAutomataIndexFormHelper.toIntHashSet(localEventsIndex);
        final TIntHashSet epsilon = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getEpsilonEventsTable());
        currEplsilon = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, epsilon);
        currLocalEvents = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, inputLocalEvents);
        currLocalEvents.addAll(currEplsilon.toArray());
        currSharedEvents = ExtendedAutomataIndexFormHelper.setMinus(currAlphabet, currLocalEvents);
        final TIntHashSet allUncontrollableEvents = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getUncontrollableEventsTable());
        uncontrollableEvents = ExtendedAutomataIndexFormHelper.setIntersection(currAlphabet, allUncontrollableEvents);
        quotient = null;
        qc = null;
        mapStateCoset = new TIntObjectHashMap<TIntHashSet>();
        mapStateStatus = new TIntIntHashMap();
    }
    
    /**
     * Return the projected EFA. If the projected EFA is the same as the original EFA (number of nodes and alphabet)
     * then will return <code>Null</code>.
     * @return Projected EFA or <code>Null</code> if the projected EFA and the original EFA are the same
     */
    public ExtendedAutomaton getProjectedEFA(){
        final ExtendedAutomaton originalEFA = indexMap.getExtendedAutomatonAt(indexAutomaton);
        final ExtendedAutomaton prjEFA = new ExtendedAutomaton(originalEFA.getName(), originalEFA.getKind());
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();

        project();
        
        for(int from = 0; from < quotient.length; from++){
            final TIntHashSet pFrom = mapStateCoset.get(from);
            final int statusFrom = mapStateStatus.get(from);
            final NodeProxy currSource = prjEFA.addState(getStateName(pFrom),
                                         ExtendedAutomataIndexFormHelper.isAccepting(statusFrom),
                                         ExtendedAutomataIndexFormHelper.isInitial(statusFrom),
                                         false);
            for(int event=0; event<quotient[from].length; event++){
                final int[] states = quotient[from][event];

                if(states.length == 1) {
                    continue;
                }

                final EventDeclProxy currEvent = indexMap.getEventAt(event);
                prjEFA.addEvent(currEvent);

                final int to = states[0];
                final TIntHashSet pTo = mapStateCoset.get(to);
                final int statusTo = mapStateStatus.get(to);
                final NodeProxy currTarget = prjEFA.addState(getStateName(pTo),
                                            ExtendedAutomataIndexFormHelper.isAccepting(statusTo),
                                            ExtendedAutomataIndexFormHelper.isInitial(statusTo),
                                            false);
                // If there is any guard then do as follows
                String guard = "";
                String action = ""; 
                if(indexMap.hasAnyGuard(indexAutomaton) || indexMap.hasAnyAction(indexAutomaton)){
                    for(TIntIterator itr = pFrom.iterator(); itr.hasNext();){
                        final int stFrom = itr.next();
                        try{
                            final int[] currGuards = indexAutomata.getGuardStateEventTable()[indexAutomaton][stFrom][event];
                            if(currGuards.length > 1){
                                for(final int currGuard : currGuards){
                                    if(currGuard == MAX_VALUE) {
                                        continue;
                                    }
                                    final SimpleExpressionProxy guardExp = indexMap.getGuardExpressionAt(currGuard);
                                    if(states.length == 2){
                                        guard = guardExp.toString();
                                        break;
                                    }
                                    final String strGuard = "(" + guardExp.toString() + ")";
                                    if(guard.isEmpty()){
                                        guard = strGuard;
                                    } else {
                                        if(!guard.contains(guardExp.toString())) {
                                            guard += cot.getOrOperator().getName() + strGuard;
                                        }
                                    }
                                }
                            }
                        } catch(final Exception exc){}
                        try{
                            final int[] currActions = indexAutomata.getActionStateEventTable()[indexAutomaton][stFrom][event];
                            if(currActions.length > 1){
                                for(final int currAction : currActions){
                                    if(currAction == MAX_VALUE) {
                                        continue;
                                    }
                                    final BinaryExpressionProxy actionExp = indexMap.getActionExpressionAt(currAction);
                                    if(action.isEmpty()){
                                        action = actionExp.toString();
                                    } else {
                                        if(!action.contains(actionExp.toString())) {
                                            action += "; " + actionExp.toString();
                                        }
                                    }
                                }
                            }
                        } catch(final Exception exc){}
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
            qc = getQC();
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

    private THashSet<TIntHashSet> getQC(){
        final Stack<TIntHashSet> W = new Stack<TIntHashSet>(); // Set W (splitter) in WONG paper
        final THashSet<TIntHashSet> R = new THashSet<TIntHashSet>(); // Set Rho in WONG paper
        TIntHashSet phiB,inter,diff; // Sets of phiB, intersection, and difference

        final TIntHashSet Q = new TIntHashSet();

        final int nbrStates = indexAutomata.getAutomataSize()[indexAutomaton];
        for(int i=0; i < nbrStates; i++) {
            Q.add(i);
        }

        // Initiate the sets W and R with all states
        W.push(Q);
        R.add(Q);

        // New mark event (Tao_m) to handle marked states
        currSharedEvents.add(MARK_EVENT);
        while(!W.isEmpty()){
            final TIntHashSet B = W.pop(); // B: temporary partition
            for(final TIntIterator itr = currSharedEvents.iterator(); itr.hasNext();){
                final int e = itr.next();
                final THashSet<TIntHashSet> I = new THashSet<TIntHashSet>();
                final THashSet<TIntHashSet> I12 = new THashSet<TIntHashSet>();

                if(e == MARK_EVENT){
                    phiB = phiTAOm(B);
                } else {
                    phiB = phiTao(B,e);
                }
                if(phiB.isEmpty()) {
                    continue;
                }

                // X: A partition in Rho
                for (final TIntHashSet X : R) {
                    inter = ExtendedAutomataIndexFormHelper.setIntersection(X, phiB);
                    if (inter.isEmpty()) {
                        continue;
                    }
                    diff = ExtendedAutomataIndexFormHelper.setMinus(X, phiB);
                    if (diff.isEmpty()) {
                        continue;
                    }
                    I.add(X);
                    I12.add(inter);
                    I12.add(diff);
                }
                for(final TIntHashSet p : I){
                    R.remove(p);
                    W.remove(p);
                }

                for(final TIntHashSet p : I12){
                    R.add(p);
                    W.push(p);
                }
            }
        }
        currSharedEvents.remove(MARK_EVENT);
        return R;
    }
    
    private void getQuotient(){
        // Clear the old quotient model
        mapStateCoset.clear();
        mapStateStatus.clear();
        // Building up new quotient state space
        int newstate = 0;
        for(final TIntHashSet p : qc){
            boolean isInitial = false;
            boolean isAccepted = false;
            // Forbidden location are not considered in this implementation!
            final boolean isForbbiden = false;
            for(final TIntIterator itr = p.iterator(); itr.hasNext();){
                final int status = indexAutomata.getStateStatusTable()[indexAutomaton][itr.next()];
                if(!isInitial) {
                    isInitial = ExtendedAutomataIndexFormHelper.isInitial(status);
                }
                if(!isAccepted) {
                    isAccepted = ExtendedAutomataIndexFormHelper.isAccepting(status);
                }
            }
            final int status = ExtendedAutomataIndexFormHelper.createStatus(isInitial, isAccepted, isForbbiden);
            
            mapStateCoset.put(newstate, p);
            mapStateStatus.put(newstate, status);
            newstate++;
        }
        // <state> x <event> -> <state[]>
        quotient = new int[qc.size()][indexAutomata.getNbrUnionEvents()][];

        for(TIntObjectIterator<TIntHashSet> itrm = mapStateCoset.iterator(); itrm.hasNext();){
            itrm.advance();
            final int source = itrm.key();
            for(int event=0; event<indexAutomata.getNbrUnionEvents(); event++){
                quotient[source][event] = new int[]{MAX_VALUE};
                TIntHashSet sourceCoset = itrm.value();
                for(final TIntIterator itr = sourceCoset.iterator(); itr.hasNext();){
                    final int next = indexAutomata.getNextStateTable()[indexAutomaton][itr.next()][event];
                    if(next == MAX_VALUE) {
                        continue;
                    }
                    final int target = getQuotientState(next);
                    if(target == MAX_VALUE) {
                        throw new NullPointerException("getQuotient : finding quotient for the state <" + next + "> returns Null");
                    }
                    final int[] currQuoStates = quotient[source][event];
                    if(currSharedEvents.contains(event) || source != target){
                        quotient[source][event] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(target,currQuoStates);
                    }

                }
            }
        }
    }

    private boolean extendEvent(){
        boolean hasLocalEvent = false;
        // Enlargement from the local events in quotient EFA
        final TIntHashSet B = new TIntHashSet();
        for(int state = 0; state < quotient.length; state++){
            for(int event = 0; event < quotient[state].length; event++){
                final int[] nexts = quotient[state][event];
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
        final TIntHashSet N = getNdStates();

        // If it is nondeterministic then fix it by making local events to observable events
        if(!N.isEmpty()){
            // The local events hidden in the cosets
            final TIntHashSet H = new TIntHashSet();
            for(final TIntIterator itr = N.iterator(); itr.hasNext();){
                final TIntHashSet p = mapStateCoset.get(itr.next());
                final TIntHashSet hiddenEvents = getHiddenEvents(p);
                H.addAll(hiddenEvents.toArray());
            }

            // Add all events in B and H (B union H) to the set of shared events
            addAllSharedEvent(B);
            addAllSharedEvent(H);

            // The set of events which are in H but not in B
            final TIntHashSet H_B = ExtendedAutomataIndexFormHelper.setMinus(H, B);


            // Check each event
            for(final TIntIterator itr = H_B.iterator(); itr.hasNext();){
                int e = itr.next();
                addLocalEvent(e);
                for(final TIntIterator itr2 = N.iterator(); itr2.hasNext();){
                    if(!split(itr2.next())){
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
         * Second, if there is any guard or action, check for the possible critical states, i.e., those states which have 
         * two or more outgoing transitions with the same label and target state but augmented with different actions and in some 
         * cases guards
         */
        if(indexMap.hasAnyAction(indexAutomaton) || indexMap.hasAnyGuard(indexAutomaton)){
            for(int source = 0; source < quotient.length; source++){
                for(int event = 0; event < quotient[source].length; event++){
                    int[] targets = quotient[source][event];
                    // If the target states is one (+ MAX_VALUE) then it is ok otherwise check them.
                    // Note that the targets will the same since we do not have nondeterministic behaviour
                    if(targets.length <= 2) {
                        continue;
                    }
                    // Removing duplicated states
                    final TIntHashSet temp = new TIntHashSet();
                    for(final int target : targets){
                        if(target == MAX_VALUE) {
                            continue;
                        }
                        final boolean added = temp.add(target);
                        // If the target state is already in the set temp then it might be critical so check it
                        if(!added){
                            // Check for actions
                            final boolean checkActions = checkActions(source, event, target);
                            // Check for guards
                            final boolean checkGuards = checkGuards(source, event, target);
                            // If any of above checking fails then do as follows
                            if(!(checkActions && checkGuards)){
                                // Find the local events hidden in the cosets so by making them observable we may fix the problem 
                                // otherwise later split them
                                final TIntHashSet H = new TIntHashSet();
                                final TIntHashSet p = mapStateCoset.get(source);
                                final TIntHashSet hiddenEvents = getHiddenEvents(p);
                                H.addAll(hiddenEvents.toArray());
                                if(!H.isEmpty()){
                                    for(final TIntIterator itr = H.iterator(); itr.hasNext();){
                                        if(!nopath2(mapStateCoset.get(source), event, mapStateCoset.get(target))){
                                            // If no path exists then make it observable to fix the guard or action problem
                                            addSharedEvent(itr.next());
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
            final TIntHashSet sourceCoset = mapStateCoset.get(state);
            if(sourceCoset.size() > 1){
                boolean hasUnconEvent = false;
                final TIntIntHashMap map = new TIntIntHashMap();
                final int[][] eventState = quotient[state];
                for(int event = 0; event < eventState.length; event++){
                    if(uncontrollableEvents.contains(event)){
                        hasUnconEvent = true;
                        int[] states = eventState[event];
                        if(states.length == 1) {
                            continue;
                        }
                        // Put the state into the event-state map
                        map.put(event, states[0]);
                    }
                }
                if(hasUnconEvent){
                    final TIntStack stack = new TIntStack();
                    for(final int event : map.keys()){
                        final TIntHashSet targetCoset = mapStateCoset.get(map.get(event));
                        for(final TIntIterator itr = sourceCoset.iterator(); itr.hasNext();){
                            int source = itr.next();
                            if(source == MAX_VALUE) {
                                continue;
                            }
                            final int st = nextStateTable[source][event];
                            if(st == MAX_VALUE) {
                                continue;
                            }
                            if(targetCoset.contains(st)) {
                                stack.push(source);
                            }
                        }
                    }

                    final TIntHashSet visited = new TIntHashSet();
                    while(stack.size() > 0){
                        final int node = stack.pop();
                        if(!visited.add(node)) {
                            continue;
                        }
                        final int[][] preEventStates = indexAutomata.getPrevStatesTable()[indexAutomaton][node];
                        for(final TIntIterator itr = currAlphabet.iterator(); itr.hasNext();){
                            int event = itr.next();
                            if(currSharedEvents.contains(event) || indexAutomata.getEpsilonEventsTable()[event]) {
                                continue;
                            }

                            final int[] preStates = preEventStates[event];
                            if(preStates.length == 1) {
                                continue;
                            }

                            if(uncontrollableEvents.contains(event)){
                                for(final int preState : preStates) {
                                    if(preState != MAX_VALUE) {
                                        stack.push(preState);
                                    }
                                }
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
    private TIntHashSet findEquivalentStates(final int state, final boolean downstream){
        final TIntHashSet eqStates = new TIntHashSet();
        final TIntStack stk = new TIntStack();
        eqStates.add(state);
        stk.push(state);

        while(stk.size() > 0){
            final int currstate = stk.pop();
            if(downstream){
                for(final TIntIterator itr = currAlphabet.iterator(); itr.hasNext();){
                    int currEvent = itr.next();
                    if(currLocalEvents.contains(currEvent)){
                        final int st = nextStateTable[currstate][currEvent];
                        if(st == MAX_VALUE) {
                            continue;
                        }
                        final boolean isNew = eqStates.add(st);
                        if(isNew) {
                            stk.push(st);
                        }
                    }
                }
            } else {
                for(final TIntIterator itr = currAlphabet.iterator(); itr.hasNext();){
                    int currEvent = itr.next();
                    if(currLocalEvents.contains(currEvent)){
                        final int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                        if(preStates.length == 1) {
                            continue;
                        }
                        for(final int pre : preStates){
                            if(pre == MAX_VALUE) {
                                continue;
                            }
                            final boolean isNew = eqStates.add(pre);
                            if(isNew) {
                                stk.push(pre);
                            }
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
    private TIntHashSet findEquivalentStates(final TIntHashSet states, final boolean downstream){
        final TIntHashSet eqStates = new TIntHashSet();
        for(final TIntIterator itr = states.iterator();itr.hasNext();){
            final TIntHashSet eqsts = findEquivalentStates(itr.next(), downstream);
            eqStates.addAll(eqsts.toArray());
        }
        return eqStates;
    }

    private TIntHashSet findImdPreImg(final int state, final int event){
        final TIntHashSet eqStates = new TIntHashSet();
        final TIntHashSet imdImg = new TIntHashSet();
        final TIntStack stk = new TIntStack();

        eqStates.add(state);
        stk.push(state);
        while(stk.size() > 0){
            final int currstate = stk.pop();
            for(final TIntIterator itr = currAlphabet.iterator(); itr.hasNext();){
                final int currEvent = itr.next();
                final int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                if(preStates.length == 1) {
                    continue;
                }
                if(currLocalEvents.contains(currEvent)){
                    for(final int pre : preStates){
                        if(pre == MAX_VALUE) {
                            continue;
                        }
                        final boolean isNew = eqStates.add(pre);
                        if(isNew) {
                            stk.push(pre);
                        }
                    }
                } else if(currEvent == event){
                    for(final int preState : preStates){
                        if(preState != MAX_VALUE) {
                            imdImg.add(preState);
                        }
                    }
                }
            }
        }
        return imdImg;
    }

    private TIntHashSet findImdPreImg(final TIntHashSet states, final int event){
        final TIntHashSet imdImg = new TIntHashSet();
        for(final TIntIterator itr = states.iterator();itr.hasNext();) {
            imdImg.addAll(findImdPreImg(itr.next(), event).toArray());
        }

        return imdImg;
    }

    private TIntHashSet phiTao(final TIntHashSet b, final int e) {
        final TIntHashSet imdImg = findImdPreImg(b, e);
        final TIntHashSet phiB = findEquivalentStates(imdImg, false);
        return phiB;
    }

    private TIntHashSet phiTAOm(final TIntHashSet b) {
        final TIntHashSet phiB = new TIntHashSet();
        for(final TIntIterator itr = b.iterator(); itr.hasNext();){
            final int st = itr.next();
            final int status = indexAutomata.getStateStatusTable()[indexAutomaton][st];
            if(ExtendedAutomataIndexFormHelper.isAccepting(status)) {
                phiB.addAll(findEquivalentStates(st, false).toArray());
            }
        }
        return phiB;
    }

    private boolean split(final int ndQuoState) {
        boolean answer;
        final TIntHashSet ndQuoPartition = mapStateCoset.get(ndQuoState);
        final TIntHashSet ndEvents = new TIntHashSet();
        final int[][] eventState = quotient[ndQuoState];
        for(int i=0; i < eventState.length; i++){
            final int[] states = eventState[i];
            if(states.length == 1) {
                continue;
            }
            else if(states.length > 2) {
                ndEvents.add(i);
            }
        }

        for(final TIntIterator itr = ndEvents.iterator(); itr.hasNext();){
            final int ndEvent = itr.next();
            final int[] nextQuoStates = quotient[ndQuoState][ndEvent];
            final THashSet<TIntHashSet> nextPs = new THashSet<TIntHashSet>();
            for(final int nextQuoState : nextQuoStates){
                if(nextQuoState == MAX_VALUE) {
                    continue;
                }
                final TIntHashSet p = mapStateCoset.get(nextQuoState);
                nextPs.add(p);
            }
            answer = nopath(ndQuoPartition, ndEvent, nextPs);
            if(!answer) {
                return answer;
            }
        }
        return true;
    }

    private void split2(final int source) {
        final THashSet<TIntHashSet> newPs = new THashSet<TIntHashSet>();
        final THashSet<TIntHashSet> removePs = new THashSet<TIntHashSet>();
        for(int event = 0; event < quotient[source].length; event++){
            final int[] states = quotient[source][event];
            if(states.length <= 2) {
                continue;
            }
            final int target = states[0];
            final TIntHashSet sourceP = (TIntHashSet) mapStateCoset.get(source);
            final TIntHashSet targetP = (TIntHashSet) mapStateCoset.get(target);
            for(final TIntIterator itr = sourceP.iterator(); itr.hasNext();){
                final int st = itr.next();
                final int next = indexAutomata.getNextStateTable()[indexAutomaton][st][event];
                if(targetP.contains(next)){
                    final TIntHashSet stEq = findEquivalentStates(st, false);
                    newPs.add(stEq);
                }
            }
            removePs.add(sourceP);
        }
        qc.removeAll(removePs);
        qc.addAll(newPs);
        getQuotient();
    }

    private boolean nopath(final TIntHashSet y, final int ndEvent, final THashSet<TIntHashSet> ys) {
        final HashMap<TIntHashSet, TIntHashSet> yMap = new HashMap<TIntHashSet, TIntHashSet>();
        for(final TIntHashSet p : ys) {
            yMap.put(p, new TIntHashSet());
        }

        for(final TIntIterator itr = y.iterator(); itr.hasNext();){
            final int yNode = itr.next();
            final int st = nextStateTable[yNode][ndEvent];
            if(st == MAX_VALUE) {
                continue;
            }
            for(final TIntHashSet coset : yMap.keySet()){
                if(coset.contains(st)){
                    yMap.get(coset).add(yNode);
                }
            }
        }

        final ArrayList<TIntHashSet> Es = new ArrayList<TIntHashSet>();
        for(final TIntHashSet value : yMap.values()) {
            Es.add(value);
        }

        TIntHashSet Ei, Ej;
            for(int i=0;i<Es.size();i++){
                for(int j=i+1;j<Es.size();j++){
                    Ei = findEquivalentStates(Es.get(i), true);
                    Ej = Es.get(j);

                    if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty()) {
                    return false;
                }
                    Ei=Es.get(i);
                    Ej = findEquivalentStates(Es.get(j), true);
                    if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty()) {
                    return false;
                }
                }
            }
        return true;
    }

    private boolean nopath2(final TIntHashSet source, final int event, final TIntHashSet target) {
        final TIntHashSet states = new TIntHashSet();
        for(final TIntIterator itr = source.iterator(); itr.hasNext();){
            final int state = itr.next();
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
            if(next == MAX_VALUE || !target.contains(next)) {
                continue;
            }
            states.add(state);
        }
        TIntHashSet Ei, Ej;
        final int[] p = states.toArray();
        for(int i=0;i<p.length;i++){
            for(int j=i+1;j<p.length;j++){
                    Ei = findEquivalentStates(p[i], false);
                    Ej = findEquivalentStates(p[j], false);
                    if(!ExtendedAutomataIndexFormHelper.setIntersection(Ei, Ej).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private TIntHashSet getHiddenEvents(final TIntHashSet p) {
        final TIntHashSet hiddenEvents = new TIntHashSet();
        for(final TIntIterator itr = p.iterator(); itr.hasNext();){
            final int state = itr.next();
            final int[] activeEvents = indexAutomata.getOutgoingEventsTable()[indexAutomaton][state];
            for(final int event : activeEvents){
                if(event == MAX_VALUE) {
                    continue;
                }
                final int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
                if(currLocalEvents.contains(event) && p.contains(next) && !currEplsilon.contains(event)) {
                    hiddenEvents.add(event);
                }
            }
        }
        return hiddenEvents;
    }

    private TIntHashSet getNdStates() {
        final TIntHashSet ndStates = new TIntHashSet();
        for(int i=0; i < quotient.length; i++){
            for(int j=0; j < quotient[i].length; j++){
                // Removing duplicated states
                final TIntHashSet states = new TIntHashSet(quotient[i][j]);
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
                final int[] targets = quotient[source][event];
                if(targets.length <= 2) {
                    continue;
                }
                final TIntHashSet temp = new TIntHashSet();
                for(final int target : targets){
                    if(target == MAX_VALUE) {
                        continue;
                    }
                    final boolean added = temp.add(target);
                    // If the state is already in the set then it might be critical
                    if(!added){
                        final boolean checkAction = checkActions(source, event, target);
                        final boolean checkGuard = checkGuards(source, event, target);
                        if(!checkAction){
                            // For action split
                            split2(source);
                        } else if(!checkGuard){
                            final boolean nopath = nopath2(mapStateCoset.get(source), event, mapStateCoset.get(target));
                            // For guard if no path then split
                            if(nopath) {
                                split2(source);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkActions(final int quoFrom, final int event, final int quoTo) {
        final TIntHashSet fromP = mapStateCoset.get(quoFrom);
        final TIntHashSet toP = mapStateCoset.get(quoTo);
        final THashSet<String> set = new THashSet<String>();
        for(final TIntIterator itr = fromP.iterator(); itr.hasNext();){
            final int from = itr.next();
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];
            if(next == MAX_VALUE || !toP.contains(next)) {
                continue;
            }

            final int[] actions = indexAutomata.getActionStateEventTable()[indexAutomaton][from][event];
            if(actions == null || actions.length == 1) {
                continue;
            }

            String str = "";
            for(final int action : actions){
                if(action != MAX_VALUE) {
                    str += indexMap.getActionExpressionAt(action).toString();
                }
            }
            if(!str.isEmpty()) {
                set.add(str);
            }
        }

        if(set.size() > 1) {
            return false;
        }

        return true;
    }
    private boolean checkGuards(final int quoFrom, final int event, final int quoTo) {
        final TIntHashSet fromP = mapStateCoset.get(quoFrom);
        final TIntHashSet toP = mapStateCoset.get(quoTo);
        final THashSet<String> set = new THashSet<String>();
        for(final TIntIterator itr = fromP.iterator(); itr.hasNext();){
            final int from = itr.next();
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];

            if(next == MAX_VALUE || !toP.contains(next)) {
                continue;
            }

            final int[] guards = indexAutomata.getGuardStateEventTable()[indexAutomaton][from][event];
            if(guards == null || guards.length == 1) {
                continue;
            }

            String str = "";
            for(final int guard : guards){
                if(guard != MAX_VALUE) {
                    str += indexMap.getGuardExpressionAt(guard).toString();
                }
            }
            if(!str.isEmpty()) {
                set.add(str);
            }
        }

        if(set.size() > 1){
            final boolean noPath = nopath2(mapStateCoset.get(quoFrom), event, mapStateCoset.get(quoTo));
            // If there is no path then returns false since it is not a problem
            if(noPath) {
                return false;
            }
        }
        return true;
    }

    private String getStateName(final TIntHashSet coset) {
        String str = "{";
        for (final TIntIterator it = coset.iterator(); it.hasNext();) {
            final int st = it.next();
            final NodeProxy loc = indexMap.getLocationAt(indexAutomaton, st);
            if(it.hasNext()) {
                str += loc.getName() + ",";
            }
            else {
                str += loc.getName() + "}";
            }
        }
        return str;
    }

    private void addSharedEvent(final int event){
        currSharedEvents.add(event);
        currLocalEvents.remove(event);
    }

    private void addLocalEvent(final int event){
        currLocalEvents.add(event);
        currSharedEvents.remove(event);
    }

    private void addAllSharedEvent(final TIntHashSet events){
        currSharedEvents.addAll(events.toArray());
        currLocalEvents.removeAll(events.toArray());
    }

    private int getQuotientState(final int state) {
        for(TIntObjectIterator<TIntHashSet> itr = mapStateCoset.iterator(); itr.hasNext();){
            itr.advance();
            if(itr.value().contains(state)) {
                return itr.key();
            }
        }
        return MAX_VALUE;
    }

    /**
     * Returns the set of shared events in index form
     * @return The set of indexed shared events
     */    
    public THashSet<Integer> getSharedEvents(){
        final THashSet<Integer> shared = new THashSet<Integer>();
        for(final int e : currSharedEvents.toArray()) {
            shared.add(e);
        }
        return shared;
    }

    /**
     * Returns the set of local events in index form
     * @return The set of indexed local events
     */
    public THashSet<Integer> getLocalEvents(){
        final THashSet<Integer> local = new THashSet<Integer>();
        for(final int e : currLocalEvents.toArray()) {
            local.add(e);
        }
        return local;
    }
}


