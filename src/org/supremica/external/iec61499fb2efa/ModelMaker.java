/*
 *   Copyright (C) 2006 Goran Cengic
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

import java.io.File;
import java.lang.Exception;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import net.sourceforge.fuber.xsd.libraryelement.*;

// TODO: load IEC 61499 application

// TODO: make instance queue model

// TODO: make event execution thread model

// TODO: make jobs queue model

// TODO: make algorithms execution thread model

class ModelMaker
{

    private List libraryPathList = new LinkedList();
    private JAXBContext context;
    private Unmarshaller unmarshaller;

	private JaxbSystem theSystem;
	

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

		System.exit(0);
	}
}
