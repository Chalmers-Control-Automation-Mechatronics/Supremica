package org.supremica.util.BDD.test;

import java.io.*;
import org.supremica.util.BDD.*;
import org.supremica.util.BDD.li.*;
import org.supremica.automata.IO.*;
import org.supremica.automata.algorithms.*;

/**
 * "test" the result of algorithms against a set of pre-solved problems
 * We are in the "dist" directory during these tests.
 *
 */

public class TestAlgo
{

	/**
	 * It works like this:
	 * controllablility tests are always done.
	 * reachability test only if reachables != -1
	 * co-reachability test only if coreachables != -1
	 * non-blocking test only if can do reachability and co-reachability
	 * supNBC only if supstates != -1
	 */
	private static final String[] TEST_FILES = {
												"../examples/SynthesizerTest.xml",
												"../examples/includeInJarFile/OtherExamples/parallelManufacturingExample.xml",
												 "../examples/includeInJarFile/OtherExamples/agv.xml",
												 "../examples/includeInJarFile/OtherExamples/catmouse.xml",
												 "../examples/includeInJarFile/OtherExamples/circularTable.xml",
												 "../examples/includeInJarFile/OtherExamples/flexibleManufacturingSystem.xml",
												 "../examples/benchmark/simple1.xml",
												 "../examples/includeInJarFile/OtherExamples/aip/System4_system4.xml",
												 "../examples/c3.xml" };

	// XXX:         these number probably haev double-floating-point  overflows, so if we count them in some other way we might not
	//        get exactly the same number for the big ones!
	private static final double reachables[] = { 10,
		5702550, 25731072, 18, 199, 2274519862886400.0, 10000000,1.101504E7, -1 };
	private static final double coreachables[] = {  6,
			5702550, 343692864, 20, 432, 2274519862886400.0,10000000, -1, -1 };


	private static final boolean controllable[] = { false,
		true, false, false, false, true, true, true, true };
	private static final boolean nonblocking[] = { false,
		true, true, true, false, false, true, true /* [2]*/, true /* [2] */
	};

	// these are the classes that have their own sup algos (we replaced conjunctive with
	// disjunctive since the former is soooo slow on simple reachability search :(  )
	private static final int [] SUP_ALGOS = {
		Options.ALGO_MONOLITHIC, Options.ALGO_DISJUNCTIVE,
		Options.ALGO_DISJUNCTIVE_WORKSET,
		Options.ALGO_DISJUNCTIVE_STEPSTONE
	};

// which models should we test for sup synthesis??
	private static final boolean testsup[] = { true, false, true, true, true, false, false, false, false };


	// ----------------------------------------------------------------------------------
	private int fail, pass;
	private ProjectBuildFromXml builder;
	private org.supremica.util.BDD.BDDAutomata automata2;
	private org.supremica.util.BDD.Supervisor supervisor;
	private org.supremica.automata.Automata automata1;
	private org.supremica.automata.algorithms.AutomataBDDVerifier verifier;

	// ----------------------------------------------------------------------------------
	public TestAlgo()
	{
		builder = new ProjectBuildFromXml();
	}

	private void load(String name)
		throws Exception
	{
		automata1 = builder.build(new File(name));
		verifier = new AutomataBDDVerifier(automata1, null);
		automata2 = verifier.getBDDAutomata();
		supervisor = verifier.getSupervisor();
	}

	private boolean monolithic_ok(double reachables)
	{
		if ((reachables == -1) || (reachables > 5000000))
		{
			return false;
		}

		return automata2.getSize() < 15;
	}

	// ----------------------------------------------------------------------------------
	private void error(String msg)
	{
		System.err.println("\nERROR: " + msg);
		System.err.println("Reachability family: " + Options.REACH_ALGO_NAMES[Options.algo_family]);
		System.err.println("Encoding algo: " + Options.ENCODING_NAMES[Options.encoding_algorithm]);
		System.err.println("Ordering algo: " + Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm]);
		System.err.println();
		System.exit(20);    // comment out to allow ALL tests to run before stopped

