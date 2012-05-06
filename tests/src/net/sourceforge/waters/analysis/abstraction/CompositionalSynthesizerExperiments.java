//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * This class runs experiments using the {@link CompositionalSynthesizer} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */

public class CompositionalSynthesizerExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public CompositionalSynthesizerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null, null);
  }

  public CompositionalSynthesizerExperiments
    (final String statsFilename,
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
    mPrintWriter.println("InternalStateLimit," + internalStateLimit +
                         ",InternalTransitionLimit," +
                         internalTransitionLimit +
                         ",FinalStateLimit," + finalStateLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting +
                         ",SelecHeuristic," + mSelecting);
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


  //#########################################################################
  //# Invocation
  void runModel(final String group,
                final String subdir,
                final String name)
  throws Exception
  {
    runModel(group, subdir, name, null);
  }

  void runModel(final String group,
                final String subdir,
                final String name,
                final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    printAndLog("Running " + name + " with " +
                mPreselecting + "/" + mSelecting + " ...");
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
    mSynthesizer.setUsedAbstractionMethods
    (CompositionalSynthesizer.USE_SOE);
    try {
      mSynthesizer.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final CompositionalSynthesisResult stats =
        (CompositionalSynthesisResult) mSynthesizer.getAnalysisResult();
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
        final String filename = args[0];
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        final CompositionalSynthesizerExperiments experiment =
          new CompositionalSynthesizerExperiments(filename);
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
      System.err.println
        ("Usage: CompositionalSynthesizerExperiments " +
         "<outputFilename> <preselectingHeuristic> <selectingHeuristic>");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    /*
    synthesisTransferline(100);
    synthesisTransferline(200);
    synthesisTransferline(300);
    */
//    synthesiseTbedNoderailB();
//    synthesiseCentralLockingKoordwspBlock();
    synthesisAGV();
    synthesisAGVB();
    synthesissAip0Alps();
    synthesissRhoneSubPatch0();
//    synthesissAip0Aip();
    synthesisFenCaiWon09B();
//    synthesisFenCaiWon09Synth();
    synthesissFms2003();
    synthesiseFischertechnik();
    synthesiseIPC();
    synthesiseCentralLockingKoordwspBlock();
//    synthesisAip0tough();
//    synthesiseTbedCtct();
    synthesiseTbedNoderailUncont();
    synthesiseTbedNoderailB();
//    synthesiseCentralLockingVerriegel3b();
//    synthesiseVerrigel4B();
//    synthesiseFlexibleManufacturingSystem();
//  synthesisLargestCoherent();
  }


  //#########################################################################
  //# Models
  // Central locking
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "synthesis_experiment", "koordwsp_block.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "synthesis_experiment", "verriegel3b.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseVerrigel4B() throws Exception
  {
    runModel("valid", "synthesis_experiment", "verriegel4b.wmod");
  }

  // AIP
  private void synthesissAip0Alps() throws Exception
  {
    runModel("valid", "synthesis_experiment", "aip0alps.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseAip0Aip() throws Exception
  {
    runModel("valid", "synthesis_experiment", "aip0aip.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisFenCaiWon09Synth() throws Exception
  {
    runModel("valid", "synthesis_experiment", "FenCaiWon09_synth.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisLargestCoherent() throws Exception
  {
    runModel("valid", "synthesis_experiment", "largest_coherent.wmod");
  }

  // Train testbed
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_uncont.wmod");
  }

  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_noderail_block.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseTbedCtct() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_ctct.wmod");
  }

  //AGV
  private void synthesisAGVB() throws Exception
  {
    runModel("valid", "synthesis_experiment", "agvb.wmod");
  }

  private void synthesisAGV() throws Exception
  {
    runModel("valid", "synthesis_experiment", "agv.wmod");
  }

  //
  private void synthesiseIPC() throws Exception
  {
    runModel("valid", "synthesis_experiment", "IPC.wmod");
  }

  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("valid", "synthesis_experiment", "rhone_subsystem1_patch0.wmod");
  }

  private void synthesissFms2003() throws Exception
  {
    runModel("valid", "synthesis_experiment", "fms2003_synth1.wmod");
  }

  //flexible production cell
  private void synthesiseFischertechnik() throws Exception
  {
    runModel("valid", "synthesis_experiment", "ftechnik.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseFlexibleManufacturingSystem() throws Exception
  {
    runModel("valid", "synthesis_experiment", "flexible_man_sys.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisTip3Bad() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tip3_bad.wmod");
  }

  private void synthesisFenCaiWon09B() throws Exception
  {
    runModel("valid", "synthesis_experiment", "FenCaiWon09b.wmod");
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
    runModel("handwritten", null, "transferline.wmod", bindings);
    final long stop = System.currentTimeMillis();
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
  private boolean mHasBeenPrinted;

  private AbstractCompositionalModelAnalyzer.PreselectingMethod
    mPreselecting;
  private AbstractCompositionalModelAnalyzer.SelectingMethod mSelecting;
}
