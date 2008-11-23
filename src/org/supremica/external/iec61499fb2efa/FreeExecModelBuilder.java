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
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.lang.Exception;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java_cup.runtime.Scanner;
import net.sourceforge.fuber.model.interpreters.st.Lexer;
import net.sourceforge.fuber.model.interpreters.st.Parser;
import net.sourceforge.fuber.model.interpreters.Finder;
import net.sourceforge.fuber.model.interpreters.st.Translator;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Goal;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Identifier;

import net.sourceforge.fuber.xsd.libraryelement.*;

class FreeExecModelBuilder 
	extends SequentialExecModelBuilder
	implements ModelBuilder
{

	// input arguments
	private boolean expandTransitions = false;
	private boolean generatePlantModels = false;

	private Integer eventQueuePlaces = 0;
	private Integer instanceQueuePlaces = 0;
	private Integer jobQueuePlaces = 0;

	private int intVarMinValue = 0;
	private int intVarMaxValue = 2;

	private String systemFileName;
	private String outputFileName;
    private List<File> libraryPathList = new LinkedList();
	// end input arguments

    private JAXBContext iecContext;
    private Unmarshaller iecUnmarshaller;

	private JaxbSystem theSystem;
	private JaxbFBNetwork systemFBNetwork;

	// String name, String type name
	private Map functionBlocks = new HashMap();

	// String name, String type name
	private Map basicFunctionBlocks = new HashMap();
	// String name, Integer ID
	private Map basicFunctionBlocksID = new HashMap();
	// list to order the blocks with algorithms first
	// used to assign lowest block ids to blocks with algorithms
	private List basicFunctionBlocksList = new ArrayList();
	private int fbMaxID = 1;

	// String fb name, Map event input name -> event ID
	private Map events = new HashMap();
	// String fb name, max event ID
	private Map eventsMaxID = new HashMap();

	// String fb name, Map alg name -> alg ID
	private Map algorithms = new HashMap();
	private int algIDCounter = 1;
	private int algMaxID = 0;
	// number of blocks containing the algorithms
	private int algFB = 0;
	// String fb name, Map alg name -> JaxbAlgorithm
	private Map algorithmTexts = new HashMap();

	// String name, JaxbFBType type object
	private Map fbTypes = new HashMap();

	// String fb name, Map event conn map eo->ei
	private Map eventConnections = new HashMap();
	// String fb name, Map data conn map do->di
	private Map dataConnections = new HashMap();

	private String restartInstance = null;
	private String stopInstance = null;

	private ExtendedAutomata automata;

	// attributes used for ECC generation only
	private Map operatorMap = null;
	private int nameCounter = 0;
	private boolean doneInitActions = false;
	private boolean doneInitFinish = false;
	
	FreeExecModelBuilder(Map<String, String> arguments)
	{
		// get arguments
		if (arguments.get("expandTransitions") != null)
		{
			expandTransitions = (new Boolean(arguments.get("expandTransitions"))).booleanValue();
		}
		if (arguments.get("generatePlantModels") != null)
		{
			generatePlantModels = (new Boolean(arguments.get("generatePlantModels"))).booleanValue();
		}

		if (arguments.get("eventQueuePlaces") != null)
		{
			eventQueuePlaces = new Integer(arguments.get("eventQueuePlaces"));
		}	
		if (arguments.get("instanceQueuePlaces") != null)
		{
			instanceQueuePlaces = new Integer(arguments.get("instanceQueuePlaces"));
		}	
		if (arguments.get("jobQueuePlaces") != null)
		{
			jobQueuePlaces = new Integer(arguments.get("jobQueuePlaces"));
		}	

		if (arguments.get("intVarMinValue") != null)
		{
			intVarMinValue = (new Integer(arguments.get("intVarMinValue"))).intValue();
		}
		if (arguments.get("intVarMaxValue") != null)
		{
			intVarMaxValue = (new Integer(arguments.get("intVarMaxValue"))).intValue();
		}
		
		this.systemFileName = arguments.get("systemFileName");
		this.outputFileName = arguments.get("outputFileName");

		String libraryPathBase = arguments.get("libraryPathBase");
		String libraryPath = arguments.get("libraryPath");

		// convert libraryPath string into a list of Files
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
					Logger.output(Logger.ERROR, builderName() + "(): Specified library base is not a directory!: " + libraryPathBaseFile.getName());
				}
				else if (!libraryPathBaseFile.exists())
				{
					Logger.output(Logger.ERROR, builderName() + "(): Specified library base does not exist!: " + libraryPathBaseFile.getName());
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
					Logger.output(Logger.ERROR, builderName() + "(): Specified library path element " + curLibraryDir.getAbsolutePath() + " is not a directory!");
				}
				else if (!curLibraryDir.exists())
				{
					Logger.output(Logger.ERROR, builderName() + "(): Specified library path element " + curLibraryDir.getAbsolutePath() + " does not exist!");
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

		// make operator map for ST to EFA translation
		operatorMap = new HashMap();
		operatorMap.put("AND", "&");
		operatorMap.put("OR", "|");
		operatorMap.put("NOT", "!");
		operatorMap.put("=", "==");
		operatorMap.put("<>", "!=");
		operatorMap.put("MOD", "%");				

		// get unmarshaller for XML reading
		try
		{
			iecContext = JAXBContext.newInstance("net.sourceforge.fuber.xsd.libraryelement");
			iecUnmarshaller = iecContext.createUnmarshaller();
		}
		catch (Exception e)
		{
			Logger.output(Logger.ERROR, e.toString());
			exit(1);
		}
	}

	String builderName()
	{
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
	}

	public void loadSystem()
	{
		Logger.output(builderName() + ".loadSystem()");

		loadSystem(systemFileName);
	}

	public void analyzeSystem()
	{
		Logger.output(builderName() + ".analyzeSystem()");

		makeEventConnectionsMap(systemFBNetwork, null, 0);
		
		makeDataConnectionsMap(systemFBNetwork, null, 0);
		
		if (Logger.getVerboseLevel() <= Logger.DEBUG)
		{
			printFunctionBlocksMap();
			printBasicFunctionBlocksMap();
			printEventsMap();
			printAlgorithmsMap();
			printAlgorithmTextsMap();
			printFBTypesMap();
			printEventConnectionsMap();
			printDataConnectionsMap();
		}
	}
	
	public void buildModels()
	{
		Logger.output(builderName() + ".buildModels()");

 		automata = new ExtendedAutomata(theSystem.getName(), expandTransitions);
		
		makeStartup();
		
		//makeInstanceQueue();
		
		makeEventExecution();
		
		makeAlgorithmExecution();
		
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
			makeBasicFB(fbName);
		}
	}
	
	public void writeResult()
	{
		Logger.output(builderName() + ".writeResult()");
		automata.writeToFile(new File(outputFileName));
	}

    private void loadSystem(String fileName)
    {
	
		Logger.output(builderName() + ".loadSystem(" + fileName + "):");		
		File file = getFile(fileName);

		Logger.output("Loading file " + file.getName() + " from " + file.getParent(), 1);
		
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
			Logger.output(Logger.ERROR, e.toString());
			exit(1);
		}
		
		// Assign block IDs
		int counter = 1;
		for (Iterator iter = basicFunctionBlocksList.iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();
			basicFunctionBlocksID.put(instanceName, new Integer(counter));
			counter++;
		}
		fbMaxID = counter-1;
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
	
		Logger.output(builderName() + ".loadFB(" + instanceName + ", " + fileName + "):");
				
		if (typeName.equals("E_RESTART"))
		{
			Logger.output("Skipping built-in E_RESTART type.", 1);
			Logger.output("Adding FB " + instanceName, 1);
			restartInstance = instanceName;
			functionBlocks.put(instanceName,typeName);
		}
		else if (typeName.equals("E_STOP"))
		{
			Logger.output("Skipping built-in E_STOP type.", 1);
			Logger.output("Adding FB " + instanceName, 1);
			stopInstance = instanceName;
			functionBlocks.put(instanceName,typeName);
		}
		else
		{
			
			File file = getFile(fileName);
			
			Logger.output("Loading file " + file.getName() + " from " + file.getParent(), 1);

			Object unmarshalledObject = null;
			
			try
			{
				unmarshalledObject = iecUnmarshaller.unmarshal(file);
			}
			catch (Exception e)
			{
				Logger.output(Logger.ERROR, e.toString());
				exit(1);
			}
			
			if (unmarshalledObject instanceof JaxbFBType)
			{
				JaxbFBType theType = (JaxbFBType) unmarshalledObject;

				if (!fbTypes.keySet().contains(typeName))
				{
					Logger.output("Adding FB type " + typeName, 1);
					fbTypes.put(typeName, theType);					
				}

				Logger.output("Adding FB " + instanceName, 1);
				functionBlocks.put(instanceName,theType.getName());
				
				if (theType.isSetBasicFB() && !theType.isSetFBNetwork())
				{
					Logger.output("Adding Basic FB " + instanceName, 1);

					basicFunctionBlocks.put(instanceName, typeName);

					// make events map entry
					List eventInputList = (List) ((EventInputs) ((InterfaceList) theType.getInterfaceList()).getEventInputs()).getEvent();
					int eventIDCounter = 1;
					Map eventIDMap = new HashMap();
					for (Iterator eventIter = eventInputList.iterator(); eventIter.hasNext();)
					{
						JaxbEvent curEvent = (JaxbEvent) eventIter.next();

						eventIDMap.put(curEvent.getName(), new Integer(eventIDCounter));
						
						if (!eventIter.hasNext())
						{
							eventsMaxID.put(instanceName, new Integer(eventIDCounter));
						}
						eventIDCounter++;
					}
					events.put(instanceName, eventIDMap);

					// make algoritms map entry
					BasicFB basicFB = theType.getBasicFB();
					if (basicFB.isSetAlgorithm())
					{

						// blocks with algorithms go first in the list
						basicFunctionBlocksList.add(0,instanceName);
						algFB++;

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
								if (curAlg.isSetOther())
								{
									algTextMap.put(curAlgName, curAlg.getOther().getText());
								}
								if (!iter.hasNext())
								{
									if (algIDCounter > algMaxID)
									{
										algMaxID = algIDCounter;
									}
								}
								algIDCounter++;
							}
							else
							{
								Logger.output(Logger.ERROR, "Error: The algorithm does not have a name!");
								Logger.output(Logger.ERROR, "FB name: " + instanceName, 1);
								exit(1);
							}
						}
						algIDCounter = 1;
						algorithms.put(instanceName, algMap);
						algorithmTexts.put(instanceName, algTextMap);
					}
					else
					{
						basicFunctionBlocksList.add(instanceName);
					}
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
					Logger.output(Logger.ERROR, builderName() + ".loadFB(" + instanceName + ", " + fileName + "): Unsupported FB type: " + typeName);
					Logger.output(Logger.ERROR, "Neither a Basic FB nor a Composite FB.", 1);
					exit(1);
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
				Logger.output(Logger.DEBUG, builderName() + ".getFile(): Looking for file " + theFile.toString());
				if (theFile.exists())
				{
					break;
				}
			}
		}

		if (!theFile.exists())
		{
			Logger.output(Logger.ERROR, builderName() + ".getFile(" + fileName + "): The file " + fileName + " does not exist in the specified libraries...");
			if (libraryPathList != null)
			{
				for (Iterator iter = libraryPathList.iterator();iter.hasNext();)
				{
					Logger.output(Logger.ERROR, ((File) iter.next()).getAbsolutePath() + File.separator, 1);
				}
			}
			else
			{
				Logger.output(Logger.ERROR, ". (current directory)", 1);
			}
			exit(1);
		}
		return theFile;
    }


	
	private void makeEventConnectionsMap(JaxbFBNetwork fbNetwork, String parentInstance, int level)
	{
		if (parentInstance == null)
		{
			Logger.output(builderName() + ".makeEventConnectionMap(System):");
		}
		else
		{
			Logger.output(builderName() + ".makeEventConnectionMap(" + parentInstance + "):");
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
				
			Logger.output(Logger.DEBUG, "Analyzing connection: " + source + "-->" + dest, level + 1);
			
			JaxbFBType sourceType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(sourceInstance));

			if (sourceType != null)
			{
				if (sourceType.isSetFBNetwork())
				{
					makeEventConnectionsMap(sourceType.getFBNetwork(), sourceInstance, level + 1);
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
			String destInstance = getInstanceName(dest);
			String destSignal   = getSignalName(dest);
		
			Logger.output(Logger.DEBUG, "Adding connection: " + source + "-->" + dest, level + 1);
			
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

			if (!eventConnections.keySet().contains(destInstance))
			{
				eventMap = new HashMap();
				eventConnections.put(destInstance, eventMap);
			}
			else
			{
				eventMap = (Map) eventConnections.get(destInstance);
			}
			eventMap.put(destSignal, source);
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

	private void makeDataConnectionsMap(JaxbFBNetwork fbNetwork, String parentInstance, int level)
	{
		if (parentInstance == null)
		{
			Logger.output(builderName() + ".makeDataConnectionMap(System):");
		}
		else
		{
			Logger.output(builderName() + ".makeDataConnectionMap(" + parentInstance + "):");
		}
		if (fbNetwork.isSetDataConnections())
		{
			for (Iterator connIter = fbNetwork.getDataConnections().getConnection().iterator(); connIter.hasNext();)
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
				
				Logger.output(Logger.DEBUG, "Analyzing connection: " + source + "-->" + dest, level + 1);
				
				JaxbFBType destType = (JaxbFBType) fbTypes.get((String) functionBlocks.get(destInstance));
				
				if (destType != null)
				{
					if (destType.isSetFBNetwork())
					{
						makeDataConnectionsMap(destType.getFBNetwork(), destInstance, level + 1);
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
				String sourceInstance = getInstanceName(source);
				String sourceSignal   = getSignalName(source);
				
				Logger.output(Logger.DEBUG, "Adding connection: " + source + "-->" + dest, level + 1);
				
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

				if (!sourceInstance.equals(""))
				{
					if (!dataConnections.keySet().contains(sourceInstance))
					{
						dataMap = new HashMap();
						dataConnections.put(sourceInstance, dataMap);
					}
					else
					{
						dataMap = (Map) dataConnections.get(sourceInstance);
					}
					dataMap.put(sourceSignal, dest);
				}
			}	
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

	private void makeStartup()
	{
		Logger.output(builderName() + ".makeStartup():");

		String fbName = restartInstance;
		
		ExtendedAutomaton startup;
		if (stopInstance != null)
		{
			startup = getNewAutomaton("Startup and Finish");
		}
		else
		{
			startup = getNewAutomaton("Startup");
		}

		startup.addInitialState("s0");
		
		String from = "s0";
		String to = "s1"; 
		startup.addState(to,false,false);
		String event = "send_output_COLD_" + fbName + ";";
		startup.addTransition(from, to, event, null, null);

		if (isEventConnected(fbName,"COLD"))
		{			
			// get connection data for the action
			String cntName = getEventConnection(fbName, "COLD");
			String cntFB = getInstanceName(cntName);
			String cntSignal = getSignalName(cntName);

			from = to;
			to = "s2";
			startup.addState(to,false,false);
			event = "receive_event_" + cntSignal + "_" + cntFB + ";";
			startup.addTransition(from, to, event, null, null);

			from = to;
			to = "s3";
			if (stopInstance != null)
			{
				startup.addState(to,false,false);
			}
			else
			{
				startup.addAcceptingState(to);				
			}
			event = "received_event_" + cntSignal + "_" + cntFB + ";";
			startup.addTransition(from, to, event, null, null);

			from = to;
			to = to;
			event = "remove_fb;";
			startup.addTransition(from, to, event, null, null);

			if (stopInstance != null)
			{
				from = to;
				to = "s4" ;
				startup.addState(to,false,false);
				event = "receive_event_STOP_" + stopInstance + ";";
				startup.addTransition(from, to, event, null, null);
				
				from = to;
				to = "s5";
				startup.addAcceptingState(to);
				event = "received_event_STOP_" + stopInstance + ";";
				startup.addTransition(from, to, event, null, null);
			}
		}
		else
		{
			Logger.output(Logger.ERROR, "The E_RESTART.COLD is not connected!");
			Logger.output(Logger.ERROR, "The application can not start.", 1);
			exit(1);
		}
		automata.addAutomaton(startup);
	}

	private void makeInstanceQueue()
	{
		Logger.output(builderName() + ".makeInstanceQueue():");

		ExtendedAutomaton instanceQueue = getNewAutomaton("Instance Queue");
		
		// the maximum number of FB instances in the queue at the same time
		int places = basicFunctionBlocks.keySet().size();
		if (instanceQueuePlaces > 0 && instanceQueuePlaces <= places)
		{
			places = instanceQueuePlaces.intValue();
		}

		instanceQueue.addIntegerVariable("current_fb", 0, fbMaxID, 0, 0);
		instanceQueue.addIntegerVariable("fb_first", 1, places, 1, 0);
		
		instanceQueue.addInitialState("s0");
		for (int i = 0; i <= (places - 1); i++)
		{			
			instanceQueue.addIntegerVariable("fb_place_" + (i+1), 0, fbMaxID, 0, 0);

			instanceQueue.addState("s" + (i+1));

			String from = "s" + i;
			String to = "s" + (i+1);
			String event = "";
			String guard = "";
			String action = "";
			
			//Transiton when queuing instance
			// i is number of blocks in queue when making the transition
			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String fbName = (String) fbIter.next();		
				Integer fbID = (Integer) basicFunctionBlocksID.get(fbName);
				
				event = "queue_fb_" + fbName + ";";
				for (int j = 1; j<=places; j++)
				{
					guard = "fb_first == " + j;
					action = "fb_place_" + ((((j-1)+i) % places) + 1) + " = " + fbID + ";";
					instanceQueue.addTransition(from, to, event, guard, action);
				}
			}
			
			// Transiton when dequeuing instance
			from = "s" + (i+1);
			to = "s" + i;
			event = "remove_fb;";      
			for (int j = 1; j<=places; j++)
			{
				guard = "fb_first == " + j;
				action = "current_fb = fb_place_" + j + ";";
				action = action + "fb_first = " + ((j % places) + 1) + ";";
				action = action + "fb_place_" + j + " = 0;";
				instanceQueue.addTransition(from, to, event, guard, action);
			}
		}
		automata.addAutomaton(instanceQueue);
	}

	private void makeEventExecution()
	{
		Logger.output(builderName() + ".makeEventExecution():");

		ExtendedAutomaton eventExecution = getNewAutomaton("Event Execution");

		eventExecution.addInitialState("s0");
		eventExecution.addState("s1",false,false);
		eventExecution.addTransition("s0", "s1", "remove_fb;", null, null);	

		int nameCounter = 2;

		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();
			
			String from = "s1";
			String to = "s" + nameCounter;
			nameCounter++;
			eventExecution.addState(to,false,false);
			String event = "handle_event_" + instanceName + ";";
			eventExecution.addTransition(from, to, event, null, null);

			from = to;
			to = "s0";
			event = "handling_event_done_" + instanceName + ";";
			eventExecution.addTransition(from, to, event, null, null);
		}
		automata.addAutomaton(eventExecution);
	}

	private void makeAlgorithmExecution()
	{
		if (algMaxID > 0)
		{
			Logger.output(builderName() + ".makeAlgorithmExecution():");
			
			ExtendedAutomaton algorithmExecution = getNewAutomaton("Algorithm Execution");
				
			algorithmExecution.addInitialState("s0");
			//algorithmExecution.addState("s1");
			//algorithmExecution.addTransition("s0", "s1", "remove_job;", null, null);	

			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String instanceName = (String) fbIter.next();
				Integer instanceID = (Integer) basicFunctionBlocksID.get(instanceName);
				String typeName = (String) basicFunctionBlocks.get(instanceName);
				JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
				Map algorithmMap = (Map) algorithms.get(instanceName);
				// localy re-defining class attribute
				List algorithms = theType.getBasicFB().getAlgorithm();

				String from = "";
				String to = "";
				String event = "";
				String guard = "";
				String action = "";
				int nameCounter = 1;

				if (algorithmMap != null)
				{
					for (Iterator algIter = algorithms.iterator(); algIter.hasNext();)
					{
						JaxbAlgorithm curAlg = (JaxbAlgorithm) algIter.next();
						String algName = curAlg.getName();
						String algLang = curAlg.getOther().getLanguage();
						String algText = curAlg.getOther().getText();
						Integer algID = (Integer) algorithmMap.get(algName);
					
						if (algLang.toLowerCase().equals("java"))
						{
							from = "s0";
							to = "s" + nameCounter;
							nameCounter++;
							algorithmExecution.addState(to,false,false);
							event = "execute_" + algName + "_" + instanceName + ";";
							//guard = "current_job_fb == " + instanceID;
							//guard = guard + " & current_job_alg == " + algID;
							algorithmExecution.addTransition(from, to, event, null, null);
							from = to;

							to = "s0";
							event = "finished_execution_" + algName + "_" + instanceName + ";";
							algorithmExecution.addTransition(from, to, event, null, null);
						}
					}
				}
			}
			automata.addAutomaton(algorithmExecution);
		}
	}

	private void makeBasicFB(String fbName)
	{	
		Logger.output(builderName() + ".makeBasicFB(" + fbName + "):");
	
		makeBasicFBEventHandling(fbName);
		makeBasicFBEventQueue(fbName);
		makeBasicFBExecutionControlChart(fbName);
		makeBasicFBAlgorithms(fbName);
	}

	private void makeBasicFBEventHandling(String fbName)
	{
		Logger.output("Event Handling", 1);
		
		ExtendedAutomaton eventHandling = getNewAutomaton(fbName + ": Event Handling");

		eventHandling.addInitialState("s0");
		eventHandling.addState("s1");
		eventHandling.addState("s2");
		eventHandling.addState("s3");

		String from = "s0";
		String to = "s1";
		String event = "handle_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s1";
		to = "s2";
		event = "remove_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s2";
		to = "s3";
		event = "update_ECC_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s3";
		to = "s2";
		event = "no_more_actions_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);
		
		from = "s3";
		to = "s3";
		event = "no_transition_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s3";
		to = "s0";
		event = "handling_event_done_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		automata.addAutomaton(eventHandling);
	}

	private void makeBasicFBEventQueue(String fbName)
	{
		Logger.output("Event Queue", 1);

		String typeName = (String) basicFunctionBlocks.get(fbName);
		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
		List eventInputList = (List) ((EventInputs) ((InterfaceList) theType.getInterfaceList()).getEventInputs()).getEvent();
		
		ExtendedAutomaton eventQueue = getNewAutomaton(fbName + ": Event Queue");
		
		// the maximum number of events in the queue at the same time
		int places = ((Integer) eventsMaxID.get(fbName)).intValue();	
		if (eventQueuePlaces > 0 && eventQueuePlaces <= places )
		{
			places = eventQueuePlaces.intValue();
		}
		
		// event input variables
		if (theType.getInterfaceList().isSetEventInputs())
		{
			final List eventInputs = theType.getInterfaceList().getEventInputs().getEvent();
			for (Iterator eventInputsIter = eventInputs.iterator(); eventInputsIter.hasNext();)
			{
				JaxbEvent curEventInput = (JaxbEvent) eventInputsIter.next();
				String curEventInputName = curEventInput.getName();
				eventQueue.addIntegerVariable("event_" + curEventInputName + "_" + fbName, 0, 1, 0, 0);
			}
		}

		// data input variables
		if (theType.getInterfaceList().isSetInputVars())
		{
			final List dataInputs = theType.getInterfaceList().getInputVars().getVarDeclaration();
			for (Iterator dataInputsIter = dataInputs.iterator(); dataInputsIter.hasNext();)
			{
				VarDeclaration curDeclaration = (VarDeclaration) dataInputsIter.next();
				String curDataInputName = curDeclaration.getName();
				if (isDataConnected(fbName, curDataInputName))
				{
					String curDataType =  curDeclaration.getType();
					if (curDataType.toLowerCase().equals("int"))
					{
						// get possible constant data value
						String dataCnt = (String) ((Map) dataConnections.get(fbName)).get(curDataInputName);
						if (!getInstanceName(dataCnt).equals(""))
						{
							Logger.output(Logger.DEBUG, "Making non constant data variable", 2);
							eventQueue.addIntegerVariable("data_" + curDataInputName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
						}
						else
						{
							Integer dataValue = new Integer(getSignalName(dataCnt));
							Logger.output(Logger.DEBUG, "Making constant data variable data_" + curDataInputName + "_" + fbName + " with value " + dataValue, 2);
							eventQueue.addIntegerVariable("data_" + curDataInputName + "_" + fbName, intVarMinValue, intVarMaxValue, dataValue.intValue(), 0);
						}
					}
					else if (curDataType.toLowerCase().equals("bool"))
					{
						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 1);
						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
						exit(1);
					}
					else if (curDataType.toLowerCase().equals("real"))
					{
						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 1);
						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
						exit(1);
					}
					else if (curDataType.toLowerCase().equals("string"))
					{
						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 1);
						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
						exit(1);
					}
					else if (curDataType.toLowerCase().equals("object"))
					{
						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 1);
						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
						exit(1);
					}
				}
			}
		}
		
		// data output variables
		// written only by algorithms
		if (theType.getInterfaceList().isSetOutputVars())
		{
			final List dataOutputs = theType.getInterfaceList().getOutputVars().getVarDeclaration();
			for (Iterator dataOutputsIter = dataOutputs.iterator(); dataOutputsIter.hasNext();)
			{
				VarDeclaration curDeclaration = (VarDeclaration) dataOutputsIter.next();
				String curDataOutputName = curDeclaration.getName();
				String curDataType =  curDeclaration.getType();
				if (curDataType.toLowerCase().equals("int"))
				{
					eventQueue.addIntegerVariable("data_" + curDataOutputName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
				}
				else if (curDataType.toLowerCase().equals("bool"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 1);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
					exit(1);
				}
				else if (curDataType.toLowerCase().equals("real"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 1);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
					exit(1);
				}
				else if (curDataType.toLowerCase().equals("string"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 1);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
					exit(1);
				}
				else if (curDataType.toLowerCase().equals("object"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 1);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
					exit(1);
				}
			}
		}

		// first queue element pointer
		eventQueue.addIntegerVariable("event_" + fbName + "_first", 1, places, 1, 0);
		
		String from = "";
		String to = "";
		String event = "";
		String guard = "";
		String action = "";
		int nameCounter = 1;
		
		eventQueue.addInitialState("s0");

		for (int i = 1; i <= places; i++)
		{
			Integer numEvents = (Integer) eventsMaxID.get(fbName);
			eventQueue.addIntegerVariable("event_place_" + i + "_" + fbName, 0, numEvents, 0, 0);
			
			// data input variables for each queue place
			if (theType.getInterfaceList().isSetInputVars())
			{
				final List dataInputs = theType.getInterfaceList().getInputVars().getVarDeclaration();
				for (Iterator dataInputsIter = dataInputs.iterator(); dataInputsIter.hasNext();)
				{
					VarDeclaration curDeclaration = (VarDeclaration) dataInputsIter.next();
					String curDataInputName = curDeclaration.getName();
					if (isDataConnected(fbName, curDataInputName))
					{
						String curDataType =  curDeclaration.getType();
						if (curDataType.toLowerCase().equals("int"))
						{
							eventQueue.addIntegerVariable("data_place_" + i + "_" + curDataInputName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
						}
						else if (curDataType.toLowerCase().equals("bool"))
						{
							Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 1);
							Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
							exit(1);
						}
						else if (curDataType.toLowerCase().equals("real"))
						{
							Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 1);
							Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
							exit(1);
						}
						else if (curDataType.toLowerCase().equals("string"))
						{
							Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 1);
							Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
							exit(1);
						}
						else if (curDataType.toLowerCase().equals("object"))
						{
							Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 1);
							Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
							exit(1);
						}
					}
				}
			}
			
