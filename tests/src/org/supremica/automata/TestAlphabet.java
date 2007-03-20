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
import org.supremica.automata.algorithms.AutomataSynchronizer;
import java.util.Iterator;

public class TestAlphabet
	extends TestCase
{

	public TestAlphabet(String name)
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
		TestSuite suite = new TestSuite(TestAlphabet.class);
		return suite;
	}


	public void testSimpleOperations()
	{
		Alphabet alph1 = new Alphabet();
		assertTrue(alph1.size() == 0);
		{
			LabeledEvent e1 = new LabeledEvent("e1");
			alph1.addEvent(e1);
			assertTrue(alph1.size() == 1);
			LabeledEvent e2 = new LabeledEvent("e2");
			alph1.addEvent(e2);
			assertTrue(alph1.size() == 2);
			LabeledEvent e3 = new LabeledEvent("e3");
			alph1.addEvent(e3);
			assertTrue(alph1.size() == 3);
		}


		Alphabet alph2 = new Alphabet();
		assertTrue(alph2.size() == 0);
		{
			LabeledEvent e1 = new LabeledEvent("e1");
			alph2.addEvent(e1);
			assertTrue(alph2.size() == 1);
			LabeledEvent e2 = new LabeledEvent("e2");
			alph2.addEvent(e2);
			assertTrue(alph2.size() == 2);
			LabeledEvent e3 = new LabeledEvent("e3");
			alph2.addEvent(e3);
			assertTrue(alph2.size() == 3);
		}

		Alphabet alph3 = new Alphabet(alph1);
		assertTrue(alph3.size() == 3);
		alph3.union(alph2);
		assertTrue(alph3.size() == 3);
		alph3.intersect(alph2);
		assertTrue(alph3.size() == 3);
		alph3.minus(alph2);
		assertTrue(alph3.size() == 0);

		Alphabet alph4 = new Alphabet(alph1);
		{
			LabeledEvent e2 = alph4.getEvent("e2");
			alph4.removeEvent(e2);
			assertTrue(alph4.size() == 2);
			alph2.minus(alph4);
			assertTrue(alph2.size() == 1);
			alph2.union(alph1);
			assertTrue(alph2.size() == 3);
		}

		alph1.setIndices();
		int minIndex = 0;
		int maxIndex = alph1.size() - 1;
		for (Iterator<LabeledEvent> evIt = alph1.iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.next();
			int currIndex = currEvent.getSynchIndex();
			assertTrue(currIndex >= minIndex);
			assertTrue(currIndex <= maxIndex);
		}
	}

	public void testAlphabetEquality1()
	{
        Alphabet alph1 = new Alphabet();
        Alphabet alph2 = new Alphabet();
        assertTrue(alph1.equals(alph2));
        LabeledEvent e1 = new LabeledEvent("e1");
        LabeledEvent e2 = new LabeledEvent("e2");
        LabeledEvent e2_copy = new LabeledEvent(e2);        
        alph1.addEvent(e1);
        assertTrue(!alph1.equals(alph2));
        alph2.addEvent(e1);
        assertTrue(alph1.equals(alph2));
        alph1.addEvent(e2);
        alph2.addEvent(e2_copy);
        assertTrue(alph1.equals(alph2));
        LabeledEvent e3_1 = new LabeledEvent("e3");
        LabeledEvent e3_2 = new LabeledEvent("e3");      
        alph1.addEvent(e3_1);
        alph2.addEvent(e3_2);
        assertTrue(alph1.equals(alph2));
        Alphabet alph3 = new Alphabet();
        Alphabet alph4 = new Alphabet();
        LabeledEvent e1_1 = new LabeledEvent("e1");
        LabeledEvent e2_1 = new LabeledEvent("e2");
        alph3.addEvent(e1_1);
        alph3.addEvent(e2_1);
        LabeledEvent e1_2 = new LabeledEvent("e1");
        LabeledEvent e2_2 = new LabeledEvent("e2");  
        alph4.addEvent(e2_2);
        alph4.addEvent(e1_2);
        assertTrue(alph3.equals(alph4));
        alph4.addEvent(e3_1);
        assertTrue(!alph3.equals(alph4));        
    }    
    
	public void testAlphabetEquality2()
	{
		try
		{
			ProjectBuildFromXml builder = new ProjectBuildFromXml();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.AutomaticCarParkGate));

			// Test equality
			Automaton plant = AutomataSynchronizer.synchronizeAutomata(theProject.getPlantAutomata());
			Automaton spec = AutomataSynchronizer.synchronizeAutomata(theProject.getSpecificationAutomata());
            assertTrue(plant.getAlphabet() instanceof Alphabet);
            assertTrue(spec.getAlphabet() instanceof Alphabet);
//            System.err.println("spec: " + spec.getAlphabet().toString());
//            System.err.println("plant: " + plant.getAlphabet().toString());
            assertTrue(plant.getAlphabet().size() == spec.getAlphabet().size());
            assertTrue(plant.getAlphabet().equals(spec.getAlphabet()));

			// Test inequality
			spec = theProject.getAutomaton("Functional mode");
			assertTrue(!plant.getAlphabet().equals(spec.getAlphabet()));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}
}
