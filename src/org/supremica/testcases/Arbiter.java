/** Arbiter.java ******************** */
package org.supremica.testcases;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizer;

/**
 * @author Hugo
 */
public class Arbiter
{
	private Project project;
	private int arbiterCount = 0;
	private int userCount = 0;
	private String rootName = "s";
	private boolean synchronize;

	Automaton server = new Automaton();
	Automata arbiters = new Automata();
	Automata users = new Automata();

	public Arbiter(int nbrOfUsers, boolean synchronize)
		throws Exception
	{
		project = new Project("Arbiter tree structure");
		project.setComment("Tree arbiter cell structure adapted from 'Compositional Model Checking' by E.M. Clarke et. al. Each arbiter cell has three communication channels, two users and one server. The cell gets requests from its two users and as a response, initiates requests events to the server. Each user/server initates two requests before returning to its initial state. The system is nonblocking for any number of users.");

		// Synchronize arbiter?
		this.synchronize = synchronize;
	
		// Start the recursion with the root node, the server
		String serverName = buildServer();
		String arbiterName = "a" + arbiterCount++;

		// Split the users between the two branches!
		int half = nbrOfUsers/2;
		int rest = nbrOfUsers-half;
		String user0 = buildArbiter(half, arbiterName);
		String user1 = buildArbiter(rest, arbiterName);

		buildArbiter(user0, user1, arbiterName, serverName);

		project.addAutomaton(server);
		project.addAutomata(arbiters);
		project.addAutomata(users);
	}

	private String buildArbiter(int nbrOfUsers, String serverName)
	{
		if (nbrOfUsers == 1)
		{
			// This is a leaf, a user!
			return buildUser();
		}
		
		// This is an arbiter cell with two users (possibly arbiters)! 
		String name = "a" + arbiterCount++;

		// Split the users between the two branches!
		int half = nbrOfUsers/2;
		int rest = nbrOfUsers-half;
		String user0 = buildArbiter(half, name);
		String user1 = buildArbiter(rest, name);

		return buildArbiter(user0, user1, name, serverName);
	}

