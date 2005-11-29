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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.minimization.BisimulationEquivalenceMinimizer;
import org.supremica.automata.IO.*;

import java.util.*;

public class TestAutomatonMinimizer
    extends TestCase
{
    public TestAutomatonMinimizer(String name)
    {
        super(name);
    }

    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown()
    {
    }

    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestAutomatonMinimizer.class);
        return suite;
    }

    public void testLanguageEquivalenceMinimization()
    {
        try
        {
            ProjectBuildFromXml builder = new ProjectBuildFromXml();
            Project theProject = builder.build(TestFiles.getFile(TestFiles.MachineBufferMachine));
            SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynchronizationOptions();
            syncOptions.setForbidUncontrollableStates(true);
            AutomataSynchronizer synchronizer = new AutomataSynchronizer(theProject, syncOptions);
            synchronizer.execute();
            Automaton synch = synchronizer.getAutomaton();
            Alphabet alpha = synch.getAlphabet();
            Alphabet hide = new Alphabet();
            hide.addEvent(alpha.getEvent("Start1"));
            hide.addEvent(alpha.getEvent("Start2"));
            hide.addEvent(alpha.getEvent("End1"));
            hide.addEvent(alpha.getEvent("End2"));
            hide = AlphabetHelpers.minus(alpha, hide);
            synch.hide(hide);

            MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.LanguageEquivalence);

            // Test language equivalence minimization
            AutomatonMinimizer minimizer = new AutomatonMinimizer(synch);
            Automaton languageMin = minimizer.getMinimizedAutomaton(options);
            assertTrue(languageMin.nbrOfStates() == 8);
            assertTrue(languageMin.nbrOfTransitions() == 18);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void testObservationEquivalenceMinimization()
    {
        try
        {
			// Test
            ProjectBuildFromXml builder = new ProjectBuildFromXml();
            Project theProject = builder.build(TestFiles.getFile(TestFiles.ObservationEquivalence));
            MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.ObservationEquivalence);
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
        catch (Exception ex)
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
            ProjectBuildFromXml builder = new ProjectBuildFromXml();
            Project theProject = builder.build(TestFiles.getFile(TestFiles.BisimulationEquivalence));
            MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
            options.setMinimizationType(EquivalenceRelation.BisimulationEquivalence);
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
                       (min.getStateWithName("2.0.1").nbrOfOutgoingArcs() == 2));

            //System.err.println("Westin");
            // Test bisimulation equivalence minimization
            minimizer = new AutomatonMinimizer(theProject.getAutomaton("Test (from Westin)"));
            min = minimizer.getMinimizedAutomaton(options);
            assertTrue((min.nbrOfStates() == 4) &&
                       (min.nbrOfTransitions() == 4) &&
                       (min.getStateWithName("q2.q1.p1").nbrOfOutgoingArcs() == 2));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void testConflictEquivalenceMinimization()
    {
        try
        {
            ProjectBuildFromXml builder = new ProjectBuildFromXml();
            Project theProject = builder.build(TestFiles.getFile(TestFiles.ConflictEquivalence));

            Automata tests = theProject.getPlantAutomata();
            Automata min = new Automata();
            Automata key = theProject.getSpecificationAutomata();

            // Iterate over tests and minimize each individually
            Iterator<Automaton> autIt = tests.iterator();
            while (autIt.hasNext())
            {
                Automaton currAutomaton = autIt.next();

                // Minimize this one
                AutomatonMinimizer minimizer = new AutomatonMinimizer(currAutomaton);
                MinimizationOptions options = new MinimizationOptions();
                options.setMinimizationType(EquivalenceRelation.ConflictEquivalence);
                options.setCompositionalMinimization(false);
                options.setAlsoTransitions(true);
                options.setKeepOriginal(true);
				options.setMinimizationStrategy(MinimizationStrategy.MostStatesFirst);
                Automaton newAutomaton = minimizer.getMinimizedAutomaton(options);
                min.addAutomaton(newAutomaton);
            }

            // Compare the minimized automata with the correct solution
            for (int i=0; i<min.size(); i++)
            {
                Automaton currMin = min.getAutomatonAt(i);
                Automaton currKey = key.getAutomatonAt(i);

                // System.err.println(currMin + " vs. " + currKey);

                assertTrue(currMin.nbrOfStates() == currKey.nbrOfStates());
                assertTrue(currMin.getAlphabet().equals(currKey.getAlphabet()));
                assertTrue(currMin.nbrOfTransitions() == currKey.nbrOfTransitions());
                assertTrue(currMin.nbrOfEpsilonTransitions() == currKey.nbrOfEpsilonTransitions());
                assertTrue(currMin.getInitialState().nbrOfOutgoingArcs() == currKey.getInitialState().nbrOfOutgoingArcs());
                assertTrue(currMin.getInitialState().nbrOfIncomingArcs() == currKey.getInitialState().nbrOfIncomingArcs());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}
