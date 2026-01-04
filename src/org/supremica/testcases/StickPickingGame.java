
/***************************** StickPickingGame.java *******************/
package org.supremica.testcases;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;

class PlayerEvents
{
    public LabeledEvent a1;
    public LabeledEvent a2;
    public LabeledEvent a3;

    public PlayerEvents(final int id)
    {
        this.a1 = new LabeledEvent('e' + getPlayerId(id) + ":1");
        this.a2 = new LabeledEvent('e' + getPlayerId(id) + ":2");
        this.a3 = new LabeledEvent('e' + getPlayerId(id) + ":3");

        this.a1.setControllable(id == 0);
        this.a2.setControllable(id == 0);
        this.a3.setControllable(id == 0);
    }

    public static String getPlayerId(final int id)
    {
        return Integer.toString(id+1);
    }
}

class Sticks
    extends Automaton
{
    State[] sticks;    // we have to cache the states ourselves

    public Sticks(final int numsticks, final int numplayers, final boolean countdown)
    throws Exception
    {
        super("Sticks:" + numsticks);

        setType(AutomatonType.PLANT);

        sticks = new State[numsticks + 1];    // one more state than sticks
		for(int i = 0; i < numsticks + 1; i++)
        {
            sticks[i] = new State("S" + Integer.toString(numsticks - i));
            addState(sticks[i]);
        }

        if(countdown)
        {
			// There is no (easy) way to invert arcs, so instead... more code
			doStandardCountDown(sticks, numsticks, numplayers);
		}
		else
		{
			doNonstandardCountUp(sticks, numsticks, numplayers);
		}

		sticks = null;    // done with the cache
	}

	private void doNonstandardCountUp(final State[] sticks, final int numsticks, final int numplayers)
	{
		// Just copy all of the standard arcs, and invert them
		sticks[numsticks].setInitial(true);    // last state is initial
		sticks[0].setAccepting(true);    // first state is accepting

        for (int player = 0; player < numplayers; ++player)
        {
            State s0 = sticks[0];
            State s1 = sticks[0 + 1];
            State s2 = sticks[0 + 2];
            State s3 = sticks[0 + 3];

            final PlayerEvents pe = new PlayerEvents(player);
            getAlphabet().addEvent(pe.a1);
            getAlphabet().addEvent(pe.a2);
            getAlphabet().addEvent(pe.a3);

            addArc(new Arc(s1, s0, pe.a1));
            addArc(new Arc(s2, s0, pe.a2));
            addArc(new Arc(s3, s0, pe.a3));

            for (int i = 1; i < numsticks - 2; ++i)
            {
                s0 = s1;
                s1 = s2;
                s2 = s3;
                s3 = sticks[i + 3];

                addArc(new Arc(s1, s0, pe.a1));
                addArc(new Arc(s2, s0, pe.a2));
                addArc(new Arc(s3, s0, pe.a3));
            }

            s0 = s1;
            s1 = s2;
            s2 = s3;    // sticks[num-1];


            addArc(new Arc(s1, s0, pe.a1));
            addArc(new Arc(s2, s0, pe.a2));

            s0 = s1;
            s1 = s2;

            addArc(new Arc(s1, s0, pe.a1));
        }
	}

	private void doStandardCountDown(final State[] sticks, final int numsticks, final int numplayers)
	{
        sticks[0].setInitial(true);    // first state is initial
        sticks[numsticks].setAccepting(true);    // last state is accepting

        for (int player = 0; player < numplayers; ++player)
        {
            // Note, we assume _at_least_ 5 sticks here!
            State s0 = sticks[0];
            State s1 = sticks[0 + 1];
            State s2 = sticks[0 + 2];
            State s3 = sticks[0 + 3];

            final PlayerEvents pe = new PlayerEvents(player);
            getAlphabet().addEvent(pe.a1);
            getAlphabet().addEvent(pe.a2);
            getAlphabet().addEvent(pe.a3);

            addArc(new Arc(s0, s1, pe.a1));
            addArc(new Arc(s0, s2, pe.a2));
            addArc(new Arc(s0, s3, pe.a3));

            for (int i = 1; i < numsticks - 2; ++i)
            {
                s0 = s1;
                s1 = s2;
                s2 = s3;
                s3 = sticks[i + 3];

                addArc(new Arc(s0, s1, pe.a1));
                addArc(new Arc(s0, s2, pe.a2));
                addArc(new Arc(s0, s3, pe.a3));
            }

            s0 = s1;
            s1 = s2;
            s2 = s3;    // sticks[num-1];


            addArc(new Arc(s0, s1, pe.a1));
            addArc(new Arc(s0, s2, pe.a2));

            s0 = s1;
            s1 = s2;

            addArc(new Arc(s0, s1, pe.a1));
        }
    }
}

