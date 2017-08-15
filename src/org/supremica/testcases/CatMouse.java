/*
 * CatMouse.java
 * Created on February 29, 2008, 1:07 PM
 *
 * Cat & Mouse exmaple of Wonham, except we allow several cats and mice
 * As of August 2017, there is also teh option to have the forbidden states
 * expressed by uc self-loops (see also org.supremica.automata.algorithms.Forbidder.java)
 *
 * Note: This code depends on stuff from ExtCatMouse.java, which in turn relies 
 * on this code. THat is why some class components are protected instead of private.
 * Such cross-dependency is a big mess and should of course not exist! // MF
 */

package org.supremica.testcases;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.Forbidder;
import org.supremica.automata.ForbiddenEvent;

/**
 *
 * @author Sajed
 */

// Builds a Cat automaton
class Cat
{
    private final String CAT_NAME = "Cat";
    private final String CAT_ID = "c";

    private final static int NUMBER_OF_STATES = 5;
    private static State[][] states;

    final static int NUMBER_OF_EVENTS = 7;
    static LabeledEvent[] events;


    final static String LABEL_SEP = "_";

    // note, must be the same in both Cat and Mouse
    final static String NAME_SEP = ":";

    // Need not be the same everywhere
    static Automaton cat = null;

    static boolean inited = false;

    public Cat()
    throws Exception
    {
        events = new LabeledEvent[NUMBER_OF_EVENTS];
        states = new State[1][NUMBER_OF_STATES];
        for(int i = 0; i < NUMBER_OF_STATES; i++)
            states[0][i] = new State(CAT_ID+i);

        for(int i = 0; i < NUMBER_OF_EVENTS; i++)
            events[i] = new LabeledEvent(CAT_ID+i);

        //indices into events
//        for(i=0; i<number_of_events;i++)
//            event_indices[i] = i;


/*        if (inited)
        {
            // The only thing that may need to be changed is the controllability
            Alphabet alpha = philo.getAlphabet();
            alpha.getEvent(events[L_TAKE].getLabel()).setControllable(l_take);
            alpha.getEvent(events[R_TAKE].getLabel()).setControllable(r_take);
            alpha.getEvent(events[L_PUT].getLabel()).setControllable(l_put);
            alpha.getEvent(events[R_PUT].getLabel()).setControllable(r_put);
            alpha.getEvent(events[INTERM_EVENT].getLabel()).setControllable(true);
            alpha.getEvent(events[START_EATING].getLabel()).setControllable(true);

            return;
        }
*/
        // Here we create the "template" automaton, cat
        cat = new Automaton(CAT_NAME);
        cat.setType(AutomatonType.PLANT);

        // These are fivestate project
        states[0][2].setInitial(true);
        states[0][2].setAccepting(true);
        for (int i = 0; i < states[0].length; ++i)
        {
            cat.addState(states[0][i]);
        }

        for(int i = 0; i < (events.length-1);i++)
            events[i].setControllable(true);
        events[(events.length-1)].setControllable(false);

        for (int i = 0; i < events.length; ++i)
        {
            cat.getAlphabet().addEvent(events[i]);
        }

        cat.addArc(new Arc(states[0][2], states[0][0], events[2]));
        cat.addArc(new Arc(states[0][0], states[0][1], events[0]));
        cat.addArc(new Arc(states[0][1], states[0][2], events[1]));
        cat.addArc(new Arc(states[0][0], states[0][3], events[3]));
        cat.addArc(new Arc(states[0][3], states[0][4], events[4]));
        cat.addArc(new Arc(states[0][4], states[0][0], events[5]));
        cat.addArc(new Arc(states[0][1], states[0][3], events[6]));
        cat.addArc(new Arc(states[0][3], states[0][1], events[6]));

//        inited = true;
    }

