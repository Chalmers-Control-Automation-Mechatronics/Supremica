package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

/** Show the node grow AND the delta satcount  in two different windows */
public class BDDNodeANDSATDiffGrow
	extends BDDNodeGrow
{
	private BDDSATDiffGrow satdiff = null;

	public BDDNodeANDSATDiffGrow(BDDAutomata manager, String title)
	{
		super(manager, title);

		satdiff = new BDDSATDiffGrow(manager, title);
	}

	public void add(int bdd)
	{
		super.add(bdd);
		satdiff.add(bdd);
	}

	public void startTimer()
	{
		super.startTimer();

		// if satlog = null, then we are still in the constructor of (this and )super
		if (satdiff != null)
		{
			satdiff.startTimer();
		}
	}

	public void stopTimer()
	{
		super.stopTimer();
		satdiff.stopTimer();
	}

	public void mark(String txt)
	{
		super.mark(txt);
		satdiff.mark(txt);
	}
}
