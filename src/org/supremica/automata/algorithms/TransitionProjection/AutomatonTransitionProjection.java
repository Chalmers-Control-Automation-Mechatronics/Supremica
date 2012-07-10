
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
    private final TIntHashSet currEplsilon;
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
        ps = null;
        quotient = null;
    }

    public ExtendedAutomaton getProjectedEFA(){
        final ExtendedAutomaton originalEFA = indexMap.getExtendedAutomatonAt(indexAutomaton);
        final ExtendedAutomaton prjEFA = new ExtendedAutomaton(originalEFA.getName(), originalEFA.getKind());
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();

        project();

        for(int from = 0; from < quotient.length; from++){
            final Partition pFrom = getPartition(from);
            final int statusFrom = pFrom.getStatus();
            final NodeProxy currSource = prjEFA.addState(getStateName(pFrom.getCoset()),
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

                final Partition pTo = getPartition(to);
                final int statusTo = pTo.getStatus();
                final NodeProxy currTarget = prjEFA.addState(getStateName(pTo.getCoset()),
                                            ExtendedAutomataIndexFormHelper.isAccepting(statusTo),
                                            ExtendedAutomataIndexFormHelper.isInitial(statusTo),
                                            false);
                // If there is any guard then do as follows
                String guard = "";
                String action = "";
                if(indexMap.hasAnyGuard(indexAutomaton) || indexMap.hasAnyAction(indexAutomaton)){
                    for(final int stFrom : pFrom.getCoset().toArray()){
                        try{
                            int[] currGuards = indexAutomata.getGuardStateEventTable()[indexAutomaton][stFrom][event];
                            if(currGuards.length > 1){
                                currGuards = ExtendedAutomataIndexFormHelper.clearMaxInteger(currGuards);
                                for(final int currGuard : currGuards){
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
                        } catch(Exception exc){}
                        try{
                            final int[] currActions = indexAutomata.getActionStateEventTable()[indexAutomaton][stFrom][event];
                            if(currActions.length > 1){
                                for(final int currAction : currActions){
                                    if(currAction == Integer.MAX_VALUE) {
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
        final Stack<Partition> W = new Stack<Partition>(); // Set W (splitter) in WONG paper
        final HashSet<Partition> R = new HashSet<Partition>(); // Set Rho in WONG paper
        TIntHashSet phiB,inter,diff; // Sets of phiB, intersection, and difference

        final TIntHashSet Q = new TIntHashSet();

        final int nbrStates = indexAutomata.getAutomataSize()[indexAutomaton];
        for(int i=0; i < nbrStates; i++) {
            Q.add(i);
        }

        // Initiate the sets W and R with all states
        W.push(new Partition(Q));
        R.add(new Partition(Q));

        // New mark event (Tao_m) to handle marked states
        currSharedEvents.add(MARK_EVENT);
        while(!W.isEmpty()){
            final Partition B = W.pop(); // B: temporary partition
            for(final int e : currSharedEvents.toArray()){
                final HashSet<Partition> I = new HashSet<Partition>();
                final HashSet<Partition> I12 = new HashSet<Partition>();

                if(e == MARK_EVENT){
                    phiB = phiTAOm(B.getCoset());
                } else {
                    phiB = phiTao(B.getCoset(),e);
                }
                if(phiB.isEmpty()) {
                    continue;
                }

                // X: A partition in Rho
                for (final Partition X : R) {
                    inter = ExtendedAutomataIndexFormHelper.setIntersection(X.getCoset(), phiB);
                    if (inter.isEmpty()) {
                        continue;
                    }
                    diff = ExtendedAutomataIndexFormHelper.setMinus(X.getCoset(), phiB);
                    if (diff.isEmpty()) {
                        continue;
                    }
                    I.add(X);
                    I12.add(new Partition(inter));
                    I12.add(new Partition(diff));
                }
                for(final Partition p : I){
                    R.remove(p);
                    W.remove(p);
                }

                for(final Partition p : I12){
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
        for(final Partition p : ps){
            boolean isInitial = false;
            boolean isAccepted = false;
            final boolean isForbbiden = false;
            for(final int st : p.getCoset().toArray()){
                final int status = indexAutomata.getStateStatusTable()[indexAutomaton][st];
                if(!isInitial) {
                    isInitial = ExtendedAutomataIndexFormHelper.isInitial(status);
                }
                if(!isAccepted) {
                    isAccepted = ExtendedAutomataIndexFormHelper.isAccepting(status);
                }
                // Forbidden location are not considered in this implementation!
//                if(!isForbbiden)
//                    isForbbiden = ExtendedAutomataIndexFormHelper.isForbidden(status);
            }
            final int status = ExtendedAutomataIndexFormHelper.createStatus(isInitial, isAccepted, isForbbiden);
            p.setState(newstate++, status);
        }
        // <state> x <event> -> <state[]>
        quotient = new int[ps.size()][indexAutomata.getNbrUnionEvents()][];

        for(final Partition p : ps){
            final int source = p.getState();
            if(source == Integer.MAX_VALUE) {
                throw new NullPointerException("getQuotient : partition returns Null quotiont state");
            }
            for(int event=0; event<indexAutomata.getNbrUnionEvents(); event++){
                quotient[source][event] = new int[]{Integer.MAX_VALUE};
                for(final int currState : p.getCoset().toArray()){
                    final int next = indexAutomata.getNextStateTable()[indexAutomaton][currState][event];
                    if(next == Integer.MAX_VALUE) {
                        continue;
                    }
                    final int target = getQuotientState(next, ps);
                    if(target == Integer.MAX_VALUE) {
                        throw new NullPointerException("getQuotient : finding quotient for the state <" + next + "> returns Null");
                    }
                    final int[] currQuotientStates = quotient[source][event];
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
            for(final int ndState : N.toArray()){
                final Partition p = getPartition(ndState);
                final TIntHashSet hiddenEvents = getHiddenEvents(p);
                H.addAll(hiddenEvents.toArray());
            }

            // Add all events in B and H (B union H) to the set of shared events
            addAllSharedEvent(B);
            addAllSharedEvent(H);

            // The set of events which are in H but not in B
            final TIntHashSet H_B = ExtendedAutomataIndexFormHelper.setMinus(H, B);


            // Check each event
            for(final int e : H_B.toArray()){
                addLocalEvent(e);
                for(final int y : N.toArray()){
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
                    if(targets.length <= 2) {
                        continue;
                    }
                    targets = ExtendedAutomataIndexFormHelper.clearMaxInteger(targets);
                    final TIntHashSet temp = new TIntHashSet();
                    for(final int target : targets){
                        final boolean added = temp.add(target);
                        // If the state is already in the set then it might be critical
                        if(!added){
                            final boolean checkActions = checkActions(source, event, target);
                            final boolean checkGuards = checkGuards(source, event, target);
                            if(!(checkActions && checkGuards)){
                                // Find the local events hidden in the cosets so by making them observable we may fix the problem otherwise later split them
                                final TIntHashSet H = new TIntHashSet();
                                final Partition p = getPartition(source);
                                final TIntHashSet hiddenEvents = getHiddenEvents(p);
                                H.addAll(hiddenEvents.toArray());
                                if(!H.isEmpty()){
                                    for(final int e : H.toArray()){
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
            final TIntHashSet sourceCoset = getPartition(state).getCoset();
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
                        states = ExtendedAutomataIndexFormHelper.clearMaxInteger(states);
                        map.put(event, states[0]);
                    }
                }
                if(hasUnconEvent){
                    final TIntStack stack = new TIntStack();
                    for(final int event : map.keys()){
                        final TIntHashSet targetCoset = getPartition(map.get(event)).getCoset();
                        for(final int source : sourceCoset.toArray()){
                            if(source == Integer.MAX_VALUE) {
                                continue;
                            }
                            final int st = nextStateTable[source][event];
                            if(st == Integer.MAX_VALUE) {
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
                        for(final int event : currAlphabet.toArray()){
                            if(currSharedEvents.contains(event) || indexAutomata.getEpsilonEventsTable()[event]) {
                                continue;
                            }

                            final int[] preStates = preEventStates[event];
                            if(preStates.length == 1) {
                                continue;
                            }

                            if(uncontrollableEvents.contains(event)){
                                for(final int preState : preStates) {
                                    if(preState != Integer.MAX_VALUE) {
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
                for(final int currEvent : currAlphabet.toArray()){
                    if(currLocalEvents.contains(currEvent)){
                        final int st = nextStateTable[currstate][currEvent];
                        if(st == Integer.MAX_VALUE) {
                            continue;
                        }
                        final boolean isNew = eqStates.add(st);
                        if(isNew) {
                            stk.push(st);
                        }
                    }
                }
            } else {
                for(final int currEvent : currAlphabet.toArray()){
                    if(currLocalEvents.contains(currEvent)){
                        final int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                        if(preStates.length == 1) {
                            continue;
                        }
                        for(final int pre : preStates){
                            if(pre == Integer.MAX_VALUE) {
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
        for(TIntIterator itr = states.iterator();itr.hasNext();){
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
            for(final int currEvent : currAlphabet.toArray()){
                final int[] preStates = indexAutomata.getPrevStatesTable()[indexAutomaton][currstate][currEvent];
                if(preStates.length == 1) {
                    continue;
                }
                if(currLocalEvents.contains(currEvent)){
                    for(final int pre : preStates){
                        if(pre == Integer.MAX_VALUE) {
                            continue;
                        }
                        final boolean isNew = eqStates.add(pre);
                        if(isNew) {
                            stk.push(pre);
                        }
                    }
                } else if(currEvent == event){
                    for(final int preState : preStates){
                        if(preState != Integer.MAX_VALUE) {
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
        for(TIntIterator itr = states.iterator();itr.hasNext();) {
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
        for(TIntIterator itr = b.iterator(); itr.hasNext();){
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
        final Partition ndQuoPartition = getPartition(ndQuoState);
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

        for(final int ndEvent : ndEvents.toArray()){
            int[] nextQuoStates = quotient[ndQuoState][ndEvent];
            nextQuoStates = ExtendedAutomataIndexFormHelper.clearMaxInteger(nextQuoStates);
            final HashSet<Partition> nextPs = new HashSet<Partition>();
            for(final int nextQuoState : nextQuoStates){
                final Partition p = getPartition(nextQuoState);
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
        final HashSet<Partition> newPs = new HashSet<Partition>();
        final HashSet<Partition> removePs = new HashSet<Partition>();
        for(int event = 0; event < quotient[source].length; event++){
            final int[] states = quotient[source][event];
            if(states.length <= 2) {
                continue;
            }
            final int target = states[0];
            final Partition sourceP = getPartition(source);
            final Partition targetP = getPartition(target);
            for(final int st : sourceP.getCoset().toArray()){
                final int next = indexAutomata.getNextStateTable()[indexAutomaton][st][event];
                if(targetP.getCoset().contains(next)){
                    final TIntHashSet stEq = findEquivalentStates(st, false);
                    newPs.add(new Partition(stEq));
                }
            }
            removePs.add(sourceP);
        }
        ps.removeAll(removePs);
        ps.addAll(newPs);
        getQuotient();
    }

    private boolean nopath(final Partition y, final int ndEvent, final HashSet<Partition> ys) {
        final HashMap<Partition, TIntHashSet> yMap = new HashMap<Partition, TIntHashSet>();
        for(final Partition p:ys) {
            yMap.put(p, new TIntHashSet());
        }

        for(final int yNode : y.getCoset().toArray()){
            final int st = nextStateTable[yNode][ndEvent];

            if(st == Integer.MAX_VALUE) {
                continue;
            }

            for(final Partition p:yMap.keySet()){
                final TIntHashSet coset = p.getCoset();
                if(coset.contains(st)){
                    yMap.get(p).add(yNode);
                }
            }
        }

        final ArrayList<TIntHashSet> Es = new ArrayList<TIntHashSet>();
        for(final TIntHashSet value:yMap.values()) {
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

    private boolean nopath2(final Partition source, final int event, final Partition target) {
        final TIntHashSet states = new TIntHashSet();
        for(final int state : source.getCoset().toArray()){
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
            if(next == Integer.MAX_VALUE || !target.getCoset().contains(next)) {
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

    private TIntHashSet getHiddenEvents(final Partition p) {
        final TIntHashSet hiddenEvents = new TIntHashSet();
        for(final int state : p.getCoset().toArray()){
            int[] activeEvents = indexAutomata.getOutgoingEventsTable()[indexAutomaton][state];
            activeEvents = ExtendedAutomataIndexFormHelper.clearMaxInteger(activeEvents);
            for(final int event : activeEvents){
                final int next = indexAutomata.getNextStateTable()[indexAutomaton][state][event];
                if(currLocalEvents.contains(event) && p.getCoset().contains(next) && !currEplsilon.contains(event)) {
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
                    if(target == Integer.MAX_VALUE) {
                        continue;
                    }
                    final boolean added = temp.add(target);
                    // If the state is already in the set then it might be critical
                    if(!added){
                        final boolean checkAction = checkActions(source, event, target);
                        final boolean checkGuard = checkGuards(source, event, target);
                        if(!(checkAction && checkGuard)){
                            final boolean nopath = nopath2(getPartition(source), event, getPartition(target));
                            // No path then split
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
        final Partition fromP = getPartition(quoFrom);
        final Partition toP = getPartition(quoTo);
        final HashSet<String> set = new HashSet<String>();
        for(final int from : fromP.getCoset().toArray()){
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];
            if(next == Integer.MAX_VALUE || !toP.getCoset().contains(next)) {
                continue;
            }

            final int[] actions = indexAutomata.getActionStateEventTable()[indexAutomaton][from][event];
            if(actions == null || actions.length == 1) {
                continue;
            }

            String str = "";
            for(final int action : actions){
                if(action != Integer.MAX_VALUE) {
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
        final Partition fromP = getPartition(quoFrom);
        final Partition toP = getPartition(quoTo);
        final HashSet<String> set = new HashSet<String>();
        for(final int from : fromP.getCoset().toArray()){
            final int next = indexAutomata.getNextStateTable()[indexAutomaton][from][event];

            if(next == Integer.MAX_VALUE || !toP.getCoset().contains(next)) {
                continue;
            }

            final int[] guards = indexAutomata.getGuardStateEventTable()[indexAutomaton][from][event];
            if(guards == null || guards.length == 1) {
                continue;
            }

            String str = "";
            for(final int guard : guards){
                if(guard != Integer.MAX_VALUE) {
                    str += indexMap.getGuardExpressionAt(guard).toString();
                }
            }
            if(!str.isEmpty()) {
                set.add(str);
            }
        }

        if(set.size() > 1){
            final boolean noPath = nopath2(getPartition(quoFrom), event, getPartition(quoTo));
            // If there is no path then returns false since it is not a problem
            if(noPath) {
                return false;
            }
        }
        return true;
    }

    private Partition getPartition(final int state){
        for(final Partition p:ps) {
            if(p.getState() == state) {
                return p;
            }
        }
        return null;
    }

    private String getStateName(final TIntHashSet coset) {
        String str = "{";
        for (TIntIterator it = coset.iterator(); it.hasNext();) {
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

    private int getQuotientState(final int state, final HashSet<Partition> ps) {
        for(final Partition p : ps){
            for(final int st : p.getCoset().toArray()){
                if(state == st){
                    return p.getState();
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public HashSet<Integer> getSharedEvents(){
        final HashSet<Integer> shared = new HashSet<Integer>();
        for(final int e : currSharedEvents.toArray()) {
            shared.add(e);
        }
        return shared;
    }

    public HashSet<Integer> getLocalEvents(){
        final HashSet<Integer> local = new HashSet<Integer>();
        for(final int e : currLocalEvents.toArray()) {
            local.add(e);
        }
        return local;
    }

    class Partition{
        private final TIntHashSet coset;
        private int state;
        private int status;

        Partition(){
            coset = new TIntHashSet();
            state = Integer.MAX_VALUE;
            status = Integer.MAX_VALUE;
        }

        Partition(final TIntHashSet coset){
            this.coset = coset;
            state = Integer.MAX_VALUE;
            status = Integer.MAX_VALUE;
        }

        public TIntHashSet getCoset(){
            return coset;
        }

        public void setState(final int state, final int status){
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