// 			eventQueue.addState("s" + i,false,false);
			eventQueue.addState("s" + i);

			for (Iterator evIter = eventInputList.iterator(); evIter.hasNext();)
			{

				JaxbEvent curEvent = (JaxbEvent) evIter.next();
				String eventName = curEvent.getName();
				int eventID = ((Integer) ((Map) events.get(fbName)).get(eventName)).intValue();
				
				// Transitons when queuing event
				if (isEventConnected(fbName, eventName))
				{					
					from = "s" + (i-1);
					to = "s" + (places + nameCounter);
					nameCounter++;
// 					eventQueue.addState(to,false,false);
					eventQueue.addState(to);
					event = "receive_event_" + eventName + "_" + fbName + ";";
					eventQueue.addTransition(from, to, event, null, null);
					
					from = to;
					to = "s" + (places + nameCounter);
					nameCounter++;
//					eventQueue.addState(to,false,false);
 					eventQueue.addState(to);
					event = "queue_event_" + eventName + "_" + fbName + ";";
					for (int j = 1; j <= places; j++)
					{
						guard = "event_" + fbName + "_first == " + j;
						action = "event_place_" + ((((j-1)+(i-1)) % places) + 1) + "_" + fbName + " = " + eventID + ";";
						if (curEvent.isSetWith())
						{
							List withData = curEvent.getWith();
							for (Iterator withIter = withData.iterator(); withIter.hasNext();)
							{
								String curWith = ((With) withIter.next()).getVar();
								if (isDataConnected(fbName, curWith))
								{														
									String cntFrom = (String) ((Map) dataConnections.get(fbName)).get(curWith);
									String fromInstance = getInstanceName(cntFrom);
									String fromSignal = getSignalName(cntFrom);				
									if (fromInstance.equals(""))
									{
										// constant data connection
										action = action + 
											"data_place_" + ((((j-1)+(i-1)) % places) + 1) + "_" + curWith + "_" + fbName + 
											" = " + new Integer(fromSignal) + ";";
									}
									else
									{
										// instance data connection
										action = action + 
											"data_place_" + ((((j-1)+(i-1)) % places) + 1) + "_" + curWith + "_" + fbName + 
											" = data_" + fromSignal + "_" + fromInstance + ";";
									}
								}
							}
						}
						eventQueue.addTransition(from, to, event, guard, action);
					}
					
					from = to;
					to = "s" + i;
					event = "received_event_" + eventName + "_" + fbName + ";";
					eventQueue.addTransition(from, to, event, null, null);
				}
				
			}


			// Transitions when dequeuing event
			for (Iterator evIter = eventInputList.iterator(); evIter.hasNext();)
			{
				JaxbEvent curEvent = (JaxbEvent) evIter.next();
				String eventName = curEvent.getName();
				int eventID = ((Integer) ((Map) events.get(fbName)).get(eventName)).intValue();

				if (isEventConnected(fbName, eventName))
				{					
					from = "s" + i;
					to = "s" + (places + nameCounter);
					nameCounter++;
//					eventQueue.addState(to,false,false);
 					eventQueue.addState(to);
					event = "remove_event_" + fbName + ";";
					for (int j = 1; j <= places; j++)
					{
						guard = "(event_" + fbName + "_first == " + j + ") & ";
						guard = guard + "(event_place_" + j + "_" + fbName + " == " + eventID + ")";
						action = "event_" + eventName + "_" + fbName + " = 1;";
						action = action + "event_" + fbName + "_first = " + ((j % places) + 1) + ";";				
						action = action + "event_place_" + j + "_" + fbName +  " = 0;";
						if (curEvent.isSetWith())
						{
							List withData = curEvent.getWith();
						
							// get first data in the queue
							for (Iterator withIter = withData.iterator(); withIter.hasNext();)
							{
								String curWith = ((With) withIter.next()).getVar();
								if (isDataConnected(fbName, curWith))
								{														
									action = action + 
										"data_" + curWith + "_" + fbName + " = data_place_" + j + "_" + curWith + "_" + fbName + ";";
									action = action + 
										"data_place_" + j + "_" + curWith + "_" + fbName + " = 0;";
								}
							}
						}
						eventQueue.addTransition(from, to, event, guard, action);
					}					

					from = to;
					to = "s" + (i-1);
					event = "reset_event_" + eventName + "_" + fbName + ";";
					action = "event_" + eventName + "_" + fbName + " = 0;";
					eventQueue.addTransition(from, to, event, null, action);

					//from = to;
					//to = "s" + (i-1);
					//event = "update_ECC_" + fbName + ";";
					//eventQueue.addTransition(from, to, event, null, null);
				}
			}
		}		
		automata.addAutomaton(eventQueue);	
	}
	
	private void makeBasicFBExecutionControlChart(String fbName)
	{
		Logger.output("Execution Control Chart", 1);
		
		String typeName = (String) basicFunctionBlocks.get(fbName);
		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
		ECC theECC = theType.getBasicFB().getECC();
		List ecStates = theECC.getECState();
		List ecTransitions = theECC.getECTransition();
		Set visitedECStates = new HashSet();

		ExtendedAutomaton ecc = getNewAutomaton(fbName + ": Execution Control Chart");

		// internal variables
		if (theType.getBasicFB().isSetInternalVars())
		{
			final List internalVars = theType.getBasicFB().getInternalVars().getVarDeclaration();
			for (Iterator internalVarIter = internalVars.iterator(); internalVarIter.hasNext();)
			{
				VarDeclaration curDeclaration = (VarDeclaration) internalVarIter.next();
				String curName = curDeclaration.getName();
				String curType =  curDeclaration.getType();
				if (curType.toLowerCase().equals("int"))
				{
					ecc.addIntegerVariable("internal_" + curName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
				}
				else if (curType.toLowerCase().equals("bool"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 2);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curName, 3);
					exit(1);
				}
				else if (curType.toLowerCase().equals("real"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 2);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curName, 3);
					exit(1);
				}
				else if (curType.toLowerCase().equals("string"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 2);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curName, 3);
					exit(1);
				}
				else if (curType.toLowerCase().equals("object"))
				{
					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 2);
					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curName, 3);
					exit(1);
				}
			}
		}

		// make identifier map
		Map identifierMap = new HashMap();
		// get the FB type and all the variables
		List eventInputs = null;
		List inputVars = null;
		List outputVars = null;
		List internalVars = null;
		if (theType.getInterfaceList().isSetEventInputs())
		{
			eventInputs = theType.getInterfaceList().getEventInputs().getEvent();
		}		
		if (theType.getInterfaceList().isSetInputVars())
		{
			inputVars = theType.getInterfaceList().getInputVars().getVarDeclaration();
		}		
		if (theType.getInterfaceList().isSetOutputVars())
		{
			outputVars = theType.getInterfaceList().getOutputVars().getVarDeclaration();
		}
		if (theType.getBasicFB().isSetInternalVars())
		{
			internalVars = theType.getBasicFB().getInternalVars().getVarDeclaration();
		}
		// input events
		if (eventInputs != null)
		{
			for (Iterator iter = eventInputs.iterator();iter.hasNext();)
			{
				JaxbEvent curEventInput = (JaxbEvent) iter.next();
				String curEventInputName = curEventInput.getName();
				identifierMap.put(curEventInputName, "event_" + curEventInputName + "_" + fbName + " == 1");
			}
		}
		// input vars
		if (inputVars != null)
		{
			for (Iterator iter = inputVars.iterator();iter.hasNext();)
			{
				VarDeclaration curVar = (VarDeclaration) iter.next();
				String curVarName = curVar.getName();
				identifierMap.put(curVarName, "data_" + curVarName + "_" + fbName);
			}
		}
		// output vars
		if (outputVars != null)
		{
			for (Iterator iter = outputVars.iterator();iter.hasNext();)
			{
				VarDeclaration curVar = (VarDeclaration) iter.next();
				String curVarName = curVar.getName();
				identifierMap.put(curVarName, "data_" + curVarName + "_" + fbName);
			}
		}
		// internal vars
		if (internalVars != null)
		{
			for (Iterator iter = internalVars.iterator();iter.hasNext();)
			{
				VarDeclaration curVar = (VarDeclaration) iter.next();
				String curVarName = curVar.getName();
				identifierMap.put(curVarName, "internal_" + curVarName + "_" + fbName);
			}
		}
				
		nameCounter = 0;
		doneInitActions = false;
		doneInitFinish = false;
		JaxbECState firstECState = (JaxbECState) ecStates.get(0);
		String firstECStateName = firstECState.getName();
		if (firstECStateName.startsWith("m"))
		{
			ecc.addInitialState(firstECStateName);
		}
		else
		{
			ecc.addState(firstECStateName,false,true);
		}

		Logger.output(Logger.DEBUG, "Calling makeECStateBranch() from makeBasicFBExecutionControlChart()", 2);
		makeECStateBranch(ecc, fbName, firstECStateName, firstECStateName, ecStates, ecTransitions, visitedECStates, 2, identifierMap);

		automata.addAutomaton(ecc);	
	}

	private void makeECStateBranch(ExtendedAutomaton ecc, String fbName, String ecStateName, String prevStateName, List ecStates, List ecTransitions, Set visitedECStates, int level, Map identifierMap)
	{
		Logger.output(Logger.DEBUG, "Entering makeECStateBranch(): ecStateName = " + ecStateName + ": prevStateName = " + prevStateName, level);

		// temporary variables
		String from = null;
		String to = null;
		String event = null;
		String guard = null;
		String action = null;		
		String noTransitionFrom = null;
		String noTransitionTo = null;
		String noTransitionGuard = null;
		boolean makeNoTransition = true;

		// get event inputs for the block
		String typeName = (String) basicFunctionBlocks.get(fbName);
		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
		List eventInputs = null;
		if (theType.getInterfaceList().isSetEventInputs())
		{
			eventInputs = theType.getInterfaceList().getEventInputs().getEvent();
		}		

		// get the first EC state (ie initial)
		JaxbECState firstECState = (JaxbECState) ecStates.get(0);
		String firstECStateName = firstECState.getName();

		// mark the EC state as visited
		Logger.output(Logger.DEBUG, "Visited EC state: " + ecStateName, level);
		visitedECStates.add(ecStateName);

		// get the EC state
		JaxbECState ecState = null;
		for (Iterator iter = ecStates.iterator();iter.hasNext();)
		{
			JaxbECState curECState = (JaxbECState) iter.next();
			if (ecStateName.contains(curECState.getName()))
			{
				ecState = curECState;
			}
		}
		
		// find all transitions from this EC state
		Set ecStateTransitions = new HashSet();
		for (Iterator ecTransitionsIter = ecTransitions.iterator(); ecTransitionsIter.hasNext();)
		{
			JaxbECTransition curECTransition = (JaxbECTransition) ecTransitionsIter.next();
			if (curECTransition.getSource().equals(ecStateName))
			{
				ecStateTransitions.add(curECTransition);
			}
		}
		
		// make update_ECC model transition					
		from = prevStateName;
		to = "s" + nameCounter;
		nameCounter++;
		Logger.output(Logger.DEBUG, "Adding state: " + to, level);
		ecc.addState(to,false,false);
		event = "update_ECC_" + fbName + ";";
		Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
		ecc.addTransition(from, to, event, null, null);
		noTransitionFrom = to;
		noTransitionTo = from;
		prevStateName = to;

		// make model for each EC transition
		for (Iterator ecStateTransitionsIter = ecStateTransitions.iterator(); ecStateTransitionsIter.hasNext();)
		{
			JaxbECTransition curECTransition = (JaxbECTransition) ecStateTransitionsIter.next();
			String curECSourceName = curECTransition.getSource();			
			String curECDestName = curECTransition.getDestination();			
			String curECCondition = curECTransition.getCondition();			

			Logger.output(Logger.DEBUG, "Analyzing EC transition: from: " + curECSourceName +
				   ", to: " + curECDestName + ", cond: " + curECCondition, level);

			// loop temporary vars
			boolean oneTransitionFromECSource = false;
			boolean oneTransitionFromECDest = false;
			String next = "";

			// get the current destination EC state
			JaxbECState curECDestState = null;
			for (Iterator iter = ecStates.iterator();iter.hasNext();)
			{
				JaxbECState curECState = (JaxbECState) iter.next();
				if (curECState.getName().equals(curECDestName))
				{
					curECDestState = curECState;
				}
			}
		
			// make model transition for the current EC transition
			from = prevStateName;
			to =  curECDestName + "_actions";
			Logger.output(Logger.DEBUG, "Adding state: " + to, level);
			ecc.addState(to,false,false);
			if (curECTransition.getCondition().equals("1"))
			{
				oneTransitionFromECSource = true;
				makeNoTransition = false;
				event = "one_transition_" + fbName + ";";
				guard = null;
				action = null;
				Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
				ecc.addTransition(from, to, event, guard, action);
				next = to;					
			}
			else
			{				
				// parse the current EC condition and translate to guard
				StringReader conditionReader = new StringReader(curECCondition);
				Lexer lexer = new Lexer((Reader) conditionReader);
				Parser parser = new Parser((Scanner) lexer);
				Goal parsedCondition = null;
				try
				{
					parsedCondition = (Goal) parser.parse().value;
				}
				catch(Exception e)
				{
					Logger.output(Logger.ERROR, "Error!: Parsing of the EC condition failed:", level);
					Logger.output(Logger.ERROR, "Condition: " + curECCondition, level + 1);
					exit(1);
				}
				Finder finder = new Finder(parsedCondition);
				Translator translator = new Translator(parsedCondition, identifierMap, operatorMap);

				guard = translator.translate();

				// make transition for every input event in EC condition
				String newGuard = null;
				if (eventInputs != null)
				{
					for (Iterator iter = eventInputs.iterator(); iter.hasNext();)
					{
						JaxbEvent curEventInput = (JaxbEvent) iter.next();
						String curEventInputName = curEventInput.getName();
						if (isEventConnected(fbName, curEventInputName))
						{
							if (finder.existsIdentifier(curEventInputName))
							{
								from = prevStateName;
								to = "s" + nameCounter;
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "event_input_" + curEventInputName + "_" + fbName + ";";
								newGuard = "event_" + curEventInputName + "_" + fbName + " == 1 & (" + guard + ")";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, newGuard, null);
								
								from = to;
								to = curECDestName + "_actions";
								event = "reset_event_" + curEventInputName + "_" + fbName + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
							}
						}
					}
				}
				next = to;
				
				// add to gurad for no_transition event
				if (makeNoTransition)
				{
					if (noTransitionGuard == null)
					{
						noTransitionGuard = "!(" + guard + ")";
					}
					else
					{
						noTransitionGuard = noTransitionGuard + " & !(" + guard + ")";
					}
				}				
			}
			
			// make actions model of the destination EC state
			if (!visitedECStates.contains(curECDestName) || (curECDestName.equals(firstECStateName) && !doneInitActions))
			{
				if (curECDestName.equals(firstECStateName))
				{
					doneInitActions = true;
				}
				Logger.output(Logger.DEBUG, "Making actions for EC state: " + curECDestName, level);
				List destECActions = curECDestState.getECAction(); 
				if (destECActions.size()>0)
				{
					for (Iterator actionsIter = destECActions.iterator(); actionsIter.hasNext();)
					{
						JaxbECAction curAction = (JaxbECAction) actionsIter.next();
						if (curAction.isSetAlgorithm())
						{
							// get action algorithm
							String actionAlgorithm = curAction.getAlgorithm();
							Integer blockID = (Integer) basicFunctionBlocksID.get(fbName);
							
							from = next;
							to = "s" + nameCounter; 
							nameCounter++;
							Logger.output(Logger.DEBUG, "Adding state: " + to, level);
							ecc.addState(to,false,false);
							event = "execute_" + actionAlgorithm + "_" + fbName + ";";
							Logger.output(Logger.DEBUG, "Adding transition: from " + from + ": to " + to + ": event " + event, level);
							ecc.addTransition(from, to, event, null, null);
							next = to;						
							
							if (curAction.isSetOutput())
							{
								from = next;
								to = "s" + nameCounter; 
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "finished_execution_" + actionAlgorithm + "_" + fbName + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
								next = to;						
								
								from = next;
								to = "s" + nameCounter; 
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "send_output_" + curAction.getOutput() + "_" + fbName + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
								next = to;						
								
								if (isEventConnected(fbName, curAction.getOutput()))
								{
									// get connection data for the action
									String cntName = getEventConnection(fbName, curAction.getOutput());
									String cntFB = getInstanceName(cntName);
									String cntSignal = getSignalName(cntName);

									from = next;
									to = "s" + nameCounter; 
									nameCounter++;
									Logger.output(Logger.DEBUG, "Adding state: " + to, level);
									ecc.addState(to,false,false);
									event = "receive_event_" + cntSignal + "_" + cntFB + ";";
									Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
									ecc.addTransition(from, to, event, null, null);
									next = to;						

									from = next;
									to = "s" + nameCounter; 
									nameCounter++;
									Logger.output(Logger.DEBUG, "Adding state: " + to, level);
									ecc.addState(to,false,false);
									event = "received_event_" + cntSignal + "_" + cntFB + ";";
									Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
									ecc.addTransition(from, to, event, null, null);
									next = to;						
								}
							}
							else
							{
								from = next;
								to = "s" + nameCounter; 
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "finished_execution_" + actionAlgorithm + "_" + fbName + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
								next = to;						
							}
						}
						else if (curAction.isSetOutput())
						{
							from = next;
							to = "s" + nameCounter; 
							nameCounter++;
							Logger.output(Logger.DEBUG, "Adding state: " + to, level);
							ecc.addState(to,false,false);
							event = "send_output_" + curAction.getOutput() + "_" + fbName + ";";
							Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
							ecc.addTransition(from, to, event, null, null);
							next = to;						
							
							if (isEventConnected(fbName, curAction.getOutput()))
							{
								// get connection data for the action
								String cntName = getEventConnection(fbName, curAction.getOutput());
								String cntFB = getInstanceName(cntName);
								String cntSignal = getSignalName(cntName);

								from = next;
								to = "s" + nameCounter; 
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "receive_event_" + cntSignal + "_" + cntFB + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
								next = to;						

								from = next;
								to = "s" + nameCounter; 
								nameCounter++;
								Logger.output(Logger.DEBUG, "Adding state: " + to, level);
								ecc.addState(to,false,false);
								event = "received_event_" + cntSignal + "_" + cntFB + ";";
								Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
								ecc.addTransition(from, to, event, null, null);
								next = to;						
							}
						}
					}
				}
			}

			// find if there is any transition on "1" from curECDestState
			for (Iterator ecTransitionsIter = ecTransitions.iterator(); ecTransitionsIter.hasNext();)
			{
				JaxbECTransition tempECTransition = (JaxbECTransition) ecTransitionsIter.next();
				if (tempECTransition.getSource().equals(curECDestName))
				{
					if (tempECTransition.getCondition().equals("1"))
					{
						oneTransitionFromECDest = true;
					}
				}
			}

			// one transition loop warning
			if (oneTransitionFromECSource && oneTransitionFromECDest)
			{
				Logger.output(Logger.WARN, "Warning!: Loop with \"1\" transitions found. This gives a live lock in the application!!", level);
				Logger.output(Logger.WARN, "Check EC states: " + curECSourceName + " and " + curECDestName, level + 1);
			}

			// finish this state
			if (oneTransitionFromECDest)
			{
				if (!visitedECStates.contains(curECDestName))
				{	
					// no_action model transition
					from = next;
					to = curECDestName; 
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					if(curECDestName.startsWith("m"))
					{
						ecc.addState(to);
					}
					else
					{
						ecc.addState(to,false,false);
					}
					event = "no_more_actions_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
					
					Logger.output(Logger.DEBUG, "Calling makeECStateBranch() from makeECStateBranch()", level);
					makeECStateBranch(ecc, fbName, curECDestName, to, ecStates, ecTransitions, visitedECStates, level + 1, identifierMap);
				}
				else if (curECDestName.equals(firstECStateName)  && !doneInitFinish)
				{
					doneInitFinish = true;
					// no_action model transition
					from = next;
					to = curECDestName; 
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					if(curECDestName.startsWith("m"))
					{
						ecc.addState(to);
					}
					else
					{
						ecc.addState(to,false,false);
					}
					event = "no_more_actions_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
				}
			}
			else
			{				
				if (!visitedECStates.contains(curECDestName))
				{
					// no_action model transition
					from = next;
					to = "s" + nameCounter;
					nameCounter++;
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					ecc.addState(to,false,false);
					event = "no_more_actions_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
					
					// make update_ECC model transition					
					from = next;
					to = "s" + nameCounter;
					nameCounter++;
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					ecc.addState(to,false,false);
					event = "update_ECC_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;
					
					// handling_event_done model transition
					from = next;
					to = curECDestName;
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					if(curECDestName.startsWith("m"))
					{
						ecc.addState(to);
					}
					else
					{
						ecc.addState(to,false,false);
					}
					event = "handling_event_done_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
					
					Logger.output(Logger.DEBUG, "Calling makeECStateBranch() from makeECStateBranch()", level);
					makeECStateBranch(ecc, fbName, curECDestName, next, ecStates, ecTransitions, visitedECStates, level + 1, identifierMap);
				}
				else if (curECDestName.equals(firstECStateName) && !doneInitFinish)
				{
					doneInitFinish = true;
					// no_action model transition
					from = next;
					to = "s" + nameCounter;
					nameCounter++;
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					ecc.addState(to,false,false);
					event = "no_more_actions_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
					
					// make update_ECC model transition					
					from = next;
					to = "s" + nameCounter;
					nameCounter++;
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					ecc.addState(to,false,false);
					event = "update_ECC_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;
					
					// handling_event_done model transition
					from = next;
					to = curECDestName;
					if(curECDestName.startsWith("m"))
					{
						ecc.addState(to);
					}
					else
					{
						ecc.addState(to,false,false);
					}
					Logger.output(Logger.DEBUG, "Adding state: " + to, level);
					event = "handling_event_done_" + fbName + ";";
					Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
					ecc.addTransition(from, to, event, null, null);
					next = to;				
				}
			}
		}

		// make no_transition event and guard
		if (makeNoTransition)
		{
			from = noTransitionFrom;
			to = "s" + nameCounter;
			nameCounter++;
			ecc.addState(to,false,false);
			event = "no_transition_" + fbName + ";";
			Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
			ecc.addTransition(from, to, event, noTransitionGuard, null);
			
			from = to;
			to = "s" + nameCounter;
			nameCounter++;
			ecc.addState(to,false,false);
			// reset active event
			for (Iterator iter = eventInputs.iterator();iter.hasNext();)
			{
				JaxbEvent curEventInput = (JaxbEvent) iter.next();
				String curEventInputName = curEventInput.getName();
				event = "reset_event_" + curEventInputName + "_" + fbName + ";";
				guard = "event_" + curEventInputName + "_" + fbName + " == 1";
				Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
				ecc.addTransition(from, to, event, guard, null);
			}
			
			from = to;
			to = noTransitionTo;
			event = "handling_event_done_" + fbName + ";";
			Logger.output(Logger.DEBUG, "Adding transition: from: " + from + ", to: " + to + ", event: " + event, level);
			ecc.addTransition(from, to, event, null, null);
		}
	}
	
	private void makeBasicFBAlgorithms(String fbName)
	{
		String typeName = (String) basicFunctionBlocks.get(fbName);
		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
		List algorithms = theType.getBasicFB().getAlgorithm();

		if (algorithms.size() > 0)
		{
			Logger.output("Algorithms", 1);
		}

		// temporary variables
		String from = null;
		String to = null;
		String event = null;
		String action = null;

		// for all algorithms
		for (Iterator algIter = algorithms.iterator(); algIter.hasNext();)
		{
			JaxbAlgorithm curAlg = (JaxbAlgorithm) algIter.next();
			String algName = curAlg.getName();
			String algLang = curAlg.getOther().getLanguage();
			String algText = curAlg.getOther().getText();
			ExtendedAutomaton curAlgModel = getNewAutomaton(fbName + " " + algName + ": Algorithm");
			int nameCounter = 0;

			if (algLang.toLowerCase().equals("java"))
			{
				// get the variables and make identifier map for translation
				Map identifierMap = new HashMap();
				//Map reverseIdentifierMap = new HashMap();
				if (theType.getInterfaceList().isSetInputVars())
				{
					List inputVars = theType.getInterfaceList().getInputVars().getVarDeclaration();
					for (Iterator iter = inputVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "data_" + curVarName + "_" + fbName);
						//reverseIdentifierMap.put("alg_data_" + curVarName + "_" + algName + "_" + fbName, "data_" + curVarName + "_" + fbName);
					}
				}		
				if (theType.getInterfaceList().isSetOutputVars())
				{
					List outputVars = theType.getInterfaceList().getOutputVars().getVarDeclaration();
					for (Iterator iter = outputVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "data_" + curVarName + "_" + fbName);
						//reverseIdentifierMap.put("alg_data_" + curVarName + "_" + algName + "_" + fbName, "data_" + curVarName + "_" + fbName);
					}
				}
				if (theType.getBasicFB().isSetInternalVars())
				{
					List internalVars = theType.getBasicFB().getInternalVars().getVarDeclaration();
					for (Iterator iter = internalVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "internal_" + curVarName + "_" + fbName);
						//reverseIdentifierMap.put("alg_internal_" + curVarName + "_" + algName + "_" + fbName, "internal_" + curVarName + "_" + fbName);
					}
				}
				
				// parse the Java algorithm
				StringReader reader = new StringReader(algText);
				net.sourceforge.fuber.model.interpreters.java.Lexer javaLexer = new net.sourceforge.fuber.model.interpreters.java.Lexer((Reader) reader);
				net.sourceforge.fuber.model.interpreters.java.Parser javaParser = new net.sourceforge.fuber.model.interpreters.java.Parser((Scanner) javaLexer);
				Goal algSyntaxTree = null;
				try
				{
					algSyntaxTree = (Goal) javaParser.parse().value;
				}
				catch(Exception e)
				{
					Logger.output(Logger.ERROR, "Error!: Parsing of the Java algorithm failed:");
					Logger.output(Logger.ERROR, "Algorithm: " + algName, 1);
					Logger.output(Logger.ERROR, "Text: " + algText, 1);
					exit(1);
				}	
				
				// translate algorithm idents and expressions to efa
				net.sourceforge.fuber.model.interpreters.java.Translator translator = new net.sourceforge.fuber.model.interpreters.java.Translator(algSyntaxTree, identifierMap, null);
				String efaAlgText = translator.translate();
				
				// parse the efa algorithm
				reader = new StringReader(efaAlgText);
				net.sourceforge.fuber.model.interpreters.efa.Lexer efaLexer = new net.sourceforge.fuber.model.interpreters.efa.Lexer((Reader) reader);
				net.sourceforge.fuber.model.interpreters.efa.Parser efaParser = new net.sourceforge.fuber.model.interpreters.efa.Parser((Scanner) efaLexer);
				Goal efaAlgSyntaxTree = null;
				try
				{
					efaAlgSyntaxTree = (Goal) efaParser.parse().value;
				}
				catch(Exception e)
				{
					Logger.output(Logger.ERROR, "Error!: Parsing of the EFA algorithm failed:");
					Logger.output(Logger.ERROR, "Algorithm: " + algName, 1);
					Logger.output(Logger.ERROR, "Text: " + efaAlgText, 1);
					exit(1);
				}	
				
				// get all identifiers			
				Finder finder = new Finder(efaAlgSyntaxTree);
				Set assignmentIdents = finder.getAssignmentIdentifiers();
				Set expressionIdents = finder.getExpressionIdentifiers();
				// put all identifiers into single set
				Set algorithmIdents = new LinkedHashSet();
				for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
				{
					String curIdent = ((Identifier) iter.next()).a;
					if (!algorithmIdents.contains(curIdent))
					{
						algorithmIdents.add(curIdent);
					}
				}
				for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
				{
					String curIdent = ((Identifier) iter.next()).a;
					if (!algorithmIdents.contains(curIdent))
					{
						algorithmIdents.add(curIdent);
					}
				}
				
