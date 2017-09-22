package org.supremica.testcases.warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.ComputerHumanExtender;
import org.supremica.automata.execution.Action;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;

class Resource
{
    private static Logger logger = LogManager.getLogger(Resource.class);
    @SuppressWarnings("unused")
	private final boolean exclusive;
    private final String identity;
    private final LinkedList<Resource> u1NextResources = new LinkedList<Resource>();
    private final LinkedList<Resource> u2NextResources = new LinkedList<Resource>();
    @SuppressWarnings("unused")
	private final User u1;
    @SuppressWarnings("unused")
	private final User u2;
    private final int x;
    private final int y;
    private Automaton theAutomaton = null;
    private final State orgInitialState;
    private final Warehouse warehouse;

    public Resource(final Warehouse warehouse, final int x, final int y, final boolean exclusive, final User u1, final User u2)
    {
        this.x = x;
        this.y = y;
        this.identity = Integer.toString(x) + Integer.toString(y);
        this.exclusive = exclusive;
        this.u1 = u1;
        this.u2 = u2;
        theAutomaton = new Automaton("Zone_" + identity);

        theAutomaton.setType(AutomatonType.SPECIFICATION);

        final State initialState = theAutomaton.createUniqueState("idle");

        initialState.setInitial(true);
        initialState.setAccepting(true);
        theAutomaton.addState(initialState);

        orgInitialState = initialState;
        this.warehouse = warehouse;
    }

    public String getIdentity()
    {
        return identity;
    }

    @Override
    public String toString()
    {
        return identity;
    }

