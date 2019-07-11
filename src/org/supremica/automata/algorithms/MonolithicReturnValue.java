package org.supremica.automata.algorithms;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;


// This one is used for doMonolithic to return two values
public class MonolithicReturnValue
{
    public Automaton automaton;
    public boolean didSomething;
    public Alphabet disabledUncontrollableEvents;    // see AutomatonSynthesizer
}
