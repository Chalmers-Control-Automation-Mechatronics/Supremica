package org.supremica.util.BDD;

public class BDDStateTreeExplorer
{
	private BDDAutomata automata;
	private BDDAutomaton[] av;
	private int size;
	private int bdd_zero;

	public BDDStateTreeExplorer(BDDAutomata automata)
	{
		this.automata = automata;
		this.av = automata.getAutomataVector();
		this.size = automata.getSize();
		this.bdd_zero = automata.getZero();
	}

	// BDD of S, nothing else!
	public IncompleteStateTree getCompleteStateTree(int bdd)
	{
		IncompleteStateTree tree = new IncompleteStateTree();

		if ((bdd != bdd_zero) && (size > 0))
		{
			extract_states_rec(bdd, tree.getRoot(), 0);
		}

		return tree;
	}

	private void extract_states_rec(int bdd, IncompleteStateTree.StateTreeNode node, int level)
	{
		if (level >= size)
		{
			return;
		}

		State[] states = av[level].getStates();

		node.setAutomaton(av[level].getName());

		int cube = av[level].getCube();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = automata.relProd(bdd, states[i].bdd_s, cube);

			if (tmp != bdd_zero)
			{
				IncompleteStateTree.StateTreeNode next = node.insert(states[i].name_id);

				extract_states_rec(tmp, next, level + 1);
			}

			automata.deref(tmp);
		}
	}
}
