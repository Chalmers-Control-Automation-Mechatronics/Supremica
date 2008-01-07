/*
 * Copyright (C) 2007 Goran Cengic
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.external.iec61499fb2efa;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.sourceforge.fuber.xsd.libraryelement.JaxbSystem;
import net.sourceforge.fuber.xsd.libraryelement.JaxbFBType;

class ModelMaker
{

	public static void main(String args[])
    {
		String outputFileName = null;
		String systemFileName = null;
		List<File> libraryPathList = new LinkedList();
		Properties options = new Properties();
		String libraryPathBase = null;
		String libraryPath = null;

		if (args.length == 0)
		{
			Logger.output(Logger.ERROR, "Usage: ModelMaker [-qdpe] [-m 's'|'d'] [-o outputFileName] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			return;
		}

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-e"))
			{
				//expandTransitions = true;
			}
			if (args[i].equals("-p"))
			{
				//generatePlantModels = false;
			}
			if (args[i].equals("-d"))
			{
				//verboseLevel = DEBUG;
			}
			if (args[i].equals("-q"))
			{
				//verboseLevel = QUIET;
			}
			if (args[i].equals("-m"))
			{
				if (i + 1 < args.length)
				{
					//execModel = args[i + 1];
				}
			}
			if (args[i].equals("-ix"))
			{
				if (i + 1 < args.length)
				{
					//intVarMaxValue = (new Integer(args[i + 1])).intValue();
				}
			}
			if (args[i].equals("-im"))
			{
				if (i + 1 < args.length)
				{
					//intVarMinValue = (new Integer(args[i + 1])).intValue();
				}
			}
			if (args[i].equals("-ip"))
			{
				if (i + 1 < args.length)
				{
					//instanceQueuePlaces = new Integer(args[i + 1]);
				}
			}
			if (args[i].equals("-jp"))
			{
				if (i + 1 < args.length)
				{
					//jobQueuePlaces = new Integer(args[i + 1]);
				}
			}
			if (args[i].equals("-ep"))
			{
				if (i + 1 < args.length)
				{
					//eventQueuePlaces = new Integer(args[i + 1]);
				}
			}
			if (args[i].equals("-o"))
			{
				if (i + 1 < args.length)
				{
					//outputFileName = args[i + 1];
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
				if (outputFileName == null)
				{
					outputFileName = systemFileName + ".wmod";
				}
			}
			
		}

		Logger.output("ModelMaker  Copyright (C) 2008  Goran Cengic");
		Logger.output("");
		Logger.output("Input arguments: \n" 
			   + "\t output file: " + outputFileName + "\n"
			   + "\t system file: " + systemFileName + "\n"
			   + "\t library path base: " + libraryPathBase + "\n"
			   + "\t library path: " + libraryPath + "\n");
	}
}
