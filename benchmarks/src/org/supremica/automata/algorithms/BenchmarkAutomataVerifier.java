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

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// For the benchmarks
import java.io.File;
import org.supremica.util.ActionTimer;
import org.supremica.automata.algorithms.minimization.*;
import org.supremica.automata.IO.*;
import org.supremica.automata.Project;
import java.util.Date;

// Instantiated testcases
import org.supremica.testcases.Arbiter;
import org.supremica.testcases.TransferLine;
import org.supremica.testcases.DiningPhilosophers;

public class BenchmarkAutomataVerifier
    extends TestCase
{
    OutputStreamWriter file;
    
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
    
    public void testBenchmarkVerification()
    throws Exception
    {
        // Init options and verifier
        VerificationOptions vOptions;
        SynchronizationOptions sOptions;
        MinimizationOptions mOptions;
        sOptions = SynchronizationOptions.getDefaultVerificationOptions();
        mOptions = MinimizationOptions.getDefaultNonblockingOptions();
        
        // Controllability / Nonblocking
        vOptions = VerificationOptions.getDefaultNonblockingOptions();
        int type = 1;
        if (type == 0)
            // Nonblocking
            vOptions.setVerificationType(VerificationType.NONBLOCKING);
        else if (type == 1)
            // Controllability
            vOptions.setVerificationType(VerificationType.CONTROLLABILITY);
        else if (type == 2)
            // Both
            vOptions.setVerificationType(VerificationType.CONTROLLABILITYNONBLOCKING);
        
        // Compositional / Modular
        int algo = 0;
        if (algo == 0)
            // Compositional
            vOptions.setAlgorithmType(VerificationAlgorithm.COMPOSITIONAL);
        else if (algo == 1)
            // Modular
            vOptions.setAlgorithmType(VerificationAlgorithm.MODULAR);
        
        // Strategies
        MinimizationStrategy[] strategyArray =
        {
            MinimizationStrategy.FewestTransitionsFirst,
            /*
             */
            MinimizationStrategy.MostStatesFirst,
            MinimizationStrategy.AtLeastOneLocal,
            MinimizationStrategy.AtLeastOneLocalMaxThree,
            //MinimizationStrategy.FewestStatesFirst,
            //MinimizationStrategy.FewestEventsFirst,
            /*
            MinimizationStrategy.RandomFirst
             */
            MinimizationStrategy.FewestNeighboursFirst,
        };
        
        // Heuristics
        MinimizationHeuristic[] heuristicArray =
        {
            MinimizationHeuristic.MostLocal,
            /*
             */
            /*
             */
            //MinimizationHeuristic.FewestStates,
            //MinimizationHeuristic.LeastExtension,
            //MinimizationHeuristic.FewestTransitions,
            MinimizationHeuristic.MostCommon,
            //MinimizationHeuristic.FewestEvents,
            MinimizationHeuristic.LeastFanning
        };
        
        try
        {
            String fileName = "benchmarklog.txt";
            file = new FileWriter(fileName);
            file.write("BENCHMARKS FOR VERIFICATION OF " + vOptions.getVerificationType() + " USING THE " + vOptions.getAlgorithmType() + " ALGORITHM...\n");
            file.write("Date: " + new Date() + "\n");
            file.flush();
            System.out.println("BENCHMARK LOG WILL BE WRITTEN TO " + fileName);
            System.out.flush();
            collectGarbage();
        }
        catch (IOException ex)
        {
            System.err.println(ex);
        }
        
        // Try all strategies combined with all heuristics...
        for (int i=0; i<strategyArray.length; i++)
        {
            mOptions.setMinimizationStrategy(strategyArray[i]);
            for (int j=0; j<heuristicArray.length; j++)
            {
                mOptions.setMinimizationHeuristic(heuristicArray[j]);
                
                // Go!
                System.out.println("");
                System.out.println("BENCHMARKING " + vOptions.getAlgorithmType() + " " + vOptions.getVerificationType() + " VERIFICATION");
                System.out.println("Primary 1:st stage heuristic: " + mOptions.getMinimizationStrategy());
                System.out.println("Primary 2:nd stage heuristic: " + mOptions.getMinimizationHeuristic());
                System.out.flush();
                file.write("Primary 1:st stage heuristic: " + mOptions.getMinimizationStrategy() + "\n");
                file.write("Primary 2:nd stage heuristic: " + mOptions.getMinimizationHeuristic() + "\n");
                file.flush();
                
                //if (false) // Academic
                if (true) // Industrial
                {
                    ///////////////////////////////////
                    // "Industrial" model benchmarks //
                    ///////////////////////////////////
                    
                    // Benchmark path
                    String prefix = "benchmarks/benchmarkfiles/";
                    // Benchmarks
                    String[] test =
                    {
                                                /*
                                                 */
                        "agv", "agvb",
                        "verriegel3", "verriegel3b",
                        "verriegel4", "verriegel4b",
                        //"bmw_fh",
                        "big_bmw",
                        "FMS",
                        "SMS",
                        "PMS",
                        "IPC",
                        "ftechnik",
                        //"ftechnik_nocoll", // Includes some "property" specifications
                        //"fzelle", // All states are marked!?
                        "rhone_tough",
                        //"tbed_ctct"
                        "AIP_minus_AS3_TU4",
                        "tbed_valid",
                        "PLanTS",
                        "profisafe_i4",
                                                /*
                                                 */
                    };
                    
                    // Run tests
                    for (int k=0; k<test.length; k++)
                    {
                        ProjectBuildFromXML builder = new ProjectBuildFromXML();
                        Project theProject = builder.build(new File(prefix + test[k] + ".xml"));
                        theProject.setName(test[k]);
                        runBenchmark(test[k], theProject, vOptions, sOptions, mOptions);
                    }
                }
                else
                {
                    ///////////////////////////////////
                    // Instantiated model benchmarks //
                    ///////////////////////////////////
                    
                    DiningPhilosophers philo;
                    TransferLine line;
                    Arbiter arbiter;
                    
                    Project theProject;
                    
                                        /*
                    // Dining philosophers
                                        philo = new DiningPhilosophers(256, true, true, false, false, false, false);
                    theProject = philo.getProject();
                    runBenchmark("256philo", theProject, vOptions, sOptions, mOptions);
                                        philo = new DiningPhilosophers(512, true, true, false, false, false, false);
                                        theProject = philo.getProject();
                                        runBenchmark("512philo", theProject, vOptions, sOptions, mOptions);
                                        philo = new DiningPhilosophers(1024, true, true, false, false, false, false);
                                        theProject = philo.getProject();
                    runBenchmark("1024philo", theProject, vOptions, sOptions, mOptions);
                                        //philo = new DiningPhilosophers(2048, true, true, false, false, false, false);
                    //theProject = philo.getProject();
                    //runBenchmark("2048philo", theProject, vOptions, sOptions, mOptions);
                    // Transfer line
                                        line = new TransferLine(128, 3, 1, false);
                    theProject = line.getProject();
                    runBenchmark("128transfer", theProject, vOptions, sOptions, mOptions);
                    line = new TransferLine(256, 3, 1, false);
                    theProject = line.getProject();
                    runBenchmark("256transfer", theProject, vOptions, sOptions, mOptions);
                    line = new TransferLine(512, 3, 1, false);
                    theProject = line.getProject();
                    runBenchmark("512transfer", theProject, vOptions, sOptions, mOptions);
                                         */
                    // Arbiter
                    arbiter = new Arbiter(128, false);
                    theProject = arbiter.getProject();
                    runBenchmark("128arbiter", theProject, vOptions, sOptions, mOptions);
                    arbiter = new Arbiter(256, false);
                    theProject = arbiter.getProject();
                    runBenchmark("256arbiter", theProject, vOptions, sOptions, mOptions);
                    arbiter = new Arbiter(512, false);
                    theProject = arbiter.getProject();
                    runBenchmark("512arbiter", theProject, vOptions, sOptions, mOptions);
                }
            }
        }
        file.write("BENCHMARKING COMPLETED\n");
        file.flush();
        file.close();
    }
    
    private void runBenchmark(String name, Project theProject,
        VerificationOptions vOptions,
        SynchronizationOptions sOptions,
        MinimizationOptions mOptions)
    {
        try
        {
            System.out.println("  CURRENT BENCHMARK: " + name);
            AutomataVerifier verifier = new AutomataVerifier(theProject, vOptions, sOptions, mOptions);
            ActionTimer timer = new ActionTimer();
            //System.out.println("Verifying...");
            // Run the benchmark!
            timer.start();
            boolean nonblocking = verifier.verify();
            timer.stop();
            {
                // Write to logfile
                String message = verifier.getTheMessage();
                message = message.replaceFirst("NAME", name);
                message = message.replaceFirst("TIME", "" + timer.toStringShort());
                message = message.replaceFirst("BLOCK", "" + nonblocking);
                message = message.replaceFirst(" milliseconds", "m");
                message = message.replaceFirst(" seconds", "s");
                //message = message.replaceAll("_", "\u005c\_");
                message = message.replaceAll("_", "\\_");
                message = message.replaceFirst("ALGO1", mOptions.getMinimizationStrategy().toStringAbbreviated());
                message = message.replaceFirst("ALGO2", mOptions.getMinimizationHeuristic().toStringAbbreviated());
                file.write(message + "\n");
                file.flush();
            }
            System.out.println("  TIME: " + timer + "\n  RESULT (NONBLOCKING/CONTROLLABLE): " + nonblocking);
        }
        catch (Throwable ex)
        {
            System.out.println("Failed! " + ex);
        }
        collectGarbage();
    }
    
    private void collectGarbage()
    {
        // Garbage collect now!
        try
        {
            System.runFinalization();
            System.gc();
            System.out.println("Running garbage collector...");
            System.out.flush();
            Thread.sleep(5000);
        }
        catch (Throwable ex)
        {
            System.out.println("Garbage collection failed! " + ex);
        }
    }
}
