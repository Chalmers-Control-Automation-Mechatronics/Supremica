
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;



public class StateHolder
{

	private int[] theState;
	private int problemPlant = 0;
	private int problemEvent = 0;
	private boolean found = false;

	public StateHolder(int[] theState)
	{
		this.theState = theState;
	}

	public StateHolder(int[] theState, int problemPlant, int problemEvent)
	{

		this.theState = theState;
		this.problemPlant = problemPlant;
		this.problemEvent = problemEvent;
	}

	public int[] getArray()
	{
		return theState;
	}

	public int getProblemPlant()
	{
		return problemPlant;
	}

	public int getProblemEvent()
	{
		return problemEvent;
	}

	// ** MF ** Why isn't this code trivially optimised?
	public boolean equals(Object other)
	{

		boolean equal = true;
		int[] otherState = ((StateHolder) other).getArray();

		for (int i = 0; i < theState.length; i++)
		{
			if (theState[i] != otherState[i])
			{
				equal = false;
			}
		}

		return equal;
	}

	/** MF ** trivial optimization
	public boolean equals(Object other)
	{
																	int[] otherState = ((StateHolder)other).getArray();
																	for (int i = 0; i < theState.length; i++)
																																	if (theState[i] != otherState[i])
																																																	return false;
																	return true;
	}
	*/

	// Stolen and modified version of the hashCodeIntArray method in IntArrayHashTable
	public int hashCode()
	{

		int hashCode = 1;

		for (int i = 0; i < theState.length; i++)
		{
			hashCode = 127 * hashCode + 7 * theState[i];
		}

		return hashCode;
	}

	public void setFound(boolean found)
	{
		this.found = found;
	}

	public boolean isFound()
	{
		return found;
	}
}
