
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

import javax.swing.text.html.Option;
import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.minimization.AutomataMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationHelper;
import org.supremica.automata.BDD.BDDVerifier;
import org.supremica.util.BDD.*;
import org.supremica.properties.Config;

/**
 * For performing verification. Uses AutomataSynchronizerExecuter for much of the actual verification work.
 *
 * @author  ka
 * @since  November 28, 2001
 * @see  AutomataSynchronizerExecuter
 */
public class AutomataVerifier
    implements Stoppable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataVerifier.class);
    private Automata theAutomata;
    
    // MF Started puting in all these timer.start/stop but...
    // private ActionTimer timer = new ActionTimer();
    
    /**
     * Map from an uc LabeledEvent to the Set of plant Automaton-objects that contain this event
     *
     *@see  AlphabetAnalyzer
     */
    private Map<LabeledEvent,Automata> uncontrollableEventToPlantsMap = null;
    private AutomataSynchronizerHelper synchHelper;
    private ArrayList synchronizationExecuters = new ArrayList();
    private StateMemorizer potentiallyUncontrollableStates;
    
    // Used by findUncontrollableStates
    private AutomataSynchronizerHelper uncontrollabilityCheckHelper;
    private ArrayList uncontrollabilityCheckExecuters = new ArrayList();
    
    // Used in excludeUncontrollableStates
    private int stateAmountLimit;
    private int stateAmount;
    private int attempt;
    
    /**
     * Determines if more detailed information on the progress of things should be displayed.
     *
     *@see SynchronizationOptions
     */
    private VerificationOptions verificationOptions;
    private SynchronizationOptions synchronizationOptions;
    private MinimizationOptions minimizationOptions;
    
    /** For stopping execution. */
    private ExecutionDialog executionDialog = null;
    private boolean stopRequested = false;
    private Stoppable threadToStop = null;
    
    /** For error message when Supremica can't be certain on the answer. */
    private boolean failure = false;
    
    public AutomataVerifier(Automata theAutomata, VerificationOptions verificationOptions, SynchronizationOptions synchronizationOptions, MinimizationOptions minimizationOptions)
    throws IllegalArgumentException, Exception
    {
        Automaton currAutomaton;
        
        this.theAutomata = new Automata(theAutomata);
        this.verificationOptions = verificationOptions;
        this.synchronizationOptions = synchronizationOptions;
        this.minimizationOptions = minimizationOptions;
    }
    
    /**
     * Evaluates the supplied options.
     *
     *@return null if everything is OK, otherwise returns a String describing the problem.
     */
    public static String validOptions(Automata theAutomata, VerificationOptions verificationOptions)
    {
        // At least one automaton
        if (theAutomata.size() < 1)
        {
            return "At least one automaton must be selected.";
        }
        
        // Must be prioritized?
        if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.MODULAR ||
            verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMPOSITIONAL)
        {
            if (!theAutomata.isAllEventsPrioritized())
            {
                return "All events must be prioritized.";
            }
        }
        
        // MODULAR algorithms demand systems with more than one module...
        if ((verificationOptions.getAlgorithmType() == VerificationAlgorithm.MODULAR) && (theAutomata.size() < 2))
        {
            logger.warn("Using monolithic algorithm instead, since the system is not modular.");
            verificationOptions.setAlgorithmType(VerificationAlgorithm.MONOLITHIC);
        }
        
        // Check Controllability
        if (verificationOptions.getVerificationType() == VerificationType.CONTROLLABILITY)
        {
            if (theAutomata.hasNoPlants() || theAutomata.hasNoSpecificationsAndSupervisors())
                return "At least one plant and one specification/supervisor must be selected.";
        }
        
        // Check Nonblocking
        if (verificationOptions.getVerificationType() == VerificationType.NONBLOCKING)
        {
            if (!theAutomata.hasAcceptingState())
            {
                return "Some automaton has no marked states. This system is trivially blocking!";
            }
        }
        
        // Check Language Inclusion
        if (verificationOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
        {
            if ((verificationOptions.getInclusionAutomata() != null) &&
                (verificationOptions.getInclusionAutomata().size() < 1))
            {
                return "At least one automaton must be unselected.";
            }
            
            // theAutomata = theAutomata.add(verificationOptions.getInclusionAutomata());
        }
        
        // Everything seems OK!
        return null;
    }
    
    /**
     * This is an attempt to clean up this interface.
     */
    public boolean verify()
    throws UnsupportedOperationException
    {
        try
        {
            // Find out what should be done and do it!
            if (verificationOptions.getVerificationType() == VerificationType.CONTROLLABILITY ||
                verificationOptions.getVerificationType() == VerificationType.INVERSECONTROLLABILITY ||
                verificationOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
            {
                // All of these verification types use the same algorithm. Just need to do some preparation first...
                
                // Inverse controllability? Invert controllability!
                if (verificationOptions.getVerificationType() == VerificationType.INVERSECONTROLLABILITY)
                {
                    // Invert controllability and plant/spec status
                    prepareForInverseControllability();
                }
                else if (verificationOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
                {
                    // Treat the unselected automata as plants (and the rest as supervisors, implicitly)
                    prepareForLanguageInclusion(verificationOptions.getInclusionAutomata());
                }
                
                // We're gonna do some synchronization! Initialize a synchronization helper!
                // Only some of the below algorithms use this helper?
                synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);
                synchHelper.setExecutionDialog(executionDialog);
                
                // Work!
                if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.MONOLITHIC)
                {
                    return monolithicControllabilityVerification();
                }
                else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.MODULAR)
                {
                    return modularControllabilityVerification();
                }
                else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMPOSITIONAL ||
                    verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMBINED)
                {
                    return compositionalControllabilityVerification();
                }
                else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.BDD)
                {
                    return BDDControllabilityVerification(theAutomata);
                }
                else
                {
                    throw new UnsupportedOperationException("The selected algorithm is not implemented");
                }
            }
            else if (verificationOptions.getVerificationType() == VerificationType.CONTROLLABILITYNONBLOCKING)
            {
                if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMPOSITIONAL ||
                    verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMBINED)
                {
                    return compositionalControllabilityNonblockingVerification();
                }
                else
                {
                    throw new UnsupportedOperationException("The selected algorithm is not implemented");
                }
            }
            else if (verificationOptions.getVerificationType() == VerificationType.NONBLOCKING)
            {
                if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.MONOLITHIC)
                {
                    // We're gonna do some serious synchronization! Initialize a synchronization helper!
                    synchronizationOptions.setForbidUncontrollableStates(false);
                    synchronizationOptions.setExpandForbiddenStates(true);
                    synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);
                    synchHelper.setExecutionDialog(executionDialog);
                    
                    // Work!
                    return monolithicNonblockingVerification();
                }
                 else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.MONOLITHICBDD)
                {
                    return monolithicBDDNonblockingVerification();
                }
                else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.BDD)
                {
                    return BDDNonblockingVerification();
                }
                else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMPOSITIONAL ||
                    verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMBINED)
                {
                    return compositionalNonblockingVerification();
                }
                else
                {
                    throw new UnsupportedOperationException("The selected algorithm is not implemented");
                }
            }
            else
            {
                throw new UnsupportedOperationException("The selected type of verification " +
                    "is not implemented");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception in AutomataVerifier: " + e);
            
            throw new RuntimeException(e);    // Try change this later
        }
    }
    
    /**
     * Prepares the helper and the automataindexform for inverse controllability...
     */
    public void prepareForInverseControllability()
    {
        // Invert the properties!
        for (Automaton aut: theAutomata)
        {
            // Invert automaton type
            if (aut.isPlant())
            {
                aut.setType(AutomatonType.SPECIFICATION);
            }
            else
            {
                aut.setType(AutomatonType.PLANT);
            }
            
            // Invert event controllability
            Alphabet alpha = aut.getAlphabet();
            for (LabeledEvent ev: alpha)
            {
                ev.setControllable(!ev.isControllable());
            }
        }
    }
    
    /**
     * Prepares the helper and the automataindexform for language inclusion...
     *
     *@param inclusionAutomata The automata that should be verified for inclusion
     */
    public void prepareForLanguageInclusion(Automata inclusionAutomata)
    throws Exception
    {
        if (inclusionAutomata == null)
        {
            throw new IllegalArgumentException("Inclusion automata must be non null for language inclusion verification.");
        }
        
        // Make a copy and modify!
        
        // We shall verify   L(autA) \subseteq L(autB)
        Automata autA = new Automata(inclusionAutomata); // The automata that are not selected
        Automata autB = new Automata(theAutomata); // The selected automata
        
        // Make autA plants
        for (Automaton aut: autA)
        {
            aut.setType(AutomatonType.PLANT);
            Alphabet alpha = aut.getAlphabet();
            for (LabeledEvent ev: alpha)
            {
                ev.setControllable(false);
            }
        }
        
        // Make autB specifications
        for (Automaton aut: autB)
        {
            aut.setType(AutomatonType.SPECIFICATION);
            Alphabet alpha = aut.getAlphabet();
            for (LabeledEvent ev: alpha)
            {
                ev.setControllable(false);
            }
        }
        
        theAutomata = new Automata();
        theAutomata.addAutomata(autA);
        theAutomata.addAutomata(autB);
        
        /*
        // Maybe we should just make a copy of the whole project and modify what needs to
        // be modified right there instead? Yup.
         
        theAutomata.addAutomata(inclusionAutomata);
         
        // After these preparations, controllability verification verifies language inclusion
        synchHelper.getAutomataIndexForm().defineTypeIsPlantTable(inclusionAutomata);
         
        uncontrollableEventToPlantsMap = AlphabetHelpers.buildEventToAutomataMap(inclusionAutomata);
         
        // This last one is not really good... we'd like to do this only once! Perhaps
        // a switch in the synchronizeroptions or verificationoptions instead? FIXA!!
        synchHelper.considerAllEventsUncontrollable();
         */
    }
       
    /**
     * Performs modular controllability verification on theAutomata..
     *
     *@return  true if controllable, false if not or false (with error message) if don't know.
     *@exception  Exception Description of the Exception
     *@see  AutomataVerificationWorker
     */
    private boolean modularControllabilityVerification()
    throws Exception
    {
        if (uncontrollableEventToPlantsMap == null)
        {
            uncontrollableEventToPlantsMap = AlphabetHelpers.buildUncontrollableEventToAutomataMap(theAutomata.getPlantAutomata());
        }
        
        potentiallyUncontrollableStates = synchHelper.getStateMemorizer();
        
        Automata selectedAutomata = new Automata();
        boolean allModulesControllable = true;
        boolean[] typeIsSupSpecTable = synchHelper.getAutomataIndexForm().getTypeIsSupSpecTable();
        boolean[] controllableEventsTable = synchHelper.getAutomataIndexForm().getControllableEventsTable();
        AutomataIndexMap indexMap = synchHelper.getIndexMap();
        
        // Iterate over supervisors/specifications
        loop: for (Automaton supervisor: theAutomata)
        {
            // To enable the overriding the AutomatonType of automata we use typeIsSupSpecTable!
            // if ((supervisor.getType() == AutomatonType.Supervisor) || (supervisor.getType() == AutomatonType.SPECIFICATION))
            // if (!typeIsPlantTable[supervisor.getIndex()])
            if (typeIsSupSpecTable[indexMap.getAutomatonIndex(supervisor)])
            {
                // This is a relevant automaton!
                selectedAutomata.addAutomaton(supervisor);
                
                // Examine uncontrollable events in supervisor
                // and select plants containing these events
                for (LabeledEvent event : supervisor.getAlphabet())
                {
                    // To enable overriding the controllability status of events!
                    //if (!event.isControllable())
                    if (!controllableEventsTable[indexMap.getEventIndex(event)])
                    {
                        // Note that in the language inclusion case, the
                        // uncontrollableEventToPlantsMap has been adjusted...
                        if (uncontrollableEventToPlantsMap.get(event) != null)
                        {
                            // Iterate over the plants and add them to selectedAutomata
                            //for (Iterator plantIt = ((Set) uncontrollableEventToPlantsMap.get(event)).iterator();
                            for (Automaton plant : uncontrollableEventToPlantsMap.get(event))
                            {
                                if (!selectedAutomata.containsAutomaton(plant))
                                {
                                    selectedAutomata.addAutomaton(plant);
                                }
                            }
                            
                            if (verificationOptions.getOneEventAtATime())
                            {
                                if (stopRequested)
                                {
                                    return false;
                                }
                                
                                if (selectedAutomata.size() > 1)
                                {
                                    // Check module
                                    allModulesControllable &= moduleIsControllable(selectedAutomata);
                                    
                                    // Stop if uncontrollable
                                    if (!allModulesControllable)
                                    {
                                        //logger.verbose("Uncontrollable state found.");
                                        break loop;
                                    }
                                    
                                    // Clean selectedAutomata before continuing
                                    selectedAutomata.clear();
                                    selectedAutomata.addAutomaton(supervisor);
                                }
                            }
                        }
                    }
                }
                
                if (!verificationOptions.getOneEventAtATime())
                {
                    if (stopRequested)
                    {
                        return false;
                    }
                    
                    if (selectedAutomata.size() > 1)
                    {
                        // Check module
                        allModulesControllable &= moduleIsControllable(selectedAutomata);
                        
                        // Stop if uncontrollable
                        if (!allModulesControllable)
                        {
                            //logger.verbose("Uncontrollable state found.");
                            break loop;
                        }
                    }
                }
                
                // Clean selectedAutomata before continuing
                selectedAutomata.clear();
            }
        }
        
        // Did the loop finish without failure?
        if (failure)
        {
            logger.warn("Supremica's modular verification algorithm can't solve this " +
                "problem. Try the monolithic or BDD algorithm instead. There are " +
                potentiallyUncontrollableStates.size() +
                " states that perhaps makes this system uncontrollable.");
            
            return false;
        }
        
        //return "\\texttt{NAME} & " + initialNbrOfAutomata + " & SIZE & " + mostStates + " & " + mostTransitions + " & TIME & BLOCK & " + AutomatonMinimizer.getWodesStatisticsLaTeX() + " & ALGO1 & ALGO2 \\\\";
        message = "\\texttt{NAME} & " + theAutomata.size() + " & SIZE & " + synchHelper.getStatisticsLineLatex() + " & TIME & " + !allModulesControllable;

        return allModulesControllable;
    }
    
    /**
     * Performs monolithic controllablity verification on one module using AutomataSynchronizerExecuter.
     *
     *@param  selectedAutomata the automata that should be verified
     *@return  true if controllable, false if not or false if don't know.
     *@exception  Exception Description of the Exception
     *@see  AutomataSynchronizerExecuter
     */
    private boolean moduleIsControllable(Automata selectedAutomata)
    throws Exception
    {
        // Clear the hash-table and set some variables in the synchronization helper
        synchHelper.clear();
        synchHelper.setRememberUncontrollable(true);
        synchHelper.initialize();
        
        if (stopRequested)
        {
            return false;
        }
        
        // Initialize the synchronizationExecuters
        synchronizationExecuters.clear();
        
        for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
        {
            AutomataSynchronizerExecuter currSynchronizationExecuter =
                new AutomataSynchronizerExecuter(synchHelper);
            
            synchronizationExecuters.add(currSynchronizationExecuter);
        }
        
        // Start all the synchronization executers and wait for completion
        // For the moment we assume that we only have one thread
        for (int i = 0; i < synchronizationExecuters.size(); i++)
        {
            AutomataSynchronizerExecuter currExec =
                (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
            
            currExec.selectAutomata(selectedAutomata);
            currExec.start();
        }
        
        ((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
        
        if (stopRequested)
        {
            return false;
        }
        
        // The name of the "synchronized" automata
        StringBuffer automataNames = new StringBuffer();
        
        if (Config.VERBOSE_MODE.isTrue())
        {
            // For printing the names of the automata in selectedAutomata
            for (Iterator autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                automataNames = automataNames.append(((Automaton) autIt.next()).getName());
                
                if (autIt.hasNext())
                {
                    automataNames = automataNames.append("||");
                }
            }
        }
        
        // Was the result uncontrollable?
        if (!synchHelper.getAutomataIsControllable())
        {
            AutomataIndexMap indexMap = synchHelper.getIndexMap();
            // Try to add some more automata
            // Make array with indices of selected automata to remember which were originally selected
            int[] automataIndices = new int[selectedAutomata.size()];
            int i = 0;
            
            for (Iterator<Automaton> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                automataIndices[i++] = indexMap.getAutomatonIndex(autIt.next());
            }
            
            if (Config.VERBOSE_MODE.isTrue())
            {
                String states;
                int size = potentiallyUncontrollableStates.size(automataIndices);
                
                if (size == 1)
                {
                    states = "one state";
                }
                else if (size == 2)
                {
                    states = "two states";
                }
                else
                {
                    states = size + " states";
                }
                
                logger.info("'" + automataNames + "' has " + states + " that might be uncontrollable...");
            }
            
            // Get a sorted array of indexes of automata with similar alphabets
            int[] similarAutomata = findSimilarAutomata(theAutomata, selectedAutomata);
            if (similarAutomata == null)
            {
                // This never happens?
                
                // There are no similar automata, this module must be uncontrollable
                if (Config.VERBOSE_MODE.isTrue())
                {
                    // Print the uncontrollable state(s)...
                    synchHelper.printUncontrollableStates(automataIndices);
                }
                
                return false;
            }
            
            if (Config.VERBOSE_MODE.isTrue())
            {
                logger.info("There are " + similarAutomata.length + " automata with similar alphabets...");
            }
            
            // Make nbrOfAttempts attempts on prooving controllability and
            // uncontrollability alternatingly and then give up
            int nbrOfAttempts = verificationOptions.getNbrOfAttempts();
            stateAmount = 1;
            for (attempt = 1; attempt <= nbrOfAttempts; attempt++)
            {
                logger.debug("Attempt number " + attempt + ".");
                
                // Have we already added all similar automata?
                if (similarAutomata.length == selectedAutomata.size() - automataIndices.length)
                {
                    // Try to find more similarities
                    int[] moreSimilarAutomata = findSimilarAutomata(theAutomata, selectedAutomata);
                    if (moreSimilarAutomata != null)
                    {
                        int[] newSimilarAutomata = new int[similarAutomata.length + moreSimilarAutomata.length];
                        if (Config.VERBOSE_MODE.isTrue())
                        {
                            logger.info("All similar automata are already added, trying to add some more...");
                        }
                        
                        System.arraycopy(similarAutomata, 0, newSimilarAutomata, 0, similarAutomata.length);
                        System.arraycopy(moreSimilarAutomata, 0, newSimilarAutomata, similarAutomata.length, moreSimilarAutomata.length);
                        
                        similarAutomata = newSimilarAutomata;
                    }
                    else
                    {
                        if (Config.VERBOSE_MODE.isTrue())
                        {
                            logger.info("All similar automata are already added, " +
                                "no chance for controllability.");
                            
                            // CAN'T BE DONE... TRACE NOT REMEMBERED...
                            // Print the uncontrollable state(s)...
                            //synchHelper.printUncontrollableStates();
                            
                            // Print event trace reaching uncontrollable state
                            //if (verificationOptions.showBadTrace())
                            //{
                            //	synchHelper.displayTrace();
                            //}
                        }
                        
                        return false;
                    }
                }
                
                // Add the similar automata in hope of removing uncontrollable
                // states from potentiallyUncontrollableStates...
                excludeUncontrollableStates(similarAutomata, selectedAutomata, automataIndices);
                
                if (stopRequested)
                {
                    return false;
                }
                
                // Are there any potentially uncontrollable states left?
                if (potentiallyUncontrollableStates.size(automataIndices) > 0)
                {
                    if (!verificationOptions.getSkipUncontrollabilityCheck())
                    {
                        logger.verbose("Couldn't prove controllability, " +
                            "trying to prove uncontrollability...");
                        
                        // Try to prove remaining states in the stateMemorizer as being uncontrollable
                        if (findUncontrollableStates(automataIndices))
                        {
                            // Uncontrollable state found!
                            if (Config.VERBOSE_MODE.isTrue() || verificationOptions.showBadTrace())
                            {    
                                // Print the uncontrollable state(s)...
                                uncontrollabilityCheckHelper.printUncontrollableStates();
                            }
                            if (verificationOptions.showBadTrace())
                            {
                                // Print event trace reaching uncontrollable state
                                uncontrollabilityCheckHelper.displayTrace();
                            }
                            
                            return false;
                        }
                    }
                    else
                    {
                        logger.verbose("Skipped uncontrollability check!");
                    }
                }
                else
                {
                    // All uncontrollable states were removed!
                    break;
                }
            }
            
            // Now, we've tried ruling them out... did we make it?
            if (potentiallyUncontrollableStates.size(automataIndices) > 0)
            {
                // There are still some uncontrollable states that we're not sure as of being either
                // controllable or uncontrollable. We now have no idea what so ever on the
                // controllability so... we chicken out and give up.
                // Print remaining suspected uncontrollable state(s)
                if (Config.VERBOSE_MODE.isTrue())
                {
                    logger.info("Unfortunately the following states might be uncontrollable...");
                    synchHelper.printUncontrollableStates(automataIndices);
                }
                
                failure = true;
                
                return false;
            }
        }
        
        // Nothing bad has happened. Very nice!
        logger.verbose("'" + automataNames + "' is controllable.");
        
        return true;
    }
    
    /**
     * Finds similar automata and sorts these automata in a smart way...
     *
     *@param selectedAutomata the selected automata in the current "composition".
     *@param theAutomata reference to the global variable with the same name... eh...
     *@return an int array with indexes of interesting automata in order of interesting interest.
     *@see #compareAlphabets(org.supremica.automata.Alphabet, org.supremica.automata.Alphabet)
     *@see #excludeUncontrollableStates(int[], org.supremica.automata.Automata, int[])
     */
    private int[] findSimilarAutomata(Automata theAutomata, Automata selectedAutomata)
    throws Exception
    {
        int amountOfSelected = selectedAutomata.size();
        int amountOfAutomata = theAutomata.size();
        int amountOfUnselected = amountOfAutomata - amountOfSelected;
        
        // Are there any automata to find in the first place?
        if (amountOfUnselected == 0)
        {
            return null;
        }
        
        // Compute the union alphabet of the automata in selectedAutomata
        Alphabet synchAlphabet = selectedAutomata.getUnionAlphabet();
        
        // Do the work, compare the new automata with the already selected
        Automaton currAutomaton;
        int[] tempArray = new int[amountOfUnselected];
        double[] arraySortValue = new double[amountOfUnselected];
        int count = 0;
        
        AutomataIndexMap indexMap = synchHelper.getIndexMap();
        for (Iterator<Automaton> autIt = theAutomata.iterator(); autIt.hasNext(); )
        {
            currAutomaton = autIt.next();
            
            // Is this automaton interesting?
            if (selectedAutomata.containsAutomaton(currAutomaton))
            {
                continue;
            }
            
            // This line is the essence of it all...
            arraySortValue[count] = compareAlphabets(currAutomaton.getAlphabet(), synchAlphabet);
            
            // Did we get a value?
            if (arraySortValue[count] > 0)
            {
                tempArray[count++] = indexMap.getAutomatonIndex(currAutomaton);
                
                // Have we found everything possible already?
                if (count == amountOfUnselected)
                {
                    break;
                }
            }
        }
        
        // Did we find anything interesting at all?
        if (count == 0)
        {
            return null;
        }
        
        // Bubblesort the array according to arraySortValue... bubblesort? FIXA!
        double tempDouble = 0;
        int tempInt = 0;
        int changes = 1;
        
        while (changes > 0)
        {
            changes = 0;
            
            for (int i = 0; i < count - 1; i++)
            {
                if (arraySortValue[i] < arraySortValue[i + 1])
                {
                    tempInt = tempArray[i];
                    tempArray[i] = tempArray[i + 1];
                    tempArray[i + 1] = tempInt;
                    tempDouble = arraySortValue[i];
                    arraySortValue[i] = arraySortValue[i + 1];
                    arraySortValue[i + 1] = tempDouble;
                    
                    changes++;
                }
            }
        }
        
        // Return an array of appropriate length
        int[] outArray = new int[count];
        
        System.arraycopy(tempArray, 0, outArray, 0, count);
        
        return outArray;
    }
    
    /**
     * Compares two alphabets for determining how similar they are in some sense.
     * All events in rightAlphabet are examined if they are unique to rightAlphabet
     * or appear in leftAlphabet too.
     *
     *@param  leftAlphabet the alphabet to compare.
     *@param  rightAlphabet the alphabet to compare to.
     *@return  double representing how similar the two alphabets are. Returns quota between common
     * events in the alphabets and unique events in rightAlphabet.
     */
    private double compareAlphabets(Alphabet leftAlphabet, Alphabet rightAlphabet)
    {
        // USE Alphabet.nbrOfCommonEvents INSTEAD!!!!
        // Naaaah... that's not the same thing, but this method should be in AlphabetHelpers
        //
        int amountOfCommon = 0;
        int amountOfUnique = 0;
        Iterator eventIterator = rightAlphabet.iterator();
        LabeledEvent currEvent;
        
        while (eventIterator.hasNext())
        {
            currEvent = (LabeledEvent) eventIterator.next();
            
            if (leftAlphabet.contains(currEvent.getLabel()))
            {
                amountOfCommon++;
            }
            else
            {
                amountOfUnique++;
            }
        }
        
        if (amountOfCommon < 1)
        {
            return 0;
        }
        
        if (amountOfUnique > 0)
        {
            // return (double)amountOfCommon; // Another way of doing it...
            return (double) amountOfCommon / (double) amountOfUnique;
        }
        else
        {
            return Double.MAX_VALUE;
        }
    }
    
    /**
     * Excludes potentially uncontrollable states from potentiallyUncontrollableStates by synchronizing the
     * automata in the current composition with automata with similar alphabets.
     *
     *@param  similarAutomata integer array with indices of automata with similar alphabets (from similarAutomata()).
     *@param  selectedAutomata The automata currently selected (the ones in the current "composition" plus perhaps some of the similar automata from earlier runs of this method).
     *@param  automataIndices integer array with indices of automata in the current "composition".
     *@see  #findSimilarAutomata(org.supremica.automata.Automata, org.supremica.automata.Automata)
     */
    private void excludeUncontrollableStates(int[] similarAutomata, Automata selectedAutomata, int[] automataIndices)
    throws Exception
    {
        AutomataIndexMap indexMap = synchHelper.getIndexMap();
        String addedAutomata = "";
        int start = selectedAutomata.size() - automataIndices.length;
        
        if (attempt == 1)
        {
            // First attempt
            stateAmountLimit = verificationOptions.getExclusionStateLimit();
            
            for (int i = 0; i < automataIndices.length; i++)
            {
                stateAmount = stateAmount * indexMap.getAutomatonAt(automataIndices[i]).nbrOfStates();
            }
        }
        else
        {
            // Been here before, already added some automata
            for (int i = 0; i < start; i++)
            {
                addedAutomata = addedAutomata + " " + indexMap.getAutomatonAt(similarAutomata[i]);
            }
            
            // Increase the limit each time
            stateAmountLimit = stateAmountLimit * 5;
        }
        
        logger.verbose("stateAmountLimit: " + stateAmountLimit + ".");
        
        synchHelper.clear();
        
        // Add some of the similar automata, but make sure the stateAmount doesn't explode!
        for (int i = start; i < similarAutomata.length; i++)
        {
            // Add automaton
            selectedAutomata.addAutomaton(indexMap.getAutomatonAt(similarAutomata[i]));
            
            addedAutomata = addedAutomata + " " + indexMap.getAutomatonAt(similarAutomata[i]);
            stateAmount = stateAmount * indexMap.getAutomatonAt(similarAutomata[i]).nbrOfStates();
            
            if ((stateAmount > stateAmountLimit) || (i == similarAutomata.length - 1))
            {
                // Synchronize...
                // synchHelper.clear(); // This is done while analyzing the result se *** below
                synchHelper.initialize();
                
                if (stopRequested)
                {
                    return;
                }
                
                // Initialize the synchronizationExecuters
                synchronizationExecuters.clear();
                
                for (int j = 0; j < synchronizationOptions.getNbrOfExecuters(); j++)
                {
                    AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
                    
                    synchronizationExecuters.add(currSynchronizationExecuter);
                }
                
                // Start all the synchronization executers and wait for completion
                // For the moment we assume that we only have one thread
                for (int j = 0; j < synchronizationExecuters.size(); j++)
                {
                    AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(j);
                    
                    currExec.selectAutomata(selectedAutomata);
                    currExec.start();
                }
                
                ((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
                
                if (stopRequested)
                {
                    return;
                }
                
                // Examine if there are states in potentiallyUncontrollableStates
                // that are not represented in the new synchronization
                int stateCount = 0;
                Iterator stateIt = synchHelper.getStateIterator();
                while (stateIt.hasNext())
                {
                    int[] currState = (int[]) stateIt.next();
                    
                    // Look for the state among the potentially uncontrollable states
                    potentiallyUncontrollableStates.find(automataIndices, currState);
                    
                    // Instead of using clear()... se *** above
                    stateIt.remove();
                    
                    stateCount++;
                }
                                /*
                                int[][] currStateTable = synchHelper.getStateTable();
                                int stateCount = 0;
                                 
                                for (int j = 0; j < currStateTable.length; j++)
                                {
                                        if (currStateTable[j] != null)
                                        {
                                                // Look for the state among the potentially uncontrollable states
                                                potentiallyUncontrollableStates.find(automataIndices, currStateTable[j]);
                                 
                                                // Instead of using clear()... se *** above
                                                currStateTable[j] = null;
                                 
                                                stateCount++;
                                        }
                                }
                                 */
                
                logger.verbose("Worst-case state amount: " + stateAmount + ", real state amount: " + stateCount + ".");
                
                stateAmount = stateCount;
                
                // Remove states in the stateMemorizer that are not represented in the new
                // automaton and therefore can't be reached in the total synchronization.
                // Reachable states are marked with potentiallyUncontrollableStates.find() above.
                potentiallyUncontrollableStates.clean(automataIndices);
                
                // Print result
                int statesLeft = potentiallyUncontrollableStates.size(automataIndices);
                if (Config.VERBOSE_MODE.isTrue())
                {
                    String message = "";
                    
                    switch (statesLeft)
                    {
                        
                        case 0 :
                            message = "No uncontrollable states ";
                            break;
                            
                        case 1 :
                            message = "Still one state ";
                            break;
                            
                        case 2 :
                            message = "Still two states ";
                            break;
                            
                        default :
                            message = "Still " + statesLeft + " states ";
                    }
                    
                    logger.info(message + "left after adding" + addedAutomata + ".");
                }
                
                // Are we ready?
                if (statesLeft == 0)
                {
                    return;
                }
                
                // Is it time to give up this attempt?
                if (stateAmount > stateAmountLimit)
                {
                    
                    // Make sure the limit and the real amount is not too different in magnitude.
                    stateAmountLimit = (stateAmount / 1000) * 1000;
                    
                    break;
                }
            }
        }
    }
    
    /**
     * Makes attempt on finding states in the total synchronization that REALLY are uncontrollable.
     * This is done by making not a full synchronization but a full synchronization limited by
     * in the greatest extent possible following the enabled transitions in the current "composition".
     *
     *@param  automataIndices integer array with indices of automata in the current "composition".
     *@return  Description of the Return Value
     *@exception  Exception Description of the Exception
     */
    private boolean findUncontrollableStates(int[] automataIndices)
    throws Exception
    {
        // WOHOOPS! Eventuellt �r det listigt att g�ra ny onlinesynchronizer,
        // med den nya automataIndices varje g�ng... t�nk p� det. FIXA!
        if (uncontrollabilityCheckHelper == null)
        {
            AutomataSynchronizerExecuter onlineSynchronizer = new AutomataSynchronizerExecuter(synchHelper);
            onlineSynchronizer.selectAutomata(automataIndices);
            onlineSynchronizer.initialize();
            
            uncontrollabilityCheckHelper = new AutomataSynchronizerHelper(synchHelper);
            
            // It's important that setRememberTrace occurs before initialize()!
            uncontrollabilityCheckHelper.setRememberTrace(verificationOptions.showBadTrace());
            uncontrollabilityCheckHelper.setCoExecute(true);
            uncontrollabilityCheckHelper.setCoExecuter(onlineSynchronizer);
            uncontrollabilityCheckHelper.setExhaustiveSearch(true);
            uncontrollabilityCheckHelper.setRememberUncontrollable(true);
            uncontrollabilityCheckHelper.initialize();
        }
        
        // Stop after having found a suitable amount of new states
        uncontrollabilityCheckHelper.stopExecutionAfter(verificationOptions.getReachabilityStateLimit() * attempt);
        
        // Initialize the synchronizationExecuters
        synchronizationExecuters.clear();
        
        for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
        {
            AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(uncontrollabilityCheckHelper);
            
            synchronizationExecuters.add(currSynchronizationExecuter);
        }
        
        // Start all the synchronization executers and wait for completion
        for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
        {
            AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
            
            currExec.selectAllAutomata();
            currExec.start();
        }
        
        ((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
        
        return !uncontrollabilityCheckHelper.getAutomataIsControllable();
        
                /*
                 * // This is the whole method as it was before...
                 * synchHelper.clear();
                 * AutomataOnlineSynchronizer onlineSynchronizer = new AutomataOnlineSynchronizer(synchHelper);
                 * onlineSynchronizer.selectAutomata(automataIndices);
                 * onlineSynchronizer.initialize();
                 *
                 * if (Config.VERBOSE_MODE.isTrue())
                 * {       // It's important that setRememberTrace occurs before addState!
                 * synchHelper.setRememberTrace(true);
                 * }
                 * synchHelper.addState(initialState);
                 * synchHelper.setCoExecute(true);
                 * synchHelper.setCoExecuter(onlineSynchronizer);
                 * synchHelper.setExhaustiveSearch(true);
                 * synchHelper.setRememberUncontrollable(true);
                 *
                 * // Initialize the synchronizationExecuters
                 * synchronizationExecuters.clear();
                 * for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
                 * {
                 * AutomataSynchronizerExecuter currSynchronizationExecuter =
                 * new AutomataSynchronizerExecuter(synchHelper);
                 * synchronizationExecuters.add(currSynchronizationExecuter);
                 * }
                 *
                 * // Start all the synchronization executers and wait for completion
                 * for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
                 * {
                 * AutomataSynchronizerExecuter currExec =
                 * (AutomataSynchronizerExecuter)synchronizationExecuters.get(i);
                 * currExec.selectAllAutomata();
                 * currExec.start();
                 * }
                 * ((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();
                 *
                 * return !synchHelper.getAutomataIsControllable();
                 */
    }
    
    /**
     * Answers YES/NO to the language inclusion problem
     *
     * @see org.supremica.util.BDD.BDDAutomata
     * @see AutomataBDDVerifier
     */
    private boolean BDDLanguageInclusionVerification()
    throws Exception
    {
        Automata unselected = ActionMan.getGui().getUnselectedAutomata();
        
        // we already know the answer: L(P) = \Sigma^*
        if (unselected.size() < 1)
        {
            return true;
        }
        
        Automata selected = new Automata(theAutomata, true);    /* <-- MUST BE SHALLOW COPY ... */
        
        selected.removeAutomata(unselected);    /* .. OR THIS REMOVE WONT WORK !!! */
        
        boolean ret = false;
        org.supremica.util.BDD.Timer timer = new org.supremica.util.BDD.Timer("BDDLanguageInclusionVerification");
        
        switch (Options.inclusion_algorithm)
        {
            
            case Options.INCLUSION_ALGO_MONOLITHIC :
                AutomataBDDVerifier abf = new AutomataBDDVerifier(selected, unselected);
                
                ret = abf.passLanguageInclusion();
                
                abf.cleanup();
                break;
                
            case Options.INCLUSION_ALGO_MODULAR :
                org.supremica.util.BDD.li.ModularLI mli = new org.supremica.util.BDD.li.ModularLI(selected, unselected);
                
                ret = mli.passLanguageInclusion();
                
                mli.cleanup();
                break;
                
            case Options.INCLUSION_ALGO_INCREMENTAL :
                org.supremica.util.BDD.li.IncrementalLI ili = new org.supremica.util.BDD.li.IncrementalLI(selected, unselected);
                
                ret = ili.passLanguageInclusion();
                
                ili.cleanup();
                break;
                
            default :
                throw new SupremicaException("Unknown BDD/language containment algorithm!");
        }
        
        if (Options.profile_on)
        {
            timer.report("total execution time", true);
        }
        
        Options.out.flush();
        
        return ret;
    }
    
    /**
     * Answers YES/NO to the controllability problem
     *
     * @return  true if the system is controllable
     * @see org.supremica.util.BDD.BDDAutomata
     * @see AutomataBDDVerifier
     */
    private boolean BDDControllabilityVerification(Automata theAutomata)
    throws Exception
    {
        boolean ret;
        
        // why compute when we already know the answer: L(P) = \Sigma^*  ?
        if (theAutomata.hasNoPlants())
        {
            return true;
        }
        
        org.supremica.util.BDD.Timer timer = new org.supremica.util.BDD.Timer("BDDControllabilityVerification");
        
        switch (Options.inclusion_algorithm)
        {
            case Options.INCLUSION_ALGO_MONOLITHIC :
                AutomataBDDVerifier abf = new AutomataBDDVerifier(theAutomata);
                
                ret = abf.isControllable();
                
                abf.cleanup();
                break;
                
            case Options.INCLUSION_ALGO_MODULAR :
                org.supremica.util.BDD.li.ModularLI mli = new org.supremica.util.BDD.li.ModularLI(theAutomata);
                
                ret = mli.isControllable();
                
                mli.cleanup();
                break;
                
            case Options.INCLUSION_ALGO_INCREMENTAL :
                org.supremica.util.BDD.li.IncrementalLI ili = new org.supremica.util.BDD.li.IncrementalLI(theAutomata);
                
                ret = ili.isControllable();
                
                ili.cleanup();
                break;
                
            default :
                throw new SupremicaException("Unknown BDD/language containment algorithm!");
        }
        
        if (Options.profile_on)
        {
            timer.report("total execution time", true);
        }
        
        Options.out.flush();
        
        return ret;
    }
 
    /**
     * Answers YES/NO to the NONBLOCKING problem
     *
     *@return  true if the system is nonblocking
     */
    private boolean monolithicBDDNonblockingVerification()
    throws Exception
    {
        BDDVerifier bddVerifier = new BDDVerifier(theAutomata);
        boolean isNonblocking = bddVerifier.isNonblocking();
        logger.info("Number of reachable state: " + bddVerifier.numberOfReachableStates());
        logger.info("Number of coreachable states: " + bddVerifier.numberOfCoreachableStates());
        logger.info("Number of blocking states: " + bddVerifier.numberOfBlockingStates());
        bddVerifier.done();
        return isNonblocking;       
    }    
    
    /**
     * Answers YES/NO to the NONBLOCKING problem
     *
     *@return  true if the system is nonblocking
     *@see org.supremica.util.BDD.BDDAutomata
     *@see AutomataBDDVerifier
     */
    private boolean BDDNonblockingVerification()
    throws Exception
    {
        
        // timer.start();
        AutomataBDDVerifier abf = new AutomataBDDVerifier(theAutomata);
        boolean ret = abf.isNonblocking();
        
        abf.cleanup();
        
        // timer.stop();
        Options.out.flush();
        
        return ret;
    }
    
    /**
     * Examines controllability by synchronizing all automata in the system and in each state check if some
     * uncontrollable event is enabled in a plant and not in a supervisor.
     *
     *@return  Description of the Return Value
     *@exception  Exception Description of the Exception
     *@see  AutomataSynchronizerExecuter
     */
    private boolean monolithicControllabilityVerification()
    throws Exception
    {
        synchHelper.setExhaustiveSearch(true);
        synchHelper.initialize();
        
        // Initialize the synchronizationExecuters
        for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
        {
            AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
            
            synchronizationExecuters.add(currSynchronizationExecuter);
        }
        
        // Start all the synchronization executers and wait for completion
        for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
        {
            AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
            
            currExec.selectAllAutomata();
            currExec.start();
        }
        
        ((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
        
        return synchHelper.getAutomataIsControllable();
    }
    
    public AutomataSynchronizerHelper getHelper()
    {
        return synchHelper;
    }
    
    /**
     * Examines nonblocking monolithically, by examining all reachable states.
     * Lots and lots of work for big systems.
     *
     *@return True if nonblocking, false if blocking
     *@exception  Exception Description of the Exception
     *@see  AutomataSynchronizerExecuter
     */
    private boolean monolithicNonblockingVerification()
    throws Exception
    {
        // Maybe the system is monolithic already?
        if (theAutomata.size() == 1)
        {
            // No need to synchronize!
            return moduleIsNonblocking(theAutomata.getFirstAutomaton());
        }
        
        // Otherwise we must synchronize!
        Automaton theAutomaton = AutomataSynchronizer.synchronizeAutomata(synchHelper);
        
                /*
                // Otherwise we must synchronize...
                synchHelper.setExhaustiveSearch(false);
                synchHelper.initialize();
                 
                // Initialize the synchronizationExecuters
                for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
                {
                        AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
                        synchronizationExecuters.add(currSynchronizationExecuter);
                }
                 
                // Start all the synchronization executers and wait for completion
                for (int i = 0; i < synchronizationOptions.getNbrOfExecuters(); i++)
                {
                        AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
                        currExec.selectAllAutomata();
                        currExec.start();
                }
                ((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
                AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);
                 
                // Get the synchronized automaton
                Automaton theAutomaton;
                try
                {
                        if (currExec.buildAutomaton())
                        {
                                theAutomaton = synchHelper.getAutomaton();
                        }
                        else
                        {
                                requestStop();
                                theAutomaton = null;
                 
                 
                                return false;
                        }
                }
                catch (Exception ex)
                {
                        logger.error("Error when building automaton: " + ex.toString());
                        logger.debug(ex.getStackTrace());
                 
                        throw ex;
                }
                 */
        
        // Now its just a matter of examining the states (and we can
        // do that destructively unless we want to find traces)
        return moduleIsNonblocking(theAutomaton, !verificationOptions.showBadTrace());
    }
    
    /**
     * Examines each automaton individually for nonblocking.
     *
     * Does not use the synchHelper!
     */
    private boolean isIndividuallyNonblocking()
    throws Exception
    {
        boolean allIndividuallyNonblocking = true;
        Iterator autIt = theAutomata.iterator();
        Automaton currAutomaton;
        
        while (autIt.hasNext())
        {
            currAutomaton = (Automaton) autIt.next();
            allIndividuallyNonblocking = allIndividuallyNonblocking && moduleIsNonblocking(currAutomaton);
            
            if (stopRequested)
            {
                return false;
            }
            
            if (!allIndividuallyNonblocking)
            {
                logger.error("The automaton " + currAutomaton + " is blocking!");
                
                // logger.error("Aborting verification...");
                requestStop();
                
                return false;
            }
        }
        
        logger.info("All automata are individually nonblocking.");
        
        return true;
    }
       
    // BENCHMARKING JUNK
    private static String message = "";
    /**
     * Returns LaTeX-code with statistics for a table.
     */
    public String getTheMessage()
    {
        return message;
    }    
    
    /**
     * Verifies controllability compositionally (by transforming the 
     * controllability problems into blocking problems).
     *
     * Does not use the synchHelper.
     */
    private boolean compositionalControllabilityVerification()
    throws Exception
    {
        // Mark all states in all automata
        for (Automaton automaton : theAutomata)
        {
            automaton.setAllStatesAccepting();
        }
        
        // Now the system is trivially nonblocking so verifying controllability AND nonblocking
        // equals verifying controllability!
        return compositionalControllabilityNonblockingVerification();
    }
    
    /**
     * Verifies both controllability and nonblocking simultaneously using a compositional approach.
     *
     * Does not use the synchHelper.
     */
    private boolean compositionalControllabilityNonblockingVerification()
    throws Exception
    {
        // Plantify all automata (transform controllability problems to blocking problems)
        MinimizationHelper.plantify(theAutomata);
        
        // Verify nonblocking (yep, that's right)
        return compositionalNonblockingVerification();
    }
    
    /**
     * Compositionally minimizes the automata and examines the end result...
     *
     * Does not use the synchHelper.
     */
    private boolean compositionalNonblockingVerification()
    throws Exception
    {
        // Initialize execution dialog
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                if (executionDialog != null)
                {
                    executionDialog.setMode(ExecutionDialogMode.VERIFYINGNONBLOCKING);
                }
            }
        });
        
        // If combined, use BDD:s when the components grow too big!
        if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMBINED)
            minimizationOptions.setComponentSizeLimit(250);
        
        // Minimize the system compositionally
        Automata result;
        try
        {
            // BENCHMARKING
            message = "";
            
            // Minimizer
            AutomataMinimizer minimizer = new AutomataMinimizer(theAutomata);
            threadToStop = minimizer;
            if (executionDialog != null)
            {
                minimizer.setExecutionDialog(executionDialog);
            }
            
            // Minimize!
            result = minimizer.getCompositionalMinimization(minimizationOptions);
            
            // Something went wrong?
            if (result == null)
            {
                requestStop();
                return false;
            }
            threadToStop = null;
            
            // JUNK
            message = minimizer.getStatisticsLineLaTeX();
        }
        catch (Exception ex)
        {
            requestStop();
            logger.error("Error in AutomataVerifier when verifying nonblocking compositionally. " + ex);
            logger.error(ex.getStackTrace());
            return false;
        }
        
        if (stopRequested)
        {
            return false;
        }
        
        // Examine the result and return the verdict!
        theAutomata  = result; 
        if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.COMBINED)
        {
            logger.info("Automata after minimisation:");
            for (Automaton aut : theAutomata)
            {
                logger.info(aut + ", " + aut.nbrOfStates() + " states.");
            }
            return monolithicBDDNonblockingVerification();
        }
        else
            return monolithicNonblockingVerification();
    }
    
    private boolean modularLanguageinclusionVerification(Automata inclusionAutomata)
    throws Exception
    {
        prepareForLanguageInclusion(inclusionAutomata);
        
        return modularControllabilityVerification();
    }
    
    private boolean monolithicLanguageinclusionVerification(Automata inclusionAutomata)
    throws Exception
    {
        prepareForLanguageInclusion(inclusionAutomata);
        
        return monolithicControllabilityVerification();
    }
    
    /**
     * Examines nonblocking monolithically, by examining all reachable states.
     * Lots and lots of work for big systems.
     *
     *@return True if nonblocking, false if blocking
     *@exception  Exception Description of the Exception
     *@see AutomataSynchronizerExecuter
     */
    private boolean moduleIsNonblocking(Automaton theAutomaton)
    throws Exception
    {
        return moduleIsNonblocking(theAutomaton, false);
    }
    
    /**
     * Examines nonblocking monolithically, by examining all reachable states.
     * Allows destructive verification (perhaps we don't need to make a copy)
     *
     *@return True if nonblocking, false if blocking
     *@exception  Exception Description of the Exception
     *@see AutomataSynchronizerExecuter
     */
    private boolean moduleIsNonblocking(Automaton original, boolean destructive)
    throws Exception
    {
        // Should we save the original by creating a copy that we can destroy?
        Automaton aut;
        if (destructive)
        {
            aut = original;
        }
        else
        {
            aut = new Automaton(original);
        }
        
        // Examine all states, starting from the marked ones and moving backwards...
        LinkedList statesToExamine = new LinkedList();
        Iterator stateIterator = aut.stateIterator();
        State currState;
        
        // Add all marked states
        while (stateIterator.hasNext())
        {
            currState = (State) stateIterator.next();
            
            if (currState.isAccepting())
            {
                statesToExamine.add(currState);
            }
        }
        
        // Examine all guaranteed nonblocking states for incoming arcs
        State examinedState;
        Iterator<Arc> incomingArcIterator;
        while (statesToExamine.size() > 0)
        {
            examinedState = (State) statesToExamine.removeFirst();    // OBS. removeFirst!
            incomingArcIterator = examinedState.incomingArcsIterator();
            
            while (incomingArcIterator.hasNext())
            {
                currState = incomingArcIterator.next().getFromState();
                
                if (!currState.equals(examinedState))    // Self-loops...
                {
                    statesToExamine.add(currState);
                }
            }
            
            aut.removeState(examinedState);
        }
        
        // Present result (if ordered to)
        if (verificationOptions.showBadTrace())
        {
            // Show all blocking states? They can be many!
            stateIterator = aut.stateIterator();
            while (stateIterator.hasNext())
            {
                currState = (State) stateIterator.next();
                logger.info("Blocking state: " + currState.getName());
                
                // If we did a copy of theAutomata before we destroyed it we could display the trace...
                if (!destructive && verificationOptions.showBadTrace())
                {
                    String trace = (original.getTrace(original.getStateWithName(currState.getName()))).toString();
                    if (!trace.equals(""))
                    {
                        logger.info("Trace to blocking state: " + trace);
                    }
                    else
                    {
                        logger.info("The initial state is blocking!");
                    }
                }
                
                if (stopRequested)
                {
                    return false;
                }
            }
        }
        
        return aut.nbrOfStates() == 0;
    }
    
    /**
     * Displays info about the previous operation.
     */
    public void displayInfo()
    {
        if (synchHelper != null)
        {
            synchHelper.printStatistics();
        }
    }
    
    /**
     * Assigns the verifier an ExecutionDialog.
     */
    public void setExecutionDialog(ExecutionDialog executionDialog)
    {
        this.executionDialog = executionDialog;
        
        if (synchHelper != null)
        {
            synchHelper.setExecutionDialog(executionDialog);
        }
    }
    
    /**
     * Method that stops AutomataVerifier as soon as possible.
     *
     * @see  ExecutionDialog
     */
    public void requestStop()
    {
        logger.debug("AutomataVerifier requested to stop.");
        
        stopRequested = true;
        
        // Stop everything!
        for (int i = 0; i < synchronizationExecuters.size(); i++)
        {
            ((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).requestStop();
        }
        
        if (threadToStop != null)
        {
            threadToStop.requestStop();
        }
        
        if (executionDialog != null)
        {
            executionDialog.stopAllThreads();
        }
        // Clear!
        executionDialog = null;
    }
    
    public boolean isStopped()
    {
        return stopRequested;
    }
    
    /**
     * Standard method for monolithic nonblocking verification on theAutomaton.
     */
    public static boolean verifyMonolithicNonblocking(Automata automata)
    throws Exception
    {
        SynchronizationOptions synchronizationOptions;
        VerificationOptions verificationOptions;
        
        verificationOptions = VerificationOptions.getDefaultNonblockingOptions();
        verificationOptions.setAlgorithmType(VerificationAlgorithm.MONOLITHIC);
        synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
        
        AutomataVerifier verifier = new AutomataVerifier(automata, verificationOptions, synchronizationOptions, null);
        
        return verifier.verify();
    }
    
    /**
     * Standard method for compositional nonblocking verification on theAutomaton.
     */
    public static boolean verifyCompositionalNonblocking(Automata automata)
    throws Exception
    {
        VerificationOptions verificationOptions;
        SynchronizationOptions synchronizationOptions;
        MinimizationOptions minimizationOptions;
        
        verificationOptions = VerificationOptions.getDefaultNonblockingOptions();
        synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
        minimizationOptions = MinimizationOptions.getDefaultNonblockingOptions();
        minimizationOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
        minimizationOptions.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
        
        AutomataVerifier verifier = new AutomataVerifier(automata, verificationOptions, synchronizationOptions, minimizationOptions);
        
        return verifier.verify();
    }
    
    /**
     * Standard method for performing compositional controllability verification on theAutomata.
     */
    public static boolean verifyCompositionalControllability(Automata theAutomata)
    throws Exception
    {
        SynchronizationOptions synchronizationOptions;
        VerificationOptions verificationOptions;
        MinimizationOptions minimizationOptions;
        
        synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
        verificationOptions = VerificationOptions.getDefaultControllabilityOptions();
        verificationOptions.setAlgorithmType(VerificationAlgorithm.MODULAR);
        minimizationOptions = MinimizationOptions.getDefaultNonblockingOptions();
        minimizationOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
        minimizationOptions.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
        
        AutomataVerifier verifier = new AutomataVerifier(theAutomata, verificationOptions, synchronizationOptions, minimizationOptions);
        
        return verifier.verify();
    }
    
    /**
     * Standard method for performing modular controllability verification on theAutomata.
     */
    public static boolean verifyModularControllability(Automata theAutomata)
    throws Exception
    {
        SynchronizationOptions synchronizationOptions;
        VerificationOptions verificationOptions;
        
        synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
        verificationOptions = VerificationOptions.getDefaultControllabilityOptions();
        
        AutomataVerifier verifier = new AutomataVerifier(theAutomata, verificationOptions, synchronizationOptions, null);
        
        return verifier.verify();
    }
    
    /**
     * Standard method for performing modular languageInclusion verification on automataA
     * and automataB.
     *
     * @param automataA the automata that should be included.
     * @param automataB the automata that should include
     * @return true if "L(automataA)" is included in "L^-1(automataB)".
     */
    public static boolean verifyModularInclusion(Automata automataA, Automata automataB)
    throws Exception
    {
        SynchronizationOptions synchronizationOptions;
        VerificationOptions verificationOptions;
        
        verificationOptions = VerificationOptions.getDefaultLanguageInclusionOptions();
        verificationOptions.setInclusionAutomata(automataA);
        synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
        
        Automata theAutomata = new Automata();
        theAutomata.addAutomata(automataA);
        theAutomata.addAutomata(automataB);
        //theAutomata.setIndicies();
        
        AutomataVerifier verifier = new AutomataVerifier(theAutomata, verificationOptions, synchronizationOptions, null);
        
        return verifier.verify();
    }
}
