//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * This class runs experiments using the {@link EFSMConflictChecker} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */

public class EFSMConflictCheckerExperiments
  extends EFSMConflictCheckerTest
{

  //#########################################################################
  //# Constructor
  public EFSMConflictCheckerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null);
  }

  public EFSMConflictCheckerExperiments
    (final String statsFilename,
     final CompositionSelectionHeuristic compositionSelectionHeuristic)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mCompositionSelectionHeuristic = compositionSelectionHeuristic;
  }


  //#########################################################################
  //# Test Suite
  private void runAllTests() throws Exception
  {
    testPrimeSieve4();
    testPrimeSieve4b();
    testPrimeSieve6();
    testPrimeSieve7();
    testPrimeSieve8();
    testPslBig();
    testPsl();
    //testPslWithResetTrans();
    checkPhilosophers("dining_philosophers", 1000, false);
    checkPhilosophers("dining_philosophers", 2000, false);
    checkPhilosophers("dining_philosophers", 4000, false);
    checkTransferLine("transferline_efsm", 500, 10, true);
    checkTransferLine("transferline_efsm", 1000, 10, true);
    checkTransferLine("transferline_efsm", 2000, 10, true);
  }


  //#########################################################################
  //# Tests
  private void testPrimeSieve7()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve7");
    checkConflict(module, true);
  }

  private void testPrimeSieve8()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve8");
    checkConflict(module, true);
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 1) {
      try {
        final String filename = args[0];
        final EFSMConflictCheckerExperiments experiment =
          new EFSMConflictCheckerExperiments(filename);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err.println
        ("USAGE: " +
         ProxyTools.getShortClassName(EFSMConflictCheckerExperiments.class) +
         "<outputFilename>");
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    mPrintWriter.println("InternalTransitionLimit," +
                         mInternalTransitionLimit);
    mPrintWriter.println("CompositionSelectionHeuristic," +
                         mCompositionSelectionHeuristic);
    mHasBeenPrinted = false;
  }

  @Override
  protected void tearDown() throws Exception
  {
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.efsm.EFSMConflictCheckerTest
  @Override
  boolean checkConflict(final ModuleProxy module,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expected)
    throws EvalException, AnalysisException
  {
    final String name = module.getName();
    printAndLog("Running " + name + " with " +
                mCompositionSelectionHeuristic + " ...");
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final EFSMConflictChecker conflictChecker =
      new EFSMConflictChecker(module, factory);
    configure(conflictChecker);
    conflictChecker.setBindings(bindings);
    try {
      return conflictChecker.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
      return false;
    } finally {
      final EFSMConflictCheckerAnalysisResult stats =
        conflictChecker.getAnalysisResult();
      if (!mHasBeenPrinted) {
        mHasBeenPrinted = true;
        mPrintWriter.print("Model,");
        stats.printCSVHorizontalHeadings(mPrintWriter);
        mPrintWriter.println();
      }
      mPrintWriter.print(name);
      mPrintWriter.print(',');
      stats.printCSVHorizontal(mPrintWriter);
      mPrintWriter.println();
    }
  }

  @Override
  void configure(final EFSMConflictChecker checker)
  {
    // final int internalStateLimit = 5000;
    // mConflictChecker.setInternalStateLimit(internalStateLimit);
    checker.setInternalTransitionLimit(mInternalTransitionLimit);
    // final int finalStateLimit = 2000000;
    // mConflictChecker.setMonolithicStateLimit(finalStateLimit);
    if (mCompositionSelectionHeuristic != null) {
      checker.setCompositionSelectionHeuristic(mCompositionSelectionHeuristic);
    }
  }


  //#########################################################################
  //# Logging
  private void printAndLog(final String msg)
  {
    System.out.println(msg);
    getLogger().info(msg);
  }


  //#########################################################################
  //# Data Members
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;

  private final int mInternalTransitionLimit = 1000000;
  private final CompositionSelectionHeuristic mCompositionSelectionHeuristic;
}
