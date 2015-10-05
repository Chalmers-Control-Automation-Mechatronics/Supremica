/*
 *   Copyright (C) 2008 Goran Cengic
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 3 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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

	public static void output(int verboseLevel)
	{
		output(verboseLevel, "", 0);
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
