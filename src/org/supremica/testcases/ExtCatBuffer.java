/*
 * ExtCatBuffer.java
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
public class ExtCatBuffer{
    
    Automaton buffer;
    
    /** Creates a new instance of ExtCatBuffer */
    public ExtCatBuffer(int number_of_cats, int nbrLevels, int LEVEL_ID, int ROOM_ID) 
    {
        CatBuffer cb = new CatBuffer(number_of_cats, nbrLevels, LEVEL_ID, ROOM_ID);
        LabeledEvent[] MFEvents = new MouseBuffer(number_of_cats, nbrLevels, LEVEL_ID, ROOM_ID).getFEvents();
        
        buffer = new Automaton(cb.getAutomaton());
        
        State FORBIDDEN_STATE = new State("F");
        FORBIDDEN_STATE.setForbidden(true);
        buffer.addState(FORBIDDEN_STATE);
        
        for(int i=0;i<MFEvents.length;i++)
        {
            String event_name = MFEvents[i].getName();
            if(String.valueOf(""+event_name.charAt(event_name.length()-1)).equals(""+ROOM_ID))
            {
                buffer.getAlphabet().addEvent(MFEvents[i]);
                for(int j=0; j<buffer.nbrOfStates();j++)
                {
                    State temp_state = buffer.getStateWithIndex(j);
                    String state_name = temp_state.getName();

                    if(!String.valueOf(""+state_name.charAt(state_name.length()-1)).equals("0"))
                    {
                        buffer.addArc(new Arc(temp_state,FORBIDDEN_STATE,MFEvents[i]));
                    }
                    else
                        buffer.addArc(new Arc(temp_state,temp_state,MFEvents[i]));
                }

            }
            
        }
    }
    
    public Automaton getAutomaton()
    {
        return buffer;
    }
    
}
