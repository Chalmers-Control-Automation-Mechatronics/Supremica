
/** Users.java *********************** */
package org.supremica.testcases;

import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;

class User
	extends Automaton
{
    final static String SEPARATOR = ":";    // This was once ".", but then someone broke the possibility to use the dot in event/state-names

	User(final int id, final int num_resources, final boolean a, final boolean b, final boolean c)
		throws Exception
	{
		super("User_" + id);

		setType(AutomatonType.PLANT);

		final State init = new State("I");    // initial state - only one

		init.setInitial(true);
		init.setAccepting(true);
		addState(init);

		// setInitialState(init);
		// Do the following for each resource
		for (int r = 1; r <= num_resources; ++r)
		{
			final LabeledEvent ai = new LabeledEvent("a" + id + SEPARATOR + r);
			final LabeledEvent bi = new LabeledEvent("b" + id + SEPARATOR + r);
			final LabeledEvent ci = new LabeledEvent("c" + id + SEPARATOR + r);

			ai.setControllable(a);
			bi.setControllable(b);
			ci.setControllable(c);
			getAlphabet().addEvent(ai);
			getAlphabet().addEvent(bi);
			getAlphabet().addEvent(ci);

			// add states
			final State requ = new State("R" + r);
			final State usin = new State("U" + r);

			addState(requ);
			addState(usin);

			// and finally the transitions
			addArc(new Arc(init, requ, ai));
			addArc(new Arc(requ, usin, bi));
			addArc(new Arc(usin, init, ci));
		}

		// done!
	}
}

class Fifo
	extends Automaton
{
        final static String SEPARATOR = ":";    // This was once ".", but then someone broke the possibility to use the dot in event/state-names

	Fifo(final int id1, final int id2, final int resrc, final boolean a, final boolean b, final boolean c)
		throws Exception
	{
		super("Fifo_" + resrc + ":" + id1 + "x" + id2);

		setType(AutomatonType.SPECIFICATION);

		final LabeledEvent a1 = new LabeledEvent("a" + id1 + SEPARATOR + resrc);
		final LabeledEvent a2 = new LabeledEvent("a" + id2 + SEPARATOR + resrc);
		final LabeledEvent b1 = new LabeledEvent("b" + id1 + SEPARATOR + resrc);
		final LabeledEvent b2 = new LabeledEvent("b" + id2 + SEPARATOR + resrc);

		a1.setControllable(a);
		a2.setControllable(a);
		b1.setControllable(b);
		b2.setControllable(b);
		getAlphabet().addEvent(a1);
		getAlphabet().addEvent(a2);
		getAlphabet().addEvent(b1);
		getAlphabet().addEvent(b2);

		// add states
		final State q0 = new State("q0");
		final State q1 = new State("q1");
		final State q2 = new State("q2");
		final State q3 = new State("q3");
		final State q4 = new State("q4");

		q0.setInitial(true);
		q0.setAccepting(true);
		addState(q0);    // setInitialState(q0);
		addState(q1);
		addState(q2);
		addState(q3);
		addState(q4);

		// and add transitions
		addArc(new Arc(q0, q1, a1));
		addArc(new Arc(q0, q2, a2));
		addArc(new Arc(q1, q3, a2));
		addArc(new Arc(q1, q0, b1));
		addArc(new Arc(q2, q4, a1));
		addArc(new Arc(q2, q0, b2));
		addArc(new Arc(q3, q2, b1));
		addArc(new Arc(q4, q1, b2));
	}
}

class Mutex
	extends Automaton
{
	    final static String SEPARATOR = ":";    // This was once ".", but then someone broke the possibility to use the dot in event/state-names

	Mutex(final int id1, final int id2, final int resrc, final boolean a, final boolean b, final boolean c)
		throws Exception
	{
		super("Mutex_" + resrc + ":" + id1 + "x" + id2);

		setType(AutomatonType.SPECIFICATION);

		final LabeledEvent b1 = new LabeledEvent("b" + id1 + SEPARATOR + resrc);
		final LabeledEvent b2 = new LabeledEvent("b" + id2 + SEPARATOR + resrc);
		final LabeledEvent c1 = new LabeledEvent("c" + id1 + SEPARATOR + resrc);
		final LabeledEvent c2 = new LabeledEvent("c" + id2 + SEPARATOR + resrc);

		b1.setControllable(b);
		b2.setControllable(b);
		c1.setControllable(c);
		c2.setControllable(c);
		getAlphabet().addEvent(b1);
		getAlphabet().addEvent(b2);
		getAlphabet().addEvent(c1);
		getAlphabet().addEvent(c2);

		// add states
		final State x = new State("X");
		final State y = new State("Y");
		final State z = new State("Z");

		x.setInitial(true);
		x.setAccepting(true);
		addState(x);    // setInitialState(x);
		addState(y);
		addState(z);

		// and add transitions
		addArc(new Arc(x, z, b2));
		addArc(new Arc(x, y, b1));
		addArc(new Arc(z, x, c2));
		addArc(new Arc(y, x, c1));
	}
}

public class Users
{
	Project project = new Project();

	public Users(final int num_users, final int num_resources, final boolean a, final boolean b, final boolean c)
		throws Exception
	{
		project.setComment("Users competing for mutual resources. Each user can request any of the resources. The 'Fifo' specifications specifies that the users get access to the resources in the order they request them and the 'Mutex' specifications specifies that each resource is accessed by at most one user at a time. The specifications apply pairwise between users.");

		// first generate the users - numbered 1...n
		for (int i = 0; i < num_users; ++i)
		{
			project.addAutomaton(new User(i + 1, num_resources, a, b, c));
		}

		// Next go for the fifo specs - 2-by-2 for all combinations and for each resource
		for (int r = 0; r < num_resources; ++r)
		{
			for (int i = 0; i < num_users; ++i)
			{
				for (int j = i + 1; j < num_users; ++j)
				{
					project.addAutomaton(new Fifo(i + 1, j + 1, r + 1, a, b, c));
				}
			}
		}

		// And finally the mutex-specs - 2-by-2 for all combinations
		for (int r = 0; r < num_resources; ++r)
		{
			for (int i = 0; i < num_users; ++i)
			{
				for (int j = i + 1; j < num_users; ++j)
				{
					project.addAutomaton(new Mutex(i + 1, j + 1, r + 1, a, b, c));
				}
			}
		}
	}

	public Project getProject()
	{
		return project;
	}
}
