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
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

package org.supremica.automata;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


public class ExtendedAutomaton
{

    private boolean allAcceptingStates = false;

    private String name;
    private final ExtendedAutomata automata;
    private final ModuleSubjectFactory factory;
    private IdentifierSubject identifier;
    private final SimpleComponentSubject component;
    private final GraphSubject graph;
    private final ExpressionParser parser;
    private final HashSet<EventDeclProxy> alphabet;
    private final HashSet<EventDeclProxy> uncontrollableAlphabet;
    private final HashSet<EventDeclProxy> controllableAlphabet;
    private final HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap;
    private final HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToIngoingEdgesMap;
    private final HashMap<String,NodeProxy> nameToLocationMap;
    private final HashSet<NodeProxy> nodes;
    private final Set<NodeProxy> initialLocations;
    private final Set<NodeProxy> acceptedLocations;
    private final Set<NodeProxy> forbiddenLocations;
    private final NodeProxy blockedLocation = null;
    private final Map<EventDeclProxy,Set<VariableComponentProxy>> guardVariables;
    private final Map<EventDeclProxy,Set<VariableComponentProxy>> actionVariables;
    private final Map<String, EventDeclProxy> eventIdToProxyMap;
    private final Set<VariableComponentProxy> usedSourceVariables;
    private final Set<VariableComponentProxy> usedTargetVariables;
    private final HashSet<SimpleExpressionProxy> allGuards;
    private final HashSet<BinaryExpressionProxy> allActions;


    @Deprecated
    public ExtendedAutomaton(final String name, final ExtendedAutomata automata, final boolean acceptingStates)
    {
        this.name = name;
        factory = ModuleSubjectFactory.getInstance();
        this.automata = automata;
        identifier = factory.createSimpleIdentifierProxy(name);
        graph = factory.createGraphProxy();
        component = factory.createSimpleComponentProxy(identifier, ComponentKind.PLANT, graph);
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        allAcceptingStates = acceptingStates;
        nodes = new HashSet<NodeProxy>();
        initialLocations = new HashSet<NodeProxy>();
        forbiddenLocations = new HashSet<NodeProxy>();
        acceptedLocations = new HashSet<NodeProxy>();
        locationToOutgoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>();
        locationToIngoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>();
        nameToLocationMap = new HashMap<String, NodeProxy>();
        alphabet = new HashSet<EventDeclProxy>();
        uncontrollableAlphabet = new HashSet<EventDeclProxy>();
        controllableAlphabet = new HashSet<EventDeclProxy>();
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        allGuards = new HashSet<SimpleExpressionProxy>();
        allActions = new HashSet<BinaryExpressionProxy>();
        usedSourceVariables = null;
        usedTargetVariables = null;
        guardVariables = null;
        actionVariables = null;

    }

    public ExtendedAutomaton(final String name, final ComponentKind kind)
    {
        this.name = name;
        this.automata = null;
        factory = ModuleSubjectFactory.getInstance();
        identifier = factory.createSimpleIdentifierProxy(name);
        graph = factory.createGraphProxy();
        component = factory.createSimpleComponentProxy(identifier, kind, graph);
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        nodes = new HashSet<NodeProxy>();
        initialLocations = new HashSet<NodeProxy>();
        forbiddenLocations = new HashSet<NodeProxy>();
        acceptedLocations = new HashSet<NodeProxy>();
        locationToOutgoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>();
        locationToIngoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>();
        nameToLocationMap = new HashMap<String, NodeProxy>();
        alphabet = new HashSet<EventDeclProxy>();
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        uncontrollableAlphabet = new HashSet<EventDeclProxy>();
        controllableAlphabet = new HashSet<EventDeclProxy>();
        allGuards = new HashSet<SimpleExpressionProxy>();
        allActions = new HashSet<BinaryExpressionProxy>();
        usedSourceVariables = null;
        usedTargetVariables = null;
        guardVariables = null;
        actionVariables = null;
    }

