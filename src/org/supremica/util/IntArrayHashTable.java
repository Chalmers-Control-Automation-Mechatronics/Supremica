
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

import org.supremica.automata.AutomataIndexFormHelper;
import java.util.*;

/**
 * Insert <int_1, int_2, int_3, ..., int_n, prev_state, int_status>
 */
public final class IntArrayHashTable
{
	private int[][] theTable;
	private int size;
	private float loadFactor;
	private boolean doExpand = true;
	private int maxSize;

	public IntArrayHashTable(int capacity)
	{
		this(capacity, 0.75f);
	}

	public IntArrayHashTable(int capacity, boolean doExpand)
	{
		this(capacity);

		this.doExpand = doExpand;
	}

	public IntArrayHashTable(int capacity, float loadFactor)
	{
		if ((capacity <= 0) || (loadFactor <= 0.0))
		{
			throw new IllegalArgumentException();
		}

		this.loadFactor = loadFactor;

		if (capacity % 2 == 0)
		{
			capacity += 1;
		}

		if (capacity < Integer.MAX_VALUE)
		{
			theTable = new int[capacity][];
			maxSize = (int) (capacity * loadFactor);
		}
	}

	// Try to add theArray to the table.
	// If there already exists an entry with the
	// first theArray.length-1 first equals then return
	// that entry. Otherwise add theArray
	// and return null.
	public int[] add(int[] theArray)
		throws Exception
	{
		int table[][] = theTable;
		int hash = hashCodeIntArray(theArray);
		int index = getTableIndex(hash);

		while (true)
		{
			if (table[index] != null)
			{
				if (equalsIntArray(theArray, table[index]))
				{
					return null;
				}

				index = (index + 1) % table.length;
			}
			else
			{
				int[] newArray = AutomataIndexFormHelper.createCopyOfState(theArray);

				table[index] = newArray;

				size++;

				if (size >= maxSize)
				{
					if (doExpand)
					{
						expandTable();
					}
					else
					{
						throw new Exception("HashTable is nearly full");
					}
				}

				return newArray;
			}
		}
	}

	public void clear()
	{
		int table[][] = theTable;

		for (int i = 0; i < table.length; i++)
		{
			table[i] = null;
		}

		size = 0;
	}

	// Returns an int[] if it exists otherwise null
	public int[] get(int[] theArray)
	{
		int table[][] = theTable;
		int hash = hashCodeIntArray(theArray);
		int index = getTableIndex(hash);

		while (true)
		{
			if (table[index] != null)
			{
				if (equalsIntArray(theArray, table[index]))
				{
					return table[index];
				}

				index = (index + 1) % table.length;
			}
			else
			{
				return null;
			}
		}
	}

	// Returns the index of theArray if it exists otherwise -1
	public int getIndex(int[] theArray)
	{
		int table[][] = theTable;
		int hash = hashCodeIntArray(theArray);
		int index = getTableIndex(hash);

		while (true)
		{
			if (table[index] != null)
			{
				if (equalsIntArray(theArray, table[index]))
				{
					return index;
				}

				index = (index + 1) % table.length;
			}
			else
			{
				return -1;
			}
		}
	}

/*
	private static int getIndex(int[][] theTable, int[] theArray)
	{
		return theTable.getIndex(theArray);
	}
*/

	private int getTableIndex(int hash)
	{
		return (hash & 0x7FFFFFFF) % theTable.length;
	}

	private void expandTable()
	{
		int[][] oldTable = theTable;
		int oldCapacity = theTable.length;
		int capacity = oldCapacity * 2 + 1;

		theTable = new int[capacity][];
		maxSize = (int) (capacity * loadFactor);

		// System.err.println("Rehashing oldCapcity: " + oldCapacity + " newCapacity: " + capacity);
		int[][] table = theTable;

		for (int i = 0; i < oldTable.length; i++)
		{
			if (oldTable[i] != null)
			{
				int[] theArray = oldTable[i];
				int hash = hashCodeIntArray(theArray);
				int index = getTableIndex(hash);

				while (true)
				{
					if (table[index] != null)
					{
						index = (index + 1) % capacity;
					}
					else
					{
						table[index] = theArray;

						break;
					}
				}
			}
		}

		// Adjust the prev state indicies
		for (int i = 0; i < oldTable.length; i++)
		{
			if (oldTable[i] != null)
			{
				int[] currState = oldTable[i];
				int prevStateIndex = currState[currState.length - AutomataIndexFormHelper.STATE_PREVSTATE_FROM_END];
				if (prevStateIndex != AutomataIndexFormHelper.STATE_NO_PREVSTATE)
				{
					int[] prevState = oldTable[prevStateIndex];
					int newPrevStateIndex = getIndex(prevState);
					AutomataIndexFormHelper.setPrevStateIndex(prevState, newPrevStateIndex);
				}
			}
		}
	}