    @Override
    public int hashCode()
    {
        return identity.hashCode();
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void addNextAGVResource(final Resource currResource)
    {
        u1NextResources.add(currResource);
    }

    public void addNextTruckResource(final Resource currResource)
    {
        u2NextResources.add(currResource);
    }

    public Automaton getAutomaton()
    {
        buildTransitions();

        return theAutomaton;
    }

    private void buildTransitions()
    {
        final State usedByAGV = theAutomaton.createUniqueState("qagv");
        final State usedByTruck = theAutomaton.createUniqueState("qtruck");

        if (getIdentity().equals("16"))
        {
            usedByTruck.setInitial(true);
            usedByTruck.setAccepting(true);
            orgInitialState.setInitial(false);
            orgInitialState.setAccepting(false);
        }
        else if (getIdentity().equals("41"))
        {
            usedByAGV.setInitial(true);
            usedByAGV.setAccepting(true);
            orgInitialState.setInitial(false);
            orgInitialState.setAccepting(false);
        }

        theAutomaton.addState(usedByAGV);
        theAutomaton.addState(usedByTruck);

        final Alphabet agvAlphabet = warehouse.getAGVAlphabet();
        final Alphabet truckAlphabet = warehouse.getTruckAlphabet();
        final Alphabet thisAlphabet = theAutomaton.getAlphabet();

        {

            // Add agv allocation event
            final LabeledEvent agvAllocationEvent = agvAlphabet.getEvent("agv" + getIdentity());
            final LabeledEvent thisAGVAllocationEvent = new LabeledEvent(agvAllocationEvent);
            thisAGVAllocationEvent.setOperatorReset(true);

            thisAlphabet.addEvent(thisAGVAllocationEvent);

            final Arc agvAllocationArc = new Arc(orgInitialState, usedByAGV, thisAGVAllocationEvent);

            try
            {
                theAutomaton.addArc(agvAllocationArc);
            }
            catch (final Exception ex)
            {
                logger.error("Could not add arc." + ex);
            }
        }

        {

            // Add agv deallocation events
            for (final Iterator<Resource> nextResIt = u1NextResources.iterator();
            nextResIt.hasNext(); )
            {
                final Resource nextResource = nextResIt.next();
                final String nextIdentity = nextResource.getIdentity();
                final LabeledEvent agvDeallocationEvent = agvAlphabet.getEvent("agv" + nextIdentity);
                final LabeledEvent thisAGVDeallocationEvent = new LabeledEvent(agvDeallocationEvent);

                thisAGVDeallocationEvent.setPrioritized(false);
                thisAGVDeallocationEvent.setOperatorReset(true);

                thisAlphabet.addEvent(thisAGVDeallocationEvent);

                final Arc agvDeallocationArc = new Arc(usedByAGV, orgInitialState, thisAGVDeallocationEvent);

                try
                {
                    theAutomaton.addArc(agvDeallocationArc);
                }
                catch (final Exception ex)
                {
                    logger.error("Could not add arc." + ex);
                }
            }
        }

        {

            // Add truck allocation event
            final String truckAllocationString = "truck" + getIdentity();
            final LabeledEvent truckAllocationEvent = truckAlphabet.getEvent(truckAllocationString);

            if (truckAllocationEvent == null)
            {
                logger.debug("Could not find event " + truckAllocationString);
            }
            else
            {
                final LabeledEvent thisTruckAllocationEvent = new LabeledEvent(truckAllocationEvent);
                truckAllocationEvent.setOperatorIncrease(true);

                thisAlphabet.addEvent(thisTruckAllocationEvent);

                final Arc truckAllocationArc = new Arc(orgInitialState, usedByTruck, thisTruckAllocationEvent);

                try
                {
                    theAutomaton.addArc(truckAllocationArc);
                }
                catch (final Exception ex)
                {
                    logger.error("Could not add arc." + ex);
                }
            }
        }

        {

            // Add truck deallocation events
            for (final Iterator<Resource> nextResIt = u2NextResources.iterator();
            nextResIt.hasNext(); )
            {
                final Resource nextResource = nextResIt.next();
                final String nextIdentity = nextResource.getIdentity();
                final LabeledEvent truckDeallocationEvent = truckAlphabet.getEvent("truck" + nextIdentity);
                final LabeledEvent thisTruckDeallocationEvent = new LabeledEvent(truckDeallocationEvent);
                truckDeallocationEvent.setOperatorIncrease(true);

                thisTruckDeallocationEvent.setPrioritized(false);
                thisAlphabet.addEvent(thisTruckDeallocationEvent);

                final Arc truckDeallocationArc = new Arc(usedByTruck, orgInitialState, thisTruckDeallocationEvent);

                try
                {
                    theAutomaton.addArc(truckDeallocationArc);
                }
                catch (final Exception ex)
                {
                    logger.error("Could not add arc." + ex);
                }
            }
        }
    }
}

class User
{
    private final boolean controllable;
    private final String identity;
    private final LinkedList<ArrayList<Object>> transitions = new LinkedList<ArrayList<Object>>();
    @SuppressWarnings("unused")
	private Resource initial;
    private final Project theProject;

    public User(final String identity, final boolean controllable, final Project theProject)
    {
        this.identity = identity;
        this.controllable = controllable;
        this.theProject = theProject;
    }

    public void addTransition(final Resource fromResource, final Resource[] toResources)
    {
        final ArrayList<Object> currTransition = new ArrayList<Object>(2);

        currTransition.add(fromResource);
        currTransition.add(toResources);
        transitions.addLast(currTransition);

        if (identity.equalsIgnoreCase("agv"))
        {
            for (int i = 0; i < toResources.length; i++)
            {
                fromResource.addNextAGVResource(toResources[i]);
            }
        }
        else if (identity.equalsIgnoreCase("truck"))
        {
            for (int i = 0; i < toResources.length; i++)
            {
                fromResource.addNextTruckResource(toResources[i]);
            }
        }
        else
        {
            System.err.println("Unknown type");
        }
    }

    public void setInitalResource(final Resource initial)
    {
        this.initial = initial;
    }

