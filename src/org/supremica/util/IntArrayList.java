
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
	private int currMinBlockIndex = -1;    // index of the first occupied entry
	private int currMaxBlockIndex = -1;    // index of the first free entry
	private int size = -1;
	private int maxSize = -1;
	private int[][] firstBlock = null;
	private int[][] lastBlock = null;
	private final LinkedList<int[][]> blocks = new LinkedList<int[][]>();

	public IntArrayList()
	{
		this(5 * 1024);
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
		addLast(theArray);
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

		// System.out.println("currMinBlockIndex: " + currMinBlockIndex);
		// System.out.println("currMaxBlockIndex: " + currMaxBlockIndex);
		// System.out.println("firstBlock: " + firstBlock);
		// System.out.println("lastBlock: " + lastBlock);
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
		{    // This is quick and dirty
			currArray = removeLast();

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

			firstBlock = blocks.getFirst();
		}

		size--;

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

			lastBlock = blocks.getLast();
		}

		int[] currArray = lastBlock[--currMaxBlockIndex];

		lastBlock[currMaxBlockIndex] = null;

		size--;

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

	public Iterator<?> iterator()
	{
		return new IntArrayListIterator();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		for (Iterator<?> it = iterator(); it.hasNext(); )
		{
			int[] currEntry = (int[]) it.next();

			sb.append(toString(currEntry));
			sb.append("\n");
		}

		return sb.toString();
	}

	public String blocksToString()
	{
		StringBuffer sb = new StringBuffer("blocks: \n");

		for (Iterator<int[][]> it = blocks.iterator(); it.hasNext(); )
		{
			int[][] currBlock = it.next();

			sb.append(toString(currBlock));
		}

		return sb.toString();
	}

	private static String toString(int[][] theArray)
	{
		StringBuffer sb = new StringBuffer();

		if (theArray == null)
		{
			sb.append("[\nnull\n]\n");

			return sb.toString();
		}

		sb.append("[\n");

		for (int i = 0; i < theArray.length; i++)
		{
			sb.append(toString(theArray[i]) + "\n");
		}

		sb.append("]\n");

		return sb.toString();
	}

	public static String toString(int[] theArray)
	{
		StringBuffer sb = new StringBuffer();

		if (theArray == null)
		{
			sb.append("[null]");

			return sb.toString();
		}

		sb.append("[");

		for (int i = 0; i < theArray.length; i++)
		{
			if (i != 0)
			{
				sb.append(" ");
			}

			sb.append(theArray[i]);
		}

		sb.append("]");

		return sb.toString();
	}

	private class IntArrayListIterator
		implements Iterator<Object>
	{
		private int[][] currBlock = null;
		private int currIndex = 0;
		private Iterator<int[][]> blockIterator = null;
		private int currSize;
		private int currElement;

		public IntArrayListIterator()
		{
			currIndex = currMinBlockIndex;
			blockIterator = blocks.iterator();
			currBlock = blockIterator.next();
			currSize = size();
			currElement = 0;
		}

		public boolean hasNext()
		{
			if (currElement < currSize)
			{
				return true;
			}

			return false;
		}

		public Object next()
		{
			Object theObject = (Object) currBlock[currIndex];

			currElement++;
			currIndex++;

			if (currIndex >= blockSize)
			{
				currIndex = 0;

				if (blockIterator.hasNext())
				{
					currBlock = blockIterator.next();
				}
				else
				{
					throw new NoSuchElementException();
				}
			}

			return theObject;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	public static void main(String[] args)
	{
		IntArrayList theList = new IntArrayList(3);
		int[] dummy0 = new int[]{ 0 };
		int[] dummy1 = new int[]{ 1 };
		int[] dummy2 = new int[]{ 2 };
		int[] dummy3 = new int[]{ 3 };

		theList.add(dummy0);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());

		// System.out.println(theList.blocksToString());
		theList.add(dummy1);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		theList.add(dummy2);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		theList.add(dummy3);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		System.out.println("new");
		theList.removeFirst();
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		theList.removeLast();
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		theList.addFirst(dummy0);
		theList.addLast(dummy3);
		System.out.println(IntArrayList.toString(theList.getFirst()));
		System.out.println(IntArrayList.toString(theList.getLast()));
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		theList.addFirst(dummy3);
		theList.addLast(dummy0);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
		theList.removeFirst();
		theList.removeLast();
		theList.removeFirst();
		theList.removeLast();
		theList.removeFirst();
		theList.removeLast();
		theList.add(dummy2);
		theList.add(dummy2);
		System.out.println("*** Size: " + theList.size() + "\n" + theList.toString());
		System.out.println(theList.blocksToString());
	}
}
