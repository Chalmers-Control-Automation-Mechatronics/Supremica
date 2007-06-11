
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

import java.util.Map;
import org.supremica.automata.*;

public enum MinimizationStrategy
{
    AtLeastOneLocal("At least one local", "mustL", Type.SPECIAL),
    AtLeastOneLocalMaxThree("At least one local, max three", "mustL3", Type.SPECIAL),
    FewestTransitionsFirst("Pair with fewest transition automaton", "minT", Type.MINIMIZE),
    FewestStatesFirst("Pair with fewest states automaton", "minS", Type.MINIMIZE),
    FewestEventsFirst("Pair with fewest events automaton", "minE", Type.MINIMIZE),
    MostTransitionsFirst("Pair with most transitions automaton", "maxT", Type.MAXIMIZE),
    MostStatesFirst("Pair with most states automaton", "maxS", Type.MAXIMIZE),
    MostEventsFirst("Pair with most events automaton", "maxE", Type.MAXIMIZE),
    FewestNeighboursFirst("Pair with fewest neighbours automaton", "minN", Type.MINIMIZE),
    RandomFirst("Pair with random automaton", "rand", Type.MAXIMIZE),
    ExperimentalMin("Experimental min", Type.MINIMIZE),
    ExperimentalMax("Experimental max", Type.MAXIMIZE);
    
    private enum Type {MAXIMIZE, MINIMIZE, SPECIAL}
    
    /** Textual description. */
    private final String description;
    /** Textual description abbreviated. */
    private final String abbreviation;
    
    private final Type type;
    
    private MinimizationStrategy(String description, Type type)
    {
        this(description, description, type);
    }

    private MinimizationStrategy(String description, String abbreviation, Type type)
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
    
    public static MinimizationStrategy toStrategy(String description)
    {
        for (MinimizationStrategy strategy: values())
        {
            if (strategy.description.equals(description))
            {
                return strategy;
            }
        }
        return null;
    }
    
    /**
     * Return the value of automata in this strategy.
     */
    public int value(final Automaton aut, final Map<LabeledEvent, Automata> eventToAutomataMap)
    throws Exception
    {
        if (this == MostStatesFirst || this == FewestStatesFirst)
            return aut.nbrOfStates();
        else if (this == MostTransitionsFirst || this == FewestTransitionsFirst)
            return aut.nbrOfTransitions();
        else if (this == MostEventsFirst || this == FewestEventsFirst)
            return aut.nbrOfEvents();
        else if (this == ExperimentalMax || this == ExperimentalMin)
            return aut.nbrOfTransitions() + 1*aut.nbrOfEpsilonTransitions();
        else if (this == FewestNeighboursFirst)
        {
            final Automata autNeighbours = new Automata();
            for (LabeledEvent event : aut.getAlphabet())
            {
                autNeighbours.addAutomata(eventToAutomataMap.get(event));
            }
            return autNeighbours.size()-1;
        }
        else if (this == RandomFirst)
            return (int) (Math.random()*10000.0);
        else
            throw new Exception("Unknown strategy.");
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
     * Special strategy? Does not return values.
     */
    public boolean isSpecial()
    {
        return type == Type.SPECIAL;
    }
    
    /**
     * The initial value for improvement comparisons.
     */
    public int worstValue()
    {
        return (maximize() ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }
}
