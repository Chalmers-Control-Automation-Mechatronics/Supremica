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

import java.util.ArrayList;
import java.util.Iterator;

import org.supremica.log.*;
import org.supremica.automata.State;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;


/**
 * Input class to ModularForbidder
 * @author patrik
 * @since December 14, 2009
 */
public class ModularForbidderInput
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(ModularForbidderInput.class);

    private ArrayList<SubState> subStates;

    public ModularForbidderInput()
    {
        subStates = new ArrayList<SubState>();
    }

    public void createSubState()
    {
        SubState ss = new SubState();
        subStates.add(ss);
    }

    /**
     * Creates a new sub-state based on some automata and their local-state indexes
     * @param automataToExt
     * @param localStateIndex
     */
    public void createSubState(Automata automataToExt, int[] localStateIndex)
    {
        createSubState();
        Iterator<Automaton> it = automataToExt.iterator();
        while(it.hasNext())
        {
            Automaton a = it.next();
            int s = localStateIndex[automataToExt.getAutomatonIndex(a)];
            addLocalStateIn(a, s, subStates.size()-1);
        }
    }

    /**
     * Adds a new local-state to subState with index subStateIndex
     * The local-state is based on an automaton and a stateIndex
     * @param automaton
     * @param stateIndex
     * @param subStateIndex
     */
    public void addLocalStateIn(Automaton automaton, int stateIndex, int subStateIndex)
    {
        SubState ss = (SubState) subStates.get(subStateIndex);
        State state = automaton.getStateWithIndex(stateIndex);
        ss.addLocalState(automaton, state);
    }

    public ArrayList<SubState> getSubStates()
    {
        return subStates;
    }
    
    /** 
     * @return all automata in all local-states = all sub-states
     */
    public Automata getTotalAutomata()
    {
        Automata a = new Automata();
        Iterator<SubState> it = subStates.iterator();
        while(it.hasNext())
        {
            a.addAutomata(it.next().getAutomataInSubState());
        }
        return a;
    }

    /**
     * Class for sub-states
     * Each sub-state comprises an ArrayList with localStates
     */
    public class SubState
    {
        private ArrayList<LocalState> localStates;

        public SubState()
        {
            localStates = new ArrayList<LocalState>();
        }

        /**
         * Creates a new local-state based on an automaton and a state
         * The local-state is added to the ArrayList
         * @param automaton
         * @param state
         */
        public void addLocalState(Automaton automaton, State state)
        {
            LocalState ls = new LocalState(automaton, state);
            localStates.add(ls);
        }

        public ArrayList<LocalState> getLocalStates()
        {
            return localStates;
        }

        /**
         * Returns all automata in all local-states for this sub-state
         * @return
         */
        public Automata getAutomataInSubState()
        {
            Automata a = new Automata();
            Iterator<LocalState> it = localStates.iterator();
            while(it.hasNext())
            {
                a.addAutomaton(it.next().getAutomaton());
            }
            return a;
        }
    }

    /**
     * Class for local-states
     * Each local-state comprises an Automaton and a State
     */
    public class LocalState
    {
        private Automaton a = null;
        private State s = null;

        public LocalState(Automaton automaton, State state)
        {
            set(automaton,state);
        }

        public State getState()
        {
            return s;
        }

        public Automaton getAutomaton()
        {
            return a;
        }

        public void set(Automaton automaton, State state)
        {
            this.a = automaton;
            this.s = state;
        }

    }

}
