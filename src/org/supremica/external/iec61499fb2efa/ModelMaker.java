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
						arguments.put("execModel", "freeseq");
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
            Logger.output(Logger.ERROR, "Usage: ModelMaker [-d (debug info)] [-q (quiet)] [-p (generate models as plants)] [-e (expand transitions (deprecated))] [-im int (int var min)] [-ix int (int var max)] [-m 'd' (dual exec model (deprecated)) |  -m 'f' (free seq exec model (default)) | -m 's' (seq exec model)] [-ip int (instance q places)] [-ep int (event q places)] [-jp int (job q places (deprecated))] [-o outputFileName] [-lb libraryPathBase] [-lp libraryDirectory]... systemFile");
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

		if(arguments.get("execModel") == null || arguments.get("execModel").equals("freeseq"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the free sequential execution model.");
			theBuilder = new FreeSequentialExecModelBuilder(arguments);
		}
		else if (arguments.get("execModel").equals("seq"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the sequential execution model.");
			theBuilder = new SequentialExecModelBuilder(arguments);
		}
		else
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the dual execution model.");
			theBuilder = new DualExecModelBuilder(arguments);
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
