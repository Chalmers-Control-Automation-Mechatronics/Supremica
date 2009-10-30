
/** PigeonHole.java.java ***************** */
package org.supremica.testcases;

import org.supremica.automata.*;

public class PigeonHole
{
	private Project project;
	@SuppressWarnings("unused")
	private int pigeons, holes;
	private LabeledEvent[] iEvents, lEvents;
	@SuppressWarnings("unused")
	private static boolean first = true;

	private final LabeledEvent invadeEvent(int p, int h)
	{
		return iEvents[p + h * pigeons];
	}

	private final LabeledEvent leaveEvent(int p, int h)
	{
		return lEvents[p + h * pigeons];
	}

	public PigeonHole(int pigeons, int holes)
	{
		this.pigeons = pigeons;
		this.holes = holes;
		project = new Project("Pigeon-hole principle");

		// if(first)
		project.setComment("The Pigeon-Hole Principle (PHP): having n pigeons in m holes, if m < n, " + "then there is at least one hole that contains more than one pigeon. " + "Note that similar situations often arise in reality, for example in a batch system with large buffers. " + "In this testcase model, the (globally) marked states represent states where all the pigeons are inside the " + "holes, but there is no more than one pigeon in each hole. Clearly, if m < n, this can not be achieved  " + "and the system is blocking. Here, we have choosen a model where each Pigeon and Hole have unique " + "identies, which makes the problem very hard. " + "An anonymous Petri-Net model on the other hand, would be extremly easy to traverse. " + "To see why this problem is so hard, refer to 'The Intractability of Resolution' by A. Haken. ");

		// first = false;
		// create events:
		iEvents = new LabeledEvent[pigeons * holes];
		lEvents = new LabeledEvent[pigeons * holes];

		for (int i = 0; i < pigeons; i++)
		{
			for (int j = 0; j < holes; j++)
			{
				iEvents[i + j * pigeons] = new LabeledEvent("In:P" + (i + 1) + ":H" + (j + 1));
				lEvents[i + j * pigeons] = new LabeledEvent("Out:P" + (i + 1) + ":H" + (j + 1));
			}
		}

		// add pigeons
		for (int i = 0; i < pigeons; i++)
		{
			Automaton p = new Automaton("P" + (i + 1));

			for (int j = 0; j < holes; j++)
			{
				p.getAlphabet().addEvent(invadeEvent(i, j));
				p.getAlphabet().addEvent(leaveEvent(i, j));
			}

			State si = new State("P" + (i + 1) + ":fly");

			si.setInitial(true);
			p.addState(si);

			for (int j = 0; j < holes; j++)
			{
				State s = new State("P" + (i + 1) + ":H" + (j + 1));

				s.setAccepting(true);
				p.addState(s);
				p.addArc(new Arc(s, si, leaveEvent(i, j)));
				p.addArc(new Arc(si, s, invadeEvent(i, j)));
			}

			p.setType(AutomatonType.PLANT);
			project.addAutomaton(p);
		}

		// add holes:
		for (int i = 0; i < holes; i++)
		{
			Automaton h = new Automaton("H" + (i + 1));
			State last = null;

			for (int k = 0; k < pigeons; k++)
			{
				h.getAlphabet().addEvent(invadeEvent(k, i));
				h.getAlphabet().addEvent(leaveEvent(k, i));
			}

			for (int j = 0; j <= pigeons; j++)
			{
				State next = new State("H" + (i + 1) + ":" + (j));

				next.setAccepting((j == 0) || (j == 1));
				next.setInitial(j == 0);

				if (last != null)
				{
					for (int k = 0; k < pigeons; k++)
					{
						h.addArc(new Arc(last, next, invadeEvent(k, i)));
						h.addArc(new Arc(next, last, leaveEvent(k, i)));
					}
				}

				h.addState(next);

				last = next;
			}

			h.setType(AutomatonType.SPECIFICATION);
			project.addAutomaton(h);
		}
	}

	public Project getProject()
	{
		return project;
	}
}