    public Automaton build()
    {
        final Automaton theAutomaton = new Automaton(identity);

        final HashMap<Resource, State> resourceMap = new HashMap<Resource, State>();

        // For each used resource add a state
        for (final Iterator<ArrayList<Object>> transitionIt = transitions.iterator();
        transitionIt.hasNext(); )
        {
            final ArrayList<?> currTransition = transitionIt.next();
            final Resource resource = (Resource) currTransition.get(0);
            final State resourceState = new State(resource.getIdentity());

            //resourceState.setAccepting(true);
            //resourceState.setId(resource.getIdentity());
            //resourceState.setName(resource.getIdentity());
            if (identity.equals("truck") && resource.getIdentity().equals("16"))
            {
                resourceState.setInitial(true);
                resourceState.setAccepting(true);
            }

            if (identity.equals("agv") && resource.getIdentity().equals("41"))
            {
                resourceState.setInitial(true);
                resourceState.setAccepting(true);
            }

            resourceMap.put(resource, resourceState);
            theAutomaton.addState(resourceState);
        }

        final Actions theActions = theProject.getActions();
        final Controls theControls = theProject.getControls();
        final Alphabet theAlphabet = theAutomaton.getAlphabet();

        // For each resource add an arc
        for (final Iterator<ArrayList<Object>> transitionIt = transitions.iterator();
        transitionIt.hasNext(); )
        {
            final ArrayList<?> currTransition = transitionIt.next();
            final Resource sourceResource = (Resource) currTransition.get(0);
            @SuppressWarnings("unused")
            final
			State sourceState = resourceMap.get(sourceResource);

            // System.err.println(identity + sourceResource.getIdentity());
            final String label = identity + sourceResource.getIdentity();
            final LabeledEvent currEvent = new LabeledEvent(label);

            //currEvent.setId(identity + sourceResource.getIdentity());
            if (!controllable)
            {
                currEvent.setControllable(false);
                //currEvent.setOperatorIncrease(true);
            }

            if (identity.equalsIgnoreCase("agv"))
            {
                currEvent.setOperatorReset(true);
            }

            if (identity.equalsIgnoreCase("truck"))
            {
                currEvent.setOperatorIncrease(true);
            }

            theAlphabet.addEvent(currEvent);

            // Add actions
            final Action theAction = new Action(label);

            theAction.addCommand(new Command(identity + ".goX." + sourceResource.getX()));
            theAction.addCommand(new Command(identity + ".goY." + sourceResource.getY()));
            theActions.addAction(theAction);

            // Add controls
            final Control theControl = new Control(label);

            theControl.addCondition(new Condition(identity + "X.end"));
            theControl.addCondition(new Condition(identity + "Y.end"));
            theControls.addControl(theControl);
        }

        // For each transition add an arc
        for (final Iterator<ArrayList<Object>> transitionIt = transitions.iterator();
        transitionIt.hasNext(); )
        {
            final ArrayList<?> currTransition = transitionIt.next();
            final Resource sourceResource = (Resource) currTransition.get(0);
            final Resource[] destResources = (Resource[]) currTransition.get(1);
            final State sourceState = resourceMap.get(sourceResource);

            for (int i = 0; i < destResources.length; i++)
            {
                final Resource destResource = destResources[i];
                final State destState = resourceMap.get(destResource);
                final LabeledEvent currEvent = theAlphabet.getEvent(identity + destResource.getIdentity());

                if (currEvent == null)
                {
                    System.err.println("currEvent is null");
                    System.exit(0);
                }

                //System.err.println("sourceState: " + sourceState + " destState: " + destState);
                if (theAutomaton.getStateWithName(sourceState.getName()) == null)
                {
                    System.err.println("source state is null");
                    System.exit(0);
                }

                if (theAutomaton.getStateWithName(destState.getName()) == null)
                {
                    System.err.println("dest state is null");
                    System.exit(0);
                }

                final Arc currArc = new Arc(sourceState, destState, currEvent);

                theAutomaton.addArc(currArc);
            }
        }

        //theAutomaton.setAlphabet(theAlphabet);
        theAutomaton.setType(AutomatonType.PLANT);

        return theAutomaton;
    }
}

public class Warehouse
{
    private static Logger logger = LogManager.getLogger(Warehouse.class);
    protected Project theProject = null;
    protected int k = 3;
    protected int m = 1;
    protected Automaton agvAutomaton = null;
    protected Automaton truckAutomaton = null;
    Resource r11 = null;
    Resource r12 = null;
    Resource r13 = null;
    Resource r14 = null;
    Resource r15 = null;
    Resource r16 = null;
    Resource r17 = null;
    Resource r18 = null;
    Resource r19 = null;
    Resource r21 = null;
    Resource r22 = null;
    Resource r23 = null;
    Resource r24 = null;
    Resource r25 = null;
    Resource r26 = null;
    Resource r27 = null;
    Resource r28 = null;
    Resource r29 = null;
    Resource r31 = null;
    Resource r32 = null;
    Resource r33 = null;
    Resource r41 = null;
    Resource r42 = null;

