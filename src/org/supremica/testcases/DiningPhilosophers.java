
/** DiningPhilosophers.java ***************** */
package org.supremica.testcases;

import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.execution.*;

// Builds a Philo automaton
class Philosopher
{
	static State[] states =
	{
		new State("s0"), new State("lu"),

		// left fork picked up
		new State("ru"),

		// right fork picked up
		new State("eat"), new State("ld"),

		// left fork put down
		new State("rd")

		// right fork put down
	};
	final static int INIT = 0;
	final static int L_UP = 1;
	final static int R_UP = 2;
	final static int EAT = 3;
	final static int L_DN = 4;
	final static int R_DN = 5;
	static LabeledEvent[] events =
	{
		new LabeledEvent("L_take"),

		// pick up left
		new LabeledEvent("R_take"),

		// pick up right
		new LabeledEvent("L_put"),

		// put down left
		new LabeledEvent("R_put"),

		// put down right
	};
	final static int L_TAKE = 0;
	final static int R_TAKE = 1;
	final static int L_PUT = 2;
	final static int R_PUT = 3;
	final static String LABEL_SEP = ".";

	// note, must be the same in both Philosopher and Fork
	final static String NAME_SEP = ":";

	// Need note be the same everywhere
	static Automaton philo = null;
	static boolean inited = false;

	public Philosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put)
		throws Exception
	{
		if (inited)
		{
			return;
		}

		// Here we create the "template" automaton, philo
		philo = new Automaton("Philo template");

		philo.setType(AutomatonType.Plant);

		// These are fivestate project
		states[0].setInitial(true);
		states[0].setAccepting(true);

		for (int i = 0; i < states.length; ++i)
		{
			philo.addState(states[i]);
		}

		// Now the events, these should be (re)named uniquely for each philosopher
		// (each fork-pair, actually)
		events[L_TAKE].setControllable(l_take);
		events[R_TAKE].setControllable(r_take);
		events[L_PUT].setControllable(l_put);
		events[R_PUT].setControllable(r_put);

		for (int i = 0; i < events.length; ++i)
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

	public Automaton build(int id, int l_fork, int r_fork)
		throws Exception
	{
		Automaton sm = new Automaton(philo);

		// deep copy, I hope
		sm.setName("Philo" + NAME_SEP + id);

		// adjust the event names according to l_fork and r_fork
		// L_take becomes take<id>.<l_fork>
		// R_take becomes take<id>.<r_fork>
		// L_put becomes put<id>.<l_fork>
		// R_put becomes put<id>.<r_fork>
		Alphabet alpha = sm.getAlphabet();

		alpha.getEventWithId("L_take").setLabel("take" + id + LABEL_SEP + l_fork);
		alpha.getEventWithId("R_take").setLabel("take" + id + LABEL_SEP + r_fork);
		alpha.getEventWithId("L_put").setLabel("put" + id + LABEL_SEP + l_fork);
		alpha.getEventWithId("R_put").setLabel("put" + id + LABEL_SEP + r_fork);
		alpha.rehash();

		// must rehash since we've changed the label (that's the way it works)
		return sm;
	}
}


// Builds a Philo automaton
class EatingPhilosopher
{
	static State[] states =
	{
		new State("s0"), new State("lu"),

		// left fork picked up
		new State("ru"),

		// right fork picked up
		new State("eat"), new State("ld"),

		// left fork put down
		new State("rd"),

		// right fork put down
		new State("eat2")
	};
	final static int INIT = 0;
	final static int L_UP = 1;
	final static int R_UP = 2;
	final static int EAT = 3;
	final static int L_DN = 4;
	final static int R_DN = 5;
	final static int EAT2 = 6;
	static LabeledEvent[] events =
	{
		new LabeledEvent("L_take"),

		// pick up left
		new LabeledEvent("R_take"),

		// pick up right
		new LabeledEvent("L_put"),

		// put down left
		new LabeledEvent("R_put"),

		// put down right
		new LabeledEvent("Start_eating"),
	};
	final static int L_TAKE = 0;
	final static int R_TAKE = 1;
	final static int L_PUT = 2;
	final static int R_PUT = 3;
	final static int START_EATING = 4;
	final static String LABEL_SEP = ".";

	// note, must be the same in both Philosopher and Fork
	final static String NAME_SEP = ":";

	// Need note be the same everywhere
	static Automaton philo = null;
	static boolean inited = false;

