
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;

public class TestAutomataSynthesizer
    extends TestCase
{
    public TestAutomataSynthesizer(String name)
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
        TestSuite suite = new TestSuite(TestAutomataSynthesizer.class);
        return suite;
    }
    
    public void testAGV()
    {
        try
        {            
            ProjectBuildFromXml builder = new ProjectBuildFromXml();
            Project theProject = builder.build(TestFiles.getFile(TestFiles.AGV));
            assertTrue(theProject.nbrOfAutomata() == 17);
            SynthesizerOptions syntOptions = new SynthesizerOptions();
            syntOptions.setSynthesisType(SynthesisType.CONTROLLABLE);
            syntOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MODULAR);
            syntOptions.setPurge(true);
            syntOptions.setMaximallyPermissive(true);
            
            SynchronizationOptions syncOptions = new SynchronizationOptions();
            
            // Test Prioritized synchronization, although all events are prioritized in this example
            {
                AutomataSynthesizer synthesizer = new AutomataSynthesizer(theProject, syncOptions, syntOptions);
                Automata supAutomata = synthesizer.execute();
                
/*
                                assertTrue(synchronizer.getNumberOfStates() == 8);
                                //Automaton theAutomaton = synchronizer.getAutomaton();
                                Alphabet theAlphabet = theAutomaton.getAlphabet();
                                assertTrue(theAutomaton.getType() == AutomatonType.Specification);
                                assertTrue(theAutomaton.nbrOfStates() == 8);
                                assertTrue(theAutomaton.nbrOfAcceptingStates() == 2);
                                assertTrue(theAutomaton.nbrOfForbiddenStates() == 3);
                                assertTrue(theAutomaton.nbrOfTransitions() == 11);
                                assertTrue(theAutomaton.isAllEventsPrioritized());
                                assertTrue(theAutomaton.hasInitialState());
                                assertTrue(!theAutomaton.isNullAutomaton());
                                assertTrue(theAlphabet.nbrOfEvents() == 5);
                                assertTrue(theAlphabet.nbrOfControllableEvents() == 4);
                                assertTrue(theAlphabet.nbrOfPrioritizedEvents() == 5);
                                assertTrue(theAlphabet.nbrOfImmediateEvents() == 0);
                                assertTrue(theAlphabet.nbrOfEpsilonEvents() == 0);
 */
            }
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}

