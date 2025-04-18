
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
package org.supremica.automata.algorithms;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.algorithms.minimization.AutomatonMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;


public class AutomatonSplit
{
    private static Logger logger = LogManager.getLogger(AutomatonSplit.class);

    /**
     * Decomposes automaton into two automata (experimental).
     * @return an automata containing parts that when composed give the original or null if the operation failed.
     */
    public static Automata split(final Automaton original)
    {
        if (!original.isDeterministic())
        {
            logger.error("The \"Automaton split\" algorithm does not work for nondeterministic automata.");
            return null;
        }

        final Automata split = new Automata();
        Automaton splitA = null;
        Automaton splitB = null;

        try
        {
            int bestResult = Integer.MAX_VALUE;
            for (final Iterator<LabeledEvent> evIt = original.eventIterator(); evIt.hasNext(); )
            {
                // Remove one event from A's alphabet
                final LabeledEvent remove = evIt.next();

                splitA = removeEvent(original, remove);
                splitB = reduceAutomaton(new Automaton(original), new Automata(splitA));
                splitA = reduceAutomaton(new Automaton(original), new Automata(splitB));

                                /*
                                // This costs more than it tastes... and it won't even give
                                // the optimal solution...
                                int value = splitA.nbrOfStates()*splitA.nbrOfStates() + splitB.nbrOfStates()*splitB.nbrOfStates();
                                logger.info("A: " + splitA.nbrOfStates() + " B: " + splitB.nbrOfStates());
                                if (bestValue > value && (splitA.nbrOfStates() > 1) && (splitB.nbrOfStates() > 1))
                                {
                                                split.clear();
                                                splitA.setComment(original.getName() + "_A");
                                                splitB.setComment(original.getName() + "_B");
                                                split.addAutomaton(splitA);
                                                split.addAutomaton(splitB);
                                                bestValue = value;
                                }
                                 */
                if (!(splitA.nbrOfEvents() == 0) || (splitB.nbrOfEvents() == 0))
                {
                    final int states = splitA.nbrOfStates() + splitB.nbrOfStates();
                    if (states < bestResult)
                    {
                        bestResult = states;
                        split.clear();
                        splitA.setComment(original.getName() + "_A");
                        splitB.setComment(original.getName() + "_B");
                        split.addAutomaton(splitA);
                        split.addAutomaton(splitB);
                    }
                }
            }
        }
        catch (final Exception ex)
        {
            logger.error("Error when splitting " + original);
            return null;
        }

        // Fail?
        if (split.size() == 0)
        {
            logger.info("Unable to split automaton " + original + ".");
            return null;
        }

        // Return the results
        return split;
    }

    /**
     * Calculates a reduced (in amount of events and possibly states) supervisor
     * having the same behaviour as the original had when synchronized with its 'parents'.
     *  The supervisor is supposed to be generated by synthesis from the automata 'parents'.
     * This means, among other things, that the alphabet of the supervisor is the union
     * alphabet of the 'parents'...
     *
     * @param supervisor The supervisor to be reduced.
     * @param parents The automata that were used to generate the supervisor.
     * @return The reduced supervisor or null if the operation failed.
     */
    public static Automaton reduceAutomaton(final Automaton supervisor, final Automata parents)
    {
        if (!supervisor.isDeterministic())
        {
            logger.error("The \"Reduce automaton\" algorithm does not work for nondeterministic automata.");
            return null;
        }

        if (supervisor.nbrOfStates() == 0)
        {
            return supervisor;
        }

        Automaton result = supervisor;
        final Automata automataB = new Automata(supervisor);
        final Alphabet parentUnion = parents.getUnionAlphabet();

        try
        {
            // Remove event and examine if it was redundant
            for (final Iterator<LabeledEvent> evIt = supervisor.eventIterator(); evIt.hasNext(); )
            {
                final LabeledEvent event = evIt.next();

                // Do not remove an event that is local in the supervisor
                if (!parentUnion.containsEqualEvent(event))
                {
                    continue;
                }

                // Remove one event from the automaton�s alphabet
                final Automaton reduction = removeEvent(result, event);

                // Have we removed something we shouldn't have?
                final Automata automataA = new Automata();
                automataA.addAutomaton(reduction);
                automataA.addAutomata(parents);
                if (AutomataVerifier.verifyModularInclusion(automataA, automataB))
                {
                    // Removing the event didn't make a difference!
                    result = reduction;
                    break;
                }
                // Toolkit.getDefaultToolkit().beep();
            }

            // Minimize result
            final AutomatonMinimizer minimizer = new AutomatonMinimizer(result);
            final MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setIgnoreMarking(true);
            // options.setKeepOriginal(false);
            result = minimizer.getMinimizedAutomaton(options);

            /*
                        // Merge states and examine if it was redundant
                        for (ArcIterator arcIt = result.arcIterator(); arcIt.hasNext();)
                        {
                                        Arc arc = arcIt.nextArc();

                                        // Merge states in transition, make selfloop
                                        Automaton reduction = removeTransition(result, arc);

                                        // Have we removed something vital? (If not, that's good!)
                                        Automata automataA = new Automata();
                                        automataA.addAutomaton(reduction);
                                        automataA.addAutomata(parents);
                                        if (AutomataVerifier.verifyInclusion(automataA, automataB))
                                        {
                        // Removing the event didn't make a difference!
                                                result = reduction;
                                        }
                        }
             */
        }
        catch (final Exception ex)
        {
            logger.error("Error when reducing automaton " + supervisor + ", " + ex);
            return null;
        }

        // Give the new automaton an appropriate comment
        result.setComment("red(" + supervisor.getName() + ")");

        // Set the right type
        result.setType(supervisor.getType());    // should be a supervisor...

        // Return the smallest one of the original and the "reduction"
        if (supervisor.nbrOfStates() < result.nbrOfStates())
        {
            return supervisor;
        }
        else
        {
            return result;
        }
    }

    /**
     * Removes, by natural projection, one event from the alphabet.
     * The method returns a determinized and minimized automaton
     */
    private static Automaton removeEvent(final Automaton automaton, final LabeledEvent event)
    throws Exception
    {

        final Automaton result = new Automaton(automaton);

        // Remove one event and make deterministic
        final Alphabet restrictAlphabet = new Alphabet();
        restrictAlphabet.addEvent(event);

        // Hide event
        result.hide(restrictAlphabet, false);

        // Hiding is enough!!!!!!
                /*
                // Minimize
                AutomatonMinimizer minimizer = new AutomatonMinimizer(result);
                MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
                options.setIgnoreMarking(true);
                options.setKeepOriginal(false);
                result = minimizer.getMinimizedAutomaton(options);
                 */
                /*
                // Determinize
                Determinizer determinizer = new Determinizer(result);
                determinizer.execute();
                result = determinizer.getNewAutomaton();
                 */

        // Set comment and return
        result.setName(null);
        result.setComment(automaton + "\\" + restrictAlphabet);

        return result;
    }

    /**
     * Merges the from and to states of a transition, the transition itself
     * becomes a self loop.
     */
        /*
        private static Automaton removeTransition(Automaton automaton, Arc arc)
                throws Exception
        {
                State fromState = arc.getFromState();
                State toState = arc.getToState();

                // Merge states
                automaton.mergeStates(fromState, toState);

                // Minimize (language equivalence) (also makes deterministic)
                AutomatonMinimizer minimizer = new AutomatonMinimizer(automaton);
                MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
                options.setIgnoreMarking(true);
                options.setKeepOriginal(false);
                Automaton result = minimizer.getMinimizedAutomaton(options);

                return result;
        }
         */
}
