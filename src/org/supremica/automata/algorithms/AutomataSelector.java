
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
class AutomataSelector
{
    private static Logger logger = LoggerFactory.createLogger(AutomataSelector.class);
    private Automata globalSet;
    private Automata partialSet = new Automata();
    private Iterator specIterator;
    private EventToAutomataMap eventToAutomataMap;
    private int progress = 0;
    private boolean seenSpec = false;    // keep track of wether no spec exists, may need to do some job anyway
    
    public AutomataSelector(Automata globalSet)
    throws Exception
    {
        this.globalSet = globalSet;
        this.specIterator = globalSet.iterator();
        
        eventToAutomataMap = AlphabetHelpers.buildUncontrollableEventToPlantsMap(globalSet);
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
            Automaton currSupervisorAutomaton = (Automaton) specIterator.next();
            
            // Is this really a sup/spec?
            if (currSupervisorAutomaton.isSupervisor() || currSupervisorAutomaton.isSpecification())
            {
                seenSpec = true;    // yes, we've found a spec/sup
                progress++;
                
                // Examine uncontrollable events in currSupervisorAutomaton and select plants accordingly
                partialSet.addAutomaton(currSupervisorAutomaton);
                logger.debug("AutomataSelector::Added spec/sup " + currSupervisorAutomaton.getName());
                
                //ArrayList eventList = new ArrayList(currSupervisorAutomaton.eventCollection());
                //while (!eventList.isEmpty())
                for (Iterator<LabeledEvent> it = currSupervisorAutomaton.eventIterator(); it.hasNext(); )
                {
                    //LabeledEvent currEvent = (LabeledEvent) eventList.remove(0);
                    LabeledEvent currEvent = it.next();
                    
                    // Add plants for uncontrollable events only
                    if (!currEvent.isControllable())
                    {
                        addPlants(currEvent);
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
                    // Have another go...
                    partialSet.clear();
                }
            }
        }
        
        // This will be empty (cleared) only when we're done!
        return partialSet;
    }
    
    /**
     * To your current selection of spec and plants, add all plants that have this event
     */
    public Automata addPlants(LabeledEvent currEvent)
    {
        if (eventToAutomataMap.get(currEvent) != null)
        {
            partialSet.addAutomata(eventToAutomataMap.get(currEvent));
            
                        /*
                        Iterator plantIterator = eventToAutomataMap.get(currEvent).iterator();
                        while (plantIterator.hasNext())
                        {
                                Automaton currPlantAutomaton = (Automaton) plantIterator.next();
                         
                                // This check is performed in eventToAutomataMap
                                // if (currPlantAutomaton.getType() == AutomatonType.Plant)
                                if (!partialSet.containsAutomaton(currPlantAutomaton))
                                {
                                        partialSet.addAutomaton(currPlantAutomaton);
                                        logger.debug("AutomataSelector::Added plant " + currPlantAutomaton.getName());
                         
                                        // closedSet stuff removed
                                }
                        }
                         */
        }
        
        return partialSet;    // return the updated set
    }
    
    /**
     * To your current selection of spec and plants, add all plants that have these events
     */
    public Automata addPlants(Alphabet events)
    {
        for (Iterator it = events.iterator(); it.hasNext(); )
        {
            addPlants((LabeledEvent) it.next());
        }
        
        return partialSet;
    }
    
    /**
     * Return wether we've seen a spec/sup or not
     */
    boolean hadSpec()
    {
        return seenSpec;
    }
    
    /**
     * Return the sequential number of the last supervisor/spec considered or 0 if no
     * supervisor/spec has ever been considered.
     */
    int getProgress()
    {
        return progress;
    }
}
