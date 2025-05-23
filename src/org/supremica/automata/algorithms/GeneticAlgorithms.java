
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.properties.Config;


/**
 * This class is a result of a project in the course Evolutionary Computation,
 * FFR105 (2002) at Chalmers University of Technology.
 * This class provides a method for predicting the size of the composition
 * of two automata.
 *
 * @author Hugo Flordal, Arash Vahidi
 */

public class GeneticAlgorithms
{
    private static Logger logger = LogManager.getLogger(GeneticAlgorithms.class);
    private static final int GA_DATA_SIZE = 16;

    public static double[] extractData(final Automaton autA, final Automaton autB)
    {
        final double[] data = new double[GA_DATA_SIZE];

        for (int i = 0; i < GA_DATA_SIZE; i++)
        {
            data[i] = 0.0;
        }

        // Amount of states in autA
        data[0] = autA.nbrOfStates();    // DON'T TOUCH!!! THIS MUST BE DATA[0]!!!

        // Amount of states in autB
        data[1] = autB.nbrOfStates();    // DON'T TOUCH!!! THIS MUST BE DATA[1]!!!

        final Alphabet alphabetIntersection = AlphabetHelpers.intersect(autA.getAlphabet(), autB.getAlphabet());

        // Percentage of common events in autA
        data[2] = ((double) alphabetIntersection.size()) / ((double) autA.nbrOfEvents());

        // Percentage of common events in autB
        data[3] = ((double) alphabetIntersection.size()) / ((double) autB.nbrOfEvents());

        // Amount of transitions in autA
        data[4] = autA.nbrOfTransitions();

        // Amount of transitions in autB
        data[5] = autB.nbrOfTransitions();

        // Percentage of selfloops among transitions in autA
        if (data[4] == 0)
        {
            data[8] = 0;
        }
        else
        {
            data[8] = (autA.nbrOfSelfLoops()) / data[4];
        }

        // Percentage of selfloops among transitions in autB
        if (data[5] == 0)
        {
            data[9] = 0;
        }
        else
        {
            data[9] = (autB.nbrOfSelfLoops()) / data[5];
        }

        // Percentage of transitions with common events in autA
        Iterator<Arc> arcIterator = autA.arcIterator();
        int commonTransitionsA = 0;
        while (arcIterator.hasNext())
        {
            if (alphabetIntersection.contains(arcIterator.next().getEvent()))
            {
                commonTransitionsA++;
            }
        }
        data[6] = ((double) commonTransitionsA) / ((double) autA.nbrOfTransitions());

        // Percentage of common transitions that are selfloops in autA
        if (commonTransitionsA == 0)
        {
            data[10] = 0;
        }
        else
        {
            data[10] = ((double) autA.nbrOfSelfLoops(alphabetIntersection)) / commonTransitionsA;
        }

        // Percentage of transitions with common events in autB
        arcIterator = autB.arcIterator();
        int commonTransitionsB = 0;
        while (arcIterator.hasNext())
        {
            if (alphabetIntersection.contains(arcIterator.next().getEvent()))
            {
                commonTransitionsB++;
            }
        }
        data[7] = ((double) commonTransitionsB) / ((double) autB.nbrOfTransitions());

        // Percentage of common transitions that are selfloops in autB
        if (commonTransitionsB == 0)
        {
            data[11] = 0;
        }
        else
        {
            data[11] = ((double) autB.nbrOfSelfLoops(alphabetIntersection)) / commonTransitionsB;
        }

        // Depth of autA
        data[12] = autA.depth();

        // Depth of autB
        data[13] = autB.depth();

        // Mean depth of occurences of common events in autA
        if (commonTransitionsA * data[12] == 0)
        {
            data[14] = 0;
        }
        else
        {
            data[14] = autA.depthSum(alphabetIntersection) / (commonTransitionsA * data[12]);
        }

        // Mean depth of occurences of common events in autB
        if (commonTransitionsB * data[13] == 0)
        {
            data[15] = 0;
        }
        else
        {
            data[15] = autB.depthSum(alphabetIntersection) / (commonTransitionsB * data[13]);
        }

        // Rounding... just for esthetical reasons
        data[2] = ((int) (100000.0 * data[2])) / 100000.0;
        data[3] = ((int) (100000.0 * data[3])) / 100000.0;
        data[6] = ((int) (100000.0 * data[6])) / 100000.0;
        data[7] = ((int) (100000.0 * data[7])) / 100000.0;
        data[8] = ((int) (100000.0 * data[8])) / 100000.0;
        data[9] = ((int) (100000.0 * data[9])) / 100000.0;
        data[10] = ((int) (100000.0 * data[10])) / 100000.0;
        data[11] = ((int) (100000.0 * data[11])) / 100000.0;
        data[14] = ((int) (100000.0 * data[14])) / 100000.0;
        data[15] = ((int) (100000.0 * data[15])) / 100000.0;

        // CONCEIVABLE DATA THAT MIGHT BE OF INTEREST
        // * Amount of states
        // * Amount of events (percentage common events)
        // * Amount of transitions (percentage that are common events)
        // * Amount of states in worst case
        // * Amount of selfloops (percentage)
        // * Amount of prioritized events (overkill)
        // * AutomatonType
        // * Maximum depth of automata
        return data;
    }

