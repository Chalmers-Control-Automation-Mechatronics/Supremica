/*
 *   Copyright (C) 2005 Goran Cengic
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   To contact author please refer to contact information in the README file.
 */

/*
 * @author Goran Cengic
 */
package org.supremica.external.iec61499fb2efa;

import java.lang.System;
import java.lang.Math;

// load IEC 61499 application

// make instance queue model

// make event execution thread model

// make jobs queue model

// make algorithms execution thread model

class ModelMaker
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
				
		System.out.println("Input arguments: " 
						   + "system file name: " + systemFileName 
						   + " , library path base: " + libraryPathBase 
						   + " , library path: " + libraryPath 
						   + " , number of threads: " + threads);

		Device theDevice = new Device("FBRuntime Device", systemFileName, libraryPathBase, libraryPath, threads);

		theDevice.run();
		System.out.println("FBRuntime.main(): Exiting");
		System.exit(0);
	}
}
