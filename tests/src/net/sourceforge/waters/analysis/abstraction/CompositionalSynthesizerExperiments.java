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

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
  void runModel(final String group, final String subdir, final String name,
                final boolean hasBeenPrinted)
  throws Exception
  {
    System.out.println("Running " + name + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    final File groupdir = new File(rootdir, group);
    final File dir = new File(groupdir, subdir);
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, null);
    mSynthesizer.setModel(des);
    try {
      mSynthesizer.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final CompositionalSynthesisResult stats =
        (CompositionalSynthesisResult) mSynthesizer.getAnalysisResult();
      if (!hasBeenPrinted) {
        mPrintWriter.print("Model,");
        stats.printCSVHorizontalHeadings(mPrintWriter);
        mPrintWriter.println();
      }
      mPrintWriter.print(name);
      mPrintWriter.print(',');
      stats.printCSVHorizontal(mPrintWriter);
      mPrintWriter.println();
      // TODO Format everything properly.
    }
  }

  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 3) {
      try {
        final String filename = args[0];
        final String outputprop = System.getProperty("waters.test.outputdir");
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        final CompositionalSynthesizerExperiments
          experiment =
            new CompositionalSynthesizerExperiments
              (outputprop + filename);
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
      System.err
      .println("Usage: CompositionalGeneralisedSynthesizerCheckerExperiments "
      + "outputFilename preselectingHeuristic selectingHeuristic " +
      "listOfRulesSelection");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {

//    synthesiseTbedNoderailB();
//    synthesiseCentralLockingKoordwspBlock();
//    synthesiseCentralLockingVerriegel3b();
//    synthesiseVerrigel4B();
//    synthesissAip0Alps();
//    synthesissAip0Aip();
//    synthesisAip0tough();
//    synthesiseTbedCtct();
    synthesiseTbedNoderailB();
//    synthesiseTbedNoderailUncont();
//    synthesiseFischertechnik();
//    synthesisLargestCoherent();
//    synthesisTip3Bad();
    synthesisFenCaiWon09B();
    synthesisAGVB();
    synthesisAGV();
    synthesissRhoneSubPatch0();
    synthesissFms2003();
    synthesiseIPC();
    synthesiseFlexibleManufacturingSystem();

  }


  //#########################################################################
  //# Models
  // Central locking
  @SuppressWarnings("unused")
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "synthesis_experiment", "koordwsp_block.wmod", false);
  }

  @SuppressWarnings("unused")
  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "synthesis_experiment", "verriegel3b.wmod", false);
  }

  @SuppressWarnings("unused")
  private void synthesiseVerrigel4B() throws Exception
  {
    runModel("valid", "synthesis_experiment", "verriegel4b.wmod", false);
  }

  // AIP
  @SuppressWarnings("unused")
  private void synthesissAip0Alps() throws Exception
  {
    runModel("valid", "synthesis_experiment", "aip0alps.wmod", false);
  }

  @SuppressWarnings("unused")
  private void synthesissAip0Aip() throws Exception
  {
    runModel("valid", "synthesis_experiment", "aip0aip.wmod", false);
  }

  @SuppressWarnings("unused")
  private void synthesisAip0tough() throws Exception
  {
    runModel("valid", "synthesis_experiment", "aip0tough.wmod", false);
  }

  @SuppressWarnings("unused")
  private void synthesisLargestCoherent() throws Exception
  {
    runModel("valid", "synthesis_experiment", "largest_coherent.wmod", false);
  }

  // Train testbed
  @SuppressWarnings("unused")
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_uncont.wmod", false);
  }

  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_noderail_block.wmod",
                                                                        false);
  }

  @SuppressWarnings("unused")
  private void synthesiseTbedCtct() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tbed_ctct.wmod", false);
  }

  //AGV
  private void synthesisAGVB() throws Exception
  {
    runModel("valid", "synthesis_experiment", "agvb.wmod", true);
  }

  private void synthesisAGV() throws Exception
  {
    runModel("valid", "synthesis_experiment", "agv.wmod", true);
  }

  //
  private void synthesiseIPC() throws Exception
  {
    runModel("valid", "synthesis_experiment", "IPC.wmod", true);
  }

  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("valid", "synthesis_experiment", "rhone_subsystem1_patch0.wmod",
                                                                       true);
  }

  private void synthesissFms2003() throws Exception
  {
    runModel("valid", "synthesis_experiment", "fms2003_synth1.wmod", true);
  }

  //flexible production cell
  @SuppressWarnings("unused")
private void synthesiseFischertechnik() throws Exception
  {
    runModel("valid", "synthesis_experiment", "ftechnik.wmod", true);
  }

  private void synthesiseFlexibleManufacturingSystem() throws Exception
  {
    runModel("valid", "synthesis_experiment", "flexible_man_sys.wmod", true);
  }

  @SuppressWarnings("unused")
  private void synthesisTip3Bad() throws Exception
  {
    runModel("valid", "synthesis_experiment", "tip3_bad.wmod", false);
  }

  private void synthesisFenCaiWon09B() throws Exception
  {
    runModel("valid", "synthesis_experiment", "FenCaiWon09b.wmod", false);
  }


  //#########################################################################
  //# Data Members
  private CompositionalSynthesizer mSynthesizer;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;

  private AbstractCompositionalModelAnalyzer.PreselectingMethod
    mPreselecting;
  private AbstractCompositionalModelAnalyzer.SelectingMethod mSelecting;
}
