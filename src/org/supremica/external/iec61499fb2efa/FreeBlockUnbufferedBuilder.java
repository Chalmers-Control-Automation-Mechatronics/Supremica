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

class FreeBlockUnbufferedBuilder
	extends FreeBlockExecModelBuilder
	implements ModelBuilder
{

	FreeBlockUnbufferedBuilder(Properties arguments)
	{
		super(arguments);
	}

	public void buildModels()
	{
		Logger.output(builderName() + ".buildModels()");

 		automata = new ExtendedAutomata(theSystem.getName(), expandTransitions);
		
		makeStartup();
		
		makeEventExecution();
		
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
			makeBasicFB(fbName);
		}
	}

	void makeEventExecution()
	{
		Logger.output(builderName() + ".makeEventExecution():");

		ExtendedAutomaton eventExecution = getNewAutomaton("Event Execution");

		eventExecution.addInitialState("s0");
		eventExecution.addState("s1",false,false);
		eventExecution.addTransition("s0", "s1", "select_fb;", null, null);	

		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();

			int nameCounter = 2;
			
			String from = "s1";
			String to = "s" + nameCounter;
			nameCounter++;
			eventExecution.addState(to,false,false);
			String event = "handle_event_" + instanceName + ";";
			eventExecution.addControllableTransition(from, to, event, null, null);

			from = to;
			to = "s0";
			event = "no_event_" + instanceName + ";";
			eventExecution.addTransition(from, to, event, null, null);
		}
		automata.addAutomaton(eventExecution);
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

		// first queue element pointer
		eventQueue.addIntegerVariable("event_" + fbName + "_first", 1, places, 1, 0);
		
		String from = "";
		String to = "";
		String event = "";
		String guard = "";
		String action = "";
		int nameCounter = 1;
		
		eventQueue.addInitialState("s0");

		from = "s0";
		to = "s0";
		event = "no_event_" + fbName + ";";
		eventQueue.addTransition(from, to, event, null, null);

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
					eventQueue.addState(to,false,false);
					event = "select_event_" + fbName + ";";
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
				}
			}
		}		
		automata.addAutomaton(eventQueue);	
	}
}

