
/** Counters.java ***************** */
package org.supremica.testcases;

import org.supremica.automata.*;

//import org.supremica.automata.execution.*;
public class Counters
    extends Automata
{
    private static final long serialVersionUID = 1L;

    private Project project;

    public Counters(int num, int states)
    {
        project = new Project("Counters");
        project.setComment("Independent counters. Used to produce huge state spaces " +
                "with no interaction.");

        for (int i = 0; i < num; ++i)
        {
            Automaton counter = new Automaton("Counter:" + (i + 1));
            LabeledEvent event = new LabeledEvent("count:" + (i + 1));

            counter.getAlphabet().addEvent(event);

            State[] state_vector = new State[states];

            for (int s = 0; s < states; s++)
            {
                state_vector[s] = new State("q" + s);
                counter.addState(state_vector[s]);
            }

			state_vector[0].setInitial(true);
			state_vector[0].setAccepting(true);

            for (int s = 0; s < states; s++)
            {
                counter.addArc(new Arc(state_vector[s], state_vector[(s + 1) % states], event));
            }

            counter.setType(AutomatonType.PLANT);
            // System.err.println(counter.toCode());
            project.addAutomaton(counter);
        }
    }

    public Project getProject()
    {
        return project;
    }
}
