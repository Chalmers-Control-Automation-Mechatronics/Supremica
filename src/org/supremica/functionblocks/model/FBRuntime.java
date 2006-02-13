/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, It is freely available without fee for education,
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
 * Licen see may not use the name, logo, or any other symbol
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
/*
 * @author cengic
 */
package org.supremica.functionblocks.model;

import java.io.File;

public class FBRuntime
{

    public static void main(String[] args)
    {
		//System.out.println("Number of args: " + args.length);
		//for(int i = 0; i < args.length; i++)
		//{
		//    System.out.println("  arg[" + i + "]: " + args[i] );
		//}
                
		String systemFileName = null;
		String libraryPathBase = null;
		String libraryPath = null;
		int threads = 1;

		if (args.length == 0)
		{
			System.err.println("Usage: FBRuntime [-t num] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			return;
		}

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-t"))
			{
				if (i + 1 < args.length)
				{
					threads =  new Integer(args[i + 1]).intValue();
				}
			}
			if (args[i].equals("-lb"))
			{
				if (i + 1 < args.length)
				{
					libraryPathBase = args[i + 1];
				}
			}
			if (args[i].equals("-lp"))
			{
				if (i + 1 < args.length)
				{
					if (libraryPath == null)
					{
						libraryPath = args[i + 1];
					}
					else
					{
						libraryPath = libraryPath + File.pathSeparator + args[i + 1];
					}
				}
			}
			if (i == args.length-1)
			{
				systemFileName = args[i];
			}
			
		}
				
		System.out.println("Input arguments: system file name: " + systemFileName + " , library path base: " + libraryPathBase + " , library path: " + libraryPath + " , number of threads: " + threads);

		Device theDevice = new Device("FBRuntime Device", systemFileName, libraryPathBase, libraryPath, threads);
		theDevice.run();
		System.out.println("FBRuntime.main(): Exiting.");
		System.exit(0);
    }

}
