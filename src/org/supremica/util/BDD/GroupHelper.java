
// TODO: this code IS SLOW, we need a prioroty queue!
//       we also need to remember from which group each automaton was taken!
package org.supremica.util.BDD;

/**
 * This class re-creates the automaton-list by merging two groups and using
 * the original ordering (according to BDDAutomaton.index)
 * (last Automata first, to allow bottom-up construction)
 */
public class GroupHelper
{
	private int size;
	private int[] tpri, cube, cubep, twave;
	private boolean[] type;
	private BDDAutomaton[] sorted_list;
	private BinaryHeap bh;

	/**
	 * create a ordred automata list from two groups
	 * TODO: use a priority queue !!
	 */
	public GroupHelper(Group g1, Group g2)
	{
		bh = new BinaryHeap();

		// "plant"
		BDDAutomaton[] tmp = g1.getMembers();

		for (int i = 0; i < g1.getSize(); i++)
		{
			bh.insert(tmp[i]);
			tmp[i].setMembership(1);
		}

		// "spec"
		tmp = g2.getMembers();

		for (int i = 0; i < g2.getSize(); i++)
		{
			bh.insert(tmp[i]);
			tmp[i].setMembership(0);
		}

		sort();
		get_type();
	}

	public GroupHelper(BDDAutomaton[] a)
	{
		bh = new BinaryHeap();

		for (int i = 0; i < a.length; i++)
		{
			bh.insert(a[i]);
			a[i].setMembership((a[i].getType() == Automaton.TYPE_PLANT)
							   ? 1
							   : 0);
		}

		sort();
		get_type();
	}

	private void get_type()
	{
		int len = sorted_list.length;

		type = new boolean[len];

		for (int i = 0; i < len; i++)
		{
			type[i] = sorted_list[i].getMembership() == 1;
		}
	}

	private void sort()
	{
		size = bh.size();
		tpri = new int[size];
		cube = new int[size];
		cubep = new int[size];
		sorted_list = new BDDAutomaton[size];

		for (int i = size - 1; i >= 0; i--)
		{    // largest first!
			BDDAutomaton a = (BDDAutomaton) bh.deleteMin().object();

			sorted_list[i] = a;
			tpri[i] = a.getTpri();
			cube[i] = a.getCube();
			cubep[i] = a.getCubep();
		}

		twave = null;    // NOT computed unless needed!
	}

	// --------------------------------------------------------------------------
	public int getSize()
	{
		return size;
	}

	public int[] getTpri()
	{
		return tpri;
	}

	public int[] getCube()
	{
		return cube;
	}

	public int[] getCubep()
	{
		return cubep;
	}

	public BDDAutomaton[] getSortedList()
	{
		return sorted_list;
	}

	/** true if its a plant */
	public boolean[] getSortedType()
	{
		return type;
	}

	/** get ~T (disjunctive T). <br>
	 * This will probably trigger a long sequence of computation first time.<br>
	 * (internal: you dont need to cleanup after this function, its done elsewhere)
	 */
	public int[] getTwave()
	{
		if (twave == null)
		{
			twave = new int[size];

			for (int i = 0; i < size; i++)
			{
				twave[i] = sorted_list[i].getDependencySet().getTwave();
			}
		}

		return twave;
	}

	// ---------------------------------------------------------------------------

	/** the code to compute Q_M is moved here.<br>
	 * this is to allow different models of marked state to be controlled from the same place
	 */
	public static int getM(JBDD m, Group spec, Group plant)
	{

		// This one is tricky:
		// if spec is empty, then we cant assume that all events in P are marked
		// because then everything is marked (there is no spec, remember?)
		// int m_all = spec.isEmpty() ? plant.getM() : spec.getM();
		// or do it as usual
		int m_all = m.and(spec.getM(), plant.getM());

		m.ref(m_all);

		return m_all;
	}
}
