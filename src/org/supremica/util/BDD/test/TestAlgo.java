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
												// the first four (or was it number 2,3,4) are used in some slower tests
												"../examples/SynthesizerTest.xml",
												 "../examples/includeInJarFile/OtherExamples/agv.xml",
												 "../examples/includeInJarFile/OtherExamples/catmouse.xml",
												 "../examples/includeInJarFile/OtherExamples/circularTable.xml",
												 // and the rest...
												 "../examples/includeInJarFile/OtherExamples/parallelManufacturingExample.xml",
												 "../examples/includeInJarFile/OtherExamples/flexibleManufacturingSystem.xml",
												 "../examples/benchmark/simple1.xml",
												 "../examples/includeInJarFile/OtherExamples/aip/System4_system4.xml",
												 "../examples/c3.xml" };

	/**
	 * Same as TEST_FILES, but for supNBC only. small enough for supNBC algo
	 */
	private static final String[] TEST_FILES_SUP = {
		"../examples/SynthesizerTest.xml",
		"../examples/includeInJarFile/OtherExamples/catmouse.xml",
		"../examples/includeInJarFile/OtherExamples/circularTable.xml",
		"../examples/includeInJarFile/OtherExamples/dosingUnit.xml",
		"../examples/includeInJarFile/OtherExamples/telecommunicationsNetwork.xml",
		"../examples/benchmark/simple1.xml",
		"../examples/includeInJarFile/OtherExamples/agv.xml"
	};

	// XXX:         these number probably haev double-floating-point  overflows, so if we count them in some other way we might not
	//        get exactly the same number for the big ones!
	private static final double reachables[] = { 10,
		25731072, 18, 199, 5702550, 2274519862886400.0, 10000000,1.101504E7, -1 };
	private static final double coreachables[] = {  6,
			343692864, 20, 432, 5702550, 2274519862886400.0,10000000, -1, -1 };


	private static final boolean controllable[] = { false,
		false, false, false, true, true, true, true, true };
	private static final boolean nonblocking[] = { false,
		true, true, false, true, false, true, true /* [2]*/, true /* [2] */
	};

	// these are the classes that have their own sup algos (we replaced conjunctive with
	// disjunctive since the former is soooo slow on simple reachability search :(  )
	private static final int [] SUP_ALGOS = {
		Options.ALGO_MONOLITHIC, Options.ALGO_DISJUNCTIVE,
		Options.ALGO_DISJUNCTIVE_WORKSET,
		Options.ALGO_DISJUNCTIVE_STEPSTONE
	};


	// The reachability algos that currently use the disjunctive optimization
	private static final int [] DISJ_OPT_ALGOS = {
		Options.ALGO_DISJUNCTIVE,
		// Options.ALGO_SMOOTHED_MONO_WORKSET,
		Options.ALGO_SMOOTHED_MONO,
		Options.ALGO_SMOOTHED_DELAYED_MONO,
		Options.ALGO_SMOOTHED_DELAYED_STAR_MONO,
	};

	// These are the algorithms that use the H1/H2 heuristic paris
	private static final int [] H1H2_HEURISTIC_ALGOS = {
		Options.ALGO_DISJUNCTIVE_WORKSET,
		Options.ALGO_SMOOTHED_MONO_WORKSET,
		Options.ALGO_PETRINET,
	};

	// ----------------------------------------------------------------------------------
	private static int find(String file)
	{
		for(int i = 0; i < TEST_FILES.length; i++)
		{
			if(TEST_FILES[i].indexOf(file) != -1)
			{
				return i;
			}
		}
		return -1;
	}
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
		if(!Options.interleaved_variables)	System.err.println("Using SEPARATED ordering!");
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
		Options.test_integrity = true; // enable extra runtime tests!
		fail = pass = 0;

		// we will use AGV in this experiment
		int agv = find("agv.xml");
		int catmouse = find("catmouse.xml");


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
			System.out.println("\n***** Testing all ordering algorithms (slow!)");
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



		// test the interleaved/separated ordering
		System.out.println("\n***** Testing interleaved & separated ordering, using catmouse");
		boolean save_int = Options.interleaved_variables;

		for(int i = 0; i < 2; i++) {
			Options.interleaved_variables = (i == 0);
			load(TEST_FILES[catmouse]);
			adjust(Options.interleaved_variables ? "interleaved" : "separated", 40);

			testR(reachables[catmouse]);
			testCR(coreachables[catmouse]);

			verifier.cleanup();

			if (Options.interleaved_variables == save_int)
			{
				System.out.print("   (DEFAULT) ");
			}
			System.out.println();

		}

		Options.interleaved_variables = save_int;




		// test the disjunctive optimization:
		System.out.println("\n***** Testing disjunctive optimization, using AGV");
		int oldalgo = Options.algo_family;
		int oldopt = Options.disj_optimizer_algo;
		for(int i = 0; i < DISJ_OPT_ALGOS.length; i++)
		{
			Options.algo_family = DISJ_OPT_ALGOS[i];
			System.out.println("Reachability family: " + Options.REACH_ALGO_NAMES[Options.algo_family]);

			for(int k = 0; k < Options.DISJ_OPTIMIZER_NAMES.length; k++)
			{
				Options.disj_optimizer_algo = k;
				announce("  optimizer " + Options.DISJ_OPTIMIZER_NAMES[k] );

				load(TEST_FILES[agv]);
				testR(reachables[agv]);
				testCR(coreachables[agv]);

				if (k == oldopt)
				{
					System.out.print(" (DEFAULT) ");
				}

				verifier.cleanup();
				System.out.println();

			}
		}
		Options.algo_family = oldalgo;
		Options.disj_optimizer_algo = oldopt;




		// We also test the H1/H2 heuristics. note that we dont test performance here
		System.out.println("\n***** Testing H1 and H2 heuristics, using AGV");
		int oldh1 = Options.es_heuristics;
		int oldh2 = Options.ndas_heuristics;
		oldalgo = Options.algo_family;

		for(int r = 0; r < H1H2_HEURISTIC_ALGOS.length; r++) {
			// H1 and H2 works only with workset and mono workset
			Options.algo_family = H1H2_HEURISTIC_ALGOS[r];
			System.out.println("Reachability Algorithm: " + Options.REACH_ALGO_NAMES[Options.algo_family]);

			// test H1:
			Options.ndas_heuristics = Options.NDAS_RANDOM; // fix H2 to random

			for(int i = 1; i < Options.ES_HEURISTIC_NAMES.length; i++) // first one is interactive!
			{
				announce("  H1=" + Options.ES_HEURISTIC_NAMES[i]);
				Options.es_heuristics = i;
				load(TEST_FILES[agv]);
				testR(reachables[agv]);
				testCR(coreachables[agv]);
				verifier.cleanup();
				System.out.println();
			}

			// test H2:
			Options.es_heuristics = Options.ES_HEURISTIC_ANY; // fix H1 to all-pass
			for(int i = 0; i < Options.NDAS_HEURISTIC_NAMES.length; i++)
			{
				announce("  H2=" + Options.NDAS_HEURISTIC_NAMES[i]);
				Options.ndas_heuristics = i;
				load(TEST_FILES[agv]);
				testR(reachables[agv]);
				testCR(coreachables[agv]);
				verifier.cleanup();
				System.out.println();
			}
		}
		// cleanup:
		Options.es_heuristics = oldh1 ;
		Options.ndas_heuristics = oldh2;
		Options.algo_family = oldalgo;



		// the supervisor synthesis
		System.out.println("\n***** Testing DES and SCT/verification algorithms");


		// XXX: for reasons i haven't figured out (might have to do with the selection heuristics),
		//      modular code perform very bad will the default FORCE ordering heuristics
		int save_ordering = Options.ordering_algorithm;
		Options.ordering_algorithm = Options.AO_HEURISTIC_BFS;
		System.out.println("   (note: temporarily switched to '" + Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm] + "' ordering)");
		for (int i = 0; i < TEST_FILES.length; i++)
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

		// cleanup
		Options.ordering_algorithm = save_ordering;


		// ------------------------- testing safe state supervisor synthesis:
		System.out.println("\n***** Testing SCT/synthesis algorithms");
		for (int i = 0; i < TEST_FILES_SUP.length; i++)
		{
			System.out.println("Loading " + TEST_FILES_SUP[i] + "...");

			for(int j = 0; j < Options.SUP_REACHABILITY_NAMES.length; j++) {
				System.out.println(" Reachability mode: " + Options.SUP_REACHABILITY_NAMES[j]);
				Options.sup_reachability_type = j;
				testSupNBC(TEST_FILES_SUP[i]);
			}
		}


		System.out.println("\n\n");
		if (fail == 0)
		{
			System.out.println("All " + pass + " tests PASSED");
		}
		else
		{
			System.out.println("" + fail + ((fail == 1)
											? " test FAILED"
											: " tests FAILED"));
		}

		System.out.println("\n\n");

		// old C habbit, makes the launcher (Make, ANT, etc) to fail too
		System.exit( fail == 0 ? 0 : 20);

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
 Revision 1.23  2004/10/13 13:25:42  vahidi
 *** empty log message ***

 Revision 1.22  2004/09/29 12:40:17  vahidi
 minor or major changed related to ordering

 Revision 1.21  2004/08/20 14:48:42  vahidi
 the ordering algorithms now use quick-sort if needed.

 Revision 1.20  2004/08/10 15:20:23  vahidi
 Finally rewrote DisjOptimizer and added optimization to some search functions
 (not yet workset)

 Revision 1.19  2004/08/03 12:23:24  vahidi
 The testbed now also tests the H1 and H2 heuristics with workset and monotonic-workset

 Revision 1.18  2004/07/22 11:50:39  vahidi
 1. cleaned up in the BDD panels in PreferencesDialog.
 2. supNBC can now choose level of reachability (non, uc only, total) [TestAlgo updated for this]

 Revision 1.17  2004/07/21 12:47:08  vahidi
 the test algo is more sane when it coes to supNBC tests.

 for some very odd reason, AGV.xml does not fail when using the StepStoneSupervisor anymore.

 something is wrong !

 Revision 1.16  2004/07/09 14:56:01  vahidi

 safe state supNBC support added to the StepStoneSupervisor (not working yet)


 testSupNBC have been re-written.


 NOTE: testSupNBC fails currently on StepStoneSupervisor and AGV.xml

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
