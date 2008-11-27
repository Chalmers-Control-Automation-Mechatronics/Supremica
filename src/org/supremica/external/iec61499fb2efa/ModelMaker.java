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
import java.util.Properties;

class ModelMaker
{
    private static Properties properties;

	private static final String helpString = "Usage: ModelMaker \n" + 
		"\t[-d (debug info)] \n" +
		"\t[-q (quiet)] \n" +
		"\t[-p (generate models as plants)] \n" +
		"\t[-im int (int var min)] \n" +
		"\t[-ix int (int var max)] \n" + 
		"\t[-m free (free buffered exec model (default)) | \n" +
		"\t-m dual (dual buffered exec model) | \n" +
		"\t-m seq (sequential buffered exec model) |\n" +
		"\t-m cycl (cyclic buffered exec model) |\n" +
		"\t-m hybrid (hybrid buffered exec model) |\n" +
		"\t-m npmtr (npmtr exec model)] \n" +
		"\t[-ip int (instance q places)] \n" +
		"\t[-ep int (event q places)] \n" +
		"\t[-jp int (job q places (deprecated))] \n" +
		"\t[-o outputFileName] \n" +
		"\t[-lb libraryPathBase] \n" +
		"\t[-lp libraryDirectory]... systemFile";

    public static void main(String args[])
    {
        properties = new Properties();
        new ModelMaker(args);
    }

    public static Properties getProperties()
    {
        return properties;
    }
	
	private ModelMaker(String[] args)
	{
		properties.setProperty("execModel", "free"); // default exec model
		
		// argument parsing ===================================================
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
				properties.setProperty("expandTransitions","true");
            }
            else if (args[i].equals("-p"))
            {
				properties.setProperty("generatePlantModels","false");
            }
            else if (args[i].equals("-m"))
            {
                if (i + 1 < args.length)
                {
                    properties.setProperty("execModel", args[i + 1]);
                }
            }
            else if (args[i].equals("-im"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("intVarMinValue", args[i + 1]);
                }
            }
            else if (args[i].equals("-ix"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("intVarMaxValue", args[i + 1]);
                }
            }
            else if (args[i].equals("-ip"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("instanceQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-jp"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("jobQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-ep"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("eventQueuePlaces", args[i + 1]);
                }
            }
            else if (args[i].equals("-o"))
            {
                if (i + 1 < args.length)
                {
					properties.setProperty("outputFileName", args[i + 1]);
                }
            }
            else if (args[i].equals("-lb"))
            {
                if (i + 1 < args.length)
                {
                    properties.setProperty("libraryPathBase", args[i + 1]);
                }
            }
            else if (args[i].equals("-lp"))
            {
                if (i + 1 < args.length)
                {
                    if (properties.getProperty("libraryPath") == null)
                    {
                        properties.setProperty("libraryPath", args[i + 1]);
                    }
                    else
                    {
                        properties.setProperty("libraryPath", properties.getProperty("libraryPath") + File.pathSeparator + args[i + 1]);
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
                properties.setProperty("systemFileName", args[i]);
                if (properties.getProperty("outputFileName") == null)
                {
                    properties.setProperty("outputFileName", properties.getProperty("systemFileName") + ".wmod");
                }
            }
        }

        // startup ============================================================
        String outputFileName = null;
        String systemFileName = null;
        String libraryPathBase = null;
        String libraryPath = null;
        List<File> libraryPathList = new LinkedList<File>();

		ModelBuilder theBuilder = null;

		Logger.output("ModelMaker  Copyright (C) " + Calendar.getInstance().get(Calendar.YEAR) + " Goran Cengic");
        Logger.output("");
		
        if (properties.getProperty("systemFileName") == null)
        {
			Logger.output(Logger.ERROR, "ERROR: No system file specified!");
			Logger.output(Logger.ERROR);
            Logger.output(Logger.ERROR, helpString);
			System.exit(1);;
        }
		
        Logger.output(Logger.DEBUG, "ModelMaker.main(): Input arguments:");
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();)
		{
			String curKey = (String) iter.next();
			String curValue = properties.getProperty(curKey);
			Logger.output(Logger.DEBUG, curKey + " = " + curValue, 1);
		}
		Logger.output(Logger.DEBUG);

		// Free event exec model (default), see above
		// events are chosen freely from all first events in the queus of all blocks
		if(properties.getProperty("execModel").equals("free"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the FREE EVENT execution model.");
			theBuilder = new FreeEventExecModelBuilder(properties);
		}
		// Free block exec model
		// blocks are chosen freely, all events are handled in a single run
		else if(properties.getProperty("execModel").equals("freeb"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the FREE BLOCK execution model.");
			theBuilder = new FreeBlockExecModelBuilder(properties);
		}
        // Sequential event exec model
		// one place in scheduler per fb event received
		else if (properties.getProperty("execModel").equals("seqe"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the SEQUENTIAL EVENT execution model.");
			theBuilder = new SequentialEventExecModelBuilder(properties);
		}
        // Sequential block exec model
		// one place in scheduler per all fb events received, all events are handled in a single run
		else if (properties.getProperty("execModel").equals("seqb"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the SEQUENTIAL BLOCK execution model.");
			theBuilder = new SequentialBlockExecModelBuilder(properties);
		}
        // Cyclic exec model: block are run cyclicaly, all events are handled in a single run
		else if (properties.getProperty("execModel").equals("cycl"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the CYCLIC execution model.");
			theBuilder = new CyclicExecModelBuilder(properties);
		}
        // Dual exec model, not fully working
		else if (properties.getProperty("execModel").equals("dual"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the DUAL execution model.");
			theBuilder = new DualExecModelBuilder(properties);
		}
        // NPMTR exec model, not working
		else if (properties.getProperty("execModel").equals("npmtr"))
		{
			Logger.output("ModelMaker.makeModel(): Making EFA model for the NPMTR execution model.");
			theBuilder = new NpmtrExecModelBuilder(properties);
		}
		else
		{
			Logger.output(Logger.ERROR, "ERROR: Unsupported execution model specified!");
			Logger.output(Logger.ERROR);
            Logger.output(Logger.ERROR, helpString);
			System.exit(1);;
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
