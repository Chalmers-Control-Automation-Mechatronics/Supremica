/*************************** Room.java **********************/
/* Class for building rooms for the CatMouse and ExtCatMouse
 * test cases. Originally this was part of CatMouse.java, but
 * that created an inheritance loop, as Room was extended by
 * Level (now named Floor due to name clash)
 * in ExtCatMouse, yet CatMouse used the LEVEL_LABEL. Fixing a
 * bug with MODEL_room_id 1 of ExtCatMouse led to refactoring // MF
**/
package org.supremica.testcases;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;

public class Room
{
	public final String ROOM_NAME = "Room";
	public final String ROOM_ID = "r";
	public final String CAT_ID = "c";
	public final String MOUSE_ID = "m";
	final static String LABEL_SEP = "_";
	final static String NAME_SEP = ":";

	private Automaton room;
	private int number_of_states;
	private int num_cats;
	private State[] states;
	private LabeledEvent[][] events;
	private Arc[][][] arcs;

	private final int RE = 0; // empty room index
	private int[] RC;
	private int[] RM;
	private int room_id;

	public Room(final int room_id, final int num_cats)
	throws Exception
	{
		this.room_id = room_id;
		this.num_cats = num_cats;
    
		this.number_of_states = 1 + 2*this.num_cats;
		this.states = new State[this.number_of_states];
    
		this.RC = new int[this.num_cats+1];
		this.RM = new int[this.num_cats+1];
		this.states[RE] = new State(ROOM_ID + room_id + "e");
    
		this.events = new LabeledEvent[this.num_cats][];
		this.arcs = new Arc[num_cats][this.num_cats][];    
    
    // First the template is built, then the user calls this.build to alter the template
    buildTemplate();
  }
  
  private void buildTemplate()
  {
		this.room = new Automaton("Room template");
		this.room.setType(AutomatonType.SPECIFICATION);
  
		for(int i = 1; i <= num_cats; i++)
		{
			RC[i] = i;
			RM[i] = i + num_cats;
      
			states[RC[i]] = new State(ROOM_ID + room_id + LABEL_SEP + i + CAT_ID);
			states[RM[i]] = new State(ROOM_ID + room_id + LABEL_SEP + i + MOUSE_ID);
		}
    
		switch(room_id)
		{
			case 0:
        buildRoom0();
				break;
			case 1:
        buildRoom1();
				break;
			case 2:
        buildRoom2();
				break;
			case 3:
        buildRoom3();
				break;
			case 4:
        buildRoom4();
				break;
		}

		for (int i = 0; i < states.length; ++i)
		{
			room.addState(states[i]);
		}

		for(int i = 0; i < num_cats; i++)
			for (int j = 0; j < events[i].length; ++j)
				room.getAlphabet().addEvent(events[i][j]);

		for(int i = 0; i < num_cats; i++)
			for(int k = 0; k < num_cats; k++)
				for(int j = 0; j < arcs[k][i].length; j++)
					room.addArc(arcs[k][i][j]);
	}

