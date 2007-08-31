/**
 * this class hold basic functions for building EFA
 *
 */
package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.io.File;
import java.util.LinkedList;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;

class EFAbase
{
    
    /**
     *
     * list of states to store added state
     * in ExtendedAutomata
     *
     */
    private static LinkedList<String> states;
    
    /**
     * list of events to store added event
     * in ExtendedAutomaton
     */
    private static LinkedList<String> events;
    
    /**
     * automata
     */
    private static ExtendedAutomaton automaton;
    
    /**
     *module
     */
    private static ExtendedAutomata automata;
    
    protected static void init(String machine, String comment)
    {
        
        states = new LinkedList<String>();
        events = new LinkedList<String>();
        
        automata = new ExtendedAutomata(comment, true);
        automaton = new ExtendedAutomaton(machine,automata, true);
        
        //add first initialstate
        automaton.addState("s0",true,true);
        states.add("s0");
        automata.addAutomaton(automaton);
    }
    
    /**
     * write ExtendedAutomata to water file
     * @param f
     */
    protected static void writeToFile(File f)
    {
        automata.writeToFile(f);
    }
    
    /**
     * Add new state String s to automaton.
     * if state s already present or null do
     * nothing.
     * @param s
     */
    protected static void addState(String s)
    {
        
        //check indata
        if(s == null || s.length() == 0)
        {
            return;
        }
        
        //check if we already added this state
        if(!states.isEmpty())
        {
            if(states.contains(s))
            {
                return;
            }
        }
        
        automaton.addState(s);
        states.add(s);
        
        //debugg
        System.out.println("added state " + s);
        //debugg
    }
    
    /**
     * add event String e to automata (module).
     * if e already exist or null does nothing.
     * @param e
     */
    protected static void addEvent(String e)
    {
        
        //check indata
        if(e == null || e.length() == 0)
        {
            return;
        }
        
        //check if event allready exist
        if(events.contains(e))
        {
            return;
        }
        
        //add new event to automata
        automata.addEvent(e,"controllable");
        events.add(e);
        
        //debugg
        System.out.println("added event " + e);
        //debugg
    }
    
    /**
     * add transitin betwen two states to and from whit event label
     * and guard and action.
     *
     * if to and from not present in automata
     * they will be added.
     *
     * If label not present in automata it will
     * be added.
     *
     * no regexp are alowed in label because <String>.split(";")
     * is used.
     *
     * @param from
     * @param to
     * @param label
     * @param guard
     * @param action
     */
    protected static void addTransition(String from, String to,
        String label,
        String guard, String action)
    {
        
        //debugg
        System.out.println();
        System.out.println("addTransition( "+from+ ", "+to+", "+label+", "+guard+", "+action+" )");
        //debugg
        
        //add unknown items
        if(!stateExist(from))
        {
            addState(from);
        }
        
        if(!stateExist(to))
        {
            addState(to);
        }
        
        if(!eventExist(label))
        {
            String[] tmp = label.split(";");
            if(tmp != null)
            {
                for(int i = 0; i < tmp.length; i++)
                {
                    addEvent(tmp[i]);
                }
            }
        }
        
        //events ned ; to be correct parsed in
        //ExtendedAutomaton addTransition
        if(!label.equals("") && !label.endsWith(";"))
        {
            label = label + ";";
        }
        
        if(!action.equals("") && !action.endsWith(";"))
        {
            action = action + ";";
        }
        
        automaton.addTransition(from, to, label, guard, action);
        
        //debugg
        System.out.println("addTransition done");
        System.out.println();
        //debugg
    }
    
    /**
     * Add a new unique state and return its name
     * as a String.
     * @return a new state with a unique name.
     */
    protected static String newUniqueState()
    {
        String s = nextState();
        addState(s);
        return s;
    }
    
    /**
     * returns the name of next state
     * returned by
     * @return next state.
     */
    protected static String nextState()
    {
        if(states == null)
        {
            return "s"+0;
        }
        return "s"+states.size();
    }
    
    /**
     * return last state in list
     * @return last state in list
     */
    protected static String lastState()
    {
        return states.getLast();
    }
    
    /**
     * return true if state already in list of states
     * @param state
     * @return true if state exists.
     */
    protected static boolean stateExist(String state)
    {
        return states.contains(state);
    }
    
    /**
     * return true if event already in list of events
     * @param event
     * @return
     */
    protected static boolean eventExist(String event)
    {
        return events.contains(event);
    }
}
