/*
 * ExtDiningPhilosophers.java
 *
 * Created on February 21, 2008, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import java.util.Iterator;
import org.omg.CORBA.portable.IDLEntity;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.execution.*;
import uk.ac.ic.doc.scenebeans.Null;

/**
 *
 * @author Sajed
 */

class ExtEatingPhilosopher
{
    int j;
    private int numInterm;
    static State[] intermStates;
    Automaton EXTphilo;
    
    public final String PHILO_NAME = "Philo";
    
    static int nbrOfStates = 3;
    static State[] states = new State[nbrOfStates];
    // indices into states[]
    final static int INIT = 0;
    final static int L_UP = 1;
    final static int EAT = 2;
    
    static int number_of_events = 4;
    static LabeledEvent[] events = new LabeledEvent[number_of_events];

    // indicies into events[]
    final static int L_TAKE = 0;
    final static int R_TAKE = 1;
    final static int INTERM_EVENT = 2;
    final static int PUT = 3;
    final static String LABEL_SEP = ".";
    
    // note, must be the same in both Philosopher and Fork
    final static String NAME_SEP = ":";
    
    // Need not be the same everywhere
    static Automaton philo = null;
    
    static boolean inited = false;
    
    int length;

    public ExtEatingPhilosopher()
    {
         states[0] = new State("think");
         states[1] = new State("lu");
         states[2] = new State("eat");

         events[0] = new LabeledEvent("L_take");    // pick up left
         events[1] = new LabeledEvent("R_take");    // pick up right
         events[2] = new LabeledEvent("Interm_event");
         events[3] = new LabeledEvent("Put");
        
    }
    
    public ExtEatingPhilosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put, int length, int numInterm)
    throws Exception
    {
        this();
        this.length = length;
        this.numInterm = numInterm;
        
        intermStates = new State[(numInterm-1)];
        if(numInterm > 1)
        {
            for(j=0; j<(numInterm-1); j++)
            {
                intermStates[j] = new State("intermediate_"+(j+1));
            }
        }


        // Here we create the "template" automaton, EXTphilo
        EXTphilo = new Automaton("EXTPhilo template");
        EXTphilo.setType(AutomatonType.PLANT);

        states[0].setInitial(true);
        states[0].setAccepting(true);
        for (int i = 0; i < states.length; ++i)
        {
            EXTphilo.addState(states[i]);
        }
        
        for (j = 0; j < intermStates.length; ++j)
        {
            EXTphilo.addState(intermStates[j]);
        }
        
        // Now the events, these should be (re)named uniquely for each philosopher
        // (each fork-pair, actually)
        events[L_TAKE].setControllable(l_take);
        events[R_TAKE].setControllable(r_take);
        events[PUT].setControllable(true); 
        events[INTERM_EVENT].setControllable(true);
        
        for (int i = 0; i < events.length; ++i)
        {
            EXTphilo.getAlphabet().addEvent(events[i]);
        }
        
        // And finally the arcs - first the left side (where the left is picked up
        // and put down first)
        
        EXTphilo.addArc(new Arc(states[INIT], states[L_UP], events[L_TAKE]));
        if(numInterm > 1)
        {
            EXTphilo.addArc(new Arc(states[L_UP], intermStates[0], events[INTERM_EVENT]));
        
            for (j = 0; j < (intermStates.length-1); ++j)
            {
                EXTphilo.addArc(new Arc(intermStates[j], intermStates[j+1], events[INTERM_EVENT]));
            }
            EXTphilo.addArc(new Arc(intermStates[(intermStates.length-1)], states[EAT], events[R_TAKE]));
        }
        else
            EXTphilo.addArc(new Arc( states[L_UP], states[EAT], events[R_TAKE]));
        
        EXTphilo.addArc(new Arc(states[EAT], states[INIT], events[PUT]));
        
        inited = true;
    }
     public void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);        
    }
     
    public Automaton build(boolean i_l_take, Automaton spec_philo, int id, int l_fork, int r_fork)
    throws Exception
    {
        //logger.info(philo.getAlphabet().toDebugString());
        //AutomataToXML builder = new AutomataToXML(philo);
        //logger.debug(builder.serialize());
        // deep copy, I hope
        
        Automaton sm = new Automaton(spec_philo);
        sm.setName(PHILO_NAME + NAME_SEP + pad(id));
        
        // adjust the event names according to l_fork and r_fork
        // L_take becomes take<id>.<l_fork>
        // R_take becomes take<id>.<r_fork>
        // L_put becomes put<id>.<l_fork>
        // R_put becomes put<id>.<r_fork>
        renameEvent(sm, L_TAKE, "take" + pad(id)+ LABEL_SEP +pad(l_fork));
        renameEvent(sm, R_TAKE, "take" + pad(id)+ LABEL_SEP +pad(r_fork));
        renameEvent(sm, PUT, "put" + pad(id));
        
//        renameEvent(sm, START_EATING, "start_eating" + pad(id));
        renameEvent(sm, INTERM_EVENT, "intermediate" + pad(id));
        
        if(i_l_take)
        {   
            if((id%2)==0)
            {
                sm.getAlphabet().getEvent("take" + id + LABEL_SEP + id).setControllable(false);
            }
        }
        
        // Used Automaton::replaceEvent, so no need to rehash
        // // must rehash since we've changed the label (that's the way it works (unfortunately))
        // alpha.rehash();
        //AutomataToXML builder = new AutomataToXML(sm);
        //logger.debug(builder.serialize());
        return sm;
    }
    
    public String pad(int num)
    {
        String returnValue = "" + num;
//        while (returnValue.length() < length)
//            returnValue = "0" + returnValue;
        return returnValue;
    }
    
    public Automaton getPhilo()
    {
        return EXTphilo;
    }
}