// 				// make alg local model variables
// 				for (Iterator iter = algorithmIdents.iterator(); iter.hasNext();)
// 				{
// 					String curIdent = (String) iter.next();
// 					curAlgModel.addIntegerVariable(curIdent, intVarMinValue, intVarMaxValue, 0, 0);
// 				}
				
				String[] efaAlgTextLines = efaAlgText.split(";");
				
				// make execution model
				from = "s" + nameCounter;
				nameCounter++;	
				curAlgModel.addInitialState(from);
				to = "s" + nameCounter;
				curAlgModel.addState(to);
				nameCounter++;
				event = "execute_" + algName + "_" + fbName + ";";
				curAlgModel.addTransition(from, to, event, null, null);
				from = to;

// 				//get the block vars
// 				to = "s" + nameCounter;
// 				nameCounter++;
// 				curAlgModel.addState(to);
// 				event = "copy_variables_" + algName + "_" + fbName + ";";
// 				// for each assignment var
// 				action = "";
// 				for (Iterator iter = algorithmIdents.iterator(); iter.hasNext();)
// 				{
// 					String curIdent = (String) iter.next();
// 					String blockVar = (String) reverseIdentifierMap.get(curIdent);
// 					action = action + curIdent + " = " + blockVar + ";";
// 				}
// 				curAlgModel.addTransition(from, to, event, null, action);
// 				from = to;
				
				// for all lines in the algorithm text
				for (int i = 0; i < efaAlgTextLines.length; i++)
				{
					String statement = efaAlgTextLines[i];
					Logger.output(Logger.DEBUG, "Making statement: " + statement, 2);
					
					to = "s" + nameCounter;
					nameCounter++;
					curAlgModel.addState(to);
					event = "statement_" + (i+1) + "_" + algName + "_" + fbName + ";";
					action = statement;
					
					Logger.output(Logger.DEBUG, "Made model action: " + action, 2);
					// make model transition
					curAlgModel.addTransition(from, to, event, null, action);
					from = to;
				}

