package org.supremica.util.BDD;

public class Counter
{
	public Counter(long l)
	{
		value = l;
	}

	public Counter()
	{
		this(0);
	}

	public long get()
	{
		return value;
	}

	public void increase()
	{
		value++;
	}

	private long value;
}
