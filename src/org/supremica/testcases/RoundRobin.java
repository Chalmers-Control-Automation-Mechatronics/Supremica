/** RoundRobin.java ******************** */
package org.supremica.testcases;
import org.supremica.automata.*;

/**
 * @author Hugo
 */
public class RoundRobin
{
	private Project project;
	
	public RoundRobin(final int processes)
		throws Exception
	{
		project = new Project();
		project.setComment("Adapted from 'Compositional Minimization of Finite State Systems' by S. Graf and B. Steffen. Round robin access by token passing, the token starts at Process 1. The system is mutually but not globally nonblocking and the behaviour of the plants does not violate the specification.");

		Automaton resource = buildResource(processes);
		Automata buffers = buildBuffers(processes);
		Automata procs = buildProcesses(processes);
		Automaton spec = buildSpecification(processes);

		project.addAutomaton(resource);
		project.addAutomata(buffers);
		project.addAutomata(procs);
		project.addAutomaton(spec);
	}

	public Automaton buildResource(int processes)
	{
		Automaton resource = new Automaton("Resource");
		Alphabet alpha = resource.getAlphabet();

		State initialState = new State("Free");
		initialState.setAccepting(true);
		resource.addState(initialState);
		resource.setInitialState(initialState);

		for (int i=1; i<=processes; i++)
		{
			State state = new State("" + i);
			resource.addState(state);
			
			LabeledEvent ps = new LabeledEvent("ps" + i);
			LabeledEvent sb = new LabeledEvent("sb" + i);
			alpha.addEvent(ps);
			alpha.addEvent(sb);
			resource.addArc(new Arc(initialState, state, ps));
			resource.addArc(new Arc(state, initialState, sb));
		}

		resource.setType(AutomatonType.Plant);
		return resource;
	}

	public Automata buildBuffers(int processes)
	{
		Automata buffers = new Automata();

		for (int i=1; i<=processes; i++)
		{
			Automaton buffer = new Automaton("Buffer " + i);
			Alphabet alpha = buffer.getAlphabet();

			State initialState = new State("0");
			initialState.setAccepting(true);
			buffer.addState(initialState);
			buffer.setInitialState(initialState);

			State otherState = new State("1");
			buffer.addState(otherState);

			LabeledEvent sb = new LabeledEvent("sb" + i);
			LabeledEvent bp = new LabeledEvent("bp" + i);
			alpha.addEvent(sb);
			alpha.addEvent(bp);
			buffer.addArc(new Arc(initialState, otherState, sb));
			buffer.addArc(new Arc(otherState, initialState, bp));

			buffer.setType(AutomatonType.Plant);
			buffers.addAutomaton(buffer);
		}

		return buffers;
	}

	public Automata buildProcesses(int processes)
	{
		Automata procs = new Automata();

		for (int i=1; i<=processes; i++)
		{
			Automaton proc = new Automaton("Process " + i);
			Alphabet alpha = proc.getAlphabet();

			State initialState = new State("Idle");
			initialState.setAccepting(true);
			State state1 = new State("1");
			State state2 = new State("2");
			State state3 = new State("3");
			State state4 = new State("4");
			proc.addState(initialState);
			proc.addState(state1);
			proc.addState(state2);
			proc.addState(state3);
			proc.addState(state4);

			if (i != 1)
				proc.setInitialState(initialState);
			else
				proc.setInitialState(state1);

			LabeledEvent tk = new LabeledEvent("tk" + i);
			LabeledEvent tkNext;
			if (i < processes)
				tkNext = new LabeledEvent("tk" + (i+1));
			else
				tkNext = new LabeledEvent("tk" + 1);
			LabeledEvent ps = new LabeledEvent("ps" + i);
			LabeledEvent bp = new LabeledEvent("bp" + i);
			alpha.addEvent(tk);
			alpha.addEvent(tkNext);
			alpha.addEvent(ps);
			alpha.addEvent(bp);
			proc.addArc(new Arc(initialState, state1, tk));
			proc.addArc(new Arc(state1, state2, ps));
			proc.addArc(new Arc(state2, state3, tkNext));
			proc.addArc(new Arc(state2, state4, bp));
			proc.addArc(new Arc(state3, initialState, bp));
			proc.addArc(new Arc(state4, initialState, tkNext));

			proc.setType(AutomatonType.Plant);
			procs.addAutomaton(proc);
		}

		return procs;
	}

	public Automaton buildSpecification(int processes)
	{
		Automaton spec = new Automaton("Token passing");
		Alphabet alpha = spec.getAlphabet();
		
		State initialState = new State("P1");
		initialState.setAccepting(true);
		spec.addState(initialState);
		spec.setInitialState(initialState);
		
		State lastState = initialState;
		for (int i=2; i<=processes; i++)
		{
			State newState = new State("P" + i);
			spec.addState(newState);
			
			LabeledEvent tk = new LabeledEvent("tk" + i);
			alpha.addEvent(tk);
			spec.addArc(new Arc(lastState, newState, tk));

			lastState = newState;
		}
		LabeledEvent tk = new LabeledEvent("tk" + 1);
		alpha.addEvent(tk);
		spec.addArc(new Arc(lastState, initialState, tk));
		
		spec.setType(AutomatonType.Specification);
		return spec;
	}

	public Project getProject()
	{
		return project;
	}
}
