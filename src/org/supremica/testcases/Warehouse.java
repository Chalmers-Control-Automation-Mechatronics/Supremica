package org.supremica.testcases;

import java.util.*;
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.IO.*;
import org.supremica.automata.execution.*;

class WarehouseResource
{
	private boolean exclusive;
	private String identity;
	private LinkedList u1NextResources = new LinkedList();
	private LinkedList u2NextResources = new LinkedList();
	private WarehouseUser u1;
	private WarehouseUser u2;
	private int x;
	private int y;

	public WarehouseResource(int x, int y, boolean exclusive, WarehouseUser u1, WarehouseUser u2)
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

/*
class Resource
{
	private boolean exclusive;
	private String identity;
	private LinkedList u1NextResources = new LinkedList();
	private LinkedList u2NextResources = new LinkedList();
	private User u1;
	private User u2;

	public Resource(String identity, boolean exclusive, User u1, User u2)
	{
		this.identity = identity;
		this.exclusive = exclusive;
		this.u1 = u1;
		this.u2 = u2;
	}

	public void addTransition(User u, Resource[] toResources)
	{
		 if (u == u1)
		 {
		 	for(int i = 0; i < toResources.length; i++)
		 	{
		 		if (!u1NextResources.contains(toResources[i]))
		 		{
		 			u1NextResources.addLast(toResources[i]);
		 		}
		 	}
		 }
		 else
		 {
		 	for(int i = 0; i < toResources.length; i++)
		 	{
		 		if (!u2NextResources.contains(toResources[i]))
		 		{
		 			u2NextResources.addLast(toResources[i]);
		 		}
		 	}
		 }
	}

	public Automaton build()
		throws Exception
	{
		Automaton theAutomaton = new Automaton();
		theAutomaton.setName(identity);
		Alphabet theAlphabet = theAutomaton.getAlphabet();

		State idle = new State();
		//idle.setId(identity + "idle");
		idle.setName(identity + "idle");
		idle.setAccepting(true);
		idle.setInitial(true);
		State u1 = new State();
		//u1.setId(identity + "u1");
		u1.setAccepting(true);
		u1.setName(identity + "u1");
		State u2 = new State();
		//u2.setId(identity + "u2");
		u2.setAccepting(true);
		u2.setName(identity + "u2");
		State u12 = new State();
		//u12.setId(identity + "u12");
		u12.setForbidden(true);
		u12.setName(identity + "u12");
		theAutomaton.addState(idle);
		theAutomaton.addState(u1);
		theAutomaton.addState(u2);
		theAutomaton.addState(u12);

		LabeledEvent u1books = new LabeledEvent("u1" + identity);
		//u1books.setId(u1books.getLabel());
		LabeledEvent u2books = new LabeledEvent("u2" + identity);
		//u2books.setId(u2books.getLabel());

		Arc a1 = new Arc(idle, u1, u1books);
		Arc a2 = new Arc(idle, u2, u2books);
		Arc a3 = new Arc(u1, u12, u2books);
		Arc a4 = new Arc(u2, u12, u1books);

		theAutomaton.addArc(a1);
		theAutomaton.addArc(a2);
		theAutomaton.addArc(a3);
		theAutomaton.addArc(a4);

		theAlphabet.addEvent(u1books);
		theAlphabet.addEvent(u2books);

		// Set deallocate events
		// u1
		Iterator rIt = u1NextResources.iterator();
		while (rIt.hasNext())
		{
			Resource currResource = (Resource)rIt.next();
			LabeledEvent currLabeledEvent = new LabeledEvent("u1" + currResource.identity);
			//currLabeledEvent.setId(currLabeledEvent.getLabel());
			currLabeledEvent.setPrioritized(false);
			currLabeledEvent.setControllable(true);
			theAlphabet.addEvent(currLabeledEvent);
			Arc a = new Arc(u1, idle, currLabeledEvent);
			theAutomaton.addArc(a);
		}

		// u2
		rIt = u2NextResources.iterator();
		while (rIt.hasNext())
		{
			Resource currResource = (Resource)rIt.next();
			LabeledEvent currLabeledEvent = new LabeledEvent("u2" + currResource.identity);
			//currLabeledEvent.setId(currLabeledEvent.getLabel());
			currLabeledEvent.setPrioritized(false);
			currLabeledEvent.setControllable(true);
			theAlphabet.addEvent(currLabeledEvent);
			Arc a = new Arc(u2, idle, currLabeledEvent);
			theAutomaton.addArc(a);
		}

		//theAutomaton.setAlphabet(theAlphabet);
		theAutomaton.setType(AutomatonType.Plant);
		return theAutomaton;
	}

	public String getIdentity()
	{
		return identity;
	}
}
*/

/*
class User
{
	private boolean controllable;
	private String identity;
	private LinkedList transitions = new LinkedList();
	private Resource initial;

	public User(String identity, boolean controllable)
	{
		this.identity = identity;
		this.controllable = controllable;
	}

	public void addTransition(Resource fromResource, Resource[] toResources)
	{
		ArrayList currTransition = new ArrayList(2);
		currTransition.add(fromResource);
		currTransition.add(toResources);
		transitions.addLast(currTransition);
		fromResource.addTransition(this, toResources);
	}


	public void setInitalResource(Resource initial)
	{
		this.initial = initial;
	}

	public Automaton build()
		throws Exception
	{
		Automaton theAutomaton = new Automaton();
		theAutomaton.setName(identity);
		HashMap resourceMap = new HashMap();

		// For each used resource add a state
		Iterator transitionIt = transitions.iterator();
		while(transitionIt.hasNext())
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			Resource resource = (Resource)currTransition.get(0);
			State resourceState = new State();
			resourceState.setAccepting(true);
			//resourceState.setId(resource.getIdentity());
			resourceState.setName(resource.getIdentity());
			if (identity.equals("truck") && resource.getIdentity().equals("r27"))
			{
				resourceState.setInitial(true);
			}
			if (identity.equals("agv") && resource.getIdentity().equals("r41"))
			{
				resourceState.setInitial(true);
			}
			resourceMap.put(resource, resourceState);
			theAutomaton.addState(resourceState);
		}

		Alphabet theAlphabet = theAutomaton.getAlphabet();
		// For each resource add an arc
		transitionIt = transitions.iterator();
		while(transitionIt.hasNext())
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			Resource sourceResource = (Resource)currTransition.get(0);
			State sourceState = (State)resourceMap.get(sourceResource);
			// System.err.println(identity + sourceResource.getIdentity());
			LabeledEvent currLabeledEvent = new LabeledEvent(identity + sourceResource.getIdentity());
			//currLabeledEvent.setId(identity + sourceResource.getIdentity());
			if (!controllable)
			{
				currLabeledEvent.setControllable(false);
			}
			theAlphabet.addEvent(currLabeledEvent);
		}

		// For each transition add an arc
		transitionIt = transitions.iterator();
		while(transitionIt.hasNext())
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
				Arc currArc = new Arc(sourceState, destState, currEvent);
				theAutomaton.addArc(currArc);
			}
		}


		theAutomaton.setType(AutomatonType.Plant);
		return theAutomaton;
	}
}
*/

class WarehouseUser
{
	private boolean controllable;
	private String identity;
	private LinkedList transitions = new LinkedList();
	private WarehouseResource initial;
	private Project theProject;