	private String buildArbiter(String user0, String user1, String arbiterName, String serverName)
	{
		// Arbiter alphabet
		LabeledEvent r0 = new LabeledEvent("r" + user0);
		LabeledEvent t0 = new LabeledEvent("t" + user0);
		LabeledEvent a0 = new LabeledEvent("a" + user0);
		LabeledEvent r1 = new LabeledEvent("r" + user1);
		LabeledEvent t1 = new LabeledEvent("t" + user1);
		LabeledEvent a1 = new LabeledEvent("a" + user1);
		LabeledEvent rs;
		LabeledEvent as;
		// If the server is the root, the server events are labeled differently...
		if (serverName.equals(rootName))
		{
			rs = new LabeledEvent("r" + serverName);
			as = new LabeledEvent("a" + serverName);
		}
		else
		{
			rs = new LabeledEvent("r" + arbiterName);
			as = new LabeledEvent("a" + arbiterName);
		}

		// Arbiter automaton
		Automaton arbiter = new Automaton("Arbiter " + arbiterName);
		{
			Alphabet alpha = arbiter.getAlphabet();
			
			State[] states =
			{
				new State("0"),
				new State("1"),
				new State("2"),
				new State("3"),
				new State("4"),
				new State("5"),
				new State("6"),
				new State("7"),
				new State("8"),
				new State("9"),
				new State("10")
			};
			for (int i = 0; i < states.length; ++i)
			{
				arbiter.addState(states[i]);
			}
			
			State initialState = states[0];
			initialState.setAccepting(true);
			arbiter.setInitialState(initialState);
			
			alpha.addEvent(r0);
			alpha.addEvent(r1);
			alpha.addEvent(rs);
			arbiter.addArc(new Arc(states[0], states[1], r0));
			arbiter.addArc(new Arc(states[0], states[2], r1));
			arbiter.addArc(new Arc(states[1], states[3], r1));
			arbiter.addArc(new Arc(states[1], states[4], rs));
			arbiter.addArc(new Arc(states[2], states[3], r0));
			arbiter.addArc(new Arc(states[2], states[5], rs));
			arbiter.addArc(new Arc(states[3], states[7], rs));
			arbiter.addArc(new Arc(states[4], states[6], r0));
			arbiter.addArc(new Arc(states[4], states[7], r1));
			arbiter.addArc(new Arc(states[5], states[7], r0));
			arbiter.addArc(new Arc(states[5], states[8], r1));
			arbiter.addArc(new Arc(states[6], states[9], r1));
			arbiter.addArc(new Arc(states[6], states[0], rs));
			arbiter.addArc(new Arc(states[7], states[9], r0));
			arbiter.addArc(new Arc(states[7], states[10], r1));
			arbiter.addArc(new Arc(states[8], states[10], r0));
			arbiter.addArc(new Arc(states[8], states[0], rs));
			arbiter.addArc(new Arc(states[9], states[2], rs));
			arbiter.addArc(new Arc(states[10], states[1], rs));
			arbiter.setType(AutomatonType.Specification);
		}

		// Communication channel C0
		Automaton arb0 = new Automaton("Com " + arbiterName + " - " + user0);
		{		
			Alphabet alpha = arb0.getAlphabet();
			
			State[] states =
			{
				new State("0"),
				new State("1"),
				new State("2"),
				new State("3"),
				new State("4"),
				new State("5")
			};
			for (int i = 0; i < states.length; ++i)
			{
				arb0.addState(states[i]);
			}
			
			State initialState = states[0];
			initialState.setAccepting(true);
			arb0.setInitialState(initialState);
			
			alpha.addEvent(r0);
			alpha.addEvent(t0);
			alpha.addEvent(a0);
			arb0.addArc(new Arc(states[0], states[1], r0));
			arb0.addArc(new Arc(states[1], states[2], t0));
			arb0.addArc(new Arc(states[2], states[3], a0));
			arb0.addArc(new Arc(states[3], states[4], r0));
			arb0.addArc(new Arc(states[4], states[5], t0));
			arb0.addArc(new Arc(states[5], states[0], a0));
			arb0.setType(AutomatonType.Specification);
		}

		// Communication channel C1
		Automaton arb1 = new Automaton("Com " + arbiterName + " - " + user1);
		{
			Alphabet alpha = arb1.getAlphabet();
			
			State[] states =
			{
				new State("0"),
				new State("1"),
				new State("2"),
				new State("3"),
				new State("4"),
				new State("5")
			};
			for (int i = 0; i < states.length; ++i)
			{
				arb1.addState(states[i]);
			}
			
			State initialState = states[0];
			initialState.setAccepting(true);
			arb1.setInitialState(initialState);
			
			alpha.addEvent(r1);
			alpha.addEvent(t1);
			alpha.addEvent(a1);
			arb1.addArc(new Arc(states[0], states[1], r1));
			arb1.addArc(new Arc(states[1], states[2], t1));
			arb1.addArc(new Arc(states[2], states[3], a1));
			arb1.addArc(new Arc(states[3], states[4], r1));
			arb1.addArc(new Arc(states[4], states[5], t1));
			arb1.addArc(new Arc(states[5], states[0], a1));
			arb1.setType(AutomatonType.Specification);
		}

		// Communication channel Cp
		Automaton arbs = new Automaton("Com " + arbiterName + " - " + serverName);
		{
			Alphabet alpha = arbs.getAlphabet();
			
			State[] states =
			{
				new State("0"),
				new State("1"),
				new State("2"),
				new State("3"),
				new State("4"),
				new State("5"),
				new State("6"),
				new State("7"),
				new State("8")
			};
			for (int i = 0; i < states.length; ++i)
			{
				arbs.addState(states[i]);
			}
			
			State initialState = states[0];
			initialState.setAccepting(true);
			arbs.setInitialState(initialState);
			
			alpha.addEvent(t0);
			alpha.addEvent(t1);
			alpha.addEvent(rs);
			alpha.addEvent(as);
			arbs.addArc(new Arc(states[0], states[1], rs));
			arbs.addArc(new Arc(states[1], states[2], as));
			arbs.addArc(new Arc(states[2], states[3], t0));
			arbs.addArc(new Arc(states[2], states[4], t1));
			arbs.addArc(new Arc(states[3], states[5], rs));
			arbs.addArc(new Arc(states[4], states[6], rs));
			arbs.addArc(new Arc(states[5], states[7], as));
			arbs.addArc(new Arc(states[6], states[8], as));
			arbs.addArc(new Arc(states[7], states[0], t0));
			arbs.addArc(new Arc(states[8], states[0], t1));
			arbs.setType(AutomatonType.Specification);
		}
		

		if (synchronize)
		{
			// Synchronize the arbiter and the communication channels!
			Automata synchAutomata = new Automata();
			synchAutomata.addAutomaton(arbiter);
			synchAutomata.addAutomaton(arb0);
			synchAutomata.addAutomaton(arb1);
			synchAutomata.addAutomaton(arbs);
			try
			{
				Automaton synch = AutomataSynchronizer.synchronizeAutomata(synchAutomata);
				synch.setName("Arbiter cell " + arbiterName);
				arbiters.addAutomaton(synch);
			}
			catch (Exception ex)
			{
				System.err.println("Error when synchronizing arbiter.");;
			}
		}
		else
		{
			// Add the arbiter and communication channels separately   
			arbiters.addAutomaton(arbiter);
			arbiters.addAutomaton(arb0);
			arbiters.addAutomaton(arb1);
			arbiters.addAutomaton(arbs);
		}
		
		return arbiterName;
	}

