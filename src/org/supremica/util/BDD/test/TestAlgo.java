package org.supremica.util.BDD.test;

import java.io.*;

import org.supremica.util.BDD.*;
import org.supremica.util.BDD.li.*;


import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.supremica.automata.algorithms.*;

/**
 * "test" the result of algorithms against a set of pre-solved problems
 * We are in the "dist" directory during these tests.
 */

public class TestAlgo {

	/**
	 * It works like this:
	 * controllablility tests is always done.
	 * reachability test only if size != -1
	 * co-reachability test only if size != -1
	 * non-blocking test only if can do reachability and co-reachability
	 */
	private final String [] TEST_FILES = {
		"../examples/includeInJarFile/OtherExamples/agv.xml" ,
		"../examples/includeInJarFile/OtherExamples/catmouse.xml",
		"../examples/includeInJarFile/OtherExamples/circularTable.xml",
		"../examples/includeInJarFile/OtherExamples/flexibleManufacturingSystem.xml",
		"../examples/benchmark/simple1.xml",
		"../examples/includeInJarFile/OtherExamples/aip/System4_system4.xml",
		"../examples/c3.xml"

		};
	private final double reachables[] = { 25731072, 18, 199, 2274519862886400.0, 10000000, 1.101504E7, -1};
	private final double coreachables[] = { 343692864, 20, 432, 2274519862886400.0, 10000000, -1, -1};
	private final boolean controllable[] = { false, false, false, true, true, false, true};
	private final boolean nonblocking[] = { true, true, false, false, true, true /* dont know */, true /* dont know */};


	// ----------------------------------------------------------------------------------
	private int fail, pass;
	private ProjectBuildFromXml builder;
	private org.supremica.util.BDD.BDDAutomata automata2;
	private org.supremica.util.BDD.Supervisor supervisor;
	private org.supremica.automata.Automata automata1;
	private org.supremica.automata.algorithms.AutomataBDDVerifier verifier;

	// ----------------------------------------------------------------------------------

	public TestAlgo() {
		builder = new ProjectBuildFromXml();
	}


	private void load(String name) throws Exception {
		automata1 = builder.build(new File(name));
		verifier = new AutomataBDDVerifier(automata1, null);
		automata2 = verifier.getBDDAutomata();
		supervisor = verifier.getSupervisor();
	}

	private boolean monolithic_ok(double reachables) {
		if(reachables == -1 || reachables > 5000000) return false;
		return automata2.getSize() < 15;
	}
	// ----------------------------------------------------------------------------------

	private void testC(boolean result, double reachables) {
		// WHAT IS THIS?
		// if(Options.inclsuion_algorithm != Options.INCLUSION_ALGO_MONOLITHIC) return; // not monolithic!

		if(!monolithic_ok(reachables) ) return; // system to large??

		System.out.print("C ");
		boolean is_controllable = verifier.isControllable();

		if(is_controllable != result) {
			System.out.println("\nERROR: [controllability] got " + is_controllable + ", expected " + result);
			fail ++;
			return;
		}
		pass++;
	}


	private void testR(double states_r) {
		if(states_r < 0) return;

		System.out.print("R ");

		int bdd_r = supervisor.getReachables();
		double got = automata2.count_states(bdd_r);
		if(got != states_r) {
			System.err.println("ERROR: [reachability] " + got + " states reachable, expected " + states_r);
			fail ++;
			return;
		}
		pass++;
	}

	private void testCR(double states_cr) {
		if(states_cr < 0) return;

		System.out.print("coR ");

		int bdd_r = supervisor.getCoReachables();
		double got = automata2.count_states(bdd_r);
		if(got != states_cr) {
			System.err.println("ERROR: [co-reachability] " + got + " states reachable, expected " + states_cr);
			fail ++;
			return;
		}
		pass++;
	}

	private void testNB(double states_r, double states_cr, boolean result) {
		if(states_r < 0 || states_cr < 0) return;

		System.out.print("NB ");

		boolean nb = verifier.isNonBlocking();

		if(nb != result) {
			System.err.println("ERROR: [non-blocking] got " + nb + ", expected " + result);
			fail ++;
			return;
		}
		pass++;
	}
	// ------------------------------------------------------------------------------------

	private void incrementalC(boolean result) throws Exception {
		System.out.print("incrC ");
		// IncrementalBDDLanguageInclusion ili = new IncrementalBDDLanguageInclusion(automata1, null);
		IncrementalLI ili = new IncrementalLI(automata1, null);
		boolean is_controllable = ili.isControllable();
		ili.cleanup();

		if(is_controllable != result) {
			System.err.println("ERROR: [incremental controllability] got " + is_controllable + ", expected " + result);
			fail ++;
			return;
		}
		pass++;
	}
	// modular stuff dont use the verifier!
	private void modularC(boolean result) throws Exception {

		System.out.print("modC ");
		ModularLI mli = new ModularLI(automata1, null);
		boolean is_controllable = mli.isControllable();
		mli.cleanup();

		if(is_controllable != result) {
			System.err.println("ERROR: [modular controllability] got " + is_controllable + ", expected " + result);
			fail ++;
			return;
		}
		pass++;

	}