	public WarehouseUser(String identity, boolean controllable, Project theProject)
	{
		this.identity = identity;
		this.controllable = controllable;
		this.theProject = theProject;
	}

	public void addTransition(WarehouseResource fromResource, WarehouseResource[] toResources)
	{
		ArrayList currTransition = new ArrayList(2);
		currTransition.add(fromResource);
		currTransition.add(toResources);
		transitions.addLast(currTransition);
	}


	public void setInitalResource(WarehouseResource initial)
	{
		this.initial = initial;
	}

	public Automaton build()
		throws Exception
	{
		Automaton theAutomaton = new Automaton();
		theAutomaton.setName(identity);
		HashMap resourceMap = new HashMap();

		// For each used resource add a state
		for(Iterator transitionIt = transitions.iterator(); transitionIt.hasNext(); )
		{
			ArrayList currTransition = (ArrayList)transitionIt.next();
			WarehouseResource resource = (WarehouseResource)currTransition.get(0);
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
			WarehouseResource sourceResource = (WarehouseResource)currTransition.get(0);
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
			WarehouseResource sourceResource = (WarehouseResource)currTransition.get(0);
			WarehouseResource[] destResources = (WarehouseResource[])currTransition.get(1);
			State sourceState = (State)resourceMap.get(sourceResource);
			for (int i = 0; i < destResources.length; i++)
			{
				WarehouseResource destResource = destResources[i];
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

	public static void main(String args[])
		throws Exception
	{

		Project theAutomata = new Project();

		WarehouseUser u1 = new WarehouseUser("agv", true, theAutomata);
		WarehouseUser u2 = new WarehouseUser("truck", false, theAutomata);

		WarehouseResource r11 = new WarehouseResource(1, 0, true, u1, u2);
		WarehouseResource r12 = new WarehouseResource(2, 0, true, u1, u2);
		WarehouseResource r13 = new WarehouseResource(3, 0, true, u1, u2);
		WarehouseResource r14 = new WarehouseResource(1, 1, true, u1, u2);
		WarehouseResource r15 = new WarehouseResource(2, 1, true, u1, u2);
		WarehouseResource r16 = new WarehouseResource(3, 1, true, u1, u2);
		WarehouseResource r17 = new WarehouseResource(1, 2, true, u1, u2);
		WarehouseResource r18 = new WarehouseResource(2, 2, true, u1, u2);
		WarehouseResource r19 = new WarehouseResource(3, 2, true, u1, u2);

		WarehouseResource r21 = new WarehouseResource(1, 4, true, u1, u2);
		WarehouseResource r22 = new WarehouseResource(2, 4, true, u1, u2);
		WarehouseResource r23 = new WarehouseResource(3, 4, true, u1, u2);
		WarehouseResource r24 = new WarehouseResource(1, 5, true, u1, u2);
		WarehouseResource r25 = new WarehouseResource(2, 5, true, u1, u2);
		WarehouseResource r26 = new WarehouseResource(3, 5, true, u1, u2);
		WarehouseResource r27 = new WarehouseResource(1, 6, true, u1, u2);
		WarehouseResource r28 = new WarehouseResource(2, 6, true, u1, u2);
		WarehouseResource r29 = new WarehouseResource(3, 6, true, u1, u2);

		WarehouseResource r31 = new WarehouseResource(0, 0, true, u1, u2);
		WarehouseResource r32 = new WarehouseResource(0, 3, true, u1, u2);
		WarehouseResource r33 = new WarehouseResource(0, 6, true, u1, u2);

		WarehouseResource r41 = new WarehouseResource(4, 1, true, u1, u2);
		WarehouseResource r42 = new WarehouseResource(4, 5, true, u1, u2);

		// agv
		u1.addTransition(r11, new WarehouseResource[]{r12, r14, r31});
		u1.addTransition(r12, new WarehouseResource[]{r11, r13});
		u1.addTransition(r13, new WarehouseResource[]{r12, r16});
		u1.addTransition(r14, new WarehouseResource[]{r11, r15, r17});
		u1.addTransition(r15, new WarehouseResource[]{r14, r18});
		u1.addTransition(r16, new WarehouseResource[]{r13, r19, r41});
		u1.addTransition(r17, new WarehouseResource[]{r14, r18});
		u1.addTransition(r18, new WarehouseResource[]{r15, r17, r19});
		u1.addTransition(r19, new WarehouseResource[]{r16, r18});

		u1.addTransition(r21, new WarehouseResource[]{r22, r24});
		u1.addTransition(r22, new WarehouseResource[]{r21, r23, r25});
		u1.addTransition(r23, new WarehouseResource[]{r22, r26});
		u1.addTransition(r24, new WarehouseResource[]{r21, r25, r27});
		u1.addTransition(r25, new WarehouseResource[]{r22, r24, r28});
		u1.addTransition(r26, new WarehouseResource[]{r23, r29, r42});
		u1.addTransition(r27, new WarehouseResource[]{r24, r28});
		u1.addTransition(r28, new WarehouseResource[]{r25, r27, r29});
		u1.addTransition(r29, new WarehouseResource[]{r26, r28});

		u1.addTransition(r31, new WarehouseResource[]{r32});
		u1.addTransition(r32, new WarehouseResource[]{r33});
		u1.addTransition(r33, new WarehouseResource[]{r27});

		u1.addTransition(r41, new WarehouseResource[]{r16, r42});
		u1.addTransition(r42, new WarehouseResource[]{r26, r41});

		// truck
		u2.addTransition(r11, new WarehouseResource[]{r12, r14, r31});
		u2.addTransition(r12, new WarehouseResource[]{r11, r13});
		u2.addTransition(r13, new WarehouseResource[]{r12, r16});
		u2.addTransition(r14, new WarehouseResource[]{r11, r15, r17});
		u2.addTransition(r15, new WarehouseResource[]{r14, r18});
		u2.addTransition(r16, new WarehouseResource[]{r13, r19});
		u2.addTransition(r17, new WarehouseResource[]{r14, r18});
		u2.addTransition(r18, new WarehouseResource[]{r15, r17, r19});
		u2.addTransition(r19, new WarehouseResource[]{r16, r18});

		u2.addTransition(r21, new WarehouseResource[]{r22, r24});
		u2.addTransition(r22, new WarehouseResource[]{r21, r23, r25});
		u2.addTransition(r23, new WarehouseResource[]{r22, r26});
		u2.addTransition(r24, new WarehouseResource[]{r21, r25, r27});
		u2.addTransition(r25, new WarehouseResource[]{r22, r24});
		u2.addTransition(r26, new WarehouseResource[]{r23, r29});
		u2.addTransition(r27, new WarehouseResource[]{r24, r33});
		// u2.addTransition(r28, new Resource[]{});
		u2.addTransition(r29, new WarehouseResource[]{r26});

		u2.addTransition(r31, new WarehouseResource[]{r32, r11});
		u2.addTransition(r32, new WarehouseResource[]{r31, r33});
		u2.addTransition(r33, new WarehouseResource[]{r32, r27});

		theAutomata.addAutomaton(u1.build());
		theAutomata.addAutomaton(u2.build());

/*
		theAutomata.addAutomaton(r1.build());
		theAutomata.addAutomaton(r2.build());
		theAutomata.addAutomaton(r3.build());
		theAutomata.addAutomaton(r4.build());
		theAutomata.addAutomaton(r5.build());
		theAutomata.addAutomaton(r6.build());
		theAutomata.addAutomaton(r7.build());
		theAutomata.addAutomaton(r8.build());
		theAutomata.addAutomaton(r9.build());
*/

		ProjectToSP exporter = new ProjectToSP(theAutomata);
//		AutomataToXml exporter = new AutomataToXml(theAutomata);
		exporter.serialize(new PrintWriter(System.out));

	}
}