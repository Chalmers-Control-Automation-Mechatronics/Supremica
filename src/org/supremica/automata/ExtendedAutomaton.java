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
 * @author Goran Cengic
 */

package org.supremica.automata;

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
		graph.getNodesModifiable().add(factory.createSimpleNodeProxy(name));
	}

	public void addState(String name, boolean initial)
	{
		graph.getNodesModifiable().add(factory.createSimpleNodeProxy(name,null, initial,null,null,null));
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
		SimpleNodeSubject fromNode = (SimpleNodeSubject) graph.getNodesModifiable().get(from);
		SimpleNodeSubject toNode = (SimpleNodeSubject) graph.getNodesModifiable().get(to);
		
		// parse label into event name list and make LabelBlockSubject 
		List events = new LinkedList();
		String remainingEvents = label;
		String curEvent;
		while(remainingEvents.contains(";"))
		{
			curEvent = remainingEvents.substring(0,remainingEvents.indexOf(";"));
			remainingEvents = remainingEvents.substring(remainingEvents.indexOf(";") + 1);
			// Add event declaration to the module if needed
			boolean gotIt = false;
			for(Iterator iter = module.getEventDeclList().iterator();iter.hasNext();)
			{
				if(((EventDeclProxy) iter.next()).getName().equals(curEvent))
				{
					gotIt = true;
				}
			}
			if(!gotIt)
			{
				automata.addEvent(curEvent);
			}
			events.add(factory.createSimpleIdentifierProxy(curEvent));
		}
		LabelBlockSubject labelBlock = factory.createLabelBlockProxy(events, null);
		
		// make GuardActionSubject
		// Get guard ...
		SimpleExpressionSubject guard = null;
		try
		{
			final String guardText = guardIn;
			if (guardText != null && !guardText.trim().equals(""))
			{
				guard = (SimpleExpressionSubject) parser.parse(guardText, Operator.TYPE_BOOLEAN);
			}
		}
		catch (final ParseException exception)
		{
			System.out.println("ExtendedAutomaton.addTransition(): Syntax error in guard!");
			System.out.println("\t automaton: " + name);
			System.out.println("\t from: " + from);
			System.out.println("\t to: " + to);
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
			final String[] texts = actionIn.split(";");
			actions =	new ArrayList<BinaryExpressionSubject>(texts.length);
			for (final String text : texts)
			{
				if (text.length() > 0)
				{
					try
					{
						final SimpleExpressionSubject action = (SimpleExpressionSubject) parser.parse(text);
						if (!(action instanceof BinaryExpressionSubject))
						{
							throw new TypeMismatchException(action, "ACTION");
						}
						final BinaryExpressionSubject binaction = (BinaryExpressionSubject) action;
						actions.add(binaction);
					}
					catch (final ParseException exception)
					{
						System.out.println("ExtendedAutomaton.addTransition(): Syntax error in action!");
						return;
					}
					catch (final TypeMismatchException exception)
					{
						System.out.println("ExtendedAutomaton.addTransition(): Syntax error in action!");
						return;
					}
				}
			}
		}
		// Store parsed results ...
		final GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
		final List<SimpleExpressionSubject> bguards = guardActionBlock.getGuardsModifiable();
		bguards.clear();
		if (guard != null)
		{
			bguards.add(guard);
		}
		final List<BinaryExpressionSubject> bactions = guardActionBlock.getActionsModifiable();
		bactions.clear();
		if (actions != null)
		{
			bactions.addAll(actions);
		}


		EdgeSubject newEdge = factory.createEdgeProxy(fromNode, toNode, labelBlock, guardActionBlock, null, null, null);
		graph.getEdgesModifiable().add(newEdge);	
	}

}
