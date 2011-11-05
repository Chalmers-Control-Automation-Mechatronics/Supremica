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
    mOut = new FileOutputStream(statsFilename);
    mPrintWriter = null;
    mPreselecting = preselectingHeuristic;
    mSelecting = selectingHeuristic;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = new CompositionalSynthesizer(factory);
    mPrintWriter = new PrintWriter(mOut, true);
    final int internalStateLimit = 5000;
    mSynthesizer.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 2000000;
    mSynthesizer.setMonolithicStateLimit(finalStateLimit);
    final int finalTransitionLimit = 0;
    mSynthesizer.setMonolithicTransitionLimit(finalTransitionLimit);
    mSynthesizer.setPreselectingMethod(mPreselecting);
    mSynthesizer.setSelectingMethod(mSelecting);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit +
                         ",InternalTransitionLimit," +
                         internalTransitionLimit +
                         ",FinalStateLimit," + finalStateLimit +
                         ",FinalTransitionLimit," + finalTransitionLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting +
                         ",SelecHeuristic," + mSelecting);

    // TODO This may fail to print all headings. May have to print the
    // headings from the analysis result obtained from the first test.
    final CompositionalSynthesisResult stats =
      new CompositionalSynthesisResult();
    mPrintWriter.print("Model,");
    stats.printCSVHorizontalHeadings(mPrintWriter);
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
  void runModel(final String group, final String subdir, final String name)
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
    } catch (final Exception e) {
      System.out.print(e.getMessage());
      mPrintWriter.println(name + "," + e.getMessage());
    } finally {
      final CompositionalSynthesisResult stats =
        (CompositionalSynthesisResult) mSynthesizer.getAnalysisResult();
      stats.printCSVHorizontalHeadings(mPrintWriter);
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
    synthesiseCentralLockingDritueren();
    synthesiseCentralLockingKoordwsp();
    synthesiseCentralLockingKoordwspBlock();
    synthesiseCentralLockingVerriegel3();
    synthesiseCentralLockingVerriegel3b();
    synthesiseFischertechnik();
    synthesisAssemblyStation1();
    synthesisLargetCoherent();
    synthesisTransportUnit1();
  }


  //#########################################################################
  //# Models
  private void synthesiseCentralLockingDritueren() throws Exception
  {
    runModel("valid", "central_locking", "dreitueren.wmod");
  }

  private void synthesiseCentralLockingKoordwsp() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp.wmod");
  }

  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp_block.wmod");
  }

  private void synthesiseCentralLockingVerriegel3() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3.wmod");
  }

  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3b.wmod");
  }

  private void synthesiseFischertechnik() throws Exception
  {
    runModel("valid", "fischertechnik", "fischertechnik.wmod");
  }

  private void synthesisAssemblyStation1() throws Exception
  {
    runModel("valid", "AIP", "assembly_station1.wmod");
  }

  private void synthesisLargetCoherent() throws Exception
  {
    runModel("valid", "AIP", "larget_coherent.wmod");
  }

  private void synthesisTransportUnit1() throws Exception
  {
    runModel("valid", "AIP", "transport_unit1.wmod");
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
