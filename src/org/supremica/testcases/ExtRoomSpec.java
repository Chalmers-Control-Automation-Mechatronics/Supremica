/*
 * ExtRoomSpec.java
 *
 * Created on March 27, 2008, 5:59 AM
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
public class ExtRoomSpec {
    
    String SPEC_NAME = "Room";
    
    final String CAT_LABEL = "c";
    final String MOUSE_LABEL = "m";
    final String LEVEL_LABEL = "v";
    final String ROOM_LABEL = "r";
    final String EMPTY_LABEL = "e";
    final String LABEL_SEP = "_";
    final String LABEL_SEP2 = ".";
    
    int ROOM_ID;
    int LEVEL_ID;
    
    static int number_of_states;
    static State[][] states;
    static State emptyState;
    
//    static int number_of_events;

    static Automaton spec = null;
    
    /** Creates a new instance of RoomSpec */
    public ExtRoomSpec(int LEVEL_ID, int ROOM_ID, int num_cats, int num_levels, LabeledEvent[] CFEvents, LabeledEvent[] CBEvents, LabeledEvent[] MFEvents, LabeledEvent[] MBEvents) 
    {
        this.ROOM_ID = ROOM_ID;
        
        SPEC_NAME = SPEC_NAME+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID;
        
        State FORBIDDEN_STATE = new State("F");
        FORBIDDEN_STATE.setForbidden(true);
        
//        number_of_events = CFEvents.length + CBEvents.length + MFEvents.length + MBEvents.length;
        number_of_states = 2*num_cats + 1;
        
        spec = new Automaton(SPEC_NAME);
        spec.setType(AutomatonType.SPECIFICATION);
        
        states = new State[2][num_cats];
        
        emptyState = new State(LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP+EMPTY_LABEL);
        for(int i=0;i<num_cats;i++)
        {
            states[0][i] = new State(LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP+(i+1)+CAT_LABEL);
            states[1][i] = new State(LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP+(i+1)+MOUSE_LABEL);            
        }

        
        int init_acc;
        if(LEVEL_ID == 1 && ROOM_ID == 1)
        {
            states[0][num_cats-1].setInitial(true);
            states[0][num_cats-1].setAccepting(true);
        }
        else if(LEVEL_ID == num_levels && ROOM_ID == 5)
        {
            states[1][num_cats-1].setInitial(true);
            states[1][num_cats-1].setAccepting(true);
        }
        else
        {
            emptyState.setInitial(true);
            emptyState.setAccepting(true);
        }
        
        
        spec.addState(emptyState);
        spec.addState(FORBIDDEN_STATE);
        for (int i = 0; i < num_cats; ++i)
        {
            spec.addState(states[0][i]);
            spec.addState(states[1][i]);
        }
        
        
        for (int i = 0; i < CFEvents.length; ++i)
        {
            spec.getAlphabet().addEvent(CFEvents[i]);
            spec.addArc(new Arc(emptyState,states[0][0],CFEvents[i]));
            for(int j=0; j<num_cats-1;j++)
                spec.addArc(new Arc(states[0][j],states[0][j+1],CFEvents[i]));
            
            String event_name = CFEvents[i].getName();
            if(String.valueOf(""+event_name.charAt(event_name.length()-1)).equals(""+ROOM_ID))
            {
                for(int j=0; j<states[1].length;j++)
                {
                    State temp_state = states[1][j];
                    String state_name = temp_state.getName();
                    
                    spec.addArc(new Arc(temp_state,FORBIDDEN_STATE,CFEvents[i]));
                }
            }
        }
        
        for (int i = 0; i < CBEvents.length; ++i)
        {
            spec.getAlphabet().addEvent(CBEvents[i]);
            spec.addArc(new Arc(states[0][0],emptyState,CBEvents[i]));
            for(int j=0; j<num_cats-1;j++)
                spec.addArc(new Arc(states[0][j+1],states[0][j],CBEvents[i]));
        }
        
        for (int i = 0; i < MFEvents.length; ++i)
        {
            spec.getAlphabet().addEvent(MFEvents[i]);
            spec.addArc(new Arc(emptyState,states[1][0],MFEvents[i]));
            for(int j=0; j<num_cats-1;j++)
                spec.addArc(new Arc(states[1][j],states[1][j+1],MFEvents[i]));
            
            String event_name = MFEvents[i].getName();
            if(String.valueOf(""+event_name.charAt(event_name.length()-1)).equals(""+ROOM_ID))
            {
                for(int j=0; j<states[0].length;j++)
                {
                    State temp_state = states[0][j];
                    String state_name = temp_state.getName();
                    
                    spec.addArc(new Arc(temp_state,FORBIDDEN_STATE,MFEvents[i]));
                }
            }
        }
        
        for (int i = 0; i < MBEvents.length; ++i)
        {
            spec.getAlphabet().addEvent(MBEvents[i]);
            spec.addArc(new Arc(states[1][0],emptyState,MBEvents[i]));
            for(int j=0; j<num_cats-1;j++)
                spec.addArc(new Arc(states[1][j+1],states[1][j],MBEvents[i]));
        }
 
    }
    
    public Automaton getAutomaton()
    {
        return spec;
    }
    
}
