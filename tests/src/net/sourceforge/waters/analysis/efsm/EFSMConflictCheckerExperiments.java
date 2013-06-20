//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMConflictCheckerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

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
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
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
 * This class runs experiments using the {@link EFSMConflictChecker} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani, Robi Malik
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
  //# Test Suite
  private void runAllTests() throws Exception
  {
    runAllTests(new EFSMConflictCheckerWrapper());
    runAllTests(new CompositionalConflictCheckerWrapper());
    runAllTests(new BDDConflictCheckerWrapper());
  }

  private void runAllTests(final ConflictCheckerWrapper wrapper) throws Exception
  {
    mConflictCheckerWrapper = wrapper;
    try {
      testPsl();
      testPslBig();
      testPslBigWithManyRestartTrans();
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
      // next please ...
    }
    try {
      testPslBigBlocking();
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
      // next please ...
    }
    try {
      testPslBigNonblocking();
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
      // next please ...
    }
    for (int m = 2; m <= 10; m += 2) {
      try {
        for (int n = 200; n <= 2000; n+= 200) {
          checkTransferLine("transferline_efsm", n, m, true);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      } catch (final EvalException exception) {
        // next please ...
      }
      try {
        for (int n = 200; n <= 2000; n+= 200) {
          checkTransferLine("transferline_efsm_block", n, m, false);
        }
      } catch (final AnalysisException exception) {
        // next please ...
      } catch (final EvalException exception) {
        // next please ...
      }
    }
    try {
      for (int n = 1000; n <= 4000; n+= 1000) {
        checkPhilosophers("dining_philosophers", n, false);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
      // next please ...
    }
    try {
      testPrimeSieve4();
      testPrimeSieve4b();
      testPrimeSieve6();
      testPrimeSieve7();
      testPrimeSieve8();
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
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
    } catch (final EvalException exception) {
      // next please ...
    }
    try {
      for (int maxseqno = 31; maxseqno <= 255; maxseqno += 32) {
        checkProfisafe("profisafe_islave_efsm", maxseqno, true);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
      // next please ...
    }
    try {
      for (int maxseqno = 31; maxseqno <= 255; maxseqno += 32) {
        checkProfisafe("profisafe_ihost_efsm", maxseqno, true);
      }
    } catch (final AnalysisException exception) {
      // next please ...
    } catch (final EvalException exception) {
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
    mPrintWriter.println("InternalTransitionLimit," +
                         mInternalTransitionLimit);
    mPrintWriter.println("CompositionSelectionHeuristic," +
                         mCompositionSelectionHeuristic);
    mHasBeenPrinted = false;
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
  //# Overrides for net.sourceforge.waters.analysis.efsm.EFSMConflictCheckerTest
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
    throws EvalException, OverflowException
  {
    try {
      final DocumentManager manager = getDocumentManager();
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final ModuleCompiler compiler =
        new ModuleCompiler(manager, factory, module);
      final List<String> none = Collections.emptyList();
      compiler.setEnabledPropertyNames(none);
      mWatchdog.addAbortable(compiler);
      final ProductDESProxy des = compiler.compile(bindings);
      mWatchdog.removeAbortable(compiler);
      return des;
    } catch (final OutOfMemoryError error) {
      System.gc();
      throw new OverflowException(error);
    }
  }


  //#########################################################################
  //# Logging
  private void printAndLog(final String moduleName,
                           final List<ParameterBindingProxy> bindings,
                           final String methodName)
  {
    final StringBuffer buffer = new StringBuffer("Running ");
    buffer.append(moduleName);
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
    buffer.append(" with ");
    buffer.append(methodName);
    buffer.append(" ... ");
    printAndLog(buffer.toString());
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
      String className = ProxyTools.getShortClassName(this);
      if (className.endsWith("Wrapper")) {
        final int len = className.length();
        className = className.substring(0, len - 7);
      }
      printAndLog(moduleName, bindings, className);
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
        mPrintWriter.print(moduleName);
        mPrintWriter.print(',');
        stats.printCSVHorizontal(mPrintWriter);
        mPrintWriter.println();
        return stats.isSatisfied();
      } catch (final Throwable exception) {
        System.out.println(ProxyTools.getShortClassName(exception));
        mPrintWriter.println(moduleName + "," + exception.getMessage());
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
      checker.setBindings(bindings);
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
      mWatchdog.addAbortable(mConflictChecker);
      // Configuration of CompositionalConflictChecker ...
      mConflictChecker.setAbstractionProcedureFactory
        (ConflictAbstractionProcedureFactory.NB);
      mConflictChecker.setPreselectingMethod
        (AbstractCompositionalModelAnalyzer.MustL);
      mConflictChecker.setSelectingMethod
        (AbstractCompositionalModelAnalyzer.MinS);
      mConflictChecker.setInternalStateLimit(10000);
      mConflictChecker.setMonolithicStateLimit(50000000);
      mConflictChecker.setMonolithicTransitionLimit(0);
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
      mConflictChecker.run();
      return mConflictChecker.getAnalysisResult();
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
      mWatchdog.addAbortable(mConflictChecker);
      // Configuration of CompositionalConflictChecker ...
      mConflictChecker.setBDDPackage(BDDPackage.CUDD);
      mConflictChecker.setTransitionPartitioningStrategy
        (TransitionPartitioningStrategy.AUTOMATA);
      mConflictChecker.setPartitioningSizeLimit(5000);
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
      mConflictChecker.run();
      return mConflictChecker.getAnalysisResult();
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

  private final int mTimeout = 1200;  // 20 minutes
  private final int mInternalTransitionLimit = 1000000;
  private final CompositionSelectionHeuristic mCompositionSelectionHeuristic;
}
