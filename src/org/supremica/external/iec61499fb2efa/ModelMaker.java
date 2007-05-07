/*
 *   Copyright (C) 2006 Goran Cengic
 *
 *   This file is free software; you can redistribute it and/or
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.net.URI;
import java.lang.Exception;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ComponentKind;

import net.sourceforge.fuber.xsd.libraryelement.*;

class ModelMaker
{

    private JAXBContext iecContext;
    private Unmarshaller iecUnmarshaller;

	private String systemFileName;
	private String outputFileName;
	// File
    private List libraryPathList = new LinkedList();
	
	private JaxbSystem theSystem;
	private JaxbFBNetwork systemFBNetwork;
	// String name, String type name
	private Map functionBlocks = new HashMap();
	// String name, String type name
	private Map basicFunctionBlocks = new HashMap();
	// String name, JaxbFBType type object
	private Map fbTypes = new HashMap();

	// String fb name, Map event conn map eo->ei
	private Map eventConnectionMaps = new HashMap();
	// String fb name, Map data conn map do->di
	private Map dataConnectionMaps = new HashMap();

	private ExtendedAutomata automata;


	private ModelMaker(String outputFileName, String systemFileName, String libraryPathBase, String libraryPath) 
	{

		try
		{
			iecContext = JAXBContext.newInstance("net.sourceforge.fuber.xsd.libraryelement");
			iecUnmarshaller = iecContext.createUnmarshaller();
		}
		catch (Exception e)
		{
			System.err.println(e);
			System.exit(1);
		}

		this.outputFileName = outputFileName;
		this.systemFileName = systemFileName;


		// convert libraryPath string into list of Files
		if (libraryPath == null) // libraryPath is not specified
		{
			if (libraryPathBase == null)
			{
				libraryPathList = null;
			}
			else
			{
				File libraryPathBaseFile = new File(libraryPathBase);

				if (!libraryPathBaseFile.isDirectory())
				{
					System.err.println("ModelMaker(): Specified library base is not a directory!: " + libraryPathBaseFile.getName());
				}
				else if (!libraryPathBaseFile.exists())
				{
					System.err.println("ModelMaker(): Specified library base does not exist!: " + libraryPathBaseFile.getName());
				}
				else
				{
					libraryPathList.add(libraryPathBaseFile);
				}
			}
		}
		else // libraryPath is specified by the user
		{
		
			while (true)
			{
				
				File curLibraryDir;

				if (libraryPath.indexOf(File.pathSeparatorChar) == -1)
				{
					curLibraryDir = new File(libraryPathBase, libraryPath);
				}
				else
				{
					curLibraryDir = new File(libraryPathBase, libraryPath.substring(0,libraryPath.indexOf(File.pathSeparatorChar)));
				}

				if (!curLibraryDir.isDirectory())
				{
					System.err.println("ModelMaker(): Specified library path element " + curLibraryDir.getAbsolutePath() + " is not a directory!");
				}
				else if (!curLibraryDir.exists())
				{
					System.err.println("ModelMaker(): Specified library path element " + curLibraryDir.getAbsolutePath() + " does not exist!");
				}
				else
				{
					libraryPathList.add(curLibraryDir);
				}

				if (libraryPath.indexOf(File.pathSeparatorChar) == -1)
				{
					break;
				}

				libraryPath = libraryPath.substring(libraryPath.indexOf(File.pathSeparatorChar)+1);
			}
		}
	}


    // find the fileName in libraries and return the corresponding File
    private File getFile(String fileName)
    {
		File theFile = new File(fileName);

		if (libraryPathList != null)
		{
			for (Iterator iter = libraryPathList.iterator();iter.hasNext();)
			{
				File curLibraryDir = (File) iter.next();
				theFile = new File(curLibraryDir, fileName);
				//System.out.println("ModelMaker.getFile(" + fileName + "): Looking for file in " + theFile.toString());
				if (theFile.exists())
				{
					break;
				}
			}
		}

		if (!theFile.exists())
		{
			System.err.println("ModelMaker.getFile(" + fileName + "): The file " + fileName + " does not exist in the specified libraries...");
			if (libraryPathList != null)
			{
				for (Iterator iter = libraryPathList.iterator();iter.hasNext();)
				{
					System.err.println("\t" + ((File) iter.next()).getAbsolutePath() + File.separator);
				}
			}
			else
			{
				System.err.println("\t. (current directory)");
			}
			System.err.println();
			System.err.println("Usage: ModelMaker [-o outputFile] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			System.exit(1);

		}

		return theFile;
    }
	
    private void loadSystem(String fileName)
    {
	
		System.out.println("ModelMaker.loadSystem(" + fileName + "): Loading file " + fileName);
		
		File file = getFile(fileName);
		
		try
		{
			Object unmarshalledObject = iecUnmarshaller.unmarshal(file);
			if (unmarshalledObject instanceof JaxbSystem)
			{ 
				theSystem = (JaxbSystem) unmarshalledObject;
				if (theSystem.isSetDevice())
				{
					JaxbDevice theDevice = (JaxbDevice) theSystem.getDevice().get(0);
					if(theDevice.isSetResource())
					{
						JaxbResource theResource = (JaxbResource) theDevice.getResource().get(0);
						if (theResource.isSetFBNetwork())
						{
							systemFBNetwork = ((JaxbFBNetwork) theResource.getFBNetwork());
							if (systemFBNetwork.isSetFB())
							{
								for (Iterator fbIter = systemFBNetwork.getFB().iterator(); fbIter.hasNext();)
								{
									FB curFB = (FB) fbIter.next();
									String instanceName = curFB.getName();
									String typeName = curFB.getType();
									// get and load the FB type
									if (typeName.startsWith("E_SPLIT") || typeName.startsWith("E_MERGE"))
									{
										functionBlocks.put(instanceName,curFB.getType());								
									}
									else
									{
										loadFB(instanceName, curFB.getType() + ".fbt");
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

    private void loadFB(String instanceName, String fileName)
    {
	
		System.out.println("ModelMaker.loadFB(" + instanceName + ", " + fileName + "):");
		
		File file = getFile(fileName);
		
		try
		{
			Object unmarshalledObject = iecUnmarshaller.unmarshal(file);
			if (unmarshalledObject instanceof JaxbFBType)
			{
				JaxbFBType theType = (JaxbFBType) unmarshalledObject;
				String typeName = theType.getName();
				if (!fbTypes.keySet().contains(typeName))
				{
					System.out.println("\t Adding FB type " + typeName);
					fbTypes.put(typeName, theType);					
				}
				System.out.println("\t Adding FB " + instanceName);
				functionBlocks.put(instanceName,theType.getName());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private void makeBasicFBMap(JaxbFBNetwork fbNetwork, String parentName)
	{
		for (Iterator iter = fbNetwork.getFB().iterator(); iter.hasNext();)
		{
			
			String curBlock = ((FB) iter.next()).getName();
			String curTypeName = (String) functionBlocks.get(curBlock);
			JaxbFBType curType = (JaxbFBType) fbTypes.get(curTypeName);
			if (curType.isSetBasicFB() & !curType.isSetFBNetwork())
			{
				if (parentName == null)
				{
					basicFunctionBlocks.put(curBlock, curType);
				}
				else
				{
					basicFunctionBlocks.put(parentName + "." + curBlock, curType);					
				}
			}
			else if (!curType.isSetBasicFB() & curType.isSetFBNetwork())
			{
				makeBasicFBMap(curType.getFBNetwork(), curBlock);
			}
			else
			{
				System.out.println("ModelMake.makeBasicFBMap: Unsupported FB type: " + curTypeName);
				System.out.println("\t Neither a Basic FB nor Composite FB.");
				System.exit(1);
			}
		}
	}

	private void makeEventConnectionMap(JaxbFBNetwork fbNetwork, String parentName)
	{
		
	}

	private void makeDataConnectionMap(JaxbFBNetwork fbNetwork, String parentName)
	{

	}


	private void makeInstanceQueue()
	{
		ExtendedAutomaton instanceQueue = new ExtendedAutomaton("instanceQueue", automata);
		
		// the maximum number of FB instances in queue at the same time
		final int places = 5;
		
	
		instanceQueue.addState("s0", true);
		for (int i = 1; i <= places; i++)
		{
			instanceQueue.addState("s" + i);
			//Transiton when queueing instance
			String from = "s" + (i-1);
			String to = "s" + i;
			String event = "";
//         \ForAll{$b \in B$}
// 		\State{$bID \gets$ nameOf($b$)}
// 		\State{$event \gets event +$ {\rm "queue\_fb\_$bID$;"}}
//         \EndFor
// 		\State{$guard\gets$ "queueing\_fb$>$0"}
//         \For{$j \gets 1,i-1$}
// 		\State{$guard\gets guard$ + " \& queueing\_fb != fb\_place\_$j$"}
//         \EndFor
// 		\State{$action \gets$ "fb\_place\_$i$ := queuing\_fb;"}
//         \State{$action \gets action$ + "queuing\_fb := 0;"}
//         \State{addTransition($from$, $to$, $event$, $guard$, $action$)}
//         \State{$\triangleright$ Transiton when dequeueing instance}
//         \State{$from \gets s_i$}
//         \State{$to \gets s_{i-1}$}
//         \State{$event \gets$ "remove\_fb;"}      
//         \State{$guard \gets$ ""}      
//         \State{$action\gets$ "current\_fb := fb\_place\_1;"}
//         \For{$j \gets 1,i-1$}
// 		\State{$action\gets action$ + "fb\_place\_$j$ := fb\_place\_$(j+1)$;"}
//         \EndFor
// 		\State{$action\gets action$ + "fb\_place\_$i$ := 0;"}
//         \State{addTransition($from$, $to$, $event$, $guard$, $action$)}       
		}
		
	}

	private void makeEventExecution()
	{
		ExtendedAutomaton eventExecution = new ExtendedAutomaton("eventExecution", automata);
		
	}

	private void makeJobQueue()
	{
		ExtendedAutomaton jobQueue = new ExtendedAutomaton("jobQueue", automata);
		
		// the maximum number of jobs in queue at the same time
		final int places = 5;
	}

	private void makeAlgorithmExecution()
	{
		ExtendedAutomaton algorithmExecution = new ExtendedAutomaton("algorithmExecution", automata);
				
	}

	private void makeBasicFB(String fbName, JaxbFBType fbType)
	{
				
	}

	private void makeMerge(int size)
	{
		System.out.println("Making Merge of size: " + size);
	}
	
	private void makeSplit(int size)
	{
		System.out.println("Making Split of size: " + size);
	}


	
	public void makeModel()
	{
		
		loadSystem(systemFileName);

		makeBasicFBMap(systemFBNetwork, null);
		
		makeEventConnectionMap(systemFBNetwork, null);

		makeDataConnectionMap(systemFBNetwork, null);

 		automata = new ExtendedAutomata(theSystem.getName());

		makeInstanceQueue();

		makeEventExecution();
		
		makeJobQueue();

		makeAlgorithmExecution();

		//for each basic FB instance make models
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			JaxbFBType fbType = (JaxbFBType) basicFunctionBlocks.get(fbName);
			String typeName = fbType.getName();
			if (fbType != null)
			{
				if (fbType.isSetBasicFB())
				{
					makeBasicFB(fbName, fbType);
				}
				else if (typeName.equals("E_RESTART"))
				{
					
				}
				else
				{
					System.err.println("ModelMaker.makeModel(): Unsupported FB type: " + functionBlocks.get(fbName));
					System.err.println("\t Info: The type in neither Basic nor Composite FB type.");
					System.exit(1);
				}
			}
			else
			{
				if (typeName.startsWith("E_SPLIT"))
				{
					makeSplit((new Integer(((String) functionBlocks.get(fbName)).substring(7))).intValue());
				}
				else if (typeName.startsWith("E_MERGE"))
				{
					makeMerge((new Integer(((String) functionBlocks.get(fbName)).substring(7))).intValue());
				}
				else
				{
					System.err.println("ModelMaker.makeModel(): Unsupported FB type: " + functionBlocks.get(fbName));
					System.err.println("\t Info: The type in neither Basic nor Composite FB type.");
					System.exit(1);
				}
			}
		}
		
		
// 		// test automata classes
// 		ExtendedAutomaton test = new ExtendedAutomaton("test", automata);
// 		test.addState("s0", true);
// 		test.addState("s1");
// 		test.addIntegerVariable("var1", 0, 5, 0, null);
// 		test.addTransition("s0","s1","e1;e2;","var1 == 1","var1  = 4;");
// 		automata.addAutomaton(test);
// 		ExtendedAutomaton test2 = new ExtendedAutomaton("test2", automata);
// 		test2.addState("s0", true);
// 		test2.addState("s1");
// 		test2.addIntegerVariable("var1", 0, 5, 0, null);
// 		test2.addTransition("s0","s1","e1;e2;","var1 == 1","var1  = 4;");
// 		automata.addAutomaton(test2);

		automata.writeToFile(new File(outputFileName));
	}




	public static void main(String args[])
    {
		String outputFileName = null;
		String systemFileName = null;
		String libraryPathBase = null;
		String libraryPath = null;

		if (args.length == 0)
		{
			System.err.println("Usage: ModelMaker [-o outputFileName] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			return;
		}

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-o"))
			{
				if (i + 1 < args.length)
				{
					outputFileName = args[i + 1];
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
		
		System.out.println("Input arguments: \n" 
						   + "\t output name: " + outputFileName + "\n"
						   + "\t system name: " + systemFileName + "\n"
						   + "\t library path base: " + libraryPathBase + "\n"
						   + "\t library path: " + libraryPath + "\n");
		
		(new ModelMaker(outputFileName,systemFileName,libraryPathBase,libraryPath)).makeModel();
		System.exit(0);

	}

}
