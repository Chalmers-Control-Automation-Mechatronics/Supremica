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
	// String name, Integer ID
	private Map basicFunctionBlocksID = new HashMap();
	private int fbIDCounter = 1;

	// String fb name, Map event input name -> event ID
	private Map events = new HashMap();
	// String fb name, max event ID
	private Map eventsMaxID = new HashMap();
	private int eventIDCounter = 1;

	// String fb name, Map alg name -> alg ID
	private Map algorithms = new HashMap();
	private int algIDCounter = 1;
	private int algIDCounterMax = 1;
	// String fb name, Map alg name -> JaxbAlgorithm
	private Map algorithmTexts = new HashMap();

	// String name, JaxbFBType type object
	private Map fbTypes = new HashMap();

	// String fb name, Map event conn map eo->ei
	private Map eventConnections = new HashMap();
	// String fb name, Map data conn map do->di
	private Map dataConnections = new HashMap();

	private String restartInstance = null;

	private ExtendedAutomata automata;

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
						   + "\t output file: " + outputFileName + "\n"
						   + "\t system file: " + systemFileName + "\n"
						   + "\t library path base: " + libraryPathBase + "\n"
						   + "\t library path: " + libraryPath + "\n");
		
		(new ModelMaker(outputFileName,systemFileName,libraryPathBase,libraryPath)).makeModel();
		System.exit(0);

	}

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
	
	public void makeModel()
	{

		System.out.println("ModelMaker.makeModel(): Loading and Analyzing System -----------------------------");
		
		loadSystem(systemFileName);
		
		makeEventConnectionMap(systemFBNetwork, null, 0);
		
		makeDataConnectionMap(systemFBNetwork, null, 0);

// 		printFunctionBlocksMap();
// 		printBasicFunctionBlocksMap();
// 		printAlgorithmsMap();
// 		printAlgorithmTextsMap();
// 		printFBTypesMap();
// 		printEventConnectionsMap();
// 		printDataConnectionsMap();

		System.out.println("ModelMaker.makeModel(): Generating Model -----------------------------------------");

 		automata = new ExtendedAutomata(theSystem.getName());

		makeInstanceQueue();

		makeEventExecution();
		
		makeJobQueue();

		makeAlgorithmExecution();

		//for each basic FB instance make models
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
// 			if (typeName.startsWith("E_SPLIT"))
// 			{
// 				makeSplit((new Integer(((String) functionBlocks.get(fbName)).substring(7))).intValue());
// 			}
// 			else if (typeName.startsWith("E_MERGE"))
// 			{
// 				makeMerge((new Integer(((String) functionBlocks.get(fbName)).substring(7))).intValue());
// 			}
// 			else
// 			{
				makeBasicFB(fbName);
//			}
		}
		
