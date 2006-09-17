
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

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;

// AutomataSelector a set of specs/sups/plants,
// For each spec/sup it returns that automaton together with the plants with which
// it shares uc-events (it never returns a spec with no plants)
// If closedSet == true the returned set is closed in that all plants that share
// uc-events with any plant in the set is also included
class AutomataSelectorFactory
{
    public static AutomataSelector getAutomataSelector(Automata automata, SynthesizerOptions options)
    throws Exception
    {
        //if (!options.oneEventAtATime)
        {
            return new PerSpecificationAutomataSelector(automata);
        }

        //return null;
    }
        
    /**
     * AutomataSelector a set of specs/sups/plants,
     * For each spec/sup it returns that automaton together with the plants with which
     * it shares uc-events (it never returns a spec with no plants)
     * If closedSet == true the returned set is closed in that all plants that share
     * uc-events with any plant in the set is also included
     */
    static class PerSpecificationAutomataSelector
        implements AutomataSelector
    {
        private static Logger logger = LoggerFactory.createLogger(AutomataSelector.class);
        private Automata globalSet;
        private Automata partialSet = new Automata();
        private Iterator specIterator;
        private Map<LabeledEvent,Automata> eventToAutomataMap;
        private int progress = 0;
        private int progressMax;
                
        public PerSpecificationAutomataSelector(Automata automata)
        throws Exception
        {
            globalSet = automata.getSpecificationAndSupervisorAutomata();
            specIterator = globalSet.iterator();
            progressMax = automata.size();
            
            eventToAutomataMap = AlphabetHelpers.buildUncontrollableEventToPlantsMap(automata);
        }
        
        /**
         * Returns a spec/supervisor together with the plants sharing uncontrollable events
         */
        public Automata next()
        {
            partialSet.clear();
            
            // Iterate over spec/sups (not really, we check this later)
            while (specIterator.hasNext())
            {
                Automaton spec = (Automaton) specIterator.next();
                assert (spec.isSupervisor() || spec.isSpecification());
                
                progress++;
                
                // Examine uncontrollable events in spec and select plants accordingly
                partialSet.addAutomaton(spec);
                logger.debug("AutomataSelector added spec/sup " + spec.getName());
                
                // Add plants for all uncontrollable events
                for (LabeledEvent event : spec.getAlphabet())
                {
                    // Add plants for uncontrollable events only
                    if (!event.isControllable())
                    {
                        addPlants(event);
                    }
                }
                
                // Did we find any plants?
                if (partialSet.size() > 1)
                {
                    // Nice! Then we're ready
                    break;
                }
                else
                {
                    // Have another go... get a new spec etc...
                    partialSet.clear();
                }
            }
            
            // This will be empty (cleared) only when we're done!
            return partialSet;
        }
        
        /**
         * To the current selection of spec and plants, add all plants that have this event
         */
        public Automata addPlants(LabeledEvent currEvent)
        {
            if (eventToAutomataMap.get(currEvent) != null)
            {
                partialSet.addAutomata(eventToAutomataMap.get(currEvent));
            }
            
            return partialSet;
        }
        
        /**
         * To your current selection of spec and plants, add all plants that have these events
         */
        public Automata addPlants(Alphabet events)
        {
            for (LabeledEvent event : events)
            {
                addPlants(event);
            }
            
            return partialSet;
        }
        
        /**
         * Return the sequential number of the last supervisor/spec considered or 0 if no
         * supervisor/spec has ever been considered.
         */
        public int getProgress()
        {
            return progress/progressMax;
        }
    }
}
