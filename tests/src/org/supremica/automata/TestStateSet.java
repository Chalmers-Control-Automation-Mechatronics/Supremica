
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

public class TestStateSet
	extends TestCase
{

	public TestStateSet(String name)
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
		TestSuite suite = new TestSuite(TestStateSet.class);
		return suite;
	}

	public void testStateSets()
	{
		State q0 = new State("q0"); // id and name, set to the same
		State q1 = new State("q1");
		State q2 = new State("q2");
		State q3 = new State("q3");

		StateSet oneset = new StateSet();
		assertTrue(oneset.size() == 0);
		oneset.add(q0);
		assertTrue(oneset.size() == 1);
		oneset.add(q1);
		assertTrue(oneset.size() == 2);
		// oneset.add(q2);
		// oneset.add(q3);
		oneset.add(q1);
		assertTrue(oneset.size() == 2); // should not add existing

		StateSet twoset = new StateSet();
		assertTrue(twoset.size() == 0);
		twoset.add(q0);
		assertTrue(twoset.size() == 1);
		twoset.add(q1);
		assertTrue(twoset.size() == 2);
		// twoset.add(q2);
		// twoset.add(q3);
		twoset.add(new State(q0));
		assertTrue(twoset.size() == 2); // should not add existing

		assertTrue(oneset == oneset);
		assertTrue(oneset.equals(oneset));
		assertTrue(!(oneset == twoset));
		assertTrue(oneset.equals(twoset));
		
		try
		{
			ProjectBuildFromXml builder = new ProjectBuildFromXml();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.CentralLocking3Doors));
			
			Automaton aut = theProject.getAutomaton("decoder");
			StateSet ss = new StateSet(aut);
			StateSet ps = ss.previousStates(aut.getAlphabet().getEvent("ER"));

			assertTrue(ps.size() == 5);
			// System.err.println(ps);
		}
		catch (Exception ex)
		{
			System.err.println(ex);
		}
	}
}