    public Warehouse()
    {
        this(3, 1);
    }

    public Warehouse(final int k, final int m)
    {
        this.k = k;
        this.m = m;
        theProject = new Project("Warehouse");

        final User u1 = new User("agv", true, theProject);
        final User u2 = new User("truck", false, theProject);

        r11 = new Resource(this, 1, 0, true, u1, u2);
        r12 = new Resource(this, 2, 0, true, u1, u2);
        r13 = new Resource(this, 3, 0, true, u1, u2);
        r14 = new Resource(this, 1, 1, true, u1, u2);
        r15 = new Resource(this, 2, 1, true, u1, u2);
        r16 = new Resource(this, 3, 1, true, u1, u2);
        r17 = new Resource(this, 1, 2, true, u1, u2);
        r18 = new Resource(this, 2, 2, true, u1, u2);
        r19 = new Resource(this, 3, 2, true, u1, u2);
        r21 = new Resource(this, 1, 4, true, u1, u2);
        r22 = new Resource(this, 2, 4, true, u1, u2);
        r23 = new Resource(this, 3, 4, true, u1, u2);
        r24 = new Resource(this, 1, 5, true, u1, u2);
        r25 = new Resource(this, 2, 5, true, u1, u2);
        r26 = new Resource(this, 3, 5, true, u1, u2);
        r27 = new Resource(this, 1, 6, true, u1, u2);
        r28 = new Resource(this, 2, 6, true, u1, u2);
        r29 = new Resource(this, 3, 6, true, u1, u2);
        r31 = new Resource(this, 0, 0, true, u1, u2);
        r32 = new Resource(this, 0, 3, true, u1, u2);
        r33 = new Resource(this, 0, 6, true, u1, u2);
        r41 = new Resource(this, 4, 1, true, u1, u2);
        r42 = new Resource(this, 4, 5, true, u1, u2);

        // agv
        u1.addTransition(r11, new Resource[]{ r12, r14, r31 });
        u1.addTransition(r12, new Resource[]{ r11, r13 });
        u1.addTransition(r13, new Resource[]{ r12, r16 });
        u1.addTransition(r14, new Resource[]{ r11, r15, r17 });
        u1.addTransition(r15, new Resource[]{ r14, r18 });
        u1.addTransition(r16, new Resource[]{ r13, r19, r41 });
        u1.addTransition(r17, new Resource[]{ r14, r18 });
        u1.addTransition(r18, new Resource[]{ r15, r17, r19 });
        u1.addTransition(r19, new Resource[]{ r16, r18 });
        u1.addTransition(r21, new Resource[]{ r22, r24 });
        u1.addTransition(r22, new Resource[]{ r21, r23, r25 });
        u1.addTransition(r23, new Resource[]{ r22, r26 });
        u1.addTransition(r24, new Resource[]{ r21, r25, r27 });
        u1.addTransition(r25, new Resource[]{ r22, r24, r28 });
        u1.addTransition(r26, new Resource[]{ r23, r29, r42 });
        u1.addTransition(r27, new Resource[]{ r24, r28 });
        u1.addTransition(r28, new Resource[]{ r25, r27, r29 });
        u1.addTransition(r29, new Resource[]{ r26, r28 });
        u1.addTransition(r31, new Resource[]{ r32 });
        u1.addTransition(r32, new Resource[]{ r33 });
        u1.addTransition(r33, new Resource[]{ r27 });
        u1.addTransition(r41, new Resource[]{ r16, r42 });
        u1.addTransition(r42, new Resource[]{ r26, r41 });

        // truck
        u2.addTransition(r11, new Resource[]{ r12, r14, r31 });
        u2.addTransition(r12, new Resource[]{ r11, r13 });
        u2.addTransition(r13, new Resource[]{ r12, r16 });
        u2.addTransition(r14, new Resource[]{ r11, r15, r17 });
        u2.addTransition(r15, new Resource[]{ r14, r18 });
        u2.addTransition(r16, new Resource[]{ r13, r19 });
        u2.addTransition(r17, new Resource[]{ r14, r18 });
        u2.addTransition(r18, new Resource[]{ r15, r17, r19 });
        u2.addTransition(r19, new Resource[]{ r16, r18 });
        u2.addTransition(r21, new Resource[]{ r22, r24 });
        u2.addTransition(r22, new Resource[]{ r21, r23, r25 });
        u2.addTransition(r23, new Resource[]{ r22, r26 });
        u2.addTransition(r24, new Resource[]{ r21, r25, r27 });
        u2.addTransition(r25, new Resource[]{ r22, r24 });
        u2.addTransition(r26, new Resource[]{ r23, r29 });
        u2.addTransition(r27, new Resource[]{ r24, r33 });
        u2.addTransition(r29, new Resource[]{ r26 });
        u2.addTransition(r31, new Resource[]{ r32, r11 });
        u2.addTransition(r32, new Resource[]{ r31, r33 });
        u2.addTransition(r33, new Resource[]{ r32, r27 });

        agvAutomaton = u1.build();
        truckAutomaton = u2.build();

        theProject.addAutomaton(agvAutomaton);
        theProject.addAutomaton(truckAutomaton);
    }