class Players
    extends Automaton
{
    private State[] players;    // have to cache the states ourselves

    public Players(final int num, final boolean countdown)
    throws Exception
    {
        super("Players:" + num);

        setType(AutomatonType.PLANT);

        // One state for each player
        // First player is special (initial and accepting)
        players = new State[num];
        players[0] = new State("P" + PlayerEvents.getPlayerId(0));
        players[0].setInitial(true);
        addState(players[0]);

        final Alphabet alpha = getAlphabet();

        for (int i = 0; i < num - 1; ++i)    // for each player except the last one, add arcs to the next guy
        {
            final PlayerEvents pe = new PlayerEvents(i);

            alpha.addEvent(pe.a1);
            alpha.addEvent(pe.a2);
            alpha.addEvent(pe.a3);

            // From this guy...
            final State from = players[i];
            // ...to this one
            players[i + 1] = new State("P" + PlayerEvents.getPlayerId(i + 1));
            final State to = players[i + 1];
            addState(to);

            addArc(new Arc(from, to, pe.a1));
            addArc(new Arc(from, to, pe.a2));
            addArc(new Arc(from, to, pe.a3));
        }

        // finally, add the last to first player arcs
        final PlayerEvents pe = new PlayerEvents(num - 1);

        alpha.addEvent(pe.a1);
        alpha.addEvent(pe.a2);
        alpha.addEvent(pe.a3);

        final State from = players[num - 1];    // from this guy...
        final State to = players[0];    // ...to this one

        addArc(new Arc(from, to, pe.a1));
        addArc(new Arc(from, to, pe.a2));
        addArc(new Arc(from, to, pe.a3));


		// Default is Player 1 to win, Player 1's events are controllable
		if(countdown)
		{	// If we're counting down, the loser is the one who takes the last stick
			// Mark all states except the state reached from the Player~1 state
			for(final State player : players)
				player.setAccepting(true);

			players[1].setAccepting(false);
		}
		else
		{	// If we're counting up, the winner is the one who takes the last stick
			// Mark the state reached from the Player~1 state
			players[1].setAccepting(true);
		}

        players = null;    // done with the cache
    }
}

public class StickPickingGame
{
    Project project;
	final static String upwin = "The player who takes the last stick wins the game. ";
	final static String dnlose = "The player who takes the last stick loses the game. ";

    public StickPickingGame(final int players, final int sticks, final boolean countdown)
    throws Exception
    {
        if(players < 1)
          throw new java.lang.IllegalArgumentException("Requires at least one player");
        if(sticks < 3)
          throw new java.lang.IllegalArgumentException("Requires at least three sticks");

		final String count = countdown ? "dn" : "up";
        this.project = new Project("Stick Picking (" + players + ", " + sticks + ", " + count + ")");
        project.setComment(
		"A number of players take turns to pick 1, 2, or 3 sticks." +
		(countdown ? dnlose : upwin) +
		"This is an example of a user-interactive DES, that for " +
		"certain configurations (number of players, number of sticks, " +
		"counting up or down) exhibit bias towards or against player(s)." +
		"The existence of a supervisor reveals such bias." +
		"By setting a certain player's events controllable, and marking the " +
		"state where that player wins, positive bias towards that player can " +
		"be revealed. By setting a certain player's events uncontrollable " +
		"(and the other player's events uncontrollable), and marking the states " +
		"where that player loses the game, negative bias against that player " +
		"can be revealed."
		);

        try
        {
			final Automaton plyrs = new Players(players, countdown);
			final Automaton stcks = new Sticks(sticks, players, countdown);
			project.addAutomaton(plyrs);
            project.addAutomaton(stcks);
        }
        catch (final Exception excp)
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