	private void adjust(String s, int size) {
		System.out.print(s);
		int n = size - s.length();
		while(n-- > 0) System.out.print(' ');
		System.out.flush();
	}

	private void announce(String nam) {
		int n = nam.lastIndexOf('/');
		if(n > 0) nam = nam.substring(n+1);
		adjust(nam + ":", 40);
	}
	// ------------------------------------------------------------------------------------
	public void runTests() throws Exception {
		fail  = pass = 0;
		int len = TEST_FILES.length;


		System.out.println("NOTE: test target for the first phase is : " + TEST_FILES[0]);

		// test different algos
		System.out.println("\n***** Testing all search algorithms");
		int save_algo_family = Options.algo_family; // save the default crap
		load(TEST_FILES[0]);
		for(int i = 0; i < Options.REACH_ALGO_NAMES.length; i++) {
			Options.algo_family = i;
			adjust(Options.REACH_ALGO_NAMES[Options.algo_family], 40);
			testR(reachables[0]);
			testCR(coreachables[0]);

			if(i == save_algo_family) System.out.print("   (DEFAULT) ");
			System.out.println();
		}
		verifier.cleanup();
		Options.algo_family = save_algo_family;

	// test different encodings
		System.out.println("\n***** Testing all encoding functions");
		int save_encoding = Options.encoding_algorithm;
		for(int i = 0; i < Options.ENCODING_NAMES.length; i++) {
			load(TEST_FILES[0]);
			Options.encoding_algorithm = i;
			adjust(Options.ENCODING_NAMES[Options.encoding_algorithm], 40);
			testR(reachables[0]);
			testCR(coreachables[0]);
			verifier.cleanup();

			if(i == save_encoding) System.out.print("   (DEFAULT) ");
			System.out.println();
		}
		Options.encoding_algorithm = save_encoding;
		System.out.println("NOTE: default algorithm is " + Options.ENCODING_NAMES[Options.encoding_algorithm]);


// test different encodings
		System.out.println("\n***** Testing all encoding functions");
		int save_ordering = Options.ordering_algorithm;
		for(int i = 0; i < Options.ORDERING_ALGORITHM_NAMES.length; i++) {
			load(TEST_FILES[0]);
			Options.ordering_algorithm = i;
			adjust(Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm], 40);
			testR(reachables[0]);
			testCR(coreachables[0]);
			verifier.cleanup();

			if(i == save_ordering) System.out.print("   (DEFAULT) ");
			System.out.println();
		}
		Options.ordering_algorithm = save_ordering;
		System.out.println("NOTE: default algorithm is " + Options.ENCODING_NAMES[Options.ordering_algorithm]);





		System.out.println("\n***** Testing DES/SCT algorithms");
		for(int i = 0; i < len; i++) {
			announce(TEST_FILES[i]);

			load(TEST_FILES[i]);


			testR(reachables[i]);
			testCR(coreachables[i]);
			testNB(reachables[i], coreachables[i], nonblocking[i]);
			testC(controllable[i],  reachables[i]);
			verifier.cleanup(); // cleans up both supervisor and automata2

			// what a waste of resources, we will do all BDD pre-calcs again :(
			incrementalC(controllable[i]);
			modularC(controllable[i]);

			System.out.println();
		}

		if(fail == 0) {
			System.out.println("All " + pass + " tests passed ");
		} else {
			System.out.println("" + fail + (fail == 1 ? " test FAILED" : " tests FAILED"));
			System.exit(20); // old C habbit...

		}
	}


	public static void main(String [] args) {

		Options.profile_on = false;


		try {
			Options.debug_on = true;
			FileOutputStream fos = new FileOutputStream("bdd_tests.txt", false);
			PrintStream ps = new PrintStream(fos);
			Options.out = ps;
			Options.debug_on = true;
		} catch(IOException exx) {
			Options.out.println("Could not set proof file: " + exx);
		}


		// remeber, there is no GUI:
		Options.size_watch = false;
		Options.user_alters_PCG = false;
		Options.show_grow = Options.SHOW_GROW_NONE;
		Options.show_encoding = false;


		TestAlgo ta = new TestAlgo();
		try {
			ta.runTests();
		} catch(Exception exx) {
			exx.printStackTrace();
		}
	}
}

/*
 $Log: not supported by cvs2svn $
 Revision 1.8  2004/01/30 14:45:04  vahidi
 testing log keywork, ignore this one


*/