
/** Counters.java ***************** */
package org.supremica.testcases;

import org.supremica.automata.*;

//import org.supremica.automata.execution.*;
public class Counters
	extends Automata
{
	private Project project;
	private static boolean first = true;

	public Counters(int num, int states)
	{
		project = new Project();

		if (first)
		{
			project.setComment("Independent counters. Used to produce huge state spaces " + "with no interaction. For such models, BDDs are superior " + "to traditional methods. Note however that this behaviour " + "is very uncommon in real-life models.");

			first = false;
		}

		for (int i = 0; i < num; ++i)
		{
			Automaton counter = new Automaton("Counter " + (i + 1));
			LabeledEvent event = new LabeledEvent("count:" + (i + 1));

			counter.getAlphabet().addEvent(event);

			State[] state_vector = new State[states];

			for (int s = 0; s < states; s++)
			{
				state_vector[s] = new State("" + s);

				if (s == 0)
				{
					state_vector[s].setInitial(true);
					state_vector[s].setAccepting(true);
				}

				counter.addState(state_vector[s]);
			}

			for (int s = 0; s < states; s++)
			{
				counter.addArc(new Arc(state_vector[s], state_vector[(s + 1) % states], event));
			}

			counter.setType(AutomatonType.Plant);
			project.addAutomaton(counter);
		}
	}

	public Project getProject()
	{
		return project;
	}
}
