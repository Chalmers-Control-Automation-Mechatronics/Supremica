//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * This class runs experiments using the {@link CompositionalAutomataSynthesizer} with
 * a variety of configurations. The heuristics for choosing candidates can be
 * varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani, Fangqian Qiu, Robi Malik
 */

public class ModularAndCompositionalSynthesizerExperiments extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public ModularAndCompositionalSynthesizerExperiments
    (final ModularAndCompositionalSynthesizer synthesizer,
     final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, synthesizer, null, null);
  }

  public ModularAndCompositionalSynthesizerExperiments
    (final String statsFilename,
     final ModularAndCompositionalSynthesizer synthesizer,
     final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingHeuristic,
     final SelectionHeuristicCreator selectionHeuristic)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mSynthesizer = synthesizer;
    mPreselecting = preselectingHeuristic;
    mSelecting = selectionHeuristic;
    mWatchdog = new Watchdog(mSynthesizer, mTimeout);
    mWatchdog.setVerbose(true);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    final int internalStateLimit = 5000;
    mSynthesizer.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 1000000;
    mSynthesizer.setMonolithicStateLimit(finalStateLimit);
    final int finalTransitionLimit = 5000000;
    mSynthesizer.setMonolithicTransitionLimit(finalTransitionLimit);
    mSynthesizer.setPreselectingMethod(mPreselecting);
    mSynthesizer.setSelectionHeuristic(mSelecting);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit +
                         ",InternalTransitionLimit," + internalTransitionLimit +
                         ",FinalStateLimit," + finalStateLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting +
                         ",SelecHeuristic," + mSelecting );
    // mPrintWriter.println("SupervisorReduction," + mSupervisorReductionEnabled);
    mHasBeenPrinted = false;
    mSynthesizer.setSupervisorReductionEnabled(true);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mSynthesizer = null;
    mPrintWriter.close();
    mOut.close();
    mWatchdog.terminate();
    System.out.println("All experiments complete");
    super.tearDown();
  }

  //#########################################################################
  //# Simple Access
  ModularAndCompositionalSynthesizer getSynthesizer()
  {
    return mSynthesizer;
  }

  //#########################################################################
  //# Configuration
  void setPreselectingHeuristic(final String name)
  {
    final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory factory =
      mSynthesizer.getPreselectingMethodFactory();
    mPreselecting = factory.getEnumValue(name);

  }

  /**
   * Sets the selecting heuristic with the given name
   */
  void setSelectingHeuristic(final String name)
  {
    final CompositionalSelectionHeuristicFactory factory =
      mSynthesizer.getSelectionHeuristicFactory();
    mSelecting = factory.getEnumValue(name);
  }

  void setTimeOut(final int timeout)
  {
    mTimeout = timeout;
  }


  //#########################################################################
  //# Invocation
  boolean runModel(final String group, final String subdir, final String name)
    throws Exception
  {
    return runModel(group, subdir, name, null);
  }

  boolean runModel(final String group, final String subdir, final String name,
                final List<ParameterBindingProxy> bindings) throws Exception
  {
    printAndLog("Running " + name + " with " + mPreselecting + "/"
                + mSelecting + " ... ", false);
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    File dir = new File(rootdir, group);
    if (subdir != null) {
      dir = new File(dir, subdir);
    }
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    mSynthesizer.setModel(des);
    String answer = null;
    final long start = System.currentTimeMillis();
    try {
      mWatchdog.reset();
      final boolean result = mSynthesizer.run();
      answer = Boolean.toString(result);
      return true;
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
      answer = ProxyTools.getShortClassName(exception);
      return false;
    } catch (final Throwable exception) {
      answer = ProxyTools.getShortClassName(exception);
      return false;
    } finally {
      final long stop = System.currentTimeMillis();
      final float difftime = 0.001f * (stop - start);
      final String msg = String.format("%s (%.3fs)", answer, difftime);
      printAndLog(msg, true);
      final ProductDESResult stats =
        mSynthesizer.getAnalysisResult();
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


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 3) {
      try {
        final ProductDESProxyFactory factory =
          ProductDESElementFactory.getInstance();
        final ModularAndCompositionalSynthesizer synthesizer;
        final String filename = args[0];
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        synthesizer = new ModularAndCompositionalSynthesizer(factory);
        final ModularAndCompositionalSynthesizerExperiments experiment =
          new ModularAndCompositionalSynthesizerExperiments(synthesizer, filename);
        experiment.setPreselectingHeuristic(preselectingHeuristic);
        experiment.setSelectingHeuristic(selectingHeuristic);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err.println("Usage: ModularAndCompositionalSynthesizerExperiments " +
                         "<outputFilename> " +
                         "<preselectingHeuristic> " +
                         "<selectingHeuristic>");
    }
  }

  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    mWatchdog.start();
    synthesisAGV();// 1
    synthesisAGVB();// 2
    synthesissAip0Alps();// 3
//    synthesiseAip0though();
    synthesisFenCaiWon09B();// 4
    synthesisFenCaiWon09Synth();// 5
    synthesisFms2003();// 6
    synthesisePSLBig();
    synthesisePSLBigWithManyRestartTrans();
    synthesisePSLWithResetTrans();
    synthesisePSLWithResetTransWithPartLeftCounters();
    synthesisePSLWithResetTransWithPartLeftPlants();
//    synthesiseTbedCtct(); No supervisor
    synthesiseTbedHISC();
    synthesiseTbedNoderailB();// 7
    synthesiseTbedNoderailUncont();// 8
    synthesiseCentralLockingVerriegel3b();// 9
    synthesiseVerrigel4B();// 10
    synthesis6linka();// 11
    synthesis6linki();// 12
    synthesis6linkp();// 13
    synthesis6linkre();// 14
//    for (int n = 100; n <= 1000; n+=100) {
//      if (!synthesisTransferline(n)) {
//        break;
//      }
//    }


    //synthesiseCentralLockingKoordwspBlock();
    //synthesissRhoneSubPatch0();
    //synthesissAip0Aip();
    //synthesiseFischertechnik();
    //synthesiseIPC();
    //synthesiseCentralLockingKoordwspBlock();
    //synthesisAip0tough();
    //synthesiseTbedCtct();
    //synthesiseFlexibleManufacturingSystem();
    //synthesisLargestCoherent();
  }

  //#########################################################################
  //# Models
  // Central locking
  @SuppressWarnings("unused")
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp_block.wmod");
  }

  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3b.wmod");
  }

  private void synthesiseVerrigel4B() throws Exception
  {
    runModel("tests", "incremental_suite", "verriegel4b.wmod");
  }

  // AIP
  private void synthesissAip0Alps() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0alps.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseAip0Aip() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0aip.wmod");
  }

  // This one cannot be solved yet ...
  @SuppressWarnings("unused")
  private void synthesiseAip0though() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0tough.wmod");
  }

  // Train testbed
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_uncont.wmod");
  }

  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_noderailb.wmod");
  }

  // This one does not have a solution.
  @SuppressWarnings("unused")
  private void synthesiseTbedCtct() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_ctct.wmod");
  }


  private void synthesiseTbedHISC() throws Exception
  {
    runModel("despot", "tbed_hisc", "tbed_hisc1.wmod");
  }

  //AGV
  private void synthesisAGVB() throws Exception
  {
    runModel("tests", "incremental_suite", "agvb.wmod");
  }

  private void synthesisAGV() throws Exception
  {
    runModel("tests", "incremental_suite", "agv.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseIPC() throws Exception
  {
    runModel("tests", "synthesis", "IPC.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("tests", "hisc", "rhone_subsystem1_patch0.wmod");
  }

  private void synthesisFms2003() throws Exception
  {
    runModel("tests", "fms2003", "fms2003.wmod");
  }

  // PSL
  private void synthesisePSLBig() throws Exception
  {
    runModel("tests", "psl", "pslBig.wmod");
  }

  private void synthesisePSLBigWithManyRestartTrans() throws Exception
  {
    runModel("tests", "psl", "pslBigWithManyRestartTrans.wmod");
  }

  private void synthesisePSLWithResetTrans() throws Exception
  {
    runModel("tests", "psl", "pslWithResetTrans.wmod");
  }

  private void synthesisePSLWithResetTransWithPartLeftCounters() throws Exception
  {
    runModel("tests", "psl", "pslWithResetTransWithPartLeftCounters.wmod");
  }

  private void synthesisePSLWithResetTransWithPartLeftPlants() throws Exception
  {
    runModel("tests", "psl", "pslWithResetTransWithPartLeftPlants.wmod");
  }

  //flexible production cell
  @SuppressWarnings("unused")
  private void synthesiseFischertechnik() throws Exception
  {
    runModel("tests", "incremental_suite", "ftechnik.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisTip3Bad() throws Exception
  {
    runModel("tip", "acsw2006", "tip3_bad.wmod");
  }

  private void synthesisFenCaiWon09B() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09b.wmod");
  }

  private void synthesisFenCaiWon09Synth() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09s.wmod");
  }

  private void synthesis6linka() throws Exception
  {
    runModel("tests", "6link", "6linka.wmod");
  }

  private void synthesis6linki() throws Exception
  {
    runModel("tests", "6link", "6linki.wmod");
  }

  private void synthesis6linkp() throws Exception
  {
    runModel("tests", "6link", "6linkp.wmod");
  }

  private void synthesis6linkre() throws Exception
  {
    runModel("tests", "6link", "6linkre.wmod");
  }

  @SuppressWarnings("unused")
  private boolean synthesisTransferline(final int n) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    return runModel("handwritten", null, "transferline_uncont1.wmod", bindings);
  }


  //#########################################################################
  //# Logging
  private void printAndLog(final String msg, final boolean newline)
  {
    if (newline) {
      System.out.println(msg);
    } else {
      System.out.print(msg);
      System.out.flush();
    }
    getLogger().info(msg);
  }


  //#########################################################################
  //# Data Members
  private ModularAndCompositionalSynthesizer mSynthesizer;
  private AbstractCompositionalModelAnalyzer.PreselectingMethod mPreselecting;
  private SelectionHeuristicCreator mSelecting;
  private final Watchdog mWatchdog;
  private int mTimeout = 600;

  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;
}
