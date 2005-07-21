
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
 * Haradsgatan 26A
 * 431 42 Molndal
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

import org.supremica.automata.*;
import java.util.*;
import org.supremica.log.*;

/**
 * Wrapper class for bisimulation equivalence minimization. Makes use
 * of native functions written in c based on the algorithm presented
 * in "Fast Decision of Strong Bisimulation Equivalence using
 * Partition Refinement" by Sven Westin.
 *
 * Run (something like) this command to generate the header-file...
 *
 * javah -classpath ~/Supremica/build -jni org.supremica.automata.algorithms.minimization.BisimulationEquivalenceMinimizer
 *
 * Then write the code and place it in a c-file.
 *
 * On linux, compile the c-file with (something like)
 *
 * cc -shared -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.c -o ~/Supremica/dist/libBisimulationEquivalence.so
 *
 * or rather...
 *
 * cc -shared -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.c -o ~/Supremica/platform/linux.x86/lib/libBisimulationEquivalence.so
 *
 * @author flordal
 */
public class BisimulationEquivalenceMinimizer
{
    private static Logger logger = LoggerFactory.createLogger(BisimulationEquivalenceMinimizer.class);

	/**
	 * True if native library is available, false otherwise.
	 */
	private static boolean libraryLoaded;

    // Load library
    static
    {
		try
		{
        	System.loadLibrary("BisimulationEquivalence");
        	libraryLoaded = true;
		}
		catch (UnsatisfiedLinkError ex)
		{
			libraryLoaded = false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			libraryLoaded = false;
		}
    }

    /**
     * Minimizes automaton with respect to bisimulation equivalence.
     *
     * @param strong If true, minimizes with respect to strong bisimulation equivalence,
     * if false, minimized with respect to weak bisimulation equivalence.
     */
    public static void minimize(Automaton aut, boolean useShortNames, boolean strong)
        throws UnsatisfiedLinkError
    {
        // Set the indices in the automaton.
        aut.setIndicies();

        // States...
        State[] states = new State[aut.nbrOfStates()];

        // Initialize
        {
            // The initial partitioning in an array, separated by -1 elements.
            // Normally, there should be two partitions, the set of  marked and
            // the set of nonmarked states
            int nbrOfPartitions;
            if (aut.hasAcceptingState() && aut.hasNonacceptingState())
            {
                nbrOfPartitions = 2;
            }
            else
            {
                nbrOfPartitions = 1;
            }
            int[] initialPartitioning = new int[aut.nbrOfStates()-1+nbrOfPartitions];
            int forwIndex = 0;
            int backIndex = initialPartitioning.length-1;
            for (Iterator<State> stIt = aut.stateIterator(); stIt.hasNext(); )
            {
                State state = stIt.next();
                int index = state.getIndex();
                assert((index >= 0) && (index < aut.nbrOfStates()));
                assert(states[index] == null);
                states[index] = state;
                //logger.info("State: " + state + " id: " + index);
                if (!state.isAccepting(!strong))
                {
                    initialPartitioning[forwIndex++] = index;
                }
                else
                {
                    initialPartitioning[backIndex--] = index;
                }

                if (forwIndex == backIndex)
                {
                    initialPartitioning[forwIndex] = -1;
                }
            }
            // This is an array of int, every third int is the index of a
            // from-state, event or to-state respectively...
            int[] transitions;
            int transitionCount = 0;
            if (strong)
            {
                // Strong bisimulation equivalence
                transitions = new int[aut.nbrOfTransitions()*3];
                int index = 0;
                for (Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
                {
                    Arc arc = arcIt.next();
                    transitions[index++] = arc.getFromState().getIndex();
                    transitions[index++] = arc.getEvent().getIndex();
                    transitions[index++] = arc.getToState().getIndex();
                }

                transitionCount = aut.nbrOfTransitions();
            }
            else
            {
                // Weak bisimulation equivalence
                // Find out how many transitions there are (after saturation)
                for (Iterator<State> fromIt = aut.stateIterator(); fromIt.hasNext(); )
                {
                    State from = fromIt.next();
                    for (Iterator<LabeledEvent> evIt = aut.eventIterator(); evIt.hasNext(); )
                    {
                        LabeledEvent event = evIt.next();
                        StateSet nextStates;
                        if (event.isEpsilon())
                        {
                            nextStates = from.epsilonClosure(true);
                        }
                        else
                        {
                            nextStates = from.nextStates(event, true);
                        }
                        for (Iterator<State> toIt = nextStates.iterator(); toIt.hasNext(); )
                        {
                            State to = toIt.next();
                            transitionCount++;
                        }
                    }
                }
				//logger.error("Number of transitions " + transitionCount);
                // Now, do the same thing again, and put the info in the array
                transitions = new int[transitionCount*3];
                int index = 0;
                for (Iterator<State> fromIt = aut.stateIterator(); fromIt.hasNext(); )
                {
                    State from = fromIt.next();
                    for (Iterator<LabeledEvent> evIt = aut.eventIterator(); evIt.hasNext(); )
                    {
                        LabeledEvent event = evIt.next();
                        StateSet nextStates;
                        if (event.isEpsilon())
                        {
                            nextStates = from.epsilonClosure(true);
                        }
                        else
                        {
                            nextStates = from.nextStates(event, true);
                        }
                        for (Iterator<State> toIt = nextStates.iterator(); toIt.hasNext(); )
                        {
                            State to = toIt.next();
                            //logger.info("From: " + from + " ev: " + event + " (" + event.getIndex() + ") to: " + to);
                            transitions[index++] = from.getIndex();
                            transitions[index++] = event.getIndex();
                            transitions[index++] = to.getIndex();
                        }
                    }
                }
            }

            // Initialize
            initialize(aut.nbrOfStates(), aut.nbrOfEvents(), transitionCount,
                       initialPartitioning, transitions);
        }

        // Do the partitioning
        partition();

        // Merge partitions based on the partitioning done in the native environment
        int[] part = getPartitioning();
        State blob = null;
        for (int i=0; i<part.length; i++)
        {
            if (part[i] != -1)
            {
                //logger.info("part[" + i + "] = " + part[i] + ", " + states[part[i]].getName());
                if (blob == null)
                {
                    blob = states[part[i]];
                }
                else
                {
                    State state = states[part[i]];
                    //logger.info("Merging " + blob + " and " + state);
                    blob = MinimizationHelper.mergeStates(aut, blob, state, useShortNames);
                }
            }
            else
            {
                //logger.info("Next partition:");
                // Prepare for new blob
                blob = null;
            }

            // Merge states in partition!
        }
    }

    /**
     * Initializes the native class with the necessary information.
     */
    private static native void initialize(int nbrOfStates, int nbrOfEvents, int nbrOfTransitions, int[] initialPartitioning, int[] transitions);

    /**
     * Runs the Generalized Relational Coarsest Partition Algorithm.
     */
    private static native void partition();

    /**
     * Returns an int-array representing the coarsest partitioning.
     */
    private static native int[] getPartitioning();

	/**
	 * Returns true if the necessary native library has been loaded properly.
	 */
	public static boolean libraryLoaded()
	{
		return libraryLoaded;
	}
}
