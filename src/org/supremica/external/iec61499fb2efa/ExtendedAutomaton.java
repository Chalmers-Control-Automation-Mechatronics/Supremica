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

import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.model.module.EventDeclProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;

import org.supremica.automata.VariableHelper;

public class ExtendedAutomaton
{

	private boolean allAcceptingStates = false;

	private String name;
	private ExtendedAutomata automata;
	private ModuleSubjectFactory factory;
 	private IdentifierSubject identifier;
	private ModuleSubject module;
	private SimpleComponentSubject component;
	private GraphSubject graph;

	private ExpressionParser parser;

	public ExtendedAutomaton(String name, ExtendedAutomata automata, boolean acceptingStates)
	{
		this.name = name;

		factory = ModuleSubjectFactory.getInstance();
		
		this.automata = automata;
		
		module = automata.getModule();

		identifier = factory.createSimpleIdentifierProxy(name);
		graph = factory.createGraphProxy();
		component = factory.createSimpleComponentProxy(identifier, ComponentKind.PLANT, graph);

		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

		allAcceptingStates = acceptingStates;
	}

// 	public ExtendedAutomaton(String name, ComponentKind kind, ExtendedAutomata automata) 
// 	{
// 		this.name = name;

// 		factory = ModuleSubjectFactory.getInstance();
		
// 		this.automata = automata;
		
// 		module = automata.getModule();

// 		identifier = factory.createSimpleIdentifierProxy(name);
// 		graph = factory.createGraphProxy();
// 		component = factory.createSimpleComponentProxy(identifier, kind, graph);

// 		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
// 	}

	protected SimpleComponentSubject getComponent()
	{
		return component;
	}

	public void addInitialState(String name)
	{
		addState(name, true, true);
	}

	public void addAcceptingState(String name)
	{
		addState(name, true, false);
	}

	public void addState(String name)
	{
			addState(name, true, false);
	}

	public void addState(String name, boolean accepting, boolean initial)
	{
		if (allAcceptingStates)
		{
			accepting = true;
		}

		SimpleNodeSubject node = (SimpleNodeSubject) graph.getNodesModifiable().get(name);
		if (node == null)
		{
			if (accepting)
			{
				final List<Proxy> propList = new LinkedList<Proxy>();
				propList.add(factory.createSimpleIdentifierProxy
                               (EventDeclProxy.DEFAULT_MARKING_NAME));
				PlainEventListSubject acceptingProposition =
                    factory.createPlainEventListProxy(propList);
				graph.getNodesModifiable().add
                    (factory.createSimpleNodeProxy(name, acceptingProposition,
                                                   initial, null, null, null));
			}
			else
			{
				graph.getNodesModifiable().add(factory.createSimpleNodeProxy(name, null, initial, null, null, null));
			}
		}
	}

	public void addIntegerVariable(String name, int lowerBound, int upperBound, int initialValue, Integer markedValue)
	{
		module.getComponentListModifiable().add(VariableHelper.createIntegerVariable(name, lowerBound, upperBound, initialValue, null));
	}