	public EatingPhilosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put)
		throws Exception
	{
		if (inited)
		{
			return;
		}

		// Here we create the "template" automaton, philo
		philo = new Automaton("Philo template");

		philo.setType(AutomatonType.Plant);

		// These are fivestate project
		states[0].setInitial(true);
		states[0].setAccepting(true);

		for (int i = 0; i < states.length; ++i)
		{
			philo.addState(states[i]);
		}

		// Now the events, these should be (re)named uniquely for each philosopher
		// (each fork-pair, actually)
		events[L_TAKE].setControllable(l_take);
		events[R_TAKE].setControllable(r_take);
		events[L_PUT].setControllable(l_put);
		events[R_PUT].setControllable(r_put);
		events[START_EATING].setControllable(true);

		for (int i = 0; i < events.length; ++i)
		{
			philo.getAlphabet().addEvent(events[i]);
		}

		// And finally the arcs - first the left side (where the left is picked up
		// and put down first)
		philo.addArc(new Arc(states[INIT], states[L_UP], events[L_TAKE].getId()));
		philo.addArc(new Arc(states[L_UP], states[EAT], events[R_TAKE].getId()));
		philo.addArc(new Arc(states[EAT], states[EAT2], events[START_EATING].getId()));
		philo.addArc(new Arc(states[EAT2], states[L_DN], events[L_PUT].getId()));
		philo.addArc(new Arc(states[L_DN], states[INIT], events[R_PUT].getId()));

		// And then the right side (where th eright fork is picked up and put down first)
		philo.addArc(new Arc(states[INIT], states[R_UP], events[R_TAKE].getId()));
		philo.addArc(new Arc(states[R_UP], states[EAT], events[L_TAKE].getId()));
		philo.addArc(new Arc(states[EAT2], states[R_DN], events[R_PUT].getId()));
		philo.addArc(new Arc(states[R_DN], states[INIT], events[L_PUT].getId()));

		inited = true;
	}

	public Automaton build(int id, int l_fork, int r_fork)
		throws Exception
	{
		Automaton sm = new Automaton(philo);

		// deep copy, I hope
		sm.setName("Philo" + NAME_SEP + id);

		// adjust the event names according to l_fork and r_fork
		// L_take becomes take<id>.<l_fork>
		// R_take becomes take<id>.<r_fork>
		// L_put becomes put<id>.<l_fork>
		// R_put becomes put<id>.<r_fork>
		Alphabet alpha = sm.getAlphabet();

		alpha.getEventWithId("L_take").setLabel("take" + id + LABEL_SEP + l_fork);
		alpha.getEventWithId("R_take").setLabel("take" + id + LABEL_SEP + r_fork);
		alpha.getEventWithId("L_put").setLabel("put" + id + LABEL_SEP + l_fork);
		alpha.getEventWithId("R_put").setLabel("put" + id + LABEL_SEP + r_fork);
		alpha.getEventWithId("Start_eating").setLabel("startEating" + id);
		alpha.rehash();

		// must rehash since we've changed the label (that's the way it works)
		return sm;
	}
}

// Builds a chopstick automaton
class Chopstick
{
	static State[] states = { new State("0"), new State("1") };
	static LabeledEvent[] events = { new LabeledEvent("L_up"),
									 new LabeledEvent("R_up"),
									 new LabeledEvent("L_dn"),
									 new LabeledEvent("R_dn") };
	final static int L_TAKE = 0;
	final static int R_TAKE = 1;
	final static int L_PUT = 2;
	final static int R_PUT = 3;
	final static String LABEL_SEP = ".";

	// note, must be the same in both Philosopher and Fork
	final static String NAME_SEP = ":";

	// Need note be the same everywhere
	static Automaton fork = null;
	static boolean inited = false;


	public Chopstick(boolean l_take, boolean r_take, boolean l_put, boolean r_put)
		throws Exception
	{
		if (inited)
		{
			return;
		}

		fork = new Automaton("Fork template");

		fork.setType(AutomatonType.Plant);

		// First the states
		states[0].setInitial(true);
		states[0].setAccepting(true);

		for (int i = 0; i < states.length; ++i)
		{
			fork.addState(states[i]);
		}

		// Now the events
		events[L_TAKE].setControllable(l_take);
		events[R_TAKE].setControllable(r_take);
		events[L_PUT].setControllable(l_put);
		events[R_PUT].setControllable(r_put);

		for (int i = 0; i < events.length; ++i)
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

	Automaton build(int id, int l_philo, int r_philo)
		throws Exception
	{
		Automaton sm = new Automaton(fork);

		// deep copy, I hope
		sm.setName("Fork" + NAME_SEP + id);

		Alphabet alpha = sm.getAlphabet();

		alpha.getEventWithId("L_up").setLabel("take" + l_philo + LABEL_SEP + id);
		alpha.getEventWithId("R_up").setLabel("take" + r_philo + LABEL_SEP + id);
		alpha.getEventWithId("L_dn").setLabel("put" + l_philo + LABEL_SEP + id);
		alpha.getEventWithId("R_dn").setLabel("put" + r_philo + LABEL_SEP + id);
		alpha.rehash();

		// must rehash since we've changed the label (that's the way it works)
		return sm;
	}
}

public class DiningPhilosophers
{
	Project project = new Project();

