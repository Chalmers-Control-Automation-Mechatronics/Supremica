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
import java.util.Iterator;
import java.util.Calendar;

class ModelMaker
{

    public static void main(String args[])
    {
        String outputFileName = null;
        String systemFileName = null;
        List<File> libraryPathList = new LinkedList<File>();
        Map<String,String> arguments = new HashMap<String,String>();
        String libraryPathBase = null;
        String libraryPath = null;

        Logger.output("ModelMaker  Copyright (C) " + Calendar.getInstance().get(Calendar.YEAR) + " Goran Cengic");
        Logger.output("");

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-d"))
            {
				Logger.setVerboseLevel(Logger.DEBUG);
            }
            if (args[i].equals("-q"))
            {
				Logger.setVerboseLevel(Logger.QUIET);
            }
            if (args[i].equals("-e"))
            {
				arguments.put("expandTransitions","true");
            }
            if (args[i].equals("-p"))
            {
				arguments.put("generatePlantModels","false");
            }
            if (args[i].equals("-m"))
            {
                if (i + 1 < args.length)
                {
					if (args[i + 1].equals("d"))
					{
						arguments.put("execModel", "dual");
					}
					else if (args[i + 1].equals("s"))
					{
						arguments.put("execModel", "seq");
					}
                }
            }
            if (args[i].equals("-im"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("intVarMinValue", args[i + 1]);
                }
            }
            if (args[i].equals("-ix"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("intVarMaxValue", args[i + 1]);
                }
            }
            if (args[i].equals("-ip"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("instanceQueuePlaces", args[i + 1]);
                }
            }
            if (args[i].equals("-jp"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("jobQueuePlaces", args[i + 1]);
                }
            }
            if (args[i].equals("-ep"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("eventQueuePlaces", args[i + 1]);
                }
            }
            if (args[i].equals("-o"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("outputFileName", args[i + 1]);
                }
            }
            if (args[i].equals("-lb"))
            {
                if (i + 1 < args.length)
                {
                    arguments.put("libraryPathBase", args[i + 1]);
                }
            }
            if (args[i].equals("-lp"))
            {
                if (i + 1 < args.length)
                {
                    if (arguments.get("libraryPath") == null)
                    {
                        arguments.put("libraryPath", args[i + 1]);
                    }
                    else
                    {
                        arguments.put("libraryPath", arguments.get("libraryPath") + File.pathSeparator + args[i + 1]);
                    }
                }
            }
            if (args[i].charAt(0) != '-')
            {
                arguments.put("systemFileName", args[i]);
                if (arguments.get("outputFileName") == null)
                {
                    arguments.put("outputFileName", arguments.get("systemFileName") + ".wmod");
                }
            }

        }
		
        if (arguments.get("systemFileName") == null)
        {
			Logger.output(Logger.ERROR, "ERROR: No system file specified!");
			Logger.output(Logger.ERROR);
            Logger.output(Logger.ERROR, "Usage: ModelMaker [-d] [-q] [-p] [-e] [ [-m 'd' [-im int] [-ix int] [-ip int] [-jp int] [-ep int]] | [-m 's'] ] [-o outputFileName] [-lb libraryPathBase] [-lp libraryDirectory]... systemFile");
            return;
        }
		
        Logger.output(Logger.DEBUG, "ModelMaker.main(): Input arguments:");
		for (Iterator iter = arguments.keySet().iterator(); iter.hasNext();)
		{
			String curKey = (String) iter.next();
			String curValue = arguments.get(curKey);
			Logger.output(Logger.DEBUG, curKey + " = " + curValue, 1);
		}
		Logger.output(Logger.DEBUG);

		new ModelMaker(arguments);

    }

	ModelMaker(Map<String,String> arguments)
	{
		makeModel(arguments);
	}

	void makeModel(Map<String,String> arguments)
	{
		ModelBuilder theBuilder;

		if(arguments.get("execModel") == null || arguments.get("execModel").equals("dual"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the dual execution model.");
			theBuilder = new DualExecModelBuilder(arguments);
		}
		else
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the sequential execution model.");
			theBuilder = new SequentialExecModelBuilder(arguments);
		}
			
		Logger.output("ModelMaker.makeModel(): Loading the System -------------------------------------");
		theBuilder.loadSystem();

		Logger.output("ModelMaker.makeModel(): Analyzing the System -----------------------------------");
		theBuilder.analyzeSystem();

		Logger.output("ModelMaker.makeModel(): Generating Model ---------------------------------------");
		theBuilder.buildModels();

		Logger.output("ModelMaker.makeModel(): Writing Model ------------------------------------------");
		theBuilder.writeResult();

		Logger.output("ModelMaker.makeModel(): Done ---------------------------------------------------");
		
	}

}