// 		// test automata classes
// 		ExtendedAutomaton test = new ExtendedAutomaton("test", automata);
// 		test.addState("s0", true);
// 		test.addState("s1");
// 		test.addIntegerVariable("var1", 0, 5, 0, null);
// 		automata.addEvent("e1", "controllable");
// 		test.addTransition("s0","s1","e1;e2;","var1 == 1","var1 = 4;");
// 		automata.addAutomaton(test);
// 		ExtendedAutomaton test2 = new ExtendedAutomaton("test2", automata);
// 		test2.addState("s0", true);
// 		test2.addState("s1");
// 		test2.addIntegerVariable("var1", 0, 5, 0, null);
// 		test2.addTransition("s0","s1","e1;e2;","var1 == 1","var1  = 4;");
// 		automata.addAutomaton(test2);

		automata.writeToFile(new File(outputFileName));
	}

    private void loadSystem(String fileName)
    {
	
		System.out.println("ModelMaker.loadSystem(" + fileName + "):");
		System.out.println("\t Loading file " + fileName);
		
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
									loadFB(curFB, null, null);
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

    private void loadFB(FB fb, FB parent, String parentName)
    {
		
		String instanceName = null;

		if (parent == null)
		{
			instanceName = fb.getName();
		}
		else
		{
			instanceName = parentName + "_" + fb.getName();		
		}

		String typeName = fb.getType();
		String fileName = typeName + ".fbt";
	
		System.out.println("ModelMaker.loadFB(" + instanceName + ", " + fileName + "):");
				
		if (typeName.equals("E_RESTART"))
		{
			System.out.println("\t Skipping built-in E_RESTART type.");
			System.out.println("\t Adding FB " + instanceName);
			restartInstance = instanceName;
			functionBlocks.put(instanceName,typeName);
		}
// 		else if (typeName.startsWith("E_MERGE"))
// 		{
// 			System.out.println("\t Skipping built-in E_MERGE type.");			
// 			System.out.println("\t Adding FB " + instanceName);
// 			functionBlocks.put(instanceName,typeName);
// 			System.out.println("\t Adding Basic FB " + instanceName);			
// 			basicFunctionBlocks.put(instanceName, typeName);
					
// 		}
// 		else if (typeName.startsWith("E_SPLIT"))
// 		{
// 			System.out.println("\t Skipping built-in E_SPLIT type.");			
// 			System.out.println("\t Adding FB " + instanceName);
// 			functionBlocks.put(instanceName,typeName);
// 			System.out.println("\t Adding Basic FB " + instanceName);			
// 			basicFunctionBlocks.put(instanceName, typeName);
// 		}
		else
		{
			
			File file = getFile(fileName);
			
			Object unmarshalledObject = null;
			
			try
			{
				unmarshalledObject = iecUnmarshaller.unmarshal(file);
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
				System.exit(1);
			}
			
			if (unmarshalledObject instanceof JaxbFBType)
			{
				JaxbFBType theType = (JaxbFBType) unmarshalledObject;

				if (!fbTypes.keySet().contains(typeName))
				{
					System.out.println("\t Adding FB type " + typeName);
					fbTypes.put(typeName, theType);					
				}

				System.out.println("\t Adding FB " + instanceName);
				functionBlocks.put(instanceName,theType.getName());
				
				if (theType.isSetBasicFB() && !theType.isSetFBNetwork())
				{
					System.out.println("\t Adding Basic FB " + instanceName);			

					basicFunctionBlocks.put(instanceName, typeName);
					basicFunctionBlocksID.put(instanceName, new Integer(fbIDCounter));
					fbIDCounter++;
					
					// make algoritms map entry
					BasicFB basicFB = theType.getBasicFB();
					if (basicFB.isSetAlgorithm())
					{
						List fbAlgs = (List) basicFB.getAlgorithm();
						Map algMap = new HashMap();
						Map algTextMap = new HashMap();
						for (Iterator iter = fbAlgs.iterator(); iter.hasNext();)
						{
							JaxbAlgorithm curAlg = (JaxbAlgorithm) iter.next();
							if (curAlg.isSetName())
							{
								String curAlgName = curAlg.getName();
								algMap.put(curAlgName, new Integer(algIDCounter));
								algIDCounter++;
								if (algIDCounterMax < algIDCounter) algIDCounterMax = algIDCounter;
								if (curAlg.isSetOther())
								{
									algTextMap.put(curAlgName, curAlg.getOther().getText());
								}
							}
							else
							{
								System.out.println("\t Error: The algorithm does not have a name!");
								System.exit(1);
							}
						}
						algIDCounter = 1;
						algorithms.put(instanceName, algMap);
						algorithmTexts.put(instanceName, algTextMap);
					}

					// make events map entry
					
				}
				else if (!theType.isSetBasicFB() && theType.isSetFBNetwork())
				{
					JaxbFBNetwork fbNetwork = theType.getFBNetwork();
					if (fbNetwork.isSetFB())
					{
						for (Iterator fbIter = fbNetwork.getFB().iterator(); fbIter.hasNext();)
						{
							FB curFB = (FB) fbIter.next();
							loadFB(curFB, fb, instanceName);
						}
					}
				}
				else
				{
					System.out.println("ModelMaker.loadFB(" + instanceName + ", " + fileName
									   + "): Unsupported FB type: " + typeName);
					System.out.println("\t Neither a Basic FB nor Composite FB.");
					System.exit(1);
				}
			}
		}
	}

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
			System.err.println("ModelMaker.getFile(" + fileName + "): The file " + fileName 
							   + " does not exist in the specified libraries...");
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
	
	private void makeEventConnectionMap(JaxbFBNetwork fbNetwork, String parentInstance, int level)
	{
		for (int i = 0; i<level; i++)
		{
			System.out.print("\t");
		}
		if (parentInstance == null)
		{
			System.out.println("ModelMaker.makeEventConnectionMap(System):");
		}
		else
		{
			System.out.println("ModelMaker.makeEventConnectionMap(" + parentInstance + "):");
		}
		for (Iterator connIter = fbNetwork.getEventConnections().getConnection().iterator();
			 connIter.hasNext();)
		{
			JaxbConnection connection = (JaxbConnection) connIter.next();
			String source = connection.getSource().replace(".", "_");
			String dest = connection.getDestination().replace(".", "_");			
			
			if (parentInstance != null)
			{
				// don't process internal event input connections in composite blocks...
				if (!getInstanceName(source).equals(""))
				{
					source = parentInstance + "_" + source;
					dest   = parentInstance + "_" + dest;			
				}
			}
			String sourceInstance = getInstanceName(source);
			String sourceSignal   = getSignalName(source);
				
// 			for (int i = 0; i<level; i++)
// 			{
// 				System.out.print("\t");
// 			}
// 			System.out.println("Analyzing connection: " 
// 							   + source
// 							   + "-->" 
// 							   + dest);
			
			JaxbFBType sourceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(sourceInstance));

			if (sourceType != null)
			{
				if (sourceType.isSetFBNetwork())
				{
					makeEventConnectionMap(sourceType.getFBNetwork(), sourceInstance, level + 1);
				}
			}
		
			// ...process them here
			if (sourceInstance.equals(""))
			{
				source = parentInstance + "_" + source;
				dest   = parentInstance + "_" + dest;			
			}
			
			// flattening
			source = getInternalEventOutputConnection(source);
			dest = getInternalEventInputConnection(dest);
			sourceInstance = getInstanceName(source);
			sourceSignal   = getSignalName(source);
		
// 			for (int i = 0; i<level; i++)
// 			{
// 				System.out.print("\t");
// 			}
// 			System.out.println("Adding connection: " 
// 							   + source
// 							   + "-->" 
// 							   + dest);
			
			Map eventMap;
			if (!eventConnections.keySet().contains(sourceInstance))
			{
				eventMap = new HashMap();
				eventConnections.put(sourceInstance, eventMap);
			}
			else
			{
				eventMap = (Map) eventConnections.get(sourceInstance);
			}
			eventMap.put(sourceSignal, dest);
		}
	}		

	private String getInternalEventInputConnection(String externalConnection)
	{
		String instanceName = getInstanceName(externalConnection);
		String signalName = getSignalName(externalConnection);
		JaxbFBType instanceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(instanceName));
		if (instanceType != null)
		{
			if (instanceType.isSetFBNetwork())
			{
				for (Iterator internalConnIter = instanceType.getFBNetwork().getEventConnections().getConnection().iterator(); 
					 internalConnIter.hasNext();)
				{
					JaxbConnection curConn = (JaxbConnection) internalConnIter.next();
					String source = curConn.getSource().replace(".","_");
					if (source.equals(signalName))
					{
						String destination = curConn.getDestination().replace(".","_");
						String destInstance = getInstanceName(destination);
						if (basicFunctionBlocks.keySet().contains(instanceName + "_" + destInstance))
						{
							return instanceName + "_" + destination;
						}
						else
						{
							return getInternalEventInputConnection(instanceName + "_" + destination);
						}
					}
				}
			}
		}
		return externalConnection;
	}

	private String getInternalEventOutputConnection(String externalConnection)
	{
		String instanceName = getInstanceName(externalConnection);
		String signalName = getSignalName(externalConnection);
		JaxbFBType instanceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(instanceName));
		if (instanceType != null)
		{
			if (instanceType.isSetFBNetwork())
			{
				for (Iterator internalConnIter = instanceType.getFBNetwork().getEventConnections().getConnection().iterator(); 
					 internalConnIter.hasNext();)
				{
					JaxbConnection curConn = (JaxbConnection) internalConnIter.next();
					String dest = curConn.getDestination().replace(".","_");
					if (dest.equals(signalName))
					{
						String source = curConn.getSource().replace(".","_");
						String sourceInstance = getInstanceName(source);
						if (basicFunctionBlocks.keySet().contains(instanceName + "_" + sourceInstance))
						{
							return instanceName + "_" + source;
						}
						else
						{
							return getInternalEventOutputConnection(instanceName + "_" + source);
						}
					}
				}
			}
		}
		return externalConnection;
	}

	private void makeDataConnectionMap(JaxbFBNetwork fbNetwork, String parentInstance, int level)
	{
		for (int i = 0; i<level; i++)
		{
			System.out.print("\t");
		}
		if (parentInstance == null)
		{
			System.out.println("ModelMaker.makeDataConnectionMap(System):");
		}
		else
		{
			System.out.println("ModelMaker.makeDataConnectionMap(" + parentInstance + "):");
		}
		for (Iterator connIter = fbNetwork.getDataConnections().getConnection().iterator();
			 connIter.hasNext();)
		{
			JaxbConnection connection = (JaxbConnection) connIter.next();
			String source = connection.getSource().replace(".","_");
			String dest = connection.getDestination().replace(".","_");			

			if (parentInstance != null)
			{
				// don't process internal event input connections in composite blocks...
				if (!getInstanceName(dest).equals(""))
				{
					source = parentInstance + "_" + source;
					dest   = parentInstance + "_" + dest;			
				}
			}

			String destInstance = getInstanceName(dest);
			String destSignal   = getSignalName(dest);
			
// 			System.out.println("Analyzing connection: " 
// 							   + source
// 							   + "-->" 
// 							   + dest);
			
			JaxbFBType destType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(destInstance));

			if (destType != null)
			{
				if (destType.isSetFBNetwork())
				{
					makeDataConnectionMap(destType.getFBNetwork(), destInstance, level + 1);
				}
			}
		
			// ...process them here
			if (destInstance.equals(""))
			{
				source = parentInstance + "_" + source;
				dest   = parentInstance + "_" + dest;			
			}
			
			// flattening
			source = getInternalDataOutputConnection(source);
			dest = getInternalDataInputConnection(dest);
			destInstance = getInstanceName(dest);
			destSignal   = getSignalName(dest);
		
// 			for (int i = 0; i<level; i++)
// 			{
// 				System.out.print("\t");
// 			}
// 			System.out.println("Adding connection: " 
// 							   + source
// 							   + "-->" 
// 							   + dest);
			
			Map dataMap;
			if (!dataConnections.keySet().contains(destInstance))
			{
				dataMap = new HashMap();
				dataConnections.put(destInstance, dataMap);
			}
			else
			{
				dataMap = (Map) dataConnections.get(destInstance);
			}
			dataMap.put(destSignal, source);
		}	
	}

	private String getInternalDataInputConnection(String externalConnection)
	{
		String instanceName = getInstanceName(externalConnection);
		String signalName = getSignalName(externalConnection);
		JaxbFBType instanceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(instanceName));
		if (instanceType != null)
		{
			if (instanceType.isSetFBNetwork())
			{
				for (Iterator internalConnIter = instanceType.getFBNetwork().getDataConnections().getConnection().iterator(); 
					 internalConnIter.hasNext();)
				{
					JaxbConnection curConn = (JaxbConnection) internalConnIter.next();
					String source = curConn.getSource().replace(".","_");
					if (source.equals(signalName))
					{
						String destination = curConn.getDestination().replace(".","_");
						String destInstance = getInstanceName(destination);
						if (basicFunctionBlocks.keySet().contains(instanceName + "_" + destInstance))
						{
							return instanceName + "_" + destination;
						}
						else
						{
							return getInternalDataInputConnection(instanceName + "_" + destination);
						}
					}
				}
			}
		}
		return externalConnection;
	}

	private String getInternalDataOutputConnection(String externalConnection)
	{
		String instanceName = getInstanceName(externalConnection);
		String signalName = getSignalName(externalConnection);
		JaxbFBType instanceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(instanceName));
		if (instanceType != null)
		{
			if (instanceType.isSetFBNetwork())
			{
				for (Iterator internalConnIter = instanceType.getFBNetwork().getDataConnections().getConnection().iterator(); 
					 internalConnIter.hasNext();)
				{
					JaxbConnection curConn = (JaxbConnection) internalConnIter.next();
					String dest = curConn.getDestination().replace(".","_");
					if (dest.equals(signalName))
					{
						String source = curConn.getSource().replace(".","_");
						String sourceInstance = getInstanceName(source);
						if (basicFunctionBlocks.keySet().contains(instanceName + "_" + sourceInstance))
						{
							return instanceName + "_" + source;
						}
						else
						{
							return getInternalDataOutputConnection(instanceName + "_" + source);
						}
					}
				}
			}
		}
		return externalConnection;
	}

	private String getInstanceName(String cntSpec)
	{
		if (cntSpec.indexOf("_") < 0)
		{
			return "";
		}
		return cntSpec.substring(0,cntSpec.lastIndexOf("_"));
	}

	private String getSignalName(String cntSpec)
	{
		if (cntSpec.indexOf("_") < 0)
		{
			return cntSpec;
		}
		return cntSpec.substring(cntSpec.lastIndexOf("_")+1,cntSpec.length());
	}

	private void makeInstanceQueue()
	{
		System.out.println("ModelMaker.makeInstanceQueue():");

		ExtendedAutomaton instanceQueue = new ExtendedAutomaton("Instance Queue", automata);
		
		// the maximum number of FB instances in queue at the same time
		final int places = basicFunctionBlocks.keySet().size();
		
		instanceQueue.addIntegerVariable("queuing_fb", 0, fbIDCounter - 1, 0, null);
		instanceQueue.addIntegerVariable("current_fb", 0, fbIDCounter - 1, 0, null);
		
		instanceQueue.addState("s0", true);
		for (int i = 1; i <= places; i++)
		{
			instanceQueue.addIntegerVariable("fb_place_" + i, 0, fbIDCounter - 1, 0, null);

			instanceQueue.addState("s" + i);
			//Transiton when queuing instance
			String from = "s" + (i-1);
			String to = "s" + i;
			String event = "";
			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String instanceName = (String) fbIter.next();
				event = event + "queue_fb_" + instanceName + ";";
			}

			String guard = "queuing_fb > 0";
			for (int j = 1; j <= (i-1); j++)
			{
				guard = guard + " & queuing_fb != fb_place_" + j;
			}
			String action = "fb_place_" + i + " = queuing_fb;";
			action = action + "queuing_fb = 0;";
			instanceQueue.addTransition(from, to, event, guard, action);
			// Transiton when dequeuing instance
			from = "s" + i;
			to = "s" + (i-1);
			event = "remove_fb;";      
			guard = "";      
			action = "current_fb = fb_place_1;";
			for (int j = 1; j <= i-1; j++)
			{
				action = action + "fb_place_" + j + " = fb_place_" + (j+1) + ";";
			}
			action = action + "fb_place_" + i + " = 0;";
			instanceQueue.addTransition(from, to, event, guard, action);      
		}
		automata.addAutomaton(instanceQueue);
	}

	private void makeEventExecution()
	{
		System.out.println("ModelMaker.makeEventExecution():");

		ExtendedAutomaton eventExecution = new ExtendedAutomaton("Event Execution", automata);

		eventExecution.addState("s0", true);
		eventExecution.addState("s1");
		eventExecution.addState("s2");
		
		eventExecution.addTransition("s0", "s1", "remove_fb;", null, null);	
		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();

			String event = "handle_event_" + instanceName + ";";
			String guard = "current_fb == " + (Integer) basicFunctionBlocksID.get(instanceName);
			eventExecution.addTransition("s1", "s2", event, guard, null);
			event = "handling_event_done_" + instanceName + ";";
			eventExecution.addTransition("s2", "s0", event, null, null);
		}
		automata.addAutomaton(eventExecution);
	}

	private void makeJobQueue()
	{
		System.out.println("ModelMaker.makeJobQueue():");

		ExtendedAutomaton jobQueue = new ExtendedAutomaton("Job Queue", automata);
		
		// the maximum number of jobs in queue at the same time
		final int places = basicFunctionBlocks.keySet().size();	
		
		jobQueue.addIntegerVariable("queuing_job_fb", 0, fbIDCounter - 1, 0, null);
		jobQueue.addIntegerVariable("queuing_job_alg", 0, algIDCounterMax - 1, 0, null);
		jobQueue.addIntegerVariable("current_job_fb", 0, fbIDCounter - 1, 0, null);
		jobQueue.addIntegerVariable("current_job_alg", 0, algIDCounterMax - 1, 0, null);

		jobQueue.addState("s0", true);
		for (int i = 1; i <= places; i++)
		{
			jobQueue.addIntegerVariable("job_fb_place_" + i, 0, fbIDCounter - 1, 0, null);
			jobQueue.addIntegerVariable("job_alg_place_" + i, 0, algIDCounterMax - 1, 0, null);

			jobQueue.addState("s" + i);
			//Transiton when queuing job
			String from = "s" + (i-1);
			String to = "s" + i;
			String event = "";
			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String instanceName = (String) fbIter.next();
				event = event + "queue_job_" + instanceName + ";";
			}
			String guard = "queuing_job_fb > 0 & queuing_job_alg > 0";
			String action = "job_fb_place_" + i + " = queuing_job_fb;";
			action = action + "job_alg_place_" + i + " = queuing_job_alg;";
			action = action + "queuing_job_fb = 0;";
			action = action + "queuing_job_alg = 0;";
			jobQueue.addTransition(from, to, event, guard, action);
			// Transiton when dequeuing job
			from = "s" + i;
			to = "s" + (i-1);
			event = "remove_job;";      
			guard = "";      
			action = "current_job_fb = job_fb_place_1;";
			action = action + "current_job_alg = job_alg_place_1;";
			for (int j = 1; j <= i-1; j++)
			{
				action = action + "job_fb_place_" + j + " = job_fb_place_" + (j+1) + ";";
				action = action + "job_alg_place_" + j + " = job_alg_place_" + (j+1) + ";";
			}
			action = action + "job_fb_place_" + i + " = 0;";
			action = action + "job_alg_place_" + i + " = 0;";
			jobQueue.addTransition(from, to, event, guard, action);      
		}
		automata.addAutomaton(jobQueue);	
	}

	private void makeAlgorithmExecution()
	{
		System.out.println("ModelMaker.makeAlgorithmExecution():");

		ExtendedAutomaton algorithmExecution = new ExtendedAutomaton("Algorithm Execution", automata);
				
		algorithmExecution.addState("s0", true);
		algorithmExecution.addState("s1");
		algorithmExecution.addState("s2");
		
		algorithmExecution.addTransition("s0", "s1", "remove_job;", null, null);	

		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();
			Integer instanceID = (Integer) basicFunctionBlocksID.get(instanceName);
			Map algorithmMap = (Map) algorithms.get(instanceName);
			if (algorithmMap != null)
			{
				for (Iterator algIter = algorithmMap.keySet().iterator();algIter.hasNext();)
				{
					String algName = (String) algIter.next();
					Integer algID = (Integer) algorithmMap.get(algName);
					String event = "execute_" + instanceName + "_" + algName + ";";
					String guard = "current_job_fb == " + instanceID;
					guard = guard + " & current_job_alg == " + algID;
					algorithmExecution.addTransition("s1", "s2", event, guard, null);
					event = "finished_execution_" + instanceName + "_" + algName + ";";
					algorithmExecution.addTransition("s2", "s0", event, null, null);
				}
			}
		}
		automata.addAutomaton(algorithmExecution);
	}

	private void makeBasicFB(String fbName)
	{	
		System.out.println("ModelMaker.makeBasicFB(" + fbName + "):");
	
		makeBasicFBEventReceiving(fbName);
		makeBasicFBEventHandling(fbName);
		makeBasicFBEventQueue(fbName);
		makeBasicFBExecutionControlChart(fbName);
		makeBasicFBAlgorithms(fbName);
	}

	private void makeBasicFBEventReceiving(String fbName)
	{
		System.out.println("\t Event Receiving");

		Integer fbID = (Integer) basicFunctionBlocksID.get(fbName);

		ExtendedAutomaton eventReceiving = new ExtendedAutomaton("Event Receiving " + fbName , automata);
				
		eventReceiving.addState("s0", true);
		eventReceiving.addState("s1");
		eventReceiving.addState("s2");
		eventReceiving.addState("s3");
		eventReceiving.addState("s4");

		String from = "s0";
		String to = "s1";
		String event = "receive_event_" + fbName + ";";
		String action = "queuing_event_" + fbName + " = receiving_event_" + fbName + ";";
		eventReceiving.addTransition(from, to, event, null, action);

		from = "s1";
		to = "s2";
		event = "queue_event_" + fbName + ";";
		action = "queuing_fb = " + fbID + ";";
		eventReceiving.addTransition(from, to, event, null, action);
		
		from = "s2";
		to = "s3";
		event = "queue_fb_" + fbName + ";";
		eventReceiving.addTransition(from, to, event, null, null);

		from = "s3";
		to = "s0";
		event = "handle_event_" + fbName + ";";
		eventReceiving.addTransition(from, to, event, null, null);

		from = "s3";
		to = "s4";
		event = "receive_event_" + fbName + ";";
		action = "queuing_event_" + fbName + " = receiving_event_" + fbName + ";";
		eventReceiving.addTransition(from, to, event, null, action);

		from = "s4";
		to = "s3";
		event = "queue_event_" + fbName + ";";
		eventReceiving.addTransition(from, to, event, null, null);

		automata.addAutomaton(eventReceiving);
	}
	
	private void makeBasicFBEventHandling(String fbName)
	{
		System.out.println("\t Event Handling");
		
	}

	private void makeBasicFBEventQueue(String fbName)
	{
		System.out.println("\t Event Queue");

	}

	private void makeBasicFBExecutionControlChart(String fbName)
	{
		System.out.println("\t Execution Control Chart");
		
	}

	private void makeBasicFBAlgorithms(String fbName)
	{
		System.out.println("\t Algorithms");
				
	}


	private void makeMerge(int size)
	{
		System.out.println("Making Merge of size: " + size);
	}
	
	private void makeSplit(int size)
	{
		System.out.println("Making Split of size: " + size);
	}

	private void printFunctionBlocksMap()
	{
		System.out.println("ModelMaker.printFunctionBlocksMap():");
		for (Iterator iter = functionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			String curType  = (String) functionBlocks.get(curBlock);
			System.out.println("\t " + curBlock + "\t" + curType);
		}
	}

	private void printBasicFunctionBlocksMap()
	{
		System.out.println("ModelMaker.printBasicFunctionBlocksMap():");
		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			String curType  = (String) basicFunctionBlocks.get(curBlock);
			Integer curID = (Integer) basicFunctionBlocksID.get(curBlock);
			System.out.println("\t " + curBlock + "\t" + curType + "\t" + curID);
		}
	}	

	private void printEventsMap()
	{

		// TODO: Do it!!!!!

		System.out.println("ModelMaker.printEventsMap():");
		for (Iterator iter = algorithms.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curAlgMap  = (Map) algorithms.get(curBlock);
			System.out.println("\t " + curBlock);
			for (Iterator algIter = curAlgMap.keySet().iterator(); algIter.hasNext();)
			{
				String curAlgName = (String) algIter.next();
				Integer curAlgID = (Integer) curAlgMap.get(curAlgName);
				System.out.println("\t\t " + curAlgName + "\t" + curAlgID);
			}
		}
	}	

	private void printAlgorithmsMap()
	{
		System.out.println("ModelMaker.printAlgorithmsMap():");
		for (Iterator iter = algorithms.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curAlgMap  = (Map) algorithms.get(curBlock);
			System.out.println("\t " + curBlock);
			for (Iterator algIter = curAlgMap.keySet().iterator(); algIter.hasNext();)
			{
				String curAlgName = (String) algIter.next();
				Integer curAlgID = (Integer) curAlgMap.get(curAlgName);
				System.out.println("\t\t " + curAlgName + "\t" + curAlgID);
			}
		}
	}	

	private void printAlgorithmTextsMap()
	{
		System.out.println("ModelMaker.printAlgorithmTextsMap():");
		for (Iterator iter = algorithmTexts.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curAlgTextMap  = (Map) algorithmTexts.get(curBlock);
			System.out.println("\t " + curBlock);
			for (Iterator algIter = curAlgTextMap.keySet().iterator(); algIter.hasNext();)
			{
				String curAlgName = (String) algIter.next();
				String curAlgText = (String) curAlgTextMap.get(curAlgName);
				System.out.println("\t\t " + curAlgName + "\t" + curAlgText);
			}
		}
	}	

	private void printFBTypesMap()
	{
		System.out.println("ModelMaker.printFBTypesMap():");
		for (Iterator iter = fbTypes.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			JaxbFBType curType  = (JaxbFBType) fbTypes.get(curBlock);
			System.out.println("\t " + curBlock + "\t" + curType.getName());
		}
	}

	private void printEventConnectionsMap()
	{
		System.out.println("ModelMaker.printEventConnectionsMap():");
		for (Iterator fbIter = eventConnections.keySet().iterator(); fbIter.hasNext();)
		{
			String curBlock = (String) fbIter.next();
			Map curEvents = (Map) eventConnections.get(curBlock);
			System.out.println("\t " + curBlock);
			for (Iterator evIter = curEvents.keySet().iterator(); evIter.hasNext();)
			{
				String curEvent = (String) evIter.next();
				String curConnection  = (String) curEvents.get(curEvent);
				System.out.println("\t\t " + curEvent + " --> " + curConnection);
			}
		}
	}

	private void printDataConnectionsMap()
	{
		System.out.println("ModelMaker.printDataConnectionsMap():");
		for (Iterator fbIter = dataConnections.keySet().iterator(); fbIter.hasNext();)
		{
			String curBlock = (String) fbIter.next();
			Map curDatas = (Map) dataConnections.get(curBlock);
			System.out.println("\t " + curBlock);
			for (Iterator dataIter = curDatas.keySet().iterator(); dataIter.hasNext();)
			{
				String curData = (String) dataIter.next();
				String curConnection  = (String) curDatas.get(curData);
				System.out.println("\t\t " + curConnection + " --> " + curData);
			}
		}
	}
}
