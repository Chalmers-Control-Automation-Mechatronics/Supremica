/*
 * CatMouse.java
 *
 * Created on February 29, 2008, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import java.util.Iterator;
import java.util.StringTokenizer;
import org.omg.CORBA.portable.IDLEntity;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.StateSet;
import org.supremica.automata.execution.*;
import uk.ac.ic.doc.scenebeans.Null;

/**
 *
 * @author Sajed
 */

// Builds a Cat automaton
class Cat
{
    public final String CAT_NAME = "Cat";
    public final String CAT_ID = "c";
    
    final static int number_of_states = 5;
    static State[][] states;
      
    final static int number_of_events = 7;
    static LabeledEvent[] events;


    final static String LABEL_SEP = "_";
    
    // note, must be the same in both Cat and Mouse
    final static String NAME_SEP = ":";
    
    // Need not be the same everywhere
    static Automaton cat = null;
    
    static boolean inited = false;
    
    int i;
    
    public Cat()
    throws Exception
    {
        events = new LabeledEvent[number_of_events];
        states = new State[1][number_of_states];
        for(i=0; i<number_of_states;i++)
            states[0][i] = new State(CAT_ID+i);
        
        for(i=0; i<number_of_events;i++)
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
        for (int i = 0; i < states.length; ++i)
        {
            cat.addState(states[0][i]);
        }
        
        for(i=0; i<(events.length-1);i++)
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
    
    public Cat(int id, int num_levels)
    throws Exception
    {
         events = new LabeledEvent[number_of_events];
        states = new State[1][number_of_states];
        
        cat = new Automaton(CAT_NAME+NAME_SEP+id);
        cat.setType(AutomatonType.PLANT);
        
        String LABEL_LEVEL = new Level().LEVEL_NAME;
        
        states = new State[num_levels][number_of_states];
        
        int INIT_LEVEL = 0;
        int INIT_ROOM = 2;
        
        for(i=0; i<number_of_events;i++)
        {
            events[i] = new LabeledEvent(CAT_ID + id + LABEL_SEP + i);
            if(i!=(number_of_events-1))
                events[i].setControllable(true);
            else
                events[i].setControllable(false);
            
            cat.getAlphabet().addEvent(events[i]);
        }

        for(int in_level=0; in_level<num_levels;in_level++)
        {
            for(int j=0;j<number_of_states;j++)
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
        
        for(int in_level=0; in_level<num_levels;in_level++)
        {
            int ROOM_ID = in_level%(new CatMouse().number_of_rooms);

            if((in_level+1)<num_levels)
            {
                LabeledEvent lec = new LabeledEvent("c"+id+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));

                lec.setControllable(true);

                cat.getAlphabet().addEvent(lec);

                State thisState, state_1=null, state_2=null;
                int cnt = 0;
                int level_index;
                int room_index;

                Iterator<State> iter_state = cat.getStateSet().descendingIterator();
                while(iter_state.hasNext())
                {
                    thisState = (State)iter_state.next();
                    String state_name = thisState.getName();
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
    
    public void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);
        
    }
    
    public void renameState(Automaton sm, int st_index, final String new_label)
    {
        StateSet ss = sm.getStateSet();
        State st_old = sm.getStateWithIndex(st_index);
        State st_new = new State(st_old,new_label);
        sm.replaceState(st_old, st_new);
    }
    
    public Automaton build(int id)
    throws Exception
    {
        Automaton sm = new Automaton(cat);
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
    int i;
    
    public Mouse()
    throws Exception
    {
        states = new State[1][number_of_states];
        events = new LabeledEvent[number_of_events];
        event_indices = new int[number_of_events];
        for(i=0; i<number_of_states;i++)
            states[0][i] = new State(MOUSE_ID+i);
        
        for(i=0; i<number_of_events;i++)
            events[i] = new LabeledEvent(MOUSE_ID+i);
        
        //indices into events
        for(i=0; i<number_of_events;i++)
            event_indices[i] = i;
        
        this.length = length;

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
        for (int i = 0; i < states.length; ++i)
        {
            mouse.addState(states[0][i]);
        }
        
        for(i=0; i<number_of_events;i++)
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
    
    public Mouse(int id, int num_levels)
    throws Exception
    {

        events = new LabeledEvent[number_of_events];
        event_indices = new int[number_of_events];
        
        mouse = new Automaton(MOUSE_NAME+NAME_SEP+id);
        mouse.setType(AutomatonType.PLANT);
        
        String LABEL_LEVEL = new Level().LEVEL_NAME;
        
        states = new State[num_levels][number_of_states];
        
        int INIT_LEVEL = num_levels-1;
        int INIT_ROOM = 4;
        
        for(i=0; i<number_of_events;i++)
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
            int ROOM_ID = in_level%(new CatMouse().number_of_rooms);

            if((in_level+1)<num_levels)
            {
                LabeledEvent lem = new LabeledEvent(MOUSE_ID+id+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));

                lem.setControllable(true);

                mouse.getAlphabet().addEvent(lem);

                State thisState, state_1=null, state_2=null;
                int cnt = 0;
                int level_index;
                int room_index;

                Iterator<State> iter_state = mouse.getStateSet().descendingIterator();
                while(iter_state.hasNext())
                {
                    thisState = (State)iter_state.next();
                    String state_name = thisState.getName();
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
    
    public void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);
        
    }
    
    public Automaton build(int id)
    throws Exception
    {
        Automaton sm = new Automaton(mouse);
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
class Room
    implements RoomBuilder
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
    private int length;
    
    int id;
    
    public Room(){}
    
    public Room(int id, int num_cats)
    throws Exception
    {
        this.id = id;
        this.length = length;
        this.num_cats = num_cats;
        
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
    
    public void renameState(Automaton sm, int st_index, final String new_label)
    {
        StateSet ss = sm.getStateSet();
        State st_old = sm.getStateWithIndex(st_index);
        State st_new = new State(st_old,new_label);
        sm.replaceState(st_old, st_new);   
    }
    
    public Automaton build(Automaton thisRoom, int in_level, int num_levels)
    throws Exception
    {
        
        Automaton sm = new Automaton(thisRoom);
        sm.setName(ROOM_NAME + NAME_SEP + id);

        
        /*
         The following part should indeed be in the build() function belonging to Level, however, then the 'waters' package 
         would give an exception while drawing the automata. This is because some of the names for events and states 
         created in the Room class will be modified which is apparently not accpeted by 'waters'.
        */
        if(in_level>=0)
        {  
            String LABEL_LEVEL = new Level().LEVEL_NAME;
            for(int j=0;j<sm.nbrOfStates();j++)
            {
                State s = sm.getStateWithIndex(j);
                String old_n = s.getName();
                renameState(sm,j,LABEL_LEVEL+in_level+LABEL_SEP+old_n);
            }
            
            
            if((in_level%(new CatMouse().number_of_rooms))== id )
            {
                if((in_level+1)<num_levels && num_cats > 1)
                {
                    LabeledEvent[] lec = new LabeledEvent[num_cats];
                    LabeledEvent[] lem = new LabeledEvent[num_cats];
                    for(int h=0;h<num_cats;h++)
                    {
                        lec[h] = new LabeledEvent("c"+h+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));
                        lem[h] = new LabeledEvent("m"+h+"_"+LABEL_LEVEL+in_level+LABEL_LEVEL+(in_level+1));
                        
                        lec[h].setControllable(true);
                        lem[h].setControllable(true);
                        
                        sm.getAlphabet().addEvent(lec[h]);
                        sm.getAlphabet().addEvent(lem[h]);
                    }
                    
                    int RC_cnt = 1;
                    int RM_cnt = 1;
                    String ECM;
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
    Project project = new Project("Cat & Mouse");
    public final int number_of_rooms = 5;
    
    // These are helpers for counting modulo num philos/forks
    // Note that we adjust for 0's, indices are from 1 to modulo
    
    public CatMouse(){}
    
    public CatMouse(int num)
        throws Exception
    {
        // Add comment
        project.setComment("The cat and mouse problem. The cat and mouse must never be in the same room. This is specified 'locally', by the five specifications for the different rooms. Since this is a static specification, this can also be expressed 'globally' as a set of forbidden states in the composed plant model, 'cat||mouse'.");
        
        int idLength = ("" + num).length();
        
        Cat cat = new Cat();
        Mouse mouse = new Mouse();
        for (int i = 0; i < num; ++i){
            project.addAutomaton(cat.build(i));
            project.addAutomaton(mouse.build(i));
        }
        
        Room room;
        for (int i = 0; i < number_of_rooms; ++i)
        {
            room = new Room(i,num);
            project.addAutomaton(room.build(room.getAutomaton(), -1,-1));
        }
        
    }
    
    public Project getProject()
    {
        return project;
    }
}