  private void buildRoom0()
  {
    states[RE].setInitial(true);
    states[RE].setAccepting(true);

    for(int i = 0; i < num_cats; i++)
    {
      events[i] = new LabeledEvent[8];

      events[i][0] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 2);
      events[i][1] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 5);
      events[i][2] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 3);
      events[i][3] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 0);

      events[i][4] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 2);
      events[i][5] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 5);
      events[i][6] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 3);
      events[i][7] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 0);

      for(int j = 0; j < events[i].length; j++)
        events[i][j].setControllable(true);

      for(int k = 1; k <= num_cats; k++)
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
  }
  
  private void buildRoom1()
  {
    states[RE].setInitial(true);
    states[RE].setAccepting(true);
    
    for(int i = 0; i < num_cats; i++)
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
      
      for(int k = 1; k <= num_cats; k++)
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
  }
  
  private void buildRoom2()
  {
    states[RC[num_cats]].setInitial(true);
    states[RC[num_cats]].setAccepting(true);
    
    for(int i = 0; i < num_cats; i++)
    {
      events[i] = new LabeledEvent[4];
      
      events[i][0] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 2);
      events[i][1] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 0);
      
      events[i][2] = new LabeledEvent(MOUSE_ID + i + LABEL_SEP + 1);
      events[i][3] = new LabeledEvent(CAT_ID + i + LABEL_SEP + 1);
      
      for(int j = 0; j < events[i].length; j++)
        events[i][j].setControllable(true);
      
      for(int k = 1; k <= num_cats; k++)
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
  }
  private void buildRoom3()
  {
    states[RE].setInitial(true);
    states[RE].setAccepting(true);

    for(int i = 0; i < num_cats; i++)
    {
      events[i] = new LabeledEvent[5];

      events[i][0] = new LabeledEvent(CAT_ID+i+LABEL_SEP+3);
      events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+4);

      events[i][2] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+4);
      events[i][3] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+5);

      events[i][4] = new LabeledEvent(CAT_ID+i+LABEL_SEP+6);

      for(int j = 0; j < (events[i].length-1); j++)
        events[i][j].setControllable(true);
      
      events[i][(events[i].length-1)].setControllable(false);

      for(int k = 1; k <= num_cats; k++)
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
  }
  
  private void buildRoom4()
  {
    states[RM[num_cats]].setInitial(true);
    states[RM[num_cats]].setAccepting(true);

    for(int i = 0; i < num_cats; i++)
    {
      events[i] = new LabeledEvent[4];
      
      events[i][0] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+4);
      events[i][1] = new LabeledEvent(CAT_ID+i+LABEL_SEP+4);
      
      events[i][2] = new LabeledEvent(CAT_ID+i+LABEL_SEP+5);
      events[i][3] = new LabeledEvent(MOUSE_ID+i+LABEL_SEP+3);
      
      for(int j = 0; j < events[i].length; j++)
        events[i][j].setControllable(true);
      
      for(int k = 1; k <= num_cats; k++)
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
  }


	public Automaton buildFloorFromTemplate(final Automaton thisRoom, final int curr_floor, final int num_floors)
	throws Exception
	{
    // Copy template for this floor
		final Automaton sm = new Automaton(thisRoom); 
		sm.setName(ROOM_NAME + NAME_SEP + room_id);
    
    // For only one floor (even with multiple cats/mice), the template is fine as is, no need to expand
    if(num_floors == 1)
    {
      return sm;
    }
    // For more floors than one, need to expand the template to one more floor
    return expandTemplate(sm, curr_floor, num_floors);
  }

  /**
   * The original renameStates() did not work correctly, since it generated a new State, which
   * left all Arcs dangling with references to the old states. Here, the old state is renamed
   * "inline" so that all Arc references remain valid and referring to the renamed state.
   * 
   * Before being renamed the state has to be removed from the the state set, and then after
   * being renamed it has to be inserted again into the state set, as the state set uses the
   * state names as keys.
  **/
  private void renameStates(final Automaton sm, final String FLOOR_LABEL, final int curr_floor)
  {
    final StateSet stateSet = sm.getStateSet();
    final java.util.Iterator<State> stateIt = sm.safeStateIterator();
    while(stateIt.hasNext())
    {
      final State state = stateIt.next();
      final String old_name = state.getName();
      
      stateSet.remove(state);
      
      state.setName(FLOOR_LABEL + curr_floor + LABEL_SEP + old_name);
      stateSet.add(state);
    }    
  }
  
  /*
   The following part should indeed be in the build() function belonging to Floor, however, then the 'waters' package
   would give an exception while drawing the automata. This is because some of the names for events and states
   created in the Room class will be modified which is apparently not accpeted by 'waters'.
  *///MF I have no clue what this comment means...
  
  private Automaton expandTemplate(final Automaton sm, final int curr_floor, final int num_floors)
  {
    final String FLOOR_LABEL = Floor.getFloorLabel();

    renameStates(sm, FLOOR_LABEL, curr_floor);
    
    if((curr_floor % (CatMouse.NUMBER_OF_ROOMS)) == room_id )
    {
      if((curr_floor+1) < num_floors && num_cats > 1)
      {
        final LabeledEvent[] lec = new LabeledEvent[num_cats];
        final LabeledEvent[] lem = new LabeledEvent[num_cats];
        for(int h = 0; h < num_cats; h++)
        {
          lec[h] = new LabeledEvent(CAT_ID + h + LABEL_SEP + FLOOR_LABEL + curr_floor + FLOOR_LABEL + (curr_floor+1));
          lem[h] = new LabeledEvent(MOUSE_ID + h + LABEL_SEP + FLOOR_LABEL + curr_floor + FLOOR_LABEL + (curr_floor+1));

          lec[h].setControllable(true);
          lem[h].setControllable(true);

          sm.getAlphabet().addEvent(lec[h]);
          sm.getAlphabet().addEvent(lem[h]);
        }
        
        for(int k = 1; k <= num_cats; k++)
        {
          final State init_state = sm.getInitialState();
          if(k == 1)
          {
            for(int h = 0; h < num_cats; h++)
            {
              sm.addArc(new Arc(init_state, sm.getStateWithIndex(RC[k]), lec[h]));
              sm.addArc(new Arc(sm.getStateWithIndex(RC[k]), init_state, lec[h]));
              // System.err.println("new Arc(" + init_state.getName() + ", " + sm.getStateWithIndex(RC[k]).getName() + ", " + lec[h].getName() + ")");
                
              if(room_id != 2 && room_id != 4)
              {
                sm.addArc(new Arc(init_state, sm.getStateWithIndex(RM[k]), lem[h]));
                sm.addArc(new Arc(sm.getStateWithIndex(RM[k]), init_state, lem[h]));
              }
             }
          }
          else
          {
            for(int h = 0; h < num_cats; h++)
            {
              if(!sm.getStateWithIndex(RC[k-1]).getName().equals(init_state.getName()) && !sm.getStateWithIndex(RC[k]).getName().equals(init_state.getName()))
              {
                sm.addArc(new Arc(sm.getStateWithIndex(RC[k-1]), sm.getStateWithIndex(RC[k]), lec[h]));
                sm.addArc(new Arc(sm.getStateWithIndex(RC[k]), sm.getStateWithIndex(RC[k-1]), lec[h]));
              }

              if(!sm.getStateWithIndex(RM[k-1]).getName().equals(init_state.getName()) && !sm.getStateWithIndex(RM[k]).getName().equals(init_state.getName()))
              {
                sm.addArc(new Arc(sm.getStateWithIndex(RM[k-1]), sm.getStateWithIndex(RM[k]), lem[h]));
                sm.addArc(new Arc(sm.getStateWithIndex(RM[k]), sm.getStateWithIndex(RM[k-1]), lem[h]));
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
		return sm;
	}

	public Automaton getAutomaton()
	{
		return room;
	}
}

