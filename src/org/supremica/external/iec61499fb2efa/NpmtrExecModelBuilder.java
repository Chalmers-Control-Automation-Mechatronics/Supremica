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

class NpmtrExecModelBuilder 
	extends FreeEventExecModelBuilder
	implements ModelBuilder
{
	
	NpmtrExecModelBuilder(Properties arguments)
	{
		super(arguments);
	}

	public void buildModels()
	{
		Logger.output(builderName() + ".buildModels()");

 		automata = new ExtendedAutomata(theSystem.getName(), expandTransitions);

		makeStartup();

		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
			makeBasicFB(fbName);
		}
	}

	void makeStartup()
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
			if (stopInstance != null)
			{
				startup.addState(to,false,false);
			}
			else
			{
				startup.addAcceptingState(to);				
			}
			event = "receive_event_" + cntSignal + "_" + cntFB + ";";
			startup.addTransition(from, to, event, null, null);

			if (stopInstance != null)
			{
				from = to;
				to = "s3" ;
				startup.addState(to,true,false);
				event = "receive_event_STOP_" + stopInstance + ";";
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

    // non-rentrant block
	void makeBasicFBEventQueue(String fbName)
	{
		Logger.output("Event Receiving", 1);

		String typeName = (String) basicFunctionBlocks.get(fbName);
		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
		List eventInputList = (List) ((EventInputs) ((InterfaceList) theType.getInterfaceList()).getEventInputs()).getEvent();
		
		ExtendedAutomaton eventQueue = getNewAutomaton(fbName + ": Event Receiving");
		
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

		for (Iterator evIter = eventInputList.iterator(); evIter.hasNext();)
		{
			
			JaxbEvent curEvent = (JaxbEvent) evIter.next();
			String eventName = curEvent.getName();
			int eventID = ((Integer) ((Map) events.get(fbName)).get(eventName)).intValue();
				
			// Transitons when receiving event
			if (isEventConnected(fbName, eventName))
			{					
				from = "s0";
				to = "s" + nameCounter;
				nameCounter++;
				eventQueue.addState(to,false,false);
				event = "receive_event_" + eventName + "_" + fbName + ";";
				eventQueue.addTransition(from, to, event, null, null);
					
				from = to;
				to = "s" + nameCounter;
				nameCounter++;
				eventQueue.addState(to,false,false);
				event = "set_event_" + eventName + "_" + fbName + ";";
				action = "event_" + eventName + "_" + fbName + " = 1;";
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
									"data_" + curWith + "_" + fbName + " = " + new Integer(fromSignal) + ";";
							}
							else
							{
								// instance data connection
								action = action + 
									"data_" + curWith + "_" + fbName + " = data_" + fromSignal + "_" + fromInstance + ";";
							}
						}
					}
				}
				eventQueue.addTransition(from, to, event, guard, action);

				from = to;
				to = "s" + nameCounter;
				nameCounter++;
				eventQueue.addState(to,false,false);
				event = "handle_event_" + fbName + ";";
				eventQueue.addTransition(from, to, event, null, null);
				
				from = to;
				to = "s" + nameCounter;
				nameCounter++;
				eventQueue.addState(to,false,false);
				event = "select_event_" + fbName + ";";
				eventQueue.addTransition(from, to, event, null, null);

				from = to;
				to = "s" + nameCounter;
				nameCounter++;
 				eventQueue.addState(to,false,false);
				event = "reset_event_" + eventName + "_" + fbName + ";";
				action = "event_" + eventName + "_" + fbName + " = 0;";
				eventQueue.addTransition(from, to, event, null, action);

                String backTo = to;
                for (Iterator eventIter = eventInputList.iterator(); eventIter.hasNext();)
                {			
                    JaxbEvent theEvent = (JaxbEvent) eventIter.next();
                    String theEventName = theEvent.getName();
                    if (isEventConnected(fbName, theEventName))
                    {					
                        from = to;
                        to = "s" + nameCounter;
                        nameCounter++;
                        eventQueue.addState(to,false,false);
                        event = "receive_event_" + theEventName + "_" + fbName + ";";
                        eventQueue.addTransition(from, to, event, null, null);

                        from = to;
                        to = "s" + nameCounter;
                        nameCounter++;
                        eventQueue.addState(to,false,false);
                        event = "already_handling_event_" + fbName + ";";
                        eventQueue.addTransition(from, to, event, null, null);

                        from = to;
                        to = backTo;
                        nameCounter++;
                        eventQueue.addState(to,false,false);
                        event = "received_event_" + theEventName + "_" + fbName + ";";
                        eventQueue.addTransition(from, to, event, null, null);
                    }
                }

 				from = to;
 				to = "s" + nameCounter;
 				nameCounter++;
 				eventQueue.addState(to,false,false);
 				event = "handling_event_done_" + fbName + ";";
 				eventQueue.addTransition(from, to, event, null, null);

 				from = to;
 				to = "s0";
 				event = "received_event_" + eventName + "_" + fbName + ";";
 				eventQueue.addTransition(from, to, event, null, null);
			}
		}		
		automata.addAutomaton(eventQueue);	
	}

    // rentrant block
