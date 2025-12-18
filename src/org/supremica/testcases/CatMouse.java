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
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.Forbidder;

class Seps
{
  final static String LABEL_SEP = "_";
  final static String NAME_SEP = ":";
}

/**
 *
 * @author Sajed
 */
class Cat
{
    private final static String CAT_NAME = "Cat";
    public final static String CAT_ID = "c";

    private final static int NUMBER_OF_STATES = 5;
    private State[][] states;

    private final static int NUMBER_OF_EVENTS = 7;
    private LabeledEvent[] events;

    private Automaton cat;

    public Cat()
    throws Exception
    {
        this.events = new LabeledEvent[NUMBER_OF_EVENTS];
        this.states = new State[1][NUMBER_OF_STATES];
        
        for(int i = 0; i < NUMBER_OF_STATES; i++)
            states[0][i] = new State(CAT_ID + i);
            
        for(int i = 0; i < NUMBER_OF_EVENTS; i++)
            events[i] = new LabeledEvent(CAT_ID + i);
            
      buildSimpleTemplate();
    }
    
    private void buildSimpleTemplate()
    {
        this.cat = new Automaton(CAT_NAME);
        cat.setType(AutomatonType.PLANT);

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
        addArcs(0);
    }

    public Cat(final int id, final int num_levels)
    throws Exception
    {
        this.events = new LabeledEvent[NUMBER_OF_EVENTS];
        this.states = new State[1][NUMBER_OF_STATES];

        this.cat = new Automaton(CAT_NAME + Seps.NAME_SEP + id);
        cat.setType(AutomatonType.PLANT);

        final String FLOOR_LABEL = Floor.getFloorLabel();

        states = new State[num_levels][NUMBER_OF_STATES];

        final int INIT_LEVEL = 0;
        final int INIT_ROOM = 2;

        for(int i = 0; i < NUMBER_OF_EVENTS; i++)
        {
            events[i] = new LabeledEvent(CAT_ID + id + Seps.LABEL_SEP + i);
            events[i].setControllable(true);
            
            if(i == NUMBER_OF_EVENTS - 1)
                events[i].setControllable(false);

            cat.getAlphabet().addEvent(events[i]);
        }

        for(int in_floor = 0; in_floor < num_levels; in_floor++)
        {
            for(int j = 0; j < NUMBER_OF_STATES; j++)
            {
                states[in_floor][j] = new State(FLOOR_LABEL + in_floor + Seps.LABEL_SEP + CAT_ID + j);

                if(in_floor == INIT_LEVEL && j == INIT_ROOM)
                {
                    states[in_floor][j].setInitial(true);
                    states[in_floor][j].setAccepting(true);
                }

                cat.addState(states[in_floor][j]);
            }
            addArcs(in_floor);
        }

        for(int in_floor = 0; in_floor < num_levels; in_floor++)
        {
            final int ROOM_ID = in_floor % CatMouse.NUMBER_OF_ROOMS;

            if((in_floor+1) < num_levels)
            {
                final LabeledEvent lec = new LabeledEvent(CAT_ID + id + Seps.LABEL_SEP + FLOOR_LABEL + in_floor + FLOOR_LABEL +(in_floor+1));

                lec.setControllable(true);

                cat.getAlphabet().addEvent(lec);

                // State thisState;
                State state_1 = null, state_2 = null;
                int cnt = 0;
                // int level_index;
                // int room_index;

                final TreeSet<State> treeset = new TreeSet<State>(cat.getStateSet());
                final Iterator<State> iter_state = treeset.descendingIterator();
                while(iter_state.hasNext())
                {
                    final State thisState = iter_state.next();
                    final String state_name = thisState.getName();
                    final int level_index = Integer.parseInt(state_name.substring(1, state_name.indexOf(Seps.LABEL_SEP)));
                    final int room_index = Integer.parseInt(state_name.substring(state_name.indexOf(CAT_ID)+1));

                    if(in_floor == level_index && ROOM_ID == room_index)
                    {
                        state_1 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }

                    if((in_floor+1) == level_index && ROOM_ID == room_index)
                    {
                        state_2 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }
                }

                cat.addArc(new Arc(state_1, state_2, lec));
                cat.addArc(new Arc(state_2, state_1, lec));

            }
        }
    }
    