	// These are helpers for counting modulo num philos/forks
	// Note that we adjust for 0's, indices are from 1 to modulo
	int nextId(int id, int modulo)
	{
		int nxt = id + 1;

		if (nxt > modulo)
		{
			return nxt - modulo;
		}
		else
		{
			return nxt;
		}
	}

	int prevId(int id, int modulo)
	{
		int nxt = id - 1;

		if (nxt <= 0)
		{
			return modulo;
		}
		else
		{
			return nxt;
		}
	}

	public DiningPhilosophers(int num, boolean l_take, boolean r_take, boolean l_put, boolean r_put, boolean animation)
		throws Exception
	{

		// First the philosphers
		// Philosopher philo = new Philosopher(l_take, r_take, l_put, r_put);
		EatingPhilosopher philo = new EatingPhilosopher(l_take, r_take, l_put, r_put);


		for (int i = 0; i < num; ++i)
		{
			int id = i + 1;

			//Automaton currPhil = philo.build(id, id, prevId(id, num));
			Automaton currPhil = philo.build(id, id, nextId(id, num));
			// id's are from 1...n
			project.addAutomaton(currPhil);

			// To his right a philo has fork #id, and to his left is fork #id-1


			if (animation)
			{
				Alphabet alpha = currPhil.getAlphabet();

				LabeledEvent lTake = alpha.getEventWithId("L_take");
				LabeledEvent rTake = alpha.getEventWithId("R_take");
				LabeledEvent lPut = alpha.getEventWithId("L_put");
				LabeledEvent rPut = alpha.getEventWithId("R_put");
				LabeledEvent startEating = alpha.getEventWithId("Start_eating");

				Actions currActions = project.getActions();
				Controls currControls = project.getControls();
				// The forks in the animation are numbered 0 to nbr of forks - 1

				Action lTakeAction = new Action(lTake.getLabel());
				currActions.addAction(lTakeAction);
				lTakeAction.addCommand("fork." + id  + ".get");
				lTakeAction.addCommand("phil." + id  + ".leftfork");

				Action rTakeAction = new Action(rTake.getLabel());
				currActions.addAction(rTakeAction);
				rTakeAction.addCommand("fork." + nextId(id, num) + ".get");
				rTakeAction.addCommand("phil." + id + ".rightfork");

				Action lPutAction = new Action(lPut.getLabel());
				currActions.addAction(lPutAction);
				lPutAction.addCommand("fork." + id  + ".put");
				lPutAction.addCommand("phil." + id  + ".thinking.begin");

				Control lPutControl = new Control(lPut.getLabel());
				currControls.addControl(lPutControl);
				lPutControl.addCondition("phil." + id + ".eating.end");

				Action rPutAction = new Action(rPut.getLabel());
				currActions.addAction(rPutAction);
				rPutAction.addCommand("fork." + nextId(id, num) + ".put");
				rPutAction.addCommand("phil." + id + ".thinking.begin");

				Control rPutControl = new Control(rPut.getLabel());
				currControls.addControl(rPutControl);
				rPutControl.addCondition("phil." + id + ".eating.end");

				Action startEatingAction = new Action(startEating.getLabel());
				currActions.addAction(startEatingAction);
				startEatingAction.addCommand("phil." + id + ".eating.begin");

			}

		}

		// Next the forks aka chopsticks
		Chopstick fork = new Chopstick(l_take, r_take, l_put, r_put);

		for (int i = 0; i < num; ++i)
		{
			int id = i + 1;

			// id's are from 1...n
			project.addAutomaton(fork.build(id, prevId(id, num), id));

			// To its right a fork has philo #id, and to its left philo #id-1
		}

		if (animation)
		{
			project.setAnimationPath("/scenebeans/mageekramer/xml/diners.xml");
		}
	}

	public Project getProject()
	{
		return project;
	}
}