    public ExtendedAutomaton(final ExtendedAutomata automata, final SimpleComponentSubject component)
    {
        this.name = component.getName();
        factory = ModuleSubjectFactory.getInstance();
        this.automata = automata;
        this.component = component;
        graph = component.getGraph();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        nodes = new HashSet<NodeProxy>();
        nodes.addAll(graph.getNodes());
        controllableAlphabet = new HashSet<EventDeclProxy>();
        initialLocations = new HashSet<NodeProxy>();
        forbiddenLocations = new HashSet<NodeProxy>();
        acceptedLocations = new HashSet<NodeProxy>();
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        locationToOutgoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>(nodes.size());
        locationToIngoingEdgesMap = new HashMap<NodeProxy, ArrayList<EdgeSubject>>(nodes.size());
        nameToLocationMap = new HashMap<String, NodeProxy>(nodes.size());
        alphabet = new HashSet<EventDeclProxy>();
        uncontrollableAlphabet = new HashSet<EventDeclProxy>();
        allGuards = new HashSet<SimpleExpressionProxy>();
        allActions = new HashSet<BinaryExpressionProxy>();

        final HashMap<NodeProxy, HashSet<String>> locationToOutgoingEventsMap = new HashMap<NodeProxy, HashSet<String>>(nodes.size());

        usedSourceVariables = new HashSet<VariableComponentProxy>();
        usedTargetVariables = new HashSet<VariableComponentProxy>();
        guardVariables = new HashMap<EventDeclProxy, Set<VariableComponentProxy>>();
        actionVariables = new HashMap<EventDeclProxy, Set<VariableComponentProxy>>();

        for(final NodeProxy node:nodes)
        {
            nameToLocationMap.put(node.getName(),node);
            locationToOutgoingEdgesMap.put(node, new ArrayList<EdgeSubject>());
            locationToIngoingEdgesMap.put(node, new ArrayList<EdgeSubject>());
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

        final EventListExpressionProxy blockedEvents = component.getGraph().getBlockedEvents();
        if(blockedEvents != null)
        {
            for(final Proxy event:blockedEvents.getEventList())
            {
                final String eventName = ((SimpleIdentifierSubject)event).getName();
                final EventDeclProxy e = automata.eventIdToProxy(eventName);
                if(!eventIdToProxyMap.containsKey(eventName)){
                    eventIdToProxyMap.put(eventName, e);
                    alphabet.add(e);
                    if(e.getKind() == EventKind.CONTROLLABLE)
                        controllableAlphabet.add(e);
                    else if(e.getKind() == EventKind.UNCONTROLLABLE)
                        uncontrollableAlphabet.add(e);
                }
            }
        }


        for(final EdgeSubject edge:component.getGraph().getEdgesModifiable())
        {
            for(final Proxy event:edge.getLabelBlock().getEventList())
            {
                final String eventName = ((SimpleIdentifierSubject)event).getName();
                final EventDeclProxy e = automata.eventIdToProxy(eventName);
                //The variables that appear in the guards and actions will later be used in the weighted matrix of the PCG
                if(edge.getGuardActionBlock() != null)
                {
                    if(!edge.getGuardActionBlock().getGuards().isEmpty())
                    {
                        final SimpleExpressionProxy guard = edge.getGuardActionBlock().getGuards().get(0);
                        allGuards.add(guard);
                        final Set<VariableComponentProxy> vars = automata.extractVariablesFromExpr(guard);

                        usedSourceVariables.addAll(vars);
                        if(guardVariables.get(e) == null)
                            guardVariables.put(e, vars);
                        else
                            guardVariables.get(e).addAll(vars);

                        //Create the relations between different variables (if two variables appear in the same guard, they are related!)
                        for(final VariableComponentProxy v:vars)
                        {
                            final ArrayList<VariableComponentProxy> varsExtended = new ArrayList<VariableComponentProxy>(vars);
                            varsExtended.remove(v);
                            automata.addToRelatedVars(v, varsExtended);
                        }
                    }

                    if(!edge.getGuardActionBlock().getActions().isEmpty())
                    {
                        for(final BinaryExpressionProxy action:edge.getGuardActionBlock().getActions())
                        {
                            allActions.add(action);
                            final Set<VariableComponentProxy> vars = automata.extractVariablesFromExpr(action.getLeft());
                            usedTargetVariables.addAll(vars);
                            if(actionVariables.get(e) == null)
                                actionVariables.put(e, vars);
                            else
                                actionVariables.get(e).addAll(vars);
                        }
                    }
                }

                if(!eventIdToProxyMap.containsKey(eventName)){
                    eventIdToProxyMap.put(eventName, e);
                    alphabet.add(e);
                    if(e.getKind() == EventKind.CONTROLLABLE)
                        controllableAlphabet.add(e);
                    else if(e.getKind() == EventKind.UNCONTROLLABLE)
                        uncontrollableAlphabet.add(e);
                }

                locationToOutgoingEventsMap.get(edge.getSource()).add(e.getName());

                if(e.getKind() == EventKind.UNCONTROLLABLE)
                {
                    if(!uncontrollableAlphabet.contains(e))
                        uncontrollableAlphabet.add(e);
                }
            }
            locationToOutgoingEdgesMap.get(edge.getSource()).add(edge);
            locationToIngoingEdgesMap.get(edge.getTarget()).add(edge);
        }
    }

    public void setAllAcceptingStates(final boolean allAcceptingStates){
        this.allAcceptingStates = allAcceptingStates;
    }

    public Collection<EdgeProxy> getTransitions()
    {
        return component.getGraph().getEdges();
    }

    /**
     * Deprecated method.
     * @param expr
     * @return
     * @deprecated Use extractVariablesFromExpr method in ExtendedAutomata instead
     * @see ExtendedAutomata
     */
    @Deprecated
    public Set<VariableComponentProxy> extractVariablesFromExpr(final SimpleExpressionProxy expr)
    {
        final Set<VariableComponentProxy> vars = new HashSet<VariableComponentProxy>();
        for(final Proxy proxy:automata.getModule().getComponentList())
        {
            if(proxy instanceof VariableComponentProxy)
            {
                final VariableComponentProxy var = (VariableComponentProxy)proxy;
                if(expr.toString().contains(var.getName()))
                {
                    vars.add(var);
                }
            }
        }
        return vars;
    }
    /**
     * Returns the set of variables used in guards of transitions labeled by event @param e
     * @param e Event
     * @return Set of variables or <CODE>Null</CODE> if ExtendedAutomata is not provided in the constructor or event is not available
     */
    public Set<VariableComponentProxy> getGuardVariables(final EventDeclProxy e)
    {
        return guardVariables.get(e);
    }

    /**
     * Returns the set of variables used in actions of transitions labeled by event @param e
     * @param e Event
     * @return Set of variables or <CODE>Null</CODE> if ExtendedAutomata is not provided in the constructor or event is not available
     */
    public Set<VariableComponentProxy> getActionVariables(final EventDeclProxy e)
    {
        return actionVariables.get(e);
    }

    /**
     *
     * @return Set of variables or <CODE>Null</CODE> if ExtendedAutomata is not provided in the constructor or event is not available
     */
    public Set<VariableComponentProxy> getUsedSourceVariables()
    {
        return usedSourceVariables;
    }

    /**
     *
     * @return Set of variables or <CODE>Null</CODE> if ExtendedAutomata is not provided in the constructor or event is not available
     */
    public Set<VariableComponentProxy> getUsedTargetVariables()
    {
        return usedTargetVariables;
    }

    public NodeProxy getblockedLocation()
    {
        return blockedLocation;
    }

    public boolean isLocationPlantifiedBlocked(final NodeProxy node)
    {
        if(node.equals(blockedLocation))
            return true;

        return false;
    }

    public NodeProxy getInitialLocation()
    {
        return initialLocations.iterator().next();
    }

    public boolean isLocationInitial(final NodeProxy node)
    {
        if(initialLocations.contains(node))
            return true;
        return false;
    }

    public boolean isLocationForbidden(final NodeProxy node)
    {
        if(forbiddenLocations.contains(node))
            return true;
        return false;
    }

    public boolean isLocationAccepted(final NodeProxy node)
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

    public  HashMap<NodeProxy,ArrayList<EdgeSubject>> getLocationToIngoingEdgesMap()
    {
        return  locationToIngoingEdgesMap;
    }

    public  ArrayList<EdgeSubject> getOutgoingEdges(final NodeProxy location)
    {
        return  locationToOutgoingEdgesMap.get(location);
    }

    public  ArrayList<EdgeSubject> getIncommingEdges(final NodeProxy location)
    {
        return  locationToIngoingEdgesMap.get(location);
    }

    public ComponentKind getKind()
    {
        return component.getKind();
    }

    public List<NodeProxy> getNodes()
    {
        return new ArrayList<NodeProxy>(nodes);
    }

    public NodeProxy getLocationWithName(final String name)
    {
        return nameToLocationMap.get(name);
    }

    public EventDeclProxy getEvent(final String name)
    {
        return eventIdToProxyMap.get(name);
    }

    public EventDeclProxy getEvent(final Proxy event)
    {
        return eventIdToProxyMap.get(((SimpleIdentifierSubject)event).getName());
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
        return new ArrayList<EventDeclProxy>(alphabet);
    }

    public List<EventDeclProxy> getUncontrollableAlphabet()
    {
        return new ArrayList<EventDeclProxy>(uncontrollableAlphabet);
    }

    public List<EventDeclProxy> getControllableAlphabet()
    {
        return new ArrayList<EventDeclProxy>(controllableAlphabet);
    }

    public NodeProxy addState(final String name)
    {
        NodeProxy state;
        if (allAcceptingStates)
            state = addState(name, true, false, false);
        else
            state =  addState(name, false, false, false);

        return state;
    }

    public NodeProxy addState(final String name, final boolean accepting, final boolean initial, final boolean forbidden)
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

            final PlainEventListSubject markingProposition = factory.createPlainEventListProxy(propList);

            node = factory.createSimpleNodeProxy(name, markingProposition,initial, null, null, null);
            graph.getNodesModifiable().add (node);
            nodes.add(node);
            nameToLocationMap.put(name, node);

            if(accepting)
                acceptedLocations.add(node);
            if(forbidden)
                forbiddenLocations.add(node);
            if(initial)
                initialLocations.add(node);
        }

