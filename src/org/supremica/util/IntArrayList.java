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

package org.supremica.util;

import java.util.*;

/**
 * An efficient implementation of a list of int[].
 * It is only possible to insert and get/remove elements
 * from the ends of the list.
 * Note, that this class is not thread safe.
 */
public final class IntArrayList
{
	private final int blockSize;
 	private int currMinBlockIndex = -1; // index of the first occupied entry
 	private int currMaxBlockIndex = -1; // index of the first free entry
 	private int size = -1;
 	private int maxSize = -1;
 	private int[][] firstBlock = null;
 	private int[][] lastBlock = null;

 	private final LinkedList blocks = new LinkedList();

    public IntArrayList()
    {
		this(10*1024);
    }

    public IntArrayList(int blockSize)
    {
		this.blockSize = blockSize;
		firstBlock = new int[blockSize][];
		lastBlock = firstBlock;
		blocks.addFirst(firstBlock);
		currMinBlockIndex = 0;
		currMaxBlockIndex = 0;
		size = 0;
    }

	/**
	 * @param theArray the int[] to insert
	 */
	public void add(int[] theArray)
	{
		addFirst(theArray);
	}

	/**
	 * @param theArray the int[] to insert
	 */
	public void addFirst(int[] theArray)
	{
		if (currMinBlockIndex <= 0)
		{
			int[][] newBlock = new int[blockSize][];
			blocks.addFirst(newBlock);
			firstBlock = newBlock;
			currMinBlockIndex = blockSize;
		}
		firstBlock[--currMinBlockIndex] = theArray;
		size++;
		if (size > maxSize)
		{
			maxSize = size;
		}
	}

	/**
	 * @param theArray the int[] to insert
	 */
	public void addLast(int[] theArray)
	{
		lastBlock[currMaxBlockIndex++] = theArray;
		if (currMaxBlockIndex >= blockSize)
		{
			int[][] newBlock = new int[blockSize][];
			blocks.addLast(newBlock);
			lastBlock = newBlock;
			currMaxBlockIndex = 0;
		}
		size++;
		if (size > maxSize)
		{
			maxSize = size;
		}
	}

	/**
	 * This returns the first element if it exists, otherwise null
	 * The returned element is not removed.
	 * @return theArray if it exists, null otherwise
	 */
	public int[] getFirst()
	{
		if (size <= 0)
		{
			return null;
		}
		return firstBlock[currMinBlockIndex];
	}

	/**
	 * This returns the last element if it exists, otherwise null
	 * The returned element is not removed.
	 * @return theArray if it exists, null otherwise
	 */
	public int[] getLast()
	{
		if (size <= 0)
		{
			return null;
		}
		int[] currArray = null;
		if (currMaxBlockIndex <= 0)
		{ // This is quick and dirty
			currArray  = removeLast();
			addLast(currArray);
		}
		else
		{
			currArray = lastBlock[currMaxBlockIndex - 1];
		}
		return currArray;
	}

	/**
	 * This returns the first element if it exists, otherwise null
	 * The returned element is removed.
	 * @return theArray if it exists, null otherwise
	 */
	public int[] removeFirst()
	{
		if (size <= 0)
		{
			return null;
		}
		int[] currArray = firstBlock[currMinBlockIndex];
		firstBlock[currMinBlockIndex++] = null;
		if (currMinBlockIndex >= blockSize)
		{
			currMinBlockIndex = 0;
			blocks.removeFirst();
		}
		return currArray;
	}

	/**
	 * This returns the last element if it exists, otherwise null
	 * The returned element is removed.
	 * @return theArray if it exists, null otherwise
	 */
	public int[] removeLast()
	{
		if (size <= 0)
		{
			return null;
		}
		if (currMaxBlockIndex <= 0)
		{
			currMaxBlockIndex = blockSize;
			blocks.removeLast();
		}
		int[] currArray = lastBlock[--currMaxBlockIndex];
		lastBlock[currMaxBlockIndex] = null;
		return currArray;
	}


	/**
	 * @return Number of elements in the list.
	 */
	public int size()
	{
		return size;
	}

	public int maxSize()
	{
		return maxSize;
	}

	public Iterator iterator()
	{
		return new IntArrayListIterator();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator it = iterator(); it.hasNext();)
		{
			int[] currEntry = (int[]) it.next();
			sb.append(currEntry);
			sb.append("\n");
		}
		return sb.toString();
	}

	private class IntArrayListIterator
		implements Iterator
	{
		private int[][] currBlock = null;
		private int currIndex = 0;
		private Iterator blockIterator = null;

		public IntArrayListIterator()
		{
			currBlock = firstBlock;
			currIndex = currMinBlockIndex;
			blockIterator = blocks.iterator();
		}

		public boolean hasNext()
		{
			if (currIndex < currMaxBlockIndex)
			{
				return true;
			}
			if (currBlock != lastBlock)
			{
				return true;
			}
			return false;
		}

		public Object next()
		{
			currIndex++;
			if (currIndex >= blockSize)
			{
				currIndex = 0;
				if (blockIterator.hasNext())
				{
					currBlock = (int[][])blockIterator.next();
				}
				else
				{
					throw new NoSuchElementException();
				}
			}
			return (Object)currBlock[currIndex];
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}


}
