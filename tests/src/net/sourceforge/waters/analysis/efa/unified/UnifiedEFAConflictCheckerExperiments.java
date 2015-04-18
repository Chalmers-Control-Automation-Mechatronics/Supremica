//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAConflictCheckerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.bdd.BDDConflictChecker;
import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.analysis.bdd.TransitionPartitioningStrategy;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.compositional.Candidate;
import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.efa.efsm.EFSMConflictChecker;
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

public class UnifiedEFAConflictCheckerExperiments
  extends UnifiedEFAConflictCheckerTest
{

  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    try {
      final UnifiedEFAConflictCheckerExperiments experiment =
        new UnifiedEFAConflictCheckerExperiments();
      experiment.setUp();
      experiment.runAllTests();
      experiment.tearDown();
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR");
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Constructors
  public UnifiedEFAConflictCheckerExperiments()
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
  }


  //#########################################################################
  //# Test Suite
  private void runAllTests() throws Exception
  {
    final long start = System.currentTimeMillis();
    final ConflictCheckerWrapper bddWrapper = new BDDConflictCheckerWrapper();
    bddWrapper.runAllTests();
    for (final SelectionHeuristic<UnifiedEFACandidate>
         candidateSelectionHeuristic : CANDIDATE_SELECTION_HEURISTICS) {
      for (final SelectionHeuristic<UnifiedEFAVariable>
           variableSelectionHeuristic : VARIABLE_SELECTION_HEURISTICS) {
        final ConflictCheckerWrapper unifiedWrapper =
          new UnifiedEFAConflictCheckerWrapper(candidateSelectionHeuristic,
                                               variableSelectionHeuristic);
        unifiedWrapper.runAllTests();
      }
    }
    final ConflictCheckerWrapper compWrapper =
      new CompositionalConflictCheckerWrapper();
    compWrapper.runAllTests();
    final long finish = System.currentTimeMillis();
    printAndLog(start, finish);
  }

  private void runAllTests(final ConflictCheckerWrapper wrapper)
    throws Exception
  {
    mConflictCheckerWrapper = wrapper;
    for (int c = 2; c <= 3; c += 1) {
      try {
        for (int n = 50; n <= 50; n += 5) {
          checkPML("pml3", c, n, true);
//          checkPML("pml4", c, n, true);
        }
      } catch (final AnalysisException | EvalException exception) {
        // next please ...
      }
    }

    try {
      testPrimeSieve4b();
      testPrimeSieve4();
      if (!(wrapper instanceof BDDConflictCheckerWrapper)) {
//        testPrimeSieve5();
        // CUDD locks up after 56 iterations in relprod (?)
        testPrimeSieve6();
//        testPrimeSieve7();
        testPrimeSieve8();
      }
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }

    try {
//      testCaseStudy();
//      testCaseStudyNonblocking();
      testProductionCell();
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
    try {
      for (int maxseqno = 127; maxseqno <= 255; maxseqno += 128) {
        checkProfisafe("profisafe_ihost_efa_0b", maxseqno, false);
      }
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
    try {
      testPslBig();
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }

    try {
      testPslBigBlocking();
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
    try {
      testPslBigNonblocking();
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
    try {
      testPslBigWithManyRestartTrans();
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
    if (!(wrapper instanceof BDDConflictCheckerWrapper)) {

      for (int r = 3; r <= 4; r += 1) {
        try {
          for (int n = 500; n <= 500; n+= 100) {
            checkTransferLineRework("transferline_efsm_rework_block", r, n, false);
          }
        } catch (final AnalysisException | EvalException exception) {
          // next please ...
        }
      }
      for (int r = 3; r <= 4; r += 1) {
        try {
          for (int n = 500; n <= 500; n+= 100) {
            checkTransferLineRework("transferline_efsm_rework", r, n, true);
          }
        } catch (final AnalysisException | EvalException exception) {
          // next please ...
        }
      }
//      try {
//        for (int n = 200; n <= 1000; n+= 200) {
//          checkPhilosophers("dining_philosophers", n, false);
//        }
//      } catch (final AnalysisException exception) {
//        // next please ...
//      } catch (final EvalException exception) {
//        // next please ...
//      }
    }

 //    try {
//      final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
//      for (int s = 3; s < primes.length; s++) {
//        final int maxval = primes[s] * primes[s] - 1;
//        checkPrimeSieve("dynamic_prime_sieve", s, maxval, true);
//      }
//    } catch (final AnalysisException | EvalException exception) {
//      // next please ...
//    }
//    try {
//      for (int maxseqno = 15; maxseqno <= 255; maxseqno += 16) {
//        checkProfisafe("profisafe_ihost_efa_2", maxseqno, false);
//      }
//    } catch (final AnalysisException | EvalException exception) {
//      // next please ...
//    }

//    try {
//      for (int maxseqno = 127; maxseqno <= 255; maxseqno += 128) {
//        checkProfisafe("profisafe_islave_efa", maxseqno, true);
//      }
//    } catch (final AnalysisException | EvalException exception) {
//      // next please ...
//    }
    try {
      for (int i = 300; i <= 400; i += 100) {
        checkRoundRobin(i);
      }
    } catch (final AnalysisException | EvalException exception) {
      // next please ...
    }
//    try {
//      for (int n = 0; n <= 1000; n += 50) {
//        final int nn = n == 0 ? 1 : n;
//        checkJDEDS2014(10, nn);
//      }
//    } catch (final AnalysisException | EvalException exception) {
//      // next please ...
//    }
  }


  //#########################################################################
  //# Tests
  @SuppressWarnings("unused")
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
    mWatchdog.setVerbose(true);
    mWatchdog.start();
  }

  @Override
  protected void tearDown() throws Exception
  {
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
  private String getFullModuleName(final String moduleName,
                                   final List<ParameterBindingProxy> bindings)
  {
    final StringBuilder buffer = new StringBuilder(moduleName);
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

  private void printAndLog(final long start, final long finish)
  {
    final long duration = (finish - start) / 1000;
    final int seconds = (int) (duration % 60);
    final int minutes = (int) ((duration / 60) % 60);
    final int hours = (int) (duration / 3600);
    final String msg =
      String.format("Completed in %d:%02d:%02d", hours, minutes, seconds);
    printAndLog(msg);
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
    void printHeader()
    {
      mPrintWriter.println(getName());
      mPrintWriter.println("Timeout," + mTimeout);
    }

    void runAllTests() throws Exception
    {
      final String outputProp = System.getProperty("waters.test.outputdir");
      final String fileName = getFileName() + ".csv";
      final File statsFile = new File(outputProp, fileName);
      mOut = new FileOutputStream(statsFile);
      mPrintWriter = new PrintWriter(mOut, true);
      try {
        printHeader();
        UnifiedEFAConflictCheckerExperiments.this.runAllTests(this);
      } finally {
        mPrintWriter.close();
      }
    }

    private boolean run(final ModuleProxy module,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expected)
      throws EvalException, AnalysisException
    {
      final String moduleName = module.getName();
      final String fullModuleName = getFullModuleName(moduleName, bindings);
      final String fileName = getFileName();
      printAndLog("Running " + fullModuleName + " with " + fileName + " ... ");
      try {
        System.gc();
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
        mPrintWriter.print('"');
        mPrintWriter.print(fullModuleName);
        mPrintWriter.print("\",");
        stats.printCSVHorizontal(mPrintWriter);
        mPrintWriter.println();
        return stats.isSatisfied();
      } catch (final Throwable exception) {
        System.out.println(ProxyTools.getShortClassName(exception));
        if (exception.getMessage() != null) {
          mPrintWriter.println('\"' + fullModuleName + "\"," +
                               exception.getMessage());
        }
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

    String getName()
    {
      final String className = ProxyTools.getShortClassName(this);
      if (className.endsWith("Wrapper")) {
        final int len = className.length();
        return className.substring(0, len - 7);
      } else {
        return className;
      }
    }

    String getFileName()
    {
      return getName();
    }

    PrintWriter getPrintWriter()
    {
      return mPrintWriter;
    }

    //#########################################################################
    //# Abstract Methods
    abstract AnalysisResult runConflictChecker
      (ModuleProxy module, List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException;

    //#########################################################################
    //# Data Members
    private FileOutputStream mOut;
    private PrintWriter mPrintWriter;
    private boolean mHasBeenPrinted = false;
  }


  //#########################################################################
  //# Inner Class UnifiedEFAConflictCheckerWrapper
  private class UnifiedEFAConflictCheckerWrapper
    extends ConflictCheckerWrapper
  {

    //#######################################################################
    //# Constructor
    private UnifiedEFAConflictCheckerWrapper()
    {
      this(CHAIN_MINF, CHAIN_MAXE);
    }

    private UnifiedEFAConflictCheckerWrapper
      (final SelectionHeuristic<UnifiedEFACandidate> compositionSelectionHeuristic,
       final SelectionHeuristic<UnifiedEFAVariable> variableSelectionHeuristic)
    {
      mCompositionSelectionHeuristic = compositionSelectionHeuristic;
      mVariableSelectionHeuristic = variableSelectionHeuristic;
    }

    //#######################################################################
    //# Overrides for ConflictCheckerWrapper
    @Override
    String getFileName()
    {
      return super.getFileName() + "_" +
        mCompositionSelectionHeuristic.getName() + "_" +
        mVariableSelectionHeuristic.getName();
    }

    @Override
    void printHeader()
    {
      super.printHeader();
      final PrintWriter writer = getPrintWriter();
      writer.println("Composition selection heuristic," +
                     mCompositionSelectionHeuristic);
      writer.println("Variable selection heuristic," +
                     mVariableSelectionHeuristic);
    }

    @Override
    AnalysisResult runConflictChecker(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException
    {
      final ModuleProxyFactory factory = getModuleProxyFactory();
      final UnifiedEFAConflictChecker checker =
        new UnifiedEFAConflictChecker(module, factory);
      checker.setCompositionSelectionHeuristic(mCompositionSelectionHeuristic);
      checker.setVariableSelectionHeuristic(mVariableSelectionHeuristic);
      // Configuration of UnifiedEFAConflictChecker ...
      checker.setUsesLocalVariable(true);
      // Configuration end
      checker.setBindings(bindings);
      mWatchdog.addAbortable(checker);
      checker.run();
      mWatchdog.removeAbortable(checker);
      return checker.getAnalysisResult();
    }

    //#######################################################################
    //# Data Members
    private final SelectionHeuristic<UnifiedEFACandidate> mCompositionSelectionHeuristic;
    private final SelectionHeuristic<UnifiedEFAVariable> mVariableSelectionHeuristic;
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
      mConflictChecker.setAbstractionProcedureCreator
        (ConflictAbstractionProcedureFactory.NB);
      mConflictChecker.setPreselectingMethod
        (AbstractCompositionalModelAnalyzer.MustL);
      final SelectionHeuristic<Candidate> chain =
        CompositionalSelectionHeuristicFactory.createChainHeuristic
          (CompositionalSelectionHeuristicFactory.MinF,
           CompositionalSelectionHeuristicFactory.MinS,
           CompositionalSelectionHeuristicFactory.MaxL,
           CompositionalSelectionHeuristicFactory.MaxC,
           CompositionalSelectionHeuristicFactory.MinE);
      mConflictChecker.setSelectionHeuristic(chain);
      mConflictChecker.setInternalStateLimit(10000);
      mConflictChecker.setMonolithicStateLimit(50000000);
      mConflictChecker.setMonolithicTransitionLimit(0);
      mConflictChecker.setCounterExampleEnabled(false);
      // Configuration end
    }

    //#######################################################################
    //# Overrides for ConflictCheckerWrapper
    @Override
    AnalysisResult runConflictChecker(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
      throws EvalException, AnalysisException
    {
      final long start = System.currentTimeMillis();
      final ProductDESProxy des = compile(module, bindings);
      final long finish = System.currentTimeMillis();
      mConflictChecker.setModel(des);
      mConflictChecker.run();
      final CompositionalAnalysisResult result =
        mConflictChecker.getAnalysisResult();
      result.setCompileTime(finish - start);
      return result;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public void finalize()
    {
      mWatchdog.removeAbortable(mConflictChecker);
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
      // Configuration of BDDConflictChecker ...
      mConflictChecker.setBDDPackage(BDDPackage.CUDD);
      mConflictChecker.setTransitionPartitioningStrategy
        (TransitionPartitioningStrategy.AUTOMATA);
      mConflictChecker.setPartitioningSizeLimit(5000);
      mConflictChecker.setInitialSize(1000000);
      mConflictChecker.setNodeLimit(25000000);
      mConflictChecker.setCounterExampleEnabled(false);
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
    //# Overrides for java.lang.Object
    @Override
    public void finalize()
    {
      mWatchdog.removeAbortable(mConflictChecker);
    }

    //#######################################################################
    //# Data Members
    private final BDDConflictChecker mConflictChecker;
  }


  //#########################################################################
  //# Data Members
  private ConflictCheckerWrapper mConflictCheckerWrapper;
  private Watchdog mWatchdog;

  private final int mTimeout = 600;  // measured in seconds


  //#########################################################################
  //# Class Constants
  private static final SelectionHeuristic<UnifiedEFACandidate> CHAIN_MINS;
  private static final SelectionHeuristic<UnifiedEFACandidate> CHAIN_MINF;
  private static final List<SelectionHeuristic<UnifiedEFACandidate>>
    CANDIDATE_SELECTION_HEURISTICS;

  private static final SelectionHeuristic<UnifiedEFAVariable> CHAIN_MAXE;
  private static final SelectionHeuristic<UnifiedEFAVariable> CHAIN_MAXS;
  private static final SelectionHeuristic<UnifiedEFAVariable> CHAIN_MIND;
  private static final List<SelectionHeuristic<UnifiedEFAVariable>>
    VARIABLE_SELECTION_HEURISTICS;


  static {
    final SelectionHeuristic<UnifiedEFACandidate> minF =
      new CompositionSelectionHeuristicMinF1();
    final SelectionHeuristic<UnifiedEFACandidate> minS =
      new CompositionSelectionHeuristicMinS();
    CHAIN_MINF = new ChainSelectionHeuristic<UnifiedEFACandidate>(minF, minS);
    CHAIN_MINS = new ChainSelectionHeuristic<UnifiedEFACandidate>(minS, minF);
    CANDIDATE_SELECTION_HEURISTICS = new ArrayList<>(2);
    CANDIDATE_SELECTION_HEURISTICS.add(CHAIN_MINF);
    CANDIDATE_SELECTION_HEURISTICS.add(CHAIN_MINS);

    final SelectionHeuristic<UnifiedEFAVariable> maxE =
      new VariableSelectionHeuristicMaxE();
    final SelectionHeuristic<UnifiedEFAVariable> maxS =
      new VariableSelectionHeuristicMaxS();
    final SelectionHeuristic<UnifiedEFAVariable> minD =
      new VariableSelectionHeuristicMinD();
    CHAIN_MAXE =
      new ChainSelectionHeuristic<UnifiedEFAVariable>(maxE, maxS, minD);
    CHAIN_MAXS =
      new ChainSelectionHeuristic<UnifiedEFAVariable>(maxS, maxE, minD);
    CHAIN_MIND =
      new ChainSelectionHeuristic<UnifiedEFAVariable>(minD, maxE, maxS);
    VARIABLE_SELECTION_HEURISTICS = new ArrayList<>(3);
    VARIABLE_SELECTION_HEURISTICS.add(CHAIN_MAXE);
    VARIABLE_SELECTION_HEURISTICS.add(CHAIN_MAXS);
    VARIABLE_SELECTION_HEURISTICS.add(CHAIN_MIND);
  }

}