		fail++;
	}

	// ----------------------------------------------------------------
	/**
	 * count the number of states in the safe state supervisor.
	 * the problem we face here is that these states are not all reachable so we cant just compare the
	 * results with supremicas traditional algorithms. we can compute the intersaction fo safe states and
	 * the reachable states, but that doesnt remove states that are both safe and reachable  buth unreachable
	 * _under supervision_.
	 *
	 */
	private void testSupNBC(String name)
	{
		int save_algo_family = Options.algo_family;    // save the default crap

		// DEBUG:
		Options.profile_on = Options.debug_on = true;


		try {
			automata1 = builder.build(new File(name));

			double got_1 = -1, got_2 = -1;

			for(int i = 0; i < SUP_ALGOS.length; i++) {
				Options.algo_family = SUP_ALGOS[i];
				verifier = new AutomataBDDVerifier(automata1, null);
				automata2 = verifier.getBDDAutomata();
				supervisor = verifier.getSupervisor();

				System.out.print("   " + Options.REACH_ALGO_NAMES[Options.algo_family] + "...");
				int safe_states = supervisor.getSafeStates(true, true);
				int reachables = supervisor.getReachables();
				int reachable_safe = automata2.and(reachables, safe_states);

				double found = automata2.count_states(safe_states);
				double found_reachable = automata2.count_states(reachable_safe);

				if(i == 0) {
					got_1 = found;
					got_2 = found_reachable;
					System.out.println(" (recorded)");
				} else {
					if(got_1 != found)
					{
						error("[testSupNBC] got " + found + " safe states, expected " + got_1);
					}
					else if(got_2 != found_reachable)
					{
						error("[testSupNBC] got " + found_reachable + " reachable safe states, expected " + got_2);
					}
					else
					{
						pass++;
						System.out.println();
					}
				}

				verifier.cleanup();
			}
		}
		catch(Exception exx)
		{
			error("[testSupNBC] something bad happened: " + exx);
		}

		Options.algo_family = save_algo_family;
	}

	// ----------------------------------------------------------------------------------
	private void testC(boolean result, double reachables)
	{

		// dont know if this is correct, but we skip controllability for less than two automata
		if( automata2.getSize()  < 2)
		{
			return ;
		}

		// WHAT IS THIS?
		// if(Options.inclsuion_algorithm != Options.INCLUSION_ALGO_MONOLITHIC) return; // not monolithic!
		if (!monolithic_ok(reachables))
		{
			return;    // system to large??
		}

		System.out.print("C ");

		boolean is_controllable = verifier.isControllable();

		if (is_controllable != result)
		{
			error("[controllability] got " + is_controllable + ", expected " + result);

			return;
		}

		pass++;
	}

	private void testR(double states_r)
	{
		if (states_r < 0)
		{
			return;
		}

		System.out.print("R ");

		int bdd_r = supervisor.getReachables();
		double got = automata2.count_states(bdd_r);

		if (got != states_r)
		{
			error("[reachability] " + got + " states reachable, expected " + states_r);

			return;
		}

		pass++;
	}

	private void testCR(double states_cr)
	{
		if (states_cr < 0)
		{
			return;
		}

		System.out.print("coR ");

		int bdd_r = supervisor.getCoReachables();
		double got = automata2.count_states(bdd_r);

		if (got != states_cr)
		{
			error("[co-reachability] " + got + " states reachable, expected " + states_cr);

			return;
		}

		pass++;
	}

	private void testNB(double states_r, double states_cr, boolean result)
	{
		if ((states_r < 0) || (states_cr < 0))
		{
			return;
		}

		System.out.print("NB ");

		boolean nb = verifier.isNonBlocking();

		if (nb != result)
		{
			error("[non-blocking] got " + nb + ", expected " + result);

			return;
		}

		pass++;
	}

	// ------------------------------------------------------------------------------------
	private void incrementalC(boolean result)
		throws Exception
	{

		// dont know if this is correct, but we skip controllability for less than two automata
		if( automata2.getSize()  < 2)
		{
			return ;
		}

		System.out.print("incrC ");

		// IncrementalBDDLanguageInclusion ili = new IncrementalBDDLanguageInclusion(automata1, null);
		IncrementalLI ili = new IncrementalLI(automata1, null);
		boolean is_controllable = ili.isControllable();

		ili.cleanup();

		if (is_controllable != result)
		{
			error("[incremental controllability] got " + is_controllable + ", expected " + result);

			return;
		}

		pass++;
	}

	// modular stuff dont use the verifier!
	private void modularC(boolean result)
		throws Exception
	{

		// dont know if this is correct, but we skip controllability for less than two automata
		if( automata2.getSize()  < 2)
		{
			return ;
		}

		System.out.print("modC ");

		ModularLI mli = new ModularLI(automata1, null);
		boolean is_controllable = mli.isControllable();

		mli.cleanup();

		if (is_controllable != result)
		{
			error("[modular controllability] got " + is_controllable + ", expected " + result);

			return;
		}

		pass++;
	}

	private void adjust(String s, int size)
	{
		System.out.print(s);

		int n = size - s.length();

		while (n-- > 0)
		{
			System.out.print(' ');
		}

		System.out.flush();
	}

	private void announce(String nam)
	{

		int n = nam.lastIndexOf('/');
		if (n > 0)
		{
			nam = nam.substring(n + 1);
		}

		n = nam.lastIndexOf('.');
		if(n > 0)
		{
			nam = nam.substring(0, n);
		}

		adjust(nam + ":", 35);
	}

	// ------------------------------------------------------------------------------------
	public void runTests()
		throws Exception
	{
		fail = pass = 0;
		int len = TEST_FILES.length;

		for (int k = 0; k < 4; k++)
		{
			System.out.println("\nTarget #" + k + " is " + TEST_FILES[k]);

			// test different algos
			System.out.println("\n***** Testing all search algorithms");

			int save_algo_family = Options.algo_family;    // save the default crap

			load(TEST_FILES[k]);

			for (int i = 0; i < Options.REACH_ALGO_NAMES.length; i++)
			{
				Options.algo_family = i;

				adjust(Options.REACH_ALGO_NAMES[Options.algo_family], 40);
				testR(reachables[k]);
				testCR(coreachables[k]);

				if (i == save_algo_family)
				{
					System.out.print("   (DEFAULT) ");
				}

				System.out.println();
			}

			verifier.cleanup();

			Options.algo_family = save_algo_family;

			// test different encodings
			System.out.println("\n***** Testing all encoding functions");

			int save_encoding = Options.encoding_algorithm;

			for (int i = 0; i < Options.ENCODING_NAMES.length; i++)
			{
				load(TEST_FILES[k]);

				Options.encoding_algorithm = i;

				adjust(Options.ENCODING_NAMES[Options.encoding_algorithm], 40);
				testR(reachables[k]);
				testCR(coreachables[k]);
				verifier.cleanup();

				if (i == save_encoding)
				{
					System.out.print("   (DEFAULT) ");
				}

				System.out.println();
			}

			Options.encoding_algorithm = save_encoding;

			// test different encodings
			System.out.println("\n***** Testing all encoding functions");

			int save_ordering = Options.ordering_algorithm;

			for (int i = 0; i < Options.ORDERING_ALGORITHM_NAMES.length; i++)
			{
				load(TEST_FILES[k]);

				Options.ordering_algorithm = i;

				adjust(Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm], 40);
				testR(reachables[k]);
				testCR(coreachables[k]);
				verifier.cleanup();

				if (i == save_ordering)
				{
					System.out.print("   (DEFAULT) ");
				}

				System.out.println();
			}

			Options.ordering_algorithm = save_ordering;
		}



		System.out.println("\n***** Testing DES/SCT algorithms");
		for (int i = 0; i < len; i++)
		{
			announce(TEST_FILES[i]);
			load(TEST_FILES[i]);
			testR(reachables[i]);
			testCR(coreachables[i]);
			testNB(reachables[i], coreachables[i], nonblocking[i]);
			testC(controllable[i], reachables[i]);
			verifier.cleanup();    // cleans up both supervisor and automata2

			// what a waste of resources, we will do all BDD pre-calcs again :(
			incrementalC(controllable[i]);
			modularC(controllable[i]);
			System.out.println();
		}

		System.out.println("\n***** Testing SCT/synthesis algorithms");
		for (int i = 0; i < len; i++)
		{
			if(testsup[i]) {
				System.out.println("Loading " + TEST_FILES[i] + "...");
				testSupNBC(TEST_FILES[i]);
			}
		}




		if (fail == 0)
		{
			System.out.println("All " + pass + " tests passed ");
		}
		else
		{
			System.out.println("" + fail + ((fail == 1)
											? " test FAILED"
											: " tests FAILED"));
			System.exit(20);    // old C habbit...
		}
	}

	public static void main(String[] args)
	{
		Options.profile_on = false;

		try
		{
			Options.debug_on = true;

			FileOutputStream fos = new FileOutputStream("bdd_tests.txt", false);
			PrintStream ps = new PrintStream(fos);

			Options.out = ps;
		}
		catch (IOException exx)
		{
			Options.out.println("Could not set proof file: " + exx);
		}

		// remeber, there is no GUI:
		Options.debug_on = false;
		Options.size_watch = false;
		Options.user_alters_PCG = false;
		Options.show_grow = Options.SHOW_GROW_NONE;
		Options.show_encoding = false;

		TestAlgo ta = new TestAlgo();

		try
		{
			ta.runTests();
		}
		catch (Exception exx)
		{
			exx.printStackTrace();
		}
	}
}

