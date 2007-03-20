
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
        if (!options.oneEventAtATime)
        {
            // Maybe it's just as easy to put it all in the same 
            // class anyway... the factory was unnecessary...
            return new PerSpecificationAutomataSelector(automata, options);
        }
        else
        {
            return new PerEventAutomataSelector(automata, options);
        }

        //return null;
    }
        
    static class PerEventAutomataSelector
        extends PerSpecificationAutomataSelector
    {
        /**
         * Map keeping track of which uncontrollable events each spec 
         * has left that has not been taken care of.
         */
        private Map<Automaton,Alphabet> specToEventsLeftMap;

        /**
         * The spec currently being examined.
         */
        private Automaton spec;
        
        public PerEventAutomataSelector(Automata automata, SynthesizerOptions options)
        throws Exception
        {
            super(automata, options);
            
            // Initiate spec to events map
            specToEventsLeftMap = new TreeMap<Automaton,Alphabet>();
            for (Automaton spec : super.globalSet)
            {
                System.out.println("Added " + spec + " with " + spec.getAlphabet().getUncontrollableAlphabet());
                
                // Add spec and its uncontrollable alphabet, assert that 
                Alphabet alreadyThere = specToEventsLeftMap.put(spec, spec.getAlphabet().getUncontrollableAlphabet());
                assert(alreadyThere == null);
            }
        }
        
        public Automata next()
        {
            super.partialSet.clear();

            for (Map.Entry<Automaton,Alphabet> entry : specToEventsLeftMap.entrySet())
            {
                // If there are no events left, we don't need synthesis
                if (entry.getValue().size() == 0)
                {
                    // Then we're done
                    continue;
                }
                
                // This is the spec
                spec = entry.getKey();
                super.partialSet.addAutomaton(spec);
                
                // This is an event that has not been taken care of
                LabeledEvent event = entry.getValue().iterator().next();
                // After this we're done examining this event!
                entry.getValue().removeEvent(event);
                // Build alphabet
                Alphabet ucAlpha = new Alphabet();
                ucAlpha.addEvent(event);

                // Add plant
                if (super.options.addOnePlantAtATime)
                    // Add one plant
                    addPlant(ucAlpha);
                else
                    // Add all plants
                    addPlants(ucAlpha);  
    
                // Did we find any plants?
                if (super.partialSet.size() > 1)
                {
                    // Nice! Then we're ready
                    break;
                }
                
                super.partialSet.clear();                
            }
            
            System.out.println("The task is " + super.partialSet);
            
            return super.partialSet;
        }

        public Automata addPlant(Alphabet alpha)
        {
            Automata result = super.addPlant(alpha);
            removeCoveredEvents();
            
            System.out.println("The task is now " + super.partialSet);

            return result;
        }
        
        public Automata addPlants(Alphabet alpha)
        {
            Automata result = super.addPlants(alpha);
            removeCoveredEvents();

            System.out.println("The task is now " + super.partialSet);

            return result;
       }

        private void removeCoveredEvents()
        {
            // Remove all events that are now covered
            Alphabet specUC = specToEventsLeftMap.get(spec);
            for (Automaton automaton : super.partialSet.getPlantAutomata())
            {
                for (LabeledEvent event : automaton.getAlphabet().getUncontrollableAlphabet())
                {
                    if (specUC.contains(event))
                        specUC.removeEvent(event);
                }
            }
            System.out.println("These events are not covered: " + specUC + " for spec " + spec);
        }
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
        private Map<LabeledEvent,Automata> ucEventToPlantMap;
        private int progress = 0;
        private int progressMax;
        private SynthesizerOptions options;
                
        /**
         * Selects specifications and plants depending on how uncontrollable 
         * events are shared.
         */
        public PerSpecificationAutomataSelector(Automata automata, SynthesizerOptions options)
        throws Exception
        {
            globalSet = automata.getSpecificationAndSupervisorAutomata();
            specIterator = globalSet.iterator();
            progressMax = automata.size();
            this.options = options;
            
            ucEventToPlantMap = AlphabetHelpers.buildUncontrollableEventToAutomataMap(automata.getPlantAutomata());
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
                Alphabet ucAlpha = spec.getAlphabet().getUncontrollableAlphabet();
                /*
                if (options.addOnePlantAtATime)
                    // Add one plant
                    addPlant(ucAlpha);
                else
                    // Add all plants
                    addPlants(ucAlpha);
                */
                addPlants(ucAlpha); 
                
                // Did we find any plants?
                if (partialSet.size() > 1)
                {
                    // Nice! Then we're ready
                    break;
                }

                // Have another go... get a new spec etc...
                partialSet.clear();
            }
            
            // This will be empty (cleared) only when we're done!
            return partialSet;
        }
        
        /**
         * To your current selection of spec and plants, add all plants that have these events..
         */
        public Automata addPlants(Alphabet events)
        {
            for (LabeledEvent event : events)
            {
                if (ucEventToPlantMap.get(event) != null)
                {
                    partialSet.addAutomata(ucEventToPlantMap.get(event));
                }
            }
            
            return partialSet;
        }
        
        /**
         * To your current selection of spec and plants, add one plant that has at least one 
         * of these events and that has not been added before, if there is one...
         */
        public Automata addPlant(Alphabet events)
        {
            for (LabeledEvent event : events)
            {
                if (ucEventToPlantMap.get(event) != null)
                {
                    Automata sharers = ucEventToPlantMap.get(event);
                    for (Automaton plant : sharers)
                    {
                        if (!partialSet.containsAutomaton(plant))
                        {
                            partialSet.addAutomaton(plant);
                            return partialSet;
                        }
                    }
                }
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
