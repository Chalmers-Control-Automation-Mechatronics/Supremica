
package org.supremica.automata.algorithms.TransitionProjection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * ExtendedAutomataIndexForm class is the index form of EFAs which allows the algorithms run in array mode. This class is an adopted version of AutomataIndexForm to support Extended Finite Automata.
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class ExtendedAutomataIndexForm {
    
    // <automaton> x <event> -> <true|false>
    private boolean[][] alphabetEventsTable;

    // <automaton> x <state> -> <event[]>
    private int[][][] outgoingEventsTable;

    // <automaton> x <state> -> <event[]>
    private int[][][] incomingEventsTable;

    // <automaton> x <state> x <event> -> <state>
    private int[][][] nextStateTable;

    // <automaton> x <state> x <event> -> <state[]>
    private int[][][][] nextStatesTable;

    // <automaton> x <state> x <event> -> <state[]>
    private int[][][][] prevStatesTable;

    // <automaton> x <state> -> <State>
    private NodeProxy[][] stateTable;

    // <automaton> x <state> -> <status>
    private int[][] stateStatusTable;

    // <event> -> <true|false>
    private boolean[] controllableEventsTable;

    // <event> -> <true|false>
    private boolean[] uncontrollableEventsTable;
    
    // <event> -> <true|false>
    private boolean[] epsilonEventsTable;

    // <automaton> -> <isPlant>
    private boolean[] typeIsPlantTable;

    // <automaton> -> <isSupervisor||isSpecification>
    private boolean[] typeIsSupSpecTable;

    // <automaton> -> <nbr_of_states>
    private int[] automataSize;

    // <automaton> x <event> -> <state[]>
    private int[][][] enableEventsTable;

    // <event> -> <automaton[]>
    private int[][] eventToAutomatonTable;

    // <automaton> -> <max state index in current automaton>
    private int[] automatonStateMaxIndex;
    
    // <automaton> x <state> x <event> -> <Guard[]>
    private int[][][][] guardStateEventTable;
    
    // <automaton> x <state> x <event> -> <Action[]>
    private int[][][][] actionStateEventTable;
    
    // <automaton> x <event> -> <Guard[]>
    private int[][][] eventGuradTable;
    
    // <automaton> x <event> -> <Action[]>
    private int[][][] eventActionTable;
    
    // Max value 
    private final int MAX_VALUE = Integer.MAX_VALUE;    
    
    private final Logger logger = LoggerFactory.createLogger(ExtendedAutomataIndexFormHelper.class);
    private final ExtendedAutomataIndexMap indexMap;
    private final int nbrAutomaton;
    private final int nbrUnionEvents;
    
    public ExtendedAutomataIndexForm(final ExtendedAutomata exAutomata){
        indexMap = new ExtendedAutomataIndexMap(exAutomata);
        nbrAutomaton = exAutomata.size();
        nbrUnionEvents = exAutomata.getUnionAlphabet().size();

        try
        {
            generateAutomataIndices(exAutomata);
            generateEventIndices(exAutomata);
            generateStateIndices(exAutomata);
            generateNextStateTransitionIndices(exAutomata);
            generatePrevStatesTransitionIndices(exAutomata);
            generateEventsTables(exAutomata);
            generateEventToAutomatonTable(exAutomata);
        } catch (final Exception ex){
            logger.error("Error while generating AutomataIndexForm", ex);
            logger.debug(ex.getStackTrace());
        }
    }
    
    /**
     * Give each automaton a unique index Remember that this index
     * must be consistent with getAutomatonAt(int) in Automata
     */
    private void generateAutomataIndices(final ExtendedAutomata exAutomata)
    {
        typeIsPlantTable = new boolean[nbrAutomaton];
        typeIsSupSpecTable = new boolean[nbrAutomaton];
        automataSize = new int[nbrAutomaton];

        for (final ExtendedAutomaton currExAutomaton : exAutomata)
        {
            final int i = indexMap.getExtendedAutomatonIndex(currExAutomaton);
            final ComponentKind currAutomatonType = currExAutomaton.getKind();

            typeIsPlantTable[i] = currAutomatonType == ComponentKind.PLANT;
            typeIsSupSpecTable[i] = ((currAutomatonType == ComponentKind.SUPERVISOR) || (currAutomatonType == ComponentKind.SPEC));
            automataSize[i] = currExAutomaton.getNodes().size();
        }
    }
    
    private void generateEventIndices(final ExtendedAutomata exAutomata)
    {
        final List<EventDeclProxy> theAlphabet = exAutomata.getUnionAlphabet();

        // Generate a synchIndex for each event

        // Build tables from where it fast can be concluded
        // if a certain event is included or prioritized in a given automaton
        alphabetEventsTable = new boolean[nbrAutomaton][theAlphabet.size()];

        for (final EventDeclProxy currEvent : theAlphabet)
        {
            final int currEventSynchIndex = indexMap.getEventIndex(currEvent);

            for (int i = 0; i < nbrAutomaton; i++)
            {
                final List<EventDeclProxy> currExAutAlphabet = indexMap.getExtendedAutomatonAt(i).getAlphabet();

                if (currExAutAlphabet.contains(currEvent))
                {
                    alphabetEventsTable[i][currEventSynchIndex] = true;
                }
                else
                {
                    alphabetEventsTable[i][currEventSynchIndex] = false;
                }
            }
        }
    }

    /**
     * Creates an index in each state in all automata. The index is
     * unique for the automaton to which the state belongs.
     * Also builds a state table that connects the state index to
     * the physical state.
     *
     *@param  theAutomata Description of the Parameter
     */
    private void generateStateIndices(final ExtendedAutomata theAutomata)
    {
        stateTable = new NodeProxy[theAutomata.size()][];
        stateStatusTable = new int[theAutomata.size()][];
        automatonStateMaxIndex = new int[theAutomata.size()];

        for (final ExtendedAutomaton currAutomaton : theAutomata)
        {
            final int currAutomatonIndex = indexMap.getExtendedAutomatonIndex(currAutomaton);
            final int currNbrOfStates = currAutomaton.getNodes().size();

            stateTable[currAutomatonIndex] = new NodeProxy[currNbrOfStates];
            stateStatusTable[currAutomatonIndex] = new int[currNbrOfStates];

            int maxIndex = 0;

            for (final NodeProxy currState : currAutomaton.getNodes())
            {
                final int currIndex = indexMap.getLocationIndex(currAutomaton, currState);
                final boolean isInitial = currAutomaton.isLocationInitial(currState);
                final boolean isAccepted = currAutomaton.isLocationAccepted(currState);
                final boolean isForbbiden = currAutomaton.isLocationForbidden(currState);
                stateTable[currAutomatonIndex][currIndex] = currState;
                stateStatusTable[currAutomatonIndex][currIndex] = ExtendedAutomataIndexFormHelper.createStatus(isInitial, isAccepted, isForbbiden);
              if (currIndex > maxIndex)
                {
                    maxIndex = currIndex;
                }
            }
            automatonStateMaxIndex[currAutomatonIndex] = maxIndex;
        }
    }    

    /**
     * For each state in the automaton precompute an array
     * that contains the index of all events that leave the current
     * state. This array must be sorted, and the last element must be
     * MAX_VALUE. Note that this computation can not be
     * done in the states, since they do not know about the alphabet.
     *
     * Insert into enableEventsTable all states that enables a specific event.
     *
     * @param  theAutomata Description of the Parameter
     * @param  theAutomaton Description of the Parameter
     * @exception  Exception Description of the Exception
     */
    private void generateNextStateTransitionIndices(final ExtendedAutomata theAutomata)
    throws Exception
    {
        // Compute the nextStateTable and outgoingEventsTable
        /// also generate nextStatesTable
        final List<EventDeclProxy> theAlphabet = theAutomata.getUnionAlphabet();
        final int nbrOfAutomaton = theAutomata.size();
        final TreeSet<Integer> sortedEventIndices = new TreeSet<Integer>();
        final int alphabetSize = theAlphabet.size();
        
        nextStateTable = new int[nbrOfAutomaton][][];
        nextStatesTable = new int[nbrOfAutomaton][][][];
        outgoingEventsTable = new int[nbrOfAutomaton][][];
        enableEventsTable = new int[nbrOfAutomaton][][];
        guardStateEventTable = new int[nbrOfAutomaton][][][];
        actionStateEventTable = new int[nbrOfAutomaton][][][];
        eventGuradTable = new int[nbrOfAutomaton][alphabetSize][];
        eventActionTable = new int[nbrOfAutomaton][alphabetSize][];
        for(int i=0; i<nbrOfAutomaton; i++){
            for(int j=0; j< alphabetSize; j++){
                eventGuradTable[i][j] = new int[]{MAX_VALUE};
                eventActionTable[i][j] = new int[]{MAX_VALUE};
            }
        }
                        
        for(final ExtendedAutomaton currAutomaton:theAutomata){
            final int automatonIndex = indexMap.getExtendedAutomatonIndex(currAutomaton);
            final int nbrNodes = currAutomaton.getNodes().size();
            final int nbrEvents = theAlphabet.size();
            guardStateEventTable[automatonIndex] = new int[nbrNodes][nbrEvents][];
            actionStateEventTable[automatonIndex] = new int[nbrNodes][nbrEvents][];
                        
            for(final EdgeProxy tran : currAutomaton.getTransitions()){
                final int indexSource = indexMap.getLocationIndex(automatonIndex, tran.getSource());
                for(Iterator<Proxy> itr=tran.getLabelBlock().getEventIdentifierList().iterator();itr.hasNext();){
                    final EventDeclProxy currEvent = currAutomaton.getEvent(((SimpleIdentifierSubject)itr.next()).getName());
                    final int indexEvent = indexMap.getEventIndex(currEvent);
                    try{
                        final List<SimpleExpressionProxy> guards = tran.getGuardActionBlock().getGuards();
                        for(final SimpleExpressionProxy guard : guards){
                            final int guardIndex = indexMap.getGuardExpressionIndex(guard);
                            final int[] guardTable = eventGuradTable[automatonIndex][indexEvent];
                            final int[] guardStateTable = guardStateEventTable[automatonIndex][indexSource][indexEvent];
                            eventGuradTable[automatonIndex][indexEvent] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(guardIndex, guardTable);
                            guardStateEventTable[automatonIndex][indexSource][indexEvent] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(guardIndex, guardStateTable);
                        }
                        guardStateEventTable[automatonIndex][indexSource][indexEvent] = ExtendedAutomataIndexFormHelper.addToEndOfArray(MAX_VALUE, 
                                                                                                     guardStateEventTable[automatonIndex][indexSource][indexEvent]);
                    } catch(Exception ex){}
                    try{
                        final List<BinaryExpressionProxy> actions = tran.getGuardActionBlock().getActions();
                        for(final BinaryExpressionProxy action : actions){
                            final int actionIndex = indexMap.getActionExpressionIndex(action);
                            final int[] actionTable = eventActionTable[automatonIndex][indexEvent];
                            final int[] actionStateTable = actionStateEventTable[automatonIndex][indexSource][indexEvent];
                            eventActionTable[automatonIndex][indexEvent] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(actionIndex, actionTable);
                            actionStateEventTable[automatonIndex][indexSource][indexEvent] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(actionIndex, actionStateTable);
                        }
                        actionStateEventTable[automatonIndex][indexSource][indexEvent] = ExtendedAutomataIndexFormHelper.addToEndOfArray(MAX_VALUE, 
                                                                                                     actionStateEventTable[automatonIndex][indexSource][indexEvent]);
                        
                    } catch(Exception ex){}
                }
            }
        }
            
        for(final ExtendedAutomaton currAutomaton:theAutomata)
        {
            final int currAutomatonIndex = indexMap.getExtendedAutomatonIndex(currAutomaton);
            final int currAutomatonNbrOfStates = currAutomaton.getNodes().size();
            final int nbrOfEvents = theAlphabet.size();

            nextStateTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];
            nextStatesTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][][];
            outgoingEventsTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];
            
            // The "worst" case is that all states enables each event
            enableEventsTable[currAutomatonIndex] = new int[alphabetSize][];
            for (int i = 0; i < alphabetSize; i++)
            {
                enableEventsTable[currAutomatonIndex][i] = new int[currAutomatonNbrOfStates + 1];
                enableEventsTable[currAutomatonIndex][i][0] = MAX_VALUE;
            }


            for(final NodeProxy currState : currAutomaton.getNodes())
            {
                final int currStateIndex = indexMap.getLocationIndex(currAutomaton, currState);

                nextStateTable[currAutomatonIndex][currStateIndex] = new int[nbrOfEvents];
                nextStatesTable[currAutomatonIndex][currStateIndex] = new int[nbrOfEvents][];
                
                // Insert all event indices in a tree (sorted), here it is cleared, below it is filed
                sortedEventIndices.clear();

                // Sort arcs with respect to their associated events (the elements of the lists are not sorted!)nbrOfEvents
                final LinkedList<?>[] sortedArcs = new LinkedList<?>[nbrOfEvents];

                // Set a default value of each nextState
                for (int i = 0; i < nbrOfEvents; i++)
                {
                    nextStateTable[currAutomatonIndex][currStateIndex][i] = MAX_VALUE;
                    sortedArcs[i] = new LinkedList<EdgeProxy>();
                }

                // Iterate over outgoing arcs
                for(final EdgeProxy currArc : currAutomaton.getLocationToOutgoingEdgesMap().get(currState))
                {
                    
                    for(Iterator<Proxy> itrEvent = currArc.getLabelBlock().getEventIdentifierList().iterator();itrEvent.hasNext();){
                        // Get the event from the automaton
                        final EventDeclProxy currEvent = currAutomaton.getEvent(((SimpleIdentifierSubject)itrEvent.next()).getName());
//                        EventDeclProxy currEvent = theAutomata.eventIdToProxy(((SimpleIdentifierSubject)e).getName());
                        final int currEventIndex = indexMap.getEventIndex(currEvent);

                        // Sort
                        sortedEventIndices.add(currEventIndex);
                        @SuppressWarnings("unchecked") final
                        LinkedList<EdgeProxy> arcs = (LinkedList<EdgeProxy>) sortedArcs[currEventIndex];
                        arcs.add(currArc);

                        // Now insert the nextState index into the table
                        final NodeProxy currNextState = currArc.getTarget();
                        final int currNextStateIndex = indexMap.getLocationIndex(currAutomaton, currNextState);
                        nextStateTable[currAutomatonIndex][currStateIndex][currEventIndex] = currNextStateIndex;
                    }
                }

                // Allocate array for outgoingEventsTable
                outgoingEventsTable[currAutomatonIndex][currStateIndex] = new int[sortedEventIndices.size()+1];

                // Now copy all indices to an int array
                int i = 0;
                for(final Integer sortedEventIndicesIt : sortedEventIndices)
                {
                    // Generate outgoingEventsTable
                    final int thisIndex = sortedEventIndicesIt.intValue();
                    outgoingEventsTable[currAutomatonIndex][currStateIndex][i++] = thisIndex;

                    // Generate enableEventsTable
                    // Insert all states that enables the current event into
                    // enableEventsTable. This could easily be optimized to avoid the search.
                    int j = 0;
                    while (enableEventsTable[currAutomatonIndex][thisIndex][j] != MAX_VALUE)
                    {
                        j++;
                    }
                    enableEventsTable[currAutomatonIndex][thisIndex][j] = currStateIndex;
                    try
                    {
                        enableEventsTable[currAutomatonIndex][thisIndex][j + 1] = MAX_VALUE;
                    }
                    catch (final Exception ex)
                    {
                        logger.error("Error in AutomataIndexForm.generateNextStateTransitionIndices. " + ex);
                    }
                }
                outgoingEventsTable[currAutomatonIndex][currStateIndex][i] = MAX_VALUE;

                // Generate nextStatesTable based on sortedArcs
                for (i=0; i<nbrOfEvents; i++)
                {
                    @SuppressWarnings("unchecked")
                    final LinkedList<EdgeProxy> arcList = (LinkedList<EdgeProxy>)sortedArcs[i];

                    // Make new array
                    nextStatesTable[currAutomatonIndex][currStateIndex][i] = new int[arcList.size()+1];
                    // Add the target states' indices
                    int j=0;
                    for (final EdgeProxy arc : arcList)
                    {
                        final NodeProxy currNextState = arc.getTarget();
                        final int currNextStateIndex = indexMap.getLocationIndex(currAutomaton, currNextState);
                        nextStatesTable[currAutomatonIndex][currStateIndex][i][j] = currNextStateIndex;
                        j++;
                    }
                    nextStatesTable[currAutomatonIndex][currStateIndex][i][j] = MAX_VALUE;
                }
            }
        }
    }

    /**
     * For each state in the automaton precompute an array
     * that contains the index of all events that leave the current
     * state. This array must be sorted, and the last element must be
     * MAX_VALUE. Note that this computation can not be
     * done in the states, since they do not know about the alphabet.
     *
     *@param  theAutomata Description of the Parameter
     *@exception  Exception Description of the Exception
     */
    private void generatePrevStatesTransitionIndices(final ExtendedAutomata theAutomata)
    throws Exception
    {
        // Compute the prevStateTable and outgoingEventsTable
        final List<EventDeclProxy> theAlphabet = theAutomata.getUnionAlphabet();
        final int nbrOfAutomata = theAutomata.size();
        final int nbrOfEvents = theAlphabet.size();

        prevStatesTable = new int[nbrOfAutomata][][][];
        incomingEventsTable = new int[nbrOfAutomata][][];

        final TreeSet<Integer> sortedEventIndices = new TreeSet<Integer>();
        for(final ExtendedAutomaton currAutomaton : theAutomata)
        {
            final int currAutomatonIndex = indexMap.getExtendedAutomatonIndex(currAutomaton);
            final int currAutomatonNbrOfStates = currAutomaton.getNodes().size();

            prevStatesTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][][];
            incomingEventsTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];

            for(final NodeProxy currState : currAutomaton.getNodes())
            {
                final int currStateIndex = indexMap.getLocationIndex(currAutomaton, currState);

                prevStatesTable[currAutomatonIndex][currStateIndex] = new int[nbrOfEvents][];

                // Set a default value of each nextState
                for (int i = 0; i < nbrOfEvents; i++)
                {
                    prevStatesTable[currAutomatonIndex][currStateIndex][i] = new int[]{MAX_VALUE};
                }

                // Insert all indices in a tree (sorted), here it is cleared, in the below loop, it is filled
                sortedEventIndices.clear();
                // Interate over incoming arcs
                final ArrayList<EdgeSubject> incommingArcs = currAutomaton.getLocationToIngoingEdgesMap().get(currState);
                for(final EdgeProxy currArc : incommingArcs)
                {
                    for(Iterator<Proxy> itr=currArc.getLabelBlock().getEventIdentifierList().iterator();itr.hasNext();){
                        // Get the event from the automaton
                        final EventDeclProxy currEvent = theAutomata.eventIdToProxy(((SimpleIdentifierSubject)itr.next()).getName());
                        final int currEventIndex = indexMap.getEventIndex(currEvent);

                        sortedEventIndices.add(currEventIndex);
                        // Now insert the prevState index into the table
                        final NodeProxy currPrevState = currArc.getSource();
                        final int currPrevStateIndex = indexMap.getLocationIndex(currAutomaton, currPrevState);
                        final int[] currPreviousStates = prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex];
                        prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex] = ExtendedAutomataIndexFormHelper.addToBeginningOfArray(currPrevStateIndex,currPreviousStates);
                    }
                }

                incomingEventsTable[currAutomatonIndex][currStateIndex] = new int[sortedEventIndices.size() + 1];

                // Now copy all indices to an int array
                int i = 0;
                for(final Integer sortedEventIndicesIt : sortedEventIndices) {
                    incomingEventsTable[currAutomatonIndex][currStateIndex][i++] = sortedEventIndicesIt.intValue();
                }
                
                incomingEventsTable[currAutomatonIndex][currStateIndex][i] = MAX_VALUE;
            }
        }
    }

    private void generateEventsTables(final ExtendedAutomata theAutomata)
    throws Exception
    {
        final List<EventDeclProxy> theAlphabet = theAutomata.getUnionAlphabet();

        controllableEventsTable = new boolean[theAlphabet.size()];
        uncontrollableEventsTable = new boolean[theAlphabet.size()];
        epsilonEventsTable = new boolean[theAlphabet.size()];

        for (int i = 0; i < theAlphabet.size(); i++)
        {
            final EventDeclProxy currEvent = indexMap.getEventAt(i);

            controllableEventsTable[i] = (currEvent.getKind()==EventKind.CONTROLLABLE);
            uncontrollableEventsTable[i] = (currEvent.getKind()==EventKind.UNCONTROLLABLE);
            epsilonEventsTable[i] = !currEvent.isObservable();	// An event is epsilon if (and only if) it is unobservable
        }
    }

    private void generateEventToAutomatonTable(final ExtendedAutomata theAutomata)
    {
        final List<EventDeclProxy> theAlphabet = theAutomata.getUnionAlphabet();

        eventToAutomatonTable = new int[theAlphabet.size()][];

        final int nbrOfAutomata = theAutomata.size();

        for (final EventDeclProxy currEvent : theAlphabet)
        {
            final int currEventSynchIndex = indexMap.getEventIndex(currEvent);

            eventToAutomatonTable[currEventSynchIndex] = new int[nbrOfAutomata + 1];

            int currTablePosition = 0;
            for (final ExtendedAutomaton currAutomaton : theAutomata)
            {
                final List<EventDeclProxy> currAutAlphabet = currAutomaton.getAlphabet();

                if (currAutAlphabet.contains(currEvent))
                {
                    eventToAutomatonTable[currEventSynchIndex][currTablePosition] = indexMap.getExtendedAutomatonIndex(currAutomaton);
                    currTablePosition++;
                }
            }
            eventToAutomatonTable[currEventSynchIndex][currTablePosition] = MAX_VALUE;
        }
    }

    public boolean[][] getAlphabetEventsTable()
    {
        return alphabetEventsTable;
    }

    public int[][][] getOutgoingEventsTable()
    {
        return outgoingEventsTable;
    }

    public int[][][] getIncomingEventsTable()
    {
        return incomingEventsTable;
    }

    public int[][][] getNextStateTable()
    {
        return nextStateTable;
    }

    public int[][][][] getNextStatesTable()
    {
        return nextStatesTable;
    }

    public int[][][][] getPrevStatesTable()
    {
        return prevStatesTable;
    }

    public NodeProxy[][] getStateTable()
    {
        return stateTable;
    }

    public int[][] getStateStatusTable()
    {
        return stateStatusTable;
    }

    public boolean[] getTypeIsPlantTable()
    {
        return typeIsPlantTable;
    }

    public boolean[] getTypeIsSupSpecTable()
    {
        return typeIsSupSpecTable;
    }

    public boolean[] getControllableEventsTable()
    {
        return controllableEventsTable;
    }

    public boolean[] getUncontrollableEventsTable()
    {
        return uncontrollableEventsTable;
    }

    public boolean[] getEpsilonEventsTable()
    {
        return epsilonEventsTable;
    }

    public int[] getAutomataSize()
    {
        return automataSize;
    }

    public int[][][] getEnableEventsTable()
    {
        return enableEventsTable;
    }

    public int[][] getEventToAutomatonTable()
    {
        return eventToAutomatonTable; 
    }

    public NodeProxy getState(final int automatonIndex, final int stateIndex)
    {
        return stateTable[automatonIndex][stateIndex];
    }

    /** Returns the highest state index in each automaton */
    public int[] getAutomatonStateMaxIndex()
    {
        return automatonStateMaxIndex;
    }

    public ExtendedAutomataIndexMap getAutomataIndexMap()
    {
        return indexMap;
    }
    
    public int getNbrAutomaton(){
        return nbrAutomaton;
    }

    public int getNbrUnionEvents(){
        return nbrUnionEvents;
    }
    
    public int[][][][] getGuardStateEventTable(){
        return guardStateEventTable;
    }

    public int[][][][] getActionStateEventTable(){
        return actionStateEventTable;
    }

    public int[][][] getEventGuardTable(){
        return eventGuradTable;
    }

    public int[][][] getEventActionTable(){
        return eventActionTable;
    }
    
}
