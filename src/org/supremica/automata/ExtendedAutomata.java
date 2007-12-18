//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata
//# CLASS:   ExtendedAutomata
//###########################################################################
//# $Id: ExtendedAutomata.java,v 1.4 2007-12-18 19:21:48 cengic Exp $
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.automata;

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

import net.sourceforge.fuber.model.dual.Variables;
import net.sourceforge.fuber.model.dual.IntegerVariable;

import net.sourceforge.fuber.model.interpreters.Finder;
import net.sourceforge.fuber.model.interpreters.Evaluator;
import net.sourceforge.fuber.model.interpreters.Printer;
import net.sourceforge.fuber.model.interpreters.efa.Lexer;
import net.sourceforge.fuber.model.interpreters.efa.Parser;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Goal;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.StatementList;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Expression;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Identifier;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.TypeMismatchException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;


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
		// get all component variables
		// and put them in a map
		Map moduleVariables = new HashMap();
        for (final Proxy proxy : module.getComponentList()) {
            if (proxy instanceof VariableComponentProxy) {
                final VariableComponentProxy curVar =
                    (VariableComponentProxy) proxy;
				final String curVarName = curVar.getName();
				if (moduleVariables.keySet().contains(curVarName))
				{
					System.out.println("ExtendedAutomata.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
					System.out.println("Variable name: " + curVarName);
				}
				System.out.println(curVar.getName());
				moduleVariables.put(curVar.getName(), curVar);
			}
		}
		
		// expand transitions for all components in the module
        for (final Proxy proxy : module.getComponentList()) {
            if (!(proxy instanceof SimpleComponentProxy)) {
                continue;
            }
            final SimpleComponentProxy curComponent =
                (SimpleComponentProxy) proxy;
			String curComponentName = curComponent.getName();
			System.out.println("ExtendedAutomata.expandTransitions(): Component");
			System.out.println(curComponentName);

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
					System.out.println("ExtendedAutomata.expandTransitions(): Expanding edge");

					// get guards
					List curGuards = curBlock.getGuards();
					String guardText = "(";
					if (curGuards.size() > 0 )
					{
						System.out.println("ExtendedAutomata.expandTransitions(): Guard");
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
						System.out.println(guardText);
					}
					
					// get actions
					List curActions = curBlock.getActions();
					String actionsText = "";
					if (curActions.size() > 0 )
					{
						System.out.println("ExtendedAutomata.expandTransitions(): Actions");
						for (Iterator iter = curActions.iterator(); iter.hasNext();)
						{
							actionsText = actionsText + ((BinaryExpressionProxy) iter.next()).getPlainText() + "; ";
						}
						System.out.println(actionsText);
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
							System.out.println("ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							System.out.println("\t automaton: " + curComponentName);
							System.out.println("\t from: " + source.getName());
							System.out.println(" to: " + target.getName());
							System.out.println("\t guard: " + guardText);
							System.out.println("\t action: " + actionsText);
							System.exit(1);
						}
						
						System.out.println("ExtendedAutomata.expandTransitions(): Succesfully parsed guard.");
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
							System.out.println("ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							System.out.println("\t automaton: " + curComponentName);
							System.out.println("\t from: " + source.getName());
							System.out.println(" to: " + target.getName());
							System.out.println("\t guard: " + guardText);
							System.out.println("\t action: " + actionsText);
							System.exit(1);
						}
						
						System.out.println("ExtendedAutomata.expandTransitions(): Succesfully parsed actions.");
					}
					
					// put all identifiers in one set
					Set assignmentIdents = new LinkedHashSet();
					Set expressionIdents = new LinkedHashSet();
					if (guardSyntaxTree != null)
					{
						// get guard identifiers
						Finder finder = new Finder(guardSyntaxTree);
						Set guardExpressionIdents = finder.getExpressionIdentifiers();
						for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!expressionIdents.contains(curIdent))
							{
								expressionIdents.add(curIdent);
							}
						}						
					}
					if (actionsSyntaxTree != null)
					{
						// get actions identifiers
						Finder finder = new Finder(actionsSyntaxTree);
						Set actionsAssignmentIdents = finder.getAssignmentIdentifiers();
						for (Iterator iter = actionsAssignmentIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!assignmentIdents.contains(curIdent))
							{
								assignmentIdents.add(curIdent);
							}
						}
						Set actionsExpressionIdents = finder.getExpressionIdentifiers();
						for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = ((Identifier) iter.next()).a;
							if (!expressionIdents.contains(curIdent))
							{
								expressionIdents.add(curIdent);
							}
						}
					}
					
					if (expressionIdents.size() > 0)
					{
						// make symbols
						// currently only integer variables
						Variables symbols = new Variables();
						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							if (!symbols.contains(curIdent))
							{
								symbols.addVariable(curIdent, new IntegerVariable());
							}
						}
						if (assignmentIdents.size() > 0)
						{
							for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								if (!symbols.contains(curIdent))
								{
									symbols.addVariable(curIdent, new IntegerVariable());
								}
							}
						}
			
						// count through all expression identifers and evaluate guard and actions
						// initialize couters and upper bounds maps
						Map identCounters = new LinkedHashMap();
						Map identUpperBounds = new HashMap();
						Map identLowerBounds = new HashMap();
						System.out.println("ExtendedAutomata.expandTransitions(): Initializing counters");
						for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);

							System.out.println(curIdent);

							Integer lowerBound = VariableHelper.getLowerBound(curModuleVariable);
							Integer upperBound = VariableHelper.getUpperBound(curModuleVariable);	
							if (!identCounters.keySet().contains(curIdent))
							{
								identCounters.put(curIdent, lowerBound);
							}
							if (!identUpperBounds.keySet().contains(curIdent))
							{
								identUpperBounds.put(curIdent, upperBound);
							}
							if (!identLowerBounds.keySet().contains(curIdent))
							{
								identLowerBounds.put(curIdent, lowerBound);
							}
						}
					
						// count up all identifiers ant evaluate guard and actions
						Evaluator evaluator = null;
						Variables updatedSymbols = null;
						boolean keepCounting = true;
						System.out.println("ExtendedAutomata.expandTransitions(): Counting");
						while (keepCounting)
						{
							System.out.println(identCounters.toString());
						
							// set expression symbols to counters and make gaurd addition
							String addToGuard = "";
							for (Iterator iter = expressionIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								int value = ((Integer) identCounters.get(curIdent)).intValue();
								((IntegerVariable) symbols.getVariable(curIdent)).setValue(value);
								addToGuard = addToGuard + curIdent + " == " + value;
								if (iter.hasNext())
								{
									addToGuard = addToGuard + " & ";
								}
							}
						
							evaluator = new Evaluator(symbols);

							if (guardSyntaxTree != null)
							{
								Boolean oldGuardValue = (Boolean) evaluator.evaluate(guardSyntaxTree);
								
								// only if old guard is evaluated to true add the transition
								if (oldGuardValue)
								{
									// make new guard
									String guard =  addToGuard;
									
									// make new actions
									String actions = null;
									if (actionsSyntaxTree != null)
									{
										// evaluate the actions
										updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);

										actions = "";
										for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
										{
											String curIdent = (String) iter.next();
											int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
											
											actions = actions + curIdent + " = " + value + ";";
										}
									}
									// mark new edge for adding
									addEdges.add(makeTransition((NodeProxy) source.clone(), (NodeProxy) target.clone(), (LabelBlockProxy) curLabel.clone(), guard, actions));
								}
							}
							else if (actionsSyntaxTree != null)
							{
								// evaluate the actions
								updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);

								// make new guard
								String guard =  addToGuard;

								String actions = "";
								for (Iterator iter = assignmentIdents.iterator(); iter.hasNext();)
								{
									String curIdent = (String) iter.next();
									int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
									
									actions = actions + curIdent + " = " + value + ";";
								}

								// mark new edge for adding
								addEdges.add(makeTransition((NodeProxy) source.clone(), (NodeProxy) target.clone(), (LabelBlockProxy) curLabel.clone(), guard, actions));
							}
							
							// increase ident counters
							List atUpperBound = new LinkedList();
							for (Iterator countIter = identCounters.keySet().iterator(); countIter.hasNext();)
							{
									String curIdent = (String) countIter.next();
									int value = ((Integer) identCounters.get(curIdent)).intValue();
									int upperBound = ((Integer) identUpperBounds.get(curIdent)).intValue();
									
									if (value < upperBound)
									{
									identCounters.put(curIdent, new Integer(value + 1));
									if (atUpperBound.size() > 0)
									{
										for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
										{
											String curAtBound = (String) iter.next();
											int lowerBound = ((Integer) identLowerBounds.get(curAtBound)).intValue();
										
											identCounters.put(curAtBound, new Integer(lowerBound));
										}
										atUpperBound.clear();
									}
									break;
								}
								else
								{
									atUpperBound.add(curIdent);
								}
							}
							// calculate keepCouting condition
							keepCounting = (atUpperBound.size() != identCounters.keySet().size()); 
						}
						// mark current edge for removal
						removeEdges.add(curEdge);
					}
				}
			}

			// remove old edges
			for (Iterator iter = removeEdges.iterator(); iter.hasNext();)
			{
				edges.remove(iter.next());
			}
			
			// add new edges
			for (Iterator iter = addEdges.iterator(); iter.hasNext();)
			{
				edges.add(iter.next());
			}
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
