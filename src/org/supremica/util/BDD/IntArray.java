package org.supremica.util.BDD;

public class IntArray
{
	private static final int DEFAULT_SIZE = 64;
	private int size, capacity, min, max;
	private int[] array;

	public IntArray()
	{
		size = 0;
		array = null;
		min = max = 0;
	}

	public void add(int val)
	{
		if ((array == null) || (size == capacity))
		{
			resize();
		}

		if ((size == 0) || (val < min))
		{
			min = val;
		}

		if ((size == 0) || (val > max))
		{
			max = val;
		}

		array[size++] = val;
	}

	public int getSize()
	{
		return size;
	}


	public int get(int index)
	{
		return ((index >= 0) && (index < size))
			   ? array[index]
			   : 0;
	}

	public int [] getAll()
	{
		return array;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	public void clear()
	{
		size = 0;
	}


	private void resize()
	{
		if (array == null)
		{
			capacity = DEFAULT_SIZE;
			array = new int[capacity];
		}
		else
		{
			capacity = capacity * 2 + 1;

			int[] tmp = new int[capacity];

			if(size > 200)	// native method call is to slow for small values due to call overhead!
			{
				System.arraycopy(array, 0, tmp, 0, size);
			}
			else
			{
				for (int i = 0; i < size; i++)
				{
					tmp[i] = array[i];
				}
			}

			array = tmp;
		}
	}
}
