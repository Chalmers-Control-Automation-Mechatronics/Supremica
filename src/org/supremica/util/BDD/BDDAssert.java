package org.supremica.util.BDD;

public class BDDAssert
{
	public static void fatal(String s)
	{
		System.err.println("FATAL ERROR: " + s);

		try
		{
			Thread.sleep(5000);
		}
		catch (Exception exx) {}

		System.exit(20);
	}

	public static void warning(String s)
	{
		System.err.println("WARNING: " + s);
	}

	public static void debug(String s)
	{
		if (Options.debug_on)
		{
			System.err.println("DEBUG: " + s);
		}
	}

	public static void bddAssert(boolean condition, String msg)
		throws BDDException
	{
		if (!condition)
		{
			throw new BDDException(msg);
		}
	}

	/**
	 * This is similar to bddAssert, but it does not throw an exception
	 * but calls fatal()
	 */
	public static void internalCheck(boolean condition, String msg)
	{
		if (!condition)
		{
			fatal(msg);
		}
	}
}
