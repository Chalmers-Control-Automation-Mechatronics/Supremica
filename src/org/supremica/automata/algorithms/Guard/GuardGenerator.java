/*
 * GuardGenerator.java
 *
 * Created on May 7, 2008, 12:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;
import java.util.*;

import org.supremica.automata.*;

/**
 *
 * @author Sajed
 */
public class GuardGenerator {

    Automaton automaton;
    String AND = "&";
    String OR = "|";
    String nameSep = ".";
    //Compute the guards as allowed or forbidden expressions
    boolean AF;
    /** Creates a new instance of GuardGenerator */
    public GuardGenerator(final Automaton automaton, final boolean AF) {
        this.automaton = automaton;
        this.AF = AF;
    }

    public String extractGuard(final LabeledEvent event)
    {
        String output = "";
        final TreeSet<String> states = getAllowedStates(event);
        final Iterator<String> stateItr = states.iterator();
        TreeSet<String> tsAND = new TreeSet<String>();
        final TreeSet<String> tsOR = new TreeSet<String>();
        while(stateItr.hasNext())
        {
            tsAND = new TreeSet<String>();
            final String currState = ""+stateItr.next();
            final StringTokenizer st = new StringTokenizer(currState, nameSep);
            while(st.hasMoreTokens())
            {
                tsAND.add(st.nextToken());
            }
            tsOR.add(operand(AND,tsAND));
        }

        output = operand(OR,tsOR);

        return output;

    }

    public TreeSet<String> getAllowedStates(final LabeledEvent event)
    {
        final TreeSet<String> output = new TreeSet<String>();
        final Iterator<Arc> arcItr = automaton.arcIterator();
        while(arcItr.hasNext())
        {
            final Arc arc = arcItr.next();
            if(arc.getEvent().getName().equals(event.getName()))
                output.add(arc.getSource().getName());
        }
        return output;
    }

    public String operand(final String opr, final TreeSet<?> terms)
    {
        final Iterator<?> itr = terms.iterator();
        String output = "";
        if(opr.equals(AND))
            output += "(";
        while(itr.hasNext())
        {
            output += ""+itr.next();

            if(itr.hasNext())
                output += opr;
        }
        if(opr.equals(AND))
            output += ")";
        return output;
    }

}
