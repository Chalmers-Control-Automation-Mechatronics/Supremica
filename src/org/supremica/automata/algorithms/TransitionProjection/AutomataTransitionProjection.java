
package org.supremica.automata.algorithms.TransitionProjection;

import gnu.trove.TIntHashSet;
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
 * AutomataTransitionProjection class is the implementation of my transition projection method. 
 * To reduce the computation complexity, the controller is synthesized on the model abstraction 
 * of subsystems and the global model of the entire system is unnecessary. Sufficient conditions 
 * are checked to guarantee the supervisors result in maximally permissive and 
 * nonblocking control to the entire system.
 * See more in http://publications.lib.chalmers.se/cpl/record/index.xsql?pubid=155706
 * 
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class AutomataTransitionProjection {
    
    private final Logger logger = LoggerFactory.createLogger(AutomataTransitionProjection.class);        
    private final ExtendedAutomata exAutomata;
    private final ActionTimer TPTimer;
    private ExtendedAutomataIndexForm indexAutomata;
    private final ExtendedAutomataIndexMap indexMap;
    private TIntHashSet localEvents;
    private TIntHashSet sharedEvents;
    private final int nbrAutomaton;
    private final TIntHashSet unionAlphabet;
    
    /**
     * The constructor of the class.
     * @param automatic <code>true</code>: Automatic event selection <code>false</code>: Manual selection
     * @param exAutomata ExtendedAutomata containing the EFAs
     */
    public AutomataTransitionProjection(ExtendedAutomata exAutomata, boolean automatic){
        Args.checkForNull(automatic);
        this.exAutomata = exAutomata;
        TPTimer = new ActionTimer();     
        indexAutomata = new ExtendedAutomataIndexForm(exAutomata);
        indexMap = indexAutomata.getAutomataIndexMap();
        nbrAutomaton = exAutomata.size();
        unionAlphabet = new TIntHashSet();
        for(int event = 0; event < indexAutomata.getNbrUnionEvents(); event++)
            unionAlphabet.add(event);
        if(automatic){
            localEvents = getLocalEventSet(exAutomata);
            sharedEvents = ExtendedAutomataIndexFormHelper.setDifference(unionAlphabet, localEvents);
        } else {
            localEvents = new TIntHashSet();
            sharedEvents = new TIntHashSet();
        }
    }
    
    /**
     * Returns the projected EFA
     * @param name The name of EFA to be projected
     * @return The projected EFA
     */
    public ExtendedAutomaton projectEFA(String name){
        ExtendedAutomaton efa = exAutomata.getExtendedAutomaton(name);
        Args.checkForNull(efa);
        int exAutomatonIndex = indexMap.getExtendedAutomatonIndex(efa);
        TIntHashSet currAlphabet = ExtendedAutomataIndexFormHelper.getTrueIndexes(indexAutomata.getAlphabetEventsTable()[exAutomatonIndex]);
        TIntHashSet currLocalEvents = ExtendedAutomataIndexFormHelper.setIntersection(localEvents, currAlphabet);
        AutomatonTransitionProjection tp = new AutomatonTransitionProjection(indexAutomata, exAutomatonIndex, currLocalEvents.toArray());
        TPTimer.reset();
        TPTimer.start();
        ExtendedAutomaton prjEFA = tp.getProjectedEFA();
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
    private TIntHashSet getLocalEventSet(ExtendedAutomata exAutomata){
        TIntHashSet locEvents = new TIntHashSet();
        CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        boolean[][] alphabetTable = indexAutomata.getAlphabetEventsTable();
        // For one model locals are unobservable evetns that will be checked by equivalency algorithm
        if(indexAutomata.getNbrAutomaton() == 1)
            return locEvents;
        
        // Make a string of all guards
        String strAllGuards = "";
        for(int[][] gss : indexAutomata.getEventGuardTable()){
            for(int[] gs : gss){
                if(gs.length == 1)
                    continue;
                gs = ExtendedAutomataIndexFormHelper.clearMaxInteger(gs);
                for(int g : gs){
                    strAllGuards += indexMap.getExpressionAt(g).toString();
                }
            }
        }
            
        TIntHashSet temp, sigmai, sigmaj;
        for(int currAutomaton=0; currAutomaton < nbrAutomaton; currAutomaton++){
            sigmai = ExtendedAutomataIndexFormHelper.getTrueIndexes(alphabetTable[currAutomaton]);
            sigmaj = new TIntHashSet();
            temp = new TIntHashSet();
            // Preliminary set of local events (DFA local events) just by checking the events
            for(int otherAutomaton=0; otherAutomaton<nbrAutomaton; otherAutomaton++){
                if(otherAutomaton!=currAutomaton)
                    sigmaj = ExtendedAutomataIndexFormHelper.setUnion(sigmaj, ExtendedAutomataIndexFormHelper.getTrueIndexes(alphabetTable[otherAutomaton]));
            }   
            temp.addAll(ExtendedAutomataIndexFormHelper.setDifference(sigmai, sigmaj).toArray());

            // Checking the local events conditions based on my paper (for EFAs)
            if(!indexMap.hasAnyGuard(currAutomaton) && !indexMap.hasAnyAction(currAutomaton)){
                locEvents.addAll(temp.toArray());
                return locEvents;
            }
                
            outerloop:
            for(int currEvent : temp.toArray()){
                int[] guards = indexAutomata.getEventGuardTable()[currAutomaton][currEvent];
                // If there is any guard the nexclude this event from the set of local events
                if(guards.length > 1){
                    continue outerloop;
                }
                
                int[] actions = indexAutomata.getEventActionTable()[currAutomaton][currEvent];
                // If no guards and actions then its locals
                if(actions.length == 1){
                    locEvents.add(currEvent);
                    continue outerloop;
                }
                
                actions = ExtendedAutomataIndexFormHelper.clearMaxInteger(actions);
                for(int action : actions){
                    BinaryExpressionProxy actionExp = (BinaryExpressionProxy) indexMap.getExpressionAt(action);
                    Set<VariableComponentProxy> actionRightVars = exAutomata.extractVariablesFromExpr(actionExp.getRight());
                    // If the action is in the form of, e.g., x += 1, x-=1, or x = y + 1 the it is not local  
                    if(actionExp.getOperator().equals(cot.getIncrementOperator()) || actionExp.getOperator().equals(cot.getDecrementOperator()) || !actionRightVars.isEmpty()){
                        continue outerloop;
                    }
                    
                    // If the left side avariable appears in any guard then it is not local
                    Set<VariableComponentProxy> actionLeftVars = exAutomata.extractVariablesFromExpr(actionExp.getLeft());
                    for(VariableComponentProxy var : actionLeftVars){
                        if(strAllGuards.contains(var.getName())){
                            continue outerloop;
                        }
                    }
                }
                locEvents.add(currEvent);
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

    public String getTimer(){
        return TPTimer.toString();
    }

    /**
     * Setting the set of shared events. This will overwrite the current set of shared events
     * @param shareEvents Set of shred events
     */
    public void setSharedEvents(HashSet<EventDeclProxy> shareEvents) {
        for(EventDeclProxy event : shareEvents)
            sharedEvents.add(indexMap.getEventIndex(event));
        localEvents = ExtendedAutomataIndexFormHelper.setDifference(unionAlphabet, sharedEvents);
    }

    /**
     * Setting the set of local events. This will overwrite the current set of local events 
     * @param locEvents Set of local events
     */
    public void setLocalEvents(HashSet<EventDeclProxy> locEvents) {
        for(EventDeclProxy event : locEvents)
            localEvents.add(indexMap.getEventIndex(event));
        
        sharedEvents.addAll(ExtendedAutomataIndexFormHelper.setDifference(unionAlphabet, localEvents).toArray());
    }

}
