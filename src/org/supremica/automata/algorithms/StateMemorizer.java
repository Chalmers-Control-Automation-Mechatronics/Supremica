
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

// Skr�p
import org.supremica.log.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Hashtable;

public final class StateMemorizer
{    // Skr�p
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(StateMemorizer.class);
    
    private class HashtableHolder
    {
        private Hashtable<StateHolder, StateHolder> stateHash;
        private int[] automataIndices;
        
        // Jikes 1.15 workaround
        public HashtableHolder(Hashtable<StateHolder, StateHolder> stateHash, int[] automataIndices)
        {
            this.stateHash = stateHash;
            this.automataIndices = automataIndices;
        }
        
        @SuppressWarnings("unused")
		private Hashtable<StateHolder, StateHolder> getHashtable()
        {
            return stateHash;
        }
        
        private int[] getAutomataIndices()
        {
            return automataIndices;
        }
    }
    
    private LinkedList<HashtableHolder> tableList = new LinkedList<HashtableHolder>();
    private Hashtable<StateHolder, Hashtable<StateHolder, StateHolder>> tableHash = new Hashtable<StateHolder, Hashtable<StateHolder, StateHolder>>(1023);
    @SuppressWarnings("unused")
	private int[] automataIndices;
    
    public StateMemorizer()
    {}
    
    public StateMemorizer(int[] automataIndices)
    {
        this.automataIndices = automataIndices;
    }
    
    public void add(int[] automataIndices, int[] fullState, int problemPlant, int problemEvent)
    {
        int[] stateIndices = stateCompression(automataIndices, fullState);
        Hashtable<StateHolder, StateHolder> stateHash = tableHash.get(new StateHolder(automataIndices));
        
        if (stateHash == null)
        {
            stateHash = new Hashtable<StateHolder, StateHolder>(47);
            
            tableHash.put(new StateHolder(automataIndices), stateHash);
            tableList.add(new HashtableHolder(stateHash, automataIndices));
        }
        
        stateHash.put(new StateHolder(stateIndices), new StateHolder(stateIndices, problemPlant, problemEvent));
    }
    
    public void remove(int[] automataIndices, int[] fullState)
    {
        int[] stateIndices = stateCompression(automataIndices, fullState);
        Hashtable<?, ?> stateHash = tableHash.get(new StateHolder(automataIndices));
        
        stateHash.remove(new StateHolder(stateIndices));
    }
    
    // Iterates through the tableList and examines all hashtables for the state...
    public boolean contains(int[] fullState)
    {
        HashtableHolder hashtableHolder;
        Iterator<HashtableHolder> tableIterator = tableList.iterator();
        
        while (tableIterator.hasNext())
        {
            hashtableHolder = tableIterator.next();
            
            if (contains(hashtableHolder.getAutomataIndices(), fullState))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean contains(int[] automataIndices, int[] fullState)
    {
        int[] stateIndices = stateCompression(automataIndices, fullState);
        Hashtable<?, ?> stateHash = tableHash.get(new StateHolder(automataIndices));
        
        return stateHash.containsKey(new StateHolder(stateIndices));
    }
    
    private StateHolder getStateHolder(int[] automataIndices, int[] fullState)
    {
        int[] stateIndices = stateCompression(automataIndices, fullState);
        Hashtable<?, ?> stateHash = tableHash.get(new StateHolder(automataIndices));
        
        return (StateHolder) stateHash.get(new StateHolder(stateIndices));
    }
    
    // Returns indices of the states in the automata represented by the indices in automataIndices
    private int[] stateCompression(int[] automataIndices, int[] fullState)
    {
        int[] stateIndices = new int[automataIndices.length];
        
        for (int i = 0; i < automataIndices.length; i++)
        {
            stateIndices[i] = fullState[automataIndices[i]];
        }
        
        return stateIndices;
    }
    
    // Marks a state as found if found, see the method clean.
    public boolean find(int[] automataIndices, int[] fullState)
    {
        if (contains(automataIndices, fullState))
        {
            getStateHolder(automataIndices, fullState).setFound(true);
        }
        else
        {
            return false;
        }
        
        return true;
    }
    
    // Count uncontrollable states
    public int size()
    {
        int count = 0;
        HashtableHolder hashtableHolder;
        Iterator<HashtableHolder> tableIterator = tableList.iterator();
        
        while (tableIterator.hasNext())
        {
            hashtableHolder = tableIterator.next();
            count = count + size(hashtableHolder.getAutomataIndices());
        }
        
        return count;
    }
    
    // Count uncontrollable states in single hashtable
    public int size(int[] automataIndices)
    {
        
        // return ((Hashtable) tableHash.get(new StateHolder(automataIndices))).size();
        Hashtable<?, ?> hashtable = tableHash.get(new StateHolder(automataIndices));
        
        if (hashtable != null)
        {
            return hashtable.size();
        }
        else
        {
            return 0;
        }
    }
    
    public void clear(int[] automataIndices)
    {
        
        // ((Hashtable) tableHash.get(new StateHolder(automataIndices))).clear();
        Hashtable<StateHolder, StateHolder> hashtable = tableHash.get(new StateHolder(automataIndices));
        
        tableList.remove(new HashtableHolder(hashtable, automataIndices));
        
        hashtable = null;
    }
    
    // Removes states that are not marked as "found" in
    // the hashtable associated with automataIndices
    public void clean(int[] automataIndices)
    {
        Iterator<?> stateIterator = iterator(automataIndices);
        StateHolder stateHolder;
        
        while (stateIterator.hasNext())
        {
            stateHolder = (StateHolder) stateIterator.next();
            
            if (!stateHolder.isFound())
            {
                stateIterator.remove();
            }
            else
            {
                stateHolder.setFound(false);
            }
        }
    }
    
    public Iterator<?> iterator(int[] automataIndices)
    {
        Hashtable<?, ?> stateHash = tableHash.get(new StateHolder(automataIndices));
        
        return stateHash.values().iterator();
    }
}
