
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
import org.supremica.automata.algorithms.GeneticAlgorithms;

public enum MinimizationHeuristic
{
    MostLocal("Highest local ratio", "maxL", Type.MAXIMIZE),
    MostCommon("Highest common ratio", "maxC", Type.MAXIMIZE),
    LeastExtension("Least extension of alphabet", Type.MINIMIZE),
    FewestTransitions("Fewest transitions", "minT", Type.MINIMIZE),
    FewestStates("Fewest states", "minS", Type.MINIMIZE),
    FewestEvents("Fewest events", "minE", Type.MINIMIZE),
    FewestAutomata("Fewest automata", "minA", Type.MINIMIZE),
    MostTransitions("Most transitions", "maxT", Type.MAXIMIZE),
    MostStates("Most states", "maxS", Type.MAXIMIZE),
    MostEvents("Most events", "maxE", Type.MAXIMIZE),
    MostAutomata("Most automata", "maxA", Type.MAXIMIZE),
    GeneticAlgorithm("Genetic algorithm prediction", "genetic", Type.MINIMIZE),
    LeastFanning("Least fanning increase", "minFan", Type.MINIMIZE),
    Random("Random order", "rand", Type.MAXIMIZE);
    
    private enum Type {MAXIMIZE, MINIMIZE, SPECIAL}
    
    /** Textual description. */
    private final String description;
    /** Textual description abbreviated. */
    private final String abbreviation;

    private Type type;
    
    private MinimizationHeuristic(String description, Type type)
    {
		this(description, description, type);
    }

    private MinimizationHeuristic(String description, String abbreviation, Type type)
    {
        this.description = description;
		this.abbreviation = abbreviation; 
        this.type = type;
    }
    
    public String toString()
    {
        return description;
    }

    public String toStringAbbreviated()
    {
        return abbreviation;
    }
    
    public static MinimizationHeuristic toHeuristic(String description)
    {
        for (MinimizationHeuristic heuristic: values())
        {
            if (heuristic.description.equals(description))
            {
                return heuristic;
            }
        }
        return null;
    }
    
    /**
     * Return the value of automata in this heuristic.
     *
     * @param eventToAutomataMap is a map from all (global) events to all (global) automata.
     */
    public final double value(final Automata selection, final Map<LabeledEvent, Automata> eventToAutomataMap, final Alphabet targetAlphabet)
    throws Exception
    {
        if (this == MostLocal)
        {
            Alphabet localEvents = MinimizationHelper.getLocalEvents(selection, eventToAutomataMap);
            localEvents.minus(targetAlphabet);
            int nbrOfLocalEvents = localEvents.size();
            int unionAlphabetSize = selection.getUnionAlphabet().size();
            //System.err.println(" Value: " + (int) (1000 * ((double) nbrOfLocalEvents)/((double) unionAlphabetSize)) +  " aut: " + selection);
            return ((double) nbrOfLocalEvents)/((double) unionAlphabetSize);
        }
        else if (this == MostCommon)
        {
            Alphabet commonEvents = MinimizationHelper.getCommonEvents(selection, eventToAutomataMap);
            int nbrOfCommonEvents = commonEvents.size();
            int unionAlphabetSize = selection.getUnionAlphabet().size();
            return ((double) nbrOfCommonEvents)/((double) unionAlphabetSize);
        }
        else if (this == LeastExtension)
        {
            int unionAlphabetSize = selection.getUnionAlphabet().size();
            int largestAlphabetSize = 0;
            for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
            {
                int size = autIt.next().getAlphabet().size();
                if (size > largestAlphabetSize)
                {
                    largestAlphabetSize = size;
                }
            }
            return ((double) unionAlphabetSize)/((double) largestAlphabetSize);
        }
        else if (this == MostStates || this == FewestStates)
        {
                        /*
                        // Prod
                        int value = 1;
                        for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
                                value *= autIt.next().nbrOfStates();
                         */
            // Least squares
            int value = 0;
            for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
                value += Math.pow(autIt.next().nbrOfStates(), 2);
            
            return value;
        }
        else if (this == MostEvents || this == FewestEvents)
        {
            return selection.getUnionAlphabet().size();
        }
        else if (this == MostTransitions || this == FewestTransitions)
        {
                        /*
                        // Prod
                        int value = 1;
                        for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
                                value *= autIt.next().nbrOfTransitions();
                         */
            // Least squares
            int value = 0;
            for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
                value += Math.pow(autIt.next().nbrOfTransitions(), 2);
            
            return value;
        }
        else if (this == MostAutomata || this == FewestAutomata)
        {
            return selection.size();
        }
        else if (this == GeneticAlgorithm)
        {
            return GeneticAlgorithms.predictSynchronizationSize(selection);
        }
        else if (this == Random)
        {
            return Math.random();
        }
        else if (this == LeastFanning)
        {
            // How many connections does the most connected component in the selection have 
            // and how many connections will the composition have?
            // (not counting in-selection connections)
            int mostNeighbours = 0;
            final Automata selectionNeighbours = new Automata();
            for (Automaton aut : selection)
            {
                final Automata autNeighbours = new Automata();
                for (LabeledEvent event : aut.getAlphabet())
                {
                    if (event.isObservable())
                    {
                        autNeighbours.addAutomata(eventToAutomataMap.get(event));
                        selectionNeighbours.addAutomata(eventToAutomataMap.get(event));
                    }
                }
                autNeighbours.removeAutomata(selection);
                if (mostNeighbours < autNeighbours.size())
                    mostNeighbours = autNeighbours.size();
            }
            selectionNeighbours.removeAutomata(selection);
            // Return the difference, i.e. the increase in connections
            return selectionNeighbours.size() - mostNeighbours;
        }
        
        throw new Exception("Unknown heuristic.");
    }
    
    /**
     * Maximization criteria?
     */
    public boolean maximize()
    {
        return type == Type.MAXIMIZE;
    }
    
    /**
     * Minimization criteria?
     */
    public boolean minimize()
    {
        return type == Type.MINIMIZE;
    }
    
    /**
     * Minimization criteria?
     */
    public boolean isSpecial()
    {
        return type == Type.SPECIAL;
    }
    
    /**
     * The initial value for improvement comparisons.
     */
    public double worstValue()
    {
        return (maximize() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }
}
