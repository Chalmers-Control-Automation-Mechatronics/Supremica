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
import java.util.Properties;
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

class DualBufferedBuilder 
	extends ExecModelBuilder 
	implements ModelBuilder
{

	DualBufferedBuilder(Properties arguments)
	{
		super(arguments);
	}
	
	public void buildModels()
	{
		Logger.output(builderName() + ".buildModels()");

 		automata = new ExtendedAutomata(theSystem.getName(), expandTransitions);
		
		makeStartup();
		
		makeInstanceQueue();
		
		makeEventExecution();
		
		makeJobQueue();
		
		makeAlgorithmExecution();
		
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
			makeBasicFB(fbName);
		}
	}

	void makeInstanceQueue()
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
		
		instanceQueue.addInitialState("s0");
		for (int i = 1; i <= places; i++)
		{
			instanceQueue.addIntegerVariable("fb_place_" + i, 0, fbMaxID, 0, 0);

			instanceQueue.addState("s" + i);
			//Transiton when queuing instance
			String from = "s" + (i-1);
			String to = "s" + i;
			String event = "";
			String action = "";
			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String fbName = (String) fbIter.next();		
				Integer fbID = (Integer) basicFunctionBlocksID.get(fbName);

				event = "submit_fb_" + fbName + ";";
				action = "fb_place_" + i + " = " + fbID + ";";
				instanceQueue.addTransition(from, to, event, null, action);
			}

			// Transiton when dequeuing instance
			from = "s" + i;
			to = "s" + (i-1);
			event = "select_fb;";      
			action = "current_fb = fb_place_1;";
			for (int j = 1; j <= i-1; j++)
			{
				action = action + "fb_place_" + j + " = fb_place_" + (j+1) + ";";
			}
			action = action + "fb_place_" + i + " = 0;";
			instanceQueue.addTransition(from, to, event, null, action);      
		}
		automata.addAutomaton(instanceQueue);
	}

	private void makeJobQueue()
	{
		if (algMaxID > 0)
		{
			Logger.output("DualExecModelBuilder.makeJobQueue():");

			ExtendedAutomaton jobQueue = getNewAutomaton("Job Queue");
		
			// the maximum number of jobs in the queue at the same time
			int places = algMaxID;	
			if (jobQueuePlaces != 0)
			{
				places = jobQueuePlaces.intValue();
			}
		
			jobQueue.addIntegerVariable("current_job_fb", 0, algFB, 0, 0);
			jobQueue.addIntegerVariable("current_job_alg", 0, algMaxID, 0, 0);

			jobQueue.addInitialState("s0");
			for (int i = 1; i <= places; i++)
			{
				jobQueue.addIntegerVariable("job_fb_place_" + i, 0, algFB, 0, 0);
				jobQueue.addIntegerVariable("job_alg_place_" + i, 0, algMaxID, 0, 0);
	
				jobQueue.addState("s" + i);
				//Transiton when queuing job
				String from = "s" + (i-1);
				String to = "s" + i;
				String event = "";
				String guard = "";
				String action = "";
				for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
				{
					String fbName = (String) fbIter.next();
					Integer fbID = (Integer) basicFunctionBlocksID.get(fbName);
					Map fbAlgorithms = (Map) algorithms.get(fbName);
					if (fbAlgorithms != null)
					{
						for (Iterator algIter = fbAlgorithms.keySet().iterator(); algIter.hasNext();)
						{
							String curAlg = (String) algIter.next();
							Integer algID = (Integer) fbAlgorithms.get(curAlg);
					
							event = "queue_job_" + curAlg + "_" + fbName +";";
							action = "job_fb_place_" + i + " = " + fbID + ";";
							action = action + "job_alg_place_" + i + " = " + algID + ";";
							jobQueue.addTransition(from, to, event, null, action);
						}
					}
				}

				// Transiton when dequeuing job
				from = "s" + i;
				to = "s" + (i-1);
				event = "remove_job;";      
				action = "current_job_fb = job_fb_place_1;";
				action = action + "current_job_alg = job_alg_place_1;";
				for (int j = 1; j <= i-1; j++)
				{
					action = action + "job_fb_place_" + j + " = job_fb_place_" + (j+1) + ";";
					action = action + "job_alg_place_" + j + " = job_alg_place_" + (j+1) + ";";
				}
				action = action + "job_fb_place_" + i + " = 0;";
				action = action + "job_alg_place_" + i + " = 0;";
				jobQueue.addTransition(from, to, event, null, action);      
			}
			automata.addAutomaton(jobQueue);	
		}
	}

	private void makeAlgorithmExecution()
	{
		if (algMaxID > 0)
		{
			Logger.output(builderName() + ".makeAlgorithmExecution():");
			
			ExtendedAutomaton algorithmExecution = getNewAutomaton("Algorithm Execution");
				
			algorithmExecution.addInitialState("s0");
			algorithmExecution.addState("s1");
		
			algorithmExecution.addTransition("s0", "s1", "remove_job;", null, null);	

			for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
			{
				String instanceName = (String) fbIter.next();
				Integer instanceID = (Integer) basicFunctionBlocksID.get(instanceName);
				String typeName = (String) basicFunctionBlocks.get(instanceName);
				JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
				Map algorithmMap = (Map) algorithms.get(instanceName);
				// localy re-defining class attribute
				List algorithms = theType.getBasicFB().getAlgorithm();

				if (algorithmMap != null)
				{
					for (Iterator algIter = algorithms.iterator(); algIter.hasNext();)
					{
						JaxbAlgorithm curAlg = (JaxbAlgorithm) algIter.next();
						String algName = curAlg.getName();
						String algLang = curAlg.getOther().getLanguage();
						String algText = curAlg.getOther().getText();
						Integer algID = (Integer) algorithmMap.get(algName);
						
						String from = "";
						String to = "";
						String event = "";
						String guard = "";
						String action = "";
						int nameCounter = 2;	
						
						if (algLang.toLowerCase().equals("java"))
						{
							from = "s1";
							to = "s" + nameCounter;
							nameCounter++;
							algorithmExecution.addState(to);
							event = "execute_" + algName + "_" + instanceName + ";";
							guard = "current_job_fb == " + instanceID;
							guard = guard + " & current_job_alg == " + algID;
							algorithmExecution.addTransition(from, to, event, guard, null);
							from = to;

							to = "s" + nameCounter;
							nameCounter++;
							algorithmExecution.addState(to);
							event = "copy_variables_" + algName + "_" + instanceName + ";";
							algorithmExecution.addTransition(from, to, event, null, null);
							from = to;
												
							// get the variables and make identifier map for translation
							Map identifierMap = new HashMap();
							Map reverseIdentifierMap = new HashMap();
							if (theType.getInterfaceList().isSetInputVars())
							{
								List inputVars = theType.getInterfaceList().getInputVars().getVarDeclaration();
								for (Iterator iter = inputVars.iterator(); iter.hasNext();)
								{
									VarDeclaration curVar = (VarDeclaration) iter.next();
									String curVarName = curVar.getName();
									identifierMap.put(curVarName, "alg_data_" + curVarName + "_" + algName + "_" + instanceName);
									reverseIdentifierMap.put(curVarName, "data_" + curVarName + "_" + instanceName);
								}
							}		
							if (theType.getInterfaceList().isSetOutputVars())
							{
								List outputVars = theType.getInterfaceList().getOutputVars().getVarDeclaration();
								for (Iterator iter = outputVars.iterator(); iter.hasNext();)
								{
									VarDeclaration curVar = (VarDeclaration) iter.next();
									String curVarName = curVar.getName();
									identifierMap.put(curVarName, "alg_data_" + curVarName + "_" + algName + "_" + instanceName);
									reverseIdentifierMap.put(curVarName, "data_" + curVarName + "_" + instanceName);
								}
							}
							if (theType.getBasicFB().isSetInternalVars())
							{
								List internalVars = theType.getBasicFB().getInternalVars().getVarDeclaration();
								for (Iterator iter = internalVars.iterator(); iter.hasNext();)
								{
									VarDeclaration curVar = (VarDeclaration) iter.next();
									String curVarName = curVar.getName();
									identifierMap.put(curVarName, "alg_internal_" + curVarName + "_" + algName + "_" + instanceName);
									reverseIdentifierMap.put(curVarName, "internal_" + curVarName + "_" + instanceName);
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
						
							// get all identifiers			
							Finder finder = new Finder(algSyntaxTree);
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
						
							//get the local alg vars
							to = "s" + nameCounter;
							nameCounter++;
							algorithmExecution.addState(to);					
							event = "get_variables_" + algName + "_" + instanceName + ";";
							action = "";
							for (Iterator iter = algorithmIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								String algVar = (String) identifierMap.get(curIdent);
								String blockVar = (String) reverseIdentifierMap.get(curIdent);
								action = action + blockVar + " = " + algVar + ";";
							}
							algorithmExecution.addTransition(from, to, event, null, action);
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

	void makeBasicFBEventQueue(String fbName)
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
 					eventQueue.addState(to,false,false);
					event = "receive_event_" + eventName + "_" + fbName + ";";
					eventQueue.addTransition(from, to, event, null, null);
					
					from = to;
					to = "s" + (places + nameCounter);
					nameCounter++;
					eventQueue.addState(to,false,false);
					event = "queue_event_" + eventName + "_" + fbName + ";";
					guard = null;
					action = "event_place_" + i + "_" + fbName + " = " + eventID + ";";
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
										"data_place_" + i + "_" + curWith + "_" + fbName + 
										" = " + new Integer(fromSignal) + ";";
								}
								else
								{
									// instance data connection
									action = action + 
										"data_place_" + i + "_" + curWith + "_" + fbName + 
										" = data_" + fromSignal + "_" + fromInstance + ";";
								}
							}
						}
					}
					eventQueue.addTransition(from, to, event, guard, action);
					
					from = to;
					to = "s" + (places + nameCounter);
					nameCounter++;
					eventQueue.addState(to,false,false);
					event = "submit_fb_" + fbName + ";";
					eventQueue.addTransition(from, to, event, null, null);
					
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
					eventQueue.addState(to,false,false);
					event = "select_event_" + fbName + ";";
					guard = "event_place_1_" + fbName + " == " + eventID;
					action = "event_" + eventName + "_" + fbName + " = 1;";
					// move events in the queue
					for (int j = 1; j <= i-1; j++)
					{
						action = action + "event_place_" + j + "_" + fbName +  " = event_place_" + (j+1) + "_" + fbName + ";";
					}
					action = action + "event_place_" + i + "_" + fbName +  " = 0;";
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
									"data_" + curWith + "_" + fbName + " = data_place_" + i + "_" + curWith + "_" + fbName + ";";
							}
						}
						// move the queue
						for (Iterator withIter = withData.iterator(); withIter.hasNext();)
						{
							String curWith = ((With) withIter.next()).getVar();
							if (isDataConnected(fbName, curWith))
							{														
								for (int j = 1; j <= i-1; j++)
								{
									action = action + 
										"data_place_" + j + "_" + curWith + "_" + fbName + " = data_place_" + (j+1) + "_" + curWith + "_" + fbName + ";";						
								}
								action = action + 
									"data_place_" + i + "_" + curWith + "_" + fbName + " = 0;";
							}
						}
					}
					eventQueue.addTransition(from, to, event, guard, action);
					
					from = to;
					to = "s" + (i-1);
					event = "reset_event_" + eventName + "_" + fbName + ";";
					action = "event_" + eventName + "_" + fbName + " = 0;";
					eventQueue.addTransition(from, to, event, null, action);
				}
			}
		}		
		automata.addAutomaton(eventQueue);	
	}

	void makeECStateBranch(ExtendedAutomaton ecc, String fbName, String ecStateName, String prevStateName, List ecStates, List ecTransitions, Set visitedECStates, int level, Map identifierMap)
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
							event = "queue_job_" + actionAlgorithm + "_" + fbName + ";";
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

	void makeBasicFBAlgorithms(String fbName)
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
				Map reverseIdentifierMap = new HashMap();
				if (theType.getInterfaceList().isSetInputVars())
				{
					List inputVars = theType.getInterfaceList().getInputVars().getVarDeclaration();
					for (Iterator iter = inputVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "alg_data_" + curVarName + "_" + algName + "_" + fbName);
						reverseIdentifierMap.put("alg_data_" + curVarName + "_" + algName + "_" + fbName, "data_" + curVarName + "_" + fbName);
					}
				}		
				if (theType.getInterfaceList().isSetOutputVars())
				{
					List outputVars = theType.getInterfaceList().getOutputVars().getVarDeclaration();
					for (Iterator iter = outputVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "alg_data_" + curVarName + "_" + algName + "_" + fbName);
						reverseIdentifierMap.put("alg_data_" + curVarName + "_" + algName + "_" + fbName, "data_" + curVarName + "_" + fbName);
					}
				}
				if (theType.getBasicFB().isSetInternalVars())
				{
					List internalVars = theType.getBasicFB().getInternalVars().getVarDeclaration();
					for (Iterator iter = internalVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "alg_internal_" + curVarName + "_" + algName + "_" + fbName);
						reverseIdentifierMap.put("alg_internal_" + curVarName + "_" + algName + "_" + fbName, "internal_" + curVarName + "_" + fbName);
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
				
				// make alg local model variables
				for (Iterator iter = algorithmIdents.iterator(); iter.hasNext();)
				{
					String curIdent = (String) iter.next();
					curAlgModel.addIntegerVariable(curIdent, intVarMinValue, intVarMaxValue, 0, 0);
				}
				
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

				//get the block vars
				to = "s" + nameCounter;
				nameCounter++;
				curAlgModel.addState(to);
				event = "copy_variables_" + algName + "_" + fbName + ";";
				// for each assignment var
				action = "";
				for (Iterator iter = algorithmIdents.iterator(); iter.hasNext();)
				{
					String curIdent = (String) iter.next();
					String blockVar = (String) reverseIdentifierMap.get(curIdent);
					action = action + curIdent + " = " + blockVar + ";";
				}
				curAlgModel.addTransition(from, to, event, null, action);
				from = to;
				
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

				to = "s" + nameCounter;
				curAlgModel.addState(to);
				nameCounter++;
				event = "get_variables_" + algName + "_" + fbName + ";";
				curAlgModel.addTransition(from, to, event, null, null);
				from = to;

				
				to = "s0";
				event = "finished_execution_" + algName + "_" + fbName + ";";
				curAlgModel.addTransition(from, to, event, null, null);
				
				automata.addAutomaton(curAlgModel);	
			}
		}
	}
}