// 	void makeBasicFBEventQueue(String fbName)
// 	{
// 		Logger.output("Event Receiving", 1);

// 		String typeName = (String) basicFunctionBlocks.get(fbName);
// 		JaxbFBType theType = (JaxbFBType) fbTypes.get(typeName);
// 		List eventInputList = (List) ((EventInputs) ((InterfaceList) theType.getInterfaceList()).getEventInputs()).getEvent();
		
// 		ExtendedAutomaton eventQueue = getNewAutomaton(fbName + ": Event Receiving");
		
// 		// event input variables
// 		if (theType.getInterfaceList().isSetEventInputs())
// 		{
// 			final List eventInputs = theType.getInterfaceList().getEventInputs().getEvent();
// 			for (Iterator eventInputsIter = eventInputs.iterator(); eventInputsIter.hasNext();)
// 			{
// 				JaxbEvent curEventInput = (JaxbEvent) eventInputsIter.next();
// 				String curEventInputName = curEventInput.getName();
// 				eventQueue.addIntegerVariable("event_" + curEventInputName + "_" + fbName, 0, 1, 0, 0);
// 			}
// 		}

// 		// data input variables
// 		if (theType.getInterfaceList().isSetInputVars())
// 		{
// 			final List dataInputs = theType.getInterfaceList().getInputVars().getVarDeclaration();
// 			for (Iterator dataInputsIter = dataInputs.iterator(); dataInputsIter.hasNext();)
// 			{
// 				VarDeclaration curDeclaration = (VarDeclaration) dataInputsIter.next();
// 				String curDataInputName = curDeclaration.getName();
// 				if (isDataConnected(fbName, curDataInputName))
// 				{
// 					String curDataType =  curDeclaration.getType();
// 					if (curDataType.toLowerCase().equals("int"))
// 					{
// 						// get possible constant data value
// 						String dataCnt = (String) ((Map) dataConnections.get(fbName)).get(curDataInputName);
// 						if (!getInstanceName(dataCnt).equals(""))
// 						{
// 							Logger.output(Logger.DEBUG, "Making non constant data variable", 2);
// 							eventQueue.addIntegerVariable("data_" + curDataInputName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
// 						}
// 						else
// 						{
// 							Integer dataValue = new Integer(getSignalName(dataCnt));
// 							Logger.output(Logger.DEBUG, "Making constant data variable data_" + curDataInputName + "_" + fbName + " with value " + dataValue, 2);
// 							eventQueue.addIntegerVariable("data_" + curDataInputName + "_" + fbName, intVarMinValue, intVarMaxValue, dataValue.intValue(), 0);
// 						}
// 					}
// 					else if (curDataType.toLowerCase().equals("bool"))
// 					{
// 						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 1);
// 						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
// 						exit(1);
// 					}
// 					else if (curDataType.toLowerCase().equals("real"))
// 					{
// 						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 1);
// 						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
// 						exit(1);
// 					}
// 					else if (curDataType.toLowerCase().equals("string"))
// 					{
// 						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 1);
// 						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
// 						exit(1);
// 					}
// 					else if (curDataType.toLowerCase().equals("object"))
// 					{
// 						Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 1);
// 						Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataInputName, 2);
// 						exit(1);
// 					}
// 				}
// 			}
// 		}
		
// 		// data output variables
// 		// written only by algorithms
// 		if (theType.getInterfaceList().isSetOutputVars())
// 		{
// 			final List dataOutputs = theType.getInterfaceList().getOutputVars().getVarDeclaration();
// 			for (Iterator dataOutputsIter = dataOutputs.iterator(); dataOutputsIter.hasNext();)
// 			{
// 				VarDeclaration curDeclaration = (VarDeclaration) dataOutputsIter.next();
// 				String curDataOutputName = curDeclaration.getName();
// 				String curDataType =  curDeclaration.getType();
// 				if (curDataType.toLowerCase().equals("int"))
// 				{
// 					eventQueue.addIntegerVariable("data_" + curDataOutputName + "_" + fbName, intVarMinValue, intVarMaxValue, 0, 0);
// 				}
// 				else if (curDataType.toLowerCase().equals("bool"))
// 				{
// 					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: BOOL", 1);
// 					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
// 					exit(1);
// 				}
// 				else if (curDataType.toLowerCase().equals("real"))
// 				{
// 					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: REAL", 1);
// 					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
// 					exit(1);
// 				}
// 				else if (curDataType.toLowerCase().equals("string"))
// 				{
// 					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: STRING", 1);
// 					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
// 					exit(1);
// 				}
// 				else if (curDataType.toLowerCase().equals("object"))
// 				{
// 					Logger.output(Logger.ERROR, "Error: Unsupported input data variable type: OBJECT", 1);
// 					Logger.output(Logger.ERROR, "Variable name: " + fbName + "_" + curDataOutputName, 2);
// 					exit(1);
// 				}
// 			}
// 		}