    public Cat(final int id, final int num_levels)
    throws Exception
    {
         events = new LabeledEvent[NUMBER_OF_EVENTS];
        states = new State[1][NUMBER_OF_STATES];

        cat = new Automaton(CAT_NAME+NAME_SEP+id);
        cat.setType(AutomatonType.PLANT);

        final String LABEL_LEVEL = new Level().LEVEL_NAME;

        states = new State[num_levels][NUMBER_OF_STATES];

        final int INIT_LEVEL = 0;
        final int INIT_ROOM = 2;

        for(int i = 0; i < NUMBER_OF_EVENTS; i++)
        {
            events[i] = new LabeledEvent(CAT_ID + id + LABEL_SEP + i);
            if(i!=(NUMBER_OF_EVENTS-1))
                events[i].setControllable(true);
            else
                events[i].setControllable(false);

            cat.getAlphabet().addEvent(events[i]);
        }

        for(int in_level = 0; in_level < num_levels; in_level++)
        {
            for(int j=0;j<NUMBER_OF_STATES;j++)
            {
                states[in_level][j] = new State(LABEL_LEVEL+in_level+"_"+CAT_ID+j);

                if(in_level == INIT_LEVEL && j == INIT_ROOM)
                {
                    states[in_level][j].setInitial(true);
                    states[in_level][j].setAccepting(true);
                }

                cat.addState(states[in_level][j]);
            }

            cat.addArc(new Arc(states[in_level][2], states[in_level][0], events[2]));
            cat.addArc(new Arc(states[in_level][0], states[in_level][1], events[0]));
            cat.addArc(new Arc(states[in_level][1], states[in_level][2], events[1]));
            cat.addArc(new Arc(states[in_level][0], states[in_level][3], events[3]));
            cat.addArc(new Arc(states[in_level][3], states[in_level][4], events[4]));
            cat.addArc(new Arc(states[in_level][4], states[in_level][0], events[5]));
            cat.addArc(new Arc(states[in_level][1], states[in_level][3], events[6]));
            cat.addArc(new Arc(states[in_level][3], states[in_level][1], events[6]));

        }

        for(int in_level = 0; in_level < num_levels; in_level++)
        {
            final int ROOM_ID = in_level%(CatMouse.NUMBER_OF_ROOMS);

            if((in_level+1) < num_levels)
            {
                final LabeledEvent lec = new LabeledEvent("c"+id+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));

                lec.setControllable(true);

                cat.getAlphabet().addEvent(lec);

                State thisState, state_1=null, state_2=null;
                int cnt = 0;
                int level_index;
                int room_index;

                final TreeSet<State> treeset =
                  new TreeSet<State>(cat.getStateSet());
                final Iterator<State> iter_state = treeset.descendingIterator();
                while(iter_state.hasNext())
                {
                    thisState = (State)iter_state.next();
                    final String state_name = thisState.getName();
                    level_index = Integer.parseInt(state_name.substring(1,state_name.indexOf("_")));
                    room_index = Integer.parseInt(state_name.substring(state_name.indexOf(CAT_ID)+1));

                    if(in_level == level_index && ROOM_ID == room_index){
                        state_1 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }

                    if((in_level+1) == level_index && ROOM_ID == room_index){
                        state_2 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }
                }

                cat.addArc(new Arc(state_1,state_2,lec));
                cat.addArc(new Arc(state_2,state_1,lec));

            }
        }


    }
    public Automaton getCat()
    {
        return cat;
    }

    public void renameEvent(final Automaton sm, final int ev_index, final String new_label)
    {
        final Alphabet alpha = sm.getAlphabet();
        final LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        final LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);

    }

    public void renameState(final Automaton sm, final int st_index, final String new_label)
    {
        sm.getStateSet();
        final State st_old = sm.getStateWithIndex(st_index);
        final State st_new = new State(st_old,new_label);
        sm.replaceState(st_old, st_new);
    }

    public Automaton build(final int id)
    throws Exception
    {
        final Automaton sm = new Automaton(cat);
        sm.setName(CAT_NAME + NAME_SEP + id);
        StringTokenizer st;
        for(int i=0;i<events.length;i++)
        {
            st = new StringTokenizer(events[i].getLabel(), CAT_ID);
            renameEvent(sm, i, CAT_ID + id + LABEL_SEP + st.nextToken());
        }

        return sm;
    }


    public Automaton getAutomaton()
    {
        return cat;
    }

}

// Builds a Cat automaton
class Mouse
{
    public final String MOUSE_NAME = "Mouse";
    public final String MOUSE_ID = "m";

    final static int number_of_states = 5;
    static State[][] states;

    final static int number_of_events = 6;
    static LabeledEvent[] events;
    static int[] event_indices;

    final static String LABEL_SEP = "_";

    // note, must be the same in both Cat and Mouse
    final static String NAME_SEP = ":";

    // Doesn't need not be the same everywhere
    static Automaton mouse = null;

    static boolean inited = false;

    int length;
    // int i;

