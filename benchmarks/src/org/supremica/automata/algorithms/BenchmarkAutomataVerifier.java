/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// For the benchmarks
import java.io.File;
import org.supremica.util.ActionTimer;

//import org.supremica.testhelpers.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.minimization.*;
import org.supremica.automata.IO.*;

// Instantiated testcases
import org.supremica.testcases.Arbiter;
import org.supremica.testcases.TransferLine;
import org.supremica.testcases.DiningPhilosophers;

public class BenchmarkAutomataVerifier
	extends TestCase
{
	public BenchmarkAutomataVerifier(String name)
	{
		super(name);
	}

	/**
	 * Assembles and returns a test suite
	 * for all the test methods of this test case.
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite(BenchmarkAutomataVerifier.class);
		return suite;
	}

	public void testBenchmarkModularNonblockingVerification()
		throws Exception
	{
		// Init options and verifier
		VerificationOptions vOptions;
		SynchronizationOptions sOptions;
		MinimizationOptions mOptions;
		vOptions = VerificationOptions.getDefaultNonblockingOptions();
		sOptions = SynchronizationOptions.getDefaultVerificationOptions();
		mOptions = MinimizationOptions.getDefaultNonblockingOptions();
		// Strategy
		mOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.AtLeastOneLocalMaxThree);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.AtLeastOneLocal);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.AtLeastOneLocalMaxThree);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.MostStatesFirst);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.FewestStatesFirst);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.FewestEventsFirst);
		//mOptions.setMinimizationStrategy(MinimizationStrategy.RandomFirst);
		// Heuristic
		mOptions.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.MostCommon);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.FewestTransitions);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.FewestStates);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.FewestEvents);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.LeastExtension);
		//mOptions.setMinimizationHeuristic(MinimizationHeuristic.Random);
	   

		System.out.println("");
		System.out.println("BENCHMARKING MODULAR NONBLOCKING VERIFICATION ALGORITHMS");
		System.out.println("Primary 1:st stage heuristic: " + mOptions.getMinimizationStrategy());
		System.out.println("Primary 2:nd stage heuristic: " + mOptions.getMinimizationHeuristic());

		ProjectBuildFromXml builder = new ProjectBuildFromXml();
		Project theProject;
		
		///////////////////////////////////
		// Instantiated model benchmarks //
		///////////////////////////////////

		if (false)
		{
			// Dining philosophers
			DiningPhilosophers philo = new DiningPhilosophers(256, true, true, false, false, false, false);
			theProject = philo.getProject();
			benchmarkNonblocking("256philo", theProject, vOptions, sOptions, mOptions);
			philo = new DiningPhilosophers(512, true, true, false, false, false, false);
			theProject = philo.getProject();
			benchmarkNonblocking("512philo", theProject, vOptions, sOptions, mOptions);
			philo = new DiningPhilosophers(1024, true, true, false, false, false, false);
			theProject = philo.getProject();
			benchmarkNonblocking("1024philo", theProject, vOptions, sOptions, mOptions);
			// Arbiter
			Arbiter arbiter = new Arbiter(128, false);
			theProject = arbiter.getProject();
			benchmarkNonblocking("128arbiter", theProject, vOptions, sOptions, mOptions);
			arbiter = new Arbiter(256, false);
			theProject = arbiter.getProject();
			benchmarkNonblocking("256arbiter", theProject, vOptions, sOptions, mOptions);
			arbiter = new Arbiter(512, false);
			theProject = arbiter.getProject();
			benchmarkNonblocking("512arbiter", theProject, vOptions, sOptions, mOptions);
			// Transfer line
			TransferLine line = new TransferLine(128, 3, 1, false);
			theProject = line.getProject();
			benchmarkNonblocking("128transfer", theProject, vOptions, sOptions, mOptions);		
			line = new TransferLine(256, 3, 1, false);
			theProject = line.getProject();
			benchmarkNonblocking("256ansfer", theProject, vOptions, sOptions, mOptions);		
			line = new TransferLine(512, 3, 1, false);
			theProject = line.getProject();
			benchmarkNonblocking("512transfer", theProject, vOptions, sOptions, mOptions);		
		}

		///////////////////////////////////
		// "Industrial" model benchmarks //
		///////////////////////////////////

		// Benchmark path
		//String prefix = "/users/s2/flordal/benchmark/";
		String prefix = "benchmarks/benchmarkfiles/";
		// Benchmarks
		String[] test = 
		{
			"verriegel3", "verriegel3b",
			"verriegel4", "verriegel4b",
			"agv", "agvb", 
			"IPS",
			"SMS", "PMS",
			"bmw_fh", "big_bmw",
			"tbed_valid", "tbed_ctct", 
			"fzelle",
			"ftechnik", "ftechnik_nocoll",
			"AIP_minus_AS3_TU4",
			"profisafe_i4"
		};		
	   
		// Run tests
		for (int i=0; i<test.length; i++)
		{
			theProject = builder.build(new File(prefix + test[i] + ".xml"));
			benchmarkNonblocking(test[i], theProject, vOptions, sOptions, mOptions);
			/*
			  try
			  {
			  System.out.println("");
			  System.out.println("CURRENT BENCHMARK: " + test[i]);
			  theProject = builder.build(new File(prefix + test[i] + ".xml"));
			  verifier = new AutomataVerifier(theProject, vOptions, sOptions, mOptions);
			  ActionTimer timer = new ActionTimer();
			  timer.start();
			  boolean nonblocking = verifier.verify();
			  timer.stop();
			  System.out.println("TIME: " + timer + ", BLOCKING: " + !nonblocking);
			  }
			  catch (Throwable ex)
			  {
			  System.out.println("Failed! " + ex);
			  }
			*/
		}
	}		

	private void benchmarkNonblocking(String name, Project theProject, 
									  VerificationOptions vOptions, 
									  SynchronizationOptions sOptions, 
									  MinimizationOptions mOptions)
	{
		try
		{
			System.out.println("");
			System.out.println("CURRENT BENCHMARK: " + name);
			AutomataVerifier verifier = new AutomataVerifier(theProject, vOptions, sOptions, mOptions);
			ActionTimer timer = new ActionTimer();
			//System.out.println("Verifying...");
			// Run the benchmark!
			timer.start();
			boolean nonblocking = verifier.verify();
			timer.stop();
			System.out.println("TIME: " + timer + ", BLOCKING: " + !nonblocking);
			// Garbage collect now!
			System.runFinalization();
			System.gc();
			//System.out.println("Running garbage collector...");
			Thread.sleep(10000);
		}
		catch (Throwable ex)
		{
			System.out.println("Failed! " + ex);
		}
	}
}
