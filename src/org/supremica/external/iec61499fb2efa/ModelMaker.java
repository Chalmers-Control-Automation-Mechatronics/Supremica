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
    private List<File> libraryPathList = new LinkedList<File>();
	
	private JaxbSystem theSystem;
	private JaxbFBNetwork systemFBNetwork;
	private Map<String,JaxbFBType> fbTypes = new HashMap<String,JaxbFBType>();
	private Map<String,String> functionBlocks = new HashMap<String,String>();

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
									// get and load the FB type
									if (curFB.getType().startsWith("E_SPLIT"))
									{
										//constructSplitType((new Integer(curFB.getType().substring(7))).intValue());
									}
									else if (curFB.getType().startsWith("E_MERGE"))
									{
										//constructMergeType((new Integer(curFB.getType().substring(7))).intValue());
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

// 	private void constructMergeType(int size)
// 	{
// 		resource.addBasicFBType("E_MERGE" + size);
		
// 		BasicFBType newBasicFBType = (BasicFBType) resource.getFBType("E_MERGE" + size);	

// 		for(int i=1; i<=size; i++)
// 		{
// 			newBasicFBType.addVariable("EI" + i, new BooleanVariable("EventInput",false));
// 		}
// 		newBasicFBType.addVariable("EO", new BooleanVariable("EventInput",false));

// 		newBasicFBType.getECC().addInitialState("S0");
// 		newBasicFBType.getECC().addState("S1");
// 		newBasicFBType.getECC().getState("S1").addAction(null, "EO");
// 		String condition = "";
// 		for(int i=1; i<=size; i++)
// 		{
// 			if (i==size)
// 			{
// 				condition = condition + "EI" + i;
// 			}
// 			else
// 			{
// 				condition = condition + "EI" + i + " OR ";
// 			}
// 		}
// 		newBasicFBType.getECC().addTransition("S0","S1",condition);
// 		newBasicFBType.getECC().addTransition("S1","S0","TRUE");
// 	}

// 	private void constructSplitType(int size)
// 	{
// 		resource.addBasicFBType("E_SPLIT" + size);
		
// 		BasicFBType newBasicFBType = (BasicFBType) resource.getFBType("E_SPLIT" + size);
		
// 		newBasicFBType.addVariable("EI", new BooleanVariable("EventInput",false));

// 		for(int i=1; i<=size; i++)
// 		{
// 			newBasicFBType.addVariable("EO" + i, new BooleanVariable("EventInput",false));
// 		}

// 		newBasicFBType.getECC().addInitialState("S0");
// 		newBasicFBType.getECC().addState("S1");
// 		for(int i=1; i<=size; i++)
// 		{
// 			newBasicFBType.getECC().getState("S0").addAction(null, "EO" + i);
// 		}
// 		newBasicFBType.getECC().addTransition("S0","S1","EI");
// 		newBasicFBType.getECC().addTransition("S1","S0","TRUE");
// 	}

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
	


	private void makeInstanceQueue()
	{
		
	}
	
	public void makeModel()
	{
		
		// load IEC 61499 application
		loadSystem(systemFileName);

		automata = new ExtendedAutomata(theSystem.getName());

		ExtendedAutomaton test = new ExtendedAutomaton("test", ComponentKind.PLANT);
		
		test.addState("blah");

		automata.add(test);

		automata.writeToFile(new File(outputFileName));

		// make instance queue model
		makeInstanceQueue();

		// TODO: make event execution thread model

		// TODO: make jobs queue model
		
		// TODO: make algorithms execution thread model	

		// TODO: for each FB instance make models
		
		// Write out the module file
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
