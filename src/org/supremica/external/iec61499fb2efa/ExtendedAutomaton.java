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
import net.sourceforge.waters.subject.module.VariableHelper;

import net.sourceforge.waters.model.module.EventDeclProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.TypeMismatchException;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;



public class ExtendedAutomaton
{

	private String name;
	private ExtendedAutomata automata;
	private ModuleSubjectFactory factory;
 	private IdentifierSubject identifier;
	private ModuleSubject module;
	private SimpleComponentSubject component;
	private GraphSubject graph;

	private ExpressionParser parser;

	private boolean expandActions = false;

	public ExtendedAutomaton(String name, ExtendedAutomata automata)
	{
		this.name = name;

		factory = ModuleSubjectFactory.getInstance();
		
		this.automata = automata;
		
		module = automata.getModule();

		identifier = factory.createSimpleIdentifierProxy(name);
		graph = factory.createGraphProxy();
		component = factory.createSimpleComponentProxy(identifier, ComponentKind.PLANT, graph);

		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
	}

	public ExtendedAutomaton(String name, ExtendedAutomata automata, boolean expand) 
	{
		this.name = name;

		factory = ModuleSubjectFactory.getInstance();
		
		this.automata = automata;
		
		module = automata.getModule();

		identifier = factory.createSimpleIdentifierProxy(name);
		graph = factory.createGraphProxy();
		component = factory.createSimpleComponentProxy(identifier, ComponentKind.PLANT, graph);

		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

		expandActions = expand;
	}

	public ExtendedAutomaton(String name, ComponentKind kind, ExtendedAutomata automata) 
	{
		factory = ModuleSubjectFactory.getInstance();
		
		this.automata = automata;
		
		module = automata.getModule();

		identifier = factory.createSimpleIdentifierProxy(name);
		graph = factory.createGraphProxy();
		component = factory.createSimpleComponentProxy(identifier, kind, graph);
	}


	protected SimpleComponentSubject getComponent()
	{
		return component;
	}

	public void addState(String name)
	{
		addState(name, false);
	}

	public void addState(String name, boolean initial)
	{
		SimpleNodeSubject node = (SimpleNodeSubject) graph.getNodesModifiable().get(name);
		if (node == null)
		{
			graph.getNodesModifiable().add(factory.createSimpleNodeProxy(name,null, initial,null,null,null));
		}
	}

	public void addIntegerVariable(String name, int lowerBound, int upperBound, int initialValue, Integer markedValue)
	{
		component.getVariablesModifiable().add(VariableHelper.createIntegerVariable(name, lowerBound, upperBound, initialValue, markedValue));
	}

	/**
	 * Adds transition to the extended finite automaton 
	 *
	 * @param from  name of the source state
	 * @param to name of the destination state
	 * @param label semi-colon separated list of event names for the transition
	 * @param guardIn guard expression for the transition
	 * @param actionIn action expression for the transition
	 */
	public void addTransition(String from, String to, String label, String guardIn, String actionIn)
	{
		if (expandActions)
		{
			addExtendedTransition(from, to, label, guardIn, actionIn);
		}
		else
		{
			addNormalTransition(from, to, label, guardIn, actionIn);
		}
	}

	private void addNormalTransition(String from, String to, String label, String guardIn, String actionIn)
	{
		SimpleNodeSubject fromNode = (SimpleNodeSubject) graph.getNodesModifiable().get(from);
		if (fromNode == null)
		{
			System.out.println("ExtendedAutomaton.addNormalTransition(): From node " + from + " does not exist!");
		}
		SimpleNodeSubject toNode = (SimpleNodeSubject) graph.getNodesModifiable().get(to);
		if (toNode == null)
		{
			System.out.println("ExtendedAutomaton.addNormalTransition(): To node " + to + " does not exist!");
		}
			
		// parse label into event name list and make LabelBlockSubject 
		List events = new LinkedList();
		String remainingEvents = label;
		String curEvent;
		while(remainingEvents.contains(";"))
		{
			curEvent = remainingEvents.substring(0,remainingEvents.indexOf(";"));
			remainingEvents = remainingEvents.substring(remainingEvents.indexOf(";") + 1);
			// Add event declaration to the module if needed
			for(Iterator iter = module.getEventDeclList().iterator();iter.hasNext();)
			{
				if(((EventDeclProxy) iter.next()).getName().equals(curEvent))
				{	
					automata.addEvent(curEvent);
				}
			}
			events.add(factory.createSimpleIdentifierProxy(curEvent));
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
			System.out.println("ExtendedAutomaton.addNormalTransition(): Syntax error in guard!");
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
						System.out.println("ExtendedAutomaton.addNormalTransition(): Syntax error in action!");
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
						System.out.println("ExtendedAutomaton.addNormalTransition(): Type mismatch error in action!");
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

	private void addExtendedTransition(String from, String to, String label, String guardIn, String actionIn)
	{

		// expand actions
		StringReader stringReader = new StringReader(actionIn);
		Lexer lexer = new Lexer((Reader) stringReader);
		Parser parser = new Parser((Scanner) lexer);
		Goal syntaxTree = null;
		try
		{
			syntaxTree = (Goal) parser.parse().value;
		}
		catch(Exception e)
		{
			System.out.println("ExtendedAutomaton.addExtendedTransition(): Type mismatch error in action!");
			System.out.println("\t automaton: " + name);
			System.out.print("\t from: " + from);
			System.out.println(" to: " + to);
			System.out.println("\t label: " + label);
			System.out.println("\t guard: " + guardIn);
			System.out.println("\t action: " + actionIn);
			return;
		}

		if (syntaxTree instanceof StatementList)
		{

		}
		else if (syntaxTree instanceof Expression)
		{
			System.out.println("ExtendedAutomaton.addExtendedTransition(): Type mismatch error in action!");
			System.out.println("\t automaton: " + name);
			System.out.print("\t from: " + from);
			System.out.println(" to: " + to);
			System.out.println("\t label: " + label);
			System.out.println("\t guard: " + guardIn);
			System.out.println("\t action: " + actionIn);
			return;
		}


		addNormalTransition(from, to, label, guardIn, actionIn);
	}
}
