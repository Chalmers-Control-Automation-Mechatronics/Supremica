
/** PigeonHole.java.java ***************** */

package org.supremica.testcases;

import org.supremica.automata.*;



public class PigeonHole extends Automata {
	private Project project;
	private int pigeons, holes;
	private LabeledEvent [] iEvents, lEvents;

	private final LabeledEvent invadeEvent(int p, int h) { return iEvents[ p + h * pigeons]; }
	private final LabeledEvent leaveEvent(int p, int h) { return lEvents[ p + h * pigeons]; }

	public PigeonHole(int pigeons, int holes)
	{
		this.pigeons = pigeons;
		this.holes = holes;

		project =  new Project();


		// create events:
		iEvents = new LabeledEvent[ pigeons * holes ];
		lEvents = new LabeledEvent[ pigeons * holes ];

		for (int i = 0; i < pigeons; i++) {
			for (int j = 0; j < holes; j++) {
				iEvents[ i + j * pigeons] = new LabeledEvent("I:" + (i+1) + ":" + (j + 1) );
				lEvents[ i + j * pigeons] = new LabeledEvent("L:" + (i+1) + ":" + (j + 1) );
			}
		}



		// add pigeons
		for (int i = 0; i < pigeons; i++)
		{
			Automaton p = new Automaton("P" + (i + 1));
			for(int j = 0; j < holes; j++) {
				p.getAlphabet().addEvent(invadeEvent(i,j) );
				p.getAlphabet().addEvent(leaveEvent(i,j) );
			}


			State si = new State("P:" + (i + 1) + ":fly");
			si.setInitial(true);
			p.addState(si);

			for(int j = 0; j < holes; j++)
			{
				State s = new State("P:" + (i + 1) + ":" + (j + 1));
				s.setAccepting(true);
				p.addState( s);
				p.addArc(new Arc(s, si, leaveEvent(i, j)));
				p.addArc(new Arc(si, s, invadeEvent(i, j)));
			}
			p.setType(AutomatonType.Plant);
			project.addAutomaton(p);
		}


		// add holes:
		for (int i = 0; i < holes; i++)
		{
			Automaton h = new Automaton("H:" + (i + 1) + ":empty");
			State last = null;

			for(int k = 0; k < pigeons; k++) {
				h.getAlphabet().addEvent(invadeEvent(k, i));
				h.getAlphabet().addEvent(leaveEvent(k, i));
			}

			for (int j = 0; j <= pigeons; j++)
			{
				State next = new State("H:" + (i + 1)+ ":" + ( j + 1) );
				next.setAccepting(j == 0 || j == 1);
				next.setInitial(j == 0);

				if(last != null) {
					for(int k = 0; k < pigeons; k++) {
						h.addArc(new Arc(last, next, invadeEvent(k, i)));
						h.addArc(new Arc(next, last, leaveEvent(k, i)));
					}
				}
				h.addState(next);
				last = next;

			}

			h.setType(AutomatonType.Plant);
			project.addAutomaton(h);
		}
	}


	public Project getProject()
	{
		return project;
	}
}