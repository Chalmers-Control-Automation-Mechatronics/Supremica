package org.supremica.util.BDD;

/**
 * Handles the optimization of the frontier set.
 * During image computation we may choose to go forward
 * from R_k ro find R_k+1, or just the latest addition ,
 * that is (R_k - R_k-1). In fact, anything in between would do!
 *
 * This class handles the choice of the "frontier set", somewhere
 * between R_k (r in the code) and (R_k - R_k-1) [front in the code]
 *
 *
 * TODO: add constraint and restrict/simplify optimization soon
 *
 */
public class FrontierSetOptimizer
{
	private BDDAutomata manager;

	public FrontierSetOptimizer(BDDAutomata manager)
	{
		this.manager = manager;
	}

	public void cleanup()
	{

		// nothing yet...
	}

	/**
	 * choose something between front and r,
	 * TAKES CARE OF DE-REFING front!!
	 *
	 */
	public int choose(int r, int front)
	{
		boolean choose_r = true;

		switch (Options.frontier_strategy)
		{

		case Options.FRONTIER_STRATEGY_RANDOM :
			choose_r = (Math.random() >= 0.5);
			break;

		case Options.FRONTIER_STRATEGY_R :
			choose_r = true;
			break;

		case Options.FRONTIER_STRATEGY_FRONT :
			choose_r = false;
			break;

		case Options.FRONTIER_STRATEGY_FRONT_MINUS_R :
			if (r == front)
			{
				break;    // we dont optimize if they are equal (maybe the first round!)
			}

			int x = manager.ite(r, manager.getZero(), front);    // x = front - r

			manager.deref(front);

			return x;

		case Options.FRONTIER_STRATEGY_MIN :
			int s1 = manager.nodeCount(r);
			int s2 = manager.nodeCount(front);

			choose_r = (s1 < s2);
			break;
		}

		if (choose_r)
		{
			manager.deref(front);

			return manager.ref(r);
		}
		else
		{
			return front;
		}
	}
}
