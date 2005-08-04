package org.supremica.util.BDD.test;

import org.supremica.util.BDD.*;

/**
 * This class is used for profiling reachability (at the moment) without
 * need to choose a verification or synthesis operation from the menu. It just
 * computes what you ask it.
 *
 */
public class DeveloperTest
{
	BDDAutomata automata;
	Supervisor sup;

	private DeveloperTest(org.supremica.automata.Automata a)
		throws BDDException
	{
		Builder b = new Builder(a);

		automata = b.getBDDAutomata();

		try
		{
			sup = SupervisorFactory.createSupervisor(automata, automata.getAutomataVector());
		}
		catch (Exception exx)
		{
			automata.cleanup();

			exx.printStackTrace(); // DEBUG!
			throw new BDDException("SupervisorFactory.createSupervisor factory failed: " + exx);
		}
	}

	private void cleanup()
	{
		sup.cleanup();
		automata.cleanup();
	}

	// -----------------------------------------------------------------------------

	private void do_reachability()
	{
		int bdd_r = sup.getReachables();

		automata.count_states("Reachable states:", bdd_r);
	}

	private void do_coreachability()
	{
		int bdd_r = sup.getCoReachables();

		automata.count_states("Coreachable states:", bdd_r);
	}

	private void do_under_construction()
	{
		// currentlty, we are playing with the synthesis algo:
		int bdd_safe = sup.getSafeStates(true, true);
		automata.count_states("Safe states:", bdd_safe);


		int bdd_r = sup.getReachables();
		int tmp = automata.and(bdd_r, bdd_safe);
		automata.count_states("REACHABLE safe states:", tmp);
	}

	/*
	private void do_deadlock()
	{
		System.err.println("UNDER DEVELOPMENT...");
	}
	*/


	// -----------------------------------------------------------------------------
	public static void DoReachability(org.supremica.automata.Automata a)
	{
		try
		{
			DeveloperTest dt = new DeveloperTest(a);

			dt.do_reachability();
			dt.cleanup();
		}
		catch (BDDException exx)
		{
			exx.printStackTrace();
		}
	}

	public static void DoCoReachability(org.supremica.automata.Automata a)
	{
		try
		{
			DeveloperTest dt = new DeveloperTest(a);

			dt.do_coreachability();
			dt.cleanup();
		}
		catch (BDDException exx)
		{
			exx.printStackTrace();
		}
	}

/*
	public static void DoDeadlock(org.supremica.automata.Automata a)
	{
		try
		{
			DeveloperTest dt = new DeveloperTest(a);

			dt.do_deadlock();
			dt.cleanup();
		}
		catch (BDDException exx)
		{
			exx.printStackTrace();
		}
	}
	*/

	public static void DoUnderConstruction(org.supremica.automata.Automata a)
	{
		try
		{
			DeveloperTest dt = new DeveloperTest(a);

			dt.do_under_construction();
			dt.cleanup();
		}
		catch (BDDException exx)
		{
			exx.printStackTrace();
		}
	}
}
