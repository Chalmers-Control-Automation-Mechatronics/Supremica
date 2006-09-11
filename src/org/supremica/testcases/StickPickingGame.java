
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
        return Integer.toString(id+1);
    }
}

class Sticks
    extends Automaton
{
    State[] sticks;    // we have to cache the states ourselves
    
    public Sticks(int num, int players)
    throws Exception
    {
        super("Sticks:" + num);
        
        setType(AutomatonType.PLANT);
        
        sticks = new State[num + 1];    // one more state than sticks
        sticks[0] = new State(Integer.toString(num));    // starting at "num" and counting down
        
        sticks[0].setInitial(true);    // first state is initial
        addState(sticks[0]);
        
        for (int i = 1; i < num + 1; ++i)
        {
            sticks[i] = new State(Integer.toString(num - i));
            
            addState(sticks[i]);
        }
        
        sticks[num].setAccepting(true);    // last state is accepting
        
        for (int player = 0; player < players; ++player)
        {
            // Note, we assume _at_least_ 5 sticks here!
            State s0 = sticks[0];
            State s1 = sticks[0 + 1];
            State s2 = sticks[0 + 2];
            State s3 = sticks[0 + 3];
            PlayerEvents pe = new PlayerEvents(player);
            
            getAlphabet().addEvent(pe.a1);
            getAlphabet().addEvent(pe.a2);
            getAlphabet().addEvent(pe.a3);
            
//                      addArc(new Arc(s0, s1, pe.a1.getId()));
//                      addArc(new Arc(s0, s2, pe.a2.getId()));
//                      addArc(new Arc(s0, s3, pe.a3.getId()));
            addArc(new Arc(s0, s1, pe.a1));
            addArc(new Arc(s0, s2, pe.a2));
            addArc(new Arc(s0, s3, pe.a3));
            
            for (int i = 1; i < num - 2; ++i)
            {
                s0 = s1;
                s1 = s2;
                s2 = s3;
                s3 = sticks[i + 3];
                
//                              addArc(new Arc(s0, s1, pe.a1.getId()));
//                              addArc(new Arc(s0, s2, pe.a2.getId()));
//                              addArc(new Arc(s0, s3, pe.a3.getId()));
                addArc(new Arc(s0, s1, pe.a1));
                addArc(new Arc(s0, s2, pe.a2));
                addArc(new Arc(s0, s3, pe.a3));
            }
            
            s0 = s1;
            s1 = s2;
            s2 = s3;    // sticks[num-1];
            
//                      addArc(new Arc(s0, s1, pe.a1.getId()));
//                      addArc(new Arc(s0, s2, pe.a2.getId()));
            addArc(new Arc(s0, s1, pe.a1));
            addArc(new Arc(s0, s2, pe.a2));
            
            s0 = s1;
            s1 = s2;
            
//                      addArc(new Arc(s0, s1, pe.a1.getId()));
            addArc(new Arc(s0, s1, pe.a1));
        }
        
        sticks = null;    // done with the cache
    }
}

class Players
    extends Automaton
{
    private State[] players;    // have to cache the states ourselves
    
    public Players(int num)
    throws Exception
    {
        super("Players:" + num);
        
        setType(AutomatonType.PLANT);
        
        // One state for each player
        // First player is special (initial and accepting)
        // All other states except for the one immediately after the first player
        // has made a move should be marked (player 1 must not take the last stick)!
        players = new State[num];
        players[0] = new State(PlayerEvents.getPlayerId(0));
        players[0].setInitial(true);
        players[0].setAccepting(true);
        addState(players[0]);
        
        Alphabet alpha = getAlphabet();
        
        for (int i = 0; i < num - 1; ++i)    // for each player except the last one, add arcs to the next guy
        {
            PlayerEvents pe = new PlayerEvents(i);
            
            alpha.addEvent(pe.a1);
            alpha.addEvent(pe.a2);
            alpha.addEvent(pe.a3);
            
            // From this guy...
            State from = players[i];
            // ...to this one
            players[i + 1] = new State(PlayerEvents.getPlayerId(i + 1));
            State to = players[i + 1];
            
            // All but one state should be accepting!
            if (i != 0)
            {
                to.setAccepting(true);
            }
            
            addState(to);
            
//                      addArc(new Arc(from, to, pe.a1.getId()));
//                      addArc(new Arc(from, to, pe.a2.getId()));
//                      addArc(new Arc(from, to, pe.a3.getId()));
            addArc(new Arc(from, to, pe.a1));
            addArc(new Arc(from, to, pe.a2));
            addArc(new Arc(from, to, pe.a3));
        }
        
        // finally, add the last to first player arcs
        PlayerEvents pe = new PlayerEvents(num - 1);
        
        alpha.addEvent(pe.a1);
        alpha.addEvent(pe.a2);
        alpha.addEvent(pe.a3);
        
        State from = players[num - 1];    // from this guy...
        State to = players[0];    // ...to this one
        
//              addArc(new Arc(from, to, pe.a1.getId()));
//              addArc(new Arc(from, to, pe.a2.getId()));
//              addArc(new Arc(from, to, pe.a3.getId()));
        addArc(new Arc(from, to, pe.a1));
        addArc(new Arc(from, to, pe.a2));
        addArc(new Arc(from, to, pe.a3));
        
        players = null;    // done with the cache
    }
}

public class StickPickingGame
{
    Project project = new Project();
    
    public StickPickingGame(int players, int sticks)
    throws Exception
    {
        project.setComment("A number of players alternatingly take one, two or three sticks. The player who takes the last stick loses. If you take the first turn, can you guarantee that you will not lose? In this model, not losing is equivalent to reaching a marked state, and only your own moves are controllable. Try to synthesize a controllable and nonblocking supervisor!");
        
        try
        {
            project.addAutomaton(new Players(players));
            project.addAutomaton(new Sticks(sticks, players));
        }
        catch (Exception excp)
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
