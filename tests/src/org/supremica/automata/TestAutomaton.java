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

public class TestAutomaton
	extends TestCase
{
	public TestAutomaton(String name)
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
		TestSuite suite = new TestSuite(TestAutomaton.class);
		return suite;
	}

	public void testCopyConstructor()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Ex4_5_b));
			Automaton spec = theProject.getAutomaton("Spec");
			assertTrue(spec != null);
			Automaton specCopy = new Automaton(spec);
			assertTrue(spec.equalAutomaton(specCopy));
			spec.removeAllStates();
			assertTrue(spec.nbrOfStates() == 0);
			assertTrue(spec.nbrOfTransitions() == 0);
			assertTrue(spec.nbrOfEvents() == 3);
			assertTrue(specCopy.nbrOfStates() == 3);
			assertTrue(specCopy.nbrOfTransitions() == 3);
			assertTrue(specCopy.nbrOfEvents() == 3);
			Alphabet orgAlphabet = spec.getAlphabet();
			Alphabet copyAlphabet = specCopy.getAlphabet();
			assertTrue(orgAlphabet.nbrOfEvents() == 3);
			assertTrue(copyAlphabet.nbrOfEvents() == 3);
			orgAlphabet.clear();
			assertTrue(orgAlphabet.nbrOfEvents() == 0);
			assertTrue(copyAlphabet.nbrOfEvents() == 3);
			copyAlphabet.clear();
			assertTrue(orgAlphabet.nbrOfEvents() == 0);
			assertTrue(copyAlphabet.nbrOfEvents() == 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	@SuppressWarnings("deprecation")
	public void testAddState()
	{
		Automaton theAutomaton = new Automaton();
		assertTrue(theAutomaton.nbrOfStates() == 0);
		assertTrue(theAutomaton.nbrOfTransitions() == 0);
		assertTrue(theAutomaton.nbrOfEvents() == 0);
		// assertTrue(theAutomaton.getName().equals(""));
		State q1 = new State("q1");
		State q2 = new State("q2");
		assertTrue(theAutomaton.nbrOfStates() == 0);
		theAutomaton.addState(q1);
		assertTrue(theAutomaton.nbrOfStates() == 1);
		theAutomaton.addState(q2);
		assertTrue(theAutomaton.nbrOfStates() == 2);
	}

	public void testRemove()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Ex4_5_b));
			Automaton spec = theProject.getAutomaton("Spec");
			assertTrue(spec != null);
			State s1 = spec.getStateWithName("s1");
			assertTrue(s1 != null);
			State initialState = spec.getInitialState();
			assertTrue(s1 == initialState);
			assertTrue(s1.equalState(initialState));
			spec.removeState(s1);
			assertTrue(spec.nbrOfStates() == 2);
			assertTrue(spec.nbrOfEvents() == 3);
			assertTrue(spec.nbrOfTransitions() == 1);
			assertTrue(!spec.hasInitialState());
			assertTrue(spec.isNullAutomaton());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

}
