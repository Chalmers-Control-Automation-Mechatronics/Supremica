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
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.external.iec61499fb2efa;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.Reader;
import java.io.StringReader;
import java_cup.runtime.Scanner;

import net.sourceforge.fuber.model.Variables;
import net.sourceforge.fuber.model.IntegerVariable;

import net.sourceforge.fuber.model.interpreters.Finder;
import net.sourceforge.fuber.model.interpreters.Evaluator;
import net.sourceforge.fuber.model.interpreters.Printer;
import net.sourceforge.fuber.model.interpreters.efa.Lexer;
import net.sourceforge.fuber.model.interpreters.efa.Parser;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Goal;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.StatementList;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Expression;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Identifier;

import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.subject.base.ListSubject;

import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.model.module.*;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.TypeMismatchException;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;


public class ExtendedAutomata
{

	private static ModuleSubjectFactory factory;
	private IdentifierSubject identifier;
	private ModuleSubject module;
	private boolean expand;

	private static ExpressionParser parser;

	public ExtendedAutomata(String name, boolean expand) 
	{
		factory = ModuleSubjectFactory.getInstance();

		identifier = factory.createSimpleIdentifierProxy(name);

		module = new ModuleSubject(identifier.getName(), null);

		module.getEventDeclListModifiable().add(factory.createEventDeclProxy(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION));

		this.expand = expand;
		
		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
	}

	protected ModuleSubject getModule()
	{
		return module;
	}

	public void addEvent(String name)
	{
		addEvent(name,"controllable");
	}
	
	public void addEvent(String name, String kind)
	{
		if (kind.equals("controllable"))
		{
			module.getEventDeclListModifiable().add(factory.createEventDeclProxy(name, EventKind.CONTROLLABLE));
		}
		else if (kind.equals("uncontrollable"))
		{
			module.getEventDeclListModifiable().add(factory.createEventDeclProxy(name, EventKind.UNCONTROLLABLE));
		}
	}


	public void addAutomaton(ExtendedAutomaton automaton)
	{
		module.getComponentListModifiable().add(automaton.getComponent());
	}

