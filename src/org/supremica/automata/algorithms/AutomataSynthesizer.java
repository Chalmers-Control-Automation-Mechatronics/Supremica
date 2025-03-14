
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.compositional.AutomataSynthesisAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer;
import net.sourceforge.waters.analysis.compositional.CompositionalSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizerNormality;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.SynthesisKindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.ModularSupervisor;
import org.supremica.automata.Supervisor;
import org.supremica.automata.BDD.BDDSynthesizer;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.algorithms.minimization.AutomataMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationHelper;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.ExecutionDialog;
import org.supremica.gui.ExecutionDialogMode;
import org.supremica.util.ActionTimer;

/**
 * Does synthesis in automata-scale, modularly,
 * uses AutomatonSynthesizer for monolithic problems
 */
public class AutomataSynthesizer
    implements Abortable
{
    private static Logger logger = LogManager.getLogger(AutomataSynthesizer.class);
    private Automata theAutomata;
    private final Map<LabeledEvent,Automata> ucEventToPlantMap;
    private final SynchronizationOptions synchronizationOptions;
    private final SynthesizerOptions synthesizerOptions;

    private ExecutionDialog executionDialog = null;

    // For the stopping
    private boolean mAbortRequested = false;
    private Abortable mThreadToAbort = null;

    private ActionTimer timer = new ActionTimer();
    // Statistics
    AutomataSynchronizerHelperStatistics helperStatistics = new AutomataSynchronizerHelperStatistics();

    private long numberOfStatesBDD;
    private long numberOfNodesBDD;
    public static boolean synthesis=false;
    public AutomataSynthesizer(final Automata theAutomata, final SynchronizationOptions synchronizationOptions,
        final SynthesizerOptions synthesizerOptions)
        throws IllegalArgumentException
    {
        // initialization stuff that need no computation
        this.theAutomata = theAutomata;
        this.synchronizationOptions = synchronizationOptions;
        this.synthesizerOptions = synthesizerOptions;

        // Some sanity tests (should already have been tested from ActionMan?)
        if ((synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MODULAR) &&
            !theAutomata.isAllEventsPrioritized())
        {
            throw new IllegalArgumentException("All events are not prioritized!");
        }

        // Fix this later
        synthesizerOptions.setRememberDisabledUncontrollableEvents(true);

        ucEventToPlantMap = AlphabetHelpers.buildUncontrollableEventToAutomataMap(theAutomata.getPlantAutomata());
    }

    /**
     * Synthesizes supervisors
     */
    public Automata execute()
    throws Exception
    {
        timer = new ActionTimer();
        timer.start();

        final Automata result = new Automata();

        if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MONOLITHIC)
        {
            // MONOLITHIC synthesis, just whack the entire stuff into the monolithic algo
            final MonolithicAutomataSynthesizer synthesizer = new MonolithicAutomataSynthesizer();
			mThreadToAbort = synthesizer;
			final MonolithicReturnValue retval = synthesizer.synthesizeSupervisor(
					theAutomata, synthesizerOptions, synchronizationOptions,
					executionDialog, helperStatistics, false);
			if (mAbortRequested) return new Automata();
			result.addAutomaton(retval.automaton);

			logger.debug("AutomataSynthesizer.execute::Monolithic with rename set to " + this.synthesizerOptions.doRename());
			if(this.synthesizerOptions.doRename())
			{
				final EnumerateStates enumerator = new EnumerateStates(result, "q");
				enumerator.execute();
			}
        }
        else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MODULAR)
        {
            // MODULAR (controllability) synthesis
            final Automata newSupervisors = doModular(theAutomata);

            if (mAbortRequested || newSupervisors == null) return new Automata();
            logger.info(helperStatistics);
            result.addAutomata(newSupervisors);
        }
        else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL)
        {
            // Use supervision equivalence minimization!

            // Prepare for synthesis
            // Make a copy
            theAutomata = new Automata(theAutomata);
            // Make preparations based on synthesis type
            final SynthesisType type = synthesizerOptions.getSynthesisType();
            if (type == SynthesisType.NONBLOCKING)
            {
                // Only nonblocking? Then everything should be considered controllable!
                for (final Automaton automaton : theAutomata)
                {
                    for (final LabeledEvent event : automaton.getAlphabet())
                    {
                        event.setControllable(true);
                    }
                }
            }
            else if (type == SynthesisType.CONTROLLABLE)
            {
                // Only controllable? Then everything should be considered marked...
                // and AFTER that, the specs must be plantified!
                for (final Automaton automaton : theAutomata)
                {
                    automaton.setAllStatesAccepting();
                }
                // Plantify specs
                MinimizationHelper.plantify(theAutomata);
            }
            else if (type == SynthesisType.NONBLOCKING_CONTROLLABLE)
            {

                // NONBLOCKING and controllable. Plantify the specifications and supervisors!
            	// if no plants in model, just set everything to type 'plant'
                if (theAutomata.plantIterator().hasNext()) MinimizationHelper.plantify(theAutomata);
                else {
                	for (final Automaton a : theAutomata) {
                		a.setComment("plant(" + a.getName() + ")");
                        a.setType(AutomatonType.PLANT);
                	}
                }
            }

            // Do the stuff!
            final AutomataMinimizer minimizer = new AutomataMinimizer(theAutomata);
            minimizer.setExecutionDialog(executionDialog);

            final MinimizationOptions options = MinimizationOptions.getDefaultSynthesisOptions();

            final Automata min = minimizer.getCompositionalMinimization(options);
            for (final Automaton sup: min)
            {

                sup.setComment("sup(" + min.getName() + ")");
                sup.setName(null);
            }

            // Present result
            if (min.size() == 1 && min.getFirstAutomaton().nbrOfStates() < 100)
            {
                // This may not be true if more advanced simplification rules have been used!
                logger.info("The following states are allowed by the maximally permissive, "
                    + "controllable and nonblocking supervisor: " +  min.getFirstAutomaton().getStateSet() + ".");
            }
            result.addAutomata(min);
        }
        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

         else if (synthesizerOptions.getSynthesisAlgorithm() ==
                  SynthesisAlgorithm.MONOLITHIC_WATERS)
         {

           final ProductDESProxyFactory factory =
             ProductDESElementFactory.getInstance();
           final KindTranslator translator;
           final EventProxy marking;
           final SynthesisType synthesisType =
             synthesizerOptions.getSynthesisType();
           switch (synthesisType) {
           case NONBLOCKING:
             translator = SynthesisKindTranslator.getInstanceWithoutControllability();
             marking = null;
             break;
           case CONTROLLABLE:
             theAutomata = new Automata(theAutomata);
             translator = SynthesisKindTranslator.getInstanceWithControllability();
             marking =
               factory.createEventProxy(":none", EventKind.PROPOSITION);
             break;
           case NONBLOCKING_CONTROLLABLE:
           case NONBLOCKING_CONTROLLABLE_NORMAL:
             theAutomata = new Automata(theAutomata);
             translator = SynthesisKindTranslator.getInstanceWithControllability();
             marking = null;
             break;
           default:
             throw new IllegalStateException
               ("Unsupported synthesis type " +
                synthesizerOptions.getSynthesisType() + "!");
           }
           final AutomataToWaters exporter = new AutomataToWaters(factory);
           final ProductDESProxy des = exporter.convertAutomata(theAutomata);
           final SupervisorSynthesizer synthesizer;
           if (synthesisType == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL) {
             synthesizer =
               new MonolithicSynthesizerNormality(des, factory, translator);
           } else {
             synthesizer =
               new MonolithicSynthesizer(des, factory, translator);
           }
           mThreadToAbort = synthesizer;
           synthesizer.setConfiguredDefaultMarking(marking);
           final SupervisorReductionFactory supervisorReduction;
           if (synthesizerOptions.getReduceSupervisors()) {
             supervisorReduction =
               new ProjectingSupervisorReductionFactory
               (SupervisorReductionMainMethod.SU_WONHAM);
           } else {
             supervisorReduction =
               new ProjectingSupervisorReductionFactory();
           }
           synthesizer.setSupervisorReductionFactory(supervisorReduction);
           final boolean supervisorLocalization =
             synthesizerOptions.getLocalizeSupervisors();
           synthesizer.setSupervisorLocalizationEnabled(supervisorLocalization);
           synthesizer.run();
           final ProductDESResult watersResult =
             synthesizer.getAnalysisResult();
           final ProjectBuildFromWaters importer =
             new ProjectBuildFromWaters(null);
           if (watersResult.isSatisfied()) {
             for (final AutomatonProxy proxy :
                  watersResult.getComputedAutomata()) {
               final Automaton aut = importer.build(proxy);
               result.addAutomaton(aut);
             }
           } else {
             final Automaton aut = new Automaton("empty_supervisor");
             aut.setType(AutomatonType.SUPERVISOR);
             result.addAutomaton(aut);
           }

         }
        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

        else if (synthesizerOptions.getSynthesisAlgorithm() ==
                 SynthesisAlgorithm.COMPOSITIONAL_WATERS)
        {

          final ProductDESProxyFactory factory =
            ProductDESElementFactory.getInstance();
          final KindTranslator translator;
          final EventProxy marking;
          switch (synthesizerOptions.getSynthesisType()) {
          case NONBLOCKING:
            translator = ConflictKindTranslator.getInstanceControllable();
            marking = null;
            break;
          case CONTROLLABLE:
            theAutomata = new Automata(theAutomata);
            MinimizationHelper.plantify(theAutomata);
            translator = IdenticalKindTranslator.getInstance();
            marking =
              factory.createEventProxy(":none", EventKind.PROPOSITION);
            break;
          case NONBLOCKING_CONTROLLABLE:
            theAutomata = new Automata(theAutomata);
            MinimizationHelper.plantify(theAutomata);
            translator = IdenticalKindTranslator.getInstance();
            marking = null;
            break;
          default:
            throw new IllegalStateException
              ("Unsupported synthesis type " +
               synthesizerOptions.getSynthesisType() + "!");
          }

          final AutomataToWaters exporter = new AutomataToWaters(factory);
          final ProductDESProxy des = exporter.convertAutomata(theAutomata);

          final CompositionalAutomataSynthesizer synthesizer =
            new CompositionalAutomataSynthesizer
              (des, factory, translator,
               AutomataSynthesisAbstractionProcedureFactory.WSOE);
          synthesizer.setConfiguredDefaultMarking(marking);
          final SupervisorReductionFactory supervisorReduction;
          if (synthesizerOptions.getReduceSupervisors()) {
            supervisorReduction =
              new ProjectingSupervisorReductionFactory
              (SupervisorReductionMainMethod.SU_WONHAM);
          } else {
            supervisorReduction =
              new ProjectingSupervisorReductionFactory();
          }
          synthesizer.setSupervisorReductionFactory(supervisorReduction);
          synthesizer.setSupervisorLocalizationEnabled
            (synthesizerOptions.getLocalizeSupervisors());
          synthesizer.setInternalStateLimit(5000);
          final MinimizationOptions mOptions = new MinimizationOptions();
          final String preselectingHeuristic = mOptions.getMinimizationPreselctingHeuristic().toStringAbbreviated();
          final String selectingHeuristic =  mOptions.getMinimizationSelctingHeuristic().toStringAbbreviated();
          if (preselectingHeuristic.equals("Pairs")) {
            synthesizer.setPreselectingMethod
            (AbstractCompositionalModelAnalyzer.Pairs);
          } else if (preselectingHeuristic.equals("MinT")) {
            synthesizer.setPreselectingMethod
            (AbstractCompositionalModelAnalyzer.MinT);
          } else if (preselectingHeuristic.equals("MaxS")) {
            synthesizer.setPreselectingMethod
            (AbstractCompositionalModelAnalyzer.MaxS);
          } else if (preselectingHeuristic.equals( "MustL")) {
            synthesizer.setPreselectingMethod
            (AbstractCompositionalModelAnalyzer.MustL);
          }

          if (selectingHeuristic.equals("MinSync")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MinSync);
          } else if (selectingHeuristic.equals("MaxL")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MaxL);
          } else if (selectingHeuristic.equals("MinS")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MinS);
          } else if (selectingHeuristic.equals("MinE")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MinE);
          } else if (selectingHeuristic.equals("MinF")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MinF);
          } else if (selectingHeuristic.equals("MaxC")) {
            synthesizer.setSelectionHeuristic
            (CompositionalSelectionHeuristicFactory.MaxC);
          }

          logger.info("Heuristics: " + preselectingHeuristic +" / "+
            selectingHeuristic);
          synthesizer.run();
          final ProductDESResult watersResult =
            synthesizer.getAnalysisResult();
          final ProjectBuildFromWaters importer =
            new ProjectBuildFromWaters(null);
          if (watersResult.isSatisfied()) {
            for (final AutomatonProxy proxy :
                 watersResult.getComputedAutomata()) {
              final Automaton aut = importer.build(proxy);
              result.addAutomaton(aut);
            }
          } else {
            final Automaton aut = new Automaton("empty_supervisor");
            aut.setType(AutomatonType.SUPERVISOR);
            result.addAutomaton(aut);
          }

        }

        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

        else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MONOLITHICBDD)
        {
            final BDDSynthesizer bddSynthesizer = new BDDSynthesizer(theAutomata);
            bddSynthesizer.computeSupervisor();
            bddSynthesizer.getSupervisor();
            bddSynthesizer.done();
        }
        else
        {
            logger.error("Unknown synthesis algorithm");
        }

        timer.stop();
        return result;
    }

    public String getTime()
    {
        return timer.toString();
    }

    public BigDecimal getTimeSeconds()
    {
        final String[] result = (getTime()).split("\\s");
        Float out = (float)-1;
        if(result.length==2)
            out = (Float.parseFloat(result[0]))/1000;
        else
        {
            float f1 = -1;
            float f2 = -1;
            if(result[1].equals("seconds"))
                f1 = Float.parseFloat(result[0]);
            else if(result[1].equals("minutes"))
                f1 = Float.parseFloat(result[0])*60;
            else if(result[1].equals("hours"))
                f1 = Float.parseFloat(result[0])*60*60;

            if(result[3].equals("milliseconds"))
                f2 = Float.parseFloat(result[2])/ 1000;
            else if(result[3].equals("seconds"))
                f2 = Float.parseFloat(result[2]);
            else if(result[3].equals("minutes"))
                f2 = Float.parseFloat(result[2])*60;

            out = (f1+f2);
        }

        BigDecimal bd = new BigDecimal(out);
        bd = bd.setScale(3, RoundingMode.DOWN);

        return bd;

    }

    public long getNbrOfStatesBDD()
    {
        return numberOfStatesBDD;
    }

    public long getNbrOfNodesBDD()
    {
        return numberOfNodesBDD;
    }
     /**
     * A method for automaton abstraction to figure out if the tauevent should be
     * renamed back to to the original events. In the case of synthesis it returns
     * true otherwise false.
     */
    public static boolean renameBack(){
        return synthesis;
    }

    /**
     * Removes from disabledUncontrollableEvents those events that are "insignificant"
     * Returns the result, which is an altered disabledUncontrollableEvents
     */
    private Alphabet checkMaximallyPermissive(final Automata automata, final Alphabet disabledUncontrollableEvents)
    {
        if (disabledUncontrollableEvents != null)
        {
            for (final Automaton currAutomaton : automata)
            {
                // disregard the uc-events of this spec/supervisor
                if (currAutomaton.isSupervisor() || currAutomaton.isSpecification())
                {
                    final Alphabet currAlphabet = currAutomaton.getAlphabet();

                    disabledUncontrollableEvents.minus(currAlphabet);
                }
            }

            // Remove those disabled events that are not included in another plant
            final LinkedList<LabeledEvent> eventsToBeRemoved = new LinkedList<LabeledEvent>();

            for (final LabeledEvent currEvent : disabledUncontrollableEvents)
            {
                //Set currAutomata = (Set) ucEventToPlantMap.get(currEvent);
                final Automata currAutomata = ucEventToPlantMap.get(currEvent);
                boolean removeEvent = true;

                // currAutomata contains those plant automata that contain this event.
                for (final Iterator<Automaton> autIt = currAutomata.iterator();
                autIt.hasNext(); )
                {
                    final Automaton currAutomaton = autIt.next();

                    if (currAutomaton.isPlant())
                    {
                        // Check if there is a plant not included in this
                        // modular supervisor. If no such plant exists then remove this
                        // event from the set of disabled events.
                        if (!automata.containsAutomaton(currAutomaton.getName()))
                        {
                            removeEvent = false;
                        }
                    }
                }

                if (removeEvent)
                {
                    eventsToBeRemoved.add(currEvent);
                }
            }

            for (final LabeledEvent currEvent : eventsToBeRemoved)
            {
                disabledUncontrollableEvents.removeEvent(currEvent);
            }
        }

        return disabledUncontrollableEvents;
    }

    /**
     * Does modular synthesis...
     */
    private Automata doModular(final Automata aut)
    throws Exception
    {
        // Automata that collects the calculated supervisors
        final Automata supervisors = new Automata();

        // Selector - always start with non-max perm
        final AutomataSelector selector = AutomataSelectorFactory.getAutomataSelector(aut, synthesizerOptions, !synthesizerOptions.getRemoveUnecessarySupervisors());   // MF Change here, return solitary specs if we are not to remove unnecessary supervisors

        // Initialize execution dialog
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (executionDialog != null)
                {
                    final int nbrOfSpecAndSup = theAutomata.getSpecificationAndSupervisorAutomata().size();
                    executionDialog.initProgressBar(0, nbrOfSpecAndSup);
                }
            }
        });

        // Loop over specs/sups AND their corresponding plants (dealt with by the selector)
        for (Automata automata = selector.next(); automata.size() > 0; automata = selector.next())
        {
            if (mAbortRequested)
            {
                return new Automata();
            }

            /* This if-clause handles the case with a spec that does not have any uc-events in common with any plant
             * Previously, such spec/supervisor were not treated at all, but here a copy gets added as supervisor, since
             * this makes more sense from teh view that the modular approach creates one sup for each spec.
             * If the Remove unnecessary supervisors flag is turned on, such specs/sups are not considered.
             * A boolean parameter was added to the AutomataSelectorFactory and to PerSpecificationAutomataSelector to handle this
             * Additional changes to the modular algorithm was done below, to handle the case of controllable specs that do share uc-evenst with some plant
             * MF, Sept-Oct 2011
             */
            if(automata.size() == 1 && !synthesizerOptions.getRemoveUnecessarySupervisors())
            {   // This spec does not have any uc-events, hence it is directly a supervisor, add it immediately and break loop // MF
              final Automaton sol_spec = automata.getAutomatonAt(0); // The one and only
              final Automaton sol_spec_sup = sol_spec.clone();
              sol_spec_sup.setName("sup(" + sol_spec.getName() + ")");
              sol_spec_sup.setType(AutomatonType.SUPERVISOR);
              supervisors.addAutomaton(sol_spec_sup);
              logger.info(sol_spec.getName() + " has no uc events in common with any plant, hence is directly controllable.");
              continue; // next loop
            }

            // In the non incremental approach, immediately add all plants that are related
            // by uncontrollable events. Otherwise this is done incrementally below
            if (synthesizerOptions.getMaximallyPermissive() &&
                !synthesizerOptions.getMaximallyPermissiveIncremental())
            {
                // Loop until no new uncontrollable events are found
                Alphabet uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();
                int previousSize = 0;
                while (uncontrollableEvents.size() > previousSize)
                {
                    // Count current amount of uncontrollable
                    previousSize = uncontrollableEvents.size();
                    // Add plants that share those events
                    automata = selector.addPlants(uncontrollableEvents);
                    // Which uncontrollable exist now?
                    uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();
                }
            }

            // Do monolithic synthesis on this subsystem
            final MonolithicAutomataSynthesizer synthesizer = new MonolithicAutomataSynthesizer();
			mThreadToAbort = synthesizer;
			MonolithicReturnValue retval = synthesizer.synthesizeSupervisor(
					automata, synthesizerOptions, synchronizationOptions,
					executionDialog, helperStatistics, false);

            if (mAbortRequested)
            {
                return new Automata();
            }

            // Did anything happen?
            if (retval.didSomething)
            {
                Alphabet disabledUncontrollableEvents = checkMaximallyPermissive(automata, retval.disabledUncontrollableEvents);

                // Do we care about max perm?
                if (synthesizerOptions.getMaximallyPermissive())
                {
                    // As long as uncontrollable events had to be "disabled" by the supervisor that
                    // instead could have been disabled by a plant (right?) repeat the below...
                    // In this manner, we're *guaranteed* max perm
                    while (disabledUncontrollableEvents.size() > 0)
                    {
                        // Note that in the non-incremental approach, this will add no new plants
                        // since they are already added!
                        if (synthesizerOptions.addOnePlantAtATime)
                            // Add one plant
                            automata = selector.addPlant(disabledUncontrollableEvents);
                        else
                            // Add all plants
                            automata = selector.addPlants(disabledUncontrollableEvents);

                        // Do monolithic synthesis on this extended subsystem
                        retval = synthesizer.synthesizeSupervisor(automata,
								synthesizerOptions, synchronizationOptions,
								executionDialog, helperStatistics, false);

                        if (mAbortRequested)
                        {
                            return new Automata();
                        }

                        disabledUncontrollableEvents = checkMaximallyPermissive(automata, retval.disabledUncontrollableEvents);
                    }
                }
                else
                {
                    // We do not care about max perm, but could at least notify
                    if (disabledUncontrollableEvents.size() > 0)
                    {
                        // Not guaranteed to be max perm
                        logger.info("The synthesized supervisor '" + retval.automaton.getComment() +
                            "' might not be maximally permissive since the events " +
                            disabledUncontrollableEvents + " are included in the plant but not " +
                            "in the supervisor.");
                    }
                    else
                    {
                        // It's max perm in any case
                        logger.info("The synthesized supervisor '" + retval.automaton.getComment() +
                            "' is maximally permissive.");
                    }
                }

                supervisors.addAutomaton(retval.automaton);
            }
            else if (!synthesizerOptions.getRemoveUnecessarySupervisors())  // Added to handle the case of controllable specs that share uc-events with some plant // MF
            {   // synthesis for this particular sub-system did nothing, meaning that the spec is usable as a supervisor
                // But we only add it if we are not to remove unnecessary supervisors, otherwise we would first add then remove this one
                supervisors.addAutomaton(retval.automaton);
                logger.info(retval.automaton.getName() + " is supervisor directly by synch.");
            }

            // Update execution dialog
            if (executionDialog != null)
            {
                executionDialog.setProgress(selector.getProgress());
            }
        }

        /*
        // If no spec/sup is in the selected automata, only nonblocking requires work
        // if we've not seen any spec, do monolithic synthesis on each plant individually
        if (!selector.hadSpec())
        {
                logger.debug("No spec/sup seen, performing monolithic synthesis on the each plant.");

                MonolithicReturnValue retval = doMonolithic(aut);

                if (stopRequested)
                {
                return new Automata();
                }

                if (retval.didSomething)
                {
                supervisors.addAutomaton(retval.automaton);
                }
        }
         */

        // Should we optimize the result (throw unnecessary supervisors away)
        if (synthesizerOptions.getRemoveUnecessarySupervisors())
        {
            if (executionDialog != null)
            {
                executionDialog.setMode(ExecutionDialogMode.SYNTHESISOPTIMIZING);
                executionDialog.initProgressBar(0, supervisors.size());
            }

            removeUnnecessarySupervisors(aut, supervisors);
        }

        // Did we do anything at all?
        if (supervisors.size() == 0)
        {
            logger.info("No problems found, the current specifications and supervisors " +
                "can be used to supervise the system.");
        }

        // NONBLOCKING synthesis is not implemented...
        if ((synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING) || (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING_CONTROLLABLE))
        {
            logger.warn("Currently global nonblocking is NOT guaranteed. The only guarantee " +
                "is that each supervisor is individually nonblocking with respect to the " +
                "plants it controls");
        }

        // Return the new supervisors
        return supervisors;
    }


    /**
     * Removes unnecessary automata, i.e. synthesised supervisors that
     * don't affect the controllability.
     *
     * Note: At the moment, only controllability is checked, no
     * non-blocking.
     *
     * After this method has completed, the unnecessary supervisors have been removed from
     * candidateSupervisors.
     *
     * @param  theAutomata contains the originally given specs/sups and plants
     * @param  candidateSupervisors the Automata-object containing the new supervisors, is altered!
     */
    private void removeUnnecessarySupervisors(final Automata theAutomata, final Automata candidateSupervisors)
    throws Exception
    {
        logger.debug("AutomataSynthesizer.optimize");

        // Deep copy the new automata, so we can purge without affecting the originals
        final Automata newSupervisors = new Automata(candidateSupervisors);

        // Make sure the automata are purged - they must be for the optimization to work...
        if (!synthesizerOptions.doPurge())
        {
            // We have not purged earlier - do that now!
            final Iterator<Automaton> autIt = newSupervisors.iterator();
            while (autIt.hasNext())
            {
                final AutomatonPurge automatonPurge = new AutomatonPurge(autIt.next());
                automatonPurge.execute();
            }
        }

        final Automata currAutomata = new Automata();
        currAutomata.addAutomata(theAutomata);
        currAutomata.addAutomata(newSupervisors);

        // Remove one of the candidate supervisors in newSupervisors at a time and see
        // if the behaviour of the rest of the system is included in that supervisor
        // (i.e. the system already behaves like that without the supervisor).

        // Remove the new automata one by one and examine if it had impact on the result.
        int progress = 0;
        for (int i = newSupervisors.size() - 1; i >= 0; i--)
        {
            final Automaton currSupervisor = newSupervisors.getAutomatonAt(i);
            currAutomata.removeAutomaton(currSupervisor);
            progress++;

            // Prepare a verifier for verifying the need for this supervisor
	    final VerificationOptions verificationOptions = VerificationOptions.getDefaultControllabilityOptions();
            final SynchronizationOptions synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
            final AutomataVerifier verifier = new AutomataVerifier(currAutomata, verificationOptions,
                synchronizationOptions, null);

            if (mAbortRequested)
            {
                return;
            }

            // Will the supervisor affect the system at all?
            logger.info("Examining whether the supervisor candidate " +
                        currSupervisor + " is needed.");
            mThreadToAbort = verifier;
            // if (AutomataVerifier.verifyModularInclusion(currAutomata, new Automata(currSupervisor)))
            if (verifier.verify())
            {
                // Nope, this one didn't matter! Remove it!
                // currAutomata.removeAutomaton(currSupervisor); // Use for LanguageInclusion
                candidateSupervisors.removeAutomaton(candidateSupervisors.getAutomatonAt(i));
            }
            else
            {
                // This one was important! Don't remove it and put it back!!
                currAutomata.addAutomaton(currSupervisor); // Not for LanguageInclusion
            }
            mThreadToAbort = null;

            if (executionDialog != null)
            {
                executionDialog.setProgress(progress);
            }
        }
    }

    public void setExecutionDialog(final ExecutionDialog dialog)
    {
        executionDialog = dialog;
    }

    /**
     * Method that stops the synthesizer as soon as possible.
     *
     * @see  ExecutionDialog
     */
    @Override
    public void requestAbort(final AbortRequester sender)
    {
        mAbortRequested = true;

        logger.debug("AutomataSynthesizer requested to stop.");

        // Stop currently executing thread!
        if (mThreadToAbort != null)
        {
            mThreadToAbort.requestAbort(sender);
        }
    }

    @Override
    public boolean isAborting()
    {
        return mAbortRequested;
    }

    @Override
    public void resetAbort(){
      mAbortRequested = false;
    }

    /**
     * Default method for SYNTHESIZING a controllable and nonblocking supervisor.
     */
    public static Supervisor synthesizeControllableNonblocking(final Automata model)
    throws Exception
    {
        final SynchronizationOptions synchOptions = SynchronizationOptions.getDefaultSynthesisOptions();
        final SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultMonolithicCNBSynthesizerOptions();
        final AutomataSynthesizer synthesizer = new AutomataSynthesizer(model, synchOptions, synthOptions );
        final Automata result = synthesizer.execute();

        return new ModularSupervisor(result);
    }
}
