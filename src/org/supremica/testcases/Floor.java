/*********************** Floor.java ***********************/
/* This was factored out from ExtCatMouse.java during bug
 * hunting. For ExtCatMouse MODEL_ID 1, there was a bug where
 * the resulting automata could not be copied due to the from
 * and to states of arcs were somehow missing. See also Room.java
 *
 * This class was originally called Level, but that clashed
 * Also, Level inherited from Room, for no reason whatsoever
**/
package org.supremica.testcases;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;

public class Floor // extends Room
{
  private final Automata floor;
  private final int floor_id;
  private final static String FLOOR_LABEL = "L";

  public Floor(final int floor_id, final int num_cats, final int num_floors)
  throws Exception
  {
    this.floor = new Automata();
    this.floor_id = floor_id;
    
    for (int room_id = 0; room_id < CatMouse.NUMBER_OF_ROOMS; ++room_id)
    {
      final Room room = new Room(room_id, num_cats);
      final Automaton room_template = room.getAutomaton();

      final Automaton room_automaton = room.buildFloorFromTemplate(room_template, floor_id, num_floors);
      floor.addAutomaton(room_automaton);
      // System.err.println("Sanity check for "+ room_automaton.getName());
      // room_automaton.sanityCheck();
    }
  }

  public Automata buildFloor()
	throws Exception
  {
    final Automata sm = new Automata(this.floor);
    final Automaton[] rooms = new Automaton[CatMouse.NUMBER_OF_ROOMS];
    
    for(int room_id = 0; room_id < CatMouse.NUMBER_OF_ROOMS; room_id++)
    {
      rooms[room_id] = sm.getAutomatonAt(room_id);
      sm.renameAutomaton(rooms[room_id], FLOOR_LABEL + this.floor_id + Room.NAME_SEP + rooms[room_id].getName());
    }
    
    return sm;
  }

  public Automata getAutomata()
  {
    return floor;
  }

  public static String getFloorLabel()
  {
    return FLOOR_LABEL;
  }
}