	private String buildServer()
	{
		String name = rootName;
		server.setName("Server");
		Alphabet alpha = server.getAlphabet();

		State[] states =
		{
			new State("0"),
			new State("1"),
			new State("2"),
			new State("3")
		};
		for (int i = 0; i < states.length; ++i)
		{
			server.addState(states[i]);
		}

		State initialState = states[0];
		initialState.setAccepting(true);
		server.setInitialState(initialState);
		
		LabeledEvent r = new LabeledEvent("r" + name);
		LabeledEvent a = new LabeledEvent("a" + name);
		alpha.addEvent(r);
		alpha.addEvent(a);
		server.addArc(new Arc(states[0], states[1], r));
		server.addArc(new Arc(states[1], states[2], a));
		server.addArc(new Arc(states[2], states[3], r));
		server.addArc(new Arc(states[3], states[0], a));
		server.setType(AutomatonType.Plant);

		return name;
	}

	private String buildUser()
	{
		String name = "u" + userCount++;
		Automaton user = new Automaton("User " + name);
		Alphabet alpha = user.getAlphabet();

		State[] states =
		{
			new State("0"),
			new State("1"),
			new State("2"),
			new State("3")
		};
		for (int i = 0; i < states.length; ++i)
		{
			user.addState(states[i]);
		}

		State initialState = states[0];
		initialState.setAccepting(true);
		user.setInitialState(initialState);
		
		LabeledEvent r = new LabeledEvent("r" + name);
		LabeledEvent a = new LabeledEvent("a" + name);
		alpha.addEvent(r);
		alpha.addEvent(a);
		user.addArc(new Arc(states[0], states[1], r));
		user.addArc(new Arc(states[1], states[2], a));
		user.addArc(new Arc(states[2], states[3], r));
		user.addArc(new Arc(states[3], states[0], a));
		user.setType(AutomatonType.Plant);

		users.addAutomaton(user);
		return name;
	}

	public Project getProject()
	{
		return project;
	}
}
