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

class SequentialExecModelBuilder 
	extends ExecModelBuilder
	implements ModelBuilder
{

	SequentialExecModelBuilder(Map<String, String> arguments)
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
		
		for (Iterator fbIter = basicFunctionBlocks.keySet().iterator(); fbIter.hasNext();)
		{
			String fbName = (String) fbIter.next();
			String typeName = (String) basicFunctionBlocks.get(fbName);
			
			makeBasicFB(fbName);
		}
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
				if (theType.getInterfaceList().isSetInputVars())
				{
					List inputVars = theType.getInterfaceList().getInputVars().getVarDeclaration();
					for (Iterator iter = inputVars.iterator(); iter.hasNext();)
					{
						VarDeclaration curVar = (VarDeclaration) iter.next();
						String curVarName = curVar.getName();
						identifierMap.put(curVarName, "data_" + curVarName + "_" + fbName);
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

				to = "s0";
				event = "finished_execution_" + algName + "_" + fbName + ";";
				curAlgModel.addTransition(from, to, event, null, null);
				
				automata.addAutomaton(curAlgModel);	
			}
		}
	}
}
