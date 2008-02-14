/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

/*
 * Copyright (C) 2008 Markus Skoldstam
 * Copyright (C) 2005 Goran Cengic
*/
/**
 * @author Markus Sk&ouml;ldstam (skoldsta@chalmers.se)
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

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
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

import org.supremica.log.*;

public class ExtendedAutomataExpander
{

	private static ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
	private static ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    private static Logger log = LoggerFactory.createLogger(ExtendedAutomataExpander.class);

	public static void expandTransitions(final ModuleSubject module)
	{


		log.info("ExtendedAutomataExpander.expandTransitions(): Expanding transitions.");
		
		// get all component variables
		// and put them in a map
		log.debug("ExtendedAutomataExpander.expandTransitions(): Making module variables map.");
		final Map<String,VariableComponentProxy> moduleVariables =
            new HashMap<String,VariableComponentProxy>();
        for (final Proxy proxy : module.getComponentList()) {
            if (proxy instanceof VariableComponentProxy) {
                final VariableComponentProxy curVar =
                    (VariableComponentProxy) proxy;
				final String curVarName = curVar.getName();
				if (moduleVariables.keySet().contains(curVarName))
				{
					log.warn("ExtendedAutomataExpander.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
					log.warn("Variable name: " + curVarName);
				}
                else
                {
                    moduleVariables.put(curVar.getName(), curVar);    
                }
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
			log.debug("ExtendedAutomataExpander.expandTransitions(): Component");
			log.debug(curComponentName);

			final ListSubject<EdgeSubject> edges =
                ((GraphSubject) curComponent.getGraph()).getEdgesModifiable();
			final List<EdgeSubject> removeEdges =
                new LinkedList<EdgeSubject>();
			final List<EdgeSubject> addEdges = new LinkedList<EdgeSubject>();

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
					log.debug("ExtendedAutomataExpander.expandTransitions(): Expanding edge");

					// get guards
					List curGuards = curBlock.getGuards();
					String guardText = "(";
					if (curGuards.size() > 0 )
					{
						log.debug("ExtendedAutomataExpander.expandTransitions(): Guard");
						for (Iterator iter = curGuards.iterator(); iter.hasNext();)
						{
							//guardText = guardText + ((SimpleExpressionProxy) iter.next()).getPlainText();
							guardText = guardText + ((SimpleExpressionProxy) iter.next()).toString();
							if (iter.hasNext())
							{
								guardText = guardText + ") & (";
							}
							else
							{
								guardText = guardText + ")";
							}
						}
						log.debug(guardText);
					}

					// get actions
					List <BinaryExpressionProxy> curActions = curBlock.getActions();
					String actionsText = "";
					String origActionsText = "";
					if (curActions.size() > 0 )
					{
						log.debug("ExtendedAutomataExpander.expandTransitions(): Actions");
						for (Iterator iter = curActions.iterator(); iter.hasNext();)
						{
							BinaryExpressionProxy curAction = (BinaryExpressionProxy) iter.next();
							actionsText = actionsText + curAction.toString() + "; ";
							if (iter.hasNext())
							{
								origActionsText = origActionsText + curAction.toString() + "; ";
							}
							else
							{
								origActionsText = origActionsText + curAction.toString();
							}
						}
						log.debug(actionsText);
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
							log.error("ExtendedAutomataExpander.expandTransitions(): Couldn't parse the action!");
							log.error("\t automaton: " + curComponentName);
							log.error("\t from: " + source.getName());
							log.error(" to: " + target.getName());
							log.error("\t guard: " + guardText);
							log.error("\t action: " + actionsText);
							System.exit(1);
						}
						
						log.debug("ExtendedAutomataExpander.expandTransitions(): Succesfully parsed guard.");
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
							log.error("ExtendedAutomataExpander.expandTransition(): Couldn't parse the action!");
							log.error("\t automaton: " + curComponentName);
							log.error("\t from: " + source.getName());
							log.error(" to: " + target.getName());
							log.error("\t guard: " + guardText);
							log.error("\t action: " + actionsText);
							System.exit(1);
						}
						
						log.debug("ExtendedAutomataExpander.expandTransitions(): Succesfully parsed actions.");
					}
					
					// get guard identifiers
					Set<String> guardExpressionIdents =
                        new LinkedHashSet<String>();
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
					Set<String> actionsAssignmentIdents =
                        new LinkedHashSet<String>();
					// expression idents found in actions
                    // that are not in the guard
					Set<String> actionsExpressionIdents =
                        new LinkedHashSet<String>();
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
							//if (!guardExpressionIdents.contains(curIdent))
							//{
								if (!actionsExpressionIdents.contains(curIdent))
								{
									actionsExpressionIdents.add(curIdent);
								}
							//}
						}
					}

					
					// evaluate guard expression
					if (guardSyntaxTree != null)
					{
						// make symbols
						// currently only integer variables
						Variables symbols = new Variables();
						for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							symbols.addVariable(curIdent, new IntegerVariable());
						}
			
						// count through all expression identifers and evaluate the guard
						// initialize couters and upper bounds maps
						Map<String,Integer> identCounters =
                            new LinkedHashMap<String,Integer>();
						Map<String,Integer> identUpperBounds =
                            new HashMap<String,Integer>();
						Map<String,Integer> identLowerBounds =
                            new HashMap<String,Integer>();
						log.debug("ExtendedAutomataExpander.expandTransitions(): Initializing guard identifiers counters");
						for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);

							log.debug(curIdent);

							Integer lowerBound = VariableHelper.getLowerBound(curModuleVariable);
							Integer upperBound = VariableHelper.getUpperBound(curModuleVariable);	
							if (!identCounters.keySet().contains(curIdent))
							{
								// intialize identifier to lower bound
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
					
						// count up all identifiers and evaluate the guard
						Evaluator evaluator = null;
						Boolean oldGuardValue = false;
						boolean keepCountingGuard = true;
						log.debug("ExtendedAutomataExpander.expandTransitions(): Counting guard identifiers");
						while (keepCountingGuard)
						{
							log.debug(identCounters.toString());
						
							// set expression symbols to counters and make guard addition
							String addToGuard = "";
							for (final Iterator<String> iter = 
                                     guardExpressionIdents.iterator();
                                 iter.hasNext(); ) {
                                final String curIdent = iter.next();
								final int value = identCounters.get(curIdent);
                                final IntegerVariable intvar =
                                    (IntegerVariable) symbols.getVariable(curIdent);
								intvar.setValue(value);
								addToGuard = addToGuard + curIdent + " == " + value;
								if (iter.hasNext())
								{
									addToGuard = addToGuard + " & ";
								}
							}
							
							// evaluate the guard with interpreter
							if (evaluator == null)
							{
								evaluator = new Evaluator(symbols);
							}
							else
							{
								evaluator.setVariables(symbols);
							}
							oldGuardValue = (Boolean) evaluator.evaluate(guardSyntaxTree);
								
							// if guard is evaluated to true, evaluate the actions
							if (oldGuardValue)
							{
								// make new guard
								String guard =  addToGuard;
								
								// count up all identifiers and evaluate the actions
								if (actionsSyntaxTree != null)
								{
									// if there are no idents in action's expressions
									if (actionsExpressionIdents.size()>0)
									{
										// add symbols for action identifiers
										// currently only integer variables
										for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
										{
											String curIdent = (String) iter.next();
											symbols.addVariable(curIdent, new IntegerVariable());
										}
										for (Iterator iter = actionsAssignmentIdents.iterator(); iter.hasNext();)
										{
											String curIdent = (String) iter.next();
											if (!symbols.contains(curIdent))
											{
												symbols.addVariable(curIdent, new IntegerVariable());
											}
										}

										// count through all actions expression identifers and evaluate the actions
										// initialize couters and upper bounds maps
										Map<String,Integer> actionsIdentCounters = new LinkedHashMap<String,Integer>();
										Map<String,Integer> actionsIdentUpperBounds = new HashMap<String,Integer>();
										Map<String,Integer> actionsIdentLowerBounds = new HashMap<String,Integer>();
										log.debug("ExtendedAutomataExpander.expandTransitions(): Initializing actions identifiers counters");
										for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
										{
											String curIdent = (String) iter.next();
											VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);
											
											log.debug(curIdent);
											
											Integer lowerBound = VariableHelper.getLowerBound(curModuleVariable);
											Integer upperBound = VariableHelper.getUpperBound(curModuleVariable);	
											if (!actionsIdentCounters.keySet().contains(curIdent))
											{
												// intialize identifier to lower bound
												actionsIdentCounters.put(curIdent, lowerBound);
											}
											if (!actionsIdentUpperBounds.keySet().contains(curIdent))
											{
												actionsIdentUpperBounds.put(curIdent, upperBound);
											}
											if (!actionsIdentLowerBounds.keySet().contains(curIdent))
											{
												actionsIdentLowerBounds.put(curIdent, lowerBound);
											}
										}
										
										// count up all identifiers and evaluate the actions
										evaluator = null;
										Variables updatedSymbols = null;
										boolean keepCountingActions = true;
										String oldGuard = guard;
										log.debug("ExtendedAutomataExpander.expandTransitions(): Counting actions identifiers");
										while (keepCountingActions)
										{
											log.debug(actionsIdentCounters.toString());
											
											// set action expression symbols to counters
											addToGuard = "";
											for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
											{
												String curIdent = (String) iter.next();
												int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
												((IntegerVariable) symbols.getVariable(curIdent)).setValue(value);
												addToGuard = addToGuard + curIdent + " == " + value;
												if (iter.hasNext())
												{
													addToGuard = addToGuard + " & ";
												}
											}
											
											if (!addToGuard.equals(""))
											{
												guard = oldGuard + " & " + addToGuard;
											}
											
											// evaluate the actions
											if (evaluator == null)
											{
												evaluator = new Evaluator(symbols);
											}
											else
											{
												evaluator.setVariables(symbols);
											}
											updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);
											
											// make new actions
											String actions = "";
											for (Iterator iter = actionsAssignmentIdents.iterator(); iter.hasNext();)
											{
												String curIdent = (String) iter.next();
												int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
												
												actions = actions + curIdent + " = " + value + ";";
											}
											
											// mark new edge for adding
											addEdges.add(makeTransition((NodeProxy) source/*.clone()*/, (NodeProxy) target/*.clone()*/, 
																		(LabelBlockProxy) curLabel.clone(), guard, actions));
										
											// increase actions ident counters
											List<String> atUpperBound =
                                                new LinkedList<String>();
											for (Iterator countIter = actionsIdentCounters.keySet().iterator(); countIter.hasNext();)
											{
												String curIdent = (String) countIter.next();
												int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
												int upperBound = ((Integer) actionsIdentUpperBounds.get(curIdent)).intValue();
												
												if (value < upperBound)
												{
													actionsIdentCounters.put(curIdent, value + 1);
													if (atUpperBound.size() > 0)
													{
														for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
														{
															String curAtBound = (String) iter.next();
															int lowerBound = ((Integer) actionsIdentLowerBounds.get(curAtBound)).intValue();
															
															actionsIdentCounters.put(curAtBound, lowerBound);
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
											
											// calculate keepCoutingActions condition
											keepCountingActions = (atUpperBound.size() != actionsIdentCounters.keySet().size()); 
										}
									}
									else
									{
										// mark new edge for adding
										addEdges.add(makeTransition((NodeProxy) source/*.clone()*/, (NodeProxy) target/*.clone()*/, 
																	(LabelBlockProxy) curLabel.clone(), guard, origActionsText));
									}
								}
								else
								{
									// mark new edge for adding
									addEdges.add(makeTransition((NodeProxy) source/*.clone()*/, (NodeProxy) target/*.clone()*/, 
																(LabelBlockProxy) curLabel.clone(), guard, ""));
								}
							}

							// increase guard ident counters
							final List<String> atUpperBound =
                                new LinkedList<String>();
							for (Iterator countIter = identCounters.keySet().iterator(); countIter.hasNext();)
							{
								String curIdent = (String) countIter.next();
								int value = ((Integer) identCounters.get(curIdent)).intValue();
								int upperBound = ((Integer) identUpperBounds.get(curIdent)).intValue();
									
								if (value < upperBound)
								{
									identCounters.put(curIdent, value + 1);
									if (atUpperBound.size() > 0)
									{
										for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
										{
											String curAtBound = (String) iter.next();
											int lowerBound = ((Integer) identLowerBounds.get(curAtBound)).intValue();
										
											identCounters.put(curAtBound, lowerBound);
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
							keepCountingGuard = (atUpperBound.size() != identCounters.keySet().size()); 
						}
						// mark current edge for removal
						removeEdges.add(curEdge);
					}
					else if (actionsSyntaxTree != null)
					{
						// if there are no idents in action's expressions
						if (actionsExpressionIdents.size()>0)
						{
							
							// make symbols
							// currently only integer variables
							Variables symbols = new Variables();
							for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								symbols.addVariable(curIdent, new IntegerVariable());
							}
							
							// add symbols for action identifiers
							// currently only integer variables
							for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								symbols.addVariable(curIdent, new IntegerVariable());
							}
							for (Iterator iter = actionsAssignmentIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
								if (!symbols.contains(curIdent))
								{
									symbols.addVariable(curIdent, new IntegerVariable());
								}
							}
							
							// count through all actions expression identifers and evaluate the actions
							// initialize couters and upper bounds maps
							final Map<String,Integer> actionsIdentCounters =
                                new LinkedHashMap<String,Integer>();
							final Map<String,Integer> actionsIdentUpperBounds =
                                new HashMap<String,Integer>();
							final Map<String,Integer> actionsIdentLowerBounds =
                                new HashMap<String,Integer>();
							log.debug("ExtendedAutomataExpander.expandTransitions(): Initializing actions identifiers counters");
							for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
							{
								String curIdent = (String) iter.next();
							VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);
							
							log.debug(curIdent);
							
							Integer lowerBound = VariableHelper.getLowerBound(curModuleVariable);
							Integer upperBound = VariableHelper.getUpperBound(curModuleVariable);	
							if (!actionsIdentCounters.keySet().contains(curIdent))
							{
								// intialize identifier to lower bound
								actionsIdentCounters.put(curIdent, lowerBound);
							}
							if (!actionsIdentUpperBounds.keySet().contains(curIdent))
							{
								actionsIdentUpperBounds.put(curIdent, upperBound);
							}
							if (!actionsIdentLowerBounds.keySet().contains(curIdent))
							{
								actionsIdentLowerBounds.put(curIdent, lowerBound);
							}
							}
							
							// count up all identifiers and evaluate the actions
							Evaluator evaluator = null;
							Variables updatedSymbols = null;
							boolean keepCountingActions = true;
							log.debug("ExtendedAutomataExpander.expandTransitions(): Counting actions identifiers");
							while (keepCountingActions)
							{
								log.debug(actionsIdentCounters.toString());
								
								// set action expression symbols to counters
								String addToGuard = "";
								for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
								{
									String curIdent = (String) iter.next();
									int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
									((IntegerVariable) symbols.getVariable(curIdent)).setValue(value);
									addToGuard = addToGuard + curIdent + " == " + value;
									if (iter.hasNext())
									{
										addToGuard = addToGuard + " & ";
									}
								}
								
								String guard =  addToGuard;
								
								// evaluate the actions
								if (evaluator == null)
								{
									evaluator = new Evaluator(symbols);
								}
								else
								{
									evaluator.setVariables(symbols);
								}
								updatedSymbols = (Variables) evaluator.evaluateNonSequentially(actionsSyntaxTree);
								
								// make new actions
								String actions = "";
								for (Iterator iter = actionsAssignmentIdents.iterator(); iter.hasNext();)
								{
									String curIdent = (String) iter.next();
									int value = ((IntegerVariable) updatedSymbols.getVariable(curIdent)).getValue().intValue();
									
									actions = actions + curIdent + " = " + value + ";";
								}
								
								// mark new edge for adding
								addEdges.add(makeTransition((NodeProxy) source/*.clone()*/, (NodeProxy) target/*.clone()*/,
															(LabelBlockProxy) curLabel.clone(), guard, actions));
								
								// increase actions ident counters
								final List<String> atUpperBound =
                                    new LinkedList<String>();
								for (Iterator countIter = actionsIdentCounters.keySet().iterator(); countIter.hasNext();)
								{
									String curIdent = (String) countIter.next();
									int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
									int upperBound = ((Integer) actionsIdentUpperBounds.get(curIdent)).intValue();
									
									if (value < upperBound)
									{
										actionsIdentCounters.put(curIdent, value + 1);
										if (atUpperBound.size() > 0)
										{
											for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
											{
												String curAtBound = (String) iter.next();
												int lowerBound = ((Integer) actionsIdentLowerBounds.get(curAtBound)).intValue();
												
												actionsIdentCounters.put(curAtBound, lowerBound);
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
							
								// calculate keepCoutingActions condition
								keepCountingActions = (atUpperBound.size() != actionsIdentCounters.keySet().size()); 
							}
						}
						else
						{
							// mark new edge for adding
							addEdges.add(makeTransition((NodeProxy) source/*.clone()*/, (NodeProxy) target/*.clone()*/,
														(LabelBlockProxy) curLabel.clone(), "", origActionsText));
						}
						// mark current edge for removal
						removeEdges.add(curEdge);
					}
 				}
 			}

 			// remove old edges
            edges.removeAll(removeEdges);
 			// add new edges
            edges.addAll(addEdges);
		}

		//writeModuleToFile(module ,"blah.wmod");

		log.info("ExtendedAutomataExpander.expandTransitions(): Done expanding transitions.");

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
			System.out.println("ExtendedAutomataExpander.makeTransition(): Syntax error in guard!");
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
						System.out.println("ExtendedAutomataExpander.makeTransition(): Syntax error in action!");
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return null;
					}
					catch (TypeMismatchException exception)
					{
						System.out.println("ExtendedAutomataExpander.makeTransition(): Type mismatch error in action!");
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

	private static void writeModuleToFile(ModuleSubject module, String fileName)
	{

		log.info("ExtendedAutomataExpander.writeModuleToFile(): Writing module to file.");

		try
		{
			JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, new File(fileName));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