	public static void expandTransitions(ModuleSubject module)
	{
		
		ModelMaker.output("ExtendedAutomata.expandTransitions(): Expanding transitions.");
		
		// get all component variables
		// and put them in a map
		ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Making module variables map.");
		Map moduleVariables = new HashMap();
		for (Iterator compIter = module.getComponentList().iterator(); compIter.hasNext();)
		{
			SimpleComponentProxy curComponent = (SimpleComponentProxy) compIter.next();
			for (Iterator varIter = curComponent.getVariables().iterator(); varIter.hasNext();)
			{
				VariableProxy curVar = (VariableProxy) varIter.next();
				String curVarName = curVar.getName();
				if (moduleVariables.keySet().contains(curVarName))
				{
					ModelMaker.output(ModelMaker.WARN, "ExtendedAutomata.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
					ModelMaker.output(ModelMaker.WARN, "Variable name: " + curVarName, 1);
				}
				ModelMaker.output(ModelMaker.DEBUG, curVar.getName(), 1);
				moduleVariables.put(curVar.getName(), curVar);
			}
		}
		
		// expand transitions for all components in the module
		for (Iterator compIter = module.getComponentList().iterator(); compIter.hasNext();)
		{
			SimpleComponentProxy curComponent = (SimpleComponentProxy) compIter.next();
			String curComponentName = curComponent.getName();
			ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Component", 1);
			ModelMaker.output(ModelMaker.DEBUG, curComponentName, 2);

			ListSubject edges = ((GraphSubject) curComponent.getGraph()).getEdgesModifiable();
			List removeEdges = new LinkedList();
			List addEdges = new LinkedList();

			for (Iterator edgeIter = edges.iterator(); edgeIter.hasNext();)
			{
				EdgeSubject curEdge = (EdgeSubject) edgeIter.next();

				NodeProxy source = curEdge.getSource();
				NodeProxy target = curEdge.getTarget();

				LabelBlockProxy curLabel = curEdge.getLabelBlock();

				GuardActionBlockProxy curBlock = curEdge.getGuardActionBlock();

				// do expansion only if guard and actions block is present
				if (curBlock != null)
				{
					ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Expanding edge", 2);

					// get guards
					List curGuards = curBlock.getGuards();
					String guardText = "(";
					if (curGuards.size() > 0 )
					{
						ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Guard", 3);
						for (Iterator iter = curGuards.iterator(); iter.hasNext();)
						{
							guardText = guardText + ((SimpleExpressionProxy) iter.next()).getPlainText();
							if (iter.hasNext())
							{
								guardText = guardText + ") & (";
							}
							else
							{
								guardText = guardText + ")";
							}
						}
						ModelMaker.output(ModelMaker.DEBUG, guardText, 4);
					}

					// get actions
					List curActions = curBlock.getActions();
					String actionsText = "";
					if (curActions.size() > 0 )
					{
						ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Actions", 3);
						for (Iterator iter = curActions.iterator(); iter.hasNext();)
						{
							actionsText = actionsText + ((BinaryExpressionProxy) iter.next()).getPlainText() + "; ";
						}
						ModelMaker.output(ModelMaker.DEBUG, actionsText, 4);
					}

					// parse transition guard
					Goal guardSyntaxTree = null;
					if (!guardText.equals("("))
					{
						StringReader stringReader = new StringReader(guardText);
						Lexer lexer = new Lexer((Reader) stringReader);
						Parser parser = new Parser((Scanner) lexer);
						try
						{
							guardSyntaxTree = (Goal) parser.parse().value;
						}
						catch(Exception e)
						{
							ModelMaker.output(ModelMaker.ERROR, "ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							ModelMaker.output(ModelMaker.ERROR, "\t automaton: " + curComponentName);
							ModelMaker.output(ModelMaker.ERROR, "\t from: " + source.getName());
							ModelMaker.output(ModelMaker.ERROR, " to: " + target.getName());
							ModelMaker.output(ModelMaker.ERROR, "\t guard: " + guardText);
							ModelMaker.output(ModelMaker.ERROR, "\t action: " + actionsText);
							System.exit(1);
						}
						
						ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Succesfully parsed guard.", 3);
					}
					
					// parse transition actions
					Goal actionsSyntaxTree = null;
					if (!actionsText.equals(""))
					{
						StringReader stringReader = new StringReader(actionsText);
						Lexer lexer = new Lexer((Reader) stringReader);
						Parser parser = new Parser((Scanner) lexer);
						try
						{
							actionsSyntaxTree = (Goal) parser.parse().value;
						}
						catch(Exception e)
						{
							ModelMaker.output(ModelMaker.ERROR, "ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							ModelMaker.output(ModelMaker.ERROR, "\t automaton: " + curComponentName);
							ModelMaker.output(ModelMaker.ERROR, "\t from: " + source.getName());
							ModelMaker.output(ModelMaker.ERROR, " to: " + target.getName());
							ModelMaker.output(ModelMaker.ERROR, "\t guard: " + guardText);
							ModelMaker.output(ModelMaker.ERROR, "\t action: " + actionsText);
							System.exit(1);
						}
						
						ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Succesfully parsed actions.", 3);
					}
					
					// get guard identifiers
					Set guardExpressionIdents = new LinkedHashSet();
					if (guardSyntaxTree != null)
					{
						Finder finder = new Finder(guardSyntaxTree);
						Set expressionIdents = finder.getExpressionIdentifiers();
						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!guardExpressionIdents.contains(curIdent))
							{
								guardExpressionIdents.add(curIdent);
							}
						}						
					}
										
					// get actions identifiers
					Set actionsAssignmentIdents = new LinkedHashSet();
					// expression idents found in actions that are not in the guard
					Set actionsExpressionIdents = new LinkedHashSet();
					if (actionsSyntaxTree != null)
					{
						Finder finder = new Finder(actionsSyntaxTree);
						Set assignmentIdents = finder.getAssignmentIdentifiers();
						for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!actionsAssignmentIdents.contains(curIdent))
							{
								actionsAssignmentIdents.add(curIdent);
							}
						}
						Set expressionIdents = finder.getExpressionIdentifiers();
						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!guardExpressionIdents.contains(curIdent))
							{
								if (!actionsExpressionIdents.contains(curIdent))
								{
									actionsExpressionIdents.add(curIdent);
								}
							}
						}
					}

// 					if (expressionIdents.size() > 0)
// 					{
// 						// make symbols
// 						// currently only integer variables
// 						Variables symbols = new Variables();
// 						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
// 						{
// 							String curIdent = (String) iter.next();
// 							if (!symbols.contains(curIdent))
// 							{
// 								symbols.addVariable(curIdent, new IntegerVariable());
// 							}
// 						}
// 						if (assignmentIdents.size() > 0)
// 						{
// 							for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
// 							{
// 								String curIdent = (String) iter.next();
// 								if (!symbols.contains(curIdent))
// 								{
// 									symbols.addVariable(curIdent, new IntegerVariable());
// 								}
// 							}
// 						}
			
// 						// count through all expression identifers and evaluate guard and actions
// 						// initialize couters and upper bounds maps
// 						Map identCounters = new LinkedHashMap();
// 						Map identUpperBounds = new HashMap();
// 						Map identLowerBounds = new HashMap();
// 						ModelMaker.output(ModelMaker.DEBUG,"ExtendedAutomata.expandTransitions(): Initializing counters", 3);
// 						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
// 						{
// 							String curIdent = (String) iter.next();
// 							VariableProxy curModuleVariable = (VariableProxy) moduleVariables.get(curIdent);

// 							ModelMaker.output(ModelMaker.DEBUG, curIdent, 4);

// 							Integer lowerBound = VariableHelper.getLowerBound(curModuleVariable);
// 							Integer upperBound = VariableHelper.getUpperBound(curModuleVariable);	
// 							if (!identCounters.keySet().contains(curIdent))
// 							{
// 								identCounters.put(curIdent, lowerBound);
// 							}
// 							if (!identUpperBounds.keySet().contains(curIdent))
// 							{
// 								identUpperBounds.put(curIdent, upperBound);
// 							}
// 							if (!identLowerBounds.keySet().contains(curIdent))
// 							{
// 								identLowerBounds.put(curIdent, lowerBound);
// 							}
// 						}
					
// 						// count up all identifiers ant evaluate guard and actions
// 						Evaluator evaluator = null;
// 						Variables updatedSymbols = null;
// 						boolean keepCounting = true;
// 						ModelMaker.output(ModelMaker.DEBUG, "ExtendedAutomata.expandTransitions(): Counting", 3);
// 						while (keepCounting)
// 						{
// 							ModelMaker.output(ModelMaker.DEBUG, identCounters.toString(), 4);
						
// 							// set expression symbols to counters and make gaurd addition
// 							String addToGuard = "";
// 							for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
// 							{
// 								String curIdent = (String) iter.next();
// 								int value = ((Integer) identCounters.get(curIdent)).intValue();
// 								((IntegerVariable) symbols.getVariable(curIdent)).setValue(value);
// 								addToGuard = addToGuard + curIdent + " == " + value;
// 								if (iter.hasNext())
// 								{
// 									addToGuard = addToGuard + " & ";
// 								}
// 							}
						
// 							evaluator = new Evaluator(symbols);

// 							if (guardSyntaxTree != null)
// 							{
// 								Boolean oldGuardValue = (Boolean) evaluator.evaluate(guardSyntaxTree);
								
// 								// only if old guard is evaluated to true add the transition
// 								if (oldGuardValue)
// 								{
// 									// make new guard
// 									String guard =  addToGuard;
									
// 									// make new actions
// 									String actions = null;
// 									if (actionsSyntaxTree != null)
// 									{
// 										// evaluate the actions
// 										updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);

// 										actions = "";
// 										for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
// 										{
// 											String curIdent = (String) iter.next();
// 											int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
											
// 											actions = actions + curIdent + " = " + value + ";";
// 										}
// 									}
// 									// mark new edge for adding
// 									addEdges.add(makeTransition((NodeProxy) source.clone(), (NodeProxy) target.clone(), (LabelBlockProxy) curLabel.clone(), guard, actions));
// 								}
// 							}
// 							else if (actionsSyntaxTree != null)
// 							{
// 								// evaluate the actions
// 								updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);

// 								// make new guard
// 								String guard =  addToGuard;

// 								String actions = "";
// 								for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
// 								{
// 									String curIdent = (String) iter.next();
// 									int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
									
// 									actions = actions + curIdent + " = " + value + ";";
// 								}

// 								// mark new edge for adding
// 								addEdges.add(makeTransition((NodeProxy) source.clone(), (NodeProxy) target.clone(), (LabelBlockProxy) curLabel.clone(), guard, actions));
// 							}
							
// 							// increase ident counters
// 							List atUpperBound = new LinkedList();
// 							for (Iterator countIter = identCounters.keySet().iterator(); countIter.hasNext();)
// 							{
// 									String curIdent = (String) countIter.next();
// 									int value = ((Integer) identCounters.get(curIdent)).intValue();
// 									int upperBound = ((Integer) identUpperBounds.get(curIdent)).intValue();
									
// 									if (value < upperBound)
// 									{
// 									identCounters.put(curIdent, new Integer(value + 1));
// 									if (atUpperBound.size() > 0)
// 									{
// 										for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
// 										{
// 											String curAtBound = (String) iter.next();
// 											int lowerBound = ((Integer) identLowerBounds.get(curAtBound)).intValue();
										
// 											identCounters.put(curAtBound, new Integer(lowerBound));
// 										}
// 										atUpperBound.clear();
// 									}
// 									break;
// 								}
// 								else
// 								{
// 									atUpperBound.add(curIdent);
// 								}
// 							}
// 							// calculate keepCouting condition
// 							keepCounting = (atUpperBound.size() != identCounters.keySet().size()); 
// 						}
// 						// mark current edge for removal
// 						removeEdges.add(curEdge);
// 					}
 				}
 			}

