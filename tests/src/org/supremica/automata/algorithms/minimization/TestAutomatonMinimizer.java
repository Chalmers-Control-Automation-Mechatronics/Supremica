//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica Tests
//# PACKAGE: org.supremica.automata.algorithms.minimization
//# CLASS:   TestAutomatonMinimizer
//###########################################################################
//# $Id$
//###########################################################################

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

package org.supremica.automata.algorithms.minimization;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.IO.ProjectBuildFromXML;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.testhelpers.TestFiles;


public class TestAutomatonMinimizer
    extends AbstractAnalysisTest
{
    //#######################################################################
    //# Entry points in junit.framework.TestCase
    public TestAutomatonMinimizer(final String name)
    {
        super(name);
    }

    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite()
    {
        final TestSuite suite = new TestSuite(TestAutomatonMinimizer.class);
        return suite;
    }

    public static void main(final String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final DocumentManager manager = getDocumentManager();
        mBuilder = new ProjectBuildFromWaters(manager);
    }


    //#######################################################################
    //# Supremica Test Cases
    public void testLanguageEquivalenceMinimization()
      throws Exception
    {
            final ProjectBuildFromXML builder = new ProjectBuildFromXML();
            final Project theProject = builder.build(TestFiles.getFile(TestFiles.MachineBufferMachine));
            final SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynchronizationOptions();
            syncOptions.setForbidUncontrollableStates(true);
            final AutomataSynchronizer synchronizer = new AutomataSynchronizer(theProject, syncOptions, false);
            synchronizer.execute();
            final Automaton synch = synchronizer.getAutomaton();
            final Alphabet alpha = synch.getAlphabet();
            Alphabet hide = new Alphabet();
            hide.addEvent(alpha.getEvent("Start1"));
            hide.addEvent(alpha.getEvent("Start2"));
            hide.addEvent(alpha.getEvent("End1"));
            hide.addEvent(alpha.getEvent("End2"));
            hide = AlphabetHelpers.minus(alpha, hide);
            synch.hide(hide, false);

            final MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.LANGUAGEEQUIVALENCE);

            // Test language equivalence minimization
            final AutomatonMinimizer minimizer = new AutomatonMinimizer(synch);
            final Automaton languageMin = minimizer.getMinimizedAutomaton(options);
            assertTrue(languageMin.nbrOfStates() == 8);
            assertTrue(languageMin.nbrOfTransitions() == 18);
    }

    public void testObservationEquivalenceMinimization()
    {
        try
        {
            // Test
            final ProjectBuildFromXML builder = new ProjectBuildFromXML();
            final Project theProject = builder.build(TestFiles.getFile(TestFiles.ObservationEquivalence));
            final MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.OBSERVATIONEQUIVALENCE);
            options.setAlsoTransitions(true);
            options.setKeepOriginal(true);

            AutomatonMinimizer minimizer;
            Automaton observationMin;

            // Test observation equivalence minimization
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("viii.a"));
            observationMin = minimizer.getMinimizedAutomaton(options);
            assertTrue((observationMin.nbrOfStates() == 5) &&
                (observationMin.nbrOfTransitions() == 6) &&
                (observationMin.getStateWithName("1").nbrOfOutgoingArcs() == 2));

            // Test observation equivalence minimization
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("viii.b"));
            observationMin = minimizer.getMinimizedAutomaton(options);
            assertTrue((observationMin.nbrOfStates() == 5) &&
                (observationMin.nbrOfTransitions() == 9) &&
                (observationMin.getStateWithName("0").nbrOfOutgoingArcs() == 3));

            // Test a part of the dining philosophers example (observation equivalence minimization)
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("P1F1F2"));
            observationMin = minimizer.getMinimizedAutomaton(options);
            assertTrue(observationMin.nbrOfStates() == 6);
            assertTrue(observationMin.nbrOfTransitions() == 10);
            assertTrue(observationMin.nbrOfEpsilonTransitions() == 2);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void testBisimulationEquivalenceMinimization()
    {
        // Check if the library with the native methods is ok
        if (!BisimulationEquivalenceMinimizer.libraryLoaded())
        {
            System.err.println("Library BisimulationEquivalence not in library path, test skipped.");
            return;
        }

        try
        {
            final ProjectBuildFromXML builder = new ProjectBuildFromXML();
            final Project theProject = builder.build(TestFiles.getFile(TestFiles.BisimulationEquivalence));
            final MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.BISIMULATIONEQUIVALENCE);
            options.setAlsoTransitions(true);
            options.setKeepOriginal(true);

            AutomatonMinimizer minimizer;
            Automaton min;

            //System.err.println("Fernandez");
            // Test bisimulation equivalence minimization
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("Test (from Fernandez)"));
            min = minimizer.getMinimizedAutomaton(options);
            assertTrue((min.nbrOfStates() == 3) &&
                (min.nbrOfTransitions() == 3) &&
                (min.getStateWithName("2,0,1").nbrOfOutgoingArcs() == 2));

            //System.err.println("Westin");
            // Test bisimulation equivalence minimization
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("Test (from Westin)"));
            min = minimizer.getMinimizedAutomaton(options);
            assertTrue((min.nbrOfStates() == 4) &&
                (min.nbrOfTransitions() == 4) &&
                (min.getStateWithName("q2,q1,p1").nbrOfOutgoingArcs() == 2));
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void testConflictEquivalenceMinimization()
    {
        try
        {
            final ProjectBuildFromXML builder = new ProjectBuildFromXML();
            final Project theProject = builder.build(TestFiles.getFile(TestFiles.ConflictEquivalence));

            final Automata tests = theProject.getPlantAutomata();
            final Automata min = new Automata();
            final Automata key = theProject.getSpecificationAutomata();

            // Iterate over tests and minimize each individually
            final Iterator<Automaton> autIt = tests.iterator();
            while (autIt.hasNext())
            {
                final Automaton currAutomaton = autIt.next();

                // Minimize this one
                final AutomatonMinimizer minimizer = new AutomatonMinimizer(currAutomaton);
                final MinimizationOptions options = new MinimizationOptions();
                options.setMinimizationType(EquivalenceRelation.CONFLICTEQUIVALENCE);
                options.setCompositionalMinimization(false);
                options.setAlsoTransitions(true);
                options.setKeepOriginal(true);
                options.setMinimizationStrategy(MinimizationStrategy.MostStatesFirst);
                final Automaton newAutomaton = minimizer.getMinimizedAutomaton(options);
                min.addAutomaton(newAutomaton);
            }

            // Compare the minimized automata with the correct solution
            for (int i=0; i<min.size(); i++)
            {
                final Automaton currMin = min.getAutomatonAt(i);
                final Automaton currKey = key.getAutomatonAt(i);

                assertTrue(currMin.nbrOfStates() == currKey.nbrOfStates());
                assertTrue(currMin.getAlphabet().equals(currKey.getAlphabet()));
                assertTrue(currMin.nbrOfTransitions() == currKey.nbrOfTransitions());
                assertTrue(currMin.nbrOfEpsilonTransitions() == currKey.nbrOfEpsilonTransitions());
                assertTrue(currMin.getInitialState().nbrOfOutgoingArcs() == currKey.getInitialState().nbrOfOutgoingArcs());
                assertTrue(currMin.getInitialState().nbrOfIncomingArcs() == currKey.getInitialState().nbrOfIncomingArcs());
            }
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }


    //#######################################################################
    //# Waters Test Cases
    public void testAutomatonMinimizerForWaters()
    throws Exception
    {
        final ProductDESProxy des = getBmwDES();
        final AutomatonProxy aut = getComfortFunctionAutomaton(des);
        final Set<EventProxy> target = getReqCloseHidingAlhabet(des, aut);
        final Alphabet alphabet = mBuilder.buildAlphabet(target);
        final MinimizationOptions opt = getMinimizationOptions(aut, alphabet);
        final Automaton supaut = mBuilder.build(aut);
        final AutomatonMinimizer minimizer = new AutomatonMinimizer(supaut);
        final AutomatonProxy minaut = minimizer.getMinimizedAutomaton(opt);
        final Set<EventProxy> minevents = minaut.getEvents();
        final Set<EventProxy> fixedevents =
			getUniqueWatersEvents(minevents, des);
        assertEquals("Unexpected event set in result!", target, fixedevents);
    }

    public void testAutomataMinimizerForWaters()
    throws Exception
    {
        final ProductDESProxy des = getBmwDES();
        final AutomatonProxy aut = getComfortFunctionAutomaton(des);
        final Set<EventProxy> target = getReqCloseHidingAlhabet(des, aut);
        final Alphabet alphabet = mBuilder.buildAlphabet(target);
        final MinimizationOptions opt = getMinimizationOptions(aut, alphabet);
        final Automaton supaut = mBuilder.build(aut);
        final Automata supmodel = new Automata(supaut);
        final AutomataMinimizer minimizer = new AutomataMinimizer(supmodel);
        final ProductDESProxy minmodel =
            minimizer.getCompositionalMinimization(opt);
        assertEquals("Unexpected number of automata in result!",
            1, minmodel.getAutomata().size());
        final AutomatonProxy minaut = minmodel.getAutomata().iterator().next();
        final Set<EventProxy> minevents = minaut.getEvents();
        final Set<EventProxy> fixedevents =
			getUniqueWatersEvents(minevents, des);
        assertEquals("Unexpected event set in result!", target, fixedevents);
    }


    //#######################################################################
    //# Auxiliary Methods for Waters Test
    private ProductDESProxy getBmwDES()
    throws Exception
    {
        final String groupname = "valid";
        final String subdirname = "bmw_fh";
        final String filename = "bmw_fh.wdes";
        final File rootdir = getWatersInputRoot();
        final File groupdir = new File(rootdir, groupname);
        final File subdir = new File(groupdir, subdirname);
        final File file = new File(subdir, filename);
        return getCompiledDES(file);
    }

    private AutomatonProxy getComfortFunctionAutomaton
        (final ProductDESProxy des)
    {
        final String autname = "comfort_function";
        return findAutomaton(des, autname);
    }

    private Set<EventProxy> getReqCloseHidingAlhabet
		(final ProductDESProxy des, final AutomatonProxy aut)
    {
        final String eventname = "REQ[CLOSE]";
        final EventProxy victim = findEvent(des, eventname);
        final Set<EventProxy> target =
            new HashSet<EventProxy>(aut.getEvents());
        target.remove(victim);
        return target;
    }

    private MinimizationOptions getMinimizationOptions
        (final AutomatonProxy aut, final Alphabet target)
    {
        final int numstates = aut.getStates().size();
        final MinimizationOptions opt = new MinimizationOptions();
        opt.setAlsoTransitions(true);
        opt.setComponentSizeLimit(10 * numstates);
        opt.setCompositionalMinimization(true);
        opt.setIgnoreMarking(true);
        opt.setKeepOriginal(false);
        opt.setMinimizationType(EquivalenceRelation.LANGUAGEEQUIVALENCE);
        opt.setTargetAlphabet(target);
        return opt;
    }

	/**
	 * Gets the original WATERS events from a Supremica alphabet.
	 * WATERS requires unique {@link EventProxy} objects, unfortunately
	 * Supremica does not provide them. This method is but an ugly and
	 * inefficient workaround to the problem.
	 * @param  alphabet A collection of events obtained from a Supremica
	 *                  automaton. Typically these are {@link LabeledEvent}
	 *                  objects.
	 * @param  des      The product DES that provides the set of WATERS
	 *                  {@link EventProxy} objects to be used.
	 * @return The set of those {@link EventProxy} objects in the alphabet
	 *         of the given product DES that have the same names as the
	 *         events in the given alphabet.
	 * @throws NameNotFoundException to indicate that an event name was not
	 *                  found in the product DES
	 * @throws DuplicateNameException to indicate that a name was used
	 *                  more than once.
	 */
	private Set<EventProxy> getUniqueWatersEvents
		(final Collection<EventProxy> alphabet, final ProductDESProxy des)
	throws DuplicateNameException, NameNotFoundException
	{
		final int size = alphabet.size();
		final Set<EventProxy> result = new HashSet<EventProxy>(size);
		for (final EventProxy supevent : alphabet) {
			final String name = supevent.getName();
			final EventProxy event = getEvent(des, name);
			if (event != null) {
			final boolean added = result.add(event);
			  if (!added) {
				throw new DuplicateNameException
					("Duplicate event name '" + name +
					 "' in Supremica alphabet!");
			  }
			}
		}
		return result;
	}


    //#######################################################################
    //# Data Members
    private ProjectBuildFromWaters mBuilder;

}