    public Mouse()
    throws Exception
    {
        states = new State[1][number_of_states];
        events = new LabeledEvent[number_of_events];
        event_indices = new int[number_of_events];
        for(int i = 0; i < number_of_states; i++)
            states[0][i] = new State(MOUSE_ID+i);

        for(int i = 0; i < number_of_events; i++)
            events[i] = new LabeledEvent(MOUSE_ID+i);

        //indices into events
        for(int i = 0; i < number_of_events; i++)
            event_indices[i] = i;

/*        if (inited)
        {
            // The only thing that may need to be changed is the controllability
            Alphabet alpha = philo.getAlphabet();
            alpha.getEvent(events[L_TAKE].getLabel()).setControllable(l_take);
            alpha.getEvent(events[R_TAKE].getLabel()).setControllable(r_take);
            alpha.getEvent(events[L_PUT].getLabel()).setControllable(l_put);
            alpha.getEvent(events[R_PUT].getLabel()).setControllable(r_put);
            alpha.getEvent(events[INTERM_EVENT].getLabel()).setControllable(true);
            alpha.getEvent(events[START_EATING].getLabel()).setControllable(true);

            return;
        }
*/
        // Here we create the "template" automaton, mouse
        mouse = new Automaton("Mouse");
        mouse.setType(AutomatonType.PLANT);

        // These are fivestate project
        states[0][4].setInitial(true);
        states[0][4].setAccepting(true);
        for (int i = 0; i < states[0].length; ++i)
        {
            mouse.addState(states[0][i]);
        }

        for(int i = 0; i < number_of_events; i++)
            events[i].setControllable(true);

        for (int i = 0; i < events.length; ++i)
        {
            mouse.getAlphabet().addEvent(events[i]);
        }

        mouse.addArc(new Arc(states[0][4], states[0][3], events[4]));
        mouse.addArc(new Arc(states[0][3], states[0][0], events[5]));
        mouse.addArc(new Arc(states[0][0], states[0][2], events[0]));
        mouse.addArc(new Arc(states[0][2], states[0][1], events[1]));
        mouse.addArc(new Arc(states[0][1], states[0][0], events[2]));
        mouse.addArc(new Arc(states[0][0], states[0][4], events[3]));

        inited = true;
    }

    public Mouse(final int id, final int num_levels)
    throws Exception
    {

        events = new LabeledEvent[number_of_events];
        event_indices = new int[number_of_events];

        mouse = new Automaton(MOUSE_NAME+NAME_SEP+id);
        mouse.setType(AutomatonType.PLANT);

        final String LABEL_LEVEL = new Level().LEVEL_NAME;

        states = new State[num_levels][number_of_states];

        final int INIT_LEVEL = num_levels-1;
        final int INIT_ROOM = 4;

        for(int i =0 ; i < number_of_events; i++)
        {
            events[i] = new LabeledEvent(MOUSE_ID + id + LABEL_SEP + i);
            if(i!=(number_of_events-1))
                events[i].setControllable(true);

            mouse.getAlphabet().addEvent(events[i]);
        }

        for(int in_level=0; in_level<num_levels;in_level++)
        {
            for(int j=0;j<number_of_states;j++)
            {
                states[in_level][j] = new State(LABEL_LEVEL+in_level+"_"+MOUSE_ID+j);

                if(in_level == INIT_LEVEL && j == INIT_ROOM)
                {
                    states[in_level][j].setInitial(true);
                    states[in_level][j].setAccepting(true);
                }

                mouse.addState(states[in_level][j]);
            }

            mouse.addArc(new Arc(states[in_level][4], states[in_level][3], events[4]));
            mouse.addArc(new Arc(states[in_level][3], states[in_level][0], events[5]));
            mouse.addArc(new Arc(states[in_level][0], states[in_level][2], events[0]));
            mouse.addArc(new Arc(states[in_level][2], states[in_level][1], events[1]));
            mouse.addArc(new Arc(states[in_level][1], states[in_level][0], events[2]));
            mouse.addArc(new Arc(states[in_level][0], states[in_level][4], events[3]));

        }

        for(int in_level=0; in_level<num_levels;in_level++)
        {
            final int ROOM_ID = in_level%(CatMouse.NUMBER_OF_ROOMS);

            if((in_level+1)<num_levels)
            {
                final LabeledEvent lem = new LabeledEvent(MOUSE_ID+id+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));

                lem.setControllable(true);

                mouse.getAlphabet().addEvent(lem);

                State thisState, state_1=null, state_2=null;
                int cnt = 0;
                int level_index;
                int room_index;

                final TreeSet<State> treeset =
                  new TreeSet<State>(mouse.getStateSet());
                final Iterator<State> iter_state = treeset.descendingIterator();
                while(iter_state.hasNext())
                {
                    thisState = (State)iter_state.next();
                    final String state_name = thisState.getName();
                    level_index = Integer.parseInt(state_name.substring(1,state_name.indexOf("_")));
                    room_index = Integer.parseInt(state_name.substring(state_name.indexOf(MOUSE_ID)+1));

                    if(in_level == level_index && ROOM_ID == room_index){
                        state_1 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }

                    if((in_level+1) == level_index && ROOM_ID == room_index){
                        state_2 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }
                }

                mouse.addArc(new Arc(state_1,state_2,lem));
                mouse.addArc(new Arc(state_2,state_1,lem));

            }
        }

    }

    public Automaton getMouse()
    {
        return mouse;
    }

    public void renameEvent(final Automaton sm, final int ev_index, final String new_label)
    {
        final Alphabet alpha = sm.getAlphabet();
        final LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        final LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);

    }

    public Automaton build(final int id)
    throws Exception
    {
        final Automaton sm = new Automaton(mouse);
        sm.setName(MOUSE_NAME + NAME_SEP + id);

        StringTokenizer st;
        for(int i=0;i<events.length;i++){
            st = new StringTokenizer(events[i].getLabel(), MOUSE_ID);
            renameEvent(sm, i, MOUSE_ID + id + LABEL_SEP + st.nextToken());
        }

        return sm;
    }

    public Automaton getAutomaton()
    {
        return mouse;
    }
}

