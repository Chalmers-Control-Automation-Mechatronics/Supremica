
/************************ EnumerateStates.java ***************/
// Walks the state set and renames the states as <prfx><num>
// where <prfx> is given and <num> is calculated. Initial state
// is always numbered 0
// Note that the original automata are altered
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.State;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;

public class EnumerateStates
{
    final Automata automata;
    final StringBuilder prefix;
    final int prefixlen;
    
    /**
     * Creates enumerator for the supplied automata.
     */
    public EnumerateStates(Automata automata, final String prefix)
    {
        this.automata = automata;
        this.prefix = new StringBuilder(prefix);
        this.prefixlen = prefix.length();
    }

    /**
     * Makes sure the enumeration is made.
     */
    public void execute()
    {
        final Iterator<Automaton> autit = automata.iterator();
        
        while (autit.hasNext())
        {
            enumerate(autit.next());
        }
    }
    
    /**
     * Enumerates the states in automaton.
     */
    private void enumerate(final Automaton automaton)
    {
        automaton.beginTransaction();
        prefix.append("0");
        
        final State init = automaton.getInitialState();
        
        if (init != null)
        {
            init.setName(prefix.toString());
        }
        
        prefix.setLength(prefixlen);
        
        int num = 1;
        final Iterator<State> stateit = automaton.stateIterator();
        while (stateit.hasNext())
        {
            final State state = stateit.next();
            
            if (!state.isInitial())
            {
                prefix.append(num);
                state.setName(prefix.toString());
                
                num++;
                
                prefix.setLength(prefixlen);
            }
        }
        
        automaton.invalidate();
        automaton.endTransaction();
    }
}
