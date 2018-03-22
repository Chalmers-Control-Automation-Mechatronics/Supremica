
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.abstraction.OPSearchAutomatonSimplifier;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;
import org.supremica.automata.StateSets;
import org.supremica.automata.TauEvent;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.AutomataVerifier;
import org.supremica.automata.algorithms.AutomatonSynthesizer;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.gui.ActionMan;
import org.supremica.gui.ExecutionDialog;
import org.supremica.properties.Config;
import org.supremica.util.SupremicaException;

/**
 * This class can be used to reduce the size of an automaton while
 * preserving different equivalences.
 */
public class AutomatonMinimizer
    implements Abortable
{
    int l=0;
    private static Logger logger = LogManager.getLogger(AutomatonMinimizer.class);

    // Stoppable stuff
    private ExecutionDialog executionDialog = null;
    private boolean abortRequested = false;

    /** The automaton being minimized (may be a copy of the original). */
    private Automaton theAutomaton;

    /** The supplied options. */
    private MinimizationOptions options;

    // Use short names (state names are either integers or based on their parent states)
    private boolean useShortNames = false;

    // TODO Remove unused variable ?
    // private final Map<EquivalenceClass,Boolean> moreThanOneSilenceOutgoing = new HashMap<EquivalenceClass,Boolean>();

    /**
     * Basic constructor.
     * @param theAutomaton an <code>Automaton</code> value
     */
    public AutomatonMinimizer(final Automaton theAutomaton)
    {
        this.theAutomaton = theAutomaton;

    }

    /**
     * Returns minimized automaton, minimized with respect to the supplied options.
     * Hides all events except for the ones in "targetAlphabet".
     */
    public Automaton getMinimizedAutomaton(final MinimizationOptions options)
    throws Exception
    {

        if (options.getTargetAlphabet() != null)
        {
            return getMinimizedAutomaton(options, AlphabetHelpers.minus(theAutomaton.getAlphabet(), options.getTargetAlphabet()));
        }
        else
        {
            return getMinimizedAutomaton(options, new Alphabet());
        }
    }

    public Automaton getMinimizedAutomaton(final MinimizationOptions options, final Alphabet hideThese, final boolean synthesis)
    throws Exception
    {

        if (synthesis)
        {
            return getMinimizedAutomaton(options, hideThese);
        }
        else
        {
            getMinimizedAutomaton(options,hideThese);

            return theAutomaton;
        }
    }

    /**
     * Returns minimized automaton, minimized with respect to the supplied options.
     */



    public Automaton getMinimizedAutomaton(final MinimizationOptions options, final Alphabet hideThese)
    throws Exception
    {

        this.options = options;

        // Check sanity
        if ((options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE && !theAutomaton.isPlant())
                ||options.getMinimizationType() == EquivalenceRelation.SYNTHESISABSTRACTION && !theAutomaton.isPlant())
            throw new Exception("Automaton " + theAutomaton + " must be plantified before minimisation.");

        // Don't notify listeners!
        theAutomaton.beginTransaction();

        // Hide the events!
        final boolean preserveControllability = (options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE)
                ||(options.getMinimizationType() == EquivalenceRelation.SYNTHESISABSTRACTION);

        if (options.getMinimizationType() == EquivalenceRelation.SYNTHESISABSTRACTION)
		{
            theAutomaton.synthesisHide(hideThese, preserveControllability);
        }
		else
		{
			theAutomaton.hide(hideThese, preserveControllability);
        }
        // Message

        final int before = theAutomaton.nbrOfStates();
        final int epsilons = theAutomaton.nbrOfEpsilonTransitions();
        final int total = theAutomaton.nbrOfTransitions();
        logger.debug("Minimizing " + theAutomaton + " with " + before +
                    " states and " + epsilons + " epsilon transitions (" +
                    Math.round(100*(((double) epsilons)*100/total))/100.0 + "%).");

        // Are the options valid?
        if (!options.isValid())
        {
            throw new IllegalArgumentException("Invalid minimization options");
        }

        // Make accessible
        {

//            final SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptionsS();
//            StateSet st= theAutomaton.getStateSet();
//            StateSet st1=(StateSet)st.clone();
//            StateSet st2=new StateSet();
//            while(st1.size()>0){
//                State s=st1.remove();
//                if(s.isAccepting()){
//                    st2.add(s);
//                }
//            }
//            logger.debug("Accepting states "+st2+" the automaton name is "+theAutomaton.getName());
//            final AutomatonSynthesizer synth = new AutomatonSynthesizer(theAutomaton, synthOptions);
//            synth.doReachable(true);
            final LinkedList<State> toBeRemoved = new LinkedList<State>();
            for (final Iterator<State> it = theAutomaton.stateIterator(); it.hasNext(); )
            {
                final State state = it.next();
                if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
                {
                    logger.debug("The state " + state + " will be removed since it is not reachable.");
                    toBeRemoved.add(state);
                }

                // The forbidden states may actually be reachable?
                if (state.isForbidden())
                {
                    //logger.fatal("The state " + state + " is forbidden.");
                }
            }
            while (toBeRemoved.size() != 0)
            {
                theAutomaton.removeState(toBeRemoved.remove(0));
            }
        }

        //////////////
        // MINIMIZE //
        //////////////

        // Find out what to do
        final EquivalenceRelation equivalenceRelation = options.getMinimizationType();
        if (equivalenceRelation == EquivalenceRelation.OP)
        {
          final ProductDESProxyFactory factory =
            ProductDESElementFactory.getInstance();
          final AutomataToWaters importer = new AutomataToWaters(factory);
          final AutomatonProxy aut = importer.convertAutomaton(theAutomaton);
          final Collection<EventProxy> hidden = new ArrayList<EventProxy>(2);
          final String tau = Config.MINIMIZATION_SILENT_EVENT_NAME.getAsString();
          final String tau_c =
            Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.getAsString();
          final String tau_u =
            Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString();
          for (final EventProxy event : aut.getEvents()) {
            final String name = event.getName();
            if (name.equals(tau) || name.equals(tau_c) || name.equals(tau_u)) {
              hidden.add(event);
            }
          }
          final KindTranslator translator = IdenticalKindTranslator.getInstance();
          final OPSearchAutomatonSimplifier simp =
            new OPSearchAutomatonSimplifier(aut, hidden, factory, translator);
          simp.run();
          final AutomatonProxy result = simp.getComputedAutomaton();
          if (result != aut) {
            final ProjectBuildFromWaters exporter =
              new ProjectBuildFromWaters(null);
            theAutomaton = exporter.build(result);
            theAutomaton.beginTransaction();
          }
          theAutomaton.setComment("op(" + theAutomaton.getName() + ")");
          theAutomaton.setName("");
          theAutomaton.endTransaction();
          return theAutomaton;
        }


        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        //Abstraction base on synthesis abstraction(DCDS2011).
        else if(equivalenceRelation == EquivalenceRelation.SYNTHESISABSTRACTION)
		{
            // Merge the states that only have one local outgoing transition.
//            theAutomaton.endTransaction();
            if (theAutomaton.getAlphabet().size() > 1)
            {

                mergeTriviallyObservationEquivalentStatesSynthesis(theAutomaton);
//                logger.debug("mergetrivially the name "+ theAutomaton.getName()+"nbrofStates "+theAutomaton.nbrOfStates());
            }
            mergeEpsilonLoopsSynthesis(theAutomaton);



           //Applying halfway synthesis.
            //!!! if we apply halfway synthesis before
            // mergetrivially it takes a long time to calculate the abstracted automaton!!!!

            halfWaySynthesis(theAutomaton);

            // Applying synthesis abstraction
            EquivalenceClasses equivClasses = new EquivalenceClasses();

                try
                {
                    // Find initial partitioning (based on marking, basically)
                    equivClasses = findInitialPartitioning(theAutomaton);
                    // Partition
                    findCoarsestPartitioning(equivClasses, equivalenceRelation);
                } catch (final Exception ex) {
                    requestAbort();
                    throw ex;
                }


            if (abortRequested)
            {
                return null;
            }
             theAutomaton.beginTransaction();
           final Iterator<?> equivClassIt = equivClasses.iterator();

                while (equivClassIt.hasNext())
                {
                    State blob = null;
                    final EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();
                    // Merge states in partitions (the partitions are separated by -1)!
                    while(currEquivClass.size()>0){
                        //logger.debug("part[" + i + "] = " + part[i] + ", " + states[part[i]].getName());
                        if (blob == null)
                        {
                            blob = currEquivClass.remove();
                        }
                        else
                        {
                            //logger.debug("Merging " + blob + " and " + states[part[i]]);
                            blob = MinimizationHelper.mergeSynthesisStates(theAutomaton, blob, currEquivClass.remove(), useShortNames);
                        }

                    }

                }

            if(!AutomataSynthesizer.renameBack()){
                   renameBackToOriginalEvents(theAutomaton);
         }
            return theAutomaton;



        }
        ////////////////////////////////////////////////////////////////////////////

        else if (equivalenceRelation == EquivalenceRelation.SUPERVISIONEQUIVALENCE)
        {
            //Coreachability is strange, forbidden and MAX_COST
            //coincides? Why!? I'll ignore forbidden states for now.
            if (theAutomaton.nbrOfForbiddenStates() > 0)
            {
                logger.warn("Supervision equivalence can not cope with previously forbidden states.");
                requestAbort();
                throw new IllegalArgumentException("Automaton contains forbidden states, which is currently not supported by supervision equivalence");
            }

            int countHWS = 0;
            final int countBSE = 0;

            /////////////////////////
            // "Halfway-synthesis" //
            /////////////////////////
            countHWS += halfWaySynthesis(theAutomaton);

            // Check if the library with the native methods is ok
            /*
            if (false && BisimulationEquivalenceMinimizer.libraryLoaded())
            {
                // tau_u-saturate!
                supervisionEquivalenceSaturate(theAutomaton);

                // Partition using native methods, long state names and STRONG bisimulation equivalence
                countBSE += BisimulationEquivalenceMinimizer.minimize(theAutomaton, false, true);
            }
            */

            // Message
            final int after = theAutomaton.nbrOfStates();
            logger.debug("There were " + before + " states before and " + after +
                        " states after the minimization. Reduction: " +
                        Math.round(100*(((double) (before-after))*100/before))/100.0 + "%.");

            // Start listening again
            theAutomaton.endTransaction();

            logger.debug("Halfway synthesis: " + countHWS + ", bisimulation equivalence: " + countBSE);

            totalHWS += countHWS;
            totalBSE += countBSE;

            // Finished!
            return theAutomaton;
        }

        //######################################################################
        // All the below relations use partitioning with respect to observation equivalence!

		//**** LANGUAGEEQUIVALENCE ****
        else if (equivalenceRelation == EquivalenceRelation.LANGUAGEEQUIVALENCE)
        {
            // Is this automaton nondeterministic?
            if (!theAutomaton.isDeterministic())
            {
                // Make deterministic
                final Determinizer determinizer = new Determinizer(theAutomaton);
                determinizer.execute();
                theAutomaton = determinizer.getNewAutomaton();
                theAutomaton.beginTransaction();
            }

            // Now we're ready for partitioning!
        }
		//**** OBSERVATIONEQUIVALENCE, CONFLICTEQUIVALENCE ****
        else if (equivalenceRelation == EquivalenceRelation.OBSERVATIONEQUIVALENCE ||
            equivalenceRelation == EquivalenceRelation.CONFLICTEQUIVALENCE)
        {
            // Merge silent loops and other obvious OE stuff (to save computation later)...
            // Don't bother if there is only one event in alphabet (epsilon or not)
            if (theAutomaton.getAlphabet().size() > 1)
            {
                final int trivialCount = mergeTriviallyObservationEquivalentStates(theAutomaton);
                //logger.debug("trivial count: "+trivialCount);
                // logger.debug("oes:"+totalOES);

                if (trivialCount > 0)
                {
                    logger.debug("Removed " + trivialCount + " trivially equivalent states " +
                                "before running the partitioning.");
                }
                final int loopCount = mergeEpsilonLoops(theAutomaton);
//                logger.debug("loop count: "+loopCount);
                if (loopCount > 0)
                {
                    logger.debug("Removed " + loopCount + " states involved in silent loops " +
                                "before running the partitioning.");
                }
                totalOES = totalOES + trivialCount + loopCount;
            }

            ////////////
            // RULE D //
            ////////////

            // Adjust marking based on epsilon transitions
            final int countD = rulePropagateMarking(theAutomaton);
            //logger.debug("countD: "+countD);
            //logger.debug(totalOES);
            totalD += countD;

            if (abortRequested)
            {
                return null;
            }

            ///////////////////////////
            // Conflict Equivalence- //
            // specific stuff        //
            ///////////////////////////

            if (equivalenceRelation == EquivalenceRelation.CONFLICTEQUIVALENCE)
            {
                // If there is just one event and it's epsilon,
                // it's easy! (If we don't care about state names.)
                if ((theAutomaton.getAlphabet().size() == 1) &&
                    (theAutomaton.getAlphabet().nbrOfUnobservableEvents() == 1) &&
                    useShortNames)
                {
                    // The conflict equivalent automaton is just one state, the initial state.
                    // Marked if the automaton is nonblocking, nonmarked otherwise.
                    final Automaton newAutomaton = new Automaton("min(" + theAutomaton.getName() + ")");
                    final State initial = newAutomaton.createUniqueState();
                    initial.setInitial(true);
                    newAutomaton.addState(initial);

                    // Accepting iff nonblocking
                    initial.setAccepting(AutomataVerifier.verifyMonolithicNonblocking(new Automata(theAutomaton)));

                    return newAutomaton;
                }

                //////////////////////////
                // RULES A, AA, B, C, F //
                //////////////////////////

                final int count = runConflictEquivalenceRules(theAutomaton);
                if (count > 0)
                {
                    logger.debug("Removed " + count + " states based on conflict equivalence " +
                                "before running the partitioning.");
                }
            }

            // Now we're ready for partitioning!
        }
        else
        {
            throw new Exception("Unknown equivalence relation(" + equivalenceRelation.toString() + ")");
        }

        if (abortRequested)
        {
            return null;
        }
        // Do the partitioning!
        final int statesBefore = theAutomaton.nbrOfStates();

        EquivalenceClasses equivClasses = new EquivalenceClasses();
        try {
          // Find initial partitioning (based on marking, basically)
          equivClasses = findInitialPartitioning(theAutomaton);
          // Partition
          findCoarsestPartitioning(equivClasses, equivalenceRelation);
        } catch (final Exception exception) {
          requestAbort();
          throw exception;
        }
        if (abortRequested) {
          return null;
        }
        theAutomaton.beginTransaction();
        // Build the minimised automaton based on the partitioning in equivClasses
        theAutomaton = buildAutomaton(equivClasses);

        final int diffSize = statesBefore - theAutomaton.nbrOfStates();
        totalOES += diffSize;
        //logger.debug("oes:"+totalOES);
        if (diffSize > 0)
        {
            logger.debug("Removed " + diffSize + " states based on partitioning with " +
                        "respect to observation equivalence.");
        }

        if (abortRequested)
        {
            return null;
        }

        // Some more conflict equivalent reductions may be possible now...
        if (equivalenceRelation == EquivalenceRelation.CONFLICTEQUIVALENCE)
        {
            //////////////////////////
            // RULES A, AA, B, C, F //
            //////////////////////////

            final int count = runConflictEquivalenceRules(theAutomaton);
            if (count > 0)
            {
                logger.debug("Removed " + count + " states based on conflict equivalence " +
                            "after running partitioning.");
            }
        }

        // Should we remove redundant transitions to minimize also with respect to transitions?
        if (options.getAlsoTransitions())
        {
            final int addedArcs = epsilonSaturate(theAutomaton, false);
            final int removedArcs = removeRedundantTransitions(theAutomaton);
            totalOET += removedArcs-addedArcs;
        }

        // Remove from alphabet epsilon events that are never used

        removeUnusedEpsilonEvents(theAutomaton);
        // Message
        final int after = theAutomaton.nbrOfStates();
        logger.debug("There were " + before + " states before and " + after +
                    " states after the minimization. Reduction: " +
                    Math.round(100*(((double) (before-after))*100/before))/100.0 + "%.");

        // Return the result of the minimization!
        return theAutomaton;

    }

    /*
    private void checkForEpsilonLoops(Automaton aut)
    {
        StateSet statesToExamine = new StateSet(aut.getStateSet());

        // Count the removed states
        int count = 0;

        // Do the merging
        while (statesToExamine.size() != 0)
        {
            // Get and remove arbitrary state
            State one = statesToExamine.remove();

            // Find forwards and backwards closures
            StateSet forwardsClosure = one.epsilonClosure(false);
            StateSet backwardsClosure = one.backwardsEpsilonClosure();

            // Merge all states in the intersection!
            forwardsClosure.remove(one); // Skip self
            while (forwardsClosure.size() != 0)
            {
                State two = forwardsClosure.remove();

                if (backwardsClosure.contains(two))
                {
                    // A loop!
                    count++;
                    //logger.debug(one);
                }
            }
        }

        if (count > 0)
        {
            logger.verbose("Loops! " + count);
        }
    }
     */
     public static void renameBackToOriginalEvents(final Automaton aut)
     {
          for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
          {
              final Arc arc = arcIt.next();
              final LabeledEvent event = arc.getEvent();

              if(event.isTauEvent())
              {
                final LabeledEvent orig = ((TauEvent)event).getOriginalEvent();
                assert(orig != null); // It should never happen that orig == null!
                assert(aut.getAlphabet().contains(event));  // It should never happen that event is not in the alphabet
                if( !aut.getAlphabet().contains(orig) )  // should this be an assertion?
                 {
                     aut.getAlphabet().addEvent(orig);
                 }
                arc.setEvent(orig);
                aut.getAlphabet().removeEvent(event);
             }
          }
      }



    public static void renameBackToOriginalEvents(final Automata auto)
    {
        for(final Automaton aut:auto)
        {
           renameBackToOriginalEvents(aut);
        }
    }
    /**
     * Find the initial partitioning of this automaton, based on the marking and forbidden
     * states (marking can be ignored using the minimization option ignoreMarking.
     */
    private EquivalenceClasses findInitialPartitioning(final Automaton aut)
    {
        if (aut == null)
        {
            return null;
        }

        // Divide the state space into three initial equivalence classes, based on markings
        final EquivalenceClass acceptingStates = new EquivalenceClass(); //EqClassFactory.getEqClass();
        final EquivalenceClass forbiddenStates = new EquivalenceClass(); //EqClassFactory.getEqClass();
        final EquivalenceClass ordinaryStates = new EquivalenceClass(); //EqClassFactory.getEqClass();

        // Examine each state for which class it fits into
//        final Iterator<State> stateIt = aut.stateIterator();
//        while (stateIt.hasNext())
//        {
//            final State currState = stateIt.next();
//
//            if (currState.isForbidden() && !options.getIgnoreMarking())
//            {
//                currState.setStateSet(forbiddenStates);
//                forbiddenStates.add(currState);
//            }
//            else if (currState.isAccepting() && !options.getIgnoreMarking())
//            {
//                currState.setStateSet(acceptingStates);
//                acceptingStates.add(currState);
//            }
//            else
//            {
//                currState.setStateSet(ordinaryStates);
//                ordinaryStates.add(currState);
//            }
//        }




        final StateSet markedStates = new StateSet();
        final StateSet ordStates = new StateSet();
        for (final Iterator<State> stateIt = aut.stateIterator(); stateIt.hasNext();)
        {

            final State currState = stateIt.next();

            if (currState.isAccepting(true))
            {
                currState.setStateSet(acceptingStates);
                acceptingStates.add(currState);
                markedStates.add(currState);

            }
            else if (currState.isForbidden() && !options.getIgnoreMarking())
            {
                currState.setStateSet(forbiddenStates);
                forbiddenStates.add(currState);
            }
            else
            {
                currState.setStateSet(ordinaryStates);
                ordinaryStates.add(currState);
                ordStates.add(currState);
            }
        }


        // Adjust the marking!
//        while (markedStates.size()>0)
//        {
//            final StateSet closure = markedStates.remove().backwardsEpsilonClosure();
//
//            for (final Iterator<State> closureIt = closure.iterator(); closureIt.hasNext(); )
//            {
//                final State state = closureIt.next();
//                if (!acceptingStates.contains(state))
//                {   acceptingStates.add(state);
//                    state.setStateSet(acceptingStates);
//                    if(ordinaryStates.contains(state)){
//                        ordinaryStates.remove(state);
//                    }
//                }
//
//
//            }
//        }

        // Put these new classes into a single object
        // Only if there are any states in the class...
        final EquivalenceClasses equivClasses = new EquivalenceClasses();
        if (acceptingStates.size() > 0)
        {
            equivClasses.add(acceptingStates);
        }

        if (ordinaryStates.size() > 0)
        {
//            logger.debug("the ordinary class "+ordinaryStates);
            equivClasses.add(ordinaryStates);
        }

        if (forbiddenStates.size() > 0)
        {
            equivClasses.add(forbiddenStates);
        }
//        logger.debug("initial partitioning base on marking "+equivClasses);
//        for(StateSet cl:equivClasses){
//        for(State st:cl){
////            logger.debug("the State "+st);
////            logger.debug("class "+st.getStateSet());
//        }
//    }
        return equivClasses;

    }

    /**
     * Generate states with short names!
     */
    public void useShortStateNames(final boolean bool)
    {
        useShortNames = bool;
    }

    /**
     * Returns the minimized automaton, based on the partitioning in equivClasses.
     */
    private Automaton buildAutomaton(final EquivalenceClasses equivClasses)
    throws Exception
    {


        final Automaton newAutomaton = new Automaton(theAutomaton.getName());

        // Don't listen to this!
        newAutomaton.beginTransaction();

        newAutomaton.setType(theAutomaton.getType());
        newAutomaton.getAlphabet().union(theAutomaton.getAlphabet()); // Odd... but it works.

//        logger.debug("State automaton"+state);
        // Associate one state with each equivalence class
        //logger.debug("eqclass"+equivClasses);
        Iterator<?> equivClassIt = equivClasses.iterator();
        while (equivClassIt.hasNext())
        {
            final EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();



            //logger.debug("stateto bemerged"+currEquivClass);
            final State currState = currEquivClass.getSingleStateRepresentation();
            if (currEquivClass.hasInitialState())
            {
                // This is where the problem is... the name may have been used already!
                // we should set the name already in the getSingleStateRepresentation method!
                newAutomaton.addState(currState);
                newAutomaton.setInitialState(currState);
            }
            else
            {
                newAutomaton.addState(currState);
            }
        }

        /* TODO Remove unused variables ?
            final Map<Arc,LabeledEvent> checkToBeRemoved= new HashMap<Arc, LabeledEvent>();
            final Map<LabeledEvent,Boolean> checkEvent= new HashMap<LabeledEvent, Boolean>();
            final ArcSet toBeRemoved=new ArcSet();
        */

        // Build all transitions
        equivClassIt = equivClasses.iterator();
        while (equivClassIt.hasNext())
        {
            if (abortRequested)
            {
                return null;
            }


            final EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();

            final State fromState = currEquivClass.getSingleStateRepresentation();

            // Note that the below only returns an iterator to SOME of the outgoing arc
            // from the EquivalenceClass (from one state only). This is OK, though, since
            // all states in the equivalence class should have the same outgoing transitions
            // (with respect to equivalence classes), otherwise they are not really equivalent!
            //Iterator<Arc> outgoingArcsIt = currEquivClass.get().outgoingArcsIterator();

            // Since the automaton isn't saturated, we have to loop through all arcs!

            final Iterator<Arc> outgoingArcsIt = currEquivClass.outgoingArcsIterator();

            while (outgoingArcsIt.hasNext())
            {
                final Arc currArc = outgoingArcsIt.next();
                final LabeledEvent currEvent = currArc.getEvent();
//                if(!newAlphabet.contains(currEvent)){
//                    newAlphabet.add(currEvent);
//                }


                final State oldToState = currArc.getToState();
//

                // Add the new arc!
                final EquivalenceClass nextEquivalenceClass = (EquivalenceClass) oldToState.getStateSet();
                if(nextEquivalenceClass.size()>0){
                final State toState = nextEquivalenceClass.getSingleStateRepresentation();

                final Arc newArc = new Arc(fromState, toState, currEvent);
                // check if the tau event only apears in selfloops( Tau loop removal).
//                if (currEvent.isUnobservable()){
//                    if(newArc.isSelfLoop()){
//                        //if the event has been checked before...
//                            if(checkEvent.containsKey(currEvent)){
//                                // and it was in selfloop only...
//                                if(checkEvent.get(currEvent)){
//                                    // add the arc to the removed arc set.
//                                    checkToBeRemoved.put(newArc, currEvent);
//                                    toBeRemoved.add(newArc);
//                                }
//                            }
//                        // but if the event has been checked before and now it is in selfloop
//                            //the arc should be added in the remove arc set.
//                        else{
//                            checkEvent.put(currEvent, true);
//                            checkToBeRemoved.put(newArc, currEvent);
//                            toBeRemoved.add(newArc);
//                        }
//                    }
//                    // if the arc is not selfloop, then the corresponding event will
//                    // have a false value. If these event had been apeared in selfloop
//                    // and the removed arc set contains arc that has this event,
//                    //these arc should be removed.
//                    else{
//
//                        checkEvent.put(currEvent, false);
//
////                        logger.debug(newArc);
////                        logger.debug(checkToBeRemoved.containsKey(newArc));
//                        if(checkToBeRemoved.containsValue(currEvent)){
//
//                            toBeRemoved.removeAll(checkToBeRemoved.keySet());
//                        }
//                    }
//
//                }

                // If we should minimize the number of transitions, make sure a transition is never
                // present more than once (this is performed in an ugly way below)
                //if (!(options.getAlsoTransitions() && newArc.getFromState().containsOutgoingArc(newArc)))

                if (!newArc.getFromState().containsOutgoingArc(newArc))
                {
                    // Add arc
                    newAutomaton.addArc(newArc);
                }
            }
            }

        }


        // Start listening again


        // selfloop removel.
//        while(toBeRemoved.size()>0){
//            final Arc remove = toBeRemoved.getFirst();
//            final State stateFrom = remove.getFromState();
//            final State stateTo = remove.getToState();
//
//            if( stateFrom.containsOutgoingArc(remove)&& stateTo.containsIncomingArc(remove)){
//                newAutomaton.removeArc(toBeRemoved.removeFirst());
//            }
//        }


        newAutomaton.endTransaction();
        // Return the new automaton!

        return newAutomaton;
    }

    /**
     * Finds the coarsest partitioning of the supplied equivalence classes.
     * In each partition, all states have corresponding outgoing arcs. Based
     * on this, an automaton with a minimal number of states wrt OE can be generated...
     */
    private void findCoarsestPartitioning(final EquivalenceClasses equivClasses, final EquivalenceRelation equivalenceRelation)
    {

        // Repeat until no refinement occurs.
        boolean refined = true;



        while (refined)
        {


            //logger.debug("OEEEEEEE        eq"+equivClasses);
            refined = false;

            if (executionDialog != null)
            {
                executionDialog.setValue(equivClasses.size());
            }

            // Split all current equivClasses
            final Object[] array = equivClasses.toArray();

            for (int i=0; i<array.length; i++)
            {
                //logger.debug("array"+l+i+array[i]);
                if (abortRequested)
                {
                    return;
                }

                final EquivalenceClass currClass = (EquivalenceClass) array[i];

                // Don't try to refine single-state classes!
                if (currClass.size() > 1)
                {
                    // refined = refined || partition(equivClasses, currClass); // WRONG! SHOULD REFINE EITHER WAY!

                    refined = partition(equivClasses, currClass, equivalenceRelation) || refined;
                }
            }
        }

    }

    /**
     * Partitions equivClass by all events.
     *@return true if a partitioning was made, false otherwise.
     */
    private boolean partition(final EquivalenceClasses equivClasses, final EquivalenceClass equivClass, final EquivalenceRelation equivalenceRelation)
    {
        boolean refined = false;
     //logger.debug("eqq"+equivClasses);
//        logger.debug(theAutomaton.getAlphabet());
        for (final Iterator<?> eventIt = theAutomaton.getAlphabet().iterator(); eventIt.hasNext(); )
        {
            if (abortRequested)
            {
                return false;
            }

            final LabeledEvent currEvent = (LabeledEvent) eventIt.next();

            // refined = partition(equivClasses, equivClass, currEvent) || refined; // WRONG!
            refined = refined || partition(equivClasses, equivClass, currEvent, equivalenceRelation);

            /*
            // Return as soon as a change is found!
            if (refined)
            {
                return true;
            }
             */
        }

        return refined;
    }
    /**
     * Partitions equivClass by the event e.
     * @return true if a partitioning was made, false otherwise.
     */
     // "Split" class on event 'e', i.e. based on where the 'e'-transitions lead
    private boolean partition(final EquivalenceClasses equivClasses,
        final EquivalenceClass equivClass, final LabeledEvent e, final EquivalenceRelation equivalenceRelation)
    {
        boolean extraCheck = false;
       final EquivalenceClass equivClass2= (EquivalenceClass)equivClass.clone();
       EquivalenceClassHolder newEquivClassHolder = new EquivalenceClassHolder();
       loop:if(equivalenceRelation == EquivalenceRelation.SYNTHESISABSTRACTION){
            if(e.isControllable()){
                for(final State st:equivClass2){
                    if(st.nbrOfOutgoingUnobservableArcs()>1){
                        extraCheck = true;
                        break;
                    }
                }
                if(extraCheck){
                    while(equivClass2.size()>0){
                     final State currState=equivClass2.remove();

                         StateSet nextcurr=new StateSet();
                         nextcurr = currState.epsilonClosureOnlyFirst(false,true,false,true);
                         while(nextcurr.size()>0){
                             final State next=nextcurr.remove();
                             newEquivClassHolder = splitSynthesisACUo(equivClass, currState, next ,e);
                             if(newEquivClassHolder.size()>1){
                                 break loop;
                             }
                        }

                    }
               }
                else{
                    newEquivClassHolder = splitSynthesisA(equivClass, e);
                }
         }
            else{

                 newEquivClassHolder = splitSynthesisA(equivClass, e);
            }

       }
       else{
           newEquivClassHolder = oeSplit(equivClass, e);
       }

        // Do the states in equivClass have different behaviour on 'e'?
//        logger.debug("class holder in partintion "+newEquivClassHolder);
        if (newEquivClassHolder.size() > 1)
        {
            // Throw old class away!
            equivClasses.remove(equivClass);
            equivClass.clear();

            // Add the new classes to equivClasses
            equivClasses.addAll(newEquivClassHolder);

            if (executionDialog != null)
            {
                executionDialog.setValue(equivClasses.size());
            }

            // Help garbage collector
            newEquivClassHolder.clear();

            return true;
        }
        else
        {
            // All states in equivClass classifies to the same class
            // (at this stage)
            // No need to do anything except maybe helping
            // the garbage collector
            newEquivClassHolder.clear();

            return false;
        }
    }
    /**
     * Split an equivalence class according to what can be reached on this event
     * Return all new equivalence classes in the holder
     * If the holder contains only one entry, all reached states have the same eq class
     * (at this point)
     *@param eqClass the class to be split.
     *@param e the event that should be considered.
     */
    private EquivalenceClassHolder oeSplit(final StateSet eqClass, final LabeledEvent e)
 {
        final EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();

        /*
        // Build a list of equivalance classes that e transfers to
        // from each of the states in this eq-class
        // Note, for each state there is only one successor state
        // for this event (determinism)
        // NOOOOOOOOOOOOOOOOO! NOT IT'S NOT DETERMINISTIC!!
        Iterator<State> stateIt = eqClass.iterator();
        while (stateIt.hasNext())
        {
            State currState = stateIt.next();
            State nextState = currState.next(e);
            EquivalenceClass nextEquivalenceClass = null;
            if (nextState != null)
            {
                nextEquivalenceClass = (EquivalenceClass) nextState.getStateSet();
            }
            newEquivalenceClassHolder.addState(currState, nextEquivalenceClass);
        }

        return newEquivalenceClassHolder;
         */

        // Build a list of equivalence classes that e transfers to
        // from each of the states in this eq-class
        // Note, for each state there may be several successor state
        // for this event (nondeterminism)
        final Iterator<State> stateIt = eqClass.iterator();
        while (stateIt.hasNext())
        {
            if (abortRequested)
            {
                newEquivalenceClassHolder.clear();
                return newEquivalenceClassHolder;
            }

            final State currState = stateIt.next();
            StateSet nextStates;
            if (e.isUnobservable())
            {
                // Find the states that can be reached by epsilons,
                // i.e. the epsilonclosure of currState
                nextStates = currState.epsilonClosure(true);
            }
            else
            {
                // Find the states that can be reached by e from the epsilonclosure of currState
                nextStates = currState.nextStates(e, true);
            }
            final EquivalenceClass nextClass = new EquivalenceClass();
            for (final Iterator<State> nextIt = nextStates.iterator(); nextIt.hasNext(); )
            {
                final State nextState = nextIt.next();
                final EquivalenceClass thisNextClass = (EquivalenceClass) nextState.getStateSet();
                nextClass.addAll(thisNextClass);
            }
            newEquivalenceClassHolder.addState(currState, nextClass);
        }

        return newEquivalenceClassHolder;
    }

//##############################################################################

    /**
     * Split an equivalence class according to synthesis observation equivalnce. This
     * method is used when the event is controllable and unobservable (DCDS 2011).
     *@param eqClass the class to be split.
     *@param e the event that should be considered.
     */

    private EquivalenceClassHolder splitSynthesisACUo(final StateSet eqClass, final State currstate, final State next, final LabeledEvent e)
    {
//   logger.debug("eqClass "+eqClass);
//   logger.debug("event "+e);
//        for(State st:eqClass){
//            logger.debug("the State "+st);
//            logger.debug("eqvalence calss "+st.getStateSet());
//        }
//        logger.debug("next "+next);
        final EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();
        final Iterator<State> stateIt = eqClass.iterator();
        final EquivalenceClass curreq=(EquivalenceClass)currstate.getStateSet();
        final StateSet nexteq = next.getStateSet();
        while (stateIt.hasNext())
        {
            boolean extraCheck=false;
            if (abortRequested)
            {
                newEquivalenceClassHolder.clear();
                return newEquivalenceClassHolder;
            }

            final State currState = stateIt.next();
//
            StateSet nextStates=new StateSet();
            if(currstate.equalState(currState)){
                nextStates.add(next);
              nextStates.add(currstate);

//
             }
             else{

                nextStates = currState.epsilonClosure(false,true,true,true);
                if(nextStates.contains(currState)){
                    nextStates.remove(currState);
                }
//
                extraCheck=true;
             }
//          logger.debug("nextState "+ nextStates);
            final EquivalenceClass nextClass = new EquivalenceClass();
            // Since we dont include the currState in nextStates, the corresponding
            // equivalnce class should be added to nextClass.
            nextClass.addAll(curreq);
            final EquivalenceClass classCurr= (EquivalenceClass)currstate.getStateSet();
            for (final Iterator<State> nextIt = nextStates.iterator(); nextIt.hasNext(); )
            {

                final State nextState = nextIt.next();

                final EquivalenceClass thisNextClass = (EquivalenceClass) nextState.getStateSet();
                // If this state is selected it means it has been reached by a local controllable event.
                // So it must be in the same class as the the currState or the state that is reached by the currState.
//               logger.debug("nextState "+nextState);
//                logger.debug("thisNextClass "+thisNextClass);
                if(extraCheck&&nextState.isSelected()){
                    if(nexteq.equals(thisNextClass)|| classCurr.equals(thisNextClass) ){
                        nextClass.addAll(thisNextClass);
//                  logger.debug("nextClass "+nextClass);
                    }
                    // if it is not the above case break and dont add anything to nextclass.
                    else{
                        break;
                    }
                 }
                // if it is not reached by a controllable local transition then non of those extra check is neede.
                else{
                    nextClass.addAll(thisNextClass);
//                 logger.debug("nextClass "+nextClass);
                }
            }
            newEquivalenceClassHolder.addState(currState, nextClass);
        }
//    logger.debug("after CUO "+newEquivalenceClassHolder);
        return newEquivalenceClassHolder;
    }

     //#########################################################################

     /**
     * Split an equivalence class according to synthesis observation equivalnce. This
     * method is used when the event is not controllable and unobservable (DCDS 2011).
     *@param eqClass the class to be split.
     *@param e the event that should be considered.
     */
     private EquivalenceClassHolder splitSynthesisA(final StateSet eqClass,final LabeledEvent e)
    {
//        logger.debug("eqClass "+eqClass);
//        logger.debug("event "+e);

        final EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();
        final Iterator<State> stateIt = eqClass.iterator();

        while (stateIt.hasNext())
        {
            if (abortRequested)
            {
                newEquivalenceClassHolder.clear();
                return newEquivalenceClassHolder;
            }

            final State currState = stateIt.next();
            StateSet nextStates1;
//            logger.debug("currstate "+currState);
            if (e.isControllable())
            {
                if(e.isObservable()){
                    nextStates1 = currState.nextStates(e, false, true);
                }
                else{
                    nextStates1 = currState.epsilonClosure(true,true,true,false);
                }

            }


            //if the event is uncontrollable then the local events should be uncontrollable.
            else{
                if (e.isUnobservable())
                {

                // Find the states that can be reached by e from the epsilonclosure of currState
                    nextStates1 = currState.epsilonClosureWithNoCon(true);
                }

                else{
                    nextStates1 = currState.nextStatesWithUncoClosure(e, true);
                }

            }
//            logger.debug("nextState1"+ nextStates1);
            final EquivalenceClass nextClass = new EquivalenceClass();

            for (final Iterator<State> nextIt = nextStates1.iterator(); nextIt.hasNext(); )
                {
                    final State nextState = nextIt.next();
//                    if (e.isUnobservable()&&!nextState.equalState(currState)){
//                        nextClass.addAll(curreq);}
                    final EquivalenceClass thisNextClass = (EquivalenceClass) nextState.getStateSet();
                     nextClass.addAll(thisNextClass);
                }
//            logger.debug("nextClass "+nextClass);

            newEquivalenceClassHolder.addState(currState, nextClass);

        }
//        logger.debug("newEquivalenceClassHolder "+newEquivalenceClassHolder);
        return newEquivalenceClassHolder;
    }



     //##########################################################################




    /**
     * Merges all "single-outgoing-epsilon-transition-states".
     *
     * @return Number of states that have been removed by merging or
     * -1 if method didn't complete successfully.
     */
    public int mergeTriviallyObservationEquivalentStatesO(final Automaton aut)
    {
        final StateSet statesToExamine = new StateSet(aut.getStateSet());

        // Count the removed states
        int count = 0;

        // Do the merging
        loop: while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            // Get and remove arbitrary state
            final State one = statesToExamine.remove();

            // If there is only one transition out of this state AND it is an epsilon-transition
            // merge this and next state. (This is actually a special case of rule B.)
            Iterator<Arc> arcIt = one.outgoingArcsIterator();
            if (arcIt.hasNext())
            {
                // So this arc may be OK, just check that there are no more arcs in the iterator!
                final Arc arc = arcIt.next();
                final State two = arc.getToState();
                final Alphabet alphabetTwo = two.activeEvents(true);
                final Alphabet unconTwo= alphabetTwo.getUncontrollableAlphabet();
                final boolean arcOK = (!arcIt.hasNext() && arc.getEvent().isUnobservable() &&  arc.getEvent().isControllable() && unconTwo.size()==0 && !arc.isSelfLoop())||
                        (!arcIt.hasNext() && arc.getEvent().isUnobservable() &&  !arc.getEvent().isControllable()&& !arc.isSelfLoop());
                final boolean markingOK = options.getIgnoreMarking() || (one.hasEqualMarking(two));
                arcIt = null;
                if (arcOK && markingOK)
                {
                    // We can remove this arc, it will become an epsilon self-loop!
                    aut.removeArc(arc);

                    // Merge!

                    count++;

                    statesToExamine.remove(two);
                    final State state = MinimizationHelper.mergeStates(aut, one, two, useShortNames);
                    statesToExamine.add(state);
                    // Add states that may have changed to stack
                    for (final Iterator<Arc> it = state.incomingArcsIterator(); it.hasNext(); )
                    {
                        final Arc epsilonArc = it.next();
                        if (epsilonArc.getEvent().isUnobservable())
                        {
                            statesToExamine.add(epsilonArc.getFromState());
                        }
                    }
                    //logger.debug("I merged " + one + " and " + two + " since I thought they were OE.");
                    continue loop; // Get next "one" from stack
                }
            }
        }
        //logger.debug("trivially:"+count);
        return count;
    }
