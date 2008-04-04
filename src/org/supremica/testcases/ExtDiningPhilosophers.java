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

class ExtEatingPhilosopher extends EatingPhilosopher
{
    int j;
    private int numInterm;
    static State[] intermStates;
    Automaton EXTphilo;

    public ExtEatingPhilosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put, int length, int numInterm)
    throws Exception
    {
        super();
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
        events[L_PUT].setControllable(l_put);
        events[R_PUT].setControllable(r_put);
        events[PUT].setControllable(true);
        events[START_EATING].setControllable(true);
        
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
            EXTphilo.addArc(new Arc(intermStates[(intermStates.length-1)], states[READY], events[R_TAKE]));
        }
        else
            EXTphilo.addArc(new Arc( states[L_UP], states[READY], events[R_TAKE]));
        
        
        EXTphilo.addArc(new Arc(states[READY], states[EAT], events[START_EATING]));
        EXTphilo.addArc(new Arc(states[EAT], states[L_DN], events[L_PUT]));
        EXTphilo.addArc(new Arc(states[L_DN], states[INIT], events[R_PUT]));
        
        // And then the right side (where the right fork is picked up and put down first)
        EXTphilo.addArc(new Arc(states[INIT], states[R_UP], events[R_TAKE]));
        EXTphilo.addArc(new Arc(states[R_UP], states[READY], events[L_TAKE]));
        EXTphilo.addArc(new Arc(states[EAT], states[R_DN], events[R_PUT]));
        EXTphilo.addArc(new Arc(states[R_DN], states[INIT], events[L_PUT]));
        
        EXTphilo.addArc(new Arc(states[EAT], states[INIT], events[PUT]));
        
        EXTphilo.removeState(states[R_DN]);
        EXTphilo.removeState(states[L_DN]);
        EXTphilo.removeState(states[R_UP]);
        
        inited = true;
    }
    
    public Automaton getPhilo()
    {
        return EXTphilo;
    }
}

public class ExtDiningPhilosophers{
    
    Project project = new Project("Extended Dining philosophers");
    Automata theAutomata = new Automata();
    final static String LABEL_SEP = "_";
    
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
        project.setComment("Dining Philosophers (parameters: n = # philosophers, k = #  intermediate states of each philosopher). \nConsider the dining philosophers problem where the number of intermediates states (after taking the fork on the left and before taking the fork on the right) may vary. This means that each philosopher, from the idles state takes the fork on his left reaching intermediate state 1, executes k-1 intermediate events reaching intermediate state k, takes his right fork entering a state where he eats, and when he is done goes back to the idle state. The uncontrollable events are \"philosopher i takes the left fork\" for i even. There are n philosophers around the table. Design a maximally permissive nonblocking supervisor.");
        
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
            Automaton currFork = fork.build(i_l_take, id, prevId(id, num), id);
            
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
