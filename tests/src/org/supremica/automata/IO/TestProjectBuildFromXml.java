
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

import java.io.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;

public class TestProjectBuildFromXml
	extends TestCase
{
	public TestProjectBuildFromXml(String name)
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
		TestSuite suite = new TestSuite(TestProjectBuildFromXml.class);
		return suite;
	}

	public void testEx45b()
	{
		try
		{

			ProjectBuildFromXml builder = new ProjectBuildFromXml();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Ex4_5_b));
			assertTrue(theProject.nbrOfAutomata() == 3);
			assertTrue(theProject.getName().equals("Ex4_5"));
			assertTrue(theProject.isDeterministic());
			assertTrue(theProject.hasAcceptingState());
			assertTrue(theProject.isAllEventsPrioritized());
			assertTrue(theProject.isEventControllabilityConsistent());
			assertTrue(!theProject.hasSelfLoop());

			Automaton specAutomaton = null;
			Automaton robotAutomaton = null;
			Automaton machineAutomaton = null;

			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				Alphabet currAlphabet = currAutomaton.getAlphabet();
				String autName = currAutomaton.getName();
				assertTrue(autName != null);
				assertTrue(currAlphabet != null);
				if (autName.equals("Spec"))
				{
					specAutomaton = currAutomaton;
					assertTrue(currAutomaton.getType() == AutomatonType.Specification);
					assertTrue(currAutomaton.nbrOfStates() == 3);
					assertTrue(currAutomaton.nbrOfEvents() == 3);
					assertTrue(currAutomaton.nbrOfTransitions() == 3);
					assertTrue(currAutomaton.hasInitialState());
					assertTrue(!currAutomaton.hasSelfLoop());
					assertTrue(currAutomaton.hasAcceptingState());
					assertTrue(currAutomaton.isAllEventsPrioritized());
					assertTrue(currAutomaton.isDeterministic());
					assertTrue(currAutomaton.nbrOfAcceptingStates() == 1);
					assertTrue(currAutomaton.nbrOfForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfAcceptingAndForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfControllableEvents() == 2);
					assertTrue(currAutomaton.nbrOfPrioritizedEvents() == 3);
					assertTrue(currAutomaton.nbrOfImmediateEvents() == 0);
					assertTrue(currAutomaton.nbrOfEpsilonEvents() == 0);
					assertTrue(currAlphabet.nbrOfEvents() == 3);
					assertTrue(currAutomaton.equalAutomaton(currAutomaton));
					for (EventIterator alphIt = currAlphabet.iterator(); alphIt.hasNext();)
					{
						LabeledEvent currEvent = alphIt.nextEvent();
						String currLabel = currEvent.getLabel();
						assertTrue(currLabel != null);
						if (currLabel.equals("b"))
						{
							assertTrue(!currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else if (currLabel.equals("c"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else if (currLabel.equals("d"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else
						{
							assertTrue(false);
						}
					}
				}
				else if (autName.equals("Robot"))
				{
					robotAutomaton = currAutomaton;
					assertTrue(currAutomaton.getType() == AutomatonType.Plant);
					assertTrue(currAutomaton.nbrOfStates() == 2);
					assertTrue(currAutomaton.nbrOfEvents() == 2);
					assertTrue(currAutomaton.nbrOfTransitions() == 2);
					assertTrue(currAutomaton.hasInitialState());
					assertTrue(!currAutomaton.hasSelfLoop());
					assertTrue(currAutomaton.hasAcceptingState());
					assertTrue(currAutomaton.isAllEventsPrioritized());
					assertTrue(currAutomaton.isDeterministic());
					assertTrue(currAutomaton.nbrOfAcceptingStates() == 2);
					assertTrue(currAutomaton.nbrOfForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfAcceptingAndForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfControllableEvents() == 1);
					assertTrue(currAutomaton.nbrOfPrioritizedEvents() == 2);
					assertTrue(currAutomaton.nbrOfImmediateEvents() == 0);
					assertTrue(currAutomaton.nbrOfEpsilonEvents() == 0);
					assertTrue(currAlphabet.nbrOfEvents() == 2);
					assertTrue(currAutomaton.equalAutomaton(currAutomaton));
					for (EventIterator alphIt = currAlphabet.iterator(); alphIt.hasNext();)
					{
						LabeledEvent currEvent = alphIt.nextEvent();
						String currLabel = currEvent.getLabel();
						assertTrue(currLabel != null);
						if (currLabel.equals("a"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else if (currLabel.equals("b"))
						{
							assertTrue(!currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else
						{
							assertTrue(false);
						}
					}
				}
				else if (autName.equals("Machine"))
				{
					machineAutomaton = currAutomaton;
					assertTrue(currAutomaton.getType() == AutomatonType.Plant);
					assertTrue(currAutomaton.nbrOfStates() == 2);
					assertTrue(currAutomaton.nbrOfEvents() == 3);
					assertTrue(currAutomaton.nbrOfTransitions() == 3);
					assertTrue(currAutomaton.hasInitialState());
					assertTrue(!currAutomaton.hasSelfLoop());
					assertTrue(currAutomaton.hasAcceptingState());
					assertTrue(currAutomaton.isAllEventsPrioritized());
					assertTrue(currAutomaton.isDeterministic());
					assertTrue(currAutomaton.nbrOfAcceptingStates() == 2);
					assertTrue(currAutomaton.nbrOfForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfAcceptingAndForbiddenStates() == 0);
					assertTrue(currAutomaton.nbrOfControllableEvents() == 3);
					assertTrue(currAutomaton.nbrOfPrioritizedEvents() == 3);
					assertTrue(currAutomaton.nbrOfImmediateEvents() == 0);
					assertTrue(currAutomaton.nbrOfEpsilonEvents() == 0);
					assertTrue(currAlphabet.nbrOfEvents() == 3);
					assertTrue(currAutomaton.equalAutomaton(currAutomaton));
					for (EventIterator alphIt = currAlphabet.iterator(); alphIt.hasNext();)
					{
						LabeledEvent currEvent = alphIt.nextEvent();
						String currLabel = currEvent.getLabel();
						assertTrue(currLabel != null);
						if (currLabel.equals("c"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else if (currLabel.equals("d"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else if (currLabel.equals("e"))
						{
							assertTrue(currEvent.isControllable());
							assertTrue(currEvent.isPrioritized());
							assertTrue(!currEvent.isImmediate());
							assertTrue(!currEvent.isEpsilon());
						}
						else
						{
							assertTrue(false);
						}
					}
				}
				else
				{
					assertTrue(false);
				}
			}
			assertTrue(specAutomaton != null);
			assertTrue(robotAutomaton != null);
			assertTrue(machineAutomaton != null);
			assertTrue(!specAutomaton.equalAutomaton(robotAutomaton));
			assertTrue(!specAutomaton.equalAutomaton(machineAutomaton));
			assertTrue(!robotAutomaton.equalAutomaton(machineAutomaton));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

}