// 				to = "s" + nameCounter;
// 				curAlgModel.addState(to);
// 				nameCounter++;
// 				event = "get_variables_" + algName + "_" + fbName + ";";
// 				curAlgModel.addTransition(from, to, event, null, null);
// 				from = to;

				
				to = "s0";
				event = "finished_execution_" + algName + "_" + fbName + ";";
				curAlgModel.addTransition(from, to, event, null, null);
				
				automata.addAutomaton(curAlgModel);	
			}
		}
	}
	
	private boolean isEventConnected(String fbName, String signal)
	{
		Map fbEventCons = (Map) eventConnections.get(fbName);
		if (fbEventCons != null)
		{
			String cntName = (String) fbEventCons.get(signal);
			if (cntName != null)
			{
				return true;
			}
		}
		return false;
	}

	private boolean isDataConnected(String fbName, String signal)
	{
		Map fbDataCons = (Map) dataConnections.get(fbName);
		if (fbDataCons != null)
		{
			String cntName = (String) fbDataCons.get(signal);
			if (cntName != null)
			{
				return true;
			}
		}
		return false;
	}

	private String getEventConnection(String fbName, String signal)
	{
		Map fbEventCons = (Map) eventConnections.get(fbName);
		if (fbEventCons != null)
		{
			String cntName = (String) fbEventCons.get(signal);
			if (cntName != null)
			{
				return cntName;
			}
		}
		return null;
	}

	private String getDataConnection(String fbName, String signal)
	{
		Map fbDataCons = (Map) dataConnections.get(fbName);
		if (fbDataCons != null)
		{
			String cntName = (String) fbDataCons.get(signal);
			if (cntName != null)
			{
				return cntName;
			}
		}
		return null;
	}

	private ExtendedAutomaton getNewAutomaton(String name)
	{
		if (generatePlantModels)
		{
			return new ExtendedAutomaton(name, automata, true);
		}
		else
		{
			return new ExtendedAutomaton(name, automata, false);
		}
	}

	private void printFunctionBlocksMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printFunctionBlocksMap():");
		for (Iterator iter = functionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			String curType  = (String) functionBlocks.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock + "\t" + curType, 1);
		}
	}

	private void printBasicFunctionBlocksMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printBasicFunctionBlocksMap():");
		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			String curType  = (String) basicFunctionBlocks.get(curBlock);
			Integer curID = (Integer) basicFunctionBlocksID.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock + "\t" + curType + "\t" + curID, 1);
		}
		Logger.output(Logger.DEBUG, "Maximal block ID: " + fbMaxID, 1);			
	}	

	private void printEventsMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printEventsMap():");
		for (Iterator iter = events.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curEventIDMap  = (Map) events.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock, 1);
			for (Iterator evIter = curEventIDMap.keySet().iterator(); evIter.hasNext();)
			{
				String curEventName = (String) evIter.next();
				Integer curEventID = (Integer) curEventIDMap.get(curEventName);
				Logger.output(Logger.DEBUG, curEventName + "\t" + curEventID, 2);
			}
			Integer evMaxID = (Integer) eventsMaxID.get(curBlock);
			Logger.output(Logger.DEBUG, "Maximal event ID: " + evMaxID, 1);
		}
	}	

	private void printAlgorithmsMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printAlgorithmsMap():");
		for (Iterator iter = algorithms.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curAlgMap  = (Map) algorithms.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock, 1);
			for (Iterator algIter = curAlgMap.keySet().iterator(); algIter.hasNext();)
			{
				String curAlgName = (String) algIter.next();
				Integer curAlgID = (Integer) curAlgMap.get(curAlgName);
				Logger.output(Logger.DEBUG, curAlgName + "\t" + curAlgID, 2);
			}
		}
		Logger.output(Logger.DEBUG, "Maximal algorithm ID: " + algMaxID, 1);
	}	

	private void printAlgorithmTextsMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printAlgorithmTextsMap():");
		for (Iterator iter = algorithmTexts.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			Map curAlgTextMap  = (Map) algorithmTexts.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock, 1);
			for (Iterator algIter = curAlgTextMap.keySet().iterator(); algIter.hasNext();)
			{
				String curAlgName = (String) algIter.next();
				String curAlgText = (String) curAlgTextMap.get(curAlgName);
				Logger.output(Logger.DEBUG, curAlgName + "\t" + curAlgText, 2);
			}
		}
	}	
	
	private void printFBTypesMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printFBTypesMap():");
		for (Iterator iter = fbTypes.keySet().iterator(); iter.hasNext();)
		{
			String curBlock = (String) iter.next();
			JaxbFBType curType  = (JaxbFBType) fbTypes.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock + "\t" + curType.getName(), 1);
		}
	}
	
	private void printEventConnectionsMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printEventConnectionsMap():");
		for (Iterator fbIter = eventConnections.keySet().iterator(); fbIter.hasNext();)
		{
			String curBlock = (String) fbIter.next();
			Map curEvents = (Map) eventConnections.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock, 1);
			for (Iterator evIter = curEvents.keySet().iterator(); evIter.hasNext();)
			{
				String curEvent = (String) evIter.next();
				String curConnection  = (String) curEvents.get(curEvent);
				Logger.output(Logger.DEBUG, curEvent + " --> " + curConnection, 2);
			}
		}
	}
	
	private void printDataConnectionsMap()
	{
		Logger.output(Logger.DEBUG, builderName() + ".printDataConnectionsMap():");
		for (Iterator fbIter = dataConnections.keySet().iterator(); fbIter.hasNext();)
		{
			String curBlock = (String) fbIter.next();
			Map curDatas = (Map) dataConnections.get(curBlock);
			Logger.output(Logger.DEBUG, curBlock, 1);
			for (Iterator dataIter = curDatas.keySet().iterator(); dataIter.hasNext();)
			{
				String curData = (String) dataIter.next();
				String curConnection  = (String) curDatas.get(curData);
				Logger.output(Logger.DEBUG, curData + " --> " + curConnection, 2);
			}
		}
	}
	
	private void exit(int status)
	{
		System.exit(status);
	}

}
