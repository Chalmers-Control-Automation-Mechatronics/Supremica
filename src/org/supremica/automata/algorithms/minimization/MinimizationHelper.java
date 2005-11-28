
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms.minimization;

import java.util.*;
import org.supremica.automata.*;
import org.supremica.properties.SupremicaProperties;

/**
 * This class has methods used by minimization algorithms.
 *
 * @author <a href="mailto:flordal@chalmers.se">Hugo Flordal</a>
 * @version 1.0
 */
public class MinimizationHelper
{
    /**
     * Merges two states, giving the new state the union of incoming and outgoing transitions,
     * if at least one state was accepting, the result is accepting, and similarily for
     * initial and forbidden states
     *
     * @param aut The automaton in which the collapsing is done, both
     * states must be in this automaton.
     * @param useShortNames If true, a new, short, name is generated
     * for the new state instead of concatenating the names of the
     * originals.
     * @return The resulting state (which belongs to aut).
     */
    public static State mergeStates(Automaton aut, State one, State two, boolean useShortNames)
    {
        // Don't merge if equal
        if (one.equals(two))
        {
            return one;
        }

        // Make new state
        State newState;
        if (useShortNames)
        {
            newState = aut.createUniqueState();
        }
        else
        {
            newState = new State(one.getName() + SupremicaProperties.getStateSeparator() + two.getName());
        }
        aut.addState(newState);

        // Set markings
        if (one.isAccepting() || two.isAccepting()) // Looks odd but is correct?
        {
            newState.setAccepting(true);
        }
        if (one.isForbidden() || two.isForbidden())
        {
            newState.setForbidden(true);
        }
        if (one.isInitial() || two.isInitial())
        {
            newState.setInitial(true);
            aut.setInitialState(newState);
        }

        // Add transitions
        LinkedList toBeAdded = new LinkedList();
        for (ArcIterator arcIt = one.outgoingArcsIterator(); arcIt.hasNext(); )
        {
            Arc arc = arcIt.nextArc();
            State toState = arc.getToState();
            if (toState.equals(one) || toState.equals(two))
            {
                toState = newState;
            }

            toBeAdded.add(new Arc(newState, toState, arc.getEvent()));
        }
        for (ArcIterator arcIt = two.outgoingArcsIterator(); arcIt.hasNext(); )
        {
            Arc arc = arcIt.nextArc();
            State toState = arc.getToState();
            if (toState.equals(one) || toState.equals(two))
            {
                toState = newState;
            }

            toBeAdded.add(new Arc(newState, toState, arc.getEvent()));
        }
        for (ArcIterator arcIt = one.incomingArcsIterator(); arcIt.hasNext(); )
        {
            Arc arc = arcIt.nextArc();
            State fromState = arc.getFromState();
            if (fromState.equals(one) || fromState.equals(two))
            {
                fromState = newState;
            }

            toBeAdded.add(new Arc(fromState, newState, arc.getEvent()));
        }
        for (ArcIterator arcIt = two.incomingArcsIterator(); arcIt.hasNext(); )
        {
            Arc arc = arcIt.nextArc();
            State fromState = arc.getFromState();
            if (fromState.equals(one) || fromState.equals(two))
            {
                fromState = newState;
            }

            toBeAdded.add(new Arc(fromState, newState, arc.getEvent()));
        }
        // Add the new arcs!
        while (toBeAdded.size() != 0)
        {
            // Add if not already there or epsilon selfloop
            Arc arc = (Arc) toBeAdded.remove(0);
            if (arc.getEvent().isEpsilon() && arc.isSelfLoop())
            {
                continue;
            }
            if (!arc.getFromState().containsOutgoingArc(arc))
            {
                aut.addArc(arc);
            }
        }

        // Remove the states
        aut.removeState(one);
        aut.removeState(two);

        /*
        // Adjust the index of the new state (see the "Här blir det fel" discussion in AutomataIndexForm)
        if (one.getIndex() < two.getIndex())
        {
            // Take over the index of state one
            newState.setIndex(one.getIndex());
        }
        else
        {
            // Take over the index of state two
            newState.setIndex(two.getIndex());
        }
        */

        // Return the new state
        return newState;
    }
}
