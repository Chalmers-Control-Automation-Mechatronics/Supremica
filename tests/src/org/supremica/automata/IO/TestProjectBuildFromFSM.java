
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
package org.supremica.automata.IO;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.automata.*;

public class TestProjectBuildFromFSM
	extends TestCase
{
	public TestProjectBuildFromFSM(String name)
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
		TestSuite suite = new TestSuite(TestProjectBuildFromFSM.class);
		return suite;
	}

	public void rem_testUMDES_1()
	{
		try
		{

			ProjectBuildFromFSM builder = new ProjectBuildFromFSM();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.UMDES_1).toURL());
			assertTrue(theProject.nbrOfAutomata() == 1);
			// assertTrue(theProject.getName().equals("Ex4_5"));
			assertTrue(theProject.isDeterministic());
			assertTrue(theProject.hasAcceptingState());
			assertTrue(theProject.isAllEventsPrioritized());
			assertTrue(theProject.isEventControllabilityConsistent());
			assertTrue(!theProject.hasSelfLoop());

			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				Alphabet currAlphabet = currAutomaton.getAlphabet();
				String autName = currAutomaton.getName();
				assertTrue(autName != null);
				assertTrue(currAlphabet != null);

				assertTrue(currAutomaton.getType() == AutomatonType.Specification);
				assertTrue(currAutomaton.nbrOfStates() == 4);
				assertTrue(currAutomaton.nbrOfEvents() == 5);
				assertTrue(currAutomaton.nbrOfTransitions() == 4);
				assertTrue(currAutomaton.hasInitialState());
				assertTrue(!currAutomaton.hasSelfLoop());
				assertTrue(currAutomaton.hasAcceptingState());
				assertTrue(currAutomaton.isAllEventsPrioritized());
				assertTrue(currAutomaton.isDeterministic());
				assertTrue(currAutomaton.nbrOfAcceptingStates() == 2);
				assertTrue(currAutomaton.nbrOfForbiddenStates() == 0);
				assertTrue(currAutomaton.nbrOfAcceptingAndForbiddenStates() == 0);
				assertTrue(currAutomaton.nbrOfControllableEvents() == 4);
				assertTrue(currAutomaton.nbrOfPrioritizedEvents() == 5);
				assertTrue(currAutomaton.nbrOfUnobservableEvents() == 0);
				assertTrue(currAutomaton.nbrOfImmediateEvents() == 0);
				assertTrue(currAutomaton.nbrOfEpsilonEvents() == 0);
				assertTrue(currAlphabet.nbrOfEvents() == 5);
				assertTrue(currAutomaton.equalAutomaton(currAutomaton));
				for (EventIterator alphIt = currAlphabet.iterator(); alphIt.hasNext();)
				{
					LabeledEvent currEvent = alphIt.nextEvent();
					String currLabel = currEvent.getLabel();
					assertTrue(currLabel != null);
					if (currLabel.equals("take"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("load"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("put"))
					{
						assertTrue(!currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("unload_A"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("unload_B"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else
					{
						assertTrue(false);
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}


	public void testUMDES_2()
	{
		try
		{

			ProjectBuildFromFSM builder = new ProjectBuildFromFSM();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.UMDES_2).toURL());
			assertTrue(theProject.nbrOfAutomata() == 1);
			assertTrue(theProject.isDeterministic());
			assertTrue(theProject.hasAcceptingState());
			assertTrue(theProject.isAllEventsPrioritized());
			assertTrue(theProject.isEventControllabilityConsistent());
			assertTrue(theProject.hasSelfLoop());

			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				Alphabet currAlphabet = currAutomaton.getAlphabet();
				String autName = currAutomaton.getName();
				assertTrue(autName != null);
				assertTrue(currAlphabet != null);

				assertTrue(currAutomaton.getType() == AutomatonType.Specification);
				assertTrue(currAutomaton.nbrOfStates() == 3);
				assertTrue(currAutomaton.nbrOfEvents() == 5);
				assertTrue(currAutomaton.nbrOfTransitions() == 5);
				assertTrue(currAutomaton.hasInitialState());
				assertTrue(currAutomaton.hasSelfLoop());
				assertTrue(currAutomaton.hasAcceptingState());
				assertTrue(currAutomaton.isAllEventsPrioritized());
				assertTrue(!currAutomaton.isAllEventsObservable());
				assertTrue(currAutomaton.isDeterministic());
				assertTrue(currAutomaton.nbrOfAcceptingStates() == 1);
				assertTrue(currAutomaton.nbrOfForbiddenStates() == 0);
				assertTrue(currAutomaton.nbrOfAcceptingAndForbiddenStates() == 0);
				assertTrue(currAutomaton.nbrOfControllableEvents() == 3);
				assertTrue(currAutomaton.nbrOfPrioritizedEvents() == 5);
				assertTrue(currAutomaton.nbrOfUnobservableEvents() == 2);
				assertTrue(currAutomaton.nbrOfImmediateEvents() == 0);
				assertTrue(currAutomaton.nbrOfEpsilonEvents() == 0);
				assertTrue(currAlphabet.nbrOfEvents() == 5);
				assertTrue(currAutomaton.equalAutomaton(currAutomaton));
				for (EventIterator alphIt = currAlphabet.iterator(); alphIt.hasNext();)
				{
					LabeledEvent currEvent = alphIt.nextEvent();
					String currLabel = currEvent.getLabel();
					assertTrue(currLabel != null);
					if (currLabel.equals("put1"))
					{
						assertTrue(!currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(!currEvent.isObservable());
					}
					else if (currLabel.equals("put2"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(!currEvent.isObservable());
					}
					else if (currLabel.equals("put3"))
					{
						assertTrue(!currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("load"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else if (currLabel.equals("unload_A"))
					{
						assertTrue(currEvent.isControllable());
						assertTrue(currEvent.isPrioritized());
						assertTrue(!currEvent.isImmediate());
						assertTrue(!currEvent.isEpsilon());
						assertTrue(currEvent.isObservable());
					}
					else
					{
						assertTrue(false);
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}


}

