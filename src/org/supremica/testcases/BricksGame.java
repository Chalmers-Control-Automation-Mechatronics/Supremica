
/********************* BricksGame.java *********************/
package org.supremica.testcases;



import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Event;
import org.supremica.automata.Arc;


class BrickBuilder
{

	private final State[][] shared_states;
	private final int rows;
	private final int cols;
	private Automaton automaton = null;
	private int number;		// this bricks number

	private void addRight(int r, int c, int num, boolean reverse)
		throws Exception
	{

		State source = automaton.getState(shared_states[r][c]);
		State dest = automaton.getState(shared_states[r][c + 1]);		// to the right
		Event src_dst = reverse
						? new Event(dest.getId() + source.getId() + num)
						: new Event(source.getId() + dest.getId() + num);
		Event dst_src = reverse
						? new Event(source.getId() + dest.getId() + num)
						: new Event(dest.getId() + source.getId() + num);

		automaton.getAlphabet().addEvent(src_dst);
		automaton.getAlphabet().addEvent(dst_src);
		automaton.addArc(new Arc(source, dest, src_dst.getLabel()));
		automaton.addArc(new Arc(dest, source, dst_src.getLabel()));
	}

	private void addDown(int r, int c, int num, boolean reverse)
		throws Exception
	{

		State source = automaton.getState(shared_states[r][c]);
		State dest = automaton.getState(shared_states[r + 1][c]);		// downwards
		Event src_dst = reverse
						? new Event(dest.getId() + source.getId() + num)
						: new Event(source.getId() + dest.getId() + num);
		Event dst_src = reverse
						? new Event(source.getId() + dest.getId() + num)
						: new Event(dest.getId() + source.getId() + num);

		automaton.getAlphabet().addEvent(src_dst);
		automaton.getAlphabet().addEvent(dst_src);
		automaton.addArc(new Arc(source, dest, src_dst.getLabel()));
		automaton.addArc(new Arc(dest, source, dst_src.getLabel()));
	}

	public BrickBuilder(final int r, final int c)
		throws Exception
	{

		rows = r;
		cols = c;
		shared_states = new State[rows + 1][cols + 1];		// the +1 is a quick fixx to set boundaries

		// Create the shared states - all states are named the same between automata (even the zero one)
		// Create them just once, then share them (will this work in practice?)
		for (int i = 1; i <= rows; ++i)
		{
			for (int j = 1; j <= cols; ++j)
			{
				shared_states[i][j] = new State(Integer.toString(i) + j + ".");
			}
		}
	}

	public Automaton buildBrick(final int r, final int c)
		throws Exception
	{

		number = (r - 1) * cols + c;
		automaton = new Automaton("Brick" + number);

		automaton.setType(AutomatonType.Plant);
		shared_states[r][c].setInitial(true);		// the initial state for this automaton

		// Now add these states to this automaton
		for (int i = 1; i <= rows; ++i)
		{
			for (int j = 1; j <= cols; ++j)
			{
				automaton.addState(new State(shared_states[i][j]));		// we must copy, mustn't we
			}
		}

		// Now for the transitions - and in the meantime create the events
		// Since each transition has a unique event, there's a 1-2-1 mapping
		for (int i = 1; i < rows; ++i)
		{
			for (int j = 1; j < cols; ++j)		// goes only to cols-1
			{
				addRight(i, j, number, false);
				addDown(i, j, number, false);
			}

			// In the final column of this row, there's no states to the right
			addDown(i, cols, number, false);
		}

		// In the final row there's no down-states, only right-states (until we're at the last column)
		for (int j = 1; j < cols; ++j)
		{
			addRight(rows, j, number, false);
		}

		shared_states[r][c].setInitial(false);

		return automaton;
	}

	public Automaton zeroBrick()
		throws Exception
	{

		number = 0;
		automaton = new Automaton("Brick0");

		automaton.setType(AutomatonType.Plant);
		shared_states[rows][cols].setInitial(true);		// the initial state for this automaton

		// Now add these states to this automaton
		for (int i = 1; i <= rows; ++i)
		{
			for (int j = 1; j <= cols; ++j)
			{
				automaton.addState(new State(shared_states[i][j]));		// we must copy, mustn't we
			}
		}

		// Now for the transitions - and in the meantime create the events
		// Since each transition has a unique event, there's a 1-2-1 mapping
		for (int i = 1; i < rows; ++i)
		{
			for (int j = 1; j < cols; ++j)		// goes only to cols-1
			{
				for (int k = 1; k < rows * cols; ++k)
				{
					addRight(i, j, k, true);
					addDown(i, j, k, true);
				}
			}

			// In the final column of this row, there's no states to the right
			for (int k = 1; k < rows * cols; ++k)
			{
				addDown(i, cols, k, true);
			}
		}

		// In the final row there's no down-states, only right-states (until we're at the last column)
		for (int j = 1; j < cols; ++j)
		{
			for (int k = 1; k < rows * cols; ++k)
			{
				addRight(rows, j, k, true);
			}
		}

		shared_states[rows][cols].setInitial(false);

		return automaton;
	}
}

public class BricksGame
{

	private Automata automata = new Automata();

	public BricksGame(int rows, int cols)
		throws Exception
	{

		BrickBuilder builder = new BrickBuilder(rows, cols);

		// Create rows*cols-1 utomata. Note cols-1, the last one is special (the 0-brick)
		Automaton sm = null;

		for (int i = 1; i < rows; ++i)
		{
			for (int j = 1; j < cols; ++j)
			{
				sm = builder.buildBrick(i, j);

				automata.addAutomaton(sm);
			}

			sm = builder.buildBrick(i, cols);

			automata.addAutomaton(sm);
		}

		for (int j = 1; j < cols; ++j)
		{
			sm = builder.buildBrick(rows, j);

			automata.addAutomaton(sm);
		}

		automata.addAutomaton(builder.zeroBrick());
	}

	public Automata getAutomata()
	{
		return automata;
	}
}
