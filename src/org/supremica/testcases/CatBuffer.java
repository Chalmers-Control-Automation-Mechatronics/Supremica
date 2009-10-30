/*
 * CatBuffer.java
 *
 * Created on March 26, 2008, 3:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


/**
 *
 * @author Sajed
 */

public class CatBuffer {
    
    final int NBR_ROOMS = 5;
    
    int ROOM_ID;
    int LEVEL_ID;
    int nbrLevels;
    
    public final String CATBUFFER_NAME;
    
    public final String CAT_LABEL = "C";
    public final String BUFFER_LABEL = "B";
    public final String ROOM_LABEL = "r";
    public final String LEVEL_LABEL = "v";
    
    final static String LABEL_SEP1 = "_";
    final static String LABEL_SEP2 = ".";
    
    static int number_of_states;
    static State[] states;
    
    static int number_of_events;
    static LabeledEvent[] events;
    
    static LabeledEvent[] fEvents;
    static LabeledEvent[] bEvents;

    static Automaton buffer = null;
    
    /** Creates a new instance of CatBuffer */
    public CatBuffer(){CATBUFFER_NAME="";}
     
    public CatBuffer(int number_of_cats, int nbrLevels, int LEVEL_ID, int ROOM_ID) 
    {
        this.LEVEL_ID = LEVEL_ID;
        this.ROOM_ID = ROOM_ID;
        this.nbrLevels = nbrLevels;
        
        CATBUFFER_NAME = BUFFER_LABEL+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+CAT_LABEL;
        
        buffer = new Automaton(CATBUFFER_NAME);
        buffer.setType(AutomatonType.PLANT);
        
        number_of_states = number_of_cats+1;
        
        if(ROOM_ID == 1 || ROOM_ID == 5)
            number_of_events = 2;
        else
            number_of_events = 4;

        if(isLevelConnector() >= 0)
        {
            number_of_events += 2;
        }
        
        states = new State[number_of_states];
        events = new LabeledEvent[number_of_events];
        
        fEvents = new LabeledEvent[number_of_events/2];
        bEvents = new LabeledEvent[number_of_events/2];
        
        for(int i=0; i<number_of_states;i++)
        {
            String temp = CATBUFFER_NAME + LABEL_SEP2 + i;
            states[i] = new State(temp);
        }
        
        if(ROOM_ID == 1 && LEVEL_ID == 1)
        {
            states[number_of_states-1].setInitial(true);
            states[number_of_states-1].setAccepting(true);
        }
        else
        {
            states[0].setInitial(true);
            states[0].setAccepting(true);
        }
        
        switch(ROOM_ID)
        {
            case 1:
                events[0] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+1+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3);
                events[1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+1);
                
                events[0].setControllable(true);
                events[1].setControllable(true);
                break;
            case 2:
                events[0] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+1);
                events[1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4);
                events[2] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2);
                events[3] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2);
                
                events[0].setControllable(true);
                events[1].setControllable(false);
                events[2].setControllable(true);
                events[3].setControllable(false);
                break;
           
            case 3:
                events[0] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2);
                events[1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4);
                events[2] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+1+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3);
                events[3] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+5+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3);
                
                for(int i=0; i<4;i++)
                    events[i].setControllable(true);
                break;
            case 4:
                events[0] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+5);
                events[1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2);
                events[2] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4);
                events[3] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+2+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4);
                
                events[0].setControllable(true);
                events[1].setControllable(false);
                events[2].setControllable(true);
                events[3].setControllable(false);
                break;
            case 5:
                events[0] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+5+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+3);
                events[1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+4+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+5);
                
                events[0].setControllable(true);
                events[1].setControllable(true);
                break;
        }
        
        if(isLevelConnector() == 0)
        {
            int NEXT_LEVEL_ID = LEVEL_ID + 1;
            events[number_of_events-2] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP1+LEVEL_LABEL+NEXT_LEVEL_ID+ROOM_LABEL+ROOM_ID);
            events[number_of_events-1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+NEXT_LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID);
            events[number_of_events-2].setControllable(true);
            events[number_of_events-1].setControllable(true);
        }
        else if(isLevelConnector() == 1)
        {
            int PREV_LEVEL_ID = LEVEL_ID - 1;
            events[number_of_events-2] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP1+LEVEL_LABEL+PREV_LEVEL_ID+ROOM_LABEL+ROOM_ID);
            events[number_of_events-1] = new LabeledEvent(CAT_LABEL+LABEL_SEP2+LEVEL_LABEL+PREV_LEVEL_ID+ROOM_LABEL+ROOM_ID+LABEL_SEP1+LEVEL_LABEL+LEVEL_ID+ROOM_LABEL+ROOM_ID);
            events[number_of_events-2].setControllable(true);
            events[number_of_events-1].setControllable(true);
        }
        
        for (int i = 0; i < number_of_states; ++i)
            buffer.addState(states[i]);
        
        for (int i = 0; i < number_of_events; ++i)
            buffer.getAlphabet().addEvent(events[i]);
        
        for (int i = 0; i < number_of_states-1; ++i)
        {
             if(isLevelConnector() >= 0)
             {
                 for (int j = 0; j < (number_of_events-2)/2; ++j)
                 {
                    buffer.addArc(new Arc(states[i+1],states[i],events[j]));
                    bEvents[j] = events[j];
                 }
                 
                 for (int j = (number_of_events-2)/2; j < number_of_events-2; ++j)
                 {
                    buffer.addArc(new Arc(states[i],states[i+1],events[j]));
                    fEvents[j-(number_of_events-2)/2] = events[j];
                 }
                 
                 buffer.addArc(new Arc(states[i+1],states[i],events[number_of_events-2]));
                 buffer.addArc(new Arc(states[i],states[i+1],events[number_of_events-1]));
                 
                 bEvents[number_of_events/2-1] = events[number_of_events-2];
                 fEvents[number_of_events/2-1] = events[number_of_events-1];
             }    
             else
             {
                 for (int j = 0; j < number_of_events/2; ++j)
                 {
                    buffer.addArc(new Arc(states[i+1],states[i],events[j]));
                    bEvents[j] = events[j];
                 }
                 
                 for (int j = number_of_events/2; j < number_of_events; ++j)
                 {
                    buffer.addArc(new Arc(states[i],states[i+1],events[j]));
                    fEvents[j-number_of_events/2] = events[j];
                 }
             }         
        }
       
    }
    
//    public CatBuffer(int number_of_cats, int nbrLevels, int LEVEL_ID, int ROOM_ID, ) 
    
    public int isLevelConnector()
    {
        
        if((LEVEL_ID-ROOM_ID)%NBR_ROOMS == 0 && LEVEL_ID+1 <= nbrLevels) 
            return 0;
        else if((LEVEL_ID-ROOM_ID-1)%NBR_ROOMS == 0 && LEVEL_ID-1 > 0 )
            return 1;
        else
            return -1;
    }
    
    public Automaton getAutomaton()
    {
        return buffer;
    }
    
    public LabeledEvent[] getFEvents()
    {
        return fEvents;
    }
    
    public LabeledEvent[] getBEvents()
    {
        return bEvents;
    }
    
}
