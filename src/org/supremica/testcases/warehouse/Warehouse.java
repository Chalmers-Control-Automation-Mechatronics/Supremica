package org.supremica.testcases.warehouse;

import java.util.*;
import java.io.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.IO.*;
import org.supremica.automata.execution.*;

class Resource
{
	private boolean exclusive;
	private String identity;
	private LinkedList u1NextResources = new LinkedList();
	private LinkedList u2NextResources = new LinkedList();
	private User u1;
	private User u2;
	private int x;
	private int y;

	public Resource(int x, int y, boolean exclusive, User u1, User u2)
	{
		this.x = x;
		this.y = y;
		this.identity = Integer.toString(x) + Integer.toString(y);
		this.exclusive = exclusive;
		this.u1 = u1;
		this.u2 = u2;
	}

	public String getIdentity()
	{
		return identity;
	}

	public String toString()
	{
		return identity;
	}

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
}


class User
{
	private boolean controllable;
	private String identity;
	private LinkedList transitions = new LinkedList();
	private Resource initial;
	private Project theProject;

	public User(String identity, boolean controllable, Project theProject)
	{
		this.identity = identity;
		this.controllable = controllable;
		this.theProject = theProject;
	}

	public void addTransition(Resource fromResource, Resource[] toResources)
	{
		ArrayList currTransition = new ArrayList(2);
		currTransition.add(fromResource);
		currTransition.add(toResources);
		transitions.addLast(currTransition);
	}


	public void setInitalResource(Resource initial)
	{
		this.initial = initial;
	}

	public Automaton build()
	{
		Automaton theAutomaton = new Automaton();
		theAutomaton.setName(identity);
		HashMap resourceMap = new HashMap();

		// For each used resource add a state
		for(Iterator transitionIt = transitions.iterator(); transitionIt.hasNext(); )
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			Resource resource = (Resource)currTransition.get(0);
			State resourceState = new State(resource.getIdentity());
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

		Actions theActions = theProject.getActions();
		Controls theControls = theProject.getControls();

		Alphabet theAlphabet = theAutomaton.getAlphabet();
		// For each resource add an arc
		for(Iterator transitionIt = transitions.iterator(); transitionIt.hasNext(); )
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			Resource sourceResource = (Resource)currTransition.get(0);
			State sourceState = (State)resourceMap.get(sourceResource);
			// System.err.println(identity + sourceResource.getIdentity());
			String label = identity + sourceResource.getIdentity();
			LabeledEvent currEvent = new LabeledEvent(label);
			//currEvent.setId(identity + sourceResource.getIdentity());
			if (!controllable)
			{
				currEvent.setControllable(false);
			}
			theAlphabet.addEvent(currEvent);

			// Add actions
			Action theAction = new Action(label);
			theAction.addCommand(new Command(identity + ".goX." + sourceResource.getX()));
			theAction.addCommand(new Command(identity + ".goY." + sourceResource.getY()));
			theActions.addAction(theAction);

			// Add controls
			Control theControl = new Control(label);
			theControl.addCondition(new Condition(identity + "X.end"));
			theControl.addCondition(new Condition(identity + "Y.end"));
			theControls.addControl(theControl);
		}

		// For each transition add an arc
		for(Iterator transitionIt = transitions.iterator(); transitionIt.hasNext(); )
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			Resource sourceResource = (Resource)currTransition.get(0);
			Resource[] destResources = (Resource[])currTransition.get(1);
			State sourceState = (State)resourceMap.get(sourceResource);
			for (int i = 0; i < destResources.length; i++)
			{
				Resource destResource = destResources[i];
				State destState = (State)resourceMap.get(destResource);
				LabeledEvent currEvent = theAlphabet.getEventWithLabel(identity + destResource.getIdentity());
				if (currEvent == null)
				{
					System.err.println("currEvent is null");
					System.exit(0);
				}
				//System.err.println("sourceState: " + sourceState + " destState: " + destState);
				if (theAutomaton.getState(sourceState) == null)
				{
					System.err.println("source state is null");
					System.exit(0);
				}
				if (theAutomaton.getState(destState) == null)
				{
					System.err.println("dest state is null");
					System.exit(0);
				}
				Arc currArc = new Arc(sourceState, destState, currEvent);
				theAutomaton.addArc(currArc);
			}
		}

		//theAutomaton.setAlphabet(theAlphabet);
		theAutomaton.setType(AutomatonType.Plant);
		return theAutomaton;
	}
}


public class Warehouse
{
	private static Logger logger = LoggerFactory.createLogger(Warehouse.class);

	protected Project theProject= null;	
	protected int k = 3;
	protected int l = 1;
	
	public Warehouse(int k, int l)
	{
		this.k = k;
		this.l = l;
	}
	
