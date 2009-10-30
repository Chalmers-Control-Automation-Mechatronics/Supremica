package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

/** show the [1000 * log_10] number of minterms (i.e. the size of SAT) for each tree */
public class BDDSATLogGrow
	extends GrowFrame
{
    private static final long serialVersionUID = 1L;

    private BDDAutomata manager;
	private double log10 = Math.log(10);

	public BDDSATLogGrow(BDDAutomata manager, String title)
	{
		super("1000*SATlog/" + title);

		this.manager = manager;
	}

	public void add(int bdd)
	{
		double d = manager.count_states(bdd);

		if (d > 0)
		{
			super.add((int) (1000 * Math.log(d) / log10));
		}
	}
}
