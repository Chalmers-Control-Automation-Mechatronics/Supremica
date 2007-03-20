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
package org.supremica.automata;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.automata.IO.*;
import org.supremica.testhelpers.*;


public class TestState
	extends TestCase
{
	public TestState(String name)
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
		TestSuite suite = new TestSuite(TestState.class);
		return suite;
	}

	public void testEpsilonClosure()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.ObservationEquivalence));
			Automaton aut = theProject.getAutomaton("P1F1F2");
			State state = aut.getInitialState();

			assertTrue(state.activeEvents(false).size() == 2);
			assertTrue(state.activeEvents(true).size() == 2);

			LabeledEvent event = new LabeledEvent("apa");
			aut.getAlphabet().addEvent(event);
			State from = aut.getStateWithName("ready.2.2");
			State to = aut.getStateWithName("think.0.1");
			aut.addArc(new Arc(from, to, event));
			assertTrue(state.activeEvents(false).size() == 2);
			assertTrue(state.activeEvents(true).size() == 3);

			LabeledEvent tau = aut.getAlphabet().getEvent("tau");
			assertTrue(state.nextStates(tau, false).size() == 2);
			assertTrue(state.nextStates(tau, true).size() == 7);

			aut.addArc(new Arc(to, from, tau));
			assertTrue(state.epsilonClosure(true).size() == 7);
			assertTrue(state.epsilonClosure(false).size() == 7);
			assertTrue(state.backwardsEpsilonClosure().size() == 9);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}
}