interface RoomBuilder
{
    public Automaton build(Automaton thisRoom, int in_level, int nl)
    throws Exception;
}

// Builds a room automaton
class Room implements RoomBuilder
{
    public final String ROOM_NAME = "Room";
    public final String ROOM_ID = "r";
    public final String CAT_ID = "c";
    public final String MOUSE_ID = "m";


    private static int number_of_states;
    static int num_cats;

    static State[] states;

    static LabeledEvent[][] events;

    static Arc[][][] arcs;

    private final int RE=0;
    private int[] RC;
    private int[] RM;

    final static String LABEL_SEP = "_";

    // note, must be the same in both Philosopher and Fork
    final static String NAME_SEP = ":";

    // Need not be the same everywhere
    static Automaton room = null;
    static boolean inited = false;
    @SuppressWarnings("unused")
	private int length;

    int id;

    public Room(){}

    public Room(final int id, final int num_cats)
    throws Exception
    {
        this.id = id;
        Room.num_cats = num_cats;

        number_of_states = 1 + 2*(num_cats);
        states = new State[number_of_states];

        RC = new int[num_cats+1];
        RM = new int[num_cats+1];
        states[RE] = new State(ROOM_ID+id+"e");
        for(int i=1;i<=num_cats;i++)
        {
            RC[i] = i;
            RM[i] = i+num_cats;

            states[RC[i]] = new State(ROOM_ID+id+LABEL_SEP+i+CAT_ID);
            states[RM[i]] = new State(ROOM_ID+id+LABEL_SEP+i+MOUSE_ID);
        }

/*        if (inited)
        {
            // The only thing that may need to be changed is the controllability
            Alphabet alpha = fork.getAlphabet();
            alpha.getEvent(events[L_TAKE].getLabel()).setControllable(l_take);
            alpha.getEvent(events[R_TAKE].getLabel()).setControllable(r_take);
            alpha.getEvent(events[L_PUT].getLabel()).setControllable(l_put);
            alpha.getEvent(events[R_PUT].getLabel()).setControllable(r_put);

            return;
        }
 */

        room = new Automaton("Room template");
        room.setType(AutomatonType.SPECIFICATION);
        events = new LabeledEvent[num_cats][];
        arcs = new Arc[num_cats][num_cats][];

        switch(id)
        {
            case 0:
                states[RE].setInitial(true);
                states[RE].setAccepting(true);

                for(int i=0;i<num_cats;i++)
                {
                    events[i] = new LabeledEvent[8];

                    events[i][0] = new LabeledEvent(CAT_ID+i+LABEL_SEP+2);
                    events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+5);
                    events[i][2] = new LabeledEvent(CAT_ID+i+LABEL_SEP+3);
                    events[i][3] = new LabeledEvent(CAT_ID+i+LABEL_SEP+0);

                    events[i][4] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+2);
                    events[i][5] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+5);
                    events[i][6] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+3);
                    events[i][7] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+0);

                    for(int j=0; j<events[i].length;j++)
                        events[i][j].setControllable(true);

                    for(int k=1;k<=num_cats;k++)
                    {
                        arcs[k-1][i] = new Arc[8];

                        if(k==1)
                        {
                            arcs[k-1][i][0] = new Arc(states[RE], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RE], states[RC[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RE], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RE], events[i][3]);

                            arcs[k-1][i][4] = new Arc(states[RE], states[RM[k]], events[i][4]);
                            arcs[k-1][i][5] = new Arc(states[RE], states[RM[k]], events[i][5]);
                            arcs[k-1][i][6] = new Arc(states[RM[k]], states[RE], events[i][6]);
                            arcs[k-1][i][7] = new Arc(states[RM[k]], states[RE], events[i][7]);
                        }
                        else
                        {
                            arcs[k-1][i][0] = new Arc(states[RC[k-1]], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RC[k-1]], states[RC[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RC[k-1]], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RC[k-1]], events[i][3]);

                            arcs[k-1][i][4] = new Arc(states[RM[k-1]], states[RM[k]], events[i][4]);
                            arcs[k-1][i][5] = new Arc(states[RM[k-1]], states[RM[k]], events[i][5]);
                            arcs[k-1][i][6] = new Arc(states[RM[k]], states[RM[k-1]], events[i][6]);
                            arcs[k-1][i][7] = new Arc(states[RM[k]], states[RM[k-1]], events[i][7]);
                        }
                    }
                }

                break;
            case 1:
                states[RE].setInitial(true);
                states[RE].setAccepting(true);

                for(int i=0;i<num_cats;i++)
                {
                    events[i] = new LabeledEvent[5];

                    events[i][0] = new LabeledEvent(CAT_ID+i+LABEL_SEP+0);
                    events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+1);

                    events[i][2] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+1);
                    events[i][3] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+2);

                    events[i][4] = new LabeledEvent(CAT_ID+i+LABEL_SEP+6);

                    for(int j=0; j<(events[i].length-1);j++)
                        events[i][j].setControllable(true);
                    events[i][(events[i].length-1)].setControllable(false);

                    for(int k=1;k<=num_cats;k++)
                    {
                        arcs[k-1][i] = new Arc[6];

                        if(k==1)
                        {
                            arcs[k-1][i][0] = new Arc(states[RE], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RE], states[RC[k]], events[i][4]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RE], events[i][1]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RE], events[i][4]);

                            arcs[k-1][i][4] = new Arc(states[RE], states[RM[k]], events[i][2]);
                            arcs[k-1][i][5] = new Arc(states[RM[k]], states[RE], events[i][3]);
                        }
                        else
                        {
                            arcs[k-1][i][0] = new Arc(states[RC[k-1]], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RC[k-1]], states[RC[k]], events[i][4]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RC[k-1]], events[i][1]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RC[k-1]], events[i][4]);

                            arcs[k-1][i][4] = new Arc(states[RM[k-1]], states[RM[k]], events[i][2]);
                            arcs[k-1][i][5] = new Arc(states[RM[k]], states[RM[k-1]], events[i][3]);
                        }
                    }
                }
                break;
            case 2:
                    states[RC[num_cats]].setInitial(true);
                    states[RC[num_cats]].setAccepting(true);

                for(int i=0;i<num_cats;i++)
                {
                    events[i] = new LabeledEvent[4];

                    events[i][0] = new LabeledEvent(CAT_ID+i+LABEL_SEP+2);
                    events[i][1] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+0);

                    events[i][2] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+1);
                    events[i][3] = new LabeledEvent(CAT_ID+i+LABEL_SEP+1);

                    for(int j=0; j<events[i].length;j++)
                        events[i][j].setControllable(true);

                    for(int k=1;k<=num_cats;k++)
                    {
                        arcs[k-1][i] = new Arc[4];

                        if(k==1)
                        {
                            arcs[k-1][i][0] = new Arc(states[RC[k]], states[RE], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RE], states[RM[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RM[k]], states[RE], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RE], states[RC[k]], events[i][3]);
                        }
                        else
                        {
                            arcs[k-1][i][0] = new Arc(states[RC[k]], states[RC[k-1]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RM[k-1]], states[RM[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RM[k]], states[RM[k-1]], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RC[k-1]], states[RC[k]], events[i][3]);
                        }
                    }
                }

                break;
            case 3:
                states[RE].setInitial(true);
                states[RE].setAccepting(true);

                for(int i=0;i<num_cats;i++)
                {
                    events[i] = new LabeledEvent[5];

                    events[i][0] = new LabeledEvent(CAT_ID+i+LABEL_SEP+3);
                    events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+4);

                    events[i][2] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+4);
                    events[i][3] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+5);

                    events[i][4] = new LabeledEvent(CAT_ID+i+LABEL_SEP+6);

                    for(int j=0; j<(events[i].length-1);j++)
                        events[i][j].setControllable(true);
                    events[i][(events[i].length-1)].setControllable(false);

                    for(int k=1;k<=num_cats;k++)
                    {
                        arcs[k-1][i] = new Arc[6];

                        if(k==1)
                        {
                            arcs[k-1][i][0] = new Arc(states[RE], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RE], states[RC[k]], events[i][4]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RE], events[i][1]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RE], events[i][4]);

                            arcs[k-1][i][4] = new Arc(states[RE], states[RM[k]], events[i][2]);
                            arcs[k-1][i][5] = new Arc(states[RM[k]], states[RE], events[i][3]);
                        }
                        else
                        {
                            arcs[k-1][i][0] = new Arc(states[RC[k-1]], states[RC[k]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RC[k-1]], states[RC[k]], events[i][4]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RC[k-1]], events[i][1]);
                            arcs[k-1][i][3] = new Arc(states[RC[k]], states[RC[k-1]], events[i][4]);

                            arcs[k-1][i][4] = new Arc(states[RM[k-1]], states[RM[k]], events[i][2]);
                            arcs[k-1][i][5] = new Arc(states[RM[k]], states[RM[k-1]], events[i][3]);
                        }
                    }
                }

                break;
            case 4:
                states[RM[num_cats]].setInitial(true);
                states[RM[num_cats]].setAccepting(true);

                for(int i=0;i<num_cats;i++)
                {
                    events[i] = new LabeledEvent[4];

                    events[i][0] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+4);
                    events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+4);

                    events[i][2] = new LabeledEvent(CAT_ID+i+LABEL_SEP+5);
                    events[i][3] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+3);

                    for(int j=0; j<events[i].length; j++)
                        events[i][j].setControllable(true);

                    for(int k=1;k<=num_cats;k++)
                    {
                        arcs[k-1][i] = new Arc[4];

                        if(k==1)
                        {
                            arcs[k-1][i][0] = new Arc(states[RM[k]], states[RE], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RE], states[RC[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RE], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RE], states[RM[k]], events[i][3]);
                        }
                        else
                        {
                            arcs[k-1][i][0] = new Arc(states[RM[k]], states[RM[k-1]], events[i][0]);
                            arcs[k-1][i][1] = new Arc(states[RC[k-1]], states[RC[k]], events[i][1]);
                            arcs[k-1][i][2] = new Arc(states[RC[k]], states[RC[k-1]], events[i][2]);
                            arcs[k-1][i][3] = new Arc(states[RM[k-1]], states[RM[k]], events[i][3]);
                        }
                    }
                }

                break;
        }

        for (int i = 0; i < states.length; ++i)
        {
            room.addState(states[i]);
        }

        for(int i=0; i<num_cats; i++)
            for (int j = 0; j < events[i].length; ++j)
                room.getAlphabet().addEvent(events[i][j]);

        for(int i=0; i<num_cats; i++)
            for(int k=0; k<num_cats; k++)
                for(int j=0;j<arcs[k][i].length;j++)
                    room.addArc(arcs[k][i][j]);

