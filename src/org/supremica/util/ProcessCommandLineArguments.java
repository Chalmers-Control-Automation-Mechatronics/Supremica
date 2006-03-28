
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

import org.supremica.properties.SupremicaProperties;

import java.lang.Exception;
import java.io.*;

public class ProcessCommandLineArguments
{
	public static void process(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-h") || args[i].equals("-?") || args[i].equals("--help") || args[i].equals("--usage"))
			{
				ProcessCommandLineArguments.printUsage();
			}
			else if (args[i].equals("-p") || args[i].equals("--properties"))
			{
				if (i + 1 < args.length)
				{
					String fileName = args[i + 1];
					i++;
					File propFile = new File(fileName);

					try
					{
						if (!propFile.exists())
						{
							System.err.println("Properties file not found: " + propFile.getAbsolutePath());
							System.err.println("Creating empty properties file: " + propFile.getAbsolutePath());
							propFile.createNewFile();
						}

						SupremicaProperties.loadProperties(propFile);
					}
					catch (Exception e)
					{
						System.err.println("Error reading properties file: " + propFile.getAbsolutePath());
					}
				}
			}
			else if (args[i].equals("-l") || args[i].equals("--list"))
			{
				System.out.println(SupremicaProperties.getProperties());
			}
			else
			{
				System.out.println("Invalid usage.");
				ProcessCommandLineArguments.printUsage();
			}
		}
	}

	private static void printUsage()
	{
		System.out.println("Supremica: " + org.supremica.Version.version());
		System.out.println("More information about Supremica is available at www.supremica.org\n");
		System.out.println("Usage: Supremica [OPTION]\n");
		System.out.println("Property options: \n  -p, --properties FILE\t Load properties from FILE");
		System.out.println("List: \n  -l FILE, --list\t List properties with current values");
		System.out.println("Help options: \n  -?, -h, --help --usage\t show this help message");
	}
}