    /**
     * Calculates the exact synchronisation size. (Without building the full
     * automata model of the composed system.)
     */
    public static int calculateSynchronizationSize(final Automata automata)
    {
        return calculateSynchronizationSize(automata, null);
    }

    /**
     * Calculates the exact synchronisation size. (Without building the full
     * automata model of the composed system.)
     */
    public static int calculateSynchronizationSize(final Automata automata, SynchronizationOptions syncOptions)
    {
        // If there's only one automaton, there's no need to synchronize
        if ((automata.size() == 1) || (automata.getAutomatonAt(0) == automata.getAutomatonAt(1)))
        {
            return automata.getAutomatonAt(0).nbrOfStates();
        }

        if (syncOptions == null)
        {
            try
            {
                syncOptions = new SynchronizationOptions();
            }
            catch (final Exception e)
            {
                logger.error("Error in GeneticAlgorithms.java: " + e);
            }
        }

        AutomataSynchronizer theSynchronizer = null;

        try
        {
            theSynchronizer = new AutomataSynchronizer(automata, syncOptions, Config.SYNTHESIS_SUP_AS_PLANT.getValue());

            theSynchronizer.execute();
        }
        catch (final Exception e)
        {
            logger.error("Error in GeneticAlgorithms.java: " + e);
        }

        // The correct value, the number of states in the synchronization
        return (int) theSynchronizer.getNumberOfStates();
    }

    /**
     * This is not finished... the idea is to predict what happens when we
     * synchronize several automata together, adding one at a time.
     *
     * First we need to predict all the data in the synchronizaed automaton
     * * The predicted amount of states
     * * The predicted amount of transitions
     * * Calculate the amount of shared events (Alphabet.nbrOfCommonEvents())
     */
    /*
    private static double predictSynchronizationSize(Automata automata)
    {
        if (automata.size() == 2)
        {
            return predictSynchronizationSize(automata.getAutomatonAt(0), automata.getAutomatonAt(1));
        }
        else if (automata.size() == 1)
        {
            return predictSynchronizationSize(automata.getAutomatonAt(0), automata.getAutomatonAt(0));
        }
        else
        {
            logger.error("Only the size of two synchronized automata can be predicted!");

            return 0.0;
        }
    }
     */

    /**
     * Predicts the size (number of states) of the composition of automata.
     */
    public static double predictSynchronizationSize(final Automata automata)
    {
        // Predict pairwise compositions and multiply the results
        double prediction = 1;
        for (final Iterator<Automaton> autIt = automata.iterator(); autIt.hasNext(); )
        {
            final Automaton autA = autIt.next();
            if (autIt.hasNext())
            {
                final Automaton autB = autIt.next();
                prediction *= predictSynchronizationSize(autA, autB);
            }
            else
            {
                prediction *= autA.nbrOfStates();
            }
        }
        return prediction;
    }

    /**
     * Predicts the size (number of states) of the composition of autA and autB.
     */
    public static double predictSynchronizationSize(final Automaton autA, final Automaton autB)
    {
        final double[] data = GeneticAlgorithms.extractData(autA, autB);
        double prediction = PredictionFunction.f(data);

        if (prediction > data[0] * data[1])
        {
            prediction = data[0] * data[1];
        }

        return prediction;
    }

    /**
     * This class is automatically generated by an external
     * Evolutionary Computation software (By Flordal and Vahidi). It
     * was based on training data from the central lock example (verriegel3).
     */
    private static class PredictionFunction
    {
        private static double[] r = new double[16];

