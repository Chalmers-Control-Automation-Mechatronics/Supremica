/***************************** StickPickingGame.java *******************/
package org.supremica.testcases;

import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
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

class TestingXyz
	extends Automaton
{
	public TestingXyz()		// This is just test ---
		throws Exception
	{
		State stateZero = new State("zero");
		addState(stateZero);
		State from = getStateWithIndex(0); // should be stateZero (not?)
		if(from == null)
			System.err.println("from == null");
		else
			System.err.println("from != null");		
		
		LabeledEvent ev = new LabeledEvent("event");
		getAlphabet().addEvent(ev);
		
		System.err.println("Adding arc...");
		addArc(new Arc(null, null, ev.getId()));
		throw new Exception("Arc added?");
	}
}

class Sticks
	extends Automaton
{
	public Sticks(int num, int players)
	{
		super("Sticks:" + num);
		setType(AutomatonType.Plant);
	System.err.println("Sticks::construct");
		
		State firstState = new State(Integer.toString(num)); // first state is special
		firstState.setInitial(true);
		firstState.setAccepting(true);
		addState(firstState);
		
		for(int i = num-1; i <= 0; --i) // states are numbered from num to 0
		{
			addState(new State(Integer.toString(i)));
		}
		
		// Note, we assume _at_least_ 5 sticks here!
		State s0 = getStateWithIndex(0);
		State s1 = getStateWithIndex(0+1);
		State s2 = getStateWithIndex(0+2);
		State s3 = getStateWithIndex(0+3);
		
		int player = 0;
		PlayerEvents pe = new PlayerEvents(player);
		
		for( ; player < players; ++player) 
		{
			addArc(new Arc(s0, s1, pe.a1.getId()));
			addArc(new Arc(s0, s2, pe.a2.getId()));
			addArc(new Arc(s0, s3, pe.a3.getId()));

			for(int i = 1; i < num-2; ++i)
			{
				s0 = s1;
				s1 = s2;
				s2 = s3;
				s3 = getStateWithIndex(i+3);
				addArc(new Arc(s0, s1, pe.a1.getId()));
				addArc(new Arc(s0, s2, pe.a2.getId()));
				addArc(new Arc(s0, s3, pe.a3.getId()));
			}
			
			pe = new PlayerEvents(player);
		}

		s0 = s1;
		s1 = s2;
		s2 = getStateWithIndex(num-2);
		addArc(new Arc(s0, s1, pe.a1.getId()));
		addArc(new Arc(s0, s2, pe.a2.getId()));
		
		s0 = s1;
		s1 = getStateWithIndex(num-1);
		addArc(new Arc(s0, s1, pe.a1.getId()));
		
	}
}

class Players
	extends Automaton
{
	public Players(int num)
		throws Exception
	{
		super("Players:" + num);
		setType(AutomatonType.Plant);
	System.err.println("Players::Players(" + num + ")");
		State firstPlayer = new State("1");	// first player is special (initial and accepting)
		firstPlayer.setInitial(true);
		firstPlayer.setAccepting(true);
		addState(firstPlayer);	// this is state[0]

/** Something's fishy going on here, the second println statement is not printing!		
		PlayerEvents pe = new PlayerEvents(0);	// first players events are special (controllable)
		
		Alphabet alpha = getAlphabet();
		alpha.addEvent(pe.a1);
		alpha.addEvent(pe.a2);
		alpha.addEvent(pe.a3);
		
		for(int i = 1; i < num; ++i)	// the rest of the players are boring
		{
			State to = new State(PlayerEvents.getPlayerId(i));
			addState(to);
			State from = getStateWithIndex(i-1);
	System.err.println("from-state gotten");
			addArc(new Arc(from, to, pe.a1.getId()));
			addArc(new Arc(from, to, pe.a2.getId()));
			addArc(new Arc(from, to, pe.a3.getId()));
	System.err.println("Players::player events added for player: " + PlayerEvents.getPlayerId(i));
			
			pe = new PlayerEvents(i);
		}
**/

		Alphabet alpha = getAlphabet();
		for(int i = 0; i < num-1; ++i)
		{
			PlayerEvents pe = new PlayerEvents(i);
			alpha.addEvent(pe.a1);
			alpha.addEvent(pe.a2);
			alpha.addEvent(pe.a3);
		
			State to = new State(PlayerEvents.getPlayerId(i+1));
			addState(to);
			State from = getStateWithIndex(i);
	System.err.println("from-state gotten");
			
			addArc(new Arc(from, to, pe.a1.getId()));
			addArc(new Arc(from, to, pe.a2.getId()));
			addArc(new Arc(from, to, pe.a3.getId()));
	System.err.println("Players::player events added for player: " + PlayerEvents.getPlayerId(i));
			
		}
		
		// finally, add the last to first player arcs	
		PlayerEvents pe = new PlayerEvents(num-1);
		alpha.addEvent(pe.a1);
		alpha.addEvent(pe.a2);
		alpha.addEvent(pe.a3);
		
		State to = getStateWithIndex(0);
		State from = getStateWithIndex(num-1);
	System.err.println("from-state gotten");
		addArc(new Arc(from, to, pe.a1.getId()));
		addArc(new Arc(from, to, pe.a2.getId()));
		addArc(new Arc(from, to, pe.a3.getId()));
	System.err.println("Players::player events added for player: " + PlayerEvents.getPlayerId(num-1));

	}

}	

public class StickPickingGame
{
	Automata automata = new Automata();
	
	public StickPickingGame(int players, int sticks)
		throws Exception
	{
		automata.addAutomaton(new TestingXyz());
		// System.err.println("StickPickingGame::construct");
		// automata.addAutomaton(new Players(players));
		// automata.addAutomaton(new Sticks(sticks, players)); 
	}
	
	public Automata getAutomata()
	{
		System.err.println("StickPickingGame::getAutomata()");
		return automata;
	}

}