
/************************** Users.java ************************/
package org.supremica.testcases;



import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Event;
import org.supremica.automata.Arc;


class User
	extends Automaton
{

	User(int id, boolean a, boolean b, boolean c)
		throws Exception
	{

		super("User " + id);

		setType(AutomatonType.Plant);

		// add events
		Event ai = new Event("a" + id);
		Event bi = new Event("b" + id);
		Event ci = new Event("c" + id);

		ai.setControllable(a);
		bi.setControllable(b);
		ci.setControllable(c);
		getAlphabet().addEvent(ai);
		getAlphabet().addEvent(bi);
		getAlphabet().addEvent(ci);

		// add states
		State init = new State("I");
		State requ = new State("R");
		State usin = new State("U");

		init.setInitial(true);
		init.setAccepting(true);
		addState(init);
		addState(requ);
		addState(usin);

		// and finally the transitions
		addArc(new Arc(init, requ, ai.getId()));
		addArc(new Arc(requ, usin, bi.getId()));
		addArc(new Arc(usin, init, ci.getId()));

		// done!
	}
}

class Fifo
	extends Automaton
{

	Fifo(int id1, int id2, boolean a, boolean b, boolean c)
		throws Exception
	{

		super("Fifo " + id1 + "x" + id2);

		setType(AutomatonType.Specification);

		// add events
		Event a1 = new Event("a" + id1);
		Event a2 = new Event("a" + id2);
		Event b1 = new Event("b" + id1);
		Event b2 = new Event("b" + id2);

		a1.setControllable(a);
		a2.setControllable(a);
		b1.setControllable(b);
		b2.setControllable(b);
		getAlphabet().addEvent(a1);
		getAlphabet().addEvent(a2);
		getAlphabet().addEvent(b1);
		getAlphabet().addEvent(b2);

		// add states
		State q0 = new State("q0");
		State q1 = new State("q1");
		State q2 = new State("q2");
		State q3 = new State("q3");
		State q4 = new State("q4");

		q0.setInitial(true);
		q0.setAccepting(true);
		addState(q0);
		addState(q1);
		addState(q2);
		addState(q3);
		addState(q4);

		// and add transitions
		addArc(new Arc(q0, q1, a1.getId()));
		addArc(new Arc(q0, q2, a2.getId()));
		addArc(new Arc(q1, q3, a2.getId()));
		addArc(new Arc(q1, q0, b1.getId()));
		addArc(new Arc(q2, q4, a1.getId()));
		addArc(new Arc(q2, q0, b2.getId()));
		addArc(new Arc(q3, q2, b1.getId()));
		addArc(new Arc(q4, q1, b2.getId()));
	}
}

class Mutex
	extends Automaton
{

	Mutex(int id1, int id2, boolean a, boolean b, boolean c)
		throws Exception
	{

		super("Mutex " + id1 + "x" + id2);

		setType(AutomatonType.Specification);

		// add events
		Event b1 = new Event("b" + id1);
		Event b2 = new Event("b" + id2);
		Event c1 = new Event("c" + id1);
		Event c2 = new Event("c" + id2);

		b1.setControllable(b);
		b2.setControllable(b);
		c1.setControllable(c);
		c2.setControllable(c);
		getAlphabet().addEvent(b1);
		getAlphabet().addEvent(b2);
		getAlphabet().addEvent(c1);
		getAlphabet().addEvent(c2);

		// add states
		State x = new State("X");
		State y = new State("Y");
		State z = new State("Z");

		x.setInitial(true);
		x.setAccepting(true);
		addState(x);
		addState(y);
		addState(z);

		// and add transitions
		addArc(new Arc(x, z, b2.getId()));
		addArc(new Arc(x, y, b1.getId()));
		addArc(new Arc(z, x, c2.getId()));
		addArc(new Arc(y, x, c1.getId()));
	}
}

public class Users
{

	Automata automata = new Automata();

	public Users(int num_users, boolean a, boolean b, boolean c)
		throws Exception
	{

		// first generate the users - numbered 1...n
		for (int i = 0; i < num_users; ++i)
		{
			automata.addAutomaton(new User(i + 1, a, b, c));
		}

		// Next go for the fifo specs - 2-by-2 for all combinations
		for (int i = 0; i < num_users; ++i)
		{
			for (int j = i + 1; j < num_users; ++j)
			{
				automata.addAutomaton(new Fifo(i + 1, j + 1, a, b, c));
			}
		}

		// And finally the mutex-specs - 2-by-2 for all combinations
		for (int i = 0; i < num_users; ++i)
		{
			for (int j = i + 1; j < num_users; ++j)
			{
				automata.addAutomaton(new Mutex(i + 1, j + 1, a, b, c));
			}
		}
	}

	public Automata getAutomata()
	{
		return automata;
	}
}
