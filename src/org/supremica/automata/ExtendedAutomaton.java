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
import java.util.StringTokenizer;
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
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


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
    private List<EventDeclProxy> uncontrollableAlphabet;
    private HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap;
    private HashMap<String,NodeProxy> nameToLocationMap;
    private List<NodeProxy> nodes;
    private Set<NodeProxy> initialLocations;
    private Set<NodeProxy> acceptedLocations;
    private Set<NodeProxy> forbiddenLocations;
    private NodeProxy blockedLocation = null;

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
        nodes = new ArrayList<NodeProxy>();
        nodes.addAll(graph.getNodes());

        initialLocations = new HashSet<NodeProxy>();
        forbiddenLocations = new HashSet<NodeProxy>();
        acceptedLocations = new HashSet<NodeProxy>();

        locationToOutgoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>(nodes.size());
        nameToLocationMap = new HashMap<String, NodeProxy>(nodes.size());
        alphabet = new ArrayList<EventDeclProxy>();
        uncontrollableAlphabet = new ArrayList<EventDeclProxy>();

        HashMap<NodeProxy, HashSet<String>> locationToOutgoingEventsMap = new HashMap<NodeProxy, HashSet<String>>(nodes.size());

        for(NodeProxy node:nodes)
        {
            nameToLocationMap.put(node.getName(),node);            
            locationToOutgoingEdgesMap.put(node, new ArrayList<EdgeSubject>());
            locationToOutgoingEventsMap.put(node, new HashSet<String>());

            if (new StringTokenizer(node.toString(), " ").nextToken().equals("initial"))
            {
                initialLocations.add(node);
            }
            if (node.toString().contains(EventDeclProxy.DEFAULT_MARKING_NAME))
            {
                acceptedLocations.add(node);
            }
            if (node.toString().contains(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
            {
               forbiddenLocations.add(node);
            }
        }
        EventListExpressionProxy blockedEvents = component.getGraph().getBlockedEvents();
        HashSet<String> unconAlphabetString = new HashSet<String>();
        if(blockedEvents != null)
        {
            for(Proxy event:blockedEvents.getEventList())
            {
                String eventName = ((SimpleIdentifierSubject)event).getName();
                EventDeclProxy e = automata.eventIdToProxy(eventName);
                alphabet.add(e);
                if(e.getKind() == EventKind.UNCONTROLLABLE)
                {
                    uncontrollableAlphabet.add(e);
                    unconAlphabetString.add(e.getName());
                }
            }
        }

        for(EdgeSubject edge:component.getGraph().getEdgesModifiable())
        {
            for(Proxy event:edge.getLabelBlock().getEventList())
            {
                String eventName = ((SimpleIdentifierSubject)event).getName();
                EventDeclProxy e = automata.eventIdToProxy(eventName);
                alphabet.add(e);
                locationToOutgoingEventsMap.get(edge.getSource()).add(e.getName());

                if(e.getKind() == EventKind.UNCONTROLLABLE)
                {
                    uncontrollableAlphabet.add(e);
                    unconAlphabetString.add(e.getName());
                }
            }
            locationToOutgoingEdgesMap.get(edge.getSource()).add(edge);
        }

        //PLANTIFY
 /*       if(isSpecification() && uncontrollableAlphabet.size()>0)
        {
            //Add a blocked state for "plantify" purposes
//            final List<Proxy> propList = new LinkedList<Proxy>();
//            propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME));
//            PlainEventListSubject markingProposition = factory.createPlainEventListProxy(propList);
            NodeSubject bNode = factory.createSimpleNodeProxy(name, null,false, null, null, null);
            bNode.setName("BlOcKeDStAtE");
            NodeProxy blockedNode = (NodeProxy)bNode;
            nodes.add(blockedNode);
            blockedLocation = blockedNode;

            locationToOutgoingEdgesMap.put(blockedLocation, new ArrayList<EdgeSubject>());
            //Make a transition with an uncontrollble event(UE) to the blocked state from source states where
            //the UE does not exist on the outgoing transitions
            //(transformation of specification to plant to later consider the controllabailty problem as a blocking problem).

            for(NodeProxy node:nodes)
            {
                if(!isLocationPlantifiedBlocked(node) && !forbiddenLocations.contains(node))
                {
                    ArrayList<EdgeSubject> edges = (ArrayList<EdgeSubject>)locationToOutgoingEdgesMap.get(node).clone();
                    if(edges.isEmpty())
                    {
                        EdgeSubject newEdge = null;
                        ArrayList<Proxy> es = new ArrayList<Proxy>();
                        for(String e:unconAlphabetString)
                        {
                            es.add(factory.createSimpleIdentifierProxy(e));
                        }
                        LabelBlockSubject uncommonLabelBlock = factory.createLabelBlockProxy(es,null);

                        newEdge = factory.createEdgeProxy(node, blockedLocation, uncommonLabelBlock, null, null, null, null);

                        locationToOutgoingEdgesMap.get(node).add(newEdge);
                    }
                    else
                    {
                        for(EdgeSubject edge:edges)
                        {

                            EdgeSubject newEdge = null;
                            HashSet<String> commonUnEvents = new HashSet<String>(unconAlphabetString);
                            HashSet<String> uncommonUnEvents = new HashSet<String>(unconAlphabetString);

                            commonUnEvents.retainAll(locationToOutgoingEventsMap.get(edge.getSource()));
                            uncommonUnEvents.removeAll(commonUnEvents);

                            //Create a transition (without guards and actions) from source to the forbidden state with all uncontrollable events that are not included in "edge"
                            if(uncommonUnEvents.size()>0)
                            {
                                ArrayList<Proxy> es = new ArrayList<Proxy>();
                                for(String e:uncommonUnEvents)
                                {
                                    es.add(factory.createSimpleIdentifierProxy(e));
                                }
                                LabelBlockSubject uncommonLabelBlock = factory.createLabelBlockProxy(es,null);

                                newEdge = factory.createEdgeProxy(edge.getSource(), blockedLocation, uncommonLabelBlock, null, null, null, null);
                            }

                            //Create a transition from source to the forbidden state with an uncontrollable event that is included in "edge" but with the complement of edge.guard
                            GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
                            List<SimpleExpressionSubject> blockGuards = guardActionBlock.getGuardsModifiable();
                            if(edge.getGuardActionBlock() != null)
                            {
                                for(SimpleExpressionProxy guard:edge.getGuardActionBlock().getGuards())
                                {
                                    try{blockGuards.add((SimpleExpressionSubject)parser.parse("!("+guard.getPlainText()+")", Operator.TYPE_BOOLEAN));}catch(ParseException e){}
                                }

                                if(commonUnEvents.size()>0)
                                {
                                    ArrayList<Proxy> es = new ArrayList<Proxy>();
                                    for(String e:commonUnEvents)
                                    {
                                        es.add(factory.createSimpleIdentifierProxy(e));
                                    }
                                    LabelBlockSubject commonLabelBlock = factory.createLabelBlockProxy(es,null);

                                    if(!edge.getGuardActionBlock().getGuards().isEmpty())
                                        newEdge = factory.createEdgeProxy(edge.getSource(), blockedLocation, commonLabelBlock, guardActionBlock, null, null, null);
                                }
                            }

                            if(newEdge != null)
                            {
                                locationToOutgoingEdgesMap.get(edge.getSource()).add(newEdge);
                            }
                        }
                    }
                }
            }
        }
*/
    }

    public NodeProxy getblockedLocation()
    {
        return blockedLocation;
    }

    public boolean isLocationPlantifiedBlocked(NodeProxy node)
    {
        if(node.equals(blockedLocation))
            return true;

        return false;
    }

    public NodeProxy getInitialLocation()
    {
        return initialLocations.iterator().next();
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

    public Set<NodeProxy> getMarkedLocations()
    {
        return acceptedLocations;
    }

    public  HashMap<NodeProxy,ArrayList<EdgeSubject>> getLocationToOutgoingEdgesMap()
    {
        return  locationToOutgoingEdgesMap;
    }

    public ComponentKind getKind()
    {
        return component.getKind();
    }

    public List<NodeProxy> getNodes()
    {
        return nodes;
    }

    public NodeProxy getLocationWithName(String name)
    {
        return nameToLocationMap.get(name);
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

    public List<EventDeclProxy> getUncontrollableAlphabet()
    {
        return uncontrollableAlphabet;
    }

	public void addState(String name)
	{
		if (allAcceptingStates)
		{
			addState(name, true, false, false);
		}
		else
		{
			addState(name, false, false, false);
		}
	}

	public SimpleNodeSubject addState(String name, boolean accepting, boolean initial, boolean forbidden)
	{
		SimpleNodeSubject node = (SimpleNodeSubject) graph.getNodesModifiable().get(name);
		if (node == null)
		{
            final List<Proxy> propList = new LinkedList<Proxy>();
			if (accepting)
			{				
				propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));
			}
			if (forbidden)
			{				
				propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME));
			}

            PlainEventListSubject markingProposition = factory.createPlainEventListProxy(propList);

            node = factory.createSimpleNodeProxy(name, markingProposition,initial, null, null, null);
            graph.getNodesModifiable().add (node);
		}

        return node;
	}
    
    public boolean isSpecification()
    {
        return (component.getKind() == ComponentKind.SPEC);
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