	private static int hashCodeIntArray(int[] theArray)
	{    // Assume that the last element is a status field
		int hashCode = 1;

		for (int i = 0; i < theArray.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; i++)
		{
			hashCode = 127 * hashCode + 7 * theArray[i];
		}

		return hashCode;
	}

	public static boolean equalsIntArray(int[] firstArray, int[] secondArray)
	{    // Assume that the last element is a status field
		for (int i = 0; i < firstArray.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; i++)
		{
			if (firstArray[i] != secondArray[i])
			{
				return false;
			}
		}

		return true;
	}

	public int[][] getTable()
	{
		return theTable;
	}

	public Iterator iterator()
	{
		return new IntArrayHashTableIterator();
	}

	public String toString()
	{
		StringBuffer theString = new StringBuffer();
		int[][] table = theTable;

		theString.append("size: " + size + "\n");

		for (int i = 0; i < table.length; i++)
		{
			if (table[i] != null)
			{
				int[] theArray = table[i];

				theString.append(i);
				theString.append(" ");

				int optimalIndex = getTableIndex(hashCodeIntArray(theArray));

				theString.append(optimalIndex);

				if (i != optimalIndex)
				{
					theString.append("*");
				}
				else
				{
					theString.append(" ");
				}

				for (int j = 0; j < theArray.length; j++)
				{
					theString.append(" ");
					theString.append(theArray[j]);
				}

				theString.append("\n");
			}
		}

		return theString.toString();
	}

	public static void main(String[] args)
	{
		int size = 4;
		IntArrayHashTable theHashTable = new IntArrayHashTable(size);
		int[] a1 = { 3, 4, 5, 1, 2, 2, 5 };
		int[] a2 = { 7, 5, 6, 4, 1, 2, 5 };
		int[] a3 = { 3, 4, 6, 2, 3, 22, 5 };

		try
		{
			System.out.println("add a1: " + theHashTable.add(a1));
			System.out.println("add a2: " + theHashTable.add(a2));
			System.out.println("add a3: " + theHashTable.add(a3));
		}
		catch (Exception e)
		{
			System.err.println(e);
		}

		System.out.println("theHashTable: \n" + theHashTable);

		Iterator hashIt = theHashTable.iterator();

		while (hashIt.hasNext())
		{
			int[] currElement = (int[]) hashIt.next();

			System.out.println(IntArrayList.toString(currElement));
		}
	}

	private class IntArrayHashTableIterator
		implements Iterator
	{
		private int currIndex = -1;
		private int nextIndex = -1;

		public IntArrayHashTableIterator() {}

		public boolean hasNext()
		{
			if (nextIndex == -1)
			{
				nextIndex = currIndex + 1;

				int size = theTable.length;

				while ((nextIndex < size) && (theTable[nextIndex] == null))
				{
					nextIndex++;
				}

				if (nextIndex == size)
				{
					nextIndex = Integer.MAX_VALUE;

					return false;
				}
				else
				{
					return true;
				}
			}
			else
			{
				if (nextIndex == Integer.MAX_VALUE)
				{
					return false;
				}
				else
				{
					return true;
				}
			}
		}

		public Object next()
		{
			if (hasNext())
			{
				Object currObject = theTable[nextIndex];

				currIndex = nextIndex;
				nextIndex = -1;

				return currObject;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		/**
		 * Use this if you do not want
		 * to make a cast.
		 */
		public int[] nextIntArray()
		{
			if (hasNext())
			{
				int[] currObject = theTable[nextIndex];

				currIndex = nextIndex;
				nextIndex = -1;

				return currObject;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