    public void setK(final int k)
    {
        this.k = k;
    }

    public int getK()
    {
        return k;
    }

    public void setM(final int m)
    {
        this.m = m;
    }

    public int getM()
    {
        return m;
    }

    public Alphabet getAGVAlphabet()
    {

        // return new Alphabet();
        return agvAutomaton.getAlphabet();
    }

    public Alphabet getTruckAlphabet()
    {

        // return new Alphabet();
        return truckAutomaton.getAlphabet();
    }

    protected void buildProject()
    {
        theProject.addAutomaton(r11.getAutomaton());
        theProject.addAutomaton(r12.getAutomaton());
        theProject.addAutomaton(r13.getAutomaton());
        theProject.addAutomaton(r14.getAutomaton());
        theProject.addAutomaton(r15.getAutomaton());
        theProject.addAutomaton(r16.getAutomaton());
        theProject.addAutomaton(r17.getAutomaton());
        theProject.addAutomaton(r18.getAutomaton());
        theProject.addAutomaton(r19.getAutomaton());
        theProject.addAutomaton(r21.getAutomaton());
        theProject.addAutomaton(r22.getAutomaton());
        theProject.addAutomaton(r23.getAutomaton());
        theProject.addAutomaton(r24.getAutomaton());
        theProject.addAutomaton(r25.getAutomaton());
        theProject.addAutomaton(r26.getAutomaton());
        theProject.addAutomaton(r27.getAutomaton());
        theProject.addAutomaton(r28.getAutomaton());
        theProject.addAutomaton(r29.getAutomaton());
        theProject.addAutomaton(r31.getAutomaton());
        theProject.addAutomaton(r32.getAutomaton());
        theProject.addAutomaton(r33.getAutomaton());
        theProject.addAutomaton(r41.getAutomaton());
        theProject.addAutomaton(r42.getAutomaton());

        final ComputerHumanExtender extender = new ComputerHumanExtender(theProject, k, m);

        try
        {
            extender.execute();

            final Automaton newAutomaton = extender.getNewAutomaton();

            //newAutomaton.setName("Extender");
            theProject.addAutomaton(newAutomaton);
        }
        catch (final Exception ex)
        {
            logger.error("Error in ComputerHumanExtender");
            logger.debug(ex.getStackTrace());
        }

    }

    public Project getProject()
    {

        //System.err.println("warehouse get project");
        buildProject();

        //System.err.println(theProject.nbrOfAutomata());
        //System.err.println("Warehouse getProject");
        return theProject;
    }
}