//      inited = true;
    }

    public void renameState(final Automaton sm, final int st_index, final String new_label)
    {
        sm.getStateSet();
        final State st_old = sm.getStateWithIndex(st_index);
        final State st_new = new State(st_old,new_label);
        sm.replaceState(st_old, st_new);
    }

	@Override
    public Automaton build(final Automaton thisRoom, final int in_level, final int num_levels)
    throws Exception
    {

        final Automaton sm = new Automaton(thisRoom);
        sm.setName(ROOM_NAME + NAME_SEP + id);


        /*
         The following part should indeed be in the build() function belonging to Level, however, then the 'waters' package
         would give an exception while drawing the automata. This is because some of the names for events and states
         created in the Room class will be modified which is apparently not accpeted by 'waters'.
        */
        if(in_level >= 0)
        {
            final String LABEL_LEVEL = new Level().LEVEL_NAME;
            for(int j=0;j<sm.nbrOfStates();j++)
            {
                final State s = sm.getStateWithIndex(j);
                final String old_n = s.getName();
                renameState(sm,j,LABEL_LEVEL+in_level+LABEL_SEP+old_n);
            }


            if((in_level%(CatMouse.NUMBER_OF_ROOMS))== id )
            {
                if((in_level+1)<num_levels && num_cats > 1)
                {
                    final LabeledEvent[] lec = new LabeledEvent[num_cats];
                    final LabeledEvent[] lem = new LabeledEvent[num_cats];
                    for(int h=0;h<num_cats;h++)
                    {
                        lec[h] = new LabeledEvent("c"+h+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));
                        lem[h] = new LabeledEvent("m"+h+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));

                        lec[h].setControllable(true);
                        lem[h].setControllable(true);

                        sm.getAlphabet().addEvent(lec[h]);
                        sm.getAlphabet().addEvent(lem[h]);
                    }

                    State init_state;

                    //compute which states specify cat and mouse
/*                    for(int m=0;m<room.nbrOfStates();m++)
                    {
                        ECM = room.getStateWithIndex(m).getName();
                        if(ECM.contains("e"))
                            RE = m;
                        if(ECM.contains("c"))
                        {
                            RC[RC_cnt] = m;
                            RC_cnt++;
                        }
                        if(ECM.contains("m"))
                        {
                            RM[RM_cnt] = m;
                            RM_cnt++;
                        }
                    }
*/ //                 System.out.println("in level: "+in_level);
                    for(int k=1;k<=num_cats;k++)
                    {
                        init_state = sm.getInitialState();
                        if(k==1)
                        {
                             for(int h=0;h<num_cats;h++)
                            {
                                sm.addArc(new Arc(init_state,sm.getStateWithIndex(RC[k]),lec[h]));
                                sm.addArc(new Arc(sm.getStateWithIndex(RC[k]),init_state,lec[h]));

                                if(id!=2 && id!=4)
                                {
                                    sm.addArc(new Arc(init_state,sm.getStateWithIndex(RM[k]),lem[h]));
                                    sm.addArc(new Arc(sm.getStateWithIndex(RM[k]),init_state,lem[h]));
                                }
                             }
                        }
                        else
                        {
                             for(int h=0;h<num_cats;h++)
                            {
                                if(!sm.getStateWithIndex(RC[k-1]).getName().equals(init_state.getName()) && !sm.getStateWithIndex(RC[k]).getName().equals(init_state.getName()))
                                {
                                    sm.addArc(new Arc(sm.getStateWithIndex(RC[k-1]),sm.getStateWithIndex(RC[k]),lec[h]));
                                    sm.addArc(new Arc(sm.getStateWithIndex(RC[k]),sm.getStateWithIndex(RC[k-1]),lec[h]));
                                }

                                if(!sm.getStateWithIndex(RM[k-1]).getName().equals(init_state.getName()) && !sm.getStateWithIndex(RM[k]).getName().equals(init_state.getName()))
                                {
                                    sm.addArc(new Arc(sm.getStateWithIndex(RM[k-1]),sm.getStateWithIndex(RM[k]),lem[h]));
                                    sm.addArc(new Arc(sm.getStateWithIndex(RM[k]),sm.getStateWithIndex(RM[k-1]),lem[h]));
                                }
                            }
                        }

                     /*   for(int i=0;i<sm.nbrOfStates();i++)
                        {
                            System.out.println("state name: "+sm.getStateWithIndex(i).getName());
                            System.out.println("number of arcs: "+sm.getStateWithIndex(i).nbrOfOutgoingArcs());
                        }*/
                    } //
                }
            }
        }

        return sm;
    }
    public Automaton getAutomaton()
    {
        return room;
    }

}

