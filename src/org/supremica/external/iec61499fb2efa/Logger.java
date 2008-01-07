/*
 * Copyright (C) 2007 Goran Cengic
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.external.iec61499fb2efa;

class Logger
{

	public static final int ERROR = -2;
	public static final int WARN = -1;
	public static final int QUIET = 0;
	public static final int INFO = 1;
	public static final int DEBUG = 2;

	private static int verboseLevel = INFO;

	public static void setVerboseLevel(int level)
	{
		verboseLevel = level;
	}

	public static int getVerboseLevel()
	{
		return verboseLevel;
	}

	public static void output(String text)
	{
		output(INFO, text, 0);
	}

	public static void output(int verboseLevel, String text)
	{
		output(verboseLevel, text, 0);
	}

	public static void output(String text, int indentLevel)
	{
		output(INFO, text, indentLevel);
	}

	public static void output(int verboseLevel, String text, int indentLevel)
	{
		if (verboseLevel <= Logger.verboseLevel)
		{
			for (int i = 1; i <= indentLevel; i++)
			{
				System.out.print("\t");
			}
			System.out.println(text);
		}
	}
}
