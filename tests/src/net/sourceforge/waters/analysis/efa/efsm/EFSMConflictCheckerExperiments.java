//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.bdd.BDDConflictChecker;
import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.analysis.bdd.TransitionPartitioningStrategy;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * This class runs experiments using the {@link EFSMConflictChecker} and
 * other algorithms.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class EFSMConflictCheckerExperiments
  extends EFSMConflictCheckerTest
{

  //#########################################################################
  //# Constructors
  public EFSMConflictCheckerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null);
  }

  public EFSMConflictCheckerExperiments
    (final String statsFilename,
     final SelectionHeuristic<EFSMPair> compositionSelectionHeuristic)
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
        ("USAGE: java " +
         ProxyTools.getShortClassName(EFSMConflictCheckerExperiments.class) +
         "<outputFilename>");
    }
  }


  //#########################################################################
  //# Test Suite
  private void runAllTests() throws Exception
  {
    runAllTests(new EFSMConflictCheckerWrapper());
    runAllTests(new BDDConflictCheckerWrapper());
    runAllTests(new CompositionalConflictCheckerWrapper());
  }

  private void runAllTests(final ConflictCheckerWrapper wrapper)
    throws Exception
  {
    mPrintWriter.println();
    mPrintWriter.println(wrapper.getName());
    mHasBeenPrinted = false;
    mConflictCheckerWrapper = wrapper;

    final SelectionHeuristic<EFSMPair> minV =
      new MinSharedVariablesCompositionSelectionHeuristic();
    final SelectionHeuristic<EFSMPair> minF =
      new MinFrontierCompositionSelectionHeuristic();
    final SelectionHeuristic<EFSMPair> minSync =
      new MinSynchCompositionSelectionHeuristic();
    mCompositionSelectionHeuristic =
      new ChainSelectionHeuristic<EFSMPair>(minV, minF, minSync);

    checkPML("pml3", 2, 3, true); // Dummy call, result to be discarded.
    checkPML("pml3", 2, 3, true);
    for (int n = 2; n <= 10; n+=2) {
      try {
        for (int c = 3; c <= 6; c+=3) {
          checkPML("pml3", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    for (int c = 6; c <= 6; c += 2) {
      try {
        for (int n = 2; n <= 100; n += 2) {
          checkPML("pml3", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    for (int c = 6; c <= 6; c += 2) {
      try {
        for (int n = 2; n <= 2; n += 2) {
          checkPML("pml7", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    for (int c = 6; c <= 6; c += 2) {
      try {
        for (int n = 2; n <= 2; n += 2) {
          checkPML("pml8", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    for (int c = 6; c <= 6; c += 2) {
      try {
        for (int n = 2; n <= 2; n += 2) {
          checkPML("pml9", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    for (int c = 6; c <= 6; c += 2) {
      try {
        for (int n = 2; n <= 2; n += 2) {
          checkPML("pml10", c, n, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    try {
      testPsl();
      testPslBig();
      testPslBigWithManyRestartTrans();
    } catch (final AnalysisException exception) {
      // next please ...
    }
    try {
      testPslBigBlocking();
    } catch (final AnalysisException exception) {
      // next please ...
    }
    try {
      testPslBigNonblocking();
    } catch (final AnalysisException exception) {
      // next please ...
    }

    if (!(wrapper instanceof BDDConflictCheckerWrapper)
         && !(wrapper instanceof EFSMConflictCheckerWrapper )) {
      for (int m = 3; m <= 5; m += 1) {
        try {
          for (int n = 1; n <= 3; n+= 2) {
            checkTransferLineRework("transferline_efsm_rework", m, n, false);
          }
        } catch (final AnalysisException exception) {
          // next please ...
        }
      }
       }
    if (!(wrapper instanceof BDDConflictCheckerWrapper)) {
      for (int m = 3; m <= 5; m += 1) {
        try {
          for (int n = 1; n <= 3; n+= 2) {
            checkTransferLineRework("transferline_efsm_rework_block", m, n, false);
          }
        } catch (final AnalysisException exception) {
          // next please ...
        }
      }
      try {
        for (int n = 1000; n <= 4000; n+= 1000) {
          checkPhilosophers("dining_philosophers", n, false);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      }
    }
    try {
      testPrimeSieve4();
      testPrimeSieve4b();
      if (!(wrapper instanceof BDDConflictCheckerWrapper)) {
        testPrimeSieve5();
        testPrimeSieve6();
        testPrimeSieve7();
        testPrimeSieve8();
      }
    } catch (final AnalysisException exception) {
      // next please ...
    }
    try {
      final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
      for (int s = 3; s < primes.length; s++) {
        final int maxval = primes[s] * primes[s] - 1;
        checkPrimeSieve("dynamic_prime_sieve", s, maxval, true);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    }
    try {
      for (int maxseqno = 31; maxseqno <= 255; maxseqno += 32) {
        checkProfisafe("profisafe_islave_efsm", maxseqno, true);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    }
    try {
      for (int maxseqno = 31; maxseqno <= 255; maxseqno += 32) {
        checkProfisafe("profisafe_ihost_efsm", maxseqno, true);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    }
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
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mWatchdog = new Watchdog(mTimeout);
    mPrintWriter = new PrintWriter(mOut, true);
    mPrintWriter.println("Timeout," + mTimeout);
    /*
    mPrintWriter.println("CompositionSelectionHeuristic," +
                         mCompositionSelectionHeuristic);
    */
    mWatchdog.start();
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
  //# Overrides for net.sourceforge.waters.analysis.efa.efsm.EFSMConflictCheckerTest
  @Override
  boolean checkConflict(final ModuleProxy module,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expected)
    throws EvalException, AnalysisException
  {
    return mConflictCheckerWrapper.run(module, bindings, expected);
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy compile(final ModuleProxy module,
                                  final List<ParameterBindingProxy> bindings)
    throws AnalysisException
  {
    try {
      final DocumentManager manager = getDocumentManager();
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final ModuleCompiler compiler =
        new ModuleCompiler(manager, factory, module);
      final List<String> none = Collections.emptyList();
      compiler.setEnabledPropertyNames(none);
      mWatchdog.addAbortable(compiler);
      final long start = System.currentTimeMillis();
      compiler.setParameterBindings(bindings);
      final ProductDESProxy des = compiler.compile();
      final long finish = System.currentTimeMillis()-start;
      System.out.println("events:"+des.getEvents().size());
      System.out.println("flatten time:"+ finish);
      mWatchdog.removeAbortable(compiler);
      return des;
    } catch (final OutOfMemoryError error) {
      System.gc();
      throw new OverflowException(error);
    }
  }


  //#########################################################################
  //# Logging
  private String getFullModuleName(final String moduleName,
                                   final List<ParameterBindingProxy> bindings)
  {
    final StringBuffer buffer = new StringBuffer(moduleName);
    if (bindings != null) {
      buffer.append('<');
      final Iterator<ParameterBindingProxy> iter = bindings.iterator();
      while (iter.hasNext()) {
        final ParameterBindingProxy binding = iter.next();
        buffer.append(binding.getName());
        buffer.append('=');
        buffer.append(binding.getExpression());
        if (iter.hasNext()) {
          buffer.append(',');
        } else {
          buffer.append('>');
        }
      }
    }
    return buffer.toString();
  }

  private void printAndLog(final String msg)
  {
    System.out.print(msg);
    getLogger().info(msg);
  }


  //#########################################################################
  //# Inner Class ConflictCheckerWrapper
  private abstract class ConflictCheckerWrapper
  {
    //#######################################################################
    //# Inner Class ConflictCheckerWrapper
    private boolean run(final ModuleProxy module,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expected)
      throws EvalException, AnalysisException
    {
      final String moduleName = module.getName();
      final String fullModuleName = getFullModuleName(moduleName, bindings);
      final String className = getName();
      printAndLog("Running " + fullModuleName + " with " + className + " ... ");
      try {
        mWatchdog.reset();
        final long start = System.currentTimeMillis();
        final AnalysisResult stats = runConflictChecker(module, bindings);
        final long stop = System.currentTimeMillis();
        @SuppressWarnings("resource")
        final Formatter formatter = new Formatter(System.out);
        final float seconds = 0.001f * (stop - start);
        formatter.format("%.3fs\n", seconds);
        if (!mHasBeenPrinted) {
          mHasBeenPrinted = true;
          mPrintWriter.print("Model,");
          stats.printCSVHorizontalHeadings(mPrintWriter);
          mPrintWriter.println();
        }
        mPrintWriter.print('\"');
        mPrintWriter.print(fullModuleName);
        mPrintWriter.print("\",");
        stats.printCSVHorizontal(mPrintWriter);
        mPrintWriter.println();
        return stats.isSatisfied();
      } catch (final Throwable exception) {
        System.out.println(ProxyTools.getShortClassName(exception));
        mPrintWriter.println("\"" + fullModuleName + "\"," +
                             exception.getMessage());
        if (exception instanceof AnalysisException) {
          throw (AnalysisException) exception;
        } else if (exception instanceof EvalException) {
          throw (EvalException) exception;
        } else if (exception instanceof RuntimeException) {
          throw (RuntimeException) exception;
        } else {
          throw new WatersRuntimeException(exception);
        }
      }
    }

    private String getName()
    {
      final String className = ProxyTools.getShortClassName(this);
      if (className.endsWith("Wrapper")) {
        final int len = className.length();
        return className.substring(0, len - 7);
      } else {
        return className;
      }
    }


    //#########################################################################
    //# Abstract Methods
    abstract AnalysisResult runConflictChecker
      (ModuleProxy module, List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException;
  }


  //#########################################################################
  //# Inner Class EFSMConflictCheckerWrapper
  private class EFSMConflictCheckerWrapper extends ConflictCheckerWrapper
  {

    //#######################################################################
    //# Overrides for ConflictCheckerWrapper
    @Override
    AnalysisResult runConflictChecker(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException
    {
      final ModuleProxyFactory factory = getModuleProxyFactory();
      final EFSMConflictChecker checker =
        new EFSMConflictChecker(module, factory);
      // Configuration of EFSMConflictChecker ...
      checker.setInternalTransitionLimit(mInternalTransitionLimit);
      if (mCompositionSelectionHeuristic != null) {
        checker.setCompositionSelectionHeuristic(mCompositionSelectionHeuristic);
      }
      // Configuration end
      checker.setParameterBindings(bindings);
      mWatchdog.addAbortable(checker);
      checker.run();
      mWatchdog.removeAbortable(checker);
      return checker.getAnalysisResult();
    }

  }


  //#########################################################################
  //# Inner Class CompositionalConflictCheckerWrapper
  private class CompositionalConflictCheckerWrapper
    extends ConflictCheckerWrapper
  {

    //#######################################################################
    //# Constructor
    private CompositionalConflictCheckerWrapper()
    {
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      mConflictChecker =  new CompositionalConflictChecker(factory);
      // Configuration of CompositionalConflictChecker ...
      mConflictChecker.setAbstractionProcedureCreator
        (ConflictAbstractionProcedureFactory.NBA);
      mConflictChecker.setPreselectingMethod
        (AbstractCompositionalModelAnalyzer.MustL);
      mConflictChecker.setSelectionHeuristic
        (CompositionalSelectionHeuristicFactory.MinF);
      /*
       * To remove zigzag from pml plot MinSync is added.
      mConflictChecker.setSelectionHeuristic
        (CompositionalSelectionHeuristicFactory.MinSync);

       */
      mConflictChecker.setInternalStateLimit(100000);
      mConflictChecker.setMonolithicStateLimit(50000000);
      mConflictChecker.setMonolithicTransitionLimit(0);
      mConflictChecker.setDetailedOutputEnabled(false);
      // Configuration end
    }

    //#######################################################################
    //# Overrides for ConflictCheckerWrapper
    @Override
    AnalysisResult runConflictChecker(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException
    {
      final ProductDESProxy des = compile(module, bindings);
      mConflictChecker.setModel(des);
      mWatchdog.addAbortable(mConflictChecker);
      mConflictChecker.run();
      final AnalysisResult result = mConflictChecker.getAnalysisResult();
      mWatchdog.removeAbortable(mConflictChecker);
      return result;
    }

    //#######################################################################
    //# Data Members
    private final CompositionalConflictChecker mConflictChecker;
  }


  //#########################################################################
  //# Inner Class BDDConflictCheckerWrapper
  private class BDDConflictCheckerWrapper
    extends ConflictCheckerWrapper
  {

    //#######################################################################
    //# Constructor
    private BDDConflictCheckerWrapper()
    {
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      mConflictChecker =  new BDDConflictChecker(factory);
      // Configuration of CompositionalConflictChecker ...
      mConflictChecker.setBDDPackage(BDDPackage.CUDD);
      mConflictChecker.setTransitionPartitioningStrategy
        (TransitionPartitioningStrategy.AUTOMATA);
      mConflictChecker.setPartitioningSizeLimit(5000);
      mConflictChecker.setNodeLimit(25000000);
      mConflictChecker.setDetailedOutputEnabled(false);
      // Configuration end
    }

    //#######################################################################
    //# Overrides for ConflictCheckerWrapper
    @Override
    AnalysisResult runConflictChecker(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException
    {
      final ProductDESProxy des = compile(module, bindings);
      mConflictChecker.setModel(des);
      mWatchdog.addAbortable(mConflictChecker);
      mConflictChecker.run();
      final AnalysisResult result = mConflictChecker.getAnalysisResult();
      mWatchdog.removeAbortable(mConflictChecker);
      return result;
    }

    //#######################################################################
    //# Data Members
    private final BDDConflictChecker mConflictChecker;
  }


  //#########################################################################
  //# Data Members
  private ConflictCheckerWrapper mConflictCheckerWrapper;
  private Watchdog mWatchdog;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;

  private final int mTimeout = 3000;  // 20 minutes
  private final int mInternalTransitionLimit = 1000000;
  private SelectionHeuristic<EFSMPair> mCompositionSelectionHeuristic;

}