public class CatMouse
{
    private final Project project = new Project("Cat & Mouse");
    final static int NUMBER_OF_ROOMS = 5;
	private final static String X_SPEC_NAME = "x:SpecC&M";
		
    // These are helpers for counting modulo num philos/forks
    // Note that we adjust for 0's, indices are from 1 to modulo

	public CatMouse(final int num)
		throws Exception
	{
		this(num, false); // Build the model with the Room specs (instead of self-looped forbidden states)
	}
	
	public CatMouse(final int num, final boolean use_selfloops)
		throws Exception
	{
        project.setComment("The cat and mouse problem. The cat and mouse must never be in the same room. " +
			"This is specified 'locally', by the five specifications for the different rooms. " +
			"Since this is a static specification, this can also be expressed 'globally' " +
			"as a set of forbidden states in the composed plant model. " +
			"This is done in a 'modular' way when the 'Use self-loops' option is checked.");
		
		final Automaton cats[] = new Automaton[num]; // These are really only needed when we use forbidden self-loops
		final Automaton mice[] = new Automaton[num];
		
        final Cat cat_builder = new Cat();
        final Mouse mouse_builder = new Mouse();
		
        for (int i = 0; i < num; ++i)
		{
			cats[i] = cat_builder.build(i);
			mice[i] = mouse_builder.build(i);
			
            project.addAutomaton(cats[i]);
            project.addAutomaton(mice[i]);
        }
		
		// The Room specs are only neede when not using self-loops to express forbidden states
		if(!use_selfloops)
		{
			for (int i = 0; i < NUMBER_OF_ROOMS; ++i)
			{
				final Room room = new Room(i,num);
				project.addAutomaton(room.build(room.getAutomaton(), -1,-1));
			}
		}
		else // Use self-loops to express the forbidden states -- see also org.supremica.automata.algorithms.Forbidder
		{
			// Generate uniquely-named uc-events
			// Add selfloops on the correct combinations
			// Generate the forbidden state x_spec.
			
			// Generate the single state x_spec, and below add the events to it as a blocked event
			final Automaton x_spec = new Automaton(X_SPEC_NAME);
			x_spec.setType(AutomatonType.SPECIFICATION);
			final State init_state = new State("x0");
			init_state.setInitial(true);
			init_state.setAccepting(true);
			x_spec.addState(init_state);
						
			final Alphabet x_alpha = x_spec.getAlphabet();	// Holds the x-events
			
			// Note that the states need to be pair-wise forbidden, for each cat it cannot be allowed in the same room as any mouse
			for(int c = 0; c < cats.length; c++)
			{
				for(int m = 0; m < mice.length; m++)
				{
					for(int r = 0; r < NUMBER_OF_ROOMS; r++)
					{
						final Automaton cat = cats[c];
						final Automaton mouse = mice[m];
						
						// Find in each component the states that should have forbidden self-loops
						final String m_room = "m" + r;
						final String c_room = "c" + r;
						final State m_state = mouse.getStateWithName(m_room);
						final State c_state = cat.getStateWithName(c_room);
						
						// Create and add the forbidden events - needs to be done before adding the self-loops
						final StringBuffer x_event_label = new StringBuffer(Forbidder.FORBIDDEN_EVENT_PREFIX);
						x_event_label.append('c').append(c).append('m').append(m).append('r').append(r);
						final LabeledEvent x_event = new ForbiddenEvent(x_event_label.toString());
						x_event.setControllable(false);
						cat.getAlphabet().addEvent(x_event);
						mouse.getAlphabet().addEvent(x_event);
						
						// Create and add the self-loops
						final Arc m_arc = new Arc(m_state, m_state, x_event);
						final Arc c_arc = new Arc(c_state, c_state, x_event);
						mouse.addArc(m_arc);
						cat.addArc(c_arc);
						
						// Add the event to the x_spec as a blocked event (meaning, no arcs)
						x_alpha.addEvent(x_event);
					}
				}
			}
			project.addAutomaton(x_spec);
		}
    }
	
