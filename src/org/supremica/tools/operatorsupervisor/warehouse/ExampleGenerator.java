package org.supremica.tools.operatorsupervisor.warehouse;

import java.util.*;
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.IO.*;

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

		return theAutomaton;
	}

	public String getIdentity()
	{
		return identity;
	}
}

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
			if (identity.equals("u1") && resource.getIdentity().equals("r3"))
			{
				resourceState.setInitial(true);
			}
			if (identity.equals("u2") && resource.getIdentity().equals("r9"))
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



		return theAutomaton;
	}
}


public class ExampleGenerator
{

	public static void main(String args[])
		throws Exception
	{

		User u1 = new User("u1", true);
		User u2 = new User("u2", false);

		Resource r1 = new Resource("r1", true, u1, u2);
		Resource r2 = new Resource("r2", true, u1, u2);
		Resource r3 = new Resource("r3", true, u1, u2);
		Resource r4 = new Resource("r4", true, u1, u2);
		Resource r5 = new Resource("r5", true, u1, u2);
		Resource r6 = new Resource("r6", true, u1, u2);
		Resource r7 = new Resource("r7", true, u1, u2);
		Resource r8 = new Resource("r8", true, u1, u2);
		Resource r9 = new Resource("r9", true, u1, u2);

		// User 1
		u1.addTransition(r1, new Resource[]{r2});
		u1.addTransition(r2, new Resource[]{r1, r3, r5});
		u1.addTransition(r3, new Resource[]{r2, r6});
		u1.addTransition(r4, new Resource[]{});
		u1.addTransition(r5, new Resource[]{r2, r6, r8});
		u1.addTransition(r6, new Resource[]{r3, r5, r9});
		u1.addTransition(r7, new Resource[]{r8});
		u1.addTransition(r8, new Resource[]{r5, r7, r9});
		u1.addTransition(r9, new Resource[]{r6, r8});

		// User 2
		u2.addTransition(r1, new Resource[]{r2, r4});
		u2.addTransition(r2, new Resource[]{r1, r3});
		u2.addTransition(r3, new Resource[]{r2, r6});
		u2.addTransition(r4, new Resource[]{r1, r5, r7});
		u2.addTransition(r5, new Resource[]{r4, r6});
		u2.addTransition(r6, new Resource[]{r3, r5, r9});
		u2.addTransition(r7, new Resource[]{r4, r8});
		u2.addTransition(r8, new Resource[]{r7, r9});
		u2.addTransition(r9, new Resource[]{r6, r8});

		Project theAutomata = new Project();
		theAutomata.addAutomaton(u1.build());
		theAutomata.addAutomaton(u2.build());

		theAutomata.addAutomaton(r1.build());
		theAutomata.addAutomaton(r2.build());
		theAutomata.addAutomaton(r3.build());
		theAutomata.addAutomaton(r4.build());
		theAutomata.addAutomaton(r5.build());
		theAutomata.addAutomaton(r6.build());
		theAutomata.addAutomaton(r7.build());
		theAutomata.addAutomaton(r8.build());
		theAutomata.addAutomaton(r9.build());

		ProjectToSP exporter = new ProjectToSP(theAutomata);
		exporter.serialize(new PrintWriter(System.out));

	}
}