	/**
	 * Adds transition to the extended finite automaton with an uncontrollable event
	 *
	 * @param from  name of the source state
	 * @param to name of the destination state
	 * @param label semi-colon separated list of event names for the transition
	 * @param guardIn guard expression for the transition
	 * @param actionIn action expression for the transition
	 */
	public void addControllableTransition(String from, String to, String label, String guardIn, String actionIn)
	{
		SimpleNodeSubject fromNode = (SimpleNodeSubject) graph.getNodesModifiable().get(from);
		if (fromNode == null)
		{
			System.out.println("ExtendedAutomaton.addTransition(): From node " + from + " does not exist!");
		}
		SimpleNodeSubject toNode = (SimpleNodeSubject) graph.getNodesModifiable().get(to);
		if (toNode == null)
		{
			System.out.println("ExtendedAutomaton.addTransition(): To node " + to + " does not exist!");
		}
			
		// parse label into event name list and make LabelBlockSubject 
		final List<Proxy> events = new LinkedList<Proxy>();
		String remainingEvents = label;
		String curEvent;
		while(remainingEvents.contains(";"))
		{
			curEvent = remainingEvents.substring(0,remainingEvents.indexOf(";"));
			remainingEvents = remainingEvents.substring(remainingEvents.indexOf(";") + 1);
			events.add(factory.createSimpleIdentifierProxy(curEvent));

			// Add event declaration to the module if needed
            automata.addEvent(curEvent,"co");
		}
		LabelBlockSubject labelBlock = factory.createLabelBlockProxy(events, null);
			
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
			System.out.println("ExtendedAutomaton.addTransition(): Syntax error in guard!");
			System.out.println("\t automaton: " + name);
			System.out.print("\t from: " + from);
			System.out.println(" to: " + to);
			System.out.println("\t label: " + label);
			System.out.println("\t guard: " + guardIn);
			System.out.println("\t action: " + actionIn);
			return;
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
						System.out.println("ExtendedAutomaton.addTransition(): Syntax error in action!");
						System.out.println("\t automaton: " + name);
						System.out.print("\t from: " + from);
						System.out.println(" to: " + to);
						System.out.println("\t label: " + label);
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return;
					}
					catch (TypeMismatchException exception)
					{
						System.out.println("ExtendedAutomaton.addTransition(): Type mismatch error in action!");
						System.out.println("\t automaton: " + name);
						System.out.print("\t from: " + from);
						System.out.println(" to: " + to);
						System.out.println("\t label: " + label);
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return;
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
			
		EdgeSubject newEdge = factory.createEdgeProxy(fromNode, toNode, labelBlock, guardActionBlock, null, null, null);
		graph.getEdgesModifiable().add(newEdge);	
	}

	public void addTransition(String from, String to, String label, String guardIn, String actionIn)
	{
		SimpleNodeSubject fromNode = (SimpleNodeSubject) graph.getNodesModifiable().get(from);
		if (fromNode == null)
		{
			System.out.println("ExtendedAutomaton.addTransition(): From node " + from + " does not exist!");
		}
		SimpleNodeSubject toNode = (SimpleNodeSubject) graph.getNodesModifiable().get(to);
		if (toNode == null)
		{
			System.out.println("ExtendedAutomaton.addTransition(): To node " + to + " does not exist!");
		}
			
		// parse label into event name list and make LabelBlockSubject 
		final List<Proxy> events = new LinkedList<Proxy>();
		String remainingEvents = label;
		String curEvent;
		while(remainingEvents.contains(";"))
		{
			curEvent = remainingEvents.substring(0,remainingEvents.indexOf(";"));
			remainingEvents = remainingEvents.substring(remainingEvents.indexOf(";") + 1);
			events.add(factory.createSimpleIdentifierProxy(curEvent));

			// Add event declaration to the module if needed
            automata.addEvent(curEvent);
		}
		LabelBlockSubject labelBlock = factory.createLabelBlockProxy(events, null);
			
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
			System.out.println("ExtendedAutomaton.addTransition(): Syntax error in guard!");
			System.out.println("\t automaton: " + name);
			System.out.print("\t from: " + from);
			System.out.println(" to: " + to);
			System.out.println("\t label: " + label);
			System.out.println("\t guard: " + guardIn);
			System.out.println("\t action: " + actionIn);
			return;
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
						System.out.println("ExtendedAutomaton.addTransition(): Syntax error in action!");
						System.out.println("\t automaton: " + name);
						System.out.print("\t from: " + from);
						System.out.println(" to: " + to);
						System.out.println("\t label: " + label);
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return;
					}
					catch (TypeMismatchException exception)
					{
						System.out.println("ExtendedAutomaton.addTransition(): Type mismatch error in action!");
						System.out.println("\t automaton: " + name);
						System.out.print("\t from: " + from);
						System.out.println(" to: " + to);
						System.out.println("\t label: " + label);
						System.out.println("\t guard: " + guardIn);
						System.out.println("\t action: " + actionIn);
						return;
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
			
		EdgeSubject newEdge = factory.createEdgeProxy(fromNode, toNode, labelBlock, guardActionBlock, null, null, null);
		graph.getEdgesModifiable().add(newEdge);	
	}

}
