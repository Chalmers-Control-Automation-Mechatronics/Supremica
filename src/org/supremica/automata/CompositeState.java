
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
package org.supremica.automata;

import java.util.*;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.log.*;

public class CompositeState
    extends State
{
    private static Logger logger = LoggerFactory.createLogger(CompositeState.class);
    
    /** The indices of the underlying states */
    
    //private int[] compositeIndices = null;
    
    /** The costs corresponding to the underlying states */
    private double[] compositeCosts = null;

    /**
     * The current costs in this state (only important in a composite state).
     * Depend on which automaton is to be fired as well as the path to this state.
     * See for ex. T.Liljenvalls Lic. (currentCosts = T_v).
     */
    private double[] currentCosts = null;
    private ArrayList composingStates;
    
    public CompositeState(State state)
    {
        super(state);
    }
    
    public CompositeState(String name)
    {
        super(name);
    }
    
    public CompositeState(int[] indices, AutomataSynchronizerHelper helper)
    {
        this("non_def", indices, helper);
    }
    
    public CompositeState(String name, int[] indices, AutomataSynchronizerHelper helper)
    {
        this(name);
        
        //setOwnerAutomaton(ownerAutomaton);
        initialize(indices, helper);
    }
    
    public void initialize(int[] indices, AutomataSynchronizerHelper helper)
    {
        composingStates = new ArrayList();
        
        // -2 since the last two indices correspond to something funny, not the nbrs of the underlying states.
        for (int i = 0; i < indices.length - 2; i++)
        {            
			//             theAutomata.getAutomatonAt(i).remapStateIndices();
            initComposingStates(helper.getIndexMap().getStateAt(helper.getAutomata().getAutomatonAt(i), indices[i]));
        }
        
        initCosts();
    }
    
    /**
     * Stores the underlying (non-composite) states.
     */
    private void initComposingStates(State currState)
    {
        if (currState instanceof CompositeState)
        {
            composingStates.addAll(((CompositeState) currState).getComposingStates());
        }
        else
        {
            composingStates.add(currState);
        }
    }
    
    /**
     * This method should only be called if the current state is initial in the
     * composed automaton. The currentCosts are then set to composedCosts.
     */
    public void initCosts()
    {
        compositeCosts = new double[composingStates.size()];
        currentCosts = new double[composingStates.size()];
        
        for (int i = 0; i < currentCosts.length; i++)
        {
            compositeCosts[i] = ((State) composingStates.get(i)).getCost();
            
            if (isInitial())
            {
                currentCosts[i] = compositeCosts[i];
            }
            else
            {
                currentCosts[i] = -1;
            }
        }
        
        accumulatedCost = 0;
    }
    
    /**
     *      Returns the indices of the underlying states.
     */
    
//      public int[] getCompositeIndices() { return compositeIndices; }
    
    /**
     *      Stores the indices of the constituting states.
     */
    
/*      protected void setCompositeIndices(int[] indices)
                {
                                if (compositeIndices == null)
                                                initialize(indices);
 
                                for (int i=0; i<compositeIndices.length; i++)
                                                compositeIndices[i] = indices[i];
                }
 */
    
    /**
     *      Returns the costs corresponding to the underlying states. Overrides
     *      the @link getCost() method in org.supremica.automata.State.java.
     */
    public double[] getCompositeCosts()
    {
        return compositeCosts;
    }
    
    /**
     *      Returns the current costs associated to this state (keeping in mind the
     *      path to this state).
     */
    public double[] getCurrentCosts()
    {
        return currentCosts;
    }
    
    /**
     *      Calculates and updates the currentCosts-vector and the accumulated cost.
     *      The costs in the previously visited state must be submitted as parameters.
     *      If the current state does not have any underlying cost(s) associated, the
     *      accumulatedCost of the previously visited state is kept.
     */
    public void updateCosts(double[] prevCurrentCosts, boolean[] firingAutomata, double prevAccumulatedCost)
    {
        double costAddition = 0;
        
        // The value of costAddition is set as the maximal cost for the firing/active automata
        for (int i = 0; i < firingAutomata.length; i++)
        {
            if ((firingAutomata[i] == true) && (prevCurrentCosts[i] > costAddition))
            {
                costAddition = prevCurrentCosts[i];
            }
        }
        
        // The currentCosts-vector is updated
        for (int i = 0; i < firingAutomata.length; i++)
        {
            if (firingAutomata[i] == false)
            {
                if (prevCurrentCosts[i] > -1)
                {
                    currentCosts[i] = Math.max(0, prevCurrentCosts[i] - costAddition);
                }
                else
                {
                    currentCosts[i] = -1;
                }
            }
            else
            {
                currentCosts[i] = compositeCosts[i];
            }
        }
        
        // The accumulatedCost is updated
        accumulatedCost = prevAccumulatedCost + costAddition;
    }
    
    /**
     *      Calculates the firing automata and other necessary parameters and calls
     *      updateCosts(int[], boolean[], int);
     */
    public void updateCosts(State state)
    {
        if (state instanceof CompositeState)
        {
            CompositeState prevState = (CompositeState) state;
            
            if (prevState.isUpdatingCosts())
            {
                if (prevState.isTimed())
                {
                    updateCosts(prevState.getCurrentCosts(), getFiringAutomata(prevState), prevState.getAccumulatedCost());
                }
                else
                {
                    accumulatedCost = prevState.getAccumulatedCost();
                }
            }
        }
        else
        {
            super.updateCosts(state);
        }
    }
    
    /**
     * Calculates the automaton/a that fired the transition from to this state.
     *
     * @param prevState CompositeState prevState - previously visited state
     */
    public boolean[] getFiringAutomata(CompositeState prevState)
    {
        boolean[] firingAutomata = new boolean[composingStates.size()];
        ArrayList prevComposingStates = prevState.getComposingStates();
        
        for (int i = 0; i < firingAutomata.length; i++)
        {
            if (composingStates.get(i).equals(prevComposingStates.get(i)))
            {
                firingAutomata[i] = false;
            }
            else
            {
                firingAutomata[i] = true;
            }
        }
        
        return firingAutomata;
    }
    
    /**
     *      This method checks if this state has underlying costs associated to it.
     *      Otherwise the updating of costs would not make sense.
     */
    private boolean isTimed()
    {
        boolean timed = false;
        
        for (int i = 0; i < compositeCosts.length; i++)
        {
            if (compositeCosts[i] > -1)
            {
                timed = true;
            }
        }
        
        return timed;
    }
    
    /**
     *      Checks if the cost updating has not been closed, i.e. if the path to
     *      this state is known.
     */
    private boolean isUpdatingCosts()
    {
        return (accumulatedCost > -1);
    }
    
    public ArrayList getComposingStates()
    {
        return composingStates;
    }
    
    /**
     *      Copies this state and its variables,
     *      setting the costs undefined (i.e. equal to -1 or 0).
     */
    
/*      public State copy() {
                                int size = this.composingStates.size();
 
                                CompositeState copiedState = new CompositeState(this);
 
                                copiedState.composingStates = new ArrayList(size);
                                copiedState.compositeCosts = new int[size];
                                copiedState.currentCosts = new int[size];
 
                                for (int i=0; i<size; i++) {
                                                copiedState.compositeCosts[i] = this.compositeCosts[i];
 
                                                if (isInitial())
                                                                copiedState.currentCosts[i] = this.currentCosts[i];
                                                else
                                                                copiedState.currentCosts[i] = UNDEF_COST;
                                }
 
                                if (isTimed())
                                                copiedState.accumulatedCost = MIN_COST;
                                else
                                                copiedState.accumulatedCost = UNDEF_COST;
 
                                copiedState.composingStates.addAll(0, this.composingStates);
 
                                return copiedState;
                }
 */
}