        public static double f(final double[] in)
        {
            for (int i = 0; i < 16; i++)
            {
                r[i] = in[i];
            }

            r[11] = Math.cos(in[9]);
            r[15] = 0.546875;
            r[9] = Math.pow(r[9], r[2]);
            r[0] = Math.max(r[0], r[13]);
            r[3] = r[3] - r[15];
            r[0] = Math.min(r[0], r[4]);
            r[9] = r[9] * in[3];
            r[13] = Math.max(r[13], r[1]);
            r[2] = Math.pow(r[2], r[0]);
            r[2] = r[2] + in[2];

            if (r[1] > in[0])
            {
                if (in[7] != 0)
                {
                    r[2] = r[2] / in[7];
                }
            }

            r[9] = Math.pow(r[9], r[2]);

            if (r[9] > in[6])
            {
                r[13] = r[13] * in[12];
            }

            r[12] = r[12] + in[11];
            r[0] = Math.max(r[0], r[1]);
            r[12] = r[12] - r[11];
            r[12] = r[12] * in[14];
            r[0] = Math.max(r[0], r[13]);

            if (r[3] > 0)
            {
                r[1] = Math.log(r[3]);
            }

            r[0] = Math.max(r[0], r[13]);

            if (r[3] > 0)
            {
                r[1] = Math.log(r[3]);
            }

            r[9] = r[9] + in[4];
            r[9] = r[9] - r[12];
            r[4] = r[4] - in[11];
            r[13] = Math.max(r[13], r[1]);
            r[9] = r[9] * in[3];
            r[0] = Math.max(r[0], r[13]);
            r[9] = r[9] - r[12];

            if (r[3] > 0)
            {
                r[9] = Math.log(r[3]);
            }

            r[6] = Math.sqrt(Math.abs(r[0]));

            if (r[6] != 0)
            {
                r[9] = r[9] / r[6];
            }

            r[13] = -0.0625;

            if (in[3] != 0)
            {
                r[9] = r[9] / in[3];
            }

            r[9] = r[9] * in[3];
            r[12] = Math.cos(in[7]);
            r[13] = Math.abs(r[1]);

            if (r[9] > 0)
            {
                r[13] = Math.log(r[9]);
            }

            r[0] = Math.max(r[0], r[13]);

            if (r[3] > 0)
            {
                r[1] = Math.log(r[3]);
            }

            r[7] = Math.sqrt(Math.abs(r[0]));
            r[4] = r[4] - in[11];
            r[13] = Math.exp(r[12]);
            r[12] = Math.cos(in[7]);
            r[13] = Math.abs(r[1]);
            r[13] = Math.abs(r[1]);
            r[7] = Math.min(r[7], in[3]);
            r[13] = Math.max(r[13], r[7]);

            if (r[4] > in[5])
            {
                if (r[0] > in[3])
                {
                    r[13] = Math.max(r[13], r[1]);
                }
            }

            r[7] = Math.min(r[7], in[3]);
            r[13] = Math.max(r[13], r[7]);

            if (r[4] > in[5])
            {
                r[7] = r[7] + in[12];
            }

            r[13] = Math.max(r[13], r[1]);
            r[7] = Math.max(r[7], in[9]);
            r[0] = Math.min(r[0], r[4]);
            r[13] = r[13] - r[7];

            if (r[7] > in[10])
            {
                r[13] = r[13] - r[7];
            }

            r[13] = r[13] - in[10];
            r[12] = Math.max(r[12], in[9]);
            r[0] = r[0] - in[11];
            r[13] = r[13] * in[12];
            r[0] = Math.max(r[0], r[13]);
            r[0] = Math.max(r[0], r[13]);
            r[12] = r[12] + in[11];
            r[0] = Math.max(r[0], r[1]);
            r[13] = Math.abs(in[15]);
            r[0] = Math.max(r[0], r[13]);
            r[11] = Math.sqrt(Math.abs(r[13]));
            r[0] = Math.max(r[0], r[13]);
            r[0] = Math.max(r[0], r[13]);
            r[1] = Math.cos(r[11]);
            r[0] = Math.max(r[0], r[13]);
            r[13] = Math.max(r[13], r[0]);
            r[0] = -0.3125;
            r[0] = Math.max(r[0], r[13]);
            r[1] = r[1] - in[5];
            r[1] = Math.max(r[1], r[12]);
            r[13] = Math.sin(in[9]);
            r[0] = Math.max(r[0], r[13]);
            r[13] = Math.max(r[13], r[1]);
            r[0] = Math.max(r[0], r[13]);
            r[0] = Math.max(r[0], r[13]);
            r[0] = r[0] + r[0];

            return r[0];
        }    // 88 instructions, fitness: 0.08753301196436607
    }
}
