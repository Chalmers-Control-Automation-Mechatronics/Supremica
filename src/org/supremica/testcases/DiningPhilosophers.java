/*************** DiningPhilosophers.java ******************/
package org.supremica.testcases;

import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Event;
import org.supremica.automata.Arc;

// Builds a Philo automaton
class Philosopher
{
	static State[] states =
	{
		new State("s0"),
		new State("lu"), // left fork picked up
		new State("ru"), // right fork picked up
		new State("eat"),
		new State("ld"), // left fork put down
		new State("rd") // right fork put down
 	};
 							
 	static final int INIT = 0;
 	static final int L_UP = 1;
 	static final int R_UP = 2;
 	static final int EAT = 3;
 	static final int L_DN = 4;
 	static final int R_DN = 5;
 	
	static Event[] events = 
	{ 	
		new Event("L_take"), // pick up left
		new Event("R_take"), // pick up right
		new Event("L_put"), // put down left
		new Event("R_put"), // put down right
	};
	
	static final int L_TAKE = 0;
	static final int R_TAKE = 1;
	static final int L_PUT = 2;
	static final int R_PUT = 3;
	
	static Automaton philo = null;
	static boolean inited = false;
	
	public Philosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put) 
		throws Exception
	{
		if(inited)
		{
			return;
		}

		// Here we create the "template" automaton, philo
		philo = new Automaton("Philo template");
		philo.setType(AutomatonType.Plant);
		
		// These are fivestate automata
		states[0].setInitial(true);
		
		for(int i = 0; i < states.length; ++i)
		{
			philo.addState(states[i]);
		}
		
		// Now the events, these should be (re)named uniquely for each philosopher 
		// (each fork-pair, actually)
		events[L_TAKE].setControllable(l_take);
		events[R_TAKE].setControllable(r_take);
		events[L_PUT].setControllable(l_put);
		events[R_PUT].setControllable(r_put);
		
		for(int i = 0; i < events.length; ++i)
		{
			philo.getAlphabet().addEvent(events[i]);
		}

		// And finally the arcs - first the left side (where the left is picked up 
		// and put down first)
		philo.addArc(new Arc(states[INIT], states[L_UP], events[L_TAKE].getId()));
		philo.addArc(new Arc(states[L_UP], states[EAT], events[R_TAKE].getId()));
		philo.addArc(new Arc(states[EAT], states[L_DN], events[L_PUT].getId()));
		philo.addArc(new Arc(states[L_DN], states[INIT], events[R_PUT].getId()));
		// And then the right side (where th eright fork is picked up and put down first)
		philo.addArc(new Arc(states[INIT], states[R_UP], events[R_TAKE].getId()));
		philo.addArc(new Arc(states[R_UP], states[EAT], events[L_TAKE].getId()));
		philo.addArc(new Arc(states[EAT], states[R_DN], events[R_PUT].getId()));
		philo.addArc(new Arc(states[R_DN], states[INIT], events[L_PUT].getId()));

		inited = true;
	}
	
	public Automaton build(int id, int l_fork, int r_fork) throws Exception
	{
		Automaton sm = new Automaton(philo); // deep copy, I hope
		sm.setName("Philo:" + id);
		
		// adjust the event names according to l_fork and r_fork
		// L_take becomes take<id>.<l_fork>
		// R_take becomes take<id>.<r_fork>
		// L_put becomes put<id>.<l_fork>
		// R_put becomes put<id>.<r_fork>
		Alphabet alpha = sm.getAlphabet();
		alpha.getEventWithId("L_take").setLabel("take" + id +"." + l_fork);
		alpha.getEventWithId("R_take").setLabel("take" + id +"." + r_fork);
		alpha.getEventWithId("L_put").setLabel("put" + id +"." + l_fork);
		alpha.getEventWithId("R_put").setLabel("put" + id +"." + r_fork);

		alpha.rehash();

		return sm;
	}
}

// Builds a chopstick automaton
class Chopstick
{
	static State[] states = 
	{ 
		new State("0"), 
		new State("1") 
	};
	static Event[] events = 
	{
		new Event("L_up"), 
		new Event("R_up"), 
		new Event("L_dn"), 
		new Event("R_dn") 
	};
	static final int L_TAKE = 0;
	static final int R_TAKE = 1;
	static final int L_PUT = 2;
	static final int R_PUT = 3;
	
	static Automaton fork = null;
	static boolean inited = false;
	
	public Chopstick(boolean l_take, boolean r_take, boolean l_put, boolean r_put) 
		throws Exception
	{
		if(inited)
			return;
		
		fork = new Automaton("Fork template");
		fork.setType(AutomatonType.Plant);

		// First the states
		states[0].setInitial(true);

		for(int i = 0; i < states.length; ++i)
		{
			fork.addState(states[i]);
		}
		// Now the events
		events[L_TAKE].setControllable(l_take);
		events[R_TAKE].setControllable(r_take);
		events[L_PUT].setControllable(l_put);
		events[R_PUT].setControllable(r_put);

		for(int i = 0; i < events.length; ++i)
		{
			fork.getAlphabet().addEvent(events[i]);
		}
		// And finally the arcs - there's four of them
		fork.addArc(new Arc(states[0], states[1], events[0].getId()));
		fork.addArc(new Arc(states[0], states[1], events[1].getId()));
		fork.addArc(new Arc(states[1], states[0], events[2].getId()));
		fork.addArc(new Arc(states[1], states[0], events[3].getId()));
		
		inited = true;
	}
	Automaton build(int id, int l_philo, int r_philo) throws Exception
	{
		Automaton sm = new Automaton(fork); // deep copy, I hope
		sm.setName("Fork:" + id);
		
		Alphabet alpha = sm.getAlphabet();
		alpha.getEventWithId("L_up").setLabel("take" + l_philo + "." + id);		
		alpha.getEventWithId("R_up").setLabel("take" + r_philo + "." + id);		
		alpha.getEventWithId("L_dn").setLabel("put" + l_philo + "." + id);		
		alpha.getEventWithId("R_dn").setLabel("put" + r_philo + "." + id);	
			
		alpha.rehash();

		return sm;	
	}
}

public class DiningPhilosophers
{
	Automata automata = new Automata();
	
	// These are helpers for counting modulo num philos/forks
	// Note that we adjust for 0's, indices are from 1 to modulo
	int nextId(int id, int modulo)
	{
		int nxt = id + 1;
		if(nxt > modulo)
			return nxt - modulo;
		else
			return nxt;
	}
	int prevId(int id, int modulo)
	{
		int nxt = id - 1;
		if(nxt <= 0)
			return modulo;
		else
			return nxt;
	}
	
	public DiningPhilosophers(int num, boolean l_take, boolean r_take, boolean l_put, boolean r_put) 
		throws Exception
	{
		// First the philosphers
		Philosopher philo = new Philosopher(l_take, r_take, l_put, r_put);
		for(int i = 0; i < num; ++i)
		{
			int id = i + 1; // id's are from 1...n
			automata.addAutomaton(philo.build(id, id, prevId(id, num)));
			// To his left a philo has fork #id, and to his right is fork #id-1
		}
		// Next the forks aka chopsticks
		Chopstick fork = new Chopstick(l_take, r_take, l_put, r_put);
		for(int i = 0; i < num; ++i)
		{
			int id = i + 1; // id's are from 1...n
			automata.addAutomaton(fork.build(id, nextId(id, num), id));
			// To its left a fork has philo #id+1, and to its right philo #id
		}
	}

	public Automata getAutomata()
	{
		return automata;
	}
	
}