        return node;
    }

    public boolean isSpecification()
    {
        return (component.getKind() == ComponentKind.SPEC);
    }

    public boolean isPlant()
    {
        return (component.getKind() == ComponentKind.PLANT);
    }

    public EventDeclProxy addEvent(final String name, final String kind, final boolean observable)
    {
        EventDeclProxy event = eventIdToProxyMap.get(name);
        if(event == null){
            final SimpleIdentifierProxy ident = factory.createSimpleIdentifierProxy(name);
            if (kind.equalsIgnoreCase(EventKind.CONTROLLABLE.value())) {
                event = factory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, observable, ScopeKind.LOCAL, null, null, null);
                alphabet.add(event);
                eventIdToProxyMap.put(name, event);
                controllableAlphabet.add(event);
            } else if (kind.equalsIgnoreCase(EventKind.UNCONTROLLABLE.value())) {
                event = factory.createEventDeclProxy(ident,EventKind.UNCONTROLLABLE, observable, ScopeKind.LOCAL, null, null, null);
                alphabet.add(event);
                eventIdToProxyMap.put(name, event);
                uncontrollableAlphabet.add(event);
            } else if (kind.equalsIgnoreCase(EventKind.PROPOSITION.value())){
                event = factory.createEventDeclProxy(ident, EventKind.PROPOSITION, observable, ScopeKind.LOCAL, null, null, null);
                alphabet.add(event);
                eventIdToProxyMap.put(name, event);
            }
        }
        return event;
    }

    public boolean addEvent(final EventDeclProxy event){
        if(eventIdToProxyMap.get(event.getName()) == null){
            eventIdToProxyMap.put(event.getName(), event);
            alphabet.add(event);
            if(event.getKind() == EventKind.CONTROLLABLE)
                controllableAlphabet.add(event);
            else if(event.getKind() == EventKind.UNCONTROLLABLE)
                uncontrollableAlphabet.add(event);
            return true;
        }
        return false;
    }

    public EventDeclProxy addEvent(final String name, final String kind){
        return addEvent(name, kind, true);
    }

    /**
     * Adds transition to the extended finite automaton
     *
     * @param source  Name of the source state, if has not defined before will be added
     * @param target Name of the target state, if has not defined before will be added
     * @param label An event name or semi-colon separated list of event names for this transition, if has not defined before will be added as controllable
     * @param guardIn Guard expression for the transition. Use <code>toString</code> method to convert any SimpleExpressionProxy element
     * @param actionIn Action expression for the transition. Use <code>toString</code> method to convert any BinaryExpressionProxy element
     */
    @SuppressWarnings("serial")
    public void addTransition(final String source, final String target, final String label, final String guardIn, final String actionIn)
    {
        NodeProxy fromNode = (SimpleNodeSubject) graph.getNodesModifiable().get(source);
        if (fromNode == null)
        {
                System.out.println("ExtendedAutomaton.addTransition(): From node " + source + " does not exist and therefore, creating one!");
                fromNode = addState(source);
        }

        NodeProxy toNode = (SimpleNodeSubject) graph.getNodesModifiable().get(target);
        if (toNode == null)
        {
                System.out.println("ExtendedAutomaton.addTransition(): To node " + target + " does not exist and therefore, creating one!");
                toNode = addState(target);
        }

        // parse label into event name list and make LabelBlockSubject
        final List<Proxy> events = new LinkedList<Proxy>();
        String remainingEvents = label;
        String curEvent;
        if(label.contains(";")){
            while(remainingEvents.contains(";"))
            {
                curEvent = remainingEvents.substring(0,remainingEvents.indexOf(";"));
                remainingEvents = remainingEvents.substring(remainingEvents.indexOf(";") + 1);
                EventDeclProxy event = eventIdToProxyMap.get(curEvent);
                if (event == null){
                    System.out.println("ExtendedAutomaton.addTransition(): Event " + curEvent + " does not exist and therefore, creating one controllable!");
                    event = addEvent(curEvent, EventKind.CONTROLLABLE.name(), true);
                }
                events.add(factory.createSimpleIdentifierProxy(event.getName()));

            }
        } else {
            EventDeclProxy event = eventIdToProxyMap.get(label.trim());
            if (event == null){
                System.out.println("ExtendedAutomaton.addTransition(): Event " + label.trim() + " does not exist and therefore, creating one controllable!");
                event = addEvent(label.trim(), EventKind.CONTROLLABLE.name(), true);
            }
            events.add(factory.createSimpleIdentifierProxy(event.getName()));
        }

        final LabelBlockSubject labelBlock = factory.createLabelBlockProxy(events, null);

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
        catch (final ParseException exc)
        {
                System.out.println("ExtendedAutomaton.addTransition(): Syntax error in guard!");
                System.out.println("\t automaton: " + name);
                System.out.print("\t from: " + source);
                System.out.println(" to: " + target);
                System.out.println("\t label: " + label);
                System.out.println("\t guard: " + guardIn);
                System.out.println("\t action: " + actionIn);
                return;
        }
        // Get actions ...
        List<BinaryExpressionSubject> actions = null;
        final String actionText = actionIn;
        if (actionText != null && !actionText.trim().equals(""))
        {
                final String[] texts = actionIn.split(";");
                actions = new ArrayList<BinaryExpressionSubject>(texts.length);
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
                                        System.out.println("\t automaton: " + name);
                                        System.out.print("\t from: " + source);
                                        System.out.println(" to: " + target);
                                        System.out.println("\t label: " + label);
                                        System.out.println("\t guard: " + guardIn);
                                        System.out.println("\t action: " + actionIn);
                                        return;
                                }
                                catch (final TypeMismatchException exception)
                                {
                                        System.out.println("ExtendedAutomaton.addTransition(): Type mismatch error in action!");
                                        System.out.println("\t automaton: " + name);
                                        System.out.print("\t from: " + source);
                                        System.out.println(" to: " + target);
                                        System.out.println("\t label: " + label);
                                        System.out.println("\t guard: " + guardIn);
                                        System.out.println("\t action: " + actionIn);
                                        return;
                                }
                        }
                }
        }

        // Store parsed results ...
        final GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
        final List<SimpleExpressionSubject> blockGuards = guardActionBlock.getGuardsModifiable();
        blockGuards.clear();
        if (guard != null)
        {
            blockGuards.add(guard);
            allGuards.add(guard);
        }
        final List<BinaryExpressionSubject> blockActions = guardActionBlock.getActionsModifiable();
        blockActions.clear();
        if (actions != null)
        {
            for(final BinaryExpressionSubject action : actions){
                blockActions.add(action);
                allActions.add(action);
            }
        }

        final EdgeSubject newEdge = factory.createEdgeProxy(fromNode, toNode, labelBlock, guardActionBlock, null, null, null);
        graph.getEdgesModifiable().add(newEdge);
        final ArrayList<EdgeSubject> inEdges = locationToIngoingEdgesMap.get(toNode);
        final ArrayList<EdgeSubject> outEdges = locationToOutgoingEdgesMap.get(fromNode);
        if(inEdges == null){
            locationToIngoingEdgesMap.put(toNode, new ArrayList<EdgeSubject>(){{add(newEdge);}});
        } else {
            inEdges.add(newEdge);
        }
        if(outEdges == null){
            locationToOutgoingEdgesMap.put(fromNode, new ArrayList<EdgeSubject>(){{add(newEdge);}});
        } else {
            outEdges.add(newEdge);
        }
    }

    public void setKind(final ComponentKind kind){
        component.setKind(kind);
    }

    public void setName(final String newName){
        this.name = newName;
        component.setIdentifier(new SimpleIdentifierSubject(newName));
    }

    public HashSet<SimpleExpressionProxy> getAllGuards(){
        return allGuards;
    }

    public HashSet<BinaryExpressionProxy> getAllActions(){
        return allActions;
    }

    /**
     * Check if the current EFA is nondeterministic
     * @return The corresponding <code>NondeterministicEFAException</code>
     * or <code>Null</code> if it is deterministic.
     */
    public NondeterministicEFAException isNondeterministic(){
        if(initialLocations.size() > 1)
            return new NondeterministicEFAException(this);

        for(final NodeProxy node : locationToOutgoingEdgesMap.keySet()){
            final HashSet<EventDeclProxy> events = new HashSet<EventDeclProxy>();
            for(final EdgeSubject tran : locationToOutgoingEdgesMap.get(node)){
                for(final Proxy event : tran.getLabelBlock().getEventList()){
                    final EventDeclProxy e = getEvent(event);
                    final boolean added = events.add(e);
                    if(!added){
                        SimpleExpressionProxy guard = null;
                        try{guard = tran.getGuardActionBlock().getGuards().get(0);} catch (final Exception exp){}
                        if(guard == null)
                            return new NondeterministicEFAException(this, node, e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ExtendedAutomaton clone(){
        return new ExtendedAutomaton(automata, component.clone());
    }

    @Override
    public String toString(){
        return this.name;
    }

    public static Collection<? extends Proxy> setUnion(final Collection<? extends Proxy> x, final Collection<? extends Proxy> y){
        final Collection<Proxy> result = new HashSet<Proxy>();
        result.addAll(x);
        result.addAll(y);
        return result;
    }

    public static Collection<? extends Proxy> setIntersection(final Collection<? extends Proxy> x, final Collection<? extends Proxy> y){
        final Collection<Proxy> result = new HashSet<Proxy>(x);
        result.retainAll(y);
        return result;
    }

    public static Collection<? extends Proxy> setMinus(final Collection<? extends Proxy> x, final Collection<? extends Proxy> y){
        final Collection<Proxy> result = new HashSet<Proxy>();
        for (final Proxy n:x)
            if(!y.contains(n))
                result.add(n);
        return result;
    }

}
