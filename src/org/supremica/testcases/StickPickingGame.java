/***************************** StickPickingGame.java *******************/
package org.supremica.testcases;

import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;

class PlayerEvents
{
	public LabeledEvent a1;
	public LabeledEvent a2;
	public LabeledEvent a3;

	public PlayerEvents(int id)
	{
		this.a1 = new LabeledEvent(getPlayerId(id) + ":1");
		this.a2 = new LabeledEvent(getPlayerId(id) + ":2");
		this.a3 = new LabeledEvent(getPlayerId(id) + ":3");

		this.a1.setControllable(id == 0);
		this.a2.setControllable(id == 0);
		this.a3.setControllable(id == 0);
	}

	public static String getPlayerId(int id)
	{
		return Integer.toString(id);
	}
}

class Sticks
	extends Automaton
{
	State[] sticks; // we have to cache the states ourselves

	public Sticks(int num, int players)
		throws Exception
	{
		super("Sticks:" + num);
		setType(AutomatonType.Plant);

		sticks = new State[num+1];	// one more state than sticks

		sticks[0] = new State(Integer.toString(num)); // starting at "num" and counting down
		sticks[0].setInitial(true); // first state is initial
		addState(sticks[0]);

		for(int i = 1; i < num+1; ++i)
		{
			sticks[i] = new State(Integer.toString(num-i));
			addState(sticks[i]);
		}
		sticks[num].setAccepting(true);	// last state is accepting

		for(int player = 0; player < players; ++player)
		{
			// Note, we assume _at_least_ 5 sticks here!
			State s0 = sticks[0];
			State s1 = sticks[0+1];
			State s2 = sticks[0+2];
			State s3 = sticks[0+3];

			PlayerEvents pe = new PlayerEvents(player);
			getAlphabet().addEvent(pe.a1);
			getAlphabet().addEvent(pe.a2);
			getAlphabet().addEvent(pe.a3);

			addArc(new Arc(s0, s1, pe.a1.getId()));
			addArc(new Arc(s0, s2, pe.a2.getId()));
			addArc(new Arc(s0, s3, pe.a3.getId()));

			for(int i = 1; i < num-2; ++i)
			{
				s0 = s1;
				s1 = s2;
				s2 = s3;
				s3 = sticks[i+3];
				addArc(new Arc(s0, s1, pe.a1.getId()));
				addArc(new Arc(s0, s2, pe.a2.getId()));
				addArc(new Arc(s0, s3, pe.a3.getId()));
			}

			s0 = s1;
			s1 = s2;
			s2 = s3; // sticks[num-1];
			addArc(new Arc(s0, s1, pe.a1.getId()));
			addArc(new Arc(s0, s2, pe.a2.getId()));

			s0 = s1;
			s1 = s2;
			addArc(new Arc(s0, s1, pe.a1.getId()));
		}


		sticks = null; // done with the cache
	}
}

class Players
	extends Automaton
{
	private State[] players; // have to cache the states ourselves

	public Players(int num)
		throws Exception
	{
		super("Players:" + num);
		setType(AutomatonType.Plant);

		players = new State[num]; // one state for each player

		players[0] = new State(PlayerEvents.getPlayerId(0));	// first player is special (initial and accepting)
		players[0].setInitial(true);
		players[0].setAccepting(true);
		addState(players[0]);

		Alphabet alpha = getAlphabet();
		for(int i = 0; i < num-1; ++i) // for each player excpet the last one, add arcs to the next guy
		{
			PlayerEvents pe = new PlayerEvents(i);
			alpha.addEvent(pe.a1);
			alpha.addEvent(pe.a2);
			alpha.addEvent(pe.a3);

			State from = players[i];	// from this guy...
			players[i+1] = new State(PlayerEvents.getPlayerId(i+1));
			State to = players[i+1];	// ...to this one
			addState(to);

			addArc(new Arc(from, to, pe.a1.getId()));
			addArc(new Arc(from, to, pe.a2.getId()));
			addArc(new Arc(from, to, pe.a3.getId()));

		}

		// finally, add the last to first player arcs
		PlayerEvents pe = new PlayerEvents(num-1);
		alpha.addEvent(pe.a1);
		alpha.addEvent(pe.a2);
		alpha.addEvent(pe.a3);

		State from = players[num-1];	// from this guy...
		State to = players[0];	// ...to this one

		addArc(new Arc(from, to, pe.a1.getId()));
		addArc(new Arc(from, to, pe.a2.getId()));
		addArc(new Arc(from, to, pe.a3.getId()));

		players = null; // done with the cache
	}

}

public class StickPickingGame
{
	Project project = new Project();

	public StickPickingGame(int players, int sticks)
		throws Exception
	{
		try
		{
			project.addAutomaton(new Players(players));
			project.addAutomaton(new Sticks(sticks, players));
		}
		catch(Exception excp)
		{
			excp.printStackTrace();
			throw excp;
		}
	}

	public Project getProject()
	{
		return project;
	}

}