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

class CyclicExecModelBuilder 
	extends FreeExecModelBuilder
	implements ModelBuilder
{

	CyclicExecModelBuilder(Properties arguments)
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
		
		String from = "";
		String to = "s0";
		String event = ""; 
		
		int nameCounter = 1;
		
		for (Iterator iter = basicFunctionBlocks.keySet().iterator(); iter.hasNext();)
		{
			String instanceName = (String) iter.next();
			
			from = to;
			to = "s" + nameCounter;
			nameCounter++;
			eventExecution.addState(to,false,false);
			event = "select_fb;";
			eventExecution.addTransition(from, to, event, null, null);
			
			from = to;
			to = "s" + nameCounter;
			nameCounter++;
			eventExecution.addState(to,false,false);
			event = "handle_event_" + instanceName + ";";
			eventExecution.addTransition(from, to, event, null, null);

			from = to;
			if (iter.hasNext())
			{
				to = "s" + nameCounter;
				nameCounter++;
				eventExecution.addState(to);
			}
			else
			{
				to = "s0";
			}
			//event = "handling_event_done_" + instanceName + ";";
			//eventExecution.addTransition(from, to, event, null, null);
			
			event = "no_event_" + instanceName + ";";
			eventExecution.addTransition(from, to, event, null, null);
		}
		automata.addAutomaton(eventExecution);
	}

	void makeBasicFBEventHandling(String fbName)
	{
		Logger.output("Event Handling", 1);
		
		ExtendedAutomaton eventHandling = getNewAutomaton(fbName + ": Event Handling");

		eventHandling.addInitialState("s0");
		eventHandling.addState("s1");
		eventHandling.addState("s2");
		eventHandling.addState("s3");
		eventHandling.addState("s4");

		String from = "s0";
		String to = "s1";
		String event = "handle_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s1";
		to = "s0";
		event = "no_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s1";
		to = "s2";
		event = "select_event_" + fbName + ";";
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
		to = "s4";
		event = "handling_event_done_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s4";
		to = "s2";
		event = "select_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		from = "s4";
		to = "s0";
		event = "no_event_" + fbName + ";";
		eventHandling.addTransition(from, to, event, null, null);

		automata.addAutomaton(eventHandling);
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