//################################################################################################





    public int mergeTriviallyObservationEquivalentStates(final Automaton aut)
    {
        final StateSet statesToExamine = new StateSet(aut.getStateSet());

        // Count the removed states
        int count = 0;

        // Do the merging
        loop: while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            // Get and remove arbitrary state
            final State one = statesToExamine.remove();

            // If there is only one transition out of this state AND it is an epsilon-transition
            // merge this and next state. (This is actually a special case of rule B.)
            Iterator<Arc> arcIt = one.outgoingArcsIterator();
            if (arcIt.hasNext())
            {
                // So this arc may be OK, just check that there are no more arcs in the iterator!
                final Arc arc = arcIt.next();
                final State two = arc.getToState();
                final boolean arcOK = !arcIt.hasNext() && arc.getEvent().isUnobservable() && !arc.isSelfLoop();
                final boolean markingOK = options.getIgnoreMarking() || (one.hasEqualMarking(two));
                arcIt = null;
                if (arcOK && markingOK)
                {
                    // We can remove this arc, it will become an epsilon self-loop!
                    aut.removeArc(arc);

                    // Merge!
                    count++;
                    statesToExamine.remove(two);

                    final State state = MinimizationHelper.mergeStates(aut, one, two, useShortNames);

                    statesToExamine.add(state);
                    // Add states that may have changed to stack
                    for (final Iterator<Arc> it = state.incomingArcsIterator(); it.hasNext(); )
                    {
                        final Arc epsilonArc = it.next();
                        if (epsilonArc.getEvent().isUnobservable())
                        {
                            statesToExamine.add(epsilonArc.getFromState());
                        }
                    }
                    //logger.debug("I merged " + one + " and " + two + " since I thought they were OE.");
                    continue loop; // Get next "one" from stack
                }
            }
        }

        return count;
    }





    /**
     * Merges all "single-outgoing-epsilon-transition-states".
     */
    private void mergeTriviallyObservationEquivalentStatesSynthesis(final Automaton aut)
    {
      final StateSet statesToExamine = new StateSet(aut.getStateSet());
      loop: while (statesToExamine.size() != 0)
        {

            if (abortRequested)
            {
                return ;
            }

            // Get and remove arbitrary state
            final State one = statesToExamine.remove();


            // If there is only one transition out of this state AND it is an epsilon-transition
            // merge this and next state. (This is actually a special case of rule B.)
            Iterator<Arc> arcIt = one.outgoingArcsIterator();
            if (arcIt.hasNext())
            {
                // So this arc may be OK, just check that there are no more arcs in the iterator!
                final Arc arc = arcIt.next();
                final State two = arc.getToState();
//                boolean ok1= arc.getEvent().isUnobservable();
//                boolean ok2= !arcIt.hasNext();
                final boolean arcOK = (!arcIt.hasNext() && arc.getEvent().isUnobservable()&& !arc.getEvent().isControllable() &&  !arc.isSelfLoop())||
                        (!arcIt.hasNext() && arc.getEvent().isUnobservable()&& arc.getEvent().isControllable() &&  !arc.isSelfLoop()&&!containsUncontrollble(two));
                final boolean markingOK = options.getIgnoreMarking() || (one.isAccepting(true) && two.isAccepting(true));
                arcIt = null;
//                logger.debug("the event"+arc.getEvent());
//                logger.debug("one"+one);
//                logger.debug("two"+two);
//                logger.debug("markOk"+markingOK);
//                logger.debug("arcOk1" + one.isAccepting(true));
//                logger.debug((two.isAccepting()));


                if (arcOK && markingOK)
                {
//                    EquivalenceClass st = new EquivalenceClass();
//                    st.add(one);

//                    st.add(two);


//                    // We can remove this arc, it will become an epsilon self-loop!
//                    aut.removeArc(arc);
//
//                    // Merge!
//
//                    count++;
//
                    statesToExamine.remove(two);

                    final State state = MinimizationHelper.mergeSynthesisStates(aut, one, two, useShortNames);

//                    logger.debug(one);
//                    logger.debug(two);
                    statesToExamine.add(state);
                    // Add states that may have changed to stack
                    for (final Iterator<Arc> it = state.incomingArcsIterator(); it.hasNext(); )
                    {
                        final Arc epsilonArc = it.next();
                        if (epsilonArc.getEvent().isUnobservable())
                        {
                            statesToExamine.add(epsilonArc.getFromState());
                        }
                    }
                    //logger.debug("I merged " + one + " and " + two + " since I thought they were OE.");
                    continue loop; // Get next "one" from stack
                }
            }
        }


    }
  public boolean containsUncontrollble(final State state){
      boolean contain=false;

      for(final Iterator<Arc> it= state.outgoingArcsIterator(); it.hasNext();){
          final Arc arc=it.next();
          if(!arc.getEvent().isControllable()){
              contain=true;
              break;
          }
      }
      return contain;
  }
    /**
     * Merges all epsilon loops in automaton. Also merges states with different marking!
     *
     * @return the number of states that have been removed or -1 if method didn't complete successfully.
     */
    public int mergeEpsilonLoops(final Automaton aut)
    {
        final StateSet statesToExamine = new StateSet(aut.getStateSet());


        // Count the removed states
        int count = 0;

        // Do the merging
        while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            // Get and remove arbitrary state
            State one = statesToExamine.remove();
            // Merge loops in which this state is involved

            // Find forwards and backwards closures
            final StateSet forwardsClosure = one.epsilonClosure(false);
            final StateSet backwardsClosure = one.backwardsEpsilonClosure();

            // Merge all states in the intersection!
            forwardsClosure.remove(one); // Skip self
            while (forwardsClosure.size() != 0)
            {
                final State two = forwardsClosure.remove();

                if (backwardsClosure.contains(two))
                {

                    // Merge!
                    count++;
                    statesToExamine.remove(two);
                    one = MinimizationHelper.mergeStates(aut, one, two, useShortNames);
                }
            }
        }

        return count;
    }

     public int mergeEpsilonLoopsSynthesis(final Automaton aut)
    {
        final StateSet statesToExamine = new StateSet(aut.getStateSet());

        // Count the removed states
        int count = 0;

        // Do the merging
        while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            // Get and remove arbitrary state
            State one = statesToExamine.remove();

            // Merge loops in which this state is involved

            // Find forwards and backwards closures
            final StateSet forwardsClosure = one.epsilonClosureWithActiveEventCheck(false);
            final StateSet backwardsClosure = one.backwardsEpsilonClosureActiveEventCheck();

            // Merge all states in the intersection!
            forwardsClosure.remove(one); // Skip self
            while (forwardsClosure.size() != 0)
            {
                final State two = forwardsClosure.remove();

                if (backwardsClosure.contains(two))
                {
                    // Merge!
                    count++;
                    statesToExamine.remove(two);
                    one = MinimizationHelper.mergeSynthesisStates(aut, one, two, useShortNames);
                }
            }
        }

        return count;
    }


    private static boolean hasEpsilonLoops(final Automaton aut)
    {
        final StateSet statesToExamine = new StateSet(aut.getStateSet());

        while (statesToExamine.size() != 0)
        {
            // Get and remove arbitrary state
            final State one = statesToExamine.remove();

            // Find forwards and backwards closures
            final StateSet forwardsClosure = one.epsilonClosure(false);
            final StateSet backwardsClosure = one.backwardsEpsilonClosure();

            // If some state is in both, then there are loops!
            forwardsClosure.remove(one); // Skip self
            while (forwardsClosure.size() != 0)
            {
                final State two = forwardsClosure.remove();
                if (backwardsClosure.contains(two))
                {
                    System.out.println("Loopstates:" + " " + one + ", " + two);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * The rules.
     */
    enum Rule
    { SC, AE, OSI, OSO, CC };

    /**
     * Runs some rules for minimization wrt conflict equivalence.
     *
     * @return total number of states removed.
     */
    public int runConflictEquivalenceRules(final Automaton aut)
    {
        // Remove redundant transitions
        int countOET = removeRedundantTransitions(aut);
        int countSC = 0;
        int countOSI = 0;
        int countAE = 0;
        int countCC = 0;
        int countOSO = 0;

        //final Rule[] order = { Rule.SC, Rule.AE, Rule.OSI, Rule.CC, Rule.OSO}; // "Original" order
        //final Rule[] order = { Rule.AE, Rule.SC, Rule.OSO, Rule.OSI, Rule.CC}; // Good order
        //final Rule[] order = { Rule.AE, Rule.SC, Rule.CC, Rule.OSO, Rule.OSI, };
        //final Rule[] order = { Rule.CC, Rule.AE, Rule.SC, Rule.OSO, Rule.OSI};
        final Rule[] order = { Rule.AE, Rule.SC, Rule.CC, Rule.OSO, Rule.AE, Rule.SC, Rule.OSI, }; // Works for arbiter
        //final Rule[] order = { Rule.AE, Rule.SC, Rule.OSO, Rule.OSI, Rule.CC, Rule.AE, Rule.SC, };

        try
        {
            boolean rerunNeeded;
            do
            {
                rerunNeeded = false;

                // Run the rules in the specified order...
                for (final Rule rule : order)
                {
                    int count;
                    boolean change = false;
                    do
                    {
                        // This must never happen as it is assumed by many of the rules
                        assert(!hasEpsilonLoops(aut));

                        count = 0;
                        switch (rule)
                        {
                            case OSO:
                                // Rule F (only silent outgoing)
                                if (options.getUseRuleOSO())
                                {
                                    count = ruleOnlySilentOut(aut);
                                    countOSO += count;
                                }
                            case OSI:
                                // Rule AA (only silent incoming)
                                if (options.getUseRuleOSI())
                                {
                                    count = ruleOnlySilentIn(aut);
                                    countOSI += count;
                                }
                            case SC:
                                // Rule A (silent continuation)
                                if (options.getUseRuleSC())
                                {
                                    count = silentContinuationRule(aut);
                                    countSC += count;
                                }
                            case AE:
                                // Rule B (active events)
                                if (options.getUseRuleAE())
                                {
                                    count = activeEventsRule(aut);
                                    countAE += count;
                                }
                            case CC:
                                // Rule C (certain conflicts)
                                if (true)
                                {
                                    count = certainConflictsRule(aut);
                                    countCC += count;
                                }
                        }

                        // Did anything happen
                        if (abortRequested)
                            return -1;
                        if (count < 0)
                            logger.error("Error when running rule: " + rule + " count: " + count);
                        if (count > 0)
                            change = true;

                        //System.out.println("Rule: " + rule + " count: " + count);
                    //} while(false);
                    //} while((rule == Rule.OSO || rule == Rule.OSI || rule == Rule.AE) && count > 0);
                    } while((rule == Rule.SC || rule == Rule.AE) && count > 0);

                    // Has anything happened to the number of states?
                    if (change)
                    {
                        // This may have opened up the possibility to apply the rules again!
                        rerunNeeded = true;
                        // Also remove redundant transitions
                        countOET += removeRedundantTransitions(aut);
                    }
                }
            } while (rerunNeeded);
        }
        catch (final Exception excp)
        {
            logger.error(excp);
            logger.debug(excp.getStackTrace());
            return -1;
        }

        // Log statistics
        logger.debug("Rule SC: " + countSC + ", rule AE: " + countAE +
            ", rule CC: " + countCC + ", rule OSI: " + countOSI +
            ", rule OSO: " + countOSO + ", count OET: " + countOET);

        // Add to totals
        totalSC += countSC;
        totalAE += countAE;
        totalOSI += countOSI;
        totalCC += countCC;
        totalOSO += countOSO;
        totalOET += countOET;

        // Return state nbr change
        return countSC+countOSI+countAE+countCC+countOSO;
    }

    /**
     * Rule A. NOTE! There must be no epsilon-loops in <code>aut</code>.
     *
     * @return the number of states that have been removed or -1 if method didn't complete successfully.
     */
    public int silentContinuationRule(final Automaton aut)
    throws Exception
    {
        ////////////
        // RULE A //
        ////////////

        // Count the removed states
        int count = 0;

        // If two states has the same incoming arcs (from the same state(s) and with the same
        // event(s)) AND if there is an epsilon transition present in both states, the states
        // can be merged. NOTE! THIS PRESUPPOSES THAT THERE ARE NO EPSILON-LOOPS IN THE AUTOMATON!
        final Hashtable<Integer,List<StateInfo>> infoHash = new Hashtable<Integer,List<StateInfo>>(aut.nbrOfStates()*4/3);
        for (final State state : aut)
        {
            // If this state doesn't have an outgoing epsilon-transition, it's not gonna be equivalent
            // to anything else...
            boolean check = false;
            for (final Iterator<Arc> arcIt = state.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                final Arc arc = arcIt.next();
                if (arc.getEvent().isUnobservable())
                {
                    assert(!arc.isSelfLoop());
                    check = true;
                    break;
                }
            }
            if (!check)
            {
                continue;
            }

            // Find relevant info about the state
            final StateInfo info = new StateInfoIncoming(state);

            // Get list of states with same hashcode, add this info
            List<StateInfo> list = infoHash.get(new Integer(info.hashCode()));
            if (list == null)
            {
                list = new LinkedList<StateInfo>();
                infoHash.put(new Integer(info.hashCode()), list);
            }
            list.add(info);
        }

        // Now all equivalent states should be in the lists, merge!
        count += mergeEquivalent(aut, infoHash.values());

        infoHash.clear();

        return count;
    }

    /**
     *  Look through all lists, merge all states with equivalent StateInfo.
     */
    private int mergeEquivalent(final Automaton aut, final Collection<List<StateInfo>> lists)
    {

        int count = 0;
        final List<StateInfo> toBeRemoved = new LinkedList<StateInfo>();
        for (final List<StateInfo> list : lists)
        {
            while (list.size() != 0)
            {
                final StateInfo one = list.remove(0);
                toBeRemoved.clear();
                for (final StateInfo two : list)
                {
                    if (one.equivalentTo(two))
                    {
                        final State merge = MinimizationHelper.mergeStates(aut, one.getState(), two.getState(), useShortNames);
                        one.setState(merge);
                        //logger.debug("Merging " + one.getState() + " and " + two.getState() + " into " + merge + ".");
                        toBeRemoved.add(two);
                        count++;
                    }
                }
                while (toBeRemoved.size() > 0)
                {
                    list.remove(toBeRemoved.remove(0));
                }
            }
        }
        return count;
    }

    /*
    public int silentContinuationRule(Automaton aut)
    throws Exception
    {
        //assert(!hasEpsilonSelfloops(aut));

        logger.fatal("NY");

        ////////////
        // RULE A //
        ////////////

        // Count the removed states
        int count = 0;

        // If two states has the same incoming arcs (from the same state(s) and with the same
        // event(s)) AND if there is an epsilon transition present in both states, the states
        // can be merged. NOTE! THIS PRESUPPOSES THAT THERE ARE NO EPSILON-LOOPS IN THE AUTOMATON!
        Hashtable infoHash = new Hashtable((aut.nbrOfStates()*4)/3+1);
        StateSet statesToExamine = new StateSet(aut.getStateSet());
        loop: while (statesToExamine.size() != 0)
        {
            State state = statesToExamine.remove();

            // If this state doesn't have an outgoing epsilon-transition, it's not gonna be equivalent
            // to anything else...
            boolean check = false;
            for (Iterator<Arc> arcIt = state.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                Arc arc = arcIt.next();
                if (arc.getEvent().isUnobservable())
                {
                    assert(!arc.isSelfLoop());
                    check = true;
                    break;
                }
            }
            if (!check)
            {
                continue loop;
            }

            // Find relevant info about the state
            StateInfoIncoming info = new StateInfoIncoming(state);
            logger.error("Stateinfo " + info);

            // Get list of states with same hashcode
            LinkedList list = (LinkedList) infoHash.get(new Integer(info.hashCode()));
            if (list == null)
            {
                list = new LinkedList();
                infoHash.put(new Integer(info.hashCode()), list);
                list.add(info);
                continue loop;
            }
            // Look through the list for equivalent entries (there may be nonequivalent entries
            // id they happen to have the same hashcode!
            for (Iterator it = list.iterator(); it.hasNext(); )
            {
                StateInfoIncoming old = (StateInfoIncoming) it.next();
                if (info.equivalentTo(old))
                {
                    // Merge states!
                    State merge = MinimizationHelper.mergeStates(aut, old.getState(), info.getState(), useShortNames);
                    logger.debug("Merging " + old.getState() + " and " + info.getState() + " info " + merge + ".");
                    count++;
                    // Use the old entry in the list but with the new state!
                    old.setState(merge);
                    // There can be no more of this characterization in the list
                    // Get a new state!
                    continue loop;
                }
                else
                {
                    logger.fatal("The state " + info.getState() + " is not equivalent to " + old.getState());
                }
            }
            // No equivalent entries in list. Then add this one to the list!
            list.add(info);
        }
        infoHash.clear();

        return count;
    }
     */

    /**
     * Rule AA, only silent incoming and not stable (observation equivalence "backwards" and then silentContinuation).
     *
     * @return the number of states that have been removed or -1 if method didn't complete successfully.
     */
    public int ruleOnlySilentIn(final Automaton aut)
    throws Exception
    {
        /////////////
        // RULE AA //
        /////////////

        // Count the removed states
        int count = 0;

        // If there is at least one outgoing epsilon transition AND this is not the initial
        // state AND all incoming transitions are epsilon!
        // Copy the outgoing of this state to all the previous states, and remove this state!
        final StateSet statesToExamine = new StateSet(aut.getStateSet());
        loop: while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            // Get and remove arbitrary state
            final State one = statesToExamine.remove();

            // Don't fiddle with the initial state!
            if (one.isInitial())
            {
                continue loop;
            }

            // Examine this state
            // Is there at least one epsilon outgoing?
            boolean ok = false;
            final List<Arc> toBeRemoved = new LinkedList<Arc>();
            for (final Iterator<Arc> outIt = one.outgoingArcsIterator(); outIt.hasNext(); )
            {
                final Arc arc = outIt.next();
                if (arc.getEvent().isUnobservable())
                {
                    if (!arc.isSelfLoop())
                    {
                        ok = true;
                        //break;
                    }
                    else
                    {
                        toBeRemoved.add(arc);
                    }
                }
            }
            while (toBeRemoved.size() > 0)
            {

                final Arc arc = toBeRemoved.remove(0);
                logger.fatal("Remove arc: " + arc);
                //ActionMan.getGui().addAutomaton(aut);
                //Thread.sleep(500);
                aut.removeArc(arc);
            }

            // Are all incoming epsilon?
            if (ok && (one.nbrOfIncomingArcs() == one.nbrOfIncomingUnobservableArcs()))
            {
                // "Copy" outgoing arcs from one to the previous states.
                for (final Iterator<Arc> inIt = one.incomingArcsIterator(); inIt.hasNext(); )
                {
                    final Arc inArc = inIt.next(); // ConcurrentModificationError!?
                    final State fromState = inArc.getFromState();

                    // Should be epsilon!
                    assert(inArc.getEvent().isUnobservable());
                    // Should not be a selfloop
                    assert(!inArc.isSelfLoop());

                    for (final Iterator<Arc> outIt = one.outgoingArcsIterator(); outIt.hasNext(); )
                    {
                        final Arc outArc = outIt.next();
                        final State toState = outArc.getToState();
                        final LabeledEvent event = outArc.getEvent();

                        final Arc newArc = new Arc(fromState, toState, event);
                        aut.addArc(newArc);
                    }
                }

                aut.removeState(one);
                count++;
            }
        }

        return count;
    }

    /**
     * Rule B.
     *
     * @return the number of states that have been removed or -1 if method didn't complete successfully.
     */
    public int activeEventsRule(final Automaton aut)
    throws Exception
    {
        ////////////
        // RULE B //
        ////////////

        // Count the removed states
        int count = 0;

        // If two states have the same incoming arcs (from the same state(s) and with the same
        // event(s)) AND if both states have the same events active, then they can be merged!
        final Hashtable<Integer, List<StateInfo>> infoHash = new Hashtable<Integer, List<StateInfo>>((aut.nbrOfStates()*4)/3+1);
        for (final State state : aut)
        {
            // Find relevant info about the state
            final StateInfo info = new StateInfoActiveEventsRule(state);

            // Get list of states with same hashcode
            LinkedList<StateInfo> list = (LinkedList<StateInfo>) infoHash.get(new Integer(info.hashCode()));
            if (list == null)
            {
                list = new LinkedList<StateInfo>();
                infoHash.put(new Integer(info.hashCode()), list);
            }
            list.add(info);
        }

        // Now all equivalent states should be in the lists, merge!
        count += mergeEquivalent(aut, infoHash.values());

        infoHash.clear();

        return count;
    }
    /*
    public int activeEventsRule(Automaton aut)
    throws Exception
    {
        //assert(!hasEpsilonSelfloops(aut));

        ////////////
        // RULE B //
        ////////////

        // Count the removed states
        int count = 0;

        // If two states have the same incoming arcs (from the same state(s) and with the same
        // event(s)) AND if both states have the same events active, then they can be merged!
        Hashtable infoHash = new Hashtable((aut.nbrOfStates()*4)/3+1);
        StateSet statesToExamine = new StateSet(aut.getStateSet());
        loop: while (statesToExamine.size() != 0)
        {
            State state = statesToExamine.remove();

            // Find relevant info about the state
            StateInfoActiveEventsRule info = new StateInfoActiveEventsRule(state);

            // Get list of states with same hashcode
            LinkedList list = (LinkedList) infoHash.get(new Integer(info.hashCode()));
            if (list == null)
            {
                list = new LinkedList();
                infoHash.put(new Integer(info.hashCode()), list);
                list.add(info);
                continue loop;
            }
            // Look through the list for equivalent entries (there may be nonequivalent entries
            // id they happen to have the same hashcode!
            for (Iterator it = list.iterator(); it.hasNext(); )
            {
                StateInfoActiveEventsRule old = (StateInfoActiveEventsRule) it.next();
                if (info.equivalentTo(old))
                {
                    // Merge states!
                    //logger.debug("Merging " + old.getState() + " and " + info.getState() + ".");
                    State merge = MinimizationHelper.mergeStates(aut, old.getState(), info.getState(), useShortNames);
                    count++;
                    // Use the old entry in the list but with the new state!
                    old.setState(merge);
                    // There can be no more of this characterization in the list
                    // Get a new state!
                    continue loop;
                }
            }
            // No equivalent entries in list. Then add this one to the list!
            list.add(info);
        }
        infoHash.clear();

        return count;
    }
     */

    /**
     * Certain conflicts rule.
     *
     * @return the number of states that have been removed or -1 if method didn't complete successfully.
     */
    public int certainConflictsRule(final Automaton aut)
    throws Exception
    {
        //assert(!hasEpsilonSelfloops(aut));

        ////////////////////////////
        // RULE C (not all cases) //
        ////////////////////////////

        // Count the removed states
        int count = 0;

        // Mark noncoreachable states with State.MAX_COST
        final SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions();
        final AutomatonSynthesizer synth = new AutomatonSynthesizer(aut, synthOptions);
        synth.doCoreachable();

        // Remove all outgoing arcs from these states
        for (final Iterator<?> it = aut.stateIterator(); it.hasNext(); )
        {
            if (abortRequested)
            {
                return -1;
            }

            final State state = (State) it.next();
            if ((state.getCost() == State.MAX_COST))
            {
                // This state can not be accepting if the coreachability worked!
                assert(!state.isAccepting());

                // We will never want to propagate from here...
                // ... or from any state in the backwards epsilon closure!!
                final StateSet statesToModify = new StateSet();
                statesToModify.add(state);
                while (statesToModify.size() != 0)
                {
                    final State currState = statesToModify.remove();

                    // Accepting states that by epsilons may reach a block are modified to be
                    // nonaccepting...
                    // We might as well exchange the below if-clause with
                    // only one statement, "currState.setAccepting(false);".
                    if (currState.isAccepting())
                    {
                        if (currState.isInitial())
                        {
                            // We know that the system is blocking!
                            logger.debug("The system was found to be blocking.");

                            // Do nothing...
                            //continue;
                        }

                        currState.setAccepting(false);
                    }

                    // We won't want to propagate from here, this is now a blocking state!
                    currState.removeOutgoingArcs();

                    ////////////////
                    // RULE C.1-3 //
                    ////////////////

                    // Follow all transitions backwards
                    for (final Iterator<Arc> arcIt = currState.incomingArcsIterator(); arcIt.hasNext(); )
                    {
                        final Arc arc = arcIt.next();
                        final State previous = arc.getFromState();

                        if (arc.getEvent().isUnobservable())
                        {
                            if (previous.getCost() != State.MAX_COST)
                            {
                                //previous.setCost(State.MAX_COST); // Dangerous, make sure you reset!
                                statesToModify.add(previous);
                                logger.debug("Rule C.3 came to use. State " + previous + ".");
                            }
                        }
                        else
                        {
                            // Loop over the outgoing arcs of the previous state, remove
                            // arcs labeled by the same event if they lead somewhere other
                            // than to currState
                            final Iterator<Arc> outIt = previous.outgoingArcsIterator();
                            boolean fail = false;
                            final LinkedList<Arc> toBeRemoved = new LinkedList<Arc>();
                            while (outIt.hasNext())
                            {
                                final Arc currArc = outIt.next();

                                if (!currArc.getToState().equals(currState))
                                {
                                    if (currArc.getEvent().equals(arc.getEvent()))
                                    {
                                        // The arc "currArc" can be removed
                                        toBeRemoved.add(currArc);
                                        logger.debug("Rule C.1 came to use. Arc " + currArc + ".");
                                    }
                                    else
                                    {
                                        fail = true;
                                    }
                                }
                            }
                            while (toBeRemoved.size() != 0)
                            {
                                aut.removeArc(toBeRemoved.remove(0));
                            }

                            // If this is true, then there are NO outgoing at all from
                            // "previous" then it is blocking unless it is marked itself!
                            if (!fail)
                            {
                                // This may appear as a result of rule C.3 above!
                                if (!previous.isAccepting() && previous.getCost() != State.MAX_COST)
                                {
                                    //previous.setCost(State.MAX_COST); // Dangerous, make sure you reset!
                                    statesToModify.add(previous);
                                    logger.debug("Rule C.2 came to use. State " + previous + ".");
                                }
                            }
                        }
                    }
                }

                // Reset cost...
                if (!state.isForbidden())
                {
                    state.setCost(State.UNDEF_COST);
                }
            }
        }
        // After the above there may be nonreachable parts... make reachable!
        synth.doReachable();
        final LinkedList<State> toBeRemoved = new LinkedList<State>();
        for (final Iterator<State> it = aut.stateIterator(); it.hasNext(); )
        {
            final State state = it.next();
            if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
            {
                toBeRemoved.add(state);
            }
        }
        while (toBeRemoved.size() != 0)
        {
            final State remove = toBeRemoved.remove(0);
            logger.debug("Nonreachable state: " + remove);

            aut.removeState(remove);
            count++;
        }

        return count;
    }


    /**
     * Rule D.
     *
     * All states that can reach marked states by epsilon events are also considered marked.
     *
     * @return the number of states that have been marked or -1 if the method didn't complete successfully.
     */
    //public int adjustMarking(Automaton aut)
    public int rulePropagateMarking(final Automaton aut)
    {
        ////////////
        // RULE D //
        ////////////

        // Count the states that get marked
        int count = 0;

        if (aut == null || abortRequested)
        {
            return -1;
        }

        /*
        // States that can reach marked states by epsilon events only can be considered marked
        LinkedList toBeMarked = new LinkedList();
        for (Iterator<State> stateIt = aut.suateIterator(); stateIt.hasNext();)
        {
            State currState = stateIt.next();
            if (!currState.isAccepting())
            {
                StateSet closure = currState.epsilonClosure();
                for (Iterator<State> closureIt = closure.iterator(); closureIt.hasNext(); )
                {
                    State otherState = closureIt.next();
                    if (otherState.isAccepting())
                    {
                        toBeMarked.add(currState);
                        break;
                    }
                }
            }
        }
         */

        // States that can reach marked states by epsilon events only can be considered marked
        // First find all the marked states, then adjust the states in the backwardsEpsilonclosures
        final StateSet markedStates = new StateSet();
        for (final Iterator<State> stateIt = aut.stateIterator(); stateIt.hasNext();)
        {
            final State currState = stateIt.next();
            if (currState.isAccepting())
            {
                markedStates.add(currState);
            }
        }

        // Adjust the marking!
        while (markedStates.size() != 0)
        {
            final StateSet closure = markedStates.remove().backwardsEpsilonClosure();

            for (final Iterator<State> closureIt = closure.iterator(); closureIt.hasNext(); )
            {
                final State state = closureIt.next();
                if (!state.isAccepting())
                {
                    state.setAccepting(true);
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Rule F, only silent outgoing (observation equivalence backwards and then active events rule?).
     *
     * @return the number of states that have been removed or -1 if the method didn't complete successfully.
     */
    public int ruleOnlySilentOut(final Automaton aut)
    throws Exception
    {
        ////////////
        // RULE F //
        ////////////

        final StateSet statesToExamine = new StateSet(aut.getStateSet());

        // Count the removed states
        int count = 0;

        // States that only have epsilon events as outgoing can be bypassed!
        loop: while (statesToExamine.size() != 0)
        {
            if (abortRequested)
            {
                return -1;
            }

            final State state = statesToExamine.remove();

            // Don't remove the initial state!
            if (state.isInitial())
            {
                continue loop;
            }

            // If this state is marked, we need to make sure that at least one of the
            // following states is marked!
            final boolean isMarked = state.isAccepting();
            boolean hasMarkedDecessor = false;

            // All outgoing (at least one!) must be epsilon (and maybe have a marked toState)
            for (final Iterator<Arc> outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
            {
                final Arc arc = outIt.next();
                if (!arc.getEvent().isUnobservable())
                {
                    continue loop;
                }
                assert(!arc.isSelfLoop());
                final State toState = arc.getToState();
                hasMarkedDecessor |= toState.isAccepting();
            }

            // So, at least one outgoing and if "state" is marked, there must be a marked decessor
            if ((state.outgoingArcsIterator().hasNext()) && (!isMarked || hasMarkedDecessor))
            {
                for (final Iterator<Arc> outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
                {
                    final Arc outArc = outIt.next();

                    for (final Iterator<Arc> inIt = state.incomingArcsIterator(); inIt.hasNext(); )
                    {
                        final Arc inArc = inIt.next();
                        final State fromState = inArc.getFromState();
                        final State toState = outArc.getToState();
                        final Arc newArc = new Arc(fromState, toState, inArc.getEvent());
                        aut.addArc(newArc);
                    }

                    // BUG! There is a problem here, since when using the State.setName method, all
                    // StateSet:s that the state is involved in would need to be rebuilt since the
                    // hashCode of the state is changed... not gooooood...
                    //   On the other hand... maybe we shouldn't give the state a new name at all?
                    // After all, we don't merge states, we just remove a state?
                    /*
                    if (!useShortNames)
                    {
                        State toState = outArc.getToState();
                        toState.setName(state.getName() + Config.GENERAL_STATELABEL_SEPARATOR.get() +
                            toState.getName());
                        //logger.warn("AutomatonMinimizer: Name changed on state, problems with bad hashcodes may appear...");
                    }
                     */
                }

                aut.removeState(state);
                //logger.error("Removed state " + state + ", added " + arcCount + " arcs.");
                count++;
            }

            /*
            // All outgoing (at least one!) must be epsilon, i.e. there should be no events in the alphabet
            // of active events if we don't consider the epsilon closure...
            if ((state.outgoingArcsIterator().hasNext()) && (state.activeEvents(false).size() == 0))
            {
                for (Iterator<Arc> outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
                {
                    Arc outArc = outIt.next();

                    for (Iterator<Arc> inIt = state.incomingArcsIterator(); inIt.hasNext(); )
                    {
                        Arc inArc = inIt.next();
                        Arc newArc = new Arc(inArc.getFromState(), outArc.getToState(), inArc.getEvent());

                        aut.addArc(newArc);
                    }
                }

                aut.removeState(state);
                count++;
            }
             */
        }

        return count;
    }

    // Statistics for ConflictEquivalence
    private static int totalSC;
    private static int totalOSI;
    private static int totalAE;
    private static int totalCC;
    private static int totalD;
    private static int totalOSO;
    private static int totalOES;
    private static int totalOET;

    // Statistics for SupervisionEquivalence
    private static int totalHWS;
    private static int totalBSE;

    /**
     * Reset statistics info.
     */
    public static void resetStatistics()
    {
        totalSC = 0;
        totalOSI = 0;
        totalAE = 0;
        totalCC = 0;
        totalD = 0;
        totalOSO = 0;
        totalOES = 0;
        totalOET = 0;

        totalBSE = 0;
        totalHWS = 0;
    }

    /**
     * Loggs some relevant statistics for the minimisation.
     */
    public static void logStatistics(final MinimizationOptions options)
    {
        if (options.getMinimizationType() == EquivalenceRelation.CONFLICTEQUIVALENCE)
            logger.debug("Reduction statistics: activeEvent: " + totalAE + ", silentCont: " + totalSC + ", certainConf: " + totalCC + ", onlySilentIn: " + totalOSI + ", onlySilentOut: " + totalOSO + ", D: " + totalD + ", OES: " + totalOES + ", OET: " + totalOET + ".");
        else if (options.getMinimizationType() == EquivalenceRelation.SUPERVISIONEQUIVALENCE)
            logger.debug("Reduction statistics: halfway Synthesis: " + totalHWS + ", bisimulation equivalence " + totalBSE);
    }

    public static String getStatisticsLaTeX()
    {
        return(totalAE + " & " + totalSC + " & " + totalCC + " & " + totalOSI + " & " + totalOSO + " & " + totalOES + " & " + totalOET);

    }

    public static String getWodesStatisticsLaTeX()
    {
        return(totalAE + " & " + totalSC + " & " + totalCC + " & " + (totalOSI + totalOSO) + " & " + totalOES + " & " + totalOET);
    }

    /**
     * Add tau-transitions to cover for the epsilon events (aka "saturate"). More formally, each
     * time there is a transition "p =epsilon=> q", after completing the transitive closure (or
     * "saturation"), there is also a transition "p -epsilon-> q".
     */
    public static int epsilonSaturate(final Automaton aut, final boolean addSelfloops)
    throws SupremicaException
    {
        int count = 0;

        // Get/create silent event tau
        final String silentName = Config.MINIMIZATION_SILENT_EVENT_NAME.getAsString();
        LabeledEvent tau = aut.getAlphabet().getEvent(silentName);
        if (tau == null)
        {
            tau = new LabeledEvent(silentName);
            tau.setUnobservable(true);
            aut.getAlphabet().addEvent(tau);
        }
        else
        {
            if (!tau.isUnobservable())
            {
                throw new SupremicaException("Misuse of reserved event name 'tau'.");
            }
        }

        // For each state, find epsilon closure and add add transitions
        for (final Iterator<State> fromIt = aut.stateIterator(); fromIt.hasNext();)
        {
            final State from = fromIt.next();

            // Iterate over states in closure
            final StateSet closure = from.epsilonClosure(addSelfloops);
            for (final State to : closure)
            {
                final Arc arc = new Arc(from, to, tau);
                if (!arc.getFromState().containsOutgoingArc(arc))
                {
                    aut.addArc(arc);
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Each time there is a transition "p -tau_u^*sigma-> q", after
     * completing the saturation, there is also a transition "p
     * -sigma-> q".
     */

    public static int supervisionEquivalenceSaturate(final Automaton aut)
    throws SupremicaException
    {
        int count = 0;

        // Add silent event (if it's not already there)
        final String silentUName = Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString();
        LabeledEvent tau_u = aut.getAlphabet().getEvent(silentUName);
        if (tau_u == null)
        {
            tau_u = new LabeledEvent(silentUName);
            tau_u.setUnobservable(true);
            tau_u.setControllable(false);
            aut.getAlphabet().addEvent(tau_u);
        }
        else
        {
            if (!tau_u.isUnobservable() || tau_u.isControllable())
            {
                throw new SupremicaException("Misuse of reserved event name 'tau_u'.");
            }
        }

        // For each state, find tau_u-epsilon closure and add transitions
        for (final Iterator<State> fromIt = aut.stateIterator(); fromIt.hasNext();)
        {
            final State from = fromIt.next();

            // Add self-loop
            {
                final Arc arc = new Arc(from, from, tau_u);
                if (!from.containsOutgoingArc(arc))
                {
                    aut.addArc(arc);
                    count++;
                }
            }

            // Iterate over states in closure
            final StateSet closure = from.epsilonClosure(false, false, true);
            for (final Iterator<State> toIt = closure.iterator(); toIt.hasNext(); )
            {
                final State to = toIt.next();
                final LinkedList<Arc> toBeAdded = new LinkedList<Arc>();
                for (final Iterator<Arc> outIt = to.outgoingArcsIterator(); outIt.hasNext(); )
                {
                    final Arc oldArc = outIt.next();
                    final Arc arc = new Arc(from, oldArc.getToState(), oldArc.getEvent());
                    if (!from.containsOutgoingArc(arc))
                    {
                        toBeAdded.add(arc);
                    }
                }
                for (final Arc arc : toBeAdded)
                {
                    aut.addArc(arc);
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Add transitions to cover for the epsilon events (aka "saturate"). More formally, each
     * time there is a transition "p =a=> q", after completing the transitive closure (or
     * "saturation"), there is also a transition "p -a-> q".
     */
        /*
    public int saturate(Automaton aut)
    {
        if (aut == null)
        {
            return -1;
        }

        // Find epsilon-closure for each state, put this info in each state
        for (Iterator<State> stateIt = aut.stateIterator(); stateIt.hasNext();)
        {
            if (stopRequested)
            {
                aut = null;
                return -1;
            }

            State currState = stateIt.next();

            // Find closure, associate it with this state
            StateSet closure = currState.epsilonClosure(true);
            currState.setStateSet(closure);
        }

        // From each state add transitions that are present in its closure
        LinkedList toBeAdded = new LinkedList();
        for (Iterator<State> stateIt = aut.stateIterator(); stateIt.hasNext();)
        {
            if (stopRequested)
            {
                aut = null;
                return -1;
            }

            State currState = stateIt.next();
            StateSet closure = currState.getStateSet();

            // Iterate over outgoing arcs in the closure
            for (Iterator<State> closureIt = closure.iterator(); closureIt.hasNext(); )
            {
                arcLoop: for (Iterator<Arc> arcIt = closureIt.next().outgoingArcsIterator(); arcIt.hasNext(); )
                {
                    Arc arc = arcIt.next();

                    // We can safely ignore the closure, we'll get there, don't worry
                    if (arc.getEvent().isUnobservable())
                    {
                        // Don't add self-loops of epsilons (we will do that later in each state)
                        if (!currState.equals(arc.getToState()))
                        {
                            toBeAdded.add(new Arc(currState, arc.getToState(), arc.getEvent()));
                        }

                        // Ignore the below loop
                        continue arcLoop;
                    }

                    // Where may we end up if we move along this transition?
                    // Anywhere in the epsilon-closure of the toState...
                    StateSet toClosure = arc.getToState().getStateSet();
                    for (Iterator<State> toIt = toClosure.iterator(); toIt.hasNext(); )
                    {
                        State toState = toIt.next();

                        // Don't add already existing transitions
                        if (!(currState.equals(arc.getFromState()) && toState.equals(arc.getToState())))
                        {
                            toBeAdded.add(new Arc(currState, toState, arc.getEvent()));
                        }
                    }
                }
            }

            // Add silent self-loop
            LabeledEvent tau = new LabeledEvent(SupremicaProperties.getSilentEventName());
            tau.setUnobservable(true);
            if (!aut.getAlphabet().contains(tau))
            {
                aut.getAlphabet().addEvent(tau);
            }
            toBeAdded.add(new Arc(currState, currState, tau));
        }

        // Add the new arcs
        int amount = toBeAdded.size();
        int added = 0;
        //logger.debug("Added " + toBeAdded.size() + " transitions to " + aut + ".");
        while (added != amount)
        {
            // Add if not already there
            Arc arc = (Arc) toBeAdded.remove(0);
            if (!arc.getFromState().containsOutgoingArc(arc))
            {
                aut.addArc(arc);
            }

            added++;
        }

        if (stopRequested)
        {
            aut = null;
            return -1;
        }

        return amount;
    }
         */

    /**
     * True if there are epsilon selfloops in aut.
     */
    @SuppressWarnings("unused")
	private static boolean hasEpsilonSelfloops(final Automaton aut)
    {
        for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
        {
            final Arc arc = arcIt.next();
            if (arc.isSelfLoop() && arc.getEvent().isUnobservable())
            {
                logger.fatal("Epsilon selfloop: " + arc);
                ActionMan.getGui().addAutomaton(aut);
                return true;
            }
        }
        return false;
    }

    /**
     * Algorithm inspired by "Minimizing the Number of Transitions with Respect to Observation
     * Equivalence" by Jaana Eloranta. Removes all transitions that are redundant.
     *
     * @return the number of arcs that have been removed.
     */
    private static int removeRedundantTransitions(final Automaton aut)
    {

        // Are there any silent-self-loops? They can be removed!
        // Note! These are not "redundant" by Jaana Elorantas definition, but must be
        // removed before removing her "redundant transitions" (see below).

        // Count the removed arcs
        int count = 0;

        // Put silent self-loops in a list, remove afterwards
        final List<Arc> toBeRemoved = new LinkedList<Arc>();
        for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
        {
            final Arc currArc = arcIt.next();
            if (currArc.isSelfLoop() && currArc.getEvent().isUnobservable())
            {
                toBeRemoved.add(currArc);
            }
        }
        while (toBeRemoved.size() > 0)
        {
            aut.removeArc(toBeRemoved.remove(0));

            count++;
        }

        // Put redundant arcs in list, remove after all have been found
        toBeRemoved.clear();
        for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
        {
            final Arc arc = arcIt.next();
            final LabeledEvent event = arc.getEvent();

            // Using Elorantas notation... (s1, s2 and (later) s3)
            final State s1 = arc.getFromState();
            final State s2 = arc.getToState();

            // Is the criteria fulfilled? (I.e. does there exist a s3 such that either
            // (s1 -a-> s3 and s3 -tau-> s2) or (s1 -tau-> s3 and s3 -a-> s2) holds?)
            test: for (final Iterator<Arc> outIt = s1.outgoingArcsIterator(); outIt.hasNext(); )
            {
                final Arc firstArc = outIt.next();
                final LabeledEvent firstEvent = firstArc.getEvent();

                if (firstEvent.isUnobservable() || firstEvent.equals(event))
                {
                    final State s3 = firstArc.getToState();

                    for (final Iterator<Arc> inIt = s2.incomingArcsIterator(); inIt.hasNext(); )
                    {
                        final Arc secondArc = inIt.next();
                        if (s3.equals(secondArc.getFromState()))
                        {
                            final LabeledEvent secondEvent = secondArc.getEvent();
                            // The order (wrt ||) in this if-clause is important!!
                            if ((secondEvent.isUnobservable() &&
                                (!firstEvent.isUnobservable() || arc.getEvent().isUnobservable())) ||
                                (secondEvent.equals(arc.getEvent()) &&
                                !firstEvent.equals(secondEvent)))
                            {
                                // Redundant!!!!
                                toBeRemoved.add(arc);

                                break test;
                            }
                        }
                    }
                }
            }
        }
        /*
        // Put redundant arcs in list, remove after all have been found
        toBeRemoved.clear();
        int i=0;
        arcLoop: for (Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
        {
            logger.debug("" + ++i);
            Arc arc = arcIt.next();

            // Using Elorantas notation... (a, s1, s2 and (not exactly) s3)... but not her method...
            LabeledEvent a = arc.getEvent();
            State s1 = arc.getFromState();
            State s2 = arc.getToState();

            // So there does exist an arc (s1 -a-> s2)... now, check if there is ANOTHER way of
            // getting to s2. I.e. (s1 -a-> s2) can be removed if (s1 -tau*.a.tau*-> s2) with at
            // least one tau in either of the tau*-expressions. Remember that there at this point
            // should be no tau-loops in this system!

            // Yhe string tau*.a.tau* must begin with either a or tau.

            // First assume it starts with tau
            //StateSet s3set = s1.nextStates(tau, false);
            StateSet s3set = s1.epsilonClosure(false);
            assert(!s3set.contains(s1)); // There should be no loops!
            // Now if s2 can be reached from s3set using at least one a, the arc can be removed!
            StateSet set;
            if (a.isUnobservable())
            {
                set = s3set.epsilonClosure(false);
            }
            else
            {
                set = s3set.nextStates(a, true);
            }
            if (set.contains(s2))
            {
                toBeRemoved.add(arc);
                continue arcLoop;
            }
            // If a is epsilon, we will do the same thing all over again... so skip it!
            if (a.isUnobservable())
            {
                continue arcLoop;
            }
            // Now the same again, but assuming it starts with an a
            s3set = s1.nextStates(a, false);
            // Now if s2 can be reached using at least one tau, the arc can be removed!
            //if (s3set.nextStates(tau, true).contains(s2))
            if (s3set.epsilonClosure(false).contains(s2))
            {
                toBeRemoved.add(arc);
                continue arcLoop;
            }
        }
         */
        //logger.debug("redandunt arcs "+toBeRemoved);
        while (toBeRemoved.size() > 0)
        {
            aut.removeArc(toBeRemoved.remove(0));
            count++;
        }

        return count;
    }

    /**
     * Removes epsilon events that are never used from alphabet.
     */
    private static void removeUnusedEpsilonEvents(final Automaton aut)
    {
        final Alphabet alpha = aut.getAlphabet();

        // Put them in a list, remove afterwards
        final LinkedList<LabeledEvent> toBeRemoved = new LinkedList<LabeledEvent>();
        loop: for (final Iterator<LabeledEvent> evIt = alpha.iterator(); evIt.hasNext(); )
        {
            final LabeledEvent event = evIt.next();

            // Epsilon?
            if (event.isUnobservable())
            {
                // Is this event in use?
                for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
                {
                    // Is this transition associated with the right
                    // event? If so continue the outer loop.
                    if (event.equals(arcIt.next().getEvent()))
                    {
                        // Not unused!
                        continue loop;
                    }
                }
                toBeRemoved.add(event);
            }
        }
        while (toBeRemoved.size() > 0)
        {
            alpha.removeEvent(toBeRemoved.remove(0));
        }
    }

    /**
     * Performs "half-way" synthesis on aut.
     *
     * @return size of state space reduction.
     */
    public static int halfWaySynthesis(final Automaton aut)
    throws Exception
    {
        //logger.debug("Halfway-synthesis on " + aut);
        final int before = aut.nbrOfStates();

        // Loop
        boolean outerChange = true;
        while (outerChange)
        {
            outerChange = false;
            final SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions();
            final AutomatonSynthesizer synth = new AutomatonSynthesizer(aut, synthOptions);
            final StateSet blockingStates = synth.doCoreachable();
            //logger.debug("Blocking: " + blockingStates);
            // Remove outgoing arcs from blocking states
            for (final State state : blockingStates)
            {
                state.removeOutgoingArcs();
            }
            // Find all states that can reach blockingStates using tau_u events
            boolean innerChange = true;
            while (innerChange)
            {
                // Find new states
                final StateSet newStates = blockingStates.previousStates(Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString());
                // Remove outgoing arcs from newStates
                for (final State state : newStates)
                {
                    state.removeOutgoingArcs();
                }
                // Add new to old
                final int oldSize = blockingStates.size();
                blockingStates.addAll(newStates);
                final int newSize = blockingStates.size();
                innerChange = (oldSize < newSize);
            }
            //logger.debug("Blocking + tau_u-uncontrollable: " + blockingStates);

//            if (stopRequested)
//            {
//                throw new Exception("Stop requested during Halfway-synthesis.");
//            }

            // Remove all controllable incoming transitions
            for (final State state : blockingStates)
            {
                final List<Arc> toBeRemoved = new LinkedList<Arc>();
                for (final Iterator<Arc> arcIt = state.incomingArcsIterator(); arcIt.hasNext(); )
                {
                    final Arc arc = arcIt.next();
                    if (arc.getEvent().isControllable())
                    {
                        toBeRemoved.add(arc);
                    }
                }
                while (toBeRemoved.size() > 0)
                {
                    aut.removeArc(toBeRemoved.remove(0));
                }
            }
            // Merge all states in blockingStates
            if (blockingStates.size() > 1)
            {
                State blob = blockingStates.remove();
                while (blockingStates.size() > 0)
                {
                    final State state = blockingStates.remove();
                    blob = MinimizationHelper.mergeStates(aut, blob, state, false);
                }
                blob.setName("dump");
                blob.setAccepting(false);
                outerChange = true;

            }
        }

        // Reset costs
        for (final State state : aut)
        {
            state.setCost(0);
        }
        // Make accessible
        final SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions();
        final AutomatonSynthesizer synth = new AutomatonSynthesizer(aut, synthOptions);
        synth.doReachable(true);
        final LinkedList<State> toBeRemoved = new LinkedList<State>();
        for (final Iterator<State> it = aut.stateIterator(); it.hasNext(); )
        {
            final State state = it.next();
            if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
            {
                logger.debug("The state " + state + " will be removed since it is not reachable.");
                toBeRemoved.add(state);
            }

            // The forbidden states may actually be reachable?
            if (state.isForbidden())
            {
                //logger.fatal("The state " + state + " is forbidden.");
            }

            if(state.nbrOfIncomingArcs()<1&!toBeRemoved.contains(state)&&!state.isInitial()){

                     toBeRemoved.add(state);

                 }
        }

        while (toBeRemoved.size() != 0)
        {
            final State st = toBeRemoved.removeFirst();

            aut.removeState(st);

        }

        return before - aut.nbrOfStates();
    }





    /**
     * Sets the executionDialog of this AutomatonMinimizer. If executionDialog is null,
     * the dialog is not updated.
     */
    public void setExecutionDialog(final ExecutionDialog executionDialog)
    {
        this.executionDialog = executionDialog;
    }


    @Override
    public void requestAbort()
    {
      abortRequested = true;

      logger.debug("AutomatonMinimizer requested to stop.");
    }

    @Override
    public boolean isAborting()
    {
      return abortRequested;
    }

    @Override
    public void resetAbort()
    {
      abortRequested = false;
    }
}

interface StateInfo
{
    public State getState();
    public void setState(State state);
    public boolean equivalentTo(StateInfo info);
}

/**
 * Class for characterizing states based on their ~_incoming-properties, see
 * "Modular Nonblocking using Conflict Equivalence" by Flordal et al.
 */
class StateInfoIncoming
    implements StateInfo
{
    private State state;
    private final Arclets incomingArcs;
    private final boolean isInitial;

    public StateInfoIncoming(final State state)
    {
        this.state = state;
        final StateSet backwardsClosure = state.backwardsEpsilonClosure();
        incomingArcs = new Arclets(backwardsClosure.incomingArcsIterator());
        isInitial = backwardsClosure.hasInitialState();
    }

    @Override
    public void setState(final State state)
    {
        this.state = state;
    }

    @Override
    public State getState()
    {
        return state;
    }

    @Override
    public boolean equivalentTo(final StateInfo info)
    {
        assert(info instanceof StateInfoIncoming);
        final StateInfoIncoming other = (StateInfoIncoming) info;

        boolean result = true;
        result &= incomingArcs.equals(other.incomingArcs);
        result &= isInitial == other.isInitial;

        return result;
    }

    @Override
    public boolean equals(final Object object)
    {
        final StateInfoIncoming other = (StateInfoIncoming) object;

        return state.equals(other.state);
    }

    @Override
    public int hashCode()
    {
        int hash = incomingArcs.hashCode();
        hash = hash * 41 + (isInitial ? 1523 : 1531);

        return hash;
    }

    @Override
    public String toString()
    {
        String result = "";
        result += "State: " + state;
        result += "\n\tArclets: " + incomingArcs;
        result += "\n\tInitial: " + isInitial;
        return result;
    }

    /**
     * Class describing "half" arcs, i.e. only one state and the event.
     */
    class Arclet
        implements Comparable<Arclet>
    {
        private final State state;
        private final LabeledEvent event;

        public Arclet(final Arc arc)
        {
            state = arc.getFromState();
            event = arc.getEvent();
        }

        /**
         * Compares the state indices first and then the events.
         */
        @Override
        public int compareTo(final Arclet other)
        {

            // Should compare the labels instead of the indices?
            if (this.state.getIndex() < other.state.getIndex())
            {
                return -1;
            }
            else if (this.state.getIndex() > other.state.getIndex())
            {
                return 1;
            }
            else
            {
                return this.event.compareTo(other.event);
            }
        }

        @Override
        public boolean equals(final Object obj)
        {
            final Arclet other = (Arclet) obj;

            return (state.equals(other.state) && event.equals(other.event));
        }

        @Override
        public int hashCode()
        {
            return (17*state.hashCode())*event.hashCode();
        }

        @Override
        public String toString()
        {
            return "<" + state.getName() + ", " + event.getLabel() + ">";
        }
    }

    /**
     * A set of <code>Arclet</code>:s.
     */
    class Arclets
        extends TreeSet<Arclet>
    {
		private static final long serialVersionUID = 1L;

		public Arclets(final Iterator<Arc> arcIterator)
        {
            while (arcIterator.hasNext())
            {
                final Arc arc = arcIterator.next();

                // Ignore epsilon events
                if (!arc.getEvent().isUnobservable())
                {
                    final Arclet arclet = new Arclet(arc);
                    add(arclet);
                    //arclets.add(arclet);
                }
            }
        }

        @Override
        public String toString()
        {
            String result = "";

            //Iterator it = arclets.iterator();
            final Iterator<Arclet> it = iterator();
            while (it.hasNext())
            {
                result += it.next();
                if (it.hasNext())
                {
                    result += ", ";
                }
            }
            return result;
        }
    }
}

/**
 * Class for characterizing states based on their B-properties.
 */
class StateInfoActiveEventsRule
    extends StateInfoIncoming
{
    private final Alphabet activeEvents;
    private final boolean isMarked;

    // Store hash number to avoid unnecessary calculation
    private int hash = 0;

    public StateInfoActiveEventsRule(final State state)
    {
        super(state);
        activeEvents = state.activeEvents(true);
        isMarked = state.isAccepting();
    }

    @Override
    public boolean equivalentTo(final StateInfo info)
    {
        assert(info instanceof StateInfoIncoming);
        final StateInfoActiveEventsRule other = (StateInfoActiveEventsRule) info;

        boolean result = super.equivalentTo(other);
        result &= activeEvents.equals(other.activeEvents);
        result &= isMarked == other.isMarked;

        return result;
    }

    @Override
    public int hashCode()
    {
        // If the number hasn't been generated before, do it now!
        if (hash == 0)
        {
            hash = super.hashCode();
            hash = hash * 37 + activeEvents.hashCode();
            hash = hash * 31 + (isMarked ? 1231 : 1237);
        }

        return hash;
    }

    @Override
    public String toString()
    {
        String result = super.toString();
        result += "\n\tActive: " + activeEvents;
        result += "\n\tMarked : " + isMarked;
        return result;
    }
}


class EquivalenceClasses

    extends StateSets
{
	private static final long serialVersionUID = 1L;

	public void addAll(final EquivalenceClassHolder equivClassHolder)
    {
        final Iterator<?> equivIt = equivClassHolder.iterator();
        while (equivIt.hasNext())
        {
            //System.out.println("eqaaaaaaaaaaa:"+equivIt);
            final EquivalenceClass currEquivClass = (EquivalenceClass) equivIt.next();

            currEquivClass.update();
            //System.out.println("currEquivClassafter addall:"+currEquivClass);
            super.add(currEquivClass);

        }
    }
        public void addAll(final EquivalenceClassHolder equivClassHolder, final boolean list)
    {
		if(list == false)
		{
			addAll(equivClassHolder);
		}
		else
		{

                    final Iterator<?> equivIt = equivClassHolder.iterator();
                    while (equivIt.hasNext())
                    {
                        final EquivalenceClass currEquivClass = (EquivalenceClass) equivIt.next();
                        super.add(currEquivClass);
                    }
		}
	}

}

class EquivalenceClass
    extends StateSet
{
	//private static final long serialVersionUID = 1L;
}

/**
 * Fabians play-around version of EquivalencClass
 * This class itself generates the state that it corresponds to
 * It sets the state-name as the concatenation of the original state names
 * This has the effect that it keeps the same name for singleton equivalence classes
 */
/*
class EqClass
    extends EquivalenceClass
{
    public EqClass()
    {
        super();
    }

    public EqClass(EquivalenceClass ec)
    {
        super(ec);
    }

    public State getState(Automaton theAutomaton)
    {
        // State newState = super.getState();
        if (newState == null)
        {
            // create a new state named as the concatenation of all state-names
            StringBuilder str = new StringBuilder();
            Iterator it = iterator();

            while (it.hasNext())
            {
                str.append(((State) it.next()).getName() + SupremicaProperties.getStateSeparator());
            }
            str.setLength(str.length() - SupremicaProperties.getStateSeparator().length());

            createNewState(theAutomaton, str.toString());
        }

        return newState;
    }
}
 */

/**
 * Temporary help object for storing new equivalence classes.
 *
 * @author  ka
 * @since  November 28, 2001
 */
class EquivalenceClassHolder
    extends HashMap<EquivalenceClass,EquivalenceClass>
{
    private static final long serialVersionUID = 1L;

    public void addState(final State state, final EquivalenceClass nextClass)
    {//System.out.println("key"+nextClass);
        // If the next equivalence class does not exist create it
        if (!containsKey(nextClass))
        {
            final EquivalenceClass newEquivClass = new EquivalenceClass(); //EqClassFactory.getEqClass(nextClass);
            put(nextClass, newEquivClass);
        }

        // Now get the EquivalenceClass associated with the nextEquivClass
        // and add the state to it.
        final EquivalenceClass theEquivalenceClass = get(nextClass);

        theEquivalenceClass.add(state);
        //System.out.println("value"+theEquivalenceClass);
    }

    public void addState(final State state, final EquivalenceClass nextClass,final State currState, final State nextState,final boolean list)
    {//System.out.println("key"+nextClass);
        // If the next equivalence class does not exist create it
        if (!containsKey(nextClass))
        {
            final EquivalenceClass newEquivClass = new EquivalenceClass(); //EqClassFactory.getEqClass(nextClass);
            put(nextClass, newEquivClass);
        }
     if (list){
         addState(state, nextClass);
     }


        // Now get the EquivalenceClass associated with the nextEquivClass
        // and add the state to it.
        final EquivalenceClass theEquivalenceClass = get(nextClass);

        theEquivalenceClass.add(state);
        System.out.println("value"+theEquivalenceClass);
    }


    public Iterator<EquivalenceClass> iterator()
    {
        return values().iterator();
    }

    public void update()
    {
        final Iterator<EquivalenceClass> equivClassIt = iterator();

        while (equivClassIt.hasNext())
        {
            final EquivalenceClass currEquivClass = equivClassIt.next();
            currEquivClass.update();
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        final Iterator<?> equivClassIt = iterator();

        sb.append("(");

        while (equivClassIt.hasNext())
        {
            final EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();

            sb.append(currEquivClass);
        }

        sb.append(")");

        return sb.toString();
    }
}

