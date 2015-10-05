package org.supremica.external.interfaceExample;

import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

public class InterfaceExample
{
    public static void createAutomaton()
    {
		Automata theAutomata = new Automata();
		Automaton theAutomaton = new Automaton("theAutomaton");
		theAutomaton.setType(AutomatonType.PLANT);
		theAutomata.addAutomaton(theAutomaton);

		// Get alphabet
		Alphabet theAlphabet = theAutomaton.getAlphabet();

		// Create events
		LabeledEvent e1 = new LabeledEvent("e1");
		e1.setControllable(false);
		LabeledEvent e2 = new LabeledEvent("e2");

		// Add events to the alphabet
		theAlphabet.addEvent(e1);
		theAlphabet.addEvent(e2);

		// Create states
		State q1 = new State("q1");
		q1.setInitial(true);
		State q2 = new State("q2");
		q2.setAccepting(true);
		q2.setCost(3);

		// Add states to the automaton
		theAutomaton.addState(q1);
		theAutomaton.addState(q2); 

		// Add transitions
		theAutomaton.addArc(new Arc(q1, q2, e1));
		theAutomaton.addArc(new Arc(q2, q1, e2));

		// Output the automata as XML
		AutomataToXML serializer = new AutomataToXML(theAutomata);

		serializer.serialize(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    public static void projectBuilder(File file)
        throws Exception
    {
        ProjectBuildFromXML builder = new ProjectBuildFromXML();
        Project theProject = builder.build(file);

        // Iterate over all automata in the project
        for (Automaton currAutomaton : theProject)
        {
            System.out.println("Automaton: " + currAutomaton.getName());

            // Iterate over all events in currAutomaton
            for (LabeledEvent currEvent : currAutomaton.getAlphabet())
            {
                System.out.println("Event: " + currEvent.getName());
            }
            // Iterate over all states in currAutomaton
            for (State currState : currAutomaton)
            {
                System.out.println("State: " + currState.getName());
                for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
                {
                    Arc currArc = arcIt.next();
                    System.out.println("\t toState: " + currArc.getTarget().getName() + " Event: " + currArc.getLabel());
                }
            }
        }  
        
        
    }

	public static void main(String[] args)
        throws Exception
    {
        //createAutomaton();
        projectBuilder(new File("C:\\Documents and Settings\\Knut\\My Documents\\research\\software\\Supremica\\examples\\scanvaccum_org.xml"));
	}
}