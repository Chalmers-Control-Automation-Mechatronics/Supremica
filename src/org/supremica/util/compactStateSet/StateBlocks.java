
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
package org.supremica.util.compactStateSet;

import java.util.*;

public final class StateBlocks
{
	private final StateCompressor compressor;
	private final int nbrOfStatesInEachBlock; // number of states in this block
	private int size = 0; // current number of states
	private ArrayList stateBlocks;
	private StateBlock currBlock;

	public StateBlocks(StateCompressor compressor, int nbrOfStatesInEachBlock)
	{
		this.compressor = compressor;
		this.nbrOfStatesInEachBlock = nbrOfStatesInEachBlock;
		stateBlocks = new ArrayList();
		currBlock = new StateBlock(compressor, nbrOfStatesInEachBlock);
		stateBlocks.add(currBlock);
	}

	public int add(int[] theState)
	{
		if (currBlock.size() >= nbrOfStatesInEachBlock)
		{
			currBlock = new StateBlock(compressor, nbrOfStatesInEachBlock);
			stateBlocks.add(currBlock);
		}
		currBlock.add(theState);

		return size++;
	}

	public void get(int position, int[] theState)
	{
		// First get the block
		int theBlockIndex = position / nbrOfStatesInEachBlock;
		int theBlockPosition = position % nbrOfStatesInEachBlock;
		StateBlock theBlock = (StateBlock)stateBlocks.get(theBlockIndex);
		theBlock.get(theBlockPosition, theState);
	}

	public int size()
	{
		return size;
	}

}
