

/** RandomAutomata.java ***************** */

package org.supremica.testcases;

import org.supremica.automata.*;
//import org.supremica.automata.execution.*;


public class RandomAutomata extends Automata {
	private Project project;
	private static boolean first = true;

	private class TransitionStruct {
		int s0,s1,event;
	};


	public RandomAutomata(int num, int states, int events, double dens)
	{
		project =  new Project();

		if(first)
			project.setComment("Composition of random automata tend to have very few states (often " + 
							   "only one). This is very favourable for traditional algorithms in " + 
							   "contrast to the symbolic ones (e.g. BDD-based). In this sense, the " + 
							   "'Random automata' testcase is the exact opposite to the 'Counters' " + 
							   "test case."
				);

		first = false;

		if(dens > 1.0)
		{
			dens = 1.0;
		}

		LabeledEvent [] events_vector = new LabeledEvent[events];
		boolean [] events_used = new boolean[events];

		for(int i = 0; i < events; i++)
		{
			events_vector[i] = new 	LabeledEvent("e"+ (i+1) );
			events_vector[i].setControllable(Math.random() > 0.4); // 40% uncontrollables ?
		}

		int tcount = (int)((double)states * events * dens);
		TransitionStruct [] transitions = new TransitionStruct[tcount];
		for(int i = 0; i < tcount; i++)
		{
			transitions[i] = new TransitionStruct();
		}

		for (int i = 0; i < num; ++i)
		{
			Automaton random = new Automaton("random " + (i + 1));

			State [] state_vector = new State[states];
			for(int s = 0; s < states; s++)
			{
				state_vector[s] = new State("q"+s);
				if(s == 0)
				{
					state_vector[s].setInitial(true);
					state_vector[s].setAccepting(true);
				}
				random.addState(state_vector[s]);
			}


			computeRandomTransitions(transitions, states, events);

			for(int j = 0; j < events; j++)
			{
				events_used[j] = false;
			}

			for(int j = 0; j < transitions.length; j++) {
				events_used[ transitions[j].event] = true;
			}

			for(int j = 0; j < events; j++)
			{
				if(events_used[j])
				{
					random.getAlphabet().addEvent(events_vector[j] );
				}
			}

			for(int s = 0; s < transitions.length; s++)
			{
				//System.out.println(""+ transitions[s].s0 + ", " +  transitions[s].event + " -> " + transitions[s].s1);
				random.addArc(
					new Arc(state_vector[transitions[s].s0],
							state_vector[transitions[s].s1],
							events_vector[transitions[s].event]
							)
						);
			}

			random.setType(AutomatonType.Plant);
			project.addAutomaton(random);
		}

	}

	// this is highly inefficient :(
	private void computeRandomTransitions(TransitionStruct []ts, int states, int events)
	{
		int count = 0;
		while(count < ts.length)
		{
			int s0 = (int)(Math.random() * states);
			int s1 = (int)(Math.random() * states);
			int e = (int)(Math.random() * events);

			if(!exists(ts, count, s0,s1,e))
			{
				ts[count].s0 = s0;
				ts[count].s1 = s1;
				ts[count].event = e;
				count++;
			}

		}
	}

	// yet more lame code...
	private boolean exists(TransitionStruct []ts, int size, int s0, int s1, int event) {
		for(int i = 0; i < size; i++)
		{
			if(ts[i].s0 == s0 && ts[i].event == event /* IT IS DETEMINISTIC!!  &&  ts[i].s1 == s1  */)
			{
				return true;
			}
		}

		return false;
	}


	public Project getProject()
	{
		return project;
	}
}