    private void addArcs(int in_floor)
    {
      cat.addArc(new Arc(states[in_floor][2], states[in_floor][0], events[2]));
      cat.addArc(new Arc(states[in_floor][0], states[in_floor][1], events[0]));
      cat.addArc(new Arc(states[in_floor][1], states[in_floor][2], events[1]));
      cat.addArc(new Arc(states[in_floor][0], states[in_floor][3], events[3]));
      cat.addArc(new Arc(states[in_floor][3], states[in_floor][4], events[4]));
      cat.addArc(new Arc(states[in_floor][4], states[in_floor][0], events[5]));
      cat.addArc(new Arc(states[in_floor][1], states[in_floor][3], events[6]));
      cat.addArc(new Arc(states[in_floor][3], states[in_floor][1], events[6]));
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
        // sm.getStateSet();
        final State st_old = sm.getStateWithIndex(st_index);
        final State st_new = new State(st_old,new_label);
        sm.replaceState(st_old, st_new);
    }

    public Automaton build(final int id)
    throws Exception
    {
        final Automaton sm = new Automaton(cat);
        sm.setName(CAT_NAME + Seps.NAME_SEP + id);
        StringTokenizer st;
        for(int i=0;i<events.length;i++)
        {
            st = new StringTokenizer(events[i].getLabel(), CAT_ID);
            renameEvent(sm, i, CAT_ID + id + Seps.LABEL_SEP + st.nextToken());
        }
        
        return sm;
    }


    public Automaton getAutomaton()
    {
        return cat;
    }

}

// Builds a Mouse automaton
class Mouse
{
    public final static String MOUSE_NAME = "Mouse";
    public final static String MOUSE_ID = "m";

    private final int number_of_states = 5;
    private State[][] states;

    private final int number_of_events = 6;
    private LabeledEvent[] events;
    private int[] event_indices;

    private Automaton mouse;

    private int length;

    public Mouse()
    throws Exception
    {
        this.states = new State[1][number_of_states];
        this.events = new LabeledEvent[number_of_events];
        this.event_indices = new int[number_of_events];
        
        for(int i = 0; i < number_of_states; i++)
            states[0][i] = new State(MOUSE_ID+i);

        for(int i = 0; i < number_of_events; i++)
            events[i] = new LabeledEvent(MOUSE_ID + i);

        //indices into events
        for(int i = 0; i < number_of_events; i++)
            event_indices[i] = i;
        
        buildSimpleTemplate();
    }
    
    private void buildSimpleTemplate()
    {
        mouse = new Automaton(MOUSE_NAME);
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
        addArcs(0);   
    }