	protected void buildProject()
	{
		theProject = new Project("Warehouse");	
		
		User u1 = new User("agv", true, theProject);
		User u2 = new User("truck", false, theProject);

		Resource r11 = new Resource(1, 0, true, u1, u2);
		Resource r12 = new Resource(2, 0, true, u1, u2);
		Resource r13 = new Resource(3, 0, true, u1, u2);
		Resource r14 = new Resource(1, 1, true, u1, u2);
		Resource r15 = new Resource(2, 1, true, u1, u2);
		Resource r16 = new Resource(3, 1, true, u1, u2);
		Resource r17 = new Resource(1, 2, true, u1, u2);
		Resource r18 = new Resource(2, 2, true, u1, u2);
		Resource r19 = new Resource(3, 2, true, u1, u2);

		Resource r21 = new Resource(1, 4, true, u1, u2);
		Resource r22 = new Resource(2, 4, true, u1, u2);
		Resource r23 = new Resource(3, 4, true, u1, u2);
		Resource r24 = new Resource(1, 5, true, u1, u2);
		Resource r25 = new Resource(2, 5, true, u1, u2);
		Resource r26 = new Resource(3, 5, true, u1, u2);
		Resource r27 = new Resource(1, 6, true, u1, u2);
		Resource r28 = new Resource(2, 6, true, u1, u2);
		Resource r29 = new Resource(3, 6, true, u1, u2);

		Resource r31 = new Resource(0, 0, true, u1, u2);
		Resource r32 = new Resource(0, 3, true, u1, u2);
		Resource r33 = new Resource(0, 6, true, u1, u2);

		Resource r41 = new Resource(4, 1, true, u1, u2);
		Resource r42 = new Resource(4, 5, true, u1, u2);

		// agv
		u1.addTransition(r11, new Resource[]{r12, r14, r31});
		u1.addTransition(r12, new Resource[]{r11, r13});
		u1.addTransition(r13, new Resource[]{r12, r16});
		u1.addTransition(r14, new Resource[]{r11, r15, r17});
		u1.addTransition(r15, new Resource[]{r14, r18});
		u1.addTransition(r16, new Resource[]{r13, r19, r41});
		u1.addTransition(r17, new Resource[]{r14, r18});
		u1.addTransition(r18, new Resource[]{r15, r17, r19});
		u1.addTransition(r19, new Resource[]{r16, r18});

		u1.addTransition(r21, new Resource[]{r22, r24});
		u1.addTransition(r22, new Resource[]{r21, r23, r25});
		u1.addTransition(r23, new Resource[]{r22, r26});
		u1.addTransition(r24, new Resource[]{r21, r25, r27});
		u1.addTransition(r25, new Resource[]{r22, r24, r28});
		u1.addTransition(r26, new Resource[]{r23, r29, r42});
		u1.addTransition(r27, new Resource[]{r24, r28});
		u1.addTransition(r28, new Resource[]{r25, r27, r29});
		u1.addTransition(r29, new Resource[]{r26, r28});

		u1.addTransition(r31, new Resource[]{r32});
		u1.addTransition(r32, new Resource[]{r33});
		u1.addTransition(r33, new Resource[]{r27});

		u1.addTransition(r41, new Resource[]{r16, r42});
		u1.addTransition(r42, new Resource[]{r26, r41});

		// truck
		u2.addTransition(r11, new Resource[]{r12, r14, r31});
		u2.addTransition(r12, new Resource[]{r11, r13});
		u2.addTransition(r13, new Resource[]{r12, r16});
		u2.addTransition(r14, new Resource[]{r11, r15, r17});
		u2.addTransition(r15, new Resource[]{r14, r18});
		u2.addTransition(r16, new Resource[]{r13, r19});
		u2.addTransition(r17, new Resource[]{r14, r18});
		u2.addTransition(r18, new Resource[]{r15, r17, r19});
		u2.addTransition(r19, new Resource[]{r16, r18});

		u2.addTransition(r21, new Resource[]{r22, r24});
		u2.addTransition(r22, new Resource[]{r21, r23, r25});
		u2.addTransition(r23, new Resource[]{r22, r26});
		u2.addTransition(r24, new Resource[]{r21, r25, r27});
		u2.addTransition(r25, new Resource[]{r22, r24});
		u2.addTransition(r26, new Resource[]{r23, r29});
		u2.addTransition(r27, new Resource[]{r24, r33});
		u2.addTransition(r29, new Resource[]{r26});

		u2.addTransition(r31, new Resource[]{r32, r11});
		u2.addTransition(r32, new Resource[]{r31, r33});
		u2.addTransition(r33, new Resource[]{r32, r27});

		theProject.addAutomaton(u1.build());
		theProject.addAutomaton(u2.build());
		
		ComputerHumanExtender extender = new ComputerHumanExtender(theProject, k);

		try
		{
			extender.execute();
			Automaton newAutomaton = extender.getNewAutomaton();
			theProject.addAutomaton(newAutomaton);
		}
		catch (Exception ex)
		{
			logger.error("Error in ComputerHumanExtender");
			logger.debug(ex.getStackTrace());
		}		
	}

	public Project getProject()
	{
		buildProject();
		return theProject;	
	}
}