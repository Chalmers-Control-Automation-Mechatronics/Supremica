package org.supremica.util.BDD.test;

import java.io.*;

import org.supremica.util.BDD.*;
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
		"../examples/c2.xml" ,
		"../examples/c3.xml"

		};
	private final long reachables[] = { 25731072, 18, 199, 2274519862886400L, 10000000, -1, -1};
	private final long coreachables[] = { 343692864, 68, 4920, 2538998916710400L, 10000000, -1, -1};
	private final boolean controllable[] = { false, false, false, true, true, true, true};
	private final boolean nonblocking[] = { true, true, false, true, true, true /* dont know */, true /* dont know */};


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

	// ----------------------------------------------------------------------------------

	private void testC(boolean result) {
		if(Options.inclsuion_algorithm != Options.INCLUSION_ALGO_MONOLITHIC) return; // not monolithic!

		System.out.print("C ");
		boolean is_controllable = verifier.isControllable();

		if(is_controllable != result) {
			System.err.println("ERROR: [controllability] got " + is_controllable + ", expected " + result);
			fail ++;
			return;
		}
		pass++;
	}


	private void testR(long states_r) {
		if(states_r < 0) return;

		System.out.print("R ");

		int bdd_r = supervisor.getReachables();
		long got = automata2.count_states(bdd_r);
		if(got != states_r) {
			System.err.println("ERROR: [reachability] " + got + " states reachable, expected " + states_r);
			fail ++;
			return;
		}
		pass++;
	}

	private void testCR(long states_cr) {
		if(states_cr < 0) return;

		System.out.print("coR ");

		int bdd_r = supervisor.getCoReachables();
		long got = automata2.count_states(bdd_r);
		if(got != states_cr) {
			System.err.println("ERROR: [co-reachability] " + got + " states reachable, expected " + states_cr);
			fail ++;
			return;
		}
		pass++;
	}

	private void testNB(long states_r, long states_cr, boolean result) {
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
		IncrementalBDDLanguageInclusion ili = new IncrementalBDDLanguageInclusion(automata1, null);
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
		ModularBDDLanguageInclusion mli = new ModularBDDLanguageInclusion(automata1, null);
		boolean is_controllable = mli.isControllable();
		mli.cleanup();

		if(is_controllable != result) {
			System.err.println("ERROR: [modular controllability] got " + is_controllable + ", expected " + result);
			fail ++;
			return;
		}
		pass++;

	}

	private void announce(String nam) {
		int n = nam.lastIndexOf('/');
		if(n > 0) nam = nam.substring(n+1);
		System.out.print(nam);
		System.out.print(':');
		n = 40 - nam.length();
		while(n-- > 0) System.out.print(' ');
		System.out.flush();
	}
	// ------------------------------------------------------------------------------------
	public void runTests() throws Exception {
		fail  = pass = 0;
		int len = TEST_FILES.length;

		System.out.println("Using serach algorithm: " + Options.REACH_ALGO_NAMES[Options.algo_family]);
		for(int i = 0; i < len; i++) {
			announce(TEST_FILES[i]);

			load(TEST_FILES[i]);

			testC(controllable[i]);
			testR(reachables[i]);
			testCR(coreachables[i]);
			testNB(reachables[i], coreachables[i], nonblocking[i]);
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
		// these will create a lot of noise!
		Options.profile_on = false;
		Options.debug_on = false;


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
