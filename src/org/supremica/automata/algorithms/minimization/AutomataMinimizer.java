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
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.*;
import org.supremica.util.ActionTimer;
import org.supremica.properties.Config;
import java.util.*;

public class AutomataMinimizer
    implements Stoppable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataMinimizer.class);
    // Stoppable stuff
    private ExecutionDialog executionDialog;
    private Stoppable threadToStop = null;
    private boolean stopRequested = false;
    
    /** The automata being minimized (may be a copy of the original). */
    private Automata theAutomata;
    
    /** The supplied options. */
    private MinimizationOptions options;
    
    /** The strategy to use, in order of priority */
    private LinkedList<MinimizationStrategy> strategyList;
    
    /** The heuristic to use, in order of priority */
    private LinkedList<MinimizationHeuristic> heuristicList;
    
    ////////////////
    // Statistics //
    ////////////////
    
    /** Largest number of states come across. */
    private int mostStates = 0;
    /** Total number of states come across. */
    private int totalStates = 0;
    
    /** Largest number of transitions come across. */
    private int mostTransitions = 0;
    /** Total number of transitions come across. */
    private int totalTransitions = 0;
    
    /** Number of automata in the beginning. */
    private int initialNbrOfAutomata;
    
    /** Event to automata map, for choosing the next task in compositional minimization. */
    private EventToAutomataMap eventToAutomataMap;
    
    private boolean preserveControllability;
    private boolean useShortStateNames;

    private Automata neighbours = new Automata();
    private Automata selection = new Automata();
    private Automata taskExtra = new Automata();
    
    /**
     * Basic constructor.
     */
    public AutomataMinimizer(Automata theAutomata)
    {
        initialNbrOfAutomata = theAutomata.nbrOfAutomata();
        this.theAutomata = theAutomata;
    }
    
    /**
     * Sets the executionDialog of this AutomataMinimizer. If executionDialog is null,
     * the dialog is not updated.
     */
    public void setExecutionDialog(ExecutionDialog executionDialog)
    {
        this.executionDialog = executionDialog;
    }
    
    /**
     * Returns minimized automaton, minimized with respect to the supplied options.
     */
    public Automata getCompositionalMinimization(MinimizationOptions options)
    throws Exception
    {
        this.options = options;
        
        // Are the options valid?
        if (!options.isValid()) throw new IllegalArgumentException("Invalid compositional minimization options");
        
        if (options.getMinimizationType() == EquivalenceRelation.CONFLICTEQUIVALENCE &&
            theAutomata.hasForbiddenState())
        {
            logger.warn("All forbidden states must be removed before attempting the compositional approach.");
            throw new IllegalArgumentException("Automata contain forbidden states, which is currently not supported by conflict equivalence");
        }

        // Preserve controllablity during hiding?
        //preserveControllability = options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE;
        preserveControllability = true;
        // Do we care about state names?
        //useShortStateNames = options.getMinimizationType() != EquivalenceRelation.SUPERVISIONEQUIVALENCE; //**
        useShortStateNames = true;

        // Initialize execution dialog
        if (executionDialog != null)
        {
            executionDialog.initProgressBar(0, theAutomata.size()-1);
        }
        
        // Make priority list of the strategies (used in get NextMinimizationTask())
        strategyList = new LinkedList<MinimizationStrategy>();
        //strategyList.add(MinimizationStrategy.AtLeastOneLocalMaxThree);
        strategyList.add(MinimizationStrategy.FewestTransitionsFirst); // 1
        strategyList.add(MinimizationStrategy.AtLeastOneLocal);
        //strategyList.add(MinimizationStrategy.FewestStatesFirst); // 2
        //strategyList.add(MinimizationStrategy.FewestEventsFirst); // 3
        //strategyList.add(MinimizationStrategy.MostStatesFirst);
        //strategyList.add(MinimizationStrategy.RandomFirst);
        // Make sure the chosen strategy is the first one we try!
        strategyList.remove(options.getMinimizationStrategy());
        strategyList.addFirst(options.getMinimizationStrategy());
        
        // Make priority list of the heuristics (used in get NextMinimizationTask())
        heuristicList = new LinkedList<MinimizationHeuristic>();
        heuristicList.add(MinimizationHeuristic.MostLocal); // 1
        heuristicList.add(MinimizationHeuristic.MostCommon); // 2
        //heuristicList.add(MinimizationHeuristic.FewestTransitions);
        heuristicList.add(MinimizationHeuristic.FewestStates); // 3
        //heuristicList.add(MinimizationHeuristic.FewestEvents);
        //heuristicList.add(MinimizationHeuristic.LeastExtension);
        //heuristicList.add(MinimizationHeuristic.Random);
        // Make sure the chosen heuristic is the first one we try!
        heuristicList.remove(options.getMinimizationHeuristic());
        heuristicList.addFirst(options.getMinimizationHeuristic());
        
        // Initialize statistics count
        AutomatonMinimizer.resetStatistics();
        
        // Special pre-minimization stuff
        if (options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE)
        {
            MinimizationHelper.plantify(theAutomata);
        }
        
        // Remove inadequate events...
        for (Automaton aut: theAutomata)
        {
            Alphabet inadequate = aut.getInadequateEvents(true);
            if (inadequate.size() > 0)
            {
                //logger.fatal("Hiding inadequate events " + inadequate + " in " + aut + ".");
                aut.hide(inadequate, preserveControllability);
            }
        }
        
        // For each event, find the automata that has this event in its alphabet
        eventToAutomataMap = AlphabetHelpers.buildEventToAutomataMap(theAutomata);
        // Sizes in the beginning (may include epsilon?)
        int globalAlphabetSize = eventToAutomataMap.size() - options.getTargetAlphabet().size();
        // Current alphabet size...
        int currentAlphabetSize = globalAlphabetSize;

		// Measure the time for the task selection
        ActionTimer taskSelectionTimer = new ActionTimer();
        
        // If there's just one automaton, the user must have wanted it minimised!
        if (theAutomata.size() == 1)
            theAutomata = new Automata(monolithicMinimization(theAutomata, AlphabetHelpers.minus(theAutomata.getUnionAlphabet(), options.getTargetAlphabet())));
        
        // As long as there are at least two automata,
        // select some automata to compose and minimize!
        while (theAutomata.size() >= 2)
        {
            /*
			  // Don't always give the same result
			  MinimizationTask newTask = getNextMinimizationTask(true);
			  MinimizationTask oldTask = getNextMinimizationTask(false);
			  if (!oldTask.equals(newTask))
			  {
			  logger.info("Unequal!!");
			  logger.info("New: " + newTask);
			  logger.info("Old: " + oldTask);
			  }
             */
            
            // Get next automata to minimize
            taskSelectionTimer.start();
            
            //MinimizationTask task = getNextMinimizationTask(false); // old implementation
            MinimizationTask task = getNextMinimizationTask(true); // new implementation
            //MinimizationTask task = getNextMinimizationTask(heuristicList.getFirst() != MinimizationHeuristic.MostLocal); // decide based on heuristic
            Automata selection;
            Alphabet hideThese;
            if (task != null)
            {
                // Get task
                selection = task.getAutomata();
                hideThese = task.getEventsToHide();
            }
            else
            {
                // Could not find a task? Select everything!
                selection = theAutomata;
                hideThese = selection.getUnionAlphabet();
                hideThese.minus(options.getTargetAlphabet());
            }
            taskSelectionTimer.stop();
            if (stopRequested)
            {
                return null;
            }
            
            // Perform the minimization, unless of course this is the last step
            // and it should be skipped...
            Automaton min;
            if (options.getSkipLast() && (theAutomata.size() == selection.size()))
            {
                // Just synch and hide
                min = AutomataSynchronizer.synchronizeAutomata(selection);
                min.hide(hideThese, preserveControllability);
                
                // Examine for largest sizes (this is a special case, this is otherwise done in minolithicMinimization())
                if (min.nbrOfStates() > mostStates)
                {
                    mostStates = min.nbrOfStates();
                }
                if (min.nbrOfTransitions() > mostTransitions)
                {
                    mostTransitions = min.nbrOfTransitions();
                }
                totalStates += min.nbrOfStates();
                totalTransitions += min.nbrOfTransitions();
            }
            else
            {
                // Compose and minimize!
                min = monolithicMinimization(selection, hideThese);
            }
            if (stopRequested)
            {
                return null;
            }

            // Early termination
            if ((options.getMinimizationType() == EquivalenceRelation.CONFLICTEQUIVALENCE) ||
                (options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE))
            {
                // If initial state is blocking, we can early terminate!
                // Easy check: If there's just one state and it is not marked, then it's blocking!
                if (min.nbrOfStates() == 1 && !min.getInitialState().isAccepting())
                {
                    // Return a one state blocking automaton (min for example)
                    logger.info("Early termination--a blocking state can be reached silently!");
                    min.hide(new Alphabet(min.getAlphabet()).minus(options.getTargetAlphabet()), false);
                    theAutomata = new Automata(min);
                    continue;
                }
                
                if (options.getMinimizationType() == EquivalenceRelation.CONFLICTEQUIVALENCE)
                {
                    // If all states are marked in all automata, we're done!
                    boolean ok = true;
                    for (Automaton aut: theAutomata)
                    {
                        if (aut.hasNonacceptingState())
                        {
                            ok = false;
                            break;
                        }
                    }
                    if (ok)
                    {
                        logger.info("Early termination--all states are marked!");
                        Automaton aut = new Automaton("NONBLOCK");
                        State state = new State("q");
                        state.setAccepting(true);
                        state.setInitial(true);
                        aut.addState(state);
                        //aut.setInitialState(state);
                        theAutomata = new Automata(aut);
                        continue;
                    }
                }
            }
            
            // Adjust the eventToAutomataMap
            // Remove the hidden events from the map
            for (Iterator<LabeledEvent> it = hideThese.iterator(); it.hasNext(); )
            {
                eventToAutomataMap.remove(it.next());
            }
            // Remove the examined automata from the map
            for (Iterator<LabeledEvent> it = selection.getUnionAlphabet().iterator(); it.hasNext(); )
            {
                Automata aut = eventToAutomataMap.get(it.next());
                if (aut != null)
                {
                    aut.removeAutomata(selection);
                }
            }
            // And add the new automaton!
            for (LabeledEvent event: min.getAlphabet())
            {
                if (event.isObservable())
                    eventToAutomataMap.insert(event, min);
            }
            
            // Adjust the automata
            theAutomata.removeAutomata(selection);
            theAutomata.addAutomaton(min);
            
            // Update execution dialog
            if (executionDialog != null)
            {
                executionDialog.setProgress(initialNbrOfAutomata-theAutomata.size());
                currentAlphabetSize -= hideThese.size()-hideThese.nbrOfUnobservableEvents();
                executionDialog.setSubheader("Events left: " + currentAlphabetSize +
                    " (" + globalAlphabetSize + ")");
            }
        }
        
        // Print statistics
        if (Config.VERBOSE_MODE.isTrue())
        {
            // Print total reduction statistics
            AutomatonMinimizer.logStatistics(options);
            // Print largest automaton sizeif observable

            logger.info("The automaton with the most states had " + mostStates + " states.");
            logger.info("The automaton with the most transitions had " + mostTransitions + " transitions.");
            // Print total state & transition number examined
            logger.info("The automata encountered had " + totalStates + " states and " + totalTransitions + " transitions in total.");
        }
        else
        {
            //logger.info("The automaton with the most states had " + mostStates + " states.");
            //logger.info("The automaton with the most transitions had " + mostTransitions + " transitions.");
        }
        //logger.info("Timer time: " + taskSelectionTimer);
        //logger.info(theAutomata.getName() + " & " + initialNbrOfAutomata + " & & " + mostStates + " & " + mostTransitions + " & TIME & true/false & " + AutomatonMinimizer.getWodesStatisticsLaTeX() + " & ALGO \\\\");
        // Return the result of the minimization!
        return theAutomata;
    }
    
    /**
     * Returns a string of incomplete LaTeX-code describing the minimisation performed.
     */
    public String getStatisticsLineLaTeX()
    {
        //return "\\texttt{NAME} & " + initialNbrOfAutomata + " & SIZE & " + mostStates + " & " + mostTransitions + " & TIME & BLOCK & " + AutomatonMinimizer.getStatisticsLaTeX() + " & ALGO1 & ALGO2 \\\\";
        return "\\texttt{NAME} & " + initialNbrOfAutomata + " & SIZE & " + mostStates + " & " + mostTransitions + " & " + totalStates + " & " + totalTransitions + " & TIME & BLOCK & " + AutomatonMinimizer.getStatisticsLaTeX() + " & ALGO1 & ALGO2 \\\\";
    }
    
    /**
     * Class holding info about what should be done in the next
     * minimization. Which automata should be composed and which
     * events can be hidden.
     */
    private class MinimizationTask
    {
        private Automata automata;
        private Alphabet eventsToHide;
        public MinimizationTask(Automata automata, Alphabet eventsToHide)
        {
            this.automata = automata;
            this.eventsToHide = eventsToHide;
        }
        public Automata getAutomata()
        {
            return automata;
        }
        public Alphabet getEventsToHide()
        {
            return eventsToHide;
        }
        @SuppressWarnings("unused")
		public boolean equals(MinimizationTask other)
        {
            if (automata.equalAutomata(other.automata))
            {
                return eventsToHide.toString().equals(other.eventsToHide.toString());
            }
            else
            {
                return false;
            }
        }
        public String toString()
        {
            return automata + ", " + eventsToHide;
        }
    }
    
    /**
     * Returns the next Automata that is predicted to be the best one
     * to do minimization on next and the Alphabet of events that can
     * be hidden.
     *@return an appropriate MinimizationTask or null if none can be found (e.g. too large automata).
     */
    private MinimizationTask getNextMinimizationTask(boolean newAlgo)
    throws Exception
    {
        Automata taskAutomata = new Automata();
        // Target alphabet
        Alphabet targetAlphabet = options.getTargetAlphabet();
        // Try the heuristics in order of priority...
        strategyLoop: for (int strategyIndex = 0; strategyIndex < strategyList.size(); strategyIndex++)
        {
            ////////////////
            // FIRST STEP //
            ////////////////
            
            // Begin with the first strategy!
            MinimizationStrategy strategy = strategyList.get(strategyIndex);
            // ... and the first heuristic
            int heuristicIndex = 0;
            MinimizationHeuristic heuristic = heuristicList.get(heuristicIndex);
            // Try to find a nice task
            if (strategy == MinimizationStrategy.AtLeastOneLocal ||
                strategy == MinimizationStrategy.AtLeastOneLocalMaxThree)
            {
                // Look through the map and find the best set of automata
                double bestValue = heuristic.worstValue();
                loop: for (LabeledEvent event : eventToAutomataMap.keySet())
                {
                    // Skip the events in targetAlphabet and epsilon events!
                    if (targetAlphabet.contains(event) || event.isUnobservable())
                    {
                        continue;
                    }
                    if (stopRequested)
                    {
                        return null;
                    }
                    
                    // Get the selection that have this event in their alphabet
                    Automata selection = eventToAutomataMap.get(event);
                    
                    // If there is an automaton that by itself has
                    // local events, choose that one as the task!
                    // This can happen in the beginning, and
					// if events have become inadequate
                    if (selection.size() == 1)
                    {
                        taskAutomata = selection;
                        break strategyLoop;
                    }
                    if (strategy == MinimizationStrategy.AtLeastOneLocalMaxThree &&
                        selection.size() > 3)
                    {
                        continue loop;
                    }
                    // Skip selections with too large automata
                    for (Automaton aut: selection)
                    {
                        if (aut.nbrOfStates() > options.getComponentSizeLimit())
                            continue loop;
                    }
                    
                    /////////////////
                    // SECOND STEP //
                    /////////////////
                    
                    // Evaluate selection
                    double thisValue = heuristic.value(selection, eventToAutomataMap, targetAlphabet);
                    // Maximize or minimize?
                    if ((heuristic.maximize() && thisValue > bestValue) ||
                        (heuristic.minimize() && thisValue < bestValue))
                    {
                        //logger.info(heuristic + ", " + selection);
                        bestValue = thisValue;
                        taskAutomata = selection;
                        continue loop;
                    }
                    else if (bestValue == thisValue)
                    {
                        // As good as the best one?
                        // Use lower priority heuristic to make a decision!
                        for (int i = heuristicIndex+1; i<heuristicList.size(); i++)
                        {
                            MinimizationHeuristic nextHeuristic = heuristicList.get(i);
                            double nextHeuristicBest = nextHeuristic.value(taskAutomata,
                                eventToAutomataMap,
                                targetAlphabet);
                            double nextHeuristicThis = nextHeuristic.value(selection,
                                eventToAutomataMap,
                                targetAlphabet);
                            if ((nextHeuristic.maximize() && nextHeuristicThis > nextHeuristicBest) ||
                                (nextHeuristic.minimize() && nextHeuristicThis < nextHeuristicBest))
                            {
                                //logger.info(nextHeuristic + ", " + selection);
                                taskAutomata = selection;
                                continue loop;
                            }
                            else if (!(nextHeuristicThis == nextHeuristicBest))
                            {
                                // Worse value!
                                continue loop;
                            }
                        }
                    }
                }
                
                // Couldn't find an appropriate result?
                if (taskAutomata.size() == 0)
                {
                    // Try next strategy
                    continue strategyLoop;
                }
            }
            else
            {
                ////////////////
                // FIRST STEP //
                ////////////////
                
                // Choose the "best" automaton...
                taskExtra.clear();
                Automaton bestAutomaton = null;
                {
                    int bestValue = strategy.worstValue();
                    // Search among all the automata for the best one according to the current strategy...
                    assert(!strategy.isSpecial());
                    loop: for (Automaton aut: theAutomata)
                    {
                        // Skip all the too large automata
                        if (aut.nbrOfStates() > options.getComponentSizeLimit())
                            continue;
                        
                        // If the automaton has no transitions, add it to the task without further ado
                        if (aut.nbrOfTransitions() == 0)
                        {
                            //logger.info("It happened!!" + aut);
                            taskExtra.addAutomaton(aut);
                            continue;
                        }
                        
                        // Evaluate using current strategy
                        int thisValue = strategy.value(aut, eventToAutomataMap);
                        // Maximize or minimize?
                        if ((strategy.maximize() && thisValue > bestValue) ||
                            (strategy.minimize() && thisValue < bestValue))
                        {
                            bestAutomaton = aut;
                            bestValue = thisValue;
                            continue loop;
                        }
                        else if (bestValue == thisValue)
                        {
                            // They werue equal! Use lower priority strategy to make a decision!
                            //for (MinimizationStrategy nextStrategy: strategyList)
                            for (int i = strategyIndex+1; i<strategyList.size(); i++)
                            {
                                MinimizationStrategy nextStrategy = strategyList.get(i);
                                if (nextStrategy.isSpecial()) // Don't use special strategies here
                                {
                                    continue;
                                }
                                int nextStrategyBest = nextStrategy.value(bestAutomaton, eventToAutomataMap);
                                int nextStrategyThis = nextStrategy.value(aut, eventToAutomataMap);
                                // Better?
                                if ((nextStrategy.maximize() && nextStrategyThis > nextStrategyBest) ||
                                    (nextStrategy.minimize() && nextStrategyThis < nextStrategyBest))
                                {
                                    bestAutomaton = aut;
                                    continue loop;
                                }
                                // Worse?
                                if (!(nextStrategyThis == nextStrategyBest))
                                {
                                    continue loop;
                                }
                            }
                            // Equal in all regards? Compare alphabetical order to get reproducible results
                            if (aut.compareTo(bestAutomaton) < 0)
                            {
                                //logger.info(bestAutomaton + " " + aut);
                                bestAutomaton = aut;
                            }
                        }
                        //logger.info("Apa: " + aut);
                    }
                    // Got no result?
                    if (bestAutomaton == null)
                    {
                        return null;
                    }
                }
                
                /////////////////
                // SECOND STEP //
                /////////////////
                if (newAlgo)
                {
                    // Search among all automata
                    assert(!heuristic.isSpecial());
                    double bestValue = heuristic.worstValue();

                    // Find neighbours
                    neighbours.clear();
                    for (LabeledEvent event: bestAutomaton.getAlphabet())
                    {
                        if (event.isObservable())
                            neighbours.addAutomata(eventToAutomataMap.get(event));
                    }
                    neighbours.removeAutomaton(bestAutomaton);
                    /*
                    if (neighbours.size() == 0)
                    {
                        logger.info("Disjoint!! " + bestAutomaton + " size " + bestAutomaton.nbrOfStates());
                    }
                     */
                    loop: for (Automaton aut: neighbours)
                    {
                        // Skip all the too large automata
                        if (aut.nbrOfStates() > options.getComponentSizeLimit())
                            continue loop;
                        
                        // Now we have a candidate
                        selection.clear();
                        selection.addAutomaton(bestAutomaton);
                        selection.addAutomaton(aut);
                        
                        // Evaluate selection
                        double thisValue = heuristic.value(selection, eventToAutomataMap, targetAlphabet);
                        // Maximize or minimize?
                        if ((heuristic.maximize() && thisValue > bestValue) ||
                            (heuristic.minimize() && thisValue < bestValue))
                        {
                            //logger.info(heuristic + ", this: " + thisValue + ", selection: " + selection);
                            bestValue = thisValue;
                            taskAutomata.clear();
                            taskAutomata.addAutomata(selection);
                            continue loop;
                        }
                        else if (bestValue == thisValue)
                        {
                            // Use lower priority heuristic to make a decision!
                            for (int i = heuristicIndex+1; i<heuristicList.size(); i++)
                            {
                                MinimizationHeuristic nextHeuristic = heuristicList.get(i);
                                double nextHeuristicBest = nextHeuristic.value(taskAutomata, eventToAutomataMap,
                                    targetAlphabet);
                                double nextHeuristicThis = nextHeuristic.value(selection, eventToAutomataMap,
                                    targetAlphabet);
                                if ((nextHeuristic.maximize() && nextHeuristicThis > nextHeuristicBest) ||
                                    (nextHeuristic.minimize() && nextHeuristicThis < nextHeuristicBest))
                                {
                                    //logger.info(nextHeuristic + ", this: " + nextHeuristicThis + ", selection: " + selection);
                                    taskAutomata.clear();
                                    taskAutomata.addAutomata(selection);
                                    continue loop;
                                }
                                else if (nextHeuristicThis != nextHeuristicBest)
                                {
                                    // Different and worse value!
                                    break;
                                }
                            }
                        }
                    }
                }
                else
                {
                    // Find the automaton with the highest "local to total" (number of events)
                    // ratio with respect to bestAutomaton
                    Alphabet alphaA = bestAutomaton.getAlphabet();
                    double bestLocalRatio = 0;
                    double bestCommonRatio = 0;
                    int bestSize = Integer.MAX_VALUE;
                    
                    // Find neighbours
                    neighbours.clear();
                    for (LabeledEvent event: bestAutomaton.getAlphabet())
                    {
                        if (event.isObservable())
                            neighbours.addAutomata(eventToAutomataMap.get(event));
                    }
                    neighbours.removeAutomaton(bestAutomaton);
                    if (neighbours.size() == 0)
                    {
                        logger.info("Disjoint!! " + bestAutomaton);
                    }
                    for (Automaton aut: neighbours)
                    {
                        Alphabet alpha = aut.getAlphabet();
                        // We have a new cndidate!
                        selection.clear();
                        selection.addAutomaton(bestAutomaton);
                        selection.addAutomaton(aut);
                        
                        // Skip all the too large automata
                        if (aut.nbrOfStates() > options.getComponentSizeLimit())
                            continue;
                        
                        // Skip self
                        if (bestAutomaton == aut)
                            continue;
                        
                        // If there are no common events, try next automaton
                        //int nbrOfCommonEvents = alphaA.nbrOfCommonEvents(alpha);
                        Alphabet commonEvents = MinimizationHelper.getCommonEvents(selection, eventToAutomataMap);
                        int nbrOfCommonEvents = commonEvents.size();
                        if (nbrOfCommonEvents == 0)
                        {
                            if ((bestLocalRatio == 0) && (bestCommonRatio == 0) &&
                                (aut.nbrOfStates() < bestSize))
                            {
                                bestSize = aut.nbrOfStates();
                                taskAutomata.clear();
                                taskAutomata.addAutomata(selection);
                            }
                            continue;
                        }
                        
                        // Calculate the alphabet of local events
                        Alphabet localEvents = MinimizationHelper.getLocalEvents(selection, eventToAutomataMap);
                        localEvents.minus(targetAlphabet); // Ignore events from targetAlphabet!
                        
                        // Find ratios
                        int nbrOfLocalEvents = localEvents.size();
                        int unionAlphabetSize = alphaA.size() + alpha.size() - nbrOfCommonEvents;
                        double thisLocalRatio = ((double) nbrOfLocalEvents)/((double) unionAlphabetSize);
                        double thisCommonRatio = ((double) nbrOfCommonEvents)/((double) unionAlphabetSize);
                        
                        // Improvement?
                        if (thisLocalRatio > bestLocalRatio)
                        {
                            bestLocalRatio = thisLocalRatio;
                            taskAutomata.clear();
                            taskAutomata.addAutomata(selection);
                        }
                        else if ((bestLocalRatio == 0) && (thisCommonRatio > bestCommonRatio))
                        {
                            bestCommonRatio = thisCommonRatio;
                            taskAutomata.clear();
                            taskAutomata.addAutomata(selection);
                        }
                    }
                    if (stopRequested)
                    {
                        return null;
                    }
                    
                    // Was the system disjoint?
                    if ((bestLocalRatio == 0) && (bestCommonRatio == 0))
                    {
                        logger.warn("The system has disjoint parts. They should " +
                            "be treated separately.");
                    }
                }
            }
            // We're ready?
            if (taskAutomata.size() > 0)
            {
                break;
            }
        }
        // A choice has been made?
        if (taskAutomata.size() == 0)
        {
            logger.info("Could not find a task! Everything is disjoint?");
            return null;
        }
        
        // Add extra automata (single state automata found along the way) to task
        taskAutomata.addAutomata(taskExtra);
        
        // Which events should be hidden?
        Alphabet hideThese = MinimizationHelper.getLocalEvents(taskAutomata, eventToAutomataMap);
        hideThese.minus(targetAlphabet);
        //System.out.println("Task: " + taskAutomata + ", " + hideThese);
        // Result found! Return!
        return new MinimizationTask(taskAutomata, hideThese);
    }
    
    /**
     * Composes automata and minimizes the result with hideThese considered as epsilon
     * events.
     */
    private Automaton monolithicMinimization(Automata automata, Alphabet hideThese)
    throws Exception
    {
        //System.err.println("Minimizing " + automata + ", hiding: " + hideThese);
        
        // Synchronize, or if there's just one automaton, just find it
        Automaton aut;
        if (automata.size() > 1)
        {
            // Synch
            SynchronizationOptions synchOptions = SynchronizationOptions.getDefaultSynchronizationOptions();
            synchOptions.setUseShortStateNames(useShortStateNames);
            aut = AutomataSynchronizer.synchronizeAutomata(automata, synchOptions);
            
            // Examine for largest sizes
            if (aut.nbrOfStates() > mostStates)
            {
                mostStates = aut.nbrOfStates();
            }
            if (aut.nbrOfTransitions() > mostTransitions)
            {
                mostTransitions = aut.nbrOfTransitions();
            }
            totalStates += aut.nbrOfStates();
            totalTransitions += aut.nbrOfTransitions();
        }
        else
        {
            aut = automata.getFirstAutomaton();
        }
        
        // Return miminisation of aut
        return monolithicMinimization(aut, hideThese);
    }

    /**
     * Composes automata and minimizes the result with hideThese considered as epsilon
     * events.
     */
    private Automaton monolithicMinimization(Automaton aut, Alphabet hideThese)
    throws Exception
    {
        /*
        // If supervision equivalence, make the result a kripke automaton!
        if (options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE)
        {
            aut = new KripkeAutomaton(aut); 
        }
        */
               
        // Is it at all possible to minimize? (It may actually be possible even
        // if there are no epsilons)
        //if (hideThese.size() > 0 || aut.nbrOfEpsilonTransitions() > 0)
        {
            // Minimize!
            AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);
            minimizer.useShortStateNames(useShortStateNames);
            threadToStop = minimizer;
            Automaton newAut = minimizer.getMinimizedAutomaton(options, hideThese);
            aut = newAut;
            threadToStop = null;
            if (stopRequested)
            {
                return null;
            }
        }

        // Remove inadequate events
        Alphabet inadequate = aut.getInadequateEvents(true);
        if (inadequate.size() > 0)
        {
            //logger.fatal("Hiding inadequate events " + inadequate + " in " + aut + ".");
            aut.hide(inadequate, preserveControllability);

            // Adjust eventToAutomataMap
            for (LabeledEvent event: inadequate)
            {
                Automata sharers = eventToAutomataMap.get(event);

                sharers.removeAutomaton(aut);
                if (sharers.size() == 0)
                {
                    eventToAutomataMap.remove(event);
                    logger.info("Event " + event + " was removed altogether from inadequateness.");
                }
            }
             
            // Did any event become local?
            Alphabet local = new Alphabet();
            Automata localAutomata = new Automata();
            for (LabeledEvent event: eventToAutomataMap.keySet())
            {
                Automata value = eventToAutomataMap.get(event);
                if (value.size() == 1)
                {
                    local.addEvent(event);
                    localAutomata.addAutomata(value);
                }
            }

            if (localAutomata.size() > 0)
            {
                logger.verbose("The automata " + localAutomata + " now have local events: " + local);
            }
        }
        
        /*
        // Remove blocked events from other automata?
        Alphabet blocked = aut.getBlockedEvents();
        if (blocked != null)
        {
            logger.info("Blocked: " + blocked + " in " + aut);
        }
         */
            
		/*
        if (useShortStateNames)
        {
            aut.setName("");
            aut.setComment("" + nameIndex++);
        }
		*/

        return aut;
    }
    
    /**
     * Method that stops AutomataMinimizer as soon as possible.
     *
     * @see  ExecutionDialog
     */
    public void requestStop()
    {
        stopRequested = true;
        logger.debug("AutomataMinimizer requested to stop.");
        // Stop current minimization thread!
        if (threadToStop != null)
        {
            threadToStop.requestStop();
        }
    }
    
    public boolean isStopped()
    {
        return stopRequested;
    }
}
