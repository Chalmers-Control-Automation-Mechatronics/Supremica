package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

/** show the number of nodes in the trees */
public class BDDNodeDiffGrow
	extends GrowFrame
{
	private BDDAutomata manager;
	private int last = 0;

	public BDDNodeDiffGrow(BDDAutomata manager, String title)
	{
		super("NODEdiff/" + title);

		this.manager = manager;
	}

	public void add(int bdd)
	{
		int curr = manager.nodeCount(bdd);

		super.add(curr - last);

		last = curr;
	}
}
