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
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * This class runs experiments using the {@link CompositionalSynthesizer} with
 * a variety of configurations. The heuristics for choosing candidates can be
 * varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */

public class CompositionalSynthesizerExperiments extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public CompositionalSynthesizerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null, null);
  }

  public CompositionalSynthesizerExperiments(final String statsFilename,
                                             final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingHeuristic,
                                             final AbstractCompositionalModelAnalyzer.SelectingMethod selectingHeuristic)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mPreselecting = preselectingHeuristic;
    mSelecting = selectingHeuristic;
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = new CompositionalSynthesizer(factory);
    watchdog = new Watchdog(mSynthesizer, 600);
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
    final int finalStateLimit = 2000000;
    mSynthesizer.setMonolithicStateLimit(finalStateLimit);
    mSynthesizer.setPreselectingMethod(mPreselecting);
    mSynthesizer.setSelectingMethod(mSelecting);
    mSynthesizer.setSupervisorReductionEnabled(mSupervisorReductionEnabled);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit
                         + ",InternalTransitionLimit,"
                         + internalTransitionLimit + ",FinalStateLimit,"
                         + finalStateLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting
                         + ",SelecHeuristic," + mSelecting);
    mPrintWriter.println("SupervisorReduction," + mSupervisorReductionEnabled);
    mHasBeenPrinted = false;
  }

  @Override
  protected void tearDown() throws Exception
  {
    mSynthesizer = null;
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }

  //#########################################################################
  //# Simple Access
  CompositionalSynthesizer getSynthesizer()
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
    final AbstractCompositionalModelAnalyzer.SelectingMethodFactory factory =
      mSynthesizer.getSelectingMethodFactory();
    mSelecting = factory.getEnumValue(name);
  }

  void setSupervisorReductionEnabled(final boolean enabled)
  {
    mSupervisorReductionEnabled = enabled;
  }

  //#########################################################################
  //# Invocation
  void runModel(final String group, final String subdir, final String name)
    throws Exception
  {
    runModel(group, subdir, name, null);
  }

  void runModel(final String group, final String subdir, final String name,
                final List<ParameterBindingProxy> bindings) throws Exception
  {
    printAndLog("Running " + name + " with " + mPreselecting + "/"
                + mSelecting + " ...");
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
    /*
    mSynthesizer
      .setAbstractionProcedureFactory(SynthesisAbstractionProcedureFactory.SOE_ONLY);
    */
    try {
      watchdog.reset();
      mSynthesizer.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final CompositionalSynthesisResult stats =
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
    if (args.length == 4) {
      try {
        final String filename = args[0];
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        final int enabled = Integer.parseInt(args[3]);
        final boolean supervisorReductionEnabled =
          (enabled == 0) ? false : true;
        final CompositionalSynthesizerExperiments experiment =
          new CompositionalSynthesizerExperiments(filename);
        experiment.setPreselectingHeuristic(preselectingHeuristic);
        experiment.setSelectingHeuristic(selectingHeuristic);
        experiment.setSupervisorReductionEnabled(supervisorReductionEnabled);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err
        .println("Usage: CompositionalSynthesizerExperiments "
                 + "<outputFilename> <preselectingHeuristic> <selectingHeuristic>");
    }
  }

  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    watchdog.start();
    synthesisAGV();// 1
    synthesisAGVB();// 2
    synthesissAip0Alps();// 3
    synthesisFenCaiWon09B();// 4
    synthesisFenCaiWon09Synth();// 5
    synthesissFms2003();// 6
    synthesiseTbedNoderailB();// 7
    synthesiseTbedNoderailUncont();// 8
    synthesiseCentralLockingVerriegel3b();// 9
    synthesiseVerrigel4B();// 10
    synthesis6linka();// 11
    synthesis6linki();// 12
    synthesis6linkp();// 13
    synthesis6linkre();// 14


    //synthesisTransferline(100);
    //synthesisTransferline(200);
    //synthesisTransferline(300);
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

  // Train testbed
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_uncont.wmod");
  }

  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_noderail_block.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseTbedCtct() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_ctct.wmod");
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

  private void synthesissFms2003() throws Exception
  {
    runModel("tests", "fms2003", "fms2003.wmod");
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
    runModel("tests", "fencaiwon09", "FenCaiWon09_synth.wmod");
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
  private void synthesisTransferline(final int n) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final long start = System.currentTimeMillis();
    runModel("handwritten", null, "transferline_uncont.wmod", bindings);
    final long stop = System.currentTimeMillis();
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(System.out);
    final float difftime = 0.001f * (stop - start);
    formatter.format("%.3f s\n", difftime);
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
  private CompositionalSynthesizer mSynthesizer;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private final Watchdog watchdog;
  private boolean mHasBeenPrinted;
  private boolean mSupervisorReductionEnabled;

  private AbstractCompositionalModelAnalyzer.PreselectingMethod mPreselecting;
  private AbstractCompositionalModelAnalyzer.SelectingMethod mSelecting;
}
