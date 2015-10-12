/*
 * SchedulingHelper.java
 *
 * Created on den 22 augusti 2007, 09:38
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Iterator;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author Avenir Kobetski
 *
 * This class contains several helpful static methods that different scheduling
 * algorithms might benefit from.
 */
public class SchedulingHelper
{
    /**
     *  This method prepares a plant for a scheduling algorithm.
     *  It checks whether the plant has an initial state
     *  (prior to the call to this method, the plant is composed with
     *  its specifications, i.e. specifications that only consist of events in
     *  this plant. If the specifications are too restrictive, an initial
     *  state may not exist). Next, if the initial state of the plant is also
     *  accepting, a dummy accepting state is added to the plant in order to
     *  allow the optimization algorithm to start. If there is a self-loop
     *  in the initial state (which should only occur after one run of the
     *  optimization algorithm), it is removed (otherwise MILP cannot function).
     *  If there are several accepting states, they are replaced by one accepting state.
     *
     *  @param currPlant The plant to be prepared for the scheduling.
     */
    public static void preparePlantForScheduling(final Automaton currPlant)
        throws Exception
    {
        final State currInitialState = currPlant.getInitialState();

        // If there is no initial state, throw exception
        if (currInitialState == null)
        {
            final int plantNameRoopIndex = currPlant.getName().indexOf("_constr");
            String plantName;
            if (plantNameRoopIndex < 0)
            {
                plantName = currPlant.getName();
            }
            else
            {
                plantName = currPlant.getName().substring(0, plantNameRoopIndex);
            }
            throw new Exception(plantName + " has no initial state, possibly due to the restrictions imposed by its specifications. The system has thus no (optimal) path.");
        }

        // Remove the self-loops in the initial state. Such self-loops are sometimes created during
        // schedule construction to run a schedule repeatedly.
        final ArrayList<Arc> arcsToBeRemoved = new ArrayList<Arc>();
        for (final Iterator<Arc> arcIt = currInitialState.outgoingArcsIterator(); arcIt.hasNext();)
        {
            final Arc arc = arcIt.next();
            if (arc.isSelfLoop())
            {
                arcsToBeRemoved.add(arc);
            }
        }
        for (final Arc arc : arcsToBeRemoved)
        {
            currPlant.removeArc(arc);
        }
//        if (selfLoopDetected)
//        {
//            currPlant.remapStateIndices();
//        }

        // Add a dummy accepting state if the initial state is accepting
        if (currInitialState.isAccepting())
        {
            currInitialState.setAccepting(false);

            final State dummyState = new State("dummy_" + currInitialState.getName());
            currPlant.addState(dummyState);

            for (final Iterator<Arc> incomingArcIt = currInitialState.incomingArcsIterator(); incomingArcIt.hasNext(); )
            {
                final Arc currArc = incomingArcIt.next();

                currPlant.addArc(new Arc(currArc.getFromState(), dummyState, currArc.getEvent()));
            }

            currInitialState.removeIncomingArcs();

            dummyState.setAccepting(true);
            dummyState.setCost(0);

//            currPlant.remapStateIndices();
        }

        // Collect the accepting states
        final ArrayList<State> acceptingStates = new ArrayList<State>();
        for (final Iterator<State> stateIt = currPlant.stateIterator(); stateIt.hasNext();)
        {
            final State state = stateIt.next();
            if (state.isAccepting())
            {
                acceptingStates.add(state);
            }
        }
        // If they are more than one, unmark them and add a dummy event from
        // each of them to a new (dummy) accepting state
        if (acceptingStates.size() > 1)
        {
            String accStateName = "q_dummy_acc";
            while (currPlant.containsStateWithName(accStateName))
            {
                accStateName += "1";
            }
            final State newAccState = new State(accStateName);
            newAccState.setAccepting(true);
            newAccState.setCost(0);
            currPlant.addState(newAccState);

            String dummyEventName = currPlant.getName() + "_dummy_event";
            while (currPlant.getAlphabet().contains(dummyEventName))
            {
                dummyEventName += "1";
            }
            final LabeledEvent dummyEvent = new LabeledEvent(dummyEventName);
            currPlant.getAlphabet().addEvent(dummyEvent);

            for (int i = 0; i < acceptingStates.size(); i++)
            {
                final State oldAccState = acceptingStates.get(i);
                currPlant.addArc(new Arc(oldAccState, newAccState, dummyEvent));
                oldAccState.setAccepting(false);
            }
        }

        currPlant.remapStateIndices();
    }

    /**
     * This method calls @see{preparePlantForScheduling} for each plant
     * in the supplied automata.
     *
     * @param  plants the plants to be prepared
     */
    public static void preparePlantsForScheduling(final Automata plants)
        throws Exception
    {
        for (final Iterator<Automaton> plantIt = plants.iterator(); plantIt.hasNext(); )
        {
            preparePlantForScheduling(plantIt.next());
        }
    }
}