	public Project getProject()
    {
        return project;
    }
	
	// For debugging only
	public static void main(String[] args)
	{
		try
		{
//			final CatMouse cm = new CatMouse(1, true);
//			System.out.println(cm.project.toString());
//			for(int i = 0; i < cm.project.nbrOfAutomata(); i++)
//			{
//				final Automaton aut = cm.project.getAutomatonAt(i);
//				System.out.println(aut.toDebugString());
//			}
			testForbidderForbidStates();

		}
		catch(final Exception excp)
		{
			excp.printStackTrace();
		}
	}
	
	private static void testForbidderForbidStates()
		throws Exception
	{
		final Automaton cat = new Cat().build(1);
		final Automaton mouse = new Mouse().build(1);
		final Automaton animals[] = new Automaton[2];
		animals[0] = cat;
		animals[1] = mouse;

		State[][] stateset = new State[NUMBER_OF_ROOMS][animals.length];

		for(int r = 0; r < NUMBER_OF_ROOMS; r++)
		{
			// Find in each component the states that should have forbidden self-loops
			final String c_room = "c" + r;
			final String m_room = "m" + r;
			final State c_state = cat.getStateWithName(c_room);	
			final State m_state = mouse.getStateWithName(m_room);

			stateset[r][0] = c_state;
			stateset[r][1] = m_state;
		}

		final Automaton x_spec = Forbidder.forbidStates(animals, stateset, Forbidder.FORBIDDEN_EVENT_PREFIX, true);
		
		System.out.println(cat.toDebugString());
		System.out.println(mouse.toDebugString());
		System.out.println(x_spec.toDebugString());
	}
}