    public Mouse(final int id, final int num_floors)
    throws Exception
    {

        events = new LabeledEvent[number_of_events];
        event_indices = new int[number_of_events];

        mouse = new Automaton(MOUSE_NAME + Seps.NAME_SEP + id);
        mouse.setType(AutomatonType.PLANT);

        final String FLOOR_LABEL = Floor.getFloorLabel();

        states = new State[num_floors][number_of_states];

        final int INIT_LEVEL = num_floors-1;
        final int INIT_ROOM = 4;

        for(int i =0 ; i < number_of_events; i++)
        {
            events[i] = new LabeledEvent(MOUSE_ID + id + Seps.LABEL_SEP + i);
            if(i!=(number_of_events-1))
                events[i].setControllable(true);

            mouse.getAlphabet().addEvent(events[i]);
        }

        for(int in_floor=0; in_floor<num_floors;in_floor++)
        {
            for(int j=0;j<number_of_states;j++)
            {
                states[in_floor][j] = new State(FLOOR_LABEL + in_floor + Seps.LABEL_SEP + MOUSE_ID + j);

                if(in_floor == INIT_LEVEL && j == INIT_ROOM)
                {
                    states[in_floor][j].setInitial(true);
                    states[in_floor][j].setAccepting(true);
                }

                mouse.addState(states[in_floor][j]);
            }
            addArcs(in_floor);
        }

        for(int in_floor=0; in_floor<num_floors;in_floor++)
        {
            final int ROOM_ID = in_floor%(CatMouse.NUMBER_OF_ROOMS);

            if((in_floor+1)<num_floors)
            {
                final LabeledEvent lem = new LabeledEvent(MOUSE_ID+id+ Seps.LABEL_SEP +FLOOR_LABEL +in_floor+FLOOR_LABEL +(in_floor+1));

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
                    thisState = iter_state.next();
                    final String state_name = thisState.getName();
                    level_index = Integer.parseInt(state_name.substring(1,state_name.indexOf(Seps.LABEL_SEP)));
                    room_index = Integer.parseInt(state_name.substring(state_name.indexOf(MOUSE_ID)+1));

                    if(in_floor == level_index && ROOM_ID == room_index){
                        state_1 = thisState;
                        cnt++;
                        if(cnt == 2)
                            break;
                    }

                    if((in_floor+1) == level_index && ROOM_ID == room_index){
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

    private void addArcs(final int in_floor)
    {
      mouse.addArc(new Arc(states[in_floor][4], states[in_floor][3], events[4]));
      mouse.addArc(new Arc(states[in_floor][3], states[in_floor][0], events[5]));
      mouse.addArc(new Arc(states[in_floor][0], states[in_floor][2], events[0]));
      mouse.addArc(new Arc(states[in_floor][2], states[in_floor][1], events[1]));
      mouse.addArc(new Arc(states[in_floor][1], states[in_floor][0], events[2]));
      mouse.addArc(new Arc(states[in_floor][0], states[in_floor][4], events[3]));  
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
        sm.setName(MOUSE_NAME + Seps.NAME_SEP + id);

        StringTokenizer st;
        for(int i=0;i<events.length;i++){
            st = new StringTokenizer(events[i].getLabel(), MOUSE_ID);
            renameEvent(sm, i, MOUSE_ID + id + Seps.LABEL_SEP + st.nextToken());
        }

        return sm;
    }

    public Automaton getAutomaton()
    {
        return mouse;
    }
}

public class CatMouse
{
    private final Project project; // = new Project("Cat & Mouse");
    final static int NUMBER_OF_ROOMS = 5;
	private final static String X_SPEC_NAME = "x:SpecC&M";

    // These are helpers for counting modulo num philos/forks
    // Note that we adjust for 0's, indices are from 1 to modulo

	public CatMouse(final String name, final int num)
  throws Exception
	{
		this(name, num, false); // Build the model with the Room specs (instead of self-looped forbidden states)
	}

	public CatMouse(final String name, final int num, final boolean use_selfloops)
  throws Exception
	{
		this.project = new Project(name);
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

		// The Room specs are only needed when not using self-loops to express forbidden states
		if(!use_selfloops)
		{
			for (int room_id = 0; room_id < NUMBER_OF_ROOMS; ++room_id)
			{
				final Room room = new Room(room_id, num);
        final int num_floors = 1;
        final int curr_floor = 1;
				project.addAutomaton(room.buildFloorFromTemplate(room.getAutomaton(), curr_floor, num_floors));
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
						final String m_room = Mouse.MOUSE_ID + r;
						final String c_room = Cat.CAT_ID + r;
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
	public static void main(final String[] args)
	{
		try
		{
//			testCatMouse();
			testForbidderForbidStates();
		}
		catch(final Exception excp)
		{
			excp.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
  private static void testCatMouse()
  throws Exception
	{
		final CatMouse cm = new CatMouse("Cat & Mouse", 1, true);
		System.out.println(cm.project.toString());
		for(int i = 0; i < cm.project.nbrOfAutomata(); i++)
		{
			final Automaton aut = cm.project.getAutomatonAt(i);
			System.out.println(aut.toDebugString());
		}
	}

	// This part tests the static function forbidStates() of org.supremica.automata.algorithms.Forbidder
	// Here the simple case of only a single cat and a single mouse...
	private static void testForbidderForbidStates()
		throws Exception
	{
		final Automaton animals[] = new Automaton[2];
		final Automaton cat = new Cat().build(1);
		final Automaton mouse = new Mouse().build(1);
		animals[0] = cat;
		animals[1] = mouse;

		final State[][] stateset = new State[NUMBER_OF_ROOMS][animals.length];

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

	// Here the more complex case of num number of cats and mice
	@SuppressWarnings("unused")
    private static void testForbidderForbidStates(final int num)
		throws Exception
	{
		assert num >= 1 : "Can only handle positive number of cats and mice";

		final int NUM_CATS = num;
		final int NUM_MICE = num;
		final int NUM_ANIMALS = NUM_CATS + NUM_MICE;
		final Automaton animals[] = new Automaton[NUM_ANIMALS];	// First half of this array holds the cats, second half the mice

		final State[][] stateset = new State[NUMBER_OF_ROOMS * NUM_CATS * NUM_MICE][NUM_ANIMALS];

		for(int c = 0; c < NUM_CATS; c++)
		{
			final Automaton cat = new Cat().build(c);
			animals[c] = cat;

			for(int m = 0; m < NUM_MICE; m++)
			{
				final Automaton mouse = new Mouse().build(m);
				animals[NUM_CATS + m] = mouse;

				for(int r = 0; r < NUMBER_OF_ROOMS; r++)
				{
					// Find in each component the states that should have forbidden self-loops
					final String c_room = "c" + r;
					final String m_room = "m" + r;
					final State c_state = cat.getStateWithName(c_room);
					final State m_state = mouse.getStateWithName(m_room);

					// This is not fully worked out yet, but at least it compiles :-)
					// stateset[xxx][yyy] = c_state;
					// stateset[zzz][vvv] = m_state;
				}
			}

		}
	}
}