/*
 $Log: not supported by cvs2svn $
 Revision 1.15  2004/06/29 14:57:14  vahidi

 Added workset support for the supNBC algo ==> faster synthesis.

 + some minor bug fixes
 + supNBC added to the testcases (not working very good though)

 Revision 1.14  2004/06/11 21:12:52  knut
 After running JIndent

 Revision 1.13  2004/05/21 15:28:08  vahidi

 major  bugfix patch:

 1. (most) BDD options are now saved and loaded from the property file
 2. stupid bug fixed in disjunctive mode (and all its supervisors):
	 plant and interfaces are now PLANT, anything else is SPEC.
 3. bug fixed in incremental LI, when a condition for initial-state-uncontrollable was REVERSED :(
 4. bug fixed in modular LI, when a NUMBER was used instead of a BDD :(
 5. bug fixed in Supremica/Gui, now selectAutomata() works corectly (i hope) !!


 misc:
 a new test case added (parellel manfct), which helped to catch bug 3 and 4.
   "ant bdd_tests" wil now take longer time to complete (35-45 secs)

 Vector changed to proper Collection structures in AutomataCommunicationHelper.java

 fixed buddy DLL path in cfg-file to point to d:\\code instead of c:\\code :)

 plus  more stuff i cant remember :(

 Revision 1.12  2004/05/04 10:01:08  vahidi
 EPS files created via DOT can now fit an A4 page. It is on by default and cannot be disabled, see AutomatonToDot.java

 Revision 1.11  2004/05/03 08:11:48  torda
 Made some constants static

 Revision 1.10  2004/04/26 08:35:44  torda
 Removed all(?) unused import statements (hundreds) for the following reasons:

 - Unused imports can be confusing
 - They give unnessesary dependencies
 - Eclipse marked out everyone so it was easily done
 - It felt good to remove them

 Revision 1.9  2004/04/08 11:10:20  vahidi
 encoding bug originated from IntQueue fixed. more tests added to discover all such stupid problems in future :(

 Revision 1.8  2004/01/30 14:45:04  vahidi
 testing log keywork, ignore this one


*/
