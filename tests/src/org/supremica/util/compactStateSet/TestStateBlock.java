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
package org.supremica.util.compactStateSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestStateBlock
	extends TestCase
{

	public TestStateBlock(String name)
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
		TestSuite suite = new TestSuite(TestStateBlock.class);
		return suite;
	}


	public void testStateBlock()
	{
		int stateLength = 2;
		int nbrOfStates = 4;
		StateCompressor compressor = new DefaultStateCompressor(stateLength);
		StateBlock block = new StateBlock(compressor, nbrOfStates);
		int[] currState = new int[stateLength];
		int[] pos0State = new int[stateLength];
		int[] pos1State = new int[stateLength];
		int[] posLastState = new int[stateLength];

		for (int i = 0; i < nbrOfStates; i++)
		{
			//System.err.println("Building state: " + i);
			for (int j = 0; j < stateLength; j++)
			{
				currState[j] = i+j;
			}
			if (i == 0)
			{
				System.arraycopy(currState, 0, pos0State, 0, pos0State.length);
			}
			else if (i == 1)
			{
				System.arraycopy(currState, 0, pos1State, 0, pos1State.length);
			}
			else if (i == nbrOfStates - 1)
			{
				System.arraycopy(currState, 0, posLastState, 0, posLastState.length);
			}
			assertTrue(block.add(currState) == i);

			//System.err.println(block.toString());
		}
		assertTrue(block.add(currState) == -1);

		int[] pos0StateCopy = new int[stateLength];
		int[] pos1StateCopy = new int[stateLength];
		int[] posLastStateCopy = new int[stateLength];

//		System.err.println("0: ");
//		print(pos0State);

		block.get(0, pos0StateCopy);

//		print(pos0StateCopy);
//		System.err.println(isEqual(pos0State, pos0StateCopy));
		assertTrue(isEqual(pos0State, pos0StateCopy));

//		System.err.println("1:");

		block.get(1, pos1StateCopy);
		assertTrue(isEqual(pos1State, pos1StateCopy));


//		System.err.println("Last:");

		block.get(nbrOfStates - 1, posLastStateCopy);
		assertTrue(isEqual(posLastState, posLastStateCopy));
	}

	boolean isEqual(int[] first, int[] second)
	{
		if (first == null || second == null)
		{
			return false;
		}
		if (first.length != second.length)
		{
			return false;
		}
		for (int i = 0; i < first.length; i++)
		{
			if (first[i] != second[i])
			{
				return false;
			}
		}
		return true;
	}

	void print(int[] first)
	{
		if (first == null)
		{
			System.err.println("null");
		}

		System.err.print("[");
		for (int i = 0; i < first.length; i++)
		{
			System.err.print(first[i] + " ");
		}
		System.err.print("]");
	}

}
