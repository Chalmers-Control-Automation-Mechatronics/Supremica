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

		if (args.length == 1)
		{
			systemFileName = args[0];
		}
		else if (args.length == 2)
		{
			systemFileName = args[0];
			if (args[1].contains(File.pathSeparator))
			{
				libraryPath = args[1];
			}
			else
			{
				libraryPathBase = args[1];
			}
		}
		else if (args.length >= 3)
		{
			systemFileName = args[0];
			libraryPathBase = args[1];
			for(int i = 2; i < args.length; i++)
			{
				if (libraryPath == null)
				{
					libraryPath = args[i];
				}
				else
				{
					libraryPath = libraryPath + File.pathSeparator + args[i];
				}
			}   
		}
		else
		{
			System.err.println("Usage: FBRuntime file.sys [libraryPathBase] [libraryPathDirectory]...");
			return;
		}

		Device theDevice = new Device("FBRuntime Device", systemFileName, libraryPathBase, libraryPath);
		theDevice.run();
		System.out.println("FBRuntime.main(): Exiting.");
		System.exit(0);
    }

}
