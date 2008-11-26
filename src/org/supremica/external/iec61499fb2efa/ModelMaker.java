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

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;

class ModelMaker
{

	private static final String helpString = "Usage: ModelMaker \n" + 
		"\t[-d (debug info)] \n" +
		"\t[-q (quiet)] \n" +
		"\t[-p (generate models as plants)] \n" +
		"\t[-im int (int var min)] \n" +
		"\t[-ix int (int var max)] \n" + 
		"\t[-m f (free exec model (default)) | \n" +
		"\t-m d (dual exec model | \n" +
		"\t-m s (seqequential exec model)] \n" +
		"\t-m n (npmtr exec model)] \n" +
		"\t-m c (cyclic exec model)] \n" +
		"\t-m h (hybrid exec model)] \n" +
		"\t[-ip int (instance q places)] \n" +
		"\t[-ep int (event q places)] \n" +
		"\t[-jp int (job q places (deprecated))] \n" +
		"\t[-o outputFileName] \n" +
		"\t[-lb libraryPathBase] \n" +
		"\t[-lp libraryDirectory]... systemFile";

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
            else if (args[i].equals("-q"))
            {
				Logger.setVerboseLevel(Logger.QUIET);
            }
            else if (args[i].equals("-e"))
            {
				arguments.put("expandTransitions","true");
            }
            else if (args[i].equals("-p"))
            {
				arguments.put("generatePlantModels","false");
            }
            else if (args[i].equals("-m"))
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
					else if (args[i + 1].equals("f"))
					{
						arguments.put("execModel", "free");
					}
					else if (args[i + 1].equals("c"))
					{
						arguments.put("execModel", "cyclic");
					}
					else if (args[i + 1].equals("h"))
					{
						arguments.put("execModel", "hybrid");
					}
					else if (args[i + 1].equals("n"))
					{
						arguments.put("execModel", "npmtr");
					}
                }
            }
            else if (args[i].equals("-im"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("intVarMinValue", args[i + 1]);
                }
            }
            else if (args[i].equals("-ix"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("intVarMaxValue", args[i + 1]);
                }
            }
            else if (args[i].equals("-ip"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("instanceQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-jp"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("jobQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-ep"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("eventQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-o"))
            {
                if (i + 1 < args.length)
                {
					arguments.put("outputFileName", args[i + 1]);
                }
            }
            else if (args[i].equals("-lb"))
            {
                if (i + 1 < args.length)
                {
                    arguments.put("libraryPathBase", args[i + 1]);
                }
            }
            else if (args[i].equals("-lp"))
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
            else if (args[i].startsWith("-"))
            {
				Logger.output(Logger.WARN, "Warning: Unknown argument!: " + args[i]);
				Logger.output(Logger.WARN);
            }
            else if (i+1 == args.length)
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
            Logger.output(Logger.ERROR, helpString);
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
		ModelBuilder theBuilder = null;

		if(arguments.get("execModel") == null || arguments.get("execModel").equals("free"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the FREE execution model.");
			theBuilder = new FreeExecModelBuilder(arguments);
		}
        // Dual exec model  
		else if (arguments.get("execModel").equals("dual"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the DUAL execution model.");
			theBuilder = new DualExecModelBuilder(arguments);
		}
        // Sequential exec model (default): one place in scheduler per fb event received
		else if (arguments.get("execModel").equals("seq"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the SEQUENTIAL execution model.");
			theBuilder = new SequentialExecModelBuilder(arguments);
		}
        // Cyclic exec model: fb handles all fb events each run
		else if (arguments.get("execModel").equals("cyclic"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the CYCLIC execution model.");
			theBuilder = new CyclicExecModelBuilder(arguments);
		}
        // Hybrid exec model: one place in scheduler per all fb events received, handle all fb events per run
		else if (arguments.get("execModel").equals("hybrid"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the HYBRID execution model.");
			theBuilder = new HybridExecModelBuilder(arguments);
		}
        // NPMTR exec model
		else if (arguments.get("execModel").equals("npmtr"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the NPMTR execution model.");
			theBuilder = new NpmtrExecModelBuilder(arguments);
		}
		else
		{
			Logger.output(Logger.ERROR, "ERROR: Unsupported execution model specified!");
			Logger.output(Logger.ERROR);
            Logger.output(Logger.ERROR, helpString);
			System.exit(1);
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