// 			// remove old edges
// 			for (Iterator iter = removeEdges.iterator(); iter.hasNext();)
// 			{
// 				edges.remove(iter.next());
// 			}
			
// 			// add new edges
// 			for (Iterator iter = addEdges.iterator(); iter.hasNext();)
// 			{
// 				edges.add(iter.next());
// 			}
		}
	}
	
	private static EdgeSubject makeTransition(NodeProxy from, NodeProxy to, LabelBlockProxy labelBlock, String guardIn, String actionIn)
	{
		// make GuardActionSubject
		// Get guard ...
		SimpleExpressionSubject guard = null;
		try
		{
			String guardText = guardIn;
			if (guardText != null && !guardText.trim().equals(""))
			{
				guard = (SimpleExpressionSubject) parser.parse(guardText, Operator.TYPE_BOOLEAN);
			}
		}
		catch (ParseException exception)
		{
			System.out.println("ExtendedAutomaton.addNormalTransition(): Syntax error in guard!");
			System.out.println("\t guard: " + guardIn);
			System.out.println("\t action: " + actionIn);
			return null;
		}
		// Get actions ...
		List<BinaryExpressionSubject> actions = null;
		String actionText = actionIn;
		if (actionText != null && !actionText.trim().equals(""))
		{
			String[] texts = actionIn.split(";");
			actions = new ArrayList<BinaryExpressionSubject>(texts.length);
			for (String text : texts)
			{
				if (text.length() > 0)
				{
					try
					{
						SimpleExpressionSubject action = (SimpleExpressionSubject) parser.parse(text);
						if (!(action instanceof BinaryExpressionSubject))
						{
							throw new TypeMismatchException(action, "ACTION");
						}
						BinaryExpressionSubject binaction = (BinaryExpressionSubject) action;
						actions.add(binaction);
					}
					catch (ParseException exception)
					{
						System.out.println("ExtendedAutomaton.addNormalTransition(): Syntax error in action!");
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return null;
					}
					catch (TypeMismatchException exception)
					{
						System.out.println("ExtendedAutomaton.addNormalTransition(): Type mismatch error in action!");
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return null;
					}
				}
			}
		}
			
		// Store parsed results ...
		GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
		List<SimpleExpressionSubject> blockGuards = guardActionBlock.getGuardsModifiable();
		blockGuards.clear();
		if (guard != null)
		{
			blockGuards.add(guard);
		}
		List<BinaryExpressionSubject> blockActions = guardActionBlock.getActionsModifiable();
		blockActions.clear();
		if (actions != null)
		{
			blockActions.addAll(actions);
		}
			
		EdgeSubject newEdge = factory.createEdgeProxy(from, to, labelBlock, guardActionBlock, null, null, null);

		return newEdge;
	}

	
	public void writeToFile(File file)
	{

		if (expand)
		{
			expandTransitions(module);
		}

		try
		{
			JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
