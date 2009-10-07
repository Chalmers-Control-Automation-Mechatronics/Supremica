//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
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
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.automata;

import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;


import java.util.HashSet;
import net.sourceforge.waters.subject.module.*;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;

import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


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
    private List<EventDeclProxy> alphabet;
    private HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap;
    private HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToIngoingEdgesMap;
    private Set<NodeProxy> nodes;
    private Set<NodeProxy> initialLocations;
    private Set<NodeProxy> acceptedLocations;
    private Set<NodeProxy> forbiddenLocations;

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

    public ExtendedAutomaton(ExtendedAutomata automata, SimpleComponentSubject component)
    {
        this.name = component.getName();
        factory = ModuleSubjectFactory.getInstance();
        this.automata = automata;
        this.component = component;
        graph = component.getGraph();
        module = automata.getModule();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        nodes = graph.getNodes();

        locationToOutgoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>(graph.getNodes().size());
        locationToIngoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>(graph.getNodes().size());
        alphabet = new ArrayList<EventDeclProxy>();

        initialLocations = new HashSet<NodeProxy>();
        forbiddenLocations = new HashSet<NodeProxy>();
        acceptedLocations = new HashSet<NodeProxy>();

        for(NodeProxy node:nodes)
        {
            locationToOutgoingEdgesMap.put(node, new ArrayList<EdgeSubject>());
            locationToIngoingEdgesMap.put(node, new ArrayList<EdgeSubject>());

            if (node.toString().contains("initial"))
            {
                initialLocations.add(node);
            }
            if (node.toString().contains("accepting"))
            {
                acceptedLocations.add(node);
            }
            if (node.toString().contains("forbidden"))
            {
               forbiddenLocations.add(node);
            }
        }


        EventListExpressionProxy blockedEvents = component.getGraph().getBlockedEvents();
        if(blockedEvents != null)
        {
            for(Proxy event:blockedEvents.getEventList())
            {
                String eventName = ((SimpleIdentifierSubject)event).getName();
                alphabet.add(automata.eventIdToProxy(eventName));
            }
        }

        for(EdgeSubject edge:component.getGraph().getEdgesModifiable())
        {
            for(Proxy event:edge.getLabelBlock().getEventList())
            {
                String eventName = ((SimpleIdentifierSubject)event).getName();
                alphabet.add(automata.eventIdToProxy(eventName));
            }

            locationToOutgoingEdgesMap.get(edge.getSource()).add(edge);
            locationToIngoingEdgesMap.get(edge.getTarget()).add(edge);
        }
    }

    public boolean isLocationInitial(NodeProxy node)
    {
        if(initialLocations.contains(node))
            return true;
        return false;
    }

    public boolean isLocationForbidden(NodeProxy node)
    {
        if(forbiddenLocations.contains(node))
            return true;
        return false;
    }

    public boolean isLocationAccepted(NodeProxy node)
    {
        if(acceptedLocations.contains(node))
            return true;
        return false;
    }

    public  HashMap<NodeProxy,ArrayList<EdgeSubject>> getLocationToOutgoingEdgesMap()
    {
        return  locationToOutgoingEdgesMap;
    }

    public  HashMap<NodeProxy,ArrayList<EdgeSubject>> getLocationToIngoingEdgesMap()
    {
        return  locationToIngoingEdgesMap;
    }

    public ComponentKind getKind()
    {
        return component.getKind();
    }

    public Set<NodeProxy> getNodes()
    {
        return nodes;
    }

    public int nbrOfNodes()
    {
        return nodes.size();
    }

    public String getName()
    {
        return component.getName();
    }

	public SimpleComponentSubject getComponent()
	{
		return component;
	}

    public List<EventDeclProxy> getAlphabet()
    {
        return alphabet;
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
		if (allAcceptingStates)
		{
			addState(name, true, false);
		}
		else
		{
			addState(name, false, false);			
		}
	}

	public void addState(String name, boolean accepting, boolean initial)
	{
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
            automata.includeControllableEvent(curEvent);
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
