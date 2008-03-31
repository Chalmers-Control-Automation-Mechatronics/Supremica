/*
 * ExtMouseBuffer.java
 *
 * Created on March 31, 2008, 1:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import org.supremica.automata.*;

/**
 *
 * @author Sajed
 */
public class ExtMouseBuffer{
    
    Automaton buffer;
    
    /** Creates a new instance of ExtMouseBuffer */
    public ExtMouseBuffer(int number_of_mice, int nbrLevels, int LEVEL_ID, int ROOM_ID) 
    {
        MouseBuffer mb = new MouseBuffer(number_of_mice, nbrLevels, LEVEL_ID, ROOM_ID);
        LabeledEvent[] CFEvents = new CatBuffer(number_of_mice, nbrLevels, LEVEL_ID, ROOM_ID).getFEvents();
        
        buffer = new Automaton(mb.getAutomaton());
        
        State FORBIDDEN_STATE = new State("F");
        FORBIDDEN_STATE.setForbidden(true);
        buffer.addState(FORBIDDEN_STATE);
        
        for(int i=0;i<CFEvents.length;i++)
        {
            String event_name = CFEvents[i].getName();
            if(String.valueOf(""+event_name.charAt(event_name.length()-1)).equals(""+ROOM_ID))
            {
                buffer.getAlphabet().addEvent(CFEvents[i]);
                for(int j=0; j<buffer.nbrOfStates();j++)
                {
                    State temp_state = buffer.getStateWithIndex(j);
                    String state_name = temp_state.getName();

                    if(!String.valueOf(""+state_name.charAt(state_name.length()-1)).equals("0") )
                    {
                        buffer.addArc(new Arc(temp_state,FORBIDDEN_STATE,CFEvents[i]));
                    }
                    else
                        buffer.addArc(new Arc(temp_state,temp_state,CFEvents[i]));
                }

            }
            
        }
    }
    
    public Automaton getAutomaton()
    {
        return buffer;
    }
    
}
