
package org.supremica.automata.algorithms.TransitionProjection;

import gnu.trove.set.hash.TIntHashSet;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;
import org.supremica.util.Args;

/**
 * AutomataTransitionProjection class is the implementation of my Transition Projection abstraction method.
 * To reduce the computation complexity, the controller is synthesized on the model abstraction
 * of subsystems and the global model of the entire system is unnecessary. Sufficient conditions
 * such as E-observer and OCC are checked to guarantee the supervisor result in maximally permissive and
 * nonblocking control to the entire system.
 * For more elaboration on this method see http://publications.lib.chalmers.se/cpl/record/index.xsql?pubid=155706
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class AutomataTransitionProjection {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.createLogger(AutomataTransitionProjection.class);
    private final ExtendedAutomata exAutomata;
    private final ActionTimer TPTimer;
    private final ExtendedAutomataIndexForm indexAutomata;
    private final ExtendedAutomataIndexMap indexMap;
    private TIntHashSet localEvents;
    private TIntHashSet sharedEvents;
    private final int nbrAutomaton;
    private final TIntHashSet unionAlphabet;
    private final TIntHashSet epsilon;
    private final int MAX_VALUE = Integer.MAX_VALUE;

    /**
     * The constructor of the class.
     * @param exAutomata ExtendedAutomata containing the EFAs
     * 
     * @param automatic <code>true</code>: Automatic event selection <Br />
     *                  <code>false</code>: Manual selection. Initially local events set is empty and
     *                                      shared events set is the union alphabet. Set these sets
     *                                      using the corresponding methods. 

     */
    public AutomataTransitionProjection(final ExtendedAutomata exAutomata, final boolean automatic){
        Args.checkForNull(automatic);
        this.exAutomata = exAutomata;
        TPTimer = new ActionTimer();
        indexAutomata = new ExtendedAutomataIndexForm(exAutomata);
        indexMap = indexAutomata.getAutomataIndexMap();
        nbrAutomaton = exAutomata.size();
        unionAlphabet = new TIntHashSet();
        epsilon = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getEpsilonEventsTable());
        for(int event = 0; event < indexAutomata.getNbrUnionEvents(); event++) {
            unionAlphabet.add(event);
        }

        if(automatic){ // Automatically find the local events
            localEvents = getLocalEventSet(exAutomata);
            sharedEvents = ExtendedAutomataIndexFormHelper.setMinus(unionAlphabet, localEvents);
        } else { // Manually setting the local / shared events
            localEvents = new TIntHashSet();
            sharedEvents = new TIntHashSet(unionAlphabet.toArray());
        }
    }

    /**
     * Returns the projected EFA
     * @param name The name of EFA to be projected
     * @return The projected EFA
     */
    public ExtendedAutomaton projectEFA(final String name){
        final ExtendedAutomaton efa = exAutomata.getExtendedAutomaton(name);
        Args.checkForNull(efa);
        final int exAutomatonIndex = indexMap.getExtendedAutomatonIndex(efa);
        final TIntHashSet currAlphabet = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getAlphabetEventsTable()[exAutomatonIndex]);
        final TIntHashSet currLocalEvents = ExtendedAutomataIndexFormHelper.setIntersection(localEvents, currAlphabet);
        final AutomatonTransitionProjection tp = new AutomatonTransitionProjection(indexAutomata, exAutomatonIndex, currLocalEvents.toArray());
        TPTimer.reset();
        TPTimer.start();
        final ExtendedAutomaton prjEFA = tp.getProjectedEFA();
        TPTimer.stop();
        return prjEFA;
    }

    /**
     * Automatically calculates the set of local events in which each event:
     * <ul>
     * <li>Only appears in one EFA,</li>
     * <li>Any transition labeled by this event has no guard or the guard is a tautology,</li>
     * <li>Execution of any action on transitions labeled by this event has no effect on any guards evaluation.</li>
     * </ul>
     * Note that if an event is unobservable it is automatically local.
     *
     * @param exAutomata The Extended Automata
     * @return Set of local events
     */
    private TIntHashSet getLocalEventSet(final ExtendedAutomata exAutomata){
        final TIntHashSet locEvents = new TIntHashSet();
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        final boolean[][] alphabetTable = indexAutomata.getAlphabetEventsTable();

        // Make a string of all guards
        String strAllGuards = "";
        for(final int[][] gss : indexAutomata.getEventGuardTable()){
            for(final int[] gs : gss){
                if(gs.length == 1) {
                    continue;
                }
                for(final int g : gs){
                    if(g != MAX_VALUE) {
                        strAllGuards += " " + indexMap.getGuardExpressionAt(g).toString();
                    }
                }
            }
        }

        TIntHashSet temp, sigmai, sigmaj;
        for(int currAutomaton=0; currAutomaton < nbrAutomaton; currAutomaton++){
            temp = new TIntHashSet();
            if(nbrAutomaton > 1){
                sigmai = ExtendedAutomataIndexFormHelper.getTrueIndexes(alphabetTable[currAutomaton]);
                sigmai = ExtendedAutomataIndexFormHelper.setMinus(sigmai, epsilon);
                sigmaj = new TIntHashSet();

                // Preliminary set of local events (DFA local events) just by checking the events
                for(int otherAutomaton=0; otherAutomaton<nbrAutomaton; otherAutomaton++){
                    if(otherAutomaton!=currAutomaton) {
                        sigmaj = ExtendedAutomataIndexFormHelper.setUnion(sigmaj, ExtendedAutomataIndexFormHelper.getTrueIndexes(alphabetTable[otherAutomaton]));
                    }
                }
                sigmaj = ExtendedAutomataIndexFormHelper.setMinus(sigmaj, epsilon);
                temp.addAll(ExtendedAutomataIndexFormHelper.setMinus(sigmai, sigmaj).toArray());
            } else {
                // All are local
                temp.addAll(ExtendedAutomataIndexFormHelper.getTrueIndexes(alphabetTable[currAutomaton]).toArray());
            }
            // Checking the local events conditions based on my paper (for EFAs)
            if(indexMap.hasAnyGuard(currAutomaton) || indexMap.hasAnyAction(currAutomaton)){
                outerloop:
                for(final int currEvent : temp.toArray()){
                    final int[] guards = indexAutomata.getEventGuardTable()[currAutomaton][currEvent];
                    // If there is any guard the nexclude this event from the set of local events
                    if(guards.length > 1) {
                        continue outerloop;
                    }

                    int[] actions = indexAutomata.getEventActionTable()[currAutomaton][currEvent];
                    // If no guards and actions then its locals
                    if(actions.length == 1){
                        locEvents.add(currEvent);
                        continue outerloop;
                    }

                    for(final int action : actions){
                        if(action == MAX_VALUE) {
                            continue;
                        }
                        final BinaryExpressionProxy actionExp = indexMap.getActionExpressionAt(action);
                        final Set<VariableComponentProxy> actionRightVars = exAutomata.extractVariablesFromExpr(actionExp.getRight());
                        // If the action is in the form of, e.g., x += 1, x-=1, or x = y + 1 the it is not local
                        if(actionExp.getOperator().equals(cot.getIncrementOperator()) || actionExp.getOperator().equals(cot.getDecrementOperator()) || !actionRightVars.isEmpty()){
                            continue outerloop;
                        }

                        // If the left side avariable appears in any guard then it is not local
                        final Set<VariableComponentProxy> actionLeftVars = exAutomata.extractVariablesFromExpr(actionExp.getLeft());
                        for(final VariableComponentProxy var : actionLeftVars){
                            if(strAllGuards.contains(var.getName())){
                                continue outerloop;
                            }
                        }
                    }
                    locEvents.add(currEvent);
                }
            } else {
                locEvents.addAll(temp.toArray());
            }
        }
        return locEvents;
    }

    /**
     * Returns the time that takes to calculate the projected EFA
     * @return Elapsed time in milliseconds
     */
    public long getElapsedTime(){
        return TPTimer.elapsedTime();
    }

    /**
     * Return the string form of the current timer
     * @return Current timer in string
     */
    public String getTimer(){
        return TPTimer.toString();
    }

    /**
     * Setting the set of shared events. This will overwrite the current set of shared events
     * @param events Set of shared events
     */
    public void setSharedEvents(final HashSet<EventDeclProxy> events) {
        sharedEvents.clear();
        localEvents.clear();
        for(final EventDeclProxy event : events) {
            sharedEvents.add(indexMap.getEventIndex(event));
        }
        localEvents = ExtendedAutomataIndexFormHelper.setMinus(unionAlphabet, sharedEvents);
    }

    /**
     * Setting the set of local events. This will overwrite the current set of local events
     * @param events Set of local events
     */
    public void setLocalEvents(final HashSet<EventDeclProxy> events) {
        sharedEvents.clear();
        localEvents.clear();
        for(final EventDeclProxy event : events) {
            localEvents.add(indexMap.getEventIndex(event));
        }
        sharedEvents.addAll(ExtendedAutomataIndexFormHelper.setMinus(unionAlphabet, localEvents).toArray());
    }

    /**
     * Return the index form of the <code>exAutomata</code>
     * @return Index form of the given Extended Automata
     */
    public ExtendedAutomataIndexForm getIndexForm(){
        return indexAutomata;
    }

    /**
     * Return the index map of the <code>exAutomata</code>
     * @return Index map of the given Extended Automata
     */
    public ExtendedAutomataIndexMap getIndexMap(){
        return indexMap;
    }

}