// 		String from = "";
// 		String to = "";
// 		String event = "";
// 		String guard = "";
// 		String action = "";

// 		int nameCounter = 1;

//         Map<String,String> eventStateMap = new HashMap<String,String>();

// 		eventQueue.addInitialState("s0");
        
// 		for (Iterator evIter = eventInputList.iterator(); evIter.hasNext();)
// 		{
			
// 			JaxbEvent curEvent = (JaxbEvent) evIter.next();
// 			String eventName = curEvent.getName();
// 			int eventID = ((Integer) ((Map) events.get(fbName)).get(eventName)).intValue();
				
// 			// Transitons when receiving event
// 			if (isEventConnected(fbName, eventName))
// 			{					
// 				from = "s0";
// 				to = "s" + nameCounter;
// 				nameCounter++;
// 				eventQueue.addState(to,false,false);
// 				event = "receive_event_" + eventName + "_" + fbName + ";";
// 				eventQueue.addTransition(from, to, event, null, null);
					
// 				from = to;
// 				to = "s" + nameCounter;
// 				nameCounter++;
// 				eventQueue.addState(to,false,false);
// 				event = "set_event_" + eventName + "_" + fbName + ";";
// 				action = "event_" + eventName + "_" + fbName + " = 1;";
// 				if (curEvent.isSetWith())
// 				{
// 					List withData = curEvent.getWith();
// 					for (Iterator withIter = withData.iterator(); withIter.hasNext();)
// 					{
// 						String curWith = ((With) withIter.next()).getVar();
// 						if (isDataConnected(fbName, curWith))
// 						{														
// 							String cntFrom = (String) ((Map) dataConnections.get(fbName)).get(curWith);
// 							String fromInstance = getInstanceName(cntFrom);
// 							String fromSignal = getSignalName(cntFrom);				
// 							if (fromInstance.equals(""))
// 							{
// 								// constant data connection
// 								action = action + 
// 									"data_" + curWith + "_" + fbName + " = " + new Integer(fromSignal) + ";";
// 							}
// 							else
// 							{
// 								// instance data connection
// 								action = action + 
// 									"data_" + curWith + "_" + fbName + " = data_" + fromSignal + "_" + fromInstance + ";";
// 							}
// 						}
// 					}
// 				}
// 				eventQueue.addTransition(from, to, event, guard, action);

//                 eventStateMap.put(eventName + "to",from);

// 				from = to;
// 				to = "s" + nameCounter;
// 				nameCounter++;
// 				eventQueue.addState(to,false,false);
// 				event = "handle_event_" + fbName + ";";
// 				eventQueue.addTransition(from, to, event, null, null);
				
// 				from = to;
// 				to = "s" + nameCounter;
// 				nameCounter++;
// 				eventQueue.addState(to,false,false);
// 				event = "select_event_" + fbName + ";";
// 				eventQueue.addTransition(from, to, event, null, null);

// 				from = to;
// 				to = "s" + nameCounter;
// 				nameCounter++;
//  				eventQueue.addState(to,false,false);
// 				event = "reset_event_" + eventName + "_" + fbName + ";";
// 				action = "event_" + eventName + "_" + fbName + " = 0;";
// 				eventQueue.addTransition(from, to, event, null, action);
                
//                 eventStateMap.put(eventName + "from",to);

//  				from = to;
//  				to = "s" + nameCounter;
//  				nameCounter++;
//  				eventQueue.addState(to,false,false);
//  				event = "handling_event_done_" + fbName + ";";
//  				eventQueue.addTransition(from, to, event, null, null);

//  				from = to;
//  				to = "s0";
//  				event = "received_event_" + eventName + "_" + fbName + ";";
//  				eventQueue.addTransition(from, to, event, null, null);
// 			}
// 		}		

//         for (Iterator evIter = eventInputList.iterator(); evIter.hasNext();)
//         {			
//             JaxbEvent curEvent = (JaxbEvent) evIter.next();
//             String curEventName = curEvent.getName();
//             if (isEventConnected(fbName, curEventName))
//             {					
//                 from = eventStateMap.get(curEventName + "from");
//                 for (Iterator eventIter = eventInputList.iterator(); eventIter.hasNext();)
//                 {			
//                     JaxbEvent theEvent = (JaxbEvent) eventIter.next();
//                     String theEventName = theEvent.getName();
//                     if (isEventConnected(fbName, theEventName))
//                     {					
//                         to = eventStateMap.get(theEventName + "to");
//                         event = "receive_event_" + theEventName + "_" + fbName + ";";
//                         eventQueue.addTransition(from, to, event, null, null);
//                     }
//                 }
//             }
//         }
// 		automata.addAutomaton(eventQueue);	
// 	}

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
}
