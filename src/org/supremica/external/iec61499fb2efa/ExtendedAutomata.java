/*
 *   Copyright (C) 2006 Goran Cengic
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

import org.supremica.automata.VariableHelper;


public class ExtendedAutomata
{

	private static ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
	private IdentifierSubject identifier;
	private ModuleSubject module;
	private boolean expand;

	private static ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());

	public ExtendedAutomata(String name, boolean expand) 
	{
		identifier = factory.createSimpleIdentifierProxy(name);

		module = new ModuleSubject(identifier.getName(), null);

		// make marking proposition
		module.getEventDeclListModifiable().add(factory.createEventDeclProxy(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION));

		this.expand = expand;

		//factory = ModuleSubjectFactory.getInstance();

		
		//parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
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
		
		Logger.output("ExtendedAutomata.expandTransitions(): Expanding transitions.");
		
		// get all component variables
		// and put them in a map
		Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Making module variables map.");
		Map moduleVariables = new HashMap();
        for (final Proxy proxy : module.getComponentList()) {
            if (proxy instanceof VariableComponentProxy) {
                final VariableComponentProxy curVar =
                    (VariableComponentProxy) proxy;
				final String curVarName = curVar.getName();
				if (moduleVariables.keySet().contains(curVarName))
				{
					Logger.output(Logger.WARN, "ExtendedAutomata.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
					Logger.output(Logger.WARN, "Variable name: " + curVarName, 1);
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
			Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Component", 1);
			Logger.output(Logger.DEBUG, curComponentName, 2);

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
					Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Expanding edge", 2);

					// get guards
					List curGuards = curBlock.getGuards();
					String guardText = "(";
					if (curGuards.size() > 0 )
					{
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Guard", 3);
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
						Logger.output(Logger.DEBUG, guardText, 4);
					}

					// get actions
					List <BinaryExpressionProxy> curActions = curBlock.getActions();
					String actionsText = "";
					String origActionsText = "";
					if (curActions.size() > 0 )
					{
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Actions", 3);
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
						Logger.output(Logger.DEBUG, actionsText, 4);
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
							Logger.output(Logger.ERROR, "ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							Logger.output(Logger.ERROR, "\t automaton: " + curComponentName);
							Logger.output(Logger.ERROR, "\t from: " + source.getName());
							Logger.output(Logger.ERROR, " to: " + target.getName());
							Logger.output(Logger.ERROR, "\t guard: " + guardText);
							Logger.output(Logger.ERROR, "\t action: " + actionsText);
							System.exit(1);
						}
						
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Succesfully parsed guard.", 3);
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
							Logger.output(Logger.ERROR, "ExtendedAutomaton.addExtendedTransition(): Couldn't parse the action!");
							Logger.output(Logger.ERROR, "\t automaton: " + curComponentName);
							Logger.output(Logger.ERROR, "\t from: " + source.getName());
							Logger.output(Logger.ERROR, " to: " + target.getName());
							Logger.output(Logger.ERROR, "\t guard: " + guardText);
							Logger.output(Logger.ERROR, "\t action: " + actionsText);
							System.exit(1);
						}
						
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Succesfully parsed actions.", 3);
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
						Map identCounters = new LinkedHashMap();
						Map identUpperBounds = new HashMap();
						Map identLowerBounds = new HashMap();
						Logger.output(Logger.DEBUG,"ExtendedAutomata.expandTransitions(): Initializing guard identifiers counters", 3);
						for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);

							Logger.output(Logger.DEBUG, curIdent, 4);

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
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Counting guard identifiers", 3);
						while (keepCountingGuard)
						{
							Logger.output(Logger.DEBUG, identCounters.toString(), 4);
						
							// set expression symbols to counters and make guard addition
							String addToGuard = "";
							for (Iterator iter = guardExpressionIdents.iterator(); iter.hasNext();)
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
									if (actionsExpressionIdents.size()>0)
									{
										Map actionsIdentCounters = new LinkedHashMap();
										Map actionsIdentUpperBounds = new HashMap();
										Map actionsIdentLowerBounds = new HashMap();
										Logger.output(Logger.DEBUG,"ExtendedAutomata.expandTransitions(): Initializing actions identifiers counters", 3);
										for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
										{
											String curIdent = (String) iter.next();
											VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);
											
											Logger.output(Logger.DEBUG, curIdent, 4);
											
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
										Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Counting actions identifiers", 3);
										while (keepCountingActions)
										{
											Logger.output(Logger.DEBUG, actionsIdentCounters.toString(), 4);
											
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
											List atUpperBound = new LinkedList();
											for (Iterator countIter = actionsIdentCounters.keySet().iterator(); countIter.hasNext();)
											{
												String curIdent = (String) countIter.next();
												int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
												int upperBound = ((Integer) actionsIdentUpperBounds.get(curIdent)).intValue();
												
												if (value < upperBound)
												{
													actionsIdentCounters.put(curIdent, new Integer(value + 1));
													if (atUpperBound.size() > 0)
													{
														for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
														{
															String curAtBound = (String) iter.next();
															int lowerBound = ((Integer) actionsIdentLowerBounds.get(curAtBound)).intValue();
															
															actionsIdentCounters.put(curAtBound, new Integer(lowerBound));
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
							keepCountingGuard = (atUpperBound.size() != identCounters.keySet().size()); 
						}
						// mark current edge for removal
						removeEdges.add(curEdge);
					}
					else if (actionsSyntaxTree != null)
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
						Map actionsIdentCounters = new LinkedHashMap();
						Map actionsIdentUpperBounds = new HashMap();
						Map actionsIdentLowerBounds = new HashMap();
						Logger.output(Logger.DEBUG,"ExtendedAutomata.expandTransitions(): Initializing actions identifiers counters", 3);
						for (Iterator iter = actionsExpressionIdents.iterator(); iter.hasNext();)
						{
							String curIdent = (String) iter.next();
							VariableComponentProxy curModuleVariable = (VariableComponentProxy) moduleVariables.get(curIdent);
							
							Logger.output(Logger.DEBUG, curIdent, 4);
							
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
						Logger.output(Logger.DEBUG, "ExtendedAutomata.expandTransitions(): Counting actions identifiers", 3);
						while (keepCountingActions)
						{
							Logger.output(Logger.DEBUG, actionsIdentCounters.toString(), 4);
							
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
							List atUpperBound = new LinkedList();
							for (Iterator countIter = actionsIdentCounters.keySet().iterator(); countIter.hasNext();)
							{
								String curIdent = (String) countIter.next();
								int value = ((Integer) actionsIdentCounters.get(curIdent)).intValue();
								int upperBound = ((Integer) actionsIdentUpperBounds.get(curIdent)).intValue();
								
								if (value < upperBound)
								{
									actionsIdentCounters.put(curIdent, new Integer(value + 1));
									if (atUpperBound.size() > 0)
									{
										for (Iterator iter = atUpperBound.iterator(); iter.hasNext();)
										{
											String curAtBound = (String) iter.next();
											int lowerBound = ((Integer) actionsIdentLowerBounds.get(curAtBound)).intValue();
											
											actionsIdentCounters.put(curAtBound, new Integer(lowerBound));
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

		writeModuleToFile(module ,"blah.wmod");

		Logger.output("ExtendedAutomata.expandTransitions(): Done expanding transitions.");

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

		Logger.output("ExtendedAutomata.writeToFile(): Writing model file.");

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

	public static void writeModuleToFile(ModuleSubject module, String fileName)
	{

		Logger.output("ExtendedAutomata.writeModuleToFile(): Writing module to file.");

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