public class ExtDiningPhilosophers{
    
    Project project = new Project("Extended Dining philosophers");
    Automata theAutomata = new Automata();
    final static String LABEL_SEP = ".";
    
    int nextId(int id, int modulo)
    {
        int nxt = id + 1;
        
        if (nxt > modulo)
        {
            return nxt - modulo;
        }
        else
        {
            return nxt;
        }
    }
    
    int prevId(int id, int modulo)
    {
        int nxt = id - 1;
        
        if (nxt <= 0)
        {
            return modulo;
        }
        else
        {
            return nxt;
        }
    }
    
    /** Creates a new instance of ExtDiningPhilosophers */
    public ExtDiningPhilosophers(boolean i_l_take, int num, int numInterm, boolean l_take, boolean r_take,
        boolean l_put, boolean r_put, boolean animation, boolean forkmemory)
        throws Exception
    {        
        // Add comment
        project.setComment("Extended Dining Philosophers (parameters: n = # philosophers, k = #  intermediate states of each philosopher). \nConsider the dining philosophers problem where the number of intermediates states (after taking the fork on the left and before taking the fork on the right) may vary. This means that each philosopher, from the idles state takes the fork on his left reaching intermediate state 1, executes k-1 intermediate events reaching intermediate state k, takes his right fork entering a state where he eats, and when he is done goes back to the idle state. The uncontrollable events are \"philosopher i takes the left fork\" for i even. There are n philosophers around the table. Design a maximally permissive nonblocking supervisor.");
        
        int idLength = ("" + num).length();
        int intermLength = ("" + numInterm).length();
        
        // First the philosphers
        // Philosopher philo = new Philosopher(l_take, r_take, l_put, r_put);
        ExtEatingPhilosopher philo = new ExtEatingPhilosopher(l_take, r_take, l_put, r_put, idLength, numInterm);
 
        for (int i = 0; i < num; ++i)
        {
            int id = i + 1;
            
            Automaton currPhil = philo.build(i_l_take, philo.getPhilo(), id, id, nextId(id, num));
                      
/*            if(i_l_take)
            {   
                if((id%2)==0)
                {
                    currPhil.getAlphabet().getEvent("take" + id + LABEL_SEP + id).setControllable(false);
                }
            }
 */           
            // id's are from 1...n
            project.addAutomaton(currPhil);
            theAutomata.addAutomaton(currPhil);
            
            // To his right a philo has fork #id, and to his left is fork #id-1
        }
        
        // Next the forks aka chopsticks
        ChopstickBuilder fork;
        if (forkmemory)
        {
            fork = new MemoryChopstick(l_take, r_take, l_put, r_put, idLength);
        }
        else
        {
            fork = new Chopstick(l_take, r_take, l_put, r_put, idLength);
        }
        
        for (int i = 0; i < num; ++i)
        {
            int id = i + 1;
            
            // id's are from 1...n
            Automaton currFork = fork.build(true, i_l_take, id, prevId(id, num), id);
            
/*            if(i_l_take)
            {
                if((id%2)==0)
                {
                    currFork.getAlphabet().getEvent("take" + id + LABEL_SEP + id).setControllable(false);
                }
            }
*/            
            project.addAutomaton(currFork);
            theAutomata.addAutomaton(currFork);
            
            // To its right a fork has philo #id, and to its left philo #id-1
        }
        
    }
    
    public void changeControllability(Automaton a, String label, boolean new_control)
    {
        Alphabet alpha = a.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(label);
        LabeledEvent ev_new = new LabeledEvent(ev_old);
        ev_new.setControllable(new_control);
        a.replaceEvent(ev_old, ev_new);
    }
    
    public Automata getAutomata()
    {
        return theAutomata;
    }
    
    public Project getProject()
    {
        return project;
    }
}
